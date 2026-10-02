package com.ruoyi.business.service;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.awt.geom.Point2D;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.exception.ServiceException;

/** 将已通过服务端校验的流程图 JSON 渲染为 AI 可识别的 JPEG。 */
@Service
public class FlowchartImageRenderer {
    private static final int NODE_W = 148;
    private static final int NODE_H = 64;
    private final ObjectMapper objectMapper;

    public FlowchartImageRenderer(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }

    public File render(String documentJson, File output) {
        try {
            JsonNode root = objectMapper.readTree(documentJson);
            JsonNode nodes = root.path("nodes");
            JsonNode edges = root.path("edges");
            if (!nodes.isArray() || nodes.size() > 200) throw new ServiceException("流程图节点数量无效");
            double minX = 0, minY = 0, maxX = 340, maxY = 180;
            for (JsonNode node : nodes) {
                minX = Math.min(minX, node.path("x").asDouble() - nodeWidth(node) / 2d - 8);
                minY = Math.min(minY, node.path("y").asDouble() - nodeHeight(node) / 2d - 8);
                maxX = Math.max(maxX, node.path("x").asDouble() + nodeWidth(node) / 2d + 8);
                maxY = Math.max(maxY, node.path("y").asDouble() + nodeHeight(node) / 2d + 8);
            }
            for (JsonNode edge : edges) {
                List<JsonNode> positions = new ArrayList<JsonNode>();
                edge.path("pointsList").forEach(positions::add);
                positions.add(edge.path("startPoint")); positions.add(edge.path("endPoint"));
                positions.add(edge.path("textPosition"));
                for (JsonNode p : positions) {
                    if (!p.has("x") || !p.has("y")) continue;
                    double pad = p == edge.path("textPosition") ? edge.path("text").asText("").length() * 8 + 16 : 12;
                    minX = Math.min(minX, p.path("x").asDouble() - pad);
                    maxX = Math.max(maxX, p.path("x").asDouble() + pad);
                    minY = Math.min(minY, p.path("y").asDouble() - 20);
                    maxY = Math.max(maxY, p.path("y").asDouble() + 20);
                }
            }
            // 平移和等比缩放整幅图，负坐标及伸出节点范围的折线也不能被裁掉。
            double scale = Math.min(1d, Math.min(2200d / (maxX - minX + 80), 1600d / (maxY - minY + 80)));
            int width = (int) Math.ceil((maxX - minX + 80) * scale);
            int height = (int) Math.ceil((maxY - minY + 80) * scale);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE); g.fillRect(0, 0, width, height);
            g.scale(scale, scale); g.translate(40 - minX, 40 - minY);
            g.setStroke(new BasicStroke(2f));
            Map<String, JsonNode> byId = new HashMap<String, JsonNode>();
            for (JsonNode node : nodes) byId.put(node.path("id").asText(), node);
            g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
            for (JsonNode edge : edges) drawEdge(g, edge, byId);
            for (JsonNode node : nodes) drawNode(g, node);
            g.dispose();
            File parent = output.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) throw new ServiceException("流程图图片目录创建失败");
            if (!ImageIO.write(image, "jpg", output)) throw new ServiceException("流程图图片生成失败");
            return output;
        } catch (ServiceException e) { throw e; }
        catch (Exception e) { throw new ServiceException("流程图图片生成失败"); }
    }

    private void drawEdge(Graphics2D g, JsonNode edge, Map<String, JsonNode> byId) {
        JsonNode source = byId.get(edge.path("sourceNodeId").asText());
        JsonNode target = byId.get(edge.path("targetNodeId").asText());
        if (source == null || target == null) return;
        List<Point2D.Double> points = new ArrayList<Point2D.Double>();
        for (JsonNode point : edge.path("pointsList")) points.add(point(point));
        if (points.size() < 2) {
            // 历史文档没有路径，只能使用端点或默认锚点降级，不能声称恢复了原折线。
            Point2D.Double start = edge.has("startPoint") ? point(edge.get("startPoint"))
                    : new Point2D.Double(source.path("x").asDouble(), source.path("y").asDouble() + nodeHeight(source) / 2d);
            Point2D.Double end = edge.has("endPoint") ? point(edge.get("endPoint"))
                    : new Point2D.Double(target.path("x").asDouble(), target.path("y").asDouble() - nodeHeight(target) / 2d);
            points.clear(); points.add(start);
            points.add(new Point2D.Double(start.x, (start.y + end.y) / 2));
            points.add(new Point2D.Double(end.x, (start.y + end.y) / 2)); points.add(end);
        }
        Path2D path = new Path2D.Double(); path.moveTo(points.get(0).x, points.get(0).y);
        for (int i = 1; i < points.size(); i++) path.lineTo(points.get(i).x, points.get(i).y);
        g.setColor(new Color(90, 100, 115)); g.draw(path);
        Point2D.Double end = points.get(points.size() - 1), previous = points.get(0);
        for (int i = points.size() - 2; i >= 0; i--) {
            if (points.get(i).distance(end) > 0) { previous = points.get(i); break; }
        }
        double x1 = previous.x, y1 = previous.y, x2 = end.x, y2 = end.y;
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int size = 9;
        int ax1 = (int) (x2 - size * Math.cos(angle - Math.PI / 6));
        int ay1 = (int) (y2 - size * Math.sin(angle - Math.PI / 6));
        int ax2 = (int) (x2 - size * Math.cos(angle + Math.PI / 6));
        int ay2 = (int) (y2 - size * Math.sin(angle + Math.PI / 6));
        Path2D arrow = new Path2D.Double(); arrow.moveTo(x2, y2); arrow.lineTo(ax1, ay1); arrow.lineTo(ax2, ay2); arrow.closePath();
        g.fill(arrow);
        String text = edge.path("text").asText("");
        if (!text.isEmpty()) {
            Point2D.Double label = edge.has("textPosition") ? point(edge.get("textPosition")) : points.get(points.size() / 2);
            g.setColor(new Color(40, 45, 55));
            g.drawString(text, (float) (label.x - g.getFontMetrics().stringWidth(text) / 2d), (float) (label.y + 5));
        }
    }

    private Point2D.Double point(JsonNode point) {
        return new Point2D.Double(point.path("x").asDouble(), point.path("y").asDouble());
    }

    private int nodeColumns(JsonNode node) {
        String type = node.path("type").asText();
        int longest = 0;
        for (String row : node.path("text").asText("").split("\\r\\n|\\r|\\n", -1)) {
            longest = Math.max(longest, row.codePointCount(0, row.length()));
        }
        int minimum = "decision".equals(type) ? 4 : "inputOutput".equals(type) ? 6 : 7;
        return Math.max(minimum, Math.min("decision".equals(type) ? 10 : 18, (int) Math.ceil(Math.sqrt(longest) * 1.3)));
    }

    private int nodeWidth(JsonNode node) {
        int saved = node.path("properties").path("width").asInt(0);
        if (saved >= 80 && saved <= 1200) return saved;
        String type = node.path("type").asText();
        int textWidth = nodeColumns(node) * 15;
        return "decision".equals(type) ? Math.max(164, textWidth * 2 + 20)
                : Math.max("inputOutput".equals(type) ? 140 : NODE_W, textWidth + 36);
    }

    private int nodeHeight(JsonNode node) {
        int saved = node.path("properties").path("height").asInt(0);
        if (saved >= 40 && saved <= 800) return saved;
        String type = node.path("type").asText();
        int minimum = "decision".equals(type) ? 96 : "inputOutput".equals(type) ? 60 : NODE_H;
        return Math.max(minimum, (nodeLines(node).size() * 20 + 16) * ("decision".equals(type) ? 2 : 1));
    }

    /** 与浏览器使用相同的全角字宽和中心文字区，避免 AI 图片中的文字越过图形。 */
    private List<String> nodeLines(JsonNode node) {
        String type = node.path("type").asText();
        int columns = nodeColumns(node);
        List<String> lines = new ArrayList<>();
        for (String row : node.path("text").asText("").split("\\r\\n|\\r|\\n", -1)) {
            int[] chars = row.codePoints().toArray();
            if (chars.length == 0) lines.add("");
            for (int i = 0; i < chars.length; i += columns) {
                lines.add(new String(chars, i, Math.min(columns, chars.length - i)));
            }
        }
        return lines;
    }

    private void drawNode(Graphics2D g, JsonNode node) {
        String type = node.path("type").asText("");
        int NODE_W = nodeWidth(node);
        int NODE_H = nodeHeight(node);
        int x = (int) Math.round(node.path("x").asDouble() - NODE_W / 2d);
        int y = (int) Math.round(node.path("y").asDouble() - NODE_H / 2d);
        g.setColor(new Color(245, 248, 252)); g.setStroke(new BasicStroke(2f));
        if ("terminal".equals(type)) g.fillRoundRect(x, y, NODE_W, NODE_H, 64, 64);
        else if ("decision".equals(type)) {
            Path2D diamond = new Path2D.Double(); diamond.moveTo(x + NODE_W / 2d, y); diamond.lineTo(x + NODE_W, y + NODE_H / 2d);
            diamond.lineTo(x + NODE_W / 2d, y + NODE_H); diamond.lineTo(x, y + NODE_H / 2d); diamond.closePath(); g.fill(diamond);
        } else if ("inputOutput".equals(type)) {
            Path2D shape = new Path2D.Double(); shape.moveTo(x + 12, y); shape.lineTo(x + NODE_W, y); shape.lineTo(x + NODE_W - 12, y + NODE_H); shape.lineTo(x, y + NODE_H); shape.closePath(); g.fill(shape);
        } else g.fillRect(x, y, NODE_W, NODE_H);
        g.setColor(new Color(70, 90, 115));
        if ("terminal".equals(type)) g.drawRoundRect(x, y, NODE_W, NODE_H, 64, 64);
        else if ("decision".equals(type)) {
            Path2D diamond = new Path2D.Double(); diamond.moveTo(x + NODE_W / 2d, y); diamond.lineTo(x + NODE_W, y + NODE_H / 2d);
            diamond.lineTo(x + NODE_W / 2d, y + NODE_H); diamond.lineTo(x, y + NODE_H / 2d); diamond.closePath(); g.draw(diamond);
        } else if ("inputOutput".equals(type)) {
            Path2D shape = new Path2D.Double(); shape.moveTo(x + 12, y); shape.lineTo(x + NODE_W, y); shape.lineTo(x + NODE_W - 12, y + NODE_H); shape.lineTo(x, y + NODE_H); shape.closePath(); g.draw(shape);
        } else g.drawRect(x, y, NODE_W, NODE_H);
        Font priorFont = g.getFont();
        g.setFont(priorFont.deriveFont(15f));
        List<String> lines = nodeLines(node);
        FontMetrics fm = g.getFontMetrics();
        g.setColor(new Color(30, 35, 45));
        int baseline = y + (NODE_H - lines.size() * 20) / 2 + (20 + fm.getAscent() - fm.getDescent()) / 2;
        for (String line : lines) {
            g.drawString(line, x + (NODE_W - fm.stringWidth(line)) / 2, baseline);
            baseline += 20;
        }
        g.setFont(priorFont);
    }
}
