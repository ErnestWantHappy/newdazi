<template>
  <div class="reader" :class="{ 'is-student-reader': isStudentView }">
    <header class="reader-bar">
      <el-button text class="back-btn" @click="backToShelf">
        <el-icon><ArrowLeft /></el-icon>
        <span>{{ isStudentView ? '返回' : '书架' }}</span>
      </el-button>
      <div class="book-title" :title="book?.title">{{ book?.title || '电子教材' }}</div>

      <div class="bar-right">
        <el-select
          v-model="selectedKey"
          class="section-select"
          placeholder="选择课次"
          filterable
          clearable
          size="small"
          @change="onSelectSection"
        >
          <el-option-group v-for="unit in units" :key="unit.unitNo" :label="unit.title">
            <el-option
              v-if="unit.intro"
              :label="`单元导入 · ${unit.title}`"
              :value="unit.intro.sectionKey"
            />
            <el-option
              v-for="lesson in unit.lessons"
              :key="lesson.sectionKey"
              :label="`第${lesson.lessonNo}课 ${lesson.title}`"
              :value="lesson.sectionKey"
            />
            <el-option
              v-if="unit.summary"
              :label="`单元小结 · ${unit.title}`"
              :value="unit.summary.sectionKey"
            />
          </el-option-group>
        </el-select>

        <el-input-number
          v-model="jumpPage"
          class="page-jump"
          size="small"
          :min="1"
          :max="totalPages || 1"
          :controls="false"
          @change="onJump"
        />
        <span class="page-total">/ {{ totalPages }}</span>

        <div class="zoom-group">
          <el-button size="small" text class="zoom-btn" :disabled="!canZoomOut" title="缩小" @click="stepZoom(-1)">−</el-button>
          <el-select v-model="zoomMode" class="zoom-select" size="small" @change="persistZoom">
            <el-option label="适应整页" value="page" />
            <el-option label="适应宽度" value="width" />
            <el-option v-for="level in ZOOM_LEVELS" :key="level" :label="`${level}%`" :value="level" />
          </el-select>
          <el-button size="small" text class="zoom-btn" :disabled="!canZoomIn" title="放大" @click="stepZoom(1)">＋</el-button>
        </div>

        <el-button
          v-if="!isStudentView"
          size="small"
          text
          :loading="bookPdfLoading"
          title="下载整册 PDF"
          @click="downloadWholeBook"
        >
          <el-icon><Download /></el-icon>
          <span>下载整册</span>
        </el-button>
        <el-button size="small" type="primary" plain :disabled="!currentSection" @click="printSection">
          <el-icon><Printer /></el-icon>
          <span>打印本课</span>
        </el-button>
        <el-button
          v-if="!isStudentView"
          size="small"
          type="primary"
          plain
          :disabled="!currentLessonSection"
          :loading="sectionPdfLoading"
          title="保存本课 PDF"
          @click="saveSectionPdf"
        >
          <span>保存本课 PDF</span>
        </el-button>
        <el-button size="small" text class="toc-toggle" @click="tocVisible = !tocVisible">
          {{ tocVisible ? '收起目录' : '显示目录' }}
        </el-button>
      </div>
    </header>

    <div v-if="failedPages.size >= FAIL_BANNER_LIMIT" class="page-fail-banner">
      <span>{{ failedPageText }}加载失败，可能是教材图片服务暂时不可用；单页可点击重试。</span>
      <el-button size="small" @click="retryFailedPages">重试失败页</el-button>
    </div>

    <div v-if="loading" class="reader-loading">正在加载教材…</div>

    <div v-else-if="errorText" class="reader-loading is-error">
      {{ errorText }}
      <el-button size="small" @click="load">重试</el-button>
    </div>

    <div v-else class="reader-body">
      <aside v-show="tocVisible" class="toc">
        <div
          v-if="frontSection"
          class="toc-item is-muted"
          @click="goToPage(frontSection.pdfStart)"
        >
          封面 · 编者的话 · 目录
        </div>
        <div v-for="unit in units" :key="unit.unitNo" class="toc-unit">
          <div class="toc-unit-title">{{ unit.title }}</div>
          <div
            v-if="unit.intro"
            class="toc-item is-intro"
            :class="{ 'is-active': selectedKey === unit.intro.sectionKey }"
            @click="selectAndGo(unit.intro)"
          >
            单元导入
          </div>
          <div
            v-for="lesson in unit.lessons"
            :key="lesson.sectionKey"
            class="toc-item"
            :class="{ 'is-active': selectedKey === lesson.sectionKey }"
            @click="selectAndGo(lesson)"
          >
            <span class="toc-no">第{{ lesson.lessonNo }}课</span>
            <span class="toc-text">{{ lesson.title }}</span>
          </div>
          <div
            v-if="unit.summary"
            class="toc-item is-summary"
            :class="{ 'is-active': selectedKey === unit.summary.sectionKey }"
            @click="selectAndGo(unit.summary)"
          >
            单元小结与反思
          </div>
        </div>
        <div v-if="appendixSection" class="toc-item is-muted" @click="goToPage(appendixSection.pdfStart)">
          版权页 · 封底
        </div>
      </aside>

      <main ref="scrollEl" class="pages">
        <div v-if="!selectedKey" class="pick-hint">
          请先在右上角「选择课次」；选定后可跳转页码、打印本课。
        </div>

        <div class="pages-inner" :style="pageWidth ? { width: `${pageWidth}px` } : undefined">
          <div
            v-for="page in pages"
            :key="page.pageNo"
            :ref="el => registerPage(el, page.pageNo)"
            class="page-wrap"
            :data-page-no="page.pageNo"
            :class="{ 'is-outside': isOutsideSection(page.pageNo) }"
          >
            <LazyPageImage
              :book-id="bookId"
              :page-no="page.pageNo"
              :width="page.width"
              :height="page.height"
              :alt="`第 ${page.pageNo} 页`"
              :retry-token="retryTick"
              @page-failed="markPageFailed"
              @page-loaded="markPageLoaded"
            />
            <div class="page-label">{{ pageLabel(page) }}</div>
          </div>
        </div>
      </main>
    </div>

    <footer v-if="!loading && !errorText" class="reader-foot">
      <el-button text size="small" :disabled="currentPage <= 1" @click="goToPage(currentPage - 1)">
        上一页
      </el-button>
      <span class="foot-info">
        第 {{ currentPage }} 页
        <template v-if="currentSection">
          · {{ sectionLabel(currentSection) }}（{{ sectionRange(currentSection) }}）
        </template>
      </span>
      <el-button text size="small" :disabled="currentPage >= totalPages" @click="goToPage(currentPage + 1)">
        下一页
      </el-button>
    </footer>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { saveAs } from 'file-saver'
