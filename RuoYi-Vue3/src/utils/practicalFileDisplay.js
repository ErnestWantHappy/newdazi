export const practicalFileNameLimit = 100

export function formatPracticalFileSize(value) {
  if (value == null || value === '' || !Number.isFinite(Number(value)) || Number(value) < 0) return '大小未知'
  const bytes = Number(value)
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KiB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MiB`
}
