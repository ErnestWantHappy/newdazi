# 选择题图片功能 · 独立设计原型

2026-11-06。仅为方案审阅，不是已实现的正式功能。所有业务接口、数据库、正式Vue页面、路由和部署均未改动。

## 打开与审阅

双击 [index.html](index.html)，即可查看五页统一入口；也可以直接打开任意一页，不需要启动前后端、登录账号或联网。请在浏览器中打开HTML，不要仅查看HTML源文件。

| 设计页 | 原型 | 桌面PNG | 390px补充图 |
| --- | --- | --- | --- |
| UI1 录题 | [新增/编辑](01-editor.html) | [设计图](screenshots/01-editor.png) | [窄屏](screenshots/01-editor-390.png) |
| UI2 题库 | [列表与完整预览](02-bank.html) | [设计图](screenshots/02-bank.png) | [窄屏](screenshots/02-bank-390.png) |
| UI3 Excel | [预览和修正](03-import.html) | [设计图](screenshots/03-import.png) | [窄屏](screenshots/03-import-390.png) |
| UI4 学生 | [两种题面作答](04-student.html) | [设计图](screenshots/04-student.png) | [窄屏](screenshots/04-student-390.png) |
| UI5 组卷 | [候选/已选/预览](05-designer.html) | [设计图](screenshots/05-designer.png) | [窄屏](screenshots/05-designer-390.png) |

补充状态：[保存结果提示](screenshots/01-editor-saved.png)、[导入图片归属修正](screenshots/03-import-fix.png)、[学生选中答案](screenshots/04-student-selected.png)。

## 可操作内容

- UI1：点击图片入口选择本机图、在题干/选项区域粘贴截图、拖入图片；点放大/Esc关闭、更换/删除；答案切换；保存后演示“已保留历史版本”，点“查看新题”在示例题库看到结果。
- UI2：搜索示例题；查看不同题目的完整预览；跳编辑；加入课程跳UI5并带入题目。
- UI3：选择/拖入Excel会明确提示“当前原型不解析，仍显示示例结果”；点第12题“修正”，选择图片归属，必要时本机替换，应用后49/1变50/0，才能确认；有本题文字/答案编辑入口；确认后展示示例结果和返回题库入口。
- UI4：两题分别选A—D；点放大不会选答案；完成后提交得到示例分数并锁定选项。
- UI5：添加/移除题、上移/下移、单题改分/示例批量设5分、完整预览；保存只提示本机演示结果。

所有页面底部持续标识“交互设计稿/示例数据/不上传不写题库”。分页、其他题型和历史成绩入口仅说明保留的产品入口，不连接真实系统；没有把缺失功能假报为真实成功。

## 与未来正式产品的差别

- Excel的50题/76图、49正常/1待修正是人工构造的界面状态，清单只放6条代表行。没有真正运行POI，没有证明Excel/WPS兼容率或50题处理耗时。
- 图片只用浏览器Object URL本地预览，离页释放；没有服务器上传、持久化、权限校验、图像压缩或真实失败恢复。示例SVG用于教学图示和本机原型，未来首期上传格式建议PNG/JPEG。
- 原型保存文字/答案摘要使用独立的`choice-ui-demo-saved-question`本机存储键，演示新题可找回，不涉及账号或平台数据；不是后台幂等/历史版本实现，新增本机图片不保证跨页恢复。
- 在线修改演示当前题面，不是持久化批次与并发revision。真实批次恢复、事务和防重要求见专项设计。
- 原型有“放大原图/关闭”，未完整实现正式图片缩放、平移、触控与焦点陷阱；正式组件需按方案验收。
- 组卷左右顺序按本轮用户要求：左候选、右已选/预览。当前Vue源码是另一顺序，本轮未改正式页面。
- 题库“加入课程”是本轮建议的快捷跳转，源码目前只有查看/预览/编辑/删除，不把建议标成已存在能力。

## 视觉来源

阅读并参照：

- `RuoYi-Vue3/src/views/business/question/index.vue`：980px编辑弹窗、表单与筛选。
- `RuoYi-Vue3/src/views/business/question/components/QuestionCard.vue`：紧凑卡片、摘要/教师/年级与文字操作。
- `RuoYi-Vue3/src/views/business/lesson/designer.vue`：题库候选、已选、分值/排序和预览。
- `RuoYi-Vue3/src/views/student/index.vue`：智慧课堂页头、理论题网格、蓝色选中态和绿色提交。
- `RuoYi-Vue3/src/assets/styles/variables.module.scss`：#304156侧栏、#409eff主色、Element Plus状态色。

不使用远程字体、CDN、生成式照片或网络请求；教学图示为可读的本地SVG。五页可独立审阅，手机改为单列，内容不强挤横向滚动。

## 本轮验证与复现

使用仓库已有Playwright与已安装Chromium，无安装新依赖。当前 [prototype-validation.json](prototype-validation.json) 为 **48项通过、0页面脚本异常**，包括五页桌面截图、390/320无水平溢出、本机图片实际解码、粘贴/拖拽入口、自动另存提示与结果定位、导入修正门禁、放大不误选、示例评分、排序和分值联动（排序保留已填分值）。

粘贴/拖拽自动化使用浏览器ClipboardEvent/DragEvent模拟，未在各操作系统做真实剪贴板权限兼容测试；未将其宣称为全机房实测。视觉上已查看五张桌面图、窄屏录题及统一预览。

这是**静态原型验收**，不是真实教师录题/Excel解析/学生评分/数据库迁移或生产验收。没有为了设计稿运行后端业务测试。

从仓库根目录复现（只写本目录）：

```powershell
python -X utf8 docs/choice-question-images-ui/build_prototypes.py
node --check docs/choice-question-images-ui/app.js
node docs/choice-question-images-ui/render_prototypes.mjs
```

`build_prototypes.py`生成HTML和SVG，`render_prototypes.mjs`用file URL渲染并导出PNG及验证JSON。前者不会覆盖手写app.js/style.css。`update_plan.py`仅用于本轮更新文档并保存v1归档，复看原型无需运行。

## 方案文档

- [优化后的技术方案](../choice-question-images-implementation-plan-20261106.md)
- [Excel图文导入专项设计](../choice-question-excel-image-import-design-20261106.md)
- [第一轮历史方案](previous-plan-v1.md)（旧版手动另存/ID列已废止，不作为开发要求）

审阅重点：录题轻量入口是否合适、Excel纠错是否清楚、自动另存提示是否易懂、学生图题是否可读、组卷信息密度是否合适。用户确认前不进入业务开发。
