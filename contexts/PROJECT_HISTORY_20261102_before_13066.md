> 历史归档：2026-11-02，正式1.30.66发布前的v4.88完整原文。旧“待发布/最新版本”仅代表当时状态，当前以PROJECT_CORE.md为准。

# 信息科技学业测评平台：当前核心事实

> 上下文版本 **v4.88**，更新日期 **2026-11-02**。130正式 **1.30.65**：后端 `20261102_extension_dual_role_v1/backend`（JAR `4bbf46e0`，内嵌 business `ae22d297`，仅权限热更），前端 `20261102_extension_textbook_v1/frontend`（首页 `3e6ea828`），机位助手 0.2.6（默认 C 盘）。本轮上线拓展课双空间、课程教材绑定、PDF 修复与独立审核后的 P0/P1/P2 修复（更新 **142 / 1.30.64**），随后按用户决定放行双角色教师管理拓展班（更新 **143 / 1.30.65**）；两条更新均 PUBLISHED 且教师可见。具体证据见 [RELEASE_LOG](RELEASE_LOG.md)。本地课堂推进、题库切页仍未发布；精简前原文见[历史归档](PROJECT_HISTORY_20261015.md)。

## 本机待发布（2026-11-02）：课堂 Python 交互终端

- 用户确认浏览器运行、原后台判题路线。仅课堂 `StudentProgrammingQuestion` 改为“运行→input提示→输入/回车→回显结果”，删除重复说明、公开样例和预填stdin；保留交作业、自动草稿及折叠历史，独立题单与DATA_LAB不变。
- 复用本站Pyodide 0.26.4 + 独占Worker + JSPI/run_sync，不改写学生代码；支持函数/循环内多次input、停止/重建、加载180秒/执行10秒/等待输入5分钟超时及UTF-8输出64KB保护。修复原草稿pendingDraft误用及失败高频重试，旧题请求不覆盖新题。
- 本机验证：运行控制6/6、真实Pyodide10/10、桌面输入8/手机390px输入3、停止恢复、模拟接口提交/草稿刷新找回、Vue3生产构建通过。模拟提交不代表真实Judge0或成绩写库；正式学生只读查看，未代交作业。见[完整验收](python-interactive-20261102/tasks.md)与[ADR-016](python-judge0/adr/ADR-016-course-browser-interactive-run.md)。
- 未发布130，无SQL/新依赖/后端接口/环境变更，无需后端重启；后续只发布隔离前端并核对已有pyodide资产，回退前端即可。内置Chromium浏览器实测且支持JSPI，学校实际浏览器、低配整班压力待验；正式stdout比较仍包含input提示，运行成功不等于判题通过。

## 本机修复待发布（2026-11-02）：跨校同届同班号课堂监控消歧

- **线上反馈**：文峰、弘正两校都有 701/702，教师 651666 从教师首页课程卡进入课堂监控（lessonId=990774）报「该年级班号在多个学校存在」且学生列表为空。根因：入口只带 entryYear/classCode，后端 `resolveManagedClassDept` 的非 admin 分支忽略 token 当前学校，按 (教师, 年份, 班号) 查到两个 dept 即抛错；`desktopOverview` 先消歧后查课程，未利用课程所属学校。
- **修复**：`ClassGroupingService.resolveManagedClassDept` 在多校时，若当前学校在任课学校集合内则采用当前学校；不在集合仍抛原错误；单校仍以任课学校为准，admin 不变。Controller、前端、库表均未改，课堂监控/overview/分班/座位布局六个接口同时生效。切换学校会重签 token 并作废旧 token，当前学校可靠。
- **验证（本机）**：`mvn -pl ruoyi-business -am -DforkCount=0 test` **865 项通过、0 失败、3 跳过**；ClassGrouping 定向 13 项通过，含多校当前学校命中、当前学校不在任课集合拒绝、overview 消歧、课程跨校拒绝。
- **未做**：未发布 130，未用真实教师账号做浏览器复验；发布后需用 651666 或同形账号从两校课程卡各进一次课堂监控，确认学生列表与分班/座位正常。

## 本轮已发布 1.30.64（2026-11-02）：拓展课双空间 + 教材绑定 + 审核 P0/P1/P2 修复

- **P0 题单 1267**：`PythonPracticeMapper.selectPlanClasses` 与拓展班表字符串 JOIN 排序规则不一致（0900_ai_ci × general_ci），教师 Python 题单列表/详情在所有题单上 500。已改 `CAST(... AS BINARY)`；本机主库实测修复后 JOIN 可执行且匹配 8 行，原写法仍报 1267。`scripts/extension_precheck.py` 增加同形 JOIN 只读门禁，失败拒绝发布（实测生效）。
- **P1 学生登录被拓展接口硬耦合**：`StudentSpaceSwitch.selectAtLogin()` 捕获 context 加载失败后降级进 `/student/index`，不再出现“登录已成功、页面加载失败”。
- **P1 教材绑定被静默清空**：原册下架或目录加载失败时保留原绑定并提示，不再清空；后端提交绑定与库内一致时跳过上下架/目录校验（`BizLessonServiceImpl`），保证老师仍能改课程其它字段。教材下拉加载加请求序号，迟到响应丢弃。
- **P1 PDF 少页**：`TextbookPdfService` 整册校验页数=totalPages、课次校验页数=区间长度且页号连续，缺行直接失败；缓存版本改为全部页哈希（只改中间页也会失效）。
- **P1 负责人双角色（2026-11-02 已按用户决定调整）**：拓展班**管理权限只看 `teacher` 角色**——同时兼任教研员的教师可以管理和创建自己的拓展班；负责人候选下拉与 `teacher/teachers` 查询仍排除教研员身份用户，纯教研员与学生仍无管理权限；管理员从下拉选负责人时看不到双角色教师。
- **P2**：`currentLesson` 拆为普通读与 `currentLessonForUpdate`（看板/学生 15 秒轮询不再持锁）；学生空间 store 退出清空、带账号与请求序号校验，换账号不显示上一账号班级；阅读器“保存本课 PDF”只对 LESSON 可用。
- **P2 第二批（2026-11-02 续）**：①历史名单冻结——签到名单与题单学情分母改为一律读 `biz_extension_lesson_member`/`biz_extension_plan_member`，新增 `sql/extension_legacy_snapshot_v1.sql`（幂等 INSERT IGNORE）把旧 50 班当前成员一次性回填，登记旧班后必须立即执行，前检脚本新增 `legacy_snapshot_backlog` 缺口提示；②常规入口守卫——新增 `LessonScopeUtils.requireRegular`，在课程指派（含 `currentAssignmentQuery` 过滤 `lesson_scope='regular'`）、课程设计器保存、课堂分组、在线协作、物联网实验统一拒绝拓展/历史课；③候选学生——`sys_dept` 随查询 JOIN 返回 `school_type`，去掉逐行查部门，SQL 取 201 行判断截断，接口返回 `{items,truncated}`，前端提示“只显示前 200 人”；④PDF——全局并发信号量限 2 份构建、超时提示稍后重试，缓存每小时最多清理一次（14 天过期、最多 40 份）。
- **验证（本机）**：`mvn -pl ruoyi-business -am test -DforkCount=0` **855 项通过、0 失败、3 跳过**（新增 `TextbookPdfServicePageValidationTest` 4 项、`LessonScopeUtilsTest` 3 项、候选截断 2 项）；`ruoyi-admin` 编译通过；Vue3 `build:prod` 通过（含登录门禁）；前检脚本复跑 `plan_class_join ok/matched=8`、`legacy_snapshot_backlog 0/0`。未重启本机 8080 旧 JAR。
- **130 发布与验证（2026-11-02 12:12–12:18）**：备份 `/data/backups/xueyeceping/20261102_extension_textbook_v1`（整库 `database.sql.gz` 51,278,014B / `83881da2…`、Dump completed，含 unit/site/env/jar/index/config 与 SHA256SUMS）。先执行 `extension_class_v1.sql`→`lesson_textbook_binding_v1.sql`→`extension_legacy_snapshot_v1.sql`（旧50班尚未登记，回填 0 行，`legacy_snapshot_backlog` 后续再查）；复核 999 课 / 9 题单 / 27,897 学生全部默认 `regular`，4 张拓展表 0 行、绑定 0 行。后端以线上外层 JAR 复制件替换内嵌 business（条目集合 291 不变）后切换 systemd unit，3009 探活 200、PID 2834269、NRestarts 0、启动日志 0 条 ERROR/Exception；前端 Nginx root 切换后 `nginx -t` 通过、首页磁盘与 HTTP 同为 `3e6ea828`、`/prod-api/captchaImage` 200。
- **130 线上实测（2026-11-02）**：P0 排序规则——原 JOIN 在真实库复现 `ERROR 1267`，`CAST(... AS BINARY)` 版返回 85 行；接口冒烟通过（教师 `/business/teacher/dashboard-data` 200、教材 13 册 200；学生 `/extension/student/context` 200 且含 `space_pref/space_selected`；学生调拓展教师接口 403）。双角色权限调整后复测：教师 `classes/teachers/candidates` 200，负责人候选 3 人不含双角色账号，学生仍 403。
- **回退**：恢复 `service.before` 到 unit 后 `daemon-reload`+重启（回 1.30.61 后端），恢复 `nginx.before` 后 `nginx -t`+reload（回 1.30.62 前端），更新 142 转 DRAFT；新增表/列均为可空或带默认值，保留不影响旧版本，无业务数据回滚。

