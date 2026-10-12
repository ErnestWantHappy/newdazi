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
/* 预览区撑满父容器，源码/网页在内部滚动，避免被祖先 overflow:hidden 裁掉。
   min-height 保留兜底值，使本组件在非 flex 父容器（如学生端弹窗）中也不会塌陷。 */
.text-work-preview { width: 100%; flex: 1; min-height: 0; display: flex; flex-direction: column; overflow: hidden; }
.preview-modes { margin: 12px 0; flex: none; }
.html-work { flex: 1; width: 100%; min-height: 320px; border: 1px solid #dcdfe6; background: white; }
.source-work { flex: 1; min-height: 200px; white-space: pre-wrap; overflow-wrap: anywhere; font: 15px/1.6 Consolas, monospace; color: #000; background: #fff; padding: 16px; overflow: auto; }
</style>
