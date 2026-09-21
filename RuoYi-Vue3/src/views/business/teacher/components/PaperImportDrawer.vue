<template>
  <el-drawer :model-value="true" title="导入纸质作业" size="min(960px, 100%)" :before-close="close">
    <div class="scope"><strong>{{ scope.lessonTitle }}</strong><span>{{ scope.entryYear }}级 {{ scope.classCode }}班</span><span>{{ scope.questionTitle }}</span></div>
    <div class="actions">
      <el-radio-group v-model="importMode" :disabled="busy"><el-radio-button value="single" label="single">每份PDF一名学生</el-radio-button><el-radio-button value="class" label="class">整班合并PDF</el-radio-button></el-radio-group>
      <el-button :icon="Upload" :disabled="busy || !roster.length" @click="picker.click()">选择 PDF</el-button>
      <input ref="picker" type="file" accept="application/pdf,.pdf" multiple hidden @change="chooseFiles" />
      <el-button type="primary" :disabled="busy || !rows.some(r => r.token && r.studentId && r.status !== '已导入')" @click="importMatched">导入已匹配作业</el-button>
      <span>{{ rows.filter(r => r.status === '已导入').length }} / {{ rows.length }} 已导入</span>
    </div>
    <el-alert v-if="error" :title="error" type="error" :closable="false" />
    <el-table :data="rows" empty-text="暂无 PDF" row-key="id">
      <el-table-column label="原件" min-width="170"><template #default="{ row }"><el-button link type="primary" @click="preview = row">{{ row.file.name }}</el-button><div v-if="row.pageCount">{{ row.pageCount }} 页</div></template></el-table-column>
      <el-table-column label="学生" min-width="180"><template #default="{ row }">
        <el-select v-model="row.studentId" filterable placeholder="待核对" :disabled="busy || !!row.batchToken || row.locked || row.status === '已导入'">
          <el-option v-for="s in roster" :key="s.studentId" :value="s.studentId" :label="`${s.studentNo} ${s.studentName}`" />
        </el-select>
        <div v-if="row.identity" class="detail">{{ row.identity.studentNo }} {{ row.identity.name }}</div>
      </template></el-table-column>
      <el-table-column label="状态" min-width="170"><template #default="{ row }"><span>{{ row.status }}</span><div class="detail">{{ row.message }}</div></template></el-table-column>
      <el-table-column label="操作" width="135"><template #default="{ row }">
        <el-button v-if="row.batchToken" link type="primary" :disabled="busy" @click="openSegments(row)">确认学生页段</el-button>
        <el-button v-if="row.token && row.status !== '已导入'" link type="primary" :disabled="busy || !row.studentId" @click="importOne(row, true)">确认导入</el-button>
        <el-button v-if="!row.token && row.status === '失败'" link type="primary" :disabled="busy" @click="retry(row)">重试</el-button>
        <el-button v-if="row.status !== '已导入'" :icon="Delete" circle text title="移除" :disabled="busy" @click="remove(row)" />
      </template></el-table-column>
    </el-table>
    <el-dialog v-model="segmentsVisible" title="整班PDF学生页段" width="min(1050px, 96vw)" append-to-body :close-on-click-modal="false" :before-close="closeSegments">
      <template v-if="batchRow">
        <div class="actions"><span>共 {{ batchRow.pageCount }} 页 · 每人页数</span><el-input-number v-model="pagesPerStudent" :min="1" :max="50" :disabled="busy" />
          <el-button :disabled="busy" @click="fixedSegments">按页数分段</el-button><el-button :loading="identifying" :disabled="busy" @click="autoSegments">逐页识别身份</el-button>
          <el-button v-if="identifying" @click="stopIdentity = true">停止识别</el-button><span>{{ segmentProgress }}</span></div>
        <el-alert v-if="segmentError" :title="segmentError" type="warning" :closable="false" />
        <el-table :data="segments" max-height="430">
          <el-table-column label="起始页" width="85"><template #default="{ $index }">{{ segmentStart($index) }}</template></el-table-column>
          <el-table-column label="结束页" width="160"><template #default="{ row }"><el-input-number v-model="row.endPage" :min="1" :max="batchRow.pageCount" :disabled="busy" @change="clearConfirmations" /></template></el-table-column>
          <el-table-column label="学生" min-width="180"><template #default="{ row }"><el-select v-model="row.studentId" filterable :disabled="busy" @change="row.confirmed = false"><el-option v-for="s in roster" :key="s.studentId" :value="s.studentId" :label="`${s.studentNo} ${s.studentName}`" /></el-select></template></el-table-column>
          <el-table-column label="核对" width="160"><template #default="{ row, $index }"><el-button link @click="preview = { url: batchRow.url + '#page=' + segmentStart($index) }">查看原页</el-button><el-checkbox v-model="row.confirmed" :disabled="busy">已核对</el-checkbox></template></el-table-column>
          <el-table-column width="55"><template #default="{ $index }"><el-button :icon="Delete" circle text title="删除页段" :disabled="busy" @click="segments.splice($index, 1); clearConfirmations()" /></template></el-table-column>
        </el-table>
        <div class="actions"><el-button :disabled="busy" @click="segments.push({ endPage: batchRow.pageCount, studentId: null, confirmed: false })">添加页段</el-button><el-button type="primary" :loading="splitting" :disabled="busy" @click="confirmSegments">确认分段</el-button></div>
      </template>
    </el-dialog>
    <el-dialog :model-value="!!preview" title="PDF 原件" width="min(1000px, 95vw)" append-to-body @close="preview = null">
      <iframe v-if="preview" :src="preview.url" title="PDF 原件" class="pdf" />
    </el-dialog>
  </el-drawer>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessageBox } from 'element-plus'
