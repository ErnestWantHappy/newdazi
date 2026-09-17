const DIRECT_IMAGE_PREFIXES = [
  '/profile/upload/research-activity/images/',
  '/dev-api/profile/upload/research-activity/images/',
  '/prod-api/profile/upload/research-activity/images/'
]

const RESOURCE_VIEW_PATHS = new Set([
  '/common/resource/view',
  '/dev-api/common/resource/view',
  '/prod-api/common/resource/view'
])

function isDirectResearchImage(path) {
  return DIRECT_IMAGE_PREFIXES.some(prefix => path.startsWith(prefix))
}

/** 判断正文图片能否通过公开通知的受控图片接口读取。 */
export function isResearchNoticeImageSource(src) {
  const value = String(src || '').trim()
  if (!value) return false
  if (isDirectResearchImage(value)) return true

  try {
    const parsed = new URL(value, 'http://research-notice.local')
    if (!RESOURCE_VIEW_PATHS.has(parsed.pathname)) return false
    return isDirectResearchImage(parsed.searchParams.get('resource') || '')
  } catch {
    return false
  }
}
