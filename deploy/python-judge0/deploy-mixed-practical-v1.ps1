$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'

# This release only fixes student DTO delivery and mixed practical rendering.
$root = 'D:\program\3009dazipingtai'
$oldReleaseName = '20260817_194500_python_practical_mode_v2'
$releaseName = '20260818_111000_python_mixed_practical_v1'
$oldRelease = Join-Path $root ('releases\' + $oldReleaseName)
$newRelease = Join-Path $root ('releases\' + $releaseName)
$backup = Join-Path $root ('backups\' + $releaseName)
$archive = Join-Path $backup 'incoming\python-mixed-practical-v1.zip'
$service = 'NewDaziBackend3009'
$nginx = 'D:\programsoftware\nginx\nginx-1.29.4'
$nginxConf = Join-Path $nginx 'conf\nginx.conf'
$candidate = Join-Path $nginx ('conf\nginx.' + $releaseName + '.candidate.conf')

if (!(Test-Path $oldRelease) -or !(Test-Path $archive)) { throw 'Current release or incoming archive is missing' }
if (Test-Path $newRelease) { throw 'Target release already exists' }
New-Item -ItemType Directory -Force -Path $backup | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $backup 'incoming') | Out-Null

$nssm = (Get-CimInstance Win32_Process | Where-Object { $_.Name -eq 'nssm.exe' } | Select-Object -First 1 -ExpandProperty ExecutablePath)
if ([string]::IsNullOrWhiteSpace($nssm)) { throw 'NSSM was not found' }
$app = ((& $nssm get $service Application) -join '') -replace [char]0, ''
$args = ((& $nssm get $service AppParameters) -join '') -replace [char]0, ''
$dir = ((& $nssm get $service AppDirectory) -join '') -replace [char]0, ''
if (!$args.Contains($oldReleaseName) -or !$dir.Contains($oldReleaseName)) {
    throw 'NSSM is not pointing to the expected current release'
}

Copy-Item $nginxConf (Join-Path $backup 'nginx.conf.before') -Force
Set-Content (Join-Path $backup 'nssm-application.before.txt') $app -Encoding utf8
Set-Content (Join-Path $backup 'nssm-parameters.before.txt') $args -Encoding utf8
Set-Content (Join-Path $backup 'nssm-directory.before.txt') $dir -Encoding utf8
reg export ('HKLM\SYSTEM\CurrentControlSet\Services\' + $service) (Join-Path $backup ($service + '.reg')) /y | Out-Null

# No student scores are changed in this release. Keep a full backup for audit and rollback.
& mysqldump -uroot -pXsdata@123qwe --single-transaction --routines --events ry-vue | Set-Content (Join-Path $backup 'ry-vue_before_python_mixed_practical_v1.sql') -Encoding utf8
Get-FileHash (Join-Path $backup 'ry-vue_before_python_mixed_practical_v1.sql') -Algorithm SHA256 | Format-List | Out-File (Join-Path $backup 'backup-sha256.txt') -Encoding utf8

Expand-Archive -Path $archive -DestinationPath $newRelease -Force
if (!(Test-Path (Join-Path $newRelease 'backend\ruoyi-admin.jar')) -or !(Test-Path (Join-Path $newRelease 'frontend\index.html'))) {
    throw 'New release is incomplete'
}
Copy-Item (Join-Path $oldRelease 'config') (Join-Path $newRelease 'config') -Recurse -Force

# Course 270 / question 1754 is the active Python lesson. Only fill blank starter code.
& mysql -uroot -pXsdata@123qwe -N -B -e "UPDATE biz_programming_question_config SET starter_code='print(\"hello world\")\n', update_time=NOW() WHERE question_id=1754 AND (starter_code IS NULL OR starter_code='');" ry-vue
& mysql -uroot -pXsdata@123qwe -N -B -e "SELECT q.question_id,q.question_type,q.practical_mode,c.enabled,COUNT(tc.test_case_id) AS cases,SUM(tc.is_public='0') AS hidden_cases FROM biz_question q LEFT JOIN biz_programming_question_config c ON c.question_id=q.question_id LEFT JOIN biz_programming_test_case tc ON tc.question_id=q.question_id WHERE q.question_id=1754 GROUP BY q.question_id,q.question_type,q.practical_mode,c.enabled;" ry-vue | Set-Content (Join-Path $backup 'question-1754-postfix.tsv') -Encoding utf8

$nginxText = [IO.File]::ReadAllText($nginxConf)
if (!$nginxText.Contains($oldReleaseName)) { throw 'Nginx is not pointing to the expected current release' }
[IO.File]::WriteAllText($candidate, $nginxText.Replace($oldReleaseName, $releaseName), [Text.Encoding]::ASCII)
Push-Location $nginx
cmd /c ('"' + (Join-Path $nginx 'nginx.exe') + '" -t -c "' + $candidate + '"') | Out-Null
if ($LASTEXITCODE -ne 0) { Pop-Location; throw 'Nginx candidate validation failed' }
Pop-Location

$newArgs = $args.Replace($oldReleaseName, $releaseName)
$newDir = $dir.Replace($oldReleaseName, $releaseName)
try {
    & $nssm set $service AppParameters $newArgs | Out-Null
    & $nssm set $service AppDirectory $newDir | Out-Null
    Restart-Service -Name $service -Force
    $ready = $false
    for ($i = 0; $i -lt 30; $i++) {
        try { if ((Invoke-WebRequest -UseBasicParsing -TimeoutSec 3 'http://127.0.0.1:3009/').StatusCode -eq 200) { $ready = $true; break } } catch { }
        Start-Sleep -Seconds 2
    }
    if (!$ready) { throw 'New backend did not return HTTP 200 within 60 seconds' }
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
    throw 'Nginx live configuration validation failed; old frontend config restored'
}
cmd /c ('"' + (Join-Path $nginx 'nginx.exe') + '" -s reload') | Out-Null
Pop-Location

Write-Output ('RELEASE=' + $newRelease)
Write-Output ('BACKEND_HTTP=' + (Invoke-WebRequest -UseBasicParsing -TimeoutSec 10 'http://127.0.0.1:3009/').StatusCode)
Write-Output ('FRONTEND_HTTP=' + (Invoke-WebRequest -UseBasicParsing -TimeoutSec 10 'http://127.0.0.1:3010/').StatusCode)
