package com.ruoyi.business.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.framework.web.service.PermissionService;

/**
 * 学生教材访问边界。
 *
 * 教师、教研员和管理员保留整册阅读能力；学生只能打开当前班级课程绑定的教材，
 * 不能仅靠修改 URL 中的 bookId 读取其他年级或学校的教材。
 */
@Service
public class TextbookStudentAccessService
{
    private static final String STAFF_ROLES = "admin,teacher,researcher";

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private BizStudentMapper studentMapper;

    @Autowired
    private BizLessonAssignmentMapper lessonAssignmentMapper;

    @Autowired
    private BizLessonMapper lessonMapper;

    public void assertCanReadBook(Long bookId)
    {
        if (permissionService.hasAnyRoles(STAFF_ROLES))
        {
            return;
        }
        assertStudentBookBinding(bookId, SecurityUtils.getUserId(), SecurityUtils.getDeptId());
    }

    void assertStudentBookBinding(Long bookId, Long userId, Long deptId)
    {
        BizStudent student = studentMapper.selectBizStudentByUserId(userId);
        if (student == null)
        {
            throw new ServiceException("仅可阅读当前课程绑定的教材");
        }
        Long currentLessonId = lessonAssignmentMapper.selectCurrentLessonByClass(
                student.getEntryYear(), student.getClassCode(), deptId);
        if (currentLessonId == null)
        {
            throw new ServiceException("当前课程未绑定教材");
        }
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(currentLessonId);
        if (lesson == null || deptId == null || !deptId.equals(lesson.getDeptId())
                || bookId == null || !bookId.equals(lesson.getTextbookBookId()))
        {
            throw new ServiceException("仅可阅读当前课程绑定的教材");
        }
    }
}
