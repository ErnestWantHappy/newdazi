$content = Get-Content 'E:\Project\newdazi\RuoYi-Vue3\node_modules\vform3-builds\dist\designer.umd.js' -Raw

# Search for form-widget-list context - look for the draggable component in the form designer
$idx = $content.IndexOf("form-widget-list")
Write-Host "=== form-widget-list at $idx ==="
# Search for the @add handler or group configuration near form-widget-list
$region = $content.Substring([Math]::Max(0, $idx - 500), 1500)
# Find dragGroup in this region
$dgIdx = $region.IndexOf("dragGroup")
if ($dgIdx -ge 0) {
    Write-Host "dragGroup at offset $dgIdx in region:"
    Write-Host $region.Substring([Math]::Max(0, $dgIdx - 50), 300)
}

# Search for the form-widget-list draggable's @add handler
$idx = $content.IndexOf("onFieldWidgetAdd")
if ($idx -ge 0) {
    Write-Host "=== onFieldWidgetAdd at $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 50), 500)
}

# Search for the form designer's internal draggable component setup
$idx = $content.IndexOf("draggable")
if ($idx -ge 0) {
    Write-Host "=== First draggable at $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 50), 300)
}

# Search for the add event handler on the form-widget-list
$idx = $content.IndexOf("onAdd")
if ($idx -ge 0) {
    Write-Host "=== First onAdd at $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 50), 300)
}

# Search for handleWidgetListAdd
$idx = $content.IndexOf("handleWidgetListAdd")
if ($idx -ge 0) {
    Write-Host "=== handleWidgetListAdd at $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 50), 500)
}