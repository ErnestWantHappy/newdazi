<template>
  <footer v-if="showCopyright" class="copyright" aria-label="平台版权信息">
    版权所有：象山县教育局教科研中心
    <span class="copyright-separator">·</span>
    地址：浙江省象山县高教园区教科研中心
    <span class="copyright-separator">·</span>
    浙ICP备05007927号
  </footer>
</template>

<script setup>
import useUserStore from '@/store/modules/user'

const userStore = useUserStore()
const showCopyright = computed(() => {
  const roles = userStore.roles || []
  return roles.includes('teacher') || roles.includes('researcher')
})
</script>

<style scoped>
.copyright {
  width: 100%;
  box-sizing: border-box;
  padding: 14px 20px 18px;
  color: #9aa3ad;
  border-top: 1px solid #eef1f4;
  background: #fff;
  font-family: Arial, "Microsoft YaHei", sans-serif;
  font-size: 11px;
  line-height: 1.8;
  text-align: center;
  letter-spacing: 0.1px;
}

.copyright-separator {
  padding: 0 8px;
  color: #c7cdd4;
}

@media (max-width: 900px) {
  .copyright {
    padding: 12px 16px 16px;
    font-size: 10px;
  }

  .copyright-separator {
    padding: 0 4px;
  }
}
</style>
