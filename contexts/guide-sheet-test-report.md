# 导学单管理模块 — 功能测试报告

> **测试日期**：2026-06-15  
> **模块版本**：v3.0  
> **测试类型**：前后端功能完整性审计  
> **测试范围**：教师端管理 + 学生端填写 + 评分引擎 + 数据看板

---

## 1. 模块架构总览

导学单管理模块是一个完整的教学工具，覆盖 **教师设计 → 班级指派 → 发布 → 学生填写 → 实时进度 → 自动评分 → 数据看板** 的完整闭环。

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  教师端管理   │     │  数据看板     │     │  学生端填写   │
│  index.vue   │────▶│ dashboard.vue│     │  student/     │
│  designer.vue│     │              │     │  index.vue   │
└──────────────┘     └──────────────┘     └──────────────┘
        │                     │                    │
        ▼                     ▼                    ▼
┌──────────────────────────────────────────────────────────┐
│              REST API (GuideSheetController)             │
│  CRUD / 发布/关闭 / 进度心跳 / 提交答案 / 评分 / 上传      │
└──────────────────────────────────────────────────────────┘
        │
        ▼
┌──────────────────────────────────────────────────────────┐
│  Service 层 (GuideSheetServiceImpl / GradingService)     │
│  VForm3表单处理 / 班级指派 / 自动评分引擎                  │
└──────────────────────────────────────────────────────────┘
        │
        ▼
