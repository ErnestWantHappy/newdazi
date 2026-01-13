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
export function getScoreSummary(entryYear, classCode, lessonId) {
  return request({
    url: '/business/score/summary',
    method: 'get',
    params: { entryYear, classCode, lessonId }
  })
}

// 导出 Excel
export function exportScoreExcel(entryYear, classCode, lessonIds) {
  return request({
    url: '/business/score/export',
    method: 'get',
    params: { entryYear, classCode, lessonIds: lessonIds?.join(',') }, // 支持多选
    responseType: 'blob'
  })
}

// 获取题目分析数据
export function getQuestionAnalysis(lessonId) {
  return request({
    url: '/business/score/analysis/' + lessonId,
    method: 'get'
  })
}
