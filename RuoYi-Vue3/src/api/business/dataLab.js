import request from '@/utils/request'

// ==========================================
// 学生端接口
// ==========================================

/** 获取实验题配置（说明、初始代码模板、Excel 文件名等） */
export function getStudentDataLabConfig(questionId, sourceId) {
  return request({
    url: `/business/dataLab/student/config/${questionId}`,
    method: 'get',
    params: { sourceId }
  })
}

/** 学生提交实验代码与图表 */
export function submitStudentDataLab(data) {
  return request({
    url: '/business/dataLab/student/submit',
    method: 'post',
    data
  })
}

/** 学生查询自己的历史提交 */
export function getStudentDataLabSubmission(params) {
  return request({
    url: '/business/dataLab/student/submission',
    method: 'get',
    params
  })
}

/** 下载实验题绑定的 Excel 数据文件二进制 Blob */
export function getDataLabExcelBlob(questionId, sourceId) {
  return request({
    url: `/business/dataLab/student/excel/${questionId}`,
    method: 'get',
    responseType: 'blob',
    params: { sourceId }
  })
}

// ==========================================
// 教师端接口
// ==========================================

/** 教师保存或更新实验题配置 */
export function saveDataLabConfig(data) {
  return request({
    url: '/business/dataLab/config',
    method: 'post',
    data
  })
}

/** 教师获取实验题配置详情 */
export function getDataLabConfig(questionId) {
  return request({
    url: `/business/dataLab/config/${questionId}`,
    method: 'get'
  })
}

/** 教师上传 Excel 数据集 */
export function uploadDataLabExcel(data) {
  return request({
    url: '/business/dataLab/uploadExcel',
    method: 'post',
    headers: { 'Content-Type': 'multipart/form-data' },
    data
  })
}

/** 教师查看某实验题的学生提交列表 */
export function getDataLabSubmissions(params) {
  return request({
    url: '/business/dataLab/submissions',
    method: 'get',
    params
  })
}

/** 教师查看某个学生的提交详情 */
export function getDataLabSubmissionDetail(submissionId) {
  return request({
    url: `/business/dataLab/submission/${submissionId}`,
    method: 'get'
  })
}

/** 教师给学生提交评分 */
export function scoreDataLabSubmission(submissionId, data) {
  return request({
    url: `/business/dataLab/submission/${submissionId}/score`,
    method: 'put',
    data
  })
}
