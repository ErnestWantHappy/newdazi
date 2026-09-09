<template>
  <div
    class="image-add-widget-wrapper field-wrapper design-time-bottom-margin"
    :class="{ 'image-selected': isDesignMode && isSelected }"
    :data-id="field.id"
    @mousedown.stop="selectField"
    @click.stop="selectField"
  >
    <!-- 设计模式：选中时显示操作按钮 -->
    <div v-if="isDesignMode && isSelected" class="field-action">
      <i title="选中父组件" @click.stop="selectParentWidget">
        <el-icon><Back /></el-icon>
      </i>
      <i v-if="parentList && parentList.length > 1" title="上移组件" @click.stop="moveUpWidget">
        <el-icon><Top /></el-icon>
      </i>
      <i v-if="parentList && parentList.length > 1" title="下移组件" @click.stop="moveDownWidget">
        <el-icon><Bottom /></el-icon>
      </i>
      <i title="移除组件" @click.stop="removeFieldWidget">
        <el-icon><Delete /></el-icon>
      </i>
    </div>
    <!-- 设计模式：选中时显示拖拽手柄 -->
    <div v-if="isDesignMode && isSelected" class="drag-handler background-opacity">
      <i title="拖拽手柄">
        <el-icon><Rank /></el-icon>
      </i>
      <i>图片展示</i>
    </div>

    <!-- 图片区域 -->
    <div class="image-add-container" ref="resizeContainer">
      <!-- 设计模式：无图片时显示上传占位 -->
      <div v-if="!imageUrl && isDesignMode" class="image-add-placeholder" @click="onPlaceholderClick">
        <el-icon :size="28"><Plus /></el-icon>
        <span>点击添加图片</span>
      </div>
      <!-- 运行模式：无图片时显示占位提示 -->
      <div v-else-if="!imageUrl && !isDesignMode" class="image-add-empty">
        <span>暂无图片</span>
      </div>
      <!-- 有图片时：预览 + 拖拽手柄 -->
      <div v-else class="image-add-preview" :style="previewContainerStyle">
        <el-image
          :src="imageUrl"
          :style="previewStyle"
          fit="contain"
          preview-teleported
          :preview-src-list="[imageUrl]"
        />
        <!-- 设计模式：显示操作按钮 -->
        <div v-if="isDesignMode" class="image-add-overlay">
          <el-button size="small" circle :icon="Refresh" @click.stop="triggerUpload" title="更换图片" />
          <el-button size="small" circle :icon="Delete" type="danger" @click.stop="removeImage" title="删除图片" />
        </div>
        <!-- 设计模式：拖拽调整大小手柄 -->
        <template v-if="isDesignMode && isSelected">
          <!-- 右下角手柄 -->
          <div class="resize-handle resize-handle-se" @mousedown.stop="startResize($event, 'se')">
            <svg viewBox="0 0 12 12" width="12" height="12"><path d="M0 12 L12 0 L12 12 Z" fill="#409eff"/></svg>
          </div>
          <!-- 右边手柄 -->
          <div class="resize-handle resize-handle-e" @mousedown.stop="startResize($event, 'e')">
            <div class="resize-handle-line"></div>
          </div>
          <!-- 下边手柄 -->
          <div class="resize-handle resize-handle-s" @mousedown.stop="startResize($event, 's')">
            <div class="resize-handle-line"></div>
          </div>
          <!-- 尺寸提示 -->
          <div class="resize-size-tip">{{ imageWidth }} x {{ imageHeight }}</div>
        </template>
      </div>
      <input
        ref="fileInput"
        type="file"
        accept="image/*"
        style="display: none"
        @change="handleFileChange"
      />
    </div>
    <!-- 组件标签：位于图片正下方 -->
    <div class="image-add-label">{{ displayLabel }}</div>
  </div>
</template>

<script setup>
import { ref, computed, onBeforeUnmount } from 'vue'
import { Plus, Refresh, Delete, Back, Top, Bottom, Rank } from '@element-plus/icons-vue'

const props = defineProps({
  field: { type: Object, default: () => ({}) },
  designer: { type: Object, default: null },
  parentList: { type: Array, default: null },
  indexOfParentList: { type: Number, default: -1 },
  parentWidget: { type: Object, default: null },
  designState: { type: Boolean, default: false },
  subFormRowIndex: { type: Number, default: -1 },
  subFormColIndex: { type: Number, default: -1 },
  subFormRowId: { type: String, default: '' }
})

const fileInput = ref(null)
const resizeContainer = ref(null)

// 判断是否为设计模式（有 designer 或 designState 为 true）
const isDesignMode = computed(() => {
  return !!(props.designState || props.designer)
})

// 是否被选中
const isSelected = computed(() => {
  return !!(props.designer && props.field && props.field.id === props.designer.selectedId)
})

