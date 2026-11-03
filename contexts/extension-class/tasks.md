# 拓展课双空间：实施与验收

> 当前状态（2026-11-02）：已随130正式1.30.64/65上线，1.30.66补齐双角色教师前端入口与首页收起；真实教师表单/页签/收起和权限接口复验通过，未创建生产测试班级或成员。旧50班逐人登记/冻结仍待核对。[集中发布验收](../release-20261102-all.md)。下方2026-10-30的“未发布”等描述为历史阶段。

更新日期：2026-10-30。用户确认旧50班数据在130，本轮直接本机开发；未访问130、未发布、未冻结旧账号。

## 已实现的本地切片

- [x] S0 本机前检、备份、结构迁移及幂等复跑；旧50班位于130，不因本机无旧数据阻塞代码开发。
- [x] S1 教师拓展班、成员抽屉、同校在读检验、唯一活跃归属、归档、负责人及管理员权限。
- [x] S2 账号空间偏好与首次选择、登录弹窗、顶部切换、独立拓展页、简单签到课、名单快照和幂等签到。
- [x] S3 Python空间列表/题目/草稿/提交校验，拒绝混投及DATA_LAB发布，投放成员学情。
- [x] S4 教师首页独立区块、班级管理页签、历史只读入口代码；实际旧50班迁移尚未执行。
- [x] S5 本地定向回归、打包、接口与桌面/390px浏览器验收，文档同步。
- [ ] 130旧数据只读前检、逐人账号对应、历史登记/冻结及真实旧签到/题单学情验证，须后续明确授权范围再执行。
- [ ] 真实Judge0运行、真实机房与生产学生验证；本轮提交闭环使用mock判题。

## 数据结构与备份

`sql/extension_class_v1.sql` 新增4表、7列：班级/活跃成员/课时名单/题单投放名单，lesson与plan的scope及归属、学生空间偏好及选择状态。四表显式使用与旧业务表一致的utf8mb4_general_ci，避免混合排序规则导致建课SQL失败；最终脚本在本机完整复跑两次成功。表级历史名单不随成员移出/归档删除。新成员立即获得已发布Python题单，同时补记题单投放名单，但不进入已建课时的签到名单。

本机库：`xueyeceping_server_20260729`。迁移前整库备份：
`D:/dmwprogram/newdazipingtai/backups/extension_local_20261030_124144/xueyeceping_server_20260729.sql`
103,680,919 bytes，SHA-256 `e342d024f76384cc5ba8ec742aebcd503714a1a95d9238f966f25af941106276`。

前检入口 `scripts/extension_precheck.py`，本机旧50班学生、任教、指派及题单关联均0，班号变体也未发现。该结果不代表130无旧数据。证据：`output/extension-precheck/20261030-121953.json`。

## 实际验证

- 后端90项定向及相关回归全部通过（18项拓展服务测试，以及Python、学生入口、课程推进、课程服务、删除与访问范围相关测试），0失败/错误/跳过。日志 `output/extension-backend-regression.log`。
- 独立目录完整 `mvn -pl ruoyi-admin -am clean package -DskipTests` 成功，未覆盖原8080占用的JAR。日志 `output/extension-package-isolated.log`。
- Vue3 `npm run build:prod` 成功，发布检查通过。日志 `output/extension-frontend-final.log`。
- 隔离MySQL库真实REST接口20项通过：建班/归属/跨年级加人/单一成员关系、账号偏好、非成员拒绝、幂等签到与名单、建课后加入拒签、Python成员数/混投/DATA_LAB发布拒绝、双空间与猜ID/草稿校验、历史名单、归档释放及常规入口。证据 `output/extension-verify/api-verification.json`。
- 追加4项真实接口验证：发布题单后新成员加入即能练习并进入学情，不能签到已建课程；移出后历史学情人数保留。证据 `output/extension-verify/late-join-verification.json`。
- 真实浏览器12项通过：教师首页建班/搜索原账号/发签到，学生首次选择/切换签到/Python页/刷新/换浏览器记忆/无拓展班不弹窗/移出失权返回，390px无溢出，0页面JavaScript异常。截图及报告 `output/playwright/extension-class-20261030/`。
- 生产构建补验4项通过：跨空间提交拒绝、模拟判题到结果闭环，最终迁移复跑，390px常规身份入口与独立拓展页无溢出，生产预览0页面异常。证据 `output/extension-verify/final-verification.json`。mock成功不能当作真实Judge0验收。

## 验收环境及边界

