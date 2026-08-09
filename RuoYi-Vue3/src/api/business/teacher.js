import request from '@/utils/request'

// 获取教师首页仪表盘的完整数据
export function getDashboardData() {
  return request({
    url: '/business/teacher/dashboard-data',
    method: 'get'
  })
}

// 教师首页课程先显示，操作题批改红点随后异步补充
export function getDashboardPracticalStatus(lessonIds) {
  return request({
    url: '/business/teacher/dashboard-practical-status',
    method: 'post',
    data: lessonIds
  })
}
