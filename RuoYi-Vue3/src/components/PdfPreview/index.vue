<template>
  <!-- PDF预览对话框 -->
  <el-dialog
    v-model="dialogVisible"
    title="文件预览"
    width="70%"
    top="5vh"
    append-to-body
    destroy-on-close
  >
    <div v-loading="loading" style="height: 75vh;">
      <iframe
        v-if="pdfUrl"
        :src="pdfUrl"
        width="100%"
        height="100%"
        frameborder="0"
        @load="onFrameLoad"
      ></iframe>
      <div v-else class="empty-state">
        没有可预览的文件。
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue';

// 控制对话框是否显示
const dialogVisible = ref(false);
// 存储要预览的PDF文件URL
const pdfUrl = ref('');
// 控制加载状态
const loading = ref(false);

/**
 * 打开预览对话框
 * @param {string} url - 要预览的PDF文件的完整URL
 */
const open = (url) => {
  if (!url) {
    console.error("Preview URL is not provided.");
    return;
  }
  loading.value = true;
  pdfUrl.value = url;
  dialogVisible.value = true;
};

/**
 * iframe加载完成时的回调
 */
const onFrameLoad = () => {
  loading.value = false;
};

// 使用 defineExpose 将 open 方法暴露给父组件
defineExpose({
  open
});
</script>

<style scoped>
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #909399;
  font-size: 16px;
}
/* 覆盖el-dialog的默认内边距，让iframe铺满 */
:deep(.el-dialog__body) {
  padding: 0;
}
</style>
