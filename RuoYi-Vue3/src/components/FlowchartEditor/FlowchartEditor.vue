<template>
  <div class="huacheng-editor" :class="{ 'is-readonly': readonly }">
    <div class="huacheng-toolbar">
      <div v-if="!readonly" class="huacheng-palette">
        <button v-for="item in palette" :key="item.key" type="button" class="shape-button" draggable="true"
          :disabled="!permissionsState.allowAddNode" @dragstart="startPaletteDrag($event, item)"
          @dragend="stopPaletteDrag" @click="addNode(item)">
          <span :class="['shape-icon', `shape-${item.type}`]"></span>{{ item.label }}
        </button>
      </div>
      <div class="huacheng-actions">
        <el-button v-if="!readonly" size="small" @click="undo">撤销</el-button>
        <el-button v-if="!readonly" size="small" @click="redo">重做</el-button>
        <el-button size="small" @click="zoom(false)">缩小</el-button>
        <el-button size="small" @click="zoom(true)">放大</el-button>
        <el-button size="small" @click="fitView">适应画布</el-button>
        <el-button v-if="!readonly && selectedId" size="small" type="danger" plain
          :disabled="!selectedDeletable" @click="deleteSelected">
          删除{{ selectedIsNode ? '节点' : '连线' }}
        </el-button>
        <el-button v-if="authorMode && selectedId" size="small" type="warning" plain @click="toggleSelectedLock">
          {{ selectedLocked ? '解锁元素' : '锁定元素' }}
        </el-button>
        <el-button v-if="exportable" size="small" type="primary" plain @click="exportPng">导出 PNG</el-button>
      </div>
    </div>
    <div class="huacheng-hint">
      <span v-if="readonly">只读查看：可以缩放和导出，不会修改作品。</span>
      <span v-else>单击图形可自动添加，也可把图形拖到指定位置；选中节点或连线后可删除。</span>
      <slot name="status"></slot>
    </div>
    <div ref="canvasRef" class="huacheng-canvas" :style="{ height: `${height}px` }"
      @dragover.prevent @drop.prevent="dropPaletteNode"></div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import LogicFlow, {
  DiamondNode, DiamondNodeModel, EllipseNode, EllipseNodeModel,
  PolygonNode, PolygonNodeModel, RectNode, RectNodeModel
} from '@logicflow/core'
import '@logicflow/core/es/index.css'
import {
  flowchartToLogicFlow, logicFlowToFlowchart, normalizeFlowchartPermissions,
  parseFlowchartDocument, stringifyFlowchartDocument
} from './schema'

const props = defineProps({
  modelValue: { type: String, default: '' },
  mode: { type: String, default: 'STUDENT' },
  permissions: { type: [String, Object], default: () => ({}) },
  height: { type: Number, default: 560 },
  exportable: { type: Boolean, default: true }
})
const emit = defineEmits(['update:modelValue', 'change'])
const canvasRef = ref()
const selectedId = ref('')
const selectedLocked = ref(false)
const selectedType = ref('')
let lf = null
let rendering = false
let draggingPaletteItem = null

const readonly = computed(() => props.mode === 'READONLY')
const authorMode = computed(() => props.mode === 'AUTHOR_ANSWER' || props.mode === 'AUTHOR_STARTER')
const permissionsState = computed(() => authorMode.value
  ? normalizeFlowchartPermissions({})
  : normalizeFlowchartPermissions(props.permissions))
const selectedIsNode = computed(() => ['terminal', 'process', 'decision', 'inputOutput'].includes(selectedType.value))
const selectedDeletable = computed(() => Boolean(selectedId.value)
  && !selectedLocked.value
  && (selectedIsNode.value ? permissionsState.value.allowDeleteNode : permissionsState.value.allowDeleteEdge))
const palette = [
  { key: 'start', type: 'terminal', label: '开始', text: '开始' },
  { key: 'end', type: 'terminal', label: '结束', text: '结束' },
  { key: 'process', type: 'process', label: '处理', text: '处理步骤' },
  { key: 'decision', type: 'decision', label: '判断', text: '是否满足？' },
  { key: 'inputOutput', type: 'inputOutput', label: '输入/输出', text: '输入或输出' }
]

class TerminalModel extends EllipseNodeModel {
  setAttributes() { this.rx = 72; this.ry = 34 }
}
class ProcessModel extends RectNodeModel {
  setAttributes() { this.width = 148; this.height = 64; this.radius = 6 }
}
class DecisionModel extends DiamondNodeModel {
  setAttributes() { this.rx = 82; this.ry = 48 }
}
class InputOutputModel extends PolygonNodeModel {
  setAttributes() {
    // PolygonNodeModel 以图形左上角为原点；使用负坐标会让图形与文字中心错位。
    this.points = [[12, 0], [140, 0], [128, 60], [0, 60]]
  }

