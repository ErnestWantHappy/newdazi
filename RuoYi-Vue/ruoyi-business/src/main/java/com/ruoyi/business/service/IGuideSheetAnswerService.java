package com.ruoyi.business.service;

import java.util.List;
import java.util.Map;

import com.ruoyi.business.domain.BizGuideSheetAnswer;

public interface IGuideSheetAnswerService
{
    BizGuideSheetAnswer getByStudentAndBinding(Long studentId, Long bindingId);

    BizGuideSheetAnswer getByAnswerId(Long answerId);

    List<BizGuideSheetAnswer> getByBindingAndClass(Long bindingId, Long deptId,
                                                   String entryYear, String classCode);

    Double getAvgScore(Long bindingId, Long deptId, String entryYear, String classCode);

    BizGuideSheetAnswer saveManualGrades(Long bindingId, Long studentId,
                                          List<Map<String, Object>> items);

    int saveAnswer(BizGuideSheetAnswer answer);

    int submitAnswer(BizGuideSheetAnswer answer);

    int submitAnswer(BizGuideSheetAnswer answer, Integer tabIndex);

    int updateGrading(BizGuideSheetAnswer answer);
}
