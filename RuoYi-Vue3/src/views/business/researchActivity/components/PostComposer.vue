<template>
  <el-dialog :model-value="modelValue" :title="dialogTitle" width="min(900px, 96vw)" destroy-on-close @close="close">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item v-if="!post?.postId" label="内容类型" prop="postType">
        <el-radio-group v-model="form.postType">
          <el-radio-button value="COMMENT">课堂反思</el-radio-button>
          <el-radio-button value="MOMENT">活动纪实</el-radio-button>
          <el-radio-button value="RESOURCE">课程资源</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <ResourceFields v-if="form.postType === 'RESOURCE'" v-model="resourceForm" />
      <el-form-item v-else label="正文" prop="contentHtml"><ResearchRichEditor v-model="form.contentHtml" :min-height="280" /></el-form-item>
      <el-form-item label="附件"><AttachmentFields :resources="post?.resources || []" @change="attachments = $event" /></el-form-item>
    </el-form>
    <template #footer><el-button @click="close">取消</el-button><el-button type="primary" :loading="submitting" @click="submit">保存</el-button></template>
  </el-dialog>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { saveResearchAttachmentPost } from '@/api/business/researchActivity.js'
import AttachmentFields from './AttachmentFields.vue'
import ResearchRichEditor from './ResearchRichEditor.vue'
import ResourceFields from './ResourceFields.vue'
import { createResourceForm, normalizeResourcePayload, validateLinks } from '../utils/resourceForm.js'

const props = defineProps({ modelValue: Boolean, topicId: { type: [Number, String], required: true }, post: { type: Object, default: null } })
const emit = defineEmits(['update:modelValue', 'saved'])
const formRef = ref()
const submitting = ref(false)
const attachments = ref({ retainedResourceIds: [], files: [] })
const form = reactive({ postType: 'COMMENT', contentHtml: '' })
const resourceForm = ref(createResourceForm())
const postTypeName = computed(() => ({ COMMENT: '课堂反思', MOMENT: '活动纪实', RESOURCE: '课程资源' })[form.postType] || '内容')
const dialogTitle = computed(() => `${props.post?.postId ? '编辑' : '发布'}${postTypeName.value}`)
// 先判断原 HTML 是否含图片/表格，再剥标签查文字；否则纯图正文会被误判为空
const nonemptyHtml = (_rule, value, callback) => {
  const html = String(value || '')
  if (/<img\b|<table\b/i.test(html)) return callback()
  if (/\S/.test(html.replace(/<[^>]*>/g, ' '))) return callback()
  callback(new Error('请输入正文'))
}
const rules = { contentHtml: [{ validator: nonemptyHtml, trigger: 'change' }] }

watch(() => props.modelValue, open => {
  if (!open) return
  form.postType = props.post?.postType || 'COMMENT'
  form.contentHtml = props.post?.contentHtml || ''
  resourceForm.value = createResourceForm(props.post || {})
  attachments.value = { retainedResourceIds: (props.post?.resources || []).filter(item => item.resourceType === 'F').map(item => item.resourceId), files: [] }
})

function close() { emit('update:modelValue', false) }
async function submit() {
  if (form.postType !== 'RESOURCE') await formRef.value.validate()
  submitting.value = true
  try {
    const course = form.postType === 'RESOURCE' ? normalizeResourcePayload(resourceForm.value) : null
    if (course) {
      const linkError = validateLinks(course.links)
      if (linkError) throw new Error(linkError)
      if (!attachments.value.files.length && !attachments.value.retainedResourceIds.length && !course.links.length) throw new Error('请上传附件或添加云盘链接')
    }
    await saveResearchAttachmentPost(props.topicId, props.post?.postId, {
      postType: form.postType, contentHtml: course ? course.contentHtml : form.contentHtml,
      course, retainedResourceIds: attachments.value.retainedResourceIds
    }, attachments.value.files)
    ElMessage.success(`${postTypeName.value}${props.post?.postId ? '已更新' : '已发布'}`)
    emit('saved'); close()
  } catch (error) {
    if (!error?.response) ElMessage.error(error?.message || '请检查表单')
  } finally { submitting.value = false }
}
</script>
