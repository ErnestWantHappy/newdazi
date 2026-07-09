$content = Get-Content 'E:\Project\newdazi\RuoYi-Vue3\node_modules\vform3-builds\dist\designer.umd.js' -Raw
$len = $content.Length
Write-Host "File length: $len"

# Search for addCustomWidgetSchema
$idx1 = $content.IndexOf('addCustomWidgetSchema')
if ($idx1 -ge 0) {
    Write-Host "`n=== addCustomWidgetSchema at index $idx1 ==="
    Write-Host $content.Substring([Math]::Max(0, $idx1 - 50), 300)
}

# Search for getFieldWidgetByType
$idx2 = $content.IndexOf('getFieldWidgetByType')
if ($idx2 -ge 0) {
    Write-Host "`n=== getFieldWidgetByType at index $idx2 ==="
    Write-Host $content.Substring([Math]::Max(0, $idx2 - 50), 400)
}

# Search for export
$idx3 = $content.IndexOf('exports.')
if ($idx3 -ge 0) {
    Write-Host "`n=== exports. at index $idx3 ==="
    Write-Host $content.Substring([Math]::Max(0, $idx3 - 50), 400)
}

# Search for install
$idx4 = $content.LastIndexOf('install')
if ($idx4 -ge 0) {
    Write-Host "`n=== install at index $idx4 ==="
    Write-Host $content.Substring([Math]::Max(0, $idx4 - 50), 400)
}

# Last 500 chars
Write-Host "`n=== Last 500 chars ==="
Write-Host $content.Substring($len - 500)