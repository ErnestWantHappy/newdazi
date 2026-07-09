$content = Get-Content 'E:\Project\newdazi\RuoYi-Vue3\node_modules\vform3-builds\dist\designer.umd.js' -Raw

# Search for the form-widget-list area around index 1151432
# Let me look at the actual template code around form-widget-list
$start = 1151000
$len = 1000
Write-Host "=== Region around form-widget-list ($start to $($start+$len)) ==="
Write-Host $content.Substring($start, $len)
Write-Host ""

# Also search for the actual form-widget-list draggable configuration
# Look for the put:true configuration
$idx = $content.IndexOf("put:!0")
if ($idx -ge 0) {
    Write-Host "=== put:!0 at $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 100), 400)
    Write-Host ""
}

# Search for the form designer's @add event handler
$idx = $content.IndexOf("onAdd:")
if ($idx -ge 0) {
    Write-Host "=== onAdd: at $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 50), 300)
    Write-Host ""
}