# 信息科技学业测评平台：当前核心事实

> 上下文版本 **v4.37**，更新日期 **2026-10-13**。130 最新正式 **1.30.44**（后端现场为 `20261013_lo_crashfix_v4`，前端仍 1.30.43），助手 0.2.1。连线题 1.30.45 候选完成，**尚未切换正式服务**。旧过程在 [历史归档](PROJECT_HISTORY_20261005.md)。

## 当前焦点：连线题页图兼容（2026-10-13，本机完成、待切换）

- 用户已确认临时副本方案。Word 环绕底图将部分锚点移到图右侧，LibreOffice 分支解释不同；取绝对值或仅改正的栏内偏移都会误移。公共入口同步将 DrawingML/VML 改为页面绝对坐标，保留学生原件、垂直位置、箭头方向、成绩及毒文件原路径身份。
- 49 份含负值候选逐份核对：25 份 / 139 条连接符符合规则，24 份原样；不能宣称 49 份都丢线。仅覆盖可确定环绕边界的单节单栏正文直线连接符，表格/多栏/其他图形、正常 v:line、合法负值与真实页外图形保持。
- 57 项定向测试、clean package、49 份 LibreOffice 转换与 Word 定位对照通过，水平最大差异约 0.05pt；原件及其他 ZIP 内容不变，正常对照不变。公共入口六调用点已核对。
- `page-jpeg-v3` 防止新转换复用旧缓存，页图目录也带版本以更换浏览器资源地址并保留旧图；旧成功附件须备份后精确重算 25 份。无新依赖、配置或结构 SQL；需重启后端。候选仅替换正式 v4 的 6 个 class，前端保持。
- 候选已上传校验，整库及 25 份旧预览/状态备份完成，正式仍 v4；用户已确认等交卷高峰结束后发布，当前等待连续 5 分钟无交卷且无运行中 AI。最终候选另经公共入口/隔离分支实转三份正常与缺线对照通过。正式探活、旧图重算、登记和教师可见性未完成，不得称已上线。
- 哈希、证据及回退见 [专题验收](connector-render-fix-20261013/verification.md)，需求/设计/任务/ADR 同目录。下一步安排切换、验收旧图与正常样本、登记 1.30.45。AOCI 未同步，相关索引需后续复核。

## 当前正式修复（2026-10-13，130 后端已发布 1.30.44）

LibreOffice 毒文件隔离修复的**第二轮**。第一轮（`d3b04ca`，release `20261012_lo_crashfix_v1`）只在内存生效，
线上实测发现隔离结果从未落库，毒文件仍会被反复捞回、反复打崩进程池。本轮修掉两个真实根因，
后端 release `20261013_lo_crashfix_v3`，前端未改动。验证证据见 `output/audit-130-20261012/` 的 p82～p92 脚本与产物。

**版本号**：上一个已发布版本 `1.30.43`（`update_id=121`），本轮顺延为 **`1.30.44`**（`update_id=122`）。

### 两个根因（都已实测修复）

1. **崩溃判定不能看异常文本**：坏文档让 `soffice` SIGABRT 后，jodconverter 抛的是 `Task was cancelled`（含 `cancelled`），
   会被 `isServiceLevelFailure` 判成**服务级异常**，导致原本写在「文档级失败」分支里的毒文件登记**根本不可达**；
   而且它会**立刻补起新实例**，按进程数在同一秒内就看不出异常。
   改为转换前后各采集一次 `soffice` PID 集合，**出现旧 PID 消失即判为崩溃**，与异常文案和瞬时重启均无关。
2. **毒文件标记必须落在真正执行的链路上**：实际调用链是
   `AsyncConversionService.claimRetryAndExecute` → `enqueueClaimedPreview` → **`AsyncConversionWorkerService`**。
   上一轮的 `isUnconvertible` 判定被写在 `AsyncConversionService` 里**同名的私有方法**上（全项目零调用方，死代码），
   所以共享池与一次性隔离进程都失败后，落库的仍是「PDF 文件生成失败」。
   现在在 **Worker** 的失败分支补上判定：命中则**顶满重试计数**并把判定文本写入 `preview_error_message`。
   **手动重转会传 `ignoreRetryLimit=true` 绕过计数上限，只有这行文本能在领取前拦住它。**
   区域抽测路径同步处理；`AsyncConversionService` 的死代码已加注释标明，避免下次再改错文件。

**原始文件一律保留不删**；学生重新导出后是新文件新行，不会被标记误伤。

### 已验证事实（130 实测）

- **单测**：`mvn -pl ruoyi-business -am test -Dtest=FileConversionUtilsHealthTest,OfficeLinuxProcessScopeTest -Dsurefire.failIfNoSpecifiedTests=false -DforkCount=0`
  → **Tests run: 34, Failures: 0, Errors: 0, BUILD SUCCESS**（含 PID 身份丢失 5 例）。
- **产物**：`ruoyi-admin.jar` 108,078,833 B，SHA-256 `9843ed35f96d6aedf5316bd8a8948890d7e398258d877b0f1b5198ae95071aaa`，远端校验一致。
- **正常文件未误伤**：`answer_id=101556` 由 `failed/rc=0` → **`success/rc=1`**，PDF 重新生成（日志 `【服务模式转换】成功`）。
- **毒文件隔离闭环成立**：`answer_id=548595` 由 `failed/rc=1/"PDF 文件生成失败"` → **`failed/rc=3/隔离标记文本`**，10 秒内完成；
  `sys-error.log` 留下 `【隔离模式转换】失败 exitCode=1` 与 `登记不可转换文件（保留原文件，停止自动重试）` 两行铁证。
- **4 条毒文件行全部 `MARKED` + `rc=3`**（440616 / 514480 / 514546 / 548595）。
- **后端稳定**：`active`、`NRestarts=0`、恒为 2 个 `soffice.bin`，观察期内**无重建风暴**。
- **103 号自愈任务已恢复**：`sys_job.status='0'`，重启后按分钟连续执行（01:34/01:35/01:36/01:37），耗时 41～64 ms。
- **发布登记**：`biz_platform_update` `update_id=122` / `1.30.44` → `PUBLISHED`，published_at 2026-10-13 01:40:47；
  **真实教师账号 `19157727791` 经 `/business/platform-update/list` 实测可见**（`code=200`，`total=98`，1.30.44 位于首位）。
- **回滚**：unit 备份 `<BACKUP_DIR>/xueyeceping-130.service.bak_v3` 指向 v2；登记回滚执行
  `UPDATE \`ry-vue\`.biz_platform_update SET status='DRAFT' WHERE version_no='1.30.44';`

### 关键经验（避免重复踩坑）

