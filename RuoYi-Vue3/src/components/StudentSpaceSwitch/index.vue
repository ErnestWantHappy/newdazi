<template>
  <span v-if="!login && ownedContext?.extensionClass" class="space-switch">
    <span>{{ space === 'extension' ? `拓展课 · ${ownedContext.extensionClass.class_name}` : `常规课 · ${ownedContext.regularLabel}` }}</span>
    <el-button link @click="open(false)">切换</el-button>
  </span>
  <el-button v-if="!login && loadError" link type="danger" @click="reload">重试学习空间</el-button>
  <el-dialog v-model="visible" title="选择学习空间" width="min(420px, 94vw)" :close-on-click-modal="!required" :close-on-press-escape="!required" :show-close="!required" append-to-body>
    <el-radio-group v-model="choice" class="space-options" :disabled="saving">
      <el-radio value="regular" label="regular" border>常规课（{{ ownedContext?.regularLabel }}）</el-radio>
      <el-radio value="extension" label="extension" border>拓展课（{{ ownedContext?.extensionClass?.class_name }}）</el-radio>
    </el-radio-group>
    <el-alert v-if="error" :title="error" type="error" :closable="false" style="margin-top:12px" />
    <template #footer><el-button type="primary" :loading="saving" :disabled="!choice" @click="confirm">进入所选空间</el-button></template>
  </el-dialog>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import useStudentSpace from '@/store/modules/studentSpace'
import useUserStore from '@/store/modules/user'
const props = defineProps({ space: { type: String, default: 'regular' }, login: Boolean })
const router = useRouter(), store = useStudentSpace(), userStore = useUserStore()
// 只渲染属于当前账号的上下文，换学生账号时不显示上一账号的班级名。
const ownedContext = computed(() => (store.account && store.account === userStore.id ? store.context : null))
const loadError = ref(false)
const visible = ref(false), required = ref(false), choice = ref(''), saving = ref(false), error = ref('')
let resolveLogin
function open(first) { required.value = first; choice.value = first ? '' : props.space; error.value = ''; visible.value = true }
async function selectAtLogin() {
  // 学习空间是可选功能：接口故障时降级进常规首页，不能阻断学生登录。
  let context = null
  try {
    context = await store.load(true)
  } catch (e) {
    return '/student/index'
  }
  if (!context?.extensionClass) return '/student/index'
  if (Number(context.space_selected) === 1) return context.space_pref === 'extension' ? '/student/extension' : '/student/index'
  open(true)
  return new Promise(resolve => { resolveLogin = resolve })
}
async function confirm() {
  saving.value = true; error.value = ''
  try {
    const target = await store.select(choice.value)
    visible.value = false
    if (resolveLogin) { resolveLogin(target); resolveLogin = null } else await router.push(target)
  } catch (e) { error.value = e?.message || '保存失败，请重试' }
  finally { saving.value = false }
}
async function reload() {
  if (props.login) return
  loadError.value = false
  try {
    const context = await store.load(true)
    if (context?.extensionClass && Number(context.space_selected) !== 1) open(true)
  } catch (e) { loadError.value = true; error.value = e?.message || '学习空间加载失败' }
}
onMounted(reload)
defineExpose({ selectAtLogin })
</script>
<style scoped>
.space-switch{display:inline-flex;align-items:center;gap:8px;max-width:100%;font-size:13px;color:#1f5fbf}.space-switch>span{overflow-wrap:anywhere}.space-options{display:flex;flex-direction:column;align-items:stretch;width:100%;gap:12px}.space-options :deep(.el-radio){margin:0;width:100%;height:auto;min-height:42px;white-space:normal}.space-options :deep(.el-radio__label){white-space:normal;line-height:1.5}
</style>
