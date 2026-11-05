<template>
  <div class="case-votes" @click.stop @keydown.stop>
    <!-- 推荐/不推荐主按钮 -->
    <button
      v-for="choice in choices"
      :key="choice.value"
      type="button"
      :class="{ chosen: Number(item.myVote) === choice.value }"
      :aria-pressed="Number(item.myVote) === choice.value"
      :disabled="!canVote || busy"
      :title="canVote ? (Number(item.myVote) === choice.value ? '再次点击撤销评价' : choice.label) : '仅教师可评价'"
      @click="onQuickVote(choice.value)"
    >
      <span aria-hidden="true">{{ choice.icon }}</span>
      <span>{{ choice.label }}</span>
      <span class="count">{{ Number(item[choice.count]) || 0 }}</span>
    </button>

    <!-- 教师写理由入口 -->
    <el-button
      v-if="canVote"
      size="small"
      text
      class="reason-btn"
      :disabled="busy"
      :title="item.myReason ? '查看/修改我的评价理由' : '为评价附言，供其他老师参考'"
      @click="openReasonDialog"
    >
      <el-icon><EditPen /></el-icon>
      {{ item.myReason ? '修改理由' : '写理由' }}
    </el-button>

    <!-- 查看全部教师理由入口 -->
    <el-button
      size="small"
      text
      class="view-comments-btn"
      title="查看其他教师对该课例的评价理由"
      @click="openCommentsDialog"
    >
      <el-icon><ChatDotRound /></el-icon>
      评价理由
    </el-button>

    <span v-if="busy" role="status" class="saving-hint">保存中…</span>

    <!-- 写评价理由弹窗 -->
    <el-dialog
      v-model="reasonDialogVisible"
      title="写评价理由"
      :close-on-click-modal="!reasonSaving"
      :close-on-press-escape="!reasonSaving"
      :show-close="!reasonSaving"
      width="min(440px, 92vw)"
      append-to-body
      destroy-on-close
    >
      <div class="reason-dialog-content">
        <p class="case-info">
          <strong>{{ item.title }}</strong>
          <span v-if="item.author" class="muted"> · {{ item.author }}</span>
        </p>
        <div class="vote-selector">
          <span>评价态度：</span>
          <el-radio-group v-model="pendingVote" size="small">
            <el-radio-button :label="1">👍 推荐</el-radio-button>
            <el-radio-button :label="-1">👎 不推荐</el-radio-button>
          </el-radio-group>
        </div>
        <el-input
          v-model="reasonText"
          type="textarea"
          :rows="4"
          maxlength="500"
          show-word-limit
          placeholder="写下您的评价理由或教学使用建议，供全平台教师参考（匿名展示）"
        />
        <p class="privacy-tip">
          <el-icon><InfoFilled /></el-icon>
          平台将以“同行教师”匿名展示您的评价，保护您的个人身份。
        </p>
        <p v-if="reasonError" role="alert" class="comments-error">{{ reasonError }}</p>
      </div>
      <template #footer>
        <el-button :disabled="reasonSaving" @click="reasonDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="reasonSaving" :disabled="busy && !reasonSaving" @click="submitWithReason">保存评价</el-button>
      </template>
    </el-dialog>

    <!-- 查看全平台教师评价理由弹窗 -->
    <el-dialog
      v-model="commentsDialogVisible"
      :title="`《${item.title}》教师评价理由`"
      width="min(560px, 94vw)"
      append-to-body
      destroy-on-close
    >
      <div v-loading="commentsLoading" class="comments-container">
        <div v-if="commentsError" class="comments-error">
          <p>读取评价理由失败</p>
          <el-button link type="primary" @click="loadComments">重试</el-button>
        </div>
        <el-empty
          v-else-if="!comments.length"
          description="本课例暂无文字评价理由，欢迎您在评价时写下宝贵建议！"
          :image-size="60"
        />
        <div v-else class="comments-list">
          <div v-for="(comment, index) in comments" :key="index" class="comment-card">
            <div class="comment-header">
              <span class="comment-badge" :class="comment.vote === 1 ? 'is-like' : 'is-dislike'">
                {{ comment.vote === 1 ? '👍 推荐' : '👎 不推荐' }}
              </span>
              <span class="comment-author">同行教师</span>
              <span class="comment-time">{{ formatTime(comment.updatedAt) }}</span>
            </div>
            <p class="comment-text">{{ comment.reason }}</p>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { EditPen, ChatDotRound, InfoFilled } from '@element-plus/icons-vue'