1. **本机日志分级陷阱**：`logback.xml` 用 `LevelFilter` 精确过滤——`sys-info.log` **只收 INFO**，`sys-error.log` **只收 ERROR**。
   `log.warn` 在**两个文件里都看不到**（只进 console/journald）。上一轮因此误判「隔离没生效」，
   实际是**证据被日志级别过滤掉了**。排查警告级行为必须换 ERROR 或直接查库/查接口。
2. **`s.sql()` 是「执行并返回结果文本」**，不能再把返回值塞进 `run()`，否则 bash 会拿查询结果当命令跑（`rc=127 / command not found`）。
3. **`sys_job` 无 `previous_time` / `next_time`**（在 Quartz 表里）；`sys_job_log` 也无 `job_id`，只有 `job_name`。
4. **直连 SQL 改 `sys_job.status` 不会让运行中的 Quartz 注册任务**：`resumeJob` 对「从未注册过」的 job 是空操作，
   唯一可靠办法是**重启**，让它随启动按 `status='0'` 重新 `createScheduleJob`。
5. 本机 `ps` 视角与 JVM 视角一致（JVM 属主 `admin2`，`processCount=2`），进程采集本身没问题。

### 剩余风险与下一步

1. **第 5 个异常 DOCX 仍未定位**（`8ba91c9a…1cd2bde2.docx`）：在 `biz_student_answer` 中**查不到任何对应行**，
   因此既不参与重试也不触发崩溃，暂按「无影响」处理，但来源（是否为已清理的测试数据）未确认。
2. **手动重转的兜底依赖文本匹配**：`preview_error_message` 被截断到 255 字符，判定用 `contains` 关键片段。
   若未来有人改写这段文案，需同步更新 `UNCONVERTIBLE_MESSAGE`。
3. **隔离判定仍有一处 `log.warn`**（`进程身份丢失` 那行）在文件日志中不可见，不利于现场排障；
   后续可考虑把该行提升为 `log.error` 或补写独立诊断文件。
4. `AsyncConversionService` 中两份**死代码**（`convertClaimedPreview` / `convertCountyExamPreview`）未删除，只加了注释；
   建议后续专项清理，避免再次改错文件。
5. AOCI 未同步，受管源码变化后相关索引需后续复核。

## 历史正式修复（2026-10-12，130 双端已发布 1.30.43）

只读审计 130 后按用户「生成方案后直接按推荐方案改代码」完成的修复，已**正式发布到 130**。release `20261012_audit_fix_v1`（前后端同版）。方案与全部探测证据见 `output/audit-130-20261012/`（`方案-130修复.md` 与 p1–p44 脚本/输出）。

**版本号**：`1.30.41` / `1.30.42` 已于 2026-10-10 被 codex 占用（`update_id` 119/120），本次登记顺延为 **1.30.43**（`update_id=121`）。

### 发布事实（已验证）

- **前端**：nginx root 切至 `releases/20261012_audit_fix_v1/frontend`，`index.html` SHA-256 `9cf22d85d6de7a19636806ca74b82e1b9d65cfe33ba82099497617470aa1e5c8`，入口 `index-B281qkOb.js` 3,555,615 B；`nginx -t` 通过后 `reload`（rc=0，零停机）；旧 `static` 已用 `cp -an` 合并保留（2387→2727 个文件），已打开页面仍可解析旧 chunk。
- **后端**：systemd unit `WorkingDirectory` + `ExecStart` 切至本 release，`daemon-reload` + `restart`，130 本地 **18:57:34 重启，20 秒 READY**，`Started RuoYiApplication in 17.878 seconds`；危险日志模式（`Invalid bound statement` / `APPLICATION FAILED TO START` / `Error parsing Mapper` / `MyBatisSystemException`）计数全 0；新 mapper 条件命中 16 处；部署包内确认 `page-jpeg-v2`。
- **制品**：后端 `ruoyi-admin.jar` 108,072,209 B、SHA-256 `761c0c82e21eb3895f63fb1ec125cf374c012cf2ec965b4e8620b0e3673dcee7`。
- **效果**：重启后慢 SQL 由当日累计 2232 条降至 **1** 条。
- **备份与回滚**：`/data/backups/xueyeceping/20261012_audit_fix_v1/`，含整库 dump 387,133,113 bytes（SHA-256 `e7d64a65…`）与 `service.before` / `nginx-site.before` / `config.before/` / 两个 env 文件 `.before`。回滚 = 还原 unit 与 nginx root 后各重启一次；登记回滚执行 `UPDATE biz_platform_update SET status='DRAFT' WHERE version_no='1.30.43';`。
- **发布登记**：`biz_platform_update` `update_id=121` / `1.30.43` 已置 `PUBLISHED`（published_at 2026-10-12 19:11:17），**真实教师账号 `19157727791` 调 `/business/platform-update/list` 实测第 1 条可见**（`code=200`，`total=97`）。`contexts/RELEASE_LOG.md` 已登记。

### 已完成的代码改动（9 个文件）

- **后端 SQL 性能**：`StudentProfileMapper.xml`（4 处）+ `BizStudentAnswerMapper.xml`（12 处）移除冗余派生表 join；后者另修 10 处空作品过滤（补 `practical_artifact_id` 判定，覆盖成绩汇总与 `selectClassStatusByLesson`）。
- **课堂一键推进**：`countScoredStudentsByLessonAndClass` 原忽略「只提交操作题作品、无文本答案」的学生，导致班级达不到推进门槛、自动与手动推进均被拒；现按唯一键直接统计，条件放宽为「有文本框内容、有分数或有操作题作品」任一即计入。**该改动是严格超集，只会让推进门槛更易达到。**
- **后端压缩包**：`BizQuestionServiceImpl.handlePracticalQuestionFile` 对 ZIP/RAR/7Z 置 `unsupported`、不再交 LibreOffice（原会失败并永久卡 `pending`）；`MimeTypeUtils` 白名单补 `7z`。
- **后端页图精度**：`PracticalPageRenderer` DPI 120→150、MAX_EDGE 1800→2000、JPEG 质量 0.85→0.92，`RENDERER_VERSION` 升 `page-jpeg-v2` 以失效旧缓存。
- **前端**：`PracticalTextPreview` 源码区撑满父容器并在内部滚动（修「只能看一截」）；`grading.vue` 星级 `texts` 按满分缓存消除频闪，并新增「页图 / PDF 原图」切换（默认仍为页图）；`designer.vue` 指派班级加全选/反选/清空；`question/index.vue` 提交格式默认全勾 16 项、起始文件与资源支持 RAR/7Z、修复 `downloadPythonTemplate`（原引用未定义变量 `questions`，点击必抛错）。

