package com.ruoyi.business.service.impl;

import java.util.Arrays;
import java.util.List;
import com.ruoyi.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.BizTeacherClass;
import com.ruoyi.business.service.IBizStudentService;
import com.ruoyi.business.service.IBizTeacherClassService;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysUserRole;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.mapper.SysUserRoleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 学生管理Service业务层处理 (编译修复版)
 */
@Service
public class BizStudentServiceImpl implements IBizStudentService
{
    private static final Logger log = LoggerFactory.getLogger(BizStudentServiceImpl.class);
    @Autowired
    private BizStudentMapper bizStudentMapper;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private SysUserRoleMapper userRoleMapper;
    @Autowired
    private SysDeptMapper deptMapper;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private IBizTeacherClassService teacherClassService;

    @Override
    public BizStudent selectBizStudentByStudentId(Long studentId)
    {
        return bizStudentMapper.selectBizStudentByStudentId(studentId);
    }

    @Override
    public List<BizStudent> selectBizStudentList(BizStudent bizStudent)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long currentDeptId = null;
        Long currentUserId = null;
        if (loginUser != null && loginUser.getUser() != null)
        {
            currentDeptId = loginUser.getUser().getDeptId();
            currentUserId = loginUser.getUserId();
        }
        // 非超级管理员需要关联教师班级表过滤
        if (loginUser != null && loginUser.getUser() != null && !loginUser.getUser().isAdmin())
        {
            bizStudent.setDeptId(currentDeptId);
            bizStudent.setTeacherUserId(currentUserId);
        }
        else if (bizStudent.getDeptId() == null && currentDeptId != null)
        {
            // 管理员未指定校区时默认沿用当前选中的校区
            bizStudent.setDeptId(currentDeptId);
            bizStudent.setTeacherUserId(currentUserId);
        }
        return bizStudentMapper.selectBizStudentList(bizStudent);
    }

    @Override
    @Transactional
    public int insertBizStudent(BizStudent bizStudent)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long teacherDeptId = loginUser.getUser().getDeptId();
        String operName = loginUser.getUsername();
        if (StringUtils.isNull(teacherDeptId)) {
            throw new ServiceException("操作的教师账号没有关联学校，无法新增学生！");
        }

        SysDept school = deptMapper.selectDeptById(teacherDeptId);
        if (school == null || StringUtils.isEmpty(school.getSchoolCode())) {
            throw new ServiceException("教师关联的学校信息不完整，缺少学校官方ID (school_code)");
        }
        String schoolCode = school.getSchoolCode();

        // 核心编译错误修复：方法名 padl 应为 leftPad
        String formattedClassCode = StringUtils.leftPad(bizStudent.getClassCode(), 2, '0');
        // 核心编译错误修复：方法名 padl 应为 leftPad
        String formattedStudentNo = StringUtils.leftPad(bizStudent.getStudentNo(), 2, '0');
        String generatedUserName = bizStudent.getEntryYear() + schoolCode + formattedClassCode + formattedStudentNo;

        if (userMapper.checkUserNameUnique(generatedUserName) != null)
        {
            throw new ServiceException("生成登录账号 " + generatedUserName + " 已存在，请检查入学年份、班级和学号");
        }

        SysUser newUser = new SysUser();
        newUser.setDeptId(teacherDeptId);
        newUser.setUserName(generatedUserName);
        newUser.setNickName(bizStudent.getStudentName());
        newUser.setPassword(SecurityUtils.encryptPassword("123456"));
        newUser.setCreateBy(operName);
        userMapper.insertUser(newUser);

        SysUserRole ur = new SysUserRole();
        ur.setUserId(newUser.getUserId());
        ur.setRoleId(4L);
        userRoleMapper.batchUserRole(Arrays.asList(ur));

        bizStudent.setUserId(newUser.getUserId());
        int result = bizStudentMapper.insertBizStudent(bizStudent);
        
        // 自动将该班级添加到教师的管理班级中
        autoAssignTeacherClass(loginUser.getUserId(), teacherDeptId, bizStudent.getEntryYear(), bizStudent.getClassCode());
        
        return result;
    }

    @Override
    @Transactional
    public int updateBizStudent(BizStudent bizStudent)
    {
        SysUser userUpdate = new SysUser();
        userUpdate.setUserId(bizStudent.getUserId());
        userUpdate.setNickName(bizStudent.getStudentName());
        userMapper.updateUser(userUpdate);

        return bizStudentMapper.updateBizStudent(bizStudent);
    }

    @Override
    @Transactional
    public int deleteBizStudentByStudentIds(Long[] studentIds)
    {
        for (Long studentId : studentIds) {
            BizStudent student = bizStudentMapper.selectBizStudentByStudentId(studentId);
            if (student != null && student.getUserId() != null) {
                userMapper.deleteUserById(student.getUserId());
                userRoleMapper.deleteUserRoleByUserId(student.getUserId());
            }
        }
        return bizStudentMapper.deleteBizStudentByStudentIds(studentIds);
    }

    @Override
    @Transactional
    public int deleteBizStudentByStudentId(Long studentId)
    {
        BizStudent student = bizStudentMapper.selectBizStudentByStudentId(studentId);
        if (student != null && student.getUserId() != null) {
            userMapper.deleteUserById(student.getUserId());
        }
        return bizStudentMapper.deleteBizStudentByStudentId(studentId);
    }

    @Override
    @Transactional
    public String importStudent(List<BizStudent> studentList, String operName)
    {
        if (StringUtils.isNull(studentList) || studentList.size() == 0)
        {
            throw new ServiceException("导入学生数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();

        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long teacherDeptId = loginUser.getUser().getDeptId();
        if (StringUtils.isNull(teacherDeptId)) {
            throw new ServiceException("操作的教师账号没有关联学校，无法导入学生！");
        }

        SysDept school = deptMapper.selectDeptById(teacherDeptId);
        if (school == null || StringUtils.isEmpty(school.getSchoolCode())) {
            throw new ServiceException("教师关联的学校信息不完整，缺少学校官方ID (school_code)");
        }
        String schoolCode = school.getSchoolCode();

        for (BizStudent student : studentList)
        {
            try
            {
                // 核心编译错误修复：方法名 padl 应为 leftPad
                String formattedClassCode = StringUtils.leftPad(student.getClassCode(), 2, '0');
                // 核心编译错误修复：方法名 padl 应为 leftPad
                String formattedStudentNo = StringUtils.leftPad(student.getStudentNo(), 2, '0');
                String generatedUserName = student.getEntryYear() + schoolCode + formattedClassCode + formattedStudentNo;

                if (userMapper.checkUserNameUnique(generatedUserName) != null)
                {
                    failureNum++;
                    failureMsg.append("<br/>").append(failureNum).append("、学生 ").append(student.getStudentName()).append(" 生成的登录账号 ").append(generatedUserName).append(" 已存在");
                    continue;
                }

                SysUser newUser = new SysUser();
                newUser.setDeptId(teacherDeptId);
                newUser.setUserName(generatedUserName);
                newUser.setNickName(student.getStudentName());
                newUser.setPassword(SecurityUtils.encryptPassword("123456"));
                newUser.setCreateBy(operName);
                userMapper.insertUser(newUser);

                SysUserRole ur = new SysUserRole();
                ur.setUserId(newUser.getUserId());
                ur.setRoleId(4L);
                userRoleMapper.batchUserRole(Arrays.asList(ur));

                student.setUserId(newUser.getUserId());
                bizStudentMapper.insertBizStudent(student);

                successNum++;
                successMsg.append("<br/>").append(successNum).append("、学生 ").append(student.getStudentName()).append(" 导入成功，登录账号为 ").append(generatedUserName);
                
                // 自动将该班级添加到教师的管理班级中
                autoAssignTeacherClass(loginUser.getUserId(), teacherDeptId, student.getEntryYear(), student.getClassCode());
            }
            catch (Exception e)
            {
                failureNum++;
                String msg = "<br/>" + failureNum + "、学生 " + student.getStudentName() + " 导入失败：";
                failureMsg.append(msg).append(e.getMessage());
                log.error(msg, e);
            }
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        }
        else
        {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }

    @Override
    public int resetStudentPwd(Long[] userIds) {
        int successCount = 0;
        String password = SecurityUtils.encryptPassword("123456");
        for(Long userId : userIds) {
            SysUser user = new SysUser();
            user.setUserId(userId);
            user.setPassword(password);
            successCount += userService.resetPwd(user);
        }
        return successCount;
    }

    /**
     * 自动将班级添加到教师的管理班级中（如果尚未管理）
     */
    private void autoAssignTeacherClass(Long userId, Long deptId, String entryYear, String classCode) {
        try {
            // 检查是否已存在关联
            BizTeacherClass query = new BizTeacherClass();
            query.setUserId(userId);
            query.setEntryYear(entryYear);
            query.setClassCode(classCode);
            List<BizTeacherClass> existing = teacherClassService.selectBizTeacherClassList(query);
            
            // 如果不存在，自动添加
            if (existing == null || existing.isEmpty()) {
                BizTeacherClass tc = new BizTeacherClass();
                tc.setUserId(userId);
                tc.setDeptId(deptId);
                tc.setEntryYear(entryYear);
                tc.setClassCode(classCode);
                teacherClassService.insertBizTeacherClass(tc);
                log.info("自动添加教师管理班级: userId={}, entryYear={}, classCode={}", userId, entryYear, classCode);
            }
        } catch (Exception e) {
            log.warn("自动添加教师管理班级失败: {}", e.getMessage());
        }
    }
}
