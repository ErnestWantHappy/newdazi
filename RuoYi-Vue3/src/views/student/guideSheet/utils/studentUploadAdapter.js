function joinAccessUrl(accessBase, accessUrl) {
  if (/^(?:https?:)?\/\//i.test(accessUrl)) return accessUrl
  const base = String(accessBase || '').replace(/\/$/, '')
  const path = accessUrl.startsWith('/') ? accessUrl : `/${accessUrl}`
  return `${base}${path}`
}

export function parseGuideSheetUploadResponse(result, accessBase = '') {
  const code = result?.code
  if (code !== undefined && code !== null && Number(code) !== 200) {
    return {
      ok: false,
      message: String(result?.msg || '文件上传失败，请稍后重试')
    }
  }

  const payload = result?.data && typeof result.data === 'object' ? result.data : result
  const fileName = payload?.fileName || payload?.name
  const accessUrl = payload?.accessUrl || payload?.url
  if (!fileName || !accessUrl) {
    return {
      ok: false,
      message: '文件上传失败，服务器未返回有效文件信息'
    }
  }

  return {
    ok: true,
    file: {
      name: String(fileName),
      url: joinAccessUrl(accessBase, String(accessUrl))
    }
  }
}

function setUploadHeader(widgetRef, name, value) {
  if (value) {
    if (typeof widgetRef.setUploadHeader === 'function') {
      widgetRef.setUploadHeader(name, value)
    } else {
      widgetRef.uploadHeaders[name] = value
    }
    return
  }
  if (widgetRef.uploadHeaders && typeof widgetRef.uploadHeaders === 'object') {
    delete widgetRef.uploadHeaders[name]
  }
}

function setUploadData(widgetRef, name, value) {
  if (typeof widgetRef.setUploadData === 'function') {
    widgetRef.setUploadData(name, value)
  } else {
    widgetRef.uploadData[name] = value
  }
}

function refreshUploadRequest(widgetRef, context, file) {
  const headers = context.getAuthorizationHeader?.() || {}
  setUploadHeader(widgetRef, 'Authorization', headers.Authorization)
  setUploadData(widgetRef, 'bindingId', String(context.bindingId))
  setUploadData(widgetRef, 'questionName', context.fieldName)
  if (file) {
    setUploadData(widgetRef, 'clientUploadId', context.getClientUploadId(file))
  }
  return Boolean(headers.Authorization)
}

function removeFailedFile(fileList, failedFile) {
  if (!Array.isArray(fileList)) return
  const index = fileList.findIndex(item => {
    if (failedFile?.uid !== undefined && item?.uid !== undefined) {
      return item.uid === failedFile.uid
    }
    return item === failedFile
  })
  if (index >= 0) fileList.splice(index, 1)
}

export function configureStudentUploadWidget(widgetRef, context) {
  if (!widgetRef || !context) return
  widgetRef.__guideSheetUploadContext = context
  refreshUploadRequest(widgetRef, context)
  if (widgetRef.__guideSheetUploadConfigured) return

  const originalBeforeUpload = typeof widgetRef.handleOnBeforeUpload === 'function'
    ? widgetRef.handleOnBeforeUpload.bind(widgetRef)
    : () => true
  const uploadSuccessMethod = typeof widgetRef.handleFileUpload === 'function'
    ? 'handleFileUpload'
    : 'handlePictureUpload'
  const originalUploadSuccess = typeof widgetRef[uploadSuccessMethod] === 'function'
    ? widgetRef[uploadSuccessMethod].bind(widgetRef)
    : () => {}

  widgetRef.__guideSheetUploadConfigured = true
  widgetRef.handleOnBeforeUpload = file => {
    const current = widgetRef.__guideSheetUploadContext
    if (!refreshUploadRequest(widgetRef, current, file)) {
      current.notifyError?.('登录状态已过期，请重新登录后再上传')
      return false
    }
    return originalBeforeUpload(file)
  }
  widgetRef[uploadSuccessMethod] = (result, file, fileList) => {
    const current = widgetRef.__guideSheetUploadContext
    const parsed = parseGuideSheetUploadResponse(result, current.accessBase)
    if (!parsed.ok) {
      if (file && typeof file === 'object') file.status = 'fail'
      removeFailedFile(fileList, file)
      if (widgetRef.fileList !== fileList) removeFailedFile(widgetRef.fileList, file)
      current.notifyError?.(parsed.message)
      return false
    }
    return originalUploadSuccess(result, file, fileList)
  }
}
