/**
 * Pyodide 运行器 — 封装浏览器端 Python 执行能力
 * 专门用于数据分析实验题（Pandas + Matplotlib + Openpyxl + Excel 分析），与现有 Judge0 完全隔离。
 */

let pyodideInstance = null
let loadingPromise = null

// 默认使用本站资源，学校内网不需要连外部 CDN。
const DEFAULT_CDN = new URL('/pyodide/', self.location.origin).href

/**
 * 动态加载 Pyodide JS 脚本
 */
function loadPyodideScript(cdnUrl = DEFAULT_CDN) {
  return import(/* @vite-ignore */ `${cdnUrl}pyodide.mjs`).then(module => {
    globalThis.loadPyodide = module.loadPyodide
  })
}

/**
 * 初始化 Pyodide 环境（单例复用）
 * @param {Function} onProgress 进度回调 ({ percent: number, stage: string, detail: string })
 * @param {string} customCdn 自定义 CDN 地址
 */
export async function initPyodide(onProgress, customCdn = DEFAULT_CDN) {
  if (pyodideInstance) {
    onProgress?.({ percent: 100, stage: 'ready', detail: 'Python 环境就绪' })
    return pyodideInstance
  }
  if (loadingPromise) {
    return loadingPromise
  }

  loadingPromise = (async () => {
    try {
      onProgress?.({ percent: 15, stage: 'script', detail: '正在加载 Pyodide 基础运行库…' })
      await loadPyodideScript(customCdn)

      onProgress?.({ percent: 35, stage: 'wasm', detail: '正在初始化 WebAssembly Python 核心引擎…' })
      const pyodide = await globalThis.loadPyodide({
        indexURL: customCdn
      })

      onProgress?.({ percent: 60, stage: 'packages', detail: '正在加载数据分析库 (pandas, openpyxl, matplotlib)…' })
      await pyodide.loadPackage(['pandas', 'matplotlib', 'numpy', 'micropip'])
      // Pyodide 0.26 不内置 openpyxl，固定随站点提供的纯 Python wheel，避免临时访问 PyPI。
      const wheelBase = new URL('/datalab/', self.location.origin).href
      pyodide.globals.set('_lab_wheel_base', wheelBase)
      await pyodide.runPythonAsync(`
import micropip
await micropip.install([_lab_wheel_base + 'et_xmlfile-2.0.0-py3-none-any.whl', _lab_wheel_base + 'openpyxl-3.1.5-py2.py3-none-any.whl'], deps=False)
`)

      onProgress?.({ percent: 90, stage: 'config', detail: '正在配置图形绘制引擎…' })
      // 配置 matplotlib 使用无头后端，避免弹窗
      await pyodide.runPythonAsync(`
import matplotlib
matplotlib.use('AGG')
import matplotlib.pyplot as plt
plt.rcParams['axes.unicode_minus'] = False
plt.rcParams['font.sans-serif'] = ['DejaVu Sans', 'Arial', 'sans-serif']
`)

      pyodideInstance = pyodide
      onProgress?.({ percent: 100, stage: 'ready', detail: '数据分析实验环境准备完成' })
      return pyodide
    } catch (err) {
      loadingPromise = null
      onProgress?.({ percent: 0, stage: 'error', detail: err.message })
      throw err
    }
  })()

  return loadingPromise
}

/**
 * 将 Excel 文件的二进制 ArrayBuffer 写入 Pyodide 虚拟文件系统
 * @param {string} fileName 原始文件名
 * @param {ArrayBuffer} arrayBuffer 文件二进制内容
 */
export async function loadExcelFile(fileName, arrayBuffer) {
  if (!fileName || /[\\/\x00-\x1f]/.test(fileName) || fileName === '..') throw new Error('数据文件名无效')
  const pyodide = await initPyodide()
  const uint8 = new Uint8Array(arrayBuffer)
  try {
    // 写入当前目录
    pyodide.FS.writeFile(fileName, uint8)
  } catch (e) {
    console.error('写入 Pyodide 虚拟文件系统失败', e)
    throw new Error(`无法载入数据文件: ${e.message}`)
  }
}

/**
 * 快速预览 Excel 文件前 20 行
 * @param {string} fileName 文件名
 * @returns {Promise<{ headers: string[], rows: any[][], totalRows: number, totalCols: number }>}
 */
