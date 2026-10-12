# AI 发布登记单（Agent Release Log）

## 2026-10-10 · 1.30.42 · 登录页视觉渲染降级与云机房防卡死优化

130 前端 release `20261010_login_render_fix_v1` 已发布（**仅切前端，未重启后端，零停机**；发布时间 130 本地 2026-10-10 17:43）。针对实验小学等云机房（无独立显卡，依赖 CPU 软件渲染）由于大面积实时模糊滤镜造成浏览器合成管线阻塞、输入严重卡死的问题，进行纯静态视觉降级。

- **视觉降级（本次核心）**：
  1. 移除 `login.vue` 模板中 3 个全屏动态高模糊光晕 DOM（`blob-1/2/3`），CSS `.login-bg-mesh` 设为 `display: none !important;`，彻底剔除 `filter: blur(80px)` 实时计算；
  2. 移除 4 个大模块卡片 `.module-card` 的 `backdrop-filter: blur(16px)` 和 `-webkit-backdrop-filter: blur(16px)`（毛玻璃磨砂效果），卡片背景统一改为纯白 `#ffffff`，保留精致浅蓝边框与微投影；
  3. 彻底释放浏览器合成管线与 GPU/CPU 渲染负担，保留教研大图轮播、开课看板和全部登录逻辑。
- **验证（本地构建产物 + 线上 130 探活）**：
  - 本地构建 `npm run build:prod` 顺利通过；
  - 线上探活：`index.html` SHA-256 `f0361aaf045d693339fddf7339e44d438b2206079b1564c4dc54a09ad9fd116a`（与本地构建一致）；
  - 线上探活：`captchaImage` 返回 200，三服务 active；
  - 线上登录脚本中 `backdrop-filter:blur` 命中数为 0；
  - 旧入口文件访问依然 200（已通过 `cp -an` 合并旧 static 目录），彻底消除 404 隐患。
- **发布登记**：`biz_platform_update` 记录 update_id=120 / version_no=`1.30.42` 已设为 `PUBLISHED`，真实教师账号通过 `/business/platform-update/list` 接口实测完全可见。
- **备份与回滚**：站点配置备份至 `/data/backups/xueyeceping/20261010_login_render_fix_v1/xueyeceping-130.before`；回滚只需将 Nginx root 切回 `20261009_entry_split_v1/frontend` 并 `nginx -t && systemctl reload nginx`。前端 zip SHA-256 `5f7de34deb3cfcb5c432fbc18a55e6b02df1f93ef53d143e6fa703a628abb0b7`。

## 2026-10-10 · 1.30.41 · 首页入口拆包与表单设计器按需加载

130 前端 release `20261009_entry_split_v1` 已发布（**仅切前端，未重启后端，零停机**；发布时间 130 本地 2026-10-10 00:15）。承接 1.30.40 之后的独立只读诊断（`output/130verify-diag-20261009/`），处理「入口 chunk 过大」这一客户端卡顿主因。

- **入口拆包（本次核心）**：`vform3-builds`（自包含 UMD，未压缩约 1.31 MB / gzip 约 319 KB，另有 147 KB CSS）原先在 `main.js` 静态引入并 `app.use` 全局注册，使登录页首屏也必须下载并执行。现改为：`main.js` 挂载后只登记 app 实例（`setVFormApp`，**不触发加载**）；进入电子导学单等页面时由 `permission.js` 守卫调用 `ensureVForm()` 按需加载并 `await vformReady`，注册完成后才渲染。入口 chunk 3,548,025 B → 1,711,816 B（gzip 957,861 B → **558,025 B，-42%**）；`index.html` 中不再出现 `designer.umd` 预载。
- **UMD 导出解析（关键坑）**：生产构建该 chunk 的导出为 `export{designer_umd$1 as d}`（Rollup 的 CJS interop 命名空间），**`mod.default` 为 undefined**，若按 `mod.default || mod` 取用会让 `app.use` 静默失效、导学单空白。改用按结构逐层查找（是否带 `install` / 是否为函数插件）的 `resolveVFormPlugin()`；浏览器探针实测 `resolvedAs=key:d`、`pluginHasInstall=true`。
- **验证（本地构建产物 + 线上 130 直连，双份一致）**：登录页零 vform3 请求、零 pageerror、输入框可用；`/business/guide-sheet`、`/student/guide-sheet` 进入后才加载 vform3，无「组件未注册」错误；`/teacher-dashboard/index` 正常落地。线上探活：`index.html` SHA-256 `66a862cc303e2774ea4571d837a3fe6c2e065acc7538cb50979275d31bfc0656`（与本地构建一致）、入口 gzip `558,025` B、`Content-Encoding: gzip`、`Cache-Control: immutable`、旧入口 chunk 仍 `200`、三服务 active、captcha 与 `/login` 均 200。
- **兼容处理**：新旧前端 `static` 目录合并（`cp -an`），发布前已打开的页面其旧 chunk 仍可访问，不出现 404。
- **不改后端**：未改动 JAR、数据库结构、接口与权限，全程未重启后端进程。
- **备份与回滚**：站点配置 `/data/backups/xueyeceping/20261009_entry_split_v1/xueyeceping-130.before`（SHA-256 `60b15affe8aa99db577b7311992507f2e8320925a66e56dfb1e6b3eef4669754`）；回滚只需把 Nginx root 切回 `20261009_slowness_fix_v1/frontend` 并 `nginx -t && systemctl reload nginx`，无需回灌数据库。前端 zip SHA-256 `e8c17d8cb897d59d84b07574f927ebbd1d6dac2e67ff474d701c9ee93b774d1f`。
- **同步做的服务器配置修复（非发布制品）**：新增 `/etc/needrestart/conf.d/99-xueyeceping-noauto-restart.conf`，以合并写法把 `xueyeceping-130.service` 的 `override_rc` 置 0，阻止 unattended-upgrades 升级共享库后 needrestart 自动重启该服务——这是 1.30.40 遗留的「06:03:53 服务被 SIGTERM」的真实根因（详见诊断报告 F-01）。
- **顺带确认（1.30.40 遗留项闭环）**：`logrotate` 的 `su root root` 修复**已真实生效**——2026-10-10 00:00 `logrotate.service` 首次 `status=0/SUCCESS`，产生 `xueyeceping-130.stdout.log.1`（1,084,737,028 B）与 `aiot-lamp.stderr.log.1`，新日志文件从 0 重新开始；此前 10-07/08/09 连续三次均 `FAILURE`。
- **待跟进**：① 残留慢 SQL `ResourceAccessMapper.selectStudentAnswerIdByResource`（资源预览鉴权，`student_answer = ? or preview_path = ?` 两列均无索引 → 43.7 万行全表扫，今日 262 次、最大 1,608 ms）需索引类改造，前轮已试并回滚，本次未动；`StudentProfileMapper` 4 条仍用全局派生表。② needrestart 排除配置已加载生效（`needrestart -m u -b` 中该服务计数为 0），但当前 java 进程未持「已删除」库文件、触发条件不成立，**须在下次库升级时复核**。③ 导学单设计器默认进入「新手向导」，`<v-form-designer>` 仅在高级模式渲染，本次以「chunk 按需加载 + 插件解析探针」作为组件可用性证据，未做高级模式实操。④ 1.30.40 期间主要慢 SQL 家族（`selectStudentAnswerMatrix` 等 9 处）经核对**已使用按课程限定片段**，诊断中统计的 2,069 条为 13:14 换 JAR 之前的历史数据。

## 2026-10-09 · 1.30.40 · 首页加载性能与运行稳定性优化

130 前后端 release `20261009_slowness_fix_v1` 已发布（Nginx 根零停机切换，后端重启两次）。针对「机房同时开学时首页进入即卡顿、按钮无法点击、输入无响应、页面关不掉」的线上问题，全部改动如下。

