<template>
  <el-image v-if="src" :src="src" :preview-src-list="[src]" fit="contain" style="max-width:100%;max-height:460px" />
  <span v-else>{{ error || '正在加载图表' }}</span>
</template>
<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'
import request from '@/utils/request'
const props = defineProps({ resource: String })
const src = ref(''), error = ref('')
let generation = 0
function clear() { if (src.value) URL.revokeObjectURL(src.value); src.value = '' }
watch(() => props.resource, async resource => {
  const current = ++generation
  clear(); error.value = ''
  if (!resource) return
  try {
    const blob = await request({ url: '/common/resource/view', params: { resource }, responseType: 'blob' })
    if (current !== generation) return
    if (!blob.type.startsWith('image/png')) throw new Error('图表无法读取')
    src.value = URL.createObjectURL(blob)
  } catch (e) { if (current === generation) error.value = '图表无法读取，请刷新后重试' }
}, { immediate: true })
onBeforeUnmount(() => { ++generation; clear() })
</script>
