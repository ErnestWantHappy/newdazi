<template>
  <el-dialog :model-value="modelValue" title="课堂推进" width="900px" class="lesson-advance-dialog"
    :close-on-click-modal="false" :close-on-press-escape="!executing" :show-close="!executing"
    @update:model-value="closeDialog">
    <p class="advance-intro">只推进达到人数门槛的班级，无需等待自动推进计时。切课后，学生不能继续提交旧课，已有作品和成绩仍可查看。</p>
    <div class="advance-filters">
      <label>年级
        <el-select v-model="entryYear" aria-label="推进年级" :disabled="executing">
          <el-option v-for="group in gradeOptions" :key="group.entryYear" :value="String(group.entryYear)"
            :label="`${group.entryYear}级（${group.gradeName}）`" />
        </el-select>
      </label>
      <label>班级
        <el-select v-model="classCodes" aria-label="推进班级" multiple collapse-tags collapse-tags-tooltip :disabled="executing">
          <el-option v-for="code in classOptions" :key="code" :value="code" :label="`${code}班`" />
        </el-select>
      </label>
      <el-button :disabled="executing || previewLoading || !classCodes.length" @click="refreshPreview">刷新状态</el-button>
    </div>
    <el-alert v-if="previewError" type="error" :closable="false" show-icon :title="previewError" />
    <div v-loading="previewLoading" :aria-busy="previewLoading">
      <p v-if="previewLoading" role="status">正在核对各班当前课程和推进条件…</p>
      <div class="advance-classes">
        <article v-for="row in rows" :key="row.classCode" class="advance-class">
          <div class="advance-class-title">
            <strong>{{ row.classCode }}班</strong>
            <el-tag :type="row.canAdvance ? 'success' : 'warning'">{{ row.canAdvance ? '可手动推进' : '暂不能推进' }}</el-tag>
          </div>
          <div class="advance-course"><span>当前课</span>{{ row.currentLessonTitle || '暂无可操作课程' }}</div>
          <div class="advance-course"><span>下一课</span>{{ row.nextLessonTitle || '暂无可操作课程' }}</div>
          <p v-if="row.totalCount != null" class="advance-count">有成绩 {{ row.readyCount }}/{{ row.totalCount }} 人（含已交作品），需 {{ row.thresholdPct }}%（至少{{ row.requiredCount }}人）</p>
          <p v-if="!row.canAdvance" class="advance-reason">{{ row.reason }}</p>
          <p class="advance-auto">自动推进：{{ row.autoStatus }}<template v-if="row.autoDueTime">；预计 {{ formatDue(row.autoDueTime) }} 附近执行（每5分钟扫描）</template></p>
        </article>
      </div>
      <el-empty v-if="!previewLoading && !rows.length && !previewError" description="请选择班级查看推进条件" :image-size="60" />
    </div>
    <div v-if="resultSummary" ref="resultPanel" class="advance-results" role="status" aria-live="polite">
      <strong>{{ resultSummary }}</strong>
      <p v-for="result in results" :key="result.classCode">{{ result.classCode }}班 · {{ result.success ? '已推进' : '未推进' }}：{{ result.message }}</p>
      <p>结果已保留。再次操作前请点击“刷新状态”，核对各班是否已进入新课。</p>
    </div>
    <template #footer>
      <span class="advance-footer-hint">{{ executed ? '本轮已结束，请先刷新状态' : previewError ? '状态核对失败，请刷新重试' : `当前可推进 ${readyRows.length} 个班级` }}</span>
      <el-button :disabled="executing" @click="closeDialog(false)">关闭</el-button>
      <el-button type="primary" :loading="executing" :disabled="!canSubmit" @click="executeAdvance">推进 {{ readyRows.length }} 个达标班级</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ElMessageBox } from 'element-plus'
import { manualAdvanceLesson, previewAdvanceLesson } from '@/api/business/lesson'

const props = defineProps({ modelValue: Boolean, gradeGroups: { type: Array, default: () => [] } })
const emit = defineEmits(['update:modelValue', 'advanced'])
const entryYear = ref('')
const classCodes = ref([])
const rows = ref([])
const previewLoading = ref(false)
const previewError = ref('')
const executing = ref(false)
const executed = ref(false)
const results = ref([])
const resultSummary = ref('')
const resultPanel = ref(null)
let requestVersion = 0

const normalize = code => String(code ?? '').replace('班', '').trim()
function managedClasses(group) {
  const managed = new Set((group?.allClassesInGrade || []).map(normalize))
  return [...new Set((group?.lessons || [])
    .filter(lesson => lesson.lessonMode !== 'attendance' && Number(lesson.grade) === Number(group.gradeId))
    .flatMap(lesson => lesson.assignedClasses || []).map(normalize).filter(code => code && managed.has(code)))]
    .sort((a, b) => Number(a) - Number(b))
}
const gradeOptions = computed(() => props.gradeGroups.filter(group => managedClasses(group).length))
const classOptions = computed(() => managedClasses(props.gradeGroups.find(group => String(group.entryYear) === entryYear.value)))
const readyRows = computed(() => rows.value.filter(row => row.canAdvance))
const canSubmit = computed(() => !executing.value && !executed.value && !previewLoading.value && !previewError.value && readyRows.value.length > 0)

