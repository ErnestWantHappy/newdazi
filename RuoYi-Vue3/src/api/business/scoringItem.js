import request from '@/utils/request'

// 获取题目的评分项列表
export function getScoringItems(lessonId, questionId) {
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

// 保存评分项配置（批量）
export function saveScoringItems(lessonId, questionId, items) {
  return request({
    url: '/business/scoring/item/batch',
    method: 'post',
    params: { lessonId, questionId },
    data: items
  })
}

// 获取评分项管理列表
export function listScoringItems(lessonId, questionId) {
  return request({
    url: '/business/scoring/item/list',
    method: 'get',
    params: { lessonId, questionId }
  })
}

// 新增评分项
export function addScoringItem(data) {
  return request({
    url: '/business/scoring/item',
    method: 'post',
    data: data
  })
}

// 修改评分项
export function updateScoringItem(data) {
  return request({
    url: '/business/scoring/item',
    method: 'put',
    data: data
  })
}

// 删除评分项
export function delScoringItem(itemIds) {
  return request({
    url: `/business/scoring/item/${itemIds}`,
    method: 'delete'
  })
}
