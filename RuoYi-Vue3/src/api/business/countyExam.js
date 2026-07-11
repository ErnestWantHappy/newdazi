import request from '@/utils/request'

export function listCountyExam(query) {
  return request({
    url: '/business/countyExam/list',
    method: 'get',
    params: query
  })
}

export function getCountyExam(examId) {
  return request({
    url: `/business/countyExam/${examId}`,
    method: 'get'
  })
}

export function addCountyExam(data) {
  return request({
    url: '/business/countyExam',
    method: 'post',
    data
  })
}

export function updateCountyExam(data) {
  return request({
    url: '/business/countyExam',
    method: 'put',
    data
  })
}

export function delCountyExam(examIds) {
  return request({
    url: `/business/countyExam/${examIds}`,
    method: 'delete'
  })
}

export function saveCountyExamQuestions(examId, data) {
  return request({
    url: `/business/countyExam/${examId}/questions`,
    method: 'post',
    data
  })
}

export function saveCountyExamClasses(examId, data) {
  return request({
    url: `/business/countyExam/${examId}/classes`,
    method: 'post',
    data
  })
}

export function getAssignableCountyExamClasses(params) {
  return request({
    url: '/business/countyExam/classes/assignable',
    method: 'get',
    params
  })
}

export function openCountyExam(examId, data) {
  return request({
    url: `/business/countyExam/${examId}/open`,
    method: 'post',
    data
  })
}

export function closeCountyExam(examId) {
  return request({
    url: `/business/countyExam/${examId}/close`,
    method: 'post'
  })
}

export function allocateCountyExamGraders(examId, data) {
  return request({
    url: `/business/countyExam/${examId}/graders/allocate`,
    method: 'post',
    data
  })
}

export function resetCountyExamGraders(examId) {
  return request({
    url: `/business/countyExam/${examId}/graders/reset`,
    method: 'post'
  })
}

export function enableCountyExamGrading(examId) {
  return request({
    url: `/business/countyExam/${examId}/grading/enable`,
    method: 'post'
  })
}

export function disableCountyExamGrading(examId) {
  return request({
    url: `/business/countyExam/${examId}/grading/disable`,
    method: 'post'
  })
}

export function getAssignableCountyExamGraders(params) {
  return request({
    url: '/business/countyExam/graders/assignable',
    method: 'get',
    params
  })
}

export function publishCountyExam(examId) {
  return request({
    url: `/business/countyExam/${examId}/publish`,
    method: 'post'
  })
}

export function getCountyExamSummary(examId) {
  return request({
    url: `/business/countyExam/${examId}/summary`,
    method: 'get'
  })
}

export function listCountyExamStudents(examId, params) {
  return request({
    url: `/business/countyExam/${examId}/students`,
    method: 'get',
    params
  })
}

export function exportCountyExamStudents(examId) {
  return request({
    url: `/business/countyExam/${examId}/export`,
    method: 'get',
    responseType: 'blob'
  })
}

export function getCurrentCountyExam() {
  return request({
    url: '/business/countyExam/student/current',
    method: 'get'
  })
}

export function checkCurrentCountyExam() {
  return request({
    url: '/business/countyExam/student/check',
    method: 'get'
  })
}

export function saveCountyExamDraft(data) {
  return request({
    url: '/business/countyExam/student/draft',
    method: 'post',
    data,
    headers: { repeatSubmit: false }
  })
}

export function submitCountyExam(data) {
  return request({
    url: '/business/countyExam/student/submit',
    method: 'post',
    data,
    headers: { repeatSubmit: false }
  })
}
export function getCountyExamGradingEntry() {
  return request({
    url: '/business/countyExam/grading/entry',
    method: 'get'
  })
}

export function getCountyExamGradingTasks(params) {
  return request({
    url: '/business/countyExam/grading/tasks',
    method: 'get',
    params
  })
}

export function getCountyExamGradingAnswer(answerId) {
  return request({
    url: `/business/countyExam/grading/answers/${answerId}`,
    method: 'get'
  })
}

export function gradeCountyExamAnswer(data) {
  return request({
    url: '/business/countyExam/grading/grade',
    method: 'post',
    data
  })
}
