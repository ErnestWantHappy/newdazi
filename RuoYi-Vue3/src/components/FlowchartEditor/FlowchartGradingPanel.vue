<template>
  <div v-loading="loading" class="flowchart-grading">
    <el-tabs v-if="submission" v-model="activeTab">
      <el-tab-pane label="学生作品" name="student">
        <div v-if="aiSuggestion" class="ai-suggestion-panel">
          <div class="ai-suggestion-head">
            <strong>AI 建议：{{ aiSuggestion.suggestedScore }} 分</strong>
            <el-tag size="small" type="warning">仅供教师复核</el-tag>
          </div>
          <div v-if="aiSummary" class="ai-summary">{{ aiSummary }}</div>
          <div class="ai-confidence">置信度：{{ formatConfidence(aiSuggestion.confidence) }}</div>
          <div class="ai-note">图片为主要评分依据，流程图 JSON 和结构检查结果仅供参考。</div>
          <el-button type="success" plain size="small" @click="$emit('apply-suggestion', Number(aiSuggestion.suggestedScore || 0))">
            采用到评分框
          </el-button>
        </div>
        <flowchart-editor :model-value="submission.documentJson" mode="READONLY" :height="460" />
      </el-tab-pane>
      <el-tab-pane label="标准答案" name="answer">
        <flowchart-editor :model-value="answerJson" mode="READONLY" :height="460" />
      </el-tab-pane>
      <el-tab-pane label="结构检查" name="check">
        <div class="check-summary">
          <el-statistic title="结构检查参考分" :value="Number(submission.suggestedScore || 0)" />
          <el-button type="success" plain @click="$emit('apply-suggestion', Number(submission.suggestedScore || 0))">填入评分框</el-button>
          <span>结构检查只作参考，正式成绩以教师确认提交为准。</span>
        </div>
        <el-table :data="checkItems" border>
          <el-table-column label="结果" width="90" align="center">
            <template #default="{ row }"><el-tag :type="row.status === 'PASS' ? 'success' : 'danger'">{{ row.status === 'PASS' ? '通过' : '需检查' }}</el-tag></template>
          </el-table-column>
          <el-table-column label="类型" width="80"><template #default="{ row }">{{ row.kind === 'NODE' ? '节点' : '连线' }}</template></el-table-column>
          <el-table-column prop="message" label="说明" min-width="240" />
          <el-table-column prop="weight" label="权重" width="90" />
        </el-table>
      </el-tab-pane>
    </el-tabs>
    <el-empty v-else description="未读取到画程提交" />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import FlowchartEditor from './FlowchartEditor.vue'
import { getFlowchartGradingSubmission } from '@/api/business/flowchart'

const props = defineProps({
  lessonId: { type: [Number, String], default: null },
  questionId: { type: [Number, String], default: null },
  studentId: { type: [Number, String], default: null },
  aiSuggestion: { type: Object, default: null },
  aiSummary: { type: String, default: '' }
})
defineEmits(['apply-suggestion'])
const loading = ref(false)
const activeTab = ref('student')
const submission = ref(null)
const answerJson = ref('')
const checkItems = ref([])

watch(() => [props.lessonId, props.questionId, props.studentId], load, { immediate: true })

async function load() {
  if (!props.lessonId || !props.questionId || !props.studentId) return
  loading.value = true
  submission.value = null
  try {
    const response = await getFlowchartGradingSubmission({
      lessonId: props.lessonId, questionId: props.questionId, studentId: props.studentId
    })
    submission.value = response.data?.submission || null
    answerJson.value = response.data?.answerJson || ''
    try { checkItems.value = JSON.parse(submission.value?.checkResultJson || '{}').items || [] }
    catch (_) { checkItems.value = [] }
    activeTab.value = 'student'
  } finally { loading.value = false }
}

function formatConfidence(value) {
  const numeric = Number(value)
  return Number.isFinite(numeric) ? `${Math.round(numeric * 100)}%` : '--'
}
</script>

<style scoped lang="scss">
.flowchart-grading { height: 100%; padding: 0 10px 10px; overflow: auto; }
.check-summary { display: flex; align-items: center; gap: 18px; padding: 14px; margin-bottom: 12px; background: #f0f9eb; border-radius: 10px; }
.check-summary span { color: #67798a; font-size: 13px; }
.ai-suggestion-panel { padding: 14px; margin-bottom: 14px; border: 1px solid #b3e19d; background: #f0f9eb; border-radius: 8px; }
.ai-suggestion-head { display: flex; align-items: center; gap: 10px; }
.ai-summary { margin: 8px 0; color: #445; line-height: 1.5; }
.ai-confidence, .ai-note { margin: 6px 0 10px; color: #67798a; font-size: 13px; }
</style>
