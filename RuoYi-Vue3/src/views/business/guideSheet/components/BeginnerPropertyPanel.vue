<template>
  <aside class="property-panel">
    <div class="panel-heading">
      <div>
        <strong>内容设置</strong>
        <span v-if="draft.moduleType">{{ definition?.label || '教学模块' }}</span>
      </div>
      <el-button v-if="item" text type="danger" :icon="Delete" @click="$emit('delete')" />
    </div>

    <el-empty v-if="!item" description="选择中间的教学模块后即可编辑" :image-size="58" />
    <div v-else-if="item.advanced" class="advanced-note">
      <el-icon :size="24"><Tools /></el-icon>
      <strong>高级组件</strong>
      <p>该内容由高级模式创建，已为你完整保留。需要修改时请切换到高级模式。</p>
    </div>
    <el-form v-else label-position="top" class="teacher-form">
      <el-form-item :label="isStatic ? '模块标题' : '题目'">
        <el-input v-model="draft.title" maxlength="200" show-word-limit />
      </el-form-item>

      <el-form-item v-if="isStatic" label="教学内容">
        <el-input v-model="draft.content" type="textarea" :rows="6" maxlength="2000" show-word-limit />
      </el-form-item>

      <template v-if="isChoice">
        <el-form-item label="选项">
          <div class="option-list">
            <div v-for="(option, index) in draft.options" :key="`${option.value}-${index}`" class="option-row">
              <span>{{ String.fromCharCode(65 + index) }}</span>
              <el-input v-model="option.label" maxlength="100" />
              <el-button text :icon="Close" :disabled="draft.options.length <= 2" @click="removeOption(index)" />
            </div>
            <el-button plain :icon="Plus" class="add-option" @click="addOption">添加选项</el-button>
          </div>
        </el-form-item>
      </template>

      <el-form-item v-if="isAnswerField" label="是否必答">
        <el-switch v-model="draft.required" />
      </el-form-item>

      <el-form-item v-if="draft.moduleType === 'singleChoice'" label="正确答案">
        <el-radio-group v-model="draft.correctAnswer">
          <el-radio-button v-for="option in draft.options" :key="option.value" :value="option.value">
            {{ option.label }}
          </el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="draft.moduleType === 'multipleChoice'" label="正确答案">
        <el-checkbox-group v-model="multipleAnswers">
          <el-checkbox v-for="option in draft.options" :key="option.value" :value="option.value">
            {{ option.label }}
          </el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item v-if="isScored" label="分值">
        <el-input-number v-model="draft.score" :min="0" :max="100" controls-position="right" />
      </el-form-item>

      <el-form-item v-if="isScored" label="答案解析">
        <el-input v-model="draft.explanation" type="textarea" :rows="3" maxlength="500" show-word-limit />
      </el-form-item>

      <el-button
        v-if="canPolish"
        plain
        :icon="MagicStick"
        class="polish-button"
        :disabled="!aiAvailable"
        @click="$emit('polish', { ...draft })"
      >优化题目表达</el-button>
    </el-form>
  </aside>
</template>

<script setup>
import { computed, nextTick, reactive, ref, watch } from 'vue'
import { Close, Delete, MagicStick, Plus, Tools } from '@element-plus/icons-vue'
import { getModuleDefinition } from '../utils/presetFactories.js'

const props = defineProps({
  item: { type: Object, default: null },
  aiAvailable: { type: Boolean, default: false }
})
const emit = defineEmits(['update', 'delete', 'polish'])

const draft = reactive({ options: [] })
const syncing = ref(false)

const definition = computed(() => getModuleDefinition(draft.moduleType))
const isStatic = computed(() => ['learningObjective', 'knowledgeExplanation'].includes(draft.moduleType))
const isChoice = computed(() => ['singleChoice', 'multipleChoice'].includes(draft.moduleType))
const isAnswerField = computed(() => !isStatic.value)
const isScored = computed(() => ['preClassCheck', 'singleChoice', 'multipleChoice', 'shortAnswer'].includes(draft.moduleType))
const canPolish = computed(() => ['preClassCheck', 'singleChoice', 'multipleChoice', 'shortAnswer', 'reflection'].includes(draft.moduleType))
const multipleAnswers = computed({
  get: () => Array.isArray(draft.correctAnswer)
    ? draft.correctAnswer
    : String(draft.correctAnswer || '').split(',').filter(Boolean),
  set: value => { draft.correctAnswer = value }
})

watch(
  () => props.item,
  value => {
    syncing.value = true
    Object.keys(draft).forEach(key => delete draft[key])
    Object.assign(draft, value ? JSON.parse(JSON.stringify(value)) : { options: [] })
    nextTick(() => { syncing.value = false })
  },
  { immediate: true, deep: true }
)

watch(
  draft,
  value => {
    if (!syncing.value && props.item && !props.item.advanced) {
      emit('update', JSON.parse(JSON.stringify(value)))
    }
  },
  { deep: true }
)

function addOption() {
  const usedValues = new Set(draft.options.map(option => String(option.value)))
  let index = 0
  while (usedValues.has(String.fromCharCode(65 + index)) && index < 26) index += 1
  const value = index < 26 ? String.fromCharCode(65 + index) : `OPTION_${Date.now()}`
  draft.options.push({ label: `选项 ${value}`, value })
}

function removeOption(index) {
  const removed = draft.options[index]
  draft.options.splice(index, 1)
  if (draft.moduleType === 'singleChoice' && draft.correctAnswer === removed.value) draft.correctAnswer = ''
  if (draft.moduleType === 'multipleChoice') {
    multipleAnswers.value = multipleAnswers.value.filter(value => value !== removed.value)
  }
}
</script>

<style scoped>
.property-panel { min-width: 0; padding: 16px; border-left: 1px solid #dde5e8; background: #fff; }
.panel-heading { display: flex; align-items: center; justify-content: space-between; gap: 8px; min-height: 32px; margin-bottom: 13px; }
.panel-heading strong, .panel-heading span { display: block; }
.panel-heading strong { color: #20343d; font-size: 15px; }
.panel-heading span { margin-top: 2px; color: #73868d; font-size: 12px; }
.teacher-form :deep(.el-form-item) { margin-bottom: 16px; }
.teacher-form :deep(.el-form-item__label) { color: #536871; font-size: 12px; font-weight: 650; }
.option-list, .option-row { display: grid; gap: 7px; width: 100%; }
.option-row { grid-template-columns: 22px minmax(0, 1fr) 28px; align-items: center; }
.option-row > span { display: grid; place-items: center; height: 22px; color: #236e69; border-radius: 50%; background: #e9f4f2; font-size: 11px; font-weight: 700; }
.add-option, .polish-button { width: 100%; }
.advanced-note { padding: 24px 16px; color: #65777e; text-align: center; border: 1px dashed #cdd9dc; border-radius: 6px; background: #f8fafa; }
.advanced-note strong { display: block; margin: 8px 0 4px; color: #344e58; }
.advanced-note p { margin: 0; font-size: 12px; line-height: 1.7; }
@media (max-width: 1100px) { .property-panel { border-top: 1px solid #dde5e8; border-left: 0; } }
</style>
