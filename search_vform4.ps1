$content = Get-Content 'E:\Project\newdazi\RuoYi-Vue3\node_modules\vform3-builds\dist\designer.umd.js' -Raw

# Search for the DESIGNER object's addFieldByDbClick (not the widget panel's)
# The designer's methods should be defined somewhere else
$idx = $content.IndexOf("addFieldByDbClick(e)")
if ($idx -ge 0) {
    Write-Host "=== addFieldByDbClick(e) at index $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 100), 600)
    Write-Host ""
}

# Search for "designer.addFieldByDbClick" to find the actual implementation
$idx = $content.IndexOf("designer.addFieldByDbClick")
if ($idx -ge 0) {
    Write-Host "=== designer.addFieldByDbClick ref at index $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 30), 300)
    Write-Host ""
}

# Search for the actual implementation (likely in a big object)
$idx = $content.IndexOf("addFieldByDbClick:")
if ($idx -ge 0) {
    Write-Host "=== addFieldByDbClick: at index $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 50), 600)
    Write-Host ""
}

# Search for the form designer draggable @add handler
$idx = $content.IndexOf("onFieldAdd")
if ($idx -ge 0) {
    Write-Host "=== onFieldAdd at index $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 50), 400)
    Write-Host ""
}

# Search for handleFieldDrop
$idx = $content.IndexOf("handleFieldDrop")
if ($idx -ge 0) {
    Write-Host "=== handleFieldDrop at index $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 50), 400)
    Write-Host ""
}