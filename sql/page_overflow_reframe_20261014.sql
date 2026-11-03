-- page-jpeg-v4「页框外内容」重算脚本（130 正式库专用，MySQL 8）
--
-- 背景：Linux 字体度量比 WPS 偏大，DOCX 里 wrapNone 浮动图形会被推出页框。
--       v3 页图按页框栅格化，页外的文字与图形不会被画出来，教师看到的就是「底部被截断」。
--       v4 渲染前先测内容边界，越界就把取景框扩到内容处，并把这套页框回写到平台自己生成的
--       转换副本 PDF 上。学生原件与成绩始终不动。
--
-- 为什么需要本脚本：页图缓存按 renderer_version 分目录，v4 只对「新转换」生效；
--       旧 success 行的 normalized_pages_json 仍指向 page-jpeg-v3 目录，不会自己重算，
--       教师会一直看到旧裁切图。claimAttachmentNormalization 只认 normalized_status='pending'，
--       所以必须显式把目标行置为 failed（由 selectRecoverableAttachments 领取）才会重算。
--
-- 重要：不要全表重置。OFFICE 行重算会真的再跑一次 LibreOffice 转换，几千份一起放出去
--       会打满 soffice 进程池并拖慢正式服务。因此本脚本只接受「已测量出的越界清单」，
--       清单由只读测量脚本扫描 previewPath PDF 的页框外内容后生成。
--
-- 执行顺序（缺一不可）：
--   1) 先整库备份 + 备份这些附件的旧页图目录与预览 PDF；
--   2) 跑下面的「前检」，确认候选数量与 renderer_version 分布符合预期；
--   3) 把测量脚本产出的 attachment_id 填进 @ids；
--   4) 执行「重置」并查看 reset_count；
--   5) 跑「后检」，再等既有恢复任务分批领取重算（每批 200）。

USE `ry-vue`;

-- ========== 前检（只读） ==========

-- 1. 旧版本页图总量：确认还有多少行停留在 v3 及更早，决定是否需要本步骤
SELECT COALESCE(renderer_version, '(null)') AS renderer_version, file_kind,
       normalized_status, COUNT(*) AS attachment_count
FROM biz_practical_attachment
WHERE file_kind IN ('OFFICE', 'PDF')
GROUP BY renderer_version, file_kind, normalized_status
ORDER BY renderer_version, file_kind, normalized_status;

-- 2. 可重置基线：只有 success、还没顶满重试次数、且版本不是 v4 的行才能进清单
SELECT COUNT(*) AS resettable_count
FROM biz_practical_attachment
WHERE normalized_status = 'success'
  AND file_kind IN ('OFFICE', 'PDF')
  AND COALESCE(renderer_version, '') <> 'page-jpeg-v4'
  AND normalized_retry_count < 3;

-- 3. 越界清单占位：由测量脚本生成，形如 42835, 42882, ...
SET @ids = '0';

-- 清单必须非空，否则下面的重置会静默什么都不做，容易误判为「已处理」
SELECT CASE WHEN @ids = '0'
            THEN '请先用测量脚本生成越界 attachment_id 清单并填入 @ids'
            ELSE CONCAT('待重置 ', (LENGTH(@ids) - LENGTH(REPLACE(@ids, ',', '')) + 1), ' 份')
       END AS ids_ready;

-- ========== 重置（先备份，再执行） ==========

START TRANSACTION;

-- 只重置清单内、且仍满足安全条件的行：
--   normalized_status='success' → 不打扰正在转换或本来就失败的行；
--   renderer_version <> v4     → 已重算过的不重复处理（可重复执行）；
--   normalized_retry_count < 3 → 与 MAX_NORMALIZATION_RETRY_COUNT 一致，避免顶满后无法领取。
-- 置为 failed 而非 pending：failed 由 selectRecoverableAttachments 按 last_retry_time 分批领取，
-- 天然限流；pending 会被 claimAttachmentNormalization 立即抢走，容易瞬间打满转换池。
UPDATE biz_practical_attachment
SET normalized_status = 'failed',
    preview_status = 'pending',
    normalized_last_retry_time = DATE_SUB(NOW(), INTERVAL 2 HOUR),
    normalized_error_message = '页框外内容兼容升级，等待重新生成',
    update_time = NOW()
WHERE normalized_status = 'success'
  AND file_kind IN ('OFFICE', 'PDF')
  AND COALESCE(renderer_version, '') <> 'page-jpeg-v4'
  AND normalized_retry_count < 3
  AND FIND_IN_SET(attachment_id, @ids) > 0;

SELECT ROW_COUNT() AS reset_count;

COMMIT;

-- ========== 后检（只读） ==========

-- 1. 本次重置应恰好等于清单长度；不等说明有行不满足安全条件，需查明后单独处理
SELECT COUNT(*) AS waiting_recompute
FROM biz_practical_attachment
WHERE normalized_status = 'failed'
  AND normalized_error_message = '页框外内容兼容升级，等待重新生成';

-- 2. 剩余旧版本 success 行：应为「本次未列入清单」的数量，不是 0
SELECT COUNT(*) AS untouched_legacy_success
FROM biz_practical_attachment
WHERE normalized_status = 'success'
  AND file_kind IN ('OFFICE', 'PDF')
  AND COALESCE(renderer_version, '') <> 'page-jpeg-v4';

-- 3. 清单里的行必须全部离开 success，否则会被教师继续看到旧裁切图
SELECT COUNT(*) AS ids_still_success
FROM biz_practical_attachment
WHERE normalized_status = 'success'
  AND FIND_IN_SET(attachment_id, @ids) > 0;

-- 4. 重算完成后复核（等恢复任务跑完再执行）：应全部 success 且带 v4
SELECT COALESCE(renderer_version, '(null)') AS renderer_version,
       normalized_status, COUNT(*) AS attachment_count
FROM biz_practical_attachment
WHERE FIND_IN_SET(attachment_id, @ids) > 0
GROUP BY renderer_version, normalized_status;

-- 5. 面积性复核：重算成功的行页图必须存在，且没有零页或空数组
SELECT COUNT(*) AS invalid_success_page_count
FROM biz_practical_attachment
WHERE FIND_IN_SET(attachment_id, @ids) > 0
  AND normalized_status = 'success'
  AND (normalized_pages_json IS NULL OR JSON_LENGTH(normalized_pages_json) = 0);
