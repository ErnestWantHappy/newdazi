<template>
  <span class="platform-logo">
    <button
      type="button"
      class="platform-logo__trigger"
      :aria-label="`放大查看${alt}`"
      :title="`点击查看${alt}大图`"
      @click.stop="previewVisible = true"
    >
      <img :src="src" :alt="alt" class="platform-logo__image" />
    </button>
    <el-dialog
      v-model="previewVisible"
      :title="alt"
      width="min(680px, 92vw)"
      align-center
      append-to-body
      class="platform-logo-preview-dialog"
    >
      <div class="platform-logo__preview">
        <img :src="src" :alt="alt" class="platform-logo__preview-image" />
      </div>
    </el-dialog>
  </span>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  src: {
    type: String,
    required: true
  },
  alt: {
    type: String,
    default: '信息科技学业测评平台 Logo'
  }
})

const previewVisible = ref(false)
</script>

<style lang="scss" scoped>
.platform-logo {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  vertical-align: middle;

  &__trigger {
    display: flex;
    width: 100%;
    height: 100%;
    align-items: center;
    justify-content: center;
    padding: 0;
    border: 0;
    border-radius: 8px;
    background: transparent;
    cursor: zoom-in;

    &:focus-visible {
      outline: 2px solid #409eff;
      outline-offset: 3px;
    }
  }

  &__image {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: contain;
  }

  &__preview {
    display: flex;
    min-height: 220px;
    align-items: center;
    justify-content: center;
    padding: 12px 8px 4px;
    background: transparent;
  }

  &__preview-image {
    display: block;
    width: min(100%, 560px);
    max-height: 70vh;
    object-fit: contain;
  }
}

:global(.el-dialog.platform-logo-preview-dialog) {
  --el-dialog-bg-color: transparent;
  background: transparent;
  box-shadow: none;
}

:global(.platform-logo-preview-dialog .el-dialog__title) {
  color: #fff;
  text-shadow: 0 1px 8px rgb(0 0 0 / 45%);
}

:global(.platform-logo-preview-dialog .el-dialog__headerbtn .el-dialog__close) {
  color: #fff;
  text-shadow: 0 1px 8px rgb(0 0 0 / 45%);
}

:global(.platform-logo-preview-dialog .el-dialog__headerbtn:hover .el-dialog__close) {
  color: #fff;
}
</style>
