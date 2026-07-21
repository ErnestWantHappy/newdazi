<template>
  <section class="advanced-shell">
    <div class="advanced-toolbar">
      <div>
        <strong>高级模式</strong>
        <span>适合需要精细布局、事件和扩展能力的熟练教师</span>
      </div>
      <el-button :icon="Back" @click="$emit('return-beginner')">返回新手模式</el-button>
    </div>
    <slot />
  </section>
</template>

<script setup>
import { Back } from '@element-plus/icons-vue'
defineEmits(['return-beginner'])
</script>

<style scoped>
.advanced-shell {
  min-height: calc(100vh - 84px);
  min-width: 0;
  background: #f4f7f8;
}
.advanced-shell :deep(.app-container) {
  min-width: 0;
  max-width: 100%;
  box-sizing: border-box;
}
.advanced-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 20px;
  border-bottom: 1px solid #d9e2e5;
  background: #fff;
}
.advanced-toolbar strong, .advanced-toolbar span { display: block; }
.advanced-toolbar strong { color: #213c46; }
.advanced-toolbar span { margin-top: 2px; color: #7c8c92; font-size: 12px; }
</style>
