import test from 'node:test'
import assert from 'node:assert/strict'
import { logicFlowToFlowchart, stringifyFlowchartDocument, parseFlowchartDocument, flowchartToLogicFlow } from '../schema.js'

const nodes = [{ id: 'a', type: 'process', x: 100, y: 100 }, { id: 'b', type: 'decision', x: 400, y: 300 }]
const edge = { id: 'e', sourceNodeId: 'a', targetNodeId: 'b', startPoint: { x: 174, y: 100 }, endPoint: { x: 318, y: 300 }, sourceAnchorId: 'a_right', targetAnchorId: 'b_left', pointsList: [{ x: 174, y: 100 }, { x: 250, y: 100 }, { x: 250, y: 300 }, { x: 318, y: 300 }], text: { x: 263, y: 200, value: '是' } }
test('保存、JSON往返、恢复编辑均保留绕行折点、锚点和标签位置', () => {
  let graph = { nodes, edges: [edge] }
  for (let i = 0; i < 3; i++) graph = flowchartToLogicFlow(parseFlowchartDocument(stringifyFlowchartDocument(logicFlowToFlowchart(graph))))
  for (const key of ['pointsList', 'startPoint', 'endPoint', 'sourceAnchorId', 'targetAnchorId', 'text']) assert.deepEqual(graph.edges[0][key], edge[key])
})
test('旧作品无几何仍可读，非法几何不进入文档', () => {
  const legacy = logicFlowToFlowchart({ nodes, edges: [{ id: 'e', sourceNodeId: 'a', targetNodeId: 'b', text: '是' }] })
  assert.equal(flowchartToLogicFlow(legacy).edges[0].text, '是')
  const invalid = logicFlowToFlowchart({ nodes, edges: [{ ...edge, pointsList: [{ x: Infinity, y: 1 }, { x: 2, y: 3 }], startPoint: { x: 10001, y: 1 } }] })
  assert.equal(invalid.edges[0].pointsList, undefined)
  assert.equal(invalid.edges[0].startPoint, undefined)
})
