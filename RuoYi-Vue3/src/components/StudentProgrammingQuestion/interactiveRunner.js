// 按需加载后端接口：Node 环境下可直接测试运行器本身，不需要解析 Vite 别名。
async function serverRunProgramming(payload) {
  const api = await import('@/api/business/programming')
  return api.serverRunProgramming(payload)
}

// 本机运行依赖 JSPI（WebAssembly.Suspending/promising），只有 Chrome/Edge 137+ 默认支持。
export function canRunLocally() {
  return typeof WebAssembly !== 'undefined' && 'Suspending' in WebAssembly && 'promising' in WebAssembly
}

// 每次运行独占解释器；停止、切题和超时均销毁线程，避免旧程序继续消耗资源。
export function createInteractiveRunner(onEvent, options = {}) {
  let worker = null
  let timer
  let inputId = null
  let requestSeq = null
  const createWorker = options.createWorker || (() => new Worker(new URL('./interactiveWorker.js', import.meta.url), { type: 'module' }))
  const limits = { load: 180000, run: 10000, input: 300000, ...options.limits }

  function release() {
    // 停止、切题、重新运行都要让在途的服务器响应作废。
    requestSeq = null
    clearTimeout(timer)
    worker?.terminate()
    worker = null
    inputId = null
  }
  // 第三个参数标明这次失败是否属于“本机跑不了”，由外层决定是否自动改用服务器运行。
  function deadline(duration, message, compat) {
    clearTimeout(timer)
    timer = setTimeout(() => {
      release()
      onEvent({ type: 'error', text: message, compat: Boolean(compat) })
    }, duration)
  }
  return {
    /**
     * 旧版浏览器的交互式运行：每次提交“到目前为止的全部输入行”，
     * 服务端跑到下一个 input() 停下，前端因此能像本机一样逐行提问。
     */
    async runInteractiveOnServer(code, context, inputs) {
      release()
      const instance = {}
      requestSeq = instance
      onEvent({ type: 'running' })
      try {
        const call = options.interactiveRun || (payload => serverRunProgramming(payload))
        const res = await call({
          lessonId: context.lessonId,
          questionId: context.questionId,
          sourceCode: code,
          // 每行输入都以换行结束，直接回车也必须算一行。
          stdin: (inputs || []).map(value => `${value}\n`).join('')
        })
        if (requestSeq !== instance) return
        const data = res?.data || {}
        onEvent({ type: 'output', text: data.output || '' })
        if (data.needInput) onEvent({ type: 'input' })
        else if (data.success === false) onEvent({ type: 'error', text: data.output || '程序运行出错，请检查代码' })
        else onEvent({ type: 'done' })
      } catch (error) {
        if (requestSeq !== instance) return
        onEvent({ type: 'error', text: (error && (error.msg || error.message)) || '服务器运行失败，请检查网络后重新运行' })
      }
    },
    run(code, baseUrl) {
      release()
      let instance
      try { instance = createWorker() }
      catch { onEvent({ type: 'error', compat: true, text: '无法创建 Python 运行环境，已改用服务器运行；代码已保留' }); return }
      worker = instance
      const fail = (text, compat) => { release(); onEvent({ type: 'error', text, compat: Boolean(compat) }) }
      instance.onerror = () => {
        // 线程本身出错说明本机环境起不来（脚本加载失败等），交给外层降级；学生代码报错走 message 分支。
        if (worker === instance) fail('Python 运行环境加载失败，已改用服务器运行；代码已保留', true)
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
      // 加载超时通常是网络拿不到 Pyodide，属于环境问题，可以安全地改走服务器。
      deadline(limits.load, 'Python 加载超时，已改用服务器运行；代码已保留', true)
      try { instance.postMessage({ type: 'run', code, baseUrl }) }
      catch { fail('无法启动 Python，已改用服务器运行；代码已保留', true) }
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
