<template>
  <div class="app-container textbook-shelf">
    <div class="shelf-head">
      <div>
        <h2 class="shelf-title">电子教材与教学资源</h2>
      </div>
      <el-input
        v-model="keyword"
        class="shelf-search"
        placeholder="搜索年级或册次"
        clearable
        :prefix-icon="Search"
      />
    </div>

    <div v-loading="loading" class="shelf-body">
      <template v-for="group in visibleGroups" :key="group.key">
        <div v-if="group.books.length || group.pending.length" class="stage-block">
          <div class="stage-title">{{ group.title }}</div>
          <div class="book-grid">
            <div
              v-for="book in group.books"
              :key="book.bookId"
              class="book-card"
              @click="openBook(book)"
            >
              <div class="book-cover">
                <LazyPageImage
                  :book-id="book.bookId"
                  :page-no="1"
                  :alt="book.title"
                  cover
                  eager
                />
              </div>
              <div class="book-name" :title="book.title">{{ book.shortTitle }} <el-tag v-if="caseBookIds.has(Number(book.bookId))" size="small">优秀课例</el-tag></div>
              <div class="book-meta">{{ book.unitCount }} {{ Number(book.grade) > 9 ? '章' : '单元' }} · {{ book.lessonCount }} {{ Number(book.grade) > 9 ? '节' : '课' }}</div>
              <div class="book-actions">
                <span class="book-open">打开阅读</span>
                <el-button
                  class="book-download"
                  link
                  type="primary"
                  size="small"
                  :loading="downloadingBookId === book.bookId"
                  :disabled="downloadingBookId !== null && downloadingBookId !== book.bookId"
                  @click.stop="downloadBook(book)"
                >
                  下载整册
                </el-button>
              </div>
              <div v-if="bookCompilations.has(Number(book.bookId))" class="book-compilation">
                <div class="compilation-heading">
                  <span>教学设计汇编</span>
                </div>
                <div class="compilation-actions">
                  <el-button link type="primary" size="small" @click.stop="previewCompilation(book)">预览汇编</el-button>
                  <el-button link size="small" @click.stop="downloadCompilation(book)">下载</el-button>
                </div>
              </div>
            </div>

            <div v-for="item in group.pending" :key="`pending-${item.grade}-${item.semester}`" class="book-card is-pending">
              <div class="book-cover pending-cover">
                <el-icon><Document /></el-icon>
              </div>
              <div class="book-name">{{ item.shortTitle }}</div>
              <div class="book-meta">暂未上架</div>
            </div>
          </div>
        </div>
      </template>

      <div v-if="visibleGuides.length" class="stage-block guide-block">
        <div class="stage-title">教学指南与配套课件</div>
        <div class="guide-grid">
          <div v-for="guide in visibleGuides" :key="guide.grade" class="guide-card" @click="openGuide(guide)">
            <div class="guide-grade">{{ guide.grade }} 年级</div>
            <div class="guide-name">课程教学指南</div>
            <div class="guide-meta">{{ guide.lessonCount }} 份课件</div>
            <el-tag v-if="Number(guide.missingCount)" size="small" type="warning" effect="plain">暂缺 {{ guide.missingCount }} 课</el-tag>
            <span class="book-open">查看指南</span>
          </div>
        </div>
      </div>

      <div v-if="standards.length" class="stage-block guide-block">
        <div class="stage-title">课程标准</div>
        <div class="guide-grid">
          <div v-for="standard in visibleStandards" :key="standard.standardId" class="guide-card standard-card" @click="previewStandard(standard)">
            <div class="guide-grade">{{ standard.audience }}</div>
            <div class="guide-name">{{ standard.title }}</div>
            <div class="guide-meta">PDF · {{ formatBytes(standard.bytes) }}</div>
            <div class="standard-actions">
              <span class="book-open">在线阅读</span>
              <el-button link type="primary" size="small" @click.stop="downloadStandard(standard)">下载 PDF</el-button>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && !visibleGroups.length && !visibleGuides.length && !visibleStandards.length" description="没有匹配的教材、指南或标准" />
    </div>

    <p v-if="caseSummaryError" class="shelf-tip">教学资源标记加载失败。<el-button link type="primary" @click="loadCaseSummary">重试</el-button></p>
    <p v-if="guideError" class="shelf-tip">课程指南加载失败。<el-button link type="primary" @click="loadResources">重试</el-button></p>
    <p v-if="standardError" class="shelf-tip">课程标准加载失败。<el-button link type="primary" @click="loadStandards">重试</el-button></p>
    <p v-if="compilationError" class="shelf-tip">教学设计汇编加载失败。<el-button link type="primary" @click="loadResources">重试</el-button></p>
    <CompilationPreview v-model="compilationVisible" :resource="selectedCompilation" />
    <el-dialog v-model="standardVisible" :title="selectedStandard?.title || '课程标准'" class="standard-preview-dialog" width="96%" top="3vh" append-to-body destroy-on-close>
      <CasePreview
        v-if="standardVisible && selectedStandard"
        :key="selectedStandard.standardKey"
        :resource-id="selectedStandard.standardKey"
        source="standard"
        :title="selectedStandard.title"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { saveAs } from 'file-saver'
