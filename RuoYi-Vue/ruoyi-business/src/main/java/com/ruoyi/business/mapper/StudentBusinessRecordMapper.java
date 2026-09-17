package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

/** 汇总学生硬删除前必须保留的业务记录。 */
public interface StudentBusinessRecordMapper
{
    int countOtherBusinessRecords(@Param("studentIds") List<Long> studentIds);
}
