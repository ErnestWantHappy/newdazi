# 任务清单 —— 站点运维与信息展示

> 状态：需求已确认，待开工。与 student-experiment-tools 专题并行开发。

## 阶段 1：后端 + SQL
- [ ] ExtensionHealthController：解析探针新增字段（os / java{version,startTime,home,arguments}），保持降级语义
- [ ] deploy/host129/hwprobe.sh：129 侧探针脚本（供凭据恢复后部署）
- [ ] sql/platform_update_history_v2.sql：历史补录（幂等，覆盖 2025 与 2026-03~04 空档及近期功能）

## 阶段 2：前端
- [ ] monitor/extension/index.vue：129 硬件卡补齐 JVM/OS/暂缺提示
- [ ] help/recommendedEnv.js + help/index.vue 推荐环境区块
- [ ] Copyright/index.vue 去地址；登录/注册页复查去地址

## 阶段 3：Agent 词典
- [ ] contexts/RELEASE_LOG.md 创建
- [ ] AGENTS.md 增加发布登记硬性条款（收尾清单两项）
- [ ] tools/check-release-log.ps1（先做提示态）

## 阶段 4：验证
- [ ] mvn package 通过；npm run build:prod 通过
- [ ] 本机库执行补录 SQL 前检/后检
- [ ] 129 页 mock 验证；帮助中心三卡；底部无地址

## 阶段 5：收口
- [ ] PROJECT_CORE.md 更新（含 RELEASE_LOG 条款引用与本次发布登记）
- [ ] 正式发布流程（备份→SQL→制品→探活→RELEASE_LOG 登记→平台更新写入）

## 2026-08-24 收口发布

- [x] 掌控板卡片、Judge0 管理文案和重复联系入口完成精简。
- [x] 修复 `tools/check-release-log.ps1` 日期匹配被字面引号包住的问题，当前发布登记校验通过。
- [x] 正式库隐藏若依官网菜单、清理角色授权并写入 1.26.1 平台更新；后检、备份、release 切换和 3009/3010 探活完成。
