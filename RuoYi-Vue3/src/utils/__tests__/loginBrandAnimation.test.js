import test from 'node:test'
import assert from 'node:assert/strict'
import { consumeLoginBrandAnimation, markLoginBrandAnimation } from '../loginBrandAnimation.js'

function withWindow(run) {
  const originalWindow = globalThis.window
  try {
    return run()
  } finally {
    if (originalWindow === undefined) delete globalThis.window
    else globalThis.window = originalWindow
  }
}

test('登录品牌动画标记只消费一次，刷新不会重复播放', () => {
  withWindow(() => {
    const values = new Map()
    globalThis.window = {
      sessionStorage: {
        getItem: key => values.get(key) ?? null,
        setItem: (key, value) => values.set(key, value),
        removeItem: key => values.delete(key)
      }
    }

    assert.equal(consumeLoginBrandAnimation(), false)
    markLoginBrandAnimation()
    assert.equal(consumeLoginBrandAnimation(), true)
    assert.equal(consumeLoginBrandAnimation(), false)
  })
})

test('浏览器禁用会话存储时不影响登录流程', () => {
  withWindow(() => {
    globalThis.window = {
      get sessionStorage() {
        throw new Error('storage disabled')
      }
    }

    assert.doesNotThrow(markLoginBrandAnimation)
    assert.equal(consumeLoginBrandAnimation(), false)
  })
})
