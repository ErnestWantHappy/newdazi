<template>
  <el-card v-if="tasks.length" shadow="never" class="returned-tasks">
    <template #header><strong>老师退回的作业</strong></template>
    <div v-for="task in tasks" :key="`${task.lessonId}:${task.questionId}`" class="returned-task">
      <div><el-tag type="warning" size="small">待重交</el-tag> {{ task.lessonTitle }} · {{ task.questionContent }}</div>
      <el-button v-if="task.practicalMode === 'FLOWCHART'" type="primary" @click="$emit('open-flowchart', task)">修改流程图并重新提交</el-button>
      <template v-else>
        <el-upload :action="uploadUrl" :headers="uploadHeaders" :data="{ lessonId: task.lessonId, questionId: task.questionId }"
          :disabled="submitting === key(task)"
          :accept="accept(task)" :multiple="true" :show-file-list="false" :before-upload="file => beforeUpload(task, file)"
          :on-success="(response, file) => uploadSuccess(task, response, file)"
          :on-error="(error, file) => uploadError(task, file)">
          <el-button type="primary" plain>选择新作品</el-button>
        </el-upload>
        <span v-if="drafts[key(task)]?.length">已选择 {{ drafts[key(task)].length }} 个文件
          <span v-if="hasPending(task)">（上传中）</span>
        </span>
        <el-button v-if="drafts[key(task)]?.length" link @click="clearDraft(task)">清空已选</el-button>
        <el-button :disabled="!canSubmit(task)" :loading="submitting === key(task)" type="warning" @click="submit(task)">提交新版本</el-button>
      </template>
    </div>
  </el-card>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'
import { submitPracticalArtifact } from '@/api/business/studentHome'

defineProps({ tasks: { type: Array, default: () => [] } })
const emit = defineEmits(['open-flowchart', 'submitted'])
const uploadUrl = import.meta.env.VITE_APP_BASE_API + '/business/student-home/practical-upload'
const uploadHeaders = { Authorization: 'Bearer ' + getToken() }
const drafts = ref({})
const submitting = ref('')
const key = task => `${task.lessonId}:${task.questionId}`
const extensions = task => String(task.practicalAllowedExtensions || 'doc,docx,pdf,ppt,pptx,xls,xlsx,jpg,jpeg,png')
  .toLowerCase().split(',').map(value => value.trim()).filter(Boolean)
const accept = task => extensions(task).map(value => `.${value}`).join(',')

function beforeUpload(task, file) {
  const extension = String(file.name || '').split('.').pop().toLowerCase()
  if (!extensions(task).includes(extension) || file.size > 50 * 1024 * 1024) {
    ElMessage.error('文件格式不符或超过 50MB')
    return false
  }
  const current = drafts.value[key(task)] || []
  const image = ['jpg', 'jpeg', 'png'].includes(extension)
  const max = Math.min(Math.max(Number(task.practicalImageMaxCount || 10), 1), 10)
  if ((current.length && !image) || (current.length && current.some(item => item.fileKind !== 'IMAGE')) || current.length >= (image ? max : 1)) {
    ElMessage.warning('Office/PDF 只能提交一个文件，图片最多提交指定张数')
    return false
  }
  const taskKey = key(task)
  drafts.value[taskKey] = [...current, { uid: file.uid, name: file.name, fileKind: image ? 'IMAGE' : 'DOCUMENT', pending: true }]
  return true
}

function uploadSuccess(task, response, file) {
  const taskKey = key(task)
  const current = drafts.value[taskKey] || []
  const index = current.findIndex(item => item.uid === file.uid)
  if (index < 0) return
  if (response.code !== 200 || !response.uploadToken) {
    ElMessage.error(response.msg || '文件上传失败')
    drafts.value[taskKey] = current.filter(item => item.uid !== file.uid)
    return
  }
  const updated = [...current]
  updated[index] = { ...updated[index], token: response.uploadToken, fileKind: response.fileKind, pending: false }
  drafts.value[taskKey] = updated
}

function uploadError(task, file) {
  const taskKey = key(task)
  drafts.value[taskKey] = (drafts.value[taskKey] || []).filter(item => item.uid !== file.uid)
  ElMessage.error('文件上传失败')
}

function hasPending(task) {
  return (drafts.value[key(task)] || []).some(item => item.pending)
}

function canSubmit(task) {
  const items = drafts.value[key(task)] || []
  return items.length > 0 && !items.some(item => item.pending || !item.token) && submitting.value !== key(task)
}

function clearDraft(task) {
  drafts.value[key(task)] = []
}

async function submit(task) {
  const taskKey = key(task)
  const items = drafts.value[taskKey] || []
  if (!canSubmit(task)) return
  submitting.value = taskKey
  try {
    await submitPracticalArtifact({ lessonId: task.lessonId, questionId: task.questionId,
      expectedVersionId: task.artifact?.versionId || null, uploadTokens: items.map(item => item.token) })
    drafts.value[taskKey] = []
    ElMessage.success('作品已重新提交')
    emit('submitted')
  } finally { submitting.value = '' }
}
</script>

<style scoped>
.returned-tasks { margin-bottom: 20px; }
.returned-task { display: flex; align-items: center; flex-wrap: wrap; gap: 12px; padding: 12px 0; border-bottom: 1px solid var(--el-border-color-lighter); }
.returned-task:last-child { border-bottom: 0; }
</style>
