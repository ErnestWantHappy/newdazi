$ErrorActionPreference = 'Stop'
$compiler = Join-Path $env:WINDIR 'Microsoft.NET/Framework64/v4.0.30319/csc.exe'
if (!(Test-Path -LiteralPath $compiler)) { throw '需要 Windows .NET Framework 4 编译器。' }
$target = Join-Path $PSScriptRoot 'dist'
New-Item -ItemType Directory -Path $target -Force | Out-Null
$executable = Join-Path $target 'SeatHelper.exe'
$source = Join-Path $PSScriptRoot 'SeatHelper.cs'
$icon = Join-Path $target 'platform.ico'
& (Join-Path $PSScriptRoot 'make-icon.ps1') -OutputPath $icon
& $compiler /nologo /codepage:65001 /target:winexe /platform:anycpu /optimize+ "/win32icon:$icon" "/resource:$icon,SeatHelper.PlatformIcon" "/out:$executable" /reference:System.Windows.Forms.dll /reference:System.Drawing.dll /reference:System.Web.Extensions.dll /reference:System.Management.dll /reference:System.Security.dll $source
if ($LASTEXITCODE -ne 0) { throw '机位助手编译失败。' }
$hash = (Get-FileHash -LiteralPath $executable -Algorithm SHA256).Hash
# 用 .NET 显式写无 BOM 文本，避免 PowerShell 5.1 与 7 的 -Encoding UTF8 行为差异。
$utf8 = New-Object System.Text.UTF8Encoding($false)
[IO.File]::WriteAllText((Join-Path $target 'SeatHelper.exe.sha256'), ($hash + '  SeatHelper.exe' + [Environment]::NewLine), $utf8)
$manifest = @{ version='0.2.6'; file='SeatHelper.exe'; sha256=$hash; bytes=(Get-Item -LiteralPath $executable).Length; runtime='.NET Framework 4'; signed=$false } | ConvertTo-Json
[IO.File]::WriteAllText((Join-Path $target 'manifest.json'), $manifest, $utf8)
Write-Output ('已生成试点程序和校验清单：' + $hash)

