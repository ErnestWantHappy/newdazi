import request from '@/utils/request'

export function listSupervisionSchools(query) {
  return request({ url: '/business/schoolScore/schools', method: 'get', params: query })
}

export function listSupervisionTeachers(query) {
  return request({ url: '/business/schoolScore/teachers', method: 'get', params: query })
}

export function listSupervisionCourses(query) {
  return request({ url: '/business/schoolScore/courses', method: 'get', params: query })
}

export function listSupervisionTimeline(query) {
  return request({ url: '/business/schoolScore/timeline', method: 'get', params: query })
}

export function listSupervisionClasses(query) {
  return request({ url: '/business/schoolScore/classes', method: 'get', params: query })
}

export function listSupervisionStudents(query) {
  return request({ url: '/business/schoolScore/students', method: 'get', params: query })
}

export function listSupervisionQuestions(query) {
  return request({ url: '/business/schoolScore/questions', method: 'get', params: query })
}

export function listPracticalAnswerDetails(query) {
  return request({ url: '/business/schoolScore/practical-answers', method: 'get', params: query })
}

export function getDeadlineConfig() {
  return request({ url: '/business/practical-deadline/config', method: 'get' })
}

export function updateDeadlineConfig(deadlineDays) {
  return request({
    url: '/business/practical-deadline/config',
    method: 'put',
    data: { deadlineDays }
  })
}

export function adjustPracticalDeadline(deadlineId, data) {
  return request({
    url: `/business/practical-deadline/${deadlineId}/adjust`,
    method: 'post',
    data
  })
}

export function listDeadlineAudits(deadlineId) {
  return request({
    url: `/business/practical-deadline/${deadlineId}/audits`,
    method: 'get'
  })
}
