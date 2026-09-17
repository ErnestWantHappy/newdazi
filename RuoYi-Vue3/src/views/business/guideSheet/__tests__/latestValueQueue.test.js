import test from 'node:test'
import assert from 'node:assert/strict'

import { createLatestValueQueue } from '../utils/latestValueQueue.js'

test('并发 flush 等待在途保存并只处理最新待保存内容', async () => {
  let releaseFirst
  const firstGate = new Promise(resolve => { releaseFirst = resolve })
  const processed = []
  let revision = 0
  const queue = createLatestValueQueue(async value => {
    processed.push(value)
    if (value === 'first') await firstGate
    revision += 1
  })

  queue.enqueue('first')
  queue.enqueue('second')
  queue.enqueue('latest')
  let settled = false
  const flushing = queue.flush().then(() => { settled = true })
  await Promise.resolve()
  assert.equal(settled, false)

  releaseFirst()
  await flushing
  assert.deepEqual(processed, ['first', 'latest'])
  assert.equal(revision, 2)

  queue.enqueue('after-flush')
  await queue.flush()
  assert.deepEqual(processed, ['first', 'latest', 'after-flush'])
})
