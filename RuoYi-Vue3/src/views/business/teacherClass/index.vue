<template>
  <div class="app-container teacher-class-page">
    <!-- 顶部产品经理视角看板：任教与班级数据概览 -->
    <div class="class-dashboard-banner">
      <div class="stat-card">
        <div class="stat-icon stat-icon--managed">
          <el-icon><School /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-title">已任教班级</div>
          <div class="stat-value">{{ classList.length }} <span class="stat-unit">个</span></div>
          <div class="stat-sub">当前校区任教管辖</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon stat-icon--students">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-title">覆盖学生总数</div>
          <div class="stat-value">{{ totalStudents }} <span class="stat-unit">人</span></div>
          <div class="stat-sub">关联学籍与作业档案</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon stat-icon--available">
          <el-icon><Collection /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-title">待认领班级</div>
          <div class="stat-value">{{ unmanagedCount }} <span class="stat-unit">个</span></div>
          <div class="stat-sub">本校共享班级库待分配</div>
        </div>
      </div>

      <div class="school-info-card">
        <div class="school-tag-box">
          <el-tag :type="schoolStageTypeTag" effect="plain" class="stage-tag">{{ schoolStageName }}</el-tag>
          <span class="school-name">{{ currentSchoolName }}</span>
        </div>
        <div class="school-hint">
          <el-icon><InfoFilled /></el-icon>
          系统根据当前教师所在学部自动换算年级并标注
        </div>
      </div>
    </div>

    <!-- 主体区域：左侧我管理的班级 + 右侧认领新班级 -->
    <el-row :gutter="20" class="main-content-row">
      <!-- 左侧：我任教的班级 -->
      <el-col :xs="24" :lg="13" class="mb-4">
        <el-card shadow="never" class="box-card custom-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <span class="header-title">我任教的班级</span>
                <el-badge :value="filteredMyClassList.length" type="primary" class="header-badge" />
              </div>
              <div class="header-right">
                <el-input
                  v-model="myClassKeyword"
                  placeholder="搜索届别/班号..."
                  prefix-icon="Search"
                  size="small"
                  clearable
                  class="search-input"
                />
              </div>
            </div>
          </template>

          <el-table
            v-loading="loading"
            :data="filteredMyClassList"
            stripe
            class="custom-table"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="48" align="center" />
            
            <el-table-column label="所属年级 / 入学年份" min-width="170">
              <template #default="scope">
                <div class="grade-cell">
                  <el-tag :type="getGradeTagType(scope.row.entryYear)" effect="light" round size="small" class="grade-badge">
                    {{ getGradeLabel(scope.row.entryYear) }}
                  </el-tag>
                  <span class="entry-year-label">{{ formatEntryYear(scope.row.entryYear) }}</span>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="班级名称" align="center" width="100">
              <template #default="scope">
                <span class="class-name-tag">{{ scope.row.classCode }} 班</span>
              </template>
            </el-table-column>

            <el-table-column label="学生人数" align="center" width="100">
              <template #default="scope">
                <span class="student-count-tag">
                  <el-icon><UserFilled /></el-icon>
                  {{ scope.row.studentCount || 0 }}人
                </span>
              </template>
            </el-table-column>

            <el-table-column label="快捷教学操作" align="center" min-width="210" fixed="right">
              <template #default="scope">
                <div class="action-buttons">
                  <el-tooltip content="查看与管理本班学生学籍名单" placement="top">
                    <el-button link type="primary" size="small" icon="User" @click="handleStudentManage(scope.row)">
                      名单
                    </el-button>
                  </el-tooltip>
                  <el-tooltip content="进入课堂桌面监控、座位排布与机位状态" placement="top">
                    <el-button link type="success" size="small" icon="Monitor" @click="handleDesktop(scope.row)">
                      桌面
                    </el-button>
                  </el-tooltip>
                  <el-tooltip content="解除该班级的任教关系（不删除学生数据）" placement="top">
                    <el-button
                      link
                      type="danger"
                      size="small"
                      icon="Delete"
                      v-hasPermi="['business:teacherClass:remove']"
                      @click="handleDelete(scope.row)"
                    >
                      移出
                    </el-button>
                  </el-tooltip>
                </div>
              </template>
            </el-table-column>

            <template #empty>
              <el-empty
                :image-size="70"
                description="暂无任教班级，可在右侧本校班级池中认领"
              />
            </template>
          </el-table>

          <!-- 批量操作栏 -->
          <div class="card-footer-toolbar" v-if="selectedIds.length > 0">
            <span class="selected-text">已选中 <b>{{ selectedIds.length }}</b> 个班级</span>
            <el-button
              type="danger"
              size="small"
              icon="Delete"
              v-hasPermi="['business:teacherClass:remove']"
              @click="handleBatchDelete"
            >
              批量移出任教
            </el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：认领新班级（全校班级池） -->
      <el-col :xs="24" :lg="11" class="mb-4">
        <el-card shadow="never" class="box-card custom-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <span class="header-title">认领新班级</span>
                <span class="header-subtitle">（{{ currentSchoolName }}）</span>
              </div>
              <div class="header-right">
                <el-button size="small" plain icon="Refresh" @click="getAvailableList">
                  刷新
                </el-button>
              </div>
            </div>
          </template>

          <!-- 年级快捷过滤标签栏（解决几十个班级混在一起难找的痛点） -->
          <div class="grade-filter-bar" v-if="gradeFilterTabs.length > 1">
            <el-radio-group v-model="selectedGradeFilter" size="small" class="grade-tabs">
              <el-radio-button value="ALL">全部</el-radio-button>
              <el-radio-button
                v-for="item in gradeFilterTabs"
                :key="item.value"
                :value="item.value"
              >
                {{ item.label }}
              </el-radio-button>
            </el-radio-group>
          </div>

          <!-- 可选班级表格 -->
          <el-table
            v-loading="availableLoading"
            :data="filteredAvailableList"
            stripe
            class="custom-table"
            @selection-change="handleAvailableSelectionChange"
          >
            <el-table-column type="selection" width="48" align="center" :selectable="checkSelectable" />

            <el-table-column label="年级 / 入学年份" min-width="150">
              <template #default="scope">
                <div class="grade-cell">
                  <el-tag :type="getGradeTagType(scope.row.entryYear)" effect="plain" size="small" class="grade-badge">
                    {{ getGradeLabel(scope.row.entryYear) }}
                  </el-tag>
                  <span class="entry-year-label">{{ formatEntryYear(scope.row.entryYear) }}</span>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="班级" align="center" width="85">
              <template #default="scope">
                <span class="class-name-tag">{{ scope.row.classCode }} 班</span>
              </template>
            </el-table-column>

            <el-table-column label="人数" align="center" width="80">
              <template #default="scope">
                <span class="student-count-text">{{ scope.row.studentCount || 0 }}人</span>
              </template>
            </el-table-column>

            <el-table-column label="操作 / 状态" align="center" width="105" fixed="right">
              <template #default="scope">
                <el-tag v-if="isManaged(scope.row)" type="info" size="small" effect="light" class="managed-tag">
                  已在教
                </el-tag>
                <el-button
                  v-else
                  type="primary"
                  size="small"
                  plain
                  v-hasPermi="['business:teacherClass:add']"
                  @click="handleSingleAdd(scope.row)"
                >
                  认领
                </el-button>
              </template>
            </el-table-column>

            <template #empty>
              <el-empty :image-size="70" description="该年级暂无待认领班级" />
            </template>
          </el-table>

          <!-- 批量认领按钮区 -->
          <div class="card-footer-toolbar" v-if="selectedAvailableList.length > 0">
            <span class="selected-text">选中 <b>{{ selectedAvailableList.length }}</b> 个待认领班级</span>
            <el-button
              type="primary"
              size="small"
              icon="Plus"
              v-hasPermi="['business:teacherClass:add']"
              @click="handleBatchAdd"
            >
              批量认领所选班级
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="TeacherClass">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import useUserStore from '@/store/modules/user'
import { calculateYearsInSection } from '@/utils/academicYear'
import {
  listTeacherClass,
  getAvailableClasses,
  batchAddTeacherClass,
  delTeacherClass
} from '@/api/business/teacherClass'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  School,
  User,
  Collection,
  InfoFilled,
  UserFilled
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const availableLoading = ref(false)
const classList = ref([])
const availableList = ref([])
const selectedIds = ref([])
const selectedAvailableList = ref([])