import { ArrowLeft, Download, Printer } from '@element-plus/icons-vue'
import {
  getTextbookToc,
  loadTextbookPage,
  clearTextbookPageCache,
  retainTextbookUrls,
  releaseTextbookUrls,
  downloadTextbookBookPdf,
  downloadTextbookSectionPdf
} from '@/api/business/textbook'
import LazyPageImage from './components/LazyPageImage.vue'

const route = useRoute()
const router = useRouter()

const bookId = computed(() => Number(route.params.bookId))
// 学生端复用同一阅读器，但返回目标与页面高度不同（学生布局没有顶部导航和页签栏）。
const isStudentView = computed(() => route.path.startsWith('/student'))
const loading = ref(true)
const errorText = ref('')
const book = ref(null)
const units = ref([])
const pages = ref([])
const frontSection = ref(null)
const appendixSection = ref(null)

const scrollEl = ref(null)
// 窄屏默认收起目录，避免目录挤占正文；桌面端保持展开
const tocVisible = ref(typeof window === 'undefined' || window.innerWidth >= 900)
const selectedKey = ref('')
const jumpPage = ref(1)
const currentPage = ref(1)

const totalPages = computed(() => book.value?.totalPages || pages.value.length || 0)

// 阅读缩放：默认“适应整页”，避免打开时一屏只看到页面上半部分；教师的选择会记住。
const ZOOM_LEVELS = [50, 60, 67, 75, 85, 100, 110, 125, 150, 175, 200]
const ZOOM_STORAGE_KEY = 'textbook-zoom-mode'

function readZoomMode() {
  try {
    const raw = localStorage.getItem(ZOOM_STORAGE_KEY)
    if (raw === 'page' || raw === 'width') return raw
    const value = Number(raw)
    return ZOOM_LEVELS.includes(value) ? value : 'page'
  } catch {
    return 'page'
  }
}

