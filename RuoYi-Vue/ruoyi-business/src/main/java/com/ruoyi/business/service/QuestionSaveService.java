package com.ruoyi.business.service;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.BizQuestion;
import com.ruoyi.business.mapper.BizQuestionMapper;
import com.ruoyi.business.utils.QuestionMediaCodec;
import com.ruoyi.business.utils.TypingContentNormalizer;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/** 新题与回执同事务，丢响应重试不会再复制一份题目。 */
@Service
public class QuestionSaveService {
    @Autowired private QuestionMediaStore store;
    @Autowired private BizQuestionMapper questions;
    @Autowired private IBizQuestionService questionService;
    @Autowired private QuestionImageService images;

    @Transactional(rollbackFor=Exception.class)
    public Map<String,Object> save(BizQuestion q, boolean create) {
        Long owner=SecurityUtils.getUserId();
        Long sourceId=create?null:q.getQuestionId();
        if (!create && sourceId==null) throw new ServiceException("请选择要修改的题目");
        // Python/文件作品等分步配置继续沿用原保存链路，不能共享选择题的幂等请求键。
        BizQuestion current=create?null:questions.selectQuestionForUpdate(sourceId);
        String type=q.getQuestionType()!=null?q.getQuestionType():(current==null?null:current.getQuestionType());
        // 打字题学生只能敲英文撇，弯单引号会导致永远打不对；保存前把字母间的弯单引号归一化。
        if("typing".equals(type)) q.setQuestionContent(TypingContentNormalizer.normalize(q.getQuestionContent()));
        if(!"choice".equals(type) && !QuestionMediaCodec.hasImages(q.getMediaJson()) && (current==null || !QuestionMediaCodec.hasImages(current.getMediaJson()))) {
            int rows=create?questionService.insertBizQuestion(q):questionService.updateBizQuestion(q);
            if(rows==-1)throw new ServiceException("无权编辑他人创建的题目",403);
            if(rows!=1)throw new ServiceException("题目未保存，请重试");
            return result(q.getQuestionId(),create?"CREATED":"UPDATED");
        }
        // 请求原样摘要用于幂等；不能先按当前源题合并，否则分叉后的重放会得到不同摘要。
        String payloadHash=QuestionMediaCodec.hash((create+":"+sourceId+":"+QuestionMediaCodec.write(q)).getBytes(StandardCharsets.UTF_8));
        String request=q.getSaveRequestId();
        if (request==null || request.isEmpty()) request=UUID.randomUUID().toString();
        if (!request.matches("[A-Za-z0-9_-]{8,64}")) throw new ServiceException("保存请求无效，请刷新页面");
        store.jdbc().update("insert ignore into biz_question_save_receipt(owner_id,request_id,payload_hash,source_question_id) values(?,?,?,?)",owner,request,payloadHash,sourceId);
        Map<String,Object> receipt=store.one("select * from biz_question_save_receipt where owner_id=? and request_id=? for update",owner,request);
        if (!payloadHash.equals(receipt.get("payload_hash"))) throw new ServiceException("保存请求已用于其他内容，请重新保存");
        if (receipt.get("question_id")!=null) return result(QuestionMediaStore.number(receipt.get("question_id")),String.valueOf(receipt.get("save_mode")));
        BizQuestion original=current;
        if (!create && original==null) throw new ServiceException("题目不存在，请刷新题库");
        if (original!=null && !SecurityUtils.isAdmin(owner) && !owner.equals(original.getCreatorId())) throw new ServiceException("无权编辑他人创建的题目",403);
        if (original!=null) {
            mergeUnsubmitted(q,original);
            if (q.getExpectedSourceFingerprint()!=null && !q.getExpectedSourceFingerprint().equals(QuestionMediaCodec.fingerprint(original)))
                throw new ServiceException("原题已在其他窗口修改，请重新打开后保存");
            q.setCreatorId(original.getCreatorId());
            q.setCreateBy(original.getCreateBy());
            q.setCreateTime(original.getCreateTime());
        }
        boolean imageContent=QuestionMediaCodec.hasImages(q.getMediaJson()) || (original!=null && QuestionMediaCodec.hasImages(original.getMediaJson()));
        if (imageContent && !"choice".equals(q.getQuestionType())) throw new ServiceException("含图选择题请保持原题型，其他题型请新建");
        if ("choice".equals(q.getQuestionType())) {
            q.setAnswer(QuestionMediaCodec.empty(q.getAnswer()).trim().toUpperCase(java.util.Locale.ROOT));
            List<String> errors=QuestionMediaCodec.validate(q,false);
            if (!errors.isEmpty()) throw new ServiceException(String.join("；",errors));
        } else if (q.isMediaTouched() && QuestionMediaCodec.hasImages(q.getMediaJson())) throw new ServiceException("本次仅支持选择题图片");
        boolean contentChanged=original!=null && !QuestionMediaCodec.fingerprint(q).equals(QuestionMediaCodec.fingerprint(original));
        boolean fork=imageContent && contentChanged && original!=null && used(sourceId);
        if (fork && q.getExpectedSourceFingerprint()==null) throw new ServiceException("本题已用于课程，请刷新页面后安全保存修改");
        if (imageContent) images.bind(q,original);
        String mode;
        q.allowMediaSave();
        if (create || fork) {
            q.setQuestionId(null);
            if (questionService.insertBizQuestion(q)!=1) throw new ServiceException("题目未保存，请重试");
            mode=fork?"FORKED":"CREATED";
        } else {
            if (questionService.updateBizQuestion(q)!=1) throw new ServiceException("题目未保存，请重试");
            mode="UPDATED";
        }
        store.jdbc().update("update biz_question_save_receipt set question_id=?,save_mode=? where receipt_id=?",q.getQuestionId(),mode,receipt.get("receipt_id"));
        return result(q.getQuestionId(),mode);
    }