- 独立演示前端 `http://localhost:18081`，后端18080，数据库 `extension_verify_20261030`；使用原本机账号登录可自行建班，浏览器测试另建合成账号只存在于隔离库。
- 原8080后端未重启；本机主库仅执行本任务结构迁移，没有创建测试学生、班级、课程或冻结账号。原主库仍缺DATA_LAB快照/上传表等历史依赖，因此Python真实集成在隔离库补空表后验证；没有给旧实验伪造快照，不能宣称原库实验迁移完成。
- 隔离预览不是正式发布。旧50班历史读取代码已实现，但没有130数据迁移或冻结、也没有真实旧50班验收。
- 未引入产品依赖、未修改旧Vue2、未推送Git；未生成产品更新版本。切回旧应用可保留新增表列；不要整库回灌覆盖之后的数据。未来实际启用需配套新后端、前端与增量SQL。
- 自动安全审查阻止了隔离构建目录上传副本的清理，副本仍保留；原上传目录未删除。

## 2026-11-02 独立审核修复（本机，未发布）

- [x] P0：`selectPlanClasses` 的 `plan_class × extension_class` 字符串 JOIN 排序规则不一致（0900_ai_ci × general_ci）导致教师题单列表/详情 1267；改为 `CAST(... AS BINARY)`。本机主库实测新写法可执行并匹配 8 行，原写法仍报 1267；本地主库当前 0 个拓展班/0 条 legacy 数据。
- [x] `scripts/extension_precheck.py` 增加同形 JOIN 只读门禁：CAST 版本失败即以非零退出拒绝发布；原始的未转换 JOIN 只作为环境提示记录（`plan_class_raw_join`）。
- [x] 拓展班负责人候选与校验排除同时具有 `researcher` 角色的教师，避免负责人建班后无法管理自己的班。
- [x] `currentLesson` 拆为普通读与 `currentLessonForUpdate`；教师看板、学生首页轮询改走不加锁读，只有签到事务用 `FOR UPDATE`。
- [x] 学生空间 store 退出登录清空、按账号与请求序号丢弃迟到响应；登录时拓展 context 失败降级进 `/student/index`，不再阻断学生登录。
- [x] 验证：`mvn -pl ruoyi-business -am test -DforkCount=0` 850 项通过；Vue3 生产构建通过。
- [ ] 未做：重打 fat jar、重启 8080/18080、重跑 12 项浏览器流程；130 未连接，排序规则与迁移顺序仍须发布前只读复核。

## 2026-11-02 续：剩余 P2（本机，未发布）

- [x] 历史名单冻结：`BizExtensionClassMapper.roster` 去掉按当前行政班实时计算的 legacy 分支；题单学情 `plan_scope!='extension'` 改为 `plan_scope='regular'`，extension/legacy 一律读投放名单快照。
- [x] 新增 `sql/extension_legacy_snapshot_v1.sql`：按 legacy 载体与题单投放班级回填两张名单表，`INSERT IGNORE` 幂等；登记旧 50 班后立即执行一次。
- [x] `scripts/extension_precheck.py` 增加 `legacy_snapshot_backlog`（缺名单的历史课/历史题单数）与 `plan_class_join` 门禁。
- [x] 常规入口守卫：`LessonScopeUtils.requireRegular` 接入课程指派、课程设计器保存、课堂分组（两处快照）、在线协作、物联网实验（学生入口与教师列表）；`currentAssignmentQuery` 只取 `lesson_scope='regular'` 的课程。
- [x] 候选学生：学校类型随 SQL JOIN 返回，去掉逐行 `selectDeptById`；上限 200 改为取 201 行判断截断，接口返回 `{items,truncated}`，前端提示只显示前 200 人。新增 `add()` 复用班级学校的学段，避免逐人查部门。
- [x] 验证：业务模块 855 项测试通过（新增候选截断 2 项、守卫 3 项）；Vue3 生产构建通过；前检复跑 `plan_class_join ok/matched=8`、`legacy_snapshot_backlog 0/0`。
- [x] 130 发布（2026-11-02，1.30.64）：整库备份后执行 4 表 7 列迁移，复核 999 课/9 题单/27,897 学生默认 `regular`、拓展表 0 行；真实库验证原 JOIN 报 1267、`CAST` 版返回 85 行；三角色接口冒烟通过。证据见 `RELEASE_LOG.md`。
- [x] 双角色规则调整（2026-11-02，1.30.65 热更）：`requireTeacher` 只看 `teacher` 角色，兼任教研员的教师可管理/创建自己的拓展班；`create` 放行双角色本人建班；负责人候选与 `teacher/teachers` 查询仍排除 researcher。单测 23 项通过，130 实测教师 `classes/teachers/candidates` 200、候选 3 人不含双角色账号、学生 403。
- [ ] 未做：旧 50 班在 130 登记与 `extension_legacy_snapshot_v1.sql` 回填实跑（登记后必须立即执行，否则会把中间调动算进历史名单）。
