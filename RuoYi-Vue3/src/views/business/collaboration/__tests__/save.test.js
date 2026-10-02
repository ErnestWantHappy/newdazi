import { test } from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import vm from 'node:vm'

const source = fs.readFileSync(new URL('../index.vue', import.meta.url), 'utf8')
test('批量保存保留失败标识和错误，成功后释放标识', async () => {
  const ref = value => ({ value })
  const calls = []
  let id = 0, succeed = false
  const ctx = { saving: ref(false), saveError: ref(''), selectedMaterialIds: ref([10]),
    selectedClassItems: ref([{ entryYear: '2025', classCode: '1', students: [1] }]),
    lessonId: ref(7), batchRequestIds: new Map(), newRequestId: () => `batch-${++id}`,
    splitGroupsFor: students => [{ studentIds: students, materialId: 10 }],
    ElMessage: { warning() {}, success() {}, error() {} },
    load: async () => { ctx.saveError.value = '' },
    saveCollaborationWorkspace: async (lesson, payload) => {
      calls.push(payload)
      if (!succeed) throw new Error('网络中断')
    } }
  vm.createContext(ctx)
  vm.runInContext(source.slice(source.indexOf('async function batchSave()'), source.indexOf('function removeMember(')), ctx)
  await ctx.batchSave(); await ctx.batchSave()
  assert.equal(calls[0].requestId, calls[1].requestId)
  assert.match(ctx.saveError.value, /网络中断/)
  succeed = true
  await ctx.batchSave(); await ctx.batchSave()
  assert.equal(calls[1].requestId, calls[2].requestId)
  assert.notEqual(calls[2].requestId, calls[3].requestId)
  assert.equal(ctx.saving.value, false)
})

test('协作保存未知结果重试复用标识，改内容或成功后更换标识', async () => {
  const ref = value => ({ value })
  const calls = []
  let id = 0, succeed = false
  const ctx = { saving: ref(false), saveError: ref(''), validateBeforeSave: () => '',
    currentDetail: ref(null), isNewRound: ref(false), requestId: ref('initial'), lastSavePayload: '',
    activeClass: ref({ entryYear: '2025', classCode: '1' }), selectedMaterialIds: ref([10]),
    editGroups: ref([{ studentIds: [1], materialId: 10 }]), lessonId: ref(7),
    newRequestId: () => `request-${++id}`, load: async () => {},
    ElMessage: { warning() {}, success() {}, error() {} },
    saveCollaborationWorkspace: async (lesson, payload) => {
      calls.push(payload)
      if (!succeed) throw new Error('网络中断')
    } }
  vm.createContext(ctx)
  vm.runInContext(source.slice(source.indexOf('async function save()'), source.indexOf('function startNewRound()')), ctx)
  await ctx.save(); await ctx.save()
  assert.equal(calls[0].requestId, calls[1].requestId)
  ctx.editGroups.value[0].studentIds.push(2)
  await ctx.save()
  assert.notEqual(calls[1].requestId, calls[2].requestId)
  succeed = true
  await ctx.save(); await ctx.save()
  assert.equal(calls[2].requestId, calls[3].requestId)
  assert.notEqual(calls[3].requestId, calls[4].requestId)
})
