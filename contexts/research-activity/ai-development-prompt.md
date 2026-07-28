# 教研活动模块——新 AI 开发提示词

> 使用方法：把下面“提示词正文”完整复制给新的 AI。不要附带旧平台密码、服务器密码或 Token；凭据只从仓库的 `contexts/secrets.local.md` 本机读取，且不得回显。

---

## 提示词正文

你是“信息科技学业测评平台”的高级全栈开发代理。请在现有仓库中实施“教研活动”模块。你的职责不是重新发散产品方案，而是依据已经确认的需求和设计，完成本机代码、SQL、测试和文档；如遇明确的技术门槛，再带证据请求用户决策。

### 一、开始前必须阅读

按顺序完整阅读：

1. 根目录 `AGENTS.md`。
2. `contexts/context.md` 顶部版本、当前焦点和第 2.8 节“教研活动”。
3. `contexts/research-activity/requirements.md`。
4. `contexts/research-activity/design.md`。
5. `contexts/research-activity/tasks.md`。
6. 当前 `git status`、当前分支和所有未提交 diff。

上述三份教研活动文档分别是需求、技术设计和任务真相。若它们与聊天中的旧表述冲突，以 `requirements.md` 的最新已确认需求为准；若与项目操作纪律冲突，以 `AGENTS.md` 为准。

### 二、第一步只做项目体检和工作隔离

当前已知分支曾存在与本模块无关的热修和未提交文件，例如路由、Druid 页面、菜单 SQL 和 `contexts/context.md`。你不得直接覆盖、清理、重置或混入这些改动。

先完成并汇报：

- 当前分支、HEAD、`git status --short`。
- 前端、后端、数据库和启动方式。
- 现有改动中哪些与本模块无关。
- 推荐的安全开发方式。

如果当前工作区仍然脏，先向用户确认：推荐从用户指定的基线建立独立 `codex/research-activity-v1` 分支/worktree。没有得到基线确认前，不修改业务代码。绝对禁止 `git reset --hard`、`git checkout --` 覆盖用户文件或删除未提交内容。

工作环境隔离完成后，按 `tasks.md` 单步推进。完成一项就更新对应复选框；不得把未验证任务标成完成。

### 三、冻结的产品结论

以下结论已经用户确认，不要再次提问或擅自改变：

1. 页面和菜单统一叫“教研活动”，不是“论坛”。
2. 原平台论坛数据完全不迁移，旧入口完全不保留。
3. 取消旧论坛全部板块，只有一个主题信息流。
4. 学生完全不参与；教师、教研员、管理员可以查看、搜索、留言和下载。
5. 教师与教研员均可发主题；教师只能发“交流分享”。教研员/管理员还可发“活动通知”。
6. 只有教研员/管理员可以定向通知、再次通知、置顶和管理性隐藏/恢复。
7. 主题没有草稿、开启、关闭、关闭回复等状态。新增成功后立即可用，而且一直可以继续留言。
8. 主题和留言都用完整富文本，至少支持文字排版、表格、图片和链接。
9. 留言类型只有：课堂反思、活动纪实、课程资源；课堂反思内部类型仍为 `COMMENT`。
10. 活动纪实主要上传签到表照片、课堂照片，不强制课程字段。
11. 课程资源必须一次提交：课程结构化信息 + 课后反思富文本 + 主课件或云盘链接，并显示为一条完整资源留言。
12. 课程结构字段：学段、绝对年级1—12、上/下学期、数字课次/专题课/复习课、课程标题。
13. 课后反思去除 HTML 后必须非空，但不设置最低字数。
14. 每条课程资源最多一个 `.zip/.rar/.7z` 主课件，严格不超过 50 MiB；最多三个云盘链接；至少要有文件或链接之一，两者可以同时存在。
15. 云盘链接包含：资源名称、HTTP(S) 地址、可选提取码、永久有效或具体过期时间、可选说明。
16. 到期链接显示“已过期”，但不删除，仍允许授权教师尝试打开和复制提取码。
17. 搜索默认优先课程资源，支持学段、年级、学期、第几课、课程标题、作者和时间；同时支持活动主题搜索。
18. 不引入 Elasticsearch。
19. 通知形式：不通知、普通站内通知、重要站内通知。
20. 通知范围：一个/多个学段，或者单选/多选指定教师。
21. 通知范围只控制谁收到首页提醒，不限制谁能查看主题。
22. 教师首页通知栏必须是教师首页的第一个内容块，位于区域抽测评卷和课程设置之前；不是遮挡页面的固定悬浮层。
23. 通知栏显示未读数和最近5条，重要优先；点击标记已读后直达主题；没有未读时显示紧凑空状态。
24. 作者可以持续编辑/软删除本人内容。教研员/管理员可以隐藏/恢复他人内容，但任何人都不能修改他人的正文或课后反思。
25. 一期保留置顶、软删除、浏览数、回复数和下载数；不做点赞、收藏、积分或排行榜。

