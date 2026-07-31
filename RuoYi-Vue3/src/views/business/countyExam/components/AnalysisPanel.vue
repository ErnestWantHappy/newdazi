<template>
  <section ref="panelRef" class="analysis-panel">
    <el-alert
      class="result-alert"
      :title="official ? '成绩已发布，以下为正式结果' : '成绩尚未发布，以下数据仅供审核预览'"
      :type="official ? 'success' : 'warning'"
      :closable="false"
      show-icon
    />

    <div class="overview-strip">
      <div class="overview-item">
        <span>参考学校</span>
        <strong>{{ numberValue(overview.schoolCount) }}</strong>
      </div>
      <div class="overview-item">
        <span>参考人数</span>
        <strong>{{ numberValue(overview.participantCount) }}</strong>
      </div>
      <div class="overview-item">
        <span>已提交</span>
        <strong>{{ numberValue(overview.submittedCount) }}</strong>
      </div>
      <div class="overview-item">
        <span>全场平均分</span>
        <strong>{{ decimalValue(overview.averageScore) }}</strong>
      </div>
      <div class="overview-item">
        <span>及格率</span>
        <strong>{{ decimalValue(overview.passRate) }}<small>%</small></strong>
      </div>
    </div>

    <div class="chart-grid">
      <section class="chart-section">
        <header class="chart-header">
          <div>
            <h3>学校平均分排名</h3>
            <p>按学校平均分由高到低</p>
          </div>
        </header>
        <div
          v-if="schools.length"
          ref="schoolChartRef"
          class="chart-canvas"
          role="img"
          aria-label="学校平均分排名图"
        />
        <el-empty v-else :image-size="72" description="暂无学校成绩" class="chart-empty" />
      </section>

      <section class="chart-section">
        <header class="chart-header">
          <div>
            <h3>全场分数分布</h3>
            <p>按百分制折算后的分数段</p>
          </div>
        </header>
        <div
          v-if="hasDistributionData"
          ref="distributionChartRef"
          class="chart-canvas"
          role="img"
          aria-label="全场分数分布图"
        />
        <el-empty v-else :image-size="72" description="暂无学生成绩" class="chart-empty" />
      </section>

      <section class="chart-section question-section">
        <header class="chart-header">
          <div>
            <h3>各题表现</h3>
            <p>选择、判断题显示正确率，打字、操作题显示平均得分率</p>
          </div>
        </header>
        <div
          v-if="questions.length"
          ref="questionChartRef"
          class="chart-canvas question-chart"
          role="img"
          aria-label="区域抽测各题表现图"
        />
        <el-empty v-else :image-size="72" description="暂无题目成绩" class="chart-empty" />
      </section>
    </div>
  </section>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import { questionTypeLabel } from '@/utils/questionType'

const props = defineProps({
  official: { type: Boolean, default: false },
  overview: { type: Object, default: () => ({}) },
  schools: { type: Array, default: () => [] },
  distribution: { type: Array, default: () => [] },
  questions: { type: Array, default: () => [] }
})

const panelRef = ref(null)
const schoolChartRef = ref(null)
const distributionChartRef = ref(null)
const questionChartRef = ref(null)

let schoolChart = null
let distributionChart = null
let questionChart = null
let resizeObserver = null

const hasDistributionData = computed(() =>
  props.distribution.some(item => Number(item.count || 0) > 0)
)

function numberValue(value) {
  const number = Number(value)
  return Number.isFinite(number) ? number : 0
}

function decimalValue(value) {
  const number = Number(value)
  return Number.isFinite(number) ? number.toFixed(1) : '0.0'
}

function questionTypeText(value) {
  return questionTypeLabel(value)
}

function shortText(value, maxLength = 18) {
  const text = String(value || '未命名题目').replace(/\s+/g, ' ').trim()
  return text.length > maxLength ? `${text.slice(0, maxLength)}...` : text
}

function encode(value) {
  return echarts.format.encodeHTML(String(value ?? ''))
}

function disposeCharts() {
  if (schoolChart) {
    schoolChart.dispose()
    schoolChart = null
  }
  if (distributionChart) {
    distributionChart.dispose()
    distributionChart = null
  }
  if (questionChart) {
    questionChart.dispose()
    questionChart = null
  }
}

function resizeCharts() {
  schoolChart?.resize()
  distributionChart?.resize()
  questionChart?.resize()
}

