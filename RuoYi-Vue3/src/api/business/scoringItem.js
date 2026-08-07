import request from '@/utils/request'

// 获取题目的评分项列表
export function getScoringItems(lessonId, questionId, practicalVersionId) {
  return request({
    url: '/business/teacher/grading/scoring-items',
    method: 'get',
    params: { lessonId, questionId }
  })
}

// 获取答题的分项得分
export function getScoringDetails(answerId) {
  return request({
    url: `/business/teacher/grading/scoring-details/${answerId}`,
    method: 'get'
  })
}

// 获取评分项管理列表
export function listScoringItems(lessonId, questionId) {
  return request({
    url: '/business/scoring/item/list',
    method: 'get',
    params: { lessonId, questionId, practicalVersionId }
  })
}
