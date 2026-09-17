package com.ruoyi.business.service.impl;

import java.util.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.BizGuideSheet;
import com.ruoyi.business.domain.vo.GuideSheetProgressVo;
import com.ruoyi.business.domain.vo.GuideSheetVo;
import com.ruoyi.business.mapper.GuideSheetMapper;
import com.ruoyi.business.mapper.GuideSheetProgressMapper;
import com.ruoyi.business.service.GuideSheetAccessService;
import com.ruoyi.business.service.IGuideSheetService;
import com.ruoyi.business.service.OrganizationBoundaryService;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuideSheetServiceImpl implements IGuideSheetService
{
    private static final int MAX_FORM_JSON_LENGTH = 2 * 1024 * 1024;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private GuideSheetMapper guideSheetMapper;

    @Autowired
    private GuideSheetProgressMapper guideSheetProgressMapper;

    @Autowired
    private GuideSheetAccessService accessService;

    @Autowired
    private OrganizationBoundaryService organizationBoundaryService;

    @Override
    public BizGuideSheet getBySheetId(Long sheetId)
    {
        return guideSheetMapper.selectBizGuideSheetBySheetId(sheetId);
    }

    @Override
    public GuideSheetVo selectGuideSheetDetail(Long sheetId)
    {
        BizGuideSheet sheet = accessService.requireVisibleTemplate(sheetId);
        GuideSheetVo vo = new GuideSheetVo();
        BeanUtils.copyProperties(sheet, vo);
        return vo;
    }

    @Override
    public List<BizGuideSheet> selectBizGuideSheetList(BizGuideSheet query)
    {
        Long userId = SecurityUtils.getUserId();
        query.getParams().put("viewerId", userId);
        query.getParams().put("countyDeptId",
                organizationBoundaryService.resolveCountyDeptId(SecurityUtils.getDeptId()));
        query.getParams().put("bypassVisibility", SecurityUtils.isAdmin(userId));
        return guideSheetMapper.selectBizGuideSheetList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GuideSheetVo saveGuideSheetDetail(GuideSheetVo vo)
    {
        validateTemplate(vo);
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Date now = new Date();
        if (vo.getSheetId() == null)
        {
            BizGuideSheet sheet = new BizGuideSheet();
            copyEditableFields(vo, sheet);
            sheet.setCreatorId(loginUser.getUserId());
            sheet.setDeptId(loginUser.getDeptId());
            sheet.setCountyDeptId(organizationBoundaryService.resolveCountyDeptId(loginUser.getDeptId()));
            sheet.setVersionNo(1);
            sheet.setDelFlag("0");
            sheet.setCreateBy(loginUser.getUsername());
            sheet.setCreateTime(now);
            guideSheetMapper.insertBizGuideSheet(sheet);
            BeanUtils.copyProperties(sheet, vo);
            return vo;
        }

        accessService.assertCanManageTemplate(vo.getSheetId());
        BizGuideSheet existing = guideSheetMapper.selectBizGuideSheetBySheetId(vo.getSheetId());
        if (existing == null || !"0".equals(existing.getDelFlag()))
        {
            throw new ServiceException("已归档的导学单不能继续编辑");
        }
        if (vo.getVersionNo() == null || !vo.getVersionNo().equals(existing.getVersionNo()))
        {
            throw new ServiceException("导学单已被其他页面修改，请刷新后重试");
        }
        BizGuideSheet update = new BizGuideSheet();
        update.setSheetId(existing.getSheetId());
        // 必须使用页面读取时的版本做 CAS，禁止用刚查询到的新版本掩盖并发覆盖。
        update.setVersionNo(vo.getVersionNo());
        copyEditableFields(vo, update);
        update.setUpdateBy(loginUser.getUsername());
        update.setUpdateTime(now);
        if (guideSheetMapper.updateBizGuideSheet(update) != 1)
        {
            throw new ServiceException("导学单已被其他用户修改，请刷新后重试");
        }
        BizGuideSheet saved = guideSheetMapper.selectBizGuideSheetBySheetId(vo.getSheetId());
        BeanUtils.copyProperties(saved, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int archiveGuideSheet(Long sheetId)
    {
        accessService.assertCanManageTemplate(sheetId);
        return guideSheetMapper.archiveBySheetId(sheetId, SecurityUtils.getUsername(), new Date());
    }

    @Override
    public List<GuideSheetProgressVo> getProgress(Long bindingId, Long deptId, String entryYear,
                                                   String classCode)
    {
        List<GuideSheetProgressVo> currentRoster = guideSheetProgressMapper.selectFullProgressByBindingAndClass(
                bindingId, deptId, entryYear, classCode);
        List<GuideSheetProgressVo> historicalRows = guideSheetProgressMapper.selectByBindingAndClass(
                bindingId, deptId, entryYear, classCode);
        if (currentRoster.isEmpty())
        {
            return historicalRows;
        }
        List<GuideSheetProgressVo> merged = new ArrayList<>(currentRoster);
        Set<Long> currentStudentIds = new HashSet<>();
        for (GuideSheetProgressVo row : currentRoster)
        {
            if (row.getStudentId() != null)
            {
                currentStudentIds.add(row.getStudentId());
            }
        }
        for (GuideSheetProgressVo row : historicalRows)
        {
            if (row.getStudentId() == null || currentStudentIds.add(row.getStudentId()))
            {
                // 转班或移出当前名单的学生仍需出现在原班级历史成绩中。
                merged.add(row);
            }
        }
        return merged;
    }

    @Override
    public List<Map<String, Object>> getCreatorList()
    {
        Long countyDeptId = organizationBoundaryService.resolveCountyDeptId(SecurityUtils.getDeptId());
        return guideSheetMapper.selectCreatorList(countyDeptId);
    }

    private void copyEditableFields(GuideSheetVo source, BizGuideSheet target)
    {
        target.setSheetTitle(StringUtils.trim(source.getSheetTitle()));
        target.setGrade(source.getGrade());
        target.setSemester(source.getSemester());
        target.setLessonNum(source.getLessonNum());
        target.setIsPublic(source.getIsPublic());
        target.setFormJson(stripLegacySecrets(source.getFormJson()));
        target.setMaxPages(source.getMaxPages());
        target.setTeacherMachineIp(source.getTeacherMachineIp());
    }

    private void validateTemplate(GuideSheetVo vo)
    {
        if (vo == null || StringUtils.isBlank(vo.getSheetTitle()))
        {
            throw new ServiceException("导学单标题不能为空");
        }
        if (vo.getGrade() == null || StringUtils.isBlank(vo.getSemester())
                || vo.getLessonNum() == null || vo.getLessonNum() <= 0)
        {
            throw new ServiceException("年级、学期和课次必须完整填写");
        }
        if (!"Y".equals(vo.getIsPublic()) && !"N".equals(vo.getIsPublic()))
        {
            throw new ServiceException("请选择导学单是否公开");
        }
        validateFormStructure(vo.getFormJson());
    }

    private void validateFormStructure(String formJson)
    {
        if (StringUtils.isBlank(formJson))
        {
            throw new ServiceException("请至少添加一个教学模块后再保存");
        }
        if (formJson.getBytes(StandardCharsets.UTF_8).length > MAX_FORM_JSON_LENGTH)
        {
            throw new ServiceException("导学单内容过大，请精简模块或改用文件提交");
        }
        try
        {
            JsonNode root = objectMapper.readTree(formJson);
            JsonNode widgetList = root == null ? null : root.get("widgetList");
            if (widgetList == null || !widgetList.isArray() || widgetList.size() == 0)
            {
                throw new ServiceException("请至少添加一个教学模块后再保存");
            }
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("导学单表单JSON格式无效");
        }
    }

    private String stripLegacySecrets(String formJson)
    {
        if (StringUtils.isBlank(formJson))
        {
            return formJson;
        }
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
            throw new ServiceException("导学单表单JSON格式无效");
        }
    }
}