const zoomMode = ref(readZoomMode())
const viewportSize = reactive({ width: 0, height: 0 })
const pageAspect = computed(() => {
  const first = pages.value[0]
  return first?.width && first?.height ? first.width / first.height : 1400 / 1980
})
// 100% 仍沿用原来的阅读宽度，保证“适应宽度/100%”与旧版观感一致
const baseWidth = computed(() => Math.min(900, Math.max(120, viewportSize.width * 0.92)))
const pageWidth = computed(() => {
  if (!viewportSize.width || !viewportSize.height) return 0
  if (zoomMode.value === 'page') {
    // 同时受视口高度和宽度限制：窄屏上按宽度收窄，不会出现横向溢出
    const fitted = (viewportSize.height - 72) * pageAspect.value
    return Math.round(Math.max(120, Math.min(baseWidth.value, fitted)))
  }
  if (zoomMode.value === 'width') return Math.round(baseWidth.value)
  // 原图宽 1400px，放大到 1800px 只做轻微插值，保证各档位都有可见差异
  return Math.round(Math.min(1800, Math.max(120, baseWidth.value * Number(zoomMode.value) / 100)))
})
const currentPercent = computed(() => {
  if (zoomMode.value === 'page') return Math.round(pageWidth.value / Math.max(baseWidth.value, 1) * 100)
  if (zoomMode.value === 'width') return 100
  return Number(zoomMode.value)
})
const canZoomOut = computed(() => currentPercent.value > ZOOM_LEVELS[0])
const canZoomIn = computed(() => currentPercent.value < ZOOM_LEVELS[ZOOM_LEVELS.length - 1])

function persistZoom() {
  try {
    localStorage.setItem(ZOOM_STORAGE_KEY, String(zoomMode.value))
  } catch {
    // 隐私模式禁用 localStorage 时不影响阅读
  }
}

function stepZoom(direction) {
  const percent = currentPercent.value
  const next = direction > 0
    ? ZOOM_LEVELS.find(level => level > percent)
    : [...ZOOM_LEVELS].reverse().find(level => level < percent)
  if (next === undefined) return
  zoomMode.value = next
  persistZoom()
}

let zoomObserver = null

function refreshViewport() {
  const el = scrollEl.value
  if (!el) return
  viewportSize.width = el.clientWidth
  viewportSize.height = el.clientHeight
}

function setupZoomObserver() {
  refreshViewport()
  if (typeof ResizeObserver === 'undefined' || !scrollEl.value) return
  zoomObserver?.disconnect()
  zoomObserver = new ResizeObserver(refreshViewport)
  zoomObserver.observe(scrollEl.value)
}

/** 全部可选条目（单元导入 / 课次 / 单元小结），课次就是"本课" */
const sections = computed(() => {
  const list = []
  units.value.forEach(unit => {
    if (unit.intro) list.push(unit.intro)
    unit.lessons.forEach(lesson => list.push(lesson))
    if (unit.summary) list.push(unit.summary)
  })
  return list
})

const currentSection = computed(() => sections.value.find(item => item.sectionKey === selectedKey.value) || null)
// 后端“保存本课 PDF”只接受 LESSON；单元导入/小结虽可选中阅读，但没有可保存的本课 PDF。
const currentLessonSection = computed(() => currentSection.value?.kind === 'LESSON')

// 零散缺页由页内提示兜底；连续多页失败通常意味着 TEXTBOOK_ROOT 未配置或图片目录缺失，
// 需要在阅读器顶部给一条整体说明，避免老师以为整册教材都坏了。
const FAIL_BANNER_LIMIT = 3
const failedPages = reactive(new Set())
const retryTick = ref(0)
const failedPageText = computed(() => {
  const list = [...failedPages].sort((a, b) => a - b)
  return list.length > 8 ? `第 ${list.slice(0, 8).join('、')} 等 ${list.length} 页` : `第 ${list.join('、')} 页`
})

function markPageFailed(pageNo) {
  failedPages.add(pageNo)
}

function markPageLoaded(pageNo) {
  failedPages.delete(pageNo)
}

function retryFailedPages() {
  retryTick.value += 1
}

function sectionLabel(section) {
  if (!section) return ''
  if (section.kind === 'UNIT_INTRO') return '单元导入'
  if (section.kind === 'SUMMARY') return '单元小结与反思'
  return `第${section.lessonNo}课 ${section.title}`
}

function sectionRange(section) {
  if (!section) return ''
  const start = section.printedStart ?? section.pdfStart
  const end = section.printedEnd ?? section.pdfEnd
  return start === end ? `印刷页 ${start}` : `印刷页 ${start}–${end}`
}

function pageLabel(page) {
  return page.printedNo ? `${page.printedNo}` : `PDF ${page.pageNo}`
}

