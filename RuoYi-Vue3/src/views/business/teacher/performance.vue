<template>
  <div class="app-container">
    <!-- 顶部筛选 -->
    <div class="filter-container">
      <span class="filter-label">课程：</span>
      <el-select v-model="selectedLessonId" placeholder="请选择课程" @change="onLessonChange" style="width: 220px">
        <el-option-group v-for="group in gradeGroups" :key="group.entryYear" :label="group.entryYear + '级 ' + group.gradeName">
          <el-option v-for="l in group.lessons" :key="l.lessonId" :label="l.lessonTitle" :value="l.lessonId" />
        </el-option-group>
      </el-select>
      
      <span class="filter-label" style="margin-left: 20px">班级：</span>
      <el-select v-model="selectedClassCode" placeholder="请选择班级" @change="onClassChange" :disabled="!selectedLessonId || classes.length === 0" style="width: 120px">
        <el-option v-for="c in classes" :key="c.classCode" :label="c.classCode + '班'" :value="c.classCode" />
      </el-select>
    </div>

    <!-- 学生列表表格 -->
    <el-table 
      :data="studentList" 
      v-loading="loading" 
      border 
      style="width: 100%; margin-top: 20px"
      :row-class-name="tableRowClassName"
    >
      <el-table-column prop="studentNo" label="学号" width="120" />
      <el-table-column prop="studentName" label="姓名" width="120" />
      <el-table-column label="平时分" width="180">
        <template #default="scope">
          <el-input-number 
            v-model="scope.row.score" 
            :min="-10" 
            :max="10" 
            :precision="0"
            size="small"
            controls-position="right"
            @change="onScoreChange(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="原因" min-width="280">
        <template #default="scope">
          <el-input 
            v-model="scope.row.reason" 
            placeholder="加分/扣分原因（如：积极举手、上课说话）"
            size="small"
            clearable
            @change="onReasonChange(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.modified" type="warning" size="small">未保存</el-tag>
          <el-tag v-else-if="scope.row.performanceId" type="success" size="small">已保存</el-tag>
          <el-tag v-else type="info" size="small">未设置</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <!-- 底部操作 -->
    <div class="bottom-actions" v-if="studentList.length > 0">
      <el-button type="primary" size="large" @click="batchSave" :loading="saving">
        <el-icon><Check /></el-icon> 保存全部
      </el-button>
      <span class="tips" style="margin-left: 16px; color: #909399">
        提示：平时分范围 -10 ~ +10，正数为加分，负数为扣分
      </span>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && studentList.length === 0 && selectedClassCode" description="暂无学生数据" />
  </div>
</template>

<script setup name="ClassroomPerformance">
import { ref, onMounted } from 'vue'
import { getDashboardData } from '@/api/business/teacher'
import { getClassesByLesson } from '@/api/business/teacherGrading'
import { getPerformanceList, batchSavePerformance } from '@/api/business/classroomPerformance'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'

const loading = ref(false)
const saving = ref(false)
const gradeGroups = ref([])
const classes = ref([])
const studentList = ref([])

const selectedLessonId = ref(null)
const selectedClassCode = ref(null)
const selectedEntryYear = ref(null)

onMounted(() => {
  fetchDashboardData()
})

function fetchDashboardData() {
  getDashboardData().then(res => {
    gradeGroups.value = res.data
  })
}

function onLessonChange(val) {
  selectedClassCode.value = null
  selectedEntryYear.value = null
  classes.value = []
  studentList.value = []
  
  if (val) {
    getClassesByLesson(val).then(res => {
      classes.value = res.data || []
      // 自动选中第一个班级
      if (classes.value.length === 1) {
        selectedClassCode.value = classes.value[0].classCode
        selectedEntryYear.value = classes.value[0].entryYear
        onClassChange(selectedClassCode.value)
      }
    })
  }
}

function onClassChange(val) {
  if (!val) return
  const classInfo = classes.value.find(c => c.classCode === val)
  selectedEntryYear.value = classInfo?.entryYear || ''
  loadStudentList()
}

function loadStudentList() {
  if (!selectedLessonId.value || !selectedClassCode.value || !selectedEntryYear.value) return
  
  loading.value = true
  getPerformanceList(selectedLessonId.value, selectedClassCode.value, selectedEntryYear.value)
    .then(res => {
      studentList.value = (res.data || []).map(item => ({
        ...item,
        score: item.score || 0,
        reason: item.reason || '',
        modified: false
      }))
    })
    .finally(() => {
      loading.value = false
    })
}

function onScoreChange(row) {
  row.modified = true
}

function onReasonChange(row) {
  row.modified = true
}

function tableRowClassName({ row }) {
  if (row.score > 0) return 'positive-score'
  if (row.score < 0) return 'negative-score'
  return ''
}

function batchSave() {
  if (studentList.value.length === 0) return
  
  saving.value = true
  const performances = studentList.value.map(item => ({
    studentId: item.studentId,
    score: item.score,
    reason: item.reason
  }))
  
  batchSavePerformance({
    lessonId: selectedLessonId.value,
    performances
  }).then(res => {
    ElMessage.success(res.msg || '保存成功')
    // 标记所有为已保存
    studentList.value.forEach(item => {
      item.modified = false
      item.performanceId = true // 标记为已有记录
    })
  }).finally(() => {
    saving.value = false
  })
}
</script>

<style lang="scss" scoped>
.filter-container {
  padding: 16px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  
  .filter-label {
    font-weight: bold;
    color: #606266;
  }
}

.bottom-actions {
  margin-top: 20px;
  padding: 16px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}

:deep(.positive-score) {
  background-color: #f0f9eb !important;
}

:deep(.negative-score) {
  background-color: #fef0f0 !important;
}
</style>
