<template>
  <div class="app-container">
    <!-- 筛选区域 -->
    <el-card class="filter-card">
      <div class="filter-row">
        <span class="filter-label">入学年份：</span>
        <el-select v-model="queryParams.entryYear" placeholder="选择年份" @change="onYearChange" style="width: 120px">
          <el-option v-for="item in yearOptions" :key="item.entryYear" :label="item.entryYear + '级'" :value="item.entryYear" />
        </el-select>
        
        <span class="filter-label">班级：</span>
        <el-select v-model="queryParams.classCode" placeholder="全部班级" clearable @change="onClassChange" style="width: 120px">
          <el-option v-for="item in classOptions" :key="item.classCode" :label="item.classCode + '班'" :value="item.classCode" />
        </el-select>
        
        <span class="filter-label">课程：</span>
        <el-select v-model="dropdownLessonIds" placeholder="全部课程" multiple collapse-tags collapse-tags-tooltip clearable style="width: 280px" @change="onDropdownChange">
          <el-option v-for="item in lessonOptions" :key="item.lessonId" :label="item.lessonTitle" :value="item.lessonId" />
        </el-select>
        
        <!-- 学生搜索 -->
        <span class="filter-label">搜索学生：</span>
        <el-input v-model="searchKeyword" placeholder="姓名或学号" clearable style="width: 150px" @input="filterStudents" />
        
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button type="success" icon="Download" @click="handleExport" :disabled="!tableData.length">导出 Excel</el-button>
        <el-button type="warning" icon="DataAnalysis" @click="handleAnalysis" :disabled="selectedLessonIds.length !== 1">答题分析</el-button>
        
        <!-- 选中课程提示 -->
        <span v-if="selectedLessonIds.length > 0" class="selected-tip">
          已选中 {{ selectedLessonIds.length }} 门课程
          <el-button link type="primary" @click="clearSelection">清除选择</el-button>
        </span>
      </div>
    </el-card>

    <!-- 图表区域 -->
    <el-row :gutter="15" v-if="tableData.length > 0" class="chart-row">
      <!-- 班级平均分对比：仅在未选择具体班级时显示 -->
      <el-col :span="12" v-if="!queryParams.classCode">
        <el-card class="chart-card">
          <template #header>
            <span>📊 班级平均分对比</span>
          </template>
          <div ref="classChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="queryParams.classCode ? 24 : 12">
        <el-card class="chart-card">
          <template #header>
            <span>📈 成绩分布（按总分排名）</span>
          </template>
          <div ref="rankChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 打字题专属图表区域 -->
    <el-row :gutter="15" v-if="tableData.length > 0 && hasTypingData" class="chart-row">
      <el-col :span="24">
        <el-card class="chart-card typing-chart-card">
          <template #header>
            <div class="typing-chart-header">
              <span>⌨️ 打字数据分布</span>
              <div class="typing-chart-controls">
                <el-select v-model="typingChartLesson" placeholder="全部课程" clearable size="small" style="width: 160px; margin-right: 10px" @change="renderTypingChart">
                  <el-option label="全部课程" :value="null" />
                  <el-option v-for="l in lessonOptions" :key="l.lessonId" :label="l.lessonTitle" :value="l.lessonId" />
                </el-select>
                <el-radio-group v-model="typingChartMetric" size="small" @change="renderTypingChart">
                  <el-radio-button label="speed">打字速度</el-radio-button>
                  <el-radio-button label="accuracy">正确率</el-radio-button>
                  <el-radio-button label="completion">完成率</el-radio-button>
                  <el-radio-button label="score">得分</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>
          <div ref="typingChartRef" class="chart-container" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-card class="data-card">
      <el-table :data="displayData" v-loading="loading" border stripe :default-sort="{ prop: 'studentNo', order: 'ascending' }">
        <el-table-column prop="className" label="班级" width="80" align="center" sortable />
        <el-table-column prop="studentNo" label="学号" width="80" align="center" sortable />
        <el-table-column prop="studentName" label="姓名" width="100" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="showStudentProfile(scope.row)">{{ scope.row.studentName }}</el-button>
          </template>
        </el-table-column>
        
        <!-- 各课程成绩：带复选框 -->
        <el-table-column label="各课程成绩（点击勾选参与统计）" align="center" min-width="300">
          <template #default="scope">
            <div class="score-list">
              <div v-for="score in scope.row.scores" :key="score.lessonId" class="score-item">
                <el-checkbox 
                  :model-value="selectedLessonIds.includes(score.lessonId)"
                  @change="(val) => toggleLesson(score.lessonId, val)"
                  size="small"
                />
                <span class="lesson-name">{{ score.lessonTitle }}</span>
                <el-popover placement="bottom" :width="240" trigger="hover">
                  <template #reference>
                    <el-tag 
                      :type="getScoreType(score.totalScore)" 
                      size="small"
                      :class="{ 'selected-tag': selectedLessonIds.includes(score.lessonId) }"
                    >{{ score.totalScore }}</el-tag>
                  </template>
                  <div class="score-detail">
                    <p><b>打字：</b>{{ score.typingScore }} 分</p>
                    <p><b>理论：</b>{{ score.theoryScore }} 分</p>
                    <p><b>操作：</b>{{ score.practicalScore }} 分</p>
                    <el-divider v-if="score.avgTypingSpeed" style="margin: 8px 0" />
                    <template v-if="score.avgTypingSpeed">
                      <p><b>打字速度：</b>{{ score.avgTypingSpeed }} 字/分</p>
                      <p><b>正确率：</b>{{ score.avgAccuracyRate }}%</p>
                      <p><b>完成率：</b>{{ score.avgCompletionRate }}%</p>
                    </template>
                  </div>
                </el-popover>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="avgTyping" label="打字平均" width="95" align="center" sortable>
          <template #default="scope">
            <span class="gray-text">{{ scope.row.avgTyping }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="overallTypingSpeed" label="打字速度" width="100" align="center" sortable>
          <template #default="scope">
            <span v-if="scope.row.overallTypingSpeed" class="typing-speed">{{ scope.row.overallTypingSpeed }} <small>字/分</small></span>
            <span v-else class="gray-text">-</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="overallAccuracy" label="打字正确率" width="100" align="center" sortable>
          <template #default="scope">
            <span v-if="scope.row.overallAccuracy" class="typing-accuracy">{{ scope.row.overallAccuracy }}%</span>
            <span v-else class="gray-text">-</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="overallCompletion" label="打字完成率" width="100" align="center" sortable>
          <template #default="scope">
            <span v-if="scope.row.overallCompletion" class="typing-completion">{{ scope.row.overallCompletion }}%</span>
            <span v-else class="gray-text">-</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="avgTheory" label="理论平均" width="95" align="center" sortable>
          <template #default="scope">
            <span class="gray-text">{{ scope.row.avgTheory }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="avgPractical" label="操作平均" width="95" align="center" sortable>
          <template #default="scope">
            <span class="gray-text">{{ scope.row.avgPractical }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="filteredTotal" label="总分" width="100" align="center" sortable>
          <template #default="scope">
            <div class="data-bar-cell">
              <div class="data-bar" :style="{ width: getBarWidth(scope.row.filteredTotal, maxTotal) + '%' }"></div>
              <span class="data-bar-value total-score">{{ scope.row.filteredTotal }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="filteredAverage" label="平均分" width="100" align="center" sortable>
          <template #default="scope">
            <div class="data-bar-cell avg-bar">
              <div class="data-bar" :style="{ width: getBarWidth(scope.row.filteredAverage, 100) + '%' }"></div>
              <span class="data-bar-value avg-score">{{ scope.row.filteredAverage }}</span>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <div v-if="!tableData.length && !loading" class="empty-tip">
        请选择入学年份后点击查询
      </div>
    </el-card>

    <!-- 学生画像弹窗 -->
    <el-dialog v-model="profileDialogVisible" :title="currentStudent?.studentName + ' 的成绩画像'" width="850px">
      <div v-if="currentStudent" class="profile-content">
        <div class="profile-header">
          <span>学号: {{ currentStudent.studentNo }}</span>
          <span>班级: {{ currentStudent.className }}</span>
          <span>总分: {{ currentStudent.filteredTotal }}</span>
          <span>平均分: {{ currentStudent.filteredAverage }}</span>
        </div>
        
        <!-- 筛选控件 -->
        <div class="profile-filters">
          <el-select v-model="profileLesson" placeholder="全部课程" clearable size="small" style="width: 160px; margin-right: 10px" @change="updateProfileChart">
            <el-option label="全部课程" :value="null" />
            <el-option v-for="s in currentStudent.scores" :key="s.lessonId" :label="s.lessonTitle || '课程' + s.lessonId" :value="s.lessonId" />
          </el-select>
          <el-radio-group v-model="profileScoreType" size="small" @change="updateProfileChart">
            <el-radio-button label="total">总分</el-radio-button>
            <el-radio-button label="typingSpeed">打字速度</el-radio-button>
            <el-radio-button label="theoryAccuracy">理论正确率</el-radio-button>
          </el-radio-group>
        </div>
        
        <div ref="profileChartRef" class="profile-chart"></div>
        
        <!-- 详细数据表格 -->
        <el-table :data="profileTableData" border stripe size="small" style="margin-top: 15px" max-height="200">
          <el-table-column prop="lessonTitle" label="课程" width="120" />
          <el-table-column prop="typingScore" label="打字" width="80" align="center" />
          <el-table-column prop="theoryScore" label="理论" width="80" align="center" />
          <el-table-column prop="practicalScore" label="操作" width="80" align="center" />
          <el-table-column prop="totalScore" label="总分" width="80" align="center" />
        </el-table>
      </div>
    </el-dialog>

    <!-- 答题分析弹窗 -->
    <el-dialog v-model="analysisDialogVisible" title="答题情况分析" width="900px" top="5vh">
      <div v-loading="analysisLoading">
        <!-- 易错题图表 -->
        <div class="chart-header" style="margin-bottom: 10px; font-weight: bold; border-left: 4px solid #409EFF; padding-left: 10px;">
          📊 易错题统计
        </div>
        <div ref="analysisChartRef" style="width: 100%; height: 350px;"></div>
        
        <el-divider />
        
        <!-- 详细数据表格 -->
        <div class="chart-header" style="margin-bottom: 10px; font-weight: bold; border-left: 4px solid #67C23A; padding-left: 10px;">
          📋 详细分析
        </div>
        <el-table :data="analysisData" border stripe height="400">
          <el-table-column label="题目内容" prop="questionContent" min-width="300">
            <template #default="scope">
              <span v-if="scope.row.questionType === 'choice'" class="question-type-tag choice">[选择]</span>
              <span v-else class="question-type-tag judgment">[判断]</span>
              {{ scope.row.questionContent }}
            </template>
          </el-table-column>
          <el-table-column label="正确答案" prop="answer" width="80" align="center" />
          <el-table-column label="正确率" prop="accuracy" width="100" align="center" sortable>
            <template #default="scope">
              <el-tag :type="scope.row.accuracy >= 60 ? 'success' : 'danger'">
                {{ scope.row.accuracy }}%
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="答题人数" prop="studentCount" width="80" align="center" />
          <el-table-column label="选项分布" min-width="200">
            <template #default="scope">
              <div class="dist-bar-container">
                <div v-for="(count, opt) in scope.row.answerDistribution" :key="opt" class="dist-item">
                  <span class="dist-label" :class="{ correct: opt === scope.row.answer }">{{ opt }}</span>
                  <div class="dist-bar-bg">
                    <div class="dist-bar" :style="{ width: (count / scope.row.studentCount * 100) + '%' }"></div>
                  </div>
                  <span class="dist-count">{{ count }}人</span>
                </div>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="ScoreQuery">
import { ref, watch, onMounted, nextTick, computed } from 'vue';
import { useRoute } from 'vue-router';
import { getScoreClasses, getScoreLessons, getScoreSummary, exportScoreExcel, getQuestionAnalysis } from '@/api/business/score';
import { ElMessage } from 'element-plus';
import * as echarts from 'echarts';

const route = useRoute();
const loading = ref(false);
const yearOptions = ref([]);
const classOptions = ref([]);
const lessonOptions = ref([]);
const dropdownLessonIds = ref([]);
const rawData = ref([]);
const tableData = ref([]);
const selectedLessonIds = ref([]);
const searchKeyword = ref('');

// 图表相关
const classChartRef = ref(null);
const rankChartRef = ref(null);
const profileChartRef = ref(null);
const typingChartRef = ref(null);  // 打字题专属图表
let classChartInstance = null;
let rankChartInstance = null;
let profileChartInstance = null;
let typingChartInstance = null;  // 打字题专属图表实例

// 打字图表控制
const typingChartMetric = ref('speed');  // speed | accuracy | completion | score
const typingChartLesson = ref(null);  // 课程筛选

// 学生画像弹窗
const profileDialogVisible = ref(false);
const currentStudent = ref(null);

// 答题分析弹窗
const analysisDialogVisible = ref(false);
const analysisData = ref([]);
const analysisLoading = ref(false);
const analysisChartRef = ref(null);
let analysisChartInstance = null;
const profileLesson = ref(null);  // 学生画像课程筛选
const profileScoreType = ref('total');  // total | typing | theory | practical

// 学生画像详细表格数据
const profileTableData = computed(() => {
  if (!currentStudent.value || !currentStudent.value.scores) return [];
  return currentStudent.value.scores
    .filter(s => s.lessonTitle)  // 过滤掉没有课程名的记录
    .map(s => ({
      lessonTitle: s.lessonTitle || '未知课程',
      typingScore: s.typingScore || '-',
      theoryScore: s.theoryScore || '-',
      practicalScore: s.practicalScore || '-',
      totalScore: s.totalScore || '-'
    }));
});

const queryParams = ref({
  entryYear: null,
  classCode: null
});

// 搜索过滤后的数据
const displayData = computed(() => {
  if (!searchKeyword.value.trim()) {
    return tableData.value;
  }
  const kw = searchKeyword.value.trim().toLowerCase();
  return tableData.value.filter(s => 
    s.studentName?.toLowerCase().includes(kw) || 
    String(s.studentNo).includes(kw)
  );
});

// 计算最大总分（用于 Data Bar 比例）
const maxTotal = computed(() => {
  if (tableData.value.length === 0) return 100;
  return Math.max(...tableData.value.map(s => s.filteredTotal || 0), 1);
});

// 计算 Data Bar 宽度百分比
function getBarWidth(value, max) {
  if (!value || !max) return 0;
  return Math.min(100, Math.round((value / max) * 100));
}

onMounted(async () => {
  await loadClasses();
  
  const urlLessonId = route.query.lessonId;
  const urlEntryYear = route.query.entryYear;
  const urlClassCode = route.query.classCode;
  
  if (urlEntryYear) {
    queryParams.value.entryYear = urlEntryYear;
    if (urlClassCode) {
      queryParams.value.classCode = urlClassCode;
    }
    
    if (window._allClasses) {
      classOptions.value = window._allClasses
        .filter(c => (c.entry_year || c.entryYear) === urlEntryYear)
        .map(c => ({ classCode: c.class_code || c.classCode }))
        .sort((a, b) => parseInt(a.classCode) - parseInt(b.classCode));
    }
    
    const lessonRes = await getScoreLessons(urlEntryYear);
    lessonOptions.value = lessonRes.data || [];
    
    if (urlLessonId) {
      const lessonIdNum = Number(urlLessonId);
      selectedLessonIds.value = [lessonIdNum];
      dropdownLessonIds.value = [lessonIdNum];
    }
    
    handleQuery();
  }
});

function loadClasses() {
  return getScoreClasses().then(res => {
    const data = res.data || [];
    const yearSet = new Set();
    data.forEach(item => yearSet.add(item.entry_year || item.entryYear));
    yearOptions.value = Array.from(yearSet).map(y => ({ entryYear: y })).sort((a, b) => b.entryYear - a.entryYear);
    window._allClasses = data;
  });
}

function onYearChange(val) {
  queryParams.value.classCode = null;
  tableData.value = [];
  rawData.value = [];
  selectedLessonIds.value = [];
  dropdownLessonIds.value = [];
  lessonOptions.value = [];
  
  if (val && window._allClasses) {
    classOptions.value = window._allClasses
      .filter(c => (c.entry_year || c.entryYear) === val)
      .map(c => ({ classCode: c.class_code || c.classCode }))
      .sort((a, b) => parseInt(a.classCode) - parseInt(b.classCode));
  }
  
  if (val) {
    getScoreLessons(val).then(res => {
      lessonOptions.value = res.data || [];
    });
  }
}

function onClassChange() {
  tableData.value = [];
  rawData.value = [];
}

function handleQuery() {
  if (!queryParams.value.entryYear) {
    ElMessage.warning('请选择入学年份');
    return;
  }
  
  loading.value = true;
  
  getScoreSummary(queryParams.value.entryYear, queryParams.value.classCode, null)
    .then(res => {
      rawData.value = res.data || [];
      processData();
      // 使用延时确保 DOM 完全渲染后再初始化图表
      nextTick(() => {
        setTimeout(() => {
          renderCharts();
        }, 100);
      });
    })
    .finally(() => {
      loading.value = false;
    });
}

function toggleLesson(lessonId, checked) {
  if (checked) {
    if (!selectedLessonIds.value.includes(lessonId)) {
      selectedLessonIds.value.push(lessonId);
    }
  } else {
    selectedLessonIds.value = selectedLessonIds.value.filter(id => id !== lessonId);
  }
  dropdownLessonIds.value = [...selectedLessonIds.value];
  processData();
  nextTick(() => renderCharts());
}

function clearSelection() {
  selectedLessonIds.value = [];
  dropdownLessonIds.value = [];
  processData();
  nextTick(() => setTimeout(() => renderCharts(), 100));
}

function onDropdownChange(val) {
  selectedLessonIds.value = [...val];
  processData();
  nextTick(() => setTimeout(() => renderCharts(), 100));
}

function filterStudents() {
  // 使用 computed displayData 自动过滤
}

function calculateGrade(entryYear) {
  const now = new Date();
  const currentYear = now.getFullYear();
  const currentMonth = now.getMonth() + 1;
  const currentDay = now.getDate();
  
  const afterAug15 = (currentMonth > 8) || (currentMonth === 8 && currentDay >= 15);
  const schoolYear = afterAug15 ? currentYear : currentYear - 1;
  
  return schoolYear - entryYear + 7;
}

function processData() {
  const selectedIds = selectedLessonIds.value;
  const entryYear = parseInt(queryParams.value.entryYear);
  const grade = calculateGrade(entryYear);
  
  tableData.value = rawData.value.map(student => {
    let className = '';
    if (student.classCode) {
      const code = String(student.classCode).padStart(2, '0');
      className = `${grade}${code}`;
    }

    let filteredScores = student.scores || [];
    if (selectedIds && selectedIds.length > 0) {
      filteredScores = filteredScores.filter(s => selectedIds.includes(s.lessonId));
    }
    
    const count = filteredScores.length;
    let sumTyping = 0, sumTheory = 0, sumPractical = 0, sumTotal = 0;
    
    // 打字统计：累加有效记录
    let typingSpeedSum = 0, accuracySum = 0, completionSum = 0, typingCount = 0;
    
    filteredScores.forEach(s => {
      sumTyping += (s.typingScore || 0);
      sumTheory += (s.theoryScore || 0);
      sumPractical += (s.practicalScore || 0);
      sumTotal += (s.totalScore || 0);
      
      // 累加打字统计（只统计有数据的记录）
      if (s.avgTypingSpeed) {
        typingSpeedSum += Number(s.avgTypingSpeed);
        accuracySum += Number(s.avgAccuracyRate || 0);
        completionSum += Number(s.avgCompletionRate || 0);
        typingCount++;
      }
    });
    
    const avgTyping = count > 0 ? (sumTyping / count).toFixed(1) : '0.0';
    const avgTheory = count > 0 ? (sumTheory / count).toFixed(1) : '0.0';
    const avgPractical = count > 0 ? (sumPractical / count).toFixed(1) : '0.0';
    const filteredAverage = count > 0 ? (sumTotal / count).toFixed(1) : '0.0';
    
    // 计算整体打字指标
    const overallTypingSpeed = typingCount > 0 ? Math.round(typingSpeedSum / typingCount) : null;
    const overallAccuracy = typingCount > 0 ? (accuracySum / typingCount).toFixed(1) : null;
    const overallCompletion = typingCount > 0 ? (completionSum / typingCount).toFixed(1) : null;
    
    return {
      ...student,
      className: Number(className),
      filteredTotal: sumTotal,
      filteredAverage: Number(filteredAverage),
      avgTyping: Number(avgTyping),
      avgTheory: Number(avgTheory),
      avgPractical: Number(avgPractical),
      overallTypingSpeed,
      overallAccuracy,
      overallCompletion
    };
  });
}

// 渲染图表
function renderCharts() {
  renderClassChart();
  renderRankChart();
  renderTypingChart();  // 打字题专属图表
}

// 计算是否有打字数据
const hasTypingData = computed(() => {
  return tableData.value.some(s => s.overallTypingSpeed !== null && s.overallTypingSpeed !== undefined);
});

// 打字统计表格数据
const typingTableData = computed(() => {
  return tableData.value
    .filter(s => s.overallTypingSpeed)
    .map(s => ({
      className: s.className,
      studentNo: s.studentNo,
      studentName: s.studentName,
      speed: Number(s.overallTypingSpeed) || 0,
      accuracy: Number(s.overallAccuracy) || 0,
      completion: Number(s.overallCompletion) || 0,
      score: Number(s.avgTyping) || 0
    }))
    .sort((a, b) => b.speed - a.speed);
});

function renderClassChart() {
  if (!classChartRef.value) return;
  
  if (!classChartInstance) {
    classChartInstance = echarts.init(classChartRef.value);
  }
  
  // 按班级分组计算平均分
  const classMap = new Map();
  tableData.value.forEach(s => {
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
      axisLabel: { rotate: 0 }
    },
    yAxis: { type: 'value', name: '平均分' },
    series: [{
      type: 'bar',
      data: avgScores,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#409EFF' },
          { offset: 1, color: '#67C23A' }
        ])
      },
      label: { show: true, position: 'top' }
    }],
    grid: { left: '10%', right: '10%', bottom: '15%', top: '15%' }
  };
  
  classChartInstance.setOption(option);
}

function renderRankChart() {
  if (!rankChartRef.value) return;
  
  if (!rankChartInstance) {
    rankChartInstance = echarts.init(rankChartRef.value);
  }
  
  // 按总分排序取前20
  const sorted = [...tableData.value]
    .sort((a, b) => b.filteredTotal - a.filteredTotal)
    .slice(0, 20);
  
  const names = sorted.map(s => s.studentName);
  const scores = sorted.map(s => s.filteredTotal);
  
  const option = {
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: { rotate: 45, fontSize: 10 }
    },
    yAxis: { type: 'value', name: '总分' },
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
      label: { show: true, position: 'top', fontSize: 10 }
    }],
    grid: { left: '10%', right: '5%', bottom: '25%', top: '15%' }
  };
  
  rankChartInstance.setOption(option);
}

