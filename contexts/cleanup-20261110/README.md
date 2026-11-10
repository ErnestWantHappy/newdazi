# 2026-11-10 本地与130冗余清理记录

## 结论

- 本地清理前 `output/` 约 98.55GB；删除 38 个未跟踪发布/课例/worktree 大目录和临时目录后，项目总占用降至约 33.87GB，其中保留本机教材页图 19.77GB。D 盘当前可用约 184.34GB。
- 130 `/data` 可用空间由 322GB 增至 348GB；`/data/apps/xueyeceping/releases` 由 52GB 降至 26GB。共删除 68 个历史 release，约 43.49GB。
- 130 服务未重启、数据库未修改、备份目录未删除。清理后服务 active，首页和验证码均 200，当前 JAR 与 Nginx root 仍为 1.30.92，课例软链接仍指向 `20261107_teaching_resources_v1/resources/cases`。

## Git 收口

- `7e5c441`：保存 1.30.87～1.30.92 已上线源码、SQL 和文档。
- `76bf7f6`：排除 Python 运行缓存。
- `4abcef8`：移除旧 Vue2 前端、误入库的 Windows 缓存路径、AI 记忆、AGENTS 备份和 `dist.zip`。
- `04cfa91`：移除历史临时 PDF 产物。
- 均仅在本地提交，未执行 `git push`。

## 130 保留

- `20261110_python_manual_confirm_v1`：当前 1.30.92。
- `20261110_python_grading_snapshot_v1`：上一版与数据修复回退基线。
- `20261109_python_grading_fix_v1`、`20261109_grading_mobile_v1`：1.30.88/1.30.89 回滚组合。
- `20261109_high_school_textbooks_v1`、`20261109_high_school_overview_guard_v1`：高中教材与总览热修回滚组合。
- `20261008_seat_install_v1`：当前 systemd 外置配置依赖。
- `20261107_teaching_resources_v1`：共享课例软链接目标。
- 另有 `20261015_practical_pptx_v1`、`20261028_fullstack_v1` 因 root 权限文件未被普通删除移除，本轮保守保留。

## 验证

- 后端：`mvn -pl ruoyi-admin -am clean package -DskipTests` 成功。
- 前端：`npm run build:prod` 成功，包含登录 9 项、机位助手 9 项、题库工作台 25 项和发布门禁。
- 本机后端：8080 启动成功；`/captchaImage` 200；`/business/textbook/books` 200 且未登录返回 401 业务包，说明服务、路由和权限边界正常。
- 130 数据库清理前后均为：课例 327、资源 1050、教材 16 册、页 1840、课标 2、孤儿资源 0。

## 后续

- 当前工作区仍有一批 Python 交互相关未提交改动，是清理开始前已存在的独立开发内容，未混入本次清理提交。
- 130 剩余 10 个 release 均有当前运行、近期回滚、外置配置或权限保护原因；下一轮清理需单独确认 root 权限目录后再处理。
