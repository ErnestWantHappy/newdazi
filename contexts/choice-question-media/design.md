# 选择题图片支持设计

2026-11-09 已随130正式1.30.82发布：两列四表迁移完成，正式API与浏览器验收通过；证据见[1.30.82验收](release-1.30.82.md)。

2026-11-09 本机追加 Excel 导入 v3：新增 7 个图片专用列，旧模板继续兼容；图片专用列按左上角锚点所在列归属，并检查是否跨题目行。该改动未发布，正式1.30.82仍是旧实现。

- biz_question和个人抽测快照增加nullable media_json，保留文字列；对象schemaVersion=1，stem/option A—D有序imageId列表。
- 独立图片元数据、导入批次/行、保存回执；TEMP与RETAINED隔离，已绑定图不自动物理删。
- 图片读用受控业务上下文+身份，Axios Blob，不放开/profile，不在URL放令牌。
- 本机原文件位于当前profile同级目录；130生产使用独立持久目录`/data/upload-question-images`（`admin2:admin2 750`），位于release和公开资源根外，后续发布不得清理。TEMP七天清理，RETAINED不自动删；七天跨期运行未实测。
- 保存内容指纹/题目行锁/请求唯一键，使用后自动另存，课题绑定也锁题目，阻止竞态。
- 导入专用reader/writer，文字与图片分别提取；v3新增题干图片1-3、选项A-D图片共7列，按左上角锚点归属、跨行进入待确认，旧模板同格图仍按覆盖面积规则；图上限、异常预览、COMMITTED与正式题/图片绑定/结果映射同事务。
- 正式130已整库备份、迁移、双端发布并重启后端，产品更新160/PUBLISHED；不使用123跳板机的旧Windows发布命令。

组件边界：QuestionImage负责鉴权读图/Blob释放；QuestionMedia展示题干或某选项；QuestionMediaInput负责单slot上传/粘贴/拖拽与待传状态；QuestionImportPreview负责批次加载/修正/确认。页面协调保存/选择，不将评分下放到媒体组件。

实现细节：Repository经Spring代理，JdbcTemplate通过方法取，不能直接访问代理实例字段；预览有效期以数据库日期判断，避免驱动Timestamp/LocalDateTime差异。非选择题保留原分步保存，不共享选择题回执。保存请求失败会查询本人回执找回新题并保留当前表单；抽测组卷仅对已持久引用的题传examId，未保存候选仍按题库权限读取。
