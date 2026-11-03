<template>
  <div class="extension-page">
    <header><div class="brand"><img src="@/assets/logo/logo.png" alt="平台标志" /><strong>智慧课堂</strong></div><StudentSpaceSwitch space="extension" /><span>{{ user.nickName || user.name }}</span><el-button link @click="logout">退出</el-button></header>
    <main v-loading="loading">
      <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
      <div v-if="unavailable" class="unavailable"><p>你已不在该拓展班</p><el-button type="primary" @click="$router.push('/student/index')">返回常规课</el-button></div>
      <template v-else-if="home">
        <h1>{{ home.extensionClass.class_name }}</h1>
        <section><h2>当前签到课</h2><template v-if="home.currentLesson"><h3>{{ home.currentLesson.lesson_title }}</h3><p v-if="home.currentLesson.remark">{{ home.currentLesson.remark }}</p><p>{{ date(home.currentLesson.lesson_date) }}</p><p v-if="home.checkinTime">已签到 · {{ date(home.checkinTime) }}</p><el-button v-else type="primary" :loading="signing" :disabled="!home.eligible" @click="checkin">签到</el-button><p v-if="!home.eligible" class="hint">你在本课建课后加入，从下一节课开始签到。</p></template><el-empty v-else description="暂时没有签到课，等待老师发布" :image-size="56" /></section>
        <section><h2>Python 题单</h2><template v-if="questionCount"><p>{{ planCount }} 份题单 · {{ questionCount }} 道题</p><el-button type="primary" @click="$router.push('/student/python-practice?space=extension')">进入 Python 练习</el-button></template><el-empty v-else description="老师还没有发布 Python 题单" :image-size="56" /></section>
        <section><h2>本人签到记录</h2><el-table v-if="home.history.length" :data="home.history" size="small"><el-table-column prop="lesson_title" label="课程" /><el-table-column label="签到时间"><template #default="{ row }">{{ date(row.checkin_time) }}</template></el-table-column></el-table><el-empty v-else description="暂无签到记录" :image-size="56" /></section>
      </template>
      <el-button v-if="error && !unavailable" @click="load">重新加载</el-button>
    </main>
  </div>
</template>
<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import StudentSpaceSwitch from '@/components/StudentSpaceSwitch'
import useUserStore from '@/store/modules/user'
import { getExtensionHome } from '@/api/business/extension'
import { getStudentPracticeOverview } from '@/api/business/pythonPractice'
const user = useUserStore(), router = useRouter()
const home = ref(null), loading = ref(false), signing = ref(false), error = ref(''), unavailable = ref(false), planCount = ref(0), questionCount = ref(0)
let timer
const date = value => value ? String(value).replace('T', ' ').slice(0, 19) : ''
async function load() {
  if (loading.value) return
  loading.value = true; error.value = ''
  try {
    const [context, practice] = await Promise.all([getExtensionHome(), getStudentPracticeOverview({ space: 'extension' })])
    home.value = context.data; unavailable.value = false
    const questions = practice.data?.questions || []
    questionCount.value = questions.length; planCount.value = new Set(questions.map(q => q.plan_id)).size
  } catch (e) {
    error.value = e?.message || '加载失败，请重试'
    if (Number(e?.code) === 403 || /不在|无权|成员/.test(error.value)) { unavailable.value = true; home.value = null; error.value = '' }
  } finally { loading.value = false }
}
async function checkin() {
  signing.value = true; error.value = ''
  try { await request({ url: '/business/student-home/checkin', method: 'post', data: { lessonId: home.value.currentLesson.lesson_id } }); await load() }
  catch (e) { error.value = e?.message || '签到失败，请重试' }
  finally { signing.value = false }
}
async function logout() { await user.logOut(); router.push('/login') }
onMounted(() => { load(); timer = setInterval(() => { if (!document.hidden) load() }, 15000) })
onUnmounted(() => clearInterval(timer))
</script>
<style scoped>
.extension-page{--el-color-primary:#1f5fbf;min-height:100vh;background:#f4f6f9;color:#303133}.extension-page header{display:flex;align-items:center;flex-wrap:wrap;gap:12px;padding:12px 20px;background:white;border-bottom:1px solid #dce3ed}.brand{display:flex;align-items:center;gap:8px;color:#1f5fbf}.brand img{width:30px;height:30px}.extension-page main{max-width:1000px;margin:auto;padding:16px;min-height:200px}.extension-page h1{font-size:20px;margin:8px 0 16px}.extension-page h2{font-size:16px;margin:0 0 12px}.extension-page h3{font-size:15px}.extension-page section{padding:16px;background:white;border:1px solid #e0e5ed;border-radius:4px;margin-bottom:14px;overflow-wrap:anywhere}.hint{font-size:13px;color:#606266}.unavailable{padding:24px 0}@media(max-width:480px){.extension-page header{padding:10px;gap:8px}.extension-page main{padding:10px}.extension-page section{padding:12px}.brand{width:100%}}
</style>
