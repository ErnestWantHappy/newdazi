import test from 'node:test'
import assert from 'node:assert/strict'
import { logicFlowToFlowchart, stringifyFlowchartDocument, parseFlowchartDocument, flowchartToLogicFlow } from '../schema.js'

const nodes = [{ id: 'a', type: 'process', x: 100, y: 100 }, { id: 'b', type: 'decision', x: 400, y: 300 }]
const edge = { id: 'e', sourceNodeId: 'a', targetNodeId: 'b', startPoint: { x: 174, y: 100 }, endPoint: { x: 318, y: 300 }, sourceAnchorId: 'a_right', targetAnchorId: 'b_left', pointsList: [{ x: 174, y: 100 }, { x: 250, y: 100 }, { x: 250, y: 300 }, { x: 318, y: 300 }], text: { x: 263, y: 200, value: '是' } }
test('旧侧边锚点迁移到中线，旧折线由画布重新布线，标签位置仍保留', () => {
  let graph = { nodes, edges: [edge] }
  for (let i = 0; i < 3; i++) graph = flowchartToLogicFlow(parseFlowchartDocument(stringifyFlowchartDocument(logicFlowToFlowchart(graph))))
  assert.equal(graph.edges[0].sourceAnchorId, 'a_bottom')
  assert.equal(graph.edges[0].targetAnchorId, 'b_left')
  assert.equal(graph.edges[0].pointsList, undefined)
  assert.deepEqual(graph.edges[0].text, edge.text)
})
test('图形尺寸保存往返，平行四边形旧锚点转为上下中线', () => {
  const graph = flowchartToLogicFlow(logicFlowToFlowchart({
    nodes: [{ id: 'p', type: 'inputOutput', x: 100, y: 100, properties: { width: 320, height: 110 } },
      { id: 'q', type: 'process', x: 100, y: 350 }],
    edges: [{ id: 'e', sourceNodeId: 'p', targetNodeId: 'q', sourceAnchorId: 'p_1', targetAnchorId: 'q_top' }]
  }))
  assert.equal(graph.nodes[0].properties.width, 320)
  assert.equal(graph.nodes[0].properties.height, 110)
  assert.equal(graph.edges[0].sourceAnchorId, 'p_bottom')
})

test('判断菱形保存完整宽高且去掉临时半径字段', () => {
  const document = logicFlowToFlowchart({
    nodes: [{ id: 'd', type: 'decision', x: 260, y: 180,
      properties: { width: 300, height: 180, rx: 150, ry: 90 } }],
    edges: []
  })
  const restored = flowchartToLogicFlow(parseFlowchartDocument(stringifyFlowchartDocument(document)))
  assert.deepEqual(restored.nodes[0].properties.width, 300)
  assert.deepEqual(restored.nodes[0].properties.height, 180)
  assert.equal(restored.nodes[0].properties.rx, undefined)
})
test('旧作品无几何仍可读，非法几何不进入文档', () => {
  const legacy = logicFlowToFlowchart({ nodes, edges: [{ id: 'e', sourceNodeId: 'a', targetNodeId: 'b', text: '是' }] })
  assert.equal(flowchartToLogicFlow(legacy).edges[0].text, '是')
  const invalid = logicFlowToFlowchart({ nodes, edges: [{ ...edge, pointsList: [{ x: Infinity, y: 1 }, { x: 2, y: 3 }], startPoint: { x: 10001, y: 1 } }] })
  assert.equal(invalid.edges[0].pointsList, undefined)
  assert.equal(invalid.edges[0].startPoint, undefined)
})
