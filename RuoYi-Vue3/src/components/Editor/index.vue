<template>
  <div>
    <el-upload
      :action="uploadUrl"
      :before-upload="handleBeforeUpload"
      :on-success="handleUploadSuccess"
      :on-error="handleUploadError"
      name="file"
      :show-file-list="false"
      :headers="headers"
      class="editor-img-uploader"
      v-if="type == 'url' && !enableImageBatch"
    >
      <i ref="uploadRef" class="editor-img-uploader"></i>
    </el-upload>
    <input
      v-if="type == 'url' && enableImageBatch"
      ref="batchInputRef"
      class="editor-img-uploader"
      type="file"
      :accept="allowedImageTypes.join(',')"
      multiple
      @change="handleBatchSelection"
    />
  </div>
  <div
    ref="editorContainerRef"
    class="editor"
    :class="{
      'editor--table-enabled': enableTable,
      'editor--image-resize-enabled': enableImageResize
    }"
  >
    <div v-if="enableTable" class="editor-table-tools">
      <el-button size="small" plain data-testid="editor-insert-table" @click="handleTableCommand('insertTable')">
        插入 3×3 表格
      </el-button>
      <el-dropdown trigger="click" @command="handleTableCommand">
        <el-button size="small" plain data-testid="editor-table-menu">
          调整表格<el-icon class="el-icon--right"><ArrowDown /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="insertRowAbove">上方插入行</el-dropdown-item>
            <el-dropdown-item command="insertRowBelow">下方插入行</el-dropdown-item>
            <el-dropdown-item command="insertColumnLeft">左侧插入列</el-dropdown-item>
            <el-dropdown-item command="insertColumnRight">右侧插入列</el-dropdown-item>
            <el-dropdown-item command="deleteRow" divided>删除当前行</el-dropdown-item>
            <el-dropdown-item command="deleteColumn">删除当前列</el-dropdown-item>
            <el-dropdown-item command="deleteTable">删除表格</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    <div v-if="enableImageBatch && batchUpload.total" class="editor-batch-status" role="status">
      {{ batchUploadText }}
    </div>
    <quill-editor
      ref="quillEditorRef"
      v-model:content="content"
      contentType="html"
      @textChange="(e) => $emit('update:modelValue', content)"
      :options="options"
      :style="styles"
    />
    <div
      v-if="enableImageResize && selectedImage"
      class="editor-image-resize-overlay"
      :style="imageOverlayStyle"
    >
      <span class="editor-image-size-label">{{ selectedImageWidth }}px</span>
      <button
        class="editor-image-resize-handle"
        type="button"
        aria-label="拖动调整图片大小"
        title="拖动调整图片大小"
        @pointerdown.stop.prevent="startImageResize"
      />
    </div>
  </div>
</template>

<script setup>
import axios from 'axios'
import { QuillEditor } from "@vueup/vue-quill"
import { ArrowDown } from '@element-plus/icons-vue'
import "@vueup/vue-quill/dist/vue-quill.snow.css"
import {
  handleSessionExpired,
  isSessionExpiredCode,
  isSessionExpiredError,
  refreshAuthorizationHeader
} from '@/utils/session'

const { proxy } = getCurrentInstance()
const emit = defineEmits(['update:modelValue'])

const quillEditorRef = ref()
const quillInstance = shallowRef(null)
const pendingImageUploads = ref(0)
const batchInputRef = ref()
const editorContainerRef = ref()
const selectedImage = shallowRef(null)
const selectedImageWidth = ref(0)
const imageOverlayStyle = ref({})
const batchUpload = reactive({ active: false, total: 0, completed: 0, success: 0, failed: 0 })
let resizeState = null
const uploadUrl = computed(() => {
  if (/^https?:\/\//i.test(props.uploadAction)) {
    return props.uploadAction
  }
  return import.meta.env.VITE_APP_BASE_API + props.uploadAction
})
const headers = ref(refreshAuthorizationHeader())

