import request from '@/utils/request'

// 教师：查询本校常驻工具列表
export function listStudentTools(keyword) {
  return request({
    url: '/business/student-tool/list',
    method: 'get',
    params: keyword ? { keyword } : {}
  })
}

// 教师：查询单个常驻工具（含适用范围）
export function getStudentTool(toolId) {
  return request({ url: '/business/student-tool/' + toolId, method: 'get' })
}

// 教师：新增常驻工具（tool + scopes）
export function addStudentTool(data) {
  return request({ url: '/business/student-tool', method: 'post', data })
}

// 教师：修改常驻工具
export function updateStudentTool(data) {
  return request({ url: '/business/student-tool', method: 'put', data })
}

// 教师：批量删除常驻工具
export function delStudentTool(toolIds) {
  return request({ url: '/business/student-tool/' + toolIds, method: 'delete' })
}

// 教师：查询某课程的本节课工具
export function listLessonTools(lessonId) {
  return request({ url: '/business/student-tool/lesson/' + lessonId, method: 'get' })
}

// 教师：全量替换某课程的本节课工具
export function saveLessonTools(lessonId, tools) {
  return request({ url: '/business/student-tool/lesson/' + lessonId, method: 'put', data: tools })
}

// 学生：当前课程工具（可选刷新通道）
export function getMyStudentTools(lessonId) {
  return request({ url: '/business/student-tool/mine', method: 'get', params: lessonId ? { lessonId } : {} })
}
