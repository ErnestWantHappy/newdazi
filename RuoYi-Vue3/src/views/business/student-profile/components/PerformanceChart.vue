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
  const values = props.data.map(d => d.performance || 0)
  
  const option = {
    tooltip: {
      trigger: 'axis'
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
        rotate: labels.length > 6 ? 30 : 0,
        interval: 0,
        formatter: (val) => val.length > 8 ? val.slice(0, 8) + '...' : val
      }
    },
    yAxis: {
      type: 'value',
      name: '分数'
    },
    series: [
      {
        name: '课堂表现分',
        type: 'line',
        data: values,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: { color: '#f39c12' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(243, 156, 18, 0.4)' },
            { offset: 1, color: 'rgba(243, 156, 18, 0.05)' }
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
