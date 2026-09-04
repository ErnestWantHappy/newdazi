<template>
  <div class="student-layout">
    <app-main />
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted } from 'vue'
import { AppMain } from './components'

let socket
let heartbeatTimer
let reconnectTimer
let disposed = false

function deviceId() {
  const key = 'classroom-presence-device-id'
  let id = localStorage.getItem(key)
  if (!id) {
    // 内网 HTTP 不提供 randomUUID，仍需生成稳定的本机终端标识以维持 Presence 连接。
    id = typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function'
      ? crypto.randomUUID().replace(/-/g, '')
      : `${Date.now().toString(16)}${Math.random().toString(16).slice(2)}`
    localStorage.setItem(key, id)
  }
  return id
}

function connectPresence() {
  if (disposed) return
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  socket = new WebSocket(`${protocol}//${window.location.host}/ws/presence/${deviceId()}`)
  socket.onopen = () => {
    if (disposed) { socket.close(); return }
    heartbeatTimer = window.setInterval(() => { if (socket?.readyState === WebSocket.OPEN) socket.send('{"type":"heartbeat"}') }, 30000)
  }
  socket.onclose = () => {
    window.clearInterval(heartbeatTimer)
    if (!disposed) reconnectTimer = window.setTimeout(connectPresence, 5000)
  }
}

onMounted(() => { disposed = false; connectPresence() })
onBeforeUnmount(() => { disposed = true; window.clearInterval(heartbeatTimer); window.clearTimeout(reconnectTimer); socket?.close() })
</script>

<style lang="scss" scoped>
.student-layout {
  min-height: 100vh;
}

/* AppMain 默认隐藏溢出会成为 sticky 的非滚动祖先，导致学生页顶部导航随页面滚走。 */
.student-layout :deep(.app-main) {
  overflow: visible;
}
</style>
