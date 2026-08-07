import request from '@/utils/request'

// 获取学生当前课程及题目
export function getCurrentLesson() {
  return request({
    url: '/business/student-home/current-lesson',
    method: 'get'
  })
}

/** 学生课堂签到（不计作业分） */
export function studentCheckin(lessonId) {
  return request({
    url: '/business/student-home/checkin',
    method: 'post',
    data: { lessonId }
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

// 将已暂存文件提交为一个新的操作题作品版本
export function submitPracticalArtifact(data) {
  return request({
    url: '/business/student-home/practical-artifact/submit',
    method: 'post',
    data
  })
}

// 删除当前操作题作品；服务器仍保留历史版本用于审计
export function deletePracticalArtifact(data) {
  return request({
    url: '/business/student-home/practical-artifact/delete',
    method: 'post',
    data
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
