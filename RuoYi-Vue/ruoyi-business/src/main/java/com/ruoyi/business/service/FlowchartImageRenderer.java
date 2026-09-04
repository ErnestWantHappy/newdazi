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
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.exception.ServiceException;

/** 将已通过服务端校验的流程图 JSON 渲染为 AI 可识别的 PNG。 */
@Service
public class FlowchartImageRenderer {
    private static final int NODE_W = 150;
    private static final int NODE_H = 64;
    private final ObjectMapper objectMapper;

    public FlowchartImageRenderer(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }

    public File render(String documentJson, File output) {
        try {
            JsonNode root = objectMapper.readTree(documentJson);
            JsonNode nodes = root.path("nodes");
            JsonNode edges = root.path("edges");
            if (!nodes.isArray() || nodes.size() > 200) throw new ServiceException("流程图节点数量无效");
            int width = 420, height = 260;
            for (JsonNode node : nodes) {
                width = Math.max(width, (int) Math.ceil(node.path("x").asDouble() + NODE_W + 40));
                height = Math.max(height, (int) Math.ceil(node.path("y").asDouble() + NODE_H + 40));
            }
            width = Math.min(width, 2200); height = Math.min(height, 1600);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE); g.fillRect(0, 0, width, height);
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
        int x1 = (int) Math.round(source.path("x").asDouble() + NODE_W / 2d);
        int y1 = (int) Math.round(source.path("y").asDouble() + NODE_H / 2d);
        int x2 = (int) Math.round(target.path("x").asDouble() + NODE_W / 2d);
        int y2 = (int) Math.round(target.path("y").asDouble() + NODE_H / 2d);
        g.setColor(new Color(90, 100, 115)); g.drawLine(x1, y1, x2, y2);
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int size = 9;
        int ax1 = (int) (x2 - size * Math.cos(angle - Math.PI / 6));
        int ay1 = (int) (y2 - size * Math.sin(angle - Math.PI / 6));
        int ax2 = (int) (x2 - size * Math.cos(angle + Math.PI / 6));
        int ay2 = (int) (y2 - size * Math.sin(angle + Math.PI / 6));
        Path2D arrow = new Path2D.Double(); arrow.moveTo(x2, y2); arrow.lineTo(ax1, ay1); arrow.lineTo(ax2, ay2); arrow.closePath();
        g.fill(arrow);
        String text = edge.path("text").asText("");
        if (!text.isEmpty()) { g.setColor(new Color(40, 45, 55)); g.drawString(text, (x1 + x2) / 2, (y1 + y2) / 2 - 4); }
    }

    private void drawNode(Graphics2D g, JsonNode node) {
        int x = (int) Math.round(node.path("x").asDouble());
        int y = (int) Math.round(node.path("y").asDouble());
        String type = node.path("type").asText("");
        g.setColor(new Color(245, 248, 252)); g.setStroke(new BasicStroke(2f));
        if ("terminal".equals(type)) g.fillRoundRect(x, y, NODE_W, NODE_H, 28, 28);
        else if ("decision".equals(type)) {
            Path2D diamond = new Path2D.Double(); diamond.moveTo(x + NODE_W / 2d, y); diamond.lineTo(x + NODE_W, y + NODE_H / 2d);
            diamond.lineTo(x + NODE_W / 2d, y + NODE_H); diamond.lineTo(x, y + NODE_H / 2d); diamond.closePath(); g.fill(diamond);
        } else if ("inputOutput".equals(type)) {
            Path2D shape = new Path2D.Double(); shape.moveTo(x + 18, y); shape.lineTo(x + NODE_W, y); shape.lineTo(x + NODE_W - 18, y + NODE_H); shape.lineTo(x, y + NODE_H); shape.closePath(); g.fill(shape);
        } else g.fillRect(x, y, NODE_W, NODE_H);
        g.setColor(new Color(70, 90, 115));
        if ("terminal".equals(type)) g.drawRoundRect(x, y, NODE_W, NODE_H, 28, 28);
        else if ("decision".equals(type)) {
            Path2D diamond = new Path2D.Double(); diamond.moveTo(x + NODE_W / 2d, y); diamond.lineTo(x + NODE_W, y + NODE_H / 2d);
            diamond.lineTo(x + NODE_W / 2d, y + NODE_H); diamond.lineTo(x, y + NODE_H / 2d); diamond.closePath(); g.draw(diamond);
        } else if ("inputOutput".equals(type)) {
            Path2D shape = new Path2D.Double(); shape.moveTo(x + 18, y); shape.lineTo(x + NODE_W, y); shape.lineTo(x + NODE_W - 18, y + NODE_H); shape.lineTo(x, y + NODE_H); shape.closePath(); g.draw(shape);
        } else g.drawRect(x, y, NODE_W, NODE_H);
        String text = node.path("text").asText("");
        FontMetrics fm = g.getFontMetrics(); int tx = x + (NODE_W - fm.stringWidth(text)) / 2;
        g.setColor(new Color(30, 35, 45)); g.drawString(text, tx, y + (NODE_H + fm.getAscent() - fm.getDescent()) / 2);
    }
}
