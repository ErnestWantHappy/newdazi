<template>
  <div class="app-container">
    <el-alert title="按最近5分钟有活动的账号统计，同一账号只计1人；关闭页面或断网后最多5分钟移出。" type="info" :closable="false" class="online-note" />
    <el-form :model="queryParams" ref="queryRef" :inline="true" @submit.prevent="handleQuery">
      <el-form-item label="所属部门" prop="deptId">
        <el-tree-select v-model="queryParams.deptId" :data="deptOptions" node-key="id" check-strictly filterable clearable
          placeholder="全部部门（包含下级）" style="width: 240px" />
      </el-form-item>
      <el-form-item label="登录地址" prop="ipaddr">
        <el-input v-model="queryParams.ipaddr" placeholder="请输入登录地址" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="登录名称" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="请输入账号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <div class="online-summary">
      <span>当前在线 <strong>{{ onlineTotal }}</strong> 人</span>
      <span>筛选结果 <strong>{{ total }}</strong> 人</span>
      <span class="sample-time">统计时间：{{ sampledAt ? parseTime(sampledAt) : '尚未加载' }}</span>
    </div>
    <el-table v-loading="loading" :data="onlineList">
      <el-table-column label="序号" type="index" width="60" :index="index => (queryParams.pageNum - 1) * queryParams.pageSize + index + 1" />
      <el-table-column label="登录名称" prop="userName" show-overflow-tooltip />
      <el-table-column label="姓名" prop="nickName" show-overflow-tooltip />
      <el-table-column label="所属部门" prop="deptName" show-overflow-tooltip />
      <el-table-column label="最近活动地址" prop="ipaddr" show-overflow-tooltip />
      <el-table-column label="操作系统" prop="os" show-overflow-tooltip />
      <el-table-column label="浏览器" prop="browser" show-overflow-tooltip />
      <el-table-column label="最近活动时间" width="180">
        <template #default="scope">{{ parseTime(scope.row.lastSeenAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <el-button link type="primary" @click="handleForceLogout(scope.row)" v-hasPermi="['monitor:online:forceLogout']">强退最近会话</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="Online">
import { forceLogout, list, deptTree } from '@/api/monitor/online'
const { proxy } = getCurrentInstance()
const onlineList = ref([])
const deptOptions = ref([])
const loading = ref(false)
const total = ref(0)
const onlineTotal = ref(0)
const sampledAt = ref(null)
const queryParams = ref({ pageNum: 1, pageSize: 10, deptId: undefined, ipaddr: undefined, userName: undefined })
let requestId = 0
async function getList() {
  const id = ++requestId
  loading.value = true
  try {
    const response = await list(queryParams.value)
    if (id !== requestId) return
    onlineList.value = response.rows || []
    total.value = response.total
    onlineTotal.value = response.onlineTotal
    sampledAt.value = response.sampledAt
  } finally {
    if (id === requestId) loading.value = false
  }
}
function handleQuery() { queryParams.value.pageNum = 1; getList().catch(() => {}) }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
async function handleForceLogout(row) {
  try {
    await proxy.$modal.confirm(`确认退出“${row.userName}”最近活动的会话？其他设备会话不受影响。`)
    await forceLogout(row.tokenId)
    proxy.$modal.msgSuccess('该会话已退出')
    await getList()
  } catch (_) { /* 取消或请求错误由统一提示处理。 */ }
}
deptTree().then(response => { deptOptions.value = response.data || [] }).catch(() => {})
getList().catch(() => {})
</script>

<style scoped>
.online-note { margin-bottom: 20px; }
.online-summary { display: flex; flex-wrap: wrap; align-items: center; gap: 24px; margin: 4px 0 20px; }
.online-summary strong { color: var(--el-color-primary); font-size: 24px; margin: 0 4px; }
.sample-time { color: var(--el-text-color-secondary); font-size: 13px; }
</style>
