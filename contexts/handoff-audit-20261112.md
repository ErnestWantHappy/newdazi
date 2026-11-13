# 交接审计提示词：诊断中心复制修复 + 视觉重构 + 慢 SQL 治理（2026-11-12）

> 用途：交给其他 AI 做**只找问题、不改代码**的审计。可直接整段复制。
> 事实来源：本轮改动由 ZCode 完成，已自测通过；以下事实卡、改动清单、风险点均来自实际执行记录。

---

## 项目事实卡（不超过 15 行）

- 项目：信息科技学业测评平台（中小学教学 + 多维度测评），位于 `D:\dmwprogram\newdazipingtai\newdazi`
- 技术栈：后端 RuoYi / Spring Boot（Java 8）/ MyBatis / MySQL 8 / Druid / Redis；前端仅维护 `RuoYi-Vue3`（Vue3 + Vite + Element Plus + ECharts）
- 角色：管理员 / 教研员 / 教师 / 学生；诊断中心仅 `admin,researcher` 可见（`@ss.hasAnyRoles`）
- 本机环境：后端 `http://localhost:8080`，前端 `http://localhost`（当前均在运行）；本地验收账号见 `AGENTS.md` 4.1 节（密码不写入本提示词）
- 正式环境：10.52.1.130（Linux/systemd/Nginx，API 3009 / 站点 80），当前线上版本 1.30.96；**本轮改动尚未发布 130**
- 数据库：本机 `xueyeceping_server_20260729`；130 库名 `ry-vue`（biz_student_answer 约 67.8 万行）
- 本轮改动：10 个文件（后端 6 + 前端 4），git 已修改未提交；无表结构变更
- 必读接管文档：`AGENTS.md` → `contexts/PROJECT_CORE.md`（v5.50）

---

# 审计提示词（复制以下全部内容给审计 AI）

````
# 角色
你是一名资深测试工程师 + 后端架构师 + 前端 UI 审查员。本轮只做审计：不修改任何文件、不部署、不执行任何写操作、不连生产环境。只输出问题、证据和建议。

# 审计对象
- 项目：信息科技学业测评平台，路径 D:\dmwprogram\newdazipingtai\newdazi
- 本机可访问：前端 http://localhost（账号见 AGENTS.md 4.1，用 admin）
- 本轮改动：诊断中心三件事——①内网 HTTP 下复制按钮失效修复（4 个页面）；②诊断中心页面视觉重构（1 个 vue 文件重写）；③慢 SQL 治理（平台概览缓存升级 + 慢 SQL 事件区分后台任务/用户触发）

# 改动文件清单与意图（git diff 可查全部改动）
后端（RuoYi-Vue/）：
1. ruoyi-admin/.../business/PlatformOverviewController.java —— 平台概览快照缓存：TTL 60s→300s，改为 stale-while-revalidate（有过期旧快照时立即返回旧值并由单线程守护池异步刷新，refreshInProgress 防并发重复；冷启动无旧值仍同步构建双检锁；@EventListener(ApplicationReadyEvent) 启动异步预热一次）
2. ruoyi-framework/.../interceptor/SlowSqlInterceptor.java —— recordSlowSql 时在请求线程解析 SecurityUtils.getUsername()（catch 后为 null），传给异步落库，用于区分"用户触发"与"后台任务"
3. ruoyi-system/.../ISysPerfEventService.java + impl/SysPerfEventServiceImpl.java —— recordSlowSql 增加 operName 参数并落库 sys_perf_event.oper_name；enrichEvent 对无用户的 slow_sql 事件给"后台任务触发"建议文案
4. ruoyi-common/.../diagnosis/DiagnosisAdvisor.java —— adviseForSlowSql 新增两条规则：SQL 含 biz_lesson_checkin → 识别为"登录页开课看板后台预刷新"（info 级）；平台概览关键词命中文案更新（warning 级）
5. ruoyi-business/.../mapper/business/ResourceAccessMapper.xml —— selectStudentAnswerIdByResourceJson 的 JSON_CONTAINS 三参改两参（原 path 显式 '$'，两参默认 '$'，语义等价；曾配套多值索引方案，索引已因 MySQL 单行多值总长上限 ERROR 3906 否决，仅保留两参化）

