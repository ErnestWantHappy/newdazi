# 课例评价与教材体验：1.30.69发布证据

## 2026-11-03 · 1.30.69 · 课例评价、材料合包与响应式修复（已正式上线）

130后端`20261103_case_feedback_v2`、前端`20261103_case_feedback_menu_v4`，JAR`755207824e4cb788fb1004909858921c48748a7b597f4d9a3613ab2e8b591603`、首页`e29d4d2c54514bd8dba064fa9e3f8715cede24d1a5b15115a4f03c2bcc8a4e4a`。52课例196资源（44份配套材料包），307个资源文件逐一哈希通过。更新`147	1.30.69	PUBLISHED	2026-11-03 21:55:22`，真实教师更新列表可见。

完成推荐/不推荐、改票/撤销、匿名文字理由与低频自愿邀请；教师兼教研员按teacher角色放行。修复多人踩票返回0、撤销后理由复活、理由不能清空、失败预览仍邀请、联网条件漏报、保存失败丢草稿、手机偏移。真实并发发现的共享锁升级死锁和旧快照返回旧票一并修复：教师行排他锁、评价/邀请事务使用READ_COMMITTED、本人状态用当前读。

菜单数据库与前端角色排序同时调整；排除同名隐藏阅读器/成绩详情路由，实际侧边栏验证教材紧随题库、成绩紧随导学单、教研活动紧靠学生管理之前。手机390/320宽预览均x/y=0、视口全高。

验证：Java定向20项；真实MySQL Mapper回归；真实本机API 12次并发成功且单行、清空/撤销/501字拒绝通过，测试状态已恢复；联网识别3项；前端生产构建含7+9项既有测试和发布门禁，额外菜单6项；本机异常UI与正式1500×900/1366×600/390×844/320×568通过。正式师生基础页、匿名401/学生403、设计/课件首末页、最小包下载、52/196及44包通过；正式未生成测试评价。

后端PID2885391、NRestarts=0，三服务active，env未变，非预期ERROR为0。真实验收教师roles为teacher+researcher，双角色登录、教材、菜单及写入口角色准入已验；没有代写生产有效评价。正式确有3个双角色账号，其余账号未逐人操作；学校机房工具/共享Key有效性未逐一验证。

SQL：已执行`sql/textbook_case_feedback_v1.sql`、`sql/menu_order_textbook_v1.sql`、本轮审核清单生成的事务导入SQL，以及验收后的`sql/textbook_feedback_release_20261103.sql`。新增评价/邀请表，不改成绩/权限分配；材料仍离线维护。后端基于实际1.30.68定向替换4个课例class，未带入概览/登录慢查询等无关修改。

备份：`/data/backups/xueyeceping/20261103_case_feedback_v2`，整库gzip SHA256 `0b4091f264fbe7d5fced78776bcf0a7aa1c9d5609cbf719a0662af434b5a9f25`，gzip与完成标记已核验；同时保存旧unit/site/env/JAR/config、两课例表元数据、菜单顺序、资源软链目标和SHA256SUMS。

回退：确认当前仍为本次后端后执行`output/release-20261103-feedback/release.py rollback`。恢复旧程序至1.30.68、旧课例元数据/菜单及资源软链，更新1.30.69转DRAFT；保留新评价表与评价，不回灌整库，不覆盖新学生成绩。旧release与备份均保留。

详细证据：`output/release-20261103-feedback/`的manifest、backup、stage、migration、switch、backend-refresh、frontend-menu-switch、production-verification、published及正式截图；本机日志`output/textbook-cases/fix-*.log`。最初线上验收脚本误用了本机bookId及未展开路由，后已按正式summary动态取课并验证；菜单隐藏路由定位问题已真实修正，未将初次失败当成功。
