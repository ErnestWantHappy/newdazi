import request from '@/utils/request'

// 查询课程/主题列表
export function listLesson(query) {
  return request({
    url: '/business/lesson/list',
    method: 'get',
    params: query
  })
}

// 查询课程/主题详细
export function getLesson(lessonId) {
  return request({
    url: '/business/lesson/' + lessonId,
    method: 'get'
  })
}

// 新增课程/主题
export function addLesson(data) {
  return request({
    url: '/business/lesson',
    method: 'post',
    data: data
  })
}

// 修改课程/主题
export function updateLesson(data) {
  return request({
    url: '/business/lesson',
    method: 'put',
    data: data
  })
}

// 删除课程/主题
export function delLesson(lessonId) {
  return request({
    url: '/business/lesson/' + lessonId,
    method: 'delete'
  })
}
