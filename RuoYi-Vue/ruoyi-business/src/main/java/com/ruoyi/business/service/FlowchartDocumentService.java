package com.ruoyi.business.service;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruoyi.common.exception.ServiceException;

/**
 * 画程文档的服务端安全边界。
 * 浏览器图数据不能直接入库，这里只保留首期明确支持的字段，避免脚本、外链和超大图进入业务数据。
 */
@Service
public class FlowchartDocumentService {
    public static final String SCHEMA_VERSION = "1.0";
    public static final String EMPTY_DOCUMENT = "{\"schemaVersion\":\"1.0\",\"nodes\":[],\"edges\":[]}";
    public static final String DEFAULT_PERMISSIONS = "{\"allowAddNode\":true,\"allowDeleteNode\":true,\"allowEditText\":true,\"allowAddEdge\":true,\"allowDeleteEdge\":true,\"allowMoveNode\":true}";

    private static final int MAX_DOCUMENT_BYTES = 512 * 1024;
    private static final int MAX_NODES = 200;
    private static final int MAX_EDGES = 400;
    private static final int MAX_TEXT_LENGTH = 200;
    private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9_-]{1,64}");
    private static final Set<String> NODE_TYPES = new HashSet<String>();
    static {
        NODE_TYPES.add("terminal");
        NODE_TYPES.add("process");
        NODE_TYPES.add("decision");
        NODE_TYPES.add("inputOutput");
    }

    private final ObjectMapper objectMapper;

    public FlowchartDocumentService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String normalizeDocument(String json) {
        if (json == null || json.trim().isEmpty()) json = EMPTY_DOCUMENT;
        if (json.getBytes(StandardCharsets.UTF_8).length > MAX_DOCUMENT_BYTES) {
            throw new ServiceException("流程图内容超过 512KB，请减少节点或文字");
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root == null || !root.isObject()) throw new ServiceException("流程图数据格式不正确");
            JsonNode nodes = root.get("nodes");
            JsonNode edges = root.get("edges");
            if (nodes == null || !nodes.isArray() || edges == null || !edges.isArray()) {
                throw new ServiceException("流程图必须包含节点和连线");
            }
            if (nodes.size() > MAX_NODES || edges.size() > MAX_EDGES) {
                throw new ServiceException("流程图最多 200 个节点和 400 条连线");
            }

            ObjectNode normalized = objectMapper.createObjectNode();
            normalized.put("schemaVersion", SCHEMA_VERSION);
            ArrayNode safeNodes = normalized.putArray("nodes");
            Set<String> nodeIds = new HashSet<String>();
            for (JsonNode node : nodes) {
                String id = requiredId(node, "id", "节点");
                if (!nodeIds.add(id)) throw new ServiceException("流程图存在重复节点ID");
                String type = text(node.get("type"));
                if (!NODE_TYPES.contains(type)) throw new ServiceException("流程图包含不支持的节点类型");
                ObjectNode safe = safeNodes.addObject();
                safe.put("id", id);
                safe.put("type", type);
                safe.put("x", coordinate(node.get("x")));
                safe.put("y", coordinate(node.get("y")));
                safe.put("text", safeText(node.get("text")));
                ObjectNode properties = safe.putObject("properties");
                JsonNode sourceProperties = node.get("properties");
                properties.put("locked", booleanValue(sourceProperties, "locked", false));
                properties.put("textEditable", booleanValue(sourceProperties, "textEditable", true));
            }

            ArrayNode safeEdges = normalized.putArray("edges");
            Set<String> edgeIds = new HashSet<String>();
            for (JsonNode edge : edges) {
                String id = requiredId(edge, "id", "连线");
                if (!edgeIds.add(id)) throw new ServiceException("流程图存在重复连线ID");
                String source = requiredId(edge, "sourceNodeId", "连线起点");
                String target = requiredId(edge, "targetNodeId", "连线终点");
                if (!nodeIds.contains(source) || !nodeIds.contains(target)) {
                    throw new ServiceException("流程图存在未连接到节点的连线");
                }
                ObjectNode safe = safeEdges.addObject();
                safe.put("id", id);
                safe.put("type", "polyline");
                safe.put("sourceNodeId", source);
                safe.put("targetNodeId", target);
                safe.put("text", safeText(edge.get("text")));
                ObjectNode properties = safe.putObject("properties");
                JsonNode sourceProperties = edge.get("properties");
                properties.put("locked", booleanValue(sourceProperties, "locked", false));
                properties.put("textEditable", booleanValue(sourceProperties, "textEditable", true));
            }
            String result = objectMapper.writeValueAsString(normalized);
            if (result.getBytes(StandardCharsets.UTF_8).length > MAX_DOCUMENT_BYTES) {
                throw new ServiceException("流程图规范化后仍超过 512KB");
            }
            return result;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("流程图 JSON 无法解析");
        }
    }

    public String normalizePermissions(String json) {
        try {
            JsonNode source = json == null || json.trim().isEmpty()
                    ? objectMapper.readTree(DEFAULT_PERMISSIONS) : objectMapper.readTree(json);
            if (source == null || !source.isObject()) throw new ServiceException("画程操作权限格式不正确");
            ObjectNode safe = objectMapper.createObjectNode();
            safe.put("allowAddNode", booleanValue(source, "allowAddNode", true));
            safe.put("allowDeleteNode", booleanValue(source, "allowDeleteNode", true));
            safe.put("allowEditText", booleanValue(source, "allowEditText", true));
            safe.put("allowAddEdge", booleanValue(source, "allowAddEdge", true));
            safe.put("allowDeleteEdge", booleanValue(source, "allowDeleteEdge", true));
            safe.put("allowMoveNode", booleanValue(source, "allowMoveNode", true));
            return objectMapper.writeValueAsString(safe);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("画程操作权限 JSON 无法解析");
        }
    }

    public JsonNode readDocument(String json) {
        try {
            return objectMapper.readTree(normalizeDocument(json));
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("流程图数据无法读取");
        }
    }

    public String normalizeText(String value) {
        if (value == null) return "";
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC).toLowerCase();
        normalized = normalized.replaceAll("[\\s\\p{Punct}，。！？；：、“”‘’（）【】《》]+", "");
        return normalized.trim();
    }

    private String requiredId(JsonNode node, String field, String label) {
        String value = text(node == null ? null : node.get(field));
        if (!SAFE_ID.matcher(value).matches()) throw new ServiceException(label + "ID不合法");
        return value;
    }

    private double coordinate(JsonNode node) {
        if (node == null || !node.isNumber()) throw new ServiceException("节点坐标格式不正确");
        double value = node.asDouble();
        if (!Double.isFinite(value) || value < -10000 || value > 10000) {
            throw new ServiceException("节点坐标超出允许范围");
        }
        return Math.round(value * 100.0d) / 100.0d;
    }

    private String safeText(JsonNode source) {
        if (source == null || source.isNull()) return "";
        String value;
        if (source.isObject() && source.has("value")) value = source.get("value").asText("");
        else value = source.asText("");
        value = value.replaceAll("[\\u0000-\\u0008\\u000B\\u000C\\u000E-\\u001F]", "").trim();
        if (value.length() > MAX_TEXT_LENGTH) throw new ServiceException("节点或连线文字最多 200 个字符");
        if (value.contains("<") || value.contains(">") || value.matches("(?is).*javascript\\s*:.*")) {
            throw new ServiceException("流程图文字不能包含 HTML 或脚本内容");
        }
        return value;
    }

    private boolean booleanValue(JsonNode object, String field, boolean defaultValue) {
        if (object == null || !object.isObject() || !object.has(field)) return defaultValue;
        return object.get(field).asBoolean(defaultValue);
    }

    private String text(JsonNode node) { return node == null || node.isNull() ? "" : node.asText("").trim(); }
}
