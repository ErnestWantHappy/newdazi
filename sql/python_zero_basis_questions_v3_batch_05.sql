-- Python 零基础系统题库 V3 第 5 批幂等导入脚本。
-- 执行前必须备份目标数据库；每次执行会按 PYV3 外部题号更新本题包并重建对应测试点。
-- 全部中文以 UTF-8 十六进制写入，避免 Windows 客户端代码页污染。
-- 题目数：20；测试点数：72。
-- batch-05.json: 62B30E7631D26C64E3D125A9EB51A25A9DEB9329300B3A6B732AF8202442B065
SET NAMES utf8mb4;
DROP PROCEDURE IF EXISTS import_python_zero_basis_v3_batch_05;
DELIMITER $$
CREATE PROCEDURE import_python_zero_basis_v3_batch_05()
BEGIN
    DECLARE v_count INT DEFAULT 0;
    DECLARE v_creator_id BIGINT DEFAULT NULL;
    DECLARE v_creator_username VARCHAR(64) DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    SELECT COUNT(*) INTO v_count FROM sys_user u WHERE u.nick_name=CONVERT(0xE98391E4B89CE697AD USING utf8mb4) COLLATE utf8mb4_general_ci AND u.status='0' AND u.del_flag='0' AND EXISTS (SELECT 1 FROM sys_user_role ur INNER JOIN sys_role r ON r.role_id=ur.role_id WHERE ur.user_id=u.user_id AND r.role_key='teacher' AND r.status='0');
    IF v_count<>1 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='郑东旭有效教师账号不是唯一一条，已拒绝导入'; END IF;
    SELECT u.user_id,u.user_name INTO v_creator_id,v_creator_username FROM sys_user u WHERE u.nick_name=CONVERT(0xE98391E4B89CE697AD USING utf8mb4) COLLATE utf8mb4_general_ci AND u.status='0' AND u.del_flag='0' AND EXISTS (SELECT 1 FROM sys_user_role ur INNER JOIN sys_role r ON r.role_id=ur.role_id WHERE ur.user_id=u.user_id AND r.role_key='teacher' AND r.status='0') LIMIT 1;
    SELECT COUNT(*) INTO v_count FROM biz_programming_question_config WHERE external_id IN (CONVERT(0x505956332D303831 USING utf8mb4),CONVERT(0x505956332D303832 USING utf8mb4),CONVERT(0x505956332D303833 USING utf8mb4),CONVERT(0x505956332D303834 USING utf8mb4),CONVERT(0x505956332D303835 USING utf8mb4),CONVERT(0x505956332D303836 USING utf8mb4),CONVERT(0x505956332D303837 USING utf8mb4),CONVERT(0x505956332D303838 USING utf8mb4),CONVERT(0x505956332D303839 USING utf8mb4),CONVERT(0x505956332D303930 USING utf8mb4),CONVERT(0x505956332D303931 USING utf8mb4),CONVERT(0x505956332D303932 USING utf8mb4),CONVERT(0x505956332D303933 USING utf8mb4),CONVERT(0x505956332D303934 USING utf8mb4),CONVERT(0x505956332D303935 USING utf8mb4),CONVERT(0x505956332D303936 USING utf8mb4),CONVERT(0x505956332D303937 USING utf8mb4),CONVERT(0x505956332D303938 USING utf8mb4),CONVERT(0x505956332D303939 USING utf8mb4),CONVERT(0x505956332D313030 USING utf8mb4)) AND COALESCE(create_by,'')<>'python-system-v3';
    IF v_count<>0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='PYV3 外部题号已被其他来源占用，已拒绝导入'; END IF;
    SELECT COUNT(*) INTO v_count FROM biz_programming_question_config WHERE title IN (CONVERT(0xE8BE93E587BAE4BA94E6ACA12048656C6C6F USING utf8mb4),CONVERT(0xE8BE93E587BA203120E588B0206E USING utf8mb4),CONVERT(0xE8BE93E587BA203220E588B020313020E79A84E581B6E695B0 USING utf8mb4),CONVERT(0xE8BE93E587BA203120E588B0203920E79A84E5A587E695B0 USING utf8mb4),CONVERT(0xE8AEA1E7AE97203120E588B0203520E79A84E5928C USING utf8mb4),CONVERT(0xE8AEA1E7AE97203120E588B0206E20E79A84E5928C USING utf8mb4),CONVERT(0xE8BE93E587BA206E20E6ACA1E6989FE58FB7 USING utf8mb4),CONVERT(0xE5B08FE78CABE58FAB206E20E5A3B0 USING utf8mb4),CONVERT(0xE4BB8E203520E58092E695B0E588B02031 USING utf8mb4),CONVERT(0xE4B99DE4B99DE4B998E6B395E8A1A8E79A84E4B880E8A18C USING utf8mb4),CONVERT(0x7768696C6520E8BE93E587BA203120E588B02035 USING utf8mb4),CONVERT(0x7768696C6520E8BE93E587BA203120E588B0206E USING utf8mb4),CONVERT(0x7768696C6520E8AEA1E7AE97E7B4AFE58AA0E5928C USING utf8mb4),CONVERT(0xE8BE93E585A5203020E7BB93E69D9FE7BB9FE8AEA1 USING utf8mb4),CONVERT(0xE58092E8AEA1E697B6E79BB4E588B0E99BB6 USING utf8mb4),CONVERT(0xE78C9CE695B0E5AD97E7ACACE4B880E585B3EFBC9AE588A4E696ADE698AFE590A6E78C9CE5AFB9 USING utf8mb4),CONVERT(0xE78C9CE695B0E5AD97E7ACACE4BA8CE585B3EFBC9AE78C9CE5A4A7E4BA86E8BF98E698AFE78C9CE5B08FE4BA86 USING utf8mb4),CONVERT(0xE9B8A1E58594E5908CE7ACBCE7ACACE4B880E585B3EFBC9AE8AEA1E7AE97E9B8A1E58594E680BBE885BFE695B0 USING utf8mb4),CONVERT(0xE9B8A1E58594E5908CE7ACBCE7ACACE4BA8CE585B3EFBC9AE69E9AE4B8BEE9B8A1E5928CE58594E79A84E695B0E9878F USING utf8mb4),CONVERT(0xE99FA9E4BFA1E782B9E585B5E585A5E997A8EFBC9AE59CA8E5B08FE88C83E59BB4E58685E5AFBBE689BEE7ACA6E59088E4BD99E695B0E69DA1E4BBB6E79A84E695B0E5AD97 USING utf8mb4)) AND (external_id IS NULL OR external_id NOT IN (CONVERT(0x505956332D303831 USING utf8mb4),CONVERT(0x505956332D303832 USING utf8mb4),CONVERT(0x505956332D303833 USING utf8mb4),CONVERT(0x505956332D303834 USING utf8mb4),CONVERT(0x505956332D303835 USING utf8mb4),CONVERT(0x505956332D303836 USING utf8mb4),CONVERT(0x505956332D303837 USING utf8mb4),CONVERT(0x505956332D303838 USING utf8mb4),CONVERT(0x505956332D303839 USING utf8mb4),CONVERT(0x505956332D303930 USING utf8mb4),CONVERT(0x505956332D303931 USING utf8mb4),CONVERT(0x505956332D303932 USING utf8mb4),CONVERT(0x505956332D303933 USING utf8mb4),CONVERT(0x505956332D303934 USING utf8mb4),CONVERT(0x505956332D303935 USING utf8mb4),CONVERT(0x505956332D303936 USING utf8mb4),CONVERT(0x505956332D303937 USING utf8mb4),CONVERT(0x505956332D303938 USING utf8mb4),CONVERT(0x505956332D303939 USING utf8mb4),CONVERT(0x505956332D313030 USING utf8mb4)));
    IF v_count<>0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='题库已存在同名非 PYV3 题目，已拒绝导入'; END IF;
    START TRANSACTION;

    -- PYV3-081 输出五次 Hello
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303831 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE4BDBFE794A820666F7220E5BEAAE78EAFE8BE93E587BAE4BA94E8A18C2048656C6C6FE38082 USING utf8mb4),NULL,CONVERT(0x4D454449554D USING utf8mb4),NULL,NULL,CONVERT(0x666F72205F20696E2072616E67652835293A0A202020207072696E74282248656C6C6F22290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303831E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE4BDBFE794A820666F7220E5BEAAE78EAFE8BE93E587BAE4BA94E8A18C2048656C6C6FE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x4D454449554D USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x666F72205F20696E2072616E67652835293A0A202020207072696E74282248656C6C6F22290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303831E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303831 USING utf8mb4),CONVERT(0xE8BE93E587BAE4BA94E6ACA12048656C6C6F USING utf8mb4),CONVERT(0x666F72E5BEAAE78EAFEFBC8CE9878DE5A48DE8BE93E587BA USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE8BF9EE7BBADE8BE93E587BAE4BA94E8A18C2048656C6C6FE38082 USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE5BF85E9A1BBE4BDBFE794A8E5BEAAE78EAFEFBC8CE5A4A7E5B08FE58699E8A681E4B880E887B4E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E585ADE5B9B4E7BAA7E68896E5889DE4B8ADE4B880E5B9B4E7BAA7E585A5E997A8E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE7AE80E58D95E8BF9BE998B6E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE5BEAAE78EAFE58F98E9878FE58FAFE4BBA5E69A82E697B6E4B88DE794A8EFBC8CE794A8E4B88BE58892E7BABFE8A1A8E7A4BAE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0x48656C6C6F0A48656C6C6F0A48656C6C6F0A48656C6C6F0A48656C6C6F0A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0x48656C6C6F0A48656C6C6F0A48656C6C6F0A48656C6C6F0A48656C6C6F0A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-082 输出 1 到 n
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303832 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E6ADA3E695B4E695B0206EEFBC8CE4BDBFE794A820666F7220E5BEAAE78EAFE4BB8E203120E8BE93E587BAE588B0206EEFBC8CE6AF8FE4B8AAE695B0E5AD97E58DA0E4B880E8A18CE38082 USING utf8mb4),NULL,CONVERT(0x4D454449554D USING utf8mb4),NULL,NULL,CONVERT(0x6E203D20696E7428696E7075742829290A666F72206E756D62657220696E2072616E676528312C206E202B2031293A0A202020207072696E74286E756D626572290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303832E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E6ADA3E695B4E695B0206EEFBC8CE4BDBFE794A820666F7220E5BEAAE78EAFE4BB8E203120E8BE93E587BAE588B0206EEFBC8CE6AF8FE4B8AAE695B0E5AD97E58DA0E4B880E8A18CE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x4D454449554D USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x6E203D20696E7428696E7075742829290A666F72206E756D62657220696E2072616E676528312C206E202B2031293A0A202020207072696E74286E756D626572290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303832E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303832 USING utf8mb4),CONVERT(0xE8BE93E587BA203120E588B0206E USING utf8mb4),CONVERT(0x666F72E5BEAAE78EAFEFBC8C72616E6765EFBC8CE8BE93E585A5 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE6ADA3E695B4E695B0206EE38082 USING utf8mb4),CONVERT(0xE4BE9DE6ACA1E8BE93E587BA203120E588B0206EE38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203520E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA2031E3808132E3808133E3808134E3808135E38082 USING utf8mb4),CONVERT(0x3120E289A4206E20E289A420313030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E585ADE5B9B4E7BAA7E68896E5889DE4B8ADE4B880E5B9B4E7BAA7E585A5E997A8E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE7AE80E58D95E8BF9BE998B6E38082E8A7A3E9A298E68F90E7A4BAEFBC9A72616E676528312C206E2B312920E58FAFE4BBA5E58C85E590AB206EE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x350A USING utf8mb4),CONVERT(0x310A320A330A340A350A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x310A USING utf8mb4),CONVERT(0x310A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x320A USING utf8mb4),CONVERT(0x310A320A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x31300A USING utf8mb4),CONVERT(0x310A320A330A340A350A360A370A380A390A31300A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-083 输出 2 到 10 的偶数
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303833 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE4BDBFE794A820666F7220E5BEAAE78EAFE8BE93E587BA2032E3808134E3808136E3808138E380813130EFBC8CE6AF8FE4B8AAE695B0E5AD97E58DA0E4B880E8A18CE38082 USING utf8mb4),NULL,CONVERT(0x4D454449554D USING utf8mb4),NULL,NULL,CONVERT(0x666F72206E756D62657220696E2072616E676528322C2031312C2032293A0A202020207072696E74286E756D626572290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303833E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE4BDBFE794A820666F7220E5BEAAE78EAFE8BE93E587BA2032E3808134E3808136E3808138E380813130EFBC8CE6AF8FE4B8AAE695B0E5AD97E58DA0E4B880E8A18CE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x4D454449554D USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x666F72206E756D62657220696E2072616E676528322C2031312C2032293A0A202020207072696E74286E756D626572290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303833E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303833 USING utf8mb4),CONVERT(0xE8BE93E587BA203220E588B020313020E79A84E581B6E695B0 USING utf8mb4),CONVERT(0x666F72E5BEAAE78EAFEFBC8C72616E6765EFBC8CE581B6E695B0 USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE68C89E4BB8EE5B08FE588B0E5A4A7E79A84E9A1BAE5BA8FE8BE93E587BAE4BA94E4B8AAE581B6E695B0E38082 USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE5BF85E9A1BBE4BDBFE794A8E5BEAAE78EAFE5AE8CE68890E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E585ADE5B9B4E7BAA7E68896E5889DE4B8ADE4B880E5B9B4E7BAA7E585A5E997A8E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE7AE80E58D95E8BF9BE998B6E38082E8A7A3E9A298E68F90E7A4BAEFBC9A72616E676520E58FAFE4BBA5E8AEBEE7BDAEE6ADA5E995BFE4B8BA2032E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0x320A340A360A380A31300A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0x320A340A360A380A31300A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-084 输出 1 到 9 的奇数
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303834 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE4BDBFE794A820666F7220E5BEAAE78EAFE8BE93E587BA2031E3808133E3808135E3808137E3808139EFBC8CE6AF8FE4B8AAE695B0E5AD97E58DA0E4B880E8A18CE38082 USING utf8mb4),NULL,CONVERT(0x4D454449554D USING utf8mb4),NULL,NULL,CONVERT(0x666F72206E756D62657220696E2072616E676528312C2031302C2032293A0A202020207072696E74286E756D626572290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303834E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE4BDBFE794A820666F7220E5BEAAE78EAFE8BE93E587BA2031E3808133E3808135E3808137E3808139EFBC8CE6AF8FE4B8AAE695B0E5AD97E58DA0E4B880E8A18CE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x4D454449554D USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x666F72206E756D62657220696E2072616E676528312C2031302C2032293A0A202020207072696E74286E756D626572290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303834E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303834 USING utf8mb4),CONVERT(0xE8BE93E587BA203120E588B0203920E79A84E5A587E695B0 USING utf8mb4),CONVERT(0x666F72E5BEAAE78EAFEFBC8C72616E6765EFBC8CE5A587E695B0 USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE68C89E4BB8EE5B08FE588B0E5A4A7E79A84E9A1BAE5BA8FE8BE93E587BAE4BA94E4B8AAE5A587E695B0E38082 USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE5BF85E9A1BBE4BDBFE794A8E5BEAAE78EAFE5AE8CE68890E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E585ADE5B9B4E7BAA7E68896E5889DE4B8ADE4B880E5B9B4E7BAA7E585A5E997A8E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE7AE80E58D95E8BF9BE998B6E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE4BB8E203120E5BC80E5A78BEFBC8CE6AF8FE6ACA1E5A29EE58AA02032E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0x310A330A350A370A390A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0x310A330A350A370A390A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-085 计算 1 到 5 的和
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303835 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE4BDBFE794A820666F7220E5BEAAE78EAFE8AEA1E7AE9720312B322B332B342B35EFBC8CE5B9B6E8BE93E587BAE7BB93E69E9CE38082 USING utf8mb4),NULL,CONVERT(0x4D454449554D USING utf8mb4),NULL,NULL,CONVERT(0x746F74616C203D20300A666F72206E756D62657220696E2072616E676528312C2036293A0A20202020746F74616C202B3D206E756D6265720A7072696E7428746F74616C290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303835E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE4BDBFE794A820666F7220E5BEAAE78EAFE8AEA1E7AE9720312B322B332B342B35EFBC8CE5B9B6E8BE93E587BAE7BB93E69E9CE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x4D454449554D USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x746F74616C203D20300A666F72206E756D62657220696E2072616E676528312C2036293A0A20202020746F74616C202B3D206E756D6265720A7072696E7428746F74616C290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303835E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303835 USING utf8mb4),CONVERT(0xE8AEA1E7AE97203120E588B0203520E79A84E5928C USING utf8mb4),CONVERT(0x666F72E5BEAAE78EAFEFBC8CE7B4AFE58AA0 USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE695B4E695B0203135E38082 USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE5BF85E9A1BBE4BDBFE794A8E5BEAAE78EAFE5928CE7B4AFE58AA0E58F98E9878FE38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E585ADE5B9B4E7BAA7E68896E5889DE4B8ADE4B880E5B9B4E7BAA7E585A5E997A8E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE7AE80E58D95E8BF9BE998B6E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE58588E68A8AE680BBE5928CE8AEBEE4B8BA2030EFBC8CE6AF8FE6ACA1E68A8AE5BD93E5898DE695B0E5AD97E58AA0E8BF9BE58EBBE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0x31350A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0x31350A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-086 计算 1 到 n 的和
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303836 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E6ADA3E695B4E695B0206EEFBC8CE8AEA1E7AE97203120E588B0206E20E68980E69C89E695B4E695B0E79A84E5928CE38082 USING utf8mb4),NULL,CONVERT(0x4D454449554D USING utf8mb4),NULL,NULL,CONVERT(0x6E203D20696E7428696E7075742829290A746F74616C203D20300A666F72206E756D62657220696E2072616E676528312C206E202B2031293A0A20202020746F74616C202B3D206E756D6265720A7072696E7428746F74616C290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303836E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E6ADA3E695B4E695B0206EEFBC8CE8AEA1E7AE97203120E588B0206E20E68980E69C89E695B4E695B0E79A84E5928CE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x4D454449554D USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x6E203D20696E7428696E7075742829290A746F74616C203D20300A666F72206E756D62657220696E2072616E676528312C206E202B2031293A0A20202020746F74616C202B3D206E756D6265720A7072696E7428746F74616C290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303836E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303836 USING utf8mb4),CONVERT(0xE8AEA1E7AE97203120E588B0206E20E79A84E5928C USING utf8mb4),CONVERT(0x666F72E5BEAAE78EAFEFBC8CE7B4AFE58AA0EFBC8CE8BE93E585A5 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE6ADA3E695B4E695B0206EE38082 USING utf8mb4),CONVERT(0xE8BE93E587BA203120E588B0206E20E79A84E680BBE5928CE38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203520E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA203135E38082 USING utf8mb4),CONVERT(0x3120E289A4206E20E289A42031303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E585ADE5B9B4E7BAA7E68896E5889DE4B8ADE4B880E5B9B4E7BAA7E585A5E997A8E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE7AE80E58D95E8BF9BE998B6E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE4BDBFE794A8E58F98E9878F20746F74616C20E8AEB0E5BD95E4B88DE696ADE5A29EE58AA0E79A84E680BBE5928CE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x350A USING utf8mb4),CONVERT(0x31350A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x310A USING utf8mb4),CONVERT(0x310A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x31300A USING utf8mb4),CONVERT(0x35350A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x313030300A USING utf8mb4),CONVERT(0x3530303530300A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-087 输出 n 次星号
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303837 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E6ADA3E695B4E695B0206EEFBC8CE4BDBFE794A820666F7220E5BEAAE78EAFE8BE93E587BA206E20E8A18CE6989FE58FB7EFBC8CE6AF8FE8A18CE4B880E4B8AAE6989FE58FB7E38082 USING utf8mb4),NULL,CONVERT(0x4D454449554D USING utf8mb4),NULL,NULL,CONVERT(0x6E203D20696E7428696E7075742829290A666F72205F20696E2072616E6765286E293A0A202020207072696E7428222A22290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303837E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E6ADA3E695B4E695B0206EEFBC8CE4BDBFE794A820666F7220E5BEAAE78EAFE8BE93E587BA206E20E8A18CE6989FE58FB7EFBC8CE6AF8FE8A18CE4B880E4B8AAE6989FE58FB7E38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x4D454449554D USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x6E203D20696E7428696E7075742829290A666F72205F20696E2072616E6765286E293A0A202020207072696E7428222A22290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303837E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303837 USING utf8mb4),CONVERT(0xE8BE93E587BA206E20E6ACA1E6989FE58FB7 USING utf8mb4),CONVERT(0x666F72E5BEAAE78EAFEFBC8CE9878DE5A48DE8BE93E587BAEFBC8CE8BE93E585A5 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE6ADA3E695B4E695B0206EE38082 USING utf8mb4),CONVERT(0xE8BE93E587BA206E20E8A18CEFBC8CE6AF8FE8A18CE4B880E4B8AA202AE38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203320E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA202AE380812AE380812AE38082 USING utf8mb4),CONVERT(0x3120E289A4206E20E289A4203230E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E585ADE5B9B4E7BAA7E68896E5889DE4B8ADE4B880E5B9B4E7BAA7E585A5E997A8E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE7AE80E58D95E8BF9BE998B6E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE5BEAAE78EAFE689A7E8A18CE587A0E6ACA1EFBC8C7072696E74282920E5B0B1E4BC9AE8BF90E8A18CE587A0E6ACA1E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x330A USING utf8mb4),CONVERT(0x2A0A2A0A2A0A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x310A USING utf8mb4),CONVERT(0x2A0A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x350A USING utf8mb4),CONVERT(0x2A0A2A0A2A0A2A0A2A0A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x32300A USING utf8mb4),CONVERT(0x2A0A2A0A2A0A2A0A2A0A2A0A2A0A2A0A2A0A2A0A2A0A2A0A2A0A2A0A2A0A2A0A2A0A2A0A2A0A2A0A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-088 小猫叫 n 声
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303838 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E6ADA3E695B4E695B0206EEFBC8CE4BDBFE794A8E5BEAAE78EAFE8BE93E587BA206E20E8A18CE2809CE596B5E2809DE38082 USING utf8mb4),NULL,CONVERT(0x4D454449554D USING utf8mb4),NULL,NULL,CONVERT(0x6E203D20696E7428696E7075742829290A666F72205F20696E2072616E6765286E293A0A202020207072696E742822E596B522290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303838E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E6ADA3E695B4E695B0206EEFBC8CE4BDBFE794A8E5BEAAE78EAFE8BE93E587BA206E20E8A18CE2809CE596B5E2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x4D454449554D USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x6E203D20696E7428696E7075742829290A666F72205F20696E2072616E6765286E293A0A202020207072696E742822E596B522290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303838E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303838 USING utf8mb4),CONVERT(0xE5B08FE78CABE58FAB206E20E5A3B0 USING utf8mb4),CONVERT(0x666F72E5BEAAE78EAFEFBC8CE9878DE5A48DE8BE93E587BAEFBC8CE8BE93E585A5 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE6ADA3E695B4E695B0206EE38082 USING utf8mb4),CONVERT(0xE8BE93E587BA206E20E8A18CEFBC8CE6AF8FE8A18CE4B880E4B8AAE2809CE596B5E2809DE38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203220E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA20E596B5E38081E596B5E38082 USING utf8mb4),CONVERT(0x3120E289A4206E20E289A4203230E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E585ADE5B9B4E7BAA7E68896E5889DE4B8ADE4B880E5B9B4E7BAA7E585A5E997A8E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE7AE80E58D95E8BF9BE998B6E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE6AF8FE6ACA1E5BEAAE78EAFE8BE93E587BAE4B880E6ACA1E2809CE596B5E2809DE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x320A USING utf8mb4),CONVERT(0xE596B50AE596B50A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x310A USING utf8mb4),CONVERT(0xE596B50A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x340A USING utf8mb4),CONVERT(0xE596B50AE596B50AE596B50AE596B50A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x32300A USING utf8mb4),CONVERT(0xE596B50AE596B50AE596B50AE596B50AE596B50AE596B50AE596B50AE596B50AE596B50AE596B50AE596B50AE596B50AE596B50AE596B50AE596B50AE596B50AE596B50AE596B50AE596B50AE596B50A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-089 从 5 倒数到 1
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303839 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE4BDBFE794A820666F7220E5BEAAE78EAFE4BE9DE6ACA1E8BE93E587BA2035E3808134E3808133E3808132E3808131EFBC8CE6AF8FE4B8AAE695B0E5AD97E58DA0E4B880E8A18CE38082 USING utf8mb4),NULL,CONVERT(0x4D454449554D USING utf8mb4),NULL,NULL,CONVERT(0x666F72206E756D62657220696E2072616E676528352C20302C202D31293A0A202020207072696E74286E756D626572290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303839E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE4BDBFE794A820666F7220E5BEAAE78EAFE4BE9DE6ACA1E8BE93E587BA2035E3808134E3808133E3808132E3808131EFBC8CE6AF8FE4B8AAE695B0E5AD97E58DA0E4B880E8A18CE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x4D454449554D USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x666F72206E756D62657220696E2072616E676528352C20302C202D31293A0A202020207072696E74286E756D626572290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303839E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303839 USING utf8mb4),CONVERT(0xE4BB8E203520E58092E695B0E588B02031 USING utf8mb4),CONVERT(0x666F72E5BEAAE78EAFEFBC8CE58092E5BA8F72616E6765 USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE68C89E58092E5BA8FE8BE93E587BAE4BA94E8A18CE695B0E5AD97E38082 USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE5BF85E9A1BBE4BDBFE794A8E5BEAAE78EAFE5AE8CE68890E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E585ADE5B9B4E7BAA7E68896E5889DE4B8ADE4B880E5B9B4E7BAA7E585A5E997A8E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE7AE80E58D95E8BF9BE998B6E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE58092E5BA8F2072616E676520E79A84E6ADA5E995BFE58FAFE4BBA5E58699E68890202D31E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0x350A340A330A320A310A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0x350A340A330A320A310A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-090 九九乘法表的一行
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303930 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A52031EFBD9E3920E79A84E695B4E695B0206EEFBC8CE8BE93E587BAE4B998E6B395E8A1A8E4B8ADE7ACAC206E20E8A18CEFBC8CE4BB8E206E2A3120E588B0206E2A6EEFBC8CE6AF8FE4B8AAE7AE97E5BC8FE58DA0E4B880E8A18CE38082 USING utf8mb4),NULL,CONVERT(0x4D454449554D USING utf8mb4),NULL,NULL,CONVERT(0x6E203D20696E7428696E7075742829290A666F72206920696E2072616E676528312C206E202B2031293A0A202020207072696E742866227B6E7D2A7B697D3D7B6E202A20697D22290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303930E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A52031EFBD9E3920E79A84E695B4E695B0206EEFBC8CE8BE93E587BAE4B998E6B395E8A1A8E4B8ADE7ACAC206E20E8A18CEFBC8CE4BB8E206E2A3120E588B0206E2A6EEFBC8CE6AF8FE4B8AAE7AE97E5BC8FE58DA0E4B880E8A18CE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x4D454449554D USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x6E203D20696E7428696E7075742829290A666F72206920696E2072616E676528312C206E202B2031293A0A202020207072696E742866227B6E7D2A7B697D3D7B6E202A20697D22290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303930E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303930 USING utf8mb4),CONVERT(0xE4B99DE4B99DE4B998E6B395E8A1A8E79A84E4B880E8A18C USING utf8mb4),CONVERT(0x666F72E5BEAAE78EAFEFBC8CE4B998E6B395E8A1A8EFBC8CE8BE93E585A5 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE695B4E695B0206EE38082 USING utf8mb4),CONVERT(0xE4BE9DE6ACA1E8BE93E587BA206E2A3120E588B0206E2A6E20E79A84E7AE97E5BC8FE4B88EE7BB93E69E9CE38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203320E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA20332A313D33E38081332A323D36E38081332A333D39E38082 USING utf8mb4),CONVERT(0x3120E289A4206E20E289A42039E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E585ADE5B9B4E7BAA7E68896E5889DE4B8ADE4B880E5B9B4E7BAA7E585A5E997A8E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE7AE80E58D95E8BF9BE998B6E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE5BEAAE78EAFE58F98E9878FE4BB8E203120E588B0206EEFBC8CE7BB93E69E9CE698AF206E20E4B998E5BEAAE78EAFE58F98E9878FE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x330A USING utf8mb4),CONVERT(0x332A313D330A332A323D360A332A333D390A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x310A USING utf8mb4),CONVERT(0x312A313D310A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x350A USING utf8mb4),CONVERT(0x352A313D350A352A323D31300A352A333D31350A352A343D32300A352A353D32350A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x390A USING utf8mb4),CONVERT(0x392A313D390A392A323D31380A392A333D32370A392A343D33360A392A353D34350A392A363D35340A392A373D36330A392A383D37320A392A393D38310A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-091 while 输出 1 到 5
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303931 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE4BDBFE794A8207768696C6520E5BEAAE78EAFE4BE9DE6ACA1E8BE93E587BA203120E588B02035EFBC8CE6AF8FE4B8AAE695B0E5AD97E58DA0E4B880E8A18CE38082 USING utf8mb4),NULL,CONVERT(0x4D454449554D USING utf8mb4),NULL,NULL,CONVERT(0x6E756D626572203D20310A7768696C65206E756D626572203C3D20353A0A202020207072696E74286E756D626572290A202020206E756D626572202B3D20310A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303931E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE4BDBFE794A8207768696C6520E5BEAAE78EAFE4BE9DE6ACA1E8BE93E587BA203120E588B02035EFBC8CE6AF8FE4B8AAE695B0E5AD97E58DA0E4B880E8A18CE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x4D454449554D USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x6E756D626572203D20310A7768696C65206E756D626572203C3D20353A0A202020207072696E74286E756D626572290A202020206E756D626572202B3D20310A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303931E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303931 USING utf8mb4),CONVERT(0x7768696C6520E8BE93E587BA203120E588B02035 USING utf8mb4),CONVERT(0x7768696C65E5BEAAE78EAFEFBC8CE8AEA1E695B0E58F98E9878F USING utf8mb4),1,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE69CACE9A298E697A0E99C80E8BE93E585A5E38082 USING utf8mb4),CONVERT(0xE4BE9DE6ACA1E8BE93E587BAE4BA94E8A18CE695B0E5AD97203120E588B02035E38082 USING utf8mb4),CONVERT(0xE69CACE9A298E6B2A1E69C89E8BE93E585A5EFBC8CE7A88BE5BA8FE8BF90E8A18CE5908EE79BB4E68EA5E8BE93E587BAE59BBAE5AE9AE58685E5AEB9E38082 USING utf8mb4),CONVERT(0xE5BF85E9A1BBE4BDBFE794A8207768696C6520E5BEAAE78EAFE38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E585ADE5B9B4E7BAA7E68896E5889DE4B8ADE4B880E5B9B4E7BAA7E585A5E997A8E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE7AE80E58D95E8BF9BE998B6E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE6AF8FE8BDAEE5BEAAE78EAFE5908EE983BDE8A681E8AEA9E8AEA1E695B0E58F98E9878FE5A29EE58AA02031E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),'',CONVERT(0x310A320A330A340A350A USING utf8mb4),1,50,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),'',CONVERT(0x310A320A330A340A350A USING utf8mb4),0,50,2,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-092 while 输出 1 到 n
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303932 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E6ADA3E695B4E695B0206EEFBC8CE4BDBFE794A8207768696C6520E5BEAAE78EAFE4BE9DE6ACA1E8BE93E587BA203120E588B0206EE38082 USING utf8mb4),NULL,CONVERT(0x4D454449554D USING utf8mb4),NULL,NULL,CONVERT(0x6E203D20696E7428696E7075742829290A6E756D626572203D20310A7768696C65206E756D626572203C3D206E3A0A202020207072696E74286E756D626572290A202020206E756D626572202B3D20310A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303932E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E6ADA3E695B4E695B0206EEFBC8CE4BDBFE794A8207768696C6520E5BEAAE78EAFE4BE9DE6ACA1E8BE93E587BA203120E588B0206EE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x4D454449554D USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x6E203D20696E7428696E7075742829290A6E756D626572203D20310A7768696C65206E756D626572203C3D206E3A0A202020207072696E74286E756D626572290A202020206E756D626572202B3D20310A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303932E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303932 USING utf8mb4),CONVERT(0x7768696C6520E8BE93E587BA203120E588B0206E USING utf8mb4),CONVERT(0x7768696C65E5BEAAE78EAFEFBC8CE8AEA1E695B0E58F98E9878FEFBC8CE8BE93E585A5 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE6ADA3E695B4E695B0206EE38082 USING utf8mb4),CONVERT(0xE6AF8FE4B8AAE695B0E5AD97E58DA0E4B880E8A18CEFBC8CE4BE9DE6ACA1E8BE93E587BA203120E588B0206EE38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203420E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA2031E3808132E3808133E3808134E38082 USING utf8mb4),CONVERT(0x3120E289A4206E20E289A420313030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E585ADE5B9B4E7BAA7E68896E5889DE4B8ADE4B880E5B9B4E7BAA7E585A5E997A8E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE7AE80E58D95E8BF9BE998B6E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE5BEAAE78EAFE69DA1E4BBB6E58FAFE4BBA5E58699206E756D626572203C3D206EE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x340A USING utf8mb4),CONVERT(0x310A320A330A340A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x310A USING utf8mb4),CONVERT(0x310A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x31300A USING utf8mb4),CONVERT(0x310A320A330A340A350A360A370A380A390A31300A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x3130300A USING utf8mb4),CONVERT(0x310A320A330A340A350A360A370A380A390A31300A31310A31320A31330A31340A31350A31360A31370A31380A31390A32300A32310A32320A32330A32340A32350A32360A32370A32380A32390A33300A33310A33320A33330A33340A33350A33360A33370A33380A33390A34300A34310A34320A34330A34340A34350A34360A34370A34380A34390A35300A35310A35320A35330A35340A35350A35360A35370A35380A35390A36300A36310A36320A36330A36340A36350A36360A36370A36380A36390A37300A37310A37320A37330A37340A37350A37360A37370A37380A37390A38300A38310A38320A38330A38340A38350A38360A38370A38380A38390A39300A39310A39320A39330A39340A39350A39360A39370A39380A39390A3130300A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-093 while 计算累加和
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303933 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E6ADA3E695B4E695B0206EEFBC8CE4BDBFE794A8207768696C6520E5BEAAE78EAFE8AEA1E7AE97203120E588B0206E20E79A84E5928CE38082 USING utf8mb4),NULL,CONVERT(0x4D454449554D USING utf8mb4),NULL,NULL,CONVERT(0x6E203D20696E7428696E7075742829290A6E756D626572203D20310A746F74616C203D20300A7768696C65206E756D626572203C3D206E3A0A20202020746F74616C202B3D206E756D6265720A202020206E756D626572202B3D20310A7072696E7428746F74616C290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303933E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E6ADA3E695B4E695B0206EEFBC8CE4BDBFE794A8207768696C6520E5BEAAE78EAFE8AEA1E7AE97203120E588B0206E20E79A84E5928CE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x4D454449554D USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x6E203D20696E7428696E7075742829290A6E756D626572203D20310A746F74616C203D20300A7768696C65206E756D626572203C3D206E3A0A20202020746F74616C202B3D206E756D6265720A202020206E756D626572202B3D20310A7072696E7428746F74616C290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303933E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303933 USING utf8mb4),CONVERT(0x7768696C6520E8AEA1E7AE97E7B4AFE58AA0E5928C USING utf8mb4),CONVERT(0x7768696C65E5BEAAE78EAFEFBC8CE7B4AFE58AA0EFBC8CE8BE93E585A5 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE6ADA3E695B4E695B0206EE38082 USING utf8mb4),CONVERT(0xE8BE93E587BA203120E588B0206E20E79A84E680BBE5928CE38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203520E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA203135E38082 USING utf8mb4),CONVERT(0x3120E289A4206E20E289A42031303030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E585ADE5B9B4E7BAA7E68896E5889DE4B8ADE4B880E5B9B4E7BAA7E585A5E997A8E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE7AE80E58D95E8BF9BE998B6E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE5908CE697B6E7BBB4E68AA4E8AEA1E695B0E58F98E9878FE5928CE680BBE5928CE58F98E9878FE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x350A USING utf8mb4),CONVERT(0x31350A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x310A USING utf8mb4),CONVERT(0x310A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x31300A USING utf8mb4),CONVERT(0x35350A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x313030300A USING utf8mb4),CONVERT(0x3530303530300A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-094 输入 0 结束统计
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303934 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE6AF8FE8A18CE8BE93E585A5E4B880E4B8AAE695B4E695B0EFBC8CE8BE93E585A5203020E697B6E7BB93E69D9FE38082E7BB9FE8AEA1E5B9B6E8BE93E587BAE59CA8203020E4B98BE5898DE8BE93E585A5E4BA86E587A0E4B8AAE99D9EE99BB6E695B4E695B0E38082 USING utf8mb4),NULL,CONVERT(0x4D454449554D USING utf8mb4),NULL,NULL,CONVERT(0x636F756E74203D20300A6E756D626572203D20696E7428696E7075742829290A7768696C65206E756D62657220213D20303A0A20202020636F756E74202B3D20310A202020206E756D626572203D20696E7428696E7075742829290A7072696E7428636F756E74290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303934E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE6AF8FE8A18CE8BE93E585A5E4B880E4B8AAE695B4E695B0EFBC8CE8BE93E585A5203020E697B6E7BB93E69D9FE38082E7BB9FE8AEA1E5B9B6E8BE93E587BAE59CA8203020E4B98BE5898DE8BE93E585A5E4BA86E587A0E4B8AAE99D9EE99BB6E695B4E695B0E38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x4D454449554D USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x636F756E74203D20300A6E756D626572203D20696E7428696E7075742829290A7768696C65206E756D62657220213D20303A0A20202020636F756E74202B3D20310A202020206E756D626572203D20696E7428696E7075742829290A7072696E7428636F756E74290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303934E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303934 USING utf8mb4),CONVERT(0xE8BE93E585A5203020E7BB93E69D9FE7BB9FE8AEA1 USING utf8mb4),CONVERT(0x7768696C65E5BEAAE78EAFEFBC8CE593A8E585B5E580BCEFBC8CE8AEA1E695B0 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE88BA5E5B9B2E8A18CE695B4E695B0EFBC8CE69C80E5908EE4B880E8A18CE4B880E5AE9AE698AF2030E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE99D9EE99BB6E695B4E695B0E79A84E4B8AAE695B0E38082 USING utf8mb4),CONVERT(0xE8BE93E585A52033E3808135E380813020E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA2032E38082 USING utf8mb4),CONVERT(0x3020E4B98BE5898DE69C80E5A49AE69C892031303020E4B8AAE695B4E695B0E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E585ADE5B9B4E7BAA7E68896E5889DE4B8ADE4B880E5B9B4E7BAA7E585A5E997A8E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE7AE80E58D95E8BF9BE998B6E38082E8A7A3E9A298E68F90E7A4BAEFBC9A3020E698AFE7BB93E69D9FE6A087E8AEB0EFBC8CE4B88DE8AEA1E585A5E695B0E9878FE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x330A350A300A USING utf8mb4),CONVERT(0x320A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x300A USING utf8mb4),CONVERT(0x300A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x370A300A USING utf8mb4),CONVERT(0x310A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x310A320A330A340A300A USING utf8mb4),CONVERT(0x340A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-095 倒计时直到零
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303935 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E99D9EE8B49FE695B4E695B0206EEFBC8CE4BDBFE794A8207768696C6520E5BEAAE78EAFE4BB8E206E20E58092E695B0E588B02030EFBC8CE6AF8FE4B8AAE695B0E5AD97E58DA0E4B880E8A18CE38082 USING utf8mb4),NULL,CONVERT(0x4D454449554D USING utf8mb4),NULL,NULL,CONVERT(0x6E756D626572203D20696E7428696E7075742829290A7768696C65206E756D626572203E3D20303A0A202020207072696E74286E756D626572290A202020206E756D626572202D3D20310A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303935E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E99D9EE8B49FE695B4E695B0206EEFBC8CE4BDBFE794A8207768696C6520E5BEAAE78EAFE4BB8E206E20E58092E695B0E588B02030EFBC8CE6AF8FE4B8AAE695B0E5AD97E58DA0E4B880E8A18CE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x4D454449554D USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x6E756D626572203D20696E7428696E7075742829290A7768696C65206E756D626572203E3D20303A0A202020207072696E74286E756D626572290A202020206E756D626572202D3D20310A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303935E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303935 USING utf8mb4),CONVERT(0xE58092E8AEA1E697B6E79BB4E588B0E99BB6 USING utf8mb4),CONVERT(0x7768696C65E5BEAAE78EAFEFBC8CE58092E8AEA1E697B6EFBC8CE8BE93E585A5 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE99D9EE8B49FE695B4E695B0206EE38082 USING utf8mb4),CONVERT(0xE4BE9DE6ACA1E8BE93E587BA206EE380816E2D31EFBC8CE79BB4E588B02030E38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203320E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA2033E3808132E3808131E3808130E38082 USING utf8mb4),CONVERT(0x3020E289A4206E20E289A420313030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E585ADE5B9B4E7BAA7E68896E5889DE4B8ADE4B880E5B9B4E7BAA7E585A5E997A8E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE7AE80E58D95E8BF9BE998B6E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE6AF8FE8BDAEE8BE93E587BAE5908EE8AEA9206E20E5878FE5B0912031EFBC8CE5BEAAE78EAFE69DA1E4BBB6E8A681E58581E8AEB8E8BE93E587BA2030E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x330A USING utf8mb4),CONVERT(0x330A320A310A300A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x300A USING utf8mb4),CONVERT(0x300A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x310A USING utf8mb4),CONVERT(0x310A300A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x31300A USING utf8mb4),CONVERT(0x31300A390A380A370A360A350A340A330A320A310A300A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-096 猜数字第一关：判断是否猜对
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303936 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE7ACACE4B880E8A18CE8BE93E585A5E7A798E5AF86E695B0E5AD97EFBC8CE7ACACE4BA8CE8A18CE8BE93E585A5E78C9CE6B58BE695B0E5AD97E38082E5A682E69E9CE79BB8E5908CE8BE93E587BAE2809CE78C9CE5AFB9E4BA86E2809DEFBC8CE590A6E58899E8BE93E587BAE2809CE6B2A1E78C9CE5AFB9E2809DE38082 USING utf8mb4),NULL,CONVERT(0x48415244 USING utf8mb4),NULL,NULL,CONVERT(0x736563726574203D20696E7428696E7075742829290A6775657373203D20696E7428696E7075742829290A6966206775657373203D3D207365637265743A0A202020207072696E742822E78C9CE5AFB9E4BA8622290A656C73653A0A202020207072696E742822E6B2A1E78C9CE5AFB922290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303936E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE7ACACE4B880E8A18CE8BE93E585A5E7A798E5AF86E695B0E5AD97EFBC8CE7ACACE4BA8CE8A18CE8BE93E585A5E78C9CE6B58BE695B0E5AD97E38082E5A682E69E9CE79BB8E5908CE8BE93E587BAE2809CE78C9CE5AFB9E4BA86E2809DEFBC8CE590A6E58899E8BE93E587BAE2809CE6B2A1E78C9CE5AFB9E2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x48415244 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x736563726574203D20696E7428696E7075742829290A6775657373203D20696E7428696E7075742829290A6966206775657373203D3D207365637265743A0A202020207072696E742822E78C9CE5AFB9E4BA8622290A656C73653A0A202020207072696E742822E6B2A1E78C9CE5AFB922290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303936E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303936 USING utf8mb4),CONVERT(0xE78C9CE695B0E5AD97E7ACACE4B880E585B3EFBC9AE588A4E696ADE698AFE590A6E78C9CE5AFB9 USING utf8mb4),CONVERT(0x6966E588A4E696ADEFBC8CE78C9CE695B0E5AD97 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B8A4E8A18CE695B4E695B0EFBC8CE7ACACE4B880E8A18CE698AFE7A798E5AF86E695B0E5AD97EFBC8CE7ACACE4BA8CE8A18CE698AFE78C9CE6B58BE695B0E5AD97E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE588A4E696ADE7BB93E69E9CE38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203336E38081333620E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA20E78C9CE5AFB9E4BA86E38082 USING utf8mb4),CONVERT(0x3120E289A420E4B8A4E4B8AAE695B0E5AD9720E289A420313030EFBC8CE4B88DE4BDBFE794A8E99A8FE69CBAE695B0E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E9AB98E5B9B4E7BAA7E68B93E5B195E68896E5889DE4B8ADE585A5E997A8E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE8B6A3E591B3E68C91E68898E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE79BB4E68EA5E6AF94E8BE83E7A798E5AF86E695B0E5AD97E5928CE78C9CE6B58BE695B0E5AD97E698AFE590A6E79BB8E7AD89E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x33360A33360A USING utf8mb4),CONVERT(0xE78C9CE5AFB9E4BA860A USING utf8mb4),1,25,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x33360A32300A USING utf8mb4),CONVERT(0xE6B2A1E78C9CE5AFB90A USING utf8mb4),0,25,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x310A3130300A USING utf8mb4),CONVERT(0xE6B2A1E78C9CE5AFB90A USING utf8mb4),0,25,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x37350A37350A USING utf8mb4),CONVERT(0xE78C9CE5AFB9E4BA860A USING utf8mb4),0,25,4,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-097 猜数字第二关：猜大了还是猜小了
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303937 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE7ACACE4B880E8A18CE8BE93E585A5E7A798E5AF86E695B0E5AD97EFBC8CE7ACACE4BA8CE8A18CE8BE93E585A5E78C9CE6B58BE695B0E5AD97E38082E78C9CE5AFB9E8BE93E587BAE2809CE78C9CE5AFB9E4BA86E2809DEFBC8CE78C9CE5A4A7E8BE93E587BAE2809CE78C9CE5A4A7E4BA86E2809DEFBC8CE78C9CE5B08FE8BE93E587BAE2809CE78C9CE5B08FE4BA86E2809DE38082 USING utf8mb4),NULL,CONVERT(0x48415244 USING utf8mb4),NULL,NULL,CONVERT(0x736563726574203D20696E7428696E7075742829290A6775657373203D20696E7428696E7075742829290A6966206775657373203D3D207365637265743A0A202020207072696E742822E78C9CE5AFB9E4BA8622290A656C6966206775657373203E207365637265743A0A202020207072696E742822E78C9CE5A4A7E4BA8622290A656C73653A0A202020207072696E742822E78C9CE5B08FE4BA8622290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303937E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE7ACACE4B880E8A18CE8BE93E585A5E7A798E5AF86E695B0E5AD97EFBC8CE7ACACE4BA8CE8A18CE8BE93E585A5E78C9CE6B58BE695B0E5AD97E38082E78C9CE5AFB9E8BE93E587BAE2809CE78C9CE5AFB9E4BA86E2809DEFBC8CE78C9CE5A4A7E8BE93E587BAE2809CE78C9CE5A4A7E4BA86E2809DEFBC8CE78C9CE5B08FE8BE93E587BAE2809CE78C9CE5B08FE4BA86E2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x48415244 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x736563726574203D20696E7428696E7075742829290A6775657373203D20696E7428696E7075742829290A6966206775657373203D3D207365637265743A0A202020207072696E742822E78C9CE5AFB9E4BA8622290A656C6966206775657373203E207365637265743A0A202020207072696E742822E78C9CE5A4A7E4BA8622290A656C73653A0A202020207072696E742822E78C9CE5B08FE4BA8622290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303937E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303937 USING utf8mb4),CONVERT(0xE78C9CE695B0E5AD97E7ACACE4BA8CE585B3EFBC9AE78C9CE5A4A7E4BA86E8BF98E698AFE78C9CE5B08FE4BA86 USING utf8mb4),CONVERT(0xE5A49AE58886E694AFEFBC8CE78C9CE695B0E5AD97 USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B8A4E8A18CE695B4E695B0EFBC8CE7ACACE4B880E8A18CE698AFE7A798E5AF86E695B0E5AD97EFBC8CE7ACACE4BA8CE8A18CE698AFE78C9CE6B58BE695B0E5AD97E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE4B889E7A78DE588A4E696ADE7BB93E69E9CE4B98BE4B880E38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203336E38081333620E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA20E78C9CE5AFB9E4BA86E38082 USING utf8mb4),CONVERT(0x3120E289A420E4B8A4E4B8AAE695B0E5AD9720E289A420313030EFBC8CE4B88DE4BDBFE794A8E99A8FE69CBAE695B0E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E9AB98E5B9B4E7BAA7E68B93E5B195E68896E5889DE4B8ADE585A5E997A8E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE8B6A3E591B3E68C91E68898E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE58588E588A4E696ADE79BB8E7AD89EFBC8CE5868DE6AF94E8BE83E78C9CE6B58BE695B0E5AD97E79A84E5A4A7E5B08FE38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x33360A33360A USING utf8mb4),CONVERT(0xE78C9CE5AFB9E4BA860A USING utf8mb4),1,20,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x33360A35300A USING utf8mb4),CONVERT(0xE78C9CE5A4A7E4BA860A USING utf8mb4),0,20,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x33360A32300A USING utf8mb4),CONVERT(0xE78C9CE5B08FE4BA860A USING utf8mb4),0,20,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x310A3130300A USING utf8mb4),CONVERT(0xE78C9CE5A4A7E4BA860A USING utf8mb4),0,20,4,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92034 USING utf8mb4),CONVERT(0x3130300A310A USING utf8mb4),CONVERT(0xE78C9CE5B08FE4BA860A USING utf8mb4),0,20,5,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-098 鸡兔同笼第一关：计算鸡兔总腿数
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303938 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E9B8A1E79A84E58FAAE695B0E5928CE58594E79A84E58FAAE695B0EFBC8CE8AEA1E7AE97E5AE83E4BBACE4B880E585B1E69C89E5A49AE5B091E69DA1E885BFE38082E9B8A1E69C89203220E69DA1E885BFEFBC8CE58594E69C89203420E69DA1E885BFE38082 USING utf8mb4),NULL,CONVERT(0x48415244 USING utf8mb4),NULL,NULL,CONVERT(0x636869636B656E732C2072616262697473203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E7428636869636B656E73202A2032202B2072616262697473202A2034290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303938E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E9B8A1E79A84E58FAAE695B0E5928CE58594E79A84E58FAAE695B0EFBC8CE8AEA1E7AE97E5AE83E4BBACE4B880E585B1E69C89E5A49AE5B091E69DA1E885BFE38082E9B8A1E69C89203220E69DA1E885BFEFBC8CE58594E69C89203420E69DA1E885BFE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x48415244 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x636869636B656E732C2072616262697473203D206D617028696E742C20696E70757428292E73706C69742829290A7072696E7428636869636B656E73202A2032202B2072616262697473202A2034290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303938E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303938 USING utf8mb4),CONVERT(0xE9B8A1E58594E5908CE7ACBCE7ACACE4B880E585B3EFBC9AE8AEA1E7AE97E9B8A1E58594E680BBE885BFE695B0 USING utf8mb4),CONVERT(0xE58F98E9878FEFBC8CE4B998E6B395EFBC8CE9B8A1E58594E5908CE7ACBC USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B8A4E4B8AAE99D9EE8B49FE695B4E695B0EFBC8CE4BE9DE6ACA1E698AFE9B8A1E5928CE58594E79A84E58FAAE695B0E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE885BFE79A84E680BBE695B0E38082 USING utf8mb4),CONVERT(0xE8BE93E585A52033203220E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA203134E38082 USING utf8mb4),CONVERT(0x3020E289A420E9B8A1E38081E58594E58FAAE695B020E289A420313030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E9AB98E5B9B4E7BAA7E68B93E5B195E68896E5889DE4B8ADE585A5E997A8E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE8B6A3E591B3E68C91E68898E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE680BBE885BFE695B03DE9B8A1E695B0C397322BE58594E695B0C39734E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x3320320A USING utf8mb4),CONVERT(0x31340A USING utf8mb4),1,20,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x3020350A USING utf8mb4),CONVERT(0x32300A USING utf8mb4),0,20,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x3820300A USING utf8mb4),CONVERT(0x31360A USING utf8mb4),0,20,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x3020300A USING utf8mb4),CONVERT(0x300A USING utf8mb4),0,20,4,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92034 USING utf8mb4),CONVERT(0x313030203130300A USING utf8mb4),CONVERT(0x3630300A USING utf8mb4),0,20,5,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-099 鸡兔同笼第二关：枚举鸡和兔的数量
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D303939 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E58AA8E789A9E680BBE58FAAE695B0E5928CE885BFE79A84E680BBE695B0EFBC8CE69E9AE4B8BEE5B9B6E8BE93E587BAE9B8A1E58594E695B0E9878FEFBC8CE6A0BCE5BC8FE4B8BAE2809CE9B8A17820E5859479E2809DEFBC9BE6B2A1E69C89E7AD94E6A188E697B6E8BE93E587BAE2809CE697A0E8A7A3E2809DE38082 USING utf8mb4),NULL,CONVERT(0x48415244 USING utf8mb4),NULL,NULL,CONVERT(0x68656164732C206C656773203D206D617028696E742C20696E70757428292E73706C69742829290A666F756E64203D2046616C73650A666F72207261626269747320696E2072616E6765286865616473202B2031293A0A20202020636869636B656E73203D206865616473202D20726162626974730A20202020696620636869636B656E73202A2032202B2072616262697473202A2034203D3D206C6567733A0A20202020202020207072696E74286622E9B8A17B636869636B656E737D20E585947B726162626974737D22290A2020202020202020666F756E64203D20547275650A2020202020202020627265616B0A6966206E6F7420666F756E643A0A202020207072696E742822E697A0E8A7A322290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303939E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E58AA8E789A9E680BBE58FAAE695B0E5928CE885BFE79A84E680BBE695B0EFBC8CE69E9AE4B8BEE5B9B6E8BE93E587BAE9B8A1E58594E695B0E9878FEFBC8CE6A0BCE5BC8FE4B8BAE2809CE9B8A17820E5859479E2809DEFBC9BE6B2A1E69C89E7AD94E6A188E697B6E8BE93E587BAE2809CE697A0E8A7A3E2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x48415244 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x68656164732C206C656773203D206D617028696E742C20696E70757428292E73706C69742829290A666F756E64203D2046616C73650A666F72207261626269747320696E2072616E6765286865616473202B2031293A0A20202020636869636B656E73203D206865616473202D20726162626974730A20202020696620636869636B656E73202A2032202B2072616262697473202A2034203D3D206C6567733A0A20202020202020207072696E74286622E9B8A17B636869636B656E737D20E585947B726162626974737D22290A2020202020202020666F756E64203D20547275650A2020202020202020627265616B0A6966206E6F7420666F756E643A0A202020207072696E742822E697A0E8A7A322290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D303939E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D303939 USING utf8mb4),CONVERT(0xE9B8A1E58594E5908CE7ACBCE7ACACE4BA8CE585B3EFBC9AE69E9AE4B8BEE9B8A1E5928CE58594E79A84E695B0E9878F USING utf8mb4),CONVERT(0x666F72E5BEAAE78EAFEFBC8CE69E9AE4B8BEEFBC8CE9B8A1E58594E5908CE7ACBC USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B8A4E4B8AAE99D9EE8B49FE695B4E695B0EFBC8CE4BE9DE6ACA1E698AFE680BBE58FAAE695B0E5928CE680BBE885BFE695B0E38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE594AFE4B880E79A84E9B8A1E58594E695B0E9878FE7BB84E59088EFBC8CE68896E8BE93E587BAE2809CE697A0E8A7A3E2809DE38082 USING utf8mb4),CONVERT(0xE8BE93E585A5203520313420E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA20E9B8A13320E5859432E38082 USING utf8mb4),CONVERT(0x3020E289A420E680BBE58FAAE695B020E289A420313030EFBC8C3020E289A420E680BBE885BFE695B020E289A420343030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E9AB98E5B9B4E7BAA7E68B93E5B195E68896E5889DE4B8ADE585A5E997A8E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE8B6A3E591B3E68C91E68898E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE69E9AE4B8BEE58594E79A84E58FAAE695B0EFBC8CE9B8A1E79A84E58FAAE695B0E7AD89E4BA8EE680BBE58FAAE695B0E5878FE58594E695B0E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x352031340A USING utf8mb4),CONVERT(0xE9B8A13320E58594320A USING utf8mb4),1,20,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x31302032300A USING utf8mb4),CONVERT(0xE9B8A1313020E58594300A USING utf8mb4),0,20,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x31302034300A USING utf8mb4),CONVERT(0xE9B8A13020E5859431300A USING utf8mb4),0,20,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x3320370A USING utf8mb4),CONVERT(0xE697A0E8A7A30A USING utf8mb4),0,20,4,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92034 USING utf8mb4),CONVERT(0x3020300A USING utf8mb4),CONVERT(0xE9B8A13020E58594300A USING utf8mb4),0,20,5,'python-system-v3',NOW(),'python-system-v3',NOW());

    -- PYV3-100 韩信点兵入门：在小范围内寻找符合余数条件的数字
    SET @python_v3_question_id = (SELECT question_id FROM biz_programming_question_config WHERE external_id=CONVERT(0x505956332D313030 USING utf8mb4) LIMIT 1);
    INSERT INTO biz_question
        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
    SELECT
        'practical',CONVERT(0xE8BE93E585A5E4B88AE99990206EEFBC8CE59CA82031EFBD9E6E20E4B8ADE5AFBBE689BEE69C80E5B08FE79A84E695B4E695B02078EFBC8CE4BDBF207820E999A4E4BBA5203320E4BD992032E38081E999A4E4BBA5203520E4BD992033E38081E999A4E4BBA5203720E4BD992032E38082E689BEE588B0E5B0B1E8BE93E587BA2078EFBC8CE590A6E58899E8BE93E587BAE2809CE6B2A1E69C89E689BEE588B0E2809DE38082 USING utf8mb4),NULL,CONVERT(0x48415244 USING utf8mb4),NULL,NULL,CONVERT(0x6E203D20696E7428696E7075742829290A666F756E64203D2046616C73650A666F72206E756D62657220696E2072616E676528312C206E202B2031293A0A202020206966206E756D62657220252033203D3D203220616E64206E756D62657220252035203D3D203320616E64206E756D62657220252037203D3D20323A0A20202020202020207072696E74286E756D626572290A2020202020202020666F756E64203D20547275650A2020202020202020627265616B0A6966206E6F7420666F756E643A0A202020207072696E742822E6B2A1E69C89E689BEE588B022290A USING utf8mb4),CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D313030E38082 USING utf8mb4),'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()
    WHERE @python_v3_question_id IS NULL;
    SET @python_v3_question_id=COALESCE(@python_v3_question_id,LAST_INSERT_ID());
    UPDATE biz_question SET
        question_type='practical',question_content=CONVERT(0xE8BE93E585A5E4B88AE99990206EEFBC8CE59CA82031EFBD9E6E20E4B8ADE5AFBBE689BEE69C80E5B08FE79A84E695B4E695B02078EFBC8CE4BDBF207820E999A4E4BBA5203320E4BD992032E38081E999A4E4BBA5203520E4BD992033E38081E999A4E4BBA5203720E4BD992032E38082E689BEE588B0E5B0B1E8BE93E587BA2078EFBC8CE590A6E58899E8BE93E587BAE2809CE6B2A1E69C89E689BEE588B0E2809DE38082 USING utf8mb4),grade=NULL,difficulty=CONVERT(0x48415244 USING utf8mb4),semester=NULL,lesson_num=NULL,
        answer=CONVERT(0x6E203D20696E7428696E7075742829290A666F756E64203D2046616C73650A666F72206E756D62657220696E2072616E676528312C206E202B2031293A0A202020206966206E756D62657220252033203D3D203220616E64206E756D62657220252035203D3D203320616E64206E756D62657220252037203D3D20323A0A20202020202020207072696E74286E756D626572290A2020202020202020666F756E64203D20547275650A2020202020202020627265616B0A6966206E6F7420666F756E643A0A202020207072696E742822E6B2A1E69C89E689BEE588B022290A USING utf8mb4),analysis=CONVERT(0x507974686F6E20E99BB6E59FBAE7A180E7B3BBE7BB9FE9A298205633EFBC9BE7A8B3E5AE9AE9A298E58FB7EFBC9A505956332D313030E38082 USING utf8mb4),practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()
    WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_question_config
        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
    VALUES
        (@python_v3_question_id,'python',CONVERT(0x505956332D313030 USING utf8mb4),CONVERT(0xE99FA9E4BFA1E782B9E585B5E585A5E997A8EFBC9AE59CA8E5B08FE88C83E59BB4E58685E5AFBBE689BEE7ACA6E59088E4BD99E695B0E69DA1E4BBB6E79A84E695B0E5AD97 USING utf8mb4),CONVERT(0x666F72E5BEAAE78EAFEFBC8CE58F96E4BD99EFBC8CE69E9AE4B8BE USING utf8mb4),0,'VALID',NOW(),'python-system-v3',3,CONVERT(0x2320E8AFB7E59CA8E8BF99E9878CE7BC96E58699E7A88BE5BA8F0A USING utf8mb4),CONVERT(0xE4B880E8A18CE4B880E4B8AAE6ADA3E695B4E695B0206EE38082 USING utf8mb4),CONVERT(0xE8BE93E587BAE69C80E5B08FE79A84E7ACA6E59088E69DA1E4BBB6E79A84E695B4E695B0EFBC8CE68896E2809CE6B2A1E69C89E689BEE588B0E2809DE38082 USING utf8mb4),CONVERT(0xE8BE93E585A520333020E697B6EFBC8CE68C89E9A298E6848FE8AEA1E7AE97E5908EE8BE93E587BA203233E38082 USING utf8mb4),CONVERT(0x3120E289A4206E20E289A420353030E38082 USING utf8mb4),CONVERT(0xE98082E794A8E5ADA6E6AEB5EFBC9AE5B08FE5ADA6E9AB98E5B9B4E7BAA7E68B93E5B195E68896E5889DE4B8ADE585A5E997A8E38082E99ABEE5BAA6E5B182E7BAA7EFBC9AE8B6A3E591B3E68C91E68898E38082E8A7A3E9A298E68F90E7A4BAEFBC9AE4BB8E203120E5BC80E5A78BE98090E4B8AAE5B09DE8AF95EFBC8CE5908CE697B6E6A380E69FA5E4B889E4B8AAE4BD99E695B0E69DA1E4BBB6E38082 USING utf8mb4),2,131072,8,1024,64,'1','python-system-v3',NOW(),'python-system-v3',NOW())
    ON DUPLICATE KEY UPDATE
        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();
    DELETE FROM biz_programming_test_case WHERE question_id=@python_v3_question_id;
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE585ACE5BC80E6A0B7E4BE8B USING utf8mb4),CONVERT(0x33300A USING utf8mb4),CONVERT(0x32330A USING utf8mb4),1,20,1,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92031 USING utf8mb4),CONVERT(0x32320A USING utf8mb4),CONVERT(0xE6B2A1E69C89E689BEE588B00A USING utf8mb4),0,20,2,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92032 USING utf8mb4),CONVERT(0x32330A USING utf8mb4),CONVERT(0x32330A USING utf8mb4),0,20,3,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92033 USING utf8mb4),CONVERT(0x3130300A USING utf8mb4),CONVERT(0x32330A USING utf8mb4),0,20,4,'python-system-v3',NOW(),'python-system-v3',NOW());
    INSERT INTO biz_programming_test_case
        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
    VALUES (@python_v3_question_id,CONVERT(0xE99A90E8978FE6B58BE8AF95E782B92034 USING utf8mb4),CONVERT(0x3530300A USING utf8mb4),CONVERT(0x32330A USING utf8mb4),0,20,5,'python-system-v3',NOW(),'python-system-v3',NOW());

    SELECT COUNT(*) INTO v_count FROM biz_programming_question_config c INNER JOIN biz_question q ON q.question_id=c.question_id
    WHERE c.external_id IN (CONVERT(0x505956332D303831 USING utf8mb4),CONVERT(0x505956332D303832 USING utf8mb4),CONVERT(0x505956332D303833 USING utf8mb4),CONVERT(0x505956332D303834 USING utf8mb4),CONVERT(0x505956332D303835 USING utf8mb4),CONVERT(0x505956332D303836 USING utf8mb4),CONVERT(0x505956332D303837 USING utf8mb4),CONVERT(0x505956332D303838 USING utf8mb4),CONVERT(0x505956332D303839 USING utf8mb4),CONVERT(0x505956332D303930 USING utf8mb4),CONVERT(0x505956332D303931 USING utf8mb4),CONVERT(0x505956332D303932 USING utf8mb4),CONVERT(0x505956332D303933 USING utf8mb4),CONVERT(0x505956332D303934 USING utf8mb4),CONVERT(0x505956332D303935 USING utf8mb4),CONVERT(0x505956332D303936 USING utf8mb4),CONVERT(0x505956332D303937 USING utf8mb4),CONVERT(0x505956332D303938 USING utf8mb4),CONVERT(0x505956332D303939 USING utf8mb4),CONVERT(0x505956332D313030 USING utf8mb4)) AND c.create_by='python-system-v3' AND q.creator_id=v_creator_id AND CAST(q.create_by AS BINARY)=CAST(v_creator_username AS BINARY) AND q.question_type='practical' AND q.practical_mode='PYTHON' AND q.grade IS NULL AND q.semester IS NULL AND q.lesson_num IS NULL AND q.is_public='Y' AND c.validation_status='VALID' AND c.enabled='1';
    IF v_count<>20 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='本批 PYV3 题目或配置后检数量不一致，事务已回滚'; END IF;
    SELECT COUNT(*) INTO v_count FROM biz_programming_test_case tc INNER JOIN biz_programming_question_config c ON c.question_id=tc.question_id
    WHERE c.external_id IN (CONVERT(0x505956332D303831 USING utf8mb4),CONVERT(0x505956332D303832 USING utf8mb4),CONVERT(0x505956332D303833 USING utf8mb4),CONVERT(0x505956332D303834 USING utf8mb4),CONVERT(0x505956332D303835 USING utf8mb4),CONVERT(0x505956332D303836 USING utf8mb4),CONVERT(0x505956332D303837 USING utf8mb4),CONVERT(0x505956332D303838 USING utf8mb4),CONVERT(0x505956332D303839 USING utf8mb4),CONVERT(0x505956332D303930 USING utf8mb4),CONVERT(0x505956332D303931 USING utf8mb4),CONVERT(0x505956332D303932 USING utf8mb4),CONVERT(0x505956332D303933 USING utf8mb4),CONVERT(0x505956332D303934 USING utf8mb4),CONVERT(0x505956332D303935 USING utf8mb4),CONVERT(0x505956332D303936 USING utf8mb4),CONVERT(0x505956332D303937 USING utf8mb4),CONVERT(0x505956332D303938 USING utf8mb4),CONVERT(0x505956332D303939 USING utf8mb4),CONVERT(0x505956332D313030 USING utf8mb4));
    IF v_count<>72 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='PYV3 测试点后检数量不一致，事务已回滚'; END IF;
    SELECT COUNT(*) INTO v_count FROM (
        SELECT c.question_id FROM biz_programming_question_config c INNER JOIN biz_programming_test_case tc ON tc.question_id=c.question_id
        WHERE c.external_id IN (CONVERT(0x505956332D303831 USING utf8mb4),CONVERT(0x505956332D303832 USING utf8mb4),CONVERT(0x505956332D303833 USING utf8mb4),CONVERT(0x505956332D303834 USING utf8mb4),CONVERT(0x505956332D303835 USING utf8mb4),CONVERT(0x505956332D303836 USING utf8mb4),CONVERT(0x505956332D303837 USING utf8mb4),CONVERT(0x505956332D303838 USING utf8mb4),CONVERT(0x505956332D303839 USING utf8mb4),CONVERT(0x505956332D303930 USING utf8mb4),CONVERT(0x505956332D303931 USING utf8mb4),CONVERT(0x505956332D303932 USING utf8mb4),CONVERT(0x505956332D303933 USING utf8mb4),CONVERT(0x505956332D303934 USING utf8mb4),CONVERT(0x505956332D303935 USING utf8mb4),CONVERT(0x505956332D303936 USING utf8mb4),CONVERT(0x505956332D303937 USING utf8mb4),CONVERT(0x505956332D303938 USING utf8mb4),CONVERT(0x505956332D303939 USING utf8mb4),CONVERT(0x505956332D313030 USING utf8mb4)) GROUP BY c.question_id,c.no_input
        HAVING SUM(tc.is_public='1')>=1 AND SUM(tc.is_public='0')>=1 AND ROUND(SUM(tc.score_weight),6)=100
           AND ((c.no_input='1' AND COUNT(*)=2) OR (c.no_input='0' AND COUNT(*) BETWEEN 3 AND 5))
    ) checked_questions;
    IF v_count<>20 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='本批 PYV3 测试点公开性、数量或权重后检失败，事务已回滚'; END IF;
    COMMIT;
