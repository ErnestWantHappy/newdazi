import request from '@/utils/request'

// 查询教研员平台概览
export function getPlatformOverview() {
  return request({
    url: '/business/platformOverview/summary',
    method: 'get'
  })
}