### 四、必须遵循的技术路线

#### 1. 技术栈

- 后端：现有 `RuoYi-Vue` 中的 Spring Boot、Java、MyBatis、MySQL。
- 前端：只改 `RuoYi-Vue3`（Vue3、Vite、Element Plus）。
- 禁止修改 `RuoYi-Vue/ruoyi-ui` 旧 Vue2。
- 注释用简体中文，解释业务原因，不写机械注释。
- 保持现有项目风格，最小范围实现，不做无关重构。

#### 2. 富文本路线

项目当前已经使用：

- `@vueup/vue-quill 1.2.0`
- npm overrides 强制 `quill 2.0.2`
- 公共组件 `RuoYi-Vue3/src/components/Editor/index.vue`

一期必须优先复用 Quill 2。编码完整模块前，先做兼容门槛验证：

- table module 可初始化。
- 插入表格、增删行列、删除表格。
- HTML 保存、刷新回显、再次编辑不丢表格。
- 图片选择、图片粘贴、授权头刷新正常。
- 同页多个编辑器正常。
- `npm run build:prod` 正常。

如果验证失败，不要偷偷安装其他编辑器。先向用户报告最小复现、错误、截图和影响，并推荐只在教研活动模块回退 WangEditor；得到确认后才能引入。不要选择 Tiptap。

验证通过后，只为公共 Editor 增加默认关闭的可选配置，例如 `enableTable`、`uploadAction`、`allowedImageTypes`、`maxImageCount`。默认行为必须不变，避免影响现有页面。教研活动再通过 `ResearchRichEditor` 传入表格、10MiB图片、JPG/PNG/WebP和最多20张。

#### 3. 富文本安全

后端新建独立 `ResearchActivityHtmlSanitizer`，使用项目已有 Jsoup。必须：

- 保留安全排版、表格、图片、链接和必要 Quill class。
- 移除 script、iframe、事件属性、危险协议、任意 style、base64 图片。
- 链接只保留 HTTP(S)，统一 `noopener noreferrer`。
- 保存清洗后的 `content_html` 和纯文本 `content_text`。
- 后端再次校验正文非空、图片≤20和内容长度。

不要只依赖前端校验，也不要直接 `v-html` 展示未经后端清洗的用户输入。

#### 4. 通知模型

绝对不要直接复用或改造 `sys_notice`。它没有接收人和已读状态。

新建业务表 `biz_research_notice_recipient`，发布或再次通知时保存具体教师账号快照。按学段时通过启用 teacher 角色账号的：

```text
sys_user.dept_id → sys_dept.school_type
```

计算接收人。一个自然人的多个账号分别接收。指定教师必须由后端重新验证仍是启用 teacher 账号。唯一键为 `(topic_id,user_id)`；再次通知 upsert 并重置未读。

#### 5. 文件和链接

- 主课件用专用受控私有目录和专用鉴权下载接口，不能公开 `/profile/**` 绝对资源。
- 文件限制按 `50L * 1024L * 1024L`，同时校验扩展、MIME、文件头。
- ZIP 文件头 `PK`；RAR4/RAR5 和 7z 文件头均需验证。
- Spring multipart 建议 `max-file-size: 55MB`、`max-request-size: 60MB`，但业务仍严格 50 MiB。
- 正式 Nginx 3010 同步 `client_max_body_size 60m`；修改前必须读取真实配置并 `nginx -t`。
- 云盘只允许绝对 HTTP(S)。永久有效映射为 `expire_time=NULL`；非永久时间保存时必须晚于当前时间。
- 正文、完整 URL、提取码和绝对文件路径不得进入操作日志。

### 五、数据库设计不得偏离

新建 `sql/research_activity_v1.sql`，尽量幂等，包含：

1. `biz_research_topic`
   - 类型、标题、HTML、纯文本、通知级别/范围/学段、置顶、浏览/回复/下载数、最后互动、作者学校、软删除和审计字段。
2. `biz_research_post`
   - 主题、留言类型、HTML/纯文本、学段/年级/学期/课次类型/课次/课程标题、置顶、作者学校、软删除和审计字段。
3. `biz_research_resource`
   - 文件/链接类型、资源名称、文件原名/私有相对路径/大小/MIME、URL/提取码/过期时间/说明、访问数、排序和软删除。
