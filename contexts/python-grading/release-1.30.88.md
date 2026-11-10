# Python 操作题批改稳定化发布验收（1.30.88）

状态：**130 正式已上线，产品更新 166/PUBLISHED**

## 发布内容

- 批改页代码、分数、判题时间、用时、内存和通过用例统一取自最高分正式提交。
- 无答题行但有正式 Python 提交时，可预览、人工评分和批量采用；批量采用在事务内补建答题行。
- 同分重交采用最新代码，避免新指标配旧代码。
- 批量采用增加行锁、最高分复核、退回检查和个案跳过；页面列表快照比对在 1.30.90 补齐。
- 学生档案缺失不再让整班批量采用回滚。
- 题目满分为 NULL/负数时拒绝批量采用。
- 新增最高分查询索引。
- 课例来源为空时不再误显示“优秀课例”，不开放旧课例评价。
- 教研导入和高中教材脚本的路径/软链接/重入修复已在本轮源码收口；它们是离线运维脚本，不进入运行时 JAR/前端制品。1.30.89 再补老师手机批改布局。

## 正式版本

| 项目 | 结果 |
| --- | --- |
| 后端 release | `/data/apps/xueyeceping/releases/20261109_python_grading_fix_v1` |
| 前端 release | 同 release 下 `/frontend` |
| JAR | `0cfb023c559ec7e4c6b5490b90405240ccd6aba8dee62e437b4edeccbbcd5c13`，108,299,981B |
| 首页 | `27d47323c3216af7a45235a64000c1c55a8823d0d067c793293e00dd3bf623d7` |
| 服务 | MainPID `3093098`、NRestarts 0、首页/验证码 200、启动错误 0 |
| 产品更新 | `166 / 1.30.88 / PUBLISHED`，真实教师可见 |
| 最高分索引 | `idx_programming_submission_best` 已建立 |

制品目录：`output/release-20261109-python-grading-fix/`。

## 备份

- 目录：`/data/backups/xueyeceping/20261109_232033_20261109_python_grading_fix_v1`
- 整库：`database.sql.gz`，53,200,539B
- SHA-256：`f1f6d2609f4424df4468cf1849babd13774372db518b953d05d910d0ff5d51e9`
- 同时保留 `service.before`、`nginx.before`、`env.before`、`jar.before`、`index.before.html`、`release.before.json`。

## 测试与线上验收

- 后端业务全量：968 项通过，0 失败，3 跳过。
- Python 定向测试：无答题行补建、并发快照跳过、满分为空拒绝 3 项通过。
- 前端生产构建与发布检查：通过，820 文件。
- 本机新 JAR 启动、验证码 200、教师登录、操作题列表和 43 人批改名单接口通过。
- 130 教师接口：Python 批改范围 45 人，40 人有正式系统判题；最高分代码、分数和 submissionId 40/40 与数据库一致。
- 130 MySQL 8.0.46 `EXPLAIN`：命中 `idx_programming_submission_best`，`using_filesort=false`。
- 证据：`output/release-20261109-python-grading-fix/acceptance-result.json`、`remote-state.json`、`explain-best-index.txt`。

## 未验证边界

- 没有向生产真实学生写分，因此无答题行补建没有在生产成绩链上触发；该路径由单元测试覆盖。
- 没有做 130 两名教师并发改分、学生并发重交的真实压测。
- 没有在生产用真实 WPS/Excel 变体重跑选择题图文导入。

## 回退

1. 确认当前前端仍为 `20261109_python_grading_fix_v1/frontend`。
2. 使用发布脚本或恢复备份中的 `service.before`、`nginx.before`，退回 1.30.87 JAR/前端。
3. `systemctl daemon-reload`、重启 `xueyeceping-130`、`nginx -t && systemctl reload nginx`。
4. 将产品更新 166 转 DRAFT。
5. 新增索引可保留；已补建的正式答题行不回删、不整库回灌。