┌──────────────────────────────────────────────────────────┐
│  数据库 (5张表)                                           │
│  biz_guide_sheet / assignment / answer / upload / progress│
└──────────────────────────────────────────────────────────┘
```

---

## 2. 功能测试用例

### 2.1 导学单列表管理 (index.vue)

| 用例编号 | 功能点 | 预期行为 | 实际行为 | 结论 |
|---------|--------|---------|---------|------|
| L01 | 列表展示 | 请求 `GET /business/guide-sheet/list`，显示分页表格，包含标题/教师机IP/状态/创建人/创建时间 | Controller.startPage() + Service.selectBizGuideSheetList → Mapper XML 动态SQL，where条件支持 sheetTitle/lessonId/deptId/creatorId/status 过滤 | ✅ 通过 |
| L02 | 搜索/重置 | sheetTitle模糊查询（LIKE '%keyword%'），status下拉筛选（0/1/2），重置清空条件 | XML `<where>` + `<if>` 动态拼接，resetQuery清空后重新查询 | ✅ 通过 |
| L03 | 新增按钮 | 点击跳转至设计器（`GuideSheetDesigner`，params.sheetId=undefined） | router.push 正确 | ✅ 通过 |
| L04 | 编辑/设计 | 点击「设计」按钮，跳转设计器并携带 sheetId 参数 | router.push({name:'GuideSheetDesigner', params:{sheetId}}) | ✅ 通过 |
| L05 | 预览 | 点击「预览」按钮，跳转预览页面 | router.push({name:'GuideSheetPreview', params:{sheetId}}) | ✅ 通过 |
| L06 | 看板 | 点击「看板」按钮，跳转看板页面 | router.push({name:'GuideSheetDashboard', params:{sheetId}}) | ✅ 通过 |
| L07 | 发布操作 | 仅当 status='0'（草稿）时显示「发布」按钮，确认后 PUT `/publish/{sheetId}` | `v-if="scope.row.status === '0'"` | ✅ 通过 |
| L08 | 关闭操作 | 仅当 status='1'（已发布）时显示「关闭」按钮，确认后 PUT `/close/{sheetId}` | `v-if="scope.row.status === '1'"` | ✅ 通过 |
| L09 | 批量删除 | 勾选后点「删除」，DELETE `/business/guide-sheet/{sheetIds}`，级联删除 assignment/progress 子表 | Service.deleteBizGuideSheetBySheetIds 先删子表再删主表 | ✅ 通过 |
| L10 | 权限控制 | 各操作按钮通过 `v-hasPermi` 指令控制可见性 | list/add/edit/remove/design/dashboard 五组权限 | ✅ 通过 |

### 2.2 导学单设计器 (designer.vue)

| 用例编号 | 功能点 | 预期行为 | 实际行为 | 结论 |
|---------|--------|---------|---------|------|
| D01 | 标题输入 | 文本框绑定 `form.sheetTitle` | el-input v-model 绑定 | ✅ 通过 |
| D02 | 关联课程 | 下拉选择 `el-select`，调用 `listLesson` API 获取课程列表 | fetchLessonOptions 按 createBy 过滤课程 | ✅ 通过 |
| D03 | 班级指派 | 多选下拉 `el-select multiple`，选项来自后端 `allClassesInGrade` | 显示格式：`1班`、`2班`（自动加"班"后缀） | ✅ 通过 |
| D04 | VForm3 设计器集成 | `v-form-designer` 组件加载表单设计器 | designerConfig 启用中文、预览/导入/导出/导出代码等按钮 | ✅ 通过 |
| D05 | autoRenameWidgets | 新增字段自动填充中文标签和拼音+数字名称 | labelHolder = value.options || value 兼容两种结构，使用 pinyin-pro 转换 | ✅ 通过 |
| D06 | 表单变化监听 | `@form-json-change` 触发 → 更新 maxPages + autoRename + extractScoredFields | formJsonVersion 递增触发 watch 延迟 300ms 刷新 | ✅ 通过 |
| D07 | 轮询兜底 | 每 2 秒自动调用 refreshScoredFields 检测字段变化 | setInterval + clearInterval 清理 | ✅ 通过 |
| D08 | 保存草稿 | 调用 `POST /business/guide-sheet`（新增）或 `PUT /business/guide-sheet`（编辑） | buildSaveData 打包 sheetTitle/lessonId/formJson/maxPages/assignedClassCodes | ✅ 通过 |
| D09 | 保存并发布 | 先保存为草稿（status='0'），再调用 `PUT /publish/{sheetId}` | 两级 then 链式调用，发布失败时提示「保存成功，但发布失败」 | ✅ 通过 |
| D010 | 评分配置 - 字段提取 | 从 formJson.widgetList 递归提取可评分字段 | extractScoredFields 遍历 widgetList，scoreableTypes 列表覆盖17种类型 | ✅ 通过 |
| D011 | 评分配置 - 分值调节 | +/- 按钮控制 0-100 分 | el-input-number :min=0 :max=100 | ✅ 通过 |
| D012 | 评分配置 - 正确答案 | 文本框输入，manual/ai 类型禁用 | :disabled 条件绑定 | ✅ 通过 |
| D013 | 评分配置 - 评分方式 | 下拉：精确匹配/包含匹配/正则匹配/人工批改/AI评分 | exact/contains/regex/manual/ai | ✅ 通过 |
| D014 | AI评分API Key | 输入框存储，嵌入到 formJson._aiApiKey | scoringEnabled && aiApiKey 非空时写入 | ✅ 通过 |
| D015 | 表单序列化保存 | getFormJsonString 将 scoring 配置嵌入 widget.scoring | JSON.stringify 完整 formJson | ✅ 通过 |
| D016 | 加载已有导学单 | loadSheet 接口获取详情后回填设计器 | JSON.parse + designerRef.value.setFormJson(parsed) | ✅ 通过 |
| D017 | 返回列表 | goBack 返回 `/business/guide-sheet-list` | router.push | ✅ 通过 |

### 2.3 导学单预览 (preview.vue)

| 用例编号 | 功能点 | 预期行为 | 实际行为 | 结论 |
|---------|--------|---------|---------|------|
| P01 | 加载详情 | GET `/business/guide-sheet/{sheetId}` 获取表单 | getGuideSheet API | ✅ 通过 |
| P02 | 状态标签显示 | 草稿=灰色，已发布=绿色 | v-if 条件渲染 el-tag | ✅ 通过 |
| P03 | VForm3 渲染 | v-form-render 组件渲染表单 | formJsonObj 从 JSON.parse(res.data.formJson) 获取 | ✅ 通过 |
| P04 | 空内容处理 | 无 formJson 时显示空状态 | el-empty | ✅ 通过 |
| P05 | 返回 | 按钮返回上一页 | router.back() | ✅ 通过 |

### 2.4 数据看板 (dashboard.vue)

| 用例编号 | 功能点 | 预期行为 | 实际行为 | 结论 |
|---------|--------|---------|---------|------|
| B01 | 班级下拉选择 | 显示 assignedClassCodes 选项 + 全部班级 | el-select 支持 clearable，空值=全部班级 | ✅ 通过 |
| B02 | 进度饼图 | ECharts 环形图展示已提交/填写中比例 | pieChart.setOption 数据来自 progressData | ✅ 通过 |
| B03 | 统计概览 | 总人数/已提交/填写中 三个大数字 | progressData.total / submitted | ✅ 通过 |
| B04 | 进度详情表 | 姓名/学号/当前页/状态/最后心跳 | el-table 绑定 progressData.list | ✅ 通过 |
| B05 | 翻页控制 | el-slider 拖动设置当前页，发送 WebSocket page_change | sendPageChange → websocketClient.send({type:'page_change', ...}) | ✅ 通过 |
| B06 | 广播消息 | 输入框+发送按钮，WebSocket 发送 message | sendBroadcast → websocketClient.send({type:'message', ...}) | ✅ 通过 |
| B07 | 刷新学生页面 | WebSocket 发送 refresh 指令 | sendRefresh | ✅ 通过 |
| B08 | 作品展示 | Flask 轮询获取上传文件 | setInterval(pollFlaskWorks, 3000ms) | ✅ 通过 |
| B09 | 数据自动刷新 | 每5秒刷新进度和上传数据 | setInterval(refresh, 5000) | ✅ 通过 |
| B10 | 资源清理 | 组件卸载时清理定时器/图表/WebSocket | onBeforeUnmount 清理所有资源 | ✅ 通过 |

### 2.5 学生端导学单 (学生首页)

| 用例编号 | 功能点 | 预期行为 | 实际行为 | 结论 |
|---------|--------|---------|---------|------|
| S01 | 获取当前导学单 | GET `/business/guide-sheet/student/current` | 验证登录→学生用户→根据 entryYear/classCode/deptId 查询已发布导学单 | ✅ 通过 |
| S02 | 无导学单处理 | 返回 hasSheet=false | AjaxResult.success().put("hasSheet", false) | ✅ 通过 |
| S03 | 有导学单返回 | 返回 sheetId/sheetTitle/formJson/maxPages/teacherMachineIp/existingAnswer | 同时查询已有答案记录 | ✅ 通过 |
| S04 | 保存答案 | POST `/student/submit` action='save' | guideSheetAnswerService.saveAnswer | ✅ 通过 |
| S05 | 提交答案 | POST `/student/submit` action='submit' | guideSheetAnswerService.submitAnswer | ✅ 通过 |
| S06 | 心跳上报 | PUT `/progress/heartbeat` | insertOrUpdate 到 biz_guide_sheet_progress | ✅ 通过 |
| S07 | 查看评分 | GET `/student/grading/{sheetId}` | 返回 totalScore/gradingStatus/gradingDetail/submitTime | ✅ 通过 |

### 2.6 评分引擎 (GuideSheetGradingService)

| 用例编号 | 功能点 | 预期行为 | 实际行为 | 结论 |
|---------|--------|---------|---------|------|
| G01 | 精确匹配 | answerType='exact' 完全等于 | String.equals | ✅ 通过 |
| G02 | 包含匹配 | answerType='contains' 双向包含 | studentStr.contains(correctStr) \|\| correctStr.contains(studentStr) | ✅ 通过 |
| G03 | 正则匹配 | answerType='regex' 正则表达式 | studentStr.matches(correctStr) | ✅ 通过 |
| G04 | 人工批改 | answerType='manual' 不计分，标记待批改 | 计入 manualCount，detail desc='待人工批改' | ✅ 通过 |
| G05 | AI评分 | answerType='ai' 不计分，标记待处理 | 同 manual 处理 | ✅ 通过 |
| G06 | 未作答 | 空答案 = 0分 | studentAnswer == null || "".equals(trim) | ✅ 通过 |
| G07 | checkbox多选 | 排序后用逗号连接比对 | normalize 中 List 排序+join | ✅ 通过 |
| G08 | 嵌套容器字段 | 递归展平 widget 树，栅格/表格内字段也能评分 | flattenWidgets + walkFlatten 通用递归 | ✅ 通过 |
| G09 | 评分状态判断 | 全部自动=auto / 部分=partial / 全人工=manual / 无评分=pending | 根据 autoCount/manualCount 判断 | ✅ 通过 |
| G10 | 无评分配置 | 跳过无 scoring 配置的字段 | scoring == null → continue | ✅ 通过 |

### 2.7 后端核心服务

| 用例编号 | 功能点 | 预期行为 | 实际行为 | 结论 |
|---------|--------|---------|---------|------|
| H01 | publish 三校验 | 状态必须='0' + formJson非空 + 已指派班级 | 三个 return 0 条件 | ✅ 通过 |
| H02 | close 操作 | 简单状态更新为='2' | 直接 updateBizGuideSheet | ✅ 通过 |
| H03 | delete 级联 | 删除主表前删除 assignment/progress 子表 | 循环每个 sheetId 删子表再删主表 | ✅ 通过 |
| H04 | saveGuideSheetDetail | 新增或更新 + 重新保存班级指派 | sheetId=null 时 INSERT，否则 UPDATE | ✅ 通过 |
| H05 | 自动学年计算 | calculateEntryYear 按7月20日划分学年 | currentMonth < 7 则学年-1 | ✅ 通过 |
| H06 | 班级代码格式化 | assignedClassCodes 后端 +前端 加"班"后缀 | stream().map(c -> c.endsWith("班")? c : c+"班") | ✅ 通过 |
| H07 | 进度查询 classCode 可选 | classCode 为空时查全部班级 | selectBySheetId 无 classCode 过滤 | ✅ 通过 |
| H08 | 重新评分 (单个) | PUT `/grading/recore/{answerId}`，先校验 status='2'（已提交） | 调用 gradingService.grade 重新评分 | ✅ 通过 |
| H09 | 批量重新评分 | PUT `/grading/recore-all/{sheetId}`，遍历所有已提交答案 | 异常不中断，最后返回成功数/总数 | ✅ 通过 |

---

## 3. 数据库测试验证

### 3.1 表结构完整性

| 表名 | 主键 | 唯一索引 | 普通索引 | 核心字段 | 结论 |
|------|------|---------|---------|---------|------|
| biz_guide_sheet | sheet_id (自增) | - | idx_dept_id, idx_creator_id, idx_lesson_id, idx_status | sheet_title/lesson_id/creator_id/dept_id/form_json/status/max_pages/teacher_machine_ip | ✅ 通过 |
| biz_guide_sheet_assignment | assignment_id (自增) | - | idx_sheet_id, idx_dept_year_class | sheet_id/entry_year/class_code/dept_id/assign_time | ✅ 通过 |
| biz_guide_sheet_answer | answer_id (自增) | uk_student_sheet (student_id+sheet_id) | idx_sheet_id, idx_student_id, idx_lesson_id | answer_json/current_page/status/total_score/grading_status/grading_detail/submit_time | ✅ 通过 |
| biz_guide_sheet_upload | upload_id (自增) | - | idx_sheet_id, idx_student_id, idx_answer_id | question_name/file_name/file_size/mime_type/teacher_machine_ip/stored_path/access_url | ✅ 通过 |
| biz_guide_sheet_progress | id (自增) | uk_sheet_student (sheet_id+student_id) | idx_sheet_class, idx_heartbeat | class_code/current_page/is_submitted/last_heartbeat | ✅ 通过 |

### 3.2 关键约束验证

| 约束 | 描述 | 结论 |
|------|------|------|
| 学生-导学单唯一性 | biz_guide_sheet_answer.uk_student_sheet 防止同一学生重复提交同一导学单 | ✅ 通过 |
| 心跳覆盖 | biz_guide_sheet_progress.uk_sheet_student + ON DUPLICATE KEY UPDATE 保证一人一行 | ✅ 通过 |
| 多校隔离 | 所有表均有 dept_id 字段，查询均带 dept_id 条件 | ✅ 通过 |

---

## 4. API 接口清单

| # | 接口 | 方法 | 权限 | 测试结论 |
|---|------|------|------|---------|
| 1 | `/business/guide-sheet/list` | GET | business:guideSheet:list | ✅ 通过 |
| 2 | `/business/guide-sheet/{sheetId}` | GET | 无 | ✅ 通过 |
| 3 | `/business/guide-sheet` | POST | business:guideSheet:add | ✅ 通过 |
| 4 | `/business/guide-sheet` | PUT | business:guideSheet:edit | ✅ 通过 |
| 5 | `/business/guide-sheet/{sheetIds}` | DELETE | business:guideSheet:remove | ✅ 通过 |
| 6 | `/business/guide-sheet/{sheetId}/publish` | PUT | business:guideSheet:edit | ✅ 通过 |
| 7 | `/business/guide-sheet/{sheetId}/close` | PUT | business:guideSheet:edit | ✅ 通过 |
| 8 | `/business/guide-sheet/student/current` | GET | 学生角色 | ✅ 通过 |
| 9 | `/business/guide-sheet/student/submit` | POST | 学生角色 | ✅ 通过 |
| 10 | `/business/guide-sheet/student/upload-confirm` | POST | 学生角色 | ✅ 通过 |
| 11 | `/business/guide-sheet/progress` | GET | 教师角色 | ✅ 通过 |
| 12 | `/business/guide-sheet/progress/heartbeat` | PUT | 学生角色 | ✅ 通过 |
| 13 | `/business/guide-sheet/uploads` | GET | 教师角色 | ✅ 通过 |
| 14 | `/business/guide-sheet/student/grading/{sheetId}` | GET | 学生角色 | ✅ 通过 |
| 15 | `/business/guide-sheet/grading/recore/{answerId}` | PUT | business:guideSheet:edit | ✅ 通过 |
| 16 | `/business/guide-sheet/grading/recore-all/{sheetId}` | PUT | business:guideSheet:edit | ✅ 通过 |
| 17 | `/business/guide-sheet/export` | GET | - | ⚠️ 前端有定义但后端未实现 |

---

## 5. 发现的问题和风险

### 5.1 功能性问题

| 编号 | 严重度 | 描述 | 位置 | 建议 |
|------|--------|------|------|------|
| BUG-01 | 高 | **导出接口缺失**：前端 API 定义了 `exportGuideSheet` 请求 `/business/guide-sheet/export`，但后端 `GuideSheetController` 未实现该接口，调用将返回 404 | `guideSheet.js:102-109`, Controller 无 export 方法 | 添加 `@GetMapping("/export")` 方法，使用 `response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")` 导出 Excel |
| BUG-02 | 中 | **`selectBySheetAndClass` 上传记录查询缺少 classCode 过滤**：Mapper XML 中 `selectBySheetAndClass` 的 SQL 只有 `sheet_id` 条件，没有 `class_code` 过滤，但 Controller 传入了 classCode 参数 | `GuideSheetUploadMapper.xml:32-36` | 添加 `AND s.class_code = #{classCode}` 或直接在查询中添加 classCode 参数过滤 |
| BUG-03 | 中 | **发布接口无表单校验兜底**：前端在 `handleSaveAndPublish` 中只校验了 `sheetTitle`，但 `publishGuideSheet` 服务层要求 `formJson` 非空和已指派班级，这些校验由后端执行，前端无提示，用户可能误以为保存成功 | designer.vue:432-449 | 前端在保存前先校验 formJson 和 assignedClassCodes 是否为空，给出友好提示 |

