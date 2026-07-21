let ws = null
let reconnectTimer = null
let reconnectAttempts = 0
let desiredUrl = null
let connectionGeneration = 0
let reconnectEnabled = false
const MAX_RECONNECT = 5
const RECONNECT_INTERVAL = 3000

const listeners = new Map()

function connect(url) {
  if (!url) return null
  if (ws && desiredUrl === url && (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING)) {
    return
  }
  const generation = ++connectionGeneration
  desiredUrl = url
  reconnectEnabled = true
  reconnectAttempts = 0
  clearReconnectTimer()
  closeCurrentSocket()

  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.host
  const socket = new WebSocket(`${protocol}//${host}${url}`)
  ws = socket

  socket.onopen = () => {
    if (generation !== connectionGeneration) return
    reconnectAttempts = 0
    emit('connected')
  }

  socket.onmessage = (event) => {
    if (generation !== connectionGeneration) return
    try {
      const data = JSON.parse(event.data)
      emit('message', data)
      if (data.type) {
        emit(data.type, data)
      }
    } catch (e) {
      emit('message', event.data)
    }
  }

  socket.onclose = () => {
    if (ws === socket) ws = null
    if (generation !== connectionGeneration) return
    emit('disconnected')
    tryReconnect(url, generation)
  }

  socket.onerror = (e) => {
    if (generation !== connectionGeneration) return
    emit('error', e)
  }

  return socket
}

function tryReconnect(url, generation) {
  if (!reconnectEnabled || desiredUrl !== url || generation !== connectionGeneration || reconnectTimer) return
  if (reconnectAttempts >= MAX_RECONNECT) {
    emit('reconnect_failed')
    return
  }
  reconnectAttempts++
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null
    if (!reconnectEnabled || desiredUrl !== url || generation !== connectionGeneration) return
    reconnectWithGeneration(url, generation)
  }, RECONNECT_INTERVAL)
}

function reconnectWithGeneration(url, generation) {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.host
  const socket = new WebSocket(`${protocol}//${host}${url}`)
  ws = socket
  socket.onopen = () => {
    if (generation !== connectionGeneration) return
    reconnectAttempts = 0
    emit('connected')
  }
  socket.onmessage = event => {
    if (generation !== connectionGeneration) return
    try {
      const data = JSON.parse(event.data)
      emit('message', data)
      if (data.type) emit(data.type, data)
    } catch (_error) {
      emit('message', event.data)
    }
  }
  socket.onclose = () => {
    if (ws === socket) ws = null
    if (generation !== connectionGeneration) return
    emit('disconnected')
    tryReconnect(url, generation)
  }
  socket.onerror = error => {
    if (generation === connectionGeneration) emit('error', error)
  }
}

function send(data) {
  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(typeof data === 'string' ? data : JSON.stringify(data))
  }
}

function subscribe(key) {
  const subscriptions = []
  return {
    on(event, callback) {
      addListener(event, callback)
      subscriptions.push({ event, callback })
      return this
    },
    off(event, callback) {
      removeListener(event, callback)
      return this
    },
    unsubscribe() {
      subscriptions.splice(0).forEach(({ event, callback }) => removeListener(event, callback))
      return key
    }
  }
}

function on(event, callback) {
  addListener(event, callback)
  return () => removeListener(event, callback)
}

function off(event, callback) {
  removeListener(event, callback)
}

function addListener(event, callback) {
  if (!listeners.has(event)) listeners.set(event, new Set())
  listeners.get(event).add(callback)
}

function removeListener(event, callback) {
  const eventListeners = listeners.get(event)
  if (!eventListeners) return
  eventListeners.delete(callback)
  if (eventListeners.size === 0) listeners.delete(event)
}

function emit(event, data) {
  const eventListeners = listeners.get(event)
  if (!eventListeners) return
  Array.from(eventListeners).forEach(cb => {
    try {
      cb(data)
    } catch (e) {
      console.warn('WebSocket listener error:', e)
    }
  })
}

function disconnect() {
  reconnectEnabled = false
  desiredUrl = null
  reconnectAttempts = 0
  connectionGeneration++
  clearReconnectTimer()
  closeCurrentSocket()
}

function clearReconnectTimer() {
  if (!reconnectTimer) return
  clearTimeout(reconnectTimer)
  reconnectTimer = null
}

function closeCurrentSocket() {
  const socket = ws
  ws = null
  if (!socket) return
  socket.onclose = null
  socket.onerror = null
  if (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING) {
    socket.close()
  }
}

function connectClassroom(deptId, entryYear, classCode) {
  const path = [deptId, entryYear, classCode].map(value => encodeURIComponent(String(value))).join('/')
  return connect(`/ws/classroom/${path}`)
}

export default {
  connect,
  connectClassroom,
  send,
  on,
  off,
  subscribe,
  disconnect
}
