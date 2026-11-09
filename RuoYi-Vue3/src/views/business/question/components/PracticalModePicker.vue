<!-- 操作题四种作答方式入口：选中切换下方题目，新增按钮只触发新增，不误触切换。 -->
<template>
  <div class="mode-wrap">
  <div class="mode-grid">
    <div
      v-for="item in modes"
      :key="item.key"
      class="mode"
      :class="{ 'is-active': item.key === active }"
      role="button"
      tabindex="0"
      :aria-pressed="item.key === active"
      @click="emit('update:modelValue', item.key)"
      @keydown.enter.prevent="emit('update:modelValue', item.key)"
      @keydown.space.prevent="emit('update:modelValue', item.key)"
    >
      <div class="mode__main">
        <el-icon class="mode__icon"><component :is="icons[item.key]" /></el-icon>
        <div class="mode__text">
          <div class="mode__label">{{ item.label }}</div>
          <div class="mode__hint">{{ item.hint }}</div>
          <div class="mode__count">
            <template v-if="counts && counts[item.key] != null">{{ counts[item.key] }} 题</template>
            <template v-else>
              <span>题量未知</span>
              <el-button
                v-if="failedCounts.includes(item.key)"
                link
                type="primary"
                size="small"
                class="mode__retry"
                :data-testid="`practical-count-retry-${item.key}`"
                @click.stop="emit('retry-count', item.key)"
              >重新获取</el-button>
            </template>
          </div>
        </div>
      </div>
      <el-button
        v-if="canAdd"
        size="small"
        :type="item.key === active ? 'primary' : 'default'"
        plain
        class="mode__add"
        @click.stop="emit('add', item.key)"
      ><el-icon><Plus /></el-icon>新增</el-button>
    </div>
  </div>
  <div class="mode-foot">
    <span>当前方式下方列出该方式的题目。</span>
    <el-button link type="primary" @click="emit('view-all')">
      查看全部<span v-if="totalText"> {{ totalText }}</span><el-icon><ArrowRight /></el-icon>
    </el-button>
  </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ArrowRight, DataLine, Document, Monitor, Share } from '@element-plus/icons-vue'
import { PRACTICAL_MODES } from '@/utils/questionWorkbench'

const props = defineProps({
  modelValue: { type: String, default: 'FILE' },
  counts: { type: Object, default: null },
  failedCounts: { type: Array, default: () => [] },
  canAdd: { type: Boolean, default: false },
  // 当前方式题量文案；失败或未加载时为空，不显示假数字。
  totalText: { type: String, default: '' },
})
const emit = defineEmits(['update:modelValue', 'add', 'view-all', 'retry-count'])
const modes = PRACTICAL_MODES
// 选中项只由父组件的 mode 决定：新增按钮 stop 冒泡，不会把方式改回来。
const active = computed(() => props.modelValue)
// 直接绑定已注册的 Element Plus 图标组件，避免动态字符串解析不到。
const icons = { FILE: Document, PYTHON: Monitor, DATA_LAB: DataLine, FLOWCHART: Share }
</script>

<style scoped>
.mode-wrap { margin-top: 12px; }
.mode-grid { display: grid; gap: 10px; grid-template-columns: repeat(4, minmax(0, 1fr)); }
.mode {
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: space-between;
  padding: 10px 12px;
  border: 1px solid #e6e9ef;
  border-radius: 10px;
  background: #fff;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s;
}
.mode:hover { border-color: #a9c2f0; }
.mode.is-active { border-color: #3b82f6; background: #f5f8ff; }
.mode:focus-visible { outline: 2px solid #3b82f6; outline-offset: 2px; }
.mode__main { display: flex; gap: 8px; align-items: flex-start; min-width: 0; }
.mode__icon { margin-top: 2px; color: #3b82f6; flex-shrink: 0; }
.mode__text { min-width: 0; }
.mode__label { font-size: 13px; font-weight: 600; color: #1f2329; }
.mode__hint { margin-top: 2px; color: #8a9099; font-size: 12px; line-height: 1.4; }
.mode__count { margin-top: 2px; color: #5b6472; font-size: 12px; display: flex; flex-wrap: wrap; gap: 4px; align-items: center; }
.mode__retry { margin-left: 0; padding: 0; height: auto; font-size: 12px; }
.mode__add { align-self: flex-end; }
.mode-foot { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; justify-content: space-between; margin-top: 10px; color: #8a9099; font-size: 12px; }
/* 窄屏两列，按钮保持在方式名称旁，不压到文字上 */
@media (max-width: 768px) {
  .mode-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
</style>
