<template>
  <el-card class="performance-section-card" v-if="lessonId && classCode && entryYear">
    <template #header>
      <div class="card-header">
        <span style="font-weight: bold; font-size: 16px;">📋 课堂表现管理</span>
        <div class="header-right">
          <el-radio-group v-model="viewMode" size="small" style="margin-right: 15px;">
            <el-radio-button value="table">表格</el-radio-button>
            <el-radio-button value="chart">图表</el-radio-button>
          </el-radio-group>
          <el-button v-if="viewMode === 'table'" type="primary" size="small" @click="saveAll" :loading="saving">
            全部手动保存
          </el-button>
        </div>
      </div>
    </template>
    
    <!-- 表格视图 -->
    <div v-if="viewMode === 'table'" v-loading="loading">
      <div class="tip-text">课堂表现分范围 -10 ~ +10，正数为加分，负数为扣分。总分 = min(作业分 + 课堂表现分, 100)</div>
      <el-table class="performance-table" :data="tableData" border stripe max-height="350" style="width: 100%">
        <el-table-column prop="studentNo" label="学号" width="70" align="center" sortable :sort-method="naturalCodeCompare" />
        <el-table-column prop="studentName" label="姓名" width="100" align="center" />
        <el-table-column label="课堂表现分" width="160" align="center" class-name="performance-score-column">
          <template #default="scope">
            <PerformanceScoreStepper
              v-model="scope.row.score" 
              :min="-10" 
              :max="10" 
              :step="1"
              :disabled="scope.row.isAbsent"
              @change="handleDataChange(scope.row)"
              @blur="handleDataChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="原因" min-width="180">
          <template #default="scope">
            <el-input 
              v-model="scope.row.reason" 
              placeholder="请假/加减分原因"
              size="small"
              @blur="handleDataChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center" class-name="performance-status-column">
          <template #default="scope">
            <el-tag v-if="scope.row.saving" type="primary" size="small">保存中</el-tag>
            <el-tag v-else-if="scope.row.changed" type="warning" size="small">已修改</el-tag>
            <el-tag v-else-if="scope.row.performanceId" type="success" size="small">已保存</el-tag>
            <el-tag v-else type="info" size="small">-</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 图表视图 -->
    <div v-show="viewMode === 'chart'">
      <div ref="chartRef" class="performance-chart"></div>
      <el-empty v-if="viewMode === 'chart' && !hasChartData" description="暂无课堂表现分数据" />
    </div>
  </el-card>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { getPerformanceList, batchSavePerformance } from '@/api/business/classroomPerformance'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import PerformanceScoreStepper from '@/components/PerformanceScoreStepper/index.vue'

const props = defineProps({
  lessonId: { type: Number, default: null },
  classCode: { type: String, default: '' },
  entryYear: { type: String, default: '' }
})

const emit = defineEmits(['saved'])

const viewMode = ref('table')
const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const originalData = ref({}) // 保存原始数据用于对比
const chartRef = ref(null)
let chartInstance = null
const autoSaveTimers = new Map()

// 学号可能以字符串保存；按自然数值顺序比较，避免 1、10、2 的字典序。
function naturalCodeCompare(a, b) {
  return String(a ?? '').localeCompare(String(b ?? ''), 'zh-CN', { numeric: true, sensitivity: 'base' })
}

// 判断是否有图表数据
const hasChartData = computed(() => {
  return tableData.value.some(s => s.score !== 0 && s.score !== null)
})

// 加载数据
async function loadData() {
  if (!props.lessonId || !props.classCode || !props.entryYear) return
  
  loading.value = true
  try {
    const res = await getPerformanceList({
      lessonId: props.lessonId,
      classCode: props.classCode,
      entryYear: props.entryYear
    })
    tableData.value = (res.data || []).map(item => ({
      ...item,
      score: item.score,
      reason: item.reason || '',
      changed: false,
      saving: false
    }))
    // 保存原始数据
    originalData.value = {}
    tableData.value.forEach(item => {
      originalData.value[item.studentId] = { score: item.score, reason: item.reason }
    })
  } catch (e) {
    console.error('加载课堂表现数据失败', e)
  } finally {
    loading.value = false
  }
}

// 监控数据变化
watch(() => tableData.value, (newVal) => {
  newVal.forEach(item => {
    const orig = originalData.value[item.studentId]
    if (orig && !item.saving) {
      item.changed = item.score !== orig.score || item.reason !== orig.reason
    }
  })
}, { deep: true })

function hasRowChanged(row) {
  const orig = originalData.value[row.studentId] || { score: null, reason: '' }
  return row.score !== orig.score || (row.reason || '') !== (orig.reason || '')
}

// 失去焦点或数据变化时，防抖触发单个自动保存
function handleDataChange(row) {
  row.changed = hasRowChanged(row)
  if (!row.changed || row.saving) return

  const timer = autoSaveTimers.get(row.studentId)
  if (timer) clearTimeout(timer)

  autoSaveTimers.set(
    row.studentId,
    setTimeout(() => {
      autoSaveTimers.delete(row.studentId)
      saveSingleRow(row)
    }, 800)
  )
}

