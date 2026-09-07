param([Parameter(Mandatory=$true)][string]$RequestFile)
# 由计划任务独立运行，SSH 断开不会打断备份、切换或回滚。
$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
$root = 'D:\program\3009dazipingtai'
$reg = 'HKLM:\SYSTEM\CurrentControlSet\Services\NewDaziBackend3009\Parameters'
$nginxHome = 'D:\programsoftware\nginx\nginx-1.29.4'
$nginxConf = Join-Path $nginxHome 'conf\nginx.conf'
$utf8 = New-Object Text.UTF8Encoding($false)
$request = Get-Content -LiteralPath $RequestFile -Raw -Encoding UTF8 | ConvertFrom-Json
$job = Split-Path $RequestFile
$script:phase = 'initializing'
$script:switched = $false
$script:backup = $null
$mutex = New-Object Threading.Mutex($false, 'Global\NewDaziProductionDeploy')
$locked = $false

function Save-State($status, $detail) {
    $state = @{status=$status;phase=$script:phase;detail=$detail;release=$request.release;time=(Get-Date).ToString('s');backup=$script:backup}
    $temp = Join-Path $job 'state.tmp'
    [IO.File]::WriteAllText($temp, ($state | ConvertTo-Json -Depth 8), $utf8)
    Move-Item -LiteralPath $temp -Destination (Join-Path $job 'state.json') -Force
}
function Phase($name) { $script:phase=$name; Save-State 'running' '' }
function Hash($path) { (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash }
function Assert-Release($name) {
    if ($name -notmatch '^[A-Za-z0-9][A-Za-z0-9_-]{2,100}$') { throw 'invalid_release_name' }
    Join-Path (Join-Path $root 'releases') $name
}
function Run-Exe($exe, $arguments, $seconds, $label) {
    $stdout=Join-Path $job ($label+'.stdout');$stderr=Join-Path $job ($label+'.stderr')
    $p=Start-Process -FilePath $exe -ArgumentList $arguments -PassThru -WindowStyle Hidden -RedirectStandardOutput $stdout -RedirectStandardError $stderr
    # Windows PowerShell 5.1 必须持有句柄，进程退出后才能可靠获取 ExitCode。
    $handle=$p.Handle
    if (-not $p.WaitForExit($seconds*1000)) {
        # 只终止本脚本启动且仍由此 Process 对象持有的工具，绝不按 Java/进程名称批量杀。
        $p.Kill();$p.WaitForExit();throw ('timeout_'+$label)
    }
    $p.Refresh()
    if ($p.ExitCode -ne 0) { throw ('command_failed_'+$label) }
}
function Test-Nginx($file) {
    # 相对 include 以配置文件目录解析，候选必须放在现有 conf 目录验证。
    $check=Join-Path $nginxHome ('conf\deploy-check-'+$request.jobId+'.conf')
    Copy-Item -LiteralPath $file -Destination $check
    try {Run-Exe (Join-Path $nginxHome 'nginx.exe') @('-t','-p',('"'+$nginxHome.Replace('\','/')+'/"'),'-c',('"'+$check.Replace('\','/')+'"')) 15 'nginx-test'}
    finally {Remove-Item -LiteralPath $check -ErrorAction SilentlyContinue}
}
function Reload-Nginx {
    # 以 SYSTEM 运行，与 NSSM 的 Nginx 同账户，不重启共享 80/3012 服务。
    Run-Exe (Join-Path $nginxHome 'nginx.exe') @('-s','reload','-p',('"'+$nginxHome.Replace('\','/')+'/"')) 15 'nginx-reload'
}
function Get-Live {
    $p=Get-ItemProperty $reg
    $conf=[IO.File]::ReadAllText($nginxConf)
    $matches=[regex]::Matches($conf,'(?m)^\s*root\s+(D:/program/3009dazipingtai/releases/[^;]+/frontend)\s*;')
    if ($matches.Count -ne 1) { throw 'ambiguous_platform_frontend_root' }
    $dir=[IO.Path]::GetFullPath($p.AppDirectory)
    if (-not $dir.StartsWith(($root+'\releases\'),[StringComparison]::OrdinalIgnoreCase)) { throw 'unexpected_live_directory' }
    if ($p.AppParameters -ne '-Xms512m -Xmx2048m -jar backend/ruoyi-admin.jar') { throw 'unexpected_backend_arguments' }
    @{directory=$dir;frontend=$matches[0].Groups[1].Value;jarHash=(Hash (Join-Path $dir 'backend\ruoyi-admin.jar'));indexHash=(Hash (Join-Path $matches[0].Groups[1].Value 'index.html'));nginxHash=(Hash $nginxConf);environmentCount=@($p.AppEnvironmentExtra).Count}
}
function Test-Artifacts($dir,$manifest) {
    if ((Hash (Join-Path $dir 'backend\ruoyi-admin.jar')) -ne $manifest.jarSha256) {throw 'jar_hash_mismatch'}
    if ((Hash (Join-Path $dir 'frontend.zip')) -ne $manifest.frontendZipSha256) {throw 'frontend_zip_hash_mismatch'}
    $front=Join-Path $dir 'frontend'
    foreach($item in $manifest.frontendFiles.PSObject.Properties) {
        if($item.Name -match '(^/|\\|(^|/)\.\.(/|$)|:)'){throw 'unsafe_frontend_path'}
        if((Hash (Join-Path $front $item.Name)) -ne $item.Value){throw 'frontend_file_hash_mismatch'}
    }
    if(@(Get-ChildItem $front -Recurse -File).Count -ne @($manifest.frontendFiles.PSObject.Properties).Count){throw 'unexpected_frontend_files'}
}
function Set-DatabaseEnv($directory) {
    # 仅支持当前已验证的单 master 外置配置；遇到多数据源或环境覆盖直接中止。
    $t=[IO.File]::ReadAllText((Join-Path $directory 'config\application-druid.yml'))
    $passwords=[regex]::Matches($t,'(?m)^\s*password:\s*(.+)$')
    if($passwords.Count -ne 1 -or $t -notmatch 'jdbc:mysql://127\.0\.0\.1:3306/ry-vue\?') {throw 'unsupported_database_config'}
    if($t -notmatch '(?m)^\s*username:\s*root\s*$'){throw 'unexpected_database_user'}
    $env:MYSQL_PWD=$passwords[0].Groups[1].Value.Trim().Trim('"').Trim("'")
}
function Backup($live) {
    $script:backup=Join-Path (Join-Path $root 'backups') $request.jobId
    if(Test-Path $script:backup){throw 'backup_already_exists'}
    New-Item -ItemType Directory $script:backup | Out-Null
    & icacls.exe $script:backup /inheritance:r /grant:r '*S-1-5-18:(OI)(CI)F' '*S-1-5-32-544:(OI)(CI)F' | Out-Null
    if($LASTEXITCODE -ne 0){throw 'backup_acl_failed'}
    Copy-Item -LiteralPath $nginxConf -Destination (Join-Path $script:backup 'nginx.conf.before')
    Copy-Item -LiteralPath (Join-Path $live.directory 'config') -Destination (Join-Path $script:backup 'config') -Recurse
    $p=Get-ItemProperty $reg
    # CLIXML 保留 REG_MULTI_SZ 数组，避免曾发生的环境变量压成单行事故。
    @{AppDirectory=$p.AppDirectory;AppStdout=$p.AppStdout;AppStderr=$p.AppStderr;AppEnvironmentExtra=@($p.AppEnvironmentExtra)} | Export-Clixml (Join-Path $script:backup 'backend-before.xml')
    [IO.File]::WriteAllText((Join-Path $script:backup 'live-before.json'),($live|ConvertTo-Json),$utf8)
    Set-DatabaseEnv $live.directory
    try {
        $dump=Join-Path $script:backup 'ry-vue.sql'
        Run-Exe 'D:\MySQL\MySQL Server 8.0\bin\mysqldump.exe' @('--host=127.0.0.1','--user=root','--single-transaction','--quick','--routines','--triggers','--events','--hex-blob','--no-tablespaces','--set-gtid-purged=OFF','--default-character-set=utf8mb4',('--result-file="'+$dump+'"'),'ry-vue') 240 'database-backup'
        if((Get-Item $dump).Length -lt 1024 -or -not ((Get-Content $dump -Tail 6) -match 'Dump completed on')){throw 'incomplete_database_backup'}
        $e=@{database='ry-vue';bytes=(Get-Item $dump).Length;sha256=(Hash $dump);nginxSha256=(Hash (Join-Path $script:backup 'nginx.conf.before'));previous=$live}
        [IO.File]::WriteAllText((Join-Path $script:backup 'evidence.json'),($e|ConvertTo-Json -Depth 6),$utf8)
    } finally {Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue}
}
function Stop-Backend {
    $service=Get-Service 'NewDaziBackend3009'
    if($service.Status -eq 'Stopped'){return}
    $service.Stop()
    try {$service.WaitForStatus('Stopped',[TimeSpan]::FromSeconds(45))} catch {throw 'backend_stop_timeout_no_global_kill'}
}
function Start-Backend {
    $service=Get-Service 'NewDaziBackend3009';$service.Refresh()
    if($service.Status -eq 'StopPending'){$service.WaitForStatus('Stopped',[TimeSpan]::FromSeconds(45));$service.Refresh()}
    if($service.Status -eq 'Stopped'){$service.Start()}
    $service.WaitForStatus('Running',[TimeSpan]::FromSeconds(30))
}
function Health($expectedIndex,$seconds) {
    $end=(Get-Date).AddSeconds($seconds)
    do {
        try {
            $captcha=Invoke-RestMethod 'http://127.0.0.1:3009/captchaImage' -TimeoutSec 6
            if($captcha.code -ne 200){throw 'captcha_not_ready'}
            $headers=@{Authorization=('Bearer '+$request.healthToken)}
            $info=Invoke-RestMethod 'http://127.0.0.1:3010/prod-api/getInfo' -Headers $headers -TimeoutSec 6
            if($info.code -ne 200 -or -not $info.user.userId){throw 'database_auth_not_ready'}
            $health=Invoke-RestMethod 'http://127.0.0.1:3010/prod-api/business/collaboration/health' -Headers $headers -TimeoutSec 6
            if($health.code -ne 200 -or $health.data.ready -ne $true){throw 'integration_not_ready'}
            $html=Invoke-WebRequest 'http://127.0.0.1:3010/' -UseBasicParsing -TimeoutSec 6
            $sha=[Security.Cryptography.SHA256]::Create()
            $actual=[BitConverter]::ToString($sha.ComputeHash($html.RawContentStream.ToArray())).Replace('-','')
            if($actual -ne $expectedIndex){throw 'served_index_mismatch'}
            foreach($url in @('http://127.0.0.1/','http://127.0.0.1:3012/','http://127.0.0.1:3018/api/config','http://127.0.0.1:3019/')){
                if((Invoke-WebRequest $url -UseBasicParsing -TimeoutSec 6).StatusCode -ne 200){throw 'shared_gateway_unhealthy'}
            }
            return
        } catch {if((Get-Date) -ge $end){throw 'health_timeout'};Start-Sleep -Seconds 3}
    } while((Get-Date) -lt $end)
    throw 'health_timeout'
}
function Restore($backup) {
    $prior=Get-Content (Join-Path $backup 'live-before.json') -Raw | ConvertFrom-Json
    $saved=Import-Clixml (Join-Path $backup 'backend-before.xml')
    if((Hash (Join-Path $prior.directory 'backend\ruoyi-admin.jar')) -ne $prior.jarHash){throw 'rollback_jar_changed'}
    if((Hash (Join-Path $prior.frontend 'index.html')) -ne $prior.indexHash){throw 'rollback_frontend_changed'}
    Test-Nginx (Join-Path $backup 'nginx.conf.before')
    Stop-Backend
    foreach($key in @('AppDirectory','AppStdout','AppStderr')) {Set-ItemProperty $reg $key $saved[$key]}
    # 不改环境变量；若外部并发改动则拒绝覆盖他人设置。
    $current=@((Get-ItemProperty $reg).AppEnvironmentExtra)
    if(($current -join "`n") -cne ($saved.AppEnvironmentExtra -join "`n")){throw 'environment_drift_during_rollback'}
    Copy-Item -LiteralPath (Join-Path $backup 'nginx.conf.before') -Destination $nginxConf -Force
    Start-Backend
    Reload-Nginx
    Health $prior.indexHash 150
}

try {
    $locked=$mutex.WaitOne(0)
    if(-not $locked){throw 'another_deployment_running'}
    $dir=Assert-Release $request.release
    Phase 'preflight'
    $live=Get-Live
    if($request.action -eq 'rollback') {
        $script:backup=[IO.Path]::GetFullPath($request.backup)
        if(-not $script:backup.StartsWith(($root+'\backups\'),[StringComparison]::OrdinalIgnoreCase)){throw 'invalid_backup_path'}
        if($live.directory -ne $dir){throw 'rollback_target_is_not_current_release'}
        Phase 'rolling-back';Restore $script:backup;Save-State 'rolled-back' 'application_only_database_untouched'
    } else {
        $manifest=Get-Content (Join-Path $dir 'release-manifest.json') -Raw -Encoding UTF8 | ConvertFrom-Json
        Test-Artifacts $dir $manifest
        if($live.directory -eq $dir -and $live.indexHash -eq $manifest.indexSha256){Health $manifest.indexSha256 30;Save-State 'succeeded' 'already_active_no_restart';exit 0}
        if($live.jarHash -ne $manifest.baselineSha256 -or $live.indexHash -ne $manifest.baselineIndexSha256){throw 'baseline_drift'}
        if(@((Get-ItemProperty $reg).AppEnvironmentExtra).Count -ne 20){throw 'unexpected_environment_count'}
        Health $live.indexHash 30
        foreach($s in @('NewDaziBackend3009','UnifiedNginx')){if((Get-Service $s).Status -ne 'Running'){throw 'baseline_service_not_running'}}
        $candidate=Join-Path $job 'nginx.candidate.conf'
        $text=[IO.File]::ReadAllText($nginxConf)
        $newRoot=$dir.Replace('\','/')+'/frontend'
        $next=$text.Replace($live.frontend,$newRoot)
        if($next -eq $text -or $next.Replace($newRoot,$live.frontend) -cne $text){throw 'nginx_change_not_exact'}
        [IO.File]::WriteAllText($candidate,$next,$utf8)
        Test-Nginx $candidate
        if($request.action -eq 'check'){Save-State 'checked' 'hashes_baseline_services_auth_integration_nginx_ok';exit 0}
        Phase 'backup';Backup $live
        Phase 'prepare-config'
        foreach($f in Get-ChildItem (Join-Path $live.directory 'config') -Recurse -File) {
            $relative=$f.FullName.Substring((Join-Path $live.directory 'config').Length).TrimStart('\')
            $target=Join-Path (Join-Path $dir 'config') $relative
            if(Test-Path $target){if((Hash $target) -ne (Hash $f.FullName)){throw 'candidate_config_differs'}}
            else {New-Item -ItemType Directory (Split-Path $target) -Force|Out-Null;Copy-Item -LiteralPath $f.FullName -Destination $target}
        }
        if((Hash $nginxConf) -ne $live.nginxHash -or (Get-Live).directory -ne $live.directory){throw 'live_drift_before_switch'}
        New-Item -ItemType Directory (Join-Path $dir 'logs') -Force | Out-Null
        Phase 'switch-backend';$script:switched=$true
        Stop-Backend
        Set-ItemProperty $reg 'AppDirectory' $dir
        Set-ItemProperty $reg 'AppStdout' (Join-Path $dir 'logs\backend.stdout.log')
        Set-ItemProperty $reg 'AppStderr' (Join-Path $dir 'logs\backend.stderr.log')
        Start-Backend
        # 先让新后端服务恢复，再切换前端，减少不可用窗口。
        Phase 'warmup';Health $live.indexHash 150
        Phase 'switch-frontend';Copy-Item -LiteralPath $candidate -Destination $nginxConf -Force
        Reload-Nginx
        Phase 'health';Health $manifest.indexSha256 60
        $after=Get-Live
        if($after.jarHash -ne $manifest.jarSha256 -or $after.environmentCount -ne $live.environmentCount){throw 'post_switch_drift'}
        $saved=Import-Clixml (Join-Path $script:backup 'backend-before.xml')
        if((@((Get-ItemProperty $reg).AppEnvironmentExtra) -join "`n") -cne ($saved.AppEnvironmentExtra -join "`n")){throw 'environment_values_changed'}
        [IO.File]::WriteAllText((Join-Path $job 'live-after.json'),($after|ConvertTo-Json),$utf8)
        Save-State 'succeeded' 'backup_hashes_services_auth_database_integration_frontend_verified'
    }
} catch {
    $failure=$script:phase
    $reason=$_.Exception.GetType().Name+'@'+$_.InvocationInfo.ScriptLineNumber
    if($_.Exception.Message -match '^[a-z0-9_-]+$'){$reason=$_.Exception.Message}
    if($script:switched -and $script:backup){
        try {Phase 'automatic-rollback';Restore $script:backup;Save-State 'rolled-back' ('failed_at_'+$failure)}
        catch {Save-State 'rollback-failed' ('manual_recovery_required_after_'+$failure)}
    } else {Save-State 'failed' ('failed_at_'+$failure+':'+$reason)}
    exit 1
} finally {
    # 任务日志只存阶段，不记录 Token、数据库口令或原始异常。
    $request.healthToken=''
    [IO.File]::WriteAllText($RequestFile,($request|ConvertTo-Json -Depth 8),$utf8)
    if($locked){$mutex.ReleaseMutex()};$mutex.Dispose()
}
