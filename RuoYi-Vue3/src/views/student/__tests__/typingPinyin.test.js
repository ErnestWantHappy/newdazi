import test from 'node:test'
import assert from 'node:assert/strict'
import {
  buildTypingPinyinSegments,
  createTypingPinyinSegments,
  createTypingPinyinController
} from '../typingPinyin.js'

function fakeSegments(text) {
  return Array.from(text).map((char, index) => ({
    origin: char,
    start: index,
    hasPinyin: false,
    pinyin: ''
  }))
}

test('整段转换保留原文，并正确处理多音字、ü/v 和英文数字标点', async () => {
  const text = '银行在重庆，女生绿色旅行。Hello 2026!\n第二行 重要重复'
  const segments = await buildTypingPinyinSegments(text)

  assert.equal(segments.map((segment) => segment.origin).join(''), text)
  assert.deepEqual(
    segments.filter((segment) => segment.hasPinyin).map((segment) => segment.pinyin),
    [
      'yin', 'hang', 'zai', 'chong', 'qing',
      'nv', 'sheng', 'lv', 'se', 'lv', 'xing',
      'di', 'er', 'hang',
      'zhong', 'yao', 'chong', 'fu'
    ]
  )

  const nonChinese = segments.filter((segment) => !segment.hasPinyin)
  assert.ok(nonChinese.some((segment) => segment.origin.includes('Hello 2026!')))
  assert.ok(nonChinese.some((segment) => segment.origin.includes('\n')))
  assert.ok(nonChinese.every((segment) => segment.pinyin === ''))
})

test('转换结果与原文长度不一致时拒绝生成注音', () => {
  assert.throws(
    () => createTypingPinyinSegments('中文', [{ origin: '中', isZh: true, pinyin: 'zhong' }]),
    /不一致/
  )
})

test('每道题独立控制显示状态，首次展开才转换且正文不变时复用', async () => {
  const calls = []
  const controller = createTypingPinyinController(async (text) => {
    calls.push(text)
    return fakeSegments(text)
  })
  const first = { questionId: 1, questionContent: '甲' }
  const second = { questionId: 2, questionContent: '乙' }

  assert.equal(controller.isVisible(1), false)
  assert.equal(calls.length, 0)

  await controller.toggle(first)
  assert.equal(controller.isVisible(1), true)
  assert.equal(controller.isVisible(2), false)
  assert.deepEqual(calls, ['甲'])

  await controller.toggle(second)
  assert.equal(controller.isVisible(1), true)
  assert.equal(controller.isVisible(2), true)
  assert.deepEqual(calls, ['甲', '乙'])

  controller.toggle(first)
  assert.equal(controller.isVisible(1), false)
  assert.equal(controller.segmentsFor(first).length, 0)

  await controller.toggle(first)
  assert.equal(controller.isVisible(1), true)
  assert.deepEqual(calls, ['甲', '乙'])
})

test('正文变化时不回显旧注音，并按新正文重新转换', async () => {
  const calls = []
  const controller = createTypingPinyinController(async (text) => {
    calls.push(text)
    return fakeSegments(text)
  })
  const original = { questionId: 1, questionContent: '甲' }
  await controller.toggle(original)

  const updated = { questionId: 1, questionContent: '乙' }
  assert.equal(controller.segmentsFor(updated).length, 0)

  await controller.sync(updated)
  assert.equal(controller.isVisible(1), true)
  assert.equal(controller.segmentsFor(updated)[0].origin, '乙')
  assert.deepEqual(calls, ['甲', '乙'])
})

test('迟到的旧正文转换结果不能覆盖新正文注音', async () => {
  let resolveFirst
  const pendingFirst = new Promise((resolve) => { resolveFirst = resolve })
  const controller = createTypingPinyinController((text) => (
    text === '甲' ? pendingFirst : Promise.resolve(fakeSegments(text))
  ))
  const original = { questionId: 1, questionContent: '甲' }
  const updated = { questionId: 1, questionContent: '乙' }

  const firstRequest = controller.toggle(original)
  await controller.sync(updated)
  resolveFirst(fakeSegments('甲'))
  await firstRequest

  assert.equal(controller.statusOf(1), 'ready')
  assert.equal(controller.segmentsFor(updated)[0].origin, '乙')
})

test('转换失败保留展开状态且可重试，成功后恢复注音', async () => {
  let shouldFail = true
  const controller = createTypingPinyinController(async (text) => {
    if (shouldFail) throw new Error('模拟失败')
    return fakeSegments(text)
  })
  const question = { questionId: 3, questionContent: '丙' }

  await controller.toggle(question)
  assert.equal(controller.isVisible(3), true)
  assert.equal(controller.statusOf(3), 'error')
  assert.equal(controller.segmentsFor(question).length, 0)

  shouldFail = false
  await controller.retry(question)
  assert.equal(controller.isVisible(3), true)
  assert.equal(controller.statusOf(3), 'ready')
  assert.equal(controller.segmentsFor(question)[0].origin, '丙')
})

test('隐藏题目不做转换，重置后回到默认隐藏', async () => {
  const calls = []
  const controller = createTypingPinyinController(async (text) => {
    calls.push(text)
    return fakeSegments(text)
  })
  const question = { questionId: 4, questionContent: '丁' }

  await controller.sync(question)
  assert.equal(calls.length, 0)

  await controller.toggle(question)
  assert.equal(calls.length, 1)
  controller.reset()
  assert.equal(controller.isVisible(4), false)
  assert.equal(controller.segmentsFor(question).length, 0)
})
