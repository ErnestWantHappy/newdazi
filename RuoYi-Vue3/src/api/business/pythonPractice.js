import request from '@/utils/request'

export function getTeacherPlans(params = {}) { return request({ url: '/business/python-practice/teacher/plans', method: 'get', params }) }
export function getTeacherManagedClasses() { return request({ url: '/business/python-practice/teacher/classes', method: 'get' }) }
export function getTeacherPlan(planId) { return request({ url: `/business/python-practice/teacher/plans/${planId}`, method: 'get' }) }
export function createTeacherPlan(data) { return request({ url: '/business/python-practice/teacher/plans', method: 'post', data }) }
export function updateTeacherPlan(planId, data) { return request({ url: `/business/python-practice/teacher/plans/${planId}`, method: 'put', data }) }
export function deleteTeacherPlan(planId) { return request({ url: `/business/python-practice/teacher/plans/${planId}`, method: 'delete' }) }
export function addTeacherQuestion(planVersionId, data) { return request({ url: `/business/python-practice/teacher/versions/${planVersionId}/questions`, method: 'post', data }) }
export function addTeacherQuestions(planVersionId, questionIds) { return request({ url: `/business/python-practice/teacher/versions/${planVersionId}/questions/batch`, method: 'post', data: { questionIds } }) }
export function reorderTeacherQuestions(planVersionId, questionIds) { return request({ url: `/business/python-practice/teacher/versions/${planVersionId}/questions/order`, method: 'put', data: { questionIds } }) }
export function recommendTeacherQuestions(planVersionId, count = 12) { return request({ url: `/business/python-practice/teacher/versions/${planVersionId}/recommend`, method: 'post', data: { count } }) }
export function removeTeacherQuestion(planVersionId, questionId) { return request({ url: `/business/python-practice/teacher/versions/${planVersionId}/questions/${questionId}`, method: 'delete' }) }
export function publishTeacherPlan(planId, planVersionId) { return request({ url: `/business/python-practice/teacher/plans/${planId}/versions/${planVersionId}/publish`, method: 'post' }) }
export function getTeacherAnalytics(params) { return request({ url: '/business/python-practice/teacher/analytics', method: 'get', params }) }

export function getStudentPracticeOverview() { return request({ url: '/business/python-practice/student/overview', method: 'get' }) }
export function getStudentPracticeQuestion(params) { return request({ url: '/business/python-practice/student/question', method: 'get', params }) }
export function saveStudentPracticeDraft(data) { return request({ url: '/business/python-practice/student/draft', method: 'post', data }) }
export function submitStudentPractice(data) { return request({ url: '/business/python-practice/student/submit', method: 'post', data }) }
