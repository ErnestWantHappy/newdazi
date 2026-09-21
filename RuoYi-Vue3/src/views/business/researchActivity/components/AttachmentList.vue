<template>
  <div class="attachments">
    <div v-for="item in files" :key="item.resourceId" class="attachment">
      <span>{{ item.originalFileName || item.resourceName }}</span>
      <div class="actions">
        <el-button v-if="canPreview(item)" icon="View" link type="primary" :loading="busy === item.resourceId" @click="preview(item)">预览</el-button>
        <el-button icon="Download" link type="primary" @click="download(item)">下载</el-button>
      </div>
    </div>
    <el-dialog v-model="visible" :title="title" width="min(1100px, 96vw)" append-to-body destroy-on-close @closed="clear">
      <iframe v-if="url" :src="url" title="附件预览" class="preview" />
    </el-dialog>
  </div>
</template>
<script setup>
import { saveAs } from 'file-saver'
import { ElMessage } from 'element-plus'
import { downloadResearchResource, previewResearchResource } from '@/api/business/researchActivity.js'
const props = defineProps({ resources: { type: Array, default: () => [] } })
const files = computed(() => props.resources.filter(item => item.resourceType === 'F'))
const busy = ref(null)
const visible = ref(false)
const title = ref('附件预览')
const url = ref('')
const canPreview = item => /\.(pdf|docx?|xlsx?|pptx?)$/i.test(item.originalFileName || '')
async function validBlob(blob) {
  if (blob.type?.includes('json')) {
    let message = '附件读取失败'
    try { message = JSON.parse(await blob.text()).msg || message } catch { /* 非标准错误响应使用统一提示。 */ }
    throw new Error(message)
  }
  return blob
}
async function download(item) {
  try { saveAs(await validBlob(await downloadResearchResource(item.resourceId)), item.originalFileName || item.resourceName) }
  catch (error) { ElMessage.error(error.message || '附件下载失败') }
}
async function preview(item) {
  busy.value = item.resourceId
  try {
    const blob = await validBlob(await previewResearchResource(item.resourceId))
    clear()
    url.value = URL.createObjectURL(new Blob([blob], { type: 'application/pdf' }))
    title.value = item.originalFileName || '附件预览'
    visible.value = true
  } catch (error) { ElMessage.error(error.message || '预览失败，请下载查看') }
  finally { busy.value = null }
}
function clear() { if (url.value) URL.revokeObjectURL(url.value); url.value = '' }
onBeforeUnmount(clear)
</script>
<style scoped>
.attachment { display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 8px 0; border-bottom: 1px solid var(--el-border-color-lighter); }
.attachment > span { min-width: 0; overflow-wrap: anywhere; }
.actions { display: flex; flex-shrink: 0; }
.preview { width: 100%; height: 70vh; border: 0; }
@media (max-width: 600px) { .attachment { align-items: flex-start; flex-direction: column; } }
</style>
