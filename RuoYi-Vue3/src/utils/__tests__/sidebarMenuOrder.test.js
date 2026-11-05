import assert from 'node:assert/strict'
import test from 'node:test'
import { sortSidebarRoutes } from '../sidebarMenuOrder.js'

const routes = paths => paths.map(path => ({ path }))
const paths = values => values.map(item => item.path)

test('教师菜单按高频教学流程排序，帮助中心已并入教师工具不再单独置顶', () => {
  const result = sortSidebarRoutes(routes([
    '/studentguanli', '/teacher-tools', '/teacher-dashboard',
    '/teacherClass', '/score', '/question'
  ]), ['teacher'])

  assert.deepEqual(paths(result), [
    '/teacher-dashboard', '/question', '/score', '/studentguanli',
    '/teacherClass', '/teacher-tools'
  ])
})

test('兼容若依单菜单生成的根路径斜杠包装结构', () => {
  const wrapped = title => ({ path: '/', children: [{ path: 'wrapped', meta: { title } }] })
  const result = sortSidebarRoutes([
    wrapped('学生管理'), wrapped('题库管理'), wrapped('教师首页')
  ], ['teacher'])

  assert.deepEqual(result.map(item => item.children[0].meta.title), [
    '教师首页', '题库管理', '学生管理'
  ])
})

test('教研员先看到统计监管业务，系统菜单靠后', () => {
  const result = sortSidebarRoutes(routes([
    '/system', '/teacher-tools', '/county-exam', '/schoolStats',
    '/monitor', '/research-activity', '/schoolScore'
  ]), ['admin', 'researcher'])

  assert.deepEqual(paths(result), [
    '/schoolStats', '/schoolScore', '/county-exam', '/research-activity',
    '/teacher-tools', '/system', '/monitor'
  ])
})

test('未配置角色时保持原有顺序', () => {
  const result = sortSidebarRoutes(routes(['/alpha', '/help-center', '/beta']), ['admin'])
  assert.deepEqual(paths(result), ['/alpha', '/help-center', '/beta'])
})

test('教师、双角色和教研员均保持教材、成绩、教研活动的指定相邻顺序', () => {
  const values = ['/score', '/studentguanli', '/textbook', '/teacher-tools', '/question', '/research-activity', '/business/guide-sheet-list']
  for (const roles of [['teacher'], ['researcher', 'teacher'], ['researcher'], ['admin']]) {
    const result = paths(sortSidebarRoutes(routes(values), roles))
    assert.equal(result.indexOf('/textbook'), result.indexOf('/question') + 1)
    assert.equal(result.indexOf('/score'), result.indexOf('/business/guide-sheet-list') + 1)
    assert.equal(result.indexOf('/research-activity'), result.indexOf('/studentguanli') - 1)
  }
})

test('同名隐藏阅读器和成绩详情不能抢占实际菜单定位', () => {
  const hidden = [
    { path: '/business/textbook/read/:id', hidden: true, meta: { title: '电子教材' } },
    { path: '/business/teacher/detail', hidden: true, meta: { title: '成绩查询' } }
  ]
  const visible = routes(['/studentguanli', '/question', '/textbook', '/score', '/business/guide-sheet-list', '/research-activity'])
  const result = paths(sortSidebarRoutes([...hidden, ...visible], ['researcher', 'teacher']).filter(route => !route.hidden))
  assert.equal(result.indexOf('/textbook'), result.indexOf('/question') + 1)
  assert.equal(result.indexOf('/score'), result.indexOf('/business/guide-sheet-list') + 1)
  assert.equal(result.indexOf('/research-activity'), result.indexOf('/studentguanli') - 1)
})
