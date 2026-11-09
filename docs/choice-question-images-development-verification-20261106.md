# 选择题图片支持：开发与本机验收

2026-11-06。用户已确认增强方案 A 并授权开发。**本机已实现，未部署、未提交或推送 Git。** 本文记录实现结果；原 v2 设计不再代表“尚未开发”。

> 2026-11-09 v3 本机追加：Excel 新模板增加7个图片专用列，旧模板继续兼容；图片专用列按左上角锚点所在列归属，跨题行进入待确认，导出落回专用列。专项16项（reader 14、writer 2）、关联35项、业务模块全量964项（0失败、3跳过）及前端生产构建/发布检查已通过；改动未部署，正式1.30.82仍为v2实现。真实WPS/Excel变体未验。[ADR-002](../contexts/choice-question-media/ADR-002-excel-image-columns-and-anchor-ownership.md)

## 1. 教师和学生现在如何使用

- 网页录题保持题干与 A—D 输入框。题干最多 3 图、每选项 1 图；图片可点击上传、粘贴截图、拖入，自动上传并预览，可放大、更换、移除。单图 PNG/JPG ≤5MiB，最长边 ≤8192，≤2000万像素。没有新增图片地址、ID、图注输入。
- 从已打开的编辑页计算，题目文字/答案已填好时，**粘贴一张截图 → 保存**，最少 2 个动作；纯文字题沿用原流程。完整新题仍需填写题干/选项/答案与原有必填分类，不能把这些输入说成只有 2 步。
- Excel：**选择一个文件 → 自动识别并预览 → 确认导入**。50 题/76 图片实例已真实一次识别；正常题无需逐图操作。异常可在预览中修正文字、替换图片、确认归属或明确排除单题。沿用原模板，可下载原模板，不上传图片包。
- 已用于课程、抽测或历史的含图题，正常编辑保存会自动创建新题，原课程不换题。提示“已保留历史版本”，题库顶部可以直接查看/继续编辑新题；保存响应丢失时按回执找回，不重复创建。**普通纯文字旧题的编辑规则保持。**
- 学生仍选 A/B/C/D，图片放大不改变答案，提交、锁定、评分、刷新恢复沿用原链路。

## 2. 实际页面

[五张真实页面截图统一入口](choice-question-images-ui/implemented/index.html)，可本地浏览器打开；不同于前期静态原型。分别为录题、题库、Excel 预览、学生作答、课程组卷。图片为独立测试数据，测试记录已清理。

窄屏证据保存在 `output/choice-media/real-edit-{390,320}.png`、`real-import-preview-{390,320}.png`、`real-student-saved-{390,320}.png`、`real-lesson-preview-{390,320}.png` 和 `real-exam-preview-{390,320}.png`。已检查弹窗边界，补齐了选择题编辑弹窗固定宽度导致右侧裁切的问题；学生顶部操作与分数说明在窄屏换行。

本轮独立验收地址：`http://127.0.0.1:4178/question`，静态生产构建代理本机后端 **18080**；原 8080 服务没有被替换。该入口用于本机审阅，非线上发布。普通开发入口若仍连接旧后端，需要按项目启动流程同步新版后端。

## 3. 模块与代码清单

以下路径均相对仓库根目录，旧 Vue2 未修改。

