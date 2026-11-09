<template>
  <div class="case-preview" tabindex="0" @keydown.left="onArrow($event, -1)" @keydown.right="onArrow($event, 1)">
    <div class="preview-toolbar">
      <el-button size="small" @click="thumbnailsVisible = !thumbnailsVisible">{{ thumbnailsVisible ? '收起缩略图' : '缩略图' }}</el-button>
      <el-button size="small" :disabled="!total || pageNo <= 1" @click="goToPage(pageNo - 1)">上一页</el-button>
      <el-input-number v-model="jumpPage" aria-label="预览页码" size="small" :min="1" :max="total || 1" :controls="false" @change="jump" />
      <span>/ {{ total || '…' }} 页</span>
      <el-button size="small" :disabled="!total || pageNo >= total" @click="goToPage(pageNo + 1)">下一页</el-button>
      <div class="zoom-controls">
        <el-button size="small" aria-label="缩小预览" :disabled="!total || percent <= 25" @click="stepZoom(-25)">−</el-button>
        <el-select v-model="zoom" size="small" aria-label="预览缩放">
          <el-option label="适应整页" value="page" /><el-option label="适应宽度" value="width" />
          <el-option v-for="level in levels" :key="level" :label="`${level}%`" :value="level" />
        </el-select>
        <el-button size="small" aria-label="放大预览" :disabled="!total || percent >= 200" @click="stepZoom(25)">＋</el-button>
      </div>
    </div>
    <div class="preview-workspace">
      <nav v-if="thumbnailsVisible" ref="thumbnailRail" class="thumbnail-rail" aria-label="页面缩略图">
        <button v-for="page in total" :key="page" :data-page="page" :class="{ selected: page === pageNo }" :aria-label="`跳到第${page}页`" :aria-current="page === pageNo ? 'page' : undefined" @click="goToPage(page)">
          <img v-if="thumbnails[page]" :src="thumbnails[page]" alt="" /><span v-else class="thumbnail-placeholder">{{ page }}</span>
          <span>第 {{ page }} 页</span>
        </button>
      </nav>
      <div ref="canvas" class="preview-canvas" tabindex="0" aria-label="课例连续阅读区" v-loading="loading" @scroll.passive="onScroll">
        <div v-if="error" role="alert">{{ error }} <el-button @click="load">重试预览</el-button></div>
        <div v-else class="preview-pages" :style="{ height: `${documentHeight}px`, width: `${documentWidth}px` }">
          <section v-for="item in windowPages" :key="item.page" :data-preview-page="item.page" class="preview-page" :style="{ top: `${item.top}px`, height: `${item.blockHeight}px` }" :aria-label="`第${item.page}页`">
            <div class="preview-page-sheet" :style="{ width: `${item.width}px`, height: `${item.height}px` }">
              <img v-if="images[item.page]?.url" :src="images[item.page].url" :alt="`${title}，第${item.page}页`" class="case-preview-image" />
              <div v-else-if="images[item.page]?.error" class="page-state" role="alert">第{{ item.page }}页加载失败 <el-button size="small" @click="retryPage(item.page)">重试本页</el-button></div>
              <div v-else class="page-state" role="status">正在加载第{{ item.page }}页…</div>
            </div>
            <div class="page-label">第 {{ item.page }} / {{ total }} 页</div>
          </section>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onBeforeUnmount, onMounted } from 'vue'
import { getCasePreviewInfo, getCasePreviewPage } from '@/api/business/textbookCase'
import { getGuidePreviewInfo, getGuidePreviewPage, getGuideLessonPreviewInfo, getGuideLessonPreviewPage, getBookResourcePreviewInfo, getBookResourcePreviewPage } from '@/api/business/textbookResource'
import { buildPreviewLayout, currentScrollPage, visiblePreviewPages } from '@/utils/casePreviewScroll'