watch(() => props.modelValue, open => {
  if (open) {
    results.value = []
    resultSummary.value = ''
    entryYear.value = String(gradeOptions.value[0]?.entryYear || '')
    classCodes.value = [...classOptions.value]
  } else {
    requestVersion++
  }
})
watch(entryYear, () => { classCodes.value = [...classOptions.value] })
watch(() => [props.modelValue, entryYear.value, classCodes.value], () => {
  if (props.modelValue) refreshPreview()
}, { deep: true })

async function refreshPreview(resetExecution = true) {
  const version = ++requestVersion
  rows.value = []
  previewError.value = ''
  if (resetExecution) executed.value = false
  if (!props.modelValue || !entryYear.value || !classCodes.value.length) {
    previewLoading.value = false
    return
  }
  previewLoading.value = true
  try {
    const response = await previewAdvanceLesson({ entryYear: entryYear.value, classCodes: [...classCodes.value] })
    if (version !== requestVersion) return
    if (!Array.isArray(response.data?.rows)) throw new Error('推进状态响应不完整')
    rows.value = response.data.rows
  } catch {
    if (version === requestVersion) previewError.value = '未能读取推进状态，班级选择已保留，请点击“刷新状态”重试。'
  } finally {
    if (version === requestVersion) previewLoading.value = false
  }
}

async function executeAdvance() {
  if (!canSubmit.value) return
  const selected = readyRows.value.map(row => ({ ...row }))
  const selectedYear = entryYear.value
  // 确认期间也锁住选择，防止确认的班级与实际发送的请求不一致。
  executing.value = true
  try {
    await ElMessageBox.confirm(`将 ${selected.map(row => row.classCode + '班').join('、')} 推进到上方所示下一课？切课后学生不能继续提交旧课。`, '确认课堂推进', {
      type: 'warning', confirmButtonText: '推进达标班级', cancelButtonText: '返回核对'
    })
  } catch {
    executing.value = false
    return
  }
  executed.value = true
  requestVersion++
  try {
    const response = await manualAdvanceLesson({
      entryYear: selectedYear,
      classCodes: selected.map(row => row.classCode),
      expectedLessonIds: Object.fromEntries(selected.map(row => [row.classCode, row.currentLessonId]))
    })
    if (!Array.isArray(response.data?.details)) throw new Error('推进结果响应不完整')
    results.value = response.data.details
    resultSummary.value = `已推进 ${response.data.advanced} 个班级，${response.data.failed} 个未推进`
    if (Number(response.data.advanced) > 0) emit('advanced')
    await refreshPreview(false)
  } catch {
    results.value = []
    resultSummary.value = '未能确认本次推进结果。部分班级可能已经推进，请先刷新状态核对，避免重复切课。'
    emit('advanced')
  } finally {
    executing.value = false
    await nextTick()
    resultPanel.value?.scrollIntoView({ block: 'nearest' })
  }
}

function formatDue(time) {
  return new Date(time).toLocaleString('zh-CN', { hour12: false })
}
function closeDialog(value) {
  if (!executing.value) emit('update:modelValue', value)
}
let refreshTimer
onMounted(() => {
  refreshTimer = setInterval(() => {
    if (props.modelValue && !executing.value && !executed.value && !previewLoading.value) refreshPreview(false)
  }, 30000)
})
onBeforeUnmount(() => { requestVersion++; clearInterval(refreshTimer) })
</script>

<style scoped>
.advance-intro { margin: 0 0 16px; line-height: 1.7; color: #606266; }
.advance-filters { display: flex; flex-wrap: wrap; gap: 12px; align-items: end; margin-bottom: 16px; }
.advance-filters label { display: grid; gap: 6px; flex: 1; min-width: 180px; }
.advance-classes { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; }
.advance-class { border: 1px solid #dcdfe6; border-radius: 6px; padding: 12px; overflow-wrap: anywhere; }
.advance-class-title { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.advance-course { display: flex; gap: 12px; margin: 6px 0; }
.advance-course span { flex-shrink: 0; color: #606266; }
.advance-count, .advance-auto, .advance-reason { font-size: 13px; line-height: 1.6; margin: 8px 0 0; }
.advance-auto { color: #606266; }
.advance-reason { color: #9b5800; }
.advance-results { margin-top: 16px; padding: 12px; background: #f4f7fb; border-radius: 6px; line-height: 1.7; }
.advance-results p { margin: 8px 0 0; }
.advance-footer-hint { display: block; margin-bottom: 10px; color: #606266; font-size: 13px; }
@media (max-width: 640px) {
  .advance-classes { grid-template-columns: 1fr; }
  .advance-filters label { min-width: 100%; }
}
</style>
<style>
.lesson-advance-dialog { max-width: calc(100vw - 24px); }
@media (max-width: 640px) {
  .lesson-advance-dialog { margin-top: 4vh; }
  .lesson-advance-dialog .el-dialog__body { max-height: 64vh; overflow-y: auto; }
  .lesson-advance-dialog .el-dialog__footer { text-align: right; }
}
</style>
