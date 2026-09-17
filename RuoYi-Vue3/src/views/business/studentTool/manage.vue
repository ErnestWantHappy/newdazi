<template>
  <div class="app-container student-tool-manage" data-testid="student-tool-manage-page">
    <header class="manage-head">
      <el-button link icon="ArrowLeft" @click="router.push('/student-tool')">返回学生实验工具</el-button>
      <h2>学生实验工具管理</h2>
      <p>配置学生端可用的常驻工具，按“入学年份 + 班级”生效；也可选择整个年级。保存后学生端顶部“学生实验工具”面板立即生效。</p>
    </header>

    <el-card shadow="never">
      <el-form :model="query" inline class="query-form">
        <el-form-item label="关键词"><el-input v-model="query.keyword" clearable placeholder="工具名称或网址" @keyup.enter="loadTools" /></el-form-item>
        <el-form-item><el-button type="primary" icon="Search" @click="loadTools">查询</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
      </el-form>

      <el-button v-hasPermi="['business:studentTool:manage']" type="primary" plain icon="Plus" data-testid="student-tool-add" @click="openDialog()">新增工具</el-button>

      <el-table v-loading="loading" :data="tools" class="data-table">
        <el-table-column label="工具" min-width="240">
          <template #default="scope"><strong>{{ scope.row.toolName }}</strong><div class="subline">{{ scope.row.toolUrl }}</div></template>
        </el-table-column>
        <el-table-column prop="toolDesc" label="说明" min-width="160" show-overflow-tooltip />
        <el-table-column label="适用范围" min-width="200" show-overflow-tooltip>
          <template #default="scope">{{ formatScopes(scope.row) }}</template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="70" />
        <el-table-column label="状态" width="90"><template #default="scope"><el-tag :type="scope.row.enabled == 1 ? 'success' : 'info'">{{ scope.row.enabled == 1 ? '启用' : '停用' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="openDialog(scope.row)">编辑</el-button>
            <el-button link type="danger" @click="removeTool(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && tools.length === 0" description="还没有配置常驻工具" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.toolId ? '编辑工具' : '新增工具'" width="760px" append-to-body destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="18">
          <el-col :span="12"><el-form-item label="工具名称" prop="toolName"><el-input v-model="form.toolName" maxlength="100" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="工具排序"><el-input-number v-model="form.sortOrder" :min="0" controls-position="right" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="工具网址" prop="toolUrl"><el-input v-model="form.toolUrl" placeholder="http:// 或 https:// 开头" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="form.toolDesc" type="textarea" :rows="2" maxlength="255" show-word-limit placeholder="可选，学生端可见" /></el-form-item>
        <el-divider content-position="left">适用范围</el-divider>
        <el-alert type="info" :closable="false" show-icon title="按入学年份 + 班级生效；勾选“整个年级”则该届全部班级可见。" style="margin-bottom: 12px" />
        <div v-for="(group, gi) in scopeGroups" :key="gi" class="scope-group">
          <el-row :gutter="12" align="middle">
            <el-col :span="7">
              <el-select v-model="group.entryYear" placeholder="选择入学年份/级" filterable style="width: 100%">
                <el-option v-for="y in yearOptions" :key="y" :label="y + '级'" :value="y" />
              </el-select>
            </el-col>
            <el-col :span="5"><el-checkbox v-model="group.allGrade">整个年级</el-checkbox></el-col>
            <el-col :span="10">
              <el-select v-if="!group.allGrade" v-model="group.classCodes" multiple filterable collapse-tags placeholder="选择班级" style="width: 100%">
                <el-option v-for="c in classOptionsOf(group.entryYear)" :key="c.classCode" :label="c.classCode + '班'" :value="c.classCode" />
              </el-select>
            </el-col>
            <el-col :span="2"><el-button link type="danger" icon="Delete" @click="removeScopeGroup(gi)" /></el-col>
          </el-row>
        </div>
        <el-button size="small" type="primary" plain icon="Plus" @click="addScopeGroup">添加适用年级</el-button>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" :loading="submitting" data-testid="student-tool-submit" @click="submitForm">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup name="StudentToolManage">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listStudentTools, addStudentTool, updateStudentTool, delStudentTool } from '@/api/business/studentTool'