// 打字题专属图表：多指标切换
function renderTypingChart() {
  if (!typingChartRef.value) return;
  
  if (!typingChartInstance) {
    typingChartInstance = echarts.init(typingChartRef.value);
  }
  
  // 获取打字数据
  let typingData = [];
  const metric = typingChartMetric.value;
  const selectedLesson = typingChartLesson.value;
  
  // 根据课程筛选获取打字数据
  if (selectedLesson) {
    // 从特定课程获取打字数据
    tableData.value.forEach(student => {
      const lessonScore = student.scores?.find(s => s.lessonId === selectedLesson);
      if (lessonScore && lessonScore.avgTypingSpeed) {
        typingData.push({
          name: student.studentName,
          speed: Number(lessonScore.avgTypingSpeed) || 0,
          accuracy: Number(lessonScore.avgAccuracyRate) || 0,
          completion: Number(lessonScore.avgCompletionRate) || 0,
          score: Number(lessonScore.typingScore) || 0
        });
      }
    });
  } else {
    // 使用总体数据
    tableData.value.forEach(student => {
      if (student.overallTypingSpeed) {
        typingData.push({
          name: student.studentName,
          speed: Number(student.overallTypingSpeed) || 0,
          accuracy: Number(student.overallAccuracy) || 0,
          completion: Number(student.overallCompletion) || 0,
          score: Number(student.avgTyping) || 0
        });
      }
    });
  }
  
  // 按当前指标排序取前30
  typingData.sort((a, b) => b[metric] - a[metric]);
  typingData = typingData.slice(0, 30);
  
  if (typingData.length === 0) {
    typingChartInstance.setOption({
      title: { text: '暂无打字数据', left: 'center', top: 'center' },
      xAxis: { show: false },
      yAxis: { show: false },
      series: []
    }, true);
    return;
  }
  
  const names = typingData.map(s => s.name);
  const values = typingData.map(s => s[metric]);
  
  // 根据指标设置标签和颜色
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
        return `${data.name}<br/>` +
          `打字速度: ${data.speed} 字/分<br/>` +
          `正确率: ${data.accuracy}%<br/>` +
          `完成率: ${data.completion}%<br/>` +
          `得分: ${data.score} 分`;
      }
    },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: { rotate: 45, fontSize: 10 }
    },
    yAxis: { 
      type: 'value', 
      name: `${config.name}(${config.unit})`
    },
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
      label: { show: true, position: 'top', fontSize: 10 }
    }],
    grid: { left: '8%', right: '5%', bottom: '20%', top: '15%' }
  };
  
  typingChartInstance.setOption(option, true);
}

