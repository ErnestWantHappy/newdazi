-- Python 零基础系统题库 V3 第 2 批幂等导入脚本。
-- 执行前必须备份目标数据库；每次执行会按 PYV3 外部题号更新本题包并重建对应测试点。
-- 全部中文以 UTF-8 十六进制写入，避免 Windows 客户端代码页污染。
-- 题目数：20；测试点数：72。
-- batch-02.json: BE3CAFFC740220B24E5711988ED49EE4DEA02FA683C1F096A58722991E1152C6
SET NAMES utf8mb4;
DROP PROCEDURE IF EXISTS import_python_zero_basis_v3_batch_02;
DELIMITER $$
CREATE PROCEDURE import_python_zero_basis_v3_batch_02()
BEGIN
    DECLARE v_count INT DEFAULT 0;
    DECLARE v_creator_id BIGINT DEFAULT NULL;
    DECLARE v_creator_username VARCHAR(64) DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    SELECT COUNT(*) INTO v_count FROM sys_user u WHERE u.nick_name=CONVERT(0xE98391E4B89CE697AD USING utf8mb4) COLLATE utf8mb4_general_ci AND u.status='0' AND u.del_flag='0' AND EXISTS (SELECT 1 FROM sys_user_role ur INNER JOIN sys_role r ON r.role_id=ur.role_id WHERE ur.user_id=u.user_id AND r.role_key='teacher' AND r.status='0');
    IF v_count<>1 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='郑东旭有效教师账号不是唯一一条，已拒绝导入'; END IF;
    SELECT u.user_id,u.user_name INTO v_creator_id,v_creator_username FROM sys_user u WHERE u.nick_name=CONVERT(0xE98391E4B89CE697AD USING utf8mb4) COLLATE utf8mb4_general_ci AND u.status='0' AND u.del_flag='0' AND EXISTS (SELECT 1 FROM sys_user_role ur INNER JOIN sys_role r ON r.role_id=ur.role_id WHERE ur.user_id=u.user_id AND r.role_key='teacher' AND r.status='0') LIMIT 1;
    SELECT COUNT(*) INTO v_count FROM biz_programming_question_config WHERE external_id IN (CONVERT(0x505956332D303231 USING utf8mb4),CONVERT(0x505956332D303232 USING utf8mb4),CONVERT(0x505956332D303233 USING utf8mb4),CONVERT(0x505956332D303234 USING utf8mb4),CONVERT(0x505956332D303235 USING utf8mb4),CONVERT(0x505956332D303236 USING utf8mb4),CONVERT(0x505956332D303237 USING utf8mb4),CONVERT(0x505956332D303238 USING utf8mb4),CONVERT(0x505956332D303239 USING utf8mb4),CONVERT(0x505956332D303330 USING utf8mb4),CONVERT(0x505956332D303331 USING utf8mb4),CONVERT(0x505956332D303332 USING utf8mb4),CONVERT(0x505956332D303333 USING utf8mb4),CONVERT(0x505956332D303334 USING utf8mb4),CONVERT(0x505956332D303335 USING utf8mb4),CONVERT(0x505956332D303336 USING utf8mb4),CONVERT(0x505956332D303337 USING utf8mb4),CONVERT(0x505956332D303338 USING utf8mb4),CONVERT(0x505956332D303339 USING utf8mb4),CONVERT(0x505956332D303430 USING utf8mb4)) AND COALESCE(create_by,'')<>'python-system-v3';
    IF v_count<>0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='PYV3 外部题号已被其他来源占用，已拒绝导入'; END IF;
    SELECT COUNT(*) INTO v_count FROM biz_programming_question_config WHERE title IN (CONVERT(0xE8AEA1E7AE972034C39735 USING utf8mb4),CONVERT(0xE8AEA1E7AE97203230C3B734 USING utf8mb4),CONVERT(0xE4B889E4B8AAE695B0E79BB8E58AA0 USING utf8mb4),CONVERT(0xE8AEB0E5BD95E5B08FE6988EE79A84E5B9B4E9BE84 USING utf8mb4),CONVERT(0xE6988EE5B9B4E68891E587A0E5B281 USING utf8mb4),CONVERT(0xE4B8A4E695B0E79BB8E5878F USING utf8mb4),CONVERT(0xE4B8A4E695B0E79BB8E4B998 USING utf8mb4),CONVERT(0xE6B182E695B4E695B0E999A4E6B395E79A84E59586 USING utf8mb4),CONVERT(0xE6B182E999A4E6B395E79A84E4BD99E695B0 USING utf8mb4),CONVERT(0xE4B9B0E88BB9E69E9CE88AB1E5A49AE5B091E992B1 USING utf8mb4),CONVERT(0xE4B9B0E99385E7AC94E88AB1E5A49AE5B091E992B1 USING utf8mb4),CONVERT(0xE4B9B0E4B9A6E58C85E88AB1E5A49AE5B091E992B1 USING utf8mb4),CONVERT(0xE4B9B0E4B89CE8A5BFE689BEE99BB6E992B1 USING utf8mb4),CONVERT(0xE6ADA3E696B9E5BDA2E79A84E591A8E995BF USING utf8mb4),CONVERT(0xE6ADA3E696B9E5BDA2E79A84E99DA2E7A7AF USING utf8mb4),CONVERT(0xE995BFE696B9E5BDA2E79A84E591A8E995BF USING utf8mb4),CONVERT(0xE995BFE696B9E5BDA2E79A84E99DA2E7A7AF USING utf8mb4),CONVERT(0xE4B889E8A792E5BDA2E79A84E591A8E995BF USING utf8mb4),CONVERT(0xE6B8A9E5BAA6E58D87E9AB98E4B880E5BAA6 USING utf8mb4),CONVERT(0xE8AEA1E7AE97E4B889E4B8AAE68890E7BBA9E79A84E5B9B3E59D87E58886 USING utf8mb4)) AND (external_id IS NULL OR external_id NOT IN (CONVERT(0x505956332D303231 USING utf8mb4),CONVERT(0x505956332D303232 USING utf8mb4),CONVERT(0x505956332D303233 USING utf8mb4),CONVERT(0x505956332D303234 USING utf8mb4),CONVERT(0x505956332D303235 USING utf8mb4),CONVERT(0x505956332D303236 USING utf8mb4),CONVERT(0x505956332D303237 USING utf8mb4),CONVERT(0x505956332D303238 USING utf8mb4),CONVERT(0x505956332D303239 USING utf8mb4),CONVERT(0x505956332D303330 USING utf8mb4),CONVERT(0x505956332D303331 USING utf8mb4),CONVERT(0x505956332D303332 USING utf8mb4),CONVERT(0x505956332D303333 USING utf8mb4),CONVERT(0x505956332D303334 USING utf8mb4),CONVERT(0x505956332D303335 USING utf8mb4),CONVERT(0x505956332D303336 USING utf8mb4),CONVERT(0x505956332D303337 USING utf8mb4),CONVERT(0x505956332D303338 USING utf8mb4),CONVERT(0x505956332D303339 USING utf8mb4),CONVERT(0x505956332D303430 USING utf8mb4)));
    IF v_count<>0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='题库已存在同名非 PYV3 题目，已拒绝导入'; END IF;
    START TRANSACTION;

    -- PYV3-021 计算 4×5
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303231 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8AEA1E7AE97203420E4B998203520E79A84E7BB93E69E9CE5B9B6E8BE93E587BAE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x7072696E742834202A2035290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303231E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8AEA1E7AE97203420E4B998203520E79A84E7BB93E69E9CE5B9B6E8BE93E587BAE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x7072696E742834202A2035290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303231E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303231 USING utf8mb4),CONVERT(0xE8AEA1E7AE972034C39735 USING utf8mb4),CONVERT(0xE4B998E6B395EFBC8C7072696E74E8BE93E587BA USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE8AEA1E7AE97E7BB93E69E9C203230E38082 USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0x507974686F6E20E4B8ADE4B998E6B395E7ACA6E58FB7E698AF202AE38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE4B88DE8A681E4BDBFE794A8E695B0E5ADA6E4B9A6E4B8ADE79A84E4B998E58FB720C39720E4BD9CE4B8BAE8BF90E7AE97E7ACA6E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0x32300A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0x32300A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-022 计算 20÷4
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303232 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8AEA1E7AE9720323020E999A4E4BBA5203420E79A84E695B4E695B0E7BB93E69E9CE5B9B6E8BE93E587BAE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x7072696E74283230202F2F2034290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303232E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8AEA1E7AE9720323020E999A4E4BBA5203420E79A84E695B4E695B0E7BB93E69E9CE5B9B6E8BE93E587BAE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x7072696E74283230202F2F2034290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303232E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303232 USING utf8mb4),CONVERT(0xE8AEA1E7AE97203230C3B734 USING utf8mb4),CONVERT(0xE695B4E695B0E999A4E6B395EFBC8C7072696E74E8BE93E587BA USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE695B4E695B02035E38082 USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE7BB93E69E9CE8A681E6B182E698AFE695B4E695B0E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE58FAFE4BBA5E4BDBFE794A8202F2F20E5BE97E588B0E695B4E695B0E59586E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0x350A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0x350A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-023 三个数相加
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303233 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE68A8A2033E3808135E380813720E4B889E4B8AAE695B4E695B0E79BB8E58AA0EFBC8CE5B9B6E8BE93E587BAE680BBE5928CE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x61203D20330A62203D20350A63203D20370A7072696E742861202B2062202B2063290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303233E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE68A8A2033E3808135E380813720E4B889E4B8AAE695B4E695B0E79BB8E58AA0EFBC8CE5B9B6E8BE93E587BAE680BBE5928CE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x61203D20330A62203D20350A63203D20370A7072696E742861202B2062202B2063290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303233E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303233 USING utf8mb4),CONVERT(0xE4B889E4B8AAE695B0E79BB8E58AA0 USING utf8mb4),CONVERT(0xE58AA0E6B395EFBC8CE58F98E9878F USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B889E4B8AAE695B0E79A84E5928C203135E38082 USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE4B889E4B8AAE695B0E983BDE698AFE59BBAE5AE9AE695B4E695B0E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE58FAFE4BBA5E58588E68A8AE4B889E4B8AAE695B0E58886E588ABE4BF9DE5AD98E588B0E58F98E9878FE4B8ADE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0x31350A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0x31350A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-024 记录小明的年龄
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303234 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE794A8E58F98E9878F2061676520E8AEB0E5BD95E5B08FE6988EE79A84E5B9B4E9BE84203132EFBC8CE5B9B6E8BE93E587BAE8BF99E4B8AAE58F98E9878FE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x616765203D2031320A7072696E7428616765290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303234E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE794A8E58F98E9878F2061676520E8AEB0E5BD95E5B08FE6988EE79A84E5B9B4E9BE84203132EFBC8CE5B9B6E8BE93E587BAE8BF99E4B8AAE58F98E9878FE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x616765203D2031320A7072696E7428616765290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303234E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303234 USING utf8mb4),CONVERT(0xE8AEB0E5BD95E5B08FE6988EE79A84E5B9B4E9BE84 USING utf8mb4),CONVERT(0xE58F98E9878FEFBC8CE8B58BE580BC USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE695B4E695B0203132E38082 USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE58F98E9878FE5908DE5BF85E9A1BBE883BDE8A1A8E8BEBEE5AE83E4BF9DE5AD98E79A84E58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE58588E8B58BE580BCEFBC8CE5868DE68A8AE58F98E9878FE4BAA4E7BB99207072696E742829E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0x31320A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0x31320A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-025 明年我几岁
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303235 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4B880E4B8AAE695B4E695B0E8A1A8E7A4BAE4BDA0E4BB8AE5B9B4E79A84E5B9B4E9BE84EFBC8CE8AEA1E7AE97E5B9B6E8BE93E587BAE6988EE5B9B4E79A84E5B9B4E9BE84E38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x616765203D20696E7428696E7075742829290A7072696E7428616765202B2031290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303235E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4B880E4B8AAE695B4E695B0E8A1A8E7A4BAE4BDA0E4BB8AE5B9B4E79A84E5B9B4E9BE84EFBC8CE8AEA1E7AE97E5B9B6E8BE93E587BAE6988EE5B9B4E79A84E5B9B4E9BE84E38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x616765203D20696E7428696E7075742829290A7072696E7428616765202B2031290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303235E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303235 USING utf8mb4),CONVERT(0xE6988EE5B9B4E68891E587A0E5B281 USING utf8mb4),CONVERT(0xE58F98E9878FEFBC8CE58AA0E6B395EFBC8CE8BE93E585A5 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE695B4E695B0EFBC8CE8A1A8E7A4BAE4BB8AE5B9B4E79A84E5B9B4E9BE84E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E8A18CE695B4E695B0EFBC8CE8A1A8E7A4BAE6988EE5B9B4E79A84E5B9B4E9BE84E38082 USING utf8mb4),CONVERT(0xE8BE93E585A520313220E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA203133E38082 USING utf8mb4),CONVERT(0x3120E289A420E5B9B4E9BE8420E289A420313030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE68A8AE8BE93E585A5E8BDACE68DA2E4B8BAE695B4E695B0EFBC8CE5868DE58AA02031E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x31320A USING utf8mb4),CONVERT(0x31330A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x310A USING utf8mb4),CONVERT(0x320A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x39390A USING utf8mb4),CONVERT(0x3130300A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x34350A USING utf8mb4),CONVERT(0x34360A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-026 两数相减
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303236 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4B8A4E4B8AAE695B4E695B0206120E5928C2062EFBC8CE8BE93E587BA20612D6220E79A84E7BB93E69E9CE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x612C2062203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E742861202D2062290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303236E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4B8A4E4B8AAE695B4E695B0206120E5928C2062EFBC8CE8BE93E587BA20612D6220E79A84E7BB93E69E9CE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x612C2062203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E742861202D2062290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303236E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303236 USING utf8mb4),CONVERT(0xE4B8A4E695B0E79BB8E5878F USING utf8mb4),CONVERT(0xE58F98E9878FEFBC8CE5878FE6B395EFBC8CE8BE93E585A5 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B8A4E4B8AAE695B4E695B02061E3808162EFBC8CE4B8ADE997B4E794A8E4B880E4B8AAE7A9BAE6A0BCE58886E99A94E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E8A18CE695B4E695B0EFBC8CE8A1A8E7A4BA20612D62E38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203130203320E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA2037E38082 USING utf8mb4),CONVERT(0x2D3130303020E289A420612C6220E289A42031303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE58588E794A82073706C6974282920E58886E5BC80E4B8A4E4B8AAE695B0EFBC8CE5868DE5819AE5878FE6B395E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x313020330A USING utf8mb4),CONVERT(0x370A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x332031300A USING utf8mb4),CONVERT(0x2D370A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x3020300A USING utf8mb4),CONVERT(0x300A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x2D35202D380A USING utf8mb4),CONVERT(0x330A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-027 两数相乘
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303237 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4B8A4E4B8AAE695B4E695B0EFBC8CE8AEA1E7AE97E5B9B6E8BE93E587BAE5AE83E4BBACE79A84E4B998E7A7AFE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x612C2062203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E742861202A2062290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303237E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4B8A4E4B8AAE695B4E695B0EFBC8CE8AEA1E7AE97E5B9B6E8BE93E587BAE5AE83E4BBACE79A84E4B998E7A7AFE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x612C2062203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E742861202A2062290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303237E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303237 USING utf8mb4),CONVERT(0xE4B8A4E695B0E79BB8E4B998 USING utf8mb4),CONVERT(0xE58F98E9878FEFBC8CE4B998E6B395EFBC8CE8BE93E585A5 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B8A4E4B8AAE695B4E695B0EFBC8CE4B8ADE997B4E794A8E4B880E4B8AAE7A9BAE6A0BCE58886E99A94E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E8A18CE695B4E695B0EFBC8CE8A1A8E7A4BAE4B8A4E695B0E4B998E7A7AFE38082 USING utf8mb4),CONVERT(0xE8BE93E585A52034203520E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA203230E38082 USING utf8mb4),CONVERT(0x2D31303020E289A420E4B8A4E4B8AAE695B4E695B020E289A420313030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9A507974686F6E20E79A84E4B998E6B395E8BF90E7AE97E7ACA6E698AF202AE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x3420350A USING utf8mb4),CONVERT(0x32300A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x3020390A USING utf8mb4),CONVERT(0x300A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x2D3320360A USING utf8mb4),CONVERT(0x2D31380A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x2D34202D370A USING utf8mb4),CONVERT(0x32380A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-028 求整数除法的商
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303238 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E8A2ABE999A4E695B0E5928CE999A4E695B0EFBC8CE8BE93E587BAE695B4E695B0E999A4E6B395E5BE97E588B0E79A84E59586E38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x612C2062203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E742861202F2F2062290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303238E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E8A2ABE999A4E695B0E5928CE999A4E695B0EFBC8CE8BE93E587BAE695B4E695B0E999A4E6B395E5BE97E588B0E79A84E59586E38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x612C2062203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E742861202F2F2062290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303238E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303238 USING utf8mb4),CONVERT(0xE6B182E695B4E695B0E999A4E6B395E79A84E59586 USING utf8mb4),CONVERT(0xE695B4E695B0E999A4E6B395EFBC8CE8BE93E585A5 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B8A4E4B8AAE695B4E695B02061E3808162EFBC8C6220E4B88DE4B8BA2030E38082 USING utf8mb4),CONVERT(0xE8BE93E587BA20612F2F6220E79A84E695B4E695B0E7BB93E69E9CE38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203230203420E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA2035E38082 USING utf8mb4),CONVERT(0x3020E289A4206120E289A4203130303030EFBC8C3120E289A4206220E289A420313030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE4BDBFE794A8202F2F20E8BF90E7AE97E7ACA6E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x323020340A USING utf8mb4),CONVERT(0x350A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x313720350A USING utf8mb4),CONVERT(0x330A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x3020330A USING utf8mb4),CONVERT(0x300A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x39392031300A USING utf8mb4),CONVERT(0x390A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-029 求除法的余数
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303239 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4B8A4E4B8AAE6ADA3E695B4E695B0EFBC8CE8BE93E587BAE7ACACE4B880E4B8AAE695B0E999A4E4BBA5E7ACACE4BA8CE4B8AAE695B0E79A84E4BD99E695B0E38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x612C2062203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E74286120252062290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303239E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4B8A4E4B8AAE6ADA3E695B4E695B0EFBC8CE8BE93E587BAE7ACACE4B880E4B8AAE695B0E999A4E4BBA5E7ACACE4BA8CE4B8AAE695B0E79A84E4BD99E695B0E38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x612C2062203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E74286120252062290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303239E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303239 USING utf8mb4),CONVERT(0xE6B182E999A4E6B395E79A84E4BD99E695B0 USING utf8mb4),CONVERT(0xE58F96E4BD99EFBC8CE8BE93E585A5 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B8A4E4B8AAE6ADA3E695B4E695B02061E3808162E38082 USING utf8mb4),CONVERT(0xE8BE93E587BA2061256220E79A84E7BB93E69E9CE38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203137203520E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA2032E38082 USING utf8mb4),CONVERT(0x3120E289A4206120E289A4203130303030EFBC8C3120E289A4206220E289A420313030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE58F96E4BD99E8BF90E7AE97E7ACA6E698AF2025E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x313720350A USING utf8mb4),CONVERT(0x320A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x323020340A USING utf8mb4),CONVERT(0x300A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x3320350A USING utf8mb4),CONVERT(0x330A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x31303020390A USING utf8mb4),CONVERT(0x310A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-030 买苹果花多少钱
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303330 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE88BB9E69E9CE6AF8FE4B8AA203320E58583EFBC8CE8BE93E585A5E8B4ADE4B9B0E695B0E9878FEFBC8CE8AEA1E7AE97E680BBE4BBB7E38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x636F756E74203D20696E7428696E7075742829290A7072696E742833202A20636F756E74290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303330E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE88BB9E69E9CE6AF8FE4B8AA203320E58583EFBC8CE8BE93E585A5E8B4ADE4B9B0E695B0E9878FEFBC8CE8AEA1E7AE97E680BBE4BBB7E38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x636F756E74203D20696E7428696E7075742829290A7072696E742833202A20636F756E74290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303330E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303330 USING utf8mb4),CONVERT(0xE4B9B0E88BB9E69E9CE88AB1E5A49AE5B091E992B1 USING utf8mb4),CONVERT(0xE4B998E6B395EFBC8CE7949FE6B4BBE5BA94E794A8 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE695B4E695B0EFBC8CE8A1A8E7A4BAE88BB9E69E9CE695B0E9878FE38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E8A18CE695B4E695B0EFBC8CE8A1A8E7A4BAE680BBE4BBB7E38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203520E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA203135E38082 USING utf8mb4),CONVERT(0x3020E289A420E695B0E9878F20E289A420313030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE680BBE4BBB7E7AD89E4BA8EE58D95E4BBB7E4B998E695B0E9878FE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x350A USING utf8mb4),CONVERT(0x31350A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x300A USING utf8mb4),CONVERT(0x300A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x310A USING utf8mb4),CONVERT(0x330A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x32300A USING utf8mb4),CONVERT(0x36300A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-031 买铅笔花多少钱
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303331 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE99385E7AC94E6AF8FE694AF203220E58583EFBC8CE8BE93E585A5E8B4ADE4B9B0E695B0E9878FEFBC8CE8BE93E587BAE99C80E8A681E79A84E992B1E695B0E38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x636F756E74203D20696E7428696E7075742829290A7072696E742832202A20636F756E74290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303331E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE99385E7AC94E6AF8FE694AF203220E58583EFBC8CE8BE93E585A5E8B4ADE4B9B0E695B0E9878FEFBC8CE8BE93E587BAE99C80E8A681E79A84E992B1E695B0E38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x636F756E74203D20696E7428696E7075742829290A7072696E742832202A20636F756E74290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303331E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303331 USING utf8mb4),CONVERT(0xE4B9B0E99385E7AC94E88AB1E5A49AE5B091E992B1 USING utf8mb4),CONVERT(0xE4B998E6B395EFBC8CE7949FE6B4BBE5BA94E794A8 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE695B4E695B0EFBC8CE8A1A8E7A4BAE99385E7AC94E694AFE695B0E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E8A18CE695B4E695B0EFBC8CE8A1A8E7A4BAE680BBE4BBB7E38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203620E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA203132E38082 USING utf8mb4),CONVERT(0x3020E289A420E695B0E9878F20E289A420323030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE794A8E59BBAE5AE9AE58D95E4BBB7203220E4B998E694AFE695B0E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x360A USING utf8mb4),CONVERT(0x31320A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x300A USING utf8mb4),CONVERT(0x300A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x310A USING utf8mb4),CONVERT(0x320A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x35300A USING utf8mb4),CONVERT(0x3130300A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-032 买书包花多少钱
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303332 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE4B880E4B8AAE4B9A6E58C8520363820E58583EFBC8CE8BF98E8A681E4BB98203220E58583E78EAFE4BF9DE8A28BE8B4B9E794A8E38082E8BE93E585A5E8B4ADE4B9B0E79A84E4B9A6E58C85E695B0E9878FEFBC8CE8BE93E587BAE680BBE8B4B9E794A8E38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x636F756E74203D20696E7428696E7075742829290A7072696E74283638202A20636F756E74202B2032290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303332E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE4B880E4B8AAE4B9A6E58C8520363820E58583EFBC8CE8BF98E8A681E4BB98203220E58583E78EAFE4BF9DE8A28BE8B4B9E794A8E38082E8BE93E585A5E8B4ADE4B9B0E79A84E4B9A6E58C85E695B0E9878FEFBC8CE8BE93E587BAE680BBE8B4B9E794A8E38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x636F756E74203D20696E7428696E7075742829290A7072696E74283638202A20636F756E74202B2032290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303332E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303332 USING utf8mb4),CONVERT(0xE4B9B0E4B9A6E58C85E88AB1E5A49AE5B091E992B1 USING utf8mb4),CONVERT(0xE58AA0E6B395EFBC8CE7949FE6B4BBE5BA94E794A8 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE695B4E695B0EFBC8CE8A1A8E7A4BAE4B9A6E58C85E695B0E9878FE38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E8A18CE695B4E695B0EFBC8CE8A1A8E7A4BAE4B9A6E58C85E680BBE4BBB7E58AA0203220E58583E78EAFE4BF9DE8A28BE8B4B9E794A8E38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203120E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA203730E38082 USING utf8mb4),CONVERT(0x3120E289A420E695B0E9878F20E289A4203130E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE58588E7AE97E4B9A6E58C85E680BBE4BBB7EFBC8CE5868DE58AA0E59BBAE5AE9AE79A84203220E58583E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x310A USING utf8mb4),CONVERT(0x37300A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x320A USING utf8mb4),CONVERT(0x3133380A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x350A USING utf8mb4),CONVERT(0x3334320A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x31300A USING utf8mb4),CONVERT(0x3638320A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-033 买东西找零钱
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303333 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4BB98E6ACBEE98791E9A29DE5928CE59586E59381E4BBB7E6A0BCEFBC8CE8AEA1E7AE97E5BA94E689BEE59B9EE5A49AE5B091E992B1E38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x706169642C207072696365203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E742870616964202D207072696365290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303333E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4BB98E6ACBEE98791E9A29DE5928CE59586E59381E4BBB7E6A0BCEFBC8CE8AEA1E7AE97E5BA94E689BEE59B9EE5A49AE5B091E992B1E38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x706169642C207072696365203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E742870616964202D207072696365290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303333E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303333 USING utf8mb4),CONVERT(0xE4B9B0E4B89CE8A5BFE689BEE99BB6E992B1 USING utf8mb4),CONVERT(0xE5878FE6B395EFBC8CE7949FE6B4BBE5BA94E794A8 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B8A4E4B8AAE695B4E695B0EFBC8CE4BE9DE6ACA1E698AFE4BB98E6ACBEE98791E9A29DE5928CE59586E59381E4BBB7E6A0BCE38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E8A18CE695B4E695B0EFBC8CE8A1A8E7A4BAE689BEE99BB6E98791E9A29DE38082 USING utf8mb4),CONVERT(0xE8BE93E585A52031303020363820E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA203332E38082 USING utf8mb4),CONVERT(0xE4BB98E6ACBEE98791E9A29DE4B88DE5B08FE4BA8EE59586E59381E4BBB7E6A0BCEFBC8CE4BA8CE88085E4B88DE8B685E8BF872031303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE689BEE99BB6E7AD89E4BA8EE4BB98E6ACBEE98791E9A29DE5878FE59586E59381E4BBB7E6A0BCE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x3130302036380A USING utf8mb4),CONVERT(0x33320A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x35302035300A USING utf8mb4),CONVERT(0x300A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x323020370A USING utf8mb4),CONVERT(0x31330A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x3130303020310A USING utf8mb4),CONVERT(0x3939390A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-034 正方形的周长
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303334 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E6ADA3E696B9E5BDA2E8BEB9E995BFEFBC8CE8AEA1E7AE97E5B9B6E8BE93E587BAE591A8E995BFE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x73696465203D20696E7428696E7075742829290A7072696E742873696465202A2034290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303334E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E6ADA3E696B9E5BDA2E8BEB9E995BFEFBC8CE8AEA1E7AE97E5B9B6E8BE93E587BAE591A8E995BFE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x73696465203D20696E7428696E7075742829290A7072696E742873696465202A2034290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303334E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303334 USING utf8mb4),CONVERT(0xE6ADA3E696B9E5BDA2E79A84E591A8E995BF USING utf8mb4),CONVERT(0xE4B998E6B395EFBC8CE587A0E4BD95E5BA94E794A8 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE6ADA3E695B4E695B0EFBC8CE8A1A8E7A4BAE8BEB9E995BFE38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE8BEB9E995BFE4B998203420E79A84E7BB93E69E9CE38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203520E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA203230E38082 USING utf8mb4),CONVERT(0x3120E289A420E8BEB9E995BF20E289A42031303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE6ADA3E696B9E5BDA2E59B9BE69DA1E8BEB9E4B880E6A0B7E995BFE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x350A USING utf8mb4),CONVERT(0x32300A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x310A USING utf8mb4),CONVERT(0x340A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x31300A USING utf8mb4),CONVERT(0x34300A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x313030300A USING utf8mb4),CONVERT(0x343030300A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-035 正方形的面积
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303335 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E6ADA3E696B9E5BDA2E8BEB9E995BFEFBC8CE8AEA1E7AE97E5B9B6E8BE93E587BAE99DA2E7A7AFE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x73696465203D20696E7428696E7075742829290A7072696E742873696465202A2073696465290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303335E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E6ADA3E696B9E5BDA2E8BEB9E995BFEFBC8CE8AEA1E7AE97E5B9B6E8BE93E587BAE99DA2E7A7AFE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x73696465203D20696E7428696E7075742829290A7072696E742873696465202A2073696465290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303335E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303335 USING utf8mb4),CONVERT(0xE6ADA3E696B9E5BDA2E79A84E99DA2E7A7AF USING utf8mb4),CONVERT(0xE4B998E6B395EFBC8CE587A0E4BD95E5BA94E794A8 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE6ADA3E695B4E695B0EFBC8CE8A1A8E7A4BAE8BEB9E995BFE38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE8BEB9E995BFE4B998E8BEB9E995BFE79A84E7BB93E69E9CE38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203520E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA203235E38082 USING utf8mb4),CONVERT(0x3120E289A420E8BEB9E995BF20E289A42031303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE99DA2E7A7AFE7AD89E4BA8EE8BEB9E995BFC397E8BEB9E995BFE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x350A USING utf8mb4),CONVERT(0x32350A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x310A USING utf8mb4),CONVERT(0x310A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x31320A USING utf8mb4),CONVERT(0x3134340A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x313030300A USING utf8mb4),CONVERT(0x313030303030300A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-036 长方形的周长
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303336 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E995BFE696B9E5BDA2E79A84E995BFE5928CE5AEBDEFBC8CE8AEA1E7AE97E591A8E995BFE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x6C656E6774682C207769647468203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E7428286C656E677468202B20776964746829202A2032290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303336E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E995BFE696B9E5BDA2E79A84E995BFE5928CE5AEBDEFBC8CE8AEA1E7AE97E591A8E995BFE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x6C656E6774682C207769647468203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E7428286C656E677468202B20776964746829202A2032290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303336E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303336 USING utf8mb4),CONVERT(0xE995BFE696B9E5BDA2E79A84E591A8E995BF USING utf8mb4),CONVERT(0xE58AA0E6B395EFBC8CE4B998E6B395EFBC8CE587A0E4BD95E5BA94E794A8 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B8A4E4B8AAE6ADA3E695B4E695B0EFBC8CE4BE9DE6ACA1E8A1A8E7A4BAE995BFE5928CE5AEBDE38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE995BFE696B9E5BDA2E591A8E995BFE38082 USING utf8mb4),CONVERT(0xE8BE93E585A52036203420E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA203230E38082 USING utf8mb4),CONVERT(0x3120E289A420E995BFE38081E5AEBD20E289A42031303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE58588E68A8AE995BFE5928CE5AEBDE79BB8E58AA0EFBC8CE5868DE4B9982032E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x3620340A USING utf8mb4),CONVERT(0x32300A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x3120310A USING utf8mb4),CONVERT(0x340A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x313020330A USING utf8mb4),CONVERT(0x32360A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x31303030203939390A USING utf8mb4),CONVERT(0x333939380A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-037 长方形的面积
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303337 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E995BFE696B9E5BDA2E79A84E995BFE5928CE5AEBDEFBC8CE8AEA1E7AE97E99DA2E7A7AFE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x6C656E6774682C207769647468203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E74286C656E677468202A207769647468290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303337E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E995BFE696B9E5BDA2E79A84E995BFE5928CE5AEBDEFBC8CE8AEA1E7AE97E99DA2E7A7AFE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x6C656E6774682C207769647468203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E74286C656E677468202A207769647468290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303337E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303337 USING utf8mb4),CONVERT(0xE995BFE696B9E5BDA2E79A84E99DA2E7A7AF USING utf8mb4),CONVERT(0xE4B998E6B395EFBC8CE587A0E4BD95E5BA94E794A8 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B8A4E4B8AAE6ADA3E695B4E695B0EFBC8CE4BE9DE6ACA1E8A1A8E7A4BAE995BFE5928CE5AEBDE38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE995BFE696B9E5BDA2E99DA2E7A7AFE38082 USING utf8mb4),CONVERT(0xE8BE93E585A52036203420E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA203234E38082 USING utf8mb4),CONVERT(0x3120E289A420E995BFE38081E5AEBD20E289A42031303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE99DA2E7A7AFE7AD89E4BA8EE995BFE4B998E5AEBDE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x3620340A USING utf8mb4),CONVERT(0x32340A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x3120310A USING utf8mb4),CONVERT(0x310A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x313020330A USING utf8mb4),CONVERT(0x33300A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x31303030203939390A USING utf8mb4),CONVERT(0x3939393030300A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-038 三角形的周长
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303338 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4B889E8A792E5BDA2E4B889E69DA1E8BEB9E79A84E995BFE5BAA6EFBC8CE8BE93E587BAE4B889E69DA1E8BEB9E995BFE5BAA6E4B98BE5928CE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x612C20622C2063203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E742861202B2062202B2063290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303338E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4B889E8A792E5BDA2E4B889E69DA1E8BEB9E79A84E995BFE5BAA6EFBC8CE8BE93E587BAE4B889E69DA1E8BEB9E995BFE5BAA6E4B98BE5928CE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x612C20622C2063203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E742861202B2062202B2063290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303338E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303338 USING utf8mb4),CONVERT(0xE4B889E8A792E5BDA2E79A84E591A8E995BF USING utf8mb4),CONVERT(0xE58AA0E6B395EFBC8CE587A0E4BD95E5BA94E794A8 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B889E4B8AAE6ADA3E695B4E695B0EFBC8CE4BE9DE6ACA1E8A1A8E7A4BAE4B889E69DA1E8BEB9E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B889E8A792E5BDA2E591A8E995BFE38082 USING utf8mb4),CONVERT(0xE8BE93E585A520332034203520E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA203132E38082 USING utf8mb4),CONVERT(0xE8BE93E585A5E4BF9DE8AF81E58FAFE4BBA5E7BB84E68890E4B889E8A792E5BDA2EFBC8CE6AF8FE69DA1E8BEB9E4B88DE8B685E8BF872031303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE591A8E995BFE5B0B1E698AFE4B889E69DA1E8BEB9E79BB8E58AA0E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x33203420350A USING utf8mb4),CONVERT(0x31320A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x35203520350A USING utf8mb4),CONVERT(0x31350A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x32203320340A USING utf8mb4),CONVERT(0x390A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x31303020313230203135300A USING utf8mb4),CONVERT(0x3337300A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-039 温度升高一度
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303339 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E78EB0E59CA8E79A84E695B4E695B0E6B8A9E5BAA6EFBC8CE8BE93E587BAE58D87E9AB98203120E69184E6B08FE5BAA6E5908EE79A84E6B8A9E5BAA6E38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x74656D7065726174757265203D20696E7428696E7075742829290A7072696E742874656D7065726174757265202B2031290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303339E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E78EB0E59CA8E79A84E695B4E695B0E6B8A9E5BAA6EFBC8CE8BE93E587BAE58D87E9AB98203120E69184E6B08FE5BAA6E5908EE79A84E6B8A9E5BAA6E38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x74656D7065726174757265203D20696E7428696E7075742829290A7072696E742874656D7065726174757265202B2031290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303339E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303339 USING utf8mb4),CONVERT(0xE6B8A9E5BAA6E58D87E9AB98E4B880E5BAA6 USING utf8mb4),CONVERT(0xE58F98E9878FEFBC8CE58AA0E6B395EFBC8CE8BE93E585A5 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE695B4E695B0EFBC8CE8A1A8E7A4BAE5BD93E5898DE6B8A9E5BAA6E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E8A18CE695B4E695B0EFBC8CE8A1A8E7A4BAE58D87E9AB98E5908EE79A84E6B8A9E5BAA6E38082 USING utf8mb4),CONVERT(0xE8BE93E585A520323020E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA203231E38082 USING utf8mb4),CONVERT(0x2D353020E289A420E6B8A9E5BAA620E289A4203530E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE6B8A9E5BAA6E58FAFE883BDE698AFE8B49FE695B0EFBC8CE79BB4E68EA5E58AA0203120E58DB3E58FAFE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x32300A USING utf8mb4),CONVERT(0x32310A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x300A USING utf8mb4),CONVERT(0x310A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x2D350A USING utf8mb4),CONVERT(0x2D340A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x35300A USING utf8mb4),CONVERT(0x35310A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-040 计算三个成绩的平均分
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303430 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4B889E4B8AAE695B4E695B0E68890E7BBA9EFBC8CE8AEA1E7AE97E5B9B3E59D87E58886EFBC8CE5B9B6E4BF9DE79599E4B880E4BD8DE5B08FE695B0E8BE93E587BAE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x612C20622C2063203D206D617028696E742C20696E70757428292E73706C69742829290A61766572616765203D202861202B2062202B206329202F20330A7072696E742866227B617665726167653A2E31667D22290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303430E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4B889E4B8AAE695B4E695B0E68890E7BBA9EFBC8CE8AEA1E7AE97E5B9B3E59D87E58886EFBC8CE5B9B6E4BF9DE79599E4B880E4BD8DE5B08FE695B0E8BE93E587BAE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x612C20622C2063203D206D617028696E742C20696E70757428292E73706C69742829290A61766572616765203D202861202B2062202B206329202F20330A7072696E742866227B617665726167653A2E31667D22290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303430E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303430 USING utf8mb4),CONVERT(0xE8AEA1E7AE97E4B889E4B8AAE68890E7BBA9E79A84E5B9B3E59D87E58886 USING utf8mb4),CONVERT(0xE58AA0E6B395EFBC8CE999A4E6B395EFBC8CE6A0BCE5BC8FE58C96E8BE93E587BA USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B889E4B8AAE695B4E695B0E68890E7BBA9EFBC8CE4B8ADE997B4E794A8E7A9BAE6A0BCE58886E99A94E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE5B9B3E59D87E58886EFBC8CE4BF9DE79599E4B880E4BD8DE5B08FE695B0E38082 USING utf8mb4),CONVERT(0xE8BE93E585A520393020383020373020E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA2038302E30E38082 USING utf8mb4),CONVERT(0x3020E289A420E6AF8FE4B8AAE68890E7BBA920E289A420313030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE58588E6B182E680BBE58886EFBC8CE5868DE999A4E4BBA52033EFBC8CE4BDBFE794A8203A2E316620E4BF9DE79599E4B880E4BD8DE5B08FE695B0E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x39302038302037300A USING utf8mb4),CONVERT(0x38302E300A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x31303020313030203130300A USING utf8mb4),CONVERT(0x3130302E300A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x30203020300A USING utf8mb4),CONVERT(0x302E300A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x38382038392039300A USING utf8mb4),CONVERT(0x38392E300A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    SELECT COUNT(*) INTO v_count FROM biz_programming_question_config c INNER JOIN biz_question q ON q.question_id=c.question_id
    WHERE c.external_id IN (CONVERT(0x505956332D303231 USING utf8mb4),CONVERT(0x505956332D303232 USING utf8mb4),CONVERT(0x505956332D303233 USING utf8mb4),CONVERT(0x505956332D303234 USING utf8mb4),CONVERT(0x505956332D303235 USING utf8mb4),CONVERT(0x505956332D303236 USING utf8mb4),CONVERT(0x505956332D303237 USING utf8mb4),CONVERT(0x505956332D303238 USING utf8mb4),CONVERT(0x505956332D303239 USING utf8mb4),CONVERT(0x505956332D303330 USING utf8mb4),CONVERT(0x505956332D303331 USING utf8mb4),CONVERT(0x505956332D303332 USING utf8mb4),CONVERT(0x505956332D303333 USING utf8mb4),CONVERT(0x505956332D303334 USING utf8mb4),CONVERT(0x505956332D303335 USING utf8mb4),CONVERT(0x505956332D303336 USING utf8mb4),CONVERT(0x505956332D303337 USING utf8mb4),CONVERT(0x505956332D303338 USING utf8mb4),CONVERT(0x505956332D303339 USING utf8mb4),CONVERT(0x505956332D303430 USING utf8mb4)) AND c.create_by='python-system-v3' AND q.creator_id=v_creator_id AND CAST(q.create_by AS BINARY)=CAST(v_creator_username AS BINARY) AND q.question_type='practical' AND q.practical_mode='PYTHON' AND q.grade IS NULL AND q.semester IS NULL AND q.lesson_num IS NULL AND q.is_public='Y' AND c.validation_status='VALID' AND c.enabled='1';
    IF v_count<>20 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='本批 PYV3 题目或配置后检数量不一致，事务已回滚'; END IF;
    SELECT COUNT(*) INTO v_count FROM biz_programming_test_case tc INNER JOIN biz_programming_question_config c ON c.question_id=tc.question_id
    WHERE c.external_id IN (CONVERT(0x505956332D303231 USING utf8mb4),CONVERT(0x505956332D303232 USING utf8mb4),CONVERT(0x505956332D303233 USING utf8mb4),CONVERT(0x505956332D303234 USING utf8mb4),CONVERT(0x505956332D303235 USING utf8mb4),CONVERT(0x505956332D303236 USING utf8mb4),CONVERT(0x505956332D303237 USING utf8mb4),CONVERT(0x505956332D303238 USING utf8mb4),CONVERT(0x505956332D303239 USING utf8mb4),CONVERT(0x505956332D303330 USING utf8mb4),CONVERT(0x505956332D303331 USING utf8mb4),CONVERT(0x505956332D303332 USING utf8mb4),CONVERT(0x505956332D303333 USING utf8mb4),CONVERT(0x505956332D303334 USING utf8mb4),CONVERT(0x505956332D303335 USING utf8mb4),CONVERT(0x505956332D303336 USING utf8mb4),CONVERT(0x505956332D303337 USING utf8mb4),CONVERT(0x505956332D303338 USING utf8mb4),CONVERT(0x505956332D303339 USING utf8mb4),CONVERT(0x505956332D303430 USING utf8mb4));
    IF v_count<>72 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='PYV3 测试点后检数量不一致，事务已回滚'; END IF;
    SELECT COUNT(*) INTO v_count FROM (
        SELECT c.question_id FROM biz_programming_question_config c INNER JOIN biz_programming_test_case tc ON tc.question_id=c.question_id
        WHERE c.external_id IN (CONVERT(0x505956332D303231 USING utf8mb4),CONVERT(0x505956332D303232 USING utf8mb4),CONVERT(0x505956332D303233 USING utf8mb4),CONVERT(0x505956332D303234 USING utf8mb4),CONVERT(0x505956332D303235 USING utf8mb4),CONVERT(0x505956332D303236 USING utf8mb4),CONVERT(0x505956332D303237 USING utf8mb4),CONVERT(0x505956332D303238 USING utf8mb4),CONVERT(0x505956332D303239 USING utf8mb4),CONVERT(0x505956332D303330 USING utf8mb4),CONVERT(0x505956332D303331 USING utf8mb4),CONVERT(0x505956332D303332 USING utf8mb4),CONVERT(0x505956332D303333 USING utf8mb4),CONVERT(0x505956332D303334 USING utf8mb4),CONVERT(0x505956332D303335 USING utf8mb4),CONVERT(0x505956332D303336 USING utf8mb4),CONVERT(0x505956332D303337 USING utf8mb4),CONVERT(0x505956332D303338 USING utf8mb4),CONVERT(0x505956332D303339 USING utf8mb4),CONVERT(0x505956332D303430 USING utf8mb4)) GROUP BY c.question_id,c.no_input
        HAVING SUM(tc.is_public='1')>=1 AND SUM(tc.is_public='0')>=1 AND ROUND(SUM(tc.score_weight),6)=100
           AND ((c.no_input='1' AND COUNT(*)=2) OR (c.no_input='0' AND COUNT(*) BETWEEN 3 AND 5))
    ) checked_questions;
    IF v_count<>20 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='本批 PYV3 测试点公开性、数量或权重后检失败，事务已回滚'; END IF;
    COMMIT;
END$$
DELIMITER ;
CALL import_python_zero_basis_v3_batch_02();
DROP PROCEDURE IF EXISTS import_python_zero_basis_v3_batch_02;
SELECT q.difficulty,COUNT(*) question_count FROM biz_programming_question_config c INNER JOIN biz_question q ON q.question_id=c.question_id
WHERE c.external_id IN (CONVERT(0x505956332D303231 USING utf8mb4),CONVERT(0x505956332D303232 USING utf8mb4),CONVERT(0x505956332D303233 USING utf8mb4),CONVERT(0x505956332D303234 USING utf8mb4),CONVERT(0x505956332D303235 USING utf8mb4),CONVERT(0x505956332D303236 USING utf8mb4),CONVERT(0x505956332D303237 USING utf8mb4),CONVERT(0x505956332D303238 USING utf8mb4),CONVERT(0x505956332D303239 USING utf8mb4),CONVERT(0x505956332D303330 USING utf8mb4),CONVERT(0x505956332D303331 USING utf8mb4),CONVERT(0x505956332D303332 USING utf8mb4),CONVERT(0x505956332D303333 USING utf8mb4),CONVERT(0x505956332D303334 USING utf8mb4),CONVERT(0x505956332D303335 USING utf8mb4),CONVERT(0x505956332D303336 USING utf8mb4),CONVERT(0x505956332D303337 USING utf8mb4),CONVERT(0x505956332D303338 USING utf8mb4),CONVERT(0x505956332D303339 USING utf8mb4),CONVERT(0x505956332D303430 USING utf8mb4)) GROUP BY q.difficulty ORDER BY q.difficulty;
SELECT COUNT(*) question_count,SUM(case_count) test_case_count FROM (SELECT c.question_id,COUNT(tc.test_case_id) case_count FROM biz_programming_question_config c INNER JOIN biz_programming_test_case tc ON tc.question_id=c.question_id
WHERE c.external_id IN (CONVERT(0x505956332D303231 USING utf8mb4),CONVERT(0x505956332D303232 USING utf8mb4),CONVERT(0x505956332D303233 USING utf8mb4),CONVERT(0x505956332D303234 USING utf8mb4),CONVERT(0x505956332D303235 USING utf8mb4),CONVERT(0x505956332D303236 USING utf8mb4),CONVERT(0x505956332D303237 USING utf8mb4),CONVERT(0x505956332D303238 USING utf8mb4),CONVERT(0x505956332D303239 USING utf8mb4),CONVERT(0x505956332D303330 USING utf8mb4),CONVERT(0x505956332D303331 USING utf8mb4),CONVERT(0x505956332D303332 USING utf8mb4),CONVERT(0x505956332D303333 USING utf8mb4),CONVERT(0x505956332D303334 USING utf8mb4),CONVERT(0x505956332D303335 USING utf8mb4),CONVERT(0x505956332D303336 USING utf8mb4),CONVERT(0x505956332D303337 USING utf8mb4),CONVERT(0x505956332D303338 USING utf8mb4),CONVERT(0x505956332D303339 USING utf8mb4),CONVERT(0x505956332D303430 USING utf8mb4)) GROUP BY c.question_id) x;