### 关键实测事实（生产库只读，判定依据）

1. **`biz_student_answer` 上 `uk_student_lesson_question` 为 UNIQUE**（三列 student_id/lesson_id/question_id），且 `rows_all = groups_cnt = 424414` 严格相等 → 原 `MAX(answer_id) GROUP BY 三列` 派生表是**恒等式**，join 回原表等于原表全选；其全局 GROUP BY 会强制物化全表并使 `WHERE lesson_id` 无法下推。**A/B 对拍 4 组查询结果完全一致（diff = 0）**。
2. **线上 jar 与 git 的 9 处语句差异已逐条归一化比对**：除「派生表写法」外**无任何其他未入库业务逻辑**，改写后不存在「从 HEAD 打包会静默回退」的风险。
3. **空作品过滤原有真实漏算**：`practical_version_id IS NULL` 判定会漏掉「已交作品但未生成版本号」的提交。全库被挡 **196 行，其中 150 行带真实 `practical_artifact_id`**，仅 46 行是真正空白。
4. **lesson 639「一键推进 0/43」不是 bug**：该课全平台答题表 0 行、`biz_student_task_state` 0 行、`classroom_performance` 0 行；1/2/8 班实际提交的是 lesson 572（42/41/38 人）。
5. `schedule-pool-*` 的 1962 条 ERROR **全部来自 Druid `StatFilter` 慢 SQL 告警**，非 Quartz 异常。

### 验收状态

- `mvn -pl ruoyi-business -am compile` 通过；`mvn -pl ruoyi-admin -am clean package` 通过。
- `npm run build:prod` 通过（EXIT=0，0 错误）。
- 线上后检：三服务 active、`/profile/` 200、验证码 200、gzip_static 命中（入口 Content-Length 957,769 + `Content-Encoding: gzip`）。
- **未做**：文件操作题连线的**肉眼最终效果**未做浏览器实测（仅提升渲染保真度并增加原图切换）；LibreOffice PDF 导出滤镜参数未改；未做课堂时段整班并发压测；学生端 6 项交互未逐条现场复验。

### 剩余风险与下一步

1. **Quartz 已于 2026-10-12 20:16:53 启用（原「未解决」结论作废，需重启才生效）**：`releases/20261008_seat_install_v1/migration-isolation.env` 第 2 行 `SPRING_QUARTZ_AUTO_STARTUP` 由 `false` 改为 `true`（文件 SHA-256 `b72c39e4…` → `8e8928f1…`；改前备份 `migration-isolation.env.20261012_201649.before_quartz_on`，sha 与改前一致），`daemon-reload` + `restart` 后 20 秒 READY，`MainPID=2181061`、`NRestarts=0`。**硬证据**：`/proc/2181061/environ` 读到 `SPRING_QUARTZ_AUTO_STARTUP=true`；日志出现 `[quartzScheduler_Worker-N]` 线程；`sys_job_log` 自重启后新增 24 行（20:18–20:35），101/103/105 号 `status=0`、`exception_info` 全空、`SCHEDULER_EXCEPTION=0`，耗时 103 号 45–68 毫秒 / 101 号 307–514 毫秒 / 105 号 4436–5757 毫秒。**开启前后错误面未变化**（重启前 5 分 23 秒 8 条慢 SQL，重启后同样水位；错误日志里 1800 余行 Broken pipe 是学生浏览器断开的正常堆栈，非业务异常）。
   > **发布红线**：该开关只存在于服务器 env 文件，**仓库内没有任何模板或脚本设置它**（仅两份上下文文档提及）。下次发布若沿用 `20261008_seat_install_v1` 的旧 env 副本，会再次静默关闭全部定时任务；发布核对清单必须比对这一行。
2. 页图精度提升会使既有页图缓存失效，首次访问按需重渲染，课堂高峰有一次性转换压力。
3. 预览积压已核清（2026-10-12 20:40 实测）：`biz_student_answer` 中操作题且 `preview_path IS NULL` 共 3676 条，其中 **3327 条 `preview_status` 为 NULL**——这些的 `student_answer` 存的是 **Python 代码正文**（非文件路径），本就不参与文件预览转换；另 238 条已成功、204 条 `failed` 且重试次数已达 3 封顶。**21:00 的 100 号任务实际只会捞走 4 条**，不存在「315 件一次性放量」风险。`ResourceAccessMapper.selectStudentAnswerIdByResource` 两列均无索引的鉴权全表扫仍未动。
4. AOCI 未同步，相关索引需后续复核。

## 当前正式修复（2026-10-10，130 前端已发布 1.30.42）

登录页视觉渲染降级与云机房防卡死优化，**仅前端发布、零停机、未重启后端、无结构 SQL**。release `20261010_login_render_fix_v1`；线上 `index.html` SHA-256 `f0361aaf045d693339fddf7339e44d438b2206079b1564c4dc54a09ad9fd116a`，前端 zip SHA-256 `5f7de34deb3cfcb5c432fbc18a55e6b02df1f93ef53d143e6fa703a628abb0b7`。

- **改动**：针对老旧电脑、Win7 与实验小学等云桌面环境（无独立显卡，使用 CPU 软件渲染）画面合成管线阻塞的问题，修改 `login.vue`：
  1. 移除模板中的 3 个全屏动态高模糊光晕（原 `blob-1/2/3`），样式 `.login-bg-mesh` 设为 `display: none !important;`，彻底剔除 `filter: blur(80px)` 滤镜计算；
  2. 移除 4 个模块卡片 `.module-card` 的 `backdrop-filter: blur(16px)` 和 `-webkit-backdrop-filter: blur(16px)` 毛玻璃效果，背景调整为扎实纯白 `#ffffff`，保留浅蓝边框与微投影；
  3. 保留教研大图轮播、开课看板数据、账号密码登录与验证码全部交互逻辑。
- **效果（实测）**：
  - 本地与诊断隔离实验实测：页面帧率由 12 FPS 恢复至 60 FPS 满帧，输入响应延迟从 141ms 降至 29ms 以下；
  - 线上探活检查：线上 `login-*.js` 中 `backdrop-filter:blur` 命中数为 0；
  - 线上旧静态资源已平滑合并（`cp -an`），旧入口文件访问返回 200，绝对无 404；三服务 active。
- **发布登记**：`biz_platform_update` update_id=120 / 1.30.42 已置为 `PUBLISHED`，真实教师端接口实测可见。
- **备份与回滚**：站点配置备份至 `/data/backups/xueyeceping/20261010_login_render_fix_v1/xueyeceping-130.before`；回滚只需将 Nginx root 切回 `20261009_entry_split_v1/frontend` 并 `nginx -t && systemctl reload nginx`。

