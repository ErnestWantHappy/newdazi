<template>
  <div class="app-container teacher-tools-manage" data-testid="teacher-tools-manage-page">
    <header class="manage-head">
      <div>
        <el-button link icon="ArrowLeft" @click="router.push('/teacher-tools')">返回教师工具</el-button>
        <h2>教师工具管理</h2>
        <p>维护分类、推荐顺序和上下架状态；变更保存后立即影响教师端导航。</p>
      </div>
    </header>

    <el-tabs v-model="activeTab" class="manage-tabs">
      <el-tab-pane label="工具管理" name="tools">
        <el-card shadow="never">
          <el-form :model="query" inline class="query-form">
            <el-form-item label="关键词"><el-input v-model="query.keyword" clearable placeholder="名称、说明或标签" @keyup.enter="searchTools" /></el-form-item>
            <el-form-item label="分类">
              <el-select v-model="query.categoryId" clearable placeholder="全部分类" style="width: 180px">
                <el-option v-for="item in categories" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态"><el-select v-model="query.status" clearable placeholder="全部" style="width: 110px"><el-option label="已上架" value="0" /><el-option label="已下架" value="1" /></el-select></el-form-item>
            <el-form-item label="删除"><el-select v-model="query.delFlag" clearable placeholder="未删除" style="width: 110px"><el-option label="未删除" value="0" /><el-option label="已删除" value="2" /></el-select></el-form-item>
            <el-form-item><el-button type="primary" icon="Search" @click="searchTools">查询</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
          </el-form>
          <el-button v-hasPermi="['business:teacherTool:manage']" type="primary" plain icon="Plus" data-testid="teacher-tool-add" @click="openToolDialog()">新增工具</el-button>
          <el-table v-loading="toolLoading" :data="tools" class="data-table">
            <el-table-column label="工具" min-width="220">
              <template #default="scope"><strong>{{ scope.row.title }}</strong><div class="subline">{{ scope.row.description }}</div></template>
            </el-table-column>
            <el-table-column prop="categoryNameText" label="所属分类" min-width="180" show-overflow-tooltip />
            <el-table-column label="访问方式" width="100"><template #default="scope">{{ accessLabels[scope.row.accessType] || scope.row.accessType }}</template></el-table-column>
            <el-table-column label="推荐" width="80"><template #default="scope"><el-tag v-if="scope.row.isRecommended === 'Y'" type="warning">推荐</el-tag><span v-else>-</span></template></el-table-column>
            <el-table-column prop="sortOrder" label="排序" width="70" />
            <el-table-column label="状态" width="90"><template #default="scope"><el-switch v-model="scope.row.status" active-value="0" inactive-value="1" :disabled="scope.row.delFlag === '2'" @change="changeToolStatus(scope.row)" /></template></el-table-column>
            <el-table-column label="操作" width="190" fixed="right">
              <template #default="scope">
                <el-button v-if="scope.row.delFlag !== '2'" link type="primary" @click="openToolDialog(scope.row)">编辑</el-button>
                <el-button v-if="scope.row.delFlag !== '2'" link type="danger" @click="removeTool(scope.row)">删除</el-button>
                <el-button v-else link type="success" @click="restoreToolRow(scope.row)">恢复</el-button>
              </template>
            </el-table-column>
          </el-table>
          <pagination v-show="total > 0" :total="total" v-model:page="query.pageNum" v-model:limit="query.pageSize" @pagination="loadTools" />
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="分类管理" name="categories">
        <el-card shadow="never">
          <el-button type="primary" plain icon="Plus" data-testid="teacher-tool-category-add" @click="openCategoryDialog()">新增分类</el-button>
          <el-table v-loading="categoryLoading" :data="categories" class="data-table">
            <el-table-column prop="categoryName" label="分类名称" min-width="150" />
            <el-table-column prop="categoryCode" label="编码" min-width="150" />
            <el-table-column label="层级" width="100"><template #default="scope"><el-tag :type="scope.row.sectionLevel === 'PRIMARY' ? 'primary' : 'info'">{{ scope.row.sectionLevel === 'PRIMARY' ? '重点' : '次要' }}</el-tag></template></el-table-column>
            <el-table-column label="默认展开" width="100"><template #default="scope">{{ scope.row.defaultExpanded === 'Y' ? '是' : '否' }}</template></el-table-column>
            <el-table-column prop="previewLimit" label="预览数" width="80" />
            <el-table-column prop="sortOrder" label="排序" width="70" />
            <el-table-column label="状态" width="90"><template #default="scope"><el-switch v-model="scope.row.status" active-value="0" inactive-value="1" @change="changeCategoryStatus(scope.row)" /></template></el-table-column>
            <el-table-column label="操作" width="90"><template #default="scope"><el-button link type="primary" @click="openCategoryDialog(scope.row)">编辑</el-button></template></el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="toolDialogVisible" :title="toolForm.toolId ? '编辑工具' : '新增工具'" width="760px" append-to-body destroy-on-close>
      <el-form ref="toolFormRef" :model="toolForm" :rules="toolRules" label-width="100px" data-testid="teacher-tool-form">
        <el-row :gutter="18">
          <el-col :span="12"><el-form-item label="工具名称" prop="title"><el-input v-model="toolForm.title" maxlength="100" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="所属分类" prop="categoryIds"><el-select v-model="toolForm.categoryIds" multiple filterable collapse-tags style="width: 100%"><el-option v-for="item in enabledCategories" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" /></el-select></el-form-item></el-col>
        </el-row>
        <el-form-item label="用途说明" prop="description"><el-input v-model="toolForm.description" type="textarea" :rows="2" maxlength="500" show-word-limit /></el-form-item>
        <el-form-item label="工具地址" prop="url"><el-input v-model="toolForm.url" placeholder="仅允许 http:// 或 https://" /></el-form-item>
        <el-form-item label="图标地址" prop="iconUrl"><el-input v-model="toolForm.iconUrl" placeholder="可不填；失败时显示内置工具图标" /></el-form-item>
        <el-form-item label="标签"><el-input v-model="toolForm.tags" placeholder="多个标签用逗号分隔" /></el-form-item>
        <el-row :gutter="18">
          <el-col :span="8"><el-form-item label="访问方式"><el-select v-model="toolForm.accessType"><el-option v-for="(label, value) in accessLabels" :key="value" :label="label" :value="value" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="来源"><el-select v-model="toolForm.sourceType"><el-option v-for="(label, value) in sourceLabels" :key="value" :label="label" :value="value" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="来源标识"><el-input v-model="toolForm.sourceRef" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="18">
          <el-col :span="8"><el-form-item label="工具排序"><el-input-number v-model="toolForm.sortOrder" :min="0" controls-position="right" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="常用推荐"><el-switch v-model="toolForm.isRecommended" active-value="Y" inactive-value="N" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="推荐排序"><el-input-number v-model="toolForm.recommendOrder" :min="0" controls-position="right" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="上架状态"><el-radio-group v-model="toolForm.status"><el-radio value="0">上架</el-radio><el-radio value="1">下架</el-radio></el-radio-group></el-form-item>
      </el-form>
      <template #footer><el-button @click="toolDialogVisible = false">取消</el-button><el-button type="primary" :loading="submitting" data-testid="teacher-tool-submit" @click="submitTool">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="categoryDialogVisible" :title="categoryForm.categoryId ? '编辑分类' : '新增分类'" width="620px" append-to-body destroy-on-close>
      <el-form ref="categoryFormRef" :model="categoryForm" :rules="categoryRules" label-width="100px" data-testid="teacher-tool-category-form">
        <el-form-item label="分类名称" prop="categoryName"><el-input v-model="categoryForm.categoryName" maxlength="50" /></el-form-item>
        <el-form-item label="分类编码" prop="categoryCode"><el-input v-model="categoryForm.categoryCode" maxlength="32" placeholder="小写字母、数字或短横线" /></el-form-item>
        <el-form-item label="分类说明"><el-input v-model="categoryForm.description" type="textarea" maxlength="200" /></el-form-item>
        <el-row :gutter="18">
          <el-col :span="12"><el-form-item label="层级"><el-radio-group v-model="categoryForm.sectionLevel"><el-radio value="PRIMARY">重点</el-radio><el-radio value="SECONDARY">次要</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="默认展开"><el-switch v-model="categoryForm.defaultExpanded" active-value="Y" inactive-value="N" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="18">
          <el-col :span="8"><el-form-item label="预览数量"><el-input-number v-model="categoryForm.previewLimit" :min="1" :max="20" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="排序"><el-input-number v-model="categoryForm.sortOrder" :min="0" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="图标"><el-input v-model="categoryForm.icon" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="状态"><el-radio-group v-model="categoryForm.status"><el-radio value="0">启用</el-radio><el-radio value="1">停用</el-radio></el-radio-group></el-form-item>
      </el-form>
      <template #footer><el-button @click="categoryDialogVisible = false">取消</el-button><el-button type="primary" :loading="submitting" @click="submitCategory">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createTeacherTool, createTeacherToolCategory, deleteTeacherTool, getTeacherTool,
  listTeacherToolCategories, listTeacherTools, restoreTeacherTool, updateTeacherTool,
  updateTeacherToolCategory, updateTeacherToolCategoryStatus, updateTeacherToolStatus
} from '@/api/business/teacherTools.js'
import { isSafeHttpUrl } from './teacherToolsUtils.js'

