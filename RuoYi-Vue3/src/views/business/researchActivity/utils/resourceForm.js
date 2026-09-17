const MAX_FILE_BYTES = 50 * 1024 * 1024
const ALLOWED_EXTENSIONS = ['zip', 'rar', '7z']

export function createEmptyLink() {
  return { resourceName: '', linkUrl: '', extractCode: '', permanent: true, expireTime: null, description: '' }
}

export function createResourceForm(source = {}) {
  return {
    schoolType: source.schoolType || '',
    grade: source.grade ?? null,
    semester: source.semester || '',
    lessonKind: source.lessonKind || 'N',
    lessonNo: source.lessonNo ?? null,
    courseTitle: source.courseTitle || '',
    contentHtml: source.contentHtml || '',
    fileAction: 'KEEP',
    links: (source.resources || []).filter(item => item.resourceType === 'L').map(item => ({
      resourceName: item.resourceName || '',
      linkUrl: item.linkUrl || '',
      extractCode: item.extractCode || '',
      permanent: !item.expireTime,
      expireTime: item.expireTime || null,
      description: item.description || ''
    }))
  }
}

export function normalizeResourcePayload(form) {
  const numericLesson = form.lessonKind === 'N'
  return {
    schoolType: String(form.schoolType || ''),
    grade: form.grade == null ? null : Number(form.grade),
    semester: String(form.semester || ''),
    lessonKind: String(form.lessonKind || ''),
    lessonNo: numericLesson && form.lessonNo != null ? Number(form.lessonNo) : null,
    courseTitle: String(form.courseTitle || '').trim(),
    contentHtml: form.contentHtml || '',
    fileAction: form.fileAction || 'KEEP',
    links: (form.links || []).map(link => ({
      resourceName: String(link.resourceName || '').trim(),
      linkUrl: String(link.linkUrl || '').trim(),
      extractCode: String(link.extractCode || '').trim() || null,
      permanent: link.permanent !== false,
      expireTime: link.permanent === false ? normalizeExpireTime(link.expireTime) : null,
      description: String(link.description || '').trim() || null
    }))
  }
}

export function validatePackageFile(file) {
  if (!file) return null
  const extension = String(file.name || '').split('.').pop().toLowerCase()
  if (!ALLOWED_EXTENSIONS.includes(extension)) return '主课件只支持 ZIP、RAR 或 7z 文件'
  if (file.size > MAX_FILE_BYTES) return '文件超过50MB，请改用云盘链接'
  return null
}

export function normalizeExpireTime(value) {
  if (!value) return null
  if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(String(value))) return String(value)
  const date = value instanceof Date ? value : new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const pad = number => String(number).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

export function validateLinks(links, now = new Date()) {
  if ((links || []).length > 3) return '每条课程资源最多添加3个云盘链接'
  for (const link of links || []) {
    if (!String(link.resourceName || '').trim()) return '请输入资源名称'
    if (!/^https?:\/\/[^\s]+$/i.test(String(link.linkUrl || '').trim())) return '云盘链接必须是完整的 HTTP(S) 地址'
    if (link.permanent === false) {
      if (!link.expireTime) return '请选择资源过期时间'
      if (new Date(link.expireTime).getTime() <= now.getTime()) return '过期时间必须晚于当前时间'
    }
  }
  return null
}

export { MAX_FILE_BYTES }
