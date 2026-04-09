import axios from 'axios'
import { ElMessageBox, ElNotification } from 'element-plus'
import { getToken } from '@/utils/auth'
import useUserStore from '@/store/modules/user'

export const isRelogin = { show: false }

export const SESSION_EXPIRED_MESSAGE = '登录状态已过期，请重新登录后重试。'

const HEARTBEAT_INTERVAL = 5 * 60 * 1000
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
  const message = String(error?.message || error || '')
  return message.includes('401')
    || message.includes('会话已过期')
    || message.includes('登录状态已过期')
    || message.includes('认证失败')
}

export function handleSessionExpired(message) {
  const finalMessage = normalizeExpiredMessage(message)
  if (isRelogin.show) {
    return
  }
  isRelogin.show = true
  ElMessageBox.confirm(finalMessage, '系统提示', {
    confirmButtonText: '重新登录',
    cancelButtonText: '留在当前页',
    type: 'warning'
  }).then(() => {
    useUserStore().logOut().then(() => {
      location.href = '/index'
    })
  }).catch(() => {
  }).finally(() => {
    isRelogin.show = false
  })
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
      return
    }
    notifyExpiring(res?.data?.data?.expireTime)
  } catch (error) {
    if (isSessionExpiredError(error)) {
      handleSessionExpired()
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