import { Upload, Delete } from '@element-plus/icons-vue'
import { getPaperRoster, uploadPaper, submitPaper, paperPageIdentity, splitPaper } from '@/api/business/paperScan'
import { getPracticalSubmissions } from '@/api/business/teacherGrading'
const props = defineProps({ scope: { type: Object, required: true } })
const emit = defineEmits(['close', 'imported'])
// 抽屉打开时固定上下文，后台请求不随外部选课变化。
const scope = { ...props.scope }
const picker = ref(null), rows = ref([]), roster = ref([]), busy = ref(false), error = ref(''), preview = ref(null)
const importMode = ref('single'), segmentsVisible = ref(false), batchRow = ref(null), segments = ref([]), pagesPerStudent = ref(2)
const identifying = ref(false), splitting = ref(false), segmentProgress = ref(''), segmentError = ref('')
let stopIdentity = false
function segmentStart(index) { return index === 0 ? 1 : Number(segments.value[index - 1].endPage) + 1 }
function clearConfirmations() { segments.value.forEach(s => { s.confirmed = false }) }
function openSegments(row) { batchRow.value = row; segments.value = row.segments || []; segmentError.value = ''; segmentProgress.value = ''; segmentsVisible.value = true; if (!segments.value.length) fixedSegments() }
function closeSegments(done) { if (!busy.value) { batchRow.value.segments = segments.value; done() } }
function fixedSegments() {
  if (!Number.isInteger(pagesPerStudent.value) || pagesPerStudent.value < 1 || pagesPerStudent.value > 50) { segmentError.value = '每人页数须为1至50的整数'; return }
  segmentError.value = ''
  segments.value = []
  for (let start = 1; start <= batchRow.value.pageCount; start += pagesPerStudent.value) segments.value.push({ endPage: Math.min(start + pagesPerStudent.value - 1, batchRow.value.pageCount), studentId: null, confirmed: false })
}
async function autoSegments() {
  busy.value = true; identifying.value = true; stopIdentity = false; segmentError.value = ''; segments.value = []
  let unknown = 0
  try {
    for (let page = 1; page <= batchRow.value.pageCount && !stopIdentity; page++) {
      segmentProgress.value = `身份识别 ${page}/${batchRow.value.pageCount} 页`
      const result = (await paperPageIdentity({ ...scope, token: batchRow.value.batchToken, page })).data
      let last = segments.value[segments.value.length - 1]
      if (!last || (result.studentId && result.studentId !== last.studentId)) { last = { endPage: page, studentId: result.studentId, confirmed: false }; segments.value.push(last) }
      last.endPage = page
      if (!result.studentId) unknown++
    }
    segmentError.value = `识别建议尚未确认，${unknown}页身份不明确；请逐段核对原页、学生及边界。`
  } catch (e) { segmentError.value = e.message || '身份识别失败，可按页数分段并人工核对' }
  finally { busy.value = false; identifying.value = false }
}
async function confirmSegments() {
  const plan = segments.value.map((s, i) => ({ ...s, startPage: segmentStart(i) }))
  if (!plan.length || plan.some(s => !s.confirmed || !s.studentId || s.endPage < s.startPage || s.endPage - s.startPage >= 50) || plan.at(-1).endPage !== batchRow.value.pageCount || new Set(plan.map(s => s.studentId)).size !== plan.length) { segmentError.value = '请完整分配所有页面，核对每名学生且不重复，单人最多50页'; return }
  busy.value = true; splitting.value = true
  try {
    const children = (await splitPaper({ ...scope, token: batchRow.value.batchToken, segments: plan })).data
    const parent = batchRow.value
    const added = children.map(c => ({ ...c, id: c.token, file: parent.file, url: URL.createObjectURL(parent.file), locked: true, status: '页段已确认，待导入', message: `原件第${c.startPage}至${c.endPage}页` }))
    rows.value.splice(rows.value.indexOf(parent), 1, ...added); URL.revokeObjectURL(parent.url); segmentsVisible.value = false
  } catch (e) { segmentError.value = e.message || '分段失败，请重试原方案' }
  finally { busy.value = false; splitting.value = false }
}
onMounted(async () => {
  try { roster.value = (await getPaperRoster(scope)).data || [] }
  catch (e) { error.value = e.message || '名单读取失败' }
})
onBeforeUnmount(() => { stopIdentity = true; rows.value.forEach(r => URL.revokeObjectURL(r.url)) })
async function upload(row) {
  row.status = '上传及识别中'; row.message = ''
  const data = new FormData()
  for (const key of ['lessonId', 'questionId', 'entryYear', 'classCode']) data.append(key, scope[key])
  data.append('file', row.file)
  data.append('classPdf', row.classPdf)
  try {
    Object.assign(row, (await uploadPaper(data)).data)
    row.status = row.batchToken ? '待确认学生页段' : row.studentId ? '已匹配，待导入' : '待核对学生'
  } catch (e) { row.status = '失败'; row.message = e.message || '上传失败' }
}
async function chooseFiles(event) {
  const files = Array.from(event.target.files || []); event.target.value = ''
  const added = []
  for (const file of files) {
    if (!/\.pdf$/i.test(file.name) || file.size > 50 * 1024 * 1024 || !file.size) { error.value = '仅支持非空 PDF，单文件不超过 50 MiB'; continue }
    const row = { id: `${Date.now()}-${rows.value.length}`, file, classPdf: importMode.value === 'class', url: URL.createObjectURL(file), status: '等待上传', studentId: null }
    rows.value.push(row); added.push(rows.value[rows.value.length - 1])
  }
  busy.value = true
  let cursor = 0
  async function worker() { while (cursor < added.length) { const row = added[cursor++]; await upload(row) } }
  try { await Promise.all([worker(), worker()]) } finally { busy.value = false }
}
async function retry(row) { busy.value = true; try { await upload(row) } finally { busy.value = false } }
async function performImport(row, allowReplace) {
  if (rows.value.some(r => r !== row && r.studentId === row.studentId && r.status === '已导入')) {
    row.status = '待核对'; row.message = '本次已有该学生的作业，请核对重复文件'; return
  }
  const current = ((await getPracticalSubmissions(scope.lessonId, scope.questionId, scope.classCode, scope.entryYear)).data || []).find(s => String(s.studentId) === String(row.studentId))
  const existing = current && (current.submitted || current.score != null || current.practicalVersionId)
  if (existing && !allowReplace) { row.status = '待确认替换'; row.message = '该学生已有作品，请单独确认'; return }
  if (existing) {
    await ElMessageBox.confirm(`确认替换${current.studentName}的当前作品？原作品及成绩将归档，新作品需要重新批改。`, '替换确认', { type: 'warning' })
  }
  row.status = '导入中'; row.message = ''
  await submitPaper({ ...scope, token: row.token, studentId: row.studentId, expectedVersionId: current?.practicalVersionId || null, expectedScore: current?.score ?? null, replace: !!existing })
  row.status = '已导入'; emit('imported')
}
async function importOne(row, allowReplace) {
  busy.value = true
  try { await performImport(row, allowReplace) }
  catch (e) { if (e !== 'cancel' && e !== 'close') { row.status = '导入失败'; row.message = e.message || '请刷新后重试' } }
  finally { busy.value = false }
}
async function importMatched() {
  busy.value = true
  try {
    for (const row of rows.value.filter(r => r.token && r.studentId && r.status !== '已导入')) {
      try { await performImport(row, false) } catch (e) { row.status = '导入失败'; row.message = e.message || '请重试' }
    }
  } finally { busy.value = false }
}
function remove(row) { URL.revokeObjectURL(row.url); rows.value = rows.value.filter(r => r !== row) }
async function close() {
  if (busy.value) return
  if (rows.value.some(r => r.status !== '已导入')) {
    try { await ElMessageBox.confirm('还有未导入文件，关闭后需重新选择文件。确认关闭？', '未完成导入', { type: 'warning' }) } catch { return }
  }
  emit('close')
}
</script>

<style scoped>
.scope { display: flex; flex-wrap: wrap; gap: 12px; margin-bottom: 18px; overflow-wrap: anywhere; }
.actions { display: flex; align-items: center; flex-wrap: wrap; gap: 10px; margin-bottom: 16px; }
.detail { color: #73767a; font-size: 12px; white-space: normal; overflow-wrap: anywhere; }
.pdf { width: 100%; height: 70vh; border: 0; }
</style>
