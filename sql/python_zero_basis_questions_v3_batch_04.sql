-- Python 零基础系统题库 V3 第 4 批幂等导入脚本。
-- 执行前必须备份目标数据库；每次执行会按 PYV3 外部题号更新本题包并重建对应测试点。
-- 全部中文以 UTF-8 十六进制写入，避免 Windows 客户端代码页污染。
-- 题目数：20；测试点数：77。
-- batch-04.json: 0F032414F48ABC7711917C158F9F7D674FE266EBD1443897ED3CFB57F59FDFEB
SET NAMES utf8mb4;
DROP PROCEDURE IF EXISTS import_python_zero_basis_v3_batch_04;
DELIMITER $$
CREATE PROCEDURE import_python_zero_basis_v3_batch_04()
BEGIN
    DECLARE v_count INT DEFAULT 0;
    DECLARE v_creator_id BIGINT DEFAULT NULL;
    DECLARE v_creator_username VARCHAR(64) DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    SELECT COUNT(*) INTO v_count FROM sys_user u WHERE u.nick_name=CONVERT(0xE98391E4B89CE697AD USING utf8mb4) COLLATE utf8mb4_general_ci AND u.status='0' AND u.del_flag='0' AND EXISTS (SELECT 1 FROM sys_user_role ur INNER JOIN sys_role r ON r.role_id=ur.role_id WHERE ur.user_id=u.user_id AND r.role_key='teacher' AND r.status='0');
    IF v_count<>1 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='郑东旭有效教师账号不是唯一一条，已拒绝导入'; END IF;
    SELECT u.user_id,u.user_name INTO v_creator_id,v_creator_username FROM sys_user u WHERE u.nick_name=CONVERT(0xE98391E4B89CE697AD USING utf8mb4) COLLATE utf8mb4_general_ci AND u.status='0' AND u.del_flag='0' AND EXISTS (SELECT 1 FROM sys_user_role ur INNER JOIN sys_role r ON r.role_id=ur.role_id WHERE ur.user_id=u.user_id AND r.role_key='teacher' AND r.status='0') LIMIT 1;
    SELECT COUNT(*) INTO v_count FROM biz_programming_question_config WHERE external_id IN (CONVERT(0x505956332D303631 USING utf8mb4),CONVERT(0x505956332D303632 USING utf8mb4),CONVERT(0x505956332D303633 USING utf8mb4),CONVERT(0x505956332D303634 USING utf8mb4),CONVERT(0x505956332D303635 USING utf8mb4),CONVERT(0x505956332D303636 USING utf8mb4),CONVERT(0x505956332D303637 USING utf8mb4),CONVERT(0x505956332D303638 USING utf8mb4),CONVERT(0x505956332D303639 USING utf8mb4),CONVERT(0x505956332D303730 USING utf8mb4),CONVERT(0x505956332D303731 USING utf8mb4),CONVERT(0x505956332D303732 USING utf8mb4),CONVERT(0x505956332D303733 USING utf8mb4),CONVERT(0x505956332D303734 USING utf8mb4),CONVERT(0x505956332D303735 USING utf8mb4),CONVERT(0x505956332D303736 USING utf8mb4),CONVERT(0x505956332D303737 USING utf8mb4),CONVERT(0x505956332D303738 USING utf8mb4),CONVERT(0x505956332D303739 USING utf8mb4),CONVERT(0x505956332D303830 USING utf8mb4)) AND COALESCE(create_by,'')<>'python-system-v3';
    IF v_count<>0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='PYV3 外部题号已被其他来源占用，已拒绝导入'; END IF;
    SELECT COUNT(*) INTO v_count FROM biz_programming_question_config WHERE title IN (CONVERT(0xE695B0E5AD97E5A4A7E4BA8E20313020E59097 USING utf8mb4),CONVERT(0xE588A4E696ADE698AFE4B88DE698AFE6ADA3E695B0 USING utf8mb4),CONVERT(0xE588A4E696ADE698AFE4B88DE698AFE99BB6 USING utf8mb4),CONVERT(0xE588A4E696ADE698AFE4B88DE698AFE581B6E695B0 USING utf8mb4),CONVERT(0xE588A4E696ADE698AFE4B88DE698AFE5A587E695B0 USING utf8mb4),CONVERT(0xE4B8A4E4B8AAE695B0E8B081E69BB4E5A4A7 USING utf8mb4),CONVERT(0xE4B8A4E4B8AAE695B0E8B081E69BB4E5B08F USING utf8mb4),CONVERT(0xE698AFE4B88DE698AFE6BBA1E58886 USING utf8mb4),CONVERT(0xE88083E8AF95E58F8AE6A0BCE4BA86E59097 USING utf8mb4),CONVERT(0xE4B88AE5ADA6E69C89E6B2A1E69C89E8BF9FE588B0 USING utf8mb4),CONVERT(0xE5B9B4E9BE84E698AFE590A6E7ACA6E59088E6B4BBE58AA8E8A681E6B182 USING utf8mb4),CONVERT(0xE6B8A9E5BAA6E698AFE590A6E8BF87E9AB98 USING utf8mb4),CONVERT(0xE887AAE58AA8E5BC80E781AFE8BF98E698AFE585B3E781AF USING utf8mb4),CONVERT(0xE588A4E696ADE8BE93E585A5E79A84E58FA3E4BBA4 USING utf8mb4),CONVERT(0xE695B0E5AD97E698AFE590A6E4B88DE8B685E8BF8720313030 USING utf8mb4),CONVERT(0xE4B889E4B8AAE695B0E8B081E69C80E5A4A7 USING utf8mb4),CONVERT(0xE4B88BE99BA8E698AFE590A6E5B8A6E4BC9E USING utf8mb4),CONVERT(0xE7AE80E58D95E68890E7BBA9E7AD89E7BAA7E588A4E696AD USING utf8mb4),CONVERT(0xE8BE93E587BA203120E588B02035 USING utf8mb4),CONVERT(0xE8BE93E587BA203120E588B0203130 USING utf8mb4)) AND (external_id IS NULL OR external_id NOT IN (CONVERT(0x505956332D303631 USING utf8mb4),CONVERT(0x505956332D303632 USING utf8mb4),CONVERT(0x505956332D303633 USING utf8mb4),CONVERT(0x505956332D303634 USING utf8mb4),CONVERT(0x505956332D303635 USING utf8mb4),CONVERT(0x505956332D303636 USING utf8mb4),CONVERT(0x505956332D303637 USING utf8mb4),CONVERT(0x505956332D303638 USING utf8mb4),CONVERT(0x505956332D303639 USING utf8mb4),CONVERT(0x505956332D303730 USING utf8mb4),CONVERT(0x505956332D303731 USING utf8mb4),CONVERT(0x505956332D303732 USING utf8mb4),CONVERT(0x505956332D303733 USING utf8mb4),CONVERT(0x505956332D303734 USING utf8mb4),CONVERT(0x505956332D303735 USING utf8mb4),CONVERT(0x505956332D303736 USING utf8mb4),CONVERT(0x505956332D303737 USING utf8mb4),CONVERT(0x505956332D303738 USING utf8mb4),CONVERT(0x505956332D303739 USING utf8mb4),CONVERT(0x505956332D303830 USING utf8mb4)));
    IF v_count<>0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='题库已存在同名非 PYV3 题目，已拒绝导入'; END IF;
    START TRANSACTION;

    -- PYV3-061 数字大于 10 吗
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303631 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4B880E4B8AAE695B4E695B0E38082E5A682E69E9CE5A4A7E4BA8E20313020E8BE93E587BAE2809CE698AFE2809DEFBC8CE590A6E58899E8BE93E587BAE2809CE590A6E2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x6E756D626572203D20696E7428696E7075742829290A6966206E756D626572203E2031303A0A202020207072696E742822E698AF22290A656C73653A0A202020207072696E742822E590A622290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303631E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4B880E4B8AAE695B4E695B0E38082E5A682E69E9CE5A4A7E4BA8E20313020E8BE93E587BAE2809CE698AFE2809DEFBC8CE590A6E58899E8BE93E587BAE2809CE590A6E2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x6E756D626572203D20696E7428696E7075742829290A6966206E756D626572203E2031303A0A202020207072696E742822E698AF22290A656C73653A0A202020207072696E742822E590A622290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303631E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303631 USING utf8mb4),CONVERT(0xE695B0E5AD97E5A4A7E4BA8E20313020E59097 USING utf8mb4),CONVERT(0x6966E588A4E696ADEFBC8CE6AF94E8BE83E8BF90E7AE97 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE695B4E695B0E38082 USING utf8mb4),CONVERT(0xE6A0B9E68DAEE588A4E696ADE8BE93E587BAE2809CE698AFE2809DE68896E2809CE590A6E2809DE38082 USING utf8mb4),CONVERT(0xE8BE93E585A520313520E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA20E698AFE38082 USING utf8mb4),CONVERT(0x2D3130303020E289A420E695B4E695B020E289A42031303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE6AF94E8BE83E8A1A8E8BEBEE5BC8FE698AF206E756D626572203E203130E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x31350A USING utf8mb4),CONVERT(0xE698AF0A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x31300A USING utf8mb4),CONVERT(0xE590A60A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x31310A USING utf8mb4),CONVERT(0xE698AF0A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x2D330A USING utf8mb4),CONVERT(0xE590A60A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-062 判断是不是正数
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303632 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4B880E4B8AAE695B4E695B0EFBC8CE6ADA3E695B0E8BE93E587BAE2809CE6ADA3E695B0E2809DEFBC8CE590A6E58899E8BE93E587BAE2809CE4B88DE698AFE6ADA3E695B0E2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x6E756D626572203D20696E7428696E7075742829290A6966206E756D626572203E20303A0A202020207072696E742822E6ADA3E695B022290A656C73653A0A202020207072696E742822E4B88DE698AFE6ADA3E695B022290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303632E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4B880E4B8AAE695B4E695B0EFBC8CE6ADA3E695B0E8BE93E587BAE2809CE6ADA3E695B0E2809DEFBC8CE590A6E58899E8BE93E587BAE2809CE4B88DE698AFE6ADA3E695B0E2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x6E756D626572203D20696E7428696E7075742829290A6966206E756D626572203E20303A0A202020207072696E742822E6ADA3E695B022290A656C73653A0A202020207072696E742822E4B88DE698AFE6ADA3E695B022290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303632E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303632 USING utf8mb4),CONVERT(0xE588A4E696ADE698AFE4B88DE698AFE6ADA3E695B0 USING utf8mb4),CONVERT(0x6966E588A4E696ADEFBC8CE6ADA3E695B0 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE695B4E695B0E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE588A4E696ADE7BB93E69E9CE38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203820E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA20E6ADA3E695B0E38082 USING utf8mb4),CONVERT(0x2D3130303020E289A420E695B4E695B020E289A42031303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE6ADA3E695B0E5BF85E9A1BBE4B8A5E6A0BCE5A4A7E4BA8E2030E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x380A USING utf8mb4),CONVERT(0xE6ADA3E695B00A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x300A USING utf8mb4),CONVERT(0xE4B88DE698AFE6ADA3E695B00A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x2D310A USING utf8mb4),CONVERT(0xE4B88DE698AFE6ADA3E695B00A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x313030300A USING utf8mb4),CONVERT(0xE6ADA3E695B00A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-063 判断是不是零
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303633 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4B880E4B8AAE695B4E695B0EFBC8CE7AD89E4BA8E203020E8BE93E587BAE2809CE698AFE99BB6E2809DEFBC8CE590A6E58899E8BE93E587BAE2809CE4B88DE698AFE99BB6E2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x6E756D626572203D20696E7428696E7075742829290A6966206E756D626572203D3D20303A0A202020207072696E742822E698AFE99BB622290A656C73653A0A202020207072696E742822E4B88DE698AFE99BB622290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303633E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4B880E4B8AAE695B4E695B0EFBC8CE7AD89E4BA8E203020E8BE93E587BAE2809CE698AFE99BB6E2809DEFBC8CE590A6E58899E8BE93E587BAE2809CE4B88DE698AFE99BB6E2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x6E756D626572203D20696E7428696E7075742829290A6966206E756D626572203D3D20303A0A202020207072696E742822E698AFE99BB622290A656C73653A0A202020207072696E742822E4B88DE698AFE99BB622290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303633E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303633 USING utf8mb4),CONVERT(0xE588A4E696ADE698AFE4B88DE698AFE99BB6 USING utf8mb4),CONVERT(0x6966E588A4E696ADEFBC8CE79BB8E7AD89E588A4E696AD USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE695B4E695B0E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE588A4E696ADE7BB93E69E9CE38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203020E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA20E698AFE99BB6E38082 USING utf8mb4),CONVERT(0x2D3130303020E289A420E695B4E695B020E289A42031303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE588A4E696ADE79BB8E7AD89E8A681E4BDBFE794A8203D3DE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x300A USING utf8mb4),CONVERT(0xE698AFE99BB60A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x310A USING utf8mb4),CONVERT(0xE4B88DE698AFE99BB60A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x2D310A USING utf8mb4),CONVERT(0xE4B88DE698AFE99BB60A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x3939390A USING utf8mb4),CONVERT(0xE4B88DE698AFE99BB60A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-064 判断是不是偶数
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303634 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4B880E4B8AAE695B4E695B0EFBC8CE581B6E695B0E8BE93E587BAE2809CE581B6E695B0E2809DEFBC8CE590A6E58899E8BE93E587BAE2809CE4B88DE698AFE581B6E695B0E2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x6E756D626572203D20696E7428696E7075742829290A6966206E756D62657220252032203D3D20303A0A202020207072696E742822E581B6E695B022290A656C73653A0A202020207072696E742822E4B88DE698AFE581B6E695B022290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303634E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4B880E4B8AAE695B4E695B0EFBC8CE581B6E695B0E8BE93E587BAE2809CE581B6E695B0E2809DEFBC8CE590A6E58899E8BE93E587BAE2809CE4B88DE698AFE581B6E695B0E2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x6E756D626572203D20696E7428696E7075742829290A6966206E756D62657220252032203D3D20303A0A202020207072696E742822E581B6E695B022290A656C73653A0A202020207072696E742822E4B88DE698AFE581B6E695B022290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303634E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303634 USING utf8mb4),CONVERT(0xE588A4E696ADE698AFE4B88DE698AFE581B6E695B0 USING utf8mb4),CONVERT(0x6966E588A4E696ADEFBC8CE58F96E4BD99EFBC8CE581B6E695B0 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE695B4E695B0E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE588A4E696ADE7BB93E69E9CE38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203820E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA20E581B6E695B0E38082 USING utf8mb4),CONVERT(0x2D313030303020E289A420E695B4E695B020E289A4203130303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE883BDE8A2AB203220E695B4E999A4E79A84E695B0E999A4E4BBA5203220E4BD99E695B0E4B8BA2030E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x380A USING utf8mb4),CONVERT(0xE581B6E695B00A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x370A USING utf8mb4),CONVERT(0xE4B88DE698AFE581B6E695B00A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x300A USING utf8mb4),CONVERT(0xE581B6E695B00A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x2D340A USING utf8mb4),CONVERT(0xE581B6E695B00A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-065 判断是不是奇数
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303635 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4B880E4B8AAE695B4E695B0EFBC8CE5A587E695B0E8BE93E587BAE2809CE5A587E695B0E2809DEFBC8CE590A6E58899E8BE93E587BAE2809CE4B88DE698AFE5A587E695B0E2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x6E756D626572203D20696E7428696E7075742829290A6966206E756D6265722025203220213D20303A0A202020207072696E742822E5A587E695B022290A656C73653A0A202020207072696E742822E4B88DE698AFE5A587E695B022290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303635E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4B880E4B8AAE695B4E695B0EFBC8CE5A587E695B0E8BE93E587BAE2809CE5A587E695B0E2809DEFBC8CE590A6E58899E8BE93E587BAE2809CE4B88DE698AFE5A587E695B0E2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x6E756D626572203D20696E7428696E7075742829290A6966206E756D6265722025203220213D20303A0A202020207072696E742822E5A587E695B022290A656C73653A0A202020207072696E742822E4B88DE698AFE5A587E695B022290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303635E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303635 USING utf8mb4),CONVERT(0xE588A4E696ADE698AFE4B88DE698AFE5A587E695B0 USING utf8mb4),CONVERT(0x6966E588A4E696ADEFBC8CE58F96E4BD99EFBC8CE5A587E695B0 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE695B4E695B0E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE588A4E696ADE7BB93E69E9CE38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203720E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA20E5A587E695B0E38082 USING utf8mb4),CONVERT(0x2D313030303020E289A420E695B4E695B020E289A4203130303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE5A587E695B0E999A4E4BBA5203220E79A84E4BD99E695B0E4B88DE698AF2030E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x370A USING utf8mb4),CONVERT(0xE5A587E695B00A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x380A USING utf8mb4),CONVERT(0xE4B88DE698AFE5A587E695B00A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x300A USING utf8mb4),CONVERT(0xE4B88DE698AFE5A587E695B00A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x2D330A USING utf8mb4),CONVERT(0xE5A587E695B00A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-066 两个数谁更大
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303636 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4B8A4E4B8AAE695B4E695B0EFBC8CE8BE93E587BAE8BE83E5A4A7E79A84E695B0EFBC9BE4B8A4E4B8AAE695B0E79BB8E7AD89E697B6E8BE93E587BAE2809CE79BB8E7AD89E2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x612C2062203D206D617028696E742C20696E70757428292E73706C69742829290A69662061203E20623A0A202020207072696E742861290A656C69662062203E20613A0A202020207072696E742862290A656C73653A0A202020207072696E742822E79BB8E7AD8922290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303636E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4B8A4E4B8AAE695B4E695B0EFBC8CE8BE93E587BAE8BE83E5A4A7E79A84E695B0EFBC9BE4B8A4E4B8AAE695B0E79BB8E7AD89E697B6E8BE93E587BAE2809CE79BB8E7AD89E2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x612C2062203D206D617028696E742C20696E70757428292E73706C69742829290A69662061203E20623A0A202020207072696E742861290A656C69662062203E20613A0A202020207072696E742862290A656C73653A0A202020207072696E742822E79BB8E7AD8922290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303636E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303636 USING utf8mb4),CONVERT(0xE4B8A4E4B8AAE695B0E8B081E69BB4E5A4A7 USING utf8mb4),CONVERT(0x6966E588A4E696ADEFBC8CE6AF94E8BE83E8BF90E7AE97 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B8A4E4B8AAE695B4E695B02061E3808162E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE8BE83E5A4A7E695B4E695B0E68896E69687E5AD97E2809CE79BB8E7AD89E2809DE38082 USING utf8mb4),CONVERT(0xE8BE93E585A52038203320E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA2038E38082 USING utf8mb4),CONVERT(0x2D3130303020E289A420612C6220E289A42031303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE99C80E8A681E58886E588ABE5A484E7908620613E62E38081613C6220E5928CE79BB8E7AD89E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x3820330A USING utf8mb4),CONVERT(0x380A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x3320380A USING utf8mb4),CONVERT(0x380A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x3520350A USING utf8mb4),CONVERT(0xE79BB8E7AD890A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x2D32202D370A USING utf8mb4),CONVERT(0x2D320A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-067 两个数谁更小
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303637 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4B8A4E4B8AAE695B4E695B0EFBC8CE8BE93E587BAE8BE83E5B08FE79A84E695B0EFBC9BE4B8A4E4B8AAE695B0E79BB8E7AD89E697B6E8BE93E587BAE2809CE79BB8E7AD89E2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x612C2062203D206D617028696E742C20696E70757428292E73706C69742829290A69662061203C20623A0A202020207072696E742861290A656C69662062203C20613A0A202020207072696E742862290A656C73653A0A202020207072696E742822E79BB8E7AD8922290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303637E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4B8A4E4B8AAE695B4E695B0EFBC8CE8BE93E587BAE8BE83E5B08FE79A84E695B0EFBC9BE4B8A4E4B8AAE695B0E79BB8E7AD89E697B6E8BE93E587BAE2809CE79BB8E7AD89E2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x612C2062203D206D617028696E742C20696E70757428292E73706C69742829290A69662061203C20623A0A202020207072696E742861290A656C69662062203C20613A0A202020207072696E742862290A656C73653A0A202020207072696E742822E79BB8E7AD8922290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303637E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303637 USING utf8mb4),CONVERT(0xE4B8A4E4B8AAE695B0E8B081E69BB4E5B08F USING utf8mb4),CONVERT(0x6966E588A4E696ADEFBC8CE6AF94E8BE83E8BF90E7AE97 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B8A4E4B8AAE695B4E695B02061E3808162E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE8BE83E5B08FE695B4E695B0E68896E69687E5AD97E2809CE79BB8E7AD89E2809DE38082 USING utf8mb4),CONVERT(0xE8BE93E585A52038203320E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA2033E38082 USING utf8mb4),CONVERT(0x2D3130303020E289A420612C6220E289A42031303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE68A8AE5A4A7E4BA8EE58FB7E68DA2E68890E5B08FE4BA8EE58FB7EFBC8CE4BB8DE8A681E5A484E79086E79BB8E7AD89E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x3820330A USING utf8mb4),CONVERT(0x330A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x3320380A USING utf8mb4),CONVERT(0x330A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x3520350A USING utf8mb4),CONVERT(0xE79BB8E7AD890A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x2D32202D370A USING utf8mb4),CONVERT(0x2D370A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-068 是不是满分
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303638 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E88083E8AF95E68890E7BBA9EFBC8CE7AD89E4BA8E2031303020E8BE93E587BAE2809CE6BBA1E58886E2809DEFBC8CE590A6E58899E8BE93E587BAE2809CE7BBA7E7BBADE58AAAE58A9BE2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x73636F7265203D20696E7428696E7075742829290A69662073636F7265203D3D203130303A0A202020207072696E742822E6BBA1E5888622290A656C73653A0A202020207072696E742822E7BBA7E7BBADE58AAAE58A9B22290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303638E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E88083E8AF95E68890E7BBA9EFBC8CE7AD89E4BA8E2031303020E8BE93E587BAE2809CE6BBA1E58886E2809DEFBC8CE590A6E58899E8BE93E587BAE2809CE7BBA7E7BBADE58AAAE58A9BE2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x73636F7265203D20696E7428696E7075742829290A69662073636F7265203D3D203130303A0A202020207072696E742822E6BBA1E5888622290A656C73653A0A202020207072696E742822E7BBA7E7BBADE58AAAE58A9B22290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303638E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303638 USING utf8mb4),CONVERT(0xE698AFE4B88DE698AFE6BBA1E58886 USING utf8mb4),CONVERT(0x6966E588A4E696ADEFBC8CE68890E7BBA9 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE695B4E695B0E68890E7BBA9E38082 USING utf8mb4),CONVERT(0xE6A0B9E68DAEE68890E7BBA9E8BE93E587BAE68C87E5AE9AE69687E5AD97E38082 USING utf8mb4),CONVERT(0xE8BE93E585A52031303020E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA20E6BBA1E58886E38082 USING utf8mb4),CONVERT(0x3020E289A420E68890E7BBA920E289A420313030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE6BBA1E58886E5BF85E9A1BBE6ADA3E5A5BDE7AD89E4BA8E20313030E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x3130300A USING utf8mb4),CONVERT(0xE6BBA1E588860A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x39390A USING utf8mb4),CONVERT(0xE7BBA7E7BBADE58AAAE58A9B0A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x300A USING utf8mb4),CONVERT(0xE7BBA7E7BBADE58AAAE58A9B0A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x36300A USING utf8mb4),CONVERT(0xE7BBA7E7BBADE58AAAE58A9B0A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-069 考试及格了吗
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303639 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E88083E8AF95E68890E7BBA9EFBC8CE8BEBEE588B020363020E58886E8BE93E587BAE2809CE58F8AE6A0BCE2809DEFBC8CE590A6E58899E8BE93E587BAE2809CE4B88DE58F8AE6A0BCE2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x73636F7265203D20696E7428696E7075742829290A69662073636F7265203E3D2036303A0A202020207072696E742822E58F8AE6A0BC22290A656C73653A0A202020207072696E742822E4B88DE58F8AE6A0BC22290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303639E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E88083E8AF95E68890E7BBA9EFBC8CE8BEBEE588B020363020E58886E8BE93E587BAE2809CE58F8AE6A0BCE2809DEFBC8CE590A6E58899E8BE93E587BAE2809CE4B88DE58F8AE6A0BCE2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x73636F7265203D20696E7428696E7075742829290A69662073636F7265203E3D2036303A0A202020207072696E742822E58F8AE6A0BC22290A656C73653A0A202020207072696E742822E4B88DE58F8AE6A0BC22290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303639E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303639 USING utf8mb4),CONVERT(0xE88083E8AF95E58F8AE6A0BCE4BA86E59097 USING utf8mb4),CONVERT(0x6966E588A4E696ADEFBC8CE68890E7BBA9 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE695B4E695B0E68890E7BBA9E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE2809CE58F8AE6A0BCE2809DE68896E2809CE4B88DE58F8AE6A0BCE2809DE38082 USING utf8mb4),CONVERT(0xE8BE93E585A520363020E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA20E58F8AE6A0BCE38082 USING utf8mb4),CONVERT(0x3020E289A420E68890E7BBA920E289A420313030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9A363020E58886E4B99FE7AE97E58F8AE6A0BCEFBC8CE68980E4BBA5E4BDBFE794A8203E3DE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x36300A USING utf8mb4),CONVERT(0xE58F8AE6A0BC0A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x35390A USING utf8mb4),CONVERT(0xE4B88DE58F8AE6A0BC0A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x3130300A USING utf8mb4),CONVERT(0xE58F8AE6A0BC0A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x300A USING utf8mb4),CONVERT(0xE4B88DE58F8AE6A0BC0A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-070 上学有没有迟到
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303730 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E588B0E6A0A1E697B6E997B4E6AF94E8A784E5AE9AE697B6E997B4E6999AE4BA86E5A49AE5B091E58886E9929FE38082E695B0E580BCE4B88DE5A4A7E4BA8E203020E8BE93E587BAE2809CE58786E697B6E2809DEFBC8CE5A4A7E4BA8E203020E8BE93E587BAE2809CE8BF9FE588B0E2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x6D696E75746573203D20696E7428696E7075742829290A6966206D696E75746573203C3D20303A0A202020207072696E742822E58786E697B622290A656C73653A0A202020207072696E742822E8BF9FE588B022290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303730E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E588B0E6A0A1E697B6E997B4E6AF94E8A784E5AE9AE697B6E997B4E6999AE4BA86E5A49AE5B091E58886E9929FE38082E695B0E580BCE4B88DE5A4A7E4BA8E203020E8BE93E587BAE2809CE58786E697B6E2809DEFBC8CE5A4A7E4BA8E203020E8BE93E587BAE2809CE8BF9FE588B0E2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x6D696E75746573203D20696E7428696E7075742829290A6966206D696E75746573203C3D20303A0A202020207072696E742822E58786E697B622290A656C73653A0A202020207072696E742822E8BF9FE588B022290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303730E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303730 USING utf8mb4),CONVERT(0xE4B88AE5ADA6E69C89E6B2A1E69C89E8BF9FE588B0 USING utf8mb4),CONVERT(0x6966E588A4E696ADEFBC8CE697B6E997B4 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE695B4E695B0EFBC8CE8A1A8E7A4BAE6999AE588B0E79A84E58886E9929FE695B0EFBC9BE68F90E5898DE588B0E794A8E8B49FE695B0E8A1A8E7A4BAE38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE2809CE58786E697B6E2809DE68896E2809CE8BF9FE588B0E2809DE38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203020E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA20E58786E697B6E38082 USING utf8mb4),CONVERT(0x2D363020E289A420E58886E9929FE695B020E289A420313230E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9A3020E8A1A8E7A4BAE5889AE5A5BDE68C89E697B6EFBC8CE4B99FE5B19EE4BA8EE58786E697B6E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x300A USING utf8mb4),CONVERT(0xE58786E697B60A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x350A USING utf8mb4),CONVERT(0xE8BF9FE588B00A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x2D31300A USING utf8mb4),CONVERT(0xE58786E697B60A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x3132300A USING utf8mb4),CONVERT(0xE8BF9FE588B00A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-071 年龄是否符合活动要求
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303731 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE6B4BBE58AA8E58581E8AEB8203130EFBD9E313420E5B281E5ADA6E7949FE58F82E58AA0E38082E8BE93E585A5E5B9B4E9BE84EFBC8CE7ACA6E59088E8BE93E587BAE2809CE58FAFE4BBA5E58F82E58AA0E2809DEFBC8CE590A6E58899E8BE93E587BAE2809CE4B88DE883BDE58F82E58AA0E2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x616765203D20696E7428696E7075742829290A6966203130203C3D20616765203C3D2031343A0A202020207072696E742822E58FAFE4BBA5E58F82E58AA022290A656C73653A0A202020207072696E742822E4B88DE883BDE58F82E58AA022290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303731E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE6B4BBE58AA8E58581E8AEB8203130EFBD9E313420E5B281E5ADA6E7949FE58F82E58AA0E38082E8BE93E585A5E5B9B4E9BE84EFBC8CE7ACA6E59088E8BE93E587BAE2809CE58FAFE4BBA5E58F82E58AA0E2809DEFBC8CE590A6E58899E8BE93E587BAE2809CE4B88DE883BDE58F82E58AA0E2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x616765203D20696E7428696E7075742829290A6966203130203C3D20616765203C3D2031343A0A202020207072696E742822E58FAFE4BBA5E58F82E58AA022290A656C73653A0A202020207072696E742822E4B88DE883BDE58F82E58AA022290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303731E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303731 USING utf8mb4),CONVERT(0xE5B9B4E9BE84E698AFE590A6E7ACA6E59088E6B4BBE58AA8E8A681E6B182 USING utf8mb4),CONVERT(0x6966E588A4E696ADEFBC8CE980BBE8BE91E4B88E USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE695B4E695B0E5B9B4E9BE84E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE698AFE590A6E58FAFE4BBA5E58F82E58AA0E38082 USING utf8mb4),CONVERT(0xE8BE93E585A520313220E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA20E58FAFE4BBA5E58F82E58AA0E38082 USING utf8mb4),CONVERT(0x3120E289A420E5B9B4E9BE8420E289A420313030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE5B9B4E9BE84E8A681E5908CE697B6E6BBA1E8B6B3E4B88DE5B08FE4BA8E20313020E5928CE4B88DE5A4A7E4BA8E203134E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x31320A USING utf8mb4),CONVERT(0xE58FAFE4BBA5E58F82E58AA00A USING utf8mb4),1,20,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x31300A USING utf8mb4),CONVERT(0xE58FAFE4BBA5E58F82E58AA00A USING utf8mb4),0,20,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x31340A USING utf8mb4),CONVERT(0xE58FAFE4BBA5E58F82E58AA00A USING utf8mb4),0,20,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x390A USING utf8mb4),CONVERT(0xE4B88DE883BDE58F82E58AA00A USING utf8mb4),0,20,4,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92034 USING utf8mb4),CONVERT(0x31350A USING utf8mb4),CONVERT(0xE4B88DE883BDE58F82E58AA00A USING utf8mb4),0,20,5,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-072 温度是否过高
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303732 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E695B4E695B0E6B8A9E5BAA6EFBC8C333520E69184E6B08FE5BAA6E58F8AE4BBA5E4B88AE8BE93E587BAE2809CE6B8A9E5BAA6E8BF87E9AB98E2809DEFBC8CE590A6E58899E8BE93E587BAE2809CE6B8A9E5BAA6E6ADA3E5B8B8E2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x74656D7065726174757265203D20696E7428696E7075742829290A69662074656D7065726174757265203E3D2033353A0A202020207072696E742822E6B8A9E5BAA6E8BF87E9AB9822290A656C73653A0A202020207072696E742822E6B8A9E5BAA6E6ADA3E5B8B822290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303732E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E695B4E695B0E6B8A9E5BAA6EFBC8C333520E69184E6B08FE5BAA6E58F8AE4BBA5E4B88AE8BE93E587BAE2809CE6B8A9E5BAA6E8BF87E9AB98E2809DEFBC8CE590A6E58899E8BE93E587BAE2809CE6B8A9E5BAA6E6ADA3E5B8B8E2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x74656D7065726174757265203D20696E7428696E7075742829290A69662074656D7065726174757265203E3D2033353A0A202020207072696E742822E6B8A9E5BAA6E8BF87E9AB9822290A656C73653A0A202020207072696E742822E6B8A9E5BAA6E6ADA3E5B8B822290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303732E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303732 USING utf8mb4),CONVERT(0xE6B8A9E5BAA6E698AFE590A6E8BF87E9AB98 USING utf8mb4),CONVERT(0x6966E588A4E696ADEFBC8CE6B8A9E5BAA6 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE695B4E695B0E6B8A9E5BAA6E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE6B8A9E5BAA6E588A4E696ADE7BB93E69E9CE38082 USING utf8mb4),CONVERT(0xE8BE93E585A520333520E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA20E6B8A9E5BAA6E8BF87E9AB98E38082 USING utf8mb4),CONVERT(0x2D353020E289A420E6B8A9E5BAA620E289A4203630E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE4B8B4E7958CE580BC20333520E4B99FE5B19EE4BA8EE8BF87E9AB98E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x33350A USING utf8mb4),CONVERT(0xE6B8A9E5BAA6E8BF87E9AB980A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x33340A USING utf8mb4),CONVERT(0xE6B8A9E5BAA6E6ADA3E5B8B80A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x36300A USING utf8mb4),CONVERT(0xE6B8A9E5BAA6E8BF87E9AB980A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x2D31300A USING utf8mb4),CONVERT(0xE6B8A9E5BAA6E6ADA3E5B8B80A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-073 自动开灯还是关灯
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303733 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E58589E7BABFE4BAAEE5BAA6E580BCE38082E4BAAEE5BAA6E5B08FE4BA8E20353020E8BE93E587BAE2809CE5BC80E781AFE2809DEFBC8CE590A6E58899E8BE93E587BAE2809CE585B3E781AFE2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x6C69676874203D20696E7428696E7075742829290A6966206C69676874203C2035303A0A202020207072696E742822E5BC80E781AF22290A656C73653A0A202020207072696E742822E585B3E781AF22290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303733E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E58589E7BABFE4BAAEE5BAA6E580BCE38082E4BAAEE5BAA6E5B08FE4BA8E20353020E8BE93E587BAE2809CE5BC80E781AFE2809DEFBC8CE590A6E58899E8BE93E587BAE2809CE585B3E781AFE2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x6C69676874203D20696E7428696E7075742829290A6966206C69676874203C2035303A0A202020207072696E742822E5BC80E781AF22290A656C73653A0A202020207072696E742822E585B3E781AF22290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303733E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303733 USING utf8mb4),CONVERT(0xE887AAE58AA8E5BC80E781AFE8BF98E698AFE585B3E781AF USING utf8mb4),CONVERT(0x6966E588A4E696ADEFBC8CE4BCA0E6849FE599A8 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE695B4E695B0E4BAAEE5BAA6E580BCE38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE2809CE5BC80E781AFE2809DE68896E2809CE585B3E781AFE2809DE38082 USING utf8mb4),CONVERT(0xE8BE93E585A520333020E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA20E5BC80E781AFE38082 USING utf8mb4),CONVERT(0x3020E289A420E4BAAEE5BAA6E580BC20E289A420313030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE4BAAEE5BAA6E8B68AE5B08FE8A1A8E7A4BAE78EAFE5A283E8B68AE69A97E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x33300A USING utf8mb4),CONVERT(0xE5BC80E781AF0A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x35300A USING utf8mb4),CONVERT(0xE585B3E781AF0A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x300A USING utf8mb4),CONVERT(0xE5BC80E781AF0A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x3130300A USING utf8mb4),CONVERT(0xE585B3E781AF0A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-074 判断输入的口令
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303734 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4B880E8A18CE58FA3E4BBA4E38082E58FA3E4BBA4E7AD89E4BA8E20707974686F6E20E697B6E8BE93E587BAE2809CE6ADA3E7A1AEE2809DEFBC8CE590A6E58899E8BE93E587BAE2809CE99499E8AFAFE2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x70617373776F7264203D20696E70757428290A69662070617373776F7264203D3D2022707974686F6E223A0A202020207072696E742822E6ADA3E7A1AE22290A656C73653A0A202020207072696E742822E99499E8AFAF22290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303734E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4B880E8A18CE58FA3E4BBA4E38082E58FA3E4BBA4E7AD89E4BA8E20707974686F6E20E697B6E8BE93E587BAE2809CE6ADA3E7A1AEE2809DEFBC8CE590A6E58899E8BE93E587BAE2809CE99499E8AFAFE2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x70617373776F7264203D20696E70757428290A69662070617373776F7264203D3D2022707974686F6E223A0A202020207072696E742822E6ADA3E7A1AE22290A656C73653A0A202020207072696E742822E99499E8AFAF22290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303734E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303734 USING utf8mb4),CONVERT(0xE588A4E696ADE8BE93E585A5E79A84E58FA3E4BBA4 USING utf8mb4),CONVERT(0x6966E588A4E696ADEFBC8CE5AD97E7ACA6E4B8B2E6AF94E8BE83 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE58FAAE590ABE5B08FE58699E88BB1E69687E5AD97E6AF8DE79A84E58FA3E4BBA4E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE2809CE6ADA3E7A1AEE2809DE68896E2809CE99499E8AFAFE2809DE38082 USING utf8mb4),CONVERT(0xE8BE93E585A520707974686F6E20E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA20E6ADA3E7A1AEE38082 USING utf8mb4),CONVERT(0xE58FA3E4BBA4E995BFE5BAA6E4B8BA2031EFBD9E3230E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE5AD97E7ACA6E4B8B2E6AF94E8BE83E4B99FE4BDBFE794A8203D3DEFBC8CE5A4A7E5B08FE58699E8A681E4B880E887B4E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x707974686F6E0A USING utf8mb4),CONVERT(0xE6ADA3E7A1AE0A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x507974686F6E0A USING utf8mb4),CONVERT(0xE99499E8AFAF0A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x68656C6C6F0A USING utf8mb4),CONVERT(0xE99499E8AFAF0A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x707974686F0A USING utf8mb4),CONVERT(0xE99499E8AFAF0A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-075 数字是否不超过 100
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303735 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4B880E4B8AAE695B4E695B0EFBC8CE4B88DE8B685E8BF872031303020E8BE93E587BAE2809CE88C83E59BB4E58685E2809DEFBC8CE590A6E58899E8BE93E587BAE2809CE8B685E587BAE88C83E59BB4E2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x6E756D626572203D20696E7428696E7075742829290A6966206E756D626572203C3D203130303A0A202020207072696E742822E88C83E59BB4E5868522290A656C73653A0A202020207072696E742822E8B685E587BAE88C83E59BB422290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303735E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4B880E4B8AAE695B4E695B0EFBC8CE4B88DE8B685E8BF872031303020E8BE93E587BAE2809CE88C83E59BB4E58685E2809DEFBC8CE590A6E58899E8BE93E587BAE2809CE8B685E587BAE88C83E59BB4E2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x6E756D626572203D20696E7428696E7075742829290A6966206E756D626572203C3D203130303A0A202020207072696E742822E88C83E59BB4E5868522290A656C73653A0A202020207072696E742822E8B685E587BAE88C83E59BB422290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303735E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303735 USING utf8mb4),CONVERT(0xE695B0E5AD97E698AFE590A6E4B88DE8B685E8BF8720313030 USING utf8mb4),CONVERT(0x6966E588A4E696ADEFBC8CE6AF94E8BE83E8BF90E7AE97 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE695B4E695B0E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE88C83E59BB4E588A4E696ADE7BB93E69E9CE38082 USING utf8mb4),CONVERT(0xE8BE93E585A52031303020E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA20E88C83E59BB4E58685E38082 USING utf8mb4),CONVERT(0x2D313030303020E289A420E695B4E695B020E289A4203130303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE4B88DE8B685E8BF87E8A1A8E7A4BAE5B08FE4BA8EE68896E7AD89E4BA8EE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x3130300A USING utf8mb4),CONVERT(0xE88C83E59BB4E586850A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x3130310A USING utf8mb4),CONVERT(0xE8B685E587BAE88C83E59BB40A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x2D350A USING utf8mb4),CONVERT(0xE88C83E59BB4E586850A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x31303030300A USING utf8mb4),CONVERT(0xE8B685E587BAE88C83E59BB40A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-076 三个数谁最大
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303736 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4B889E4B8AAE695B4E695B0EFBC8CE4BDBFE794A8E69DA1E4BBB6E588A4E696ADE689BEE587BAE5B9B6E8BE93E587BAE69C80E5A4A7E580BCE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x612C20622C2063203D206D617028696E742C20696E70757428292E73706C69742829290A6D6178696D756D203D20610A69662062203E206D6178696D756D3A0A202020206D6178696D756D203D20620A69662063203E206D6178696D756D3A0A202020206D6178696D756D203D20630A7072696E74286D6178696D756D290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303736E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4B889E4B8AAE695B4E695B0EFBC8CE4BDBFE794A8E69DA1E4BBB6E588A4E696ADE689BEE587BAE5B9B6E8BE93E587BAE69C80E5A4A7E580BCE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x612C20622C2063203D206D617028696E742C20696E70757428292E73706C69742829290A6D6178696D756D203D20610A69662062203E206D6178696D756D3A0A202020206D6178696D756D203D20620A69662063203E206D6178696D756D3A0A202020206D6178696D756D203D20630A7072696E74286D6178696D756D290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303736E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303736 USING utf8mb4),CONVERT(0xE4B889E4B8AAE695B0E8B081E69C80E5A4A7 USING utf8mb4),CONVERT(0x6966E588A4E696ADEFBC8CE69C80E5A4A7E580BC USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B889E4B8AAE695B4E695B0E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B889E4B8AAE695B0E4B8ADE79A84E69C80E5A4A7E580BCE38082 USING utf8mb4),CONVERT(0xE8BE93E585A520332039203520E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA2039E38082 USING utf8mb4),CONVERT(0x2D3130303020E289A420E6AF8FE4B8AAE695B4E695B020E289A42031303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE58588E68A8AE7ACACE4B880E4B8AAE695B0E5BD93E4BD9CE69C80E5A4A7E580BCEFBC8CE5868DE4BE9DE6ACA1E6AF94E8BE83E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x33203920350A USING utf8mb4),CONVERT(0x390A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x38203220310A USING utf8mb4),CONVERT(0x380A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x34203420340A USING utf8mb4),CONVERT(0x340A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x2D33202D38202D310A USING utf8mb4),CONVERT(0x2D310A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-077 下雨是否带伞
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303737 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E5A4A9E6B094E78AB6E68081E2809CE4B88BE99BA8E2809DE68896E2809CE4B88DE4B88BE99BA8E2809DE38082E4B88BE99BA8E8BE93E587BAE2809CE5B8A6E4BC9EE2809DEFBC8CE590A6E58899E8BE93E587BAE2809CE4B88DE5B8A6E4BC9EE2809DE38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x77656174686572203D20696E70757428290A69662077656174686572203D3D2022E4B88BE99BA8223A0A202020207072696E742822E5B8A6E4BC9E22290A656C73653A0A202020207072696E742822E4B88DE5B8A6E4BC9E22290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303737E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E5A4A9E6B094E78AB6E68081E2809CE4B88BE99BA8E2809DE68896E2809CE4B88DE4B88BE99BA8E2809DE38082E4B88BE99BA8E8BE93E587BAE2809CE5B8A6E4BC9EE2809DEFBC8CE590A6E58899E8BE93E587BAE2809CE4B88DE5B8A6E4BC9EE2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x77656174686572203D20696E70757428290A69662077656174686572203D3D2022E4B88BE99BA8223A0A202020207072696E742822E5B8A6E4BC9E22290A656C73653A0A202020207072696E742822E4B88DE5B8A6E4BC9E22290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303737E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303737 USING utf8mb4),CONVERT(0xE4B88BE99BA8E698AFE590A6E5B8A6E4BC9E USING utf8mb4),CONVERT(0x6966E588A4E696ADEFBC8CE5AD97E7ACA6E4B8B2E6AF94E8BE83EFBC8CE7949FE6B4BBE5BA94E794A8 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE5A4A9E6B094E78AB6E68081E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE698AFE590A6E5B8A6E4BC9EE38082 USING utf8mb4),CONVERT(0xE8BE93E585A520E4B88BE99BA820E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA20E5B8A6E4BC9EE38082 USING utf8mb4),CONVERT(0xE8BE93E585A5E58FAAE4BC9AE698AFE2809CE4B88BE99BA8E2809DE68896E2809CE4B88DE4B88BE99BA8E2809DE38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE79BB4E68EA5E588A4E696ADE8BE93E585A5E698AFE590A6E7AD89E4BA8EE2809CE4B88BE99BA8E2809DE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0xE4B88BE99BA80A USING utf8mb4),CONVERT(0xE5B8A6E4BC9E0A USING utf8mb4),1,34,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0xE4B88DE4B88BE99BA80A USING utf8mb4),CONVERT(0xE4B88DE5B8A6E4BC9E0A USING utf8mb4),0,33,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0xE4B88BE99BA80A USING utf8mb4),CONVERT(0xE5B8A6E4BC9E0A USING utf8mb4),0,33,3,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-078 简单成绩等级判断
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303738 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E68890E7BBA9EFBC9A393020E58886E58F8AE4BBA5E4B88AE4B8BA2041EFBC8C3830EFBD9E383920E4B8BA2042EFBC8C3630EFBD9E373920E4B8BA2043EFBC8CE4BD8EE4BA8E20363020E4B8BA2044E38082 USING utf8mb4),NULL,CONVERT(0x53494D504C45 USING utf8mb4),NULL,NULL,CONVERT(0x73636F7265203D20696E7428696E7075742829290A69662073636F7265203E3D2039303A0A202020207072696E7428224122290A656C69662073636F7265203E3D2038303A0A202020207072696E7428224222290A656C69662073636F7265203E3D2036303A0A202020207072696E7428224322290A656C73653A0A202020207072696E7428224422290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303738E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E68890E7BBA9EFBC9A393020E58886E58F8AE4BBA5E4B88AE4B8BA2041EFBC8C3830EFBD9E383920E4B8BA2042EFBC8C3630EFBD9E373920E4B8BA2043EFBC8CE4BD8EE4BA8E20363020E4B8BA2044E38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x53494D504C45 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x73636F7265203D20696E7428696E7075742829290A69662073636F7265203E3D2039303A0A202020207072696E7428224122290A656C69662073636F7265203E3D2038303A0A202020207072696E7428224222290A656C69662073636F7265203E3D2036303A0A202020207072696E7428224322290A656C73653A0A202020207072696E7428224422290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303738E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303738 USING utf8mb4),CONVERT(0xE7AE80E58D95E68890E7BBA9E7AD89E7BAA7E588A4E696AD USING utf8mb4),CONVERT(0x6966E588A4E696ADEFBC8CE5A49AE58886E694AFEFBC8CE68890E7BBA9 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE695B4E695B0E68890E7BBA9E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE5AFB9E5BA94E79A84E5A4A7E58699E7AD89E7BAA7E5AD97E6AF8DE38082 USING utf8mb4),CONVERT(0xE8BE93E585A520393520E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA2041E38082 USING utf8mb4),CONVERT(0x3020E289A420E68890E7BBA920E289A420313030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E4BA94E38081E585ADE5B9B4E7BAA7E99BB6E59FBAE7A180E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE99D9EE5B8B8E7AE80E58D95E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE69DA1E4BBB6E5BA94E4BB8EE69C80E9AB98E58886E6AEB5E5BC80E5A78BE588A4E696ADE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x39350A USING utf8mb4),CONVERT(0x410A USING utf8mb4),1,20,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x39300A USING utf8mb4),CONVERT(0x410A USING utf8mb4),0,20,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x38390A USING utf8mb4),CONVERT(0x420A USING utf8mb4),0,20,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x36300A USING utf8mb4),CONVERT(0x430A USING utf8mb4),0,20,4,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92034 USING utf8mb4),CONVERT(0x35390A USING utf8mb4),CONVERT(0x440A USING utf8mb4),0,20,5,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-079 输出 1 到 5
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303739 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE4BDBFE794A820666F7220E5BEAAE78EAFE4BE9DE6ACA1E8BE93E587BA2031E3808132E3808133E3808134E3808135EFBC8CE6AF8FE4B8AAE695B0E5AD97E58DA0E4B880E8A18CE38082 USING utf8mb4),NULL,CONVERT(0x4D454449554D USING utf8mb4),NULL,NULL,CONVERT(0x666F72206E756D62657220696E2072616E676528312C2036293A0A202020207072696E74286E756D626572290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303739E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE4BDBFE794A820666F7220E5BEAAE78EAFE4BE9DE6ACA1E8BE93E587BA2031E3808132E3808133E3808134E3808135EFBC8CE6AF8FE4B8AAE695B0E5AD97E58DA0E4B880E8A18CE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x4D454449554D USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x666F72206E756D62657220696E2072616E676528312C2036293A0A202020207072696E74286E756D626572290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303739E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303739 USING utf8mb4),CONVERT(0xE8BE93E587BA203120E588B02035 USING utf8mb4),CONVERT(0x666F72E5BEAAE78EAFEFBC8C72616E6765 USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE4BE9DE6ACA1E8BE93E587BAE4BA94E8A18CE695B0E5AD97203120E588B02035E38082 USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE5BF85E9A1BBE4BDBFE794A8E5BEAAE78EAFE5AE8CE68890E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E585ADE5B9B4E7BAA7E68896E5889DE4B8ADE4B880E5B9B4E7BAA7E585A5E997A8E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE7AE80E58D95E8BF9BE998B6E38082E8A7A3E9A298E68F90E7A4BAEFBC9A72616E676528312C20362920E4BC9AE4BAA7E7949F203120E588B02035E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0x310A320A330A340A350A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0x310A320A330A340A350A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-080 输出 1 到 10
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303830 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE4BDBFE794A820666F7220E5BEAAE78EAFE4BE9DE6ACA1E8BE93E587BA203120E588B0203130EFBC8CE6AF8FE4B8AAE695B0E5AD97E58DA0E4B880E8A18CE38082 USING utf8mb4),NULL,CONVERT(0x4D454449554D USING utf8mb4),NULL,NULL,CONVERT(0x666F72206E756D62657220696E2072616E676528312C203131293A0A202020207072696E74286E756D626572290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303830E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE4BDBFE794A820666F7220E5BEAAE78EAFE4BE9DE6ACA1E8BE93E587BA203120E588B0203130EFBC8CE6AF8FE4B8AAE695B0E5AD97E58DA0E4B880E8A18CE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x4D454449554D USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x666F72206E756D62657220696E2072616E676528312C203131293A0A202020207072696E74286E756D626572290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303830E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303830 USING utf8mb4),CONVERT(0xE8BE93E587BA203120E588B0203130 USING utf8mb4),CONVERT(0x666F72E5BEAAE78EAFEFBC8C72616E6765 USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE4BE9DE6ACA1E8BE93E587BAE58D81E8A18CE695B0E5AD97203120E588B0203130E38082 USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE5BF85E9A1BBE4BDBFE794A8E5BEAAE78EAFE5AE8CE68890E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E585ADE5B9B4E7BAA7E68896E5889DE4B8ADE4B880E5B9B4E7BAA7E585A5E997A8E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE7AE80E58D95E8BF9BE998B6E38082E8A7A3E9A298E68F90E7A4BAEFBC9A72616E676520E79A84E7BB93E69D9FE580BCE4B88DE58C85E590ABE59CA8E7BB93E69E9CE4B8ADE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0x310A320A330A340A350A360A370A380A390A31300A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0x310A320A330A340A350A360A370A380A390A31300A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    SELECT COUNT(*) INTO v_count FROM biz_programming_question_config c INNER JOIN biz_question q ON q.question_id=c.question_id
    WHERE c.external_id IN (CONVERT(0x505956332D303631 USING utf8mb4),CONVERT(0x505956332D303632 USING utf8mb4),CONVERT(0x505956332D303633 USING utf8mb4),CONVERT(0x505956332D303634 USING utf8mb4),CONVERT(0x505956332D303635 USING utf8mb4),CONVERT(0x505956332D303636 USING utf8mb4),CONVERT(0x505956332D303637 USING utf8mb4),CONVERT(0x505956332D303638 USING utf8mb4),CONVERT(0x505956332D303639 USING utf8mb4),CONVERT(0x505956332D303730 USING utf8mb4),CONVERT(0x505956332D303731 USING utf8mb4),CONVERT(0x505956332D303732 USING utf8mb4),CONVERT(0x505956332D303733 USING utf8mb4),CONVERT(0x505956332D303734 USING utf8mb4),CONVERT(0x505956332D303735 USING utf8mb4),CONVERT(0x505956332D303736 USING utf8mb4),CONVERT(0x505956332D303737 USING utf8mb4),CONVERT(0x505956332D303738 USING utf8mb4),CONVERT(0x505956332D303739 USING utf8mb4),CONVERT(0x505956332D303830 USING utf8mb4)) AND c.create_by='python-system-v3' AND q.creator_id=v_creator_id AND CAST(q.create_by AS BINARY)=CAST(v_creator_username AS BINARY) AND q.question_type='practical' AND q.practical_mode='PYTHON' AND q.grade IS NULL AND q.semester IS NULL AND q.lesson_num IS NULL AND q.is_public='Y' AND c.validation_status='VALID' AND c.enabled='1';
    IF v_count<>20 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='本批 PYV3 题目或配置后检数量不一致，事务已回滚'; END IF;
    SELECT COUNT(*) INTO v_count FROM biz_programming_test_case tc INNER JOIN biz_programming_question_config c ON c.question_id=tc.question_id
    WHERE c.external_id IN (CONVERT(0x505956332D303631 USING utf8mb4),CONVERT(0x505956332D303632 USING utf8mb4),CONVERT(0x505956332D303633 USING utf8mb4),CONVERT(0x505956332D303634 USING utf8mb4),CONVERT(0x505956332D303635 USING utf8mb4),CONVERT(0x505956332D303636 USING utf8mb4),CONVERT(0x505956332D303637 USING utf8mb4),CONVERT(0x505956332D303638 USING utf8mb4),CONVERT(0x505956332D303639 USING utf8mb4),CONVERT(0x505956332D303730 USING utf8mb4),CONVERT(0x505956332D303731 USING utf8mb4),CONVERT(0x505956332D303732 USING utf8mb4),CONVERT(0x505956332D303733 USING utf8mb4),CONVERT(0x505956332D303734 USING utf8mb4),CONVERT(0x505956332D303735 USING utf8mb4),CONVERT(0x505956332D303736 USING utf8mb4),CONVERT(0x505956332D303737 USING utf8mb4),CONVERT(0x505956332D303738 USING utf8mb4),CONVERT(0x505956332D303739 USING utf8mb4),CONVERT(0x505956332D303830 USING utf8mb4));
    IF v_count<>77 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='PYV3 测试点后检数量不一致，事务已回滚'; END IF;
    SELECT COUNT(*) INTO v_count FROM (
        SELECT c.question_id FROM biz_programming_question_config c INNER JOIN biz_programming_test_case tc ON tc.question_id=c.question_id
        WHERE c.external_id IN (CONVERT(0x505956332D303631 USING utf8mb4),CONVERT(0x505956332D303632 USING utf8mb4),CONVERT(0x505956332D303633 USING utf8mb4),CONVERT(0x505956332D303634 USING utf8mb4),CONVERT(0x505956332D303635 USING utf8mb4),CONVERT(0x505956332D303636 USING utf8mb4),CONVERT(0x505956332D303637 USING utf8mb4),CONVERT(0x505956332D303638 USING utf8mb4),CONVERT(0x505956332D303639 USING utf8mb4),CONVERT(0x505956332D303730 USING utf8mb4),CONVERT(0x505956332D303731 USING utf8mb4),CONVERT(0x505956332D303732 USING utf8mb4),CONVERT(0x505956332D303733 USING utf8mb4),CONVERT(0x505956332D303734 USING utf8mb4),CONVERT(0x505956332D303735 USING utf8mb4),CONVERT(0x505956332D303736 USING utf8mb4),CONVERT(0x505956332D303737 USING utf8mb4),CONVERT(0x505956332D303738 USING utf8mb4),CONVERT(0x505956332D303739 USING utf8mb4),CONVERT(0x505956332D303830 USING utf8mb4)) GROUP BY c.question_id,c.no_input
        HAVING SUM(tc.is_public='1')>=1 AND SUM(tc.is_public='0')>=1 AND ROUND(SUM(tc.score_weight),6)=100
           AND ((c.no_input='1' AND COUNT(*)=2) OR (c.no_input='0' AND COUNT(*) BETWEEN 3 AND 5))
    ) checked_questions;
    IF v_count<>20 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='本批 PYV3 测试点公开性、数量或权重后检失败，事务已回滚'; END IF;
    COMMIT;
END$$
DELIMITER ;
CALL import_python_zero_basis_v3_batch_04();
DROP PROCEDURE IF EXISTS import_python_zero_basis_v3_batch_04;
SELECT q.difficulty,COUNT(*) question_count FROM biz_programming_question_config c INNER JOIN biz_question q ON q.question_id=c.question_id
WHERE c.external_id IN (CONVERT(0x505956332D303631 USING utf8mb4),CONVERT(0x505956332D303632 USING utf8mb4),CONVERT(0x505956332D303633 USING utf8mb4),CONVERT(0x505956332D303634 USING utf8mb4),CONVERT(0x505956332D303635 USING utf8mb4),CONVERT(0x505956332D303636 USING utf8mb4),CONVERT(0x505956332D303637 USING utf8mb4),CONVERT(0x505956332D303638 USING utf8mb4),CONVERT(0x505956332D303639 USING utf8mb4),CONVERT(0x505956332D303730 USING utf8mb4),CONVERT(0x505956332D303731 USING utf8mb4),CONVERT(0x505956332D303732 USING utf8mb4),CONVERT(0x505956332D303733 USING utf8mb4),CONVERT(0x505956332D303734 USING utf8mb4),CONVERT(0x505956332D303735 USING utf8mb4),CONVERT(0x505956332D303736 USING utf8mb4),CONVERT(0x505956332D303737 USING utf8mb4),CONVERT(0x505956332D303738 USING utf8mb4),CONVERT(0x505956332D303739 USING utf8mb4),CONVERT(0x505956332D303830 USING utf8mb4)) GROUP BY q.difficulty ORDER BY q.difficulty;
SELECT COUNT(*) question_count,SUM(case_count) test_case_count FROM (SELECT c.question_id,COUNT(tc.test_case_id) case_count FROM biz_programming_question_config c INNER JOIN biz_programming_test_case tc ON tc.question_id=c.question_id
WHERE c.external_id IN (CONVERT(0x505956332D303631 USING utf8mb4),CONVERT(0x505956332D303632 USING utf8mb4),CONVERT(0x505956332D303633 USING utf8mb4),CONVERT(0x505956332D303634 USING utf8mb4),CONVERT(0x505956332D303635 USING utf8mb4),CONVERT(0x505956332D303636 USING utf8mb4),CONVERT(0x505956332D303637 USING utf8mb4),CONVERT(0x505956332D303638 USING utf8mb4),CONVERT(0x505956332D303639 USING utf8mb4),CONVERT(0x505956332D303730 USING utf8mb4),CONVERT(0x505956332D303731 USING utf8mb4),CONVERT(0x505956332D303732 USING utf8mb4),CONVERT(0x505956332D303733 USING utf8mb4),CONVERT(0x505956332D303734 USING utf8mb4),CONVERT(0x505956332D303735 USING utf8mb4),CONVERT(0x505956332D303736 USING utf8mb4),CONVERT(0x505956332D303737 USING utf8mb4),CONVERT(0x505956332D303738 USING utf8mb4),CONVERT(0x505956332D303739 USING utf8mb4),CONVERT(0x505956332D303830 USING utf8mb4)) GROUP BY c.question_id) x;
