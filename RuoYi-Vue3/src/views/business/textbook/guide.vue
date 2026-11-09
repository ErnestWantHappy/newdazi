<template>
  <div class="app-container guide-page">
    <header class="guide-head">
      <el-button text class="back-btn" @click="back">
        <el-icon><ArrowLeft /></el-icon>
        <span>资源书架</span>
      </el-button>
      <div class="head-title">
        <h2>{{ grade }} 年级 · {{ guide?.title || '课程教学指南' }}</h2>
      </div>
    </header>

    <div v-if="loading" class="guide-state">正在加载指南…</div>
    <div v-else-if="unavailable" class="guide-state is-empty">
      <p>{{ grade }} 年级指南暂未上架</p>
      <p class="state-detail">当前提供 3—8 年级课程教学指南，请返回资源书架选择已上架年级。</p>
      <el-button type="primary" plain @click="back">返回资源书架</el-button>
    </div>
    <div v-else-if="error" class="guide-state is-error">
      指南加载失败
      <el-button size="small" @click="load">重试</el-button>
    </div>
    <template v-else>
      <section class="guide-pdf-card">
        <div>
          <strong>课程教学指南（PDF）</strong>
        </div>
        <div class="guide-pdf-actions">
          <el-button type="primary" plain @click="openGuidePdf">在线阅读</el-button>
          <el-button @click="openUrl(guideDownloadUrl(grade))">下载指南 PDF</el-button>
          <el-button v-if="guide?.usageFilename" text type="primary" @click="openUrl(guideUsageUrl(grade))">下载使用说明</el-button>
        </div>
      </section>

      <section class="lesson-section">
        <div class="section-head">
          <h3>配套课件</h3>
          <span>{{ lessons.filter(item => Number(item.available)).length }} 份<template v-if="missingLesson"> · 第 {{ missingLesson.lessonNo }} 课暂缺</template></span>
        </div>
        <div class="lesson-grid">
          <article v-for="lesson in lessons" :key="lesson.lessonNo" class="lesson-card" :class="{ 'is-missing': !Number(lesson.available) }">
            <div class="lesson-no">第 {{ lesson.lessonNo }} 课</div>
            <div class="lesson-title" :title="lesson.title">{{ lesson.title || '课次待补充' }}</div>
            <template v-if="Number(lesson.available)">
              <div class="lesson-actions">
                <el-button size="small" type="primary" plain @click="previewLesson(lesson)">预览</el-button>
                <el-button size="small" :loading="downloading === lesson.lessonId" :disabled="downloading !== null" @click="downloadLesson(lesson)">下载课件</el-button>
              </div>
            </template>
            <template v-else>
              <el-tag size="small" type="warning" effect="plain">暂未提供</el-tag>
            </template>
          </article>
        </div>
      </section>
    </template>

    <el-dialog v-model="previewVisible" :title="previewTitle" class="guide-preview-dialog" width="96%" top="3vh" append-to-body destroy-on-close>
      <CasePreview
        v-if="previewVisible && previewSource"
        :key="`${previewSource}-${previewId}`"
        :resource-id="previewId"
        :source="previewSource"
        :title="previewTitle"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getGuide, guideDownloadUrl, guideUsageUrl, guideLessonDownloadUrl } from '@/api/business/textbookResource'
import CasePreview from './components/CasePreview.vue'

const route = useRoute()
const router = useRouter()
const grade = computed(() => Number(route.params.grade))
const guide = ref(null)
const lessons = ref([])
const loading = ref(false)
const error = ref(false)
const unavailable = ref(false)
const downloading = ref(null)
const previewVisible = ref(false)
const previewSource = ref('')
const previewId = ref(0)
const previewTitle = ref('')

const missingLesson = computed(() => lessons.value.find(item => !Number(item.available)))
let loadSequence = 0

async function load() {
  const ticket = ++loadSequence
  previewVisible.value = false
  guide.value = null
  lessons.value = []
  loading.value = true
  error.value = false
  unavailable.value = false
  try {
    const res = await getGuide(grade.value)
    if (ticket !== loadSequence) return
    guide.value = res.data || null
    lessons.value = (res.data?.lessons || []).slice().sort((a, b) => Number(a.lessonNo) - Number(b.lessonNo))
  } catch (failure) {
    if (ticket === loadSequence) {
      // “暂未上架”是明确空态，不是网络故障；反复重试不会产生资源。
      unavailable.value = String(failure?.message || '').includes('暂未上架')
      error.value = !unavailable.value
    }
  } finally {
    if (ticket === loadSequence) loading.value = false
  }
}

