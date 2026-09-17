<template>
  <div class="app-container research-page">
    <div class="page-head">
      <div><h2>教研活动</h2><p>分享活动纪实、课程资源与教学反思</p></div>
      <el-space>
        <el-button v-if="manager" plain icon="RefreshLeft" @click="openHidden">隐藏内容管理</el-button>
        <el-button type="primary" icon="Plus" v-hasPermi="['business:researchActivity:add']" @click="openTopic()">发布主题</el-button>
      </el-space>
    </div>
    <el-card shadow="never" class="search-card">
      <el-segmented v-model="view" :options="[{ label: '活动主题', value: 'topics' }, { label: '课程资源', value: 'resources' }]" @change="scheduleLoad(true)" />
      <el-form :model="query" inline class="filters" @submit.prevent>
        <el-form-item label="关键词"><el-input v-model="query.keyword" clearable placeholder="课程标题、主题、反思或作者" @input="scheduleLoad(true)" /></el-form-item>
        <template v-if="view === 'resources'">
          <el-form-item label="学段"><el-select v-model="query.schoolType" clearable @change="schoolTypeChanged"><el-option v-for="item in SCHOOL_TYPES" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
          <el-form-item label="年级"><el-select v-model="query.grade" clearable @change="scheduleLoad(true)"><el-option v-for="grade in grades" :key="grade" :label="gradeLabel(grade)" :value="grade" /></el-select></el-form-item>
          <el-form-item label="学期"><el-select v-model="query.semester" clearable @change="scheduleLoad(true)"><el-option v-for="item in SEMESTERS" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
          <el-form-item label="课次"><el-select v-model="query.lessonKind" clearable @change="lessonKindChanged"><el-option v-for="item in LESSON_KINDS" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
          <el-form-item v-if="query.lessonKind === 'N'" label="第几课"><el-input-number v-model="query.lessonNo" :min="1" :max="999" controls-position="right" @change="scheduleLoad(true)" /></el-form-item>
        </template>
        <el-form-item label="创建时间"><el-date-picker v-model="createDateRange" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" @change="scheduleLoad(true)" /></el-form-item>
        <el-form-item v-if="view === 'topics'" label="活动时间"><el-date-picker v-model="activityDateRange" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" @change="scheduleLoad(true)" /></el-form-item>
        <el-form-item><el-button @click="reset">重置</el-button></el-form-item>
      </el-form>
    </el-card>

    <div v-loading="loading" class="result-list">
      <template v-if="view === 'resources'">
        <ResourceCard v-for="post in rows" :key="post.postId" :post="post" :manager="manager" show-topic @open-topic="openTopicDetail" @action="handlePostAction" />
      </template>
      <template v-else>
        <TopicCard v-for="topic in rows" :key="topic.topicId" :topic="topic" :manager="manager" @open="openTopicDetail" @action="handleTopicAction" />
      </template>
      <el-empty v-if="!loading && !rows.length" :description="view === 'resources' ? '没有找到课程资源' : '暂无活动主题'" />
    </div>
    <pagination v-show="total > 0" :total="total" v-model:page="query.pageNum" v-model:limit="query.pageSize" @pagination="load" />
    <TopicComposer v-model="topicDialog" :topic="editingTopic" :manager="manager" @saved="load" />
    <PostComposer v-if="editingPost" v-model="postDialog" :topic-id="editingPost.topicId" :post="editingPost" @saved="load" />
    <NotificationComposer v-model="notifyDialog" :topic-id="notifyTopicId" @sent="load" />
    <el-drawer v-model="hiddenDrawer" title="隐藏内容管理" size="720px">
      <el-tabs v-model="hiddenTab" @tab-change="loadHidden">
        <el-tab-pane label="主题" name="topics">
          <el-table v-loading="hiddenLoading" :data="hiddenRows"><el-table-column prop="title" label="主题" min-width="260" /><el-table-column prop="creatorName" label="作者" width="120" /><el-table-column prop="updateTime" label="隐藏时间" width="170" /><el-table-column label="操作" width="90"><template #default="{ row }"><el-button link type="primary" @click="restoreTopic(row)">恢复</el-button></template></el-table-column></el-table>
        </el-tab-pane>
        <el-tab-pane label="留言/资源" name="posts">
          <el-table v-loading="hiddenLoading" :data="hiddenRows"><el-table-column prop="topicTitle" label="所属主题" min-width="180" /><el-table-column label="内容" min-width="220"><template #default="{ row }">{{ row.courseTitle || row.contentText }}</template></el-table-column><el-table-column prop="authorName" label="作者" width="110" /><el-table-column label="操作" width="90"><template #default="{ row }"><el-button link type="primary" @click="restorePost(row)">恢复</el-button></template></el-table-column></el-table>
        </el-tab-pane>
      </el-tabs>
    </el-drawer>
  </div>
</template>

<script setup name="ResearchActivity">
import { ElMessageBox, ElMessage } from 'element-plus'
import useUserStore from '@/store/modules/user.js'
import { deleteResearchPost, deleteResearchTopic, listHiddenResearchPosts, listHiddenResearchTopics, pinResearchPost, pinResearchTopic, restoreResearchPost, restoreResearchTopic, searchResearchResources, searchResearchTopics } from '@/api/business/researchActivity.js'
import NotificationComposer from './components/NotificationComposer.vue'
import PostComposer from './components/PostComposer.vue'
import ResourceCard from './components/ResourceCard.vue'
import TopicCard from './components/TopicCard.vue'
import TopicComposer from './components/TopicComposer.vue'
import { LESSON_KINDS, SCHOOL_TYPES, SEMESTERS, gradeLabel, gradesForSchoolType } from './utils/researchActivityFormat.js'