4. `biz_research_notice_recipient`
   - 主题、具体用户、来源类型/学段、通知级别、未读/已读时间、通知时间；唯一 `(topic_id,user_id)`。

索引至少覆盖：主题信息流、主题留言、课程结构筛选、作者、资源归属、过期时间、通知未读。不要使用数据库外键破坏项目现有风格；在服务层校验归属。

SQL 还要幂等创建一级菜单“教研活动”和权限：

```text
business:researchActivity:list
business:researchActivity:add
business:researchActivity:edit
business:researchActivity:remove
business:researchActivity:download
business:researchActivity:notify
business:researchActivity:pin
business:researchActivity:manage
```

admin/teacher/researcher 获得 list/add/edit/remove/download；只有 admin/researcher 获得 notify/pin/manage。学生不授权。脚本末尾加入表、索引、菜单、三角色权限和重复通知组复核查询。脚本必须在本机连续执行两次验证幂等。

### 六、建议的后端结构

在 `ruoyi-business` 新增：

```text
controller/ResearchActivityController.java
controller/ResearchActivityResourceController.java
domain/BizResearchTopic.java
domain/BizResearchPost.java
domain/BizResearchResource.java
domain/BizResearchNoticeRecipient.java
domain/dto/Research*.java
domain/vo/Research*.java
mapper/ResearchActivityMapper.java
resources/mapper/business/ResearchActivityMapper.xml
service/ResearchActivityService.java
service/ResearchActivityAccessService.java
service/ResearchActivityHtmlSanitizer.java
service/ResearchActivityUploadService.java
```

接口统一前缀 `/business/research-activity`。至少实现：

```text
GET/POST/PUT/DELETE  /topics...
GET/POST/PUT/DELETE  /topics/{id}/posts 或 /resource-posts...
GET                  /search/topics
GET                  /search/resources
GET                  /notifications/summary
GET                  /notifications
PUT                  /notifications/{id}/read
PUT                  /notifications/read-all
GET                  /notification-targets/teachers
POST                 /images
GET                  /resources/{id}/download
POST                 /resources/{id}/access
POST                 /topics/{id}/notify
PUT                  /topics/{id}/pin
PUT                  /posts/{id}/pin
```

详细请求、响应、事务和权限以 `design.md` 为准。

关键要求：

- 资源搜索以“课程资源留言”为一条结果，不按文件/链接重复行。
- 先分页 resource post，再按当前页 postIds 批量查资源，禁止 N+1。
- 关键词排序：课程标题精确 > 前缀 > 包含 > 活动标题 > 反思/教师。
- LIKE 特殊字符统一转义。
- 浏览、回复、下载、访问计数用原子 SQL。
- 课程资源保存采用 multipart `payload + optional file` 和事务；数据库失败要清理新文件。
- 并发编辑资源时锁定 post，不能突破一个文件/三个链接。
- 作者可编辑本人；任何角色都不能编辑他人正文。管理角色只能隐藏/恢复/置顶。

### 七、建议的 Vue3 结构

新建：

```text
src/api/business/researchActivity.js
src/views/business/researchActivity/index.vue
src/views/business/researchActivity/detail.vue
src/views/business/researchActivity/components/TopicComposer.vue
src/views/business/researchActivity/components/PostComposer.vue
src/views/business/researchActivity/components/ResourceFields.vue
src/views/business/researchActivity/components/TopicCard.vue
src/views/business/researchActivity/components/ResourceCard.vue
src/views/business/researchActivity/components/ResearchRichEditor.vue
src/views/business/researchActivity/components/ResearchNotificationBar.vue
src/views/business/researchActivity/utils/*.js
```

页面规则：

- 主菜单来自动态菜单 SQL。
- 主题详情和全部通知使用静态隐藏路由，roles 限定 teacher/researcher/admin，activeMenu 指向主菜单。
- 首页默认“课程资源”检索，另一视图为“活动主题”。
- 课程筛选：学段→年级、学期、课次类型、第几课、作者、时间；请求防抖并取消旧请求。
- 主题详情：主题在顶、留言最新在前，筛选全部/资源/纪实/普通，置顶资源优先。
- 资源表单是一张连续表单：课程字段、富文本反思、一个文件、最多三个云盘行。
- 云盘永久有效默认开启；关闭后必须选过期时间。
- 文件下载使用授权 blob 请求；云盘新窗口打开并 `noopener,noreferrer`；有提取码才显示复制按钮。
- 过期链接有明显标识但仍可打开。

教师首页修改必须精确：在 `RuoYi-Vue3/src/views/business/teacher/index.vue` 的 `.teacher-dashboard` 内，第一个子组件渲染 `ResearchNotificationBar`，然后才是现有区域抽测评卷和课程设置。通知请求必须独立失败，不得让课程首页白屏或整体 loading。

