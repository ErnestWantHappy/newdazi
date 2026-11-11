/**
 * “本地跑不了就换服务器”的统一判定。
 *
 * 之前两个终端各自用中文字符串正则（含“不支持/加载失败”等字样）来猜是不是兼容性问题，
 * 学生代码里只要报错信息出现这些词，就会被误判成本机环境故障、静默切到服务器，
 * 真正的语法/运行错误反而被吞掉。现在由运行器给出 compat 标记，这里只做纯决策。
 */
export function shouldFallbackToServer(event, mode) {
  return mode === 'local' && event?.type === 'error' && event?.compat === true
}

/**
 * 降级后给师生的一句人话说明。不出现 Pyodide、JSPI、WebAssembly 等技术词，
 * 也不要求升级浏览器才能继续用。
 */
export const FALLBACK_NOTICE = '当前浏览器无法在本机运行 Python，已自动改用服务器运行，用法完全一样。'
