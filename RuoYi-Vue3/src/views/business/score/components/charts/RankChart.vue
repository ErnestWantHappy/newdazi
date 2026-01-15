<template>
  <el-card class="chart-card" shadow="hover" ref="cardRef" :class="{ 'is-fullscreen': isFullscreen }">
    <template #header>
      <div class="card-header">
        <span class="header-title">📈 成绩分布（按总分排名）</span>
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

// 保存原始高度
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
    // 全屏时重新计算高度
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

  const sorted = [...props.data].sort((a, b) => b.filteredTotal - a.filteredTotal);
  const names = sorted.map(s => s.studentName);
  
  const detailMap = {};
  sorted.forEach((s) => {
      detailMap[s.studentName] = {
          total: Math.round(s.filteredTotal || 0),
          theory: Math.round(s.avgTheory || 0),
          practical: Math.round(s.avgPractical || 0),
          typing: Math.round(s.avgTyping || 0),
          studentNo: s.studentNo
      };
  });
  
  const scores = sorted.map(s => Math.round(s.filteredTotal || 0));
  
  const option = {
    tooltip: { 
        trigger: 'axis',
        formatter: function(params) {
            const name = params[0].name;
            const score = params[0].value;
            const detail = detailMap[name] || {};
            return `
                <div style="font-weight:bold; margin-bottom:5px;">${name} (${detail.studentNo}号)</div>
                <div>总分：<b>${score}</b></div>
                <hr style="margin:5px 0; border:0; border-top:1px dashed #ccc;">
                <div>⌨️ 打字：${detail.typing}</div>
                <div>📝 理论：${detail.theory}</div>
                <div>🖥️ 操作：${detail.practical}</div>
            `;
        }
    },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: { 
          rotate: 45, 
          fontSize: isFullscreen.value ? 14 : 10
      }
    },
    yAxis: { 
      type: 'value', 
      name: '总分',
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
      type: 'bar',
      data: scores,
      itemStyle: {
        color: (params) => {
          const colors = ['#F56C6C', '#E6A23C', '#67C23A'];
          if (params.dataIndex < 3) return colors[params.dataIndex];
          return '#409EFF';
        }
      },
      label: { show: true, position: 'top', fontSize: isFullscreen.value ? 12 : 10 }
    }],
    grid: { 
      left: '10%', 
      right: '5%', 
      bottom: isFullscreen.value ? '15%' : '20%', 
      top: isFullscreen.value ? '10%' : '15%' 
    }
  };
  
  chartInstance.setOption(option, true);
}

// 最简单粗暴的全屏方案：直接用JS硬算高度
function toggleFullscreen() {
    const cardEl = cardRef.value?.$el;
    if (!cardEl) return;
  
    if (!document.fullscreenElement) {
        // 进入全屏
        cardEl.requestFullscreen().then(() => {
            isFullscreen.value = true;
            
            // 等待全屏生效后，用JS直接算高度
            setTimeout(() => {
                const headerEl = cardEl.querySelector('.el-card__header');
                const headerHeight = headerEl ? headerEl.offsetHeight : 60;
                
                // 直接设置像素高度，不依赖任何CSS布局
                if (chartRef.value) {
                    chartRef.value.style.height = (window.innerHeight - headerHeight - 40) + 'px';
                }
                
                // 重新渲染图表
                renderChart();
                chartInstance && chartInstance.resize();
            }, 300);
        });
    } else {
        // 退出全屏
        document.exitFullscreen().then(() => {
            isFullscreen.value = false;
            
            // 恢复原始高度
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

// 监听ESC退出全屏
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

/* 全屏时的基础样式 */
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
