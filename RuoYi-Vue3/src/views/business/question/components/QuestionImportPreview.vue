<script setup>
import { computed, getCurrentInstance, ref, shallowRef, watch } from 'vue'
import { ElMessage } from 'element-plus'
import useUserStore from '@/store/modules/user'
import QuestionMedia from '@/components/QuestionMedia/QuestionMedia.vue'
import QuestionImage from '@/components/QuestionMedia/QuestionImage.vue'
import QuestionMediaInput from '@/components/QuestionMedia/QuestionMediaInput.vue'
import ChoiceOptionFields from '@/components/QuestionMedia/ChoiceOptionFields.vue'
import { newQuestionRequestId, normalizeQuestionMedia } from '@/utils/questionMedia'
import { uploadQuestionWorkbook, getQuestionImport, listQuestionImports, patchQuestionImport, reviewQuestionImport, confirmQuestionImport } from '@/api/business/questionImages'

const props = defineProps({ modelValue: Boolean })
const { proxy }=getCurrentInstance()
function downloadTemplate(){proxy.download('business/question/importTemplate',{},'题库导入模板.xlsx')}
const emit = defineEmits(['update:modelValue','completed','view'])
const user = useUserStore(), picker = ref(null), batch = ref(null), recent = ref([]), selectedId = shallowRef('')
const busy = shallowRef(false), progress = shallowRef(null), error = shallowRef(''), rowBusy = shallowRef(false)
const onlyErrors = shallowRef(false), page = shallowRef(1), draft = ref(null), placements = ref([]), resolvedFields = ref([])
const uploadBlocked = ref({}), lastFile = shallowRef(null), showResult = shallowRef(false), stemInput = ref(null)
const storedKey = `choice-import-last:${user.id}`
let originalDraft = 'null', originalPlacements = '[]', requestSequence = 0
const selected = computed(() => batch.value?.items?.find(x => x.itemId === selectedId.value))
const rows = computed(() => (batch.value?.items || []).filter(x => !onlyErrors.value || (x.included && x.errors.length)))
const visibleRows = computed(() => rows.value.slice((page.value-1)*10,page.value*10))
const dirty = computed(() => JSON.stringify(draft.value) !== originalDraft || JSON.stringify(placements.value) !== originalPlacements || resolvedFields.value.length > 0)
const completed = computed(() => batch.value?.state === 'COMMITTED')
const globalPending = computed(() => batch.value?.issues?.messages?.length && !batch.value?.issues?.reviewed)
const blocked = computed(() => Object.values(uploadBlocked.value).some(Boolean))
const ready = computed(() => batch.value && !completed.value && !batch.value.errorCount && batch.value.includedCount > 0 && !globalPending.value && !dirty.value && !blocked.value && !rowBusy.value && !busy.value)
function storeLast(id) { try { localStorage.setItem(storedKey,String(id)) } catch { /* 浏览器禁用存储时仍可从最近导入找回。 */ } }
function accept(data, prefer) {
  batch.value = data; storeLast(data.batchId)
  selectedId.value = prefer && data.items.some(x => x.itemId === prefer) ? prefer : (data.items.find(x => x.included && x.errors.length)?.itemId || data.items[0]?.itemId || '')
  prepareDraft()
}
function prepareDraft() {
  const row = selected.value
  uploadBlocked.value = {}; resolvedFields.value = []
  draft.value = row ? JSON.parse(JSON.stringify(row.draft.question)) : null
  if (draft.value) draft.value.media = normalizeQuestionMedia(draft.value.media)
  placements.value = (row?.draft.pendingImages || []).map(p => ({ pendingId:p.pendingId || '',imageId:p.imageId,slot:p.slot,targetItemId:Number(row.itemId),discard:false }))
  originalDraft = JSON.stringify(draft.value); originalPlacements = JSON.stringify(placements.value)
}
function selectRow(row) {
  if (rowBusy.value || blocked.value) return ElMessage.warning('请先处理当前图片上传')
  if (dirty.value) return ElMessage.warning('本题修改尚未保存，请先保存修正或恢复原内容')
  selectedId.value = row.itemId; prepareDraft()
}
async function load(id) {
  busy.value = true;error.value='';const sequence=++requestSequence
  try { const response=await getQuestionImport(id);if(sequence===requestSequence) accept(response.data) }
  catch(e){error.value=e.message||'预览读取失败，可重试或从最近导入重新打开'}
  finally {if(sequence===requestSequence)busy.value=false}
}
async function loadRecent() {try {recent.value=(await listQuestionImports()).data||[]} catch { /* 失败不清空当前预览。 */ }}
async function chooseFile(file, allowDuplicate=false) {
  if (!file || busy.value) return
  if(!/\.(xlsx|xls)$/i.test(file.name)||file.size>50*1024*1024){error.value='请选择不超过50MB的xls或xlsx文件';return}
  if(dirty.value) {error.value='请先保存或恢复当前题目的修正，再更换文件';return}
  lastFile.value=file; busy.value=true;progress.value=null;error.value='';showResult.value=false
  const identity=`${file.name}:${file.size}:${file.lastModified}`
  let requestId=newQuestionRequestId()
  try {const cached=JSON.parse(localStorage.getItem(storedKey+':upload')||'null');if(!allowDuplicate&&cached?.identity===identity)requestId=cached.requestId;localStorage.setItem(storedKey+':upload',JSON.stringify({identity,requestId}))}catch{}
  try {
    const response=await uploadQuestionWorkbook(file,requestId,value=>progress.value=value,allowDuplicate)
    accept(response.data);try{localStorage.removeItem(storedKey+':upload')}catch{}
    await loadRecent()
  } catch(e) {error.value=`${e.message||'识别失败'}；原预览和题库未清空，可重试或查看最近导入。`;await loadRecent()}
  finally {busy.value=false;progress.value=null}
}
function fileChanged(e){const f=e.target.files?.[0];if(f)chooseFile(f);e.target.value=''}
function dropFile(e){e.preventDefault();const file=e.dataTransfer?.files?.[0];if(file)chooseFile(file)}
async function saveRow(included=selected.value.included) {
  if(blocked.value)return ElMessage.warning('请先处理上传中的图片或取消失败图片')
  rowBusy.value=true;error.value=''
  try {const response=await patchQuestionImport(batch.value.batchId,selectedId.value,{expectedRevision:batch.value.revision,question:draft.value,resolvedFields:resolvedFields.value,placements:placements.value,included});accept(response.data,selectedId.value)}
  catch(e){error.value=e.message||'修正保存失败，当前输入已保留'}finally{rowBusy.value=false}
}
async function reviewSpecial(){rowBusy.value=true;try{const r=await reviewQuestionImport(batch.value.batchId,batch.value.revision);accept(r.data,selectedId.value)}catch(e){error.value=e.message}finally{rowBusy.value=false}}
async function confirm(){if(!ready.value)return;busy.value=true;error.value='';try{const r=await confirmQuestionImport(batch.value.batchId,batch.value.revision);accept(r.data);showResult.value=true;emit('completed',r.data.result);await loadRecent()}catch(e){error.value=`${e.message||'确认失败'}，未收到完成结果；请刷新批次查询，可安全重试。`}finally{busy.value=false}}
function close(){if(busy.value||rowBusy.value||blocked.value){ElMessage.warning('请等待当前上传或保存结束');return}if(dirty.value){ElMessage.warning('请先保存修正或恢复原内容');return}emit('update:modelValue',false)}
watch(() => props.modelValue, async value=>{if(value){await loadRecent();if(!batch.value){let id;try{id=localStorage.getItem(storedKey)}catch{}if(id)load(id)}}})
</script>

