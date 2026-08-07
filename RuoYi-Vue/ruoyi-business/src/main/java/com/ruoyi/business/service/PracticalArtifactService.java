package com.ruoyi.business.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.business.domain.BizScoringDetail;
import com.ruoyi.business.domain.BizStudentAnswer;
import com.ruoyi.business.domain.PracticalArtifact;
import com.ruoyi.business.domain.PracticalAttachment;
import com.ruoyi.business.domain.PracticalQuestionMaterial;
import com.ruoyi.business.domain.PracticalSubmissionVersion;
import com.ruoyi.business.domain.PracticalRubricSnapshot;
import com.ruoyi.business.domain.dto.PracticalUploadTicket;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.domain.vo.PracticalArtifactVo;
import com.ruoyi.business.domain.vo.PracticalSubmissionVo;
import com.ruoyi.business.mapper.BizScoringDetailMapper;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.PracticalArtifactMapper;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.file.FileUploadUtils;

/**
 * 普通课程操作题逻辑作品、版本和附件闭环。
 */
@Service
public class PracticalArtifactService
{
    private static final long STUCK_NORMALIZATION_TIMEOUT_MILLIS = 10L * 60L * 1000L;
    private static final String CONTEXT_LESSON = "LESSON";
    private static final String TICKET_PREFIX = "student:practical-artifact-ticket:";

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private PracticalFilePolicyService filePolicyService;

    @Autowired
    private PracticalArtifactMapper artifactMapper;

    @Autowired
    private BizStudentAnswerMapper studentAnswerMapper;

    @Autowired
    private BizScoringDetailMapper scoringDetailMapper;

    @Autowired
    private PracticalAttachmentConversionService conversionService;

