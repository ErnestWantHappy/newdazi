import request from '@/utils/request'

const BASE_URL = '/business/teacher-tools'

export function getTeacherToolCatalog() {
  return request({ url: `${BASE_URL}/catalog`, method: 'get' })
}

export function listTeacherToolCategories() {
  return request({ url: `${BASE_URL}/manage/categories`, method: 'get' })
}

export function createTeacherToolCategory(data) {
  return request({ url: `${BASE_URL}/manage/categories`, method: 'post', data })
}

export function updateTeacherToolCategory(categoryId, data) {
  return request({ url: `${BASE_URL}/manage/categories/${categoryId}`, method: 'put', data })
}

export function updateTeacherToolCategoryStatus(categoryId, status) {
  return request({ url: `${BASE_URL}/manage/categories/${categoryId}/status`, method: 'put', params: { status } })
}

export function listTeacherTools(params) {
  return request({ url: `${BASE_URL}/manage/tools`, method: 'get', params })
}

export function getTeacherTool(toolId) {
  return request({ url: `${BASE_URL}/manage/tools/${toolId}`, method: 'get' })
}

export function createTeacherTool(data) {
  return request({ url: `${BASE_URL}/manage/tools`, method: 'post', data })
}

export function updateTeacherTool(toolId, data) {
  return request({ url: `${BASE_URL}/manage/tools/${toolId}`, method: 'put', data })
}

export function updateTeacherToolStatus(toolId, status) {
  return request({ url: `${BASE_URL}/manage/tools/${toolId}/status`, method: 'put', params: { status } })
}

export function deleteTeacherTool(toolId) {
  return request({ url: `${BASE_URL}/manage/tools/${toolId}`, method: 'delete' })
}

export function restoreTeacherTool(toolId) {
  return request({ url: `${BASE_URL}/manage/tools/${toolId}/restore`, method: 'put' })
}