- **Nginx 压缩与缓存**：`gzip on` 之外的历史注释项全部启用（`gzip_vary/proxied/comp_level 6/gzip_types`）并启用 `gzip_static`，复用构建产物已有 `.gz`；`/static/` 增加 `Cache-Control: public, max-age=31536000, immutable`。实测入口 JS 3,555,615 B → 957,772 B（-73%），CSS 527,316 B → 71,292 B（-86%）。
- **图片体积**：`logo.png` 由 704,963 B（1254×1254）改为 512×512 WebP `logo.webp` 62,730 B（-91%），6 处引用同步；登录兜底轮播 `slide-1/2/3` 由 831/826/903 KB 改为 WebP 124/118/142 KB（-85%）。
- **登录厅延迟加载（补齐 1.30.39 漏发的前端）**：首图优先渲染，其余 4 张约 15 秒后进入 DOM；浏览器实测轮播项 1 → 5。1.30.39 的 release 目录原先只有 backend/config，无任何 frontend。
- **遮罩兜底**：`permission.js` 为 `route-pending` 增加 10 秒兜底解除，网络异常时不再出现整页按钮永久不可点击。
- **退出拦截**：学生首页 `beforeunload` 只在「确实会丢失的未提交作答」时拦截（已提交/已批阅不再拦截），并在退出前写 `sessionStorage` 本地快照，重进时按课程号恢复未提交内容。
- **MQTT 频繁断线**：130 与 123 复用默认 `dazi-platform-iot` 客户端 ID 互相踢线（约 1.3 秒一轮、日均 33,846 行）。130 shared env 新增 `IOT_MQTT_CLIENT_ID=dazi-platform-iot-130`；123 旧 MQTT 按用户决定停用。重启后 `connectionLost` 计数为 0。
- **日志与轮转**：shared env 新增 `LOG_PATH=/data/logs/app` 并建目录，消除 logback `./logs` 权限导致的启动失败隐患；`/etc/logrotate.d/xueyeceping` 由 `su admin2 admin2` 改为 `su root root`，修复自 09-16 起持续失败、日志涨到 1.06 GB 不轮转的问题。
- **慢 SQL**：批改/课堂类查询普遍内联「全表物化派生表」`SELECT MAX(answer_id) … GROUP BY student_id, lesson_id, question_id`（43.7 万行）。jar 内 `BizStudentAnswerMapper.xml` 新增按课程限定的同名片段，替换 9 处外层已限定 `lesson_id` 的语句（仅 1 个嵌套条目变化，其余 290 个外层条目字节不变）。与 `latestAnswerIdSubquery` 语义等价：真实数据对拍结果完全一致（184 行/194 行、校验和相同），`EXPLAIN ANALYZE` 由 1,578 ms → 17 ms、2,036 ms → 15 ms。切换前后同口径统计：业务慢 SQL 由 11.1 条/分钟降至 2.2 条/分钟。
- **数据清洗**：2 条停滞 3 个半月的 `preview_status='converting'` 脏状态（answer_id 150004、184413）置为 `failed` 并写明原因，保留作品与版本引用；当前无残留 `converting`。
- **验证**：Vite 生产构建通过；旧浏览器兼容性回归对比——`??=`/`||=`/`&&=`/`structuredClone`/`findLast`/`.at(` 等风险语法命中数与现网构建完全一致（0/0/0/0/0/4），未为优化牺牲 Chrome 83/86 与 Win7。Playwright 实测登录页零 pageerror、零 5xx、无横向溢出，账号/密码可正常输入；管理员（105 菜单）、教师（70 菜单）登录并由选校弹窗进入 `teacher-dashboard`。真实教师接口核对更新 118/1.30.40 可见。
- **已知代价（务必记录）**：本次共重启后端 **5 次**（12:41 env 变更；13:14 切换 jar；13:26、13:30 尝试恢复 MQTT 未果；13:38 修正目录属主后成功）。今日 5xx 统计：06 时 20 次（历史遗留）、12 时 128 次、13 时 8,273 次，全部集中在重启的那一分钟，均为轮询客户端重试；**13:39 起状态码恢复正常**，未观察到数据丢失。教训：课堂高峰在线连接可达 576，重启 21 秒即造成该分钟约 2,300 次 502，**后端重启必须避开课堂时段**。
- **事故与修复（13:14–13:38）**：切换 jar 后 MQTT 一直连接失败（`MqttPersistenceException` at `MqttDefaultFilePersistence.open`）。根因是**新 release 的 `backend` 目录由 sudo 创建、属主为 root**，而服务以 admin2 运行，Paho 无法在工作目录写持久化文件；`chown -R admin2:admin2` 后 13:38:15 立即显示 `物联网 MQTT 接收器已连接`，`connectionLost` 为 0、`java→1883` 连接数为 1。同一类问题正是历史 logback `./logs` 权限启动失败的成因，**今后任何新 release 目录在切换 unit 前必须先 chown admin2:admin2**。
- **备份与回滚**：整库 `/data/backups/xueyeceping/20261009_slowness_fix_v1/database.sql` 362,841,290 bytes，SHA-256 `3086521a0eb1f9aab487193d5349737da5643a4e53be1b2eeed2bc3f7d16ef82`；同目录 `nginx.conf.before`(`b659a333…`)、`nginx-site.before`(`102e94ad…`)、`logrotate.before`(`8fbd96b7…`)、`env.before`(`36530188…`)、`service.before.newbackend`。回滚：恢复以上配置 → `daemon-reload` + `restart xueyeceping-130` + `nginx -t && reload nginx`；慢 SQL 与数据清洗均为可精确回退的 SQL（见 `sql/biz_student_answer_latest_index_20261009.sql` 与本文），不需整库回灌。
- **未解决/待跟进**：① 06:03:53 服务被 SIGTERM 停止的发起者仍未查明（无 JVM 崩溃、无 OOM、无 SSH/sudo、无 cron/timer，与 unattended-upgrades 时段重合）；② 入口 chunk 3.55 MB 中的 `vform3-builds`（仅 UMD default 导出、ref 语义敏感）未做懒加载拆分，列入后续专项；③ `StudentProfileMapper` 4 条与 `BizStudentAnswerMapper` 的 `selectScoreSummaryByStudents`、`selectRecoverablePracticalAnswersForRetry` 仍用全局派生表；④ 315 条 `preview_status='failed'` 历史积压未清理；⑤ 学生首页本地草稿快照为静态验证，未用真实学生账号做浏览器端到端验证。

## 2026-10-08 · 1.30.37 · 课堂机位显示与助手安装体验优化

130 release `20261008_seat_install_v1`已发布。课堂优先内网IP、姓名分行、隐藏自动电脑编号；助手0.2.1增加D盘安装结果、自启独立核验及还原基线提示。后端JAR不变，仅下载路径更新并重启。客户端30项、Node3项和独立前端构建通过；正式三角色只读接口、教师课堂/学生首页/两尺寸登录页及实际EXE下载主体和运行自检通过。三服务active，更新115 PUBLISHED且教师可见。整库备份339961705 bytes，SHA-256 `1b38f378afc3a0aaf729b2ea319985b6fb04b4ce4e270cfffd428b3e73489018`。无结构迁移；学生机安装/重启还原待现场验收。回滚与完整证据见 [正式验收](seat-install-release-verification-20261008.md)。

## 2026-10-07 · 1.30.36 · 并发操作与页面状态同步修复

