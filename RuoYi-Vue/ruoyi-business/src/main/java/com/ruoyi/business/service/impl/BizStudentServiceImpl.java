package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.service.IBizStudentService;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysUserRole;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.mapper.SysUserRoleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * 学生管理Service业务层处理
 * 
 * @author zdx
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
    /**
     * 查询学生管理
     * 
     * @param studentId 学生管理主键
     * @return 学生管理
     */
    @Override
    public BizStudent selectBizStudentByStudentId(Long studentId)
    {
        return bizStudentMapper.selectBizStudentByStudentId(studentId);
    }

    /**
     * 查询学生管理列表
     * 
     * @param bizStudent 学生管理
     * @return 学生管理
     */
    @Override
    public List<BizStudent> selectBizStudentList(BizStudent bizStudent)
    {
        return bizStudentMapper.selectBizStudentList(bizStudent);
    }

    /**
     * 新增学生管理
     * 
     * @param bizStudent 学生管理
     * @return 结果
     */
    @Override
    public int insertBizStudent(BizStudent bizStudent)
    {
        return bizStudentMapper.insertBizStudent(bizStudent);
    }

    /**
     * 修改学生管理
     * 
     * @param bizStudent 学生管理
     * @return 结果
     */
    @Override
    public int updateBizStudent(BizStudent bizStudent)
    {
        return bizStudentMapper.updateBizStudent(bizStudent);
    }

    /**
     * 批量删除学生管理
     * 
     * @param studentIds 需要删除的学生管理主键
     * @return 结果
     */
    @Override
    public int deleteBizStudentByStudentIds(Long[] studentIds)
    {
        return bizStudentMapper.deleteBizStudentByStudentIds(studentIds);
    }

    /**
     * 删除学生管理信息
     * 
     * @param studentId 学生管理主键
     * @return 结果
     */
    @Override
    public int deleteBizStudentByStudentId(Long studentId)
    {
        return bizStudentMapper.deleteBizStudentByStudentId(studentId);
    }

    /**
     * 导入学生数据
     * * @param studentList 学生数据列表
     * @param operName 操作用户
     * @return 结果
     */
    @Override
    @Transactional // 开启事务，确保所有操作要么全部成功，要么全部失败回滚
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

        // 获取当前操作的教师用户，以及他所在的学校ID
        LoginUser loginUser = SecurityUtils.getLoginUser();
        log.info("当前操作的教师用户：" + loginUser.getUser());
        Long schoolId = loginUser.getUser().getSchoolId();
        if (StringUtils.isNull(schoolId)) {
            throw new ServiceException("操作的教师账号没有关联学校，无法导入学生！");
        }

        for (BizStudent student : studentList)
        {
            try
            {
                // 1. 组装学生登录账号: 入学年份 + 学校ID + 班级编号 + 学号
                // 1. 格式化班级编号和学号，确保为两位数，不足两位的前面补0
                String formattedClassCode = StringUtils.leftPad(student.getClassCode(), 2, "0");
                String formattedStudentNo = StringUtils.leftPad(student.getStudentNo(), 2, "0");

                // 2. 组装学生登录账号: 使用格式化后的编号
                String generatedUserName = student.getEntryYear() + schoolId + formattedClassCode + formattedStudentNo;

                // 2. 验证该登录账号是否已存在
                SysUser u = userMapper.selectUserByUserName(generatedUserName);
                if (StringUtils.isNotNull(u))
                {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、学生 " + student.getStudentName() + " 生成的登录账号 " + generatedUserName + " 已存在");
                    continue; // 跳过当前循环，处理下一个学生
                }

                // 3. 创建并插入sys_user表的数据
                SysUser newUser = new SysUser();
                newUser.setSchoolId(schoolId);
                newUser.setUserName(generatedUserName);
                newUser.setNickName(student.getStudentName()); // 昵称使用Excel中的真实姓名
                newUser.setPassword(SecurityUtils.encryptPassword("123456")); // 设置默认密码并加密
                newUser.setCreateBy(operName);
                userMapper.insertUser(newUser); // 插入后，newUser对象会自动获得生成的userId

                // 4. 为新用户分配"学生"角色
                SysUserRole ur = new SysUserRole();
                ur.setUserId(newUser.getUserId());
                ur.setRoleId(4L); // 关键点：请根据您的数据库sys_role表，确认“学生”角色的真实ID
                userRoleMapper.batchUserRole(List.of(ur));

                // 5. 创建并插入biz_student表的数据
                student.setUserId(newUser.getUserId());
                student.setSchoolId(schoolId);
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
}
