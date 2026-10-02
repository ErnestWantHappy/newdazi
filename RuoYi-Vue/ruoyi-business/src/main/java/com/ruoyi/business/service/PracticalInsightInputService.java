package com.ruoyi.business.service;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.*;
import com.ruoyi.business.domain.dto.PracticalInsightV3;
import com.ruoyi.business.domain.dto.PracticalInsightV3.*;
import com.ruoyi.business.domain.vo.PracticalSubmissionVo;
import com.ruoyi.business.mapper.*;
import com.ruoyi.business.util.AcademicYearUtils;
import com.ruoyi.business.utils.FileConversionUtils;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.SysDeptMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

@Service
public class PracticalInsightInputService
{
    @Autowired private TeacherPaperSubmissionService scope;
    @Autowired private PracticalClassInsightService legacy;
    @Autowired private BizStudentAnswerMapper answers;
    @Autowired private BizScoringDetailMapper details;
    @Autowired private PracticalRubricSnapshotMapper snapshots;
    @Autowired private PracticalArtifactMapper artifacts;
    @Autowired private PracticalInsightMapper mapper;
    @Autowired private PracticalAiReferenceAnswerService references;
    @Autowired private TeacherAiConfigService configs;
    @Autowired private AiModelPricingService prices;
    @Autowired private SysDeptMapper departments;
    @Autowired private PracticalPageRenderer renderer;
    @Autowired private ObjectMapper json;