const props = defineProps({
  resourceId: { type: Number, required: true },
  title: String,
  // case=课例资源，guide=整本指南 PDF，guideLesson=指南课件，book=册级教学设计汇编
  source: { type: String, default: 'case' },
  // 同一汇编关闭再打开时由父层记住最后阅读页；不写入浏览器或服务器。
  initialPage: { type: Number, default: 1 }
})
const previewApi = computed(() => ({
  case: { info: getCasePreviewInfo, page: getCasePreviewPage },
  guide: { info: getGuidePreviewInfo, page: getGuidePreviewPage },
  guideLesson: { info: getGuideLessonPreviewInfo, page: getGuideLessonPreviewPage },
  book: { info: getBookResourcePreviewInfo, page: getBookResourcePreviewPage }
}[props.source] || { info: getCasePreviewInfo, page: getCasePreviewPage }))
const emit = defineEmits(['ready', 'pending', 'failed', 'page-change'])
const initialPage = Math.max(1, Math.trunc(Number(props.initialPage) || 1))
const pageNo = ref(initialPage), jumpPage = ref(initialPage), total = ref(0), loading = ref(false), error = ref('')
const zoom = ref('page'), canvas = ref(null), scrollTop = ref(0), thumbnailRail = ref(null)
const thumbnailsVisible = ref(false), thumbnails = ref({}), images = ref({}), sizes = ref({})
const fallback = ref({ width: 1200, height: 1600 }), baseKnown = ref(false)
const viewport = ref({ width: 600, height: 500 }), levels = [25, 50, 75, 100, 125, 150, 175, 200]
const layout = computed(() => buildPreviewLayout(total.value, sizes.value, fallback.value, viewport.value, zoom.value))
const windowPages = computed(() => visiblePreviewPages(layout.value, scrollTop.value, viewport.value.height))
const documentHeight = computed(() => layout.value.at(-1)?.top + (layout.value.at(-1)?.blockHeight || 0) || 0)
const documentWidth = computed(() => Math.max(viewport.value.width, ...layout.value.map(item => item.width)))
const percent = computed(() => (layout.value[pageNo.value - 1]?.width || 0) / (sizes.value[pageNo.value]?.width || fallback.value.width) * 100)
let disposed = false, pumping = false, resizeObserver, thumbnailObserver, scrollFrame = 0, infoSequence = 0
let readingState = ''
let layoutAnchor = null
const visibleThumbnails = new Set()

function setReadingState() {
  const image = images.value[pageNo.value]
  const state = image?.url ? 'ready' : image?.error || error.value ? 'failed' : 'pending'
  if (state !== readingState) { readingState = state; emit(state) }
}
function stepZoom(step) { zoom.value = Math.max(25, Math.min(200, Math.round(percent.value / 25) * 25 + step)) }
function onArrow(event, step) {
  if (event.target.closest('input, textarea, [role=combobox]')) return
  event.preventDefault(); goToPage(pageNo.value + step)
}
function jump(value) { if (value && value !== pageNo.value) goToPage(value) }
function goToPage(value) {
  const page = Math.max(1, Math.min(total.value, Math.trunc(value)))
  const entry = layout.value[page - 1]
  if (!entry || !canvas.value) return
  canvas.value.scrollTop = entry.top
  readScroll()
}
function onScroll() {
  if (!scrollFrame) scrollFrame = requestAnimationFrame(() => { scrollFrame = 0; readScroll() })
}
function readScroll() {
  if (!canvas.value || disposed || layoutAnchor) return
  scrollTop.value = canvas.value.scrollTop
  if (baseKnown.value) pageNo.value = currentScrollPage(layout.value, scrollTop.value)
  jumpPage.value = pageNo.value
  setReadingState(); pump()
}
function releaseImage(page) {
  if (images.value[page]?.url) URL.revokeObjectURL(images.value[page].url)
  delete images.value[page]
}
function retryPage(page) { releaseImage(page); setReadingState(); pump() }

