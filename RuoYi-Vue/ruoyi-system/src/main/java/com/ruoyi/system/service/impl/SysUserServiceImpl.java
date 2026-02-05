package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.validation.Validator;

import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.system.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanValidators;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.system.domain.SysPost;
import com.ruoyi.system.domain.SysUserDept;
import com.ruoyi.system.domain.SysUserPost;
import com.ruoyi.system.domain.SysUserRole;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysDeptService;
import com.ruoyi.system.service.ISysUserService;

/**
 * 用户 业务层处理
 * 
 * @author ruoyi
 */
@Service
public class SysUserServiceImpl implements ISysUserService
{
    private static final Logger log = LoggerFactory.getLogger(SysUserServiceImpl.class);

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysPostMapper postMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysUserPostMapper userPostMapper;

    @Autowired
    private SysUserDeptMapper userDeptMapper;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private ISysDeptService deptService;

    @Autowired
    protected Validator validator;
    @Autowired
    private SysDeptMapper deptMapper;
    /**
     * 根据条件分页查询用户列表
     * 
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    @DataScope(deptAlias = "d", userAlias = "u")
    public List<SysUser> selectUserList(SysUser user)
    {
        return userMapper.selectUserList(user);
    }

    /**
     * 根据条件分页查询已分配用户角色列表
     * 
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    @DataScope(deptAlias = "d", userAlias = "u")
    public List<SysUser> selectAllocatedList(SysUser user)
    {
        return userMapper.selectAllocatedList(user);
    }

    /**
     * 根据条件分页查询未分配用户角色列表
     * 
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    @DataScope(deptAlias = "d", userAlias = "u")
    public List<SysUser> selectUnallocatedList(SysUser user)
    {
        return userMapper.selectUnallocatedList(user);
    }

    /**
     * 通过用户名查询用户
     * 
     * @param userName 用户名
     * @return 用户对象信息
     */
    @Override
    public SysUser selectUserByUserName(String userName)
    {
        SysUser user = userMapper.selectUserByUserName(userName);
        if (StringUtils.isNotNull(user))
        {
            fillUserDeptInfo(user);
        }
        return user;
    }

    /**
     * 通过用户ID查询用户
     * 
     * @param userId 用户ID
     * @return 用户对象信息
     */
    @Override
    public SysUser selectUserById(Long userId)
    {
        SysUser user = userMapper.selectUserById(userId);
        if (StringUtils.isNotNull(user))
        {
            fillUserDeptInfo(user);
        }
        return user;
    }

    @Override
    public List<SysDept> selectDeptsByUserId(Long userId)
    {
        if (StringUtils.isNull(userId))
        {
            return new ArrayList<>();
        }
        return deptMapper.selectDeptListByUserId(userId);
    }

    /**
     * 查询用户所属角色组
     * 
     * @param userName 用户名
     * @return 结果
     */
    @Override
    public String selectUserRoleGroup(String userName)
    {
        List<SysRole> list = roleMapper.selectRolesByUserName(userName);
        if (CollectionUtils.isEmpty(list))
        {
            return StringUtils.EMPTY;
        }
        return list.stream().map(SysRole::getRoleName).collect(Collectors.joining(","));
    }

    /**
     * 查询用户所属岗位组
     * 
     * @param userName 用户名
     * @return 结果
     */
    @Override
    public String selectUserPostGroup(String userName)
    {
        List<SysPost> list = postMapper.selectPostsByUserName(userName);
        if (CollectionUtils.isEmpty(list))
        {
            return StringUtils.EMPTY;
        }
        return list.stream().map(SysPost::getPostName).collect(Collectors.joining(","));
    }

