import request from '@/utils/request'

// 获取课程分配的班级列表
export function getClassesByLesson(lessonId) {
  return request({
    url: `/business/teacher/grading/classes/${lessonId}`,
    method: 'get'
  })
}

// 获取课程的操作题列表
export function getPracticalQuestions(lessonId) {
  return request({
    url: `/business/teacher/grading/practical-questions/${lessonId}`,
    method: 'get'
  })
}

// P5: 获取班级所有学生的操作题提交情况（含未提交）
export function getPracticalSubmissions(lessonId, questionId, classCode, entryYear) {
  return request({
    url: '/business/teacher/grading/practical-submissions',
    method: 'get',
    params: { lessonId, questionId, classCode, entryYear }
  })
}

// 重新转换当前课程班级下失败的操作题文件
export function retryFailedPreviews(data) {
  return request({
    url: '/business/teacher/grading/retry-failed-previews',
    method: 'post',
    data
  })
}

// 获取当前课程班级的操作题批改期限状态
export function getPracticalDeadlineStatus(lessonId, entryYear, classCode) {
  return request({
    url: '/business/teacher/grading/deadline-status',
    method: 'get',
    params: { lessonId, entryYear, classCode }
  })
}

// Python 编程题批量采用系统判题最高分
export function adoptPythonSystemScores(data) {
  return request({
    url: '/business/teacher/grading/python/batch-adopt-system-scores',
    method: 'post',
    data
  })
}

// 教师运行查看 Python 最高分提交，不产生判题记录
export function runPythonSubmission(submissionId, stdin) {
  return request({
    url: `/business/teacher/grading/python/${submissionId}/run`,
    method: 'post',
    data: { stdin },
    timeout: 30000
  })
}

// 批改打分
export function gradeSubmission(data) {
  return request({
    url: '/business/teacher/grading/grade',
    method: 'post',
    data
  })
}

// 将当前作品退回重交；历史作品与原成绩仍保留用于审计。
export function returnClassroomTask(data) {
  return request({
    url: '/business/classroom-state/return',
    method: 'post',
    data
  })
}
