# Python 批量采用页面快照保护发布验收（1.30.90）

状态：**130 正式已上线，产品更新 168/PUBLISHED**

## 问题与修复

1.30.88 已为批量采用增加答题行锁、最高分提交复核和个案容错，但老师页面上的学生列表仍是请求开始时的内存快照。如果页面打开后学生在另一窗口重交，或另一名教师已经人工改分，旧页面再次点击批量采用仍可能覆盖新事实。

1.30.90 在批量请求中提交当前页面学生快照：

- 快照包含 `answerId`、分数、答案内容、作品版本、系统提交 ID 和系统分。
- 后端逐人比对当前列表，任何字段变化都跳过该学生并计入 `skippedCount`。
- 原有答题行锁、最高分提交复核、退回检查和事务边界继续保留。
- 不改课程、题库、既有成绩、权限或表结构。

## 正式版本

| 项目 | 结果 |
| --- | --- |
| 后端/前端 release | `/data/apps/xueyeceping/releases/20261110_python_grading_snapshot_v1` |
| JAR | `ec36df3f9118e5b711c6cb44e445b8d64bae303ccd495cf9df7b60bfb07fd440`，108,301,513B |
| 首页 | `f66da562736e9c1faf6c920a9629f7c980ab2433856bf2f3eb073a7f52a084c0` |
| 制品 | `frontend.tar.gz` 43,377,956B，SHA-256 `c3c9c75b8167a9d3d2cbaa7b590ca7604219381620ff7b1cee643a169467417a`，820 文件 |
| 合并后前端 | 2,043 文件 |
| 服务 | MainPID `3097248`、NRestarts 0、首页/验证码 200、启动错误 0 |
| 产品更新 | `168 / 1.30.90 / PUBLISHED`，真实教师可见 |

## 备份

- 目录：`/data/backups/xueyeceping/20261110_000343_20261110_python_grading_snapshot_v1`
- 整库：53,200,609B
- SHA-256：`2de6cf0240fbf731bc5626672cb59f3db09f20d5d8c0259f5f0d909f7b20d6cf`
- 含 `service.before`、`nginx.before`、`env.before`、`jar.before`、`index.before.html`、`release.before.json`。

## 验证

- 后端业务全量 969 项通过、0 失败、3 跳过。
- Python 批量采用 4 项通过，其中页面快照变更跳过用例通过。
- 前端生产构建通过：登录 9、机位 9、题库工作台 25 项门禁及发布检查通过。
- 130 真实教师 1440/390 只读验收：45 人名单、林煜琦代码、系统判题 40/40、判题时间和批量按钮正常；桌面三栏宽度 250/630/300，手机三块主区域各 350px；均 0 横向溢出。
- 0 页面脚本异常、0 请求失败、0 个 5xx、0 控制台错误；课例来源空值仍显示“来源未标注”，无“优秀课例”、无评价投票。
- 未点击批量采用，未写正式成绩。
- 证据：`output/playwright/python-grading-1.30.90-20261110/`、`output/release-20261110-python-grading-snapshot/remote-state.json`。

## 回退

1. 确认当前前后端仍指向 `20261110_python_grading_snapshot_v1`。
2. 恢复本备份 `service.before` 到 `/etc/systemd/system/xueyeceping-130.service`，`nginx.before` 到站点配置。
3. `systemctl daemon-reload`、重启 `xueyeceping-130`、`nginx -t && systemctl reload nginx`。
4. 退回 1.30.89 前端与 1.30.88 后端，将产品更新 168 转 DRAFT。
5. 不删除新增答题行、不整库回灌。

## 未验边界

- 未在生产真实成绩链触发并发重交或两名教师同时改分；页面快照跳过由单元测试与只读线上链路覆盖。
- 未做整班 Judge0 并发提交、队列积压和批改页刷新时延压测。
