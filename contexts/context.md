# 信息科技学业测评平台 (Context)

> **历史事实索引（2026-08-20 起不再作为默认入口）**：新接手 AI 必须先读 `contexts/PROJECT_CORE.md` 和 `docs/architecture/INDEX.md`。本文件保留原始发布、排障和回滚证据；其中出现的旧“当前接力焦点”均为当时快照，不能覆盖最新核心事实。遇到冲突时，以当前代码、可复核部署/数据库证据和有日期的最新记录为准。

> **版本**：v3.29
> **更新时间**：2026-08-21
> **核心定位**：中小学信息科技 **教学 + 多维度测评**（选择/判断/操作/打字、批改、学情、导学单、区域抽测）。  
> **文档用途**：多 AI / 人工接力的 **业务真相**；操作纪律见 `AGENTS.md`。  
> **文档恢复**：2026-07-22 按方案 A，以热修/发布壳为底座，从 Git `main`（`a88cdcd` 重写前完整版，约 1113 行）回填业务语言、角色、流程、库表、规则与技术细节；机位锁等否决项仅作摘要，不恢复为待实现。

> **课程级物联开关与协作体验修复发布（2026-08-21，已上线）**：正式 release `D:\program\3009dazipingtai\releases\20260821_iot_course_switch_v1`（backend/config/frontend）。物联入口由全局显示改为课程级开关：`sql/iot_course_switch_v1.sql` 给 `biz_lesson` 增加 `iot_enabled`（幂等，按已有 `biz_iot_experiment` 回填；后检 3 门课开启、实验课未开启为 0），教师课程设计器新增「开启物联网」开关（考勤课强制关闭），教师首页课程卡片底部入口条与学生首页按钮按开关显示，学生页带 `lessonId` 进入；教师物联页新增「学生数据收集」卡（小组统计 + 消息分页 + 类型/关键词过滤，接口 `GET /business/iot/experiments/{id}/messages`）。协作侧修复「协作文档版本已变化」频繁提示（保存链串行化 + 版本 CAS 失败时重拉会话重试一次，此前 3010 线上为无重试旧构建，15 秒自动保存必带旧版本号）与 OnlyOffice 参与人身份显示（学生 a 编辑却显示学生 b、教师只看到自己），并新增在线成员列表与版本历史接口 `GET /business/collaboration/room/{roomId}/revisions`。
> 写库前整库备份 `D:\program\3009dazipingtai\backups\20260821_185607_before_iot_course_switch_9f29fff\ry-vue_full.sql`（140,735,436 bytes，SHA-256 `88F1BA8F24CC9ED3FB9A5C7FF2F4C8B05E229286365021691A51323D12B44664`）。新 release JAR SHA-256 `BC48A6F484CE7813AB7D196D99E49AA27DDF19EA456514F31654AF70ECD55F57`，application.yml `09C7574BBAC83192D5C5350F91D8347AEE14E3F8FD2CD19C07EBD8A21EA2067A`，前端 616 文件与本地一致。NSSM `NewDaziBackend3009` 已指向新 release 并重启，环境变量新增 `IOT_MQTT_ENABLED=true`、`IOT_MQTT_USERNAME/PASSWORD`（platform_iot_subscriber，凭据见 secrets.local.md）、`IOT_PASSCODE_SECRET`（与历史默认值一致以兼容存量密文）、`IOT_EMQX_API_KEY/SECRET`；外置 application.yml `iot.mqtt.enabled=true`、broker `tcp://10.52.1.129:1883`。3009 HTTP 200，日志确认「物联网 MQTT 接收器已连接 broker=tcp://10.52.1.129:1883 subscription=county/#」；3010 Nginx 已切新前端并 reload（线上 index.html SHA-256 与新版一致）；生产 API 验证：学生 2020710101 current-lesson `iotEnabled=true`、教师课程详情 252 `iotEnabled=True`、实验 1 消息接口 200（共 0 条，待设备上报）。
> EMQX（10.52.1.129）管理 API 18083 由仅本机改为开放给内网：容器以相同镜像/环境/挂载重建，旧容器改名 `school-emqx-poc-bak-20260821` 留作回滚；`acl.conf` 替换为「订阅账号只读订阅 county/#、`class_*` 设备收发、device01 测试、deny all」（备份 `/srv/emqx-school-poc/etc/acl.conf.bak-20260821`）；订阅账号与 `dazi-backend` API 密钥均已生效。CryptPad 参与人身份修复通过服务器侧只读挂载 `/srv/cryptpad/patches/inner.js`（SHA-256 `72CD97D17E6646D5BA0704F39B58EF6E22261A260623481A2F12FB2218CB94B3`）覆盖容器内 onlyoffice inner.js（成员表名字优先），compose 备份 `docker-compose.yml.bak-20260821`，容器重建后 healthy，公网入口已验证返回补丁内容。
> 回滚：NSSM 切回 `20260820_iot_scheme_a_v1` jar 路径并移除新增环境变量后重启；数据库执行 `sql/iot_course_switch_v1_rollback.sql`；EMQX 容器 rename 回退或恢复 acl 备份；CryptPad 恢复 compose 备份后 `docker compose up -d`。剩余：教师收集页需真实设备上报验证；协作多人编辑身份与保存版本递增待真实同班账号复测。

> **初中物联网功能（方案 A）线上正式部署上线与全链路验收完成（2026-08-20）**：按用户最终确认的“方案 A”，正式 MQTT Broker 使用标准 EMQX（`10.52.1.129:1883`），原 SIoT（`10.52.1.123:1883`）保留为回退链路。同班学生共享班级账号 `class_{deptId}_{entryYear}_{classCode}` 与 6 位易读课堂口令（排除 0/O/1/I/L），按学号自然升序自动连续分组生成持久快照，Topic 遵循 `county/{schoolId}/{courseId}/{classCode}/{experimentId}/group{groupId}/data` 规范；Broker 层面通过 ACL 限制班级前缀，班级内部靠 Topic 业务隔离。完成 EMQX v5 管理 API 适配器（Java 8 兼容）、增量数据库迁移（`biz_iot_class_config`、`biz_iot_group_student`、`biz_iot_group` 扩展 `group_no/topic/last_seen_at`）、AES-256-GCM 口令加密存储、教师端班级分组控制台/口令轮换/投屏配置卡、学生端 `/student/iot` 看板及一键复制 Mind+ 配置。后端 324/324 单元测试通过。
> 正式环境发布 release 为 `D:\program\3009dazipingtai\releases\20260820_iot_scheme_a_v1`；后端 JAR SHA-256 为 `C826DF4F4F5B1C806A7ED91B43541EFFC1B9F443CF9EBEF2AB6C4A1C1F2CBE14`，前端构建压缩包 SHA-256 为 `B9130E008B12AF19E9C668C60600484BE11E2D07C2372B2CA9882B6286AFCB87`。写库前整库备份位于 `D:\program\3009dazipingtai\backups\20260820_iot_scheme_a_v1\ry-vue.before.sql`（80,507,916 bytes，SHA-256 `FCE5EAA1FB402C9BEF172F669150A2E14C57FEFC6F235B20679E6A945B95C5C3`）。增量 SQL `sql/iot_class_grouping_v2.sql` 执行成功。NSSM `NewDaziBackend3009` 已切换并重启（HTTP 200），3010 Nginx 切换并 reload 成功（HTTP 200）。已在生产环境完成真实端到端 API 验证：教师 `19157727791` 登录成功、查询课程 252 班级、创建实验、对 2020 级 7 班（43 名学生）执行方案 A 自动分组生成 11 个小组与 6 位口令、获取课堂投屏配置卡（Broker: `10.52.1.129:1883`）、学生 `2020710701`（陈语馨）登录并获取个人物联网看板（自动匹配所属第 1 组、4 位同组组员、专属发布 Topic `county/139/252/2020-07/iot_demo_exp/group01/data`）。各项接口全部 HTTP 200。旧 release `20260820_python_practice_preview_fix_v1` 保留可秒级回滚。

> **登录与全局署名调整（2026-08-20）**：Vue3 前端已移除所有页面右下角及登录页原有的“开发支持：象山县-郑东旭”展示，不再保留开发主页跳转入口。登录页底部改为低对比度两行版权、地址、邮编和备案信息；注册页同步移除旧署名。教师、教研员登录后的公共内容框架在内容末尾显示一次低对比度版权、地址和备案信息，使用自然文档流而非悬浮定位，学生端不显示。仅涉及前端展示，尚未上线，无后端、数据库、权限或部署配置变更；Vue3 `npm run build:prod` 已通过。

> **Python 刷题预览与系统题起始代码修复（2026-08-20）**：系统公开 Python 题的“查看题目”原先复用了编辑权限，教师预览他人创建的公开题会被错误拒绝。已发布后端 `D:\program\3009dazipingtai\releases\20260820_python_practice_preview_fix_v1`，JAR SHA-256 为 `8A06807E6E778178D4981E2843285E0CA4CBBBD7A23045B62F26E9BC4276A098`；公开题现在可查看题面、配置说明和公开样例，私有题仍仅创建者或管理员可预览，隐藏测试点不返回。后续发现 80 道系统题及 1 份已有题单快照的起始代码被导入为问号，已在正式库 `ry-vue` 备份 `D:\program\3009dazipingtai\backups\20260820_105436_python_starter_code_repair_v3_8223a3b\ry-vue.before.sql`（90,751,028 bytes，SHA-256 `8D24301B459ED73C28C10C65471DD1F78605EE9E679138CD2A963AD023DCC904`）后执行 `sql/python_system_questions_starter_code_repair_v3.sql`（SHA-256 `3C9E4743CE36E634736F896EBEC6F6BBB007FF9145B4D42A76FC6035F41044B0`）。后检为配置乱码 0、快照乱码 0；正式教师重新打开题目 1764 的预览，已显示“# 请根据题目要求编写程序”，截图为 `output/playwright/python-practice-production-starter-code-repair-v3.png`。该 SQL 仅替换带 `?` 的系统题起始代码，不改题面、测试点、题单结构、学生草稿、提交或成绩；后端无需重启，NSSM `NewDaziBackend3009` 仍为 `SERVICE_RUNNING`。若需回退，使用上述整库备份恢复，或只将受影响两表的 `starter_code` 恢复至迁移前值；仍未完成真实班级规模压测。

> **在线协作身份与兼容性修复发布（2026-08-20）**：后端协作会话现在优先将学生显示名生成为“学号 姓名”，教师使用系统昵称，缺失字段时降级为可辨认的通用名称，不再把内部用户 ID 拼进编辑器显示名；姓名中的换行/制表符会被清理。前端新增 WebAssembly/WebSocket/Blob 能力检查、CryptPad 集成脚本错误提示、20 秒 iframe 加载超时和诊断信息复制降级，避免老浏览器无限“加载中”或在诊断按钮上二次报错。后端 `mvn -pl ruoyi-business -am test -DforkCount=0` 为 317/317，Vue3 `npm run build:prod` 成功，后端 clean package 成功。正式 release 为 `D:\program\3009dazipingtai\releases\20260820_collaboration_identity_compat_v1`，JAR SHA-256 为 `C070B36A3F349898F3D2B8E6400A9EEAB16B8F47CBB6A8929B6852606A912FDA`，首页 SHA-256 为 `52548590AF791158ED6A6EB31C0215A98BACF51972646ED0B4311FA07798E769`；NSSM 为 `Running`，Nginx `-t`/reload 成功，服务器内外 3009/3010 和验证码接口均 HTTP 200。发布前端、Nginx 和 NSSM 注册表备份位于 `D:\program\3009dazipingtai\backups\20260820_collaboration_identity_compat_v1`，旧 release `20260820_python_practice_ux_v1` 保留可回滚。正式学生账号 `2020710101` 登录成功但当前协作房间数为 0，教师测试账号访问房间 17 按学校边界拒绝；尚未取得真实同班学生账号完成多人同步、刷新保留和 onSave 版本递增闭环，不能据此宣称机房 40 人现场已全部解决。

> **在线协作入口与旧 Chrome 提示优化（2026-08-20，开发中）**：按用户确认的方案 A，课程设计器将“开启协作”放入文件作品的“预览/移除”之间，开启后复用既有协作服务按课程已指派班级创建独立房间；教师首页仅对存在开放房间的课程显示“在线协作”，点击后选择班级进入。教师/学生会话新增不暴露内部 ID 的稳定参与者标识并同时传入显示名，CryptPad 编辑器继续使用其原生成员列表。针对现场低版本 Chrome 解析 CryptPad class field 时报 `Unexpected token '='` 的情况，脚本加载前增加语法门禁并提示升级 Chrome 或使用 Edge。当前本机后端测试 318/318、Vue3 生产构建成功；正式发布和真实教师/学生回归待完成。

> **审计修复发布补充（2026-08-20）**：正式 JAR 已因 IoT 关闭状态下定时任务误查未迁移表而补发，修复 `IotCredentialExpiryJob` 在 `IOT_MQTT_ENABLED=false` 时直接跳过查询；修复后 JAR SHA-256 为 `FC4D2727B590359D98616A1C4553C1641BF52505C645F7517FD52326EC4F7EF5`，NSSM `NewDaziBackend3009` 为 `RUNNING`，启动日志显示 MQTT 接收器未开启、数据库初始化成功，未再出现 `biz_iot_device` 不存在错误。其余本段验收结论不变。

> **IoT EMQX 现场兼容与数据库热修（2026-08-20）**：用户现场确认掌控板使用 Mind+ 的 SIoT 模块连接 `10.52.1.129:1883` 成功，证明 Mind+ 可直连标准 EMQX Broker；原 `10.52.1.123` SIoT 未修改，保留为回退。129 的 `school-emqx-poc` 容器运行、`emqx-school-poc.service` 为 active，1883 仅允许教育网 `10.52.0.0/16`，123→129 TCP 探测成功。平台物联页此前因正式 `ry-vue` 尚无 `biz_iot_*` 表报错；已先完成正式整库备份 `D:\program\3009dazipingtai\backups\20260820_iot_schema_v1\ry-vue.before.sql`，SHA-256 `1B427F1F0A5F492C34D3073083D29BCABC496DB4BB9C17B1BD0E4E1FC49A4983`，执行 `sql/iot_mqtt_integration_v1.sql` 后检 5 张表全部存在，未重启服务、未启用 MQTT receiver、未改 EMQX。下一门禁是按 EMQX API 实现每设备独立账号、强密码及精确 Topic ACL；不得使用共享 SIoT 弱账号作为多校正式方案。

> **在线协作学生路由热修（2026-08-20）**：学生从 `/student/collaboration/{roomId}` 点击“进入协作编辑”原先错误跳到教师专用 `/business/collaboration/editor/{roomId}`，被 `permission.js` 的学生路径守卫重定向回 `/student/index`。已新增学生专用 `/student/collaboration/editor/:roomId` 路由并改正按钮跳转；后端 `CollaborationRoomService.requireRoomAccess` 未放宽，仍按当前课程、学校、届别和班级校验。Vue3 生产构建成功，前端压缩包 SHA-256 为 `BCB506A1DD74DA0791667958D3F43DB098573EF77B625322BB9B3B2CD5656EC7`；已发布到 `10.52.1.123` 活动 release `D:\program\3009dazipingtai\releases\20260820_audit_fix_v1\frontend`，备份目录为 `D:\program\3009dazipingtai\backups\20260820_collaboration_student_route_fix\frontend.before`。3010 Nginx 使用 `D:\programsoftware\nginx\nginx-1.29.4\conf\nginx.conf` 检查成功并 reload，`http://10.52.1.123:3010/` 和 `/prod-api/captchaImage` 均 HTTP 200。线上学生冒烟已确认点击后 URL 保持 `/student/collaboration/editor/17`；测试账号不属于房间 17 当前班级，后端按预期拒绝，尚缺有该房间权限的真实同班学生完成“进入编辑器、多人同步、刷新保留、onSave 回平台版本递增”闭环，不得用跨班账号代替。

> **IoT 现场口径（2026-08-20）**：现场掌控板继续连接正式 SIoT `10.52.1.123:1883`，不切换到 `10.52.1.129`；129 的 1883 即使从部分网络可达，也只代表测试端口连通，不代表已获准承载生产订阅。当前不得要求为本轮正式现场专门开放 129:1883，也不得启用 `IOT_MQTT_ENABLED` 或执行 `sql/iot_mqtt_*.sql`。现场最短验收为：掌控板连 123:1883、连续发送 10 分钟、断网自动重连、记录消息到达率；只有 SIoT 独立账号/ACL 认证问题解决后，才做平台接收、教师实时显示和跨班/跨校拒绝闭环。当前从验收电脑到 123:1883、129:1883 均可建立 TCP，129 SSH 暂不可达，故 129 服务状态仍需信息中心另行确认。

> **在线协作临时部署事实（2026-08-19）**：按用户明确要求，扩展服务器 `10.52.1.129` 的 CryptPad `2026.5.1` 已切换为内网 HTTP：`office.xsedu.net.cn` 与 `office-sandbox.xsedu.net.cn` 使用 `http://`，WebSocket 使用 `ws://`。服务器容器为 healthy，本机 `/checkup/` 和 `/cryptpad-api.js` 返回 200；当前验收电脑访问 `10.52.1.129:80` 仍超时，网络/防火墙路径尚未放行。HTTP 明文传输，仅允许内网临时测试；恢复可信 HTTPS 前不得宣称正式可用。2026-08-20 已部署 CSP 修复并完成教师房间 17 编辑器加载验收，真实多人同步和回平台保存仍待补验。

> **在线协作 HTTP 入口修复（2026-08-19）**：机房网络打通后，教师页面提示“CryptPad 集成脚本加载失败”。SSH 核查确认 CryptPad/Nginx 正常，根因是正式平台 `10.52.1.123` 的 NSSM 后端仍下发旧 `https://office.xsedu.net.cn/cryptpad-api.js`。已备份服务环境参数，将 `CRYPTPAD_BASE_URL`、`CRYPTPAD_API_URL` 改为 `http://` 并重启 `NewDaziBackend3009`；服务恢复 `SERVICE_RUNNING`，服务器访问 HTTP 脚本和 `/checkup/` 均 200。教师机需使用 `http://` 平台并强制刷新后重试；尚未完成真实多人编辑和保存闭环验收。

> **在线协作 iframe 拒绝连接修复（2026-08-19）**：脚本地址修复后，教师页面出现“office.xsedu.net.cn 拒绝连接”。原因是 CryptPad 上游 Content-Security-Policy 的 `frame-ancestors` 只允许自身 `office` 域名，未允许平台 `xxkj.xsedu.net.cn` 嵌入。已在 `10.52.1.129` 的 CryptPad Nginx 隐藏上游 CSP 并补充 `http://xxkj.xsedu.net.cn`、`http://10.52.1.123:3010`，`nginx -t` 成功并 reload；响应头复核已包含平台来源。教师机需强制刷新后再次进入房间。

> **在线协作嵌入开关诊断（2026-08-19）**：强制刷新后弹窗变为“此 CryptPad 实例禁用嵌入”。SSH 读取 `http://office.xsedu.net.cn/api/config` 确认 `enableEmbedding: false`，这是 CryptPad 实例管理开关，和浏览器控制台中的 JSON 解析/字体 304 次要日志无关。需在 `/srv/cryptpad/data/decrees/decree.ndjson` 追加官方 `ENABLE_EMBEDDING` 管理记录并重启/复核 `api/config` 为 `true`；本次因跳板 SSH 临时连接层错误尚未成功执行，当前不得宣称嵌入已恢复。

> **在线协作嵌入开关已修复（2026-08-19）**：通过 `10.52.1.123` 跳板 SSH 核查发现 decree 最后一行曾被写坏为带多余字符的 NDJSON；已保留坏文件副本，使用干净备份恢复 `/srv/cryptpad/data/decrees/decree.ndjson`，追加合法 `ENABLE_EMBEDDING=true` 记录并重启 `cryptpad-2026-5-1`。容器恢复 `healthy`，`http://office.xsedu.net.cn/api/config` 返回 `"enableEmbedding": true`，响应 CSP 已同时允许 `http://xxkj.xsedu.net.cn`。尚未完成教师/学生真实多人编辑和保存闭环验收，教师机需用 HTTP 平台强制刷新后复测。

> **当前接力焦点（2026-08-19：初中物联网标准 MQTT Broker 与 Mind+ 并行兼容验证）**
> 0. 原 `10.52.1.123` 的 SIoT 2618 和 `CountySIoTPoc` 未修改、仍可回退使用；因其新增独立强密码账号实际认证失败，不能承载 P2 正式的临时凭据与设备 ACL。
> 1. **历史 PoC 记录**：曾在扩展服务器 `10.52.1.129` 新建隔离的 EMQX 5.8.8 测试实例 `school-emqx-poc`，用于兼容性验证；该路线后续按 P2 决策停用，不作为正式平台 Broker。现有 CryptPad、Judge0 与正式平台未因该 PoC 改切。
> 2. 仅监听 `10.52.1.129:1883` 供教育网 `10.52.0.0/16` 使用；管理端仅绑定 `127.0.0.1:18083`，未开放 TLS `8883`。持久防火墙规则为允许该教育网段访问 1883、拒绝其他来源。平台服务器 `10.52.1.123` TCP 探活成功。
> 3. Broker 实测：错误密码得到 MQTT CONNACK 4；测试设备可订阅自己的 `county/test/device01/#`，订阅其他设备 Topic 得到 SUBACK 128。测试账号仅保存在服务器受限目录与本机 `contexts/secrets.local.md`，未写入 Git、日志或本文件。
> 4. **历史门禁已关闭**：当时曾计划让实体掌控板临时改连 `10.52.1.129:1883` 验证 Mind+ 兼容；后续用户确认继续使用 SIoT，故不再以该 PoC 作为正式发布前置条件，正式现场统一执行 `10.52.1.123:1883`。
> 5. 部署前容器检查备份为 `/srv/emqx-school-poc/backups/20260819_085141_before_school_expose/container-inspect.json`，SHA-256 `318F087ADA131B53533695228C51B01EC740FB5F066738108A3D91C3299992CC`。回滚为禁用/停止 `emqx-school-poc.service` 并移除两条 `DOCKER-USER` 的 1883 规则；不删除数据目录，原 SIoT 可立即继续使用。

> **当前接力焦点（2026-08-19：Python 刷题已完成正式迁移、发布、真实 Judge0 与浏览器验收）**
> 0. 已按独立业务域落地：不写 `biz_student_answer`、不改变课程成绩和课程推进；教师一级菜单为“Python 刷题”，学生独立入口为“Python 练习”。
> 1. 已备份当前开发库：`backups/20260819_134719_before_python_practice/`；已执行 `sql/python_practice_v1.sql`，11 张独立刷题表和 4 项菜单/权限写入成功；已执行 `sql/python_system_questions_v1.sql`，导入 80 道系统题、80 条配置和 160 个测试点，参考解法本机 `160/160` 通过。
> 2. 已创建本机验证题单：计划 ID `1`、版本 ID `1`、5 道基础题；学生 `2020710101` 可见 5 道题。学生正式提交第一题后得到 `ACCEPTED`、100 分、2/2；数据库存在 1 条 `biz_python_practice_submission` 和 1 条 `biz_python_practice_progress`，该题 `biz_student_answer` 仍为 0 条。
> 3. 学生控制器 4 个接口已从系统角色注解改为项目既有 `@studentSs.isStudent()`，解决本机学生账号未维护 `sys_user_role` 时的权限阻断；教师校区选择、教师 `/python-practice` 工作台和学生 `/student/python-practice` 页面均已完成 Playwright 冒烟。截图和脱敏报告在 `output/playwright/python-practice-local-*.png/json`。
> 4. 本机与正式验证证据：`mvn -pl ruoyi-business -am test -DforkCount=0` 316/316 通过；后端 `clean package -DskipTests` 和 Vue3 `npm run build:prod` 成功（仅既有 vform eval 与大 chunk 警告）。正式库 `ry-vue` 已完成 11 表、4 菜单/权限、80 题、80 配置、160 测试点迁移；正式整库备份为 `D:\program\3009dazipingtai\backups\20260819_151440_before_python_practice\ry-vue.before.sql`，SHA-256 `86ED5EA5C283B9AD23661225C50FE08B33E0E2EC0B265F1C1293201A5464D498`。
> 5. 正式发布已完成：release `D:\program\3009dazipingtai\releases\20260819_python_practice_v1`；后端 NSSM `NewDaziBackend3009`、前端 Nginx 3010 均已切换并探活 200，3012 仍为 200，80 端口独立 OpenResty 未修改。Judge0 使用正式 HTTP 地址 `10.52.1.129:2358`，Token 仅保存在服务器配置。
> 6. 真实链路已通过：正式教师创建一题验收题单并发布，正式学生 `2020710101` 对系统题 `1754` 单次提交 `ACCEPTED / 100 / 2/2`；随后 3 个并发提交全部 `ACCEPTED / 100 / 2/2`，独立进度为 4 次、最高分 100。教师 `/python-practice`、学生 `/student/python-practice` 浏览器冒烟无 page error，截图和脱敏报告位于 `output/playwright/python-practice-production-*.png/json`。
> 7. 验收数据已按题单/版本/学生/题目精确清理，清理后 Python 独立表和 `biz_student_answer` 均为 0；正式教学数据未创建。回滚备份位于 `D:\program\3009dazipingtai\backups\20260819_python_practice_v1_release_switch`，保留旧后端 `20260819_platform_update_v1`、旧前端 `20260819_platform_update_v5`，不删除旧 release 或备份。剩余风险仅为尚未进行高并发压测和班级规模压测。
> 8. **2026-08-19 Python 刷题体验修正已正式发布**：题库管理新增“操作方式”筛选和列，可区分 `PYTHON` Python 在线编程与 `FILE` 文件作品；公开题查询兼容历史 `Y` 与系统题导入使用的 `1`。题单新建只显示“年级、题单名称”，后端固定学期为 `0` 并按当前学年、学段自动推算 `entry_year`；配置题目和班级学情分别使用可搜索题库、负责班级可视化选择，不再手填编号或调用 `window.prompt`。后端 317 项测试、Vue3 生产构建和最新 `clean package` 均通过。
> 9. 正式 release 为 `D:\program\3009dazipingtai\releases\20260819_python_practice_ui_v2`；后端 JAR SHA-256 为 `8620553864AA05B6BD708489A2F146A2134E4A7BAB99B8B099FCE6779BAF7EAA`，前端 `index.html` SHA-256 为 `CA4B27CB2294A5CB2381D37463E6599E0C9403B435AE869844AA1E08DE20CC2D`。正式库 `ry-vue` 整库备份为 `D:\program\3009dazipingtai\backups\20260819_python_practice_ui_v2_before_switch_retry\ry-vue.before.sql`，SHA-256 为 `F66EF3CCBE0D700F3FC9C6141079B7C569C91FE5311894947D2BF3B17D1F8487`。
> 10. 已执行 `sql/python_practice_menu_text_v2.sql`：前检 4 个 `business:pythonPractice:*` 菜单均为问号乱码，后检已恢复为“Python刷题 / Python刷题配置 / Python刷题发布 / Python刷题学情”，UTF-8 十六进制复核正确。NSSM `NewDaziBackend3009` 已切换且为 `SERVICE_RUNNING`，3010 Nginx 已 `-t` 和 reload 成功；外部 `10.52.1.123:3009`、`3010` 和 `3010/prod-api/captchaImage` 均为 HTTP 200。管理员登录后，`practicalMode=PYTHON` 题库查询返回 200，当前总数为 81。Nginx 与 NSSM 切换前配置保存在上述备份目录，旧 release `20260819_python_practice_v1` 已保留；回滚时恢复该目录的配置，将两个服务路径改回旧 release 后重启/reload。高并发和真实班级规模压测仍未执行。

