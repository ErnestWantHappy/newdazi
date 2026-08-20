package com.ruoyi.business.service;

import com.ruoyi.business.domain.BizPlatformUpdate;
import java.util.List;

public interface IBizPlatformUpdateService
{
    List<BizPlatformUpdate> selectPublishedList(String keyword);
    List<BizPlatformUpdate> selectManageList(BizPlatformUpdate query);
    int create(BizPlatformUpdate update);
    int update(BizPlatformUpdate update);
    int changeStatus(Long updateId, String status);
}