  getDefaultAnchor() {
    // 多边形默认把四个顶点作为锚点；流程图应从四条边的中点连接，便于小学生对齐箭头。
    const points = this.pointsPosition
    return points.map((point, index) => {
      const next = points[(index + 1) % points.length]
      return {
        x: (point.x + next.x) / 2,
        y: (point.y + next.y) / 2,
        id: `${this.id}_${index}`
      }
    })
  }
}

onMounted(async () => {
  await nextTick()
  lf = new LogicFlow({
    container: canvasRef.value,
    grid: { size: 20, visible: true, type: 'dot', config: { color: '#d7e8f7' } },
    edgeType: 'polyline',
    history: true,
    keyboard: { enabled: true },
    snapline: true,
    allowRotate: false,
    allowResize: false,
    guards: {
      beforeDelete: data => canDelete(data)
    }
  })
  lf.batchRegister([
    { type: 'terminal', view: EllipseNode, model: TerminalModel },
    { type: 'process', view: RectNode, model: ProcessModel },
    { type: 'decision', view: DiamondNode, model: DecisionModel },
    { type: 'inputOutput', view: PolygonNode, model: InputOutputModel }
  ])
  lf.setTheme({
    baseNode: { fill: '#ffffff', stroke: '#3182ce', strokeWidth: 2 },
    diamond: { fill: '#fff8e6', stroke: '#e6a23c' },
    ellipse: { fill: '#ecf8ff', stroke: '#1597bb' },
    polygon: { fill: '#f0f9eb', stroke: '#4aa564' },
    polyline: { stroke: '#476582', strokeWidth: 2 },
    arrow: { fill: '#476582', stroke: '#476582' },
    nodeText: { color: '#1f2d3d', fontSize: 15 },
    edgeText: { color: '#34495e', fontSize: 14, background: { fill: '#ffffff' } }
  })
  applyEditConfig()
  lf.graphModel.addNodeMoveRules(nodeModel => canMove(nodeModel))
  renderValue(props.modelValue)
  lf.on('element:click', ({ data }) => {
    selectedId.value = data?.id || ''
    selectedLocked.value = Boolean(data?.properties?.locked)
    selectedType.value = data?.type || ''
  })
  lf.on('blank:click', clearSelection)
  lf.on('history:change', emitChange)
  window.addEventListener('resize', resize)
})

onBeforeUnmount(() => window.removeEventListener('resize', resize))

watch(() => props.modelValue, value => {
  if (!lf || rendering) return
  const current = stringifyFlowchartDocument(logicFlowToFlowchart(lf.getGraphRawData()))
  const incoming = stringifyFlowchartDocument(parseFlowchartDocument(value))
  if (current !== incoming) renderValue(value)
})

watch([readonly, permissionsState], () => applyEditConfig(), { deep: true })

function applyEditConfig() {
  if (!lf) return
  const p = permissionsState.value
  lf.updateEditConfig({
    isSilentMode: readonly.value,
    adjustNodePosition: !readonly.value && p.allowMoveNode,
    adjustEdge: !readonly.value && (p.allowAddEdge || p.allowDeleteEdge),
    adjustEdgeStartAndEnd: !readonly.value && p.allowAddEdge,
    nodeTextEdit: !readonly.value && p.allowEditText,
    edgeTextEdit: !readonly.value && p.allowEditText,
    hideAnchors: readonly.value || !p.allowAddEdge,
    allowRotate: false,
    allowResize: false
  })
}

function renderValue(value) {
  if (!lf) return
  rendering = true
  lf.render(flowchartToLogicFlow(parseFlowchartDocument(value)))
  setTimeout(() => { rendering = false; applyElementLocks(); lf.fitView(36, 36) }, 0)
}

function applyElementLocks() {
  if (!lf) return
  for (const node of lf.graphModel.nodes) {
    const locked = Boolean(node.properties?.locked)
    node.draggable = !readonly.value && permissionsState.value.allowMoveNode && !locked
    if (node.text) node.text.editable = !readonly.value && permissionsState.value.allowEditText
      && !locked && node.properties?.textEditable !== false
  }
  for (const edge of lf.graphModel.edges) {
    const locked = Boolean(edge.properties?.locked)
    edge.draggable = !readonly.value && !locked
    edge.isShowAdjustPoint = !readonly.value && !locked
    if (edge.text) edge.text.editable = !readonly.value && permissionsState.value.allowEditText
      && !locked && edge.properties?.textEditable !== false
  }
}

function addNode(item, position) {
  if (!lf || readonly.value || !permissionsState.value.allowAddNode) return
  const count = lf.graphModel.nodes.length
  const x = position?.x ?? 240 + (count % 4) * 150
  const y = position?.y ?? 120 + Math.floor(count / 4) * 100
  lf.addNode({
    id: `node_${Date.now()}_${count}`,
    type: item.type,
    x,
    y,
    text: { x, y, value: item.text },
    properties: { locked: false, textEditable: true }
  })
}

function startPaletteDrag(event, item) {
  if (!lf || readonly.value || !permissionsState.value.allowAddNode) return
  draggingPaletteItem = item
  event.dataTransfer.effectAllowed = 'copy'
  event.dataTransfer.setData('text/plain', item.key)
}

