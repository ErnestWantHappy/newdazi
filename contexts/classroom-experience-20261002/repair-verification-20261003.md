# 课堂体验审查修复验收（2026-10-03）

> 后续状态：2026-10-03已正式发布130/1.30.24，真实API13组与浏览器9组通过、更新103教师可见。以 [正式验收](release-verification-20261003.md) 为当前状态；下文未发布描述保留为本机阶段历史。

状态：本机修复候选 v4.01，未正式发布。承接 [修复方案](./review-repair-plan-20261003.md) 的 R1～R8，并处理用户后补四项和两项机位小问题。保留工作区原有修改，没有提交、推送或连接正式服务器。

## 已修改的行为与主要文件

以下路径相对仓库根目录。

| 项目 | 修复结果 | 主要文件 |
| --- | --- | --- |
| R1 本机草稿 | 键含登录用户、课程、题目，内容再验 owner；不自动迁移无归属旧键；加载/保存/提交/定时器均绑定身份和弹窗实例 | `RuoYi-Vue3/src/components/FlowchartEditor/{localDraft.js,StudentFlowchartDialog.vue}` |
| R2 删除未批作品 | 同事务清空答案、提交时间 NULL、当前作品指针，专用接口将任务状态退回 WORKING 并在提交后广播；普通学生状态上报仍不能把 SUBMITTED 降级 | `PracticalArtifactService.java`、`ClassroomTaskStateService.java`、`ClassroomTaskStateWriter.java`、`BizStudentAnswerMapper.java/XML`、`BizStudentTaskStateMapper.java/XML`、`ClassGroupingMapper.xml` |
| R3 流程图只读 | 显示正式提交 JSON，不以草稿代替；只读加载不覆盖本机未同步草稿 | `StudentFlowchartDialog.vue` |
| R4 切班 | 请求序号与班级课程键隔离结果；切班立即发新请求并清理旧班显示；开关读写和卸载同样隔离 | `RuoYi-Vue3/src/views/business/classroomDesktop/index.vue` |
| R5 连续上传 | 受控文件列表按成功批次 UID 清理，失败留待重试；切课中断上传并使迟到响应、预览轮询失效 | `RuoYi-Vue3/src/views/student/index.vue` |
| R6 注册码 | 加载、保存、生成、撤销均捕获学校和请求代次；切校或机房机位变化清码；撤销确认及请求期间防重 | `RuoYi-Vue3/src/views/business/seatHelper/index.vue` |
| R7 网页续期 | 30 秒续期移到学生布局，首页组件仅展示；换身份/退出/卸载终止旧轮询，退出清理未消费凭证 | `layout/StudentLayout.vue`、`components/SeatHelper/{StudentSeatHelper.vue,studentSession.js}`、`utils/seatHelper.js`、`store/modules/user.js` |
| R8 Windows 入口 | 服务失败时用已校验普通网页根地址降级；损坏配置可重新配置；托盘与 `--configure` 提供管理员入口；注册响应增加公开 platformUrl | `clients/seat-helper/{SeatHelper.cs,build.ps1,README.md}`、`SeatHelperService.java` |
| R9 空作品仍待批 | 反馈返回 submitted=false/WORKING；成绩汇总不再将空文件占位当提交，页面及导出显示未提交、均分不计入该空记录；0 分、独立课堂表现和人工修正保持原规则 | `StudentFeedbackService.java`、`BizStudentAnswerMapper.xml`、`ScoreQueryController.java`、`views/business/score/index.vue`、`StudentFeedbackDialog/index.vue` |
| R10 反馈刷新 | 刷新明确重新加载课程列表再取反馈，点击事件不作为 silent；关闭和旧响应隔离 | `RuoYi-Vue3/src/components/StudentFeedbackDialog/index.vue` |
| R11 包大小 | 学生上传/作品、教师批改、个人反馈显示文件大小，未知值明确标记，不假装为 0 | `utils/practicalFileDisplay.js`、学生首页、教师 `grading.vue`、反馈弹窗 |
| R12 文件名 | 前端和文件策略均按落盘工具现有 100 字符上限（含扩展名）提前拒绝，并提示缩短文件名 | `PracticalFilePolicyService.java`、`utils/practicalFileDisplay.js`、学生首页 |

后端服务/Mapper路径均位于 `RuoYi-Vue/ruoyi-business/src/main/`。没有更换技术栈、权限模型、增加依赖或改动旧 Vue2。

