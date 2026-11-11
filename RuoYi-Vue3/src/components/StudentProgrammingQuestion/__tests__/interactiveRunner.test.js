import test from 'node:test'
import assert from 'node:assert/strict'
import { createInteractiveRunner } from '../interactiveRunner.js'

function setup(limits = {}) {
  const workers = [], events = []
  const runner = createInteractiveRunner(event => events.push(event), {
    limits,
    createWorker() {
      const worker = { messages: [], terminated: false, postMessage(data) { this.messages.push(data) }, terminate() { this.terminated = true } }
      workers.push(worker)
      return worker
    }
  })
  return { runner, workers, events, send(data, index = workers.length - 1) { workers[index].onmessage({ data }) } }
}

test('仅等待输入时发送一次，输入请求编号必须匹配', () => {
  const { runner, workers, send } = setup()
  runner.run('input()', '/pyodide/')
  assert.equal(runner.input('意外输入'), false)
  send({ type: 'input', inputId: 7 })
  assert.equal(runner.input('8'), true)
  assert.equal(runner.input('重复回车'), false)
  assert.deepEqual(workers[0].messages[1], { type: 'input', inputId: 7, value: '8' })
  runner.dispose()
})

test('停止后忽略旧线程输出，新运行重新创建线程', () => {
  const { runner, workers, events, send } = setup()
  runner.run('input()', '/pyodide/')
  runner.stop()
  assert.equal(workers[0].terminated, true)
  const length = events.length
  send({ type: 'output', text: '迟到输出' }, 0)
  assert.equal(events.length, length)
  runner.run('print(42)', '/pyodide/')
  assert.equal(workers.length, 2)
  send({ type: 'done' })
  assert.equal(workers[1].terminated, true)
})

test('等待输入暂停执行超时，回车后恢复超时保护', async () => {
  const { runner, workers, events, send } = setup({ run: 15, input: 1000 })
  runner.run('input()', '/pyodide/')
  send({ type: 'running' })
  send({ type: 'input', inputId: 1 })
  await new Promise(resolve => setTimeout(resolve, 35))
  assert.equal(workers[0].terminated, false)
  runner.input('')
  await new Promise(resolve => setTimeout(resolve, 35))
  assert.equal(workers[0].terminated, true)
  assert.match(events.at(-1).text, /运行超过/)
})

test('加载失败或完成都会释放线程并允许重新运行', () => {
  const { runner, workers, events, send } = setup()
  runner.run('print(1)', '/pyodide/')
  workers[0].onerror()
  assert.equal(events.at(-1).type, 'error')
  assert.equal(workers[0].terminated, true)
  runner.run('print(2)', '/pyodide/')
  send({ type: 'done' })
  assert.equal(events.at(-1).type, 'done')
  assert.equal(workers[1].terminated, true)
})

test('加载与等待输入都有独立超时保护', async () => {
  for (const type of ['load', 'input']) {
    const { runner, workers, events, send } = setup({ [type]: 10 })
    runner.run('input()', '/pyodide/')
    if (type === 'input') send({ type: 'input', inputId: 1 })
    await new Promise(resolve => setTimeout(resolve, 30))
    assert.equal(workers[0].terminated, true)
    assert.equal(events.at(-1).type, 'error')
  }
})

test('浏览器无法创建线程时给出可重试的错误', () => {
  const events = []
  const runner = createInteractiveRunner(event => events.push(event), { createWorker() { throw new Error('blocked') } })
  runner.run('print(1)', '/pyodide/')
  assert.equal(events[0].type, 'error')
  assert.match(events[0].text, /代码已保留/)
  runner.dispose()
})

test('无 JSPI 时判定为不支持本机运行', async () => {
  const { canRunLocally } = await import('../interactiveRunner.js')
  const saved = { Suspending: WebAssembly.Suspending, promising: WebAssembly.promising }
  try {
    delete WebAssembly.Suspending
    assert.equal(canRunLocally(), false)
    WebAssembly.Suspending = function () {}
    delete WebAssembly.promising
    assert.equal(canRunLocally(), false)
    WebAssembly.promising = function () {}
    assert.equal(canRunLocally(), true)
  } finally {
    if (saved.Suspending) WebAssembly.Suspending = saved.Suspending
    if (saved.promising) WebAssembly.promising = saved.promising
  }
})

test('服务器交互运行忽略过期响应，只保留最新一次结果', async () => {
  const events = []
  // 每次调用各自持有一个 Promise，避免共用 resolver 导致前一次请求永远悬挂
  const pending = []
  const runner = createInteractiveRunner(event => events.push(event), {
    interactiveRun: () => new Promise(resolve => pending.push(resolve))
  })
  const first = runner.runInteractiveOnServer('print(1)', { lessonId: 1, questionId: 2 }, [])
  const second = runner.runInteractiveOnServer('print(2)', { lessonId: 1, questionId: 2 }, [])
  pending[0]({ data: { output: '旧结果', needInput: false, success: true } })
  await first
  assert.equal(events.filter(event => event.type === 'output').length, 0, '旧响应不能覆盖当前结果')
  pending[1]({ data: { output: '新结果', needInput: false, success: true } })
  await second
  assert.equal(events.find(event => event.type === 'output').text, '新结果')
  assert.equal(events.at(-1).type, 'done')
})

