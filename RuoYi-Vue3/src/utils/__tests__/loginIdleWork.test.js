import test from 'node:test'
import assert from 'node:assert/strict'
import { createLoginIdleWork } from '../loginIdleWork.js'
import { needsVForm, resolveVFormPlugin } from '../vform.js'

function clock() {
  let time = 0
  let id = 0
  let blocked = false
  const timers = new Map()
  const busy = []
  const setTimer = (fn, delay) => { timers.set(++id, { fn, at: time + delay }); return id }
  const clearTimer = id => timers.delete(id)
  const work = createLoginIdleWork({ now: () => time, setTimer, clearTimer,
    requestIdle: fn => setTimer(fn, 1), cancelIdle: clearTimer,
    isBlocked: () => blocked, onBusyChange: value => busy.push(value), quietMs: 1200 })
  return { work, busy, block: value => { blocked = value }, tick(ms) {
    const end = time + ms
    for (let count = 0; count < 10000; count++) {
      const next = [...timers].sort((a, b) => a[1].at - b[1].at)[0]
      if (!next || next[1].at > end) break
      time = next[1].at
      timers.delete(next[0])
      next[1].fn()
    }
    time = end
  } }
}

test('持续输入超过固定十五秒后仍不启动展厅工作，停输后才恢复', () => {
  const c = clock(); let applied = 0
  c.work.schedule('slides', () => applied++, 15000)
  for (let i = 0; i < 200; i++) { c.work.interact(); c.tick(100) }
  assert.equal(applied, 0)
  c.tick(1200)
  assert.equal(applied, 1)
  assert.equal(c.busy.at(-1), false)
})

test('已经发出的请求迟到返回时，结果也等待输入停止', () => {
  const c = clock(); const result = []
  c.work.interact()
  c.work.schedule('result', () => result.push('old'))
  c.tick(500)
  c.work.schedule('result', () => result.push('latest'))
  c.work.interact(); c.tick(1199)
  assert.deepEqual(result, [])
  c.tick(5)
  assert.deepEqual(result, ['latest'])
})

test('中文合成、隐藏页面或登录进行中不执行排队更新', () => {
  const c = clock(); let applied = false
  c.block(true)
  c.work.schedule('result', () => { applied = true })
  c.tick(5000)
  assert.equal(applied, false)
  c.block(false); c.work.wake(); c.tick(2)
  assert.equal(applied, true)
})

test('空闲回调已排队后重新输入，仍须取消并重新等待', () => {
  const c = clock(); let applied = false
  c.work.schedule('result', () => { applied = true }); c.tick(0)
  c.work.interact(); c.tick(1000)
  assert.equal(applied, false)
  c.tick(205)
  assert.equal(applied, true)
})

test('离开页面取消回调和迟到响应，避免卸载后更新', () => {
  const c = clock(); let applied = false
  c.work.schedule('result', () => { applied = true }); c.tick(0)
  c.work.dispose()
  c.work.schedule('late', () => { applied = true }); c.work.interact(); c.tick(20000)
  assert.equal(applied, false)
})

test('生产 UMD 导出兼容并且不把未知对象当成已成功加载', () => {
  const plugin = { install() {} }
  for (const module of [plugin, { default: plugin }, { d: { default: plugin } }]) {
    assert.equal(resolveVFormPlugin(module), plugin)
  }
  assert.throws(() => resolveVFormPlugin({ unknown: {} }), /导学单组件加载失败/)
})

test('导学单独立入口与学生首页、课程嵌入入口均覆盖，登录不加载', () => {
  for (const path of ['/student/index', '/student/guide-sheet', '/business/guideSheet/designer/1', '/business/guide-sheet-preview/1', '/business/lesson/designer/1']) {
    assert.equal(needsVForm(path), true, path)
  }
  for (const path of ['/login', '/teacher-dashboard', '/student/python-practice']) {
    assert.equal(needsVForm(path), false, path)
  }
})