function isOutsideSection(pageNo) {
  if (!currentSection.value) return false
  return pageNo < currentSection.value.pdfStart || pageNo > currentSection.value.pdfEnd
}

async function load() {
  loading.value = true
  errorText.value = ''
  failedPages.clear()
  try {
    const res = await getTextbookToc(bookId.value)
    const data = res.data || {}
    book.value = data.book
    units.value = data.units || []
    pages.value = data.pages || []
    frontSection.value = data.front
    appendixSection.value = data.appendix
    document.title = data.book?.title || '电子教材'
  } catch (error) {
    errorText.value = error?.message || '教材加载失败'
    return
  } finally {
    loading.value = false
  }
  await nextTick()
  setupObserver()
  setupZoomObserver()
  // 页宽由视口测量决定，必须等宽度落到 DOM 后再算页位置，否则会滚到错误页码。
  await nextTick()
  applyPresetSection()
}

// 课程绑定的课次通过 query.section 传入：打开阅读器即定位到该课，不再要求老师手动选。
let presetTimer = null

function applyPresetSection(attempt = 0) {
  const presetKey = String(route.query.section || '')
  if (!presetKey) return
  const section = sections.value.find(item => item.sectionKey === presetKey)
  if (!section) {
    ElMessage.warning('教材课次已更新，请在右上角重新选择')
    return
  }
  selectedKey.value = section.sectionKey
  // 路由切换动画期间容器可能还没有宽度，页位置会算成 0；等布局就绪再滚。
  if (pageWidth.value <= 0 || !pageEls.get(section.pdfStart)) {
    if (attempt < 20) {
      presetTimer = window.setTimeout(() => applyPresetSection(attempt + 1), 50)
    }
    return
  }
  goToPage(section.pdfStart, 'auto')
}

// ----- 当前页跟踪：用 IntersectionObserver 只看顶部 8%–18% 的窄带，避免滚动时逐页取 rect -----
const pageEls = new Map()
const visiblePages = new Set()
let pageObserver = null

function registerPage(el, pageNo) {
  if (!el) {
    pageEls.delete(pageNo)
    return
  }
  pageEls.set(pageNo, el)
  pageObserver?.observe(el)
}

function setupObserver() {
  pageObserver?.disconnect()
  pageObserver = null
  visiblePages.clear()
  if (!scrollEl.value || typeof IntersectionObserver === 'undefined') return
  pageObserver = new IntersectionObserver(entries => {
    entries.forEach(entry => {
      const pageNo = Number(entry.target.dataset.pageNo)
      if (entry.isIntersecting) {
        visiblePages.add(pageNo)
      } else {
        visiblePages.delete(pageNo)
      }
    })
    if (visiblePages.size) {
      currentPage.value = Math.min(...visiblePages)
      jumpPage.value = currentPage.value
    }
  }, {
    root: scrollEl.value,
    rootMargin: '-6% 0px -86% 0px',
    threshold: 0
  })
  pageEls.forEach(el => pageObserver.observe(el))
}

function scrollToPage(pageNo, behavior = 'smooth') {
  const target = pageEls.get(pageNo)
  if (!target || !scrollEl.value) return
  const top = target.getBoundingClientRect().top - scrollEl.value.getBoundingClientRect().top + scrollEl.value.scrollTop
  if (behavior === 'auto') {
    // 容器 CSS 是 smooth，直接赋 scrollTop 也会沿途动画；预选课次必须瞬时落位。
    const el = scrollEl.value
    const previous = el.style.scrollBehavior
    el.style.scrollBehavior = 'auto'
    el.scrollTop = top - 8
    requestAnimationFrame(() => {
      if (scrollEl.value === el) el.style.scrollBehavior = previous
    })
    return
  }
  scrollEl.value.scrollTo({ top: top - 8, behavior })
}

function goToPage(pageNo, behavior = 'smooth') {
  const target = Number(pageNo)
  if (!target || target < 1 || target > totalPages.value) return
  currentPage.value = target
  jumpPage.value = target
  scrollToPage(target, behavior)
}

function onJump(value) {
  if (value) goToPage(value)
}

function onSelectSection(key) {
  const section = sections.value.find(item => item.sectionKey === key)
  if (!section) return
  goToPage(section.pdfStart)
}

function selectAndGo(section) {
  selectedKey.value = section.sectionKey
  goToPage(section.pdfStart)
}

