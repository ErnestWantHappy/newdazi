<template>
  <div class="data-lab-container">
    <!-- 顶部状态与操作栏 -->
    <div class="lab-toolbar">
      <div class="toolbar-left">
        <el-tag type="warning" effect="dark" class="lab-badge">🧪 数据分析实验</el-tag>
        <span class="lab-title">{{ config?.experimentName || question?.question_content || '数据分析实验' }}</span>
        <el-tag v-if="config?.maxScore" type="info" size="small">满分: {{ config.maxScore }}分</el-tag>
        <span v-if="excelLoaded" class="dataset-tag">
          📊 数据集: <strong>{{ config?.excelFileName || '数据表格.xlsx' }}</strong>
        </span>
      </div>
      <div class="toolbar-right">
        <el-button v-if="config?.excelFileName" size="small" @click="downloadExcelFile">
          <el-icon><Download /></el-icon> 下载 Excel
        </el-button>
        <el-button size="small" :disabled="running || envLoading" @click="resetToStarter">
          重置代码
        </el-button>
        <el-button type="primary" :loading="running" :disabled="envLoading" @click="handleRun">
          ▶ 运行分析
        </el-button>
        <el-button type="success" :loading="submitting" :disabled="running || envLoading" @click="handleSubmit">
          📤 提交成果
        </el-button>
      </div>
    </div>

    <!-- Pyodide 环境加载提示条 -->
    <div v-if="envLoading" class="env-loading-bar">
      <el-progress :percentage="envProgress.percent" :status="envProgress.percent === 100 ? 'success' : ''" :stroke-width="14" striped striped-flow />
      <div class="env-tip">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>{{ envProgress.detail || '正在准备浏览器端 Python 数据分析引擎…' }}</span>
      </div>
    </div>

    <!-- 主工作区：左侧代码编辑，右侧结果与数据预览 -->
    <div class="lab-body">
      <splitpanes class="lab-split" @resized="onSplitResized">
        <!-- 左侧：代码编辑器 -->
        <pane :size="50" :min-size="30">
          <div class="editor-pane">
            <div class="pane-header">
              <span>&lt;/&gt; Python 3 实验代码</span>
              <span class="theme-toggle" @click="toggleTheme">{{ darkTheme ? '☀️ 浅色' : '🌙 深色' }}</span>
            </div>
            <div ref="editorRef" class="cm-editor-wrapper" />
          </div>
        </pane>

        <!-- 右侧：Tab 面板（运行结果、数据预览、实验说明、历史提交） -->
        <pane :size="50" :min-size="30">
          <div class="output-pane">
            <el-tabs v-model="activeTab" class="output-tabs">
              <!-- Tab 1: 运行结果（图表 + print 输出） -->
              <el-tab-pane label="📊 运行结果" name="result">
                <div class="result-container">
                  <!-- 图表展示区 -->
                  <div v-if="currentChart" class="chart-box">
                    <div class="box-title">生成的统计图表:</div>
                    <div class="chart-image-wrap">
                      <el-image :src="currentChart" :preview-src-list="[currentChart]" fit="contain" class="chart-img" />
                    </div>
                  </div>
                  <div v-else-if="!running && !runError" class="no-chart-tip">
                    <el-empty :image-size="64" description="暂无图表，运行包含 matplotlib 绘图的代码后将在此展示" />
                  </div>

                  <!-- 错误信息提示 -->
                  <div v-if="runError" class="error-box">
                    <div class="box-title">运行报错:</div>
                    <pre class="error-text">{{ runError }}</pre>
                  </div>

                  <!-- 文本输出区 -->
                  <div class="output-box">
                    <div class="box-title">控制台输出 (print):</div>
                    <pre class="stdout-text">{{ runOutput || '（暂无输出）' }}</pre>
                  </div>
                </div>
              </el-tab-pane>

              <!-- Tab 2: Excel 数据表格预览 -->
              <el-tab-pane label="📋 数据预览" name="preview">
                <div class="preview-container">
                  <div v-if="previewLoading" class="preview-loading">
                    <el-icon class="is-loading"><Loading /></el-icon> 正在读取 Excel 数据表…
                  </div>
                  <div v-else-if="previewData" class="preview-content">
                    <div class="table-info">
                      共 {{ previewData.totalRows }} 行，{{ previewData.totalCols }} 列（仅展示前 {{ previewData.rows.length }} 行）
                    </div>
                    <el-table :data="previewTableData" size="small" border stripe height="100%" class="data-table">
                      <el-table-column
                        v-for="(col, idx) in previewData.headers"
                        :key="idx"
                        :prop="String(idx)"
                        :label="col"
                        min-width="120"
                        show-overflow-tooltip
                      />
                    </el-table>
                  </div>
                  <div v-else class="preview-empty">
                    <el-empty description="暂未加载到 Excel 数据" />
                  </div>
                </div>
              </el-tab-pane>

              <!-- Tab 3: 实验说明 -->
              <el-tab-pane label="📝 任务说明" name="desc">
                <div class="desc-container">
                  <div class="desc-content">{{ config?.description || '请根据要求编写 Python 代码读取 Excel 数据并进行统计分析绘图。' }}</div>
                </div>
              </el-tab-pane>

              <!-- Tab 4: 我的提交记录 -->
              <el-tab-pane label="📑 提交记录" name="submission">
                <div class="submission-container">
                  <div v-if="mySubmission" class="sub-detail">
                    <div class="sub-meta">
                      <span>提交时间: {{ mySubmission.submittedAt || '-' }}</span>
                      <el-tag v-if="mySubmission.score !== null && mySubmission.score !== undefined" type="success">
                        得分: {{ mySubmission.score }} 分
                      </el-tag>
                      <el-tag v-else type="info">待教师评分</el-tag>
                    </div>
                    <div v-if="mySubmission.scoreComment" class="sub-comment">
                      <strong>教师评语:</strong> {{ mySubmission.scoreComment }}
                    </div>
                    <div v-if="mySubmission.chartImageUrl" class="sub-chart">
                      <div class="sub-label">已提交的图表:</div>
                      <DataLabChart :resource="mySubmission.chartImageUrl" />
                    </div>
                  </div>
                  <div v-else class="sub-empty">
                    <el-empty description="暂无提交记录，点击上方【提交成果】后记录将保存在这里" />
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
        </pane>
      </splitpanes>
    </div>
  </div>
