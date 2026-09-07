# One-key deploy for 3009/3010 (run ON 10.52.1.123, admin PowerShell).
# ASCII only: server console is GBK, non-ASCII breaks parsing.
# Usage (upload artifacts first):
#   .\prod-deploy.ps1 -Release 20260905_xxx_v1 -PreviousRelease 20260905_yyy_v1
#   .\prod-deploy.ps1 -Release 20260905_xxx_v1 -PreviousRelease 20260905_yyy_v1 -FrontendOnly
#   .\prod-deploy.ps1 -Release 20260905_xxx_v1 -PreviousRelease 20260905_yyy_v1 -Execute
# Without -Execute this is a dry-run: checks only, touches nothing.
param(
    [Parameter(Mandatory = $true)][string]$Release,
    [Parameter(Mandatory = $true)][string]$PreviousRelease,
    [switch]$FrontendOnly,
    [switch]$Execute
)

$ErrorActionPreference = 'Stop'
$root = 'D:\program\3009dazipingtai\releases'
$nginxConf = 'D:\programsoftware\nginx\nginx-1.29.4\conf\nginx.conf'
$nginxBin = 'D:\programsoftware\nginx\nginx-1.29.4\nginx.exe'
$nginxHome = 'D:\programsoftware\nginx\nginx-1.29.4\'
$newDir = Join-Path $root $Release
$failures = 0
function Step($name, [scriptblock]$check, [scriptblock]$run) {
    if (& $check) { Write-Host "[OK] $name" -ForegroundColor Green; return }
    if ($Execute -and $run) { & $run; Write-Host "  -> fixed: $name" -ForegroundColor Cyan }
    if (& $check) { Write-Host "[OK] $name" -ForegroundColor Green; return }
    Write-Host "[FAIL] $name" -ForegroundColor Red
    $script:failures++
}

function Get-NssmDir() {
    # nssm output has blank lines and wide chars: first non-blank line, nulls stripped
    $out = nssm get NewDaziBackend3009 AppDirectory
    foreach ($ln in $out) {
        $s = ("" + $ln).Replace("`0", '').Trim()
        if ($s -ne '') { return $s }
    }
    return ''
}

function Probe($url, $seconds) {
    $deadline = (Get-Date).AddSeconds($seconds)
    while ((Get-Date) -lt $deadline) {
        try {
            $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 10
            if ($r.StatusCode -eq 200) { return $true }
        } catch { Start-Sleep -Seconds 5 }
    }
    return $false
}


# 1. Artifacts (frontend-only releases skip jar)
if ($Execute) { Write-Host "=== deploy: $Release (live) ===" -ForegroundColor Yellow } else { Write-Host "=== dry-run: $Release ===" -ForegroundColor Yellow }
$newJar = Join-Path $newDir 'backend\ruoyi-admin.jar'
$newFront = Join-Path $newDir 'frontend\index.html'
if (-not $FrontendOnly) {
    Step 'new backend jar present' { Test-Path $newJar } $null
}
Step 'new frontend present' { Test-Path $newFront } $null

# 2. External config (copy from the LIVE backend dir, not previous release:
#    frontend-only releases carry no config)
if (-not $FrontendOnly) {
    $newConfig = Join-Path $newDir 'config\application.yml'
    Step 'external config ready' { Test-Path $newConfig } {
        $live = Get-NssmDir
        if ($live -eq '') { throw 'cannot read live backend dir from NSSM' }
        Copy-Item (Join-Path $live 'config') (Join-Path $newDir 'config') -Recurse -Force
    }
}
if (-not $FrontendOnly) {
    Step 'NSSM points at new dir' {
        (Get-NssmDir) -eq $newDir
    } {
        nssm set NewDaziBackend3009 AppDirectory $newDir | Out-Null
    }
}

# 4. nginx root target
Step 'nginx root points at new frontend' {
    (Select-String -Path $nginxConf -Pattern ([regex]::Escape("$Release/frontend")) -Quiet)
} {
    $c = Get-Content $nginxConf -Raw
    $c = [regex]::Replace($c, [regex]::Escape("$PreviousRelease/frontend"), "$Release/frontend", 1)
    $c | Set-Content $nginxConf -NoNewline
}
# 5. nginx syntax (nginx writes success to stderr: lower EA briefly to avoid false alarm)
Step 'nginx -t passes' {
    $prevEA = $ErrorActionPreference; $ErrorActionPreference = 'Continue'
    try { $out = & $nginxBin -t -p $nginxHome 2>&1 | Out-String } finally { $ErrorActionPreference = $prevEA }
    ($out -match 'test is successful') -and ($LASTEXITCODE -eq 0)
} $null

if ($failures -gt 0) { Write-Host 'checks failed, abort.' -ForegroundColor Red; exit 1 }
if (-not $Execute) { Write-Host 'dry-run passed, add -Execute to run for real.' -ForegroundColor Yellow; exit 0 }

# 6. Restart backend (nssm stop often hangs STOP_PENDING: kill lingerers)
if (-not $FrontendOnly) {
    nssm stop NewDaziBackend3009 | Out-Null
    Start-Sleep -Seconds 40
    $left = Get-Process java -ErrorAction SilentlyContinue
    if ($left) { Stop-Process -Name java -Force; Start-Sleep -Seconds 5 }
    nssm start NewDaziBackend3009 | Out-Null
    if (-not (Probe 'http://127.0.0.1:3009/' 180)) { Write-Host 'backend not ready in 180s, ROLL BACK!' -ForegroundColor Red; exit 1 }
    Write-Host '[OK] backend 3009 ready' -ForegroundColor Green
}

# 7. Restart nginx (reload is Access-denied on this box: restart the service)
nssm restart UnifiedNginx | Out-Null
Start-Sleep -Seconds 15
if (-not (Probe 'http://127.0.0.1:3010/' 60)) { Write-Host 'frontend not ready in 60s, ROLL BACK!' -ForegroundColor Red; exit 1 }
Write-Host '[OK] frontend 3010 ready' -ForegroundColor Green

Write-Host '=== deploy done ===' -ForegroundColor Green
Write-Host "rollback: nssm set AppDirectory to $root\$PreviousRelease; nginx root back to $PreviousRelease/frontend; restart both."
