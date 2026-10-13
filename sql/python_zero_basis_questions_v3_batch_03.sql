-- Python 零基础系统题库 V3 第 3 批幂等导入脚本。
-- 执行前必须备份目标数据库；每次执行会按 PYV3 外部题号更新本题包并重建对应测试点。
-- 全部中文以 UTF-8 十六进制写入，避免 Windows 客户端代码页污染。
-- 题目数：20；测试点数：79。
-- batch-03.json: E28159696EE197AFBF9F4EBD5FFC4BAC84271A6CC084B1EC2CD62839EA14662F
SET NAMES utf8mb4;
DROP PROCEDURE IF EXISTS import_python_zero_basis_v3_batch_03;
DELIMITER $$
CREATE PROCEDURE import_python_zero_basis_v3_batch_03()
BEGIN
    DECLARE v_count INT DEFAULT 0;
    DECLARE v_creator_id BIGINT DEFAULT NULL;
    DECLARE v_creator_username VARCHAR(64) DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    SELECT COUNT(*) INTO v_count FROM sys_user u WHERE u.nick_name=CONVERT(0xE98391E4B89CE697AD USING utf8mb4) COLLATE utf8mb4_general_ci AND u.status='0' AND u.del_flag='0' AND EXISTS (SELECT 1 FROM sys_user_role ur INNER JOIN sys_role r ON r.role_id=ur.role_id WHERE ur.user_id=u.user_id AND r.role_key='teacher' AND r.status='0');
    IF v_count<>1 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='郑东旭有效教师账号不是唯一一条，已拒绝导入'; END IF;
    SELECT u.user_id,u.user_name INTO v_creator_id,v_creator_username FROM sys_user u WHERE u.nick_name=CONVERT(0xE98391E4B89CE697AD USING utf8mb4) COLLATE utf8mb4_general_ci AND u.status='0' AND u.del_flag='0' AND EXISTS (SELECT 1 FROM sys_user_role ur INNER JOIN sys_role r ON r.role_id=ur.role_id WHERE ur.user_id=u.user_id AND r.role_key='teacher' AND r.status='0') LIMIT 1;
    SELECT COUNT(*) INTO v_count FROM biz_programming_question_config WHERE external_id IN (CONVERT(0x505956332D303431 USING utf8mb4),CONVERT(0x505956332D303432 USING utf8mb4),CONVERT(0x505956332D303433 USING utf8mb4),CONVERT(0x505956332D303434 USING utf8mb4),CONVERT(0x505956332D303435 USING utf8mb4),CONVERT(0x505956332D303436 USING utf8mb4),CONVERT(0x505956332D303437 USING utf8mb4),CONVERT(0x505956332D303438 USING utf8mb4),CONVERT(0x505956332D303439 USING utf8mb4),CONVERT(0x505956332D303530 USING utf8mb4),CONVERT(0x505956332D303531 USING utf8mb4),CONVERT(0x505956332D303532 USING utf8mb4),CONVERT(0x505956332D303533 USING utf8mb4),CONVERT(0x505956332D303534 USING utf8mb4),CONVERT(0x505956332D303535 USING utf8mb4),CONVERT(0x505956332D303536 USING utf8mb4),CONVERT(0x505956332D303537 USING utf8mb4),CONVERT(0x505956332D303538 USING utf8mb4),CONVERT(0x505956332D303539 USING utf8mb4),CONVERT(0x505956332D303630 USING utf8mb4)) AND COALESCE(create_by,'')<>'python-system-v3';
    IF v_count<>0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='PYV3 外部题号已被其他来源占用，已拒绝导入'; END IF;
    SELECT COUNT(*) INTO v_count FROM biz_programming_question_config WHERE title IN (CONVERT(0xE68891E79A84E7ACACE4B880E4B8AAE8BE93E585A5 USING utf8mb4),CONVERT(0xE8BE93E585A5E5908DE5AD97E68993E68B9BE591BC USING utf8mb4),CONVERT(0xE8BE93E585A5E695B0E5AD97E5868DE8BE93E587BA USING utf8mb4),CONVERT(0xE8BE93E585A5E5B9B4E9BE84E58AA0E4B880E5B281 USING utf8mb4),CONVERT(0xE8BE93E585A5E4B8A4E4B8AAE695B0E6B182E5928C USING utf8mb4),CONVERT(0xE8BE93E585A5E4B8A4E4B8AAE695B0E6B182E5B7AE USING utf8mb4),CONVERT(0xE8BE93E585A5E4B8A4E4B8AAE695B0E6B182E7A7AF USING utf8mb4),CONVERT(0xE8BE93E585A5E4B8A4E4B8AAE695B0E6B182E59586 USING utf8mb4),CONVERT(0xE8BE93E585A5E5A793E5908DE5928CE5ADA6E6A0A1 USING utf8mb4),CONVERT(0xE4BDA0E5969CE6ACA2E4BB80E4B988E9A29CE889B2 USING utf8mb4),CONVERT(0xE8BE93E585A5E4B889E4B8AAE695B0E6B182E5928C USING utf8mb4),CONVERT(0xE8B4ADE4B9B0E69687E585B7E7AE97E680BBE4BBB7 USING utf8mb4),CONVERT(0xE8BE93E585A5E8BEB9E995BFE7AE97E591A8E995BF USING utf8mb4),CONVERT(0xE8BE93E585A5E8BEB9E995BFE7AE97E99DA2E7A7AF USING utf8mb4),CONVERT(0xE8BE93E585A5E995BFE5AEBDE7AE97E591A8E995BF USING utf8mb4),CONVERT(0xE8BE93E585A5E995BFE5AEBDE7AE97E99DA2E7A7AF USING utf8mb4),CONVERT(0xE58E98E7B1B3E68DA2E7AE97E68890E7B1B3 USING utf8mb4),CONVERT(0xE58886E9929FE68DA2E7AE97E68890E7A792 USING utf8mb4),CONVERT(0xE5B08FE697B6E68DA2E7AE97E68890E58886E9929F USING utf8mb4),CONVERT(0xE7BB99E88083E8AF95E68890E7BBA9E58AA0E4BA94E58886 USING utf8mb4)) AND (external_id IS NULL OR external_id NOT IN (CONVERT(0x505956332D303431 USING utf8mb4),CONVERT(0x505956332D303432 USING utf8mb4),CONVERT(0x505956332D303433 USING utf8mb4),CONVERT(0x505956332D303434 USING utf8mb4),CONVERT(0x505956332D303435 USING utf8mb4),CONVERT(0x505956332D303436 USING utf8mb4),CONVERT(0x505956332D303437 USING utf8mb4),CONVERT(0x505956332D303438 USING utf8mb4),CONVERT(0x505956332D303439 USING utf8mb4),CONVERT(0x505956332D303530 USING utf8mb4),CONVERT(0x505956332D303531 USING utf8mb4),CONVERT(0x505956332D303532 USING utf8mb4),CONVERT(0x505956332D303533 USING utf8mb4),CONVERT(0x505956332D303534 USING utf8mb4),CONVERT(0x505956332D303535 USING utf8mb4),CONVERT(0x505956332D303536 USING utf8mb4),CONVERT(0x505956332D303537 USING utf8mb4),CONVERT(0x505956332D303538 USING utf8mb4),CONVERT(0x505956332D303539 USING utf8mb4),CONVERT(0x505956332D303630 USING utf8mb4)));
    IF v_count<>0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='题库已存在同名非 PYV3 题目，已拒绝导入'; END IF;
    START TRANSACTION;

    -- PYV3-041 我的第一个输入
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303431 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4B880E8A18CE69687E5AD97EFBC8CE5868DE68A8AE8BF99E8A18CE69687E5AD97E58E9FE6A0B7E8BE93E587BAE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x74657874203D20696E70757428290A7072696E742874657874290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303431E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4B880E8A18CE69687E5AD97EFBC8CE5868DE68A8AE8BF99E8A18CE69687E5AD97E58E9FE6A0B7E8BE93E587BAE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x74657874203D20696E70757428290A7072696E742874657874290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303431E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303431 USING utf8mb4),CONVERT(0xE68891E79A84E7ACACE4B880E4B8AAE8BE93E585A5 USING utf8mb4),CONVERT(0x696E707574E8BE93E585A5EFBC8CE5AD97E7ACA6E4B8B2 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE69687E5AD97EFBC8CE4B88DE58C85E590ABE68DA2E8A18CE7ACA6E38082 USING utf8mb4),CONVERT(0xE58E9FE6A0B7E8BE93E587BAE8BE93E585A5E79A84E69687E5AD97E38082 USING utf8mb4),CONVERT(0xE8BE93E585A520507974686F6E20E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA20507974686F6EE38082 USING utf8mb4),CONVERT(0xE69687E5AD97E995BFE5BAA6E4B8BA2031EFBD9E333020E4B8AAE5AD97E7ACA6E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9A696E707574282920E8AFBBE588B0E79A84E58685E5AEB9E69CACE69DA5E5B0B1E698AFE5AD97E7ACA6E4B8B2E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x507974686F6E0A USING utf8mb4),CONVERT(0x507974686F6E0A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0xE4BFA1E681AFE7A791E68A800A USING utf8mb4),CONVERT(0xE4BFA1E681AFE7A791E68A800A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x3132330A USING utf8mb4),CONVERT(0x3132330A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0xE4BDA0E5A5BDEFBC810A USING utf8mb4),CONVERT(0xE4BDA0E5A5BDEFBC810A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-042 输入名字打招呼
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303432 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4B880E4B8AAE5908DE5AD97EFBC8CE8BE93E587BAE2809CE4BDA0E5A5BDEFBC8CE5908DE5AD97EFBC81E2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x6E616D65203D20696E70757428290A7072696E74286622E4BDA0E5A5BDEFBC8C7B6E616D657DEFBC8122290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303432E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4B880E4B8AAE5908DE5AD97EFBC8CE8BE93E587BAE2809CE4BDA0E5A5BDEFBC8CE5908DE5AD97EFBC81E2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x6E616D65203D20696E70757428290A7072696E74286622E4BDA0E5A5BDEFBC8C7B6E616D657DEFBC8122290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303432E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303432 USING utf8mb4),CONVERT(0xE8BE93E585A5E5908DE5AD97E68993E68B9BE591BC USING utf8mb4),CONVERT(0x696E707574E8BE93E585A5EFBC8CE5AD97E7ACA6E4B8B2E68BBCE68EA5 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE5908DE5AD97E38082 USING utf8mb4),CONVERT(0xE68C89E2809CE4BDA0E5A5BDEFBC8CE5908DE5AD97EFBC81E2809DE79A84E6A0BCE5BC8FE8BE93E587BAE38082 USING utf8mb4),CONVERT(0xE8BE93E585A520E5B08FE6988E20E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA20E4BDA0E5A5BDEFBC8CE5B08FE6988EEFBC81E38082 USING utf8mb4),CONVERT(0xE5908DE5AD97E995BFE5BAA6E4B8BA2031EFBD9E313020E4B8AAE5AD97E7ACA6E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE58FAFE4BBA5E4BDBFE794A8206620E5AD97E7ACA6E4B8B2E68A8AE5908DE5AD97E694BEE8BF9BE997AEE58099E8AFADE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0xE5B08FE6988E0A USING utf8mb4),CONVERT(0xE4BDA0E5A5BDEFBC8CE5B08FE6988EEFBC810A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0xE5B08FE7BAA20A USING utf8mb4),CONVERT(0xE4BDA0E5A5BDEFBC8CE5B08FE7BAA2EFBC810A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x507974686F6E0A USING utf8mb4),CONVERT(0xE4BDA0E5A5BDEFBC8C507974686F6EEFBC810A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0xE69D8EE88081E5B8880A USING utf8mb4),CONVERT(0xE4BDA0E5A5BDEFBC8CE69D8EE88081E5B888EFBC810A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-043 输入数字再输出
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303433 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4B880E4B8AAE695B4E695B0EFBC8CE5868DE8BE93E587BAE8BF99E4B8AAE695B4E695B0E38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x6E756D626572203D20696E7428696E7075742829290A7072696E74286E756D626572290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303433E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4B880E4B8AAE695B4E695B0EFBC8CE5868DE8BE93E587BAE8BF99E4B8AAE695B4E695B0E38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x6E756D626572203D20696E7428696E7075742829290A7072696E74286E756D626572290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303433E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303433 USING utf8mb4),CONVERT(0xE8BE93E585A5E695B0E5AD97E5868DE8BE93E587BA USING utf8mb4),CONVERT(0x696E707574E8BE93E585A5EFBC8CE695B4E695B0 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE695B4E695B0E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE5908CE4B880E4B8AAE695B4E695B0E38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203820E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA2038E38082 USING utf8mb4),CONVERT(0x2D313030303020E289A420E695B4E695B020E289A4203130303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE58588E794A820696E74282920E68A8AE8BE93E585A5E8BDACE68DA2E68890E695B4E695B0E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x380A USING utf8mb4),CONVERT(0x380A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x300A USING utf8mb4),CONVERT(0x300A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x2D31320A USING utf8mb4),CONVERT(0x2D31320A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x31303030300A USING utf8mb4),CONVERT(0x31303030300A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-044 输入年龄加一岁
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303434 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4BB8AE5B9B4E79A84E5B9B4E9BE84EFBC8CE68A8AE5B9B4E9BE84E58F98E9878FE5A29EE58AA0203120E5908EE8BE93E587BAE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x616765203D20696E7428696E7075742829290A616765203D20616765202B20310A7072696E7428616765290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303434E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4BB8AE5B9B4E79A84E5B9B4E9BE84EFBC8CE68A8AE5B9B4E9BE84E58F98E9878FE5A29EE58AA0203120E5908EE8BE93E587BAE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x616765203D20696E7428696E7075742829290A616765203D20616765202B20310A7072696E7428616765290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303434E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303434 USING utf8mb4),CONVERT(0xE8BE93E585A5E5B9B4E9BE84E58AA0E4B880E5B281 USING utf8mb4),CONVERT(0x696E707574E8BE93E585A5EFBC8CE58F98E9878FE69BB4E696B0 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE695B4E695B0EFBC8CE8A1A8E7A4BAE4BB8AE5B9B4E5B9B4E9BE84E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE5A29EE58AA0203120E5908EE79A84E5B9B4E9BE84E38082 USING utf8mb4),CONVERT(0xE8BE93E585A520313020E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA203131E38082 USING utf8mb4),CONVERT(0x3120E289A420E5B9B4E9BE8420E289A420313030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE58FAFE4BBA5E5869920616765203D20616765202B203120E69BB4E696B0E58F98E9878FE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x31300A USING utf8mb4),CONVERT(0x31310A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x310A USING utf8mb4),CONVERT(0x320A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x31380A USING utf8mb4),CONVERT(0x31390A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x39390A USING utf8mb4),CONVERT(0x3130300A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-045 输入两个数求和
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303435 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4B8A4E4B8AAE695B4E695B0EFBC8CE8AEA1E7AE97E5B9B6E8BE93E587BAE5AE83E4BBACE79A84E5928CE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x612C2062203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E742861202B2062290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303435E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4B8A4E4B8AAE695B4E695B0EFBC8CE8AEA1E7AE97E5B9B6E8BE93E587BAE5AE83E4BBACE79A84E5928CE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x612C2062203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E742861202B2062290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303435E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303435 USING utf8mb4),CONVERT(0xE8BE93E585A5E4B8A4E4B8AAE695B0E6B182E5928C USING utf8mb4),CONVERT(0x696E707574E8BE93E585A5EFBC8CE58AA0E6B395 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B8A4E4B8AAE695B4E695B0EFBC8CE4B8ADE997B4E794A8E7A9BAE6A0BCE58886E99A94E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E8A18CE695B4E695B0EFBC8CE8A1A8E7A4BAE4B8A4E695B0E4B98BE5928CE38082 USING utf8mb4),CONVERT(0xE8BE93E585A52033203520E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA2038E38082 USING utf8mb4),CONVERT(0x2D313030303020E289A420E4B8A4E695B020E289A4203130303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE794A8206D617028696E742C20696E70757428292E73706C697428292920E4B880E6ACA1E8AFBBE585A5E4B8A4E4B8AAE695B4E695B0E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x3320350A USING utf8mb4),CONVERT(0x380A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x3020300A USING utf8mb4),CONVERT(0x300A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x2D3320380A USING utf8mb4),CONVERT(0x350A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x3130303030202D31303030300A USING utf8mb4),CONVERT(0x300A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-046 输入两个数求差
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303436 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4B8A4E4B8AAE695B4E695B0EFBC8CE8BE93E587BAE7ACACE4B880E4B8AAE695B0E5878FE7ACACE4BA8CE4B8AAE695B0E79A84E7BB93E69E9CE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x612C2062203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E742861202D2062290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303436E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4B8A4E4B8AAE695B4E695B0EFBC8CE8BE93E587BAE7ACACE4B880E4B8AAE695B0E5878FE7ACACE4BA8CE4B8AAE695B0E79A84E7BB93E69E9CE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x612C2062203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E742861202D2062290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303436E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303436 USING utf8mb4),CONVERT(0xE8BE93E585A5E4B8A4E4B8AAE695B0E6B182E5B7AE USING utf8mb4),CONVERT(0x696E707574E8BE93E585A5EFBC8CE5878FE6B395 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B8A4E4B8AAE695B4E695B02061E3808162E38082 USING utf8mb4),CONVERT(0xE8BE93E587BA20612D62E38082 USING utf8mb4),CONVERT(0xE8BE93E585A52039203420E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA2035E38082 USING utf8mb4),CONVERT(0x2D313030303020E289A420612C6220E289A4203130303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE6B3A8E6848FE5878FE6B395E69C89E58588E5908EE9A1BAE5BA8FE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x3920340A USING utf8mb4),CONVERT(0x350A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x3420390A USING utf8mb4),CONVERT(0x2D350A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x3020300A USING utf8mb4),CONVERT(0x300A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x2D32202D370A USING utf8mb4),CONVERT(0x350A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-047 输入两个数求积
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303437 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4B8A4E4B8AAE695B4E695B0EFBC8CE8BE93E587BAE5AE83E4BBACE79A84E4B998E7A7AFE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x612C2062203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E742861202A2062290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303437E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4B8A4E4B8AAE695B4E695B0EFBC8CE8BE93E587BAE5AE83E4BBACE79A84E4B998E7A7AFE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x612C2062203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E742861202A2062290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303437E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303437 USING utf8mb4),CONVERT(0xE8BE93E585A5E4B8A4E4B8AAE695B0E6B182E7A7AF USING utf8mb4),CONVERT(0x696E707574E8BE93E585A5EFBC8CE4B998E6B395 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B8A4E4B8AAE695B4E695B0EFBC8CE4B8ADE997B4E794A8E7A9BAE6A0BCE58886E99A94E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B8A4E695B0E4B998E7A7AFE38082 USING utf8mb4),CONVERT(0xE8BE93E585A52036203720E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA203432E38082 USING utf8mb4),CONVERT(0x2D3130303020E289A420E4B8A4E695B020E289A42031303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE4B998E6B395E8BF90E7AE97E7ACA6E698AF202AE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x3620370A USING utf8mb4),CONVERT(0x34320A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x302039390A USING utf8mb4),CONVERT(0x300A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x2D3520340A USING utf8mb4),CONVERT(0x2D32300A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x2D38202D390A USING utf8mb4),CONVERT(0x37320A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-048 输入两个数求商
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303438 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4B8A4E4B8AAE695B4E695B0EFBC8CE8BE93E587BAE7ACACE4B880E4B8AAE695B0E695B4E999A4E7ACACE4BA8CE4B8AAE695B0E79A84E59586E38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x612C2062203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E742861202F2F2062290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303438E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4B8A4E4B8AAE695B4E695B0EFBC8CE8BE93E587BAE7ACACE4B880E4B8AAE695B0E695B4E999A4E7ACACE4BA8CE4B8AAE695B0E79A84E59586E38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x612C2062203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E742861202F2F2062290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303438E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303438 USING utf8mb4),CONVERT(0xE8BE93E585A5E4B8A4E4B8AAE695B0E6B182E59586 USING utf8mb4),CONVERT(0x696E707574E8BE93E585A5EFBC8CE695B4E695B0E999A4E6B395 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B8A4E4B8AAE695B4E695B02061E3808162EFBC8C6220E4B88DE4B8BA2030E38082 USING utf8mb4),CONVERT(0xE8BE93E587BA20612F2F6220E79A84E695B4E695B0E59586E38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203138203320E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA2036E38082 USING utf8mb4),CONVERT(0x3020E289A4206120E289A4203130303030EFBC8C3120E289A4206220E289A420313030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE4BDBFE794A8202F2FEFBC8CE8BE93E587BAE4B8ADE4B88DE4BC9AE587BAE78EB0E5B08FE695B0E782B9E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x313820330A USING utf8mb4),CONVERT(0x360A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x313920340A USING utf8mb4),CONVERT(0x340A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x3020350A USING utf8mb4),CONVERT(0x300A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x3939392031300A USING utf8mb4),CONVERT(0x39390A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-049 输入姓名和学校
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303439 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE7ACACE4B880E8A18CE8BE93E585A5E5A793E5908DEFBC8CE7ACACE4BA8CE8A18CE8BE93E585A5E5ADA6E6A0A1EFBC8CE8BE93E587BAE2809CE5A793E5908DE59CA8E5ADA6E6A0A1E5ADA6E4B9A0E2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x6E616D65203D20696E70757428290A7363686F6F6C203D20696E70757428290A7072696E742866227B6E616D657DE59CA87B7363686F6F6C7DE5ADA6E4B9A022290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303439E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE7ACACE4B880E8A18CE8BE93E585A5E5A793E5908DEFBC8CE7ACACE4BA8CE8A18CE8BE93E585A5E5ADA6E6A0A1EFBC8CE8BE93E587BAE2809CE5A793E5908DE59CA8E5ADA6E6A0A1E5ADA6E4B9A0E2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x6E616D65203D20696E70757428290A7363686F6F6C203D20696E70757428290A7072696E742866227B6E616D657DE59CA87B7363686F6F6C7DE5ADA6E4B9A022290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303439E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303439 USING utf8mb4),CONVERT(0xE8BE93E585A5E5A793E5908DE5928CE5ADA6E6A0A1 USING utf8mb4),CONVERT(0x696E707574E8BE93E585A5EFBC8CE5A49AE8A18CE8BE93E585A5EFBC8CE6A0BCE5BC8FE58C96E8BE93E587BA USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE7ACACE4B880E8A18CE698AFE5A793E5908DEFBC8CE7ACACE4BA8CE8A18CE698AFE5ADA6E6A0A1E5908DE7A7B0E38082 USING utf8mb4),CONVERT(0xE68C89E2809CE5A793E5908DE59CA8E5ADA6E6A0A1E5ADA6E4B9A0E2809DE79A84E6A0BCE5BC8FE8BE93E587BAE38082 USING utf8mb4),CONVERT(0xE8BE93E585A520E5B08FE6988EE38081E59091E998B3E5B08FE5ADA620E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA20E5B08FE6988EE59CA8E59091E998B3E5B08FE5ADA6E5ADA6E4B9A0E38082 USING utf8mb4),CONVERT(0xE5A793E5908DE5928CE5ADA6E6A0A1E5908DE7A7B0E59D87E4B88DE8B685E8BF8720323020E4B8AAE5AD97E7ACA6E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE8BF9EE7BBADE4BDBFE794A8E4B8A4E6ACA120696E707574282920E8AFBBE58F96E4B8A4E8A18CE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0xE5B08FE6988E0AE59091E998B3E5B08FE5ADA60A USING utf8mb4),CONVERT(0xE5B08FE6988EE59CA8E59091E998B3E5B08FE5ADA6E5ADA6E4B9A00A USING utf8mb4),1,34,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0xE5B08FE7BAA20AE5AE9EE9AA8CE5B08FE5ADA60A USING utf8mb4),CONVERT(0xE5B08FE7BAA2E59CA8E5AE9EE9AA8CE5B08FE5ADA6E5ADA6E4B9A00A USING utf8mb4),0,33,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0xE69D8EE58D8E0AE69CAAE69DA5E5ADA6E6A0A10A USING utf8mb4),CONVERT(0xE69D8EE58D8EE59CA8E69CAAE69DA5E5ADA6E6A0A1E5ADA6E4B9A00A USING utf8mb4),0,33,3,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-050 你喜欢什么颜色
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303530 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4B880E7A78DE9A29CE889B2EFBC8CE8BE93E587BAE2809CE68891E5969CE6ACA2E9A29CE889B2E2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x636F6C6F72203D20696E70757428290A7072696E74286622E68891E5969CE6ACA27B636F6C6F727D22290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303530E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4B880E7A78DE9A29CE889B2EFBC8CE8BE93E587BAE2809CE68891E5969CE6ACA2E9A29CE889B2E2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x636F6C6F72203D20696E70757428290A7072696E74286622E68891E5969CE6ACA27B636F6C6F727D22290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303530E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303530 USING utf8mb4),CONVERT(0xE4BDA0E5969CE6ACA2E4BB80E4B988E9A29CE889B2 USING utf8mb4),CONVERT(0x696E707574E8BE93E585A5EFBC8CE5AD97E7ACA6E4B8B2E68BBCE68EA5 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE9A29CE889B2E5908DE7A7B0E38082 USING utf8mb4),CONVERT(0xE68C89E2809CE68891E5969CE6ACA2E9A29CE889B2E2809DE79A84E6A0BCE5BC8FE8BE93E587BAE38082 USING utf8mb4),CONVERT(0xE8BE93E585A520E8939DE889B220E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA20E68891E5969CE6ACA2E8939DE889B2E38082 USING utf8mb4),CONVERT(0xE9A29CE889B2E5908DE7A7B0E995BFE5BAA6E4B8BA2031EFBD9E313020E4B8AAE5AD97E7ACA6E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE68A8AE8BE93E585A5E79A84E9A29CE889B2E68BBCE68EA5E588B0E2809CE68891E5969CE6ACA2E2809DE5908EE99DA2E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0xE8939DE889B20A USING utf8mb4),CONVERT(0xE68891E5969CE6ACA2E8939DE889B20A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0xE7BAA2E889B20A USING utf8mb4),CONVERT(0xE68891E5969CE6ACA2E7BAA2E889B20A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0xE7BBBFE889B20A USING utf8mb4),CONVERT(0xE68891E5969CE6ACA2E7BBBFE889B20A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0xE6A999E889B20A USING utf8mb4),CONVERT(0xE68891E5969CE6ACA2E6A999E889B20A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-051 输入三个数求和
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303531 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4B889E4B8AAE695B4E695B0EFBC8CE8BE93E587BAE5AE83E4BBACE79A84E680BBE5928CE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x612C20622C2063203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E742861202B2062202B2063290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303531E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4B889E4B8AAE695B4E695B0EFBC8CE8BE93E587BAE5AE83E4BBACE79A84E680BBE5928CE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x612C20622C2063203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E742861202B2062202B2063290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303531E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303531 USING utf8mb4),CONVERT(0xE8BE93E585A5E4B889E4B8AAE695B0E6B182E5928C USING utf8mb4),CONVERT(0x696E707574E8BE93E585A5EFBC8CE58AA0E6B395 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B889E4B8AAE695B4E695B0EFBC8CE4B8ADE997B4E794A8E7A9BAE6A0BCE58886E99A94E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E8A18CE695B4E695B0EFBC8CE8A1A8E7A4BAE4B889E4B8AAE695B0E4B98BE5928CE38082 USING utf8mb4),CONVERT(0xE8BE93E585A520312032203320E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA2036E38082 USING utf8mb4),CONVERT(0x2D3130303020E289A420E6AF8FE4B8AAE695B4E695B020E289A42031303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE4B889E4B8AAE58F98E9878FE983BDE8A681E58F82E4B88EE58AA0E6B395E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x31203220330A USING utf8mb4),CONVERT(0x360A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x30203020300A USING utf8mb4),CONVERT(0x300A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x2D31203520360A USING utf8mb4),CONVERT(0x31300A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x31303030203130303020313030300A USING utf8mb4),CONVERT(0x333030300A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-052 购买文具算总价
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303532 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E69687E585B7E58D95E4BBB7E5928CE8B4ADE4B9B0E695B0E9878FEFBC8CE8BE93E587BAE680BBE4BBB7E38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x70726963652C20636F756E74203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E74287072696365202A20636F756E74290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303532E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E69687E585B7E58D95E4BBB7E5928CE8B4ADE4B9B0E695B0E9878FEFBC8CE8BE93E587BAE680BBE4BBB7E38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x70726963652C20636F756E74203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E74287072696365202A20636F756E74290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303532E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303532 USING utf8mb4),CONVERT(0xE8B4ADE4B9B0E69687E585B7E7AE97E680BBE4BBB7 USING utf8mb4),CONVERT(0x696E707574E8BE93E585A5EFBC8CE4B998E6B395EFBC8CE7949FE6B4BBE5BA94E794A8 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B8A4E4B8AAE99D9EE8B49FE695B4E695B0EFBC8CE4BE9DE6ACA1E698AFE58D95E4BBB7E5928CE695B0E9878FE38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE58D95E4BBB7E4B998E695B0E9878FE79A84E7BB93E69E9CE38082 USING utf8mb4),CONVERT(0xE8BE93E585A52033203520E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA203135E38082 USING utf8mb4),CONVERT(0x3020E289A420E58D95E4BBB720E289A420313030EFBC8C3020E289A420E695B0E9878F20E289A420313030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE680BBE4BBB73DE58D95E4BBB7C397E695B0E9878FE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x3320350A USING utf8mb4),CONVERT(0x31350A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x3020380A USING utf8mb4),CONVERT(0x300A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x313220310A USING utf8mb4),CONVERT(0x31320A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x313030203130300A USING utf8mb4),CONVERT(0x31303030300A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-053 输入边长算周长
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303533 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E6ADA3E696B9E5BDA2E8BEB9E995BFEFBC8CE8BE93E587BAE5AE83E79A84E591A8E995BFE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x73696465203D20696E7428696E7075742829290A7072696E742873696465202A2034290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303533E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E6ADA3E696B9E5BDA2E8BEB9E995BFEFBC8CE8BE93E587BAE5AE83E79A84E591A8E995BFE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x73696465203D20696E7428696E7075742829290A7072696E742873696465202A2034290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303533E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303533 USING utf8mb4),CONVERT(0xE8BE93E585A5E8BEB9E995BFE7AE97E591A8E995BF USING utf8mb4),CONVERT(0x696E707574E8BE93E585A5EFBC8CE4B998E6B395EFBC8CE6ADA3E696B9E5BDA2 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE6ADA3E695B4E695B0EFBC8CE8A1A8E7A4BAE8BEB9E995BFE38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE8BEB9E995BFE79A84203420E5808DE38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203720E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA203238E38082 USING utf8mb4),CONVERT(0x3120E289A420E8BEB9E995BF20E289A4203130303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE6ADA3E696B9E5BDA2E69C89E59B9BE69DA1E79BB8E7AD89E79A84E8BEB9E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x370A USING utf8mb4),CONVERT(0x32380A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x310A USING utf8mb4),CONVERT(0x340A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x32350A USING utf8mb4),CONVERT(0x3130300A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x31303030300A USING utf8mb4),CONVERT(0x34303030300A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-054 输入边长算面积
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303534 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E6ADA3E696B9E5BDA2E8BEB9E995BFEFBC8CE8BE93E587BAE5AE83E79A84E99DA2E7A7AFE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x73696465203D20696E7428696E7075742829290A7072696E742873696465202A2073696465290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303534E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E6ADA3E696B9E5BDA2E8BEB9E995BFEFBC8CE8BE93E587BAE5AE83E79A84E99DA2E7A7AFE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x73696465203D20696E7428696E7075742829290A7072696E742873696465202A2073696465290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303534E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303534 USING utf8mb4),CONVERT(0xE8BE93E585A5E8BEB9E995BFE7AE97E99DA2E7A7AF USING utf8mb4),CONVERT(0x696E707574E8BE93E585A5EFBC8CE4B998E6B395EFBC8CE6ADA3E696B9E5BDA2 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE6ADA3E695B4E695B0EFBC8CE8A1A8E7A4BAE8BEB9E995BFE38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE8BEB9E995BFE4B998E8BEB9E995BFE79A84E7BB93E69E9CE38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203720E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA203439E38082 USING utf8mb4),CONVERT(0x3120E289A420E8BEB9E995BF20E289A4203130303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE99DA2E7A7AFE4B88DE698AFE8BEB9E995BFE4B9982034EFBC8CE8A681E794A8E8BEB9E995BFE4B998E887AAE5B7B1E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x370A USING utf8mb4),CONVERT(0x34390A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x310A USING utf8mb4),CONVERT(0x310A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x32350A USING utf8mb4),CONVERT(0x3632350A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x31303030300A USING utf8mb4),CONVERT(0x3130303030303030300A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-055 输入长宽算周长
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303535 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E995BFE696B9E5BDA2E79A84E995BFE5928CE5AEBDEFBC8CE8BE93E587BAE591A8E995BFE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x6C656E6774682C207769647468203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E7428286C656E677468202B20776964746829202A2032290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303535E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E995BFE696B9E5BDA2E79A84E995BFE5928CE5AEBDEFBC8CE8BE93E587BAE591A8E995BFE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x6C656E6774682C207769647468203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E7428286C656E677468202B20776964746829202A2032290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303535E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303535 USING utf8mb4),CONVERT(0xE8BE93E585A5E995BFE5AEBDE7AE97E591A8E995BF USING utf8mb4),CONVERT(0x696E707574E8BE93E585A5EFBC8CE995BFE696B9E5BDA2EFBC8CE4B998E6B395 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B8A4E4B8AAE6ADA3E695B4E695B0EFBC8CE4BE9DE6ACA1E8A1A8E7A4BAE995BFE5928CE5AEBDE38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE995BFE696B9E5BDA2E591A8E995BFE38082 USING utf8mb4),CONVERT(0xE8BE93E585A52038203520E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA203236E38082 USING utf8mb4),CONVERT(0x3120E289A420E995BFE38081E5AEBD20E289A4203130303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE591A8E995BF3D28E995BF2BE5AEBD29C39732E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x3820350A USING utf8mb4),CONVERT(0x32360A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x3120310A USING utf8mb4),CONVERT(0x340A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x323020330A USING utf8mb4),CONVERT(0x34360A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x313030303020310A USING utf8mb4),CONVERT(0x32303030320A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-056 输入长宽算面积
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303536 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E995BFE696B9E5BDA2E79A84E995BFE5928CE5AEBDEFBC8CE8BE93E587BAE99DA2E7A7AFE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x6C656E6774682C207769647468203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E74286C656E677468202A207769647468290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303536E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E995BFE696B9E5BDA2E79A84E995BFE5928CE5AEBDEFBC8CE8BE93E587BAE99DA2E7A7AFE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x6C656E6774682C207769647468203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E74286C656E677468202A207769647468290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303536E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303536 USING utf8mb4),CONVERT(0xE8BE93E585A5E995BFE5AEBDE7AE97E99DA2E7A7AF USING utf8mb4),CONVERT(0x696E707574E8BE93E585A5EFBC8CE995BFE696B9E5BDA2EFBC8CE4B998E6B395 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B8A4E4B8AAE6ADA3E695B4E695B0EFBC8CE4BE9DE6ACA1E8A1A8E7A4BAE995BFE5928CE5AEBDE38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE995BFE696B9E5BDA2E99DA2E7A7AFE38082 USING utf8mb4),CONVERT(0xE8BE93E585A52038203520E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA203430E38082 USING utf8mb4),CONVERT(0x3120E289A420E995BFE38081E5AEBD20E289A4203130303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE99DA2E7A7AF3DE995BFC397E5AEBDE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x3820350A USING utf8mb4),CONVERT(0x34300A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x3120310A USING utf8mb4),CONVERT(0x310A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x323020330A USING utf8mb4),CONVERT(0x36300A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x313030303020310A USING utf8mb4),CONVERT(0x31303030300A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-057 厘米换算成米
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303537 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E695B4E695B0E58E98E7B1B3E695B0EFBC8CE68DA2E7AE97E68890E7B1B3E5B9B6E4BF9DE79599E4B8A4E4BD8DE5B08FE695B0E38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x636D203D20696E7428696E7075742829290A7072696E742866227B636D202F203130303A2E32667D22290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303537E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E695B4E695B0E58E98E7B1B3E695B0EFBC8CE68DA2E7AE97E68890E7B1B3E5B9B6E4BF9DE79599E4B8A4E4BD8DE5B08FE695B0E38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x636D203D20696E7428696E7075742829290A7072696E742866227B636D202F203130303A2E32667D22290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303537E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303537 USING utf8mb4),CONVERT(0xE58E98E7B1B3E68DA2E7AE97E68890E7B1B3 USING utf8mb4),CONVERT(0x696E707574E8BE93E585A5EFBC8CE58D95E4BD8DE68DA2E7AE97EFBC8CE6A0BCE5BC8FE58C96E8BE93E587BA USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE99D9EE8B49FE695B4E695B0EFBC8CE58D95E4BD8DE698AFE58E98E7B1B3E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE7B1B3E695B0EFBC8CE4BF9DE79599E4B8A4E4BD8DE5B08FE695B0E38082 USING utf8mb4),CONVERT(0xE8BE93E585A52032353020E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA20322E3530E38082 USING utf8mb4),CONVERT(0x3020E289A420E58E98E7B1B3E695B020E289A420313030303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9A3120E7B1B3E7AD89E4BA8E2031303020E58E98E7B1B3E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x3235300A USING utf8mb4),CONVERT(0x322E35300A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x300A USING utf8mb4),CONVERT(0x302E30300A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x310A USING utf8mb4),CONVERT(0x302E30310A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x3130303030300A USING utf8mb4),CONVERT(0x313030302E30300A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-058 分钟换算成秒
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303538 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E58886E9929FE695B0EFBC8CE68DA2E7AE97E68890E7A792E38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x6D696E75746573203D20696E7428696E7075742829290A7072696E74286D696E75746573202A203630290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303538E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E58886E9929FE695B0EFBC8CE68DA2E7AE97E68890E7A792E38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x6D696E75746573203D20696E7428696E7075742829290A7072696E74286D696E75746573202A203630290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303538E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303538 USING utf8mb4),CONVERT(0xE58886E9929FE68DA2E7AE97E68890E7A792 USING utf8mb4),CONVERT(0x696E707574E8BE93E585A5EFBC8CE58D95E4BD8DE68DA2E7AE97EFBC8CE4B998E6B395 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE99D9EE8B49FE695B4E695B0EFBC8CE8A1A8E7A4BAE58886E9929FE695B0E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE5AFB9E5BA94E79A84E7A792E695B0E38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203520E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA20333030E38082 USING utf8mb4),CONVERT(0x3020E289A420E58886E9929FE695B020E289A4203130303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9A3120E58886E9929FE7AD89E4BA8E20363020E7A792E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x350A USING utf8mb4),CONVERT(0x3330300A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x300A USING utf8mb4),CONVERT(0x300A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x310A USING utf8mb4),CONVERT(0x36300A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x31303030300A USING utf8mb4),CONVERT(0x3630303030300A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-059 小时换算成分钟
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303539 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E5B08FE697B6E695B0EFBC8CE68DA2E7AE97E68890E58886E9929FE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x686F757273203D20696E7428696E7075742829290A7072696E7428686F757273202A203630290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303539E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E5B08FE697B6E695B0EFBC8CE68DA2E7AE97E68890E58886E9929FE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x686F757273203D20696E7428696E7075742829290A7072696E7428686F757273202A203630290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303539E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303539 USING utf8mb4),CONVERT(0xE5B08FE697B6E68DA2E7AE97E68890E58886E9929F USING utf8mb4),CONVERT(0x696E707574E8BE93E585A5EFBC8CE58D95E4BD8DE68DA2E7AE97EFBC8CE4B998E6B395 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE99D9EE8B49FE695B4E695B0EFBC8CE8A1A8E7A4BAE5B08FE697B6E695B0E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE5AFB9E5BA94E79A84E58886E9929FE695B0E38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203220E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA20313230E38082 USING utf8mb4),CONVERT(0x3020E289A420E5B08FE697B6E695B020E289A42031303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9A3120E5B08FE697B6E7AD89E4BA8E20363020E58886E9929FE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x320A USING utf8mb4),CONVERT(0x3132300A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x300A USING utf8mb4),CONVERT(0x300A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x310A USING utf8mb4),CONVERT(0x36300A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x313030300A USING utf8mb4),CONVERT(0x36303030300A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-060 给考试成绩加五分
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303630 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4B880E4B8AAE4B88DE8B685E8BF8720393520E79A84E88083E8AF95E68890E7BBA9EFBC8CE8BE93E587BAE58AA0203520E58886E5908EE79A84E68890E7BBA9E38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x73636F7265203D20696E7428696E7075742829290A7072696E742873636F7265202B2035290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303630E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4B880E4B8AAE4B88DE8B685E8BF8720393520E79A84E88083E8AF95E68890E7BBA9EFBC8CE8BE93E587BAE58AA0203520E58886E5908EE79A84E68890E7BBA9E38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x73636F7265203D20696E7428696E7075742829290A7072696E742873636F7265202B2035290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303630E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303630 USING utf8mb4),CONVERT(0xE7BB99E88083E8AF95E68890E7BBA9E58AA0E4BA94E58886 USING utf8mb4),CONVERT(0x696E707574E8BE93E585A5EFBC8CE58AA0E6B395EFBC8CE68890E7BBA9 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE695B4E695B0E68890E7BBA9E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE68890E7BBA9E58AA0203520E5908EE79A84E7BB93E69E9CE38082 USING utf8mb4),CONVERT(0xE8BE93E585A520383020E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA203835E38082 USING utf8mb4),CONVERT(0x3020E289A420E68890E7BBA920E289A4203935E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE79BB4E68EA5E59CA8E58E9FE68890E7BBA9E59FBAE7A180E4B88AE58AA02035E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x38300A USING utf8mb4),CONVERT(0x38350A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x300A USING utf8mb4),CONVERT(0x350A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x39300A USING utf8mb4),CONVERT(0x39350A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x39350A USING utf8mb4),CONVERT(0x3130300A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    SELECT COUNT(*) INTO v_count FROM biz_programming_question_config c INNER JOIN biz_question q ON q.question_id=c.question_id
    WHERE c.external_id IN (CONVERT(0x505956332D303431 USING utf8mb4),CONVERT(0x505956332D303432 USING utf8mb4),CONVERT(0x505956332D303433 USING utf8mb4),CONVERT(0x505956332D303434 USING utf8mb4),CONVERT(0x505956332D303435 USING utf8mb4),CONVERT(0x505956332D303436 USING utf8mb4),CONVERT(0x505956332D303437 USING utf8mb4),CONVERT(0x505956332D303438 USING utf8mb4),CONVERT(0x505956332D303439 USING utf8mb4),CONVERT(0x505956332D303530 USING utf8mb4),CONVERT(0x505956332D303531 USING utf8mb4),CONVERT(0x505956332D303532 USING utf8mb4),CONVERT(0x505956332D303533 USING utf8mb4),CONVERT(0x505956332D303534 USING utf8mb4),CONVERT(0x505956332D303535 USING utf8mb4),CONVERT(0x505956332D303536 USING utf8mb4),CONVERT(0x505956332D303537 USING utf8mb4),CONVERT(0x505956332D303538 USING utf8mb4),CONVERT(0x505956332D303539 USING utf8mb4),CONVERT(0x505956332D303630 USING utf8mb4)) AND c.create_by='python-system-v3' AND q.creator_id=v_creator_id AND CAST(q.create_by AS BINARY)=CAST(v_creator_username AS BINARY) AND q.question_type='practical' AND q.practical_mode='PYTHON' AND q.grade IS NULL AND q.semester IS NULL AND q.lesson_num IS NULL AND q.is_public='Y' AND c.validation_status='VALID' AND c.enabled='1';
    IF v_count<>20 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='本批 PYV3 题目或配置后检数量不一致，事务已回滚'; END IF;
    SELECT COUNT(*) INTO v_count FROM biz_programming_test_case tc INNER JOIN biz_programming_question_config c ON c.question_id=tc.question_id
    WHERE c.external_id IN (CONVERT(0x505956332D303431 USING utf8mb4),CONVERT(0x505956332D303432 USING utf8mb4),CONVERT(0x505956332D303433 USING utf8mb4),CONVERT(0x505956332D303434 USING utf8mb4),CONVERT(0x505956332D303435 USING utf8mb4),CONVERT(0x505956332D303436 USING utf8mb4),CONVERT(0x505956332D303437 USING utf8mb4),CONVERT(0x505956332D303438 USING utf8mb4),CONVERT(0x505956332D303439 USING utf8mb4),CONVERT(0x505956332D303530 USING utf8mb4),CONVERT(0x505956332D303531 USING utf8mb4),CONVERT(0x505956332D303532 USING utf8mb4),CONVERT(0x505956332D303533 USING utf8mb4),CONVERT(0x505956332D303534 USING utf8mb4),CONVERT(0x505956332D303535 USING utf8mb4),CONVERT(0x505956332D303536 USING utf8mb4),CONVERT(0x505956332D303537 USING utf8mb4),CONVERT(0x505956332D303538 USING utf8mb4),CONVERT(0x505956332D303539 USING utf8mb4),CONVERT(0x505956332D303630 USING utf8mb4));
    IF v_count<>79 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='PYV3 测试点后检数量不一致，事务已回滚'; END IF;
    SELECT COUNT(*) INTO v_count FROM (
        SELECT c.question_id FROM biz_programming_question_config c INNER JOIN biz_programming_test_case tc ON tc.question_id=c.question_id
        WHERE c.external_id IN (CONVERT(0x505956332D303431 USING utf8mb4),CONVERT(0x505956332D303432 USING utf8mb4),CONVERT(0x505956332D303433 USING utf8mb4),CONVERT(0x505956332D303434 USING utf8mb4),CONVERT(0x505956332D303435 USING utf8mb4),CONVERT(0x505956332D303436 USING utf8mb4),CONVERT(0x505956332D303437 USING utf8mb4),CONVERT(0x505956332D303438 USING utf8mb4),CONVERT(0x505956332D303439 USING utf8mb4),CONVERT(0x505956332D303530 USING utf8mb4),CONVERT(0x505956332D303531 USING utf8mb4),CONVERT(0x505956332D303532 USING utf8mb4),CONVERT(0x505956332D303533 USING utf8mb4),CONVERT(0x505956332D303534 USING utf8mb4),CONVERT(0x505956332D303535 USING utf8mb4),CONVERT(0x505956332D303536 USING utf8mb4),CONVERT(0x505956332D303537 USING utf8mb4),CONVERT(0x505956332D303538 USING utf8mb4),CONVERT(0x505956332D303539 USING utf8mb4),CONVERT(0x505956332D303630 USING utf8mb4)) GROUP BY c.question_id,c.no_input
        HAVING SUM(tc.is_public='1')>=1 AND SUM(tc.is_public='0')>=1 AND ROUND(SUM(tc.score_weight),6)=100
           AND ((c.no_input='1' AND COUNT(*)=2) OR (c.no_input='0' AND COUNT(*) BETWEEN 3 AND 5))
    ) checked_questions;
    IF v_count<>20 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='本批 PYV3 测试点公开性、数量或权重后检失败，事务已回滚'; END IF;
    COMMIT;
END$$
DELIMITER ;
CALL import_python_zero_basis_v3_batch_03();
DROP PROCEDURE IF EXISTS import_python_zero_basis_v3_batch_03;
SELECT q.difficulty,COUNT(*) question_count FROM biz_programming_question_config c INNER JOIN biz_question q ON q.question_id=c.question_id
WHERE c.external_id IN (CONVERT(0x505956332D303431 USING utf8mb4),CONVERT(0x505956332D303432 USING utf8mb4),CONVERT(0x505956332D303433 USING utf8mb4),CONVERT(0x505956332D303434 USING utf8mb4),CONVERT(0x505956332D303435 USING utf8mb4),CONVERT(0x505956332D303436 USING utf8mb4),CONVERT(0x505956332D303437 USING utf8mb4),CONVERT(0x505956332D303438 USING utf8mb4),CONVERT(0x505956332D303439 USING utf8mb4),CONVERT(0x505956332D303530 USING utf8mb4),CONVERT(0x505956332D303531 USING utf8mb4),CONVERT(0x505956332D303532 USING utf8mb4),CONVERT(0x505956332D303533 USING utf8mb4),CONVERT(0x505956332D303534 USING utf8mb4),CONVERT(0x505956332D303535 USING utf8mb4),CONVERT(0x505956332D303536 USING utf8mb4),CONVERT(0x505956332D303537 USING utf8mb4),CONVERT(0x505956332D303538 USING utf8mb4),CONVERT(0x505956332D303539 USING utf8mb4),CONVERT(0x505956332D303630 USING utf8mb4)) GROUP BY q.difficulty ORDER BY q.difficulty;
SELECT COUNT(*) question_count,SUM(case_count) test_case_count FROM (SELECT c.question_id,COUNT(tc.test_case_id) case_count FROM biz_programming_question_config c INNER JOIN biz_programming_test_case tc ON tc.question_id=c.question_id
WHERE c.external_id IN (CONVERT(0x505956332D303431 USING utf8mb4),CONVERT(0x505956332D303432 USING utf8mb4),CONVERT(0x505956332D303433 USING utf8mb4),CONVERT(0x505956332D303434 USING utf8mb4),CONVERT(0x505956332D303435 USING utf8mb4),CONVERT(0x505956332D303436 USING utf8mb4),CONVERT(0x505956332D303437 USING utf8mb4),CONVERT(0x505956332D303438 USING utf8mb4),CONVERT(0x505956332D303439 USING utf8mb4),CONVERT(0x505956332D303530 USING utf8mb4),CONVERT(0x505956332D303531 USING utf8mb4),CONVERT(0x505956332D303532 USING utf8mb4),CONVERT(0x505956332D303533 USING utf8mb4),CONVERT(0x505956332D303534 USING utf8mb4),CONVERT(0x505956332D303535 USING utf8mb4),CONVERT(0x505956332D303536 USING utf8mb4),CONVERT(0x505956332D303537 USING utf8mb4),CONVERT(0x505956332D303538 USING utf8mb4),CONVERT(0x505956332D303539 USING utf8mb4),CONVERT(0x505956332D303630 USING utf8mb4)) GROUP BY c.question_id) x;
