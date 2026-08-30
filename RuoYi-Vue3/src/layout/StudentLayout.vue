<template>
  <div class="student-layout">
    <app-main />
  </div>
</template>

<script setup>
import { AppMain } from './components'
</script>

<style lang="scss" scoped>
.student-layout {
  min-height: 100vh;
}

/* AppMain 默认隐藏溢出会成为 sticky 的非滚动祖先，导致学生页顶部导航随页面滚走。 */
.student-layout :deep(.app-main) {
  overflow: visible;
}
</style>
