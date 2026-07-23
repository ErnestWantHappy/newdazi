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
      v-if="type == 'url'"
    >
      <i ref="uploadRef" class="editor-img-uploader"></i>
    </el-upload>
  </div>
  <div class="editor" :class="{ 'editor--table-enabled': enableTable }">
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
    <quill-editor
      ref="quillEditorRef"
      v-model:content="content"
      contentType="html"
      @textChange="(e) => $emit('update:modelValue', content)"
      :options="options"
      :style="styles"
    />
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

const quillEditorRef = ref()
const quillInstance = shallowRef(null)
const pendingImageUploads = ref(0)
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
  }
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
        proxy.$refs.uploadRef.click()
      } else {
        quill.format("image", false)
      }
    })
    quill.root.addEventListener('paste', handlePasteCapture, true)
  }
})

onBeforeUnmount(() => {
  if (quillInstance.value) {
    quillInstance.value.root.removeEventListener('paste', handlePasteCapture, true)
  }
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
  if (!props.allowedImageTypes.includes(file.type)) {
    proxy.$modal.msgError('图片格式错误!')
    return false
  }
  if (props.fileSize && file.size / 1024 / 1024 > props.fileSize) {
    proxy.$modal.msgError(`上传文件大小不能超过 ${props.fileSize} MB!`)
    return false
  }
  if (props.maxImageCount != null
      && currentImageCount() + pendingImageUploads.value >= props.maxImageCount) {
    proxy.$modal.msgError(`每条内容最多上传 ${props.maxImageCount} 张图片`)
    return false
  }
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
    // 获取富文本实例
    let quill = toRaw(quillEditorRef.value).getQuill()
    // 获取光标位置
    let length = quill.selection.savedRange.index
    // 插入图片，res.url为服务器返回的图片链接地址
    // 专用业务接口返回已受控的公开相对路径；公共上传仍沿用历史资源查看地址。
    const imageUrl = props.uploadAction === '/common/upload'
      ? import.meta.env.VITE_APP_BASE_API + "/common/resource/view?resource=" + encodeURIComponent(res.fileName)
      : import.meta.env.VITE_APP_BASE_API + res.url
    quill.insertEmbed(length, "image", imageUrl)
    // 调整光标到最后
    quill.setSelection(length + 1)
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
  const formData = new FormData()
  formData.append("file", file)
  axios.post(uploadUrl.value, formData, {
    headers: {
      "Content-Type": "multipart/form-data",
      ...headers.value
    }
  }).then(res => {
    handleUploadSuccess(res.data)
  }).catch(err => {
    handleUploadError(err)
  })
}

defineExpose({
  getQuill,
  handleTableCommand,
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
