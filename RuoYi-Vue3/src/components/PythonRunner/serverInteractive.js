/**
 * 服务器交互运行的统一封装。
 *
 * 低版本浏览器没有 JSPI，无法在本地挂起 input()。服务器每次收到“到目前为止的全部输入行”
 * 就重跑一次程序，跑到下一个 input() 停下并返回 needInput，
 * 前端因此能呈现与本地一致的“提示语 → 你输入 → 输出”顺序，而不是一次性灌入。
 */
export async function runInteractiveOnServer(call, inputs) {
  const response = await call({ stdin: inputs.join('\n') })
  // 兼容 axios 原始响应和已解包的结果
  const data = response?.data?.data || response?.data || response || {}
  return {
    output: data.output || '',
    needInput: Boolean(data.needInput),
    success: data.success !== false
  }
}

/**
 * 静态估算程序需要几次输入，只用于文案提示，不参与判题。
 * 统计不到的写法（如 input() 写在循环里）最多算一次，避免提示数字误导。
 */
export function guessInputCount(code) {
  if (!code) return 0
  const matches = code.match(/\binput\s*\(/g)
  if (!matches) return 0
  const inLoop = /\b(for|while)\b[\s\S]*?\binput\s*\(/.test(code)
  return inLoop ? null : matches.length
}
