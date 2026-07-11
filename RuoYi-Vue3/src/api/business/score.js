import request from '@/utils/request'

// 获取班级列表
export function getScoreClasses() {
  return request({
    url: '/business/score/classes',
    method: 'get'
  })
}

// 获取课程列表
export function getScoreLessons(entryYear) {
  return request({
    url: '/business/score/lessons',
    method: 'get',
    params: { entryYear }
  })
}

// 查询成绩汇总
export function getScoreSummary(entryYear, classCode, lessonIds, keyword) {
  return request({
    url: '/business/score/summary',
    method: 'get',
    params: {
      entryYear,
      classCode,
      lessonIds: Array.isArray(lessonIds) ? lessonIds.join(',') : lessonIds,
      keyword
    }
  })
}

// 导出 Excel
export function exportScoreExcel(entryYear, classCode, lessonIds, keyword, columns) {
  return request({
    url: '/business/score/export',
    method: 'get',
    params: {
      entryYear,
      classCode,
      lessonIds: lessonIds?.join(','),
      keyword,
      columns: Array.isArray(columns) ? columns.join(',') : columns
    }, // 支持多选、当前搜索条件和导出列选择
    responseType: 'blob'
  })
}

// 获取题目分析数据
export function getQuestionAnalysis(lessonId, classCode, entryYear) {
  return request({
    url: '/business/score/analysis/' + lessonId,
    method: 'get',
    params: { classCode, entryYear }
  })
}

// 获取学生答题详情矩阵
export function getStudentAnswerMatrix(lessonId, classCode, entryYear) {
  return request({
    url: '/business/score/studentAnswerMatrix',
    method: 'get',
    params: { lessonId, classCode, entryYear }
  })
}

// 设置/取消某节课缺考请假
export function setStudentAbsent(studentId, lessonId, isAbsent) {
  return request({
    url: '/business/score/absent',
    method: 'put',
    data: { studentId, lessonId, isAbsent }
  })
}

// 人工修正某节课作业分
export function saveManualHomeworkScore(data) {
  return request({
    url: '/business/score/manual-homework-score',
    method: 'put',
    data
  })
}

// 取消某节课作业分人工修正
export function cancelManualHomeworkScore(data) {
  return request({
    url: '/business/score/manual-homework-score/cancel',
    method: 'put',
    data
  })
}
