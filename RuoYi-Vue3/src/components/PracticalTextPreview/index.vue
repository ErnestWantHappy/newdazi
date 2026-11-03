<template>
  <div ref="previewRoot" v-loading="loading" class="text-work-preview" :class="{ 'is-expanded': expanded }">
    <el-alert v-if="error" :title="error" type="warning" :closable="false" />
    <template v-else-if="data">
      <el-alert v-if="data.fileKind === 'HTML'" title="单个网页支持内联 CSS 和 JavaScript；外部资源和跳转受限。" type="info" :closable="false" />
      <div class="preview-toolbar">
        <el-radio-group v-if="data.fileKind === 'HTML'" v-model="mode" class="preview-modes">
          <el-radio-button value="effect">网页效果</el-radio-button>
          <el-radio-button value="source">源码</el-radio-button>
        </el-radio-group>
        <span v-else />
        <el-button type="primary" plain @click="toggleExpanded">{{ expanded ? '退出放大' : '放大查看' }}</el-button>
      </div>
      <iframe v-if="data.fileKind === 'HTML' && mode === 'effect'" :srcdoc="previewDocument" sandbox="allow-scripts" referrerpolicy="no-referrer" title="学生网页作品效果" class="html-work" />
      <pre v-else class="source-work">{{ data.text }}</pre>
    </template>
  </div>
</template>
<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import request from '@/utils/request';
import { buildPracticalHtmlPreview } from '@/utils/practicalHtmlPreview';
const props = defineProps({ attachmentId: { type: [Number, String], required: true } });
const loading = ref(false), error = ref(''), data = ref(null), mode = ref('effect');
const previewRoot = ref(null), expanded = ref(false);
let sequence = 0;
const previewDocument = computed(() => buildPracticalHtmlPreview(data.value?.text));
function handleFullscreenChange() {
  expanded.value = document.fullscreenElement === previewRoot.value;
}
async function toggleExpanded() {
  try {
    if (document.fullscreenElement === previewRoot.value) {
      await document.exitFullscreen();
      return;
    }
    if (previewRoot.value?.requestFullscreen) {
      await previewRoot.value.requestFullscreen();
      return;
    }
  } catch (_) {
    // 浏览器或嵌入环境拒绝全屏时，继续使用页面内放大，不中断批改。
  }
  // 极少数旧浏览器没有 Fullscreen API，仍提供固定定位的放大兜底。
  expanded.value = !expanded.value;
}
onMounted(() => document.addEventListener('fullscreenchange', handleFullscreenChange));
onBeforeUnmount(() => document.removeEventListener('fullscreenchange', handleFullscreenChange));
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
   min-height 保留兜底值，使本组件在非 flex 父容器（如学生端弹窗）中也不会塌陷。
   批改页里上方还有表单、提示条和工具栏，窄屏下剩余高度可能低于 iframe 自身的
   320px 下限；此时外层改为纵向滚动，保证网页效果和源码始终可见，不再只有放大
   或全屏才能看到内容。 */
.text-work-preview { width: 100%; flex: 1; min-height: 280px; display: flex; flex-direction: column; overflow-y: auto; overflow-x: hidden; }
.text-work-preview.is-expanded { position: fixed; inset: 16px; z-index: 4000; min-height: 0; padding: 16px; background: #f5f7fa; box-sizing: border-box; overflow: hidden; }
.text-work-preview:fullscreen { position: static; width: 100%; height: 100%; min-height: 100vh; padding: 16px; background: #f5f7fa; box-sizing: border-box; overflow: hidden; }
.preview-toolbar { display: flex; justify-content: space-between; align-items: center; gap: 12px; margin: 12px 0; flex: none; }
.preview-modes { flex: none; }
.html-work { flex: 1 1 auto; min-height: 320px; width: 100%; border: 1px solid #dcdfe6; background: white; }
.source-work { flex: 1 1 auto; min-height: 320px; white-space: pre-wrap; overflow-wrap: anywhere; font: 15px/1.6 Consolas, monospace; color: #000; background: #fff; padding: 16px; overflow: auto; }
</style>
