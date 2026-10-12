import { test } from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import vm from 'node:vm'
import { ref, computed } from 'vue'
import { buildPracticalHtmlPreview } from '../practicalHtmlPreview.js'

const read = path => fs.readFileSync(new URL('../../' + path, import.meta.url), 'utf8')
const script = path => read(path).match(/<script setup>([\s\S]*?)<\/script>/)[1].replace(/^import[\s\S]*?from ['"][^'"]+['"];?\s*$/gm, '')
const deferred = () => { let resolve, reject; const promise = new Promise((a, b) => { resolve = a; reject = b }); return { promise, resolve, reject } }

test('网页预览保留标准模式声明且策略先于用户脚本', () => {
  const html = buildPracticalHtmlPreview('<!DOCTYPE html><html><head><script>test()</script></head></html>')
  assert.ok(html.startsWith('<!DOCTYPE html><meta'))
  assert.ok(html.indexOf('Content-Security-Policy') < html.indexOf('<script>'))
})

test('切班慢响应只保留一个轮询，失败后仍可重试，卸载全部清理', async () => {
  const props = { lessonId: 1, entryYear: '2026', classCode: '1' }, requests = [], timers = new Map()
  let watchScope, unmount, timerId = 0
  const ctx = { ref, computed, defineProps: () => props, defineEmits: () => () => {},
    watch: (_get, fn) => { watchScope = fn }, onBeforeUnmount: fn => { unmount = fn },
    setInterval: fn => { timers.set(++timerId, fn); return timerId }, clearInterval: id => timers.delete(id),
    request: () => { const d = deferred(); requests.push(d); return d.promise } }
  vm.createContext(ctx)
  vm.runInContext(script('components/SeatHelper/ClassroomSeatPanel.vue') + '\nglobalThis.api={data,loadError,load};', ctx)
  watchScope(); props.classCode = '2'; watchScope()
  requests[1].resolve({ data: { enabled: true, roomName: '新班机房' } }); await new Promise(setImmediate)
  requests[0].resolve({ data: { enabled: true, roomName: '旧班机房' } }); await new Promise(setImmediate)
  assert.equal(timers.size, 1); assert.equal(ctx.api.data.value.roomName, '新班机房')
  const pending = ctx.api.load(); requests[2].reject(new Error('断网')); await pending
  assert.equal(ctx.api.loadError.value, true); assert.equal(timers.size, 1)
  unmount(); assert.equal(timers.size, 0)
})

test('切换机房和请求失败期间旧名单不能用于确认', async () => {
  const props = { lessonId: 1, entryYear: '2026', classCode: '1' }, requests = []
  const ctx = { ref, computed, defineProps: () => props, defineEmits: () => () => {},
    watch() {}, onBeforeUnmount() {}, request: () => { const d = deferred(); requests.push(d); return d.promise } }
  vm.createContext(ctx)
  vm.runInContext(script('components/SeatHelper/ClassroomSeatPanel.vue') + '\nglobalThis.api={data,room,ready,identified,loadError,changeRoom};', ctx)
  const h = ctx.api
  h.room.value = '甲机房'
  h.data.value = { enabled: true, roomName: '甲机房', rows: [{ studentId: 1, state: 'UNBOUND' }] }
  assert.equal(h.identified.value.length, 1)
  h.room.value = '乙机房'
  h.changeRoom()
  assert.equal(h.ready.value, false)
  assert.equal(h.identified.value.length, 0)
  requests[0].reject(new Error('断网'))
  await new Promise(setImmediate)
  assert.equal(h.loadError.value, true)
  assert.equal(h.identified.value.length, 0)
})

test('保存布局不会抹去任务和成绩状态', async () => {
  const source = script('views/business/classroomDesktop/index.vue')
  const fn = source.slice(source.indexOf('function saveLayout()'), source.indexOf('function studentLabel('))
  const students = ref([{ studentId: 1, taskState: 'GRADED', performance: { isAbsent: true }, theory: { score: 90 } }])
  const ctx = { students, columnsCount: ref(6), layout: ref({}), layoutMode: ref(true), entryYear: '2026', classCode: '1',
    saveClassroomLayout: async () => ({ data: { students: [{ studentId: 1, gridRow: 2, gridCol: 3 }] } }),
    ElMessage: { success() {}, error() {} } }
  vm.createContext(ctx); vm.runInContext(fn + '\nsaveLayout()', ctx); await new Promise(setImmediate)
  assert.equal(students.value[0].taskState, 'GRADED'); assert.equal(students.value[0].theory.score, 90)
  assert.equal(students.value[0].performance.isAbsent, true); assert.equal(students.value[0].gridCol, 3)
})

test('图片失败集合变化立即重新计算兜底图', () => {
  const source = script('views/login.vue')
  const ctx = { ref, computed, focusSlides: ref([{ image: '/business/broken.jpg' }]), researchPreviews: ref([]),
    failedSlideImages: ref(new Set()), defaultResearchSlides: [{ image: '/fallback.jpg' }], apiBase: '' }
  vm.createContext(ctx)
  vm.runInContext(source.slice(source.indexOf('function formatSlideImage('), source.indexOf('// 方案一：')) + '\nglobalThis.api={researchSlides,handleSlideImageError};', ctx)
  const first = ctx.api.researchSlides.value[0]
  ctx.api.handleSlideImageError(first, 0)
  assert.equal(ctx.api.researchSlides.value[0].image, '/fallback.jpg')
})
