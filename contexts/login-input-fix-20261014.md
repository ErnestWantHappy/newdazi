# 登录输入迟显修复与历史构建清理

2026-10-14，用户审核推荐方案后实施，130 正式前端 **1.30.48** 已上线。

## 原因与实现

10 月 9 日入口拆分和 WebP 引用未完整保留到后续源码：10 月 10 日及最近三天版本入口重新成为约 3.56MB，并引用 705KB PNG Logo、831KB JPG 首图。浏览器低性能模拟下出现 13.4 秒长任务。单独对照旧优化页仍有约 1.2 秒输入绘制等待峰值；暂停轮播并模拟空展厅返回后明显改善，因此同时处理入口资源和输入期间的非必要工作竞争。

- `RuoYi-Vue3/src/main.js` 移除 VForm 静态导入，`src/utils/vform.js` 统一按需注册；`permission.js` 在路由确认前等待，覆盖独立导学单、学生首页和课程内嵌面板。兼容生产 UMD 命名空间导出，失败报错并允许重试，路由错误清除遮罩。
- 登录使用 WebP Logo 和三张兜底图。`loginIdleWork.js` 在输入停止至少1.2秒、页面可见且非中文组合/登录/验证码/选校过程中，才分批执行展厅工作；迟到接口结果也排队。输入暂停自动轮播，十五秒后的图片扩展每次只增加一张。三个展厅以 `v-memo` 避免账号/密码更新引发无关子树重绘。
- `npm run build:prod` 先跑7项登录定向测试，再校验入口和登录资源：入口gzip超过650KB、重新引用旧PNG/JPG或丢失WebP Logo时拒绝制品。当前gzip约559KB。唯一维护源码是当前 `RuoYi-Vue3/src`，禁止从历史 `output/*/build` 覆盖或发布。
- 无新依赖、接口、权限、计分或结构变更。普通课程练习逻辑未修改。

## 验证

- 生产构建成功；7项登录定向与12项既有批改刷新检查，共19项通过。
- 真实生产组件配模拟接口：中文组合态、迟到展厅响应、连续输入、空闲恢复、登录零VForm请求、三角色跳转、教师和学生实际VForm输入控件、验证码弹窗均通过，异常0、真实业务写入0。
- 正式教师在候选页和上线后均通过真实登录、选择校区、进入工作台；更新列表HTTP200/业务200可见 `126/1.30.48/PUBLISHED`，异常和HTTP5xx均为0。
- CPU减速15倍、4Mbps/60ms、120次输入，原正式页/新正式页各一轮：账号框进入DOM 21,380→12,031ms；最大长任务13,399→5,036ms；输入事件后两帧等待P95 189→65.1ms、峰值1,011.7→94.3ms。两帧等待是绘制调度近似，不能当成物理按键到屏幕的完整测量或真实机房结论。候选页同条件峰值115.2ms。原始结果位于 `output/typing-latency-20261014/browser-{current,candidate,online-fixed}.json`。
- 正式只读统计：打字题584道，平均257字、最大767字。按这两个长度模拟真实学生组件（CPU×15、每轮40次连续输入及5次退格），两帧等待P95分别273.2/439.4ms，峰值732.2/907.9ms；输入/退格后文本正确，异常0、真实业务写入0，证据 `output/typing-latency-20261014/exercise-profile.json`。确认练习区也存在低性能延迟，但不等于已证明每项开销的贡献；提出减少统计/逐字高亮更新、交卷前精算的最小第二阶段，等待用户再次确认后实施。实际输入法、云桌面及整班高峰未验。

## 发布、备份和回滚

- 当前前端 `/data/apps/xueyeceping/releases/20261014_login_input_v1/frontend`；后端仍 `20261013_python_v3_full_v1`、PID2308320不变，仅Nginx reload，三服务active。
- ZIP 43,347,277B，SHA-256 `ad4f4ec1eded0f44fc65553f1d6ef5e62d7d9f50ede8df1ccf9a8e1bdf21e79a`；首页SHA-256 `c59bce0b628c490c3de8f4e16de8551a3ac963f7eb13edc98ac9a4bfece50dfa`。
- 备份 `/data/backups/xueyeceping/20261014_login_input_v1`，整库403,011,220B，SHA-256 `cce4bc53999777fd77b7e55dfd23ee3f27d6b0675ddceb36b957be85426f5e34`；保存Nginx、unit和共享env。基线比较、候选语法及完整备份通过后才切换，后检失败自动恢复Nginx。
- 仅执行产品登记 `sql/login_input_latency_release_20261014.sql`，不执行结构迁移或成绩SQL，不重启后端。
- 回滚：确认当前前端仍为本release，将备份 `nginx.before` 恢复到 `/etc/nginx/sites-available/xueyeceping-130`，`nginx -t`后reload，校验首页回到 `9a7fb4bcf3192196e9f5093441d9deb9c9918c2835121701f3f7383db561f6e6`；更新1.30.48撤DRAFT。旧前端 `20261014_grading_ui_v5` 保留；不需后端/业务SQL回滚，不回灌整库。

## 清理

用户明确授权后，核对运行服务工作目录、启动命令、环境文件及Nginx真实配置，先归档、检查归档可读、计算SHA-256，再删除五个无引用release：`20261008_login_input_latency_v1`、`20261009_entry_split_v1`、`20261009_slowness_fix_v1`、`20261010_login_render_fix_v1`、`20261012_audit_fix_v1`。

归档在本轮备份 `obsolete-releases/`，逐项摘要/删除状态见远端 `manifest.json` 和本机 `output/typing-latency-20261014/cleanup-online.json`。当前、上一版前端、当前后端和助手/环境文件依赖的 `20261008_seat_install_v1` 均保留。新release已合并旧哈希静态文件；清理后旧入口JS、当前首页、验证码均HTTP200，三服务active。旧静态资源不等于保留旧发布入口。

本地两份旧构建 `output/130-opt-20261009/build`、`output/login-input-latency-20261008/build` 已移出工作区。归档校验至 `D:/dmwprogram/newdazipingtai/backups/20261014_login_input_local_cleanup/` 后，可逆迁移至同目录 `*-extracted`。未遍历node_modules Junction，当前源码依赖正常。两份ZIP摘要分别为 `7c34f12c9894716d4e4e8bc4f7c85f871d90efb92ff209d171c14e2c7695ca42`、`ba64860838f2cf61a7e44efa7a3a7b487f75419b5b0f8fa0fae1e0937efb6673`。本地递归删除被自动审批以“blocked by policy”拒绝，故采用恢复性迁移；未永久删除。其他Git worktree、未提交改动、业务原件和旧备份未清理。

## 后续边界

- 反馈学校刷新后以一两台实际学生机复测，再观察整班同开。本轮没有生产并发压测。
- 练习逻辑未改，低配模拟已复现约0.7～0.9秒峰值，第二阶段等待用户确认；不能以登录修复宣称练习卡顿全部解决。
- 助手仍是最新0.2.1，下载包及启动环境文件在旧release；迁至固定共享目录应配套正常后端发布，本轮未为清目录重启。
- 同期另有后端 `page-jpeg-v4` 本机候选，未在本轮发布；其原预留1.30.48已被本次正式版本占用，后续须重新查库分配版本。
- AOCI未同步，相关索引需后续复核。
