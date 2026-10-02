<template>
  <el-dialog v-model="visible" title="数据分析实验题" width="min(760px, 94vw)" append-to-body :close-on-click-modal="false">
    <el-form v-loading="loading" label-width="90px" :disabled="saving">
      <el-form-item label="实验名称"><el-input v-model="form.experimentName" maxlength="200" /></el-form-item>
      <el-form-item label="实验说明"><el-input v-model="form.description" type="textarea" :rows="4" maxlength="10000" /></el-form-item>
      <el-form-item label="Excel 文件">
        <input type="file" accept=".xlsx" :disabled="uploading || saving" @change="upload" />
        <span>{{ uploading ? '正在上传' : form.excelFileName }}</span>
      </el-form-item>
      <el-form-item label="初始代码"><el-input v-model="form.starterCode" type="textarea" :rows="9" maxlength="20000" /></el-form-item>
      <el-form-item label="满分"><el-input-number v-model="form.maxScore" :min="1" :max="100" /></el-form-item>
    </el-form>
    <template #footer><el-button :disabled="saving" @click="visible=false">取消</el-button><el-button type="primary" :loading="saving" :disabled="loading || uploading" @click="save">保存实验题</el-button></template>
  </el-dialog>
</template>
<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { addQuestion, getQuestion, updateQuestion } from '@/api/business/question'
import { getDataLabConfig, saveDataLabConfig, uploadDataLabExcel } from '@/api/business/dataLab'
const emit = defineEmits(['saved'])
const visible=ref(false), loading=ref(false), saving=ref(false), uploading=ref(false), form=ref({})
let question = {}, generation = 0
async function open(questionId) {
  const current = ++generation
  visible.value=true; loading.value=true
  form.value={ experimentName:'', description:'', starterCode:'import pandas as pd\nimport matplotlib.pyplot as plt\n', maxScore:100 }
  question={ questionType:'practical', practicalMode:'DATA_LAB', difficulty:'MEDIUM', isPublic:'Y' }
  try {
    if (questionId) {
      const [q,c] = await Promise.all([getQuestion(questionId), getDataLabConfig(questionId)])
      if (current !== generation) return
      question=q.data
      form.value={...form.value,...c.data,questionId}
    }
  } finally { if (current === generation) loading.value=false }
}
async function upload(event) {
  const file=event.target.files?.[0], current=generation
  event.target.value=''
  if (!file) return
  if (!/\.xlsx$/i.test(file.name) || file.size>10*1024*1024) return ElMessage.warning('请选择不超过10MB的xlsx文件')
  uploading.value=true
  try {
    const body=new FormData(); body.append('file',file)
    const {data}=await uploadDataLabExcel(body)
    if (current===generation) Object.assign(form.value,{excelFileUrl:data.url,excelFileName:data.fileName,excelFileSize:data.fileSize})
  } finally { uploading.value=false }
}
async function save() {
  if (!form.value.experimentName?.trim() || !form.value.excelFileUrl) return ElMessage.warning('请填写实验名称并上传Excel文件')
  saving.value=true
  try {
    Object.assign(question,{questionContent:form.value.experimentName.trim(),practicalMode:'DATA_LAB',questionType:'practical'})
    if (question.questionId) await updateQuestion(question)
    else { const res=await addQuestion(question); question.questionId=res.questionId || res.data?.questionId }
    if (!question.questionId) throw new Error('保存未返回题目编号')
    await saveDataLabConfig({...form.value,questionId:question.questionId})
    visible.value=false; emit('saved'); ElMessage.success('实验题已保存')
  } finally { saving.value=false }
}
defineExpose({open})
</script>