import { getCaseComments } from '@/api/business/textbookCase'

const props = defineProps({
  item: { type: Object, required: true },
  canVote: { type: Boolean, default: false },
  busy: { type: Boolean, default: false }
})

const emit = defineEmits(['vote'])

const choices = [
  { value: 1, label: '推荐', icon: '👍', count: 'likes' },
  { value: -1, label: '不推荐', icon: '👎', count: 'dislikes' }
]

const reasonDialogVisible = ref(false)
const pendingVote = ref(1)
const reasonText = ref('')
const reasonSaving = ref(false)
const reasonError = ref('')

const commentsDialogVisible = ref(false)
const comments = ref([])
const commentsLoading = ref(false)
const commentsError = ref(false)

function onQuickVote(targetVote) {
  if (props.busy || !props.canVote) return
  const currentVote = Number(props.item.myVote) || 0
  if (currentVote === targetVote) {
    // 再次点击相同选择 -> 撤销评价
    emit('vote', props.item, 0)
  } else {
    // 切换选择，保留原有理由
    emit('vote', props.item, targetVote)
  }
}

function openReasonDialog() {
  const currentVote = Number(props.item.myVote) || 0
  pendingVote.value = currentVote !== 0 ? currentVote : 1
  reasonText.value = props.item.myReason || ''
  reasonError.value = ''
  reasonDialogVisible.value = true
}

function submitWithReason() {
  if (reasonSaving.value || props.busy) return
  reasonSaving.value = true
  reasonError.value = ''
  emit('vote', props.item, pendingVote.value, reasonText.value, (success, message) => {
    reasonSaving.value = false
    if (success) reasonDialogVisible.value = false
    else reasonError.value = message || '评价保存失败，内容已保留，请重试'
  })
}

async function openCommentsDialog() {
  commentsDialogVisible.value = true
  await loadComments()
}

async function loadComments() {
  commentsLoading.value = true
  commentsError.value = false
  try {
    const res = await getCaseComments(props.item.caseId)
    comments.value = res.data || []
  } catch {
    commentsError.value = true
  } finally {
    commentsLoading.value = false
  }
}

function formatTime(isoStr) {
  if (!isoStr) return ''
  const date = new Date(isoStr)
  if (isNaN(date.getTime())) return String(isoStr)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${d} ${hh}:${mm}`
}
</script>

<style scoped>
.case-votes {
  display: inline-flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  font-size: 12px;
}

button {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 8px;
  min-height: 28px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: white;
  color: #606266;
  cursor: pointer;
  white-space: nowrap;
  font-size: 12px;
  transition: all 0.15s ease;
}

button:hover:not(:disabled) {
  border-color: #c6e2ff;
  color: #409eff;
  background: #ecf5ff;
}

button.chosen {
  color: #1676c9;
  background: #ecf5ff;
  border-color: #409eff;
  font-weight: 600;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

button:focus-visible {
  outline: 2px solid #409eff;
  outline-offset: 2px;
}

.count {
  font-variant-numeric: tabular-nums;
  margin-left: 2px;
}

.reason-btn, .view-comments-btn {
  padding: 0 4px;
  height: 28px;
  font-size: 12px;
}

.saving-hint {
  font-size: 12px;
  color: #909399;
}

.reason-dialog-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.case-info {
  margin: 0;
  font-size: 14px;
}

.muted {
  color: #909399;
  font-size: 13px;
}

.vote-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.privacy-tip {
  margin: 0;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.comments-container {
  min-height: 120px;
  max-height: 60vh;
  overflow-y: auto;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.comment-card {
  padding: 10px 12px;
  border-radius: 6px;
  background: #f8f9fa;
  border: 1px solid #ebeef5;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  font-size: 12px;
}

.comment-badge {
  padding: 2px 6px;
  border-radius: 3px;
  font-weight: 500;
  font-size: 11px;
}

.comment-badge.is-like {
  background: #f0f9eb;
  color: #67c23a;
  border: 1px solid #e1f3d8;
}

.comment-badge.is-dislike {
  background: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fde2e2;
}

.comment-author {
  color: #303133;
  font-weight: 500;
}

.comment-time {
  color: #909399;
  margin-left: auto;
}

.comment-text {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
