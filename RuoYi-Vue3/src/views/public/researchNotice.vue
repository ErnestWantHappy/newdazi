<template>
  <main class="public-notice" v-loading="loading">
    <el-result v-if="error" icon="warning" title="该通知不存在或已失效" sub-title="请向发布者索取新的分享链接" />
    <article v-else-if="notice" class="notice-content">
      <h1>{{ notice.title }}</h1>
      <div class="meta">{{ notice.creatorName || '未知发布人' }} · {{ notice.deptName || '未标注学校' }} · {{ notice.createTime }}<template v-if="notice.activityTime"> · 活动时间 {{ notice.activityTime }}</template></div>
      <div class="rich-content" v-html="renderContent(notice.contentHtml)" />
    </article>
  </main>
</template>
<script setup>
import { getPublicResearchNotice } from '@/api/business/researchActivity.js'
import { isResearchNoticeImageSource } from '@/views/business/researchActivity/utils/publicNoticeImage.js'
const route = useRoute(); const loading = ref(true); const error = ref(false); const notice = ref(null); const token = computed(() => String(route.params.token || '')); const baseApi = import.meta.env.VITE_APP_BASE_API || ''
function renderContent(html) { if (!html) return ''; const wrapper = document.createElement('div'); wrapper.innerHTML = html; wrapper.querySelectorAll('img[src]').forEach(image => { const src = image.getAttribute('src') || ''; if (isResearchNoticeImageSource(src)) { image.setAttribute('src', `${baseApi}/business/research-activity/public/notices/${encodeURIComponent(token.value)}/images?src=${encodeURIComponent(src)}`) } image.setAttribute('loading', 'lazy') }); return wrapper.innerHTML }
async function load() { loading.value = true; error.value = false; try { notice.value = await getPublicResearchNotice(token.value) } catch { error.value = true } finally { loading.value = false } }
onMounted(load)
</script>
<style scoped>
.public-notice { min-height: 100vh; padding: 32px 18px; background: #f5f7fa; }
.notice-content { max-width: 860px; margin: 0 auto; padding: 28px 32px; background: #fff; border: 1px solid #ebeef5; }
h1 { margin: 0; color: #1f2937; font-size: 28px; line-height: 1.35; }
.meta { margin: 14px 0 24px; color: #909399; font-size: 14px; line-height: 1.7; }
.rich-content { color: #303133; line-height: 1.8; overflow-wrap: anywhere; }
.rich-content :deep(img) { max-width: 100%; height: auto; }
.rich-content :deep(table) { max-width: 100%; border-collapse: collapse; }
.rich-content :deep(td), .rich-content :deep(th) { border: 1px solid #dcdfe6; padding: 6px; }
@media (max-width: 640px) { .public-notice { padding: 0; } .notice-content { min-height: 100vh; padding: 22px 18px; border: 0; } h1 { font-size: 23px; } }
</style>
