<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 左侧：我管理的班级 -->
      <el-col :span="14">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>我管理的班级</span>
            </div>
          </template>
          <el-table v-loading="loading" :data="classList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="入学年份" align="center" prop="entryYear" width="100" />
            <el-table-column label="班级" align="center" prop="classCode" width="80">
              <template #default="scope">
                {{ scope.row.classCode }}班
              </template>
            </el-table-column>
            <el-table-column label="学生人数" align="center" prop="studentCount" width="100">
              <template #default="scope">
                <el-tag type="info">{{ scope.row.studentCount || 0 }}人</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="添加时间" align="center" prop="createTime" width="160" />
            <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
              <template #default="scope">
                <el-button link type="primary" icon="User" @click="handleStudentManage(scope.row)">
                  学生管理
                </el-button>
                <el-button link type="danger" @click="handleDelete(scope.row)" v-hasPermi="['business:teacherClass:remove']">
                  移除管理
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div v-if="selectedIds.length > 0" style="margin-top: 10px;">
            <el-button type="danger" size="small" @click="handleBatchDelete" v-hasPermi="['business:teacherClass:remove']">
              批量移除 ({{ selectedIds.length }})
            </el-button>
          </div>
        </el-card>
      </el-col>
      
      <!-- 右侧：可选班级 -->
      <el-col :span="10">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>可选班级（本校）</span>
              <el-button size="small" @click="getAvailableList">
                <el-icon><Refresh /></el-icon>刷新
              </el-button>
            </div>
          </template>
          <el-table v-loading="availableLoading" :data="availableList" @selection-change="handleAvailableSelectionChange">
            <el-table-column type="selection" width="55" align="center" :selectable="checkSelectable" />
            <el-table-column label="入学年份" align="center" prop="entryYear" width="100" />
            <el-table-column label="班级" align="center" prop="classCode" width="80">
              <template #default="scope">
                {{ scope.row.classCode }}班
              </template>
            </el-table-column>
            <el-table-column label="学生人数" align="center" prop="studentCount" width="100">
              <template #default="scope">
                <el-tag type="success">{{ scope.row.studentCount || 0 }}人</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" align="center" width="100">
              <template #default="scope">
                <el-tag v-if="isManaged(scope.row)" type="info">已管理</el-tag>
                <el-tag v-else type="warning">未管理</el-tag>
              </template>
            </el-table-column>
          </el-table>
          
          <div v-if="selectedAvailableList.length > 0" style="margin-top: 10px;">
            <el-button type="primary" size="small" @click="handleBatchAdd" v-hasPermi="['business:teacherClass:add']">
              添加选中 ({{ selectedAvailableList.length }})
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="TeacherClass">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listTeacherClass, getAvailableClasses, addTeacherClass, batchAddTeacherClass, delTeacherClass } from '@/api/business/teacherClass'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const availableLoading = ref(false)
const classList = ref([])
const availableList = ref([])
const selectedIds = ref([])
const selectedAvailableList = ref([])

/** 查询我管理的班级列表 */
function getList() {
  loading.value = true
  listTeacherClass().then(response => {
    classList.value = response.rows
    loading.value = false
  })
}

/** 查询可选班级列表 */
function getAvailableList() {
  availableLoading.value = true
  getAvailableClasses().then(response => {
    availableList.value = response.data
    availableLoading.value = false
  })
}

/** 检查班级是否已被管理 */
function isManaged(row) {
  return classList.value.some(item => 
    item.entryYear === row.entryYear && item.classCode === row.classCode
  )
}

/** 可选班级表格：禁用已管理的班级 */
function checkSelectable(row) {
  return !isManaged(row)
}

/** 我的班级多选框 */
function handleSelectionChange(selection) {
  selectedIds.value = selection.map(item => item.id)
}

/** 可选班级多选框 */
function handleAvailableSelectionChange(selection) {
  selectedAvailableList.value = selection.filter(item => !isManaged(item))
}

/** 批量添加班级 */
function handleBatchAdd() {
  if (selectedAvailableList.value.length === 0) {
    ElMessage.warning('请选择要添加的班级')
    return
  }
  const data = selectedAvailableList.value.map(item => ({
    entryYear: item.entryYear,
    classCode: item.classCode
  }))
  batchAddTeacherClass(data).then(() => {
    ElMessage.success('添加成功')
    getList()
    getAvailableList()
  })
}

/** 删除单个班级管理 */
function handleDelete(row) {
  ElMessageBox.confirm('确认移除对"' + row.entryYear + '级' + row.classCode + '班"的管理吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    delTeacherClass(row.id).then(() => {
      ElMessage.success('移除成功')
      getList()
      getAvailableList()
    })
  })
}

/** 批量删除 */
function handleBatchDelete() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要移除的班级')
    return
  }
  ElMessageBox.confirm('确认移除选中的' + selectedIds.value.length + '个班级管理吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    delTeacherClass(selectedIds.value.join(',')).then(() => {
      ElMessage.success('批量移除成功')
      getList()
      getAvailableList()
    })
  })
}

/** 跳转到学生管理 */
function handleStudentManage(row) {
  router.push({
    path: '/studentguanli',
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
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
