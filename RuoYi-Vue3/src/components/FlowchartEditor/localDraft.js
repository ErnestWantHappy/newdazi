const PREFIX = 'huacheng:draft:'

export function studentDraftKey(userId, lessonId, questionId) {
  // 无归属的旧键不能自动迁给下一位学生。
  return userId && lessonId && questionId ? `v2:${userId}:${lessonId}:${questionId}` : ''
}

export function loadLocalFlowchartDraft(key, ownerUserId) {
  if (!key || !ownerUserId) return null
  try {
    const value = JSON.parse(localStorage.getItem(PREFIX + key) || 'null')
    return value?.documentJson && String(value.ownerUserId) === String(ownerUserId) ? value : null
  } catch (_) {
    return null
  }
}

export function saveLocalFlowchartDraft(key, documentJson, revision, synced = false, ownerUserId) {
  if (!key || !documentJson || !ownerUserId) return
  try {
    localStorage.setItem(PREFIX + key, JSON.stringify({
      documentJson,
      ownerUserId,
      revision,
      synced,
      savedAt: Date.now()
    }))
  } catch (_) {
    // 本地空间不足不能阻断服务端自动保存，页面仍会显示服务端保存状态。
  }
}

export function markLocalFlowchartDraftSynced(key, revision, documentJson, ownerUserId) {
  saveLocalFlowchartDraft(key, documentJson, revision, true, ownerUserId)
}

export function clearLocalFlowchartDraft(key) {
  if (!key) return
  try { localStorage.removeItem(PREFIX + key) } catch (_) {}
}
