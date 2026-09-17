package com.ruoyi.business.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.AiModelPrice;

public interface AiModelPriceMapper
{
    List<AiModelPrice> selectAll();
    AiModelPrice selectByModel(@Param("providerCode") String providerCode,
                               @Param("modelName") String modelName);
    int upsert(AiModelPrice price);
    Map<String, Object> selectAverageUsageByModel(@Param("providerCode") String providerCode,
                                                   @Param("modelName") String modelName);
}
