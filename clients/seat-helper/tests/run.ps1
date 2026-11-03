$ErrorActionPreference = 'Stop'
$compiler = Join-Path $env:WINDIR 'Microsoft.NET/Framework64/v4.0.30319/csc.exe'
$workspace = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path
$target = Join-Path $workspace 'output/review-repair-client-tests'
New-Item -ItemType Directory -Path $target -Force | Out-Null
$exe = Join-Path $target 'SeatHelperRegression.exe'
$icon = Join-Path $target 'platform.ico'
& (Join-Path $PSScriptRoot '../make-icon.ps1') -OutputPath $icon
& $compiler /nologo /codepage:65001 /target:exe /main:SeatHelperRegression "/resource:$icon,SeatHelper.PlatformIcon" "/out:$exe" /reference:System.Windows.Forms.dll /reference:System.Drawing.dll /reference:System.Web.Extensions.dll /reference:System.Management.dll /reference:System.Security.dll (Join-Path $PSScriptRoot '../SeatHelper.cs') (Join-Path $PSScriptRoot 'SeatHelperRegression.cs')
if ($LASTEXITCODE -ne 0) { throw '客户端测试编译失败' }
& $exe (Join-Path $target 'fixture')
if ($LASTEXITCODE -ne 0) { throw '客户端回归失败' }
& $compiler /nologo /codepage:65001 /target:exe /main:SeatHelperBrowserProbe "/resource:$icon,SeatHelper.PlatformIcon" "/out:$(Join-Path $target 'SeatHelperBrowserProbe.exe')" /reference:System.Windows.Forms.dll /reference:System.Drawing.dll /reference:System.Web.Extensions.dll /reference:System.Management.dll /reference:System.Security.dll (Join-Path $PSScriptRoot '../SeatHelper.cs') (Join-Path $PSScriptRoot 'SeatHelperBrowserProbe.cs')
if ($LASTEXITCODE -ne 0) { throw '浏览器独立验收程序编译失败' }
