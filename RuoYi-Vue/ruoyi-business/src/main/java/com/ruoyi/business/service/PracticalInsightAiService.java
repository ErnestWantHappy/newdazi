package com.ruoyi.business.service;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruoyi.business.domain.TeacherAiConfig;
import com.ruoyi.business.domain.dto.PracticalInsightV3.*;
import com.ruoyi.business.mapper.PracticalInsightMapper;
import com.ruoyi.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PracticalInsightAiService
{
    private static final Logger log = LoggerFactory.getLogger(PracticalInsightAiService.class);
    @Autowired private TeacherAiConfigService configs;
    @Autowired private QwenPracticalVisionGradingProvider provider;
    @Autowired private PracticalInsightMapper mapper;
    @Autowired private ObjectMapper json;
    @Value("${practical-insight.max-cost-yuan:20}") private BigDecimal maxCostYuan;

    public static class BudgetExceeded extends RuntimeException
    {
        public BudgetExceeded() { super("已达到诊断费用软上限，保留已完成结果；请联系管理员核对费用后调整上限"); }
    }

    static void requireBudget(Job job, Input input, BigDecimal limit)
    {
        if (limit == null || limit.signum() <= 0 || input.inputPrice == null || input.outputPrice == null)
            throw new BudgetExceeded();
        BigDecimal cost = input.inputPrice.multiply(BigDecimal.valueOf(job.promptTokens))
                .add(input.outputPrice.multiply(BigDecimal.valueOf(job.completionTokens)))
                .divide(BigDecimal.valueOf(1000));
        // 请求前检查累计消耗；在途调用及超时账单不受此软上限保证。
        if (cost.compareTo(limit) >= 0) throw new BudgetExceeded();
    }

    public Paper paper(Job job, Input input, List<File> files, List<String> labels) throws Exception
    {
        String prompt = "先分析完整试卷考查什么。学科=" + input.scope.subject + "，测评年级=" + input.gradeLabel
                + "。读取所有起始材料和参考答案，拆分真实题目/子题。不要把评分项、题号、答案是否正确当知识点。"
                + "知识点具体到可教学，例如Excel文件读取、DataFrame条件筛选、groupby分组聚合，仅在题目确实考查时使用。"
                + "一题可考多个知识点，同一知识点可关联多题。无依据不编造；材料不全在limitations记录。"
                + "summary限80字。完整保留选择题全部选项（含代码、表格），代码上下文保留缩进、变量和数据表，不能只抄正确选项。"
                + "每题parts列出全部独立作答位（选择题也有一个part），id稳定，保留各空题意及参考答案。"
                + "逐空核对参考答案与完整代码、前后题数据列/变量是否一致，parts.referenceIssue必须返回字符串：无冲突为空，有冲突说明具体矛盾、依据与教师核对问题，保留原参考不能擅改。特别注意连续筛选是否误用原始df而非上步dfs、聚合后列名是否沿用原列，图标题不能当列名。此核对不依赖学生是否作答。"
                + "contextComplete仅在题干、全部选项、代码/数据上下文和参考齐全可读时为true，否则false并记录缺失项，禁止补造。"
                + "每知识点可定义0至6个有本卷依据的具体错误模式errorPatterns，同一模式跨题必须是同一操作错误，不能只命名概念错误/语法错误；只是待核对目录，不代表学生已犯错。"
                + "输出{summary:字符串,questions:[{id,number,stem,type:CHOICE或OTHER,optionCount:实际选项数,options:[带标签的完整选项],codeContext:完整代码及数据上下文,contextComplete:布尔,parts:[{id,prompt,referenceAnswer,referenceIssue}],referenceAnswer,sourcePages:[全局页码]}],"
                + "knowledgePoints:[{id,name,core:布尔,questionIds:[题目id],rationale:题目考查该知识的依据,errorPatterns:[{id,name,questionIds:[适用题目id]}]}],limitations:[字符串]}。"
                + "每个part另外必须返回referenceStatus=CLEAR/DISPUTED/UNKNOWN之一，明确无争议为CLEAR、确有矛盾为DISPUTED、无法确认参考为UNKNOWN。referenceIssue只是说明，不能代替状态。"
                + "每个part返回sourceKeys数组，把下列既有评分作答位key对应到试卷真实作答位；只能选给定key，不能按数组顺序猜题号。多个标签指向同一空时可列多个key；不能确认对应关系则留空。评分项综合多空不表示每空都有分数。"
                + "既有作答位索引：" + json.writeValueAsString(input.sourceSlots)
                + "必须使用双引号JSON。平台题干：" + input.questionContent;
        Paper result = call(job, input, prompt, files, labels, 10000, Paper.class);
        PracticalInsightEngine.validatePaper(result, files.size());
        Set<String> allowed = new HashSet<>(), seen = new HashSet<>();
        for (Object slot : input.sourceSlots) allowed.add(json.valueToTree(slot).path("key").asText());
        for (Question q : result.questions) for (Part p : q.parts)
        {
            if (!Arrays.asList("CLEAR", "DISPUTED", "UNKNOWN").contains(p.referenceStatus))
                throw new ServiceException("试卷参考状态缺失，请重试试卷解析");
            if ("CLEAR".equals(p.referenceStatus)) p.referenceIssue = "";
            else if (p.referenceIssue == null || p.referenceIssue.trim().isEmpty()) p.referenceIssue = "参考答案需教师核对";
            if (p.sourceKeys == null) throw new ServiceException("试卷作答位映射缺失");
            for (String key : p.sourceKeys)
                if (!allowed.contains(key) || !seen.add(key)) throw new ServiceException("试卷作答位映射重复或越界");
        }
        return result;
    }
    public Result diagnose(Job job, Input input, Paper paper, Student student) throws Exception
    {
        List<File> pages = new ArrayList<>(); List<String> labels = new ArrayList<>();
        for (String path : student.pages) { pages.add(PracticalInsightInputService.resource(path)); labels.add("学生原卷第" + pages.size() + "页"); }
        if (pages.isEmpty()) return unknown(paper);
        List<Object> questions = new ArrayList<>();
        for (Question q : paper.questions) questions.add(PracticalInsightEngine.map("id", q.id, "number", q.number,
                "stem", q.stem, "options", q.options, "codeContext", q.codeContext,
                "parts", q.parts.stream().map(p -> PracticalInsightEngine.map("id", p.id, "prompt", p.prompt)).collect(java.util.stream.Collectors.toList())));
        Recognition recognition = call(job, input,
                "只按题号逐空抄录学生原卷，不解题、不补全、不纠正。索引每个part恰好返回一次，partId必须使用索引id。页缺失/空位不可见标uncertain，只有看见空作答位才能blank。written表示清晰原文，blank表示空白且text必须为空，"
                + "uncertain表示看不清。输出{\"evidence\":[{\"id\":\"E1\",\"questionId\":\"题目id\",\"partId\":\"子题/空号\","
                + "\"page\":1,\"text\":\"实际原文\",\"status\":\"written或blank或uncertain\"}]}。题目索引："
                + json.writeValueAsString(questions) + "。既有同版本识别仅供核对，仍以原卷为准：" + student.recognitionJson,
                pages, labels, 10000, Recognition.class);
        try { PracticalInsightEngine.validateRecognition(recognition, paper, pages.size()); }
        catch (ServiceException e) { log.warn("学情逐空校验失败，报告{}：{}", job.reportId, e.getMessage()); throw e; }
        return diagnoseRecognized(job, input, paper, recognition);
    }
    Result diagnoseRecognized(Job job, Input input, Paper paper, Recognition recognition) throws Exception
    {
        String prompt = diagnosisPrompt(input, paper) + "；原文证据：" + json.writeValueAsString(recognition);
        Result result = call(job, input, prompt, null, null, 10000, Result.class);
        try { PracticalInsightEngine.validateResult(result, recognition, paper); }
        catch (ServiceException e) {
            result = call(job, input, prompt + "。上一版未通过校验：" + e.getMessage()
                    + "。返回完整修正JSON，不更改原文；无法判断用INSUFFICIENT。上一版："
                    + json.writeValueAsString(result), null, null, 10000, Result.class);
            PracticalInsightEngine.validateResult(result, recognition, paper);
        }
        return result;
    }
    private String diagnosisPrompt(Input input, Paper paper) throws Exception
    {
        return "依据学生原文与参考答案逐知识点诊断，不修改分数，不根据总分倒推学生会什么。每个知识点恰好一条。"
                + "state仅可MASTERED/PARTIAL/MISCONCEPTION/CONFLICT/BLANK/INSUFFICIENT/NOT_COVERED。"
                + "空白不等于不会，看不清或题目contextComplete=false不能据此判错；缺少足够完整证据为INSUFFICIENT。综合题失分不能推给所有知识点。"
                + "CONFLICT只用于不同题同一技能、同等条件的直接矛盾，comparability说明任务为何可比并引用两题清晰证据。单条件正确而多条件错误属于PARTIAL，不能判矛盾。"
                + "correctEvidenceIds必须完整列出本知识点已确认正确的原答证据，不能只在observation文字说正确而漏填。存在正确作答和明确错误时为PARTIAL，不得标MISCONCEPTION；后者仅表示已判定作答全错。不同难度的对错并存不算CONFLICT。"
                + "observation只写可核验事实；causeHypothesis明确是待验证原因。旧兼容字段errorCode固定填OTHER，真正错因分类只写在errors.category。"
                + "PARTIAL/MISCONCEPTION须有errors逐题列出具体错误，同一证据只归最直接知识点，避免重复归因。patternId只能从该知识errorPatterns选，未匹配用空字符串，不强行归类。"
                + "每条errors.evidenceIds只能包含该条questionId对应的written证据，禁止放入blank、uncertain或其他题的证据。空白可在observation描述，但绝不进入errors。"
                + "patternId必须与observedError的具体错误完全一致，不能找最接近的模式凑数。例如学生用了or而目录只有误用and，必须将patternId留空，不能套用and模式。逐条核对模式名称与学生所选选项的实际内容。"
                + "未命中patternId时，尽量提供actualFragment/expectedFragment作为具体错误与正确片段，逐字来自原答或学生所选选项、以及expectedApproach，不包含变量名/题号等无关内容。例如把groupby误写为group，actualFragment为group(、expectedFragment为groupby(；or误代&，则使用or与&，不得用C与B这样的选择字母。相同错误片段保持一致，不用宽泛原因名称；不能用具体片段表达则均填空字符串，不强合并。"
                + "errors.category必须从CONCEPT/SKILL/METHOD/READING/OTHER五者选一，不得使用CONDITION、SYNTAX等其他词。每条errors的observedError、expectedApproach、causeHypothesis、verificationQuestion均为必填非空字符串。observedError写原文具体差异，expectedApproach写正确做法，causeHypothesis只能是待验证假设，verificationQuestion写老师可直接问的验证问题。无可确认错误时errors返回空数组，不要返回空白模板。"
                + "参考答案与题干或代码上下文矛盾时，保留原参考，争议处标证据不足并请教师核对，不得机械按参考判学生错。"
                + "referenceIssue非空的作答位不能进入正确/错误证据；可依据同知识的其他无争议作答诊断，若没有则INSUFFICIENT。"
                + "不得从选择字母猜测无选项依据的错因，不得猜粗心、态度、习惯、时长。不得推断班级人数或共性。"
                + "输出示例（E1正确、E2错误，实际按原文判断）：{\"diagnoses\":[{\"knowledgeId\":\"K1\",\"state\":\"PARTIAL\",\"evidenceIds\":[\"E1\",\"E2\"],"
                + "\"observation\":\"\",\"causeHypothesis\":\"\",\"errorCode\":\"OTHER\",\"comparability\":\"\",\"correctEvidenceIds\":[\"E1\"],"
                + "\"errors\":[{\"questionId\":\"Q1\",\"patternId\":\"\",\"category\":\"SKILL\",\"evidenceIds\":[\"E2\"],\"observedError\":\"\",\"expectedApproach\":\"\",\"actualFragment\":\"\",\"expectedFragment\":\"\",\"causeHypothesis\":\"待验证：...\",\"verificationQuestion\":\"\"}]}]}。"
                + "学科：" + input.scope.subject + "；年级：" + input.gradeLabel + "；试卷：" + json.writeValueAsString(paper)
                + "。为控制长度，观察和原因各限40字，错误描述各限60字；actualFragment和expectedFragment可留空。";
    }
    public Map<Long, Result> diagnoseBatch(Job job, Input input, Paper paper, List<Student> students) throws Exception
    {
        Map<Long, Result> results = new LinkedHashMap<>();
        Map<String, Object> payload = new LinkedHashMap<>();
        Map<String, Recognition> evidence = new LinkedHashMap<>();
        Map<String, Student> identities = new LinkedHashMap<>();
        for (Student s : students)
        {
            Recognition r;
            try { r = existingRecognition(paper, s); }
            catch (Exception e) { r = null; }
            if (r == null) { Result missing = unknown(paper); missing.reviewReasons.add("当前版本缺少可对应的逐空原答，请补充批改识别证据"); results.put(s.studentId, missing); continue; }
            String alias = "S" + identities.size(); identities.put(alias, s); evidence.put(alias, r);
            payload.put(alias, PracticalInsightEngine.map("recognition", r, "scoringDetails", s.scoringDetails));
        }
        if (payload.isEmpty()) return results;
        JsonNode batch = call(job, input, diagnosisPrompt(input, paper)
                + "。以下是多名学生既有原文和正式评分项得分。逐名独立诊断，不抄录图片，不重新评分，不混用学生证据。"
                + "外层输出{\"students\":{\"S0\":{\"diagnoses\":[...]},...}}，使用输入匿名编号。不要输出evidence原文副本。数据："
                + json.writeValueAsString(payload), null, null, 16000, JsonNode.class);
        for (String alias : identities.keySet())
        {
            Result candidate = null;
            // 仅按本批唯一匿名编号取结果，隔离单人结构异常；兼容模型把编号放在顶层。
            JsonNode nested = batch == null ? null : batch.path("students").get(alias);
            JsonNode flat = batch == null ? null : batch.get(alias);
            JsonNode node = nested == null ? flat : flat == null ? nested : null;
            if (node != null) try { candidate = json.treeToValue(node, Result.class); }
            catch (JsonProcessingException e) { log.warn("学情单人结构异常，报告{}，匿名编号{}", job.reportId, alias); }
            results.put(identities.get(alias).studentId, PracticalInsightEngine.keepValidDiagnoses(candidate, evidence.get(alias), paper));
        }
        return results;
    }

    Recognition existingRecognition(Paper paper, Student student) throws Exception
    {
        if (student.recognitionJson == null || student.rubricSnapshotId == null || student.pages.isEmpty()) return null;
        Map<String, JsonNode> sources = new HashMap<>(); Set<String> duplicates = new HashSet<>();
        for (JsonNode item : json.readTree(student.recognitionJson).path("items")) for (JsonNode p : item.path("parts"))
        {
            String key = student.rubricSnapshotId + ":" + item.path("rubricItemId").asText() + ":" + p.path("label").asText();
            if (sources.put(key, p) != null) duplicates.add(key);
        }
        Recognition result = new Recognition(); boolean found = false;
        for (Question q : paper.questions) for (Part p : q.parts)
        {
            List<JsonNode> matches = new ArrayList<>();
            for (String key : p.sourceKeys) if (sources.containsKey(key) && !duplicates.contains(key)) matches.add(sources.get(key));
            Evidence e = new Evidence(); e.id = "E" + (result.evidence.size() + 1); e.questionId = q.id; e.partId = p.id;
            e.status = "uncertain"; e.text = ""; e.page = 1;
            if (matches.size() == 1)
            {
                JsonNode source = matches.get(0); int page = source.path("page").asInt(); String status = source.path("status").asText();
                String text = source.path("text").asText();
                if (page > 0 && page <= student.pages.size() && source.path("text").isTextual()
                        && Arrays.asList("written", "blank", "uncertain").contains(status)
                        && (!"blank".equals(status) || text.trim().isEmpty()) && (!"written".equals(status) || !text.trim().isEmpty()))
                { e.page = page; e.status = status; e.text = text; found = true; }
            }
            result.evidence.add(e);
        }
        if (!found) return null;
        PracticalInsightEngine.validateRecognition(result, paper, student.pages.size()); return result;
    }
    public Teaching teaching(Job job, Input input, Paper paper, Map<String, Object> report) throws Exception
    {
        // 只给匿名聚合事实；人数和名单不会由语言模型重新生成。
        List<Map<String, Object>> knowledgeFacts = new ArrayList<>();
        for (Map<String, Object> row : (List<Map<String, Object>>) report.get("knowledgeStats"))
        {
            Map<String, Object> copy = new LinkedHashMap<>(row); copy.remove("members"); knowledgeFacts.add(copy);
        }
        List<Object> errorFacts = new ArrayList<>();
        for (Map<String, Object> row : (List<Map<String, Object>>) report.get("errorStats"))
        {
            Map<String, Object> copy = new LinkedHashMap<>(row); copy.remove("studentIds");
            List<Object> examples = new ArrayList<>();
            for (Map<String, Object> example : (List<Map<String, Object>>) row.get("examples"))
            {
                Map<String, Object> anonymous = new LinkedHashMap<>(example); anonymous.remove("studentId");
                examples.add(anonymous); if (examples.size() >= 2) break;
            }
            copy.put("examples", examples); errorFacts.add(copy);
        }
        Object facts = PracticalInsightEngine.map("coverage", report.get("coverage"), "knowledgeStats", knowledgeFacts, "errorStats", errorFacts);
        Teaching result = call(job, input,
                "为" + input.gradeLabel + input.scope.subject + "设计下一课，最多" + input.scope.lessonMinutes + "分钟，最多5项活动。"
                + "只针对本次样本的可核验问题；coverage.sufficient=false时只给暂定补证/个别练习方案，严禁全班普遍等外推。"
                + "不写人数、比例、姓名，不贴固定能力标签。优先核心薄弱知识；无问题证据时设计验证性任务，不编造薄弱点。"
                + "依据errorStats中的具体原答案差异设计讲法与分层练习，原因仍待验证，不推测学习习惯。每项输出完整可直接使用的题目及答案，检测限5分钟，检测标准必须可观察。"
                + "输出{\"activities\":[{\"knowledgeId\":\"K1\",\"objective\":\"\",\"explanation\":\"\","
                + "\"foundationPractice\":\"\",\"extensionPractice\":\"\",\"referenceAnswer\":\"\","
                + "\"exitQuestion\":\"\",\"exitAnswer\":\"\",\"successCriteria\":\"\",\"minutes\":10}]}。"
                + "试卷：" + json.writeValueAsString(paper) + "；服务端事实：" + json.writeValueAsString(facts),
                null, null, 6000, Teaching.class);
        PracticalInsightEngine.validateTeaching(result, paper, input.scope.lessonMinutes);
        // 无论覆盖率多少，模型都不得把未观测学生概括成全班结论。
        if (json.writeValueAsString(result).matches("(?s).*(全班普遍|全班都|所有学生都|普遍存在|大多数学生|全体学生均).*"))
            throw new ServiceException("教学建议包含未经证实的班级外推");
        return result;
    }
    private <T> T call(Job job, Input input, String prompt, List<File> files, List<String> labels, int tokens, Class<T> type) throws Exception
    {
        Job current = mapper.get(job.reportId);
        if (current == null || !"RUNNING".equals(current.status) || !Objects.equals(current.runToken, job.runToken)) throw new ServiceException("诊断已停止");
        requireBudget(current, input, maxCostYuan);
        TeacherAiConfig config = configs.status(input.teacherId);
        if (config == null || !Boolean.TRUE.equals(config.getEnabled()) || !Objects.equals(config.getModelName(), input.model))
            throw new ServiceException("AI配置已变化，请重新生成诊断");
        JsonNode response = provider.chatInsight(config, configs.apiKey(config), prompt, files, labels, tokens);
        mapper.usage(job.reportId, response.path("usage").path("prompt_tokens").asLong(), response.path("usage").path("completion_tokens").asLong());
        if ("length".equals(response.path("choices").path(0).path("finish_reason").asText())) throw new ServiceException("诊断输出超出长度，请缩小材料范围");
        try { return json.readValue(response.path("choices").path(0).path("message").path("content").asText(), type); }
        catch (JsonMappingException e) {
            // 仅记录结构路径，不将学生原文或供应商响应写入日志。
            log.warn("学情JSON结构不符，报告{}，阶段{}，类型{}，字段{}", job.reportId, type.getSimpleName(), e.getClass().getSimpleName(), e.getPathReference());
            throw e;
        }
    }
    public static Result unknown(Paper paper)
    {
        Result result = new Result();
        for (Knowledge k : paper.knowledgePoints)
        {
            Diagnosis d = new Diagnosis(); d.knowledgeId = k.id; d.state = State.INSUFFICIENT;
            d.observation = "没有可读取的当前版本作答证据"; result.diagnoses.add(d);
        }
        return result;
    }
}
