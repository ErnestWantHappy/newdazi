package com.ruoyi.business.service.impl;

import java.util.Arrays; // 核心修复：添加必要的 import
import java.util.List;
import com.ruoyi.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.service.IBizStudentService;
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
 * 学生管理Service业务层处理 (重构版)
 * * @author zdx
 * @date 2025-06-25
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

    @Override
    public BizStudent selectBizStudentByStudentId(Long studentId)
    {
        return bizStudentMapper.selectBizStudentByStudentId(studentId);
    }

    @Override
    public List<BizStudent> selectBizStudentList(BizStudent bizStudent)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        // 如果不是超级管理员，则强制只能查询自己学校的学生
        if (!loginUser.getUser().isAdmin()) {
            // 此处不再需要设置任何ID，因为Mapper的查询将直接使用当前用户的dept_id进行关联查询
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

        String formattedClassCode = StringUtils.leftPad(bizStudent.getClassCode(), 2, "0");
        String formattedStudentNo = StringUtils.leftPad(bizStudent.getStudentNo(), 2, "0");
        String generatedUserName = bizStudent.getEntryYear() + schoolCode + formattedClassCode + formattedStudentNo;

        if (userMapper.checkUserNameUnique(generatedUserName) != null)
        {
            throw new ServiceException("生成登录账号 " + generatedUserName + " 已存在，请检查入学年份、班级和学号");
        }

        SysUser newUser = new SysUser();
        newUser.setDeptId(teacherDeptId); // 用户的部门ID就是学校的内部ID
        newUser.setUserName(generatedUserName);
        newUser.setNickName(bizStudent.getStudentName());
        newUser.setPassword(SecurityUtils.encryptPassword("123456"));
        newUser.setCreateBy(operName);
        userMapper.insertUser(newUser);

        SysUserRole ur = new SysUserRole();
        ur.setUserId(newUser.getUserId());
        ur.setRoleId(4L); // 确认学生角色ID
        // 核心修复：使用 Arrays.asList 替代 List.of，以兼容旧版Java
        userRoleMapper.batchUserRole(Arrays.asList(ur));

        bizStudent.setUserId(newUser.getUserId());
        // 注意：此处不再需要 setSchoolId
        return bizStudentMapper.insertBizStudent(bizStudent);
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
                // 先删除关联的sys_user账号
                userMapper.deleteUserById(student.getUserId());
                // 再删除关联的用户角色信息
                userRoleMapper.deleteUserRoleByUserId(student.getUserId());
            }
        }
        // 最后批量删除学生业务表信息
        return bizStudentMapper.deleteBizStudentByStudentIds(studentIds);
    }

    @Override
    @Transactional
    public int deleteBizStudentByStudentId(Long studentId)
    {
        // 完善删除逻辑：先删除关联的sys_user账号
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
                String formattedClassCode = StringUtils.leftPad(student.getClassCode(), 2, "0");
                String formattedStudentNo = StringUtils.leftPad(student.getStudentNo(), 2, "0");
                String generatedUserName = student.getEntryYear() + schoolCode + formattedClassCode + formattedStudentNo;

                if (userMapper.checkUserNameUnique(generatedUserName) != null)
                {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、学生 " + student.getStudentName() + " 生成的登录账号 " + generatedUserName + " 已存在");
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
                ur.setRoleId(4L); // 确认学生角色ID
                // 核心修复：使用 Arrays.asList 替代 List.of，以兼容旧版Java
                userRoleMapper.batchUserRole(Arrays.asList(ur));

                student.setUserId(newUser.getUserId());
                // 注意：此处不再需要 setSchoolId
                bizStudentMapper.insertBizStudent(student);

                successNum++;
                successMsg.append("<br/>" + successNum + "、学生 " + student.getStudentName() + " 导入成功，登录账号为 " + generatedUserName);
            }
            catch (Exception e)
            {
                failureNum++;
                String msg = "<br/>" + failureNum + "、学生 " + student.getStudentName() + " 导入失败：";
                failureMsg.append(msg + e.getMessage());
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

    /**
     * 批量重置学生密码
     *
     * @param userIds 需要重置密码的用户ID数组
     * @return 结果
     */
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
}
