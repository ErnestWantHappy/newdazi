package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.BizTeacherTool;
import com.ruoyi.business.domain.BizTeacherToolCategory;
import com.ruoyi.business.domain.dto.TeacherToolQuery;
import com.ruoyi.business.domain.vo.TeacherToolCategoryVo;
import com.ruoyi.business.domain.vo.TeacherToolVo;

/** 教师工具分类、工具与归类关系的数据访问。 */
public interface TeacherToolMapper
{
    List<TeacherToolCategoryVo> selectCatalogCategories();
    List<TeacherToolVo> selectCatalogTools();
    List<TeacherToolVo> selectRecommendedTools();

    List<BizTeacherToolCategory> selectCategoryList();
    BizTeacherToolCategory selectCategoryById(Long categoryId);
    int countCategoryCode(@Param("categoryCode") String categoryCode, @Param("excludeId") Long excludeId);
    int insertCategory(BizTeacherToolCategory category);
    int updateCategory(BizTeacherToolCategory category);
    int updateCategoryStatus(@Param("categoryId") Long categoryId, @Param("status") String status,
                             @Param("updateBy") String updateBy);

    List<TeacherToolVo> selectToolList(TeacherToolQuery query);
    BizTeacherTool selectToolById(Long toolId);
    List<Long> selectCategoryIdsByToolId(Long toolId);
    int countCategoriesByIds(@Param("categoryIds") List<Long> categoryIds);
    int insertTool(BizTeacherTool tool);
    int updateTool(BizTeacherTool tool);
    int updateToolStatus(@Param("toolId") Long toolId, @Param("status") String status,
                         @Param("updateBy") String updateBy);
    int updateToolDeleteFlag(@Param("toolId") Long toolId, @Param("delFlag") String delFlag,
                             @Param("updateBy") String updateBy);
    int deleteToolCategoryRelations(Long toolId);
    int insertToolCategoryRelations(@Param("toolId") Long toolId, @Param("categoryIds") List<Long> categoryIds);
}
