/*
 Navicat Premium Dump SQL

 Source Server         : ssm
 Source Server Type    : MySQL
 Source Server Version : 80039 (8.0.39)
 Source Host           : localhost:3306
 Source Schema         : ry-vue

 Target Server Type    : MySQL
 Target Server Version : 80039 (8.0.39)
 File Encoding         : 65001

 Date: 09/01/2026 08:38:43
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for biz_lesson
-- ----------------------------
DROP TABLE IF EXISTS `biz_lesson`;
CREATE TABLE `biz_lesson`  (
  `lesson_id` bigint NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  `lesson_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '课程/作业标题',
  `grade` int NULL DEFAULT NULL COMMENT '年级 (例如: 1, 2, 3, 4, 5, 6)',
  `semester` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '学期 (0上册, 1下册)',
  `lesson_num` int NULL DEFAULT NULL COMMENT '第几课 (例如: 1, 2, 3)',
  `creator_id` bigint NULL DEFAULT NULL COMMENT '创建教师ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`lesson_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课程/作业信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of biz_lesson
-- ----------------------------
INSERT INTO `biz_lesson` VALUES (1, '55', 1, '1', 5, NULL, '', '2025-08-14 09:43:54', '', NULL);
INSERT INTO `biz_lesson` VALUES (2, '11', 7, '0', 1, 1, 'admin', '2025-08-18 21:35:48', 'admin', '2025-08-18 21:36:42');
INSERT INTO `biz_lesson` VALUES (3, '222', 7, '1', 2, 1, 'admin', '2025-08-19 07:29:05', '', NULL);
INSERT INTO `biz_lesson` VALUES (4, '44', 7, '1', 4, 1, 'admin', '2025-08-19 07:30:43', 'admin', '2025-08-20 09:59:53');
INSERT INTO `biz_lesson` VALUES (16, '1010', 7, '0', 1, NULL, '19157727791', NULL, '', NULL);
INSERT INTO `biz_lesson` VALUES (17, '2020', 7, '0', 1, NULL, '19157727791', NULL, '', NULL);
INSERT INTO `biz_lesson` VALUES (18, '始业教育', 7, '0', 3, NULL, '19157727791', NULL, '19157727791', NULL);
INSERT INTO `biz_lesson` VALUES (19, '第二课', 7, '0', 1, NULL, '19157727791', NULL, '', NULL);
INSERT INTO `biz_lesson` VALUES (20, '第三课', 7, '0', 1, NULL, '19157727791', NULL, '', NULL);

-- ----------------------------
-- Table structure for biz_lesson_assignment
-- ----------------------------
DROP TABLE IF EXISTS `biz_lesson_assignment`;
CREATE TABLE `biz_lesson_assignment`  (
  `assignment_id` bigint NOT NULL AUTO_INCREMENT COMMENT '指派记录ID',
  `lesson_id` bigint NOT NULL COMMENT '课程ID (关联 biz_lesson.lesson_id)',
  `entry_year` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '指派班级的入学年份',
  `class_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '指派班级的班级编号',
  `assigner_id` bigint NULL DEFAULT NULL COMMENT '指派教师的用户ID (关联 sys_user.user_id)',
  `assign_time` datetime NULL DEFAULT NULL COMMENT '指派时间',
  PRIMARY KEY (`assignment_id`) USING BTREE,
  INDEX `idx_lesson_id`(`lesson_id` ASC) USING BTREE,
  INDEX `idx_entry_year_class_code`(`entry_year` ASC, `class_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 51 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课程班级指派表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of biz_lesson_assignment
-- ----------------------------
INSERT INTO `biz_lesson_assignment` VALUES (49, 20, '2025', '1', 104, '2026-01-08 08:56:41');
INSERT INTO `biz_lesson_assignment` VALUES (50, 20, '2025', '5', 104, '2026-01-08 08:56:41');

-- ----------------------------
-- Table structure for biz_lesson_question
-- ----------------------------
DROP TABLE IF EXISTS `biz_lesson_question`;
CREATE TABLE `biz_lesson_question`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `lesson_id` bigint NOT NULL COMMENT '课程ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `question_score` int NULL DEFAULT 0 COMMENT '该题目在本课程中的分值',
  `order_num` int NULL DEFAULT 0 COMMENT '题目在课程中的排序',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 63 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课程-题目关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of biz_lesson_question
-- ----------------------------
INSERT INTO `biz_lesson_question` VALUES (3, 2, 2, 10, 1);
INSERT INTO `biz_lesson_question` VALUES (4, 2, 1, 10, 2);
INSERT INTO `biz_lesson_question` VALUES (5, 2, 3, 10, 3);
INSERT INTO `biz_lesson_question` VALUES (6, 3, 1, 10, 1);
INSERT INTO `biz_lesson_question` VALUES (7, 3, 2, 10, 2);
INSERT INTO `biz_lesson_question` VALUES (10, 4, 1, 10, 1);
INSERT INTO `biz_lesson_question` VALUES (11, 4, 2, 90, 2);
INSERT INTO `biz_lesson_question` VALUES (38, 16, 9, 100, 1);
INSERT INTO `biz_lesson_question` VALUES (52, 18, 15, 50, 2);
INSERT INTO `biz_lesson_question` VALUES (53, 18, 43, 30, 2);
INSERT INTO `biz_lesson_question` VALUES (54, 18, 24, 10, 4);
INSERT INTO `biz_lesson_question` VALUES (55, 18, 48, 10, 4);
INSERT INTO `biz_lesson_question` VALUES (56, 19, 50, 50, 1);
INSERT INTO `biz_lesson_question` VALUES (57, 19, 15, 40, 2);
INSERT INTO `biz_lesson_question` VALUES (58, 19, 43, 10, 3);
INSERT INTO `biz_lesson_question` VALUES (59, 20, 43, 30, 1);
INSERT INTO `biz_lesson_question` VALUES (60, 20, 15, 10, 2);
INSERT INTO `biz_lesson_question` VALUES (61, 20, 17, 10, 3);
INSERT INTO `biz_lesson_question` VALUES (62, 20, 51, 50, 4);

-- ----------------------------
-- Table structure for biz_question
-- ----------------------------
DROP TABLE IF EXISTS `biz_question`;
CREATE TABLE `biz_question`  (
  `question_id` bigint NOT NULL AUTO_INCREMENT COMMENT '题目ID',
  `question_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '题目类型 (choice, typing, judgment, practical)',
  `question_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '题目内容/题干',
  `grade` int NULL DEFAULT NULL COMMENT '年级 (用于筛选)',
  `semester` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '学期 (0上册, 1下册, 用于筛选)',
  `lesson_num` int NULL DEFAULT NULL COMMENT '第几课 (1-15)',
  `option_a` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '选项A',
  `option_b` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '选项B',
  `option_c` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '选项C',
  `option_d` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '选项D',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '标准答案 (选择题存\"A\", 判断题存\"T\"或\"F\")',
  `analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '题目解析',
  `file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作题文件路径',
  `preview_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '预览文件路径(PDF)',
  `is_public` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '是否公开 (0私有 1公有)',
  `typing_duration` int NULL DEFAULT NULL COMMENT '打字时长(分钟)',
  `word_count` int NULL DEFAULT NULL COMMENT '总字数',
  `creator_id` bigint NULL DEFAULT NULL COMMENT '创建教师ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`question_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 52 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '统一题库表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of biz_question
-- ----------------------------
INSERT INTO `biz_question` VALUES (2, 'typing', 'kdkewdfkewfhdksaedjhfksdfuhvbdksusghvss', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Y', NULL, NULL, NULL, '', '2025-08-18 20:17:42', '', NULL);
INSERT INTO `biz_question` VALUES (4, 'typing', '成本v的司法环境测试v白色的数据库v从就开始第一个是白醋央视播出v就爱吃v吧而艰苦程度高于把苏永康VS从啊电池健康素养干哈八成功的的介绍客户参观巴萨大哭一场vu可以减肥成功就爱读书u会计初级的sys从数据库约定俗成啊u开始从访问速度快也是从v诉苦从v的快速有擦拭的库存苏看得出洒督促v发给多少库存v有发给谁毒抗蚜虫FSUKCFGU空间互踩法国的夙愿吃饭撒大开车的师傅他u撒督陈v发撒法看得出的素材v是SDFDCSUKY FGCSDAU6CAUSDC UASSJCVF GASDUCFDSUYCTFGVDS CJSUDCF USDCY AUACDY FDUCSF DSUCFDU CSDUSA CFADJYHSFGDCJUYSDCSAJ好几个v还记得上次是v成就感刀剑神域程度也是上次考试高分萨克创建VS就参与的素材是v方式督促v是督促萨克调查v从v苦于该发生的v看u哭的v阿萨', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '0', NULL, NULL, NULL, '', '2025-08-19 07:46:05', '', NULL);
INSERT INTO `biz_question` VALUES (5, 'choice', '<p><img src=\"/dev-api/profile/upload/2025/08/20/4_20250820110019A001.jpg\"></p><p>和规范化非常</p>', NULL, NULL, NULL, 'a', 'b', 'c', 'd', 'A', '<p><img src=\"/dev-api/profile/upload/2025/08/20/Quicker_20240827_151948_20250820110048A002.png\"></p>', NULL, NULL, 'Y', NULL, NULL, 1, 'admin', '2025-08-20 11:00:50', '', NULL);
INSERT INTO `biz_question` VALUES (6, 'typing', '<p>adscadsc都开始撑死了水库v出东方你死了你倒是快说v较好的死咯被忽略十五v的</p>', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Y', NULL, NULL, 1, 'admin', '2025-08-20 11:01:58', '', NULL);
INSERT INTO `biz_question` VALUES (7, 'judgment', '<p>除了vn选正确</p>', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'T', '<p>解析</p>', NULL, NULL, 'Y', NULL, NULL, 1, 'admin', '2025-08-20 11:02:27', '', NULL);
INSERT INTO `biz_question` VALUES (9, 'choice', '选择题', 7, '1', NULL, 'a', 'b', 'cv', 'd', 'A', 'a', NULL, NULL, '0', NULL, NULL, 1, 'admin', '2025-08-20 11:18:31', '', NULL);
INSERT INTO `biz_question` VALUES (10, 'choice', '第三次', 1, '0', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '0', NULL, NULL, 1, 'admin', '2025-08-20 11:21:00', '', NULL);
INSERT INTO `biz_question` VALUES (11, 'choice', 'u', 1, '1', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Y', NULL, NULL, 1, 'admin', '2025-08-20 11:36:40', '', NULL);
INSERT INTO `biz_question` VALUES (13, 'typing', '请输入这段用于打字练习的文字。', 8, '1', NULL, '', '', '', '', '', '', NULL, NULL, '1', NULL, NULL, 1, 'admin', '2025-08-20 12:12:38', '', NULL);
INSERT INTO `biz_question` VALUES (14, 'judgment', '地球是平的。', 7, '0', NULL, '', '', '', '', 'F', '', NULL, NULL, '1', NULL, NULL, 1, 'admin', '2025-08-20 12:12:38', '', NULL);
INSERT INTO `biz_question` VALUES (15, 'choice', '中国的首都是哪里？', 7, '0', NULL, '北京', '上海', '广州', '深圳', 'A', '', NULL, NULL, '1', NULL, NULL, 1, 'admin', '2025-08-20 12:19:41', '', NULL);
INSERT INTO `biz_question` VALUES (16, 'typing', '请输入这段用于打字练习的文字。', 8, '1', NULL, '', '', '', '', '', '', NULL, NULL, '1', NULL, NULL, 1, 'admin', '2025-08-20 12:19:41', '', NULL);
INSERT INTO `biz_question` VALUES (17, 'judgment', '地球是平的。', 7, '0', NULL, '', '', '', '', 'F', '', NULL, NULL, '0', NULL, NULL, 1, 'admin', '2025-08-20 12:19:41', '', NULL);
INSERT INTO `biz_question` VALUES (18, 'choice', '中国的首都是哪里？', 7, '0', NULL, '北京', '上海', '广州', '深圳', 'A', '', NULL, NULL, '0', NULL, NULL, 1, 'admin', '2025-08-20 12:25:03', '', NULL);
INSERT INTO `biz_question` VALUES (19, 'typing', '请输入这段用于打字练习的文字。', 8, '1', NULL, '', '', '', '', '', '', NULL, NULL, '0', NULL, NULL, 1, 'admin', '2025-08-20 12:25:03', '', NULL);
INSERT INTO `biz_question` VALUES (20, 'judgment', '地球是平的。', 7, '0', NULL, '', '', '', '', 'F', '', NULL, NULL, '0', NULL, NULL, 1, 'admin', '2025-08-20 12:25:03', '', NULL);
INSERT INTO `biz_question` VALUES (21, 'typing', '试点城市水水', 1, '1', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'N', NULL, NULL, 1, 'admin', '2025-08-20 12:26:01', '', NULL);
INSERT INTO `biz_question` VALUES (22, 'choice', '中国的首都是哪里？', 7, '0', NULL, '北京', '上海', '广州', '深圳', 'A', '', NULL, NULL, 'Y', NULL, NULL, 1, 'admin', '2025-08-20 12:26:48', '', NULL);
INSERT INTO `biz_question` VALUES (23, 'typing', '请输入这段用于打字练习的文字。', 8, '1', NULL, '', '', '', '', '', '', NULL, NULL, 'Y', NULL, NULL, 1, 'admin', '2025-08-20 12:26:48', '', NULL);
INSERT INTO `biz_question` VALUES (24, 'judgment', '地球是平的。', 7, '0', NULL, '', '', '', '', 'F', '', NULL, NULL, 'N', NULL, NULL, 1, 'admin', '2025-08-20 12:26:48', '', NULL);
INSERT INTO `biz_question` VALUES (26, 'typing', '商店超市菜市场', 1, '0', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'N', 1, 7, 1, 'admin', '2025-08-22 12:49:55', '', NULL);
INSERT INTO `biz_question` VALUES (27, 'choice', '中国的首都是哪里？', 7, '0', NULL, '北京', '上海', '广州', '深圳', 'A', '', NULL, NULL, 'Y', NULL, NULL, 1, 'admin', '2025-08-22 12:51:08', '', NULL);
INSERT INTO `biz_question` VALUES (28, 'typing', '请输入这段用于打字练习的文字。', 8, '1', NULL, '', '', '', '', '', '', NULL, NULL, 'Y', 2, 15, 1, 'admin', '2025-08-22 12:51:08', '', NULL);
INSERT INTO `biz_question` VALUES (29, 'judgment', '地球是平的。', 7, '0', NULL, '', '', '', '', 'F', '', NULL, NULL, 'N', NULL, NULL, 1, 'admin', '2025-08-22 12:51:08', '', NULL);
INSERT INTO `biz_question` VALUES (32, 'choice', '第三次多少', 5, '0', NULL, '2', '23', '3', '4', 'B', NULL, NULL, NULL, 'N', NULL, NULL, 1, 'admin', '2025-08-22 14:02:36', '', NULL);
INSERT INTO `biz_question` VALUES (33, 'typing', '按顺序是', 2, '1', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'N', 2, 4, 1, 'admin', '2025-08-22 14:23:04', '', NULL);
INSERT INTO `biz_question` VALUES (35, 'judgment', 'lihli', 2, '1', NULL, NULL, NULL, NULL, NULL, 'T', NULL, NULL, NULL, 'Y', NULL, NULL, 1, 'admin', '2025-08-25 17:16:30', '', NULL);
INSERT INTO `biz_question` VALUES (40, 'typing', '而感染尴尬而广泛认同过热', 7, '0', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Y', 4, 12, 104, '19157727791', '2025-08-25 22:08:59', '', NULL);
INSERT INTO `biz_question` VALUES (41, 'typing', 'fregfefg', 7, '0', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Y', 3, 8, 104, '19157727791', '2025-12-29 18:56:46', '', NULL);
INSERT INTO `biz_question` VALUES (42, 'typing', '你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是', 7, '0', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Y', 4, 612, 104, '19157727791', '2026-01-05 08:37:21', '', NULL);
INSERT INTO `biz_question` VALUES (43, 'typing', '一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。', 7, '0', 4, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Y', 5, 189, 104, '19157727791', '2026-01-05 08:46:43', '19157727791', '2026-01-05 08:47:40');
INSERT INTO `biz_question` VALUES (45, 'practical', '是公司架构', 7, '0', 3, NULL, NULL, NULL, NULL, NULL, NULL, '/profile/upload/2026/01/05/附件1_20260105152408A001.docx', '/profile./uploadPath/upload/2026/01/05/附件1_20260105152408A001.pdf', 'Y', NULL, NULL, 104, '19157727791', '2026-01-05 15:24:12', '', NULL);
INSERT INTO `biz_question` VALUES (46, 'practical', '山地车山地车山地车', 7, '1', 4, NULL, NULL, NULL, NULL, NULL, NULL, '/profile/upload/2026/01/05/附件1_20260105152856A001.docx', '/profile/upload/2026/01/05/附件1_20260105152856A001.pdf', 'Y', NULL, NULL, 104, '19157727791', '2026-01-05 15:28:58', '', NULL);
INSERT INTO `biz_question` VALUES (47, 'practical', '等等', 7, '1', 6, NULL, NULL, NULL, NULL, NULL, NULL, '/profile/upload/2026/01/05/附表2：作品创作说明_20260105153315A002.docx', '/profile/upload/2026/01/05/附表2：作品创作说明_20260105153315A002.pdf', 'Y', NULL, NULL, 104, '19157727791', '2026-01-05 15:33:18', '', NULL);
INSERT INTO `biz_question` VALUES (48, 'practical', '任务操作1', 7, '1', 1, NULL, NULL, NULL, NULL, NULL, NULL, '/profile/upload/2026/01/07/物品申请单_20260107141913A001.docx', '/profile/upload/2026/01/07/物品申请单_20260107141913A001.pdf', 'Y', NULL, NULL, 104, '19157727791', '2026-01-07 14:20:24', '', NULL);
INSERT INTO `biz_question` VALUES (49, 'practical', 'zdx', 7, '1', 5, NULL, NULL, NULL, NULL, NULL, NULL, '/profile/upload/2026/01/07/大模型本地部署配置_20260107162900A001.docx', '/profile/upload/2026/01/07/大模型本地部署配置_20260107162900A001.pdf', 'Y', NULL, NULL, 104, '19157727791', '2026-01-07 16:29:55', '', NULL);
INSERT INTO `biz_question` VALUES (50, 'practical', '郑东旭', 7, '1', 2, NULL, NULL, NULL, NULL, NULL, NULL, '/profile/upload/2026/01/07/物品申请单_20260107163743A002.docx', '/profile/upload/2026/01/07/物品申请单_20260107163743A002.pdf', 'Y', NULL, NULL, 104, '19157727791', '2026-01-07 16:38:16', '', NULL);
INSERT INTO `biz_question` VALUES (51, 'practical', '操作18', 7, '1', 7, NULL, NULL, NULL, NULL, NULL, NULL, '/profile/upload/2026/01/08/111_20260108085453A001.docx', '/profile/upload/2026/01/08/111_20260108085453A001.pdf', 'Y', NULL, NULL, 104, '19157727791', '2026-01-08 08:55:24', '', NULL);

-- ----------------------------
-- Table structure for biz_scoring_detail
-- ----------------------------
DROP TABLE IF EXISTS `biz_scoring_detail`;
CREATE TABLE `biz_scoring_detail`  (
  `detail_id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `answer_id` bigint NOT NULL COMMENT '答题记录ID',
  `item_id` bigint NOT NULL COMMENT '评分项ID',
  `score` int NOT NULL DEFAULT 0 COMMENT '得分',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`detail_id`) USING BTREE,
  UNIQUE INDEX `uk_answer_item`(`answer_id` ASC, `item_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '操作题分项得分记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_scoring_detail
-- ----------------------------
INSERT INTO `biz_scoring_detail` VALUES (5, 74, 8, 5, '2026-01-08 09:24:41', '2026-01-08 09:24:41');
INSERT INTO `biz_scoring_detail` VALUES (6, 74, 9, 6, '2026-01-08 09:24:41', '2026-01-08 09:24:41');

-- ----------------------------
-- Table structure for biz_scoring_item
-- ----------------------------
DROP TABLE IF EXISTS `biz_scoring_item`;
CREATE TABLE `biz_scoring_item`  (
  `item_id` bigint NOT NULL AUTO_INCREMENT COMMENT '评分项ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `item_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评分项名称',
  `item_score` int NOT NULL DEFAULT 10 COMMENT '该项满分',
  `order_num` int NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`item_id`) USING BTREE,
  INDEX `idx_question`(`question_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '操作题评分项定义' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_scoring_item
-- ----------------------------
INSERT INTO `biz_scoring_item` VALUES (6, 50, '界面', 40, 0, '2026-01-07 16:38:27', '2026-01-07 16:38:27');
INSERT INTO `biz_scoring_item` VALUES (7, 50, '你还哦', 60, 1, '2026-01-07 16:38:27', '2026-01-07 16:38:27');
INSERT INTO `biz_scoring_item` VALUES (8, 51, '界面', 60, 0, '2026-01-08 08:55:38', '2026-01-08 08:55:38');
INSERT INTO `biz_scoring_item` VALUES (9, 51, '答案', 40, 1, '2026-01-08 08:55:38', '2026-01-08 08:55:38');

-- ----------------------------
-- Table structure for biz_student
-- ----------------------------
DROP TABLE IF EXISTS `biz_student`;
CREATE TABLE `biz_student`  (
  `student_id` bigint NOT NULL AUTO_INCREMENT COMMENT '学生主键ID',
  `user_id` bigint NOT NULL COMMENT '关联的用户ID (sys_user.user_id)',
  `student_no` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '学号',
  `entry_year` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '入学年份',
  `class_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '班级编号',
  PRIMARY KEY (`student_id`) USING BTREE,
  UNIQUE INDEX `user_id_unique`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 62 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学生业务表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of biz_student
-- ----------------------------
INSERT INTO `biz_student` VALUES (54, 159, '1', '2024', '1');
INSERT INTO `biz_student` VALUES (55, 160, '2', '2024', '2');
INSERT INTO `biz_student` VALUES (56, 161, '4', '2024', '3');
INSERT INTO `biz_student` VALUES (57, 162, '2', '2025', '1');
INSERT INTO `biz_student` VALUES (58, 163, '6', '2025', '5');
INSERT INTO `biz_student` VALUES (59, 164, '1', '2024', '2');
INSERT INTO `biz_student` VALUES (60, 165, '2', '2023', '1');
INSERT INTO `biz_student` VALUES (61, 167, '1', '2025', '5');

-- ----------------------------
-- Table structure for biz_student_answer
-- ----------------------------
DROP TABLE IF EXISTS `biz_student_answer`;
CREATE TABLE `biz_student_answer`  (
  `answer_id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `lesson_id` bigint NOT NULL COMMENT '课程ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `student_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '学生答案',
  `is_correct` tinyint(1) NULL DEFAULT 0 COMMENT '是否正确',
  `score` int NULL DEFAULT 0 COMMENT '得分',
  `answer_time` int NULL DEFAULT 0 COMMENT '答题时间(秒)',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `typing_speed` int NULL DEFAULT NULL COMMENT '打字速度(字符/分钟)',
  `accuracy_rate` decimal(5, 2) NULL DEFAULT NULL COMMENT '正确率(%)',
  `completion_rate` decimal(5, 2) NULL DEFAULT NULL COMMENT '完成率(%)',
  PRIMARY KEY (`answer_id`) USING BTREE,
  INDEX `idx_student_lesson`(`student_id` ASC, `lesson_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 75 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生答题记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_student_answer
-- ----------------------------
INSERT INTO `biz_student_answer` VALUES (15, 58, 18, 40, '而感染尴尬而广泛', 0, 1, 0, '2026-01-05 08:31:57', NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (16, 58, 18, 42, '你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是念iv尼斯念你是  你是你是你是你是你是你是你是你是你是你是你是你是你是你是是你是你是你是你是你是你是是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是', 0, 14, 0, '2026-01-05 08:40:53', NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (29, 58, 18, 43, '一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统打算从事地产都是重复v发的地方不够的吧', 0, 5, 300, '2026-01-05 14:34:02', NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (57, 58, 18, 15, 'B', 0, 0, 0, '2026-01-06 09:18:13', NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (58, 58, 18, 24, '错', 1, 10, 0, '2026-01-06 09:18:13', NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (68, 58, 18, 47, '/profile/upload/2026/01/06/附表2：作品创作说明_20260105153315A002 (1)_20260106101625A003.docx', 0, 10, 0, '2026-01-06 10:16:26', NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (69, 58, 18, 48, '/profile/upload/2026/01/07/六年级信息科技测试题（含答题卡）_20260105160515A001 (1)_20260107142257A002.docx', 0, 0, 0, '2026-01-07 14:22:58', NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (70, 58, 19, 50, '/profile/upload/2026/01/07/两篇读书报告_加长版（确保每篇1000字以上）_20260107164120A003.docx', 0, 0, 0, '2026-01-07 16:41:20', NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (71, 58, 20, 43, '一直想做一款后吃', 0, 1, 14, '2026-01-08 08:57:56', NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (72, 58, 20, 15, 'B', 0, 0, 0, '2026-01-08 08:58:04', NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (73, 58, 20, 17, '错', 1, 10, 0, '2026-01-08 08:58:04', NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (74, 58, 20, 51, '/profile/upload/2026/01/08/附表2：作品创作说明_20260105153315A002_20260108085816A002.docx', 0, 11, 0, '2026-01-08 08:58:16', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for biz_teacher_class
-- ----------------------------
DROP TABLE IF EXISTS `biz_teacher_class`;
CREATE TABLE `biz_teacher_class`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '教师用户ID (关联 sys_user.user_id)',
  `dept_id` bigint NOT NULL COMMENT '学校ID (关联 sys_dept.dept_id)',
  `entry_year` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '入学年份',
  `class_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '班级编号',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_teacher_class`(`user_id` ASC, `dept_id` ASC, `entry_year` ASC, `class_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '教师-班级管理关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_teacher_class
-- ----------------------------
INSERT INTO `biz_teacher_class` VALUES (8, 104, 169, '2024', '2', '2025-12-29 18:23:25');
INSERT INTO `biz_teacher_class` VALUES (9, 104, 169, '2024', '1', '2025-12-29 18:23:25');
INSERT INTO `biz_teacher_class` VALUES (12, 104, 169, '2024', '3', '2025-12-29 18:23:41');
INSERT INTO `biz_teacher_class` VALUES (13, 104, 169, '2025', '1', '2025-12-29 18:35:32');
INSERT INTO `biz_teacher_class` VALUES (14, 104, 169, '2025', '5', '2025-12-29 18:35:32');
INSERT INTO `biz_teacher_class` VALUES (18, 166, 169, '2025', '1', '2025-12-30 14:02:35');
INSERT INTO `biz_teacher_class` VALUES (19, 166, 169, '2025', '5', '2025-12-30 14:02:35');
INSERT INTO `biz_teacher_class` VALUES (20, 166, 169, '2024', '1', '2025-12-30 14:02:47');
INSERT INTO `biz_teacher_class` VALUES (21, 166, 169, '2024', '2', '2025-12-30 14:02:47');
INSERT INTO `biz_teacher_class` VALUES (22, 166, 169, '2024', '3', '2025-12-30 14:02:47');

-- ----------------------------
-- Table structure for gen_table
-- ----------------------------
DROP TABLE IF EXISTS `gen_table`;
CREATE TABLE `gen_table`  (
  `table_id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '表名称',
  `table_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '表描述',
  `sub_table_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联子表的表名',
  `sub_table_fk_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '子表关联的外键名',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '实体类名称',
  `tpl_category` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'crud' COMMENT '使用的模板（crud单表操作 tree树表操作）',
  `tpl_web_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '前端模板类型（element-ui模版 element-plus模版）',
  `package_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成包路径',
  `module_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成模块名',
  `business_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成业务名',
  `function_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成功能名',
  `function_author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成功能作者',
  `gen_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
  `gen_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
  `options` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '其它生成选项',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`table_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代码生成业务表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of gen_table
-- ----------------------------
INSERT INTO `gen_table` VALUES (3, 'biz_school', '学校信息表', NULL, NULL, 'BizSchool', 'crud', 'element-plus', 'com.ruoyi.business', 'business', 'school', '学校信息', 'ruoyi', '0', '/', '{}', 'admin', '2025-06-24 10:40:46', '', '2025-06-24 10:41:30', NULL);
INSERT INTO `gen_table` VALUES (4, 'biz_student', '学生管理表', NULL, NULL, 'BizStudent', 'crud', 'element-plus', 'com.ruoyi.business', 'business', 'student', '学生管理', 'zdx', '0', '/', '{}', 'admin', '2025-06-25 10:38:55', '', '2025-06-25 10:41:19', NULL);
INSERT INTO `gen_table` VALUES (5, 'biz_lesson', '课程/作业信息表', NULL, NULL, 'BizLesson', 'crud', 'element-plus', 'com.ruoyi.business', 'business', 'lesson', '课程/作业信息', 'ruoyi', '0', '/', '{\"parentMenuId\":0}', 'admin', '2025-08-13 16:39:03', '', '2025-08-13 16:55:19', NULL);
INSERT INTO `gen_table` VALUES (6, 'biz_question', '题库管理表', NULL, NULL, 'BizQuestion', 'crud', 'element-plus', 'com.ruoyi.business', 'business', 'question', '题库管理', 'zdx', '0', '/', '{\"parentMenuId\":0}', 'admin', '2025-08-14 09:57:15', '', '2025-08-18 20:01:01', NULL);
INSERT INTO `gen_table` VALUES (7, 'biz_lesson_question', '课程-题目关联表', NULL, NULL, 'BizLessonQuestion', 'crud', 'element-plus', 'com.ruoyi.business', 'businesss', 'question', '课程-题目关联', 'ruoyi', '0', '/', '{\"parentMenuId\":0}', 'admin', '2025-08-18 20:11:18', '', '2025-08-18 20:38:28', NULL);
INSERT INTO `gen_table` VALUES (8, 'biz_lesson_assignment', '课程班级指派表', NULL, NULL, 'BizLessonAssignment', 'crud', 'element-plus', 'com.ruoyi.business', 'business', 'assignment', '课程班级指派', 'ruoyi', '0', '/', '{}', 'admin', '2025-08-25 19:12:22', '', '2025-08-25 19:16:09', NULL);

-- ----------------------------
-- Table structure for gen_table_column
-- ----------------------------
DROP TABLE IF EXISTS `gen_table_column`;
CREATE TABLE `gen_table_column`  (
  `column_id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_id` bigint NULL DEFAULT NULL COMMENT '归属表编号',
  `column_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列名称',
  `column_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列描述',
  `column_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列类型',
  `java_type` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'JAVA类型',
  `java_field` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'JAVA字段名',
  `is_pk` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否主键（1是）',
  `is_increment` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否自增（1是）',
  `is_required` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否必填（1是）',
  `is_insert` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否为插入字段（1是）',
  `is_edit` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否编辑字段（1是）',
  `is_list` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否列表字段（1是）',
  `is_query` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否查询字段（1是）',
  `query_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'EQ' COMMENT '查询方式（等于、不等于、大于、小于、范围）',
  `html_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  `dict_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
  `sort` int NULL DEFAULT NULL COMMENT '排序',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`column_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 70 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代码生成业务表字段' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of gen_table_column
-- ----------------------------
INSERT INTO `gen_table_column` VALUES (24, 3, 'school_id', '学校ID', 'bigint', 'Long', 'schoolId', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-06-24 10:40:46', '', '2025-06-24 10:41:30');
INSERT INTO `gen_table_column` VALUES (25, 3, 'school_name', '学校完整名称', 'varchar(100)', 'String', 'schoolName', '0', '0', '1', '1', '1', '1', '1', 'LIKE', 'input', '', 2, 'admin', '2025-06-24 10:40:46', '', '2025-06-24 10:41:30');
INSERT INTO `gen_table_column` VALUES (26, 3, 'school_type', '学校类型 (1小学 2初中 3高中)', 'char(1)', 'String', 'schoolType', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'select', '', 3, 'admin', '2025-06-24 10:40:46', '', '2025-06-24 10:41:30');
INSERT INTO `gen_table_column` VALUES (27, 4, 'student_id', '学生主键ID', 'bigint', 'Long', 'studentId', '1', '1', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-06-25 10:38:55', '', '2025-06-25 10:41:19');
INSERT INTO `gen_table_column` VALUES (28, 4, 'user_id', '关联的用户ID (sys_user.user_id)', 'bigint', 'Long', 'userId', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2025-06-25 10:38:55', '', '2025-06-25 10:41:19');
INSERT INTO `gen_table_column` VALUES (29, 4, 'school_id', '所属学校ID', 'bigint', 'Long', 'schoolId', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 3, 'admin', '2025-06-25 10:38:55', '', '2025-06-25 10:41:19');
INSERT INTO `gen_table_column` VALUES (30, 4, 'student_no', '学号', 'varchar(30)', 'String', 'studentNo', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 4, 'admin', '2025-06-25 10:38:55', '', '2025-06-25 10:41:19');
INSERT INTO `gen_table_column` VALUES (31, 4, 'entry_year', '入学年份', 'varchar(4)', 'String', 'entryYear', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 5, 'admin', '2025-06-25 10:38:55', '', '2025-06-25 10:41:19');
INSERT INTO `gen_table_column` VALUES (32, 4, 'class_code', '班级编号', 'varchar(30)', 'String', 'classCode', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 6, 'admin', '2025-06-25 10:38:55', '', '2025-06-25 10:41:19');
INSERT INTO `gen_table_column` VALUES (33, 5, 'lesson_id', '课程ID', 'bigint', 'Long', 'lessonId', '1', '1', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-08-13 16:39:03', '', '2025-08-13 16:55:19');
INSERT INTO `gen_table_column` VALUES (34, 5, 'lesson_title', '课程/作业标题', 'varchar(255)', 'String', 'lessonTitle', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2025-08-13 16:39:03', '', '2025-08-13 16:55:19');
INSERT INTO `gen_table_column` VALUES (35, 5, 'grade', '年级 (例如: 1, 2, 3, 4, 5, 6)', 'int', 'Long', 'grade', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'select', 'biz_grade', 3, 'admin', '2025-08-13 16:39:03', '', '2025-08-13 16:55:19');
INSERT INTO `gen_table_column` VALUES (36, 5, 'semester', '学期 (0上册, 1下册)', 'char(1)', 'String', 'semester', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'select', 'biz_semester', 4, 'admin', '2025-08-13 16:39:03', '', '2025-08-13 16:55:19');
INSERT INTO `gen_table_column` VALUES (37, 5, 'lesson_num', '第几课 (例如: 1, 2, 3)', 'int', 'Long', 'lessonNum', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 5, 'admin', '2025-08-13 16:39:03', '', '2025-08-13 16:55:19');
INSERT INTO `gen_table_column` VALUES (38, 5, 'creator_id', '创建教师ID', 'bigint', 'Long', 'creatorId', '0', '0', '0', '0', '0', '1', '1', 'EQ', 'input', '', 6, 'admin', '2025-08-13 16:39:03', '', '2025-08-13 16:55:19');
INSERT INTO `gen_table_column` VALUES (39, 5, 'create_by', '创建者', 'varchar(64)', 'String', 'createBy', '0', '0', '0', '0', NULL, NULL, NULL, 'EQ', 'input', '', 7, 'admin', '2025-08-13 16:39:03', '', '2025-08-13 16:55:19');
INSERT INTO `gen_table_column` VALUES (40, 5, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '0', NULL, NULL, NULL, 'EQ', 'datetime', '', 8, 'admin', '2025-08-13 16:39:03', '', '2025-08-13 16:55:19');
INSERT INTO `gen_table_column` VALUES (41, 5, 'update_by', '更新者', 'varchar(64)', 'String', 'updateBy', '0', '0', '0', '0', '0', NULL, NULL, 'EQ', 'input', '', 9, 'admin', '2025-08-13 16:39:03', '', '2025-08-13 16:55:19');
INSERT INTO `gen_table_column` VALUES (42, 5, 'update_time', '更新时间', 'datetime', 'Date', 'updateTime', '0', '0', '0', '0', '0', NULL, NULL, 'EQ', 'datetime', '', 10, 'admin', '2025-08-13 16:39:03', '', '2025-08-13 16:55:19');
INSERT INTO `gen_table_column` VALUES (43, 6, 'question_id', '题目ID', 'bigint', 'Long', 'questionId', '1', '1', '0', '0', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-08-14 09:57:15', '', '2025-08-18 20:01:01');
INSERT INTO `gen_table_column` VALUES (44, 6, 'question_type', '题目类型 (choice, typing, judgment, practical)', 'varchar(20)', 'String', 'questionType', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'select', 'biz_question_type', 2, 'admin', '2025-08-14 09:57:15', '', '2025-08-18 20:01:01');
INSERT INTO `gen_table_column` VALUES (45, 6, 'question_content', '题目内容/题干', 'text', 'String', 'questionContent', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'textarea', '', 3, 'admin', '2025-08-14 09:57:15', '', '2025-08-18 20:01:01');
INSERT INTO `gen_table_column` VALUES (46, 6, 'option_a', '选项A', 'varchar(255)', 'String', 'optionA', '0', '0', '0', '1', '1', '0', '0', 'EQ', 'input', '', 4, 'admin', '2025-08-14 09:57:15', '', '2025-08-18 20:01:01');
INSERT INTO `gen_table_column` VALUES (47, 6, 'option_b', '选项B', 'varchar(255)', 'String', 'optionB', '0', '0', '0', '1', '1', '0', '0', 'EQ', 'input', '', 5, 'admin', '2025-08-14 09:57:15', '', '2025-08-18 20:01:01');
INSERT INTO `gen_table_column` VALUES (48, 6, 'option_c', '选项C', 'varchar(255)', 'String', 'optionC', '0', '0', '0', '1', '1', '0', '0', 'EQ', 'input', '', 6, 'admin', '2025-08-14 09:57:15', '', '2025-08-18 20:01:01');
INSERT INTO `gen_table_column` VALUES (49, 6, 'option_d', '选项D', 'varchar(255)', 'String', 'optionD', '0', '0', '0', '1', '1', '0', '0', 'EQ', 'input', '', 7, 'admin', '2025-08-14 09:57:15', '', '2025-08-18 20:01:01');
INSERT INTO `gen_table_column` VALUES (50, 6, 'answer', '标准答案 (选择题存\"A\", 判断题存\"T\"或\"F\")', 'text', 'String', 'answer', '0', '0', '0', '1', '1', '0', '0', 'EQ', 'textarea', '', 8, 'admin', '2025-08-14 09:57:15', '', '2025-08-18 20:01:01');
INSERT INTO `gen_table_column` VALUES (51, 6, 'analysis', '题目解析', 'text', 'String', 'analysis', '0', '0', '0', '1', '1', '0', '0', 'EQ', 'textarea', '', 9, 'admin', '2025-08-14 09:57:15', '', '2025-08-18 20:01:01');
INSERT INTO `gen_table_column` VALUES (52, 6, 'file_path', '操作题文件路径', 'varchar(255)', 'String', 'filePath', '0', '0', '0', '1', '1', '0', '0', 'EQ', 'fileUpload', '', 10, 'admin', '2025-08-14 09:57:15', '', '2025-08-18 20:01:01');
INSERT INTO `gen_table_column` VALUES (53, 6, 'is_public', '是否公开 (0私有 1公有)', 'char(1)', 'String', 'isPublic', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'radio', 'sys_yes_no', 11, 'admin', '2025-08-14 09:57:15', '', '2025-08-18 20:01:01');
INSERT INTO `gen_table_column` VALUES (54, 6, 'creator_id', '创建教师ID', 'bigint', 'Long', 'creatorId', '0', '0', '0', '0', '0', '0', '0', 'EQ', 'input', '', 12, 'admin', '2025-08-14 09:57:15', '', '2025-08-18 20:01:01');
INSERT INTO `gen_table_column` VALUES (55, 6, 'create_by', '创建者', 'varchar(64)', 'String', 'createBy', '0', '0', '0', '0', NULL, '1', NULL, 'EQ', 'input', '', 13, 'admin', '2025-08-14 09:57:15', '', '2025-08-18 20:01:01');
INSERT INTO `gen_table_column` VALUES (56, 6, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '0', NULL, '1', NULL, 'EQ', 'datetime', '', 14, 'admin', '2025-08-14 09:57:15', '', '2025-08-18 20:01:01');
INSERT INTO `gen_table_column` VALUES (57, 6, 'update_by', '更新者', 'varchar(64)', 'String', 'updateBy', '0', '0', '0', '0', '0', NULL, NULL, 'EQ', 'input', '', 15, 'admin', '2025-08-14 09:57:15', '', '2025-08-18 20:01:01');
INSERT INTO `gen_table_column` VALUES (58, 6, 'update_time', '更新时间', 'datetime', 'Date', 'updateTime', '0', '0', '0', '0', '0', NULL, NULL, 'EQ', 'datetime', '', 16, 'admin', '2025-08-14 09:57:15', '', '2025-08-18 20:01:01');
INSERT INTO `gen_table_column` VALUES (59, 7, 'id', NULL, 'bigint', 'Long', 'id', '1', '1', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-08-18 20:11:18', '', '2025-08-18 20:38:28');
INSERT INTO `gen_table_column` VALUES (60, 7, 'lesson_id', '课程ID', 'bigint', 'Long', 'lessonId', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2025-08-18 20:11:18', '', '2025-08-18 20:38:28');
INSERT INTO `gen_table_column` VALUES (61, 7, 'question_id', '题目ID', 'bigint', 'Long', 'questionId', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 3, 'admin', '2025-08-18 20:11:18', '', '2025-08-18 20:38:28');
INSERT INTO `gen_table_column` VALUES (62, 7, 'question_score', '该题目在本课程中的分值', 'int', 'Long', 'questionScore', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 4, 'admin', '2025-08-18 20:11:18', '', '2025-08-18 20:38:28');
INSERT INTO `gen_table_column` VALUES (63, 7, 'order_num', '题目在课程中的排序', 'int', 'Long', 'orderNum', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 5, 'admin', '2025-08-18 20:11:18', '', '2025-08-18 20:38:28');
INSERT INTO `gen_table_column` VALUES (64, 8, 'assignment_id', '指派记录ID', 'bigint', 'Long', 'assignmentId', '1', '1', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-08-25 19:12:22', '', '2025-08-25 19:16:09');
INSERT INTO `gen_table_column` VALUES (65, 8, 'lesson_id', '课程ID (关联 biz_lesson.lesson_id)', 'bigint', 'Long', 'lessonId', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2025-08-25 19:12:22', '', '2025-08-25 19:16:09');
INSERT INTO `gen_table_column` VALUES (66, 8, 'entry_year', '指派班级的入学年份', 'varchar(4)', 'String', 'entryYear', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 3, 'admin', '2025-08-25 19:12:22', '', '2025-08-25 19:16:09');
INSERT INTO `gen_table_column` VALUES (67, 8, 'class_code', '指派班级的班级编号', 'varchar(30)', 'String', 'classCode', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 4, 'admin', '2025-08-25 19:12:22', '', '2025-08-25 19:16:09');
INSERT INTO `gen_table_column` VALUES (68, 8, 'assigner_id', '指派教师的用户ID (关联 sys_user.user_id)', 'bigint', 'Long', 'assignerId', '0', '0', '0', '0', '0', '0', '0', 'EQ', 'input', '', 5, 'admin', '2025-08-25 19:12:22', '', '2025-08-25 19:16:09');
INSERT INTO `gen_table_column` VALUES (69, 8, 'assign_time', '指派时间', 'datetime', 'Date', 'assignTime', '0', '0', '0', '0', '0', '1', '1', 'EQ', 'datetime', '', 6, 'admin', '2025-08-25 19:12:22', '', '2025-08-25 19:16:09');

-- ----------------------------
-- Table structure for qrtz_blob_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_blob_triggers`;
CREATE TABLE `qrtz_blob_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `blob_data` blob NULL COMMENT '存放持久化Trigger对象',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_blob_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'Blob类型的触发器表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_blob_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_calendars
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_calendars`;
CREATE TABLE `qrtz_calendars`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `calendar_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '日历名称',
  `calendar` blob NOT NULL COMMENT '存放持久化calendar对象',
  PRIMARY KEY (`sched_name`, `calendar_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '日历信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_calendars
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_cron_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_cron_triggers`;
CREATE TABLE `qrtz_cron_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `cron_expression` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'cron表达式',
  `time_zone_id` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '时区',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_cron_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'Cron类型的触发器表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_cron_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_fired_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_fired_triggers`;
CREATE TABLE `qrtz_fired_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `entry_id` varchar(95) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度器实例id',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `instance_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度器实例名',
  `fired_time` bigint NOT NULL COMMENT '触发的时间',
  `sched_time` bigint NOT NULL COMMENT '定时器制定的时间',
  `priority` int NOT NULL COMMENT '优先级',
  `state` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务名称',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务组名',
  `is_nonconcurrent` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否并发',
  `requests_recovery` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否接受恢复执行',
  PRIMARY KEY (`sched_name`, `entry_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '已触发的触发器表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_fired_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_job_details
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_job_details`;
CREATE TABLE `qrtz_job_details`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务组名',
  `description` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '相关介绍',
  `job_class_name` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '执行任务类名称',
  `is_durable` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否持久化',
  `is_nonconcurrent` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否并发',
  `is_update_data` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否更新数据',
  `requests_recovery` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否接受恢复执行',
  `job_data` blob NULL COMMENT '存放持久化job对象',
  PRIMARY KEY (`sched_name`, `job_name`, `job_group`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '任务详细信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_job_details
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_locks
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_locks`;
CREATE TABLE `qrtz_locks`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `lock_name` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '悲观锁名称',
  PRIMARY KEY (`sched_name`, `lock_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '存储的悲观锁信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_locks
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_paused_trigger_grps
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_paused_trigger_grps`;
CREATE TABLE `qrtz_paused_trigger_grps`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  PRIMARY KEY (`sched_name`, `trigger_group`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '暂停的触发器表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_paused_trigger_grps
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_scheduler_state
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_scheduler_state`;
CREATE TABLE `qrtz_scheduler_state`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `instance_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '实例名称',
  `last_checkin_time` bigint NOT NULL COMMENT '上次检查时间',
  `checkin_interval` bigint NOT NULL COMMENT '检查间隔时间',
  PRIMARY KEY (`sched_name`, `instance_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '调度器状态表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_scheduler_state
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_simple_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_simple_triggers`;
CREATE TABLE `qrtz_simple_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `repeat_count` bigint NOT NULL COMMENT '重复的次数统计',
  `repeat_interval` bigint NOT NULL COMMENT '重复的间隔时间',
  `times_triggered` bigint NOT NULL COMMENT '已经触发的次数',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_simple_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '简单触发器的信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_simple_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_simprop_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_simprop_triggers`;
CREATE TABLE `qrtz_simprop_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `str_prop_1` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'String类型的trigger的第一个参数',
  `str_prop_2` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'String类型的trigger的第二个参数',
  `str_prop_3` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'String类型的trigger的第三个参数',
  `int_prop_1` int NULL DEFAULT NULL COMMENT 'int类型的trigger的第一个参数',
  `int_prop_2` int NULL DEFAULT NULL COMMENT 'int类型的trigger的第二个参数',
  `long_prop_1` bigint NULL DEFAULT NULL COMMENT 'long类型的trigger的第一个参数',
  `long_prop_2` bigint NULL DEFAULT NULL COMMENT 'long类型的trigger的第二个参数',
  `dec_prop_1` decimal(13, 4) NULL DEFAULT NULL COMMENT 'decimal类型的trigger的第一个参数',
  `dec_prop_2` decimal(13, 4) NULL DEFAULT NULL COMMENT 'decimal类型的trigger的第二个参数',
  `bool_prop_1` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Boolean类型的trigger的第一个参数',
  `bool_prop_2` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Boolean类型的trigger的第二个参数',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_simprop_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '同步机制的行锁表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_simprop_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_triggers`;
CREATE TABLE `qrtz_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器的名字',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器所属组的名字',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_job_details表job_name的外键',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_job_details表job_group的外键',
  `description` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '相关介绍',
  `next_fire_time` bigint NULL DEFAULT NULL COMMENT '上一次触发时间（毫秒）',
  `prev_fire_time` bigint NULL DEFAULT NULL COMMENT '下一次触发时间（默认为-1表示不触发）',
  `priority` int NULL DEFAULT NULL COMMENT '优先级',
  `trigger_state` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器状态',
  `trigger_type` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器的类型',
  `start_time` bigint NOT NULL COMMENT '开始时间',
  `end_time` bigint NULL DEFAULT NULL COMMENT '结束时间',
  `calendar_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '日程表名称',
  `misfire_instr` smallint NULL DEFAULT NULL COMMENT '补偿执行的策略',
  `job_data` blob NULL COMMENT '存放持久化job对象',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  INDEX `sched_name`(`sched_name` ASC, `job_name` ASC, `job_group` ASC) USING BTREE,
  CONSTRAINT `qrtz_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `job_name`, `job_group`) REFERENCES `qrtz_job_details` (`sched_name`, `job_name`, `job_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '触发器详细信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `config_id` int NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数键值',
  `config_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '参数配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y', 'admin', '2025-06-12 14:49:21', '', NULL, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow');
INSERT INTO `sys_config` VALUES (2, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 'Y', 'admin', '2025-06-12 14:49:21', '', NULL, '初始化密码 123456');
INSERT INTO `sys_config` VALUES (3, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 'Y', 'admin', '2025-06-12 14:49:21', '', NULL, '深色主题theme-dark，浅色主题theme-light');
INSERT INTO `sys_config` VALUES (4, '账号自助-验证码开关', 'sys.account.captchaEnabled', 'true', 'Y', 'admin', '2025-06-12 14:49:21', '', NULL, '是否开启验证码功能（true开启，false关闭）');
INSERT INTO `sys_config` VALUES (5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'false', 'Y', 'admin', '2025-06-12 14:49:21', '', NULL, '是否开启注册用户功能（true开启，false关闭）');
INSERT INTO `sys_config` VALUES (6, '用户登录-黑名单列表', 'sys.login.blackIPList', '', 'Y', 'admin', '2025-06-12 14:49:21', '', NULL, '设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）');
INSERT INTO `sys_config` VALUES (7, '用户管理-初始密码修改策略', 'sys.account.initPasswordModify', '1', 'Y', 'admin', '2025-06-12 14:49:21', '', NULL, '0：初始密码修改策略关闭，没有任何提示，1：提醒用户，如果未修改初始密码，则在登录时就会提醒修改密码对话框');
INSERT INTO `sys_config` VALUES (8, '用户管理-账号密码更新周期', 'sys.account.passwordValidateDays', '0', 'Y', 'admin', '2025-06-12 14:49:21', '', NULL, '密码更新周期（填写数字，数据初始化值为0不限制，若修改必须为大于0小于365的正整数），如果超过这个周期登录系统时，则在登录时就会提醒修改密码对话框');

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `dept_id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父部门id',
  `ancestors` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '祖级列表',
  `dept_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '部门名称',
  `order_num` int NULL DEFAULT 0 COMMENT '显示顺序',
  `leader` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `school_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '学校官方ID',
  `school_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '学校类型 (1小学 2初中 3高中)',
  PRIMARY KEY (`dept_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 180 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '部门表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept` VALUES (100, 0, '0', '象山县', 0, '管理员', '15888888888', 'ry@qq.com', '0', '0', '', NULL, '', NULL, NULL, NULL);
INSERT INTO `sys_dept` VALUES (101, 100, '0,100', '小学', 1, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, NULL, NULL);
INSERT INTO `sys_dept` VALUES (102, 100, '0,100', '初中', 2, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, NULL, NULL);
INSERT INTO `sys_dept` VALUES (103, 100, '0,100', '高中', 3, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, NULL, NULL);
INSERT INTO `sys_dept` VALUES (104, 101, '0,100,101', '测试学校', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '2', '1');
INSERT INTO `sys_dept` VALUES (105, 101, '0,100,101', '实验小学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '4', '1');
INSERT INTO `sys_dept` VALUES (106, 101, '0,100,101', '城南学校', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '5', '1');
INSERT INTO `sys_dept` VALUES (107, 101, '0,100,101', '大徐小学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '7', '1');
INSERT INTO `sys_dept` VALUES (108, 101, '0,100,101', '丹城二小', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '8', '1');
INSERT INTO `sys_dept` VALUES (109, 101, '0,100,101', '丹城三小', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '10', '1');
INSERT INTO `sys_dept` VALUES (110, 101, '0,100,101', '东陈小学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '13', '1');
INSERT INTO `sys_dept` VALUES (111, 101, '0,100,101', '番头小学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '15', '1');
INSERT INTO `sys_dept` VALUES (112, 101, '0,100,101', '爵溪学校(小学部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '17', '1');
INSERT INTO `sys_dept` VALUES (113, 101, '0,100,101', '茅洋学校(小学部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '20', '1');
INSERT INTO `sys_dept` VALUES (114, 101, '0,100,101', '石浦小学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '22', '1');
INSERT INTO `sys_dept` VALUES (115, 101, '0,100,101', '泗洲头小学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '23', '1');
INSERT INTO `sys_dept` VALUES (116, 101, '0,100,101', '涂茨小学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '24', '1');
INSERT INTO `sys_dept` VALUES (117, 101, '0,100,101', '外国语学校(小学部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '25', '1');
INSERT INTO `sys_dept` VALUES (118, 101, '0,100,101', '文峰学校(小学部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '27', '1');
INSERT INTO `sys_dept` VALUES (119, 101, '0,100,101', '西周小学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '29', '1');
INSERT INTO `sys_dept` VALUES (120, 101, '0,100,101', '贤庠学校(小学部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '31', '1');
INSERT INTO `sys_dept` VALUES (121, 101, '0,100,101', '晓塘小学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '35', '1');
INSERT INTO `sys_dept` VALUES (122, 101, '0,100,101', '延昌小学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '37', '1');
INSERT INTO `sys_dept` VALUES (123, 101, '0,100,101', '丹城四小', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '38', '1');
INSERT INTO `sys_dept` VALUES (124, 101, '0,100,101', '林海学校(小学部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '40', '1');
INSERT INTO `sys_dept` VALUES (125, 101, '0,100,101', '昌国小学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '42', '1');
INSERT INTO `sys_dept` VALUES (126, 101, '0,100,101', '墙头学校(小学部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '43', '1');
INSERT INTO `sys_dept` VALUES (127, 101, '0,100,101', '新桥学校(小学部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '45', '1');
INSERT INTO `sys_dept` VALUES (128, 101, '0,100,101', '新港小学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '47', '1');
INSERT INTO `sys_dept` VALUES (129, 101, '0,100,101', '金星学校(小学部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '49', '1');
INSERT INTO `sys_dept` VALUES (130, 101, '0,100,101', '下沈小学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '52', '1');
INSERT INTO `sys_dept` VALUES (131, 101, '0,100,101', '鹤浦小学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '53', '1');
INSERT INTO `sys_dept` VALUES (132, 101, '0,100,101', '黄避岙小学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '54', '1');
INSERT INTO `sys_dept` VALUES (133, 101, '0,100,101', '高塘学校(小学部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '55', '1');
INSERT INTO `sys_dept` VALUES (134, 101, '0,100,101', '定塘小学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '57', '1');
INSERT INTO `sys_dept` VALUES (135, 101, '0,100,101', '启力学校(小学部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '58', '1');
INSERT INTO `sys_dept` VALUES (136, 101, '0,100,101', '丹城五小', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '59', '1');
INSERT INTO `sys_dept` VALUES (137, 101, '0,100,101', '梅林小学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '68', '1');
INSERT INTO `sys_dept` VALUES (138, 101, '0,100,101', '亭溪完小', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '70', '1');
INSERT INTO `sys_dept` VALUES (139, 101, '0,100,101', '大目湾学校(小学部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '71', '1');
INSERT INTO `sys_dept` VALUES (140, 101, '0,100,101', '墩岙塘小学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '74', '1');
INSERT INTO `sys_dept` VALUES (141, 101, '0,100,101', '实验小学南区', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '75', '1');
INSERT INTO `sys_dept` VALUES (142, 101, '0,100,101', '丹城六小', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '77', '1');
INSERT INTO `sys_dept` VALUES (143, 101, '0,100,101', '培智学校(小学部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '79', '1');
INSERT INTO `sys_dept` VALUES (144, 101, '0,100,101', '丹山书院(小学部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '82', '1');
INSERT INTO `sys_dept` VALUES (145, 102, '0,100,102', '测试学校', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '3', '2');
INSERT INTO `sys_dept` VALUES (146, 102, '0,100,102', '丹城二中', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '9', '2');
INSERT INTO `sys_dept` VALUES (147, 102, '0,100,102', '丹城中学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '11', '2');
INSERT INTO `sys_dept` VALUES (148, 102, '0,100,102', '定塘中学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '12', '2');
INSERT INTO `sys_dept` VALUES (149, 102, '0,100,102', '东陈中学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '14', '2');
INSERT INTO `sys_dept` VALUES (150, 102, '0,100,102', '鹤浦中学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '16', '2');
INSERT INTO `sys_dept` VALUES (151, 102, '0,100,102', '爵溪学校(初中部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '18', '2');
INSERT INTO `sys_dept` VALUES (152, 102, '0,100,102', '荔港中学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '19', '2');
INSERT INTO `sys_dept` VALUES (153, 102, '0,100,102', '茅洋学校(初中部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '21', '2');
INSERT INTO `sys_dept` VALUES (154, 102, '0,100,102', '外国语学校(初中部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '26', '2');
INSERT INTO `sys_dept` VALUES (155, 102, '0,100,102', '文峰学校(初中部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '28', '2');
INSERT INTO `sys_dept` VALUES (156, 102, '0,100,102', '西周中学(初中部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '30', '2');
INSERT INTO `sys_dept` VALUES (157, 102, '0,100,102', '贤庠学校(初中部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '32', '2');
INSERT INTO `sys_dept` VALUES (158, 102, '0,100,102', '石浦中学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '36', '2');
INSERT INTO `sys_dept` VALUES (159, 102, '0,100,102', '殷夫中学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '39', '2');
INSERT INTO `sys_dept` VALUES (160, 102, '0,100,102', '林海学校(初中部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '41', '2');
INSERT INTO `sys_dept` VALUES (161, 102, '0,100,102', '墙头学校(初中部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '44', '2');
INSERT INTO `sys_dept` VALUES (162, 102, '0,100,102', '新桥学校(初中部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '46', '2');
INSERT INTO `sys_dept` VALUES (163, 102, '0,100,102', '滨海学校(初中部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '48', '2');
INSERT INTO `sys_dept` VALUES (164, 102, '0,100,102', '金星学校(初中部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '50', '2');
INSERT INTO `sys_dept` VALUES (165, 102, '0,100,102', '泗洲头中学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '51', '2');
INSERT INTO `sys_dept` VALUES (166, 102, '0,100,102', '高塘学校(初中部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '56', '2');
INSERT INTO `sys_dept` VALUES (167, 102, '0,100,102', '实验初中', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '60', '2');
INSERT INTO `sys_dept` VALUES (168, 102, '0,100,102', '教科研中心', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '61', '2');
INSERT INTO `sys_dept` VALUES (169, 102, '0,100,102', '大目湾学校(初中部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '72', '2');
INSERT INTO `sys_dept` VALUES (170, 102, '0,100,102', '启力学校(初中部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '76', '2');
INSERT INTO `sys_dept` VALUES (171, 102, '0,100,102', '塔山中学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '78', '2');
INSERT INTO `sys_dept` VALUES (172, 102, '0,100,102', '培智学校(初中部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '80', '2');
INSERT INTO `sys_dept` VALUES (173, 102, '0,100,102', '丹山书院(初中部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '81', '2');
INSERT INTO `sys_dept` VALUES (174, 103, '0,100,103', '象山二中', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '62', '3');
INSERT INTO `sys_dept` VALUES (175, 103, '0,100,103', '象山三中', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '63', '3');
INSERT INTO `sys_dept` VALUES (176, 103, '0,100,103', '滨海学校(高中部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '64', '3');
INSERT INTO `sys_dept` VALUES (177, 103, '0,100,103', '西周中学(高中部)', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '65', '3');
INSERT INTO `sys_dept` VALUES (178, 103, '0,100,103', '象山中学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '67', '3');
INSERT INTO `sys_dept` VALUES (179, 103, '0,100,103', '大目湾高级中学', 0, NULL, NULL, NULL, '0', '0', '', NULL, '', NULL, '83', '3');

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
  `dict_code` bigint NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  `dict_sort` int NULL DEFAULT 0 COMMENT '字典排序',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '表格回显样式',
  `is_default` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 118 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典数据表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO `sys_dict_data` VALUES (1, 1, '男', '0', 'sys_user_sex', '', '', 'Y', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '性别男');
INSERT INTO `sys_dict_data` VALUES (2, 2, '女', '1', 'sys_user_sex', '', '', 'N', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '性别女');
INSERT INTO `sys_dict_data` VALUES (3, 3, '未知', '2', 'sys_user_sex', '', '', 'N', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '性别未知');
INSERT INTO `sys_dict_data` VALUES (4, 1, '显示', '0', 'sys_show_hide', '', 'primary', 'Y', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '显示菜单');
INSERT INTO `sys_dict_data` VALUES (5, 2, '隐藏', '1', 'sys_show_hide', '', 'danger', 'N', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '隐藏菜单');
INSERT INTO `sys_dict_data` VALUES (6, 1, '正常', '0', 'sys_normal_disable', '', 'primary', 'Y', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (7, 2, '停用', '1', 'sys_normal_disable', '', 'danger', 'N', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` VALUES (8, 1, '正常', '0', 'sys_job_status', '', 'primary', 'Y', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (9, 2, '暂停', '1', 'sys_job_status', '', 'danger', 'N', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` VALUES (10, 1, '默认', 'DEFAULT', 'sys_job_group', '', '', 'Y', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '默认分组');
INSERT INTO `sys_dict_data` VALUES (11, 2, '系统', 'SYSTEM', 'sys_job_group', '', '', 'N', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '系统分组');
INSERT INTO `sys_dict_data` VALUES (12, 1, '是', 'Y', 'sys_yes_no', '', 'primary', 'Y', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '系统默认是');
INSERT INTO `sys_dict_data` VALUES (13, 2, '否', 'N', 'sys_yes_no', '', 'danger', 'N', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '系统默认否');
INSERT INTO `sys_dict_data` VALUES (14, 1, '通知', '1', 'sys_notice_type', '', 'warning', 'Y', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '通知');
INSERT INTO `sys_dict_data` VALUES (15, 2, '公告', '2', 'sys_notice_type', '', 'success', 'N', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '公告');
INSERT INTO `sys_dict_data` VALUES (16, 1, '正常', '0', 'sys_notice_status', '', 'primary', 'Y', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (17, 2, '关闭', '1', 'sys_notice_status', '', 'danger', 'N', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '关闭状态');
INSERT INTO `sys_dict_data` VALUES (18, 99, '其他', '0', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '其他操作');
INSERT INTO `sys_dict_data` VALUES (19, 1, '新增', '1', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '新增操作');
INSERT INTO `sys_dict_data` VALUES (20, 2, '修改', '2', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '修改操作');
INSERT INTO `sys_dict_data` VALUES (21, 3, '删除', '3', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '删除操作');
INSERT INTO `sys_dict_data` VALUES (22, 4, '授权', '4', 'sys_oper_type', '', 'primary', 'N', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '授权操作');
INSERT INTO `sys_dict_data` VALUES (23, 5, '导出', '5', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '导出操作');
INSERT INTO `sys_dict_data` VALUES (24, 6, '导入', '6', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '导入操作');
INSERT INTO `sys_dict_data` VALUES (25, 7, '强退', '7', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '强退操作');
INSERT INTO `sys_dict_data` VALUES (26, 8, '生成代码', '8', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '生成操作');
INSERT INTO `sys_dict_data` VALUES (27, 9, '清空数据', '9', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '清空操作');
INSERT INTO `sys_dict_data` VALUES (28, 1, '成功', '0', 'sys_common_status', '', 'primary', 'N', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (29, 2, '失败', '1', 'sys_common_status', '', 'danger', 'N', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` VALUES (100, 1, '上册', '0', 'biz_semester', NULL, 'default', 'N', '0', 'admin', '2025-08-13 16:47:11', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (101, 2, '下册', '1', 'biz_semester', NULL, 'default', 'N', '0', 'admin', '2025-08-13 16:47:25', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (102, 1, '一年级', '1', 'biz_grade', NULL, 'default', 'N', '0', 'admin', '2025-08-13 16:52:47', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (103, 2, '二年级', '2', 'biz_grade', NULL, 'default', 'N', '0', 'admin', '2025-08-13 16:52:58', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (104, 3, '三年级', '3', 'biz_grade', NULL, 'default', 'N', '0', 'admin', '2025-08-13 16:53:07', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (105, 4, '四年级', '4', 'biz_grade', NULL, 'default', 'N', '0', 'admin', '2025-08-13 16:53:16', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (106, 5, '五年级', '5', 'biz_grade', NULL, 'default', 'N', '0', 'admin', '2025-08-13 16:53:29', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (107, 6, '六年级', '6', 'biz_grade', NULL, 'default', 'N', '0', 'admin', '2025-08-13 16:53:38', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (108, 7, '七年级', '7', 'biz_grade', NULL, 'default', 'N', '0', 'admin', '2025-08-13 16:53:51', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (109, 8, '八年级', '8', 'biz_grade', NULL, 'default', 'N', '0', 'admin', '2025-08-13 16:53:59', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (110, 9, '九年级', '9', 'biz_grade', NULL, 'default', 'N', '0', 'admin', '2025-08-13 16:54:09', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (111, 10, '高一', '10', 'biz_grade', NULL, 'default', 'N', '0', 'admin', '2025-08-13 16:54:19', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (112, 11, '高二', '11', 'biz_grade', NULL, 'default', 'N', '0', 'admin', '2025-08-13 16:54:29', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (113, 12, '高三', '12', 'biz_grade', NULL, 'default', 'N', '0', 'admin', '2025-08-13 16:54:43', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (114, 1, '选择题', 'choice', 'biz_question_type', NULL, 'default', 'N', '0', 'admin', '2025-08-14 09:55:24', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (115, 2, '打字题', 'typing', 'biz_question_type', NULL, 'default', 'N', '0', 'admin', '2025-08-14 09:55:43', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (116, 3, '判断题', 'judgment', 'biz_question_type', NULL, 'default', 'N', '0', 'admin', '2025-08-14 09:56:01', '', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (117, 4, '操作题', 'practical', 'biz_question_type', NULL, 'default', 'N', '0', 'admin', '2025-08-14 09:56:23', '', NULL, NULL);

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `dict_id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_id`) USING BTREE,
  UNIQUE INDEX `dict_type`(`dict_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 103 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典类型表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1, '用户性别', 'sys_user_sex', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '用户性别列表');
INSERT INTO `sys_dict_type` VALUES (2, '菜单状态', 'sys_show_hide', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '菜单状态列表');
INSERT INTO `sys_dict_type` VALUES (3, '系统开关', 'sys_normal_disable', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '系统开关列表');
INSERT INTO `sys_dict_type` VALUES (4, '任务状态', 'sys_job_status', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '任务状态列表');
INSERT INTO `sys_dict_type` VALUES (5, '任务分组', 'sys_job_group', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '任务分组列表');
INSERT INTO `sys_dict_type` VALUES (6, '系统是否', 'sys_yes_no', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '系统是否列表');
INSERT INTO `sys_dict_type` VALUES (7, '通知类型', 'sys_notice_type', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '通知类型列表');
INSERT INTO `sys_dict_type` VALUES (8, '通知状态', 'sys_notice_status', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '通知状态列表');
INSERT INTO `sys_dict_type` VALUES (9, '操作类型', 'sys_oper_type', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '操作类型列表');
INSERT INTO `sys_dict_type` VALUES (10, '系统状态', 'sys_common_status', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '登录状态列表');
INSERT INTO `sys_dict_type` VALUES (100, '学期', 'biz_semester', '0', 'admin', '2025-08-13 16:46:33', '', NULL, NULL);
INSERT INTO `sys_dict_type` VALUES (101, '年级', 'biz_grade', '0', 'admin', '2025-08-13 16:52:20', '', NULL, NULL);
INSERT INTO `sys_dict_type` VALUES (102, '题目类型', 'biz_question_type', '0', 'admin', '2025-08-14 09:54:28', '', NULL, NULL);

-- ----------------------------
-- Table structure for sys_job
-- ----------------------------
DROP TABLE IF EXISTS `sys_job`;
CREATE TABLE `sys_job`  (
  `job_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '任务名称',
  `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
  `invoke_target` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调用目标字符串',
  `cron_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT 'cron执行表达式',
  `misfire_policy` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '3' COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
  `concurrent` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '1' COMMENT '是否并发执行（0允许 1禁止）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1暂停）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '备注信息',
  PRIMARY KEY (`job_id`, `job_name`, `job_group`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '定时任务调度表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_job
-- ----------------------------
INSERT INTO `sys_job` VALUES (1, '系统默认（无参）', 'DEFAULT', 'ryTask.ryNoParams', '0/10 * * * * ?', '3', '1', '1', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_job` VALUES (2, '系统默认（有参）', 'DEFAULT', 'ryTask.ryParams(\'ry\')', '0/15 * * * * ?', '3', '1', '1', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_job` VALUES (3, '系统默认（多参）', 'DEFAULT', 'ryTask.ryMultipleParams(\'ry\', true, 2000L, 316.50D, 100)', '0/20 * * * * ?', '3', '1', '1', 'admin', '2025-06-12 14:49:21', '', NULL, '');

-- ----------------------------
-- Table structure for sys_job_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_job_log`;
CREATE TABLE `sys_job_log`  (
  `job_log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务日志ID',
  `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务组名',
  `invoke_target` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调用目标字符串',
  `job_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '日志信息',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '执行状态（0正常 1失败）',
  `exception_info` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '异常信息',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`job_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '定时任务调度日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_job_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_logininfor
-- ----------------------------
DROP TABLE IF EXISTS `sys_logininfor`;
CREATE TABLE `sys_logininfor`  (
  `info_id` bigint NOT NULL AUTO_INCREMENT COMMENT '访问ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '用户账号',
  `ipaddr` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '登录IP地址',
  `login_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '登录地点',
  `browser` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '操作系统',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
  `msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '提示消息',
  `login_time` datetime NULL DEFAULT NULL COMMENT '访问时间',
  PRIMARY KEY (`info_id`) USING BTREE,
  INDEX `idx_sys_logininfor_s`(`status` ASC) USING BTREE,
  INDEX `idx_sys_logininfor_lt`(`login_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 305 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统访问记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_logininfor
-- ----------------------------
INSERT INTO `sys_logininfor` VALUES (100, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-12 15:01:32');
INSERT INTO `sys_logininfor` VALUES (101, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-12 16:23:12');
INSERT INTO `sys_logininfor` VALUES (102, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-17 15:21:15');
INSERT INTO `sys_logininfor` VALUES (103, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-17 16:55:17');
INSERT INTO `sys_logininfor` VALUES (104, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-17 17:32:49');
INSERT INTO `sys_logininfor` VALUES (105, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-17 18:31:04');
INSERT INTO `sys_logininfor` VALUES (106, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-06-17 18:40:14');
INSERT INTO `sys_logininfor` VALUES (107, 't01', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-17 18:40:26');
INSERT INTO `sys_logininfor` VALUES (108, 't01', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-18 16:26:51');
INSERT INTO `sys_logininfor` VALUES (109, 't01', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-18 17:00:16');
INSERT INTO `sys_logininfor` VALUES (110, 't01', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-19 08:34:15');
INSERT INTO `sys_logininfor` VALUES (111, 't01', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-06-19 09:01:18');
INSERT INTO `sys_logininfor` VALUES (112, 't01', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-19 09:01:23');
INSERT INTO `sys_logininfor` VALUES (113, 't01', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-06-19 09:23:47');
INSERT INTO `sys_logininfor` VALUES (114, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-19 09:24:01');
INSERT INTO `sys_logininfor` VALUES (115, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-06-19 09:48:55');
INSERT INTO `sys_logininfor` VALUES (116, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '用户不存在/密码错误', '2025-06-19 09:49:05');
INSERT INTO `sys_logininfor` VALUES (117, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-19 09:49:16');
INSERT INTO `sys_logininfor` VALUES (118, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-19 13:22:28');
INSERT INTO `sys_logininfor` VALUES (119, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-06-19 14:20:56');
INSERT INTO `sys_logininfor` VALUES (120, '2022710422', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '用户不存在/密码错误', '2025-06-19 14:21:06');
INSERT INTO `sys_logininfor` VALUES (121, '2022710422', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '用户不存在/密码错误', '2025-06-19 14:21:42');
INSERT INTO `sys_logininfor` VALUES (122, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-19 14:22:11');
INSERT INTO `sys_logininfor` VALUES (123, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-06-19 14:23:33');
INSERT INTO `sys_logininfor` VALUES (124, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-19 14:23:44');
INSERT INTO `sys_logininfor` VALUES (125, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-06-19 14:26:29');
INSERT INTO `sys_logininfor` VALUES (126, '2022710488', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-19 14:26:43');
INSERT INTO `sys_logininfor` VALUES (127, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-19 14:40:39');
INSERT INTO `sys_logininfor` VALUES (128, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-19 15:14:45');
INSERT INTO `sys_logininfor` VALUES (129, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-23 10:19:49');
INSERT INTO `sys_logininfor` VALUES (130, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-06-23 10:23:05');
INSERT INTO `sys_logininfor` VALUES (131, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-23 10:23:09');
INSERT INTO `sys_logininfor` VALUES (132, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-24 08:47:04');
INSERT INTO `sys_logininfor` VALUES (133, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-24 09:44:39');
INSERT INTO `sys_logininfor` VALUES (134, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-06-24 09:45:33');
INSERT INTO `sys_logininfor` VALUES (135, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-24 09:45:48');
INSERT INTO `sys_logininfor` VALUES (136, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-06-24 09:47:35');
INSERT INTO `sys_logininfor` VALUES (137, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-24 09:47:39');
INSERT INTO `sys_logininfor` VALUES (138, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-06-24 09:47:42');
INSERT INTO `sys_logininfor` VALUES (139, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-24 09:47:54');
INSERT INTO `sys_logininfor` VALUES (140, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-06-24 10:30:04');
INSERT INTO `sys_logininfor` VALUES (141, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-24 10:30:17');
INSERT INTO `sys_logininfor` VALUES (142, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-24 13:23:52');
INSERT INTO `sys_logininfor` VALUES (143, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-24 14:07:06');
INSERT INTO `sys_logininfor` VALUES (144, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-06-24 14:27:57');
INSERT INTO `sys_logininfor` VALUES (145, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-24 14:28:02');
INSERT INTO `sys_logininfor` VALUES (146, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-25 10:18:40');
INSERT INTO `sys_logininfor` VALUES (147, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-06-25 15:10:35');
INSERT INTO `sys_logininfor` VALUES (148, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-07-02 08:21:18');
INSERT INTO `sys_logininfor` VALUES (149, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-07-02 08:52:09');
INSERT INTO `sys_logininfor` VALUES (150, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-07-02 09:40:52');
INSERT INTO `sys_logininfor` VALUES (151, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-07-02 10:11:12');
INSERT INTO `sys_logininfor` VALUES (152, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码已失效', '2025-07-02 10:14:25');
INSERT INTO `sys_logininfor` VALUES (153, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-07-02 10:14:30');
INSERT INTO `sys_logininfor` VALUES (154, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-07-02 10:16:07');
INSERT INTO `sys_logininfor` VALUES (155, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-07-02 10:16:20');
INSERT INTO `sys_logininfor` VALUES (156, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-07-02 10:16:55');
INSERT INTO `sys_logininfor` VALUES (157, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-07-02 10:17:18');
INSERT INTO `sys_logininfor` VALUES (158, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-07-02 10:21:35');
INSERT INTO `sys_logininfor` VALUES (159, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-07-02 10:21:51');
INSERT INTO `sys_logininfor` VALUES (160, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-07-02 10:25:45');
INSERT INTO `sys_logininfor` VALUES (161, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-07-02 10:26:05');
INSERT INTO `sys_logininfor` VALUES (162, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-07-02 13:09:36');
INSERT INTO `sys_logininfor` VALUES (163, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-07-02 13:45:52');
INSERT INTO `sys_logininfor` VALUES (164, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-07-02 14:52:12');
INSERT INTO `sys_logininfor` VALUES (165, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-07-02 14:52:18');
INSERT INTO `sys_logininfor` VALUES (166, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-07-02 14:55:41');
INSERT INTO `sys_logininfor` VALUES (167, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2025-07-02 14:55:45');
INSERT INTO `sys_logininfor` VALUES (168, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2025-07-02 14:55:53');
INSERT INTO `sys_logininfor` VALUES (169, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-07-02 14:59:23');
INSERT INTO `sys_logininfor` VALUES (170, '2024720401', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-07-02 15:03:46');
INSERT INTO `sys_logininfor` VALUES (171, '2024720401', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-07-02 15:05:40');
INSERT INTO `sys_logininfor` VALUES (172, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-07-02 15:05:45');
INSERT INTO `sys_logininfor` VALUES (173, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-07-07 18:51:08');
INSERT INTO `sys_logininfor` VALUES (174, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '退出成功', '2025-07-07 18:51:51');
INSERT INTO `sys_logininfor` VALUES (175, '19157727791', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-07-07 18:52:10');
INSERT INTO `sys_logininfor` VALUES (176, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '1', '验证码错误', '2025-08-04 16:14:36');
INSERT INTO `sys_logininfor` VALUES (177, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-08-04 16:14:40');
INSERT INTO `sys_logininfor` VALUES (178, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '用户不存在/密码错误', '2025-08-04 16:15:32');
INSERT INTO `sys_logininfor` VALUES (179, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '用户不存在/密码错误', '2025-08-04 16:15:55');
INSERT INTO `sys_logininfor` VALUES (180, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-08-04 16:16:32');
INSERT INTO `sys_logininfor` VALUES (181, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-08-07 15:43:49');
INSERT INTO `sys_logininfor` VALUES (182, '2024720401', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-08-07 15:51:00');
INSERT INTO `sys_logininfor` VALUES (183, '2024720401', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-08-07 15:51:35');
INSERT INTO `sys_logininfor` VALUES (184, '2024720401', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '用户不存在/密码错误', '2025-08-07 15:51:42');
INSERT INTO `sys_logininfor` VALUES (185, '2024720401', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '用户不存在/密码错误', '2025-08-07 15:51:56');
INSERT INTO `sys_logininfor` VALUES (186, '2024720401', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-08-07 15:52:07');
INSERT INTO `sys_logininfor` VALUES (187, '2024720401', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2025-08-07 15:54:12');
INSERT INTO `sys_logininfor` VALUES (188, '2024720401', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-08-07 15:54:21');
INSERT INTO `sys_logininfor` VALUES (189, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '退出成功', '2025-08-07 15:56:17');
INSERT INTO `sys_logininfor` VALUES (190, '19157727791', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-08-07 15:56:29');
INSERT INTO `sys_logininfor` VALUES (191, '19157727791', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '退出成功', '2025-08-07 16:25:29');
INSERT INTO `sys_logininfor` VALUES (192, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-08-07 16:25:33');
INSERT INTO `sys_logininfor` VALUES (193, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '退出成功', '2025-08-07 16:25:51');
INSERT INTO `sys_logininfor` VALUES (194, '19157727791', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-08-07 16:26:05');
INSERT INTO `sys_logininfor` VALUES (195, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-08-07 17:33:12');
INSERT INTO `sys_logininfor` VALUES (196, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '退出成功', '2025-08-07 17:33:44');
INSERT INTO `sys_logininfor` VALUES (197, '19157727791', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-08-07 17:34:02');
INSERT INTO `sys_logininfor` VALUES (198, '19157727791', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '1', '验证码错误', '2025-08-08 14:37:19');
INSERT INTO `sys_logininfor` VALUES (199, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-08-08 16:54:53');
INSERT INTO `sys_logininfor` VALUES (200, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '退出成功', '2025-08-08 16:57:26');
INSERT INTO `sys_logininfor` VALUES (201, '19157727791', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-08-08 16:57:45');
INSERT INTO `sys_logininfor` VALUES (202, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-08-09 19:20:32');
INSERT INTO `sys_logininfor` VALUES (203, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-08-12 15:47:02');
INSERT INTO `sys_logininfor` VALUES (204, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-08-13 16:30:47');
INSERT INTO `sys_logininfor` VALUES (205, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-08-14 09:28:05');
INSERT INTO `sys_logininfor` VALUES (206, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-08-18 20:00:00');
INSERT INTO `sys_logininfor` VALUES (207, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-08-18 20:09:59');
INSERT INTO `sys_logininfor` VALUES (208, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-08-18 21:34:46');
INSERT INTO `sys_logininfor` VALUES (209, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-08-19 07:18:57');
INSERT INTO `sys_logininfor` VALUES (210, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-08-20 09:50:49');
INSERT INTO `sys_logininfor` VALUES (211, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-08-20 10:59:16');
INSERT INTO `sys_logininfor` VALUES (212, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-08-20 13:12:07');
INSERT INTO `sys_logininfor` VALUES (213, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-08-22 11:20:55');
INSERT INTO `sys_logininfor` VALUES (214, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-08-22 12:49:15');
INSERT INTO `sys_logininfor` VALUES (215, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '1', '验证码错误', '2025-08-22 13:52:59');
INSERT INTO `sys_logininfor` VALUES (216, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-08-22 13:53:02');
INSERT INTO `sys_logininfor` VALUES (217, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-08-25 17:16:15');
INSERT INTO `sys_logininfor` VALUES (218, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-08-25 18:13:10');
INSERT INTO `sys_logininfor` VALUES (219, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '1', '验证码已失效', '2025-08-25 20:03:17');
INSERT INTO `sys_logininfor` VALUES (220, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-08-25 20:03:21');
INSERT INTO `sys_logininfor` VALUES (221, '19157727791', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2025-08-25 20:06:11');
INSERT INTO `sys_logininfor` VALUES (222, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-08-25 21:36:49');
INSERT INTO `sys_logininfor` VALUES (223, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-10-07 12:06:14');
INSERT INTO `sys_logininfor` VALUES (224, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-07 12:07:24');
INSERT INTO `sys_logininfor` VALUES (225, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '验证码已失效', '2025-10-07 14:09:09');
INSERT INTO `sys_logininfor` VALUES (226, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-07 14:09:14');
INSERT INTO `sys_logininfor` VALUES (227, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-10-07 14:09:29');
INSERT INTO `sys_logininfor` VALUES (228, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-10-07 14:41:23');
INSERT INTO `sys_logininfor` VALUES (229, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-07 14:42:00');
INSERT INTO `sys_logininfor` VALUES (230, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2025-10-08 13:13:45');
INSERT INTO `sys_logininfor` VALUES (231, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-08 13:14:16');
INSERT INTO `sys_logininfor` VALUES (232, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-08 14:32:37');
INSERT INTO `sys_logininfor` VALUES (233, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2025-10-09 16:26:04');
INSERT INTO `sys_logininfor` VALUES (234, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-10-09 16:26:17');
INSERT INTO `sys_logininfor` VALUES (235, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-11-21 16:12:00');
INSERT INTO `sys_logininfor` VALUES (236, 'dmwxinli', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2025-12-17 08:44:40');
INSERT INTO `sys_logininfor` VALUES (237, 'dmwxinli', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2025-12-17 08:45:22');
INSERT INTO `sys_logininfor` VALUES (238, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-17 08:45:39');
INSERT INTO `sys_logininfor` VALUES (239, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-26 15:47:01');
INSERT INTO `sys_logininfor` VALUES (240, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-29 14:24:12');
INSERT INTO `sys_logininfor` VALUES (241, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-12-29 14:24:42');
INSERT INTO `sys_logininfor` VALUES (242, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-29 14:24:57');
INSERT INTO `sys_logininfor` VALUES (243, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-12-29 14:28:55');
INSERT INTO `sys_logininfor` VALUES (244, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-29 14:28:59');
INSERT INTO `sys_logininfor` VALUES (245, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-12-29 14:29:35');
INSERT INTO `sys_logininfor` VALUES (246, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-29 14:29:49');
INSERT INTO `sys_logininfor` VALUES (247, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-29 15:39:05');
INSERT INTO `sys_logininfor` VALUES (248, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-29 18:10:43');
INSERT INTO `sys_logininfor` VALUES (249, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-30 09:55:20');
INSERT INTO `sys_logininfor` VALUES (250, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-12-30 10:44:59');
INSERT INTO `sys_logininfor` VALUES (251, 'yelaoshi', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-30 10:45:14');
INSERT INTO `sys_logininfor` VALUES (252, 'yelaoshi', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-30 13:59:50');
INSERT INTO `sys_logininfor` VALUES (253, 'yelaoshi', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-12-30 14:03:36');
INSERT INTO `sys_logininfor` VALUES (254, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-30 14:03:49');
INSERT INTO `sys_logininfor` VALUES (255, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-30 15:03:16');
INSERT INTO `sys_logininfor` VALUES (256, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-12-30 15:04:31');
INSERT INTO `sys_logininfor` VALUES (257, 'yelaoshi', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-30 15:04:44');
INSERT INTO `sys_logininfor` VALUES (258, 'yelaoshi', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2025-12-30 15:05:06');
INSERT INTO `sys_logininfor` VALUES (259, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-30 15:05:22');
INSERT INTO `sys_logininfor` VALUES (260, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-30 15:53:14');
INSERT INTO `sys_logininfor` VALUES (261, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-30 16:41:20');
INSERT INTO `sys_logininfor` VALUES (262, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '验证码已失效', '2025-12-31 10:34:49');
INSERT INTO `sys_logininfor` VALUES (263, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-31 10:34:53');
INSERT INTO `sys_logininfor` VALUES (264, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-31 10:37:03');
INSERT INTO `sys_logininfor` VALUES (265, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-31 11:32:22');
INSERT INTO `sys_logininfor` VALUES (266, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2025-12-31 11:32:43');
INSERT INTO `sys_logininfor` VALUES (267, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-04 16:30:05');
INSERT INTO `sys_logininfor` VALUES (268, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-04 16:30:42');
INSERT INTO `sys_logininfor` VALUES (269, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-05 08:23:57');
INSERT INTO `sys_logininfor` VALUES (270, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-05 08:25:15');
INSERT INTO `sys_logininfor` VALUES (271, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-05 11:20:55');
INSERT INTO `sys_logininfor` VALUES (272, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-05 11:21:22');
INSERT INTO `sys_logininfor` VALUES (273, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-05 13:11:56');
INSERT INTO `sys_logininfor` VALUES (274, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-05 13:12:18');
INSERT INTO `sys_logininfor` VALUES (275, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-05 14:07:03');
INSERT INTO `sys_logininfor` VALUES (276, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-05 14:50:49');
INSERT INTO `sys_logininfor` VALUES (277, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-05 15:35:28');
INSERT INTO `sys_logininfor` VALUES (278, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-06 08:36:40');
INSERT INTO `sys_logininfor` VALUES (279, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-06 08:37:09');
INSERT INTO `sys_logininfor` VALUES (280, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-06 10:15:54');
INSERT INTO `sys_logininfor` VALUES (281, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-06 11:32:13');
INSERT INTO `sys_logininfor` VALUES (282, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-06 11:45:26');
INSERT INTO `sys_logininfor` VALUES (283, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-06 11:45:44');
INSERT INTO `sys_logininfor` VALUES (284, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2026-01-06 14:44:36');
INSERT INTO `sys_logininfor` VALUES (285, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2026-01-06 14:44:41');
INSERT INTO `sys_logininfor` VALUES (286, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-06 14:44:56');
INSERT INTO `sys_logininfor` VALUES (287, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2026-01-06 15:11:57');
INSERT INTO `sys_logininfor` VALUES (288, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-06 15:12:07');
INSERT INTO `sys_logininfor` VALUES (289, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '验证码已失效', '2026-01-06 16:53:19');
INSERT INTO `sys_logininfor` VALUES (290, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-06 16:53:24');
INSERT INTO `sys_logininfor` VALUES (291, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-07 08:58:52');
INSERT INTO `sys_logininfor` VALUES (292, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-07 14:17:55');
INSERT INTO `sys_logininfor` VALUES (293, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-07 14:22:45');
INSERT INTO `sys_logininfor` VALUES (294, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-07 16:28:05');
INSERT INTO `sys_logininfor` VALUES (295, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2026-01-07 16:40:23');
INSERT INTO `sys_logininfor` VALUES (296, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-07 16:40:42');
INSERT INTO `sys_logininfor` VALUES (297, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-08 08:53:55');
INSERT INTO `sys_logininfor` VALUES (298, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-08 08:57:35');
INSERT INTO `sys_logininfor` VALUES (299, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-08 09:48:14');
INSERT INTO `sys_logininfor` VALUES (300, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-08 09:48:20');
INSERT INTO `sys_logininfor` VALUES (301, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-08 09:53:23');
INSERT INTO `sys_logininfor` VALUES (302, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-08 09:53:41');
INSERT INTO `sys_logininfor` VALUES (303, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-08 12:54:39');
INSERT INTO `sys_logininfor` VALUES (304, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-08 16:22:17');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `menu_id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单名称',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父菜单ID',
  `order_num` int NULL DEFAULT 0 COMMENT '显示顺序',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '组件路径',
  `query` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路由参数',
  `route_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '路由名称',
  `is_frame` int NULL DEFAULT 1 COMMENT '是否为外链（0是 1否）',
  `is_cache` int NULL DEFAULT 0 COMMENT '是否缓存（0缓存 1不缓存）',
  `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '#' COMMENT '菜单图标',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2043 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '菜单权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, '系统管理', 0, 1, 'system', NULL, '', '', 1, 0, 'M', '0', '0', '', 'system', 'admin', '2025-06-12 14:49:21', '', NULL, '系统管理目录');
INSERT INTO `sys_menu` VALUES (2, '系统监控', 0, 2, 'monitor', NULL, '', '', 1, 0, 'M', '0', '0', '', 'monitor', 'admin', '2025-06-12 14:49:21', '', NULL, '系统监控目录');
INSERT INTO `sys_menu` VALUES (3, '系统工具', 0, 3, 'tool', NULL, '', '', 1, 0, 'M', '0', '0', '', 'tool', 'admin', '2025-06-12 14:49:21', '', NULL, '系统工具目录');
INSERT INTO `sys_menu` VALUES (4, '若依官网', 0, 4, 'http://ruoyi.vip', NULL, '', '', 0, 0, 'M', '0', '0', '', 'guide', 'admin', '2025-06-12 14:49:21', '', NULL, '若依官网地址');
INSERT INTO `sys_menu` VALUES (100, '用户管理', 1, 1, 'user', 'system/user/index', '', '', 1, 0, 'C', '0', '0', 'system:user:list', 'user', 'admin', '2025-06-12 14:49:21', '', NULL, '用户管理菜单');
INSERT INTO `sys_menu` VALUES (101, '角色管理', 1, 2, 'role', 'system/role/index', '', '', 1, 0, 'C', '0', '0', 'system:role:list', 'peoples', 'admin', '2025-06-12 14:49:21', '', NULL, '角色管理菜单');
INSERT INTO `sys_menu` VALUES (102, '菜单管理', 1, 3, 'menu', 'system/menu/index', '', '', 1, 0, 'C', '0', '0', 'system:menu:list', 'tree-table', 'admin', '2025-06-12 14:49:21', '', NULL, '菜单管理菜单');
INSERT INTO `sys_menu` VALUES (103, '部门管理', 1, 4, 'dept', 'system/dept/index', '', '', 1, 0, 'C', '0', '0', 'system:dept:list', 'tree', 'admin', '2025-06-12 14:49:21', '', NULL, '部门管理菜单');
INSERT INTO `sys_menu` VALUES (104, '岗位管理', 1, 5, 'post', 'system/post/index', '', '', 1, 0, 'C', '0', '0', 'system:post:list', 'post', 'admin', '2025-06-12 14:49:21', '', NULL, '岗位管理菜单');
INSERT INTO `sys_menu` VALUES (105, '字典管理', 1, 6, 'dict', 'system/dict/index', '', '', 1, 0, 'C', '0', '0', 'system:dict:list', 'dict', 'admin', '2025-06-12 14:49:21', '', NULL, '字典管理菜单');
INSERT INTO `sys_menu` VALUES (106, '参数设置', 1, 7, 'config', 'system/config/index', '', '', 1, 0, 'C', '0', '0', 'system:config:list', 'edit', 'admin', '2025-06-12 14:49:21', '', NULL, '参数设置菜单');
INSERT INTO `sys_menu` VALUES (107, '通知公告', 1, 8, 'notice', 'system/notice/index', '', '', 1, 0, 'C', '0', '0', 'system:notice:list', 'message', 'admin', '2025-06-12 14:49:21', '', NULL, '通知公告菜单');
INSERT INTO `sys_menu` VALUES (108, '日志管理', 1, 9, 'log', '', '', '', 1, 0, 'M', '0', '0', '', 'log', 'admin', '2025-06-12 14:49:21', '', NULL, '日志管理菜单');
INSERT INTO `sys_menu` VALUES (109, '在线用户', 2, 1, 'online', 'monitor/online/index', '', '', 1, 0, 'C', '0', '0', 'monitor:online:list', 'online', 'admin', '2025-06-12 14:49:21', '', NULL, '在线用户菜单');
INSERT INTO `sys_menu` VALUES (110, '定时任务', 2, 2, 'job', 'monitor/job/index', '', '', 1, 0, 'C', '0', '0', 'monitor:job:list', 'job', 'admin', '2025-06-12 14:49:21', '', NULL, '定时任务菜单');
INSERT INTO `sys_menu` VALUES (111, '数据监控', 2, 3, 'druid', 'monitor/druid/index', '', '', 1, 0, 'C', '0', '0', 'monitor:druid:list', 'druid', 'admin', '2025-06-12 14:49:21', '', NULL, '数据监控菜单');
INSERT INTO `sys_menu` VALUES (112, '服务监控', 2, 4, 'server', 'monitor/server/index', '', '', 1, 0, 'C', '0', '0', 'monitor:server:list', 'server', 'admin', '2025-06-12 14:49:21', '', NULL, '服务监控菜单');
INSERT INTO `sys_menu` VALUES (113, '缓存监控', 2, 5, 'cache', 'monitor/cache/index', '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list', 'redis', 'admin', '2025-06-12 14:49:21', '', NULL, '缓存监控菜单');
INSERT INTO `sys_menu` VALUES (114, '缓存列表', 2, 6, 'cacheList', 'monitor/cache/list', '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list', 'redis-list', 'admin', '2025-06-12 14:49:21', '', NULL, '缓存列表菜单');
INSERT INTO `sys_menu` VALUES (115, '表单构建', 3, 1, 'build', 'tool/build/index', '', '', 1, 0, 'C', '0', '0', 'tool:build:list', 'build', 'admin', '2025-06-12 14:49:21', '', NULL, '表单构建菜单');
INSERT INTO `sys_menu` VALUES (116, '代码生成', 3, 2, 'gen', 'tool/gen/index', '', '', 1, 0, 'C', '0', '0', 'tool:gen:list', 'code', 'admin', '2025-06-12 14:49:21', '', NULL, '代码生成菜单');
INSERT INTO `sys_menu` VALUES (117, '系统接口', 3, 3, 'swagger', 'tool/swagger/index', '', '', 1, 0, 'C', '0', '0', 'tool:swagger:list', 'swagger', 'admin', '2025-06-12 14:49:21', '', NULL, '系统接口菜单');
INSERT INTO `sys_menu` VALUES (500, '操作日志', 108, 1, 'operlog', 'monitor/operlog/index', '', '', 1, 0, 'C', '0', '0', 'monitor:operlog:list', 'form', 'admin', '2025-06-12 14:49:21', '', NULL, '操作日志菜单');
INSERT INTO `sys_menu` VALUES (501, '登录日志', 108, 2, 'logininfor', 'monitor/logininfor/index', '', '', 1, 0, 'C', '0', '0', 'monitor:logininfor:list', 'logininfor', 'admin', '2025-06-12 14:49:21', '', NULL, '登录日志菜单');
INSERT INTO `sys_menu` VALUES (1000, '用户查询', 100, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:query', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1001, '用户新增', 100, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:add', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1002, '用户修改', 100, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1003, '用户删除', 100, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1004, '用户导出', 100, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:export', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1005, '用户导入', 100, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:import', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1006, '重置密码', 100, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1007, '角色查询', 101, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:query', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1008, '角色新增', 101, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:add', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1009, '角色修改', 101, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1010, '角色删除', 101, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1011, '角色导出', 101, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:export', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1012, '菜单查询', 102, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:query', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1013, '菜单新增', 102, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:add', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1014, '菜单修改', 102, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:edit', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1015, '菜单删除', 102, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:remove', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1016, '部门查询', 103, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1017, '部门新增', 103, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1018, '部门修改', 103, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1019, '部门删除', 103, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1020, '岗位查询', 104, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:query', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1021, '岗位新增', 104, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:add', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1022, '岗位修改', 104, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1023, '岗位删除', 104, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1024, '岗位导出', 104, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:export', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1025, '字典查询', 105, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:query', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1026, '字典新增', 105, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:add', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1027, '字典修改', 105, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:edit', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1028, '字典删除', 105, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:remove', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1029, '字典导出', 105, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:export', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1030, '参数查询', 106, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:query', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1031, '参数新增', 106, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:add', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1032, '参数修改', 106, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:edit', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1033, '参数删除', 106, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:remove', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1034, '参数导出', 106, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:export', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1035, '公告查询', 107, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:query', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1036, '公告新增', 107, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:add', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1037, '公告修改', 107, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:edit', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1038, '公告删除', 107, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:remove', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1039, '操作查询', 500, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1040, '操作删除', 500, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1041, '日志导出', 500, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1042, '登录查询', 501, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1043, '登录删除', 501, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1044, '日志导出', 501, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1045, '账户解锁', 501, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:unlock', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1046, '在线查询', 109, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:query', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1047, '批量强退', 109, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1048, '单条强退', 109, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1049, '任务查询', 110, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:query', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1050, '任务新增', 110, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:add', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1051, '任务修改', 110, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:edit', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1052, '任务删除', 110, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:remove', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1053, '状态修改', 110, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:changeStatus', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1054, '任务导出', 110, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:export', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1055, '生成查询', 116, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:query', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1056, '生成修改', 116, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:edit', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1057, '生成删除', 116, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:remove', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1058, '导入代码', 116, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:import', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1059, '预览代码', 116, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:preview', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1060, '生成代码', 116, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:code', '#', 'admin', '2025-06-12 14:49:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2006, '课程管理', 0, 2, 'lesson', 'business/lesson/index', NULL, '', 1, 0, 'C', '0', '0', 'business:lesson:list', 'button', 'admin', '2025-06-23 10:48:51', 'admin', '2025-08-19 07:37:41', '课程/主题菜单');
INSERT INTO `sys_menu` VALUES (2007, '课程/主题查询', 2006, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:lesson:query', '#', 'admin', '2025-06-23 10:48:51', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2008, '课程/主题新增', 2006, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:lesson:add', '#', 'admin', '2025-06-23 10:48:51', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2009, '课程/主题修改', 2006, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:lesson:edit', '#', 'admin', '2025-06-23 10:48:51', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2010, '课程/主题删除', 2006, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:lesson:remove', '#', 'admin', '2025-06-23 10:48:51', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2011, '课程/主题导出', 2006, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:lesson:export', '#', 'admin', '2025-06-23 10:48:51', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2018, '学生管理', 0, 1, 'studentguanli', 'business/student/index', NULL, '', 1, 0, 'C', '0', '0', 'business:student:list', 'user', 'admin', '2025-06-25 15:16:52', 'admin', '2025-06-25 15:17:59', '学生管理菜单');
INSERT INTO `sys_menu` VALUES (2019, '学生管理查询', 2018, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:student:query', '#', 'admin', '2025-06-25 15:16:52', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2020, '学生管理新增', 2018, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:student:add', '#', 'admin', '2025-06-25 15:16:52', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2021, '学生管理修改', 2018, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:student:edit', '#', 'admin', '2025-06-25 15:16:52', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2022, '学生管理删除', 2018, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:student:remove', '#', 'admin', '2025-06-25 15:16:52', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2023, '学生管理导出', 2018, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:student:export', '#', 'admin', '2025-06-25 15:16:52', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2024, '学生管理导入', 2018, 6, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'business:student:import', '#', '', NULL, '', NULL, '');
INSERT INTO `sys_menu` VALUES (2026, '课程/作业信息查询', 2025, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:lesson:query', '#', 'admin', '2025-08-14 09:38:18', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2027, '课程/作业信息新增', 2025, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:lesson:add', '#', 'admin', '2025-08-14 09:38:18', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2028, '课程/作业信息修改', 2025, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:lesson:edit', '#', 'admin', '2025-08-14 09:38:18', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2029, '课程/作业信息删除', 2025, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:lesson:remove', '#', 'admin', '2025-08-14 09:38:18', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2030, '课程/作业信息导出', 2025, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:lesson:export', '#', 'admin', '2025-08-14 09:38:18', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2031, '题库管理', 0, 1, 'question', 'business/question/index', NULL, '', 1, 0, 'C', '0', '0', 'business:question:list', '#', 'admin', '2025-08-18 20:03:35', '', NULL, '题库管理菜单');
INSERT INTO `sys_menu` VALUES (2032, '题库管理查询', 2031, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:question:query', '#', 'admin', '2025-08-18 20:03:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2033, '题库管理新增', 2031, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:question:add', '#', 'admin', '2025-08-18 20:03:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2034, '题库管理修改', 2031, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:question:edit', '#', 'admin', '2025-08-18 20:03:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2035, '题库管理删除', 2031, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:question:remove', '#', 'admin', '2025-08-18 20:03:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2036, '题库管理导出', 2031, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:question:export', '#', 'admin', '2025-08-18 20:03:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2037, '教师首页', 0, 0, 'teacher-dashboard', 'business/teacher/index', NULL, '', 1, 0, 'C', '0', '0', 'business:teacher:list', 'theme', 'admin', '2025-08-25 20:05:44', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2038, '班级管理', 0, 5, 'teacherClass', 'business/teacherClass/index', NULL, '', 1, 0, 'C', '0', '0', 'business:teacherClass:list', 'peoples', 'admin', '2025-12-29 14:22:28', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2039, '添加班级', 2038, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'business:teacherClass:add', '#', 'admin', '2025-12-29 14:22:28', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2040, '移除班级', 2038, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'business:teacherClass:remove', '#', 'admin', '2025-12-29 14:22:28', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2042, '成绩查询', 0, 5, 'score', 'business/score/index', NULL, '', 1, 0, 'C', '0', '0', 'business:score:list', 'job', '19157727791', '2026-01-08 09:44:00', '', NULL, '');

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `notice_id` int NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `notice_title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告标题',
  `notice_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告类型（1通知 2公告）',
  `notice_content` longblob NULL COMMENT '公告内容',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`notice_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '通知公告表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
INSERT INTO `sys_notice` VALUES (1, '温馨提醒：2018-07-01 若依新版本发布啦', '2', 0xE696B0E78988E69CACE58685E5AEB9, '0', 'admin', '2025-06-12 14:49:21', '', NULL, '管理员');
INSERT INTO `sys_notice` VALUES (2, '维护通知：2018-07-01 若依系统凌晨维护', '1', 0xE7BBB4E68AA4E58685E5AEB9, '0', 'admin', '2025-06-12 14:49:21', '', NULL, '管理员');

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log`  (
  `oper_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '模块标题',
  `business_type` int NULL DEFAULT 0 COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '请求方式',
  `operator_type` int NULL DEFAULT 0 COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
  `oper_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '操作人员',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '部门名称',
  `oper_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '返回参数',
  `status` int NULL DEFAULT 0 COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint NULL DEFAULT 0 COMMENT '消耗时间',
  PRIMARY KEY (`oper_id`) USING BTREE,
  INDEX `idx_sys_oper_log_bt`(`business_type` ASC) USING BTREE,
  INDEX `idx_sys_oper_log_s`(`status` ASC) USING BTREE,
  INDEX `idx_sys_oper_log_ot`(`oper_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 407 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '操作日志记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_oper_log
-- ----------------------------
INSERT INTO `sys_oper_log` VALUES (100, '代码生成', 6, 'com.ruoyi.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin', '研发部门', '/tool/gen/importTable', '127.0.0.1', '内网IP', '{\"tables\":\"biz_student\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-17 15:43:23', 173);
INSERT INTO `sys_oper_log` VALUES (101, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门', '/tool/gen', '127.0.0.1', '内网IP', '{\"businessName\":\"student\",\"className\":\"BizStudent\",\"columns\":[{\"capJavaField\":\"StudentId\",\"columnComment\":\"学生ID\",\"columnId\":1,\"columnName\":\"student_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-06-17 15:43:23\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":true,\"isIncrement\":\"1\",\"isInsert\":\"1\",\"isPk\":\"1\",\"isRequired\":\"0\",\"javaField\":\"studentId\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":1,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"UserId\",\"columnComment\":\"关联系统用户ID\",\"columnId\":2,\"columnName\":\"user_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-06-17 15:43:23\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"0\",\"javaField\":\"userId\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":false,\"sort\":2,\"superColumn\":false,\"tableId\":1,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"StudentNo\",\"columnComment\":\"学号\",\"columnId\":3,\"columnName\":\"student_no\",\"columnType\":\"varchar(30)\",\"createBy\":\"admin\",\"createTime\":\"2025-06-17 15:43:23\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"0\",\"javaField\":\"studentNo\",\"javaType\":\"String\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":false,\"sort\":3,\"superColumn\":false,\"tableId\":1,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"StudentName\",\"columnComment\":\"学生姓名\",\"columnId\":4,\"columnName\":\"student_name\",\"columnType\":\"varchar(30)\",\"createBy\":\"admin\",\"createTime\":\"2025-06-17 15:43:23\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isReq', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-17 15:46:41', 178);
INSERT INTO `sys_oper_log` VALUES (102, '代码生成', 8, 'com.ruoyi.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin', '研发部门', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{\"tables\":\"biz_student\"}', NULL, 0, NULL, '2025-06-17 15:46:53', 508);
INSERT INTO `sys_oper_log` VALUES (103, '角色管理', 2, 'com.ruoyi.web.controller.system.SysRoleController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/role', '127.0.0.1', '内网IP', '{\"admin\":true,\"createTime\":\"2025-06-12 14:49:21\",\"dataScope\":\"1\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[1,100,1000,1001,1002,1003,1004,1005,1006,101,1007,1008,1009,1010,1011,102,1012,1013,1014,1015,103,1016,1017,1018,1019,104,1020,1021,1022,1023,1024,105,1025,1026,1027,1028,1029,106,1030,1031,1032,1033,1034,107,1035,1036,1037,1038,108,500,1039,1040,1041,501,1042,1043,1044,1045,2,109,1046,1047,1048,110,1049,1050,1051,1052,1053,1054,111,112,113,114,3,115,2000,2001,2002,2003,2004,2005,116,1055,1056,1057,1058,1059,1060,117,4],\"params\":{},\"remark\":\"超级管理员\",\"roleId\":1,\"roleKey\":\"admin\",\"roleName\":\"超级管理员\",\"roleSort\":1,\"status\":\"0\"}', NULL, 1, '不允许操作超级管理员角色', '2025-06-17 18:36:12', 15);
INSERT INTO `sys_oper_log` VALUES (104, '角色管理', 2, 'com.ruoyi.web.controller.system.SysRoleController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/role', '127.0.0.1', '内网IP', '{\"admin\":false,\"createTime\":\"2025-06-12 14:49:21\",\"dataScope\":\"2\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[1,100,1000,1001,1002,1003,1004,1005,1006,101,1007,1008,1009,1010,1011,102,1012,1013,1014,1015,103,1016,1017,1018,1019,104,1020,1021,1022,1023,1024,105,1025,1026,1027,1028,1029,106,1030,1031,1032,1033,1034,107,1035,1036,1037,1038,108,500,1039,1040,1041,501,1042,1043,1044,1045,2,109,1046,1047,1048,110,1049,1050,1051,1052,1053,1054,111,112,113,114,3,115,2000,2001,2002,2003,2004,2005,116,1055,1056,1057,1058,1059,1060,117,4],\"params\":{},\"remark\":\"普通角色\",\"roleId\":2,\"roleKey\":\"common\",\"roleName\":\"普通角色\",\"roleSort\":2,\"status\":\"0\",\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-17 18:36:30', 83);
INSERT INTO `sys_oper_log` VALUES (105, '角色管理', 1, 'com.ruoyi.web.controller.system.SysRoleController.add()', 'POST', 1, 'admin', '研发部门', '/system/role', '127.0.0.1', '内网IP', '{\"admin\":false,\"createBy\":\"admin\",\"deptCheckStrictly\":true,\"deptIds\":[],\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[1,100,1000,1001,1002,1003,1004,1005,1006,101,1007,1008,1009,1010,1011,102,1012,1013,1014,1015,103,1016,1017,1018,1019,104,1020,1021,1022,1023,1024,105,1025,1026,1027,1028,1029,106,1030,1031,1032,1033,1034,107,1035,1036,1037,1038,108,500,1039,1040,1041,501,1042,1043,1044,1045,2,109,1046,1047,1048,110,1049,1050,1051,1052,1053,1054,111,112,113,114,3,115,2000,2001,2002,2003,2004,2005,116,1055,1056,1057,1058,1059,1060,117,4],\"params\":{},\"roleId\":100,\"roleKey\":\"teacher\",\"roleName\":\"教师\",\"roleSort\":0,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-17 18:39:24', 54);
INSERT INTO `sys_oper_log` VALUES (106, '用户管理', 1, 'com.ruoyi.web.controller.system.SysUserController.add()', 'POST', 1, 'admin', '研发部门', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"createBy\":\"admin\",\"nickName\":\"zdx\",\"params\":{},\"postIds\":[2],\"roleIds\":[100],\"status\":\"0\",\"userId\":100,\"userName\":\"t01\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-17 18:40:07', 111);
INSERT INTO `sys_oper_log` VALUES (107, '学生信息', 1, 'com.ruoyi.business.controller.BizStudentController.add()', 'POST', 1, 't01', NULL, '/business/student', '127.0.0.1', '内网IP', '{\"className\":\"1\",\"createTime\":\"2025-06-19 08:50:04\",\"gender\":\"男\",\"grade\":7,\"params\":{},\"schoolName\":\"大目湾\",\"studentId\":1,\"studentName\":\"朱毅\",\"studentNo\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 08:50:05', 220);
INSERT INTO `sys_oper_log` VALUES (108, '部门管理', 2, 'com.ruoyi.web.controller.system.SysDeptController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0\",\"children\":[],\"deptId\":100,\"deptName\":\"象山县\",\"email\":\"1733472703@qq.com\",\"leader\":\"郑东旭\",\"orderNum\":0,\"params\":{},\"parentId\":0,\"phone\":\"19157727791\",\"status\":\"0\",\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:24:53', 27);
INSERT INTO `sys_oper_log` VALUES (109, '部门管理', 2, 'com.ruoyi.web.controller.system.SysDeptController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100\",\"children\":[],\"deptId\":101,\"deptName\":\"小学\",\"email\":\"ry@qq.com\",\"leader\":\"小学负责人\",\"orderNum\":1,\"params\":{},\"parentId\":100,\"parentName\":\"象山县\",\"phone\":\"15888888888\",\"status\":\"0\",\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:26:10', 45);
INSERT INTO `sys_oper_log` VALUES (110, '部门管理', 2, 'com.ruoyi.web.controller.system.SysDeptController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100\",\"children\":[],\"deptId\":102,\"deptName\":\"初中\",\"email\":\"ry@qq.com\",\"leader\":\"初中负责人\",\"orderNum\":2,\"params\":{},\"parentId\":100,\"parentName\":\"象山县\",\"phone\":\"15888888888\",\"status\":\"0\",\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:26:28', 40);
INSERT INTO `sys_oper_log` VALUES (111, '部门管理', 1, 'com.ruoyi.web.controller.system.SysDeptController.add()', 'POST', 1, 'admin', '研发部门', '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100\",\"children\":[],\"createBy\":\"admin\",\"deptName\":\"高中\",\"orderNum\":3,\"params\":{},\"parentId\":100,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:26:56', 19);
INSERT INTO `sys_oper_log` VALUES (112, '部门管理', 3, 'com.ruoyi.web.controller.system.SysDeptController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/dept/109', '127.0.0.1', '内网IP', '109', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:27:04', 21);
INSERT INTO `sys_oper_log` VALUES (113, '部门管理', 2, 'com.ruoyi.web.controller.system.SysDeptController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100,102\",\"children\":[],\"deptId\":108,\"deptName\":\"大目湾实验学校\",\"email\":\"ry@qq.com\",\"leader\":\"若依\",\"orderNum\":1,\"params\":{},\"parentId\":102,\"parentName\":\"初中\",\"phone\":\"15888888888\",\"status\":\"0\",\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:27:45', 25);
INSERT INTO `sys_oper_log` VALUES (114, '部门管理', 1, 'com.ruoyi.web.controller.system.SysDeptController.add()', 'POST', 1, 'admin', '研发部门', '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100\",\"children\":[],\"createBy\":\"admin\",\"deptName\":\"九年制学区\",\"leader\":\"九年制负责人\",\"orderNum\":4,\"params\":{},\"parentId\":100,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:35:30', 15);
INSERT INTO `sys_oper_log` VALUES (115, '部门管理', 2, 'com.ruoyi.web.controller.system.SysDeptController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100,102\",\"children\":[],\"deptId\":108,\"deptName\":\"塔山中学\",\"email\":\"ry@qq.com\",\"leader\":\"若依\",\"orderNum\":1,\"params\":{},\"parentId\":102,\"parentName\":\"初中\",\"phone\":\"15888888888\",\"status\":\"0\",\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:35:44', 23);
INSERT INTO `sys_oper_log` VALUES (116, '部门管理', 3, 'com.ruoyi.web.controller.system.SysDeptController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/dept/103', '127.0.0.1', '内网IP', '103', '{\"msg\":\"部门存在用户,不允许删除\",\"code\":601}', 0, NULL, '2025-06-19 09:35:47', 6);
INSERT INTO `sys_oper_log` VALUES (117, '部门管理', 3, 'com.ruoyi.web.controller.system.SysDeptController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/dept/104', '127.0.0.1', '内网IP', '104', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:35:55', 14);
INSERT INTO `sys_oper_log` VALUES (118, '部门管理', 3, 'com.ruoyi.web.controller.system.SysDeptController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/dept/105', '127.0.0.1', '内网IP', '105', '{\"msg\":\"部门存在用户,不允许删除\",\"code\":601}', 0, NULL, '2025-06-19 09:35:57', 6);
INSERT INTO `sys_oper_log` VALUES (119, '部门管理', 3, 'com.ruoyi.web.controller.system.SysDeptController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/dept/107', '127.0.0.1', '内网IP', '107', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:36:01', 14);
INSERT INTO `sys_oper_log` VALUES (120, '部门管理', 3, 'com.ruoyi.web.controller.system.SysDeptController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/dept/106', '127.0.0.1', '内网IP', '106', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:36:04', 14);
INSERT INTO `sys_oper_log` VALUES (121, '部门管理', 2, 'com.ruoyi.web.controller.system.SysDeptController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100,101\",\"children\":[],\"deptId\":103,\"deptName\":\"六小\",\"email\":\"ry@qq.com\",\"leader\":\"若依\",\"orderNum\":1,\"params\":{},\"parentId\":101,\"parentName\":\"小学\",\"phone\":\"15888888888\",\"status\":\"0\",\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:36:15', 23);
INSERT INTO `sys_oper_log` VALUES (122, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":true,\"avatar\":\"\",\"createBy\":\"admin\",\"createTime\":\"2025-06-12 14:49:21\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0,100,101\",\"children\":[],\"deptId\":103,\"deptName\":\"六小\",\"leader\":\"若依\",\"orderNum\":1,\"params\":{},\"parentId\":101,\"status\":\"0\"},\"deptId\":100,\"email\":\"ry@163.com\",\"loginDate\":\"2025-06-19 09:24:01\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"若依\",\"params\":{},\"phonenumber\":\"15888888888\",\"postIds\":[1],\"pwdUpdateDate\":\"2025-06-12 14:49:21\",\"remark\":\"管理员\",\"roleIds\":[1],\"roles\":[{\"admin\":true,\"dataScope\":\"1\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":1,\"roleKey\":\"admin\",\"roleName\":\"超级管理员\",\"roleSort\":1,\"status\":\"0\"}],\"sex\":\"1\",\"status\":\"0\",\"userId\":1,\"userName\":\"admin\"}', NULL, 1, '不允许操作超级管理员用户', '2025-06-19 09:36:36', 16);
INSERT INTO `sys_oper_log` VALUES (123, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":true,\"avatar\":\"\",\"createBy\":\"admin\",\"createTime\":\"2025-06-12 14:49:21\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0,100,101\",\"children\":[],\"deptId\":103,\"deptName\":\"六小\",\"leader\":\"若依\",\"orderNum\":1,\"params\":{},\"parentId\":101,\"status\":\"0\"},\"deptId\":101,\"email\":\"ry@163.com\",\"loginDate\":\"2025-06-19 09:24:01\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"若依\",\"params\":{},\"phonenumber\":\"15888888888\",\"postIds\":[1],\"pwdUpdateDate\":\"2025-06-12 14:49:21\",\"remark\":\"管理员\",\"roleIds\":[1],\"roles\":[{\"admin\":true,\"dataScope\":\"1\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":1,\"roleKey\":\"admin\",\"roleName\":\"超级管理员\",\"roleSort\":1,\"status\":\"0\"}],\"sex\":\"1\",\"status\":\"0\",\"userId\":1,\"userName\":\"admin\"}', NULL, 1, '不允许操作超级管理员用户', '2025-06-19 09:36:45', 0);
INSERT INTO `sys_oper_log` VALUES (124, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":true,\"avatar\":\"\",\"createBy\":\"admin\",\"createTime\":\"2025-06-12 14:49:21\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0,100,101\",\"children\":[],\"deptId\":103,\"deptName\":\"六小\",\"leader\":\"若依\",\"orderNum\":1,\"params\":{},\"parentId\":101,\"status\":\"0\"},\"deptId\":105,\"email\":\"ry@163.com\",\"loginDate\":\"2025-06-19 09:24:01\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"若依\",\"params\":{},\"phonenumber\":\"15888888888\",\"postIds\":[1],\"pwdUpdateDate\":\"2025-06-12 14:49:21\",\"remark\":\"管理员\",\"roleIds\":[1],\"roles\":[{\"admin\":true,\"dataScope\":\"1\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":1,\"roleKey\":\"admin\",\"roleName\":\"超级管理员\",\"roleSort\":1,\"status\":\"0\"}],\"sex\":\"1\",\"status\":\"0\",\"userId\":1,\"userName\":\"admin\"}', NULL, 1, '不允许操作超级管理员用户', '2025-06-19 09:36:51', 1);
INSERT INTO `sys_oper_log` VALUES (125, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"avatar\":\"\",\"createBy\":\"admin\",\"createTime\":\"2025-06-12 14:49:21\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0,100,101\",\"children\":[],\"deptId\":105,\"deptName\":\"测试部门\",\"leader\":\"若依\",\"orderNum\":3,\"params\":{},\"parentId\":101,\"status\":\"0\"},\"deptId\":108,\"email\":\"ry@qq.com\",\"loginDate\":\"2025-06-12 14:49:21\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"若依\",\"params\":{},\"phonenumber\":\"15666666666\",\"postIds\":[2],\"pwdUpdateDate\":\"2025-06-12 14:49:21\",\"remark\":\"测试员\",\"roleIds\":[2],\"roles\":[{\"admin\":false,\"dataScope\":\"2\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":2,\"roleKey\":\"common\",\"roleName\":\"普通角色\",\"roleSort\":2,\"status\":\"0\"}],\"sex\":\"1\",\"status\":\"0\",\"updateBy\":\"admin\",\"userId\":2,\"userName\":\"ry\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:38:06', 54);
INSERT INTO `sys_oper_log` VALUES (126, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"avatar\":\"\",\"createBy\":\"admin\",\"createTime\":\"2025-06-12 14:49:21\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0,100,102\",\"children\":[],\"deptId\":108,\"deptName\":\"塔山中学\",\"leader\":\"若依\",\"orderNum\":1,\"params\":{},\"parentId\":102,\"status\":\"0\"},\"deptId\":100,\"email\":\"ry@qq.com\",\"loginDate\":\"2025-06-12 14:49:21\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"若依\",\"params\":{},\"phonenumber\":\"15666666666\",\"postIds\":[2],\"pwdUpdateDate\":\"2025-06-12 14:49:21\",\"remark\":\"测试员\",\"roleIds\":[2],\"roles\":[{\"admin\":false,\"dataScope\":\"2\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":2,\"roleKey\":\"common\",\"roleName\":\"普通角色\",\"roleSort\":2,\"status\":\"0\"}],\"sex\":\"1\",\"status\":\"0\",\"updateBy\":\"admin\",\"userId\":2,\"userName\":\"ry\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:38:32', 30);
INSERT INTO `sys_oper_log` VALUES (127, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"avatar\":\"\",\"createBy\":\"admin\",\"createTime\":\"2025-06-12 14:49:21\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0\",\"children\":[],\"deptId\":100,\"deptName\":\"象山县\",\"leader\":\"郑东旭\",\"orderNum\":0,\"params\":{},\"parentId\":0,\"status\":\"0\"},\"deptId\":108,\"email\":\"ry@qq.com\",\"loginDate\":\"2025-06-12 14:49:21\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"朱毅\",\"params\":{},\"phonenumber\":\"15666666666\",\"postIds\":[2],\"pwdUpdateDate\":\"2025-06-12 14:49:21\",\"remark\":\"测试员\",\"roleIds\":[2],\"roles\":[{\"admin\":false,\"dataScope\":\"2\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":2,\"roleKey\":\"common\",\"roleName\":\"普通角色\",\"roleSort\":2,\"status\":\"0\"}],\"sex\":\"1\",\"status\":\"0\",\"updateBy\":\"admin\",\"userId\":2,\"userName\":\"ry\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:38:43', 30);
INSERT INTO `sys_oper_log` VALUES (128, '部门管理', 2, 'com.ruoyi.web.controller.system.SysDeptController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100\",\"children\":[],\"deptId\":103,\"deptName\":\"六小\",\"email\":\"ry@qq.com\",\"leader\":\"若依\",\"orderNum\":1,\"params\":{},\"parentId\":100,\"parentName\":\"小学\",\"phone\":\"15888888888\",\"status\":\"0\",\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:41:17', 21);
INSERT INTO `sys_oper_log` VALUES (129, '部门管理', 2, 'com.ruoyi.web.controller.system.SysDeptController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100\",\"children\":[],\"deptId\":103,\"deptName\":\"管理员\",\"email\":\"ry@qq.com\",\"leader\":\"若依\",\"orderNum\":1,\"params\":{},\"parentId\":100,\"parentName\":\"象山县\",\"phone\":\"15888888888\",\"status\":\"0\",\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:41:42', 26);
INSERT INTO `sys_oper_log` VALUES (130, '部门管理', 2, 'com.ruoyi.web.controller.system.SysDeptController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100\",\"children\":[],\"deptId\":101,\"deptName\":\"小学\",\"email\":\"ry@qq.com\",\"leader\":\"小学负责人\",\"orderNum\":2,\"params\":{},\"parentId\":100,\"parentName\":\"象山县\",\"phone\":\"15888888888\",\"status\":\"0\",\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:41:53', 26);
INSERT INTO `sys_oper_log` VALUES (131, '部门管理', 2, 'com.ruoyi.web.controller.system.SysDeptController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100,101\",\"children\":[],\"deptId\":105,\"deptName\":\"六小\",\"email\":\"ry@qq.com\",\"leader\":\"若依\",\"orderNum\":3,\"params\":{},\"parentId\":101,\"parentName\":\"小学\",\"phone\":\"15888888888\",\"status\":\"0\",\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:42:20', 25);
INSERT INTO `sys_oper_log` VALUES (132, '部门管理', 1, 'com.ruoyi.web.controller.system.SysDeptController.add()', 'POST', 1, 'admin', '研发部门', '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100,201\",\"children\":[],\"createBy\":\"admin\",\"deptName\":\"大目湾实验学校\",\"orderNum\":0,\"params\":{},\"parentId\":201,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:42:41', 19);
INSERT INTO `sys_oper_log` VALUES (133, '用户管理', 4, 'com.ruoyi.web.controller.system.SysUserController.insertAuthRole()', 'PUT', 1, 'admin', '研发部门', '/system/user/authRole', '127.0.0.1', '内网IP', '{\"roleIds\":\"100\",\"userId\":\"2\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:44:42', 11);
INSERT INTO `sys_oper_log` VALUES (134, '角色管理', 3, 'com.ruoyi.web.controller.system.SysRoleController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/role/2', '127.0.0.1', '内网IP', '[2]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:45:18', 30);
INSERT INTO `sys_oper_log` VALUES (135, '角色管理', 1, 'com.ruoyi.web.controller.system.SysRoleController.add()', 'POST', 1, 'admin', '研发部门', '/system/role', '127.0.0.1', '内网IP', '{\"admin\":false,\"createBy\":\"admin\",\"deptCheckStrictly\":true,\"deptIds\":[],\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[4],\"params\":{},\"roleId\":101,\"roleKey\":\"student\",\"roleName\":\"学生\",\"roleSort\":2,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:45:57', 22);
INSERT INTO `sys_oper_log` VALUES (136, '用户管理', 1, 'com.ruoyi.web.controller.system.SysUserController.add()', 'POST', 1, 'admin', '研发部门', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"createBy\":\"admin\",\"deptId\":202,\"nickName\":\"郑东旭\",\"params\":{},\"phonenumber\":\"13105527781\",\"postIds\":[],\"roleIds\":[100],\"sex\":\"0\",\"status\":\"0\",\"userId\":101,\"userName\":\"19157727791\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:48:45', 85);
INSERT INTO `sys_oper_log` VALUES (137, '用户管理', 1, 'com.ruoyi.web.controller.system.SysUserController.add()', 'POST', 1, '19157727791', '大目湾实验学校', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"createBy\":\"19157727791\",\"deptId\":108,\"nickName\":\"朱屹\",\"params\":{},\"phonenumber\":\"13968385077\",\"postIds\":[],\"roleIds\":[100],\"status\":\"0\",\"userId\":102,\"userName\":\"13968385077\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:51:35', 90);
INSERT INTO `sys_oper_log` VALUES (138, '用户管理', 3, 'com.ruoyi.web.controller.system.SysUserController.remove()', 'DELETE', 1, '19157727791', '大目湾实验学校', '/system/user/2', '127.0.0.1', '内网IP', '[2]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:51:40', 19);
INSERT INTO `sys_oper_log` VALUES (139, '部门管理', 1, 'com.ruoyi.web.controller.system.SysDeptController.add()', 'POST', 1, '19157727791', '大目湾实验学校', '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100,101\",\"children\":[],\"createBy\":\"19157727791\",\"deptName\":\"小学东区\",\"orderNum\":0,\"params\":{},\"parentId\":101,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:53:21', 14);
INSERT INTO `sys_oper_log` VALUES (140, '部门管理', 1, 'com.ruoyi.web.controller.system.SysDeptController.add()', 'POST', 1, '19157727791', '大目湾实验学校', '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100,101\",\"children\":[],\"createBy\":\"19157727791\",\"deptName\":\"小学南区\",\"orderNum\":0,\"params\":{},\"parentId\":101,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:53:31', 13);
INSERT INTO `sys_oper_log` VALUES (141, '部门管理', 1, 'com.ruoyi.web.controller.system.SysDeptController.add()', 'POST', 1, '19157727791', '大目湾实验学校', '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100,101\",\"children\":[],\"createBy\":\"19157727791\",\"deptName\":\"小学西区\",\"orderNum\":0,\"params\":{},\"parentId\":101,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:53:38', 23);
INSERT INTO `sys_oper_log` VALUES (142, '部门管理', 1, 'com.ruoyi.web.controller.system.SysDeptController.add()', 'POST', 1, '19157727791', '大目湾实验学校', '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100,101\",\"children\":[],\"createBy\":\"19157727791\",\"deptName\":\"小学北区\",\"orderNum\":0,\"params\":{},\"parentId\":101,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:53:44', 12);
INSERT INTO `sys_oper_log` VALUES (143, '部门管理', 2, 'com.ruoyi.web.controller.system.SysDeptController.edit()', 'PUT', 1, '19157727791', '大目湾实验学校', '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100,101,205\",\"children\":[],\"deptId\":105,\"deptName\":\"六小\",\"email\":\"ry@qq.com\",\"leader\":\"若依\",\"orderNum\":3,\"params\":{},\"parentId\":205,\"parentName\":\"小学\",\"phone\":\"15888888888\",\"status\":\"0\",\"updateBy\":\"19157727791\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:53:58', 22);
INSERT INTO `sys_oper_log` VALUES (144, '部门管理', 1, 'com.ruoyi.web.controller.system.SysDeptController.add()', 'POST', 1, '19157727791', '大目湾实验学校', '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100,102\",\"children\":[],\"createBy\":\"19157727791\",\"deptName\":\"初中东区\",\"orderNum\":0,\"params\":{},\"parentId\":102,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:54:10', 16);
INSERT INTO `sys_oper_log` VALUES (145, '部门管理', 1, 'com.ruoyi.web.controller.system.SysDeptController.add()', 'POST', 1, '19157727791', '大目湾实验学校', '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100,102\",\"children\":[],\"createBy\":\"19157727791\",\"deptName\":\"初中南区\",\"orderNum\":0,\"params\":{},\"parentId\":102,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:54:17', 14);
INSERT INTO `sys_oper_log` VALUES (146, '部门管理', 1, 'com.ruoyi.web.controller.system.SysDeptController.add()', 'POST', 1, '19157727791', '大目湾实验学校', '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100,102\",\"children\":[],\"createBy\":\"19157727791\",\"deptName\":\"初中西区\",\"orderNum\":0,\"params\":{},\"parentId\":102,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:54:23', 12);
INSERT INTO `sys_oper_log` VALUES (147, '部门管理', 1, 'com.ruoyi.web.controller.system.SysDeptController.add()', 'POST', 1, '19157727791', '大目湾实验学校', '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100,102,207\",\"children\":[],\"createBy\":\"19157727791\",\"deptName\":\"初中北区\",\"orderNum\":0,\"params\":{},\"parentId\":207,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:54:31', 13);
INSERT INTO `sys_oper_log` VALUES (148, '部门管理', 3, 'com.ruoyi.web.controller.system.SysDeptController.remove()', 'DELETE', 1, '19157727791', '大目湾实验学校', '/system/dept/210', '127.0.0.1', '内网IP', '210', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:54:37', 16);
INSERT INTO `sys_oper_log` VALUES (149, '部门管理', 1, 'com.ruoyi.web.controller.system.SysDeptController.add()', 'POST', 1, '19157727791', '大目湾实验学校', '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100,102\",\"children\":[],\"createBy\":\"19157727791\",\"deptName\":\"初中北区\",\"orderNum\":0,\"params\":{},\"parentId\":102,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:54:46', 9);
INSERT INTO `sys_oper_log` VALUES (150, '部门管理', 2, 'com.ruoyi.web.controller.system.SysDeptController.edit()', 'PUT', 1, '19157727791', '大目湾实验学校', '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100,102,207\",\"children\":[],\"deptId\":108,\"deptName\":\"塔山中学\",\"email\":\"ry@qq.com\",\"leader\":\"若依\",\"orderNum\":1,\"params\":{},\"parentId\":207,\"parentName\":\"初中\",\"phone\":\"15888888888\",\"status\":\"0\",\"updateBy\":\"19157727791\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:55:02', 51);
INSERT INTO `sys_oper_log` VALUES (151, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, '19157727791', '大目湾实验学校', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"component\":\"business/student/index\",\"createTime\":\"2025-06-17 16:56:13\",\"icon\":\"chart\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2000,\"menuName\":\"学生信息\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":0,\"path\":\"student\",\"perms\":\"business:student:list\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"19157727791\",\"visible\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 09:58:03', 21);
INSERT INTO `sys_oper_log` VALUES (152, '岗位管理', 2, 'com.ruoyi.web.controller.system.SysPostController.edit()', 'PUT', 1, '19157727791', '大目湾实验学校', '/system/post', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"createTime\":\"2025-06-12 14:49:21\",\"flag\":false,\"params\":{},\"postCode\":\"ceo\",\"postId\":1,\"postName\":\"教研员\",\"postSort\":1,\"remark\":\"\",\"status\":\"0\",\"updateBy\":\"19157727791\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 10:02:47', 17);
INSERT INTO `sys_oper_log` VALUES (153, '岗位管理', 2, 'com.ruoyi.web.controller.system.SysPostController.edit()', 'PUT', 1, '19157727791', '大目湾实验学校', '/system/post', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"createTime\":\"2025-06-12 14:49:21\",\"flag\":false,\"params\":{},\"postCode\":\"se\",\"postId\":2,\"postName\":\"分区组长\",\"postSort\":2,\"remark\":\"\",\"status\":\"0\",\"updateBy\":\"19157727791\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 10:02:59', 8);
INSERT INTO `sys_oper_log` VALUES (154, '岗位管理', 3, 'com.ruoyi.web.controller.system.SysPostController.remove()', 'DELETE', 1, '19157727791', '大目湾实验学校', '/system/post/3', '127.0.0.1', '内网IP', '[3]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 10:03:22', 11);
INSERT INTO `sys_oper_log` VALUES (155, '岗位管理', 2, 'com.ruoyi.web.controller.system.SysPostController.edit()', 'PUT', 1, '19157727791', '大目湾实验学校', '/system/post', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"createTime\":\"2025-06-12 14:49:21\",\"flag\":false,\"params\":{},\"postCode\":\"user\",\"postId\":4,\"postName\":\"普通教师\",\"postSort\":4,\"remark\":\"\",\"status\":\"0\",\"updateBy\":\"19157727791\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 10:03:30', 9);
INSERT INTO `sys_oper_log` VALUES (156, '用户管理', 1, 'com.ruoyi.web.controller.system.SysUserController.add()', 'POST', 1, '19157727791', '大目湾实验学校', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"createBy\":\"19157727791\",\"deptId\":202,\"nickName\":\"张三\",\"params\":{},\"postIds\":[],\"roleIds\":[101],\"sex\":\"0\",\"status\":\"0\",\"userId\":103,\"userName\":\"2022710488\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 14:26:23', 93);
INSERT INTO `sys_oper_log` VALUES (157, '部门管理', 3, 'com.ruoyi.web.controller.system.SysDeptController.remove()', 'DELETE', 1, 'admin', '管理员', '/system/dept/201', '127.0.0.1', '内网IP', '201', '{\"msg\":\"存在下级部门,不允许删除\",\"code\":601}', 0, NULL, '2025-06-19 14:42:04', 4);
INSERT INTO `sys_oper_log` VALUES (158, '部门管理', 3, 'com.ruoyi.web.controller.system.SysDeptController.remove()', 'DELETE', 1, 'admin', '管理员', '/system/dept/202', '127.0.0.1', '内网IP', '202', '{\"msg\":\"部门存在用户,不允许删除\",\"code\":601}', 0, NULL, '2025-06-19 14:42:08', 4);
INSERT INTO `sys_oper_log` VALUES (159, '部门管理', 2, 'com.ruoyi.web.controller.system.SysDeptController.edit()', 'PUT', 1, 'admin', '管理员', '/system/dept', '127.0.0.1', '内网IP', '{\"ancestors\":\"0,100,102,207\",\"children\":[],\"deptId\":202,\"deptName\":\"大目湾实验学校初中\",\"orderNum\":0,\"params\":{},\"parentId\":207,\"parentName\":\"九年制学区\",\"status\":\"0\",\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 14:42:28', 32);
INSERT INTO `sys_oper_log` VALUES (160, '部门管理', 3, 'com.ruoyi.web.controller.system.SysDeptController.remove()', 'DELETE', 1, 'admin', '管理员', '/system/dept/201', '127.0.0.1', '内网IP', '201', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-19 14:42:32', 12);
INSERT INTO `sys_oper_log` VALUES (161, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '管理员', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"component\":\"business/student/index\",\"createTime\":\"2025-06-17 16:56:13\",\"icon\":\"chart\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2000,\"menuName\":\"学生信息\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":0,\"path\":\"student-info\",\"perms\":\"business:student:list\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-23 10:22:48', 48);
INSERT INTO `sys_oper_log` VALUES (162, '代码生成', 6, 'com.ruoyi.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin', '管理员', '/tool/gen/importTable', '127.0.0.1', '内网IP', '{\"tables\":\"biz_lesson\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-23 10:29:19', 163);
INSERT INTO `sys_oper_log` VALUES (163, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '管理员', '/tool/gen', '127.0.0.1', '内网IP', '{\"businessName\":\"lesson\",\"className\":\"BizLesson\",\"columns\":[{\"capJavaField\":\"LessonId\",\"columnComment\":\"课程ID\",\"columnId\":15,\"columnName\":\"lesson_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-06-23 10:29:19\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":true,\"isIncrement\":\"1\",\"isInsert\":\"1\",\"isPk\":\"1\",\"isRequired\":\"0\",\"javaField\":\"lessonId\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":2,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"LessonName\",\"columnComment\":\"课程/主题名称\",\"columnId\":16,\"columnName\":\"lesson_name\",\"columnType\":\"varchar(100)\",\"createBy\":\"admin\",\"createTime\":\"2025-06-23 10:29:19\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"lessonName\",\"javaType\":\"String\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"LIKE\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":2,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"LessonType\",\"columnComment\":\"类型 (daily日常, exam考试, regional区域检测)\",\"columnId\":17,\"columnName\":\"lesson_type\",\"columnType\":\"varchar(20)\",\"createBy\":\"admin\",\"createTime\":\"2025-06-23 10:29:19\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"select\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"0\",\"javaField\":\"lessonType\",\"javaType\":\"String\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":false,\"sort\":3,\"superColumn\":false,\"tableId\":2,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"SchoolId\",\"columnComment\":\"所属学校ID\",\"columnId\":18,\"columnName\":\"school_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-06-23 10:29:19\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-23 10:43:37', 95);
INSERT INTO `sys_oper_log` VALUES (164, '代码生成', 8, 'com.ruoyi.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin', '管理员', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{\"tables\":\"biz_lesson\"}', NULL, 0, NULL, '2025-06-23 10:43:51', 551);
INSERT INTO `sys_oper_log` VALUES (165, '课程/主题', 1, 'com.ruoyi.business.controller.BizLessonController.add()', 'POST', 1, 'admin', '管理员', '/business/lesson', '127.0.0.1', '内网IP', '{\"classIds\":\"4\",\"createTime\":\"2025-06-23 10:53:59\",\"creatorId\":2,\"gradeLevel\":2,\"lessonId\":1,\"lessonName\":\"第一课\",\"params\":{},\"schoolId\":72}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-23 10:53:59', 175);
INSERT INTO `sys_oper_log` VALUES (166, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '管理员', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"component\":\"business/lesson/index\",\"createTime\":\"2025-06-23 10:48:51\",\"icon\":\"button\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2006,\"menuName\":\"课程/主题\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":3,\"path\":\"lesson\",\"perms\":\"business:lesson:list\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-24 09:46:36', 49);
INSERT INTO `sys_oper_log` VALUES (167, '角色管理', 2, 'com.ruoyi.web.controller.system.SysRoleController.edit()', 'PUT', 1, 'admin', '管理员', '/system/role', '127.0.0.1', '内网IP', '{\"admin\":false,\"createTime\":\"2025-06-17 18:39:24\",\"dataScope\":\"1\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[1,100,1000,1001,1002,1003,1004,1005,1006,101,1007,1008,1009,1010,1011,102,1012,1013,1014,1015,103,1016,1017,1018,1019,104,1020,1021,1022,1023,1024,105,1025,1026,1027,1028,1029,106,1030,1031,1032,1033,1034,107,1035,1036,1037,1038,108,500,1039,1040,1041,501,1042,1043,1044,1045,2000,2001,2002,2003,2004,2005,2,109,1046,1047,1048,110,1049,1050,1051,1052,1053,1054,111,112,113,114,3,115,2006,2007,2008,2009,2010,2011,116,1055,1056,1057,1058,1059,1060,117,4],\"params\":{},\"roleId\":100,\"roleKey\":\"teacher\",\"roleName\":\"教师\",\"roleSort\":0,\"status\":\"0\",\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-24 09:47:10', 71);
INSERT INTO `sys_oper_log` VALUES (168, '课程/主题', 1, 'com.ruoyi.business.controller.BizLessonController.add()', 'POST', 1, '19157727791', '大目湾实验学校初中', '/business/lesson', '127.0.0.1', '内网IP', '{\"classIds\":\"4\",\"createTime\":\"2025-06-24 09:48:19\",\"gradeLevel\":4,\"lessonId\":2,\"lessonName\":\"44\",\"params\":{},\"schoolId\":0}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-24 09:48:19', 18);
INSERT INTO `sys_oper_log` VALUES (169, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, '19157727791', '大目湾实验学校初中', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"avatar\":\"\",\"createBy\":\"admin\",\"createTime\":\"2025-06-19 09:48:45\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0,100,102,207\",\"children\":[],\"deptId\":202,\"deptName\":\"大目湾实验学校初中\",\"orderNum\":0,\"params\":{},\"parentId\":207,\"status\":\"0\"},\"deptId\":202,\"email\":\"\",\"loginDate\":\"2025-06-24 09:47:55\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"郑东旭\",\"params\":{},\"phonenumber\":\"13105527781\",\"postIds\":[],\"roleIds\":[100],\"roles\":[{\"admin\":false,\"dataScope\":\"1\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":100,\"roleKey\":\"teacher\",\"roleName\":\"教师\",\"roleSort\":0,\"status\":\"0\"}],\"sex\":\"0\",\"status\":\"0\",\"updateBy\":\"19157727791\",\"userId\":101,\"userName\":\"19157727791\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-24 10:09:24', 51);
INSERT INTO `sys_oper_log` VALUES (170, '代码生成', 6, 'com.ruoyi.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin', '管理员', '/tool/gen/importTable', '127.0.0.1', '内网IP', '{\"tables\":\"biz_school\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-24 10:40:46', 71);
INSERT INTO `sys_oper_log` VALUES (171, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '管理员', '/tool/gen', '127.0.0.1', '内网IP', '{\"businessName\":\"school\",\"className\":\"BizSchool\",\"columns\":[{\"capJavaField\":\"SchoolId\",\"columnComment\":\"学校ID\",\"columnId\":24,\"columnName\":\"school_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-06-24 10:40:46\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isPk\":\"1\",\"isRequired\":\"0\",\"javaField\":\"schoolId\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":3,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"SchoolName\",\"columnComment\":\"学校完整名称\",\"columnId\":25,\"columnName\":\"school_name\",\"columnType\":\"varchar(100)\",\"createBy\":\"admin\",\"createTime\":\"2025-06-24 10:40:46\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"schoolName\",\"javaType\":\"String\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"LIKE\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":3,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"SchoolType\",\"columnComment\":\"学校类型 (1小学 2初中 3高中)\",\"columnId\":26,\"columnName\":\"school_type\",\"columnType\":\"char(1)\",\"createBy\":\"admin\",\"createTime\":\"2025-06-24 10:40:46\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"select\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"schoolType\",\"javaType\":\"String\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":3,\"superColumn\":false,\"tableId\":3,\"updateBy\":\"\",\"usableColumn\":false}],\"crud\":true,\"functionAuthor\":\"ruoyi\",\"functionName\":\"学校信息\",\"genPath\":\"/\",\"genType\":\"0\",\"moduleName\":\"business\",\"options\":\"{}\",\"packageName\":\"com.ruoyi.business\",\"params\":{},\"sub\":false,\"tableComment\":\"学校信息表\",\"tableId\":3,\"tableName\":\"biz_school\",\"tplCategory\":\"crud\",\"tplWebType\":\"element-plus\",\"tree\":false}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-24 10:41:30', 71);
INSERT INTO `sys_oper_log` VALUES (172, '代码生成', 8, 'com.ruoyi.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin', '管理员', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{\"tables\":\"biz_school\"}', NULL, 0, NULL, '2025-06-24 10:41:43', 474);
INSERT INTO `sys_oper_log` VALUES (173, '用户管理', 1, 'com.ruoyi.web.controller.system.SysUserController.add()', 'POST', 1, 'admin', '高中', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"createBy\":\"admin\",\"nickName\":\"郑东旭\",\"params\":{},\"postIds\":[],\"roleIds\":[100],\"status\":\"0\",\"userId\":104,\"userName\":\"19157727791\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-25 10:28:28', 184);
INSERT INTO `sys_oper_log` VALUES (174, '代码生成', 3, 'com.ruoyi.generator.controller.GenController.remove()', 'DELETE', 1, 'admin', '高中', '/tool/gen/1', '127.0.0.1', '内网IP', '[1]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-25 10:38:38', 29);
INSERT INTO `sys_oper_log` VALUES (175, '代码生成', 6, 'com.ruoyi.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin', '高中', '/tool/gen/importTable', '127.0.0.1', '内网IP', '{\"tables\":\"biz_student\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-25 10:38:55', 106);
INSERT INTO `sys_oper_log` VALUES (176, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '高中', '/tool/gen', '127.0.0.1', '内网IP', '{\"businessName\":\"student\",\"className\":\"BizStudent\",\"columns\":[{\"capJavaField\":\"StudentId\",\"columnComment\":\"学生主键ID\",\"columnId\":27,\"columnName\":\"student_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-06-25 10:38:55\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":true,\"isIncrement\":\"1\",\"isInsert\":\"1\",\"isPk\":\"1\",\"isRequired\":\"0\",\"javaField\":\"studentId\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":4,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"UserId\",\"columnComment\":\"关联的用户ID (sys_user.user_id)\",\"columnId\":28,\"columnName\":\"user_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-06-25 10:38:55\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"userId\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":4,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"SchoolId\",\"columnComment\":\"所属学校ID\",\"columnId\":29,\"columnName\":\"school_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-06-25 10:38:55\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"schoolId\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":3,\"superColumn\":false,\"tableId\":4,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"StudentNo\",\"columnComment\":\"学号\",\"columnId\":30,\"columnName\":\"student_no\",\"columnType\":\"varchar(30)\",\"createBy\":\"admin\",\"createTime\":\"2025-06-25 10:38:55\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-25 10:41:19', 133);
INSERT INTO `sys_oper_log` VALUES (177, '代码生成', 8, 'com.ruoyi.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin', '高中', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{\"tables\":\"biz_student\"}', NULL, 0, NULL, '2025-06-25 10:41:27', 480);
INSERT INTO `sys_oper_log` VALUES (178, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '高中', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"component\":\"business/student/index\",\"createTime\":\"2025-06-25 10:43:23\",\"icon\":\"date\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2012,\"menuName\":\"学生管理\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":0,\"path\":\"student\",\"perms\":\"business:student:list\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-25 10:44:11', 31);
INSERT INTO `sys_oper_log` VALUES (179, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '高中', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"component\":\"business/student/index\",\"createTime\":\"2025-06-25 10:43:23\",\"icon\":\"date\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2012,\"menuName\":\"学生管理\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":0,\"path\":\"student-guanli\",\"perms\":\"business:student:list\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-25 10:45:24', 14);
INSERT INTO `sys_oper_log` VALUES (180, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '高中', '/system/menu/2000', '127.0.0.1', '内网IP', '2000', '{\"msg\":\"存在子菜单,不允许删除\",\"code\":601}', 0, NULL, '2025-06-25 10:46:05', 8);
INSERT INTO `sys_oper_log` VALUES (181, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '高中', '/system/menu/2001', '127.0.0.1', '内网IP', '2001', '{\"msg\":\"菜单已分配,不允许删除\",\"code\":601}', 0, NULL, '2025-06-25 10:46:23', 16);
INSERT INTO `sys_oper_log` VALUES (182, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '高中', '/system/menu/2005', '127.0.0.1', '内网IP', '2005', '{\"msg\":\"菜单已分配,不允许删除\",\"code\":601}', 0, NULL, '2025-06-25 10:56:12', 7);
INSERT INTO `sys_oper_log` VALUES (183, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '高中', '/system/menu/2005', '127.0.0.1', '内网IP', '2005', '{\"msg\":\"菜单已分配,不允许删除\",\"code\":601}', 0, NULL, '2025-06-25 10:56:18', 8);
INSERT INTO `sys_oper_log` VALUES (184, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '高中', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"component\":\"business/student/index\",\"createTime\":\"2025-06-17 16:56:13\",\"icon\":\"chart\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2000,\"menuName\":\"学生信息\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":0,\"path\":\"student-info\",\"perms\":\"business:student:list\",\"routeName\":\"\",\"status\":\"1\",\"updateBy\":\"admin\",\"visible\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-25 10:56:29', 13);
INSERT INTO `sys_oper_log` VALUES (185, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '高中', '/system/menu/2000', '127.0.0.1', '内网IP', '2000', '{\"msg\":\"存在子菜单,不允许删除\",\"code\":601}', 0, NULL, '2025-06-25 10:56:35', 5);
INSERT INTO `sys_oper_log` VALUES (186, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '高中', '/system/menu/2002', '127.0.0.1', '内网IP', '2002', '{\"msg\":\"菜单已分配,不允许删除\",\"code\":601}', 0, NULL, '2025-06-25 10:56:38', 8);
INSERT INTO `sys_oper_log` VALUES (187, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '高中', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"component\":\"business/student/index\",\"createTime\":\"2025-06-25 15:16:52\",\"icon\":\"user\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2018,\"menuName\":\"学生管理\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":0,\"path\":\"student\",\"perms\":\"business:student:list\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-25 15:17:27', 20);
INSERT INTO `sys_oper_log` VALUES (188, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '高中', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"component\":\"business/student/index\",\"createTime\":\"2025-06-25 15:16:52\",\"icon\":\"user\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2018,\"menuName\":\"学生管理\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":0,\"path\":\"studentguanli\",\"perms\":\"business:student:list\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-06-25 15:17:59', 16);
INSERT INTO `sys_oper_log` VALUES (189, '学生管理', 6, 'com.ruoyi.business.controller.BizStudentController.importTemplate()', 'POST', 1, 'admin', '高中', '/business/student/importTemplate', '127.0.0.1', '内网IP', '', NULL, 0, NULL, '2025-07-02 10:00:53', 1415);
INSERT INTO `sys_oper_log` VALUES (190, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, 'admin', '高中', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"avatar\":\"\",\"createBy\":\"admin\",\"createTime\":\"2025-06-25 10:28:28\",\"delFlag\":\"0\",\"deptId\":169,\"email\":\"\",\"loginIp\":\"\",\"nickName\":\"郑东旭\",\"params\":{},\"phonenumber\":\"\",\"postIds\":[],\"roleIds\":[100],\"roles\":[{\"admin\":false,\"dataScope\":\"1\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":100,\"roleKey\":\"teacher\",\"roleName\":\"教师\",\"roleSort\":0,\"status\":\"0\"}],\"sex\":\"0\",\"status\":\"0\",\"updateBy\":\"admin\",\"userId\":104,\"userName\":\"19157727791\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-07-02 10:11:00', 66);
INSERT INTO `sys_oper_log` VALUES (191, '个人信息', 2, 'com.ruoyi.web.controller.system.SysProfileController.updatePwd()', 'PUT', 1, '19157727791', '大目湾学校(初中部)', '/system/user/profile/updatePwd', '127.0.0.1', '内网IP', '{}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-07-02 10:14:56', 221);
INSERT INTO `sys_oper_log` VALUES (192, '角色管理', 2, 'com.ruoyi.web.controller.system.SysRoleController.edit()', 'PUT', 1, 'admin', '高中', '/system/role', '127.0.0.1', '内网IP', '{\"admin\":false,\"createTime\":\"2025-06-17 18:39:24\",\"dataScope\":\"1\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[1,100,1000,1001,1002,1003,1004,1005,1006,101,1007,1008,1009,1010,1011,102,1012,1013,1014,1015,103,1016,1017,1018,1019,104,1020,1021,1022,1023,1024,105,1025,1026,1027,1028,1029,106,1030,1031,1032,1033,1034,107,1035,1036,1037,1038,108,500,1039,1040,1041,501,1042,1043,1044,1045,2018,2019,2020,2021,2022,2023,2,109,1046,1047,1048,110,1049,1050,1051,1052,1053,1054,111,112,113,114,3,115,2006,2007,2008,2009,2010,2011,116,1055,1056,1057,1058,1059,1060,117,4],\"params\":{},\"roleId\":100,\"roleKey\":\"teacher\",\"roleName\":\"教师\",\"roleSort\":0,\"status\":\"0\",\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-07-02 10:16:50', 90);
INSERT INTO `sys_oper_log` VALUES (193, '角色管理', 2, 'com.ruoyi.web.controller.system.SysRoleController.edit()', 'PUT', 1, 'admin', '高中', '/system/role', '127.0.0.1', '内网IP', '{\"admin\":false,\"createTime\":\"2025-06-17 18:39:24\",\"dataScope\":\"1\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[1,100,1000,1001,1002,1003,1004,1005,1006,101,1007,1008,1009,1010,1011,102,1012,1013,1014,1015,103,1016,1017,1018,1019,104,1020,1021,1022,1023,1024,105,1025,1026,1027,1028,1029,106,1030,1031,1032,1033,1034,107,1035,1036,1037,1038,108,500,1039,1040,1041,501,1042,1043,1044,1045,2018,2019,2020,2021,2022,2023,2024,2,109,1046,1047,1048,110,1049,1050,1051,1052,1053,1054,111,112,113,114,3,115,2006,2007,2008,2009,2010,2011,116,1055,1056,1057,1058,1059,1060,117,4],\"params\":{},\"roleId\":100,\"roleKey\":\"teacher\",\"roleName\":\"教师\",\"roleSort\":0,\"status\":\"0\",\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-07-02 10:25:40', 71);
INSERT INTO `sys_oper_log` VALUES (194, '学生管理', 6, 'com.ruoyi.business.controller.BizStudentController.importData()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/student/importData', '127.0.0.1', '内网IP', '', NULL, 1, '操作的教师账号没有关联学校，无法导入学生！', '2025-07-02 10:41:37', 993);
INSERT INTO `sys_oper_log` VALUES (195, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, '19157727791', '大目湾学校(初中部)', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"avatar\":\"\",\"createBy\":\"admin\",\"createTime\":\"2025-06-25 10:28:28\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0,100,102\",\"children\":[],\"deptId\":169,\"deptName\":\"大目湾学校(初中部)\",\"orderNum\":0,\"params\":{},\"parentId\":102,\"status\":\"0\"},\"deptId\":169,\"email\":\"\",\"loginDate\":\"2025-07-02 10:26:06\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"郑东旭\",\"params\":{},\"phonenumber\":\"\",\"postIds\":[],\"pwdUpdateDate\":\"2025-07-02 10:14:56\",\"roleIds\":[100],\"roles\":[{\"admin\":false,\"dataScope\":\"1\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":100,\"roleKey\":\"teacher\",\"roleName\":\"教师\",\"roleSort\":0,\"status\":\"0\"}],\"sex\":\"0\",\"status\":\"0\",\"updateBy\":\"19157727791\",\"userId\":104,\"userName\":\"19157727791\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-07-02 10:58:03', 336);
INSERT INTO `sys_oper_log` VALUES (196, '学生管理', 6, 'com.ruoyi.business.controller.BizStudentController.importData()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/student/importData', '127.0.0.1', '内网IP', '', NULL, 1, '操作的教师账号没有关联学校，无法导入学生！', '2025-07-02 10:58:15', 855);
INSERT INTO `sys_oper_log` VALUES (197, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, '19157727791', '大目湾学校(初中部)', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"avatar\":\"\",\"createBy\":\"admin\",\"createTime\":\"2025-06-25 10:28:28\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0,100,102\",\"children\":[],\"deptId\":169,\"deptName\":\"大目湾学校(初中部)\",\"orderNum\":0,\"params\":{},\"parentId\":102,\"status\":\"0\"},\"deptId\":169,\"email\":\"\",\"loginDate\":\"2025-07-02 10:26:06\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"郑东旭\",\"params\":{},\"phonenumber\":\"\",\"postIds\":[],\"pwdUpdateDate\":\"2025-07-02 10:14:56\",\"roleIds\":[100],\"roles\":[{\"admin\":false,\"dataScope\":\"1\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":100,\"roleKey\":\"teacher\",\"roleName\":\"教师\",\"roleSort\":0,\"status\":\"0\"}],\"sex\":\"0\",\"status\":\"0\",\"updateBy\":\"19157727791\",\"userId\":104,\"userName\":\"19157727791\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-07-02 10:59:33', 87);
INSERT INTO `sys_oper_log` VALUES (198, '用户管理', 1, 'com.ruoyi.web.controller.system.SysUserController.add()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"createBy\":\"19157727791\",\"deptId\":146,\"nickName\":\"二中老师\",\"params\":{},\"postIds\":[],\"roleIds\":[],\"status\":\"0\",\"userId\":105,\"userName\":\"erzhong\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-07-02 11:04:24', 133);
INSERT INTO `sys_oper_log` VALUES (199, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, '19157727791', '大目湾学校(初中部)', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"avatar\":\"\",\"createBy\":\"admin\",\"createTime\":\"2025-06-25 10:28:28\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0,100,102\",\"children\":[],\"deptId\":169,\"deptName\":\"大目湾学校(初中部)\",\"orderNum\":0,\"params\":{},\"parentId\":102,\"status\":\"0\"},\"deptId\":169,\"email\":\"\",\"loginDate\":\"2025-07-02 13:09:36\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"郑东旭\",\"params\":{},\"phonenumber\":\"\",\"postIds\":[],\"pwdUpdateDate\":\"2025-07-02 10:14:56\",\"roleIds\":[100],\"roles\":[{\"admin\":false,\"dataScope\":\"1\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":100,\"roleKey\":\"teacher\",\"roleName\":\"教师\",\"roleSort\":0,\"status\":\"0\"}],\"sex\":\"0\",\"status\":\"0\",\"updateBy\":\"19157727791\",\"userId\":104,\"userName\":\"19157727791\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-07-02 13:09:46', 85);
INSERT INTO `sys_oper_log` VALUES (200, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, '19157727791', '大目湾学校(初中部)', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"avatar\":\"\",\"createBy\":\"admin\",\"createTime\":\"2025-06-25 10:28:28\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0,100,102\",\"children\":[],\"deptId\":169,\"deptName\":\"大目湾学校(初中部)\",\"orderNum\":0,\"params\":{},\"parentId\":102,\"status\":\"0\"},\"deptId\":169,\"email\":\"\",\"loginDate\":\"2025-07-02 13:45:52\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"郑东旭\",\"params\":{},\"phonenumber\":\"\",\"postIds\":[],\"pwdUpdateDate\":\"2025-07-02 10:14:56\",\"roleIds\":[100],\"roles\":[{\"admin\":false,\"dataScope\":\"1\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":100,\"roleKey\":\"teacher\",\"roleName\":\"教师\",\"roleSort\":0,\"status\":\"0\"}],\"sex\":\"0\",\"status\":\"0\",\"updateBy\":\"19157727791\",\"userId\":104,\"userName\":\"19157727791\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-07-02 13:46:05', 96);
INSERT INTO `sys_oper_log` VALUES (201, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, '19157727791', '大目湾学校(初中部)', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"avatar\":\"\",\"createBy\":\"admin\",\"createTime\":\"2025-06-25 10:28:28\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0,100,102\",\"children\":[],\"deptId\":169,\"deptName\":\"大目湾学校(初中部)\",\"orderNum\":0,\"params\":{},\"parentId\":102,\"status\":\"0\"},\"deptId\":169,\"email\":\"\",\"loginDate\":\"2025-07-02 13:45:52\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"郑东旭\",\"params\":{},\"phonenumber\":\"\",\"postIds\":[],\"pwdUpdateDate\":\"2025-07-02 10:14:56\",\"roleIds\":[100],\"roles\":[{\"admin\":false,\"dataScope\":\"1\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":100,\"roleKey\":\"teacher\",\"roleName\":\"教师\",\"roleSort\":0,\"status\":\"0\"}],\"sex\":\"0\",\"status\":\"0\",\"updateBy\":\"19157727791\",\"userId\":104,\"userName\":\"19157727791\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-07-02 13:48:19', 396);
INSERT INTO `sys_oper_log` VALUES (202, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, '19157727791', '大目湾学校(初中部)', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"avatar\":\"\",\"createBy\":\"admin\",\"createTime\":\"2025-06-25 10:28:28\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0,100,102\",\"children\":[],\"deptId\":169,\"deptName\":\"大目湾学校(初中部)\",\"orderNum\":0,\"params\":{},\"parentId\":102,\"status\":\"0\"},\"deptId\":169,\"email\":\"\",\"loginDate\":\"2025-07-02 13:45:52\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"郑东旭\",\"params\":{},\"phonenumber\":\"\",\"postIds\":[],\"pwdUpdateDate\":\"2025-07-02 10:14:56\",\"roleIds\":[100],\"roles\":[{\"admin\":false,\"dataScope\":\"1\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":100,\"roleKey\":\"teacher\",\"roleName\":\"教师\",\"roleSort\":0,\"status\":\"0\"}],\"sex\":\"0\",\"status\":\"0\",\"updateBy\":\"19157727791\",\"userId\":104,\"userName\":\"19157727791\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-07-02 13:49:56', 65);
INSERT INTO `sys_oper_log` VALUES (203, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, '19157727791', '大目湾学校(初中部)', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"avatar\":\"\",\"createBy\":\"admin\",\"createTime\":\"2025-06-25 10:28:28\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0,100,102\",\"children\":[],\"deptId\":169,\"deptName\":\"大目湾学校(初中部)\",\"orderNum\":0,\"params\":{},\"parentId\":102,\"status\":\"0\"},\"deptId\":169,\"email\":\"\",\"loginDate\":\"2025-07-02 13:45:52\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"郑东旭\",\"params\":{},\"phonenumber\":\"\",\"postIds\":[],\"pwdUpdateDate\":\"2025-07-02 10:14:56\",\"roleIds\":[100],\"roles\":[{\"admin\":false,\"dataScope\":\"1\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":100,\"roleKey\":\"teacher\",\"roleName\":\"教师\",\"roleSort\":0,\"status\":\"0\"}],\"sex\":\"0\",\"status\":\"0\",\"updateBy\":\"19157727791\",\"userId\":104,\"userName\":\"19157727791\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-07-02 13:51:03', 353);
INSERT INTO `sys_oper_log` VALUES (204, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, '19157727791', '大目湾学校(初中部)', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"avatar\":\"\",\"createBy\":\"admin\",\"createTime\":\"2025-06-25 10:28:28\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0,100,102\",\"children\":[],\"deptId\":169,\"deptName\":\"大目湾学校(初中部)\",\"orderNum\":0,\"params\":{},\"parentId\":102,\"status\":\"0\"},\"deptId\":169,\"email\":\"\",\"loginDate\":\"2025-07-02 13:45:52\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"郑东旭\",\"params\":{},\"phonenumber\":\"\",\"postIds\":[],\"pwdUpdateDate\":\"2025-07-02 10:14:56\",\"roleIds\":[100],\"roles\":[{\"admin\":false,\"dataScope\":\"1\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":100,\"roleKey\":\"teacher\",\"roleName\":\"教师\",\"roleSort\":0,\"status\":\"0\"}],\"sex\":\"0\",\"status\":\"0\",\"updateBy\":\"19157727791\",\"userId\":104,\"userName\":\"19157727791\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-07-02 13:54:21', 374);
INSERT INTO `sys_oper_log` VALUES (205, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, '19157727791', '大目湾学校(初中部)', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"avatar\":\"\",\"createBy\":\"admin\",\"createTime\":\"2025-06-25 10:28:28\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0,100,102\",\"children\":[],\"deptId\":169,\"deptName\":\"大目湾学校(初中部)\",\"orderNum\":0,\"params\":{},\"parentId\":102,\"status\":\"0\"},\"deptId\":169,\"email\":\"\",\"loginDate\":\"2025-07-02 13:45:52\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"郑东旭\",\"params\":{},\"phonenumber\":\"\",\"postIds\":[],\"pwdUpdateDate\":\"2025-07-02 10:14:56\",\"roleIds\":[100],\"roles\":[{\"admin\":false,\"dataScope\":\"1\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":100,\"roleKey\":\"teacher\",\"roleName\":\"教师\",\"roleSort\":0,\"status\":\"0\"}],\"sex\":\"0\",\"status\":\"0\",\"updateBy\":\"19157727791\",\"userId\":104,\"userName\":\"19157727791\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-07-02 14:07:34', 310);
INSERT INTO `sys_oper_log` VALUES (206, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, '19157727791', '大目湾学校(初中部)', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"avatar\":\"\",\"createBy\":\"admin\",\"createTime\":\"2025-06-25 10:28:28\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0,100,102\",\"children\":[],\"deptId\":169,\"deptName\":\"大目湾学校(初中部)\",\"orderNum\":0,\"params\":{},\"parentId\":102,\"status\":\"0\"},\"deptId\":169,\"email\":\"\",\"loginDate\":\"2025-07-02 13:45:52\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"郑东旭\",\"params\":{},\"phonenumber\":\"\",\"postIds\":[],\"pwdUpdateDate\":\"2025-07-02 10:14:56\",\"roleIds\":[100],\"roles\":[{\"admin\":false,\"dataScope\":\"1\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":100,\"roleKey\":\"teacher\",\"roleName\":\"教师\",\"roleSort\":0,\"status\":\"0\"}],\"schoolId\":72,\"sex\":\"0\",\"status\":\"0\",\"updateBy\":\"19157727791\",\"userId\":104,\"userName\":\"19157727791\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-07-02 14:17:50', 271);
INSERT INTO `sys_oper_log` VALUES (207, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, '19157727791', '大目湾学校(初中部)', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"avatar\":\"\",\"createBy\":\"19157727791\",\"createTime\":\"2025-07-02 11:04:24\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0,100,102\",\"children\":[],\"deptId\":146,\"deptName\":\"丹城二中\",\"orderNum\":0,\"params\":{},\"parentId\":102,\"status\":\"0\"},\"deptId\":146,\"email\":\"\",\"loginIp\":\"\",\"nickName\":\"二中老师\",\"params\":{},\"phonenumber\":\"\",\"postIds\":[],\"roleIds\":[],\"roles\":[],\"schoolId\":9,\"sex\":\"0\",\"status\":\"0\",\"updateBy\":\"19157727791\",\"userId\":105,\"userName\":\"erzhong\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-07-02 14:18:30', 41);
INSERT INTO `sys_oper_log` VALUES (208, '学生管理', 6, 'com.ruoyi.business.controller.BizStudentController.importData()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/student/importData', '127.0.0.1', '内网IP', '', NULL, 1, '操作的教师账号没有关联学校，无法导入学生！', '2025-07-02 14:25:40', 1024);
INSERT INTO `sys_oper_log` VALUES (209, '学生管理', 6, 'com.ruoyi.business.controller.BizStudentController.importData()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/student/importData', '127.0.0.1', '内网IP', '', NULL, 1, '操作的教师账号没有关联学校，无法导入学生！', '2025-07-02 14:29:56', 857);
INSERT INTO `sys_oper_log` VALUES (210, '学生管理', 6, 'com.ruoyi.business.controller.BizStudentController.importData()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/student/importData', '127.0.0.1', '内网IP', '', NULL, 1, '操作的教师账号没有关联学校，无法导入学生！', '2025-07-02 14:33:53', 815);
INSERT INTO `sys_oper_log` VALUES (211, '学生管理', 6, 'com.ruoyi.business.controller.BizStudentController.importData()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/student/importData', '127.0.0.1', '内网IP', '', NULL, 1, '操作的教师账号没有关联学校，无法导入学生！', '2025-07-02 14:51:50', 904);
INSERT INTO `sys_oper_log` VALUES (212, '学生管理', 6, 'com.ruoyi.business.controller.BizStudentController.importData()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/student/importData', '127.0.0.1', '内网IP', '', NULL, 1, '操作的教师账号没有关联学校，无法导入学生！', '2025-07-02 14:52:26', 113);
INSERT INTO `sys_oper_log` VALUES (213, '学生管理', 6, 'com.ruoyi.business.controller.BizStudentController.importData()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/student/importData', '127.0.0.1', '内网IP', '', '{\"msg\":\"恭喜您，数据已全部导入成功！共 48 条，数据如下：<br/>1、学生 黄睿 导入成功，登录账号为 2024720401<br/>2、学生 宋子旭 导入成功，登录账号为 2024720402<br/>3、学生 吴锦轩 导入成功，登录账号为 2024720403<br/>4、学生 蒋汶轩 导入成功，登录账号为 2024720404<br/>5、学生 张海玲 导入成功，登录账号为 2024720405<br/>6、学生 翁振轩 导入成功，登录账号为 2024720406<br/>7、学生 孟效谦 导入成功，登录账号为 2024720407<br/>8、学生 赖钇伊 导入成功，登录账号为 2024720408<br/>9、学生 金可 导入成功，登录账号为 2024720409<br/>10、学生 徐一宁 导入成功，登录账号为 2024720410<br/>11、学生 吴依萌 导入成功，登录账号为 2024720411<br/>12、学生 范梓晨 导入成功，登录账号为 2024720412<br/>13、学生 陈心持 导入成功，登录账号为 2024720413<br/>14、学生 欧俊毅 导入成功，登录账号为 2024720414<br/>15、学生 陈炫昊 导入成功，登录账号为 2024720415<br/>16、学生 罗一瑜 导入成功，登录账号为 2024720416<br/>17、学生 张乐轩 导入成功，登录账号为 2024720417<br/>18、学生 张子涵 导入成功，登录账号为 2024720418<br/>19、学生 汪靖宇 导入成功，登录账号为 2024720419<br/>20、学生 刘铭希 导入成功，登录账号为 2024720420<br/>21、学生 陈弈菡 导入成功，登录账号为 2024720421<br/>22、学生 顾智源 导入成功，登录账号为 2024720422<br/>23、学生 昂梓熙 导入成功，登录账号为 2024720423<br/>24、学生 金泽宇 导入成功，登录账号为 2024720424<br/>25、学生 何川 导入成功，登录账号为 2024720425<br/>26、学生 何计辰 导入成功，登录账号为 2024720426<br/>27、学生 张优雅 导入成功，登录账号为 2024720427<br/>28、学生 张硕涵 导入成功，登录账号为 2024720428<br/>29、学生 胡羽沫 导入成功，登录账号为 2024720429<br/>30、学生 刘俊贤 导入成功，登录账号为 2024720430<br/>31、学生 陈思颖 导入成功，登录账号为 2024720431<br/>32、学生 李长锟 导入成功，登录账号为 2024720432<br/>33、学生 林徐萱 导入成功，登录账号为 2024720433<br/>34、学生 陈俊宇 导入成功，登录账号为 2024720434<br/>35、学生 俞梓谦 导入成功，登录账号为 2024720435<br/>36、学生 史侑熙 导入成功，登录账号为 2024720436<br/>37、学生 林希雨 导入成功，登录账号为 2024720437<br/>38、学生 奚一诺 导入成功，登录账号为 2024720438<br/>39、学生 王洪妍 导入成功，登录账号为 2024720439<br/>40、学生 刘敦名 导入成功，登录账号为 2024720440<br/>41、学生 陈鑫豪 导入成功，登录账号为 2024720441<br/>42、学生 虞尔柏 导入成功，登录账号为 2024720442<br/>43、学生 罗佳怡 导入成功，登录账号为 2024720443<br/>44、学生 朱漫妮 导入成功，登录账号为 2024720444<br/>45、学生 朱静琪 导入成功，登录账号为 2024720445<br/>46、学生 俞航杰 导入成功，登录账号为 2024720446<br/>47、学生 周祺竣 导入成功，登录账号为 2024720447<br/>48、学生 吴承雪 导入成功，登录账号为 2024720448\",\"code\":200}', 0, NULL, '2025-07-02 14:59:38', 5152);
INSERT INTO `sys_oper_log` VALUES (214, '学生管理', 6, 'com.ruoyi.business.controller.BizStudentController.importTemplate()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/student/importTemplate', '127.0.0.1', '内网IP', '', NULL, 0, NULL, '2025-07-07 19:00:45', 878);
INSERT INTO `sys_oper_log` VALUES (215, '学生管理', 6, 'com.ruoyi.business.controller.BizStudentController.importTemplate()', 'POST', 1, 'admin', '高中', '/business/student/importTemplate', '127.0.0.1', '内网IP', '', NULL, 0, NULL, '2025-08-07 15:48:24', 790);
INSERT INTO `sys_oper_log` VALUES (216, '学生管理', 1, 'com.ruoyi.business.controller.BizStudentController.add()', 'POST', 1, 'admin', '高中', '/business/student', '127.0.0.1', '内网IP', '{\"classCode\":\"1\",\"entryYear\":\"2024\",\"params\":{},\"schoolId\":103,\"studentId\":49,\"studentName\":\"郑东旭\",\"studentNo\":\"99\",\"userId\":154}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-07 15:49:32', 96);
INSERT INTO `sys_oper_log` VALUES (217, '个人信息', 2, 'com.ruoyi.web.controller.system.SysProfileController.updatePwd()', 'PUT', 1, '2024720401', NULL, '/system/user/profile/updatePwd', '127.0.0.1', '内网IP', '{}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-07 15:51:26', 207);
INSERT INTO `sys_oper_log` VALUES (218, '学生管理', 2, 'com.ruoyi.business.controller.BizStudentController.resetPwd()', 'PUT', 1, 'admin', '高中', '/business/student/resetPwd', '127.0.0.1', '内网IP', '{\"admin\":false,\"params\":{},\"updateBy\":\"admin\",\"userId\":106}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-07 15:52:03', 66);
INSERT INTO `sys_oper_log` VALUES (219, '学生管理', 2, 'com.ruoyi.business.controller.BizStudentController.edit()', 'PUT', 1, 'admin', '高中', '/business/student', '127.0.0.1', '内网IP', '{\"classCode\":\"9\",\"entryYear\":\"2024\",\"params\":{},\"schoolId\":72,\"studentId\":1,\"studentName\":\"黄睿2\",\"studentNo\":\"1\",\"userId\":106,\"userName\":\"2024720401\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-07 15:53:40', 6);
INSERT INTO `sys_oper_log` VALUES (220, '学生管理', 1, 'com.ruoyi.business.controller.BizStudentController.add()', 'POST', 1, 'admin', '高中', '/business/student', '127.0.0.1', '内网IP', '{\"classCode\":\"4\",\"entryYear\":\"2024\",\"params\":{},\"schoolId\":103,\"studentId\":50,\"studentName\":\"重复\",\"studentNo\":\"2\",\"userId\":155}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-07 15:55:02', 76);
INSERT INTO `sys_oper_log` VALUES (221, '学生管理', 1, 'com.ruoyi.business.controller.BizStudentController.add()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/student', '127.0.0.1', '内网IP', '{\"classCode\":\"9\",\"entryYear\":\"2024\",\"params\":{},\"schoolId\":169,\"studentId\":51,\"studentName\":\"zdx\",\"studentNo\":\"3\",\"userId\":156}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-07 15:58:24', 71);
INSERT INTO `sys_oper_log` VALUES (222, '学生管理', 1, 'com.ruoyi.business.controller.BizStudentController.add()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/student', '127.0.0.1', '内网IP', '{\"classCode\":\"1\",\"entryYear\":\"2025\",\"params\":{},\"schoolId\":169,\"studentId\":52,\"studentName\":\"33\",\"studentNo\":\"1\",\"userId\":157}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-07 16:24:31', 188);
INSERT INTO `sys_oper_log` VALUES (223, '学生管理', 3, 'com.ruoyi.business.controller.BizStudentController.remove()', 'DELETE', 1, '19157727791', '大目湾学校(初中部)', '/business/student/51', '127.0.0.1', '内网IP', '[51]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-07 16:24:39', 6);
INSERT INTO `sys_oper_log` VALUES (224, '学生管理', 6, 'com.ruoyi.business.controller.BizStudentController.importTemplate()', 'POST', 1, 'admin', '高中', '/business/student/importTemplate', '127.0.0.1', '内网IP', '', NULL, 0, NULL, '2025-08-07 17:33:20', 787);
INSERT INTO `sys_oper_log` VALUES (225, '学生管理', 6, 'com.ruoyi.business.controller.BizStudentController.importTemplate()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/student/importTemplate', '127.0.0.1', '内网IP', '', NULL, 0, NULL, '2025-08-08 17:21:33', 785);
INSERT INTO `sys_oper_log` VALUES (226, '学生管理', 1, 'com.ruoyi.business.controller.BizStudentController.add()', 'POST', 1, 'admin', '高中', '/business/student', '127.0.0.1', '内网IP', '{\"classCode\":\"1\",\"entryYear\":\"2025\",\"params\":{},\"studentName\":\"1\",\"studentNo\":\"1\"}', NULL, 1, '教师关联的学校信息不完整，缺少学校官方ID (school_code)', '2025-08-09 19:20:47', 26);
INSERT INTO `sys_oper_log` VALUES (227, '学生管理', 2, 'com.ruoyi.business.controller.BizStudentController.resetPwd()', 'PUT', 1, 'admin', '高中', '/business/student/resetPwd', '127.0.0.1', '内网IP', '[107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,155]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-12 15:58:27', 218);
INSERT INTO `sys_oper_log` VALUES (228, '学生管理', 3, 'com.ruoyi.business.controller.BizStudentController.remove()', 'DELETE', 1, 'admin', '高中', '/business/student/49', '127.0.0.1', '内网IP', '[49]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-12 15:58:36', 21);
INSERT INTO `sys_oper_log` VALUES (229, '学生管理', 6, 'com.ruoyi.business.controller.BizStudentController.importTemplate()', 'POST', 1, 'admin', '高中', '/business/student/importTemplate', '127.0.0.1', '内网IP', '', NULL, 0, NULL, '2025-08-12 15:58:57', 775);
INSERT INTO `sys_oper_log` VALUES (230, '代码生成', 3, 'com.ruoyi.generator.controller.GenController.remove()', 'DELETE', 1, 'admin', '高中', '/tool/gen/2', '127.0.0.1', '内网IP', '[2]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-13 16:38:50', 23);
INSERT INTO `sys_oper_log` VALUES (231, '代码生成', 6, 'com.ruoyi.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin', '高中', '/tool/gen/importTable', '127.0.0.1', '内网IP', '{\"tables\":\"biz_lesson\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-13 16:39:03', 46);
INSERT INTO `sys_oper_log` VALUES (232, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '高中', '/tool/gen', '127.0.0.1', '内网IP', '{\"businessName\":\"lesson\",\"className\":\"BizLesson\",\"columns\":[{\"capJavaField\":\"LessonId\",\"columnComment\":\"课程ID\",\"columnId\":33,\"columnName\":\"lesson_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-08-13 16:39:03\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":true,\"isIncrement\":\"1\",\"isInsert\":\"1\",\"isPk\":\"1\",\"isRequired\":\"0\",\"javaField\":\"lessonId\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":5,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"LessonTitle\",\"columnComment\":\"课程/作业标题\",\"columnId\":34,\"columnName\":\"lesson_title\",\"columnType\":\"varchar(255)\",\"createBy\":\"admin\",\"createTime\":\"2025-08-13 16:39:03\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"lessonTitle\",\"javaType\":\"String\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":5,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"Grade\",\"columnComment\":\"年级 (例如: 1, 2, 3, 4, 5, 6)\",\"columnId\":35,\"columnName\":\"grade\",\"columnType\":\"int\",\"createBy\":\"admin\",\"createTime\":\"2025-08-13 16:39:03\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"select\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"0\",\"javaField\":\"grade\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":false,\"sort\":3,\"superColumn\":false,\"tableId\":5,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"Semester\",\"columnComment\":\"学期 (0上册, 1下册)\",\"columnId\":36,\"columnName\":\"semester\",\"columnType\":\"char(1)\",\"createBy\":\"admin\",\"createTime\":\"2025-08-13 16:39:03\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"select\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-13 16:45:21', 41);
INSERT INTO `sys_oper_log` VALUES (233, '字典类型', 1, 'com.ruoyi.web.controller.system.SysDictTypeController.add()', 'POST', 1, 'admin', '高中', '/system/dict/type', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"dictName\":\"学期\",\"dictType\":\"biz_semester\",\"params\":{},\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-13 16:46:33', 9);
INSERT INTO `sys_oper_log` VALUES (234, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin', '高中', '/system/dict/data', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"cssClass\":\"\",\"default\":false,\"dictLabel\":\"上册\",\"dictSort\":1,\"dictType\":\"biz_semester\",\"dictValue\":\"0\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-13 16:47:11', 8);
INSERT INTO `sys_oper_log` VALUES (235, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin', '高中', '/system/dict/data', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"default\":false,\"dictLabel\":\"下册\",\"dictSort\":2,\"dictType\":\"biz_semester\",\"dictValue\":\"1\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-13 16:47:25', 7);
INSERT INTO `sys_oper_log` VALUES (236, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '高中', '/tool/gen', '127.0.0.1', '内网IP', '{\"businessName\":\"lesson\",\"className\":\"BizLesson\",\"columns\":[{\"capJavaField\":\"LessonId\",\"columnComment\":\"课程ID\",\"columnId\":33,\"columnName\":\"lesson_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-08-13 16:39:03\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":true,\"isIncrement\":\"1\",\"isInsert\":\"1\",\"isPk\":\"1\",\"isRequired\":\"0\",\"javaField\":\"lessonId\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":5,\"updateBy\":\"\",\"updateTime\":\"2025-08-13 16:45:21\",\"usableColumn\":false},{\"capJavaField\":\"LessonTitle\",\"columnComment\":\"课程/作业标题\",\"columnId\":34,\"columnName\":\"lesson_title\",\"columnType\":\"varchar(255)\",\"createBy\":\"admin\",\"createTime\":\"2025-08-13 16:39:03\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"lessonTitle\",\"javaType\":\"String\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":5,\"updateBy\":\"\",\"updateTime\":\"2025-08-13 16:45:21\",\"usableColumn\":false},{\"capJavaField\":\"Grade\",\"columnComment\":\"年级 (例如: 1, 2, 3, 4, 5, 6)\",\"columnId\":35,\"columnName\":\"grade\",\"columnType\":\"int\",\"createBy\":\"admin\",\"createTime\":\"2025-08-13 16:39:03\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"select\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"0\",\"javaField\":\"grade\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":false,\"sort\":3,\"superColumn\":false,\"tableId\":5,\"updateBy\":\"\",\"updateTime\":\"2025-08-13 16:45:21\",\"usableColumn\":false},{\"capJavaField\":\"Semester\",\"columnComment\":\"学期 (0上册, 1下册)\",\"columnId\":36,\"columnName\":\"semester\",\"columnType\":\"char(1)\",\"createBy\":\"admin\",\"createTime\":\"2025-08-13 16:39:03\",\"dictType\":\"biz_semester\",\"edit\":true,\"html', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-13 16:47:49', 25);
INSERT INTO `sys_oper_log` VALUES (237, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '高中', '/tool/gen', '127.0.0.1', '内网IP', '{\"businessName\":\"lesson\",\"className\":\"BizLesson\",\"columns\":[{\"capJavaField\":\"LessonId\",\"columnComment\":\"课程ID\",\"columnId\":33,\"columnName\":\"lesson_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-08-13 16:39:03\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":true,\"isIncrement\":\"1\",\"isInsert\":\"1\",\"isPk\":\"1\",\"isRequired\":\"0\",\"javaField\":\"lessonId\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":5,\"updateBy\":\"\",\"updateTime\":\"2025-08-13 16:47:49\",\"usableColumn\":false},{\"capJavaField\":\"LessonTitle\",\"columnComment\":\"课程/作业标题\",\"columnId\":34,\"columnName\":\"lesson_title\",\"columnType\":\"varchar(255)\",\"createBy\":\"admin\",\"createTime\":\"2025-08-13 16:39:03\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"lessonTitle\",\"javaType\":\"String\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":5,\"updateBy\":\"\",\"updateTime\":\"2025-08-13 16:47:49\",\"usableColumn\":false},{\"capJavaField\":\"Grade\",\"columnComment\":\"年级 (例如: 1, 2, 3, 4, 5, 6)\",\"columnId\":35,\"columnName\":\"grade\",\"columnType\":\"int\",\"createBy\":\"admin\",\"createTime\":\"2025-08-13 16:39:03\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"select\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"0\",\"javaField\":\"grade\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":false,\"sort\":3,\"superColumn\":false,\"tableId\":5,\"updateBy\":\"\",\"updateTime\":\"2025-08-13 16:47:49\",\"usableColumn\":false},{\"capJavaField\":\"Semester\",\"columnComment\":\"学期 (0上册, 1下册)\",\"columnId\":36,\"columnName\":\"semester\",\"columnType\":\"char(1)\",\"createBy\":\"admin\",\"createTime\":\"2025-08-13 16:39:03\",\"dictType\":\"biz_semester\",\"edit\":true,\"html', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-13 16:51:59', 19);
INSERT INTO `sys_oper_log` VALUES (238, '字典类型', 1, 'com.ruoyi.web.controller.system.SysDictTypeController.add()', 'POST', 1, 'admin', '高中', '/system/dict/type', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"dictName\":\"年级\",\"dictType\":\"biz_grade\",\"params\":{},\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-13 16:52:20', 6);
INSERT INTO `sys_oper_log` VALUES (239, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin', '高中', '/system/dict/data', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"default\":false,\"dictLabel\":\"一年级\",\"dictSort\":1,\"dictType\":\"biz_grade\",\"dictValue\":\"1\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-13 16:52:47', 6);
INSERT INTO `sys_oper_log` VALUES (240, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin', '高中', '/system/dict/data', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"default\":false,\"dictLabel\":\"二年级\",\"dictSort\":2,\"dictType\":\"biz_grade\",\"dictValue\":\"2\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-13 16:52:58', 6);
INSERT INTO `sys_oper_log` VALUES (241, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin', '高中', '/system/dict/data', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"default\":false,\"dictLabel\":\"三年级\",\"dictSort\":3,\"dictType\":\"biz_grade\",\"dictValue\":\"3\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-13 16:53:07', 6);
INSERT INTO `sys_oper_log` VALUES (242, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin', '高中', '/system/dict/data', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"default\":false,\"dictLabel\":\"四年级\",\"dictSort\":4,\"dictType\":\"biz_grade\",\"dictValue\":\"4\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-13 16:53:16', 6);
INSERT INTO `sys_oper_log` VALUES (243, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin', '高中', '/system/dict/data', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"default\":false,\"dictLabel\":\"五年级\",\"dictSort\":5,\"dictType\":\"biz_grade\",\"dictValue\":\"5\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-13 16:53:29', 6);
INSERT INTO `sys_oper_log` VALUES (244, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin', '高中', '/system/dict/data', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"default\":false,\"dictLabel\":\"六年级\",\"dictSort\":6,\"dictType\":\"biz_grade\",\"dictValue\":\"6\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-13 16:53:38', 5);
INSERT INTO `sys_oper_log` VALUES (245, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin', '高中', '/system/dict/data', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"default\":false,\"dictLabel\":\"七年级\",\"dictSort\":7,\"dictType\":\"biz_grade\",\"dictValue\":\"7\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-13 16:53:51', 6);
INSERT INTO `sys_oper_log` VALUES (246, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin', '高中', '/system/dict/data', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"default\":false,\"dictLabel\":\"八年级\",\"dictSort\":8,\"dictType\":\"biz_grade\",\"dictValue\":\"8\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-13 16:53:59', 6);
INSERT INTO `sys_oper_log` VALUES (247, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin', '高中', '/system/dict/data', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"default\":false,\"dictLabel\":\"九年级\",\"dictSort\":9,\"dictType\":\"biz_grade\",\"dictValue\":\"9\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-13 16:54:09', 5);
INSERT INTO `sys_oper_log` VALUES (248, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin', '高中', '/system/dict/data', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"default\":false,\"dictLabel\":\"高一\",\"dictSort\":10,\"dictType\":\"biz_grade\",\"dictValue\":\"10\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-13 16:54:19', 8);
INSERT INTO `sys_oper_log` VALUES (249, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin', '高中', '/system/dict/data', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"default\":false,\"dictLabel\":\"高二\",\"dictSort\":11,\"dictType\":\"biz_grade\",\"dictValue\":\"11\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-13 16:54:29', 6);
INSERT INTO `sys_oper_log` VALUES (250, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin', '高中', '/system/dict/data', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"default\":false,\"dictLabel\":\"高三\",\"dictSort\":12,\"dictType\":\"biz_grade\",\"dictValue\":\"12\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-13 16:54:43', 6);
INSERT INTO `sys_oper_log` VALUES (251, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '高中', '/tool/gen', '127.0.0.1', '内网IP', '{\"businessName\":\"lesson\",\"className\":\"BizLesson\",\"columns\":[{\"capJavaField\":\"LessonId\",\"columnComment\":\"课程ID\",\"columnId\":33,\"columnName\":\"lesson_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-08-13 16:39:03\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":true,\"isIncrement\":\"1\",\"isInsert\":\"1\",\"isPk\":\"1\",\"isRequired\":\"0\",\"javaField\":\"lessonId\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":5,\"updateBy\":\"\",\"updateTime\":\"2025-08-13 16:51:59\",\"usableColumn\":false},{\"capJavaField\":\"LessonTitle\",\"columnComment\":\"课程/作业标题\",\"columnId\":34,\"columnName\":\"lesson_title\",\"columnType\":\"varchar(255)\",\"createBy\":\"admin\",\"createTime\":\"2025-08-13 16:39:03\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"lessonTitle\",\"javaType\":\"String\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":5,\"updateBy\":\"\",\"updateTime\":\"2025-08-13 16:51:59\",\"usableColumn\":false},{\"capJavaField\":\"Grade\",\"columnComment\":\"年级 (例如: 1, 2, 3, 4, 5, 6)\",\"columnId\":35,\"columnName\":\"grade\",\"columnType\":\"int\",\"createBy\":\"admin\",\"createTime\":\"2025-08-13 16:39:03\",\"dictType\":\"biz_grade\",\"edit\":true,\"htmlType\":\"select\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"0\",\"javaField\":\"grade\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":false,\"sort\":3,\"superColumn\":false,\"tableId\":5,\"updateBy\":\"\",\"updateTime\":\"2025-08-13 16:51:59\",\"usableColumn\":false},{\"capJavaField\":\"Semester\",\"columnComment\":\"学期 (0上册, 1下册)\",\"columnId\":36,\"columnName\":\"semester\",\"columnType\":\"char(1)\",\"createBy\":\"admin\",\"createTime\":\"2025-08-13 16:39:03\",\"dictType\":\"biz_semester\",\"edit\":t', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-13 16:55:19', 17);
INSERT INTO `sys_oper_log` VALUES (252, '代码生成', 8, 'com.ruoyi.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin', '高中', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{\"tables\":\"biz_lesson\"}', NULL, 0, NULL, '2025-08-14 09:28:17', 208);
INSERT INTO `sys_oper_log` VALUES (253, '课程/主题', 1, 'com.ruoyi.business.controller.BizLessonController.add()', 'POST', 1, 'admin', '高中', '/business/lesson', '127.0.0.1', '内网IP', '{\"createTime\":\"2025-08-14 09:42:53\",\"creatorId\":1,\"params\":{}}', NULL, 1, '\r\n### Error updating database.  Cause: java.sql.SQLException: Field \'lesson_title\' doesn\'t have a default value\r\n### The error may exist in file [D:\\Program\\damuwan\\newdazi\\RuoYi-Vue\\ruoyi-business\\target\\classes\\mapper\\business\\BizLessonMapper.xml]\r\n### The error may involve com.ruoyi.business.mapper.BizLessonMapper.insertBizLesson-Inline\r\n### The error occurred while setting parameters\r\n### SQL: insert into biz_lesson          ( creator_id,             create_time )           values ( ?,             ? )\r\n### Cause: java.sql.SQLException: Field \'lesson_title\' doesn\'t have a default value\n; Field \'lesson_title\' doesn\'t have a default value; nested exception is java.sql.SQLException: Field \'lesson_title\' doesn\'t have a default value', '2025-08-14 09:42:53', 66);
INSERT INTO `sys_oper_log` VALUES (254, '课程/作业信息', 1, 'com.ruoyi.business.controller.BizLessonController.add()', 'POST', 1, 'admin', '高中', '/business/lesson', '127.0.0.1', '内网IP', '{\"createTime\":\"2025-08-14 09:43:53\",\"grade\":1,\"lessonId\":1,\"lessonNum\":5,\"lessonTitle\":\"55\",\"params\":{},\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-14 09:43:53', 114);
INSERT INTO `sys_oper_log` VALUES (255, '字典类型', 1, 'com.ruoyi.web.controller.system.SysDictTypeController.add()', 'POST', 1, 'admin', '高中', '/system/dict/type', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"dictName\":\"题目类型\",\"dictType\":\"biz_question_type\",\"params\":{},\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-14 09:54:28', 9);
INSERT INTO `sys_oper_log` VALUES (256, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin', '高中', '/system/dict/data', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"default\":false,\"dictLabel\":\"选择题\",\"dictSort\":1,\"dictType\":\"biz_question_type\",\"dictValue\":\"choice\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-14 09:55:24', 8);
INSERT INTO `sys_oper_log` VALUES (257, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin', '高中', '/system/dict/data', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"default\":false,\"dictLabel\":\"打字题\",\"dictSort\":2,\"dictType\":\"biz_question_type\",\"dictValue\":\"typing\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-14 09:55:43', 7);
INSERT INTO `sys_oper_log` VALUES (258, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin', '高中', '/system/dict/data', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"default\":false,\"dictLabel\":\"判断题\",\"dictSort\":3,\"dictType\":\"biz_question_type\",\"dictValue\":\"judgment\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-14 09:56:01', 7);
INSERT INTO `sys_oper_log` VALUES (259, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin', '高中', '/system/dict/data', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"default\":false,\"dictLabel\":\"操作题\",\"dictSort\":4,\"dictType\":\"biz_question_type\",\"dictValue\":\"practical\",\"listClass\":\"default\",\"params\":{},\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-14 09:56:23', 7);
INSERT INTO `sys_oper_log` VALUES (260, '代码生成', 6, 'com.ruoyi.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin', '高中', '/tool/gen/importTable', '127.0.0.1', '内网IP', '{\"tables\":\"biz_question\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-14 09:57:15', 61);
INSERT INTO `sys_oper_log` VALUES (261, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '高中', '/tool/gen', '127.0.0.1', '内网IP', '{\"businessName\":\"question\",\"className\":\"BizQuestion\",\"columns\":[{\"capJavaField\":\"QuestionId\",\"columnComment\":\"题目ID\",\"columnId\":43,\"columnName\":\"question_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-08-14 09:57:15\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":false,\"isIncrement\":\"1\",\"isInsert\":\"0\",\"isPk\":\"1\",\"isRequired\":\"0\",\"javaField\":\"questionId\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":6,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"QuestionType\",\"columnComment\":\"题目类型 (choice, typing, judgment, practical)\",\"columnId\":44,\"columnName\":\"question_type\",\"columnType\":\"varchar(20)\",\"createBy\":\"admin\",\"createTime\":\"2025-08-14 09:57:15\",\"dictType\":\"biz_question_type\",\"edit\":true,\"htmlType\":\"select\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"questionType\",\"javaType\":\"String\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":6,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"QuestionContent\",\"columnComment\":\"题目内容/题干\",\"columnId\":45,\"columnName\":\"question_content\",\"columnType\":\"text\",\"createBy\":\"admin\",\"createTime\":\"2025-08-14 09:57:15\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"textarea\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"0\",\"javaField\":\"questionContent\",\"javaType\":\"String\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"LIKE\",\"required\":false,\"sort\":3,\"superColumn\":false,\"tableId\":6,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"OptionA\",\"columnComment\":\"选项A\",\"columnId\":46,\"columnName\":\"option_a\",\"columnType\":\"varchar(255)\",\"createBy\":\"admin\",\"createTime\":\"2025-08-14 09:57:15\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"inse', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-14 10:03:00', 44);
INSERT INTO `sys_oper_log` VALUES (262, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '高中', '/tool/gen', '127.0.0.1', '内网IP', '{\"businessName\":\"question\",\"className\":\"BizQuestion\",\"columns\":[{\"capJavaField\":\"QuestionId\",\"columnComment\":\"题目ID\",\"columnId\":43,\"columnName\":\"question_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-08-14 09:57:15\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":false,\"isIncrement\":\"1\",\"isInsert\":\"0\",\"isPk\":\"1\",\"isRequired\":\"0\",\"javaField\":\"questionId\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":6,\"updateBy\":\"\",\"updateTime\":\"2025-08-14 10:03:00\",\"usableColumn\":false},{\"capJavaField\":\"QuestionType\",\"columnComment\":\"题目类型 (choice, typing, judgment, practical)\",\"columnId\":44,\"columnName\":\"question_type\",\"columnType\":\"varchar(20)\",\"createBy\":\"admin\",\"createTime\":\"2025-08-14 09:57:15\",\"dictType\":\"biz_question_type\",\"edit\":true,\"htmlType\":\"select\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"questionType\",\"javaType\":\"String\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":6,\"updateBy\":\"\",\"updateTime\":\"2025-08-14 10:03:00\",\"usableColumn\":false},{\"capJavaField\":\"QuestionContent\",\"columnComment\":\"题目内容/题干\",\"columnId\":45,\"columnName\":\"question_content\",\"columnType\":\"text\",\"createBy\":\"admin\",\"createTime\":\"2025-08-14 09:57:15\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"textarea\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"0\",\"javaField\":\"questionContent\",\"javaType\":\"String\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"LIKE\",\"required\":false,\"sort\":3,\"superColumn\":false,\"tableId\":6,\"updateBy\":\"\",\"updateTime\":\"2025-08-14 10:03:00\",\"usableColumn\":false},{\"capJavaField\":\"OptionA\",\"columnComment\":\"选项A\",\"columnId\":46,\"columnName\":\"option_a\",\"columnType\":\"varchar(255)\",\"createBy\":\"admin', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-18 20:01:01', 67);
INSERT INTO `sys_oper_log` VALUES (263, '代码生成', 8, 'com.ruoyi.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin', '高中', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{\"tables\":\"biz_question\"}', NULL, 0, NULL, '2025-08-18 20:01:05', 214);
INSERT INTO `sys_oper_log` VALUES (264, '角色管理', 2, 'com.ruoyi.web.controller.system.SysRoleController.edit()', 'PUT', 1, 'admin', '高中', '/system/role', '127.0.0.1', '内网IP', '{\"admin\":false,\"createTime\":\"2025-06-17 18:39:24\",\"dataScope\":\"1\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[1,100,1000,1001,1002,1003,1004,1005,1006,101,1007,1008,1009,1010,1011,102,1012,1013,1014,1015,103,1016,1017,1018,1019,104,1020,1021,1022,1023,1024,105,1025,1026,1027,1028,1029,106,1030,1031,1032,1033,1034,107,1035,1036,1037,1038,108,500,1039,1040,1041,501,1042,1043,1044,1045,2018,2019,2020,2021,2022,2023,2024,2031,2032,2033,2034,2035,2036,2,109,1046,1047,1048,110,1049,1050,1051,1052,1053,1054,111,112,113,114,3,115,2006,2007,2008,2009,2010,2011,116,1055,1056,1057,1058,1059,1060,117,4,2026,2027,2028,2029,2030],\"params\":{},\"roleId\":100,\"roleKey\":\"teacher\",\"roleName\":\"教师\",\"roleSort\":0,\"status\":\"0\",\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-18 20:10:35', 42);
INSERT INTO `sys_oper_log` VALUES (265, '代码生成', 6, 'com.ruoyi.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin', '高中', '/tool/gen/importTable', '127.0.0.1', '内网IP', '{\"tables\":\"biz_lesson_question\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-18 20:11:18', 33);
INSERT INTO `sys_oper_log` VALUES (266, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '高中', '/tool/gen', '127.0.0.1', '内网IP', '{\"businessName\":\"question\",\"className\":\"BizLessonQuestion\",\"columns\":[{\"capJavaField\":\"Id\",\"columnId\":59,\"columnName\":\"id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-08-18 20:11:18\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":true,\"isIncrement\":\"1\",\"isInsert\":\"1\",\"isPk\":\"1\",\"isRequired\":\"0\",\"javaField\":\"id\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":7,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"LessonId\",\"columnComment\":\"课程ID\",\"columnId\":60,\"columnName\":\"lesson_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-08-18 20:11:18\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"lessonId\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":7,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"QuestionId\",\"columnComment\":\"题目ID\",\"columnId\":61,\"columnName\":\"question_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-08-18 20:11:18\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"questionId\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":3,\"superColumn\":false,\"tableId\":7,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"QuestionScore\",\"columnComment\":\"该题目在本课程中的分值\",\"columnId\":62,\"columnName\":\"question_score\",\"columnType\":\"int\",\"createBy\":\"admin\",\"createTime\":\"2025-08-18 20:11:18\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"0\",\"javaField\":\"question', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-18 20:12:58', 24);
INSERT INTO `sys_oper_log` VALUES (267, '代码生成', 8, 'com.ruoyi.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin', '高中', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{\"tables\":\"biz_lesson_question\"}', NULL, 0, NULL, '2025-08-18 20:13:03', 194);
INSERT INTO `sys_oper_log` VALUES (268, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, 'admin', '高中', '/business/question', '127.0.0.1', '内网IP', '{\"analysis\":\"1\",\"answer\":\"c\",\"createTime\":\"2025-08-18 20:16:14\",\"isPublic\":\"Y\",\"optionA\":\"aa\",\"optionB\":\"bb\",\"optionC\":\"cc\",\"optionD\":\"dd\",\"params\":{},\"questionContent\":\"题目内容\",\"questionId\":1,\"questionType\":\"choice\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-18 20:16:14', 8);
INSERT INTO `sys_oper_log` VALUES (269, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, 'admin', '高中', '/business/question', '127.0.0.1', '内网IP', '{\"createTime\":\"2025-08-18 20:17:41\",\"isPublic\":\"Y\",\"params\":{},\"questionContent\":\"kdkewdfkewfhdksaedjhfksdfuhvbdksusghvss\",\"questionId\":2,\"questionType\":\"typing\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-18 20:17:41', 5);
INSERT INTO `sys_oper_log` VALUES (270, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, 'admin', '高中', '/business/question', '127.0.0.1', '内网IP', '{\"createTime\":\"2025-08-18 20:18:15\",\"filePath\":\"/profile/upload/2025/08/18/初中+信息科技+AI学情分析师：实现个性化练习的即时诊断与反馈作品设计使用说明_20250818201809A001.docx\",\"isPublic\":\"Y\",\"params\":{},\"questionId\":3,\"questionType\":\"practical\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-18 20:18:15', 4);
INSERT INTO `sys_oper_log` VALUES (271, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '高中', '/tool/gen', '127.0.0.1', '内网IP', '{\"businessName\":\"question\",\"className\":\"BizLessonQuestion\",\"columns\":[{\"capJavaField\":\"Id\",\"columnId\":59,\"columnName\":\"id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-08-18 20:11:18\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":true,\"isIncrement\":\"1\",\"isInsert\":\"1\",\"isPk\":\"1\",\"isRequired\":\"0\",\"javaField\":\"id\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":7,\"updateBy\":\"\",\"updateTime\":\"2025-08-18 20:12:58\",\"usableColumn\":false},{\"capJavaField\":\"LessonId\",\"columnComment\":\"课程ID\",\"columnId\":60,\"columnName\":\"lesson_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-08-18 20:11:18\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"lessonId\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":7,\"updateBy\":\"\",\"updateTime\":\"2025-08-18 20:12:58\",\"usableColumn\":false},{\"capJavaField\":\"QuestionId\",\"columnComment\":\"题目ID\",\"columnId\":61,\"columnName\":\"question_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-08-18 20:11:18\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"questionId\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":3,\"superColumn\":false,\"tableId\":7,\"updateBy\":\"\",\"updateTime\":\"2025-08-18 20:12:58\",\"usableColumn\":false},{\"capJavaField\":\"QuestionScore\",\"columnComment\":\"该题目在本课程中的分值\",\"columnId\":62,\"columnName\":\"question_score\",\"columnType\":\"int\",\"createBy\":\"admin\",\"createTime\":\"2025-08-18 20:11:18\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isI', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-18 20:38:28', 31);
INSERT INTO `sys_oper_log` VALUES (272, '课程/作业信息', 1, 'com.ruoyi.business.controller.BizLessonController.add()', 'POST', 1, 'admin', '高中', '/business/lesson', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"createTime\":\"2025-08-18 21:35:47\",\"creatorId\":1,\"grade\":7,\"lessonId\":2,\"lessonNum\":1,\"lessonTitle\":\"11\",\"params\":{},\"questions\":[{\"lessonId\":2,\"orderNum\":1,\"params\":{},\"questionId\":2,\"questionScore\":10},{\"lessonId\":2,\"orderNum\":2,\"params\":{},\"questionId\":1,\"questionScore\":10}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-18 21:35:48', 41);
INSERT INTO `sys_oper_log` VALUES (273, '课程/作业信息', 2, 'com.ruoyi.business.controller.BizLessonController.edit()', 'PUT', 1, 'admin', '高中', '/business/lesson', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"createTime\":\"2025-08-18 21:35:48\",\"creatorId\":1,\"grade\":7,\"lessonId\":2,\"lessonNum\":1,\"lessonTitle\":\"11\",\"params\":{},\"questions\":[{\"id\":1,\"lessonId\":2,\"orderNum\":1,\"params\":{},\"questionId\":2,\"questionScore\":10},{\"id\":2,\"lessonId\":2,\"orderNum\":2,\"params\":{},\"questionId\":1,\"questionScore\":10},{\"lessonId\":2,\"orderNum\":3,\"params\":{},\"questionId\":3,\"questionScore\":10}],\"semester\":\"0\",\"updateBy\":\"admin\",\"updateTime\":\"2025-08-18 21:36:41\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-18 21:36:41', 10);
INSERT INTO `sys_oper_log` VALUES (274, '课程/作业信息', 1, 'com.ruoyi.business.controller.BizLessonController.add()', 'POST', 1, 'admin', '高中', '/business/lesson', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"createTime\":\"2025-08-19 07:29:04\",\"creatorId\":1,\"grade\":7,\"lessonId\":3,\"lessonNum\":2,\"lessonTitle\":\"222\",\"params\":{},\"questions\":[{\"lessonId\":3,\"orderNum\":1,\"params\":{},\"questionContent\":\"题目内容\",\"questionId\":1,\"questionScore\":10,\"questionType\":\"choice\"},{\"lessonId\":3,\"orderNum\":2,\"params\":{},\"questionContent\":\"kdkewdfkewfhdksaedjhfksdfuhvbdksusghvss\",\"questionId\":2,\"questionScore\":10,\"questionType\":\"typing\"}],\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-19 07:29:04', 43);
INSERT INTO `sys_oper_log` VALUES (275, '课程/作业信息', 1, 'com.ruoyi.business.controller.BizLessonController.add()', 'POST', 1, 'admin', '高中', '/business/lesson', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"createTime\":\"2025-08-19 07:30:42\",\"creatorId\":1,\"grade\":7,\"lessonId\":4,\"lessonNum\":4,\"lessonTitle\":\"44\",\"params\":{},\"questions\":[{\"lessonId\":4,\"orderNum\":1,\"params\":{},\"questionContent\":\"题目内容\",\"questionId\":1,\"questionScore\":10,\"questionType\":\"choice\"},{\"lessonId\":4,\"orderNum\":2,\"params\":{},\"questionContent\":\"kdkewdfkewfhdksaedjhfksdfuhvbdksusghvss\",\"questionId\":2,\"questionScore\":10,\"questionType\":\"typing\"}],\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-19 07:30:42', 126);
INSERT INTO `sys_oper_log` VALUES (276, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '高中', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"component\":\"business/lesson/index\",\"createTime\":\"2025-06-23 10:48:51\",\"icon\":\"button\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2006,\"menuName\":\"课程管理\",\"menuType\":\"C\",\"orderNum\":2,\"params\":{},\"parentId\":0,\"path\":\"lesson\",\"perms\":\"business:lesson:list\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-19 07:37:41', 15);
INSERT INTO `sys_oper_log` VALUES (277, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, 'admin', '高中', '/business/question', '127.0.0.1', '内网IP', '{\"createTime\":\"2025-08-19 07:46:05\",\"params\":{},\"questionContent\":\"成本v的司法环境测试v白色的数据库v从就开始第一个是白醋央视播出v就爱吃v吧而艰苦程度高于把苏永康VS从啊电池健康素养干哈八成功的的介绍客户参观巴萨大哭一场vu可以减肥成功就爱读书u会计初级的sys从数据库约定俗成啊u开始从访问速度快也是从v诉苦从v的快速有擦拭的库存苏看得出洒督促v发给多少库存v有发给谁毒抗蚜虫FSUKCFGU空间互踩法国的夙愿吃饭撒大开车的师傅他u撒督陈v发撒法看得出的素材v是SDFDCSUKY FGCSDAU6CAUSDC UASSJCVF GASDUCFDSUYCTFGVDS CJSUDCF USDCY AUACDY FDUCSF DSUCFDU CSDUSA CFADJYHSFGDCJUYSDCSAJ好几个v还记得上次是v成就感刀剑神域程度也是上次考试高分萨克创建VS就参与的素材是v方式督促v是督促萨克调查v从v苦于该发生的v看u哭的v阿萨\",\"questionId\":4,\"questionType\":\"typing\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-19 07:46:05', 18);
INSERT INTO `sys_oper_log` VALUES (278, '课程/作业信息', 2, 'com.ruoyi.business.controller.BizLessonController.edit()', 'PUT', 1, 'admin', '高中', '/business/lesson', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"createTime\":\"2025-08-19 07:30:43\",\"creatorId\":1,\"grade\":7,\"lessonId\":4,\"lessonNum\":4,\"lessonTitle\":\"44\",\"params\":{},\"questions\":[{\"id\":8,\"lessonId\":4,\"orderNum\":1,\"params\":{},\"questionContent\":\"题目内容\",\"questionId\":1,\"questionScore\":10,\"questionType\":\"choice\"},{\"id\":9,\"lessonId\":4,\"orderNum\":2,\"params\":{},\"questionContent\":\"kdkewdfkewfhdksaedjhfksdfuhvbdksusghvss\",\"questionId\":2,\"questionScore\":90,\"questionType\":\"typing\"}],\"semester\":\"1\",\"updateBy\":\"admin\",\"updateTime\":\"2025-08-20 09:59:52\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-20 09:59:52', 35);
INSERT INTO `sys_oper_log` VALUES (279, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, 'admin', '高中', '/business/question', '127.0.0.1', '内网IP', '{\"analysis\":\"<p><img src=\\\"/dev-api/profile/upload/2025/08/20/Quicker_20240827_151948_20250820110048A002.png\\\"></p>\",\"answer\":\"A\",\"createBy\":\"admin\",\"createTime\":\"2025-08-20 11:00:50\",\"creatorId\":1,\"grade\":7,\"isPublic\":\"Y\",\"optionA\":\"a\",\"optionB\":\"b\",\"optionC\":\"c\",\"optionD\":\"d\",\"params\":{},\"questionContent\":\"<p><img src=\\\"/dev-api/profile/upload/2025/08/20/4_20250820110019A001.jpg\\\"></p><p>和规范化非常</p>\",\"questionId\":5,\"questionType\":\"choice\",\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-20 11:00:50', 16);
INSERT INTO `sys_oper_log` VALUES (280, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, 'admin', '高中', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"createTime\":\"2025-08-20 11:01:58\",\"creatorId\":1,\"grade\":2,\"isPublic\":\"Y\",\"params\":{},\"questionContent\":\"<p>adscadsc都开始撑死了水库v出东方你死了你倒是快说v较好的死咯被忽略十五v的</p>\",\"questionId\":6,\"questionType\":\"typing\",\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-20 11:01:58', 4);
INSERT INTO `sys_oper_log` VALUES (281, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, 'admin', '高中', '/business/question', '127.0.0.1', '内网IP', '{\"analysis\":\"<p>解析</p>\",\"answer\":\"T\",\"createBy\":\"admin\",\"createTime\":\"2025-08-20 11:02:27\",\"creatorId\":1,\"grade\":7,\"isPublic\":\"Y\",\"params\":{},\"questionContent\":\"<p>除了vn选正确</p>\",\"questionId\":7,\"questionType\":\"judgment\",\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-20 11:02:27', 5);
INSERT INTO `sys_oper_log` VALUES (282, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, 'admin', '高中', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"createTime\":\"2025-08-20 11:02:49\",\"creatorId\":1,\"filePath\":\"/profile/upload/2025/08/20/41111_20250820110247A003.docx\",\"grade\":2,\"isPublic\":\"Y\",\"params\":{},\"questionContent\":\"<p>名称</p>\",\"questionId\":8,\"questionType\":\"practical\",\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-20 11:02:49', 5);
INSERT INTO `sys_oper_log` VALUES (283, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, 'admin', '高中', '/business/question', '127.0.0.1', '内网IP', '{\"analysis\":\"a\",\"answer\":\"A\",\"createBy\":\"admin\",\"createTime\":\"2025-08-20 11:18:30\",\"creatorId\":1,\"grade\":7,\"isPublic\":\"0\",\"optionA\":\"a\",\"optionB\":\"b\",\"optionC\":\"cv\",\"optionD\":\"d\",\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionType\":\"choice\",\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-20 11:18:30', 21);
INSERT INTO `sys_oper_log` VALUES (284, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, 'admin', '高中', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"createTime\":\"2025-08-20 11:20:59\",\"creatorId\":1,\"grade\":1,\"isPublic\":\"0\",\"params\":{},\"questionContent\":\"第三次\",\"questionId\":10,\"questionType\":\"choice\",\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-20 11:20:59', 6);
INSERT INTO `sys_oper_log` VALUES (285, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, 'admin', '高中', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"createTime\":\"2025-08-20 11:36:39\",\"creatorId\":1,\"grade\":1,\"isPublic\":\"Y\",\"params\":{},\"questionContent\":\"u\",\"questionId\":11,\"questionType\":\"choice\",\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-20 11:36:39', 5);
INSERT INTO `sys_oper_log` VALUES (286, '题库管理', 6, 'com.ruoyi.business.controller.BizQuestionController.importData()', 'POST', 1, 'admin', '高中', '/business/question/importData', '127.0.0.1', '内网IP', '{\"updateSupport\":\"0\"}', NULL, 1, '很抱歉，导入失败！共 1 条数据格式不正确，错误如下：<br/>1、题目 ... 导入失败：\r\n### Error updating database.  Cause: java.sql.SQLException: Field \'question_type\' doesn\'t have a default value\r\n### The error may exist in file [D:\\Program\\damuwan\\newdazi\\RuoYi-Vue\\ruoyi-business\\target\\classes\\mapper\\business\\BizQuestionMapper.xml]\r\n### The error may involve com.ruoyi.business.mapper.BizQuestionMapper.insertBizQuestion-Inline\r\n### The error occurred while setting parameters\r\n### SQL: insert into biz_question          ( option_a,             option_b,             option_c,             option_d,             answer,             analysis,                                       creator_id,             create_by,             create_time )           values ( ?,             ?,             ?,             ?,             ?,             ?,                                       ?,             ?,             ? )\r\n### Cause: java.sql.SQLException: Field \'question_type\' doesn\'t have a default value\n; Field \'question_type\' doesn\'t have a default value; nested exception is java.sql.SQLException: Field \'question_type\' doesn\'t have a default value', '2025-08-20 12:12:37', 328);
INSERT INTO `sys_oper_log` VALUES (287, '题库管理', 6, 'com.ruoyi.business.controller.BizQuestionController.importData()', 'POST', 1, 'admin', '高中', '/business/question/importData', '127.0.0.1', '内网IP', '{\"updateSupport\":\"0\"}', '{\"msg\":\"恭喜您，数据已全部导入成功！共 3 条，数据如下：<br/>1、题目 中国的首都是哪里？... 导入成功<br/>2、题目 请输入这段用于打字练习的文字。... 导入成功<br/>3、题目 地球是平的。... 导入成功\",\"code\":200}', 0, NULL, '2025-08-20 12:19:41', 54);
INSERT INTO `sys_oper_log` VALUES (288, '题库管理', 6, 'com.ruoyi.business.controller.BizQuestionController.importData()', 'POST', 1, 'admin', '高中', '/business/question/importData', '127.0.0.1', '内网IP', '{\"updateSupport\":\"0\"}', '{\"msg\":\"恭喜您，数据已全部导入成功！共 3 条，数据如下：<br/>1、题目 中国的首都是哪里？... 导入成功<br/>2、题目 请输入这段用于打字练习的文字。... 导入成功<br/>3、题目 地球是平的。... 导入成功\",\"code\":200}', 0, NULL, '2025-08-20 12:25:03', 85);
INSERT INTO `sys_oper_log` VALUES (289, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, 'admin', '高中', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"createTime\":\"2025-08-20 12:26:00\",\"creatorId\":1,\"grade\":1,\"isPublic\":\"N\",\"params\":{},\"questionContent\":\"试点城市水水\",\"questionId\":21,\"questionType\":\"typing\",\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-20 12:26:00', 8);
INSERT INTO `sys_oper_log` VALUES (290, '题库管理', 6, 'com.ruoyi.business.controller.BizQuestionController.importData()', 'POST', 1, 'admin', '高中', '/business/question/importData', '127.0.0.1', '内网IP', '{\"updateSupport\":\"0\"}', '{\"msg\":\"恭喜您，数据已全部导入成功！共 3 条，数据如下：<br/>1、题目 中国的首都是哪里？... 导入成功<br/>2、题目 请输入这段用于打字练习的文字。... 导入成功<br/>3、题目 地球是平的。... 导入成功\",\"code\":200}', 0, NULL, '2025-08-20 12:26:48', 26);
INSERT INTO `sys_oper_log` VALUES (291, '题库管理', 3, 'com.ruoyi.business.controller.BizQuestionController.remove()', 'DELETE', 1, 'admin', '高中', '/business/question/12', '127.0.0.1', '内网IP', '[12]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-20 12:27:18', 6);
INSERT INTO `sys_oper_log` VALUES (292, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, 'admin', '高中', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"createTime\":\"2025-08-20 13:23:31\",\"creatorId\":1,\"filePath\":\"/profile/upload/2025/08/20/apikey_20250820132329A001.txt\",\"grade\":1,\"isPublic\":\"Y\",\"params\":{},\"questionContent\":\"caddy\",\"questionId\":25,\"questionType\":\"practical\",\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-20 13:23:31', 12);
INSERT INTO `sys_oper_log` VALUES (293, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, 'admin', '高中', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"createTime\":\"2025-08-22 12:49:55\",\"creatorId\":1,\"grade\":1,\"isPublic\":\"N\",\"params\":{},\"questionContent\":\"商店超市菜市场\",\"questionId\":26,\"questionType\":\"typing\",\"semester\":\"0\",\"typingDuration\":1,\"wordCount\":7}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-22 12:49:55', 17);
INSERT INTO `sys_oper_log` VALUES (294, '题库管理', 6, 'com.ruoyi.business.controller.BizQuestionController.importData()', 'POST', 1, 'admin', '高中', '/business/question/importData', '127.0.0.1', '内网IP', '{\"updateSupport\":\"0\"}', '{\"msg\":\"恭喜您，数据已全部导入成功！共 3 条，数据如下：<br/>1、题目 中国的首都是哪里？... 导入成功<br/>2、题目 请输入这段用于打字练习的文字。... 导入成功<br/>3、题目 地球是平的。... 导入成功\",\"code\":200}', 0, NULL, '2025-08-22 12:51:07', 158);
INSERT INTO `sys_oper_log` VALUES (295, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, 'admin', '高中', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"createTime\":\"2025-08-22 13:53:35\",\"creatorId\":1,\"filePath\":\"/profile/upload/2025/08/22/41111_20250822135332A001.docx\",\"grade\":7,\"isPublic\":\"Y\",\"params\":{},\"questionContent\":\"操作题名称8\",\"questionId\":30,\"questionType\":\"practical\",\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-22 13:53:35', 16);
INSERT INTO `sys_oper_log` VALUES (296, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, 'admin', '高中', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"createTime\":\"2025-08-22 13:56:47\",\"creatorId\":1,\"filePath\":\"/profile/upload/2025/08/22/41111_20250822135646A001.docx\",\"grade\":7,\"isPublic\":\"N\",\"params\":{},\"questionContent\":\"操作题内容9\",\"questionId\":31,\"questionType\":\"practical\",\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-22 13:56:47', 15);
INSERT INTO `sys_oper_log` VALUES (297, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, 'admin', '高中', '/business/question', '127.0.0.1', '内网IP', '{\"answer\":\"B\",\"createBy\":\"admin\",\"createTime\":\"2025-08-22 14:02:35\",\"creatorId\":1,\"grade\":5,\"isPublic\":\"N\",\"optionA\":\"2\",\"optionB\":\"23\",\"optionC\":\"3\",\"optionD\":\"4\",\"params\":{},\"questionContent\":\"第三次多少\",\"questionId\":32,\"questionType\":\"choice\",\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-22 14:02:35', 103);
INSERT INTO `sys_oper_log` VALUES (298, '题库管理', 3, 'com.ruoyi.business.controller.BizQuestionController.remove()', 'DELETE', 1, 'admin', '高中', '/business/question/1', '127.0.0.1', '内网IP', '[1]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-22 14:18:14', 16);
INSERT INTO `sys_oper_log` VALUES (299, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, 'admin', '高中', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"createTime\":\"2025-08-22 14:23:04\",\"creatorId\":1,\"grade\":2,\"isPublic\":\"N\",\"params\":{},\"questionContent\":\"按顺序是\",\"questionId\":33,\"questionType\":\"typing\",\"semester\":\"1\",\"typingDuration\":2,\"wordCount\":4}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-22 14:23:04', 19);
INSERT INTO `sys_oper_log` VALUES (300, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, 'admin', '高中', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"createTime\":\"2025-08-22 15:04:08\",\"creatorId\":1,\"filePath\":\"/profile/upload/2025/08/22/读《屏幕时代，重塑孩子的自控力》有感——郑东旭——大目湾实验中学_20250822150406A001.doc\",\"grade\":2,\"isPublic\":\"Y\",\"params\":{},\"questionContent\":\"而光荣特\",\"questionId\":34,\"questionType\":\"practical\",\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-22 15:04:08', 143);
INSERT INTO `sys_oper_log` VALUES (301, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, 'admin', '高中', '/business/question', '127.0.0.1', '内网IP', '{\"answer\":\"T\",\"createBy\":\"admin\",\"createTime\":\"2025-08-25 17:16:30\",\"creatorId\":1,\"grade\":2,\"isPublic\":\"Y\",\"params\":{},\"questionContent\":\"lihli\",\"questionId\":35,\"questionType\":\"judgment\",\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-25 17:16:30', 17);
INSERT INTO `sys_oper_log` VALUES (302, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, 'admin', '高中', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"createTime\":\"2025-08-25 17:17:06\",\"creatorId\":1,\"filePath\":\"/profile/upload/2025/08/25/读《屏幕时代，重塑孩子的自控力》有感——郑东旭——大目湾实验中学_20250825171703A001.doc\",\"grade\":5,\"isPublic\":\"Y\",\"params\":{},\"questionContent\":\"ilhpohn\",\"questionId\":36,\"questionType\":\"practical\",\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-25 17:17:06', 63);
INSERT INTO `sys_oper_log` VALUES (303, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, 'admin', '高中', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"createTime\":\"2025-08-25 17:19:29\",\"creatorId\":1,\"filePath\":\"/profile/upload/2025/08/25/大目湾实验学校-郑东旭-信息科技_20250825171926A002.docx\",\"grade\":7,\"isPublic\":\"Y\",\"params\":{},\"previewPath\":\"/profile/upload/2025/08/25/大目湾实验学校-郑东旭-信息科技_20250825171926A002.pdf\",\"questionContent\":\"你好\",\"questionId\":37,\"questionType\":\"practical\",\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-25 17:19:29', 620);
INSERT INTO `sys_oper_log` VALUES (304, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, 'admin', '高中', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"createTime\":\"2025-08-25 17:24:43\",\"creatorId\":1,\"filePath\":\"/profile/upload/2025/08/25/评价，让学生看得见_20250825172440A003.doc\",\"grade\":8,\"isPublic\":\"Y\",\"params\":{},\"questionContent\":\"shuru\",\"questionId\":38,\"questionType\":\"practical\",\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-25 17:24:43', 9);
INSERT INTO `sys_oper_log` VALUES (305, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, 'admin', '高中', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"admin\",\"createTime\":\"2025-08-25 17:35:07\",\"creatorId\":1,\"filePath\":\"/profile/upload/2025/08/25/初中+信息科技+AI学情分析师：实现个性化练习的即时诊断与反馈_20250825173458A004.docx\",\"grade\":8,\"isPublic\":\"Y\",\"params\":{},\"previewPath\":\"/profile/upload/2025/08/25/初中+信息科技+AI学情分析师：实现个性化练习的即时诊断与反馈_20250825173458A004.pdf\",\"questionContent\":\"不好吗\",\"questionId\":39,\"questionType\":\"practical\",\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-25 17:35:07', 131);
INSERT INTO `sys_oper_log` VALUES (306, '学生管理', 1, 'com.ruoyi.business.controller.BizStudentController.add()', 'POST', 1, 'admin', '高中', '/business/student', '127.0.0.1', '内网IP', '{\"classCode\":\"1\",\"entryYear\":\"2024\",\"params\":{},\"studentName\":\"郑东旭\",\"studentNo\":\"1\"}', NULL, 1, '教师关联的学校信息不完整，缺少学校官方ID (school_code)', '2025-08-25 18:12:40', 32);
INSERT INTO `sys_oper_log` VALUES (307, '学生管理', 1, 'com.ruoyi.business.controller.BizStudentController.add()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/student', '127.0.0.1', '内网IP', '{\"classCode\":\"1\",\"entryYear\":\"2024\",\"params\":{},\"studentId\":53,\"studentName\":\"zdx\",\"studentNo\":\"1\",\"userId\":158}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-25 18:13:19', 77);
INSERT INTO `sys_oper_log` VALUES (308, '代码生成', 6, 'com.ruoyi.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin', '高中', '/tool/gen/importTable', '127.0.0.1', '内网IP', '{\"tables\":\"biz_lesson_assignment\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-25 19:12:22', 35);
INSERT INTO `sys_oper_log` VALUES (309, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '高中', '/tool/gen', '127.0.0.1', '内网IP', '{\"businessName\":\"assignment\",\"className\":\"BizLessonAssignment\",\"columns\":[{\"capJavaField\":\"AssignmentId\",\"columnComment\":\"指派记录ID\",\"columnId\":64,\"columnName\":\"assignment_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-08-25 19:12:22\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":true,\"isIncrement\":\"1\",\"isInsert\":\"1\",\"isPk\":\"1\",\"isRequired\":\"0\",\"javaField\":\"assignmentId\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":8,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"LessonId\",\"columnComment\":\"课程ID (关联 biz_lesson.lesson_id)\",\"columnId\":65,\"columnName\":\"lesson_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-08-25 19:12:22\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"lessonId\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":8,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"EntryYear\",\"columnComment\":\"指派班级的入学年份\",\"columnId\":66,\"columnName\":\"entry_year\",\"columnType\":\"varchar(4)\",\"createBy\":\"admin\",\"createTime\":\"2025-08-25 19:12:22\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"entryYear\",\"javaType\":\"String\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":3,\"superColumn\":false,\"tableId\":8,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"ClassCode\",\"columnComment\":\"指派班级的班级编号\",\"columnId\":67,\"columnName\":\"class_code\",\"columnType\":\"varchar(30)\",\"createBy\":\"admin\",\"createTime\":\"2025-08-25 19:12:22\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\"', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-25 19:16:09', 30);
INSERT INTO `sys_oper_log` VALUES (310, '代码生成', 8, 'com.ruoyi.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin', '高中', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{\"tables\":\"biz_lesson_assignment\"}', NULL, 0, NULL, '2025-08-25 19:16:24', 193);
INSERT INTO `sys_oper_log` VALUES (311, '菜单管理', 1, 'com.ruoyi.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '高中', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"component\":\"business/teacher/index\",\"createBy\":\"admin\",\"icon\":\"theme\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuName\":\"教师首页\",\"menuType\":\"C\",\"orderNum\":0,\"params\":{},\"parentId\":0,\"path\":\"teacher-dashboard\",\"perms\":\"business:teacher:list\",\"status\":\"0\",\"visible\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-25 20:05:44', 19);
INSERT INTO `sys_oper_log` VALUES (312, '角色管理', 2, 'com.ruoyi.web.controller.system.SysRoleController.edit()', 'PUT', 1, 'admin', '高中', '/system/role', '127.0.0.1', '内网IP', '{\"admin\":false,\"createTime\":\"2025-06-17 18:39:24\",\"dataScope\":\"1\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[2037,1,100,1000,1001,1002,1003,1004,1005,1006,101,1007,1008,1009,1010,1011,102,1012,1013,1014,1015,103,1016,1017,1018,1019,104,1020,1021,1022,1023,1024,105,1025,1026,1027,1028,1029,106,1030,1031,1032,1033,1034,107,1035,1036,1037,1038,108,500,1039,1040,1041,501,1042,1043,1044,1045,2018,2019,2020,2021,2022,2023,2024,2031,2032,2033,2034,2035,2036,2,109,1046,1047,1048,110,1049,1050,1051,1052,1053,1054,111,112,113,114,2006,2007,2008,2009,2010,2011,3,115,116,1055,1056,1057,1058,1059,1060,117,4,2026,2027,2028,2029,2030],\"params\":{},\"roleId\":100,\"roleKey\":\"teacher\",\"roleName\":\"教师\",\"roleSort\":0,\"status\":\"0\",\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-25 20:05:51', 38);
INSERT INTO `sys_oper_log` VALUES (313, '学生管理', 3, 'com.ruoyi.business.controller.BizStudentController.remove()', 'DELETE', 1, '19157727791', '大目湾学校(初中部)', '/business/student/1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,50,52', '127.0.0.1', '内网IP', '[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,50,52]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-25 20:06:44', 172);
INSERT INTO `sys_oper_log` VALUES (314, '学生管理', 3, 'com.ruoyi.business.controller.BizStudentController.remove()', 'DELETE', 1, '19157727791', '大目湾学校(初中部)', '/business/student/53', '127.0.0.1', '内网IP', '[53]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-25 20:06:49', 7);
INSERT INTO `sys_oper_log` VALUES (315, '学生管理', 1, 'com.ruoyi.business.controller.BizStudentController.add()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/student', '127.0.0.1', '内网IP', '{\"classCode\":\"1\",\"entryYear\":\"2024\",\"params\":{},\"studentId\":54,\"studentName\":\"张1\",\"studentNo\":\"1\",\"userId\":159}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-25 20:07:11', 77);
INSERT INTO `sys_oper_log` VALUES (316, '学生管理', 1, 'com.ruoyi.business.controller.BizStudentController.add()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/student', '127.0.0.1', '内网IP', '{\"classCode\":\"2\",\"entryYear\":\"2024\",\"params\":{},\"studentId\":55,\"studentName\":\"张2\",\"studentNo\":\"2\",\"userId\":160}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-25 20:07:20', 71);
INSERT INTO `sys_oper_log` VALUES (317, '学生管理', 1, 'com.ruoyi.business.controller.BizStudentController.add()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/student', '127.0.0.1', '内网IP', '{\"classCode\":\"3\",\"entryYear\":\"2024\",\"params\":{},\"studentId\":56,\"studentName\":\"张3\",\"studentNo\":\"4\",\"userId\":161}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-25 20:07:30', 72);
INSERT INTO `sys_oper_log` VALUES (318, '学生管理', 1, 'com.ruoyi.business.controller.BizStudentController.add()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/student', '127.0.0.1', '内网IP', '{\"classCode\":\"1\",\"entryYear\":\"2025\",\"params\":{},\"studentId\":57,\"studentName\":\"张2025\",\"studentNo\":\"2\",\"userId\":162}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-25 20:07:41', 71);
INSERT INTO `sys_oper_log` VALUES (319, '学生管理', 1, 'com.ruoyi.business.controller.BizStudentController.add()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/student', '127.0.0.1', '内网IP', '{\"classCode\":\"5\",\"entryYear\":\"2025\",\"params\":{},\"studentId\":58,\"studentName\":\"张20255\",\"studentNo\":\"6\",\"userId\":163}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-25 20:07:57', 73);
INSERT INTO `sys_oper_log` VALUES (320, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\"],\"createBy\":\"19157727791\",\"grade\":7,\"lessonId\":5,\"lessonNum\":1,\"lessonTitle\":\"可持续\",\"params\":{},\"questions\":[{\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":10,\"questionType\":\"choice\"},{\"orderNum\":2,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":14,\"questionScore\":10,\"questionType\":\"judgment\"},{\"orderNum\":3,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":15,\"questionScore\":10,\"questionType\":\"choice\"},{\"orderNum\":4,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":17,\"questionScore\":10,\"questionType\":\"judgment\"},{\"orderNum\":5,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":18,\"questionScore\":10,\"questionType\":\"choice\"},{\"orderNum\":6,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":22,\"questionScore\":10,\"questionType\":\"choice\"},{\"orderNum\":7,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":24,\"questionScore\":40,\"questionType\":\"judgment\"}],\"semester\":\"0\"}', NULL, 1, 'Invalid bound statement (not found): com.ruoyi.business.mapper.BizLessonQuestionMapper.deleteByLessonId', '2025-08-25 22:04:49', 32);
INSERT INTO `sys_oper_log` VALUES (321, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"19157727791\",\"createTime\":\"2025-08-25 22:08:59\",\"creatorId\":104,\"grade\":7,\"isPublic\":\"Y\",\"params\":{},\"questionContent\":\"而感染尴尬而广泛认同过热\",\"questionId\":40,\"questionType\":\"typing\",\"semester\":\"0\",\"typingDuration\":4,\"wordCount\":12}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-25 22:08:59', 15);
INSERT INTO `sys_oper_log` VALUES (322, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"5班\",\"1班\"],\"createBy\":\"19157727791\",\"grade\":7,\"lessonId\":6,\"lessonNum\":1,\"lessonTitle\":\"二哥\",\"params\":{},\"questions\":[{\"lessonId\":6,\"orderNum\":1,\"params\":{},\"questionContent\":\"而感染尴尬而广泛认同过热\",\"questionId\":40,\"questionScore\":100,\"questionType\":\"typing\"}],\"semester\":\"0\"}', NULL, 1, 'Invalid bound statement (not found): com.ruoyi.business.mapper.BizLessonQuestionMapper.batchInsert', '2025-08-25 22:09:42', 15);
INSERT INTO `sys_oper_log` VALUES (323, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"5班\",\"1班\"],\"createBy\":\"19157727791\",\"grade\":7,\"lessonId\":7,\"lessonNum\":1,\"lessonTitle\":\"二哥\",\"params\":{},\"questions\":[{\"lessonId\":7,\"orderNum\":1,\"params\":{},\"questionContent\":\"而感染尴尬而广泛认同过热\",\"questionId\":40,\"questionScore\":100,\"questionType\":\"typing\"}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-25 22:11:54', 41);
INSERT INTO `sys_oper_log` VALUES (324, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"2班\",\"1班\"],\"createBy\":\"19157727791\",\"grade\":8,\"lessonId\":8,\"lessonNum\":1,\"lessonTitle\":\"带我去\",\"params\":{},\"questions\":[{\"lessonId\":8,\"orderNum\":1,\"params\":{},\"questionContent\":\"请输入这段用于打字练习的文字。\",\"questionId\":19,\"questionScore\":10,\"questionType\":\"typing\"},{\"lessonId\":8,\"orderNum\":2,\"params\":{},\"questionContent\":\"shuru\",\"questionId\":38,\"questionScore\":90,\"questionType\":\"practical\"}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-25 22:44:06', 118);
INSERT INTO `sys_oper_log` VALUES (325, '课程管理', 3, 'com.ruoyi.business.controller.BizLessonController.remove()', 'DELETE', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/8', '127.0.0.1', '内网IP', '[8]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-08-25 23:13:32', 27);
INSERT INTO `sys_oper_log` VALUES (326, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, 'admin', '高中', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"avatar\":\"\",\"createBy\":\"admin\",\"createTime\":\"2025-06-25 10:28:28\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0,100,102\",\"children\":[],\"deptId\":169,\"deptName\":\"大目湾学校(初中部)\",\"orderNum\":0,\"params\":{},\"parentId\":102,\"status\":\"0\"},\"deptId\":169,\"deptIds\":[169,142],\"email\":\"\",\"loginDate\":\"2025-10-07 14:09:14\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"郑东旭\",\"params\":{},\"phonenumber\":\"\",\"postIds\":[],\"pwdUpdateDate\":\"2025-07-02 10:14:56\",\"roleIds\":[100],\"roles\":[{\"admin\":false,\"dataScope\":\"1\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":100,\"roleKey\":\"teacher\",\"roleName\":\"教师\",\"roleSort\":0,\"status\":\"0\"}],\"schoolId\":72,\"sex\":\"0\",\"status\":\"0\",\"updateBy\":\"admin\",\"userId\":104,\"userName\":\"19157727791\"}', NULL, 1, '\r\n### Error updating database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column \'school_id\' in \'field list\'\r\n### The error may exist in file [D:\\Program\\damuwan\\newdazi\\RuoYi-Vue\\ruoyi-system\\target\\classes\\mapper\\system\\SysUserMapper.xml]\r\n### The error may involve com.ruoyi.system.mapper.SysUserMapper.updateUser-Inline\r\n### The error occurred while setting parameters\r\n### SQL: update sys_user     SET dept_id = ?,     nick_name = ?,    school_id = ?,     email = ?,     phonenumber = ?,     sex = ?,          password = ?,     status = ?,     login_ip = ?,     login_date = ?,     update_by = ?,          update_time = sysdate()     where user_id = ?\r\n### Cause: java.sql.SQLSyntaxErrorException: Unknown column \'school_id\' in \'field list\'\n; bad SQL grammar []; nested exception is java.sql.SQLSyntaxErrorException: Unknown column \'school_id\' in \'field list\'', '2025-10-07 14:10:06', 89);
INSERT INTO `sys_oper_log` VALUES (327, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, 'admin', '高中', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"avatar\":\"\",\"createBy\":\"admin\",\"createTime\":\"2025-06-25 10:28:28\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0,100,102\",\"children\":[],\"deptId\":169,\"deptName\":\"大目湾学校(初中部)\",\"orderNum\":0,\"params\":{},\"parentId\":102,\"status\":\"0\"},\"deptId\":169,\"deptIds\":[169,107],\"email\":\"\",\"loginDate\":\"2025-10-07 14:09:14\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"郑东旭\",\"params\":{},\"phonenumber\":\"\",\"postIds\":[],\"pwdUpdateDate\":\"2025-07-02 10:14:56\",\"roleIds\":[100],\"roles\":[{\"admin\":false,\"dataScope\":\"1\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":100,\"roleKey\":\"teacher\",\"roleName\":\"教师\",\"roleSort\":0,\"status\":\"0\"}],\"sex\":\"0\",\"status\":\"0\",\"updateBy\":\"admin\",\"userId\":104,\"userName\":\"19157727791\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-10-07 14:41:45', 39);
INSERT INTO `sys_oper_log` VALUES (328, '学生管理', 6, 'com.ruoyi.business.controller.BizStudentController.importTemplate()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/student/importTemplate', '127.0.0.1', '内网IP', '{\"deptId\":\"169\"}', NULL, 0, NULL, '2025-10-08 14:59:35', 939);
INSERT INTO `sys_oper_log` VALUES (329, '学生管理', 6, 'com.ruoyi.business.controller.BizStudentController.importTemplate()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/student/importTemplate', '127.0.0.1', '内网IP', '{\"deptId\":\"107\"}', NULL, 0, NULL, '2025-10-08 14:59:49', 23);
INSERT INTO `sys_oper_log` VALUES (330, '学生管理', 6, 'com.ruoyi.business.controller.BizStudentController.importData()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/student/importData', '127.0.0.1', '内网IP', '', '{\"msg\":\"恭喜您，数据已全部导入成功！共 2 条，数据如下：<br/>1、学生 ksj 导入成功，登录账号为 202470201<br/>2、学生 oj 导入成功，登录账号为 202370102\",\"code\":200}', 0, NULL, '2025-10-08 15:01:11', 343);
INSERT INTO `sys_oper_log` VALUES (331, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"allClassesInGrade\":[\"1班\",\"5班\"],\"assignedClassCodes\":[\"5\",\"1\",\"5班\"],\"grade\":7,\"lessonId\":7,\"lessonNum\":1,\"lessonTitle\":\"二哥\",\"params\":{},\"questions\":[{\"lessonId\":7,\"orderNum\":1,\"params\":{},\"questionContent\":\"而感染尴尬而广泛认同过热\",\"questionId\":40,\"questionScore\":100,\"questionType\":\"typing\"}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"allClassesInGrade\":[\"1班\",\"5班\"],\"assignedClassCodes\":[\"5\",\"1\",\"5班\"],\"grade\":7,\"lessonId\":7,\"lessonNum\":1,\"lessonTitle\":\"二哥\",\"params\":{},\"questions\":[{\"lessonId\":7,\"orderNum\":1,\"params\":{},\"questionContent\":\"而感染尴尬而广泛认同过热\",\"questionId\":40,\"questionScore\":100,\"questionType\":\"typing\"}],\"semester\":\"0\"}}', 0, NULL, '2025-10-09 16:30:40', 112);
INSERT INTO `sys_oper_log` VALUES (332, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"allClassesInGrade\":[\"1班\",\"5班\"],\"assignedClassCodes\":[\"5\",\"1\",\"5\"],\"grade\":7,\"lessonId\":7,\"lessonNum\":1,\"lessonTitle\":\"二哥\",\"params\":{},\"questions\":[{\"lessonId\":7,\"orderNum\":1,\"params\":{},\"questionContent\":\"而感染尴尬而广泛认同过热\",\"questionId\":40,\"questionScore\":50,\"questionType\":\"typing\"},{\"orderNum\":2,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":50,\"questionType\":\"choice\"}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"allClassesInGrade\":[\"1班\",\"5班\"],\"assignedClassCodes\":[\"5\",\"1\",\"5\"],\"grade\":7,\"lessonId\":7,\"lessonNum\":1,\"lessonTitle\":\"二哥\",\"params\":{},\"questions\":[{\"lessonId\":7,\"orderNum\":1,\"params\":{},\"questionContent\":\"而感染尴尬而广泛认同过热\",\"questionId\":40,\"questionScore\":50,\"questionType\":\"typing\"},{\"orderNum\":2,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":50,\"questionType\":\"choice\"}],\"semester\":\"0\"}}', 0, NULL, '2025-10-09 16:34:46', 32);
INSERT INTO `sys_oper_log` VALUES (333, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"5班\"],\"grade\":7,\"lessonId\":9,\"lessonNum\":1,\"lessonTitle\":\"第二课\",\"params\":{},\"questions\":[{\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":100,\"questionType\":\"choice\"}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"5班\"],\"grade\":7,\"lessonId\":9,\"lessonNum\":1,\"lessonTitle\":\"第二课\",\"params\":{},\"questions\":[{\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":100,\"questionType\":\"choice\"}],\"semester\":\"0\"}}', 0, NULL, '2025-10-09 17:09:32', 51);
INSERT INTO `sys_oper_log` VALUES (334, '课程管理', 3, 'com.ruoyi.business.controller.BizLessonController.remove()', 'DELETE', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/9', '127.0.0.1', '内网IP', '[9]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-10-09 17:09:47', 41);
INSERT INTO `sys_oper_log` VALUES (335, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"5班\"],\"grade\":7,\"lessonId\":10,\"lessonNum\":1,\"lessonTitle\":\"33\",\"params\":{},\"questions\":[{\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":100,\"questionType\":\"choice\"}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"5班\"],\"grade\":7,\"lessonId\":10,\"lessonNum\":1,\"lessonTitle\":\"33\",\"params\":{},\"questions\":[{\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":100,\"questionType\":\"choice\"}],\"semester\":\"0\"}}', 0, NULL, '2025-10-09 17:10:04', 30);
INSERT INTO `sys_oper_log` VALUES (336, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"5\",\"5班\"],\"grade\":7,\"lessonId\":10,\"lessonNum\":1,\"lessonTitle\":\"33\",\"params\":{},\"questions\":[{\"lessonId\":10,\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":100,\"questionType\":\"choice\"}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"5\",\"5班\"],\"grade\":7,\"lessonId\":10,\"lessonNum\":1,\"lessonTitle\":\"33\",\"params\":{},\"questions\":[{\"lessonId\":10,\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":100,\"questionType\":\"choice\"}],\"semester\":\"0\"}}', 0, NULL, '2025-10-09 17:11:35', 204);
INSERT INTO `sys_oper_log` VALUES (337, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"5班\"],\"grade\":7,\"lessonId\":11,\"lessonNum\":1,\"lessonTitle\":\"说说说\",\"params\":{},\"questions\":[{\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":100,\"questionType\":\"choice\"}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"5班\"],\"grade\":7,\"lessonId\":11,\"lessonNum\":1,\"lessonTitle\":\"说说说\",\"params\":{},\"questions\":[{\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":100,\"questionType\":\"choice\"}],\"semester\":\"0\"}}', 0, NULL, '2025-10-09 17:11:51', 33);
INSERT INTO `sys_oper_log` VALUES (338, '角色管理', 2, 'com.ruoyi.web.controller.system.SysRoleController.edit()', 'PUT', 1, 'admin', '高中', '/system/role', '127.0.0.1', '内网IP', '{\"admin\":false,\"createTime\":\"2025-06-17 18:39:24\",\"dataScope\":\"1\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[2037,1,100,1000,1001,1002,1003,1004,1005,1006,101,1007,1008,1009,1010,1011,102,1012,1013,1014,1015,103,1016,1017,1018,1019,104,1020,1021,1022,1023,1024,105,1025,1026,1027,1028,1029,106,1030,1031,1032,1033,1034,107,1035,1036,1037,1038,108,500,1039,1040,1041,501,1042,1043,1044,1045,2018,2019,2020,2021,2022,2023,2024,2031,2032,2033,2034,2035,2036,2,109,1046,1047,1048,110,1049,1050,1051,1052,1053,1054,111,112,113,114,2006,2007,2008,2009,2010,2011,3,115,116,1055,1056,1057,1058,1059,1060,117,4,2038,2039,2040,2026,2027,2028,2029,2030],\"params\":{},\"roleId\":100,\"roleKey\":\"teacher\",\"roleName\":\"教师\",\"roleSort\":0,\"status\":\"0\",\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-29 14:29:08', 96);
INSERT INTO `sys_oper_log` VALUES (339, '教师班级管理', 1, 'com.ruoyi.business.controller.BizTeacherClassController.batchAdd()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/teacherClass/batch', '127.0.0.1', '内网IP', '[{\"classCode\":\"1\",\"createTime\":\"2025-12-29 14:30:05\",\"deptId\":169,\"entryYear\":\"2025\",\"params\":{},\"userId\":104},{\"classCode\":\"1\",\"createTime\":\"2025-12-29 14:30:05\",\"deptId\":169,\"entryYear\":\"2024\",\"params\":{},\"userId\":104}]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-29 14:30:05', 20);
INSERT INTO `sys_oper_log` VALUES (340, '教师班级管理', 1, 'com.ruoyi.business.controller.BizTeacherClassController.batchAdd()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/teacherClass/batch', '127.0.0.1', '内网IP', '[{\"classCode\":\"5\",\"createTime\":\"2025-12-29 14:39:38\",\"deptId\":169,\"entryYear\":\"2025\",\"params\":{},\"userId\":104}]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-29 14:39:38', 12);
INSERT INTO `sys_oper_log` VALUES (341, '教师班级管理', 1, 'com.ruoyi.business.controller.BizTeacherClassController.batchAdd()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/teacherClass/batch', '127.0.0.1', '内网IP', '[{\"classCode\":\"2\",\"createTime\":\"2025-12-29 15:51:31\",\"deptId\":107,\"entryYear\":\"2024\",\"params\":{},\"userId\":104},{\"classCode\":\"1\",\"createTime\":\"2025-12-29 15:51:31\",\"deptId\":107,\"entryYear\":\"2023\",\"params\":{},\"userId\":104}]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-29 15:51:31', 14);
INSERT INTO `sys_oper_log` VALUES (342, '教师班级管理', 3, 'com.ruoyi.business.controller.BizTeacherClassController.remove()', 'DELETE', 1, '19157727791', '大目湾学校(初中部)', '/business/teacherClass/5', '127.0.0.1', '内网IP', '[5]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-29 16:07:21', 35);
INSERT INTO `sys_oper_log` VALUES (343, '教师班级管理', 3, 'com.ruoyi.business.controller.BizTeacherClassController.remove()', 'DELETE', 1, '19157727791', '大目湾学校(初中部)', '/business/teacherClass/4', '127.0.0.1', '内网IP', '[4]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-29 16:07:23', 8);
INSERT INTO `sys_oper_log` VALUES (344, '教师班级管理', 3, 'com.ruoyi.business.controller.BizTeacherClassController.remove()', 'DELETE', 1, '19157727791', '大目湾学校(初中部)', '/business/teacherClass/2', '127.0.0.1', '内网IP', '[2]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-29 16:07:25', 7);
INSERT INTO `sys_oper_log` VALUES (345, '教师班级管理', 3, 'com.ruoyi.business.controller.BizTeacherClassController.remove()', 'DELETE', 1, '19157727791', '大目湾学校(初中部)', '/business/teacherClass/3', '127.0.0.1', '内网IP', '[3]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-29 16:07:28', 10);
INSERT INTO `sys_oper_log` VALUES (346, '教师班级管理', 3, 'com.ruoyi.business.controller.BizTeacherClassController.remove()', 'DELETE', 1, '19157727791', '大目湾学校(初中部)', '/business/teacherClass/1', '127.0.0.1', '内网IP', '[1]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-29 16:07:30', 7);
INSERT INTO `sys_oper_log` VALUES (347, '教师班级管理', 1, 'com.ruoyi.business.controller.BizTeacherClassController.batchAdd()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/teacherClass/batch', '127.0.0.1', '内网IP', '[{\"classCode\":\"1\",\"createTime\":\"2025-12-29 16:07:35\",\"deptId\":169,\"entryYear\":\"2025\",\"params\":{},\"userId\":104},{\"classCode\":\"5\",\"createTime\":\"2025-12-29 16:07:35\",\"deptId\":169,\"entryYear\":\"2025\",\"params\":{},\"userId\":104}]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-29 16:07:35', 8);
INSERT INTO `sys_oper_log` VALUES (348, '教师班级管理', 3, 'com.ruoyi.business.controller.BizTeacherClassController.remove()', 'DELETE', 1, '19157727791', '大目湾学校(初中部)', '/business/teacherClass/7', '127.0.0.1', '内网IP', '[7]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-29 18:23:12', 43);
INSERT INTO `sys_oper_log` VALUES (349, '教师班级管理', 3, 'com.ruoyi.business.controller.BizTeacherClassController.remove()', 'DELETE', 1, '19157727791', '大目湾学校(初中部)', '/business/teacherClass/6', '127.0.0.1', '内网IP', '[6]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-29 18:23:14', 13);
INSERT INTO `sys_oper_log` VALUES (350, '教师班级管理', 1, 'com.ruoyi.business.controller.BizTeacherClassController.batchAdd()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/teacherClass/batch', '127.0.0.1', '内网IP', '[{\"classCode\":\"2\",\"createTime\":\"2025-12-29 18:23:24\",\"deptId\":169,\"entryYear\":\"2024\",\"params\":{},\"userId\":104},{\"classCode\":\"1\",\"createTime\":\"2025-12-29 18:23:24\",\"deptId\":169,\"entryYear\":\"2024\",\"params\":{},\"userId\":104}]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-29 18:23:24', 21);
INSERT INTO `sys_oper_log` VALUES (351, '教师班级管理', 1, 'com.ruoyi.business.controller.BizTeacherClassController.batchAdd()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/teacherClass/batch', '127.0.0.1', '内网IP', '[{\"classCode\":\"1\",\"createTime\":\"2025-12-29 18:23:41\",\"deptId\":169,\"entryYear\":\"2025\",\"params\":{},\"userId\":104},{\"classCode\":\"5\",\"createTime\":\"2025-12-29 18:23:41\",\"deptId\":169,\"entryYear\":\"2025\",\"params\":{},\"userId\":104},{\"classCode\":\"3\",\"createTime\":\"2025-12-29 18:23:41\",\"deptId\":169,\"entryYear\":\"2024\",\"params\":{},\"userId\":104}]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-29 18:23:41', 15);
INSERT INTO `sys_oper_log` VALUES (352, '教师班级管理', 3, 'com.ruoyi.business.controller.BizTeacherClassController.remove()', 'DELETE', 1, '19157727791', '大目湾学校(初中部)', '/business/teacherClass/10', '127.0.0.1', '内网IP', '[10]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-29 18:35:20', 47);
INSERT INTO `sys_oper_log` VALUES (353, '教师班级管理', 3, 'com.ruoyi.business.controller.BizTeacherClassController.remove()', 'DELETE', 1, '19157727791', '大目湾学校(初中部)', '/business/teacherClass/11', '127.0.0.1', '内网IP', '[11]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-29 18:35:21', 12);
INSERT INTO `sys_oper_log` VALUES (354, '教师班级管理', 1, 'com.ruoyi.business.controller.BizTeacherClassController.batchAdd()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/teacherClass/batch', '127.0.0.1', '内网IP', '[{\"classCode\":\"1\",\"createTime\":\"2025-12-29 18:35:31\",\"deptId\":169,\"entryYear\":\"2025\",\"params\":{},\"userId\":104},{\"classCode\":\"5\",\"createTime\":\"2025-12-29 18:35:31\",\"deptId\":169,\"entryYear\":\"2025\",\"params\":{},\"userId\":104}]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-29 18:35:31', 23);
INSERT INTO `sys_oper_log` VALUES (355, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"19157727791\",\"createTime\":\"2025-12-29 18:56:46\",\"creatorId\":104,\"grade\":7,\"isPublic\":\"Y\",\"lessonNum\":1,\"params\":{},\"questionContent\":\"fregfefg\",\"questionId\":41,\"questionType\":\"typing\",\"semester\":\"0\",\"typingDuration\":3,\"wordCount\":8}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-29 18:56:46', 48);
INSERT INTO `sys_oper_log` VALUES (356, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\"],\"grade\":7,\"lessonId\":12,\"lessonNum\":1,\"lessonTitle\":\"2w22w2w2\",\"params\":{},\"questions\":[{\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":10,\"questionType\":\"choice\"},{\"orderNum\":2,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":14,\"questionScore\":90,\"questionType\":\"judgment\"}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\"],\"grade\":7,\"lessonId\":12,\"lessonNum\":1,\"lessonTitle\":\"2w22w2w2\",\"params\":{},\"questions\":[{\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":10,\"questionType\":\"choice\"},{\"orderNum\":2,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":14,\"questionScore\":90,\"questionType\":\"judgment\"}],\"semester\":\"0\"}}', 0, NULL, '2025-12-30 10:37:53', 270);
INSERT INTO `sys_oper_log` VALUES (357, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\"],\"grade\":7,\"lessonId\":12,\"lessonNum\":1,\"lessonTitle\":\"2w22w2w2\",\"params\":{},\"questions\":[{\"lessonId\":12,\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":10,\"questionType\":\"choice\"},{\"lessonId\":12,\"orderNum\":2,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":14,\"questionScore\":90,\"questionType\":\"judgment\"}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\"],\"grade\":7,\"lessonId\":12,\"lessonNum\":1,\"lessonTitle\":\"2w22w2w2\",\"params\":{},\"questions\":[{\"lessonId\":12,\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":10,\"questionType\":\"choice\"},{\"lessonId\":12,\"orderNum\":2,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":14,\"questionScore\":90,\"questionType\":\"judgment\"}],\"semester\":\"0\"}}', 0, NULL, '2025-12-30 10:38:10', 31);
INSERT INTO `sys_oper_log` VALUES (358, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\"],\"grade\":7,\"lessonId\":12,\"lessonNum\":1,\"lessonTitle\":\"2w22w2w2\",\"params\":{},\"questions\":[{\"lessonId\":12,\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":10,\"questionType\":\"choice\"},{\"lessonId\":12,\"orderNum\":2,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":14,\"questionScore\":90,\"questionType\":\"judgment\"}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\"],\"grade\":7,\"lessonId\":12,\"lessonNum\":1,\"lessonTitle\":\"2w22w2w2\",\"params\":{},\"questions\":[{\"lessonId\":12,\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":10,\"questionType\":\"choice\"},{\"lessonId\":12,\"orderNum\":2,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":14,\"questionScore\":90,\"questionType\":\"judgment\"}],\"semester\":\"0\"}}', 0, NULL, '2025-12-30 10:38:21', 32);
INSERT INTO `sys_oper_log` VALUES (359, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\"],\"grade\":7,\"lessonId\":12,\"lessonNum\":1,\"lessonTitle\":\"2w22w2w2\",\"params\":{},\"questions\":[{\"lessonId\":12,\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":10,\"questionType\":\"choice\"},{\"lessonId\":12,\"orderNum\":2,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":14,\"questionScore\":90,\"questionType\":\"judgment\"}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\"],\"grade\":7,\"lessonId\":12,\"lessonNum\":1,\"lessonTitle\":\"2w22w2w2\",\"params\":{},\"questions\":[{\"lessonId\":12,\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":10,\"questionType\":\"choice\"},{\"lessonId\":12,\"orderNum\":2,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":14,\"questionScore\":90,\"questionType\":\"judgment\"}],\"semester\":\"0\"}}', 0, NULL, '2025-12-30 10:42:03', 29);
INSERT INTO `sys_oper_log` VALUES (360, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":12,\"lessonNum\":1,\"lessonTitle\":\"2w22w2w2\",\"params\":{},\"questions\":[{\"lessonId\":12,\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":10,\"questionType\":\"choice\"},{\"lessonId\":12,\"orderNum\":2,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":14,\"questionScore\":90,\"questionType\":\"judgment\"}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":12,\"lessonNum\":1,\"lessonTitle\":\"2w22w2w2\",\"params\":{},\"questions\":[{\"lessonId\":12,\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":10,\"questionType\":\"choice\"},{\"lessonId\":12,\"orderNum\":2,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":14,\"questionScore\":90,\"questionType\":\"judgment\"}],\"semester\":\"0\"}}', 0, NULL, '2025-12-30 10:42:09', 29);
INSERT INTO `sys_oper_log` VALUES (361, '用户管理', 1, 'com.ruoyi.web.controller.system.SysUserController.add()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"createBy\":\"19157727791\",\"deptId\":169,\"deptIds\":[169],\"nickName\":\"叶\",\"params\":{},\"postIds\":[],\"roleIds\":[100],\"sex\":\"1\",\"status\":\"0\",\"userId\":166,\"userName\":\"yelaoshi\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-30 10:44:53', 115);
INSERT INTO `sys_oper_log` VALUES (362, '教师班级管理', 1, 'com.ruoyi.business.controller.BizTeacherClassController.batchAdd()', 'POST', 1, 'yelaoshi', '大目湾学校(初中部)', '/business/teacherClass/batch', '127.0.0.1', '内网IP', '[{\"classCode\":\"1\",\"createTime\":\"2025-12-30 10:45:29\",\"deptId\":169,\"entryYear\":\"2025\",\"params\":{},\"userId\":166},{\"classCode\":\"5\",\"createTime\":\"2025-12-30 10:45:29\",\"deptId\":169,\"entryYear\":\"2025\",\"params\":{},\"userId\":166},{\"classCode\":\"1\",\"createTime\":\"2025-12-30 10:45:29\",\"deptId\":169,\"entryYear\":\"2024\",\"params\":{},\"userId\":166}]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-30 10:45:29', 19);
INSERT INTO `sys_oper_log` VALUES (363, '教师班级管理', 3, 'com.ruoyi.business.controller.BizTeacherClassController.remove()', 'DELETE', 1, 'yelaoshi', '大目湾学校(初中部)', '/business/teacherClass/17', '127.0.0.1', '内网IP', '[17]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-30 14:02:20', 27);
INSERT INTO `sys_oper_log` VALUES (364, '教师班级管理', 3, 'com.ruoyi.business.controller.BizTeacherClassController.remove()', 'DELETE', 1, 'yelaoshi', '大目湾学校(初中部)', '/business/teacherClass/16', '127.0.0.1', '内网IP', '[16]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-30 14:02:22', 8);
INSERT INTO `sys_oper_log` VALUES (365, '教师班级管理', 3, 'com.ruoyi.business.controller.BizTeacherClassController.remove()', 'DELETE', 1, 'yelaoshi', '大目湾学校(初中部)', '/business/teacherClass/15', '127.0.0.1', '内网IP', '[15]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-30 14:02:24', 8);
INSERT INTO `sys_oper_log` VALUES (366, '教师班级管理', 1, 'com.ruoyi.business.controller.BizTeacherClassController.batchAdd()', 'POST', 1, 'yelaoshi', '大目湾学校(初中部)', '/business/teacherClass/batch', '127.0.0.1', '内网IP', '[{\"classCode\":\"1\",\"createTime\":\"2025-12-30 14:02:34\",\"deptId\":169,\"entryYear\":\"2025\",\"params\":{},\"userId\":166},{\"classCode\":\"5\",\"createTime\":\"2025-12-30 14:02:34\",\"deptId\":169,\"entryYear\":\"2025\",\"params\":{},\"userId\":166}]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-30 14:02:34', 15);
INSERT INTO `sys_oper_log` VALUES (367, '教师班级管理', 1, 'com.ruoyi.business.controller.BizTeacherClassController.batchAdd()', 'POST', 1, 'yelaoshi', '大目湾学校(初中部)', '/business/teacherClass/batch', '127.0.0.1', '内网IP', '[{\"classCode\":\"1\",\"createTime\":\"2025-12-30 14:02:46\",\"deptId\":169,\"entryYear\":\"2024\",\"params\":{},\"userId\":166},{\"classCode\":\"2\",\"createTime\":\"2025-12-30 14:02:46\",\"deptId\":169,\"entryYear\":\"2024\",\"params\":{},\"userId\":166},{\"classCode\":\"3\",\"createTime\":\"2025-12-30 14:02:46\",\"deptId\":169,\"entryYear\":\"2024\",\"params\":{},\"userId\":166}]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-30 14:02:46', 10);
INSERT INTO `sys_oper_log` VALUES (368, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, 'yelaoshi', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":13,\"lessonNum\":1,\"lessonTitle\":\"3333333333333\",\"params\":{},\"questions\":[{\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":10,\"questionType\":\"choice\"},{\"orderNum\":2,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":17,\"questionScore\":90,\"questionType\":\"judgment\"}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":13,\"lessonNum\":1,\"lessonTitle\":\"3333333333333\",\"params\":{},\"questions\":[{\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":10,\"questionType\":\"choice\"},{\"orderNum\":2,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":17,\"questionScore\":90,\"questionType\":\"judgment\"}],\"semester\":\"0\"}}', 0, NULL, '2025-12-30 14:03:07', 84);
INSERT INTO `sys_oper_log` VALUES (369, '课程管理', 3, 'com.ruoyi.business.controller.BizLessonController.remove()', 'DELETE', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/7', '127.0.0.1', '内网IP', '[7]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-30 15:03:23', 52);
INSERT INTO `sys_oper_log` VALUES (370, '课程管理', 3, 'com.ruoyi.business.controller.BizLessonController.remove()', 'DELETE', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/10', '127.0.0.1', '内网IP', '[10]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-30 15:03:26', 674);
INSERT INTO `sys_oper_log` VALUES (371, '课程管理', 3, 'com.ruoyi.business.controller.BizLessonController.remove()', 'DELETE', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/11', '127.0.0.1', '内网IP', '[11]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-30 15:03:28', 22);
INSERT INTO `sys_oper_log` VALUES (372, '课程管理', 3, 'com.ruoyi.business.controller.BizLessonController.remove()', 'DELETE', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/12', '127.0.0.1', '内网IP', '[12]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-30 15:03:30', 15);
INSERT INTO `sys_oper_log` VALUES (373, '课程管理', 3, 'com.ruoyi.business.controller.BizLessonController.remove()', 'DELETE', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/13', '127.0.0.1', '内网IP', '[13]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-30 15:03:32', 13);
INSERT INTO `sys_oper_log` VALUES (374, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":14,\"lessonNum\":1,\"lessonTitle\":\"11111111\",\"params\":{},\"questions\":[{\"answer\":\"A\",\"optionA\":\"a\",\"optionB\":\"b\",\"optionC\":\"cv\",\"optionD\":\"d\",\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":10,\"questionType\":\"choice\"},{\"answer\":\"F\",\"optionA\":\"\",\"optionB\":\"\",\"optionC\":\"\",\"optionD\":\"\",\"orderNum\":2,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":14,\"questionScore\":90,\"questionType\":\"judgment\"}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":14,\"lessonNum\":1,\"lessonTitle\":\"11111111\",\"params\":{},\"questions\":[{\"answer\":\"A\",\"optionA\":\"a\",\"optionB\":\"b\",\"optionC\":\"cv\",\"optionD\":\"d\",\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":10,\"questionType\":\"choice\"},{\"answer\":\"F\",\"orderNum\":2,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":14,\"questionScore\":90,\"questionType\":\"judgment\"}],\"semester\":\"0\"}}', 0, NULL, '2025-12-30 15:04:13', 46);
INSERT INTO `sys_oper_log` VALUES (375, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, 'yelaoshi', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":15,\"lessonNum\":1,\"lessonTitle\":\"222222\",\"params\":{},\"questions\":[{\"answer\":\"A\",\"optionA\":\"a\",\"optionB\":\"b\",\"optionC\":\"cv\",\"optionD\":\"d\",\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":100,\"questionType\":\"choice\"}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":15,\"lessonNum\":1,\"lessonTitle\":\"222222\",\"params\":{},\"questions\":[{\"answer\":\"A\",\"optionA\":\"a\",\"optionB\":\"b\",\"optionC\":\"cv\",\"optionD\":\"d\",\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":100,\"questionType\":\"choice\"}],\"semester\":\"0\"}}', 0, NULL, '2025-12-30 15:04:59', 30);
INSERT INTO `sys_oper_log` VALUES (376, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":15,\"lessonNum\":1,\"lessonTitle\":\"222222\",\"params\":{},\"questions\":[{\"answer\":\"A\",\"lessonId\":15,\"optionA\":\"a\",\"optionB\":\"b\",\"optionC\":\"cv\",\"optionD\":\"d\",\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":100,\"questionType\":\"choice\"}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":15,\"lessonNum\":1,\"lessonTitle\":\"222222\",\"params\":{},\"questions\":[{\"answer\":\"A\",\"lessonId\":15,\"optionA\":\"a\",\"optionB\":\"b\",\"optionC\":\"cv\",\"optionD\":\"d\",\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":100,\"questionType\":\"choice\"}],\"semester\":\"0\"}}', 0, NULL, '2025-12-30 16:09:37', 87);
INSERT INTO `sys_oper_log` VALUES (377, '课程管理', 3, 'com.ruoyi.business.controller.BizLessonController.remove()', 'DELETE', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/14', '127.0.0.1', '内网IP', '[14]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-30 16:09:49', 40);
INSERT INTO `sys_oper_log` VALUES (378, '课程管理', 3, 'com.ruoyi.business.controller.BizLessonController.remove()', 'DELETE', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/15', '127.0.0.1', '内网IP', '[15]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-30 16:09:52', 23);
INSERT INTO `sys_oper_log` VALUES (379, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":16,\"lessonNum\":1,\"lessonTitle\":\"1010\",\"params\":{},\"questions\":[{\"answer\":\"A\",\"optionA\":\"a\",\"optionB\":\"b\",\"optionC\":\"cv\",\"optionD\":\"d\",\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":100,\"questionType\":\"choice\"}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":16,\"lessonNum\":1,\"lessonTitle\":\"1010\",\"params\":{},\"questions\":[{\"answer\":\"A\",\"optionA\":\"a\",\"optionB\":\"b\",\"optionC\":\"cv\",\"optionD\":\"d\",\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":100,\"questionType\":\"choice\"}],\"semester\":\"0\"}}', 0, NULL, '2025-12-30 16:13:59', 315);
INSERT INTO `sys_oper_log` VALUES (380, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"5班\"],\"grade\":7,\"lessonId\":17,\"lessonNum\":1,\"lessonTitle\":\"2020\",\"params\":{},\"questions\":[],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"5班\"],\"grade\":7,\"lessonId\":17,\"lessonNum\":1,\"lessonTitle\":\"2020\",\"params\":{},\"questions\":[],\"semester\":\"0\"}}', 0, NULL, '2025-12-30 16:14:19', 36);
INSERT INTO `sys_oper_log` VALUES (381, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":18,\"lessonNum\":3,\"lessonTitle\":\"始业教育\",\"params\":{},\"questions\":[{\"orderNum\":1,\"params\":{},\"questionContent\":\"而感染尴尬而广泛认同过热\",\"questionId\":40,\"questionScore\":100,\"questionType\":\"typing\",\"typingDuration\":4,\"wordCount\":12}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":18,\"lessonNum\":3,\"lessonTitle\":\"始业教育\",\"params\":{},\"questions\":[{\"orderNum\":1,\"params\":{},\"questionContent\":\"而感染尴尬而广泛认同过热\",\"questionId\":40,\"questionScore\":100,\"questionType\":\"typing\",\"typingDuration\":4,\"wordCount\":12}],\"semester\":\"0\"}}', 0, NULL, '2025-12-30 16:32:09', 37);
INSERT INTO `sys_oper_log` VALUES (382, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":18,\"lessonNum\":3,\"lessonTitle\":\"始业教育\",\"params\":{},\"questions\":[{\"lessonId\":18,\"orderNum\":1,\"params\":{},\"questionContent\":\"而感染尴尬而广泛认同过热\",\"questionId\":40,\"questionScore\":50,\"questionType\":\"typing\",\"typingDuration\":4,\"wordCount\":12},{\"answer\":\"A\",\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":2,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":15,\"questionScore\":50,\"questionType\":\"choice\"}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":18,\"lessonNum\":3,\"lessonTitle\":\"始业教育\",\"params\":{},\"questions\":[{\"lessonId\":18,\"orderNum\":1,\"params\":{},\"questionContent\":\"而感染尴尬而广泛认同过热\",\"questionId\":40,\"questionScore\":50,\"questionType\":\"typing\",\"typingDuration\":4,\"wordCount\":12},{\"answer\":\"A\",\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":2,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":15,\"questionScore\":50,\"questionType\":\"choice\"}],\"semester\":\"0\"}}', 0, NULL, '2025-12-30 16:32:34', 41);
INSERT INTO `sys_oper_log` VALUES (383, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":18,\"lessonNum\":3,\"lessonTitle\":\"始业教育\",\"params\":{},\"questions\":[{\"lessonId\":18,\"orderNum\":1,\"params\":{},\"questionContent\":\"而感染尴尬而广泛认同过热\",\"questionId\":40,\"questionScore\":50,\"questionType\":\"typing\",\"typingDuration\":4,\"wordCount\":12},{\"answer\":\"A\",\"lessonId\":18,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":2,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":15,\"questionScore\":50,\"questionType\":\"choice\"}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":18,\"lessonNum\":3,\"lessonTitle\":\"始业教育\",\"params\":{},\"questions\":[{\"lessonId\":18,\"orderNum\":1,\"params\":{},\"questionContent\":\"而感染尴尬而广泛认同过热\",\"questionId\":40,\"questionScore\":50,\"questionType\":\"typing\",\"typingDuration\":4,\"wordCount\":12},{\"answer\":\"A\",\"lessonId\":18,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":2,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":15,\"questionScore\":50,\"questionType\":\"choice\"}],\"semester\":\"0\"}}', 0, NULL, '2025-12-31 10:37:45', 102);
INSERT INTO `sys_oper_log` VALUES (384, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"19157727791\",\"createTime\":\"2026-01-05 08:37:20\",\"creatorId\":104,\"grade\":7,\"isPublic\":\"Y\",\"lessonNum\":1,\"params\":{},\"questionContent\":\"你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是\",\"questionId\":42,\"questionType\":\"typing\",\"semester\":\"0\",\"typingDuration\":4,\"wordCount\":612}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-05 08:37:20', 63);
INSERT INTO `sys_oper_log` VALUES (385, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":18,\"lessonNum\":3,\"lessonTitle\":\"始业教育\",\"params\":{},\"questions\":[{\"answer\":\"A\",\"lessonId\":18,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":2,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":15,\"questionScore\":50,\"questionType\":\"choice\"},{\"orderNum\":2,\"params\":{},\"questionContent\":\"你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是\",\"questionId\":42,\"questionScore\":50,\"questionType\":\"typing\",\"typingDuration\":4,\"wordCount\":612}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":18,\"lessonNum\":3,\"lessonTitle\":\"始业教育\",\"params\":{},\"questions\":[{\"answer\":\"A\",\"lessonId\":18,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":2,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":15,\"questionScore\":50,\"questionType\":\"choice\"},{\"orderNum\":2,\"params\":{},\"questionContent\":\"你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是\",\"questionId\":42,\"questionScore\":50,\"questionType\":\"typing\",\"typingDuration\":4,\"wordCount\":612}],\"semester\":\"0\"}}', 0, NULL, '2026-01-05 08:37:52', 73);
INSERT INTO `sys_oper_log` VALUES (386, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"19157727791\",\"createTime\":\"2026-01-05 08:46:42\",\"creatorId\":104,\"grade\":7,\"isPublic\":\"Y\",\"lessonNum\":4,\"params\":{},\"questionContent\":\"一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。\",\"questionId\":43,\"questionType\":\"typing\",\"semester\":\"0\",\"typingDuration\":3,\"wordCount\":189}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-05 08:46:42', 13);
INSERT INTO `sys_oper_log` VALUES (387, '题库管理', 2, 'com.ruoyi.business.controller.BizQuestionController.edit()', 'PUT', 1, '19157727791', '大目湾学校(初中部)', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"19157727791\",\"createTime\":\"2026-01-05 08:46:43\",\"creatorId\":104,\"grade\":7,\"isPublic\":\"Y\",\"lessonNum\":4,\"params\":{},\"questionContent\":\"一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。\",\"questionId\":43,\"questionType\":\"typing\",\"semester\":\"0\",\"typingDuration\":5,\"updateBy\":\"19157727791\",\"updateTime\":\"2026-01-05 08:47:39\",\"wordCount\":189}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-05 08:47:39', 11);
INSERT INTO `sys_oper_log` VALUES (388, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":18,\"lessonNum\":3,\"lessonTitle\":\"始业教育\",\"params\":{},\"questions\":[{\"answer\":\"A\",\"lessonId\":18,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":2,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":15,\"questionScore\":50,\"questionType\":\"choice\"},{\"orderNum\":2,\"params\":{},\"questionContent\":\"一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。\",\"questionId\":43,\"questionScore\":50,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":189}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":18,\"lessonNum\":3,\"lessonTitle\":\"始业教育\",\"params\":{},\"questions\":[{\"answer\":\"A\",\"lessonId\":18,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":2,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":15,\"questionScore\":50,\"questionType\":\"choice\"},{\"orderNum\":2,\"params\":{},\"questionContent\":\"一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。\",\"questionId\":43,\"questionScore\":50,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":189}],\"semester\":\"0\"}}', 0, NULL, '2026-01-05 08:48:18', 38);
INSERT INTO `sys_oper_log` VALUES (389, '题库管理', 3, 'com.ruoyi.business.controller.BizQuestionController.remove()', 'DELETE', 1, '19157727791', '大目湾学校(初中部)', '/business/question/3,8,25,30,31,34,36,37,38,39', '127.0.0.1', '内网IP', '[3,8,25,30,31,34,36,37,38,39]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-05 15:01:01', 34);
INSERT INTO `sys_oper_log` VALUES (390, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"19157727791\",\"createTime\":\"2026-01-05 15:01:31\",\"creatorId\":104,\"filePath\":\"/profile/upload/2026/01/05/附表2：作品创作说明_20260105150125A001.docx\",\"grade\":7,\"isPublic\":\"Y\",\"lessonNum\":1,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/05/附表2：作品创作说明_20260105150125A001.pdf\",\"questionContent\":\"吱吱吱吱\",\"questionId\":44,\"questionType\":\"practical\",\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-05 15:01:32', 1050);
INSERT INTO `sys_oper_log` VALUES (391, '题库管理', 3, 'com.ruoyi.business.controller.BizQuestionController.remove()', 'DELETE', 1, '19157727791', '大目湾学校(初中部)', '/business/question/44', '127.0.0.1', '内网IP', '[44]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-05 15:23:51', 52);
INSERT INTO `sys_oper_log` VALUES (392, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"19157727791\",\"createTime\":\"2026-01-05 15:24:11\",\"creatorId\":104,\"filePath\":\"/profile/upload/2026/01/05/附件1_20260105152408A001.docx\",\"grade\":7,\"isPublic\":\"Y\",\"lessonNum\":3,\"params\":{},\"previewPath\":\"/profile./uploadPath/upload/2026/01/05/附件1_20260105152408A001.pdf\",\"questionContent\":\"是公司架构\",\"questionId\":45,\"questionType\":\"practical\",\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-05 15:24:27', 15616);
INSERT INTO `sys_oper_log` VALUES (393, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"19157727791\",\"createTime\":\"2026-01-05 15:28:58\",\"creatorId\":104,\"filePath\":\"/profile/upload/2026/01/05/附件1_20260105152856A001.docx\",\"grade\":7,\"isPublic\":\"Y\",\"lessonNum\":4,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/05/附件1_20260105152856A001.pdf\",\"questionContent\":\"山地车山地车山地车\",\"questionId\":46,\"questionType\":\"practical\",\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-05 15:29:09', 11446);
INSERT INTO `sys_oper_log` VALUES (394, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"19157727791\",\"createTime\":\"2026-01-05 15:33:17\",\"creatorId\":104,\"filePath\":\"/profile/upload/2026/01/05/附表2：作品创作说明_20260105153315A002.docx\",\"grade\":7,\"isPublic\":\"Y\",\"lessonNum\":6,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/05/附表2：作品创作说明_20260105153315A002.pdf\",\"questionContent\":\"等等\",\"questionId\":47,\"questionType\":\"practical\",\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-05 15:33:29', 11943);
INSERT INTO `sys_oper_log` VALUES (395, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":18,\"lessonNum\":3,\"lessonTitle\":\"始业教育\",\"params\":{},\"questions\":[{\"answer\":\"A\",\"lessonId\":18,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":2,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":15,\"questionScore\":50,\"questionType\":\"choice\"},{\"lessonId\":18,\"orderNum\":2,\"params\":{},\"questionContent\":\"一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。\",\"questionId\":43,\"questionScore\":30,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":189},{\"orderNum\":3,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/05/附表2：作品创作说明_20260105153315A002.pdf\",\"questionContent\":\"等等\",\"questionId\":47,\"questionScore\":10,\"questionType\":\"practical\"},{\"answer\":\"F\",\"optionA\":\"\",\"optionB\":\"\",\"optionC\":\"\",\"optionD\":\"\",\"orderNum\":4,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":24,\"questionScore\":10,\"questionType\":\"judgment\"}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":18,\"lessonNum\":3,\"lessonTitle\":\"始业教育\",\"params\":{},\"questions\":[{\"answer\":\"A\",\"lessonId\":18,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":2,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":15,\"questionScore\":50,\"questionType\":\"choice\"},{\"lessonId\":18,\"orderNum\":2,\"params\":{},\"questionContent\":\"一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。\",\"questionId\":43,\"questionScore\":30,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":189},{\"orderNum\":3,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/05/附表2：作品创作说明_20260105153315A002.pdf\",\"questionContent\":\"等等\",\"questionId\":47,\"questionScore\":10,\"questionType\":\"practical\"},{\"answer\":\"F\",\"orderNum\":4,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":24,\"questionScore\":10,\"questionType\":\"judgment\"}],\"semester\":\"0\"}}', 0, NULL, '2026-01-05 15:35:06', 105);
INSERT INTO `sys_oper_log` VALUES (396, '个人信息', 2, 'com.ruoyi.web.controller.system.SysProfileController.updatePwd()', 'PUT', 1, '2025720506', '大目湾学校(初中部)', '/system/user/profile/updatePwd', '127.0.0.1', '内网IP', '{}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-06 11:45:26', 263);
INSERT INTO `sys_oper_log` VALUES (397, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"19157727791\",\"createTime\":\"2026-01-07 14:20:24\",\"creatorId\":104,\"filePath\":\"/profile/upload/2026/01/07/物品申请单_20260107141913A001.docx\",\"grade\":7,\"isPublic\":\"Y\",\"lessonNum\":1,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/07/物品申请单_20260107141913A001.pdf\",\"questionContent\":\"任务操作1\",\"questionId\":48,\"questionType\":\"practical\",\"scoringItems\":[{\"itemId\":1,\"itemName\":\"界面设计\",\"itemScore\":10,\"orderNum\":0,\"params\":{},\"questionId\":48},{\"itemId\":2,\"itemName\":\"答案是否正确\",\"itemScore\":10,\"orderNum\":1,\"params\":{},\"questionId\":48},{\"itemId\":3,\"itemName\":\"合理吗\",\"itemScore\":10,\"orderNum\":2,\"params\":{},\"questionId\":48}],\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-07 14:20:40', 15609);
INSERT INTO `sys_oper_log` VALUES (398, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":18,\"lessonNum\":3,\"lessonTitle\":\"始业教育\",\"params\":{},\"questions\":[{\"answer\":\"A\",\"lessonId\":18,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":2,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":15,\"questionScore\":50,\"questionType\":\"choice\"},{\"lessonId\":18,\"orderNum\":2,\"params\":{},\"questionContent\":\"一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。\",\"questionId\":43,\"questionScore\":30,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":189},{\"answer\":\"F\",\"lessonId\":18,\"optionA\":\"\",\"optionB\":\"\",\"optionC\":\"\",\"optionD\":\"\",\"orderNum\":4,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":24,\"questionScore\":10,\"questionType\":\"judgment\"},{\"orderNum\":4,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/07/物品申请单_20260107141913A001.pdf\",\"questionContent\":\"任务操作1\",\"questionId\":48,\"questionScore\":10,\"questionType\":\"practical\"}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":18,\"lessonNum\":3,\"lessonTitle\":\"始业教育\",\"params\":{},\"questions\":[{\"answer\":\"A\",\"lessonId\":18,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":2,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":15,\"questionScore\":50,\"questionType\":\"choice\"},{\"lessonId\":18,\"orderNum\":2,\"params\":{},\"questionContent\":\"一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。\",\"questionId\":43,\"questionScore\":30,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":189},{\"answer\":\"F\",\"lessonId\":18,\"orderNum\":4,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":24,\"questionScore\":10,\"questionType\":\"judgment\"},{\"orderNum\":4,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/07/物品申请单_20260107141913A001.pdf\",\"questionContent\":\"任务操作1\",\"questionId\":48,\"questionScore\":10,\"questionType\":\"practical\"}],\"semester\":\"0\"}}', 0, NULL, '2026-01-07 14:21:45', 122);
INSERT INTO `sys_oper_log` VALUES (399, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"19157727791\",\"createTime\":\"2026-01-07 16:29:55\",\"creatorId\":104,\"filePath\":\"/profile/upload/2026/01/07/大模型本地部署配置_20260107162900A001.docx\",\"grade\":7,\"isPublic\":\"Y\",\"lessonNum\":5,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/07/大模型本地部署配置_20260107162900A001.pdf\",\"questionContent\":\"zdx\",\"questionId\":49,\"questionType\":\"practical\",\"scoringItems\":[{\"itemId\":4,\"itemName\":\"界面\",\"itemScore\":50,\"orderNum\":0,\"params\":{},\"questionId\":49},{\"itemId\":5,\"itemName\":\"是否正确\",\"itemScore\":10,\"orderNum\":1,\"params\":{},\"questionId\":49}],\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-07 16:30:10', 15596);
INSERT INTO `sys_oper_log` VALUES (400, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"19157727791\",\"createTime\":\"2026-01-07 16:38:15\",\"creatorId\":104,\"filePath\":\"/profile/upload/2026/01/07/物品申请单_20260107163743A002.docx\",\"grade\":7,\"isPublic\":\"Y\",\"lessonNum\":2,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/07/物品申请单_20260107163743A002.pdf\",\"questionContent\":\"郑东旭\",\"questionId\":50,\"questionType\":\"practical\",\"scoringItems\":[{\"itemId\":6,\"itemName\":\"界面\",\"itemScore\":40,\"orderNum\":0,\"params\":{},\"questionId\":50},{\"itemId\":7,\"itemName\":\"你还哦\",\"itemScore\":60,\"orderNum\":1,\"params\":{},\"questionId\":50}],\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-07 16:38:27', 11413);
INSERT INTO `sys_oper_log` VALUES (401, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":19,\"lessonNum\":1,\"lessonTitle\":\"第二课\",\"params\":{},\"questions\":[{\"orderNum\":1,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/07/物品申请单_20260107163743A002.pdf\",\"questionContent\":\"郑东旭\",\"questionId\":50,\"questionScore\":50,\"questionType\":\"practical\"},{\"answer\":\"A\",\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":2,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":15,\"questionScore\":40,\"questionType\":\"choice\"},{\"orderNum\":3,\"params\":{},\"questionContent\":\"一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。\",\"questionId\":43,\"questionScore\":10,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":189}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":19,\"lessonNum\":1,\"lessonTitle\":\"第二课\",\"params\":{},\"questions\":[{\"orderNum\":1,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/07/物品申请单_20260107163743A002.pdf\",\"questionContent\":\"郑东旭\",\"questionId\":50,\"questionScore\":50,\"questionType\":\"practical\"},{\"answer\":\"A\",\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":2,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":15,\"questionScore\":40,\"questionType\":\"choice\"},{\"orderNum\":3,\"params\":{},\"questionContent\":\"一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。\",\"questionId\":43,\"questionScore\":10,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":189}],\"semester\":\"0\"}}', 0, NULL, '2026-01-07 16:39:58', 88);
INSERT INTO `sys_oper_log` VALUES (402, '学生管理', 1, 'com.ruoyi.business.controller.BizStudentController.add()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/student', '127.0.0.1', '内网IP', '{\"classCode\":\"5\",\"entryYear\":\"2025\",\"params\":{},\"studentId\":61,\"studentName\":\"真实姓名呢\",\"studentNo\":\"1\",\"userId\":167}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-07 16:42:09', 105);
INSERT INTO `sys_oper_log` VALUES (403, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"19157727791\",\"createTime\":\"2026-01-08 08:55:24\",\"creatorId\":104,\"filePath\":\"/profile/upload/2026/01/08/111_20260108085453A001.docx\",\"grade\":7,\"isPublic\":\"Y\",\"lessonNum\":7,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/08/111_20260108085453A001.pdf\",\"questionContent\":\"操作18\",\"questionId\":51,\"questionType\":\"practical\",\"scoringItems\":[{\"itemId\":8,\"itemName\":\"界面\",\"itemScore\":60,\"orderNum\":0,\"params\":{},\"questionId\":51},{\"itemId\":9,\"itemName\":\"答案\",\"itemScore\":40,\"orderNum\":1,\"params\":{},\"questionId\":51}],\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-08 08:55:38', 14384);
INSERT INTO `sys_oper_log` VALUES (404, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":20,\"lessonNum\":1,\"lessonTitle\":\"第三课\",\"params\":{},\"questions\":[{\"orderNum\":1,\"params\":{},\"questionContent\":\"一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。\",\"questionId\":43,\"questionScore\":30,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":189},{\"answer\":\"A\",\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":2,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":15,\"questionScore\":10,\"questionType\":\"choice\"},{\"answer\":\"F\",\"optionA\":\"\",\"optionB\":\"\",\"optionC\":\"\",\"optionD\":\"\",\"orderNum\":3,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":17,\"questionScore\":10,\"questionType\":\"judgment\"},{\"orderNum\":4,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/08/111_20260108085453A001.pdf\",\"questionContent\":\"操作18\",\"questionId\":51,\"questionScore\":50,\"questionType\":\"practical\"}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":20,\"lessonNum\":1,\"lessonTitle\":\"第三课\",\"params\":{},\"questions\":[{\"orderNum\":1,\"params\":{},\"questionContent\":\"一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。\",\"questionId\":43,\"questionScore\":30,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":189},{\"answer\":\"A\",\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":2,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":15,\"questionScore\":10,\"questionType\":\"choice\"},{\"answer\":\"F\",\"orderNum\":3,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":17,\"questionScore\":10,\"questionType\":\"judgment\"},{\"orderNum\":4,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/08/111_20260108085453A001.pdf\",\"questionContent\":\"操作18\",\"questionId\":51,\"questionScore\":50,\"questionType\":\"practical\"}],\"semester\":\"0\"}}', 0, NULL, '2026-01-08 08:56:40', 109);
INSERT INTO `sys_oper_log` VALUES (405, '菜单管理', 1, 'com.ruoyi.web.controller.system.SysMenuController.add()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"component\":\"business/score/index\",\"createBy\":\"19157727791\",\"icon\":\"job\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuName\":\"成绩查询\",\"menuType\":\"C\",\"orderNum\":5,\"params\":{},\"parentId\":0,\"path\":\"score\",\"perms\":\"business:score:list\",\"routeName\":\"\",\"status\":\"0\",\"visible\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-08 09:44:00', 90);
INSERT INTO `sys_oper_log` VALUES (406, '角色管理', 2, 'com.ruoyi.web.controller.system.SysRoleController.edit()', 'PUT', 1, 'admin', '高中', '/system/role', '127.0.0.1', '内网IP', '{\"admin\":false,\"createTime\":\"2025-06-17 18:39:24\",\"dataScope\":\"1\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[2037,1,100,1000,1001,1002,1003,1004,1005,1006,101,1007,1008,1009,1010,1011,102,1012,1013,1014,1015,103,1016,1017,1018,1019,104,1020,1021,1022,1023,1024,105,1025,1026,1027,1028,1029,106,1030,1031,1032,1033,1034,107,1035,1036,1037,1038,108,500,1039,1040,1041,501,1042,1043,1044,1045,2018,2019,2020,2021,2022,2023,2024,2031,2032,2033,2034,2035,2036,2,109,1046,1047,1048,110,1049,1050,1051,1052,1053,1054,111,112,113,114,2006,2007,2008,2009,2010,2011,3,115,116,1055,1056,1057,1058,1059,1060,117,4,2038,2039,2040,2042,2026,2027,2028,2029,2030],\"params\":{},\"roleId\":100,\"roleKey\":\"teacher\",\"roleName\":\"教师\",\"roleSort\":0,\"status\":\"0\",\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-08 09:53:20', 98);

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post`  (
  `post_id` bigint NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `post_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位编码',
  `post_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位名称',
  `post_sort` int NOT NULL COMMENT '显示顺序',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`post_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '岗位信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_post
-- ----------------------------
INSERT INTO `sys_post` VALUES (1, 'ceo', '教研员', 1, '0', 'admin', '2025-06-12 14:49:21', '19157727791', '2025-06-19 10:02:47', '');
INSERT INTO `sys_post` VALUES (2, 'se', '分区组长', 2, '0', 'admin', '2025-06-12 14:49:21', '19157727791', '2025-06-19 10:02:59', '');
INSERT INTO `sys_post` VALUES (4, 'user', '普通教师', 4, '0', 'admin', '2025-06-12 14:49:21', '19157727791', '2025-06-19 10:03:30', '');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色权限字符串',
  `role_sort` int NOT NULL COMMENT '显示顺序',
  `data_scope` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  `menu_check_strictly` tinyint(1) NULL DEFAULT 1 COMMENT '菜单树选择项是否关联显示',
  `dept_check_strictly` tinyint(1) NULL DEFAULT 1 COMMENT '部门树选择项是否关联显示',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 102 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'admin', 1, '1', 1, 1, '0', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '超级管理员');
INSERT INTO `sys_role` VALUES (2, '普通角色', 'common', 2, '2', 1, 1, '0', '2', 'admin', '2025-06-12 14:49:21', 'admin', '2025-06-17 18:36:30', '普通角色');
INSERT INTO `sys_role` VALUES (100, '教师', 'teacher', 0, '1', 1, 1, '0', '0', 'admin', '2025-06-17 18:39:24', 'admin', '2026-01-08 09:53:20', NULL);
INSERT INTO `sys_role` VALUES (101, '学生', 'student', 2, '1', 1, 1, '0', '0', 'admin', '2025-06-19 09:45:57', '', NULL, NULL);

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept`  (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `dept_id` bigint NOT NULL COMMENT '部门ID',
  PRIMARY KEY (`role_id`, `dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色和部门关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_dept
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色和菜单关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (100, 1);
INSERT INTO `sys_role_menu` VALUES (100, 2);
INSERT INTO `sys_role_menu` VALUES (100, 3);
INSERT INTO `sys_role_menu` VALUES (100, 4);
INSERT INTO `sys_role_menu` VALUES (100, 100);
INSERT INTO `sys_role_menu` VALUES (100, 101);
INSERT INTO `sys_role_menu` VALUES (100, 102);
INSERT INTO `sys_role_menu` VALUES (100, 103);
INSERT INTO `sys_role_menu` VALUES (100, 104);
INSERT INTO `sys_role_menu` VALUES (100, 105);
INSERT INTO `sys_role_menu` VALUES (100, 106);
INSERT INTO `sys_role_menu` VALUES (100, 107);
INSERT INTO `sys_role_menu` VALUES (100, 108);
INSERT INTO `sys_role_menu` VALUES (100, 109);
INSERT INTO `sys_role_menu` VALUES (100, 110);
INSERT INTO `sys_role_menu` VALUES (100, 111);
INSERT INTO `sys_role_menu` VALUES (100, 112);
INSERT INTO `sys_role_menu` VALUES (100, 113);
INSERT INTO `sys_role_menu` VALUES (100, 114);
INSERT INTO `sys_role_menu` VALUES (100, 115);
INSERT INTO `sys_role_menu` VALUES (100, 116);
INSERT INTO `sys_role_menu` VALUES (100, 117);
INSERT INTO `sys_role_menu` VALUES (100, 500);
INSERT INTO `sys_role_menu` VALUES (100, 501);
INSERT INTO `sys_role_menu` VALUES (100, 1000);
INSERT INTO `sys_role_menu` VALUES (100, 1001);
INSERT INTO `sys_role_menu` VALUES (100, 1002);
INSERT INTO `sys_role_menu` VALUES (100, 1003);
INSERT INTO `sys_role_menu` VALUES (100, 1004);
INSERT INTO `sys_role_menu` VALUES (100, 1005);
INSERT INTO `sys_role_menu` VALUES (100, 1006);
INSERT INTO `sys_role_menu` VALUES (100, 1007);
INSERT INTO `sys_role_menu` VALUES (100, 1008);
INSERT INTO `sys_role_menu` VALUES (100, 1009);
INSERT INTO `sys_role_menu` VALUES (100, 1010);
INSERT INTO `sys_role_menu` VALUES (100, 1011);
INSERT INTO `sys_role_menu` VALUES (100, 1012);
INSERT INTO `sys_role_menu` VALUES (100, 1013);
INSERT INTO `sys_role_menu` VALUES (100, 1014);
INSERT INTO `sys_role_menu` VALUES (100, 1015);
INSERT INTO `sys_role_menu` VALUES (100, 1016);
INSERT INTO `sys_role_menu` VALUES (100, 1017);
INSERT INTO `sys_role_menu` VALUES (100, 1018);
INSERT INTO `sys_role_menu` VALUES (100, 1019);
INSERT INTO `sys_role_menu` VALUES (100, 1020);
INSERT INTO `sys_role_menu` VALUES (100, 1021);
INSERT INTO `sys_role_menu` VALUES (100, 1022);
INSERT INTO `sys_role_menu` VALUES (100, 1023);
INSERT INTO `sys_role_menu` VALUES (100, 1024);
INSERT INTO `sys_role_menu` VALUES (100, 1025);
INSERT INTO `sys_role_menu` VALUES (100, 1026);
INSERT INTO `sys_role_menu` VALUES (100, 1027);
INSERT INTO `sys_role_menu` VALUES (100, 1028);
INSERT INTO `sys_role_menu` VALUES (100, 1029);
INSERT INTO `sys_role_menu` VALUES (100, 1030);
INSERT INTO `sys_role_menu` VALUES (100, 1031);
INSERT INTO `sys_role_menu` VALUES (100, 1032);
INSERT INTO `sys_role_menu` VALUES (100, 1033);
INSERT INTO `sys_role_menu` VALUES (100, 1034);
INSERT INTO `sys_role_menu` VALUES (100, 1035);
INSERT INTO `sys_role_menu` VALUES (100, 1036);
INSERT INTO `sys_role_menu` VALUES (100, 1037);
INSERT INTO `sys_role_menu` VALUES (100, 1038);
INSERT INTO `sys_role_menu` VALUES (100, 1039);
INSERT INTO `sys_role_menu` VALUES (100, 1040);
INSERT INTO `sys_role_menu` VALUES (100, 1041);
INSERT INTO `sys_role_menu` VALUES (100, 1042);
INSERT INTO `sys_role_menu` VALUES (100, 1043);
INSERT INTO `sys_role_menu` VALUES (100, 1044);
INSERT INTO `sys_role_menu` VALUES (100, 1045);
INSERT INTO `sys_role_menu` VALUES (100, 1046);
INSERT INTO `sys_role_menu` VALUES (100, 1047);
INSERT INTO `sys_role_menu` VALUES (100, 1048);
INSERT INTO `sys_role_menu` VALUES (100, 1049);
INSERT INTO `sys_role_menu` VALUES (100, 1050);
INSERT INTO `sys_role_menu` VALUES (100, 1051);
INSERT INTO `sys_role_menu` VALUES (100, 1052);
INSERT INTO `sys_role_menu` VALUES (100, 1053);
INSERT INTO `sys_role_menu` VALUES (100, 1054);
INSERT INTO `sys_role_menu` VALUES (100, 1055);
INSERT INTO `sys_role_menu` VALUES (100, 1056);
INSERT INTO `sys_role_menu` VALUES (100, 1057);
INSERT INTO `sys_role_menu` VALUES (100, 1058);
INSERT INTO `sys_role_menu` VALUES (100, 1059);
INSERT INTO `sys_role_menu` VALUES (100, 1060);
INSERT INTO `sys_role_menu` VALUES (100, 2006);
INSERT INTO `sys_role_menu` VALUES (100, 2007);
INSERT INTO `sys_role_menu` VALUES (100, 2008);
INSERT INTO `sys_role_menu` VALUES (100, 2009);
INSERT INTO `sys_role_menu` VALUES (100, 2010);
INSERT INTO `sys_role_menu` VALUES (100, 2011);
INSERT INTO `sys_role_menu` VALUES (100, 2018);
INSERT INTO `sys_role_menu` VALUES (100, 2019);
INSERT INTO `sys_role_menu` VALUES (100, 2020);
INSERT INTO `sys_role_menu` VALUES (100, 2021);
INSERT INTO `sys_role_menu` VALUES (100, 2022);
INSERT INTO `sys_role_menu` VALUES (100, 2023);
INSERT INTO `sys_role_menu` VALUES (100, 2024);
INSERT INTO `sys_role_menu` VALUES (100, 2026);
INSERT INTO `sys_role_menu` VALUES (100, 2027);
INSERT INTO `sys_role_menu` VALUES (100, 2028);
INSERT INTO `sys_role_menu` VALUES (100, 2029);
INSERT INTO `sys_role_menu` VALUES (100, 2030);
INSERT INTO `sys_role_menu` VALUES (100, 2031);
INSERT INTO `sys_role_menu` VALUES (100, 2032);
INSERT INTO `sys_role_menu` VALUES (100, 2033);
INSERT INTO `sys_role_menu` VALUES (100, 2034);
INSERT INTO `sys_role_menu` VALUES (100, 2035);
INSERT INTO `sys_role_menu` VALUES (100, 2036);
INSERT INTO `sys_role_menu` VALUES (100, 2037);
INSERT INTO `sys_role_menu` VALUES (100, 2038);
INSERT INTO `sys_role_menu` VALUES (100, 2039);
INSERT INTO `sys_role_menu` VALUES (100, 2040);
INSERT INTO `sys_role_menu` VALUES (100, 2042);
INSERT INTO `sys_role_menu` VALUES (101, 4);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '部门ID',
  `user_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户账号',
  `nick_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户昵称',
  `user_type` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '00' COMMENT '用户类型（00系统用户）',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '用户邮箱',
  `phonenumber` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '手机号码',
  `sex` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '头像地址',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '密码',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '账号状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `login_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `pwd_update_date` datetime NULL DEFAULT NULL COMMENT '密码最后更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 168 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 103, 'admin', '若依', '00', 'ry@163.com', '15888888888', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', '2026-01-08 09:48:20', '2025-06-12 14:49:21', 'admin', '2025-06-12 14:49:21', '', '2026-01-08 09:48:20', '管理员');
INSERT INTO `sys_user` VALUES (104, 169, '19157727791', '郑东旭', '00', '', '', '0', '', '$2a$10$PWHYbxojQaQh4Pi5ugZyjeXMMszlf621A5KRFASQIIc8.iwYmuAR6', '0', '0', '127.0.0.1', '2026-01-08 16:22:17', '2025-07-02 10:14:56', 'admin', '2025-06-25 10:28:28', 'admin', '2026-01-08 16:22:17', NULL);
INSERT INTO `sys_user` VALUES (105, 146, 'erzhong', '二中老师', '00', '', '', '0', '', '$2a$10$q9v/zjtdfhfRW2ja3EhPgOQYhBlYG/c8a08MSh2QZ/5gNFCcLUD9.', '0', '0', '', NULL, NULL, '19157727791', '2025-07-02 11:04:24', '19157727791', '2025-07-02 14:18:30', NULL);
INSERT INTO `sys_user` VALUES (106, NULL, '2024720401', '黄睿2', '00', '', '', '0', '', '$2a$10$TvywvPgim5EZBT3oPaEWxu9PLqsWF7U5h/WvQfXPZBzyanO2xjrOi', '0', '2', '127.0.0.1', '2025-08-07 15:54:21', '2025-08-07 15:51:26', '19157727791', '2025-07-02 14:59:34', 'admin', '2025-08-07 15:54:21', NULL);
INSERT INTO `sys_user` VALUES (107, NULL, '2024720402', '宋子旭', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:34', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (108, NULL, '2024720403', '吴锦轩', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:34', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (109, NULL, '2024720404', '蒋汶轩', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:34', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (110, NULL, '2024720405', '张海玲', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:34', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (111, NULL, '2024720406', '翁振轩', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:34', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (112, NULL, '2024720407', '孟效谦', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:34', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (113, NULL, '2024720408', '赖钇伊', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:34', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (114, NULL, '2024720409', '金可', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:34', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (115, NULL, '2024720410', '徐一宁', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:34', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (116, NULL, '2024720411', '吴依萌', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:35', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (117, NULL, '2024720412', '范梓晨', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:35', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (118, NULL, '2024720413', '陈心持', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:35', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (119, NULL, '2024720414', '欧俊毅', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:35', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (120, NULL, '2024720415', '陈炫昊', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:35', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (121, NULL, '2024720416', '罗一瑜', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:35', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (122, NULL, '2024720417', '张乐轩', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:35', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (123, NULL, '2024720418', '张子涵', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:35', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (124, NULL, '2024720419', '汪靖宇', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:35', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (125, NULL, '2024720420', '刘铭希', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:35', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (126, NULL, '2024720421', '陈弈菡', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:35', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (127, NULL, '2024720422', '顾智源', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:35', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (128, NULL, '2024720423', '昂梓熙', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:36', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (129, NULL, '2024720424', '金泽宇', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:36', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (130, NULL, '2024720425', '何川', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:36', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (131, NULL, '2024720426', '何计辰', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:36', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (132, NULL, '2024720427', '张优雅', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:36', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (133, NULL, '2024720428', '张硕涵', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:36', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (134, NULL, '2024720429', '胡羽沫', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:36', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (135, NULL, '2024720430', '刘俊贤', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:36', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (136, NULL, '2024720431', '陈思颖', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:36', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (137, NULL, '2024720432', '李长锟', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:36', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (138, NULL, '2024720433', '林徐萱', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:36', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (139, NULL, '2024720434', '陈俊宇', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:37', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (140, NULL, '2024720435', '俞梓谦', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:37', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (141, NULL, '2024720436', '史侑熙', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:37', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (142, NULL, '2024720437', '林希雨', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:37', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (143, NULL, '2024720438', '奚一诺', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:37', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (144, NULL, '2024720439', '王洪妍', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:37', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (145, NULL, '2024720440', '刘敦名', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:37', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (146, NULL, '2024720441', '陈鑫豪', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:37', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (147, NULL, '2024720442', '虞尔柏', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:37', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (148, NULL, '2024720443', '罗佳怡', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:37', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (149, NULL, '2024720444', '朱漫妮', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:37', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (150, NULL, '2024720445', '朱静琪', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:37', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (151, NULL, '2024720446', '俞航杰', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:38', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (152, NULL, '2024720447', '周祺竣', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:38', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (153, NULL, '2024720448', '吴承雪', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, '19157727791', '2025-07-02 14:59:38', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (154, 103, '20241030199', '郑东旭', '00', '', '', '0', '', '$2a$10$Pz5yMw8EUNKDff1LFIg00ukyIT9c.gPJJK9OzPvIUw.j5yMJkJzau', '0', '2', '', NULL, NULL, 'admin', '2025-08-07 15:49:32', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (155, 103, '20241030402', '重复', '00', '', '', '0', '', '$2a$10$jx3l0vNkuDrOCPlrkpdnfeTwoAbzs6y59HE8lzMD64yj5D.b.97Qu', '0', '2', '', NULL, NULL, 'admin', '2025-08-07 15:55:02', '', '2025-08-12 15:58:27', NULL);
INSERT INTO `sys_user` VALUES (156, 169, '20241690903', 'zdx', '00', '', '', '0', '', '$2a$10$S.FSuH5udjU6CjhNFBnWTeVPqRDBiwPCVmtk6pEAMsnKAs7p.bzcC', '0', '0', '', NULL, NULL, '19157727791', '2025-08-07 15:58:24', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (157, 169, '2025720101', '33', '00', '', '', '0', '', '$2a$10$VQ5y/M5Cl0Wj0sioioadwOnNrrhzXwW27j6AX6KapyCcGsNwGFyxm', '0', '2', '', NULL, NULL, '19157727791', '2025-08-07 16:24:31', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (158, 169, '2024720101', 'zdx', '00', '', '', '0', '', '$2a$10$UuytwerwE2Moml14rinmHuB8kAgh0jlb1USE4OqvTA.Quko/lgP3C', '0', '2', '', NULL, NULL, '19157727791', '2025-08-25 18:13:19', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (159, 169, '2024720101', '张1', '00', '', '', '0', '', '$2a$10$2wX90Q7U0PT0FEYwR2yaqunFI45eLdqpZ0V/QI9pcT3GyEn9i1MrS', '0', '0', '', NULL, NULL, '19157727791', '2025-08-25 20:07:11', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (160, 169, '2024720202', '张2', '00', '', '', '0', '', '$2a$10$StZI.lwI2TKVTkdVt5OxpObeO933kGfZkRaCZcQhhTtPHbRUBzdAq', '0', '0', '', NULL, NULL, '19157727791', '2025-08-25 20:07:20', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (161, 169, '2024720304', '张3', '00', '', '', '0', '', '$2a$10$P/mRbsBacmHXROWUDtdO6e/c53jocFr5SgSO90bFJ2e75ZnnF/4C2', '0', '0', '', NULL, NULL, '19157727791', '2025-08-25 20:07:30', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (162, 169, '2025720102', '张2025', '00', '', '', '0', '', '$2a$10$R4ijU2CazRVn1sXK2qDrCu4k3RyFlZ2BsOYz/mPndAsLhmx/Xs1Gy', '0', '0', '', NULL, NULL, '19157727791', '2025-08-25 20:07:41', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (163, 169, '2025720506', '张20255', '00', '', '', '0', '', '$2a$10$iS4FTMfcQgc7y5QVDx4Jrui7j4l0aEB/7NokcdaAbYwWac7Hi2VJq', '0', '0', '127.0.0.1', '2026-01-08 08:57:35', '2026-01-06 11:45:26', '19157727791', '2025-08-25 20:07:57', '', '2026-01-08 08:57:35', NULL);
INSERT INTO `sys_user` VALUES (164, 107, '202470201', 'ksj', '00', '', '', '0', '', '$2a$10$wDgjLAgL3U14mT0BqSQkK.8YIRCJkTe.Lxdz6Q5/VFXaJBnTeoRC.', '0', '0', '', NULL, NULL, '19157727791', '2025-10-08 15:01:11', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (165, 107, '202370102', 'oj', '00', '', '', '0', '', '$2a$10$QSBbMzU23cJhJ6DdFdl0ruV77odxwAxJVOugDpwRQK8odDv6kBik2', '0', '0', '', NULL, NULL, '19157727791', '2025-10-08 15:01:11', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (166, 169, 'yelaoshi', '叶', '00', '', '', '1', '', '$2a$10$eeTvwISIB/H9hmDrrWuKAu4Ot01x4GBvWokQm2papUg44a7DeckFe', '0', '0', '127.0.0.1', '2025-12-30 15:04:44', NULL, '19157727791', '2025-12-30 10:44:53', '', '2025-12-30 15:04:44', NULL);
INSERT INTO `sys_user` VALUES (167, 169, '2025720501', '真实姓名呢', '00', '', '', '0', '', '$2a$10$poNw5f1XTXzFcoK7iSCFDOKnWQGhBl6iDYPnwfJAf7f7ctL4rmUDa', '0', '0', '', NULL, NULL, '19157727791', '2026-01-07 16:42:09', '', NULL, NULL);

-- ----------------------------
-- Table structure for sys_user_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_dept`;
CREATE TABLE `sys_user_dept`  (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `dept_id` bigint NOT NULL COMMENT '部门ID',
  PRIMARY KEY (`user_id`, `dept_id`) USING BTREE,
  INDEX `idx_sys_user_dept_dept_id`(`dept_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户与部门关联' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_dept
-- ----------------------------
INSERT INTO `sys_user_dept` VALUES (104, 107);
INSERT INTO `sys_user_dept` VALUES (104, 169);
INSERT INTO `sys_user_dept` VALUES (166, 169);

-- ----------------------------
-- Table structure for sys_user_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post`  (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `post_id` bigint NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (`user_id`, `post_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户与岗位关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_post
-- ----------------------------
INSERT INTO `sys_user_post` VALUES (1, 1);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户和角色关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1);
INSERT INTO `sys_user_role` VALUES (104, 100);
INSERT INTO `sys_user_role` VALUES (156, 4);
INSERT INTO `sys_user_role` VALUES (159, 4);
INSERT INTO `sys_user_role` VALUES (160, 4);
INSERT INTO `sys_user_role` VALUES (161, 4);
INSERT INTO `sys_user_role` VALUES (162, 4);
INSERT INTO `sys_user_role` VALUES (163, 4);
INSERT INTO `sys_user_role` VALUES (164, 4);
INSERT INTO `sys_user_role` VALUES (165, 4);
INSERT INTO `sys_user_role` VALUES (166, 100);
INSERT INTO `sys_user_role` VALUES (167, 4);

SET FOREIGN_KEY_CHECKS = 1;
