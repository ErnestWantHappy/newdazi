-- 仅登记 1.30.95 打字题英文引号自动纠正，不修改课程、成绩或权限。
-- 脚本幂等；只在同版本标题一致时更新，避免覆盖其他发布。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time, update_by, update_time)
SELECT
    '1.30.95',
    '打字题英文引号自动纠正',
    '从 Word、WPS 或网页复制英文课文创建打字题时，英文缩写的撇（如 It''s）常被自动替换成中文弯单引号，这种引号在键盘上打不出来，学生逐字比对必然判错。现已修复：保存打字题时自动把英文单词之间的中文弯单引号纠正为键盘可输入的英文单引号，中文课文引号与中文标点练习题不受影响；同时已修正两道受影响的题目，学生重新打开练习即可正常输入。',
    NOW(),
    'PUBLISHED',
    'admin',
    NOW(),
    'admin',
    NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM (SELECT version_no FROM biz_platform_update WHERE version_no = '1.30.95') existing_version
);

UPDATE biz_platform_update
SET content = '从 Word、WPS 或网页复制英文课文创建打字题时，英文缩写的撇（如 It''s）常被自动替换成中文弯单引号，这种引号在键盘上打不出来，学生逐字比对必然判错。现已修复：保存打字题时自动把英文单词之间的中文弯单引号纠正为键盘可输入的英文单引号，中文课文引号与中文标点练习题不受影响；同时已修正两道受影响的题目，学生重新打开练习即可正常输入。',
    status = 'PUBLISHED',
    published_at = COALESCE(published_at, NOW()),
    update_by = 'admin',
    update_time = NOW()
WHERE version_no = '1.30.95'
  AND title = '打字题英文引号自动纠正';

SELECT update_id, version_no, title, status, published_at
FROM biz_platform_update
WHERE version_no = '1.30.95';