// 显示标签：如果 options.label 为空则默认显示"图片展示"
const displayLabel = computed(() => {
  const opts = props.field?.options
  return (opts && opts.label) ? opts.label : '图片展示'
})

// 选中当前组件
function selectField(event) {
  if (props.designer) {
    props.designer.setSelected(props.field)
    props.designer.emitEvent('field-selected', props.parentWidget)
  }
}

// 选中父组件
function selectParentWidget() {
  if (props.designer) {
    if (props.parentWidget) {
      props.designer.setSelected(props.parentWidget)
    } else {
      props.designer.clearSelected()
    }
  }
}

// 上移组件
function moveUpWidget() {
  if (props.designer && props.parentList && props.indexOfParentList > 0) {
    props.designer.moveUpWidget(props.parentList, props.indexOfParentList)
    props.designer.emitHistoryChange()
  }
}

// 下移组件
function moveDownWidget() {
  if (props.designer && props.parentList && props.indexOfParentList < props.parentList.length - 1) {
    props.designer.moveDownWidget(props.parentList, props.indexOfParentList)
    props.designer.emitHistoryChange()
  }
}

// 移除组件
function removeFieldWidget() {
  if (props.designer && props.parentList && props.indexOfParentList >= 0) {
    const nextIndex = props.indexOfParentList >= props.parentList.length - 1
      ? props.indexOfParentList - 1
      : props.indexOfParentList + 1
    props.parentList.splice(props.indexOfParentList, 1)
    if (props.parentList.length > 0 && nextIndex >= 0 && nextIndex < props.parentList.length) {
      props.designer.setSelected(props.parentList[nextIndex])
    } else {
      props.designer.clearSelected()
    }
    props.designer.emitHistoryChange()
  }
}

// 确保 field.options 存在
function ensureOptions() {
  if (!props.field.options) {
    props.field.options = {}
  }
  return props.field.options
}

const imageUrl = computed({
  get: () => ensureOptions().imageUrl || '',
  set: (val) => { ensureOptions().imageUrl = val }
})

const imageWidth = computed({
  get: () => ensureOptions().imageWidth || 200,
  set: (val) => { ensureOptions().imageWidth = Math.max(30, Math.min(2000, val)) }
})

const imageHeight = computed({
  get: () => ensureOptions().imageHeight || 200,
  set: (val) => { ensureOptions().imageHeight = Math.max(30, Math.min(2000, val)) }
})

const previewStyle = computed(() => ({
  width: imageWidth.value + 'px',
  height: imageHeight.value + 'px'
}))

const previewContainerStyle = computed(() => ({
  position: 'relative',
  display: 'inline-block',
  width: imageWidth.value + 'px',
  height: imageHeight.value + 'px'
}))

// ---- 拖拽调整大小 ----
const resizing = ref(false)
const resizeDir = ref('')
const resizeStartX = ref(0)
const resizeStartY = ref(0)
const resizeStartW = ref(0)
const resizeStartH = ref(0)

function startResize(e, dir) {
  resizing.value = true
  resizeDir.value = dir
  resizeStartX.value = e.clientX
  resizeStartY.value = e.clientY
  resizeStartW.value = imageWidth.value
  resizeStartH.value = imageHeight.value

  document.addEventListener('mousemove', onResizeMove)
  document.addEventListener('mouseup', onResizeEnd)
  e.preventDefault()
}

function onResizeMove(e) {
  if (!resizing.value) return
  const dx = e.clientX - resizeStartX.value
  const dy = e.clientY - resizeStartY.value
  const dir = resizeDir.value

  let newW = resizeStartW.value
  let newH = resizeStartH.value

  if (dir.includes('e')) newW = resizeStartW.value + dx
  if (dir.includes('s')) newH = resizeStartH.value + dy

  // 保持宽高比：右下角拖拽时按比例
  if (dir === 'se' && !e.shiftKey) {
    const ratio = resizeStartW.value / resizeStartH.value
    if (Math.abs(dx) >= Math.abs(dy)) {
      newH = newW / ratio
    } else {
      newW = newH * ratio
    }
  }

  newW = Math.round(newW)
  newH = Math.round(newH)

  if (newW >= 30 && newW <= 2000) imageWidth.value = newW
  if (newH >= 30 && newH <= 2000) imageHeight.value = newH
}

function onResizeEnd() {
  resizing.value = false
  resizeDir.value = ''
  document.removeEventListener('mousemove', onResizeMove)
  document.removeEventListener('mouseup', onResizeEnd)
}

onBeforeUnmount(() => {
  document.removeEventListener('mousemove', onResizeMove)
  document.removeEventListener('mouseup', onResizeEnd)
})

