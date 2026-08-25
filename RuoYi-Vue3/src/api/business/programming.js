import request from '@/utils/request'

export function getProgrammingQuestion(questionId) { return request({ url: `/business/programming/question/${questionId}`, method: 'get' }) }
export function previewProgrammingQuestion(questionId) { return request({ url: `/business/programming/question/${questionId}/preview`, method: 'get' }) }
export function saveProgrammingQuestion(questionId, data) { return request({ url: `/business/programming/question/${questionId}`, method: 'put', data }) }
export function validateProgrammingQuestion(questionId) { return request({ url: `/business/programming/question/${questionId}/validate`, method: 'post' }) }
export function previewProgrammingImport(data) { return request({ url: '/business/programming/question/import/preview', method: 'post', data, timeout: 600000 }) }
export function confirmProgrammingImport(confirmToken) { return request({ url: '/business/programming/question/import/confirm', method: 'post', data: { confirmToken }, timeout: 600000 }) }
export function getStudentProgramming(lessonId, questionId) { return request({ url: `/business/student-home/programming/${lessonId}/${questionId}`, method: 'get' }) }
export function saveProgrammingDraft(data) { return request({ url: '/business/student-home/programming/draft', method: 'put', data }) }
export function runProgramming(data) { return request({ url: '/business/student-home/programming/run', method: 'post', data }) }
export function customRunProgramming(data) { return request({ url: '/business/student-home/programming/custom-run', method: 'post', data }) }
export function submitProgramming(data) { return request({ url: '/business/student-home/programming/submit', method: 'post', data }) }
export function cancelProgramming(lessonId, questionId, submissionId) { return request({ url: `/business/student-home/programming/${lessonId}/${questionId}/submissions/${submissionId}/cancel`, method: 'post' }) }
