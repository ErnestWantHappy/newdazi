import assert from 'node:assert/strict'
import test from 'node:test'
import { buildPreviewLayout, currentScrollPage, visiblePreviewPages } from '../casePreviewScroll.js'

const viewport = { width: 900, height: 600 }
const fallback = { width: 1200, height: 675 }

test('连续布局包含所有页面，跳到末页无需加载前面的图片', () => {
  const layout = buildPreviewLayout(200, {}, fallback, viewport, 'page')
  assert.equal(layout.length, 200)
  assert.equal(currentScrollPage(layout, layout[149].top), 150)
  const window = visiblePreviewPages(layout, layout[149].top, viewport.height)
  assert(window.some(item => item.page === 150))
  assert(window.length < 8)
  assert(window.every(item => item.page > 145))
})

test('滚动到下一页自动更新页码，反向滚动可回到第一页', () => {
  const layout = buildPreviewLayout(15, {}, fallback, viewport, 'page')
  assert.equal(currentScrollPage(layout, 0), 1)
  assert.equal(currentScrollPage(layout, layout[1].top), 2)
  assert.equal(currentScrollPage(layout, layout[14].top), 15)
  assert.equal(currentScrollPage(layout, 0), 1)
})

test('横竖页独立适应视口，连续位置无重叠', () => {
  const layout = buildPreviewLayout(3, { 2: { width: 900, height: 1600 } }, fallback, viewport, 'page')
  for (const page of layout) {
    assert(page.width <= viewport.width)
    assert(page.height + 36 <= viewport.height + 0.001)
  }
  assert.equal(layout[1].top, layout[0].blockHeight)
  assert.equal(layout[2].top, layout[1].top + layout[1].blockHeight)
})

test('适应宽度与缩放生成可滚动的真实尺寸，空文档不报错', () => {
  assert.equal(buildPreviewLayout(1, {}, fallback, viewport, 'width')[0].width, 900)
  assert.equal(buildPreviewLayout(1, {}, fallback, viewport, 150)[0].width, 1800)
  assert.deepEqual(visiblePreviewPages([], 0, 600), [])
  assert.equal(currentScrollPage([], 0), 1)
})