</template>

<script setup>
import DataLabChart from './Chart.vue'
import { computed, nextTick, onActivated, onDeactivated, onMounted, onUnmounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Loading } from '@element-plus/icons-vue'
import { Pane, Splitpanes } from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'

import { autocompletion } from '@codemirror/autocomplete'
import { defaultKeymap, history as cmHistory, historyKeymap, indentWithTab } from '@codemirror/commands'
import { python } from '@codemirror/lang-python'
import { bracketMatching, defaultHighlightStyle, indentOnInput, syntaxHighlighting } from '@codemirror/language'
import { Compartment, EditorState } from '@codemirror/state'
import { oneDark } from '@codemirror/theme-one-dark'
import { EditorView, keymap, lineNumbers } from '@codemirror/view'

import {
  getDataLabExcelBlob,
  getStudentDataLabConfig,
  getStudentDataLabSubmission,
  submitStudentDataLab
} from '@/api/business/dataLab'
import { createPyodideRunner } from '@/utils/pyodideRunner'
const runner = createPyodideRunner()
const { initPyodide, loadExcelFile, previewExcel, runCode } = runner
let dataVersion = 0
let successfulCode = null
let suspended = false

const props = defineProps({
  question: {
    type: Object,
    required: true
  },
  sourceType: {
    type: String,
    default: 'PLAN'
  },
  sourceId: {
    type: [String, Number],
    default: null
  }
})

const emit = defineEmits(['submitted'])

// 状态定义
const config = ref(null)
const mySubmission = ref(null)
const envLoading = ref(false)
const envProgress = ref({ percent: 0, stage: '', detail: '' })
const excelLoaded = ref(false)
const running = ref(false)
const submitting = ref(false)
const activeTab = ref('result')
const darkTheme = ref(false)

const currentChart = ref(null)
const runOutput = ref('')
const runError = ref('')

