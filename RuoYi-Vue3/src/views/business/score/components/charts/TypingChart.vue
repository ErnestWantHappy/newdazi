<template>
  <el-card class="chart-card" shadow="hover" ref="cardRef" :class="{ 'is-fullscreen': isFullscreen }">
    <template #header>
      <div class="card-header">
        <span class="header-title">⌨️ 打字数据分布</span>
        <div class="chart-actions">
            <el-select 
              v-model="internalLesson" 
              placeholder="全部课程" 
              clearable 
              size="small" 
              style="width: 160px;" 
            >
              <el-option label="全部课程" :value="null" />
              <el-option v-for="l in lessonOptions" :key="l.lessonId" :label="l.lessonTitle" :value="l.lessonId" />
            </el-select>
            
            <el-radio-group v-model="internalMetric" size="small">
              <el-radio-button label="speed">速度</el-radio-button>
              <el-radio-button label="accuracy">正确率</el-radio-button>
              <el-radio-button label="completion">完成率</el-radio-button>
              <el-radio-button label="score">得分</el-radio-button>
            </el-radio-group>
            
           <el-button 
            :icon="Back" 
            circle 
            v-if="isFullscreen"
            @click="toggleFullscreen"
            title="退出全屏"
          />
          <el-button 
            :icon="FullScreen" 
            circle 
            v-else
            @click="toggleFullscreen"
            title="全屏显示"
          />
        </div>
      </div>
    </template>
    <div ref="chartRef" class="chart-container"></div>
  </el-card>
</template>

<script setup>
import { ref, onMounted, watch, nextTick, onUnmounted } from 'vue';
import * as echarts from 'echarts';
import { FullScreen, Back } from '@element-plus/icons-vue';

const props = defineProps({
  data: {
    type: Array,
    required: true
  },
  lessonOptions: {
    type: Array,
    default: () => []
  }
});

const chartRef = ref(null);
const cardRef = ref(null);
let chartInstance = null;
const isFullscreen = ref(false);
const originalHeight = '400px';

const internalMetric = ref('speed');
const internalLesson = ref(null);

watch(() => props.data, () => {
    nextTick(() => renderChart());
}, { deep: true });

watch(internalMetric, () => renderChart());
watch(internalLesson, () => renderChart());

onMounted(() => {
  nextTick(() => {
    initChart();
    renderChart();
  });
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  disposeChart();
});

function initChart() {
  if (chartRef.value && !chartInstance) {
    chartInstance = echarts.init(chartRef.value);
  }
}

function handleResize() {
  if (isFullscreen.value && chartRef.value) {
    const headerEl = cardRef.value?.$el?.querySelector('.el-card__header');
    const headerHeight = headerEl ? headerEl.offsetHeight : 60;
    chartRef.value.style.height = (window.innerHeight - headerHeight - 40) + 'px';
  }
  chartInstance && chartInstance.resize();
}

function disposeChart() {
  if (chartInstance) {
    chartInstance.dispose();
    chartInstance = null;
  }
}

