# 信息科技学业测评平台：当前核心事实

> 上下文版本 **v5.53（1.30.98 已上线）**，更新日期 **2026-11-13**。正式环境为 **1.30.98**。本轮发布证据见[发布日志](RELEASE_LOG.md)与 `output/release-20261113-extension-monitor-130/`。

## 当前焦点：扩展服务监控 129 硬件探针稳定性修复已随 1.30.98 上线（2026-11-13 09:34）

- **根因**：129 探针经 130→123→129 两跳 SSH，实测 10.8–11.3 秒（130→123 约 0.28s，123→129 约 8.5s）；后端 health 原只等 8 秒、探针超时 12 秒，60 秒缓存过期后的第一轮必然返回 `available=false`/“硬件探针未在 8 秒内返回”，页面闪“129 硬件信息暂缺”；前端首轮无数据时也直接按缺失渲染。
- **改动**：后端改为成功快照优先（有快照立即返回，过期标 `stale` 并后台刷新；首次无快照才同步探测），探针超时 12s→18s、health 等待 8s→21s、单飞、失败保留旧数据并 15 秒退避；前端区分“正在探测/确认暂缺”，过期数据标“后台刷新中”。
- **发布事实**：`releases/20261113_extension_monitor_v1`，JAR `e5abdc7b`（108,323,266B）、首页 `4547eb60`（825 文件）、MainPID 3321939、NRestarts 0；产品更新 **176/1.30.98/PUBLISHED**，真实教师 681108 可见；备份 `/data/backups/xueyeceping/20261113_093201_20261113_extension_monitor_v1`（整库 55,440,416B，SHA-256 `a5ecef6b…`）；无表结构变更。
- **验证**：后端全量 1114/0/4（无新增用例），前端 build:prod 通过；线上停轮询 65 秒后两次请求 29/33ms 返回 `stale=true`（旧 `fetchedAt` 09:37:23），13 秒后 34ms 返回 `stale=false`、`fetchedAt` 09:39:43；页面连续 90 秒未再出现“暂缺”。
- **发布窗口**：09:32 仍有 1 条 5 分钟内提交（09:28:08，判题/签到/AI 均为 0），按用户“尽快发布、停机尽量 15 秒内”指令以 `--allow-activity` 跳过课堂活动门禁；停机 09:33:49→09:34:13 约 24 秒（Spring Boot 19.5 秒），略超 15 秒目标。
- **回退**：`python scripts/deploy/deploy_extension_monitor_130.py --mode rollback --backup-dir /data/backups/xueyeceping/20261113_093201_20261113_extension_monitor_v1`，回到 1.30.97；更新 176 转 DRAFT；不整库回灌。
- 下一步：无既定在研任务；若后续强要求 15 秒内停机，需评估双实例/预热切换，不属于本轮。

## 同日数据库层优化（2026-11-13 09:19，无代码发布，随 1.30.98 基线保留）

- **已完成**：`biz_student_answer`（67.2 万行）时间索引三列→四列（追加 `score`，INPLACE 在线执行，EXPLAIN 变 range+Using index），脚本 `sql/platform_overview_index_score_20261113.sql`；MySQL `tmp_table_size`/`max_heap_table_size` 16M→64M、`innodb_buffer_pool_size` 128M→2G（SET GLOBAL 在线生效 + `mysqld.cnf` 持久化，原文件备份 `.bak.20261113_091940`）。
- **实测收益**：概览趋势 4875→2656ms（-46%）、学校聚合 2318→1744ms（-25%），均低于诊断 3 秒阈值；MySQL 与应用零重启。
- **备份**：`/data/backups/xueyeceping/20261113_091330_overview_index_score_v1/database.sql.gz`（55,435,764B，SHA-256 `88d76b24…bdd01`）。**回退**：索引按 SQL 末尾注释 ALTER 回三列；参数恢复 16777216/16777216/134217728 并删除 `mysqld.cnf` 追加段。
- 下一步：观察 1-2 天诊断中心 slow_sql 是否回落到正常水位；11-12 的 6 条业务异常（学生管理/课程设计与指派）仍待按服务器错误日志定位。

## 正式基线：1.30.98（发布后已线上核对）

- 130 为 Linux/systemd/Nginx 正式主站；前后端均为 `releases/20261113_extension_monitor_v1`，JAR SHA-256 `e5abdc7b…`，首页 `4547eb60…`，MainPID 3321939、NRestarts 0。产品更新 176/PUBLISHED 真实教师可见。1.30.98 回退：`python scripts/deploy/deploy_extension_monitor_130.py --mode rollback --backup-dir /data/backups/xueyeceping/20261113_093201_20261113_extension_monitor_v1`。
- 2026-11-13 起数据库层较 1.30.97 发布时有两项后续变更（`biz_student_answer` 时间索引含 `score` 四列；MySQL 内存参数已调优并持久化），代码已为 1.30.98。
- 上一版 1.30.97（`20261112_diagnosis_gov_v1`）的诊断中心、概览 SWR、复制降级修复，1.30.96 的 22 项修复，1.30.95 打字引号归一化，1.30.94 Python 交互运行均为历史基线，证据见[发布日志](RELEASE_LOG.md)。教材面板、电子教材16册1840页、课例327/资源1050保持。

## 稳定业务边界

