<template>
  <el-dialog :model-value="modelValue" :title="topic?.topicId ? '编辑主题' : '发布主题'" width="760px" destroy-on-close @close="close">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="主题类型" prop="topicType">
        <el-radio-group v-model="form.topicType" :disabled="!!topic?.topicId">
          <el-radio-button value="SHARE">交流分享</el-radio-button>
          <el-radio-button v-if="manager" value="NOTICE">活动通知</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="标题" prop="title"><el-input v-model="form.title" maxlength="200" show-word-limit /></el-form-item>
      <el-form-item label="正文" prop="contentHtml"><ResearchRichEditor v-model="form.contentHtml" :min-height="260" /></el-form-item>
      <template v-if="manager && form.topicType === 'NOTICE'">
        <el-form-item label="活动时间" prop="activityTime">
          <el-date-picker v-model="form.activityTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" format="YYYY-MM-DD HH:mm" placeholder="可选；活动开始前持续显示在教师首页" />
        </el-form-item>
        <template v-if="!topic?.topicId">
          <el-form-item label="通知范围" prop="noticeScope">
            <el-radio-group v-model="form.noticeScope"><el-radio value="1">按学段</el-radio><el-radio value="2">指定教师</el-radio></el-radio-group>
          </el-form-item>
          <el-form-item v-if="form.noticeScope === '1'" label="接收学段" prop="stageCodes">
            <el-checkbox-group v-model="form.stageCodes">
              <el-checkbox v-for="item in SCHOOL_TYPES" :key="item.value" :value="item.value">{{ item.label }}</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <el-form-item v-else label="接收教师" prop="teacherUserIds">
            <el-select v-model="form.teacherUserIds" multiple filterable remote :remote-method="searchTeachers" :loading="teacherLoading" placeholder="输入姓名、账号或手机号检索">
              <el-option v-for="teacher in teachers" :key="teacher.userId" :label="`${teacher.nickName}（${teacher.deptName || teacher.userName}）`" :value="teacher.userId" />
            </el-select>
          </el-form-item>
        </template>
      </template>
      <el-alert v-if="topic?.topicId && topic.topicType === 'NOTICE'" type="info" :closable="false" title="编辑主题不会再次发送通知；如需提醒，请使用“再次通知”。" />
    </el-form>
    <template #footer><el-button @click="close">取消</el-button><el-button type="primary" :loading="submitting" @click="submit">保存并发布</el-button></template>
  </el-dialog>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { createResearchTopic, listResearchTeacherTargets, updateResearchTopic } from '@/api/business/researchActivity.js'
import { SCHOOL_TYPES, isFutureActivityTime } from '../utils/researchActivityFormat.js'
import ResearchRichEditor from './ResearchRichEditor.vue'

const props = defineProps({ modelValue: Boolean, topic: { type: Object, default: null }, manager: Boolean })
const emit = defineEmits(['update:modelValue', 'saved'])
const formRef = ref()
const submitting = ref(false)
const teacherLoading = ref(false)
const teachers = ref([])
const emptyForm = () => ({ topicType: 'SHARE', title: '', contentHtml: '', noticeLevel: '1', noticeScope: '1', activityTime: null, stageCodes: [], teacherUserIds: [] })
const form = reactive(emptyForm())
const nonemptyHtml = (_rule, value, callback) => /<img\b|<table\b|\S/.test(String(value || '').replace(/<[^>]*>/g, ' ')) ? callback() : callback(new Error('请输入主题正文'))
const rules = {
  topicType: [{ required: true, message: '请选择主题类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  contentHtml: [{ validator: nonemptyHtml, trigger: 'change' }],
  noticeScope: [{ required: true, message: '请选择通知范围', trigger: 'change' }]
}

watch(() => props.modelValue, open => {
  if (!open) return
  Object.assign(form, emptyForm(), props.topic ? {
    topicType: props.topic.topicType,
    title: props.topic.title,
    contentHtml: props.topic.contentHtml,
    noticeLevel: props.topic.noticeLevel || '1',
    noticeScope: props.topic.noticeScope || '1',
    activityTime: props.topic.activityTime || null,
    stageCodes: props.topic.noticeStages ? props.topic.noticeStages.split(',').filter(Boolean) : []
  } : {})
})

function close() { emit('update:modelValue', false) }
async function searchTeachers(keyword) {
  if (!keyword?.trim()) return
  teacherLoading.value = true
  try { teachers.value = (await listResearchTeacherTargets({ keyword: keyword.trim(), pageNum: 1, pageSize: 20 })).rows || [] }
  finally { teacherLoading.value = false }
}
async function submit() {
  await formRef.value.validate()
  const activityTimeChanged = form.activityTime !== (props.topic?.activityTime || null)
  if (form.topicType === 'NOTICE' && form.activityTime && activityTimeChanged && !isFutureActivityTime(form.activityTime)) return ElMessage.warning('活动时间必须晚于当前时间')
  if (form.topicType === 'NOTICE' && !props.topic?.topicId) {
    if (form.noticeScope === '1' && !form.stageCodes.length) return ElMessage.warning('请选择至少一个学段')
    if (form.noticeScope === '2' && !form.teacherUserIds.length) return ElMessage.warning('请选择至少一名教师')
  }
  const payload = { ...form }
  if (form.topicType !== 'NOTICE') Object.assign(payload, { noticeLevel: '0', noticeScope: '0', activityTime: null, stageCodes: [], teacherUserIds: [] })
  submitting.value = true
  try {
    if (props.topic?.topicId) await updateResearchTopic(props.topic.topicId, payload)
    else await createResearchTopic(payload)
    ElMessage.success(props.topic?.topicId ? '主题已更新' : '主题已发布')
    emit('saved'); close()
  } finally { submitting.value = false }
}
</script>

<style scoped>
:deep(.el-select) { width: 100%; }
</style>
