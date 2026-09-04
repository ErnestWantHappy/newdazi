<template><div class="app-container student-collaboration"><div class="page-actions"><el-button link type="primary" @click="goHome">返回学生首页</el-button></div><el-card shadow="never"><template #header>{{ room.roomTitle || '班级在线协作' }}</template><el-skeleton v-if="loading" :rows="3" animated /><template v-else-if="errorMessage"><el-result icon="warning" title="暂时无法进入协作" :sub-title="errorMessage"><template #extra><el-button type="primary" @click="loadRoom">重新加载</el-button></template></el-result></template><template v-else><p>这是本班共享文档，只有当前课程和班级学生可以进入。</p><el-button type="primary" :disabled="!room.roomId" @click="openEditor">进入协作编辑</el-button></template></el-card></div></template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
const route = useRoute(); const router = useRouter(); const room = reactive({})
const loading = ref(false); const errorMessage = ref('')
async function loadRoom() {
  loading.value = true; errorMessage.value = ''
  try {
    const res = await request({ url: `/business/collaboration/room/${route.params.roomId}/session`, method: 'get' })
    Object.assign(room, res.data?.room || res.room || {})
    if (!room.roomId) errorMessage.value = '协作房间不存在、已关闭或当前账号无权访问。'
  } catch (error) {
    errorMessage.value = error?.message || '网络异常，请稍后重试。'
  } finally { loading.value = false }
}
onMounted(loadRoom)
// 学生必须停留在 /student 路由空间，避免权限守卫把教师编辑地址重定向回学生首页。
function openEditor() { router.replace(`/student/collaboration/editor/${route.params.roomId}`) }
function goHome() { router.push('/student') }
</script>
<style scoped>.page-actions{margin-bottom:12px}</style>
