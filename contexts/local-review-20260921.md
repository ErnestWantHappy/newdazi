# 2026-09-21 本地修复与工作区审查

## 范围和结论

本轮仅检查和修改本地代码，没有连接服务器、执行数据库SQL、写真实成绩、重启现有服务、提交或推送。保留其他AI的导学单修改、原有上传测试产物及deploy的pyc变化。DATA_LAB仍是未完成的新增功能；本轮对其采取拒绝访问的安全隔离，不代表完成业务集成。

## 连续加分

- 原调用链：PerformanceSection自动保存后发出saved，score/index.vue接handleQuery，全量summary加载后重新构造名单，同时更新统计和图表。重建列表引发排序变化和滚动丢失。
- 现按studentId和lessonId原位更新表现分和总分，不重新请求名单；row-key固定学生身份。修正学号排序收到row却当字符串比较的问题。
- 同行请求串行合并，确认已发出的快照；请求期间继续加分会补发，失败保留待保存状态。切班/卸载使旧响应失效。
- 现有历史记录里的人工批改刷新修复属于另一条调用链；本轮开始时该saved到handleQuery链仍存在。没有证据证明昨日浏览器实际加载的具体版本。

## Python题单

- 单题、批量和推荐添加统一走ensureEditableVersion。事务内先锁共同题单主行，再读取版本和分配MAX+1；发布使用同一把锁。草稿读取及序号查询采用锁定读，避免可重复读事务使用旧快照。
- 已发布版本不直接编辑，克隆草稿时复制快照/测试点/题目关联；已有草稿复用。同版本移出后再加入复用旧快照，不删除快照和测试点，因此也不会刷新成题库当前内容。
- MAX+1本身不解决并发，本轮依靠共同父锁及事务串行化。数据库唯一键仍为最终保护。没有真实MySQL并发验收，不能把mock单测视为数据库锁实测。
- 当前代码已经统一为题单版本模型；extension仅发现旧数据删除兼容SQL，未发现独立新增入口，不能声称验证了已下线的加练新增链。
- 移除普通题单对biz_data_lab_config的动态JOIN，避免新表未迁移导致普通题单查询失败，以及当前配置把历史快照动态改成DATA_LAB。

## DATA_LAB问题与处置

### 高

- 身份：studentId来自SecurityUtils而非请求body，但questionId/sourceId未校验分配班级和版本；教师菜单权限没有约束作品学校/班级。服务类现统一denyAll，实际方法安全代理测试确认所有接口在访问Mapper前拒绝。
- 图表：原ResourceAccessService允许所有学生/教师读取datalab路径，没有核验submission所属人。现归一化路径后优先拒绝整个/profile/datalab/，不会落入其他资源放行分支。/common/resource/view先鉴权再解析文件；/profile直出由Spring Security拒绝。未实际重启后端/对真实账号发请求。
- 业务闭环：配置未进入发布快照，历史题单可能跟题库配置变动；进度/首页/教师统计仍查普通Python进度，实验提交不能计入；加入和发布也仍要求普通判题VALID/测试点。以上尚未实现，安全门禁必须保留，不能仅删除denyAll启用。
- Worker：原主线程执行可被死循环卡死。现每组件独立Worker，运行/预览30秒、初始化180秒超时销毁，可重新创建。真实浏览器配替身解释器验证死循环隔离及恢复；未用真实Pyodide完成Excel/画图验收。
- 文件：补充xlsx真实POI解析、10MiB上限、文件名和规范路径校验；图表限制Base64长度、PNG魔数、真实解码、4096边长/1600万像素。文件失败不再吞异常，数据库事务回滚时清理生成图表。未做大型压缩炸弹或文件系统压力测试。

### 中

