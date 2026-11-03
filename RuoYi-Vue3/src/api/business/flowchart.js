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

// 拍照 + AI 生成流程图标准答案草稿：multipart 上传照片，返回规范化后的 answerJson，仅回填画布不入库。
export function aiGenerateFlowchartAnswer(data) {
  return request({ url: '/business/flowchart/question/ai-generate-answer', method: 'post', data, headers: { 'Content-Type': 'multipart/form-data' } })
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