> **当前接力焦点（2026-08-19：平台更新日志已正式发布）**
> 0. 用户确认采用独立“平台更新”菜单：教师、教研员、管理员可查看时间轴；仅管理员可新增、编辑、发布、撤回和转草稿。页面不区分更新类型和影响范围，记录只保留版本号（如 1.0.0）、实际发布时间、标题和面向用户的更新说明。
> 1. 已在正式库 `ry-vue` 执行 `sql/platform_update_v1.sql`：创建 `biz_platform_update` 表，写入 2025-08-31 至 2026-08-18 的 8 条 Git 历史整理版本，并幂等写入 `sys_menu` / `sys_role_menu` 权限。后检为已发布记录 8 条、主菜单 1 条、权限菜单 4 条。
> 2. Vue3 新增 `views/business/platformUpdate/index.vue` 和 API，使用 Element Plus 时间轴；后端新增更新记录领域对象、Mapper、Service 和 Controller。教师/教研员只查询 `PUBLISHED`，管理员可查看全部状态并维护记录；版本号由后端校验为三段数字格式。
> 3. 验证证据：`mvn -pl ruoyi-business -am test` 316/316 通过；后端 `clean package -DskipTests` 成功；`RuoYi-Vue3/npm run build:prod` 成功，仅有既有 vform eval 与大 chunk 警告。正式后端发布到 `D:\program\3009dazipingtai\releases\20260819_platform_update_v1`，最终前端静态文件发布到 `D:\program\3009dazipingtai\releases\20260819_platform_update_v5\frontend`；NSSM 与 Nginx 已切换，3009/3010 均 HTTP 200。v5 已移除与数据库菜单 `/platform-update` 重复的静态路由，避免教师从左侧菜单进入空白页面；用正式教师账号登录、点击左侧“平台更新”复测，页面显示 8 条时间轴记录，`GET /business/platform-update/list` 返回 HTTP 200，无控制台错误。新后端启动时需先完整停止旧 NSSM 服务，避免端口抢占。
> 4. 自动记录边界：Git push 不等于线上发布，系统不自动把 push 直接展示给用户；后续每次 AI 完成真实发布并通过接口探活后，应按本模块新增一条记录（先草稿，确认发布后置为 PUBLISHED）。管理员可在页面修订措辞、补录服务器维护并撤回误发记录。
> 5. 正式备份与回滚：整库备份为 `D:\program\3009dazipingtai\backups\20260819_platform_update_v1\ry-vue.before.sql`，SHA-256 `02FACC14858E382A3AA2F89CD48B8DE282103B137A7E1ECD2DF2908BF5526A95`；同目录保留 Nginx 配置和 NSSM 参数；旧后端 `20260818_180512_python_judge0_student_experience_v2`、旧前端 `20260818_223500_python_judge0_draft_dedup_fix_v1` 未覆盖。回滚时恢复备份的 Nginx 配置、NSSM `AppDirectory/AppParameters`，执行 `nginx -t`、reload 并启动旧服务；本次 SQL 为幂等新增，无需回滚删除。

> **当前接力焦点（2026-08-19：初中物联网 P2 代码完成、正式上线阻断）**
> 0. 用户现场确认掌控板已显示“MQTT 连接成功”，此前由教育局信息中心放行的网络端口现已可用；P1 的真实硬件 MQTT 连接门禁关闭。
> 1. 进入当前 RuoYi/Vue3 平台内的 P2 最小接入：保留 Mind+ 和 SIoT 2618，平台后端订阅 MQTT，浏览器只访问平台 API/WebSocket/SSE，不直接连接 Broker 管理接口。
> 2. P2 范围为课程内实验、班级/小组/设备、服务端 Topic、短期设备凭据、数字文本/JSON 消息接收与存档、教师实时数据和中文分层诊断；权限链必须覆盖学校→课程→活动→班级→小组→设备。
> 3. 2026-08-19 用户确认先继续使用 SIoT，不推进 EMQX；`10.52.1.129` 上的 EMQX 测试服务已停用，新增防火墙规则已删除，原 `10.52.1.123:1883/18080` SIoT 未修改。全县并发路线统一使用 `10.52.1.123:1883`，由平台生成 `county/{学校}/{课程}/{班级}/{实验}/{小组}/{设备}/data` Topic 和权限隔离，新建实验不创建端口。
> 4. P2 代码、SQL、模拟器、限流/异常记录、后端测试和 Vue3 构建已完成；本地备份及正式库备份已保留。正式上线尚未完成：SIoT 2618 新增账号写入后实际 MQTT 认证返回 CONNACK 5，故未切换正式后端，避免未验证凭据上线。SIoT 已恢复原始备份并保持 CountySIoTPoc Running；后续需先解决 SIoT 账号/ACL 管理兼容，再执行正式库迁移、发布和真实平台接收验收。
> 5. 产品入口已补入教师首页课程卡片：普通课程新增“物联”操作，跳转 `/business/iot?lessonId=...` 并自动带入课程；物联网页不再允许教师手填课程 ID。Vue3 `npm run build:prod` 已再次通过。该入口尚未发布到正式 3010，且不改变 SIoT 认证阻断结论。

> **当前接力焦点（2026-08-18：Python/Judge0 学生真实链路已通过）**
> 0. **线上修复与发布**：正式学生首页曾因 `StudentProgrammingQuestion` 模板误引用不存在的 `history`，触发 `Cannot read properties of undefined (reading 'length')`，Python 卡片无法渲染。已改为 `historyList`，并增加已保存代码去重保护，避免自动保存后手动保存被重复提交拦截器误报失败。前端已发布到 `D:\program\3009dazipingtai\releases\20260818_223500_python_judge0_draft_dedup_fix_v1`；后端未重启，Nginx 候选/正式检查和 reload 均通过，3010 HTTP 200。
> 1. **正式真实验收**：正式学生账号进入课程 `270 / python`、题目 `1754` 后，编辑器显示 `main.py`、题目说明、公开样例、运行/提交结果和历史提交；保存草稿成功，公开示例 `Accepted` 且输出 `hello world`，正式提交隐藏测试 `2/2`、状态 `Accepted`、得分 `100`，学生页“我的得分”同步为 `100`。随后又验证了错误代码返回 `SyntaxError`、死循环返回 `Time limit exceeded`，恢复初始代码后可再次运行；连续保存两次无误，刷新页面后草稿仍为 `print("hello world")`，成绩仍为 `100`。重复正式提交会生成独立历史记录，但每次均为 `ACCEPTED / 2 / 2 / 100`，符合允许学生重提的现有模型。数据库复核：`biz_programming_draft` 存在该学生/课程/题目草稿，`biz_student_answer` 的该题 `score=100`、`is_correct=1`。
> 2. **响应式与登录核验**：1366×768 桌面和 390×844 移动验收均无横向溢出，Python 编辑器、操作按钮、结果和历史区域可见；线上 `captchaImage` 返回 `captchaEnabled:false`，学生登录页无验证码输入框。之前控制台中的 `history.length` 错误均为旧 release 日志，修复后页面正常渲染；尚未发现新同类错误。
> 3. **剩余风险**：本轮真实账号已产生课程 270 的草稿、公开运行记录、错误/超时运行记录和正式提交/成绩，这是用户明确授权的验收数据；班级高并发压测和取消后重提压测仍未执行。线上页面 22:43 之后未出现新的 `history.length` 渲染错误；浏览器仍保留正式学生页面供用户查看。

> **历史架构记录（2026-08-19：Python 刷题产品方案已归档，现已进入实现阶段）**
> 0. **产品边界**：在现有课程内 Python 操作题之外，新增独立的简化 OJ 风格“Python 刷题”功能。刷题不进入课程总分、不改变课程推进、不改造课程设计器；复用现有 Python 题库、CodeMirror 编辑器、Judge0 判题、异步轮询、取消和脱敏能力。
> 1. **入口与页面结构**：教师端只新增一个一级菜单“Python 刷题”，首屏严格复用教师首页的“年级分组→练习卡→操作”路径，不使用三个页签或报表首屏。每个年级显示一张 Python 练习卡，教师从卡片进入“配置题目”或“查看班级情况”；题库只在配置时作为选择工具打开。学生端顶部新增“Python 练习”入口，进入独立刷题页，一次显示一道题并支持题目列表、未完成/已完成/错题筛选、草稿、示例运行、提交和历史记录。
> 2. **教师配置口径（已确认）**：系统提供默认 80 道 Python 题模板。一个学校、学期、年级使用一套当前发布的“年级基础题单”，基础题统一向该年级全部班级显示；教师可创建“班级加练包”，一次选一个或多个可管理班级并追加题目，不允许删除、替换或隐藏基础题。不同班级可获得不同加练包，学生端分区显示基础题和本班加练；基础题、加练题的学情分别统计，避免不同题量班级误比。管理员/教研员可管理全部范围，教师受学校/年级/班级权限限制。
> 3. **架构归档与待完成事项**：完整可执行的产品规则、数据模型、版本/快照策略、权限矩阵、接口清单、80 题规划、分阶段验收和可扩展边界已写入 `contexts/python-judge0/python-practice-master-prompt.md`；专题需求/设计/ADR/原实施提示词和任务清单已同步。当前实现以本文件顶部的新进度为准，正式题库数据和生产发布仍未进行。
> 4. **UI 原型**：教师端原型位于 `output/playwright/python-practice-ui-prototype/index.html`，已包含年级练习卡、基础题配置、班级加练、多班选择、题库调整和班级明细路径；正式开发前以该原型为讨论基础。学生端沿用同一题单模型，后续另做单题刷题界面。
> 5. **已知样式问题**：现有 CodeMirror 聚焦时 `.cm-focused` 的蓝色 outline 与外层灰色 border 叠加，造成截图中的双层边框；刷题功能开发阶段一并统一为单层焦点边框，但不影响当前课程 Python 判题链路。

> **当前接力焦点（2026-08-18：Python/Judge0 学生体验 v2 已发布，真实作答链路待可用课程账号后验收）**
> 0. **发布结果**：正式后端和前端已切换到 `D:\program\3009dazipingtai\releases\20260818_180512_python_judge0_student_experience_v2`；NSSM `NewDaziBackend3009` 为 `Running`，3010 Nginx 已 reload。3009、3010 均 HTTP 200。后端 JAR SHA-256 为 `3AE004B8D16C10230E2045C84C939F1F0EF90553CFA15960299C36E7F23C5D50`，前端 `index.html` SHA-256 为 `BCA18E8B50E1D310BAB931C86A494EB3A3CF24FB2D528F1933A446760CB47392`。
> 1. **数据库与回滚证据**：正式库已执行 `python_judge0_student_experience_v2.sql`，`biz_programming_question_config` 的 `input_description`、`output_description`、`sample_explanation`、`constraints_text`、`notes_text` 五个字段后检均存在。发布前 5 张编程表备份位于 `D:\program\3009dazipingtai\backups\20260818_180512_python_judge0_student_experience_v2_before_switch_from_173500\programming_tables.sql`，SHA-256 为 `893D26AB6D073B8FE07EA249663297ABA40989D68963F6818B5746DF942FBCD3`；旧 Nginx 配置、NSSM 参数和旧 release 均保留。回滚时恢复该目录中的 Nginx/NSSM 备份，后端切回 `20260818_173500_checkin_dashboard_refresh_v1` 并重启服务，数据库新增字段可保留，不需删列。
> 2. **线上静态包与登录开关核验**：线上首页实际返回 200，活动 release 不存在 `index.html.gz`；未登录调用 `/business/student-home/programming/270/1754` 返回 401。线上 `GET /prod-api/captchaImage` 和后端 `GET /captchaImage` 均返回 `captchaEnabled:false`，正式登录页没有验证码输入框；已部署前端静态 JS 未发现 Judge0 内网地址、2358 端口或 Token 关键词。2026-08-18 已用正式学生账号登录成功，证明当前不是网络或验证码阻塞。
> 3. **剩余门禁**：当前可用验收账号已登录，但只被指派到课程 `13`，未被指派到 Python 课程 `270`，因此不能直接验收题目 `1754` 的保存、公开运行、正式提交和成绩回写。取得课程 `270` 的学生账号或将现有验收账号加入该课程后，继续验收桌面 1366×768、1920×1080 和移动 390×844 的 Python 编辑器、草稿恢复、公开示例运行、正式提交、Judge0 状态、`biz_student_answer`/`practicalScore`/`totalScore` 写回、浏览器请求脱敏，以及班级并发和幂等压测。未完成上述步骤前，不宣称正式学生完整链路已通过。

> **当前接力焦点（2026-08-18：Python 学生编辑器显示故障已修复并真实验证）**
> 0. **故障与修复**：正式课程 `270 / python` 的题目 `1754` 在数据库中为 `practical/PYTHON`、Python 配置已启用；学生页仍显示“选择作品文件”的直接原因是线上实际加载了旧前端静态包。已重新构建 Vue3，学生页同时兼容 `practicalMode` / `practical_mode`，并且仅对作答方式为空的操作题调用平台后端的既有编程详情接口作一次安全核验；核验到 Python 配置才设为 `PYTHON`，普通文件题保持 `FILE`，不会误判。
> 1. **正式发布**：后端和前端当前 release 已迭代为 `D:\program\3009dazipingtai\releases\20260818_173500_checkin_dashboard_refresh_v1`，其中保留本条 Python 修复。NSSM `NewDaziBackend3009` 已指向该 release、状态 `Running`；Nginx 3010 根目录也已指向该 release。为避免 Nginx `gzip_static` 返回遗留的旧首页，本 release 的旧 `.gz` 静态副本已移除，由 Nginx 正常压缩响应；不可把旧 `.gz` 文件与新 `index.html` 混用。
> 2. **真实验收**：正式学生账号“何星瑶”已登录课程 `python`，2026-08-18 浏览器刷新后可见“Python 编程题”、`main.py`、初始代码 `print("hello world")`、`保存草稿`、`运行示例`、`提交判题`，文件上传按钮不再出现。截图为 `output/playwright/python-judge0/student-python-editor-v3.png`。服务器 `3009`、`3010` 均 HTTP 200；后端 JAR SHA-256 为 `06FC4550DC6C86D16C91CBBB1CCDC8A6CBA988BFDE7403DEB2B39963225FDBD5`。未在真实学生账号上点击保存、运行或提交，因此不新增学生草稿、判题记录或成绩；Judge0 的独立真实 `print(2+3)` Accepted 证据仍有效。
> 3. **回滚与剩余门禁**：若需回退，把 NSSM 的 JAR/工作目录和 3010 Nginx root 切回保留的 `20260818_140000_python_mixed_practical_v2`（或更早稳定版），执行 `nginx -t`、reload 并重启 `NewDaziBackend3009`；无 SQL 回滚。尚待在获得学生作答授权后完成保存草稿、公开示例运行、正式提交、成绩回写与班级并发压测，不能把本次界面验收当作该链路已写入成绩的证据。

> **当前接力焦点（2026-08-18：课堂考勤签到总览、学生备注与空课程即时刷新已正式发布并验收）**
> 0. **交互与数据口径**：教师首页的考勤课程点击“签到”后，不再先选班级，直接打开该届可查看班级的签到总览，逐班显示已签到、未签到和班级总人数；点击任一班级才进入学生明细。学生明细新增 `biz_student.remark` 备注列。汇总和名单均以本校、该届且有 `sys_user` 关联的学生为基准，无数据库迁移或数据修改。
> 1. **接口与权限**：新增 `GET /business/lesson/checkin-summary`，后端使用一次聚合查询返回班级统计；既有 `GET /business/lesson/checkin-roster` 同步返回学生备注。管理员和课程创建者可查看本校该届汇总，其他任课教师只返回自己在 `biz_teacher_class` 中管理的班级；进入学生明细仍逐班重复校验，未放宽跨班、跨校访问边界。
> 2. **构建、发布与浏览器验收**：本机 `mvn -pl ruoyi-business -am test` 通过 311/311，Vue3 `npm run build:prod` 成功（仅既有 `vform3` eval 与大 chunk 警告），`git diff --check` 无空白错误。正式版已发布到 `D:\program\3009dazipingtai\releases\20260818_173500_checkin_dashboard_refresh_v1`，3009/3010 均 HTTP 200，NSSM 为 `Running`；JAR SHA-256 为 `3AE004B8D16C10230E2045C84C939F1F0EF90553CFA15960299C36E7F23C5D50`，前端 `index.html` SHA-256 为 `BCA18E8B50E1D310BAB931C86A494EB3A3CF24FB2D528F1933A446760CB47392`。正式教师账号进入初中部课程“暑托6”后，签到汇总接口和名单接口均 HTTP 200；汇总显示 8 个班、总计 41/373 人，名单表实际包含“备注”列及已有备注。访问教师首页并带 `refresh` 查询标记时，线上 `/business/teacher/dashboard-data` 再次返回 200，确认空课程保存后会重新拉取首页课程数据。截图为 `output/playwright/checkin-dashboard-refresh-summary.png`、`output/playwright/checkin-dashboard-refresh-roster.png`。
> 3. **备份、回滚与风险**：本轮无 SQL、无数据库写入。切换前 NSSM 注册表、参数和 Nginx 配置备份位于 `D:\program\3009dazipingtai\backups\20260818_173500_checkin_dashboard_refresh_v1\`，旧 release `20260818_143000_python_mixed_practical_v3` 已保留；回滚时把 NSSM 的 `AppParameters`、`AppDirectory` 和 3010 Nginx root 恢复为该旧 release，执行 `nginx -t`、reload 并重启 `NewDaziBackend3009`。本轮未在正式库创建测试课程，避免写入教学数据；即时刷新以线上路由触发后的课程接口重取验证。

> **当前接力焦点（2026-08-18：Python 与文件操作题混合课程已发布）**
> 0. **根因和产品口径**：正式课程 `270 / python` 的题目 `1754` 早已是 `practical/PYTHON`，并配置了 2 个测试点（公开 1、隐藏 1）；截图显示文件上传的直接原因是 `StudentLessonQuestionVo` 未下发 `practicalMode`，学生页回退为 `FILE`。现已修复 DTO 映射和学生页按题目作答方式渲染。Python 与文件作品可同时加入一门课程，均归“操作题”；理论、打字、操作三类和课程总分 100 分口径不变。
> 1. **正式发布事实**：后端、前端已发布至 `D:\program\3009dazipingtai\releases\20260818_111000_python_mixed_practical_v1`，服务器 Java 进程实际使用该 release 的 `ruoyi-admin.jar`，与本机构建 JAR 的 SHA-256 一致；NSSM `NewDaziBackend3009` 为 `Running + Automatic`，3009/3010 均 HTTP 200。教师课程设计器已解除“最多一题操作题”的前端限制，并显示文件作品/Python 在线编程作答方式；学生历史成绩和教师成绩明细可显示操作题合计下的文件/Python 小计，原有 `practicalScore`、`totalScore` 保持不变。
> 2. **备份、数据与回滚**：发布前整库备份为 `D:\program\3009dazipingtai\backups\20260818_111000_python_mixed_practical_v1\ry-vue_before_python_mixed_practical_v1.sql`，89,682,031 bytes，SHA-256 `96FB56CA04F4AA1C512F28C2408023C726A4736FDEB05C92D667E3622E0A72AD`；同目录保留 NSSM、Nginx 和注册表备份。题目 1754 的起始代码已补为 `print("hello world")`，不改分值、测试点或成绩。校验为作答方式缺失 0、Python 配置缺失 0、无隐藏测试点 0；已有 8 条历史孤儿课程关联，均不属于课程 270，未擅自清理。回滚只需把 NSSM 参数/工作目录和 3010 Nginx root 切回 `20260817_194500_python_practical_mode_v2`，恢复备份 Nginx 后重启服务与 reload；除恢复起始代码外不需要回滚 SQL。
> 3. **验证与剩余门禁**：本机 `ProgrammingSubmissionServiceTest` 3/3、后端打包和 Vue3 生产构建均成功。正式服务器后端到 Judge0 的外置配置已存在，使用实际认证请求访问 `/about` 返回 HTTP 200；Judge0 四个容器运行中，服务/worker 资源上限和 50MB×5 日志轮转均已核对，UFW 仅放行 `10.52.1.123 -> 2358/tcp`。已打开正式学生登录页，但验证码阻止自动化登录，未绕过；因此“真实学生编辑器可见、示例运行、提交判题、成绩回写”和班级并发压测仍需在学生完成验证码登录后现场补验。Judge0 独立服务此前真实 `print(2+3)` Accepted 的证据仍有效。

> **当前接力焦点（2026-08-17：Python 在线编程与 Judge0 CE 第一阶段）**
> 0. **题型与成绩口径已确认并完成本地实现**：Python 不是第四类题型或成绩；它是 `biz_question.question_type=practical` 下的 `practical_mode=PYTHON` 作答方式，文件作品为 `FILE`。理论、打字、操作仍合计 100 分，既有操作题成绩统计同时覆盖两种方式。新增迁移 `sql/python_judge0_practical_mode_v2.sql` 会把历史 `question_type=python` 原样改为 `practical/PYTHON`，保留 Judge0 配置、测试点、草稿和提交历史，并停用旧字典选项；正式库发布前必须重新备份并执行该脚本。
> 1. **正式库和发布**：正式库 `ry-vue` 已在完整备份 `D:\program\3009dazipingtai\backups\20260817_194500_python_practical_mode_v2\ry-vue_before_python_practical_mode_v2.sql`（SHA-256 `6F03ED6C4DD16D50857D24AC4D541280F58603738139651AF31FCA3772D2AFDE`）后执行幂等迁移。后检为旧 `python` 题型 0、`practical/PYTHON` 1、`practical/FILE` 106、编程配置孤儿 0、旧 Python 字典启用 0。后端/前端已发布到 `D:\program\3009dazipingtai\releases\20260817_194500_python_practical_mode_v2`，NSSM 和 Nginx 已切换，3009/3010 HTTP 200；旧 release、NSSM 参数、Nginx 配置均保留。发布包 SHA-256 `7661944FC31153E8193F540A394FDBFDDFF0BCEC39EF717CB4D5AFE9AA42D654`。
> 2. **扩展服务器部署**：`10.52.1.129` 已完成 Ubuntu 24.04、16 核、32 GiB、根盘约 451 GiB 可用、Docker 29.1.3 盘点；Judge0 使用独立 Compose、`judge0_python_internal` 网络、独立 PostgreSQL/Redis 卷、固定镜像和 systemd 自启动，UFW 只允许 `10.52.1.123 -> 2358/tcp`。CryptPad 的 compose、网络、卷和本机 `3000/3003` 端口未修改。
> 3. **真实判题已恢复**：已备份 `/etc/default/grub` 到 Judge0 备份目录，加入 `systemd.unified_cgroup_hierarchy=0` 并重启 `10.52.1.129`；重启后确认 `/sys/fs/cgroup/memory` 为 cgroup v1。Judge0 Compose、systemd 自启动均为 active，真实 Python `print(2+3)` 返回 `Accepted`，输出 `5`、耗时约 `0.015s`、内存约 `3268KB`；本轮发布后 Judge0 容器仍为运行状态，Java 后端仍带 Judge0 外置参数。临时本机白名单测试完成后已恢复 `ALLOW_IP=10.52.1.123`；CryptPad 容器健康，3009/3010 HTTP 200。浏览器生产冒烟页面错误 0；教师账号此前已成功登录并完成校区选择。学生完整链路和压测仍需在有可用 Python 题目的正式课程上补做。

> **当前接力焦点（2026-08-17：CryptPad 在线协作第一项）**
> 0. **代码与数据库**：统一 `CollaborationProvider`、`CryptPadAdapter`、Mock Provider、AES-GCM 房间会话密钥、教师按授课班建房间/副本、学生同班共享与跨班/跨校/关闭房间拒绝、文档下载/onSave/CAS/revision、Vue3 教师和学生入口均已实现；`sql/cryptpad_collaboration_v1.sql` 正式库备份后已幂等执行，历史 WPS 表和回滚材料未删除。
> 1. **扩展服务器事实**：通过跳板机进入 `10.52.1.129` 已核验 Ubuntu 24.04、16 核、31GiB 内存、根盘可用约 453GB；Docker 29.1.3、Compose 2.40.3、Nginx 1.24.0 已存在。CryptPad 位于 `/srv/cryptpad`，固定镜像 `cryptpad/cryptpad:2026.5.1`，镜像摘要 `sha256:689634b77d1ef739efcd79b02e136788cb1b03793a7b6b6a46b2debcce130feb`，容器健康，端口只绑定 `127.0.0.1:3000/3003`；OnlyOffice v9 与 x2t 组件已按官方 SHA-512 校验并挂载到独立目录。
> 2. **运维事实**：`cryptpad-compose.service` 和 `cryptpad-backup.timer` 均为 enabled/active；每日备份目录为 `/srv/cryptpad/backups`，已现场生成带 SHA-256 的 `cryptpad_20260817_092620.tar.zst`（约 369MB），旧 WPS 数据未删除。Nginx 本机配置与仓库 `deploy/cryptpad/nginx/cryptpad.conf` SHA-256 一致。
> 3. **当前门禁**：服务器本机 `Host: office.xsedu.net.cn` 访问 `/`、`/checkup/` 和 `/cryptpad-api.js` 均 HTTP 200，WebSocket 握手返回 101；但 DNS 当前把两个域名解析到内网网关 `10.52.4.70`，从浏览器访问 HTTPS 返回网关 404（截图在 `output/playwright/cryptpad/office-gateway-404.png` 与 `office-sandbox-gateway-404.png`），说明外部网关尚未把两个 Host 转发到 `10.52.1.129`。网关管理权限/配置入口不在本机凭据中，因此 C10 浏览器可信 HTTPS 和真实房间验收不能宣称完成。
> 4. **顺序约束**：Judge0 Python 第一阶段已独立完成；MQTT 仍未作为 CryptPad 任务的一部分启动。CryptPad 后续仍需先完成 `10.52.4.70` 的两个 Host 转发和浏览器验收，再进入 MQTT 等后续任务。

> **当前接力补充（2026-08-17：初中物联网 P1 现场阻断已定位到教育局网络 ACL 与设备握手两层）**
> 0. **机房 Web 访问**：学生机没有 WireGuard，走教育局网络直连；访问 `http://10.52.1.123:18080/` 为 `ERR_CONNECTION_TIMED_OUT`，SIoT Web 日志没有对应请求。服务器 `CountySIoTPoc` 正常运行，`1883/18080` 均监听，防火墙规则为 Domain/Private 配置文件下允许 RFC1918 私网。现有证据高度指向教育局网络到 `10.52.1.0/24` 的上游路由/ACL 未放行，而非 18080 服务未启动。
> 1. **掌控板 MQTT**：照片显示 Wi-Fi 成功，Mind+ 使用 SIoT、服务器 `10.52.1.123`、账户 `siot` 和默认 MQTT 端口 `1883`；设备停留在 MQTT 连接阶段。服务器 stderr 在 2026-08-17 13:03:43、13:03:47 出现两次 broker `EOF`，表示 TCP 流量已到达 `1883` 但未形成有效 MQTT CONNECT；因此不能把该失败归因于 18080，也暂不能仅凭此日志判定是账号错误。P1 仍未通过。
> 2. **门禁与下一步**：先由信息中心放行学校/机房教育网到 `10.52.1.123` 的 TCP `1883`（设备 MQTT，必需）和按教师管理网来源限制的 TCP `18080`（SIoT 教师观察页）；当前 Mind+ 不需要 `1888`，只有改用 TLS 才申请 `8883`。放行后再复测 P1，不修改服务器端口策略、不进入 P2 正式平台开发。