// ----- 打印本课：新窗口只放本课页面图，避免把侧边栏和工具栏印进去 -----
const printing = ref(false)
const bookPdfLoading = ref(false)
const sectionPdfLoading = ref(false)

async function downloadWholeBook() {
  if (!book.value || bookPdfLoading.value) return
  bookPdfLoading.value = true
  try {
    const blob = await downloadTextbookBookPdf(bookId.value)
    saveAs(blob, `${book.value.title || '电子教材'}.pdf`)
  } catch (error) {
    ElMessage.error(error?.message || '整册 PDF 下载失败，请稍后重试')
  } finally {
    bookPdfLoading.value = false
  }
}

async function saveSectionPdf() {
  const section = currentSection.value
  if (!section || section.kind !== 'LESSON' || sectionPdfLoading.value) return
  sectionPdfLoading.value = true
  try {
    const blob = await downloadTextbookSectionPdf(bookId.value, section.sectionKey)
    saveAs(blob, `${book.value?.title || '电子教材'} ${sectionLabel(section)}.pdf`)
  } catch (error) {
    ElMessage.error(error?.message || '本课 PDF 下载失败，请稍后重试')
  } finally {
    sectionPdfLoading.value = false
  }
}

function escapeHtml(text) {
  return String(text ?? '').replace(/[&<>"']/g, ch => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  }[ch]))
}

async function printSection() {
  const section = currentSection.value
  if (!section) {
    ElMessage.warning('请先选择要打印的课次')
    return
  }
  if (printing.value) return
  const win = window.open('', '_blank')
  if (!win) {
    ElMessage.warning('浏览器拦截了新窗口，请允许本站弹窗后重试')
    return
  }
  printing.value = true
  const retained = []
  const title = `${book.value?.shortTitle || ''} ${sectionLabel(section)}`
  win.document.write(`<!doctype html><html><head><meta charset="utf-8"><title>${escapeHtml(title)}</title>
    <style>
      body { margin: 0; font-family: "Microsoft YaHei", sans-serif; }
      .prep { padding: 40px; color: #666; }
      .head { padding: 12px 16px; font-size: 14px; color: #333; border-bottom: 1px solid #ddd; }
      .head small { color: #888; margin-left: 8px; }
      .sheet { page-break-after: always; text-align: center; padding: 6px 0; }
      .sheet:last-child { page-break-after: auto; }
      .sheet img { width: 100%; max-width: 100%; }
      @page { margin: 8mm; }
    </style></head><body><p class="prep">正在准备 ${section.pdfEnd - section.pdfStart + 1} 页，请稍候…</p></body></html>`)
  try {
    const ranges = []
    for (let pageNo = section.pdfStart; pageNo <= section.pdfEnd; pageNo += 1) ranges.push(pageNo)
    const results = await Promise.all(ranges.map(pageNo =>
      loadTextbookPage(bookId.value, pageNo)
        .then(url => {
          // 打印窗持有这些 URL：准备期间若切走路由，阅读器卸载清理也不能撤销它们
          if (url) {
            retainTextbookUrls([url])
            retained.push(url)
          }
          return { pageNo, url }
        })
        .catch(() => ({ pageNo, url: '' }))
    ))
    // 缺页时必须中止：老师拿到少页的讲义会以为已经打全，比直接报错更糟
    const missing = results.filter(item => !item.url).map(item => item.pageNo)
    if (missing.length) {
      win.close()
      ElMessage.error(`第 ${missing.join('、')} 页加载失败，已取消打印，请稍后重试`)
      return
    }
    if (win.closed) return
    const body = results
      .map(item => `<div class="sheet"><img src="${item.url}" alt="第 ${item.pageNo} 页"></div>`)
      .join('')
    win.document.body.innerHTML = `<div class="head">${escapeHtml(title)}<small>${escapeHtml(sectionRange(section))}</small></div>${body}`
    await Promise.all(Array.from(win.document.images).map(img => {
      if (img.complete) return Promise.resolve()
      return new Promise(resolve => {
        img.onload = resolve
        img.onerror = resolve
      })
    }))
    win.document.close()
    win.focus()
    win.print()
  } catch (error) {
    ElMessage.error('打印准备失败，请重试')
  } finally {
    // print() 返回后打印窗已拿到图像；再释放引用，清理期间暂缓的撤销立即生效
    releaseTextbookUrls(retained)
    printing.value = false
  }
}

function backToShelf() {
  router.push(isStudentView.value ? '/student/index' : '/textbook')
}

watch(() => route.params.bookId, () => {
  selectedKey.value = ''
  currentPage.value = 1
  jumpPage.value = 1
  load()
})

// 同一册内老师改绑其他课次时（query 变化）也能重新定位。
watch(() => route.query.section, () => {
  applyPresetSection()
})

onMounted(load)
onMounted(() => window.addEventListener('resize', refreshViewport))

onBeforeUnmount(() => {
  window.clearTimeout(presetTimer)
  pageObserver?.disconnect()
  pageObserver = null
  zoomObserver?.disconnect()
  zoomObserver = null
  window.removeEventListener('resize', refreshViewport)
  pageEls.clear()
  clearTextbookPageCache()
  document.title = '信息教学平台'
})
</script>

<style scoped>
.reader {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 84px);
  background: #eef1f5;
  border-radius: 6px;
  overflow: hidden;
}

/* 学生布局没有教师端 navbar + tags-view，阅读器直接占满视口。 */
.reader.is-student-reader {
  height: 100vh;
  border-radius: 0;
}

.reader-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  row-gap: 6px;
  padding: 8px 14px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  flex: 0 0 auto;
}

