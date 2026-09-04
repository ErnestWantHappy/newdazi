import assert from 'node:assert/strict'
import test from 'node:test'
import { sortSidebarRoutes } from '../sidebarMenuOrder.js'

const routes = paths => paths.map(path => ({ path }))
const paths = values => values.map(item => item.path)

test('教师菜单按高频教学流程排序且帮助中心固定在末尾', () => {
  const result = sortSidebarRoutes(routes([
    '/studentguanli', '/help-center', '/teacher-tools', '/teacher-dashboard',
    '/teacherClass', '/score', '/question'
  ]), ['teacher'])

  assert.deepEqual(paths(result), [
    '/teacher-dashboard', '/question', '/score', '/studentguanli',
    '/teacherClass', '/teacher-tools', '/help-center'
  ])
})

test('兼容若依单菜单生成的根路径斜杠包装结构', () => {
  const wrapped = title => ({ path: '/', children: [{ path: 'wrapped', meta: { title } }] })
  const result = sortSidebarRoutes([
    wrapped('学生管理'), wrapped('帮助中心'), wrapped('题库管理'), wrapped('教师首页')
  ], ['teacher'])

  assert.deepEqual(result.map(item => item.children[0].meta.title), [
    '教师首页', '题库管理', '学生管理', '帮助中心'
  ])
})

test('教研员先看到统计监管业务，系统菜单靠后，帮助中心固定在末尾', () => {
  const result = sortSidebarRoutes(routes([
    '/system', '/help-center', '/teacher-tools', '/county-exam', '/schoolStats',
    '/monitor', '/research-activity', '/schoolScore'
  ]), ['admin', 'researcher'])

  assert.deepEqual(paths(result), [
    '/schoolStats', '/schoolScore', '/county-exam', '/research-activity',
    '/teacher-tools', '/system', '/monitor', '/help-center'
  ])
})

test('未配置角色时保持原有顺序，但帮助中心仍置底', () => {
  const result = sortSidebarRoutes(routes(['/alpha', '/help-center', '/beta']), ['admin'])
  assert.deepEqual(paths(result), ['/alpha', '/beta', '/help-center'])
})