    public Map<String,Object> lookup(String requestId) {
        if(requestId==null || !requestId.matches("[A-Za-z0-9_-]{8,64}"))throw new ServiceException("保存请求无效");
        Map<String,Object> row=store.one("select question_id,save_mode from biz_question_save_receipt where owner_id=? and request_id=?",SecurityUtils.getUserId(),requestId);
        return row==null || row.get("question_id")==null?null:result(QuestionMediaStore.number(row.get("question_id")),String.valueOf(row.get("save_mode")));
    }

    public boolean used(Long id) {
        return store.count("select (exists(select 1 from biz_lesson_question where question_id=?) or exists(select 1 from biz_county_exam_question where question_id=?) or exists(select 1 from biz_county_exam_paper_question where question_id=?) or exists(select 1 from biz_student_answer where question_id=?) or exists(select 1 from biz_student_answer_orphan_archive where question_id=?))",id,id,id,id,id)>0;
    }

    private void mergeUnsubmitted(BizQuestion q,BizQuestion old) {
        if (!q.isMediaTouched()) q.setMediaJson(old.getMediaJson());
        if (q.getQuestionType()==null) q.setQuestionType(old.getQuestionType());
        if (q.getQuestionContent()==null) q.setQuestionContent(old.getQuestionContent());
        if (q.getOptionA()==null) q.setOptionA(old.getOptionA());
        if (q.getOptionB()==null) q.setOptionB(old.getOptionB());
        if (q.getOptionC()==null) q.setOptionC(old.getOptionC());
        if (q.getOptionD()==null) q.setOptionD(old.getOptionD());
        if (q.getAnswer()==null) q.setAnswer(old.getAnswer());
        if (q.getAnalysis()==null) q.setAnalysis(old.getAnalysis());
        if (q.getGrade()==null) q.setGrade(old.getGrade());
        if (q.getSemester()==null) q.setSemester(old.getSemester());
        if (q.getLessonNum()==null) q.setLessonNum(old.getLessonNum());
        if (q.getIsPublic()==null) q.setIsPublic(old.getIsPublic());
    }
    private Map<String,Object> result(Long id,String mode) {
        Map<String,Object> r=new LinkedHashMap<>();r.put("questionId",id);r.put("saveMode",mode);
        r.put("message","FORKED".equals(mode)?"已保留历史版本，本次修改已保存为新题":"题目已保存");return r;
    }
}
