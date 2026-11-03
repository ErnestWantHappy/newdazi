<template>
  <div v-if="data.enabled || loadError" class="class-seat-panel">
    <span>机房</span>
    <el-select v-model="room" filterable allow-create default-first-option size="small" placeholder="输入或选择机房名称" style="width: 180px" :disabled="busy" @change="changeRoom">
      <el-option v-for="item in rooms" :key="item" :value="item" :label="item" />
    </el-select>
    <el-button type="primary" size="small" :loading="busy" :disabled="historical || !ready || !room.trim() || !identified.length" @click="confirm(identified)">
      一键保存为常用机位（{{ identified.length }}）
    </el-button>
    <span v-if="changes" class="seat-warning">{{ changes }} 人机位异常</span>
    <el-button v-if="loadError" size="small" @click="load">加载失败，重试</el-button>
  </div>
</template>

<script setup>
import { computed, ref, watch, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const props = defineProps({ lessonId: [String, Number], entryYear: String, classCode: String, historical: Boolean })
const emit = defineEmits(['updated'])
const data = ref({ enabled: false }), room = ref(''), busy = ref(false), loadError = ref(false)
const rooms = computed(() => (data.value.rooms || []).filter(r => r !== '未分配机房'))
const ready = computed(() => data.value.enabled && !loadError.value && data.value.roomName === room.value)
const identified = computed(() => ready.value ? (data.value.rows || []).filter(r => ['NORMAL', 'UNBOUND', 'CHANGED', 'IP_MISMATCH'].includes(r.state) && r.localIp) : [])
const changes = computed(() => (data.value.rows || []).filter(r => ['CHANGED', 'IP_MISMATCH', 'SHARED_DEVICE'].includes(r.state)).length)
let timer, sequence = 0, disposed = false, loading = false
const scope = () => ({ lessonId: props.lessonId, entryYear: props.entryYear, classCode: props.classCode, roomName: room.value })

async function load() {
  const ticket = ++sequence
  const initial = JSON.stringify(scope())
  if (props.historical || !props.lessonId || !props.entryYear || !props.classCode) {
    data.value = { enabled: false }; emit('updated', []); return
  }
  loading = true
  try {
    const response = await request({ url: '/business/seat-helper/classroom', params: scope() })
    if (disposed || ticket !== sequence || initial !== JSON.stringify(scope())) return
    data.value = response.data || { enabled: false }; loadError.value = false
    room.value = data.value.roomName || room.value
    emit('updated', data.value.rows || [])
  } catch {
    if (!disposed && ticket === sequence) { loadError.value = true; emit('updated', []) }
  } finally { if (ticket === sequence) loading = false }
}
function changeRoom() {
  data.value = { ...data.value, roomName: null, rows: [] }
  emit('updated', []); load()
}
async function confirm(rows) {
  if (busy.value || !ready.value || !room.value.trim() || !rows.length) return
  const initial = JSON.stringify(scope())
  busy.value = true
  try {
    const response = await request({ url: '/business/seat-helper/classroom/confirm', method: 'post', data: { ...scope(), students: rows.map(r => ({ studentId: r.studentId, deviceId: r.deviceId, localIp: r.localIp })) } })
    if (initial !== JSON.stringify(scope())) return
    ElMessage.success(`已保存 ${response.data?.confirmed ?? 0} 人；未连助手、重复IP和缺席学生保留原机位。`)
  } catch {
    // 统一请求层已提示失败，重新读取服务端事实，避免保留错误的成功状态。
  } finally { busy.value = false; await load() }
}
function confirmStudent(studentId) {
  return confirm(identified.value.filter(row => String(row.studentId) === String(studentId)))
}
defineExpose({ confirmStudent })
watch(() => [props.lessonId, props.entryYear, props.classCode, props.historical], () => {
  clearInterval(timer); room.value = ''; data.value = { enabled: false }; loadError.value = false
  emit('updated', []); load()
  if (!disposed && !props.historical && props.lessonId && props.entryYear && props.classCode) {
    timer = setInterval(() => { if (!loading && !busy.value) load() }, 5000)
  }
}, { immediate: true })
onBeforeUnmount(() => { disposed = true; sequence++; clearInterval(timer) })
</script>

<style scoped>
.class-seat-panel { display: inline-flex; align-items: center; flex-wrap: wrap; gap: 8px; padding: 4px 10px; border-radius: 8px; background: #f8fafc; font-size: 13px; }
.seat-warning { color: #b45309; }
</style>
