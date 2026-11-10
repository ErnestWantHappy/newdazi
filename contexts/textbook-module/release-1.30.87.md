# 高中教材与课程标准发布验收（1.30.87）

状态：**130正式已上线，产品更新165/PUBLISHED，真实教师接口可见**

## 1. 发布内容

- 电子教材书架新增“高中”分组：必修1《数据与计算》193页、必修2《信息系统与社会》161页、选择性必修1《数据与数据结构》185页，共3册539页、15单元、82目录条目。
- 新增独立“课程标准”资料区：义务教育信息科技课程标准（2022年版）74页、普通高中信息科技课程标准（2017年版2025年修订）64页；支持在线逐页预览、PDF下载。
- 原有小学、初中13册1301页、课程绑定、学生绑定教材入口、整册/本课PDF、327课例/1,050资源保持不变；教研活动课例`research-post-15/60/70`仍为PUBLISHED。
- 课程标准不伪装成教材课次，不参与课程绑定；教师、教研员、管理员可用，学生查看/下载拒绝，匿名下载401。

## 2. 正式版本与数据

| 项目 | 结果 |
| --- | --- |
| 后端release | `/data/apps/xueyeceping/releases/20261109_high_school_textbooks_v1` |
| 前端release | `/data/apps/xueyeceping/releases/20261109_high_school_overview_guard_v1/frontend` |
| 后端JAR | `cbd0042c56b217662229e570192a58e6f94afe5e6e551e344bda72db6d41f1b8`，108,296,532B |
| 首页 | `246b85165a8273239073759646b92e7bab8548197551faed9b1d025edb82a458`，820前端文件 |
| 服务 | MainPID`3089590`、NRestarts0、ActiveState active、首页/验证码200 |
| 数据库 | 16册/1840页；高中ID14/15/16、539页、15单元、82目录条目；重复页0 |
| 课程标准 | `biz_textbook_standard`2行：`it-2022`74页、`hs-it-2025`64页 |
| 产品更新 | `165 / 1.30.87 / PUBLISHED`，真实教师`/business/platform-update/list`可见 |

SQL：`sql/high_school_textbooks_20261109.sql`（270,004B，SHA-256`cd91ccca1405168c9bd275608dc5d00b096e5a832fa31b262c828572d4d5ba48`），幂等；产品更新`sql/platform_update_1_30_87_20261109.sql`。

资源：

- 高中页图：`shared/textbook/xxkj-hs-b1-2019`、`xxkj-hs-b2-2019`、`xxkj-hs-e1-2019`，共539张。
- 课标：`shared/textbook/cases/standards`，2个PDF。
- 封面：`shared/textbook/covers/14.jpg`、`15.jpg`、`16.jpg`。
- `shared/textbook/cases`仍指向`releases/20261107_teaching_resources_v1/resources/cases`；禁止删除旧release或替换链接。

## 3. 备份与制品

- 整库备份：`/data/backups/xueyeceping/20261109_203740_20261109_high_school_textbooks_v1/database.sql.gz`，53,167,168B，SHA-256`42ea3ea4b702d9850efa86ffc75afa875a74a1e54ca293ad0d3b09fc24263e99`；gzip、Dump completed、SHA256SUMS已校验。
- 备份同时含`service.before`、`nginx.before`、`env.before`、`jar.before`、`index.before.html`、`release.before.json`。
- 首发前端制品：`frontend.tar.gz`43,529,068B，SHA-256`ba96d17cf80e1da066ceb07e56e7089e68841d26cb72e98f417e80cce6de66c6`；820文件。
- 平台总览竞态热修制品：`output/release-20261109-high-school-overview-guard/frontend.tar.gz`，43,339,772B，SHA-256`acf9c8b812f4b3fbf777cdf97465397877d286b07908e0a7f1ee500f303ac87d`，820文件；Nginx切换前备份`/data/backups/xueyeceping/20261109_210407_20261109_high_school_overview_guard_v1/`。
- 教材资源制品：`textbook-resources.tar.gz`119,362,836B、544文件，SHA-256`2d9bc3bf0ac3ff790cba50d1a4826b46d273bebddf7fc1d21c2b3ae678d8d6e4`。
- 本地制品目录：`output/release-20261109-high-school/`；构建来源`output/build-high-school-20261109`，基于线上`20261109_research_python_grading_v1`隔离构建，仅包含本功能8个源码文件差异。

## 4. 验证

- 正式API：19项通过，覆盖教师书架16册/高中3册、三册目录与页图、整册PDF44,409,670B、两份课标预览/下载、学生/匿名负例、既有教研课例summary。
- 正式浏览器：真实教师1440/390/320，书架高中/课程标准分组、课标弹窗预览、高中阅读器“章/节”、0横向溢出、0个5xx、0页面脚本异常；热修后连续两次完整复跑通过。
- 前端异常已定位到平台总览页的接口迟到竞态：页面卸载后仍使用空图表容器初始化ECharts。已在`platform/overview/index.vue`增加卸载保护和容器空值判断，并以独立前端release平滑切换；未删除错误监听，不是通过放宽验收掩盖问题。
- 开发阶段验证：教材定向后端40项、前端既有43项、Vite生产构建和发布门禁通过。未把开发阶段结果替代正式环境实测。

证据：

- `output/playwright/high-school-textbook-online-20261109/api-verification.json`
- `browser-report.json`及5张截图
- `product-update-verification.json`
- `output/release-20261109-high-school/remote-state.json`

## 5. 回退

1. 只撤销总览竞态热修：确认Nginx仍指向`20261109_high_school_overview_guard_v1/frontend`后，恢复热修备份中的`nginx.before`，`nginx -t && systemctl reload nginx`；不重启后端、不动数据库。
2. 回退整轮高中功能：确认后端仍为`20261109_high_school_textbooks_v1`后，恢复原整库备份中的`service.before`到`/etc/systemd/system/xueyeceping-130.service`，`nginx.before`到`/etc/nginx/sites-available/xueyeceping-130`。
3. `systemctl daemon-reload`、`systemctl restart xueyeceping-130`、`nginx -t && systemctl reload nginx`，退回`20261109_research_python_grading_v1`。
4. 将产品更新165转DRAFT。
5. 若只想暂时下线本次新增内容，可执行业务停用SQL：

```sql
UPDATE biz_textbook_book SET status='1' WHERE book_code LIKE 'xxkj-hs-%';
UPDATE biz_textbook_standard SET status='1';
```

新增表、页图、课标和封面均为增量，优先保留；不整库回灌，不递归删除共享教材目录，尤其不要删除`20261107_teaching_resources_v1`。
