package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.BizTeacherTool;
import com.ruoyi.business.domain.BizTeacherToolCategory;
import com.ruoyi.business.domain.dto.TeacherToolQuery;
import com.ruoyi.business.domain.vo.TeacherToolCategoryVo;
import com.ruoyi.business.domain.vo.TeacherToolTaskVo;
import com.ruoyi.business.domain.vo.TeacherToolTermVo;
import com.ruoyi.business.domain.vo.TeacherToolVo;

/** 教师工具分类、工具与归类关系的数据访问。 */
public interface TeacherToolMapper
{
    List<TeacherToolCategoryVo> selectCatalogCategories();
    List<TeacherToolVo> selectCatalogTools();
    List<TeacherToolVo> selectRecommendedTools();
    List<TeacherToolTaskVo> selectCatalogTasks();
    List<TeacherToolTermVo> selectCatalogTerms();

    /** 放大本会话分组拼接上限，避免分类名/任务名汇总被默认 1024 字节截断。 */
    void increaseGroupConcatMaxLen();

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
    /** 未删除分类计数：编辑时区分“停用保留”与“不存在或已删除”，文案不再笼统。 */
    int countExistingCategoriesByIds(@Param("categoryIds") List<Long> categoryIds);
    int insertTool(BizTeacherTool tool);
    int updateTool(BizTeacherTool tool);
    int updateToolStatus(@Param("toolId") Long toolId, @Param("status") String status,
                         @Param("updateBy") String updateBy);
    int updateToolDeleteFlag(@Param("toolId") Long toolId, @Param("delFlag") String delFlag,
                             @Param("updateBy") String updateBy);
    int deleteToolCategoryRelations(Long toolId);
    int insertToolCategoryRelations(@Param("toolId") Long toolId, @Param("categoryIds") List<Long> categoryIds);

    /** 教学任务与检索别名的统一维护位置。 */
    List<TeacherToolTaskVo> selectTaskList();
    /** 保存校验用的未删除全量任务（含已停用），管理页下拉仍只展示启用任务。 */
    List<TeacherToolTaskVo> selectAllTaskList();
    List<TeacherToolTermVo> selectTermsByToolId(Long toolId);
    int selectTermCount(Long toolId);
    int deleteTermsByToolId(Long toolId);
    int insertTerms(@Param("toolId") Long toolId, @Param("terms") List<String> terms,
                    @Param("createBy") String createBy);
}
