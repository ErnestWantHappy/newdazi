> 历史归档：2026-11-06第一轮方案。已被同日v2替代，不作为当前实施要求。

# 选择题图片支持功能技术实施方案

> 日期：2026-11-06。状态：**调研完成，方案待审阅，未实施**。
> 调研基线：工作区 HEAD `9d920c4` 加当前未提交文件；不能把本机源码等同于正式运行制品。
> 本轮仅新增本文档；未修改业务代码、配置、SQL、账号或题库数据，未部署、未重启服务。

## 1. 结论与证据边界

**推荐方案 A：保留文字字段，增加结构化图片引用，复用本地文件存储和现有上传/展示技术，补齐图片归属与历史保护。** 不把选择题整体改成富文本，不改 A—D 单选与自动评分规则，不复用操作题作品字段充当题目插图。

推荐的首期范围：题干最多 3 张图片，每个选项最多 1 张；允许纯文字、纯图片选项、图文选项。题干建议仍保留一句文字说明，降低题库检索和屏幕阅读障碍。数量和限制均是待确认建议，不是已经实现的约束。

最大的技术风险不是上传控件，而是**普通课程没有选择题内容快照，多课复用同一题库记录；修改题目可能改变历史回看，删除原图可能破坏旧试卷**。A 建议对“含图且已被使用”的题目保护内容，修改时另存为新题；若必须原地编辑并让每份历史课程保留旧版，应选 B 的版本/快照路线。

### 1.1 本文标记

| 标记 | 含义 |
| --- | --- |
| **[事实·代码]** | 已读取当前源码或 Mapper 确认；证明代码行为，不代表现场操作已通过 |
| **[事实·本机库]** | 本轮在本机真实 MySQL 只读查询得到 |
| **[事实·文档]** | 项目最新部署/验收文档记录；本轮未远程复验 |
| **[推断]** | 由调用关系、数据模型推导的影响，实施时须用测试确认 |
| **[建议/待决策]** | 尚未实施的设计，包括所有新字段、新接口、限制与编辑规则 |

### 1.2 调研方法与未验证范围

- 已按 `AGENTS.md → contexts/PROJECT_CORE.md → docs/architecture/INDEX.md` 接管，结合 `DATA_MODEL.md`、部署说明、相关专题和真实源码交叉核对。索引部分旧条目仍写 1.30.73/未发布，核心文档及最新发布记录写 1.30.74；本文不据旧索引倒推线上状态。
- 检索范围覆盖唯一维护前端 `RuoYi-Vue3/src`，后端 `ruoyi-business` 及必要的 `admin/framework/common` 调用链、仓库 SQL、既有测试。不是逐行通读无关业务，也未维护旧 Vue2 前端。
- 本机库通过既有 `scripts/textbook/ningbo2025_db.py` 的连接方法读取配置，查询会话设置 `TRANSACTION READ ONLY`，结束回滚并关闭；仅查元数据、数量和内容特征，不输出凭据/学生身份。
- **未登录 130/123，未核对正式实时表结构、磁盘、Nginx 配置或上传目录权限。** 正式环境信息以项目文档为依据，实施/发布前仍须现场只读前检。
- **未实测浏览器、上传、真实作答与评分；未运行编译和单测。** 本轮没有实现代码，后文测试均为实施后的验收要求，不是通过声明。
- 工作区有题库预览、教师工具及 Python 等既有未提交修改；本轮保留它们。本文只描述相关当前源码，后续实施要重新核对差异，不能混入发布制品。

## 2. 现状分析：选择题完整业务链路

### 2.1 实际技术与数据模型

**[事实·代码]** 后端 Maven 多模块、Java 8、Spring Boot 2.5.15、MyBatis；前端 Vue 3.5.16、Vite 6.3.5、Element Plus 2.9.9，使用 Composition API 和 `<script setup>`。依据：`RuoYi-Vue/pom.xml`、`RuoYi-Vue3/package.json`、实际页面和 Mapper。无需替换技术栈或新增对象存储服务。

**[事实·本机库]** `xueyeceping_server_20260729` 本轮核对结果：

| 对象 | 已确认结构/数据 | 对设计的约束 |
| --- | --- | --- |
| `biz_question` | `question_type varchar(20)`；题干 `question_content text`；`option_a/b/c/d varchar(255)`；`answer text`、`analysis text`；`file_path/preview_path varchar(255)` | 四个独立文字选项，不是选项 JSON；不能往 255 字符选项塞 Base64 或整段富文本 |
| `biz_lesson_question` | `id, lesson_id, question_id, question_score, order_num` | 只有课程关系/分值/排序，没有选择题内容版本或图片快照 |
| `biz_student_answer` | 学生/课/题引用、`student_answer text`、`is_correct`、`score`、时间等 | 没有选择题题干/选项快照；操作题版本指针不能直接用于选择题 |
| `biz_county_exam_question` | 抽测/题目关系、分值、排序 | 抽测草稿仍关联题库 |
| `biz_county_exam_paper_question` | 每学生每题快照；题干 `text`、四个选项各 `varchar(1000)`、答案/解析/分值/排序等 | 有文本快照但没有题干/选项图片字段；需要同步扩展 |
| 题型数量 | 选择 1,146，判断 37，打字 221，操作 334 | 是本机数量，不能作为正式库数量 |
| 选择题特征 | 题干含 `<img` 为 0；非空 `file_path` 为 0；四选项最长分别 58/60/51/45 字符 | 本机未发现把题干图存 HTML 或附件字段的现有选择题；不证明线上也没有 |
| 复用与快照 | 187 道选择题被多门课引用；个人抽测题快照共 4,187 条、涉及 12 个题目 ID（包含各题型） | 不能把“改一题”当作“只影响一个页面/一份试卷” |

### 2.2 从录题到回看

