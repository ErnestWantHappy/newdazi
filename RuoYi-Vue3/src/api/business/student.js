import request from '@/utils/request'

// 查询学生管理列表
export function listStudent(query) {
  return request({
    url: '/business/student/list',
    method: 'get',
    params: query
  })
}

// 查询学生管理详细
export function getStudent(studentId) {
  return request({
    url: '/business/student/' + studentId,
    method: 'get'
  })
}

// 新增学生管理
export function addStudent(data) {
  return request({
    url: '/business/student',
    method: 'post',
    data: data
  })
}

// 修改学生管理
export function updateStudent(data) {
  return request({
    url: '/business/student',
    method: 'put',
    data: data
  })
}

// 删除学生管理
export function delStudent(studentId) {
  return request({
    url: '/business/student/' + studentId,
    method: 'delete'
  })
}

// 重置学生密码 (支持批量)
export function resetStudentPwd(userIds) {
  // 后端接收的是一个 Long[] 数组
  return request({
    url: '/business/student/resetPwd',
    method: 'put',
    data: userIds // 直接将ID数组作为请求体发送
  })
}

// 查询学生锁定状态
export function getLockStatus(userNames) {
  return request({
    url: '/business/student/lockStatus',
    method: 'get',
    params: { userNames: userNames.join(',') }
  })
}
