# AI 辅助开发提示词大纲 (v2.8.1 多项修复与优化)

> **背景说明**：这是一个基于 RuoYi-Vue3 和 Spring Boot 的信息科技学业测评平台。以下包含了 7 个待修复/优化项的详细拆解与实现思路，请 AI 逐一执行。

---

## 任务 1：修复跨校历史数据回填失败 (最高优)
**问题背景**：由于 `biz_lesson` 表早期的历史数据 `creator_id` 字段大量为 NULL，导致先前基于 `creator_id = sys_user.user_id` 的关联更新 SQL 失败，老师依然看不到早期创建的课程。
**实现步骤**：
1. 请提供重新回填 `biz_lesson.dept_id` 的 SQL 语句。由于截图显示 `create_by` 字段有真实的用户名（例如手机号 `19157727791`），需要改为通过 `create_by = sys_user.user_name` 进行关联更新：
   ```sql
   UPDATE `biz_lesson` bl 
   INNER JOIN `sys_user` su ON bl.create_by = su.user_name 
   SET bl.dept_id = su.dept_id 
   WHERE bl.dept_id IS NULL;
   ```
2. 提示用户在 Navicat 中单独执行这段 SQL，即可恢复丢失的课程。

---

## 任务 2：教师角色强密码策略与首登强制改密 (高优)
**问题背景**：平台需要加强教师账号的安全性，密码必须满足强密码规则（至少6位，包含大小写字母、数字和特殊字符），并且第一次登录或当前密码仍为弱密码时，必须强制重定向到修改密码页面。学生角色无需此限制。
**实现步骤**：
1. **密码规则修改**：修改前端 `RuoYi-Vue3/src/views/system/user/profile/resetPwd.vue` 中的校验规则，加入强密码的正则表达式，确保修改密码时验证通过。
2. **登录拦截逻辑**：
   - 在前端 `login.vue` 登录成功后，如果是教师角色，即刻校验其输入的明文密码是否符合强密码正则。
   - 如果不符合强密码条件，则将全局状态（例如 pinia 里的 user store）标记为 `needChangePwd: true`。
   - 在路由拦截器 `permission.js` 中，如果读取到 `needChangePwd`，强制重定向到 `/user/profile` 页面，提示“为保证账号安全，请先修改为强密码（含大小写字母、数字及特殊符号，至少6位）”，并且阻止其点击其他菜单。
   - 修改完成并成功提交后端后，清除前端的 `needChangePwd` 状态。

---

## 任务 3：修复成绩汇总表课程总分与平均分计算差异 (中优)
**问题背景**：成绩查询页面（`score/index.vue`）的“学生成绩汇总表”中，课程总分变化了（加入了表现分），但最右侧的平均分没有变化。
**实现步骤**：
1. 定位 `score/index.vue` 第 868-884 行的数据聚合逻辑。
2. 目前 `sumTotal += (s.totalScore || 0)` 累加的是**理论+操作+打字**的分数，而 `sumFinal += (s.finalScore || s.totalScore || 0)` 才是包含**平时表现分**的最终总成绩。
3. 修改第 884 行平均分的计算逻辑，将原先的 `filteredAverage = count > 0 ? Math.round(sumTotal / count) : 0;` 修改为基于 `sumFinal` 计算，确保平均分与显示的课程总分保持严谨的一致性。
4. 注意所有分数显示的四舍五入规则并应用 `Math.round`。

---

## 任务 4：全局学号数值类型排序 (中优)
**问题背景**：前端因为 JS 类型推断，将 `student_no`（学号）推断为了字符串，导致排序错乱（比如 1, 10, 11, 2）。需要改为严格按数值大小升序。
**实现步骤**：
1. 检查和修改以下三个场景的 SQL 查询或前端排序逻辑：
   - 批改操作题的学生列表（涉及 `TeacherGradingController` 或相关前端页面 `grading.vue`）
   - 课堂表现分的学生列表（`ClassroomPerformanceController` 或 `performance.vue`）
   - 成绩查询页面的理论测试详情（这部分由 `BizStudentAnswerMapper.xml` 里的 `ORDER BY` 控制或前端 `ScoreQuery.vue` 控制）