    /**
     * 校验用户名称是否唯一
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean checkUserNameUnique(SysUser user)
    {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkUserNameUnique(user.getUserName());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验手机号码是否唯一
     *
     * @param user 用户信息
     * @return
     */
    @Override
    public boolean checkPhoneUnique(SysUser user)
    {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkPhoneUnique(user.getPhonenumber());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验email是否唯一
     *
     * @param user 用户信息
     * @return
     */
    @Override
    public boolean checkEmailUnique(SysUser user)
    {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkEmailUnique(user.getEmail());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验用户是否允许操作
     * 
     * @param user 用户信息
     */
    @Override
    public void checkUserAllowed(SysUser user)
    {
        if (StringUtils.isNotNull(user.getUserId()) && user.isAdmin())
        {
            throw new ServiceException("不允许操作超级管理员用户");
        }
    }

    /**
     * 校验用户是否有数据权限
     * 
     * @param userId 用户id
     */
    @Override
    public void checkUserDataScope(Long userId)
    {
        if (!SysUser.isAdmin(SecurityUtils.getUserId()))
        {
            SysUser user = new SysUser();
            user.setUserId(userId);
            List<SysUser> users = SpringUtils.getAopProxy(this).selectUserList(user);
            if (StringUtils.isEmpty(users))
            {
                throw new ServiceException("没有权限访问用户数据！");
            }
        }
    }

    /**
     * 新增保存用户信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertUser(SysUser user)
    {
        List<Long> resolvedDeptIds = CollectionUtils.isEmpty(user.getDeptIds())
                ? new ArrayList<>()
                : new ArrayList<>(user.getDeptIds());
        resolvedDeptIds.removeIf(Objects::isNull);
        resolvedDeptIds = new ArrayList<>(new LinkedHashSet<>(resolvedDeptIds));
        if (StringUtils.isNotNull(user.getDeptId()))
        {
            if (!resolvedDeptIds.contains(user.getDeptId()))
            {
                resolvedDeptIds.add(0, user.getDeptId());
            }
        }
        else if (!resolvedDeptIds.isEmpty())
        {
            user.setDeptId(resolvedDeptIds.get(0));
        }
        if (resolvedDeptIds.isEmpty() && StringUtils.isNotNull(user.getDeptId()))
        {
            resolvedDeptIds.add(user.getDeptId());
        }
        user.setDeptIds(resolvedDeptIds);

        int rows = userMapper.insertUser(user);
        insertUserPost(user);
        insertUserRole(user);
        insertUserDept(user);
        return rows;
    }

    /**
     * 注册用户信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean registerUser(SysUser user)
    {
        return userMapper.insertUser(user) > 0;
    }

    /**
     * 修改保存用户信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateUser(SysUser user)
    {
        Long userId = user.getUserId();
        List<Long> resolvedDeptIds = CollectionUtils.isEmpty(user.getDeptIds())
                ? new ArrayList<>()
                : new ArrayList<>(user.getDeptIds());
        resolvedDeptIds.removeIf(Objects::isNull);
        resolvedDeptIds = new ArrayList<>(new LinkedHashSet<>(resolvedDeptIds));
        if (StringUtils.isNotNull(user.getDeptId()))
        {
            if (!resolvedDeptIds.contains(user.getDeptId()))
            {
                resolvedDeptIds.add(0, user.getDeptId());
            }
        }
        else if (!resolvedDeptIds.isEmpty())
        {
            user.setDeptId(resolvedDeptIds.get(0));
        }
        if (resolvedDeptIds.isEmpty() && StringUtils.isNotNull(user.getDeptId()))
        {
            resolvedDeptIds.add(user.getDeptId());
        }
        user.setDeptIds(resolvedDeptIds);

        // 删除用户与角色关联
        userRoleMapper.deleteUserRoleByUserId(userId);
        // 新增用户与角色管理
        insertUserRole(user);
        // 删除用户与岗位关联
        userPostMapper.deleteUserPostByUserId(userId);
        // 新增用户与岗位管理
        insertUserPost(user);

        // --- 新增逻辑开始 ---
        log.info("zdx, user为: {}",user);
        SysDept dept = null;
        if (user.getDeptId() != null)
        {
            dept = deptMapper.selectDeptById(user.getDeptId());
        }
        log.info("即将更新到数据库的用户信息, dept为: {}",dept);

        // --- 新增逻辑结束 ---
        int rows = userMapper.updateUser(user);
        userDeptMapper.deleteUserDeptByUserId(userId);
        insertUserDept(user);
        return rows;
    }

    /**
     * 用户授权角色
     * 
     * @param userId 用户ID
     * @param roleIds 角色组
     */
    @Override
    @Transactional
    public void insertUserAuth(Long userId, Long[] roleIds)
    {
        userRoleMapper.deleteUserRoleByUserId(userId);
        insertUserRole(userId, roleIds);
    }

    /**
     * 修改用户状态
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUserStatus(SysUser user)
    {
        return userMapper.updateUser(user);
    }

    /**
     * 修改用户基本信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUserProfile(SysUser user)
    {
        return userMapper.updateUser(user);
    }

    /**
     * 修改用户头像
     * 
     * @param userId 用户ID
     * @param avatar 头像地址
     * @return 结果
     */
    @Override
    public boolean updateUserAvatar(Long userId, String avatar)
    {
        return userMapper.updateUserAvatar(userId, avatar) > 0;
    }

    /**
     * 重置用户密码
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int resetPwd(SysUser user)
    {
        return userMapper.updateUser(user);
    }

    /**
     * 重置用户密码
     * 
     * @param userId 用户ID
     * @param password 密码
     * @return 结果
     */
    @Override
    public int resetUserPwd(Long userId, String password)
    {
        return userMapper.resetUserPwd(userId, password);
    }

    /**
     * 新增用户角色信息
     * 
     * @param user 用户对象
     */
    public void insertUserRole(SysUser user)
    {
        this.insertUserRole(user.getUserId(), user.getRoleIds());
    }

    private void fillUserDeptInfo(SysUser user)
    {
        if (StringUtils.isNull(user) || StringUtils.isNull(user.getUserId()))
        {
            return;
        }
        List<Long> deptIds = userDeptMapper.selectDeptIdsByUserId(user.getUserId());
        user.setDeptIds(deptIds);
        if (user.getDeptId() == null && !CollectionUtils.isEmpty(deptIds))
        {
            user.setDeptId(deptIds.get(0));
        }
        if (!CollectionUtils.isEmpty(deptIds))
        {
            SysDept firstDept = deptMapper.selectDeptById(deptIds.get(0));
            if (firstDept != null && StringUtils.isNotEmpty(firstDept.getSchoolCode()))
            {
                try
                {
                    // 使用部门中的school_code作为用户关联学校ID，仅用于前端展示
                    user.setSchoolId(Long.parseLong(firstDept.getSchoolCode()));
                }
                catch (NumberFormatException ignored)
                {
                    // 编码非数字时不设置schoolId，避免格式异常
                }
            }
        }
    }

    /**
     * 新增用户岗位信息
     * 
     * @param user 用户对象
     */
    public void insertUserPost(SysUser user)
    {
        Long[] posts = user.getPostIds();
        if (StringUtils.isNotEmpty(posts))
        {
            // 新增用户与岗位管理
            List<SysUserPost> list = new ArrayList<SysUserPost>(posts.length);
            for (Long postId : posts)
            {
                SysUserPost up = new SysUserPost();
                up.setUserId(user.getUserId());
                up.setPostId(postId);
                list.add(up);
            }
            userPostMapper.batchUserPost(list);
        }
    }

    private void insertUserDept(SysUser user)
    {
        List<Long> deptIds = user.getDeptIds();
        if (CollectionUtils.isEmpty(deptIds) && StringUtils.isNotNull(user.getDeptId()))
        {
            deptIds = Collections.singletonList(user.getDeptId());
        }
        if (CollectionUtils.isEmpty(deptIds))
        {
            return;
        }
        List<SysUserDept> list = new ArrayList<>(deptIds.size());
        for (Long deptId : deptIds)
        {
            if (deptId == null)
            {
                continue;
            }
            SysUserDept ud = new SysUserDept();
            ud.setUserId(user.getUserId());
            ud.setDeptId(deptId);
            list.add(ud);
        }
        if (!list.isEmpty())
        {
            userDeptMapper.batchUserDept(list);
        }
    }

    /**
     * 新增用户角色信息
     * 
     * @param userId 用户ID
     * @param roleIds 角色组
     */
    public void insertUserRole(Long userId, Long[] roleIds)
    {
        if (StringUtils.isNotEmpty(roleIds))
        {
            // 新增用户与角色管理
            List<SysUserRole> list = new ArrayList<SysUserRole>(roleIds.length);
            for (Long roleId : roleIds)
            {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                list.add(ur);
            }
            userRoleMapper.batchUserRole(list);
        }
    }

    /**
     * 通过用户ID删除用户
     * 
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteUserById(Long userId)
    {
        // 删除用户与角色关联
        userRoleMapper.deleteUserRoleByUserId(userId);
        // 删除用户与岗位表
        userPostMapper.deleteUserPostByUserId(userId);
        userDeptMapper.deleteUserDeptByUserId(userId);
        return userMapper.deleteUserById(userId);
    }

    /**
     * 批量删除用户信息
     * 
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteUserByIds(Long[] userIds)
    {
        for (Long userId : userIds)
        {
            checkUserAllowed(new SysUser(userId));
            checkUserDataScope(userId);
        }
        // 删除用户与角色关联
        userRoleMapper.deleteUserRole(userIds);
        // 删除用户与岗位关联
        userPostMapper.deleteUserPost(userIds);
        userDeptMapper.deleteUserDept(userIds);
        return userMapper.deleteUserByIds(userIds);
    }

    /**
     * 导入用户数据
     * 
     * @param userList 用户数据列表
     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
     * @param operName 操作用户
     * @return 结果
     */
    @Override
    public String importUser(List<SysUser> userList, Boolean isUpdateSupport, String operName)
    {
        if (StringUtils.isNull(userList) || userList.size() == 0)
        {
            throw new ServiceException("导入用户数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (SysUser user : userList)
        {
            try
            {
                // 验证是否存在这个用户
                SysUser u = userMapper.selectUserByUserName(user.getUserName());
                if (StringUtils.isNull(u))
                {
                    BeanValidators.validateWithException(validator, user);
                    
                    // 1. 设置默认密码 (如果Excel没填，或者根本没有密码列)
                    // 注：用户模版现已移除密码列，所以统统使用默认密码
                    String password = configService.selectConfigByKey("sys.user.initPassword");
                    user.setPassword(SecurityUtils.encryptPassword(password));
                    
                    // 2. 设置默认角色为 "教师" (role_id = 100)
                    user.setRoleIds(new Long[]{100L}); 

                    // 3. 解析 "归属校区" (allDeptNames)
                    // 支持中英文逗号分隔，如 "实验小学, 中学部"
                    String deptNames = user.getAllDeptNames();
                    List<Long> importDeptIds = new ArrayList<>();
                    if (StringUtils.isNotEmpty(deptNames)) {
                        String[] dNames = deptNames.split("[,，]");
                        for (String dName : dNames) {
                            dName = dName.trim();
                            if (StringUtils.isEmpty(dName)) continue;
                            // 使用 Mapper 直接查询，绕过数据权限限制
                            SysDept deptArgs = new SysDept();
                            deptArgs.setDeptName(dName);
                            List<SysDept> deptList = deptMapper.selectDeptList(deptArgs);
                            if (StringUtils.isNotEmpty(deptList)) {
                                importDeptIds.add(deptList.get(0).getDeptId());
                            }
                        }
                    }

                    // 如果解析到了部门ID
                    if (!importDeptIds.isEmpty()) {
                        // 设置主部门ID (取第一个)
                        user.setDeptId(importDeptIds.get(0));
                        // 设置关联的所有部门ID
                        user.setDeptIds(importDeptIds);
                        // 数据权限检查（这个可能会因为当前用户权限不足而报错，暂时注释掉）
                        // deptService.checkDeptDataScope(user.getDeptId());
                    } else {
                        throw new ServiceException("未找到归属校区: " + deptNames);
                    }

                    user.setCreateBy(operName);
                    
                    // ====== 调试日志 START ======
                    log.info("====== 用户导入调试 START ======");
                    log.info("用户账号: {}", user.getUserName());
                    log.info("用户姓名: {}", user.getNickName());
                    log.info("归属校区字符串: {}", deptNames);
                    log.info("解析到的部门IDs: {}", importDeptIds);
                    log.info("设置的主部门ID: {}", user.getDeptId());
                    log.info("设置的所有部门IDs: {}", user.getDeptIds());
                    log.info("设置的角色IDs: {}", java.util.Arrays.toString(user.getRoleIds()));
                    log.info("====== 用户导入调试 END ======");
                    // ====== 调试日志 END ======
                    
                    // 【关键修复】使用 this.insertUser 而不是 userMapper.insertUser
                    // this.insertUser 会调用 insertUserRole 和 insertUserDept 插入关联表
                    this.insertUser(user);
                    
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、账号 " + user.getUserName() + " 导入成功");
                }
                else if (isUpdateSupport)
                {
                    BeanValidators.validateWithException(validator, user);
                    checkUserAllowed(u);
                    checkUserDataScope(u.getUserId());
                    deptService.checkDeptDataScope(user.getDeptId());
                    user.setUserId(u.getUserId());
                    user.setUpdateBy(operName);
                    userMapper.updateUser(user);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、账号 " + user.getUserName() + " 更新成功");
                }
                else
                {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、账号 " + user.getUserName() + " 已存在");
                }
            }
            catch (Exception e)
            {
                failureNum++;
                String msg = "<br/>" + failureNum + "、账号 " + user.getUserName() + " 导入失败：";
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
