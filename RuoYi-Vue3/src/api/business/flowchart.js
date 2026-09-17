import request from '@/utils/request'

export function getFlowchartQuestion(questionId) {
  return request({ url: `/business/flowchart/question/${questionId}`, method: 'get' })
}

export function getFlowchartQuestionPreview(questionId) {
  return request({ url: `/business/flowchart/question/${questionId}/preview`, method: 'get' })
}

export function saveFlowchartQuestion(questionId, data) {
  return request({ url: `/business/flowchart/question/${questionId}`, method: 'put', data })
}

export function generateFlowchartRules(answerJson) {
  return request({ url: '/business/flowchart/question/generate-rules', method: 'post', data: { answerJson } })
}

export function getStudentFlowchartWorkspace(lessonId, questionId) {
  return request({ url: '/business/flowchart/student/workspace', method: 'get', params: { lessonId, questionId } })
}

export function saveStudentFlowchartDraft(data) {
  return request({ url: '/business/flowchart/student/draft', method: 'put', data })
}

export function submitStudentFlowchart(data) {
  return request({ url: '/business/flowchart/student/submit', method: 'post', data })
}

export function reopenStudentFlowchart(data) {
  return request({ url: '/business/flowchart/student/reopen', method: 'post', data })
}

export function getFlowchartGradingSubmission(params) {
  return request({ url: '/business/flowchart/grading/submission', method: 'get', params })
}
