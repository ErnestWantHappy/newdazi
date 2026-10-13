-- Python 零基础系统题库 V3 第 1 批幂等导入脚本。
-- 执行前必须备份目标数据库；每次执行会按 PYV3 外部题号更新本题包并重建对应测试点。
-- 全部中文以 UTF-8 十六进制写入，避免 Windows 客户端代码页污染。
-- 题目数：20；测试点数：40。
-- batch-01.json: 600B00BC6E83D2600735D3133B033C54AA5A888D8DDDC647EF87250819CBF13B
SET NAMES utf8mb4;
DROP PROCEDURE IF EXISTS import_python_zero_basis_v3_batch_01;
DELIMITER $$
CREATE PROCEDURE import_python_zero_basis_v3_batch_01()
BEGIN
    DECLARE v_count INT DEFAULT 0;
    DECLARE v_creator_id BIGINT DEFAULT NULL;
    DECLARE v_creator_username VARCHAR(64) DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    SELECT COUNT(*) INTO v_count FROM sys_user u WHERE u.nick_name=CONVERT(0xE98391E4B89CE697AD USING utf8mb4) COLLATE utf8mb4_general_ci AND u.status='0' AND u.del_flag='0' AND EXISTS (SELECT 1 FROM sys_user_role ur INNER JOIN sys_role r ON r.role_id=ur.role_id WHERE ur.user_id=u.user_id AND r.role_key='teacher' AND r.status='0');
    IF v_count<>1 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='郑东旭有效教师账号不是唯一一条，已拒绝导入'; END IF;
    SELECT u.user_id,u.user_name INTO v_creator_id,v_creator_username FROM sys_user u WHERE u.nick_name=CONVERT(0xE98391E4B89CE697AD USING utf8mb4) COLLATE utf8mb4_general_ci AND u.status='0' AND u.del_flag='0' AND EXISTS (SELECT 1 FROM sys_user_role ur INNER JOIN sys_role r ON r.role_id=ur.role_id WHERE ur.user_id=u.user_id AND r.role_key='teacher' AND r.status='0') LIMIT 1;
    SELECT COUNT(*) INTO v_count FROM biz_programming_question_config WHERE external_id IN (CONVERT(0x505956332D303031 USING utf8mb4),CONVERT(0x505956332D303032 USING utf8mb4),CONVERT(0x505956332D303033 USING utf8mb4),CONVERT(0x505956332D303034 USING utf8mb4),CONVERT(0x505956332D303035 USING utf8mb4),CONVERT(0x505956332D303036 USING utf8mb4),CONVERT(0x505956332D303037 USING utf8mb4),CONVERT(0x505956332D303038 USING utf8mb4),CONVERT(0x505956332D303039 USING utf8mb4),CONVERT(0x505956332D303130 USING utf8mb4),CONVERT(0x505956332D303131 USING utf8mb4),CONVERT(0x505956332D303132 USING utf8mb4),CONVERT(0x505956332D303133 USING utf8mb4),CONVERT(0x505956332D303134 USING utf8mb4),CONVERT(0x505956332D303135 USING utf8mb4),CONVERT(0x505956332D303136 USING utf8mb4),CONVERT(0x505956332D303137 USING utf8mb4),CONVERT(0x505956332D303138 USING utf8mb4),CONVERT(0x505956332D303139 USING utf8mb4),CONVERT(0x505956332D303230 USING utf8mb4)) AND COALESCE(create_by,'')<>'python-system-v3';
    IF v_count<>0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='PYV3 外部题号已被其他来源占用，已拒绝导入'; END IF;
    SELECT COUNT(*) INTO v_count FROM biz_programming_question_config WHERE title IN (CONVERT(0xE4BDA0E5A5BDEFBC8C507974686F6E USING utf8mb4),CONVERT(0xE8BE93E587BAE695B0E5AD9720313030 USING utf8mb4),CONVERT(0xE8BE93E587BAE68891E79A84E5ADA6E6A0A1 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B8A4E8A18CE69687E5AD97 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B889E8A18CE695B0E5AD97 USING utf8mb4),CONVERT(0xE68891E788B1E4BFA1E681AFE7A791E68A80 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E4B8B2E6989FE58FB7 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B8A4E4B8AAE88BB1E69687E58D95E8AF8D USING utf8mb4),CONVERT(0xE8BE93E587BAE4B8ADE69687E997AEE58099E8AFAD USING utf8mb4),CONVERT(0xE8BE93E587BAE4BB8AE5A4A9E6989FE69C9FE4BA94 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B8A4E6ACA1E6ACA2E8BF8EE8AFAD USING utf8mb4),CONVERT(0xE8BE93E587BAE7AE80E58D95E8A1A8E68385 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E4B8AAE695B4E695B0 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E6AEB5E59BBAE5AE9AE5AFB9E8AF9D USING utf8mb4),CONVERT(0xE8BE93E587BA2048656C6C6F USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E5BCA0E59BBAE5AE9AE68890E7BBA9E58D95 USING utf8mb4),CONVERT(0xE794A8E6989FE58FB7E794BBE5B08FE4B889E8A792E5BDA2 USING utf8mb4),CONVERT(0xE588B6E4BD9CE4B880E5BCA0E69687E5AD97E5B08FE5908DE78987 USING utf8mb4),CONVERT(0xE8AEA1E7AE9720312B32 USING utf8mb4),CONVERT(0xE8AEA1E7AE972031302D33 USING utf8mb4)) AND (external_id IS NULL OR external_id NOT IN (CONVERT(0x505956332D303031 USING utf8mb4),CONVERT(0x505956332D303032 USING utf8mb4),CONVERT(0x505956332D303033 USING utf8mb4),CONVERT(0x505956332D303034 USING utf8mb4),CONVERT(0x505956332D303035 USING utf8mb4),CONVERT(0x505956332D303036 USING utf8mb4),CONVERT(0x505956332D303037 USING utf8mb4),CONVERT(0x505956332D303038 USING utf8mb4),CONVERT(0x505956332D303039 USING utf8mb4),CONVERT(0x505956332D303130 USING utf8mb4),CONVERT(0x505956332D303131 USING utf8mb4),CONVERT(0x505956332D303132 USING utf8mb4),CONVERT(0x505956332D303133 USING utf8mb4),CONVERT(0x505956332D303134 USING utf8mb4),CONVERT(0x505956332D303135 USING utf8mb4),CONVERT(0x505956332D303136 USING utf8mb4),CONVERT(0x505956332D303137 USING utf8mb4),CONVERT(0x505956332D303138 USING utf8mb4),CONVERT(0x505956332D303139 USING utf8mb4),CONVERT(0x505956332D303230 USING utf8mb4)));
    IF v_count<>0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='题库已存在同名非 PYV3 题目，已拒绝导入'; END IF;
    START TRANSACTION;

    -- PYV3-001 你好，Python
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303031 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE7BC96E58699E7A88BE5BA8FEFBC8CE59CA8E5B18FE5B995E4B88AE8BE93E587BAE4B880E8A18CE2809CE4BDA0E5A5BDEFBC8C507974686F6EE2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x7072696E742822E4BDA0E5A5BDEFBC8C507974686F6E22290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303031E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE7BC96E58699E7A88BE5BA8FEFBC8CE59CA8E5B18FE5B995E4B88AE8BE93E587BAE4B880E8A18CE2809CE4BDA0E5A5BDEFBC8C507974686F6EE2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x7072696E742822E4BDA0E5A5BDEFBC8C507974686F6E22290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303031E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303031 USING utf8mb4),CONVERT(0xE4BDA0E5A5BDEFBC8C507974686F6E USING utf8mb4),CONVERT(0x7072696E74E8BE93E587BA USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E8A18CEFBC9AE4BDA0E5A5BDEFBC8C507974686F6E USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE5BF85E9A1BBE5AE8CE585A8E68C89E785A7E68C87E5AE9AE69687E5AD97E8BE93E587BAE38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE68A8AE8A681E8BE93E587BAE79A84E69687E5AD97E694BEE59CA8207072696E74282920E79A84E68BACE58FB7E4B8ADEFBC8CE5B9B6E794A8E5BC95E58FB7E68BACE8B5B7E69DA5E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0xE4BDA0E5A5BDEFBC8C507974686F6E0A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0xE4BDA0E5A5BDEFBC8C507974686F6E0A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-002 输出数字 100
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303032 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE7BC96E58699E7A88BE5BA8FEFBC8CE79BB4E68EA5E8BE93E587BAE695B4E695B020313030E38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x7072696E7428313030290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303032E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE7BC96E58699E7A88BE5BA8FEFBC8CE79BB4E68EA5E8BE93E587BAE695B4E695B020313030E38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x7072696E7428313030290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303032E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303032 USING utf8mb4),CONVERT(0xE8BE93E587BAE695B0E5AD9720313030 USING utf8mb4),CONVERT(0x7072696E74E8BE93E587BAEFBC8CE695B4E695B0 USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E8A18CE695B4E695B020313030E38082 USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE4B88DE8A681E8BE93E587BAE585B6E4BB96E69687E5AD97E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE695B0E5AD97E4B88DE99C80E8A681E58AA0E5BC95E58FB7EFBC8CE4B99FE58FAFE4BBA5E79BB4E68EA5E694BEE8BF9B207072696E742829E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0x3130300A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0x3130300A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-003 输出我的学校
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303033 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE7BC96E58699E7A88BE5BA8FEFBC8CE8BE93E587BAE2809CE68891E79A84E5ADA6E6A0A1E698AFE59091E998B3E5B08FE5ADA6E2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x7072696E742822E68891E79A84E5ADA6E6A0A1E698AFE59091E998B3E5B08FE5ADA622290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303033E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE7BC96E58699E7A88BE5BA8FEFBC8CE8BE93E587BAE2809CE68891E79A84E5ADA6E6A0A1E698AFE59091E998B3E5B08FE5ADA6E2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x7072696E742822E68891E79A84E5ADA6E6A0A1E698AFE59091E998B3E5B08FE5ADA622290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303033E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303033 USING utf8mb4),CONVERT(0xE8BE93E587BAE68891E79A84E5ADA6E6A0A1 USING utf8mb4),CONVERT(0x7072696E74E8BE93E587BAEFBC8CE5AD97E7ACA6E4B8B2 USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E8A18CEFBC9AE68891E79A84E5ADA6E6A0A1E698AFE59091E998B3E5B08FE5ADA6 USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE69687E5AD97E5928CE6A087E782B9E5BF85E9A1BBE5AE8CE585A8E4B880E887B4E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE4B8ADE69687E69687E5AD97E8A681E58699E59CA8E5BC95E58FB7E4B8ADE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0xE68891E79A84E5ADA6E6A0A1E698AFE59091E998B3E5B08FE5ADA60A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0xE68891E79A84E5ADA6E6A0A1E698AFE59091E998B3E5B08FE5ADA60A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-004 输出两行文字
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303034 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE7ACACE4B880E8A18CE8BE93E587BAE2809CE5BC80E5A78BE5ADA6E4B9A0E2809DEFBC8CE7ACACE4BA8CE8A18CE8BE93E587BAE2809CE5A4A9E5A4A9E8BF9BE6ADA5E2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x7072696E742822E5BC80E5A78BE5ADA6E4B9A022290A7072696E742822E5A4A9E5A4A9E8BF9BE6ADA522290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303034E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE7ACACE4B880E8A18CE8BE93E587BAE2809CE5BC80E5A78BE5ADA6E4B9A0E2809DEFBC8CE7ACACE4BA8CE8A18CE8BE93E587BAE2809CE5A4A9E5A4A9E8BF9BE6ADA5E2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x7072696E742822E5BC80E5A78BE5ADA6E4B9A022290A7072696E742822E5A4A9E5A4A9E8BF9BE6ADA522290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303034E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303034 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B8A4E8A18CE69687E5AD97 USING utf8mb4),CONVERT(0x7072696E74E8BE93E587BAEFBC8CE5A49AE8A18CE8BE93E587BA USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE68C89E8A681E6B182E8BE93E587BAE4B8A4E8A18CE69687E5AD97E38082 USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE6AF8FE58FA5E8AF9DE58D95E78BACE58DA0E4B880E8A18CE38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE8BF9EE7BBADE4BDBFE794A8E4B8A4E6ACA1207072696E742829EFBC8CE6AF8FE6ACA1E8BE93E587BAE4B880E8A18CE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0xE5BC80E5A78BE5ADA6E4B9A00AE5A4A9E5A4A9E8BF9BE6ADA50A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0xE5BC80E5A78BE5ADA6E4B9A00AE5A4A9E5A4A9E8BF9BE6ADA50A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-005 输出三行数字
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303035 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE4BE9DE6ACA1E8BE93E587BAE695B0E5AD972031E3808132E3808133EFBC8CE6AF8FE4B8AAE695B0E5AD97E58D95E78BACE58DA0E4B880E8A18CE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x7072696E742831290A7072696E742832290A7072696E742833290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303035E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE4BE9DE6ACA1E8BE93E587BAE695B0E5AD972031E3808132E3808133EFBC8CE6AF8FE4B8AAE695B0E5AD97E58D95E78BACE58DA0E4B880E8A18CE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x7072696E742831290A7072696E742832290A7072696E742833290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303035E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303035 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B889E8A18CE695B0E5AD97 USING utf8mb4),CONVERT(0x7072696E74E8BE93E587BAEFBC8CE5A49AE8A18CE8BE93E587BA USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE4BE9DE6ACA1E8BE93E587BAE4B889E8A18CE695B0E5AD972031E3808132E3808133E38082 USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE6AF8FE8A18CE58FAAE883BDE69C89E4B880E4B8AAE695B0E5AD97E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE58FAFE4BBA5E8BF9EE7BBADE58699E4B889E69DA1207072696E74282920E8AFADE58FA5E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0x310A320A330A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0x310A320A330A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-006 我爱信息科技
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303036 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE7BC96E58699E7A88BE5BA8FEFBC8CE8BE93E587BAE2809CE68891E788B1E4BFA1E681AFE7A791E68A80E2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x7072696E742822E68891E788B1E4BFA1E681AFE7A791E68A8022290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303036E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE7BC96E58699E7A88BE5BA8FEFBC8CE8BE93E587BAE2809CE68891E788B1E4BFA1E681AFE7A791E68A80E2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x7072696E742822E68891E788B1E4BFA1E681AFE7A791E68A8022290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303036E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303036 USING utf8mb4),CONVERT(0xE68891E788B1E4BFA1E681AFE7A791E68A80 USING utf8mb4),CONVERT(0x7072696E74E8BE93E587BAEFBC8CE5AD97E7ACA6E4B8B2 USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E8A18CEFBC9AE68891E788B1E4BFA1E681AFE7A791E68A80 USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE4B88DE8A681E6B7BBE58AA0E58FA5E58FB7E68896E7A9BAE6A0BCE38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE68A8AE5AE8CE695B4E58FA5E5AD90E4BD9CE4B8BAE4B880E4B8AAE5AD97E7ACA6E4B8B2E8BE93E587BAE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0xE68891E788B1E4BFA1E681AFE7A791E68A800A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0xE68891E788B1E4BFA1E681AFE7A791E68A800A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-007 输出一串星号
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303037 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE7BC96E58699E7A88BE5BA8FEFBC8CE8BE93E587BA203820E4B8AAE8BF9EE7BBADE79A84E6989FE58FB7E38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x7072696E7428222A2A2A2A2A2A2A2A22290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303037E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE7BC96E58699E7A88BE5BA8FEFBC8CE8BE93E587BA203820E4B8AAE8BF9EE7BBADE79A84E6989FE58FB7E38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x7072696E7428222A2A2A2A2A2A2A2A22290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303037E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303037 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E4B8B2E6989FE58FB7 USING utf8mb4),CONVERT(0x7072696E74E8BE93E587BAEFBC8CE5AD97E7ACA6E4B8B2 USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E8A18C203820E4B8AAE6989FE58FB7E38082 USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE6989FE58FB7E4B98BE997B4E6B2A1E69C89E7A9BAE6A0BCE38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE58588E695B0E5A5BDE6989FE58FB7E4B8AAE695B0EFBC8CE5868DE694BEE8BF9BE5BC95E58FB7E4B8ADE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0x2A2A2A2A2A2A2A2A0A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0x2A2A2A2A2A2A2A2A0A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-008 输出两个英文单词
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303038 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE59CA8E5908CE4B880E8A18CE8BE93E587BAE88BB1E69687E58D95E8AF8D2048656C6C6F20E5928C20507974686F6EEFBC8CE4B8ADE997B4E79599E4B880E4B8AAE7A9BAE6A0BCE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x7072696E74282248656C6C6F20507974686F6E22290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303038E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE59CA8E5908CE4B880E8A18CE8BE93E587BAE88BB1E69687E58D95E8AF8D2048656C6C6F20E5928C20507974686F6EEFBC8CE4B8ADE997B4E79599E4B880E4B8AAE7A9BAE6A0BCE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x7072696E74282248656C6C6F20507974686F6E22290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303038E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303038 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B8A4E4B8AAE88BB1E69687E58D95E8AF8D USING utf8mb4),CONVERT(0x7072696E74E8BE93E587BAEFBC8CE7A9BAE6A0BC USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E8A18CEFBC9A48656C6C6F20507974686F6E USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE5A4A7E5B08FE58699E5BF85E9A1BBE4B880E887B4EFBC8CE4B8ADE997B4E58FAAE69C89E4B880E4B8AAE7A9BAE6A0BCE38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE58FAFE4BBA5E68A8AE4B8A4E4B8AAE58D95E8AF8DE5928CE7A9BAE6A0BCE4B880E8B5B7E58699E68890E4B880E4B8AAE5AD97E7ACA6E4B8B2E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0x48656C6C6F20507974686F6E0A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0x48656C6C6F20507974686F6E0A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-009 输出中文问候语
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303039 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE7BC96E58699E7A88BE5BA8FEFBC8CE8BE93E587BAE2809CE5908CE5ADA6EFBC8CE4BDA0E5A5BDEFBC81E2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x7072696E742822E5908CE5ADA6EFBC8CE4BDA0E5A5BDEFBC8122290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303039E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE7BC96E58699E7A88BE5BA8FEFBC8CE8BE93E587BAE2809CE5908CE5ADA6EFBC8CE4BDA0E5A5BDEFBC81E2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x7072696E742822E5908CE5ADA6EFBC8CE4BDA0E5A5BDEFBC8122290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303039E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303039 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B8ADE69687E997AEE58099E8AFAD USING utf8mb4),CONVERT(0x7072696E74E8BE93E587BAEFBC8CE5AD97E7ACA6E4B8B2 USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E8A18CEFBC9AE5908CE5ADA6EFBC8CE4BDA0E5A5BDEFBC81 USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE4BDBFE794A8E4B8ADE69687E98097E58FB7E5928CE4B8ADE69687E6849FE58FB9E58FB7E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE6B3A8E6848FE6A0B8E5AFB9E4B8ADE69687E6A087E782B9E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0xE5908CE5ADA6EFBC8CE4BDA0E5A5BDEFBC810A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0xE5908CE5ADA6EFBC8CE4BDA0E5A5BDEFBC810A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-010 输出今天星期五
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303130 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE7BC96E58699E7A88BE5BA8FEFBC8CE8BE93E587BAE2809CE4BB8AE5A4A9E6989FE69C9FE4BA94E2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x7072696E742822E4BB8AE5A4A9E6989FE69C9FE4BA9422290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303130E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE7BC96E58699E7A88BE5BA8FEFBC8CE8BE93E587BAE2809CE4BB8AE5A4A9E6989FE69C9FE4BA94E2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x7072696E742822E4BB8AE5A4A9E6989FE69C9FE4BA9422290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303130E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303130 USING utf8mb4),CONVERT(0xE8BE93E587BAE4BB8AE5A4A9E6989FE69C9FE4BA94 USING utf8mb4),CONVERT(0x7072696E74E8BE93E587BAEFBC8CE5AD97E7ACA6E4B8B2 USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E8A18CEFBC9AE4BB8AE5A4A9E6989FE69C9FE4BA94 USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE4B88DE8A681E8BE93E587BAE697A5E69C9FE68896E585B6E4BB96E8AFB4E6988EE38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE59BBAE5AE9AE69687E5AD97E79BB4E68EA5E794A8207072696E74282920E8BE93E587BAE58DB3E58FAFE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0xE4BB8AE5A4A9E6989FE69C9FE4BA940A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0xE4BB8AE5A4A9E6989FE69C9FE4BA940A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-011 输出两次欢迎语
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303131 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BF9EE7BBADE8BE93E587BAE4B8A4E8A18CE2809CE6ACA2E8BF8EE5ADA6E4B9A0507974686F6EE2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x7072696E742822E6ACA2E8BF8EE5ADA6E4B9A0507974686F6E22290A7072696E742822E6ACA2E8BF8EE5ADA6E4B9A0507974686F6E22290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303131E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BF9EE7BBADE8BE93E587BAE4B8A4E8A18CE2809CE6ACA2E8BF8EE5ADA6E4B9A0507974686F6EE2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x7072696E742822E6ACA2E8BF8EE5ADA6E4B9A0507974686F6E22290A7072696E742822E6ACA2E8BF8EE5ADA6E4B9A0507974686F6E22290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303131E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303131 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B8A4E6ACA1E6ACA2E8BF8EE8AFAD USING utf8mb4),CONVERT(0x7072696E74E8BE93E587BAEFBC8CE9878DE5A48DE8BE93E587BA USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B8A4E8A18CE79BB8E5908CE79A84E6ACA2E8BF8EE8AFADE38082 USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE4B8A4E8A18CE58685E5AEB9E5BF85E9A1BBE5AE8CE585A8E79BB8E5908CE38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE58FAFE4BBA5E8BF9EE7BBADE4BDBFE794A8E4B8A4E6ACA1E79BB8E5908CE79A84207072696E742829E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0xE6ACA2E8BF8EE5ADA6E4B9A0507974686F6E0AE6ACA2E8BF8EE5ADA6E4B9A0507974686F6E0A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0xE6ACA2E8BF8EE5ADA6E4B9A0507974686F6E0AE6ACA2E8BF8EE5ADA6E4B9A0507974686F6E0A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-012 输出简单表情
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303132 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE7BC96E58699E7A88BE5BA8FEFBC8CE8BE93E587BAE794B1E7ACA6E58FB7E7BB84E68890E79A84E7AC91E884B8205E5F5EE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x7072696E7428225E5F5E22290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303132E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE7BC96E58699E7A88BE5BA8FEFBC8CE8BE93E587BAE794B1E7ACA6E58FB7E7BB84E68890E79A84E7AC91E884B8205E5F5EE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x7072696E7428225E5F5E22290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303132E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303132 USING utf8mb4),CONVERT(0xE8BE93E587BAE7AE80E58D95E8A1A8E68385 USING utf8mb4),CONVERT(0x7072696E74E8BE93E587BAEFBC8CE5AD97E7ACA6 USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E8A18CEFBC9A5E5F5E USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE4B88DE8A681E59CA8E7ACA6E58FB7E4B98BE997B4E6B7BBE58AA0E7A9BAE6A0BCE38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE7ACA6E58FB7E4B99FE8A681E4BD9CE4B8BAE5AD97E7ACA6E4B8B2E694BEE8BF9BE5BC95E58FB7E4B8ADE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0x5E5F5E0A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0x5E5F5E0A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-013 输出一个整数
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303133 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE7BC96E58699E7A88BE5BA8FEFBC8CE8BE93E587BAE695B4E695B02032303236E38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x7072696E742832303236290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303133E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE7BC96E58699E7A88BE5BA8FEFBC8CE8BE93E587BAE695B4E695B02032303236E38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x7072696E742832303236290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303133E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303133 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E4B8AAE695B4E695B0 USING utf8mb4),CONVERT(0x7072696E74E8BE93E587BAEFBC8CE695B4E695B0 USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E8A18CE695B4E695B02032303236E38082 USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE4B88DE8A681E8BE93E587BAE5B08FE695B0E782B9E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE79BB4E68EA5E68A8AE695B4E695B0E4BAA4E7BB99207072696E742829E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0x323032360A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0x323032360A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-014 输出一段固定对话
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303134 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE6A8A1E68B9FE4B880E6AEB5E59BBAE5AE9AE5AFB9E8AF9DEFBC9AE7ACACE4B880E8A18CE8BE93E587BAE2809CE5B08FE6988EEFBC9AE4BDA0E5A5BDEFBC81E2809DEFBC8CE7ACACE4BA8CE8A18CE8BE93E587BAE2809CE5B08FE7BAA2EFBC9AE4BDA0E5A5BDEFBC81E2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x7072696E742822E5B08FE6988EEFBC9AE4BDA0E5A5BDEFBC8122290A7072696E742822E5B08FE7BAA2EFBC9AE4BDA0E5A5BDEFBC8122290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303134E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE6A8A1E68B9FE4B880E6AEB5E59BBAE5AE9AE5AFB9E8AF9DEFBC9AE7ACACE4B880E8A18CE8BE93E587BAE2809CE5B08FE6988EEFBC9AE4BDA0E5A5BDEFBC81E2809DEFBC8CE7ACACE4BA8CE8A18CE8BE93E587BAE2809CE5B08FE7BAA2EFBC9AE4BDA0E5A5BDEFBC81E2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x7072696E742822E5B08FE6988EEFBC9AE4BDA0E5A5BDEFBC8122290A7072696E742822E5B08FE7BAA2EFBC9AE4BDA0E5A5BDEFBC8122290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303134E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303134 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E6AEB5E59BBAE5AE9AE5AFB9E8AF9D USING utf8mb4),CONVERT(0x7072696E74E8BE93E587BAEFBC8CE5A49AE8A18CE8BE93E587BA USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE68C89E9A1BAE5BA8FE8BE93E587BAE4B8A4E8A18CE5AFB9E8AF9DE38082 USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE5A793E5908DE5908EE79A84E58692E58FB7E4BDBFE794A8E4B8ADE69687E58692E58FB7E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE6AF8FE4B8AAE4BABAE8AFB4E79A84E8AF9DE4BDBFE794A8E4B880E69DA1207072696E742829E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0xE5B08FE6988EEFBC9AE4BDA0E5A5BDEFBC810AE5B08FE7BAA2EFBC9AE4BDA0E5A5BDEFBC810A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0xE5B08FE6988EEFBC9AE4BDA0E5A5BDEFBC810AE5B08FE7BAA2EFBC9AE4BDA0E5A5BDEFBC810A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-015 输出 Hello
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303135 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE7BC96E58699E7A88BE5BA8FEFBC8CE58FAAE8BE93E587BAE88BB1E69687E58D95E8AF8D2048656C6C6FE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x7072696E74282248656C6C6F22290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303135E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE7BC96E58699E7A88BE5BA8FEFBC8CE58FAAE8BE93E587BAE88BB1E69687E58D95E8AF8D2048656C6C6FE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x7072696E74282248656C6C6F22290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303135E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303135 USING utf8mb4),CONVERT(0xE8BE93E587BA2048656C6C6F USING utf8mb4),CONVERT(0x7072696E74E8BE93E587BAEFBC8CE88BB1E69687E5AD97E7ACA6E4B8B2 USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E8A18CEFBC9A48656C6C6F USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0x4820E5BF85E9A1BBE5A4A7E58699EFBC8CE585B6E4BD99E5AD97E6AF8DE5B08FE58699E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE88BB1E69687E58D95E8AF8DE99C80E8A681E694BEE59CA8E5BC95E58FB7E4B8ADE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0x48656C6C6F0A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0x48656C6C6F0A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-016 输出一张固定成绩单
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303136 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E587BAE4B880E5BCA0E4B889E8A18CE68890E7BBA9E58D95EFBC9AE7ACACE4B880E8A18CE2809CE5A793E5908DEFBC9AE5B08FE6988EE2809DEFBC8CE7ACACE4BA8CE8A18CE2809CE5ADA6E7A791EFBC9AE4BFA1E681AFE7A791E68A80E2809DEFBC8CE7ACACE4B889E8A18CE2809CE68890E7BBA9EFBC9A313030E2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x7072696E742822E5A793E5908DEFBC9AE5B08FE6988E22290A7072696E742822E5ADA6E7A791EFBC9AE4BFA1E681AFE7A791E68A8022290A7072696E742822E68890E7BBA9EFBC9A31303022290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303136E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E587BAE4B880E5BCA0E4B889E8A18CE68890E7BBA9E58D95EFBC9AE7ACACE4B880E8A18CE2809CE5A793E5908DEFBC9AE5B08FE6988EE2809DEFBC8CE7ACACE4BA8CE8A18CE2809CE5ADA6E7A791EFBC9AE4BFA1E681AFE7A791E68A80E2809DEFBC8CE7ACACE4B889E8A18CE2809CE68890E7BBA9EFBC9A313030E2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x7072696E742822E5A793E5908DEFBC9AE5B08FE6988E22290A7072696E742822E5ADA6E7A791EFBC9AE4BFA1E681AFE7A791E68A8022290A7072696E742822E68890E7BBA9EFBC9A31303022290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303136E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303136 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B880E5BCA0E59BBAE5AE9AE68890E7BBA9E58D95 USING utf8mb4),CONVERT(0x7072696E74E8BE93E587BAEFBC8CE5A49AE8A18CE8BE93E587BA USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE4B8A5E6A0BCE68C89E9A298E79BAEE8A681E6B182E8BE93E587BAE4B889E8A18CE38082 USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE4BDBFE794A8E4B8ADE69687E58692E58FB7EFBC8CE4B88DE8A681E6B7BBE58AA0E5A49AE4BD99E7A9BAE6A0BCE38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE6AF8FE4B880E9A1B9E4BDBFE794A8E4B880E69DA1207072696E742829E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0xE5A793E5908DEFBC9AE5B08FE6988E0AE5ADA6E7A791EFBC9AE4BFA1E681AFE7A791E68A800AE68890E7BBA9EFBC9A3130300A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0xE5A793E5908DEFBC9AE5B08FE6988E0AE5ADA6E7A791EFBC9AE4BFA1E681AFE7A791E68A800AE68890E7BBA9EFBC9A3130300A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-017 用星号画小三角形
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303137 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE794A8E6989FE58FB7E8BE93E587BAE4B880E4B8AAE4B889E5B182E5B08FE4B889E8A792E5BDA2EFBC8CE4B889E8A18CE58886E588ABE69C892031E3808132E380813320E4B8AAE6989FE58FB7E38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x7072696E7428222A22290A7072696E7428222A2A22290A7072696E7428222A2A2A22290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303137E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE794A8E6989FE58FB7E8BE93E587BAE4B880E4B8AAE4B889E5B182E5B08FE4B889E8A792E5BDA2EFBC8CE4B889E8A18CE58886E588ABE69C892031E3808132E380813320E4B8AAE6989FE58FB7E38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x7072696E7428222A22290A7072696E7428222A2A22290A7072696E7428222A2A2A22290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303137E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303137 USING utf8mb4),CONVERT(0xE794A8E6989FE58FB7E794BBE5B08FE4B889E8A792E5BDA2 USING utf8mb4),CONVERT(0x7072696E74E8BE93E587BAEFBC8CE5AD97E7ACA6E59BBEE5BDA2 USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B889E8A18CE6989FE58FB7E7BB84E68890E79A84E5B08FE4B889E8A792E5BDA2E38082 USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE6AF8FE8A18CE6989FE58FB7E4B98BE997B4E6B2A1E69C89E7A9BAE6A0BCE38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE4BB8EE4B880E9A297E6989FE5BC80E5A78BEFBC8CE6AF8FE8A18CE5A29EE58AA0E4B880E9A297E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0x2A0A2A2A0A2A2A2A0A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0x2A0A2A2A0A2A2A2A0A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-018 制作一张文字小名片
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303138 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE588B6E4BD9CE59BBAE5AE9AE5B08FE5908DE78987EFBC9AE4BE9DE6ACA1E8BE93E587BAE2809CE5A793E5908DEFBC9AE5B08FE7A791E2809DE2809CE78FADE7BAA7EFBC9AE585ADEFBC8831EFBC89E78FADE2809DE2809CE788B1E5A5BDEFBC9AE7BC96E7A88BE2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x7072696E742822E5A793E5908DEFBC9AE5B08FE7A79122290A7072696E742822E78FADE7BAA7EFBC9AE585ADEFBC8831EFBC89E78FAD22290A7072696E742822E788B1E5A5BDEFBC9AE7BC96E7A88B22290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303138E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE588B6E4BD9CE59BBAE5AE9AE5B08FE5908DE78987EFBC9AE4BE9DE6ACA1E8BE93E587BAE2809CE5A793E5908DEFBC9AE5B08FE7A791E2809DE2809CE78FADE7BAA7EFBC9AE585ADEFBC8831EFBC89E78FADE2809DE2809CE788B1E5A5BDEFBC9AE7BC96E7A88BE2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x7072696E742822E5A793E5908DEFBC9AE5B08FE7A79122290A7072696E742822E78FADE7BAA7EFBC9AE585ADEFBC8831EFBC89E78FAD22290A7072696E742822E788B1E5A5BDEFBC9AE7BC96E7A88B22290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303138E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303138 USING utf8mb4),CONVERT(0xE588B6E4BD9CE4B880E5BCA0E69687E5AD97E5B08FE5908DE78987 USING utf8mb4),CONVERT(0x7072696E74E8BE93E587BAEFBC8CE5A49AE8A18CE8BE93E587BA USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE4B8A5E6A0BCE68C89E8A681E6B182E8BE93E587BAE4B889E8A18CE5908DE78987E58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE4BDBFE794A8E4B8ADE69687E6A087E782B9EFBC8CE69687E5AD97E4B88DE883BDE79C81E795A5E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE68A8AE5908DE78987E79A84E6AF8FE4B880E9A1B9E58886E588ABE8BE93E587BAE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0xE5A793E5908DEFBC9AE5B08FE7A7910AE78FADE7BAA7EFBC9AE585ADEFBC8831EFBC89E78FAD0AE788B1E5A5BDEFBC9AE7BC96E7A88B0A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0xE5A793E5908DEFBC9AE5B08FE7A7910AE78FADE7BAA7EFBC9AE585ADEFBC8831EFBC89E78FAD0AE788B1E5A5BDEFBC9AE7BC96E7A88B0A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-019 计算 1+2
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303139 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8AEA1E7AE97203120E58AA0203220E79A84E7BB93E69E9CE5B9B6E8BE93E587BAE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x7072696E742831202B2032290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303139E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8AEA1E7AE97203120E58AA0203220E79A84E7BB93E69E9CE5B9B6E8BE93E587BAE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x7072696E742831202B2032290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303139E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303139 USING utf8mb4),CONVERT(0xE8AEA1E7AE9720312B32 USING utf8mb4),CONVERT(0xE58AA0E6B395EFBC8C7072696E74E8BE93E587BA USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE8AEA1E7AE97E7BB93E69E9C2033E38082 USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE4BDBFE794A820507974686F6E20E79A84E58AA0E58FB7E5AE8CE68890E8AEA1E7AE97E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE58FAFE4BBA5E79BB4E68EA5E59CA8207072696E74282920E4B8ADE58699E7AE97E5BC8FE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0x330A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0x330A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-020 计算 10-3
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303230 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8AEA1E7AE9720313020E5878F203320E79A84E7BB93E69E9CE5B9B6E8BE93E587BAE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x7072696E74283130202D2033290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303230E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8AEA1E7AE9720313020E5878F203320E79A84E7BB93E69E9CE5B9B6E8BE93E587BAE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x7072696E74283130202D2033290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303230E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303230 USING utf8mb4),CONVERT(0xE8AEA1E7AE972031302D33 USING utf8mb4),CONVERT(0xE5878FE6B395EFBC8C7072696E74E8BE93E587BA USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE8AEA1E7AE97E7BB93E69E9C2037E38082 USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE4BDBFE794A820507974686F6E20E79A84E5878FE58FB7E5AE8CE68890E8AEA1E7AE97E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE5878FE6B395E7ACA6E58FB7E698AF202DE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0x370A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0x370A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    SELECT COUNT(*) INTO v_count FROM biz_programming_question_config c INNER JOIN biz_question q ON q.question_id=c.question_id
    WHERE c.external_id IN (CONVERT(0x505956332D303031 USING utf8mb4),CONVERT(0x505956332D303032 USING utf8mb4),CONVERT(0x505956332D303033 USING utf8mb4),CONVERT(0x505956332D303034 USING utf8mb4),CONVERT(0x505956332D303035 USING utf8mb4),CONVERT(0x505956332D303036 USING utf8mb4),CONVERT(0x505956332D303037 USING utf8mb4),CONVERT(0x505956332D303038 USING utf8mb4),CONVERT(0x505956332D303039 USING utf8mb4),CONVERT(0x505956332D303130 USING utf8mb4),CONVERT(0x505956332D303131 USING utf8mb4),CONVERT(0x505956332D303132 USING utf8mb4),CONVERT(0x505956332D303133 USING utf8mb4),CONVERT(0x505956332D303134 USING utf8mb4),CONVERT(0x505956332D303135 USING utf8mb4),CONVERT(0x505956332D303136 USING utf8mb4),CONVERT(0x505956332D303137 USING utf8mb4),CONVERT(0x505956332D303138 USING utf8mb4),CONVERT(0x505956332D303139 USING utf8mb4),CONVERT(0x505956332D303230 USING utf8mb4)) AND c.create_by='python-system-v3' AND q.creator_id=v_creator_id AND CAST(q.create_by AS BINARY)=CAST(v_creator_username AS BINARY) AND q.question_type='practical' AND q.practical_mode='PYTHON' AND q.grade IS NULL AND q.semester IS NULL AND q.lesson_num IS NULL AND q.is_public='Y' AND c.validation_status='VALID' AND c.enabled='1';
    IF v_count<>20 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='本批 PYV3 题目或配置后检数量不一致，事务已回滚'; END IF;
    SELECT COUNT(*) INTO v_count FROM biz_programming_test_case tc INNER JOIN biz_programming_question_config c ON c.question_id=tc.question_id
    WHERE c.external_id IN (CONVERT(0x505956332D303031 USING utf8mb4),CONVERT(0x505956332D303032 USING utf8mb4),CONVERT(0x505956332D303033 USING utf8mb4),CONVERT(0x505956332D303034 USING utf8mb4),CONVERT(0x505956332D303035 USING utf8mb4),CONVERT(0x505956332D303036 USING utf8mb4),CONVERT(0x505956332D303037 USING utf8mb4),CONVERT(0x505956332D303038 USING utf8mb4),CONVERT(0x505956332D303039 USING utf8mb4),CONVERT(0x505956332D303130 USING utf8mb4),CONVERT(0x505956332D303131 USING utf8mb4),CONVERT(0x505956332D303132 USING utf8mb4),CONVERT(0x505956332D303133 USING utf8mb4),CONVERT(0x505956332D303134 USING utf8mb4),CONVERT(0x505956332D303135 USING utf8mb4),CONVERT(0x505956332D303136 USING utf8mb4),CONVERT(0x505956332D303137 USING utf8mb4),CONVERT(0x505956332D303138 USING utf8mb4),CONVERT(0x505956332D303139 USING utf8mb4),CONVERT(0x505956332D303230 USING utf8mb4));
    IF v_count<>40 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='PYV3 测试点后检数量不一致，事务已回滚'; END IF;
    SELECT COUNT(*) INTO v_count FROM (
        SELECT c.question_id FROM biz_programming_question_config c INNER JOIN biz_programming_test_case tc ON tc.question_id=c.question_id
        WHERE c.external_id IN (CONVERT(0x505956332D303031 USING utf8mb4),CONVERT(0x505956332D303032 USING utf8mb4),CONVERT(0x505956332D303033 USING utf8mb4),CONVERT(0x505956332D303034 USING utf8mb4),CONVERT(0x505956332D303035 USING utf8mb4),CONVERT(0x505956332D303036 USING utf8mb4),CONVERT(0x505956332D303037 USING utf8mb4),CONVERT(0x505956332D303038 USING utf8mb4),CONVERT(0x505956332D303039 USING utf8mb4),CONVERT(0x505956332D303130 USING utf8mb4),CONVERT(0x505956332D303131 USING utf8mb4),CONVERT(0x505956332D303132 USING utf8mb4),CONVERT(0x505956332D303133 USING utf8mb4),CONVERT(0x505956332D303134 USING utf8mb4),CONVERT(0x505956332D303135 USING utf8mb4),CONVERT(0x505956332D303136 USING utf8mb4),CONVERT(0x505956332D303137 USING utf8mb4),CONVERT(0x505956332D303138 USING utf8mb4),CONVERT(0x505956332D303139 USING utf8mb4),CONVERT(0x505956332D303230 USING utf8mb4)) GROUP BY c.question_id,c.no_input
        HAVING SUM(tc.is_public='1')>=1 AND SUM(tc.is_public='0')>=1 AND ROUND(SUM(tc.score_weight),6)=100
           AND ((c.no_input='1' AND COUNT(*)=2) OR (c.no_input='0' AND COUNT(*) BETWEEN 3 AND 5))
    ) checked_questions;
    IF v_count<>20 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='本批 PYV3 测试点公开性、数量或权重后检失败，事务已回滚'; END IF;
    COMMIT;
END$$
DELIMITER ;
CALL import_python_zero_basis_v3_batch_01();
DROP PROCEDURE IF EXISTS import_python_zero_basis_v3_batch_01;
SELECT q.difficulty,COUNT(*) question_count FROM biz_programming_question_config c INNER JOIN biz_question q ON q.question_id=c.question_id
WHERE c.external_id IN (CONVERT(0x505956332D303031 USING utf8mb4),CONVERT(0x505956332D303032 USING utf8mb4),CONVERT(0x505956332D303033 USING utf8mb4),CONVERT(0x505956332D303034 USING utf8mb4),CONVERT(0x505956332D303035 USING utf8mb4),CONVERT(0x505956332D303036 USING utf8mb4),CONVERT(0x505956332D303037 USING utf8mb4),CONVERT(0x505956332D303038 USING utf8mb4),CONVERT(0x505956332D303039 USING utf8mb4),CONVERT(0x505956332D303130 USING utf8mb4),CONVERT(0x505956332D303131 USING utf8mb4),CONVERT(0x505956332D303132 USING utf8mb4),CONVERT(0x505956332D303133 USING utf8mb4),CONVERT(0x505956332D303134 USING utf8mb4),CONVERT(0x505956332D303135 USING utf8mb4),CONVERT(0x505956332D303136 USING utf8mb4),CONVERT(0x505956332D303137 USING utf8mb4),CONVERT(0x505956332D303138 USING utf8mb4),CONVERT(0x505956332D303139 USING utf8mb4),CONVERT(0x505956332D303230 USING utf8mb4)) GROUP BY q.difficulty ORDER BY q.difficulty;
SELECT COUNT(*) question_count,SUM(case_count) test_case_count FROM (SELECT c.question_id,COUNT(tc.test_case_id) case_count FROM biz_programming_question_config c INNER JOIN biz_programming_test_case tc ON tc.question_id=c.question_id
WHERE c.external_id IN (CONVERT(0x505956332D303031 USING utf8mb4),CONVERT(0x505956332D303032 USING utf8mb4),CONVERT(0x505956332D303033 USING utf8mb4),CONVERT(0x505956332D303034 USING utf8mb4),CONVERT(0x505956332D303035 USING utf8mb4),CONVERT(0x505956332D303036 USING utf8mb4),CONVERT(0x505956332D303037 USING utf8mb4),CONVERT(0x505956332D303038 USING utf8mb4),CONVERT(0x505956332D303039 USING utf8mb4),CONVERT(0x505956332D303130 USING utf8mb4),CONVERT(0x505956332D303131 USING utf8mb4),CONVERT(0x505956332D303132 USING utf8mb4),CONVERT(0x505956332D303133 USING utf8mb4),CONVERT(0x505956332D303134 USING utf8mb4),CONVERT(0x505956332D303135 USING utf8mb4),CONVERT(0x505956332D303136 USING utf8mb4),CONVERT(0x505956332D303137 USING utf8mb4),CONVERT(0x505956332D303138 USING utf8mb4),CONVERT(0x505956332D303139 USING utf8mb4),CONVERT(0x505956332D303230 USING utf8mb4)) GROUP BY c.question_id) x;
