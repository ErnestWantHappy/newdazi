import { test } from 'node:test'
import assert from 'node:assert/strict'
import { nodeTextLayout, fitWithoutEnlarging } from '../layout.js'

test('单节点适配不放大，大图保留缩小比例', () => {
  for (const scale of [8, 1, 0.2]) {
    const calls = []
    fitWithoutEnlarging({ fitView: () => calls.push('fit'), getTransform: () => ({ SCALE_X: scale }),
      zoom: value => calls.push(value), translateCenter: () => calls.push('center') })
    assert.deepEqual(calls, scale > 1 ? ['fit', 1, 'center'] : ['fit'])
  }
})

test('四类图形为长文本和显式换行预留高度', () => {
  for (const type of ['terminal', 'process', 'decision', 'inputOutput']) {
    const short = nodeTextLayout(type, '开始')
    const long = nodeTextLayout(type, '很长的文字'.repeat(40))
    assert.ok(long.height > short.height)
    assert.ok(nodeTextLayout(type, '一\n二\n三\n四').height >= 96)
    assert.ok(long.textWidth <= long.width * (type === 'decision' ? 0.5 : 1))
    assert.ok(long.width <= 320)
  }
})
