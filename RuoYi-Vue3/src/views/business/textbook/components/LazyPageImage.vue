<template>
  <div ref="rootEl" class="textbook-page" :style="placeholderStyle">
    <div v-if="status === 'loading'" class="page-hint">加载中…</div>
    <div v-else-if="status === 'error'" class="page-hint is-error" @click="reload">本页加载失败，点击重试</div>
    <img v-else-if="src" :src="src" :alt="alt" class="page-image" @error="status = 'error'" />
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { loadTextbookCover, loadTextbookPage } from '@/api/business/textbook'

const props = defineProps({
  bookId: { type: [Number, String], required: true },
  pageNo: { type: Number, required: true },
  width: { type: Number, default: 1400 },
  height: { type: Number, default: 1980 },
  alt: { type: String, default: '' },
  // 书架封面走后端 360px 缩略图，避免一次下载十几张整页大图
  cover: { type: Boolean, default: false },
  // 打印时必须立即取图，滚动阅读则进入视口附近再取
  eager: { type: Boolean, default: false },
  // 阅读器点「重试失败页」时递增，失败态页面据此重新请求
  retryToken: { type: Number, default: 0 }
})

const emit = defineEmits(['page-failed', 'page-loaded'])
const rootEl = ref(null)
const src = ref('')
const status = ref('idle')
let observer = null
let alive = true
let seq = 0

const placeholderStyle = computed(() => ({
  aspectRatio: `${props.width || 1400} / ${props.height || 1980}`
}))

async function load(force = false) {
  const current = ++seq
  status.value = 'loading'
  try {
    const url = props.cover
      ? await loadTextbookCover(props.bookId)
      : await loadTextbookPage(props.bookId, props.pageNo, force)
    if (!alive || current !== seq) return
    src.value = url
    status.value = 'ready'
    emit('page-loaded', props.pageNo)
  } catch (error) {
    if (!alive || current !== seq) return
    status.value = 'error'
    emit('page-failed', props.pageNo)
  }
}

function disconnect() {
  observer?.disconnect()
  observer = null
}

function observe() {
  if (props.eager) {
    load()
    return
  }
  if (!rootEl.value || typeof IntersectionObserver === 'undefined') {
    load()
    return
  }
  observer = new IntersectionObserver(entries => {
    if (entries.some(item => item.isIntersecting)) {
      disconnect()
      load()
    }
  }, { rootMargin: '800px 0px' })
  observer.observe(rootEl.value)
}

function reload() {
  src.value = ''
  status.value = 'idle'
  // 强制绕过缓存：失败页已不在缓存里，但"blob 拿到了却解码失败"的情况仍会命中旧 URL
  load(true)
}

watch(() => [props.bookId, props.pageNo, props.cover], () => {
  src.value = ''
  status.value = 'idle'
  load()
})

watch(() => props.retryToken, () => {
  if (status.value === 'error') reload()
})

onMounted(observe)
onBeforeUnmount(() => {
  alive = false
  disconnect()
})
</script>

<style scoped>
.textbook-page {
  position: relative;
  width: 100%;
  background: #fff;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.12);
}

.page-image {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.page-hint {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 13px;
  background: repeating-linear-gradient(45deg, #fafafa, #fafafa 12px, #f4f6f8 12px, #f4f6f8 24px);
}

.page-hint.is-error {
  color: #f56c6c;
  cursor: pointer;
}
</style>