前端（RuoYi-Vue3/src/）：
6. views/monitor/diagnosis/index.vue —— 整文件重写 template+style，script 逻辑保留：复制换 utils/clipboard.js 的 copyToClipboard；仪表盘按数值变色（<70 青/70-90 橙/>90 红）+ 数值收敛 1 位小数（修复原版 JVM 显示 21.7070...% 超长小数）；mini 卡加图标语义色；时间线"来源"列（operName 为空显示"后台任务"标签）；暗色模式用 :global(html.dark .diagnosis-page) 完整包裹写法 + CSS 变量；JVM 卡 min-height 0
7. components/TeacherPythonSubmissionPreview/index.vue —— copySource 裸调 navigator.clipboard 改 copyToClipboard
8. views/business/collaboration/editor.vue —— copyDiagnostics 同上（保留失败时 ElMessage.info(text) 兜底）
9. views/business/researchActivity/components/PublicShareDialog.vue —— copyUrl 删掉自带的手写 textarea+execCommand 降级，统一换 copyToClipboard

# 背景：为什么这么改（审计时对照设计意图）
- 复制失效根因：平台经 http://10.52.1.130 访问（非 HTTPS 非 localhost），navigator.clipboard 为 undefined，旧代码同步调用抛 TypeError 无任何提示。utils/clipboard.js 是项目既有工具（优先 clipboard API，降级 textarea+execCommand）。
- 130 实测慢 SQL 6 条中：平台概览趋势/学校两条 4.4s/4.3s 在用户请求路径（60s 缓存过期首个用户同步等 9-10s）；"select Lessons"1.7s 每 55 分钟一次，实为 LoginPreviewService 后台预刷新（1h TTL 提前 5min 刷新，节奏吻合），不占用户请求；JSON 页图鉴权 735ms×14 次为低频兜底全表扫；课堂名单/学生档案高频但 avg 118ms/17ms 走索引正常；importData 为导入固有耗时。
- 已否决并回退的方案（不要再提）：biz_practical_attachment 多值索引（ERROR 3906 单行多值总长超限，49 页/10KB 的行超限 2888 字节）；biz_county_exam_answer.preview_status 与 sys_oper_log.cost_time 索引（130 实测行数仅 3667/15931，加索引零收益）。

# 本轮审计重点（只查这些，不扩散到其他模块）
1. PlatformOverviewController 的 SWR 并发正确性：refreshInProgress 标志的检查/设置/复位路径有没有竞态或死锁（synchronized(summaryLock) 与 volatile 标志交互）；异步刷新持续失败时是否会每个请求都触发刷新或卡死；@EventListener(ApplicationReadyEvent) 预热在应用启动失败/测试环境下有无副作用（会不会阻塞启动、REFRESH_POOL static 线程是否泄漏）。
2. SlowSqlInterceptor 在请求线程调 SecurityUtils.getUsername() 的副作用：该方法内部实现读了什么（SecurityContext？Redis token？），对无登录上下文的 Quartz/定时/预刷新线程是否保证只抛可捕获异常；每条 >1s 慢 SQL 多这次调用是否引入新的性能/死锁风险。
3. sys_perf_event.oper_name 列长度与插入安全：新写入的 operName 超长时 safeInsert 是否正确兜底（查 SysPerfEventMapper.xml 的 insert 与建表 SQL 列长）。
4. DiagnosisAdvisor 新关键词规则误伤面：biz_lesson_checkin 是否只出现在 LoginPreviewMapper.selectLessons（全仓 grep 谁还 join 这张表且可能成为慢 SQL）；平台概览规则 severity 从 info 改 warning 后，与 SystemDiagnosisController.enrichSlowSql 的 severity 覆盖逻辑组合是否仍符合预期（maxTimespan>=3000 应保持 critical）。
5. JSON_CONTAINS 两参化等价性：MySQL 两参与三参（'$'）在 normalized_pages_json 为 NULL、非数组 JSON、空数组时的行为是否完全一致；MyBatis #{} 传参与 JSON_QUOTE 组合有无注入面变化。
6. diagnosis/index.vue 重写后功能绑定完整性：对照 git 旧版逐个核对——复制诊断报告/行复制、时间范围切换、事件类型筛选、链路自检、LibreOffice 手动清理、业务提示折叠、窗口 resize、卸载清理，是否全部保留且事件仍触发；新增 icons import 是否全部真实存在于 @element-plus/icons-vue；gaugeLevel/round1 在 template 与 script 的引用是否一致。
7. 其余 3 个复制修复页面的行为等价性：PublicShareDialog 删除的手写降级与 copyToClipboard 的降级差异（焦点恢复、iOS）；editor.vue 失败兜底 ElMessage.info(text) 是否仍可达；TeacherPythonSubmissionPreview 无外层 try-catch 时 copyToClipboard 是否保证不 reject。
8. 诊断页 SQL 列改动：show-overflow-tooltip 被移除、改用 code 样式 + max-height 截断后，长 SQL 是否还能看到完整内容（功能是否退化、有无更好折中）。

