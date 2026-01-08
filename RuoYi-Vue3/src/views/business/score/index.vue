<template>
  <div class="app-container">
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
        
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button type="success" icon="Download" @click="handleExport" :disabled="!tableData.length">导出 Excel</el-button>
        
        <!-- 选中课程提示 -->
        <span v-if="selectedLessonIds.length > 0" class="selected-tip">
          已选中 {{ selectedLessonIds.length }} 门课程
          <el-button link type="primary" @click="clearSelection">清除选择</el-button>
        </span>
      </div>
    </el-card>

    <el-card class="data-card">
      <el-table :data="tableData" v-loading="loading" border stripe :default-sort="{ prop: 'studentNo', order: 'ascending' }">
        <el-table-column prop="className" label="班级" width="80" align="center" sortable />
        <el-table-column prop="studentNo" label="学号" width="80" align="center" sortable />
        <el-table-column prop="studentName" label="姓名" width="100" align="center" />
        
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
                <el-popover placement="bottom" :width="200" trigger="hover">
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
        
        <el-table-column prop="filteredTotal" label="总分" width="80" align="center" sortable>
          <template #default="scope">
            <span class="total-score">{{ scope.row.filteredTotal }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="filteredAverage" label="平均分" width="90" align="center" sortable>
          <template #default="scope">
            <span class="avg-score">{{ scope.row.filteredAverage }}</span>
          </template>
        </el-table-column>
      </el-table>
      
      <div v-if="!tableData.length && !loading" class="empty-tip">
        请选择入学年份后点击查询
      </div>
    </el-card>
  </div>
</template>

<script setup name="ScoreQuery">
import { ref, watch, onMounted } from 'vue';
import { getScoreClasses, getScoreLessons, getScoreSummary, exportScoreExcel } from '@/api/business/score';
import { ElMessage } from 'element-plus';

const loading = ref(false);
const yearOptions = ref([]);
const classOptions = ref([]);
const lessonOptions = ref([]); // 课程下拉选项
const dropdownLessonIds = ref([]); // 顶部下拉选中的课程
const rawData = ref([]); // 原始数据
const tableData = ref([]); // 处理后的数据
const selectedLessonIds = ref([]); // 表格内勾选的课程ID

const queryParams = ref({
  entryYear: null,
  classCode: null
});

onMounted(() => {
  loadClasses();
});

function loadClasses() {
  getScoreClasses().then(res => {
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
  
  // 加载该年级的课程
  if (val) {
    getScoreLessons(val).then(res => {
      lessonOptions.value = res.data || [];
    });
  }
}

function onClassChange(val) {
  tableData.value = [];
  rawData.value = [];
}

function handleQuery() {
  if (!queryParams.value.entryYear) {
    ElMessage.warning('请选择入学年份');
    return;
  }
  
  loading.value = true;
  selectedLessonIds.value = []; // 重置选择
  
  getScoreSummary(queryParams.value.entryYear, queryParams.value.classCode, null)
    .then(res => {
      rawData.value = res.data || [];
      processData();
    })
    .finally(() => {
      loading.value = false;
    });
}

// 切换课程选择（表格内复选框）
function toggleLesson(lessonId, checked) {
  if (checked) {
    if (!selectedLessonIds.value.includes(lessonId)) {
      selectedLessonIds.value.push(lessonId);
    }
  } else {
    selectedLessonIds.value = selectedLessonIds.value.filter(id => id !== lessonId);
  }
  // 同步到顶部下拉框
  dropdownLessonIds.value = [...selectedLessonIds.value];
  processData();
}

// 清除选择
function clearSelection() {
  selectedLessonIds.value = [];
  dropdownLessonIds.value = [];
  processData();
}

// 顶部下拉框选择变化时同步到表格选择
function onDropdownChange(val) {
  selectedLessonIds.value = [...val];
  processData();
}

// 计算年级（与后端一致，使用8月15日分界）
function calculateGrade(entryYear) {
  const now = new Date();
  const currentYear = now.getFullYear();
  const currentMonth = now.getMonth() + 1; // 0-indexed
  const currentDay = now.getDate();
  
  const afterAug15 = (currentMonth > 8) || (currentMonth === 8 && currentDay >= 15);
  const schoolYear = afterAug15 ? currentYear : currentYear - 1;
  
  return schoolYear - entryYear + 7;
}

// 根据选中的课程筛选并计算
function processData() {
  const selectedIds = selectedLessonIds.value;
  const entryYear = parseInt(queryParams.value.entryYear);
  const grade = calculateGrade(entryYear);
  
  tableData.value = rawData.value.map(student => {
    // 1. 计算班级名 (如 705)
    let className = '';
    if (student.classCode) {
      const code = String(student.classCode).padStart(2, '0');
      className = `${grade}${code}`;
    }

    // 2. 筛选课程（如果有选中则按选中过滤，否则显示全部）
    let filteredScores = student.scores || [];
    if (selectedIds && selectedIds.length > 0) {
      filteredScores = filteredScores.filter(s => selectedIds.includes(s.lessonId));
    }
    
    const count = filteredScores.length;

    // 3. 计算各项总和
    let sumTyping = 0, sumTheory = 0, sumPractical = 0, sumTotal = 0;
    
    filteredScores.forEach(s => {
      sumTyping += (s.typingScore || 0);
      sumTheory += (s.theoryScore || 0);
      sumPractical += (s.practicalScore || 0);
      sumTotal += (s.totalScore || 0);
    });
    
    // 4. 计算平均分
    const avgTyping = count > 0 ? (sumTyping / count).toFixed(1) : '0.0';
    const avgTheory = count > 0 ? (sumTheory / count).toFixed(1) : '0.0';
    const avgPractical = count > 0 ? (sumPractical / count).toFixed(1) : '0.0';
    const filteredAverage = count > 0 ? (sumTotal / count).toFixed(1) : '0.0';
    
    return {
      ...student,
      className: Number(className),
      filteredTotal: sumTotal,
      filteredAverage: Number(filteredAverage),
      avgTyping: Number(avgTyping),
      avgTheory: Number(avgTheory),
      avgPractical: Number(avgPractical)
    };
  });
}

// 监听课程选择变化
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
    selectedLessonIds.value // 传递表格内选中的课程
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
  
  .empty-tip {
    text-align: center;
    padding: 40px;
    color: #909399;
  }
}
</style>
