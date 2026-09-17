import test from 'node:test'
import assert from 'node:assert/strict'
import { lessonToolHref, lessonToolUrlHint } from '../lessonToolUrl.js'

test('普通网页保留查询参数、片段及内网地址，不探测网站可达性', () => {
  for (const url of ['http://10.52.1.130:3018/', 'https://example.com/a?x=1&y=2#page', 'https://example.com/中文']) {
    assert.equal(lessonToolHref(url), url)
    assert.equal(lessonToolUrlHint(url), '')
  }
})

test('不完整或特殊协议仅给提示，不生成学生可执行链接', () => {
  for (const url of ['', undefined, 'example.com/a', '//example.com', 'javascript:alert(1)', 'data:text/html,test', 'https://user:pass@example.com', 'http://', 'https://example.com/\npath', 'https://example.com\\path']) {
    assert.equal(lessonToolHref(url), undefined)
    assert.ok(lessonToolUrlHint(url))
  }
})
