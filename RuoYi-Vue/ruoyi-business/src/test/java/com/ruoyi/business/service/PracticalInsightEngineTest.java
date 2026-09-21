package com.ruoyi.business.service;

import java.util.*;
import com.ruoyi.business.domain.dto.PracticalInsightV3.*;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PracticalInsightEngineTest
{
    private Paper paper()
    {
        Paper p = new Paper(); Question q = new Question(); q.id = "Q1";
        q.stem = "读取Excel，筛选及格学生并按班级计算平均成绩";
        q.referenceAnswer = "pd.read_excel; df[df.score >= 60]; groupby('class').mean()";
        q.contextComplete = true; Part part = new Part(); part.id = "1"; part.referenceAnswer = q.referenceAnswer; q.parts.add(part);
        q.sourcePages.add(1); p.questions.add(q);
        String[] names = {"Excel文件读取", "DataFrame条件筛选", "groupby分组聚合"};
        for (int i = 0; i < names.length; i++) { Knowledge k = new Knowledge(); k.id = "K" + i; k.name = names[i]; k.core = true; k.rationale = q.stem; k.questionIds.add(q.id); p.knowledgePoints.add(k); }
        return p;
    }
    private Recognition recognition(String status)
    {
        Recognition r = new Recognition(); Evidence e = new Evidence(); e.id = "E1"; e.questionId = "Q1"; e.partId = "1"; e.page = 1; e.status = status; e.text = "blank".equals(status) ? "" : "pd.read_excel('a.xlsx')"; r.evidence.add(e); return r;
    }
    private Result result(State state)
    {
        Result r = new Result(); r.evidence = recognition(state == State.BLANK ? "blank" : "written").evidence;
        for (Knowledge k : paper().knowledgePoints) { Diagnosis d = new Diagnosis(); d.knowledgeId = k.id; d.state = state; d.evidenceIds.add("E1"); d.observation = "原文证据"; d.errorCode = "CONCEPT";
            if (state == State.PARTIAL || state == State.MISCONCEPTION) d.errors.add(error("Q1", "E1", "")); r.diagnoses.add(d); } return r;
    }
    private ErrorDetail error(String question, String evidence, String pattern)
    {
        ErrorDetail e = new ErrorDetail(); e.questionId = question; e.patternId = pattern; e.evidenceIds.add(evidence);
        e.category = "SKILL"; e.observedError = "筛选条件漏掉等号"; e.expectedApproach = "使用>=包含边界";
        e.causeHypothesis = "待验证：不理解边界"; e.verificationQuestion = "60分是否应该保留？"; return e;
    }
    private Input input(int total, int confirmed)
    {
        Input in = new Input(); in.total = total; in.scope = new Request();
        for (long id = 1; id <= confirmed; id++) { Student s = new Student(); s.studentId = id; s.name = "学生" + id; s.score = 90; s.maxScore = 100; in.students.add(s); } return in;
    }
    @Test void realKnowledgeAcceptedAndScoringLabelRejected()
    {
        PracticalInsightEngine.validatePaper(paper(), 1);
        Paper p = paper(); p.knowledgePoints.get(0).name = "第1题答案是否正确合理";
        assertThrows(ServiceException.class, () -> PracticalInsightEngine.validatePaper(p, 1));
    }
    @Test void invalidOptionalFragmentsDoNotDiscardValidatedDiagnosisButMissingEvidenceDoes()
    {
        Result candidate = result(State.MISCONCEPTION);
        candidate.diagnoses.get(0).errors.get(0).actualFragment = "invented";
        candidate.diagnoses.get(0).errors.get(0).expectedFragment = "unsupported";
        candidate.diagnoses.get(1).errors.get(0).verificationQuestion = "";
        Result kept = PracticalInsightEngine.keepValidDiagnoses(candidate, recognition("written"), paper());
        assertEquals(State.MISCONCEPTION, kept.diagnoses.get(0).state);
        assertEquals("", kept.diagnoses.get(0).errors.get(0).actualFragment);
        assertEquals(State.INSUFFICIENT, kept.diagnoses.get(1).state);
        assertEquals(2, kept.reviewReasons.size());
    }
    @Test void invalidQuestionAndPageRejected()
    {
        Paper p = paper(); p.knowledgePoints.get(0).questionIds.add("other");
        assertThrows(ServiceException.class, () -> PracticalInsightEngine.validatePaper(p, 1));
        assertThrows(ServiceException.class, () -> PracticalInsightEngine.validateRecognition(recognition("written"), paper(), 0));
    }
    @Test void sampleThresholdRequiresBothBoundaries()
    {
        assertFalse(PracticalInsightEngine.sufficient(1, 46, 10, .8));
        assertFalse(PracticalInsightEngine.sufficient(1, 1, 1, .1));
        assertFalse(PracticalInsightEngine.sufficient(10, 46, 1, .1));
        assertFalse(PracticalInsightEngine.sufficient(9, 9, 10, .8));
        assertFalse(PracticalInsightEngine.sufficient(10, 13, 10, .8));
        assertTrue(PracticalInsightEngine.sufficient(10, 12, 10, .8));
        assertTrue(PracticalInsightEngine.sufficient(40, 50, 10, .8));
        assertFalse(PracticalInsightEngine.sufficient(39, 50, 10, .8));
        assertNull(PracticalInsightEngine.percent(0, 0));
    }
    @Test void blankAndUncertainNeverBecomeMastery()
    {
        assertThrows(ServiceException.class, () -> PracticalInsightEngine.validateResult(result(State.MASTERED), recognition("blank"), paper()));
        assertThrows(ServiceException.class, () -> PracticalInsightEngine.validateResult(result(State.MISCONCEPTION), recognition("uncertain"), paper()));
        PracticalInsightEngine.validateResult(result(State.BLANK), recognition("blank"), paper());
    }
    @Test void conflictNeedsTwoDistinctWrittenEvidence()
    {
        Result r = result(State.CONFLICT); r.diagnoses.forEach(d -> d.evidenceIds.add("E1"));
        assertThrows(ServiceException.class, () -> PracticalInsightEngine.validateResult(r, recognition("written"), paper()));
        Recognition rec = recognition("written"); Evidence e = new Evidence(); e.id = "E2"; e.questionId = "Q1"; e.page = 1; e.status = "written"; e.text = "read_csv"; rec.evidence.add(e);
        r.diagnoses.forEach(d -> { d.evidenceIds.set(1, "E2"); d.comparability = "同等条件同一技能"; });
        assertThrows(ServiceException.class, () -> PracticalInsightEngine.validateResult(r, rec, paper()));
        Paper p = paper(); Question q = new Question(); q.id = "Q2"; q.contextComplete = true; p.questions.add(q);
        p.knowledgePoints.forEach(k -> k.questionIds.add("Q2")); e.questionId = "Q2";
        PracticalInsightEngine.validateResult(r, rec, p);
    }
    @Test void secondStageCannotRewriteOriginalText()
    {
        Result r = result(State.MASTERED); Recognition rec = recognition("written"); PracticalInsightEngine.validateResult(r, rec, paper()); assertSame(rec.evidence, r.evidence);
    }
    @Test void allKnowledgeRequiredAndUnrelatedEvidenceRejected()
    {
        Result r = result(State.MASTERED); r.diagnoses.remove(0);
        assertThrows(ServiceException.class, () -> PracticalInsightEngine.validateResult(r, recognition("written"), paper()));
        Result bad = result(State.MASTERED); bad.diagnoses.get(0).evidenceIds.add("missing");
        assertThrows(ServiceException.class, () -> PracticalInsightEngine.validateResult(bad, recognition("written"), paper()));
    }
    @Test void oneOfFortySixUsesActualDenominatorAndFlagsHighScoreWeakness()
    {
        Map<String, Object> report = PracticalInsightEngine.aggregate(input(46, 1), paper(), Collections.singletonMap(1L, result(State.MISCONCEPTION)), 10, .8);
        Map<?, ?> coverage = (Map<?, ?>) report.get("coverage"); assertEquals(false, coverage.get("sufficient")); assertEquals(1, coverage.get("valid"));
        Map<?, ?> k = (Map<?, ?>) ((List<?>) report.get("knowledgeStats")).get(0); assertEquals(45, k.get("unknown")); assertEquals(1, k.get("valid"));
        Map<?, ?> error = (Map<?, ?>) ((List<?>) report.get("errorStats")).get(0); assertEquals(1, error.get("count")); assertEquals(1, error.get("denominator"));
        assertTrue(report.get("focusStudents").toString().contains("总分较好但核心知识薄弱"));
    }
    @Test void missingAndBlankDoNotInflateErrorOrValidCounts()
    {
        Map<String, Object> report = PracticalInsightEngine.aggregate(input(46, 2), paper(), Collections.singletonMap(1L, result(State.BLANK)), 10, .8);
        assertEquals(0, ((Map<?, ?>) report.get("coverage")).get("valid")); assertTrue(((List<?>) report.get("errorStats")).isEmpty());
        assertTrue(report.get("focusStudents").toString().contains("大量空白")); assertTrue(report.get("studentMatrix").toString().contains("INSUFFICIENT"));
    }
    @Test void globalCoverageCannotMaskKnowledgeCoverage()
    {
        Input in = input(10, 10); Map<Long, Result> results = new HashMap<>();
        for (Student s : in.students) { Result r = result(State.MASTERED); r.diagnoses.get(1).state = State.INSUFFICIENT; results.put(s.studentId, r); }
        Map<String, Object> report = PracticalInsightEngine.aggregate(in, paper(), results, 10, .8);
        assertEquals(true, ((Map<?, ?>) report.get("coverage")).get("sufficient"));
        Map<?, ?> k = (Map<?, ?>) ((List<?>) report.get("knowledgeStats")).get(1); assertEquals(false, k.get("sufficient")); assertNull(k.get("masteryRate"));
    }
    @Test void emptyTeachingRejected() { assertThrows(ServiceException.class, () -> PracticalInsightEngine.validateTeaching(new Teaching(), paper(), 40)); }
    @Test void choiceMustIncludeAllOptionsAndIncompleteContextCannotExplainError()
    {
        Paper p = paper(); Question q = p.questions.get(0); q.type = "CHOICE"; q.optionCount = 4; q.options.add("A. read_excel");
        assertThrows(ServiceException.class, () -> PracticalInsightEngine.validatePaper(p, 1));
        q.options.addAll(Arrays.asList("B. read_csv", "C. to_excel", "D. to_csv")); PracticalInsightEngine.validatePaper(p, 1);
        q.contextComplete = false;
        assertThrows(ServiceException.class, () -> PracticalInsightEngine.validateResult(result(State.MISCONCEPTION), recognition("written"), p));
    }
    @Test void missingAndDuplicateSlotsAreRejected()
    {
        Paper p = paper(); Part second = new Part(); second.id = "2"; second.referenceAnswer = "正确答案"; p.questions.get(0).parts.add(second);
        assertThrows(ServiceException.class, () -> PracticalInsightEngine.validateRecognition(recognition("written"), p, 1));
        Recognition r = recognition("written"); Evidence duplicate = recognition("written").evidence.get(0); duplicate.id = "E2"; r.evidence.add(duplicate);
        assertThrows(ServiceException.class, () -> PracticalInsightEngine.validateRecognition(r, paper(), 1));
    }
    @Test void oneBlankReferencedByManyKnowledgePointsDoesNotBecomeManyBlankSlots()
    {
        Paper p = paper(); for (int i = 2; i <= 4; i++) { Part part = new Part(); part.id = "" + i; part.referenceAnswer = "答案"; p.questions.get(0).parts.add(part); }
        Map<String, Object> report = PracticalInsightEngine.aggregate(input(46, 1), p, Collections.singletonMap(1L, result(State.BLANK)), 10, .8);
        assertFalse(report.get("focusStudents").toString().contains("大量空白"));
    }
    @Test void distinctPatternsDoNotMergeAndRepeatedPatternCountsPeopleOnce()
    {
        Paper p = paper(); p.knowledgePoints = new ArrayList<>(p.knowledgePoints.subList(0, 1));
        Question q = new Question(); q.id = "Q2"; q.contextComplete = true; q.parts = p.questions.get(0).parts; p.questions.add(q);
        Knowledge k = p.knowledgePoints.get(0); k.questionIds.add("Q2");
        ErrorPattern pattern = new ErrorPattern(); pattern.id = "BOUNDARY"; pattern.name = "漏掉边界等号"; pattern.questionIds.addAll(k.questionIds); k.errorPatterns.add(pattern);
        Result r = result(State.PARTIAL); r.diagnoses = new ArrayList<>(r.diagnoses.subList(0, 1));
        Diagnosis d = r.diagnoses.get(0); d.errors = new ArrayList<>(Arrays.asList(error("Q1", "E1", pattern.id), error("Q2", "E2", pattern.id)));
        Evidence e = recognition("written").evidence.get(0); e.id = "E2"; e.questionId = "Q2"; r.evidence.add(e);
        Map<String, Object> report = PracticalInsightEngine.aggregate(input(46, 1), p, Collections.singletonMap(1L, r), 10, .8);
        Map<?, ?> row = (Map<?, ?>) ((List<?>) report.get("errorStats")).get(0);
        assertEquals(1, row.get("count")); assertEquals(2, row.get("occurrences")); assertEquals(false, row.get("common"));
        assertTrue(report.get("focusStudents").toString().contains("跨2题重复"));
        d.errors.get(1).patternId = "";
        report = PracticalInsightEngine.aggregate(input(46, 1), p, Collections.singletonMap(1L, r), 10, .8);
        assertEquals(2, ((List<?>) report.get("errorStats")).size());
        assertFalse(report.get("focusStudents").toString().contains("跨2题重复"));
    }
    private Result literalErrorResult(boolean repeated)
    {
        Result r = result(State.MISCONCEPTION);
        r.diagnoses = new ArrayList<>(r.diagnoses.subList(0, 1));
        r.evidence.get(0).text = "df.group('class').mean()";
        ErrorDetail e = r.diagnoses.get(0).errors.get(0);
        e.actualFragment = "group("; e.expectedFragment = "groupby("; e.expectedApproach = "使用groupby('class').mean()";
        if (repeated) {
            Evidence second = recognition("written").evidence.get(0); second.id = "E2"; second.questionId = "Q2"; second.text = "df.group('name').sum()"; r.evidence.add(second);
            ErrorDetail other = error("Q2", "E2", ""); other.actualFragment = e.actualFragment; other.expectedFragment = e.expectedFragment; other.expectedApproach = e.expectedApproach;
            r.diagnoses.get(0).evidenceIds.add("E2"); r.diagnoses.get(0).errors.add(other);
        }
        return r;
    }
    private Paper literalPaper()
    {
        Paper p = paper(); p.knowledgePoints = new ArrayList<>(p.knowledgePoints.subList(0, 1));
        Question q = new Question(); q.id = "Q2"; q.contextComplete = true; q.parts = p.questions.get(0).parts; p.questions.add(q);
        p.knowledgePoints.get(0).questionIds.add("Q2"); return p;
    }
    @Test void groundedDirectoryMissMergesPeopleAndRepeatedQuestions()
    {
        Paper p = literalPaper(); Map<Long, Result> results = new HashMap<>();
        for (long i = 1; i <= 10; i++) {
            Result r = literalErrorResult(true); Recognition rec = new Recognition(); rec.evidence = r.evidence;
            PracticalInsightEngine.validateResult(r, rec, p); results.put(i, r);
        }
        Map<String, Object> report = PracticalInsightEngine.aggregate(input(10, 10), p, results, 10, .8);
        List<?> errors = (List<?>) report.get("errorStats"); assertEquals(1, errors.size());
        Map<?, ?> row = (Map<?, ?>) errors.get(0); assertEquals(10, row.get("count")); assertEquals(20, row.get("occurrences"));
        assertEquals(true, row.get("common")); assertTrue(report.get("focusStudents").toString().contains("跨2题重复"));
        report = PracticalInsightEngine.aggregate(input(46, 10), p, results, 10, .8);
        assertEquals(false, ((Map<?, ?>) ((List<?>) report.get("errorStats")).get(0)).get("common"));
    }
    @Test void differentExpectedFragmentsCannotMerge()
    {
        Result a = literalErrorResult(false), b = literalErrorResult(false);
        ErrorDetail e = b.diagnoses.get(0).errors.get(0); e.expectedFragment = "sum("; e.expectedApproach = "使用sum()";
        Map<Long, Result> results = new HashMap<>(); results.put(1L, a); results.put(2L, b);
        Map<String, Object> report = PracticalInsightEngine.aggregate(input(46, 2), literalPaper(), results, 10, .8);
        assertEquals(2, ((List<?>) report.get("errorStats")).size());
    }
    @Test void fragmentsMustComeFromAnswerOrSelectedOption()
    {
        Paper p = literalPaper(); p.questions.get(0).options = Arrays.asList("A. df.groupby('class')", "D. df.group('class')");
        Result r = literalErrorResult(false); r.evidence.get(0).text = "D";
        Recognition rec = new Recognition(); rec.evidence = r.evidence;
        PracticalInsightEngine.validateResult(r, rec, p);
        r.evidence.get(0).text = "A";
        assertThrows(ServiceException.class, () -> PracticalInsightEngine.validateResult(r, rec, p));
        r.evidence.get(0).status = "blank"; r.evidence.get(0).text = "";
        assertThrows(ServiceException.class, () -> PracticalInsightEngine.validateResult(r, rec, p));
    }
    @Test void distinctDirectoryMissesInSameQuestionAreAllowedButDuplicatesRejected()
    {
        Result r = literalErrorResult(false); Diagnosis d = r.diagnoses.get(0);
        ErrorDetail second = error("Q1", "E1", ""); second.observedError = "使用了错误的聚合函数"; d.errors.add(second);
        Recognition rec = new Recognition(); rec.evidence = r.evidence;
        PracticalInsightEngine.validateResult(r, rec, literalPaper());
        d.errors.add(second);
        assertThrows(ServiceException.class, () -> PracticalInsightEngine.validateResult(r, rec, literalPaper()));
    }
    @Test void bareChoiceLettersCannotBecomeSharedErrorPatterns()
    {
        Result r = literalErrorResult(false); ErrorDetail e = r.diagnoses.get(0).errors.get(0);
        r.evidence.get(0).text = "C"; e.actualFragment = "C"; e.expectedFragment = "B"; e.expectedApproach = "应选B";
        Recognition rec = new Recognition(); rec.evidence = r.evidence;
        assertThrows(ServiceException.class, () -> PracticalInsightEngine.validateResult(r, rec, literalPaper()));
        Map<Long, Result> results = new HashMap<>(); results.put(1L, r); results.put(2L, r);
        Map<String, Object> report = PracticalInsightEngine.aggregate(input(46, 2), literalPaper(), results, 10, .8);
        assertEquals(2, ((List<?>) report.get("errorStats")).size());
    }
    @Test void referenceDisputeCannotJudgeButOtherCleanSlotsRemainUsable()
    {
        Paper p = paper(); p.questions.get(0).parts.get(0).referenceIssue = "变量与上步筛选不一致，需教师核对";
        for (State state : Arrays.asList(State.MASTERED, State.MISCONCEPTION, State.PARTIAL))
            assertThrows(ServiceException.class, () -> PracticalInsightEngine.validateResult(result(state), recognition("written"), p));
        PracticalInsightEngine.validateResult(result(State.INSUFFICIENT), recognition("written"), p);
        Part clean = new Part(); clean.id = "2"; clean.referenceAnswer = "pd.read_excel"; p.questions.get(0).parts.add(clean);
        Recognition rec = recognition("written"); rec.evidence.get(0).partId = "2";
        PracticalInsightEngine.validateResult(result(State.MASTERED), rec, p);
    }
    @Test void disputedBackgroundDoesNotDiscardCleanErrorEvidence()
    {
        Paper p = paper(); Part disputed = new Part(); disputed.id = "2"; disputed.referenceIssue = "参考有冲突"; p.questions.get(0).parts.add(disputed);
        Result r = result(State.PARTIAL); Recognition rec = recognition("written");
        Evidence background = recognition("blank").evidence.get(0); background.id = "E2"; background.partId = "2"; rec.evidence.add(background);
        r.diagnoses.forEach(d -> d.evidenceIds.add("E2"));
        PracticalInsightEngine.validateResult(r, rec, p);
        assertTrue(r.diagnoses.stream().allMatch(d -> d.state == State.PARTIAL));
    }
    @Test void mixedCorrectAndWrongIsNormalizedToPartial()
    {
        Result r = result(State.MISCONCEPTION); Recognition rec = recognition("written");
        Evidence correct = recognition("written").evidence.get(0); correct.id = "E2"; rec.evidence.add(correct);
        r.diagnoses.forEach(d -> { d.evidenceIds.add("E2"); d.correctEvidenceIds.add("E2"); });
        PracticalInsightEngine.validateResult(r, rec, paper());
        assertTrue(r.diagnoses.stream().allMatch(d -> d.state == State.PARTIAL));
    }
    @Test void correctEvidenceCannotBeUnknownBlankOrAlsoWrong()
    {
        Result r = result(State.MISCONCEPTION); Diagnosis d = r.diagnoses.get(0); d.correctEvidenceIds.add("missing");
        assertThrows(ServiceException.class, () -> PracticalInsightEngine.validateResult(r, recognition("written"), paper()));
        d.correctEvidenceIds.set(0, "E1");
        assertThrows(ServiceException.class, () -> PracticalInsightEngine.validateResult(r, recognition("written"), paper()));
        Result blank = result(State.BLANK); blank.diagnoses.get(0).correctEvidenceIds.add("E1");
        assertThrows(ServiceException.class, () -> PracticalInsightEngine.validateResult(blank, recognition("blank"), paper()));
    }
    @Test void behaviorGuessAndClassExtrapolationRejected()
    {
        Result r = result(State.PARTIAL); r.diagnoses.get(0).errors.get(0).causeHypothesis = "学生粗心";
        assertThrows(ServiceException.class, () -> PracticalInsightEngine.validateResult(r, recognition("written"), paper()));
        r.diagnoses.get(0).errors.get(0).causeHypothesis = "待验证：边界理解错误"; r.diagnoses.get(0).observation = "全班普遍存在问题";
        assertThrows(ServiceException.class, () -> PracticalInsightEngine.validateResult(r, recognition("written"), paper()));
    }
    @Test void patternCoverageCannotBeReplacedByGlobalKnowledgeCoverage()
    {
        Paper p = paper(); Knowledge k = p.knowledgePoints.get(0); ErrorPattern pattern = new ErrorPattern();
        pattern.id = "BOUNDARY"; pattern.name = "漏掉等号"; pattern.questionIds.add("Q1"); k.errorPatterns.add(pattern);
        Map<Long, Result> results = new HashMap<>();
        for (long i = 1; i <= 10; i++) {
            Result r = result(State.MASTERED);
            if (i <= 2) { r.diagnoses.get(0).state = State.PARTIAL; r.diagnoses.get(0).errors.add(error("Q1", "E1", pattern.id)); }
            else r.evidence = recognition("uncertain").evidence;
            results.put(i, r);
        }
        Map<String, Object> report = PracticalInsightEngine.aggregate(input(10, 10), p, results, 10, .8);
        assertEquals(true, ((Map<?, ?>) report.get("coverage")).get("sufficient"));
        Map<?, ?> error = (Map<?, ?>) ((List<?>) report.get("errorStats")).get(0);
        assertEquals(2, error.get("denominator")); assertEquals(false, error.get("common"));
        for (Result r : results.values()) r.evidence = recognition("written").evidence;
        report = PracticalInsightEngine.aggregate(input(10, 10), p, results, 10, .8);
        error = (Map<?, ?>) ((List<?>) report.get("errorStats")).get(0);
        assertEquals(10, error.get("denominator")); assertEquals(true, error.get("common"));
    }
}
