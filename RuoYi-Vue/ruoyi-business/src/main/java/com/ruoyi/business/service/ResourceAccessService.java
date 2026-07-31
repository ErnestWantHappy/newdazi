package com.ruoyi.business.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizQuestion;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.BizStudentAnswer;
import com.ruoyi.business.domain.BizTeacherClass;
import com.ruoyi.business.domain.CountyExamAnswer;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizQuestionMapper;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.BizTeacherClassMapper;
import com.ruoyi.business.mapper.CountyExamAnswerMapper;
import com.ruoyi.business.mapper.ResourceAccessMapper;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 本地文件读取授权服务。
 */
@Service
public class ResourceAccessService
{
    @Autowired
    private ResourceAccessMapper resourceAccessMapper;

    @Autowired
    private BizStudentAnswerMapper studentAnswerMapper;

    @Autowired
    private CountyExamAnswerMapper countyAnswerMapper;

    @Autowired
    private BizQuestionMapper questionMapper;

    @Autowired
    private BizStudentMapper studentMapper;

    @Autowired
    private BizTeacherClassMapper teacherClassMapper;

    @Autowired
    private BizLessonMapper lessonMapper;

    /**
     * 校验当前登录用户能否读取资源，并返回统一的数据库资源路径。
     */
    public String assertCanRead(String rawResource)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null)
        {
            throw new ServiceException("请先登录");
        }
        String resource = normalizeResource(rawResource);
        if (resource.toLowerCase(Locale.ROOT).startsWith("/profile/upload/guide-sheet/"))
        {
            throw new ServiceException("导学单作品只能通过专用接口读取");
        }

        Long answerId = resourceAccessMapper.selectStudentAnswerIdByResource(resource);
        if (answerId != null && answerId > 0)
        {
            assertStudentAnswerAccess(studentAnswerMapper.selectById(answerId), loginUser);
            return resource;
        }

        Long countyAnswerId = resourceAccessMapper.selectCountyAnswerIdByResource(resource);
        if (countyAnswerId != null && countyAnswerId > 0)
        {
            assertCountyAnswerAccess(countyAnswerMapper.selectById(countyAnswerId), loginUser);
            return resource;
        }

        Long questionId = resourceAccessMapper.selectQuestionIdByResource(resource);
        if (questionId != null && questionId > 0)
        {
            assertQuestionAccess(questionMapper.selectBizQuestionByQuestionId(questionId), loginUser);
            return resource;
        }

        if (resourceAccessMapper.countCountyQuestionResource(resource) > 0)
        {
            assertCountyQuestionAccess(resource, loginUser);
            return resource;
        }

        Map<String, Object> exemptionOwner =
                resourceAccessMapper.selectExemptionAttachmentOwner(resource);
        if (exemptionOwner != null)
        {
            assertExemptionAttachmentAccess(exemptionOwner, loginUser);
            return resource;
        }

        // 通用上传没有独立归属表。只给课程内容维护角色读取未落业务表的草稿文件，学生一律拒绝。
        if (isAdmin(loginUser) || hasRole(loginUser, "teacher") || hasRole(loginUser, "researcher"))
        {
            return resource;
        }
        throw new ServiceException("无权访问该文件");
    }

    private void assertExemptionAttachmentAccess(Map<String, Object> owner, LoginUser loginUser)
    {
        if (isAdmin(loginUser) || hasRole(loginUser, "researcher"))
        {
            return;
        }
        Long teacherId = numberAsLong(owner.get("teacherId"));
        Long deptId = numberAsLong(owner.get("deptId"));
        if (hasRole(loginUser, "teacher")
                && loginUser.getUserId().equals(teacherId)
                && sameDept(loginUser.getDeptId(), deptId))
        {
            return;
        }
        throw new ServiceException("无权访问该免抽测申请附件");
    }

    private void assertStudentAnswerAccess(BizStudentAnswer answer, LoginUser loginUser)
    {
        if (answer == null)
        {
            throw new ServiceException("文件记录不存在");
        }
        BizStudent owner = studentMapper.selectBizStudentByStudentId(answer.getStudentId());
        BizStudent currentStudent = studentMapper.selectBizStudentByUserId(loginUser.getUserId());
        if (currentStudent != null && currentStudent.getStudentId().equals(answer.getStudentId()))
        {
            return;
        }
        if (isAdmin(loginUser))
        {
            return;
        }
        // 教研员监管接口已做只读权限校验，资源层允许其跨校预览学生作品。
        if (hasRole(loginUser, "researcher"))
        {
            return;
        }
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(answer.getLessonId());
        if (owner == null || lesson == null || !sameDept(loginUser.getDeptId(), owner.getDeptId())
                || !sameDept(loginUser.getDeptId(), lesson.getDeptId()))
        {
            throw new ServiceException("无权访问该学生作品");
        }
        BizTeacherClass teacherClass = new BizTeacherClass();
        teacherClass.setUserId(loginUser.getUserId());
        teacherClass.setDeptId(loginUser.getDeptId());
        teacherClass.setEntryYear(owner.getEntryYear());
        teacherClass.setClassCode(owner.getClassCode());
        if (teacherClassMapper.checkTeacherClassExists(teacherClass) <= 0)
        {
            throw new ServiceException("无权访问该学生作品");
        }
    }

    private void assertCountyAnswerAccess(CountyExamAnswer answer, LoginUser loginUser)
    {
        if (answer == null)
        {
            throw new ServiceException("文件记录不存在");
        }
        BizStudent currentStudent = studentMapper.selectBizStudentByUserId(loginUser.getUserId());
        if (currentStudent != null && currentStudent.getStudentId().equals(answer.getStudentId()))
        {
            return;
        }
        if (isAdmin(loginUser))
        {
            return;
        }
        if (loginUser.getUserId().equals(answer.getGraderId())
                && resourceAccessMapper.countCountyAnswerForActiveGrader(
                        answer.getAnswerId(), loginUser.getUserId()) > 0)
        {
            return;
        }
        throw new ServiceException("无权访问该区域抽测作品");
    }

    private void assertQuestionAccess(BizQuestion question, LoginUser loginUser)
    {
        if (question == null)
        {
            throw new ServiceException("题目资源不存在");
        }
        if (isAdmin(loginUser) || "Y".equalsIgnoreCase(question.getIsPublic())
                || loginUser.getUserId().equals(question.getCreatorId()))
        {
            return;
        }
        BizStudent currentStudent = studentMapper.selectBizStudentByUserId(loginUser.getUserId());
        if (currentStudent != null
                && resourceAccessMapper.countCurrentLessonQuestionForStudent(
                        currentStudent.getStudentId(), question.getQuestionId()) > 0)
        {
            return;
        }
        throw new ServiceException("无权访问该题目资源");
    }

    private void assertCountyQuestionAccess(String resource, LoginUser loginUser)
    {
        if (isAdmin(loginUser) || hasRole(loginUser, "researcher"))
        {
            return;
        }
        BizStudent currentStudent = studentMapper.selectBizStudentByUserId(loginUser.getUserId());
        if (currentStudent != null
                && resourceAccessMapper.countCountyQuestionResourceForStudent(
                        resource, currentStudent.getStudentId()) > 0)
        {
            return;
        }
        if (resourceAccessMapper.countCountyQuestionResourceForGrader(resource, loginUser.getUserId()) > 0)
        {
            return;
        }
        throw new ServiceException("无权访问该区域抽测题目资源");
    }

    private String normalizeResource(String rawResource)
    {
        if (StringUtils.isEmpty(rawResource))
        {
            throw new ServiceException("资源路径不能为空");
        }
        String value = rawResource;
        for (int i = 0; i < 2; i++)
        {
            try
            {
                String decoded = URLDecoder.decode(value, StandardCharsets.UTF_8.name());
                if (decoded.equals(value))
                {
                    break;
                }
                value = decoded;
            }
            catch (Exception e)
            {
                throw new ServiceException("资源路径格式错误");
            }
        }
        value = value.replace('\\', '/');
        int profileIndex = value.toLowerCase(Locale.ROOT).indexOf("/profile/");
        if (profileIndex < 0)
        {
            throw new ServiceException("资源路径非法");
        }
        String normalized = value.substring(profileIndex).replaceAll("/{2,}", "/");
        if (normalized.contains("../") || normalized.endsWith("/.."))
        {
            throw new ServiceException("资源路径非法");
        }
        return normalized;
    }

    private boolean isAdmin(LoginUser loginUser)
    {
        return loginUser.getUser() != null && loginUser.getUser().isAdmin();
    }

    private boolean hasRole(LoginUser loginUser, String roleKey)
    {
        return loginUser.getUser() != null
                && loginUser.getUser().getRoles() != null
                && loginUser.getUser().getRoles().stream()
                        .anyMatch(role -> roleKey.equals(role.getRoleKey()));
    }

    private boolean sameDept(Long left, Long right)
    {
        return left != null && left.equals(right);
    }

    private Long numberAsLong(Object value)
    {
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        return value == null ? null : Long.valueOf(String.valueOf(value));
    }
}
