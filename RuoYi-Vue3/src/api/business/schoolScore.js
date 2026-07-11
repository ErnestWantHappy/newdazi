import request from '@/utils/request'

// 查询全区学校成绩统计
export function listTermStats(query) {
  return request({
    url: '/business/schoolScore/termStats',
    method: 'get',
    params: query
  })
}

// 查询学校班级成绩详情
export function listClassDetails(query) {
  return request({
    url: '/business/schoolScore/classDetails',
    method: 'get',
    params: query
  })
}
