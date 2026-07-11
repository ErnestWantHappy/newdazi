let ws = null
let reconnectTimer = null
let reconnectAttempts = 0
const MAX_RECONNECT = 5
const RECONNECT_INTERVAL = 3000

const listeners = {}

function connect(url) {
  if (ws && (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING)) {
    return
  }
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.host
  ws = new WebSocket(`${protocol}//${host}${url}`)

  ws.onopen = () => {
    reconnectAttempts = 0
    emit('connected')
  }

  ws.onmessage = (event) => {
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

  ws.onclose = () => {
    emit('disconnected')
    tryReconnect(url)
  }

  ws.onerror = (e) => {
    emit('error', e)
  }

  return ws
}

function tryReconnect(url) {
  if (reconnectTimer) return
  if (reconnectAttempts >= MAX_RECONNECT) {
    emit('reconnect_failed')
    return
  }
  reconnectAttempts++
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null
    connect(url)
  }, RECONNECT_INTERVAL)
}

function send(data) {
  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(typeof data === 'string' ? data : JSON.stringify(data))
  }
}

function subscribe(key) {
  return {
    on(event, callback) {
      addListener(event, callback)
      return this
    },
    off(event, callback) {
      removeListener(event, callback)
      return this
    }
  }
}

function on(event, callback) {
  addListener(event, callback)
}

function off(event, callback) {
  removeListener(event, callback)
}

function addListener(event, callback) {
  if (!listeners[event]) {
    listeners[event] = []
  }
  listeners[event].push(callback)
}

function removeListener(event, callback) {
  if (!listeners[event]) return
  const idx = listeners[event].indexOf(callback)
  if (idx > -1) {
    listeners[event].splice(idx, 1)
  }
}

function emit(event, data) {
  if (!listeners[event]) return
  listeners[event].forEach(cb => {
    try {
      cb(data)
    } catch (e) {
      console.warn('WebSocket listener error:', e)
    }
  })
}

function disconnect() {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  if (ws) {
    ws.close()
    ws = null
  }
}

function connectClassroom(deptId, classCode) {
  return connect(`/ws/classroom/${deptId}/${classCode}`)
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
