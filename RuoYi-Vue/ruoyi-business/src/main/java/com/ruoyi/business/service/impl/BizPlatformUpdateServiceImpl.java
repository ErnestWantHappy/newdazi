package com.ruoyi.business.service.impl;

import com.ruoyi.business.domain.BizPlatformUpdate;
import com.ruoyi.business.mapper.BizPlatformUpdateMapper;
import com.ruoyi.business.service.IBizPlatformUpdateService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BizPlatformUpdateServiceImpl implements IBizPlatformUpdateService
{
    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+$");
    private static final String DRAFT = "DRAFT";
    private static final String PUBLISHED = "PUBLISHED";
    private static final String WITHDRAWN = "WITHDRAWN";

    @Autowired
    private BizPlatformUpdateMapper updateMapper;

    @Override
    public List<BizPlatformUpdate> selectPublishedList(String keyword)
    {
        return updateMapper.selectPublishedList(StringUtils.trim(keyword));
    }

    @Override
    public List<BizPlatformUpdate> selectManageList(BizPlatformUpdate query)
    {
        return updateMapper.selectManageList(query);
    }

    @Override
    public int create(BizPlatformUpdate update)
    {
        validateRecord(update);
        update.setStatus(DRAFT);
        update.setCreateBy(SecurityUtils.getUsername());
        return updateMapper.insert(update);
    }

    @Override
    public int update(BizPlatformUpdate update)
    {
        if (update.getUpdateId() == null || updateMapper.selectById(update.getUpdateId()) == null)
        {
            throw new ServiceException("平台更新记录不存在");
        }
        validateRecord(update);
        update.setUpdateBy(SecurityUtils.getUsername());
        return updateMapper.update(update);
    }

    @Override
    public int changeStatus(Long updateId, String status)
    {
        if (updateId == null || updateMapper.selectById(updateId) == null)
        {
            throw new ServiceException("平台更新记录不存在");
        }
        if (!PUBLISHED.equals(status) && !WITHDRAWN.equals(status) && !DRAFT.equals(status))
        {
            throw new ServiceException("不支持的平台更新状态");
        }
        return updateMapper.updateStatus(updateId, status, SecurityUtils.getUsername());
    }

    private void validateRecord(BizPlatformUpdate update)
    {
        update.setVersionNo(StringUtils.trim(update.getVersionNo()));
        update.setTitle(StringUtils.trim(update.getTitle()));
        update.setContent(StringUtils.trim(update.getContent()));
        if (StringUtils.isEmpty(update.getVersionNo()) || !VERSION_PATTERN.matcher(update.getVersionNo()).matches())
        {
            throw new ServiceException("平台版本号须为 1.0.0 格式");
        }
        if (StringUtils.isEmpty(update.getTitle()) || update.getTitle().length() > 100)
        {
            throw new ServiceException("更新标题不能为空且不能超过100字");
        }
        if (StringUtils.isEmpty(update.getContent()) || update.getContent().length() > 4000)
        {
            throw new ServiceException("更新内容不能为空且不能超过4000字");
        }
        if (update.getPublishedAt() == null)
        {
            update.setPublishedAt(new Date());
        }
    }
}
