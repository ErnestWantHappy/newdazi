<template>
  <div class="app-container detail-page" v-loading="loading">
    <el-page-header content="主题详情" @back="router.push('/research-activity')" />
    <el-card v-if="topic" class="topic-detail" shadow="never">
      <div class="topic-title">
        <div>
          <el-tag v-if="topic.isPinned === 'Y'" type="danger">置顶</el-tag>
          <el-tag :type="topic.topicType === 'NOTICE' ? 'warning' : 'info'">{{ topic.topicType === 'NOTICE' ? '活动通知' : '交流分享' }}</el-tag>
          <h1>{{ topic.title }}</h1>
        </div>
        <el-space>
          <el-button v-if="topic.owner" link type="primary" @click="editingTopic = topic; topicDialog = true">编辑</el-button>
          <el-button v-if="manager" link type="primary" @click="toggleTopicPin">{{ topic.isPinned === 'Y' ? '取消置顶' : '置顶' }}</el-button>
          <el-button v-if="manager && topic.topicType === 'NOTICE'" link type="warning" @click="notifyDialog = true">再次通知</el-button>
          <el-button v-if="topic.owner || manager" link type="danger" @click="removeTopic">隐藏</el-button>
        </el-space>
      </div>
      <div class="meta">{{ topic.creatorName }} · {{ topic.deptName || '未标注学校' }} · {{ topic.createTime }}<template v-if="topic.activityTime"> · 活动时间 {{ topic.activityTime }}</template><template v-if="topic.edited"> · 已编辑</template> · 浏览 {{ topic.viewCount || 0 }}</div>
      <div class="rich-content" v-html="topic.contentHtml" />
    </el-card>

    <div class="post-toolbar">
      <el-radio-group v-model="postType" @change="loadPosts">
        <el-radio-button value="">全部</el-radio-button><el-radio-button value="RESOURCE">课程资源</el-radio-button><el-radio-button value="MOMENT">活动纪实</el-radio-button><el-radio-button value="COMMENT">普通留言</el-radio-button>
      </el-radio-group>
      <el-button type="primary" icon="Plus" v-hasPermi="['business:researchActivity:add']" @click="editingPost = null; postDialog = true">发布留言</el-button>
    </div>
    <div v-loading="postsLoading" class="posts">
      <template v-for="post in posts" :key="post.postId">
        <ResourceCard v-if="post.postType === 'RESOURCE'" :post="post" :manager="manager" @action="handlePostAction" />
        <el-card v-else class="post-card" shadow="never">
          <div class="post-head">
            <div><el-tag :type="post.postType === 'MOMENT' ? 'success' : 'info'">{{ post.postType === 'MOMENT' ? '活动纪实' : '普通留言' }}</el-tag><span>{{ post.authorName }} · {{ post.deptName || '未标注学校' }}</span></div>
            <el-dropdown v-if="post.owner || manager" @command="handlePostAction($event, post)"><el-button link icon="MoreFilled" /><template #dropdown><el-dropdown-menu><el-dropdown-item v-if="post.owner" command="edit">编辑</el-dropdown-item><el-dropdown-item command="delete">隐藏</el-dropdown-item></el-dropdown-menu></template></el-dropdown>
          </div>
          <div class="rich-content" v-html="post.contentHtml" />
          <div class="meta">{{ post.createTime }}<template v-if="post.edited"> · 已编辑</template></div>
        </el-card>
      </template>
      <el-empty v-if="!postsLoading && !posts.length" description="暂无留言" />
    </div>
    <pagination v-show="total > 0" :total="total" v-model:page="pageNum" v-model:limit="pageSize" @pagination="loadPosts" />

    <TopicComposer v-model="topicDialog" :topic="editingTopic" :manager="manager" @saved="loadTopic" />
    <PostComposer v-model="postDialog" :topic-id="topicId" :post="editingPost" @saved="afterPostSaved" />
    <NotificationComposer v-model="notifyDialog" :topic-id="topicId" @sent="loadTopic" />
  </div>
</template>

<script setup name="ResearchActivityDetail">
import { ElMessage, ElMessageBox } from 'element-plus'
import useUserStore from '@/store/modules/user.js'
import { deleteResearchPost, deleteResearchTopic, getResearchTopic, listResearchPosts, pinResearchPost, pinResearchTopic } from '@/api/business/researchActivity.js'
import NotificationComposer from './components/NotificationComposer.vue'
import PostComposer from './components/PostComposer.vue'
import ResourceCard from './components/ResourceCard.vue'
import TopicComposer from './components/TopicComposer.vue'

const route = useRoute()
const router = useRouter()
const topicId = computed(() => route.params.topicId)
const userStore = useUserStore()
const manager = computed(() => userStore.roles.includes('admin') || userStore.roles.includes('researcher'))
const loading = ref(false)
const postsLoading = ref(false)
const topic = ref(null)
const posts = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const postType = ref('')
const topicDialog = ref(false)
const editingTopic = ref(null)
const postDialog = ref(false)
const editingPost = ref(null)
const notifyDialog = ref(false)

async function loadTopic() {
  loading.value = true
  try { topic.value = (await getResearchTopic(topicId.value)).data }
  finally { loading.value = false }
}
async function loadPosts() {
  postsLoading.value = true
  try {
    const response = await listResearchPosts(topicId.value, { postType: postType.value || undefined, pageNum: pageNum.value, pageSize: pageSize.value })
    posts.value = response.rows || []; total.value = response.total || 0
  } finally { postsLoading.value = false }
}
async function toggleTopicPin() { await pinResearchTopic(topicId.value, topic.value.isPinned !== 'Y'); ElMessage.success('置顶状态已更新'); loadTopic() }
async function removeTopic() {
  await ElMessageBox.confirm('隐藏后列表、搜索、通知和下载均不可见，确认继续？', '隐藏主题', { type: 'warning' })
  await deleteResearchTopic(topicId.value); ElMessage.success('主题已隐藏'); router.replace('/research-activity')
}
async function handlePostAction(command, post) {
  if (command === 'edit') { editingPost.value = post; postDialog.value = true; return }
  if (command === 'pin') { await pinResearchPost(post.postId, post.isPinned !== 'Y'); ElMessage.success('置顶状态已更新'); return loadPosts() }
  await ElMessageBox.confirm('确认隐藏这条留言？', '隐藏留言', { type: 'warning' })
  await deleteResearchPost(post.postId); ElMessage.success('留言已隐藏'); loadPosts()
}
function afterPostSaved() { pageNum.value = 1; loadTopic(); loadPosts() }
onMounted(() => { loadTopic(); loadPosts() })
</script>

<style scoped>
.detail-page { max-width: 1100px; margin: 0 auto; }.topic-detail { margin-top: 18px; }
.topic-title, .topic-title > div, .post-toolbar, .post-head, .post-head > div { display: flex; align-items: center; gap: 10px; }
.topic-title, .post-toolbar, .post-head { justify-content: space-between; }.topic-title h1 { margin: 0; font-size: 24px; }
.meta { margin: 10px 0; color: var(--el-text-color-secondary); font-size: 13px; }
.rich-content { line-height: 1.75; overflow-wrap: anywhere; }.rich-content :deep(img) { max-width: 100%; height: auto; }.rich-content :deep(table) { border-collapse: collapse; max-width: 100%; }.rich-content :deep(td), .rich-content :deep(th) { border: 1px solid var(--el-border-color); padding: 6px; }
.post-toolbar { margin: 20px 0 14px; }.post-card { margin-bottom: 14px; }.post-head { margin-bottom: 10px; }
</style>