import { Document, Search } from '@element-plus/icons-vue'
import { listTextbookBooks, downloadTextbookBookPdf, TEXTBOOK_PENDING_VOLUMES } from '@/api/business/textbook'
import { listBookResources, bookResourceDownloadUrl, getGuides, getStandards, standardDownloadUrl } from '@/api/business/textbookResource'
import LazyPageImage from './components/LazyPageImage.vue'
import CompilationPreview from './components/CompilationPreview.vue'
import CasePreview from './components/CasePreview.vue'
import { getCaseSummary } from '@/api/business/textbookCase'

const router = useRouter()
const loading = ref(false)
const books = ref([])
const caseBookIds = ref(new Set())
const caseSummaryError = ref(false)
const bookCompilations = ref(new Map())
const guides = ref([])
const guideError = ref(false)
const standards = ref([])
const standardError = ref(false)
const standardVisible = ref(false)
const selectedStandard = ref(null)
const compilationError = ref(false)
const compilationVisible = ref(false)
const selectedCompilation = ref(null)

async function loadCaseSummary() {
  caseSummaryError.value = false
  try {
    const res = await getCaseSummary()
    caseBookIds.value = new Set((res.data || []).map(row => Number(row.bookId)))
  } catch {
    caseSummaryError.value = true
  }
}
onMounted(loadCaseSummary)
onMounted(loadResources)
onMounted(loadStandards)
const keyword = ref('')
const downloadingBookId = ref(null)

const primaryGrades = [3, 4, 5, 6]
const juniorGrades = [7, 8, 9]
const seniorGrades = [10, 11, 12]

async function loadResources() {
  compilationError.value = false
  guideError.value = false
  try {
    const res = await listBookResources()
    const map = new Map()
    for (const item of res.data || []) {
      if (item.category === 'DESIGN' && !map.has(Number(item.bookId))) map.set(Number(item.bookId), item)
    }
    bookCompilations.value = map
  } catch { compilationError.value = true }
  try {
    const res = await getGuides()
    guides.value = res.data || []
  } catch {
    guideError.value = true
  }
}

function previewCompilation(book) {
  selectedCompilation.value = bookCompilations.value.get(Number(book.bookId))
  compilationVisible.value = true
}

const visibleGuides = computed(() => {
  const key = keyword.value.trim()
  return guides.value.filter(guide => !key || `${guide.grade}年级${guide.title}`.includes(key))
})

function openGuide(guide) {
  router.push(`/business/textbook/guide/${guide.grade}`)
}

async function loadStandards() {
  standardError.value = false
  try {
    const res = await getStandards()
    standards.value = res.data || []
  } catch {
    standardError.value = true
  }
}

function previewStandard(standard) {
  selectedStandard.value = standard
  standardVisible.value = true
}

function downloadStandard(standard) {
  const link = document.createElement('a')
  link.href = standardDownloadUrl(standard.standardKey)
  link.download = standard.filename
  link.target = '_blank'
  link.rel = 'noopener'
  document.body.appendChild(link)
  link.click()
  link.remove()
}

function formatBytes(value) {
  const bytes = Number(value) || 0
  if (bytes >= 1024 * 1024) return `${(bytes / 1024 / 1024).toFixed(1)}MB`
  return `${Math.max(1, Math.round(bytes / 1024))}KB`
}

const visibleStandards = computed(() => {
  const key = keyword.value.trim()
  return standards.value.filter(item => !key || `${item.audience}${item.title}`.includes(key))
})

function downloadCompilation(book) {
  const item = bookCompilations.value.get(Number(book.bookId))
  if (!item) return
  const link = document.createElement('a')
  link.href = bookResourceDownloadUrl(item.id)
  link.download = item.filename
  link.target = '_blank'
  link.rel = 'noopener'
  document.body.appendChild(link)
  link.click()
  link.remove()
}

function matches(book) {
  const key = keyword.value.trim()
  if (!key) return true
  return `${book.title}${book.shortTitle}第${book.grade}册`.includes(key)
}

