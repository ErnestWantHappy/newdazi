# 验收记录（2025 宁波优秀课例）

> 所有命令在本机 `D:\dmwprogram\newdazipingtai\newdazi` 执行；正式验证经 123 跳板在 130 上以 `127.0.0.1` + Host 头执行。

## 1. 盘点与整理

| 命令 | 结果 |
| --- | --- |
| `python scripts/textbook/ningbo2025_inventory.py` | 2172 文件 / 19,941,483,508 字节 / 13 个压缩包；退出码 0 |
| `python scripts/textbook/ningbo2025_extract.py` + Bandizip 解 4 个 RAR | 13 包全部解出；zip 中文名 UTF-8/CP936 回退；RAR5 用 Bandizip（bsdtar 会静默漏文件） |
| `python scripts/textbook/ningbo2025_html_audit.py` | 71 个 HTML 入口：STANDALONE 28、WITH_ASSETS 40、SERVICE 1、UNCERTAIN 2 |
| `python scripts/textbook/ningbo2025_prepare.py plan` | 94 候选、上架 90；逐文件去向统计见 `output/textbook-cases-2025/resource-decisions.json` |
| `... stage --offset N --limit 12`（8 批）+ `assemble` | 355 文档 + 59 视频 + 19 HTML + 41 包 = 474 资源 |
| `python scripts/textbook/ningbo2025_repack.py` | 补 23 个 <100KB 的小包（94 个文件），合计 497 资源 |
| `python scripts/textbook/ningbo2025_prepare.py verify` | 课例 90、文件 823、问题 **0**（逐个比对 bytes 与 SHA256） |

## 2. 自动化检查结论

- 每个来源文件都有去向：`RESOURCE 355`、`PACKAGE_MEMBER 1237`、`EXCLUDED_RUNTIME_DIR 179`、`EXCLUDED_NOT_PACKAGED 94`（后被 23 个补包收编）、`EXCLUDED_EXTERNAL_LINK 54`、`EXCLUDED_LARGE_TOOL 51`、`EXCLUDED_STUDENT_WORK 30`、`EXCLUDED_ADMIN_FORM 7`、`EXCLUDED_PERSONAL_DATA 7`、`DUPLICATE_FILE 7`、`EXCLUDED_CLASS_RECORDING 6`、`EXCLUDED_VIDEO_OVERSIZE 3`、`EXCLUDED_TEMP_FILE 1`。
- 收录视频 59 个全部 ≤100,000,000 字节；`EXCLUDED_CLASS_RECORDING` 6 个（含 2.4GB、1.8GB、0.96GB 等）；超限 3 个；学生实名视频 5 个排除。
- 目标课次唯一：90 份课例的 `book_code + section_key` 前检全部命中 1 条 LESSON（本机与 130 均通过）。
- 导入幂等：本机重复执行 SQL 课例仍 142、资源 693，重复 0、孤儿 0；130 重跑补包 SQL 后 142/693。

## 3. 后端单元测试

```
cd RuoYi-Vue
mvn -q -pl ruoyi-business -am test -Dtest=TextbookCaseControllerTest \
  -Dsurefire.failIfNoSpecifiedTests=false -DforkCount=0
```

`ruoyi-business/target/surefire-reports/com.ruoyi.business.controller.TextbookCaseControllerTest.txt`：
`Tests run: 11, Failures: 0, Errors: 0, Skipped: 0`。新增 2 项：HTML 沙箱头（含「不得出现 allow-same-origin」）与学生 403、非 HTML 资源拒绝。

## 4. 本机接口验收

`python scripts/textbook/ningbo2025_verify_api.py` → **25 项通过、0 失败**，证据 `output/textbook-cases-2025/api-verification.json`。覆盖：

- 教师登录、课例汇总（>100 课次计数）、6 个册/课次抽样（新批次每类资源）。
- PDF `preview=true` 返回 `%PDF`、`preview-info` 页数、`pages/1` 返回 JPEG。
- 视频 `Range: bytes=0-1023` 返回 206 且恰好 1024 字节。
- HTML 入口 200 且 CSP 含 sandbox、无 allow-same-origin。
- 教师 Cookie 下载字节数与登记一致；匿名 200→401；学生读课例资源被拒（RuoYi 返回 HTTP 200 + `code:403`，无文件字节）。

## 5. 浏览器验收（本机）

`python scripts/textbook/ningbo2025_smoke_ui.py` → **13 项通过、0 失败**，证据 `output/playwright/textbook-cases-2025/results.json` 与 6 张截图。

- 三上第 12 课：面板同时展示陈丹、王蕙兰两份课例；PDF 连续页图预览出现 `.case-preview-image`。
- 五上第 5 课：出现「打开网页」；新标签页 `document.body.innerText` 有内容、`window.origin === "null"`、`document.cookie` 被拒绝（BLOCKED/空）。
- 七上第 10 课：`<video>` `readyState>=2`、`duration>0`、`currentTime` 可设置、`error=0`。
- 八下第 6 课：面板出现「配套材料包」。
- 390×844、320×568：课例面板可见、`scrollWidth - innerWidth ≤ 1`、无 JS 错误；桌面同样 0 pageerror。

## 6. 正式环境验收

`python output/textbook-cases-2025/release-130/verify.py` → **18 项通过、0 失败**，证据 `release-130/production-verification.json`：

- 首页 200 且哈希等于制品 `f9347b30…`；教师登录成功。
- 汇总接口 200、覆盖 89 个课次；三上/八下/七上抽样课例均可读。
- PDF 预览、页数、页图、视频 Range 206、HTML 沙箱头、HTML 内容长度、匿名打开网页 401。
- 教师 Cookie 下载字节与登记一致；匿名下载 401；旧批次课例（`caseId ≤ 52`）仍可读。
- `journalctl -u xueyeceping-130 --since '-8 min' | grep -E 'ERROR|Exception'` → 无输出。

## 7. 未实测 / 已知限制

- 130 未用学生账号做负例（本机已验证；正式缺可用学生凭据）。
- 学校机房、低配整机、真实课堂网络未实测。
- 2 个 WMV 未转码，只提供下载；1 个 MOV 同时超限未收录。
- 19 个 HTML 都在 Chromium 实测；旧版 Edge/IE 与手机微信内置浏览器未逐一验证。
- 依赖 CDN 的网页（徐佳艳「实验三」Chart.js/XLSX）在断网环境会降级，页面说明已注明需联网。
- 大 PPTX（100–150MB）沿用浏览器原生下载，未做断点续传。
