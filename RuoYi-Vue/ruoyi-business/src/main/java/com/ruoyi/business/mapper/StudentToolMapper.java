package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.BizStudentTool;
import com.ruoyi.business.domain.BizStudentToolScope;
import com.ruoyi.business.domain.BizLessonTool;

/**
 * 学生实验工具 Mapper。
 * 覆盖：常驻工具 CRUD + 适用范围 + 本节课工具 + 学生端按班匹配。
 */
public interface StudentToolMapper
{
    // ------- 常驻工具 -------
    List<BizStudentTool> selectStudentToolList(BizStudentTool query);

    BizStudentTool selectStudentToolByToolId(Long toolId);

    int insertStudentTool(BizStudentTool tool);

    int updateStudentTool(BizStudentTool tool);

    int deleteStudentToolByToolId(Long toolId);

    int deleteStudentToolsByToolIds(Long[] toolIds);

    /** 按学生（学校+年份+班级）匹配启用的常驻工具 */
    List<BizStudentTool> selectResidentToolsForStudent(@Param("deptId") Long deptId,
                                                       @Param("entryYear") String entryYear,
                                                       @Param("classCode") String classCode);

    // ------- 适用范围 -------
    List<BizStudentToolScope> selectScopesByToolId(Long toolId);

    void insertScope(BizStudentToolScope scope);

    void deleteScopesByToolId(Long toolId);

    // ------- 本节课工具 -------
    List<BizLessonTool> selectLessonToolsByLessonId(Long lessonId);

    void insertLessonTool(BizLessonTool tool);

    void updateLessonTool(BizLessonTool tool);

    void deleteLessonToolsByLessonId(Long lessonId);
}