const visibleGroups = computed(() => {
  const build = (key, title, grades) => {
    const list = books.value
      .filter(book => grades.includes(Number(book.grade)) && matches(book))
      .sort((a, b) => (a.grade - b.grade) || String(a.semester).localeCompare(String(b.semester)))
    const pending = TEXTBOOK_PENDING_VOLUMES
      .filter(item => !list.some(book => Number(book.grade) === item.grade && String(book.semester) === item.semester))
      .filter(item => grades.includes(item.grade) && (!keyword.value.trim() || item.shortTitle.includes(keyword.value.trim())))
    return { key, title, books: list, pending }
  }
  return [
    build('primary', '小学', primaryGrades),
    build('junior', '初中', juniorGrades),
    build('senior', '高中', seniorGrades)
  ].filter(group => group.books.length || group.pending.length)
})

function openBook(book) {
  router.push(`/business/textbook/read/${book.bookId}`)
}

async function downloadBook(book) {
  if (downloadingBookId.value !== null) return
  downloadingBookId.value = book.bookId
  try {
    const blob = await downloadTextbookBookPdf(book.bookId)
    saveAs(blob, `${book.title || book.shortTitle || '电子教材'}.pdf`)
  } catch (error) {
    ElMessage.error(error?.message || '整册 PDF 下载失败，请稍后重试')
  } finally {
    downloadingBookId.value = null
  }
}

async function load() {
  loading.value = true
  try {
    const res = await listTextbookBooks()
    books.value = res.data || []
  } catch (error) {
    ElMessage.error('教材列表加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.textbook-shelf {
  padding-bottom: 24px;
}

.shelf-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 28px;
}

.shelf-title {
  margin: 0;
  font-size: 22px;
  color: #303133;
}

.shelf-search {
  width: 220px;
}

.shelf-body {
  min-height: 220px;
}

.stage-block + .stage-block {
  margin-top: 32px;
}

.stage-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}

.book-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(min(168px, 100%), 1fr));
  gap: 18px;
}

.book-card {
  min-width: 0;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.book-card:hover {
  box-shadow: 0 4px 14px rgba(30, 45, 65, 0.06);
  border-color: #b9cde3;
}

.book-card.is-pending {
  cursor: default;
  opacity: 0.62;
  border-style: dashed;
}

.book-card.is-pending:hover {
  transform: none;
  box-shadow: none;
  border-color: #ebeef5;
}

.book-cover {
  aspect-ratio: 3 / 4;
  overflow: hidden;
  border-radius: 4px;
  background: #f5f7fa;
}

.book-cover :deep(.textbook-page) {
  height: 100%;
  box-shadow: none;
}

.pending-cover {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34px;
  color: #c0c4cc;
}

.book-name {
  margin-top: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.book-meta {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.book-compilation {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #f0f1f3;
}
.compilation-heading {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 4px 8px;
  font-size: 12px;
  color: #606266;
}
.compilation-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 8px;
}
.compilation-actions :deep(.el-button + .el-button) {
  margin-left: 0;
}
.book-compilation :deep(.el-button) {
  padding: 0;
  font-size: 12px;
}
.guide-block {
  border-top: 1px solid #ebeef5;
  padding-top: 28px;
}
.guide-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(min(220px, 100%), 1fr));
  gap: 14px;
}
.guide-card {
  min-width: 0;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  transition: border-color 0.2s;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
}
.guide-card:hover {
  border-color: #b9cde3;
}
.guide-grade {
  font-size: 18px;
  color: #303133;
  font-weight: 600;
}
.guide-name {
  font-size: 14px;
  color: #606266;
  line-height: 1.4;
}
.guide-meta {
  font-size: 12px;
  color: #909399;
}
.guide-card .book-open {
  margin-top: 2px;
}
.standard-card {
  justify-content: center;
}
.standard-card .guide-name {
  text-align: center;
}
.standard-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 4px;
}
@media (max-width: 390px) {
  .guide-grid {
    grid-template-columns: 1fr;
  }
}
.book-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 8px;
}
.book-open {
  font-size: 12px;
  color: #409eff;
}
.book-download {
  padding: 0;
  font-size: 12px;
}

.shelf-tip {
  margin: 22px 0 0;
  font-size: 12px;
  color: #a8abb2;
}
</style>

<style>
.standard-preview-dialog { height: 94dvh; max-width: 1800px; display: flex; flex-direction: column; margin-bottom: 0; box-sizing: border-box; }
.standard-preview-dialog .el-dialog__header { flex: none; padding-bottom: 12px; }
.standard-preview-dialog .el-dialog__title { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.standard-preview-dialog .el-dialog__body { display: flex; flex-direction: column; flex: 1; min-height: 0; overflow: hidden; }
@media (max-width: 600px) {
  .standard-preview-dialog.el-dialog:not(.is-fullscreen) { width: 100% !important; max-width: none; height: 100dvh; margin: 0 !important; border-radius: 0; padding: 12px; }
}
</style>
