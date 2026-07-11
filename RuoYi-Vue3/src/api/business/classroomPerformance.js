import request from '@/utils/request'

// 查询班级的课堂表现列表
export function getPerformanceList(params) {
  return request({
    url: '/business/classroom-performance/list',
    method: 'get',
    params
  })
}

// 保存课堂表现
export function savePerformance(data) {
  return request({
    url: '/business/classroom-performance/save',
    method: 'post',
    data
  })
}

// 批量保存课堂表现
export function batchSavePerformance(data) {
  return request({
    url: '/business/classroom-performance/batch-save',
    method: 'post',
    data
  })
}

// 查询单个学生的课堂表现
export function getStudentPerformance(studentId, lessonId) {
  return request({
    url: '/business/classroom-performance/get',
    method: 'get',
    params: { studentId, lessonId }
  })
}