END$$
DELIMITER ;
CALL import_python_zero_basis_v3_batch_05();
DROP PROCEDURE IF EXISTS import_python_zero_basis_v3_batch_05;
SELECT q.difficulty,COUNT(*) question_count FROM biz_programming_question_config c INNER JOIN biz_question q ON q.question_id=c.question_id
WHERE c.external_id IN (CONVERT(0x505956332D303831 USING utf8mb4),CONVERT(0x505956332D303832 USING utf8mb4),CONVERT(0x505956332D303833 USING utf8mb4),CONVERT(0x505956332D303834 USING utf8mb4),CONVERT(0x505956332D303835 USING utf8mb4),CONVERT(0x505956332D303836 USING utf8mb4),CONVERT(0x505956332D303837 USING utf8mb4),CONVERT(0x505956332D303838 USING utf8mb4),CONVERT(0x505956332D303839 USING utf8mb4),CONVERT(0x505956332D303930 USING utf8mb4),CONVERT(0x505956332D303931 USING utf8mb4),CONVERT(0x505956332D303932 USING utf8mb4),CONVERT(0x505956332D303933 USING utf8mb4),CONVERT(0x505956332D303934 USING utf8mb4),CONVERT(0x505956332D303935 USING utf8mb4),CONVERT(0x505956332D303936 USING utf8mb4),CONVERT(0x505956332D303937 USING utf8mb4),CONVERT(0x505956332D303938 USING utf8mb4),CONVERT(0x505956332D303939 USING utf8mb4),CONVERT(0x505956332D313030 USING utf8mb4)) GROUP BY q.difficulty ORDER BY q.difficulty;
SELECT COUNT(*) question_count,SUM(case_count) test_case_count FROM (SELECT c.question_id,COUNT(tc.test_case_id) case_count FROM biz_programming_question_config c INNER JOIN biz_programming_test_case tc ON tc.question_id=c.question_id
WHERE c.external_id IN (CONVERT(0x505956332D303831 USING utf8mb4),CONVERT(0x505956332D303832 USING utf8mb4),CONVERT(0x505956332D303833 USING utf8mb4),CONVERT(0x505956332D303834 USING utf8mb4),CONVERT(0x505956332D303835 USING utf8mb4),CONVERT(0x505956332D303836 USING utf8mb4),CONVERT(0x505956332D303837 USING utf8mb4),CONVERT(0x505956332D303838 USING utf8mb4),CONVERT(0x505956332D303839 USING utf8mb4),CONVERT(0x505956332D303930 USING utf8mb4),CONVERT(0x505956332D303931 USING utf8mb4),CONVERT(0x505956332D303932 USING utf8mb4),CONVERT(0x505956332D303933 USING utf8mb4),CONVERT(0x505956332D303934 USING utf8mb4),CONVERT(0x505956332D303935 USING utf8mb4),CONVERT(0x505956332D303936 USING utf8mb4),CONVERT(0x505956332D303937 USING utf8mb4),CONVERT(0x505956332D303938 USING utf8mb4),CONVERT(0x505956332D303939 USING utf8mb4),CONVERT(0x505956332D313030 USING utf8mb4)) GROUP BY c.question_id) x;
