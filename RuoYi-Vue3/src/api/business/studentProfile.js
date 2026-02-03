import request from '@/utils/request'

/**
 * 获取学生画像汇总数据
 */
export function getStudentProfileSummary(studentId, semesterStart, semesterEnd) {
  return request({
    url: `/business/student-profile/summary/${studentId}`,
    method: 'get',
    params: { semesterStart, semesterEnd }
  })
}

/**
 * 获取历次课程成绩
 */
export function getCourseScores(studentId, semesterStart, semesterEnd) {
  return request({
    url: `/business/student-profile/courses/${studentId}`,
    method: 'get',
    params: { semesterStart, semesterEnd }
  })
}

/**
 * 获取打字速度数据
 */
export function getTypingSpeeds(studentId, semesterStart, semesterEnd) {
  return request({
    url: `/business/student-profile/typing/${studentId}`,
    method: 'get',
    params: { semesterStart, semesterEnd }
  })
}

/**
 * 获取课堂表现分数据
 */
export function getPerformances(studentId, semesterStart, semesterEnd) {
  return request({
    url: `/business/student-profile/performance/${studentId}`,
    method: 'get',
    params: { semesterStart, semesterEnd }
  })
}

/**
 * 获取排名变化数据
 */
export function getRankChanges(studentId, semesterStart, semesterEnd) {
  return request({
    url: `/business/student-profile/rank/${studentId}`,
    method: 'get',
    params: { semesterStart, semesterEnd }
  })
}

/**
 * 获取班级列表
 */
export function getClasses() {
  return request({
    url: `/business/student-profile/classes`,
    method: 'get'
  })
}

/**
 * 获取指定班级的学生列表
 */
export function getStudentsByClass(entryYear, classCode) {
  return request({
    url: `/business/student-profile/list`,
    method: 'get',
    params: { entryYear, classCode }
  })
}
