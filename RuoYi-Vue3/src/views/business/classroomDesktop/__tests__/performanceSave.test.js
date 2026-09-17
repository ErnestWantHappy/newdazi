import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const source = readFileSync(join(dirname(fileURLToPath(import.meta.url)), '../index.vue'), 'utf8')

test('课堂监控表现分与成绩查询一致：正负分直接保存，不传布尔 isAbsent', () => {
  assert.match(source, /isAbsent:\s*0/)
  assert.doesNotMatch(source, /direction:\s*['"]add['"]/)
  assert.doesNotMatch(source, /isAbsent:\s*false/)
  assert.match(source, /正数加分、负数扣分/)
})
