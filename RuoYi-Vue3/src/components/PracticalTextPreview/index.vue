<template>
  <div v-loading="loading" class="text-work-preview">
    <el-alert v-if="error" :title="error" type="warning" :closable="false" />
    <template v-else-if="data">
      <el-alert v-if="data.fileKind === 'HTML'" title="单个网页支持内联 CSS 和 JavaScript；外部资源和跳转受限。" type="info" :closable="false" />
      <el-radio-group v-if="data.fileKind === 'HTML'" v-model="mode" class="preview-modes">
        <el-radio-button value="effect">网页效果</el-radio-button>
        <el-radio-button value="source">源码</el-radio-button>
      </el-radio-group>
      <iframe v-if="data.fileKind === 'HTML' && mode === 'effect'" :srcdoc="document" sandbox="allow-scripts" referrerpolicy="no-referrer" title="学生网页作品效果" class="html-work" />
      <pre v-else class="source-work">{{ data.text }}</pre>
    </template>
  </div>
</template>
<script setup>
import { computed, ref, watch } from 'vue';
import request from '@/utils/request';
import { buildPracticalHtmlPreview } from '@/utils/practicalHtmlPreview';
const props = defineProps({ attachmentId: { type: [Number, String], required: true } });
const loading = ref(false), error = ref(''), data = ref(null), mode = ref('effect');
let sequence = 0;
const document = computed(() => buildPracticalHtmlPreview(data.value?.text));
watch(() => props.attachmentId, async (id) => {
  const ticket = ++sequence;
  data.value = null; error.value = ''; mode.value = 'effect';
  if (!id) return;
  loading.value = true;
  try {
    const response = await request({ url: `/business/practical-attachment/${id}/text`, method: 'get' });
    if (ticket === sequence) data.value = response.data;
  } catch (e) { if (ticket === sequence) error.value = '文本预览暂不可用，可下载原文件查看。'; }
  finally { if (ticket === sequence) loading.value = false; }
}, { immediate: true });
</script>
<style scoped>
.text-work-preview { width: 100%; min-height: 180px; }
.preview-modes { margin: 12px 0; }
.html-work { width: 100%; min-height: 520px; border: 1px solid #dcdfe6; background: white; }
.source-work { white-space: pre-wrap; overflow-wrap: anywhere; font: 15px/1.6 Consolas, monospace; color: #000; background: #fff; padding: 16px; max-height: 65vh; overflow: auto; }
</style>
