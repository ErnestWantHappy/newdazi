$content = Get-Content 'E:\Project\newdazi\RuoYi-Vue3\node_modules\vuedraggable\dist\vuedraggable.umd.js' -Raw

# Search for _underlying_vm_
$idx = $content.IndexOf("_underlying_vm_")
if ($idx -ge 0) {
    Write-Host "=== _underlying_vm_ at $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 100), 600)
    Write-Host ""
}

# Search for onDragAdd
$idx = $content.IndexOf("onDragAdd")
if ($idx -ge 0) {
    Write-Host "=== onDragAdd at $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 100), 600)
    Write-Host ""
}

# Search for clone function handling
$idx = $content.IndexOf("cloneFunction")
if ($idx -ge 0) {
    Write-Host "=== cloneFunction at $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 100), 600)
    Write-Host ""
}