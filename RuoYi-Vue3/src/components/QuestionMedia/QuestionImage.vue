<script setup>
import { shallowRef, watch, onBeforeUnmount } from 'vue'
import { readQuestionImage } from '@/api/business/questionImages'
const props = defineProps({ imageId: { type: [String, Number], required: true }, context: { type: Object, default: () => ({}) }, alt: { type: String, default: '题目图片' }, compact: Boolean })
const url = shallowRef(''), error = shallowRef(''), loading = shallowRef(false)
let controller, sequence = 0
function release() { if (url.value) URL.revokeObjectURL(url.value); url.value = '' }
async function load() {
  const current = ++sequence
  controller?.abort(); controller = new AbortController(); release(); error.value = ''; loading.value = true
  try {
    const blob = await readQuestionImage(props.imageId, props.context, controller.signal)
    if (current !== sequence) return
    if (!blob?.type?.startsWith('image/')) throw new Error('图片返回异常，请重试')
    url.value = URL.createObjectURL(blob)
  } catch (e) { if (current === sequence && e?.code !== 'ERR_CANCELED') error.value = e.message || '图片加载失败' }
  finally { if (current === sequence) loading.value = false }
}
watch(() => [props.imageId, JSON.stringify(props.context)], load, { immediate: true })
onBeforeUnmount(() => { sequence++; controller?.abort(); release() })
</script>

<template>
  <div class="question-image" :class="{ compact }" @click.stop @keydown.stop>
    <div v-if="loading" class="image-state">图片加载中…</div>
    <div v-else-if="error" class="image-state image-error" role="alert"><span>{{ alt }}：{{ error }}</span><el-button link type="primary" @click="load">重试</el-button></div>
    <el-image v-else-if="url" :src="url" :alt="alt" fit="contain" :preview-src-list="[url]" preview-teleported :hide-on-click-modal="true">
      <template #error><div class="image-state">图片无法显示<el-button link @click="load">重试</el-button></div></template>
    </el-image>
    <span v-if="url && !compact" class="zoom-hint">点击图片放大</span>
  </div>
</template>

<style scoped>
.question-image{position:relative;display:flex;align-items:center;justify-content:center;min-height:80px;width:100%;max-width:100%;border:1px solid #e5eaf1;border-radius:4px;background:#fff;overflow:hidden}
.question-image :deep(.el-image){display:block;max-width:100%;width:100%;cursor:zoom-in}
.question-image :deep(.el-image__inner){max-height:440px;object-fit:contain}
.question-image.compact{width:68px;min-height:50px;height:54px;flex:none}.compact :deep(.el-image__inner){height:52px}
.image-state{display:flex;flex-wrap:wrap;align-items:center;justify-content:center;gap:8px;padding:12px;color:#909399;font-size:12px}.image-error{color:#b16e26}
.zoom-hint{position:absolute;right:7px;bottom:5px;pointer-events:none;background:#fffffff0;color:#7c8a9a;padding:2px 6px;border-radius:3px;font-size:11px}
</style>
