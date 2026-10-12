import { test } from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import vm from 'node:vm'
import { formatPracticalFileSize } from '../practicalFileDisplay.js'
import { studentDraftKey, loadLocalFlowchartDraft, saveLocalFlowchartDraft, markLocalFlowchartDraftSynced } from '../../components/FlowchartEditor/localDraft.js'

const ref = value => ({ value })
const read = path => fs.readFileSync(new URL('../../' + path, import.meta.url), 'utf8')
const script = source => source.match(/<script setup>([\s\S]*?)<\/script>/)[1].replace(/^import[\s\S]*?from ['"][^'"]+['"];?\s*$/gm, '')
const deferred = () => { let resolve, reject; const promise = new Promise((a, b) => { resolve = a; reject = b }); return { promise, resolve, reject } }
const message = Object.fromEntries(['success', 'info', 'error', 'warning'].map(key => [key, () => {}]))
const storage = () => { const values = new Map(); return { getItem: key => values.get(key), setItem: (key, value) => values.set(key, value), removeItem: key => values.delete(key) } }

test('文件大小按字节显示，缺失大小不伪装为零', () => {
  assert.equal(formatPracticalFileSize(0), '0 B')
  assert.equal(formatPracticalFileSize(2048), '2.0 KiB')
  assert.equal(formatPracticalFileSize(1048576), '1.0 MiB')
  assert.equal(formatPracticalFileSize(null), '大小未知')
})
test('反馈课程列表失败后直接刷新恢复，关闭后旧响应不回填', async () => {
  const props = { modelValue: true }, requests = []
  const ctx = { ref, watch() {}, onBeforeUnmount() {}, defineProps: () => props, defineEmits() {},
    request: data => { const d = deferred(); requests.push({ ...d, data }); return d.promise } }
  vm.createContext(ctx)
  vm.runInContext(script(read('components/StudentFeedbackDialog/index.vue')) + '\nglobalThis.api={refresh,rows,error,loading};', ctx)
  const h = ctx.api
  let p = h.refresh(); requests[0].reject(new Error('断线')); await p
  assert.match(h.error.value, /刷新反馈/); assert.equal(h.loading.value, false)
  p = h.refresh(); requests[1].resolve({ data: [{ lessonId: 9 }] }); await new Promise(setImmediate)
  assert.equal(requests[2].data.url, '/business/student/feedback')
  requests[2].resolve({ data: [{ questionId: 10, submitted: false }] }); await p
  assert.equal(h.rows.value[0].submitted, false); assert.equal(h.error.value, '')
  assert.equal(h.loading.value, false)
  p = h.refresh(); props.modelValue = false; requests[3].resolve({ data: [{ lessonId: 8 }] }); await p
  assert.equal(h.rows.value.length, 0); assert.equal(requests.length, 4)
})
test('设备撤销确认和请求期间阻止重复点击', async () => {
  const confirmation = deferred(), requests = []
  const ctx = { ref, watch() {}, onMounted() {}, onBeforeUnmount() {}, window: { location: { origin: 'https://school.test' } },
    ElMessageBox: { confirm: () => confirmation.promise },
    request: async data => { requests.push(data); return { data: {} } } }
  vm.createContext(ctx)
  vm.runInContext(script(read('views/business/seatHelper/index.vue')) + '\nglobalThis.api={revoke,deptId,busy};', ctx)
  ctx.api.deptId.value = 1
  const p = ctx.api.revoke({ deviceId: 2 }); await ctx.api.revoke({ deviceId: 2 })
  assert.equal(ctx.api.busy.value, true); assert.equal(requests.length, 0)
  confirmation.resolve(); await p
  assert.equal(requests.filter(r => r.url.includes('/revoke/')).length, 1)
  assert.equal(ctx.api.busy.value, false)
})