import { getScoreClasses } from '@/api/business/score'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const tools = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const query = reactive({ keyword: '' })
// 范围分组：{ entryYear, allGrade, classCodes }
const scopeGroups = ref([])
const yearOptions = ref([])
const allClasses = ref([])
const form = reactive(emptyForm())
const rules = {
  toolName: [{ required: true, message: '请输入工具名称', trigger: 'blur' }],
  toolUrl: [{ required: true, message: '请输入工具网址', trigger: 'blur' }],
}

function emptyForm() {
  return { toolId: null, toolName: '', toolUrl: '', toolDesc: '', sortOrder: 0, enabled: 1 }
}

async function loadTools() {
  loading.value = true
  try {
    const res = await listStudentTools(query.keyword || undefined)
    tools.value = (res.data || []).map(t => ({ ...t, scopeText: t.scopeText || '' }))
  } finally { loading.value = false }
}
function resetQuery() { query.keyword = ''; loadTools() }

function formatScopes(row) {
  // 适用范围展示由后端 scopeText 提供（可选），无则显示全部
  return row.scopeText || '（未配置范围则对所有学生可见的方案未启用，请编辑补充）'
}

function classOptionsOf(entryYear) {
  return allClasses.value.filter(c => String(c.entryYear || c.entry_year) === String(entryYear))
}

function addScopeGroup(entryYear, allGrade, classCodes) {
  scopeGroups.value.push({ entryYear: entryYear || '', allGrade: !!allGrade, classCodes: classCodes || [] })
}
function removeScopeGroup(gi) { scopeGroups.value.splice(gi, 1) }

function openDialog(row) {
  Object.assign(form, emptyForm(), row ? { toolId: row.toolId, toolName: row.toolName, toolUrl: row.toolUrl, toolDesc: row.toolDesc, sortOrder: row.sortOrder ?? 0, enabled: row.enabled ?? 1 } : {})
  scopeGroups.value = []
  if (row && row.scopes && row.scopes.length) {
    const grouped = {}
    row.scopes.forEach(s => {
      if (!grouped[s.entryYear]) grouped[s.entryYear] = { entryYear: s.entryYear, allGrade: false, classCodes: [] }
      if (!s.classCode) grouped[s.entryYear].allGrade = true
      else grouped[s.entryYear].classCodes.push(s.classCode)
    })
    scopeGroups.value = Object.values(grouped)
  }
  if (!scopeGroups.value.length) addScopeGroup()
  dialogVisible.value = true
}

async function submitForm() {
  await formRef.value.validate()
  const scopes = scopeGroups.value
    .filter(g => g.entryYear)
    .map(g => (g.allGrade ? { entryYear: g.entryYear, allGrade: true } : { entryYear: g.entryYear, allGrade: false, classCodes: g.classCodes }))
  if (scopes.length === 0) {
    ElMessage.warning('请至少配置一个应用范围（选择入学年份并勾选班级或整个年级）')
    return
  }
  submitting.value = true
  try {
    const payload = { tool: { ...form }, scopes }
    if (form.toolId) await updateStudentTool(payload)
    else await addStudentTool(payload)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadTools()
  } finally { submitting.value = false }
}

async function removeTool(row) {
  await ElMessageBox.confirm(`确认删除工具【${row.toolName}】吗？删除后学生端不再显示。`, '删除确认', { type: 'warning' })
  await delStudentTool(row.toolId)
  ElMessage.success('已删除')
  await loadTools()
}

onMounted(async () => {
  await loadTools()
  try {
    const clsRes = await getScoreClasses()
    allClasses.value = clsRes.data || []
    // 年份从班级数据中推导（去重并保持数值序）
    const years = [...new Set((allClasses.value || []).map(c => c.entry_year ?? c.entryYear).filter(Boolean))]
    yearOptions.value = years.sort((a, b) => Number(a) - Number(b))
  } catch (e) { /* 年份/班级加载失败不阻塞工具列表 */ }
})
</script>

<style scoped>
.manage-head { margin-bottom: 16px; }
.manage-head h2 { margin: 8px 0 6px; }
.manage-head p { color: #909399; font-size: 13px; }
.query-form { margin-bottom: 10px; }
.data-table { margin-top: 10px; }
.subline { color: #909399; font-size: 12px; word-break: break-all; }
.scope-group { margin-bottom: 10px; }
</style>