<template>
  <el-dialog :model-value="modelValue" title="Excel 图文题库导入" width="min(1400px, 97vw)" top="3vh" append-to-body :close-on-click-modal="false" :before-close="close" @update:model-value="value => { if (!value) close() }">
    <div class="import-workspace">
      <div class="import-topline"><span>文字写在原列，图片放右侧“题干图片1-3 / 选项A-D图片”列；系统按图片左上角所在列归类，跨到下一题行会要求确认，推荐使用浮动图片。<el-button link type="primary" @click="downloadTemplate">下载模板</el-button></span><el-select v-if="recent.length" placeholder="最近导入 / 恢复预览" style="width:260px" :disabled="busy || dirty" @change="load"><el-option v-for="r in recent" :key="r.batchId" :value="String(r.batchId)" :label="`${r.fileName} · ${r.state === 'COMMITTED' ? '已导入' : '待核对'}`" /></el-select></div>
      <div class="excel-drop" @dragover.prevent @drop="dropFile">
        <div><strong>{{ batch?.fileName || '将 Excel 拖到这里，或点击选择文件' }}</strong><p>{{ busy ? (progress !== null && progress < 100 ? `正在上传 ${progress}%` : '正在读取文字与图片，请稍候…') : '支持文字、浮动图片和图片专用列 · 无需填写图片地址或单独上传图片包' }}</p></div>
        <input ref="picker" type="file" accept=".xls,.xlsx" hidden @change="fileChanged"><el-button :loading="busy" :disabled="busy || dirty || blocked" @click="picker?.click()">{{ batch ? '更换 Excel' : '选择 Excel' }}</el-button>
      </div>
      <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="import-alert"><el-button v-if="batch" link @click="load(batch.batchId)">重新读取批次</el-button></el-alert>
      <template v-if="batch">
        <el-alert v-if="batch.alreadyImported" title="该文件已经导入，本次没有重复创建题目。" type="warning" :closable="false" class="import-alert"><el-button v-if="lastFile" link @click="chooseFile(lastFile,true)">仍要再次导入此文件</el-button></el-alert>
        <div v-if="completed" class="import-complete"><el-result icon="success" :title="`已导入 ${batch.result?.importedCount || 0} 道题目`" sub-title="题目与图片已一起保存，可从最近导入再次找到本批结果。"><template #extra><el-button type="primary" @click="showResult=true">查看本次题目</el-button></template></el-result><div v-if="showResult" class="result-rows"><div v-for="row in batch.items.filter(x=>x.questionId)" :key="row.itemId"><span>{{ row.draft.question.questionContent || '图片选择题' }}</span><el-button link type="primary" @click="emit('view',row.questionId)">查看题目</el-button></div></div></div>
        <template v-else>
          <div class="import-summary"><span>共 <b>{{ batch.questionCount }}</b> 题 / <b>{{ batch.imageCount }}</b> 张图片</span><span class="valid">{{ batch.validCount }} 题可导入</span><span class="warning">{{ batch.errorCount }} 题待修正</span><span v-if="batch.includedCount < batch.questionCount">已明确排除 {{ batch.questionCount-batch.includedCount }} 题</span><el-checkbox v-model="onlyErrors" @change="page=1">只看待修正</el-checkbox></div>
          <el-alert v-if="globalPending" type="warning" :closable="false" class="import-alert"><template #title>文件中有特殊图片格式或未定位图片，请核对预览。</template><p v-for="message in batch.issues.messages" :key="message">{{ message }}</p><el-button type="warning" plain :disabled="dirty || rowBusy" @click="reviewSpecial">已核对并补齐缺图，其余图片不属于题目</el-button></el-alert>
          <div class="import-grid">
            <section class="import-list"><el-table :data="visibleRows" highlight-current-row max-height="650" @row-click="selectRow"><el-table-column label="原表位置" width="116"><template #default="{ row }"><span>{{ row.draft.sheetName }}<br>第{{ row.draft.sourceRow }}行</span></template></el-table-column><el-table-column label="题目"><template #default="{ row }"><div class="import-title">{{ row.draft.question.questionContent || '图片选择题' }}</div><small>{{ row.draft.question.questionType === 'choice' ? '选择题' : row.draft.question.questionType === 'judgment' ? '判断题' : '打字题' }} · 答案 {{ row.draft.question.answer || '—' }}</small></template></el-table-column><el-table-column label="状态" width="94"><template #default="{ row }"><el-tag :type="!row.included ? 'info' : row.errors.length ? 'warning' : 'success'">{{ !row.included ? '已排除' : row.errors.length ? '待修正' : '正常' }}</el-tag></template></el-table-column></el-table><el-pagination v-model:current-page="page" :total="rows.length" :page-size="10" layout="prev, pager, next" small class="import-pagination" /></section>
            <section v-if="draft && selected" class="import-editor">
              <div class="import-editor-head"><strong>第{{ selected.draft.sourceRow }}行 · 图文预览与修正</strong><el-tag :type="selected.errors.length ? 'warning' : 'success'">{{ selected.errors.length ? '待修正' : '正常' }}</el-tag></div>
              <el-alert v-for="message in selected.errors" :key="message" :title="message" type="warning" :closable="false" class="row-error" />
              <el-form label-position="top" size="small">
                <el-form-item label="题干"><div style="width:100%" @paste="stemInput?.paste($event)" @dragover.prevent @drop="stemInput?.drop($event)"><el-input v-model="draft.questionContent" type="textarea" :rows="3" /><QuestionMediaInput v-if="draft.questionType === 'choice'" :key="selectedId" ref="stemInput" v-model="draft.media.stem" :limit="3" :context="{ batchId:batch.batchId }" label="题干图片" @blocked="uploadBlocked.stem=$event" /></div></el-form-item>
                <ChoiceOptionFields v-if="draft.questionType === 'choice'" :key="selectedId+'options'" v-model:question="draft" @blocked="uploadBlocked.options=$event" />
                <el-form-item v-if="draft.questionType !== 'typing'" label="标准答案"><el-radio-group v-model="draft.answer"><el-radio v-for="key in draft.questionType === 'choice' ? ['A','B','C','D'] : ['T','F']" :key="key" :value="key">{{ key }}</el-radio></el-radio-group></el-form-item>
                <el-form-item label="解析"><el-input v-model="draft.analysis" type="textarea" :rows="2" /></el-form-item>
                <el-form-item label="年级 / 学期"><div class="row-fields"><el-select v-model="draft.grade" placeholder="年级"><el-option v-for="n in 9" :key="n" :value="n" :label="n+'年级'" /></el-select><el-select v-model="draft.semester" placeholder="学期"><el-option label="上册" value="0" /><el-option label="下册" value="1" /></el-select></div></el-form-item>
                <div v-for="issue in selected.draft.issues" :key="issue.field+issue.message" class="source-issue"><span>{{ issue.message }}</span><el-checkbox v-model="resolvedFields" :value="issue.field">已核对并修正此项</el-checkbox></div>
                <div v-for="(pending,index) in selected.draft.pendingImages" :key="pending.pendingId || pending.imageId+':'+index" class="pending-image"><strong>待确认图片</strong><p>{{ pending.reason }}</p><QuestionImage :image-id="pending.imageId" :context="{batchId:batch.batchId}" /><div class="row-fields"><el-select v-model="placements[index].targetItemId" placeholder="所属题目"><el-option v-for="row in batch.items" :key="row.itemId" :value="Number(row.itemId)" :label="`第${row.draft.sourceRow}行 · ${row.draft.question.questionContent || '图片题'}`" /></el-select><el-select v-model="placements[index].slot" placeholder="放置位置"><el-option label="题干" value="stem" /><el-option v-for="key in ['A','B','C','D']" :key="key" :label="'选项'+key" :value="key" /></el-select></div><el-checkbox v-model="placements[index].discard">该图不属于题目，本次不导入此图</el-checkbox></div>
              </el-form>
              <div class="row-save"><el-button :disabled="rowBusy || blocked" @click="prepareDraft">恢复原内容</el-button><el-button :loading="rowBusy" :disabled="blocked" type="primary" @click="saveRow()">保存本题修正</el-button><el-button :disabled="rowBusy || blocked" link type="warning" @click="saveRow(!selected.included)">{{ selected.included ? '本次不导入此题' : '恢复导入此题' }}</el-button></div>
            </section>
          </div>
        </template>
      </template>
      <el-empty v-else-if="!busy" description="选择原题库模板Excel，系统会自动读取文字与图片" />
    </div>
    <template #footer><div class="import-footer"><span>{{ dirty ? '本题有未保存的修改，请先保存修正。' : completed ? '已完成的批次可从最近导入重新找到。' : '确认后整批保存；有异常先修正，不会部分导入。' }}</span><div><el-button :disabled="busy || rowBusy" @click="close">{{ completed ? '关闭' : '取消' }}</el-button><el-button v-if="batch && !completed" type="primary" :loading="busy" :disabled="!ready" @click="confirm">确认导入 {{ batch.includedCount }} 道题目</el-button></div></div></template>
  </el-dialog>