function flow() {
  globalThis.localStorage = storage()
  const user = { id: 1, token: 'session-A' }, props = { modelValue: true, lessonId: 9, question: { questionId: 10 } }
  const requests = [], saves = [], timers = []
  const ctx = { ref, computed: fn => ({ get value() { return fn() } }), watch() {}, onMounted() {}, onBeforeUnmount() {},
    defineProps: () => props, defineEmits: () => () => {}, useUserStore: () => user,
    window: { innerWidth: 1200, innerHeight: 900 }, ElMessage: message, ElMessageBox: { confirm: async () => {} },
    studentDraftKey, loadLocalFlowchartDraft, saveLocalFlowchartDraft, markLocalFlowchartDraftSynced,
    setTimeout: fn => { timers.push(fn); return timers.length }, clearTimeout() {},
    getStudentFlowchartWorkspace: () => { const d = deferred(); requests.push(d); return d.promise },
    saveStudentFlowchartDraft: data => { const d = deferred(); saves.push({ ...d, data }); return d.promise } }
  vm.createContext(ctx)
  vm.runInContext(script(read('components/FlowchartEditor/StudentFlowchartDialog.vue')) + '\nglobalThis.api={loadWorkspace,handleDocumentChange,saveDraftNow,documentJson,revision,documentReady,readOnly,closeDialog,resolveConflict,conflict,saveError};', ctx)
  return { ...ctx.api, user, props, requests, saves, timers }
}
const workspace = (documentJson = 'server', readOnly = false) => ({ data: { draft: { revision: 2, documentJson }, readOnly, latestSubmission: { versionNo: 1, documentJson: 'formal' } } })

test('草稿冲突保留本机版本时先读取最新修订号，再按新修订号保存', async () => {
  const h = flow()
  let pending = h.loadWorkspace(); h.requests[0].resolve(workspace()); await pending
  h.handleDocumentChange('本机作品')
  pending = h.saveDraftNow(); h.saves[0].reject(new Error('其他页面已更新')); await pending
  assert.equal(h.conflict.value, true)
  pending = h.resolveConflict(true); await new Promise(setImmediate)
  const newer = workspace('另一页作品'); newer.data.draft.revision = 8
  h.requests[1].resolve(newer); await pending
  assert.equal(h.documentJson.value, '本机作品'); assert.equal(h.conflict.value, false)
  pending = h.saveDraftNow(); assert.equal(h.saves[1].data.expectedRevision, 8)
  h.saves[1].resolve({ data: { revision: 9 } }); await pending
  assert.equal(h.revision.value, 9)
})

test('冲突恢复时发现已批改，改为正式作品只读并保留本机草稿', async () => {
  const h = flow()
  let pending = h.loadWorkspace(); h.requests[0].resolve(workspace()); await pending
  h.handleDocumentChange('尚未提交的本机作品')
  pending = h.saveDraftNow(); h.saves[0].reject(new Error('作品已批改，请联系老师退回')); await pending
  assert.match(h.saveError.value, /已批改/)
  pending = h.resolveConflict(true); await new Promise(setImmediate)
  h.requests[1].resolve(workspace('草稿', true)); await pending
  assert.equal(h.readOnly.value, true); assert.equal(h.documentJson.value, 'formal')
  assert.equal(loadLocalFlowchartDraft(studentDraftKey(1, 9, 10), 1).documentJson, '尚未提交的本机作品')
})

