# 晚间课例功能独立复核（2026-11-03）

> 历史交接/复核记录：所列缺陷已随1.30.69修复并正式上线；当前状态、真实验证及回退见[本轮发布](release-feedback-fix-20261103.md)。原记录保留供追溯。

结论：功能主体已实现，但不能按“全面收口、全量验证通过”验收。独立复核确认6项P1、1项P2；本轮只审查、复现及写报告，不修业务代码，不向正式环境写入或发布。

证据：`output/textbook-cases/independent-review-results.json`、`independent-review.log`、`review-unit-tests.log`，复现脚本`output/textbook-cases/review_readonly.py`。数据库使用只读事务和派生表数据；浏览器评价/邀请写请求全部拦截模拟，不修改真实评价。

## 已确认问题

### 1. [P1] 同课例有其他教师记录时，本人的不推荐返回未评价

- 位置：`TextbookCaseFeedbackMapper.java:16`。
- `MAX(CASE WHEN user_id=本人 THEN vote ELSE 0 END)`会对本人-1和其他人的0取MAX，结果为0。其他记录即便只是邀请产生的未投票行也会触发。
- 在本机MySQL执行同构SQL、派生表模拟甲-1乙+1：赞1、踩1，但甲myVote=0（应为-1）。这不是总数重复，而是本人态度丢失，导致按钮不高亮、再次点踩不能按预期撤销。
- 最小修复：非本人分支用NULL，再COALESCE到0，或独立查询本人行。补多人及他人仅有邀请记录的SQL级测试；只mock service不能覆盖。

### 2. [P1] 撤销后的理由残留，下一次投票会重新提交旧理由

- 位置：`CasePanel.vue:183`的Object.assign；`CaseVotes.vue:156`会自动带item.myReason。
- 实际只读API已确认没有理由时myReason字段可能整个省略。Object.assign不会清除旧字段。
- 浏览器模拟同样响应形状：先有理由、撤销评价，页面仍显示“修改理由”；随后再推荐的请求仍携带已撤销的旧理由，可能重新公开。
- 最小修复：规范响应始终含myReason:null，或前端合并前默认清空，并在撤销时明确清理本人理由。补撤销后直接再投票与刷新前后对比测试。

### 3. [P1] 清空文字理由并保存，旧理由仍保留

- 位置：`TextbookCaseFeedbackService.java:44-46`、`TextbookCaseFeedbackMapper.java:24`。
- 编辑理由删空后，service将空串转成null；Mapper将null解释为保留旧reason。页面显示保存成功但原文字仍存在。
- 同构SQL已验证null分支返回旧reason，代码链路明确。
- 最小修复：区分“快速投票不改理由”和“显式清空理由”，例如DTO字段/存在标记，明确清空时写NULL。补保存空串、纯空格、快速改票保留理由三种场景。

### 4. [P1] 联网识别把https地址当注释删除，已影响10个材料包的条件说明

- 位置：`scripts/textbook/consolidate_case_packages.py:55`。
- `re.sub(r'//.*$', '', ...)`会从https://中的双斜线开始删除整行余下部分，因此普通CDN脚本和API请求均被判为不联网。
- 独立调用check_network，`<script src="https://cdn.example.com/library.js">`及`fetch("https://api.example.com/v1/chat")`都返回false。
- 已逐包检查生成物，10份包存在外部script但使用条件未注明联网，例如王诗莹“数字讲解员成长工坊”、杨晶茹“认识决策树”、郑东旭物联网知识挑战等。完整名单见结果JSON。
- 最小修复：避免对字符串中的URL套用行注释正则，结合真实资源引用识别；重新审核说明并生成包、清单、导入本机。只改扫描代码不会自动修复已生成的ZIP/数据库文案。

### 5. [P1] 预览失败也能被计为成功停留并触发邀请

