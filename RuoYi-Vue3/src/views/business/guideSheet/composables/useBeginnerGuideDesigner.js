import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import useUserStore from '@/store/modules/user'
import { completeGuideSheetDraft } from '@/api/business/guideSheet'

function clone(value) {
  return value == null ? value : JSON.parse(JSON.stringify(value))
}

export function useBeginnerGuideDesigner({
  route,
  router,
  form,
  rawFormJson,
  dirty,
  saveTemplate,
  hydrateAdvancedDesigner,
  readAdvancedFormJson
}) {
  const userStore = useUserStore()
  const editorMode = ref('beginner')
  const loadingSheet = ref(Boolean(route.params.sheetId || route.query.copyFrom))
  const invalidFormJson = ref('')
  const allowNavigation = ref(false)

  function ownerKey() {
    return String(userStore.id || 'anonymous')
  }

  function resolveDraftKey() {
    const owner = ownerKey()
    if (route.params.sheetId) return `user-${owner}-sheet-${route.params.sheetId}`
    if (route.query.copyFrom) return `user-${owner}-copy-${route.query.copyFrom}`
    const storageKey = `guide-sheet:new-draft-key:${owner}`
    try {
      const existing = localStorage.getItem(storageKey)
      if (existing) return existing
      const created = `user-${owner}-new-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 8)}`
      localStorage.setItem(storageKey, created)
      return created
    } catch {
      return `user-${owner}-new-${Date.now().toString(36)}`
    }
  }

  const draftKey = ref(resolveDraftKey())

  function handleBeginnerMetadata(patch) {
    Object.assign(form, patch)
    dirty.value = true
  }

  function handleBeginnerFormJson(formJson) {
    if (!formJson) return
    const nextFormJson = clone(formJson)
    nextFormJson.formConfig ||= {}
    nextFormJson.formConfig.beginnerTeachingTopic = form.teachingTopic || ''
    nextFormJson.formConfig.beginnerEstimatedMinutes = Number(form.estimatedMinutes || 0)
    rawFormJson.value = nextFormJson
    form.formJson = JSON.stringify(nextFormJson)
    const homeTab = nextFormJson.widgetList?.find(widget => widget?.type === 'tab' && widget?.options?.name === 'HomeTab')
    form.maxPages = Math.max(1, homeTab?.tabs?.length || 1)
    invalidFormJson.value = ''
    dirty.value = true
  }

  async function enterAdvancedMode() {
    if (invalidFormJson.value) {
      ElMessage.warning('原模板内容损坏，请先创建可编辑副本，再进入高级模式')
      return
    }
    try {
      await ElMessageBox.confirm(
        '高级模式提供完整的布局、事件和扩展设置，适合熟悉表单设计的教师。进入后仍可随时返回新手模式，现有内容不会丢失。',
        '进入高级模式',
        { type: 'info', confirmButtonText: '进入高级模式', cancelButtonText: '继续使用新手模式' }
      )
    } catch {
      return
    }
    editorMode.value = 'advanced'
    nextTick(hydrateAdvancedDesigner)
  }

  function returnToBeginnerMode() {
    try {
      const current = readAdvancedFormJson()
      if (current) {
        rawFormJson.value = JSON.parse(current)
        form.formJson = current
      }
    } catch {
      ElMessage.warning('当前高级内容无法转换，已保留上一次可用草稿')
    }
    editorMode.value = 'beginner'
  }

  async function handleBeginnerSave(destination, revision) {
    const saved = await saveTemplate(true)
    if (!saved) return
    try {
      await completeGuideSheetDraft(draftKey.value, revision || 0)
    } catch {
      // 正式模板已保存时，草稿清理失败不应被误报为保存失败。
    }
    try {
      localStorage.removeItem(`guide-sheet-draft:${draftKey.value}`)
      if (draftKey.value.includes('-new-')) {
        localStorage.removeItem(`guide-sheet:new-draft-key:${ownerKey()}`)
      }
    } catch {
      // 浏览器存储不可用不影响正式保存结果。
    }
    allowNavigation.value = true
    if (destination === 'lesson') {
      router.push({
        path: '/business/lesson-auth/designer',
        query: {
          grade: form.grade,
          semester: form.semester,
          nextNum: form.lessonNum,
          guideSheetId: form.sheetId,
          redirect: '/teacher-dashboard/index'
        }
      })
    } else {
      router.push('/business/guide-sheet-list')
    }
  }

  function handleBeforeUnload(event) {
    if (!dirty.value) return
    event.preventDefault()
    event.returnValue = ''
  }

  onMounted(() => window.addEventListener('beforeunload', handleBeforeUnload))
  onBeforeUnmount(() => window.removeEventListener('beforeunload', handleBeforeUnload))

  onBeforeRouteLeave(async () => {
    if (allowNavigation.value || !dirty.value) return true
    try {
      await ElMessageBox.confirm(
        '当前页面还有未保存的修改，是否保存后离开？',
        '离开导学单设计',
        {
          type: 'warning',
          confirmButtonText: '保存后离开',
          cancelButtonText: '不保存离开',
          distinguishCancelAndClose: true
        }
      )
      const saved = await saveTemplate(false)
      if (!saved) return false
      allowNavigation.value = true
      return true
    } catch (action) {
      if (action === 'cancel') {
        allowNavigation.value = true
        return true
      }
      return false
    }
  })

  return {
    editorMode,
    loadingSheet,
    invalidFormJson,
    draftKey,
    handleBeginnerMetadata,
    handleBeginnerFormJson,
    enterAdvancedMode,
    returnToBeginnerMode,
    handleBeginnerSave
  }
}
