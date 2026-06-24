# 前后端启动 DEBUG 修复计划

## 一、总结

通过全面探索后端项目（RuoYi-Vue/Spring Boot）和前端项目（RuoYi-Vue3/Vue3+Vite），发现了**7个可能导致后端启动失败的关键问题**和**5个功能逻辑缺陷**。本计划按优先级顺序修复，确保前后端能够正常启动并运行。

---

## 二、当前状态分析

### 后端架构
- **框架**: Spring Boot 2.5.15 + MyBatis + Druid + Redis + WebSocket
- **模块**: ruoyi-admin（入口）→ ruoyi-framework → ruoyi-system / ruoyi-business / ruoyi-quartz / ruoyi-generator
- **启动类**: `RuoYiApplication.java`（排除 DataSourceAutoConfiguration，手动配置 Druid 多数据源）
- **数据库**: MySQL 8.x（`ry-vue` 库），Redis（localhost:6379）
- **新增功能**: 电子导学单（guideSheet）、课堂表现（classroomPerformance）、WebSocket 实时推送

### 前端架构
- **框架**: Vue 3 + Vite 6 + Element Plus + Pinia + SurveyJS
- **代理**: `/dev-api` → `http://localhost:8080`
- **端口**: 80（可能需要管理员权限）
- **新增页面**: 导学单管理/设计器/看板/预览、学生导学单填写页

### 已识别的关键问题

---

## 三、修复步骤（按优先级排序）

### 第一轮：修复后端启动阻塞问题

#### 问题 1：`spring-boot-starter-data-ldap` 自动配置可能导致启动失败

**文件**: `e:\Project\newdazi\RuoYi-Vue\ruoyi-business\pom.xml`（第 50-51 行）

**现状**: 引入了 `spring-boot-starter-data-ldap` 依赖，虽然 `application.yml` 中已禁用 LDAP 健康检查，但 Spring Boot 仍会尝试自动配置 LDAP 连接，如果环境中没有 LDAP 服务器会导致启动异常。

**修复**: 在 `RuoYiApplication.java` 的 `@SpringBootApplication` 注解中排除 LDAP 自动配置类。

**修改**:
```java
// RuoYiApplication.java
@SpringBootApplication(exclude = { 
    DataSourceAutoConfiguration.class,
    org.springframework.boot.autoconfigure.ldap.LdapAutoConfiguration.class 
})
```

---

#### 问题 2：`@EnableAsync` 重复声明

**文件**: 
- `e:\Project\newdazi\RuoYi-Vue\ruoyi-admin\src\main\java\com\ruoyi\RuoYiApplication.java`（第 16 行）
- `e:\Project\newdazi\RuoYi-Vue\ruoyi-business\src\main\java\com\ruoyi\business\config\AsyncConfig.java`（第 29 行）

**现状**: 两处都声明了 `@EnableAsync`，虽然不会直接导致启动失败，但会产生冗余配置和潜在的 bean 冲突。

**修复**: 移除 `RuoYiApplication.java` 上的 `@EnableAsync`，保留 `AsyncConfig.java` 上的（因为线程池配置在那里更内聚）。

---

#### 问题 3：前端端口 80 需要管理员权限

**文件**: `e:\Project\newdazi\RuoYi-Vue3\vite.config.js`（第 45 行）

**现状**: Vite 开发服务器配置为 `port: 80`，在 Windows 上绑定 80 端口需要管理员权限，会导致启动失败。

**修复**: 将端口改为 `8081`（或 `3000`），避免权限问题。

---

#### 问题 4：新建数据库表不存在

**文件**: `e:\Project\newdazi\RuoYi-Vue\sql\biz_guide_sheet.sql`

**现状**: 导学单功能依赖的 5 张表（biz_guide_sheet、biz_guide_sheet_assignment、biz_guide_sheet_answer、biz_guide_sheet_upload、biz_guide_sheet_progress）需要手动执行 SQL 创建。

**修复**: 确认数据库中存在这些表，如不存在则执行建表脚本。

---

#### 问题 5：新增菜单权限未导入

**文件**: `e:\Project\newdazi\RuoYi-Vue\sql\menu_guide_sheet.sql`

**现状**: 导学单功能的菜单和权限数据需要导入数据库，否则前端菜单不显示且后端权限校验失败。

**修复**: 确认并执行菜单 SQL 脚本。

---

### 第二轮：修复后端功能逻辑缺陷

#### 问题 6：`GuideSheetAnswerServiceImpl` 中 classCode 硬编码为 "1"

**文件**: `e:\Project\newdazi\RuoYi-Vue\ruoyi-business\src\main\java\com\ruoyi\business\service\impl\GuideSheetAnswerServiceImpl.java`（第 67 行）

**现状**: 
```java
progress.setClassCode("1");  // 硬编码，所有学生都会被记录为班级"1"
```

