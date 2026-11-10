# Python 批改页窄屏修复发布验收（1.30.89）

状态：**130 正式已上线，产品更新 167/PUBLISHED**

## 问题

1.30.88 真实验收发现：手机 390px 下批改页仍维持“学生名单 + 代码预览 + 打分区”三栏，内容被压缩成难以使用的窄条。该问题不在原始审查报告中，但在本轮已修改页面的真实用户验收中发现。

## 修复

- 宽度不超过 900px 时改为“学生名单 → 代码预览 → 打分区”纵向排列。
- 学生名单固定约 220px 高并在内部滚动。
- 筛选控件整行显示，批改期限信息纵向排列。
- 代码预览与打分区整行宽度，评分按钮可正常触控。
- Python 系统判题信息在窄屏上下排列。
- 桌面 1440px 三栏布局不变。

仅改前端：`grading.vue`、`TeacherPythonSubmissionPreview/index.vue` 和对应测试桩；不改后端、数据库结构、权限或成绩规则。

## 正式版本

| 项目 | 结果 |
| --- | --- |
| 前端 release | `/data/apps/xueyeceping/releases/20261109_grading_mobile_v1/frontend` |
| 后端 | 继续 `20261109_python_grading_fix_v1`，未重启 |
| 首页 | `db0e4c385b8172d5d7ab2b1f75223255bd04f1b188022150053053bd59214212` |
| 制品 | `frontend.tar.gz` 43,441,015B，SHA-256 `3e4c9d4efcf899e4255e5748589a9904f7df9c97fb52ca2276c120088ed6fb84`，820 文件 |
| 服务 | MainPID `3093098`、NRestarts 0、首页/验证码 200 |
| 产品更新 | `167 / 1.30.89 / PUBLISHED`，真实教师可见 |

## 备份

- 目录：`/data/backups/xueyeceping/20261109_234706_20261109_grading_mobile_v1`
- 整库：53,200,867B
- SHA-256：`095b6398fb338bb8ea59d00b25fc2f89c6eedc38c61c6e3f2afcb8f3a47347e5`
- 含 `nginx.before`、`service.before`、`env.before`、`jar.before`、`index.before.html`、`release.before.json`。

## 验证

- 前端生产构建通过：登录 9、机位 9、题库工作台 25 项门禁及发布检查通过。
- `gradingRefresh` 12 项通过。
- 本地构建接正式 API 预检：桌面与 390px 均 0 横向溢出，三块主区域窄屏宽度均大于 300px。
- 130 真实教师浏览器：1440/390 通过；45 人名单、林煜琦代码、系统判题 40/40、判题时间、通过用例和批量按钮正常；0 页面脚本异常、0 个 5xx。
- 批量按钮因该历史课程已过批改期限而按业务规则禁用；未点击采用，未写正式成绩。
- 课例未知来源模拟：显示“来源未标注”，无“优秀课例”，无评价投票。
- 证据：`output/playwright/python-grading-1.30.89-20261109/`。

## 回退

1. 确认当前 Nginx 根仍为 `20261109_grading_mobile_v1/frontend`。
2. 恢复本备份 `nginx.before`，`nginx -t && systemctl reload nginx`。
3. 将产品更新 167 转 DRAFT。
4. 不重启后端、不整库回灌。

