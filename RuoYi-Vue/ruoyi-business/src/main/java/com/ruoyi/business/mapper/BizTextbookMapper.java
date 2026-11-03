package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.BizTextbookBook;
import com.ruoyi.business.domain.BizTextbookPage;
import com.ruoyi.business.domain.BizTextbookSection;
import com.ruoyi.business.domain.BizTextbookUnit;

/**
 * 电子教材只读目录 Mapper
 *
 * 教材数据由离线导入脚本生成（sql/textbook_seed_*.sql），运行期只读，无增删改入口。
 */
public interface BizTextbookMapper
{
    /** 上架册列表，含单元数与课次数 */
    public List<BizTextbookBook> selectBookList();

    /** 按主键取上架册 */
    public BizTextbookBook selectBookById(@Param("bookId") Long bookId);

    /** 册内单元 */
    public List<BizTextbookUnit> selectUnitsByBookId(@Param("bookId") Long bookId);

    /** 册内全部目录条目 */
    public List<BizTextbookSection> selectSectionsByBookId(@Param("bookId") Long bookId);

    /** 册内页元数据，from/to 为闭区间，为空表示不限 */
    public List<BizTextbookPage> selectPagesByBookId(@Param("bookId") Long bookId,
                                                     @Param("from") Integer from,
                                                     @Param("to") Integer to);

    /** 单页元数据 */
    public BizTextbookPage selectPage(@Param("bookId") Long bookId, @Param("pageNo") Integer pageNo);
}
