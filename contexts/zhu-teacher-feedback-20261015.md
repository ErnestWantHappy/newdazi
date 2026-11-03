# 丹城二小教师反馈排查（2026-10-15）

> **正式已完成：1.30.51**。双端`20261015_practical_pptx_v1`，期限1233校正、四班40份PPTX重算、真实反馈教师浏览器与鉴权下载验收通过，更新129/PUBLISHED且教师可见。下述排查事实保留原时点，正式结果以本节及末尾发布验收为准。

## 已确认事实

- 130 正式库、课程715、学校108、2021级4班，期限1233于2026-09-24 14:24:09触发，2026-10-15 14:24:09截止；初始化来源REALTIME，无延期记录。2班期限1664独立计算，不能一起覆盖。
- 当天只读查询显示：9月24日该课程有23条打字作答；10月15日才出现非空操作作品。截图时4班40份作品均待批。现有`PracticalGradingDeadlineMapper.selectClassMetrics`按全题型首次作答达到半班触发，`PracticalGradingDeadlineService.assertCanGrade`按课程班级期限统一拒绝评分；与现有teaching-supervision REQ-04/05/07一致，但不能满足分课时先打字后交操作作品的教学安排。
- 截图学生当前答案621103、版本49712的PPTX/PDF/页图已取副本。原件39349B，PDF26108B，页图112200B；PPTX为一张16:9幻灯片，14个形状、11条连接符、无组合形状。
- PDF与正式page-jpeg-v4页图均出现输入/输出框文字异常换行、连接线走向与教师PowerPoint截图不一致。问题在Office转PDF阶段；展开保存路径及短单行框保护后Java副本经同一服务器引擎转换，反馈图中的回路线和单行文字恢复。服务器等线字体匹配为DejaVu Sans，但未单独证明字体替代的全部影响。
- 19点前基线核对时三服务active，双端为20261014_seat_local_v1；这是发布前事实。用户随后明确授权发布，已切换1.30.51，详细时间见末尾。

## 用户确认与已完成实现

用户已明确选择“操作作品提交人数达到半班后，整班开始计时21天”。Mapper保留全题型参与数，新增操作提交人数，阈值与服务触发使用后者；DTO和批改页同步显示。PPTX新增临时副本兼容，不引入依赖，不更换转换引擎，不改变页图v4。

验证：72项Java定向测试无失败；完整`mvn -pl ruoyi-admin -am clean package -DskipTests`通过；实际Mapper源码在MySQL执行3组纯CTE构造数据及真实只读查询，确认去重、空作品、打字、无时间、已移除题和跨班/无效学生边界。四班正确第23位操作提交为2026-10-15 14:18:25，截止2026-11-05同刻；二班13:37:11，不能混用。1024/1366/1920三宽度实际Vue组件配模拟API通过，无页面异常或真实写分；当前源码和正式源码隔离候选的生产构建及登录资源检查通过。用户账号真实登录及正式浏览器已在发布后验收通过。

Java实际实现生成核心样本和4份其他学生的转换副本，在130独立LibreOffice24.2.7.2进程转换成功，页数保持、文字无丢失、原件哈希保持。核心图已目视对照教师截图；其他样本没有逐份PowerPoint参考图，不能声称逐像素一致。其他形状的文字换行、曲线、旧PPT、字体替代仍有兼容风险。

## 发布制品与数据处理

- 后端以正式1.30.50 JAR为基线，只叠加8个相关业务条目（含新增类），其他外层条目和既有页图v4字节保持；完整本地JAR不直接发布，以免混入无关工作区改动。
- 前端以1.30.50发布源码快照仅覆盖本次grading.vue的一行统计，保留上次排除的未发布PracticalTextPreview样式。候选与制品差异证据在`output/zhu-feedback-20261015/candidate-evidence.json`，最终哈希以manifest为准。
- `sql/practical_deadline_zhu_20261015.sql`已执行：只改期限1233，旧值及无审计门禁后锁行校正，1条更新+1条REOPEN审计，复跑0变化。操作者system/0，原因记录平台负责人确认规则，不冒充教师/管理员。其他课程及二班期限保持原样。
- `sql/practical_pptx_previews_zhu_20261015.sql`仅重置四班40个明确附件ID，校验当前版本/学校/班级/状态/旧时间后由既有教师重试API接受40份；全部success/success/page-jpeg-v4，40份PDF/页图文件存在、0个空页数组。复跑重置0变化。无表结构迁移。
- `sql/practical_pptx_release_20261015.sql`登记更新129/1.30.51/PUBLISHED，真实反馈教师`/business/platform-update/list`已确认可见。助手0.2.3及共享env字节保持，无新配置。

