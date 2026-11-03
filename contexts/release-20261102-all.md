# 2026-11-02 今日改动正式发布与验收

状态：**130正式1.30.66已上线**；更新144/PUBLISHED（18:29:59），真实教师更新接口可见。当天较早的1.30.64/65保持生效。

## 发布范围

| 功能 | 本次动作 | 真实验收 |
| --- | --- | --- |
| 课堂Python | 交互input、输入回显、停止/超时；精简说明、样例和stdin区，保留草稿/交作业 | 学生2022130113原有代码；桌面输入8回车→请买票，390px触控输入3→免票；整页宽390/滚动宽390，0 JS错误 |
| 跨校同班号监控 | 当前任课学校消歧，越界继续拒绝 | 课堂分组10项、全业务865项回归；真实教师desktop返回43人/分组200；数据库确认651666的2026级1/2班均分布155/181；该账号无凭据，未逐校登录 |
| 拓展课入口 | 补齐双角色教师前端入口，教师首页支持收起/展开 | 真实双角色教师打开新建表单、班级管理拓展班页签、收起后列表隐藏；管理/候选接口200，学生管理接口403。未创建测试班级/成员/签到 |
| 教材与PDF | 保留中午的绑定/学生入口/PDF修复，新增13张360px封面和鉴权缓存接口 | 13册接口与书架13图均360px，封面ETag304；整册PDF16,816,368B/1.44秒、本课767,229B/0.08秒，学生PDF403、目录200，匿名书架401 |
| 登录公开概览 | lessons缓存1小时、提前5分钟刷新，活动/焦点仍60秒 | 匿名概览接口200、登录页显示数据，师生正常登录；登录后的课堂和成绩不使用该缓存 |

未加入较早、仍待验收的课堂推进/题库切页后端功能；未重做旧50班逐人映射或冻结；未修改学生作品、正式提交和成绩。独立Python题单及DATA_LAB沿用原链路。

## 制品与基线

- 新release：`/data/apps/xueyeceping/releases/20261102_all_updates_v1`。
- 后端以线上1.30.65外层JAR为基线，只替换内嵌business；业务JAR条目集合保持，内容差异仅5个class：`TextbookController`、`TextbookStorageService`、`ClassGroupingService`、`LoginPreviewService`及其`PreviewCache`。其他后端模块、依赖、配置和资源沿用线上。
- 新外层JAR：108,185,635B；SHA-256 `a149fefa5ec1f7e35d55ac90de49e065e2e8c4f662eeac520a93a11c0adf5e8f`。
- 新business：1,556,090B；SHA-256 `94bcddeba649eb1776d572e7c7b6f17646133c5d98b4f2ba5a00019fcfd9e5eb`。
- 前端由已通过生产构建的Vue3 dist封装；首页SHA-256 `4ede5fc096f93a4475b102ed747d73106a5d875166a217c3d6c331e1cb89d3cc`；tar 43,229,962B / `8771587ab87ecd12b471b2c3241051851a33f1a78097e1f5bf6b3c60ce26066e`。
- 13张派生封面共313,427B，宽360px，上传到 `TEXTBOOK_ROOT/covers`；原教材正文图未覆盖。运行环境29项、外置配置、助手0.2.6和其他服务未改。
- 无结构或业务SQL，仅运行仓库 `sql/all_updates_release_20261102.sql` 登记更新；封面接口为新增，缺图回退原第一页。

## 备份与切换

备份目录：`/data/backups/xueyeceping/20261102_all_updates_v1`。

- 整库 `database.sql.gz`：52,104,859B；SHA-256 `118839d5d4622a62464e371962fd6b372276157d3670ee43c54833ed58cad4ef`。
- mysqldump/gzip退出码均0、gzip完整性通过、Dump completed存在；同时保存service/site/env/config/JAR/index和SHA256SUMS。原封面目录不存在，已记 `covers.before.absent`。
- 首次新后端已健康，但Nginx reload刚返回仍读到旧首页，严格门禁自动回退且旧版探活成功；修正为新连接最多等待30秒后重试，第二次切换成功。
- 最终后端启动18:23:58，PID2853051、NRestarts0；xueyeceping-130/nginx/aiot-lamp均active；启动后的journal ERROR为0；HTTP首页与磁盘/制品哈希一致。
- `/pyodide/pyodide.mjs`、`pyodide.asm.wasm`和新Worker均200，MIME分别为application/javascript、application/wasm、application/javascript。

## 验证证据

- Java `mvn -pl ruoyi-business -am test -DforkCount=0`：865项，0失败、0错误、3跳过；ClassGroupingServiceTest定向10项通过。
- Python运行控制Node测试6项通过，真实Pyodide10项通过；Vue3生产构建、登录与机位既有测试通过。正式站点额外完成真实学生桌面/390px操作。
- 真实接口报告 `output/release-20261102-all/api-verification.json` failures为空，覆盖教师/双角色/学生/教研员/匿名、封面、PDF、课堂、登录预览。
- 截图：同目录 `student-desktop.png`、`student-mobile.png`、`teacher-extension.png`、`teacher-bookshelf.png`；运行来源是正式站点，不是模拟接口。
- 制品源文件哈希和后端白名单差异见 `manifest.json`；备份、切换、最终服务状态和发布记录分别见 `backup.json`、`switch.json`、`final-inspect.json`、`published.json`。旧token或密码未写入这些报告。

## 回退

以本次备份的 `service.before` / `nginx.before` 恢复到systemd unit与站点配置（权限0644），执行daemon-reload、重启xueyeceping-130、nginx -t与reload，回到后端1.30.65和前端1.30.64；将更新144改DRAFT。无需回灌数据库、删除派生封面或执行结构回滚；旧release全部保留。助手、env和教材原图无需回退。

## 未验证边界

- 651666专属凭据不在私密文件，未用该账号分别进入两校课程卡；已核对真实跨校数据、服务端选择逻辑、拒绝越界回归和普通教师真实接口。
- 未在生产创建测试拓展成员/签到/题单，完整写链仍以此前隔离验证及回归为证据；旧50班迁移不属于本轮自动数据操作。
- 学校旧浏览器、实际机房低配和整班内存待验。JSPI缺失时明确提示使用新版浏览器；浏览器运行不等于Judge0判题通过，提示语仍计入stdout。
- 未代交正式学生作业，不宣称本轮完成真实成绩写库、教师重新批改或长期缓存的一小时观测。
