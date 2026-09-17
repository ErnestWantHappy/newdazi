<template>
  <div class="flowchart-designer">
    <el-alert title="画程已内置在平台中：标准答案用于结构检查，基础图才会发给学生。" type="info" :closable="false" show-icon />
    <el-tabs v-model="activeTab" class="designer-tabs">
      <el-tab-pane label="① 标准答案" name="answer">
        <flowchart-editor v-model="state.answerJson" mode="AUTHOR_ANSWER" :height="480" />
      </el-tab-pane>
      <el-tab-pane label="② 学生基础图" name="starter">
        <div class="starter-actions">
          <el-button type="primary" plain @click="copyAnswerToStarter">从标准答案复制一份</el-button>
          <span>复制后可删除提示节点、调整文字，并锁定不允许学生修改的元素。</span>
        </div>
        <flowchart-editor v-model="state.starterJson" mode="AUTHOR_STARTER" :height="480" />
        <div class="tab-tip">选中节点或连线后可锁定；锁定元素发给学生后不能移动、改字或删除。</div>
      </el-tab-pane>
      <el-tab-pane label="③ 学生权限" name="permissions">
        <div class="permission-grid">
          <el-checkbox v-model="permissions.allowAddNode">允许添加图形</el-checkbox>
          <el-checkbox v-model="permissions.allowDeleteNode">允许删除图形</el-checkbox>
          <el-checkbox v-model="permissions.allowEditText">允许修改文字</el-checkbox>
          <el-checkbox v-model="permissions.allowAddEdge">允许添加箭头</el-checkbox>
          <el-checkbox v-model="permissions.allowDeleteEdge">允许删除箭头</el-checkbox>
          <el-checkbox v-model="permissions.allowMoveNode">允许移动图形</el-checkbox>
        </div>
      </el-tab-pane>
      <el-tab-pane label="④ 检查规则" name="rules">
        <div class="rule-actions">
          <el-button type="primary" :loading="generating" @click="generateRules">从标准答案重新生成</el-button>
          <span>检查节点类型、文字、箭头方向和分支文字；位置和颜色不计分。</span>
        </div>
        <el-table :data="rules" border max-height="430">
          <el-table-column label="检查项" min-width="180">
            <template #default="{ row }">
              {{ row.kind === 'NODE' ? '节点' : '连线' }}：{{ row.expectedText || ruleTarget(row) }}
            </template>
          </el-table-column>
          <el-table-column label="同义文字（逗号分隔）" min-width="220">
            <template #default="{ row }"><el-input v-model="row.aliasText" placeholder="例如：结束,完成" /></template>
          </el-table-column>
          <el-table-column label="权重" width="110">
            <template #default="{ row }"><el-input-number v-model="row.weight" :min="0" :max="100" controls-position="right" /></template>
          </el-table-column>
          <el-table-column label="启用" width="80" align="center">
            <template #default="{ row }"><el-switch v-model="row.enabled" /></template>
          </el-table-column>
          <el-table-column label="计分" width="80" align="center">
            <template #default="{ row }"><el-switch v-model="row.scoring" /></template>
          </el-table-column>
        </el-table>
        <el-empty v-if="rules.length === 0" description="请先制作标准答案，再生成检查规则" :image-size="70" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import FlowchartEditor from './FlowchartEditor.vue'
import {
  EMPTY_FLOWCHART, DEFAULT_FLOWCHART_PERMISSIONS,
  parseFlowchartDocument, stringifyFlowchartDocument
} from './schema'
import { generateFlowchartRules } from '@/api/business/flowchart'

const props = defineProps({ modelValue: { type: Object, default: () => ({}) } })
const emit = defineEmits(['update:modelValue'])
const activeTab = ref('answer')
const generating = ref(false)
const permissions = reactive({ ...DEFAULT_FLOWCHART_PERMISSIONS })
const state = reactive({
  questionId: null,
  configRevision: 0,
  schemaVersion: '1.0',
  starterJson: JSON.stringify(EMPTY_FLOWCHART),
  answerJson: JSON.stringify(EMPTY_FLOWCHART),
  permissionsJson: JSON.stringify(DEFAULT_FLOWCHART_PERMISSIONS),
  rulesJson: '[]'
})
const rules = ref([])
let syncing = false