const myClassKeyword = ref('')
const selectedGradeFilter = ref('ALL')

/** 1. 当前校区与学段智能解析 */
const currentSchool = computed(() => {
  return (userStore.schools || []).find(
    s => Number(s.deptId) === Number(userStore.currentDeptId)
  )
})

const currentSchoolName = computed(() => {
  return currentSchool.value?.deptName || '当前学校'
})

const currentSchoolType = computed(() => {
  if (currentSchool.value?.schoolType) return String(currentSchool.value.schoolType)
  const deptName = String(currentSchool.value?.deptName || '')
  if (deptName.includes('初中')) return '2'
  if (deptName.includes('高中')) return '3'
  return '1' // 默认小学
})

const schoolStageName = computed(() => {
  const type = currentSchoolType.value
  if (type === '2') return '初中部'
  if (type === '3') return '高中部'
  return '小学部'
})

const schoolStageTypeTag = computed(() => {
  const type = currentSchoolType.value
  if (type === '2') return 'warning'
  if (type === '3') return 'danger'
  return 'primary'
})

/** 入学年份括号标注，口径与学生管理页完全一致。 */
function formatEntryYear(year) {
  if (year == null || year === '') return '-'
  const grade = calculateYearsInSection(year)
  const schoolType = currentSchoolType.value
  if (grade == null) return String(year)
  if (schoolType === '1' && grade >= 1 && grade <= 6) return `${year}（${grade}年级）`
  if (schoolType === '2' && grade >= 1 && grade <= 3) return `${year}（初${grade}）`
  if (schoolType === '3' && grade >= 1 && grade <= 3) return `${year}（高${grade}）`
  return String(year)
}

