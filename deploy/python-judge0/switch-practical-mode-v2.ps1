$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'

$root = 'D:\program\3009dazipingtai'
$backup = Join-Path $root 'backups\20260817_194500_python_practical_mode_v2'
$oldRelease = Join-Path $root 'releases\20260817_151500_python_judge0_v1'
$newRelease = Join-Path $root 'releases\20260817_194500_python_practical_mode_v2'
$archive = Join-Path $backup 'incoming\python-practical-mode-v2-20260817.zip'
$service = 'NewDaziBackend3009'
$nssm = (Get-CimInstance Win32_Process | Where-Object {
    $_.Name -eq 'nssm.exe' -and $_.ProcessId -eq (Get-CimInstance Win32_Process -Filter "Name='java.exe'" |
        Where-Object { $_.CommandLine -match '20260817_151500_python_judge0_v1' } |
        Select-Object -First 1 -ExpandProperty ParentProcessId)
} | Select-Object -First 1 -ExpandProperty ExecutablePath)

if ([string]::IsNullOrWhiteSpace($nssm)) { throw '未找到当前后端 NSSM 服务进程' }
if ((Test-Path $newRelease) -and @((Get-ChildItem $newRelease -Force)).Count -eq 0) {
    Remove-Item -LiteralPath $newRelease
}
if (Test-Path $newRelease) {
    if (!(Test-Path (Join-Path $newRelease 'backend\ruoyi-admin.jar')) -or !(Test-Path (Join-Path $newRelease 'frontend\index.html'))) {
        throw '目标 release 不完整，停止覆盖'
    }
} else {
    Expand-Archive -Path $archive -DestinationPath $newRelease -Force
}
Copy-Item (Join-Path $oldRelease 'config') (Join-Path $newRelease 'config') -Recurse -Force

$app = ((& $nssm get $service Application) -join '') -replace [char]0, ''
$args = ((& $nssm get $service AppParameters) -join '') -replace [char]0, ''
$dir = ((& $nssm get $service AppDirectory) -join '') -replace [char]0, ''
Set-Content -Path (Join-Path $backup 'nssm-application.before.txt') -Value $app -Encoding utf8
Set-Content -Path (Join-Path $backup 'nssm-parameters.before.txt') -Value $args -Encoding utf8
Set-Content -Path (Join-Path $backup 'nssm-directory.before.txt') -Value $dir -Encoding utf8

$nginx = 'D:\programsoftware\nginx\nginx-1.29.4'
$nginxConf = Join-Path $nginx 'conf\nginx.conf'
$candidate = Join-Path $nginx 'conf\nginx.20260817_194500_python_practical_mode_v2.candidate.conf'
Copy-Item $nginxConf (Join-Path $backup 'nginx.conf.before') -Force
$nginxText = [IO.File]::ReadAllText($nginxConf)
if (!$nginxText.Contains('20260817_151500_python_judge0_v1')) { throw 'Nginx 当前配置未指向预期旧 release，停止切换' }
Copy-Item $nginxConf $candidate -Force
$nginxText.Replace('20260817_151500_python_judge0_v1', '20260817_194500_python_practical_mode_v2') | Out-File -FilePath $candidate -Encoding ascii

Push-Location $nginx
cmd /c ('"' + (Join-Path $nginx 'nginx.exe') + '" -t -c "' + $candidate + '"') | Out-Null
if ($LASTEXITCODE -ne 0) { Pop-Location; throw 'Nginx 候选配置检查失败' }
Pop-Location

$newArgs = $args.Replace('20260817_151500_python_judge0_v1', '20260817_194500_python_practical_mode_v2')
$newDir = $dir.Replace('20260817_151500_python_judge0_v1', '20260817_194500_python_practical_mode_v2')
if ($newArgs -eq $args -or $newDir -eq $dir) { throw '后端服务路径未命中旧 release，停止切换' }

try {
    & $nssm set $service AppParameters $newArgs | Out-Null
    & $nssm set $service AppDirectory $newDir | Out-Null
    Restart-Service -Name $service -Force
    $backendReady = $false
    for ($i = 0; $i -lt 30; $i++) {
        try {
            if ((Invoke-WebRequest -UseBasicParsing -TimeoutSec 3 'http://127.0.0.1:3009/').StatusCode -eq 200) {
                $backendReady = $true
                break
            }
        } catch { }
        Start-Sleep -Seconds 2
    }
    if (!$backendReady) { throw '新后端未能在 60 秒内恢复 HTTP 200' }
} catch {
    & $nssm set $service AppParameters $args | Out-Null
    & $nssm set $service AppDirectory $dir | Out-Null
    Restart-Service -Name $service -Force
    throw
}

Copy-Item $candidate $nginxConf -Force
Push-Location $nginx
cmd /c ('"' + (Join-Path $nginx 'nginx.exe') + '" -t') | Out-Null
if ($LASTEXITCODE -ne 0) {
    Copy-Item (Join-Path $backup 'nginx.conf.before') $nginxConf -Force
    cmd /c ('"' + (Join-Path $nginx 'nginx.exe') + '" -s reload') | Out-Null
    Pop-Location
    throw 'Nginx 正式配置检查失败，已恢复旧配置'
}
cmd /c ('"' + (Join-Path $nginx 'nginx.exe') + '" -s reload') | Out-Null
if ($LASTEXITCODE -ne 0) {
    Copy-Item (Join-Path $backup 'nginx.conf.before') $nginxConf -Force
    cmd /c ('"' + (Join-Path $nginx 'nginx.exe') + '" -s reload') | Out-Null
    Pop-Location
    throw 'Nginx reload 失败，已恢复旧配置'
}
Pop-Location

Write-Output 'RELEASE_SWITCHED'
Write-Output ('RELEASE=' + $newRelease)
Write-Output ('BACKEND_STATUS=' + (Get-Service $service).Status)
Write-Output ('BACKEND_HTTP=' + (Invoke-WebRequest -UseBasicParsing -TimeoutSec 10 'http://127.0.0.1:3009/').StatusCode)
Write-Output ('FRONTEND_HTTP=' + (Invoke-WebRequest -UseBasicParsing -TimeoutSec 10 'http://127.0.0.1:3010/').StatusCode)

