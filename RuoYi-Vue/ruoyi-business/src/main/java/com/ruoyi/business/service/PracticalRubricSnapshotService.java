package com.ruoyi.business.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.BizScoringItem;
import com.ruoyi.business.domain.PracticalQuestionMaterial;
import com.ruoyi.business.domain.PracticalRubricSnapshot;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.domain.vo.PracticalScoringItemVo;
import com.ruoyi.business.mapper.BizScoringItemMapper;
import com.ruoyi.business.mapper.PracticalArtifactMapper;
import com.ruoyi.business.mapper.PracticalRubricSnapshotMapper;
import com.ruoyi.common.exception.ServiceException;

/**
 * 固化课程保存时的操作题评分口径，并按学生提交版本读取，避免题库后改污染历史评分。
 */
@Service
public class PracticalRubricSnapshotService
{
    @Autowired private PracticalRubricSnapshotMapper snapshotMapper;
    @Autowired private BizScoringItemMapper scoringItemMapper;
    @Autowired private PracticalArtifactMapper artifactMapper;
    @Autowired private PracticalScoringPolicyService scoringPolicyService;
    @Autowired private ObjectMapper objectMapper;

    public void snapshotLesson(Long lessonId, List<BizLessonQuestionDetailVo> questions, Long userId)
    {
        if (lessonId == null || questions == null) return;
        for (BizLessonQuestionDetailVo question : questions)
        {
            if (question != null && "practical".equals(question.getQuestionType()))
            {
                createIfChanged(lessonId, question, userId);
            }
        }
    }

    public PracticalRubricSnapshot ensureLatest(Long lessonId,
                                                 BizLessonQuestionDetailVo question,
                                                 Long userId)
    {
        // 即使已有快照也必须比较当前规则；课程保存后再次提交时，不能继续绑定旧评分口径。
        return createIfChanged(lessonId, question, userId);
    }

    public PracticalRubricSnapshot resolve(Long versionId,
                                           Long lessonId,
                                           BizLessonQuestionDetailVo fallback,
                                           Long userId)
    {
        PracticalRubricSnapshot snapshot = versionId == null
                ? null : snapshotMapper.selectByVersionId(versionId);
        if (snapshot != null) return snapshot;
        if (lessonId == null || fallback == null) return null;
        return ensureLatest(lessonId, fallback, userId);
    }

    public List<PracticalScoringItemVo> buildScoringItems(PracticalRubricSnapshot snapshot)
    {
        if (snapshot == null) return Collections.emptyList();
        try
        {
            List<BizScoringItem> items = objectMapper.readValue(snapshot.getScoringItemsJson(),
                    new TypeReference<List<BizScoringItem>>() { });
            return scoringPolicyService.buildScoringItems(items, snapshot.getQuestionScore());
        }
        catch (Exception e)
        {
            throw new ServiceException("评分标准快照无法读取，请联系管理员");
        }
    }

    public List<PracticalQuestionMaterial> getReferenceMaterials(PracticalRubricSnapshot snapshot)
    {
        if (snapshot == null) return Collections.emptyList();
        try
        {
            return objectMapper.readValue(snapshot.getReferenceMaterialsJson(),
                    new TypeReference<List<PracticalQuestionMaterial>>() { });
        }
        catch (Exception e)
        {
            return Collections.emptyList();
        }
    }

    private PracticalRubricSnapshot createIfChanged(Long lessonId,
                                                     BizLessonQuestionDetailVo question,
                                                     Long userId)
    {
        try
        {
            String itemsJson = objectMapper.writeValueAsString(
                    scoringItemMapper.selectItemsByQuestion(question.getQuestionId()));
            List<PracticalQuestionMaterial> references = new ArrayList<PracticalQuestionMaterial>();
            for (PracticalQuestionMaterial material : artifactMapper.selectMaterialsByQuestion(
                    question.getQuestionId()))
            {
                if (material != null && "REFERENCE".equals(material.getMaterialType()))
                {
                    references.add(material);
                }
            }
            String referencesJson = objectMapper.writeValueAsString(references);
            PracticalRubricSnapshot latest = snapshotMapper.selectLatest(lessonId, question.getQuestionId());
            String content = question.getQuestionContent() == null ? "" : question.getQuestionContent();
            Integer score = question.getQuestionScore() == null ? 0 : question.getQuestionScore().intValue();
            if (latest != null
                    && Objects.equals(latest.getQuestionContent(), content)
                    && Objects.equals(latest.getQuestionScore(), score)
                    && jsonEquals(latest.getScoringItemsJson(), itemsJson)
                    && jsonEquals(latest.getReferenceMaterialsJson(), referencesJson))
            {
                return latest;
            }
            PracticalRubricSnapshot snapshot = new PracticalRubricSnapshot();
            snapshot.setLessonId(lessonId);
            snapshot.setQuestionId(question.getQuestionId());
            snapshot.setSnapshotVersion(latest == null ? 1 : latest.getSnapshotVersion() + 1);
            snapshot.setQuestionContent(content);
            snapshot.setQuestionScore(score);
            snapshot.setScoringItemsJson(itemsJson);
            snapshot.setReferenceMaterialsJson(referencesJson);
            snapshot.setCreatedByUserId(userId);
            snapshot.setCreateTime(new Date());
            snapshotMapper.insert(snapshot);
            return snapshot;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("操作题评分标准快照生成失败");
        }
    }

    private boolean jsonEquals(String left, String right) throws Exception
    {
        if (left == null || right == null) return Objects.equals(left, right);
        return objectMapper.readTree(left).equals(objectMapper.readTree(right));
    }
}
