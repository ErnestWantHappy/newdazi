<template>
  <el-card class="resource-card" shadow="hover">
    <div class="resource-card__head">
      <div>
        <el-tag v-if="post.isPinned === 'Y'" type="danger" size="small">置顶资源</el-tag>
        <strong>{{ post.courseTitle }}</strong>
      </div>
      <el-dropdown v-if="post.owner || manager" trigger="click" @command="$emit('action', $event, post)">
        <el-button link icon="MoreFilled" />
        <template #dropdown><el-dropdown-menu>
          <el-dropdown-item v-if="post.owner" command="edit">编辑</el-dropdown-item>
          <el-dropdown-item command="delete">隐藏</el-dropdown-item>
          <el-dropdown-item v-if="manager" command="pin">{{ post.isPinned === 'Y' ? '取消置顶' : '置顶' }}</el-dropdown-item>
        </el-dropdown-menu></template>
      </el-dropdown>
    </div>
    <div class="resource-card__tags">
      <el-tag size="small">{{ optionLabel(SCHOOL_TYPES, post.schoolType) }}</el-tag>
      <el-tag size="small">{{ gradeLabel(post.grade) }}</el-tag>
      <el-tag size="small">{{ optionLabel(SEMESTERS, post.semester) }}</el-tag>
      <el-tag size="small">{{ lessonLabel(post) }}</el-tag>
    </div>
    <div class="rich-content" v-html="post.contentHtml" />
    <div v-if="post.resources?.length" class="resource-list">
      <div v-for="resource in post.resources" :key="resource.resourceId" class="resource-item">
        <div>
          <strong>{{ resource.resourceName || resource.originalFileName }}</strong>
          <span v-if="resource.resourceType === 'F'">{{ formatFileSize(resource.fileSize) }}</span>
          <el-tag v-else :type="status(resource).expired ? 'danger' : 'success'" size="small">{{ status(resource).text }}</el-tag>
          <small v-if="resource.description">{{ resource.description }}</small>
        </div>
        <div class="resource-actions">
          <el-button v-if="resource.resourceType === 'F'" link type="primary" :loading="busyId === resource.resourceId" @click="download(resource)">下载</el-button>
          <template v-else>
            <el-button link type="primary" @click="openLink(resource)">打开</el-button>
            <el-button v-if="resource.extractCode" link @click="copyCode(resource.extractCode)">复制提取码</el-button>
          </template>
        </div>
      </div>
    </div>
    <div class="resource-card__meta">
      {{ post.authorName }} · {{ post.deptName || '未标注学校' }} · {{ post.createTime }}<template v-if="post.edited"> · 已编辑</template>
      <el-button v-if="showTopic && post.topicId" link type="primary" @click="$emit('open-topic', post.topicId)">查看主题：{{ post.topicTitle }}</el-button>
    </div>
  </el-card>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { saveAs } from 'file-saver'
import { accessResearchLink, downloadResearchResource } from '@/api/business/researchActivity.js'
import { SCHOOL_TYPES, SEMESTERS, formatFileSize, gradeLabel, lessonLabel, linkStatus, optionLabel } from '../utils/researchActivityFormat.js'

defineProps({ post: { type: Object, required: true }, manager: { type: Boolean, default: false }, showTopic: { type: Boolean, default: false } })
defineEmits(['action', 'open-topic'])
const busyId = ref(null)
const status = resource => resource.linkStatus ? { text: resource.linkStatusText, expired: resource.linkStatus === 'EXPIRED' } : linkStatus(resource)

async function download(resource) {
  busyId.value = resource.resourceId
  try {
    const blob = await downloadResearchResource(resource.resourceId)
    saveAs(new Blob([blob]), resource.originalFileName || resource.resourceName || '课程资源')
  } finally { busyId.value = null }
}
async function openLink(resource) {
  const response = await accessResearchLink(resource.resourceId)
  const url = response?.data?.linkUrl
  if (url) window.open(url, '_blank', 'noopener,noreferrer')
}
async function copyCode(code) {
  await navigator.clipboard.writeText(code)
  ElMessage.success('提取码已复制')
}
</script>

<style scoped>
.resource-card { margin-bottom: 14px; }
.resource-card__head, .resource-item, .resource-card__meta { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.resource-card__head > div { display: flex; gap: 8px; align-items: center; }
.resource-card__head strong { font-size: 17px; }
.resource-card__tags { display: flex; gap: 6px; margin: 10px 0; flex-wrap: wrap; }
.rich-content { line-height: 1.7; overflow-wrap: anywhere; }
.rich-content :deep(img) { max-width: 100%; }
.rich-content :deep(table) { border-collapse: collapse; max-width: 100%; }
.rich-content :deep(td), .rich-content :deep(th) { border: 1px solid var(--el-border-color); padding: 6px; }
.resource-list { margin-top: 12px; border-top: 1px solid var(--el-border-color-lighter); }
.resource-item { padding: 10px 0; border-bottom: 1px solid var(--el-border-color-lighter); }
.resource-item > div:first-child { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.resource-item small { width: 100%; color: var(--el-text-color-secondary); }
.resource-card__meta { margin-top: 12px; color: var(--el-text-color-secondary); font-size: 13px; flex-wrap: wrap; }
</style>