const props = defineProps({
  /* 编辑器的内容 */
  modelValue: {
    type: String,
  },
  /* 高度 */
  height: {
    type: Number,
    default: null,
  },
  /* 最小高度 */
  minHeight: {
    type: Number,
    default: null,
  },
  /* 只读 */
  readOnly: {
    type: Boolean,
    default: false,
  },
  /* 上传文件大小限制(MB) */
  fileSize: {
    type: Number,
    default: 5,
  },
  /* 类型（base64格式、url格式） */
  type: {
    type: String,
    default: "url",
  },
  /* 是否启用 Quill 2 表格模块；默认关闭，避免影响现有编辑页面 */
  enableTable: {
    type: Boolean,
    default: false,
  },
  /* 图片上传地址 */
  uploadAction: {
    type: String,
    default: "/common/upload",
  },
  /* 允许的图片 MIME；默认值保持历史行为 */
  allowedImageTypes: {
    type: Array,
    default: () => ["image/jpeg", "image/jpg", "image/png", "image/svg"],
  },
  /* 图片总数上限；空值表示不限制 */
  maxImageCount: {
    type: Number,
    default: null,
  },
  /* 是否允许一次选择多张图片；默认关闭，避免改变历史编辑器行为 */
  enableImageBatch: {
    type: Boolean,
    default: false,
  },
  /* 是否允许通过拖拽手柄调整图片宽度 */
  enableImageResize: {
    type: Boolean,
    default: false,
  }
})

const batchUploadText = computed(() => {
  if (batchUpload.active) {
    return `正在上传 ${batchUpload.completed}/${batchUpload.total}，成功 ${batchUpload.success}，失败 ${batchUpload.failed}`
  }
  return `批量上传完成：成功 ${batchUpload.success}，失败 ${batchUpload.failed}`
})

const options = computed(() => ({
  theme: "snow",
  bounds: document.body,
  debug: "warn",
  modules: {
    // 工具栏配置
    toolbar: [
      ["bold", "italic", "underline", "strike"],      // 加粗 斜体 下划线 删除线
      ["blockquote", "code-block"],                   // 引用  代码块
      [{ list: "ordered" }, { list: "bullet" }],      // 有序、无序列表
      [{ indent: "-1" }, { indent: "+1" }],           // 缩进
      [{ size: ["small", false, "large", "huge"] }],  // 字体大小
      [{ header: [1, 2, 3, 4, 5, 6, false] }],        // 标题
      [{ color: [] }, { background: [] }],            // 字体颜色、字体背景颜色
      [{ align: [] }],                                // 对齐方式
      ["clean"],                                      // 清除文本格式
      ["link", "image", "video"]                      // 链接、图片、视频
    ],
    ...(props.enableTable ? { table: true } : {}),
  },
  placeholder: "请输入内容",
  readOnly: props.readOnly
}))

const styles = computed(() => {
  let style = {}
  if (props.minHeight) {
    style.minHeight = `${props.minHeight}px`
  }
  if (props.height) {
    style.height = `${props.height}px`
  }
  return style
})

const content = ref("")
watch(() => props.modelValue, (v) => {
  if (v !== content.value) {
    content.value = v == undefined ? "<p></p>" : v
  }
}, { immediate: true })

function refreshHeaders() {
  headers.value = refreshAuthorizationHeader(headers.value)
}

// 如果设置了上传地址则自定义图片上传事件
onMounted(() => {
  quillInstance.value = quillEditorRef.value.getQuill()
  if (props.type == 'url') {
    let quill = quillInstance.value
    let toolbar = quill.getModule("toolbar")
    toolbar.addHandler("image", (value) => {
      if (value) {
        if (props.enableImageBatch) {
          batchInputRef.value?.click()
        } else {
          proxy.$refs.uploadRef.click()
        }
      } else {
        quill.format("image", false)
      }
    })
    quill.root.addEventListener('paste', handlePasteCapture, true)
    if (props.enableImageResize) {
      quill.root.addEventListener('click', handleEditorImageClick)
      quill.root.addEventListener('scroll', updateImageOverlay)
      window.addEventListener('resize', updateImageOverlay)
    }
  }
})

onBeforeUnmount(() => {
  if (quillInstance.value) {
    quillInstance.value.root.removeEventListener('paste', handlePasteCapture, true)
    quillInstance.value.root.removeEventListener('click', handleEditorImageClick)
    quillInstance.value.root.removeEventListener('scroll', updateImageOverlay)
  }
  window.removeEventListener('resize', updateImageOverlay)
  stopImageResize()
})

function getQuill() {
  return quillInstance.value || quillEditorRef.value?.getQuill()
}

