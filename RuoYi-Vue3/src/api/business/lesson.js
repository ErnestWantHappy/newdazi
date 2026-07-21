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

/** 教师查看班级签到名单 */
export function getLessonCheckinRoster(params) {
  return request({
    url: '/business/lesson/checkin-roster',
    method: 'get',
    params
  })
}

/** 教师首页：读取统一课程推进策略 */
export function getAdvancePolicy() {
  return request({
    url: '/business/lesson/advance-policy',
    method: 'get'
  })
}

/** 教师首页：保存统一课程推进策略（同步到全部常规课） */
export function updateAdvancePolicy(data) {
  return request({
    url: '/business/lesson/advance-policy',
    method: 'put',
    data
  })
}

/**
 * 手动一键课堂推进：多选班级，自动识别各班当前课，有成绩达统一阈值即推进
 * @param {{ entryYear: string, classCodes: string[] }} data
 */
export function manualAdvanceLesson(data) {
  return request({
    url: '/business/lesson/manual-advance',
    method: 'post',
    data
  })
}
