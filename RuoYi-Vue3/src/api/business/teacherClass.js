import request from "@/utils/request";

// 查询当前教师管理的班级列表
export function listTeacherClass(query) {
  return request({
    url: "/business/teacherClass/list",
    method: "get",
    params: query,
  });
}

// 查询我管理的班级（不分页，用于下拉框）
export function getMyClasses() {
  return request({
    url: "/business/teacherClass/myClasses",
    method: "get",
  });
}

// 查询学校内所有可选班级
export function getAvailableClasses() {
  return request({
    url: "/business/teacherClass/availableClasses",
    method: "get",
  });
}

// 新增教师班级管理
export function addTeacherClass(data) {
  return request({
    url: "/business/teacherClass",
    method: "post",
    data: data,
  });
}

// 批量新增教师班级管理
export function batchAddTeacherClass(data) {
  return request({
    url: "/business/teacherClass/batch",
    method: "post",
    data: data,
  });
}

// 删除教师班级管理
export function delTeacherClass(ids) {
  return request({
    url: "/business/teacherClass/" + ids,
    method: "delete",
  });
}
