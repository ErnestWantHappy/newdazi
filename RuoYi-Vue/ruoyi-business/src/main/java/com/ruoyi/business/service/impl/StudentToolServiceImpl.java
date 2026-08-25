package com.ruoyi.business.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.BizLessonTool;
import com.ruoyi.business.domain.BizStudentTool;
import com.ruoyi.business.domain.BizStudentToolScope;
import com.ruoyi.business.domain.vo.StudentToolScopeGroup;
import com.ruoyi.business.mapper.StudentToolMapper;
import com.ruoyi.business.service.StudentToolService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * 学生实验工具服务实现。
 * 常驻工具按学校隔离（deptId），教师只能管理本校工具；学生端按 学校+年级+班级 匹配。
 */
@Service
public class StudentToolServiceImpl implements StudentToolService
{
    private static final Logger log = LoggerFactory.getLogger(StudentToolServiceImpl.class);

    @Autowired
    private StudentToolMapper mapper;

    @Override
    public List<BizStudentTool> listTools(Long deptId, String keyword)
    {
        BizStudentTool query = new BizStudentTool();
        query.setDeptId(deptId);
        query.setToolName(keyword);
        return mapper.selectStudentToolList(query);
    }

    @Override
    public BizStudentTool getTool(Long deptId, Long toolId)
    {
        BizStudentTool tool = mapper.selectStudentToolByToolId(toolId);
        if (tool == null || !isOwned(tool, deptId))
        {
            return null;
        }
        return tool;
    }

    @Override
    @Transactional
    public BizStudentTool createTool(Long deptId, BizStudentTool tool, List<StudentToolScopeGroup> scopes)
    {
        tool.setToolId(null);
        tool.setDeptId(deptId);
        if (tool.getEnabled() == null) { tool.setEnabled(1); }
        if (tool.getSortOrder() == null) { tool.setSortOrder(0); }
        if (StringUtils.isBlank(tool.getToolName()) || StringUtils.isBlank(tool.getToolUrl()))
        {
            throw new ServiceException("工具名称和网址不能为空");
        }
        mapper.insertStudentTool(tool);
        replaceScopes(tool.getToolId(), scopes, deptId);
        return tool;
    }

    @Override
    @Transactional
    public BizStudentTool updateTool(Long deptId, BizStudentTool tool, List<StudentToolScopeGroup> scopes)
    {
        if (tool.getToolId() == null)
        {
            throw new ServiceException("缺少工具ID");
        }
        BizStudentTool existing = mapper.selectStudentToolByToolId(tool.getToolId());
        if (existing == null || !isOwned(existing, deptId))
        {
            throw new ServiceException("工具不存在或无权限修改");
        }
        if (StringUtils.isBlank(tool.getToolName()) || StringUtils.isBlank(tool.getToolUrl()))
        {
            throw new ServiceException("工具名称和网址不能为空");
        }
        tool.setDeptId(null); // 防止跨校改归属
        mapper.updateStudentTool(tool);
        if (scopes != null)
        {
            replaceScopes(tool.getToolId(), scopes, deptId);
        }
        return mapper.selectStudentToolByToolId(tool.getToolId());
    }

    @Override
    @Transactional
    public void deleteTools(Long deptId, Long[] toolIds)
    {
        if (toolIds == null || toolIds.length == 0) { return; }
        // 逐个校验归属，防止越权批量删除
        for (Long toolId : toolIds)
        {
            BizStudentTool existing = mapper.selectStudentToolByToolId(toolId);
            if (existing != null && isOwned(existing, deptId))
            {
                mapper.deleteScopesByToolId(toolId);
                mapper.deleteStudentToolByToolId(toolId);
            }
        }
    }

    @Override
    public Map<String, Object> getToolsForStudent(Long deptId, String entryYear, String classCode, Long lessonId)
    {
        Map<String, Object> result = new HashMap<>();
        List<BizLessonTool> lessonTools = new ArrayList<>();
        if (lessonId != null)
        {
            lessonTools = mapper.selectLessonToolsByLessonId(lessonId);
        }
        List<BizStudentTool> residentTools = mapper.selectResidentToolsForStudent(deptId, entryYear, classCode);
        result.put("lessonTools", lessonTools);
        result.put("residentTools", residentTools);
        return result;
    }

    @Override
    public List<BizLessonTool> getLessonTools(Long lessonId)
    {
        return mapper.selectLessonToolsByLessonId(lessonId);
    }

    @Override
    @Transactional
    public void replaceLessonTools(Long lessonId, List<BizLessonTool> tools)
    {
        if (lessonId == null) { throw new ServiceException("缺少课程ID"); }
        mapper.deleteLessonToolsByLessonId(lessonId);
        if (tools == null) { return; }
        int order = 0;
        Set<String> seen = new HashSet<>();
        for (BizLessonTool tool : tools)
        {
            if (tool == null || StringUtils.isBlank(tool.getToolName()) || StringUtils.isBlank(tool.getToolUrl()))
            {
                continue;
            }
            String key = tool.getToolName().trim() + "|" + tool.getToolUrl().trim();
            if (!seen.add(key)) { continue; } // 防重复行
            tool.setToolId(null);
            tool.setLessonId(lessonId);
            tool.setSortOrder(order++);
            mapper.insertLessonTool(tool);
        }
    }

    /** 适用范围全量替换：按年级分组展开为 (tool, entry_year, class_code) 行；class_code 空=全年级。 */
    private void replaceScopes(Long toolId, List<StudentToolScopeGroup> scopes, Long deptId)
    {
        mapper.deleteScopesByToolId(toolId);
        if (scopes == null) { return; }
        Set<String> seen = new HashSet<>();
        for (StudentToolScopeGroup group : scopes)
        {
            if (group == null || StringUtils.isBlank(group.getEntryYear())) { continue; }
            String entryYear = group.getEntryYear().trim();
            if (Boolean.TRUE.equals(group.getAllGrade()))
            {
                if (seen.add(entryYear + "|*"))
                {
                    BizStudentToolScope scope = new BizStudentToolScope();
                    scope.setToolId(toolId);
                    scope.setEntryYear(entryYear);
                    scope.setClassCode(null);
                    mapper.insertScope(scope);
                }
            }
            else if (group.getClassCodes() != null)
            {
                for (String classCode : group.getClassCodes())
                {
                    if (StringUtils.isBlank(classCode)) { continue; }
                    if (seen.add(entryYear + "|" + classCode.trim()))
                    {
                        BizStudentToolScope scope = new BizStudentToolScope();
                        scope.setToolId(toolId);
                        scope.setEntryYear(entryYear);
                        scope.setClassCode(classCode.trim());
                        mapper.insertScope(scope);
                    }
                }
            }
        }
    }

    /** 工具归属校验：平台级（deptId 空）仅管理员可改；学校级须同本校。 */
    private boolean isOwned(BizStudentTool tool, Long deptId)
    {
        if (tool.getDeptId() == null) { return false; } // 平台级工具不允许教师在本校列表直接修改
        return tool.getDeptId().equals(deptId);
    }
}
