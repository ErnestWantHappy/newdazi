const blobDownloadNames = new WeakMap()

function decodeHeaderValue(value) {
  if (!value) return ''
  const text = String(value).trim().replace(/^UTF-8''/i, '').replace(/^['"]|['"]$/g, '')
  try {
    return decodeURIComponent(text)
  } catch {
    return text
  }
}

export function sanitizeDownloadFilename(name, fallback = '下载文件') {
  const leaf = String(name || '').replace(/\\/g, '/').split('/').pop()
  const normalized = leaf
    .normalize('NFC')
    .replace(/[\\/:*?"<>|\u0000-\u001f\u007f]/g, '_')
    .replace(/\s+/g, ' ')
    .replace(/[. ]+$/g, '')
    .trim()
  const value = normalized || fallback
  if (value.length <= 120) return value
  const dot = value.lastIndexOf('.')
  const extension = dot > 0 && dot < value.length - 1 ? value.slice(dot) : ''
  return value.slice(0, Math.max(1, 120 - extension.length)) + extension
}

export function filenameFromHeaders(headers = {}) {
  const directName = headers['download-filename'] || headers['Download-Filename']
  if (directName) return sanitizeDownloadFilename(decodeHeaderValue(directName))

  const disposition = headers['content-disposition'] || headers['Content-Disposition'] || ''
  const utf8Match = disposition.match(/filename\*=UTF-8''([^;]+)/i)
  if (utf8Match) return sanitizeDownloadFilename(decodeHeaderValue(utf8Match[1]))
  const plainMatch = disposition.match(/filename\s*=\s*"?([^";]+)"?/i)
  return plainMatch ? sanitizeDownloadFilename(decodeHeaderValue(plainMatch[1])) : ''
}

export function rememberBlobDownloadFilename(blob, headers) {
  if (!(blob instanceof Blob)) return blob
  const fileName = filenameFromHeaders(headers)
  if (fileName) blobDownloadNames.set(blob, fileName)
  return blob
}

export function resolveBlobDownloadFilename(blob, fallback = '下载文件') {
  return sanitizeDownloadFilename(blobDownloadNames.get(blob) || fallback)
}
