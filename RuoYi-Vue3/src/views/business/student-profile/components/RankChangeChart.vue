<template>
  <div class="chart-container" ref="chartRef"></div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: { type: Array, default: () => [] }
})

const chartRef = ref(null)
let chart = null

function initChart() {
  if (!chartRef.value) return
  
  chart = echarts.init(chartRef.value)
  updateChart()
  
  window.addEventListener('resize', handleResize)
}

function handleResize() {
  chart?.resize()
}

function updateChart() {
  if (!chart || !props.data || props.data.length === 0) {
    chart?.clear()
    return
  }
  
  const labels = props.data.map(d => d.lessonTitle || '课程')
  const ranks = props.data.map(d => d.rank || 0)
  const classTotal = props.data[0]?.classTotal || 50
  
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const p = params[0]
        const item = props.data[p.dataIndex]
        return `${p.name}<br/>排名: 第${p.value}名 / ${item?.classTotal || classTotal}人`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: labels,
      axisLabel: {
        rotate: labels.length > 8 ? 30 : 0,
        interval: 0,
        formatter: (val) => val.length > 6 ? val.slice(0, 6) + '...' : val
      }
    },
    yAxis: {
      type: 'value',
      name: '排名',
      inverse: true, // Y轴倒序，排名低的在上面
      min: 1,
      max: classTotal
    },
    series: [
      {
        name: '班级排名',
        type: 'line',
        data: ranks,
        smooth: true,
        symbol: 'circle',
        symbolSize: 10,
        itemStyle: { color: '#9b59b6' },
        lineStyle: { width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(155, 89, 182, 0.05)' },
            { offset: 1, color: 'rgba(155, 89, 182, 0.3)' }
          ])
        }
      }
    ]
  }
  
  chart.setOption(option)
}

watch(() => props.data, updateChart, { deep: true })

onMounted(initChart)

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 280px;
}
</style>
