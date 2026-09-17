<template>
  <main class="public-notice" v-loading="loading">
    <el-result v-if="error" icon="warning" title="该通知不存在或已失效" sub-title="请向发布者索取新的分享链接" />
    <article v-else-if="notice" class="notice-content">
      <header class="notice-header">
        <div class="notice-badge">教研活动通知</div>
        <h1 class="notice-title">{{ notice.title }}</h1>
        <div class="notice-meta">
          <span class="meta-author"><el-icon><User /></el-icon> {{ notice.creatorName || '未知发布人' }}</span>
          <span v-if="notice.deptName" class="meta-dept"><el-icon><School /></el-icon> {{ notice.deptName }}</span>
          <span class="meta-time"><el-icon><Clock /></el-icon> {{ notice.createTime }}</span>
          <span v-if="notice.activityTime" class="meta-act-time">
            <el-tag type="warning" size="small" effect="plain">活动时间：{{ notice.activityTime }}</el-tag>
          </span>
        </div>
      </header>

      <div class="rich-content main-body" v-html="renderContent(notice.contentHtml)" />

      <!-- 下方留言交流区 -->
      <section class="notice-posts">
        <div class="posts-head">
          <h2 class="posts-title">
            <span>教师留言与研讨</span>
            <el-tag type="info" size="small" round class="count-tag">{{ postsTotal }} 条</el-tag>
          </h2>
        </div>

        <div v-loading="postsLoading" class="posts-wrapper">
          <el-alert
            v-if="postsError"
            type="warning"
            :closable="false"
            show-icon
            title="留言暂时无法加载，请稍后刷新。通知正文仍可阅读。"
            class="posts-error"
          />
          <div v-else-if="posts.length > 0" class="posts-list">
            <div v-for="(post, i) in posts" :key="post.postId || i" class="post-item">
              <div class="post-avatar">
                {{ (post.authorName || '教').charAt(0) }}
              </div>
              <div class="post-main">
                <div class="post-info">
                  <div class="author-info">
                    <span class="author-name">{{ post.authorName || '匿名教师' }}</span>
                    <span v-if="post.deptName" class="dept-badge">{{ post.deptName }}</span>
                  </div>
                  <div class="post-date">
                    <span>{{ post.createTime }}</span>
                    <span v-if="post.edited" class="edited-tag">已编辑</span>
                  </div>
                </div>
                <div class="rich-content post-body" v-html="renderContent(post.contentHtml)" />
              </div>
            </div>

            <div v-if="postsTotal > postsPageSize" class="posts-pagination">
              <el-pagination
                v-model:current-page="postsPage"
                :page-size="postsPageSize"
                :total="postsTotal"
                layout="prev, pager, next"
                background
                small
                @current-change="loadPosts"
              />
            </div>
          </div>

          <div v-else-if="!postsLoading" class="posts-empty">
            <el-empty :image-size="64" description="暂无教师留言，登录平台后可参与互动交流" />
          </div>
        </div>
      </section>
    </article>
  </main>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { User, School, Clock } from '@element-plus/icons-vue'
import { getPublicResearchNotice, getPublicNoticePosts } from '@/api/business/researchActivity.js'
import { isResearchNoticeImageSource } from '@/views/business/researchActivity/utils/publicNoticeImage.js'

const route = useRoute()
const loading = ref(true)
const error = ref(false)
const notice = ref(null)
const token = computed(() => String(route.params.token || ''))
const baseApi = import.meta.env.VITE_APP_BASE_API || ''

const posts = ref([])
const postsTotal = ref(0)
const postsPage = ref(1)
const postsPageSize = 10
const postsLoading = ref(false)
const postsError = ref(false)

function renderContent(html) {
  if (!html) return ''
  const wrapper = document.createElement('div')
  wrapper.innerHTML = html
  wrapper.querySelectorAll('img[src]').forEach(image => {
    const src = image.getAttribute('src') || ''
    if (isResearchNoticeImageSource(src)) {
      image.setAttribute(
        'src',
        `${baseApi}/business/research-activity/public/notices/${encodeURIComponent(token.value)}/images?src=${encodeURIComponent(src)}`
      )
    }
    image.setAttribute('loading', 'lazy')
  })
  return wrapper.innerHTML
}

async function load() {
  loading.value = true
  error.value = false
  try {
    notice.value = await getPublicResearchNotice(token.value)
    loadPosts()
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

async function loadPosts() {
  postsLoading.value = true
  postsError.value = false
  try {
    const data = (await getPublicNoticePosts(token.value, postsPage.value, postsPageSize)) || {}
    posts.value = data.rows || []
    postsTotal.value = data.total || 0
  } catch {
    posts.value = []
    postsTotal.value = 0
    postsError.value = true
  } finally {
    postsLoading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.public-notice {
  min-height: 100vh;
  padding: 40px 20px;
  background: #f3f4f6;
}

.notice-content {
  max-width: 880px;
  margin: 0 auto;
  padding: 36px 44px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  border: 1px solid #e5e7eb;
}

.notice-header {
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f2f5;
}

.notice-badge {
  display: inline-block;
  padding: 3px 10px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 12px;
  font-weight: 600;
  border-radius: 4px;
  margin-bottom: 12px;
}

.notice-title {
  margin: 0 0 16px;
  color: #111827;
  font-size: 26px;
  font-weight: 700;
  line-height: 1.4;
}

.notice-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 16px;
  color: #6b7280;
  font-size: 13px;
}

.notice-meta span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.rich-content {
  color: #374151;
  font-size: 15px;
  line-height: 1.8;
  word-break: break-word;
}

.rich-content :deep(p) {
  margin-top: 0;
  margin-bottom: 1em;
}

.rich-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 8px 0;
}

.rich-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 12px 0;
}

.rich-content :deep(td),
.rich-content :deep(th) {
  border: 1px solid #e5e7eb;
  padding: 8px 12px;
}

.notice-posts {
  margin-top: 36px;
  border-top: 2px solid #f3f4f6;
  padding-top: 24px;
}

.posts-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.posts-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.count-tag {
  font-size: 12px;
}

.posts-wrapper {
  min-height: 120px;
}

.posts-error {
  margin-bottom: 12px;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-item {
  display: flex;
  gap: 14px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 12px;
  border: 1px solid #f0f2f5;
  transition: all 0.2s ease;
}

.post-item:hover {
  background: #ffffff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
}

.post-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #3b82f6;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 15px;
  flex-shrink: 0;
}

.post-main {
  flex: 1;
  min-width: 0;
}

.post-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-name {
  font-weight: 600;
  color: #374151;
  font-size: 14px;
}

.dept-badge {
  font-size: 12px;
  color: #6b7280;
  background: #e5e7eb;
  padding: 1px 6px;
  border-radius: 4px;
}

.post-date {
  font-size: 12px;
  color: #9ca3af;
  display: flex;
  align-items: center;
  gap: 6px;
}

.edited-tag {
  color: #f59e0b;
  font-size: 11px;
}

.post-body {
  font-size: 14px;
  color: #4b5563;
}

.posts-pagination {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

.posts-empty {
  padding: 24px 0;
}

@media (max-width: 640px) {
  .public-notice {
    padding: 0;
  }
  .notice-content {
    min-height: 100vh;
    padding: 24px 16px;
    border-radius: 0;
    border: none;
    box-shadow: none;
  }
  .notice-title {
    font-size: 22px;
  }
  .post-item {
    padding: 12px;
  }
}
</style>
