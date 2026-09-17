package com.ruoyi.business.service;

import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.BizGuideSheet;
import com.ruoyi.business.domain.BizLessonGuideSheetBinding;
import com.ruoyi.business.domain.vo.LessonDetailVo;
import com.ruoyi.business.mapper.GuideSheetBindingMapper;
import com.ruoyi.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 维护课程当前导学单快照，关闭时保留绑定，仅显式更换时创建新快照。
 */
@Service
public class LessonGuideSheetBindingService
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private GuideSheetBindingMapper bindingMapper;

    @Autowired
    private GuideSheetAccessService accessService;

    public BizLessonGuideSheetBinding synchronize(LessonDetailVo detailVo, Long lessonId,
                                                   Long userId, String username)
    {
        BizLessonGuideSheetBinding current = bindingMapper.selectCurrentByLessonId(lessonId);
        if (!Boolean.TRUE.equals(detailVo.getGuideSheetEnabled()))
        {
            if (current != null && !"N".equals(current.getEnabled()))
            {
                if (bindingMapper.updateEnabled(current.getBindingId(), "N", username, new Date()) != 1)
                {
                    throw new ServiceException("课程导学单状态已变化，请刷新后重试");
                }
                current.setEnabled("N");
            }
            return current;
        }

        Long selectedSheetId = detailVo.getSourceSheetId();
        boolean replaceRequested = Boolean.TRUE.equals(detailVo.getGuideSheetReplaceRequested());
        if (current != null
                && (selectedSheetId == null || selectedSheetId.equals(current.getSourceSheetId())))
        {
            if (!"Y".equals(current.getEnabled()))
            {
                if (bindingMapper.updateEnabled(current.getBindingId(), "Y", username, new Date()) != 1)
                {
                    throw new ServiceException("课程导学单状态已变化，请刷新后重试");
                }
                current.setEnabled("Y");
            }
            return current;
        }
        if (current != null && !replaceRequested)
        {
            throw new ServiceException("更换导学单模板必须明确更换，已有学生答卷将继续保留在原快照中");
        }
        if (selectedSheetId == null)
        {
            throw new ServiceException("开启电子导学单时必须选择一份导学单");
        }

        BizGuideSheet source = accessService.requireSelectableTemplate(selectedSheetId);
        validateSnapshotContent(source.getFormJson());
        Date now = new Date();
        if (current != null)
        {
            if (bindingMapper.archiveCurrentByLessonId(lessonId, username, now) != 1)
            {
                throw new ServiceException("课程导学单已被其他操作更换，请刷新后重试");
            }
        }

        BizLessonGuideSheetBinding binding = snapshot(source, lessonId, userId, username, now);
        if (bindingMapper.insertBinding(binding) != 1)
        {
            throw new ServiceException("课程导学单快照创建失败，请重试");
        }
        if (bindingMapper.countCurrentByLessonId(lessonId) != 1)
        {
            throw new ServiceException("课程导学单绑定状态异常，请重试");
        }
        return binding;
    }

    public void assertLessonHasNoHistory(Long lessonId)
    {
        if (lessonId != null && bindingMapper.countByLessonId(lessonId) > 0)
        {
            throw new ServiceException("该课程存在电子导学单历史快照和答卷，不能物理删除");
        }
    }

    private void validateSnapshotContent(String formJson)
    {
        try
        {
            JsonNode root = OBJECT_MAPPER.readTree(formJson);
            JsonNode widgetList = root == null ? null : root.get("widgetList");
            if (widgetList == null || !widgetList.isArray() || widgetList.size() == 0)
            {
                throw new ServiceException("所选导学单还没有学习内容，请先完善模板");
            }
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("所选导学单内容已损坏，请先修复模板");
        }
    }

    private BizLessonGuideSheetBinding snapshot(BizGuideSheet source, Long lessonId, Long userId,
                                                 String username, Date now)
    {
        BizLessonGuideSheetBinding binding = new BizLessonGuideSheetBinding();
        binding.setLessonId(lessonId);
        binding.setSourceSheetId(source.getSheetId());
        binding.setSourceVersion(source.getVersionNo());
        binding.setSnapshotTitle(source.getSheetTitle());
        binding.setSnapshotGrade(source.getGrade());
        binding.setSnapshotSemester(source.getSemester());
        binding.setSnapshotLessonNum(source.getLessonNum());
        binding.setSnapshotFormJson(source.getFormJson());
        binding.setSnapshotMaxPages(source.getMaxPages());
        binding.setSnapshotTeacherMachineIp(source.getTeacherMachineIp());
        binding.setIsCurrent("Y");
        binding.setEnabled("Y");
        binding.setCreatorId(userId);
        binding.setCreateBy(username);
        binding.setCreateTime(now);
        return binding;
    }
}
