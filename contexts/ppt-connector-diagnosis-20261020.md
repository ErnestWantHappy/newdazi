# PPT连线旧预览排查（2026-10-20）

**已按用户授权完成该教师2022级2班三道PPT作业共108份预览重算并验收（首批37份，补处理71份）。**正式版本仍为1.30.52，无部署或服务重启，无结构迁移。下文排查段保留处理前事实，最终状态见末尾。

- 已核对130活动后端仍为20261017_seat_score_v1，后端与Nginx均active。
- 反馈教师681108/朱仁敏（user_id=1885、学校108），截图课程990614《9.体验算法控制》，题990831，2022级2班。真实教师登录及practical-submissions接口确认名单38人、37份附件，与截图一致。
- 截图学生陈梓悦，答案610969、附件48504、文件9.pptx，36,957字节。教师鉴权下载与服务器原件副本SHA-256一致。
- 此预览创建于2026-10-15 08:55:31，更新于08:55:34，早于当天晚间1.30.51的PPTX兼容修复；此前只精确重算了课程715四班40份，不含本课程。
- 旧页图与用户图1一致：开始线偏折、免票等箭头错位、结束线倾斜。使用现有Java PptxPreviewNormalizer生成临时副本，经130独立LibreOffice进程转PDF后，连线恢复到图2的连接关系。主代理和独立视觉复核均确认；判断框文字仍分为两行，与源文件截图存在差异，未宣称像素一致。
- 本题二班37份、三班35份当前答案已查得；尚未逐份试转，不可宣称72份全部修复。

处理前建议（本轮已完成二班）：备份后精确重算二班37份派生PDF/页图，复核原件哈希、答案成绩、版本指针，再以教师页面验收。三班35份不在本次授权范围，未改。

证据位于本机output/ppt-connector-20261020/：targets.txt、source.pptx、before.pdf、before.jpg、fixed.pptx、fixed.pdf、fixed.png、api-verification.json。试转临时文件在服务器/tmp/ppt-connector-20261020，不属于正式预览。

## 二班正式重算验收

- 用户明确要求重新转换该教师该班全部作业。现场重新前检：2022级2班38人，已交37份，附件全部为当前PPTX，原预览均在10月15日晚修复前生成；不处理三班或其他课程。
- 先整库备份及归档37份原件、PDF、旧页图和附件元数据，再执行`sql/practical_pptx_class2_recompute_20261020.sql`，精确重置37行，既有教师重试API接受37份，最终37份PDF均变化、预览与页图均success/page-jpeg-v4。复跑重置SQL为0行。
- 查出附件48497与其他旧版本存在同哈希页图缓存，直接复用会保留旧图。因此从当前正式JAR提取原有依赖与PracticalPageRenderer，独立维护进程对37份本轮PDF逐份重新渲染，共37张页图；无修改渲染算法、无需安装依赖。使用新的`ppt-recompute-20261020-<附件ID>-page-jpeg-v4`目录，避免浏览器旧地址缓存。仅以`sql/practical_pptx_class2_fresh_pages_20261020.sql`定向切换37个页图地址。
- 37份原件SHA-256逐一保持；答案分数、提交时间、作品状态/成绩快照、当前版本指针、lock_version及附件文件身份快照均保持。正式教师API下38人名单/37份作品正确；37个PDF与37个新页图鉴权下载均200，图片哈希与服务器逐一相同。
- Chromium真实登录朱老师→二班批改→陈梓悦作品，预览正常加载，页面异常/HTTP5xx/测试写分均0；主代理及独立视觉复核均看到连线恢复。37份文件均已技术验收，未逐份对照PowerPoint人工验图，两个判断框文字仍有换行差异。此次为派生预览维护，不生成新产品更新版本。
- 后端与Nginx均active，MainPID/NRestarts与处理前相同，未部署/重启。无需教师重新评分或学生重交，只需刷新批改页面。

### 备份与精确回退

服务器备份目录：`/data/backups/xueyeceping/20261020_ppt_class2_recompute_v1`。

| 内容 | 字节 | SHA-256 |
| --- | --- | --- |
| database.sql | 440492483 | `8029ae83fbc44172a38c4dabab4b479ad8dbf16a74843fcedbd05d13d81a3a58` |
| affected-previews.tar.gz | 3440781 | `0732acc820b46f7a998e8998250bbcbf12f489be30667dd12cb10c8cd35772d3` |

