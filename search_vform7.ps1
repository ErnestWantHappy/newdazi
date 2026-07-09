$content = Get-Content 'E:\Project\newdazi\RuoYi-Vue3\node_modules\vform3-builds\dist\designer.umd.js' -Raw

# Search for onGridDragAdd
$idx = $content.IndexOf("onGridDragAdd")
if ($idx -ge 0) {
    Write-Host "=== onGridDragAdd at $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 50), 500)
    Write-Host ""
}

# Search for onFieldDragAdd or onDragAdd
$idx = $content.IndexOf("onFieldDragAdd")
if ($idx -ge 0) {
    Write-Host "=== onFieldDragAdd at $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 50), 500)
    Write-Host ""
}

# Search for the form-widget-list's main draggable onAdd handler
$idx = $content.IndexOf("onFieldDragAddEnd")
if ($idx -ge 0) {
    Write-Host "=== onFieldDragAddEnd at $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 50), 500)
    Write-Host ""
}

# Search for the main form widget list's draggable configuration
# Look for the group:name:dragGroup,put:true pattern
$idx = $content.IndexOf("name:`"dragGroup`",pull:!1,put:!0")
if ($idx -ge 0) {
    Write-Host "=== put:!0 at $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 100), 400)
    Write-Host ""
}