function handleTableCommand(command) {
  const quill = getQuill()
  const table = quill?.getModule('table')
  if (!table || typeof table[command] !== 'function') {
    proxy.$modal.msgError('当前编辑器无法执行表格操作')
    return
  }
  quill.focus()
  if (!quill.getSelection()) {
    quill.setSelection(Math.max(0, quill.getLength() - 1), 0)
  }
  if (command === 'insertTable') {
    table.insertTable(3, 3)
  } else {
    table[command]()
  }
}

function currentImageCount() {
  return getQuill()?.root?.querySelectorAll('img').length || 0
}

function validateImage(file) {
  const error = imageValidationError(file)
  if (error) {
    proxy.$modal.msgError(error)
    return false
  }
  if (props.maxImageCount != null
      && currentImageCount() + pendingImageUploads.value >= props.maxImageCount) {
    proxy.$modal.msgError(`每条内容最多上传 ${props.maxImageCount} 张图片`)
    return false
  }
  return true
}

function imageValidationError(file) {
  if (!props.allowedImageTypes.includes(file.type)) return `${file.name || '图片'}：图片格式错误`
  if (props.fileSize && file.size / 1024 / 1024 > props.fileSize) {
    return `${file.name || '图片'}：大小不能超过 ${props.fileSize} MB`
  }
  return ''
}

function imageUrlFromResponse(res) {
  return props.uploadAction === '/common/upload'
    ? import.meta.env.VITE_APP_BASE_API + "/common/resource/view?resource=" + encodeURIComponent(res.fileName)
    : import.meta.env.VITE_APP_BASE_API + res.url
}

function insertUploadedImage(res, index = null) {
  if (res.code != 200) return false
  const quill = getQuill()
  const savedIndex = quill.getSelection()?.index ?? quill.selection?.savedRange?.index ?? quill.getLength() - 1
  const targetIndex = index == null ? savedIndex : index
  quill.insertEmbed(targetIndex, "image", imageUrlFromResponse(res), "user")
  quill.setSelection(targetIndex + 1, 0, "silent")
  return true
}

// 上传前校检格式和大小
function handleBeforeUpload(file) {
  refreshHeaders()
  const valid = validateImage(file)
  if (valid) pendingImageUploads.value += 1
  return valid
}

// 上传成功处理
function handleUploadSuccess(res, file) {
  pendingImageUploads.value = Math.max(0, pendingImageUploads.value - 1)
  // 如果上传成功
  if (res.code == 200) {
    insertUploadedImage(res)
  } else if (isSessionExpiredCode(res.code)) {
    handleSessionExpired(res.msg)
  } else {
    proxy.$modal.msgError("图片插入失败")
  }
}

// 上传失败处理
function handleUploadError(err) {
  pendingImageUploads.value = Math.max(0, pendingImageUploads.value - 1)
  if (isSessionExpiredError(err)) {
    handleSessionExpired()
  } else {
    proxy.$modal.msgError("图片插入失败")
  }
}

async function uploadImageFile(file) {
  refreshHeaders()
  const formData = new FormData()
  formData.append("file", file)
  const response = await axios.post(uploadUrl.value, formData, {
    headers: {
      "Content-Type": "multipart/form-data",
      ...headers.value
    }
  })
  return response.data
}

