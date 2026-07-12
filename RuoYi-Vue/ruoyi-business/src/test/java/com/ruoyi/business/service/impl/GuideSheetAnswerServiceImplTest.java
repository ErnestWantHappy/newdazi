package com.ruoyi.business.service.impl;

import com.ruoyi.business.domain.BizGuideSheetAnswer;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.GuideSheetAnswerMapper;
import com.ruoyi.business.mapper.GuideSheetMapper;
import com.ruoyi.business.mapper.GuideSheetProgressMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 导学单作答服务单测（对齐 DigitalGuide 当前实现）。
 */
@ExtendWith(MockitoExtension.class)
class GuideSheetAnswerServiceImplTest
{
    @Mock
    private GuideSheetAnswerMapper answerMapper;

    @Mock
    private GuideSheetProgressMapper progressMapper;

    @Mock
    private GuideSheetMapper guideSheetMapper;

    @Mock
    private BizStudentMapper studentMapper;

    @InjectMocks
    private GuideSheetAnswerServiceImpl service;

    @Test
    void shouldUpdateExistingAnswerOnSave()
    {
        BizGuideSheetAnswer existing = new BizGuideSheetAnswer();
        existing.setAnswerId(11L);
        existing.setStatus("1");
        when(answerMapper.selectByStudentAndSheet(9L, 7L)).thenReturn(existing);
        when(answerMapper.updateBizGuideSheetAnswer(any(BizGuideSheetAnswer.class))).thenReturn(1);
        when(studentMapper.selectBizStudentByStudentId(9L)).thenReturn(null);
        when(progressMapper.insertOrUpdate(any())).thenReturn(1);

        BizGuideSheetAnswer draft = new BizGuideSheetAnswer();
        draft.setSheetId(7L);
        draft.setStudentId(9L);
        draft.setAnswerJson("{\"q1\":\"草稿\"}");

        assertEquals(1, service.saveAnswer(draft));
        assertEquals(11L, draft.getAnswerId());
        verify(answerMapper).updateBizGuideSheetAnswer(draft);
        verify(progressMapper).insertOrUpdate(any());
    }
}
