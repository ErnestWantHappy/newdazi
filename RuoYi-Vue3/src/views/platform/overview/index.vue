<template>
  <div class="overview-page">
    <section class="overview-hero">
      <div class="hero-copy">
        <div class="eyebrow">信息科技学业测评平台</div>
        <h1>平台概览</h1>
        <p>面向教研管理、区域抽测、日常测评和平台运行的综合态势看板。</p>
      </div>
      <div class="hero-status">
        <div class="status-pill" :class="healthClass">
          <span class="dot"></span>
          {{ overview.systemHealth?.title || '正在读取平台状态' }}
        </div>
        <el-button :icon="Refresh" round @click="loadData">刷新数据</el-button>
      </div>
    </section>

    <el-row :gutter="16" class="metric-grid">
      <el-col v-for="item in metrics" :key="item.key" :xs="12" :sm="8" :md="6" :lg="6" :xl="3">
        <div class="metric-card" :class="`tone-${item.tone}`">
          <div class="metric-label">{{ item.label }}</div>
          <div class="metric-value">
            <span>{{ formatNumber(item.value) }}</span>
            <em>{{ item.unit }}</em>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-grid">
      <el-col :xs="24" :lg="14">
        <section class="panel panel-large">
          <div class="panel-head">
            <div>
              <h2>测评活跃趋势</h2>
              <span>近 6 个月学生作答人次与试卷汇总均分</span>
            </div>
          </div>
          <div ref="trendChartRef" class="chart chart-tall"></div>
        </section>
      </el-col>
      <el-col :xs="24" :lg="10">
        <section class="panel panel-large">
          <div class="panel-head">
            <div>
              <h2>题型结构</h2>
              <span>题库资源构成</span>
            </div>
          </div>
          <div ref="questionChartRef" class="chart chart-tall"></div>
        </section>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="content-grid">
      <el-col :xs="24" :lg="15">
        <section class="panel">
          <div class="panel-head">
            <div>
              <h2>近期区域抽测</h2>
              <span>教研员最常关注的抽测状态</span>
            </div>
          </div>
          <el-table :data="overview.recentCountyExams || []" height="320" empty-text="暂无区域抽测">
            <el-table-column prop="exam_name" label="抽测名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="exam_grade" label="年级" width="80">
              <template #default="{ row }">{{ formatGrade(row.exam_grade) }}</template>
            </el-table-column>
            <el-table-column prop="duration_minutes" label="作答时长" width="100">
              <template #default="{ row }">{{ row.duration_minutes || 40 }} 分钟</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusTag(row.status)" effect="light">{{ formatExamStatus(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="create_time" label="创建时间" width="170" />
          </el-table>
        </section>
      </el-col>

      <el-col :xs="24" :lg="9">
        <section class="panel">
          <div class="panel-head">
            <div>
              <h2>区域抽测状态</h2>
              <span>草稿、开启、关闭、发布分布</span>
            </div>
          </div>
          <div ref="examStatusChartRef" class="chart chart-mid"></div>
        </section>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="content-grid">
      <el-col :xs="24" :lg="14">
        <section class="panel">
          <div class="panel-head">
            <div>
              <h2>近 90 天学校活跃</h2>
              <span>按作答学生数排序</span>
            </div>
          </div>
          <el-table :data="overview.topSchools || []" height="300" empty-text="暂无作答数据">
            <el-table-column type="index" label="#" width="56" />
            <el-table-column prop="dept_name" label="学校" min-width="180" show-overflow-tooltip />
            <el-table-column prop="active_student_count" label="活跃学生" width="110" />
            <el-table-column prop="avg_score" label="平均分" width="100">
              <template #default="{ row }">{{ row.avg_score || '-' }}</template>
            </el-table-column>
          </el-table>
        </section>
      </el-col>
      <el-col :xs="24" :lg="10">
        <section class="panel intro-panel">
          <div class="panel-head">
            <div>
              <h2>平台简介</h2>
              <span>课题申报截图可直接使用的核心说明</span>
            </div>
          </div>
          <div class="intro-list">
            <div v-for="item in overview.intro || []" :key="item.title" class="intro-item">
              <strong>{{ item.title }}</strong>
              <p>{{ item.content }}</p>
            </div>
          </div>
        </section>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="PlatformOverview">
