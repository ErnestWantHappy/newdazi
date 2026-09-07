/** 随机标识当前浏览器，仅用于成功认证后的会话复用，不作为登录凭据。 */
export function getLoginClient() {
  try {
    const key = 'platform-login-client'
    let id = localStorage.getItem(key)
    if (!/^[a-f0-9]{32}$/.test(id || '')) {
      id = Array.from(crypto.getRandomValues(new Uint8Array(16)), value => value.toString(16).padStart(2, '0')).join('')
      localStorage.setItem(key, id)
    }
    return id
  } catch (_) {
    // 存储被禁用时仍允许登录，服务端账号防重复认证继续生效。
    return undefined
  }
}
