# AI 辅助开发接力提示 (2026-05-27)

> **适用范围**：这是当前仓库的开发接力提示，不再沿用旧版“7 项待修复清单”作为主线任务来源。
>
> **状态口径**：当前仓库已完成本地构建与角色冒烟验证，但**不代表已经部署线上**。线上执行 SQL 或替换 jar/dist 前仍需备份、确认目标库与发布窗口。

---

## 一、当前仓库主线（已改代码，本地已验证）

### 0. 成绩口径与导出紧急修复
- 成绩汇总页按分页懒加载读取当前筛选学生，避免一次性加载全班/全年级所有课程成绩。
- 单课口径：`课程总分 = clamp(作业分 + 课堂表现分, 0, 100)`。
- 多课口径：`作业平均 / 课堂表现平均 / 课程平均分`，请假不参与平均，课堂表现 `0` 分是有效分。
- Excel 导出每节课拆成三列：`作业分 / 课堂表现分 / 课程总分`。
- 导出汇总列按单课/多课自动切换，并同步前端搜索关键词 `keyword`。
- 2026-05-27 已验证：前端 `npm run build:prod` 通过，后端 `compile` 与 `clean package` 通过，教师成绩汇总和导出接口返回正常。

### 1. 操作题预览状态流与失败重试
- 学生端当前课程接口返回的已提交答案，已包含 `previewStatus` 与 `previewPath`。
- `biz_student_answer` 已扩展预览失败重试相关字段：
  - `previewRetryCount`
  - `previewLastRetryTime`
  - `previewErrorMessage`
- 学生页上传操作题后，会根据服务端状态展示“可预览 / 待转换 / 预览暂不可用”，并提供失败下载兜底；预览失败不等于上传失败。
- 教师批改页新增“重新转换本班失败文件”按钮。
- 后端新增接口：
  - `POST /business/teacher/grading/retry-failed-previews`
- 后端新增服务与定时任务：
  - `PracticalPreviewRetryService`
  - `practicalPreviewRetryTask.retryFailedStudentAnswerPreviews`
- 仓库已提供 SQL：
  - `sql/practical_preview_retry_fields.sql`
  - `sql/practical_preview_retry_quartz_job.sql`

### 2. 答题记录查询统一按“最新记录”聚合
- `biz_student_answer` 的多处查询已改为按 `student_id + lesson_id + question_id` 只取最新一条记录。
- 学生提交答案从原先“删旧记录 + 批量新增”，改为“存在则更新，不存在则新增”。
- 这会直接影响：
  - 成绩统计
  - 画像汇总
  - 操作题批改列表
  - 课程答题详情
- 仓库已提供历史数据清理与唯一索引脚本：
  - `sql/typing_answer_dedup_fix.sql`

### 3. 学生画像从“学期”切换为“学年”
- 学生画像接口参数已统一改为：
  - `academicYearStart`
  - `academicYearEnd`
- 前端选择器已从“学期”调整为“学年”，默认口径按 9 月开学计算。
- 学年维度已同步覆盖：
  - 课程成绩
  - 打字速度
  - 课堂表现
  - 排名变化

### 4. 题库与课程设计器配套优化
- 打字题时长不再只读，教师可以手动覆盖推荐值。
- 操作题参考模板文件改为可选；如上传，仅允许 1 个 `docx` 文件。
- 课程设计器允许先创建课程，后续再补充班级指派。

### 5. 其他已改但未单独验收的配套项
- 教师批改页会展示学生提交的预览状态，并尽量保留当前选中学生。
- 学生页打字题增加提交中防重入状态，操作题轮询在页面销毁时会清理。
- 部分排序与后端细节已补强：
  - 课程与班级排序更稳定
  - 学生导入插入语句补上 `remark`
  - 重置密码后清理登录错误缓存

---

## 二、关键接口与数据口径（继续开发时必须遵守）

### 1. 已变更接口
- 学生画像接口参数从 `semesterStart / semesterEnd` 改为 `academicYearStart / academicYearEnd`。
- 教师批改新增：
  - `POST /business/teacher/grading/retry-failed-previews`

### 2. 已变更返回结构
- 学生当前课程返回的 `submittedAnswers` 已包含：
  - `previewStatus`
  - `previewPath`

### 3. 已变更数据理解方式
- `biz_student_answer` 相关查询默认按“最新答题记录”理解，而不是按“历史全量流水”理解。
- 后续新写 SQL、Mapper、统计逻辑时，若场景需要“当前有效答案”，应优先复用“最新记录聚合”口径，避免把旧答题再次统计进去。

---

## 三、当前仍未确认上线的事项

### 1. 数据库执行状态未确认
- 没有证据表明以下 SQL 已执行：
  - `sql/practical_preview_retry_fields.sql`
  - `sql/typing_answer_dedup_fix.sql`

### 2. 定时任务配置
- 本地 `xueyeceping.sys_job` 已确认存在并启用：
  - `practicalPreviewRetryTask.retryFailedStudentAnswerPreviews`
- 线上环境是否已执行仍需发布前确认。

### 3. 构建与联调
- 本地已完成：
  - 后端 Maven `compile` / `clean package`
  - 前端 Vite `build:prod`
  - 教师、学生、教研员菜单与关键页面冒烟
- 线上发布后仍需按真实域名再做一次冒烟。

### 4. 运行产物不作为上下文事实
- `RuoYi-Vue/uploadPath/upload/2026/04/` 属于运行过程中生成的本地附件样本。
- 默认不要把该目录内容写入项目核心记忆，也不要把它当成“需要提交的功能资产”。

---

## 四、下一轮建议优先验证的内容

### 1. 先补环境确认
1. 确认线上数据库是否已经执行 `practical_preview_retry_fields.sql`。
2. 确认线上数据库是否已经执行 `typing_answer_dedup_fix.sql`。
3. 确认线上 Quartz 是否已创建 `practicalPreviewRetryTask.retryFailedStudentAnswerPreviews`。

### 2. 再做功能联调
1. 学生端上传 `docx` 后，是否能经历“上传成功 -> 转换中 -> 可预览”。
2. 学生端上传 `pdf` 后，是否能直接预览。
3. 学生端上传暂不支持在线预览的文件后，是否能正确显示“作品已上传，预览暂不可用”并允许下载原文件。
4. 教师批改页点击“重新转换本班失败文件”后，失败记录是否会重新进入转换流程。
5. 学生画像学年筛选后，课程、打字、表现、排名四类数据是否都按学年范围返回。

### 3. 最后补构建与回归
1. 线上后端启动日志是否正常。
2. 线上前端静态资源是否更新到最新 dist。
3. 最新答题记录口径是否影响历史统计页面。
4. 唯一索引落地后，提交答案是否仍能正常更新而不是报重复键错误。

---

## 五、继续协作时的默认原则

- 默认把当前状态理解为：**代码已改，验证待补**。
- 没有明确证据前，不要把 SQL 已执行、Quartz 已配置、功能已上线写成既成事实。
- 如果下一轮任务与 `biz_student_answer`、学生画像、操作题批改有关，优先核对本文件中的新接口名和新数据口径，避免继续沿用旧的 `semesterStart / semesterEnd` 或“全量答题流水”思维。
- 如果要继续扩展操作题预览链路，优先在现有 `previewStatus / previewPath / previewRetryCount / previewLastRetryTime / previewErrorMessage` 基础上演进，不要再额外引入一套平行状态字段。