## 已发布基础：拓展课双空间（2026-10-30 开发，2026-11-02 随 1.30.64 上线）

- 用户已确认旧50班在130：拓展班/成员、学生账号双空间、独立签到页、Python隔离及历史只读代码已实现；课时冻结名单，题单投放名单在发布及新成员加入时补记，移出/归档不删历史。90项Java回归、完整隔离打包、Vue3生产构建、20+4项真实接口、12项浏览器与生产构建补验通过；Python提交用mock。130 已于 2026-11-02 执行 `sql/extension_class_v1.sql`（4表7列）并随 1.30.64 发布；旧50班**尚未**在130登记，登记后须立即执行 `extension_legacy_snapshot_v1.sql` 冻结历史名单。原主库缺DATA_LAB快照依赖仍待治理，未伪造旧实验快照。[范围、备份、证据及待办](extension-class/tasks.md)。

## 已发布基础：课程教材绑定与学生端入口（2026-10-30 开发，2026-11-02 随 1.30.64 上线）

- 课程设计器删除“第几课”显示，新增“本课对应教材第几课”单选下拉：非必填、只能选本课程年级+学期对应册的一课（九年级回退全一册），保存写入 `biz_lesson.textbook_book_id/textbook_section_key`；迁移 `sql/lesson_textbook_binding_v1.sql` 已在本机库与 130 执行（130 绑定行 0）。
- 教师首页课程卡删除“第N课”标签；已绑定课程的「教材」胶囊直达该课（阅读器 `?section=` 预选），未绑定仍按年级+学期进册、课次手选。学生端顶部仅在当前课已绑定教材时显示「电子教材」，直达该课并可浏览整册；未绑定不显示。
- 权限边界：教材 `/books` 仍仅教师/教研员/管理员；`/toc`、`/pages`、`/page` 放行 `@studentSs.isStudent()`（含 `biz_student` 兜底）。页图仍走鉴权 + ETag + no-cache。
- 教师端 PDF 下载（2026-10-30 同批）：书架每册增加「下载整册」，阅读器「打印本课」旁增加「保存本课 PDF」和「下载整册」；学生端阅读器不显示下载入口。接口 `GET /business/textbook/books/{bookId}/pdf` 与 `/books/{bookId}/sections/{sectionKey}/pdf`，仅 `admin/teacher/researcher`，由后端用页图合成 PDF，缓存在 `TEXTBOOK_ROOT/.pdf-cache`（首页/末页哈希+页数作版本）；目录不可写时退回临时文件。
- 本机验证：后端编译、教材与课程定向测试 **35 项**通过；Vue3 生产构建通过；Playwright 教师选课保存→课程卡直达、学生直达/返回/未绑定隐藏共 **11 项断言**全 PASS、0 页面异常，证据 `output/playwright/lesson-textbook-binding-20261030/`；PDF 实测六上整册 **92 页/16.7MB**（首次 3.2s、缓存 0.23s）、第3课 **4 页/736KB**，学生下载 403，书架/阅读器按钮与页数验收全 PASS，证据 `output/playwright/textbook-pdf-download-20261030/`。
- 发布结果：2026-11-02 已随 1.30.64 上线（130 绑定行 0，属未使用状态）；共享课程沿用原 lesson 行，绑定随创建者数据可见。PDF 缓存目录需对应用用户可写，可按需清理 `.pdf-cache` 释放约每册 16–22MB（新版本每小时最多清理一次）。

## 本轮已发布 1.30.63（2026-10-30，机位助手 0.2.6 客户端热替换）

- **0.2.6**：默认安装目录 `C:\SchoolSeatHelper`，首次启动迁移 `D:\SchoolSeatHelper` 的配置与平台地址；exe 794,112B / SHA-256 `91EAC119…`；新 release `20261030_seat_helper_026_v1`，活动下载路径原地替换，未重启后端、未改前端/库表/env。
- **验证**：客户端回归 53/53；真实学生下载 794,236B（含站点块），去尾后 EXE 哈希一致且含 `0.2.6`/`C:\SchoolSeatHelper`；更新 141/1.30.63/PUBLISHED 且教师可见。
- **使用/回退**：已装 0.2.5 的电脑不会自动换目录，需退出后重新下载安装一次 0.2.6；0.2.5 内部固定使用 `D:\SchoolSeatHelper`，仅复制文件夹到 C 盘不能迁移。备份 `/data/backups/xueyeceping/20261030_seat_helper_026_v1` 保存 0.2.5 exe/manifest/env，恢复即可，无需重启或业务 SQL。

## 前版已发布 1.30.62（2026-10-30，前端热更 `20261030_textbook_zoom_v1`）

- **阅读缩放**：阅读器新增缩放，默认“适应整页”（一屏完整一页），可切“适应宽度”或 50%–200% 固定比例，选择记忆在 localStorage；窄屏默认收起目录、展开时目录为浮层；打印仍按整页输出。教师 130 实测 12/12 PASS（默认整页、宽度、125%、刷新记忆、打印 4 页、390px 不溢出、0 异常）。
- **制品与切换**：前端首页 `7d3730f4…`；后端未重启、库表/教材图片/env/助手未改；Nginx root 指向新 release 后平滑 reload。备份 `/data/backups/xueyeceping/20261030_textbook_zoom_v1`（整库 50,210,080B / `833fbaa9…`、nginx.before、index.before）。
- **回退**：恢复 `nginx.before`、`nginx -t`/reload，前端退 1.30.61（`cbefc947`），更新 140 转 DRAFT；无业务 SQL 回滚。