2. **后端方案**：在 MyBatis 的 XML 中，使用 `ORDER BY CAST(s.student_no AS UNSIGNED) ASC` 替代普通的 `ORDER BY s.student_no`。
3. **前端方案**：在涉及到对应列表渲染的 Vue 组件的 `table-column` 中，配置 `sortable` 并书写自定义 `sort-method`，用 `parseInt(a.student_no) - parseInt(b.student_no)` 实现正确排序。

---

## 任务 5：根据理论题存无动态隐藏理论测试详情表 (体验优化)
**问题背景**：成绩查询页面如果当前选择的课程都没有理论测试题目，依然会显示出一个空的理论测试详情表，影响阅读。
**实现步骤**：
1. 在 `score/index.vue` 的请求（如 `getQuestionAnalysis` 或 `getStudentAnswerMatrix` 返回结果后）中，判断当次所有的题库数据中是否存在选择题（`choice`）或判断题（`judgment`）。
2. 在对应的 `<el-table>` 渲染标签上，通过 `v-if="hasTheoryQuestions"` 的方式进行包裹。
3. 如果 `hasTheoryQuestions` 为 false，展示一段空状态提示或直接将其 DOM 元素隐藏。

---

## 任务 6：部门管理页面 UI 与文案深度定制为“学校管理” (低优/改造)
**问题背景**：RuoYi 原生“部门管理”需要进一步深度定制教育局-学校架构，隐藏不需要的输入框。
**实现步骤**：
1. **文案替换**：在 `views/system/dept/index.vue` 中，将所有的“部门管理”、“添加部门”、“部门名称”批量修改为“学校管理”、“添加学校”、“学校名称”。
2. **隐藏多余字段**：在新增/修改弹窗的表单 (`<el-form>`) 中，找到并删除（或 `v-if="false"` 隐藏）“负责人”、“联系电话”、“邮箱”等表单项代码（包含校验规则）。
3. **上级学校强限制**：
   - 目前系统的上级部门应只能选“小学”、“初中”、“高中”分类，教育局节点为根。
   - 在弹窗的树状下拉选择器（`<el-tree-select>`）中，设置只能通过前端数据过滤逻辑，或者在表单选择校验中，确保用户所选父级 `parent_id` 对应的节点属于这些允许的分类集合，不符合则抛出提示 `ElMessage.warning('只能选择小学、初中或高中作为上级！')`。
4. **自定义学校代码**：
   - 在弹窗表单中新增字段 `<el-input v-model="form.schoolCode" placeholder="请输入自定义学校编号" />`。
   - 确保 `rules` 中配置必填或正则校验，并在请求 `/system/dept` `POST` 与 `PUT` 接口时传递给后端。

---

## 任务 7：排查并修复部门（学校）管理点击查询报错 (最高优)
**问题背景**：用户反馈点击部门管理的查询按钮会报错。
**实现步骤**：
1. 定位 `views/system/dept/index.vue` 的搜索查询函数 `handleQuery()` 以及向后端的请求动作。
2. 可能是因为后端实体类（`SysDept.java`）中刚刚加的 `school_code`，或者其它相关返回缺少字段导致的 MyBatis 报错。
3. 或者是由于树形数据处理失败，需优先让用户贴出浏览器的 F12 Response 报错信息，或者后端控制台 `org.springframework...` 的异常栈，明确具体错误是由 SQL 语法引起（例如 MyBatis xml 层没有把 `d.school_code` 包裹进去）还是前端找不到属性。
4. （备用猜测）如果是在查询框里输入的内容传给了原有的 `deptName`，后端按 `like` 查一般不报错；报错往往是由于之前手动在 sys_dept 中添字段却没有同步修改 `selectDeptList` 的底层 xml SQL。请仔细核对 `SysDeptMapper.xml`。
