import request from '@/utils/request'

const base = '/business/teacher/grading'
export const getPaperRoster = data => request({ url: `${base}/paper-import/roster`, method: 'post', data })
export const uploadPaper = data => request({ url: `${base}/paper-import/upload`, method: 'post', data, timeout: 180000, headers: { repeatSubmit: false, 'Content-Type': 'multipart/form-data' } })
export const paperPageIdentity = data => request({ url: `${base}/paper-import/page-identity`, method: 'post', data, timeout: 180000, headers: { repeatSubmit: false } })
export const splitPaper = data => request({ url: `${base}/paper-import/split`, method: 'post', data, timeout: 180000, headers: { repeatSubmit: false } })
export const submitPaper = data => request({ url: `${base}/paper-import/submit`, method: 'post', data, timeout: 120000, headers: { repeatSubmit: false } })
export const getClassInsight = data => request({ url: `${base}/class-insight`, method: 'post', data, headers: { repeatSubmit: false } })
export const generateClassInsight = data => request({ url: `${base}/class-insight/generate`, method: 'post', data, timeout: 180000 })
const insight = `${base}/insight-v3`
export const latestInsight = data => request({ url: `${insight}/latest`, method: 'post', data, headers: { repeatSubmit: false } })
export const createInsight = data => request({ url: insight, method: 'post', data })
export const readInsight = id => request({ url: `${insight}/${id}` })
export const retryInsight = id => request({ url: `${insight}/${id}/retry`, method: 'post' })
export const cancelInsight = id => request({ url: `${insight}/${id}/cancel`, method: 'post' })
export const publishInsight = id => request({ url: `${insight}/${id}/publish`, method: 'post' })
export const insightStudent = (id, student) => request({ url: `${insight}/${id}/students/${student}` })
export const insightPage = (id, student, page) => request({ url: `${insight}/${id}/students/${student}/pages/${page}`, responseType: 'blob' })
