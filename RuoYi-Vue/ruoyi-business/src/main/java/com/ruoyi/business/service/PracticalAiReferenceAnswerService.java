package com.ruoyi.business.service;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.business.domain.TeacherPracticalReferenceAnswer;
import com.ruoyi.business.domain.dto.PracticalUploadTicket;
import com.ruoyi.business.mapper.PracticalAiGradingMapper;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.file.FileUploadUtils;

/** 保存教师在当前课程中的 AI 参考答案；旧文件保留供已创建任务的快照继续使用。 */
@Service
public class PracticalAiReferenceAnswerService
{
    @Autowired private PracticalAiGradingMapper mapper;
    @Autowired private PracticalFilePolicyService filePolicyService;

    public TeacherPracticalReferenceAnswer current(Long teacherUserId, Long deptId,
                                                    Long lessonId, Long questionId)
    {
        return mapper.selectReferenceAnswer(teacherUserId, deptId, lessonId, questionId);
    }

    public TeacherPracticalReferenceAnswer upload(Long teacherUserId, Long deptId,
                                                   Long lessonId, Long questionId,
                                                   MultipartFile file)
    {
        try
        {
            PracticalUploadTicket ticket = filePolicyService.inspect(
                    file, PracticalFilePolicyService.DEFAULT_ALLOWED_EXTENSIONS);
            String uploadDir = RuoYiConfig.getUploadPath() + "/practical-ai-reference/"
                    + UUID.randomUUID().toString().replace("-", "");
            String resourcePath = FileUploadUtils.upload(
                    uploadDir, file, new String[] { ticket.getFileExtension() }, true);
            TeacherPracticalReferenceAnswer answer = new TeacherPracticalReferenceAnswer();
            answer.setTeacherUserId(teacherUserId); answer.setDeptId(deptId);
            answer.setLessonId(lessonId); answer.setQuestionId(questionId);
            answer.setOriginalFileName(ticket.getOriginalFileName());
            answer.setResourcePath(resourcePath); answer.setFileExtension(ticket.getFileExtension());
            answer.setMimeType(ticket.getMimeType()); answer.setFileSize(ticket.getFileSize());
            answer.setSha256(ticket.getSha256());
            mapper.upsertReferenceAnswer(answer);
            return current(teacherUserId, deptId, lessonId, questionId);
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("教师参考答案保存失败，请重试");
        }
    }
}
