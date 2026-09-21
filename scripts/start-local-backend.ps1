param([string]$JavaPath = 'java')
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$jarPath = Join-Path $projectRoot 'RuoYi-Vue/ruoyi-admin/target/ruoyi-admin.jar'
$secretPath = Join-Path $projectRoot 'contexts/secrets.local.md'
if (!(Test-Path -LiteralPath $jarPath)) { throw 'Backend JAR not found. Build ruoyi-admin first.' }
if (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue) {
    throw 'Port 8080 is already occupied. Stop the local backend before starting it.'
}
# Reuse existing master key to avoid decryption failure of teacher keys.
$entries = @(Get-Content -LiteralPath $secretPath -Encoding UTF8 | Where-Object { $_ -match '^\s*-\s*PRACTICAL_AI_MASTER_KEY' })
if ($entries.Count -ne 1) { throw 'Expected one PRACTICAL_AI_MASTER_KEY entry in local secrets.' }
$keyMatch = [regex]::Match($entries[0], 'PRACTICAL_AI_MASTER_KEY\s*[:：=]\s*`?([^`\s]+)')
if (!$keyMatch.Success -or $keyMatch.Groups[1].Value.Length -lt 24) { throw 'Invalid local master key entry.' }
$logPath = Join-Path $projectRoot 'output/local-backend'
New-Item -ItemType Directory -Path $logPath -Force | Out-Null
$stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$previousKey = $env:PRACTICAL_AI_MASTER_KEY
try {
    # Pass via child process environment only, avoid leakage in commandline.
    $env:PRACTICAL_AI_MASTER_KEY = $keyMatch.Groups[1].Value
    $process = Start-Process -FilePath $JavaPath -ArgumentList '-Xms512m','-Xmx2048m','-jar',('"' + $jarPath + '"') -WorkingDirectory $projectRoot -WindowStyle Hidden -RedirectStandardOutput (Join-Path $logPath "$stamp.stdout.log") -RedirectStandardError (Join-Path $logPath "$stamp.stderr.log") -PassThru
    Write-Output "Local backend started: PID=$($process.Id); port=8080; master key loaded."
} finally {
    $env:PRACTICAL_AI_MASTER_KEY = $previousKey
}