- 位置：`CasePanel.vue:163-172`，与CasePreview错误状态未向父级传播有关。
- resumePreviewTimer仅检查弹窗/资源存在及mediaError（仅媒体错误）。PDF preview-info或页图失败时，重新聚焦仍开始计时。
- 浏览器模拟preview-info失败→blur/focus→推进测试时钟31秒→关弹窗，确实发出invitation请求。邀请响应在本次测试中拦截为false，没有真实写入。
- 最小修复：单独维护“当前资源已成功呈现且页面可见/聚焦”的状态，失败/切文件时清除；计时状态与子组件加载/失败同步。补失败、后台加载完成及重试成功的测试。

### 6. [P1] 写理由保存失败，弹窗仍关闭，重新打开丢失草稿

- 位置：`CaseVotes.vue:167-169`以及`openReasonDialog():163`。
- emit后立即关弹窗，不等待异步保存结果。再次打开按已保存item.myReason覆盖输入。
- 浏览器模拟保存接口失败，确认弹窗关闭；重开后“本次新增但保存失败的草稿”消失。
- 最小修复：保存成功才关闭，失败保留输入并在弹窗内显示错误/重试；请求中防重复提交及意外关闭。

### 7. [P2] 手机预览不是全屏，向下偏移6vh并越出视口

- 位置：`CasePanel.vue:324`与`src/assets/styles/ruoyi.scss:66-68`。
- 全局`.el-dialog:not(.is-fullscreen)`的margin-top:6vh!important比局部`.case-preview-dialog`更具体，局部margin:0!important不能覆盖。
- 实测390×844：弹窗y=50.625、height=844，底部894.625，超出视口约51px；320×568同样下移约34px。顶部露出原页面、外层仍需滚动。截图`output/textbook-cases/review-layout-390x844.png`、`review-layout-320x568.png`。
- 最小修复：窄屏使用Element Plus fullscreen属性，或用准确且足够具体的局部规则覆盖全局；验收断言需要检查x/y/底边，不仅是宽度大于350。

## 双角色规则复核

- 用户再次强调：有些账号同时为教师和教研员。
- 前端canVote判断roles.includes('teacher')；后端requireVotingTeacher对SysRole集合查teacher是否存在，包含researcher不会自动拒绝。当前源码符合双角色应放行、纯教研员应拒绝的要求。
- 本机只读查询同时绑定teacher/researcher角色的账号数为0，未以真实双角色账号完整登录验收。不能据本机无样本推断正式环境没有双角色，不能把纯教研员403测试当成双角色测试。

## 验证与交付报告的可信边界

- 独立重跑Maven定向18项：0失败、0错误；日志review-unit-tests.log。这些测试没有覆盖上述真实SQL多人分支和浏览器异常场景。
- 本机只读确认52课例、196资源、评价主键重复组0。未独立重跑全部合包工具和所有角色链路。
- 浏览器检查1500×900、1366×600、390×844、320×568；未见本次目标页整页横向溢出，弹窗body无重复纵向溢出，但手机外层偏移已确认。未检查4K、学校机房、真实弱网及全资源下载。
- 未复跑另一AI的API脚本/冒烟脚本，因为其中会在真实教师账号下留下评价；本次采用拦截写请求的独立复现。
- 未独立重跑前端生产构建。附件所写“guide-sheet-template-preview.spec.js 5项＋login-preview.spec.js 4项”与当前package.json的build:prod不一致，实际入口是test:login、test:seat-helper、vite及发布门禁。现有证据不足以按附件断言该9项测试来自本次构建。
- 附件备份SHA256记录错误：文件大小105,279,415B符合；实际计算及同目录SHA256SUMS均为`e0ad4f3f788fd8d45c019241b62d1752ded46adedc31bca04798e07e5df725e7`，不是附件/复查提示词写的`e0ad4f3fbdf6aa5d8481ffb79109315579f1ec52c93842c525f0a0c49be47137`。未做恢复演练，不能据哈希一致宣称备份已验证可恢复。
- 本轮未触碰正式环境，未改业务源码、SQL或材料包。建议先完成以上修复与针对性回归，再考虑发布。
