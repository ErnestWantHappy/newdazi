import axios from 'axios'
import { ElLoading, ElMessage } from 'element-plus'
import { saveAs } from 'file-saver'
import errorCode from '@/utils/errorCode'
import { blobValidate } from '@/utils/ruoyi'
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
  return JSON.parse(resText)
}

async function handleBlobResponse(data, headers, defaultName) {
  if (blobValidate(data)) {
    saveAs(data, decodeURIComponent(headers?.['download-filename'] || defaultName))
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
  const { showLoading = false, filename = 'download' } = options
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
  resource(resource) {
    const url = `${baseURL}/common/download/resource?resource=${encodeURIComponent(resource)}`
    return requestDownload({
      method: 'get',
      url
    }, {
      filename: 'resource'
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
