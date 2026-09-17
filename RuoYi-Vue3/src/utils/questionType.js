const QUESTION_TYPE_LABELS = Object.freeze({
  choice: '选择题',
  judgment: '判断题',
  typing: '打字题',
  practical: '操作题'
})

/**
 * 内部题型编码只用于接口和数据库，用户界面统一显示中文安全兜底。
 */
export function questionTypeLabel(value) {
  return QUESTION_TYPE_LABELS[value] || '其他题型'
}

export function questionTypeOptions() {
  return Object.entries(QUESTION_TYPE_LABELS).map(([value, label]) => ({ value, label }))
}
