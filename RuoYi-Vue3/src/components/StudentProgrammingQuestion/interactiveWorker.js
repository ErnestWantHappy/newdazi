let inputResolver
let inputSequence = 0
const encoder = new TextEncoder()
const decoder = new TextDecoder()
let outputBytes = 0
let outputLimitReached = false

function output(text) {
  outputBytes += encoder.encode(text).length
  if (outputBytes > 65536) {
    outputLimitReached = true
    throw new Error('输出超过 64KB，请检查循环或减少输出')
  }
  self.postMessage({ type: 'output', text })
}

self.onmessage = async ({ data }) => {
  if (data.type === 'input') {
    if (inputResolver && data.inputId === inputSequence) {
      const resolve = inputResolver
      inputResolver = null
      // 在恢复 Python 前回显输入，保持提示、输入、结果的真实顺序。
      output(data.value + '\n')
      resolve(data.value)
    }
    return
  }
  if (data.type !== 'run') return
  try {
    // JSPI 可以挂起同步 input()，无需改写学生代码，也不要求 HTTP 站点开启跨源隔离。
    if (!('Suspending' in WebAssembly) || !('promising' in WebAssembly)) {
      throw new Error('当前浏览器不支持交互运行，请使用新版 Chrome 或 Edge；代码仍可保存和提交')
    }
    self.postMessage({ type: 'loading', text: '正在加载 Python，首次运行需要稍等…' })
    const { loadPyodide } = await import(/* @vite-ignore */ `${data.baseUrl}pyodide.mjs`)
    const pyodide = await loadPyodide({ indexURL: data.baseUrl })
    pyodide.setStdout({ write(buffer) { output(decoder.decode(buffer, { stream: true })); return buffer.length } })
    pyodide.setStderr({ write(buffer) { output(decoder.decode(buffer, { stream: true })); return buffer.length } })
    pyodide.registerJsModule('classroom_terminal', {
      read: () => new Promise(resolve => {
        inputResolver = resolve
        self.postMessage({ type: 'input', inputId: ++inputSequence })
      })
    })
    pyodide.globals.set('_student_source', data.code)
    self.postMessage({ type: 'running' })
    await pyodide.runPythonAsync(`
import builtins, sys, traceback, linecache
from pyodide.ffi import run_sync
from classroom_terminal import read

def _terminal_input(prompt=''):
    sys.stdout.write(str(prompt))
    sys.stdout.flush()
    return run_sync(read())

builtins.input = _terminal_input
_terminal_error = None
linecache.cache['main.py'] = (len(_student_source), None, _student_source.splitlines(True), 'main.py')
try:
    exec(compile(_student_source, 'main.py', 'exec'), {'__name__': '__main__'})
except BaseException as error:
    # 仅展示学生代码的行号，避免解释器和交互桥接的内部堆栈干扰初学者。
    frames = [frame for frame in traceback.extract_tb(error.__traceback__) if frame.filename == 'main.py']
    message = ''.join(traceback.format_list(frames)) + ''.join(traceback.format_exception_only(type(error), error))
    _terminal_error = message
`)
    const errorText = outputLimitReached ? '输出超过 64KB，请检查循环或减少输出' : pyodide.globals.get('_terminal_error')
    self.postMessage(errorText ? { type: 'error', text: errorText } : { type: 'done' })
  } catch (error) {
    self.postMessage({ type: 'error', text: outputLimitReached ? '输出超过 64KB，请检查循环或减少输出' : (error.message || String(error)) })
  }
}
