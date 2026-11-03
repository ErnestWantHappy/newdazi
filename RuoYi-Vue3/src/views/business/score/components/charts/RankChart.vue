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
import { buildRankData } from './rankData';
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

  const sorted = buildRankData(props.data);
  const getCourseTotal = (student) => student.chartScore == null ? null : Math.round(student.chartScore);
  const names = sorted.map(s => s.studentName);
  
  const details = sorted.map((s) => ({
          total: getCourseTotal(s),
          theory: Math.round(s.avgTheory || 0),
          practical: s.avgPractical == null ? '待批改' : Math.round(s.avgPractical),
          typing: Math.round(s.avgTyping || 0),
          studentNo: s.studentNo,
          remark: s.remark,
          status: s.scoreStatus,
          name: s.studentName
  }));
  
  const scores = sorted.map(s => getCourseTotal(s) ?? 0);
  const escape = value => echarts.format.encodeHTML(String(value ?? ''));
  
  const option = {
    tooltip: { 
        trigger: 'axis',
        textStyle: {
            fontFamily: "'Microsoft YaHei', 'PingFang SC', sans-serif"
        },
        formatter: function(params) {
            const name = params[0].name;
            const score = params[0].value;
            const detail = details[params[0].dataIndex] || {};
            
            let html = `<div style="font-weight:bold; margin-bottom:5px;">${escape(name)} (${escape(detail.studentNo)}号)</div>`;
            if (detail.remark) {
               html += `<div style="color: #F56C6C; margin-bottom:5px;"><b>状态/备注：${escape(detail.remark)}</b></div>`;
            }
            html += `
                <div>总分：<b>${detail.total == null ? escape(detail.status || '未提交') : score}</b></div>
                <div>${escape(detail.status || '')}</div>
                <hr style="margin:5px 0; border:0; border-top:1px dashed #ccc;">
                <div>⌨️ 打字：${detail.typing}</div>
                <div>📝 理论：${detail.theory}</div>
                <div>🖥️ 操作：${detail.practical}</div>
            `;
            return html;
        }
    },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: { 
          interval: 0,
          rotate: 45, 
          fontSize: isFullscreen.value ? 14 : 11,
          fontFamily: '"Microsoft YaHei", "PingFang SC", "Helvetica Neue", Arial, sans-serif',
          color: (value, index) => {
              const detail = details[index] || {};
              // 备注学生改为橙色，避免红色过于刺眼或误导
              return detail.remark ? '#E6A23C' : '#1a1a1a';
          }
      }
    },
    yAxis: { 
      type: 'value', 
      name: '总分',
      nameTextStyle: { 
          fontSize: isFullscreen.value ? 16 : 12, 
          fontFamily: "'Microsoft YaHei', 'PingFang SC', sans-serif"
      }
    },
    dataZoom: [
      {
        type: 'slider',
        show: names.length > 25,
        start: 0,
        end: 100,
        height: 20,
        bottom: isFullscreen.value ? 30 : 5
      }
    ],
    series: [{
      type: 'bar',
      data: scores,
      itemStyle: {
        color: (params) => {
          const detail = details[params.dataIndex] || {};
          // 如果分数为 0 且存在备注，变灰
          if (detail.total == null || (params.value === 0 && detail.remark)) {
              return '#C0C4CC';
          }
          const colors = ['#F56C6C', '#E6A23C', '#67C23A'];
          if (params.dataIndex < 3) return colors[params.dataIndex];
          return '#409EFF';
        }
      },
      label: { 
        formatter: params => details[params.dataIndex]?.total == null
          ? (details[params.dataIndex]?.status || '未提交').split('').join('\n') : params.value,
        show: true, 
        position: 'top', 
        fontSize: isFullscreen.value ? 12 : 11,
        fontFamily: "'Microsoft YaHei', 'PingFang SC', sans-serif",
        color: '#000'
      }
    }],
    grid: { 
      left: '10%', 
      right: '5%', 
      bottom: isFullscreen.value ? '20%' : '26%', 
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