/** 筛选标签用短文案；在读年级才着色。 */
function getGradeLabel(year) {
  const grade = calculateYearsInSection(year)
  const schoolType = currentSchoolType.value
  if (schoolType === '1' && grade >= 1 && grade <= 6) return `${grade}年级`
  if (schoolType === '2' && grade >= 1 && grade <= 3) return `初${grade}`
  if (schoolType === '3' && grade >= 1 && grade <= 3) return `高${grade}`
  return String(year ?? '')
}

function getGradeTagType(year) {
  const grade = calculateYearsInSection(year)
  const schoolType = currentSchoolType.value
  if (schoolType === '1' && grade >= 1 && grade <= 6) return 'primary'
  if (schoolType === '2' && grade >= 1 && grade <= 3) return 'primary'
  if (schoolType === '3' && grade >= 1 && grade <= 3) return 'primary'
  return 'info'
}

/** 3. 统计指标看板计算 */
const totalStudents = computed(() => {
  return classList.value.reduce((sum, item) => sum + (Number(item.studentCount) || 0), 0)
})

const unmanagedCount = computed(() => {
  return availableList.value.filter(item => !isManaged(item)).length
})

/** 4. 快捷年级分类过滤 Tabs（右侧认领池） */
const gradeFilterTabs = computed(() => {
  const years = Array.from(new Set(availableList.value.map(item => String(item.entryYear)).filter(Boolean)))
  years.sort((a, b) => Number(b) - Number(a)) // 按年份降序
  return years.map(y => ({
    value: y,
    label: getGradeLabel(y)
  }))
})

/** 筛选我任教的班级 */
const filteredMyClassList = computed(() => {
  if (!myClassKeyword.value.trim()) return classList.value
  const kw = myClassKeyword.value.trim().toLowerCase()
  return classList.value.filter(item => {
    const formatted = formatEntryYear(item.entryYear).toLowerCase()
    const classStr = `${item.classCode}班`
    return formatted.includes(kw) || classStr.includes(kw) || String(item.entryYear).includes(kw)
  })
})

/** 筛选本校可选班级 */
const filteredAvailableList = computed(() => {
  if (selectedGradeFilter.value === 'ALL') return availableList.value
  return availableList.value.filter(item => String(item.entryYear) === selectedGradeFilter.value)
})

/** 5. 业务请求与数据加载 */
function getList() {
  loading.value = true
  listTeacherClass()
    .then(response => {
      classList.value = response.rows || []
    })
    .finally(() => {
      loading.value = false
    })
}

function getAvailableList() {
  availableLoading.value = true
  getAvailableClasses()
    .then(response => {
      availableList.value = response.data || []
    })
    .finally(() => {
      availableLoading.value = false
    })
}

/** 检查班级是否已被当前老师管理 */
function isManaged(row) {
  return classList.value.some(
    item => String(item.entryYear) === String(row.entryYear) && String(item.classCode) === String(row.classCode)
  )
}

function checkSelectable(row) {
  return !isManaged(row)
}

function handleSelectionChange(selection) {
  selectedIds.value = selection.map(item => item.id)
}

function handleAvailableSelectionChange(selection) {
  selectedAvailableList.value = selection.filter(item => !isManaged(item))
}

/** 6. 认领与移除操作 */
/** 单个班级快速认领 */
function handleSingleAdd(row) {
  const label = `${formatEntryYear(row.entryYear)} ${row.classCode}班`
  batchAddTeacherClass([{ entryYear: row.entryYear, classCode: row.classCode }]).then(() => {
    ElMessage.success(`成功认领 ${label}`)
    getList()
    getAvailableList()
  })
}

