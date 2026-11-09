<template>
  <el-dialog v-model="visible" title="画程流程图预览" width="96%" top="3vh" append-to-body destroy-on-close>
    <div v-loading="loading" class="flowchart-question-preview">
      <p v-if="questionTitle" class="preview-question-title">{{ questionTitle }}</p>
      <div v-if="failed" role="alert">流程图加载失败。<el-button link type="primary" @click="load">重新加载</el-button></div>
      <el-tabs v-else-if="!loading" v-model="activeTab">
        <el-tab-pane label="学生起始图" name="starter" />
        <el-tab-pane label="参考答案图" name="answer" />
      </el-tabs>
      <template v-if="!loading && !failed">
        <el-empty v-if="activeTab === 'answer' && !canReadAnswer" description="仅题目创建者或管理员可查看参考答案图" />
        <el-empty v-else-if="activeTab === 'answer' && !answerJson" description="尚未设置参考答案图" />
        <flowchart-editor v-else :key="activeTab" :model-value="activeTab === 'starter' ? starterJson : answerJson" mode="READONLY" :height="580" />
      </template>
    </div>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { getFlowchartQuestion, getFlowchartQuestionPreview } from '@/api/business/flowchart'
import FlowchartEditor from './FlowchartEditor.vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  question: { type: Object, default: null },
  canReadAnswer: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue'])

const loading = ref(false)
const starterJson = ref('')
const answerJson = ref('')
const activeTab = ref('starter')
const failed = ref(false)
let requestId = 0
const visible = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})
const questionTitle = computed(() => plainText(props.question?.questionContent))

async function load() {
  const id = ++requestId
  loading.value = true
  failed.value = false
  starterJson.value = ''
  answerJson.value = ''
  try {
    const response = await (props.canReadAnswer ? getFlowchartQuestion : getFlowchartQuestionPreview)(props.question.questionId)
    if (id !== requestId) return
    starterJson.value = response.data?.starterJson || ''
    answerJson.value = response.data?.answerJson || ''
  } catch (_) {
    if (id === requestId) failed.value = true
  } finally {
    if (id === requestId) loading.value = false
  }
}
watch(() => [props.modelValue, props.question?.questionId, props.canReadAnswer], ([opened, questionId]) => {
  if (!opened || !questionId) { ++requestId; return }
  activeTab.value = 'starter'
  load()
}, { immediate: true })

function plainText(value) {
  const element = document.createElement('div')
  element.innerHTML = value || ''
  return element.textContent || element.innerText || ''
}
</script>

<style scoped>
.flowchart-question-preview { min-height: 360px; }
.preview-question-title { margin: 0 0 14px; color: #303133; font-size: 16px; line-height: 1.6; }
</style>