test('草稿按账号隔离，并拒绝未知归属和旧键', () => {
  globalThis.localStorage = storage()
  const a = studentDraftKey(1, 9, 10), b = studentDraftKey(2, 9, 10)
  saveLocalFlowchartDraft(a, 'A的作品', 2, false, 1)
  assert.equal(loadLocalFlowchartDraft(a, 1).documentJson, 'A的作品')
  assert.equal(loadLocalFlowchartDraft(a, 2), null)
  assert.equal(loadLocalFlowchartDraft(b, 2), null)
  assert.equal(studentDraftKey(null, 9, 10), '')
  localStorage.setItem('huacheng:draft:9:10', JSON.stringify({ documentJson: '旧草稿', revision: 2 }))
  assert.equal(loadLocalFlowchartDraft(b, 2), null)
})
test('同修订号换账号也不恢复上一学生的草稿', async () => {
  const h = flow()
  saveLocalFlowchartDraft(studentDraftKey(1, 9, 10), 'A', 2, false, 1)
  h.user.id = 2; h.user.token = 'B'
  const p = h.loadWorkspace(); h.requests[0].resolve(workspace()); await p
  assert.equal(h.documentJson.value, 'server'); assert.equal(h.timers.length, 0)
})
test('只读显示正式版本且不覆盖本人未同步草稿', async () => {
  const h = flow(), key = studentDraftKey(1, 9, 10)
  saveLocalFlowchartDraft(key, '未同步B', 2, false, 1)
  const p = h.loadWorkspace(); h.requests[0].resolve(workspace('草稿B', true)); await p
  assert.equal(h.documentJson.value, 'formal')
  assert.equal(loadLocalFlowchartDraft(key, 1).documentJson, '未同步B')
  assert.equal(loadLocalFlowchartDraft(key, 1).synced, false)
})
test('旧账号加载和保存响应不能覆盖新账号', async () => {
  const h = flow()
  let p = h.loadWorkspace(); h.requests[0].resolve(workspace()); await p
  h.handleDocumentChange('A更新'); const saving = h.saveDraftNow()
  h.user.id = 2; h.user.token = 'B'
  p = h.loadWorkspace(); h.requests[1].resolve(workspace('B')); await p
  h.saves[0].resolve({ data: { revision: 99 } }); await saving
  assert.equal(h.documentJson.value, 'B'); assert.equal(h.revision.value, 2)
  assert.equal(loadLocalFlowchartDraft(studentDraftKey(1, 9, 10), 1).synced, false)
})

function desktop() {
  const requests = []
  const ctx = { lessonId: '9', entryYear: '2025', classCode: 'A', desktopDisposed: false, desktopPendingKey: null, desktopRequestVersion: 0,
    localStorage: storage(), fetchLessonGateState() {}, ...Object.fromEntries(['loading', 'error', 'historical', 'students', 'layout', 'hasTyping', 'hasTheory', 'hasPractical', 'columnsCount'].map(key => [key, ref(null)])),
    getClassroomDesktopOverview: () => { const d = deferred(); requests.push(d); return d.promise } }
  ctx.desktopKey = () => JSON.stringify([ctx.lessonId, ctx.entryYear, ctx.classCode])
  vm.createContext(ctx)
  const source = read('views/business/classroomDesktop/index.vue')
  vm.runInContext(source.slice(source.indexOf('function loadDesktop('), source.indexOf('function load(silent')), ctx)
  return { ctx, requests }
}
test('旧班慢响应不覆盖新班且不阻止新班发请求', async () => {
  const { ctx, requests } = desktop(); const a = ctx.loadDesktop()
  ctx.classCode = 'B'; const b = ctx.loadDesktop()
  assert.equal(requests.length, 2)
  requests[1].resolve({ data: { students: ['B'] } }); await b
  requests[0].resolve({ data: { students: ['A'] } }); await a
  assert.deepEqual(ctx.students.value, ['B'])
})
test('旧请求失败和finally不清除新请求加载态', async () => {
  const { ctx, requests } = desktop(); const a = ctx.loadDesktop()
  ctx.classCode = 'B'; const b = ctx.loadDesktop()
  requests[0].reject(new Error('A失败')); await a
  assert.equal(ctx.loading.value, true); assert.equal(ctx.error.value, '')
  requests[1].resolve({ data: { students: ['B'] } }); await b
  assert.equal(ctx.loading.value, false)
})

