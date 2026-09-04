<#
  check-release-log.ps1 - Agent 发布登记校验（AGENTS.md 硬性条款配套）
  用法：pwsh -File tools/check-release-log.ps1 [-Date <YYYY-MM-DD>] [-ReleaseDir <路径>]
  校验：contexts/RELEASE_LOG.md 中存在与目标日期匹配的登记行；
        且登记行中平台更新列非空、非“否/未写入”。
  退出码：0=通过；1=未登记或不完整。发布脚本可在切换前调用作为门禁。
#>
param(
  [string]$Date = (Get-Date -Format 'yyyy-MM-dd'),
  [string]$ReleaseDir = ''
)

$repoRoot = Split-Path $PSScriptRoot -Parent
$logPath = Join-Path $repoRoot 'contexts\RELEASE_LOG.md'
if (-not (Test-Path $logPath)) {
  Write-Error "RELEASE_LOG.md not found: $logPath"
  exit 1
}

$content = Get-Content -Raw -Encoding UTF8 $logPath

# 匹配表格登记行：| 日期 | 版本号 | release目录 | 改动摘要 | 平台更新已写入 |
# 日期列为空行（分隔表头）跳过；这里按目标日期精确匹配行首
$pattern = "(?m)^\|\s*{0}\s*\|.*\|\s*(\S+)\s*\|$" -f [regex]::Escape($Date)
$matches = [regex]::Matches($content, $pattern)

if ($matches.Count -eq 0) {
  Write-Host "[FAIL] No release entry found for $Date." -ForegroundColor Red
  Write-Host "Add a row to contexts/RELEASE_LOG.md before switching the release." -ForegroundColor Yellow
  exit 1
}

# 取最后一匹配行（最新登记），校验平台更新列
$lastRow = $matches[$matches.Count - 1].Value
$cols = @($lastRow.Trim('|').Split('|') | ForEach-Object { $_.Trim() })
# 去掉表格首尾分隔符后应为：日期、版本、release、摘要、平台更新。
$updStatus = if ($cols.Count -ge 5) { $cols[4] } else { '' }
$startsWithNo = -not [string]::IsNullOrWhiteSpace($updStatus) -and (
  $updStatus.StartsWith([string][char]0x5426) -or
  $updStatus.StartsWith([string][char]0x672A) -or
  $updStatus.StartsWith([string][char]0x65E0)
)
if ([string]::IsNullOrWhiteSpace($updStatus) -or $startsWithNo) {
  Write-Host "[FAIL] Release entry exists, but the platform-update status is incomplete: $lastRow" -ForegroundColor Red
  Write-Host "Update the final column after writing biz_platform_update." -ForegroundColor Yellow
  exit 1
}

Write-Host "[OK] Release entry verified for ${Date}: $lastRow" -ForegroundColor Green
exit 0
