package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.service.FlowchartLayoutService.FlowEdge;
import com.ruoyi.business.service.FlowchartLayoutService.FlowNode;
import com.ruoyi.common.exception.ServiceException;

/** 排版算法是本功能最大不确定点，重点验证坐标合法、异常兜底以及与服务端安全边界的协同。 */
class FlowchartLayoutServiceTest
{
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FlowchartLayoutService layout = new FlowchartLayoutService(objectMapper);
    private final FlowchartDocumentService documents = new FlowchartDocumentService(objectMapper);

    private FlowNode node(String key, String type, String text) { return new FlowNode(key, type, text); }
    private FlowEdge edge(String from, String to, String label) { return new FlowEdge(from, to, label); }

    private JsonNode normalize(String layoutJson) { return read(documents.normalizeDocument(layoutJson, "标准答案")); }
    private JsonNode read(String json)
    {
        try { return objectMapper.readTree(json); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    @Test
    void shouldLayerALinearChainTopToBottom()
    {
        List<FlowNode> nodes = new ArrayList<FlowNode>();
        nodes.add(node("k1", "terminal", "开始"));
        nodes.add(node("k2", "process", "计算面积"));
        nodes.add(node("k3", "terminal", "结束"));
        List<FlowEdge> edges = new ArrayList<FlowEdge>();
        edges.add(edge("k1", "k2", ""));
        edges.add(edge("k2", "k3", ""));

        JsonNode doc = normalize(layout.buildDocument(nodes, edges));
        JsonNode out = doc.path("nodes");
        assertEquals(3, out.size());
        assertEquals("n1", out.get(0).path("id").asText());
        assertEquals("terminal", out.get(0).path("type").asText());
        // 链式结构逐层下移，纵坐标严格递增。
        assertTrue(out.get(0).path("y").asInt() < out.get(1).path("y").asInt());
        assertTrue(out.get(1).path("y").asInt() < out.get(2).path("y").asInt());
        assertEquals(2, doc.path("edges").size());
        assertEquals("计算面积", out.get(1).path("text").asText());
    }

    @Test
    void shouldSpreadDecisionBranchesWithinOneLayer()
    {
        List<FlowNode> nodes = new ArrayList<FlowNode>();
        nodes.add(node("a", "terminal", "开始"));
        nodes.add(node("b", "decision", "是否为偶数"));
        nodes.add(node("c", "process", "输出偶数"));
        nodes.add(node("d", "process", "输出奇数"));
        nodes.add(node("e", "terminal", "结束"));
        List<FlowEdge> edges = new ArrayList<FlowEdge>();
        edges.add(edge("a", "b", ""));
        edges.add(edge("b", "c", "是"));
        edges.add(edge("b", "d", "否"));
        edges.add(edge("c", "e", ""));
        edges.add(edge("d", "e", ""));

        JsonNode doc = normalize(layout.buildDocument(nodes, edges));
        JsonNode out = doc.path("nodes");
        assertEquals(5, out.size());
        // 分支的两个处理节点应落在同一层但不同横坐标，避免重叠。
        JsonNode c = nodeById(out, "n3");
        JsonNode d = nodeById(out, "n4");
        assertEquals(c.path("y").asInt(), d.path("y").asInt());
        assertTrue(c.path("x").asInt() != d.path("x").asInt());
        assertEquals(5, doc.path("edges").size());
    }

    @Test
    void shouldStillLayOutWhenCycleAndIsolatedNodesExist()
    {
        List<FlowNode> nodes = new ArrayList<FlowNode>();
        nodes.add(node("x", "process", "循环A"));
        nodes.add(node("y", "process", "循环B"));
        nodes.add(node("z", "terminal", "孤立"));
        List<FlowEdge> edges = new ArrayList<FlowEdge>();
        edges.add(edge("x", "y", ""));
        edges.add(edge("y", "x", ""));

        JsonNode doc = normalize(layout.buildDocument(nodes, edges));
        assertEquals(3, doc.path("nodes").size());
        assertEquals(2, doc.path("edges").size());
    }

    @Test
    void shouldDropDanglingSelfAndDuplicateEdgesButKeepValidOnes()
    {
        List<FlowNode> nodes = new ArrayList<FlowNode>();
        nodes.add(node("p", "process", "步骤一"));
        nodes.add(node("q", "process", "步骤二"));
        List<FlowEdge> edges = new ArrayList<FlowEdge>();
        edges.add(edge("p", "q", ""));
        edges.add(edge("p", "p", ""));        // 自环丢弃
        edges.add(edge("p", "ghost", ""));    // 悬空丢弃
        edges.add(edge("p", "q", "重复"));    // 重复有向边丢弃

        JsonNode doc = normalize(layout.buildDocument(nodes, edges));
        assertEquals(1, doc.path("edges").size());
    }

    @Test
    void shouldRejectEmptyAndOversizedGraph()
    {
        assertThrows(ServiceException.class, () -> layout.buildDocument(new ArrayList<FlowNode>(), new ArrayList<FlowEdge>()));
        List<FlowNode> tooMany = new ArrayList<FlowNode>();
        for (int i = 0; i < 201; i++) tooMany.add(node("n" + i, "process", "步骤" + i));
        assertThrows(ServiceException.class, () -> layout.buildDocument(tooMany, new ArrayList<FlowEdge>()));
    }

    @Test
    void shouldKeepDeepChainWithinCoordinateBounds()
    {
        List<FlowNode> nodes = new ArrayList<FlowNode>();
        List<FlowEdge> edges = new ArrayList<FlowEdge>();
        int total = 80;
        for (int i = 0; i < total; i++) nodes.add(node("k" + i, "process", "步骤" + i));
        for (int i = 1; i < total; i++) edges.add(edge("k" + (i - 1), "k" + i, ""));

        JsonNode doc = normalize(layout.buildDocument(nodes, edges));
        assertEquals(total, doc.path("nodes").size());
        for (JsonNode n : doc.path("nodes"))
        {
            assertTrue(Math.abs(n.path("x").asDouble()) <= 9500, "x 越界");
            assertTrue(Math.abs(n.path("y").asDouble()) <= 9500, "y 越界");
        }
    }

    @Test
    void shouldLetDocumentServiceBlockUnsafeTextFromModel()
    {
        List<FlowNode> nodes = new ArrayList<FlowNode>();
        nodes.add(node("k1", "process", "<script>alert(1)</script>"));
        String layoutJson = layout.buildDocument(nodes, new ArrayList<FlowEdge>());
        // 排版不校验文字内容，安全边界统一由 normalizeDocument 拦截，证明 AI 产出不会绕过。
        assertThrows(ServiceException.class, () -> documents.normalizeDocument(layoutJson, "标准答案"));
    }

    @Test
    void shouldLayOutLoopingBinarySearchFlowchartByBreadthFirstLayers()
    {
        // 线上实测的教材二分查找流程图：含两条回到判断框的回边，原拓扑排序实现会把节点挤成一行。
        List<FlowNode> ns = new ArrayList<FlowNode>();
        ns.add(node("start", "terminal", "开始"));
        ns.add(node("init", "process", "da←37,cz←1,zz←100"));
        ns.add(node("c1", "decision", "cz≤zz?"));
        ns.add(node("calc", "process", "计算中间值cai"));
        ns.add(node("c2", "decision", "cai=da?"));
        ns.add(node("c3", "decision", "da<cai?"));
        ns.add(node("upd1", "process", "zz←cai-1"));
        ns.add(node("upd2", "process", "cz←cai+1"));
        ns.add(node("found", "inputOutput", "找到，输出信息"));
        ns.add(node("notfound", "inputOutput", "未找到，输出信息"));
        ns.add(node("end", "terminal", "结束"));
        List<FlowEdge> es = new ArrayList<FlowEdge>();
        for (String[] e : new String[][] {
                {"start", "init", ""}, {"init", "c1", ""}, {"c1", "calc", "是"}, {"c1", "notfound", "否"},
                {"calc", "c2", ""}, {"c2", "found", "是"}, {"c2", "c3", "否"}, {"c3", "upd1", "是"},
                {"c3", "upd2", "否"}, {"upd1", "c1", ""}, {"upd2", "c1", ""}, {"found", "end", ""}, {"notfound", "end", ""}})
            es.add(edge(e[0], e[1], e[2]));

        JsonNode doc = normalize(layout.buildDocument(ns, es));
        assertEquals(11, doc.path("nodes").size());
        assertEquals(13, doc.path("edges").size());
        java.util.Set<Integer> ys = new java.util.HashSet<Integer>();
        java.util.Map<Integer, Integer> perLayer = new java.util.HashMap<Integer, Integer>();
        for (JsonNode n : doc.path("nodes")) {
            int y = n.path("y").asInt();
            ys.add(y);
            perLayer.put(y, perLayer.getOrDefault(y, 0) + 1);
        }
        assertTrue(ys.size() >= 6, "纵向层级过少，仍会挤成一行：" + ys.size());
        for (Integer c : perLayer.values()) assertTrue(c <= 3, "单层节点过多，仍会挤成一行：" + c);
        JsonNode firstEdge = doc.path("edges").get(0);
        assertTrue(firstEdge.path("pointsList").size() >= 2, "连线缺少正交折线");
        assertTrue(firstEdge.path("sourceAnchorId").asText().endsWith("_bottom"), "连线缺少起点锚点");
        assertTrue(firstEdge.path("targetAnchorId").asText().length() > 0, "连线缺少终点锚点");
        // 判断框的两条分支应从左右不同锚点出，不能共用底部中点。
        boolean hasLeftBefore = false, hasRightBefore = false;
        for (JsonNode e : doc.path("edges")) {
            String sa = e.path("sourceAnchorId").asText();
            if (sa.endsWith("_left")) hasLeftBefore = true;
            if (sa.endsWith("_right")) hasRightBefore = true;
        }
        assertTrue(hasLeftBefore && hasRightBefore, "判断框分支未使用左右不同锚点");
    }

    private JsonNode nodeById(JsonNode nodes, String id)
    {
        for (JsonNode node : nodes) if (id.equals(node.path("id").asText())) return node;
        throw new AssertionError("未找到节点 " + id);
    }
}
