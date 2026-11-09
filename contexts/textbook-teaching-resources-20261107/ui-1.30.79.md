# 移除汇编数量说明 · 1.30.79

2026-11-08发布130正式环境，更新157/PUBLISHED，真实教师更新列表可见。

用户明确要求删除数量说明。书架与汇编预览移除“覆盖4/20课”等数量，未新增跨来源统计；书架保留教学设计汇编及预览/下载，预览保留2026版标记。实际资料与审核来源元数据保持。

主要源码：`RuoYi-Vue3/src/views/business/textbook/index.vue`、`components/CompilationPreview.vue`；删除无引用的`compilationLabel.js`。仅同步这三项到正式隔离源码构建，未混入工作区其他在研功能。

验证：

- `npm run build:prod`：既有7+9+24共40项门禁、构建与发布检查通过。
- 本机制品及正式真实教师1440/390/320：13册汇编覆盖标签不存在，汇编预览仅显示2026版，页图加载、真实PDF下载、关闭正常；八上14课2023参考预览及重复课名、五年级29份课件/第28课暂缺回归通过，0页面JS异常。桌面本机与390px正式书架截图人工复看。
- 首页与验证码200，Nginx检查及reload成功，后端PID2984156/NRestarts0保持。
- 学生有效身份、机房整班未在本轮实测。
- 证据：`output/teaching-label-20261108/`内`artifact.json`、`verification-local.json`、`verification-live.json`、`published.json`及三尺寸截图。

发布与回退：

- 新前端`/data/apps/xueyeceping/releases/20261108_teaching_label_v1/frontend`，首页SHA-256 `3c464a7bf982b8ae7dd828a67b09da8e3405b0f41731eb81055344753342bc17`。
- 制品SHA-256 `510f881c615bc2cb8f469b719bbe439363451b5c9a09810dd8e23923d2f30184`，保留上一版静态资源后叠加。
- 备份`/data/backups/xueyeceping/20261108_teaching_label_v1/nginx.conf` SHA-256 `d1224892dee60714f9e5ee01eba59e420af2e4f6208d1ec70371f10f00c71284`。
- 回退确认当前仍为本release后恢复该配置，`nginx -t`并reload，前端回`20261108_teaching_ui_v1/frontend`（首页`8c5c328c`），更新157转DRAFT。无业务SQL/DDL、后端重启或数据回灌。
- 执行入口`work/teaching_label_release.py`；未Git提交或推送。
