import test from 'node:test'
import assert from 'node:assert/strict'

import { gradesForSchoolType, isFutureActivityTime, lessonLabel, linkStatus } from '../researchActivityFormat.js'
import { isResearchNoticeImageSource } from '../publicNoticeImage.js'
import { MAX_FILE_BYTES, normalizeExpireTime, normalizeResourcePayload, validateLinks, validatePackageFile } from '../resourceForm.js'

test('学段只返回对应绝对年级', () => {
  assert.deepEqual(gradesForSchoolType('1'), [1, 2, 3, 4, 5, 6])
  assert.deepEqual(gradesForSchoolType('2'), [7, 8, 9])
  assert.deepEqual(gradesForSchoolType('3'), [10, 11, 12])
})

test('数字、专题和复习课显示正确且只给数字课保存课次', () => {
  assert.equal(lessonLabel({ lessonKind: 'N', lessonNo: 8 }), '第8课')
  assert.equal(lessonLabel({ lessonKind: 'S' }), '专题课')
  assert.equal(lessonLabel({ lessonKind: 'R' }), '复习课')
  assert.equal(normalizeResourcePayload({ lessonKind: 'S', lessonNo: 8, links: [] }).lessonNo, null)
})

test('云盘永久、有效和过期状态清晰', () => {
  const now = new Date('2026-07-22T10:00:00+08:00')
  assert.equal(linkStatus({}, now).code, 'PERMANENT')
  assert.equal(linkStatus({ expireTime: '2026-07-23T10:00:00+08:00' }, now).code, 'ACTIVE')
  assert.equal(linkStatus({ expireTime: '2026-07-21T10:00:00+08:00' }, now).code, 'EXPIRED')
})

test('文件严格允许50MiB并拒绝超出或错误扩展', () => {
  assert.equal(validatePackageFile({ name: '课件.zip', size: MAX_FILE_BYTES }), null)
  assert.match(validatePackageFile({ name: '课件.zip', size: MAX_FILE_BYTES + 1 }), /50MB/)
  assert.match(validatePackageFile({ name: '课件.exe', size: 100 }), /ZIP/)
})

test('链接上限、协议和未来过期时间在前端提前校验', () => {
  const now = new Date('2026-07-22T10:00:00+08:00')
  const valid = { resourceName: '云盘', linkUrl: 'https://example.com/a', permanent: true }
  assert.equal(validateLinks([valid], now), null)
  assert.match(validateLinks([valid, valid, valid, valid], now), /最多添加3个/)
  assert.match(validateLinks([{ ...valid, linkUrl: 'javascript:alert(1)' }], now), /HTTP/)
  assert.match(validateLinks([{ ...valid, permanent: false, expireTime: '2026-07-21 10:00:00' }], now), /晚于当前/)
})

test('编辑回显的 ISO 过期时间转换为后端约定格式', () => {
  assert.equal(normalizeExpireTime('2027-07-22T20:00:00.000+08:00'), '2027-07-22 20:00:00')
  assert.equal(normalizeExpireTime('2027-07-22 20:00:00'), '2027-07-22 20:00:00')
})

test('活动时间可不填，填写时必须晚于当前时间', () => {
  const now = new Date('2026-07-23T10:00:00+08:00')
  assert.equal(isFutureActivityTime(null, now), true)
  assert.equal(isFutureActivityTime('2026-07-23 10:01:00', now), true)
  assert.equal(isFutureActivityTime('2026-07-23 09:59:00', now), false)
  assert.equal(isFutureActivityTime('不是时间', now), false)
})

test('公开通知识别新旧教研图片地址且拒绝其他资源', () => {
  assert.equal(isResearchNoticeImageSource('/profile/upload/research-activity/images/2026/09/a.png'), true)
  assert.equal(isResearchNoticeImageSource('/prod-api/common/resource/view?resource=/profile/upload/research-activity/images/2026/09/a.png'), true)
  assert.equal(isResearchNoticeImageSource('/prod-api/common/resource/view?resource=%2Fprofile%2Fupload%2Fresearch-activity%2Fimages%2Fa.png'), true)
  assert.equal(isResearchNoticeImageSource('/prod-api/common/resource/view?resource=/profile/upload/private/a.png'), false)
  assert.equal(isResearchNoticeImageSource('https://example.com/a.png'), false)
})
