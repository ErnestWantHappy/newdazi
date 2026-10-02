// 模式切换和卸载会取消旧任务，避免迟到的初始化覆盖教师正在编辑的内容。
export function createDesignerReadyTask({ isActive, attempt, onTimeout, delay = 100, maxAttempts = 30 }) {
  let timer = null
  let generation = 0

  function cancel() {
    generation++
    clearTimeout(timer)
    timer = null
  }

  function run() {
    cancel()
    const current = generation
    let count = 0
    function tick() {
      if (current !== generation || !isActive()) return
      count++
      try {
        if (attempt()) return
      } catch {
        // VForm3 的引用可能先于内部表单就绪，允许在有限次数内重试。
      }
      if (count >= maxAttempts) {
        onTimeout?.()
        return
      }
      timer = setTimeout(tick, delay)
    }
    tick()
  }

  return { run, cancel }
}
