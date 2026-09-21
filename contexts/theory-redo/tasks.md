# 理论重做验收与交接

2026-09-20，仅本机。

## 追加：打字重做与状态修复

- [x] 两个按钮位于成绩汇总表右上角，按本课题型显示，共用选择、分别确认有效人数。390px手机换行、标题与按钮边界检查通过。
- [x] 教师打字清分、独立留档与轮次、旧请求拒绝、新轮低分替换、同轮最高分，答案和GRADED同事务。
- [x] 状态报错有本机日志与MySQL复现：并发no-op upsert后快照读为空，当前读能找到记录。写后改FOR UPDATE；16个真实并发状态请求均成功。
- [x] 31项Java、2项前端测试、后端clean package、Vue3构建通过；理论15项、打字11项真实接口/页面检查通过，含权限、批量回滚、隔离、低分、轮次、并发与师生交互，页面异常0。
- [x] 查库确认打字历史及操作题17分保持；本轮临时学生10608～10611及答案/状态/两类历史精确清理，逐表复核0。未改真实学生成绩。

证据：`output/theory-redo/typing-verification.json`、`state-race.json`、`typing-tests.log`、`typing-package.log`、`typing-build.log`、`final-header-desktop.png`、`final-header-mobile.png`和typing学生截图。

打字迁移前本机整库备份：`D:/dmwprogram/newdazipingtai/backups/theory_redo_local_20260920_102018/database.sql`，101912404字节，SHA-256 `d567e14846475647e148e598fa4987cdffe1c87237de3572a25a56eb4ec3309e`。typing_redo_v1.sql执行两次；新后端PID45232监听8080、前端80。新环境须理论与打字两份SQL和前后端配套，无新配置。未连接、上传或部署服务器。

限制：状态并发证据针对本机已复现错误，不能宣称线上类似提示全已解决。学生既有手机打字窄栏未重构，未做整班压测或课程推进同瞬间专项。以下为此前理论阶段记录。

- [x] 教师成绩表选择、确认、批量重做、待重做标签；学生清空旧理论选项并重新交卷。
- [x] 后端课程/学校/班级/角色校验、版本拒旧、私下归档、答案及课堂状态原子提交。
- [x] 25项相关Java测试、2项前端逻辑测试、后端clean package及Vue3生产构建通过。
- [x] 本机MySQL整库备份后执行SQL两次；历史表最初0行，幂等成立。
- [x] 真实接口验证批量重做、学生无权、错误班级、批量中途失败整批回滚、缺失/旧版本交卷、旧教师版本、低分替换、重复交卷、并发重做只成功一次、并发重做与旧交卷。
- [x] 真实教师浏览器选择学生并确认重做；真实临时学生页面显示待重做、无旧选择、再次提交成功；桌面/390px手机截图，页面异常0。
- [x] 数据库验证操作题答案/17分不变，历史快照保留原理论答案和题目；打字不删除路径由Java测试覆盖。
- [x] 两轮临时验收账号及其答案/状态/历史已精确删除，最终逐表复核均0；真实学生成绩未改动。

证据：`output/theory-redo/verification.json`、`verify.log`、教师/学生PNG、`migration.json`，构建日志`output/theory-redo-package.log`、`output/theory-redo-frontend.log`。验收脚本第一次路由拼出双斜杠导致404，修正后真实页面通过，不是应用路由故障。

备份：`D:/dmwprogram/newdazipingtai/backups/theory_redo_local_20260920_094915/database.sql`，101896126字节，SHA-256 `2f37123c08032a90bf478abf91a5d3dfc08be833e5dff12d073e1e76f37d7894`。目标仅localhost的xueyeceping_server_20260729。后端已用本轮JAR重启至8080，前端本地开发服务80。

剩余范围：未做整班负载压测、课程推进与重做同一瞬间的专项并发测试、真实学校课堂试用。既有手机导航和分数卡布局不在本次调整范围。未访问、上传或部署正式服务器，未生成产品发布版本。
