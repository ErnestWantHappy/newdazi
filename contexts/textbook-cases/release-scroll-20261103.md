# 课例连续滚动预览发布证据

## 2026-11-03 · 1.30.70 · 优秀课例上下连续滚动预览（仅前端）

130前端切换至`20261103_case_scroll_v1/frontend`，首页SHA256 `7d8e056a9e32112307841d17b8799298d79c67100de4b20053889711348331d7`。后端仍为`20261103_case_feedback_v2`、PID `2885391`，unit/env/JAR未变，三服务active，无后端重启。更新`148	1.30.70	PUBLISHED	2026-11-03 22:37:23`，真实教师更新列表可见。

课件、教学设计及其他PDF页图预览改为连续上下阅读，鼠标滚轮/手机触摸自然衔接下一页；当前页码和缩略图高亮同步，保留前后页、输入页码跳转与缩放。只渲染可见窗口及邻近页，退出窗口撤销图片URL；缩略图缓存最多24项，正文优先串行调度。横竖页按实际图片尺寸校准，缩放/尺寸变化保留阅读锚点，避免长文档全量加载及跳页中间态错误请求。单页失败就地重试，加载/失败状态继续与评价邀请计时联动。

涉及源码：`RuoYi-Vue3/src/views/business/textbook/components/CasePreview.vue`、`src/utils/casePreviewScroll.js`及4项数学/窗口测试。未增加依赖，不改业务接口/评价/权限/成绩/资源材料，不混入概览/登录等后端本机未发布改动。

验证：连续布局/跳页/混合横竖尺寸/空文档4项通过；前端生产构建含7+9项既有测试及发布门禁通过。本机真实滚轮/触摸与正式1500×900、1366×600、390×844、320×568通过，页码/缩略图/跳页正确且无页面JS错误。本机另用200页模拟数据验证直接跳到150页、混合方向、失败重试与缩放保位，未加载全部页面；实测正文图片常驻约3–5张（随视口/缩放变化，不是固定页数限制）。学校旧浏览器和低配机房未实测。

备份`/data/backups/xueyeceping/20261103_case_scroll_v1`：整库gzip 52900220字节，SHA256 `ef07bc1d3ff3d014ad74f8fab8ef51b53ae85060e00ba4eb26f91f4dff95cdc2`，gzip和完成标记已核；含旧Nginx、unit/env/JAR/config、首页及SHA256SUMS。旧static/assets并入新前端，旧release保留。

无业务结构SQL。仅验收后执行`sql/textbook_case_scroll_release_20261103.sql`登记产品更新。回退先确认活动前端仍为本release，再执行`python output/release-20261103-case-scroll/scroll_release.py rollback`：恢复旧Nginx根至1.30.69、nginx -t/reload，更新1.30.70转DRAFT；无需后端重启或业务库回灌。

证据：`output/release-20261103-case-scroll/`中的baseline/manifest/backup/switch/publish及verification.log；`output/playwright/textbook-case-scroll/`中的local-result.json、production-result.json和各尺寸截图；本机scroll-unit.log、scroll-ui.log、scroll-build.log。
