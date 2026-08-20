<template><div class="app-container student-collaboration"><el-card shadow="never"><template #header>{{ room.roomTitle || '班级在线协作' }}</template><p>这是本班共享文档，只有当前课程和班级学生可以进入。</p><el-button type="primary" @click="openEditor">进入协作编辑</el-button></el-card></div></template>
<script setup>
import { reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
const route = useRoute(); const router = useRouter(); const room = reactive({})
request({ url: `/business/collaboration/room/${route.params.roomId}/session`, method: 'get' }).then(res => Object.assign(room, res.data?.room || res.room || {}))
// 学生必须停留在 /student 路由空间，避免权限守卫把教师编辑地址重定向回学生首页。
function openEditor() { router.replace(`/student/collaboration/editor/${route.params.roomId}`) }
</script>
