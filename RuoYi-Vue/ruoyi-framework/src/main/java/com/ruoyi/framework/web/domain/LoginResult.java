package com.ruoyi.framework.web.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.ruoyi.common.core.domain.entity.SysDept;

/**
 * 登录结果
 *
 * @author ruoyi
 */
public class LoginResult implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String token;

    private boolean needsSchoolSelection;

    private List<SysDept> schools = new ArrayList<>();

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public boolean isNeedsSchoolSelection()
    {
        return needsSchoolSelection;
    }

    public void setNeedsSchoolSelection(boolean needsSchoolSelection)
    {
        this.needsSchoolSelection = needsSchoolSelection;
    }

    public List<SysDept> getSchools()
    {
        return schools;
    }

    public void setSchools(List<SysDept> schools)
    {
        this.schools = schools;
    }
}
