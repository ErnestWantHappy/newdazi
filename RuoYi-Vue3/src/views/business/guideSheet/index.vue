<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="标题" prop="sheetTitle">
        <el-input v-model="queryParams.sheetTitle" placeholder="请输入导学单标题" clearable style="width: 240px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 200px">
          <el-option v-for="dict in statusOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="创建人">
        <el-select v-model="creatorFilter" placeholder="筛选创建人" clearable style="width: 140px" @change="onCreatorFilterChange">
          <el-option label="所有人" value="all" />
          <el-option label="自己" value="self" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:guideSheet:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['business:guideSheet:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="sheetList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="导学单标题" align="left" prop="sheetTitle" show-overflow-tooltip min-width="160" />
      <el-table-column label="正确率" align="center" width="150">
        <template #default="scope">
          <el-progress
            :percentage="scope.row.accuracyRate || 0"
            :stroke-width="16"
            :text-inside="true"
            :color="accuracyColor(scope.row.accuracyRate)"
          >
            <span class="progress-text">{{ scope.row.accuracyRate || 0 }}%</span>
          </el-progress>
        </template>
      </el-table-column>
      <el-table-column label="完成率" align="center" width="150">
        <template #default="scope">
          <el-progress
            :percentage="scope.row.completionRate || 0"
            :stroke-width="16"
            :text-inside="true"
            :color="completionColor(scope.row.completionRate)"
          >
            <span class="progress-text">{{ scope.row.completionRate || 0 }}%</span>
          </el-progress>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.status === '0'" type="info">草稿</el-tag>
          <el-tag v-else-if="scope.row.status === '1'" type="success">已发布</el-tag>
          <el-tag v-else-if="scope.row.status === '2'" type="warning">已关闭</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建人" align="center" width="120">
        <template #default="scope">
          {{ scope.row.creatorName || scope.row.createBy }}
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180" />
      <el-table-column label="操作" align="center" width="360" fixed="right">
        <template #default="scope">
          <!-- 关闭后仍需保留最终结果查看与导出入口。 -->
          <template v-if="scope.row.status === '2'">
            <el-button link type="primary" icon="View" @click="handlePreview(scope.row)">预览</el-button>
            <el-button link type="primary" icon="DataAnalysis" @click="handleDashboard(scope.row)" v-hasPermi="['business:guideSheet:dashboard']">看板</el-button>
            <el-button link type="primary" icon="CopyDocument" @click="handleCopy(scope.row)">引用</el-button>
          </template>
          <!-- 已发布状态 -->
          <template v-else-if="scope.row.status === '1'">
            <template v-if="String(scope.row.creatorId) === String(userId)">
              <el-button link type="primary" icon="Edit" @click="handleDesign(scope.row)" v-hasPermi="['business:guideSheet:design']">设计</el-button>
            </template>
            <el-button link type="warning" icon="Close" @click="handleClose(scope.row)" v-hasPermi="['business:guideSheet:edit']">关闭</el-button>
            <el-button link type="primary" icon="View" @click="handlePreview(scope.row)">预览</el-button>
            <el-button link type="primary" icon="DataAnalysis" @click="handleDashboard(scope.row)" v-hasPermi="['business:guideSheet:dashboard']">看板</el-button>
            <el-button link type="primary" icon="CopyDocument" @click="handleCopy(scope.row)">引用</el-button>
          </template>
          <!-- 草稿状态 -->
          <template v-else>
            <template v-if="String(scope.row.creatorId) === String(userId)">
              <el-button link type="primary" icon="Edit" @click="handleDesign(scope.row)" v-hasPermi="['business:guideSheet:design']">设计</el-button>
            </template>
            <el-button link type="success" icon="Upload" @click="handlePublish(scope.row)" v-hasPermi="['business:guideSheet:edit']">发布</el-button>
            <el-button link type="primary" icon="View" @click="handlePreview(scope.row)">预览</el-button>
            <el-button link type="primary" icon="CopyDocument" @click="handleCopy(scope.row)">引用</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="GuideSheet">
import { ref, reactive, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { listGuideSheet, delGuideSheet, publishGuideSheet, closeGuideSheet } from '@/api/business/guideSheet'
import { ElMessage, ElMessageBox } from 'element-plus'
import useUserStore from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()

const userId = ref(userStore.id)

const statusOptions = [
  { label: '草稿', value: '0' },
  { label: '已发布', value: '1' },
  { label: '已关闭', value: '2' }
]

const showSearch = ref(true)
const loading = ref(false)
const total = ref(0)
const sheetList = ref([])
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const creatorFilter = ref('')

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  sheetTitle: undefined,
  status: undefined,
  creatorId: undefined
})