async function handleBatchSelection(event) {
  const files = Array.from(event.target.files || [])
  event.target.value = ''
  if (!files.length || batchUpload.active) return

  const available = props.maxImageCount == null
    ? files.length
    : Math.max(0, props.maxImageCount - currentImageCount() - pendingImageUploads.value)
  const accepted = []
  const rejected = []
  for (const file of files) {
    const error = imageValidationError(file)
    if (error) {
      rejected.push(error)
    } else if (accepted.length >= available) {
      rejected.push(`${file.name}：超过每条内容最多 ${props.maxImageCount} 张图片的限制`)
    } else {
      accepted.push(file)
    }
  }

  batchUpload.active = true
  batchUpload.total = files.length
  batchUpload.completed = rejected.length
  batchUpload.success = 0
  batchUpload.failed = rejected.length
  if (!accepted.length) {
    batchUpload.active = false
    proxy.$modal.msgError(rejected[0] || '没有可上传的图片')
    return
  }

  pendingImageUploads.value += accepted.length
  const quill = getQuill()
  let insertIndex = quill.getSelection()?.index ?? quill.selection?.savedRange?.index ?? quill.getLength() - 1
  for (const file of accepted) {
    try {
      const result = await uploadImageFile(file)
      if (isSessionExpiredCode(result.code)) {
        handleSessionExpired(result.msg)
        batchUpload.failed += 1
      } else if (insertUploadedImage(result, insertIndex)) {
        insertIndex += 1
        batchUpload.success += 1
      } else {
        batchUpload.failed += 1
      }
    } catch (error) {
      if (isSessionExpiredError(error)) handleSessionExpired()
      batchUpload.failed += 1
    } finally {
      pendingImageUploads.value = Math.max(0, pendingImageUploads.value - 1)
      batchUpload.completed += 1
    }
  }
  batchUpload.active = false
  if (batchUpload.failed) {
    proxy.$modal.msgWarning(`批量上传完成：成功 ${batchUpload.success} 张，失败 ${batchUpload.failed} 张`)
  } else {
    proxy.$modal.msgSuccess(`已按选择顺序上传 ${batchUpload.success} 张图片`)
  }
}

// 复制粘贴图片处理
function handlePasteCapture(e) {
  const clipboard = e.clipboardData || window.clipboardData
  if (clipboard && clipboard.items) {
    for (let i = 0; i < clipboard.items.length; i++) {
      const item = clipboard.items[i]
      if (item.type.indexOf('image') !== -1) {
        e.preventDefault()
        const file = item.getAsFile()
        insertImage(file)
      }
    }
  }
}

function insertImage(file) {
  refreshHeaders()
  if (!validateImage(file)) return
  pendingImageUploads.value += 1
  uploadImageFile(file).then(res => {
    handleUploadSuccess(res)
  }).catch(err => {
    handleUploadError(err)
  })
}

function handleEditorImageClick(event) {
  if (event.target?.tagName === 'IMG') {
    selectedImage.value = event.target
    updateImageOverlay()
  } else {
    selectedImage.value = null
  }
}

function updateImageOverlay() {
  const image = selectedImage.value
  const container = editorContainerRef.value
  if (!image?.isConnected || !container) {
    selectedImage.value = null
    return
  }
  const imageRect = image.getBoundingClientRect()
  const containerRect = container.getBoundingClientRect()
  selectedImageWidth.value = Math.round(imageRect.width)
  imageOverlayStyle.value = {
    left: `${imageRect.left - containerRect.left}px`,
    top: `${imageRect.top - containerRect.top}px`,
    width: `${imageRect.width}px`,
    height: `${imageRect.height}px`
  }
}

function startImageResize(event) {
  const image = selectedImage.value
  const quill = getQuill()
  if (!image || !quill) return
  resizeState = {
    pointerId: event.pointerId,
    startX: event.clientX,
    startWidth: image.getBoundingClientRect().width,
    maxWidth: quill.root.clientWidth
  }
  window.addEventListener('pointermove', resizeImage)
  window.addEventListener('pointerup', finishImageResize)
  window.addEventListener('pointercancel', finishImageResize)
}

function resizeImage(event) {
  if (!resizeState || event.pointerId !== resizeState.pointerId || !selectedImage.value) return
  const width = Math.round(Math.min(resizeState.maxWidth, Math.max(80,
    resizeState.startWidth + event.clientX - resizeState.startX)))
  selectedImage.value.setAttribute('width', String(width))
  selectedImage.value.removeAttribute('height')
  updateImageOverlay()
}

function finishImageResize(event) {
  if (!resizeState || (event?.pointerId != null && event.pointerId !== resizeState.pointerId)) return
  const quill = getQuill()
  if (selectedImage.value && quill) {
    // Quill 会把 width 属性写回 HTML；显式触发更新确保保存与再次编辑都能拿到最新尺寸。
    quill.update('user')
    content.value = quill.root.innerHTML
    emit('update:modelValue', content.value)
  }
  stopImageResize()
  updateImageOverlay()
}

function stopImageResize() {
  resizeState = null
  window.removeEventListener('pointermove', resizeImage)
  window.removeEventListener('pointerup', finishImageResize)
  window.removeEventListener('pointercancel', finishImageResize)
}

defineExpose({
  getQuill,
  handleTableCommand,
  handleBatchSelection,
})
</script>

