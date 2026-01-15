<template>
  <el-card class="rank-list-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <div class="header-left">
          <span class="title" :class="type">{{ title }}</span>
          <el-tag :type="type === 'top' ? 'danger' : 'warning'" size="small" style="margin-left: 10px">
            {{ type === 'top' ? '年级前' : '年级后' }} {{ displayLimit }} 名
          </el-tag>
        </div>
        <div class="header-right">
          <el-checkbox v-if="type === 'bottom'" v-model="excludeZero" size="small" style="margin-right: 15px">
            排除0分
          </el-checkbox>
          <el-select v-model="displayLimit" size="small" style="width: 100px">
            <el-option :value="10" label="显示10名" />
            <el-option :value="20" label="显示20名" />
            <el-option :value="50" label="显示50名" />
            <el-option :value="100" label="显示100名" />
          </el-select>
          <el-button 
             :icon="DataLine" 
             circle 
             size="small" 
             style="margin-left: 10px"
             @click="toggleChartMode"
             :title="showChart ? '切换表格' : '切换图表'"
          />
        </div>
      </div>
    </template>
    
    <div v-if="showChart" class="chart-container" ref="chartRef" style="height: 400px; width: 100%"></div>

    <el-table 
      v-else
      :data="sortedData" 
      style="width: 100%" 
      size="small" 
      stripe
      :row-class-name="tableRowClassName"
      :default-sort="{ prop: 'filteredTotal', order: type === 'top' ? 'descending' : 'ascending' }"
    >
      <el-table-column type="index" label="排名" width="60" align="center">
        <template #default="scope">
          <div class="rank-badge" :class="getRankClass(scope.$index)">
            {{ scope.$index + 1 }}
          </div>
        </template>
      </el-table-column>
      
      <el-table-column prop="className" label="班级" width="80" align="center" sortable :sort-method="(a, b) => Number(a.className) - Number(b.className)">
        <template #default="scope">
          <el-tag size="small" effect="plain">{{ scope.row.className }}班</el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="studentNo" label="学号" width="100" align="center" sortable :sort-method="(a, b) => Number(a.studentNo) - Number(b.studentNo)" />
      
      <el-table-column prop="studentName" label="姓名" width="90" align="center" show-overflow-tooltip sortable />
      
      <el-table-column prop="filteredTotal" label="总分" align="center" width="80" sortable>
        <template #default="scope">
          <span class="score-text">{{ Math.round(scope.row.filteredTotal || 0) }}</span>
        </template>
      </el-table-column>
      
      <el-table-column label="单项成绩" align="center">
        <el-table-column prop="avgTyping" label="打字" align="center" width="70" sortable />
        <el-table-column prop="avgTheory" label="理论" align="center" width="70" sortable />
        <el-table-column prop="avgPractical" label="操作" align="center" width="70" sortable />
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, computed, watch, nextTick, onUnmounted } from 'vue';
import { DataLine } from '@element-plus/icons-vue';
import * as echarts from 'echarts';

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  title: {
    type: String,
    default: '排行榜'
  },
  type: {
    type: String,
    default: 'top', // 'top' or 'bottom'
    validator: (val) => ['top', 'bottom'].includes(val)
  },
  limit: {
    type: Number,
    default: 50
  }
});

const displayLimit = ref(props.limit);
const excludeZero = ref(false);
const showChart = ref(false);
const chartRef = ref(null);
let chartInstance = null;

// 初始化 limit
watch(() => props.limit, (val) => displayLimit.value = val);

const sortedData = computed(() => {
  if (!props.data || props.data.length === 0) return [];
  
  // 1. 复制数组
  let list = [...props.data];
  
  // 2. 过滤
  if (props.type === 'bottom' && excludeZero.value) {
      list = list.filter(s => (Number(s.filteredTotal) || 0) > 0);
  }
  
  // 3. 排序 (默认按总分)
  list.sort((a, b) => {
    const scoreA = Number(a.filteredTotal || 0);
    const scoreB = Number(b.filteredTotal || 0);
    return props.type === 'top' ? scoreB - scoreA : scoreA - scoreB;
  });
  
  // 4. 截取
  return list.slice(0, displayLimit.value);
});