// 学生画像
function showStudentProfile(student) {
  currentStudent.value = student;
  profileLesson.value = null;  // 重置筛选
  profileScoreType.value = 'total';
  profileDialogVisible.value = true;
  
  nextTick(() => {
    renderProfileChart(student);
  });
}

// 更新学生画像图表
function updateProfileChart() {
  if (currentStudent.value) {
    renderProfileChart(currentStudent.value);
  }
}

function renderProfileChart(student) {
  if (!profileChartRef.value) return;
  
  if (!profileChartInstance) {
    profileChartInstance = echarts.init(profileChartRef.value);
  }
  
  // 过滤有效课程数据（修复 undefined 问题）
  let scores = (student.scores || []).filter(s => s.lessonTitle);
  
  // 如果选择了特定课程，只显示该课程
  if (profileLesson.value) {
    scores = scores.filter(s => s.lessonId === profileLesson.value);
  }
  
  if (scores.length === 0) {
    profileChartInstance.setOption({
      title: { text: '暂无成绩数据', left: 'center', top: 'center' },
      xAxis: { show: false },
      yAxis: { show: false },
      series: []
    }, true);
    return;
  }
  
  const scoreType = profileScoreType.value;
  const lessonNames = scores.map(s => s.lessonTitle);
  
  // 根据指标获取对应数据
  let scoreValues;
  let typeName;
  let yAxisName = '分数';
  
  if (scoreType === 'total') {
    scoreValues = scores.map(s => s.totalScore || 0);
    typeName = '总分';
  } else if (scoreType === 'typingSpeed') {
    scoreValues = scores.map(s => s.avgTypingSpeed || 0);
    typeName = '打字速度';
    yAxisName = '字/分';
  } else if (scoreType === 'theoryAccuracy') {
    scoreValues = scores.map(s => s.theoryAccuracy || 0);
    typeName = '理论正确率';
    yAxisName = '%';
  } else {
    scoreValues = scores.map(s => s.totalScore || 0);
    typeName = '总分';
  }
  
  const colorMap = {
    total: '#409EFF',
    typingSpeed: '#E6A23C',
    theoryAccuracy: '#67C23A'
  };
  
  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: [typeName] },
    xAxis: {
      type: 'category',
      data: lessonNames,
      axisLabel: { rotate: 30 }
    },
    yAxis: { type: 'value', name: yAxisName },
    series: [{
      name: typeName,
      type: 'line',
      data: scoreValues,
      smooth: true,
      lineStyle: { width: 3 },
      itemStyle: { color: colorMap[scoreType] },
      areaStyle: { color: colorMap[scoreType] + '33' }
    }],
    grid: { left: '10%', right: '5%', bottom: '20%', top: '15%' }
  };
  
  profileChartInstance.setOption(option, true);
}

