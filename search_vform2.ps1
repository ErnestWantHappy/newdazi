$content = Get-Content 'E:\Project\newdazi\RuoYi-Vue3\node_modules\vform3-builds\dist\designer.umd.js' -Raw

# Search for handleFieldWidgetClone
$idx = $content.IndexOf('handleFieldWidgetClone')
if ($idx -ge 0) {
    Write-Host "=== handleFieldWidgetClone at index $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 50), 400)
    Write-Host ""
}

# Search for copyNewFieldWidget
$idx = $content.IndexOf('copyNewFieldWidget')
if ($idx -ge 0) {
    Write-Host "=== copyNewFieldWidget at index $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 50), 400)
    Write-Host ""
}

# Search for addFieldByDbClick
$idx = $content.IndexOf('addFieldByDbClick')
if ($idx -ge 0) {
    Write-Host "=== addFieldByDbClick at index $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 50), 500)
    Write-Host ""
}

# Search for the form designer's @add handler
$idx = $content.IndexOf('onFieldAdd')
if ($idx -ge 0) {
    Write-Host "=== onFieldAdd at index $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 50), 400)
    Write-Host ""
}

# Search for setCurrentWidget or selectedWidget
$idx = $content.IndexOf('setCurrentWidget')
if ($idx -ge 0) {
    Write-Host "=== setCurrentWidget at index $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 50), 400)
    Write-Host ""
}

# Search for form-widget-list or widgetList
$idx = $content.IndexOf('handleWidgetAdd')
if ($idx -ge 0) {
    Write-Host "=== handleWidgetAdd at index $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 50), 500)
    Write-Host ""
}