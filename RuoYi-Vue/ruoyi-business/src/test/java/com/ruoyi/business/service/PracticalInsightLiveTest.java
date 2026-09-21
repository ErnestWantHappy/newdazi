package com.ruoyi.business.service;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import com.fasterxml.jackson.databind.*;
import com.ruoyi.business.domain.TeacherAiConfig;
import com.ruoyi.business.domain.dto.PracticalInsightV3.*;
import com.ruoyi.business.mapper.PracticalInsightMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/** 显式启用的只读真实数据回归，不写业务数据库。 */
@EnabledIfEnvironmentVariable(named="INSIGHT_LIVE_FIXTURE", matches=".+")
class PracticalInsightLiveTest
{
    @Test void existingClassEvidenceMustProduceUsableReport() throws Exception
    {
        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.WARN);
        ObjectMapper json = new ObjectMapper(); JsonNode fixture = json.readTree(new File(System.getenv("INSIGHT_LIVE_FIXTURE")));
        Input input = json.treeToValue(fixture.path("input"), Input.class);
        File directory = new File(fixture.path("outputDirectory").asText());
        PracticalInsightAiService ai = new PracticalInsightAiService();
        PracticalInsightMapper mapper = mock(PracticalInsightMapper.class);
        TeacherAiConfigService configs = mock(TeacherAiConfigService.class);
        QwenPracticalVisionGradingProvider provider = spy(new QwenPracticalVisionGradingProvider());
        ReflectionTestUtils.setField(ai,"json",json); ReflectionTestUtils.setField(ai,"mapper",mapper);
        ReflectionTestUtils.setField(ai,"configs",configs); ReflectionTestUtils.setField(ai,"provider",provider);
        ReflectionTestUtils.setField(ai,"maxCostYuan",BigDecimal.valueOf(20));
        Job job = new Job(); job.reportId=2L; job.status="RUNNING"; job.runToken="local-readonly";
        when(mapper.get(2L)).thenReturn(job);
        doAnswer(a -> { job.promptTokens += (Long)a.getArgument(1); job.completionTokens += (Long)a.getArgument(2); return null; }).when(mapper).usage(eq(2L),anyLong(),anyLong());
        TeacherAiConfig config = new TeacherAiConfig(); config.setEnabled(true); config.setModelName(input.model); config.setEndpointUrl(fixture.path("endpoint").asText());
        when(configs.status(input.teacherId)).thenReturn(config); when(configs.apiKey(config)).thenReturn(System.getenv("INSIGHT_LIVE_API_KEY"));
        List<File> pages=new ArrayList<>(); List<String> labels=new ArrayList<>();
        for(JsonNode p:fixture.path("pages")) { pages.add(new File(p.path("file").asText())); labels.add(p.path("label").asText()); }
        long started=System.currentTimeMillis();
        File paperFile=new File(directory,"live-paper.json"); boolean cached=paperFile.exists();
        Paper paper=cached ? json.readValue(paperFile,Paper.class) : ai.paper(job,input,pages,labels); json.writerWithDefaultPrettyPrinter().writeValue(paperFile,paper);
        Map<Long,Result> results=new LinkedHashMap<>(); int batchSize=Math.max(1,Math.min(6,60/paper.knowledgePoints.size()));
        for(int offset=0;offset<input.students.size();offset+=batchSize) {
            results.putAll(ai.diagnoseBatch(job,input,paper,input.students.subList(offset,Math.min(offset+batchSize,input.students.size()))));
            json.writerWithDefaultPrettyPrinter().writeValue(new File(directory,"live-results.json"),results);
            System.out.println("LIVE_PROGRESS completed="+results.size()+" seconds="+(System.currentTimeMillis()-started)/1000);
        }
        Map<String,Object> report=PracticalInsightEngine.aggregate(input,paper,results,10,.8);
        report.put("elapsedSeconds",(System.currentTimeMillis()-started)/1000); report.put("promptTokens",job.promptTokens); report.put("completionTokens",job.completionTokens);
        json.writerWithDefaultPrettyPrinter().writeValue(new File(directory,"live-report.json"),report);
        verify(provider,times(cached ? 0 : 1)).chatInsight(eq(config),anyString(),anyString(),notNull(),notNull(),eq(10000));
        verify(provider,times((input.students.size()+batchSize-1)/batchSize)).chatInsight(eq(config),anyString(),anyString(),isNull(),isNull(),eq(16000));
        assertEquals(36,results.size());
        assertEquals(Boolean.TRUE,((Map<?,?>)report.get("coverage")).get("sufficient"));
        report.put("teaching",ai.teaching(job,input,paper,report));
        json.writerWithDefaultPrettyPrinter().writeValue(new File(directory,"live-report.json"),report);
    }
}
