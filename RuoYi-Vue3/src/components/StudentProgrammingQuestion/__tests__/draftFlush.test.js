import test from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import vm from 'node:vm'

// 直接执行页面里的补存函数，锁住“切题/离开前用旧题目 ID 保存最后一段代码”的行为。
const source = fs.readFileSync(new URL('../index.vue', import.meta.url), 'utf8')
const body = source.slice(source.indexOf('function flushDraftNow('), source.indexOf('function run()'))
const ref = value => ({ value })

function setup(overrides = {}) {
  const calls = []
  const ctx = {
    pendingDraft: ref(true), sourceCode: ref('print(1)'), lastSavedSource: 'print(0)',
    saveProgrammingDraft: payload => { calls.push(payload); return Promise.resolve() },
    ...overrides
  }
  vm.createContext(ctx)
  vm.runInContext(body + '\nthis.flush = flushDraftNow', ctx)
  return { ctx, calls }
}

test('切题前用旧课程和旧题目 ID 补存未保存代码', () => {
  const { ctx, calls } = setup()
  ctx.flush(11, 22)
  assert.equal(calls.length, 1)
  assert.equal(calls[0].lessonId, 11)
  assert.equal(calls[0].questionId, 22)
  assert.equal(calls[0].sourceCode, 'print(1)')
})

test('没有待保存修改或代码为空时不重复请求', () => {
  const none = setup({ pendingDraft: ref(false) })
  none.ctx.flush(11, 22)
  assert.equal(none.calls.length, 0)
  const saved = setup({ lastSavedSource: 'print(1)' })
  saved.ctx.flush(11, 22)
  assert.equal(saved.calls.length, 0)
  const empty = setup({ sourceCode: ref('   ') })
  empty.ctx.flush(11, 22)
  assert.equal(empty.calls.length, 0)
})

test('切题监听和卸载都走同一补存函数', () => {
  assert.match(source, /watch\(\(\) => \[props\.lessonId, props\.question\.questionId\], \(value, oldValue\) => \{\s*\/\/[^\n]*\n\s*if \(oldValue\) flushDraftNow\(oldValue\[0\], oldValue\[1\]\)/)
  assert.match(source, /onUnmounted\(\(\) => \{ flushDraftNow\(props\.lessonId, loadedQuestionId\); disposed = true; clearTimeout\(draftTimer\)/)
})