- 后端 `RuoYi-Vue/` 为Java8/RuoYi/Spring Boot/MyBatis；唯一维护前端 `RuoYi-Vue3/` 为Vue3/Vite/Element Plus；不改旧Vue2。
- 服务端权限与数据范围是最终边界。历史学生可能没有sys_role学生角色，学生入口按 `studentSs.isStudent()` 含biz_student档案兜底，不能退回严格角色判断。
- 班级推进后旧课学生写入立即拒绝；教师退回和学生重交只限当前课，旧作品和成绩仍可查看批改。
- 文件作品已有0分或非零分可自主重交；暂存、取消、失败保留旧作旧分，成功同事务归档旧版并转待批。流程图有正式分仍须退回；AI建议须教师采用才写正式成绩。
- Python正式判题事实取 `SUBMIT`、非空score、已知终态，排序 `score DESC, judged_at DESC, submission_id DESC`；代码、分数和用例指标同源。同分采用最新代码。
- Python学生提交保存系统参考分，课程答案score保持NULL待教师确认；已评分代码版本不随重交静默替换。无答案行可事务内补建再加锁；人工/批量评分复核页面快照、当前最高分及退回状态。
- 答案/版本指针、成绩与课堂状态需事务一致；推送提交后发，REST校准。在线Presence与提交/评分是不同事实；独立Python练习不写课程答案、成绩或推进。
- 21天操作题期限按当前有效操作提交的学生去重达半班起算，历史期限不自动重算；本轮不改变期限业务口径。
- 普通课已批操作、打字和理论可计已知分；NULL待批不冒充0，未提交排除均分但保留全体排名，展示与导出同口径。
- 拓展班保留行政班、每学生最多一个活跃拓展班，课时/题单冻结名单不因移出缩历史；负责人限teacher或当前校admin，常规入口拒绝非regular/legacy课。
- 教材页图限教师侧或当前课绑定学生，教师侧资源/课标/PDF学生不可读；原图和课例不放无鉴权profile。HTML课例独立CSP沙箱无allow-same-origin。
- TXT/HTML≤2MiB、显式允许后上传；压缩包只下载不解压，不进入Office或AI图片评分。作品本人访问仍需归属验证。
- 课例评价含teacher即可写（兼researcher允许），纯管理员/教研员/学生不写；理由null保留、空串清空、撤票清空，身份不随匿名理由返回。
- 教师AI Key按教师/厂商加密，任务冻结厂商模型价格；未知价格不能按0估价，接口通过不代表真实评分准确。

## 本机与正式运维

- 默认本机后端8080、前端80；启动必须显式 `-Dserver.port=8080`，教材需 `TEXTBOOK_ROOT=output/textbook-pack-20261029/pages`。启动入口优先复用scripts。
- 本机库 `xueyeceping_server_20260729`；监管/免抽测本机基线不能代表130具备相同表列。不得因环境缺表擅自突破本轮无DDL边界。
- 指定Maven测试加 `-DforkCount=0 -Dsurefire.failIfNoSpecifiedTests=false`；配置/资源改动打包使用clean。当前工具Git Bash已实际验证可用，AGENTS旧Bash不可用记录不套用当前环境。
- 130 API3009、站点80，经123跳板；123为旧Windows/NSSM栈，禁止混用发布命令。凭据只在secrets.local或环境，禁止日志/截图/文档/提交泄露。
- 130外置配置依赖 `20261008_seat_install_v1/config`，新release工作目录须可写logs/MQTT；不删除该依赖release，不删旧release和备份。
- `shared/textbook/cases`仍依赖 `20261107_teaching_resources_v1/resources/cases`，不得替换/删除；私有题图目录 `/data/upload-question-images`须保持鉴权和持久化。
- 备份必须完整并校验SHA-256，新release独立落位；基线漂移停止核查，失败立即回滚。成功需RELEASE_LOG登记、产品更新PUBLISHED、真实教师接口可见。
- 本地Git提交只暂存相关文件，不 `git add -A`，不push；本轮提交保留用户指定Co-Authored-By尾注。

## 未闭环风险与待决策

1. **21天期限与纯Python提交**：未建答案行导致补批中途锁定、监管/免抽测/首页漏计纯判题提交，属于期限计算业务规则，待用户/教研决策，本轮不改。
2. **学生转班后的期限归属**：按提交时班级还是当前班级，待决策，本轮不改。
3. 真实机房45/50人Judge0并发、旧浏览器/低配设备、Safari/Firefox/iOS仍缺完整运行证据；提交班级桶180秒TTL在超长判题可能短暂低估并发。F09取消后模型抛异常的失败事件仍记 FAILED（数据库结果已丢弃，仅事件文案未区分）；F12提交刚入库即被取消的极端路径仍可能把课堂任务标成已提交。以上均非阻断，未在真实环境复现。
4. 保留既定边界：lesson_scope NULL、提交锁认主、8许可容量、刷题无限流、理论重做原子性；AI暂停取消PROCESSING扩展、导学单、免抽测唯一键、清理孤儿、物联/DataLab权限不顺手修改。
5. 跨校教师首页第二学校缺课需权限专题；账号651666跨校监控、旧50班映射与冻结、机位同传和真实250机验证仍待补。
6. 固定A4截底、Linux字体分页、历史空作品和旧数据/课程时间归属冲突未治理，不用当前日期伪造历史时间。
7. Excel图片专用列v3本机在研曾有16项专项证据，但未单独正式发布；真实WPS/Excel不同版本、整班导入、TEMP七天清理仍待验。
8. 其他既有专题、精确回滚点和未验边界见[修复前完整归档](PROJECT_HISTORY_20261111_before_13096.md)，不能因本轮修复或文档精简改写历史结论。

## 接管入口

按 `AGENTS.md` → 本文 → [架构索引](../docs/architecture/INDEX.md) → 任务相关专题读取。历史 `context.md`只按需关键词查证；AOCI已卸载，不恢复。源码及现场可复核证据优先。文档只写已验证事实，未实测明确标注；本页保持约150行，详细测试、发布、哈希放专题和发布日志。
