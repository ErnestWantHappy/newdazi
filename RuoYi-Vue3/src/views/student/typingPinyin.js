// 学生打字原文的拼音提示只做阅读辅助，不进入答案、字数统计或评分数据。
import { ref } from 'vue'

let pinyinProPromise = null
let pinyinRetryUrl = ''
let pinyinRetryCount = 0
const EMPTY_SEGMENTS = Object.freeze([])

function extractPinyinConverter(module) {
  const convert = module.pinyin || module.default?.pinyin
  if (typeof convert !== 'function') throw new Error('拼音组件不可用')
  return convert
}

function resourceNames() {
  if (typeof performance === 'undefined' || typeof performance.getEntriesByType !== 'function') return null
  return new Set(performance.getEntriesByType('resource').map((entry) => entry.name))
}

function failedScriptUrl(before) {
  if (!before) return ''
  const failed = performance.getEntriesByType('resource')
    .filter((entry) => !before.has(entry.name) && entry.initiatorType === 'script' && !entry.transferSize)
  const candidate = failed[failed.length - 1]
  return candidate && /^https?:/.test(candidate.name) ? candidate.name : ''
}

function importPinyinPro() {
  if (pinyinRetryUrl) {
    // 浏览器会缓存失败的动态导入，重试时必须换一个 URL 才能重新请求分包。
    pinyinRetryCount += 1
    const retryUrl = new URL(pinyinRetryUrl)
    retryUrl.searchParams.set('pinyin-retry', String(pinyinRetryCount))
    return import(/* @vite-ignore */ retryUrl.href).then(extractPinyinConverter)
  }
  const before = resourceNames()
  return import('pinyin-pro').then(extractPinyinConverter, (error) => {
    pinyinRetryUrl = failedScriptUrl(before)
    throw error
  })
}

function loadPinyinPro() {
  // 拼音库体积较大，首次展开时才加载，避免增加学生页初始加载和打字延迟。
  if (!pinyinProPromise) {
    pinyinProPromise = importPinyinPro().catch((error) => {
      pinyinProPromise = null
      throw error
    })
  }
  return pinyinProPromise
}

export function createTypingPinyinSegments(text, rows) {
  const source = String(text ?? '')
  if (!source) return []
  if (!Array.isArray(rows)) throw new Error('拼音转换结果无效')

  const segments = []
  let offset = 0
  for (const row of rows) {
    const origin = String(row?.origin ?? '')
    if (!origin) continue
    const pinyinText = row?.isZh ? String(row.pinyin || '') : ''
    segments.push({
      origin,
      start: offset,
      hasPinyin: Boolean(pinyinText),
      pinyin: pinyinText
    })
    offset += origin.length
  }

  // 拼音库若改变了原文顺序或字符，宁可报错回退原文，也不能显示错位注音。
  if (offset !== source.length) throw new Error('拼音转换结果与原文不一致')
  return segments
}

export async function buildTypingPinyinSegments(text) {
  const source = String(text ?? '')
  if (!source) return []
  const convert = await loadPinyinPro()
  const rows = convert(source, {
    toneType: 'none',
    v: true,
    type: 'all',
    // 连续的非汉字保持原样，避免标点、空格和换行被拼音库改写。
    nonZh: 'consecutive'
  })
  return createTypingPinyinSegments(source, rows)
}

function createEntry() {
  return {
    visible: false,
    status: 'idle',
    source: '',
    requestSource: '',
    requestId: 0,
    segments: EMPTY_SEGMENTS,
    error: ''
  }
}

export function createTypingPinyinController(convert = buildTypingPinyinSegments) {
  const states = ref({})

  function entryOf(questionId) {
    if (!states.value[questionId]) states.value[questionId] = createEntry()
    // 必须返回 ref 内的响应式代理，直接修改原始对象不会触发视图更新。
    return states.value[questionId]
  }

  function isVisible(questionId) {
    return Boolean(states.value[questionId]?.visible)
  }

  function statusOf(questionId) {
    return states.value[questionId]?.status || 'idle'
  }

  function segmentsFor(question) {
    const state = states.value[question?.questionId]
    if (!state?.visible || state.status !== 'ready') return EMPTY_SEGMENTS
    // 题目正文变化时先回退原文，等新注音生成后再显示，不能复用旧内容。
    if (state.source !== String(question?.questionContent || '')) return EMPTY_SEGMENTS
    return state.segments
  }

  async function load(question, force = false) {
    const questionId = question?.questionId
    if (questionId === undefined || questionId === null) return
    const source = String(question.questionContent || '')
    const state = entryOf(questionId)

    if (!force) {
      if (state.status === 'ready' && state.source === source) return state.segments
      if (state.status === 'loading' && state.requestSource === source) return
    }

    const requestId = state.requestId + 1
    state.requestId = requestId
    state.status = 'loading'
    state.requestSource = source
    state.error = ''
    try {
      const segments = await convert(source)
      const current = states.value[questionId]
      if (current !== state || state.requestId !== requestId) return
      state.source = source
      state.segments = segments
      state.status = 'ready'
      return segments
    } catch (error) {
      const current = states.value[questionId]
      if (current !== state || state.requestId !== requestId) return
      state.source = ''
      state.segments = EMPTY_SEGMENTS
      state.status = 'error'
      state.error = '拼音暂时无法显示'
    }
  }

  function toggle(question) {
    if (question?.questionId === undefined || question?.questionId === null) return
    const state = entryOf(question.questionId)
    state.visible = !state.visible
    if (!state.visible) return
    return load(question)
  }

  function retry(question) {
    if (question?.questionId === undefined || question?.questionId === null) return
    const state = entryOf(question.questionId)
    state.visible = true
    return load(question, true)
  }

  function sync(question) {
    const state = states.value[question?.questionId]
    if (!state?.visible) return
    return load(question)
  }

  function reset() {
    states.value = {}
  }

  return { states, isVisible, statusOf, segmentsFor, toggle, retry, sync, reset }
}