function back() {
  router.push('/textbook')
}

function openUrl(url) {
  const link = document.createElement('a')
  link.href = url
  link.target = '_blank'
  link.rel = 'noopener'
  document.body.appendChild(link)
  link.click()
  link.remove()
}

function openGuidePdf() {
  previewSource.value = 'guide'
  previewId.value = grade.value
  previewTitle.value = `${grade.value} 年级课程教学指南`
  previewVisible.value = true
}

function previewLesson(lesson) {
  previewSource.value = 'guideLesson'
  previewId.value = Number(lesson.lessonId)
  previewTitle.value = `第${lesson.lessonNo}课 ${lesson.title}`
  previewVisible.value = true
}

function downloadLesson(lesson) {
  if (downloading.value !== null) return
  downloading.value = lesson.lessonId
  openUrl(guideLessonDownloadUrl(lesson.lessonId))
  window.setTimeout(() => { downloading.value = null }, 800)
}

watch(grade, load, { immediate: true })
</script>

<style scoped>
.guide-page { padding-bottom: 30px; }
.guide-head { display: flex; align-items: flex-start; gap: 10px; margin-bottom: 18px; }
.back-btn { flex: none; padding-left: 0; }
.head-title h2 { margin: 0 0 4px; font-size: 20px; color: #303133; overflow-wrap: anywhere; }
.guide-state { padding: 40px 0; text-align: center; color: #909399; }
.guide-state p { margin: 0 0 10px; }
.guide-state.is-empty { color: #606266; }
.guide-state .state-detail { font-size: 12px; color: #909399; }
.guide-state.is-error { color: #c45656; }
.guide-pdf-card { display: flex; align-items: center; justify-content: space-between; gap: 16px; flex-wrap: wrap; padding: 16px; border: 1px solid #ebeef5; border-radius: 8px; background: #f8fbff; }
.guide-pdf-card strong { font-size: 15px; color: #303133; }
.guide-pdf-actions { display: flex; flex-wrap: wrap; gap: 8px; }
.guide-pdf-actions :deep(.el-button) { margin-left: 0; }
.lesson-section { margin-top: 24px; }
.section-head { display: flex; align-items: baseline; justify-content: space-between; gap: 12px; flex-wrap: wrap; margin-bottom: 12px; }
.section-head h3 { margin: 0; font-size: 16px; color: #303133; }
.section-head span { font-size: 12px; color: #909399; }
.lesson-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(min(235px, 100%), 1fr)); gap: 12px; }
.lesson-card { min-width: 0; padding: 14px; border: 1px solid #ebeef5; border-radius: 8px; background: #fff; display: flex; flex-direction: column; align-items: flex-start; gap: 7px; }
.lesson-card.is-missing { background: #fafafa; border-style: dashed; }
.lesson-no { font-size: 12px; color: #409eff; font-weight: 600; }
.lesson-card.is-missing .lesson-no { color: #a8abb2; }
.lesson-title { font-size: 14px; font-weight: 600; color: #303133; line-height: 1.5; overflow-wrap: anywhere; }
.lesson-actions { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 2px; }
.lesson-actions :deep(.el-button) { margin-left: 0; }
@media (max-width: 390px) {
  .guide-head { flex-direction: column; }
  .guide-pdf-card { padding: 12px; }
  .guide-pdf-actions { width: 100%; }
  .guide-pdf-actions :deep(.el-button) { flex: 1 1 auto; }
  .lesson-grid { grid-template-columns: 1fr; }
}
</style>

<style>
.guide-preview-dialog { height: 94dvh; max-width: 1800px; display: flex; flex-direction: column; margin-bottom: 0; box-sizing: border-box; }
.guide-preview-dialog .el-dialog__header { flex: none; padding-bottom: 12px; }
.guide-preview-dialog .el-dialog__title { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.guide-preview-dialog .el-dialog__body { display: flex; flex-direction: column; flex: 1; min-height: 0; overflow: hidden; }
@media (max-width: 600px) {
  .guide-preview-dialog.el-dialog:not(.is-fullscreen) { width: 100% !important; max-width: none; height: 100dvh; margin: 0 !important; border-radius: 0; padding: 12px; }
}
</style>
