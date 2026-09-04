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

// 按班级批量删除学生
export function delStudentByClass(query) {
  return request({
    url: '/business/student/byClass',
    method: 'delete',
    params: query
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

// 上传 Excel 生成纠错预览，不修改数据库
export function previewStudentCorrection(file) {
  const data = new FormData()
  data.append('file', file)
  return request({
    url: '/business/student/correction/preview',
    method: 'post',
    data,
    // 全局默认按 JSON 发送，纠错表必须显式走 multipart 才能保留二进制文件内容。
    headers: {
      'Content-Type': 'multipart/form-data',
      repeatSubmit: false
    }
  })
}

// 确认纠错；后端会再次校验后原地更新
export function applyStudentCorrection(rows) {
  return request({
    url: '/business/student/correction/apply',
    method: 'post',
    data: rows
  })
}

// 正常 0，停用 1；只改账号状态，不删除学生和历史数据
export function changeStudentStatus(studentIds, status) {
  return request({
    url: `/business/student/status/${status}`,
    method: 'put',
    data: studentIds
  })
}