## 历史正式修复（2026-10-10，130 前端已发布 1.30.41）

入口拆包与表单设计器按需加载，**仅前端发布、零停机、未重启后端、无结构 SQL**。release `20261009_entry_split_v1`；线上 `index.html` SHA-256 `66a862cc303e2774ea4571d837a3fe6c2e065acc7538cb50979275d31bfc0656`，前端 zip SHA-256 `e8c17d8cb897d59d84b07574f927ebbd1d6dac2e67ff474d701c9ee93b774d1f`。

- **改动**：`vform3-builds`（自包含 UMD 约 1.31MB、gzip 约 319KB，另 147KB CSS）原在 `main.js` 静态引入并 `app.use` 全局注册，使登录页首屏必须下载执行。现改为 `main.js` 挂载后只登记 app 实例（`setVFormApp`，不触发加载），进入导学单类路由（`/guide-?sheet/` 或含 `/lesson/`）时由 `permission.js` 守卫调用 `ensureVForm()` 按需加载并 `await vformReady` 后再渲染。
- **效果（实测）**：入口 chunk 3,548,025 B → 1,711,816 B；**gzip 957,861 B → 558,025 B（-42%）**；`index.html` 中不再出现 `designer.umd` 预载。
- **关键坑**：生产构建该 chunk 导出为 `export{designer_umd$1 as d}`（Rollup 的 CJS interop 命名空间），**`mod.default` 为 undefined**；若按 `mod.default || mod` 取用，`app.use` 会静默失效、导学单整页空白。实现中改为按结构逐层解析（带 `install` 的对象 / 函数插件）的 `resolveVFormPlugin()`；浏览器探针实测 `resolvedAs=key:d`、`pluginHasInstall=true`。
- **验证**：本地构建产物联调与线上 130 直连两轮一致——登录页零 vform3 请求、零 pageerror；导学单与学生导学单进入后才加载 vform3 且无「组件未注册」错误；教师工作台正常。线上旧入口 chunk 仍 200（新旧 `static` 已合并），三服务 active。
- **同步服务器配置修复**：`/etc/needrestart/conf.d/99-xueyeceping-noauto-restart.conf` 将 `xueyeceping-130.service` 的 `override_rc` 置 0，阻止 unattended-upgrades 升级共享库后 needrestart 自动重启服务。
- **两个 1.30.40 遗留项闭环**：① **06:03:53 SIGTERM 根因已查明**——`apt-daily-upgrade` 升级 `libpng16-16t64`（java 经 `libfontmanager.so` 加载该系统库）后，needrestart 执行了 `systemctl restart xueyeceping-130.service`（`/var/log/unattended-upgrades/unattended-upgrades-dpkg.log:305` 原文），10-06、10-08 同型；② **logrotate `su root root` 修复已验证生效**——2026-10-10 00:00 `logrotate.service` 首次 `status=0/SUCCESS`，生成 `xueyeceping-130.stdout.log.1`（1,084,737,028 B），新文件从 0 开始，此前 10-07/08/09 连续三次 FAILURE。
- **回滚**：把 Nginx root 切回 `20261009_slowness_fix_v1/frontend` 并 `nginx -t && systemctl reload nginx`。站点配置备份 `/data/backups/xueyeceping/20261009_entry_split_v1/xueyeceping-130.before`（SHA `60b15aff…`）。
- **未解决**：`ResourceAccessMapper.selectStudentAnswerIdByResource`（资源预览鉴权，`student_answer = ? or preview_path = ?` 两列均无索引 → 43.7 万行全表扫；诊断日 262 次、最大 1,608ms）需索引类改造，前轮曾试用后回滚，本次未动；`StudentProfileMapper` 4 条仍用全局派生表；needrestart 排除配置已加载但触发条件（库更新 + 进程持旧 inode）当前不成立，**须在下次库升级时复核**；导学单设计器默认「新手向导」，高级模式的 `<v-form-designer>` 未做实操验证。发布登记 `biz_platform_update` 119/1.30.41 已 PUBLISHED，真实教师接口可见。

## 当前正式修复（2026-10-09，130已发布1.30.40）

首页「进入即卡顿、按钮点不动、输入无响应」经只读诊断确认**不是服务端容量问题**（白天零 5xx；堆仅用 133MB、FGC 0 次；宿主可用内存 28GB）——直接原因是登录首屏必载 **3.39MB 未压缩入口 JS + 527KB CSS + 688KB PNG Logo + 831KB JPG**，而无 gzip；线上约 79% 的 UA 是 Chrome 83/86（含 Win7），机房 40~50 台机整点同开时校园上行饱和，页面长时间停在 `index.html` 的「正在加载系统资源」遮罩上，登录后又被 `route-pending` 全屏遮罩 + `pointer-events:none` 锁住按钮，学生首页 `beforeunload` 再让页面关不掉。

已发布修复：① Nginx 启用 `gzip_vary/proxied/comp_level 6/gzip_types` + `gzip_static`，`/static/` 加 immutable 长缓存（入口 JS 3,555,615→957,772 B，CSS 527,316→71,292 B）；② Logo 改 512×512 WebP（704,963→62,730 B），`slide-1/2/3` 改 WebP（826~904KB→118~142KB）；③ 补齐 **1.30.39 漏发的前端**（登录厅首图优先、其余 4 张 15 秒后加载，实测轮播项 1→5）；④ `permission.js` 增加 10 秒遮罩兜底解除；⑤ 学生首页退出拦截改为只拦未提交作答并写 `sessionStorage` 草稿；⑥ shared env 新增 `IOT_MQTT_CLIENT_ID=dazi-platform-iot-130`（消除与 123 同名互踢，`connectionLost` 归零）与 `LOG_PATH=/data/logs/app`；⑦ logrotate 改 `su root root`，修复日志 1.06GB 不轮转；⑧ jar 内 `BizStudentAnswerMapper.xml` 为 9 处「外层已限定 lesson」的语句改用按课程限定的派生表（真实数据对拍结果一致，1578ms→17ms、2036ms→15ms，业务慢 SQL 11.1→2.2 条/分钟，仅 1 个嵌套条目变化）；⑨ 2 条停滞 3 个半月的 `converting` 脏状态置为 `failed`。

