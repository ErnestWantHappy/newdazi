import test from 'node:test'
import assert from 'node:assert/strict'
import { runInteractiveOnServer, guessInputCount } from '../serverInteractive.js'

test('兼容后端 AjaxResult 包装，取到真正的运行结果', async () => {
  const call = async () => ({ code: 200, data: { success: true, needInput: false, output: '免票' } })
  const result = await runInteractiveOnServer(call, ['3'])
  assert.equal(result.output, '免票')
  assert.equal(result.needInput, false)
  assert.equal(result.success, true)
})

test('兼容已解包的响应体和二次包装', async () => {
  const unwrapped = await runInteractiveOnServer(async () => ({ success: true, needInput: true, output: '请输入年龄' }), [])
  assert.equal(unwrapped.needInput, true)
  const nested = await runInteractiveOnServer(async () => ({ data: { data: { success: false, output: '语法错误' } } }), [])
  assert.equal(nested.success, false)
  assert.equal(nested.output, '语法错误')
})

test('每行输入保留末尾换行，零输入保持空串', async () => {
  let received
  await runInteractiveOnServer(async payload => { received = payload; return { data: {} } }, ['3', '5'])
  assert.equal(received.stdin, '3\n5\n')
  await runInteractiveOnServer(async payload => { received = payload; return { data: {} } }, [''])
  assert.equal(received.stdin, '\n')
  await runInteractiveOnServer(async payload => { received = payload; return { data: {} } }, [])
  assert.equal(received.stdin, '')
})

test('估算输入次数只在能确定时给出数字', () => {
  assert.equal(guessInputCount('a = input()\nb = input("提示")'), 2)
  assert.equal(guessInputCount('print(1)'), 0)
  // 循环里的 input 次数不确定，返回 null 避免提示错误数字
  assert.equal(guessInputCount('for i in range(3):\n    input()'), null)
  assert.equal(guessInputCount('while True:\n    x = input()'), null)
  assert.equal(guessInputCount(''), 0)
})
