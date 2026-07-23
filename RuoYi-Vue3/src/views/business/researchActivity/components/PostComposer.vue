<template>
  <el-dialog :model-value="modelValue" :title="post?.postId ? '编辑留言' : '发布留言'" width="900px" destroy-on-close @close="close">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item v-if="!post?.postId" label="留言类型" prop="postType">
        <el-radio-group v-model="form.postType">
          <el-radio-button value="COMMENT">普通留言</el-radio-button>
          <el-radio-button value="MOMENT">活动纪实</el-radio-button>
          <el-radio-button value="RESOURCE">课程资源</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <ResourceFields v-if="form.postType === 'RESOURCE'" v-model="resourceForm" :resources="post?.resources || []" @update:file="selectedFile = $event" />
      <el-form-item v-else label="正文" prop="contentHtml"><ResearchRichEditor v-model="form.contentHtml" :min-height="280" /></el-form-item>
    </el-form>
    <template #footer><el-button @click="close">取消</el-button><el-button type="primary" :loading="submitting" @click="submit">保存</el-button></template>
  </el-dialog>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { createResearchPost, saveResearchResourcePost, updateResearchPost } from '@/api/business/researchActivity.js'
import ResearchRichEditor from './ResearchRichEditor.vue'
import ResourceFields from './ResourceFields.vue'
import { createResourceForm, normalizeResourcePayload, validateLinks, validatePackageFile } from '../utils/resourceForm.js'

const props = defineProps({ modelValue: Boolean, topicId: { type: [Number, String], required: true }, post: { type: Object, default: null } })
const emit = defineEmits(['update:modelValue', 'saved'])
const formRef = ref()
const submitting = ref(false)
const selectedFile = ref(null)
const form = reactive({ postType: 'COMMENT', contentHtml: '' })
const resourceForm = ref(createResourceForm())
const nonemptyHtml = (_rule, value, callback) => /<img\b|<table\b|\S/.test(String(value || '').replace(/<[^>]*>/g, ' ')) ? callback() : callback(new Error('请输入正文'))
const rules = { contentHtml: [{ validator: nonemptyHtml, trigger: 'change' }] }

watch(() => props.modelValue, open => {
  if (!open) return
  form.postType = props.post?.postType || 'COMMENT'
  form.contentHtml = props.post?.contentHtml || ''
  resourceForm.value = createResourceForm(props.post || {})
  selectedFile.value = null
})

function close() { emit('update:modelValue', false) }
async function submit() {
  if (form.postType !== 'RESOURCE') await formRef.value.validate()
  submitting.value = true
  try {
    if (form.postType === 'RESOURCE') {
      const payload = normalizeResourcePayload(resourceForm.value)
      const linkError = validateLinks(payload.links)
      const fileError = validatePackageFile(selectedFile.value)
      if (linkError || fileError) throw new Error(linkError || fileError)
      const hasExistingFile = props.post?.resources?.some(item => item.resourceType === 'F') && payload.fileAction === 'KEEP'
      if (!selectedFile.value && !hasExistingFile && !payload.links.length) throw new Error('请上传主课件或至少添加一个云盘链接')
      await saveResearchResourcePost(props.topicId, props.post?.postId, payload, selectedFile.value)
    } else {
      const payload = { postType: form.postType, contentHtml: form.contentHtml }
      if (props.post?.postId) await updateResearchPost(props.post.postId, payload)
      else await createResearchPost(props.topicId, payload)
    }
    ElMessage.success(props.post?.postId ? '留言已更新' : '留言已发布')
    emit('saved'); close()
  } catch (error) {
    if (!error?.response) ElMessage.error(error?.message || '请检查表单')
  } finally { submitting.value = false }
}
</script>
