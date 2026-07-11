package com.ruoyi.business.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.ruoyi.business.domain.BizGuideSheet;
import com.ruoyi.business.domain.BizGuideSheetAnswer;
import com.ruoyi.business.domain.BizGuideSheetAssignment;
import com.ruoyi.business.domain.BizGuideSheetProgress;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.vo.GuideSheetProgressVo;
import com.ruoyi.business.domain.vo.GuideSheetVo;
import com.ruoyi.business.mapper.*;
import com.ruoyi.business.service.IGuideSheetService;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.SysDeptMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
public class GuideSheetServiceImpl implements IGuideSheetService
{
    private static final Logger log = LoggerFactory.getLogger(GuideSheetServiceImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private GuideSheetMapper guideSheetMapper;

    @Autowired
    private GuideSheetAssignmentMapper guideSheetAssignmentMapper;

    @Autowired
    private GuideSheetAnswerMapper guideSheetAnswerMapper;

    @Autowired
    private GuideSheetProgressMapper guideSheetProgressMapper;

    @Autowired
    private BizStudentMapper bizStudentMapper;

    @Autowired
    private SysDeptMapper deptMapper;

    @Override
    public BizGuideSheet getBySheetId(Long sheetId)
    {
        return guideSheetMapper.selectBizGuideSheetBySheetId(sheetId);
    }

    @Override
    public GuideSheetVo selectGuideSheetDetail(Long sheetId)
    {
        GuideSheetVo vo = new GuideSheetVo();
        BizGuideSheet sheet = guideSheetMapper.selectBizGuideSheetBySheetId(sheetId);
        if (sheet == null) return null;

        vo.setSheetId(sheet.getSheetId());
        vo.setSheetTitle(sheet.getSheetTitle());
        vo.setLessonId(sheet.getLessonId());
        vo.setCreatorId(sheet.getCreatorId());
        vo.setDeptId(sheet.getDeptId());
        vo.setFormJson(sheet.getFormJson());
        vo.setStatus(sheet.getStatus());
        vo.setMaxPages(sheet.getMaxPages());
        vo.setTeacherMachineIp(sheet.getTeacherMachineIp());
        vo.setIsPublic(sheet.getIsPublic());

        List<String> assignedClassCodes = guideSheetAssignmentMapper.selectClassCodesBySheetId(sheetId);
        if (!CollectionUtils.isEmpty(assignedClassCodes))
        {
            assignedClassCodes = assignedClassCodes.stream()
                    .filter(StringUtils::isNotBlank)
                    .map(code -> code.endsWith("班") ? code : code + "班")
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
        }
        vo.setAssignedClassCodes(assignedClassCodes);

        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long deptId = null;
        if (loginUser != null && loginUser.getUser() != null)
        {
            deptId = loginUser.getUser().getDeptId();
        }
        if (deptId != null)
        {
            List<BizStudent> students = bizStudentMapper.selectDistinctYearAndClassByDeptId(deptId);
            List<String> allClasses = students.stream()
                    .filter(s -> s.getClassCode() != null)
                    .map(s -> s.getClassCode() + "班")
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            vo.setAllClassesInGrade(allClasses);
        }

        return vo;
    }

    @Override
    public List<BizGuideSheet> selectBizGuideSheetList(BizGuideSheet bizGuideSheet)
    {
        return guideSheetMapper.selectBizGuideSheetList(bizGuideSheet);
    }

    @Override
    public int insertBizGuideSheet(BizGuideSheet bizGuideSheet)
    {
        if (bizGuideSheet.getCreateTime() == null)
        {
            bizGuideSheet.setCreateTime(new Date());
        }
        return guideSheetMapper.insertBizGuideSheet(bizGuideSheet);
    }

    @Override
    public int updateBizGuideSheet(BizGuideSheet bizGuideSheet)
    {
        return guideSheetMapper.updateBizGuideSheet(bizGuideSheet);
    }

    @Override
    @Transactional
    public int deleteBizGuideSheetBySheetIds(Long[] sheetIds)
    {
        for (Long sheetId : sheetIds)
        {
            // 级联删除：先删子表（答案、指派、进度），最后删主表
            guideSheetAnswerMapper.deleteBySheetId(sheetId);
            guideSheetAssignmentMapper.deleteBySheetId(sheetId);
            guideSheetProgressMapper.deleteBySheetId(sheetId);
        }
        return guideSheetMapper.deleteBizGuideSheetBySheetIds(sheetIds);
    }

    @Override
    @Transactional
    public GuideSheetVo saveGuideSheetDetail(GuideSheetVo vo)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        String username = loginUser.getUsername();
        Long deptId = loginUser.getUser().getDeptId();

        BizGuideSheet sheet = new BizGuideSheet();
        sheet.setSheetId(vo.getSheetId());
        sheet.setSheetTitle(vo.getSheetTitle());
        sheet.setLessonId(vo.getLessonId());
        sheet.setCreatorId(loginUser.getUserId());
        sheet.setDeptId(deptId);
        sheet.setFormJson(stripLegacySecrets(vo.getFormJson()));
        sheet.setStatus(vo.getStatus() != null ? vo.getStatus() : "0");
        sheet.setMaxPages(vo.getMaxPages());
        sheet.setTeacherMachineIp(vo.getTeacherMachineIp());
        sheet.setIsPublic(vo.getIsPublic() != null ? vo.getIsPublic() : "Y");

        if (sheet.getSheetId() == null)
        {
            sheet.setCreateBy(username);
            sheet.setCreateTime(new Date());
            guideSheetMapper.insertBizGuideSheet(sheet);
        }
        else
        {
            sheet.setUpdateBy(username);
            guideSheetMapper.updateBizGuideSheet(sheet);
        }
        Long sheetId = sheet.getSheetId();
        vo.setSheetId(sheetId);

        guideSheetAssignmentMapper.deleteBySheetId(sheetId);
        List<String> classCodes = vo.getAssignedClassCodes();
        if (!CollectionUtils.isEmpty(classCodes))
        {
            String entryYear = calculateEntryYear(deptId);
            List<BizGuideSheetAssignment> assignments = new ArrayList<>();
            for (String classCode : classCodes)
            {
                if (StringUtils.isBlank(classCode)) continue;
                String pureClassCode = classCode.replace("班", "").trim();
                BizGuideSheetAssignment assignment = new BizGuideSheetAssignment();
                assignment.setSheetId(sheetId);
                assignment.setDeptId(deptId);
                assignment.setClassCode(pureClassCode);
                assignment.setEntryYear(entryYear);
                assignment.setAssignTime(new Date());
                assignments.add(assignment);
            }
            if (!assignments.isEmpty())
            {
                guideSheetAssignmentMapper.batchInsert(assignments);
            }
        }

        return vo;
    }