前端 `index.html` SHA-256 `f5e0b47f59501a47ece77d3556bff004e8562c37918dd88c8aa856240b5dc694`；后端 JAR SHA-256 `0f0dc0dfbea3b8b43a64d6722d2cb04beddaa8ae53a0d531bcd890193ce54fe2`（基于 1.30.39 的 `f63d50d6…`，嵌套 `ruoyi-business-3.9.0.jar` 由 `5c1fa58f…` 变为 `8f20024e…`）。整库备份 362,841,290 bytes，SHA-256 `3086521a0eb1f9aab487193d5349737da5643a4e53be1b2eeed2bc3f7d16ef82`，配置备份 `nginx.conf.before`/`nginx-site.before`/`logrotate.before`/`env.before`/`service.before.newbackend` 同目录。更新 118/1.30.40 为 PUBLISHED，真实教师接口可见。Playwright 实测登录页零异常、输入可用；管理员/教师登录与菜单渲染通过。**已知代价**：本次共重启后端 5 次（12:41 env；13:14 jar；13:26、13:30 恢复 MQTT 未果；13:38 修正目录属主后成功）。今日 5xx：06 时 20、12 时 128、13 时 8,273，全部集中在重启当分钟，均为轮询客户端重试，13:39 起恢复正常，未发现数据丢失；课堂高峰在线连接可达 576，**重启必须避开课堂时段**。**事故已修复**：13:14 切换 jar 后 MQTT 持续连接失败，根因是新 release 的 `backend` 目录由 sudo 创建、属主为 root，服务以 admin2 运行时 Paho 无法在工作目录写持久化文件（`MqttPersistenceException`），`chown -R admin2:admin2` 后 13:38:15 恢复连接（`connectionLost`=0）；同一类成因也是历史 logback `./logs` 启动失败的根因，**新 release 目录切换 unit 前必须先 chown admin2:admin2**。**遗留项状态（2026-10-10 更新）**：① 06:03:53 的 SIGTERM 发起者**已查明**为 needrestart 自动重启（见上方 1.30.41 段与 `output/130verify-diag-20261009/report.md` F-01）；② 入口 chunk 中 `vform3-builds` **已在 1.30.41 拆出首屏**；③ `StudentProfileMapper` 4 条及 `selectScoreSummaryByStudents`/`selectRecoverablePracticalAnswersForRetry` 仍用全局派生表，其余主要家族（`selectStudentAnswerMatrix`、`selectClassStatusByLesson` 等 9 处）经 1.30.41 核实**已改为按课程限定**；④ 315 条 `failed` 转换积压未清理；⑤ 学生端本地草稿仅静态验证。AOCI未同步。

## 当前正式修复（2026-10-08，130已发布1.30.39）

登录展厅保留真实教研图片，首图优先加载，其余4张约15秒后延迟加载；后端新上传图片自动生成独立JPEG展示图（最长边1600、JPEG质量约0.78），原图保留，历史图首次访问可补生成。下午卡顿根因是5张1.94～4.15MB原图高并发下载，约24,637次请求、下行约74.7GB，造成带宽竞争、浏览器解码和输入主线程压力；CPU/内存/磁盘/GC/Nginx 5xx未见持续故障。

正式release为 `/data/apps/xueyeceping/releases/20261008_login_preview_compress_v1`，JAR SHA-256 `f63d50d670823c320b9e46540d3864549748ade7c19cfa269b84632d463ef51a`；systemd已指向该release，三服务active，验证码/首页/焦点图均200。当前5张展示图约201～268KB，原图仍保留；平台更新117/1.30.39为PUBLISHED。数据库备份 `/data/backups/xueyeceping/20261008_login_preview_compress_v1`，352203397 bytes，SHA-256 `2e11db25baa9ff1041d0d21ed605218e0eea638f34480e0d85a336a82a4a73a9`。回滚为恢复旧release路径并重启后端，不需回灌数据库。浏览器已确认登录页账号/密码输入框可用，焦点图接口和页面无5xx；云机房真实课堂仍需现场复测输入延迟。AOCI未同步。

## 当前正式修复（2026-10-08，130已发布1.30.38）

学生登录输入阶段减少非关键工作竞争：展厅接口延迟至约1.2秒并在浏览器空闲时启动，轮播图片懒加载/异步解码，页面卸载时清理定时器、空闲回调和请求。仅发布隔离构建的前端；无后端、接口、权限、配置或表结构变更，无结构SQL。正式release为 `/data/apps/xueyeceping/releases/20261008_login_input_latency_v1`，首页SHA-256 `4e9fd77dff9014fadb05127b295dcf6a182fe135f841c96c072668dfbc78a840`。三服务active，验证码接口200；Chromium线上登录页可填写账号/密码、无页面异常/HTTP 5xx/横向溢出。平台更新116/1.30.38为PUBLISHED，真实教师列表接口可见。整库备份 `/data/backups/xueyeceping/20261008_login_input_latency_v1/database.sql`，343779698 bytes，SHA-256 `2036e6999113a8d7d6e2c6d6af2dcfebeccf62940379c7ab8c175c6999262dd8`。回滚仅需恢复本次 `nginx.before` 中旧release根路径并 `nginx -t && systemctl reload nginx`，不回灌数据库。真实课堂高并发下的主观输入延迟尚未量化；AOCI未同步。

## 当前正式修复（2026-10-08，130已发布1.30.37）

课堂监控优先助手内网IP，回退时标明连接IP；姓名与IP分行，教师/学生界面隐藏自动电脑指纹名，保留人工机位名。助手0.2.1优先D盘安装并从安装目录运行，回退目录、快捷方式和当前Windows用户登录自启结果明确展示；关联文件保留，关闭结果窗口仅退托盘。老师仍须保存还原基线并现场重启验证。

客户端30项、Node3项、独立前端构建通过。正式三服务active，真实教师45人课堂姓名/学生首页/桌面手机登录页通过，教研员登录及16条路由接口通过；学生实际下载EXE主体与0.2.1完全一致并以自检参数运行通过。更新115/1.30.37 PUBLISHED且教师可见。仅4个前端运行文件及助手升级；JAR字节保持原版，共享env仅改助手下载路径并已重启后台，无结构SQL。旧release及整库备份保留，回滚恢复本次service/nginx/env并撤回更新115，不回灌整库。详见 [正式验收](seat-install-release-verification-20261008.md)。

正式验收课堂无在线助手设备，真实内网IP对照及学生机安装/开机/还原尚未验证。下一步学生机退出旧助手后运行新版、保存还原基线并重启验证；不能宣称仅放D盘即可抗还原。AOCI未同步。

## 当前正式修复（2026-10-07，130 已发布 1.30.36）

区域抽测评卷筛选、答卷详情和评分提交已隔离迟到请求；学生明细改用服务端分页，并隔离跨考试迟到响应。成绩页分析和矩阵随课程、班级、年份查询失效，重新查询时清空旧成绩，避免旧数据与新筛选并列显示。Python 题单草稿串行保存，运行/提交固定题目与代码快照，切题后旧判题响应不再覆盖新题历史；题目加载失败后保留原题，允许重试。整班 PDF 拆分由服务单例全局串行改为同原件串行，不同原件可并行，原件幂等规则不变。

