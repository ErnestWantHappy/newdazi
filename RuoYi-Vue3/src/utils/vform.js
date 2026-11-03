let appRef
let loading

export function setVFormApp(app) {
  appRef = app
}

export function resolveVFormPlugin(module) {
  const isPlugin = value => typeof value === 'function' || typeof value?.install === 'function'
  if (isPlugin(module)) return module
  if (isPlugin(module?.default)) return module.default
  for (const value of Object.values(module || {})) {
    if (isPlugin(value)) return value
    if (isPlugin(value?.default)) return value.default
  }
  throw new Error('导学单组件加载失败，请刷新重试')
}

export function needsVForm(path) {
  // 学生首页和课程面板也会嵌入导学单，不能只覆盖独立设计器地址。
  return /guide-?sheet|\/lesson(?:\/|$)|^\/student\/index(?:\/|$)/i.test(path)
}

export function ensureVForm() {
  if (!loading) {
    loading = Promise.all([
      import('vform3-builds'),
      import('vform3-builds/dist/designer.style.css'),
    ]).then(([module]) => {
      if (!appRef) throw new Error('导学单尚未初始化，请刷新重试')
      // 生产 UMD 导出可能位于 module.d.default，不能依赖打包后的变量名称。
      appRef.use(resolveVFormPlugin(module))
    }).catch(error => {
      loading = undefined
      throw error
    })
  }
  return loading
}
