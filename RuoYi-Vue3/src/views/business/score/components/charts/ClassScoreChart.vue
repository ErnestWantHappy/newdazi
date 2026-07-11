<template>
  <el-card class="chart-card" shadow="hover" ref="cardRef" :class="{ 'is-fullscreen': isFullscreen }">
    <template #header>
      <div class="card-header">
        <span class="header-title">📊 班级平均分对比</span>
        <div class="chart-actions">
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
  }
});

const chartRef = ref(null);
const cardRef = ref(null);
let chartInstance = null;
const isFullscreen = ref(false);
const originalHeight = '400px';

watch(() => props.data, () => {
  nextTick(() => renderChart());
}, { deep: true });

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

  const classMap = new Map();
  props.data.forEach(s => {
    const cls = s.className;
    if (!classMap.has(cls)) {
      classMap.set(cls, { total: 0, count: 0 });
    }
    classMap.get(cls).total += s.filteredTotal;
    classMap.get(cls).count += 1;
  });
  
  const classNames = [];
  const avgScores = [];
  
  Array.from(classMap.entries())
    .sort((a, b) => a[0] - b[0])
    .forEach(([cls, data]) => {
      classNames.push(cls + '班');
      avgScores.push((data.total / data.count).toFixed(1));
    });
  
  const option = {
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: classNames,
      axisLabel: { 
        rotate: 0,
        fontSize: isFullscreen.value ? 16 : 12
      }
    },
    yAxis: { 
      type: 'value', 
      name: '平均分',
      nameTextStyle: { fontSize: isFullscreen.value ? 16 : 12 }
    },
    series: [{
      type: 'bar',
      data: avgScores,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#409EFF' },
          { offset: 1, color: '#67C23A' }
        ])
      },
      label: { 
        show: true, 
        position: 'top',
        fontSize: isFullscreen.value ? 14 : 12
      }
    }],
    grid: { 
      left: '10%', 
      right: '10%', 
      bottom: isFullscreen.value ? '10%' : '15%', 
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