// 静默保存单条记录
async function saveSingleRow(row) {
  row.changed = hasRowChanged(row)
  if (!row.changed) return

  row.saving = true
  try {
    const res = await batchSavePerformance({
      lessonId: props.lessonId,
      performances: [{
        studentId: row.studentId,
        score: row.score,
        reason: row.reason
      }]
    })
    // 保存成功，更新基线原始数据
    originalData.value[row.studentId] = { score: row.score, reason: row.reason || '' }
    row.changed = false
    row.performanceId = res.data || row.performanceId || true
    emit('saved')
  } catch (e) {
    ElMessage.error('自动保存失败: ' + row.studentName)
  } finally {
    row.saving = false
  }
}

// 保存全部
async function saveAll() {
  const changedItems = tableData.value.filter(item => item.changed || item.score !== 0)
  if (changedItems.length === 0) {
    ElMessage.info('没有需要保存的数据')
    return
  }
  
  saving.value = true
  try {
    await batchSavePerformance({
      lessonId: props.lessonId,
      performances: changedItems.map(item => ({
        studentId: item.studentId,
        score: item.score,
        reason: item.reason
      }))
    })
    ElMessage.success('保存成功')
    emit('saved')
    await loadData() // 重新加载刷新状态
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 渲染图表
function renderChart() {
  if (!chartRef.value) return
  
  // 过滤有分数的学生
  const withScore = tableData.value.filter(s => s.score !== 0 && s.score !== null)
  if (withScore.length === 0) return
  
  // 按分数排序（从高到低）
  const sorted = [...withScore].sort((a, b) => b.score - a.score)
  
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  
  const option = {
    title: {
      text: '🏆 课堂表现分分布',
      left: 'center',
      textStyle: { fontSize: 14, color: '#606266' }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        const data = params[0]
        const student = sorted[sorted.length - 1 - data.dataIndex] // 因为反转了
        const score = student.score
        const prefix = score > 0 ? '+' : ''
        const reason = student.reason || '未填写原因'
        return `
          <div style="padding: 5px;">
            <b>${student.studentName}</b><br/>
            课堂表现分：<span style="color: ${score > 0 ? '#67C23A' : '#F56C6C'}; font-weight: bold;">${prefix}${score}</span><br/>
            <span style="color: #909399;">原因：${reason}</span>
          </div>
        `
      }
    },
    grid: {
      left: '12%',
      right: '12%',
      top: '50px',
      bottom: '10px'
    },
    xAxis: {
      type: 'value',
      min: -10,
      max: 10,
      axisLabel: {
        formatter: (val) => (val > 0 ? '+' : '') + val
      },
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    yAxis: {
      type: 'category',
      data: [...sorted].reverse().map(s => s.studentName),
      axisLabel: { fontSize: 12, color: '#333' }
    },
    series: [{
      type: 'bar',
      data: [...sorted].reverse().map(s => ({
        value: s.score,
        itemStyle: {
          color: s.score > 0 
            ? new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                { offset: 0, color: '#E8F5E9' },
                { offset: 1, color: '#67C23A' }
              ])
            : new echarts.graphic.LinearGradient(1, 0, 0, 0, [
                { offset: 0, color: '#FFEBEE' },
                { offset: 1, color: '#F56C6C' }
              ]),
          borderRadius: s.score > 0 ? [0, 4, 4, 0] : [4, 0, 0, 4]
        }
      })),
      barWidth: '60%',
      label: {
        show: true,
        position: 'outside',
        formatter: (params) => {
          const val = params.value
          return (val > 0 ? '+' : '') + val
        },
        color: '#666',
        fontWeight: 'bold'
      }
    }]
  }
  
  chartInstance.setOption(option, true)
}

// 监听视图切换
watch(viewMode, (mode) => {
  if (mode === 'chart') {
    nextTick(() => {
      renderChart()
      chartInstance?.resize()
    })
  }
})

// 监听数据变化重新渲染图表
watch(() => tableData.value, () => {
  if (viewMode.value === 'chart') {
    nextTick(renderChart)
  }
}, { deep: true })

// 监听参数变化重新加载
watch(() => [props.lessonId, props.classCode, props.entryYear], () => {
  loadData()
}, { immediate: true })

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  autoSaveTimers.forEach(timer => clearTimeout(timer))
  autoSaveTimers.clear()
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})

function handleResize() {
  chartInstance?.resize()
}
</script>

<style lang="scss" scoped>
.performance-section-card {
  margin-bottom: 15px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .header-right {
    display: flex;
    align-items: center;
  }
  
  .tip-text {
    color: #909399;
    font-size: 12px;
    margin-bottom: 10px;
  }
  
  .performance-chart {
    height: 350px;
    width: 100%;
  }

  :deep(.performance-table .el-table__body .el-table__row) {
    height: 42px;
  }

  :deep(.performance-table .el-table__body .el-table__cell) {
    padding: 4px 0;
  }

  :deep(.performance-table .performance-score-column .cell),
  :deep(.performance-table .performance-status-column .cell) {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 28px;
    height: 28px;
    line-height: 28px;
  }
}
</style>