watch(() => selectedLessonIds.value, () => {
  if (rawData.value.length > 0) {
    processData();
  }
}, { deep: true });

function handleExport() {
  if (!rawData.value.length) return;
  
  const loadingMsg = ElMessage.loading({
    text: '正在生成 Excel...',
    duration: 0
  });
  
  exportScoreExcel(
    queryParams.value.entryYear, 
    queryParams.value.classCode, 
    selectedLessonIds.value
  ).then(res => {
    const blob = new Blob([res]);
    const link = document.createElement('a');
    link.href = window.URL.createObjectURL(blob);
    link.download = `成绩汇总_${queryParams.value.entryYear}级.xlsx`;
    link.click();
    loadingMsg.close();
    ElMessage.success('导出成功');
  }).catch(() => {
    loadingMsg.close();
    ElMessage.error('导出失败');
  });
}

// 处理答题分析
function handleAnalysis() {
  if (selectedLessonIds.value.length !== 1) {
    ElMessage.warning("请选择一门课程进行分析");
    return;
  }
  
  const lessonId = selectedLessonIds.value[0];
  
  analysisDialogVisible.value = true;
  analysisLoading.value = true;
  analysisData.value = [];
  
  getQuestionAnalysis(lessonId).then(res => {
    analysisData.value = res.data || [];
    analysisLoading.value = false;
    nextTick(() => {
      renderAnalysisChart();
    });
  }).catch(() => {
     analysisLoading.value = false;
  });
}