- MySQL唯一键允许多条NULL来源。新建表SQL改source_type/source_id NOT NULL，服务拒绝非BASE_VERSION和空/非正来源。没有执行SQL；CREATE TABLE IF NOT EXISTS不会修改已有表。已有NULL或重复数据须先前检、备份并按真实来源治理，不能批量填0或直接删除。
- 重交会清空旧评分，避免新作品沿用旧分。教师评分与学生重交的版本冲突保护尚未完整实现，目前由门禁隔离。
- 运行库默认仍依赖jsDelivr；新增VITE_PYODIDE_BASE_URL支持完整本地运行库目录，VITE_PYODIDE_FONT_URL支持中文字体。未下载运行库/字体，未验证内网访问或中文显示，默认不保证中文不出方块。
- 切题/卸载/缓存页面离开会终止Worker并清缓存、销毁编辑器；异步旧结果不回填。清理matplotlib figures和执行结果proxy，下载Blob URL撤销；未做长时间内存曲线测试。

### 低

- 原本地验证脚本包含密码常量和目标缺失仍SUCCESS，已改环境变量读密码及失败断言；资源调试不再输出完整URL/浏览器console。脚本含真实写提交/评分操作，本轮没有执行。
- seed脚本固定题号/版本并删除快照，已整体注释为不可执行历史样例，不得作为正常迁移运行。
- 原上传测试产物和pyc仍在工作区，保留且不纳入本轮业务修复；发布/提交时应单独核对。

## 课程设计器和其他修改

BizLessonServiceImpl仍严格要求practical/PYTHON配置存在、enabled=1、validationStatus=VALID。课程设计器资源列表没有提前完整拦截，旧题缺config仍会拒绝；不应伪造VALID绕过。前端提前提示有价值，但必须同时包含启用状态并保留服务端复核，本轮未改业务规则。

导学单designer、ImageAddWidget、designerReady、main注册、formJsonBridge测试及文档属于原定点移植；保留原实现，未发现足以直接修改的新确定性问题。ImagePreview的受控资源URL拼接保留；DATA_LAB图片访问仍受后端门禁约束。

## 文件归属

- 加分：score/components/PerformanceSection.vue、score/index.vue、score/__tests__/performanceSave.test.mjs、performanceBrowser.mjs。
- 题单：PythonPracticeService.java、PythonPracticeMapper.java/XML、PythonPracticeEditTest.java。
- 实验后端：DataLabServiceImpl.java、DataLabMapper.xml、ResourceAccessService.java及三个相关测试；保留前AI新增Controller/domain/IDataLabService/Mapper API。
- 实验前端：pyodideRunner.js、pyodideWorker.js、DataLabExperiment/index.vue、师生pythonPractice.vue；保留新增dataLab API。
- SQL与工具：data_lab_v1.sql、data_lab_seed_test.sql及7个DATA_LAB本地调试脚本。
- 文档：PROJECT_CORE、架构INDEX、Python专题requirements/design/tasks及ADR-013、本报告。

## 验证与下一步

- Maven业务模块及依赖编译通过，PythonPractice*Test、ResourceAccessServiceTest、DataLab*Test共16项通过。日志output/local-review-maven.log。
- Node加分和导学单本轮所选测试28项通过（并非重复宣称原导学单完整36项）；日志output/local-review-node.log。
- Vue3生产构建通过；日志output/local-review-build.log。
- 浏览器：60人列表滚到底连续加到3分，名单请求1次、保存2次、scrollTop保持2224，页面异常0。output/playwright/performance-continuous-score.png和output/local-review-browser.log。
- Worker使用替身解释器验证30秒终止及重建；不是实际pandas/matplotlib/openpyxl集成验收。
- 不执行SQL、不重启服务；Java更改需要以后重新构建并启动本地后端才生效。当前浏览器验收只启动并关闭了测试自己的127.0.0.1:5198临时Vite服务。
- 下一步优先在隔离测试库验证题单并发/版本克隆。DATA_LAB必须先补来源权限、不可变配置及文件快照、评分并发和统计口径，再解除门禁及完成真实运行库验收。当前不适合把全部工作区直接打包发布。