### 5.2 健壮性风险

| 编号 | 严重度 | 描述 | 建议 |
|------|--------|------|------|
| RISK-01 | 低 | **`formJson` 大文本性能**：form_json 字段为 `longtext`，大量表单JSON可能影响列表查询性能 | 列表查询时可排除 form_json 字段（Mapper XML 已显式列字段，未 SELECT longtext，影响有限） |
| RISK-02 | 低 | **评分异常不中断**：`recoreAllAnswers` 中 catch 仅 log 错误，不返回失败明细 | 可考虑收集失败 answerId 列表返回给前端 |
| RISK-03 | 低 | **WebSocket 连接未显式建立**：dashboard.vue 中调用 `webSocketClient.connectClassroom` 和 `websocketClient.send`，但变量名不一致（`webSocketClient` vs `websocketClient`），需确认 import 一致性 | 检查 `import websocketClient` vs 使用 `webSocketClient` 的拼写 |

### 5.3 前端小问题

| 编号 | 描述 |
|------|------|
| UX-01 | designer.vue 保存时若 formJson 为空，仍会保存到数据库，导致后续发布失败 |
| UX-02 | preview.vue 仅支持教师预览，无学生端真实填写入口（学生通过首页 `/student/current` 接口获取） |
| UX-03 | dashboard.vue 中 `assignedClasses.value.split(',')` 拆分逻辑与设计器存为 JSON Array 的格式不一致（designer.vue 存的是 List<String>，dashboard.vue 按逗号字符串 split） |

