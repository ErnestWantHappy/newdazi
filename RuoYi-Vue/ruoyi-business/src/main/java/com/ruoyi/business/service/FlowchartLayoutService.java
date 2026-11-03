package com.ruoyi.business.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruoyi.common.exception.ServiceException;

/**
 * 把“只含拓扑和文字”的流程图模型输出，换算成 LogicFlow 需要的坐标文档。
 * 模型不输出坐标、也不能信任其坐标：
 * 1) 从“开始”节点 BFS 分层，环形回边只保留连线、不改变层级；
 * 2) 显式给出节点宽高，前端按此尺寸渲染，锚点位置才可预测；
 * 3) 为每条边生成正交折线与固定锚点，避免 LogicFlow 自动直连造成的交叉与穿越节点。
 */
@Service
public class FlowchartLayoutService
{
    private static final int MAX_NODES = 200;
    private static final int MAX_EDGES = 400;
    private static final int CENTER_X = 600;
    private static final int COL_GAP = 70;
    private static final int TOP_MARGIN = 80;
    private static final int COORD_LIMIT = 9800;
    private final ObjectMapper objectMapper;

    public FlowchartLayoutService(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    public static class FlowNode
    {
        public final String key;
        public final String type;
        public final String text;
        public FlowNode(String key, String type, String text)
        {
            this.key = key; this.type = type; this.text = text;
        }
    }

    public static class FlowEdge
    {
        public final String from;
        public final String to;
        public final String label;
        public FlowEdge(String from, String to, String label)
        {
            this.from = from; this.to = to; this.label = label;
        }
    }

    /** 排版中间态：坐标、显式尺寸、层级与最终节点 id。 */
    private static class Box
    {
        String key; String type; String text; String id;
        int x; int y; int w; int h; int layer;
    }

    public String buildDocument(List<FlowNode> nodes, List<FlowEdge> edges)
    {
        if (nodes == null || nodes.isEmpty()) throw new ServiceException("未能从照片识别出流程节点，请核对照片或改用手动绘制");
        LinkedHashMap<String, FlowNode> nodeMap = new LinkedHashMap<String, FlowNode>();
        for (FlowNode node : nodes)
        {
            if (node == null || node.key == null || node.key.trim().isEmpty()) continue;
            nodeMap.put(node.key.trim(), node);
        }
        if (nodeMap.isEmpty()) throw new ServiceException("未能从照片识别出有效流程节点");
        if (nodeMap.size() > MAX_NODES) throw new ServiceException("识别出的节点过多，请改用更清晰的局部照片或手动绘制");

        Map<String, List<String>> succ = new LinkedHashMap<String, List<String>>();
        for (String key : nodeMap.keySet()) succ.put(key, new ArrayList<String>());
        List<FlowEdge> validEdges = new ArrayList<FlowEdge>();
        for (FlowEdge edge : edges == null ? new ArrayList<FlowEdge>() : edges)
        {
            if (edge == null || edge.from == null || edge.to == null) continue;
            String from = edge.from.trim(), to = edge.to.trim();
            if (!nodeMap.containsKey(from) || !nodeMap.containsKey(to) || from.equals(to)) continue;
            if (succ.get(from).contains(to)) continue;
            succ.get(from).add(to);
            validEdges.add(new FlowEdge(from, to, edge.label));
        }
        if (validEdges.size() > MAX_EDGES) throw new ServiceException("识别出的连线过多，请核对照片");

        Map<String, Integer> layer = assignLayers(nodeMap, succ);
        Map<String, String> idByKey = assignIds(nodeMap.keySet());
        Map<String, Box> boxes = layoutBoxes(nodeMap, layer, idByKey);

        try
        {
            ObjectNode doc = objectMapper.createObjectNode();
            doc.put("schemaVersion", FlowchartDocumentService.SCHEMA_VERSION);
            ArrayNode nodesOut = doc.putArray("nodes");
            for (String key : nodeMap.keySet())
            {
                Box box = boxes.get(key);
                ObjectNode n = nodesOut.addObject();
                n.put("id", box.id);
                n.put("type", box.type);
                n.put("x", box.x);
                n.put("y", box.y);
                n.put("text", box.text == null ? "" : box.text);
                ObjectNode props = n.putObject("properties");
                props.put("locked", false);
                props.put("textEditable", true);
                props.put("width", box.w);
                props.put("height", box.h);
            }
            ArrayNode edgesOut = doc.putArray("edges");
            routeEdges(edgesOut, validEdges, boxes);
            return objectMapper.writeValueAsString(doc);
        }
        catch (ServiceException e) { throw e; }
        catch (Exception ex) { throw new ServiceException("流程图坐标排版失败"); }
    }

    /**
     * 从“开始”节点做 BFS 分层，回边只保留连线、不改变层级。
     * 原实现用拓扑排序，含循环的算法流程图会把环下游全部挤到同一层，故改为 BFS。
     * 出度为 0 的终点沉到最底，避免最短路径把“结束”抬到中间。
     */
    private Map<String, Integer> assignLayers(LinkedHashMap<String, FlowNode> nodeMap, Map<String, List<String>> succ)
    {
        Map<String, Integer> layer = new LinkedHashMap<String, Integer>();
        for (String key : nodeMap.keySet()) layer.put(key, 0);
        String root = pickStart(nodeMap, succ);
        java.util.Set<String> visited = new java.util.HashSet<String>();
        Deque<String> queue = new ArrayDeque<String>();
        int maxLayer = 0;
        if (root != null) { visited.add(root); queue.add(root); }
        maxLayer = expandLayers(succ, layer, visited, queue, maxLayer);
        for (String key : nodeMap.keySet())
        {
            if (visited.contains(key)) continue;
            int base = maxLayer + 1;
            visited.add(key); layer.put(key, base);
            if (base > maxLayer) maxLayer = base;
            queue.add(key);
            maxLayer = expandLayers(succ, layer, visited, queue, maxLayer);
        }
        int bottom = maxLayer + 1;
        for (String key : nodeMap.keySet())
            if (succ.get(key).isEmpty()) layer.put(key, bottom);
        return layer;
    }

    private int expandLayers(Map<String, List<String>> succ, Map<String, Integer> layer,
            java.util.Set<String> visited, Deque<String> queue, int maxLayer)
    {
        while (!queue.isEmpty())
        {
            String from = queue.poll();
            int next = layer.get(from) + 1;
            for (String to : succ.get(from))
            {
                if (visited.add(to))
                {
                    layer.put(to, next);
                    if (next > maxLayer) maxLayer = next;
                    queue.add(to);
                }
            }
        }
        return maxLayer;
    }

    private String pickStart(LinkedHashMap<String, FlowNode> nodeMap, Map<String, List<String>> succ)
    {
        for (FlowNode node : nodeMap.values())
            if ("terminal".equals(node.type) && node.text != null && node.text.contains("开始")) return node.key;
        for (FlowNode node : nodeMap.values())
            if ("terminal".equals(node.type) && (node.text == null || !node.text.contains("结束"))) return node.key;
        Map<String, Integer> indegree = new LinkedHashMap<String, Integer>();
        for (String key : nodeMap.keySet()) indegree.put(key, 0);
        for (List<String> targets : succ.values())
            for (String to : targets) indegree.put(to, indegree.get(to) + 1);
        for (String key : nodeMap.keySet()) if (indegree.get(key) == 0) return key;
        return nodeMap.isEmpty() ? null : nodeMap.keySet().iterator().next();
    }

    private Map<String, String> assignIds(Iterable<String> keys)
    {
        Map<String, String> idByKey = new LinkedHashMap<String, String>();
        int counter = 0;
        for (String key : keys) idByKey.put(key, "n" + (++counter));
        return idByKey;
    }

    /** 按层排布：同层按节点实际宽度横向铺开并整体居中，行距随层数自适应，保证坐标合法。 */
    private Map<String, Box> layoutBoxes(LinkedHashMap<String, FlowNode> nodeMap, Map<String, Integer> layer, Map<String, String> idByKey)
    {
        Map<String, Box> boxes = new LinkedHashMap<String, Box>();
        TreeMap<Integer, List<Box>> byLayer = new TreeMap<Integer, List<Box>>();
        int maxLayer = 0;
        for (String key : nodeMap.keySet())
        {
            FlowNode node = nodeMap.get(key);
            Box box = new Box();
            box.key = key; box.type = node.type; box.text = node.text; box.id = idByKey.get(key);
            box.w = nodeWidth(node.type, node.text); box.h = nodeHeight(node.type);
            box.layer = layer.get(key);
            boxes.put(key, box);
            if (!byLayer.containsKey(box.layer)) byLayer.put(box.layer, new ArrayList<Box>());
            byLayer.get(box.layer).add(box);
            if (box.layer > maxLayer) maxLayer = box.layer;
        }
        int layerCount = maxLayer + 1;
        int rowGap = layerCount > 1 ? Math.max(80, Math.min(170, (9500 - TOP_MARGIN) / (layerCount - 1))) : 170;
        for (Map.Entry<Integer, List<Box>> entry : byLayer.entrySet())
        {
            List<Box> row = entry.getValue();
            int total = COL_GAP * (row.size() - 1);
            for (Box box : row) total += box.w;
            int cursor = CENTER_X - total / 2;
            int y = TOP_MARGIN + entry.getKey() * rowGap;
            for (Box box : row)
            {
                box.x = clamp(cursor + box.w / 2);
                box.y = y;
                cursor += box.w + COL_GAP;
            }
        }
        return boxes;
    }

    private int nodeWidth(String type, String text)
    {
        int len = text == null ? 0 : text.length();
        int width;
        if ("decision".equals(type)) width = 200 + len * 18;
        else if ("terminal".equals(type)) width = 120 + len * 12;
        else width = 160 + len * 16;
        return Math.max(120, Math.min(440, width));
    }

    private int nodeHeight(String type)
    {
        if ("decision".equals(type)) return 110;
        if ("terminal".equals(type)) return 56;
        return 64;
    }

    /**
     * 正交布线：
     * - 下行连接：源节点底部 → 层间横线 → 目标顶部（Z 形），不穿越中间节点；
     * - 同层连接：两端底部向下绕行；
     * - 回边（向上循环）：从源底部下行后走图形左侧通道再折回目标（判断框用左锚点）。
     */
    private void routeEdges(ArrayNode edgesOut, List<FlowEdge> validEdges, Map<String, Box> boxes)
    {
        int minLeft = Integer.MAX_VALUE;
        for (Box box : boxes.values()) minLeft = Math.min(minLeft, box.x - box.w / 2);

        // 判断框的多条正向分支分别走左/右锚点，避免“是/否”从同一底部端口分出（贴近课本画法）。
        Map<Integer, String> forwardSide = new HashMap<Integer, String>();
        Map<String, List<Integer>> forwardBySource = new LinkedHashMap<String, List<Integer>>();
        for (int i = 0; i < validEdges.size(); i++)
        {
            FlowEdge edge = validEdges.get(i);
            Box a = boxes.get(edge.from), b = boxes.get(edge.to);
            if (!"decision".equals(a.type) || b.layer <= a.layer) continue;
            if (!forwardBySource.containsKey(a.key)) forwardBySource.put(a.key, new ArrayList<Integer>());
            forwardBySource.get(a.key).add(i);
        }
        for (Map.Entry<String, List<Integer>> entry : forwardBySource.entrySet())
        {
            List<Integer> indexes = entry.getValue();
            if (indexes.size() < 2) continue;
            final Map<String, Box> boxRef = boxes;
            final List<FlowEdge> edgeRef = validEdges;
            indexes.sort((x, y) -> Integer.compare(boxRef.get(edgeRef.get(x).to).x, boxRef.get(edgeRef.get(y).to).x));
            if (indexes.size() == 2)
            {
                forwardSide.put(indexes.get(0), "left");
                forwardSide.put(indexes.get(1), "right");
            }
            else
            {
                forwardSide.put(indexes.get(0), "left");
                forwardSide.put(indexes.get(indexes.size() - 1), "right");
                for (int k = 1; k < indexes.size() - 1; k++) forwardSide.put(indexes.get(k), "bottom");
            }
        }

        int backIndex = 0;
        int edgeIndex = 0;
        for (int i = 0; i < validEdges.size(); i++)
        {
            FlowEdge edge = validEdges.get(i);
            Box a = boxes.get(edge.from), b = boxes.get(edge.to);
            String side = forwardSide.get(i);
            int sx, sy;
            String sourceAnchor;
            if ("left".equals(side)) { sx = clamp(a.x - a.w / 2); sy = clamp(a.y); sourceAnchor = a.id + "_left"; }
            else if ("right".equals(side)) { sx = clamp(a.x + a.w / 2); sy = clamp(a.y); sourceAnchor = a.id + "_right"; }
            else { sx = clamp(a.x); sy = clamp(a.y + a.h / 2); sourceAnchor = a.id + "_bottom"; }

            String targetAnchor;
            int ex, ey;
            List<int[]> points = new ArrayList<int[]>();
            points.add(new int[] { sx, sy });

            if (b.layer > a.layer)
            {
                ex = clamp(b.x); ey = clamp(b.y - b.h / 2);
                targetAnchor = b.id + "_top";
                int midY = (sy + ey) / 2;
                if (midY <= sy) midY = sy + 20;
                if (midY >= ey) midY = Math.max(sy + 20, ey - 20);
                if ("left".equals(side) || "right".equals(side))
                {
                    int stub = "left".equals(side) ? clamp(sx - 24) : clamp(sx + 24);
                    points.add(new int[] { stub, sy });
                    points.add(new int[] { stub, clamp(midY) });
                    points.add(new int[] { ex, clamp(midY) });
                }
                else
                {
                    points.add(new int[] { sx, clamp(midY) });
                    points.add(new int[] { ex, clamp(midY) });
                }
            }
            else if (b.layer == a.layer)
            {
                ex = clamp(b.x); ey = clamp(b.y + b.h / 2);
                targetAnchor = b.id + "_bottom";
                int belowY = clamp(Math.max(sy, ey) + 40);
                points.add(new int[] { sx, belowY });
                points.add(new int[] { ex, belowY });
            }
            else
            {
                backIndex++;
                int channelX = clamp(minLeft - 60 - backIndex * 24);
                int dropY = clamp(sy + 34);
                points.add(new int[] { sx, dropY });
                points.add(new int[] { channelX, dropY });
                if ("decision".equals(b.type))
                {
                    ex = clamp(b.x - b.w / 2); ey = clamp(b.y);
                    targetAnchor = b.id + "_left";
                    points.add(new int[] { channelX, ey });
                }
                else
                {
                    ex = clamp(b.x); ey = clamp(b.y + b.h / 2);
                    targetAnchor = b.id + "_bottom";
                    int belowY = clamp(ey + 34);
                    points.add(new int[] { channelX, belowY });
                    points.add(new int[] { ex, belowY });
                }
            }
            points.add(new int[] { ex, ey });

            ObjectNode e = edgesOut.addObject();
            e.put("id", "e" + (++edgeIndex));
            e.put("type", "polyline");
            e.put("sourceNodeId", a.id);
            e.put("targetNodeId", b.id);
            e.put("text", edge.label == null ? "" : edge.label);
            e.put("sourceAnchorId", sourceAnchor);
            e.put("targetAnchorId", targetAnchor);
            ObjectNode start = e.putObject("startPoint");
            start.put("x", points.get(0)[0]); start.put("y", points.get(0)[1]);
            ObjectNode end = e.putObject("endPoint");
            end.put("x", points.get(points.size() - 1)[0]); end.put("y", points.get(points.size() - 1)[1]);
            ArrayNode list = e.putArray("pointsList");
            for (int[] point : points)
            {
                ObjectNode p = list.addObject();
                p.put("x", point[0]); p.put("y", point[1]);
            }
        }
    }

    private int clamp(int value)
    {
        return Math.max(-COORD_LIMIT, Math.min(COORD_LIMIT, value));
    }
}