| 环节 | 当前行为及证据 | 图片功能的影响 |
| --- | --- | --- |
| 新建/编辑 | `views/business/question/index.vue` 使用 textarea 题干、四个 `el-input` 选项；前端新建要求四个选项文字非空。`BizQuestionController` 接收 `BizQuestion`，`BizQuestionServiceImpl` 保存，Mapper 显式列映射 | 新增图片状态、图片或文字的组合校验；不能只放开前端必填，后端也要校验 |
| 可见性/编辑权 | 列表过滤公开或本人；私有详情限本人/管理员，编辑删除验证创建者；按钮还有 add/edit/remove 等权限 | 图片读取/绑定要跟随题目与使用场景，不能“知道路径就能读” |
| 题库摘要/详情 | `QuestionCard.vue`、`questionWorkbench.js` 只提取非空文字选项；题库详情也用文本插值 | 纯图片选项会被摘要过滤，必须改成“文字或图片存在” |
| 删除/批量删除 | `AnswerDeletionGuardService.assertQuestionsDeletable` 按普通答案阻止删除已有作答题；批删也验归属 | 现有防删不等于完整图片引用保护；还要考虑课程引用、抽测快照、归档等 |
| 复制/跨课复用 | 课程和抽测组卷直接选择已有 questionId；设计器手工拣选 optionA 等字段组成已选列表。已查常规题库 Controller/API/页面未找到独立选择题复制接口/按钮 | 复用是已存在功能；“另存为新题”是 A 新建议，不能宣称系统已有复制能力 |
| 导入/导出 | `/business/question/importData`、`/importTemplate`、`/export` 使用 `ExcelUtil<BizQuestion>` 和 `@Excel`。导出只排除操作题。导入前检题干、单字符答案 A—D、正确选项文字、A/B 文字；并不是与页面完全相同的四项必填规则 | 保持旧模板；图片列须可选，不能把已有两选项导入直接改成四项强制 |
| 普通课组卷/开启 | `lesson/designer.vue`、`BizLessonController /save-all`、`BizLessonServiceImpl` 保存题目关联/分值，指派班级并开启；固定、随机排序、随机抽题逻辑已存在 | 列表、已选列表、刷新回显都带媒体；随机只改变题目集合/顺序，不拆散选项与图 |
| 普通课展示 | `StudentHomeController /current-lesson` 查 `BizLessonQuestionMapper.selectDetailsByLessonId` 联表实时取题库；经 `GuideSheetStudentViewService.toStudentLessonQuestions` 显式复制到学生 VO | 只加 BizQuestion 字段不够，Mapper、教师详情 VO、学生 VO 和转换代码都要同步；学生 VO 继续排除答案 |
| 普通课作答/评分 | `views/student/index.vue` 点击 A—D，`/submit-answers` 接收 questionId→字母；`StudentHomeController` 从服务端课题详情取标准答案，忽略大小写比较，全对给该题分，否则 0；`StudentAnswerSubmissionService` 处理持久化与终态等保护 | 图片只参与内容展示，不能进入答案值、正确性判断或新建成绩表；重做 revision/防重复规则保留 |
| 历史成绩/错题本 | `/history-scores` 是成绩汇总；`/wrong-questions` 的 SQL 把当前课题关系、当前题库与错题答案联表；学生页错题可再次练习 | 目前不是普通课完整试卷内容快照。含图错题必须可回访读取，不能仅允许“当前课”的图片权限 |
| 教师回看/学情 | 教师首页共享课程抽屉展示题干/选项；`ScoreQueryController /analysis/{lessonId}` 手工组装 `optionContents`；成绩页和 `AnalysisMatrix.vue` 使用该结构，图表 tooltip 拼 HTML | 图片要进入学情明细，不塞进 `optionContents` 字符串；统计百分比/人数不变，复杂图不放进小 tooltip |
| 区域抽测 | `CountyExamServiceImpl.openExam` 在开启事务内为参考学生生成试卷；`ensurePaperForStudent` 显式复制题干/选项/答案等到个人快照，已有快照不覆盖；提交/评分使用个人快照 | 必须同步复制媒体并保留文件；不允许旧快照缺媒体时回源“补当前题库图片” |
| 抽测学生/评卷/成绩 | `buildStudentExamPayload` 使用个人快照，清除 answer/analysis/scoringItems；`countyExam.vue` 展示；管理详情用题库组卷列表，分析/评卷部分使用快照 SQL，评卷主要针对操作题 | 明确管理预览与个人快照不是同一数据源；图片资源权限不能依赖源题仍公开或存在 |

### 2.3 未发现的功能与明确排除的模块

- **[事实·代码检索]** 在当前选择题调用链未发现“AI 生成选择题/整份选择题试卷”的独立入口。已存在 AI 操作题批改、纸卷识别学情、流程图拍照生成答案，以及 Python 双 Sheet 导入；它们不等同于通用 AI 出题，不需为本任务改模型提示词。
- 已有通用 `components/Editor/index.vue`（Quill）、图片上传/粘贴等能力，但选择题未使用。课程设计器还有 `stripHtml`、文本摘要和统计工具；直接把题干改成 HTML 会让多个入口丢图或展示标签。
- 已查选择题、课程设计、成绩、抽测前后端入口，未发现完整选择题试卷的专门 Word/PDF 导出或打印入口；当前 PDF 预览主要为操作题附件，成绩导出是 Excel。**不新增完整试卷打印/Word/PDF 功能**；浏览器自带打印可作为布局检查，不能冒充产品已有打印链路。
- `views/business/student/home.vue` 是另一份只读当前课展示源码；实际路由/数据库菜单命中须实施前核对，不默认是主学生页。要么接入共用展示，要么以证据确认不用；不能因为文件名相近直接删掉。
- 纸卷学情 `PracticalInsightDrawer.vue` 的 stem/options 来自纸卷识别结果，不是 `biz_question.option_a`，本轮不修改。操作题图片作品、教材页图、导学单图片也保持各自业务归属。
- 未见选择题专用内容缓存/不可变题目版本；学生页面有题目排序、已答恢复等状态。新增媒体需查清刷新回显和缓存键，但不能把操作题/流程图已有专用快照当作选择题快照。

### 2.4 已有接口索引

以下路径已从Controller核对；新增media必须贯穿对应返回/保存链，不能只修改题库详情：

| 接口 | 相关用途 |
| --- | --- |
| `GET /business/question/list`、`GET /business/question/{questionId}` | 题库、组卷候选、详情 |
| `POST /business/question`、`PUT /business/question`、`DELETE /business/question/{questionIds}` | 新建、更新、单/批量删除 |
| `POST /business/question/importData`、`/importTemplate`、`/export` | Excel导入/模板/导出 |
| `GET /business/lesson/details/{lessonId}`、`POST /business/lesson/save-all` | 普通课详情和设计保存 |
| `GET /business/student-home/current-lesson`、`POST /business/student-home/submit-answers` | 普通学生题面和正式提交 |
| `GET /business/student-home/wrong-questions`、`/history-scores` | 普通错题与成绩历史 |
| `GET /business/score/analysis/{lessonId}`、`/studentAnswerMatrix`、`/export` | 学情、作答矩阵和成绩Excel |
| `POST /business/countyExam/{examId}/questions`、`/{examId}/open` | 抽测组卷保存、开启并生成个人快照 |
| `GET /business/countyExam/student/current`、`POST /business/countyExam/student/draft`、`/student/submit` | 抽测个人题面、保存草稿、正式提交 |
| `GET /business/countyExam/{examId}/summary`、`/{examId}/export` | 抽测分析和成绩导出 |

接口存在不代表当前角色可以无条件调用，权限/归属继续以对应Controller和Service为准。`history-scores`与成绩导出不承载完整题面，不为了媒体强行扩大返回内容。

## 3. 现有图片基础设施与可复用边界

