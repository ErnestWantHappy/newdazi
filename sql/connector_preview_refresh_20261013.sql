-- 130 专用：先整库及旧预览备份，正式 1.30.45 探活后执行；不改原件、答案或分数。
USE `ry-vue`;
START TRANSACTION;
UPDATE biz_practical_attachment pa
SET normalized_status='failed', preview_status='pending',
    normalized_last_retry_time=DATE_SUB(NOW(),INTERVAL 2 HOUR),
    normalized_error_message='连接符坐标兼容升级，等待重新生成'
WHERE normalized_status='success' AND file_kind='OFFICE'
  AND COALESCE(renderer_version,'') <> 'page-jpeg-v3'
  AND normalized_retry_count<3
  AND (
    (pa.attachment_id=42835 AND pa.sha256='4901026a9dbf693ff93b0267547df74e69effc42b5d56db0c4e240829d402ecb' AND EXISTS (SELECT 1 FROM biz_student_answer a WHERE a.answer_id=568942 AND a.practical_version_id=pa.version_id))
    OR (pa.attachment_id=42882 AND pa.sha256='0fd4551dc035f20a95af0fe35fd9ac1aacc959bc6bbb1f9c27c47421585983e7' AND EXISTS (SELECT 1 FROM biz_student_answer a WHERE a.answer_id=568943 AND a.practical_version_id=pa.version_id))
    OR (pa.attachment_id=42839 AND pa.sha256='08a8a16cc4c1a5149cdb7d23f2aaea1258d462c5518e09465d1f605c87ee7886' AND EXISTS (SELECT 1 FROM biz_student_answer a WHERE a.answer_id=568946 AND a.practical_version_id=pa.version_id))
    OR (pa.attachment_id=42851 AND pa.sha256='6cbc275d55ccd870cb40ebaef4ac254221311774d40372848ca133105d80d3c1' AND EXISTS (SELECT 1 FROM biz_student_answer a WHERE a.answer_id=568958 AND a.practical_version_id=pa.version_id))
    OR (pa.attachment_id=42854 AND pa.sha256='e52eca0cdab176e661d8a0380101b2e7bd8f37ea9336537bc5dc02110239b58f' AND EXISTS (SELECT 1 FROM biz_student_answer a WHERE a.answer_id=568961 AND a.practical_version_id=pa.version_id))
    OR (pa.attachment_id=42877 AND pa.sha256='fae80d5735fc29df5a01cf799c119b3e3c2708dc99ecb329ded4b3c21c2b123c' AND EXISTS (SELECT 1 FROM biz_student_answer a WHERE a.answer_id=568984 AND a.practical_version_id=pa.version_id))
    OR (pa.attachment_id=42885 AND pa.sha256='3a44720b9e586011622d8a8e8a358af513c7d6295c5e755b78af9f2a492776ad' AND EXISTS (SELECT 1 FROM biz_student_answer a WHERE a.answer_id=568992 AND a.practical_version_id=pa.version_id))
    OR (pa.attachment_id=43614 AND pa.sha256='918c6b7a969eacf24612568bea6b293597239b3171b48e7874f5ae553f4c1406' AND EXISTS (SELECT 1 FROM biz_student_answer a WHERE a.answer_id=574599 AND a.practical_version_id=pa.version_id))
    OR (pa.attachment_id=43618 AND pa.sha256='880f606b355e548d45d21e2b9cb8ddc0948625cbea5cb9915c5b6ebeac8941a5' AND EXISTS (SELECT 1 FROM biz_student_answer a WHERE a.answer_id=574605 AND a.practical_version_id=pa.version_id))
    OR (pa.attachment_id=43743 AND pa.sha256='e8c3b1dfce85d0e360e8ecbd0400375a4655f051a43beada557dbed3ebd1c79b' AND EXISTS (SELECT 1 FROM biz_student_answer a WHERE a.answer_id=575822 AND a.practical_version_id=pa.version_id))
    OR (pa.attachment_id=43759 AND pa.sha256='0e9d9aefd06ee5e768a9892daafff6bc4653e6c662ec00b9322fc8c325aac9f6' AND EXISTS (SELECT 1 FROM biz_student_answer a WHERE a.answer_id=575870 AND a.practical_version_id=pa.version_id))
    OR (pa.attachment_id=43761 AND pa.sha256='97cbd4ce7ac8ada275f5ee13b88cbeeab20eead93603e15b2fd7ca3230443497' AND EXISTS (SELECT 1 FROM biz_student_answer a WHERE a.answer_id=575877 AND a.practical_version_id=pa.version_id))
    OR (pa.attachment_id=43797 AND pa.sha256='b2e6fc794952f5c2040fceef7fab6b7cd5fe63ed7349571feb8cf26fcc68c566' AND EXISTS (SELECT 1 FROM biz_student_answer a WHERE a.answer_id=575940 AND a.practical_version_id=pa.version_id))
    OR (pa.attachment_id=43804 AND pa.sha256='7c2649f77d46039f5eac49c161a7aeee12e00b5e0da4ea3061c39234c58aa5fb' AND EXISTS (SELECT 1 FROM biz_student_answer a WHERE a.answer_id=575947 AND a.practical_version_id=pa.version_id))
    OR (pa.attachment_id=44869 AND pa.sha256='a04ef2a5c0d04dc34adeb1016a0dfc9a8849deda306c636acfc22b04ad7adaea' AND EXISTS (SELECT 1 FROM biz_student_answer a WHERE a.answer_id=583632 AND a.practical_version_id=pa.version_id))
    OR (pa.attachment_id=45033 AND pa.sha256='ee707e5c5322ae2da9772acd896f6912063a497f061ff3ce7f4172a74c79e729' AND EXISTS (SELECT 1 FROM biz_student_answer a WHERE a.answer_id=585225 AND a.practical_version_id=pa.version_id))
    OR (pa.attachment_id=45037 AND pa.sha256='ceddde9d03e9712b2413807519b98f9fc526da864c1038f39d7690670dbc5d3c' AND EXISTS (SELECT 1 FROM biz_student_answer a WHERE a.answer_id=585229 AND a.practical_version_id=pa.version_id))
    OR (pa.attachment_id=45047 AND pa.sha256='5d538dbb0fd1c778c01cec2f2ff8f6f878cd992b8d01a886d896d7db8a7fb607' AND EXISTS (SELECT 1 FROM biz_student_answer a WHERE a.answer_id=585239 AND a.practical_version_id=pa.version_id))
    OR (pa.attachment_id=45064 AND pa.sha256='d02c06b0589f769a2b58761cc5bedf0ca887f71b7c92fcca250abfb851e305fc' AND EXISTS (SELECT 1 FROM biz_student_answer a WHERE a.answer_id=585256 AND a.practical_version_id=pa.version_id))
    OR (pa.attachment_id=45090 AND pa.sha256='7c6130b4bd788fceb321ca3370ba6406da885d448422d14e28e11bc145c2f5ec' AND EXISTS (SELECT 1 FROM biz_student_answer a WHERE a.answer_id=585277 AND a.practical_version_id=pa.version_id))
    OR (pa.attachment_id=45102 AND pa.sha256='01888458f3c7b24cdb95ea916f70796325a8da15bcec18c72b0ada8dea556a35' AND EXISTS (SELECT 1 FROM biz_student_answer a WHERE a.answer_id=585296 AND a.practical_version_id=pa.version_id))
    OR (pa.attachment_id=45107 AND pa.sha256='da29a88ca301b020fec047e7378ea46e7a27611fe00a6c31c2e2cf9f9bae3f01' AND EXISTS (SELECT 1 FROM biz_student_answer a WHERE a.answer_id=585301 AND a.practical_version_id=pa.version_id))
    OR (pa.attachment_id=45128 AND pa.sha256='d6c3d39ab517e5bd5e6ecaf411f5f33cd8f700e252ae4707932c963135b3ab31' AND EXISTS (SELECT 1 FROM biz_student_answer a WHERE a.answer_id=585322 AND a.practical_version_id=pa.version_id))
    OR (pa.attachment_id=45134 AND pa.sha256='6a59c9c3ff526d585f19f0aff659055265cfef7116220c9e61f3a4bf98a48eca' AND EXISTS (SELECT 1 FROM biz_student_answer a WHERE a.answer_id=585328 AND a.practical_version_id=pa.version_id))
    OR (pa.attachment_id=45140 AND pa.sha256='05c5cc20dc6ba35a5e4e7474d7d210339a9f748744e25f8c6f384bf5290aeb5d' AND EXISTS (SELECT 1 FROM biz_student_answer a WHERE a.answer_id=585334 AND a.practical_version_id=pa.version_id))
  );
SELECT ROW_COUNT() AS reset_count;
COMMIT;
