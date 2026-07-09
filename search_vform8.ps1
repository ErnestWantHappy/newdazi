$content = Get-Content 'E:\Project\newdazi\RuoYi-Vue3\node_modules\vform3-builds\dist\designer.umd.js' -Raw

# Search for copyNewFieldWidget implementation
$idx = $content.IndexOf("copyNewFieldWidget(")
if ($idx -ge 0) {
    Write-Host "=== copyNewFieldWidget at $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 50), 600)
    Write-Host ""
}

# Search for handleFieldWidgetClone (the clone function used by the widget panel)
$idx = $content.IndexOf("handleFieldWidgetClone(")
if ($idx -ge 0) {
    Write-Host "=== handleFieldWidgetClone at $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 50), 600)
    Write-Host ""
}