| 能力 | 已确认事实/路径 | 复用结论 |
| --- | --- | --- |
| 上传 UI | `RuoYi-Vue3/src/components/ImageUpload/index.vue` 默认 5 张、单图 5MB、png/jpg/jpeg，支持预览/移除/排序；默认 `/common/upload`；值输出逗号串 | 可以参考/薄封装交互；当前缩略图直接拼 `/profile`，与直接访问禁令冲突，不能原样拿来就宣称可用；缺明确 uploading/error 父级契约，需局部补足或用现有 el-upload 组合 |
| 读图 UI | `components/ImagePreview/index.vue` 已把 `/profile/upload` 转 `/common/resource/view`，Element Plus 支持放大；默认 `fit=cover` | 复用预览交互与 URL 处理思路；题目图必须 contain/自然比例，不裁切代码和流程图，不全局改组件默认影响其他业务 |
| 通用上传 | `ruoyi-admin/.../CommonController.java /common/upload(s)` 限 admin/teacher/researcher，调用 `FileUploadUtils`，返回 fileName/url/originalFilename | 底层存储可复用；接口本身没有题目图归属记录、逐题绑定校验或安全的未保存图片预览闭环 |
| 文件存储 | `RuoYiConfig.getUploadPath()` 位于 profile 下的 upload；`FileUploadUtils` 用日期目录和唯一文件名，保存 `/profile/...` 路径 | 新图用独立子目录，数据库不写部署域名、临时 blob URL 或 Token |
| 大小/格式 | 通用工具默认 50MiB，主要按扩展名白名单；`application.yml` multipart 单文件55MB/请求60MB。前端5MB不等于后端5MB | 新增题目图专用服务器校验，不全局修改通用上限 |
| 图片校验/压缩 | `ResearchActivityUploadService` 已做 JPG/PNG/WebP 扩展名、MIME、签名校验；原图保存，另生成登录展示 JPEG（边长1600、质量0.78）；WebP 无解码器时有降级 | 可复用校验思路/极小底层方法，不调用教研业务上传接口冒用权限；登录展示压缩不能作为考试图唯一存本，文字截图优先保清晰 |
| 鉴权 | `SecurityConfig` 禁止 `/profile/**` 直接读取，头像例外；通用读图经 `ResourceAccessService/Mapper` 反查业务归属；JWT 过滤器只对特定 GET 文件路径回退同源 Cookie | 新媒体必须有自己的业务授权；不能放开 `/profile/**`，不能在 URL 传 JWT，也不能假设任何新 `<img src>` 接口自动支持 Cookie |
| 未保存资源 | 通用上传无归属表；资源兜底对找不到归属的文件只允许管理员；现有题目资源反查主要覆盖 file_path/preview_path、操作题材料 | 新字段不会自动进入授权查询；新建题老师可能上传后无法预览。须记录上传者并提供草稿授权 |
| 删除/清理 | ImageUpload 的移除只改 modelValue，无物理删除请求；研究活动附件有提交后删除/回滚清理，纸卷有专用暂存过期清理 | 未找到可直接复用的“全业务引用统计后清理题目图”机制。不能把作品/纸卷清理器直接扫题目图目录 |
| 部署 | **[事实·文档]** 130 为 Linux/systemd/Nginx，上传 profile 使用 `/data/upload`，在 release 之外；旧123为Windows栈 | 本地文件路线可行。实施前验证磁盘/备份/账户读写权限/请求体/代理；本轮未证明正式当前容量足够。部署文档明确 `/profile` 不能改成静态 alias 绕过鉴权 |

**[推断]** 普通课当前资源授权只按当前课程指派判断学生权限，直接复用会使已推进学生的私有错题图看不到；共享图片还存在“反查仅取一个 questionId”的授权歧义。因此建议新图按明确的题目/课程/个人抽测场景鉴权，而不是给通用兜底再加一个公开目录。

## 4. 方案对比与推荐

### 4.1 方案 A：增量媒体字段 + 独立上传元数据 + 使用后保护

**[建议/待决策]**

1. 保留 `questionContent`、`optionA/B/C/D`、`answer` 原语义；`biz_question` 和 `biz_county_exam_paper_question` 各新增一个可空 `media_json TEXT`。应用用版本化对象解析，不要求 MySQL 原生 JSON 类型。
2. 只新增一张题目图元数据表，记录图片 ID、上传者、受控路径、格式/尺寸/字节数/哈希、暂存/保留状态等；解决未保存预览、绑定校验和文件定位，不建设全平台文件中心。
3. 题干/选项图片先上传，再随现有保存一次绑定。新上传成功不等于题目已保存。资源不可覆盖；更换生成新 ID/新文件。
4. 抽测沿现有快照机制复制媒体 JSON；旧快照 NULL 代表当时无图片，永不从当前题库动态补图。
5. 普通课不新增完整内容快照：对已含图或本次拟增加图片且已使用的选择题，内容修改走“另存为新题”；原题和原图继续供原课程/错题回访。详见第5.4节。
6. 原图已正式绑定后首期保留，不做自动物理删除；只清理超期、从未绑定的暂存图。后续有容量证据再做完整引用治理。

优点：不改学生答案/评分/题库文字结构；只扩展两张现有表和一张小型元数据表；避开普通课全面快照与历史回填。缺点：被使用的含图题不能原地替换内容；永久保留图增长需要监控；不能保证老版本客户端理解含图题。

### 4.2 方案 B：通用题目媒体关联 + 题目版本/使用快照

**[建议/待决策]** 保留旧文本列，另建通用媒体表、题目版本表、版本媒体关系（slot=STEM/OPTION_A…），普通课关系绑定不可变选择题内容版本，抽测快照同时冻结版本/媒体。读取、作答时使用同一个版本，教师改题产生新版本，旧课程/错题继续指向旧版本。支持未来判断题题干图、多图、图注和更可靠的引用回收。

建议普通课在首次对学生开放前冻结所选版本，已开放或有答案后不自动升级；新一轮课程可选新版本。冻结时同时锚定文字、选项、答案和媒体，课程分值仍按原课程关系规则管理；重做继续使用原课原版本。准确冻结时机与课程保存/开启的并发约束，是B必须进一步设计和验收的内容。

范围限定：先适配已有选择题，预留其他题型 slot；不借机重做其他题型、评分规则或全站编辑器。不能只版本化图片而仍实时读旧文字/答案，否则旧题干与新图仍可能错配。

优点：教师可以持续改题，历史完整性更强，媒体引用可索引查询/清理；扩展更多题型更自然。缺点：需要处理普通课版本绑定时机、已答/重做/删题归档、两个版本并行和历史回填；显示与评分必须共同选择版本，跨模块改动和回归明显更多。旧历史无法从现在恢复出过去未保存的题干，迁移只能标为“升级时基线”。

### 4.3 比较

| 维度 | A：增量字段与使用后保护 | B：版本/快照与关系模型 |
| --- | --- | --- |
| 开发复杂度 | 中等；难点在权限/导入导出/多入口和保护并发，不只是放两个上传框 | 高；增加版本选择、课程状态、历史迁移与索引关系 |
| 旧纯文字兼容 | nullable 增量、旧字段原样保留，成本低 | 能兼容，但需版本缺失兜底并明确来源 |
| 教师改题 | 未使用可直接改；已使用含图题另存，旧课不自动换题 | 新版本可原地编辑草稿；明确哪些使用方升级到新版 |
| 历史保护 | 保护新含图题及已有抽测快照；不宣称修复所有旧纯文字历史问题 | 可以逐步统一新历史；无法重建过去缺失的真实版本 |
| 长期维护/回收 | 初期简单；已绑定图默认保留，需要容量监测 | 初期复杂；引用关系清楚后更容易精确回收 |
| 技术风险 | 漏掉展示/VO、图片授权、内容锁并发、旧前端漏图 | 上述风险之外再加版本错选、回填与评分源错配 |
| 后续题型扩展 | STEM/slot格式可复用；新增题型仍需各自校验/视图适配 | 更好，但不能假设预留模型就自动支持所有题型 |
| 依赖/基础设施 | 现有 MyBatis/Jackson/Element Plus/本地文件即可 | 同样可不加外部依赖，主要成本是领域与测试 |