## 本轮实际验证

- Java 定向 39 项全部通过，0 失败、0 错误、0 跳过。包括删除锁定/0分保护、删除状态回滚、普通状态不能撤回提交、流程图访问、文件名100/101边界、新格式、反馈空行与0分、反馈快照、成绩汇总和真实 Excel 导出单元格/均分。日志：`output/review-repair-20261003-java.log`。
- `PracticalWithdrawalMysqlTest` 在隔离库 `xueyeceping_classroom_accept_20261002` 使用随机前缀的四张测试表执行真实 Mapper SQL，验证 NULL 清空、状态版本递增、删除前后统计、单人/批量成绩汇总、0分及回滚。结束在 finally 删除本测试创建的表；未更新源业务表。测试仅在显式提供隔离库连接环境变量时运行。
- Node 14 项全部通过，执行实际组件脚本验证账号隔离、只读正式版、旧响应丢弃、连续三批上传与失败保留、切校切回、布局续期、刷新失败恢复、文件大小和撤销防重。日志：`output/review-repair-20261003-node.log`。
- Chromium 8 组通过，页面脚本错误 0。是真实 Vue 页面与 Element Plus 组件、受控模拟接口：101字符文件名上传前拒绝；反馈列表失败直接刷新及未提交/0分/2 KiB压缩包显示；上限1连续上传3版；换账号草稿；只读正式画布；同标签页进入 Python 后模拟120秒仍续期；实际路由切班和浏览器前进返回；教研员布局冒烟。结果：`output/review-repair-20261003-browser.json`，截图：`output/playwright/review-repair-20261003/`。不是本轮真实师生 API 全链路验收。
- Windows 客户端 7 项通过：学校关闭、设备撤销、HTTP503、接口不可达的普通网页地址选择、损坏配置恢复及独立地址、非法文件地址拒绝。`clients/seat-helper/tests/run.ps1` 编译并执行实际源码；不启动真实浏览器、不修改自启、不注册真实设备。
- 后端 `mvn -pl ruoyi-admin -am clean package -DskipTests` 与 Vue3 `npm run build:prod` 均已通过；单测单独执行，不把跳过测试打包算成测试通过。日志分别为 `output/review-repair-20261003-package.log`、`output/review-repair-20261003-vue-build.log`。
- 本轮没有重跑上一轮 665 项全量 Java 和真实接口验收；2026-10-02 的证据仍是历史，不应混作本轮新增验证。

## 客户端与发布要求

客户端修复版 `0.1.1-pilot`：22,528 bytes，SHA-256 `486FA7AA30DE09EBA564BAE63E5341D29DE3274199A7276994F64D87C0204B65`，见 `clients/seat-helper/dist/manifest.json`。仍未签名。独立 `platform-url.txt` 只存公开站点根地址，不存关联 token 或设备密钥。旧配置必须成功打开一次建立缓存，或由管理员补填可靠地址；没有已知地址时不能保证降级打开。

本轮无新增 SQL 或配置开关。部署需配套更新前端、后端并重启后端；如启用助手，还需分发新版 EXE。整个课堂体验专题原有四份增量 SQL 仍必须按目标库状态前检、备份后执行，不能因本轮无新 SQL 而跳过。未修改本机真实业务库或正式库，未生成产品发布记录。

回退可恢复原前后端制品和客户端，保留作品/版本/反馈数据，无本轮表结构回滚。回退会重新暴露本报告修复的行为；不得通过覆盖业务库备份回退期间新成绩。旧无归属浏览器草稿保留但不自动恢复，以免再次串账号。

## 剩余风险与下一步

1. 发布前用目标环境师生身份验证“删除→首页/大屏/批改/反馈/成绩/导出→重交”，以及真实慢网切班/切校；本轮浏览器接口为模拟，不能替代该验收。
2. 历史空答案统计已防御，但此前残留的任务状态行未批量治理；发布前只读审计，再按可回滚方案处理确证旧数据。
3. Windows 10、极域、还原卡、自启与 DPAPI 密钥、公开网页地址缓存保留仍待真实机房验收；独立接口故障降级不代表后端全部停机时网页也能使用。
4. 单课多操作题、所有浏览器、真实并发压力未在本轮扩展回归。现有未批可改、0分锁定、退回保分和当前课限制保持。
