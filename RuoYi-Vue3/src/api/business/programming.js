import request from '@/utils/request'

export function getProgrammingQuestion(questionId) { return request({ url: `/business/programming/question/${questionId}`, method: 'get' }) }
export function saveProgrammingQuestion(questionId, data) { return request({ url: `/business/programming/question/${questionId}`, method: 'put', data }) }
export function getStudentProgramming(lessonId, questionId) { return request({ url: `/business/student-home/programming/${lessonId}/${questionId}`, method: 'get' }) }
export function saveProgrammingDraft(data) { return request({ url: '/business/student-home/programming/draft', method: 'put', data }) }
export function runProgramming(data) { return request({ url: '/business/student-home/programming/run', method: 'post', data }) }
export function submitProgramming(data) { return request({ url: '/business/student-home/programming/submit', method: 'post', data }) }
export function cancelProgramming(lessonId, questionId, submissionId) { return request({ url: `/business/student-home/programming/${lessonId}/${questionId}/submissions/${submissionId}/cancel`, method: 'post' }) }
