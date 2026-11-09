const LOGIN_BRAND_ANIMATION_KEY = 'login-brand-animation-pending'

// 仅在实际登录成功后写入，避免普通刷新或站内跳转重复播放品牌动画。
export function markLoginBrandAnimation() {
  try {
    window.sessionStorage.setItem(LOGIN_BRAND_ANIMATION_KEY, '1')
  } catch {
    // 隐私模式可能禁用 sessionStorage，动画失败不应影响登录。
  }
}

export function consumeLoginBrandAnimation() {
  try {
    const pending = window.sessionStorage.getItem(LOGIN_BRAND_ANIMATION_KEY) === '1'
    window.sessionStorage.removeItem(LOGIN_BRAND_ANIMATION_KEY)
    return pending
  } catch {
    return false
  }
}
