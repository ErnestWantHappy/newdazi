export function createLatestValueQueue(processValue) {
  let queuedValue
  let hasQueuedValue = false
  let runningPromise = null

  async function run() {
    while (hasQueuedValue) {
      const value = queuedValue
      queuedValue = undefined
      hasQueuedValue = false
      await processValue(value)
    }
  }

  function ensureRunning() {
    if (!runningPromise && hasQueuedValue) {
      const trackedPromise = run().finally(() => {
        if (runningPromise === trackedPromise) runningPromise = null
        if (hasQueuedValue) ensureRunning()
      })
      runningPromise = trackedPromise
    }
    return runningPromise
  }

  function enqueue(value) {
    queuedValue = value
    hasQueuedValue = true
    ensureRunning()
  }

  async function flush() {
    while (hasQueuedValue || runningPromise) {
      const activePromise = ensureRunning()
      if (activePromise) await activePromise
    }
  }

  return { enqueue, flush }
}
