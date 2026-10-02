package com.ruoyi.business.service;

import java.math.BigDecimal;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruoyi.business.domain.TeacherAiConfig;
import com.ruoyi.business.domain.dto.PracticalInsightV3.*;
import com.ruoyi.business.mapper.PracticalInsightMapper;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PracticalInsightAiServiceTest
{
    @Test void batchUsesOneTextRequestAndIsolatesMalformedStudentAndMixedEnvelope() throws Exception
    {
        ObjectMapper json = new ObjectMapper(); PracticalInsightAiService service = new PracticalInsightAiService();
        PracticalInsightMapper mapper=mock(PracticalInsightMapper.class); TeacherAiConfigService configs=mock(TeacherAiConfigService.class);
        QwenPracticalVisionGradingProvider provider=mock(QwenPracticalVisionGradingProvider.class);
        ReflectionTestUtils.setField(service,"json",json); ReflectionTestUtils.setField(service,"mapper",mapper);
        ReflectionTestUtils.setField(service,"configs",configs); ReflectionTestUtils.setField(service,"provider",provider);
        ReflectionTestUtils.setField(service,"maxCostYuan",BigDecimal.TEN);
        Job job=new Job();job.reportId=1L;job.status="RUNNING";job.runToken="test";
        Input input=new Input();input.teacherId=2L;input.model="test";input.scope=new Request();input.inputPrice=BigDecimal.ONE;input.outputPrice=BigDecimal.ONE;
        TeacherAiConfig config=new TeacherAiConfig();config.setEnabled(true);config.setModelName("test");
        when(mapper.get(1L)).thenReturn(job);when(configs.configForJob(2L,"QWEN","test")).thenReturn(config);when(configs.apiKey(config)).thenReturn("test-only");
        Paper paper=new Paper();Question q=new Question();q.id="Q1";q.contextComplete=true;paper.questions.add(q);
        Part part=new Part();part.id="P1";part.referenceStatus="CLEAR";part.sourceKeys.add("7:18:A");q.parts.add(part);
        Knowledge k=new Knowledge();k.id="K1";k.questionIds.add("Q1");paper.knowledgePoints.add(k);
        java.util.List<Student> students=new java.util.ArrayList<>();
        for(long i=1;i<=6;i++) {Student s=new Student();s.studentId=i;s.rubricSnapshotId=7L;s.pages.add("must-not-open-image");s.recognitionJson="{\"items\":[{\"rubricItemId\":18,\"parts\":[{\"label\":\"A\",\"page\":1,\"status\":\"written\",\"text\":\"answer\"}]}]}";students.add(s);}
        students.get(5).recognitionJson="invalid-json";
        ObjectNode batch=json.createObjectNode();ObjectNode nested=batch.putObject("students");
        for(int i=0;i<5;i++) {
            ObjectNode target=i==0 ? nested : batch;
            ObjectNode diagnosis=target.putObject("S"+i).putArray("diagnoses").addObject();
            diagnosis.put("knowledgeId","K1").put("state",i==4?"INVALID":"MASTERED");
            diagnosis.put("observation","原答与参考一致");
            diagnosis.putArray("evidenceIds").add("E1");diagnosis.putArray("correctEvidenceIds").add("E1");
        }
        ObjectNode response=json.createObjectNode();response.putArray("choices").addObject().putObject("message").put("content",json.writeValueAsString(batch));
        when(provider.chatInsight(eq(config),eq("test-only"),anyString(),isNull(),isNull(),eq(16000))).thenReturn(response);
        java.util.Map<Long,Result> results=service.diagnoseBatch(job,input,paper,students);
        assertEquals(6,results.size());for(long i=1;i<=4;i++)assertEquals(State.MASTERED,results.get(i).diagnoses.get(0).state);
        for(long i=5;i<=6;i++)assertEquals(State.INSUFFICIENT,results.get(i).diagnoses.get(0).state);
        verify(provider,times(1)).chatInsight(eq(config),eq("test-only"),anyString(),isNull(),isNull(),eq(16000));verifyNoMoreInteractions(provider);
    }
    @Test void reusesSavedAnswerBySnapshotItemAndLabelWithoutVision() throws Exception
    {
        PracticalInsightAiService service = new PracticalInsightAiService();
        ReflectionTestUtils.setField(service, "json", new ObjectMapper());
        Paper p = new Paper(); Question q = new Question(); q.id = "Q1"; p.questions.add(q);
        Part a = new Part(); a.id = "P1"; a.sourceKeys.add("7:18:①"); q.parts.add(a);
        Part b = new Part(); b.id = "P2"; b.sourceKeys.add("7:18:②"); q.parts.add(b);
        Student s = new Student(); s.rubricSnapshotId = 7L; s.pages.add("unused-image");
        s.recognitionJson = "{\"items\":[{\"rubricItemId\":18,\"parts\":[{\"label\":\"①\",\"page\":1,\"status\":\"written\",\"text\":\"原文\"}]}]}";
        Recognition result = service.existingRecognition(p, s);
        assertEquals("原文", result.evidence.get(0).text);
        assertEquals("uncertain", result.evidence.get(1).status);
        s.rubricSnapshotId = 8L; assertNull(service.existingRecognition(p, s));
    }

    @Test void invalidKnowledgeDoesNotDiscardOtherValidatedKnowledge()
    {
        Paper p = new Paper(); Question q = new Question(); q.id = "Q1"; q.contextComplete = true; p.questions.add(q);
        Part part = new Part(); part.id = "P1"; part.referenceStatus = "CLEAR"; part.referenceIssue = "参考正确，无冲突"; q.parts.add(part);
        for (String id : new String[]{"K1", "K2"}) { Knowledge k = new Knowledge(); k.id = id; k.name = id; k.questionIds.add(q.id); p.knowledgePoints.add(k); }
        Recognition r = new Recognition(); Evidence e = new Evidence(); e.id = "E1"; e.questionId = q.id; e.partId = part.id; e.status = "written"; e.text = "x"; r.evidence.add(e);
        Result candidate = new Result(); Diagnosis d = new Diagnosis(); d.knowledgeId = "K1"; d.state = State.MASTERED;
        d.evidenceIds.add(e.id); d.correctEvidenceIds.add(e.id); d.observation = "表达正确"; candidate.diagnoses.add(d);
        Result kept = PracticalInsightEngine.keepValidDiagnoses(candidate, r, p);
        assertEquals(State.MASTERED, kept.diagnoses.get(0).state);
        assertEquals(State.INSUFFICIENT, kept.diagnoses.get(1).state);
        assertEquals(1, kept.reviewReasons.size()); assertSame(r.evidence, kept.evidence);
        part.referenceStatus = "DISPUTED";
        assertEquals(State.INSUFFICIENT, PracticalInsightEngine.keepValidDiagnoses(candidate, r, p).diagnoses.get(0).state);
    }
    @Test void invalidDiagnosisIsCorrectedOnceAndKeepsOriginalEvidence() throws Exception
    { verifyCorrection(true); }

    @Test void invalidCorrectionStopsWithoutUnboundedRetries() throws Exception
    { verifyCorrection(false); }

    private void verifyCorrection(boolean succeeds) throws Exception
    {
        ObjectMapper json = new ObjectMapper();
        PracticalInsightAiService service = new PracticalInsightAiService();
        PracticalInsightMapper mapper = mock(PracticalInsightMapper.class);
        TeacherAiConfigService configs = mock(TeacherAiConfigService.class);
        QwenPracticalVisionGradingProvider provider = mock(QwenPracticalVisionGradingProvider.class);
        ReflectionTestUtils.setField(service, "json", json);
        ReflectionTestUtils.setField(service, "mapper", mapper);
        ReflectionTestUtils.setField(service, "configs", configs);
        ReflectionTestUtils.setField(service, "provider", provider);
        ReflectionTestUtils.setField(service, "maxCostYuan", BigDecimal.TEN);
        Job job = new Job(); job.reportId = 1L; job.status = "RUNNING"; job.runToken = "test";
        Input input = new Input(); input.teacherId = 2L; input.model = "test"; input.scope = new Request();
        input.inputPrice = BigDecimal.ONE; input.outputPrice = BigDecimal.ONE;
        TeacherAiConfig config = new TeacherAiConfig(); config.setEnabled(true); config.setModelName("test");
        when(mapper.get(1L)).thenReturn(job); when(configs.configForJob(2L,"QWEN","test")).thenReturn(config);
        when(configs.apiKey(config)).thenReturn("test-only");
        Paper paper = new Paper(); Knowledge k = new Knowledge(); k.id = "K1"; paper.knowledgePoints.add(k);
        Result invalid = new Result(), valid = new Result();
        Diagnosis d = new Diagnosis(); d.knowledgeId = "K1"; d.state = State.INSUFFICIENT; valid.diagnoses.add(d);
        when(provider.chatInsight(eq(config), eq("test-only"), anyString(), isNull(), isNull(), eq(10000)))
                .thenReturn(response(json, invalid), response(json, succeeds ? valid : invalid));
        Recognition original = new Recognition();
        if (succeeds) assertSame(original.evidence, service.diagnoseRecognized(job, input, paper, original).evidence);
        else assertThrows(ServiceException.class, () -> service.diagnoseRecognized(job, input, paper, original));
        verify(provider, times(2)).chatInsight(eq(config), eq("test-only"), anyString(), isNull(), isNull(), eq(10000));
        verify(mapper, times(2)).usage(1L, 0L, 0L);
    }

    private ObjectNode response(ObjectMapper json, Result result) throws Exception
    {
        ObjectNode root = json.createObjectNode();
        root.putArray("choices").addObject().putObject("message").put("content", json.writeValueAsString(result));
        return root;
    }
}
