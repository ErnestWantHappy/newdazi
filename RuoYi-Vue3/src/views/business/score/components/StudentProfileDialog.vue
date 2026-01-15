<template>
  <el-dialog 
    v-model="visible" 
    :title="student?.studentName ? student.studentName + ' 的成绩画像' : '学生画像'" 
    width="850px"
    @closed="handleClose"
  >
    <div v-if="student" class="profile-content">
      <div class="profile-header">
        <span>学号: <span class="score-num">{{ student.studentNo }}</span></span>
        <span>班级: {{ student.className }}</span>
        <span>总分: <span class="score-num">{{ student.filteredTotal }}</span></span>
        <span>平均分: <span class="score-num">{{ student.filteredAverage }}</span></span>
      </div>
      
      <!-- 筛选控件 -->
      <div class="profile-filters">
        <el-select v-model="internalLesson" placeholder="全部课程" clearable size="small" style="width: 160px; margin-right: 10px">
          <el-option label="全部课程" :value="null" />
          <el-option v-for="s in student.scores" :key="s.lessonId" :label="s.lessonTitle || '课程' + s.lessonId" :value="s.lessonId" />
        </el-select>
        <el-radio-group v-model="internalScoreType" size="small">
          <el-radio-button label="total">总分</el-radio-button>
          <el-radio-button label="typingSpeed">打字速度</el-radio-button>
          <el-radio-button label="theoryAccuracy">理论正确率</el-radio-button>
        </el-radio-group>
      </div>
      
      <div ref="chartRef" class="profile-chart"></div>
      
      <!-- 详细数据表格 -->
      <el-table :data="tableData" border stripe size="small" style="margin-top: 15px" max-height="200">
        <el-table-column prop="lessonTitle" label="课程" width="120" />
        <el-table-column prop="typingScore" label="打字" width="80" align="center" class-name="score-num" />
        <el-table-column prop="theoryScore" label="理论" width="80" align="center" class-name="score-num" />
        <el-table-column prop="practicalScore" label="操作" width="80" align="center" class-name="score-num" />
        <el-table-column prop="totalScore" label="总分" width="80" align="center" class-name="score-num" />
      </el-table>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch, nextTick, computed, onUnmounted } from 'vue';
import * as echarts from 'echarts';

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  student: {
    type: Object,
    default: null
  }
});

const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
});

const chartRef = ref(null);
let chartInstance = null;

const internalLesson = ref(null);
const internalScoreType = ref('total');

// 监听弹窗打开和数据变化
watch(() => props.modelValue, (val) => {
  if (val) {
    internalLesson.value = null; // 重置筛选
    internalScoreType.value = 'total';
    nextTick(() => renderChart());
  }
});

watch(internalLesson, () => renderChart());
watch(internalScoreType, () => renderChart());

const tableData = computed(() => {
  if (!props.student || !props.student.scores) return [];
  return props.student.scores
    .filter(s => s.lessonTitle)
    .map(s => ({
      lessonTitle: s.lessonTitle || '未知课程',
      typingScore: s.typingScore || '-',
      theoryScore: s.theoryScore || '-',
      practicalScore: s.practicalScore || '-',
      totalScore: s.totalScore || '-'
    }));
});

function handleClose() {
    disposeChart();
}

function initChart() {
  if (chartRef.value && !chartInstance) {
    chartInstance = echarts.init(chartRef.value);
  }
}

function disposeChart() {
  if (chartInstance) {
    chartInstance.dispose();
    chartInstance = null;
  }
}

function renderChart() {
  if (!props.student || !props.student.scores) return;
  
  initChart();
  if (!chartInstance) return;
  
  const scores = props.student.scores.filter(s => s.lessonTitle); // 过滤无效数据
  
  let dataToRender = scores;
  if (internalLesson.value) {
    dataToRender = scores.filter(s => s.lessonId === internalLesson.value);
  }
  
  const xAxisData = dataToRender.map(s => s.lessonTitle);
  let seriesData = [];
  let yAxisName = '分数';
  
  if (internalScoreType.value === 'total') {
    seriesData = dataToRender.map(s => s.totalScore);
    yAxisName = '总分';
  } else if (internalScoreType.value === 'typingSpeed') {
    seriesData = dataToRender.map(s => s.avgTypingSpeed || 0);
    yAxisName = '速度(字/分)';
  } else if (internalScoreType.value === 'theoryAccuracy') {
    seriesData = dataToRender.map(s => s.avgAccuracyRate || 0);
    yAxisName = '正确率(%)';
  }
  
  const option = {
    tooltip: { trigger: 'axis' },
    grid: { left: '10%', right: '10%', top: '15%', bottom: '10%' },
    xAxis: { type: 'category', data: xAxisData },
    yAxis: { type: 'value', name: yAxisName },
    series: [{
      data: seriesData,
      type: 'line',
      smooth: true,
      areaStyle: {},
      itemStyle: { color: '#409EFF' },
      label: { show: true, position: 'top' }
    }]
  };
  
  chartInstance.setOption(option, true);
}
</script>

<style scoped>
.profile-content {
  padding: 0 20px;
}

.profile-header {
  display: flex;
  justify-content: space-around;
  margin-bottom: 20px;
  font-size: 16px;
  font-weight: bold;
}

.profile-filters {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 15px;
}

.profile-chart {
  width: 100%;
  height: 300px;
}

.score-num {
  font-family: 'Roboto Mono', monospace;
  font-weight: bold;
  color: #409EFF;
}
</style>