const router = useRouter()
const activeTab = ref('tools')
const categories = ref([])
const tools = ref([])
const total = ref(0)
const toolLoading = ref(false)
const categoryLoading = ref(false)
const submitting = ref(false)
const toolDialogVisible = ref(false)
const categoryDialogVisible = ref(false)
const toolFormRef = ref()
const categoryFormRef = ref()
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', categoryId: undefined, status: '', delFlag: '0' })
const accessLabels = { DIRECT: '直接访问', LOGIN_REQUIRED: '需登录', INTRANET_ONLY: '限内网', DOWNLOAD: '下载工具' }
const sourceLabels = { MANUAL: '平台维护', LOCAL_3005: '原3005站', LOCAL_80: '原80根站', ZJ_DISCIPLINE: '省学科平台' }
const emptyTool = () => ({ toolId: undefined, title: '', description: '', url: '', iconUrl: '', tags: '', accessType: 'DIRECT', sourceType: 'MANUAL', sourceRef: '', isRecommended: 'N', recommendOrder: 100, sortOrder: 100, status: '0', categoryIds: [] })
const emptyCategory = () => ({ categoryId: undefined, categoryCode: '', categoryName: '', description: '', icon: 'tool', sectionLevel: 'SECONDARY', defaultExpanded: 'N', previewLimit: 4, sortOrder: 100, status: '0' })
const toolForm = reactive(emptyTool())
const categoryForm = reactive(emptyCategory())
const enabledCategories = computed(() => categories.value.filter(item => item.status === '0'))
const httpValidator = (_rule, value, callback) => !value || isSafeHttpUrl(value) ? callback() : callback(new Error('仅允许不含账号密码的 http/https 地址'))
const toolRules = {
  title: [{ required: true, message: '请输入工具名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入用途说明', trigger: 'blur' }],
  url: [{ required: true, message: '请输入工具地址', trigger: 'blur' }, { validator: httpValidator, trigger: 'blur' }],
  iconUrl: [{ validator: httpValidator, trigger: 'blur' }],
  categoryIds: [{ required: true, type: 'array', min: 1, message: '请至少选择一个分类', trigger: 'change' }]
}
const categoryRules = {
  categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  categoryCode: [{ required: true, message: '请输入分类编码', trigger: 'blur' }, { pattern: /^[a-z][a-z0-9-]{1,31}$/, message: '请输入2至32位小写字母、数字或短横线', trigger: 'blur' }]
}