130 前后端 release `20261007_bug_race_v1` 已发布。修复区域抽测筛选、答卷和学生明细迟到请求覆盖，成绩查询切换条件后旧状态残留，Python 题单切题、草稿和判题结果错位；整班 PDF 同原件串行、不同原件可并行。隔离候选 Node 7 项、Java 4 项和双端构建通过；正式三服务 active，真实教师/学生/教研员只读接口及桌面/手机登录页通过。整库备份 336221627 bytes，SHA-256 `004200d0125434d0c756f3b910a958d4a5df3517caba14937e4335fccc5fedb1`。更新 114 为 PUBLISHED，真实教师接口可见。正式答卷写入和整班 PDF 并发未压测；详见 [正式验收](bug-race-release-verification-20261007.md)。

## 2026-10-07 · 1.30.35 · 三份 AI 报告复核后的登录、课堂与机位修复

130前后端 release `20261007_bug_review_v1` 已发布。修复登录图片、选校与状态；退回作品重交、空作品成绩人数、流程图版本/冲突；机位轮询、异常提示和重复注册保护；并收紧区域抽测格式、修复7Z下载和HTML预览。Java 180项（178通过、2跳过）、Node32项、Vue3构建、模拟浏览器5项；正式教师/学生只读接口、教研员新凭据登录及角色/路由接口、桌面/手机登录页通过，三服务active。整库备份335681178 bytes，SHA-256 `5c97bfb58775924afb98a911e3f77ec94bb2aa7c5ea76c7eed44f4b28b7107c6`。更新113为PUBLISHED，真实教师接口可见。正式作品/设备写入和实体机房未验；详见[正式验收](bug-review-release-verification-20261007.md)。

## 2026-10-07 · 1.30.34 · 登录页流式自适应、开课监管看板与教研预览位置优化及Logo放大升级

130正式环境前端全面部署上线。
- **开课监管看板与教研预览位置对换**：将「区域学校开课动态与监管看板」移至左下，享有 60% 充裕的横向宽屏空间，7 列表格数据舒展展示、无需横向滚动条；将「最新教研活动预览」移至右下，与右上登录卡片垂直对齐，彻底消除右下角局促偏长、左下角空旷的失衡问题。
- **Flex 流式布局与视口自适应**：容器最大宽度扩增至 `min(1560px, 94vw)`，行高采用视口弹性伸缩 `minmax(330px, 39vh)`，内部全 Flex 自适应撑满，焦点大图轮播与监管表格随视口高度自然展开。
- **Logo 尺寸放大与视觉层次优化**：顶部导航 Logo 放大至 58px×58px，主标题升至 24px，副标题升至 12px，品牌视觉层次与辨识度显著提升；浏览器 Tab 栏 Favicon 引入追加版本防缓存机制，确保即刻呈现高清帆船图标。
- **验证与凭证**：前端 Vite 构建退出码 0，打包生成 `frontend_flow_v1.tar.gz`（SHA-256 `89d5f611d8f5251621d33bb7327ed92edeec0bacdb44c1ee7ad8d13332265d38`），远程覆盖备份至 `frontend.before_flow_v1` 并 reload Nginx。远程 index.html 哈希（`3f565a1be5c0fe3d78f4d5edf94c86ba7bbdd7f0832071e176c3d08e6e3f10f1`）与本地完全一致；`favicon.ico`（200）、`captchaImage`（200）探活通过。Playwright 1440×900 实景截图留存。平台更新记录 update_id=112 标记为 PUBLISHED，并通过真实教师账号（19157727791）在 `/business/platform-update/list` 接口中实测可见。

## 2026-10-07 · 1.30.33 · 全站统一全新高清Logo与Favicon焕新升级

130正式环境前端全面部署上线。
- **全站深度统一全新Logo**：将全平台统一替换为用户指定课题申报专属徽标（帆船海浪造型的高清透明背景矢量级 PNG，分辨率 1254×1254），涵盖登录页顶部导航、管理与教师后台侧边栏、学生端三大核心模块（日常课程题目、电子导学单、区县测评）等所有 Logo 展现位。
- **浏览器 Favicon 同步升级**：使用专业图像算法将全新 Logo 生成包含 16×16、32×32、48×48、64×64 四层级高清 ICO 文件（`public/favicon.ico`），彻底替代旧版 Favicon 图标。
- **登录页视觉微调优化**：登录页 Header 处的 Logo 去除生硬的方形阴影与边框，采用与透明图形完美契合的 `filter: drop-shadow(0 4px 10px rgba(22, 119, 255, 0.22))` 柔和微光发散滤镜，与整体温润轻拟物背景浑然一体。
- **验证与凭证**：前端 Vite 生产环境构建退出码 0，远程解压部署覆盖并 reload Nginx，远程 index.html 哈希（`38f5b165bd485b432374d07489d6e8397e90ff635bfddab2d99a5fd51d9aa938`）、`favicon.ico`（200）、`logo-Q7XUGtGO.png`（200）探活通过。Playwright 登录实景截图与教师端侧边栏截图留证完备。平台更新记录 update_id=111 标记为 PUBLISHED，并通过真实教师账号（19157727791）在 `/business/platform-update/list` 接口中实测可见。

## 2026-10-07 · 1.30.32 · 登录页面网格对齐、真实课堂大图展示与密码输入优化

130双端release `20261007_login_align_v1` 正式上线。
- **四个模块严格等高对齐**：采用 2×2 扁平 CSS Grid 架构，行高设置为 350px 与 320px，实现左上教研焦点大卡片与右上登录框、左下教研预览与右下开课监管看板在水平基准线与垂直边线上严丝合缝齐平，登录框宽度填满右列栅格，告别不对齐错位。
- **活动焦点图展示真实大图**：后端新增 `/business/login-preview/focus-slides` 与 `/post-image` 安全流式接口，自动从 `biz_research_post` 提取最新包含实拍照片的互动纪实，轮播象山二中潘桑柔老师上课现场高清实拍大图（黑板书写《2.2 算法的控制结构》），图文结合展现课堂实况，并包含加载异常自动降级保护。
- **去除记住密码**：彻底删除“记住密码”复选框并清理历史残留密码 Cookie，提升系统合规性与安全性。
- **方案一落地（密码体验优化）**：密码框配置 `show-password` 明密文切换、实时过滤中文字符防误输入、大写锁定（Caps Lock）状态轻量侦测提示，完全不影响正常英文字符与账号密码输入。
- **验证与凭证**：全量单元测试 12 项全绿，Vite 与 Maven 打包通过，整库热备份大小 332,129,129 bytes（SHA-256 `608c6cc32504bc2f675b31a1984e61eaeeab8a54213e90c4bb1a42b82bf11b51`），三服务 active，HTTP 200 探活通过。平台更新记录 update_id=110 标记为 PUBLISHED，并通过真实教师账号（19157727791）在 `/business/platform-update/list` 接口中实测可见。

## 2026-10-06 · 1.30.31 · 登录页面全新四模块升级与统一Logo


130双端release 20261006_login_redesign_v1 正式上线。
- 登录页结构全面重构为四模块（2x2左右双栏）布局：左上角独立Header设置统一Logo与优雅偏距，彻底删除原深色板块与旧文案；左上最新教研活动5图焦点图走马灯轮播，左下教研活动预览列表，右上用户登录框靠右放置，右下区域学校开课动态与监管看板。
- 背景配色全面改为温润柔和的天霁晨曦浅蓝微渐变，卡片采用晶莹白磨砂微阴影；全平台统一替换为高清Logo占位图。
- 后端活动查询扩容为5项并支持首图提取与流式代理输出，单测11项通过，前端Vite打包通过。
- 完整备份SHA-256 9e9038ed7faae3e81ad691c0ec941ad1ca2d7d900e26b4f98fcdbcf098176a5d，无表结构迁移，产品更新记录update_id=109为PUBLISHED且真实教师更新列表可见，双服务active，探活200。

## 2026-10-06 · 1.30.30 · 登录学校动态加载提速

