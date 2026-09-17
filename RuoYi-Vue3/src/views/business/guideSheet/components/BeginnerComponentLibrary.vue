<template>
  <aside class="module-library" aria-label="常用教学模块">
    <div class="panel-heading">
      <strong>添加教学模块</strong>
      <span>共 9 种常用内容</span>
    </div>
    <div class="module-grid">
      <button
        v-for="item in BEGINNER_MODULES"
        :key="item.type"
        type="button"
        class="module-button"
        @click="$emit('add', item.type)"
      >
        <el-icon :size="18"><component :is="iconMap[item.type]" /></el-icon>
        <span>
          <strong>{{ item.label }}</strong>
          <small>{{ item.description }}</small>
        </span>
        <el-icon class="add-icon"><Plus /></el-icon>
      </button>
    </div>
  </aside>
</template>

<script setup>
import {
  Aim,
  ChatLineSquare,
  CircleCheck,
  DocumentChecked,
  EditPen,
  Plus,
  Reading,
  Select,
  Star,
  UploadFilled
} from '@element-plus/icons-vue'
import { BEGINNER_MODULES } from '../utils/presetFactories.js'

defineEmits(['add'])

const iconMap = {
  learningObjective: Aim,
  preClassCheck: DocumentChecked,
  knowledgeExplanation: Reading,
  singleChoice: CircleCheck,
  multipleChoice: Select,
  shortAnswer: EditPen,
  fileSubmission: UploadFilled,
  selfAssessment: Star,
  reflection: ChatLineSquare
}
</script>

<style scoped>
.module-library {
  min-width: 0;
  padding: 16px;
  border-right: 1px solid #dde5e8;
  background: #f7f9f9;
}
.panel-heading { display: flex; align-items: baseline; justify-content: space-between; gap: 8px; margin-bottom: 12px; }
.panel-heading strong { color: #20343d; font-size: 15px; }
.panel-heading span { color: #84939a; font-size: 12px; }
.module-grid { display: grid; gap: 7px; }
.module-button {
  display: grid;
  grid-template-columns: 24px minmax(0, 1fr) 18px;
  align-items: center;
  gap: 8px;
  width: 100%;
  min-height: 54px;
  padding: 8px 9px;
  color: #31515c;
  text-align: left;
  border: 1px solid #dbe5e7;
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
  transition: border-color 150ms ease, box-shadow 150ms ease, transform 150ms ease;
}
.module-button:hover { border-color: #2c837c; box-shadow: 0 4px 12px rgba(32, 88, 84, 0.09); transform: translateY(-1px); }
.module-button strong, .module-button small { display: block; }
.module-button strong { color: #263f49; font-size: 13px; font-weight: 650; }
.module-button small { overflow: hidden; color: #7a8d94; font-size: 11px; text-overflow: ellipsis; white-space: nowrap; }
.add-icon { color: #2c837c; }
@media (max-width: 1100px) {
  .module-library { border-right: 0; border-bottom: 1px solid #dde5e8; }
  .module-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}
@media (max-width: 680px) {
  .module-grid { grid-template-columns: 1fr; }
}
</style>
