$ErrorActionPreference = 'Stop'
$compiler = Join-Path $env:WINDIR 'Microsoft.NET/Framework64/v4.0.30319/csc.exe'
if (!(Test-Path -LiteralPath $compiler)) { throw '需要 Windows .NET Framework 4 编译器。' }
$target = Join-Path $PSScriptRoot 'dist'
New-Item -ItemType Directory -Path $target -Force | Out-Null
$executable = Join-Path $target 'SeatHelper.exe'
$source = Join-Path $PSScriptRoot 'SeatHelper.cs'
& $compiler /nologo /codepage:65001 /target:winexe /platform:anycpu /optimize+ "/out:$executable" /reference:System.Windows.Forms.dll /reference:System.Drawing.dll /reference:System.Web.Extensions.dll /reference:System.Management.dll /reference:System.Security.dll $source
if ($LASTEXITCODE -ne 0) { throw '机位助手编译失败。' }
$hash = (Get-FileHash -LiteralPath $executable -Algorithm SHA256).Hash
Set-Content -LiteralPath (Join-Path $target 'SeatHelper.exe.sha256') -Value ($hash + '  SeatHelper.exe') -Encoding UTF8
@{ version='0.2.1'; file='SeatHelper.exe'; sha256=$hash; bytes=(Get-Item -LiteralPath $executable).Length; runtime='.NET Framework 4'; signed=$false } | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $target 'manifest.json') -Encoding UTF8
Write-Output ('已生成试点程序和校验清单：' + $hash)

