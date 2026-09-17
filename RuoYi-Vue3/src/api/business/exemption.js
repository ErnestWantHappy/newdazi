import request from '@/utils/request'

export function previewExemption(query) {
  return request({ url: '/business/exemption/preview', method: 'get', params: query })
}

export function submitExemption(data) {
  return request({ url: '/business/exemption/applications', method: 'post', data })
}

export function listMyExemptions() {
  return request({ url: '/business/exemption/applications/my', method: 'get' })
}

export function getExemptionDetail(applicationId) {
  return request({ url: `/business/exemption/applications/${applicationId}`, method: 'get' })
}

export function listExemptionReviews(query) {
  return request({ url: '/business/exemption/review/applications', method: 'get', params: query })
}

export function reviewExemption(applicationId, data) {
  return request({
    url: `/business/exemption/review/applications/${applicationId}`,
    method: 'put',
    data
  })
}

export function listExemptionStandards(query) {
  return request({ url: '/business/exemption/standards', method: 'get', params: query })
}

export function saveExemptionStandard(data) {
  return request({ url: '/business/exemption/standards', method: 'put', data })
}