## 前版已发布 1.30.61（2026-10-29，`20261029_textbook_seat_v1`）

- **电子教材**：13 册 1301 页（3–9 年级），教师端书架/阅读器/按课次定位/打印本课；页面图经鉴权回图，`private, no-cache` + `Vary: Authorization`，ETag 命中 304；连续 3 页失败有聚合提示，打印准备中切走不再缺图。
- **机位识别**：前端改为最低版本比较（下限 0.2.3），已装 0.2.5 的电脑不再被写死的 0.2.3/0.2.4 白名单拒绝；识别长期失败时提示请老师人工确认机位。0.2.6 已于 1.30.63 热替换上线（默认 C 盘）。
- **制品与切换**：后端基于线上 1.30.60 JAR 仅替换业务 JAR 与 `application.yml`，新 JAR 108,153,336B / `40a6e682…`；前端首页 `cbefc947…`；教材图片 1301 张（255,685,333B）上传并逐张校验。备份 `/data/backups/xueyeceping/20261029_textbook_seat_v1`（整库 50,054,633B / `796d1117…`）；`TEXTBOOK_ROOT` 已入 shared env（29 项）；后端 17:46:10 重启，PID 2784023 / NRestarts=0；Nginx 零停机 reload。
- **验证**：教师 API 13 册、`toc/1` 88 页、页图 304、越界 500；学生 403；教研员 13 册；教师 Playwright 9 项全 PASS（书架/七上 125 页/打印 4 页/滚动/0 异常），课程卡 21 个「教材」胶囊可直达对应册；学生 390px 无教材入口、0 pageerror；更新 139/1.30.61/PUBLISHED 且教师可见。
- **边界**：真实学生机器 + 实际助手 0.2.5 的认领复验未做；同传克隆共用指纹（B-1）、学校自有域名 `allowed-origins`（B-6）仍按原边界。回退与未验项见[发布日志](RELEASE_LOG.md)。

## 前版已发布 1.30.60（2026-10-28，双端 `20261028_fullstack_v1` + 助手 0.2.5）

- **电子导学单**：模板库预览改走脱敏 `/preview`（原先 `getInfo` 会在查看者浏览器执行同县他人模板里的 `onCreated`/`functions`，已探针复现并封堵）；`v-form-render` 实际 emit `formChange`，2 秒防抖原为死代码，现补 `@formChange` + 表单根 `input/change` 捕获，保存失败写本机副本并可重试；`/progress` 增 `maxTotalScore`、`/student/grading` 增 `maxScore`/`gradingStatus`，学生卡改“已得 X / Y 分”、待批改中性色，看板正确率改按满分算；必答校验自动切页并定位到具体题目；删除无入口的 `recore-all`；删课与删学生补删导学单进度/作品行；新手向导补“本课主题/预计课时”接通 AI 参数。完整审查见[导学单审查](guide-sheet-audit-20261027.md)。
- **机位登录顺序**：新增 `latest:{studentId}` 最新登录优先，旧登录只返回 `superseded`、不续期不改写不消费票据，最新登录未认领设备即清旧卡片，`owner` 必须存在且与本次会话匹配；前端无内网 IP 时整块不渲染“未连助手”，状态移到身份行悬浮提示。详见[机位专题](seat-ip-login-order-20261028.md)。
- **诊断与性能**：`SlowSqlInterceptor` 补 6 参数 `Executor.query` 签名（PageHelper 分页查询此前完全绕过持久化，`sys_perf_event` 近 7 天 0 条 slow_sql），见 [ADR-003](../docs/architecture/ADR/ADR-003-slow-sql-interceptor-six-arg.md)；资源鉴权拆为索引查询、班级状态加 45 秒缓存、题库导入错误逐行返回。
- **发布与验证**：经 123 跳板（Windows OpenSSH）发布；整库与配置备份在 `/data/backups/xueyeceping_20261028_pre_fullstack/20261028_122512`；后端在隔离副本构建（工作区 200+ 未提交改动未混入），JAR `05c966cd…`、首页 `3b1ecb12…`、助手 `2ddc1d55…`；5 条索引在线 DDL 43 秒建完；教师端与学生端均为 0 pageerror、0 4xx/5xx；学生实测下载助手去掉尾部引导块后哈希一致；更新 138/1.30.60/PUBLISHED 且教师可见。
- **边界**：机房已装助手 0.2.4 的电脑需重新下载 0.2.5 才有机位修复；同传克隆共用 `device_id` 时 IP 仍可能被后心跳覆盖（待服务器只读核对 `biz_seat_helper_device` 同指纹多行与 `192.168.1.252` 历史绑定）；`assertNoPendingCountyExam` 仍同时拦心跳/上传，`rateLimitMap` 不清理；`getInfo` 对同县教师返回答案判定为设计取舍不改（真正入口已封堵）。

## 线上回归修复：助手 0.2.5 网页认不出（2026-10-29，已随 1.30.61 发布）

- **现象**：塔山教师用学生账号安装 0.2.5 后，网页仍显示“机位未关联 / 下载机位助手”，助手窗口却显示“已保存、已连接”。上一节“需重新下载 0.2.5 才有机位修复”的边界因此不成立。
- **根因**：正式前端 `RuoYi-Vue3/src/utils/seatHelper.js` 的本地探测写死 `['0.2.3','0.2.4']`；助手回环 `/identity` 回报 0.2.5，被前端判为不可用。装了 1.30.60 配套 0.2.5 的电脑全部中招，0.2.4 旧机不受影响。
- **前端修复**：改为最低版本比较（`helperVersionAtLeast`，下限 0.2.3），后续小版本不再被拒；新增 `npm run test:seat-helper` 并纳入 `build:prod`。本机生产构建与发布检查通过，产物已无写死白名单。
- **助手 0.2.6**：默认安装目录由 `D:\SchoolSeatHelper` 改为 `C:\SchoolSeatHelper`（同传/还原通常只覆盖 C 盘），不可写回退用户 LocalAppData；同机首次运行会把旧 D 盘/用户目录的 `seat-helper.dat`、`platform-url.txt` 迁到新目录，原目录保留便于回退；版本号收口为 `AppVersion` 常量。客户端 53 项回归通过，`clients/seat-helper/dist/manifest.json` 为 0.2.6，**已于 1.30.63 热替换上线**。
- **发布**：前端与后端已随 1.30.61 发布；助手 0.2.6 已于 1.30.63 替换下载文件（默认 C 盘），已装 0.2.5 的电脑需退出后重新下载安装一次才会迁移。真实学生机器 + 实际助手认领复验未做。

## 已发布：教师端电子教材（1.30.61 上线，1.30.62 补充阅读缩放）

