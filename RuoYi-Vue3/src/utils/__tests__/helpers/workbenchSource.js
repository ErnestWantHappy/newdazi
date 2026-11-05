// 从单文件组件里抽出 <script setup> 的纯逻辑片段，供 node --test 做静态断言（不挂载组件、不装测试框架）。
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

export const frontRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../../..')

export function read(rel) {
  return fs.readFileSync(path.join(frontRoot, '..', rel), 'utf8')
}

/** 只取 <script ...> 与 </script> 之间的内容，模板里的同名文字不会干扰断言。 */
export function scriptOf(rel) {
  const text = read(rel)
  const match = text.match(/<script[^>]*>([\s\S]*?)<\/script>/)
  if (!match) throw new Error(`${rel} 没有 script 段`)
  return match[1]
}
