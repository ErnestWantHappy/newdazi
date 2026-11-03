# 本机验收（2026-10-16历史记录）

2026-10-17已按用户重新授权合并发布130正式1.30.52，双端`20261017_seat_score_v1`、助手0.2.4；真实教师/教研员验证及更新130/PUBLISHED通过，学生与机房仍待验。见[正式验收与回退](../seat-score-release-20261017.md)。下文保留10月16日本机验收当时的事实，不代表当前发布状态。

状态：本机修复候选；130最新正式版本仍为1.30.51。本轮无远程连接、部署、正式更新记录或业务表写入。

修改范围：后端`ScoreQueryController.java`（汇总/导出）、`StudentHomeController.java`（最高记录响应和空答案择优）、`BizStudentAnswerMapper.xml`（双汇总SQL）；前端`business/score/index.vue`、`RankChart.vue`、`rankData.js`、`CourseComparisonChart.vue`及`student/index.vue`。另有定向测试、浏览器验收脚本及上下文/架构/专题文档。接管时已存在的其他修改全部保留，不纳入本轮发布制品。

## 已通过

- Java定向41项：ScoreQueryControllerTest、ScorePendingExportTest、ScoreQueryExportColumnsTest、StudentAnswerSubmissionServiceTest、StudentAnswerMapperContractTest、TheoryRedoServiceTest、StudentHomeControllerTest、TypingBestSubmissionTest；0失败/错误。新交卷测试覆盖NULL/空/低分输入及多题逐题返回。
- Node14项：成绩页已有分汇总、混合操作评分、请假/缺考/未提交、真实0分、45人排名数据、重做范围/闸门竞态及打字最高分状态。旧gateRace测试补齐之前缺失的分析状态模拟对象，不改生产闸门行为。
- 本机真实MySQL临时表6组：执行源码两条汇总SQL与原子upsert；验证待批时已有分、0分已批且其他作品仍待批、批完加分、已移除题目不计、低分/同分/NULL输入不覆盖最高记录、更高分整体更新、清分后新轮可低分。临时表仅当前连接可见，断开销毁，真实业务写入0。第一次临时表LIKE同名失败后改为显式临时表定义；未因此操作真实表。
- 真实Vue组件配模拟API的Chromium5组：教师45人完整名单/图表且默认100%，35作业+5表现=40，三种无成绩状态与真实0分区分；学生重新打字过程中40分保留，低分提交后仍40并显示对应速度24，教师新轮清分后旧最高分消失。页面异常0；教师截图宽度1440/1920/1024，已人工查看并修复状态标签拥挤。

证据：`output/score-20261016-{java,node,mysql,browser,frontend,package}.*`，截图/浏览器报告在`output/playwright/score-partial-20261016/`。可复跑浏览器脚本：`node scripts/verify_score_partial_20261016.mjs`（项目根）。MySQL验收脚本为本机output产物，未增加运行时依赖。

## 打包与剩余边界

- 最终Vue3生产构建和资源门禁通过，入口gzip 559,068B；最终样式/多课显示修改后已重跑成功。
- 后端最终`mvn -pl ruoyi-admin -am clean package -DskipTests`成功，全部模块SUCCESS；打包跳过重复测试，前述41项已单独运行通过。
- 本机业务前后端未启动；未用真实教师/教研员/学生账号跑API或登录冒烟。模拟接口浏览器通过不等同真实服务联调。
- 未核对130现网该班打字记录，因此只证明本机源码择优逻辑与显示修复，不能宣称正式历史数据已修复。不得批量重写学生分数。
- 无表结构迁移、业务SQL或配置变化；发布需隔离大量既有工作区改动，配套前后端并重启。回退应用不恢复历史成绩、不整库回灌。既有机位迁移缺失、DATA_LAB缺表及其他专题待办保持。