async function loadCategories() {
  categoryLoading.value = true
  try { categories.value = (await listTeacherToolCategories()).data || [] } finally { categoryLoading.value = false }
}
async function loadTools() {
  toolLoading.value = true
  try {
    const response = await listTeacherTools(query)
    tools.value = response.rows || []
    total.value = response.total || 0
  } finally { toolLoading.value = false }
}
function searchTools() { query.pageNum = 1; loadTools() }
function resetQuery() { Object.assign(query, { pageNum: 1, pageSize: 10, keyword: '', categoryId: undefined, status: '', delFlag: '0' }); loadTools() }
async function openToolDialog(row) {
  Object.assign(toolForm, emptyTool())
  if (row?.toolId) Object.assign(toolForm, (await getTeacherTool(row.toolId)).data)
  toolDialogVisible.value = true
  nextTick(() => toolFormRef.value?.clearValidate())
}
function openCategoryDialog(row) {
  Object.assign(categoryForm, emptyCategory(), row || {})
  categoryDialogVisible.value = true
  nextTick(() => categoryFormRef.value?.clearValidate())
}
async function submitTool() {
  if (!await toolFormRef.value.validate().catch(() => false)) return
  submitting.value = true
  try {
    const action = toolForm.toolId ? updateTeacherTool(toolForm.toolId, toolForm) : createTeacherTool(toolForm)
    await action
    ElMessage.success('工具已保存')
    toolDialogVisible.value = false
    await loadTools()
  } finally { submitting.value = false }
}
async function submitCategory() {
  if (!await categoryFormRef.value.validate().catch(() => false)) return
  submitting.value = true
  try {
    const action = categoryForm.categoryId ? updateTeacherToolCategory(categoryForm.categoryId, categoryForm) : createTeacherToolCategory(categoryForm)
    await action
    ElMessage.success('分类已保存')
    categoryDialogVisible.value = false
    await loadCategories()
  } finally { submitting.value = false }
}
async function changeToolStatus(row) { await updateTeacherToolStatus(row.toolId, row.status); ElMessage.success(row.status === '0' ? '已上架' : '已下架') }
async function changeCategoryStatus(row) { await updateTeacherToolCategoryStatus(row.categoryId, row.status); ElMessage.success(row.status === '0' ? '分类已启用' : '分类已停用') }
async function removeTool(row) { await ElMessageBox.confirm(`确认删除“${row.title}”吗？删除后可恢复。`, '删除工具', { type: 'warning' }); await deleteTeacherTool(row.toolId); ElMessage.success('已删除'); await loadTools() }
async function restoreToolRow(row) { await restoreTeacherTool(row.toolId); ElMessage.success('已恢复'); await loadTools() }

onMounted(async () => { await loadCategories(); await loadTools() })
</script>

<style scoped>
.teacher-tools-manage { max-width: 1500px; margin: 0 auto; }.manage-head { margin-bottom: 14px; }.manage-head h2 { margin: 8px 0 6px; font-size: 24px; }.manage-head p { margin: 0; color: var(--el-text-color-secondary); }.manage-tabs { min-height: 520px; }.query-form { margin-bottom: -8px; }.data-table { margin-top: 16px; }.subline { margin-top: 4px; max-width: 560px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: var(--el-text-color-secondary); font-size: 12px; }
@media (max-width: 768px) { :deep(.el-dialog) { width: 96% !important; }.query-form :deep(.el-form-item) { display: flex; margin-right: 0; }.query-form :deep(.el-form-item__content) { flex: 1; }.query-form :deep(.el-input), .query-form :deep(.el-select) { width: 100% !important; } }
</style>