### 八、实施顺序

严格按 `contexts/research-activity/tasks.md`：

1. 工作隔离和 Quill 2 门槛。
2. SQL、领域模型、Mapper。
3. HTML 清洗和权限矩阵。
4. 主题、课堂反思、纪实。
5. 课程资源、文件、云盘。
6. 搜索。
7. 通知。
8. Editor 可选增强和 Vue 公共组件。
9. 主页面、详情、资源表单、教师首页通知栏。
10. 配置、单测、构建、接口和浏览器验收。
11. 文档更新。

不要先把所有页面写完再补后端校验；每个阶段都要有可验证输出。

### 九、必须执行的测试

后端：

```powershell
cd RuoYi-Vue
mvn -pl ruoyi-business -am test
mvn -pl ruoyi-admin -am clean package
```

前端：

```powershell
cd RuoYi-Vue3
node --test <新增纯函数测试文件>
npm run build:prod
```

必须有专项测试：

- HTML 保留表格、移除 script/onerror/javascript/base64。
- 教师/教研员/管理员/学生角色矩阵。
- 本人/他人编辑和管理隐藏。
- 49 MiB、恰好 50 MiB、50 MiB+1 字节。
- ZIP/RAR4/RAR5/7z 文件头和伪装文件。
- 一文件+三链接成功，第二文件/第四链接失败。
- 永久、未来到期、已到期链接。
- 年级/学段、数字/专题/复习课组合。
- 学段接收人、多账号、指定教师去重、停用账号。
- 编辑不重发、再次通知重置未读、单条/全部已读。
- 精确课程标题搜索优先和结构化筛选。
- 软删除后列表、搜索、通知和下载全部不可见。

浏览器闭环：

1. 教研员进入菜单，发布按学段/指定教师的重要通知。
2. 教师登录，首页第一块看到通知，点击直达主题。
3. 教师发布活动纪实，多图保存回显。
4. 教师发布课程资源：课程字段、反思、文件、云盘、提取码、过期时间。
5. 教师编辑本人资源。
6. 通过年级、学期、第几课和课程标题搜索并下载/打开。
7. 主题和留言分别插入表格，刷新后再次编辑。
8. 学生接口验证拒绝。

截图和报告存入 `output/playwright/`，不得包含密码或 Token。Playwright 不能替代 Java 单测和构建。

### 十、性能与发布要求

- 列表默认20、最大50。
- 以2万资源代表数据执行常用搜索和 `EXPLAIN`，目标 P95 <2秒；先优化索引/SQL，不引入搜索服务。
- 本提示词首先授权本机开发、测试和文档更新，不授权 `git push`。
- 完成本机验收后先向用户汇报；只有用户在新任务中明确进入内网发布时，才按 `AGENTS.md` 对 `10.52.1.123` 备份、计算 SHA-256、执行 SQL、修改 3010 Nginx、发布新 release、探活和冒烟。
- 发布不覆盖旧 release；回滚应用时保留新表，不执行 DROP。
- 不修改 80 端口主站或其他服务。

### 十一、禁止事项

- 不要迁移、抓取或链接旧论坛数据。
- 不要创建板块表、主题状态或关闭回复功能。
- 不要给学生开放任何资源读取。
- 不要直接使用 `sys_notice` 充当定向通知。
- 不要只改前端 50MB 校验而漏掉 Spring/Nginx。
- 不要公开课件目录或把绝对路径返回前端。
- 不要把云盘提取码、富文本正文、密码、Token 写入日志、文档或提交信息。
- 不要修改旧 Vue2。
- 不要为了论坛顺手重构课程、导学单、抽测或当前热修。
- 不要未经确认安装 WangEditor、Tiptap、Elasticsearch 或其他新框架。
- 不要 push、force-push、删除分支或覆盖他人未提交改动。

### 十二、每阶段汇报格式

每完成一个阶段，用简体中文汇报：

1. 修改了哪些文件。
2. 完成了哪些需求 ID。
3. 执行了什么测试，结果和退出码。
4. 当前风险或未完成项。
5. 下一步只做什么。

最终交付必须包含：

- 完整修改文件清单。
- 四表/索引/菜单/权限 SQL 复核结果。
- 后端测试、clean package、Vue3 build 结果。
- 权限矩阵、50 MiB、XSS、云盘过期、通知和搜索验收证据。
- 浏览器闭环截图/报告路径。
- `contexts/context.md` 已同步的版本和内容。
- 剩余风险、部署步骤和应用回滚方式。

现在先执行“第一步：项目体检和工作隔离”，不要直接开始大范围编码。

---
