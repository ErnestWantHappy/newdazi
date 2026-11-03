package com.ruoyi.business.service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.TeacherAiConfig;
import com.ruoyi.business.domain.dto.PracticalUploadTicket;
import com.ruoyi.business.domain.vo.FlowchartAiAnswerResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * 题库出题阶段：把一张流程图照片连同题目信息交给视觉模型，产出标准答案草稿。
 * 复用批改侧已有的教师 Key、端点白名单与 chatJson 视觉请求，本服务不写任何业务表，
 * 结果只作为“待老师核对”的草稿返回，正式写入仍走现有题目配置保存链路。
 */
@Service
public class FlowchartAiAnswerService
{
    private static final int MAX_TOKENS = 2000;
    private static final int MAX_TITLE_CHARS = 80;
    private static final int MAX_NODE_TEXT_CHARS = 200;

    @Autowired private QwenPracticalVisionGradingProvider provider;
    @Autowired private TeacherAiConfigService configs;
    @Autowired private FlowchartDocumentService documentService;
    @Autowired private FlowchartLayoutService layoutService;
    @Autowired private PracticalFilePolicyService filePolicy;
    @Autowired private ObjectMapper json;

    public FlowchartAiAnswerResult generate(MultipartFile file, String title, String gradeText,
            String semesterText, Integer lessonNum, Long teacherUserId)
    {
        PracticalUploadTicket ticket = filePolicy.inspect(file, "jpg,jpeg,png");
        if (!"IMAGE".equals(ticket.getFileKind())) throw new ServiceException("请上传流程图照片（jpg 或 png）");

        TeacherAiConfig config = configs.status(teacherUserId);
        if (config == null || !Boolean.TRUE.equals(config.getEnabled()))
            throw new ServiceException("请先在原 AI 设置中启用教师个人 AI 配置");
        String apiKey = configs.apiKey(config);

        List<String> warnings = new ArrayList<String>();
        File temp = null;
        try
        {
            temp = toJpegFile(file.getBytes());
            String content = provider.chatJson(config, apiKey, buildPrompt(title, gradeText, semesterText, lessonNum),
                    Arrays.asList(temp), MAX_TOKENS);
            ParsedGraph parsed = parseModelOutput(content, warnings);
            String layoutJson = layoutService.buildDocument(parsed.nodes, parsed.edges);
            String answerJson = documentService.normalizeDocument(layoutJson, "标准答案");
            FlowchartAiAnswerResult result = new FlowchartAiAnswerResult();
            result.setAnswerJson(answerJson);
            result.setWarnings(warnings);
            result.setNodeCount(parsed.nodes.size());
            return result;
        }
        catch (ServiceException e) { throw e; }
        catch (Exception e) { throw new ServiceException("AI 生成标准答案失败，请稍后重试或改用手动绘制"); }
        finally { if (temp != null) temp.delete(); }
    }