**不推荐直接富文本化**：已有 Quill 不代表题库全链支持 HTML；四选项255字符、多个文本/stripHtml入口、Excel 与 HTML tooltip 都会受影响。若未来需要文字中间任意嵌图/公式，可另立需求，不能为了几张插图启动整体改造。

**推荐 A 的条件**：接受使用后的含图题另存，以及首期不自动删除已绑定原图。如果不接受该编辑约束，选 B；不能删除这层保护后仍承诺普通课历史可靠。

## 5. 推荐方案的数据、接口与历史保护设计

### 5.1 数据格式示例

**[建议/待决策]** API 使用对象 `media`，数据库 `media_json` 由服务端序列化；原有字段继续是纯文字。示例 ID 仅为说明：

```json
{
  "questionId": 12001,
  "questionType": "choice",
  "questionContent": "观察下面的流程图，判断运行结果。",
  "optionA": "输出 1",
  "optionB": "",
  "optionC": "输出 3，示意如下",
  "optionD": "输出 4",
  "answer": "B",
  "media": {
    "schemaVersion": 1,
    "stem": [{ "imageId": "501", "alt": "题目流程图" }],
    "options": {
      "A": [],
      "B": [{ "imageId": "502", "alt": "选项B运行结果" }],
      "C": [{ "imageId": "503", "alt": "选项C输出示意" }],
      "D": []
    }
  }
}
```

imageId 为字符串表达的 bigint，避免 JavaScript 大整数损失。数据库保存 ID/图注/顺序，不保存服务器绝对路径、前端 URL、base64 或鉴权令牌。原图宽高、字节数、格式由元数据表服务端解析，前端不能伪造。

**学生题目输出仍不得包含上述示例的 `answer` 或解析**；只把 media 的安全字段加入学生 VO。服务端可另返已校验的尺寸供预留布局；API URL 由统一资源组件拼接。

### 5.2 建议数据库增量（不是可执行 SQL）

| 表 | 增量/字段 | 兼容与约束 |
| --- | --- | --- |
| `biz_question` | `media_json TEXT NULL` | 只选择题首期可写；原题 NULL，不回填业务内容 |
| `biz_county_exam_paper_question` | `media_json TEXT NULL` | 与题干/答案一起复制；已生成快照不覆盖 |
| 新 `biz_question_image` | `image_id bigint PK`、`uploader_id bigint`、`resource_path varchar(500)`、`mime_type varchar(64)`、`byte_size bigint`、`width/height int`、`sha256 char(64)`、`state varchar(16)`、创建/绑定时间 | 路径唯一；state 首期 TEMP/RETAINED；索引 `(uploader_id,state,create_time)`、`(state,create_time)`；不跨账号自动去重 |

不新增答案列、分数列、单独“图片选择题”题型，不改变 `practical_image_max_count`、file_path 或 preview_path 的操作题含义。不加全平台媒体引用表，是 A 与 B 的重要成本边界。

### 5.3 旧数据与旧接口的具体语义

1. 读：NULL media_json→空对象/空数组，旧文字展示不变。未知 schemaVersion/损坏 JSON 不静默当无图：记录脱敏错误；教师显示修复提示，学生明确“题目图片暂不可用”，防止把异常内容当完整试题。
2. 写：旧客户端**未提交 media 字段**→保留原媒体；显式空对象/空数组且通过内容保护→清空。新增请求 DTO 应区分“未提交”和“清空”，不能以 Java null 同时代表两种意思。
3. 选项规则：页面新建保持当前四选项习惯，但每项由“文字或有效图片”满足；Excel 保持既有 A/B 必填、C/D可选策略，正确字母对应项必须有文字或图片。不能因本功能强制所有历史两选项题补齐 C/D。
4. 题干首期仍要求非空文字说明；若需要整题仅一张图片再放开，另行确认，避免新增“无法检索的空题名”。选项纯图片则首期必须支持。
5. 不改答案值，仍单字符 A/B/C/D；服务端统一做有效性校验，不接受 imageId/path/HTML 作为 student_answer，不信任客户端正确答案或得分。
6. 旧客户端能继续使用旧纯文字题，不代表可正确展示新含图题。采用前后端协同发布、已有页面刷新提示；服务端识别旧客户端修改含图题时应拒绝可能擦除/误改的请求并提示刷新。不能以一个通用“未提交媒体不修改”替代含图题内容保护。
7. DTO 转换/Mapper 所有投影显式带媒体；课程设计器 push 的新对象也要带 media，否则选入即丢，刷新才出现。

### 5.4 普通课程与历史：A 的边界必须明确

**[事实·代码]** 当前普通课展示、提交评分、错题查询均读当前题库，不具备恢复过去题面的方法。数据库“已经有答案”不等于“已经冻结题目”。

**[建议/待决策]** 保护规则：

- 未使用的含图题可更换/移除图、改题干/选项/答案，保存失败保留编辑内容。
- “已经使用”建议包含：被任何课程题目关联、抽测组卷关联、个人抽测试卷快照、普通答案或删题归档引用；不只看当前活跃课或当前学生答案。
- 只要旧题已有媒体或本次拟新增媒体，并已使用，就禁止覆盖内容包（题干、四选项、答案、解析、媒体，以及改变该含图选择题的题型）。允许不改变题意的管理元数据按既有权限保存；不能通过先清空图、改题型或旧客户端请求绕过保护。
- 页面在开始编辑前说明“本题已用于课程，为保留原题，请另存为新题”；主操作“另存为新题”，保留文字/图片做草稿，服务端重新校验复制资格与图片引用，创建者/审计字段重建。
- 不提供独立复制新依赖，优先复用现有新建接口与表单；必要时增加 `sourceQuestionId` 供服务端按已授权源题校验可复制图片。不能只提交任意 imageId 就绑定其他教师私有图。
- 原课程继续引用旧题，新题需教师明确选入新课程。若旧课已有学生答案，不自动替换 questionId、不自动移动/重算成绩；沿用现有课程改题归档与重做限制。
- 普通旧纯文字题的历史实时读取问题原本就存在，本轮不大面积改锁规则。对“已有纯文字题新增图”的请求采用另存方式，避免把今天的图补进过去课程。
- 绑定课程与编辑题目需有共同并发控制：事务内锁定同一题目行、复查引用；抽测生成快照与改图也要消除检查后写入窗口。只做前端禁用不能保护历史。

该规则改变的是含图题的编辑方式，**不是评分公式**，但仍属于需用户确认的业务选择。B 的版本/快照是接受不了此约束时的替代方案。

### 5.5 上传、预览与权限契约

以下接口名称均为拟议，尚不存在：

