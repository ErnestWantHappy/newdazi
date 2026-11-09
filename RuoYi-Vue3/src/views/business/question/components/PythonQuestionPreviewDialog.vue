<script setup>
import { ref, computed, watch } from 'vue'
import { getQuestion } from '@/api/business/question'
import { getProgrammingQuestion, previewProgrammingQuestion } from '@/api/business/programming'

const props = defineProps({ modelValue: Boolean, question: Object, canReadAnswer: Boolean })
const emit = defineEmits(['update:modelValue'])
const visible = computed({ get: () => props.modelValue, set: value => emit('update:modelValue', value) })
const loading = ref(false)
const failed = ref(false)
const detail = ref(null)
const config = ref({})
const cases = ref([])
let requestId = 0
async function load() {
  const id = ++requestId
  loading.value = true
  failed.value = false
  detail.value = null
  config.value = {}
  cases.value = []
  try {
    const [question, programming] = await Promise.all([
      getQuestion(props.question.questionId),
      (props.canReadAnswer ? getProgrammingQuestion : previewProgrammingQuestion)(props.question.questionId),
    ])
    if (id !== requestId) return
    detail.value = question.data
    config.value = programming.data || {}
    cases.value = programming.testCases || []
  } catch (_) {
    if (id === requestId) failed.value = true
  } finally {
    if (id === requestId) loading.value = false
  }
}
watch(() => [props.modelValue, props.question?.questionId, props.canReadAnswer], ([opened, id]) => {
  if (opened && id) load()
  else ++requestId
})
</script>

<template>
  <el-dialog v-model="visible" title="Python 题目预览" width="min(920px, 94vw)" append-to-body destroy-on-close>
    <div v-loading="loading" class="python-preview">
      <div v-if="failed" role="alert">题目加载失败。<el-button link type="primary" @click="load">重新加载</el-button></div>
      <template v-else-if="detail">
        <h3>{{ config.title || detail.programmingTitle || 'Python 在线编程' }}</h3>
        <p>出题教师：{{ detail.nickName || question.nickName || '姓名未登记' }}</p>
        <h4>题目详情</h4><pre>{{ detail.questionContent }}</pre>
        <h4>输入说明</h4><pre>{{ config.inputDescription || '无' }}</pre>
        <h4>输出说明</h4><pre>{{ config.outputDescription || '无' }}</pre>
        <template v-if="config.constraintsText"><h4>限制条件</h4><pre>{{ config.constraintsText }}</pre></template>
        <el-tabs>
          <el-tab-pane label="学生起始代码"><pre>{{ config.starterCode || '未设置起始代码，学生从空白开始编写。' }}</pre></el-tab-pane>
          <el-tab-pane label="参考答案"><pre>{{ canReadAnswer ? (detail.answer || '尚未设置参考答案') : '仅题目创建者或管理员可查看参考答案。' }}</pre></el-tab-pane>
          <el-tab-pane label="测试输入与输出">
            <p v-if="!canReadAnswer">当前仅展示公开样例。</p>
            <el-empty v-if="!cases.length" description="暂无测试数据" />
            <article v-for="(item, index) in cases" :key="item.caseId || index" class="test-case">
              <h4>{{ item.caseName || `测试 ${index + 1}` }} · {{ String(item.isPublic) === '1' ? '公开样例' : '隐藏测试' }}</h4>
              <div class="case-grid"><section><strong>测试输入</strong><pre>{{ item.inputText || '（无输入）' }}</pre></section><section><strong>期望输出</strong><pre>{{ item.expectedOutput || '（空输出）' }}</pre></section></div>
            </article>
          </el-tab-pane>
        </el-tabs>
      </template>
    </div>
  </el-dialog>
</template>

<style scoped>
.python-preview { min-height: 180px; }
pre { white-space: pre-wrap; overflow-wrap: anywhere; line-height: 1.7; background: #f5f7fa; padding: 12px; border-radius: 6px; }
.case-grid { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); gap: 12px; }
.test-case { border-top: 1px solid #ebeef5; }
@media (max-width: 600px) {
  .case-grid { grid-template-columns: minmax(0, 1fr); }
  :deep(.el-tabs__item) { padding: 0 8px; font-size: 12px; }
}
</style>
