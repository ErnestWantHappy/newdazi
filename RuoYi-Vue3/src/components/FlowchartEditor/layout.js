// 菱形只使用中心半宽放文字，给斜边留出空间；宽度按全角字估算，避免中英文混排溢出。
export function nodeTextLayout(type, value) {
  const longestRow = Math.max(0, ...String(value || '').split(/\r\n|\r|\n/).map(row => Array.from(row).length))
  const minimum = type === 'decision' ? 4 : type === 'inputOutput' ? 6 : 7
  const columns = Math.max(minimum, Math.min(type === 'decision' ? 10 : 18, Math.ceil(Math.sqrt(longestRow) * 1.3)))
  const textWidth = columns * 15
  const width = type === 'decision' ? Math.max(164, textWidth * 2 + 20)
    : Math.max(type === 'inputOutput' ? 140 : 148, textWidth + 36)
  const lines = String(value || '').split(/\r\n|\r|\n/).flatMap(row => {
    const chars = Array.from(row)
    if (!chars.length) return ['']
    return Array.from({ length: Math.ceil(chars.length / columns) }, (_, index) =>
      chars.slice(index * columns, (index + 1) * columns).join(''))
  })
  const height = Math.max(type === 'decision' ? 96 : type === 'inputOutput' ? 60 : 64,
    (lines.length * 20 + 16) * (type === 'decision' ? 2 : 1))
  return { textWidth, width, height, lines }
}

export function fitWithoutEnlarging(lf) {
  if (!lf) return
  lf.fitView(36, 36)
  if (lf.getTransform().SCALE_X > 1) {
    lf.zoom(1)
    lf.translateCenter()
  }
}
