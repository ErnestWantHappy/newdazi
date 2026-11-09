# 教师工具卡片样式修复验收

日期：2026-11-09  
环境：130 正式站（Linux / systemd / Nginx）  
状态：**已随 1.30.83 前端包生效，真实教师页面验收通过**

## 问题与原因

教师工具页的 `ToolCard`、`ToolIcon` 由 render 函数动态创建，内部元素没有父组件的 `scoped` 标识；原卡片、图标、色调和标签规则放在 `<style scoped>`，浏览器实际没有应用，只剩白底黑字。

## 修复

- 源代码：`RuoYi-Vue3/src/views/business/teacherTools/index.vue`
- 将卡片、图标、色调、标签、悬浮态等动态渲染样式移到非 scoped 样式块。
- 所有规则统一加 `.teacher-tools-page` 前缀，避免影响其他页面。
- 分区、个人常用和教研精选增加克制的浅色渐变、边框和圆角层次。
- 修复源码 SHA-256：`A0AF7F3DC87A6279FF983E11E353DDDF4C72972DA63A0D0DF5CCCBD85BB10E4C`

## 正式发布状态

- 当前正式版本：`1.30.83`
- Nginx 前端根：`/data/apps/xueyeceping/releases/20261109_lesson_designer_layout_v2/frontend`
- 当前首页 SHA-256：`0d2aa2343758c62bf3c4d3db67331bb0a04ae2b26226de8d897122566c6246bc`
- 教师工具页面 CSS：`static/css/index-D9yAGKHg.css`，9,000B，SHA-256 `B3B7D751FA24FEFE12CCA360A613FB7AE35301FC490685DDD70EE2C58A44AA38`
- 后端仍为 `releases/20261109_choice_media_v1`，MainPID `3019157`，三服务 active、NRestarts 0；本次没有重启后端。
- 教师工具样式本身没有单独占用版本号，已包含在 1.30.83 的前端制品中，产品更新 161/1.30.83 保持 PUBLISHED；之后 1.30.84 用于品牌标识发布，与本样式修复无关。

## 正式浏览器验收

使用真实教师账号打开 `http://10.52.1.130/teacher-tools/index`，不注入 CSS、不使用模拟接口：

- 11/11 项通过；页面显示 65 张工具卡。
- 图标恢复为 `44px × 44px` 柔和色块，实测颜色如 `rgb(217, 119, 6)`、底色 `rgb(255, 243, 221)`。
- 标签恢复 `1px 7px` 胶囊内边距和底色。
- 分区恢复浅蓝渐变、1px 边框和 12px 圆角。
- 出现 `tone-orange`、`tone-cyan`、`tone-green`、`tone-purple`、`tone-blue`、`tone-rose` 六组色调。
- 1440、390、320 宽度均无横向溢出。
- 0 页面脚本异常、0 控制台错误、0 个 5xx 响应。
- 截图：`output/playwright/teacher-tools-style-release-20261109/live-1440.png`、`live-390.png`、`live-320.png`
- 结果：`output/playwright/teacher-tools-style-release-20261109/acceptance.json`

## 验证边界

- 本轮使用现代 Chromium 真实正式页验收；老旧机房浏览器未单独实测。
- 本次不修改后端、数据库结构、业务数据、课程设计布局或产品更新记录。
- 1.30.83 的原课程设计页上下布局验收继续有效，证据见 `output/release-20261109-lesson-layout/deploy-result.json` 与 `output/playwright/lesson-layout-20261109/`。
