// 展厅只是辅助信息；输入期间连迟到的接口结果也暂缓渲染，避免与按键抢占主线程。
export function createLoginIdleWork({
  now = () => Date.now(),
  setTimer = (fn, delay) => window.setTimeout(fn, delay),
  clearTimer = id => window.clearTimeout(id),
  requestIdle = fn => window.requestIdleCallback
    ? window.requestIdleCallback(fn, { timeout: 2500 }) : setTimer(fn, 0),
  cancelIdle = id => window.cancelIdleCallback
    ? window.cancelIdleCallback(id) : clearTimer(id),
  isBlocked = () => false,
  onBusyChange = () => {},
  quietMs = 1200,
} = {}) {
  const jobs = new Map()
  let timer
  let idle
  let quietUntil = 0
  let disposed = false

  function cancelWake() {
    if (timer !== undefined) clearTimer(timer)
    if (idle !== undefined) cancelIdle(idle)
    timer = idle = undefined
  }

  function wake(delay = 0) {
    if (disposed) return
    cancelWake()
    timer = setTimer(pump, Math.max(delay, quietUntil - now(), 0))
  }

  function pump() {
    timer = undefined
    if (disposed) return
    if (now() < quietUntil) return wake()
    if (isBlocked()) return wake(500)
    onBusyChange(false)
    const next = [...jobs].sort((a, b) => a[1].due - b[1].due)[0]
    if (!next) return
    if (next[1].due > now()) return wake(next[1].due - now())
    idle = requestIdle(() => {
      idle = undefined
      if (disposed) return
      if (now() < quietUntil || isBlocked()) return wake(500)
      // 接口返回和图片扩展分批执行，不在一次空闲回调里堆满工作。
      const job = jobs.get(next[0])
      if (job !== next[1]) return wake()
      jobs.delete(next[0])
      try { job.run() } finally { wake(150) }
    })
  }

  return {
    interact() {
      if (disposed) return
      quietUntil = now() + quietMs
      onBusyChange(true)
      wake()
    },
    schedule(key, run, delay = 0) {
      if (disposed) return
      jobs.set(key, { run, due: now() + delay })
      wake()
    },
    wake,
    dispose() {
      disposed = true
      cancelWake()
      jobs.clear()
    },
  }
}