| 模块 | 实际文件/目录 | 变化 |
| --- | --- | --- |
| 增量库表 | `sql/choice_question_media_v1.sql`、`sql/choice_question_media_v1_rollback.md` | 题库和个人抽测快照各加 nullable `media_json`；新增图片、保存回执、导入批次/明细四表 |
| 数据结构 | `RuoYi-Vue/ruoyi-business/src/main/java/com/ruoyi/business/domain/{BizQuestion,QuestionMedia,CountyExamPaperQuestion}.java`；`domain/vo/{BizLessonQuestionDetailVo,StudentLessonQuestionVo,QuestionAnalysisDetailVo}.java` | 媒体独立透传；学生普通课 VO 不泄露答案；媒体不代替文字/答案列 |
| 图片与保存 | 同业务 Java 根目录 `service/{QuestionMediaStore,QuestionImageService,QuestionImageCleanupJob,QuestionSaveService}.java`、`utils/QuestionMediaCodec.java` | 文件校验、题目/课/试卷/批次鉴权、绑定、七天 TEMP 清理、指纹与行锁、自动另存、幂等回执与结果恢复 |
| Excel | 同根目录 `service/{QuestionWorkbookReader,QuestionImportService,QuestionWorkbookWriter}.java` | POI 4.1.2 专用图文读取/导出；持久化预览、在线修正、revision、原子确认、防重复与回导；不改通用 ExcelUtil |
| API | 同根目录 `controller/{QuestionImageController,QuestionImportController,BizQuestionController,ScoreQueryController}.java` | 上传/鉴权读图；导入批次 GET/POST/PATCH；保存最终 ID/回执；图文导出和学情透传。旧 `/importData` 遇图片明确拒绝并引导预览，纯文字仍走原逻辑 |
| 课程/抽测/错题 | 同根目录 `service/GuideSheetStudentViewService.java`、`service/impl/{BizQuestionServiceImpl,BizLessonServiceImpl,CountyExamServiceImpl}.java`；相关 Question/LessonQuestion/StudentAnswer/CountyExamQuestion/CountyExamPaperQuestion Mapper Java/XML | 媒体复制到学生 VO/个人卷；关联课程或考试与编辑共同锁题；含图依赖防删；错题查询媒体；原成绩与 A—D 判分不变 |
| 通用前端 | `RuoYi-Vue3/src/components/QuestionMedia/`、`src/utils/questionMedia.js`、`src/api/business/questionImages.js` | 一个组件负责 Blob 生命周期，一个负责展示，上传组件负责三种入口与失败阻断；编辑和预览共享媒体结构 |
| 教师录题/导入/题库 | `src/views/business/question/index.vue`、`components/{QuestionImportPreview,QuestionCard}.vue`、`src/utils/questionWorkbench.js` | 轻量入口、纯图片校验、结果定位/恢复、持久化导入预览、含图标识；非选择题继续原分步保存 |
| 组卷 | `src/views/business/lesson/designer.vue`、`src/views/business/countyExam/{examDesigner,index}.vue` | 已选/候选媒体透传、完整预览、抽测真实引用上下文；分值、排序与组卷接口保持 |
| 学生/教师回看 | `src/views/student/{index,countyExam}.vue`、`src/views/business/{student/home,teacher/index,score/index}.vue`、`score/components/AnalysisMatrix.vue` | 普通课、抽测、错题、旧学生页、教师共享预览和学情图文显示；图片加载失败可重试 |
| 测试/运维资料 | `ruoyi-business/src/test/java/.../service/Question{MediaCodec,SaveService,WorkbookReader}Test.java`、`src/utils/__tests__/questionMedia.test.js`、`scripts/choice_media/`、`.gitignore` | 黄金样本、真实 API/浏览器脚本、数据库保护与测试数据精确清理，私有图片目录不入 Git |

未新增选择题 AI 生成、完整试卷 PDF/Word 或打印业务：现有代码没有相应完整选择题入口，不为本任务额外建立。

## 4. 数据与历史保护

```json
{"schemaVersion":1,"stem":[{"imageId":"123"}],"options":{"A":[],"B":[{"imageId":"124"}],"C":[],"D":[]}}
```

`media_json=NULL` 返回空媒体对象；旧数据不回填。图片原文件不可覆盖，换图新建 ID。已绑定 `RETAINED` 原图不自动删除，未绑定 `TEMP` 超过七天由每日 03:15 任务分批清理。预览七天过期；已确认结果保留。普通课程仍引用原题，个人抽测复制媒体与文字；旧 NULL 快照不回源补今天的图片。

文件根目录为 **当前 `ruoyi.profile` 的同级 `<profile目录名>-question-images`**，本机为 `uploadPath-question-images/`，不在公开 `/profile` 映射之内；数据库只保存受控相对路径。复用现有 profile 配置、文件系统、Spring 身份/角色与业务范围，不引入对象存储、富文本编辑器或新依赖。访问通过登录态 Blob 请求，URL 不放 Token。

保存与建题/回执同事务，先锁题再查使用关系；课程保存/抽测组卷也锁题。有使用记录的含图内容改变（包括旧纯文字题加图）自动另存，必须持有打开编辑时的指纹，陈旧窗口拒绝覆盖。导入按批次加锁、revision 检查，全量验证后在同一事务写题/绑图/写结果；提交中途失败全部回滚。Python/文件作品等不使用选择题回执键，保留原有分步保存。

