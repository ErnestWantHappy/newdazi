import test from 'node:test'
import assert from 'node:assert/strict'
import {
  categoryToolsForDisplay,
  filterTeacherToolCatalog,
  isSafeHttpUrl,
  openTeacherTool,
  resolveTeacherToolCategoryIcon,
  resolveTeacherToolIcon,
  resolveTeacherToolTone,
  splitTags
} from '../teacherToolsUtils.js'

const tools = [
  { toolId: 1, title: 'Python编程', description: '浏览器编程', tags: '七年级,算法' },
  { toolId: 2, title: '在线打字', description: '中文和英文打字', tags: '小学,免登录' },
  { toolId: 3, title: '教师云盘', description: '保存教学资料', tags: '办公' },
  { toolId: 4, title: '流程图', description: '绘制流程', tags: '实用工具' },
  { toolId: 5, title: '图像压缩', description: '压缩图片', tags: '素材' }
]

test('标签兼容中文和英文分隔符', () => {
  assert.deepEqual(splitTags('七年级, 算法、免登录，内网'), ['七年级', '算法', '免登录', '内网'])
})

test('搜索同时匹配名称、说明和标签并移除空分类', () => {
  const result = filterTeacherToolCatalog({
    recommended: tools.slice(0, 2),
    categories: [{ categoryCode: 'grade7', tools }, { categoryCode: 'empty', tools: [] }]
  }, '免登录')
  assert.equal(result.recommended.length, 1)
  assert.equal(result.categories.length, 1)
  assert.equal(result.categories[0].tools[0].title, '在线打字')
})

test('次要分类默认只显示预览数量，搜索或展开后显示全部', () => {
  const category = { sectionLevel: 'SECONDARY', defaultExpanded: 'N', previewLimit: 4, tools }
  assert.equal(categoryToolsForDisplay(category, '', false).length, 4)
  assert.equal(categoryToolsForDisplay(category, '', true).length, 5)
  assert.equal(categoryToolsForDisplay(category, '图', false).length, 5)
})

test('只允许不带账号信息的HTTP和HTTPS地址', () => {
  assert.equal(isSafeHttpUrl('https://example.com/tool'), true)
  assert.equal(isSafeHttpUrl('http://10.52.1.123:3000/'), true)
  assert.equal(isSafeHttpUrl('javascript:alert(1)'), false)
  assert.equal(isSafeHttpUrl('https://user:pwd@example.com'), false)
})

test('工具使用带隔离参数的新标签页打开', () => {
  let args
  const opened = { opener: 'parent' }
  const ok = openTeacherTool({ url: 'https://example.com/tool' }, (...values) => {
    args = values
    return opened
  })
  assert.equal(ok, true)
  assert.deepEqual(args, ['https://example.com/tool', '_blank', 'noopener,noreferrer'])
  assert.equal(opened.opener, null)
})

test('根据工具用途分配不同图标和色彩，无法识别的下载项使用下载图标', () => {
  assert.equal(resolveTeacherToolIcon({ title: 'Python 编程', tags: '算法' }, 'zj-discipline'), 'code')
  assert.equal(resolveTeacherToolIcon({ title: 'PDF24 工具箱' }, 'office-materials'), 'pdf')
  assert.equal(resolveTeacherToolIcon({ title: '在线打字' }, 'student-no-login'), 'input')
  assert.equal(resolveTeacherToolIcon({ title: 'AI 音乐学习工具' }, 'cross-subject'), 'language')
  assert.equal(resolveTeacherToolIcon({ title: '课堂随机点名', accessType: 'DOWNLOAD' }, 'homeroom-tools'), 'people')
  assert.equal(resolveTeacherToolIcon({ title: '桌面小工具', accessType: 'DOWNLOAD' }, 'office-materials'), 'download')
  assert.notEqual(resolveTeacherToolTone({ title: 'Python 编程' }, 'programming-learning'), resolveTeacherToolTone({ title: 'AI 音乐' }, 'cross-subject'))
})

test('无效或缺失的分类图标回退到分类专属图标', () => {
  assert.equal(resolveTeacherToolCategoryIcon({ categoryCode: 'ai-websites', icon: 'chat' }), 'message')
  assert.equal(resolveTeacherToolCategoryIcon({ categoryCode: 'homeroom-tools' }), 'people')
  assert.equal(resolveTeacherToolCategoryIcon({ categoryCode: 'unknown' }), 'tool')
})
