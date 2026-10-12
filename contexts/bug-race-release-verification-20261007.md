# 并发与页面状态修复正式发布验收

日期：2026-10-07。130 正式主站版本 **1.30.36**，前后端 release `20261007_bug_race_v1`；上一版为 `20261007_bug_review_v1`（1.30.35）。隔离候选只包含 `PaperBatchImportService.java`、区域抽测两个页面、成绩页及学生 Python 题单页的运行改动，未混入工作区其他未收口文件。无结构迁移、共享配置或权限变更，仅执行 `sql/bug_race_release_20261007.sql` 登记产品更新。

发布前 Node 7 项请求交错测试、Java `PaperBatchImportServiceTest` 4 项、后端打包和 Vue3 生产构建通过。制品哈希见 `output/bug-race-release-20261007/manifest.json`。正式切换任务 `release-20261007_bug_race_v1` 状态 `COMPLETED`；后台、Nginx、aiot-lamp 均 active，验证码代理 HTTP 200。JAR SHA-256 `f975b202a80ecc296a0e96ec6cf9f461b37f805b61efb8b06e9716046132acc4`，首页 SHA-256 `60c25b57e2a82a206cd01eed018e4af157590024445b538debb5b28af1f0197a`，均与候选一致。

整库热备份位于 `/data/backups/xueyeceping/20261007_bug_race_v1/`，336221627 bytes，SHA-256 `004200d0125434d0c756f3b910a958d4a5df3517caba14937e4335fccc5fedb1`；旧 release 保留。真实教师登录、首页、更新列表，真实学生登录和当前课程，教研员登录、角色与 16 条路由接口均通过；桌面 1440 和手机 390 登录页无横向溢出、脚本错误或 HTTP 500。产品更新记录 114/1.30.36 为 `PUBLISHED`，真实教师更新列表可见。结构化结果见 `output/bug-race-release-20261007/` 中的 `status-result.json`、`live-verification.json`、`published-update.json`。

本次未在正式库写入学生答卷、触发真实整班 PDF 并发拆分，也未代操作真实教师评卷；请求交错回归与锁并发单测不能替代现场吞吐观察。回退须先确认当前仍指向本 release 和无进行中的 AI 任务，再用备份目录中的旧 service/Nginx 配置切回 1.30.35，`daemon-reload`、重启后台及 reload Nginx，并撤回更新 114。无结构迁移，不能整库回灌覆盖上线后新增教学数据。AOCI 未同步，相关索引需后续复核。