- **内容**：**13 册 1301 页**（三上/三下/四上/四下/五上/五下/六上/六下/七上/七下/八上/八下/九年级全一册），1400px/JPEG q75 压缩后 **243.8MB**；3–9 年级已全部上架，书架不再有「暂未上架」灰卡。四册新书 PDF 于 2026-10-29 上午到位后补导入。
- **数据**：`sql/textbook_v1.sql` 建 4 表 + 菜单 26100（`admin,teacher,researcher`），`sql/textbook_seed_20261029.sql` 灌 **50 单元 / 334 条目（242 课、49 单元导入、19 小结、13 前置、11 附录）/ 1301 页**；均已在本机库 `xueyeceping_server_20260729` 执行，每册页号 1..total_pages 连续、`bad_range=0`、课次编号连续。
- **后端**：4 个接口 `/business/textbook/books`、`/toc`、`/pages`、`/page/{bookId}/{pageNo}`；类级三角色鉴权，页面图 `sha256` 作 ETag、**`private, no-cache` + `Vary: Authorization`**（2026-10-29 由 `max-age=604800` 改，见下）、命中 304。教材根目录 `TEXTBOOK_ROOT`，生产须指向 release 外共享目录。`LessonInfoVo` 补 `semester`。
- **前端**：侧边「电子教材」+ 书架 `/textbook` + 隐藏阅读器 `/business/textbook/read/:bookId`；进入**不预选课次**，课次手选后定位并高亮；「打印本课」只输出本课页面。教师首页课程卡顶部「教材」胶囊按 `grade + semester` 选册（九年级回退 `9`），缺册提示后跳书架。页面图走 axios blob + 80 页 LRU，避免 `<img src>` 无 token。1.30.62 起阅读器默认“适应整页”，支持适应宽度/50%–200% 缩放并记忆，窄屏目录为浮层。
- **验证**：后端 822 项单测 0 失败（3 跳过）；Vue3 生产构建通过；教师接口实测 13 册、七上 4 单元 125 页、单页 197,762B、304；无 token 401、学生 403、越界/缺册 500；Playwright 1440×900 共 12 项全部 PASS（含封面 blob、手选第 1 课定位第 11 页、打印 4 页、滚动到第 60 页、课程卡直达六上）。证据见[专题任务](textbook-module/tasks.md)、`output/playwright/20261029-textbook-smoke/`。
- **发布**：已于 2026-10-29 随 1.30.61 发布；整库备份、两份 SQL、243.8MB 图片到 `/data/apps/xueyeceping/shared/textbook`、`TEXTBOOK_ROOT`、重启与三角色验收均完成，证据见[发布日志](RELEASE_LOG.md)。发布前不得把教材图放入无鉴权的 `/profile/`；取舍见 [ADR-004](../docs/architecture/ADR/ADR-004-textbook-page-images-outside-release.md)。
- **边界**：九年级原始 PDF 缺印刷页 31–34，已按页脚分段偏移并在 `toc-review.md` 标注；不做整册下载、全文检索、自动匹配课次、学生端入口。

### 独立审计后的 P1 修复（2026-10-29，已随 1.30.61 发布）

审计发现 3 个 P1，已全部修复并验证；第二轮又修了 A-1/A-2/A-3/A-5/A-7（见下）。其余 P2/P3（种子无删除导致的残留、菜单 SQL 幂等边界、实体直出、权限串与角色不一致、根目录启动校验、全册 `<img>` 常驻、`/pages` 死代码、ETag 强比较等）仍在待办，逐条结论见 `contexts/textbook-module/handoff-review.md` 的审查轮次。

- **失败页永久缓存（P1-1）**：`api/business/textbook.js` 请求失败时不再把 entry 留在缓存（原样会让「点击重试」永远命中同一条 rejected promise 且不再发请求）；`loadTextbookPage(bookId, pageNo, force)` 支持强制绕过缓存，`LazyPageImage.reload()` 走 `force`。在途请求被淘汰/被重试时不再立即 revoke，改登记 `orphanUrls`，离开阅读器统一释放。
- **打印静默缺页（P1-2）**：`reader.vue printSection()` 收集失败页码，任一页取不到就关闭空白窗口、报「第 X 页加载失败，已取消打印」，不再产出少页讲义；`win.closed` 也做保护。
- **图片缓存跨身份复用（P1-3）**：`TextbookController.page()` 响应头改 `Cache-Control: private, no-cache` + `Vary: Authorization`。原 `max-age=604800` 实测可被绕过——同一浏览器带 token 取一页后再**不带 token** 请求同一 URL，`transferSize=0`、197,762B `image/jpeg`，服务端零请求；且改图 7 天不生效。ADR-004 决策与后果已同步改写。
- **顺带修的两处**：`utils/request.js` 的 blob 分支改为尊重 `silentError`（原先无条件弹 toast，一册多页缺失会连续弹窗）；缺册常量补 `三下`（原漏登记，三年级老师连灰卡都看不到）。
- **验证**：新增 `TextbookControllerPageResponseTest` 3 项（no-cache/Vary/ETag/304/陈旧 ETag 回全图）全通过；浏览器实测 7 项全 PASS（行内错误、无 toast、重试真的重发请求 `hits=2`、缺页中止并报页码、失败不留空白窗口、正常打印仍 4 页无坏图）；`vite build` EXIT=0；书架实测 9 册 + 4 册待上架（当日补册后为 13 册 + 0 灰卡）。
- **jar 已重打包并实测（2026-10-29 11:53）**：`ruoyi-admin/target/ruoyi-admin.jar` 108,155,250B / SHA-256 `9a887bc3…45a0b9`，嵌套 `ruoyi-business-3.9.0.jar` 内 `TextbookController.class` 已含 `no-cache`、无 `max-age` 残留。停服后 `mvn -pl ruoyi-admin -am clean package -DskipTests` 成功（48s）。**启动后端须注意本机环境变量 `SERVER__PORT=50485` 会覆盖 `server.port`（应用会起在 50485 并因端口占用退出），必须显式 `-Dserver.port=8080`**；并设 `TEXTBOOK_ROOT`。实测响应头：`Cache-Control: private, no-cache`、`Vary: Authorization`、ETag、条件请求 304、无 token 401、books=13、单页 197,762B。

### 第二轮独立审计修复（2026-10-29，已随 1.30.61 发布）

- **教材**：连续 3 页图片失败时阅读器顶部显示聚合横幅并可「重试失败页」（A-1）；打印取图期间持有 blob 引用，准备中途切走路由不再撤销打印窗图片（A-2）；强制重试复用在途请求（A-3）；`requirePage` 增加 `pageNo <= totalPages`，防止种子 SQL 残留旧页继续回图（A-5）；两个教材脚本注释由 918 页改为 1301 页（A-7）。
- **机位**：学生端连续 5 轮回报“无法识别浏览器”后改为“可请老师人工确认机位”并把轮询降到 15 秒（B-2）；版本解析改为纯数字点分，`0.2.3a`/`0.2.3-beta` 不再按 0.2.3 放行（B-4）；`build:prod` 重新纳入旧 `seatHelper.test.js`（B-5）。
- **验证**：后端 `TextbookCatalogServicePageBoundsTest` 3 项 + 页图响应 3 项通过；机位前端单测 9 项通过；`npm run build:prod` EXIT=0（入口 gzip 559533B）。教材标准冒烟 12 项全 PASS；定向浏览器检查 6 项全 PASS（拦截页图后第 1–3 页失败显示横幅、重试后恢复 1400px、打印准备中切走书架后打印窗 4 页图仍全部可用、无 JS 异常），证据在 `output/playwright/20261029-textbook-fix-check/`。机位识别降级提示只做代码与单测验证，未在受限机房实跑。
- **仍未修**：教材书架封面缓存（A-4）、`/pages` 死代码/ETag 强比较/`Files.copy` 截断/册计数子查询（A-6）、`no-cache` 回滚 304 成本（A-8）；机位同传克隆指纹（B-1，需现场数据）、学校自有域名 allowed-origins（B-6，发布前核对）、浏览器票据多标签（B-3）、PNA（B-7）、dat 原子写（B-8）。

