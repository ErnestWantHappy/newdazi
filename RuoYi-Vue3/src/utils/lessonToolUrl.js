// 保存保留老师填写的原文；只有明确的网页地址才作为学生可点击链接。
export function lessonToolHref(value) {
  const url = String(value ?? '').trim()
  if (!/^https?:\/\//i.test(url) || /[\u0000-\u0020\u007f\\]/.test(url)) return undefined
  try {
    const parsed = new URL(url)
    if (!parsed.hostname || parsed.username || parsed.password) return undefined
    return url
  } catch {
    return undefined
  }
}

export function lessonToolUrlHint(value) {
  const url = String(value ?? '').trim()
  if (lessonToolHref(url)) return ''
  if (!url) return '网址还未填写，可先保存课程，稍后再补充。'
  if (!/^https?:\/\//i.test(url)) {
    return '建议填写以 http:// 或 https:// 开头的完整网址；当前内容可保存，完善后学生才能打开。'
  }
  return '这个网址暂不能直接打开，请检查格式或移除网址内嵌的账号密码；不影响保存课程。'
}
