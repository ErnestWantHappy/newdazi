<template>
  <div class="app-container guide-sheet-preview">
    <div class="preview-header">
      <el-button icon="ArrowLeft" @click="goBack">返回</el-button>
      <h2 style="margin:0 16px">{{ sheetTitle || '导学单预览' }}</h2>
      <el-tag type="info" effect="plain">学生可见内容</el-tag>
    </div>

    <div v-if="loading" style="text-align:center;padding:60px 0">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      <p>正在加载导学单...</p>
    </div>

    <div v-else-if="!formJsonObj" style="text-align:center;padding:60px 0">
      <el-empty description="该导学单暂无内容" />
    </div>

    <div v-else style="max-width:900px;margin:16px auto">
      <v-form-render
        ref="renderRef"
        :form-json="formJsonObj"
        :form-data="formData"
        :option-data="optionData"
      />
    </div>
  </div>
</template>

<script setup name="GuideSheetPreview">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getGuideSheetPreview } from '@/api/business/guideSheet'
import { Loading } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const sheetTitle = ref('')
const formJsonObj = ref(null)
const formData = ref({})
const optionData = ref({})
const loading = ref(true)
const renderRef = ref(null)

function goBack() {
  router.back()
}

onMounted(() => {
  const sheetId = route.params.sheetId
  if (sheetId) {
    // 必须走服务端脱敏接口：模板库预览与课程面板预览共用同一份“学生可见内容”，
    // 避免同县他人公共模板里的 onCreated / functions 在本教师浏览器执行。
    getGuideSheetPreview(sheetId).then(res => {
      const data = res.data || res
      sheetTitle.value = data.title || data.sheetTitle || ''
      if (data.formJson) {
        try {
          formJsonObj.value = typeof data.formJson === 'string'
            ? JSON.parse(data.formJson)
            : data.formJson
        } catch (e) {
          console.warn('表单JSON解析失败', e)
        }
      }
      loading.value = false
    }).catch(() => { loading.value = false })
  } else {
    loading.value = false
  }
})
</script>

<style scoped>
.preview-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>