### 补齐 3–9 年级 13 册（2026-10-29，已入库并随 1.30.61 发布）

- **触发**：三下/四上/四下/七下 PDF 于当日 08:14–09:37 才落到源目录，上一版 pack 只有 9 册。
- **七下是合订本**：`七年级下册.pdf` 共 248 页，第 1–125 页其实是**七上**（与已导入七上目录逐课一致），第 126–248 页才是七下。为此给 `import_textbook.py` 增加 `source_page_start/source_page_end`，只渲染 126–248 并重新编号 1–123，`volumes.json` 仍指向原始 PDF，`toc/xxkj-7-1-2026.json` 的 notes 写明该事实。
- **TOC 口径**：三下/四上/四下无文本层，目录页（PDF 5–6）逐行抄录后按「印刷页 1 = PDF 7（偏移 6）」换算，并渲染核对首页、第二单元页、末课页、版权页；七下有文本层，20 课起始页按「第N课 课名」文本逐页校验。四册均为 4 单元 18 课（七下 20 课），主教材无独立单元小结页（沿用三上口径 `summary=null`），七下有 4 个独立小结页。
- **产物**：`output/textbook-pack-20261029/pages/` 13 个目录 1301 张 247MB（manifest 记 243.8MiB）；`sql/textbook_seed_20261029.sql` 重写为 13 册全量（幂等，无 DELETE）。
- **入库**：先 `mysqldump` 四表到 `backups/20261029_textbook_13volumes/textbook_before_13volumes.sql`（148,763B，SHA-256 `b85d640b…`），再整份单会话执行种子 SQL；复核 13 册 / 50 单元 / 334 条目 / 242 课 / 1301 页，每册页号 1..N 连续、无越界、课次号连续。
- **前端**：`TEXTBOOK_PENDING_VOLUMES` 清空（灰卡机制保留，将来缺册再填即可）；书架实测 13 册、0 张灰卡、13 张封面均 1400px；七下 `read/19` 123 页、28 个可选条目、第 1 课定位第 11 页、打印 5 页无坏图；三下 `read/11` 86 页封面正常。
- **未做**：图片尚未上传 130；`smoke_teacher_textbook.py` 的书架断言已改为 13 册 + 0 灰卡。

## 接管入口与系统


1. 读取 `AGENTS.md` → 本文件 → [架构索引](../docs/architecture/INDEX.md)，再按任务阅读专题和相关源码；历史记录按需查阅。
2. 当前源码、可复核数据库/部署证据优先；冲突时核对日期、目标主机和真实版本。工作区有大量未提交业务修改，不能视为某一正式制品或未经隔离直接发布。
3. AOCI 已卸载，不再维护索引；不自动重装。操作规则、当前事实、架构/专题说明与源码及测试共同支持后续接力。

| 层 | 当前事实 |
| --- | --- |
| 后端 | `RuoYi-Vue/`：Java 8、Spring Boot、RuoYi、Maven 多模块；`ruoyi-admin` 启动，`ruoyi-business` 为业务。 |
| 前端 | 唯一维护 `RuoYi-Vue3/`：Vue3、Vite、Element Plus；旧 Vue2 不默认修改。 |
| 数据与集成 | MySQL、Redis、MyBatis；Judge0、CryptPad、EMQX/MQTT、WebSocket、LibreOffice 边界见架构专题。 |
| 客户端与文件 | `clients/seat-helper/` 为可选 Windows 助手，不是登录/作答前置条件；SQL 放根目录 `sql/`，上传、备份、日志、output/tmp 不默认作为业务源码。 |

## 环境与正式基线

| 环境 | 最近已记录状态与边界 |
| --- | --- |
| 130 正式主站 | `http://10.52.1.130`及`http://xxkj.xsedu.net.cn`，Linux/systemd/Nginx；产品 **1.30.65**：后端 `20261102_extension_dual_role_v1/backend`（JAR `4bbf46e0`，内嵌 business `ae22d297`），前端 `20261102_extension_textbook_v1/frontend`（首页 `3e6ea828`），机位助手 **0.2.6**（默认 C 盘）。教材图片在 `/data/apps/xueyeceping/shared/textbook`，`TEXTBOOK_ROOT` 已入 shared env。最新整库备份在 `/data/backups/xueyeceping/20261102_extension_textbook_v1/`。 |
| 本机 | 默认后端 8080、Vue3 80，以实际进程/配置为准；数据库基线 `xueyeceping_server_20260729`。机位两列迁移本机尚未执行，另缺历史 `biz_data_lab_snapshot`；教材 4 表与 **1301 页（13 册）** 数据已入库，启动后端须设 `TEXTBOOK_ROOT=output/textbook-pack-20261029/pages`；工作区有 200+ 未提交改动，发布时须走隔离副本构建。 |
| 123 旧环境 | Windows/NSSM/Nginx，3009/3010；旧记录 release `20261005_ui_redesign_v1`（1.30.29），不代表本轮实测状态。 |
| 129 扩展服务 | Judge0、CryptPad、EMQX 等；健康与连接按集成/部署手册现场核对，不凭旧记录假定可用。 |

## 前版已发布：流程图判断分支左右端口优化（2026-10-25，130正式1.30.59）

- 线上反馈判断框“是/否”从同一底部端口分出、挤在一起。改为：判断框的多条正向分支按目标 x 位置分别使用左右锚点（最左 `_left`、最右 `_right`，从侧边水平伸出再纵向汇入），不再共用底部中点；非判断节点与回边逻辑不变。
- 后端仅替换 `FlowchartLayoutService` 及内部类（1.30.58 `ff8a29a7` → `be2233aa`），其余逐字节不变；前端不变（`ee215ccc`）。8 项排版定向测试（含左右锚点断言）通过。整库备份 488,160,073B SHA `4bd3de44…`；切换 COMPLETED、NRestarts=0、三服务 active、接口 200。线上端到端：7 节点/7 连线、7/7 正交折线，判断框两分支分别用 `_left`/`_right`。更新137/1.30.59 PUBLISHED 且教师可见，登记 SQL `sql/flowchart_ai_answer_branch_anchor_release_20261025.sql` 幂等。回退退回 1.30.58（`ff8a29a7`），前端不变，无 SQL。

## 前版已发布：流程图 AI 生成连线布线优化（2026-10-25，130正式1.30.58）

