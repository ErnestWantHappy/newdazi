<!-- 工作台题目缩略卡片：只显示当前题型真正用的字段，完整内容走详情/预览。 -->
<template>
  <article class="qcard" :class="`qcard--${question.questionType}`">
    <div class="qcard__body">
      <div class="qcard__title" role="button" tabindex="0" @click="emit('primary')" @keydown.enter.prevent="emit('primary')" @keydown.space.prevent="emit('primary')">
        <el-icon v-if="icon" class="qcard__icon"><component :is="icon" /></el-icon>
        <span class="qcard__title-text">{{ title }}</span>
        <el-tag v-if="hasQuestionImages(question)" size="small" type="info">含图</el-tag>
      </div>
      <ul v-if="options.length" class="qcard__options">
        <li v-for="item in options" :key="item.key"><b>{{ item.key }}</b>. {{ item.text }}<span v-if="item.hasImage" class="qcard__image-label">▧ 图片</span></li>
      </ul>
      <div class="qcard__meta">
        <span>出题教师：{{ question.nickName || '姓名未登记' }}</span>
        <span v-if="typingMeta">{{ typingMeta }}</span>
        <span v-if="scopeMeta">{{ scopeMeta }}</span>
        <span v-if="pythonMeta">{{ pythonMeta }}</span>
        <span v-if="practicalMeta">{{ practicalMeta }}</span>
        <span v-if="answerText" class="qcard__answer">{{ answerText }}</span>
        <span v-if="question.isPublic === 'N'" class="qcard__private">未公开</span>
      </div>
    </div>
    <div v-if="actions.length" class="qcard__actions">
      <el-button v-if="actions.includes('view')" link type="primary" @click="emit('view')">查看</el-button>
      <el-button v-if="actions.includes('preview')" link type="primary" @click="emit('preview')">预览</el-button>
      <el-button v-if="actions.includes('edit')" link type="primary" @click="emit('edit')">编辑</el-button>
      <el-button v-if="actions.includes('remove')" link class="qcard__danger" @click="emit('remove')">删除</el-button>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import { hasQuestionImages } from '@/utils/questionMedia'
import {
  answerLabel,
  cardTitle,
  choiceOptions,
  gradeScopeText,
  practicalModeMeta,
} from '@/utils/questionWorkbench'

const props = defineProps({
  question: { type: Object, required: true },
  actions: { type: Array, default: () => [] },
  gradeDict: { type: Array, default: () => [] },
  semesterDict: { type: Array, default: () => [] },
})
const emit = defineEmits(['view', 'preview', 'edit', 'remove', 'primary'])

const title = computed(() => cardTitle(props.question))
const options = computed(() => (props.question.questionType === 'choice' ? choiceOptions(props.question).slice(0, 4) : []))
const answerText = computed(() => answerLabel(props.question))
const scopeMeta = computed(() => gradeScopeText(props.question, props.gradeDict, props.semesterDict))

const icon = computed(() => {
  if (props.question.questionType === 'typing') return 'Grid'
  if (props.question.questionType === 'choice') return 'List'
  if (props.question.questionType === 'judgment') return 'Select'
  return null
})

const typingMeta = computed(() => {
  if (props.question.questionType !== 'typing') return ''
  const count = props.question.wordCount ? `${props.question.wordCount} 字` : ''
  const duration = props.question.typingDuration ? `${props.question.typingDuration} 分钟` : ''
  return [count, duration].filter(Boolean).join(' · ')
})

const pythonMeta = computed(() => {
  if (props.question.practicalMode !== 'PYTHON') return ''
  return [props.question.knowledgePoints, props.question.testCaseCount ? `${props.question.testCaseCount} 个测试点` : '']
    .filter(Boolean)
    .join(' · ')
})

const practicalMeta = computed(() => {
  if (props.question.questionType !== 'practical') return ''
  const mode = practicalModeMeta(props.question.practicalMode)
  if (mode.key !== 'FILE') return mode.label
  const extensions = String(props.question.practicalAllowedExtensions || '')
    .split(',')
    .filter(Boolean)
    .map(item => item.toUpperCase())
  return extensions.length > 3 ? `${extensions.slice(0, 3).join(' / ')} 等 ${extensions.length} 种格式` : extensions.join(' / ')
})
</script>

<style scoped>
.qcard {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 132px;
  padding: 12px 14px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fff;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.qcard:hover { border-color: #c6d6f5; }
.qcard__title {
  display: flex;
  gap: 6px;
  align-items: flex-start;
  font-weight: 600;
  color: #1f2329;
  line-height: 1.5;
  cursor: pointer;
  border-radius: 4px;
}
.qcard__title:focus-visible { outline: 2px solid #409eff; outline-offset: 2px; }
.qcard__icon { margin-top: 2px; color: #409eff; flex-shrink: 0; }
/* 题干压到 2～3 行，长文本不能横向撑破卡片 */
.qcard__title-text {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  overflow-wrap: anywhere;
}
.qcard__excerpt {
  display: -webkit-box;
  margin: 4px 0 0;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  overflow-wrap: anywhere;
  color: #606266;
  font-size: 12px;
  line-height: 1.6;
}
.qcard__options {
  margin: 4px 0 0;
  padding: 0;
  list-style: none;
  color: #606266;
  font-size: 12px;
  line-height: 1.6;
}
.qcard__options li { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.qcard__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 10px;
  margin-top: 6px;
  color: #8a9099;
  font-size: 12px;
}
.qcard__answer { color: #5b6b8a; }
.qcard__private { color: #e6a23c; }
.qcard__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 2px;
  justify-content: flex-end;
  margin-top: 8px;
}
/* 删除保留克制的红色文字按钮，不做成和编辑同级的主操作 */
.qcard__danger { color: #c45656; }
</style>