隔离候选 Vue3 生产构建、后端打包通过；Node 7 项请求交错测试、Java `PaperBatchImportServiceTest` 4 项通过。130 双端已发布，三服务 active，新 JAR/首页哈希与制品一致；真实教师/学生/教研员只读接口和桌面/手机登录页通过，更新 114/1.30.36 为 PUBLISHED 且教师可见。无结构迁移、配置、接口或权限变更，仅执行产品更新登记 SQL；未在正式库写入真实答卷或进行整班 PDF 并发压测，关键交互仍需现场连续操作验收。AOCI 未同步，相关索引需后续复核。

## 当前修复（2026-10-07，130 已发布 1.30.35）

三份 AI 报告按当前源码复核，逐条结论和文件清单见 [审查与修复记录](bug-review-20261007.md)。已修登录破图/选校刷新/跨标签残留/重复提示，匿名图片统一有效性检查；修复退回题关闭后重交、空作品推进与班级人数、流程图版本预览和草稿冲突恢复、布局状态丢失、机位轮询/错误状态/重复注册、7Z下载与HTML标准模式。区域抽测上传仅收转换器已支持格式；旧不支持文件未迁移。

用户本轮明确保留真实教研图片公开展示，以及独立课堂表现（含0分）计分。机位跨房不自动迁移已确认基准，旧机房仍可查看。Java定向180项（178通过、2环境跳过）、Node32项、Vue3构建及Chromium模拟接口5项通过。130双端已发布、三服务active、教师/学生只读接口和桌面/手机登录页通过，更新113/1.30.35为PUBLISHED且教师可见。正式教研员新凭据登录、角色和16条路由接口通过，浏览器菜单点击未验；真实作品写入、设备注册写入和机房客户端未验。无结构迁移/新依赖/配置变更，产品更新SQL已执行；AOCI未同步，相关索引需后续复核。下一步在隔离库验证写入分支，并安排机房现场验收。

## 1. 接管顺序与事实来源

> 2026-10-07 开发辅助规则调整：日常任务按 `AGENTS.md` 的“AOCI 调用策略”直接读取核心资料与相关源码，不自动全量 Overview 或维护索引；只有明确的 AOCI 专项任务进入完整流程。已核对当前工具合同仍要求完整加载，本次只调整项目调用边界，未修改 AOCI 引擎或索引，未验证新会话耗时。索引可能落后于源码，不能据此宣称治理对齐。无 SQL、业务配置、重启或发布要求；下一次日常任务验证不再自动启动全量加载。

1. 先读 `AGENTS.md`，再读本文件和 [架构索引](../docs/architecture/INDEX.md)。
2. 按任务读取相关架构、专题需求/设计/任务/ADR，不默认通读历史。
3. 当前源码、可复核数据库与部署证据优先；文档冲突时核对最新日期及目标环境。
4. 保留工作区既有修改。本机有大量未提交及未跟踪业务文件，不能把整个工作区视为某一正式版本，也不能未经核对混入发布制品。

## 2. 系统与目录

| 层 | 当前事实 |
| --- | --- |
| 后端 | `RuoYi-Vue/`：Java 8、Spring Boot、RuoYi、Maven 多模块；`ruoyi-admin` 启动，`ruoyi-business` 承载业务。 |
| 前端 | 唯一维护 `RuoYi-Vue3/`：Vue3、Vite、Element Plus；旧 `RuoYi-Vue/ruoyi-ui/` 不默认修改。 |
| 数据 | MySQL、Redis、MyBatis Mapper XML；表结构、接口与事务修改须同步专题资料。 |
| 外部能力 | Judge0 判题、CryptPad 协作、EMQX/MQTT、WebSocket、LibreOffice 预览/转换；边界见 [集成说明](../docs/architecture/INTEGRATIONS.md)。 |
| 本机客户端 | `clients/seat-helper/`：可选 Windows 机位助手，不是登录或作答前置条件。 |
| 迁移与制品 | SQL 只放根目录 `sql/`；`uploadPath*`、备份、日志、`output/`、`tmp/` 属于业务文件/本机产物，不默认作为业务源码。 |

## 3. 当前环境与正式版本

| 环境 | 最近已核验或记录的状态 |
| --- | --- |
| 本机开发 | 后端默认 `8080`，Vue3默认 `80`；今天登录专题记录实际联调 `8080/5173` 已启动。只读确认 `application-druid.yml` 配置库名仍为 `xueyeceping_server_20260729`，本整理轮未连接数据库或启停服务。 |
| 130正式主站 | `http://10.52.1.130`，Linux/systemd/Nginx；最新产品版本 **1.30.38**，三服务 active，登录输入优化已发布，更新116教师可见。不是旧123的NSSM发布栈。 |
| 123环境 | 10.52.1.123 3009/3010；最新 release 20261005_ui_redesign_v1（1.30.29）已上线，双服务Running，探活200。 |
| 129扩展服务 | 承载 Judge0、CryptPad、EMQX 等独立服务；连接、代理和健康证据按集成/部署手册查阅，不凭旧记录假定当前可用。 |

### 历史正式：登录页流式自适应、位置对调与Logo放大升级（2026-10-07，1.30.34）

- 登录页信息架构与自适应布局重大优化：
  1. 「区域学校开课动态与监管看板」移至左下，享有约 60% 的充裕宽屏空间，7 列表格字段舒展对齐，彻底告别横向挤压与滚动条；
  2. 「最新教研活动预览」移至右下，位于登录框正下方，右侧形成规整紧凑的垂直信息栏，消除右下角局促偏长、左下角空旷的失衡问题；
  3. 页面采用 Flex 流式与视口弹性比例，最大宽度扩展至 `min(1560px, 94vw)`，行高自适应 `minmax(330px, 39vh)`，内部全 Flex 撑满，焦点大图轮播与监管表格随视口高度自然展开；
  4. Header Logo 放大至 58px×58px 并微调标题视觉层级，浏览器 Tab 栏 Favicon 引入追加版本防缓存机制；
  5. 生产构建打包通过，远程 `index.html`（`3f565a1be5c0fe3d78f4d5edf94c86ba7bbdd7f0832071e176c3d08e6e3f10f1`）、`favicon.ico`（200）、`captchaImage`（200）探活通过，Playwright 1440×900 实景截图留存。平台更新记录 update_id=112 标记为 PUBLISHED，并通过真实教师账号（19157727791）在 `/business/platform-update/list` 接口中实测可见。

### 历史记录：全站统一全新高清Logo与Favicon焕新升级（2026-10-07，1.30.33）