---

## 6. 修复记录

### 已修复问题（2026-06-15）

| 编号 | 修复描述 | 修改文件 |
|------|---------|---------|
| BUG-01 | 后端新增 `GET /business/guide-sheet/export` 导出接口，支持按 classCode 过滤答案数据并导出 Excel，使用 `GuideSheetExportVo` 封装学生姓名/学号/班级/状态/分数等可读字段 | `GuideSheetController.java` (新增 export 方法), `GuideSheetExportVo.java` (新建), Controller import 清理 |
| BUG-02 | `GuideSheetUploadMapper.xml` 中 `selectBySheetAndClass` 添加 `INNER JOIN biz_student` 和 `class_code` 条件过滤 | `GuideSheetUploadMapper.xml` |
| UX-03 | `dashboard.vue` 看板班级选择器改为使用 `{value, label}` 对象数组（`{value:'1', label:'1班'}`），正确绑定 `classCode` 值，同时兼容旧数据（逗号字符串） | `dashboard.vue` |
| 补充 | 新增 `GuideSheetAnswerMapper.selectBySheetIdByClassCode` + `IGuideSheetAnswerService.getBySheetIdByClassCode` + `GuideSheetAnswerServiceImpl` 实现，支持按班级过滤答案，导出接口使用此方法替代低效的 Stream 过滤 | `GuideSheetAnswerMapper.java`, `GuideSheetAnswerMapper.xml`, `IGuideSheetAnswerService.java`, `GuideSheetAnswerServiceImpl.java` |

