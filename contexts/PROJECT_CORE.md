# 信息科技学业测评平台：当前核心事实

> 上下文版本 **v4.32**，更新日期 **2026-10-10**。130 前端最新正式 **1.30.42**（release `20261010_login_render_fix_v1`；后端仍为 1.30.40 的 `20261009_slowness_fix_v1`，本次未改后端），机位助手 0.2.1；修复与验收证据见 `output/login-diag-20261010/`。旧过程在 [历史归档](PROJECT_HISTORY_20261005.md)。

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
