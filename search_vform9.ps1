$content = Get-Content 'E:\Project\newdazi\RuoYi-Vue3\node_modules\vform3-builds\dist\designer.umd.js' -Raw

# Search for copyNewFieldWidget implementation (in the designer class)
$idx = $content.IndexOf("copyNewFieldWidget(e)")
if ($idx -ge 0) {
    Write-Host "=== copyNewFieldWidget(e) at $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 50), 600)
    Write-Host ""
}

# Search for copyNewFieldWidget: (colon notation)
$idx = $content.IndexOf("copyNewFieldWidget:")
if ($idx -ge 0) {
    Write-Host "=== copyNewFieldWidget: at $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 50), 600)
    Write-Host ""
}