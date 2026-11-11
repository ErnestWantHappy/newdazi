import test from 'node:test'
import assert from 'node:assert/strict'
import { shouldFallbackToServer, FALLBACK_NOTICE } from '../fallback.js'

test('只有本地运行且明确标记为兼容性问题时才降级', () => {
  assert.equal(shouldFallbackToServer({ type: 'error', compat: true }, 'local'), true)
  // 已经在服务器上运行，再失败只能如实报错，不能来回切换
  assert.equal(shouldFallbackToServer({ type: 'error', compat: true }, 'server'), false)
  assert.equal(shouldFallbackToServer({ type: 'error' }, 'local'), false)
  assert.equal(shouldFallbackToServer({ type: 'error', compat: false }, 'local'), false)
})

test('学生代码里的“不支持/加载失败”等字样不触发降级', () => {
  const studentError = {
    type: 'error',
    text: 'Traceback (most recent call last):\n  File "main.py", line 2\nValueError: 不支持这个输入'
  }
  assert.equal(shouldFallbackToServer(studentError, 'local'), false)
  const networkError = { type: 'error', text: 'Python 加载失败' }
  assert.equal(shouldFallbackToServer(networkError, 'local'), false, '缺少 compat 标记一律按真实错误展示')
})

test('降级提示不出现技术名词，也不要求升级才能继续', () => {
  assert.doesNotMatch(FALLBACK_NOTICE, /Pyodide|JSPI|WebAssembly|Worker|stdin|stdout/i)
  assert.match(FALLBACK_NOTICE, /服务器运行/)
})
