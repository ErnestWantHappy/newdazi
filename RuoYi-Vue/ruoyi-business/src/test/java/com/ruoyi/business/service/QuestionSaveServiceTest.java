package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.domain.*;
import com.ruoyi.business.mapper.BizQuestionMapper;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;

class QuestionSaveServiceTest {
    QuestionSaveService service; QuestionMediaStore store; JdbcTemplate jdbc;
    BizQuestionMapper mapper; IBizQuestionService questions; QuestionImageService images; BizQuestion old;
    @BeforeEach void setup(){
        SysUser user=new SysUser();user.setUserId(9L);user.setUserName("test");
        LoginUser login=new LoginUser(9L,2L,user,new HashSet<>());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(login,null,Collections.emptyList()));
        jdbc=mock(JdbcTemplate.class);store=spy(new QuestionMediaStore(jdbc));mapper=mock(BizQuestionMapper.class);questions=mock(IBizQuestionService.class);images=mock(QuestionImageService.class);
        service=new QuestionSaveService();ReflectionTestUtils.setField(service,"store",store);ReflectionTestUtils.setField(service,"questions",mapper);ReflectionTestUtils.setField(service,"questionService",questions);ReflectionTestUtils.setField(service,"images",images);
        old=question();old.setQuestionId(40L);old.setCreatorId(9L);
        when(mapper.selectQuestionForUpdate(40L)).thenReturn(old);
        doAnswer(inv->{Map<String,Object> r=new HashMap<>();r.put("receipt_id",1L);r.put("payload_hash",inv.getArgument(2));return r;}).when(store).one(startsWith("select * from biz_question_save_receipt"),any(),any());
        // 回执摘要需从INSERT真实参数取得，模拟JDBC而不耦合摘要实现。
        final String[] hash={null};
        when(jdbc.update(startsWith("insert ignore into biz_question_save_receipt"),any(),any(),any(),any())).thenAnswer(i->{hash[0]=i.getArgument(3);return 1;});
        doAnswer(i->{Map<String,Object> r=new HashMap<>();r.put("receipt_id",1L);r.put("payload_hash",hash[0]);return r;}).when(store).one(startsWith("select * from biz_question_save_receipt"),any(),any());
        when(questions.insertBizQuestion(any())).thenAnswer(i->{((BizQuestion)i.getArgument(0)).setQuestionId(80L);return 1;});
        when(questions.updateBizQuestion(any())).thenReturn(1);
    }
    @AfterEach void clear(){SecurityContextHolder.clearContext();}
    BizQuestion question(){BizQuestion q=new BizQuestion();q.setQuestionType("choice");q.setQuestionContent("题干");q.setOptionA("甲");q.setOptionB("乙");q.setAnswer("A");return q;}
    BizQuestion changed(){BizQuestion q=question();q.setQuestionId(40L);q.setExpectedSourceFingerprint(old.getSourceFingerprint());q.setQuestionContent("新题干");QuestionMedia m=new QuestionMedia();m.stem.add(new QuestionMedia.ImageRef("5"));q.setMedia(m);return q;}
    @Test void usedImageQuestionForksAndKeepsSource(){doReturn(1L).when(store).count(anyString(),any(),any(),any(),any(),any());BizQuestion q=changed();Map<String,Object> result=service.save(q,false);assertEquals("FORKED",result.get("saveMode"));assertEquals(80L,result.get("questionId"));assertEquals("题干",old.getQuestionContent());verify(questions,never()).updateBizQuestion(any());}
    @Test void unusedImageQuestionUpdates(){doReturn(0L).when(store).count(anyString(),any(),any(),any(),any(),any());assertEquals("UPDATED",service.save(changed(),false).get("saveMode"));verify(questions,never()).insertBizQuestion(any());}
    @Test void staleFingerprintRejectedBeforeBinding(){BizQuestion q=changed();q.setExpectedSourceFingerprint("stale");assertThrows(ServiceException.class,()->service.save(q,false));verifyNoInteractions(images,questions);}
    @Test void foreignOwnerRejected(){old.setCreatorId(22L);assertThrows(ServiceException.class,()->service.save(changed(),false));verifyNoInteractions(images,questions);}
    @Test void oldTextQuestionKeepsExistingUpdateRule(){BizQuestion q=question();q.setQuestionId(40L);q.setQuestionContent("改文字");assertEquals("UPDATED",service.save(q,false).get("saveMode"));verifyNoInteractions(images);}
    @Test void noContentChangeDoesNotFork(){QuestionMedia m=new QuestionMedia();m.stem.add(new QuestionMedia.ImageRef("5"));old.setMedia(m);BizQuestion q=question();q.setQuestionId(40L);q.setMedia(m);assertEquals("UPDATED",service.save(q,false).get("saveMode"));verify(questions,never()).insertBizQuestion(any());}
    @Test void otherQuestionTypesKeepSeparateConfigurationSaveFlow(){old.setQuestionType("practical");BizQuestion q=question();q.setQuestionId(40L);q.setQuestionType("practical");q.setMedia(new QuestionMedia());q.setSaveRequestId("same-key-python-config");assertEquals("UPDATED",service.save(q,false).get("saveMode"));verifyNoInteractions(jdbc,images);}
}