| 接口/操作 | 建议契约 | 安全与体验 |
| --- | --- | --- |
| `POST /business/question-images` | 上传单图，返回 imageId、服务端宽高/大小/格式、TEMP状态 | 沿用教师侧角色和题库 add/edit 能力，仍需保存时逐题权限；学生不可调用；上传成功只表示文件已存 |
| 现有 POST/PUT `/business/question` | 增加 media 对象，事务内验证并绑定 | TEMP必须本人；复制引用须验证源题可读/可复制；拒绝外部 URL、任意路径、已清理ID、无效slot、超量和越权 |
| `GET /business/question-images/{imageId}/content` | 身份鉴权后，以草稿/题目/课程/抽测具体上下文取图 | 使用现有 Axios/request 带 Authorization 获取 Blob，再交 el-image；卸载释放 objectURL。不扩大 Cookie 白名单、不给公开地址 |
| 移除/更换 | 未保存只改草稿；保存成功更新引用，新文件使用新ID | 不在点击“移除”时删除磁盘原图；保存前更换失败仍保留旧图 |
| 暂存清理 | 后台任务仅清理本功能 TEMP，建议超过7天；绑定成功设 RETAINED | 与绑定同一行锁/事务竞争，清理前复核；先状态隔离再提交后删文件，失败可重试，不扫 upload 根目录 |

读取时按场景校验，不能信任客户端传来的 lessonId/examId：

- 草稿：仅上传者本人或管理员读 TEMP；尚无题目 ID 时也能预览。页面刷新恢复不是已保存功能，离开未保存需提示；不得把草稿当正式题。
- 教师题库：题目中确实含此 imageId，且按既有规则可读该题。无关教师不能只凭 ID 读私有图。
- 普通课学生：当前课必须按后端真实班级/指派关系校验；历史错题必须验证该学生自己在该课该题的有效答案及可回访记录。不能仅因题库公开就绕过当前学生业务范围。
- 教师课程/学情：沿用课程管理、共享只读预览、授课班级等现有访问规则；不能扩大为所有教师通读任意课程私有图。
- 抽测学生：按本人个人试卷快照中的 imageId 和既有考试状态规则读取；源题被改成私有或删除，不影响已授权个人快照。
- 抽测管理/评卷：沿用管理者/已分配任务/开放评卷条件；当前没有选择题人工评卷需求，不为图片给普通评卷人额外整卷权限。
- 传入多个场景参数需有明确互斥/一致性校验，拒绝歧义，不能“任取一个可用上下文”越权。

默认 JPG/JPEG/PNG、单张≤5MiB；同时校验扩展名、MIME、实际可解码内容、宽高与像素总量，建议最大边8192、总像素2000万，解码前先读尺寸避免大图耗内存。数字待样图验证后确认。SVG/GIF首期不收；WebP待现有JDK/解码依赖能力确认后决定，不能为格式支持临时加复杂依赖。

文件位于 `profile/upload/question-images/日期/随机名`，保留原图、不覆盖；可预留独立缩略图但不默认做有损压缩。截图/流程图放大必须取得原图；不接入 LibreOffice 转图。返回正确 Content-Type、nosniff、私有缓存策略，缓存键包含图片 ID/身份场景，不跨用户复用未授权 Blob；会话退出/切换角色释放缓存。

### 5.6 已绑定图片清理、备份与容量

- A 首期不自动清理 RETAINED，即便当前题库 JSON 已移除，也可能被复制题、抽测快照或归档引用。删除题目仍不代表可以删图。
- TEMP→RETAINED 与题目保存同事务，失败不能假绑定；磁盘写成功、元数据落库失败时清理本次新文件或记录可重试补偿，不能丢出假成功。
- 未来正式回收必须核对全部源题/复制题、个人抽测快照和历史归档，具备引用索引、隔离保留期、恢复清单后再启用；不以 JSON LIKE 扫描线上答案大表作为每次读图的鉴权方式。
- 容量估算按真实大小：满配7张图、每张1MiB则一题约7MiB；不是预测实际平均值。先统计典型截图与新增题量，再评估磁盘/备份/同班并发；已绑定长期保留需磁盘告警。
- 数据库备份必须配套图片目录增量备份和清单/哈希，图片在 release 外，升级应用不能清空。只恢复数据库而没有对应原图，不算可恢复。

### 5.7 Excel 导入导出边界

**[事实·代码]** `ExcelUtil` 有通用 `ColumnType.IMAGE` 嵌图读写，但 `BizQuestion` 当前没有图片列；通用导入会把图片字节写文件，`ImageUtils` 还能取远程 URL。不能简单加注解就认为已满足本功能的归属/格式/事务/安全要求。

**[建议/待决策]** A 保留原模板文字列，增加可选的“题干图片引用1/2/3、选项A/B/C/D图片引用”列，以本站图片ID的带版本引用表达，不包含Token；教师正常录题不需要看到或填写技术ID，导出回导时由系统带入。这属于同站点数据交换，不是跨平台图片备份。

- 导出含图题时必须保留媒体引用，不能默默变成文字题；缺图/无权限需可见报错或结构化失败清单，不产出伪完整文件。
- 同站点导入引用须逐行验证图片存在、来源可读/可复制，再整批事务创建；旧无图模板维持现有行为与错误行提示，失败不留下半批题目。
- 不从任意外链下载图片；外站引用/不存在ID/Excel直接粘贴图片但未走支持格式，明确报错引导在录题页补图，不悄悄忽略。
- 真正“离线带图导出→其他环境重新导入”需要 ZIP清单+原图包，或安全的Excel嵌图适配。作为可选后续阶段单独估算；不能因底层有嵌图能力就纳入低成本承诺。
- 若用户要求首期Excel内直接看图且完整往返，应将其作为独立必做任务，复用POI而非新增库；限定总图数/总字节和本地资源读取，先验证后绑定，避免套用通用写盘分支绕过题目图治理。

## 6. 影响范围清单

以下“改”表示未来 A 的预计修改，不表示本轮已修改。路径均为仓库相对路径，可按前缀定位；实施前重新核对现有工作区差异。

### 6.1 前端

| 文件 | 影响/动作 |
| --- | --- |
| `RuoYi-Vue3/src/views/business/question/index.vue` | 改：媒体初始值、上传状态、四选项组合校验、新建/编辑回显/另存、保存防重/失败保留、Excel提示、完整详情 |
| `RuoYi-Vue3/src/views/business/question/components/QuestionCard.vue` | 改：图题标识/缩略图与纯图片选项摘要，卡片保持紧凑；完整阅读放详情 |
| `RuoYi-Vue3/src/utils/questionWorkbench.js` | 改：choiceOptions保留有图无字项；摘要不把媒体误判为空；相关既有测试随之调整 |
| `RuoYi-Vue3/src/views/business/lesson/designer.vue` | 改：候选、已选、显式构造newQuestion、加载详情回显；避免只传旧文本字段 |
| `RuoYi-Vue3/src/views/business/teacher/index.vue` | 改：本人/共享课预览，四项v-if由文字存在改为内容存在 |
| `RuoYi-Vue3/src/views/student/index.vue` | 改：理论题、错题本两套模板，选中/放大事件，图片加载/重试，返回刷新恢复；保留原提交规则 |
| `RuoYi-Vue3/src/views/business/student/home.vue` | 核查实际菜单命中；如在用则适配只读展示，不删除 |
| `RuoYi-Vue3/src/views/business/score/index.vue` | 改：学情图题详情、答案/选项内容展示；HTML图表tooltip只放安全摘要/明细入口 |
| `RuoYi-Vue3/src/views/business/score/components/AnalysisMatrix.vue` | 改/核查：纯图片选项不标未知内容，提供题目详情入口；矩阵保持紧凑 |
| `RuoYi-Vue3/src/views/business/countyExam/examDesigner.vue` | 改：候选/已选/手工字段复制及预览 |
| `RuoYi-Vue3/src/views/business/countyExam/index.vue` | 改：管理试卷预览/getChoiceOptions，含图无字不隐藏 |
| `RuoYi-Vue3/src/views/student/countyExam.vue` | 改：个人快照媒体、原图放大、只读/已提交状态 |
| `RuoYi-Vue3/src/views/business/countyExam/components/AnalysisPanel.vue` | 核查：当前以汇总图表为主，不为只看统计强行加载所有原图 |
| `RuoYi-Vue3/src/views/business/countyExam/grading.vue`、`views/business/teacher/grading.vue` | 回归操作题作品/现有图，不默认增加选择题人工批改功能 |
| `RuoYi-Vue3/src/api/business/question.js`、`studentHome.js`、`lesson.js`、`score.js`、`countyExam.js` | 核查新增媒体透传/Blob请求/响应处理；未改变接口契约的文件不为形式而改 |
| `RuoYi-Vue3/src/components/ImageUpload/index.vue`、`ImagePreview/index.vue`、`Editor/index.vue` | 参考/局部复用，不全局改默认渲染或引入富文本；图片组件若要扩展仅做兼容增量 |
| 拟新增 `RuoYi-Vue3/src/components/QuestionMedia/`、`src/api/business/questionImages.js` | 小型上传/题面展示/鉴权图片读取单元，控制生命周期；展示组件无答案/评分逻辑 |

