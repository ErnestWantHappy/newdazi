<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="mb-20">
      <el-form :model="queryParams" ref="queryForm" :inline="true" size="small">
        <el-form-item label="学年" prop="academicYear">
          <el-select v-model="queryParams.academicYear" placeholder="选择学年" style="width: 140px">
            <el-option
              v-for="item in academicYearOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学期" prop="semester">
          <el-select v-model="queryParams.semester" placeholder="选择学期" style="width: 120px">
            <el-option label="第一学期" value="1" />
            <el-option label="第二学期" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="学校名称" prop="deptName">
          <el-input
            v-model="queryParams.deptName"
            placeholder="搜索学校"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 概览卡片 -->
    <el-row :gutter="20" class="mb-20">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header><span>平均打字速度</span></template>
          <div class="stat-value">{{ summary.avgTypingSpeed }} <span class="unit">字/分</span></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header><span>平均总分</span></template>
          <div class="stat-value">{{ summary.avgTotalScore }} <span class="unit">分</span></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header><span>整体及格率</span></template>
          <div class="stat-value">{{ summary.passRate }} <span class="unit">%</span></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header><span>参考学校/人数</span></template>
          <div class="stat-value">{{ summary.schoolCount }} / {{ summary.studentCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-card shadow="never" class="mb-20">
      <template #header>
        <div class="card-header">
          <span>校际打字速度对比</span>
          <el-radio-group v-model="chartTab" @change="initChart" size="small">
            <el-radio-button label="1">小学组</el-radio-button>
            <el-radio-button label="2">初中组</el-radio-button>
            <el-radio-button label="3">高中组</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div ref="chartRef" style="height: 350px;"></div>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never">
      <template #header><span>详细数据</span></template>
      <el-table v-loading="loading" :data="schoolList" border stripe>
        <el-table-column label="学校" prop="deptName" min-width="150" show-overflow-tooltip />
        <el-table-column label="学部" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.schoolType === '1'">小学</el-tag>
            <el-tag v-else-if="scope.row.schoolType === '2'" type="success">初中</el-tag>
            <el-tag v-else-if="scope.row.schoolType === '3'" type="warning">高中</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="活跃人数" prop="activeStudentCount" width="100" align="center" sortable />
        <el-table-column label="平均速度(字/分)" prop="avgTypingSpeed" width="150" align="center" sortable>
          <template #default="scope">
            <strong style="color: #409EFF; font-size: 16px">{{ scope.row.avgTypingSpeed }}</strong>
          </template>
        </el-table-column>
        <el-table-column label="平均总分" prop="avgTotalScore" width="120" align="center" sortable />
        <el-table-column label="及格率" prop="passRate" width="120" align="center" sortable>
          <template #default="scope">{{ scope.row.passRate }}%</template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="120">
          <template #default="scope">
            <el-button type="primary" link icon="View" @click="handleDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog :title="detailTitle" v-model="detailVisible" width="800px" append-to-body>
      <el-table :data="classList" border v-loading="detailLoading" height="400">
        <el-table-column label="班级" prop="deptName" />
        <el-table-column label="参考人数" prop="activeStudentCount" align="center" />
        <el-table-column label="平均速度" prop="avgTypingSpeed" align="center" />
        <el-table-column label="平均总分" prop="avgTotalScore" align="center" />
        <el-table-column label="及格率" prop="passRate" align="center">
          <template #default="scope">{{ scope.row.passRate }}%</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="SchoolScoreQuery">
import { listTermStats, listClassDetails } from '@/api/business/schoolScore'
import * as echarts from 'echarts'
import { Search, Refresh, View } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const loading = ref(false)
const schoolList = ref([])
const chartRef = ref(null)
const chartTab = ref('1') // 默认小学
let chartInstance = null

// 详情
const detailVisible = ref(false)
const detailTitle = ref('')
const detailLoading = ref(false)
const classList = ref([])

// 查询参数
const currentYear = new Date().getFullYear()
const currentMonth = new Date().getMonth() + 1
// 学期定义：9-1月为第一学期，2-8月为第二学期
// 当前是2月，属于 2025-2026学年 第二学期
let defaultTerm, defaultAcademicYear
if (currentMonth >= 9) {
  // 9月后：当前学年的第一学期（如2026年9月 = 2026-2027学年第一学期）
  defaultTerm = '1'
  defaultAcademicYear = currentYear.toString()
} else if (currentMonth >= 2) {
  // 2-8月：上一学年的第二学期（如2026年2月 = 2025-2026学年第二学期）
  defaultTerm = '2'
  defaultAcademicYear = (currentYear - 1).toString()
} else {
  // 1月：上一学年的第一学期（如2026年1月 = 2025-2026学年第一学期）
  defaultTerm = '1'
  defaultAcademicYear = (currentYear - 1).toString()
}

const queryParams = ref({
  academicYear: defaultAcademicYear,
  semester: defaultTerm,
  deptName: ''
})

const academicYearOptions = [
  { value: (currentYear - 2).toString(), label: `${currentYear-2}-${currentYear-1}学年` },
  { value: (currentYear - 1).toString(), label: `${currentYear-1}-${currentYear}学年` },
  { value: currentYear.toString(), label: `${currentYear}-${currentYear+1}学年` }
]

const summary = ref({
  avgTypingSpeed: 0,
  avgTotalScore: 0,
  passRate: 0,
  schoolCount: 0,
  studentCount: 0
})

// 初始化 ECharts
function initChart() {
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  
  // 筛选当前 Tab 的数据
  const list = schoolList.value.filter(item => item.schoolType === chartTab.value)
  // 按速度降序
  list.sort((a, b) => b.avgTypingSpeed - a.avgTypingSpeed)
  
  const option = {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { 
      type: 'category', 
      data: list.map(i => i.deptName),
      axisLabel: { interval: 0, rotate: 30 }
    },
    yAxis: { type: 'value', name: '字/分' },
    series: [{
      name: '平均打字速度',
      type: 'bar',
      data: list.map(i => i.avgTypingSpeed),
      itemStyle: { color: '#409EFF' },
      label: { show: true, position: 'top' }
    }]
  }
  chartInstance.setOption(option)
}

function calculateSummary() {
  if (schoolList.value.length === 0) {
    summary.value = { avgTypingSpeed: 0, avgTotalScore: 0, passRate: 0, schoolCount: 0, studentCount: 0 }
    return
  }
  
  let totalSpeed = 0, totalScore = 0, totalPass = 0
  let studentSum = 0
  
  schoolList.value.forEach(s => {
    totalSpeed += s.avgTypingSpeed * s.activeStudentCount
    totalScore += s.avgTotalScore * s.activeStudentCount
    totalPass += (s.passRate / 100) * s.activeStudentCount
    studentSum += s.activeStudentCount
  })
  
  if (studentSum > 0) {
    summary.value = {
      schoolCount: schoolList.value.length,
      studentCount: studentSum,
      avgTypingSpeed: Math.round(totalSpeed / studentSum),
      avgTotalScore: (totalScore / studentSum).toFixed(1),
      passRate: ((totalPass / studentSum) * 100).toFixed(1)
    }
  }
}

function handleQuery() {
  loading.value = true
  console.log('查询参数:', queryParams.value)
  listTermStats(queryParams.value).then(res => {
    console.log('查询结果:', res)
    schoolList.value = res.data || []
    if (schoolList.value.length === 0) {
      proxy.$modal.msgWarning('当前学期暂无数据，请检查是否有课程在该时间段内创建')
    }
    calculateSummary()
    initChart()
    loading.value = false
  }).catch(err => {
    console.error('查询失败:', err)
    loading.value = false
  })
}

function resetQuery() {
  queryParams.value.deptName = ''
  handleQuery()
}

function handleDetail(row) {
  detailTitle.value = `${row.deptName} - 班级详情`
  detailVisible.value = true
  detailLoading.value = true
  listClassDetails({
    departmentId: row.deptId, // 这里的参数名要对上，后端是 deptId
    deptId: row.deptId,
    academicYear: queryParams.value.academicYear,
    semester: queryParams.value.semester
  }).then(res => {
    classList.value = res.rows || []
    detailLoading.value = false
  })
}

// 监听 tab 切换刷新图表 (手动触发一下，因为数据可能变了或者 resize)
watch(() => chartTab.value, () => {
  initChart()
})

onMounted(() => {
  handleQuery()
  window.addEventListener('resize', () => chartInstance && chartInstance.resize())
})
</script>

<style scoped>
.mb-20 { margin-bottom: 20px; }
.stat-value { font-size: 24px; font-weight: bold; color: #303133; margin-top: 10px; }
.unit { font-size: 14px; color: #909399; font-weight: normal; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
