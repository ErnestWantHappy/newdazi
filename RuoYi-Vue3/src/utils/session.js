import axios from 'axios'
import { ElMessageBox, ElNotification } from 'element-plus'
import { getToken } from '@/utils/auth'
import useUserStore from '@/store/modules/user'

export const isRelogin = { show: false }

export const SESSION_EXPIRED_MESSAGE = '登录状态已过期，请重新登录后重试。'

const HEARTBEAT_INTERVAL = 60 * 1000
const EXPIRE_REMIND_WINDOW = 10 * 60 * 1000
const REMIND_COOLDOWN = 5 * 60 * 1000

const heartbeatClient = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

let heartbeatTimer = null
let heartbeatPending = false
let hasBoundListeners = false
let lastRemindAt = 0
let verificationPending = null
let lastVerificationAt = 0
let lastUnavailableAt = 0
let remindedToken = null

function normalizeExpiredMessage(message) {
  if (!message) {
    return SESSION_EXPIRED_MESSAGE
  }
  const text = String(message)
  if (text.includes('无法访问系统资源') || text.includes('认证失败')) {
    return SESSION_EXPIRED_MESSAGE
  }
  return text
}

export function getAuthorizationHeader() {
  const token = getToken()
  if (!token) {
    return {}
  }
  return {
    Authorization: `Bearer ${token}`
  }
}

export function refreshAuthorizationHeader(headers = {}) {
  const nextHeaders = { ...headers }
  const authHeaders = getAuthorizationHeader()
  if (authHeaders.Authorization) {
    nextHeaders.Authorization = authHeaders.Authorization
  } else {
    delete nextHeaders.Authorization
  }
  return nextHeaders
}

export function isSessionExpiredCode(code) {
  return Number(code) === 401
}

export function isSessionExpiredError(error) {
  if (!error) {
    return false
  }
  const status = Number(error?.status || error?.response?.status || 0)
  if (status === 401) {
    return true
  }
  return isSessionExpiredCode(error?.code) || isSessionExpiredCode(error?.response?.data?.code)
}

function showSessionExpired(message, token) {
  const finalMessage = normalizeExpiredMessage(message)
  if (isRelogin.show || remindedToken === token || getToken() !== token) {
    return
  }
  remindedToken = token
  isRelogin.show = true
  ElMessageBox.confirm(finalMessage, '系统提示', {
    confirmButtonText: '重新登录',
    cancelButtonText: '留在当前页',
    type: 'warning'
  }).then(() => {
    if (getToken() !== token) return
    return useUserStore().logOut().then(() => {
      location.href = '/index'
    })
  }).catch(() => {
  }).finally(() => {
    isRelogin.show = false
  })
}

function notifyUnavailable() {
  if (Date.now() - lastUnavailableAt < REMIND_COOLDOWN) return
  lastUnavailableAt = Date.now()
  ElNotification.warning({ title: '连接暂时异常', message: '暂时无法确认登录状态，请保留当前作答，连接恢复后再保存。', duration: 6000 })
}

// 只复核会话，不重放保存、交卷等写请求；同一失效会话取消提示后不再反复打断答题。
export function handleSessionExpired(message) {
  const token = getToken()
  if (!token || remindedToken === token) return Promise.resolve()
  if (verificationPending) return verificationPending
  if (Date.now() - lastVerificationAt < 5000) return Promise.resolve()
  lastVerificationAt = Date.now()
  verificationPending = heartbeatClient.get('/auth/session-status', {
    headers: { Authorization: `Bearer ${token}` }
  }).then(res => {
    if (getToken() !== token) return
    if (isSessionExpiredCode(res?.data?.code)) showSessionExpired(SESSION_EXPIRED_MESSAGE, token)
    else if (Number(res?.data?.code) === 200) remindedToken = null
    else notifyUnavailable()
  }).catch(error => {
    if (getToken() !== token) return
    if (isSessionExpiredError(error)) showSessionExpired(SESSION_EXPIRED_MESSAGE, token)
    else notifyUnavailable()
  }).finally(() => { verificationPending = null })
  return verificationPending
}

function shouldHeartbeat() {
  if (!getToken()) {
    return false
  }
  if (typeof document !== 'undefined' && document.hidden) {
    return false
  }
  return true
}

function notifyExpiring(expireTime) {
  if (!expireTime) {
    return
  }
  const remainMs = Number(expireTime) - Date.now()
  if (remainMs <= 0 || remainMs > EXPIRE_REMIND_WINDOW) {
    return
  }
  const now = Date.now()
  if (now - lastRemindAt < REMIND_COOLDOWN) {
    return
  }
  const remainMin = Math.max(1, Math.ceil(remainMs / 60000))
  ElNotification.warning({
    title: '会话提醒',
    message: `登录状态将在约 ${remainMin} 分钟后过期，请及时保存当前内容。`,
    duration: 6000
  })
  lastRemindAt = now
}

export async function runSessionHeartbeat() {
  if (!shouldHeartbeat() || heartbeatPending) {
    return
  }
  heartbeatPending = true
  try {
    const token = getToken()
    const res = await heartbeatClient.get('/auth/session-status', {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    const code = res?.data?.code ?? 200
    if (isSessionExpiredCode(code)) {
      handleSessionExpired(res?.data?.msg)
      return
    }
    if (code !== 200) {
      notifyUnavailable()
      return
    }
    if (getToken() === token) remindedToken = null
    notifyExpiring(res?.data?.data?.expireTime)
  } catch (error) {
    if (isSessionExpiredError(error)) {
      handleSessionExpired()
    } else {
      notifyUnavailable()
    }
  } finally {
    heartbeatPending = false
  }
}

function handleVisibilityChange() {
  if (!document.hidden) {
    runSessionHeartbeat()
  }
}

export function startSessionHeartbeat() {
  if (typeof window === 'undefined') {
    return
  }
  if (heartbeatTimer) {
    return
  }
  heartbeatTimer = window.setInterval(() => {
    runSessionHeartbeat()
  }, HEARTBEAT_INTERVAL)
  if (!hasBoundListeners) {
    document.addEventListener('visibilitychange', handleVisibilityChange)
    window.addEventListener('online', runSessionHeartbeat)
    hasBoundListeners = true
  }
  runSessionHeartbeat()
}

export function stopSessionHeartbeat() {
  if (heartbeatTimer) {
    window.clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
  if (hasBoundListeners) {
    document.removeEventListener('visibilitychange', handleVisibilityChange)
    window.removeEventListener('online', runSessionHeartbeat)
    hasBoundListeners = false
  }
}
