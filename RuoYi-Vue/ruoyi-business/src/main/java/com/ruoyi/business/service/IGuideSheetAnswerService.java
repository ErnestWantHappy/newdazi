package com.ruoyi.business.service;

import com.ruoyi.business.domain.BizGuideSheetAnswer;
import java.util.List;

public interface IGuideSheetAnswerService
{
    public BizGuideSheetAnswer getByStudentAndSheet(Long studentId, Long sheetId);

    public BizGuideSheetAnswer getByAnswerId(Long answerId);

    public List<BizGuideSheetAnswer> getBySheetId(Long sheetId);

    /**
     * 按导学单ID和班级编号查询答案列表
     *
     * @param sheetId 导学单ID
     * @param classCode 班级编号
     * @return 答案列表
     */
    public List<BizGuideSheetAnswer> getBySheetIdByClassCode(Long sheetId, String classCode);

    public int saveAnswer(BizGuideSheetAnswer answer);

    public int submitAnswer(BizGuideSheetAnswer answer);

    public int updateGrading(BizGuideSheetAnswer answer);
}