function accuracyColor(val) {
  if (!val || val === 0) return '#C0C4CC'
  if (val >= 60) return '#67C23A'
  if (val >= 30) return '#E6A23C'
  return '#F56C6C'
}

function completionColor(val) {
  if (!val || val === 0) return '#C0C4CC'
  if (val >= 80) return '#67C23A'
  if (val >= 40) return '#E6A23C'
  return '#F56C6C'
}

function getList() {
  loading.value = true
  const params = { ...queryParams }
  if (creatorFilter.value === 'self') {
    params.creatorFilter = 'self'
    params.creatorId = undefined
  } else {
    params.creatorFilter = undefined
    params.creatorId = undefined
  }
  listGuideSheet(params).then(response => {
    sheetList.value = response.rows
    total.value = response.total
    loading.value = false
  }).catch(() => {
    loading.value = false
    ElMessage.error('获取导学单列表失败')
  })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.sheetTitle = undefined
  queryParams.status = undefined
  queryParams.creatorId = undefined
  creatorFilter.value = ''
  handleQuery()
}

function onCreatorFilterChange() {
  queryParams.creatorId = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.sheetId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  router.push({ name: 'GuideSheetDesigner', params: { sheetId: undefined } })
}

function handleDesign(row) {
  router.push({ name: 'GuideSheetDesigner', params: { sheetId: row.sheetId } })
}

function handlePreview(row) {
  router.push({ name: 'GuideSheetPreview', params: { sheetId: row.sheetId } })
}

function handleDashboard(row) {
  router.push({ name: 'GuideSheetDashboard', params: { sheetId: row.sheetId } })
}

function handleCopy(row) {
  router.push({ name: 'GuideSheetDesigner', params: { sheetId: undefined }, query: { copyFrom: row.sheetId } })
}

function handlePublish(row) {
  ElMessageBox.confirm('确认发布导学单「' + row.sheetTitle + '」？发布后学生即可查看。', '发布确认', { type: 'warning' })
    .then(() => publishGuideSheet(row.sheetId)).then(() => { ElMessage.success('发布成功'); getList() })
    .catch((err) => {
      // 区分用户取消和 API 错误：ElMessageBox.confirm 取消/关闭返回字符串 'cancel'/'close'
      if (err !== 'cancel' && err !== 'close') {
        const msg = err?.response?.data?.msg || err?.msg || err?.message || '发布失败，请检查表单内容和班级指派'
        ElMessage.error(typeof msg === 'string' ? msg : '发布失败')
      }
    })
}

function handleClose(row) {
  ElMessageBox.confirm('确认关闭导学单「' + row.sheetTitle + '」？关闭后学生将无法查看。', '关闭确认', { type: 'warning' })
    .then(() => closeGuideSheet(row.sheetId)).then(() => { ElMessage.success('已关闭'); getList() })
    .catch((err) => {
      if (err !== 'cancel' && err !== 'close') {
        const msg = err?.response?.data?.msg || err?.msg || err?.message || '关闭失败'
        ElMessage.error(typeof msg === 'string' ? msg : '关闭失败')
      }
    })
}

function handleDelete() {
  const sheetIds = ids.value.join(',')
  ElMessageBox.confirm('确认删除所选导学单？删除后不可恢复。', '删除确认', { type: 'warning' })
    .then(() => delGuideSheet(sheetIds)).then(() => { ElMessage.success('删除成功'); getList() })
    .catch((err) => {
      if (err !== 'cancel' && err !== 'close') {
        const msg = err?.response?.data?.msg || err?.msg || err?.message || '删除失败'
        ElMessage.error(typeof msg === 'string' ? msg : '删除失败')
      }
    })
}

onMounted(() => {
  getList()
})

onActivated(() => {
  getList()
})
</script>

<style scoped>
.progress-text {
  font-size: 11px;
}
</style>