import * as echarts from 'echarts'
import { Refresh } from '@element-plus/icons-vue'
import { getPlatformOverview } from '@/api/business/platformOverview'

const overview = ref({})
const trendChartRef = ref(null)
const questionChartRef = ref(null)
const examStatusChartRef = ref(null)
const chartInstances = []
const loading = ref(false)

const metrics = computed(() => overview.value.metrics || [])
const healthClass = computed(() => overview.value.systemHealth?.level === 'warning' ? 'is-warning' : 'is-stable')

function loadData() {
  loading.value = true
  getPlatformOverview().then(res => {
    overview.value = res.data || {}
    nextTick(renderCharts)
  }).finally(() => {
    loading.value = false
  })
}

function renderCharts() {
  disposeCharts()
  renderTrendChart()
  renderQuestionChart()
  renderExamStatusChart()
}

function renderTrendChart() {
  const chart = echarts.init(trendChartRef.value)
  chartInstances.push(chart)
  const answers = overview.value.answerTrend || []
  const scores = overview.value.scoreTrend || []
  const labels = Array.from(new Set([...answers.map(i => i.name), ...scores.map(i => i.name)]))
  chart.setOption({
    color: ['#1f9d8a', '#d07a2d'],
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 42, top: 48, bottom: 36 },
    legend: { top: 4, right: 8, data: ['作答人次', '试卷均分'] },
    xAxis: { type: 'category', data: labels, axisTick: { show: false } },
    yAxis: [
      { type: 'value', name: '人次', splitLine: { lineStyle: { color: '#e8edf2' } } },
      { type: 'value', name: '分数', min: 0, max: 100 }
    ],
    series: [
      { name: '作答人次', type: 'bar', barWidth: 18, data: alignSeries(labels, answers), itemStyle: { borderRadius: [6, 6, 0, 0] } },
      { name: '试卷均分', type: 'line', yAxisIndex: 1, smooth: true, symbolSize: 8, data: alignSeries(labels, scores) }
    ]
  })
}

function renderQuestionChart() {
  const chart = echarts.init(questionChartRef.value)
  chartInstances.push(chart)
  chart.setOption({
    color: ['#1f9d8a', '#416f91', '#d07a2d', '#b85b52', '#6b7b4f'],
    tooltip: { trigger: 'item' },
    legend: { bottom: 6, itemWidth: 10, itemHeight: 10 },
    series: [{
      name: '题型',
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '45%'],
      data: (overview.value.questionDistribution || []).map(item => ({
        name: formatQuestionType(item.name),
        value: item.value
      })),
      label: { formatter: '{b}\n{d}%' }
    }]
  })
}

function renderExamStatusChart() {
  const chart = echarts.init(examStatusChartRef.value)
  chartInstances.push(chart)
  chart.setOption({
    color: ['#8a9a5b', '#1f9d8a', '#d07a2d', '#416f91'],
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['38%', '68%'],
      center: ['50%', '48%'],
      data: (overview.value.countyExamStatus || []).map(item => ({
        name: formatExamStatus(item.name),
        value: item.value
      })),
      label: { formatter: '{b}: {c}' }
    }]
  })
}

function alignSeries(labels, rows) {
  return labels.map(label => {
    const row = rows.find(item => item.name === label)
    return row ? row.value : 0
  })
}

function disposeCharts() {
  while (chartInstances.length) {
    chartInstances.pop().dispose()
  }
}

function handleResize() {
  chartInstances.forEach(chart => chart.resize())
}

function formatNumber(value) {
  return Number(value || 0).toLocaleString()
}

function formatQuestionType(type) {
  const map = { choice: '选择题', judgment: '判断题', typing: '打字题', practical: '操作题' }
  return map[type] || type || '未分类'
}

