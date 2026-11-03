// 每次运行独占解释器；停止、切题和超时均销毁线程，避免旧程序继续消耗资源。
export function createInteractiveRunner(onEvent, options = {}) {
  let worker = null
  let timer
  let inputId = null
  const createWorker = options.createWorker || (() => new Worker(new URL('./interactiveWorker.js', import.meta.url), { type: 'module' }))
  const limits = { load: 180000, run: 10000, input: 300000, ...options.limits }

  function release() {
    clearTimeout(timer)
    worker?.terminate()
    worker = null
    inputId = null
  }
  function deadline(duration, message) {
    clearTimeout(timer)
    timer = setTimeout(() => {
      release()
      onEvent({ type: 'error', text: message })
    }, duration)
  }
  return {
    run(code, baseUrl) {
      release()
      let instance
      try { instance = createWorker() }
      catch { onEvent({ type: 'error', text: '无法创建 Python 运行环境，请使用新版浏览器重试；代码已保留' }); return }
      worker = instance
      const fail = text => { release(); onEvent({ type: 'error', text }) }
      instance.onerror = () => {
        if (worker === instance) fail('Python 加载或运行失败，请检查网络后重新运行；代码已保留')
      }
      instance.onmessage = ({ data }) => {
        if (worker !== instance) return
        if (data.type === 'running') deadline(limits.run, '运行超过 10 秒，已停止；请检查是否有死循环')
        if (data.type === 'input') {
          inputId = data.inputId
          deadline(limits.input, '等待输入超过 5 分钟，程序已停止，可以重新运行')
        }
        if (data.type === 'done' || data.type === 'error') release()
        onEvent(data)
      }
      onEvent({ type: 'loading', text: '正在准备 Python…' })
      deadline(limits.load, 'Python 加载超时，请检查网络后重新运行；代码已保留')
      try { instance.postMessage({ type: 'run', code, baseUrl }) }
      catch { fail('无法启动 Python，请重新运行；代码已保留') }
    },
    input(value) {
      if (!worker || inputId === null) return false
      worker.postMessage({ type: 'input', inputId, value })
      inputId = null
      deadline(limits.run, '运行超过 10 秒，已停止；请检查是否有死循环')
      return true
    },
    stop() { release(); onEvent({ type: 'stopped' }) },
    dispose: release
  }
}
