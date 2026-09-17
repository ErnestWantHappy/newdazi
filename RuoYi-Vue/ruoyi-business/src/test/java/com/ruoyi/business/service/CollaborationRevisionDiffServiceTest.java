package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.mapper.CollaborationMapper;
import com.ruoyi.common.exception.ServiceException;

class CollaborationRevisionDiffServiceTest
{
    @Test
    void summaryExplicitlyDoesNotAssignAllChangesToSaveTrigger()
    {
        CollaborationRevisionDiffService service = new CollaborationRevisionDiffService();

        String summary = ReflectionTestUtils.invokeMethod(service, "summarize", "段落1", "段落2");

        assertTrue(summary.contains("不代表保存触发者是全部内容作者"));
    }

    @Test
    void extractionFailureOnlyMarksAuditSummary()
    {
        CollaborationRevisionDiffService service = new CollaborationRevisionDiffService();
        CollaborationMapper mapper = mock(CollaborationMapper.class);
        CollaborationRoomService roomService = mock(CollaborationRoomService.class);
        ReflectionTestUtils.setField(service, "mapper", mapper);
        ReflectionTestUtils.setField(service, "roomService", roomService);
        Map<String, Object> revision = new HashMap<String, Object>();
        revision.put("revisionId", 21L);
        revision.put("filePath", "collaboration/missing.docx");
        when(mapper.selectRevisionPair(9L, 2)).thenReturn(revision);
        when(roomService.resolveStoredFile("collaboration/missing.docx"))
                .thenThrow(new ServiceException("测试文件不存在"));

        service.extract(9L, 2);

        verify(mapper).updateRevisionDiff(eq(21L), eq("FAILED"),
                eq("文档已变化，暂无法结构化描述"), any(String.class), any(java.util.Date.class));
    }
}
