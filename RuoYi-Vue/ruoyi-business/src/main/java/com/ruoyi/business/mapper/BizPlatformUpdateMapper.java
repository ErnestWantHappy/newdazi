package com.ruoyi.business.mapper;

import com.ruoyi.business.domain.BizPlatformUpdate;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface BizPlatformUpdateMapper
{
    List<BizPlatformUpdate> selectPublishedList(@Param("keyword") String keyword);
    List<BizPlatformUpdate> selectManageList(BizPlatformUpdate query);
    BizPlatformUpdate selectById(Long updateId);
    int insert(BizPlatformUpdate update);
    int update(BizPlatformUpdate update);
    int updateStatus(@Param("updateId") Long updateId, @Param("status") String status,
                     @Param("updateBy") String updateBy);
}
