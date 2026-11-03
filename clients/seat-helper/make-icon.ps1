param([Parameter(Mandatory=$true)][string]$OutputPath)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$logoPath = Join-Path $PSScriptRoot '../../RuoYi-Vue3/src/assets/logo/logo.png'
$logo = [System.Drawing.Image]::FromFile((Resolve-Path -LiteralPath $logoPath).Path)
$sizes = @(16, 24, 32, 48, 64, 128, 256)
$frames = @()
try {
    foreach ($size in $sizes) {
        $bitmap = New-Object System.Drawing.Bitmap($size, $size)
        $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
        $stream = New-Object System.IO.MemoryStream
        try {
            $graphics.Clear([System.Drawing.Color]::Transparent)
            $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
            # 保持原 Logo 比例，留出透明边缘，避免小图标贴边。
            $scale = ($size * 0.92) / [Math]::Max($logo.Width, $logo.Height)
            $width = [int][Math]::Round($logo.Width * $scale)
            $height = [int][Math]::Round($logo.Height * $scale)
            $graphics.DrawImage($logo, [int](($size-$width)/2), [int](($size-$height)/2), $width, $height)
            # 使用传统 DIB 帧；旧版 .NET 的 Icon.ToBitmap 对 PNG 帧可能显示乱码。
            $dib = New-Object System.IO.BinaryWriter($stream)
            $maskStride = [int]([Math]::Ceiling($size / 32.0) * 4)
            $dib.Write([uint32]40); $dib.Write([int]$size); $dib.Write([int]($size * 2))
            $dib.Write([uint16]1); $dib.Write([uint16]32); $dib.Write([uint32]0)
            $dib.Write([uint32]($size * $size * 4)); $dib.Write([int]0); $dib.Write([int]0)
            $dib.Write([uint32]0); $dib.Write([uint32]0)
            $mask = New-Object byte[] ($maskStride * $size)
            for ($y=$size-1; $y -ge 0; $y--) {
                for ($x=0; $x -lt $size; $x++) {
                    $pixel = $bitmap.GetPixel($x, $y)
                    $dib.Write([byte]$pixel.B); $dib.Write([byte]$pixel.G)
                    $dib.Write([byte]$pixel.R); $dib.Write([byte]$pixel.A)
                    if ($pixel.A -eq 0) {
                        $index = ($size-1-$y)*$maskStride + [int][Math]::Floor($x/8.0)
                        $mask[$index] = $mask[$index] -bor (128 -shr ($x % 8))
                    }
                }
            }
            $dib.Write([byte[]]$mask); $dib.Flush()
            $frames += ,($stream.ToArray())
        } finally { $graphics.Dispose(); $bitmap.Dispose(); $stream.Dispose() }
    }
    $file = [System.IO.File]::Create($OutputPath)
    $writer = New-Object System.IO.BinaryWriter($file)
    try {
        $writer.Write([uint16]0); $writer.Write([uint16]1); $writer.Write([uint16]$sizes.Count)
        $offset = 6 + 16 * $sizes.Count
        for ($i=0; $i -lt $sizes.Count; $i++) {
            $dimension = if ($sizes[$i] -eq 256) { 0 } else { $sizes[$i] }
            $writer.Write([byte]$dimension); $writer.Write([byte]$dimension)
            $writer.Write([byte]0); $writer.Write([byte]0)
            $writer.Write([uint16]1); $writer.Write([uint16]32)
            $writer.Write([uint32]$frames[$i].Length); $writer.Write([uint32]$offset)
            $offset += $frames[$i].Length
        }
        foreach ($frame in $frames) { $writer.Write([byte[]]$frame) }
    } finally { $writer.Dispose() }
} finally { $logo.Dispose() }
