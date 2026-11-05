$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
if (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue) { throw 'Port 8080 occupied' }
$savedKey = $env:PRACTICAL_AI_MASTER_KEY
$savedRoot = $env:TEXTBOOK_ROOT
try {
    # 沿用本机主密钥，避免验收进程无法读取已有教师配置。
    $entries = @(Get-Content -LiteralPath (Join-Path $projectRoot 'contexts/secrets.local.md') -Encoding UTF8 | Where-Object { $_ -match '^\s*-\s*PRACTICAL_AI_MASTER_KEY' })
    if ($entries.Count -ne 1) { throw 'Master key entry unavailable' }
    $match = [regex]::Match($entries[0], 'PRACTICAL_AI_MASTER_KEY\s*[:：=]\s*`?([^`\s]+)')
    if (!$match.Success) { throw 'Master key entry invalid' }
    $env:PRACTICAL_AI_MASTER_KEY = $match.Groups[1].Value
    $env:TEXTBOOK_ROOT = Join-Path $projectRoot 'output/textbook-pack-20261029/pages'
    $jar = Join-Path $projectRoot 'RuoYi-Vue/ruoyi-admin/target/ruoyi-admin.jar'
    $process = Start-Process -FilePath 'C:/Users/PC/.jdks/corretto-1.8.0_422/bin/java.exe' -ArgumentList '-Xms512m','-Xmx2048m','-Dserver.port=8080','-Dspring.quartz.auto-startup=false','-jar',('"'+$jar+'"') -WorkingDirectory $projectRoot -WindowStyle Hidden -RedirectStandardOutput (Join-Path $projectRoot 'output/textbook-cases/backend.stdout.log') -RedirectStandardError (Join-Path $projectRoot 'output/textbook-cases/backend.stderr.log') -PassThru
    $process.Id | Set-Content -LiteralPath (Join-Path $projectRoot 'output/textbook-cases/backend.pid')
    Write-Output "Case test backend starting PID=$($process.Id), port=8080"
} finally {
    $env:PRACTICAL_AI_MASTER_KEY = $savedKey
    $env:TEXTBOOK_ROOT = $savedRoot
}