> **当前焦点（2026-08-16：角色菜单优先级与教师帮助中心 v5 署名已发布）**
> 0. **角色菜单顺序**：教师顶级菜单固定为“教师首页 → 题库管理 → 成绩查询 → 学生管理 → 班级管理 → 教师工具 → 导学单管理 → 教研活动 → 学生个人成绩画像 → 免抽测申请 → 帮助中心”；教研员固定为“学校统计 → 课程与成绩监管 → 区域抽测 → 教研活动 → 免抽测申请审核 → 教师工具 → 导学单管理 → 学生管理 → 题库管理 → 系统管理 → 系统监控 → 帮助中心”。只调整前端顶级展示顺序，不改变权限或二级菜单；兼容若依把单菜单包装成根路径 `/` 的路由结构，帮助中心始终置底。
> 1. **帮助中心 v5**：v4 的教师六步流程、多图长页、教程分支、角色边界和全部教程素材保持不变；顶部署名更新为“由象山县一线信息科技教师 **郑东旭主要开发，朱屹辅助支持** 设计并持续开发”。本轮仅修改这一处前端文案，不改变页面结构、路由、权限、后端或数据库。
> 2. **学生导入防错**：学生管理界面班级选项限制 01～10；下载模板由前端直接生成并内置 01/2025/01 与 02/2025/02 两行示例，上传前解析 Excel，601、602 等带年级的三位班号会显示具体行号并停止上传。后端 `StudentImportRules`、示例模板和同规则校验也已在本机实现，专项测试 4/4 通过，但本次为避免把工作区其他未发布后端改动带入生产，正式环境只发布前端防错层，3009 未重启。
> 3. **验证与发布**：菜单单测 4/4、学生导入后端专项测试 4/4 以及 v4 教师/教研员/390px 手机生产验收仍为既有功能基线；v5 使用同一隔离源，仅覆盖帮助中心署名后完成 Vue3 生产构建，产物中新署名精确匹配 1 次。正式 3010 release 为 `D:\program\3009dazipingtai\releases\20260816_181815_ab6d0d6_help_credit_v5`，发布 ZIP 6,702,908 bytes、SHA-256 `55AAD7A96C28B84FEEB0DF07028104C55ED6AD63C097CB2D3693D7B0D8306853`；Nginx 候选/正式检查与 reload 均为 0，活动配置 SHA-256 `527E608722098EACE6252D1D0C44BB2841B8A8D523DAA613CE4C24E0312FD082`。3010 首页、帮助路由、后端 API 与 3012 均为 HTTP 200；未登录浏览器访问帮助中心按预期跳转登录页，页面错误 0，本轮未擅自提交生产账号密码，因此未重复执行登录后全角色冒烟。回滚点为保留的 `20260816_162101_ab6d0d6_help_center_v4/frontend`，配置备份在 `D:\program\3009dazipingtai\backups\20260816_181815_help_credit_v5`；无 SQL、无数据库回滚，3009 未重启。

> **上一焦点（2026-08-15：教师实用帮助中心 v3 与郑东旭个人主页 v6 已发布）**
> 0. **帮助中心 v3 与角色边界**：根级“帮助中心”仍只允许 `admin / teacher / researcher` 加载，学生没有菜单。教师端通过现有学生、任教班级、题库、教师首页接口给出可容错的真实准备度提示。顶部不再用难以辨认的四宫格，而是用 1 张 1280×720 课程设计真实页面大图呈现“教学练评闭环”。教师看不到教研员/管理员治理内容；教研员/管理员不显示教师智能检查和教师六步流程。未新增后端、数据库、SQL 或权限模型。
> 1. **署名与个人主页 v6**：登录页继续底部居中显示开发支持。所有登录后角色右下署名都可点击 `http://10.52.1.123:3012/`，链接无悬浮动画。个人主页源码仍独立位于 `D:\dmwprogram\zhengdongxu-portfolio`；首屏增加“00 后信息科技教师”，电话、微信二维码、深浅主题、GSAP/ScrollTrigger 与 Lenis 平滑滚动保留，小红书仍为 0。精选工具桌面固定画面与移动内嵌画面继续统一为 16:9、`object-fit: contain`；桌面固定画面改为在导航下方可用视口内垂直居中，1000px 高视口实测画面中心 489px、视口中心 500px，差 11px，短屏幕回退为顶部安全定位。
> 2. **工具内容、真实入口与截图**：精选 6 项为 `AI信息科技学业测评系统`、`错题刷题系统 2.0`、`AI 学业评价系统`、`小型网络搭建仿真网页`、`AI 音乐学习工具 2.0`、`本地班主任工作台`，班级电子宠物不再重点展示；完整档案由 18 增至 19。入口拆为“打开工具 / 抖音展示 / 网盘获取”；信息科技测评指向 `http://xxkj.xsedu.net.cn/`，错题指向 `/aicuoti/`，AI 学业评价指向 `/aiquanxuekexiangshan/`，网络仿真指向 `:3020`。AI 音乐显示演示账号 `zdx` 和密码 `123456`，跨站需手动填写。v5 从真实站点重新采集全部 6 张 1600×900 截图：网络仿真图实际放置 8 类设备并生成真实连接，不再是空画布；AI 音乐改为“教师出乐谱 + 学校歌曲库”，不再使用空成绩页。采集报告 `D:\dmwprogram\zhengdongxu-portfolio\public\project-media\capture-report.json` 为 `passed=true`。
> 3. **班主任工作台与服务器台账**：按用户确认的方案 1，把纯静态班主任工作台复制到个人主页 `public/tools/banzhuren/`，正式地址 `http://10.52.1.123:3012/tools/banzhuren/`，复用 3012 且不新增端口。服务器桌面已建立 `C:\Users\Administrator\Desktop\10.52.1.123-项目与端口台账.md`；仓库同步维护 `contexts/server-project-port-registry.md`，台账 SHA-256 `C345345C6004C4E56737AF9E015B3C95E74C011A0ECAC0F3FA313AA3E8078154`。
> 4. **六步真实页面教程**：教师流程固定为“确认学生 → 认领班级 → 准备题目 → 新建课程 → 学生作答 → 批改学情”。每一步用一张对应的 1280×720 正式环境功能截图整行展示，并同时提供“打开对应功能”和“查看图文教程”两个入口；教程抽屉复用该步骤截图，显示 4～5 条具体操作，不再另设重复的小教程卡片。六张截图来源依次为学生管理、班级管理、题库、课程设计器、学生任务、操作题批改，采集报告 `output/playwright/help-step-screenshots/report.json` 为 `passed=true`。
> 5. **隔离构建与正式发布**：3010 从 Git `ab6d0d6` 干净 worktree 仅叠加既有生产登录页、教师工具依赖、帮助中心、路由、署名与素材，2696 modules 构建成功；v3 ZIP 5,933,169 bytes，SHA-256 `83FCEC90083B56470C27EDE3A474C3035941EE31418017551D561E6E37F127E2`。3010 release 仍为 `D:\program\3009dazipingtai\releases\20260815_192819_ab6d0d6_help_center_v3`；3012 已切换到 `D:\program\zhengdongxu-portfolio\releases\20260815_204500_portfolio_v6`。个人主页 v6 ZIP 13,118,776 bytes，SHA-256 `3F9D2290D735D989A21D5601713E70311E5F029A631FAD10D40A9748CBA22973`。Nginx 候选检查、正式检查、reload 均为 0，活动配置 SHA-256 `15C90DEF4C859A44E15F3C10476252BD1B1A903DE310379611F5A591E9476757`；3010 首页/API、3012 首页、荣誉预览与班主任子路径均为 200，3009 未重启。
> 6. **生产验收与回滚**：帮助中心 `output/playwright/help-center-v3-production-smoke.json` 与个人主页 `D:\dmwprogram\zhengdongxu-portfolio\output\smoke-v6\report.json` 均为 `passed=true`，页面错误和失败资源均为 0。个人主页 6 张精选荣誉和 22 张时间线预览均为 1440×960 WebP；分类结果严格为讲座 5、本人获奖 4、指导学生 8、论文 3、公开课 2。桌面/390px 手机展开、筛选、灯箱、关闭、Esc、无横向溢出均通过，网页 PDF 链接和 PDF 网络请求均为 0。切换前 Nginx 备份在 `D:\program\zhengdongxu-portfolio\backups\20260815_204500_portfolio_v6`，备份配置 SHA-256 `D3EA61723671DD191E16663E752B166BE48CDD9D1321DFF3AB9FD8A2FEF07914`；回滚只需把 3012 root 切回保留的 `20260815_195300_portfolio_v5/frontend`，执行 `nginx -t` 和 reload，无数据库回滚。帮助中心 v3 的 3010 回滚点仍为 `20260815_184609_ab6d0d6_help_center_v2/frontend`。
> 7. **荣誉档案与隐私边界**：个人主页首页重点展示 6 项成果，“查看全部 22 项”在原页展开按日期倒序的单列时间线，支持全部/讲座培训/本人获奖/指导学生/论文成果/公开课筛选；每项可打开 WebP 证明材料灯箱。原始 PDF 只作为本地制作来源，正式 release 中 PDF 数量严格为 0。本人姓名、学校、奖项、日期、公章保留；学生姓名、验证/群二维码和联系电话等第三方敏感信息在预览层遮挡。含完整参训名单、身份证尾号和手机号的原始材料绝不上传；详细决策见 `contexts/developer-profile-help/ADR-003-public-honor-previews-without-raw-pdfs.md`。

> **并行焦点（2026-08-14：全平台下载文件统一命名已完成本地实现，待发布）**
> 0. **统一规则**：磁盘和数据库中的原存储路径保持不变，用户下载时由后端统一给出展示文件名；名称清除路径、控制字符及 Windows 非法字符，最长 120 字符。批量导出统一为“业务名称 + 范围（接口有明确范围时）+ `yyyyMMdd_HHmmss` + 扩展名”，上传附件保留原扩展名并增加“课堂题目素材、学生操作题作品、电子导学单学生作品、教研活动课程资源”等业务前缀；无法恢复原名的 UUID 文件显示为“附件_时间戳”，不再暴露 UUID 或泛化英文名。
> 1. **覆盖范围**：公共附件下载/预览、若依全部 `ExcelUtil` 导出、成绩汇总、区域抽测成绩、课程监管 CSV、代码生成 ZIP、电子导学单作品与结果、教研活动资源、WPS 存储回调，以及学生课堂素材和操作题作品均接入统一响应头。Vue3 的通用下载、Axios Blob 下载和直接 `saveAs` 下载均优先采用后端文件名，并兼容 `download-filename` 与标准 `Content-Disposition`。
> 2. **安全与兼容**：资源权限校验、文件内容、文件存储名和历史路径均未改变；通用资源下载新增可选 `downloadName`，后端仍会清洗该值且下载前继续执行原资源访问授权。无需 SQL、无需数据库写入、无需新增依赖。
> 3. **验证证据**：后端命名与响应头专项测试 5/5，前端响应头解析与清洗测试 3/3；`mvn -pl ruoyi-admin -am -DskipTests package` 成功，8 个后端模块全部构建；Vue3 `npm run build:prod` 成功（2685 modules，仅既有 vform `eval` 与大 chunk 警告）。`git diff --check` 无空白错误。
> 4. **发布要求与风险**：本轮尚未发布到 `10.52.1.123`；生效需要同时发布新 JAR 和 Vue3 `dist`，重启 3009 并切换/重载 3010。由于本轮未启动本地完整服务，尚未做浏览器真实下载冒烟；发布前应抽查“系统 Excel 导出、成绩导出、普通附件、学生题目素材”四类，确认中文名、时间戳和扩展名正确。

> **并行发布事实（2026-08-14：登录页三图轮播 v2 已前端独立发布）**
> 0. **页面实现**：Vue3 登录页保留“信息科技知识展墙”“真实信息科技课堂”两张背景；第三张改为用户提供的真实象山海湾夜景航拍图，经克制的 16:9 横向扩图和画质整理后输出 1672×941 WebP，不添加数字光线、文字、虚构学校或建筑。轮播改为每 5 秒自动切换，背景使用 1.8 秒交叉淡入和 5.4 秒轻微缩放；底部指示点由按钮改为不可点击的纯状态 `span`，页面非输入状态下可用键盘 `←` / `→` 双向切换，账号、密码、验证码输入框内的方向键仍保持文本光标语义。系统开启“减少动态效果”时继续尊重无动画偏好。
> 1. **隔离构建**：为避免把当前工作区尚未发布的下载命名、教师工具等改动误带上线，继续使用正式同基线 Git `ab6d0d6` 的隔离工作树，只复制本轮 `login.vue`、知识展墙、课堂和象山三张 WebP 后执行 `npm run build:prod`；2681 modules 构建成功。v2 发布包 5,687,145 bytes，SHA-256 `B81DEF1FA092221280B708083B19AA843D0782D8E1955A19340BAB2C89282A29`；`index.html` SHA-256 `F26CE583D847D36F2658F480D85B341B64AD76122DADD77ED4941EB153AE6A34`。产物中象山图为 `login-xiangshan-coast-v2-BC02xoNd.webp`、156,446 bytes，旧宁波合成图数量为 0。
> 2. **正式发布**：3010 Nginx 根已切换到 `D:\program\3009dazipingtai\releases\20260814_220728_ab6d0d6_login_carousel_v2\frontend`；候选配置和正式配置 `nginx -t` 均通过，reload 成功，活动配置 SHA-256 `0A95F1F3616CBCF39B80ABF39B7CCEA16BBB3CCFEC4CB0FE97CC67EC5516B787`。本轮仍只发布前端，3009 未重启且继续使用 `20260814_203252_ab6d0d6_wps_retired` 后端；无 SQL、无数据库写入，无需数据库备份或回滚 SQL。
> 3. **正式验收**：浏览器在 `http://10.52.1.123:3010/` 实测 3 个背景层和 3 个不可点击 `span` 指示点，按钮数量为 0；鼠标点击后索引不变，`←` 从第 1 张切到第 3 张、`→` 切回第 1 张，输入框内 `→` 不触发轮播；等待 5,229 ms 后自动切到第 2 张。第三张 URL 已指向新象山 WebP，登录框中心 `640` 与视口中心 `640` 一致。3009、3010、80 域名 Host、教师工具 3005、SIoT 18080 和新象山图片均为 HTTP 200。
> 4. **备份与回滚**：v2 切换前 Nginx 配置备份位于 `D:\program\3009dazipingtai\backups\20260814_220728_ab6d0d6_login_carousel_v2_before_login_carousel_v2\nginx.conf.before`，SHA-256 `521A8AD7F674CB094296B9AE8F7191EA20F67AA1613D617D2EE0FC70E94A2DC7`。回滚只需恢复该配置（或把 3010 root 改回 `20260814_212801_ab6d0d6_login_carousel/frontend`），执行 `nginx -t` 和 reload；v1、v2 及更早 release 均保留，不动 3009 和数据库。

> **并行运维事实（2026-08-11/14：生产服务器磁盘在线扩容已完成）**
> 0. **扩容结果**：VMware 磁盘 0 已将 C 盘从 79.40 GiB 在线扩展到 99.40 GiB，增加约 20 GiB；VMware 磁盘 1 已将 D 盘从 99.98 GiB 在线扩展到 599.98 GiB，增加约 500 GiB。两次均使用 Windows 原生 `Resize-Partition` 将右侧连续未分配空间并入原 NTFS 卷，没有格式化、移动、删除或改名现有内容，盘符和既有路径保持不变。
> 1. **最终证据**：磁盘 0/1 均为 GPT、Online、Healthy、非只读，未分配空间均为 0；C 盘可用 58,527,305,728 bytes（约 54.5 GiB），D 盘可用 607,510,876,160 bytes（约 565.8 GiB）。3009、3010、80、3005、18080 均为 HTTP 200。
> 2. **影响边界**：未修改业务代码、数据库、NSSM、Nginx 或端口配置，未重启服务器或业务服务；现有发布、备份和上传路径继续使用 D 盘。扩容无需应用级回滚，不建议为回到旧容量而收缩生产卷。

> **上一焦点（2026-08-14：WPS 在线协作已停用并发布；免费自托管替代方案待用户二选一）**
> 0. **产品决策**：用户明确放弃 WPS WebOffice，原因是正式容量收费且依赖 WPS 云端访问公网回调网关；不再要求信息中心为 WPS 开放公共 DNS/NAT。在线协作目标不变：多个学校、每班独立房间、全班共同编辑同一份 Word/Excel/PPT、总并发至少 200；个人作品、考试、批改和物联网继续与在线协作解耦。
> 1. **停用范围**：Vue3 已移除教师课程设计器协作面板、学生操作题协作入口、教师/学生直达编辑路由，并物理删除 WPS 前端 API、面板和编辑承载页三个文件。后端保留房间/版本模型作为迁移与回滚底座，但 `COLLABORATION_ENABLED=false` 时学生房间列表为空、会话不可创建、WPS 回调在读取/保存前直接返回“在线协作服务已停用”。四张协作表、历史房间和文件未删除，无 SQL、无数据库写入。
> 2. **构建与验收**：`mvn -pl ruoyi-admin -am clean package` 成功，业务测试 291/291、admin 3/3；Vue3 `npm run build:prod` 成功，模块由 2689 降为 2684。生产管理员登录和协作健康接口均为 200，`enabled=false`、`ready=false`、WPS AppID 未配置；旧回调地址只能得到停用结果。Playwright 教师课程设计器与旧直达路由冒烟通过，WPS/在线协作入口不可见，页面错误和 HTTP 5xx 均为 0；证据为 `output/playwright/wps-retirement-server-smoke.json` 及两张截图。
> 3. **正式发布与回滚**：3009/3010 已切换到 `D:\program\3009dazipingtai\releases\20260814_203252_ab6d0d6_wps_retired`；JAR SHA-256 `57FD2041683D2C77049C027835AAFA98F9307BCA36713A245D4A2A2B3175D538`，前端 `index.html` SHA-256 `74E13DD44FC70C7D76D4D60F548738B4BC7E230F94A251974D9D4E4A5E84EF5B`。活动 NSSM 中 6 个 `WPS_WEBOFFICE_*` 变量已移除，3009/3010 均为 HTTP 200。备份位于 `D:\program\3009dazipingtai\backups\20260814_203252_ab6d0d6_wps_retired_before_wps_retirement`，包含 NSSM 注册表和两套 Nginx 配置；旧 release 均保留，可恢复备份后重启回滚。
> 4. **Nginx 顺带修复**：正式 D 盘 Nginx 原配置有三处历史行粘连：WPS callback 注释与 `location`、WPS storage 注释与 `location`、xxkj 注释与 `server` 拼在同一行，旧主进程尚能服务但任何 reload 都会失败。本次在备份后拆行，`nginx -t` 已通过并成功 reload；`xxkj` 仍代理 3010，`aitool` 导航栏根未改变。
> 5. **替代引擎调研**：旧 `LibreOffice/online` 是历史镜像，当前活跃路线是 Collabora Online；免费 CODE 官方明确只适合测试/个人/小团队，不建议生产，仍需平台实现 WOPI 且 200 人必须压测。免费 Umo Editor 只是 Vue3/Tiptap 类 Word 编辑器，官方多人协作和 Office 导入导出属于商业版 Next/Server；截图中的 “Umo Editor Engine” 无可核验官方仓库。Yjs 只是 CRDT 同步引擎。ONLYOFFICE CE 有 20 连接限制，Univer 实时协作/导入导出属于 Pro，SuperDoc 只覆盖 DOCX。
> 6. **当前推荐与门禁**：首选验证 CryptPad Integration API：AGPL 自托管、Document/Spreadsheet/Presentation 使用浏览器端 OnlyOffice 代码但不使用 Document Server，无第三方公网回调，平台可通过 API 传文件/同班会话密钥并在 `onSave` 接回版本；仍须核对 AGPL、真实教学文件兼容和 200 人容量。备选为 Collabora CODE + WOPI。正式服务器现状为 Docker 未安装、Hyper-V/WSL/VirtualMachinePlatform 全部 Disabled，因此两种方案都应使用独立 Debian VM。详细对比和 P0～P3 验收见 `contexts/online-collaboration/provider-research-20260814.md`；用户确认 CryptPad 或 Collabora 前，不新增依赖、SQL 或替代引擎代码。

> **上一焦点（2026-08-12：在线协作学生鉴权已修复并发布；WPS 真实联机被公网 DNS/NAT 阻塞）**
> 0. **真实故障结论**：教师打开房间失败并非 AppID、签名或 WPS“可用范围”导致。WPS 调用日志明确为 `40007 ProviderError`，其云端 DNS 对 `xxkj.xsedu.net.cn` 返回 `no such host`；本机、服务器以及显式公共 DNS 查询均只得到私网地址 `10.52.1.123`。域名能够在教育内网打开，不等于 WPS 公有云能够解析和访问。学生“当前操作没有权限”是另一条独立故障：正式房间 1、课程 268、指派、学生楼尚岑的学校 169、届别 2025、班号 1 完全一致，但该班 46 名学生均未维护 `sys_user_role`，原控制器却先用 `student` 系统角色拦截。
> 1. **代码修复与安全边界**：`CollaborationController` 的学生当前房间和房间会话接口不再依赖缺失的系统角色；匿名请求仍由 Spring Security 统一拒绝，业务层继续要求学生的学校、届别、班号和当前课程全部与房间一致，教师仍须本校且为课程创建人/管理员。`CollaborationRoomService` 健康检查改为真实解析公网基础地址，私网、回环、链路本地和 IPv6 唯一本地地址均判为不可公网回调；创建 WPS 会话前强制执行健康门禁，教师/学生现在会看到域名、解析地址和“配置公网 DNS + 反向代理/NAT”的中文原因，而不是 WPS 灰屏。
> 2. **验证证据**：协作鉴权、跨班拒绝、权限先于网络诊断、私网回调识别、Token 与 WPS-2 签名专项测试 12/12；`mvn -pl ruoyi-admin -am clean package` 成功，业务测试 290/290、admin 3/3。生产教师登录成功，健康接口为 200、`ready=false`、解析地址仅 `10.52.1.123`；合法学生 `2025720104` 登录成功，`/business/collaboration/student/current` 从 403 修复为 200，并只返回房间 1；匿名请求仍为 401。教师和学生请求房间 1 均得到相同的可操作 DNS 诊断。浏览器复核中学生首页显示“本班共同编辑”，进入后显示该诊断，不再显示“当前操作没有权限”。
> 3. **生产发布与回滚**：3009 已切换到 `D:\program\3009dazipingtai\releases\20260812_211200_ab6d0d6_wps_auth_dns_fix_r2`，JAR SHA-256 `EE1BF5E3D11C91D6F65FE1958BB6F9906D7F27B9368C062B1A8A7E429D161A73`；NSSM 为 Running，3009/3010/域名 80/教师工具 3005/SIoT 18080 均为 HTTP 200，WPS 与 AI 环境变量原样保留。本轮无 SQL、无数据库写入。切换前 NSSM 注册表备份在 `D:\program\3009dazipingtai\backups\20260812_211200_before_wps_auth_dns_fix_r2\NewDaziBackend3009.reg`；应用回滚可先切回保留的 `20260812_210300_ab6d0d6_wps_auth_dns_fix`，必要时再切回 `20260811_225246_ab6d0d6_wps_weboffice_configured` 后端路径并重启。
> 4. **WPS 后台结论**：截图确认“在线预览编辑”能力已经开启；“权限管理 → 可用范围”为空不是本次 JSSDK 文件回调失败原因，当前请求在到达平台权限回调前已经死于 WPS 云端 DNS。回调网关仍应填写“网关前缀”而不是单个接口地址，并开启平台已实现的文件信息、下载地址、权限、批量用户、上传准备、上传地址、上传完成回调；但在公网 DNS/NAT 完成前，继续调整这些开关不能解决 `no such host`。
> 5. **当前唯一放行门禁**：信息中心必须让 `xxkj.xsedu.net.cn` 在互联网 DNS 中解析到公网 IP，并由公网 80/443 反向代理或 NAT 原样转发到 `10.52.1.123:3010`；不能继续把公网 A 记录直接写成 `10.52.1.123`。若使用教育专网专线，则需信息中心与 WPS 确认 WPS Solution 回调出口 IP 的路由和白名单。完成后先复核 WPS 日志不再是 DNS 错误，再进行两名同班学生 + 一名异班学生的打开、共同编辑、保存版本递增闭环；此前不得宣称在线协作“真实可用”。

