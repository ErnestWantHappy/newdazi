const PREFIX = 'huacheng:draft:'

export function loadLocalFlowchartDraft(key) {
  if (!key) return null
  try {
    const value = JSON.parse(localStorage.getItem(PREFIX + key) || 'null')
    return value?.documentJson ? value : null
  } catch (_) {
    return null
  }
}

export function saveLocalFlowchartDraft(key, documentJson, revision, synced = false) {
  if (!key || !documentJson) return
  try {
    localStorage.setItem(PREFIX + key, JSON.stringify({
      documentJson,
      revision,
      synced,
      savedAt: Date.now()
    }))
  } catch (_) {
    // 本地空间不足不能阻断服务端自动保存，页面仍会显示服务端保存状态。
  }
}

export function markLocalFlowchartDraftSynced(key, revision, documentJson) {
  saveLocalFlowchartDraft(key, documentJson, revision, true)
}

export function clearLocalFlowchartDraft(key) {
  if (!key) return
  try { localStorage.removeItem(PREFIX + key) } catch (_) {}
}

