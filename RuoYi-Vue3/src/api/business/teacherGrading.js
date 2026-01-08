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

// 批改打分
export function gradeSubmission(answerId, score) {
  return request({
    url: '/business/teacher/grading/grade',
    method: 'post',
    data: { answerId, score }
  })
}
