$content = Get-Content 'E:\Project\newdazi\RuoYi-Vue3\node_modules\sortablejs\Sortable.min.js' -Raw

# Check if filter can be a function
$idx = $content.IndexOf('filter')
if ($idx -ge 0) {
    Write-Host "=== filter handling at index $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 30), 200)
    Write-Host ""
}

# Check if multiple instances on same element are handled
$idx = $content.IndexOf('Sortable.active')
if ($idx -ge 0) {
    Write-Host "=== Sortable.active at $idx ==="
    Write-Host $content.Substring([Math]::Max(0, $idx - 30), 200)
    Write-Host ""
}