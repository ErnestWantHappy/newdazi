$ErrorActionPreference='Stop'
$projectRoot=Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
if(Get-NetTCPConnection -LocalPort 18080 -State Listen -ErrorAction SilentlyContinue){throw '18080已占用，不启动重复实例'}
$entries=@(Get-Content -LiteralPath (Join-Path $projectRoot 'contexts/secrets.local.md') -Encoding UTF8 | Where-Object {$_ -match '^\s*-\s*PRACTICAL_AI_MASTER_KEY'})
if($entries.Count -ne 1){throw '本机密钥未就绪'}
$match=[regex]::Match($entries[0],'PRACTICAL_AI_MASTER_KEY\s*[:：=]\s*`?([^`\s]+)')
if(!$match.Success){throw '本机密钥未就绪'}
$previousKey=$env:PRACTICAL_AI_MASTER_KEY
$previousTextbook=$env:TEXTBOOK_ROOT
try{
    $env:PRACTICAL_AI_MASTER_KEY=$match.Groups[1].Value
    $env:TEXTBOOK_ROOT=Join-Path $projectRoot 'output/textbook-pack-20261029/pages'
    $jar=Join-Path $projectRoot 'RuoYi-Vue/ruoyi-admin/target/ruoyi-admin.jar'
    $log=Join-Path $projectRoot 'output/choice-media'
    $process=Start-Process -FilePath java -ArgumentList '-Xms512m','-Xmx2048m','-Dserver.port=18080','-jar',('"'+$jar+'"') -WorkingDirectory $projectRoot -WindowStyle Hidden -RedirectStandardOutput (Join-Path $log 'review-api.stdout.log') -RedirectStandardError (Join-Path $log 'review-api.stderr.log') -PassThru
    $process.Id | Set-Content -LiteralPath (Join-Path $log 'review-api.pid') -Encoding utf8
    Write-Output "本轮验收后端 PID=$($process.Id)，端口18080，未操作原8080服务"
}finally{$env:PRACTICAL_AI_MASTER_KEY=$previousKey;$env:TEXTBOOK_ROOT=$previousTextbook}
