package com.ruoyi.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.ruoyi.business.domain.BizQuestion;
import com.ruoyi.business.mapper.BizQuestionMapper;
import com.ruoyi.business.mapper.BizScoringItemMapper;
import com.ruoyi.business.mapper.FlowchartMapper;
import com.ruoyi.business.mapper.PracticalArtifactMapper;
import com.ruoyi.business.mapper.ProgrammingJudgeMapper;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;

/**
 * practical_image_max_count 为 NOT NULL DEFAULT 10 列；题型切换、旧题编辑和重复保存
 * 三类更新场景都不得把它写为 NULL，非图片模式统一保留安全默认值。
 */
@ExtendWith(MockitoExtension.class)
class BizQuestionPracticalImageMaxCountTest
{
    @Mock private BizQuestionMapper bizQuestionMapper;
    @Mock private BizScoringItemMapper bizScoringItemMapper;
    @Mock private ProgrammingJudgeMapper programmingJudgeMapper;
    @Mock private PracticalArtifactMapper practicalArtifactMapper;
    @Mock private FlowchartMapper flowchartMapper;

    private BizQuestionServiceImpl service;

    @BeforeEach
    void setUp()
    {
        service = new BizQuestionServiceImpl();
        ReflectionTestUtils.setField(service, "bizQuestionMapper", bizQuestionMapper);
        ReflectionTestUtils.setField(service, "bizScoringItemMapper", bizScoringItemMapper);
        ReflectionTestUtils.setField(service, "programmingJudgeMapper", programmingJudgeMapper);
        ReflectionTestUtils.setField(service, "practicalArtifactMapper", practicalArtifactMapper);
        ReflectionTestUtils.setField(service, "flowchartMapper", flowchartMapper);

        LoginUser loginUser = new LoginUser(1L, 100L, new SysUser(), java.util.Collections.emptySet());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(loginUser, null, java.util.Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldKeepDefaultValueWhenSwitchingQuestionTypeAwayFromPractical()
    {
        BizQuestion question = new BizQuestion();
        question.setQuestionId(2029L);
        question.setQuestionType("single");
        question.setQuestionContent("普通选择题题干");
        when(bizQuestionMapper.updateBizQuestion(question)).thenReturn(1);

        service.updateBizQuestion(question);

        ArgumentCaptor<BizQuestion> captor = ArgumentCaptor.forClass(BizQuestion.class);
        verify(bizQuestionMapper).updateBizQuestion(captor.capture());
        assertEquals(10, captor.getValue().getPracticalImageMaxCount());
    }

    @Test
    void shouldKeepDefaultValueWhenEditingLegacyPythonQuestion()
    {
        BizQuestion question = new BizQuestion();
        question.setQuestionId(2029L);
        question.setQuestionType("practical");
        question.setPracticalMode("PYTHON");
        question.setQuestionContent("旧 Python 操作题");
        when(bizQuestionMapper.updateBizQuestion(question)).thenReturn(1);

        service.updateBizQuestion(question);

        ArgumentCaptor<BizQuestion> captor = ArgumentCaptor.forClass(BizQuestion.class);
        verify(bizQuestionMapper).updateBizQuestion(captor.capture());
        assertEquals(10, captor.getValue().getPracticalImageMaxCount());
        assertNull(captor.getValue().getFilePath());
        assertNull(captor.getValue().getPracticalAllowedExtensions());
    }

    @Test
    void shouldKeepDefaultValueWhenResavingFlowchartQuestion()
    {
        BizQuestion question = new BizQuestion();
        question.setQuestionId(2029L);
        question.setQuestionType("practical");
        question.setPracticalMode("FLOWCHART");
        question.setQuestionContent("流程图操作题");
        when(bizQuestionMapper.updateBizQuestion(question)).thenReturn(1);

        service.updateBizQuestion(question);

        ArgumentCaptor<BizQuestion> captor = ArgumentCaptor.forClass(BizQuestion.class);
        verify(bizQuestionMapper).updateBizQuestion(captor.capture());
        assertEquals(10, captor.getValue().getPracticalImageMaxCount());
        assertNull(captor.getValue().getPracticalAllowedExtensions());
    }

    @Test
    void shouldKeepBoundValueWhenResavingFilePracticalQuestion()
    {
        BizQuestion question = new BizQuestion();
        question.setQuestionId(2029L);
        question.setQuestionType("practical");
        question.setPracticalMode("FILE");
        question.setQuestionContent("文件操作题");
        question.setPracticalImageMaxCount(5);
        when(bizQuestionMapper.updateBizQuestion(question)).thenReturn(1);

        service.updateBizQuestion(question);

        ArgumentCaptor<BizQuestion> captor = ArgumentCaptor.forClass(BizQuestion.class);
        verify(bizQuestionMapper).updateBizQuestion(captor.capture());
        assertEquals(5, captor.getValue().getPracticalImageMaxCount());
    }

    @Test
    void shouldNotInsertNullImageMaxCountOnCreate()
    {
        BizQuestion question = new BizQuestion();
        question.setQuestionType("practical");
        question.setPracticalMode("PYTHON");
        question.setQuestionContent("新建 Python 操作题");
        when(bizQuestionMapper.insertBizQuestion(question)).thenReturn(1);

        service.insertBizQuestion(question);

        ArgumentCaptor<BizQuestion> captor = ArgumentCaptor.forClass(BizQuestion.class);
        verify(bizQuestionMapper).insertBizQuestion(captor.capture());
        assertEquals(10, captor.getValue().getPracticalImageMaxCount());
        verify(bizScoringItemMapper, never()).insertBizScoringItem(any());
    }
}
