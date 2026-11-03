# 系统诊断与性能热点治理：任务与验收（2026-10-28）

> 状态：**本机已完成并验证；130 尚未发布**。发布前必须确认源码基线与发布通道，禁止把当前 193 项未提交改动直接打包上线。

## 任务状态

| 项 | 状态 | 证据/说明 |
| --- | --- | --- |
| Druid 排序与进程内口径 | 完成 | 按 `maxTimespan` 排序；前端显示“自后端启动起（重启清零）”；本机 `summary` 已验证 `sqlStatScope` |
| 异常与业务提示分离 | 完成 | 后端返回 `recentErrors`（非 info）与 `businessTips`（聚合）；前端增加折叠区 |
| 累计失败 vs 近 7 天失败 | 完成 | 新增 `dailyFailedRecent7d/countyFailedRecent7d`、`previewErrorTop5` |
| JVM 口径 | 完成 | 健康判断改用 `used/max`；本机 570/1820MB，不再误报 |
| 慢 SQL 持久化自证 | 完成 | `POST /monitor/diagnosis/perf-event/self-test` 本机返回 `intercepted=true, persisted=true, eventId=1426` |
| PageHelper 6 参数绕过修复 | 完成 | `SlowSqlInterceptor` 增加 6 参数 `query` 签名；本机 `select sleep(1.1)` 1121ms 已落库 |
| 资源鉴权查询重写 | 完成 | 4 条索引查询 + JSON 兜底；`ResourceAccessServiceTest` 10 项通过 |
| 资源索引 | 本机已执行 | `sql/diagnosis_perf_indexes_20261028.sql`；5 条索引全部生效 |
| 班级状态缓存 | 完成 | `getStatusForDashboard` 45 秒缓存；`PracticalGradingDeadlineServiceTest` 17 项通过 |
| 教师首页轮询 | 完成 | 10 秒 → 60 秒（已确认） |
| `selectClassStatusByLesson` CTE | 完成 | 本机 `/business/teacher/grading/classes/381` 返回 1 个班级；无 SQL 错误 |
| 学生删除异步文件清理 | 完成 | 编译通过；线程池拒绝时同步兜底；未做真实删除验收 |
| 教研图片异步展示图 | 完成 | 编译通过；读路径按需补生成保持 |
| 题库导入完整错误 | 完成 | 后端真实上传返回 `details=[{row:2, reason:"选择题必须提供选项 A 和 B"}]`；前端 Playwright 验证弹窗 |
| 321 条预览失败分类 | 部分 | 诊断页已能按原因 Top5 展示；未改重试策略 |
| 题库导出 | 未改 | 先测量 SQL/POI 占比，避免无依据改导出框架 |
| updatePwd 384 次核实 | 待办 | 需 130 `sys_oper_log` 分组核实 |
| 平台概览 5 分钟刷新 | 待确认 | 涉及登录展厅新鲜度，默认不动 |

## 本机验证

- 定向测试：`DiagnosisGovernanceTest` 5 项、`P0P1GovernanceTest` 8 项、`PracticalGradingDeadlineServiceTest` 17 项、`ResourceAccessServiceTest` 10 项，合计 **40 项全绿**。
- 后端：`mvn -pl ruoyi-admin -am -DskipTests clean package` 成功；本机 JAR 108,142,372B，SHA-256 `CC5F39A57D6F6DDDAFE860875CB17A79499056A517DB356C8E2C04657A56F596`（**含工作区其他未提交改动，仅供本机验证，不是 130 发布制品**）。
- 前端：`npm run build:prod` 通过；`dist/index.html` 1,228B，SHA-256 `3B1ECB129CA9B0D550D1C50F41E1E1FD645F9250845BCB7A177A700071934D12`。
- 浏览器：
  - 教师账号真实上传错误 Excel，出现“题库导入校验未通过”、共 1 处错误、第 2 行“选择题必须提供选项 A 和选项 B”；截图 `output/playwright/import-error-dialog-20261028.png`。
  - 教研员账号进入 `/monitor/diagnosis`，确认“进程内统计”“累计/近 7 天”“链路自检”“已提交/上限”均显示；点击自检出现“链路正常”提示；截图 `output/playwright/diagnosis-20261028.png`。
- 索引计划（本机 `xueyeceping_server_20260729`）：
  - `student_answer` / `preview_path` 查询均为 `ref`，rows=1；
  - 附件 `resource_path` / `preview_path` 查询均为 `ref`，rows=1；
  - `MAX(answer_id)` 分组使用 `idx_answer_lesson_student_question_answer`（`Using index`）；
  - `MIN(submit_time)` 分组使用 `idx_answer_lesson_student_submit`（`Using index`）。

## 130 发布前置门禁

1. **源码基线**：当前工作区有大量未提交改动；必须从 130 当前 release `20261025_flowchart_ai_answer_v4` 对应的已验收源码/制品基线拉独立 worktree，只应用本期文件；构建后与线上 JAR 做条目级差异核对。
2. **发布通道**：本机到 130 的 22/2222/22022 均不可达（HTTP 200）；需确认现有跳板/发布任务通道，否则只交付制品、哈希和命令清单，不能宣称已上线。
3. **备份**：`/data/backups/xueyeceping/20261028_diagnosis_perf_v1/`，含 database.sql、service.before、nginx.before、env.before、jar.before 和 SHA-256。
4. **顺序**：低峰逐条索引 → 后端 JAR → 前端 dist → systemd 重启 / Nginx reload → 探活 → 真实教师/学生冒烟。
5. **登记**：成功后更新 `contexts/RELEASE_LOG.md`，`biz_platform_update` 设 PUBLISHED，并用真实教师接口确认可见。
6. **回滚**：恢复 service.before/nginx.before，daemon-reload、重启后台、`nginx -t && reload`，退回上一版；更新记录撤回 DRAFT；索引可保留（向后兼容）。

## 剩余风险

- `selectClassMetrics` 仍是“缓存+降频+索引”方案；若 130 压测后 DB 时间仍高，再做单次扫描重写并补逐班等价性测试。
- 资源鉴权四条索引命中时不跑 JSON 兜底；同一路径多引用的极端场景需在 130 真实数据上抽查。
- 45 秒缓存意味着首页红点最多延迟 45 秒；批改锁定判断不走缓存。
- 学生删除/教研图片未做真实课堂数据验收，发布后各抽 1 次验证。
- 321 条预览失败只分类、不自动扩大重试；确认原因后再单独处理。
