<script setup>
import { computed, ref, shallowRef, watch } from 'vue'
import { uploadQuestionImage } from '@/api/business/questionImages'
import QuestionImage from './QuestionImage.vue'
const props = defineProps({ modelValue: { type: Array, default: () => [] }, limit: { type: Number, default: 1 }, context: { type: Object, default: () => ({}) }, compact: Boolean, disabled: Boolean, label: { type: String, default: '图片' } })
const emit = defineEmits(['update:modelValue', 'busy', 'blocked'])
const picker = ref(null), busy = shallowRef(false), progress = shallowRef(null), error = shallowRef(''), replaceIndex = shallowRef(-1)
const canAdd = computed(() => props.modelValue.length < props.limit)
function pick(index = -1) { if (props.disabled || busy.value) return; replaceIndex.value = index; picker.value?.click() }
async function upload(file, index = -1) {
  if (busy.value || props.disabled) return
  if (!file || !['image/png','image/jpeg'].includes(file.type) || file.size > 5 * 1024 * 1024) { error.value = '请选择不超过5MB的PNG或JPG图片'; return }
  if (index < 0 && !canAdd.value) { error.value = `此处最多${props.limit}张图片`; return }
  busy.value = true; emit('busy', true); error.value = ''; progress.value = null
  try {
    const response = await uploadQuestionImage(file, value => { progress.value = value })
    const next = [...props.modelValue], image = { imageId: String(response.data.imageId) }
    if (index >= 0) next.splice(index, 1, image); else next.push(image)
    emit('update:modelValue', next)
  } catch (e) { error.value = `${e.message || '上传失败'}，其他内容已保留，可重新选择或粘贴图片` }
  finally { busy.value = false; emit('busy', false) }
}
function remove(index) { if (!busy.value && !props.disabled) emit('update:modelValue', props.modelValue.filter((_,i) => i !== index)) }
function changed(event) { const file = event.target.files?.[0]; if (file) upload(file, replaceIndex.value); event.target.value = '' }
function paste(event) { if (event.defaultPrevented) return; const image = [...(event.clipboardData?.items || [])].find(x => x.type.startsWith('image/')); if (image) { event.preventDefault(); upload(image.getAsFile()) } }
function drop(event) { if (event.defaultPrevented) return; event.preventDefault(); const file = [...(event.dataTransfer?.files || [])].find(x => x.type.startsWith('image/')); if (file) upload(file) }
watch(() => busy.value || !!error.value, value => emit('blocked', value))
defineExpose({ paste, drop })
</script>
<template>
  <div class="media-input" :class="{ compact }" tabindex="0" :aria-label="`${label}，支持粘贴或拖入截图`" @paste="paste" @dragover.prevent @drop="drop">
    <input ref="picker" type="file" accept="image/png,image/jpeg" hidden @change="changed">
    <div class="media-input-body">
      <div v-for="(image,index) in modelValue" :key="`${image.imageId}:${index}`" class="media-thumb">
        <QuestionImage :image-id="image.imageId" :context="context" :compact="compact" :alt="label" />
        <div class="media-actions"><el-button link type="primary" :disabled="disabled || busy" @click="pick(index)">更换</el-button><el-button link type="danger" :disabled="disabled || busy" @click="remove(index)">删除</el-button></div>
      </div>
      <el-button v-if="canAdd" link type="primary" :disabled="disabled || busy" @click="pick()"><el-icon><Picture /></el-icon><span>{{ compact ? '图片' : '添加图片' }}</span></el-button>
      <span v-if="!compact && !busy" class="media-hint">可直接粘贴或拖入截图 · 最多{{ limit }}张</span>
    </div>
    <div v-if="busy" class="media-feedback" role="status">{{ progress !== null && progress < 100 ? `正在上传 ${progress}%` : '正在校验并保存图片…' }}</div>
    <div v-if="error" class="media-feedback media-error" role="alert">{{ error }} <el-button link type="primary" @click="error = ''">不添加此图</el-button></div>
  </div>
</template>
<style scoped>
.media-input{max-width:100%;outline-offset:3px}.media-input-body{display:flex;gap:10px;align-items:center;flex-wrap:wrap}.media-thumb{width:170px;max-width:100%}.media-thumb :deep(.question-image){min-height:100px}.media-thumb :deep(.el-image__inner){max-height:135px}.media-actions{display:flex;justify-content:space-between;border:1px solid #ebeef5;padding:0 7px;font-size:12px}.media-actions :deep(.el-button){font-size:12px}.compact .media-thumb{width:74px}.compact .media-thumb :deep(.question-image){width:74px;min-height:40px}.compact .media-actions{padding:0}.compact .media-actions :deep(.el-button){font-size:10px;padding:0;margin:0}.media-hint,.media-feedback{font-size:12px;color:#909399}.media-feedback{margin-top:5px}.media-error{color:#b87926}.media-input :deep(.el-button .el-icon){margin-right:4px}
</style>