    @Transactional(readOnly=true, isolation=Isolation.REPEATABLE_READ)
    public Input build(Request request) throws Exception
    {
        if (request == null || request.subject == null || request.subject.trim().isEmpty()
                || request.subject.length() > 40 || request.lessonMinutes < 10 || request.lessonMinutes > 120)
            throw new ServiceException("请确认学科和10至120分钟的课时时长");
        Input input = new Input(); input.scope = request; input.teacherId = SecurityUtils.getUserId();
        input.questionContent = scope.requireQuestion(request).getQuestionContent(); input.deptId = scope.requireScope(request);
        if (request.grade != null && (request.grade < 1 || request.grade > 12)) throw new ServiceException("年级必须为1至12");
        Integer grade = request.grade;
        if (grade == null && request.assessmentDate != null && !request.assessmentDate.isEmpty())
        {
            SysDept dept = departments.selectDeptById(input.deptId);
            if (dept != null && dept.getSchoolType() != null)
                grade = AcademicYearUtils.resolveAbsoluteGrade(Integer.parseInt(request.entryYear), dept.getSchoolType(), LocalDate.parse(request.assessmentDate));
        }
        if (grade == null || grade < 1 || grade > 12) throw new ServiceException("请确认测评时实际年级，不能用入学年份代替年级");
        input.gradeLabel = grade + "年级" + (request.grade == null ? "（按测评日期推算）" : "（教师确认）");
        TeacherAiConfig config = configs.status(input.teacherId);
        if (config == null || !Boolean.TRUE.equals(config.getEnabled())) throw new ServiceException("请启用教师个人AI配置");
        input.provider = config.getProviderCode(); input.model = config.getModelName();
        AiModelPrice price = prices.require(config.getProviderCode(), input.model);
        if (price.getInputPricePerThousand() == null || price.getOutputPricePerThousand() == null)
            throw new ServiceException("所选模型单价待确认，暂不能计算学情诊断费用上限");
        input.inputPrice = price.getInputPricePerThousand(); input.outputPrice = price.getOutputPricePerThousand();
        input.materials = materials(input);
        List<Object> materialHashes = new ArrayList<>();
        for (PracticalQuestionMaterial m : input.materials)
            materialHashes.add(Arrays.asList(m.getMaterialType(), m.getResourcePath(), fileHash(resource(m.getResourcePath()))));
        input.paperFingerprint = hash(Arrays.asList(PracticalInsightV3.VERSION, input.teacherId, request.lessonId,
                request.questionId, request.subject, input.gradeLabel, input.questionContent,
                input.provider, input.model, materialHashes));
        Set<Long> roster = new TreeSet<>(); scope.roster(request).forEach(s -> roster.add(s.getStudentId())); input.total = roster.size();
        List<PracticalSubmissionVo> rows = answers.selectPracticalSubmissions(request.lessonId, request.questionId,
                request.classCode, request.entryYear, input.deptId);
        Map<String, Object> sourceSlots = new TreeMap<>();
        for (PracticalSubmissionVo row : rows)
        {
            if (!roster.contains(row.getStudentId()) || !PracticalClassInsightService.isConfirmed(row)) continue;
            if (row.getPracticalVersionId() != null)
            {
                PracticalArtifact artifact = artifacts.selectByContext("LESSON", request.lessonId, row.getStudentId(), request.questionId);
                if (artifact == null || !Objects.equals(artifact.getCurrentVersionId(), row.getPracticalVersionId())) continue;
            }
            Student s = new Student(); s.studentId = row.getStudentId(); s.answerId = row.getAnswerId();
            s.versionId = row.getPracticalVersionId(); s.name = row.getStudentName(); s.number = row.getStudentNo();
            s.score = row.getScore(); s.maxScore = row.getMaxScore(); s.scoringDetails = details.selectDetailsByAnswerId(s.answerId);
            PracticalRubricSnapshot rubric = s.versionId == null ? null : snapshots.selectByVersionId(s.versionId);
            if (rubric != null) { s.maxScore = rubric.getQuestionScore(); s.rubricSnapshotId = rubric.getSnapshotId(); }
            List<Object> attachments = new ArrayList<>();
            boolean incompletePages = false;
            if (s.versionId != null)
            {
                for (PracticalAttachment a : artifacts.selectAttachmentsByVersion(s.versionId))
                {
                    attachments.add(Arrays.asList(a.getAttachmentId(), a.getSha256(), a.getNormalizedStatus(), a.getNormalizedPagesJson()));
                    if ("success".equalsIgnoreCase(a.getNormalizedStatus()) && a.getNormalizedPagesJson() != null)
                        s.pages.addAll(json.readValue(a.getNormalizedPagesJson(), new TypeReference<List<String>>() {}));
                    else incompletePages = true;
                }
                String previous = mapper.recognition(s.answerId, s.versionId);
                if (previous != null && json.readTree(previous).has("paperRecognition"))
                    s.recognitionJson = json.readTree(previous).get("paperRecognition").toString();
            }
            // 旧附件无规范页图时保留为证据不足，不把未知作答猜成错误。
            if (incompletePages) s.pages.clear();
            if (rubric != null && s.recognitionJson != null)
            {
                com.fasterxml.jackson.databind.JsonNode rubricItems = json.readTree(rubric.getScoringItemsJson());
                for (com.fasterxml.jackson.databind.JsonNode item : json.readTree(s.recognitionJson).path("items"))
                    for (com.fasterxml.jackson.databind.JsonNode part : item.path("parts"))
                    {
                        String key = s.rubricSnapshotId + ":" + item.path("rubricItemId").asText() + ":" + part.path("label").asText();
                        Object scoringItem = null;
                        for (com.fasterxml.jackson.databind.JsonNode candidate : rubricItems)
                            if (candidate.path("itemId").asLong() == item.path("rubricItemId").asLong()) scoringItem = candidate;
                        sourceSlots.put(key, PracticalInsightEngine.map("key", key, "scoringItem", scoringItem, "label", part.path("label").asText()));
                    }
            }
            s.fingerprint = hash(Arrays.asList(input.paperFingerprint, s.studentId, s.answerId, s.versionId,
                    s.score, s.maxScore, s.scoringDetails, s.pages, attachments, s.recognitionJson));
            input.students.add(s);
        }
        input.students.sort(Comparator.comparing(s -> s.studentId));
        input.sourceSlots.addAll(sourceSlots.values());
        input.paperFingerprint = hash(Arrays.asList(input.paperFingerprint, input.sourceSlots));
        for (Student s : input.students) s.fingerprint = hash(Arrays.asList(input.paperFingerprint, s.fingerprint, s.rubricSnapshotId));
        input.scoringStats = (Map<String, Object>) legacy.snapshot(request).get("stats");
        input.fingerprint = hash(Arrays.asList(input.paperFingerprint, request, roster, input.students, input.scoringStats));
        return input;
    }
    private List<PracticalQuestionMaterial> materials(Input input)
    {
        List<PracticalQuestionMaterial> result = new ArrayList<>();
        TeacherPracticalReferenceAnswer ref = references.current(input.teacherId, input.deptId, input.scope.lessonId, input.scope.questionId);
        for (PracticalQuestionMaterial m : artifacts.selectMaterialsByQuestion(input.scope.questionId))
            if ("STARTER".equals(m.getMaterialType()) || (ref == null && "REFERENCE".equals(m.getMaterialType()))) result.add(m);
        if (ref != null)
        {
            PracticalQuestionMaterial m = new PracticalQuestionMaterial(); m.setMaterialId(ref.getReferenceId());
            m.setMaterialType("REFERENCE"); m.setResourcePath(ref.getResourcePath()); m.setFileExtension(ref.getFileExtension());
            m.setOriginalFileName(ref.getOriginalFileName()); m.setSha256(ref.getSha256()); result.add(m);
        }
        if (result.stream().noneMatch(m -> "REFERENCE".equals(m.getMaterialType()))) throw new ServiceException("请先上传当前试卷参考答案");
        return result;
    }
    public List<File> materialPages(Input input, List<String> labels) throws Exception
    {
        List<File> files = new ArrayList<>(); int index = 0;
        for (PracticalQuestionMaterial material : input.materials)
        {
            File source = resource(material.getResourcePath());
            String extension = material.getFileExtension();
            if (extension == null) extension = source.getName().substring(source.getName().lastIndexOf('.') + 1);
            extension = extension.toLowerCase(Locale.ROOT);
            String kind = "DOCUMENT";
            if (Arrays.asList("png", "jpg", "jpeg").contains(extension)) kind = "IMAGE";
            else if (!"pdf".equals(extension))
            {
                if (!Arrays.asList("doc", "docx", "xls", "xlsx", "ppt", "pptx").contains(extension))
                    throw new ServiceException("试卷包含暂不支持识别的材料，请提供PDF或图片");
                File directory = new File(source.getParentFile(), "insight-pdf");
                if (!directory.exists() && !directory.mkdirs()) throw new ServiceException("材料转换目录创建失败");
                source = new File(FileConversionUtils.convertOfficeToPdfWithLibreOffice(source.getAbsolutePath(), directory.getAbsolutePath()));
            }
            List<String> pages = renderer.renderForOwner("insight-" + input.paperFingerprint.substring(0, 16) + "-" + index++, kind, source);
            for (int i = 0; i < pages.size(); i++)
            {
                files.add(resource(pages.get(i)));
                labels.add(("REFERENCE".equals(material.getMaterialType()) ? "参考答案" : "试卷起始材料") + "第" + (i + 1) + "页；全局页码" + files.size());
            }
        }
        return files;
    }
    public static File resource(String path) throws IOException
    {
        if (path == null || !path.startsWith("/profile/upload/")) throw new ServiceException("诊断资源路径无效");
        Path root = Paths.get(RuoYiConfig.getProfile()).toRealPath();
        Path file = root.resolve(path.substring("/profile/".length())).normalize().toRealPath();
        if (!file.startsWith(root) || !Files.isRegularFile(file)) throw new ServiceException("诊断资源不存在或越界");
        return file.toFile();
    }
    private String fileHash(File file) throws IOException
    { try (InputStream stream = new FileInputStream(file)) { return DigestUtils.sha256Hex(stream); } }
    public String hash(Object value) throws Exception { return DigestUtils.sha256Hex(json.writeValueAsBytes(value)); }
}
