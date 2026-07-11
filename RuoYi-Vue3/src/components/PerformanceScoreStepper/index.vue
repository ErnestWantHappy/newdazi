<template>
  <div
    class="performance-score-stepper"
    :class="{
      'is-disabled': disabled,
      'is-positive': numericValue > 0,
      'is-negative': numericValue < 0
    }"
  >
    <button
      class="step-btn minus-btn"
      type="button"
      aria-label="扣分"
      :disabled="disabled || numericValue <= min"
      @click="decrease"
    >
      <el-icon><Minus /></el-icon>
    </button>
    <input
      class="score-input"
      type="number"
      inputmode="numeric"
      :min="min"
      :max="max"
      :step="step"
      :value="numericValue"
      :disabled="disabled"
      @input="handleInput"
      @blur="handleBlur"
      @keydown.up.prevent="increase"
      @keydown.down.prevent="decrease"
    />
    <button
      class="step-btn plus-btn"
      type="button"
      aria-label="加分"
      :disabled="disabled || numericValue >= max"
      @click="increase"
    >
      <el-icon><Plus /></el-icon>
    </button>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Minus, Plus } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: [Number, String],
    default: 0
  },
  min: {
    type: Number,
    default: -10
  },
  max: {
    type: Number,
    default: 10
  },
  step: {
    type: Number,
    default: 1
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'change', 'blur'])

const numericValue = computed(() => normalizeValue(props.modelValue))

function normalizeValue(value) {
  const parsed = Number(value)
  const safeValue = Number.isFinite(parsed) ? parsed : 0
  return clamp(Math.round(safeValue))
}

function clamp(value) {
  return Math.min(props.max, Math.max(props.min, value))
}

function commitValue(value, emitChange = true) {
  if (props.disabled) return

  const nextValue = normalizeValue(value)
  const currentValue = normalizeValue(props.modelValue)

  if (props.modelValue !== nextValue) {
    emit('update:modelValue', nextValue)
  }
  if (emitChange && nextValue !== currentValue) {
    emit('change', nextValue)
  }
}

function decrease() {
  commitValue(numericValue.value - props.step)
}

function increase() {
  commitValue(numericValue.value + props.step)
}

function handleInput(event) {
  const rawValue = event.target.value
  if (rawValue === '' || rawValue === '-') return
  commitValue(rawValue)
}

function handleBlur(event) {
  commitValue(event.target.value)
  emit('blur', event)
}
</script>

<style lang="scss" scoped>
.performance-score-stepper {
  display: inline-flex;
  align-items: stretch;
  width: 120px;
  height: 26px;
  min-width: 120px;
  min-height: 26px;
  max-height: 26px;
  box-sizing: border-box;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  background: #fff;
  line-height: 1;
  vertical-align: top;
  flex: 0 0 120px;
  contain: layout paint;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;

  &:focus-within {
    border-color: #409eff;
    box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.12);
  }

  &.is-positive {
    border-color: #b3e19d;
  }

  &.is-negative {
    border-color: #fab6b6;
  }

  &.is-disabled {
    background: #f5f7fa;
    border-color: #e4e7ed;
  }
}

.step-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 32px;
  width: 32px;
  height: 24px;
  min-width: 32px;
  min-height: 24px;
  max-height: 24px;
  box-sizing: border-box;
  padding: 0;
  border: 0;
  outline: none;
  color: #606266;
  background: #f5f7fa;
  cursor: pointer;
  font: inherit;
  line-height: 1;
  transition: color 0.2s ease, background-color 0.2s ease;
  appearance: none;
  -webkit-appearance: none;

  .el-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    line-height: 1;
  }

  &:disabled {
    color: #c0c4cc;
    cursor: not-allowed;
    background: #f5f7fa;
  }

  &:active,
  &:focus,
  &:focus-visible {
    outline: none;
    transform: none;
  }
}

.minus-btn {
  border-right: 1px solid #dcdfe6;

  &:not(:disabled):hover {
    color: #f56c6c;
    background: #fef0f0;
  }
}

.plus-btn {
  border-left: 1px solid #dcdfe6;

  &:not(:disabled):hover {
    color: #67c23a;
    background: #f0f9eb;
  }
}

.score-input {
  flex: 0 0 56px;
  width: 56px;
  height: 24px;
  min-width: 56px;
  min-height: 24px;
  max-height: 24px;
  box-sizing: border-box;
  padding: 0 4px;
  border: 0;
  color: #303133;
  background: transparent;
  font-size: 13px;
  font-weight: 600;
  line-height: 24px;
  text-align: center;
  outline: none;
  -moz-appearance: textfield;
  appearance: textfield;

  &:disabled {
    color: #a8abb2;
    cursor: not-allowed;
  }

  &::-webkit-outer-spin-button,
  &::-webkit-inner-spin-button {
    margin: 0;
    appearance: none;
  }
}
</style>
