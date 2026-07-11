import request from '@/utils/request'

// 获取教师首页仪表盘的完整数据
export function getDashboardData() {
  return request({
    url: '/business/teacher/dashboard-data',
    method: 'get'
  })
}
