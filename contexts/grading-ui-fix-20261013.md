# 教师操作题预览与五星布局修复

2026-10-14，130 正式站点已发布并完成真实教师只读验收。

用户反馈：部分操作题页图底部无法继续滚动；星级评分仍频闪，尤其第五颗星。上一轮仅缓存 texts 数组的修复不足以解决悬浮文案改变布局的问题。当前 Element Plus 实现只在 modelValue 变化时同步 currentValue，不能将新建 texts 数组直接认定为重绘根因。

仅修改 `RuoYi-Vue3/src/views/business/teacher/grading.vue`。预览父容器改为可收缩的 flex 区域并限制溢出，页图滚动区独占剩余高度，工具栏保持原高度。评分侧栏保持固定宽度并计入 padding，分项名称与输入分行，星标单独占行；删除悬浮行内文案，禁用星标放大动画。整题已选星数/折算分数提示保留，分项合计、满分、清零和原分数计算及提交逻辑保持。

验证：

- `npm run build:prod` 成功，日志 `output/grading-ui-build-20261013.log`。
- `node --test RuoYi-Vue3/src/views/business/teacher/__tests__/gradingRefresh.test.js`：12/12 通过。
- Chromium 挂载真实 grading.vue，复用项目 Vite 插件、Vue/Pinia/Element Plus，模拟业务接口和 2200px 长页图，无真实成绩写入。1024×768、1366×768、1920×1080 均有可用内部滚动，末尾图像底部小于滚动区底部，滚动区底部不超过主工作区。
- 分项第五颗星右侧边缘连续 30 次鼠标移动，aria-valuenow=5 且 boundingBox 不变；整题/分项点击五星通过；1366 宽度等待 11 秒跨一次名单校准后，整题五星及边界保持；页面异常为 0。
- 脚本/结果：`output/grading-ui-regression.cjs`、`output/grading-ui-regression-result.txt`；三尺寸截图：`output/grading-ui-{1024,1366,1920}.png`。临时页面入口验收后清理，不进入正式制品。

正式验收：真实教师账号登录 130 成功，平台更新列表可见 `1.30.47/PUBLISHED`；进入真实课程批改页后，1366/1024 窗口的 `.grading-main` 高度分别为 447/373，内部滚动区 `scrollHeight=4387` 且 `overflow-y:auto`，页面异常和 HTTP 5xx 均为 0。验收使用只读浏览器操作，没有提交或修改真实成绩。未更改 PDF/LibreOffice 渲染、作品或后端服务；AOCI 未同步。