const router = useRouter()
const userStore = useUserStore()
const manager = computed(() => userStore.roles.includes('admin') || userStore.roles.includes('researcher'))
const view = ref('topics')
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const createDateRange = ref([])
const activityDateRange = ref([])
const query = reactive({ keyword: '', schoolType: '', grade: null, semester: '', lessonKind: '', lessonNo: null, pageNum: 1, pageSize: 20 })
const grades = computed(() => gradesForSchoolType(query.schoolType))
const topicDialog = ref(false)
const editingTopic = ref(null)
const postDialog = ref(false)
const editingPost = ref(null)
const notifyDialog = ref(false)
const notifyTopicId = ref(null)
const hiddenDrawer = ref(false)
const hiddenTab = ref('topics')
const hiddenRows = ref([])
const hiddenLoading = ref(false)
let timer
let controller

function params() {
  const result = { ...query }
  if (createDateRange.value?.length === 2) Object.assign(result, { beginTime: `${createDateRange.value[0]} 00:00:00`, endTime: `${createDateRange.value[1]} 23:59:59` })
  if (view.value === 'topics' && activityDateRange.value?.length === 2) Object.assign(result, { activityBeginTime: `${activityDateRange.value[0]} 00:00:00`, activityEndTime: `${activityDateRange.value[1]} 23:59:59` })
  return Object.fromEntries(Object.entries(result).filter(([, value]) => value !== '' && value != null))
}
function scheduleLoad(resetPage = false) {
  if (resetPage) query.pageNum = 1
  clearTimeout(timer)
  timer = setTimeout(load, 300)
}
async function load() {
  controller?.abort()
  controller = new AbortController()
  loading.value = true
  try {
    const response = view.value === 'resources'
      ? await searchResearchResources(params(), controller.signal)
      : await searchResearchTopics(params(), controller.signal)
    rows.value = response.rows || []
    total.value = response.total || 0
  } catch (error) {
    if (error?.code !== 'ERR_CANCELED') throw error
  } finally { loading.value = false }
}
function schoolTypeChanged() { if (!grades.value.includes(Number(query.grade))) query.grade = null; scheduleLoad(true) }
function lessonKindChanged() { if (query.lessonKind !== 'N') query.lessonNo = null; scheduleLoad(true) }
function reset() { Object.assign(query, { keyword: '', schoolType: '', grade: null, semester: '', lessonKind: '', lessonNo: null, pageNum: 1 }); createDateRange.value = []; activityDateRange.value = []; load() }
function openTopic(topic = null) { editingTopic.value = topic; topicDialog.value = true }
function openTopicDetail(topic) { router.push(`/business/research-activity/detail/${topic.topicId || topic}`) }
async function handleTopicAction(command, topic) {
  if (command === 'edit') return openTopic(topic)
  if (command === 'notify') { notifyTopicId.value = topic.topicId; notifyDialog.value = true; return }
  if (command === 'pin') { await pinResearchTopic(topic.topicId, topic.isPinned !== 'Y'); ElMessage.success('置顶状态已更新'); return load() }
  await ElMessageBox.confirm('隐藏后普通列表、搜索、通知和下载均不可见，确认继续？', '隐藏主题', { type: 'warning' })
  await deleteResearchTopic(topic.topicId); ElMessage.success('主题已隐藏'); load()
}
async function handlePostAction(command, post) {
  if (command === 'edit') { editingPost.value = post; postDialog.value = true; return }
  if (command === 'pin') { await pinResearchPost(post.postId, post.isPinned !== 'Y'); ElMessage.success('置顶状态已更新'); return load() }
  await ElMessageBox.confirm('确认隐藏这条课程资源？', '隐藏资源', { type: 'warning' })
  await deleteResearchPost(post.postId); ElMessage.success('资源已隐藏'); load()
}
function openHidden() { hiddenDrawer.value = true; hiddenTab.value = 'topics'; loadHidden() }
async function loadHidden() {
  hiddenLoading.value = true
  try {
    const response = hiddenTab.value === 'topics'
      ? await listHiddenResearchTopics({ pageNum: 1, pageSize: 50 })
      : await listHiddenResearchPosts({ pageNum: 1, pageSize: 50 })
    hiddenRows.value = response.rows || []
  } finally { hiddenLoading.value = false }
}
async function restoreTopic(row) { await restoreResearchTopic(row.topicId); ElMessage.success('主题已恢复'); loadHidden(); load() }
async function restorePost(row) {
  try { await restoreResearchPost(row.postId); ElMessage.success('留言已恢复'); loadHidden(); load() }
  catch (error) { if (error?.message?.includes('主题')) ElMessage.warning('请先恢复所属主题') }
}
onMounted(load)
onBeforeUnmount(() => { clearTimeout(timer); controller?.abort() })
</script>

<style scoped>
.research-page { max-width: 1280px; margin: 0 auto; }
.page-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-head h2 { margin: 0 0 5px; }.page-head p { margin: 0; color: var(--el-text-color-secondary); }
.filters { margin-top: 16px; }.filters :deep(.el-select) { width: 130px; }.filters :deep(.el-date-editor) { width: 250px; }
.result-list { min-height: 260px; margin-top: 16px; }
</style>