    /** 统一转成白底 JPEG：兼容 png 透明通道，同时收敛体积，避免 provider 的逐图大小上限。 */
    private File toJpegFile(byte[] bytes) throws Exception
    {
        BufferedImage source = ImageIO.read(new ByteArrayInputStream(bytes));
        if (source == null) throw new ServiceException("照片无法解析，请更换更清晰的图片");
        BufferedImage rgb = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = rgb.createGraphics();
        try
        {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, source.getWidth(), source.getHeight());
            graphics.drawImage(source, 0, 0, null);
        }
        finally { graphics.dispose(); }
        File temp = File.createTempFile("flowchart-ai-", ".jpg");
        ImageIO.write(rgb, "jpg", temp);
        return temp;
    }

    String buildPrompt(String title, String gradeText, String semesterText, Integer lessonNum)
    {
        StringBuilder context = new StringBuilder();
        String safeTitle = clamp(oneLine(title), MAX_TITLE_CHARS);
        if (StringUtils.isNotBlank(safeTitle)) context.append("操作题名称：").append(safeTitle).append("。");
        String grade = oneLine(gradeText);
        String semester = oneLine(semesterText);
        if (StringUtils.isNotBlank(grade) || StringUtils.isNotBlank(semester) || lessonNum != null)
        {
            context.append("适用学段：").append(StringUtils.isBlank(grade) ? "未指定" : grade);
            context.append(StringUtils.isBlank(semester) ? "" : (" " + semester));
            if (lessonNum != null && lessonNum > 0) context.append(" 第").append(lessonNum).append("课");
            context.append("（仅帮助理解专业术语，不得据此改写图中实际结构）。");
        }
        return "你是流程图出题助手。请只识别照片里流程图的结构与文字，不要输出任何坐标或像素位置。"
            + context
            + "节点类型只能四选一：开始和结束都是 terminal；处理步骤是 process；判断是 decision；输入或输出是 inputOutput。"
            + "每个节点给一个唯一 key（如 n1、n2）和简短中文文字；开始与结束各作为一个 terminal 节点。"
            + "每条连线给 from 和 to（都必须是已有节点的 key），判断分支的条件文字写入 label，其余 label 可为空。"
            + "只依据照片中真实可见的图形，不要臆造额外步骤；看不清的文字按最接近的内容填写。忽略图例、坐标、颜色、页眉页脚和说明性文字。"
            + "只输出一个合法 JSON 对象，禁止 Markdown、解释或代码围栏。格式："
            + "{\"nodes\":[{\"key\":\"n1\",\"type\":\"terminal\",\"text\":\"开始\"}],"
            + "\"edges\":[{\"from\":\"n1\",\"to\":\"n2\",\"label\":\"\"}]}";
    }

    /** 不信任模型输出：节点去重、类型归一、丢弃悬空连线，并把无法识别的内容转成给老师的提示。 */
    private ParsedGraph parseModelOutput(String content, List<String> warnings) throws Exception
    {
        JsonNode root = json.readTree(content);
        JsonNode nodesNode = root.path("nodes");
        if (!nodesNode.isArray() || nodesNode.size() == 0)
            throw new ServiceException("未能从照片识别出流程节点，请核对照片清晰度或改用手动绘制");
        List<FlowchartLayoutService.FlowNode> nodes = new ArrayList<FlowchartLayoutService.FlowNode>();
        java.util.Set<String> seenKeys = new java.util.HashSet<String>();
        int duplicate = 0;
        for (JsonNode node : nodesNode)
        {
            String key = oneLine(node.path("key").asText(""));
            if (StringUtils.isBlank(key)) continue;
            if (!seenKeys.add(key)) { duplicate++; continue; }
            String type = normalizeType(node.path("type").asText(""), warnings);
            String text = clamp(oneLine(node.path("text").asText("")), MAX_NODE_TEXT_CHARS);
            nodes.add(new FlowchartLayoutService.FlowNode(key, type, text));
        }
        if (duplicate > 0) warnings.add("识别到 " + duplicate + " 个重复节点标识，已自动合并");
        if (nodes.isEmpty()) throw new ServiceException("未能从照片识别出有效流程节点");

        List<FlowchartLayoutService.FlowEdge> edges = new ArrayList<FlowchartLayoutService.FlowEdge>();
        int dropped = 0;
        JsonNode edgesNode = root.path("edges");
        if (edgesNode.isArray())
        {
            for (JsonNode edge : edgesNode)
            {
                String from = oneLine(edge.path("from").asText(""));
                String to = oneLine(edge.path("to").asText(""));
                if (StringUtils.isBlank(from) || StringUtils.isBlank(to)) { dropped++; continue; }
                boolean known = false;
                for (FlowchartLayoutService.FlowNode node : nodes)
                    if (node.key.equals(from)) { known = true; break; }
                if (!known) { dropped++; continue; }
                known = false;
                for (FlowchartLayoutService.FlowNode node : nodes)
                    if (node.key.equals(to)) { known = true; break; }
                if (!known) { dropped++; continue; }
                edges.add(new FlowchartLayoutService.FlowEdge(from, to, oneLine(edge.path("label").asText(""))));
            }
        }
        if (dropped > 0) warnings.add("有 " + dropped + " 条连线指向无法确定的节点，已忽略，请老师核对连线");

        ParsedGraph graph = new ParsedGraph();
        graph.nodes = nodes;
        graph.edges = edges;
        return graph;
    }

    /** 把模型可能给出的 start/end/io 等别名归一到平台支持的四类，未知类型按处理框保守保留并提示。 */
    private String normalizeType(String raw, List<String> warnings)
    {
        String value = raw == null ? "" : raw.trim().toLowerCase(java.util.Locale.ROOT);
        if ("terminal".equals(value) || "start".equals(value) || "end".equals(value)) return "terminal";
        if ("decision".equals(value) || "diamond".equals(value)) return "decision";
        if ("inputoutput".equals(value) || "input".equals(value) || "output".equals(value)
                || "io".equals(value) || "parallelogram".equals(value) || "input-output".equals(value))
            return "inputOutput";
        if ("process".equals(value) || "rectangle".equals(value) || "box".equals(value)) return "process";
        warnings.add("有节点类型无法识别，已按处理框保留，请老师核对图形");
        return "process";
    }

    private String oneLine(String value)
    {
        if (value == null) return "";
        return value.replaceAll("[\\r\\n\\t]+", " ").trim();
    }

    private String clamp(String value, int max)
    {
        if (value == null) return "";
        return value.length() <= max ? value : value.substring(0, max);
    }

    private static class ParsedGraph
    {
        List<FlowchartLayoutService.FlowNode> nodes;
        List<FlowchartLayoutService.FlowEdge> edges;
    }
}
