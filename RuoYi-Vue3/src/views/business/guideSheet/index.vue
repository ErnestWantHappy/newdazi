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
      <el-table-column label="导学单标题" align="left" prop="sheetTitle" show-overflow-tooltip />
      <el-table-column label="教师机IP" align="center" prop="teacherMachineIp" width="150" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.status === '0'" type="info">草稿</el-tag>
          <el-tag v-else-if="scope.row.status === '1'" type="success">已发布</el-tag>
          <el-tag v-else-if="scope.row.status === '2'" type="warning">已关闭</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建人" align="center" prop="createBy" width="120" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180" />
      <el-table-column label="操作" align="center" width="280" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleDesign(scope.row)" v-hasPermi="['business:guideSheet:design']">设计</el-button>
          <el-button link type="primary" icon="View" @click="handlePreview(scope.row)">预览</el-button>
          <el-button v-if="scope.row.status === '0'" link type="success" icon="Upload" @click="handlePublish(scope.row)">发布</el-button>
          <el-button v-if="scope.row.status === '1'" link type="warning" icon="Close" @click="handleClose(scope.row)">关闭</el-button>
          <el-button link type="primary" icon="DataAnalysis" @click="handleDashboard(scope.row)" v-hasPermi="['business:guideSheet:dashboard']">看板</el-button>
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

const router = useRouter()

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

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  sheetTitle: undefined,
  status: undefined
})

function getList() {
  loading.value = true
  listGuideSheet(queryParams).then(response => {
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

function handlePublish(row) {
  ElMessageBox.confirm('确认发布导学单「' + row.sheetTitle + '」？发布后学生即可查看。', '发布确认', { type: 'warning' })
    .then(() => publishGuideSheet(row.sheetId)).then(() => { ElMessage.success('发布成功'); getList() })
}

function handleClose(row) {
  ElMessageBox.confirm('确认关闭导学单「' + row.sheetTitle + '」？关闭后学生将无法查看。', '关闭确认', { type: 'warning' })
    .then(() => closeGuideSheet(row.sheetId)).then(() => { ElMessage.success('已关闭'); getList() })
}

function handleDelete() {
  const sheetIds = ids.value.join(',')
  ElMessageBox.confirm('确认删除所选导学单？删除后不可恢复。', '删除确认', { type: 'warning' })
    .then(() => delGuideSheet(sheetIds)).then(() => { ElMessage.success('删除成功'); getList() })
}

onMounted(() => {
  getList()
})

onActivated(() => {
  getList()
})
</script>
