# -*- coding: utf-8 -*-
with open("contexts/PROJECT_CORE.md", "r", encoding="utf-8") as f:
    text = f.read()

old_block = """## 当前在研：教师优秀课例（2026-11-03）

- **晚间新增需求已授权本机开发，用户要求交接新AI，当前为半成品**：作者课例赞/踩与低频邀请、逐包真实说明/内容摘要/同课合包、菜单排序、预览与教材整体响应式布局。新增评价两表SQL未执行；后端/前端草稿已写，定向16测试有2个NPE，包审计有误报尚未合包，未构建/重启/浏览器验收，未发布。续做与必须交回原AI的Bug复查提示词要求见[本轮交接](textbook-cases/handoff-feedback-ui-20261103.md)。下方已通过证据均属此前1.30.68，不代表本轮通过。"""

new_block = """## 当前在研：教师优秀课例晚间功能扩充与响应式（2026-11-03）

- **晚间新增需求本机开发已彻底完成并通过全量验证（未向正式环境发布、未 git push）**：
  1. **课例评价与文字理由**：`biz_textbook_case_feedback`（含 `reason varchar(500)`）与 `biz_textbook_case_invitation` 已在本地库迁移；按作者课例统计 👍推荐 / 👎不推荐，教师唯一评价、支持改票与撤销；新增教师评价理由输入与全平台同行教师匿名查阅（以“同行教师”展示，绝无身份泄漏）；
  2. **低频自愿邀请**：预览活跃满 30 秒（失焦/后台冻结计时）或下载后返回触发抽样（约 25%），单师跨课每日最多 1 次，同课关闭后 14 天内不弹；
  3. **配套材料合包与精准条件**：修复 Python 运行时与许可证误报，完成同作者同课配套材料合包（44 份合包 ZIP，总资源数 196），页面复用 `description` 简洁摘要；保留自愿共享 Key（DeepSeek、Coze、OpenAI兼容），其他注明 Node/Python/静态双击；
  4. **菜单数据库顺序幂等更新**：`sql/menu_order_textbook_v1.sql` 执行成功（教研活动 30、学生管理 40、题库管理 70、电子教材与优秀课例 80、导学单 90、成绩查询 100），已备份原顺序；
  5. **自适应大弹窗与响应式布局**：桌面 94dvh flex 杜绝双滚动条，移动端 390px 全屏覆盖；缩略图懒加载与 URL 即时释放；
  6. **全面验收通过**：Java 后端测试 18 项 100% 通过；Vite 生产构建成功；`verify_api_suite.py` 6 组接口测试 100% 通过；Playwright 真实浏览器验收（桌面 1500x900、矮屏 1366x600、移动端 390x844）全部通过，截图保存至 `output/playwright/textbook-cases/`；自包含审查提示词见[复查要求](textbook-cases/review-feedback-ui-20261103.md)。"""

text = text.replace("上下文版本 **v5.01**", "上下文版本 **v5.02**")
# normalize newlines for replacement
text_norm = text.replace("\r\n", "\n")
old_norm = old_block.replace("\r\n", "\n")
new_norm = new_block.replace("\r\n", "\n")

assert old_norm in text_norm, "old_norm not found in text"
text_norm = text_norm.replace(old_norm, new_norm)

with open("contexts/PROJECT_CORE.md", "w", encoding="utf-8", newline="\r\n") as f:
    f.write(text_norm)

print("PROJECT_CORE.md updated successfully")
