import request from '@/utils/request'

/** 书架：上架册列表 */
export function listTextbookBooks() {
  return request({
    url: '/business/textbook/books',
    method: 'get'
  })
}

/** 目录树：册 + 单元（导入/课次/小结）+ 前置 + 附录 + 页元数据 */
export function getTextbookToc(bookId) {
  return request({
    url: `/business/textbook/books/${bookId}/toc`,
    method: 'get'
  })
}

/** 页元数据区间，from/to 为闭区间，可省略 */
export function listTextbookPages(bookId, from, to) {
  return request({
    url: `/business/textbook/books/${bookId}/pages`,
    method: 'get',
    params: { from, to }
  })
}

// 页面图必须带 Authorization 头，<img src> 直连会 401，所以统一按 blob 取回再转 objectURL。
// 缓存既服务滚动阅读，也服务"打印本课"：打印前把该课页面全部预取，避免缺页。
const PAGE_CACHE_LIMIT = 80
const pageCache = new Map()
// 已从缓存移出、但可能还被 <img> 或打印窗口引用的 URL：立刻 revoke 会让在途图片解码失败，
// 因此先登记，等离开阅读器时统一释放。
const orphanUrls = new Set()
// 打印窗口可能在阅读器卸载后继续用图：被打印持有的 URL 即使离开阅读器也不能立刻 revoke，
// 先记下这次撤销请求，等打印结束释放引用时再真正释放。
const retainedUrls = new Map()
const pendingRevokeUrls = new Set()

function deferRevoke(url) {
  if (url) orphanUrls.add(url)
}

function revokeUrl(url) {
  if (!url) return
  if (retainedUrls.has(url)) {
    pendingRevokeUrls.add(url)
    return
  }
  URL.revokeObjectURL(url)
}

/** 打印期间持有页面 URL，避免路由切换触发的清理把打印窗口正在用的图撤销 */
export function retainTextbookUrls(urls) {
  urls.forEach(url => {
    if (url) retainedUrls.set(url, (retainedUrls.get(url) || 0) + 1)
  })
}

export function releaseTextbookUrls(urls) {
  urls.forEach(url => {
    const count = retainedUrls.get(url)
    if (!count) return
    if (count > 1) {
      retainedUrls.set(url, count - 1)
      return
    }
    retainedUrls.delete(url)
    if (pendingRevokeUrls.delete(url)) URL.revokeObjectURL(url)
  })
}

/** 把 entry 移出缓存；只在仍是同一个 entry 时动手，避免删掉后来者新取的页 */
function dropEntry(key, entry) {
  const current = pageCache.get(key)
  if (!current || (entry && current !== entry)) return
  pageCache.delete(key)
  deferRevoke(current.url)
}

function evictOldest() {
  while (pageCache.size > PAGE_CACHE_LIMIT) {
    const [key, entry] = pageCache.entries().next().value
    pageCache.delete(key)
    if (entry.url) {
      revokeUrl(entry.url)
    } else if (entry.promise) {
      // 在途请求：revoke 会打断正在等同一 promise 的 <img>，交给离开阅读器时释放
      entry.promise.then(url => deferRevoke(url)).catch(() => {})
    }
  }
}

/**
 * 取单页图片的 objectURL，带 LRU 缓存。
 * force=true 时丢弃缓存重新请求，供页面上的"点击重试"使用。
 */
export function loadTextbookPage(bookId, pageNo, force = false) {
  const key = `${bookId}/${pageNo}`
  if (force) {
    const current = pageCache.get(key)
    // 重试时已有同页请求在途：复用它，避免重复请求和旧 URL 无人释放
    if (current && !current.url && current.promise) return current.promise
    dropEntry(key)
  }
  return cachedBlobRequest(key, `/business/textbook/page/${bookId}/${pageNo}`)
}

/** 书架封面：后端返回 360px 缩略图，缺失时自动回退第 1 页原图。 */
export async function loadTextbookCover(bookId) {
  try {
    return await cachedBlobRequest(`cover/${bookId}`, `/business/textbook/books/${bookId}/cover`)
  } catch (error) {
    // 前端先于后端上线时封面接口可能还不存在，回退原页图，书架不至于整片空白。
    return loadTextbookPage(bookId, 1)
  }
}

function cachedBlobRequest(key, url) {
  const hit = pageCache.get(key)
  if (hit) {
    // 命中后移到队尾，保证 LRU 淘汰的是最久未看的页
    pageCache.delete(key)
    pageCache.set(key, hit)
    return hit.promise
  }
  const entry = { url: null, promise: null }
  entry.promise = request({
    url,
    method: 'get',
    responseType: 'blob',
    // 失败由页面显示行内提示；否则一册多页缺失会连续弹全局 toast
    silentError: true
  }).then(blob => {
    const url = URL.createObjectURL(blob)
    entry.url = url
    // 期间被重试或淘汰掉的 entry 不再由缓存持有，登记到孤儿集合，离开阅读器时统一释放
    if (pageCache.get(key) !== entry) deferRevoke(url)
    return url
  }).catch(error => {
    // 失败的 entry 必须移出缓存：留着会让"点击重试"永远命中同一条 rejected promise，
    // 页面从此无法恢复，且不会再发请求。
    dropEntry(key, entry)
    throw error
  })
  pageCache.set(key, entry)
  evictOldest()
  return entry.promise
}

/** 离开阅读器时释放 blob，避免长时间占用内存 */
export function clearTextbookPageCache() {
  pageCache.forEach(entry => {
    if (entry.url) revokeUrl(entry.url)
    else if (entry.promise) entry.promise.then(url => revokeUrl(url)).catch(() => {})
  })
  pageCache.clear()
  orphanUrls.forEach(url => revokeUrl(url))
  orphanUrls.clear()
}

/**
 * 缺册提示用：3-9 年级共 13 册（九年级全一册），2026-10-29 已全部导入上架。
 * 若将来再出现缺册，把对应 { grade, semester, shortTitle } 加回这里即可显示灰卡。
 */
export const TEXTBOOK_PENDING_VOLUMES = []

/** 整册 PDF：教师端下载，后端按页图合成，首次可能耗时较长 */
export function downloadTextbookBookPdf(bookId) {
  return request({
    url: `/business/textbook/books/${bookId}/pdf`,
    method: 'get',
    responseType: 'blob',
    timeout: 180000,
    silentError: true
  })
}

/** 本课 PDF：按课次起止页合成，教师端下载 */
export function downloadTextbookSectionPdf(bookId, sectionKey) {
  return request({
    url: `/business/textbook/books/${bookId}/sections/${encodeURIComponent(sectionKey)}/pdf`,
    method: 'get',
    responseType: 'blob',
    timeout: 120000,
    silentError: true
  })
}
