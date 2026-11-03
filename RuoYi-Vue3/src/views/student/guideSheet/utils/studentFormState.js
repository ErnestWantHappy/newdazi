function collectStudentFields(formJson) {
  const fields = []
  const stack = [formJson?.widgetList]
  const visited = new Set()

  while (stack.length > 0) {
    const current = stack.pop()
    if (!current || typeof current !== 'object' || visited.has(current)) continue
    visited.add(current)

    if (Array.isArray(current)) {
      current.forEach(item => stack.push(item))
      continue
    }

    if (['checkbox', 'file-upload', 'picture-upload'].includes(current.type)
        && typeof current.options?.name === 'string' && current.options.name) {
      fields.push({
        type: current.type,
        name: current.options.name,
        defaultValue: current.options.defaultValue
      })
    }
    Object.values(current).forEach(value => stack.push(value))
  }

  return fields
}

function asCheckboxArray(value) {
  if (Array.isArray(value)) return [...value]
  if (value === null || value === undefined) return []
  return [value]
}

/**
 * 旧答案里的附件地址可能是裸业务路径（/business/...）或纯文件名，
 * 刷新后必须补上 API 前缀，否则浏览器会打到前端静态目录得到 index.html。
 */
function joinAccessUrl(accessBase, accessUrl) {
  const url = String(accessUrl || '')
  if (!url) return url
  if (/^(?:https?:)?\/\//i.test(url) || url.startsWith('data:') || url.startsWith('blob:')) return url
  const base = String(accessBase || '').replace(/\/$/, '')
  if (!base) return url
  if (url.startsWith(`${base}/`)) return url
  return `${base}${url.startsWith('/') ? '' : '/'}${url}`
}

function normalizeUploadItem(item) {
  if (typeof item === 'string' && item) {
    const cleanPath = item.split(/[?#]/, 1)[0]
    return { name: cleanPath.split('/').pop() || '已上传文件', url: item }
  }
  if (!item || typeof item !== 'object') return null
  if (item.url) return { ...item }
  const response = item.response
  if (response?.code !== undefined && Number(response.code) !== 200) return null
  const payload = response?.data && typeof response.data === 'object' ? response.data : response
  const fileName = payload?.fileName || payload?.name
  const accessUrl = payload?.accessUrl || payload?.url
  if (!fileName || !accessUrl) return null
  return { name: String(fileName), url: String(accessUrl) }
}

function asUploadArray(value, accessBase) {
  const items = Array.isArray(value) ? value : value === null || value === undefined ? [] : [value]
  return items
    .map(normalizeUploadItem)
    .filter(Boolean)
    .map(item => ({ ...item, url: joinAccessUrl(accessBase, item.url) }))
}

export function normalizeStudentFormData(formJson, answerData, options = {}) {
  const accessBase = String(options.accessBase || '')
  const source = answerData && typeof answerData === 'object' && !Array.isArray(answerData)
    ? answerData
    : {}
  const normalized = { ...source }

  // 旧模板允许空值，但复选框组只有数组模型才能把学生勾选同步回表单。
  collectStudentFields(formJson).forEach(({ type, name, defaultValue }) => {
    const value = Object.hasOwn(source, name) ? source[name] : defaultValue
    Object.defineProperty(normalized, name, {
      value: type === 'checkbox' ? asCheckboxArray(value) : asUploadArray(value, accessBase),
      enumerable: true,
      configurable: true,
      writable: true
    })
  })

  return normalized
}