function dropPaletteNode(event) {
  if (!lf || !draggingPaletteItem) return
  const position = lf.getPointByClient({ x: event.clientX, y: event.clientY })?.canvasOverlayPosition
  addNode(draggingPaletteItem, position)
  draggingPaletteItem = null
}

function stopPaletteDrag() {
  draggingPaletteItem = null
}

function canMove(model) {
  return !readonly.value && permissionsState.value.allowMoveNode && !model?.properties?.locked
}

function canDelete(data) {
  if (readonly.value || data?.properties?.locked) return false
  const node = ['terminal', 'process', 'decision', 'inputOutput'].includes(data?.type)
  return node ? permissionsState.value.allowDeleteNode : permissionsState.value.allowDeleteEdge
}

function emitChange() {
  if (!lf || rendering || readonly.value) return
  const document = logicFlowToFlowchart(lf.getGraphRawData())
  const json = stringifyFlowchartDocument(document)
  emit('update:modelValue', json)
  emit('change', json)
  applyElementLocks()
}

function deleteSelected() {
  if (!lf || !selectedDeletable.value) return
  const deleted = lf.deleteElement(selectedId.value)
  if (deleted) {
    clearSelection()
    emitChange()
  }
}

function clearSelection() {
  selectedId.value = ''
  selectedLocked.value = false
  selectedType.value = ''
}

function toggleSelectedLock() {
  if (!lf || !authorMode.value || !selectedId.value) return
  const next = !selectedLocked.value
  lf.setProperties(selectedId.value, { locked: next, textEditable: !next })
  selectedLocked.value = next
  emitChange()
}

function undo() { lf?.undo() }
function redo() { lf?.redo() }
function zoom(direction) { lf?.zoom(direction) }
function fitView() { lf?.fitView(36, 36) }
function resize() { lf?.resize() }

async function exportPng() {
  const svg = canvasRef.value?.querySelector('svg')
  if (!svg) return
  const clone = svg.cloneNode(true)
  clone.setAttribute('xmlns', 'http://www.w3.org/2000/svg')
  const width = Math.max(canvasRef.value.clientWidth, 800)
  const height = Math.max(canvasRef.value.clientHeight, 500)
  clone.setAttribute('width', width)
  clone.setAttribute('height', height)
  const blob = new Blob([new XMLSerializer().serializeToString(clone)], { type: 'image/svg+xml;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  try {
    const image = new Image()
    await new Promise((resolve, reject) => { image.onload = resolve; image.onerror = reject; image.src = url })
    const canvas = document.createElement('canvas')
    canvas.width = width * 2; canvas.height = height * 2
    const context = canvas.getContext('2d')
    context.scale(2, 2); context.fillStyle = '#ffffff'; context.fillRect(0, 0, width, height)
    context.drawImage(image, 0, 0, width, height)
    const link = document.createElement('a')
    link.download = `画程流程图_${new Date().toISOString().slice(0, 10)}.png`
    link.href = canvas.toDataURL('image/png')
    link.click()
  } finally {
    URL.revokeObjectURL(url)
  }
}
</script>

<style scoped lang="scss">
.huacheng-editor { border: 1px solid #cfe2f3; border-radius: 12px; overflow: hidden; background: #fff; }
.huacheng-toolbar { display: flex; justify-content: space-between; gap: 12px; padding: 10px 12px; background: linear-gradient(135deg, #ecf8ff, #f0f9eb); border-bottom: 1px solid #dcecf7; }
.huacheng-palette, .huacheng-actions { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; }
.shape-button { display: inline-flex; align-items: center; gap: 7px; min-height: 38px; padding: 7px 10px; color: #24445d; background: #fff; border: 1px solid #a9cce3; border-radius: 8px; cursor: pointer; }
.shape-button:hover { border-color: #409eff; color: #1677b8; transform: translateY(-1px); }
.shape-button:disabled { cursor: not-allowed; opacity: .45; transform: none; }
.shape-icon { display: inline-block; width: 24px; height: 16px; border: 2px solid #3182ce; background: #ecf8ff; }
.shape-terminal { border-radius: 50%; }
.shape-decision { width: 17px; height: 17px; transform: rotate(45deg); background: #fff8e6; border-color: #e6a23c; }
.shape-inputOutput { transform: skew(-18deg); background: #f0f9eb; border-color: #4aa564; }
.huacheng-hint { display: flex; justify-content: space-between; gap: 12px; padding: 7px 14px; color: #5d7284; font-size: 13px; background: #fbfdff; border-bottom: 1px solid #eef5fa; }
.huacheng-canvas { width: 100%; min-height: 360px; background: #fff; }
.is-readonly .huacheng-toolbar { background: #f5f7fa; }
@media (max-width: 900px) { .huacheng-toolbar { flex-direction: column; } .shape-button { min-height: 44px; } }
</style>