130后台`20261006_login_preview_fast_v1`上线，前端保持1.30.28；仅查询聚合、独立有效快照及后台预热/提前刷新。Java11项/隔离clean package通过，旧新SQL同快照6行一致，冷查询1.3～1.4秒，17次跨80秒最大49ms、12并发最大29ms；白名单及详情401保持，页面异常/HTTP500为0。完整备份SHA-256 `9f9f1c0060db6cb43241872051bea701a595898b307c061d40aa3b54d66c83bd`；无结构迁移，只产品登记SQL。更新108 PUBLISHED且真实教师可见。Nginx/首页/env保持，回退只恢复后台1.30.28，不回灌数据库。见[性能正式验收](login-showrooms-20261005/performance-verification-20261006.md)。

## 2026-10-05 · 1.30.29 · 登录页教研活动展示升级、课堂监控大屏卡片精简与双端机位常驻优化

123 `20261005_ui_redesign_v1` 前后端正式上线。登录页左侧引入 Element Plus 走马灯轮播 5 张真实教研活动风采图，右侧登录表单重构为深色微磨砂质感；课堂监控大屏学生卡片大幅轻量化，4 项成绩微指标与 3 个快捷操作收纳至 Hover 浮层，学生备注与请假常驻展示；双端（学生端与教师大屏）统一常驻显示机房、机位与内网 IP，学生离线后保留最近已知机位记忆；大屏顶部操作栏去除 AI 常识小字并收纳控制菜单。前后端编译构建通过，门禁检查通过，双服务（3009/3010）探活 200。整库备份路径 `D:\program\3009dazipingtai\backups\20261005_190416_deploy_20261005_ui_redesign_v1`。平台更新记录 update_id=92 为 PUBLISHED，真实教师账号已验证列表第一项可见。

## 2026-10-05 · 1.30.28 · 全景登录页与教研开课概览

130 `20261005_login_showrooms_v1`双端已上线，以1.30.27隔离源码只加入7个登录专题文件。主框水平居中、精简登录、原三图与素养标签保留，匿名真实3活动/6开课摘要，详情继续鉴权。Java6项、双端构建、正式白名单/详情401、五尺寸及教师/学生登录通过，异常/HTTP500为0；教研员正式凭据不匹配未验。完整备份SHA-256 `1bec3cab2e18adb41ef73ff711c9475b51ba7f4229c09d7b0e7b0c6e3489429e`。更新107 PUBLISHED且真实教师可见，仅产品登记SQL、无结构迁移；共享env及助手0.2.0保持。冷查询约5.2秒、缓存14毫秒；回退1.30.27不整库回灌。见 [正式验收](login-showrooms-20261005/release-verification.md)。

## 2026-10-05 · 1.30.27 · 机位助手直接运行与自动关联

130 `20261005_seat_helper_auto_v2`已上线，客户端0.2.0学生直接运行、自动IP/网页登录关联，教师命名和显式确认，重复复用和撤销保护。Java708（3跳过）、客户端18、Node1及构建通过；正式API4组/浏览器7组、98秒Python续期和无助手提交通过，页面异常/HTTP500为0；现有教师42/42保留。完整备份、临时学校/账号停用、更新106 PUBLISHED且真实教师list可见。无结构迁移，工作区登录展厅排除；机房自启/还原待现场。回退用v1初始备份恢复1.30.26并撤更新，不整库回灌。详见 [正式验收](classroom-experience-20261002/seat-helper-auto-verification-20261005.md)。

## 2026-10-03 · 1.30.26 · 机位助手正式启用与内网HTTP支持

- 130前后端release `20261003_seat_helper_http_v1` 已上线，全局及61校开启，客户端0.1.2下载路径配置；无HTTPS也可使用，未安装、离线或撤销不阻断正常上课。1.30.25课堂成绩人数修复保留。
- Java3/客户端15/Node1及三端构建，正式API7组、浏览器6组、实际EXE降级及现有教师检查通过。超过98秒Python续期正常，无助手正式提交成功，pageerror/HTTP500为0；临时验收停用，机房分发仍由管理员逐机完成。
- 完整备份SHA-256 `9ed2bbdeac148985b54e85835df3f812f73f5273a5c5f7d4d8071c0d7e772f84`，无结构迁移；仅助手启用及更新SQL。更新105 PUBLISHED且真实教师list可见。回退恢复旧unit/Nginx/env并撤更新，无整库回灌；详见 [助手正式验收](classroom-experience-20261002/seat-helper-release-verification-20261003.md)。

## 2026-10-03：1.30.25

130前端 `20261003_classroom_score_count_v1` 上线：教师首页“课堂”班级弹窗与成绩查询显示相同成绩人数和配色，批改提交与期限提示保留。真实教师课程629/12班42/42有成绩，两个入口一致、课堂跳转正常，页面异常/HTTP500为0；更新104 PUBLISHED，真实教师list可见。后台继续1.30.24且未重启，无业务迁移、未修改真实学生成绩/作品。整库备份313974965 bytes，SHA-256 `26c5d6fe242de018ecba5fe44d529cf8cdc6b386b58050d182aca42da40e83eb`；回退仅恢复本次nginx.before并reload，不回灌数据库。制品/验收证据 `output/class-selection-release-20261003/`，详见DEPLOYMENT_RUNBOOK和PROJECT_CORE v4.04。

## 2026-10-03：1.30.24

130 `20261003_classroom_experience_repair_v1` 前后端上线：未批作品更新/0分锁定/退回保分重交清分、新格式人工批改、本人反馈、可选机位助手与R1～R12修复。备份后执行四份课堂SQL并补齐厂商密钥依赖，5份原密文和主密钥保持；Java676（3跳过）、Node26、正式API13组/浏览器9组通过，页面异常及HTTP500为0。更新103 PUBLISHED，现有真实教师接口可见。备份SHA-256 `e9c3e1b809cf20d8126c59fc261dd2decd820af91e20f184f7b3038a0ad3d247`。验收写入只在专用学校，验后停用并清除指派；118条历史状态未批量治理，正式助手关闭、机房待验。回退1.30.23保留新增数据，见 [正式验收](classroom-experience-20261002/release-verification-20261003.md) 和DEPLOYMENT_RUNBOOK。

## 2026-09-22：1.30.23

130全量`20260921_full_datalab_classroom_v1`已上线：DATA_LAB、连续加分、题单并发与历史快照、协作/流程图六项修复及导学单图片，保留1.30.22监控。完整备份后迁移data_lab_v1/v2；Pyodide本站化并修正Nginx mjs类型。Java654通过/2跳过、前端构建、隔离师生与并发、正式教师页面及线上Excel/中文图/死循环恢复通过。更新102为PUBLISHED，真实教师接口可见；未改正式学生成绩或作品。备份SHA-256 `46068ca2a4fc7fb3c08790e86f6a9785a206d8c7d9d43dcbdf8a39fcf745a7c9`，回退1.30.22保留新增数据，详见DEPLOYMENT_RUNBOOK及PROJECT_CORE v3.96。

## 2026-09-21：1.30.21

130前后端20260921_share_grading_v1已上线：分享链接加密回显、提交并下一位刷新修复，保留AI失败显示优化。备份后完成密文列迁移；54项后端/9项前端测试和生产构建通过，正式页面模拟批改及真实教师页面通过，真实成绩写入0。update_id=100，PUBLISHED，真实教师接口可见。分享跨设备真实操作待教研员凭据补验；备份及回滚见DEPLOYMENT_RUNBOOK。


> 规则（AGENTS.md 硬性要求）：**任何正式部署到130（含123历史发布），验收成功后必须登记本表，并自动发布对应平台更新记录（biz_platform_update.status=PUBLISHED）。**
> 验收必须使用真实教师的 `/business/platform-update/list` 确认版本可见；只写草稿不算完成，已授权部署无需再次询问发布记录。仅用户明确要求暂不公开时允许DRAFT。失败/回滚候选不得记为已上线，本机网络维护不生成产品版本。

