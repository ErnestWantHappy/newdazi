export function redoContextKey(lessonIds, entryYear, classCode) {
  return lessonIds.length === 1 && entryYear && classCode
    ? JSON.stringify([Number(lessonIds[0]), String(entryYear), String(classCode)]) : ''
}

export function isTheoryRedoPending(status) {
  return Number(status?.revision) > 0 && Number(status?.answerCount) === 0
}

// 接口返回数组，两处教师入口统一按真实学生 ID 建索引，不能把 ID 当数组下标。
export function indexRedoStudents(students) {
  return Object.fromEntries((students || []).map(student => [student.studentId, student]))
}

export function hasRedoAnswers(status, typing) {
  return Number(status?.[typing ? 'typingAnswerCount' : 'answerCount']) > 0
}