async function loadImage(page) {
  images.value[page] = { loading: true }
  let url = ''
  try {
    const blob = await previewApi.value.page(props.resourceId, page)
    if (disposed) return
    url = URL.createObjectURL(blob)
    const size = await new Promise((resolve, reject) => {
      const image = new Image()
      image.onload = () => resolve({ width: image.naturalWidth, height: image.naturalHeight })
      image.onerror = reject
      image.src = url
    })
    if (disposed) { URL.revokeObjectURL(url); return }
    if (page === 1 && !baseKnown.value) { fallback.value = size; baseKnown.value = true }
    sizes.value[page] = size
    // 跳页期间完成的旧请求不再持有图片，只保留尺寸防止混合横竖页跳动。
    if (!windowPages.value.some(item => item.page === page)) { URL.revokeObjectURL(url); delete images.value[page]; return }
    images.value[page] = { url }
  } catch {
    if (url) URL.revokeObjectURL(url)
    if (!disposed) images.value[page] = { error: true }
  } finally { if (!disposed) setReadingState() }
}

// 正文与缩略图共用串行调度，正文优先，避免本组件挤满服务端渲染名额。
async function pump() {
  if (pumping || disposed || loading.value || error.value || layoutAnchor) return
  pumping = true
  try {
    while (!disposed) {
      if (layoutAnchor) break
      const needed = baseKnown.value ? windowPages.value.map(item => item.page) : total.value ? [1] : []
      for (const key of Object.keys(images.value)) {
        if (!needed.includes(Number(key)) && !images.value[key]?.loading) releaseImage(Number(key))
      }
      const page = needed.filter(value => !images.value[value]).sort((a, b) => Math.abs(a - pageNo.value) - Math.abs(b - pageNo.value))[0]
      if (page) { await loadImage(page); continue }
      const thumbnail = thumbnailsVisible.value && [...visibleThumbnails].find(value => !thumbnails.value[value])
      if (!thumbnail) break
      try {
        const blob = await previewApi.value.page(props.resourceId, thumbnail, true)
        if (disposed) break
        thumbnails.value[thumbnail] = URL.createObjectURL(blob)
        // 已滚离的缩略图不长期占内存，回看时可重新按需加载。
        for (const key of Object.keys(thumbnails.value)) {
          if (Object.keys(thumbnails.value).length <= 24) break
          if (!visibleThumbnails.has(Number(key))) { URL.revokeObjectURL(thumbnails.value[key]); delete thumbnails.value[key] }
        }
      } catch { visibleThumbnails.delete(thumbnail) }
    }
  } finally { pumping = false }
}