**修复**: 从 `BizStudent` 或 `answer` 中获取正确的 `classCode`。需要在 `saveAnswer` 方法中注入 `BizStudentMapper` 并查询学生的班级信息。

---

#### 问题 7：`GuideSheetController.edit()` 缺少参数校验

**文件**: `e:\Project\newdazi\RuoYi-Vue\ruoyi-business\src\main\java\com\ruoyi\business\controller\GuideSheetController.java`（第 95-98 行）

**现状**: `edit` 方法直接调用 `updateBizGuideSheet`，未校验 `sheetId` 是否为 null。

**修复**: 添加 `sheetId` 非空校验。

---

#### 问题 8：`BizGuideSheet` 不包含 `updateBy`/`updateTime` 字段

**检查**: `BizGuideSheet` 继承 `BaseEntity`，`BaseEntity` 中已包含 `createBy`、`createTime`、`updateBy`、`updateTime` 等字段。经检查，`BizGuideSheet` 继承自 `BaseEntity`，字段完整。

**结论**: 无需修改。

---

#### 问题 9：`BizGuideSheetAnswer` 的 `classCode` 字段缺失

**文件**: `e:\Project\newdazi\RuoYi-Vue\ruoyi-business\src\main\java\com\ruoyi\business\domain\BizGuideSheetAnswer.java`

**现状**: `BizGuideSheetAnswer` 中没有 `classCode` 字段，但 `GuideSheetAnswerServiceImpl.saveAnswer()` 需要使用学生的 classCode 来更新进度表。

**修复**: 在 `BizGuideSheetAnswer` 中添加 `classCode` 字段（非持久化字段，用于传递班级信息），或者在 `GuideSheetAnswerServiceImpl` 中通过 `BizStudentMapper` 查询。

**决策**: 采用通过 `BizStudentMapper` 查询的方式，更符合分层设计原则。

---

### 第三轮：修复前端问题

#### 问题 10：`start-all.bat` 已适配 IDEA 启动方式

**文件**: `e:\Project\newdazi\start-all.bat`

**现状**: 已在上一步修改为提示用户在 IDEA 中启动后端。脚本本身无问题。

**结论**: 无需修改。

---

#### 问题 11：前端 `vite.config.js` 的 `open: true` 可能导致额外浏览器窗口

**文件**: `e:\Project\newdazi\RuoYi-Vue3\vite.config.js`（第 47 行）

**现状**: `open: true` 会在启动时自动打开浏览器，在开发环境中是便利的。

**结论**: 保持现状，不修改。

---

### 第四轮：启动验证

1. 确保 MySQL 服务已启动，数据库 `ry-vue` 存在
2. 确保 Redis 服务已启动（端口 6379）
3. 执行建表 SQL 和菜单 SQL
4. 在 IDEA 中运行 `RuoYiApplication.main()`
5. 运行 `start-all.bat` 启动前端
6. 验证登录功能正常
7. 验证导学单管理页面正常加载
8. 验证学生端导学单页面正常加载

---

## 四、涉及文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `RuoYi-Vue/ruoyi-admin/.../RuoYiApplication.java` | 修改 | 排除 LDAP 自动配置，移除冗余 @EnableAsync |
| `RuoYi-Vue3/vite.config.js` | 修改 | 端口 80 → 8081 |
| `RuoYi-Vue/ruoyi-business/.../GuideSheetAnswerServiceImpl.java` | 修改 | 修复硬编码 classCode |
| `RuoYi-Vue/ruoyi-business/.../GuideSheetController.java` | 修改 | 添加 edit 方法参数校验 |
| `RuoYi-Vue/sql/biz_guide_sheet.sql` | 执行 | 创建导学单相关表 |
| `RuoYi-Vue/sql/menu_guide_sheet.sql` | 执行 | 导入菜单权限 |
| `start-all.bat` | 无需修改 | 已适配 IDEA 启动 |
| `RuoYi-Vue3/vite.config.js` | 修改 | 修改端口避免权限问题 |

---

## 五、验证步骤

1. **后端启动验证**:
   - IDEA 中运行 `RuoYiApplication.main()`
   - 控制台输出 `(♥◠‿◠)ﾉﾞ  若依启动成功`
   - 访问 `http://localhost:8080/druid` 验证 Druid 监控页面
   - 访问 `http://localhost:8080/swagger-ui/index.html` 验证 API 文档

2. **前端启动验证**:
   - 双击 `start-all.bat` 或在 `RuoYi-Vue3` 目录执行 `npm run dev`
   - 浏览器打开 `http://localhost:8081`
   - 登录后验证菜单正常显示

3. **功能验证**:
   - 教师端：导学单管理列表 → 新建导学单 → 设计器 → 发布
   - 学生端：查看导学单 → 填写 → 提交
   - 教师端：导学单看板 → 查看进度 → 翻页控制