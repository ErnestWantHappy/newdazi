import request from '@/utils/request'

export function getTeacherPlans(entryYear) { return request({ url: '/business/python-practice/teacher/plans', method: 'get', params: { entryYear } }) }
export function getTeacherPlan(planId) { return request({ url: `/business/python-practice/teacher/plans/${planId}`, method: 'get' }) }
export function createTeacherPlan(data) { return request({ url: '/business/python-practice/teacher/plans', method: 'post', data }) }
export function deleteTeacherPlan(planId) { return request({ url: `/business/python-practice/teacher/plans/${planId}`, method: 'delete' }) }
export function addTeacherQuestion(planVersionId, data) { return request({ url: `/business/python-practice/teacher/versions/${planVersionId}/questions`, method: 'post', data }) }
export function removeTeacherQuestion(planVersionId, questionId) { return request({ url: `/business/python-practice/teacher/versions/${planVersionId}/questions/${questionId}`, method: 'delete' }) }
export function publishTeacherPlan(planId, planVersionId) { return request({ url: `/business/python-practice/teacher/plans/${planId}/versions/${planVersionId}/publish`, method: 'post' }) }
export function getTeacherExtensions(planId) { return request({ url: `/business/python-practice/teacher/plans/${planId}/extensions`, method: 'get' }) }
export function createTeacherExtension(planId, data) { return request({ url: `/business/python-practice/teacher/plans/${planId}/extensions`, method: 'post', data }) }
export function addTeacherExtensionQuestion(extensionId, data) { return request({ url: `/business/python-practice/teacher/extensions/${extensionId}/questions`, method: 'post', data }) }
export function publishTeacherExtension(extensionId) { return request({ url: `/business/python-practice/teacher/extensions/${extensionId}/publish`, method: 'post' }) }
export function retractTeacherExtension(extensionId) { return request({ url: `/business/python-practice/teacher/extensions/${extensionId}/retract`, method: 'post' }) }
export function getTeacherAnalytics(params) { return request({ url: '/business/python-practice/teacher/analytics', method: 'get', params }) }
export function getStudentPracticeOverview() { return request({ url: '/business/python-practice/student/overview', method: 'get' }) }
export function getStudentPracticeQuestion(params) { return request({ url: '/business/python-practice/student/question', method: 'get', params }) }
export function saveStudentPracticeDraft(data) { return request({ url: '/business/python-practice/student/draft', method: 'post', data }) }
export function submitStudentPractice(data) { return request({ url: '/business/python-practice/student/submit', method: 'post', data }) }
