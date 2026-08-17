<template>
  <footer class="copyright" aria-label="开发支持">
    <a :href="developerSiteUrl" target="_blank" rel="noopener noreferrer">开发支持：象山县-郑东旭</a>
  </footer>
</template>

<script setup>
const developerSiteUrl = import.meta.env.VITE_DEVELOPER_SITE_URL || 'http://10.52.1.123:3012/'
</script>

<style scoped>
.copyright {
  position: fixed;
  right: 14px;
  bottom: 10px;
  padding: 5px 9px;
  text-align: right;
  color: var(--el-text-color-secondary);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 999px;
  background: color-mix(in srgb, var(--el-bg-color) 88%, transparent);
  box-shadow: 0 6px 18px rgb(15 23 42 / 8%);
  font-size: 11px;
  line-height: 1.2;
  user-select: none;
  backdrop-filter: blur(8px);
  z-index: 999;
}

.copyright a {
  color: inherit;
  text-decoration: none;
}
</style>