- 线上反馈连线交叉/穿越节点。原实现只给节点坐标、让 LogicFlow 自动直连。后端改为显式输出节点宽高 + 每条边的正交折线与固定锚点：相邻层“下→横→下”Z 形、同层绕下方、向上回边走图形左侧通道（判断框用左锚点），并按文字给出统一宽高。
- 后端仅替换 `FlowchartLayoutService` 及内部类（1.30.57 `a18fc935` → `ff8a29a7`），其余逐字节不变；前端不变（`ee215ccc`）。8 项排版定向测试（含循环图折线/锚点断言）通过。整库备份 487,693,925B SHA `17804390…`；切换 COMPLETED、NRestarts=0、三服务 active、接口 200。线上端到端：7 节点/7 连线、7/7 带正交折线与锚点、6 层、每层最多 2 节点。更新136/1.30.58 PUBLISHED 且教师可见，登记 SQL `sql/flowchart_ai_answer_orthogonal_release_20261025.sql` 幂等。回退退回 1.30.57（`a18fc935`），前端不变，无 SQL。

## 前版已发布：流程图标准答案 AI 排版修复（2026-10-25，130正式1.30.57）

- 线上真实反馈：郑东旭账号用教材二分查找照片测试，含循环回边的流程图被挤成一行、连线混乱。根因是排版用拓扑排序分层，环下游节点无法收敛被统一塞入同层。改为从“开始”节点 BFS 分层（回边只保留连线、不改层级）、出度为 0 的终点沉底、孤立分支兜底，并加大行距/列距。
- 后端仅替换 `FlowchartLayoutService.class`：线上 1.30.56 JAR `458e94f` → 新 JAR `a18fc935`，外层其余逐字节不变；前端不变（首页 `ee215ccc`）。Java 排版 8 项定向测试（新增教材循环图回归）通过。整库备份 487,682,454B SHA `51b68848…`；切换 COMPLETED、NRestarts=0、三服务 active、接口 200。同一教材照片线上端到端复验：11 节点/13 连线、纵向 8 层、每层最多 2 节点、无告警。更新135/1.30.57 PUBLISHED 且教师可见，登记 SQL `sql/flowchart_ai_answer_hotfix_release_20261025.sql` 幂等。回退退回 1.30.56（`458e94f`），前端不变，无 SQL，不整库回灌。

## 前版已发布：题库画流程图题“拍照 + AI 生成标准答案”（2026-10-25，130正式1.30.56）

- 教师在题库“操作题→画流程图”标准答案页签上传一张流程图照片，后端复用操作题视觉模型（教师多厂商 Key、端点白名单、`chatJson`）连同本题已填名称/年级/学期/第几课生成拓扑，`FlowchartLayoutService` 按最长路径自动排版坐标，经 `normalizeDocument` 校验后只回填画布供老师核对，保存仍走现有 `saveFlowchartQuestion`+`config_revision`；AI 结果不直接写库，不改成绩、不改表结构。
- 制品隔离：后端基于线上 1.30.55 JAR（`08dce49`）仅替换 `FlowchartController` 并新增 3 类（含内部类共 7 条目）重打为 `458e94f`，其余逐字节不变；前端基于 research-copy 基线源码仅叠加 `flowchart.js`/`FlowchartQuestionDesigner.vue`/`question/index.vue` 重建（首页 `ee215ccc`）。未混入工作区约 166 项未验收改动。
- 切换：独立 systemd 任务 COMPLETED，三服务 active，PID2666913/NRestarts=0，`:3009/captchaImage` 与 `/prod-api/captchaImage` 均 200，启动/运行日志无类加载异常，新端点 `ai-generate-answer` 与现有流程图接口回归无 404/500。整库备份 487,661,077B（SHA `1d0013cf…`）。
- 真实照片端到端已通过：经 130 本机用郑东旭账号（百炼 qwen3.7-plus）调用，识别出 7 节点（开始/输入长和宽/面积=长×宽/面积>20?/输出大/输出小/结束）与 7 连线、类型全对、无告警；更新 134/1.30.56 已登记 PUBLISHED 且教师 list 可见，RELEASE_LOG 已登记。回滚退回 1.30.55（后端 `08dce49`/前端 `6233b36`），无结构/业务回滚 SQL。手写/复杂照片效果仍需抽样。详见[需求](flowchart-ai-answer/requirements.md)、[设计](flowchart-ai-answer/design.md)、[任务](flowchart-ai-answer/tasks.md)。

## 前版已发布：学生作品 HTML/TXT 预览与下载权限修复（2026-10-25，130正式1.30.55）

- 正式日志确认 2026-10-23 16:32 有 dept=169 的 6 名学生（u=5594/5603/5611/5612/5622/5638）在 `/business/practical-attachment/{id}/text|download` 被角色校验拒绝；这些账号在 `sys_user_role` 中无任何角色，但平台其它学生接口通过 `biz_student` 档案兜底仍可登录和提交。全库 27,898 名学生中 6,839 名无 `student` 角色。
- 只改 `PracticalAttachmentController` 类级鉴权为 `@studentSs.isStudent() or teacher or admin`，与其它学生端接口一致；作品归属和班级范围仍由 `ResourceAccessService` 校验，未放宽到任意学生互看，也未批量修改账号数据。
- 后端仅替换 `ruoyi-business-3.9.0.jar` 内该 1 个 class，外层 JAR 仅嵌套业务 JAR 不同；JAR 108,119,085B/SHA-256 `08DCE49FEEF2BB39BDCB19791833BEB73BC2AD3384CE0999EA4CF08C51A028AA`。全量 811 项及新增 2 项定向权限测试通过，完整 clean package 通过。
- 整库备份 487,635,797B/SHA-256 `30ae9c022b45b3ae9e39ffc88dffb9cd15a63c2b41f4d5807dc5b4ef5c645dc4`；12:51:41→12:52:05 恢复（24秒），PID2664495/NRestarts=0、三服务 active，API/首页 200。管理员真实登录后附件文本接口返回 HTML/200；真实教师 `/business/platform-update/list` 确认 1.30.55/PUBLISHED；更新 133 登记唯一。无结构迁移/业务数据修改；无角色学生真实浏览器复验仍待有效账号。1.30.54 云盘提取码修复的完整记录见[发布日志](RELEASE_LOG.md)。

## 前版已发布：文件作业自主重交（2026-10-20，130正式1.30.53）

- 用户确认保留原分，上传新作品成功后才清分。当前课文件作业（含图片组）已有0分/非零分均可直接重交；暂存/取消/失败留原作与原分，成功后旧版本/分数归档，新版待批。流程图等其他题型保持；缺旧版本的异常有分记录拒绝重交。
- Java15项、后端完整clean package、隔离前端构建/登录门禁通过；原Node6项与模拟上传区桌面/390px保持。正式教师/教研员API及浏览器3组通过、页面异常/5xx均0，44人名单与样本答案摘要不变；学生模块SHA匹配。17:45:43→17:46:06恢复（23秒），三服务active；更新131/PUBLISHED且教师可见。无结构/业务SQL，env/助手保持；[备份、隔离、回退和待验](student-file-resubmit-20261020/release-verification.md)。缺有效正式学生账号，真实重交/重新批改尚未实测。

## 课堂推进状态与结果（2026-10-20，仅本地）

