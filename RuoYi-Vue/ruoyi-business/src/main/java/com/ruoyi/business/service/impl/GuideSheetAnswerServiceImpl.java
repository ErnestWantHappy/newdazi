package com.ruoyi.business.service.impl;

import java.util.Date;
import java.util.List;

import com.ruoyi.business.domain.BizGuideSheetAnswer;
import com.ruoyi.business.domain.BizGuideSheetProgress;
import com.ruoyi.business.domain.BizGuideSheet;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.GuideSheetAnswerMapper;
import com.ruoyi.business.mapper.GuideSheetMapper;
import com.ruoyi.business.mapper.GuideSheetProgressMapper;
import com.ruoyi.business.service.IGuideSheetAnswerService;
import com.ruoyi.business.service.GuideSheetGradingService;
import com.ruoyi.business.service.GuideSheetGradingService.GradingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuideSheetAnswerServiceImpl implements IGuideSheetAnswerService
{
    private static final Logger log = LoggerFactory.getLogger(GuideSheetAnswerServiceImpl.class);

    @Autowired
    private GuideSheetAnswerMapper guideSheetAnswerMapper;

    @Autowired
    private GuideSheetProgressMapper guideSheetProgressMapper;

    @Autowired
    private GuideSheetMapper guideSheetMapper;

    @Autowired
    private BizStudentMapper bizStudentMapper;

    @Autowired
    private GuideSheetGradingService gradingService;

    @Override
    public BizGuideSheetAnswer getByStudentAndSheet(Long studentId, Long sheetId)
    {
        return guideSheetAnswerMapper.selectByStudentAndSheet(studentId, sheetId);
    }

    @Override
    public BizGuideSheetAnswer getByAnswerId(Long answerId)
    {
        return guideSheetAnswerMapper.selectBizGuideSheetAnswerByAnswerId(answerId);
    }

    @Override
    public List<BizGuideSheetAnswer> getBySheetId(Long sheetId)
    {
        return guideSheetAnswerMapper.selectBizGuideSheetAnswerList(new BizGuideSheetAnswer() {{
            setSheetId(sheetId);
        }});
    }

    @Override
    public List<BizGuideSheetAnswer> getBySheetIdByClassCode(Long sheetId, String classCode)
    {
        return guideSheetAnswerMapper.selectBySheetIdByClassCode(sheetId, classCode);
    }

    @Override
    @Transactional
    public int saveAnswer(BizGuideSheetAnswer answer)
    {
        try
        {
            Date now = new Date();
            BizGuideSheetAnswer existing = guideSheetAnswerMapper.selectByStudentAndSheet(
                    answer.getStudentId(), answer.getSheetId());
            if (existing != null)
            {
                answer.setAnswerId(existing.getAnswerId());
                answer.setUpdateTime(now);
                if (answer.getStatus() == null)
                {
                    answer.setStatus(existing.getStatus() != null ? existing.getStatus() : "1");
                }
                guideSheetAnswerMapper.updateBizGuideSheetAnswer(answer);
            }
            else
            {
                if (answer.getStatus() == null)
                {
                    answer.setStatus("1");
                }
                answer.setCreateTime(now);
                answer.setUpdateTime(now);
                guideSheetAnswerMapper.insertBizGuideSheetAnswer(answer);
            }

            BizGuideSheetProgress progress = new BizGuideSheetProgress();
            progress.setSheetId(answer.getSheetId());
            progress.setStudentId(answer.getStudentId());
            
            // 从学生信息中获取班级编号
            BizStudent student = bizStudentMapper.selectBizStudentByStudentId(answer.getStudentId());
            String classCode = student != null && student.getClassCode() != null ? student.getClassCode() : "1";
            progress.setClassCode(classCode);
            
            progress.setCurrentPage(answer.getCurrentPage() != null ? answer.getCurrentPage() : 0);
            progress.setIsSubmitted("2".equals(answer.getStatus()) ? "Y" : "N");
            progress.setLastHeartbeat(now);
            guideSheetProgressMapper.insertOrUpdate(progress);

            return 1;
        }
        catch (DuplicateKeyException e)
        {
            log.warn("学生重复提交导学单 studentId={} sheetId={}", answer.getStudentId(), answer.getSheetId());
            throw e;
        }
    }

    @Override
    @Transactional
    public int submitAnswer(BizGuideSheetAnswer answer)
    {
        answer.setStatus("2");
        answer.setSubmitTime(new Date());
        int result = saveAnswer(answer);

        // 自动评分
        try
        {
            BizGuideSheet sheet = guideSheetMapper.selectBizGuideSheetBySheetId(answer.getSheetId());
            if (sheet != null && sheet.getFormJson() != null)
            {
                GradingResult gradingResult = gradingService.grade(
                        sheet.getFormJson(), answer.getAnswerJson());
                answer.setTotalScore(gradingResult.totalScore);
                answer.setGradingStatus(gradingResult.gradingStatus);
                answer.setGradingDetail(gradingResult.gradingDetail);
                guideSheetAnswerMapper.updateBizGuideSheetAnswer(answer);
            }
        }
        catch (Exception e)
        {
            log.error("自动评分失败 sheetId={} studentId={}", answer.getSheetId(), answer.getStudentId(), e);
            // 评分失败不影响提交
        }
        return result;
    }

    @Override
    @Transactional
    public int updateGrading(BizGuideSheetAnswer answer)
    {
        answer.setUpdateTime(new Date());
        return guideSheetAnswerMapper.updateBizGuideSheetAnswer(answer);
    }
}