    @Autowired
    private PracticalRubricSnapshotService rubricSnapshotService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 单文件暂存。多图作品由前端逐张上传，最终一次提交全部 token。
     */
    public PracticalUploadTicket stageStudentFile(Long studentId,
                                                  Long lessonId,
                                                  BizLessonQuestionDetailVo question,
                                                  MultipartFile file) throws Exception
    {
        if (studentId == null || lessonId == null || question == null || question.getQuestionId() == null)
        {
            throw new ServiceException("操作题上传上下文不完整");
        }
        PracticalUploadTicket ticket = filePolicyService.inspect(
                file, question.getPracticalAllowedExtensions());

        String uploadDir = RuoYiConfig.getUploadPath()
                + "/student-answer-artifact/" + UUID.randomUUID().toString().replace("-", "");
        String resourcePath = FileUploadUtils.upload(
                uploadDir, file, new String[] { ticket.getFileExtension() }, true);
        String token = UUID.randomUUID().toString().replace("-", "");
        ticket.setToken(token);
        ticket.setStudentId(studentId);
        ticket.setLessonId(lessonId);
        ticket.setQuestionId(question.getQuestionId());
        ticket.setResourcePath(resourcePath);
        redisCache.setCacheObject(ticketKey(token), ticket, 60, TimeUnit.MINUTES);
        // 兼容尚未更新的学生端：旧提交接口仍可在一小时内认领该路径。
        redisCache.setCacheObject("student:practical-upload-owner:" + resourcePath,
                studentId, 60, TimeUnit.MINUTES);
        return ticket;
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public PracticalArtifactVo submitLessonArtifact(Long studentId,
                                                    Long submittedByUserId,
                                                    BizLessonQuestionDetailVo question,
                                                    Long lessonId,
                                                    Long expectedVersionId,
                                                    List<String> uploadTokens)
    {
        List<PracticalUploadTicket> tickets = resolveTickets(
                studentId, lessonId, question, uploadTokens);
        validateLogicalWork(question, tickets);

        Date now = new Date();
        PracticalArtifact artifact = artifactMapper.selectByContextForUpdate(
                CONTEXT_LESSON, lessonId, studentId, question.getQuestionId());
        if (artifact == null)
        {
            if (expectedVersionId != null)
            {
                throw new ServiceException("作品版本已发生变化，请刷新后重新提交");
            }
            artifact = new PracticalArtifact();
            artifact.setContextType(CONTEXT_LESSON);
            artifact.setContextId(lessonId);
            artifact.setStudentId(studentId);
            artifact.setQuestionId(question.getQuestionId());
            artifact.setLatestVersionNo(0);
            artifact.setLockVersion(0);
            artifact.setCreateTime(now);
            artifact.setUpdateTime(now);
            artifactMapper.insertArtifact(artifact);
            artifact = artifactMapper.selectByContextForUpdate(
                    CONTEXT_LESSON, lessonId, studentId, question.getQuestionId());
        }
        if (!Objects.equals(expectedVersionId, artifact.getCurrentVersionId()))
        {
            throw new ServiceException("作品版本已发生变化，请刷新后重新提交");
        }

        BizStudentAnswer existingAnswer = studentAnswerMapper.selectLatestByStudentLessonQuestion(
                studentId, lessonId, question.getQuestionId());
        if (existingAnswer != null)
        {
            existingAnswer = studentAnswerMapper.selectByIdForUpdate(existingAnswer.getAnswerId());
        }
        String scoringDetailsJson = serializeScoringDetails(existingAnswer);
        if (artifact.getCurrentVersionId() != null)
        {
            int superseded = artifactMapper.supersedeVersion(
                    artifact.getCurrentVersionId(),
                    existingAnswer == null ? null : existingAnswer.getScore(),
                    scoringDetailsJson,
                    now);
            if (superseded <= 0)
            {
                throw new ServiceException("作品版本已发生变化，请刷新后重新提交");
            }
        }

        int nextVersionNo = (artifact.getLatestVersionNo() == null ? 0 : artifact.getLatestVersionNo()) + 1;
        PracticalSubmissionVersion version = new PracticalSubmissionVersion();
        version.setArtifactId(artifact.getArtifactId());
        PracticalRubricSnapshot rubricSnapshot = rubricSnapshotService.ensureLatest(
                lessonId, question, submittedByUserId);
        version.setRubricSnapshotId(rubricSnapshot.getSnapshotId());
        version.setVersionNo(nextVersionNo);
        version.setVersionStatus("CURRENT");
        version.setScoreStatus("UNGRADED");
        version.setSubmittedByUserId(submittedByUserId);
        version.setSubmitTime(now);
        version.setCreateTime(now);
        artifactMapper.insertVersion(version);

        List<PracticalAttachment> attachments = new ArrayList<PracticalAttachment>();
        for (int index = 0; index < tickets.size(); index++)
        {
            PracticalUploadTicket ticket = tickets.get(index);
            PracticalAttachment attachment = toAttachment(ticket, version.getVersionId(), index, now);
            artifactMapper.insertAttachment(attachment);
            attachments.add(attachment);
        }

        PracticalAttachment primary = attachments.get(0);
        BizStudentAnswer answer = new BizStudentAnswer();
        answer.setStudentId(studentId);
        answer.setLessonId(lessonId);
        answer.setQuestionId(question.getQuestionId());
        answer.setStudentAnswer(primary.getResourcePath());
        answer.setIsCorrect(false);
        answer.setScore(null);
        answer.setAnswerTime(0);
        answer.setSubmitTime(now);
        answer.setPreviewStatus(primary.getPreviewStatus());
        answer.setPreviewPath(primary.getPreviewPath());
        answer.setPreviewRetryCount(0);
        answer.setPreviewErrorMessage(null);
        answer.setPracticalArtifactId(artifact.getArtifactId());
        answer.setPracticalVersionId(version.getVersionId());
        studentAnswerMapper.upsertAnswer(answer);
        artifactMapper.bindVersionAnswer(version.getVersionId(), answer.getAnswerId());

        if (artifactMapper.updateCurrentVersion(
                artifact.getArtifactId(), version.getVersionId(), nextVersionNo,
                artifact.getLockVersion(), now) <= 0)
        {
            throw new ServiceException("作品版本已发生变化，请刷新后重新提交");
        }
        if (existingAnswer != null)
        {
            scoringDetailMapper.deleteBizScoringDetailByAnswerId(existingAnswer.getAnswerId());
        }

        registerAfterCommit(tickets, attachments);
        return buildArtifactVo(artifact.getArtifactId(), version, attachments);
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void deleteCurrentLessonArtifact(Long studentId,
                                            Long lessonId,
                                            Long questionId,
                                            Long expectedVersionId)
    {
        PracticalArtifact artifact = artifactMapper.selectByContextForUpdate(
                CONTEXT_LESSON, lessonId, studentId, questionId);
        if (artifact == null || artifact.getCurrentVersionId() == null)
        {
            return;
        }
        if (!Objects.equals(expectedVersionId, artifact.getCurrentVersionId()))
        {
            throw new ServiceException("作品版本已发生变化，请刷新后重新操作");
        }
        BizStudentAnswer answer = studentAnswerMapper.selectLatestByStudentLessonQuestion(
                studentId, lessonId, questionId);
        if (answer == null)
        {
            throw new ServiceException("作品答题记录不存在");
        }
        answer = studentAnswerMapper.selectByIdForUpdate(answer.getAnswerId());
        Date now = new Date();
        artifactMapper.deleteCurrentVersion(
                artifact.getCurrentVersionId(), answer.getScore(), serializeScoringDetails(answer), now);
        scoringDetailMapper.deleteBizScoringDetailByAnswerId(answer.getAnswerId());
        studentAnswerMapper.clearPracticalAnswer(answer.getAnswerId(), artifact.getArtifactId(), now);
        if (artifactMapper.updateCurrentVersion(
                artifact.getArtifactId(), null, artifact.getLatestVersionNo(),
                artifact.getLockVersion(), now) <= 0)
        {
            throw new ServiceException("作品版本已发生变化，请刷新后重新操作");
        }
    }

    public PracticalArtifactVo getCurrentLessonArtifact(Long studentId, Long lessonId, Long questionId)
    {
        PracticalArtifact artifact = artifactMapper.selectByContext(
                CONTEXT_LESSON, lessonId, studentId, questionId);
        if (artifact == null || artifact.getCurrentVersionId() == null)
        {
            return null;
        }
        PracticalSubmissionVersion version = artifactMapper.selectVersionById(artifact.getCurrentVersionId());
        if (version == null)
        {
            return null;
        }
        return buildArtifactVo(artifact.getArtifactId(), version,
                artifactMapper.selectAttachmentsByVersion(version.getVersionId()));
    }

    public void enrichSubmissions(List<PracticalSubmissionVo> submissions)
    {
        if (submissions == null) return;
        for (PracticalSubmissionVo submission : submissions)
        {
            if (submission != null && submission.getPracticalVersionId() != null)
            {
                PracticalSubmissionVersion version = artifactMapper.selectVersionById(
                        submission.getPracticalVersionId());
                if (version != null)
                {
                    submission.setRubricSnapshotId(version.getRubricSnapshotId());
                    PracticalRubricSnapshot rubric = rubricSnapshotService.resolve(
                            version.getVersionId(), null, null, null);
                    if (rubric != null) submission.setMaxScore(rubric.getQuestionScore());
                }
                submission.setAttachments(artifactMapper.selectAttachmentsByVersion(
                        submission.getPracticalVersionId()));
                hydrateNormalizedPages(submission.getAttachments());
                for (PracticalAttachment attachment : submission.getAttachments())
                {
                    if (attachment != null && "pending".equals(attachment.getNormalizedStatus()))
                    {
                        conversionService.convertAsync(attachment.getAttachmentId());
                    }
                }
            }
        }
    }

    /** 学生只能看到起始文件和补充资源，教师参考答案绝不下发。 */
    public List<PracticalQuestionMaterial> getStudentMaterials(Long questionId)
    {
        List<PracticalQuestionMaterial> result = new ArrayList<PracticalQuestionMaterial>();
        for (PracticalQuestionMaterial material : artifactMapper.selectMaterialsByQuestion(questionId))
        {
            if (material != null && !"REFERENCE".equals(material.getMaterialType()))
            {
                result.add(material);
            }
        }
        return result;
    }

    public int retryFailedAttachments(List<PracticalSubmissionVo> submissions)
    {
        int accepted = 0;
        if (submissions == null) return accepted;
        for (PracticalSubmissionVo submission : submissions)
        {
            if (submission == null || submission.getAttachments() == null) continue;
            for (PracticalAttachment attachment : submission.getAttachments())
            {
                if (isRecoverableNormalization(attachment)
                        && conversionService.retry(attachment.getAttachmentId()))
                {
                    accepted++;
                }
            }
        }
        return accepted;
    }

    /** 正常转换可能耗时，只有失败或超过十分钟的卡住任务才允许人工重试。 */
    private boolean isRecoverableNormalization(PracticalAttachment attachment)
    {
        if (attachment == null || attachment.getAttachmentId() == null
                || (attachment.getNormalizedRetryCount() != null && attachment.getNormalizedRetryCount() >= 3))
        {
            return false;
        }
        if ("failed".equals(attachment.getNormalizedStatus())) return true;
        if (!"converting".equals(attachment.getNormalizedStatus())) return false;
        Date reference = attachment.getNormalizedLastRetryTime();
        if (reference == null) reference = attachment.getUpdateTime();
        return reference != null
                && System.currentTimeMillis() - reference.getTime() >= STUCK_NORMALIZATION_TIMEOUT_MILLIS;
    }

    private List<PracticalUploadTicket> resolveTickets(Long studentId,
                                                       Long lessonId,
                                                       BizLessonQuestionDetailVo question,
                                                       List<String> uploadTokens)
    {
        if (uploadTokens == null || uploadTokens.isEmpty() || uploadTokens.size() > 10)
        {
            throw new ServiceException("请选择要提交的操作题作品");
        }
        Set<String> uniqueTokens = new HashSet<String>();
        List<PracticalUploadTicket> tickets = new ArrayList<PracticalUploadTicket>();
        for (String token : uploadTokens)
        {
            String normalized = token == null ? "" : token.trim();
            if (normalized.isEmpty() || !uniqueTokens.add(normalized))
            {
                throw new ServiceException("上传凭证为空或重复");
            }
            Object cached = redisCache.getCacheObject(ticketKey(normalized));
            if (!(cached instanceof PracticalUploadTicket))
            {
                throw new ServiceException("上传凭证已失效，请重新选择文件");
            }
            PracticalUploadTicket ticket = (PracticalUploadTicket) cached;
            if (!Objects.equals(studentId, ticket.getStudentId())
                    || !Objects.equals(lessonId, ticket.getLessonId())
                    || !Objects.equals(question.getQuestionId(), ticket.getQuestionId()))
            {
                throw new ServiceException("上传文件不属于当前学生或题目");
            }
            tickets.add(ticket);
        }
        return tickets;
    }

    private void validateLogicalWork(BizLessonQuestionDetailVo question,
                                     List<PracticalUploadTicket> tickets)
    {
        Set<String> allowed = filePolicyService.parseAllowedExtensions(
                question.getPracticalAllowedExtensions());
        boolean allImages = true;
        for (PracticalUploadTicket ticket : tickets)
        {
            if (!allowed.contains(ticket.getFileExtension()))
            {
                throw new ServiceException("当前操作题不允许提交该文件格式");
            }
            allImages = allImages && filePolicyService.isImage(ticket.getFileExtension());
        }
        if (!allImages && tickets.size() != 1)
        {
            throw new ServiceException("Office或PDF作品只能提交一个文件，图片作品可提交多张");
        }
        int imageMaxCount = question.getPracticalImageMaxCount() == null
                ? 10 : question.getPracticalImageMaxCount();
        if (allImages && (tickets.size() < 1 || tickets.size() > Math.min(Math.max(imageMaxCount, 1), 10)))
        {
            throw new ServiceException("图片作品数量超出题目允许范围");
        }
    }

    private PracticalAttachment toAttachment(PracticalUploadTicket ticket,
                                             Long versionId,
                                             int order,
                                             Date now)
    {
        PracticalAttachment attachment = new PracticalAttachment();
        attachment.setVersionId(versionId);
        attachment.setFileOrder(order);
        attachment.setFileKind(ticket.getFileKind());
        attachment.setOriginalFileName(ticket.getOriginalFileName());
        attachment.setResourcePath(ticket.getResourcePath());
        attachment.setFileExtension(ticket.getFileExtension());
        attachment.setMimeType(ticket.getMimeType());
        attachment.setFileSize(ticket.getFileSize());
        attachment.setSha256(ticket.getSha256());
        attachment.setSecurityStatus("VERIFIED");
        attachment.setPreviewRetryCount(0);
        attachment.setNormalizedStatus("pending");
        attachment.setNormalizedRetryCount(0);
        attachment.setCreateTime(now);
        attachment.setUpdateTime(now);
        if ("IMAGE".equals(ticket.getFileKind()) || "PDF".equals(ticket.getFileKind()))
        {
            attachment.setPreviewStatus("success");
            attachment.setPreviewPath(ticket.getResourcePath());
        }
        else
        {
            attachment.setPreviewStatus("pending");
            attachment.setPreviewPath(null);
        }
        return attachment;
    }

    private String serializeScoringDetails(BizStudentAnswer answer)
    {
        if (answer == null || answer.getAnswerId() == null)
        {
            return "[]";
        }
        List<BizScoringDetail> details = scoringDetailMapper.selectDetailsByAnswerId(answer.getAnswerId());
        try
        {
            return objectMapper.writeValueAsString(details == null
                    ? Collections.emptyList() : details);
        }
        catch (Exception e)
        {
            throw new ServiceException("旧分项成绩快照失败，请稍后重试");
        }
    }

    private PracticalArtifactVo buildArtifactVo(Long artifactId,
                                                PracticalSubmissionVersion version,
                                                List<PracticalAttachment> attachments)
    {
        PracticalArtifactVo result = new PracticalArtifactVo();
        result.setArtifactId(artifactId);
        result.setVersionId(version.getVersionId());
        result.setVersionNo(version.getVersionNo());
        result.setVersionStatus(version.getVersionStatus());
        result.setScoreSnapshot(version.getScoreSnapshot());
        result.setSubmitTime(version.getSubmitTime());
        result.setAttachments(attachments == null
                ? new ArrayList<PracticalAttachment>() : attachments);
        hydrateNormalizedPages(result.getAttachments());
        return result;
    }

    private void hydrateNormalizedPages(List<PracticalAttachment> attachments)
    {
        if (attachments == null) return;
        for (PracticalAttachment attachment : attachments)
        {
            if (attachment == null || attachment.getNormalizedPagesJson() == null) continue;
            try
            {
                attachment.setNormalizedPages(objectMapper.readValue(
                        attachment.getNormalizedPagesJson(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
            }
            catch (Exception e)
            {
                attachment.setNormalizedPages(Collections.<String>emptyList());
            }
        }
    }

    private void registerAfterCommit(List<PracticalUploadTicket> tickets,
                                     List<PracticalAttachment> attachments)
    {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()
        {
            @Override
            public void afterCommit()
            {
                for (PracticalUploadTicket ticket : tickets)
                {
                    redisCache.deleteObject(ticketKey(ticket.getToken()));
                }
                for (PracticalAttachment attachment : attachments)
                {
                    conversionService.convertAsync(attachment.getAttachmentId());
                }
            }
        });
    }

    private String ticketKey(String token)
    {
        return TICKET_PREFIX + token;
    }
}
