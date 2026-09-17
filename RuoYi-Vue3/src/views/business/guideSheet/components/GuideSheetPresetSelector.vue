<template>
  <section class="preset-selector">
    <div class="selector-heading">
      <div>
        <strong>从空白画布开始</strong>
        <span>下方可直接添加常用教学模块，支持增删与排序</span>
      </div>
      <el-tag type="success" effect="plain">空白 + 常用模块</el-tag>
    </div>
    <div class="blank-banner">
      <div>
        <strong>当前：空白画布</strong>
        <p>不预置课堂结构。请从左侧模块库添加学习目标、检测、题目、作品提交等内容。</p>
      </div>
      <el-button v-if="showReset" plain type="primary" @click="$emit('select', DEFAULT_STRUCTURE_PRESET_ID)">
        重置为空白画布
      </el-button>
    </div>
  </section>
</template>

<script setup>
import { DEFAULT_STRUCTURE_PRESET_ID } from '../utils/presetFactories.js'

defineProps({
  /** 当前预设 id；非 blank 时显示「重置为空白」 */
  modelValue: { type: String, default: DEFAULT_STRUCTURE_PRESET_ID },
  showReset: { type: Boolean, default: false }
})
defineEmits(['select'])
</script>

<style scoped>
.preset-selector {
  padding: 16px 20px;
  border-bottom: 1px solid #dfe7e9;
  background: #fff;
}
.selector-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
}
.selector-heading strong,
.selector-heading span { display: block; }
.selector-heading strong { color: #203840; font-size: 15px; }
.selector-heading span { margin-top: 3px; color: #7d8c92; font-size: 12px; }
.blank-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 14px;
  border: 1px solid #d5ebe6;
  border-radius: 8px;
  background: #f3faf8;
}
.blank-banner strong { color: #1f5f58; font-size: 14px; }
.blank-banner p {
  margin: 4px 0 0;
  color: #5f7573;
  font-size: 12px;
  line-height: 1.5;
}
@media (max-width: 680px) {
  .blank-banner { flex-direction: column; align-items: stretch; }
}
</style>
