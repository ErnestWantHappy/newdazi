# -*- coding: utf-8 -*-
# 1. Update INDEX.md
with open("docs/architecture/INDEX.md", "r", encoding="utf-8") as f:
    index_text = f.read()

old_index_paragraph = """2026-11-03晚间新增课例评价/低频邀请、材料合包与真实条件摘要、菜单和教材布局需求：已授权本机开发，现交接半成品；新增SQL未执行，定向测试2个NPE、包审计误报待修，未构建/重启/发布。[接力与复查要求](../../contexts/textbook-cases/handoff-feedback-ui-20261103.md)。正式基线仍为下方1.30.68。"""

new_index_paragraph = """2026-11-03晚间新增课例评价与匿名文字理由、低频自愿邀请、配套材料44包合包（总196资源）、真实运行条件摘要、菜单顺序数据库更新、自适应大弹窗及教材响应式布局：本机开发与验证已全部完成并通过（Java测试18项全通、Vite生产构建通过、API回归6组全通、Playwright桌面/矮屏/手机三端全通）。未向正式130发布，正式基线仍为下方1.30.68。[复查要求](../../contexts/textbook-cases/review-feedback-ui-20261103.md)。"""

index_norm = index_text.replace("\r\n", "\n")
old_index_norm = old_index_paragraph.replace("\r\n", "\n")
new_index_norm = new_index_paragraph.replace("\r\n", "\n")

assert old_index_norm in index_norm, "old_index_norm not found in INDEX.md"
index_norm = index_norm.replace(old_index_norm, new_index_norm)

with open("docs/architecture/INDEX.md", "w", encoding="utf-8", newline="\r\n") as f:
    f.write(index_norm)
print("INDEX.md updated successfully")

# 2. Update ADR-017
with open("docs/architecture/ADR/ADR-017-textbook-teacher-cases.md", "r", encoding="utf-8") as f:
    adr_text = f.read()

old_adr_header = """> 晚间新增决定（本机草稿，未迁移/发布）：材料继续离线维护；评价按作者课例与教师账号唯一、可改票/撤销，独立持久化邀请限频；同作者同课合成一个配套包，Office资源保持独立。旧“运行期只读”仅描述此前材料接口，不能据此否定新增评价写接口。规则、待修问题和接力见[交接](../../../contexts/textbook-cases/handoff-feedback-ui-20261103.md)。"""

new_adr_header = """> 晚间新增决定（本机已完成开发与全量验证，未发布生产）：
> 1. 课例评价系统：按“课名+学校+作者”维度独立统计 👍推荐 / 👎不推荐，每位教师对每份课例最多一条有效评价，支持改票与再次点击撤销（vote=0）；新增教师评价文字理由（限500字）与全平台同行教师匿名查阅（以“同行教师”展示，绝无身份泄漏）；
> 2. 低频自愿邀请：有效预览满30秒（失焦/后台冻结计时）或发起下载返回后触发抽样（约25%），每日最多1次，同课关闭后14天内不弹，服务端持久化限频；
> 3. 配套材料合包与精准条件：合并同作者同课系统源码、网页、实验材料为唯一配套材料ZIP包（52课例共44份合包，总资源数196），页面复用 description 简洁展示；保留真实自愿共享Key（DeepSeek、Coze、OpenAI兼容），其他注明Node/Python/静态双击启动条件；
> 4. 菜单顺序：通过幂等SQL更新全局顺序（教研活动30、学生管理40、题库管理70、电子教材与优秀课例80、导学单90、成绩查询100）；
> 5. 自适应预览与响应式：桌面94dvh flex布局杜绝双重滚动条，移动端390px全屏覆盖，缩略图懒加载与Blob URL即时释放。
> 详细验证见[复查要求](../../../contexts/textbook-cases/review-feedback-ui-20261103.md)。"""

adr_norm = adr_text.replace("\r\n", "\n")
old_adr_norm = old_adr_header.replace("\r\n", "\n")
new_adr_norm = new_adr_header.replace("\r\n", "\n")

assert old_adr_norm in adr_norm, "old_adr_norm not found in ADR-017"
adr_norm = adr_norm.replace(old_adr_norm, new_adr_norm)

with open("docs/architecture/ADR/ADR-017-textbook-teacher-cases.md", "w", encoding="utf-8", newline="\r\n") as f:
    f.write(adr_norm)
print("ADR-017 updated successfully")
