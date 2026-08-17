import axios from 'axios'
import { ElNotification, ElMessage, ElLoading } from 'element-plus'
import errorCode from '@/utils/errorCode'
import { tansParams, blobValidate } from '@/utils/ruoyi'
import cache from '@/plugins/cache'
import { saveAs } from 'file-saver'
import {
  rememberBlobDownloadFilename,
  resolveBlobDownloadFilename
} from '@/utils/downloadFilename'
import {
  isRelogin,
  handleSessionExpired,
  isSessionExpiredCode,
  isSessionExpiredError,
  refreshAuthorizationHeader
} from '@/utils/session'

let downloadLoadingInstance

axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'

const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  timeout: 60000
})

function parseResponseCode(data) {
  return Number(data?.code ?? 200)
}

function parseResponseMessage(data, code) {
  return errorCode[code] || data?.msg || errorCode.default
}

async function parseBlobError(data) {
  const resText = await data.text()
  return JSON.parse(resText)
}

service.interceptors.request.use(config => {
  const isToken = (config.headers || {}).isToken === false
  const isRepeatSubmit = (config.headers || {}).repeatSubmit === false
  config.headers = config.headers || {}
  if (!isToken) {
    config.headers = refreshAuthorizationHeader(config.headers)
  }
  if (config.method === 'get' && config.params) {
    let url = config.url + '?' + tansParams(config.params)
    url = url.slice(0, -1)
    config.params = {}
    config.url = url
  }
  if (!isRepeatSubmit && (config.method === 'post' || config.method === 'put')) {
    const requestObj = {
      url: config.url,
      data: typeof config.data === 'object' ? JSON.stringify(config.data) : config.data,
      time: new Date().getTime()
    }
    const requestSize = Object.keys(JSON.stringify(requestObj)).length
    const limitSize = 5 * 1024 * 1024
    if (requestSize >= limitSize) {
      console.warn(`[${config.url}]: 请求数据大小超出允许的5M限制，无法进行防重复提交验证。`)
      return config
    }
    const sessionObj = cache.session.getJSON('sessionObj')
    if (sessionObj === undefined || sessionObj === null || sessionObj === '') {
      cache.session.setJSON('sessionObj', requestObj)
    } else {
      const s_url = sessionObj.url
      const s_data = sessionObj.data
      const s_time = sessionObj.time
      const interval = 1000
      if (s_data === requestObj.data && requestObj.time - s_time < interval && s_url === requestObj.url) {
        const message = '数据正在处理，请勿重复提交'
        console.warn(`[${s_url}]: ${message}`)
        return Promise.reject(new Error(message))
      }
      cache.session.setJSON('sessionObj', requestObj)
    }
  }
  return config
}, error => {
  console.log(error)
  return Promise.reject(error)
})

service.interceptors.response.use(async res => {
  if (res.request.responseType === 'blob' || res.request.responseType === 'arraybuffer') {
    if (blobValidate(res.data)) {
      return rememberBlobDownloadFilename(res.data, res.headers)
    }
    const rspObj = await parseBlobError(res.data)
    const code = parseResponseCode(rspObj)
    const msg = parseResponseMessage(rspObj, code)
    if (isSessionExpiredCode(code)) {
      handleSessionExpired(msg)
    } else {
      ElMessage.error(msg)
    }
    return Promise.reject(new Error(msg))
  }

  const code = parseResponseCode(res.data)
  const msg = parseResponseMessage(res.data, code)
  if (isSessionExpiredCode(code)) {
    handleSessionExpired(msg)
    return Promise.reject(new Error(msg))
  }
  if (code === 500) {
    ElMessage({ message: msg, type: 'error' })
    return Promise.reject(new Error(msg))
  }
  if (code === 601) {
    ElMessage({ message: msg, type: 'warning' })
    return Promise.reject(new Error(msg))
  }
  if (code !== 200) {
    ElNotification.error({ title: msg })
    return Promise.reject(new Error(msg))
  }
  return res.data
}, error => {
  if (axios.isCancel(error) || error?.code === 'ERR_CANCELED') {
    return Promise.reject(error)
  }
  console.log('err' + error)
  if (isSessionExpiredError(error)) {
    handleSessionExpired(error?.response?.data?.msg || error?.message)
    return Promise.reject(error)
  }

  let message = error?.message || ''
  if (message === 'Network Error') {
    message = '后端接口连接异常'
  } else if (message.includes('timeout')) {
    message = '系统接口请求超时'
  } else if (message.includes('Request failed with status code')) {
    message = '系统接口' + message.substring(message.length - 3) + '异常'
  } else if (!message) {
    message = errorCode.default
  }
  ElMessage({ message, type: 'error', duration: 5 * 1000 })
  return Promise.reject(error)
})

export { isRelogin }

export function download(url, params, filename, config) {
  downloadLoadingInstance = ElLoading.service({
    text: '正在下载数据，请稍候',
    background: 'rgba(0, 0, 0, 0.7)'
  })
  return service.post(url, params, {
    transformRequest: [(requestParams) => tansParams(requestParams)],
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    responseType: 'blob',
    ...config
  }).then(async data => {
    if (blobValidate(data)) {
      saveAs(data, resolveBlobDownloadFilename(data, filename))
      return
    }
    const rspObj = await parseBlobError(data)
    const code = parseResponseCode(rspObj)
    const errMsg = parseResponseMessage(rspObj, code)
    if (isSessionExpiredCode(code)) {
      handleSessionExpired(errMsg)
      return
    }
    ElMessage.error(errMsg)
  }).catch(error => {
    if (isSessionExpiredError(error)) {
      return
    }
    console.error(error)
    ElMessage.error(error?.message || '下载文件出现错误，请联系管理员！')
  }).finally(() => {
    downloadLoadingInstance?.close()
  })
}

export default service