async function load() {
  const ticket = ++infoSequence
  loading.value = true; error.value = ''; setReadingState()
  try {
    const res = await previewApi.value.info(props.resourceId)
    if (disposed || ticket !== infoSequence) return
    total.value = Number(res.data.pages)
    if (!Number.isInteger(total.value) || total.value < 1) throw new Error('文件没有可预览页面')
    await nextTick()
    if (disposed || ticket !== infoSequence) return
    const targetPage = Math.max(1, Math.min(total.value, Math.trunc(Number(props.initialPage) || 1)))
    if (targetPage > 1) goToPage(targetPage)
    else { pageNo.value = 1; jumpPage.value = 1 }
  } catch (failure) {
    if (!disposed && ticket === infoSequence) error.value = failure?.message || '读取预览失败，可重试或下载原件'
  } finally {
    if (!disposed && ticket === infoSequence) { loading.value = false; setReadingState(); pump() }
  }
}
async function observeThumbnails() {
  thumbnailObserver?.disconnect(); visibleThumbnails.clear()
  await nextTick()
  if (!thumbnailRail.value || disposed) return
  thumbnailObserver = new IntersectionObserver(entries => {
    for (const entry of entries) {
      const page = Number(entry.target.dataset.page)
      if (entry.isIntersecting) visibleThumbnails.add(page)
      else visibleThumbnails.delete(page)
    }
    pump()
  }, { root: thumbnailRail.value, rootMargin: '80px' })
  thumbnailRail.value.querySelectorAll('[data-page]').forEach(element => thumbnailObserver.observe(element))
}
watch([thumbnailsVisible, total], observeThumbnails)
watch(windowPages, () => { setReadingState(); pump() })
watch(layout, (next, previous) => {
  if (!canvas.value || !previous.length) return
  if (!layoutAnchor) {
    const page = currentScrollPage(previous, canvas.value.scrollTop)
    const old = previous[page - 1]
    if (!old) return
    layoutAnchor = { page, offset: (canvas.value.scrollTop - old.top) / old.blockHeight }
  }
  nextTick(() => {
    if (disposed || !canvas.value || !layoutAnchor) return
    const current = layout.value[layoutAnchor.page - 1]
    if (current) canvas.value.scrollTop = current.top + layoutAnchor.offset * current.blockHeight
    layoutAnchor = null
    readScroll()
  })
}, { flush: 'sync' })
watch(pageNo, async page => {
  emit('page-change', page)
  await nextTick()
  const rail = thumbnailRail.value, selected = rail?.querySelector(`[data-page="${page}"]`)
  if (!rail || !selected) return
  const top = selected.offsetTop - rail.offsetTop
  if (top < rail.scrollTop) rail.scrollTop = top
  else if (top + selected.offsetHeight > rail.scrollTop + rail.clientHeight) rail.scrollTop = top + selected.offsetHeight - rail.clientHeight
})
onMounted(() => {
  resizeObserver = new ResizeObserver(() => {
    if (canvas.value) viewport.value = { width: Math.max(1, canvas.value.clientWidth - 24), height: Math.max(1, canvas.value.clientHeight - 24) }
  })
  resizeObserver.observe(canvas.value); load()
})
onBeforeUnmount(() => {
  disposed = true; ++infoSequence; cancelAnimationFrame(scrollFrame)
  resizeObserver?.disconnect(); thumbnailObserver?.disconnect()
  Object.keys(images.value).forEach(page => releaseImage(Number(page)))
  Object.values(thumbnails.value).forEach(url => URL.revokeObjectURL(url))
})
</script>

<style scoped>
.case-preview { display: flex; flex-direction: column; flex: 1; min-width: 0; min-height: 0; outline: none; }
.preview-toolbar { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; padding: 8px 0; flex: none; }
.preview-toolbar .el-button { margin: 0; }
.preview-toolbar .el-input-number { width: 58px; }
.preview-toolbar span { font-size: 12px; }
.zoom-controls { display: flex; gap: 4px; margin-left: auto; }
.zoom-controls .el-select { width: 112px; }
.preview-workspace { flex: 1; min-height: 0; min-width: 0; display: flex; gap: 10px; }
.preview-canvas { flex: 1; min-width: 0; min-height: 0; overflow: auto; background: #eef1f5; padding: 12px; overscroll-behavior: contain; overflow-anchor: none; }
.preview-pages { position: relative; min-width: 100%; }
.preview-page { position: absolute; left: 0; right: 0; display: flex; flex-direction: column; align-items: center; }
.preview-page-sheet { flex: none; background: white; box-shadow: 0 1px 5px #0001; }
.case-preview-image { display: block; width: 100%; height: 100%; object-fit: contain; }
.page-state { height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px; color: #606266; font-size: 13px; }
.page-label { flex: none; height: 36px; padding-top: 5px; box-sizing: border-box; font-size: 12px; color: #606266; }
.thumbnail-rail { position: relative; flex: 0 0 112px; min-height: 0; overflow-y: auto; overscroll-behavior: contain; }
.thumbnail-rail button { display: flex; flex-direction: column; align-items: center; gap: 4px; width: 100%; padding: 6px; margin-bottom: 6px; border: 2px solid transparent; border-radius: 4px; background: #f3f5f7; color: #606266; cursor: pointer; font-size: 12px; }
.thumbnail-rail button.selected { border-color: #409eff; color: #1676c9; }
.thumbnail-rail img { max-width: 94px; max-height: 100px; }
.thumbnail-placeholder { display: grid; place-items: center; height: 70px; }
@media (max-width: 600px) { .zoom-controls { margin-left: 0; } .preview-toolbar { gap: 5px; } .thumbnail-rail { flex-basis: 80px; } .thumbnail-rail img { max-width: 64px; } }
</style>
