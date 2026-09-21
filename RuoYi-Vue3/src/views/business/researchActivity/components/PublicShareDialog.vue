<template>
  <el-dialog v-model="visible" title="分享活动通知" width="520px" @open="loadStatus">
    <el-alert type="info" :closable="false" show-icon title="链接无需登录即可公开查看，包含通知正文、活动时间及教师交流留言。" />
    <el-form label-width="100px" class="share-form">
      <el-form-item label="有效期">
        <el-select v-model="expireDays" :disabled="generating" style="width: 180px">
          <el-option :value="7" label="7天" /><el-option :value="30" label="30天" /><el-option :value="0" label="永久" />
        </el-select>
      </el-form-item>
      <el-form-item label="当前状态"><span>{{ loading ? '读取中' : status.enabled ? (status.expireTime ? `有效至 ${status.expireTime}` : '永久有效') : '未启用或已失效' }}</span></el-form-item>
      <el-alert v-if="!loading && status.enabled && !shareUrl" type="warning" :closable="false" title="旧链接仍有效，但未保存可恢复的链接；重新生成后旧链接将失效。" />
      <el-form-item v-if="shareUrl" label="分享链接">
        <el-input v-model="shareUrl" readonly @focus="$event.target.select()"><template #append><el-button @click="copyUrl">复制</el-button></template></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-if="status.enabled" :disabled="loading || generating" type="danger" plain @click="revoke">撤销链接</el-button>
      <el-button type="primary" :disabled="loading" :loading="generating" @click="generate">{{ status.enabled ? '重新生成' : '生成链接' }}</el-button>
    </template>
  </el-dialog>
</template>
<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import { createResearchPublicShare, getResearchPublicShare, revokeResearchPublicShare } from '@/api/business/researchActivity.js'
const props = defineProps({ modelValue: Boolean, topicId: [String, Number] })
const emit = defineEmits(['update:modelValue'])
const visible = computed({ get: () => props.modelValue, set: value => emit('update:modelValue', value) })
const expireDays = ref(30); const generating = ref(false); const shareUrl = ref(''); const status = ref({ enabled: false, expireTime: null })
const loading = ref(false)
function applyStatus(data) {
  status.value = data || { enabled: false, expireTime: null }
  shareUrl.value = status.value.enabled && status.value.shareUrl ? `${window.location.origin}/public/research-notice/${status.value.shareUrl}` : ''
}
async function loadStatus() {
  loading.value = true
  applyStatus(null)
  try { applyStatus((await getResearchPublicShare(props.topicId)).data) }
  finally { loading.value = false }
}
async function generate() {
  if (status.value.enabled) {
    try { await ElMessageBox.confirm('重新生成后原链接立即失效，确认继续吗？', '重新生成分享链接', { type: 'warning' }) }
    catch { return }
  }
  generating.value = true
  try {
    applyStatus((await createResearchPublicShare(props.topicId, expireDays.value)).data)
    ElMessage.success('分享链接已生成')
  } finally { generating.value = false }
}
async function copyUrl() {
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(shareUrl.value)
    } else {
      const input = document.createElement('textarea')
      input.value = shareUrl.value
      input.style.position = 'fixed'
      input.style.opacity = '0'
      document.body.appendChild(input)
      input.focus()
      input.select()
      if (!document.execCommand('copy')) throw new Error('copy failed')
      document.body.removeChild(input)
    }
    ElMessage.success('链接已复制')
  } catch {
    ElMessage.info('复制失败，请手动选中链接复制')
  }
}
async function revoke() { await ElMessageBox.confirm('撤销后原链接立即失效，确认继续吗？', '撤销分享', { type: 'warning' }); await revokeResearchPublicShare(props.topicId); shareUrl.value = ''; status.value = { enabled: false, expireTime: null }; ElMessage.success('分享链接已撤销') }
</script>
<style scoped>.share-form { margin-top: 18px; }</style>
