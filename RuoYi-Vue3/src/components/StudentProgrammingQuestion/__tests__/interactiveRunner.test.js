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