const previewLoading = ref(false)
const previewData = ref(null)

const editorRef = ref(null)
let editorView = null
const themeCompartment = new Compartment()

// 将 previewData 格式化为 el-table 数据格式
const previewTableData = computed(() => {
  if (!previewData.value?.rows) return []
  return previewData.value.rows.map(row => {
    const obj = {}
    row.forEach((val, idx) => {
      obj[String(idx)] = val
    })
    return obj
  })
})

// 初始化 CodeMirror
function initEditor(initialCode = '') {
  if (editorView) {
    editorView.destroy()
    editorView = null
  }
  if (!editorRef.value) return

  const extensions = [
    lineNumbers(),
    cmHistory(),
    indentOnInput(),
    bracketMatching(),
    syntaxHighlighting(defaultHighlightStyle, { fallback: true }),
    python(),
    autocompletion(),
    keymap.of([...defaultKeymap, ...historyKeymap, indentWithTab]),
    themeCompartment.of(darkTheme.value ? oneDark : [])
  ]

  const state = EditorState.create({
    doc: initialCode,
    extensions
  })

  editorView = new EditorView({
    state,
    parent: editorRef.value
  })
}

function getCode() {
  return editorView ? editorView.state.doc.toString() : ''
}

function setCode(code) {
  if (!editorView) return
  editorView.dispatch({
    changes: { from: 0, to: editorView.state.doc.length, insert: code }
  })
}

function toggleTheme() {
  darkTheme.value = !darkTheme.value
  if (editorView) {
    editorView.dispatch({
      effects: themeCompartment.reconfigure(darkTheme.value ? oneDark : [])
    })
  }
}

function onSplitResized() {
  if (editorView) {
    editorView.requestMeasure()
  }
}

// 恢复为初始代码模板
function resetToStarter() {
  const starter = config.value?.starterCode || '# 编写你的 Python 数据分析代码\nimport pandas as pd\nimport matplotlib.pyplot as plt\n\n'
  setCode(starter)
  ElMessage.info('已重置为初始代码模板')
}

// 下载 Excel 源文件
async function downloadExcelFile() {
  if (!props.question?.question_id) return
  try {
    const res = await getDataLabExcelBlob(props.question.question_id, props.sourceId)
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = config.value?.excelFileName || 'dataset.xlsx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (err) {
    ElMessage.error('下载 Excel 文件失败: ' + (err.message || '网络异常'))
  }
}

// 加载题目配置与历史提交
async function loadQuestionData() {
  if (suspended) return
  const version = ++dataVersion
  runner.dispose()
  cachedExcelBuffer = null
  successfulCode = null
  running.value = false
  submitting.value = false
  config.value = null
  mySubmission.value = null
  const qId = props.question?.question_id
  if (!qId) return

  excelLoaded.value = false
  currentChart.value = null
  runOutput.value = ''
  runError.value = ''
  previewData.value = null

  try {
    // 1. 获取实验题配置
    const res = await getStudentDataLabConfig(qId, props.sourceId)
    if (version !== dataVersion) return
    if (res.code === 200 && res.data) {
      config.value = res.data
    } else {
      config.value = null
    }

    // 2. 获取历史提交
    const subRes = await getStudentDataLabSubmission({
      questionId: qId,
      sourceType: props.sourceType,
      sourceId: props.sourceId
    })
    if (version !== dataVersion) return
    if (subRes.code === 200 && subRes.data) {
      mySubmission.value = subRes.data
    } else {
      mySubmission.value = null
    }

    // 确定初始代码：有历史提交则用历史提交代码，否则用题目配置的初始代码
    const initialCode = mySubmission.value?.sourceCode || config.value?.starterCode || '# 编写你的 Python 数据分析代码\nimport pandas as pd\nimport matplotlib.pyplot as plt\n\n'
    await nextTick()
    if (version !== dataVersion) return
    initEditor(initialCode)

    await prepareEnvironment(version)
  } catch (e) {
    if (version !== dataVersion) return
    console.error('加载实验题目数据失败', e)
    ElMessage.error('加载实验配置失败')
  }
}

