/**
 * 通用剪贴板复制工具。
 * 优先使用 navigator.clipboard（仅 HTTPS/localhost 等安全上下文可用）；
 * 内网 HTTP 部署（如 10.52.1.123:3010）下该 API 不存在，自动回退到
 * textarea + document.execCommand('copy') 方案，保证复制按钮始终可用。
 */
export async function copyToClipboard(text) {
  if (text === null || text === undefined) return false
  const value = String(text)

  if (navigator.clipboard && window.isSecureContext) {
    try {
      await navigator.clipboard.writeText(value)
      return true
    } catch (e) {
      // 安全上下文下也可能因权限被拒，继续走兜底方案
    }
  }

  try {
    return execCommandCopy(value)
  } catch (e) {
    return false
  }
}

function execCommandCopy(value) {
  const element = document.createElement('textarea')
  const previouslyFocused = document.activeElement

  element.value = value
  // 避免移动端弹出键盘
  element.setAttribute('readonly', '')
  element.style.contain = 'strict'
  element.style.position = 'absolute'
  element.style.left = '-9999px'
  element.style.fontSize = '12pt'

  const selection = document.getSelection()
  const originalRange = selection && selection.rangeCount > 0 ? selection.getRangeAt(0) : null

  document.body.appendChild(element)
  element.select()
  // iOS 下需要显式设置选区
  element.selectionStart = 0
  element.selectionEnd = value.length

  let isSuccess = false
  try {
    isSuccess = document.execCommand('copy')
  } catch (e) {
    isSuccess = false
  }

  element.remove()

  if (originalRange && selection) {
    selection.removeAllRanges()
    selection.addRange(originalRange)
  }
  if (previouslyFocused && typeof previouslyFocused.focus === 'function') {
    previouslyFocused.focus()
  }
  return isSuccess
}