// 渲染易错题图表
function renderAnalysisChart() {
  if (!analysisChartRef.value) return;
  if (!analysisChartInstance) {
    analysisChartInstance = echarts.init(analysisChartRef.value);
  }
  
  // 取前10个正确率最低的题目（易错题）
  // 过滤掉没有人答的题
  const validData = analysisData.value.filter(d => d.studentCount > 0);
  // 按正确率升序排列（最容易错的在前）
  const sorted = [...validData].sort((a, b) => a.accuracy - b.accuracy).slice(0, 10);
  
  const option = {
    tooltip: {
       trigger: 'axis',
        formatter: function(params) {
          const item = sorted[params[0].dataIndex];
          return `<div style="max-width:300px; white-space:normal">
                  <b>题目：</b>${item.questionContent}<br/>
                  <b>类型：</b>${item.questionType === 'choice' ? '选择题' : '判断题'}<br/>
                  <b>正确率：</b>${item.accuracy}%<br/>
                  <b>答题人数：</b>${item.studentCount}人
                  </div>`;
       }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { 
      type: 'category', 
      data: sorted.map((_, idx) => `TOP ${idx+1}`),
      axisLabel: { interval: 0 }
    },
    yAxis: { type: 'value', name: '正确率(%)', max: 100 },
    series: [
      {
        data: sorted.map(item => item.accuracy),
        type: 'bar',
        barWidth: '40%',
        itemStyle: {
           color: function(params) {
              const val = params.value;
              if (val < 60) return '#F56C6C'; // 红色
              if (val < 80) return '#E6A23C'; // 橙色
              return '#67C23A'; // 绿色
           },
           borderRadius: [4, 4, 0, 0]
        },
        label: { show: true, position: 'top', formatter: '{c}%' }
      }
    ]
  };
  
  analysisChartInstance.setOption(option);
}

function getScoreType(score) {
  if (score >= 90) return 'success';
  if (score >= 60) return '';
  return 'danger';
}
</script>

<style lang="scss" scoped>
.filter-card {
  margin-bottom: 15px;
  
  .filter-row {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-wrap: wrap;
  }
  
  .filter-label {
    color: #606266;
    font-weight: bold;
  }
  
  .selected-tip {
    margin-left: 15px;
    color: #67C23A;
    font-size: 13px;
  }
}

.chart-row {
  margin-bottom: 15px;
}

.chart-card {
  .chart-container {
    height: 280px;
  }
}

.data-card {
  .score-list {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }
  
  .score-item {
    display: flex;
    align-items: center;
    gap: 4px;
    
    .lesson-name {
      font-size: 12px;
      color: #909399;
    }
    
    .selected-tag {
      box-shadow: 0 0 0 2px #67C23A;
    }
  }
  
  .score-detail {
    p {
      margin: 5px 0;
    }
    b {
      color: #606266;
    }
  }
  
  .total-score {
    font-size: 16px;
    font-weight: bold;
    color: #409EFF;
  }
  
  .avg-score {
    font-size: 16px;
    font-weight: bold;
    color: #67C23A;
  }
  
  .gray-text {
    color: #606266;
  }
  
  .typing-speed {
    font-weight: bold;
    color: #E6A23C;
    
    small {
      font-size: 10px;
      font-weight: normal;
      color: #909399;
    }
  }
  
  .typing-detail {
    p {
      margin: 5px 0;
    }
    b {
      color: #606266;
    }
  }
  
  .typing-accuracy {
    font-weight: bold;
    color: #67C23A;
  }
  
  .typing-completion {
    font-weight: bold;
    color: #409EFF;
  }
  
  // Data Bar 样式
  .data-bar-cell {
    position: relative;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .data-bar {
      position: absolute;
      left: 0;
      top: 2px;
      bottom: 2px;
      background: linear-gradient(90deg, #e6f4ff, #bae0ff);
      border-radius: 3px;
      transition: width 0.3s ease;
    }
    
    .data-bar-value {
      position: relative;
      z-index: 1;
    }
    
    // 答题分析弹窗样式
    .question-type-tag {
      font-size: 12px;
      font-weight: bold;
      margin-right: 5px;
      
      &.choice { color: #409EFF; }
      &.judgment { color: #E6A23C; }
    }
    
    .dist-bar-container {
      display: flex;
      flex-direction: column;
      gap: 4px;
      
      .dist-item {
        display: flex;
        align-items: center;
        width: 100%;
        
        .dist-label {
          width: 20px;
          text-align: center;
          font-weight: bold;
          margin-right: 5px;
          color: #909399;
          
          &.correct {
            color: #67C23A;
            text-decoration: underline;
          }
        }
        
        .dist-bar-bg {
          flex: 1;
          height: 10px;
          background-color: #f0f2f5;
          border-radius: 5px;
          margin-right: 8px;
          overflow: hidden;
          
          .dist-bar {
            height: 100%;
            background-color: #409EFF;
          }
        }
        
        .dist-count {
          font-size: 12px;
          color: #606266;
          width: 40px;
        }
      }
    }
    
    &.avg-bar .data-bar {
      background: linear-gradient(90deg, #f0f9eb, #c6e6b8);
    }
  }
  
  .empty-tip {
    text-align: center;
    padding: 40px;
    color: #909399;
  }
}

.profile-content {
  .profile-header {
    display: flex;
    gap: 30px;
    margin-bottom: 15px;
    padding: 15px;
    background: #f5f7fa;
    border-radius: 8px;
    
    span {
      font-size: 14px;
      color: #606266;
    }
  }
  
  .profile-filters {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
    gap: 10px;
    padding: 10px 0;
    border-bottom: 1px solid #ebeef5;
  }
  
  .profile-chart {
    height: 280px;
  }
}

// 打字图表头部样式
.typing-chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.typing-chart-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>