| 日期 | 版本号 | release 目录 / 描述 | 本次改动摘要 | 平台更新已写入 |
| 2026-10-08 | 1.30.38 | 130前端 `20261008_login_input_latency_v1` | 登录页展厅请求延迟并移至空闲时启动；轮播图片懒加载和异步解码；页面卸载清理待处理工作。线上首页哈希一致，三服务active，验证码200，Chromium输入及错误/溢出检查通过。整库备份343779698 bytes，SHA-256 `2036e6999113a8d7d6e2c6d6af2dcfebeccf62940379c7ab8c175c6999262dd8`。无后端及表结构变更；回滚恢复旧Nginx配置并reload。 | 是（PUBLISHED，update_id=116，真实教师已验） |
| 2026-10-07 | 1.30.32 | 130双端 `20261007_login_align_v1` | 登录页面2×2对称Grid四模块严格等高对齐；活动焦点图直连真实教研实况轮播象山二中潘老师上课现场高清大图；移除记住密码；密码框明密文切换、自动过滤中文防误触及Caps Lock大写提示 | 是（PUBLISHED，update_id=110，真实教师已验） |
| 2026-10-06 | 1.30.31 | 130双端 20261006_login_redesign_v1 | 登录页面全新四模块重构（左上5图走马灯、左下教研预览、右上登录框、右下开课看板）；全平台统一Logo占位图与优雅偏距；柔和浅色天霁晨曦主题；首图安全联动；真实教师更新已发布可见 | 是（PUBLISHED，update_id=109） |