function renderChart() {
  if (!chartInstance) initChart();
  if (!chartInstance) return;

  let typingData = [];
  const metric = internalMetric.value;
  const selectedLesson = internalLesson.value;
  
  if (selectedLesson) {
    props.data.forEach(student => {
      const lessonScore = student.scores?.find(s => s.lessonId === selectedLesson);
      if (lessonScore && lessonScore.avgTypingSpeed) {
        typingData.push({
          name: student.studentName,
          studentNo: student.studentNo,
          speed: Number(lessonScore.avgTypingSpeed) || 0,
          accuracy: Number(lessonScore.avgAccuracyRate) || 0,
          completion: Number(lessonScore.avgCompletionRate) || 0,
          score: Number(lessonScore.typingScore) || 0
        });
      }
    });
  } else {
    props.data.forEach(student => {
      if (student.overallTypingSpeed) {
        typingData.push({
          name: student.studentName,
          studentNo: student.studentNo,
          speed: Number(student.overallTypingSpeed) || 0,
          accuracy: Number(student.overallAccuracy) || 0,
          completion: Number(student.overallCompletion) || 0,
          score: Number(student.avgTyping) || 0
        });
      }
    });
  }
  
  typingData.sort((a, b) => b[metric] - a[metric]);
  
  if (typingData.length === 0) {
    chartInstance.clear();
    chartInstance.setOption({
      title: { text: '暂无打字数据', left: 'center', top: 'center' },
      xAxis: { show: false },
      yAxis: { show: false },
      series: []
    }, true);
    return;
  }
  
  const names = typingData.map(s => s.name);
  const values = typingData.map(s => s[metric]);
  
  const metricConfig = {
    speed: { name: '打字速度', unit: '字/分', color: ['#E6A23C', '#F56C6C'] },
    accuracy: { name: '正确率', unit: '%', color: ['#67C23A', '#409EFF'] },
    completion: { name: '完成率', unit: '%', color: ['#409EFF', '#67C23A'] },
    score: { name: '得分', unit: '分', color: ['#F56C6C', '#E6A23C'] }
  };
  
  const config = metricConfig[metric];
  
  const option = {
    tooltip: { 
      trigger: 'axis',
      formatter: (params) => {
        const idx = params[0].dataIndex;
        const data = typingData[idx];
        return `<div style="font-weight:bold; margin-bottom:5px;">${data.name} (${data.studentNo}号)</div>` +
          `打字速度: ${data.speed} 字/分<br/>` +
          `正确率: ${data.accuracy}%<br/>` +
          `完成率: ${data.completion}%<br/>` +
          `得分: ${data.score} 分`;
      }
    },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: { rotate: 45, fontSize: isFullscreen.value ? 14 : 10 }
    },
    yAxis: { 
      type: 'value', 
      name: `${config.name}(${config.unit})`,
      nameTextStyle: { fontSize: isFullscreen.value ? 16 : 12 }
    },
    dataZoom: [
      {
        type: 'slider',
        show: names.length > 25,
        start: 0,
        end: names.length > 25 ? Math.min(100, 25 / names.length * 100) : 100,
        height: 20,
        bottom: isFullscreen.value ? 30 : 5
      }
    ],
    series: [{
      name: config.name,
      type: 'bar',
      data: values,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: config.color[0] },
          { offset: 1, color: config.color[1] }
        ])
      },
      label: { show: true, position: 'top', fontSize: isFullscreen.value ? 12 : 10 }
    }],
    grid: { 
      left: '8%', 
      right: '5%', 
      bottom: isFullscreen.value ? '15%' : '18%', 
      top: isFullscreen.value ? '10%' : '15%' 
    }
  };
  
  chartInstance.setOption(option, true);
}

function toggleFullscreen() {
    const cardEl = cardRef.value?.$el;
    if (!cardEl) return;
  
    if (!document.fullscreenElement) {
        cardEl.requestFullscreen().then(() => {
            isFullscreen.value = true;
            setTimeout(() => {
                const headerEl = cardEl.querySelector('.el-card__header');
                const headerHeight = headerEl ? headerEl.offsetHeight : 60;
                if (chartRef.value) {
                    chartRef.value.style.height = (window.innerHeight - headerHeight - 40) + 'px';
                }
                renderChart();
                chartInstance && chartInstance.resize();
            }, 300);
        });
    } else {
        document.exitFullscreen().then(() => {
            isFullscreen.value = false;
            if (chartRef.value) {
                chartRef.value.style.height = originalHeight;
            }
            setTimeout(() => {
                renderChart();
                chartInstance && chartInstance.resize();
            }, 100);
        });
    }
}

document.addEventListener('fullscreenchange', () => {
    if (!document.fullscreenElement && isFullscreen.value) {
        isFullscreen.value = false;
        if (chartRef.value) {
            chartRef.value.style.height = originalHeight;
        }
        setTimeout(() => {
            renderChart();
            chartInstance && chartInstance.resize();
        }, 100);
    }
});
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 400px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}
.header-title {
  flex-shrink: 0;
  font-weight: bold;
}
.chart-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: auto;
}

.is-fullscreen {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  width: 100vw !important;
  height: 100vh !important;
  z-index: 9999 !important;
  background-color: #fff !important;
  margin: 0 !important;
  border-radius: 0 !important;
}
</style>