let cachedExcelBuffer = null

// 准备 Pyodide 环境及载入 Excel 文件
async function prepareEnvironment(version = dataVersion) {
  const qId = props.question?.question_id
  if (!qId) return

  envLoading.value = true
  try {
    // 步骤 A: 初始化 Pyodide（带进度反馈）
    await initPyodide((p) => {
      if (version === dataVersion) envProgress.value = p
    })
    if (version !== dataVersion) return

    // 步骤 B: 载入 Excel 二进制并写入虚拟文件系统
    if (config.value?.excelFileName) {
      envProgress.value = { percent: 92, stage: 'excel', detail: `正在载入数据集: ${config.value.excelFileName}…` }
      let arrayBuffer = cachedExcelBuffer
      if (!arrayBuffer) {
        const blob = await getDataLabExcelBlob(qId, props.sourceId)
        arrayBuffer = await blob.arrayBuffer()
        if (version !== dataVersion) return
        cachedExcelBuffer = arrayBuffer
      }
      await loadExcelFile(config.value.excelFileName, arrayBuffer)
      if (version !== dataVersion) return
      excelLoaded.value = true

      if (!previewData.value) {
        const fastData = await previewExcel(config.value.excelFileName)
        if (version !== dataVersion) return
        if (fastData) previewData.value = fastData
      }
    }

    envProgress.value = { percent: 100, stage: 'ready', detail: '环境与数据就绪' }
    envLoading.value = false
  } catch (err) {
    if (version !== dataVersion) return
    console.error('准备实验环境失败', err)
    envLoading.value = false
    ElMessage.error(`Python 实验环境初始化失败: ${err.message || '请检查网络连接'}`)
  }
}

// 预览 Excel 数据
async function generatePreview(fileName) {
  if (previewData.value) return
  previewLoading.value = true
  try {
    const data = await previewExcel(fileName)
    if (data) previewData.value = data
  } catch (e) {
    console.warn('生成 Excel 预览失败', e)
  } finally {
    previewLoading.value = false
  }
}

// 运行分析代码
async function handleRun() {
  const version = dataVersion
  successfulCode = null
  currentChart.value = null
  const code = getCode()
  if (!code.trim()) {
    ElMessage.warning('代码不能为空')
    return
  }

  running.value = true
  successfulCode = ''
  currentChart.value = null
  runError.value = ''
  runOutput.value = ''
  activeTab.value = 'result'

  try {
    const res = await runCode(code)
    if (version !== dataVersion) return
    runOutput.value = res.output
    if (res.error) {
      runError.value = res.error
      ElMessage.warning('代码运行出错，请查看错误详情')
    } else {
      successfulCode = code
      if (res.chartBase64) {
        currentChart.value = res.chartBase64
        ElMessage.success('运行成功，统计图表已生成！')
      } else {
        ElMessage.success('运行成功！')
      }
    }
  } catch (err) {
    if (version !== dataVersion) return
    runError.value = err.message || String(err)
    ElMessage.error('运行失败: ' + err.message)
  } finally {
    if (version === dataVersion) running.value = false
  }
}

// 提交成果
async function handleSubmit() {
  const version = dataVersion
  const code = getCode()
  if (running.value || !code.trim() || successfulCode !== code) {
    ElMessage.warning('请先编写并运行代码后再提交')
    return
  }

  submitting.value = true
  try {
    const res = await submitStudentDataLab({
      questionId: props.question.question_id,
      sourceType: props.sourceType,
      sourceId: props.sourceId,
      sourceCode: code,
      chartImageBase64: currentChart.value,
      outputText: runOutput.value
    })

    if (version !== dataVersion) return
    if (res.code === 200) {
      ElMessage.success('实验成果提交成功！')
      mySubmission.value = res.data
      emit('submitted', res.data)
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  } catch (e) {
    if (version !== dataVersion) return
    ElMessage.error('提交失败: ' + (e.message || '网络异常'))
  } finally {
    if (version === dataVersion) submitting.value = false
  }
}

watch(
  () => [props.question?.question_id, props.sourceType, props.sourceId],
  () => {
    loadQuestionData()
  }
)

function formatChartUrl(url) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) {
    return url
  }
  if (url.startsWith('/profile/')) {
    return import.meta.env.VITE_APP_BASE_API + '/common/resource/view?resource=' + encodeURIComponent(url)
  }
  return import.meta.env.VITE_APP_BASE_API + url
}