| 2026-10-05 | 1.30.29 | 123前后端 `20261005_ui_redesign_v1` | 登录页教研活动展示升级（走马灯5图轮播、深色磨砂微质感表单）；课堂监控大屏卡片轻量化重构（微指标与快捷操作移入Hover，常驻显示备注与请假）；双端机位与内网IP常驻展示（离线保留记忆）；顶部操作栏精简收纳。JAR与前端哈希已全核验，服务探活200 | 是（PUBLISHED，update_id=92，已验证真实教师可见） |
| 2026-09-21 | 1.30.22 | 130 `20260921_monitor129_v1` | 平台概览菜单与系统诊断中心；扩展服务监控经123跳板访问129硬件探针，展示真实CPU/内存/磁盘/JVM信息；正式库执行幂等菜单SQL并授权教研员。备份SHA-256 `c86ed549…2406cc`，JAR SHA-256 `2bafac2c…752aa3`，130/Nginx探活通过。 | 是（PUBLISHED，待真实教师接口复核） |
| 2026-09-21 | 1.30.20 | 130前后端 `20260921_research_ai_full_v1` | 教研三类帖子统一多附件、Word/PDF预览、分享正文留言；AI部分失败/全失败数量显示与批改页布局修复。Java635项（633通过、2项真实模型测试跳过）、Vue3构建及真实教师上传编辑/预览/匿名拦截验收通过；无业务SQL | 是（PUBLISHED，update_id=99，真实教师接口可见） |
| 2026-09-20部署，09-21验收 | 1.30.19 | 130前后端 `20260920_insight_batch_pdf_v2` | 整班PDF页段确认拆分；学情复用原答、共享试卷一次映射、文本分批、局部隔离、显式参考状态、常驻进度及轮询死锁修复。真实36份完整715秒、0失败、覆盖85.71%，成绩不变；PARTIAL保留局部核对。Java49项含1跳过，72页拆36份、18次读取及桌面/手机证据通过，无业务迁移 | 是（PUBLISHED，update_id=98，岳玲教师接口可见） |
| 2026-09-20 | 1.30.18 | 123前后端 `20260920_ui_fixes_v1` | 课堂监控大屏与成绩查询双向镜像按钮直达；成绩汇总区全班重做理论/打字交互优化；题目开放状态与交互优化；7项后端核心稳定性修复（防补零、对账修复、未批改状态对齐、并发保护）；592项单测全数通过。JAR与前端哈希全验证，双服务探活200 | 是（PUBLISHED，update_id=91，已验证真实教师可见） |
| 2026-09-20 | 1.30.17 | 130前后端 `20260920_redo_export_fixes_v1` | 理论与打字重做全量上线（大屏与成绩表入口、轮次管控、两表迁移）；成绩汇总与导出口径修复（未批改操作题不补零、不拉低均分、明确标记待批改；打字均速加权统计）；题目开放与成绩查询切班竞态防护；390px移动端布局优化。JAR与前端哈希已核验，服务探活200 | 是（PUBLISHED，update_id=97，已验证真实教师可见） |
| 2026-09-19 | 1.30.16 | 130前后端 `20260919_full_insight_fixes_v1` | 全量课堂五项修复及学情V3.3.1；三表/打字权限迁移、学情开关启用、原密钥不变；Java591项含1跳过、前端73项及三角色正式冒烟通过；真实学情1份完成，正式成绩不变 | 是（PUBLISHED，update_id=96，09-20补发布并验证教师可见） |
| 2026-09-18 | 1.30.15 | 130前端 `20260918_classroom_gate_fix_v1`，后端保持1.30.14 | 修复全教师课堂大屏误读顶层开关、定时刷新误显示关闭及旧查询覆盖新保存；郑老师和郑东旭真实账号各3次读取通过；确认正式推进原已双开，无业务迁移、后端未重启 | 是（PUBLISHED，update_id=95，09-20补发布并验证教师可见） |
| 2026-09-17 | 1.30.14 | 130 `20260917_ai_apply_home_fix_v1` | 修复线上旧class跳过待核对建议、0写入误报成功、批改返回教师首页白屏；任务28真实采用1份52分，总分/分项/审计已核验；无业务表迁移 | 是（PUBLISHED，update_id=94，09-20补发布并验证教师可见） |
| 2026-09-17 | 1.30.13 | 130 `20260917_paper_ai_v2` | 纸质PDF本班导入、逐空识别及空白满分拦截、待核对可确认批量采用、AI任务创建修复、原Key切模型、3.8/0902及费用元单位；无业务表迁移，正式成绩未代采用 | 是（PUBLISHED，update_id=93，09-20补发布并验证教师可见） |
| 2026-09-16 | 1.30.12 | 130 `20260916_iot_subscribe_fix_v1` | 修复云控板原主题订阅权限；真实班级原Topic订阅成功，跨班仍拒绝 | 是（PUBLISHED，update_id=92，09-20补发布并验证教师可见） |
| 2026-09-10 | 菜单 | 130 前端 `20260910_teacher_help_menu_v1` + SQL `teacher_tools_help_menu_v1.sql` | 教师工具二级改名为「教师教学优质资源」；帮助中心并入教师工具分组。旧 `/help-center` 仍可用。需重新登录。未重启后端 | 否 |
| 2026-09-10 | 130 全量 | `releases/20260910_full_local_v1`（130） | 本机工作区前后端全量（含今日其他 AI 未提交改动）：统一删除、课程工具、教研通知/公开留言、班级管理、课堂表现分、上传 50MB 等。无 SQL。回滚 `20260910_research_class_v1`。JAR `f79e3917…310f` | 否（未写平台更新弹窗） |
| 2026-09-10 | 130 热修 | `releases/20260910_research_class_v1`（130） | 教研活动修改通知+公开分享留言；班级管理年级括号标注；课堂监控表现分正负分；课件上传按 50MB 生效并给出超限提示。未混入统一删除。无 SQL。回滚 v2。JAR `537eee58…7681` | 否（未写平台更新弹窗） |
| 2026-09-09 | 入口切换 | `nav-3010-20260909`（123 Nginx 3010 静态页） | 停止 123 学业测评后端 3009；3010 改为单按钮导航，点击新开标签到 `http://10.52.1.130`，小字「0909日更新最新版」。旧 release 与 Nginx 备份保留可回滚。未切 `xxkj` 域名 | 否（123 后端已停，非功能版本） |
|（示例）2026-08-22 | 1.25.0 | releases/20260822_xxx_v1 | 学生实验工具上线 | 是 |
| 2026-08-23 | 1.25.0 | releases/20260823_student_tool_v1 | 学生实验工具、题目开放开关、129监控增强、帮助中心推荐环境、版权精简、Agent词典（正式发布+SQL执行） | 是（正式库 PUBLISHED） |
| 2026-08-23 | 1.25.1 | releases/20260823_student_tool_hotfix_v1 | 热修 4 个线上缺陷：课程设计器保存失败（iot_enabled 插入列值不匹配）、成绩查询点课报错（历史课优雅降级）、教师成绩页题目开放开关卡片、学生端静默轮询不打断打字（正式发布，前端+后端） | 是（正式库 PUBLISHED，update_id=48） |
| 2026-08-23 | 1.25.2 | releases/20260823_iot_frontend_v1 | 物联网修复与重构：消息表外键移除+接收异常隔离与断线重连（教师端收不到数据根因修复）、教师端页面重构为小组数据总览大表+小组历史明细、学生端新增本组历史数据列表、学生端样式美化与复制按钮修复（正式发布，前端+后端，无增量SQL——外键删除已直接在正式库执行） | 是（正式库 PUBLISHED，update_id=49） |
| 2026-08-23 | 1.26.0 | releases/20260823_iot_frontend_v1（后端已合入判题扩容）+ 129 `/srv/judge0-python` | Python 判题方案 A：400 份同时提交排队、10 路真实判题；123 执行器 10/10/1000、课程并发 60，129 worker 10/队列 512；50/100/200/400 正式阶梯压测全部通过，验收数据清零 | 是（正式库 PUBLISHED，update_id=50） |
| 2026-08-24 | 1.26.1 | releases/20260824_python_iot_ux_v1 | Python 刷题县级机构降级、OJ 切题与判题状态、课程题型初始开放、首次保存在线协作、物联班级入口与数据弹窗、帮助和监控文案、隐藏若依官网菜单 | 是（正式库已发布，update_id=51） |
| 2026-08-24 | 1.26.2 | releases/20260824_course_designer_python_input_v1 | 课程设计器紧凑布局、导学单与说明精简、固定已选题操作列、协作文件题前置提示、课程 Python stdin 自定义运行 | 是（正式库已发布，update_id=52） |
| 2026-08-26 | 1.26.3 | releases/20260826_operation_ai_usage_archive_v1 | 修复课程旧题答案污染“已批改”统计；删题前事务归档；AI 模型参考价、任务 token/理论费用与百炼错误中文提示 | 是（正式库已发布，update_id=53） |
| 2026-08-27 | 1.26.4 | releases/20260827_162145_1247099_shared_course_sticky_v1 | 教师首页标识共享课程与创建教师，服务端下发设计/删除能力并修正删除校验顺序；学生端顶部导航滚动吸顶 | 是（正式库已发布，update_id=54） |
| 2026-08-29 | 1.26.5 | releases/20260829_course_designer_default_open_v1（前端） | 课程设计器现有“学生开放”理论题、操作题双开关默认开启；不覆盖存量班级状态，不改成绩查询与课程推进 | 是（正式库已发布，update_id=55） |
| 2026-08-31 | 1.27.0 | releases/20260831_084148_c0aa4f8_flowchart_v1 | 画程流程图操作题首期正式上线：教师标准答案/基础图、学生受限编辑与草稿提交、结构检查建议与教师确认；补齐节点单击/拖拽、节点/连线删除、开始/结束独立、输入输出文字与四边中点连接点 | 是（正式库 PUBLISHED，update_id=56） |
| 2026-08-31 | 1.27.1 | 服务器配置批次 `20260831_094758_teacher_tool_gateway_v1` | 教师工具统一改为 D 盘 Nginx 80 端口路径；3006 注册 NSSM 自动服务并配置异常重启；停用 C 盘 OpenResty 与旧 Nginx 计划任务，确保 80/3010/3012 只有 UnifiedNginx 进程树 | 是（正式库 PUBLISHED，update_id=57） |
| 2026-08-31 | 1.27.2 | releases/20260831_primary_iot_v1 | 小学实验板标准 MQTT 正式接入：8 个班级账号与精确 Topic ACL 同步、同步状态与失败重试、教师/学生 Python 代码入口；删除 class_* 宽权限与临时探针账号，完成本班允许/跨班拒绝/订阅拒绝验收 | 是（正式库 PUBLISHED，update_id=58） |
| 2026-08-31 | 1.27.3 | releases/20260831_primary_iot_student_python_v2（仅前端） | 学生物联页面默认打开小学实验板 Python 页签，将一键复制本人小组 Python 代码置于首要入口；初中 Mind+ 页签继续保留，后端和数据库不变；Nginx 精确重启后生效 | 是（正式库 PUBLISHED，update_id=59） |
| 2026-08-31 | 1.27.4 | releases/20260831_primary_iot_python_template_v1（仅前端） | 小学一键复制代码改为 N17 示例格式，补齐 utf_8 文件头、npython 导入说明和 oled.print 屏幕状态显示；MQTT 参数与初中 Mind+ 不变 | 是（正式库 PUBLISHED，update_id=60） |
| 2026-08-31 | 1.27.5 | releases/20260831_student_class_99_v1 | 学生新增、Excel 导入及上传前校验统一允许班级号 01～99，支持 11 班及以上；00、100、601/602 等非法格式继续拦截；前后端发布并完成正式探活 | 是（正式库 PUBLISHED，update_id=61） |
| 2026-09-01 | 1.27.6 | releases/20260901_student_add_validation_hotfix_v4/frontend（仅前端） | 修复单个新增学生时 `10～99` 学号被错误前端正则拦截的问题；后端、数据库结构不变，Nginx 重启后 3009/3010/验证码/静态脚本均 200 | 是（正式库 PUBLISHED，update_id=62） |
| 2026-09-01 | 1.27.7 | releases/20260901_operation_attachment_preview_cache_hotfix_v2（后端） | 修复操作题 Office 命中页图缓存时漏写当前附件 PDF 预览状态；启动对账已有 PDF、无 PDF 重新入队，消除 pending/队列为 0 的状态误报；正式库无结构 SQL | 是（正式库 PUBLISHED，update_id=63） |
| 2026-09-01 | 1.27.8 | releases/20260901_172047_teacher_feedback_governance_v1/frontend_v4（前端；后端沿用 1.27.7 已合入制品） | 修复课堂表现按列导出，课程选题显示出题人，教师课程增删即时反馈，批改未交提示，题库操作列固定和学生导出；学生导入改批量事务、同校锁和结构化耗时，诊断区分业务提示/慢接口/系统异常；正式库新增账号查重联合索引 | 是（正式库 PUBLISHED，update_id=64） |
| 2026-09-01 | 1.28.0 | releases/20260901_scheme2_score_numeric_v1 | 方案二安全处理正式发布：课程归档字段、学生停用/纠错与删除保护；成绩查询及班级/导学单/Python 列表改自然数值排序；后端与 Vue3 前端同步切换 | 是（正式库 PUBLISHED，update_id=65） |
| 2026-09-01 | 1.28.1 | releases/20260901_score_numeric_sort_hotfix_v1（仅前端） | 修复 Element Plus `sort-method` 参数误用导致的学号字典序；成绩主表、课堂表现、排行榜、理论详情改为正确自然数值排序回调，课程成绩列改用 `sort-by` | 是（正式库 PUBLISHED，update_id=66） |
| 2026-09-02 | 1.28.2 | releases/20260902_student_correction_upload_hotfix_v1（仅前端） | 修复学生管理批量纠错上传 Excel 未按 multipart/form-data 发送、后端收到空文件并提示系统繁忙；保留旧前端 release，未修改后端、业务数据或上传大小限制 | 是（正式库 PUBLISHED，update_id=67） |
| 2026-09-02 | 1.28.3 | releases/20260902_student_entry_year_grade_v1（仅前端） | 学生管理按当前校区学部自动在入学年份后显示当前年级：小学 x年级、初中 初x、高中 高x；筛选和保存仍提交纯年份 | 是（正式库 PUBLISHED，update_id=68） |
| 2026-09-02 | 1.28.4 | releases/20260902_research_public_notice_share_v1 | 教研活动 NOTICE 通知支持随机令牌公开分享，匿名查看正文及受控图片；支持 7 天、30 天、永久有效期与撤销；后端+Vue3+增量 SQL | 是（正式库 PUBLISHED，update_id=69） |
| 2026-09-02 | 1.28.5 | releases/20260902_research_public_notice_share_hotfix_v1/frontend（仅前端） | 修复公开分享链接页面空白（匿名接口返回对象直接赋值），并为 HTTP 正式地址增加 `document.execCommand('copy')` 复制降级；后端、数据库结构不变 | 是（正式库 PUBLISHED，update_id=70） |
| 2026-09-02 | 1.28.6 | releases/20260902_flowchart_frontend_restore_v1/frontend（仅前端） | 修复 1.28.5 前端热修覆盖画程流程图资源的问题，恢复题库流程图操作题新增、编辑和学生作答入口；后端、数据库结构和题目数据不变 | 是（正式库 PUBLISHED） |
| 2026-09-02 | 1.29.0 | releases/20260902_135546_full_restore_v1 | 紧急全量恢复：后端+前端同步部署，修复今天多次热修导致线上后端 JAR 缺失 FlowchartController（流程图 404）和 TeacherGradingController（批改 404）；从包含所有最新代码的本地源码重新编译；外置 config/ 从 scheme2_v1 复制，nginx root 已切换；无增量 SQL | 是（正式库 PUBLISHED，update_id=71） |
| 2026-09-04 | 1.29.2 | releases/20260904_codex_perf_release | 上线前审查修复发布：Presence `SCAN` 聚合与退避重连、课堂大屏查询索引/课程内聚合、协作轨迹限流和心跳降写、教师页面后台暂停轮询、物联刷新合并、课程保存防重、流程图安全校验及课程权限边界；执行分组、协作、任务状态和课堂索引四份增量 SQL | 是（正式库 PUBLISHED，update_id=73） |
| 2026-09-05 | 1.29.3 | releases/20260905_034500_answer_integrity_v1 | 修复学生答案 `student_id` 被错误按 `user_id` JOIN 导致的批改/学情/画像归属错配或隐藏；选择/判断题终态提交原子幂等，拒绝覆盖已判分成绩；跨模块路由加载遮罩。无业务结构 SQL | 是（正式库 PUBLISHED，update_id=74） |
| 2026-09-05 | 1.29.4 | releases/20260905_python_keepbest_v1 | 课程编程题与打字题一致保留历史最高分（首次部署后发现协作时间线 SQL 列名错误致 500，已同版修复重发）；批改页双口径加注；设计器指派切换确认；学生端计分说明；协作时间线显示编号+姓名；新建常驻回归验收课 373（未指派）。无增量 SQL | 是（正式库 PUBLISHED，update_id=75） |
| 2026-09-05 | 1.29.5 | releases/20260905_flowchart_ai_v1 | 修复流程图 AI 批改无法发起：就绪判定误要求文件版本，流程图提交以提交版本为锚点；同步修复判题与采用链路的版本比对空指针/误判。无增量 SQL | 是（正式库 PUBLISHED，update_id=76） |
| 2026-09-05 | 1.29.6 | releases/20260905_flowchart_create_fix_v1 | 修复流程图 AI 创建任务 500：任务标记 `FLOWCHART` 纯文本违反 JSON 列约束，改存合法 JSON 并集中判断；线上 362 课实测建任务成功，32 份中 30 成功、2 失败可重试。无增量 SQL | 是（正式库 PUBLISHED，update_id=77） |
| 2026-09-05 | 1.29.7 | releases/20260905_teacher_ux_enhancement_v1/frontend（仅前端） | 教师端五项体验轻量增强：教师首页课堂推进向导化与防误触确认开关；课堂监控大屏常驻题目发令闸；课程设计器分值仪表盘与历史题目分值锁定保护；批改工作台翻页快捷键现代化（Alt+↑/↓ 与 J/K）及防跳滚与常驻指引；成绩查询页仅冻结学号姓名两列，并构建请假/未交/正常/满分/人工修正状态色相体系。无增量 SQL | 是（正式库 PUBLISHED，update_id=78） |
| 2026-09-05 | 1.29.8 | releases/20260905_triage_fixes_v1 | 分诊四项修复：批改页整课口径与当前操作题口径分离加注、未提交标未进入；课堂未指派班级返回历史只读（线上实测 historical:true）；课程设计器缺失引用致空白已修复；协作页三步向导。部署教训：新发布目录必须复制外置 config，否则后端用包内默认配置无法起 3009；nginx 用 restart 服务而非 reload 发信号。后端 448 单测通过，无增量 SQL | 是（正式库 PUBLISHED，update_id=79） |
| 2026-09-05 | 1.30.0 | releases/20260905_collab_overhaul_v1 | 在线协作大改：一课一道操作题前后端校验（316存量2道保留冻结，368/373已精简至1道，有备份）；协作开关独立、起始文件只来自题库（公开/本人）；协作页按学号自动分组冻结；教师房间列表显示组名人数成员与进入按钮；学生单房间自动进入本组。线上实测：加第二道操作题被拦且数据无损，题库材料接口200。后端453单测通过，无增量SQL | 是（正式库PUBLISHED，update_id=80） |
| 2026-09-05 | 1.30.1 | releases/20260905_ux_p0_v1/frontend（仅前端） | P0体验修复：平台概览趋势图纵轴万位数截断改用万单位（线上截图验证1万正常）；批改页数字/五星二选一可切换（偏好localStorage记忆，默认数字），去掉五星辅助评分文案与title悬浮跳动。构建产物确认旧文案消失。后端、数据库不变，无增量SQL | 是（正式库PUBLISHED，update_id=81） |
| 2026-09-05 | 配置优化 | nginx.conf（备份见 backups/20250905_gzip_on）+ scripts/prod-deploy.ps1 | 开 gzip（含 gzip_static 复用构建产物 .gz）：3.5MB 主包降至 0.95MB（-73%），接口 JSON 动态压缩同步生效；新增一键发布脚本（dry-run 默认，config 复制/NSSM 切换/nginx 切换/探活/回滚指引，服务器已试运行通过）。后端、前端制品、数据库均不动 | —（ infra，无产品更新） |
| 2026-09-05 | 1.30.2 | releases/20260905_efficiency_v1 | 教师效率小包：毕业年级默认折叠（真机确认2020级折叠）；学生协作卡区分本组/全班（后端groupRoom标记）；编辑器在线成员可点开展示权限状态。首次由一键脚本独立完成发布（含后端探活）。后端453单测通过，无增量SQL | 是（正式库PUBLISHED，update_id=82） |
| 2026-09-06 | 1.30.3 | releases/20260906_collab_score_unlock_v1 | 课程调分解锁与协作极简重构：设计器去仪表盘、分值彻底解锁（后端删答题拦截，历史成绩不回溯）；协作开关对齐物联样式并联动文件作品多选池；协作页瘦身为单课即时分组（后端快照regroup）；123新增/cryptpad/反代至129:80，服务环境变量改相对路径，编辑器秒开。发布前整库备份backups/20260906_collab_unlock_before（131,424,519字节，SHA-256 9c680c7f…37b5fe），JAR 1de39e8e…、index.html c1599ed3…两端一致；后端453单测通过，无增量SQL | 是（正式库PUBLISHED，update_id=83） |
| 2026-09-06 | 1.30.4 | releases/20260906_probe_fixes_v2（后端；前端沿用 v1 已发制品） | 第三方探针四缺陷修复：打字统计服务端重算+500封顶（课程/县抽测双链路）；流程图学生工作台与提交返回脱敏（教师链不变）；无效提交明确拒绝；缺考显式标记且跳过汇总/排名/导出/前端均分（分母不变）。v1 已验证后追加缺考口径收紧（仅标全班有记录的课）重发 v2。JAR SHA-256 9105f10e…cb8631（线上一致）；后端461单测通过；前端构建通过；无增量SQL。另清理探针伪造满分行 answer_id 314495（有行级备份，SHA-256 7ec686e3…1db414） | 是（正式库PUBLISHED，update_id=84） |
| 2026-09-06 | 1.30.5 | releases/20260906_collab_workspace_v3（后端+前端；前端 v3b 热换 index a07e4202，旧包留 frontend.prev） | 在线协作工作台上线：同页完成选文档/选班级/自动均分/手动调人/组文档分配/保存进入；协作按文档数分组、固定组默认四人并按班跨课共用、两者隔离；开始前调整直接替换、开始后新建轮次保留历史；旧全班设置不再关闭或换钥小组房间；设计器只留入口；固定组自动生成先预览后保存。附带 1.30.3 更正（见平台更新）。发布前整库备份 backups/20260906_collab_workspace_v3/ry-vue.sql（131,772,566 字节，SHA-256 44E1BE92…90081A4）；JAR 6550c403…78759、index.html a07e4202…2800532e3 两端一致；后端 471 单测通过；增量 SQL collaboration_workspace_v2.sql 已执行（快照 round_no、活动请求去重、方案 scheme_scope，旧协作方案标 COLLAB_LEGACY，数据零删）。发布中曾因 NSSM 环境变量误压单行致集成配置短暂失效，已逐项重建并验证 ready，旧 release 与备份保留可回滚 | 是（正式库 PUBLISHED，update_id=85） |
| 2026-09-06 | 1.30.6 | releases/20260906_collab_ux_v1（后端+前端） | 协作UX重构：教师首页课堂按钮弹窗恢复；选题弹窗（年级/学期/课次/关键词+每页10条+预览+真实大小）；班级多选批量建组（单班微调）；编辑器在线成员改右侧抽屉（花名册+实时状态+刷新）；删旧全班房间卡，历史轮次收纳为往期回顾抽屉；新增roster与bank-materials/search接口。发布前整库备份 backups/20260906_collab_ux_v1/ry-vue.sql（131,818,340 字节，SHA-256 6002AA86…B883B8A1）；JAR 03828d47…727734、index.html d89c21bb…3b8730a 两端一致；后端471单测通过；正式库免迁移（resource_path/preview_path列已存在）；环境变量保持20项未动 | 是（正式库PUBLISHED，update_id=86） |
| 2026-09-07 | 1.30.8 | releases/20260907_kb_quickfix_v1（后端+前端） | 课堂表现分加扣分允许不填原因（成绩查询页+课堂监控大屏，后端去掉 /save 与 /batch-save 两处"原因必填"校验）；修复学生登录后被自动跳转到在线协作页、出现 roomId=undefined 接口报错（移除"仅一个房间即自动进入"逻辑，进入房间前对 roomId 判空）。发布前备份 nginx.conf（backups/20260907_kb_quickfix_v1_before/nginx.conf.before），NSSM AppDirectory 原值记录于 nssm-appdir.txt；JAR SHA-256 6CAC481F…A770、index.html 6B627FC5…4E39F 两端一致；3009/3010 探活通过，nginx root 已切新目录；无增量 SQL；旧 release 20260907_flowchart_ai_fix_v1 / 20260907_flowchart_ai_ui_v2 保留可回滚 | 是（正式库PUBLISHED，update_id=88） |
| 2026-09-07 | 1.30.9 | releases/20260907_online_recovery_v1（后端候选+完整前端） | 在线恢复上线：近5分钟活跃账号去重、部门含下级筛选、后端分页、诊断统一口径；登录防重入、502不退出、同浏览器同账号复用会话；保留课堂表现空原因与学生默认首页。JAR `af14241e…34457`、index.html `a4b7eab6…91bba51` 两端一致；NSSM 已切新目录、3010 root 已切新前端，双服务 Running，3009/3010 均 200；16:00:32 重启窗口 502 后 16:01 起 error.log 新增 0；无增量 SQL；旧 release 保留可回滚 | 是（正式库 DRAFT，update_id=89，待管理员审核） |
| 2026-09-07 | 1.30.10 | releases/20260907_blankfix_v1（后端+前端） | 白卷批改与评分展示修复：有答题行的白卷可进入批改；徽章与页眉统一 TRIM 口径；导出恢复请假分支；流程图版本标签按题型；AI 分项非整数严格失败。前后端均已完成制品范围校验、正式整库/Nginx/NSSM 备份、切换与健康检查；JAR `6b5409ac…c756`、index `a432c441…ba51`；无业务结构 SQL；旧 release 保留可回滚 | 是（正式库 DRAFT，update_id=90，待管理员审核） |
| 2026-09-14 | 1.30.11 | releases/20260914_lesson_fixes_v1（后端+前端） | 修复普通课程随机抽题按实际题量校验100分；自动/手动推进下一课默认开放理论题与操作题；学生端滚动条和打字按钮布局调整。后端 PID 174367，前端与后端均已切换；无业务 SQL；整库备份见 `backups/20260914_lesson_fixes_v1`，平台更新已登记 PUBLISHED。 | 是（正式库 PUBLISHED） |
| 2026-09-16 | 1.30.12 | `releases/20260914_lesson_fixes_v1` 就地换后端 JAR（仅新增 2 类）+ 130 nginx 新增 `/aiot-lamp/`；另部署独立服务 `releases/20260916_aiot_lamp_v1`（不属本平台制品） | AIoT 语音控制灯课堂工具接入与课堂接口补丁：① 把 `AiotClassroomController`/`AiotClassroomService` 两个类补回线上 JAR，修复 `/prod-api/aiot-classroom/*` 长期 404（补丁 base f42701b8…fed8eb 与线上一致，新 JAR b5efdf26…25d9eb）；② nginx 80 站点新增 `/aiot-lamp/` 反代到独立 systemd 服务 `aiot-lamp`（127.0.0.1:3014，用户 admin2，数据在 `/data/apps/aiot-lamp/shared`）。无业务 SQL、无表结构变更。备份 `backups/xueyeceping/20260916_aiot_context/ruoyi-admin.jar.bak`、`backups/nginx/20260916_aiot_lamp/xueyeceping-130.conf.bak`；重启 `xueyeceping-130` 后 502→200，`/prod-api/captchaImage` 200、平台首页 200、`/aiot-lamp/` 200。回滚：拷回 jar 重启后端 + 拷回 nginx conf reload。 | 否（未写平台更新弹窗） |

| 2026-09-16 | 1.30.12 | 130 releases/20260916_iot_subscribe_fix_v1 | 原data主题订阅权限修复，移除平台自动判定/关键词兜底及指定命令界面；真实班级账号128→0，跨班仍拒绝；真机待复验 | 是（DRAFT，update_id=92） |
