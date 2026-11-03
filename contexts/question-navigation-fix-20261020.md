# 题库切页空白修复（2026-10-20，仅本地）

用户视频显示教师首页切到题库、再回首页后内容区持续空白，菜单和地址已更新。

## 原因与最小修改

`RuoYi-Vue3/src/layout/components/AppMain.vue` 使用 `Transition mode="out-in"` 包裹 `KeepAlive` 和路由组件。题库 `src/views/business/question/index.vue` 的 `DataLabEditor` 位于页面根 div 之外，形成 Fragment 多根组件；该组件无法正常承接元素切页动画，离开时可导致切页停滞。

仅将 `DataLabEditor` 移入原根 div，保留原 ref、saved 回调、编辑器内容和挂载时机。不修改路由容器、缓存策略、接口、权限或数据库。

## 已验证事实

- 使用 `webapp-testing` 技能，以 Playwright 运行真实 AppMain、教师首页、题库及实验题编辑器；请求、用户、缓存 store 为模拟数据，通知栏替身。未连接真实业务服务。
- 修改前运行相同切页路径，已复现返回教师首页后“课程设置”无法出现的超时失败；另一次题库内容亦未出现。不能据此推断所有空白均为同一原因。
- 修改后 `node scripts/verify_question_navigation_20261020.mjs` 通过：1440px / 390px × 缓存开启 / 关闭，共4组，各连续往返3次。4组均打开并取消真实数据分析实验题弹窗，页面异常0、非元素根节点动画警告0。
- 已查看桌面与窄屏首页截图。这里只验证隔离环境中的切页与弹窗，未验收真实学校数据、完整登录、其他菜单或所有题库写入操作。
- `npm run build:prod` 通过，包括7项登录测试及前端发布资源检查。
- 产物：`output/playwright/question-navigation-20261020/result.json`、同目录四张截图；构建日志 `output/video-issue-20261020/frontend-build.log`。视频抽帧仅供本机定位。

## 发布与下一步

未发布，130仍为1.30.52。无SQL、无新依赖、无后端重启要求；未来发布仅需隔离构建并更新前端。当前工作区含多项其他未发布变更，不能直接将当前整包视为本修复制品。发布后应在反馈浏览器用真实教师账号重复“首页→题库→首页”确认。回退为恢复上一版前端。
