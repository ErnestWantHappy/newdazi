import request from '@/utils/request'

// 查询学校班级统计列表（详细）
export function listSchoolClassStats() {
  return request({
    url: '/business/schoolStats/list',
    method: 'get'
  })
}

// 查询学校汇总统计
export function getSchoolSummary() {
  return request({
    url: '/business/schoolStats/summary',
    method: 'get'
  })
}
