package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.SysUserDept;

/**
 * 用户与部门关联 数据层
 *
 * @author ruoyi
 */
public interface SysUserDeptMapper
{
    /**
     * 通过用户ID删除关联
     *
     * @param userId 用户ID
     * @return 结果
     */
    int deleteUserDeptByUserId(Long userId);

    /**
     * 批量删除用户与部门关联
     *
     * @param userIds 用户ID集合
     * @return 结果
     */
    int deleteUserDept(Long[] userIds);

    /**
     * 批量新增用户与部门关联
     *
     * @param list 关联数据
     * @return 结果
     */
    int batchUserDept(List<SysUserDept> list);

    /**
     * 查询用户关联的部门ID集合
     *
     * @param userId 用户ID
     * @return 部门ID集合
     */
    List<Long> selectDeptIdsByUserId(Long userId);

    /**
     * 校验用户是否关联指定部门
     *
     * @param userId 用户ID
     * @param deptId 部门ID
     * @return 结果
     */
    int checkUserDeptExists(@Param("userId") Long userId, @Param("deptId") Long deptId);
}
