package com.ruoyi.business.service;

import com.ruoyi.business.domain.CountyExam;
import com.ruoyi.business.domain.CountyExamClass;
import com.ruoyi.business.domain.CountyExamQuestion;
import com.ruoyi.business.domain.dto.CountyExamGradeRequest;
import com.ruoyi.business.domain.dto.CountyExamGraderAllocateRequest;
import com.ruoyi.business.domain.dto.CountyExamSubmitRequest;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

/**
 * 县考管理 Service接口
 * 
 * @author ruoyi
 */
public interface ICountyExamService {
    
    /**
     * 查询县考
     * 
     * @param examId 县考ID
     * @return 县考信息
     */
    CountyExam selectCountyExamById(Long examId);

    /**
     * 查询县考列表
     * 
     * @param countyExam 查询条件
     * @return 县考列表
     */
    List<CountyExam> selectCountyExamList(CountyExam countyExam);

    /**
     * 新增县考
     * 
     * @param countyExam 县考信息
     * @return 结果
     */
    int insertCountyExam(CountyExam countyExam);

    /**
     * 修改县考
     * 
     * @param countyExam 县考信息
     * @return 结果
     */
    int updateCountyExam(CountyExam countyExam);

    /**
     * 删除县考
     * 
     * @param examId 县考ID
     * @return 结果
     */
    int deleteCountyExamById(Long examId);

    /**
     * 批量删除县考
     * 
     * @param examIds 县考ID数组
     * @return 结果
     */
    int deleteCountyExamByIds(Long[] examIds);

    int saveQuestions(Long examId, List<CountyExamQuestion> questions);

    int saveClasses(Long examId, List<CountyExamClass> classes);

    Map<String, Object> openExam(Long examId, Integer durationMinutes);

    Map<String, Object> closeExam(Long examId);

    Map<String, Object> allocateGraders(Long examId, CountyExamGraderAllocateRequest request);

    Map<String, Object> resetGraders(Long examId);

    Map<String, Object> updateGradingEnabled(Long examId, boolean enabled);

    Map<String, Object> publishExam(Long examId);

    Map<String, Object> getExamDetail(Long examId);

    List<Map<String, Object>> getAssignableClasses(String schoolType);

    List<Map<String, Object>> getAssignableGraders(String keyword);

    Map<String, Object> getSummary(Long examId);

    List<Map<String, Object>> getStudents(Long examId, String keyword);

    void exportStudents(Long examId, HttpServletResponse response);

    Map<String, Object> checkCurrentStudentExam();

    Map<String, Object> getCurrentStudentExam();

    Map<String, Object> saveStudentDraft(CountyExamSubmitRequest request);

    Map<String, Object> submitStudentExam(CountyExamSubmitRequest request);

    Map<String, Object> getGradingEntry();

    List<Map<String, Object>> getGradingTasks(String gradingStatus);

    Map<String, Object> getGradingAnswer(Long answerId);

    Map<String, Object> gradeAnswer(CountyExamGradeRequest request);
}
