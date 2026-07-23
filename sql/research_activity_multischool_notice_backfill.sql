-- 为多校教师补齐仍未开始的活动通知；只插入缺失接收人，不重置已有已读状态。
INSERT INTO biz_research_notice_recipient
  (topic_id,user_id,source_type,source_value,notice_level,read_flag,read_time,notify_time,
   create_by,create_time,update_by,update_time)
SELECT t.topic_id,u.user_id,'S',MIN(user_stage.school_type),'1','N',NULL,t.create_time,
       t.create_by,t.create_time,t.create_by,t.create_time
FROM biz_research_topic t
JOIN (
    SELECT u1.user_id,d1.school_type
    FROM sys_user u1
    JOIN sys_dept d1 ON d1.dept_id=u1.dept_id AND d1.del_flag='0' AND d1.status='0'
    UNION
    SELECT ud.user_id,d2.school_type
    FROM sys_user_dept ud
    JOIN sys_dept d2 ON d2.dept_id=ud.dept_id AND d2.del_flag='0' AND d2.status='0'
) user_stage ON FIND_IN_SET(user_stage.school_type,t.notice_stages) > 0
JOIN sys_user u ON u.user_id=user_stage.user_id AND u.status='0' AND u.del_flag='0'
JOIN sys_user_role ur ON ur.user_id=u.user_id
JOIN sys_role r ON r.role_id=ur.role_id AND r.role_key='teacher' AND r.status='0' AND r.del_flag='0'
WHERE t.del_flag='0'
  AND t.topic_type='NOTICE'
  AND t.notice_scope='1'
  AND t.activity_time > NOW()
GROUP BY t.topic_id,u.user_id,t.create_by,t.create_time
ON DUPLICATE KEY UPDATE recipient_id=recipient_id;

SELECT COUNT(*) AS remaining_missing_future_multischool_notifications
FROM biz_research_topic t
JOIN (
    SELECT u1.user_id,d1.school_type
    FROM sys_user u1
    JOIN sys_dept d1 ON d1.dept_id=u1.dept_id AND d1.del_flag='0' AND d1.status='0'
    UNION
    SELECT ud.user_id,d2.school_type
    FROM sys_user_dept ud
    JOIN sys_dept d2 ON d2.dept_id=ud.dept_id AND d2.del_flag='0' AND d2.status='0'
) user_stage ON FIND_IN_SET(user_stage.school_type,t.notice_stages) > 0
JOIN sys_user u ON u.user_id=user_stage.user_id AND u.status='0' AND u.del_flag='0'
JOIN sys_user_role ur ON ur.user_id=u.user_id
JOIN sys_role r ON r.role_id=ur.role_id AND r.role_key='teacher' AND r.status='0' AND r.del_flag='0'
LEFT JOIN biz_research_notice_recipient n ON n.topic_id=t.topic_id AND n.user_id=u.user_id
WHERE t.del_flag='0' AND t.topic_type='NOTICE' AND t.notice_scope='1'
  AND t.activity_time > NOW() AND n.recipient_id IS NULL;
