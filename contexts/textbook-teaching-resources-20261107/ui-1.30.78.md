# 教材资源页面精简 · 1.30.78

2026-11-08发布130正式环境，更新156/PUBLISHED，真实教师更新列表可见。

用户反馈说明小字过多、课名和2023参考重复。书架删除副标题、每册长描述、指南说明段与重复用途声明；汇编保留“覆盖4/20课”等短标记。指南页删除重复解释，保留实际数量和缺课状态。资源面板相同课名只展示一次，2023参考只留标签，移除原始路径与收录说明；必要运行条件通过“使用说明”展开。作者、评价、预览、下载、错误重试保持。

主要源码：`RuoYi-Vue3/src/views/business/textbook/`下`index.vue`、`guide.vue`、`components/CasePanel.vue`、`components/CompilationPreview.vue`及`compilationLabel.js`。共4个Vue文件、1个工具文件；基于已正式1.30.77隔离源码构建，未混入图片题/教师工具等在研功能。

验证：

- `npm run build:prod`：40项既有门禁与构建、发布检查通过。
- 本机制品接正式只读接口及正式真实教师各完成1440/390/320检查：书架汇编真实页图、PDF真实下载、关闭、八上第14课标题去重、2023参考真实预览、阅读页汇编4/20、五年级29份课件/第28课暂缺、指南无横向溢出；0页面JS异常。桌面书架、手机书架与资源面板截图已人工复看。
- 正式首页及验证码200，Nginx语法检查/reload成功，后端PID2984156/NRestarts0保持。学生有效身份、机房整班和所有工具包运行条件未重验。
- 证据：`output/teaching-ui-20261108/verification-local.json`、`verification-live.json`、`published.json`及`shelf/case/guide/compilation-live-*.png`。

发布与回退：

- 新前端`/data/apps/xueyeceping/releases/20261108_teaching_ui_v1/frontend`；首页SHA-256 `8c5c328c9e242899769e69cf64c47861004f82943b4b01a496c179c835b9a49e`。
- 制品SHA-256 `f37208165a26429df0dc555fe6095e06899c3325d1dda6f5339ff9a37121b8ac`；新release保留前版静态资源，仅叠加新制品。
- 备份`/data/backups/xueyeceping/20261108_teaching_ui_v1/nginx.conf` SHA-256 `c8ef47d3fe1b26fd40b0b87f57d65104c57550b2d2680eef037be5739e3e85db`。旧前端`20261108_teaching_hotfix_v1/frontend`首页`9aa45089`完整保留。
- 无业务迁移、无后端重启、无配置/依赖更换，仅新增平台发布登记。回退时确认当前仍为本release后，恢复上述Nginx备份，`nginx -t`及reload，将更新156转DRAFT；不用业务回退SQL或整库回灌。
- 执行入口`work/teaching_ui_release.py`；未Git提交或推送。来源完整性、缺课边界和资料净化仍以[1.30.77验收](hotfix-1.30.77.md)为准。