    @Override
    public int publishGuideSheet(Long sheetId)
    {
        BizGuideSheet existing = guideSheetMapper.selectBizGuideSheetBySheetId(sheetId);
        if (existing == null) return 0;
        if (!"0".equals(existing.getStatus()))
        {
            log.warn("导学单发布失败：当前状态不是草稿 sheetId={} status={}", sheetId, existing.getStatus());
            return 0;
        }
        if (existing.getFormJson() == null || existing.getFormJson().isEmpty())
        {
            log.warn("导学单发布失败：表单内容为空 sheetId={}", sheetId);
            return 0;
        }
        List<String> assignedClasses = guideSheetAssignmentMapper.selectClassCodesBySheetId(sheetId);
        if (assignedClasses == null || assignedClasses.isEmpty())
        {
            log.warn("导学单发布失败：未指派班级 sheetId={}", sheetId);
            return 0;
        }
        BizGuideSheet sheet = new BizGuideSheet();
        sheet.setSheetId(sheetId);
        sheet.setStatus("1");
        return guideSheetMapper.updateBizGuideSheet(sheet);
    }

    @Override
    public int closeGuideSheet(Long sheetId)
    {
        BizGuideSheet sheet = new BizGuideSheet();
        sheet.setSheetId(sheetId);
        sheet.setStatus("2");
        return guideSheetMapper.updateBizGuideSheet(sheet);
    }

    @Override
    public GuideSheetVo getStudentGuideSheet(Long deptId, String entryYear, String classCode)
    {
        Long sheetId = guideSheetAssignmentMapper.selectCurrentSheetByClass(entryYear, classCode, deptId);
        if (sheetId == null)
        {
            return null;
        }
        return selectGuideSheetDetail(sheetId);
    }

    @Override
    public List<GuideSheetProgressVo> getProgress(Long sheetId, String classCode)
    {
        if (classCode != null && !classCode.isEmpty())
        {
            return guideSheetProgressMapper.selectFullProgressBySheetAndClass(sheetId, classCode);
        }
        return guideSheetProgressMapper.selectFullProgressBySheetId(sheetId);
    }

    private String calculateEntryYear(Long deptId)
    {
        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH) + 1;
        int currentDay = now.get(Calendar.DAY_OF_MONTH);
        int academicStartYear = currentYear;
        if (currentMonth < 7 || (currentMonth == 7 && currentDay < 20))
        {
            academicStartYear = currentYear - 1;
        }
        return String.valueOf(academicStartYear);
    }

    @Override
    public List<Map<String, Object>> getCreatorList(Long deptId)
    {
        return guideSheetMapper.selectCreatorList(deptId);
    }

    @Override
    public List<String> getAssignedClasses(Long sheetId)
    {
        return guideSheetAssignmentMapper.selectClassCodesBySheetId(sheetId);
    }

    private String stripLegacySecrets(String formJson)
    {
        if (StringUtils.isBlank(formJson)) return formJson;
        try
        {
            Map<String, Object> form = objectMapper.readValue(formJson,
                    new TypeReference<Map<String, Object>>() { });
            form.remove("_aiApiKey");
            form.remove("_aiProvider");
            form.remove("_aiModel");
            form.remove("_aiCustomUrl");
            return objectMapper.writeValueAsString(form);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("导学单表单JSON格式无效", e);
        }
    }
}