### 6.2 后端与存储

后端业务前缀：`RuoYi-Vue/ruoyi-business/src/main/`。下表 `java/...` 与 `resources/...` 均相对此前缀，避免重复超长目录。

| 文件 | 影响/动作 |
| --- | --- |
| `java/com/ruoyi/business/domain/BizQuestion.java` | 改：media输出，避免直接用同一实体承担“字段是否提交”的更新契约 |
| `java/com/ruoyi/business/controller/BizQuestionController.java` | 改：保存DTO/导入导出适配；权限保持，不把图片正文打进审计日志 |
| `java/com/ruoyi/business/service/impl/BizQuestionServiceImpl.java` | 改：选择题图文校验、绑定/旧字段保留、使用后保护、复制来源校验、删除保护、导入一致性 |
| `java/com/ruoyi/business/mapper/BizQuestionMapper.java`、`resources/mapper/business/BizQuestionMapper.xml` | 改：媒体读写、锁定/引用核对；保留动态SQL清空/未提交差别 |
| `java/com/ruoyi/business/domain/vo/BizLessonQuestionDetailVo.java`、`StudentLessonQuestionVo.java` | 改：教师/学生媒体投影；后者不加标准答案 |
| `resources/mapper/business/BizLessonQuestionMapper.xml` | 改：普通课程详情显式取媒体 |
| `java/com/ruoyi/business/service/GuideSheetStudentViewService.java` | 只改toStudentLessonQuestions媒体复制，导学单清洗/业务保持 |
| `java/com/ruoyi/business/controller/StudentHomeController.java` | 核查current-lesson/submit-answers/wrong-questions；保持评分，媒体权限复用服务端课程/学生范围 |
| `resources/mapper/business/BizStudentAnswerMapper.xml` | 改：错题投影媒体；不动score/history统计SQL口径 |
| `java/com/ruoyi/business/service/StudentAnswerSubmissionService.java` | 回归而非默认改：答案唯一性/终态/revision保护 |
| `java/com/ruoyi/business/controller/ScoreQueryController.java`、`domain/vo/QuestionAnalysisDetailVo.java` | 改：学情返回独立media；optionContents仍为文字 |
| `java/com/ruoyi/business/service/impl/BizLessonServiceImpl.java` | 必要改：选择题内容保护与课程关联写入并发锁配合；沿用移除答案归档，禁止自动迁分 |
| `java/com/ruoyi/business/service/AnswerDeletionGuardService.java` | 必要改/辅助调用：补含图题源记录依赖保护；不变成无条件禁删所有题型 |
| `resources/mapper/business/CountyExamQuestionMapper.xml` | 改：抽测源题详情媒体 |
| `java/com/ruoyi/business/domain/CountyExamPaperQuestion.java`、`resources/mapper/business/CountyExamPaperQuestionMapper.xml` | 改：个人快照媒体读写 |
| `java/com/ruoyi/business/service/impl/CountyExamServiceImpl.java` | 改：开启快照复制、并发保护；学生输出继续去答案/解析 |
| `resources/mapper/business/CountyExamAnswerMapper.xml` | 核查：分析/评卷使用个人快照的地方，确需题面时加媒体，不在聚合字段随意取某个人的媒体 |
| `java/com/ruoyi/business/service/ResourceAccessService.java`、`resources/mapper/business/ResourceAccessMapper.xml` | 对照权限规则与回归；A优先专用上下文读图，不给通用目录放行、不增加逐次全表JSON扫描 |
| 拟新增 `java/com/ruoyi/business/{controller,service,mapper,domain}/` 中 QuestionImage相关类、媒体保存DTO及Mapper XML | 上传元数据/验证、上下文读图、事务绑定/暂存补偿，不建设新的平台级文件服务 |
| `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/common/CommonController.java` | 既有上传/读资源参照与回归；不全局改权限/上限 |
| `RuoYi-Vue/ruoyi-common/src/main/java/com/ruoyi/common/utils/file/FileUploadUtils.java` | 复用落盘工具；专用图片限制在业务层实现 |
| `RuoYi-Vue/ruoyi-framework/src/main/java/com/ruoyi/framework/config/SecurityConfig.java`、`security/filter/JwtAuthenticationTokenFilter.java` | 保持直接访问禁令；A用Blob不需新增Cookie通行；若改为直链须重新审查严格GET白名单 |
| `RuoYi-Vue/ruoyi-common/src/main/java/com/ruoyi/common/utils/poi/ExcelUtil.java`、`utils/file/ImageUtils.java` | 核查现成能力，不改全局通用导入，避免影响其他Excel业务 |
| 拟新增仓库 `sql/choice_question_media_v1.sql` 及前检/回退说明 | 评审通过后才编写；本轮不生成/执行SQL |

## 7. 前端交互建议

### 7.1 教师完整路径

进入题库→选择“新增选择题”→填文字→题干下点“添加图片”，四个选项文字区旁各有“添加图片”→立即显示上传中/失败重试/缩略图→可预览原图、更换、移除→“保存题目”→列表可识别图题→重新编辑/课程组卷仍能看到图。

- 文字输入与图片是同一内容区，不增加“文字题/图片题”互斥模式，不要求图文都填。新建题沿用默认年级/学期等既有表单，不加无用配置步骤。
- 上传中保存禁用并就近说明；上传失败保留其他字段/其他成功图片；上传成功标明“保存题目后生效”。进度只用真实上传字节进度，服务端处理用阶段文字。
- 更换先上传新图，成功再替换草稿引用；移除立即更新预览，但直到保存成功才解除正式引用；取消编辑不删除已绑定图片。
- 图注默认“题干图1/选项B图片”，可改为有意义说明，不强制重复填一遍；删除图后检查是否造成空选项，错误显示在对应位置。
- 提供学生样式只读预览，隐藏答案；保留教师自己的答案录入，不将“预览图片”变成另一次提交。
- 被使用含图题编辑时展示明确原因/另存入口；新题保存后留在可找到的新记录，不自动改旧课程。无权限不显示可兑现不了的更换/保存按钮。

