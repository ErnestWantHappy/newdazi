<template>
  <el-dialog :model-value="modelValue" title="再次通知" width="620px" destroy-on-close @close="close">
    <el-form label-width="90px">
      <el-form-item label="通知范围"><el-radio-group v-model="form.noticeScope"><el-radio value="1">按学段</el-radio><el-radio value="2">指定教师</el-radio></el-radio-group></el-form-item>
      <el-form-item v-if="form.noticeScope === '1'" label="接收学段">
        <el-checkbox-group v-model="form.stageCodes"><el-checkbox v-for="item in SCHOOL_TYPES" :key="item.value" :value="item.value">{{ item.label }}</el-checkbox></el-checkbox-group>
      </el-form-item>
      <el-form-item v-else label="接收教师">
        <el-select v-model="form.teacherUserIds" multiple filterable remote :remote-method="searchTeachers" :loading="loading" placeholder="输入姓名、账号或手机号">
          <el-option v-for="teacher in teachers" :key="teacher.userId" :label="`${teacher.nickName}（${teacher.deptName || teacher.userName}）`" :value="teacher.userId" />
        </el-select>
      </el-form-item>
      <el-alert type="warning" :closable="false" title="再次通知会将这些教师的本主题通知重置为未读。" />
    </el-form>
    <template #footer><el-button @click="close">取消</el-button><el-button type="primary" :loading="submitting" @click="submit">发送通知</el-button></template>
  </el-dialog>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { listResearchTeacherTargets, notifyResearchTopic } from '@/api/business/researchActivity.js'
import { SCHOOL_TYPES } from '../utils/researchActivityFormat.js'

const props = defineProps({ modelValue: Boolean, topicId: { type: [Number, String], default: null } })
const emit = defineEmits(['update:modelValue', 'sent'])
const form = reactive({ noticeLevel: '1', noticeScope: '1', stageCodes: [], teacherUserIds: [] })
const teachers = ref([])
const loading = ref(false)
const submitting = ref(false)
function close() { emit('update:modelValue', false) }
async function searchTeachers(keyword) {
  if (!keyword?.trim()) return
  loading.value = true
  try { teachers.value = (await listResearchTeacherTargets({ keyword: keyword.trim(), pageNum: 1, pageSize: 20 })).rows || [] }
  finally { loading.value = false }
}
async function submit() {
  if (form.noticeScope === '1' && !form.stageCodes.length) return ElMessage.warning('请选择至少一个学段')
  if (form.noticeScope === '2' && !form.teacherUserIds.length) return ElMessage.warning('请选择至少一名教师')
  submitting.value = true
  try {
    const response = await notifyResearchTopic(props.topicId, { ...form })
    ElMessage.success(`已通知 ${response.data || 0} 个教师账号`)
    emit('sent'); close()
  } finally { submitting.value = false }
}
</script>

<style scoped>:deep(.el-select) { width: 100%; }</style>