- 全平台深度统一替换为用户指定课题申报专属徽标（帆船海浪造型的高清透明背景矢量级 PNG，分辨率 1254×1254）：
  1. 登录页顶部导航、管理后台侧边栏、学生端三大核心模块（日常课程题目、电子导学单、区县测评）等全部统一生效；
  2. 浏览器 Favicon 全面同步生成包含 16×16、32×32、48×48、64×64 四层级的高清 ICO 文件；
  3. 登录页 Header Logo 配备柔和微光发散滤镜（`filter: drop-shadow(0 4px 10px rgba(22, 119, 255, 0.22))`），与整体温润轻拟物背景浑然一体；
  4. 生产环境构建部署通过，远程 `index.html`（`38f5b165bd485b432374d07489d6e8397e90ff635bfddab2d99a5fd51d9aa938`）、`favicon.ico`（200）、`logo-Q7XUGtGO.png`（200）探活通过。Playwright 实景登录与教师端侧边栏截图留证完备，平台更新记录 update_id=111 标记为 PUBLISHED，并通过真实教师账号（19157727791）在 `/business/platform-update/list` 接口中实测可见。

### 历史记录：登录页2×2网格对齐、真实课堂大图展示与密码输入优化（2026-10-07，1.30.32）

- 登录页架构升级为严格对称的 2×2 扁平 CSS Grid 架构：
  1. 行高设定为 350px 与 320px，实现左上教研焦点大卡片与右上登录框、左下教研预览与右下开课监管看板高度与水平线绝对对齐，登录框宽度充满右列栅格，告别不对齐错位；
  2. 活动焦点图直连真实教研与课堂跟帖实况（`biz_research_post`），自动提取象山二中潘桑柔老师上课现场高清实拍大图（黑板写有《2.2 算法的控制结构》）进行图文轮播，包含异常自动降级保护；
  3. 彻底删除“记住密码”复选框并清理历史残留密码 Cookie，提升系统合规性与安全性；
  4. 方案一落地：密码框配置 `show-password` 明密文切换、实时过滤中文字符防误输入、大写锁定（Caps Lock）状态轻量提示。
- 后端新增 `/business/login-preview/focus-slides` 与 `/post-image` 安全流式接口，Java 12项单测全绿，热备份（332MB）与 130 部署通过，平台更新记录 update_id=110 标记为 PUBLISHED，真实教师账号已验可见。

### 历史记录：登录页四模块全新布局、统一Logo与柔和浅色主题（2026-10-06，1.30.31）


- 登录页结构全面升级为四模块（2x2左右双栏）布局：
  1. 左上角独立 Header 设立统一 Logo 占位图（@/assets/logo/logo.png 全平台同步更新），配置优雅偏距，彻底删除原深色板块与旧文案；
  2. 左上主模块：最新教研活动焦点图（5张图文结合轮播走马灯，展示主题、标签、日期及学校教师）；
  3. 左下主模块：最新教研活动预览列表；
  4. 右上主模块：用户登录框（靠右对齐放置，精致白磨砂微阴影卡片）；
  5. 右下主模块：区域学校开课动态与监管看板（实时表格滚动展现）。
- 整体页面基调重构为柔和、清爽、温润的浅色教育科技微渐变风格（晨曦天霁浅蓝，告别深色与刺眼黑蓝背景），所有卡片采用晶莹白磨砂玻璃质感。
- 后端扩展：LoginPreviewMapper.xml 将活动查询扩容为 LIMIT 5 并增加首图提取支持，LoginPreviewController 增加 @Anonymous 的安全 ctivity-image 接口流式回传首图；单测 11 项全部通过，前端生产打包成功。

### 历史记录：登录页双展厅及性能热修（2026-10-06，1.30.30）

- 登录页全景磨砂双展厅已上线：主框水平居中，删除指定三处文案；保留原三张背景/轮播、选校、角色跳转与防重复登录；服务器启用验证码时提交后弹窗校验，桌面双展厅、手机纵排。
- 两个固定匿名GET概览 `/business/login-preview/activities`（最多3条真实主题）与 `/lessons`（90天最多6条课程班级），独立60秒有效快照，后台预热/提前刷新，命中读取不等刷新锁；摘要字段双重白名单，不返回正文、附件地址、分享令牌、内部ID、学生身份或具体成绩。原列表/详情仍鉴权，匿名读取HTTP200/业务码401。
- Java6项、后端clean package、Vue3构建、真实SQL/API和Chromium26项（三角色）通过，页面异常0；证据见 [登录专题验收](login-showrooms-20261005/verification.md)。当前本机有效学生账号以该专题为准，旧常规课验收账号在迁移库不存在，不推翻历史结果。
- 以正式1.30.27隔离源码只叠加7个登录文件，独立Java6项和双端构建通过；新双端已发布并重启后端，共享环境完整不变，助手0.2.0保持。正式3活动/6课程、匿名详情业务401、五尺寸居中及教师/学生登录通过，页面异常/HTTP500为0。更新107 PUBLISHED且真实教师可见；仅执行产品更新SQL，无结构迁移。备份哈希、验收及回退1.30.27见 [正式验收](login-showrooms-20261005/release-verification.md)。原冷查询5.2秒是1.30.28证据；1.30.30改为先课/学生汇总再匹配班级，冷查询1.3～1.4秒，17次跨80秒请求最大49ms、12并发最大29ms，Java11项/独立构建通过，前端与环境保持。更新108 PUBLISHED且教师可见，无结构迁移；备份/回退见[性能正式验收](login-showrooms-20261005/performance-verification-20261006.md)。正式教研员本地测试凭据不匹配，未完成该角色登录冒烟。

## 4. 不可混淆的业务边界

- 管理员、教师、学生、教研员复用既有账号、学校、班级、课程和权限；服务端鉴权/数据范围是最终边界，前端隐藏不能替代。
- 课程推进后，旧课学生写入立即拒绝；教师退回和学生重交仅限当前课。旧作品、成绩仍可查阅和批改；旧“15分钟补交”记录已失效。
- 文件/流程图正式成绩为空时可更新；**0分也是已批**。当前课退回保留旧分，成功重交才归档旧分并转待批，失败不清分。草稿不能替代正式作品，AI建议须教师采用才写正式成绩。
- 作品删除、答案/当前版本指针、课堂状态必须保持事务一致；反馈、成绩和导出共享未提交事实。历史残留不能因新逻辑已修而称已治理。
- TXT、单HTML、ZIP/RAR/7Z需题目显式允许；TXT/HTML不超过2MiB，文件名含扩展名不超过100字符。HTML用独立沙箱预览，压缩包不解压；这些格式不进入Office转换或AI图像评分。旧默认与区域抽测白名单不自动扩展。
- 学生反馈只返回本人正式作答/成绩；教师明确整班发布后生成不可变个人快照，报告重试不自动替换已发布反馈，作品变化须提示旧版本来源。
- 答案/成绩、课堂任务状态、终端在线Presence是不同事实；实时推送在事务提交后发出，并由REST校准。
- 独立Python刷题不写课程答案、不改变课程成绩或推进；协作由平台掌握课程/班级/文件权限；物联网作为实验辅助，不新造评分体系。
- 教师模型Key按教师/厂商加密保存，任务按创建时厂商取Key。未知单价不能写成0；三厂商接口契约测试不等于真实模型调用或评分准确率验收。

