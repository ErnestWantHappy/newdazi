<template>
  <div class="student-profile-page">
    <!-- 顶部筛选栏 -->
    <el-card class="filter-card">
      <StudentSelector 
        v-model:studentId="selectedStudentId"
        v-model:semester="selectedSemester"
        @change="loadProfile"
      />
    </el-card>

    <!-- 内容区域 -->
    <div v-if="selectedStudentId" class="content-area" v-loading="loading">
      <!-- 基础信息卡片 -->
      <StudentInfoCard :studentInfo="profileData" />

      <!-- 图表区域 - 两列布局 -->
      <div class="charts-grid">
        <el-card class="chart-card">
          <template #header>
            <span>历次课程成绩</span>
          </template>
          <CourseScoreChart :data="courseScores" />
        </el-card>

        <el-card class="chart-card">
          <template #header>
            <span>历次打字速度</span>
          </template>
          <TypingSpeedChart :data="typingSpeeds" />
        </el-card>

        <el-card class="chart-card">
          <template #header>
            <span>课堂表现分变化</span>
          </template>
          <PerformanceChart :data="performances" />
        </el-card>

        <el-card class="chart-card">
          <template #header>
            <span>班级平均分对比</span>
          </template>
          <AverageScoreChart :data="courseScores" />
        </el-card>

        <el-card class="chart-card full-width">
          <template #header>
            <span>班级排名变化</span>
          </template>
          <RankChangeChart :data="rankChanges" />
        </el-card>
      </div>
    </div>

    <!-- 未选择学生时的提示 -->
    <el-empty v-else description="请选择学生查看成绩画像" />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import StudentSelector from './components/StudentSelector.vue'
import StudentInfoCard from './components/StudentInfoCard.vue'
import CourseScoreChart from './components/CourseScoreChart.vue'
import TypingSpeedChart from './components/TypingSpeedChart.vue'
import PerformanceChart from './components/PerformanceChart.vue'
import AverageScoreChart from './components/AverageScoreChart.vue'
import RankChangeChart from './components/RankChangeChart.vue'
import { 
  getStudentProfileSummary, 
  getCourseScores, 
  getTypingSpeeds, 
  getPerformances, 
  getRankChanges 
} from '@/api/business/studentProfile'

const route = useRoute()

const selectedStudentId = ref(null)
const selectedSemester = ref(null)
const loading = ref(false)

// 数据
const profileData = ref({})
const courseScores = ref([])
const typingSpeeds = ref([])
const performances = ref([])
const rankChanges = ref([])

// 加载学生画像数据
async function loadProfile() {
  if (!selectedStudentId.value) return
  
  loading.value = true
  
  const semesterStart = selectedSemester.value?.start || null
  const semesterEnd = selectedSemester.value?.end || null
  
  try {
    // 并行请求所有数据
    const [summaryRes, coursesRes, typingRes, perfRes, rankRes] = await Promise.all([
      getStudentProfileSummary(selectedStudentId.value, semesterStart, semesterEnd),
      getCourseScores(selectedStudentId.value, semesterStart, semesterEnd),
      getTypingSpeeds(selectedStudentId.value, semesterStart, semesterEnd),
      getPerformances(selectedStudentId.value, semesterStart, semesterEnd),
      getRankChanges(selectedStudentId.value, semesterStart, semesterEnd)
    ])
    
    profileData.value = summaryRes.data || {}
    courseScores.value = coursesRes.data || []
    typingSpeeds.value = typingRes.data || []
    performances.value = perfRes.data || []
    rankChanges.value = rankRes.data || []
  } catch (error) {
    console.error('加载学生画像失败:', error)
  } finally {
    loading.value = false
  }
}

// 从路由参数获取学生ID（支持从汇总表跳转）
onMounted(() => {
  if (route.query.studentId) {
    selectedStudentId.value = Number(route.query.studentId)
    loadProfile()
  }
})

// 监听路由变化
watch(() => route.query.studentId, (newId) => {
  if (newId) {
    selectedStudentId.value = Number(newId)
    loadProfile()
  }
})
</script>

<style scoped lang="scss">
.student-profile-page {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 84px);
}

.filter-card {
  margin-bottom: 16px;
}

.content-area {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.chart-card {
  min-height: 350px;
  
  &.full-width {
    grid-column: span 2;
  }
}
</style>
