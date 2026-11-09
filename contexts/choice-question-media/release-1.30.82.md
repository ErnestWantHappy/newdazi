# 1.30.82 选择题图片与 Excel 图文导入正式验收

日期：2026-11-09  
环境：130 正式站（Linux / systemd / Nginx）  
状态：**已发布、已迁移、已验证、测试数据已清理**

## 发布制品

- Release：`/data/apps/xueyeceping/releases/20261109_choice_media_v1`
- 后端 JAR：`ruoyi-admin.jar`，SHA-256 `2b9c65e9273a324ba42d5aec0f97f942aa81ada3f5bb0dd5ce9c3e1fa7d5161d`
- 前端首页：SHA-256 `8e2d045be4427cc598acca22d5214bfe8f0e67c6487a7c92d7e641788bce0cf3`
- 前端包：`frontend.tar.gz`，SHA-256 `a6460f6697670a186723427d02f89e20309212625e35f301981d6b2f85861905`
- 迁移 SQL：`sql/choice_question_media_v1.sql`，SHA-256 `cd7ac8b2aab5df20bfa65369b0ef906de97aed92f5d234fc632f81afe752849e`
- 更新登记 SQL：`sql/platform_update_1_30_82_20261109.sql`，SHA-256 `774a3acaaba0fb7cbf8ac4cd67bebc2138eeebfc0a2ced33e103a41c9daa0f3a`
- 上一版：`releases/20261108_review_fixes_v1` 保留。

运行时：后端 PID `3019157`，三服务 active，NRestarts 0，启动 0 ERROR；切换后首页与 `/prod-api/captchaImage` 均 200。

## 数据库迁移

- 整库备份：`/data/backups/xueyeceping/20261109_071529_20261109_choice_media_v1/database.sql.gz`
- 备份大小：70,566,942 字节
- 备份 SHA-256：`464ac8a37b0c4edc4886af96d730c4e64c0ab7b327f5ff0415afe00e9cd34966`
- `biz_question.media_json`、`biz_county_exam_paper_question.media_json`：可空 TEXT
- 新表：`biz_question_image`、`biz_question_save_receipt`、`biz_question_import_batch`、`biz_question_import_item`
- 唯一索引：4；新表列：42
- 私有原图目录：`/data/upload-question-images`，`admin2:admin2 750`

迁移前后业务数量不变：

| 数据 | 迁移前 | 迁移后 |
| --- | ---: | ---: |
| 题库题目 | 4792 | 4792 |
| 抽测题 | 4187 | 4187 |
| 四张新媒体表 | 0 | 0 |

不回填旧题媒体，不修改既有答案、成绩或课程关联。

## 功能边界

- 题干最多 3 图，每个选项最多 1 图；支持上传、粘贴和拖拽截图。
- 题库列表显示“含图”，教师详情可放大；学生当前课程可显示题干图与选项图。
- Excel 保留原模板，支持单元格内嵌图识别、预览修正、复核和整批原子确认。
- 已引用含图题发生实质修改时自动另存，原课程/历史答案不换题；保存回执可定位真实题目，防止重复建题。
- 普通课、错题、教师学情和个人抽测透传媒体；A—D 单选评分仍只使用文字答案，媒体不参与判分。
- 图片只经登录态业务接口读取，校验题目/课程/考试上下文；不使用公开 `/profile` 或 URL Token。
- 学生课程 VO 不返回标准答案，只回显本人选项与得分；因此页面不显示标准答案对错徽标。

## 正式 API 验收

9 项全部通过：

1. 教师上传两张真实 PNG，返回 TEMP 私有图片。
2. 创建同时含题干图与选项 B 图的选择题。
3. 同一 `saveRequestId` 重放不重复建题。
4. 保存回执定位原题。
5. 教师按题目凭证读取图片，内容哈希一致。
6. 匿名读取返回 HTTP 200 + 业务 `code=401`。
7. 题目挂入学生当前课程后，学生收到 `media`；本课凭证可读，错课凭证拒绝。
8. 学生真实选择 B 提交，答案、10 分和 `is_correct=1` 落库并回显。
9. 真实 xlsx 识别 1 题 2 图；预览重放不重复建批次；修正、复核、确认后题库出现含图题；重复文件定位原批次；图片全部转 RETAINED。

