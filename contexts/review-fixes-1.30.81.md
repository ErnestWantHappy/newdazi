# 1.30.81 答案泄露、教材越权与课堂并发修复验收

> 发布：2026-11-08 23:23:47 +08:00，130 正式；更新记录 **159 / 1.30.81 / PUBLISHED**。本文只记录已验证事实与明确未验边界。

## 1. 发布制品与运行状态

| 项目 | 值 |
| --- | --- |
| Release | `/data/apps/xueyeceping/releases/20261108_review_fixes_v1` |
| 后端 JAR SHA-256 | `653e603497cde1852fe52a971e52a5459036bdf1ff14836cf2d65ce8666dc906` |
| 前端首页 SHA-256 | `05d698804d74016551eedee63ec7ecc0f865e70cbbb54ccbd2e9d482f7f2e94c` |
| 线上前端文件 | 1,213（合并旧静态资源，避免旧懒加载分块 404） |
| MainPID / NRestarts | `3012796` / `0` |
| 服务 | `xueyeceping-130`、`nginx`、`aiot-lamp` 均 active |
| 整库备份 | `/data/backups/xueyeceping/20261108_232251_20261108_review_fixes_v1/database.sql.gz` |
| 备份大小 / SHA-256 | 70,854,452B / `774464dcd89a91881a581ece8d0d6281359fa11af7383fc08b271a43d7be6e90` |
| 发布结果 | `output/release-artifacts/20261108_review_fixes_v1/deploy-result.json` |
| 接口探针 | 同目录 `probe-after.json` |
| 浏览器验收 | `output/playwright/review-fixes-20261108/acceptance.json` |

## 2. 修复内容

1. **题库 Python 参考答案**：`BizQuestionController` 列表和详情接入 `QuestionReferenceAnswerPolicy`。非创建者、非管理员的普通教师查看他人 `practical + PYTHON` 题时，`answer` 与 `analysis` 不下发；创建者、管理员保留完整值。选择题、判断题、课程、考试和成绩分析查询语义未改。
2. **学生教材访问**：新增 `TextbookStudentAccessService`，`toc/pages/cover/page` 四接口校验学生档案、当前班级课程、课程学校与 `bookId`；教师/教研员/管理员保留整册阅读。书架、PDF、教学资源权限不变。
3. **教师 Python 运行容量**：`PracticalPythonRunService` 并发槽位 `2 → 8`，增加 3 秒排队；仅成功取得槽位才释放。该入口仍不写提交、成绩或判题记录。
4. **前端体验**：一年级指南明确“暂未上架”空态；操作题四模式计数改 `Promise.allSettled`，失败模式可单项“重新获取”；教学设计汇编关闭重开恢复页码；教师工具去掉重复说明并修复 320px 收藏按钮挤压标题；成绩页修正 Vue import 顺序。

## 3. 发布前后真实接口对比

发布前在 130 真实复现：普通教师可读取他人题目 `991538` 的完整参考答案（333 字符）；已绑定教材 1 的学生可读取教材 7；无绑定学生可读取教材 1。

| 探针 | 发布前 | 发布后 |
| --- | --- | --- |
| 普通教师题目详情 | 可读完整答案 | `hasAnswer=false`，答案长度 0 |
| 普通教师题目列表目标项 | 可读完整答案 | `hasAnswer=false` |
| 管理员题目详情 | 完整答案 | `hasAnswer=true`，答案长度 333 |
| 绑定学生读教材 1 | 成功 | 成功 |
| 绑定学生读教材 7 | 成功（越权） | 业务失败：“仅可阅读当前课程绑定的教材” |
| 无绑定学生读教材 1 | 成功（越权） | 业务失败：“仅可阅读当前课程绑定的教材” |
| 教师读教材 1 / 7 | 成功 | 均成功 |

## 4. 浏览器验收（7/7）

`browser_acceptance_v2.py` 在真实 130 环境通过，汇总 0 `pageErrors`、0 5xx：

- 普通教师 Python 预览只显示“仅题目创建者或管理员可查看参考答案”。
- 一年级指南显示“1 年级指南暂未上架”，不伪装成网络错误。
- 成绩页正常加载，1440/390/320 无横向溢出。
- 教师工具 1440/390/320 无横向溢出；320px 收藏按钮 26px，标题区 176px 未被挤压；“压缩图片”搜索 2 条结果且无重复全量区。
- 操作题流程图计数请求人为中断后显示“题量未知/重新获取”，点击后恢复“13 题”。
- 学生首页与绑定教材阅读页正常。
- 汇编共 51 页，第 2 页关闭重开后仍为第 2 页。

## 5. 构建与测试

- 隔离发布源后端：925 项通过，3 项既有跳过；本轮定向 18 项通过。
- 前端：登录 7 项、机位 9 项、题库工作台 25 项通过；Vite 生产构建与 `verify_frontend_release.mjs` 门禁通过（入口 gzip 561,361B）。
- 1.30.81 业务 JAR 递归扫描 `media_json`：0。
- 主工作区已精准同步本轮最小修复，未整文件覆盖未上线的选择题图片/Excel图文导入；同步后后端定向 18 项、业务模块全量 955 项（3 项既有跳过）、前端 7/9/25 项与生产构建再次通过。

## 6. 产品更新与幂等

真实教师 `/business/platform-update/list` 返回 HTTP 200、业务码 200，总数 135，目标 `updateId=159`、`versionNo=1.30.81`、`status=PUBLISHED`。`sql/platform_update_1_30_81_20261108.sql` 幂等复跑后仍只有一条 1.30.81 记录。验证脚本：`output/release-artifacts/20261108_review_fixes_v1/verify_product_update_api.py`。

## 7. 未验证与风险

- 未做 45 人真实整班 Python 并发压测；8 槽位与 3 秒排队是保守扩容，仍需课堂窗口观察。
- 未在老旧机房浏览器实机复验；1.30.75 的服务端降级链保持，但本轮未重测 Chrome 49/旧内核。
- 选择题图片/Excel 图文导入仍未上线；130 未迁移 `media_json`。任何后续整包发布前必须先执行 `sql/choice_question_media_v1.sql` 或继续使用隔离制品。
- 教材越权拒绝沿用 `ServiceException` 业务码 500，不是 HTTP 403；前端按业务失败提示。若要统一 403，需单改全局异常映射并回归全部业务码。
- 发布日志曾出现 `Broken pipe` 与已知设备心跳/跨教材拦截 WARN，人工复核无启动异常、后端崩溃或持续 5xx；粗略 `ERROR|Exception` 统计脚本因此退出 1，不作为发布失败。