export async function previewExcel(fileName) {
  const pyodide = await initPyodide()
  pyodide.globals.set('_preview_filename', fileName)
  // 在 Python 中用 pandas 读取前 20 行并序列化为 JSON
  const pyScript = `
import pandas as pd, json
_df = pd.read_excel(_preview_filename)
_df_preview = _df.head(20).fillna('')
json.dumps({
    "headers": [str(c) for c in _df.columns],
    "rows": _df_preview.values.tolist(),
    "totalRows": int(len(_df)),
    "totalCols": int(len(_df.columns))
}, ensure_ascii=False, default=str)
`
  const jsonStr = await pyodide.runPythonAsync(pyScript)
  return JSON.parse(jsonStr)
}

/**
 * 执行学生的 Python 实验代码
 * 自动拦截 print 输出与 matplotlib 图表
 * @param {string} code 待执行的 Python 代码
 * @returns {Promise<{ output: string, chartBase64: string|null, error: string|null }>}
 */
export async function runCode(code) {
  const pyodide = await initPyodide()

  let output = ''
  let chartBase64 = null
  let error = null

  // 捕获控制台标准输出与错误
  pyodide.setStdout({
    batched: (text) => {
      output = (output + text + '\n').slice(0, 65000)
    }
  })
  pyodide.setStderr({
    batched: (text) => {
      output = (output + '[stderr] ' + text + '\n').slice(0, 65000)
    }
  })

  // 注入拦截器：拦截 plt.show()，并在代码执行完毕后兜底提取未 show 的图表
  await pyodide.runPythonAsync(`
import matplotlib.pyplot as plt
import io, base64

_last_chart_base64 = None
plt.close('all')

def _capture_show(*args, **kwargs):
    global _last_chart_base64
    buf = io.BytesIO()
    plt.savefig(buf, format='png', dpi=120, bbox_inches='tight')
    buf.seek(0)
    _last_chart_base64 = "data:image/png;base64," + base64.b64encode(buf.read()).decode('ascii')
    plt.close('all')

plt.show = _capture_show
`)

  try {
    const result = await pyodide.runPythonAsync(code)
    result?.destroy?.()

    // 检查是否有生成的图表（无论是通过 plt.show 还是留在当前活跃画布）
    const checkChartPy = `
import matplotlib.pyplot as plt
import io, base64

if _last_chart_base64 is None and len(plt.get_fignums()) > 0:
    buf = io.BytesIO()
    plt.savefig(buf, format='png', dpi=120, bbox_inches='tight')
    buf.seek(0)
    _last_chart_base64 = "data:image/png;base64," + base64.b64encode(buf.read()).decode('ascii')
    plt.close('all')

_last_chart_base64 or ''
`
    const captured = await pyodide.runPythonAsync(checkChartPy)
    if (captured && typeof captured === 'string' && captured.startsWith('data:image/png;base64,')) {
      chartBase64 = captured
    }
  } catch (e) {
    error = e.message || String(e)
  } finally {
    await pyodide.runPythonAsync("import matplotlib.pyplot as plt\nplt.close('all')")
  }

  return {
    output: output.trim(),
    chartBase64,
    error
  }
}

let busy = false
self.onmessage = async ({ data }) => {
  const { id, action } = data
  if (busy) { self.postMessage({ id, error: 'Python 正在运行' }); return }
  busy = true
  try {
    let result
    if (action === 'init') {
      await initPyodide(progress => self.postMessage({ id, progress }), data.baseUrl)
      if (data.fontUrl) {
        const response = await fetch(data.fontUrl)
        if (!response.ok) throw new Error('中文字体加载失败')
        const font = await response.arrayBuffer()
        if (font.byteLength > 32 * 1024 * 1024) throw new Error('字体文件过大')
        pyodideInstance.FS.writeFile('/tmp/chinese-font.ttf', new Uint8Array(font))
        await pyodideInstance.runPythonAsync("from matplotlib import font_manager\nfont_manager.fontManager.addfont('/tmp/chinese-font.ttf')\nplt.rcParams['font.family'] = [font_manager.FontProperties(fname='/tmp/chinese-font.ttf').get_name()]")
      }
      for (const [name, buffer] of data.files) await loadExcelFile(name, buffer)
    } else if (action === 'file') await loadExcelFile(data.fileName, data.buffer)
    else if (action === 'preview') result = await previewExcel(data.fileName)
    else if (action === 'run') result = await runCode(data.code)
    else throw new Error('未知运行请求')
    self.postMessage({ id, result })
  } catch (error) { self.postMessage({ id, error: error.message || String(error) }) }
  finally { busy = false }
}
