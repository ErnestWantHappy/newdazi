<template>
  <Editor
    :model-value="modelValue"
    :height="height"
    :min-height="minHeight"
    :read-only="readOnly"
    :enable-table="true"
    :enable-image-batch="true"
    :enable-image-resize="true"
    upload-action="/business/research-activity/images"
    :allowed-image-types="['image/jpeg', 'image/jpg', 'image/png', 'image/webp']"
    :file-size="10"
    :max-image-count="20"
    @update:model-value="$emit('update:modelValue', $event)"
  />
</template>

<script setup>
defineProps({
  modelValue: { type: String, default: '' },
  height: { type: Number, default: null },
  minHeight: { type: Number, default: 220 },
  readOnly: { type: Boolean, default: false }
})
defineEmits(['update:modelValue'])
</script>
