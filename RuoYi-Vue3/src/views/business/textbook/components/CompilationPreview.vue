<script setup>
import { ref, watch } from 'vue'
import CasePreview from './CasePreview.vue'
import { bookResourceDownloadUrl } from '@/api/business/textbookResource'

const props = defineProps({ resource: { type: Object, default: null } })
const visible = defineModel({ type: Boolean, default: false })
const savedPage = ref(1)
watch(() => props.resource?.id, (next, previous) => {
  if (String(next || '') !== String(previous || '')) savedPage.value = 1
})
</script>

<template>
  <el-dialog v-model="visible" title="教学设计汇编" class="compilation-preview-dialog" width="96%" top="3vh" append-to-body destroy-on-close>
    <template v-if="resource">
      <div class="compilation-info">
        <span>2026 版</span>
        <a :href="bookResourceDownloadUrl(resource.id)" :download="resource.filename" target="_blank" rel="noopener">下载汇编原件</a>
      </div>
      <CasePreview v-if="visible" :key="resource.id" :resource-id="Number(resource.id)" :initial-page="savedPage" source="book" :title="resource.filename"
        @page-change="savedPage = $event" />
    </template>
  </el-dialog>
</template>

<style>
.compilation-preview-dialog { height: 94dvh; max-width: 1800px; display: flex; flex-direction: column; margin-bottom: 0; box-sizing: border-box; }
.compilation-preview-dialog .el-dialog__header { flex: none; padding-right: 36px; }
.compilation-preview-dialog .el-dialog__body { display: flex; flex-direction: column; flex: 1; min-height: 0; overflow: hidden; }
.compilation-info { display: flex; flex-wrap: wrap; gap: 8px 18px; margin-bottom: 10px; font-size: 12px; line-height: 1.6; overflow-wrap: anywhere; }
.compilation-info span { flex: 1 1 240px; color: #606266; }
.compilation-info a { color: #409eff; flex: none; }
@media (max-width: 600px) {
  .compilation-preview-dialog.el-dialog:not(.is-fullscreen) { width: 100% !important; height: 100dvh; margin: 0 !important; border-radius: 0; padding: 12px; }
}
</style>
