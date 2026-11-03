import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { gzipSync } from 'node:zlib'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const dist = path.resolve(process.argv[2] || path.join(root, 'RuoYi-Vue3/dist'))
const requireCheck = (passed, message) => { if (!passed) throw new Error(`前端发布检查失败：${message}`) }
const html = fs.readFileSync(path.join(dist, 'index.html'), 'utf8')
const entryPath = html.match(/<script[^>]+src="([^"]+)"/)?.[1]
requireCheck(entryPath, '首页缺少入口脚本')
const entry = fs.readFileSync(path.join(dist, entryPath.replace(/^\//, '')))
requireCheck(gzipSync(entry).length < 650000, '入口 gzip 超过 650KB，请核对是否重新全局导入导学单编辑器')
const loginName = entry.toString().match(/(?:\.\/|static\/js\/)(login-[\w-]+\.js)/)?.[1]
requireCheck(loginName, '无法定位登录模块')
const login = fs.readFileSync(path.join(dist, 'static/js', loginName), 'utf8')
requireCheck(!/\/static\/(png\/logo-|jpg\/slide-[123]-)/.test(login), '登录模块重新引用未优化的 Logo 或 JPG 兜底图')
requireCheck(/\/static\/webp\/logo-/.test(login), '登录模块未引用 WebP Logo')
console.log(`前端发布检查通过：入口 gzip ${gzipSync(entry).length}B，登录图片引用正确`)
