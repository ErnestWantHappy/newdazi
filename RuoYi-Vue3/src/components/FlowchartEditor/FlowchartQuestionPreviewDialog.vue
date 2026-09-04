<template>
  <el-dialog v-model="visible" title="画程流程图预览" width="96%" top="3vh" append-to-body destroy-on-close>
    <div v-loading="loading" class="flowchart-question-preview">
      <p v-if="questionTitle" class="preview-question-title">{{ questionTitle }}</p>
      <flowchart-editor v-if="hasStarter" :model-value="starterJson" mode="READONLY" :height="580" />
      <el-empty v-else description="该流程图题尚未设置学生基础图" />
    </div>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { getFlowchartQuestionPreview } from '@/api/business/flowchart'
import FlowchartEditor from './FlowchartEditor.vue'
import { parseFlowchartDocument } from './schema'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  question: { type: Object, default: null }
})
const emit = defineEmits(['update:modelValue'])

const loading = ref(false)
const starterJson = ref('')
const visible = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})
const questionTitle = computed(() => plainText(props.question?.questionContent))
const hasStarter = computed(() => parseFlowchartDocument(starterJson.value).nodes.length > 0)

watch(() => [props.modelValue, props.question?.questionId], async ([opened, questionId]) => {
  if (!opened || !questionId) return
  loading.value = true
  starterJson.value = ''
  try {
    const response = await getFlowchartQuestionPreview(questionId)
    starterJson.value = response.data?.starterJson || ''
  } finally {
    loading.value = false
  }
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
