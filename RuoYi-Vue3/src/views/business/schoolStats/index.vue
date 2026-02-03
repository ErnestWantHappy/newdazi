<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>学校班级统计</span>
          <div class="header-actions">
            <!-- 搜索和筛选 -->
            <el-form :model="queryParams" ref="queryForm" :inline="true" size="small" style="margin-bottom: -18px;">
              <el-form-item label="学校名称" prop="deptName">
                <el-input
                  v-model="queryParams.deptName"
                  placeholder="请输入学校名称"
                  clearable
                  @keyup.enter="handleQuery"
                />
              </el-form-item>
              <el-form-item label="学部" prop="schoolType">
                <el-select v-model="queryParams.schoolType" placeholder="请选择学部" clearable @change="handleQuery" style="width: 120px">
                  <el-option label="小学" value="1" />
                  <el-option label="初中" value="2" />
                  <el-option label="高中" value="3" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
                <el-button icon="Refresh" @click="resetQuery">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </template>

      <!-- 汇总表格 - 移除 default-expand-all -->
      <el-table 
        v-loading="loading" 
        :data="filteredList" 
        border 
        row-key="id"
        :tree-props="{ children: 'children' }"
        style="width: 100%"
      >
        <el-table-column prop="deptName" label="学校名称" min-width="200">
          <template #default="scope">
            <template v-if="scope.row.isSchool">
              <el-icon><School /></el-icon>
              <strong style="margin-left: 6px">{{ scope.row.deptName }}</strong>
              <el-tag v-if="scope.row.schoolType" size="small" type="info" style="margin-left: 8px">
                {{ getSchoolTypeName(scope.row.schoolType) }}
              </el-tag>
            </template>
            <template v-else-if="scope.row.isGrade">
              <span style="color: #606266; margin-left: 16px">
                {{ scope.row.gradeName }}
              </span>
            </template>
            <template v-else>
              <span style="color: #909399; margin-left: 32px">
                {{ scope.row.classCode }}班
              </span>
            </template>
          </template>
        </el-table-column>
        <el-table-column prop="classCount" label="班级数" width="100" align="center">
          <template #default="scope">
            <span v-if="scope.row.classCount">{{ scope.row.classCount }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="studentCount" label="学生总数" width="120" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.isSchool" type="primary" size="large">
              {{ scope.row.studentCount }}
            </el-tag>
            <el-tag v-else-if="scope.row.isGrade" type="success">
              {{ scope.row.studentCount }}
            </el-tag>
            <span v-else>{{ scope.row.studentCount }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup name="SchoolStats">
import { listSchoolClassStats, getSchoolSummary } from '@/api/business/schoolStats'
import { Search, School, Refresh } from '@element-plus/icons-vue'

const loading = ref(false)
const rawList = ref([]) // 原始数据
const filteredList = ref([]) // 过滤后的数据
const queryParams = ref({
  deptName: '',
  schoolType: ''
})

// 学部名称映射
function getSchoolTypeName(type) {
  const map = {
    '1': '小学',
    '2': '初中',
    '3': '高中'
  }
  return map[type] || '未知'
}

// 计算年级名称
function getGradeName(entryYear, schoolType) {
  if (!entryYear) return '未知年级'
  const currentYear = new Date().getFullYear()
  const currentMonth = new Date().getMonth() + 1
  // 9月份开学，9月前算上学期，9月后算下学期
  const academicYear = currentMonth >= 9 ? currentYear : currentYear - 1
  const years = academicYear - parseInt(entryYear)
  
  // 根据学校类型计算年级
  if (schoolType === '1') { // 小学
    const grade = years + 1
    if (grade >= 1 && grade <= 6) {
      return `${grade}年级 (${entryYear}级)`
    }
  } else if (schoolType === '2') { // 初中
    const grade = years + 1
    if (grade >= 1 && grade <= 3) {
      return `${grade}年级 (${entryYear}级)`
    }
  } else if (schoolType === '3') { // 高中
    const grade = years + 1
    if (grade >= 1 && grade <= 3) {
      return `高${grade} (${entryYear}级)`
    }
  }
  return `${entryYear}级`
}

// 构建树形数据
function buildTreeData(detailList, summaryData) {
  // 按学校分组
  const schoolMap = {}
  
  // 首先从汇总数据获取学校信息
  summaryData.forEach(item => {
    schoolMap[item.deptId] = {
      id: `school-${item.deptId}`, // 确保唯一key
      deptId: item.deptId,
      deptName: item.deptName,
      schoolType: item.schoolType,
      classCount: item.classCount,
      studentCount: item.studentCount,
      isSchool: true,
      children: []
    }
  })

  // 按学校和年级分组详情数据
  const gradeMap = {}
  detailList.forEach(item => {
    if (!schoolMap[item.deptId]) return
    
    const gradeKey = `${item.deptId}-${item.entryYear}`
    if (!gradeMap[gradeKey]) {
      gradeMap[gradeKey] = {
        id: gradeKey,
        deptId: item.deptId,
        entryYear: item.entryYear,
        gradeName: getGradeName(item.entryYear, item.schoolType),
        studentCount: 0,
        classCount: 0,
        isGrade: true,
        children: []
      }
    }
    
    // 添加班级
    gradeMap[gradeKey].children.push({
      id: `${gradeKey}-${item.classCode}`,
      classCode: item.classCode,
      studentCount: item.studentCount,
      isClass: true
    })
    gradeMap[gradeKey].studentCount += item.studentCount
    gradeMap[gradeKey].classCount++
  })

  // 将年级添加到学校下
  Object.values(gradeMap).forEach(grade => {
    if (schoolMap[grade.deptId]) {
      // 按年级倒序排列
      schoolMap[grade.deptId].children.push(grade)
    }
  })

  // 对每个学校的年级进行排序（按入学年份倒序）
  Object.values(schoolMap).forEach(school => {
    school.children.sort((a, b) => b.entryYear - a.entryYear)
    // 对每个年级的班级进行排序
    school.children.forEach(grade => {
      // 修复排序问题：转为数字排序
      grade.children.sort((a, b) => parseInt(a.classCode) - parseInt(b.classCode))
    })
  })

  return Object.values(schoolMap).sort((a, b) => a.deptName.localeCompare(b.deptName))
}

// 过滤数据
function filterData() {
  const name = queryParams.value.deptName.toLowerCase()
  const type = queryParams.value.schoolType

  if (!name && !type) {
    filteredList.value = rawList.value
    return
  }

  filteredList.value = rawList.value.filter(school => {
    const matchName = !name || school.deptName.toLowerCase().includes(name)
    const matchType = !type || school.schoolType === type
    return matchName && matchType
  })
}

// 搜索
function handleQuery() {
  filterData()
}

// 重置
function resetQuery() {
  queryParams.value = {
    deptName: '',
    schoolType: ''
  }
  handleQuery()
}

// 获取统计数据
async function getList() {
  loading.value = true
  try {
    const [detailRes, summaryRes] = await Promise.all([
      listSchoolClassStats(),
      getSchoolSummary()
    ])
    rawList.value = buildTreeData(detailRes.data || [], summaryRes.data || [])
    filterData() // 初始过滤（虽然是空的）
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-actions {
  display: flex;
  align-items: center;
}
</style>
