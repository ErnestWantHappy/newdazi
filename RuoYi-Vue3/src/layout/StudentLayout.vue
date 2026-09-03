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

function deviceId() {
  const key = 'classroom-presence-device-id'
  let id = localStorage.getItem(key)
  if (!id) { id = crypto.randomUUID().replace(/-/g, ''); localStorage.setItem(key, id) }
  return id
}

function connectPresence() {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  socket = new WebSocket(`${protocol}//${window.location.host}/ws/presence/${deviceId()}`)
  socket.onopen = () => { heartbeatTimer = window.setInterval(() => { if (socket?.readyState === WebSocket.OPEN) socket.send('{"type":"heartbeat"}') }, 30000) }
  socket.onclose = () => { window.clearInterval(heartbeatTimer); reconnectTimer = window.setTimeout(connectPresence, 5000) }
}

onMounted(connectPresence)
onBeforeUnmount(() => { window.clearInterval(heartbeatTimer); window.clearTimeout(reconnectTimer); socket?.close() })
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
