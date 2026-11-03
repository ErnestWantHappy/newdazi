# 130正式1.30.53：学生文件作业自主重交

2026-10-20用户明确授权“上线吧”。17:45:43切换，17:46:06完成，23秒恢复；发布任务COMPLETED，未回滚。前后端release `/data/apps/xueyeceping/releases/20261020_student_resubmit_v1`，助手仍0.2.4，路径保持10月17日版本。未推送Git。

## 范围与隔离

- 当前课文件作业含图片组已有0分或非零分也可自行重新提交；暂存、取消、失败保留原作和原分，成功后旧版本/旧分归档，新版待批。教师退回、流程图等其他题型、旧课写入限制和操作题开放规则保持。
- 后端基于现场拉取且SHA匹配的1.30.52 JAR，仅替换`PracticalArtifactService.class`及`$1.class`。嵌套业务JAR其余条目和外层其余依赖逐字节一致，页图v4、期限/PPTX和机位成绩修复保持。
- 前端基于1.30.52源码快照（原dist首页与线上SHA匹配）只覆盖`src/views/student/index.vue`，源码差异全部属于本次重交任务。未混入课堂推进、题库导航、预览样式等未发布修改。
- 发布到新目录，保留旧static资源、旧release和备份。共享env及助手字节不变；外置config/隔离env仍依赖10月8日旧目录，不得清理。

## 制品、备份与切换

| 内容 | SHA-256 |
| --- | --- |
| 新JAR | `11cfa61a110ad6363b0ebb87bf814dffe5142d2cd55c29f2bd24bdda96041fb6` |
| 新首页 | `a5cfd1a9ca00c6a111cafebc98e8bb9f58e9e78fad08fa717ff9964162c4c59f` |
| frontend.zip | `4e9a5f1f083d9bbd15ed42c39b734f0ea4d7d8f97be982bcc044f7acaa06417d` |
| 整库备份 | `6ced8dbb50c14c8e188fb80425f606298ec08e1ba72639beb2f974d4932df75c` |

完整备份 `/data/backups/xueyeceping/20261020_student_resubmit_v1/database.sql`，450,706,074字节，dump完成标记/SHA均已验证。同目录保留service.before、nginx.before、env.before、config.before及环境文件快照。无表结构或业务数据迁移。

切换前两次检查AI批改/报告任务及近5分钟提交均0，核验unit/site/env及制品SHA、Nginx候选语法后，由独立systemd job切换；脚本具有失败自动回滚。最终PID2541602、NRestarts=0，xueyeceping-130/nginx/aiot-lamp三服务active。域名及IP首页SHA匹配，env/助手SHA保持。

## 验证与登记

- 本轮后端完整clean package与Java定向15项通过；隔离前端生产构建、登录7项和资源门禁通过。先前Node提交函数6项和真实上传区配模拟接口的桌面1440px/390px已覆盖非零分/零分、取消、失败重试、慢请求保分及成功清分，页面异常0。
- 正式真实教师/教研员登录、教师首页API与操作题名单接口通过。教师可访问样本为课程537/2022级6班/题4270（流程图，作为原批改链回归），44人名单正常；该题全课程答案内容/分数/版本摘要在切换前后相同。没有执行改分、退回或上传。
- 正式域名真实教师1440px/390px批改页与教研员默认页3组通过，pageerror/HTTP5xx均0。手机批改页原有窄列排版较拥挤，本次仅验证可进入和数据加载，不宣称已优化教师移动布局。
- 学生提交接口对匿名及教师均拒绝（401/403）；线上实际学生模块`static/js/index-SL9c_Mm6.js`与隔离制品SHA一致，包含新清分确认文本。
- 仅执行`sql/student_file_resubmit_release_20261020.sql`登记更新131/1.30.53为PUBLISHED，复跑保持唯一，真实教师`/business/platform-update/list`确认可见。

## 剩余边界

缺有效正式学生凭据，未实测真实学生重交、刷新后持久化和教师重新批改，不通过修改真实学生作业完成验收。数据库事务回滚为服务单测的异常传播/事务注解检查，未开展正式库故障注入。历史有分但缺当前版本的异常记录会拒绝重交，避免丢失旧分；未批量治理。学生实际使用待有效账号或课堂反馈补验。

## 回退

先核对当前确为本release并检查课堂/后台任务空闲，再将本次备份的service.before恢复到`/etc/systemd/system/xueyeceping-130.service`、nginx.before恢复到`/etc/nginx/sites-available/xueyeceping-130`，执行daemon-reload、重启后台、nginx -t/reload并探活，回到1.30.52双端`20261017_seat_score_v1`。本轮env未改变，保留原值；必要时从env.before恢复并核对SHA。将更新131按版本/标题精确撤为DRAFT。不恢复整库、不撤学生新增作品或成绩；没有结构回滚SQL，已归档旧分继续保留。

本机证据目录：`output/student-resubmit-release-20261020/`，包含baseline/backup/manifest/prepared/status/api-verification/browser-verification/student-module-verification/published.json、构建日志、隔离源码和页面截图。脚本发布前只读选样曾选到无操作题课程，已调整为教师可访问的有作品课程；这是验收选样调整，未因此改动产品。
