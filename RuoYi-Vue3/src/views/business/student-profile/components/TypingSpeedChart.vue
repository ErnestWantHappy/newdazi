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
  const speeds = props.data.map(d => d.typingSpeed || 0)
  const baseline = props.data[0]?.gradeBaseline || 40
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['打字速度', '年级基准'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: labels,
      axisLabel: {
        rotate: labels.length > 6 ? 30 : 0,
        interval: 0,
        formatter: (val) => val.length > 8 ? val.slice(0, 8) + '...' : val
      }
    },
    yAxis: {
      type: 'value',
      name: '字/分钟'
    },
    series: [
      {
        name: '打字速度',
        type: 'line',
        data: speeds,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: { color: '#11998e' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(17, 153, 142, 0.4)' },
            { offset: 1, color: 'rgba(17, 153, 142, 0.05)' }
          ])
        }
      },
      {
        name: '年级基准',
        type: 'line',
        data: speeds.map(() => baseline),
        lineStyle: { type: 'dashed', color: '#e74c3c' },
        symbol: 'none'
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
