const LAST_MENU_WEIGHT = 10000

const teacherOrder = [
  ['教师首页', '/teacher-dashboard'],
  ['题库管理', '/question'],
  ['成绩查询', '/score'],
  ['学生管理', '/studentguanli'],
  ['班级管理', '/teacherClass'],
  ['教师工具', '/teacher-tools'],
  ['导学单管理', '/business/guide-sheet-list'],
  ['教研活动', '/research-activity'],
  ['学生个人成绩画像', '/student-profile'],
  ['免抽测申请', '/teacher-exemption']
]

const researcherOrder = [
  ['学校统计', '/schoolStats'],
  ['课程与成绩监管', '/schoolScore'],
  ['区域抽测', '/county-exam'],
  ['教研活动', '/research-activity'],
  ['免抽测申请审核', '/exemption-review'],
  ['教师工具', '/teacher-tools'],
  ['导学单管理', '/business/guide-sheet-list'],
  ['学生管理', '/studentguanli'],
  ['题库管理', '/question'],
  ['系统管理', '/system'],
  ['系统监控', '/monitor']
]

function buildWeights(groups) {
  const weights = new Map()
  groups.forEach((aliases, index) => {
    aliases.forEach(alias => {
      weights.set(alias, index * 10)
      if (alias.startsWith('/')) weights.set(alias.slice(1), index * 10)
    })
  })
  return weights
}

function collectRouteKeys(route, keys = new Set()) {
  if (!route) return keys
  if (route.path) {
    keys.add(route.path)
    keys.add(route.path.replace(/^\//, ''))
  }
  if (route.meta?.title) keys.add(route.meta.title)
  ;(route.children || []).forEach(child => collectRouteKeys(child, keys))
  return keys
}

function routeWeight(route, weights, originalIndex) {
  const keys = collectRouteKeys(route)
  if (keys.has('/help-center') || keys.has('help-center') || keys.has('帮助中心')) return LAST_MENU_WEIGHT
  const matchedWeights = [...keys].filter(key => weights.has(key)).map(key => weights.get(key))
  if (matchedWeights.length) return Math.min(...matchedWeights)
  // 未明确列出的既有菜单保留后台相对顺序，避免新增功能被意外隐藏或打乱。
  return 5000 + originalIndex
}

/**
 * 只调整侧边栏顶级菜单，不改变后端权限、路由内容或系统管理内部顺序。
 */
export function sortSidebarRoutes(routes, roles = []) {
  const roleSet = new Set(roles || [])
  const order = roleSet.has('teacher')
    ? teacherOrder
    : roleSet.has('researcher')
      ? researcherOrder
      : []
  const weights = buildWeights(order)

  return routes
    .map((route, index) => ({ route, index }))
    .sort((left, right) => {
      const difference = routeWeight(left.route, weights, left.index) - routeWeight(right.route, weights, right.index)
      return difference || left.index - right.index
    })
    .map(item => item.route)
}
