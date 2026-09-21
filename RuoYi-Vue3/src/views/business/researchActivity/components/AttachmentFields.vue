<template>
  <div class="attachment-fields">
    <div v-for="item in retained" :key="item.resourceId" class="existing-attachment">
      <span>{{ item.originalFileName || item.resourceName }}</span>
      <el-button icon="Delete" link type="danger" :title="`移除 ${item.originalFileName}`" @click="removeExisting(item.resourceId)" />
    </div>
    <el-upload v-model:file-list="fileList" action="#" multiple :auto-upload="false" :limit="10 - retained.length"
      accept=".zip,.rar,.7z,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx" :on-change="change" :on-remove="removed"
      :on-exceed="() => ElMessage.warning('每条留言最多10个附件')">
      <el-button icon="Upload">添加附件</el-button>
      <template #tip><div class="el-upload__tip">Word、PDF、Excel、PowerPoint、ZIP、RAR、7z；最多10个，每次上传合计不超过50 MB</div></template>
    </el-upload>
  </div>
</template>
<script setup>
import { ElMessage } from 'element-plus'
import { validatePackageFile, MAX_FILE_BYTES } from '../utils/resourceForm.js'
const props = defineProps({ resources: { type: Array, default: () => [] } })
const emit = defineEmits(['change'])
const fileList = ref([])
const retained = ref(props.resources.filter(item => item.resourceType === 'F'))
function sync() { emit('change', { retainedResourceIds: retained.value.map(item => item.resourceId), files: fileList.value.map(item => item.raw) }) }
function removeExisting(id) { retained.value = retained.value.filter(item => item.resourceId !== id); sync() }
function removed(_file, files) { fileList.value = files; sync() }
function change(file, files) {
  fileList.value = files
  const error = validatePackageFile(file.raw)
  const oversized = fileList.value.reduce((sum, item) => sum + (item.raw?.size || 0), 0) > MAX_FILE_BYTES
  if (error || oversized) {
    fileList.value = fileList.value.filter(item => item.uid !== file.uid)
    ElMessage.error(error || '本次上传合计不能超过50 MB')
  }
  sync()
}
</script>
<style scoped>
.existing-attachment { display: flex; gap: 12px; align-items: center; margin-bottom: 8px; }
.existing-attachment span { overflow-wrap: anywhere; min-width: 0; }
</style>
