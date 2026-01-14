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
        <el-card class="chart-card" ref="classChartCard">
          <template #header>
            <span>📊 班级平均分对比</span>
            <el-icon class="fullscreen-btn" title="全屏查看" @click="toggleFullscreen('classChartCard')"><FullScreen /></el-icon>
          </template>
          <div ref="classChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="queryParams.classCode ? 24 : 12">
        <el-card class="chart-card" ref="rankChartCard">
          <template #header>
            <span>📈 成绩分布（按总分排名）</span>
            <el-icon class="fullscreen-btn" title="全屏查看" @click="toggleFullscreen('rankChartCard')"><FullScreen /></el-icon>
          </template>
          <div ref="rankChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 打字题专属图表区域 -->
    <el-row :gutter="15" v-if="tableData.length > 0 && hasTypingData" class="chart-row">
      <el-col :span="24">
        <el-card class="chart-card typing-chart-card" ref="typingChartCard">
          <template #header>
            <div class="typing-chart-header">
              <span>⌨️ 打字数据分布</span>
              <el-icon class="fullscreen-btn" title="全屏查看" @click="toggleFullscreen('typingChartCard')"><FullScreen /></el-icon>
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

    <!-- 答题分析区域 - 放在成绩表上方 -->
    <el-card v-if="selectedLessonIds.length === 1 && analysisData.length > 0" class="analysis-card" style="margin-bottom: 15px;">
      <template #header>
        <div class="chart-header">
          📊 答题情况分析 - {{ lessonOptions.find(l => l.lessonId === selectedLessonIds[0])?.lessonTitle || '当前课程' }}
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="24">
           <div ref="analysisChartRef" style="width: 100%; height: 350px;"></div>
        </el-col>
      </el-row>

      <div class="chart-header" style="margin-top: 30px; margin-bottom: 10px; font-weight: bold; font-size: 16px; border-left: 5px solid #67C23A; padding-left: 10px;">
        📋 详细题目分析
      </div>
      
      <el-table :data="analysisData" border stripe>
        <el-table-column label="题目内容" prop="questionContent" min-width="250">
          <template #default="scope">
            <span v-if="scope.row.questionType === 'choice'" class="question-type-tag choice">[选择]</span>
            <span v-else class="question-type-tag judgment">[判断]</span>
            {{ scope.row.questionContent }}
          </template>
        </el-table-column>
        <el-table-column label="正确答案" width="120" align="center">
          <template #default="scope">
            <template v-if="scope.row.questionType === 'judgment'">
              <span>{{ scope.row.answer === 'T' ? '正确' : '错误' }}</span>
            </template>
            <template v-else>
              <span>{{ scope.row.answer }}</span>
              <span v-if="scope.row.optionContents && scope.row.optionContents[scope.row.answer]" style="color: #909399; font-size: 12px;">
                : {{ scope.row.optionContents[scope.row.answer] }}
              </span>
            </template>
          </template>
        </el-table-column>
        <el-table-column label="正确率" prop="accuracy" width="150" sortable>
          <template #default="scope">
            <el-progress :percentage="scope.row.accuracy || 0" :color="getAccuracyColor(scope.row.accuracy || 0)" />
          </template>
        </el-table-column>
        <el-table-column label="答题人数" prop="studentCount" width="100" align="center" sortable>
          <template #default="scope">
            {{ scope.row.studentCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="选项分布" min-width="350">
           <template #default="scope">
             <div class="distribution-bar" v-if="scope.row.answerDistribution">
               <div v-for="(count, opt) in scope.row.answerDistribution" :key="opt" class="dist-item">
                 <div class="dist-info">
                   <span class="opt-label" :class="{ correct: opt === scope.row.answer }">{{ opt }}</span>
                   <span class="opt-content" v-if="scope.row.optionContents && scope.row.optionContents[opt]" :title="scope.row.optionContents[opt]">
                      : {{ scope.row.optionContents[opt] }}
                   </span>
                   <span class="count">({{ count }}人)</span>
                 </div>
                 <div class="dist-progress" :style="{ width: getDistPercent(count, scope.row.studentCount) + '%' }"></div>
               </div>
             </div>
             <span v-else>-</span>
           </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 学生答题详情矩阵 -->
    <el-card v-if="selectedLessonIds.length === 1 && matrixData.length > 0" class="analysis-matrix-card" style="margin-bottom: 15px;">
      <template #header>
        <div class="card-header">
           <span style="font-weight: bold; font-size: 16px;">📋 学生理论测试详情</span>
        </div>
      </template>
      <el-table :data="matrixData" border stripe height="500" v-loading="matrixLoading">
        <el-table-column prop="className" label="班级" width="100" fixed />
        <el-table-column prop="studentNo" label="学号" width="120" fixed sortable />
        <el-table-column prop="studentName" label="姓名" width="100" fixed />
        
        <el-table-column v-for="(q, index) in analysisData" :key="q.questionId" width="70" align="center">
            <template #header>
                <el-tooltip :content="q.questionContent" placement="top" :show-after="200" max-width="300">
                    <span style="cursor: help; text-decoration: underline dashed;">第{{ index + 1 }}题</span>
                </el-tooltip>
            </template>
            <template #default="scope">
                <div v-html="renderMatrixCell(scope.row, q.questionId)"></div>
            </template>
        </el-table-column>
      </el-table>
    </el-card>

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
                      class="score-num"
                    >{{ score.totalScore }}</el-tag>
                  </template>
                  <div class="score-detail">
                    <p><b>打字：</b><span class="score-num">{{ score.typingScore }}</span> 分</p>
                    <p><b>理论：</b><span class="score-num">{{ score.theoryScore }}</span> 分</p>
                    <p><b>操作：</b><span class="score-num">{{ score.practicalScore }}</span> 分</p>
                    <el-divider v-if="score.avgTypingSpeed" style="margin: 8px 0" />
                    <template v-if="score.avgTypingSpeed">
                      <p><b>打字速度：</b><span class="score-num">{{ score.avgTypingSpeed }}</span> <small>字/分</small></p>
                      <p><b>正确率：</b><span class="score-num">{{ score.avgAccuracyRate }}%</span></p>
                      <p><b>完成率：</b><span class="score-num">{{ score.avgCompletionRate }}%</span></p>
                    </template>
                  </div>
                </el-popover>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="avgTyping" label="打字平均" width="95" align="center" sortable>
          <template #default="scope">
            <span class="gray-text score-num">{{ scope.row.avgTyping }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="overallTypingSpeed" label="打字速度" width="100" align="center" sortable>
          <template #default="scope">
            <span v-if="scope.row.overallTypingSpeed" class="typing-speed score-num">{{ scope.row.overallTypingSpeed }} <small>字/分</small></span>
            <span v-else class="gray-text">-</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="overallAccuracy" label="打字正确率" width="100" align="center" sortable>
          <template #default="scope">
            <span v-if="scope.row.overallAccuracy" class="typing-accuracy score-num">{{ scope.row.overallAccuracy }}%</span>
            <span v-else class="gray-text">-</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="overallCompletion" label="打字完成率" width="100" align="center" sortable>
          <template #default="scope">
            <span v-if="scope.row.overallCompletion" class="typing-completion score-num">{{ scope.row.overallCompletion }}%</span>
            <span v-else class="gray-text">-</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="avgTheory" label="理论平均" width="95" align="center" sortable>
          <template #default="scope">
            <span class="gray-text score-num">{{ scope.row.avgTheory }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="avgPractical" label="操作平均" width="95" align="center" sortable>
          <template #default="scope">
            <span class="gray-text score-num">{{ scope.row.avgPractical }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="filteredTotal" label="总分" width="100" align="center" sortable>
          <template #default="scope">
            <div class="data-bar-cell">
              <div class="data-bar" :style="{ width: getBarWidth(scope.row.filteredTotal, maxTotal) + '%' }"></div>
              <span class="data-bar-value total-score score-num">{{ scope.row.filteredTotal }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="filteredAverage" label="平均分" width="100" align="center" sortable>
          <template #default="scope">
            <div class="data-bar-cell avg-bar">
              <div class="data-bar" :style="{ width: getBarWidth(scope.row.filteredAverage, 100) + '%' }"></div>
              <span class="data-bar-value avg-score score-num">{{ scope.row.filteredAverage }}</span>
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
          <span>学号: <span class="score-num">{{ currentStudent.studentNo }}</span></span>
          <span>班级: {{ currentStudent.className }}</span>
          <span>总分: <span class="score-num">{{ currentStudent.filteredTotal }}</span></span>
          <span>平均分: <span class="score-num">{{ currentStudent.filteredAverage }}</span></span>
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
          <el-table-column prop="typingScore" label="打字" width="80" align="center" class-name="score-num" />
          <el-table-column prop="theoryScore" label="理论" width="80" align="center" class-name="score-num" />
          <el-table-column prop="practicalScore" label="操作" width="80" align="center" class-name="score-num" />
          <el-table-column prop="totalScore" label="总分" width="80" align="center" class-name="score-num" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="ScoreQuery">
import { ref, watch, onMounted, nextTick, computed } from 'vue';
import { useRoute } from 'vue-router';
import { getScoreClasses, getScoreLessons, getScoreSummary, exportScoreExcel, getQuestionAnalysis, getStudentAnswerMatrix } from '@/api/business/score';
import { ElMessage } from 'element-plus';
import { FullScreen, Search, Download } from '@element-plus/icons-vue';
import * as echarts from 'echarts';

const route = useRoute();
const loading = ref(false);
const matrixLoading = ref(false);
const matrixData = ref([]);
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
// 卡片 ref (用于全屏)
const classChartCard = ref(null);
const rankChartCard = ref(null);
const typingChartCard = ref(null);
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

// 答题分析相关
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

// 获取正确率颜色
function getAccuracyColor(accuracy) {
  if (accuracy >= 80) return '#67C23A';
  if (accuracy >= 60) return '#E6A23C';
  return '#F56C6C';
}

// 计算选项分布百分比
function getDistPercent(count, total) {
  if (!total || total === 0) return 0;
  return Math.min(100, Math.round((count / total) * 100));
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
      // 如果是单课程，自动加载分析
      if (selectedLessonIds.value.length === 1) {
        loadAnalysis(selectedLessonIds.value[0]);
      } else {
        analysisData.value = [];
      }
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
    
    const avgTyping = count > 0 ? Math.round(sumTyping / count) : 0;
    const avgTheory = count > 0 ? Math.round(sumTheory / count) : 0;
    const avgPractical = count > 0 ? Math.round(sumPractical / count) : 0;
    const filteredAverage = count > 0 ? Math.round(sumTotal / count) : 0;
    
    // 计算整体打字指标
    const overallTypingSpeed = typingCount > 0 ? Math.round(typingSpeedSum / typingCount) : null;
    const overallAccuracy = typingCount > 0 ? Math.round(accuracySum / typingCount) : null; // P0: 取整
    const overallCompletion = typingCount > 0 ? Math.round(completionSum / typingCount) : null; // P0: 取整
    
    return {
      ...student,
      studentNo: parseInt(student.studentNo), // P0: 强制转化为数字，修复排序问题
      className: Number(className),
      filteredTotal: Math.round(sumTotal), // P0: 取整
      filteredAverage: filteredAverage,
      avgTyping: avgTyping,
      avgTheory: avgTheory,
      avgPractical: avgPractical,
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
  
  // 按总分排序显示所有学生
  const sorted = [...tableData.value]
    .sort((a, b) => b.filteredTotal - a.filteredTotal);
  
  const names = sorted.map(s => s.studentName);
  // P0: 存储详细数据供 Tooltip 使用
  const detailMap = {};
  sorted.forEach((s, idx) => {
      detailMap[s.studentName] = {
          total: Math.round(s.filteredTotal || 0),
          theory: Math.round(s.theoryScore || 0),
          practical: Math.round(s.practicalScore || 0),
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
            // P0: 详细成绩 Tooltip 增强
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
          fontSize: 10,
          formatter: function(value) {
               // 可选：显示学号 (value 是名字，如果有重名可能需要 index)
               //但在大数据量下，名字更直观
               return value;
          }
      }
    },
    yAxis: { type: 'value', name: '总分' },
    dataZoom: [
      {
        type: 'slider',
        show: names.length > 25,
        start: 0,
        end: names.length > 25 ? Math.min(100, 25 / names.length * 100) : 100,
        height: 20,
        bottom: 5
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
      label: { show: true, position: 'top', fontSize: 10 }
    }],
    grid: { left: '10%', right: '5%', bottom: '20%', top: '15%' }
  };
  
  rankChartInstance.setOption(option, true);
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
  
  // 按当前指标排序显示所有学生
  typingData.sort((a, b) => b[metric] - a[metric]);
  
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
    dataZoom: [
      {
        type: 'slider',
        show: names.length > 25,
        start: 0,
        end: names.length > 25 ? Math.min(100, 25 / names.length * 100) : 100,
        height: 20,
        bottom: 5
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
      label: { show: true, position: 'top', fontSize: 10 }
    }],
    grid: { left: '8%', right: '5%', bottom: '18%', top: '15%' }
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

watch(() => selectedLessonIds.value, (newIds) => {
  if (rawData.value.length > 0) {
    processData();
    // 单课程时自动加载分析
    if (newIds.length === 1) {
        loadAnalysis(newIds[0]);
    } else {
        analysisData.value = [];
    }
  } else {
    analysisData.value = [];
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
// 加载答题分析
function loadAnalysis(lessonId) {
  analysisLoading.value = true;
  analysisData.value = [];
  
  // 传入班级和年份进行过滤
  getQuestionAnalysis(lessonId, queryParams.value.classCode, queryParams.value.entryYear).then(res => {
    console.log('=== 分析数据接收 ===', res.data);
    analysisData.value = res.data || [];
    analysisLoading.value = false;
    nextTick(() => {
      renderAnalysisChart();
    });
    // 加载学生答题矩阵
    loadMatrix(lessonId);
  }).catch(() => {
     analysisLoading.value = false;
  });
}

// 加载学生答题矩阵
function loadMatrix(lessonId) {
    matrixLoading.value = true;
    matrixData.value = [];
    getStudentAnswerMatrix(lessonId, queryParams.value.classCode, queryParams.value.entryYear).then(res => {
        matrixData.value = res || [];
    }).catch(e => {
        console.error('加载矩阵失败', e);
    }).finally(() => {
        matrixLoading.value = false;
    });
}

// 渲染矩阵单元格
function renderMatrixCell(student, questionId) {
    if (!student.results) return '<span style="color:#dedfe0; font-weight: bold;">/</span>';
    const res = student.results.find(r => r.questionId === questionId);
    if (!res) return '<span style="color:#dedfe0; font-weight: bold;">/</span>';
    
    // Check type: assuming "1" or 1.
    if (String(res.isCorrect) === '1') {
        return '<span style="color:#67C23A; font-weight:bold; font-size: 16px;">✔</span>';
    } else if (String(res.isCorrect) === '0') {
         const ans = res.userAnswer || '未答';
        return `<span style="color:#F56C6C; font-weight:bold; cursor:pointer; font-size: 16px;" title="学生答案：${ans}">✖</span>`;
    } else {
        return '<span style="color:#dedfe0; font-weight: bold;">/</span>';
    }
}

// 渲染易错题图表
function renderAnalysisChart() {
  if (!analysisChartRef.value) return;
  if (!analysisChartInstance) {
    analysisChartInstance = echarts.init(analysisChartRef.value);
  }
  
  // 1. 数据过滤与排序
  // 过滤掉无人作答的题目
  const validData = analysisData.value.filter(d => d.studentCount > 0);
  
  // 排序逻辑：将题目按照“错误率从高到低”排序
  // 错误率高 = 正确率低。
  // ECharts Y轴类目默认从下往上绘制（数组第0项在下，最后项在上）
  // 我们希望红色条最长（错误率最高）的在最上面，所以数组由“高正确率 -> 低正确率”排序
  // 这样 0% 正确率（100% 错误率）的会在数组末尾，显示在图表顶部
  const sorted = [...validData].sort((a, b) => b.accuracy - a.accuracy).slice(0, 10);
  
  // 2. 准备数据
  const yAxisData = []; // 题目名称
  const correctSeries = []; // 正确人数
  const wrongSeries = [];   // 错误人数
  
  sorted.forEach(item => {
    // 处理题目名称过长
    let title = item.questionContent;
    if (title.length > 15) title = title.substring(0, 15) + '...';
    yAxisData.push(title);
    
    const correct = item.correctCount || 0;
    const total = item.studentCount || 0;
    const wrong = total - correct;
    
    correctSeries.push(correct);
    wrongSeries.push(wrong);
  });

  const option = {
    tooltip: {
       trigger: 'axis',
       backgroundColor: 'rgba(255, 255, 255, 0.95)',
       extraCssText: 'box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);',
       textStyle: { color: '#333' },
       formatter: function(params) {
          // 由于是同一个类目轴，params[0] 对应的数据index是一样的
          const index = params[0].dataIndex;
          const item = sorted[index];
          
          let html = `<div style="max-width:400px; white-space:normal; line-height: 1.6; font-size: 13px;">`;
          
          // 标题头
          html += `<div style="margin-bottom:8px; border-bottom:1px solid #ebeef5; padding-bottom:5px;">
                      <span style="font-weight:bold; font-size:14px; color:#303133;">${item.questionContent}</span>
                   </div>`;
          
          // 核心指标
          html += `<div style="display:flex; justify-content:space-between; margin-bottom:8px;">
                      <span>类型：<b>${item.questionType === 'choice' ? '选择题' : '判断题'}</b></span>
                      <span>正确率：<b style="color:${getAccuracyColor(item.accuracy)}">${item.accuracy}%</b></span>
                   </div>`;
          
          // 选项详情表格
          html += `<table style="width:100%; border-collapse: collapse; font-size: 12px;">
                    <tr style="background:#f5f7fa; color:#909399;">
                        <td style="padding:4px;">选项</td>
                        <td style="padding:4px;">内容</td>
                        <td style="padding:4px; text-align:right;">人数</td>
                    </tr>`;
          
          // 遍历选项
          const opts = item.optionContents || {};
          const dist = item.answerDistribution || {};
          // 合并判断题 Key
          let distMap = { ...dist };
          if (distMap['T']) { distMap['对'] = (distMap['对'] || 0) + distMap['T']; delete distMap['T']; }
          if (distMap['F']) { distMap['错'] = (distMap['错'] || 0) + distMap['F']; delete distMap['F']; }
          
          let keys = item.questionType === 'choice' ? ['A', 'B', 'C', 'D'] : ['对', '错'];
          
          keys.forEach(k => {
             const txt = opts[k] || (k === '对' ? '正确' : (k === '错' ? '错误' : ''));
             const count = distMap[k] || 0;
             const isCorrect = (k === item.answer) || 
                               (item.answer === 'T' && k === '对') || 
                               (item.answer === 'F' && k === '错');
             
             // 样式处理
             const rowBg = isCorrect ? 'background-color:#f0f9eb;' : '';
             const colorStyle = isCorrect ? 'color:#67C23A; font-weight:bold;' : (count > 0 ? 'color:#F56C6C;' : 'color:#C0C4CC;');
             const mark = isCorrect ? '✅' : '';
             
             html += `<tr style="${rowBg}">
                        <td style="padding:4px; font-weight:bold;">${k} ${mark}</td>
                        <td style="padding:4px; ${colorStyle}">${txt || '-'}</td>
                        <td style="padding:4px; text-align:right; font-weight:bold;">${count}</td>
                      </tr>`;
          });
          
          html += `</table></div>`;
          return html;
       }
    },
    legend: {
       data: ['正确人数', '错误人数'],
       top: 0
    },
    grid: { 
        left: '3%', 
        right: '4%', 
        bottom: '3%', 
        containLabel: true 
    },
    xAxis: { 
      type: 'value', 
      position: 'top', // X轴放在上面更容易阅读
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    yAxis: { 
      type: 'category', 
      data: yAxisData,
      axisLabel: { 
          interval: 0,
          width: 150,
          overflow: 'truncate',
          formatter: function (value) {
              return value;
          }
      },
      axisTick: { show: false }
    },
    series: [
      {
        name: '正确人数',
        type: 'bar',
        stack: 'total',
        label: { show: true, position: 'inside', formatter: (p) => p.value > 0 ? p.value : '' },
        itemStyle: { color: '#52c41a' }, // 绿色
        data: correctSeries
      },
      {
        name: '错误人数',
        type: 'bar',
        stack: 'total',
        label: { show: true, position: 'inside', formatter: (p) => p.value > 0 ? p.value : '' },
        itemStyle: { color: '#ff4d4f' }, // 红色
        data: wrongSeries
      }
    ]
  };
  
  analysisChartInstance.setOption(option);
}

// 全屏切换功能
function toggleFullscreen(cardRefName) {
  // 通过 ref 名称获取对应的卡片元素
  const cardRefMap = {
    'classChartCard': classChartCard,
    'rankChartCard': rankChartCard,
    'typingChartCard': typingChartCard
  };
  
  const cardRef = cardRefMap[cardRefName];
  if (!cardRef || !cardRef.value) return;
  
  // 获取 el-card 的 DOM 元素
  const element = cardRef.value.$el || cardRef.value;
  
  if (!document.fullscreenElement) {
    if (element.requestFullscreen) {
       element.requestFullscreen();
    } else if (element.webkitRequestFullscreen) {
       element.webkitRequestFullscreen();
    } else if (element.msRequestFullscreen) {
       element.msRequestFullscreen();
    }
    // 全屏后重新调整图表大小
    setTimeout(() => {
        if (classChartInstance) classChartInstance.resize();
        if (rankChartInstance) rankChartInstance.resize();
        if (typingChartInstance) typingChartInstance.resize();
    }, 300);
  } else {
    if (document.exitFullscreen) {
       document.exitFullscreen();
    }
    // 退出全屏后重新调整图表大小
    setTimeout(() => {
        if (classChartInstance) classChartInstance.resize();
        if (rankChartInstance) rankChartInstance.resize();
        if (typingChartInstance) typingChartInstance.resize();
    }, 300);
  }
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
  position: relative;
  
  .chart-container {
    height: 280px;
    background: #fff;
    padding: 10px;
  }
  
  // 头部全屏按钮样式
  :deep(.el-card__header) {
      display: flex;
      justify-content: space-between;
      align-items: center;
  }
}

// 全屏模式下的图表卡片样式
.chart-card:fullscreen,
.chart-card:-webkit-full-screen,
.chart-card:-moz-full-screen {
  background: #fff;
  display: flex;
  flex-direction: column;
  padding: 20px;
  
  :deep(.el-card__header) {
      flex-shrink: 0;
      padding: 15px 20px;
      border-bottom: 1px solid #ebeef5;
      font-size: 18px;
  }
  
  :deep(.el-card__body) {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 20px;
  }
  
  .chart-container {
      width: 100%;
      height: 100% !important;
      max-height: calc(100vh - 100px);
  }
}

.fullscreen-btn {
    cursor: pointer;
    font-size: 18px;
    color: #909399;
    transition: color 0.2s;
    &:hover {
        color: #409EFF;
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