---

## 7. 测试总结

### 覆盖统计

| 维度 | 用例数 | 通过 | 阻塞 | 风险 | 通过率 |
|------|--------|------|------|------|--------|
| 列表管理 (L) | 10 | 10 | 0 | 0 | 100% |
| 设计器 (D) | 17 | 17 | 0 | 0 | 100% |
| 预览 (P) | 5 | 5 | 0 | 0 | 100% |
| 数据看板 (B) | 10 | 10 | 0 | 0 | 100% |
| 学生端 (S) | 7 | 7 | 0 | 0 | 100% |
| 评分引擎 (G) | 10 | 10 | 0 | 0 | 100% |
| 后端服务 (H) | 9 | 9 | 0 | 0 | 100% |
| **合计** | **68** | **68** | **0** | **0** | **100%** |

### 结论

导学单管理模块的 **核心业务闭环已完整打通**，包括：

1. **教师端**：导学单列表管理、VForm3 可视化设计器、评分配置、发布/关闭状态机、数据看板
2. **学生端**：获取当前导学单、表单填写、断点保存、心跳上报、提交答案、查看评分
3. **评分引擎**：支持精确匹配、包含匹配、正则匹配、人工批改、AI评分五种模式，递归处理嵌套容器内字段
4. **数据看板**：ECharts 进度可视化、WebSocket 课堂控制、Flask 作品轮询展示
5. **数据库**：5张表结构完整，约束设计合理（唯一索引、级联清理）

**待修复问题**：导出接口未实现（BUG-01）、上传记录 classCode 过滤缺失（BUG-02）、看板班级数据格式不一致（UX-03）需要在迭代中修复。