### 7.2 学生完整路径

进入当前课/抽测→看到题干文字和原比例图片→点选 A—D→查看选中状态→提交→保留原得分/已提交反馈→普通错题本或允许的抽测回看中再次查看图片。

- 题干图在文字下按顺序展示，图片选项默认逐行，不把四张程序截图强挤成四列；宽屏可对小图采用两列，但每图需有足够宽度且不裁切。
- `max-width:100%`、高度自适应、`object-fit:contain`，保留宽高占位防跳动；不固定方形、不按文字行高挤图、不对代码截图自动有损压缩。
- **点击选项主体选中，点击图上的明确“放大”按钮打开原图；放大控件阻止冒泡，关闭后回到原位置。** 放大提供原比例/适应宽度、缩放和平移，手机可关闭，键盘可操作，焦点可返回。
- 字母始终可见；纯图片选项不能出现“未配置”。加载中有占位；失败显示“选项B图片加载失败/重试”，不静默消失，重试不重置已选答案。
- 关键图片未成功加载时提醒本题信息不完整、提供重试/联系教师，不伪报成功；不擅自给分、自动跳题或改变抽测计时/交卷规则。是否允许整卷提交带缺图告警需与现有强制截止规则一致，作为待验决策，不自行停表。
- 390/320窄屏实测，常规内容不横向滚动；只在原图放大视图允许必要的平移。选中状态同时用字母/标志和样式，不能只靠颜色。

## 8. 实施顺序、依赖与发布回退

本节是未来任务拆分，不是执行授权。

| 阶段 | 任务与依赖 | 可验收出口 |
| --- | --- | --- |
| P0 口径锁定 | 确认A/B、使用后保护、题干3/选项1、格式/大小、Excel首期范围；补少量真实流程图/代码截图样本 | 用户确认设计，不进入大范围重构 |
| P1 基线与迁移 | 重新核对工作区/本机及目标库结构、引用和原图目录；编写幂等增量/前后检/回退；备份数据库和文件清单 | NULL旧数据兼容，迁移重复执行安全，未动旧内容 |
| P2 图片闭环 | 专用上传元数据、格式/尺寸验证、草稿预览、鉴权Blob读取、绑定事务、TEMP清理补偿 | 上传→未保存预览→保存→刷新可见；越权/取消/失败可控 |
| P3 题库与历史保护 | DTO/Mapper/图文校验/未提交语义、编辑与绑定共同锁、另存新题、删除依赖、题库卡片/详情 | 新旧题均可用；已使用原题/分数不被覆盖 |
| P4 普通课 | 设计器候选/已选、教师预览、学生VO、学生理论/错题、学情明细 | 选题→发布/作答→评分→刷新/推进后错题回访全闭环 |
| P5 抽测 | 组卷媒体、开启个人快照、学生输出脱敏、快照读图/分析 | 改源题/可见性后旧个人卷仍显示原图，分值不变 |
| P6 数据交换 | 旧模板、可选图片引用列、校验/整批失败、含图导出不丢媒体 | 本站导出回导保留图文；不支持格式明确拒绝 |
| P7 验收与收口 | 先后端单测/前端测试构建，再接口/真实库、最后桌面/窄屏及机房样图 | 完成第9节关键路径；更新核心/专题/架构后才可申请发布 |
| P8 经授权发布 | 130目标前检、无考试写入窗口、库与文件备份、新release、数据库增量、后端重启、前端同步、真实角色验收 | 登记RELEASE_LOG和PUBLISHED平台更新，并用真实教师接口确认；本轮不做 |

P3依赖P2、P4/P5依赖P3；P6不能晚于功能对教师开放，否则旧导出可能损失图片。可以分模块开发，但首发不能只上线编辑器而漏学生/抽测端。

迁移要求：增加可空列和新表，不改列类型、不全量回填题干/答案、不更新旧快照；线上DDL锁时间和数据库版本需现场验证。备份必须记录哈希并验证可恢复。

发布顺序：先兼容迁移，再兼容后端，最后前端开放录图；最终需重启后端加载实体/Mapper。发布包不得混入当前教师工具等无关改动；不更改正式核心权限配置。

**回退不是删除新列**：含图题产生后，旧前端/旧后端不理解媒体，直接回旧版会把新题变成不完整题。建议保留媒体列/表/原图及兼容读路径，先关闭新建/编辑图片能力并停止向不兼容客户端投放含图题；未产生业务记录才可常规切回旧制品。已有在答/已答含图题时应采用兼容修复版，不自动清空媒体/恢复整库覆盖新成绩。删除列/表仅作为专用维护窗口的独立破坏性操作，需再次确认。

## 9. 风险与测试验收清单

### 9.1 风险排序

| 等级 | 风险 | 应对/必须验证 |
| --- | --- | --- |
| P0 | 普通课实时读题，改图改文让历史错题面错配 | 使用后内容保护/另存，课程引用与编辑共同锁；若要原地改则B |
| P0 | 只允许当前课资源，推进后本人错题图403；或只凭公开题/ID就跨用户读图 | 专用上下文授权，历史本人关系与抽测个人快照均验证 |
| P0 | 移除/删题时删掉共享原图，历史卷缺图 | 文件不可覆盖；RETAINED保留；只清TEMP，未来全引用治理另做 |
| P0 | 学生VO加入媒体时顺带把标准答案/教师参考材料下发 | 继续白名单DTO；普通课和抽测分别做响应字段断言 |
| P1 | 媒体只加主实体，课程VO/错题/统计/快照/手动对象复制漏带 | 按第6节逐项契约与真实页面核对 |
| P1 | 纯图选项被v-if/filter隐藏、误显示未配置 | 统一“有文字或有图”判定；A—D字母和选择行为保留 |
| P1 | 老客户端覆盖媒体、旧版本回退造成新题残缺 | 未提交与清空区别、内容保护、协同发布与兼容回退 |
| P1 | 存在多项图片时上传失败仍可保存半成品，草稿权限丢失 | per-slot状态、保存条件、事务绑定/补偿、就近错误与重试 |
| P1 | Excel导出丢图、嵌图导入绕过验证、外链引发服务器请求 | 同站引用校验；不静默降级；外链不抓取；嵌图独立安全适配 |
| P1 | 缩略图压缩导致代码不可辨认，原图过大拖慢整班 | 原图保留+点击放大；尺寸/像素限制、按可见区域加载，样图与机房复核 |
| P1 | 题库公开标记兼容值Y/1/0/N不一致影响媒体 | 复用并核对真实题目读权限，不能只复制一个"Y"分支推定所有公开值 |
| P2 | 放大误选答案、弹层关不掉、320px横向溢出 | 显式放大按钮/阻止冒泡、键盘和触摸验收 |
| P2 | 长期保留原图增长、数据库和文件备份不同步 | 容量告警/文件清单与哈希；后续有证据再做引用回收 |

### 9.2 后端与真实库

