import request from '@/utils/request'

// 教师课堂大屏获取课程内每名学生的任务状态汇总。
export function getClassroomTaskSummary(params) {
  return request({
    url: '/business/classroom-state/summary',
    method: 'get',
    params
  })
}