- 韩梦琦反馈已只读定位：自动策略关闭；手动仍须50%，部分班级已推进后重复检查新课、部分未达标或无下一课；正式调度正常。详见[诊断](course-advance-diagnosis-20261020.md)。
- 用户确认保留门槛、本地修复。新增服务端只读预检，共用权限/真实指派/人数/下一课；显示逐班自动关闭、等待与真实预计时间。执行携带预览当前课快照并在锁内复核；保留逐班结果、刷新首页缓存，未知系统异常停止后续班级并如实返回已提交结果。
- 56项定向Java测试、admin完整clean package、前端生产构建（含7项登录测试/资源检查）通过；真实教师首页组件配模拟接口8组桌面/390px浏览器通过、页面异常0，手机结果自动可见，慢请求/切换班级迟到响应已验。未联调本地真实服务/数据库新接口、未推进正式班级。[需求/设计/任务/ADR与验证](course-advance-20261020/tasks.md)。
- 无SQL/配置/新依赖；未来发布须隔离无关工作区修改、前后端配套并重启后端。本专题不发布、不自动开启教师策略；正式版本见环境表。下一步为用户验收本地效果，再决定是否正式发布。

## 更早版本（1.30.50～1.30.52，已发布）

- **1.30.52**：机位助手静默/默认浏览器优先 + 已有分先汇总 + 打字最高分合并上线（09:23:22→09:23:45，PID2452324/NRestarts=0、三服务 active，更新130/PUBLISHED 且教师可见）。要点：业务待批不阻断已有总分、未批保持 NULL、自主重打取本轮最高、请假/缺考排除分母。备份 427,255,471B；[正式验收](seat-score-release-20261017.md)、[机位需求/任务](seat-helper-browser-20261016/tasks.md)、[成绩专题](score-partial-20261016/tasks.md)。
- **1.30.51**：丹城二小期限改为“有效非空操作作品按学生去重达半班后整班计时21天”，期限1233校正为10月15日14:18:25起算；PPTX 临时副本固定连接线路径与短单行输入输出框，原件/成绩/版本不变。19:25:17→19:25:40（PID2408758），更新129/PUBLISHED；[正式验收](zhu-teacher-feedback-20261015.md)、[PPTX设计](pptx-preview-fix-20261015/design.md)、[期限ADR-003](teaching-supervision/adr/adr-003-practical-submission-trigger.md)。
- **1.30.50**：机位回环认领（127.0.0.1:28123）+ 异机提醒；`sql/seat_helper_local_claim_v2.sql` 两可空列迁移已执行，111设备/42基准原字段保持，**本机库未迁移**。17:27:01→17:27:24（PID2346831），更新128/PUBLISHED；[正式验收与回退](seat-helper-local-20261014/release-verification.md)。
- 三者共同待验：正式学生账号下的下载/认领/换人、机房 LNA 与整班性能、自主重打与教师清分新轮；详细哈希与回退见[发布日志](RELEASE_LOG.md)。


## 其他近期成果与未闭环事项

2026-10-23题库画流程图题新增“拍照 + AI 生成标准答案”（本机未发布）：教师在标准答案页签上传一张流程图照片，后端复用操作题视觉模型（教师多厂商 Key、端点白名单、`chatJson`）连同本题已填的名称、年级、学期、第几课生成拓扑，再由 `FlowchartLayoutService` 按最长路径自动排版坐标，结果只回填画布，老师核对后经现有保存链路写入 `answer_json`；接口不写库，预计不改表结构。已验证：business 编译通过、排版 7 项定向单测通过（分层/分支/环兜底/悬空自环重复边丢弃/空与超量拒绝/越界自适应/注入拦截协同）、Vue3 生产构建通过。未验证：真实照片经真实视觉模型的识别准确率与教师浏览器端到端，需在该教师已配置并启用 Key 的环境现场验收。详见[需求](flowchart-ai-answer/requirements.md)、[设计](flowchart-ai-answer/design.md)、[任务](flowchart-ai-answer/tasks.md)。

2026-10-20按用户要求，130正式新增5道“输入入门”原样输出Python题（991131～991135/PYIO-001～005），全库230→235题；20测试点本机及真实Judge0全部通过，真实教师列表/详情/公开预览通过。已完整备份，无部署/重启/结构修改，不自动关联课程或题单；学生完整操作未实测。[题包、SQL、备份与验收](python-input-20261020.md)。

2026-10-20视频反馈的题库返回首页空白已本地复现并修复：DataLabEditor移入题库原根div，避免Fragment与串行切页动画冲突。真实组件/模拟接口桌面和390px、缓存开关4组共12次往返及实验题弹窗通过，页面异常0；生产构建通过。未联调真实登录/服务、未发布；无SQL/新依赖/后端重启，未来仅隔离发布前端并在反馈浏览器复验。[验收与边界](question-navigation-fix-20261020.md)。

2026-10-20朱仁敏2022级2班三道PPT作业共108份旧预览已备份重算（714题34份、990576题37份、990614题37份）。首轮漏掉其他题，用户再次反馈后补处理71份；正式渲染器逐份从新PDF生成新地址，避免旧哈希/浏览器缓存。108份success/v4，原件及成绩/提交/版本保护快照保持；补批142个鉴权下载、两道题真实教师页面通过，首批37份再次复核保持。换行/浅色线仍有差异，三班未处理。本次预览维护无部署/重启/结构迁移；[证据/SQL/备份与回退](ppt-connector-diagnosis-20261020.md)。

2026-10-16按用户明确授权，130赖鑫卢老师课程408、徐翎轩（2026级16班）答案395663已从待批设为20/30分，并同步GRADED/v6；该题40份已交全部已批、待批0。完整库已备份，原件哈希、答案其余字段及期限保持，无部署/重启；教师页面待刷新确认。[操作证据与定向回退](student-score-correction-20261016.md)。

| 专题 | 已验证状态及剩余边界 | 证据入口 |
| --- | --- | --- |
| 登录输入（1.30.48，后续版本保留） | WebP、VForm 按需、输入期间暂停展厅/轮播；19 项 Node、生产构建、组件和真实教师验证通过。受限浏览器输入 P95 189→65.1ms 属模拟结果，机房/整班待验。普通课程练习延迟已复现，最小修复方案等待确认，尚未改练习代码。 | [验收与方案](login-input-fix-20261014.md) |
| 页框与 Logo（1.30.49，后续版本保留） | 助手帆船 Logo；44 份转换副本 success/page-jpeg-v4，原件/成绩/版本保持。样本45280虽扩 CropBox，内容流固定 A4 `re W* n` 仍截底；不能宣称底部完全恢复，旧“恢复18行/5668墨点”不是完整恢复证据。需处理整页 clip 并保留合法局部裁剪。 | [正式验收](seat-logo-overflow-release-20261014.md)、[问题复核](grading-overflow-reframe-20261014.md) |
| 批改滚动/五星（1.30.47，后续版本保留） | Vue3 批改页 flex/min-height/overflow 和五星稳定布局；12 项既有测试、三宽度模拟接口浏览器及正式教师验证通过，不改计分/保存规则。 | [验收](grading-ui-fix-20261013.md) |
| Python V3（1.30.46） | 新增100题/340测试点，正式 Judge0 340/340 Accepted；系统V2 120+V3 100完整，验收时全库229题。出题人/知识点筛选、HTML源码放大已发布；新题未加入正式题单，学生暂不可见。62项定向测试和双端构建通过；本机 DATA_LAB 缺表阻断真实临时题单创建。 | [需求/任务](python-judge0/tasks.md)、[ADR-015](python-judge0/adr/ADR-015-zero-basis-v3-data-package.md)、[发布日志](RELEASE_LOG.md) |
| 连线页图（1.30.45） | 转换临时副本统一 DrawingML/VML 页面坐标，原件不改；57项测试/49样本对照，25份精确重算成功。未知布局、表格/多栏、Linux字体分页及历史缺线不在已解决范围，最新正式页图版本为v4。 | [专题验收](connector-render-fix-20261013/verification.md) |
| LibreOffice隔离（1.30.44） | 毒文件隔离持久化、健康恢复和 Quartz 已有正式修复记录；不要用更早版本覆盖。历史空作品、全局派生表查询和积压等遗留项须按原记录核对，不能随版本升级宣称全部清理。 | [精简前原文](PROJECT_HISTORY_20261015.md)、[发布日志](RELEASE_LOG.md) |

