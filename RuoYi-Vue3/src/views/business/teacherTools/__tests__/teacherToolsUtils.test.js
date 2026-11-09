import test from 'node:test'
import assert from 'node:assert/strict'
import {
  categoryToolsForDisplay,
  filterTeacherToolCatalog,
  filterTeacherTools,
  hasActiveFilters,
  isSafeHttpUrl,
  mergeCatalogTools,
  normalizeToolFilters,
  openTeacherTool,
  pickToolsByIds,
  pushRecentId,
  readFavoriteIds,
  readRecentIds,
  resolveAccessLabel,
  resolveTeacherToolCategoryIcon,
  resolveTeacherToolIcon,
  resolveTeacherToolTone,
  splitTags,
  toggleFavoriteId
} from '../teacherToolsUtils.js'

const tools = [
  { toolId: 1, title: 'Python编程', description: '浏览器编程', tags: '七年级,算法' },
  { toolId: 2, title: '在线打字', description: '中文和英文打字', tags: '小学,免登录' },
  { toolId: 3, title: '教师云盘', description: '保存教学资料', tags: '办公' },
  { toolId: 4, title: '流程图', description: '绘制流程', tags: '实用工具' },
  { toolId: 5, title: '图像压缩', description: '压缩图片', tags: '素材' }
]

test('标签兼容中文和英文分隔符', () => {
  assert.deepEqual(splitTags('七年级, 算法、免登录，内网；素材'), ['七年级', '算法', '免登录', '内网', '素材'])
})

test('搜索同时匹配名称、说明和标签，结果按 toolId 去重', () => {
  const result = filterTeacherToolCatalog({
    recommended: tools.slice(0, 2),
    categories: [{ categoryCode: 'grade7', tools }, { categoryCode: 'empty', tools: [] }]
  }, '免登录')
  assert.deepEqual(result.tools.map(tool => tool.toolId), [2])
})

test('次要分类默认只显示预览数量，搜索或展开后显示全部', () => {
  const category = { sectionLevel: 'SECONDARY', defaultExpanded: 'N', previewLimit: 4, tools }
  assert.equal(categoryToolsForDisplay(category, '', false).length, 4)
  assert.equal(categoryToolsForDisplay(category, '', true).length, 5)
  assert.equal(categoryToolsForDisplay(category, '图', false).length, 5)
})

test('同一个工具挂在推荐和多个分类时只保留一条，并合并分类归属', () => {
  const shared = { toolId: 10, title: '在线打字', description: '键盘练习', tags: '打字', recommendOrder: 5, sortOrder: 10 }
  const catalog = {
    recommended: [{ ...shared }],
    categories: [
      { categoryId: 1, categoryCode: 'zj', categoryName: '省平台', tools: [{ ...shared, categoryNameText: '省平台、学生免登录' }] },
      { categoryId: 2, categoryCode: 'no-login', categoryName: '学生免登录', tools: [{ ...shared }] }
    ]
  }
  const merged = mergeCatalogTools(catalog)
  assert.equal(merged.length, 1)
  assert.deepEqual(merged[0].categories, ['省平台', '学生免登录'])
  assert.equal(merged[0].recommended, true)
  assert.equal(merged[0].recommendedOrder, 5)
  assert.equal(filterTeacherTools(catalog, {}).length, 1)
})

test('推荐晚于分类到达时仍使用真实推荐顺序，不被兜底最大值覆盖', () => {
  const catalog = {
    categories: [{
      categoryId: 1,
      categoryCode: 'office',
      categoryName: '办公',
      tools: [
        { toolId: 1, title: '后推荐工具', recommendOrder: 30, sortOrder: 0 },
        { toolId: 2, title: '先推荐工具', recommendOrder: 10, sortOrder: 0 }
      ]
    }],
    recommended: [
      { toolId: 1, title: '后推荐工具', recommendOrder: 30 },
      { toolId: 2, title: '先推荐工具', recommendOrder: 10 }
    ]
  }
  // 分类先合并、推荐后到：必须回填 recommendOrder，按 10 在前而不是按 toolId 兜底。
  assert.deepEqual(mergeCatalogTools(catalog).map(tool => tool.toolId), [2, 1])
})

test('旧数组分类名同样能合并，不会留下逗号拼接串', () => {
  const catalog = {
    categories: [
      { categoryId: 1, categoryCode: 'zj', categoryName: '省平台', tools: [{ toolId: 10, title: '在线打字', description: '键盘练习', tags: '打字', categoryNames: ['省平台', '学生免登录'] }] }
    ]
  }
  const merged = mergeCatalogTools(catalog)
  assert.equal(merged.length, 1)
  assert.deepEqual(merged[0].categories, ['省平台', '学生免登录'])
})