test('服务器交互运行在需要输入时停下，并带着累计输入重跑', async () => {
  const events = []
  const calls = []
  const runner = createInteractiveRunner(event => events.push(event), {
    interactiveRun: payload => {
      calls.push(payload)
      return Promise.resolve({
        data: { output: calls.length === 1 ? '请输入您的年龄' : '请输入您的年龄3\n免票', needInput: calls.length === 1, success: true }
      })
    }
  })
  await runner.runInteractiveOnServer('print(1)', { lessonId: 1, questionId: 2 }, [])
  assert.equal(calls[0].stdin, '')
  assert.equal(events.at(-1).type, 'input')

  events.length = 0
  await runner.runInteractiveOnServer('print(1)', { lessonId: 1, questionId: 2 }, ['3'])
  assert.equal(calls[1].stdin, '3\n')
  // 提示语、输入、结果按真实顺序输出，不再粘连成一句话
  assert.equal(events.find(event => event.type === 'output').text, '请输入您的年龄3\n免票')
  assert.equal(events.at(-1).type, 'done')
})

test('服务器交互把空回车作为一个完整输入行发送', async () => {
  let received
  const runner = createInteractiveRunner(() => {}, { interactiveRun: async payload => {
    received = payload
    return { data: { output: '完成', success: true } }
  } })
  await runner.runInteractiveOnServer('input()', { lessonId: 1, questionId: 2 }, [''])
  assert.equal(received.stdin, '\n')
})

test('停止后服务器迟到响应不能恢复输入或输出', async () => {
  const events = []
  let resolve
  const runner = createInteractiveRunner(event => events.push(event), {
    interactiveRun: () => new Promise(done => { resolve = done })
  })
  const pending = runner.runInteractiveOnServer('input()', { lessonId: 1, questionId: 2 }, [])
  runner.stop()
  const length = events.length
  resolve({ data: { output: '旧提示', needInput: true } })
  await pending
  assert.equal(events.length, length)
  assert.equal(events.at(-1).type, 'stopped')
})

test('同题切课也会停止终端并重置运行状态', async () => {
  const { readFileSync } = await import('node:fs')
  const { watch, reactive, nextTick } = await import('vue')
  const vm = await import('node:vm')
  const source = readFileSync(new URL('../index.vue', import.meta.url), 'utf8')
  const body = source.slice(source.indexOf('watch(() => [props.lessonId'), source.indexOf('onMounted(() => nextTick(mountEditor))'))
  const props = reactive({ lessonId: 1, question: { questionId: 2 } })
  let stopped = 0, loaded = 0
  const context = { watch, props, terminal: { value: { stop: () => stopped++ } }, localRunning: { value: true },
    flushDraftNow: () => {}, clearTimeout, draftTimer: null, pendingDraft: { value: true }, lastSavedSource: 'x', loadedQuestionId: 2,
    current: { value: {} }, pollTimedOut: { value: true }, pollCount: 1, pollTimer: null,
    nextTick, load: () => loaded++ }
  vm.runInNewContext(body, context)
  await nextTick()
  const initial = stopped
  props.lessonId = 3
  await nextTick()
  await nextTick()
  assert.equal(stopped, initial + 1)
  assert.equal(context.localRunning.value, false)
  assert.ok(loaded >= 2)
})

test('服务器交互运行出错时回报错误而不是无限等待', async () => {
  const events = []
  const runner = createInteractiveRunner(event => events.push(event), {
    interactiveRun: () => Promise.resolve({ data: { output: '语法错误：line 2', success: false, needInput: false } })
  })
  await runner.runInteractiveOnServer('print(', { lessonId: 1, questionId: 2 }, [])
  assert.equal(events.at(-1).type, 'error')
  assert.match(events.at(-1).text, /语法错误/)
})

test('切题或重新运行后旧线程的输入不会串到新题目', () => {
  const { runner, workers, send } = setup()
  runner.run('input()', '/pyodide/')
  send({ type: 'input', inputId: 1 })
  runner.run('input()', '/pyodide/')
  // 新一次运行还没请求输入，旧输入不能生效
  assert.equal(runner.input('旧题目的答案'), false)
  send({ type: 'input', inputId: 2 })
  assert.equal(runner.input('新题目的答案'), true)
  assert.deepEqual(workers[1].messages.at(-1), { type: 'input', inputId: 2, value: '新题目的答案' })
  runner.dispose()
})

test('只有本机环境起不来才标记为兼容性问题，学生代码报错不算', () => {
  const { runner, workers, events, send } = setup({ load: 20 })
  runner.run('print(1)', '/pyodide/')
  // 环境加载超时：这是浏览器/网络问题，可以自动改用服务器
  return new Promise(resolve => setTimeout(resolve, 45)).then(() => {
    assert.equal(events.at(-1).type, 'error')
    assert.equal(events.at(-1).compat, true)
    assert.equal(workers[0].terminated, true)

    events.length = 0
    runner.run('print(1)', '/pyodide/')
    send({ type: 'error', text: 'NameError: name "x" is not defined' })
    assert.equal(events.at(-1).type, 'error')
    assert.notEqual(events.at(-1).compat, true, '学生代码错误不能被当作浏览器兼容性问题')
    runner.dispose()
  })
})

test('线程创建失败标记为兼容性问题，可安全降级', () => {
  const events = []
  const runner = createInteractiveRunner(event => events.push(event), { createWorker() { throw new Error('blocked') } })
  runner.run('print(1)', '/pyodide/')
  assert.equal(events[0].type, 'error')
  assert.equal(events[0].compat, true)
  runner.dispose()
})
