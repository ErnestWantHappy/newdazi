$content = Get-Content 'E:\Project\newdazi\RuoYi-Vue3\node_modules\vform3-builds\dist\designer.umd.js' -Raw

# Search for the designer's addFieldByDbClick implementation
$idx = $content.IndexOf('addFieldByDbClick(')
if ($idx -ge 0) {
    Write-Host "=== First addFieldByDbClick at index $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 30), 500)
    Write-Host ""
}

# Search for copyNewFieldWidget
$idx = $content.IndexOf('copyNewFieldWidget(')
if ($idx -ge 0) {
    Write-Host "=== copyNewFieldWidget at index $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 30), 500)
    Write-Host ""
}

# Search for the form widget list draggable component
$idx = $content.IndexOf('form-widget-list')
if ($idx -ge 0) {
    Write-Host "=== form-widget-list at index $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 30), 300)
    Write-Host ""
}

# Search for dragGroup
$idx = $content.IndexOf('dragGroup')
if ($idx -ge 0) {
    Write-Host "=== dragGroup at index $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 30), 300)
    Write-Host ""
}