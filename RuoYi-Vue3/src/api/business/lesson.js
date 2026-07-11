import request from '@/utils/request'

// 查询课程/作业信息列表
export function listLesson(query) {
  return request({
    url: '/business/lesson/list',
    method: 'get',
    params: query
  })
}

// 查询课程/作业信息详细
export function getLesson(lessonId) {
  return request({
    url: '/business/lesson/' + lessonId,
    method: 'get'
  })
}

// 新增课程/作业信息
export function addLesson(data) {
  return request({
    url: '/business/lesson',
    method: 'post',
    data: data
  })
}

// 修改课程/作业信息
export function updateLesson(data) {
  return request({
    url: '/business/lesson',
    method: 'put',
    data: data
  })
}

// 删除课程/作业信息
export function delLesson(lessonId) {
  return request({
    url: '/business/lesson/' + lessonId,
    method: 'delete'
  })
}
/**
 * 获取课程完整详情（包括题目和已指派班级）
 * @param {number} lessonId 课程ID
 */
export function getLessonDetails(lessonId) {
  return request({
    url: '/business/lesson/details/' + lessonId,
    method: 'get'
  })
}

/**
 * 一站式保存课程所有信息（新增或修改）
 * @param {object} data 包含课程所有信息的对象
 */
export function saveAllLessonDetails(data) {
  return request({
    url: '/business/lesson/save-all',
    method: 'post',
    data: data
  })
}