function toggleChartMode() {
    showChart.value = !showChart.value;
    if (showChart.value) {
        nextTick(() => initChart());
    } else {
        disposeChart();
    }
}

function initChart() {
    if (!chartRef.value) return;
    
    // 如果已经存在实例，先销毁
    disposeChart();
    
    chartInstance = echarts.init(chartRef.value);
    
    // 准备数据 (数据可能会比较多，图表需要DataZoom)
    const names = sortedData.value.map(item => item.studentName);
    const scores = sortedData.value.map(item => item.filteredTotal);
    
    // 计算 DataZoom 的起始位置，默认只显示前20条，避免太挤
    const zoomEnd = names.length > 20 ? (20 / names.length) * 100 : 100;
    
    const option = {
        tooltip: {
            trigger: 'axis',
            formatter: (params) => {
                const idx = params[0].dataIndex;
                const item = sortedData.value[idx];
                return `<div style="text-align:left">
                        <b>NO.${idx + 1} ${item.studentName}</b> (${item.studentNo})<br/>
                        班级: ${item.className}班<br/>
                        <span style="display:inline-block;width:10px;height:10px;border-radius:50%;background:${params[0].color};margin-right:5px"></span>
                        总分: <b>${item.filteredTotal}</b>
                        </div>`;
            }
        },
        grid: { left: '5%', right: '5%', bottom: '15%', top: '10%', containLabel: true },
        dataZoom: [
            {
                type: 'slider',
                show: true,
                start: 0,
                end: zoomEnd,
                bottom: 5,
                height: 20
            },
            {
                type: 'inside',
                start: 0,
                end: zoomEnd
            }
        ],
        xAxis: {
            type: 'category',
            data: names,
            axisLabel: { 
                interval: 0, 
                rotate: 45, 
                fontSize: 14, // 增大字体
                fontWeight: 'bold',
                color: '#606266'
            }
        },
        yAxis: { 
            type: 'value',
            axisLabel: { fontSize: 12 }
        },
        series: [{
            data: scores,
            type: 'bar',
            barMaxWidth: 30, // 限制柱状图最大宽度
            itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                    { offset: 0, color: props.type === 'top' ? '#F56C6C' : '#E6A23C' },
                    { offset: 1, color: props.type === 'top' ? '#FF9E9E' : '#F5DAB1' }
                ]),
                borderRadius: [4, 4, 0, 0]
            },
            label: { 
                show: true, 
                position: 'top', 
                fontSize: 14, // 增大字体
                fontWeight: 'bold' 
            }
        }]
    };
    
    chartInstance.setOption(option);
    
    // 监听窗口大小变化
    window.addEventListener('resize', handleResize);
}

function handleResize() {
    chartInstance && chartInstance.resize();
}

function disposeChart() {
    if (chartInstance) {
        window.removeEventListener('resize', handleResize);
        chartInstance.dispose();
        chartInstance = null;
    }
}

// 组件卸载时销毁图表
onUnmounted(() => {
    disposeChart();
});

function getRankClass(index) {
  if (props.type === 'bottom') return 'rank-normal';
  if (index === 0) return 'rank-1';
  if (index === 1) return 'rank-2';
  if (index === 2) return 'rank-3';
  return 'rank-normal';
}

function tableRowClassName({ row, rowIndex }) {
  if (props.type === 'bottom') return '';
  if (rowIndex < 3) return 'success-row';
  return '';
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left, .header-right {
  display: flex;
  align-items: center;
}

.title {
  font-weight: bold;
  font-size: 16px;
}

.title.top {
  color: #F56C6C;
}

.title.bottom {
  color: #E6A23C;
}

.rank-badge {
  width: 24px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  border-radius: 50%;
  background-color: #f0f2f5;
  color: #909399;
  margin: 0 auto;
  font-size: 12px;
  font-weight: bold;
}

.rank-1 {
  background-color: #F56C6C;
  color: white;
}

.rank-2 {
  background-color: #E6A23C;
  color: white;
}

.rank-3 {
  background-color: #67C23A;
  color: white;
}

.score-text {
  font-weight: bold;
  font-family: 'Roboto Mono', monospace;
  font-size: 14px;
}
</style>