watch(() => props.modelValue, value => {
  syncing = true
  Object.assign(state, value || {})
  Object.assign(permissions, parseObject(state.permissionsJson, DEFAULT_FLOWCHART_PERMISSIONS))
  rules.value = parseRules(state.rulesJson)
  queueMicrotask(() => { syncing = false })
}, { immediate: true, deep: true })

watch([state, permissions, rules], () => {
  if (syncing) return
  state.permissionsJson = JSON.stringify(permissions)
  state.rulesJson = JSON.stringify(rules.value.map(({ aliasText, ...rule }) => ({
    ...rule,
    aliases: String(aliasText || '').split(/[,，]/).map(item => item.trim()).filter(Boolean)
  })))
  emit('update:modelValue', { ...state })
}, { deep: true })

async function generateRules() {
  if (parseFlowchartDocument(state.answerJson).nodes.length === 0) {
    ElMessage.warning('请先在“标准答案”中制作流程图')
    activeTab.value = 'answer'
    return
  }
  generating.value = true
  try {
    const response = await generateFlowchartRules(state.answerJson)
    rules.value = parseRules(response.data || '[]')
    ElMessage.success('已生成结构检查规则，可继续补充同义文字和调整权重')
  } finally {
    generating.value = false
  }
}

async function copyAnswerToStarter() {
  const answer = parseFlowchartDocument(state.answerJson)
  if (answer.nodes.length === 0) {
    ElMessage.warning('标准答案还是空的，请先完成标准答案')
    activeTab.value = 'answer'
    return
  }
  const starter = parseFlowchartDocument(state.starterJson)
  if (starter.nodes.length > 0 || starter.edges.length > 0) {
    try {
      await ElMessageBox.confirm(
        '这会覆盖当前学生基础图，是否继续？',
        '从标准答案复制',
        { confirmButtonText: '确认复制', cancelButtonText: '取消', type: 'warning' }
      )
    } catch (_) {
      // 教师取消覆盖属于正常操作，不应在控制台留下未处理的 Promise 异常。
      return
    }
  }
  // 通过标准化后的 JSON 复制，避免基础图与标准答案共用对象引用而互相影响。
  state.starterJson = stringifyFlowchartDocument(answer)
  ElMessage.success('已复制为学生基础图，可继续删减和锁定元素')
}

function parseRules(value) {
  try {
    const source = typeof value === 'string' ? JSON.parse(value) : value
    return Array.isArray(source) ? source.map(rule => ({
      ...rule,
      aliasText: Array.isArray(rule.aliases) ? rule.aliases.join('，') : ''
    })) : []
  } catch (_) { return [] }
}

function parseObject(value, fallback) {
  try { return { ...fallback, ...(typeof value === 'string' ? JSON.parse(value) : value) } }
  catch (_) { return { ...fallback } }
}

function ruleTarget(rule) {
  return rule.kind === 'NODE'
    ? `${rule.nodeType || ''} (${rule.expectedNodeId || ''})`
    : `${rule.sourceExpectedNodeId || ''} → ${rule.targetExpectedNodeId || ''}`
}
</script>

<style scoped lang="scss">
.flowchart-designer { width: 100%; }
.designer-tabs { margin-top: 12px; }
.tab-tip, .rule-actions span, .starter-actions span { color: #66788a; font-size: 13px; }
.tab-tip { margin-top: 8px; }
.starter-actions { display: flex; align-items: center; gap: 12px; margin-bottom: 10px; }
.permission-grid { display: grid; grid-template-columns: repeat(2, minmax(180px, 1fr)); gap: 18px; padding: 24px; border: 1px solid #e4edf5; border-radius: 10px; background: #f8fbfd; }
.permission-grid :deep(.el-checkbox__label) { font-size: 15px; }
.rule-actions { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
</style>
