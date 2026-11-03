# 题库画流程图题：拍照 + AI 生成标准答案（任务）

> 状态：**130 正式 1.30.56 已上线、真实照片端到端验证通过、发布已登记**。日期 2026-10-25。编号对应会话任务清单 #1–#7。

## P0 方案与文档

- [x] 确认方案 A（照片 → AI → 节点+边 → 平台排版 → 直接填画布）
- [x] #1 落盘 requirements / design / tasks（本专题）
- [x] 决策要点记入 design.md 第 9 节；如需独立 ADR 可后续补，不阻断发布

## P1 后端生成链路

- [x] #2 `POST /business/flowchart/question/ai-generate-answer`：读图 + 复用 `chatJson`/教师 Key/端点白名单
- [x] #2 专用 Prompt 与 `{nodes,edges}` 输出契约解析（去重、类型归一、悬空边丢弃、越界兜底）
- [x] #3 `FlowchartLayoutService` 坐标自动排版（最长路径分层 + 分支展开 + 环/孤立兜底 + 坐标自适应）
- [x] 产出统一过 `normalizeDocument`，非法即拒；接口只返回 JSON 不写库
- [x] #4 `FlowchartLayoutServiceTest` 7 项定向单测通过

## P2 前端

- [x] #5 设计器“AI 生成标准答案”按钮 + 选图上传 + 调用 + 画布回填
- [x] #5 “请核对后保存”横幅 + 覆盖二次确认 + 费用/需配 Key 提示 + warnings 提示
- [x] #5 保存沿用 `saveTeacherConfig` 与 `config_revision`

## P3 测试与验收

- [x] business 编译通过、`mvn -pl ruoyi-admin` 制品经 class 注入校验其余条目不变
- [x] Vue3 隔离前端 `npm run build:prod` 通过（登录测试与发布门禁）
- [x] #6 真实照片端到端：130 本机以郑东旭账号（百炼 qwen3.7-plus）调用，识别 7 节点/7 连线、类型正确、无告警
- [x] #6 服务探活：systemd 切换 COMPLETED、三服务 active、`:3009`/`/prod-api` 200、启动日志无类加载异常、新端点与现有流程图接口回归无 404/500
- [ ] #6 手写或复杂交叉照片的识别效果仍需抽样（当前以打印体样例为准）
- [x] 线上真实反馈修复：含循环回边的教材二分查找图不再挤成一行（改 BFS 分层 + 终点沉底 + 加大间距），1.30.57 已发布并用同一照片线上端到端复验（11 节点 / 纵向 8 层 / 每层≤2 节点 / 无告警）
- [x] 连线交叉修复：后端输出节点显式宽高 + 每条边正交折线与固定锚点（相邻层 Z 形、同层绕下、回边走侧通道），1.30.58 已发布；线上端到端 7 节点/7 连线全部带折线与锚点、6 层、每层≤2
- [x] 判断分支端口修复：判断框多条正向分支按目标位置分别使用左/右锚点，不再共用底部中点（贴近课本画法），1.30.59 已发布；线上端到端确认 `_left`/`_right` 各 1 条

## P4 文档与发布

- [x] #7 制品隔离：后端 `08dce49→458e94f`（仅 FlowchartController + 新增 3 类）、前端 research-copy 基线叠加 3 文件 `ee215ccc`，不含 166 项未验收改动
- [x] #7 整库备份 487,661,077B（SHA `1d0013cf…`）+ service/nginx/env/config.before + 自动回滚
- [x] #7 PROJECT_CORE、架构 INDEX、RELEASE_LOG 已同步
- [x] #7 平台更新 134/1.30.56/PUBLISHED 且真实教师 `/business/platform-update/list` 可见；登记 SQL `sql/flowchart_ai_answer_release_20261025.sql` 复跑唯一
- [x] #7 发布评估：纯应用层、无表结构变更；回退点为 1.30.55（`08dce49`/`6233b36`），无业务回滚 SQL
