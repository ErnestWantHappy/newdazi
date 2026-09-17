package com.ruoyi.business.service;

import java.util.HashSet;
import java.util.Set;

import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.mapper.SysDeptMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 组织边界服务。
 */
@Service
public class OrganizationBoundaryService
{
    @Autowired
    private SysDeptMapper deptMapper;

    /**
     * 解析部门所属县域根节点，避免业务代码依赖固定部门编号。
     */
    public Long resolveCountyDeptId(Long deptId)
    {
        if (deptId == null)
        {
            throw new ServiceException("当前用户未关联组织机构");
        }

        SysDept current = deptMapper.selectDeptById(deptId);
        if (current == null)
        {
            throw new ServiceException("当前用户关联的组织机构不存在");
        }

        Set<Long> visited = new HashSet<>();
        while (current.getParentId() != null && current.getParentId() > 0)
        {
            if (!visited.add(current.getDeptId()))
            {
                throw new ServiceException("组织机构层级存在循环，无法确定县域边界");
            }
            SysDept parent = deptMapper.selectDeptById(current.getParentId());
            if (parent == null)
            {
                throw new ServiceException("组织机构上级节点不存在，无法确定县域边界");
            }
            current = parent;
        }
        return current.getDeptId();
    }

    public boolean isSameCounty(Long firstDeptId, Long secondDeptId)
    {
        if (firstDeptId == null || secondDeptId == null)
        {
            return false;
        }
        return resolveCountyDeptId(firstDeptId).equals(resolveCountyDeptId(secondDeptId));
    }
}
