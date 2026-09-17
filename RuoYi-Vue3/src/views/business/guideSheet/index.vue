<template>
  <div class="app-container guide-sheet-library">
    <section class="library-hero">
      <div>
        <h2>导学单管理</h2>
        <p class="hero-description">创建可复用的课堂导学单，在课程设计中选用后供学生填写。</p>
      </div>
      <el-button type="primary" size="large" icon="Plus" @click="handleAdd" v-hasPermi="['business:guideSheet:add']">
        新建导学单
      </el-button>
    </section>

    <el-card class="filter-card" shadow="never">
      <el-form ref="queryRef" :model="queryParams" :inline="true" label-position="top">
        <el-form-item label="模板标题" prop="sheetTitle">
          <el-input
            v-model="queryParams.sheetTitle"
            placeholder="输入标题关键词"
            clearable
            style="width: 220px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-select v-model="queryParams.grade" placeholder="全部年级" clearable style="width: 130px">
            <el-option v-for="dict in biz_grade" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="学期" prop="semester">
          <el-select v-model="queryParams.semester" placeholder="全部学期" clearable style="width: 130px">
            <el-option v-for="dict in biz_semester" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="第几课" prop="lessonNum">
          <el-select v-model="queryParams.lessonNum" placeholder="全部课次" clearable style="width: 130px">
            <el-option v-for="num in 30" :key="num" :label="`第 ${num} 课`" :value="num" />
          </el-select>
        </el-form-item>
        <el-form-item label="可见范围" prop="scope">
          <el-select v-model="queryParams.scope" placeholder="全部" clearable style="width: 150px">
            <el-option label="公共导学单" value="public" />
            <el-option label="我的私有" value="mine" />
          </el-select>
        </el-form-item>
        <el-form-item class="filter-actions" label=" ">
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="library-card" shadow="never">
      <template #header>
        <div class="card-heading">
          <div>
            <span class="card-title">我的导学单</span>
            <span class="record-count">共 {{ total }} 份</span>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="sheetList" row-key="sheetId" class="template-table">
        <el-table-column label="标题" prop="sheetTitle" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="template-title-cell">
              <span class="template-mark">导</span>
              <div>
                <strong>{{ row.sheetTitle }}</strong>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="年级" prop="grade" width="110" align="center">
          <template #default="{ row }">
            <dict-tag :options="biz_grade" :value="row.grade" />
          </template>
        </el-table-column>
        <el-table-column label="学期" prop="semester" width="110" align="center">
          <template #default="{ row }">
            <dict-tag :options="biz_semester" :value="row.semester" />
          </template>
        </el-table-column>
        <el-table-column label="第几课" prop="lessonNum" width="90" align="center">
          <template #default="{ row }">第 {{ row.lessonNum }} 课</template>
        </el-table-column>
        <el-table-column label="可见范围" prop="isPublic" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isPublic === 'Y' ? 'success' : 'info'" effect="plain" round>
              {{ row.isPublic === 'Y' ? '公共导学单' : '我的私有' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建人" width="120" align="center">
          <template #default="{ row }">{{ row.creatorName || row.createBy || '-' }}</template>
        </el-table-column>
        <el-table-column label="最近修改" prop="updateTime" width="180" align="center">
          <template #default="{ row }">{{ row.updateTime || row.createTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="320" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="View" @click="handlePreview(row)">预览</el-button>
            <el-button
              v-if="canManage(row)"
              link
              type="primary"
              icon="Edit"
              @click="handleDesign(row)"
              v-hasPermi="['business:guideSheet:design']"
            >设计</el-button>
            <el-button link type="success" icon="CopyDocument" :loading="copyingId === row.sheetId" @click="handleCopy(row)">
              复制
            </el-button>
            <el-button link type="warning" @click="goUseInLesson(row)">去课程用</el-button>
            <el-button
              v-if="canManage(row)"
              link
              type="danger"
              icon="FolderDelete"
              @click="handleArchive(row)"
              v-hasPermi="['business:guideSheet:remove']"
            >归档</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无导学单">
            <el-button type="primary" @click="handleAdd" v-hasPermi="['business:guideSheet:add']">新建第一份导学单</el-button>
          </el-empty>
        </template>
      </el-table>

      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>

    <el-dialog v-model="onboardingVisible" title="第一次使用导学单" width="min(620px, 92vw)" append-to-body>
      <div class="onboarding-intro">三步即可完成一份能用于课堂的导学单。</div>
      <div class="onboarding-steps">
        <div><span>1</span><strong>填写基本信息</strong><small>标题、年级、学期和第几课</small></div>
        <div><span>2</span><strong>添加教学内容</strong><small>按需添加常用模块</small></div>
        <div><span>3</span><strong>预览并保存</strong><small>确认后在课程中选用</small></div>
      </div>
      <el-alert type="info" :closable="false" show-icon title="设为公共后，同县教师可以复制使用，不会改动你的原模板。" />
      <template #footer>
        <el-button @click="skipOnboarding">跳过</el-button>
        <el-button type="primary" @click="startFirstGuideSheet">开始创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="GuideSheet">
import { getCurrentInstance, onActivated, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import useUserStore from '@/store/modules/user'
import {
  archiveGuideSheet,
  copyGuideSheet,
  listGuideSheet
} from '@/api/business/guideSheet'

const { proxy } = getCurrentInstance()
const router = useRouter()
const userStore = useUserStore()
const { biz_grade, biz_semester } = proxy.useDict('biz_grade', 'biz_semester')

const queryRef = ref(null)
const loading = ref(false)
const copyingId = ref(null)
const total = ref(0)
const sheetList = ref([])
const onboardingVisible = ref(false)
let listRequestId = 0
let skipInitialActivationRefresh = true

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  sheetTitle: undefined,
  grade: undefined,
  semester: undefined,
  lessonNum: undefined,
  scope: undefined
})

const ONBOARDING_KEY = `guide-sheet:onboarding-complete:${String(userStore.id || 'anonymous')}`

function buildListParams() {
  const params = {
    pageNum: queryParams.pageNum,
    pageSize: queryParams.pageSize
  }
  if (queryParams.sheetTitle) params.sheetTitle = queryParams.sheetTitle
  if (queryParams.grade !== undefined && queryParams.grade !== null && queryParams.grade !== '') {
    params.grade = queryParams.grade
  }
  if (queryParams.semester !== undefined && queryParams.semester !== null && queryParams.semester !== '') {
    params.semester = queryParams.semester
  }
  if (queryParams.lessonNum !== undefined && queryParams.lessonNum !== null && queryParams.lessonNum !== '') {
    params.lessonNum = queryParams.lessonNum
  }
  if (queryParams.scope === 'public' || queryParams.scope === 'mine') {
    params.scope = queryParams.scope
  } else {
    params.scope = 'all'
  }
  return params
}

function getList() {
  const requestId = ++listRequestId
  loading.value = true
  listGuideSheet(buildListParams())
    .then(response => {
      if (requestId !== listRequestId) return
      sheetList.value = response.rows || response.data || []
      total.value = Number(response.total || sheetList.value.length || 0)
    })
    .catch(() => {
      if (requestId === listRequestId) ElMessage.error('获取导学单模板失败')
    })
    .finally(() => {
      if (requestId === listRequestId) loading.value = false
    })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryRef.value?.resetFields()
  queryParams.scope = undefined
  handleQuery()
}

function handleAdd() {
  localStorage.setItem(ONBOARDING_KEY, '1')
  router.push({ name: 'GuideSheetDesigner' })
}

function skipOnboarding() {
  localStorage.setItem(ONBOARDING_KEY, '1')
  onboardingVisible.value = false
}

function startFirstGuideSheet() {
  onboardingVisible.value = false
  handleAdd()
}

function canManage(row) {
  return Boolean(
    row.canEdit ||
    row.canArchive ||
    String(row.creatorId) === String(userStore.id) ||
    userStore.roles.includes('admin')
  )
}

function handleDesign(row) {
  router.push({ name: 'GuideSheetDesigner', params: { sheetId: row.sheetId } })
}

function handlePreview(row) {
  router.push({ name: 'GuideSheetPreview', params: { sheetId: row.sheetId } })
}

/** 从导学单直达课程设计，新建课时可直接绑定该模板 */
function goUseInLesson(row) {
  router.push({
    path: '/business/lesson-auth/designer',
    query: {
      guideSheetId: row.sheetId,
      grade: row.grade != null ? String(row.grade) : undefined,
      semester: row.semester != null ? String(row.semester) : undefined,
      nextNum: row.lessonNum != null ? String(row.lessonNum) : undefined,
      lessonMode: 'assessment'
    }
  })
}

async function handleCopy(row) {
  copyingId.value = row.sheetId
  try {
    const response = await copyGuideSheet(row.sheetId)
    const copiedId = response?.data?.sheetId || response?.sheetId || response?.data
    ElMessage.success('模板副本已创建')
    if (copiedId) {
      router.push({ name: 'GuideSheetDesigner', params: { sheetId: copiedId } })
    } else {
      getList()
    }
  } finally {
    copyingId.value = null
  }
}

function handleArchive(row) {
  ElMessageBox.confirm(
    `归档“${row.sheetTitle}”后，新课程将不能再选择它，已有课程快照和历史成绩不会受影响。`,
    '归档模板',
    { type: 'warning', confirmButtonText: '确认归档' }
  ).then(async () => {
    await archiveGuideSheet(row.sheetId)
    ElMessage.success('模板已归档')
    getList()
  }).catch(() => {})
}

onMounted(() => {
  getList()
  if (localStorage.getItem(ONBOARDING_KEY) !== '1') onboardingVisible.value = true
})
onActivated(() => {
  if (skipInitialActivationRefresh) {
    skipInitialActivationRefresh = false
    return
  }
  getList()
})
</script>

<style scoped lang="scss">
.guide-sheet-library {
  --library-ink: #17324d;
  --library-accent: #1677a7;
  --library-soft: #eef7f8;
  background: #f2f5f5;
  min-height: calc(100vh - 84px);
}

.library-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 26px 30px;
  margin-bottom: 16px;
  color: #173b43;
  border: 1px solid #dbe5e7;
  border-left: 5px solid #287c75;
  border-radius: 6px;
  background: #fff;

  h2 { margin: 2px 0 8px; font-size: 26px; letter-spacing: 1px; }
  .hero-kicker { margin: 0; font-size: 11px; letter-spacing: 0; color: #5c8d88; }
  .hero-description { margin: 0; color: #6f8187; }
}

.filter-card,
.library-card {
  border: 0;
  border-radius: 6px;
}

.filter-card {
  margin-bottom: 16px;
  :deep(.el-card__body) { padding-bottom: 4px; }
  :deep(.el-form-item) { margin-right: 14px; margin-bottom: 14px; }
  :deep(.el-form-item__label) { color: #5b6b7a; font-size: 12px; line-height: 22px; }
}

.filter-actions { align-self: flex-end; }

.card-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  .card-title { color: var(--library-ink); font-size: 17px; font-weight: 700; }
  .record-count { margin-left: 10px; color: #8492a6; font-size: 12px; }
  .visibility-note { color: #7a8997; font-size: 12px; }
}

.template-title-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  .template-mark {
    display: inline-grid;
    place-items: center;
    flex: 0 0 34px;
    height: 34px;
    color: #fff;
    border-radius: 10px 4px 10px 4px;
    background: linear-gradient(145deg, #1681a8, #2f9c84);
    font-family: STKaiti, KaiTi, serif;
    font-size: 18px;
  }
  strong { display: block; color: #243b53; font-weight: 650; }
  small { color: #9aa7b4; }
}

.onboarding-intro { margin-bottom: 18px; color: #526970; }
.onboarding-steps { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; margin-bottom: 18px; }
.onboarding-steps > div { padding: 14px; border: 1px solid #dce5e7; border-radius: 6px; background: #f8fafa; }
.onboarding-steps span { display: grid; place-items: center; width: 28px; height: 28px; margin-bottom: 10px; color: #fff; border-radius: 4px; background: #287c75; font-family: Georgia, serif; }
.onboarding-steps strong, .onboarding-steps small { display: block; }
.onboarding-steps strong { color: #29434b; font-size: 13px; }
.onboarding-steps small { margin-top: 4px; color: #75868c; font-size: 11px; line-height: 1.5; }

@media (max-width: 900px) {
  .library-hero { align-items: flex-start; flex-direction: column; }
  .card-heading { align-items: flex-start; flex-direction: column; }
  .visibility-note { display: none; }
  .onboarding-steps { grid-template-columns: 1fr; }
}
</style>
