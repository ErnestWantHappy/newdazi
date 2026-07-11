<template>
  <el-card class="chart-card" shadow="hover" ref="cardRef" :class="{ 'is-fullscreen': isFullscreen }">
    <template #header>
      <div class="card-header">
        <span class="header-title">📊 课程成绩对比分析</span>
        <div class="chart-actions">
           <el-radio-group v-model="metric" size="small" style="margin-right: 15px;">
              <el-radio-button value="avg">平均分</el-radio-button>
              <el-radio-button value="pass">及格率</el-radio-button>
              <el-radio-button value="excellent">优秀率</el-radio-button>
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
  },
  selectedLessonIds: {
    type: Array,
    default: () => []
  }
});

const chartRef = ref(null);
const cardRef = ref(null);
let chartInstance = null;
const isFullscreen = ref(false);
const metric = ref('avg');
const originalHeight = '400px';

watch(() => props.data, () => {
  nextTick(() => renderChart());
}, { deep: true });

watch(metric, () => renderChart());

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

  const lessonStats = {};
  
  props.selectedLessonIds.forEach(id => {
      const lesson = props.lessonOptions.find(l => l.lessonId === id);
      const title = lesson ? lesson.lessonTitle : `课程${id}`;
      lessonStats[id] = {
          id,
          title,
          totalScore: 0,
          count: 0,
          passCount: 0,
          excellentCount: 0
      };
  });
  
  props.data.forEach(student => {
      if (!student.scores) return;
      
      student.scores.forEach(score => {
          if (lessonStats[score.lessonId]) {
              const s = lessonStats[score.lessonId];
              s.totalScore += (score.totalScore || 0);
              s.count += 1;
              if ((score.totalScore || 0) >= 60) s.passCount++;
              if ((score.totalScore || 0) >= 90) s.excellentCount++;
          }
      });
  });
  
  const xAxisData = [];
  const seriesData = [];
  
  Object.values(lessonStats).forEach(stat => {
      xAxisData.push(stat.title);
      if (stat.count === 0) {
          seriesData.push(0);
      } else {
          if (metric.value === 'avg') {
              seriesData.push((stat.totalScore / stat.count).toFixed(1));
          } else if (metric.value === 'pass') {
              seriesData.push(Math.round((stat.passCount / stat.count) * 100));
          } else if (metric.value === 'excellent') {
              seriesData.push(Math.round((stat.excellentCount / stat.count) * 100));
          }
      }
  });
  
  const metricConfig = {
      avg: { name: '平均分', color: ['#409EFF', '#66b1ff'], unit: '分' },
      pass: { name: '及格率', color: ['#67C23A', '#95d475'], unit: '%' },
      excellent: { name: '优秀率', color: ['#E6A23C', '#f3d19e'], unit: '%' }
  };
  
  const config = metricConfig[metric.value];
  
  const option = {
    tooltip: { 
        trigger: 'axis',
        formatter: (params) => {
            const val = params[0].value;
            const name = params[0].name;
            return `${name}<br/>${config.name}: <b>${val}</b> ${config.unit}`;
        }
    },
    xAxis: {
      type: 'category',
      data: xAxisData,
      axisLabel: { 
        interval: 0, 
        rotate: xAxisData.length > 5 ? 30 : 0,
        fontSize: isFullscreen.value ? 14 : 12
      }
    },
    yAxis: { 
      type: 'value', 
      name: config.name,
      nameTextStyle: { fontSize: isFullscreen.value ? 16 : 12 }
    },
    series: [{
      type: 'bar',
      data: seriesData,
      barMaxWidth: 50,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: config.color[0] },
          { offset: 1, color: config.color[1] }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      label: { 
        show: true, 
        position: 'top', 
        formatter: `{c}${config.unit === '%' ? '%' : ''}`,
        fontSize: isFullscreen.value ? 14 : 12
      }
    }],
    grid: { 
      left: '10%', 
      right: '10%', 
      bottom: isFullscreen.value ? '12%' : '15%', 
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
