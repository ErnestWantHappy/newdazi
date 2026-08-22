package com.ruoyi.business.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.ruoyi.business.domain.BizQuestion;
import com.ruoyi.business.domain.ProgrammingQuestionConfig;
import com.ruoyi.business.domain.ProgrammingTestCase;
import com.ruoyi.business.domain.dto.PythonQuestionImportRequest;
import com.ruoyi.business.domain.dto.PythonQuestionImportRequest.QuestionRow;
import com.ruoyi.business.domain.dto.PythonQuestionImportRequest.TestCaseRow;
import com.ruoyi.business.mapper.ProgrammingJudgeMapper;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;

/** Python 双 Sheet Excel 的预检、短期确认令牌和事务导入。 */
@Service
public class PythonQuestionImportService {
    private static final String TOKEN_PREFIX = "python:question-import:";
    @Autowired private ProgrammingSubmissionService programmingService;
    @Autowired private ProgrammingJudgeMapper programmingMapper;
    @Autowired private IBizQuestionService questionService;
    @Autowired private RedisCache redisCache;

    public Map<String, Object> preview(PythonQuestionImportRequest request, Long userId) {
        List<String> errors = new ArrayList<String>();
        if (request == null || request.getQuestions() == null || request.getQuestions().isEmpty()) throw new ServiceException("“题目”Sheet 不能为空");
        if (request.getQuestions().size() > 30) throw new ServiceException("教师单次最多导入 30 道 Python 题，请分批处理");
        if (request.getTestCases() == null) request.setTestCases(new ArrayList<TestCaseRow>());
        Map<String, List<TestCaseRow>> casesById = groupCases(request.getTestCases(), errors);
        Set<String> externalIds = new HashSet<String>(); Set<String> titles = new HashSet<String>();
        int rowNo = 1;
        for (QuestionRow row : request.getQuestions()) {
            rowNo++;
            normalize(row);
            String prefix = "题目 Sheet 第 " + rowNo + " 行";
            if (empty(row.getExternalId())) errors.add(prefix + "：外部题号不能为空");
            else if (!externalIds.add(row.getExternalId())) errors.add(prefix + "：外部题号重复 " + row.getExternalId());
            if (empty(row.getTitle())) errors.add(prefix + "：标题不能为空");
            else if (!titles.add(row.getTitle())) errors.add(prefix + "：标题在本批次重复");
            else if (programmingMapper.countConfigByTitle(row.getTitle()) > 0) errors.add(prefix + "：题库已存在同名题目“" + row.getTitle() + "”");
            if (empty(row.getDescription())) errors.add(prefix + "：题目描述不能为空");
            if (empty(row.getOutputDescription())) errors.add(prefix + "：输出格式不能为空");
            if (!"1".equals(row.getNoInput()) && empty(row.getInputDescription())) errors.add(prefix + "：有输入题必须填写输入格式");
            if (empty(row.getReferenceCode())) errors.add(prefix + "：参考代码不能为空");
            if (containsBrokenText(row.getTitle()) || containsBrokenText(row.getDescription())) errors.add(prefix + "：检测到疑似乱码字符");
            List<TestCaseRow> cases = casesById.get(row.getExternalId());
            validateCaseGroup(prefix, cases, row, errors);
        }
        for (String externalId : casesById.keySet()) if (!externalIds.contains(externalId)) errors.add("测试点 Sheet：外部题号 " + externalId + " 在题目 Sheet 中不存在");

        List<Map<String, Object>> validation = new ArrayList<Map<String, Object>>();
        if (errors.isEmpty()) {
            for (QuestionRow row : request.getQuestions()) {
                ProgrammingQuestionConfig config = toConfig(row);
                List<ProgrammingTestCase> cases = toCases(casesById.get(row.getExternalId()), row);
                Map<String, Object> result = programmingService.validateImportCandidate(row.getReferenceCode(), config, cases);
                result.put("externalId", row.getExternalId()); result.put("title", row.getTitle()); validation.add(result);
                if (!Boolean.TRUE.equals(result.get("valid"))) errors.add("题目 " + row.getExternalId() + "“" + row.getTitle() + "”：参考代码仅通过 " + result.get("passedCount") + "/" + result.get("totalCount") + " 个测试点");
            }
        }
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("questionCount", request.getQuestions().size()); result.put("testCaseCount", request.getTestCases().size()); result.put("errors", errors); result.put("validation", validation); result.put("ready", errors.isEmpty());
        if (errors.isEmpty()) {
            String token = UUID.randomUUID().toString().replace("-", "");
            redisCache.setCacheObject(tokenKey(userId, token), request, 30, TimeUnit.MINUTES);
            result.put("confirmToken", token); result.put("expiresInMinutes", 30);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> confirm(String token, Long userId, String username) {
        if (empty(token)) throw new ServiceException("导入确认令牌不能为空");
        final String cacheKey = tokenKey(userId, token); final String lockKey = cacheKey + ":lock";
        PythonQuestionImportRequest request = redisCache.getCacheObject(cacheKey);
        if (request == null) throw new ServiceException("导入预检已过期，请重新上传并预检");
        Boolean locked = redisCache.redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 5, TimeUnit.MINUTES);
        if (!Boolean.TRUE.equals(locked)) throw new ServiceException("该批次正在导入，请勿重复确认");
        try {
            Map<String, List<TestCaseRow>> casesById = groupCases(request.getTestCases(), new ArrayList<String>());
            List<Long> questionIds = new ArrayList<Long>();
            for (QuestionRow row : request.getQuestions()) {
                if (programmingMapper.countConfigByTitle(row.getTitle()) > 0) throw new ServiceException("题库已存在同名题目“" + row.getTitle() + "”，请重新预检");
                BizQuestion question = new BizQuestion(); question.setQuestionType("practical"); question.setPracticalMode("PYTHON"); question.setQuestionContent(row.getDescription()); question.setDifficulty(row.getDifficulty()); question.setAnswer(row.getReferenceCode()); question.setIsPublic(row.getIsPublic());
                questionService.insertBizQuestion(question);
                ProgrammingQuestionConfig config = toConfig(row); config.setQuestionId(question.getQuestionId()); config.setTestCases(toCases(casesById.get(row.getExternalId()), row)); config.setValidationStatus("DRAFT"); config.setCreateBy(username); config.setUpdateBy(username);
                programmingMapper.upsertConfig(config);
                int order = 1; for (ProgrammingTestCase testCase : config.getTestCases()) { testCase.setQuestionId(question.getQuestionId()); testCase.setOrderNum(order++); testCase.setCreateBy(username); testCase.setUpdateBy(username); programmingMapper.insertTestCase(testCase); }
                programmingMapper.updateValidationStatus(question.getQuestionId(), "VALID", username);
                questionIds.add(question.getQuestionId());
            }
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { redisCache.deleteObject(cacheKey); redisCache.deleteObject(lockKey); }
            });
            Map<String, Object> result = new HashMap<String, Object>(); result.put("importedCount", questionIds.size()); result.put("questionIds", questionIds); return result;
        } catch (RuntimeException ex) {
            redisCache.deleteObject(lockKey);
            throw ex;
        }
    }

    private static Map<String, List<TestCaseRow>> groupCases(List<TestCaseRow> rows, List<String> errors) {
        Map<String, List<TestCaseRow>> result = new LinkedHashMap<String, List<TestCaseRow>>(); int line = 1;
        for (TestCaseRow row : rows) { line++; row.setExternalId(trim(row.getExternalId())); if (empty(row.getExternalId())) { errors.add("测试点 Sheet 第 " + line + " 行：外部题号不能为空"); continue; } result.computeIfAbsent(row.getExternalId(), key -> new ArrayList<TestCaseRow>()).add(row); }
        return result;
    }
    private static void validateCaseGroup(String prefix, List<TestCaseRow> cases, QuestionRow question, List<String> errors) {
        if (cases == null || cases.isEmpty()) { errors.add(prefix + "：没有测试点"); return; }
        if (cases.size() > 50) errors.add(prefix + "：测试点不能超过 50 个");
        boolean publicCase=false, hiddenCase=false; Set<Integer> orders=new HashSet<Integer>(); int index=0; double totalWeight=0D;
        for (TestCaseRow row : cases) { index++; normalize(row, index); if ("1".equals(row.getIsPublic())) publicCase=true; else hiddenCase=true; totalWeight+=row.getScoreWeight(); if (!orders.add(row.getOrderNum())) errors.add(prefix + "：测试点顺序重复 " + row.getOrderNum()); if (empty(row.getExpectedOutput())) errors.add(prefix + "：测试点“" + row.getCaseName() + "”期望输出为空"); if (containsBrokenText(row.getInputText()) || containsBrokenText(row.getExpectedOutput()) || containsBrokenText(row.getCaseName())) errors.add(prefix + "：测试点“" + row.getCaseName() + "”疑似乱码"); if ("1".equals(question.getNoInput())) row.setInputText(""); }
        if (!publicCase) errors.add(prefix + "：至少需要一个公开样例"); if (!hiddenCase) errors.add(prefix + "：至少需要一个隐藏测试点");
        if (Math.abs(totalWeight-100D)>0.000001D) errors.add(prefix + "：测试点权重合计必须为 100，当前为 " + (Math.round(totalWeight*100D)/100D));
    }
    private static ProgrammingQuestionConfig toConfig(QuestionRow row) { ProgrammingQuestionConfig c=new ProgrammingQuestionConfig(); c.setTitle(row.getTitle()); c.setKnowledgePoints(row.getKnowledgePoints()); c.setNoInput(row.getNoInput()); c.setStarterCode(row.getStarterCode()); c.setInputDescription("1".equals(row.getNoInput()) ? "本题没有输入。" : row.getInputDescription()); c.setOutputDescription(row.getOutputDescription()); c.setConstraintsText(row.getConstraintsText()); c.setSampleExplanation(row.getSampleExplanation()); c.setNotesText(row.getNotesText()); c.setTimeLimitSeconds(row.getTimeLimitSeconds()); c.setMemoryLimitKb(row.getMemoryLimitKb()); c.setMaxProcesses(8); c.setMaxFileSizeKb(1024); c.setMaxOutputKb(64); c.setEnabled("1"); c.setContentVersion(1); return c; }
    private static List<ProgrammingTestCase> toCases(List<TestCaseRow> rows, QuestionRow question) { List<ProgrammingTestCase> result=new ArrayList<ProgrammingTestCase>(); for(TestCaseRow row:rows){ ProgrammingTestCase c=new ProgrammingTestCase(); c.setCaseName(row.getCaseName()); c.setInputText("1".equals(question.getNoInput()) ? "" : row.getInputText()); c.setExpectedOutput(row.getExpectedOutput()); c.setIsPublic(row.getIsPublic()); c.setScoreWeight(row.getScoreWeight()); c.setOrderNum(row.getOrderNum()); result.add(c);} return result; }
    private static void normalize(QuestionRow row) { row.setExternalId(trim(row.getExternalId())); row.setTitle(trim(row.getTitle())); row.setDifficulty(normalizeDifficulty(row.getDifficulty())); row.setKnowledgePoints(trim(row.getKnowledgePoints())); row.setNoInput(yes(row.getNoInput()) ? "1" : "0"); row.setIsPublic(no(row.getIsPublic()) ? "N" : "Y"); if(row.getTimeLimitSeconds()==null)row.setTimeLimitSeconds(2D); if(row.getMemoryLimitKb()==null)row.setMemoryLimitKb(131072); }
    private static void normalize(TestCaseRow row,int index){row.setCaseName(empty(row.getCaseName())?"测试点 "+index:trim(row.getCaseName()));row.setIsPublic(yes(row.getIsPublic())?"1":"0");if(row.getScoreWeight()==null||row.getScoreWeight()<=0)row.setScoreWeight(1D);if(row.getOrderNum()==null||row.getOrderNum()<=0)row.setOrderNum(index);}
    private static String normalizeDifficulty(String value){String v=trim(value).toUpperCase();if("简单".equals(value)||"EASY".equals(v))return "SIMPLE";if("困难".equals(value)||"HARD".equals(v))return "HARD";return "MEDIUM";}
    private static boolean containsBrokenText(String value){return value!=null&&(value.indexOf('\uFFFD')>=0||value.contains("??"));}
    private static boolean yes(String value){String v=trim(value);return "1".equals(v)||"Y".equalsIgnoreCase(v)||"是".equals(v)||"公开".equals(v)||"TRUE".equalsIgnoreCase(v);}
    private static boolean no(String value){String v=trim(value);return "0".equals(v)||"N".equalsIgnoreCase(v)||"否".equals(v)||"FALSE".equalsIgnoreCase(v);}
    private static boolean empty(String value){return value==null||value.trim().isEmpty();}
    private static String trim(String value){return value==null?"":value.trim();}
    private static String tokenKey(Long userId,String token){return TOKEN_PREFIX+userId+":"+token;}
}
