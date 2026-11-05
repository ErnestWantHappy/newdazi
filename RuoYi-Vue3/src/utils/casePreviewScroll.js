// 页面只保留位置和尺寸；图片由可见窗口按需加载，长课件不需要常驻全部图片。
export function buildPreviewLayout(total, sizes, fallback, viewport, zoom) {
  let top = 0
  return Array.from({ length: total }, (_, index) => {
    const page = index + 1
    const size = sizes[page] || fallback
    const availableHeight = Math.max(1, viewport.height - 36)
    const width = zoom === 'page'
      ? Math.min(viewport.width, availableHeight * size.width / size.height)
      : zoom === 'width' ? viewport.width : size.width * Number(zoom) / 100
    const height = width * size.height / size.width
    const item = { page, top, width, height, bottom: top + height, blockHeight: height + 36 }
    top += item.blockHeight
    return item
  })
}

export function currentScrollPage(layout, scrollTop) {
  // 顶部页只剩很小一截时再切到下一页，跳页和缩小时不会随意跳到视口中央页。
  let low = 0, high = layout.length - 1
  while (low < high) {
    const middle = Math.floor((low + high) / 2)
    const item = layout[middle]
    if (item.bottom - Math.min(80, item.height * 0.4) <= scrollTop) low = middle + 1
    else high = middle
  }
  return layout[low]?.page || 1
}

export function visiblePreviewPages(layout, scrollTop, height) {
  if (!layout.length) return []
  const first = Math.max(0, currentScrollPage(layout, scrollTop) - 2)
  let last = first
  while (last < layout.length - 1 && layout[last].top < scrollTop + height) last++
  return layout.slice(first, Math.min(layout.length, last + 2))
}