function formatExamStatus(status) {
  const map = { draft: '草稿', open: '开启', closed: '关闭', published: '已发布' }
  return map[status] || status || '-'
}

function statusTag(status) {
  const map = { draft: 'info', open: 'success', closed: 'warning', published: 'primary' }
  return map[status] || 'info'
}

function formatGrade(grade) {
  return grade ? `${grade}年级` : '-'
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  disposeCharts()
})
</script>

<style scoped lang="scss">
.overview-page {
  min-height: calc(100vh - 84px);
  padding: 22px;
  background:
    linear-gradient(135deg, rgba(31, 157, 138, 0.08), transparent 28%),
    linear-gradient(45deg, rgba(208, 122, 45, 0.08), transparent 34%),
    #f5f7f9;
  color: #24313f;
}

.overview-hero {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: flex-end;
  padding: 28px 30px;
  border: 1px solid rgba(36, 49, 63, 0.08);
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 18px 45px rgba(32, 45, 58, 0.08);
}

.hero-copy h1 {
  margin: 8px 0 10px;
  font-size: 36px;
  font-weight: 800;
  letter-spacing: 0;
}

.hero-copy p {
  max-width: 680px;
  margin: 0;
  color: #647080;
  font-size: 15px;
}

.eyebrow {
  color: #1f9d8a;
  font-size: 13px;
  font-weight: 700;
}

.hero-status {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 36px;
  padding: 0 14px;
  border-radius: 18px;
  font-weight: 700;
  background: #eef8f5;
  color: #167867;
}

.status-pill.is-warning {
  background: #fff3df;
  color: #9a5a12;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
}

.metric-grid,
.chart-grid,
.content-grid {
  margin-top: 16px;
}

.metric-card,
.panel {
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid rgba(36, 49, 63, 0.08);
  box-shadow: 0 14px 34px rgba(32, 45, 58, 0.07);
}

.metric-card {
  min-height: 126px;
  padding: 18px;
  overflow: hidden;
  position: relative;
}

.metric-card::after {
  content: "";
  position: absolute;
  right: 14px;
  bottom: 14px;
  width: 52px;
  height: 5px;
  border-radius: 5px;
  background: currentColor;
  opacity: 0.24;
}

.metric-label {
  color: #667386;
  font-weight: 700;
}

.metric-value {
  margin-top: 20px;
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.metric-value span {
  font-size: 30px;
  font-weight: 800;
}

.metric-value em {
  font-style: normal;
  color: #7b8794;
}

.tone-teal { color: #1f9d8a; }
.tone-gold { color: #c68a22; }
.tone-blue { color: #416f91; }
.tone-coral { color: #b85b52; }
.tone-green { color: #6b7b4f; }
.tone-red { color: #a2473d; }
.tone-cyan { color: #248b9b; }
.tone-slate { color: #52616f; }

.panel {
  padding: 18px;
  min-height: 360px;
}

.panel-large {
  min-height: 420px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.panel-head h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 800;
  letter-spacing: 0;
}

.panel-head span {
  display: block;
  margin-top: 5px;
  color: #7a8795;
  font-size: 13px;
}

.chart {
  width: 100%;
}

.chart-tall {
  height: 335px;
}

.chart-mid {
  height: 278px;
}

.intro-list {
  display: grid;
  gap: 12px;
}

.intro-item {
  padding: 15px 16px;
  border-left: 4px solid #1f9d8a;
  border-radius: 6px;
  background: #f7faf9;
}

.intro-item strong {
  font-size: 15px;
}

.intro-item p {
  margin: 8px 0 0;
  color: #647080;
  line-height: 1.7;
}

@media (max-width: 900px) {
  .overview-page {
    padding: 14px;
  }

  .overview-hero {
    align-items: flex-start;
    flex-direction: column;
    padding: 22px;
  }

  .hero-copy h1 {
    font-size: 30px;
  }
}
</style>
