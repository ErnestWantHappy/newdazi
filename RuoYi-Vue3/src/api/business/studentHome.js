import request from '@/utils/request'

// 获取学生当前课程及题目
export function getCurrentLesson() {
  return request({
    url: '/business/student-home/current-lesson',
    method: 'get'
  })
}

// 提交学生答案
export function submitAnswers(data) {
  return request({
    url: '/business/student-home/submit-answers',
    method: 'post',
    data: data
  })
}

// 获取学生历史成绩单
export function getHistoryScores(year) {
  return request({
    url: '/business/student-home/history-scores',
    method: 'get',
    params: { year }
  })
}

// 获取错题列表
export function getWrongQuestions(lessonId) {
  return request({
    url: '/business/student-home/wrong-questions',
    method: 'get',
    params: { lessonId }
  })
}
