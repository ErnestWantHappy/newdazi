import axios from 'axios'
import { ElLoading, ElMessage } from 'element-plus'
import { saveAs } from 'file-saver'
import errorCode from '@/utils/errorCode'
import { blobValidate } from '@/utils/ruoyi'
import { filenameFromHeaders, sanitizeDownloadFilename } from '@/utils/downloadFilename'
import {
  getAuthorizationHeader,
  handleSessionExpired,
  isSessionExpiredCode,
  isSessionExpiredError
} from '@/utils/session'

const baseURL = import.meta.env.VITE_APP_BASE_API
let downloadLoadingInstance

async function parseBlobError(data) {
  const resText = await data.text()
  try {
    return JSON.parse(resText)
  } catch (_error) {
    // 下载模块与通用请求保持一致：非 JSON 错误内容应提示，而不是抛出 JSON.parse 异常。
    return { code: 500, msg: resText.trim().slice(0, 200) || errorCode.default }
  }
}

async function handleBlobResponse(data, headers, defaultName) {
  if (blobValidate(data)) {
    saveAs(data, filenameFromHeaders(headers) || sanitizeDownloadFilename(defaultName))
    return
  }
  const rspObj = await parseBlobError(data)
  const code = Number(rspObj?.code ?? 500)
  const errMsg = errorCode[code] || rspObj?.msg || errorCode.default
  if (isSessionExpiredCode(code)) {
    handleSessionExpired(errMsg)
    return
  }
  ElMessage.error(errMsg)
}

function getHeaders() {
  return {
    ...getAuthorizationHeader()
  }
}

async function requestDownload(config, options = {}) {
  const { showLoading = false, filename = '下载文件' } = options
  if (showLoading) {
    downloadLoadingInstance = ElLoading.service({
      text: '正在下载数据，请稍候',
      background: 'rgba(0, 0, 0, 0.7)'
    })
  }
  try {
    const res = await axios({
      responseType: 'blob',
      ...config,
      headers: {
        ...getHeaders(),
        ...(config.headers || {})
      }
    })
    await handleBlobResponse(res.data, res.headers, filename)
  } catch (error) {
    if (!isSessionExpiredError(error)) {
      console.error(error)
      ElMessage.error(error?.message || '下载文件出现错误，请联系管理员！')
    }
  } finally {
    downloadLoadingInstance?.close()
    downloadLoadingInstance = null
  }
}

export default {
  name(name, isDelete = true) {
    const url = `${baseURL}/common/download?fileName=${encodeURIComponent(name)}&delete=${isDelete}`
    return requestDownload({
      method: 'get',
      url
    }, {
      filename: name
    })
  },
  resource(resource, name) {
    const nameQuery = name ? `&downloadName=${encodeURIComponent(name)}` : ''
    const url = `${baseURL}/common/download/resource?resource=${encodeURIComponent(resource)}${nameQuery}`
    return requestDownload({
      method: 'get',
      url
    }, {
      filename: name || '附件'
    })
  },
  zip(url, name) {
    return requestDownload({
      method: 'get',
      url: baseURL + url
    }, {
      showLoading: true,
      filename: name
    })
  },
  saveAs(text, name, opts) {
    saveAs(text, name, opts)
  }
}
