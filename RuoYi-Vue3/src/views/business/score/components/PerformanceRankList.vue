<template>
  <el-card class="performance-chart-card">
    <template #header>
      <div class="card-header">
        <span style="font-weight: bold; font-size: 16px;">🏆 课堂表现分排行榜</span>
        <el-radio-group v-model="viewMode" size="small">
          <el-radio-button label="positive">加分榜</el-radio-button>
          <el-radio-button label="negative">扣分榜</el-radio-button>
        </el-radio-group>
      </div>
    </template>
    
    <div v-if="displayList.length > 0" ref="chartRef" class="performance-chart"></div>
    <el-empty v-else description="暂无课堂表现分数据" />
  </el-card>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  limit: {
    type: Number,
    default: 15
  }
})

const viewMode = ref('positive')
const chartRef = ref(null)
let chartInstance = null

const displayList = computed(() => {
  // 过滤有课堂表现分的学生
  const withPerformance = props.data.filter(s => s.totalPerformance !== 0 && s.totalPerformance !== undefined)
  
  if (viewMode.value === 'positive') {
    // 加分榜：分数 > 0，按高到低排
    return withPerformance
      .filter(s => s.totalPerformance > 0)
      .sort((a, b) => b.totalPerformance - a.totalPerformance)
      .slice(0, props.limit)
  } else {
    // 扣分榜：分数 < 0，按低到高排（绝对值大的排前面）
    return withPerformance
      .filter(s => s.totalPerformance < 0)
      .sort((a, b) => a.totalPerformance - b.totalPerformance)
      .slice(0, props.limit)
  }
})

function renderChart() {
  if (!chartRef.value || displayList.value.length === 0) return
  
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  
  const isPositive = viewMode.value === 'positive'
  const list = displayList.value
  
  // 反转数组，让排名第一的在最上面
  const reversedList = [...list].reverse()
  
  const option = {
    title: {
      text: isPositive ? '🌟 加分榜 TOP' + list.length : '⚠️ 扣分榜 TOP' + list.length,
      left: 'center',
      textStyle: { fontSize: 14, color: '#606266' }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        const data = params[0]
        const student = reversedList[data.dataIndex]
        const score = student.totalPerformance
        const prefix = score > 0 ? '+' : ''
        return `
          <div style="padding: 5px;">
            <b>${student.studentName}</b> (${student.className}班)<br/>
            课堂表现分：<span style="color: ${isPositive ? '#67C23A' : '#F56C6C'}; font-weight: bold;">${prefix}${score}</span>
          </div>
        `
      }
    },
    grid: {
      left: '15%',
      right: '15%',
      top: '50px',
      bottom: '10px'
    },
    xAxis: {
      type: 'value',
      axisLabel: {
        formatter: (val) => (val > 0 ? '+' : '') + val
      },
      splitLine: { show: false }
    },
    yAxis: {
      type: 'category',
      data: reversedList.map(s => `${s.studentName}`),
      axisLabel: {
        fontSize: 12,
        color: '#333'
      }
    },
    series: [{
      type: 'bar',
      data: reversedList.map(s => ({
        value: s.totalPerformance,
        itemStyle: {
          color: isPositive 
            ? new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                { offset: 0, color: '#95F204' },
                { offset: 1, color: '#67C23A' }
              ])
            : new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                { offset: 0, color: '#F56C6C' },
                { offset: 1, color: '#FF9999' }
              ]),
          borderRadius: [0, 4, 4, 0]
        }
      })),
      barWidth: '60%',
      label: {
        show: true,
        position: isPositive ? 'right' : 'left',
        formatter: (params) => {
          const val = params.value
          return (val > 0 ? '+' : '') + val
        },
        color: isPositive ? '#67C23A' : '#F56C6C',
        fontWeight: 'bold'
      }
    }]
  }
  
  chartInstance.setOption(option, true)
}

watch([displayList, viewMode], () => {
  nextTick(renderChart)
}, { immediate: false })

watch(() => props.data, () => {
  nextTick(renderChart)
}, { deep: true })

onMounted(() => {
  nextTick(renderChart)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
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
.performance-chart-card {
  margin-top: 15px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .performance-chart {
    height: 350px;
    width: 100%;
  }
}
</style>