## 5. 最新验证证据与迁移要求

| 阶段 | 已记录验证 | 证据入口 |
| --- | --- | --- |
| 1.30.24课堂体验 | Java676项（673通过、3跳过）、Node26项；正式API13组、浏览器9组，最终页面异常/HTTP500为0。 | [正式验收](classroom-experience-20261002/release-verification-20261003.md)、[专题任务](classroom-experience-20261002/tasks.md) |
| 1.30.25成绩人数 | 真实教师课程629/12班42/42与成绩查询一致，课堂跳转通过；仅前端发布，未重启后台。 | 历史归档对应1.30.25段、`output/class-selection-release-20261003/` |
| 1.30.26助手 | Java定向3项、客户端15项、Node1项及三端构建通过；正式API7组、浏览器6组，无助手提交及同标签页Python超过98秒续期通过。 | [助手正式启用验收](classroom-experience-20261002/seat-helper-release-verification-20261003.md) |
| 1.30.27自动关联 | Java708（3跳过）、客户端18、Node1及独立构建；正式API4组/浏览器7组，实际下载EXE配对与DPAPI、同页重复关联、教师命名、Python续期和撤销均通过；页面异常/HTTP500为0。 | [自动关联验收](classroom-experience-20261002/seat-helper-auto-verification-20261005.md) |
| 1.30.30学校动态性能 | Java11项、隔离clean package；SQL同快照6行一致，冷1.3～1.4秒，17次跨80秒最大49ms，12并发最大29ms；白名单/详情401/页面通过。 | [性能正式验收](login-showrooms-20261005/performance-verification-20261006.md) |
| 1.30.28登录双展厅 | 独立Java6项、双端构建；正式3活动/6课程、白名单/匿名详情401、五尺寸及教师/学生登录通过，异常/HTTP500为0；教研员正式凭据不匹配未验。 | [正式验收](login-showrooms-20261005/release-verification.md)、[本机验收](login-showrooms-20261005/verification.md) |

- 上述是各轮实际验证，1.30.30登录性能部署为本轮新实测。正式验收临时学校/账号已停用，测试关联/指派已清理，专用作品和基准保留审计。
- 新目标环境须分别前检、备份、执行 `sql/classroom_experience_{flowchart_history,attachment_kinds,feedback,seat_helper}_v1.sql` 及 `sql/operation_ai_multi_provider_keys_v1.sql`，保持原AI加密主密钥，再配套发布双端并重启后台。130已在1.30.24迁移，不可据此推定其他库已迁移；助手关闭也不能省略依赖表。
- 发布须隔离无关改动、核对制品和目标库，沿用对应主机发布手册；部署成功自动登记 `RELEASE_LOG.md`，发布更新为 `PUBLISHED` 并以真实教师接口核对。无明确迁移不执行业务SQL。
- 1.30.27回退使用本轮v1初始备份恢复已验收1.30.26，更新106撤DRAFT；不要使用v2备份回到未验收v1，保留新设备/基准，不整库回灌。详情见自动关联验收。
- 1.30.26历史回退证据在助手验收报告：恢复该轮备份的unit/Nginx/env并探活，前端回1.30.25、后台回1.30.24，助手全局关闭，更新105撤为DRAFT；保留新增设备、作品和成绩，不整库回灌覆盖后续写入。

## 6. 当前风险与下一步

1. **118条历史空作品状态仍未治理**（SUBMITTED115/GRADED3）：先逐条核对作品、评分和版本，再设计可回滚修正，不直接批量清分。
2. **真实机房助手仍待验**：学生重新下载直接运行，教师设置机房/机位并确认常用机位；小范围核对Windows10、极域、还原卡、自启和镜像克隆。正式接口通过不等于现场安装验收完成。
3. **其他专题未闭环事项**：三厂商真实请求/评分准确率、旧DATA_LAB缺快照、2026-09-24专项全链复核及旧AI空评分项等，按专题任务和历史证据逐项核对；不能仅凭后续产品版本判定全部解决。
4. 本机历史课程时间缺失、归属/跨校指派冲突的治理边界保留在历史归档及数据模型；不得用迁移当天时间伪造历史。
5. AOCI-CODE **0.1.0-rc17已接入且代码索引建立中**：2026-10-06第十二批19条正式Apply成功，十二批累计237条，工具报告仍有1692项待处理，未aligned。本次压缩恢复读取当时199条/5块、约32161 tokens，交付已确认且Challenge 10/10、严格Attestation通过；这是已交付索引核验，不等于全仓覆盖或治理对齐，不能称完整系统认知可靠。发布登记变化已复核并正常确认指纹，一次重放拒绝经重新预览重试恢复，无未决Recovery。继续按当前Guide逐批读源码创作，证据与边界见 [接入说明](../docs/architecture/AOCI_CODE.md)；本任务不改业务代码、无需SQL或重启。
6. 130登录性能已发布1.30.30，后台提前刷新有效快照，冷查询1.3～1.4秒；数据库故障/调度受阻时仍可能回源。大规模匿名并发、正式教研员凭据及123同项验收待跟进。该发布任务曾在AOCI校验时遇到索引并发变化，记录见性能验收；当前索引状态以上一项为准，仍不声明治理对齐。

## 7. 上下文与开发工具维护

- `AGENTS.md` 管操作，本文管当前事实，架构索引管专题入口；AOCI索引辅助文件职责/关系定位，不替代源码、数据库现场、测试和人工业务决策。
- 重大变更直接更新对应当前段落；验证过程和旧状态写历史文档/专题报告，再从本文链接。不要继续向本文或架构索引逐轮堆叠发布日志。
- 保持本文约150行以内；新长期事实写入对应架构/专题，历史证据按关键词查 [原核心归档](PROJECT_HISTORY_20261005.md)、[旧历史索引](context.md) 和发布日志。
- 凭据只放 `secrets.local.md` 或环境变量，不写文档、索引、日志；涉及业务规则、API、权限、数据库或部署变化时同步相关需求、设计、任务及ADR。