</template>

<style scoped>
.import-workspace{color:#606266}.import-topline,.excel-drop,.import-summary,.import-editor-head,.import-footer{display:flex;align-items:center;justify-content:space-between;gap:12px}.import-topline{font-size:13px;margin-bottom:14px;flex-wrap:wrap}.excel-drop{padding:18px;border:1px dashed #a9cbed;border-radius:5px;background:#f8fbff}.excel-drop p{margin:7px 0 0;color:#909399;font-size:12px}.import-alert{margin:12px 0}.import-summary{justify-content:flex-start;flex-wrap:wrap;margin:16px 0}.import-summary b{font-size:18px}.import-summary .el-checkbox{margin-left:auto}.valid{color:#67a84a}.warning{color:#c58b33}.import-grid{display:grid;grid-template-columns:minmax(0,1fr) minmax(360px,.85fr);gap:18px}.import-list,.import-editor{border:1px solid #e4e7ed;border-radius:5px;min-width:0;overflow:hidden}.import-title{display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden}.import-list small{font-size:11px;color:#909399}.import-pagination{padding:12px}.import-editor{padding:16px;max-height:700px;overflow:auto}.import-editor-head{margin-bottom:15px}.row-error{margin:7px 0}.row-fields{display:flex;gap:8px;width:100%;margin:10px 0}.row-fields>.el-select{flex:1;min-width:0}.pending-image,.source-issue{background:#fffaf1;border:1px solid #f0dfc1;padding:12px;border-radius:4px;margin:12px 0;font-size:12px}.pending-image p{color:#a77b38}.row-save{display:flex;gap:8px;flex-wrap:wrap;position:sticky;bottom:-16px;background:#fff;padding:13px 0;border-top:1px solid #ebeef5}.import-footer{font-size:12px;color:#909399;flex-wrap:wrap}.result-rows{max-height:450px;overflow:auto}.result-rows>div{display:flex;align-items:center;justify-content:space-between;gap:12px;padding:10px;border-bottom:1px solid #ebeef5}.result-rows>div>span{flex:1;min-width:0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}@media(max-width:760px){.import-grid{grid-template-columns:1fr}.import-editor{max-height:none}.import-list .el-table{font-size:12px}.excel-drop{padding:12px;flex-wrap:wrap}.import-summary{gap:9px;font-size:12px}.import-summary .el-checkbox{margin-left:0}.row-save{position:static}.import-footer>span{width:100%}}
</style>
