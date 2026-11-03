# 电子导学单模块审查与修复记录（2026-10-27，仅本机）

> 只读深度审查 + 分批修复。**未发布**，未连 130/123，未执行任何 SQL 迁移。
> 审查对象为当时工作树（含大量未提交改动），不是任何正式制品。

## 一、审查方法

- 静态链：前端触发 → `api/business/guideSheet.js` → Controller → Service → Mapper XML → 表列，重点读 CAS / `ON DUPLICATE KEY` / 可见域 `otherwise → AND 1=0`。
- 实测：本机起后端 8080 + Vite 80，Playwright 12 组脚本；学生端关键场景用 route 拦截 mock，不写库。
- 只读 SQL：本机库 `xueyeceping_server_20260729`，仅 SELECT/SHOW。
- 后端定向单测：`mvn -pl ruoyi-business -am test "-Dtest=GuideSheet*" "-DforkCount=0"`，13 个测试类 78 项。

## 二、修复前的主要缺陷（按等级）

| 等级 | 问题 | 判定 |
| --- | --- | --- |
| **P0** | 模板库「预览」用未脱敏 `formJson` 渲染，同县他人公共模板的 `onCreated`/`functions` 在教师浏览器执行 | 探针复现 |
| P1 | 学生端 2s 防抖监听 `field-change`，库实际 emit `formChange` → 死代码，只剩 30s 定时器 | 复现 |
| P1 | 评分卡 `totalScore >= 60` 判红绿、无满分分母；`null` 显示“累计得分： 分” | 复现 |
| P1 | 必答校验失败只弹库原文“表单数据校验失败”，不跳题 | 复现 |
| P1 | 看板“正确率”= 原始平均分（满分 45 显示 45.0%，>100 分为负） | 复现 |
| P1 | 草稿 CAS 冲突折叠成“云端暂不可用”，之后每次保存用旧 revision 必失败 | 复现 |
| P1 | 高级模式评分方式下拉无 `manual`，人工题显示裸英文，一改即变精确匹配 | 复现 |
| P1 | 「重新评分」清人工分且无二次确认 | 复现 |
| P1 | 教师广播/刷新/翻页在 WS 未 OPEN 时静默丢弃，界面却清空输入框 | 代码证实 |
| P2 | 学生保存失败不落本地、三重 toast、无重试 | 复现 |
| P2 | 旧格式上传答案渲染成裸路径，点开落到前端 index.html | 复现 |
| P2 | 看板自评恒空死区、班级下拉恒只有“全部班级” | 复现 |
| P2 | 导出 Excel「评分状态」输出英文码值 `partial` | 复现 |
| P2 | 上传三套上限（库默认 5MB / 新手 20MB / 服务端 50MB） | 代码证实 |
| P2 | 390px 提交按钮滚出视口（y=-512）、看板两列各 183px | 复现 |

（其余 P2/P3 见下方“未处理”。）

## 三、本轮已修复

