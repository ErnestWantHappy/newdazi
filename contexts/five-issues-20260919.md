# 2026-09-19 五项课堂问题：本机修复与证据

用户已确认定位方案后实施。本轮未部署正式环境、未修改真实成绩，保留工作区已有学情等改动。SSH只允许本机先连接123，再由123连接其他服务器；两次尝试均在123 TCP连接阶段超时，未转连130。

## 定位与实现

1. 学生弹窗：源码和此前下载的正式JAR没有精确文案“登录状态保存失败”，本次现场失败接口仍未知。可确认的共用路径是业务401或GET `/auth/session-status` 心跳401进入模态框，取消后后续请求再次触发；异常文字匹配还会误判。现按结构化401识别，独立会话复核合并并发并限频，同一令牌取消后不再重复弹，成功心跳恢复提示资格。暂时不可用以限频非模态通知提示，保留当前作答，不重放写请求。后端无效JWT仍未登录；Redis读取/续期DataAccessException返回HTTP503且不进入业务处理。尚不能认定本次现场是Redis故障。
2. 批改：整课全题型有答题行人数、整课操作题批改份数、当前题非空提交人数原先混用标签。现明确三种口径，当前题分母取同一名单，名单排除停用/删除用户。未交学生仍可上下位导航，但不能评分；期限失败单独显示重试，全屏亦可见；静默名单失败保留旧名单。后端原本就拒绝空studentAnswer，已移除白卷可打0的错误提示。未获得现场37/13所属课程班级，不能证明这两个具体数字都正确。
3. 流程图：前后端白名单都曾丢弃连线几何，LogicFlow缺少pointsList时重新路由。现保留端点、2至100个折点、锚点及标签位置；schemaVersion仍为1.0，可选扩展。AI图片按保存路径绘制，节点x/y按中心，画布平移缩放保护负坐标和外伸折线。历史已丢失的折点不可恢复，不重判旧成绩。
4. 题库：真实选择题完整字段已返回，但题干列约78px、ellipsis截断且没有只读详情。现增加需原query权限的详情弹层，完整题干/选项/答案/解析，纯文本插值，题干列最小260px。
5. 打字统计：git提交05a833b（2026-07-31）删除旧校际图表；`teaching_supervision_v1.sql`又将菜单2046的旧stats权限替换为监管view权限。本机教研员getInfo实证旧权限缺失。恢复原页面打字统计页签和旧接口，增加幂等权限恢复SQL，只授予已有监管入口的有效researcher角色。保留逐条typing_speed>0作答记录平均口径，不改为逐学生平均；筛选沿用旧接口9月到次年8月学年。旧SQL末日时间边界及其与平台7月20日学年规则的差异不在本轮改动范围。

## 文件范围

- 登录：Vue3 `utils/session.js`、`utils/request.js`；framework `TokenService.java`、`JwtAuthenticationTokenFilter.java`。
- 流程图：Vue3 `components/FlowchartEditor/schema.js`；business `FlowchartDocumentService.java`、`FlowchartImageRenderer.java`。
- 批改：Vue3 `views/business/teacher/grading.vue`、business `BizStudentAnswerMapper.xml`。
- 题库：Vue3 `views/business/question/index.vue`。
- 教研员：Vue3 `api/business/schoolScore.js`、`views/business/schoolScore/index.vue`、新增`TypingStatistics.vue`；`sql/restore_typing_statistics_permission_20260919.sql`。
- 回归：新增session/schema前端测试、FlowchartImageRenderer/TokenServiceFailure后端测试，补充FlowchartDocumentService和JwtAuthenticationTokenFilter既有测试。相关上下文增量维护。

## 验证

- Java定向24项（认证、流程图）及17项（批改期限、答案Mapper契约），共41项全部通过；前端Node测试7项通过。
- Vue3生产构建通过。原目录clean package因运行中JAR被占用在admin模块失败；未停止该进程。复制后端源码到忽略的独立构建目录后，clean package及24项测试成功。该构建含工作区既有未发布改动，不能直接当作生产制品。
- 真实教师选择题：筛选304道，所开69字题干及全部选项与详情接口一致，桌面/390px手机弹层可读，无脚本异常。
- 真实教研员：恢复权限并重新登录后页签可见；2025-2026第二学期13所学校均有速度，所选学校6个班级，图表非空，桌面/手机截图，无脚本异常。当前学期没有速度时显示“暂无记录”，不补零。
- 批改浏览器使用拦截模拟：首个期限503禁用提交，重读恢复；提交下一位、未交/空作品导航、末位禁用、静默名单失败保留均通过。真实成绩写入0。
- 实际LogicFlow组件：6折点、起止点、锚点、标签保存后回显完全一致，桌面/手机图形存在；Java像素测试验证折线和负坐标缩放。
- 会话浏览器使用拦截模拟：503无模态，复核401仅一次提示，连续三次再次调用无重复弹窗，作答和令牌保留。不是正式故障复现。
- 证据目录：`output/five-issues-20260919/`内`ui-verification.json`、`grading-verification.json`、`flow-verification.json`、`session-verification.json`、构建日志及截图。定位原稿为`report.md`。

## SQL、运行与回滚

本机库`xueyeceping_server_20260729`只执行权限恢复SQL，连续执行两次结果一致：新增按钮权限26028，授予researcher，无其他角色新增授权。菜单与授权表备份29335字节，SHA-256 `0237CA2242401A2700955511923A3EC30466B76AAF2C2E2DD3D98FB93CC19A94`，位于项目外`backups/20260919_203827_before_typing_permission/menu_permissions.sql`；详情见`permission-result.json`。未修改题目、答案、成绩或业务表结构。

正式仍为前端1.30.15、后端1.30.14，本轮未发布。新前后端需配套，流程图后端未更新会继续丢几何；后端Mapper/认证变更需重启才生效。本机80前端可见新页面，本轮没有重启8080既有进程，不能将页面冒烟当作新后端端到端验收。

发布前必须重新读取正式基线、隔离既有未发布学情代码、备份菜单授权表并前检权限SQL，执行后重新登录刷新权限。回退应用要前后端配套；权限回退仅删除本次记录的新增researcher授权及新增按钮（先确认无后续其他授权），保留2046监管菜单；不得整库覆盖成绩，也不删除流程图已保存的新字段。

## 2026-09-19 正式发布补充

用户随后明确授权五项修复与学情V3.3.1一起全量部署。130现为1.30.16/20260919_full_insight_fixes_v1；前后端、权限SQL已生效，取代上文“未发布/隔离学情”阶段。完整Java591项（1跳过）/前端73项测试和构建通过，正式教师题库详情、批改口径及导航、教研员13校均速/6班明细、学生登录首页、课堂三次轮询通过。流程图几何专项仍为本机实际LogicFlow与Java像素证据，没有改写正式学生作品做保存验收。备份SHA和回滚详见PROJECT_CORE v3.80、DEPLOYMENT_RUNBOOK。

123默认SSH超时已实证与WorkBuddy校园网路由相关；单连接指定WireGuard后经123转130正常，未改全局网络配置。仍未取得现场弹窗失败接口和37/13具体班级，保留待查，不把防御修复当作现场根因证明。

## 待闭环（发布前历史清单，发布项以上节为准）

- 123恢复连通后取得课堂故障时段请求路径、响应码和脱敏认证日志，确定“登录状态保存失败”的实际来源。
- 取得37/13对应课程、班级、题目，按两种人数口径核查真实数据。
- 在不混入其他未发布功能的候选环境完成新前后端真实保存回显与角色冒烟，再按发布流程上线；本轮未做正式发布登记。