原附件元数据见`attachments.before.tsv`，保护快照见`protected-state.json`，原件和旧PDF哈希见`backup-state.json`。必要时先备份当前状态、确认附件仍为本轮版本，再用`sql/practical_pptx_class2_pages_rollback_20261020.sql`撤回本轮37个页图地址；若PDF也需恢复，仅从定向归档恢复这些附件对应PDF/旧页图，并按元数据逐行还原预览字段。不得整库回灌覆盖期间新成绩，不恢复原件或删除新业务记录。旧页图已在备份归档中保留；服务重算可能覆盖原页图路径，回退时应从归档恢复，不可仅切回旧地址。新目录保留。

完整执行证据见`output/ppt-connector-20261020/`的recompute-preflight.json、recompute-backup.json、trigger-result.json、files-verification.json、publish-pages-result.json、final-verification.json、online-browser.json及online-teacher-grading.png/online-ppt-page.png。本次仅定向重算，无业务源码修改，不需要重新编译/单测；服务、鉴权接口及真实桌面浏览器已实测，未进行手机布局或机房并发测试。

## 用户再次反馈后的范围纠正与补处理

用户再次反馈截图为陈诚《8.pptx》，“某景区规定身高不足120cm”题。首轮将“这个班全部”错误限定到课程990614单题37份，遗漏同班其他课程；这不是首轮已修复的9题再次出现错线。本轮按原授权补查该教师该班所有当前PPT/PPTX，得到三道作业总计108份：

| 课程/题 | 文件 | 当前作品 | 最终success/v4 |
| --- | --- | --- | --- |
| 714/4416 顺序结构 | 多个PPTX文件名 | 34 | 34 |
| 990576/990705 分支与双分支结构 | 8.pptx | 37 | 37 |
| 990614/990831 体验算法控制 | 9.pptx | 37 | 37（首轮完成，本轮保护复核） |

补处理71份先重新整库备份、归档原件及旧预览，执行`sql/practical_pptx_class2_remaining_recompute_20261020.sql`重置71行，既有教师API分别接受34/37份。71份PDF均更新，均success/v4；再用正式JAR页图渲染器从本轮PDF逐份生成71张全新地址，避免旧同哈希/浏览器缓存，以`sql/practical_pptx_class2_remaining_fresh_pages_20261020.sql`定向切换。首次切换旧地址比较门禁未命中（旧渲染版本地址已随正常转换升级），0行更新；核对当前转换后地址再执行，71行成功，不存在重复触发转换。

71份原件哈希逐一保持，答案成绩/提交时间/状态及版本指针保护快照保持。142个PDF/新页图鉴权下载全部200，图片哈希与服务器逐一相同；重置SQL复跑0行。首轮37份版本/成绩及新页图地址再次复核保持，最终全班三题108/108成功。

Chromium真实教师登录后分别打开714、990576的二班批改页，确认新地址图片加载、无pageerror/HTTP5xx及测试写分。截图题8的开始/输入身高直线、左右分支及结束汇合，主代理与独立视觉复核均未见原错折/斜接；顺序结构样本未见错位。连线颜色偏浅、判断框换行仍存在，未改学生样式，未宣称所有作品与PowerPoint逐像素一致。未做手机布局/并发专项测试，无应用代码修改/部署/重启/产品更新版本。教师刷新或重新进入对应题即可查看。

补处理备份：`/data/backups/xueyeceping/20261020_ppt_class2_remaining_v1`。

| 内容 | 字节 | SHA-256 |
| --- | --- | --- |
| database.sql | 440496491 | `1da6e813aaab7f07d08fcf92c1f10f0e73cd358d8dd4b4a29c134ca0977ae1f0` |
| affected-previews.tar.gz | 5520110 | `b5190782c8d768f0289dd2895fc9495965336653c8d35f4c993ca229ca24effc` |

回退仍按附件当前版本逐一核对并先备份现状。页图地址撤回脚本为`sql/practical_pptx_class2_remaining_pages_rollback_20261020.sql`；完整预览回退还需从本轮定向归档恢复71份旧PDF/页图，并依据attachments.before.tsv/protected-state.json逐行恢复预览字段及renderer_version，不恢复原件或整库回灌，不触及首轮37份。详证见`output/ppt-class-all-20261020/`中的recompute-preflight.json、recompute-backup.json、trigger-result.json、files-verification.json、final-verification.json、all108-final-inventory.json与两份真实教师截图。