test('名称和别名命中优先于用途、标签命中，同层级保持稳定顺序', () => {
  const catalog = {
    terms: [{ toolId: 7, term: 'PPT' }],
    categories: [{
      categoryId: 1,
      categoryCode: 'office',
      categoryName: '办公',
      tools: [
        { toolId: 11, title: '写作助手', description: '辅助制作 PPT 大纲', tags: 'AI' },
        { toolId: 12, title: '草稿本', description: '记录想法', tags: 'PPT' },
        { toolId: 13, title: '幻灯片模板', description: '模板下载', tags: '素材' },
        { toolId: 7, title: '演示助手', description: '生成幻灯片', tags: '' }
      ]
    }]
  }
  const result = filterTeacherTools(catalog, { keyword: 'ppt' })
  // 别名命中 7 在名称/别名层，用途命中 11、标签命中 12 依次在后。
  assert.deepEqual(result.map(tool => tool.toolId), [7, 11, 12])
})

test('多关键词需全部命中，忽略大小写与多余空格，无结果返回空数组', () => {
  const catalog = {
    categories: [{ categoryId: 1, categoryCode: 'office', categoryName: '办公', tools: [
      { toolId: 21, title: 'TinyPNG', description: '压缩 WebP、JPEG 和 PNG 图片', tags: '图片压缩' },
      { toolId: 22, title: '百度图片', description: '图片检索', tags: '图片' }
    ] }]
  }
  assert.deepEqual(filterTeacherTools(catalog, { keyword: '  tinypng  ' }).map(t => t.toolId), [21])
  assert.deepEqual(filterTeacherTools(catalog, { keyword: '图片  压缩' }).map(t => t.toolId), [21])
  assert.deepEqual(filterTeacherTools(catalog, { keyword: '视频转码' }), [])
})

test('教学任务、访问条件与关键词共同生效，清除条件后恢复全部', () => {
  const catalog = {
    tasks: [{ taskCode: 'material', taskName: '制作教学素材' }],
    categories: [{ categoryId: 1, categoryCode: 'office', categoryName: '办公', tools: [
      { toolId: 31, title: '图片压缩', description: '压缩图片', tags: '', taskTags: 'material', accessType: 'DIRECT', sourceType: 'MANUAL' },
      { toolId: 32, title: '云文档', description: '在线文档', tags: '', taskTags: 'material,resources', accessType: 'LOGIN_REQUIRED', sourceType: 'ZJ_DISCIPLINE' }
    ] }]
  }
  assert.deepEqual(filterTeacherTools(catalog, { taskCode: 'material' }).map(t => t.toolId), [31, 32])
  assert.deepEqual(filterTeacherTools(catalog, { taskCode: 'material', accessType: 'LOGIN_REQUIRED' }).map(t => t.toolId), [32])
  assert.deepEqual(filterTeacherTools(catalog, { taskCode: 'material', keyword: '压缩' }).map(t => t.toolId), [31])
  assert.deepEqual(filterTeacherTools(catalog, { sourceType: 'ZJ_DISCIPLINE', categoryCode: 'office' }).map(t => t.toolId), [32])
  assert.equal(hasActiveFilters({ keyword: '  ', taskCode: '' }), false)
  assert.deepEqual(normalizeToolFilters({ keyword: '  图片 ', accessType: 'DIRECT' }), { accessType: 'DIRECT', keyword: '图片' })
})

test('访问条件只说真实状态，不把直接打开说成免登录', () => {
  assert.equal(resolveAccessLabel({ accessType: 'DIRECT' }), '直接打开')
  assert.equal(resolveAccessLabel({ accessType: 'LOGIN_REQUIRED' }), '需登录')
  assert.equal(resolveAccessLabel({ accessType: 'INTRANET_ONLY' }), '限内网')
  assert.equal(resolveAccessLabel({ accessType: 'DOWNLOAD' }), '下载工具')
  assert.equal(resolveAccessLabel({}), '')
})

function memoryStorage() {
  const data = new Map()
  return {
    getItem: key => (data.has(key) ? data.get(key) : null),
    setItem: (key, value) => data.set(key, String(value))
  }
}