.back-btn {
  padding: 0 4px;
}

.book-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 320px;
}

.bar-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
  row-gap: 6px;
}

.zoom-group {
  display: flex;
  align-items: center;
  gap: 2px;
}

.zoom-btn {
  padding: 0 4px;
  font-size: 16px;
  line-height: 1;
}

.zoom-select {
  width: 100px;
}

.section-select {
  width: 230px;
}

.page-jump {
  width: 70px;
}

.page-total {
  font-size: 12px;
  color: #909399;
}

.reader-loading {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #909399;
}

.reader-loading.is-error {
  color: #f56c6c;
}

.page-fail-banner {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 14px;
  background: #fef0f0;
  border-bottom: 1px solid #fde2e2;
  color: #f56c6c;
  font-size: 13px;
}

.reader-body {
  position: relative;
  flex: 1;
  display: flex;
  min-height: 0;
}

/* 窄屏目录改为浮层，展开时不再把正文挤成一条 */
@media (max-width: 900px) {
  .toc {
    position: absolute;
    top: 0;
    bottom: 0;
    left: 0;
    z-index: 10;
    box-shadow: 8px 0 20px rgba(0, 0, 0, 0.14);
  }
}

.toc {
  width: 250px;
  flex: 0 0 250px;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  overflow-y: auto;
  padding: 10px 0 24px;
  font-size: 13px;
}

.toc-unit {
  margin-bottom: 8px;
}

.toc-unit-title {
  padding: 8px 14px 6px;
  font-weight: 600;
  color: #303133;
  font-size: 13px;
  line-height: 1.4;
}

.toc-item {
  padding: 6px 14px;
  color: #606266;
  cursor: pointer;
  line-height: 1.45;
  display: flex;
  gap: 6px;
}

.toc-item:hover {
  background: #f5f7fa;
  color: #409eff;
}

.toc-item.is-active {
  background: #ecf5ff;
  color: #409eff;
  font-weight: 600;
}

.toc-item.is-intro,
.toc-item.is-summary,
.toc-item.is-muted {
  color: #909399;
  padding-left: 24px;
}

.toc-item.is-muted {
  padding-left: 14px;
}

.toc-no {
  flex: 0 0 auto;
  color: #a8abb2;
}

.toc-item.is-active .toc-no {
  color: #409eff;
}

.toc-text {
  flex: 1;
}

.pages {
  flex: 1;
  overflow: auto;
  padding: 18px 0 40px;
  scroll-behavior: smooth;
}

.pages-inner {
  width: min(900px, 92%);
  margin: 0 auto;
}

.pick-hint {
  width: min(900px, 92%);
  margin: 0 auto 14px;
  padding: 10px 14px;
  background: #fdf6ec;
  color: #b88230;
  border: 1px solid #faecd8;
  border-radius: 4px;
  font-size: 13px;
}

.page-wrap {
  margin-bottom: 18px;
  transition: opacity 0.2s;
}

.page-wrap.is-outside {
  opacity: 0.55;
}

.page-label {
  margin-top: 6px;
  text-align: center;
  font-size: 12px;
  color: #909399;
}

.reader-foot {
  flex: 0 0 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  background: #fff;
  border-top: 1px solid #e4e7ed;
  font-size: 12px;
  color: #606266;
}

.foot-info {
  min-width: 260px;
  text-align: center;
}
</style>