/** 批量添加选中班级 */
function handleBatchAdd() {
  if (selectedAvailableList.value.length === 0) {
    ElMessage.warning('请选择要认领的班级')
    return
  }
  const data = selectedAvailableList.value.map(item => ({
    entryYear: item.entryYear,
    classCode: item.classCode
  }))
  batchAddTeacherClass(data).then(() => {
    ElMessage.success(`已成功认领 ${data.length} 个班级`)
    getList()
    getAvailableList()
    selectedAvailableList.value = []
  })
}

/** 删除单个班级管理关系 */
function handleDelete(row) {
  const className = `${formatEntryYear(row.entryYear)} ${row.classCode}班`
  ElMessageBox.confirm(
    `确认解除对“${className}”的任教管理吗？解除后不会删除任何学生数据或作业记录，您可随时重新认领。`,
    '移出任教提醒',
    {
      confirmButtonText: '确认移出',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    delTeacherClass(row.id).then(() => {
      ElMessage.success(`已移出 ${className}`)
      getList()
      getAvailableList()
    })
  })
}

/** 批量删除班级管理关系 */
function handleBatchDelete() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要移出的班级')
    return
  }
  ElMessageBox.confirm(
    `确认移出选中的 ${selectedIds.value.length} 个任教班级吗？解除后可随时重新认领。`,
    '批量移出任教提醒',
    {
      confirmButtonText: '确认移出',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    delTeacherClass(selectedIds.value.join(',')).then(() => {
      ElMessage.success('批量移出成功')
      getList()
      getAvailableList()
      selectedIds.value = []
    })
  })
}

/** 7. 教学业务快捷跳转 */
function handleStudentManage(row) {
  router.push({
    path: '/studentguanli',
    query: {
      entryYear: row.entryYear,
      classCode: row.classCode
    }
  })
}

function handleDesktop(row) {
  router.push({
    path: '/business/classroom-desktop',
    query: {
      entryYear: row.entryYear,
      classCode: row.classCode
    }
  })
}

onMounted(() => {
  getList()
  getAvailableList()
})
</script>

<style scoped>
.teacher-class-page {
  padding: 20px;
  background-color: #f8fafc;
  min-height: calc(100vh - 84px);
}

/* 顶部产品经理视角看板 */
.class-dashboard-banner {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)) 280px;
  gap: 16px;
  margin-bottom: 20px;
}

@media (max-width: 1200px) {
  .class-dashboard-banner {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  }
}

.stat-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.02);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.05);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.stat-icon--managed {
  background: #eff6ff;
  color: #3b82f6;
}

.stat-icon--students {
  background: #f0fdf4;
  color: #10b981;
}

.stat-icon--available {
  background: #fef3c7;
  color: #f59e0b;
}

.stat-content {
  min-width: 0;
}

.stat-title {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.2;
}

.stat-unit {
  font-size: 12px;
  font-weight: normal;
  color: #94a3b8;
  margin-left: 2px;
}

.stat-sub {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 4px;
}

.school-info-card {
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  color: #ffffff;
  border-radius: 12px;
  padding: 18px 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.school-tag-box {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.stage-tag {
  font-weight: 600;
}

.school-name {
  font-size: 15px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.school-hint {
  font-size: 12px;
  color: #94a3b8;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 主体卡片容器 */
.custom-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.header-subtitle {
  font-size: 13px;
  color: #64748b;
  font-weight: normal;
}

.header-badge {
  margin-left: 4px;
}

.search-input {
  width: 170px;
}

/* 年级快捷 Tabs */
.grade-filter-bar {
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #e2e8f0;
  overflow-x: auto;
}

.grade-tabs :deep(.el-radio-button__inner) {
  padding: 5px 12px;
}

/* 自定义表格样式 */
.custom-table {
  border-radius: 8px;
  overflow: hidden;
}

.grade-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.grade-badge {
  font-weight: 600;
}

.entry-year-label {
  font-size: 13px;
  color: #334155;
  font-weight: 500;
}

.class-name-tag {
  font-size: 14px;
  font-weight: 700;
  color: #1e293b;
}

.student-count-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: #f1f5f9;
  color: #475569;
  padding: 3px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.student-count-text {
  font-size: 13px;
  color: #475569;
}

.managed-tag {
  border-radius: 6px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 6px;
}

/* 底部操作工具条 */
.card-footer-toolbar {
  margin-top: 14px;
  padding: 10px 14px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.selected-text {
  font-size: 13px;
  color: #475569;
}

.selected-text b {
  color: #2563eb;
}
</style>