结果文件：`output/release-artifacts/20261109_choice_media_v1/acceptance-api-result.json`。

## 浏览器验收

9 组通过，0 页面脚本异常、0 个 5xx：

- 教师题库搜索到两道验收题，均有“含图/图片”标记。
- 教师详情加载题干图和选项 B 图，点击可放大。
- 教师新增对话框真实上传 PNG，缩略图加载。
- Excel 图文导入真实 xlsx 预览 1 题 2 图，缩略图加载。
- 教师题库 390px、320px 无横向溢出。
- 学生当前课程显示题干图与选项 B 图，已选 B、总 10 分，标准答案未下发。
- 学生 390px、320px 无横向溢出。

截图：`output/playwright/choice-media-20261109/`。结果文件：`output/release-artifacts/20261109_choice_media_v1/acceptance-browser-result.json`。

## 构建与测试

- 后端全量测试：951 通过，0 失败，3 跳过。
- 前端图片专项：5 项通过。
- `npm run build:prod` 与 41 项构建门禁通过。
- 生产 JAR 已确认包含 `QuestionImageController`、`QuestionImportController`、`QuestionMedia`、`QuestionMediaStore`、`QuestionSaveService`、`QuestionWorkbookReader` 等类。

## 验收数据清理

按清单精确删除：

- 手工含图题：991673
- Excel 导入含图题：991674
- 临时课程关联：14696
- 学生答案：823815
- 导入批次：1、3、4、5
- 图片：3—12，共 10 张

清理后：题库 4792、抽测 4187、课程关联 5258、学生答案 632463，四张媒体表 0 行，`/data/upload-question-images` 为空。未删除课程 990913、班级指派 8694 或其他正式数据。

结果文件：`output/release-artifacts/20261109_choice_media_v1/acceptance-cleanup-result.json`、`acceptance-manifest.json`。

## 产品更新与回滚

- 产品更新：`update_id=160`，`1.30.82`，`PUBLISHED`
- 标题：选择题图片与 Excel 图文导入
- 真实教师 `/business/platform-update/list` 可见，HTTP 200
- 登记结果：`output/release-artifacts/20261109_choice_media_v1/release-registration-result.json`

应用回退：确认当前仍为该 release 后，将 unit 的 JAR 与 Nginx root 切回 `20261108_review_fixes_v1`，`daemon-reload`、重启后端、`nginx -t` 后 reload，并把更新 160 转 DRAFT。空列/空表可保留，不整库回灌备份。

**已有正式含图题时不得直接回退到不识图旧程序**：必须先保留 `media_json`、四张媒体表、`/data/upload-question-images` 原图，或在当前版本做局部热修。

## 上线后终检

2026-11-09 07:43:52 只读终检通过：

- 线上 JAR 与首页哈希均与发布制品一致；HTTP 首页哈希也一致。
- MainPID `3019157`，三服务 active，NRestarts 0；启动后错误日志 0 行。
- 后端 3009 验证码、Nginx `/prod-api/captchaImage`、站点首页均 200。
- 题库 4792、抽测 4187、课程关联 5258、学生答案 632463。
- 四张媒体表均 0，媒体列 2、媒体表 4，验收测试题残留 0。
- `/data/upload-question-images` 仍是 `admin2:admin2 750` 空目录。
- 教师 `/getInfo`、题库列表、更新列表均 HTTP 200 / 业务 200；更新 160 为 PUBLISHED。
- 匿名图片读取仍为 HTTP 200 + 业务 `code=401`。

结果文件：`output/release-artifacts/20261109_choice_media_v1/final-regression-result.json`。

## 未验证边界

- 真实 WPS/Excel 多版本、新式 in-cell 图片变体。
- 整班并发图片上传、读取与磁盘吞吐。
- TEMP 图片七天清理的跨期实际运行。
- 老旧机房浏览器和低配设备。
- 区域抽测含图题的真实学生作答页面。
- 原图长期磁盘增长和容量告警。

