<!DOCTYPE html>
<template>
  <div class="app-container student-tool-page">
    <header class="page-head">
      <div>
        <h2>学生实验工具</h2>
        <p>配置学生端顶部“学生实验工具”面板中的常驻工具，按入学年份 + 班级生效。</p>
      </div>
      <el-button v-if="canManage" type="primary" icon="Setting" plain @click="router.push('/student-tool/manage')">工具管理</el-button>
    </header>

    <el-card shadow="never" class="tip-card">
      <el-alert type="info" :closable="false" show-icon
        title="学生登录后，顶部导航栏点击“学生实验工具”，面板先显示本节课工具（课程设计中配置），再显示这里配置的常驻工具。"
        style="margin: 10px 0" />
    </el-card>

    <el-card shadow="never" v-loading="loading">
      <el-input v-model="keyword" clearable placeholder="搜索工具名称" style="max-width: 320px; margin-bottom: 12px" @keyup.enter="loadTools">
        <template #append><el-button icon="Search" @click="loadTools" /></template>
      </el-input>
      <div class="tool-grid">
        <div v-for="tool in tools" :key="tool.toolId" class="tool-card">
          <div class="tool-main">
            <h4>{{ tool.toolName }}</h4>
            <p v-if="tool.toolDesc">{{ tool.toolDesc }}</p>
            <el-tag v-if="tool.enabled == 1" type="success" size="small">启用</el-tag>
            <el-tag v-else type="info" size="small">停用</el-tag>
          </div>
          <el-link type="primary" :href="tool.toolUrl" target="_blank" rel="noopener noreferrer">
            打开<el-icon><TopRight /></el-icon>
          </el-link>
        </div>
      </div>
      <el-empty v-if="!loading && tools.length === 0" description="尚未配置常驻工具，点击右上角“工具管理”开始配置" />
    </el-card>
  </div>
</template>

<script setup name="StudentTool">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { TopRight } from '@element-plus/icons-vue'
import useUserStore from '@/store/modules/user'
import { listStudentTools } from '@/api/business/studentTool.js'

const router = useRouter()
const userStore = useUserStore()
const canManage = computed(() => userStore.permissions.includes('business:studentTool:manage'))
const loading = ref(false)
const keyword = ref('')
const tools = ref([])

async function loadTools() {
  loading.value = true
  try {
    const res = await listStudentTools(keyword.value || undefined)
    tools.value = res.data || []
  } finally { loading.value = false }
}
onMounted(loadTools)
</script>

<style scoped>
.page-head { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; margin: 4px 0 18px; }
.page-head h2 { margin: 0 0 6px; font-size: 22px; }
.page-head p { margin: 0; color: #909399; font-size: 13px; }
.tip-card { margin-bottom: 14px; }
.tool-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 12px; }
.tool-card { padding: 14px 16px; border: 1px solid #ebeef5; border-radius: 8px; display: flex; align-items: center; justify-content: space-between; gap: 10px; background: #fff; }
.tool-card:hover { border-color: #409eff; }
.tool-main h4 { margin: 0 0 4px; font-size: 15px; }
.tool-main p { margin: 0 0 6px; color: #909399; font-size: 12px; }
</style>
