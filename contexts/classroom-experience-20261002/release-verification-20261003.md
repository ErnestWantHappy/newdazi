# 1.30.24 正式发布与真实验证（2026-10-03）

状态：130 正式站发布成功，平台更新 `update_id=103`、`1.30.24` 为 `PUBLISHED`，现有真实教师通过 `/business/platform-update/list` 确认可见。123仅用作SSH跳板。来源为本轮课堂修复及其所依赖的当前候选业务实现；没有推送Git。

## 制品、备份与配置

- 当前 release：`/data/apps/xueyeceping/releases/20261003_classroom_experience_repair_v1`；后端及前端均切入该目录。
- 上一 release：`/data/apps/xueyeceping/releases/20260921_full_datalab_classroom_v1`，1.30.23。
- 整库备份：`/data/backups/xueyeceping/20261003_classroom_experience_repair_v1/database.sql`，313,920,460 bytes，SHA-256 `e9c3e1b809cf20d8126c59fc261dd2decd820af91e20f184f7b3038a0ad3d247`；已核对dump完成标记。同目录保留原JAR、外置config、systemd unit、Nginx和env。
- JAR SHA-256：`d7187a767f810156ebcac0d4dc1f2de364f5a58e04985a3e552003859dc7101d`。
- 首页 SHA-256：`7970429b4cfdf1ff61718a6a6703a26cb870b2b949d1b98f22bbf236423169d1`。
- 客户端 `0.1.1-pilot` 已放到 release 的 `clients/SeatHelper.exe`，SHA-256 `486fa7aa30de09eba564bae63e5341d29de3274199a7276994f64d87c0204b65`。正式助手仍关闭，后续启用须配置该实际客户端路径和可信HTTPS入口。
- 原外置config、`migration-isolation.env`与共享env保持，env SHA-256 `3ee520f8e3b151b89516a313ccbc93c9c5c044b92aaa77f404c05be6fbb15213`。没有使用本机库或本机上传目录替换正式配置；保留Nginx既有Pyodide mjs MIME及旧static资源。
- 发布使用已有130 Linux systemd独立任务，状态`COMPLETED`，失败有自动恢复旧unit/Nginx/env及探活保护。`scripts/deploy.py`当前只适用于123 Windows，未将它直接用于130。

## 已执行迁移及实际问题处理

整库备份后执行四份课堂体验SQL：`classroom_experience_flowchart_history_v1.sql`、`classroom_experience_attachment_kinds_v1.sql`、`classroom_experience_feedback_v1.sql`、`classroom_experience_seat_helper_v1.sql`。后检：反馈/助手5张表、流程图2个历史列、管理员菜单及授权各1条。

首次页面验收发现教师 `/business/teacher/grading/ai/config` 返回500业务码：当前应用依赖上一轮多厂商配置，而发布准备漏检正式库缺少 `biz_teacher_ai_provider_key`。现有幂等 `operation_ai_multi_provider_keys_v1.sql` 在同一完整备份保护下补执行，5份原配置密文迁入，原 `biz_teacher_ai_config` 内容与主密钥均保持。补迁后现有教师批改页接口错误、页面异常均为0，最终浏览器全流程重跑通过。补迁证据 `output/classroom-release-20261003/migration-supplement.json`，该依赖后续发布前必须一起核对。

验收成功后执行 `sql/classroom_experience_release_20261003.sql` 登记公开更新，未改真实学生作品或成绩。实际教师可见证据 `teacher-version.json`。

## 验证证据与范围

1. 本轮发布前全量 Java：676项、0失败/错误、3跳过（含默认关闭的隔离MySQL用例）；此前修复定向39项含真实隔离MySQL已通过。前端定向26项通过。沿用本轮已成功的后端clean package、Vue3构建和客户端7项异常回归，制品内容与哈希已核对。
2. 正式 API 13组通过。新建专用验收学校、教师、两位学生与教研员，在正式应用/正式数据库真实调用：登录与当前课；101字符文件名拒绝；ZIP上传/大小/授权下载及跨学生拒绝；未批可改/不能退回；删除后首页、批改、大屏、反馈、成绩和数据库一致；真实Excel未提交；连续三版；0分锁定及删除拒绝；退回与失败重交保分；成功重交清分；HTML原文/本人反馈/助手关闭；旧课拒绝；流程图正式版与草稿独立、0分只读及退回重交。
3. 正式浏览器9组通过，页面错误0、HTTP500为0。真实登录、真实正式页面和接口：只读正式流程图不恢复他人草稿；超长名称提示；Element Plus上限1真实连续提交3版；师生两端压缩包大小；删除后反馈与网络失败刷新；教师成绩未提交；两班切换及前进返回；教研员页面；现有真实教师批改页只读检查。刷新测试仅故障注入首个网络请求失败，切班测试延迟真实服务器响应，没有伪造业务响应。
4. 服务与配置：`xueyeceping-130`、`nginx`、`aiot-lamp`均active，代理验证码HTTP200，JAR/首页/EXE哈希一致，原env及mjs规则保持。
5. 专用验收学校停用并逻辑删除，4账号停用并逻辑删除，验收班级指派/教师班级关联均清为0；专用作品历史保留供审计。没有改动现有教师密码、实际班级指派、真实学生作品或成绩。

本机证据目录 `output/classroom-release-20261003/`：`baseline.json`、`manifest.json`、`deploy-result.json`、`status-result.json`、`api-results.json`、`browser-results.json`、`final-audit.json`、`cleanup.json`、`registration.json`、`teacher-version.json`；截图在 `output/playwright/classroom-release-20261003/`。`.private`文件和登录态不得提交或公开。

## 回滚与剩余范围

回滚前核对当前仍为上述新release并确认活动AI任务，再恢复备份目录的 `service.before` 到 `/etc/systemd/system/xueyeceping-130.service`、`nginx.before` 到 `/etc/nginx/sites-available/xueyeceping-130`、`env.before` 到 `/data/apps/xueyeceping/shared/xueyeceping-130.env`，执行daemon-reload、重启后端、nginx -t和reload，探活旧首页/代理。平台更新103应撤回DRAFT。保留新增表列、厂商密文、作品和反馈，**不恢复整库覆盖发布期间新成绩**，无业务回滚SQL。

- 正式只读审计发现118条历史空文件答案仍残留课堂状态（SUBMITTED115、GRADED3），已记录，未批量改动。新删除流程及空答案统计/反馈/成绩已真实验证；历史状态应另做逐条证据核对与可回滚治理，不能把这118条宣称已经修好。
- 正式助手维持默认关闭，启用学校/活动设备均0。客户端故障降级、切学校注册码和跨页关联续期已有本机定向证据；本轮没有在正式HTTP站启用、注册真实机位。Windows10/极域/还原卡、自启与DPAPI保留仍待现场。
- 教师AI配置读取已真实验证，没有发起付费模型调用或把整班AI分析发布到真实学生。个人快照显式发布的完整写入链路沿用此前隔离验证，本轮主要验证本人反馈读取与删除口径。
