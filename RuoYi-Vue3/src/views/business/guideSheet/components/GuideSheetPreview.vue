<template>
  <section class="preview-workbench">
    <div class="preview-toolbar">
      <strong>查看学生实际填写效果</strong>
      <div class="preview-device-tools">
        <span class="device-hint">默认电脑宽屏预览</span>
        <el-segmented v-model="device" :options="deviceOptions" size="small" />
      </div>
    </div>
    <div class="preview-stage" :class="`is-${device}`">
      <div class="device-frame">
        <div v-if="device === 'student'" class="student-bar">
          <span>课堂学习</span>
          <el-tag type="info" effect="plain" size="small">学生端</el-tag>
        </div>
        <v-form-render
          v-if="formJson"
          :key="renderKey"
          :form-json="safeFormJson"
          :form-data="{}"
          :option-data="{}"
        />
        <el-empty v-else description="选择课堂结构后即可预览" :image-size="72" />
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, ref } from 'vue'
import { createSafePreviewFormJson } from '../utils/formJsonBridge.js'

const props = defineProps({ formJson: { type: Object, default: null } })
// 默认电脑宽屏；窄屏/学生端仅作教师预览，不以手机为主路径
const device = ref('pc')
const deviceOptions = [
  { label: '电脑', value: 'pc' },
  { label: '窄屏', value: 'tablet' },
  { label: '学生填写', value: 'student' }
]
const safeFormJson = computed(() => createSafePreviewFormJson(props.formJson))
const renderKey = computed(() => JSON.stringify(safeFormJson.value || {}))
</script>

<style scoped>
.preview-workbench { min-width: 0; border: 1px solid #dce5e7; border-radius: 6px; background: #fff; }
.preview-toolbar { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 12px 16px; border-bottom: 1px solid #e1e8ea; }
.preview-toolbar strong { color: #29434d; font-size: 14px; }
.preview-device-tools { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.device-hint { color: #7a8b91; font-size: 12px; }
.preview-stage { min-height: 520px; padding: 22px; overflow: auto; background: #edf1f2; }
.device-frame { width: min(100%, 1040px); min-height: 470px; padding: 22px; margin: 0 auto; border: 1px solid #d7e0e2; border-radius: 6px; background: #fff; box-shadow: 0 12px 28px rgba(43, 64, 72, 0.08); transition: width 180ms ease; }
.is-tablet .device-frame { width: min(100%, 768px); }
.is-student .device-frame { width: min(100%, 430px); padding: 14px; }
.student-bar { display: flex; align-items: center; justify-content: space-between; padding-bottom: 10px; margin-bottom: 14px; color: #31515b; border-bottom: 1px solid #e3e9eb; font-size: 13px; font-weight: 650; }
@media (max-width: 680px) {
  .preview-toolbar { align-items: flex-start; flex-direction: column; }
  .preview-stage { min-height: 400px; padding: 8px; }
  .device-frame { min-height: 360px; padding: 10px; }
}
</style>