function renderSchoolChart() {
  if (!schoolChartRef.value || !props.schools.length) return
  const rows = [...props.schools].sort((left, right) => Number(right.avgScore || 0) - Number(left.avgScore || 0))
  const names = rows.map((item, index) => `${index + 1}. ${item.deptName || '未命名学校'}`)
  schoolChart = echarts.init(schoolChartRef.value)
  schoolChart.setOption({
    animationDuration: 350,
    grid: { left: 18, right: 58, top: 8, bottom: rows.length > 10 ? 42 : 18, containLabel: true },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter(params) {
        const index = params[0]?.dataIndex || 0
        const row = rows[index] || {}
        return `<strong>${encode(row.deptName || '-')}</strong><br>`
          + `平均分：${decimalValue(row.avgScore)}<br>`
          + `参考人数：${numberValue(row.studentCount)}<br>`
          + `及格率：${decimalValue(row.passRate)}%`
      }
    },
    xAxis: {
      type: 'value',
      max: Number(props.overview.fullScore || 100),
      axisLabel: { color: '#6b7280' },
      splitLine: { lineStyle: { color: '#e5e7eb' } }
    },
    yAxis: {
      type: 'category',
      inverse: true,
      data: names,
      axisTick: { show: false },
      axisLine: { show: false },
      axisLabel: { color: '#374151', width: 150, overflow: 'truncate' }
    },
    dataZoom: rows.length > 10
      ? [{ type: 'slider', yAxisIndex: 0, right: 2, width: 12, startValue: 0, endValue: 9 }]
      : [],
    series: [{
      name: '平均分',
      type: 'bar',
      data: rows.map(item => Number(item.avgScore || 0)),
      barMaxWidth: 18,
      itemStyle: { color: '#2f7d68', borderRadius: [0, 3, 3, 0] },
      label: { show: true, position: 'right', color: '#374151', formatter: ({ value }) => decimalValue(value) }
    }]
  })
}

function renderDistributionChart() {
  if (!distributionChartRef.value || !hasDistributionData.value) return
  distributionChart = echarts.init(distributionChartRef.value)
  distributionChart.setOption({
    animationDuration: 350,
    grid: { left: 16, right: 18, top: 8, bottom: 18, containLabel: true },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter(params) {
        return `${encode(params[0]?.axisValue || '')} 百分制分：${numberValue(params[0]?.value)} 人`
      }
    },
    xAxis: {
      type: 'category',
      data: props.distribution.map(item => item.label),
      axisTick: { alignWithLabel: true },
      axisLabel: { color: '#6b7280', interval: 0, rotate: 30 }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: '#6b7280' },
      splitLine: { lineStyle: { color: '#e5e7eb' } }
    },
    series: [{
      name: '人数',
      type: 'bar',
      data: props.distribution.map(item => Number(item.count || 0)),
      barMaxWidth: 24,
      itemStyle: { color: '#456d9b', borderRadius: [3, 3, 0, 0] }
    }]
  })
}

function renderQuestionChart() {
  if (!questionChartRef.value || !props.questions.length) return
  const rows = props.questions.map((item, index) => {
    const objective = ['choice', 'judgment'].includes(item.questionType)
    return {
      ...item,
      metricLabel: objective ? '正确率' : '平均得分率',
      metricValue: Number(objective ? item.correctRate : item.scoreRate) || 0,
      axisLabel: `${index + 1}. [${questionTypeText(item.questionType)}] ${shortText(item.questionContent)}`
    }
  })
  const typeColors = { choice: '#2f7d68', judgment: '#4f7f52', typing: '#456d9b', practical: '#b7791f' }
  questionChart = echarts.init(questionChartRef.value)
  questionChart.setOption({
    animationDuration: 350,
    grid: { left: 20, right: 64, top: 8, bottom: rows.length > 10 ? 42 : 18, containLabel: true },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter(params) {
        const row = rows[params[0]?.dataIndex || 0] || {}
        return `<strong>${encode(row.questionContent || '-')}</strong><br>`
          + `题型：${encode(questionTypeText(row.questionType))}<br>`
          + `${row.metricLabel}：${decimalValue(row.metricValue)}%<br>`
          + `平均分：${decimalValue(row.avgScore)} / ${numberValue(row.questionScore)}`
      }
    },
    xAxis: {
      type: 'value',
      min: 0,
      max: 100,
      axisLabel: { color: '#6b7280', formatter: '{value}%' },
      splitLine: { lineStyle: { color: '#e5e7eb' } }
    },
    yAxis: {
      type: 'category',
      inverse: true,
      data: rows.map(item => item.axisLabel),
      axisTick: { show: false },
      axisLine: { show: false },
      axisLabel: { color: '#374151', width: 260, overflow: 'truncate' }
    },
    dataZoom: rows.length > 10
      ? [{ type: 'slider', yAxisIndex: 0, right: 2, width: 12, startValue: 0, endValue: 9 }]
      : [],
    series: [{
      name: '题目表现',
      type: 'bar',
      data: rows.map(item => ({
        value: item.metricValue,
        itemStyle: { color: typeColors[item.questionType] || '#6b7280' }
      })),
      barMaxWidth: 18,
      label: { show: true, position: 'right', color: '#374151', formatter: ({ value }) => `${decimalValue(value)}%` }
    }]
  })
}

