SELECT COUNT(*) AS preview_column_count
FROM information_schema.columns
WHERE table_schema = DATABASE() AND table_name = 'biz_practical_attachment'
  AND column_name IN ('normalized_status','normalized_pages_json','renderer_version',
                      'normalized_retry_count','normalized_last_retry_time','normalized_error_message');

SELECT normalized_status, file_kind, COUNT(*) AS attachment_count
FROM biz_practical_attachment
GROUP BY normalized_status, file_kind
ORDER BY normalized_status, file_kind;

SELECT COUNT(*) AS invalid_success_page_count
FROM biz_practical_attachment
WHERE normalized_status = 'success'
  AND (normalized_pages_json IS NULL OR JSON_LENGTH(normalized_pages_json) = 0);

SELECT COUNT(*) AS invalid_normalized_status_count
FROM biz_practical_attachment
WHERE normalized_status NOT IN ('pending','converting','success','failed');

SELECT COUNT(*) AS county_preview_column_count
FROM information_schema.columns
WHERE table_schema = DATABASE() AND table_name = 'biz_county_exam_answer'
  AND column_name IN ('normalized_status','normalized_pages_json','renderer_version',
                      'normalized_retry_count','normalized_last_retry_time','normalized_error_message');

SELECT normalized_status, COUNT(*) AS county_answer_count
FROM biz_county_exam_answer
WHERE normalized_status IS NOT NULL
GROUP BY normalized_status;

SELECT COUNT(*) AS invalid_county_success_page_count
FROM biz_county_exam_answer
WHERE normalized_status = 'success'
  AND (normalized_pages_json IS NULL OR JSON_LENGTH(normalized_pages_json) = 0);