## 正式发布验收

- 首次19:23:34切换，新后端探活、Nginx语法均通过，首页哈希门禁失败触发自动回退，19:24:21旧版恢复。该次未改期限或重算预览，也未登记产品更新。发布脚本改为最长20秒轮询确认Nginx平滑重载后的首页哈希。
- 第二次19:25:17开始，19:25:40完成（23秒），双端均`20261015_practical_pptx_v1`；PID2408758/NRestarts=0，xueyeceping-130/Nginx/aiot-lamp均active。`NRestarts=0`指自动重启计数，不表示本轮只进行了一次人工重启。
- 朱老师真实账号API显示canGrade由false→true，triggerTime为2026-10-15 14:18:25、currentDeadlineTime为2026-11-05 14:18:25，参与45/45、操作提交40/45、待批40。Chromium实际登录→教师首页→四班批改→截图学生作品，评分按钮可用、页图加载和目视对照通过；pageerror/HTTP5xx均0，未写入测试成绩。鉴权下载原件与排查副本SHA-256一致，PDF可读。
- 40份原件哈希逐一保持，答案、成绩、提交时间、版本状态/成绩快照、当前版本指针和lock_version与发布前快照完全一致。仅派生PDF/页图及其预览状态更新。未在本轮重跑正式学生/教研员浏览器、完整交卷链路或性能压测；不能把本教师验收扩大为全平台所有PPT兼容已解决。
- 排查时发现的两个7月遗留converting附件12365/12594并非在途任务，本次未修正；没有新学生提交或AI任务时才执行切换。

## 备份与制品哈希

备份目录：`/data/backups/xueyeceping/20261015_practical_pptx_v1/`；含完整数据库、unit/Nginx/env/config、相关环境文件、期限/审计/附件定向dump、原行TSV、原件与派生预览归档及保护快照。

| 内容 | 字节 / SHA-256 |
| --- | --- |
| database.sql | 418,997,463；`3f0a21eca73a3979399853ccc7968101a28fe83b4348dde8638957d8ca047bea` |
| affected-previews.tar.gz | 4,784,887；`2a94836a1c91973f9eb2479f35108e0644015592ac342179d70836fce3d23f90` |
| 正式JAR | `e2e91799ac0a8dfbef2c499667a4b203d690d8757c047cab6b7b303c166dc641` |
| 前端ZIP | `149b34a1374bbe9fe6f6fb719826d2d91b4fcd10fc419f2bc382710ca4fcadf1` |
| 首页index.html | `5d0f5b050d7af3980f6b74b1a85c9af2e08054d893b0946bddd6700817891578` |

## 回退与剩余边界

默认只回退代码：核对当前仍为本release及课堂空闲，恢复备份`service.before`到`/etc/systemd/system/xueyeceping-130.service`、`nginx.before`到`/etc/nginx/sites-available/xueyeceping-130`，`systemctl daemon-reload`及重启后台，`nginx -t`后reload，核对回到`20261014_seat_local_v1`与探活。将更新129撤为DRAFT。**保留已验证期限校正和正确预览，无需回滚SQL，不整库回灌。**

如果数据本身也需撤回，先重新备份现状并核对无后续人工延期/新版本，再逐行利用deadline.before.sql/TSV和本次精确审计还原；派生预览按归档和attachments.before.sql的字段映射恢复，不能直接执行INSERT dump覆盖现有附件主键，也不能回灌完整库覆盖期间新成绩。原件无需恢复。

共享env未改，外置config/隔离env依赖20261008旧release，助手路径仍依赖20261014_seat_local_v1，不得清理。其他历史期限、其他课程缓存、曲线/旧PPT及非输入输出形状换行按单独反馈继续验证，不全库重算。

证据：本机`output/zhu-feedback-20261015/`的查询、原件副本、PDF/页图、测试/构建日志、manifest.json、release-backup.json、release-status.json、deadline-correction.json、recompute-status.json、online-browser.json/截图和release-verification.json。服务端备份目录保留切换脚本、两次状态及verification.json。账号密码不写入本文或报告。