test('我的常用按账号隔离，可加入、移除并在刷新后恢复', () => {
  const storage = memoryStorage()
  assert.deepEqual(readFavoriteIds('u1', storage), [])
  const added = toggleFavoriteId('u1', 5, storage)
  assert.deepEqual(added, { ids: [5], added: true })
  toggleFavoriteId('u1', 9, storage)
  // 新加入的在前，但已有条目顺序不因一次点击重排。
  assert.deepEqual(readFavoriteIds('u1', storage), [9, 5])
  assert.deepEqual(readFavoriteIds('u2', storage), [])
  const removed = toggleFavoriteId('u1', 5, storage)
  assert.deepEqual(removed, { ids: [9], added: false })
})

test('收藏 ID 统一转成正整数，避免类型不一致导致回显丢失', () => {
  const storage = memoryStorage()
  storage.setItem('teacher-tools-favorites:u1', JSON.stringify(['5', 0, -1, 1.5, 'abc', 9]))
  assert.deepEqual(readFavoriteIds('u1', storage), [5, 9])
  const catalogTools = [{ toolId: 5, title: '字符串目录' }, { toolId: 9, title: '数字目录' }]
  assert.deepEqual(pickToolsByIds(catalogTools, ['5', '9']).map(tool => tool.toolId), [5, 9])
})

test('最近打开只保留会话内打开顺序并去重', () => {
  const storage = memoryStorage()
  pushRecentId('u1', 1, storage)
  pushRecentId('u1', 2, storage)
  pushRecentId('u1', 1, storage)
  assert.deepEqual(readRecentIds('u1', storage), [1, 2])
  for (let id = 10; id < 20; id += 1) pushRecentId('u1', id, storage)
  assert.equal(readRecentIds('u1', storage).length, 6)
})

test('下架或删除的工具即使仍在常用里也不再展示', () => {
  const catalogTools = [{ toolId: 1, title: '仍然存在' }]
  assert.deepEqual(pickToolsByIds(catalogTools, [1, 2, 3]).map(tool => tool.toolId), [1])
})

test('只允许不带账号信息的HTTP和HTTPS地址', () => {
  assert.equal(isSafeHttpUrl('https://example.com/tool'), true)
  assert.equal(isSafeHttpUrl('http://10.52.1.123:3000/'), true)
  assert.equal(isSafeHttpUrl('javascript:alert(1)'), false)
  assert.equal(isSafeHttpUrl('https://user:pwd@example.com'), false)
})

// 用最小 DOM 替身模拟同源空白标签页：真实浏览器里 noopener 会让 window.open 返回 null，
// 所以实现必须先拿空白句柄，再在页内点一个 noopener/noreferrer 链接完成跳转。
function fakeBlankWindow() {
  const links = []
  const opened = {
    opener: 'parent',
    closed: false,
    body: { appendChild: node => links.push(node) },
    document: {
      body: null,
      createElement: tag => ({ tag, clicked: 0, click() { this.clicked += 1 } })
    },
    close() { this.closed = true }
  }
  opened.document.body = opened.body
  return { opened, links }
}

test('工具用同源空白页承接跳转，既隔离 opener 又能识别真实拦截', () => {
  const { opened, links } = fakeBlankWindow()
  const calls = []
  const ok = openTeacherTool({ url: 'https://example.com/tool' }, (...args) => {
    calls.push(args)
    return opened
  })
  assert.equal(ok, true)
  // 首次必须是空白地址：带目标地址又带 noopener 时返回值不可靠。
  assert.deepEqual(calls, [['', '_blank']])
  assert.equal(opened.opener, null)
  assert.equal(links.length, 1)
  assert.equal(links[0].href, 'https://example.com/tool')
  assert.equal(links[0].target, '_self')
  assert.equal(links[0].rel, 'noopener noreferrer')
  assert.equal(links[0].clicked, 1)

  assert.equal(openTeacherTool({ url: 'javascript:alert(1)' }, () => opened), false)
  // 浏览器拦截弹窗时 window.open 返回 null，调用方不能再把这次操作记入最近打开。
  assert.equal(openTeacherTool({ url: 'https://example.com/tool' }, () => null), false)
})

test('空白页拿不到可用文档时关闭标签并如实报告失败', () => {
  const broken = { opener: 'parent', closed: false, close() { this.closed = true } }
  assert.equal(openTeacherTool({ url: 'https://example.com/tool' }, () => broken), false)
  assert.equal(broken.closed, true)
  assert.equal(openTeacherTool({ url: 'https://example.com/tool' }, () => { throw new Error('blocked') }), false)
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