> **上一焦点（2026-08-11：WPS WebOffice 测试应用已在生产启用，服务端协议验证通过，等待真实房间多人联机）**
> 0. **产品边界**：在线协作与物联网继续是两个独立功能。本 PoC 不开发 Office 软件，不替代学生个人操作题作品或考试；教师从课程内操作题 `STARTER` 文件选择一份 Word/Excel/PPT，平台按课程当前指派的 `学校 + 届别 + 班号` 各创建一个房间，同班编辑同一 `fileId`，异班使用不同文件副本。教师可进入、查看版本和回调诊断，也可复制只允许本班已登录学生进入的链接。
> 1. **平台实现**：新增独立 `biz_collab_room`、`biz_collab_revision`、`biz_collab_upload_ticket`、`biz_collab_callback_event` 四表及 `sql/wps_weboffice_collaboration_poc_v1.sql`；房间、权限、当前文件和不可变版本归平台持有，WPS 只作为编辑器提供方。题库起始文件仅复制，原文件不修改；关闭房间只改状态并保留历史。教师课程设计器新增 WPS PoC 设置和每班诊断表，学生操作题新增“进入在线协作空间”，Vue3 新增共用编辑器承载页。专题需求、设计、任务和决策见 `contexts/online-collaboration/`。
> 2. **WPS 协议与安全**：已按官方协议实现文件信息、下载地址、文档权限、批量用户信息，以及当前推荐的三阶段保存 `prepare → address → 一次性 PUT → complete`；每次完成保存递增版本并保留旧文件。公网回调使用 `@Anonymous` 绕过平台 JWT，但强制校验 WPS-2 的 AppID、Date 时间窗、Body/URI MD5 和 SHA1 签名，同时校验平台 HMAC-SHA256 短期 Token；下载使用短期用途 Token，上传使用 15 分钟一次性票据。AppID、AppSecret、Token 密钥、公网基础地址、官方 JSSDK 地址全部为外置环境变量，仓库和上下文不含真实凭据。
> 3. **数据库证据**：本机写库前完整备份为 `D:\dmwprogram\newdazipingtai\backups\20260811_201654_local_before_wps_weboffice_poc\xueyeceping_server_20260729.sql`，81,580,741 bytes，SHA-256 `AACB882CE08B920B05209A606EF559BCACF75664EBF7708706B7BD6C94729FCF`。正式库写入前完整备份为 `D:\program\3009dazipingtai\backups\20260811_203410_ab6d0d6_before_wps_weboffice_poc\ry-vue.sql`，76,987,373 bytes，SHA-256 `A133614E84B73319E1469854BC7C02447C10AEDE6784477787D3690EA3AF017A`。增量 SQL SHA-256 `FEDC209D24700264B84F371248332F4AE0E6D495367E9CE46AFE7DBA00E70C5A`；本机和正式库首次、重复执行均成功。激活验收产生 4 条唯一测试 fileId 回调事件，先把事件表备份为 `D:\program\3009dazipingtai\backups\20260811_225246_before_wps_activation\biz_collab_callback_event_before_probe_cleanup.sql`，3,772 bytes，SHA-256 `AAE98C355D9A416398DADB61ACC2409FC4F3F49CD9BFA4292D45C946143F3E59`，再执行 `sql/cleanup_wps_activation_probes_20260811.sql` 精确删除 4 条，探针残留为 0；没有修改真实房间、课程、题目、学生答案或成绩。
> 4. **验证证据**：短期 Token 与 WPS-2 官方 GET/JSON 签名规则专项测试 6/6；`mvn -pl ruoyi-admin -am clean package -DskipTests` 成功；Vue3 生产构建 2689 modules 成功。生产已设置真实测试应用环境变量并启用 `COLLABORATION_ENABLED=true`，教师登录与 `/business/collaboration/health` 均为 200，`ready=true`、问题数 0，AppID、AppSecret、Token 密钥、公网地址、JSSDK、存储可写全部通过。官方稳定版 JSSDK v1.1.27 已同域发布，公网 200、27,612 bytes、SHA-256 `815A2C447C85EA672714E2B26183BD862C83B1DB88D33314E3B010C82BE88343`，与 WPS 官方下载文件一致。匿名无签名请求被拒绝为“AppID 不匹配”；使用生产环境变量构造的 WPS-2 + 平台 Token 签名请求已通过全部安全校验并进入业务层，因测试 fileId 不存在返回“协作文档不存在”，证明不是平台 401 或签名失败。当前尚未收到 WPS 官方集群对真实房间的回调。
> 5. **正式发布与回滚**：3009 NSSM 与 3010 Nginx 根已切换到 `D:\program\3009dazipingtai\releases\20260811_225246_ab6d0d6_wps_weboffice_configured`；JAR 和原构建前端沿用上一 PoC release，只增加官方 JSSDK 静态文件和生产外置环境配置。既有 `PRACTICAL_AI_MASTER_KEY` 原样保留，WPS AppSecret 与随机 Token 密钥仅在 NSSM 私密环境中，不写入 Git 或本上下文；临时凭据文件已删除。切换前 NSSM 注册表与 Nginx 配置备份位于 `D:\program\3009dazipingtai\backups\20260811_225246_before_wps_activation\`。回滚可恢复该目录的注册表/Nginx 备份并切回 `20260811_203700_ab6d0d6_wps_weboffice_poc`；四张协作表继续向后兼容保留。
> 6. **域名绑定事实**：按用户提供的现有域名复用 `xxkj.xsedu.net.cn`，不再申请 `weboffice-api` 子域名。服务器曾同时存在 D 盘 Nginx 1.29.4 与 `C:\OpenResty` 两套 80 监听配置，实际监听者会随启动顺序变化；现已在两套配置中都增加 `server_name xxkj.xsedu.net.cn` 并代理到 `127.0.0.1:3010`，而 `aitool.xsedu.net.cn` 继续匹配导航栏。当前 80 实际由 D 盘 Nginx 持有。原始 TCP 验证中 `xxkj` 标题为“信息科技学业测评平台”且与 3010 一致，`aitool` 标题仍为“象山教育 AI 应用工具平台”；`xxkj/weboffice/callback/**` 已到 WPS 控制器并返回 AppID 未配置。OpenResty 配置备份为 `D:\program\3009dazipingtai\backups\20260811_223101_before_xxkj_openresty_binding\openresty-nginx.conf`，D 盘 Nginx 配置备份为 `D:\program\3009dazipingtai\backups\20260811_223253_before_xxkj_host_binding\nginx.conf`。两条探针诊断事件已精确删除，事件表恢复为 0。
> 7. **公网门禁与下一步**：WPS 控制台当前回调网关为 `http://xxkj.xsedu.net.cn/weboffice/callback`，平台公网基础地址为 `http://xxkj.xsedu.net.cn`，不需要第二个域名；HTTPS 仍是正式使用前的安全加固项。AppID/AppSecret、官方稳定 JSSDK、随机 Token 密钥和功能开关均已配置，服务器端启用门禁已解除。下一步由教师在一门带 Word/Excel/PPT 起始文件的课程中开启协作，确认 WPS 控制台所需回调接口开关均已打开，再用两名同班学生与一名异班学生验证“同班同文档、异班不同副本、保存后版本递增”。当前仍未真实打开 WPS 编辑器，也未声称 WPS 官方回调与多人保存闭环通过；免费测试版带水印、最多 5 个同时打开的不同文档、单文件 5 MB，不能证明全县 200+ 正式容量。
> 8. **生产资源事故与恢复**：激活过程中发现 PID 14948 的历史 Codex 远程 PowerShell 脚本自 22:23 起异常占用约 12.6 GB 内存，导致整机仅余约 57 MB、3009 一度 Paused、MySQL 报内存不足、Java/LibreOffice 无法提交内存；该进程命令行明确指向旧 release 的 `codex-remote-*.ps1`，不是业务服务，已按精确 PID 终止。释放后空闲物理内存约 14.2 GB、虚拟内存约 27.4 GB；3009 再次重启后为 `Running + Automatic`，约第 4 次两秒探测恢复 HTTP 200，WPS 健康仍 `ready=true`，stderr 长度保持 556 bytes 未新增 OOM 警告，最终日志中 23:05 之后无新 ERROR/Deadlock/OOM。现有 stderr 的 556 bytes 和 stdout 的早期 OOM/LibreOffice 错误属于释放内存前历史证据，不得误报为当前持续故障。

> **上一焦点（2026-08-11：QA 千并发报告真实缺陷已修复，生产 r4 在 200/1000 并发下均 0 失败）**
> 0. **范围与角色结论**：按用户确认的方案 B，管理员账号已在本机和正式环境验证可登录；管理员部分功能不作为本轮放行条件。教研员已分别在本机和正式环境完成“新增教研员账号 → 新账号登录且角色为 researcher → 删除测试账号”，残留为 0。管理员凭据只保存在 gitignore 的 `contexts/secrets.local.md`，不写入报告、上下文、日志或提交。
> 1. **报告缺陷修复**：`StudentHomeController` 增加学生角色守卫，教师访问学生首页接口由业务 500 改为 403；全局不支持 HTTP 方法由 500 改为 405；`sql/fix_teacher_lesson_list_menu.sql` 改为删除 teacher 对停用管理员课程菜单的残留关联，正式库执行前为 1、执行后为 0，实际权限边界仍为 403。历史 2087 条孤儿答卷无法从现有 17 份本机备份恢复题目定义，本轮没有猜测或清理历史成绩；新增删题保护，已有学生答卷的题目禁止硬删除，防止继续产生孤儿。
> 2. **性能修复**：保留已部署的 7 组只读索引与 Druid `initialSize=20,minIdle=20,maxActive=120`；学生历史成绩收口为一次 CTE 聚合，错题/课程答案利用唯一约束取消全表“最新答案”物化，课程题目与评分项从 N+1 改为一次 JOIN，教师班级人数改为一次分组聚合。教研学校/教师/课程/时间线按完整筛选结果缓存 60 秒并做实例内防击穿，教师首页按用户+学校缓存 30 秒，教师工具目录缓存 5 分钟；正常高频日志降为 DEBUG，默认业务日志级别改为 INFO。缓存只影响短时间刷新速度，不改变权限、接口字段或统计口径。
> 3. **编译、功能与清理**：`mvn -pl ruoyi-admin -am clean package` 成功，业务测试 278/278、admin 测试 3/3。QA 清理脚本已修复并执行，33 个 QA 账号、30 个学生、1 门课程、4 道题、100 条答卷及相关实操作品/附件均精确删除，所有 QA 标识残留为 0，证据为 `output/qa-tester/20260811-qa-1000-full/evidence/cleanup.json`。生产分页复核中 2025 学年课程共 223 条，第 1/2 页各 10 条且 ID 不重复；教师首页连续请求约 181/50/37 ms，缓存命中有效。
> 4. **并发与死锁回归**：最终 r4 生产 120 并发 60 秒共 37,172 次请求，0 失败，P50 187 ms、P95 250 ms、P99 359 ms、603.8 RPS；200 并发 60 秒共 29,531 次请求，0 失败，P50 390 ms、P95 578 ms、P99 1.0 秒、465.63 RPS，修复前为 28.637% 失败、P50 19.187 秒、P95 30.234 秒、8.3 RPS。1000 并发覆盖教师/教研员/学生 22 类只读接口，共 31,576 次请求，0 失败，P50 1.703 秒、P95 3.531 秒、P99 11.204 秒、433.66 RPS；P95 比 r3 的 7.313 秒再降约 51.7%，但极少数 P99 请求仍偏慢，不能表述为“每个请求都秒开”。本机同一学生 50 并发重复交卷为 50/50 成功、答案行数 43 前后不变、MySQL deadlock 增量 0，测试数据已原样恢复。
> 5. **备份、SQL 与发布**：本轮本机写库前备份为 `D:\dmwprogram\newdazipingtai\backups\20260811_191844_local_before_qa1000_fixes_ab6d0d6\xueyeceping_server_20260729.sql`，81,514,842 bytes，SHA-256 `5E4D2BD3AE3FAD1E4A8CC629F3F967AB2DEC309C5362899ECD2332D387775993`。正式库写入前备份为 `D:\program\3009dazipingtai\backups\20260811_193044_before_qa1000_perf_fix_ab6d0d6\ry-vue.sql`，76,962,235 bytes，SHA-256 `F98A99EA61BB28019F08DECAF4A8E1470757EC8DBBB00B4A5FD39E21FA47A0C1`。3009 当前 release 为 `D:\program\3009dazipingtai\releases\20260811_195500_ab6d0d6_qa1000_perf_fix_r4`，JAR SHA-256 `854DF893B5FB86D0B47DEE222087539CB35D2553D155933E90970A102DCB0FD8`；服务 Running，3009/3010 均 HTTP 200，压测后 stderr ERROR 与 stdout Deadlock/OOM 命中均为 0。
> 6. **回滚与剩余风险**：应用回滚可把 NSSM 切回保留的 r3 `20260811_194850_ab6d0d6_qa1000_perf_fix_r3` 并重启；本轮只追加执行权限残留清理 SQL，回滚可按 `sql/fix_teacher_lesson_list_menu.sql` 注释恢复关系。历史 2087 条孤儿答卷仍需业务人工决定保留、映射或清理；1000 并发 P99 尾延迟以及教研活动主题、题库列表两个相对慢端点可继续专项优化。当前代码未 commit、未 push。

> **上一焦点（2026-08-11：初中物联网县级 SIoT 2618 PoC 已部署，软件链路通过，等待 Mind+ + 掌控板实测）**
> 0. **产品边界**：初中学生继续在 Mind+ 编写掌控板 Wi-Fi、MQTT 和传感器程序，平台不重新开发 Mind+；物联网以 2/4 人小组实验和全班共享为主，不做考试。物联网与在线协作是两个独立功能。小学省平台本轮不接入；初中专题见 `contexts/junior-iot-poc/`。
> 1. **县级 PoC 部署**：服务器原有 2019 年 SIoT 1.2 占用 1883，由 NSSM `SIoT` 自动启动；检查时只有本机自连接，无外部学生设备。按用户明确要求，旧服务和 `D:\Tools\SIoT1.2` 已删除。删除前备份为 `D:\program\siot-poc\backups\20260811_161328_siot12_before_removal\SIoT1.2.zip`，9,863,288 bytes，SHA-256 `D23F042072D9EF4577A4D8B65737354BDAEF65DFF6EB4EB9D58BBC8D34CB0CD7`。
> 2. **新版服务**：SIoT 2618 发布到 `D:\program\siot-poc\releases\20260811_161357_2618`，上传制品 SHA-256 `5D61FDCE64924456EBE746B155B27B78D7EF66375C04F42B7A66E793401754C8`；NSSM 服务 `CountySIoTPoc` 为 `Running + Auto`。MQTT 为 `1883`、Web 为 `18080`，另由 2618 自带监听 1888/8883；新增 1883/18080 入站规则，目标为 Domain/Private 下的 RFC1918 私网地址。日志在 `D:\program\siot-poc\logs`。失败部署产生的两个非活动 release 已删除，只保留当前活动 release。
> 3. **链路与恢复证据**：开发机跨机器访问 1883/18080 成功，Web 为 HTTP 200；两个独立 MQTT 客户端完成认证、订阅、发布和同数据接收，错误凭据明确返回 CONNACK 5。模拟 `main.exe` 异常退出后，NSSM 约 1.115 秒恢复新进程及 1883/18080，恢复后 MQTT 再次收发成功。240 个模拟小组并发连接、每组 5 条消息，共 1200/1200 条全部收到，客户端错误 0，耗时约 0.274 秒；负载后进程工作集约 56 MB。QoS 0 测试消息未写入 SIoT 数据库，测试前后 `poc/%` 消息和 Topic 均为 0。浏览器使用 2618 当前凭据成功进入“数据管理”，标题为 `SIOT V2`、无 5xx 或白屏，截图为 `output/playwright/siot-poc-server-dashboard.png`；Playwright 自动填表会触发发行包登录页一次非阻断 `onValuesChange/trim` 页面异常，但仍能完成登录，真实手工登录需在 P1 一并观察。
> 4. **隔离与现网复核**：本轮没有修改学业测评平台前后端、数据库、80/3005/3009/3010 配置；最终复核这些端口与 18080 均为 HTTP 200。SIoT 默认单账号仅用于 PoC，不等于正式多校权限隔离；正式平台必须由服务端生成学校/课程/实验/小组 Topic 与设备令牌，浏览器不得持有全县共享凭据。
> 5. **下一门禁**：当前只证明“县服务器 Broker 与平台式订阅程序”软件链路和 240 组瞬时并发可行，尚未证明学校 Wi-Fi、教育网跨校路由和真实掌控板稳定。下一步严格按 `contexts/junior-iot-poc/physical-board-test.md` 用一块掌控板连续发送 10 分钟并做断网恢复；P1 通过后再开发平台最小接收器、课程/小组映射、实时展示和分层诊断。Mind+ 使用 SIoT 2618 官方默认凭据，不能继续用旧 SIoT 1.2 截图中的口令；凭据不写入核心上下文或聊天。

> **上一焦点（2026-08-11：生产高并发只读超时已按方案 B 修复并发布，120/200 并发均 0 失败）**
> 0. **根因核实**：QA 猜测的 `maxActive<=8` 不成立，旧正式 JAR 实际为 `initialSize=10,minIdle=20,maxActive=100,maxWait=30000`；MySQL `max_connections=151`，历史 `Max_used_connections=100`，说明应用连接池确实打满。Performance Schema 还确认 `/business/schoolScore/schools` 对应聚合平均约 6.67 秒、学生历史答题会物化约 17.37 万答案分组、教师班级人数按班重复扫描约 7906 名学生，慢查询与 N+1 才是主要放大器；缓冲池命中正常，本轮不改 MySQL 内存参数。
> 1. **代码与配置修复**：Druid 调整为 `initialSize=20,minIdle=20,maxActive=120,maxWait=30000`，仍给 MySQL 保留 31 个以上非应用连接余量。学生历史成绩改为单次 CTE 聚合，学生课程答案/错题查询利用既有唯一约束取消全表“最新答案”物化；教师班级人数改为按学校一次聚合；教研监管学校汇总增加 15 秒参数级 Redis 缓存和实例内防击穿；教师工具目录增加 5 分钟 Redis 缓存并在增删改、上下架、恢复时失效。没有改接口字段、权限和业务口径。
> 2. **索引、备份与回滚**：新增幂等 `sql/high_concurrency_read_performance_v1.sql`，只增加 7 组二级索引（20 个字段序号），不改表字段和业务数据。本机执行前整库备份为 `D:\dmwprogram\newdazipingtai\backups\20260811_100058_local_before_high_concurrency_read_ab6d0d6\xueyeceping_server_20260729.sql`，75,198,109 bytes，SHA-256 `8B16E0299FC9FB7C8B49A54424462D5FB3E5FD49E1A7B3454594750F865BBE00`。正式库执行前整库备份为 `D:\program\3009dazipingtai\backups\20260811_101847_ab6d0d6_before_high_concurrency_read\ry-vue.sql`，76,817,129 bytes，SHA-256 `1633B0A25BF24E1403AA009805FFB1B5A95BE1984F7D971ABAB5B57805B809CE`；正式执行 SQL SHA-256 `4A991CA9EFA1327E40B313B4B52237BA4F1BAF3A7ED8980348760241DD5C95EB`，首次与重复执行均成功，后检为 7 组/20 列、答案重复组 0。索引向后兼容，可随旧 JAR 保留；若必须撤销，应逐个 `DROP INDEX`，整库恢复会覆盖备份后的新业务数据，不能作为常规回滚。
> 3. **本机验证与死锁回归**：业务模块全量 272/272、admin `clean package` 均通过，教师首页/班级、教研学校、教师工具、学生历史成绩/错题真实 API 均为 HTTP/业务码 200。50 并发课堂写回归为 2245/2245 成功、0 超时、0 网络错误，交卷 480/480 成功（P50 28.25 ms、P95 36.82 ms），运行日志死锁和连接池错误均为 0；测试数据已精确清理，库表计数恢复、残留/重复/孤儿均为 0。证据集中在 `output/qa-tester/high-concurrency-read-20260811/`。
> 4. **正式发布与性能结果**：3009 后端已切换至 `D:\program\3009dazipingtai\releases\20260811_102008_ab6d0d6_high_concurrency_read`，JAR SHA-256 `EA5ABCAEC37261FAA45EC4058BC8A114B9B26D15713606C50FC1E95B97CE269E`；NSSM 为 `Running + Automatic`，3009 已重启，3010 前端未改。120 并发由修复前 450 请求、18.22% 失败、P50 13.156 秒、P95 45.406 秒、4.74 RPS，改善为 36,815 请求、0 失败、P50 63 ms、P95 781 ms、601.22 RPS；200 并发由 866 请求、28.637% 失败、P50 19.187 秒、P95 30.234 秒、8.3 RPS，改善为 52,518 请求、0 失败、P50 219 ms、P95 266 ms、847.28 RPS。两次均复用 QA 脚本并使用 60 秒只读施压。
> 5. **发布后健康与剩余风险**：200 并发后 31.5 MB 应用日志中连接池超时、死锁、OOM、应用 ERROR、Druid 慢 SQL均为 0，stderr 为 0；服务工作集约 777.6 MB、177 线程，MySQL 连接已回落至 21、运行中 2，3009/3010/80/3005 均 HTTP 200。短缓存最多带来监管汇总 15 秒、教师工具目录 5 分钟陈旧窗口；新增二级索引有轻微写放大，但 50 并发交卷已验证无失败/死锁。应用回滚只需把 NSSM 切回 `20260810_114800_ab6d0d6_teacher_tools_r3` 并重启，旧 release 保留。当前代码未 commit、未 push。

> **功能完成状态校正（2026-08-11，依据用户明确确认）**：课程与成绩监管、操作题限期批改、教师免抽测以及区域抽测核心闭环均已完成，不再列为“本机完成、正式服务器待发布”或“待开发”事项。区域抽测已经包含清单、状态机、参考班级/学生、作答限时、匿名评卷和成绩发布分析；操作题 AI 专题中“区域抽测暂不启用 AI”只表示 AI 辅助评卷尚未纳入该专题范围，**不代表区域抽测功能未完成**。本次只校正产品完成状态，没有重新核验或补写当时正式发布目录、数据库备份及 SQL 哈希，后续若需发布审计应以服务器留存记录为准，不得猜测。

> **上一焦点（2026-08-10：教师工具 4 个失效入口已修复，3 个课堂工具已纳入 NSSM 自动启动）**
> 0. **根因与端点结论**：打字平台旧主机 `10.52.1.92` 已离线，目录地址已按用户确认改为 `http://10.52.1.94/`。小型网络搭建原 3000 端口被 3011 信息科技基础检测后端占用，保留 3011 不动并把网络仿真改为 `http://10.52.1.123:3020/`。图像识别和小学实验项目文件完整但没有进程或自动启动，地址继续为 `http://10.52.1.123:3001/`、`http://10.52.1.123:3003/`。
> 1. **服务与网络配置**：正式服务器新增 NSSM 服务 `TeacherToolNetwork3020`、`TeacherToolImage3001`、`TeacherToolPrimaryLab3003`，均为 `Running`、`Auto`；日志固定到 `D:\program\_startup_logs\teacher-tools\`。新增同名 3020/3001/3003 入站规则，仅允许 Domain 配置文件和 `LocalSubnet`。网络入口 `D:\program\3000xiaxingwangluodajian_7shang\backend-jieru1\index.js` 只把监听端口从 3000 改为 3020；3011、3009、3010、80、3005 均未改动。
> 2. **代码与 SQL**：`sql/teacher_tools_v1.sql` 的新装种子已同步 `.94` 和 3020；新增幂等 `sql/teacher_tools_endpoint_repair_v1.sql`，按 `LOCAL_3005 + source_ref` 严格前检，只更新 `3005-typing` 与 `3005-network`，并核对小学实验、图像识别两个既有地址。专题运行决策见 `contexts/teacher-tools/ADR-002-independent-service-ports.md`。
> 3. **备份与数据库证据**：服务修改前文件备份为 `D:\program\3009dazipingtai\backups\20260810_153005_ab6d0d6_before_teacher_tool_services`，原网络入口 SHA-256 `73A38C275D34C13116E9850D8A2AB10E7FF1911DF2CB2317036DE17B0E1D7864`，图像入口 `1D7EE0FDCE4C9AD59DC1F256F5C78F09DDF9C00715DBF40DE7512E2F52C230EF`，小学入口 `BF8DED88E77B709EC2F16507E00386E7462E5EAA4BEB3E09CA7638FD8A96A6D2`。本机库备份为 `D:\dmwprogram\newdazipingtai\backups\20260810_153738_local_before_teacher_tool_endpoints_ab6d0d6\xueyeceping_server_20260729.sql`，74,943,003 bytes，SHA-256 `5463E95693A7810F05A163F6D829D6D23D4AE32FF51DDDF6D0FF36625FC4F5BC`。正式库备份为 `D:\program\3009dazipingtai\backups\20260810_153942_ab6d0d6_before_teacher_tool_endpoints\ry-vue.sql`，76,530,967 bytes，SHA-256 `90B11576295CEB92CA4E03B9583427299D25352FF4D0ACAEB47C9C4A2A76E46C`；执行 SQL SHA-256 `CF3510E18D76E2B27D96154F6A2A91122A01A2E76678ED67991944A78CFB5530`。本机和正式库首次/重复执行均为 0，正式四条地址匹配且重复来源为 0；3009 无需重启。
> 4. **正式验收**：新增三个服务首次启动及停启回归后均保持 `Running + Auto`，客户端访问 3001/3003/3020 均为 HTTP 200。浏览器确认小学平台显示三至六年级上下册 8 个入口，网络仿真显示光猫/路由器/交换机与终端设备画布，图像识别显示上传界面，`.94` 显示学生/教师登录；教师端仍为 81 个工具，打字卡片实际打开 `.94`。图像识别 `/recognize` 使用仓库 Logo 合成样本返回 `result_num=5`，页面错误日志为 0。报告为 `output/playwright/teacher-tools-endpoint-repair-server-smoke.json`。
> 5. **回滚与风险**：服务回滚先停止并删除上述 3 个 NSSM 服务及 3 条 `TeacherTools-*` 防火墙规则，再从服务备份恢复网络 `index.js`；目录地址可依据正式备份中的 `teacher-tools-before.tsv` 精确恢复，不应直接全库覆盖。图像识别仍依赖外部识别接口，`.94` 旧测评站和所有外部工具的持续可用性不受本平台控制；目前未增加自动巡检。当前代码未 commit、未 push。

> **上一焦点（2026-08-10：教师工具单页导航已发布 10.52.1.123，80/3005 旧站保持不变）**
> 0. **产品与权限结论**：Vue3 教师端新增根级一级菜单“教师工具”，紧跟教师首页。教师、教研员、管理员可浏览；仅教研员、管理员可通过页内按钮进入隐藏管理页；学生无菜单且浏览/管理接口均返回业务码 403。专题需求、设计、任务、ADR 和清洗清单见 `contexts/teacher-tools/`。
> 1. **页面能力**：浏览页采用现有平台侧栏 + 高密度单页导航，提供名称/说明/标签搜索和吸顶分类定位。小学、七年级、八年级、省学科平台为重点展开区；学生免登录、AI、编程、办公素材、教师网站、班主任、跨学科、校本区域为次要折叠区，默认预览 4 项并在本页展开。“常用推荐”由工具标记生成。工具卡片按名称、说明、标签和访问类型映射编程、网络、AI、图片、文档、PDF、表格、音乐、评价、班级等 18 类现有平台 SVG 图标及 6 组色彩；外部图片失败时也回退到对应语义图标。分类定位改为带图标的自动换行按钮组，重点/次要入口分层显示，不再产生横向滚动条。所有卡片通过 `_blank` + `noopener,noreferrer` 打开。
> 2. **数据、接口与治理**：新增 `biz_teacher_tool_category`、`biz_teacher_tool`、`biz_teacher_tool_category_rel` 三表及 `/business/teacher-tools/catalog`、分类/工具管理接口。`sql/teacher_tools_v1.sql` 可重复执行，正式库现有 12 个分类、81 个有效工具、90 条关系；重复分类编码、重复来源、孤儿关系、学生权限、禁止协议、URL 凭据和明文密码均为 0。首批数据来自 3005、80 根站和省学科工具页，已剔除旧导航循环、私人协作房间、明文凭据、娱乐/翻墙和高风险下载项；之后只在本平台维护，不再同步旧站。
> 3. **本机验收**：迁移前完整备份位于 `D:\dmwprogram\newdazipingtai\backups\20260810_111223_local_before_teacher_tools_ab6d0d6\xueyeceping_server_20260729.sql`，74,838,676 bytes，SHA-256 `D6ACF717C1AFCCD7CA3B317FEEBCDC9A6ECDAC0258185070848F13C773822855`。SQL 首次/重复执行一致；后端业务全量 270/270（本机 Surefire fork 拒绝访问后按既有口径 `forkCount=0` 通过）、admin clean package、前端纯函数 7/7、Vue3 生产构建 2684 modules 均通过。管理 CRUD、URL 拒绝、上下架、软删恢复、多个手工工具空来源标识和测试数据精确清理通过。视觉回归在 1536 和模拟 125% 的 1229 CSS 宽度下均无页面/导航横向溢出，首屏识别到 18 种语义图标，页面错误与 5xx 均为 0；截图为 `output/playwright/teacher-tools-icons-nav-desktop.png`、`teacher-tools-icons-nav-125.png`。
> 4. **正式角色与浏览器证据**：正式 API 为教师目录 200/管理 403、教研员目录与管理 200、学生目录与管理 403。正式 3010 在 `1536×813`（常用 125% 缩放等效视口）下，推荐区保持四列、卡片宽 306.5px，页面与导航 `scrollWidth` 均等于可视宽度，18 种语义图标生效且无页面错误/5xx；截图为 `output/playwright/teacher-tools-server-visual-125.png`。原功能验收继续有效：教师页 12 分类、81 工具、次要分类 4 项预览/展开、Python 搜索和无结果状态均正确，教师管理入口为 0；教研员管理页和新增表单可见；学生菜单为 0。两个不填来源标识的手工工具均成功新增，随后已连同关系精确清理为 0。
> 5. **正式备份与发布**：正式完整数据库备份仍为 `D:\program\3009dazipingtai\backups\20260810_112430_ab6d0d6_before_teacher_tools\ry-vue.sql`，76,433,395 bytes，SHA-256 `38B49FAE5A5B01C73971860265CFE635F2BEFD3D1992B5BF74904261EE109564`，清单 SHA-256 `55F31C7BA69328C1F3F6F38A861EE8E5CE982D99156A542BF3BCB244A43CFE0D`。3009 后端继续运行统一 release `20260810_114800_ab6d0d6_teacher_tools_r3`，JAR SHA-256 `DC5174D7D33AE12F91D98A70DD28CBF8626997ED80E099469649ABB0D5B2EB0F`，本轮未重启。3010 前端已切换到 `D:\program\3009dazipingtai\releases\20260810_122305_ab6d0d6_teacher_tools_visual`；压缩包 SHA-256 `1F9D516C214B74B0FD7A4564D950099908169A81F50B852B72F9699D706B5141`，index SHA-256 `3FADE5F355DEBF4E5792991646CE7836CDB98575BD93C74FB47BA50933A76AD0`，Nginx 配置备份为 `D:\program\3009dazipingtai\backups\20260810_122305_ab6d0d6_before_teacher_tools_visual\nginx.conf.before-visual-refresh`。Nginx `-t`/reload 均为 0，3010、API 代理、80、3005 均为 200。
> 6. **回滚与剩余风险**：本轮视觉热更只需把 3010 Nginx root 切回 `20260810_114800_ab6d0d6_teacher_tools_r3/frontend` 并 reload；3009 与数据库无需处理。若需回到本功能发布前，则后端切回 `20260809_172512_b1801ac`、前端切回 `20260810_103724_ab6d0d6_preview_hotfix`。三张新表可保留；若必须数据库级回滚，使用上述正式备份，但会覆盖备份后的新维护数据。外部工具的登录、验证码、服务条款和持续可用性仍由外站负责；本轮按产品决定未增加自动巡检。当前代码未 commit、未 push。

> **上一焦点（2026-08-10：操作题预览 401 与历史单图片兼容热修已发布 10.52.1.123，三个教师/教研员入口正式验收通过）**
> 0. **根因与安全边界**：题库、普通课程设计器、区域抽测设计器仍把 `previewPath` 直接拼成 `/profile/**` 新窗口地址；浏览器新窗口无法附加 `Authorization` 请求头，而后端按既有安全策略拒绝直接访问该目录，所以返回业务码 401。修复统一改走 `/common/resource/view?resource=...`，继续由 Cookie 登录态和资源归属校验授权，不放宽 `/profile/**`。
> 1. **前端修复**：`business/question/index.vue`、`business/lesson/designer.vue`、`business/countyExam/examDesigner.vue` 三个旧入口已统一使用受权资源接口。`student/index.vue` 同时兼容历史单图片作品：图片附件只要存在 `resourcePath` 即可直接预览；Office/PDF 仍要求规范化预览成功；多附件中的不可预览项会单独禁用，不再被题目级旧字段误判。
> 2. **本机与正式验收证据**：资源权限相关 Java 专项 12/12、admin `clean package`、Vue3 生产构建（2678 modules）均通过。本机临时 PDF 精确回归确认直接 `/profile/**` 返回业务码 401，受权接口返回 HTTP 200 `application/pdf`，临时题目、评分项、材料和上传文件均已清理。正式环境题库题目 1247、普通课程设计器 237、区域抽测设计器 3 的浏览器/API 冒烟 12/12：三处地址均为 `/prod-api/common/resource/view`，目标 PDF 均为 200 `application/pdf` 且 `%PDF` 签名正确；页面异常和 5xx 均为 0。报告 `output/playwright/operation-preview-server-hotfix-smoke.json`，截图为 `operation-preview-server-question.png`、`operation-preview-server-lesson-designer.png`、`operation-preview-server-county-designer.png`。
> 3. **历史数据与文件审计**：本机库共有操作题 104 道，旧预览状态为成功 71、失败 24、空 9；当前教师可见 37 道，其中 34 道有旧预览路径，但对应 34 个历史 PDF 在本机 `RuoYi-Vue/uploadPath` 均不存在。这是本机数据库克隆与历史上传目录未完整配对，不是路由代码问题。正式服务器已只读确认用户给出的示例 PDF 实体存在，大小 149,783 bytes；其正式预览已恢复。附件现状为旧预览失败 314、规范化状态失败 84，主要原因是历史 PDF 生成失败 207、历史类型不支持 107、源文件缺失 84；当前版本/附件关系检查未发现当前版本缺失、状态错配、孤儿答案版本或答案源路径错配。
> 4. **正式发布、回滚与剩余项**：本轮没有 SQL、后端接口或配置变更，3009 未重启。前端 release `D:\program\3009dazipingtai\releases\20260810_103724_ab6d0d6_preview_hotfix` 已切换，压缩包 SHA-256 `7E9D46573E961B223C08CFFF200032611980023BB0FEBD48DFE2174225D89BB9`，index SHA-256 `47C0FF84E5CEE8DD6673F6AF1244676F70A790C233F1A2F2983E5EE8EBDC2065`；Nginx 配置检查、reload、3010 首页和 API 代理均为 0/200。发布前配置备份为 `D:\program\3009dazipingtai\backups\20260810_103724_ab6d0d6_before_preview_hotfix\nginx.conf`，SHA-256 `8385988C507525328A9771A827BF4E3ABD77415E12B98EF15E5DDD66DE3F5EE5`。回滚只需恢复该配置或把 3010 root 切回 `20260809_172512_b1801ac/frontend` 后 reload。当前未 commit、未 push；正式环境没有可用的文档内学生验收账号，历史单图片按钮兼容仍待获得有效学生样本后补做只读 UI 冒烟。

> **上一焦点（2026-08-09：教师首页按课程开设年级分栏与首屏提速已发布 10.52.1.123，并通过郑东旭教师账号正式验收）**
> 0. **产品结论**：教师首页第一层继续按稳定届别 `entry_year` 分组，第二层按课程开设年级 `grade` 分栏。当前年级栏始终存在并默认展开；历史年级栏默认折叠，展开后显示全部历史课，且允许通过带目标年级提示的入口新增历史课。术语、需求、设计、任务和决策见 `contexts/teacher-dashboard-grade-history/`。
> 1. **课程不变性与课次**：`grade` 已明确为课程开设年级，课程创建后与 `entry_year` 一样不可在普通编辑中改变。新建课程的课次由服务端按“同学校 + 同教师 + 同届别 + 同课程开设年级”取最大值加一；前端只提供同口径建议。当前年级没有课程时从第 1 课开始，上下学期不重置。
> 2. **推进边界**：自动推进和教师首页手动一键推进寻找下一课时均要求课程开设年级相同；课次重新从 1 开始后不会串入历史年级课程。历史课仍保留设计、指派、批改、成绩和显式新增能力。
> 3. **首页性能收口**：`/business/teacher/dashboard-data` 只返回届别、课程和批量读取的指派班级，不再同步计算逐课程操作题批改状态；红点改由 `/business/teacher/dashboard-practical-status` 异步补充，区域抽测入口也独立加载。核心失败与辅助失败相互隔离；首次 `mounted + activated` 重复请求已消除，从设计器返回仍会刷新一次。
> 4. **本机与正式数据治理**：本机备份为 `D:\dmwprogram\newdazipingtai\backups\20260809_163849_local_before_teacher_dashboard_grade_history_bf23575\xueyeceping_server_20260729.sql`，74,808,538 bytes，SHA-256 `43BB5B7A71BE15582730A1E95C83FDFD4D9D49BDFD36FB7B04B13BCDF6D52153`。正式发布前完整备份为 `D:\program\3009dazipingtai\backups\20260809_172512_b1801ac_before_teacher_home_grade_history\ry-vue.sql`，76,162,191 bytes，SHA-256 `57BFDA751798999BEF857836BE407551D08C122C1F7BDB9210783C11DD342B9E`；6 文件清单 SHA-256 `72A63D646131BF87180397DA552DA012507BAC32E9E7F64EB676F7896788B583`。`sql/teacher_dashboard_opening_grade_lesson_num_v1.sql` 在本机和正式库首次、重复执行均成功；只把 2025 级七年级课程 263～266 精确修正为第 1～4 课、八年级课程 267 修正为第 1 课。2024 级八年级 12 门课的重复/缺号存在歧义，本轮明确不自动重排。
> 5. **验收证据**：后端专项 19/19、业务模块全量 265/265、admin clean package、Vue3 生产构建（2678 modules）均通过。本机教师 API/Playwright 冒烟 16/16。正式环境使用郑东旭教师账号密码并固定初中部 169 复验 16/16：核心课程接口约 106.2 ms，批改状态约 3551.8 ms 但不阻塞课程；2024 级当前九年级空栏新建为第 1 课、八年级 12 门历史课折叠/展开正确；2025 级七年级 1～4 与八年级第 1 课分栏正确；辅助状态失败时课程仍保留；首次核心请求 1 次、从设计器每次返回各刷新 1 次；无页面错误和 500。正式报告 `output/playwright/teacher-home-grade-history-server-b1801ac-smoke.json`，截图 `teacher-home-grade-history-server-b1801ac-01-collapsed.png`、`teacher-home-grade-history-server-b1801ac-02-expanded.png`。
> 6. **正式发布、回滚与剩余风险**：功能提交 `b1801ac` 已创建但尚未 push。统一 release `D:\program\3009dazipingtai\releases\20260809_172512_b1801ac` 已切换；JAR SHA-256 `054469608B7B0219A4CBC0BAB8D7EA9949E7D2AD1F7831B4779A5D8BEE5A482E`，前端 index SHA-256 `996C658AC2DF1AD5083F6A8B78AA12B6F34E8ECF2085A175D119263C56F785F8`。3009/3010/API 代理均为 200，服务环境变量保持，启动 ERROR=0、stderr=0。应用回滚分别切回后端 `20260809_152820_0a6a6e4`、前端 `20260809_161949_5bcc985`；若需撤销课次修正，应依据正式备份中的 `target-lessons-before.tsv` 做精确恢复，不能直接全库覆盖发布后的新业务数据。批改状态接口正式实测仍约 3.55 秒，但已移出首屏阻塞链路，后续可另立批量聚合优化。

> **更早焦点（2026-08-09：操作题 AI 批改 v4 与教师批改页顶部单卡片布局已发布 10.52.1.123；全班重新生成仍只产出建议，教师可在批量采用时选择“仅补未评分”或经二次确认“覆盖已有评分”，每份覆盖保留前后审计；真实学生准确率与隐私门禁仍未据此解除）**
> 0. **本轮产品结论**：已确认采用“逻辑作品 + 不可变提交版本 + 多附件 + 统一静态预览 + AI 草稿由教师确认”的路线。Office/PDF 主作品单文件，图片 1～10 张，单文件 50 MiB；压缩包只作教师资源；普通课程先接千问视觉模型，豆包做上线前盲测，区域抽测暂不启用 AI。专题文档见 `contexts/operation-artifact-ai-grading/`。
> 1. **P1 已完成代码**：普通课程分项满分改由服务端最大余数法分配，分项上限总和严格等于题目满分；评分保存增加题目归属、总分、分项归属/完整性/重复/上限/合计校验，并锁定答案行，以提交时间和预期旧成绩拒绝陈旧请求。直接打分会清除旧分项明细。
> 2. **P1 前端收口**：教师批改保存期间禁止换人、翻页和重复回车；评分项、评分明细异步请求增加序号与答卷归属检查，迟到响应不再覆盖当前学生；前端不再自行逐项四舍五入，直接使用服务端绝对上限。
> 3. **P1 验收证据**：`mvn -pl ruoyi-business -am '-DforkCount=0' test` 为 235/235；`mvn -pl ruoyi-admin -am clean package '-DskipTests'` 成功；`npm run build:prod` 成功（2677 modules，仅既有 vform `eval` 与大 chunk 警告）。真实教师接口/浏览器专项 10/10：陈旧提交和超满分请求均拒绝且数据不变，保存中换人/翻页/重复提交锁定，失败不跳人、成功才跳下一位，无页面错误和 500；报告 `output/playwright/practical-grading-p1-smoke.json`，截图 `practical-grading-p1-lock.png`。默认 fork 测试曾因本机 Surefire 子 JVM `Access denied` 失败，改为不 fork 后全量通过，属于本机测试运行器限制。
> 4. **P2 作品闭环已完成**：新增逻辑作品、不可变版本、附件、题目材料四表；题目增加允许格式与图片上限，答题绑定当前作品/版本。学生采用“逐文件暂存 → 明确提交新版本”，Office/PDF 单文件、图片 1～10 张、单文件 50 MiB；扩展名、MIME、文件头、OOXML 内部结构、大小和 SHA-256 均由服务端校验。教师出题已拆分学生起始文件、补充资源、教师参考材料，参考材料不下发学生；教师批改页支持附件清单/多图切换，并继续保留回车下一位与 P1 竞态门禁。
> 5. **P2 本机迁移事实**：执行 `sql/operation_artifact_v1_preflight.sql`、`operation_artifact_v1.sql`、`operation_artifact_v1_postcheck.sql`。迁移前备份为 `D:\dmwprogram\newdazipingtai\backups\20260804_180622_local_before_operation_artifact_a694428\xueyeceping_server_20260729_before_operation_artifact.sql`，64,329,458 bytes，SHA-256 `86E87FF0454EC5C6D2871C4756D427680A4A7E9E691401C4C470D4376B721D3D`。回填作品/版本/附件各 12,732，题目材料 95；重复作品、重复版本、重复附件顺序、孤儿、非当前状态、漏回填、无效题目策略均为 0。历史数据统一标记 `LEGACY_UNVERIFIED`，未移动或删除原文件，未改变历史成绩。
> 6. **P2 验收证据**：业务模块全量测试 238/238；admin clean package 成功；Vue3 生产构建成功。真实 API 用未提交测试学生完成“两张 PNG 暂存 → 同一版本提交 → 两附件均直接预览 → 删除当前版本”闭环。Playwright 冒烟 5/5，无页面错误和 500；报告 `output/playwright/operation-artifact-p2-smoke.json`，截图 `operation-artifact-p2-student.png`、`operation-artifact-p2-teacher-grading.png`。
> 7. **P3 统一页图底座已完成并补齐恢复能力**：`biz_practical_attachment` 与 `biz_county_exam_answer` 均使用独立规范化状态、页图 JSON、渲染器版本、重试次数/时间和错误信息；PDF 由 PDFBox 2.0.32 按 120 DPI 输出最多 50 页 JPEG，图片安全解码、白底和最长边 1800 像素归一化。DOC/DOCX 继续走 JODConverter 常驻池；PPT/PPTX/XLS/XLSX 改为最多 2 个并发、120 秒硬超时的一次性 `soffice.com` 进程，用户目录放系统短临时路径，规避 Windows 深路径静默失败和常驻管道卡死。新附件已纳入每小时恢复及 LibreOffice 自愈后的即时恢复，失败任务和超时卡住任务按原 3 次上限原子重置/认领。
> 8. **P3 缓存与权限结论**：哈希 + 渲染器版本命中时复用渲染结果，但页图复制到当前附件独立目录，禁止跨学生共享资源 URL；本人通过 `/common/resource/view` 读取为 200，他人学生为 403，直接访问 `/profile/**` 仍为 403。题目参考材料继续只允许管理员或具备题目权限的教师/教研员读取。
> 9. **P3/P5 正式环境匿名合成验收**：在隔离班级用 3 个临时学生分别提交 PPTX（2 页）、XLSX（1 页）和 PNG+JPG（各 1 页），所有附件均完成 PDF/页图规范化；本人/任课教师读取为 200，其他学生为 403。教师页可连续切换 3 人作品；百炼 `qwen3.7-plus` 班级任务为 `COMPLETED`、3/3 成功，建议分 30/30/0，正式成绩前后均为空。浏览器无页面错误和 500，报告 `output/playwright/student-multiformat-server-e2e.json`。该样本完全合成且不含真实学生信息，只证明批量链路可运行，不证明评分准确率。测试后临时用户、学生、答案、作品/版本/附件、AI 任务、指派、期限、孤立快照和 7 个作品目录均已精确删除；作品/版本/附件恢复为各 12,732，AI 任务/结果为 0。
> 10. **P4 不可变评分快照已完成**：新增 `biz_practical_rubric_snapshot`，课程保存时按题干、课程题目满分、评分项和教师参考材料生成新版本；每个 `biz_practical_submission_version` 在提交时绑定快照，教师评分和评分项接口按提交版本读取，题库后来修改不再污染旧作品。迁移前备份 `D:\dmwprogram\newdazipingtai\backups\20260804_190127_local_before_operation_rubric_a694428\xueyeceping_server_20260729_before_operation_rubric.sql`，73,700,640 bytes，SHA-256 `B1AFDCAB5F866BAC9753A70087ADD5AD2ABD003A475D71C9ADC500EC64DC8836`。历史 3 组已移除课程关系的题目按 `sys_oper_log` 提交前证据恢复 40/40/1 分；快照 106，未绑定版本、非法快照、孤儿绑定均为 0。真实教师接口返回 40 份提交，样本版本绑定快照 100，满分 30，2 个评分项上限合计 30；不可变性单测 3/3。
> 11. **P5 AI 草稿批改已发布且默认不自动写成绩**：主供应商采用阿里云百炼，默认模型 `qwen3.7-plus`，`qwen3.6-flash` 为降本选项；豆包只用于后续匿名同样本盲测。新增教师配置、班级任务、逐份结果 3 表；Key 使用部署主密钥派生的 AES-256-GCM 加密，前端只见末四位且可替换、连通测试、删除。后台独立线程池支持进度、逐份失败隔离、暂停/继续/取消/失败重试；输入不含学生姓名/学号。输出使用 `operation-rubric-v1` 严格 JSON，服务端重验评分项集合、逐项上限、合计和总分；建议只填入教师评分框，仍须教师点击原有提交按钮，绝不直接写正式成绩。
> 12. **GitHub 与正式发布事实**：代码已提交并推送 `codex/research-activity-v1`，现有草稿 PR #3 已包含操作题 AI v3 提交 `a8b80ac`、批改页布局热修 `997f57b`、AI 覆盖审计提交 `0a6a6e4` 及此前发布/恢复提交。当前正式 release、备份、制品哈希和验收事实以第 17～21 条为准；旧 release 均保留。
> 13. **主密钥、教师 Key 与真实模型验收**：服务器已用密码学安全随机数生成 `PRACTICAL_AI_MASTER_KEY`，仅写入 `NewDaziBackend3009` 服务环境；含主密钥的发布后注册表备份只保存在服务器受保护备份目录，其 SHA-256 为 `938E80480C193C85285A78728E8E154033F9BDCD6FDE844D39B5C33327FEA03A`。郑东旭教师账号切到 `deptId=169` 后成功保存个人百炼 Key；接口不返回明文，正式库仅 1 条 `v1:` 密文且 `sk-` 明文前缀记录为 0。使用后端生成的无学生信息测试图调用 `qwen3.7-plus` 返回 200；重启 3009 后不重存 Key 再测仍为 200，证明主密钥持久化与密文解密有效。正式 3010 页面冒烟 14/14，无页面错误和 500，报告 `output/playwright/operation-ai-server-ui-smoke.json`，截图 `operation-ai-server-settings-success.png`。
> 14. **仍未解除的产品门禁**：已完成匿名合成作品的 3 人批量技术验收，但不得据此宣称评分准确率、证据质量、真实班级时延/成本或隐私合规已验收。学校授权、百炼数据留存/训练策略审查、备份日志审查和千问/豆包匿名同样本盲测完成前，真实学生作品批量调用继续禁止。本机克隆中的 44 份历史 Word 和唯一既有区域操作题物理文件缺失，历史批量转换与区域实体样本仍待补验。
> 15. **P5 v2 闭环与正式发布（方案 A）**：创建任务前默认“仅批改未批学生”，可选全班重新生成对照建议；课程级“教师参考答案”为新任务必填，并与空白起始材料一起冻结到任务快照，提示词升级为 `operation-rubric-v2`。刷新或离开后重进批改页会恢复最近任务和逐份结果，建议卡片直接显示第一题/第二题等分项得分、上限和理由。批量采用只写入点击当时仍未人工评分且提交版本未变化的答卷，已有人工成绩永不覆盖；旧 v1 任务因没有参考答案快照只能查看或取消，不能继续或批量采用。相关决策见 `contexts/operation-artifact-ai-grading/ADR-004-ai-scope-reference-and-safe-batch-adoption.md`。
> 16. **P5 v2 验收与发布证据**：后端业务全量 258/258、admin clean package、Vue3 生产构建（2678 modules）均通过。增量 SQL `sql/operation_ai_grading_v2.sql` 已在正式库执行，后检为新表 1、任务字段 3、结果字段 3。发布前完整备份为 `D:\program\3009dazipingtai\backups\20260809_121423_0d93535_before_ai_v2`，SQL 75,873,300 bytes，SHA-256 `F38346857D6B39CC6DF847E5C60AA1A0EABE9033ED39FC27DC4F77B574529596`。当前 release 为 `20260809_121339_0d93535`，上一版 `20260807_165505_4dcb696` 保留；JAR SHA-256 `83620591E1DB126D6F01F073DA81DF0049D41E3FE131C882E6CEE616D98102BF`，3009/3010 均为 200，启动后 ERROR=0、stderr=0，原主密钥保持不变。应用回滚只需把 NSSM 与 3010 Nginx root 切回上一 release；v2 字段向后兼容，可保留，若必须数据库级回滚则恢复上述完整备份。
> 17. **正式任务 3 的真实结论**：用户后来上传教师参考答案并自行发起课程 237、2024 级 6 班、题目 1281 的全班任务。任务并未卡死，实际从 12:34:51 运行至约 12:48:45，最终 `PARTIAL_FAILED`：44 份中 42 成功、2 失败；两份失败均因“AI 返回的总分不符合评分标准”被服务端拒绝。44 条建议均为 `NOT_APPLIED`；正式答卷仍为 44 份、43 份有人工作业分、1 份未批，未因本轮诊断、迁移或发布改变。此事实不能替代学校授权、供应商数据策略或评分准确率验收。
> 18. **P5 v3 可观测性与恢复**：教师页新增“AI 处理详情”，展示已结束/处理中/等待、当前学生与阶段、运行/平均耗时、ETA、最近心跳、逐份状态和失败原因，以及不含密钥、完整提示词、模型原文、作品内容和堆栈的安全事件。处理阶段为 `PREPARING_STUDENT → REQUESTING_MODEL → VALIDATING_RESULT → COMPLETED/FAILED`；6 分钟无心跳只告警，不并发启动第二线程。教师参考答案与空白起始材料改为任务级单次准备并持久化页图缓存。应用启动自动收口取消请求、把中断中的 `PROCESSING` 退回等待并只接续未完成项；成功/失败项不重复调用。
> 19. **P5 v3 验收与正式发布**：`sql/operation_ai_grading_v3.sql` 本机首次/重复执行均成功，任务字段 4、结果字段 5、事件表 1；本机相关表备份为 `D:\dmwprogram\newdazipingtai\backups\20260809_125705_local_before_ai_v3`，6,555 bytes，SHA-256 `049858604C90EB01A2636BF177E5837DFFFA18865803B2B7D9F1A429CD3D3CA8`。后端业务全量 261/261、admin clean package、Vue3 生产构建和本机 18080 探活通过。正式发布前完整备份为 `D:\program\3009dazipingtai\backups\20260809_131054_a8b80ac_before_ai_v3`，SQL 75,975,475 bytes，SHA-256 `11EBAE17C3C13AA30CE7E711E9EDC482D78434496B5ACEF5BBE21FA14F171F29`。AI v3 后端 release `20260809_131500_a8b80ac`，上一完整版本 `20260809_121339_0d93535`；JAR SHA-256 `7244B11ECC0F803F4A3144B39C01FE94C7FB95E788E8E8405DC302859A56F858`，该版前端 index SHA-256 `ACB2947CD2B76DF90B51A1BAD4E9FD69A8AC95B5570B8F9E417CAE74542D7964`。3009/3010 为 200、主密钥保持、启动后 ERROR=0/stderr=0、活动 AI 任务=0。正式教师浏览器/API 只读验收 18/18，刷新恢复正确且页面错误/500=0；报告 `output/playwright/operation-ai-v3-observability-server.json`，截图 `operation-ai-v3-observability-server.png`。
> 20. **教师批改页 125% 缩放布局热修**：问题不是预览或学生数据损坏，而是有效视口约 `1536×813` 时顶部操作区换行，程序自动聚焦右侧分项评分框后把 `overflow:hidden` 的整个 `.grading-main` 纵向滚动约 371 px，三栏一起上移并留下大片空白。提交 `997f57b` 使用 `focus({ preventScroll: true })` 并把主工作区滚动归零，同时让右侧评分栏独立纵向滚动，保留回车连续批改。Vue3 生产构建成功（2678 modules，仅既有警告）；前端已发布到 `20260809_133739_997f57b`，后端未重启并继续运行 `20260809_131500_a8b80ac`。Nginx 配置检查/重载均为 0，3010 首页和 API 代理均为 200；新前端 index SHA-256 `278F2D598EA9B467136973C786C5904D3F8FA79DEB98C92274E8AD77328EF1D7`。Playwright 在 `1536×813` 与 `1909×1013` 两种视口均确认主工作区 `scrollTop=0`、三栏上下边界一致、控制台错误 0；截图为 `output/playwright/grading-layout-scaled-125-fixed.png`、`grading-layout-wide-fixed.png`。无需 SQL 或后端重启；回滚只需把 3010 Nginx root 切回 `20260809_131500_a8b80ac/frontend`，其配置备份位于服务器 `backups\20260809_133739_997f57b_frontend_layout`。
> 21. **P5 v4 显式覆盖正式成绩与审计（方案 A）**：全班重新生成建议本身仍不写成绩；教师点击批量采用后先选择 `FILL_UNGRADED` 或 `OVERWRITE_ALL`，覆盖模式再显示影响人数并二次危险确认。两种模式继续逐份执行答卷行锁、提交版本、评分快照、分项上限/合计、总分和期限校验；覆盖模式只放开“已有成绩跳过”门禁。新增 `biz_practical_ai_apply_audit` 保存每次成功采用的旧/新总分与分项、任务、建议、提交版本、模式、教师和时间；旧 `/apply-ungraded` 固定安全模式。专题决策见 `ADR-005-explicit-ai-overwrite-with-audit.md`。本机完整备份 74,803,349 bytes，SHA-256 `C1F386E3F4FC017377B9FEC283DFD8CFF052DDB41B64DF308FBA4830EA7A52FB`；v4 SQL 首次执行后为 1 表/12 字段，专项 3/3、业务全量 262/262、admin clean package、Vue3 2678 modules 均通过。正式备份 `D:\program\3009dazipingtai\backups\20260809_152820_0a6a6e4_before_ai_v4\ry-vue.sql` 为 76,029,463 bytes，SHA-256 `9AF5C0377406E17350676EEC7764C667B97E7CD66348AEDCF35CB023D6E35268`；SQL 首次/重复执行均为 1 表/12 字段。统一 release `20260809_152820_0a6a6e4` 已切换，JAR SHA-256 `5CBB9577AF65485E6B1D8B67873C3FC245BC0AA4555779B723380A2F45D55878`，前端 index SHA-256 `F4F8C5D438C5A1E670E1E4BF17A82A340246D14DC9829CD97189625A1D55F0FC`；3009/3010 为 200、主密钥保持、ERROR=0、stderr=0。正式页面只读验收 9/9：准确展示未评分 0、已有正式成绩 44，两级覆盖确认后返回，采用请求 0、控制台错误 0；截图 `output/playwright/ai-v4-overwrite-confirm.png`。终检审计记录 0，课程 237/题目 1281 的 309 条成绩仍全部有分，任务 3 仍为 `COMPLETED 44/44`。应用回滚切回后端 `20260809_131500_a8b80ac`、前端 `20260809_133739_997f57b`；新增审计表可保留。未来若真正执行覆盖，应用回滚不会自动恢复旧成绩，须依据审计记录做有范围恢复。
> 22. **教师批改页顶部合并热修**：提交 `b1715d8` 把课程/班级/操作题筛选、AI 操作与批改期限由两张卡片合并成一张；提交 `5bcc985` 进一步把触发/截止时间和进度条排在同一行。状态区不再重复课程名、班级和“操作题批改时限”，只显示批改进度、状态、答题/应批统计、剩余时间及触发/截止时间。Vue3 生产构建成功（2678 modules，仅既有警告）；正式 3010 前端 release `20260809_161949_5bcc985`，index SHA-256 `1246CFA968CF8C0191E6C5F51FB86131F1EF1C1F881F236A26460A57BC22AC6D`，后端 3009 未重启并继续运行 `20260809_152820_0a6a6e4`。Nginx 配置检查/重载、3010 和 API 代理均正常。正式 Playwright 在 `1909×1013` 与 `1536×813` 为 22/22：顶部单卡片高度分别为 142px/206px，较旧双卡片释放约 54px/22px；状态区属于顶部卡片且无 `el-card` 祖先、重复标题不存在、三栏对齐、主区 `scrollTop=0`、控制台错误和 500 均为 0。截图 `output/playwright/grading-header-merged-wide.png`、`grading-header-merged-scaled-125.png`。无需 SQL 或后端重启；前端回滚点为 `20260809_152820_0a6a6e4`，Nginx 备份在服务器 `backups\20260809_161949_5bcc985_frontend_merge`。
> 1. **多校免抽测修复**：教师按登录态当前学校分别生成预览和申请，身份校验同时接受 `sys_user.dept_id` 主学校与 `sys_user_dept` 关联学校。郑东旭账号 `19157727791` 在本机切换到大目湾学校初中部（169）后，预览接口返回 200 且 `deptId=169`，不再误报“当前教师未绑定有效学校”。
> 2. **教师申请与批改入口**：免抽测页默认展示历史记录，明确提供“新增申请”，按钮统一为“生成申请预览”；无真实任教班级时禁止提交并提示检查当前学校、管班和课程指派证据。教师首页课程卡片保持 `200×140`，红点班级范围已统一为“当前指派 + 历史答题事实”，修复第 8、10 课旧指派缺失导致的漏提醒；当前可见第 11、10、8 课均显示红点。批改与成绩弹窗统一为 900px、至少 360px 内容高度且每行两个班级。教师“免抽测申请”菜单改用可继承侧栏颜色的 `documentation` 图标。
> 3. **状态收口**：所有已提交操作题全部批完后始终显示“已批改”，即使截止时间已过也不改写成“已逾期”；逾期后底层 `canGrade=false` 仍保持锁定。重新开放按新截止日期展示，不作为教师端独立状态；六种底层期限状态仍保留用于控制和审计。
> 4. **本机菜单与 SQL**：已在本机库执行幂等 `sql/teaching_supervision_ui_refinement_v2.sql`。复核结果：`课程与成绩监管` 与 `免抽测申请审核` 均为根级 C 菜单，`免抽测课数标准` 为审核菜单子权限；教研员三项授权均为 1。原始 `teaching_exemption_v1.sql` 已同步新菜单结构。
> 5. **验收证据**：后端业务全量 231/231、admin clean package 成功；Vue3 生产构建成功（2677 modules，仅既有 vform `eval` 与大 chunk 警告）。原课程监管/免抽测只读 Playwright/API 冒烟 10/10；教师首页跟进冒烟 6/6，确认 `lesson_id=237/227/201` 三张可见卡片均有红点、菜单图标为 `#icon-documentation`、批改与成绩弹窗均为 900px 双列布局且无 500/页面异常。跟进报告为 `output/playwright/teacher-home-red-dot-layout-smoke.json`，截图为 `teacher-home-refinement-01` 至 `03`。本机后端 8080、前端 80 已启动。
> 6. **既有稳定性事实**：`AI_FIX_20260731_001` 的交卷短事务、死锁退避重试、删除防护和 50 VU 2,766/2,766 成功结论继续有效；在线孤儿答案已归档治理，旧库未改动。历史课程时间、课程归属和跨校指派冲突仍按原结论待治理。
> 7. **状态校正**：本段原“未发布 `10.52.1.123`、正式服务器仍是 `1488206`”结论已经过时。依据用户 2026-08-11 明确确认，课程监管、操作题限期批改和教师免抽测均已完成；当时的具体发布目录、数据库备份及迁移哈希未在本次文档校正中重新核验，不能沿用旧行数或凭空补写。

---

## 〇、环境与连接（非机密）

| 项 | 值 |
| :--- | :--- |
| 内网主机 | `10.52.1.123` |
| 后端 | 端口 **3009**，NSSM `NewDaziBackend3009` |
| 前端 | Nginx **3010**（`/prod-api/`、`/ws/`） |
| 发布根目录 | `D:\program\3009dazipingtai\`（`releases\`、`backups\`） |
| Windows 用户名 | `Administrator`（密码见 secrets.local） |
| MySQL | 用户 `root`；业务库 **`ry-vue`**（2026-07-22 只读核实）；密码见 secrets.local |
| 本机开发库 | `xueyeceping_server_20260729` @ localhost（服务器克隆 + 监管增量；旧 `xueyeceping1` 保留） |
| 私密凭据 | `contexts/secrets.local.md`（**禁止 git add / 禁止写入本文件密码**） |
| 当前开发分支 | `codex/research-activity-v1`，HEAD `ab6d0d6`；操作题预览热修、教师工具与 v2.74 上下文尚未 commit/push，用户要求不再新建分支 |

### 线上菜单差异快照（2026-07-22 只读 `ry-vue`）

| 检查项 | 结果 |
| :--- | :--- |
| 区域抽测菜单 | 存在 `menu_id=2047`，`component=business/countyExam/index`，教研员已授权 |
| 系统诊断中心 25010 | **已补齐**（执行 `researcher_monitor_menu_fix.sql` 后 =1） |
| 教研员监控子菜单 | **2 + 109 + 113 + 25010**；111/112/110/114 = 0 |
| 前端 3010 | 当前前端 release `20260810_122305_ab6d0d6_teacher_tools_visual`，后端仍为 `20260810_114800_ab6d0d6_teacher_tools_r3`；上一完整 release 与功能发布前版本均保留；80 与 3005 站点未改动 |

修复脚本：`sql/researcher_monitor_menu_fix.sql`（幂等）；`sql/platform_overview_diagnosis_menu.sql` 第 6/7 节已同步口径。

---

## 一、技术边界

| 路径 | 说明 |
| :--- | :--- |
| `RuoYi-Vue/` | 后端（`ruoyi-admin` 启动，`ruoyi-business` 业务） |
| `RuoYi-Vue3/` | **唯一前端**（Vue3 + Vite + Element Plus） |
| `sql/` | 增量 SQL（优先幂等） |
| `contexts/context.md` | **本文件（唯一业务真相）** |
| `contexts/secrets.local.md` | 密码（gitignore） |
| `output/playwright/` | 冒烟脚本与截图 |
| `output/stress/` | 压测与正式报告 |

**不要默认改**：`RuoYi-Vue/ruoyi-ui/`（旧 Vue2）；无必要大重构/换栈。

**Agent 默认行为**：先读顶部焦点；小改直接做；方案级先确认；任务指向内网时按 AGENTS 远程代操作；不 push 除非用户要求。

**重大修改上下文维护规范（2026-07-28）**：改变核心业务流程、接口或 DTO 语义、数据库表结构/迁移、权限边界、部署配置、跨模块行为，或完整功能/热修集中修改多个关键文件并需要系统回归时，AI 必须在本轮结束前更新本文件的版本、日期、当前焦点、已完成事实、测试证据、SQL/配置/重启要求、剩余风险、部署状态与下一步；涉及专题需求、设计、任务或架构决策时同步对应 `requirements.md`、`design.md`、`tasks.md` 和 ADR。只写已验证事实，上下文未更新不得宣称完成。小型文案或单点样式可不升级本文件，除非改变业务含义。上下文禁止写入密码、Token、Cookie 或私密路径内容；交接前必须确保新 AI 只读顶部即可知道当前真实焦点。


## 二、已定产品结论（2026-07-15 起，以代码为准）

### 2.1 课程用途

| 值 | 教师可见名 | 要点 |
| :--- | :--- | :--- |
| `assessment` | **常规课** | 可出题、绑导学单；可参与推进与作业均分 |
| `attendance` | **课堂考勤** | 可 0 题；签到；**不计作业分**；**永不自动/手动推进** |

- 教师「添加课程」：直接进设计器选用途，无用途弹窗。  
- 设计器文案：常规课 / 课堂考勤（不再叫「测评课」）。
- **届别与开设年级**：`biz_lesson.entry_year` 是稳定入学届别；`grade` 是课程创建时的开设年级。两者创建后均不可在普通编辑中改变。全平台学年切换日为 **每年 7 月 20 日**。
- **已毕业届别课程可见性（2026-07-22 确认，方案 A）**：教师首页某一 `entry_year` 分组（含「已毕业」）下，课程卡片应对 **本校 + 该 `entry_year` + 创建人为当前教师（或历史任教/指派证据可证明教过）** 的课程全部展示；**不得**仅因管班表 `biz_teacher_class` 已清空/未续写该届而把整组课程误杀为空。管班记录仍用于班级维度能力，不是「历史课程是否出现在首页」的唯一门槛。
- **已毕业届别写操作（2026-07-22 确认，方案 A）**：本轮与在读届别 **同等可操作**（设计器、成绩、指派、推进等仍走现有权限）。不做「已毕业只读归档」；若日后需要只读策略，单独立项。
- **教师首页装课条件（2026-07-22 确认，方案 A）**：分组内装课 **不得**依赖 `gradeId > 0`。已毕业（`gradeId=-1`）、新生/未知等凡已有 `entry_year` 分组，均按 `entry_year` 查询自建课 + 共享课。根因：已毕业时 `calculateGradeInfo` 返回 -1，旧逻辑把 lessons 置空导致「有分组无卡片」。

### 2.2 导学单（面向教师）

- 新建：标题、年级、学期、第几课、可见范围。  
- **已去掉**：教学主题、预计完成时间。  
- 选模板：默认年级+学期，**不默认第几课**。  
- 课程创建者可读实际指派班级的进度/答卷；模板作者身份不产生课程成绩权限。

### 2.3 课程推进（教师级统一策略）

**入口**：教师首页「课程设置」右上角。

| 按钮 | 行为 |
| :--- | :--- |
| **手动一键课堂推进** | 选年级；班级多选默认全选；达有成绩比例则立刻切下一课 |
| **设置** | 自动推进开关、比例、等待小时；bulk 同步本校全部常规课 |

**接口**：`GET/PUT /business/lesson/advance-policy`；`POST /business/lesson/manual-advance`（`entryYear` + `classCodes[]`）。

**规则要点**：教师+学校维度；考勤永不推进；Quartz `lessonAutoAdvanceTask`；行锁+唯一索引；推进后 15 分钟补交窗口。

### 2.4 区域抽测状态机

草稿 → 开启 → 关闭 → 已发布。评卷入口与关闭/发布独立控制。每生试卷总分必须 100 分。状态仅经专用接口转换。

**评卷教师池（2026-07-22 确认，方案 A）**：配置匿名评卷时，下拉候选为 **全平台启用且角色为教师（`role_key=teacher`）的账号**，含同一人在小学部/初中部的两个独立账号；**不按**本场抽测学段或参考校自动排除。实现上须保证关键字搜索可命中，名单不得因固定 `LIMIT` 静默截断导致「明明有账号却选不到」。

**教师首页「区域抽测评卷」入口（2026-07-22 确认）**：**仅当**当前登录教师名下存在 **待评匿名答卷任务（待评份数 > 0）** 时显示。仅保存评卷配置、抽测仍为草稿/开启、或关闭后尚未生成/分配任务 → **不显示**入口。教研员侧应用文案/校验说明「关闭抽测后才会生成评卷任务」，避免误判功能缺失。

**评卷份数一键均分（2026-07-22 确认，方案 A）**：配置匿名评卷弹窗提供 **「一键均分」** 按钮：按该操作题当前可分配规模（有提交用提交数，否则用参考人数）把 `targetCount` 立刻写入各评卷教师行（余数前几人 +1）。**保留**「不填/0 = 关闭抽测生成任务时对剩余答卷自动均分」。关闭生成任务时仍以真实可分配答卷列表为准收口。

### 2.5 教研员菜单与监控（2026-07-22）

| 入口 | 教研员 |
| :--- | :--- |
| 区域抽测 | 仅 **一条** 动态菜单（静态路由 hidden） |
| 系统诊断中心 | **主排障入口**（须菜单 25010 + 角色授权） |
| 缓存健康 / 在线用户 | 可见 |
| 数据监控 / 原生服务监控 / 定时任务 / 缓存列表 | **不可见** |
| Druid | 默认关闭；管理员保留菜单时前端探测失败友好提示 |

### 2.6 关键代码索引

| 能力 | 前端 | 后端 |
| :--- | :--- | :--- |
| 教师首页/推进 | `teacher/index.vue` | `BizLessonController` advance-policy / manual-advance |
| 导学单 | `guideSheet/*` | guide-sheet API |
| 区域抽测 | `countyExam/*` | CountyExam* |
| 系统诊断中心 | `monitor/diagnosis/index.vue` | `SystemDiagnosisController` `/monitor/diagnosis` |
| 教师工具 | `teacherTools/index.vue`、`manage.vue` | `TeacherToolController` `/business/teacher-tools/**` |
| 原生服务监控 | `monitor/server/index.vue` | `ServerController` |
| 数据监控 | `monitor/druid/index.vue` | Druid servlet（可关） |
| 学生端 | `views/student/*` | `StudentHomeController` |

### 2.7 验收与发布备忘（摘要）

- 2026-07-21：导学单+区域抽测专项、三角色冒烟、WebSocket、学年热修等见第十一节全文。  
- 2026-07-22：代码侧双菜单/Druid 提示/菜单 SQL 已改；**正式库菜单补丁与前端发布按任务推进**。  
- **2026-07-23 已毕业课/评卷修复已上线（方案 A）**：见 §11.5。

### 2.8 教研活动（2026-07-28 已部署 10.52.1.123）

- 原平台论坛数据 **不迁移**，旧论坛入口 **不保留**；现有多板块结构全部取消，重构为单一 **教研活动** 信息流。
- 学生不参与。教师、教研员和管理员可查看、搜索和留言；教师与教研员均可发主题。主题分为 **活动通知** 与 **交流分享**，仅教研员/管理员可发布带定向通知的活动通知主题。
- 主题及三类留言均使用完整富文本，至少支持文字、表格、图片与超链接。留言类型为 **课堂反思 / 活动纪实 / 课程资源**；课堂反思内部枚举仍为 `COMMENT`，不调整接口与表结构。只有课程资源强制填写学段、年级、学期、第几课和课程标题。
- 富文本一期复用现有 **Quill 2**，以可选方式补充中文表格工具；须先完成图片、粘贴、表格、链接、回显和再次编辑兼容性验证，若现有 Vue 封装无法稳定支持 Quill 2 表格，则仅教研活动模块回退使用 WangEditor。
- 一条课程资源由 **课程结构化信息 + 课后反思富文本 + 资源文件/云盘链接** 共同组成，在一个连续表单中一次发布并显示为一条留言；链接和附件须独立结构化保存，不能只藏在富文本 HTML 中。
- 课件主文件支持压缩包，上传上限为 **50 MB**；超过 50 MB 时使用云盘等外部链接。外部资源记录包含链接名称、`http/https` 地址、可选提取码、**可选过期时间（可永久有效）** 和可选说明，展示时支持直接打开链接与复制提取码；已过期链接须明确标识。
- 搜索分活动主题和课程资源，默认活动主题；主题支持独立的创建时间、活动时间范围，资源支持关键词以及学段、年级、学期、第几课、作者、创建时间等结构化筛选，不引入 Elasticsearch。
- 活动通知统一生成站内通知，不再区分普通/重要；范围可按一个或多个学段，或单选/多选指定教师。发布时可选填未来的 **活动时间**：已设置时无论是否已读都持续显示在教师首页，达到活动时间后移出首页但保留在全部通知；未设置时仅未读显示。通知范围只控制首页提醒，不限制主题可见性。
- 主题 **不设草稿、开启或关闭状态**；新增成功后立即持续可用并允许继续留言。保留主题置顶、软删除、查看数、回复数和下载数；一期不做关闭回复、点赞、收藏、积分或排行榜。
- 教师端与教研员端均保留 **教研活动** 主菜单入口。

**实现、发布与验收结果（v2.48）**：

- 数据库：`research_activity_activity_time.sql` 增加可空 `activity_time`；`research_activity_multischool_notice_backfill.sql` 只补仍未开始活动的缺失多校教师接收人，不重置已有已读状态。本机主题 15 接收人已由 25 补齐至 31，缺失数复核为 0。
- 后端：统一前缀 `/business/research-activity`；按学段投递同时计算 `sys_user.dept_id` 主学校和 `sys_user_dept` 多校关系，避免同一账号切换校区后漏收；主题查询分别支持创建时间和活动时间范围。
- 前端：Vue3 主页面默认 **活动主题**，顶部提供创建时间、活动时间两个独立筛选；有活动时间的通知卡片在右上角显示时间；教师首页 `.teacher-dashboard` 第一个内容块为 `ResearchNotificationBar`。
- 编辑器：公共 Quill 2 仅增加默认关闭的可选表格/上传/缩放参数；教研活动启用表格、JPG/PNG/WebP、单图 10 MiB、最多 20 图。图片支持文件多选、按选择顺序上传、单张失败不阻断，以及点击后拖拽右下角控制点缩放；缩放宽度写入正文并在刷新、再编辑后保留。表格一键插入 3×3，并支持增删行列、删除整表。
- **图片阻断根因与修复**：上传接口原返回 `/profile/upload/research-activity/images/...`，但 `/profile/**` 受安全配置保护；普通 `<img>` 请求不能附加 Bearer Token，因此收到业务 401 JSON 而非图片，数据库 HTML 虽保留 `<img>` 仍无法显示。上传响应现改为 `/common/resource/view?resource=...`，复用该接口的 Cookie 鉴权回退；仅为 `research-activity/images/*.webp` 开放 WebP 预览白名单，并保持路径穿越校验。`sql/research_activity_image_url_fix.sql` 幂等迁移旧 `/dev-api|prod-api/profile/...` 正文 URL，本机已执行，旧 URL 余量为 0。
- **编辑标识边界修复**：MySQL 秒级时间会让“创建后同一秒编辑”的 `update_time > create_time` 判定失效。新建主题/留言改为 `update_time=NULL`，真正编辑后再写时间，查询以是否存在更新时间判断；接口实测新建为未编辑、同秒编辑后为已编辑。
- **图片专项 15/15**：覆盖 JPG、PNG、WebP、中文名、粘贴图片、多图、接近 10 MiB、超限、伪装文件、未登录/学生读取、缺图降级，以及即时预览→保存→详情→刷新→再编辑；报告 `output/playwright/research-activity-image-e2e.json`。修复前网络/控制台/数据库证据见 `research-activity-image-before-fix.json`。
- **批量上传与缩放专项 8/8**：一次选择 3 张合法图片和 1 个非法文件，合法图片按选择顺序全部插入，非法文件单独失败且不阻断；图片从 120 px 拖拽到 260 px，保存、刷新与再编辑后宽度仍保留，无页面或控制台错误。报告 `output/playwright/research-activity-batch-resize-e2e.json`。
- **功能回归**：真实角色 API 20/20、浏览器主链路 15/15、表格完整生命周期 12/12、活动时间 15/15、多校与双时间 12/12、详情返回 4/4、课程资源 multipart 边界 5/5。覆盖发布/编辑/置顶/隐藏/恢复/再次通知、已读/全部已读、三类留言、结构化资源、授权下载、云盘、XSS、学生拒绝与多校去重；报告均在 `output/playwright/`。
- **构建门禁（2026-07-28 重跑）**：`mvn -pl ruoyi-admin -am clean package` 成功，业务 193/193、admin 3/3；前端纯函数 7/7，`npm run build:prod` 成功；批量上传/缩放 8/8、图片闭环 15/15、浏览器主流程 15/15。构建仅有既有 vform3 `eval` 和大 chunk 警告。
- **角色边界**：教研员、教师、学生已在服务器做浏览器与 API 验收；学生无菜单、接口拒绝。管理员在正式库的菜单与 8 个权限点已核验，因项目未提供管理员 Web 密码且无现成登录态，未做管理员交互登录；教研员已覆盖发布、置顶等管理操作，此项保留为明确验收缺口。
- **数据清理**：本轮 `[AI测试]` 主题/留言、通知、资源记录余量为 0；删除 23 个测试图片和 7 个测试 ZIP。保留主题 15 正文正在引用的用户图片/附件，未误删业务数据。
- 性能：本机生成并清理 20,000 条代表资源；结构化筛选 P95 34.801 ms、关键词 P95 41.864 ms，精确课程标题排首位，清理余量 0；报告 `output/stress/research-activity-performance.json`。
- **正式发布**：以已 push 的完整提交 `14882068287f349378b9ee52476bbe21c6c7e994` 构建并发布到 `20260728_151733_1488206`；数据库/配置备份、四个 SQL、Nginx 60m、NSSM/Nginx 切换、探活和服务器专项回归均完成，详见 §11.6。应用回滚切回上一 release，新表保留不 DROP；正文 URL SQL 为向前兼容修复，无需回滚。

### 2.9 课程与成绩监管、操作题限期批改（已完成；2026-08-11 状态校正）

- **专题文档**：`contexts/teaching-supervision/requirements.md`、`design.md`、`tasks.md`、`adr/adr-001-course-class-fact-and-deadline-model.md`。
- **数据模型**：`biz_lesson_class_scope` 固化课程班级事实；`biz_practical_grading_deadline` 固化首次触发和当前有效截止；`biz_practical_grading_deadline_audit` 保存延期/重新开放审计。动态参与和批改数量仍从现有学生、答案和表现表计算。
- **历史迁移文件**：执行前 `sql/teaching_supervision_v1_preflight.sql`，执行 `sql/teaching_supervision_v1.sql`，执行后 `sql/teaching_supervision_v1_postcheck.sql`。现有功能状态已校正为完成；未来迁移到新环境时仍须重新备份和复核，不能直接照搬历史行数。
- **服务器复制源备份**：`D:\dmwprogram\newdazipingtai\backups\20260729_180020_server_before_local_clone_d827415\ry-vue_server_full.sql`，62,283,806 bytes，SHA-256 `350E73232CB17F0C4D8EC0B973EAE47837976B1752E70ABE8CE686ADA5236BED`。原本机库迁移前备份仍保留在 `20260728_174812` 目录。
- **期限规则**：含操作题的课程班级在有效学生中已有答题记录人数首次达到 50% 时触发；分母 0 不触发。全局配置 `business.practicalGrading.deadlineDays=21` 只影响以后触发；`business.practicalGrading.goLiveTime` 固定历史初始化时间。实时提交使用事务提交后检查，Quartz `practicalGradingDeadlineTask.reconcileTriggers` 每 10 分钟补偿。
- **状态与锁定**：状态为 `NO_PRACTICAL / NOT_TRIGGERED / GRADING / DUE_SOON / COMPLETED / OVERDUE / REOPENED`；即将到期为 72 小时。教师评分在原数据权限校验后、任何写入前检查期限；逾期只读。教研员/管理员调整必须填写原因，乐观更新期限并在同一事务写审计。
- **监管口径**：监管学年与平台统一在 7 月 20 日切换；课程严格按 `biz_lesson.dept_id` 归校；服务端分页到学生层。学校/教师/课程先按课程预聚合；状态筛选先按 `lesson_id + dept_id + entry_year + class_code` 一次计算班级状态，再筛课程和班级，避免多班答案串算及相关子查询性能退化。旧课程无创建/修改时间时，仅用课程班级事实证据时间做监管学年归档，不回填课程字段。成绩沿用现有作业分（含最新人工改分）+课堂表现、请假排除、考勤课不计均分口径。
- **事实生命周期**：课程改派先将同课程其他当前事实置为非当前，再删除旧指派并写入新事实；删除课程时在同一事务按审计→期限→事实→课程的依赖顺序清理，避免新增事实表产生孤儿记录。
- **权限**：`business:teachingSupervision:view/export`、`business:practicalDeadline:config/adjust` 独立；控制器同时要求教研员/管理员角色和对应权限，方法级权限不能绕过角色边界。研究员本机已授权 4 项，教师和学生监管/配置 API 均实测 403。研究员读取学生操作题附件仍走资源归属校验。
- **验收证据**：当前本机库为服务器克隆 `xueyeceping_server_20260729`，迁移后事实 873、当前事实 129、历史期限 473，重复组、零分母、无操作题期限和孤儿事实均为 0；后端 clean package 成功，业务 213/213、admin 3/3；Vue3 学年边界测试 3/3、生产构建成功。2025 学年 7 种状态课程接口均 200，耗时 0.754～2.408 秒；`NO_PRACTICAL=108`、`NOT_TRIGGERED=20`、`GRADING=66`、`COMPLETED=55`，其余当前为 0，四种有数据状态的抽样班级与课程筛选一致。学校汇总分页总数 19，三次耗时 3.991～4.387 秒；教师层 1.539 秒、课程层 0.827 秒。此前 Playwright/API 26/26 报告和截图仍在 `output/playwright/teaching-supervision-*`；本次只读复核未重跑会改期限的完整浏览器链。最终 fat jar 已在本机 8080 重启并探活。
- **历史构建/重启要求**：后端改动涉及业务模块、Quartz 和 Mapper，迁移到新环境时必须重新打 fat jar 并重启；前端必须重新 `build:prod` 并切换 dist。只部署代码而未执行目标库迁移，功能不可用。
- **完成状态与审计边界**：依据用户 2026-08-11 明确确认，本功能已经完成，原“未发布服务器”结论作废。本次未重新核验当时的 release、正式库备份和 SQL 执行哈希；需要追溯部署或回滚时，应先读取服务器现存发布与备份记录，不得按本节历史本机证据推断。

### 2.10 教师免抽测、真实使用日期与课程时间治理（已完成；2026-08-11 状态校正）

- **专题文档**：需求、设计、任务跟踪见 `contexts/teaching-supervision/requirements.md`、`design.md`、`tasks.md`；快照与真实活动决策见 `adr/adr-002-exemption-snapshot-and-real-activity.md`。
- **历史迁移顺序**：目标库依次执行 `sql/teaching_exemption_v1_preflight.sql`、`sql/teaching_exemption_v1.sql`、`sql/teaching_exemption_v1_postcheck.sql`。迁移创建标准、申请、班级快照、课程快照、附件 5 表，补齐菜单与角色权限，并只修复未来课程时间默认值；历史空时间保持原样。现有功能状态已校正为完成，本顺序仅供新环境迁移或审计参考。
- **本机备份与后检**：迁移前完整备份为 `D:\dmwprogram\newdazipingtai\backups\20260730_233001_local_before_teaching_exemption_d827415\xueyeceping_server_20260729_before_teaching_exemption.sql`，62,678,787 bytes，SHA-256 `8095118C6486663DF9EF98F3AF14A4E5C51565F4AF79E1A68F8F5071371D5354`。后检确认 5 表存在；重复标准/申请、非法状态、孤儿和快照汇总异常均为 0；课程总数 228，迁移前后仍有创建时间空 206、修改时间空 225、两者均空 206。
- **统计与快照**：任教班级由当前管班、当前/历史指派和课程班级事实并集确定；参与只认答题、签到和课堂表现。教师预览 2025 学年第二学期六年级实测 9 班、76 课，操作题已批率 42.82%；12 并发提交仅 1 成功、11 被拒绝。申请经教研员审核和标准写入回读后，按测试备注和主键精确清理，申请及两级快照余量为 0。
- **接口与权限**：教师和教研员关键接口均返回 200；监管使用日期查询返回 14 所学校。教师调用审核、学生调用审核或教师预览均为 403。教研员拥有 `business:exemption:review/standard`，教师拥有 `business:exemption:apply`，学生相关权限为 0。申请附件读取继续经过资源归属判断。
- **性能与查询计划**：免抽测预览连续 25 次为 350～642 ms，P50 395 ms、P95 516 ms；监管学校查询连续 15 次为 1.123～1.276 s，P50 1.177 s、P95 1.276 s。统计 SQL 先按教师、学校、学年学期和班级范围收窄，再聚合三类活动；索引/执行路径审查未发现无界相关子查询。本轮修复了历史指派表与现表联合时的排序规则冲突。
- **自动化与浏览器**：`mvn -pl ruoyi-business -am test -DforkCount=0` 为 226/226；`mvn -pl ruoyi-admin -am clean package -DskipTests` 成功。Vue3 学年/学期与题型测试 6/6，生产构建成功（2675 modules；仅既有 vform `eval` 和大 chunk 警告）。系统 Chrome 浏览器冒烟覆盖教师预览、教研员标准/审核入口和监管日期下钻，截图保存在 `output/playwright/`。
- **完成状态与审计边界**：依据用户 2026-08-11 明确确认，教师免抽测、真实使用日期与课程时间治理已经完成，原“正式发布待执行”结论作废。本次未重新核验当时的 release、正式库备份和迁移哈希；未来迁移或回滚仍须先读取目标服务器真实记录，数据库不得直接删除已有申请快照。


## 三、历史交付（仍有效，摘要）

| 阶段 | 内容 |
| :--- | :--- |
| 成绩 | 分页懒加载；单课总分=作业+表现 clamp；多课均分；导出三列；请假不计均分 |
| 操作题 | previewStatus/Path；失败重试服务与 Quartz；批改页重转 |
| 答题 | 最新一条聚合；存在则更新 |
| 画像 | 学年维度（非学期） |
| 导学单 P0–P2 | 模板库/绑定、设计器、预览、提交墙、考勤空课等 |
| 区域抽测 | 清单/状态/匿名评卷/发布分析 |
| 系统诊断中心 | 聚合资源、缓存健康、慢 SQL、异常、慢接口、在线用户、任务说明 |
| 学年锚点 | `entry_year` NOT NULL；7 月 20 日切换 |

**被否决、不实现**：卡片密度大改、批改入口大改、**学生机位锁/固定座位**（main 中长方案仅作否决归档，见第十七节）。


## 四、上线前检查清单

1. 目标库备份 + 前置检查后执行任务相关 `sql/` 脚本（菜单类优先 `researcher_monitor_menu_fix.sql`）。  
2. 后端 `mvn -pl ruoyi-admin -am clean package`（改 yml/资源保留 clean）。  
3. 前端 `npm run build:prod`；仅 Vue3。  
4. 发布到新 `releases/<时间戳>_<hash>/`，切换 NSSM/Nginx，保留旧 release。  
5. 三角色冒烟；密码不进仓库与回复。

## 五、上线审查短提示

审查代理先读 AGENTS + 本文件。优先级：数据正确性 > 权限/安全 > 主路径可用性 > 体验。

## 六、上线审查修复摘要（v2.28，仍有效）

| 项 | 说明 |
| :--- | :--- |
| 批改 grade | 按 answer 归属校验本校/任教/创建人 |
| 课堂表现/签到 | 班级范围校验 |
| 推进与交卷 | 策略表、行锁、15 分钟补交、答案 upsert |
| 文件权限 | 授权预览下载；操作题专用上传 |
| 区域抽测 | 100 分、状态机、行锁、发布后撤评卷权限 |
| 默认配置 | Swagger、Druid 默认关 |

## 七、仓库整理（v2.26）

过程日志与过时接力文可删；保留源码、`sql/`、压测主报告、导学单部署说明、学年故障审计。

## 八、文档关系

| 文件 | 职责 |
| :--- | :--- |
| **本文件** | 业务真相、术语、结论、环境非机密、发布与事故 |
| **`AGENTS.md`** | 启动、验收、远程代操作、红线 |
| **`secrets.local.md`** | 密码（gitignore） |
| **`电子导学单部署说明.md`** | 导学单部署专题 |
| **`2026-07-21学年切换故障接力.md`** | 学年事故审计 |
| **`抽测系统方案.md`** | 抽测专题方案 |
| **`research-activity/`** | 教研活动已确认需求、系统设计、任务计划、架构决策与新 AI 开发提示词 |

冲突时：**业务以本文件为准；操作以 AGENTS 为准。**

---

## 九、业务语言（从 main 回填，仍有效）

**区域抽测**：
由教研员或管理员面向多校组织的信息科技统一测评。_避免_：县考、统考、区域考试。

**参考班级**：
被选中参加某场区域抽测的学校班级；同一场区域抽测中每所学校只能有一个参考班级。

**参考学生**：
属于区域抽测参考班级、需要完成该场抽测答题的学生。

**评卷教师**：
被分配区域抽测操作题答卷的教师；评卷时不应看到参考学生姓名、学校或班级。

**匿名评卷**：
评卷教师只看到待评答卷和评分标准，不看到参考学生身份信息的评卷方式。

**按题评卷配置**：
教研员或管理员按区域抽测中的操作题选择评卷教师，并可设置每位教师批改的答卷份数；支持 **一键均分** 预填份数；未设置份数时，由系统在关闭抽测生成任务时把该题剩余答卷自动均分。

**作答时长**：
区域抽测开启时配置的个人限时，默认 40 分钟；从参考学生首次进入区域抽测答题页开始计时，不因刷新、退出或重新登录而重置。

**个人作答截止时间**：
参考学生首次进入区域抽测后，由后端根据开始时间和作答时长生成的截止时间；超过该时间后学生端不再展示抽测内容。

**一次性打字**：
区域抽测中的打字题只能完成一次；提交或超时锁定后，不允许重新打字，也不展示速度、正确率、得分等反馈。

**成绩发布**：
教研员或管理员审核区域抽测成绩后，将该场抽测转为可用于分析和存档的最终状态。

**电子导学单**：
由教师、教研员或管理员设计并发布给指定学校班级，供学生断点填写和提交的结构化学习任务；界面可简称“导学单”。

**教研活动**：
面向教研员和教师的单一主题信息流，用于发布活动通知、交流活动内容并沉淀可检索的课程资源；界面不再使用旧论坛的板块分类。

**活动主题**：
教研活动中的顶层内容，承载一次活动或一项交流事项的说明、通知范围及其后续留言。例如“三年内新教师考核课活动”。

**活动通知**：
由教研员或管理员发布、可按学段或指定教师投递到教师首页通知栏的活动主题；点击通知直接进入对应活动主题。通知范围仅控制提醒对象，不限制教师查看主题。

**活动纪实**：
活动主题下用于上传签到表、课堂现场等照片并补充说明的留言；不强制填写课程年级、学期和课次信息。

**课程资源**：
活动主题下可独立搜索和下载的资源留言，由课程结构化信息、课后反思富文本以及课件文件或云盘链接共同组成；云盘链接可包含提取码和过期时间，也可标记为永久有效。

**导学单指派**：
一份电子导学单与一个或多个“学校 + 入学年份 + 班级编号”组合之间的发布关系。

**导学单答卷**：
一名学生对一份电子导学单形成的唯一填写记录，提交后可进入自动评分或人工处理。

**平台概览**：
教研员或管理员登录后的默认首页，展示平台简介、学校/教师/学生/题库/课程/区域抽测等核心数据，以及适合课题申报截图的可视化概览。

**系统诊断中心**：
面向线上故障定位的监控看板，聚合服务资源、缓存健康、慢 SQL、异常日志、慢接口、在线用户和后台任务状态，并提供可复制给 AI 的诊断报告。

**诊断时间窗**：
系统诊断中心默认只统计近 24 小时内的错误与慢接口；可扩展到近 7 天。_避免_把历史全库日志误当作当前风险。

**处置建议**：
诊断中心为每条异常、慢请求或资源风险附带的可读处理说明，用于区分业务校验失败与系统故障。

**作答人次**：
一名学生在某节课完成一次答题计 1 人次，按「课程 + 学生」维度统计；_避免_与答题明细行数或单题提交次数混淆。

**试卷汇总均分**：
先按学生对某节课所有题目得分求合计得到试卷总分，再对所有试卷总分求平均；_避免_与 **单题均分**（直接对所有答题明细行的 score 求平均）混淆。

**性能事件**：
系统诊断中心持久化的慢 SQL、慢接口、异常操作记录，默认保留 7 天，用于按时间定位卡顿与故障。

**缓存健康**：
对 Redis 运行状态的业务化表达；重点关注登录令牌、验证码、防重提交、限流和密码错误次数等平台基础能力是否正常，不直接暴露原始缓存列表给教研员。

**后台任务状态**：
对 Quartz 定时任务的只读展示，以中文说明平台业务任务用途与执行频率；教研员用于确认预览重试等后台任务是否正常运行，不提供新增、修改、删除或手动执行入口。

### 关系

- 一场 **区域抽测** 包含多个 **参考班级**。
- 一个 **参考班级** 包含多个 **参考学生**。
- 一名 **评卷教师** 可以评阅多个操作题答卷。
- 一条 **按题评卷配置** 绑定一场区域抽测、一道操作题和一名评卷教师。
- 一个 **参考学生** 在一场区域抽测中只有一个 **个人作答截止时间**。
- **成绩发布** 发生在所有操作题完成 **匿名评卷** 之后。
- 一份 **电子导学单** 可以包含多个 **导学单指派**。
- 一名学生对一份 **电子导学单** 最多形成一份 **导学单答卷**。
- 学生同时存在未完成的 **区域抽测** 和已发布的 **电子导学单** 时，必须先完成区域抽测。

### 示例对话

> **开发者**：“学生登录时同时有日常课程和区域抽测，先进入哪个？”
> **领域专家**：“先进入区域抽测；参考学生完成抽测或抽测关闭后，再恢复日常课程。”

> **开发者**：“同一学校不同年级都有 1 班，导学单只记录‘1班’可以吗？”
> **领域专家**：“不可以；导学单指派必须同时记录入学年份和班级编号。”

### 已解决歧义

- “抽测”“县考”“统考”“区域考试”统一称为 **区域抽测**；代码可沿用既有 CountyExam 命名，但界面和业务文案使用“区域抽测”。
- 区域抽测 v1 的“考试时间”统一称为 **作答时长**，不是全局自动开关时间窗。
- 学生端区域抽测不展示分数、对错、正确答案、解析、错题本或历史成绩；学生自己上传的操作题作品不提供下载入口。
- 区域抽测操作题在个人截止时间或考试关闭前允许覆盖上传，以最后一次上传作品为准。
- 评卷配置按操作题维度完成，关闭区域抽测后生成匿名评卷任务；匿名评卷继续不回避本校，但接口和前端都隐藏学生身份信息。
- “导学单”统一指 **电子导学单**；班级指派不得只使用班级编号，必须包含学校和入学年份。
- 区域抽测优先级高于电子导学单；区域抽测完成或关闭后，学生才能继续导学单和日常课程。
- 教研员端默认首页统一称为 **平台概览**，不再默认进入系统管理/用户管理。
- **平台概览** 作为教研员端一级首页菜单，不使用同名二级菜单。
- 面向排障的新页面称为 **系统诊断中心**；Druid 数据监控保留为高级 SQL 监控工具，不再承担业务化诊断入口职责。
- 教研员端隐藏岗位管理、参数设置、教师口径成绩查询、原始定时任务和缓存列表；保留可理解的 **缓存健康** 与只读 **后台任务状态**。

---

## 十、项目架构（从 main 回填）

基于 **RuoYi-Vue** 前后端分离架构进行深度定制开发。

- **后端**：Spring Boot, MyBatis-Plus, Spring Security, JWT
- **前端**：Vue 3, Element Plus, Vite, Pinia, **ECharts 5.6** (可视化图表)
- **数据库**：MySQL 8.0
- **文件预览**：**LibreOffice** (headless 模式，将 docx 转换为 PDF 预览)
- **核心模块**：`ruoyi-business` (业务逻辑), `RuoYi-Vue3` (前端交互)

---

> 补充：前端仅维护 RuoYi-Vue3；学年字段见 `entry_year`。

## 十一、发布口径与事故/热修记录（保留全文）

### 11.1 导学单与区域抽测发布口径（2026-07-21 已确认）

1. 全平台新学年口径以**每年 7 月 20 日**为切换日；本次至少保证区域抽测前后端与现有课程核心逻辑一致。
2. 课程创建者可以查看自己课程**实际指派班级**的导学单进度、答卷、上传与成绩；模板作者身份不产生课程成绩权限，管班教师仍需精确学校、入学年份和班级授权。
3. 区域抽测每名学生的实际试卷必须严格为 100 分。随机抽题按实际抽取数量计算；同一随机题型分值必须一致，后端在保存组卷和开启抽测时双重校验。
4. 抽测普通新增/编辑请求不能写状态、评卷开关、总分和开启/关闭/发布时间；状态只能经开启、关闭、发布专用接口转换。
5. 每校只允许一个真实有效行政班，学段、年级、入学年份必须匹配；生成 0 名有效考生时禁止开启。评卷教师必须是有效教师账号。
6. 发布、评卷开关和打分对抽测主记录加行锁；只有“已关闭且评卷开启”时才能改分或读取评卷材料，发布后立即失效。
7. 匿名操作题新上传路径的所有权目前缓存于 Redis 60 分钟。正常草稿会立即持久化路径；“上传成功但草稿保存失败，随后缓存过期/Redis 重启”的低概率场景可能要求学生重新上传。本次不新增持久化表，作为已知可用性风险保留。

### 11.2 2026-07-21 内网部署记录

- 目标服务：后端 3009、Vue3/Nginx 3010；80 端口站点未修改，切换前后页面内容哈希一致。
- 发布目录：`D:\program\3009dazipingtai\releases\20260721_192346_a88cdcd7`。旧 jar、旧前端目录和旧代码全部保留，但旧后端进程已停止，避免双写。
- 迁移前整库备份：`D:\program\3009dazipingtai\backups\20260721_192346_a88cdcd7\ry-vue-before.sql`，SHA-256 为 `9926b203e3c0c2b602941d39c3292ceefa0b56e049f5bae7dc7fc8b0d09bc642`。
- 正式库复核：13/13 张新增表、14/14 个关键字段、7/7 个关键索引、发布标记、4 个启用且禁止并发的 Quartz 任务均正确；当前课程重复组和学生答案重复组均为 0。
- 导学单菜单需在 v2 表结构之后执行 `sql/guide_sheet_menu_permissions.sql`。正式库已执行并复核：7 个权限点唯一，admin/teacher/researcher 各 7 个角色关联，未重新创建旧 `biz_guide_sheet_assignment` 表。
- 后端以 NSSM 服务 `NewDaziBackend3009` 运行，启动类型 Automatic；当前 Java 进程使用发布 jar 和发布目录外置配置，启动日志无致命异常，stderr 为空。
- Nginx 3010 已切换到发布前端，`/prod-api/` 与 `/ws/` 分别代理 HTTP API 和课堂 WebSocket。服务器曾残留两个同版本 Nginx master，导致 reload 未命中真实流量；多余 master 已优雅退出，PID 文件与唯一 3010 监听者已校正。
- 服务器验收：教师导学单管理与匿名评卷、教研员区域抽测、学生首页与导学单页面全部通过，无 page error 或 5xx；真实教师 Cookie + 精确 Origin 的 `/ws/classroom/...` 握手成功。

### 11.3 学年切换故障与修复状态（2026-07-21，已修复）

- **事故事实**：真实 2024 级课程被教师首页错误归入 2025 级，真实 2025 级课程未显示；错误路由参数触发成绩权限拒绝。正式库课程、学生、教师管班和成绩数据均存在，未发现数据删除。
- **根因**：教师首页用动态当前年级匹配静态课程 `grade`；成绩、设计器、画像等又混用 7 月 20 日、8 月 15 日和 9 月 1 日边界。
- **已确认方案**：新增稳定 `biz_lesson.entry_year`，证据回填且冲突即中止；课程归届、成绩可见集、设计器和自动推进统一使用该字段；全平台统一 7 月 20 日切换。`grade` 保留为课程创建时的开设年级，并在 v2.70 起与届别共同约束课次和推进边界。
- **实现状态**：代码提交 `1d0e7ef` 已 push 至 `codex/hotfix-academic-year-20260721`。课程创建/编辑、首页分组、成绩可见集、设计器、导学单班级权限与自动推进均显式使用稳定届别；动态学年统一经 7 月 20 日边界工具计算。
- **验证状态**：后端目标测试 27/27、业务模块 157/157、admin 2/2、clean package 通过；前端 Node 边界测试 3/3、生产构建通过。本机迁移与安全重跑通过，本地 Playwright/API 28/28 PASS；真实旧 `7d3eff1` JAR 已在隔离端口实测可在字段回滚为可空后新建/删除课程。
- **关键回滚约束**：正式字段为 `NOT NULL`，旧后端 INSERT 不写该字段，**绝不能只切回旧 JAR**。发布目录中的 `rollback_academic_year_hotfix.ps1` 固定执行：停新后端 → 校验并执行 `lesson_entry_year_anchor_rollback.sql` 把字段改回可空 → NSSM/Nginx 切回旧发布 → 启动并探活旧后端。脚本已在服务器 `-ValidateOnly` 通过，数据库与旧 JAR 联合能力已在本机真实演练。
- **接力文档**：事故证据、原阻塞和完整执行顺序仍保留在 `contexts/2026-07-21学年切换故障接力.md`，仅作审计记录，不再表示当前未发布状态。

### 11.4 学年稳定锚点热修发布记录（2026-07-21）

- 线上发布目录：`D:\program\3009dazipingtai\releases\20260721_212007_1d0e7ef`；旧发布 `20260721_192346_a88cdcd7` 完整保留。后端 NSSM `NewDaziBackend3009` 为 Automatic，重启后仍使用新 JAR；Nginx 3010 指向新前端，80 端口未修改。
- 新正式库全量备份：`D:\program\3009dazipingtai\backups\20260721_212007_1d0e7ef\ry-vue-before-entry-year.sql`，57,986,966 bytes，SHA-256 `fcc2bc97b376b1d3220e794000b4e544756beee2088c7c9d5aeb623dea52c06b`。同目录保留迁移前置检查、迁移、postflight、Nginx 配置备份和日志。
- 正式迁移前置检查：非法届别证据 0、多届冲突 0；7 门无指派/历史/答案证据课程已逐条核对。4 条为无学校归属的早期孤立/样例课程，另 3 条按静态年级和 2025 基线分别锚定 2024、2023、2023；这是唯一仍需长期留意的历史数据推断风险。
- 正式迁移结果：227/227 回填成功；来源为当前指派 47、学生答案 173、人工核对 fallback 7。最终 `entry_year varchar(4) NOT NULL`，空值 0、非法年份 0、多届冲突 0、锚点与业务证据不一致 0，`(dept_id, entry_year)` 索引存在；分布为 2025=47、2024=40、2023=55、2022=27、2021=26、2020=32。
- 线上只读 API + Playwright 共 42/42 PASS：教师首页 2024 级精确为课程 25/31/33/34/38/91/161/201/217/227/237/251 并显示九年级，2025 级精确为 263～266 并显示八年级；两届成绩课程均按 `entryYear` 锚定，summary/analysis/矩阵可读，跨届组合被拒绝；教师成绩/导学单/匿名评卷、教研员区域抽测、学生首页/导学单页面无 page error 或 5xx。
- 发布后重启与 postflight：3009/3010 均为 200，服务与 Nginx 仍使用新发布，启动/SQL/字段/排序规则严重日志匹配 0。线上验收为只读，未新建正式课程；新建课程写入稳定届别、编辑禁止跨届漂移已由本地 28/28 E2E 覆盖。

### 11.5 已毕业课可见 + 区域抽测评卷体验（2026-07-23 已上线）

- **根因（已毕业空课）**：教师首页 `getTeacherDashboardData` 在 `gradeId > 0` 才装课；已毕业 `calculateGradeInfo` 返回 `gradeId=-1`，导致「有 2020级(已毕业) 分组、课程卡片为空」。全员逻辑问题，非数据删除。
- **修复**：凡 `entry_year` 分组均按稳定届别查自建+共享课；评卷池提高搜索上限并汇总 `sys_user_dept` 多校名称；配置弹窗「一键均分」+ 关闭后生成任务说明；入口仍为待评任务数 > 0。
- **发布**：`D:\program\3009dazipingtai\releases\20260723_graduated_grading_r2`（当前）；上一版 `20260723_graduated_grading` 保留。Jar SHA-256 `cee51abd…c8ef9f`。
- **备份**：`D:\program\3009dazipingtai\backups\20260723_graduated_grading\ry-vue-before-graduated-grading.sql`，60,146,815 bytes，SHA-256 `f72b968f896194425913f136173666f3a1f40caa9fdcc9304230ffc41027d4af`；Nginx 配置备份同目录。
- **验收**：API+Playwright 冒烟通过；小学部 2020 已毕业 **13** 课；搜索「郑东旭」显示 **小学部/初中部双校**（同一 `user_id=19157727791`，非两个账号）；评卷配置弹窗含「一键均分」。
- **回滚**：NSSM/Nginx 切回 `20260723_graduated_grading` 或更早 `20260721_212007_1d0e7ef`；本轮无表结构迁移，一般 **无需** 回滚 SQL。
- **剩余说明**：教师首页「区域抽测评卷」仅在关闭抽测并生成待评任务后出现；若只保存配置未关考，属预期不显示。

### 11.6 教研活动正式发布记录（2026-07-28）

- **Git 基线**：功能提交 `9019cce`、规范提交 `1488206` 已 push 到 `origin/codex/research-activity-v1`；应用制品只以完整提交 `14882068287f349378b9ee52476bbe21c6c7e994` 构建，未新建分支、未创建 PR。
- **备份**：`D:\program\3009dazipingtai\backups\20260728_151230_1488206_research_activity`。`ry-vue.sql` 为 61,875,453 bytes，SHA-256 `F47C9C154099C131789D1129263623537EAEF8D7A711527B21BDD63771B484A3`；`sha256.txt` SHA-256 `D3C08828E1EC745E586CE8017B0EAE3BD6C963703C12720CC8BAD420D9B0108F`。同目录保存 NSSM 注册表和 Nginx/应用配置备份。
- **正式 SQL**：按前置检查执行 `research_activity_v1.sql`、`research_activity_activity_time.sql`、`research_activity_multischool_notice_backfill.sql`、`research_activity_image_url_fix.sql`。首次建表命令因未显式指定客户端 `utf8mb4`，在四张空表创建后、菜单写入前停止；确认未写入业务数据后使用 `--default-character-set=utf8mb4` 完整重跑并修复表/列注释。最终四表、15 个索引、可空 `activity_time`、8 个唯一权限点均正确；admin 8、researcher 8、teacher 5、student 0；重复接收人、重复菜单权限和旧图片 URL 余量均为 0。
- **Nginx/NSSM**：3010 的 `client_max_body_size` 已由 50m 精确改为 60m，80 站点仍为 50m；`nginx -t` 成功。NSSM `NewDaziBackend3009` 为 Running，AppDirectory 指向新 release 后端；Nginx 3010 唯一 root 指向新前端。3009/3010 切换后连续 3 次服务器内探活与服务器外探活均为 200；部署时间后后端 ERROR 为 0，Nginx 仅 1 条 reload notice，访问日志最近 3000 行 HTTP 500 为 0。
- **release**：`D:\program\3009dazipingtai\releases\20260728_151733_1488206`；JAR SHA-256 `52D561C4A04F583B060EDBB7727E96A4A60DEF2ACD72C017AFF8D2AED1D7EBED`。上一版 `20260723_graduated_grading_r2` 完整保留。NSSM 的 stdout/stderr 路径仍沿用上一 release 的日志目录，当前不影响运行，但后续统一发布脚本时宜改为固定共享日志目录，避免路径语义混淆。
- **服务器验收**：教研员发布通知→教师首页接收、活动纪实/课程资源/搜索/下载、学生接口拒绝与页面脚本检查 15/15；JPG/PNG/WebP、近 10 MiB、粘贴、保存/刷新/再次编辑、鉴权读取和缺图容错 15/15；多选按顺序上传、单张失败不阻断、拖拽缩放保存回显 8/8。测试主题、留言、孤儿通知/资源余量均为 0。课堂反思文案已随主流程和批量专项验证。
- **角色结论**：教研员、教师、学生完成真实服务器浏览器/API 验收；学生权限为 0 且访问被拒绝。管理员正式库菜单和 8 个权限点核验通过，但项目未提供管理员 Web 密码且无可复用登录态，未做管理员交互登录；禁止为补验收而猜密码、重置账号或伪造 Token。
- **回滚**：应用回滚时把 NSSM AppDirectory/AppParameters 恢复到 `D:\program\3009dazipingtai\releases\20260723_graduated_grading_r2\backend` 及其 JAR/配置，把 Nginx 3010 root 恢复为该 release 的 `frontend`，执行 `nginx -t`，再重启 NSSM 并 reload Nginx，最后复核 3009/3010。新教研活动四表保留、不执行 DROP；四个 SQL 无反向回滚，图片 URL 修复保持向前兼容。若必须恢复数据库，以本节备份为准，但会覆盖备份后产生的全库业务数据，必须另行确认。
- **下一步**：本轮部署任务已完成；除管理员交互登录这一明确缺口外无发布阻断。停止继续开发，等待用户指定下一个功能。

---

## 十二、成绩/指派/预览等历史工作快照（从 main 回填，状态已按 2026-07 代码校正）

> 下列内容来自 main 长文。其中「待验证 / 待评审」若与第二节或学年热修冲突，**以第二节与热修记录为准**。  
> 自动推进、答题去重、预览重试、导学单与抽测均已上线或已有 SQL 基线。

### 12.1 2026-05-27 更新摘要 (工作快照 - 成绩懒加载 / 分数口径收口 / 导出三列化 / 当前课程指派稳定)

> [!IMPORTANT]
> **状态口径**：本节记录本轮代码收口后的业务定义与验证边界。数据库迁移脚本已作为仓库交付物补充，但线上执行前仍需备份并确认目标库。

#### 📊 成绩口径
- **作业分**：学生在某节课所有答题记录的得分合计，不包含课堂表现分。
- **课堂表现分**：学生在某节课的课堂表现加减分，业务范围为 `-10` 到 `+10`，可为负数。
- **课程总分**：`clamp(作业分 + 课堂表现分, 0, 100)`；请假/缺考课程不参与课程总分与均分统计。
- **多课课堂表现平均**：选择多节课时，课堂表现分按非请假课程求平均，`0` 分是有效课堂表现，不能被过滤。
- **多课平均分**：按非请假课程的课程总分求平均，不再把多节课分数合计后封顶到 100；`作业平均 / 课堂表现平均 / 课程平均分` 默认保留 1 位小数。
- **成绩页展示语义**：成绩汇总页按当前筛选条件一次性展示全部学生，图表、排行、等级基于当前筛选结果全量数据；Excel 导出仍由服务端按当前筛选条件生成全量数据。
- **Excel 导出口径**：每节课拆为“作业分 / 课堂表现分 / 课程总分”三列；只选一课时汇总列为“作业分 / 课堂表现分 / 课程总分”，多课或全选时为“作业平均 / 课堂表现平均 / 课程平均分”。搜索关键词 `keyword` 会同步传给导出接口，异常班级号不再因强转整数导致导出 500。

#### 🧭 当前课程指派
- **当前课程**：同一 `dept_id + entry_year + class_code` 在同一时刻只能对应一条班级当前课程指派。
- **重复指派处理**：上线前使用 `sql/lesson_assignment_current_unique_fix.sql` 诊断并清理历史重复指派，保留最新 `assign_time / assignment_id` 的记录，再添加唯一约束。
- **课程课次**：新建课程的“第几课”由服务端按同学校、同教师、同届别、同课程开设年级的最大课次自动递增，前端只展示同口径建议；学期切换不重置，开设年级切换重新从第 1 课开始。
- **空指派课程**：课程可先创建和设计，班级指派允许为空；学生端只展示已指派到本班的当前课程。

#### 🧾 教师首页与操作题转换
- **教师首页成绩入口**：成绩班级弹窗显示每班“已出成绩人数 / 班级人数”，有答题记录或课堂表现记录且非请假即视为有成绩。
- **操作题上传与预览转换**：上传成功只表示学生作品文件已保存；在线预览依赖服务器异步转换。预览失败不等于上传失败，学生端应提示“作品已上传，预览暂不可用”，并提供下载原文件兜底。
- **定时重试**：`practicalPreviewRetryTask.retryFailedStudentAnswerPreviews` 应在 Quartz 中每小时执行，配套初始化脚本为 `sql/practical_preview_retry_quartz_job.sql`。

#### ✅ 本轮收尾验证
- **构建状态**：2026-05-27 已完成 `npm run build:prod`、`mvn -pl ruoyi-admin -am -DskipTests compile`、`mvn -pl ruoyi-admin -am -DskipTests clean package`。打包时曾因旧后端 jar 正在运行导致 `ruoyi-admin.jar` 被占用，已停止旧进程后重新打包，并用 `javap` 确认 fat jar 内嵌的 `ruoyi-business` 已包含 `exportScoreExcel(entryYear, classCode, lessonIds, keyword, response)` 新签名。
- **导出接口验证**：本地重启新后端后，教师账号 `19157727791` 切到小学部，请求 `/business/score/summary?entryYear=2020&classCode=1&pageNum=1&pageSize=3` 返回 200；请求 `/business/score/export?entryYear=2020&classCode=1` 与单课带 `keyword` 导出均成功生成 xlsx，表头已确认包含每课三列与正确的单课/多课汇总列。
- **角色冒烟**：教师、学生、教研员账号均可正常登录；教师 6 个菜单路由、学生首页、教研员 20 个菜单路由均通过页面冒烟，未发现白屏、404、接口 500、请求失败、控制台错误或控制台警告。教师首页“成绩”入口弹窗已显示“已出成绩人数 / 班级人数”统计，例如 `42/42有成绩`。
- **本地定时任务**：本地 `xueyeceping.sys_job` 已存在并启用 `practicalPreviewRetryTask.retryFailedStudentAnswerPreviews`，表达式为每小时执行一次。
- **前端警告收口**：成绩页残留的旧课堂表现弹窗引用已移除；教师/学生/教研员会触达的业务页面已改用 Element Plus 当前 `value` 单选值写法，避免旧 `label` 写法产生运行警告。

#### ⏳ 历史待评审（多数已完成）
- **自动推进下一课**：`50%` 学生有成绩后延迟 `2` 小时自动切到下一课仍为独立待评审功能。本轮只修当前手动指派稳定性，不实现自动推进。

### 12.2 操作题预览 / 答题唯一 / 画像（2026-04-22 摘要，已落地）

- 学生答案 `previewStatus` / `previewPath` / 重试字段；`PracticalPreviewRetryTask`；教师批改页重转。  
- 答题按 `student_id+lesson_id+question_id` 取最新；提交改为存在则更新。  
- 学生画像按 **学年** 维度。  
- SQL：`practical_preview_retry_fields.sql`、`typing_answer_dedup_fix.sql`、相关 Quartz 脚本。

### 12.3 2026-03-12 更新摘要 (v2.9 - 请假/缺考管理与视觉优化)

#### 📝 请假/缺考 (Absent/Leave) 深度集成
- **核心逻辑**：在 `biz_classroom_performance` 表新增 `is_absent` 字段。标记请假后，学生该课总分记为 `NULL`（而非 0）。
- **均分计算修正**：`score/index.vue` 逻辑重构，平均分分母使用 `validScoreCount`（自动排除所有请假记录），确保均分真实反映在校生水平。
- **交互入口**：
  - **汇总表快捷操作**：只选中单门课程时，表格新增“请假”图标列（小日历），支持一键标记/恢复。
  - **状态说明**：标记请假后的成绩 Tag 显示为灰色“请假”字样。
- **后端支持**：`ScoreQueryController` 新增 `setAbsent` 接口，同步更新 `BizStudentAnswerMapper` 关联查询逻辑。

#### 🎨 视觉体验与图表清晰度优化
- **字体规范统一**：全站图表禁用 `bold` 加粗（解决小字模糊），全局应用字体系列：`"Microsoft YaHei", "PingFang SC", "Helvetica Neue", Arial, sans-serif`。
- **异常状态高亮**：
  - **RankChart 姓名变色**：有备注的学生姓名在 X 轴自动显示为**橙色** (`#E6A23C`)。
  - **零分灰色占位**：分数为 0 且有备注（跳级/休学/请假）的学生，柱状图柱体强制变灰 (`#C0C4CC`)。
- **性能/响应式提升**：修正 `RankChart` 全屏状态下的像素级布局计算，支持旋转 45 度的长姓名标签不被截断。

#### ⚡ 效率提升（自动保存）
- **课堂表现分**：实现“失去焦点即保存”逻辑，移除手动保存按钮，提升实时打分体验。
- **字段宽度调整**：微调列表布局，增加特定字段的可视范围级。

### 12.4 2026-03-12 更新摘要 (v2.8 - 跨校数据隔离修复)

#### 🔐 多学校数据串台 Bug 修复

- **问题现象**：殷夫中学老师能看到大目湾实验学校创建的课程；大目湾的课堂表现分列表混入了殷夫中学的学生（多个"张三"）；理论测试详情页出现跨校学生重复。
- **根因分析**：系统最初为单学校设计，核心业务表（`biz_lesson`, `biz_lesson_assignment`, `biz_classroom_performance`）缺少 `dept_id`（学校ID）字段。当多所学校的班级编号相同（如都有"2024级8班"）时，SQL 查询仅按 `entry_year + class_code` 关联，导致跨校数据混合。更严重的是，`deleteOtherAssignmentsByClass` 方法会**跨校删除课程指派记录**。
- **修复方案**：

| 修复层面 | 具体内容 |
|:---|:---|
| **数据库** | 为 `biz_lesson`、`biz_lesson_assignment`、`biz_classroom_performance` 三张表追加 `dept_id` 字段，并通过 SQL 回填历史数据 |
| **后端代码** | 12 个文件中所有涉及跨表关联 `entry_year + class_code` 的查询，全部追加 `dept_id` 过滤条件 |

- **修改的后端文件清单**：

| 文件 | 修改内容 |
|:---|:---|
| `BizLessonMapper.xml` | `selectLessonsByGradeAndCreator` 加 `AND dept_id = #{deptId}`；共享课程查询 JOIN 加 `AND la.dept_id = tc.dept_id` |
| `BizLessonMapper.java` | `selectLessonsByGradeAndCreator` 方法签名加 `deptId` 参数 |
| `BizLessonServiceImpl.java` | 教师首页调用传入 `deptId`；`saveLessonDetails` 中 `deleteOtherAssignmentsByClass` 传入 `deptId` |
| `BizLessonAssignmentMapper.xml` | `deleteOtherAssignmentsByClass`、`selectCurrentLessonByClass` 加 `dept_id` 过滤 |
| `BizLessonAssignmentMapper.java` | 对应方法签名加 `deptId` 参数 |
| `BizClassroomPerformanceMapper.xml` | `selectListByLessonAndClass` 加 `AND u.dept_id = #{deptId}` |
| `BizClassroomPerformanceMapper.java` | 对应方法签名加 `deptId` 参数 |
| `ClassroomPerformanceController.java` | `list` 方法传入 `deptId` |
| `ScoreQueryController.java` | 课程下拉、答题分析、答题矩阵均传入 `deptId` |
| `StudentHomeController.java` | `getCurrentLesson` 传入 `deptId` |
| `BizStudentAnswerMapper.xml` | `selectByLessonAndClass`、`selectStudentAnswerMatrix`、`selectPracticalSubmissions` 加 `u.dept_id` 过滤 |
| `BizStudentAnswerMapper.java` | 对应方法签名加 `deptId` 参数 |
| `TeacherGradingController.java` | `getPracticalSubmissions` 传入 `deptId` |
| `FileConversionUtils.java` | `stopOfficeManager` 优化：关闭前先 `taskkill` 残留进程，消除重启超时 |

> [!CAUTION]
> **关键注意**：`biz_student` 表本身**没有** `dept_id` 字段！学生的学校归属必须通过 `LEFT JOIN sys_user u ON s.user_id = u.user_id` 后使用 `u.dept_id` 获取，直接写 `s.dept_id` 会报 `Unknown column` 错误。

## 十三、角色与核心流程（从 main 回填）

### 13.1 角色定义

- **管理员 (Admin)**: 拥有系统完整权限，负责基础数据维护（学校、部门、用户）。
- **教师 (Teacher)**: 核心业务操作者，负责课程创建、指派、批改、学情分析。
- **学生 (Student)**: 终端用户，进行打字练习、答题、上传作品、查看个人成绩。

### 13.2 核心业务流程

1.  **课程创建流程**: 教师创建课程 → 添加/导入题目 (选择/判断/打字/操作) → 配置题目顺序与分值 → 保存课程。
2.  **发布指派流程**: 教师选择课程 → 选择指派班级 (关联 `biz_teacher_class` 权限) → 学生端首页自动显示该课程。
3.  **答题与评分流程**:
    - **理论题**: 学生提交 → 系统自动比对答案 → 实时判分。
    - **打字题**: 实时监控输入 → 计算 WPM/正确率/完成率 → 结合基准速度公式 → 自动判分。
    - **操作题**: 学生上传文件 → 系统转 PDF 预览 → 教师后台查看并根据评分项打分 → 系统自动折算最终分。
4.  **成绩分析流程**: 记录所有答题数据 → 聚合计算班级/课程平均分 → 生成图表 (排名/分布) → 教师查阅/导出。

### 13.3 功能模块详解

#### 🟢 教学管理端

| 模块         | 功能点     | 详细描述                                                                                          | 关键交互/接口                                           |
| :----------- | :--------- | :------------------------------------------------------------------------------------------------ | :------------------------------------------------------ |
| **课程设计** | 课程管理   | 创建/编辑课程，支持**随机出题模式** (固定/乱序/抽题) 配置，拖拽排序。                             | `designer.vue`, `BizLessonController`                   |
| **题目库**   | 试题维护   | 维护四类题型，支持 **Word/Excel 批量导入**，富文本题干编辑。                                      | `question/index.vue`, `ImportController`                |
| **班级指派** | 教学安排   | 灵活将课程指派给多个行政班级，支持按年级快速筛选。                                                | `BizLessonAssignmentController`                         |
| **作业批改** | 操作题评分 | **在线预览 PDF** (无需下载)，支持**分项打分** (如: 创新性 40%，完整性 60%)，自动计算总分。        | `grading.vue`, `PdfPreview`                             |
| **学情分析** | 数据看板   | **ECharts 可视化**：班级均分、**题目答题分析** (易错题/选项分布)、不及格名单、进退步分析。        | `score/index.vue`, `ScoreQueryController`               |
| **学生画像** | 个人分析   | **v2.6 新增**：查看单个学生的历次成绩、打字速度、课堂表现、班级排名变化，支持班级筛选、跳转入口。 | `student-profile/index.vue`, `StudentProfileController` |

#### 🔵 学生学习端

| 模块           | 功能点   | 详细描述                                                                          | 关键交互/接口                      |
| :------------- | :------- | :-------------------------------------------------------------------------------- | :--------------------------------- |
| **智能工作台** | 任务驱动 | 首页根据时间轴展示最新作业，状态区分：未开始/进行中/已完成/待批改。               | `StudentHomeController`            |
| **打字测评**   | 实时反馈 | 沉浸式打字界面，**防作弊** (禁粘贴)，实时显示速度/进度，练习结束后生成详细报告。  | `Typer.vue`, `typing-utils.js`     |
| **作品提交**   | 文件处理 | 支持大文件分片上传，**自动格式转换** (Docx -> PDF) 以供预览，支持多版本覆盖提交。 | `FileUpload`, `LibreOfficeService` |
| **错题本**     | 巩固提升 | 自动收集历史错题，支持筛选课程回顾，查看正确答案与解析。                          | `WrongQuestionDialog.vue`          |

### 13.4 学生端考试体验优化 (v2.6.1 - 2026-01-29)

本次更新针对 `student/index.vue` 进行了多项 UX 优化：

#### 🔒 顶部导航栏固定

- **实现**：`.dashboard-header` 设置 `position: sticky; top: 0; z-index: 2000;`
- **效果**：学生滚动答题时导航栏始终可见，方便快速访问历史成绩、错题本等功能

#### ✅ 理论测试答题反馈

- **需求演变**：从"选项高亮正确答案"调整为"仅在卡片右上角显示对错标记"
- **实现细节**：
  - 提交后每道题卡片右上角显示 `正确` (绿色✓) 或 `错误` (红色✗) 标记
  - 使用 Element Plus 的 `<el-icon>` 组件 (`Check`, `Close`)
  - 样式类：`.result-tag.correct` / `.result-tag.wrong`
- **判断题选中高亮**：
  - 提交后判断题的选项变为 `disabled`，但选中项保持蓝色高亮
  - 使用 `:deep()` 穿透 Element Plus 默认的禁用灰色样式
  - 选择器：`.audit-group :deep(.el-radio.is-disabled.is-checked .el-radio__inner)` + `!important`

#### ⌨️ 打字题防作弊与对齐

- **防复制/拖拽**：
  - 原文容器 `.text-content` 添加 `user-select: none; pointer-events: none;`
  - 同时绑定 `@copy.prevent @paste.prevent @cut.prevent @dragstart.prevent @contextmenu.prevent`
- **输入框与原文严格对齐**：
  - **统一字体**：`font-family: Consolas, "Courier New", monospace, "Microsoft YaHei";`
  - **统一排版**：`font-size: 18px; line-height: 2; letter-spacing: 1px;`
  - **统一换行**：`word-break: break-all; white-space: pre-wrap;`
  - **关键修复**：`.input-box` 添加 `padding: 0 17px;` 补偿 `.original-text-box` 的 `padding(16px) + border(1px)` 宽度差异，确保每行字数完全一致
  - **盒模型**：两者均使用 `box-sizing: border-box;`

---

## 十四、数据库设计（从 main 回填 + 增量说明）

### 3.1 核心业务表结构

#### 1. `biz_lesson` (课程/作业主表)

_定义了一次教学活动或作业的基本属性_
| 字段名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| `lesson_id` | `bigint` | **PK** | 课程主键 ID |
| `lesson_title` | `varchar` | Yes | 课程标题 (如: "三年级上册期末考核") |
| `grade` | `int` | Yes | 适用年级 (1-9, 对应小学至初中) |
| `semester` | `char(1)` | Yes | 学期 (0:上册, 1:下册) |
| `lesson_num` | `int` | Yes | 课次序号 (用于排序, 如: 第 1 课) |
| `creator_id` | `bigint` | Yes | 创建教师 ID (数据权限控制) |
| `shuffle_mode` | `int` | - | 出题模式 (0:固定 1:随机排序 2:随机抽题) (**v2.5 新增**) |
| `random_choice_count` | `int` | - | 随机抽取选择题数 (模式 2 有效) (**v2.5 新增**) |
| `random_judgment_count` | `int` | - | 随机抽取判断题数 (模式 2 有效) (**v2.5 新增**) |
| `dept_id` | `bigint` | **FK** | 所属学校 ID (**v2.8 新增，多校隔离**) |
| `create_time` | `datetime` | - | 创建时间 |

#### 2. `biz_question` (统一题库表)

_存储所有类型的题目元数据_
| 字段名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| `question_id` | `bigint` | **PK** | 题目主键 ID |
| `question_type` | `varchar` | Yes | 题型: `choice`(选择), `judgment`(判断), `typing`(打字), `practical`(操作) |
| `question_content`| `text` | Yes | 题干内容 (支持 HTML 富文本) |
| `option_a`~`d` | `varchar` | - | 选择题选项内容 |
| `answer` | `text` | Yes | 标准答案 (选择:A/B, 判断:T/F, 打字:全文) |
| `analysis` | `text` | - | 题目解析 |
| `file_path` | `varchar` | - | 操作题素材文件路径 |
| `preview_path` | `varchar` | - | 操作题素材预览路径 (PDF) |
| `typing_duration` | `int` | - | 打字限时 (**分钟**) |
| `is_public` | `char(1)` | - | 是否公共题目 (Y/N) |

#### 3. `biz_lesson_question` (课程-题目关联表)

_实现课程与题目的多对多关系，并定义题目在课程中的特定属性_
| 字段名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| `id` | `bigint` | **PK** | 关联主键 |
| `lesson_id` | `bigint` | **FK** | 课程 ID |
| `question_id` | `bigint` | **FK** | 题目 ID |
| `question_score`| `int` | Yes | **本题分值** (同一题目在不同课程可分值不同) |
| `order_num` | `int` | Yes | 题目在课程中的排序号 |

#### 4. `biz_lesson_assignment` (班级指派表)

_控制哪些班级的学生可以看到并进行该课程_
| 字段名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| `assignment_id` | `bigint` | **PK** | 指派记录 ID |
| `lesson_id` | `bigint` | **FK** | 课程 ID |
| `entry_year` | `varchar` | Yes | 入学年份 (如 "2024") |
| `class_code` | `varchar` | Yes | 班级编号 (如 "01", "02") |
| `dept_id` | `bigint` | **FK** | 所属学校 ID (**v2.8 新增，多校隔离**) |
| _当前课唯一约束_ | - | - | `uk_lesson_assignment_current_class` (`dept_id`, `entry_year`, `class_code`) |

#### 5. `biz_student_answer` (答题记录表)

_存储学生的每一次答题详情与评分结果，是学情分析的数据源_
| 字段名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| `answer_id` | `bigint` | **PK** | 记录 ID |
| `student_id` | `bigint` | **FK** | 学生 ID |
| `lesson_id` | `bigint` | **FK** | 课程 ID |
| `question_id` | `bigint` | **FK** | 题目 ID |
| `student_answer`| `text` | - | 学生提交内容 (文本或文件路径) |
| `is_correct` | `tinyint`| - | 自动判分结果 (0:错 1:对) |
| `score` | `int` | - | 获得分数 (未批改则为 null) |
| `answer_time` | `int` | - | 答题耗时 (秒) |
| **`typing_speed`**| `int` | - | **打字速度 (WPM/字每分)** |
| **`accuracy_rate`**| `decimal`| - | **打字正确率 (%)** |
| **`completion_rate`**| `decimal`| - | **打字完成率 (%)** |

#### 6. `biz_scoring_item` (操作题评分项表)

_定义操作题的细分评分维度 (v2.4 新增)_
| 字段名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| `item_id` | `bigint` | **PK** | 评分项 ID |
| `question_id` | `bigint` | **FK** | 关联题目 ID |
| `item_name` | `varchar` | Yes | 评分维度名称 (如 "颜色搭配") |
| `item_score` | `int` | Yes | **权重比例** (合计应为 100) |

#### 7. `biz_scoring_detail` (操作题分项得分表)

_记录教师对每个评分项的具体打分 (v2.4 新增)_
| 字段名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| `detail_id` | `bigint` | **PK** | 详情 ID |
| `answer_id` | `bigint` | **FK** | 关联答题记录 |
| `item_id` | `bigint` | **FK** | 关联评分项 |
| `score` | `int` | Yes | 实际得分 (原始分, 需按权重折算) |

#### 8. `biz_teacher_class` (教师班级权限表)

_定义教师可以管理哪些班级 (v2.4 新增)_
| 字段名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| `id` | `bigint` | **PK** | 主键 |
| `user_id` | `bigint` | **FK** | 教师用户 ID |
| `dept_id` | `bigint` | **FK** | 学校 ID |
| `entry_year` | `varchar` | Yes | 入学年份 |
| `class_code` | `varchar` | Yes | 班级编号 |

#### 9. `biz_student` (学生信息表)

_存储学生的扩展信息，关联 sys_user_
| 字段名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| `student_id` | `bigint` | **PK** | 学生主键 ID |
| `user_id` | `bigint` | **FK** | 关联 sys_user.user_id |
| `entry_year` | `varchar` | Yes | 入学年份 (如 "2024") |
| `class_code` | `varchar` | Yes | 班级编号 (如 "01") |
| `student_name` | `varchar` | - | 学生姓名 (冗余字段，主要从 sys_user.nick_name 获取) |
| `remark` | `varchar` | - | 备注信息 (用于学生画像显示) |

#### 10. `biz_classroom_performance` (课堂表现分记录表)

_记录学生每节课的课堂表现加减分 (v2.6 新增)_
| 字段名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| `id` | `bigint` | **PK** | 记录 ID |
| `student_id` | `bigint` | **FK** | 学生 ID |
| `lesson_id` | `bigint` | **FK** | 课程 ID |
| `score` | `int` | Yes | 表现分 (**支持正负值**，如 +5 或 -3) |
| `dept_id` | `bigint` | **FK** | 所属学校 ID (**v2.8 新增，多校隔离**) |
| `create_time` | `datetime` | - | 记录时间 |

> **注意**：`score` 字段支持负数，用于表示扣分项；`0` 分也是有效课堂表现。成绩均分应排除请假/缺考记录，而不是按 `score != 0` 过滤。

### 3.2 系统管理表 (System Management Tables)

#### 1. `sys_user` (用户信息表)

_存储系统用户信息，包括管理员、教师和学生_
| 字段名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| `user_id` | `bigint` | **PK** | 用户主键 ID |
| `dept_id` | `bigint` | **FK** | 部门/学校 ID |
| `user_name` | `varchar` | Yes | 用户账号 (唯一) |
| `nick_name` | `varchar` | Yes | 用户昵称 |
| `user_type` | `varchar` | - | 用户类型 (00:系统用户) |
| `email` | `varchar` | - | 邮箱 |
| `phonenumber`| `varchar` | - | 手机号码 |
| `sex` | `char(1)` | - | 性别 (0 男 1 女 2 未知) |
| `password` | `varchar` | Yes | 密码 (BCrypt 加密) |
| `status` | `char(1)` | - | 状态 (0 正常 1 停用) |
| `del_flag` | `char(1)` | - | 删除标志 (0 存在 2 删除) |
| `login_ip` | `varchar` | - | 最后登录 IP |
| `login_date` | `datetime`| - | 最后登录时间 |

> **注意**: 实体类 `SysUser.java` 中的 `schoolId` 字段目前在数据库中未通过物理列存储，而是通过 `dept_id` 关联或业务逻辑处理。

#### 2. `sys_dept` (部门/学校表)

_存储组织架构，包括地区教育局、学校及校内部门_
| 字段名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| `dept_id` | `bigint` | **PK** | 部门主键 ID |
| `parent_id` | `bigint` | Yes | 父节点 ID (0 为根) |
| `ancestors` | `varchar` | Yes | 祖级列表 (如 "0,100,101") |
| `dept_name` | `varchar` | Yes | 部门/学校名称 |
| `order_num` | `int` | - | 显示顺序 |
| `leader` | `varchar` | - | 负责人 |
| `school_code` | `varchar` | - | 学校官方代码 (**新增**) |
| `school_type` | `char(1)` | - | 学校类型 (1 小学 2 初中 3 高中) (**新增**) |

---

### 14.x 2026-07 增量（main 原文未列全时以此为准）

| 对象 | 说明 |
| :--- | :--- |
| `biz_lesson.entry_year` | `varchar(4) NOT NULL`，课程所属入学届；索引 `(dept_id, entry_year)` |
| 推进策略/历史表 | 见 `sql/lesson_auto_advance.sql` |
| 区域抽测表族 | 见 `sql/county_exam_*.sql` |
| 导学单 v2 表族 | 见 `sql/guide_sheet_*.sql`、`release_20260721_server_upgrade.sql` |
| 菜单 25010 | 系统诊断中心；线上曾缺失，用 `researcher_monitor_menu_fix.sql` 补 |
| `sys_perf_event` | 诊断中心慢 SQL/慢接口/异常事件表；`sql/sys_perf_event.sql`；2026-07-22 正式库补齐 |
| Quartz 清理任务 | `sysPerfEventCleanupTask.cleanupExpiredPerfEvents`（`sql/sys_perf_event_cleanup_job.sql`） |

## 十五、业务逻辑规范（从 main 回填）

### 4.1 打字题评分公式 (v4.1)

采用简化公式，评分以原文字数为准（不依赖时长）：

```
速度系数 = min(正确字数 / 原文字数, 1.0)
得分 = 满分 × 速度系数 × 正确率
```

- **基准速度**：小学（1-6年级）20 字/分，初中及以上 40 字/分。
- **打字时长**：根据字数自动计算（字数÷基准速度），仅作为答题时间限制，不影响评分。
- **正确率**：正确字数 / 完成字数。

### 4.2 自动判分规则

- **选择题**：忽略大小写比较，全对得满分。
- **判断题**：支持中文答案（对/错/正确/错误）自动转换为 T/F 后比较。
- **操作题**：教师手动批改，支持**分项评分**（如界面设计、功能实现等），学生端显示"待批阅"状态直到教师评分。

### 4.4 分项评分逻辑 (P6)

- **配置入口**：题库管理 → 新增/编辑操作题 → 评分项配置（比例分配）
- **比例要求**：所有评分项的比例值合计必须为 **100**，否则无法保存
- **折算公式**：`实际分值 = 比例值 × (课程设定总分 / 100)`
  - 例：题库设置「界面 40、功能 60」→ 课程设置该题 50 分 → 批改时显示「界面 20 分、功能 30 分」
- **批改界面**：自动加载评分项，逐项打分后自动求和

### 4.5 随机出题机制 (v2.5)

- **配置模式**：
  - **固定顺序** (Default)：按题目添加顺序展示。
  - **随机排序**：仅针对**选择题**和**判断题**进行全量乱序，打字题/操作题固定在顶部。
  - **随机抽题**：从课程关联的所有题目中随机抽取 N 道选择题 + M 道判断题。
- **一致性保证**：学生端的随机基于 `Student_ID + Lesson_ID` 作为种子，保证同一学生多次进入或刷新页面看到的题目顺序/内容**完全一致**，但不同学生查看到的题目不同。

### 4.3 防作弊机制

- **打字题禁止复制粘贴**：`@paste.prevent`, `@copy.prevent`, `@cut.prevent`, `@contextmenu.prevent`
- 尝试粘贴时显示警告：`ElMessage.warning('打字练习禁止使用粘贴功能，请手动输入')`

---

## 十六、技术实现细节（从 main 回填）

### 5.1 文件预览 (LibreOffice)

- **依赖**：服务器需安装 LibreOffice（默认路径 `C:\Program Files\LibreOffice\program\soffice.exe`）
- **转换命令**：`soffice --headless --convert-to pdf --outdir "输出目录" "源文件"`
- **工具类**：`FileConversionUtils.convertDocxToPdfWithLibreOffice(docxPath, outputDir)`
- **前端预览**：使用 `PdfPreview` 组件（`@/components/PdfPreview/index.vue`）以 iframe 方式显示 PDF

### 5.2 操作题流程

1. 教师创建操作题时上传 `.docx` 素材 → 后端调用 LibreOffice 生成 `preview_path`（PDF）
2. 学生下载素材 → 修改后上传作品 → **提示作品已上传、等待服务器转换** → 后端异步转换 → 自动保存到 `biz_student_answer.student_answer`
3. 学生预览作品：调用 `/common/resource/view?resource=xxx` 接口（通过后端读取文件流，解决特殊字符文件名问题）
4. 右上角状态：未提交显示总分，已提交未批阅显示"待批阅"，已批阅显示"得分/总分"

### 5.3 理论测试提交规则

- **只能提交一次**：`theorySubmitted` 状态在页面加载时从 `submittedAnswers` 恢复，刷新后依然禁止重复提交
- 提交后回显已答答案和得分

### 5.4 请求超时配置

- 前端 Axios 超时设置为 **60 秒**（`request.js: timeout: 60000`），以支持 LibreOffice 转换等耗时操作

### 5.5 Loading 等待规范

- **操作题上传**：上传成功后显示 `uploadingQuestionId` loading，等待服务器异步转换预览；转换失败只代表在线预览不可用，不代表作品上传失败
- 所有涉及 LibreOffice 转换的操作都需要等待，前端必须显示 loading 提示

---

## 十七、否决项摘要（机位锁等）

- **学生机位锁 / 固定座位 / 本地助手方案**：曾在 main 文档中详细冻结，**产品结论为不实现**（成本与运维边界过高）。细节不必再展开编码；若重开需求须新开方案确认。  
- 其它否决：卡片密度大改、批改入口大改等。

---

*文档合并说明：v2.35 自 `main` 回填第 九～十六 节主体；第 〇～二、十一 节为 2026-07 热修与 2026-07-22 线上排障新事实。*
