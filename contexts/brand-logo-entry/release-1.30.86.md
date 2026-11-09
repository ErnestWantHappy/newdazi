# 首页品牌停留节奏微调验收（1.30.86）

日期：2026-11-09  
环境：130 正式站（Linux / systemd / Nginx）  
状态：**1.30.86 已上线并通过真实教师浏览器验收**

## 本次目标

- 保留 Logo 桌面 `220px`、手机 `128px` 的放大尺寸。
- 居中停留由约 2 秒缩短为约 1.2 秒。
- 保留起始全屏遮罩、移动时内容渐显、透明底大图和刷新不重播。

## 实现

- `BrandLogoLanding/index.vue` 中入场后的计时器由 `2200ms` 改为 `1400ms`。
- 入场淡入约占 0.2 秒，因此肉眼可见的居中停留约为 1.2 秒。
- 未增加入场/退出时间，移动过程仍为 `860ms`，遮罩仍用 `780ms` 渐隐。
- 只改一个前端文件；后端、数据库、权限和业务数据未变。

## 发布信息

- 正式 release：`/data/apps/xueyeceping/releases/20261109_brand_logo_hold12_v1/frontend`
- 首页 SHA-256：`be732a834ec7e589cc1cb9102e753e899d777079e5c9dd654efc2d501d6df30e`
- 制品：`frontend.tar.gz` 43,340,491B、SHA-256 `66220d2c200367f4fba4c53e2bebbe3cfae134c14369be1370ca090e54f64a8d`、820 文件
- 后端：`releases/20261109_choice_media_v1`，JAR `2b9c65e9`，MainPID `3019157`、NRestarts 0
- 整库备份：`/data/backups/xueyeceping/20261109_120529_20261109_brand_logo_hold12_v1/database.sql.gz`，70,925,485B，SHA-256 `9f307d75ccef89b4dcf5755673f47251672c363bd4327874d7bb5ef7c7a6d64a`
- 产品更新：`164` / `1.30.86` / `PUBLISHED`，真实教师接口可见

## 正式浏览器结果

使用真实教师账号访问 `http://10.52.1.130`：

- 桌面 `1440×900`：Logo `220px`，居中停留 `1240ms`，遮罩起始 `1`。
- 手机 `390×844`：Logo `128px`，居中停留 `1260ms`，遮罩起始 `1`。
- 移动过程中遮罩连续渐隐，首页内容逐渐出现。
- 标题仍为“信息科技学业测评平台”，登录页与教师首页 Logo 可查看透明底大图。
- 刷新后动画计数为 0，不重复播放。
- 0 页面脚本异常、0 控制台错误、0 个 5xx。

截图与机读结果：`output/playwright/brand-logo-hold-12-20261109/`。

![桌面居中](../../output/playwright/brand-logo-hold-12-20261109/desktop-center.png)

![手机居中](../../output/playwright/brand-logo-hold-12-20261109/mobile-center.png)

## 回退

将 Nginx root 切回 `releases/20261109_brand_logo_entry_v2/frontend`，更新 164 转 DRAFT；`1.30.85` 的 2 秒版本仍保留。无需重启后端、不整库回灌。

## 未验证边界

- 真实教室不同 Chromium/Edge 版本及低配设备未做整班实测。
- 操作系统“减少动态效果”仅按代码路径保留，未在真机逐项截图。
- 本轮验证覆盖真实教师桌面与手机模拟视口，不包含学生角色的完整登录动画复验。
