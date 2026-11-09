export const QUESTION_SLOTS = ['A', 'B', 'C', 'D']
export function emptyQuestionMedia() { return { schemaVersion: 1, stem: [], options: { A: [], B: [], C: [], D: [] } } }
export function normalizeQuestionMedia(media) {
  const result = emptyQuestionMedia()
  if (!media) return result
  if (media.schemaVersion !== 1) throw new Error('题目图片版本异常，请刷新页面')
  result.stem = Array.isArray(media.stem) ? media.stem.map(x => ({ imageId: String(x.imageId) })) : []
  for (const slot of QUESTION_SLOTS) result.options[slot] = (media.options?.[slot] || []).map(x => ({ imageId: String(x.imageId) }))
  return result
}
export function slotImages(question, slot = 'stem') {
  return slot === 'stem' ? (question?.media?.stem || []) : (question?.media?.options?.[slot] || [])
}
export function hasQuestionImages(question) { return slotImages(question).length > 0 || QUESTION_SLOTS.some(key => slotImages(question, key).length > 0) }
export function hasChoiceOption(question, slot) { return !!String(question?.[`option${slot}`] || '').trim() || slotImages(question, slot).length > 0 }
export function newQuestionRequestId() { return globalThis.crypto?.randomUUID?.() || `q${Date.now()}_${Math.random().toString(36).slice(2)}` }