// ---- 图片操作 ----
function onPlaceholderClick() {
  selectField()
  triggerUpload()
}

function triggerUpload() {
  fileInput.value?.click()
}

function handleFileChange(e) {
  const file = e.target.files[0]
  if (!file) return

  const reader = new FileReader()
  reader.onload = (event) => {
    imageUrl.value = event.target.result
  }
  reader.readAsDataURL(file)
  e.target.value = ''
}

function removeImage() {
  imageUrl.value = ''
}
</script>

<style scoped>
/* 原生 VForm3 field-wrapper 样式：position: relative + margin-bottom: 5px */
.image-add-widget-wrapper.field-wrapper {
  position: relative;
  padding: 0;
  margin-bottom: 5px;
}

/* 选中蓝色框 —— 与原生 VForm3 选中效果一致 */
.image-add-widget-wrapper.image-selected {
  outline: 2px solid #409eff !important;
  outline-offset: 0;
}

/* 操作按钮栏 —— 匹配原生 VForm3 风格：底部蓝色背景 */
.field-action {
  position: absolute;
  bottom: 0;
  right: -2px;
  height: 22px;
  line-height: 22px;
  background: #409EFF;
  z-index: 9;
  display: flex;
  border-radius: 0;
  border: none;
  padding: 0;
}

.field-action i {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #fff;
  margin: 0 5px;
  cursor: pointer;
  width: auto;
  height: auto;
  border-radius: 0;
}

.field-action i:hover {
  color: #fff;
  background: transparent;
  opacity: 0.8;
}

/* 拖拽手柄 —— 选中时始终显示蓝色背景 */
.drag-handler {
  position: absolute;
  top: 0;
  left: -1px;
  height: 20px;
  line-height: 20px;
  z-index: 9;
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: move;
  background: #409EFF;
  border: none;
  border-radius: 0;
  padding: 0;
}

.drag-handler i {
  display: inline-flex;
  align-items: center;
  font-size: 12px;
  font-style: normal;
  color: #fff;
  margin: 0 4px;
}

.image-add-label {
  font-size: 12px;
  color: #909399;
  text-align: left;
  margin-top: 4px;
  line-height: 1.4;
}

.image-add-container {
  position: relative;
}

.image-add-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 200px;
  height: 150px;
  border: 2px dashed #dcdfe6;
  border-radius: 6px;
  cursor: pointer;
  color: #c0c4cc;
  background: #fafafa;
  transition: all 0.3s;
}

.image-add-placeholder:hover {
  border-color: #409eff;
  color: #409eff;
  background: #ecf5ff;
}

.image-add-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 200px;
  height: 100px;
  color: #c0c4cc;
  font-size: 13px;
  border: 1px dashed #e4e7ed;
  border-radius: 4px;
  background: #fafafa;
}

.image-add-preview {
  position: relative;
  display: inline-block;
}

.image-add-preview .el-image {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  display: block;
}

.image-add-overlay {
  position: absolute;
  top: 6px;
  right: 6px;
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-add-preview:hover .image-add-overlay {
  opacity: 1;
}

/* ---- 拖拽调整大小手柄 ---- */
.resize-handle {
  position: absolute;
  z-index: 8;
}

/* 右下角手柄 */
.resize-handle-se {
  bottom: -4px;
  right: -4px;
  width: 16px;
  height: 16px;
  cursor: nwse-resize;
  display: flex;
  align-items: flex-end;
  justify-content: flex-end;
}

/* 右边手柄 */
.resize-handle-e {
  top: 0;
  right: -6px;
  bottom: 0;
  width: 12px;
  cursor: ew-resize;
  display: flex;
  align-items: center;
  justify-content: center;
}

.resize-handle-e .resize-handle-line {
  width: 3px;
  height: 24px;
  background: #409eff;
  border-radius: 2px;
  opacity: 0;
  transition: opacity 0.2s;
}

.resize-handle-e:hover .resize-handle-line {
  opacity: 0.6;
}

/* 下边手柄 */
.resize-handle-s {
  left: 0;
  bottom: -6px;
  right: 0;
  height: 12px;
  cursor: ns-resize;
  display: flex;
  align-items: center;
  justify-content: center;
}

.resize-handle-s .resize-handle-line {
  width: 24px;
  height: 3px;
  background: #409eff;
  border-radius: 2px;
  opacity: 0;
  transition: opacity 0.2s;
}

.resize-handle-s:hover .resize-handle-line {
  opacity: 0.6;
}

/* 尺寸提示 */
.resize-size-tip {
  position: absolute;
  bottom: -22px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 11px;
  color: #909399;
  white-space: nowrap;
  background: #fff;
  padding: 1px 6px;
  border-radius: 3px;
  pointer-events: none;
}
</style>