## 5. 验证证据

| 项目 | 实测结果 |
| --- | --- |
| 后端 | `mvn -o -pl ruoyi-business -am test -DforkCount=0`：933 项、0 失败/错误、3 跳过；其中新增媒体/保存/Excel 定向 22 项。clean package 成功，真实 JAR 18080 启动成功 |
| 前端 | `npm run build:prod` 及登录 7/机位 9/工作台 24 门禁通过；媒体工具 3 项通过。全仓 Node 检查 220 通过、1 个 Python 测试文件加载失败：其他在研修改的 `interactiveRunner.js` 引入 `@/api`，裸 Node 无别名解析；未越界修改该模块，不能称全仓前端全绿 |
| 真实 API/MySQL | 11 组：建题回执重放、受控/匿名读图、历史另存/防删、纯图片、50题76图片一次识别、第二题图片失效后整批0半成品、并发确认只落50题、重复文件找回、图文导出再导入 |
| 教师真实浏览器 | 点击上传、更换、粘贴、拖拽、删除、放大；编辑自动另存；故意丢失真实保存响应后按回执找回；Excel 50题在线改字改图→整批确认→找回。桌面/390/320，0 页面异常 |
| 学生真实浏览器与评分 | 独立新建验收学生/班级/课，不改原账号/指派。3题同时含题干图、四个图文选项、旧纯文字；放大不选答案，点击/键盘选题；真实提交 B/B/C 后 MySQL 得10/0/10分；刷新锁定、错题媒体保留；桌面/390/320 |
| 课程/抽测 | 教师普通课和教研员抽测组卷真实图文预览通过；学生个人快照图片可读、伪造考试 ID/脱离课程上下文不能读私有题；教研员按真实考试引用可预览 |
| 数据保护 | 迁移前备份 SHA-256 `c3e32c6c5dcb20b68be919ed13d82c10de5916e68f87de42ad1777850464b432`，3,839,805B。验收后逐行与该备份核对：原1,738题、4,187个人卷快照完全一致；新增测试题/课/抽测/学生/成绩与文件已精确清理，新四表恢复0行 |

本机证据：`output/choice-media/{test-summary,api-result,browser-result,student-browser-result,student-api-result,designers-browser-result,cleanup-result}.json`，构建与全测日志同目录。备份 `output/choice-media/backups/20261106-155601/before.sql`；不将私密登录态纳入文档或 Git。

## 6. 已知边界、风险与下一步

1. **最大未验风险为真实 Excel/WPS 图片变体。** 已测 POI 生成的 xlsx/xls、同格文字+图片、跨行/合并、公式异常、50题/76图片和导出回导；未用教师实际 Excel/WPS 多版本文件证明所有内嵌格式。`cellimages`/richData/公式图检测后明确人工核对，在线粘贴其可见截图；裁剪/旋转图不直接暴露被裁掉内容，要求截图替换。网络 IMAGE 不远程抓取。真实样本仍需上线前验收。
2. 首期单批≤200题、≤1000图片实例、Excel≤50MiB、展开 ZIP≤128MiB/4000条目；后端同时最多2个解析。图文导出≤200题/100MiB。机房整班并发、慢网长任务与超大文件峰值内存尚未压力实测；不保证所有50题大图文件都小于50MiB。
3. 七天定时清理和长期磁盘增长尚未跨七天实测；保留图不自动回收，以保护历史。全库图片引用治理应另行设计，不在本轮删除历史资源。
4. 生产未连接、未迁移、未部署。发布须独立备份目标库与图片目录，核对 profile 是跨 release 的持久绝对路径、同级目录可写且无公开 Nginx 映射，再执行增量 SQL，前后端同步发布并重启后端。旧前端无法显示图片，不能只换后端就宣称兼容发布；历史含图题存在时也不能直接回退到不识图的程序。
5. 工作区保留其他教师工具/Python 在研修改，当前本机构建包含它们；**不能直接拿整包发布**。正式制品应隔离无关修改并补真实样本/目标环境验收，得到部署授权后再发布。

分阶段状态：数据与受控上传 → 安全保存与媒体透传 → Excel 预览/确认/导出 → 师生页面与真实回归 **已完成本机验证**；教师实际文件、机房压力、生产存储/发布与上线观察 **尚待专项验收/授权**。