onMounted(() => {
  loadQuestionData()
})

function releaseEnvironment() {
  ++dataVersion
  runner.dispose()
  if (editorView) {
    editorView.destroy()
    editorView = null
  }
}
onUnmounted(releaseEnvironment)
onDeactivated(() => {
  suspended = true
  releaseEnvironment()
})
onActivated(() => {
  if (!suspended) return
  suspended = false
  loadQuestionData()
})
</script>

<style scoped>
.data-lab-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  background: #f8fafc;
  overflow: hidden;
}

.lab-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.lab-badge {
  font-size: 13px;
  font-weight: bold;
}

.lab-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}

.dataset-tag {
  font-size: 13px;
  color: #475569;
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 4px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.env-loading-bar {
  padding: 8px 16px;
  background: #eff6ff;
  border-bottom: 1px solid #bfdbfe;
}

.env-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #1d4ed8;
  display: flex;
  align-items: center;
  gap: 6px;
}

.lab-body {
  flex: 1;
  overflow: hidden;
  position: relative;
}

.lab-split {
  height: 100%;
  width: 100%;
}

.editor-pane {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #ffffff;
  border-right: 1px solid #e2e8f0;
}

.pane-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 14px;
  font-size: 13px;
  font-weight: 600;
  color: #334155;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.theme-toggle {
  font-size: 12px;
  cursor: pointer;
  color: #64748b;
  user-select: none;
}

.theme-toggle:hover {
  color: #0284c7;
}

.cm-editor-wrapper {
  flex: 1;
  overflow: auto;
  font-family: 'Fira Code', Consolas, Monaco, monospace;
  font-size: 14px;
}

.cm-editor-wrapper :deep(.cm-editor) {
  height: 100%;
}

.cm-editor-wrapper :deep(.cm-scroller) {
  overflow: auto;
}

.output-pane {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #ffffff;
}

.output-tabs {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.output-tabs :deep(.el-tabs__header) {
  margin: 0;
  padding: 0 12px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.output-tabs :deep(.el-tabs__content) {
  flex: 1;
  overflow: hidden;
  padding: 0;
}

.output-tabs :deep(.el-tab-pane) {
  height: 100%;
  overflow: auto;
}

.result-container {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
  overflow-y: auto;
  box-sizing: border-box;
}

.box-title {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 6px;
}

.chart-box {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px;
}

.chart-image-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f8fafc;
  border-radius: 6px;
  padding: 12px;
}

.chart-img {
  max-width: 100%;
  max-height: 420px;
  border-radius: 4px;
}

.error-box {
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  padding: 12px;
}

.error-text {
  margin: 0;
  font-family: Consolas, monospace;
  font-size: 12px;
  color: #dc2626;
  white-space: pre-wrap;
  word-break: break-all;
}

.output-box {
  background: #1e293b;
  border-radius: 8px;
  padding: 12px;
}

.output-box .box-title {
  color: #94a3b8;
}

.stdout-text {
  margin: 0;
  font-family: Consolas, Monaco, monospace;
  font-size: 13px;
  color: #f1f5f9;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 200px;
  overflow-y: auto;
}

.preview-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 12px;
  box-sizing: border-box;
}

.table-info {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 8px;
}

.data-table {
  flex: 1;
}

.desc-container {
  padding: 16px;
  font-size: 14px;
  line-height: 1.6;
  color: #334155;
  white-space: pre-wrap;
}

.submission-container {
  padding: 16px;
}

.sub-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #64748b;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e2e8f0;
}

.sub-comment {
  font-size: 13px;
  color: #0f172a;
  background: #f8fafc;
  border-left: 3px solid #3b82f6;
  padding: 8px 12px;
  margin-bottom: 12px;
}

.sub-label {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 6px;
}

.sub-chart-img {
  max-width: 100%;
  max-height: 300px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
}
</style>