- [ ] 旧 NULL媒体题：列表/详情/组卷/学生VO/错题/学情与基线一致，旧题量、答案、分值不变。
- [ ] 新题：纯文字、题干1/3图、纯图片选项、四项图文混合；正确选项只有图也可保存；空答案/空必需项拒绝。
- [ ] 上传：合法PNG/JPEG、伪后缀/MIME、损坏图、超5MiB、超尺寸/像素、路径穿越、外链/Base64/SVG拒绝，错误不泄露绝对路径/凭据。
- [ ] 绑定：本人TEMP可绑定；他人私有/不存在ID拒绝；授权公开源题另存按明确规则复制；事务失败无半绑定。
- [ ] 更新：未传media保留、显式空清除（仅允许时）、换图失败保旧、改变题型/先删图不能绕过已使用保护。
- [ ] 并发：绑定课程与改图、抽测开启与改图、清TEMP与保存同时发生，不能产生题面漂移或已绑定文件被删。
- [ ] 授权矩阵：本人教师/其他教师/管理员/当前课学生/已推进有错题学生/无关联学生/抽测本人/另一考生/匿名，各有正负例。
- [ ] 普通课：正确A—D得原分、错误/空按原规则、大小写口径、重复提交唯一行、已提交/重做revision/关闭课程保护、随机抽题行为保持。
- [ ] 历史：另存不改变旧questionId/原题/原图/旧分；课程移除按现有归档规则，禁止迁分；旧纯文字历史限制如实保留。
- [ ] 抽测：开启复制media；已有4,187条本机快照不被迁移改写；旧NULL不回源补图；源题更改可见性后本人快照仍读原图；成绩/汇总不变。
- [ ] 学生普通/抽测响应不包含answer/analysis/教师私有参考字段；媒体接口不返回答案或服务器磁盘路径。
- [ ] 导入：旧模板/旧两选项规则、正确选项纯图、非法引用整批失败、导出回导、缺图明确失败、不支持嵌图明确提示。
- [ ] 迁移：前检、重复执行、后检、代码兼容回退、保留媒体数据；数据库与原图备份恢复演练。

优先复用 `StudentHomeControllerTest`、`StudentHomeControllerSecurityTest`、`GuideSheetStudentViewServiceTest`、`TheoryRedoServiceTest`、`ResourceAccessServiceTest`、`CountyExamServiceImplTest`、`CountyExamAnalysisServiceTest`、`QuestionWorkbenchMapperTest`，新增必要图文/上传/并发用例。测试位置 `RuoYi-Vue/ruoyi-business/src/test/java/com/ruoyi/business/`。不以Playwright替代Java业务测试。

### 9.3 前端与真实用户路径

- [ ] 教师新建→上传→放大→更换→移除→保存→刷新→从题库再次找到；失败不丢字段，上传中不假成功。
- [ ] 已使用含图题“另存为新题”路径明确，原课程不自动换题；无权限按钮与后端一致。
- [ ] 普通课/抽测设计器：候选→加入已选立即看图→保存重开不丢图；教师共享预览不漏纯图项。
- [ ] 学生题干及四选项显示、选择字母正确、放大不误选、已提交不可误改，刷新答案保持。
- [ ] 普通错题本图片与原题一致，推进后仍能读；练习不改变原正式成绩（按现有错题练习行为核对）。
- [ ] 学情明细/矩阵不把纯图项标未知；人数、正确率、等级及Excel成绩导出口径不变。
- [ ] 1440px桌面、390px与320px窄屏；横/竖/超宽代码截图、细字流程图、透明PNG；原图可辨认，容器不裁切。
- [ ] 慢网/404/403/500/会话过期：图片就近反馈与重试，选择不清空，不无限loading；退出/切换身份释放Blob缓存。
- [ ] 常见机房浏览器/网络至少抽样；若未做必须标“未实测”，不能只用模拟接口宣称真实整链通过。
- [ ] 浏览器自带打印检查图片不截断（不承诺新增打印功能）；新媒体不影响其他题型图片、材料、成绩导出。

前端先跑相关 `node --test` 和 `npm run build:prod` 既有门禁；后端定向 `mvn -pl ruoyi-business -am test` 按本机说明配置选定测试与fork参数。之后才做本机接口、真实数据库和浏览器；无性能改动不默认跑全平台压力测试，仅在整班图片请求出现疑问时专项测量。

## 10. 工作量评估、待确认项与交付边界

### 10.1 基于当前代码的规模判断

这是**中等规模、横跨多入口的功能**，不是单页小改：前端至少涉及题库、普通课组卷/预览、学生理论/错题、教师学情、抽测组卷/管理/学生等；后端存在多份显式VO/SQL/快照复制，另需媒体元数据与权限闭环。预计修改文件是数十个量级，主要成本在联动验证，不宜用“几个小时加上传框”估算。

| 工作包 | A相对工作量 | 主要不确定性 |
| --- | --- | --- |
| 数据/DTO/图文校验 | 小到中 | 旧请求字段存在性、历史两选项兼容 |
| 上传/草稿/权限/绑定/清理 | 中 | 不能直接复用通用上传归属；暂存与绑定并发 |
| 多入口前端与可读性 | 中到大 | 两套学生模板、两个设计器、统计与快照入口；真实样图可读性 |
| 使用后保护/另存 | 中 | 已有引用/归档和并发锁，需要用户接受编辑约束 |
| 抽测扩展 | 中 | 个人快照、开启事务、学生脱敏与不同数据源 |
| Excel同站引用往返 | 中 | 新可选列、校验与失败提示；不含离线图片包 |
| 回归/发布准备 | 中到大 | 真实师生链路、多端、历史和权限负例，不可省略 |

B显著多出版本实体/课程绑定、显示评分同源、历史基线迁移、引用关系治理与更大回归面，按“大于A一个明显层级”估算。没有做实现验证、确定历史策略和Excel范围前，不给精确人时；确认方案后按上述工作包排期，再用P2闭环实际进展校准。

### 10.2 审阅时需要确认的设计决策

| 决策 | 默认建议 | 不采用时的影响 |
| --- | --- | --- |
| A还是B | A | 要原地改题又保历史，则B并增加版本/迁移范围 |
| 已使用含图题编辑 | 另存新题；不自动换旧课/迁分 | 放开原地改会失去普通课历史保证 |
| 首期图量 | 题干最多3、每项1；题干仍有一句文字 | 多图/任意图文交错需要扩大交互与校验 |
| 格式与大小 | PNG/JPEG、5MiB、原图保留，尺寸限制以样图确认 | WebP/更大图要验证解码与容量，不能只调前端限制 |
| Excel范围 | 旧模板兼容+同站图片引用往返，离线包/嵌图后续 | 如首期要离线带图，应单独纳入任务/测试 |
| 物理删除 | TEMP超期回收，RETAINED首期保留 | 自动回收要做全引用索引与恢复流程，不能直接delete |
| 缺图时提交 | 显示缺图与重试，保持原抽测计时/强制交卷 | 若要求阻止提交/暂停计时，会改变业务规则，须专项确认 |

### 10.3 本轮交付

仅交付本技术方案。今天最应先确定的三件事是：**历史编辑策略、首期图片边界、Excel是否需要离线带图**。这些决定范围和成本，优先于选上传框样式。

本轮没有改变业务事实、数据库或部署状态，因此不将建议写成 `PROJECT_CORE.md` 已完成功能，也不生成正式更新版本。实施经审阅确认后，再建立专题需求/设计/任务并同步受影响架构资料；开发、测试和上线是后续独立步骤。
