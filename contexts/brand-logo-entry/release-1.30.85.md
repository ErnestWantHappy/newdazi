# 首页品牌入场体验优化验收（1.30.85）

日期：2026-11-09  
环境：130 正式站（Linux / systemd / Nginx）  
状态：**1.30.85 已上线并通过真实教师浏览器验收；后续 1.30.86 已将居中停留缩短为约 1.2 秒，当前以 [1.30.86 验收](release-1.30.86.md) 为准**

## 用户目标

- 登录成功首次进入首页时，先把品牌 Logo 放大居中展示。
- Logo 再大一些，居中停留约 2 秒。
- 刚进入时首页内容完全隐藏；Logo 移向左上角的过程中，内容逐渐出现。
- 保留浏览器标题统一、Logo 点击查看透明底大图、刷新/站内跳转不重播。

## 版本关系

1.30.84 先上线品牌标题、透明 Logo 大图和约 1 秒的首次登录动画，首页 SHA-256 为 `6b1912ebad1a0cfc35c755fc7a4aae02b63da282c870f5886577a9446d4fcf8d`，制品 SHA-256 为 `5afa0828fb738eed592a75e61a41459a90e99c7972deec0b4a85c67ff1d30f43`。

1.30.85 只基于 1.30.84 前端替换 `RuoYi-Vue3/src/components/BrandLogoLanding/index.vue`，增加更大尺寸、2 秒停留和全屏遮罩渐隐；未修改后端、数据库、权限或业务数据。

## 实现要点

- 桌面 Logo 最大 `220px`；手机按短边计算，实测 `128px`。
- 入场完成后通过 `setTimeout` 停留 `2200ms` 再启动移动，实测居中停留约 2 秒。
- 新增 fixed 全屏浅色遮罩，起始不透明度为 1，动画期间阻挡页面交互。
- Logo 向 `[data-brand-logo-target]` 移动的 `860ms` 过程中，遮罩用 `780ms` 渐隐，首页内容逐渐出现。
- 品牌动画标记只消费一次；刷新和站内路由跳转不重复播放。
- `prefers-reduced-motion: reduce` 时直接跳过动画。

## 发布信息

- 正式 release：`/data/apps/xueyeceping/releases/20261109_brand_logo_entry_v2/frontend`
- 首页 SHA-256：`0e252cb6d8e319829173ea1c0fcc4f8aab274304c71ee54922c564982e7f7f52`
- 制品：`frontend.tar.gz` 43,342,265B、SHA-256 `369c4dd07780196bfa16f90e8f77dc2d56cde12ed3723cfa2c9cc6a98c67c98a`、820 文件
- 后端：`releases/20261109_choice_media_v1`，JAR `2b9c65e9`，MainPID `3019157`、NRestarts 0
- 整库备份：`/data/backups/xueyeceping/20261109_111458_20261109_brand_logo_entry_v2/database.sql.gz`，70,878,487B，SHA-256 `30aea88da7e349d9fdb71a7d4ed46b0670203e52ded79d1b3122787e224548e2`
- 产品更新：`163` / `1.30.85` / `PUBLISHED`，真实教师接口可见

## 正式浏览器结果

使用真实教师账号访问 `http://10.52.1.130`：

- 桌面 `1440×900`：Logo `220px`，居中停留 `2020ms`，遮罩起始 `1`。
- 手机 `390×844`：Logo `128px`，居中停留 `2060ms`，遮罩起始 `1`。
- 移动过程中遮罩有连续中间透明度，结束小于 `0.03`，首页内容逐渐出现。
- 登录页和教师首页标题均为“信息科技学业测评平台”。
- 登录页与教师首页 Logo 弹窗背景为 `rgba(0, 0, 0, 0)`，图片加载完整，可查看透明底大图。
- 刷新后动画计数为 0，不重复播放。
- 0 页面脚本异常、0 控制台错误、0 个 5xx。

截图与机读结果：`output/playwright/brand-logo-entry-20261109/`。

![桌面居中](../../output/playwright/brand-logo-entry-20261109/desktop-center.png)

![手机居中](../../output/playwright/brand-logo-entry-20261109/mobile-center.png)

## 发布过程说明

第一次 `20261109_brand_logo_entry_v1` 的 staged 首页仍为 `6b1912eb`，实际是 1.30.84 旧包，只完成了目录切换和更新 163 登记，不能作为本轮效果上线证据。

随后创建 `20261109_brand_logo_entry_v2`，首页为 `0e252cb6`，正式浏览器验收通过后才确认 1.30.85 上线。误发候选目录保留用于审计，不删除。

## 回退

- 只回退本轮 2 秒/遮罩体验：将 Nginx root 切至 `releases/20261109_brand_logo_v1/frontend`，更新 163 转 DRAFT，更新 162 保持 PUBLISHED。
- 完整回退到 1.30.83：切至 `releases/20261109_lesson_designer_layout_v2/frontend`，更新 162、163 均转 DRAFT。
- 两种回退都只需 `nginx -t && systemctl reload nginx`，不重启后端、不整库回灌。

## 未验证边界

- 真实教室不同 Chromium/Edge 版本及低配设备未做整班实测。
- 操作系统“减少动态效果”仅按代码路径保留，未在真机逐项截图。
- 本轮验证覆盖真实教师桌面与手机模拟视口，不包含学生角色的完整登录动画复验。
