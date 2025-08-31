import request from '@/utils/request'

// 查询题库管理列表
export function listQuestion(query) {
  return request({
    url: '/business/question/list',
    method: 'get',
    params: query
  })
}

// 查询题库管理详细
export function getQuestion(questionId) {
  return request({
    url: '/business/question/' + questionId,
    method: 'get'
  })
}

// 新增题库管理
export function addQuestion(data) {
  return request({
    url: '/business/question',
    method: 'post',
    data: data
  })
}

// 修改题库管理
export function updateQuestion(data) {
  return request({
    url: '/business/question',
    method: 'put',
    data: data
  })
}

// 删除题库管理
export function delQuestion(questionId) {
  return request({
    url: '/business/question/' + questionId,
    method: 'delete'
  })
}
