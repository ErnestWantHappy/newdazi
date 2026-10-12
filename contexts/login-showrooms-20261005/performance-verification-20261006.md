# 登录学校动态性能修复正式验收

2026-10-06，130正式 **1.30.30**，后端`20261006_login_preview_fast_v1`，前端保持`20261005_login_showrooms_v1`。123的1.30.29不是本轮发布目标。

## 原因和实现

旧SQL把近90天约22万条活动直接关联每个课程班级，执行计划出现约124万次学生匹配，EXPLAIN ANALYZE总耗时约5.9秒。先按课程/学生取MAX使用时间，再从活动匹配学生、账号和唯一班级事实，避免重复匹配；90天窗口、真实非空答题/签到/表现、有效学校/学生、最新6班、参与人数及当前题目批改口径保持。

两个缓存使用独立锁和volatile不可变快照，命中时直接读取，不等后台刷新。复用现有Spring调度器，启动3秒后预热、每20秒检查；剩余有效期不超过30秒时提前刷新。最大有效期仍60秒，不延长失败快照TTL；失败保留仍有效数据，过期请求仍回源并正常报错。同厅冷请求合并成一次查询。

## 验证

- Java11项全部通过，覆盖匿名字段白名单、容量/年级/空结果、失败重试、两个展厅不互阻、刷新中读取不阻塞、刷新失败不返回过期快照、8并发冷请求一次查询及预热避免重复。根目录与隔离目录均测试通过，独立后端clean package通过。
- 正式一致性快照及固定NOW下，旧SQL与最终SQL返回6行逐字段/排序完全一致。3次无应用缓存查询1328/1391/1359毫秒；对比摘要及计划见`output/login-perf-20261006/final-sql.json`及同目录plan文件。
- 正式17次请求跨80.4秒（覆盖原60秒TTL）：接口中位数12ms、最大49ms；12并发请求最大29ms。只代表该次内网测量，不作为大规模容量结论。
- 登录页从开始导航到学校表格出现481ms（含前端加载），截图`output/playwright/login-perf-20261006/login.png`。页面异常和HTTP500为0。
- 概览始终6行、字段完整白名单；原活动列表/主题详情和监管timeline/学生明细匿名HTTP200/业务401。登录、正文/附件、成绩详情权限及公开DTO未扩大。
- 制品基于已验收1.30.28隔离源码，仅叠加Service、Mapper XML和测试3文件；实际Jar只改变LoginPreviewService及其Java8内部/合成类和Mapper XML，其他模块内容保持。未重打前端，未混入本机5图改版或其他未收口修改。

## 发布与回退

独立systemd任务`release-20261006_login_preview_fast_v1` COMPLETED，后台/Nginx/aiot-lamp active；JAR SHA-256 `1e1055c6a9aabb9b9d4c1272e098938ae24aaf377035daf96ad4cebef2ff1106`。已重启后台，Nginx配置、实际首页和shared env哈希保持；保留原外置config/migration-isolation.env及助手0.2.0下载路径。

完整备份`/data/backups/xueyeceping/20261006_login_preview_fast_v1/database.sql`，319950649 bytes，SHA-256 `9f9f1c0060db6cb43241872051bea701a595898b307c061d40aa3b54d66c83bd`；已检查dump完成标记。无表结构迁移、新依赖或业务数据修改，只执行`sql/login_preview_fast_release_20261006.sql`，更新108/1.30.30 PUBLISHED且真实教师列表可见。

回退先确认后台仍指向本release、无活动AI任务；将本次备份service.before恢复至原systemd unit，daemon-reload并restart xueyeceping-130，核对旧Jar、captcha/prod-api和三服务active，回到1.30.28；前端始终1.30.28，无需更改Nginx或env。仅将对应更新撤DRAFT，不回灌数据库覆盖新作品/成绩，不删除旧release和备份。

## 限制

数据库不可用、刷新调度长期受阻或启动初始缓存尚未填充时，请求仍可能等待约1.3秒回源，不伪造数据。大规模匿名并发容量、正式教研员有效凭据登录及123环境同项验收不在本轮验证范围。AOCI85条/2块正文已读，校验时索引被并发修改，未获得完整认知校验；收口按工具实际状态记录，不据此声明治理对齐。