function uploads() {
  const requests = [], ctx = { practicalUploadEpoch: 1, lessonId: ref(9), practicalDrafts: ref({}), practicalFileLists: ref({}), practicalUploadStates: ref({}),
    submittingPracticalQuestionId: ref(null), practicalArtifacts: ref({}), uploadingQuestionId: ref(null), ElMessage: message,
    refreshPracticalSubmission: async () => ({ previewStatus: 'success' }),
    submitPracticalArtifact: data => { const d = deferred(); requests.push({ ...d, data }); return d.promise } }
  vm.createContext(ctx)
  const source = read('views/student/index.vue')
  vm.runInContext(source.slice(source.indexOf('function getPracticalDrafts('), source.indexOf('function removePracticalDraft(')), ctx)
  vm.runInContext(source.slice(source.indexOf('async function commitPracticalArtifact('), source.indexOf('function handleUploadError(')), ctx)
  const batch = uid => { ctx.practicalDrafts.value[10] = [{ uid, uploadToken: `token${uid}` }]; ctx.practicalFileLists.value[10] = [{ uid, status: 'success' }] }
  return { ctx, requests, batch }
}
test('同页连续提交三版，每版都释放组件额度', async () => {
  const { ctx, requests, batch } = uploads()
  for (let i = 0; i < 3; i++) {
    batch(i); const p = ctx.commitPracticalArtifact({ questionId: 10 }); requests[i].resolve({}); await p
    assert.equal(ctx.practicalFileLists.value[10].length, 0)
    assert.equal(ctx.practicalDrafts.value[10].length, 0)
  }
})
test('提交失败保留重试文件，切课后迟到成功不清理新课文件', async () => {
  const { ctx, requests, batch } = uploads(); batch(1)
  let p = ctx.commitPracticalArtifact({ questionId: 10 }); requests[0].reject(new Error('失败'))
  await assert.rejects(p); assert.equal(ctx.practicalDrafts.value[10].length, 1)
  p = ctx.commitPracticalArtifact({ questionId: 10 })
  ctx.practicalUploadEpoch++; ctx.lessonId.value = 11; batch(2)
  requests[1].resolve({}); await p
  assert.equal(ctx.practicalFileLists.value[10][0].uid, 2)
})

test('切学校以及切回原学校均丢弃旧注册码', async () => {
  const requests = [], ctx = { ref, watch() {}, onMounted() {}, onBeforeUnmount() {}, window: { location: { origin: 'https://school.test' } }, ElMessage: message,
    request: data => { const d = deferred(); requests.push({ ...d, data }); return d.promise } }
  vm.createContext(ctx)
  vm.runInContext(script(read('views/business/seatHelper/index.vue')) + '\nglobalThis.api={deptId,roomName,seatName,code,load,enroll};', ctx)
  const h = ctx.api; h.deptId.value = 1; h.roomName.value = '机房'; h.seatName.value = '1'
  let p = h.load(); requests[0].resolve({ data: {} }); await p
  const old = h.enroll()
  h.deptId.value = 2; p = h.load(); requests[2].resolve({ data: {} }); await p
  h.deptId.value = 1; p = h.load(); requests[3].resolve({ data: {} }); await p
  requests[1].resolve({ data: { code: '旧码' } }); await old
  assert.equal(h.code.value, '')
})

test('布局持续续期超过90秒，身份变化和卸载终止旧轮询', async () => {
  const user = { id: 1, token: 'A' }, timers = [], requests = []
  let start, unmount
  const ctx = { ref, window: { addEventListener() {}, removeEventListener() {} }, provide() {}, watch: (_get, callback) => { start = callback }, onBeforeUnmount: fn => { unmount = fn },
    useUserStore: () => user, takeSeatHelperToken: () => null, clearTimeout() {}, setTimeout: fn => { timers.push(fn); return timers.length },
    request: async data => { requests.push(data); return { data: { enabled: true, associated: true } } } }
  vm.createContext(ctx)
  const source = read('components/SeatHelper/studentSession.js').replace(/^import.*$/gm, '').replace('export function', 'function')
  vm.runInContext(source + '\nprovideStudentSeatSession()', ctx)
  await start([1, 'A'])
  for (let i = 0; i < 4; i++) await timers.shift()()
  assert.equal(requests.length, 5)
  user.token = ''
  await timers.shift()(); assert.equal(requests.length, 5)
  user.token = 'B'; await start([1, 'B']); unmount()
  await timers.shift()(); assert.equal(requests.length, 6)
})
