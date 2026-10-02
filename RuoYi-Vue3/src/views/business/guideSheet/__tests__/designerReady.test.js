import test from 'node:test'
import assert from 'node:assert/strict'
import { createDesignerReadyTask } from '../utils/designerReady.js'

test('引用先出现、内部尚未就绪时重试，成功后停止', t => {
  t.mock.timers.enable({ apis: ['setTimeout'] })
  let attempts = 0
  const task = createDesignerReadyTask({ isActive: () => true, attempt: () => {
    if (++attempts < 3) throw new Error('未就绪')
    return true
  } })
  task.run()
  t.mock.timers.tick(100)
  t.mock.timers.tick(100)
  t.mock.timers.tick(1000)
  assert.equal(attempts, 3)
})

test('离开模式或卸载后，旧任务不得继续回填', t => {
  t.mock.timers.enable({ apis: ['setTimeout'] })
  let attempts = 0
  const task = createDesignerReadyTask({ isActive: () => true, attempt: () => { attempts++; return false } })
  task.run()
  task.cancel()
  t.mock.timers.tick(1000)
  assert.equal(attempts, 1)
})

test('新任务替换旧任务，失败有界且仅提示一次', t => {
  t.mock.timers.enable({ apis: ['setTimeout'] })
  let attempts = 0
  let failures = 0
  const task = createDesignerReadyTask({ isActive: () => true, maxAttempts: 2,
    attempt: () => { attempts++; return false }, onTimeout: () => failures++ })
  task.run()
  task.run()
  t.mock.timers.tick(100)
  t.mock.timers.tick(1000)
  assert.equal(attempts, 3)
  assert.equal(failures, 1)
})

test('未处于高级模式时不访问设计器', () => {
  const task = createDesignerReadyTask({ isActive: () => false, attempt: () => assert.fail('不应回填') })
  task.run()
})