1. **P0 预览脱敏**：`preview.vue` 改用 `getGuideSheetPreview`；移除 v2 不存在的“草稿/已发布”标签，改为“学生可见内容”。
2. **学生自动保存**：监听 `@formChange` + 表单根 `input/change` 捕获；保存态四态（saving/saved/failed/conflict）+ 重试；失败/关页/卸载写 `guide-sheet-local-draft:{bindingId}`，进页提示恢复。
3. **评分口径**：后端 `totalMaxScore(formJson)`，`/progress` 下发 `maxTotalScore`；`/student/grading` 下发 `maxScore` + `gradingStatus`；看板正确率=平均分÷满分（无刻度降级“平均分”），学生卡“已得 X / Y 分”且待批改中性色。
4. **必答校验**：定位第一个错误字段 → 切页 → 滚动聚焦，提示“第 2 页的‘必答：写你的收获’还没填完”。
5. **重评安全**：前端二次确认；导出评分状态中文化（五态全覆盖）。
6. **反馈真实性**：`websocket.send` 返回布尔 + `isOpen()`；看板广播/刷新/翻页失败如实提示且不清空输入；轮询失败改状态条“数据可能已过期”。
7. **设计器**：评分方式下拉补“人工批改”及提示。
8. **学生端体验**：补“第 X / N 页”；操作条 `sticky` 且移除内层滚动容器；`fileMaxSize` 补 50MB；历史附件补 API 前缀；待批改按文案识别（不改白名单）。
9. **`maxPages` 语义**：高级设计器改用 `HomeTab.tabs.length`，不再拿顶层 `widgetList.length` 冒充页数。
10. **白名单收紧**：`SAFE_WIDGET_TYPES` 移除渲染器未打包的 `tree-select`/`picture`/`image`/`signature`，改为走 `safePlaceholder` 提示，避免“必答但无控件 → 永远无法提交”。
11. **本课主题/预计课时**（收尾第二批）：新手向导第一步新增两个输入框，接通此前恒空的 `teachingTopic`/`estimatedMinutes`；实测 AI 请求由 `topic:""` 变为 `topic:"顺序、分支与循环"`。
12. **删除批量重评接口**：`PUT /grading/recore-all/{bindingId}` 无前端入口且非事务、逐条吞异常，直接移除（实测 404）；单份 `recore` 保留并已有二次确认。
13. **删除清理补全**：删课与删学生都补删 `biz_guide_sheet_progress` 与 `biz_guide_sheet_upload`（删课按 `binding_id` join、必先于删绑定）；`UnifiedDeletionTest` 增加两条断言固化。

## 四、验证结果

- 后端 80 项定向单测（13 个导学单类 + `UnifiedDeletionTest`）全绿；`ruoyi-admin` clean package 通过；前端生产构建通过。
- P0 探针：修复前三组对照 A 组 `widget=1, func=1`，修复后 A/B/C 全部 `0`。
- 防抖：输入过程中 4 秒内出现保存请求（修复前 40 秒内只有 31.5s 的一次）。
- 必答：提示“第 2 页的‘必答：写你的收获’还没填完，已为你定位到那一题”，`可见 pane=1`、错项在视口内。
- 正确率：mock 4/5 → `80.0%`；自评行显示 `4 / 5`。
- 窄屏：390×844 滚到底提交按钮 `y=46`（修复前 `-512`），`scrollWidth-clientWidth=0`。
- 上传：8MB PDF 现在可选并发出上传（修复前被 5MB 挡住）。
- 保存失败：单条 toast“草稿未保存，已暂存在本机，可点击‘重试’”，localStorage 含输入内容，重试按钮存在。
- `recore-all` 实测返回 404；单份 `recore` 仍正常返回业务错误。
- 清理 SQL：事务内执行后回滚验证——删课命中 2 行 progress、删学生命中 1 行，回滚后数据仍为 2 行/1 条绑定，未产生变更。

## 五、未处理（保留待评估）

- `getInfo` 仍向同县教师返回 `scoring.answer`：**判定为设计选择不改**。公共导学单本就供同县复用复制，复制副本必然带答案，锁详情接口防不住；P0 的真实入口（预览页执行脚本）已封堵。
- `assertNoPendingCountyExam` 同时拦心跳/上传，抽测期间看板显示“未开始”。
- `rateLimitMap` key 永不清理（长跑内存）。
- 死封装：`delGuideSheet`、`getCreatorList`、`uploadGuideSheetFile`、`recoreGuideSheetAnswer`、`?copyFrom=`。

## 六、待验证 SQL（只读，需目标库执行）

```sql
SELECT COUNT(*) FROM biz_student WHERE class_code LIKE '%班';
SELECT COUNT(*) FROM biz_student s LEFT JOIN sys_user_role ur
  ON ur.user_id=s.user_id AND ur.role_id=101 WHERE ur.user_id IS NULL;
SELECT COUNT(*) FROM biz_guide_sheet_answer WHERE status='2' AND total_score IS NULL;
SELECT binding_id, student_id, question_name, COUNT(*) c
  FROM biz_guide_sheet_upload GROUP BY 1,2,3 HAVING c>1;
SELECT b.binding_id, b.source_version, g.version_no
  FROM biz_lesson_guide_sheet_binding b JOIN biz_guide_sheet g ON g.sheet_id=b.source_sheet_id
  WHERE b.source_version<>g.version_no;
```