<style>
.editor-img-uploader {
  display: none;
}
.editor-table-tools {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 6px 0;
}
.editor-batch-status {
  margin: 0 0 6px;
  color: var(--el-text-color-secondary, #909399);
  font-size: 13px;
  text-align: right;
}
.editor--image-resize-enabled {
  position: relative;
}
.editor--image-resize-enabled .ql-editor img {
  max-width: 100%;
  height: auto;
  cursor: pointer;
}
.editor-image-resize-overlay {
  position: absolute;
  z-index: 10;
  box-sizing: border-box;
  border: 2px solid var(--el-color-primary, #409eff);
  pointer-events: none;
}
.editor-image-size-label {
  position: absolute;
  right: 0;
  bottom: -25px;
  padding: 2px 6px;
  border-radius: 3px;
  color: #fff;
  background: var(--el-color-primary, #409eff);
  font-size: 12px;
  line-height: 18px;
  white-space: nowrap;
}
.editor-image-resize-handle {
  position: absolute;
  right: -7px;
  bottom: -7px;
  width: 14px;
  height: 14px;
  padding: 0;
  border: 2px solid #fff;
  border-radius: 50%;
  background: var(--el-color-primary, #409eff);
  box-shadow: 0 0 0 1px var(--el-color-primary, #409eff);
  cursor: nwse-resize;
  pointer-events: auto;
  touch-action: none;
}
.editor--table-enabled .ql-editor table {
  width: 100%;
  margin: 8px 0;
  border: 1px solid var(--el-border-color, #dcdfe6);
  border-collapse: collapse;
  table-layout: fixed;
}
.editor--table-enabled .ql-editor td,
.editor--table-enabled .ql-editor th {
  min-width: 72px;
  height: 36px;
  padding: 6px 8px;
  border: 1px solid var(--el-border-color, #dcdfe6);
  vertical-align: top;
}
.editor--table-enabled .ql-editor td:focus,
.editor--table-enabled .ql-editor th:focus {
  outline: 2px solid var(--el-color-primary, #409eff);
  outline-offset: -2px;
}
.editor, .ql-toolbar {
  white-space: pre-wrap !important;
  line-height: normal !important;
}
.quill-img {
  display: none;
}
.ql-snow .ql-tooltip[data-mode="link"]::before {
  content: "请输入链接地址:";
}
.ql-snow .ql-tooltip.ql-editing a.ql-action::after {
  border-right: 0px;
  content: "保存";
  padding-right: 0px;
}
.ql-snow .ql-tooltip[data-mode="video"]::before {
  content: "请输入视频地址:";
}
.ql-snow .ql-picker.ql-size .ql-picker-label::before,
.ql-snow .ql-picker.ql-size .ql-picker-item::before {
  content: "14px";
}
.ql-snow .ql-picker.ql-size .ql-picker-label[data-value="small"]::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value="small"]::before {
  content: "10px";
}
.ql-snow .ql-picker.ql-size .ql-picker-label[data-value="large"]::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value="large"]::before {
  content: "18px";
}
.ql-snow .ql-picker.ql-size .ql-picker-label[data-value="huge"]::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value="huge"]::before {
  content: "32px";
}
.ql-snow .ql-picker.ql-header .ql-picker-label::before,
.ql-snow .ql-picker.ql-header .ql-picker-item::before {
  content: "文本";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="1"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="1"]::before {
  content: "标题1";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="2"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="2"]::before {
  content: "标题2";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="3"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="3"]::before {
  content: "标题3";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="4"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="4"]::before {
  content: "标题4";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="5"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="5"]::before {
  content: "标题5";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="6"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="6"]::before {
  content: "标题6";
}
.ql-snow .ql-picker.ql-font .ql-picker-label::before,
.ql-snow .ql-picker.ql-font .ql-picker-item::before {
  content: "标准字体";
}
.ql-snow .ql-picker.ql-font .ql-picker-label[data-value="serif"]::before,
.ql-snow .ql-picker.ql-font .ql-picker-item[data-value="serif"]::before {
  content: "衬线字体";
}
.ql-snow .ql-picker.ql-font .ql-picker-label[data-value="monospace"]::before,
.ql-snow .ql-picker.ql-font .ql-picker-item[data-value="monospace"]::before {
  content: "等宽字体";
}
</style>
