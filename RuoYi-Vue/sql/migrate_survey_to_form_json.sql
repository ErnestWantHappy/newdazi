-- 数据库迁移脚本：survey_json 重命名为 form_json
-- 执行日期：2026-06-09
-- 说明：VForm3 替换 SurveyJS，字段名同步更新

ALTER TABLE `biz_guide_sheet` CHANGE COLUMN `survey_json` `form_json` longtext COMMENT '表单定义JSON';