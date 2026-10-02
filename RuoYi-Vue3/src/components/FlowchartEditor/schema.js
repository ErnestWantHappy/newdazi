export const FLOWCHART_SCHEMA_VERSION = '1.0'

export const EMPTY_FLOWCHART = Object.freeze({
  schemaVersion: FLOWCHART_SCHEMA_VERSION,
  nodes: [],
  edges: []
})

export const DEFAULT_FLOWCHART_PERMISSIONS = Object.freeze({
  allowAddNode: true,
  allowDeleteNode: true,
  allowEditText: true,
  allowAddEdge: true,
  allowDeleteEdge: true,
  allowMoveNode: true
})

const NODE_TYPES = new Set(['terminal', 'process', 'decision', 'inputOutput'])

export function parseFlowchartDocument(value) {
  try {
    const source = typeof value === 'string' ? JSON.parse(value) : (value || {})
    return normalizeFlowchartDocument(source)
  } catch (_) {
    return normalizeFlowchartDocument(EMPTY_FLOWCHART)
  }
}

export function normalizeFlowchartDocument(source) {
  const nodes = Array.isArray(source?.nodes) ? source.nodes.slice(0, 200) : []
  const nodeIds = new Set()
  const safeNodes = nodes.map((node, index) => {
    const id = safeId(node?.id, `node_${index + 1}`)
    nodeIds.add(id)
    return {
      id,
      type: NODE_TYPES.has(node?.type) ? node.type : 'process',
      x: safeNumber(node?.x, 240 + (index % 4) * 150),
      y: safeNumber(node?.y, 100 + Math.floor(index / 4) * 100),
      text: textValue(node?.text).slice(0, 200),
      properties: {
        locked: Boolean(node?.properties?.locked),
        textEditable: node?.properties?.textEditable !== false,
        ...nodeSize(node)
      }
    }
  })
  const safeEdges = (Array.isArray(source?.edges) ? source.edges : [])
    .slice(0, 400)
    .map((edge, index) => ({
      id: safeId(edge?.id, `edge_${index + 1}`),
      type: 'polyline',
      sourceNodeId: safeId(edge?.sourceNodeId, ''),
      targetNodeId: safeId(edge?.targetNodeId, ''),
      text: textValue(edge?.text).slice(0, 200),
      ...edgeGeometry(edge),
      properties: {
        locked: Boolean(edge?.properties?.locked),
        textEditable: edge?.properties?.textEditable !== false
      }
    }))
    .filter(edge => nodeIds.has(edge.sourceNodeId) && nodeIds.has(edge.targetNodeId))
  return { schemaVersion: FLOWCHART_SCHEMA_VERSION, nodes: safeNodes, edges: safeEdges }
}

export function stringifyFlowchartDocument(value) {
  return JSON.stringify(normalizeFlowchartDocument(value))
}

export function normalizeFlowchartPermissions(value) {
  let source = value
  if (typeof source === 'string') {
    try { source = JSON.parse(source) } catch (_) { source = {} }
  }
  return Object.fromEntries(Object.keys(DEFAULT_FLOWCHART_PERMISSIONS).map(key => [
    key,
    source?.[key] !== false
  ]))
}

export function flowchartToLogicFlow(document) {
  const source = normalizeFlowchartDocument(document)
  const nodeById = new Map(source.nodes.map(node => [node.id, node]))
  return {
    nodes: source.nodes.map(node => ({
      ...node,
      text: { x: node.x, y: node.y, value: node.text },
      properties: {
        ...node.properties,
        ...(node.type === 'inputOutput'
          ? { points: [[12, 0], [140, 0], [128, 60], [0, 60]] }
          : {})
      }
    })),
    edges: source.edges.map(edge => ({
      ...migrateLegacySideAnchors(edge, nodeById),
      text: edge.textPosition ? { ...edge.textPosition, value: edge.text } : edge.text
    }))
  }
}

function nodeSize(node) {
  const properties = node?.properties || {}
  const width = Number(properties.width)
  const height = Number(properties.height)
  return Number.isFinite(width) && width >= 80 && width <= 1200
    && Number.isFinite(height) && height >= 40 && height <= 800
    ? { width: Math.round(width), height: Math.round(height) } : {}
}

function migrateLegacySideAnchors(edge, nodeById) {
  const source = nodeById.get(edge.sourceNodeId)
  const target = nodeById.get(edge.targetNodeId)
  const result = { ...edge }
  let migrated = false
  for (const [node, peer, key, outgoing] of [
    [source, target, 'sourceAnchorId', true], [target, source, 'targetAnchorId', false]
  ]) {
    if (!node || node.type === 'decision') continue
    const oldId = result[key]
    const suffix = oldId?.slice(node.id.length + 1)
    if (!oldId || !oldId.startsWith(`${node.id}_`)) continue
    if (['left', 'right', '0', '1', '2', '3'].includes(suffix)) {
      const position = ['0', '2'].includes(suffix) ? (suffix === '0' ? 'top' : 'bottom')
        : (outgoing ? (peer?.y >= node.y ? 'bottom' : 'top') : (peer?.y > node.y ? 'bottom' : 'top'))
      result[key] = `${node.id}_${position}`
      migrated = true
    }
  }
  if (migrated) {
    delete result.startPoint
    delete result.endPoint
    delete result.pointsList
  }
  return result
}

export function logicFlowToFlowchart(graphData) {
  return normalizeFlowchartDocument({
    schemaVersion: FLOWCHART_SCHEMA_VERSION,
    nodes: graphData?.nodes || [],
    edges: graphData?.edges || []
  })
}

function textValue(text) {
  if (text && typeof text === 'object') return String(text.value || '')
  return String(text || '')
}

function safePoint(point) {
  if (!point || typeof point.x !== 'number' || typeof point.y !== 'number') return null
  const x = safeNumber(point.x, null), y = safeNumber(point.y, null)
  return x === null || y === null ? null : { x, y }
}

function edgeGeometry(edge) {
  const result = {}
  for (const key of ['startPoint', 'endPoint']) {
    const point = safePoint(edge?.[key])
    if (point) result[key] = point
  }
  if (Array.isArray(edge?.pointsList) && edge.pointsList.length >= 2 && edge.pointsList.length <= 100) {
    const points = edge.pointsList.map(safePoint)
    if (points.every(Boolean)) result.pointsList = points
  }
  for (const key of ['sourceAnchorId', 'targetAnchorId']) {
    const id = safeId(edge?.[key], '')
    if (id) result[key] = id
  }
  // 文字仍用字符串供结构判分读取，位置单独保存并在交给 LogicFlow 时还原。
  const textPosition = safePoint(edge?.text) || safePoint(edge?.textPosition)
  if (textPosition) result.textPosition = textPosition
  return result
}

function safeId(value, fallback) {
  const text = String(value || '').trim()
  return /^[A-Za-z0-9_-]{1,64}$/.test(text) ? text : fallback
}

function safeNumber(value, fallback) {
  const number = Number(value)
  return Number.isFinite(number) && number >= -10000 && number <= 10000
    ? Math.round(number * 100) / 100
    : fallback
}