# 已验证项（不必重复验证，但可抽查复核）
- 后端 mvn -pl ruoyi-admin -am clean package 通过；DiagnosisGovernanceTest 通过
- 前端 npm run build:prod 两次通过
- 复制双路径实测：localhost（安全上下文）与 http://10.52.27.200（非安全上下文，复现 130 条件）点击均出现"已复制"toast
- 暗色模式实测 html.dark 下背景 #141414/卡片 #1d1d1d；亮暗全页截图经视觉终验通过（output/playwright/diagnosis-final-{light,dark}-v3.png）
- 平台概览接口本机 3 连调 9/4/3ms；启动预热已触发（启动后诊断页即出现平台概览 SQL 记录）

# 未验证项（重点！这是最可能藏 bug 的地方）
- 130 真实数据量下 SWR 冷启动预热的 9-10 秒聚合行为、刷新失败恢复（本机数据量小无法复现）
- operName 落库后诊断时间线"后台任务"标签在真实事件的展示（本机 sys_perf_event 无慢 SQL 样本）
- 130 线上诊断中心新 UI（未发布）
- 其余 3 个复制修复页面仅编译验证，未浏览器实测

# 明确禁止
- 不修改任何文件，不执行部署、迁移、清库、git 写操作
- 不连接 130/123 生产环境做任何操作（连只读也不要，本轮用不到）
- 不要因为"看起来没问题"就跳过；不要输出"整体良好"这类结论代替具体问题
- 不要审计本轮改动之外的模块（导学单、免抽测、区域抽测等一律不碰）
- 密码/Token 不出现在你的输出里（账号见 AGENTS.md 4.1）

# 输出格式
按 P0（功能损坏/数据错误/并发或安全风险）、P1（体验缺陷/功能退化/边界行为变化）、P2（视觉与文案细节）分级，每条包含：
- 编号、级别、一句话结论
- 复现路径或代码位置（file:line，必须具体到行）
- 证据（代码片段 / 控制台输出 / 截图路径）
- 建议修法（只给方向，不动代码）
- 是否可复现（是 / 推理 / 待验证）
最后给"最该先修的 3 件事"。没有问题的检查项也要列一行"已查，未见问题"，不许静默跳过。
````

---

## 若开多个审计 AI 的分工建议

同一份提示词，只改"本轮审计重点"：

- **A 号（后端并发与数据）**：只查重点 1-5（SWR 竞态、拦截器副作用、列长、建议误伤、JSON 等价性）
- **B 号（前端功能与回归）**：只查重点 6-8（绑定完整性、复制等价性、SQL 列退化），并浏览器实操诊断页全功能
- **C 号（真实用户视角）**：用 admin 实走诊断中心新旧对照（git stash 前后），查信息可读性与误导性文案

各自加一句："你的审计编号是 A/B/C，不要参考其他编号的结论。"

## 收到报告后

按 `multi-ai-audit` skill 第三步汇总（去重/分级/冲突自核），再生成修复提示词（第四步）。
