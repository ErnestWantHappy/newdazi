<template>
  <div class="image-display-widget">
    <el-image
      v-if="imageUrl"
      :src="imageUrl"
      :alt="alt || '图片'"
      :style="imageStyle"
      fit="contain"
      :preview-src-list="[imageUrl]"
      :initial-index="0"
      preview-teleported
      hide-on-click-modal
    />
    <div v-else class="image-placeholder">
      <el-icon><PictureFilled /></el-icon>
      <span>请设置图片地址</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { PictureFilled } from '@element-plus/icons-vue'

const props = defineProps({
  imageUrl: {
    type: String,
    default: ''
  },
  alt: {
    type: String,
    default: ''
  },
  width: {
    type: [String, Number],
    default: ''
  },
  height: {
    type: [String, Number],
    default: ''
  },
  maxWidth: {
    type: String,
    default: '100%'
  }
})

const imageStyle = computed(() => {
  const style = {}
  if (props.width) style.width = typeof props.width === 'number' ? props.width + 'px' : props.width
  if (props.height) style.height = typeof props.height === 'number' ? props.height + 'px' : props.height
  if (props.maxWidth) style.maxWidth = props.maxWidth
  return style
})
</script>

<style scoped>
.image-display-widget {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 40px;
}
.image-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  font-size: 14px;
  padding: 20px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  width: 100%;
  min-height: 100px;
}
.image-placeholder .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
}
</style>