import test from 'node:test'
import assert from 'node:assert/strict'
import { helperVersionAtLeast, probeLocalSeatHelper } from '../seatHelper.js'

test('助手版本只校验最低版本，后续小版本继续可用', () => {
  for (const version of ['0.2.3', '0.2.4', '0.2.5', '0.2.6', '0.3.0', '1.0.0', '0.2.3.4']) {
    assert.equal(helperVersionAtLeast(version), true, version)
  }
  for (const version of ['0.2.2', '0.1.9', '0.2', '', null, undefined, 'abc']) {
    assert.equal(helperVersionAtLeast(version), false, String(version))
  }
})

test('带后缀或非数字的版本号不能靠前缀混过门槛', () => {
  for (const version of ['0.2.3a', '0.2.3-beta', '0.2.3_beta', 'v0.2.3', '.0.3', '0.2.', '0..3', '-1.0.0']) {
    assert.equal(helperVersionAtLeast(version), false, version)
  }
})

test('本地探测只接受已安装且版本足够的助手', async () => {
  const identity = version => ({ installed: true, version })
  const reply = value => async () => ({ ok: true, json: async () => value })
  assert.equal((await probeLocalSeatHelper(reply(identity('0.2.6')))).version, '0.2.6')
  assert.equal(await probeLocalSeatHelper(reply(identity('0.2.2'))), null)
  assert.equal(await probeLocalSeatHelper(reply({ installed: false, version: '0.2.6' })), null)
  assert.equal(await probeLocalSeatHelper(async () => ({ ok: false, json: async () => ({}) })), null)
  assert.equal(await probeLocalSeatHelper(async () => { throw new Error('offline') }), null)
})
