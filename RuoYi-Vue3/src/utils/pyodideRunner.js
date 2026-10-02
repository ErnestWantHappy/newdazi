// 每个实验组件独占 Worker，超时销毁解释器，避免死循环卡住页面。
export function createPyodideRunner() {
  let worker = null, ready = null, sequence = 0
  const pending = new Map(), files = new Map()
  function stop(reason = '实验环境已释放') {
    worker?.terminate()
    worker = null
    ready = null
    for (const task of pending.values()) {
      clearTimeout(task.timer)
      task.reject(new Error(reason))
    }
    pending.clear()
  }
  function request(action, payload, timeout = 30000, progress) {
    const id = ++sequence
    return new Promise((resolve, reject) => {
      if (!worker) { reject(new Error('实验环境已释放')); return }
      const timer = setTimeout(() => stop('运行超时，已终止 Python；可以重新运行'), timeout)
      pending.set(id, { resolve, reject, timer, progress })
      try {
        worker.postMessage({ id, action, ...payload })
      } catch (error) {
        clearTimeout(timer)
        pending.delete(id)
        reject(error)
      }
    })
  }
  async function initPyodide(progress, baseUrl = import.meta.env.VITE_PYODIDE_BASE_URL || '/pyodide/') {
    if (ready) return ready
    const instance = new Worker(new URL('./pyodideWorker.js', import.meta.url), { type: 'module' })
    worker = instance
    instance.onmessage = ({ data }) => {
      if (worker !== instance) return
      const task = pending.get(data.id)
      if (!task) return
      if (data.progress) { task.progress?.(data.progress); return }
      clearTimeout(task.timer)
      pending.delete(data.id)
      if (data.error) task.reject(new Error(data.error))
      else task.resolve(data.result)
    }
    instance.onerror = () => { if (worker === instance) stop('Python 工作线程加载失败') }
    ready = request('init', {
      baseUrl: new URL(baseUrl.replace(/\/?$/, '/'), location.href).href,
      fontUrl: new URL(import.meta.env.VITE_PYODIDE_FONT_URL || '/datalab/NotoSansSC.ttf', location.href).href,
      files: Array.from(files)
    }, 180000, progress).catch(error => { if (worker === instance) stop(error.message); throw error })
    return ready
  }
  async function loadExcelFile(fileName, buffer) {
    if (!fileName || /[\\/\x00-\x1f]/.test(fileName) || fileName === '..' || fileName.length > 200) throw new Error('数据文件名无效')
    if (buffer.byteLength > 10 * 1024 * 1024) throw new Error('数据文件不能超过10MB')
    await initPyodide()
    await request('file', { fileName, buffer })
    files.set(fileName, buffer)
  }
  return {
    initPyodide, loadExcelFile,
    async previewExcel(fileName) { await initPyodide(); return request('preview', { fileName }) },
    async runCode(code) { await initPyodide(); return request('run', { code }) },
    dispose() { stop(); files.clear() }
  }
}
