import assert from 'node:assert/strict'
import test from 'node:test'

import {
  filenameFromHeaders,
  rememberBlobDownloadFilename,
  resolveBlobDownloadFilename,
  sanitizeDownloadFilename
} from '../downloadFilename.js'

test('中文响应头文件名优先于页面兜底名', () => {
  const blob = new Blob(['test'])
  rememberBlobDownloadFilename(blob, {
    'download-filename': encodeURIComponent('课程管理数据_20260814_143025.xlsx')
  })
  assert.equal(resolveBlobDownloadFilename(blob, 'lesson_123.xlsx'), '课程管理数据_20260814_143025.xlsx')
})

test('兼容标准 Content-Disposition 文件名', () => {
  assert.equal(filenameFromHeaders({
    'content-disposition': "attachment; filename*=UTF-8''%E5%8C%BA%E5%9F%9F%E6%8A%BD%E6%B5%8B_%E6%88%90%E7%BB%A9.xlsx"
  }), '区域抽测_成绩.xlsx')
})

test('移除路径和非法字符并保留扩展名', () => {
  assert.equal(sanitizeDownloadFilename('../课程:成绩?.xlsx'), '课程_成绩_.xlsx')
})
