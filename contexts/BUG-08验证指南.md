# BUG-08 学生端评分展示 — 快速验证指南

> 以下步骤在浏览器中手动验证，无需命令行

---

## 验证步骤

### 1. 打开学生端

浏览器访问 `http://localhost:8083`，用学生账号登��：
- 账号：`2025780101`
- 密码：`123456`

点击导航到「导学单」页面。

### 2. 验证评分卡片独立展示

**预期**：页面顶部出现一个独立的「评分结果」卡片（带阴影边框），位于导学单表单上方。

**验证点**：
- 卡片标题显示"评分结果"
- 右侧显示「总分：81 分」标签（绿色，因为 81 ≥ 60）
- 右上角显示「当前: 第一 81/110 分」
- 卡片内是一个表格，列出当前标签页的题目评分

### 3. 验证按标签页自适应过滤

导学单有两个标签页：「第一」和「第二」。

**点击「第一」标签页时**：
- 评分表格应显示 4 道题：
  - radio — 30/30 分（正确）
  - textarea — 30/30 分（AI评分）
  - 物联网技术分为几种 — 6/10 分（AI评分）
  - 物联网架构分别有哪几层 — 15/20 分（部分正确）

**点击「第二」标签页时**：
- 评分表格应只显示 1 道题：
  - checkbox — 0/10 分（错误）

### 4. 验证评分卡片初始隐藏

如果是全新未提交的导学单，评分卡片不会出现（因为没有评分数据）。

只有提交过的导学单才会在加载时自动获取评分并显示卡片。

---

## 关键代码位置

| 文件 | 改动说明 |
|------|---------|
| `student/guideSheet/index.vue` | 独立评分卡片组件（`grading-card`）、按标签页过滤（`filteredGradingDetails`）、标签提取（`extractTabLabels`）|
| `guideSheet.js` | 新增 `getStudentGrading(sheetId)` API 函数 |
| `GuideSheetGradingService.java` | 新增 `flattenWidgetsWithTabIndex()` / `walkFlattenWithTabIndex()` 方法，评分详情含 `tabIndex` 字段 |
| `GuideSheetController.java` | 已有 `GET /student/grading/{sheetId}` 接口，无需修改 |