## 不可混淆的业务边界

- 管理员、教师、学生、教研员复用既有账号、学校、班级、课程和权限；服务端鉴权/数据范围是最终边界，前端隐藏不能替代。
- 历史导入学生可能没有 `sys_role` 学生角色；学生端接口统一用 `studentSs.isStudent()`（含 `biz_student` 兜底），作品附件读取不得改回严格 `@ss.hasRole('student')`，否则本人预览/下载会被 403。
- 课程推进后，旧课学生写入立即拒绝；教师退回和学生重交仅限当前课。旧作品、成绩仍可查阅和批改；旧“15分钟补交”记录已失效。
- 正式1.30.53文件作业已有分数可自主重交，流程图有正式成绩仍须先退回。**0分也是已批**；成功重交才归档旧分并转待批，暂存/取消/失败不清分。草稿不能替代正式作品，AI建议须教师采用才写正式成绩。
- 作品删除、答案/当前版本指针、课堂状态必须保持事务一致；反馈、成绩和导出共享未提交事实。历史残留不能因新逻辑已修而称已治理。
- TXT、单HTML、ZIP/RAR/7Z需题目显式允许；TXT/HTML不超过2MiB，文件名含扩展名不超过100字符。HTML用独立沙箱预览，压缩包不解压；这些格式不进入Office转换或AI图像评分。旧默认与区域抽测白名单不自动扩展。
- 学生反馈只返回本人正式作答/成绩；教师明确整班发布后生成不可变个人快照，报告重试不自动替换已发布反馈，作品变化须提示旧版本来源。
- 答案/成绩、课堂任务状态、终端在线Presence是不同事实；实时推送在事务提交后发出，并由REST校准。
- 独立Python刷题不写课程答案、不改变课程成绩或推进；协作由平台掌握课程/班级/文件权限；物联网作为实验辅助，不新造评分体系。
- 教师模型Key按教师/厂商加密保存，任务按创建时厂商取Key。未知单价不能写成0；三厂商接口契约测试不等于真实模型调用或评分准确率验收。

## 迁移、发布与回退要求

- 130 最新机位两列迁移 `sql/seat_helper_local_claim_v2.sql` 已执行及复跑，本机未执行。其他环境逐项前检、备份、迁移、后检，不能从130或旧日期本机记录推断目标库已具备所有表列。
- 课堂体验依赖 `sql/classroom_experience_{flowchart_history,attachment_kinds,feedback,seat_helper}_v1.sql` 和 `sql/operation_ai_multi_provider_keys_v1.sql`；保持原AI加密主密钥，助手关闭也不能省略依赖表。监管/免抽测迁移状态须按目标环境重新核对。
- 制品须隔离无关工作区修改，核对前后端和助手实际版本。130 外置配置/隔离 env 仍依赖 `20261008_seat_install_v1`，勿清理依赖 release；新后端目录须保证运行用户可写 logs/MQTT 持久化目录，避开课堂高峰重启。
- 最新正式回退依据 1.30.59 备份 `/data/backups/xueyeceping/20261025_flowchart_ai_answer_v4`：恢复本次 `service.before` 后 daemon-reload、重启后端（前端/Nginx 未改），退回 1.30.58（`ff8a29a7`，前端仍 `ee215ccc`）；撤更新137为 DRAFT，不整库回灌。上一版 1.30.58 回退见 `.../20261025_flowchart_ai_answer_v3`（退 1.30.57 `a18fc935`）。
- 正式发布成功自动登记 `RELEASE_LOG.md`、更新记录 `PUBLISHED`，用真实教师接口确认可见；1.30.55 已完成该登记。无表结构或外置env修改，机位两列本机迁移状态保持。

- 2026-10-15已按用户授权直接删除130的20个9月无引用release，释放3,939,569,664B（3.67GiB）；10月release、当前依赖与既有备份保留，JAR/首页哈希及服务PID不变。历史9月目录不能直接回切，详见[清理验收](server-cleanup-20261015.md)。

## 优先待办与长期风险

教材已随 1.30.61 上线并在 1.30.62 补充阅读缩放，剩余 P3（书架封面缓存、`/pages` 死代码、ETag 强比较、`no-cache` 回滚成本）按需发布后处理；课程教材绑定、学生端入口与教师端整册/本课 PDF 下载已于本机实现并验收，待用户验收后决定 130 发布（需备份、执行两列增量 SQL、重启后端并同步前端，PDF 缓存目录需可写）；本地课堂推进和题库切页修复仍待用户验收，未发布。1.30.55 已修复无 `student` 角色学生的作品预览/下载权限，6,839 个缺角色账号未批量改数据；无角色学生真实浏览器复验、文件自主重交与教师重新批改仍待有效正式学生账号。此前机位和打字的学生及机房验证待补；期限/PPTX既有成果保持，其他历史期限/未知形状不批量重算。原有待办如下。

1. 获取有效正式学生账号后验证下载、Windows认领、换人和身份切换；随后进行机房 LNA/HTTP权限、异机提醒、250机同传/还原唯一性、自启和整班性能验收。
2. 单独修复固定A4内容流裁剪导致的页图截底，保护合法局部裁剪与原件/成绩；Linux字体分页、文本框溢出和其他历史预览继续分项核对。
3. 普通课程打字延迟已复现，等待用户确认最小修复后实施；登录优化不能视为练习性能问题已解决。
4. 118条历史空作品状态（SUBMITTED115/GRADED3）仍按原文记录待治理；先核对作品、评分和版本，再制定可回滚修正，不能直接批量清分。历史课程时间缺失、归属/跨校指派冲突不得用当前时间伪造修复。
5. 三厂商真实模型调用/评分准确率、旧DATA_LAB快照、旧AI空评分和专项全链复核按专题任务处理；API通过不等于真实机房或完整业务验收。

## 文档维护与追溯

- 核心文档约150行以内，更新时替换旧状态；架构索引只提供任务入口。详细过程、哈希、回退、旧版本放专题/发布日志，不逐轮堆到启动必读文件。
- 重大业务变更同步相关架构、requirements/design/tasks/ADR；按编译/定向测试→接口→必要浏览器验收记录真实结果和跳过项。
- [v4.47完整归档](PROJECT_HISTORY_20261015.md)、[更早核心归档](PROJECT_HISTORY_20261005.md)、[历史事实索引](context.md)、[发布日志](RELEASE_LOG.md) 均按需读取，历史工具指令不代表当前工作要求。
- 凭据仅在 `secrets.local.md` 或环境变量，不写入文档、日志或交接；AOCI停用决策见 [ADR-002](../docs/architecture/ADR/ADR-002-aoci-project-memory.md)。
