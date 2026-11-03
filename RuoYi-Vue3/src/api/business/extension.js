import sendRequest from '@/utils/request'
// 反馈由当前弹窗或操作区显示，后台刷新不反复弹出通知。
const request = config => sendRequest({ ...config, silentError: true })
const teacher = '/business/extension/teacher'
const student = '/business/extension/student'
export const getExtensionContext = () => request({ url: `${student}/context` })
export const saveStudentSpace = space => request({ url: `${student}/space`, method: 'post', data: { space } })
export const getExtensionHome = () => request({ url: `${student}/home` })
export const getExtensionClasses = () => request({ url: `${teacher}/classes` })
export const getExtensionTeachers = () => request({ url: `${teacher}/teachers` })
export const createExtensionClass = data => request({ url: `${teacher}/classes`, method: 'post', data })
export const updateExtensionClass = (id, data) => request({ url: `${teacher}/classes/${id}`, method: 'put', data })
export const archiveExtensionClass = id => request({ url: `${teacher}/classes/${id}/archive`, method: 'post' })
export const getExtensionMembers = id => request({ url: `${teacher}/classes/${id}/members` })
export const getExtensionCandidates = keyword => request({ url: `${teacher}/candidates`, params: { keyword } })
export const addExtensionMembers = (id, studentIds) => request({ url: `${teacher}/classes/${id}/members`, method: 'post', data: { studentIds } })
export const removeExtensionMember = (id, sid) => request({ url: `${teacher}/classes/${id}/members/${sid}`, method: 'delete' })
export const createExtensionLesson = (id, data) => request({ url: `${teacher}/classes/${id}/lessons`, method: 'post', data })
export const getExtensionDashboard = id => request({ url: `${teacher}/classes/${id}/dashboard` })
export const getExtensionLegacy = id => request({ url: `${teacher}/classes/${id}/legacy` })
export const getExtensionRoster = (id, lessonId) => request({ url: `${teacher}/classes/${id}/lessons/${lessonId}/roster` })