function renderCharts() {
  disposeCharts()
  nextTick(() => {
    renderSchoolChart()
    renderDistributionChart()
    renderQuestionChart()
  })
}

watch(
  () => [props.schools, props.distribution, props.questions, props.overview],
  renderCharts,
  { deep: true, immediate: true }
)

onMounted(() => {
  if (window.ResizeObserver && panelRef.value) {
    resizeObserver = new window.ResizeObserver(resizeCharts)
    resizeObserver.observe(panelRef.value)
  } else {
    window.addEventListener('resize', resizeCharts)
  }
  renderCharts()
})

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  window.removeEventListener('resize', resizeCharts)
  disposeCharts()
})
</script>

<style scoped>
.analysis-panel {
  --analysis-border: #e5e7eb;
  --analysis-muted: #6b7280;
  margin-bottom: 18px;
}

.result-alert {
  margin-bottom: 12px;
}

.overview-strip {
  display: grid;
  grid-template-columns: repeat(5, minmax(110px, 1fr));
  border: 1px solid var(--analysis-border);
  border-radius: 6px;
  background: #fff;
  margin-bottom: 14px;
}

.overview-item {
  min-width: 0;
  padding: 13px 16px;
  border-right: 1px solid var(--analysis-border);
}

.overview-item:last-child {
  border-right: 0;
}

.overview-item span {
  display: block;
  color: var(--analysis-muted);
  font-size: 12px;
  margin-bottom: 5px;
}

.overview-item strong {
  color: #1f2937;
  font-size: 22px;
  font-weight: 650;
  line-height: 1;
}

.overview-item small {
  color: var(--analysis-muted);
  font-size: 12px;
  font-weight: 500;
  margin-left: 2px;
}

.chart-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(340px, 0.85fr);
  gap: 12px;
}

.chart-section {
  min-width: 0;
  border: 1px solid var(--analysis-border);
  border-radius: 6px;
  background: #fff;
  padding: 14px;
}

.question-section {
  grid-column: 1 / -1;
}

.chart-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  min-height: 42px;
  margin-bottom: 6px;
}

.chart-header h3 {
  margin: 0 0 4px;
  color: #1f2937;
  font-size: 15px;
  font-weight: 650;
}

.chart-header p {
  margin: 0;
  color: var(--analysis-muted);
  font-size: 12px;
}

.chart-canvas,
.chart-empty {
  width: 100%;
  height: 330px;
}

.question-chart {
  height: 360px;
}

@media (max-width: 1180px) {
  .overview-strip {
    grid-template-columns: repeat(3, minmax(110px, 1fr));
  }

  .overview-item:nth-child(3) {
    border-right: 0;
  }

  .overview-item:nth-child(n + 4) {
    border-top: 1px solid var(--analysis-border);
  }

  .chart-grid {
    grid-template-columns: 1fr;
  }

  .question-section {
    grid-column: auto;
  }
}

@media (max-width: 720px) {
  .overview-strip {
    grid-template-columns: 1fr 1fr;
  }

  .overview-item,
  .overview-item:nth-child(3) {
    border-right: 1px solid var(--analysis-border);
    border-top: 1px solid var(--analysis-border);
  }

  .overview-item:nth-child(odd) {
    border-right: 1px solid var(--analysis-border);
  }

  .overview-item:nth-child(even) {
    border-right: 0;
  }

  .overview-item:first-child,
  .overview-item:nth-child(2) {
    border-top: 0;
  }
}
</style>
