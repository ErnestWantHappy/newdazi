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

 Date: 16/01/2026 15:52:21
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
  `shuffle_mode` int NULL DEFAULT 0 COMMENT '出题模式:0固定,1随机排序,2随机抽取',
  `random_choice_count` int NULL DEFAULT 0 COMMENT '随机抽取选择题数',
  `random_judgment_count` int NULL DEFAULT 0 COMMENT '随机抽取判断题数',
  PRIMARY KEY (`lesson_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课程/作业信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of biz_lesson
-- ----------------------------
INSERT INTO `biz_lesson` VALUES (1, '55', 1, '1', 5, NULL, '', '2025-08-14 09:43:54', '', NULL, 0, 0, 0);
INSERT INTO `biz_lesson` VALUES (2, '11', 7, '0', 1, 1, 'admin', '2025-08-18 21:35:48', 'admin', '2025-08-18 21:36:42', 0, 0, 0);
INSERT INTO `biz_lesson` VALUES (3, '222', 7, '1', 2, 1, 'admin', '2025-08-19 07:29:05', '', NULL, 0, 0, 0);
INSERT INTO `biz_lesson` VALUES (4, '44', 7, '1', 4, 1, 'admin', '2025-08-19 07:30:43', 'admin', '2025-08-20 09:59:53', 0, 0, 0);
INSERT INTO `biz_lesson` VALUES (21, '第一课', 7, '0', 1, NULL, '19157727791', NULL, '19157727791', NULL, 0, 0, 0);
INSERT INTO `biz_lesson` VALUES (22, '第二', 7, '0', 2, NULL, '19157727791', NULL, '19157727791', NULL, 0, 0, 0);
INSERT INTO `biz_lesson` VALUES (23, '自动控制系统', 6, '0', 1, NULL, '19157727791', NULL, '19157727791', NULL, 2, 4, 2);
INSERT INTO `biz_lesson` VALUES (24, '自动控制系统复习', 6, '0', 1, NULL, '19157727791', NULL, '19157727791', NULL, 0, 0, 0);

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
) ENGINE = InnoDB AUTO_INCREMENT = 111 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课程班级指派表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of biz_lesson_assignment
-- ----------------------------
INSERT INTO `biz_lesson_assignment` VALUES (61, 21, '2025', '5', 104, '2026-01-12 09:32:33');
INSERT INTO `biz_lesson_assignment` VALUES (62, 22, '2025', '1', 104, '2026-01-12 10:52:58');
INSERT INTO `biz_lesson_assignment` VALUES (103, 24, '2020', '1', 104, '2026-01-14 16:19:29');
INSERT INTO `biz_lesson_assignment` VALUES (104, 23, '2020', '2', 104, '2026-01-16 11:55:18');
INSERT INTO `biz_lesson_assignment` VALUES (105, 23, '2020', '4', 104, '2026-01-16 11:55:18');
INSERT INTO `biz_lesson_assignment` VALUES (106, 23, '2020', '7', 104, '2026-01-16 11:55:18');
INSERT INTO `biz_lesson_assignment` VALUES (107, 23, '2020', '8', 104, '2026-01-16 11:55:18');
INSERT INTO `biz_lesson_assignment` VALUES (108, 23, '2020', '3', 104, '2026-01-16 11:55:18');
INSERT INTO `biz_lesson_assignment` VALUES (109, 23, '2020', '5', 104, '2026-01-16 11:55:18');
INSERT INTO `biz_lesson_assignment` VALUES (110, 23, '2020', '6', 104, '2026-01-16 11:55:18');

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
) ENGINE = InnoDB AUTO_INCREMENT = 207 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课程-题目关联表' ROW_FORMAT = DYNAMIC;

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
INSERT INTO `biz_lesson_question` VALUES (79, 21, 43, 10, 1);
INSERT INTO `biz_lesson_question` VALUES (80, 21, 15, 10, 2);
INSERT INTO `biz_lesson_question` VALUES (81, 21, 29, 10, 3);
INSERT INTO `biz_lesson_question` VALUES (82, 21, 51, 70, 4);
INSERT INTO `biz_lesson_question` VALUES (83, 22, 9, 5, 1);
INSERT INTO `biz_lesson_question` VALUES (84, 22, 43, 50, 2);
INSERT INTO `biz_lesson_question` VALUES (85, 22, 14, 5, 3);
INSERT INTO `biz_lesson_question` VALUES (86, 22, 15, 5, 4);
INSERT INTO `biz_lesson_question` VALUES (87, 22, 18, 5, 5);
INSERT INTO `biz_lesson_question` VALUES (194, 24, 63, 50, 1);
INSERT INTO `biz_lesson_question` VALUES (195, 24, 62, 50, 2);
INSERT INTO `biz_lesson_question` VALUES (196, 23, 54, 40, 1);
INSERT INTO `biz_lesson_question` VALUES (197, 23, 55, 30, 2);
INSERT INTO `biz_lesson_question` VALUES (198, 23, 58, 5, 3);
INSERT INTO `biz_lesson_question` VALUES (199, 23, 57, 5, 4);
INSERT INTO `biz_lesson_question` VALUES (200, 23, 56, 5, 5);
INSERT INTO `biz_lesson_question` VALUES (201, 23, 22, 5, 6);
INSERT INTO `biz_lesson_question` VALUES (202, 23, 63, 5, 7);
INSERT INTO `biz_lesson_question` VALUES (203, 23, 61, 5, 8);
INSERT INTO `biz_lesson_question` VALUES (204, 23, 60, 5, 9);
INSERT INTO `biz_lesson_question` VALUES (205, 23, 59, 5, 10);
INSERT INTO `biz_lesson_question` VALUES (206, 23, 29, 5, 11);

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
  `preview_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '预览状态：pending/converting/success/failed',
  `is_public` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '是否公开 (0私有 1公有)',
  `typing_duration` int NULL DEFAULT NULL COMMENT '打字时长(分钟)',
  `word_count` int NULL DEFAULT NULL COMMENT '总字数',
  `creator_id` bigint NULL DEFAULT NULL COMMENT '创建教师ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`question_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 68 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '统一题库表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of biz_question
-- ----------------------------
INSERT INTO `biz_question` VALUES (2, 'typing', 'kdkewdfkewfhdksaedjhfksdfuhvbdksusghvss', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Y', NULL, NULL, NULL, '', '2025-08-18 20:17:42', '', NULL);
INSERT INTO `biz_question` VALUES (4, 'typing', '成本v的司法环境测试v白色的数据库v从就开始第一个是白醋央视播出v就爱吃v吧而艰苦程度高于把苏永康VS从啊电池健康素养干哈八成功的的介绍客户参观巴萨大哭一场vu可以减肥成功就爱读书u会计初级的sys从数据库约定俗成啊u开始从访问速度快也是从v诉苦从v的快速有擦拭的库存苏看得出洒督促v发给多少库存v有发给谁毒抗蚜虫FSUKCFGU空间互踩法国的夙愿吃饭撒大开车的师傅他u撒督陈v发撒法看得出的素材v是SDFDCSUKY FGCSDAU6CAUSDC UASSJCVF GASDUCFDSUYCTFGVDS CJSUDCF USDCY AUACDY FDUCSF DSUCFDU CSDUSA CFADJYHSFGDCJUYSDCSAJ好几个v还记得上次是v成就感刀剑神域程度也是上次考试高分萨克创建VS就参与的素材是v方式督促v是督促萨克调查v从v苦于该发生的v看u哭的v阿萨', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '0', NULL, NULL, NULL, '', '2025-08-19 07:46:05', '', NULL);
INSERT INTO `biz_question` VALUES (5, 'choice', '<p><img src=\"/dev-api/profile/upload/2025/08/20/4_20250820110019A001.jpg\"></p><p>和规范化非常</p>', NULL, NULL, NULL, 'a', 'b', 'c', 'd', 'A', '<p><img src=\"/dev-api/profile/upload/2025/08/20/Quicker_20240827_151948_20250820110048A002.png\"></p>', NULL, NULL, NULL, 'Y', NULL, NULL, 1, 'admin', '2025-08-20 11:00:50', '', NULL);
INSERT INTO `biz_question` VALUES (6, 'typing', '<p>adscadsc都开始撑死了水库v出东方你死了你倒是快说v较好的死咯被忽略十五v的</p>', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Y', NULL, NULL, 1, 'admin', '2025-08-20 11:01:58', '', NULL);
INSERT INTO `biz_question` VALUES (7, 'judgment', '<p>除了vn选正确</p>', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'T', '<p>解析</p>', NULL, NULL, NULL, 'Y', NULL, NULL, 1, 'admin', '2025-08-20 11:02:27', '', NULL);
INSERT INTO `biz_question` VALUES (9, 'choice', '选择题', 7, '1', NULL, 'a', 'b', 'cv', 'd', 'A', 'a', NULL, NULL, NULL, '0', NULL, NULL, 1, 'admin', '2025-08-20 11:18:31', '', NULL);
INSERT INTO `biz_question` VALUES (10, 'choice', '第三次', 1, '0', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '0', NULL, NULL, 1, 'admin', '2025-08-20 11:21:00', '', NULL);
INSERT INTO `biz_question` VALUES (11, 'choice', 'u', 1, '1', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Y', NULL, NULL, 1, 'admin', '2025-08-20 11:36:40', '', NULL);
INSERT INTO `biz_question` VALUES (13, 'typing', '请输入这段用于打字练习的文字。', 8, '1', NULL, '', '', '', '', '', '', NULL, NULL, NULL, '1', NULL, NULL, 1, 'admin', '2025-08-20 12:12:38', '', NULL);
INSERT INTO `biz_question` VALUES (14, 'judgment', '地球是平的。', 7, '0', NULL, '', '', '', '', 'F', '', NULL, NULL, NULL, '1', NULL, NULL, 1, 'admin', '2025-08-20 12:12:38', '', NULL);
INSERT INTO `biz_question` VALUES (15, 'choice', '中国的首都是哪里？', 7, '0', NULL, '北京', '上海', '广州', '深圳', 'A', '', NULL, NULL, NULL, '1', NULL, NULL, 1, 'admin', '2025-08-20 12:19:41', '', NULL);
INSERT INTO `biz_question` VALUES (16, 'typing', '请输入这段用于打字练习的文字。', 8, '1', NULL, '', '', '', '', '', '', NULL, NULL, NULL, '1', NULL, NULL, 1, 'admin', '2025-08-20 12:19:41', '', NULL);
INSERT INTO `biz_question` VALUES (17, 'judgment', '地球是平的。', 7, '0', NULL, '', '', '', '', 'F', '', NULL, NULL, NULL, '0', NULL, NULL, 1, 'admin', '2025-08-20 12:19:41', '', NULL);
INSERT INTO `biz_question` VALUES (18, 'choice', '中国的首都是哪里？', 7, '0', NULL, '北京', '上海', '广州', '深圳', 'A', '', NULL, NULL, NULL, '0', NULL, NULL, 1, 'admin', '2025-08-20 12:25:03', '', NULL);
INSERT INTO `biz_question` VALUES (19, 'typing', '请输入这段用于打字练习的文字。', 8, '1', NULL, '', '', '', '', '', '', NULL, NULL, NULL, '0', NULL, NULL, 1, 'admin', '2025-08-20 12:25:03', '', NULL);
INSERT INTO `biz_question` VALUES (20, 'judgment', '地球是平的。', 7, '0', NULL, '', '', '', '', 'F', '', NULL, NULL, NULL, '0', NULL, NULL, 1, 'admin', '2025-08-20 12:25:03', '', NULL);
INSERT INTO `biz_question` VALUES (21, 'typing', '试点城市水水', 1, '1', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'N', NULL, NULL, 1, 'admin', '2025-08-20 12:26:01', '', NULL);
INSERT INTO `biz_question` VALUES (22, 'choice', '中国的首都是哪里？', 7, '0', NULL, '北京', '上海', '广州', '深圳', 'A', '', NULL, NULL, NULL, 'Y', NULL, NULL, 1, 'admin', '2025-08-20 12:26:48', '', NULL);
INSERT INTO `biz_question` VALUES (23, 'typing', '请输入这段用于打字练习的文字。', 8, '1', NULL, '', '', '', '', '', '', NULL, NULL, NULL, 'Y', NULL, NULL, 1, 'admin', '2025-08-20 12:26:48', '', NULL);
INSERT INTO `biz_question` VALUES (24, 'judgment', '地球是平的。', 7, '0', NULL, '', '', '', '', 'F', '', NULL, NULL, NULL, 'N', NULL, NULL, 1, 'admin', '2025-08-20 12:26:48', '', NULL);
INSERT INTO `biz_question` VALUES (26, 'typing', '商店超市菜市场', 1, '0', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'N', 1, 7, 1, 'admin', '2025-08-22 12:49:55', '', NULL);
INSERT INTO `biz_question` VALUES (27, 'choice', '中国的首都是哪里？', 7, '0', NULL, '北京', '上海', '广州', '深圳', 'A', '', NULL, NULL, NULL, 'Y', NULL, NULL, 1, 'admin', '2025-08-22 12:51:08', '', NULL);
INSERT INTO `biz_question` VALUES (28, 'typing', '请输入这段用于打字练习的文字。', 8, '1', NULL, '', '', '', '', '', '', NULL, NULL, NULL, 'Y', 2, 15, 1, 'admin', '2025-08-22 12:51:08', '', NULL);
INSERT INTO `biz_question` VALUES (29, 'judgment', '地球是平的。', 7, '0', NULL, '', '', '', '', 'F', '', NULL, NULL, NULL, 'N', NULL, NULL, 1, 'admin', '2025-08-22 12:51:08', '', NULL);
INSERT INTO `biz_question` VALUES (32, 'choice', '第三次多少', 5, '0', NULL, '2', '23', '3', '4', 'B', NULL, NULL, NULL, NULL, 'N', NULL, NULL, 1, 'admin', '2025-08-22 14:02:36', '', NULL);
INSERT INTO `biz_question` VALUES (33, 'typing', '按顺序是', 2, '1', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'N', 2, 4, 1, 'admin', '2025-08-22 14:23:04', '', NULL);
INSERT INTO `biz_question` VALUES (35, 'judgment', 'lihli', 2, '1', NULL, NULL, NULL, NULL, NULL, 'T', NULL, NULL, NULL, NULL, 'Y', NULL, NULL, 1, 'admin', '2025-08-25 17:16:30', '', NULL);
INSERT INTO `biz_question` VALUES (40, 'typing', '而感染尴尬而广泛认同过热', 7, '0', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Y', 4, 12, 104, '19157727791', '2025-08-25 22:08:59', '', NULL);
INSERT INTO `biz_question` VALUES (41, 'typing', 'fregfefg', 7, '0', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Y', 3, 8, 104, '19157727791', '2025-12-29 18:56:46', '', NULL);
INSERT INTO `biz_question` VALUES (42, 'typing', '你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是你是', 7, '0', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Y', 4, 612, 104, '19157727791', '2026-01-05 08:37:21', '', NULL);
INSERT INTO `biz_question` VALUES (43, 'typing', '一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。', 7, '0', 4, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Y', 5, 189, 104, '19157727791', '2026-01-05 08:46:43', '19157727791', '2026-01-05 08:47:40');
INSERT INTO `biz_question` VALUES (45, 'practical', '是公司架构', 7, '0', 3, NULL, NULL, NULL, NULL, NULL, NULL, '/profile/upload/2026/01/05/附件1_20260105152408A001.docx', '/profile./uploadPath/upload/2026/01/05/附件1_20260105152408A001.pdf', NULL, 'Y', NULL, NULL, 104, '19157727791', '2026-01-05 15:24:12', '', NULL);
INSERT INTO `biz_question` VALUES (46, 'practical', '山地车山地车山地车', 7, '1', 4, NULL, NULL, NULL, NULL, NULL, NULL, '/profile/upload/2026/01/05/附件1_20260105152856A001.docx', '/profile/upload/2026/01/05/附件1_20260105152856A001.pdf', NULL, 'Y', NULL, NULL, 104, '19157727791', '2026-01-05 15:28:58', '', NULL);
INSERT INTO `biz_question` VALUES (47, 'practical', '等等', 7, '1', 6, NULL, NULL, NULL, NULL, NULL, NULL, '/profile/upload/2026/01/05/附表2：作品创作说明_20260105153315A002.docx', '/profile/upload/2026/01/05/附表2：作品创作说明_20260105153315A002.pdf', NULL, 'Y', NULL, NULL, 104, '19157727791', '2026-01-05 15:33:18', '', NULL);
INSERT INTO `biz_question` VALUES (48, 'practical', '任务操作1', 7, '1', 1, NULL, NULL, NULL, NULL, NULL, NULL, '/profile/upload/2026/01/07/物品申请单_20260107141913A001.docx', '/profile/upload/2026/01/07/物品申请单_20260107141913A001.pdf', NULL, 'Y', NULL, NULL, 104, '19157727791', '2026-01-07 14:20:24', '', NULL);
INSERT INTO `biz_question` VALUES (49, 'practical', 'zdx', 7, '1', 5, NULL, NULL, NULL, NULL, NULL, NULL, '/profile/upload/2026/01/07/大模型本地部署配置_20260107162900A001.docx', '/profile/upload/2026/01/07/大模型本地部署配置_20260107162900A001.pdf', NULL, 'Y', NULL, NULL, 104, '19157727791', '2026-01-07 16:29:55', '', NULL);
INSERT INTO `biz_question` VALUES (50, 'practical', '郑东旭', 7, '1', 2, NULL, NULL, NULL, NULL, NULL, NULL, '/profile/upload/2026/01/07/物品申请单_20260107163743A002.docx', '/profile/upload/2026/01/07/物品申请单_20260107163743A002.pdf', NULL, 'Y', NULL, NULL, 104, '19157727791', '2026-01-07 16:38:16', '', NULL);
INSERT INTO `biz_question` VALUES (51, 'practical', '操作18', 7, '1', 7, NULL, NULL, NULL, NULL, NULL, NULL, '/profile/upload/2026/01/08/111_20260108085453A001.docx', '/profile/upload/2026/01/08/111_20260108085453A001.pdf', NULL, 'Y', NULL, NULL, 104, '19157727791', '2026-01-08 08:55:24', '', NULL);
INSERT INTO `biz_question` VALUES (52, 'practical', '1212', 7, '1', 4, NULL, NULL, NULL, NULL, NULL, NULL, '/profile/upload/2026/01/12/浙里报报销流程（实验）_20260112091447A001.docx', '/profile/upload/2026/01/12/浙里报报销流程（实验）_20260112091447A001.pdf', 'success', 'Y', NULL, NULL, 104, '19157727791', '2026-01-12 09:14:59', '', NULL);
INSERT INTO `biz_question` VALUES (53, 'practical', '任务', 7, '1', 1, NULL, NULL, NULL, NULL, NULL, NULL, '/profile/upload/2026/01/12/浙里报报销流程（实验）_20260112092056A001.docx', '/profile/upload/2026/01/12/浙里报报销流程（实验）_20260112092056A001.pdf', 'success', 'Y', NULL, NULL, 104, '19157727791', '2026-01-12 09:21:10', '19157727791', '2026-01-12 09:28:02');
INSERT INTO `biz_question` VALUES (54, 'typing', '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。', 6, '1', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Y', 5, 131, 104, '19157727791', '2026-01-12 12:55:45', '', NULL);
INSERT INTO `biz_question` VALUES (55, 'practical', '定时灌溉和智慧农场系统的区别', 6, '1', 1, NULL, NULL, NULL, NULL, NULL, NULL, '/profile/upload/2026/01/12/学习单_20260112125728A001.docx', '/profile/upload/2026/01/12/学习单_20260112125728A001.pdf', 'success', 'Y', NULL, NULL, 104, '19157727791', '2026-01-12 12:57:48', '', NULL);
INSERT INTO `biz_question` VALUES (56, 'choice', '下列哪一项属于算法的基本特征', 6, '1', 1, '随意性', '有限性', '娱乐性', '模糊性', 'B', NULL, NULL, NULL, NULL, 'Y', NULL, NULL, 104, '19157727791', '2026-01-12 12:59:42', '', NULL);
INSERT INTO `biz_question` VALUES (57, 'choice', '在“鸡兔同笼”问题中，常用的算法思想是？', 6, '1', 1, '排序法', '枚举法', '加密法', '压缩法', 'B', NULL, NULL, NULL, NULL, 'Y', NULL, NULL, 104, '19157727791', '2026-01-12 13:00:46', '', NULL);
INSERT INTO `biz_question` VALUES (58, 'choice', '猜数字游戏中，用于反复让用户输入的结构是', 6, '1', 1, '顺序结构', '分支结构', '循环结构', '选择结构', 'C', NULL, NULL, NULL, NULL, 'Y', NULL, NULL, 104, '19157727791', '2026-01-12 13:01:55', '', NULL);
INSERT INTO `biz_question` VALUES (59, 'judgment', '算法只能由计算机来完成。', 6, '1', 1, NULL, NULL, NULL, NULL, 'F', NULL, NULL, NULL, NULL, 'Y', NULL, NULL, 104, '19157727791', '2026-01-12 13:02:20', '', NULL);
INSERT INTO `biz_question` VALUES (60, 'judgment', '同一个问题可能有多种算法。', 6, '1', 1, NULL, NULL, NULL, NULL, 'T', NULL, NULL, NULL, NULL, 'Y', NULL, NULL, 104, '19157727791', '2026-01-12 13:02:47', '', NULL);
INSERT INTO `biz_question` VALUES (61, 'judgment', '在猜数字游戏中，如果采用二分查找的方法，最多 7 次就一定能猜出 1～100 之间的任意一个数字。', 6, '1', 1, NULL, NULL, NULL, NULL, 'T', NULL, NULL, NULL, NULL, 'Y', NULL, NULL, 104, '19157727791', '2026-01-12 13:03:18', '', NULL);
INSERT INTO `biz_question` VALUES (62, 'choice', '【选择题】中国的首都是哪里？', 6, '0', NULL, '北京', '上海', '广州', '深圳', 'A', '', NULL, NULL, NULL, 'Y', NULL, NULL, 104, '19157727791', '2026-01-12 15:32:59', '', NULL);
INSERT INTO `biz_question` VALUES (63, 'choice', '下列哪个不是编程语言？', 6, '1', NULL, 'Python', 'Java', 'Word', 'Scratch', 'C', '', NULL, NULL, NULL, 'N', NULL, NULL, 104, '19157727791', '2026-01-12 15:32:59', '', NULL);
INSERT INTO `biz_question` VALUES (64, 'judgment', '【判断题】计算机病毒可以通过网络传播。', 5, '0', NULL, '', '', '', '', 'T', '', NULL, NULL, NULL, 'Y', NULL, NULL, 104, '19157727791', '2026-01-12 15:32:59', '', NULL);
INSERT INTO `biz_question` VALUES (65, 'judgment', 'CPU是计算机的存储器。', 5, '1', NULL, '', '', '', '', 'F', '', NULL, NULL, NULL, 'N', NULL, NULL, 104, '19157727791', '2026-01-12 15:32:59', '', NULL);
INSERT INTO `biz_question` VALUES (66, 'typing', '【打字题】春眠不觉晓，处处闻啼鸟。夜来风雨声，花落知多少。', 4, '0', NULL, '', '', '', '', '', '', NULL, NULL, NULL, 'Y', 3, 29, 104, '19157727791', '2026-01-12 15:32:59', '', NULL);
INSERT INTO `biz_question` VALUES (67, 'typing', '信息技术是指利用计算机和网络对信息进行获取、处理、存储和传递的技术。', 4, '1', NULL, '', '', '', '', '', '', NULL, NULL, NULL, 'N', 5, 34, 104, '19157727791', '2026-01-12 15:32:59', '', NULL);

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
) ENGINE = InnoDB AUTO_INCREMENT = 129 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '操作题分项得分记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_scoring_detail
-- ----------------------------
INSERT INTO `biz_scoring_detail` VALUES (5, 74, 8, 5, '2026-01-08 09:24:41', '2026-01-08 09:24:41');
INSERT INTO `biz_scoring_detail` VALUES (6, 74, 9, 6, '2026-01-08 09:24:41', '2026-01-08 09:24:41');
INSERT INTO `biz_scoring_detail` VALUES (7, 83, 8, 42, '2026-01-09 09:38:52', '2026-01-09 09:38:52');
INSERT INTO `biz_scoring_detail` VALUES (8, 83, 9, 28, '2026-01-09 09:38:52', '2026-01-09 09:38:52');
INSERT INTO `biz_scoring_detail` VALUES (9, 78, 8, 6, '2026-01-09 09:38:58', '2026-01-09 09:38:58');
INSERT INTO `biz_scoring_detail` VALUES (10, 78, 9, 5, '2026-01-09 09:38:58', '2026-01-09 09:38:58');
INSERT INTO `biz_scoring_detail` VALUES (11, 518, 12, 30, '2026-01-13 09:33:57', '2026-01-13 09:33:57');
INSERT INTO `biz_scoring_detail` VALUES (12, 510, 12, 15, '2026-01-13 09:36:03', '2026-01-13 09:36:03');
INSERT INTO `biz_scoring_detail` VALUES (13, 849, 12, 25, '2026-01-13 13:51:46', '2026-01-13 13:51:46');
INSERT INTO `biz_scoring_detail` VALUES (14, 871, 12, 30, '2026-01-13 13:51:57', '2026-01-13 13:51:57');
INSERT INTO `biz_scoring_detail` VALUES (15, 860, 12, 30, '2026-01-13 13:52:03', '2026-01-13 13:52:03');
INSERT INTO `biz_scoring_detail` VALUES (16, 862, 12, 30, '2026-01-13 13:52:08', '2026-01-13 13:52:08');
INSERT INTO `biz_scoring_detail` VALUES (17, 848, 12, 30, '2026-01-13 13:52:18', '2026-01-13 13:52:18');
INSERT INTO `biz_scoring_detail` VALUES (18, 857, 12, 10, '2026-01-13 13:52:23', '2026-01-13 13:52:23');
INSERT INTO `biz_scoring_detail` VALUES (19, 847, 12, 15, '2026-01-13 13:52:28', '2026-01-13 13:52:28');
INSERT INTO `biz_scoring_detail` VALUES (20, 866, 12, 30, '2026-01-13 13:52:35', '2026-01-13 13:52:35');
INSERT INTO `biz_scoring_detail` VALUES (21, 840, 12, 0, '2026-01-13 13:53:06', '2026-01-13 13:53:06');
INSERT INTO `biz_scoring_detail` VALUES (22, 853, 12, 30, '2026-01-13 13:53:19', '2026-01-13 13:53:19');
INSERT INTO `biz_scoring_detail` VALUES (23, 846, 12, 25, '2026-01-13 13:53:26', '2026-01-13 13:53:26');
INSERT INTO `biz_scoring_detail` VALUES (24, 865, 12, 25, '2026-01-13 13:53:30', '2026-01-13 13:53:30');
INSERT INTO `biz_scoring_detail` VALUES (25, 861, 12, 30, '2026-01-13 13:53:39', '2026-01-13 13:53:39');
INSERT INTO `biz_scoring_detail` VALUES (26, 854, 12, 30, '2026-01-13 13:53:42', '2026-01-13 13:53:42');
INSERT INTO `biz_scoring_detail` VALUES (27, 843, 12, 28, '2026-01-13 13:53:48', '2026-01-13 13:53:48');
INSERT INTO `biz_scoring_detail` VALUES (28, 851, 12, 29, '2026-01-13 13:53:53', '2026-01-13 13:53:53');
INSERT INTO `biz_scoring_detail` VALUES (29, 852, 12, 26, '2026-01-13 13:53:58', '2026-01-13 13:53:58');
INSERT INTO `biz_scoring_detail` VALUES (30, 841, 12, 24, '2026-01-13 13:54:01', '2026-01-13 13:54:01');
INSERT INTO `biz_scoring_detail` VALUES (31, 850, 12, 5, '2026-01-13 13:54:07', '2026-01-13 13:54:07');
INSERT INTO `biz_scoring_detail` VALUES (33, 844, 12, 5, '2026-01-13 13:54:24', '2026-01-13 13:54:24');
INSERT INTO `biz_scoring_detail` VALUES (34, 858, 12, 25, '2026-01-13 13:54:27', '2026-01-13 13:54:27');
INSERT INTO `biz_scoring_detail` VALUES (35, 869, 12, 20, '2026-01-13 13:54:31', '2026-01-13 13:54:31');
INSERT INTO `biz_scoring_detail` VALUES (36, 864, 12, 15, '2026-01-13 13:54:34', '2026-01-13 13:54:34');
INSERT INTO `biz_scoring_detail` VALUES (37, 842, 12, 30, '2026-01-13 13:54:38', '2026-01-13 13:54:38');
INSERT INTO `biz_scoring_detail` VALUES (38, 859, 12, 28, '2026-01-13 13:54:43', '2026-01-13 13:54:43');
INSERT INTO `biz_scoring_detail` VALUES (39, 845, 12, 30, '2026-01-13 13:55:06', '2026-01-13 13:55:06');
INSERT INTO `biz_scoring_detail` VALUES (40, 856, 12, 30, '2026-01-13 13:55:12', '2026-01-13 13:55:12');
INSERT INTO `biz_scoring_detail` VALUES (41, 855, 12, 15, '2026-01-13 13:55:15', '2026-01-13 13:55:15');
INSERT INTO `biz_scoring_detail` VALUES (42, 870, 12, 20, '2026-01-13 13:55:19', '2026-01-13 13:55:19');
INSERT INTO `biz_scoring_detail` VALUES (43, 863, 12, 25, '2026-01-13 13:55:26', '2026-01-13 13:55:26');
INSERT INTO `biz_scoring_detail` VALUES (44, 867, 12, 25, '2026-01-13 13:55:30', '2026-01-13 13:55:30');
INSERT INTO `biz_scoring_detail` VALUES (48, 868, 12, 22, '2026-01-13 13:55:43', '2026-01-13 13:55:43');
INSERT INTO `biz_scoring_detail` VALUES (53, 1128, 12, 26, '2026-01-13 14:43:15', '2026-01-13 14:43:15');
INSERT INTO `biz_scoring_detail` VALUES (60, 1129, 12, 20, '2026-01-13 14:45:26', '2026-01-13 14:45:26');
INSERT INTO `biz_scoring_detail` VALUES (61, 1134, 12, 28, '2026-01-13 14:45:32', '2026-01-13 14:45:32');
INSERT INTO `biz_scoring_detail` VALUES (62, 1135, 12, 29, '2026-01-13 14:45:39', '2026-01-13 14:45:39');
INSERT INTO `biz_scoring_detail` VALUES (63, 1127, 12, 26, '2026-01-13 14:45:45', '2026-01-13 14:45:45');
INSERT INTO `biz_scoring_detail` VALUES (64, 1139, 12, 29, '2026-01-13 14:45:58', '2026-01-13 14:45:58');
INSERT INTO `biz_scoring_detail` VALUES (65, 1175, 12, 25, '2026-01-13 14:46:03', '2026-01-13 14:46:03');
INSERT INTO `biz_scoring_detail` VALUES (66, 1148, 12, 10, '2026-01-13 14:46:08', '2026-01-13 14:46:08');
INSERT INTO `biz_scoring_detail` VALUES (67, 1140, 12, 28, '2026-01-13 14:46:15', '2026-01-13 14:46:15');
INSERT INTO `biz_scoring_detail` VALUES (68, 1133, 12, 20, '2026-01-13 14:46:23', '2026-01-13 14:46:23');
INSERT INTO `biz_scoring_detail` VALUES (69, 1176, 12, 23, '2026-01-13 14:46:28', '2026-01-13 14:46:28');
INSERT INTO `biz_scoring_detail` VALUES (70, 1178, 12, 28, '2026-01-13 14:46:35', '2026-01-13 14:46:35');
INSERT INTO `biz_scoring_detail` VALUES (71, 1161, 12, 29, '2026-01-13 14:46:47', '2026-01-13 14:46:47');
INSERT INTO `biz_scoring_detail` VALUES (78, 1120, 12, 20, '2026-01-13 16:04:21', '2026-01-13 16:04:21');
INSERT INTO `biz_scoring_detail` VALUES (91, 1126, 12, 30, '2026-01-13 16:45:02', '2026-01-13 16:45:02');
INSERT INTO `biz_scoring_detail` VALUES (92, 1177, 12, 30, '2026-01-13 16:45:04', '2026-01-13 16:45:04');
INSERT INTO `biz_scoring_detail` VALUES (93, 1141, 12, 24, '2026-01-13 16:45:08', '2026-01-13 16:45:08');
INSERT INTO `biz_scoring_detail` VALUES (94, 1150, 12, 28, '2026-01-13 16:45:09', '2026-01-13 16:45:09');
INSERT INTO `biz_scoring_detail` VALUES (95, 1131, 12, 25, '2026-01-13 16:45:11', '2026-01-13 16:45:11');
INSERT INTO `biz_scoring_detail` VALUES (96, 1136, 12, 28, '2026-01-13 16:45:16', '2026-01-13 16:45:16');
INSERT INTO `biz_scoring_detail` VALUES (97, 1159, 12, 29, '2026-01-13 16:45:18', '2026-01-13 16:45:18');
INSERT INTO `biz_scoring_detail` VALUES (98, 1158, 12, 30, '2026-01-13 16:56:53', '2026-01-13 16:56:53');
INSERT INTO `biz_scoring_detail` VALUES (99, 1130, 12, 26, '2026-01-13 16:56:53', '2026-01-13 16:56:53');
INSERT INTO `biz_scoring_detail` VALUES (100, 1125, 12, 20, '2026-01-13 16:56:54', '2026-01-13 16:56:54');
INSERT INTO `biz_scoring_detail` VALUES (101, 1119, 12, 20, '2026-01-13 16:56:55', '2026-01-13 16:56:55');
INSERT INTO `biz_scoring_detail` VALUES (102, 1499, 12, 25, '2026-01-16 12:56:39', '2026-01-16 12:56:39');
INSERT INTO `biz_scoring_detail` VALUES (103, 1500, 12, 30, '2026-01-16 12:56:49', '2026-01-16 12:56:49');
INSERT INTO `biz_scoring_detail` VALUES (104, 1506, 12, 20, '2026-01-16 12:57:00', '2026-01-16 12:57:00');
INSERT INTO `biz_scoring_detail` VALUES (105, 1502, 12, 15, '2026-01-16 12:57:10', '2026-01-16 12:57:10');
INSERT INTO `biz_scoring_detail` VALUES (107, 1489, 12, 20, '2026-01-16 12:57:22', '2026-01-16 12:57:22');
INSERT INTO `biz_scoring_detail` VALUES (108, 1501, 12, 20, '2026-01-16 12:57:31', '2026-01-16 12:57:31');
INSERT INTO `biz_scoring_detail` VALUES (109, 1507, 12, 5, '2026-01-16 12:57:44', '2026-01-16 12:57:44');
INSERT INTO `biz_scoring_detail` VALUES (110, 1492, 12, 10, '2026-01-16 12:58:05', '2026-01-16 12:58:05');
INSERT INTO `biz_scoring_detail` VALUES (111, 1455, 12, 30, '2026-01-16 12:58:12', '2026-01-16 12:58:12');
INSERT INTO `biz_scoring_detail` VALUES (112, 1471, 12, 25, '2026-01-16 12:58:16', '2026-01-16 12:58:16');
INSERT INTO `biz_scoring_detail` VALUES (113, 1452, 12, 30, '2026-01-16 12:58:27', '2026-01-16 12:58:27');
INSERT INTO `biz_scoring_detail` VALUES (114, 1465, 12, 28, '2026-01-16 12:58:30', '2026-01-16 12:58:30');
INSERT INTO `biz_scoring_detail` VALUES (115, 1504, 12, 20, '2026-01-16 12:58:37', '2026-01-16 12:58:37');
INSERT INTO `biz_scoring_detail` VALUES (116, 1505, 12, 28, '2026-01-16 12:58:43', '2026-01-16 12:58:43');
INSERT INTO `biz_scoring_detail` VALUES (117, 1456, 12, 30, '2026-01-16 12:58:49', '2026-01-16 12:58:49');
INSERT INTO `biz_scoring_detail` VALUES (118, 1466, 12, 5, '2026-01-16 12:58:59', '2026-01-16 12:58:59');
INSERT INTO `biz_scoring_detail` VALUES (119, 1469, 12, 25, '2026-01-16 12:59:11', '2026-01-16 12:59:11');
INSERT INTO `biz_scoring_detail` VALUES (120, 1451, 12, 28, '2026-01-16 12:59:24', '2026-01-16 12:59:24');
INSERT INTO `biz_scoring_detail` VALUES (121, 1447, 12, 29, '2026-01-16 12:59:30', '2026-01-16 12:59:30');
INSERT INTO `biz_scoring_detail` VALUES (122, 1449, 12, 29, '2026-01-16 12:59:37', '2026-01-16 12:59:37');
INSERT INTO `biz_scoring_detail` VALUES (123, 1467, 12, 30, '2026-01-16 12:59:49', '2026-01-16 12:59:49');
INSERT INTO `biz_scoring_detail` VALUES (124, 1491, 12, 29, '2026-01-16 12:59:53', '2026-01-16 12:59:53');
INSERT INTO `biz_scoring_detail` VALUES (126, 1495, 12, 20, '2026-01-16 13:00:10', '2026-01-16 13:00:10');
INSERT INTO `biz_scoring_detail` VALUES (127, 1496, 12, 30, '2026-01-16 13:00:15', '2026-01-16 13:00:15');
INSERT INTO `biz_scoring_detail` VALUES (128, 1508, 12, 5, '2026-01-16 13:00:21', '2026-01-16 13:00:21');

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
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '操作题评分项定义' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_scoring_item
-- ----------------------------
INSERT INTO `biz_scoring_item` VALUES (6, 50, '界面', 40, 0, '2026-01-07 16:38:27', '2026-01-07 16:38:27');
INSERT INTO `biz_scoring_item` VALUES (7, 50, '你还哦', 60, 1, '2026-01-07 16:38:27', '2026-01-07 16:38:27');
INSERT INTO `biz_scoring_item` VALUES (8, 51, '界面', 60, 0, '2026-01-08 08:55:38', '2026-01-08 08:55:38');
INSERT INTO `biz_scoring_item` VALUES (9, 51, '答案', 40, 1, '2026-01-08 08:55:38', '2026-01-08 08:55:38');
INSERT INTO `biz_scoring_item` VALUES (11, 53, '界面', 100, 0, '2026-01-12 09:21:09', '2026-01-12 09:28:02');
INSERT INTO `biz_scoring_item` VALUES (12, 55, '答案是否正常', 100, 0, '2026-01-12 12:57:47', '2026-01-12 12:57:47');

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
) ENGINE = InnoDB AUTO_INCREMENT = 446 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学生业务表' ROW_FORMAT = DYNAMIC;

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
INSERT INTO `biz_student` VALUES (62, 168, '1', '2020', '7');
INSERT INTO `biz_student` VALUES (63, 169, '2', '2020', '7');
INSERT INTO `biz_student` VALUES (64, 170, '3', '2020', '7');
INSERT INTO `biz_student` VALUES (65, 171, '4', '2020', '7');
INSERT INTO `biz_student` VALUES (66, 172, '5', '2020', '7');
INSERT INTO `biz_student` VALUES (67, 173, '6', '2020', '7');
INSERT INTO `biz_student` VALUES (68, 174, '7', '2020', '7');
INSERT INTO `biz_student` VALUES (69, 175, '8', '2020', '7');
INSERT INTO `biz_student` VALUES (70, 176, '9', '2020', '7');
INSERT INTO `biz_student` VALUES (71, 177, '10', '2020', '7');
INSERT INTO `biz_student` VALUES (72, 178, '11', '2020', '7');
INSERT INTO `biz_student` VALUES (73, 179, '12', '2020', '7');
INSERT INTO `biz_student` VALUES (74, 180, '13', '2020', '7');
INSERT INTO `biz_student` VALUES (75, 181, '14', '2020', '7');
INSERT INTO `biz_student` VALUES (76, 182, '15', '2020', '7');
INSERT INTO `biz_student` VALUES (77, 183, '16', '2020', '7');
INSERT INTO `biz_student` VALUES (78, 184, '17', '2020', '7');
INSERT INTO `biz_student` VALUES (79, 185, '18', '2020', '7');
INSERT INTO `biz_student` VALUES (80, 186, '19', '2020', '7');
INSERT INTO `biz_student` VALUES (81, 187, '20', '2020', '7');
INSERT INTO `biz_student` VALUES (82, 188, '21', '2020', '7');
INSERT INTO `biz_student` VALUES (83, 189, '22', '2020', '7');
INSERT INTO `biz_student` VALUES (84, 190, '23', '2020', '7');
INSERT INTO `biz_student` VALUES (85, 191, '24', '2020', '7');
INSERT INTO `biz_student` VALUES (86, 192, '25', '2020', '7');
INSERT INTO `biz_student` VALUES (87, 193, '26', '2020', '7');
INSERT INTO `biz_student` VALUES (88, 194, '27', '2020', '7');
INSERT INTO `biz_student` VALUES (89, 195, '28', '2020', '7');
INSERT INTO `biz_student` VALUES (90, 196, '29', '2020', '7');
INSERT INTO `biz_student` VALUES (91, 197, '30', '2020', '7');
INSERT INTO `biz_student` VALUES (92, 198, '31', '2020', '7');
INSERT INTO `biz_student` VALUES (93, 199, '32', '2020', '7');
INSERT INTO `biz_student` VALUES (94, 200, '33', '2020', '7');
INSERT INTO `biz_student` VALUES (95, 201, '34', '2020', '7');
INSERT INTO `biz_student` VALUES (96, 202, '35', '2020', '7');
INSERT INTO `biz_student` VALUES (97, 203, '36', '2020', '7');
INSERT INTO `biz_student` VALUES (98, 204, '37', '2020', '7');
INSERT INTO `biz_student` VALUES (99, 205, '38', '2020', '7');
INSERT INTO `biz_student` VALUES (100, 206, '39', '2020', '7');
INSERT INTO `biz_student` VALUES (101, 207, '40', '2020', '7');
INSERT INTO `biz_student` VALUES (102, 208, '41', '2020', '7');
INSERT INTO `biz_student` VALUES (103, 209, '42', '2020', '7');
INSERT INTO `biz_student` VALUES (104, 210, '43', '2020', '7');
INSERT INTO `biz_student` VALUES (148, 254, '01', '2020', '4');
INSERT INTO `biz_student` VALUES (149, 255, '02', '2020', '4');
INSERT INTO `biz_student` VALUES (150, 256, '03', '2020', '4');
INSERT INTO `biz_student` VALUES (151, 257, '04', '2020', '4');
INSERT INTO `biz_student` VALUES (152, 258, '05', '2020', '4');
INSERT INTO `biz_student` VALUES (153, 259, '07', '2020', '4');
INSERT INTO `biz_student` VALUES (154, 260, '08', '2020', '4');
INSERT INTO `biz_student` VALUES (155, 261, '09', '2020', '4');
INSERT INTO `biz_student` VALUES (156, 262, '10', '2020', '4');
INSERT INTO `biz_student` VALUES (157, 263, '11', '2020', '4');
INSERT INTO `biz_student` VALUES (158, 264, '12', '2020', '4');
INSERT INTO `biz_student` VALUES (159, 265, '13', '2020', '4');
INSERT INTO `biz_student` VALUES (160, 266, '14', '2020', '4');
INSERT INTO `biz_student` VALUES (161, 267, '15', '2020', '4');
INSERT INTO `biz_student` VALUES (162, 268, '16', '2020', '4');
INSERT INTO `biz_student` VALUES (163, 269, '17', '2020', '4');
INSERT INTO `biz_student` VALUES (164, 270, '18', '2020', '4');
INSERT INTO `biz_student` VALUES (165, 271, '19', '2020', '4');
INSERT INTO `biz_student` VALUES (166, 272, '20', '2020', '4');
INSERT INTO `biz_student` VALUES (167, 273, '21', '2020', '4');
INSERT INTO `biz_student` VALUES (168, 274, '22', '2020', '4');
INSERT INTO `biz_student` VALUES (169, 275, '23', '2020', '4');
INSERT INTO `biz_student` VALUES (170, 276, '24', '2020', '4');
INSERT INTO `biz_student` VALUES (171, 277, '25', '2020', '4');
INSERT INTO `biz_student` VALUES (172, 278, '26', '2020', '4');
INSERT INTO `biz_student` VALUES (173, 279, '27', '2020', '4');
INSERT INTO `biz_student` VALUES (174, 280, '28', '2020', '4');
INSERT INTO `biz_student` VALUES (175, 281, '29', '2020', '4');
INSERT INTO `biz_student` VALUES (176, 282, '31', '2020', '4');
INSERT INTO `biz_student` VALUES (177, 283, '32', '2020', '4');
INSERT INTO `biz_student` VALUES (178, 284, '33', '2020', '4');
INSERT INTO `biz_student` VALUES (179, 285, '34', '2020', '4');
INSERT INTO `biz_student` VALUES (180, 286, '35', '2020', '4');
INSERT INTO `biz_student` VALUES (181, 287, '36', '2020', '4');
INSERT INTO `biz_student` VALUES (182, 288, '37', '2020', '4');
INSERT INTO `biz_student` VALUES (183, 289, '38', '2020', '4');
INSERT INTO `biz_student` VALUES (184, 290, '39', '2020', '4');
INSERT INTO `biz_student` VALUES (185, 291, '40', '2020', '4');
INSERT INTO `biz_student` VALUES (186, 292, '41', '2020', '4');
INSERT INTO `biz_student` VALUES (187, 293, '30', '2020', '4');
INSERT INTO `biz_student` VALUES (188, 294, '06', '2020', '4');
INSERT INTO `biz_student` VALUES (189, 295, '43', '2020', '4');
INSERT INTO `biz_student` VALUES (190, 296, '42', '2020', '4');
INSERT INTO `biz_student` VALUES (191, 297, '1', '2020', '2');
INSERT INTO `biz_student` VALUES (192, 298, '2', '2020', '2');
INSERT INTO `biz_student` VALUES (193, 299, '3', '2020', '2');
INSERT INTO `biz_student` VALUES (194, 300, '4', '2020', '2');
INSERT INTO `biz_student` VALUES (195, 301, '5', '2020', '2');
INSERT INTO `biz_student` VALUES (196, 302, '6', '2020', '2');
INSERT INTO `biz_student` VALUES (197, 303, '7', '2020', '2');
INSERT INTO `biz_student` VALUES (198, 304, '8', '2020', '2');
INSERT INTO `biz_student` VALUES (199, 305, '9', '2020', '2');
INSERT INTO `biz_student` VALUES (200, 306, '10', '2020', '2');
INSERT INTO `biz_student` VALUES (201, 307, '11', '2020', '2');
INSERT INTO `biz_student` VALUES (202, 308, '12', '2020', '2');
INSERT INTO `biz_student` VALUES (203, 309, '13', '2020', '2');
INSERT INTO `biz_student` VALUES (204, 310, '14', '2020', '2');
INSERT INTO `biz_student` VALUES (205, 311, '15', '2020', '2');
INSERT INTO `biz_student` VALUES (206, 312, '16', '2020', '2');
INSERT INTO `biz_student` VALUES (207, 313, '17', '2020', '2');
INSERT INTO `biz_student` VALUES (208, 314, '18', '2020', '2');
INSERT INTO `biz_student` VALUES (209, 315, '19', '2020', '2');
INSERT INTO `biz_student` VALUES (210, 316, '20', '2020', '2');
INSERT INTO `biz_student` VALUES (211, 317, '22', '2020', '2');
INSERT INTO `biz_student` VALUES (212, 318, '23', '2020', '2');
INSERT INTO `biz_student` VALUES (213, 319, '24', '2020', '2');
INSERT INTO `biz_student` VALUES (214, 320, '25', '2020', '2');
INSERT INTO `biz_student` VALUES (215, 321, '26', '2020', '2');
INSERT INTO `biz_student` VALUES (216, 322, '27', '2020', '2');
INSERT INTO `biz_student` VALUES (217, 323, '28', '2020', '2');
INSERT INTO `biz_student` VALUES (218, 324, '29', '2020', '2');
INSERT INTO `biz_student` VALUES (219, 325, '30', '2020', '2');
INSERT INTO `biz_student` VALUES (220, 326, '31', '2020', '2');
INSERT INTO `biz_student` VALUES (221, 327, '32', '2020', '2');
INSERT INTO `biz_student` VALUES (222, 328, '33', '2020', '2');
INSERT INTO `biz_student` VALUES (223, 329, '34', '2020', '2');
INSERT INTO `biz_student` VALUES (224, 330, '35', '2020', '2');
INSERT INTO `biz_student` VALUES (225, 331, '36', '2020', '2');
INSERT INTO `biz_student` VALUES (226, 332, '37', '2020', '2');
INSERT INTO `biz_student` VALUES (227, 333, '38', '2020', '2');
INSERT INTO `biz_student` VALUES (228, 334, '39', '2020', '2');
INSERT INTO `biz_student` VALUES (229, 335, '40', '2020', '2');
INSERT INTO `biz_student` VALUES (230, 336, '41', '2020', '2');
INSERT INTO `biz_student` VALUES (231, 337, '21', '2020', '2');
INSERT INTO `biz_student` VALUES (232, 338, '42', '2020', '2');
INSERT INTO `biz_student` VALUES (233, 339, '43', '2020', '2');
INSERT INTO `biz_student` VALUES (234, 340, '1', '2020', '1');
INSERT INTO `biz_student` VALUES (235, 341, '2', '2020', '1');
INSERT INTO `biz_student` VALUES (236, 342, '3', '2020', '1');
INSERT INTO `biz_student` VALUES (237, 343, '4', '2020', '1');
INSERT INTO `biz_student` VALUES (238, 344, '5', '2020', '1');
INSERT INTO `biz_student` VALUES (239, 345, '6', '2020', '1');
INSERT INTO `biz_student` VALUES (240, 346, '7', '2020', '1');
INSERT INTO `biz_student` VALUES (241, 347, '8', '2020', '1');
INSERT INTO `biz_student` VALUES (242, 348, '9', '2020', '1');
INSERT INTO `biz_student` VALUES (243, 349, '10', '2020', '1');
INSERT INTO `biz_student` VALUES (244, 350, '11', '2020', '1');
INSERT INTO `biz_student` VALUES (245, 351, '12', '2020', '1');
INSERT INTO `biz_student` VALUES (246, 352, '13', '2020', '1');
INSERT INTO `biz_student` VALUES (247, 353, '14', '2020', '1');
INSERT INTO `biz_student` VALUES (248, 354, '15', '2020', '1');
INSERT INTO `biz_student` VALUES (249, 355, '16', '2020', '1');
INSERT INTO `biz_student` VALUES (250, 356, '17', '2020', '1');
INSERT INTO `biz_student` VALUES (251, 357, '18', '2020', '1');
INSERT INTO `biz_student` VALUES (252, 358, '19', '2020', '1');
INSERT INTO `biz_student` VALUES (253, 359, '20', '2020', '1');
INSERT INTO `biz_student` VALUES (254, 360, '21', '2020', '1');
INSERT INTO `biz_student` VALUES (255, 361, '22', '2020', '1');
INSERT INTO `biz_student` VALUES (256, 362, '23', '2020', '1');
INSERT INTO `biz_student` VALUES (257, 363, '24', '2020', '1');
INSERT INTO `biz_student` VALUES (258, 364, '25', '2020', '1');
INSERT INTO `biz_student` VALUES (259, 365, '26', '2020', '1');
INSERT INTO `biz_student` VALUES (260, 366, '27', '2020', '1');
INSERT INTO `biz_student` VALUES (261, 367, '28', '2020', '1');
INSERT INTO `biz_student` VALUES (262, 368, '29', '2020', '1');
INSERT INTO `biz_student` VALUES (263, 369, '30', '2020', '1');
INSERT INTO `biz_student` VALUES (264, 370, '31', '2020', '1');
INSERT INTO `biz_student` VALUES (265, 371, '32', '2020', '1');
INSERT INTO `biz_student` VALUES (266, 372, '34', '2020', '1');
INSERT INTO `biz_student` VALUES (267, 373, '35', '2020', '1');
INSERT INTO `biz_student` VALUES (268, 374, '36', '2020', '1');
INSERT INTO `biz_student` VALUES (269, 375, '38', '2020', '1');
INSERT INTO `biz_student` VALUES (270, 376, '39', '2020', '1');
INSERT INTO `biz_student` VALUES (271, 377, '40', '2020', '1');
INSERT INTO `biz_student` VALUES (272, 378, '41', '2020', '1');
INSERT INTO `biz_student` VALUES (273, 379, '42', '2020', '1');
INSERT INTO `biz_student` VALUES (274, 380, '43', '2020', '1');
INSERT INTO `biz_student` VALUES (275, 381, '44', '2020', '1');
INSERT INTO `biz_student` VALUES (276, 382, '1', '2020', '8');
INSERT INTO `biz_student` VALUES (277, 383, '2', '2020', '8');
INSERT INTO `biz_student` VALUES (278, 384, '3', '2020', '8');
INSERT INTO `biz_student` VALUES (279, 385, '4', '2020', '8');
INSERT INTO `biz_student` VALUES (280, 386, '5', '2020', '8');
INSERT INTO `biz_student` VALUES (281, 387, '6', '2020', '8');
INSERT INTO `biz_student` VALUES (282, 388, '7', '2020', '8');
INSERT INTO `biz_student` VALUES (283, 389, '8', '2020', '8');
INSERT INTO `biz_student` VALUES (284, 390, '9', '2020', '8');
INSERT INTO `biz_student` VALUES (285, 391, '10', '2020', '8');
INSERT INTO `biz_student` VALUES (286, 392, '11', '2020', '8');
INSERT INTO `biz_student` VALUES (287, 393, '12', '2020', '8');
INSERT INTO `biz_student` VALUES (288, 394, '13', '2020', '8');
INSERT INTO `biz_student` VALUES (289, 395, '14', '2020', '8');
INSERT INTO `biz_student` VALUES (290, 396, '15', '2020', '8');
INSERT INTO `biz_student` VALUES (291, 397, '16', '2020', '8');
INSERT INTO `biz_student` VALUES (292, 398, '17', '2020', '8');
INSERT INTO `biz_student` VALUES (293, 399, '18', '2020', '8');
INSERT INTO `biz_student` VALUES (294, 400, '19', '2020', '8');
INSERT INTO `biz_student` VALUES (295, 401, '20', '2020', '8');
INSERT INTO `biz_student` VALUES (296, 402, '21', '2020', '8');
INSERT INTO `biz_student` VALUES (297, 403, '22', '2020', '8');
INSERT INTO `biz_student` VALUES (298, 404, '23', '2020', '8');
INSERT INTO `biz_student` VALUES (299, 405, '24', '2020', '8');
INSERT INTO `biz_student` VALUES (300, 406, '25', '2020', '8');
INSERT INTO `biz_student` VALUES (301, 407, '26', '2020', '8');
INSERT INTO `biz_student` VALUES (302, 408, '27', '2020', '8');
INSERT INTO `biz_student` VALUES (303, 409, '28', '2020', '8');
INSERT INTO `biz_student` VALUES (304, 410, '29', '2020', '8');
INSERT INTO `biz_student` VALUES (305, 411, '30', '2020', '8');
INSERT INTO `biz_student` VALUES (306, 412, '31', '2020', '8');
INSERT INTO `biz_student` VALUES (307, 413, '33', '2020', '8');
INSERT INTO `biz_student` VALUES (308, 414, '34', '2020', '8');
INSERT INTO `biz_student` VALUES (309, 415, '35', '2020', '8');
INSERT INTO `biz_student` VALUES (310, 416, '36', '2020', '8');
INSERT INTO `biz_student` VALUES (311, 417, '37', '2020', '8');
INSERT INTO `biz_student` VALUES (312, 418, '38', '2020', '8');
INSERT INTO `biz_student` VALUES (313, 419, '39', '2020', '8');
INSERT INTO `biz_student` VALUES (314, 420, '40', '2020', '8');
INSERT INTO `biz_student` VALUES (315, 421, '41', '2020', '8');
INSERT INTO `biz_student` VALUES (316, 422, '42', '2020', '8');
INSERT INTO `biz_student` VALUES (317, 423, '32', '2020', '8');
INSERT INTO `biz_student` VALUES (318, 424, '99', '2020', '10');
INSERT INTO `biz_student` VALUES (319, 425, '01', '2020', '3');
INSERT INTO `biz_student` VALUES (320, 426, '02', '2020', '3');
INSERT INTO `biz_student` VALUES (321, 427, '03', '2020', '3');
INSERT INTO `biz_student` VALUES (322, 428, '04', '2020', '3');
INSERT INTO `biz_student` VALUES (323, 429, '06', '2020', '3');
INSERT INTO `biz_student` VALUES (324, 430, '07', '2020', '3');
INSERT INTO `biz_student` VALUES (325, 431, '08', '2020', '3');
INSERT INTO `biz_student` VALUES (326, 432, '09', '2020', '3');
INSERT INTO `biz_student` VALUES (327, 433, '10', '2020', '3');
INSERT INTO `biz_student` VALUES (328, 434, '11', '2020', '3');
INSERT INTO `biz_student` VALUES (329, 435, '12', '2020', '3');
INSERT INTO `biz_student` VALUES (330, 436, '13', '2020', '3');
INSERT INTO `biz_student` VALUES (331, 437, '14', '2020', '3');
INSERT INTO `biz_student` VALUES (332, 438, '15', '2020', '3');
INSERT INTO `biz_student` VALUES (333, 439, '16', '2020', '3');
INSERT INTO `biz_student` VALUES (334, 440, '17', '2020', '3');
INSERT INTO `biz_student` VALUES (335, 441, '18', '2020', '3');
INSERT INTO `biz_student` VALUES (336, 442, '19', '2020', '3');
INSERT INTO `biz_student` VALUES (337, 443, '20', '2020', '3');
INSERT INTO `biz_student` VALUES (338, 444, '21', '2020', '3');
INSERT INTO `biz_student` VALUES (339, 445, '22', '2020', '3');
INSERT INTO `biz_student` VALUES (340, 446, '23', '2020', '3');
INSERT INTO `biz_student` VALUES (341, 447, '24', '2020', '3');
INSERT INTO `biz_student` VALUES (342, 448, '25', '2020', '3');
INSERT INTO `biz_student` VALUES (343, 449, '26', '2020', '3');
INSERT INTO `biz_student` VALUES (344, 450, '28', '2020', '3');
INSERT INTO `biz_student` VALUES (345, 451, '29', '2020', '3');
INSERT INTO `biz_student` VALUES (346, 452, '30', '2020', '3');
INSERT INTO `biz_student` VALUES (347, 453, '31', '2020', '3');
INSERT INTO `biz_student` VALUES (348, 454, '32', '2020', '3');
INSERT INTO `biz_student` VALUES (349, 455, '33', '2020', '3');
INSERT INTO `biz_student` VALUES (350, 456, '34', '2020', '3');
INSERT INTO `biz_student` VALUES (351, 457, '35', '2020', '3');
INSERT INTO `biz_student` VALUES (352, 458, '36', '2020', '3');
INSERT INTO `biz_student` VALUES (353, 459, '37', '2020', '3');
INSERT INTO `biz_student` VALUES (354, 460, '38', '2020', '3');
INSERT INTO `biz_student` VALUES (355, 461, '39', '2020', '3');
INSERT INTO `biz_student` VALUES (356, 462, '40', '2020', '3');
INSERT INTO `biz_student` VALUES (357, 463, '41', '2020', '3');
INSERT INTO `biz_student` VALUES (358, 464, '42', '2020', '3');
INSERT INTO `biz_student` VALUES (359, 465, '05', '2020', '3');
INSERT INTO `biz_student` VALUES (360, 466, '27', '2020', '3');
INSERT INTO `biz_student` VALUES (361, 467, '01', '2020', '5');
INSERT INTO `biz_student` VALUES (362, 468, '02', '2020', '5');
INSERT INTO `biz_student` VALUES (363, 469, '03', '2020', '5');
INSERT INTO `biz_student` VALUES (364, 470, '04', '2020', '5');
INSERT INTO `biz_student` VALUES (365, 471, '05', '2020', '5');
INSERT INTO `biz_student` VALUES (366, 472, '06', '2020', '5');
INSERT INTO `biz_student` VALUES (367, 473, '07', '2020', '5');
INSERT INTO `biz_student` VALUES (368, 474, '08', '2020', '5');
INSERT INTO `biz_student` VALUES (369, 475, '09', '2020', '5');
INSERT INTO `biz_student` VALUES (370, 476, '10', '2020', '5');
INSERT INTO `biz_student` VALUES (371, 477, '11', '2020', '5');
INSERT INTO `biz_student` VALUES (372, 478, '12', '2020', '5');
INSERT INTO `biz_student` VALUES (373, 479, '13', '2020', '5');
INSERT INTO `biz_student` VALUES (374, 480, '14', '2020', '5');
INSERT INTO `biz_student` VALUES (375, 481, '15', '2020', '5');
INSERT INTO `biz_student` VALUES (376, 482, '16', '2020', '5');
INSERT INTO `biz_student` VALUES (377, 483, '17', '2020', '5');
INSERT INTO `biz_student` VALUES (378, 484, '18', '2020', '5');
INSERT INTO `biz_student` VALUES (379, 485, '19', '2020', '5');
INSERT INTO `biz_student` VALUES (380, 486, '20', '2020', '5');
INSERT INTO `biz_student` VALUES (381, 487, '21', '2020', '5');
INSERT INTO `biz_student` VALUES (382, 488, '22', '2020', '5');
INSERT INTO `biz_student` VALUES (383, 489, '23', '2020', '5');
INSERT INTO `biz_student` VALUES (384, 490, '24', '2020', '5');
INSERT INTO `biz_student` VALUES (385, 491, '25', '2020', '5');
INSERT INTO `biz_student` VALUES (386, 492, '26', '2020', '5');
INSERT INTO `biz_student` VALUES (387, 493, '27', '2020', '5');
INSERT INTO `biz_student` VALUES (388, 494, '28', '2020', '5');
INSERT INTO `biz_student` VALUES (389, 495, '29', '2020', '5');
INSERT INTO `biz_student` VALUES (390, 496, '30', '2020', '5');
INSERT INTO `biz_student` VALUES (391, 497, '32', '2020', '5');
INSERT INTO `biz_student` VALUES (392, 498, '33', '2020', '5');
INSERT INTO `biz_student` VALUES (393, 499, '34', '2020', '5');
INSERT INTO `biz_student` VALUES (394, 500, '35', '2020', '5');
INSERT INTO `biz_student` VALUES (395, 501, '36', '2020', '5');
INSERT INTO `biz_student` VALUES (396, 502, '37', '2020', '5');
INSERT INTO `biz_student` VALUES (397, 503, '38', '2020', '5');
INSERT INTO `biz_student` VALUES (398, 504, '39', '2020', '5');
INSERT INTO `biz_student` VALUES (399, 505, '40', '2020', '5');
INSERT INTO `biz_student` VALUES (400, 506, '41', '2020', '5');
INSERT INTO `biz_student` VALUES (401, 507, '31', '2020', '5');
INSERT INTO `biz_student` VALUES (402, 508, '42', '2020', '5');
INSERT INTO `biz_student` VALUES (403, 509, '43', '2020', '5');
INSERT INTO `biz_student` VALUES (404, 510, '01', '2020', '6');
INSERT INTO `biz_student` VALUES (405, 511, '02', '2020', '6');
INSERT INTO `biz_student` VALUES (406, 512, '03', '2020', '6');
INSERT INTO `biz_student` VALUES (407, 513, '04', '2020', '6');
INSERT INTO `biz_student` VALUES (408, 514, '05', '2020', '6');
INSERT INTO `biz_student` VALUES (409, 515, '06', '2020', '6');
INSERT INTO `biz_student` VALUES (410, 516, '07', '2020', '6');
INSERT INTO `biz_student` VALUES (411, 517, '08', '2020', '6');
INSERT INTO `biz_student` VALUES (412, 518, '09', '2020', '6');
INSERT INTO `biz_student` VALUES (413, 519, '10', '2020', '6');
INSERT INTO `biz_student` VALUES (414, 520, '11', '2020', '6');
INSERT INTO `biz_student` VALUES (415, 521, '12', '2020', '6');
INSERT INTO `biz_student` VALUES (416, 522, '13', '2020', '6');
INSERT INTO `biz_student` VALUES (417, 523, '14', '2020', '6');
INSERT INTO `biz_student` VALUES (418, 524, '15', '2020', '6');
INSERT INTO `biz_student` VALUES (419, 525, '16', '2020', '6');
INSERT INTO `biz_student` VALUES (420, 526, '17', '2020', '6');
INSERT INTO `biz_student` VALUES (421, 527, '18', '2020', '6');
INSERT INTO `biz_student` VALUES (422, 528, '19', '2020', '6');
INSERT INTO `biz_student` VALUES (423, 529, '20', '2020', '6');
INSERT INTO `biz_student` VALUES (424, 530, '21', '2020', '6');
INSERT INTO `biz_student` VALUES (425, 531, '23', '2020', '6');
INSERT INTO `biz_student` VALUES (426, 532, '24', '2020', '6');
INSERT INTO `biz_student` VALUES (427, 533, '25', '2020', '6');
INSERT INTO `biz_student` VALUES (428, 534, '26', '2020', '6');
INSERT INTO `biz_student` VALUES (429, 535, '27', '2020', '6');
INSERT INTO `biz_student` VALUES (430, 536, '28', '2020', '6');
INSERT INTO `biz_student` VALUES (431, 537, '29', '2020', '6');
INSERT INTO `biz_student` VALUES (432, 538, '30', '2020', '6');
INSERT INTO `biz_student` VALUES (433, 539, '31', '2020', '6');
INSERT INTO `biz_student` VALUES (434, 540, '32', '2020', '6');
INSERT INTO `biz_student` VALUES (435, 541, '33', '2020', '6');
INSERT INTO `biz_student` VALUES (436, 542, '34', '2020', '6');
INSERT INTO `biz_student` VALUES (437, 543, '35', '2020', '6');
INSERT INTO `biz_student` VALUES (438, 544, '36', '2020', '6');
INSERT INTO `biz_student` VALUES (439, 545, '37', '2020', '6');
INSERT INTO `biz_student` VALUES (440, 546, '38', '2020', '6');
INSERT INTO `biz_student` VALUES (441, 547, '39', '2020', '6');
INSERT INTO `biz_student` VALUES (442, 548, '40', '2020', '6');
INSERT INTO `biz_student` VALUES (443, 549, '41', '2020', '6');
INSERT INTO `biz_student` VALUES (444, 550, '22', '2020', '6');
INSERT INTO `biz_student` VALUES (445, 551, '42', '2020', '6');

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
  `preview_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '预览状态：pending/converting/success/failed',
  `preview_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '预览文件路径(PDF)',
  PRIMARY KEY (`answer_id`) USING BTREE,
  INDEX `idx_student_lesson`(`student_id` ASC, `lesson_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1812 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生答题记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_student_answer
-- ----------------------------
INSERT INTO `biz_student_answer` VALUES (76, 58, 21, 15, 'B', 0, 0, 0, '2026-01-09 09:05:29', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (77, 58, 21, 29, '错', 1, 10, 0, '2026-01-09 09:05:29', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (80, 61, 21, 43, '一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若伊管理系统，', 0, 5, 73, '2026-01-09 09:11:18', 56, 98.60, 36.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (81, 61, 21, 15, 'A', 1, 10, 0, '2026-01-09 09:11:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (82, 61, 21, 29, '错', 1, 10, 0, '2026-01-09 09:11:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (83, 61, 21, 51, '/profile/upload/2026/01/09/两篇读书报告（每篇1000字以上）_20260109091133A002.docx', 0, 70, 0, '2026-01-09 09:11:34', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (84, 58, 22, 43, '一直想做一款后台管理系统图，看了 优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若伊管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，oa等等', 1, 33, 120, '2026-01-09 09:46:49', 52, 92.90, 55.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (85, 58, 22, 9, 'B', 0, 0, 0, '2026-01-09 09:46:55', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (89, 58, 21, 51, '/profile/upload/2026/01/12/111_20260108085453A001_20260112095924A001.docx', 0, 0, 0, '2026-01-12 09:59:25', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/12/111_20260108085453A001_20260112095924A001.pdf');
INSERT INTO `biz_student_answer` VALUES (93, 58, 21, 43, '', 0, 0, 6, '2026-01-12 10:23:37', 0, 100.00, 0.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (95, 134, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。', 1, 30, 248, '2026-01-12 13:19:41', 32, 100.00, 100.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (96, 132, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用 通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。', 1, 30, 257, '2026-01-12 13:19:47', 30, 99.20, 99.20, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (97, 106, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现', 1, 30, 300, '2026-01-12 13:19:50', 25, 100.00, 94.70, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (98, 136, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，', 1, 26, 300, '2026-01-12 13:19:50', 21, 100.00, 81.70, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (99, 135, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键', 0, 17, 300, '2026-01-12 13:19:53', 17, 100.00, 64.90, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (100, 130, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。', 1, 30, 282, '2026-01-12 13:19:54', 28, 100.00, 100.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (102, 146, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中                                                  ', 0, 10, 300, '2026-01-12 13:19:56', 16, 62.10, 62.60, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (103, 118, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器', 0, 7, 300, '2026-01-12 13:19:58', 11, 100.00, 41.20, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (104, 143, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：', 1, 18, 300, '2026-01-12 13:19:59', 18, 100.00, 67.20, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (105, 145, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉的、', 0, 3, 300, '2026-01-12 13:20:02', 8, 95.20, 30.50, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (106, 124, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象自五成', 0, 8, 299, '2026-01-12 13:20:08', 12, 95.20, 45.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (107, 111, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常', 0, 9, 300, '2026-01-12 13:20:14', 13, 100.00, 48.90, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (108, 129, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调 自动灌溉等。系统通常由控制器 执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现', 1, 19, 300, '2026-01-12 13:20:15', 18, 97.90, 70.20, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (109, 119, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：同', 1, 18, 300, '2026-01-12 13:20:16', 18, 98.90, 67.20, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (110, 128, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，冰场借助传感器获取反馈信息。计算机在其中起光解作用通过', 0, 15, 300, '2026-01-12 13:20:22', 17, 93.30, 63.40, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (112, 144, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常', 0, 5, 300, '2026-01-12 13:20:23', 9, 100.00, 35.10, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (115, 116, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起', 0, 16, 300, '2026-01-12 13:20:26', 17, 100.00, 63.40, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (116, 141, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常有控制器、', 0, 6, 300, '2026-01-12 13:20:27', 10, 98.00, 38.20, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (117, 126, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人', 0, 1, 300, '2026-01-12 13:20:28', 4, 100.00, 16.80, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (118, 108, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈', 0, 12, 300, '2026-01-12 13:20:30', 15, 100.00, 55.70, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (119, 122, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控', 0, 7, 300, '2026-01-12 13:20:31', 11, 100.00, 43.50, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (120, 127, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被', 0, 7, 300, '2026-01-12 13:20:33', 11, 100.00, 42.70, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (121, 138, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息', 0, 13, 300, '2026-01-12 13:20:37', 15, 100.00, 57.30, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (122, 131, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用；通过指令实现更精准、复杂和只能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。', 1, 30, 290, '2026-01-12 13:20:37', 27, 98.50, 98.50, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (123, 125, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调 ', 0, 3, 300, '2026-01-12 13:20:38', 7, 97.20, 26.70, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (124, 120, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机', 0, 14, 300, '2026-01-12 13:20:41', 16, 100.00, 60.30, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (125, 121, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常', 0, 5, 299, '2026-01-12 13:20:51', 9, 100.00, 35.10, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (126, 133, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指', 1, 20, 291, '2026-01-12 13:20:52', 19, 100.00, 69.50, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (127, 139, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、辅助和智能的控制', 1, 24, 300, '2026-01-12 13:20:56', 21, 98.10, 79.40, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (128, 115, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并长街主创阿奇获取反馈信息。计算机在其中奇幻建作用：通过指令实现更精准、复杂和智能的控制', 1, 20, 300, '2026-01-12 13:20:58', 19, 91.50, 74.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (129, 114, 23, 54, '第一节自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用；通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更', 1, 30, 300, '2026-01-12 13:21:00', 24, 97.60, 93.10, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (130, 140, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常', 0, 5, 300, '2026-01-12 13:21:04', 9, 100.00, 35.10, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (131, 106, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:21:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (132, 106, 23, 29, '错', 1, 5, 0, '2026-01-12 13:21:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (133, 106, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:21:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (134, 106, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:21:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (135, 106, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:21:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (136, 106, 23, 59, '对', 0, 0, 0, '2026-01-12 13:21:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (137, 106, 23, 60, '对', 1, 5, 0, '2026-01-12 13:21:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (138, 106, 23, 61, '对', 1, 5, 0, '2026-01-12 13:21:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (139, 147, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并长借助传感器获取fan亏', 0, 10, 300, '2026-01-12 13:21:10', 14, 93.30, 53.40, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (140, 112, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常', 0, 9, 300, '2026-01-12 13:21:19', 13, 100.00, 48.90, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (141, 137, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人关于下自动实现目标，如空调 自动灌溉的。系统通常由控制器 ', 0, 4, 300, '2026-01-12 13:21:23', 9, 90.20, 35.10, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (144, 134, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:21:50', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (145, 134, 23, 29, '错', 1, 5, 0, '2026-01-12 13:21:50', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (146, 134, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:21:50', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (147, 134, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:21:50', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (148, 134, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:21:50', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (149, 134, 23, 59, '错', 1, 5, 0, '2026-01-12 13:21:50', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (150, 134, 23, 60, '对', 1, 5, 0, '2026-01-12 13:21:50', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (151, 134, 23, 61, '对', 1, 5, 0, '2026-01-12 13:21:50', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (152, 108, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:21:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (153, 108, 23, 29, '错', 1, 5, 0, '2026-01-12 13:21:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (154, 108, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:21:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (155, 108, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:21:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (156, 108, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:21:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (157, 108, 23, 59, '错', 1, 5, 0, '2026-01-12 13:21:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (158, 108, 23, 60, '对', 1, 5, 0, '2026-01-12 13:21:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (159, 108, 23, 61, '对', 1, 5, 0, '2026-01-12 13:21:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (160, 123, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机', 0, 14, 296, '2026-01-12 13:21:58', 16, 100.00, 60.30, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (161, 110, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更', 1, 30, 187, '2026-01-12 13:22:01', 31, 100.00, 72.50, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (162, 144, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:22:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (163, 144, 23, 29, '错', 1, 5, 0, '2026-01-12 13:22:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (164, 144, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:22:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (165, 144, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:22:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (166, 144, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:22:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (167, 144, 23, 59, '错', 1, 5, 0, '2026-01-12 13:22:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (168, 144, 23, 60, '对', 1, 5, 0, '2026-01-12 13:22:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (169, 144, 23, 61, '错', 0, 0, 0, '2026-01-12 13:22:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (170, 111, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:22:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (171, 111, 23, 29, '错', 1, 5, 0, '2026-01-12 13:22:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (172, 111, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:22:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (173, 111, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:22:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (174, 111, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:22:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (175, 111, 23, 59, '对', 0, 0, 0, '2026-01-12 13:22:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (176, 111, 23, 60, '对', 1, 5, 0, '2026-01-12 13:22:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (177, 111, 23, 61, '对', 1, 5, 0, '2026-01-12 13:22:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (178, 143, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:22:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (179, 143, 23, 29, '错', 1, 5, 0, '2026-01-12 13:22:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (180, 143, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:22:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (181, 143, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:22:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (182, 143, 23, 58, 'A', 0, 0, 0, '2026-01-12 13:22:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (183, 143, 23, 59, '错', 1, 5, 0, '2026-01-12 13:22:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (184, 143, 23, 60, '对', 1, 5, 0, '2026-01-12 13:22:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (185, 143, 23, 61, '对', 1, 5, 0, '2026-01-12 13:22:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (186, 121, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:22:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (187, 121, 23, 29, '错', 1, 5, 0, '2026-01-12 13:22:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (188, 121, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:22:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (189, 121, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:22:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (190, 121, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:22:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (191, 121, 23, 59, '错', 1, 5, 0, '2026-01-12 13:22:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (192, 121, 23, 60, '对', 1, 5, 0, '2026-01-12 13:22:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (193, 121, 23, 61, '错', 0, 0, 0, '2026-01-12 13:22:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (194, 130, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:22:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (195, 130, 23, 29, '错', 1, 5, 0, '2026-01-12 13:22:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (196, 130, 23, 56, 'D', 0, 0, 0, '2026-01-12 13:22:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (197, 130, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:22:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (198, 130, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:22:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (199, 130, 23, 59, '错', 1, 5, 0, '2026-01-12 13:22:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (200, 130, 23, 60, '对', 1, 5, 0, '2026-01-12 13:22:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (201, 130, 23, 61, '对', 1, 5, 0, '2026-01-12 13:22:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (202, 136, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:22:15', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (203, 136, 23, 29, '错', 1, 5, 0, '2026-01-12 13:22:15', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (204, 136, 23, 56, 'A', 0, 0, 0, '2026-01-12 13:22:15', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (205, 136, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:22:15', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (206, 136, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:22:15', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (207, 136, 23, 59, '错', 1, 5, 0, '2026-01-12 13:22:15', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (208, 136, 23, 60, '对', 1, 5, 0, '2026-01-12 13:22:15', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (209, 136, 23, 61, '对', 1, 5, 0, '2026-01-12 13:22:15', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (210, 133, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:22:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (211, 133, 23, 29, '错', 1, 5, 0, '2026-01-12 13:22:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (212, 133, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:22:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (213, 133, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:22:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (214, 133, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:22:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (215, 133, 23, 59, '错', 1, 5, 0, '2026-01-12 13:22:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (216, 133, 23, 60, '对', 1, 5, 0, '2026-01-12 13:22:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (217, 133, 23, 61, '对', 1, 5, 0, '2026-01-12 13:22:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (218, 128, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:22:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (219, 128, 23, 29, '错', 1, 5, 0, '2026-01-12 13:22:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (220, 128, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:22:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (221, 128, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:22:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (222, 128, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:22:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (223, 128, 23, 59, '错', 1, 5, 0, '2026-01-12 13:22:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (224, 128, 23, 60, '对', 1, 5, 0, '2026-01-12 13:22:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (225, 128, 23, 61, '对', 1, 5, 0, '2026-01-12 13:22:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (226, 129, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:22:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (227, 129, 23, 29, '错', 1, 5, 0, '2026-01-12 13:22:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (228, 129, 23, 56, 'D', 0, 0, 0, '2026-01-12 13:22:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (229, 129, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:22:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (230, 129, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:22:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (231, 129, 23, 59, '错', 1, 5, 0, '2026-01-12 13:22:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (232, 129, 23, 60, '对', 1, 5, 0, '2026-01-12 13:22:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (233, 129, 23, 61, '对', 1, 5, 0, '2026-01-12 13:22:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (242, 135, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:22:29', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (243, 135, 23, 29, '错', 1, 5, 0, '2026-01-12 13:22:29', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (244, 135, 23, 56, 'A', 0, 0, 0, '2026-01-12 13:22:29', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (245, 135, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:22:29', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (246, 135, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:22:29', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (247, 135, 23, 59, '错', 1, 5, 0, '2026-01-12 13:22:29', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (248, 135, 23, 60, '对', 1, 5, 0, '2026-01-12 13:22:29', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (249, 135, 23, 61, '对', 1, 5, 0, '2026-01-12 13:22:29', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (250, 120, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:22:31', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (251, 120, 23, 29, '错', 1, 5, 0, '2026-01-12 13:22:31', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (252, 120, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:22:31', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (253, 120, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:22:31', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (254, 120, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:22:31', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (255, 120, 23, 59, '错', 1, 5, 0, '2026-01-12 13:22:31', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (256, 120, 23, 60, '对', 1, 5, 0, '2026-01-12 13:22:31', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (257, 120, 23, 61, '对', 1, 5, 0, '2026-01-12 13:22:31', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (258, 125, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:22:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (259, 125, 23, 29, '错', 1, 5, 0, '2026-01-12 13:22:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (260, 125, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:22:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (261, 125, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:22:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (262, 125, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:22:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (263, 125, 23, 59, '错', 1, 5, 0, '2026-01-12 13:22:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (264, 125, 23, 60, '对', 1, 5, 0, '2026-01-12 13:22:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (265, 125, 23, 61, '错', 0, 0, 0, '2026-01-12 13:22:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (266, 118, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:22:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (267, 118, 23, 29, '错', 1, 5, 0, '2026-01-12 13:22:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (268, 118, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:22:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (269, 118, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:22:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (270, 118, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:22:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (271, 118, 23, 59, '错', 1, 5, 0, '2026-01-12 13:22:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (272, 118, 23, 60, '对', 1, 5, 0, '2026-01-12 13:22:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (273, 118, 23, 61, '错', 0, 0, 0, '2026-01-12 13:22:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (274, 147, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:22:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (275, 147, 23, 29, '错', 1, 5, 0, '2026-01-12 13:22:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (276, 147, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:22:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (277, 147, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:22:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (278, 147, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:22:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (279, 147, 23, 59, '错', 1, 5, 0, '2026-01-12 13:22:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (280, 147, 23, 60, '对', 1, 5, 0, '2026-01-12 13:22:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (281, 147, 23, 61, '错', 0, 0, 0, '2026-01-12 13:22:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (282, 119, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:22:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (283, 119, 23, 29, '错', 1, 5, 0, '2026-01-12 13:22:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (284, 119, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:22:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (285, 119, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:22:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (286, 119, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:22:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (287, 119, 23, 59, '错', 1, 5, 0, '2026-01-12 13:22:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (288, 119, 23, 60, '对', 1, 5, 0, '2026-01-12 13:22:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (289, 119, 23, 61, '对', 1, 5, 0, '2026-01-12 13:22:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (290, 115, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:22:47', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (291, 115, 23, 29, '错', 1, 5, 0, '2026-01-12 13:22:47', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (292, 115, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:22:47', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (293, 115, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:22:47', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (294, 115, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:22:47', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (295, 115, 23, 59, '错', 1, 5, 0, '2026-01-12 13:22:47', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (296, 115, 23, 60, '对', 1, 5, 0, '2026-01-12 13:22:47', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (297, 115, 23, 61, '错', 0, 0, 0, '2026-01-12 13:22:47', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (299, 127, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:22:51', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (300, 127, 23, 29, '错', 1, 5, 0, '2026-01-12 13:22:51', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (301, 127, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:22:51', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (302, 127, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:22:51', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (303, 127, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:22:51', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (304, 127, 23, 59, '错', 1, 5, 0, '2026-01-12 13:22:51', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (305, 127, 23, 60, '对', 1, 5, 0, '2026-01-12 13:22:51', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (306, 127, 23, 61, '对', 1, 5, 0, '2026-01-12 13:22:51', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (307, 122, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:22:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (308, 122, 23, 29, '错', 1, 5, 0, '2026-01-12 13:22:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (309, 122, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:22:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (310, 122, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:22:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (311, 122, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:22:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (312, 122, 23, 59, '错', 1, 5, 0, '2026-01-12 13:22:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (313, 122, 23, 60, '对', 1, 5, 0, '2026-01-12 13:22:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (314, 122, 23, 61, '错', 0, 0, 0, '2026-01-12 13:22:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (315, 139, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:23:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (316, 139, 23, 29, '错', 1, 5, 0, '2026-01-12 13:23:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (317, 139, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:23:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (318, 139, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:23:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (319, 139, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:23:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (320, 139, 23, 59, '错', 1, 5, 0, '2026-01-12 13:23:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (321, 139, 23, 60, '对', 1, 5, 0, '2026-01-12 13:23:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (322, 139, 23, 61, '对', 1, 5, 0, '2026-01-12 13:23:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (323, 123, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:23:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (324, 123, 23, 29, '错', 1, 5, 0, '2026-01-12 13:23:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (325, 123, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:23:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (326, 123, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:23:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (327, 123, 23, 58, 'D', 0, 0, 0, '2026-01-12 13:23:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (328, 123, 23, 59, '错', 1, 5, 0, '2026-01-12 13:23:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (329, 123, 23, 60, '对', 1, 5, 0, '2026-01-12 13:23:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (330, 123, 23, 61, '错', 0, 0, 0, '2026-01-12 13:23:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (331, 126, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:23:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (332, 126, 23, 29, '错', 1, 5, 0, '2026-01-12 13:23:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (333, 126, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:23:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (334, 126, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:23:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (335, 126, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:23:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (336, 126, 23, 59, '错', 1, 5, 0, '2026-01-12 13:23:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (337, 126, 23, 60, '对', 1, 5, 0, '2026-01-12 13:23:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (338, 126, 23, 61, '错', 0, 0, 0, '2026-01-12 13:23:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (339, 146, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:23:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (340, 146, 23, 29, '错', 1, 5, 0, '2026-01-12 13:23:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (341, 146, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:23:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (342, 146, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:23:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (343, 146, 23, 58, 'B', 0, 0, 0, '2026-01-12 13:23:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (344, 146, 23, 59, '错', 1, 5, 0, '2026-01-12 13:23:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (345, 146, 23, 60, '对', 1, 5, 0, '2026-01-12 13:23:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (346, 146, 23, 61, '对', 1, 5, 0, '2026-01-12 13:23:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (347, 124, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:23:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (348, 124, 23, 29, '错', 1, 5, 0, '2026-01-12 13:23:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (349, 124, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:23:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (350, 124, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:23:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (351, 124, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:23:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (352, 124, 23, 59, '错', 1, 5, 0, '2026-01-12 13:23:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (353, 124, 23, 60, '对', 1, 5, 0, '2026-01-12 13:23:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (354, 124, 23, 61, '对', 1, 5, 0, '2026-01-12 13:23:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (355, 141, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:23:23', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (356, 141, 23, 29, '错', 1, 5, 0, '2026-01-12 13:23:23', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (357, 141, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:23:23', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (358, 141, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:23:23', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (359, 141, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:23:23', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (360, 141, 23, 59, '错', 1, 5, 0, '2026-01-12 13:23:23', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (361, 141, 23, 60, '对', 1, 5, 0, '2026-01-12 13:23:23', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (362, 141, 23, 61, '错', 0, 0, 0, '2026-01-12 13:23:23', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (363, 142, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:23:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (364, 142, 23, 29, '错', 1, 5, 0, '2026-01-12 13:23:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (365, 142, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:23:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (366, 142, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:23:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (367, 142, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:23:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (368, 142, 23, 59, '错', 1, 5, 0, '2026-01-12 13:23:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (369, 142, 23, 60, '对', 1, 5, 0, '2026-01-12 13:23:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (370, 142, 23, 61, '错', 0, 0, 0, '2026-01-12 13:23:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (371, 140, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:23:29', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (372, 140, 23, 29, '错', 1, 5, 0, '2026-01-12 13:23:29', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (373, 140, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:23:29', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (374, 140, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:23:29', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (375, 140, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:23:29', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (376, 140, 23, 59, '错', 1, 5, 0, '2026-01-12 13:23:29', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (377, 140, 23, 60, '对', 1, 5, 0, '2026-01-12 13:23:29', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (378, 140, 23, 61, '对', 1, 5, 0, '2026-01-12 13:23:29', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (379, 110, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:23:30', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (380, 110, 23, 29, '错', 1, 5, 0, '2026-01-12 13:23:30', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (381, 110, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:23:30', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (382, 110, 23, 57, 'A', 0, 0, 0, '2026-01-12 13:23:30', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (383, 110, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:23:30', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (384, 110, 23, 59, '错', 1, 5, 0, '2026-01-12 13:23:30', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (385, 110, 23, 60, '对', 1, 5, 0, '2026-01-12 13:23:30', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (386, 110, 23, 61, '对', 1, 5, 0, '2026-01-12 13:23:30', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (387, 116, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:23:31', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (388, 116, 23, 29, '错', 1, 5, 0, '2026-01-12 13:23:31', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (389, 116, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:23:31', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (390, 116, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:23:31', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (391, 116, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:23:31', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (392, 116, 23, 59, '错', 1, 5, 0, '2026-01-12 13:23:31', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (393, 116, 23, 60, '对', 1, 5, 0, '2026-01-12 13:23:31', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (394, 116, 23, 61, '错', 0, 0, 0, '2026-01-12 13:23:31', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (395, 145, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:23:37', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (396, 145, 23, 29, '错', 1, 5, 0, '2026-01-12 13:23:37', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (397, 145, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:23:37', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (398, 145, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:23:37', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (399, 145, 23, 58, 'B', 0, 0, 0, '2026-01-12 13:23:37', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (400, 145, 23, 59, '错', 1, 5, 0, '2026-01-12 13:23:37', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (401, 145, 23, 60, '对', 1, 5, 0, '2026-01-12 13:23:37', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (402, 145, 23, 61, '对', 1, 5, 0, '2026-01-12 13:23:37', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (403, 137, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:23:52', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (404, 137, 23, 29, '错', 1, 5, 0, '2026-01-12 13:23:52', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (405, 137, 23, 56, 'A', 0, 0, 0, '2026-01-12 13:23:52', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (406, 137, 23, 57, 'A', 0, 0, 0, '2026-01-12 13:23:52', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (407, 137, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:23:52', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (408, 137, 23, 59, '错', 1, 5, 0, '2026-01-12 13:23:52', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (409, 137, 23, 60, '对', 1, 5, 0, '2026-01-12 13:23:52', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (410, 137, 23, 61, '对', 1, 5, 0, '2026-01-12 13:23:52', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (411, 132, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:23:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (412, 132, 23, 29, '错', 1, 5, 0, '2026-01-12 13:23:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (413, 132, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:23:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (414, 132, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:23:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (415, 132, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:23:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (416, 132, 23, 59, '错', 1, 5, 0, '2026-01-12 13:23:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (417, 132, 23, 60, '对', 1, 5, 0, '2026-01-12 13:23:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (418, 132, 23, 61, '对', 1, 5, 0, '2026-01-12 13:23:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (419, 112, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:24:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (420, 112, 23, 29, '错', 1, 5, 0, '2026-01-12 13:24:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (421, 112, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:24:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (422, 112, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:24:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (423, 112, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:24:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (424, 112, 23, 59, '错', 1, 5, 0, '2026-01-12 13:24:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (425, 112, 23, 60, '对', 1, 5, 0, '2026-01-12 13:24:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (426, 112, 23, 61, '对', 1, 5, 0, '2026-01-12 13:24:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (427, 138, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:24:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (428, 138, 23, 29, '错', 1, 5, 0, '2026-01-12 13:24:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (429, 138, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:24:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (430, 138, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:24:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (431, 138, 23, 58, 'B', 0, 0, 0, '2026-01-12 13:24:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (432, 138, 23, 59, '错', 1, 5, 0, '2026-01-12 13:24:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (433, 138, 23, 60, '对', 1, 5, 0, '2026-01-12 13:24:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (434, 138, 23, 61, '对', 1, 5, 0, '2026-01-12 13:24:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (435, 131, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:24:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (436, 131, 23, 29, '错', 1, 5, 0, '2026-01-12 13:24:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (437, 131, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:24:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (438, 131, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:24:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (439, 131, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:24:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (440, 131, 23, 59, '错', 1, 5, 0, '2026-01-12 13:24:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (441, 131, 23, 60, '对', 1, 5, 0, '2026-01-12 13:24:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (442, 131, 23, 61, '对', 1, 5, 0, '2026-01-12 13:24:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (443, 142, 23, 54, '第', 0, 0, 19, '2026-01-12 13:24:22', 3, 100.00, 0.80, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (445, 114, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:24:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (446, 114, 23, 29, '错', 1, 5, 0, '2026-01-12 13:24:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (447, 114, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:24:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (448, 114, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:24:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (449, 114, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:24:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (450, 114, 23, 59, '错', 1, 5, 0, '2026-01-12 13:24:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (451, 114, 23, 60, '对', 1, 5, 0, '2026-01-12 13:24:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (452, 114, 23, 61, '对', 1, 5, 0, '2026-01-12 13:24:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (453, 109, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:24:36', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (454, 109, 23, 29, '错', 1, 5, 0, '2026-01-12 13:24:36', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (455, 109, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:24:36', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (456, 109, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:24:36', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (457, 109, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:24:36', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (458, 109, 23, 59, '错', 1, 5, 0, '2026-01-12 13:24:36', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (459, 109, 23, 60, '对', 1, 5, 0, '2026-01-12 13:24:36', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (460, 109, 23, 61, '对', 1, 5, 0, '2026-01-12 13:24:36', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (461, 105, 23, 22, 'C', 0, 0, 0, '2026-01-12 13:25:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (462, 105, 23, 29, '错', 1, 5, 0, '2026-01-12 13:25:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (463, 105, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:25:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (464, 105, 23, 57, 'C', 0, 0, 0, '2026-01-12 13:25:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (465, 105, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:25:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (466, 105, 23, 59, '错', 1, 5, 0, '2026-01-12 13:25:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (467, 105, 23, 60, '对', 1, 5, 0, '2026-01-12 13:25:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (468, 105, 23, 61, '错', 0, 0, 0, '2026-01-12 13:25:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (469, 105, 23, 54, '', 0, 0, 300, '2026-01-12 13:29:36', 0, 100.00, 0.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (472, 117, 23, 22, 'A', 1, 5, 0, '2026-01-12 13:47:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (473, 117, 23, 29, '错', 1, 5, 0, '2026-01-12 13:47:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (474, 117, 23, 56, 'B', 1, 5, 0, '2026-01-12 13:47:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (475, 117, 23, 57, 'B', 1, 5, 0, '2026-01-12 13:47:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (476, 117, 23, 58, 'C', 1, 5, 0, '2026-01-12 13:47:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (477, 117, 23, 59, '错', 1, 5, 0, '2026-01-12 13:47:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (478, 117, 23, 60, '对', 1, 5, 0, '2026-01-12 13:47:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (479, 117, 23, 61, '错', 0, 0, 0, '2026-01-12 13:47:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (481, 128, 23, 55, '/profile/upload/2026/01/12/学习单_20260112125728A001_20260112134957A003.docx', 0, 0, 0, '2026-01-12 13:49:58', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/12/学习单_20260112125728A001_20260112134957A003.pdf');
INSERT INTO `biz_student_answer` VALUES (483, 106, 23, 55, '/profile/upload/2026/01/12/学习单_20260112125728A001 (1)_20260112135009A004.docx', 0, 0, 0, '2026-01-12 13:50:09', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/12/学习单_20260112125728A001 (1)_20260112135009A004.pdf');
INSERT INTO `biz_student_answer` VALUES (484, 117, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现', 0, 4, 132, '2026-01-12 13:50:11', 13, 100.00, 22.10, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (485, 122, 23, 55, '/profile/upload/2026/01/12/学习单_20260112125728A001_20260112135021A005.docx', 0, 0, 0, '2026-01-12 13:50:22', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/12/学习单_20260112125728A001_20260112135021A005.pdf');
INSERT INTO `biz_student_answer` VALUES (486, 129, 23, 55, '/profile/upload/2026/01/12/1_20260112135046A006.docx', 0, 0, 0, '2026-01-12 13:50:47', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/12/1_20260112135046A006.pdf');
INSERT INTO `biz_student_answer` VALUES (487, 109, 23, 55, '/profile/upload/2026/01/12/学习单_20260112125728A001 (2)_20260112135051A007.docx', 0, 0, 0, '2026-01-12 13:50:52', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/12/学习单_20260112125728A001 (2)_20260112135051A007.pdf');
INSERT INTO `biz_student_answer` VALUES (488, 132, 23, 55, '/profile/upload/2026/01/12/学习单_20260112125728A001_20260112135052A008.docx', 0, 0, 0, '2026-01-12 13:50:52', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/12/学习单_20260112125728A001_20260112135052A008.pdf');
INSERT INTO `biz_student_answer` VALUES (489, 131, 23, 55, '/profile/upload/2026/01/12/学习单_20260112125728A001_20260112135122A009.docx', 0, 0, 0, '2026-01-12 13:51:22', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/12/学习单_20260112125728A001_20260112135122A009.pdf');
INSERT INTO `biz_student_answer` VALUES (490, 121, 23, 55, '/profile/upload/2026/01/12/学习单_20260112125728A001_20260112135122A010.docx', 0, 0, 0, '2026-01-12 13:51:22', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/12/学习单_20260112125728A001_20260112135122A010.pdf');
INSERT INTO `biz_student_answer` VALUES (492, 111, 23, 55, '/profile/upload/2026/01/12/学习单_20260112125728A001_20260112135150A012.docx', 0, 0, 0, '2026-01-12 13:51:51', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/12/学习单_20260112125728A001_20260112135150A012.pdf');
INSERT INTO `biz_student_answer` VALUES (494, 146, 23, 55, '/profile/upload/2026/01/12/42_20260112135211A013.docx', 0, 0, 0, '2026-01-12 13:52:12', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/12/42_20260112135211A013.pdf');
INSERT INTO `biz_student_answer` VALUES (495, 135, 23, 55, '/profile/upload/2026/01/12/31_20260112135226A014.docx', 0, 0, 0, '2026-01-12 13:52:27', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/12/31_20260112135226A014.pdf');
INSERT INTO `biz_student_answer` VALUES (496, 114, 23, 55, '/profile/upload/2026/01/12/学习单_20260112125728A001_20260112135228A015.docx', 0, 0, 0, '2026-01-12 13:52:29', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/12/学习单_20260112125728A001_20260112135228A015.pdf');
INSERT INTO `biz_student_answer` VALUES (497, 130, 23, 55, '/profile/upload/2026/01/12/学习单_20260112125728A001_20260112135240A016.docx', 0, 0, 0, '2026-01-12 13:52:40', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/12/学习单_20260112125728A001_20260112135240A016.pdf');
INSERT INTO `biz_student_answer` VALUES (498, 109, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。', 0, 4, 300, '2026-01-12 13:56:04', 8, 100.00, 32.10, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (499, 134, 23, 55, '/profile/upload/2026/01/12/学习单_20260112125728A001 (3)_20260112135905A017.docx', 0, 0, 0, '2026-01-12 13:59:06', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/12/学习单_20260112125728A001 (3)_20260112135905A017.pdf');
INSERT INTO `biz_student_answer` VALUES (500, 105, 23, 55, '/profile/upload/2026/01/12/60401_20260112135908A018.docx', 0, 0, 0, '2026-01-12 13:59:08', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/12/60401_20260112135908A018.pdf');
INSERT INTO `biz_student_answer` VALUES (501, 139, 23, 55, '/profile/upload/2026/01/12/学习单_20260112125728A001 (3)_20260112135918A019.docx', 0, 0, 0, '2026-01-12 13:59:18', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/12/学习单_20260112125728A001 (3)_20260112135918A019.pdf');
INSERT INTO `biz_student_answer` VALUES (502, 66, 24, 5, 'C', 0, 0, 0, '2026-01-12 14:58:02', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (503, 151, 23, 54, 'gbfgbfgbf介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉', 0, 9, 74, '2026-01-13 09:30:00', 25, 77.50, 23.70, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (504, 151, 23, 22, 'A', 1, 5, 0, '2026-01-13 09:30:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (505, 151, 23, 29, '对', 0, 0, 0, '2026-01-13 09:30:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (506, 151, 23, 56, 'B', 1, 5, 0, '2026-01-13 09:30:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (507, 151, 23, 57, 'A', 0, 0, 0, '2026-01-13 09:30:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (508, 151, 23, 61, '对', 1, 5, 0, '2026-01-13 09:30:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (509, 151, 23, 63, 'D', 0, 0, 0, '2026-01-13 09:30:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (510, 151, 23, 55, '/profile/upload/2026/01/13/111_20260108085453A001 (1)_20260113093030A001.docx', 0, 15, 0, '2026-01-13 09:30:31', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/111_20260108085453A001 (1)_20260113093030A001.pdf');
INSERT INTO `biz_student_answer` VALUES (511, 150, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标', 1, 28, 32, '2026-01-13 09:32:06', 58, 100.00, 23.70, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (512, 150, 23, 22, 'C', 0, 0, 0, '2026-01-13 09:32:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (513, 150, 23, 29, '对', 0, 0, 0, '2026-01-13 09:32:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (514, 150, 23, 56, 'C', 0, 0, 0, '2026-01-13 09:32:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (515, 150, 23, 57, 'A', 0, 0, 0, '2026-01-13 09:32:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (516, 150, 23, 61, '对', 1, 5, 0, '2026-01-13 09:32:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (517, 150, 23, 63, 'C', 1, 10, 0, '2026-01-13 09:32:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (518, 150, 23, 55, '/profile/upload/2026/01/13/六年级信息科技测试题（含答题卡）_20260105160515A001_20260113093228A002.docx', 0, 30, 0, '2026-01-13 09:32:29', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/六年级信息科技测试题（含答题卡）_20260105160515A001_20260113093228A002.pdf');
INSERT INTO `biz_student_answer` VALUES (519, 67, 23, 22, 'C', 0, 0, 0, '2026-01-13 11:33:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (520, 67, 23, 56, 'B', 1, 5, 0, '2026-01-13 11:33:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (521, 67, 23, 57, 'B', 1, 5, 0, '2026-01-13 11:33:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (522, 67, 23, 59, '对', 0, 0, 0, '2026-01-13 11:33:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (523, 67, 23, 61, '对', 1, 5, 0, '2026-01-13 11:33:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (524, 67, 23, 63, 'B', 0, 0, 0, '2026-01-13 11:33:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (525, 154, 23, 22, 'B', 0, 0, 0, '2026-01-13 11:34:54', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (526, 154, 23, 29, '对', 0, 0, 0, '2026-01-13 11:34:54', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (527, 154, 23, 56, 'B', 1, 5, 0, '2026-01-13 11:34:54', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (528, 154, 23, 57, 'B', 1, 5, 0, '2026-01-13 11:34:54', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (529, 154, 23, 61, '对', 1, 5, 0, '2026-01-13 11:34:54', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (530, 154, 23, 63, 'A', 0, 0, 0, '2026-01-13 11:34:54', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (531, 161, 23, 22, 'B', 0, 0, 0, '2026-01-13 13:02:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (532, 161, 23, 57, 'A', 0, 0, 0, '2026-01-13 13:02:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (533, 161, 23, 58, 'B', 0, 0, 0, '2026-01-13 13:02:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (534, 161, 23, 59, '对', 0, 0, 0, '2026-01-13 13:02:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (535, 161, 23, 60, '错', 0, 0, 0, '2026-01-13 13:02:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (536, 161, 23, 63, 'A', 0, 0, 0, '2026-01-13 13:02:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (537, 205, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制，', 1, 40, 221, '2026-01-13 13:24:18', 35, 99.20, 99.20, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (538, 212, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用；通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。', 1, 40, 235, '2026-01-13 13:24:43', 33, 99.20, 99.20, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (539, 220, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。', 1, 40, 256, '2026-01-13 13:25:10', 31, 100.00, 100.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (540, 203, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常有控制器、执行器和被控对象组成，并常借助传感器获取反馈形象，计算机在其中起关键擢用：通常指令实现更精准、复杂和只能的控制，提升系统灵活性与效率。计算机能实现更智能的控制', 1, 40, 273, '2026-01-13 13:25:19', 27, 94.60, 93.90, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (541, 202, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。', 1, 40, 275, '2026-01-13 13:25:20', 29, 100.00, 100.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (542, 209, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制', 1, 40, 277, '2026-01-13 13:25:31', 28, 100.00, 99.20, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (543, 221, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用:通过指令实现更精准、', 1, 28, 300, '2026-01-13 13:25:32', 19, 99.00, 74.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (544, 222, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过', 1, 25, 296, '2026-01-13 13:25:33', 18, 100.00, 68.70, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (545, 195, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常有控制器、执行器和被控对象组成，并常借馋俺器获取', 0, 11, 300, '2026-01-13 13:25:35', 13, 91.40, 48.90, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (546, 192, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用', 0, 23, 300, '2026-01-13 13:25:35', 17, 100.00, 66.40, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (547, 197, 23, 54, '第一课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器。执行器和带控带向组成，定常', 0, 9, 300, '2026-01-13 13:25:37', 12, 90.60, 44.30, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (548, 215, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常', 0, 13, 300, '2026-01-13 13:25:37', 13, 100.00, 48.90, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (549, 232, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并', 0, 12, 300, '2026-01-13 13:25:38', 13, 100.00, 48.10, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (550, 206, 23, 54, '第1课自动丛植系统介绍了自动超值系统能在无人干预下自动实现目标，如楤调、自动灌溉等。系统通常由从之前、执行', 0, 5, 300, '2026-01-13 13:25:41', 9, 84.90, 34.40, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (551, 200, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调，自动灌溉等。系统通常由控制器，执行器和被控对象组成，并常', 0, 11, 300, '2026-01-13 13:25:42', 12, 96.90, 47.30, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (552, 207, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，好常借助传感器', 0, 14, 300, '2026-01-13 13:25:43', 14, 98.60, 51.90, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (553, 194, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中其关键', 0, 21, 300, '2026-01-13 13:25:44', 17, 98.80, 64.10, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (554, 214, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象', 0, 11, 300, '2026-01-13 13:25:44', 12, 100.00, 45.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (555, 213, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂', 1, 31, 300, '2026-01-13 13:25:44', 20, 100.00, 76.30, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (556, 229, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中', 0, 21, 300, '2026-01-13 13:25:46', 16, 100.00, 62.60, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (557, 227, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取', 0, 15, 300, '2026-01-13 13:25:48', 14, 100.00, 54.20, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (558, 198, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、', 1, 29, 300, '2026-01-13 13:25:49', 20, 100.00, 74.80, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (559, 199, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和只能的控制，提升系统灵活性与效率。计算机', 1, 40, 300, '2026-01-13 13:25:51', 24, 99.20, 91.60, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (560, 193, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、', 1, 29, 300, '2026-01-13 13:25:51', 20, 100.00, 74.80, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (561, 216, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并', 0, 12, 299, '2026-01-13 13:25:55', 13, 100.00, 48.10, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (562, 210, 23, 54, '第1课自动超值系统介绍了自动控制系统能在无人关于下自动实现目标，如空调、自动钢管等。系统通常由控制器、执行器和被控对象组成，吧常', 0, 9, 300, '2026-01-13 13:25:56', 11, 89.10, 43.50, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (563, 201, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。', 0, 18, 300, '2026-01-13 13:25:57', 15, 100.00, 58.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (564, 208, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。通常', 0, 5, 300, '2026-01-13 13:26:00', 8, 95.50, 32.10, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (565, 230, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键', 0, 22, 300, '2026-01-13 13:26:01', 17, 100.00, 64.90, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (566, 228, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。', 0, 18, 300, '2026-01-13 13:26:02', 15, 100.00, 58.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (568, 204, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现', 1, 40, 300, '2026-01-13 13:26:02', 25, 100.00, 94.70, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (569, 217, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现', 1, 40, 300, '2026-01-13 13:26:03', 25, 100.00, 94.70, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (570, 196, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起光解作用：', 0, 22, 300, '2026-01-13 13:26:03', 17, 97.70, 65.60, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (571, 218, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌慨等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通常指令实现更精准、复杂和', 1, 29, 300, '2026-01-13 13:26:06', 20, 98.00, 75.60, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (572, 224, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现', 1, 27, 300, '2026-01-13 13:26:11', 19, 100.00, 71.80, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (573, 225, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调,自动灌溉', 0, 5, 300, '2026-01-13 13:26:12', 8, 97.50, 29.80, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (574, 205, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:26:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (575, 205, 23, 29, '错', 1, 5, 0, '2026-01-13 13:26:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (576, 205, 23, 56, 'B', 1, 5, 0, '2026-01-13 13:26:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (577, 205, 23, 57, 'B', 1, 5, 0, '2026-01-13 13:26:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (578, 205, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:26:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (579, 205, 23, 59, '对', 0, 0, 0, '2026-01-13 13:26:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (580, 222, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:26:16', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (581, 222, 23, 29, '错', 1, 5, 0, '2026-01-13 13:26:16', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (582, 222, 23, 57, 'B', 1, 5, 0, '2026-01-13 13:26:16', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (583, 222, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:26:16', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (584, 222, 23, 59, '错', 1, 5, 0, '2026-01-13 13:26:16', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (585, 222, 23, 63, 'C', 1, 5, 0, '2026-01-13 13:26:16', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (586, 209, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:26:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (587, 209, 23, 29, '错', 1, 5, 0, '2026-01-13 13:26:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (588, 209, 23, 56, 'B', 1, 5, 0, '2026-01-13 13:26:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (589, 209, 23, 57, 'B', 1, 5, 0, '2026-01-13 13:26:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (590, 209, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:26:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (591, 209, 23, 60, '对', 1, 5, 0, '2026-01-13 13:26:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (592, 226, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调·自动灌溉等。系统通常由控制器~执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过', 0, 23, 300, '2026-01-13 13:26:19', 18, 97.80, 67.20, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (593, 213, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:26:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (594, 213, 23, 29, '错', 1, 5, 0, '2026-01-13 13:26:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (595, 213, 23, 56, 'B', 1, 5, 0, '2026-01-13 13:26:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (596, 213, 23, 57, 'B', 1, 5, 0, '2026-01-13 13:26:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (597, 213, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:26:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (598, 213, 23, 59, '错', 1, 5, 0, '2026-01-13 13:26:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (599, 223, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，', 0, 3, 300, '2026-01-13 13:26:29', 6, 100.00, 24.40, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (600, 214, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:26:39', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (601, 214, 23, 56, 'B', 1, 5, 0, '2026-01-13 13:26:39', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (602, 214, 23, 57, 'B', 1, 5, 0, '2026-01-13 13:26:39', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (603, 214, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:26:39', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (604, 214, 23, 59, '错', 1, 5, 0, '2026-01-13 13:26:39', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (605, 214, 23, 60, '对', 1, 5, 0, '2026-01-13 13:26:39', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (606, 212, 23, 22, 'B', 0, 0, 0, '2026-01-13 13:26:43', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (607, 212, 23, 29, '错', 1, 5, 0, '2026-01-13 13:26:43', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (608, 212, 23, 56, 'B', 1, 5, 0, '2026-01-13 13:26:43', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (609, 212, 23, 57, 'A', 0, 0, 0, '2026-01-13 13:26:43', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (610, 212, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:26:43', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (611, 212, 23, 59, '错', 1, 5, 0, '2026-01-13 13:26:43', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (612, 233, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和', 0, 9, 300, '2026-01-13 13:26:45', 11, 100.00, 42.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (613, 211, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率', 1, 40, 300, '2026-01-13 13:26:48', 23, 100.00, 89.30, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (614, 207, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:26:51', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (615, 207, 23, 56, 'B', 1, 5, 0, '2026-01-13 13:26:51', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (616, 207, 23, 57, 'B', 1, 5, 0, '2026-01-13 13:26:51', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (617, 207, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:26:51', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (618, 207, 23, 59, '错', 1, 5, 0, '2026-01-13 13:26:51', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (619, 207, 23, 60, '对', 1, 5, 0, '2026-01-13 13:26:51', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (620, 194, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:26:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (621, 194, 23, 56, 'B', 1, 5, 0, '2026-01-13 13:26:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (622, 194, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:26:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (623, 194, 23, 60, '对', 1, 5, 0, '2026-01-13 13:26:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (624, 194, 23, 61, '对', 1, 5, 0, '2026-01-13 13:26:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (625, 194, 23, 63, 'C', 1, 5, 0, '2026-01-13 13:26:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (626, 193, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:26:54', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (627, 193, 23, 56, 'B', 1, 5, 0, '2026-01-13 13:26:54', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (628, 193, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:26:54', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (629, 193, 23, 60, '对', 1, 5, 0, '2026-01-13 13:26:54', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (630, 193, 23, 61, '对', 1, 5, 0, '2026-01-13 13:26:54', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (631, 193, 23, 63, 'C', 1, 5, 0, '2026-01-13 13:26:54', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (632, 231, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能', 1, 40, 300, '2026-01-13 13:26:55', 24, 100.00, 93.10, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (633, 191, 23, 54, '第1课', 0, 0, 27, '2026-01-13 13:26:57', 7, 100.00, 2.30, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (634, 215, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:26:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (635, 215, 23, 56, 'B', 1, 5, 0, '2026-01-13 13:26:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (636, 215, 23, 57, 'B', 1, 5, 0, '2026-01-13 13:26:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (637, 215, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:26:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (638, 215, 23, 59, '错', 1, 5, 0, '2026-01-13 13:26:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (639, 215, 23, 60, '对', 1, 5, 0, '2026-01-13 13:26:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (640, 219, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：用过指令实现更精准、', 1, 28, 300, '2026-01-13 13:26:58', 19, 99.00, 74.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (641, 224, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:26:58', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (642, 224, 23, 29, '错', 1, 5, 0, '2026-01-13 13:26:58', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (643, 224, 23, 57, 'B', 1, 5, 0, '2026-01-13 13:26:58', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (644, 224, 23, 58, 'A', 0, 0, 0, '2026-01-13 13:26:58', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (645, 224, 23, 59, '错', 1, 5, 0, '2026-01-13 13:26:58', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (646, 224, 23, 63, 'B', 0, 0, 0, '2026-01-13 13:26:58', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (647, 216, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:26:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (648, 216, 23, 56, 'B', 1, 5, 0, '2026-01-13 13:26:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (649, 216, 23, 57, 'B', 1, 5, 0, '2026-01-13 13:26:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (650, 216, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:26:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (651, 216, 23, 59, '错', 1, 5, 0, '2026-01-13 13:26:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (652, 216, 23, 60, '对', 1, 5, 0, '2026-01-13 13:26:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (653, 206, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:27:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (654, 206, 23, 56, 'B', 1, 5, 0, '2026-01-13 13:27:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (655, 206, 23, 57, 'B', 1, 5, 0, '2026-01-13 13:27:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (656, 206, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:27:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (657, 206, 23, 59, '错', 1, 5, 0, '2026-01-13 13:27:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (658, 206, 23, 60, '对', 1, 5, 0, '2026-01-13 13:27:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (659, 195, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:27:05', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (660, 195, 23, 56, 'B', 1, 5, 0, '2026-01-13 13:27:05', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (661, 195, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:27:05', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (662, 195, 23, 60, '对', 1, 5, 0, '2026-01-13 13:27:05', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (663, 195, 23, 61, '对', 1, 5, 0, '2026-01-13 13:27:05', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (664, 195, 23, 63, 'C', 1, 5, 0, '2026-01-13 13:27:05', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (665, 230, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:27:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (666, 230, 23, 29, '错', 1, 5, 0, '2026-01-13 13:27:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (667, 230, 23, 56, 'B', 1, 5, 0, '2026-01-13 13:27:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (668, 230, 23, 57, 'B', 1, 5, 0, '2026-01-13 13:27:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (669, 230, 23, 60, '对', 1, 5, 0, '2026-01-13 13:27:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (670, 230, 23, 63, 'B', 0, 0, 0, '2026-01-13 13:27:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (671, 232, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:27:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (672, 232, 23, 29, '错', 1, 5, 0, '2026-01-13 13:27:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (673, 232, 23, 56, 'B', 1, 5, 0, '2026-01-13 13:27:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (674, 232, 23, 57, 'B', 1, 5, 0, '2026-01-13 13:27:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (675, 232, 23, 60, '对', 1, 5, 0, '2026-01-13 13:27:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (676, 232, 23, 63, 'B', 0, 0, 0, '2026-01-13 13:27:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (677, 210, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:27:23', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (678, 210, 23, 29, '错', 1, 5, 0, '2026-01-13 13:27:23', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (679, 210, 23, 56, 'B', 1, 5, 0, '2026-01-13 13:27:23', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (680, 210, 23, 57, 'B', 1, 5, 0, '2026-01-13 13:27:23', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (681, 210, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:27:23', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (682, 210, 23, 60, '对', 1, 5, 0, '2026-01-13 13:27:23', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (683, 226, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:27:29', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (684, 226, 23, 29, '错', 1, 5, 0, '2026-01-13 13:27:29', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (685, 226, 23, 57, 'B', 1, 5, 0, '2026-01-13 13:27:29', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (686, 226, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:27:29', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (687, 226, 23, 61, '错', 0, 0, 0, '2026-01-13 13:27:29', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (688, 226, 23, 63, 'B', 0, 0, 0, '2026-01-13 13:27:29', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (689, 217, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:27:34', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (690, 217, 23, 29, '错', 1, 5, 0, '2026-01-13 13:27:34', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (691, 217, 23, 56, 'B', 1, 5, 0, '2026-01-13 13:27:34', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (692, 217, 23, 57, 'B', 1, 5, 0, '2026-01-13 13:27:34', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (693, 217, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:27:34', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (694, 217, 23, 60, '对', 1, 5, 0, '2026-01-13 13:27:34', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (695, 198, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:27:35', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (696, 198, 23, 56, 'A', 0, 0, 0, '2026-01-13 13:27:35', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (697, 198, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:27:35', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (698, 198, 23, 59, '错', 1, 5, 0, '2026-01-13 13:27:35', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (699, 198, 23, 61, '对', 1, 5, 0, '2026-01-13 13:27:35', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (700, 198, 23, 63, 'B', 0, 0, 0, '2026-01-13 13:27:35', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (701, 221, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:27:47', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (702, 221, 23, 57, 'B', 1, 5, 0, '2026-01-13 13:27:47', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (703, 221, 23, 58, 'D', 0, 0, 0, '2026-01-13 13:27:47', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (704, 221, 23, 59, '错', 1, 5, 0, '2026-01-13 13:27:47', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (705, 221, 23, 61, '对', 1, 5, 0, '2026-01-13 13:27:47', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (706, 221, 23, 63, 'C', 1, 5, 0, '2026-01-13 13:27:47', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (707, 220, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:27:51', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (708, 220, 23, 57, 'B', 1, 5, 0, '2026-01-13 13:27:51', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (709, 220, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:27:51', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (710, 220, 23, 59, '错', 1, 5, 0, '2026-01-13 13:27:51', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (711, 220, 23, 61, '对', 1, 5, 0, '2026-01-13 13:27:51', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (712, 220, 23, 63, 'C', 1, 5, 0, '2026-01-13 13:27:51', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (713, 225, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:27:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (714, 225, 23, 29, '错', 1, 5, 0, '2026-01-13 13:27:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (715, 225, 23, 57, 'B', 1, 5, 0, '2026-01-13 13:27:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (716, 225, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:27:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (717, 225, 23, 61, '错', 0, 0, 0, '2026-01-13 13:27:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (718, 225, 23, 63, 'B', 0, 0, 0, '2026-01-13 13:27:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (719, 202, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:28:00', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (720, 202, 23, 56, 'B', 1, 5, 0, '2026-01-13 13:28:00', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (721, 202, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:28:00', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (722, 202, 23, 60, '对', 1, 5, 0, '2026-01-13 13:28:00', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (723, 202, 23, 61, '对', 1, 5, 0, '2026-01-13 13:28:00', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (724, 202, 23, 63, 'C', 1, 5, 0, '2026-01-13 13:28:00', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (725, 231, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:28:04', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (726, 231, 23, 29, '错', 1, 5, 0, '2026-01-13 13:28:04', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (727, 231, 23, 56, 'B', 1, 5, 0, '2026-01-13 13:28:04', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (728, 231, 23, 57, 'B', 1, 5, 0, '2026-01-13 13:28:04', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (729, 231, 23, 60, '对', 1, 5, 0, '2026-01-13 13:28:04', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (730, 231, 23, 63, 'C', 1, 5, 0, '2026-01-13 13:28:04', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (731, 223, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:28:09', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (732, 223, 23, 29, '错', 1, 5, 0, '2026-01-13 13:28:09', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (733, 223, 23, 57, 'B', 1, 5, 0, '2026-01-13 13:28:09', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (734, 223, 23, 58, 'B', 0, 0, 0, '2026-01-13 13:28:09', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (735, 223, 23, 59, '错', 1, 5, 0, '2026-01-13 13:28:09', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (736, 223, 23, 63, 'C', 1, 5, 0, '2026-01-13 13:28:09', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (737, 196, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:28:09', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (738, 196, 23, 56, 'B', 1, 5, 0, '2026-01-13 13:28:09', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (739, 196, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:28:09', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (740, 196, 23, 59, '错', 1, 5, 0, '2026-01-13 13:28:09', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (741, 196, 23, 61, '对', 1, 5, 0, '2026-01-13 13:28:09', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (742, 196, 23, 63, 'C', 1, 5, 0, '2026-01-13 13:28:09', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (743, 200, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:28:10', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (744, 200, 23, 56, 'B', 1, 5, 0, '2026-01-13 13:28:10', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (745, 200, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:28:10', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (746, 200, 23, 59, '错', 1, 5, 0, '2026-01-13 13:28:10', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (747, 200, 23, 60, '对', 1, 5, 0, '2026-01-13 13:28:10', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (748, 200, 23, 63, 'D', 0, 0, 0, '2026-01-13 13:28:10', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (749, 208, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:28:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (750, 208, 23, 56, 'B', 1, 5, 0, '2026-01-13 13:28:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (751, 208, 23, 57, 'B', 1, 5, 0, '2026-01-13 13:28:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (752, 208, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:28:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (753, 208, 23, 59, '错', 1, 5, 0, '2026-01-13 13:28:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (754, 208, 23, 60, '对', 1, 5, 0, '2026-01-13 13:28:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (755, 227, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:28:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (756, 227, 23, 29, '错', 1, 5, 0, '2026-01-13 13:28:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (757, 227, 23, 57, 'B', 1, 5, 0, '2026-01-13 13:28:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (758, 227, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:28:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (759, 227, 23, 61, '错', 0, 0, 0, '2026-01-13 13:28:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (760, 227, 23, 63, 'B', 0, 0, 0, '2026-01-13 13:28:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (761, 197, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:28:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (762, 197, 23, 56, 'A', 0, 0, 0, '2026-01-13 13:28:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (763, 197, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:28:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (764, 197, 23, 59, '错', 1, 5, 0, '2026-01-13 13:28:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (765, 197, 23, 61, '对', 1, 5, 0, '2026-01-13 13:28:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (766, 197, 23, 63, 'D', 0, 0, 0, '2026-01-13 13:28:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (767, 229, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:28:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (768, 229, 23, 57, 'B', 1, 5, 0, '2026-01-13 13:28:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (769, 229, 23, 58, 'B', 0, 0, 0, '2026-01-13 13:28:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (770, 229, 23, 59, '错', 1, 5, 0, '2026-01-13 13:28:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (771, 229, 23, 61, '错', 0, 0, 0, '2026-01-13 13:28:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (772, 229, 23, 63, 'B', 0, 0, 0, '2026-01-13 13:28:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (773, 199, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:28:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (774, 199, 23, 56, 'B', 1, 5, 0, '2026-01-13 13:28:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (775, 199, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:28:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (776, 199, 23, 59, '错', 1, 5, 0, '2026-01-13 13:28:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (777, 199, 23, 60, '对', 1, 5, 0, '2026-01-13 13:28:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (778, 199, 23, 63, 'C', 1, 5, 0, '2026-01-13 13:28:13', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (779, 228, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:28:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (780, 228, 23, 57, 'B', 1, 5, 0, '2026-01-13 13:28:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (781, 228, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:28:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (782, 228, 23, 59, '错', 1, 5, 0, '2026-01-13 13:28:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (783, 228, 23, 61, '错', 0, 0, 0, '2026-01-13 13:28:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (784, 228, 23, 63, 'B', 0, 0, 0, '2026-01-13 13:28:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (785, 211, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:28:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (786, 211, 23, 29, '错', 1, 5, 0, '2026-01-13 13:28:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (787, 211, 23, 56, 'B', 1, 5, 0, '2026-01-13 13:28:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (788, 211, 23, 57, 'B', 1, 5, 0, '2026-01-13 13:28:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (789, 211, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:28:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (790, 211, 23, 60, '对', 1, 5, 0, '2026-01-13 13:28:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (791, 218, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:28:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (792, 218, 23, 29, '错', 1, 5, 0, '2026-01-13 13:28:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (793, 218, 23, 57, 'B', 1, 5, 0, '2026-01-13 13:28:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (794, 218, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:28:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (795, 218, 23, 61, '对', 1, 5, 0, '2026-01-13 13:28:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (796, 218, 23, 63, 'C', 1, 5, 0, '2026-01-13 13:28:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (797, 203, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:28:15', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (798, 203, 23, 56, 'B', 1, 5, 0, '2026-01-13 13:28:15', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (799, 203, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:28:15', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (800, 203, 23, 60, '对', 1, 5, 0, '2026-01-13 13:28:15', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (801, 203, 23, 61, '对', 1, 5, 0, '2026-01-13 13:28:15', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (802, 203, 23, 63, 'C', 1, 5, 0, '2026-01-13 13:28:15', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (803, 219, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:28:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (804, 219, 23, 29, '错', 1, 5, 0, '2026-01-13 13:28:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (805, 219, 23, 57, 'B', 1, 5, 0, '2026-01-13 13:28:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (806, 219, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:28:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (807, 219, 23, 61, '对', 1, 5, 0, '2026-01-13 13:28:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (808, 219, 23, 63, 'C', 1, 5, 0, '2026-01-13 13:28:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (809, 204, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:28:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (810, 204, 23, 56, 'B', 1, 5, 0, '2026-01-13 13:28:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (811, 204, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:28:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (812, 204, 23, 59, '错', 1, 5, 0, '2026-01-13 13:28:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (813, 204, 23, 61, '对', 1, 5, 0, '2026-01-13 13:28:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (814, 204, 23, 63, 'C', 1, 5, 0, '2026-01-13 13:28:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (815, 191, 23, 29, '错', 1, 5, 0, '2026-01-13 13:28:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (816, 191, 23, 56, 'B', 1, 5, 0, '2026-01-13 13:28:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (817, 191, 23, 57, 'C', 0, 0, 0, '2026-01-13 13:28:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (818, 191, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:28:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (819, 191, 23, 60, '对', 1, 5, 0, '2026-01-13 13:28:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (820, 191, 23, 63, 'B', 0, 0, 0, '2026-01-13 13:28:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (821, 192, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:28:20', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (822, 192, 23, 56, 'B', 1, 5, 0, '2026-01-13 13:28:20', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (823, 192, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:28:20', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (824, 192, 23, 59, '错', 1, 5, 0, '2026-01-13 13:28:20', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (825, 192, 23, 60, '对', 1, 5, 0, '2026-01-13 13:28:20', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (826, 192, 23, 63, 'B', 0, 0, 0, '2026-01-13 13:28:20', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (827, 201, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:28:24', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (828, 201, 23, 56, 'C', 0, 0, 0, '2026-01-13 13:28:24', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (829, 201, 23, 58, 'C', 1, 5, 0, '2026-01-13 13:28:24', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (830, 201, 23, 60, '对', 1, 5, 0, '2026-01-13 13:28:24', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (831, 201, 23, 61, '对', 1, 5, 0, '2026-01-13 13:28:24', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (832, 201, 23, 63, 'C', 1, 5, 0, '2026-01-13 13:28:24', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (833, 233, 23, 22, 'A', 1, 5, 0, '2026-01-13 13:28:30', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (834, 233, 23, 56, 'B', 1, 5, 0, '2026-01-13 13:28:30', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (835, 233, 23, 57, 'B', 1, 5, 0, '2026-01-13 13:28:30', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (836, 233, 23, 60, '对', 1, 5, 0, '2026-01-13 13:28:30', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (837, 233, 23, 61, '错', 0, 0, 0, '2026-01-13 13:28:30', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (838, 233, 23, 63, 'D', 0, 0, 0, '2026-01-13 13:28:30', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (840, 210, 23, 55, '', 0, 0, 0, '2026-01-13 13:41:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (841, 221, 23, 55, '/profile/upload/2026/01/13/32_20260113134836A002.docx', 0, 24, 0, '2026-01-13 13:48:36', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/32_20260113134836A002.pdf');
INSERT INTO `biz_student_answer` VALUES (842, 194, 23, 55, '/profile/upload/2026/01/13/04_20260113134859A003.docx', 0, 30, 0, '2026-01-13 13:48:59', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/04_20260113134859A003.pdf');
INSERT INTO `biz_student_answer` VALUES (843, 193, 23, 55, '/profile/upload/2026/01/13/03_20260113134905A004.docx', 0, 28, 0, '2026-01-13 13:49:06', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/03_20260113134905A004.pdf');
INSERT INTO `biz_student_answer` VALUES (844, 223, 23, 55, '/profile/upload/2026/01/13/学习单_20260112125728A001 (2)_20260113134907A005.docx', 0, 5, 0, '2026-01-13 13:49:07', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/学习单_20260112125728A001 (2)_20260113134907A005.pdf');
INSERT INTO `biz_student_answer` VALUES (845, 230, 23, 55, '/profile/upload/2026/01/13/41_20260113134948A006.docx', 0, 30, 0, '2026-01-13 13:49:48', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/41_20260113134948A006.pdf');
INSERT INTO `biz_student_answer` VALUES (846, 211, 23, 55, '/profile/upload/2026/01/13/学习单_20260112125728A001 (6)_20260113134959A007.docx', 0, 25, 0, '2026-01-13 13:50:00', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/学习单_20260112125728A001 (6)_20260113134959A007.pdf');
INSERT INTO `biz_student_answer` VALUES (847, 207, 23, 55, '/profile/upload/2026/01/13/17_20260113135005A008.docx', 0, 15, 0, '2026-01-13 13:50:06', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/17_20260113135005A008.pdf');
INSERT INTO `biz_student_answer` VALUES (848, 205, 23, 55, '/profile/upload/2026/01/13/15_20260113135010A009.docx', 0, 30, 0, '2026-01-13 13:50:10', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/15_20260113135010A009.pdf');
INSERT INTO `biz_student_answer` VALUES (849, 200, 23, 55, '/profile/upload/2026/01/13/10_20260113135016A010.docx', 0, 25, 0, '2026-01-13 13:50:16', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/10_20260113135016A010.pdf');
INSERT INTO `biz_student_answer` VALUES (850, 222, 23, 55, '/profile/upload/2026/01/13/33_20260113135017A011.docx', 0, 5, 0, '2026-01-13 13:50:17', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/33_20260113135017A011.pdf');
INSERT INTO `biz_student_answer` VALUES (851, 219, 23, 55, '/profile/upload/2026/01/13/30_20260113135017A012.docx', 0, 29, 0, '2026-01-13 13:50:18', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/30_20260113135017A012.pdf');
INSERT INTO `biz_student_answer` VALUES (852, 220, 23, 55, '/profile/upload/2026/01/13/31_20260113135021A013.docx', 0, 26, 0, '2026-01-13 13:50:22', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/31_20260113135021A013.pdf');
INSERT INTO `biz_student_answer` VALUES (853, 231, 23, 55, '/profile/upload/2026/01/13/21 602 赵轩逸_20260113135025A014.docx', 0, 30, 0, '2026-01-13 13:50:25', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/21 602 赵轩逸_20260113135025A014.pdf');
INSERT INTO `biz_student_answer` VALUES (854, 218, 23, 55, '/profile/upload/2026/01/13/29_20260113135025A015.docx', 0, 30, 0, '2026-01-13 13:50:26', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/29_20260113135025A015.pdf');
INSERT INTO `biz_student_answer` VALUES (855, 233, 23, 55, '/profile/upload/2026/01/13/43_20260113135030A016.docx', 0, 15, 0, '2026-01-13 13:50:31', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/43_20260113135030A016.pdf');
INSERT INTO `biz_student_answer` VALUES (856, 232, 23, 55, '/profile/upload/2026/01/13/学习单_20260112125728A001_20260113135033A017.docx', 0, 30, 0, '2026-01-13 13:50:34', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/学习单_20260112125728A001_20260113135033A017.pdf');
INSERT INTO `biz_student_answer` VALUES (857, 206, 23, 55, '/profile/upload/2026/01/13/16_20260113135035A018.docx', 0, 10, 0, '2026-01-13 13:50:36', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/16_20260113135035A018.pdf');
INSERT INTO `biz_student_answer` VALUES (858, 224, 23, 55, '/profile/upload/2026/01/13/35_20260113135036A019.docx', 0, 25, 0, '2026-01-13 13:50:37', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/35_20260113135036A019.pdf');
INSERT INTO `biz_student_answer` VALUES (859, 229, 23, 55, '/profile/upload/2026/01/13/40_20260113135037A020.docx', 0, 28, 0, '2026-01-13 13:50:37', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/40_20260113135037A020.pdf');
INSERT INTO `biz_student_answer` VALUES (860, 202, 23, 55, '/profile/upload/2026/01/13/12_20260113135038A021.docx', 0, 30, 0, '2026-01-13 13:50:38', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/12_20260113135038A021.pdf');
INSERT INTO `biz_student_answer` VALUES (861, 217, 23, 55, '/profile/upload/2026/01/13/28_20260113135039A022.docx', 0, 30, 0, '2026-01-13 13:50:39', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/28_20260113135039A022.pdf');
INSERT INTO `biz_student_answer` VALUES (862, 203, 23, 55, '/profile/upload/2026/01/13/13_20260113135040A023.docx', 0, 30, 0, '2026-01-13 13:50:40', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/13_20260113135040A023.pdf');
INSERT INTO `biz_student_answer` VALUES (863, 197, 23, 55, '/profile/upload/2026/01/13/07_20260113135043A024.docx', 0, 25, 0, '2026-01-13 13:50:44', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/07_20260113135043A024.pdf');
INSERT INTO `biz_student_answer` VALUES (864, 227, 23, 55, '/profile/upload/2026/01/13/38_20260113135045A025.docx', 0, 15, 0, '2026-01-13 13:50:45', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/38_20260113135045A025.pdf');
INSERT INTO `biz_student_answer` VALUES (865, 214, 23, 55, '/profile/upload/2026/01/13/25_20260113135046A026.docx', 0, 25, 0, '2026-01-13 13:50:46', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/25_20260113135046A026.pdf');
INSERT INTO `biz_student_answer` VALUES (866, 209, 23, 55, '/profile/upload/2026/01/13/19_20260113135047A027.docx', 0, 30, 0, '2026-01-13 13:50:48', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/19_20260113135047A027.pdf');
INSERT INTO `biz_student_answer` VALUES (867, 198, 23, 55, '/profile/upload/2026/01/13/08_20260113135049A028.docx', 0, 25, 0, '2026-01-13 13:50:50', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/08_20260113135049A028.pdf');
INSERT INTO `biz_student_answer` VALUES (868, 199, 23, 55, '/profile/upload/2026/01/13/09_20260113135051A029.docx', 0, 22, 0, '2026-01-13 13:50:52', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/09_20260113135051A029.pdf');
INSERT INTO `biz_student_answer` VALUES (869, 226, 23, 55, '/profile/upload/2026/01/13/37_20260113135052A030.docx', 0, 20, 0, '2026-01-13 13:50:52', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/37_20260113135052A030.pdf');
INSERT INTO `biz_student_answer` VALUES (870, 196, 23, 55, '/profile/upload/2026/01/13/06_20260113135055A031.docx', 0, 20, 0, '2026-01-13 13:50:56', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/06_20260113135055A031.pdf');
INSERT INTO `biz_student_answer` VALUES (871, 201, 23, 55, '/profile/upload/2026/01/13/11_20260113135057A032.docx', 0, 30, 0, '2026-01-13 13:50:57', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/11_20260113135057A032.pdf');
INSERT INTO `biz_student_answer` VALUES (872, 204, 23, 55, '/profile/upload/2026/01/13/14_20260113135719A033.docx', 0, 0, 0, '2026-01-13 13:57:20', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/14_20260113135719A033.pdf');
INSERT INTO `biz_student_answer` VALUES (873, 240, 23, 54, '第1', 0, 0, 11, '2026-01-13 14:21:29', 11, 100.00, 1.50, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (875, 240, 23, 55, '', 0, 0, 0, '2026-01-13 14:26:09', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (881, 260, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。', 1, 40, 260, '2026-01-13 14:31:22', 30, 100.00, 100.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (882, 261, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。', 1, 40, 287, '2026-01-13 14:31:48', 27, 100.00, 100.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (883, 242, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现', 1, 40, 300, '2026-01-13 14:31:50', 25, 100.00, 94.70, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (884, 259, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过', 1, 25, 291, '2026-01-13 14:31:52', 19, 100.00, 68.70, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (885, 254, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动  等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取  信息。计算机在其中起关键作用：通过', 0, 22, 300, '2026-01-13 14:31:54', 17, 95.60, 65.60, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (886, 273, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取', 0, 16, 295, '2026-01-13 14:31:55', 14, 100.00, 54.20, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (887, 266, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常', 0, 13, 300, '2026-01-13 14:31:55', 13, 100.00, 48.90, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (888, 236, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机', 1, 40, 300, '2026-01-13 14:32:01', 24, 100.00, 92.40, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (889, 251, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组', 0, 11, 300, '2026-01-13 14:32:01', 12, 100.00, 45.80, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (890, 253, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的', 1, 40, 300, '2026-01-13 14:32:01', 26, 100.00, 97.70, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (891, 272, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常 控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其', 0, 19, 300, '2026-01-13 14:32:02', 16, 98.80, 61.10, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (892, 257, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈', 0, 16, 300, '2026-01-13 14:32:02', 15, 100.00, 55.70, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (893, 247, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与', 1, 40, 300, '2026-01-13 14:32:03', 23, 100.00, 87.80, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (894, 235, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：', 1, 24, 300, '2026-01-13 14:32:03', 18, 100.00, 67.20, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (895, 274, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调，自动', 0, 4, 300, '2026-01-13 14:32:04', 7, 97.40, 28.20, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (896, 271, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调‘自动灌溉扥。系统通常由', 0, 6, 297, '2026-01-13 14:32:05', 9, 95.70, 34.40, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (897, 248, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和', 0, 9, 300, '2026-01-13 14:32:05', 11, 100.00, 42.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (898, 246, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常有控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制', 1, 40, 300, '2026-01-13 14:32:06', 26, 99.20, 98.50, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (899, 264, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调 自动灌溉等。系统通常有控制器 执行器和被控对象组成，', 0, 10, 300, '2026-01-13 14:32:06', 12, 95.20, 45.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (900, 234, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。', 0, 18, 300, '2026-01-13 14:32:07', 15, 100.00, 58.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (901, 252, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。', 0, 18, 300, '2026-01-13 14:32:07', 15, 100.00, 58.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (902, 263, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的', 1, 40, 300, '2026-01-13 14:32:08', 26, 100.00, 97.70, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (903, 275, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助', 0, 13, 300, '2026-01-13 14:32:08', 13, 100.00, 50.40, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (904, 270, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息', 0, 17, 300, '2026-01-13 14:32:08', 15, 100.00, 57.30, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (905, 262, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与小鹿。计算机能实现更', 1, 40, 300, '2026-01-13 14:32:09', 25, 98.40, 93.90, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (906, 260, 23, 22, 'A', 1, 5, 0, '2026-01-13 14:32:10', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (907, 260, 23, 29, '对', 0, 0, 0, '2026-01-13 14:32:10', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (908, 260, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:32:10', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (909, 260, 23, 58, 'C', 1, 5, 0, '2026-01-13 14:32:10', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (910, 260, 23, 59, '错', 1, 5, 0, '2026-01-13 14:32:10', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (911, 260, 23, 63, 'B', 0, 0, 0, '2026-01-13 14:32:10', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (912, 238, 23, 54, '第1课自动  系统介绍了自动  系统能在无人  下自动实现目标，如重调', 0, 2, 300, '2026-01-13 14:32:11', 6, 80.00, 21.40, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (913, 244, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动限时目标，如空调、自动  等。系统通常由控制器、执行器和被控对象组成，并', 0, 10, 300, '2026-01-13 14:32:12', 12, 93.70, 45.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (914, 256, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活', 1, 39, 300, '2026-01-13 14:32:12', 23, 100.00, 86.30, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (915, 241, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象', 0, 11, 300, '2026-01-13 14:32:13', 12, 100.00, 45.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (916, 267, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由', 0, 7, 300, '2026-01-13 14:32:13', 9, 100.00, 35.90, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (917, 268, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键', 0, 22, 300, '2026-01-13 14:32:14', 17, 100.00, 64.90, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (918, 249, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传', 0, 14, 300, '2026-01-13 14:32:15', 13, 100.00, 51.10, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (919, 255, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中', 0, 21, 300, '2026-01-13 14:32:17', 16, 100.00, 62.60, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (920, 261, 23, 22, 'A', 1, 5, 0, '2026-01-13 14:32:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (921, 261, 23, 29, '错', 1, 5, 0, '2026-01-13 14:32:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (922, 261, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:32:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (923, 261, 23, 58, 'C', 1, 5, 0, '2026-01-13 14:32:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (924, 261, 23, 59, '对', 0, 0, 0, '2026-01-13 14:32:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (925, 261, 23, 63, 'C', 1, 5, 0, '2026-01-13 14:32:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (926, 237, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取', 0, 15, 300, '2026-01-13 14:32:19', 14, 100.00, 54.20, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (927, 250, 23, 54, '第一课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传', 0, 13, 300, '2026-01-13 14:32:30', 13, 98.50, 50.40, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (928, 259, 23, 22, 'A', 1, 5, 0, '2026-01-13 14:32:36', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (929, 259, 23, 29, '错', 1, 5, 0, '2026-01-13 14:32:36', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (930, 259, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:32:36', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (931, 259, 23, 58, 'C', 1, 5, 0, '2026-01-13 14:32:36', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (932, 259, 23, 59, '错', 1, 5, 0, '2026-01-13 14:32:36', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (933, 259, 23, 63, 'B', 0, 0, 0, '2026-01-13 14:32:36', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (934, 254, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:32:37', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (935, 254, 23, 57, 'B', 1, 5, 0, '2026-01-13 14:32:37', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (936, 254, 23, 58, 'B', 0, 0, 0, '2026-01-13 14:32:37', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (937, 254, 23, 59, '错', 1, 5, 0, '2026-01-13 14:32:37', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (938, 254, 23, 60, '对', 1, 5, 0, '2026-01-13 14:32:37', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (939, 254, 23, 63, 'B', 0, 0, 0, '2026-01-13 14:32:37', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (940, 242, 23, 22, 'A', 1, 5, 0, '2026-01-13 14:32:38', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (941, 242, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:32:38', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (942, 242, 23, 57, 'B', 1, 5, 0, '2026-01-13 14:32:38', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (943, 242, 23, 60, '对', 1, 5, 0, '2026-01-13 14:32:38', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (944, 242, 23, 61, '对', 1, 5, 0, '2026-01-13 14:32:38', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (945, 242, 23, 63, 'C', 1, 5, 0, '2026-01-13 14:32:38', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (946, 246, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:32:47', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (947, 246, 23, 57, 'B', 1, 5, 0, '2026-01-13 14:32:47', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (948, 246, 23, 58, 'C', 1, 5, 0, '2026-01-13 14:32:47', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (949, 246, 23, 59, '错', 1, 5, 0, '2026-01-13 14:32:47', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (950, 246, 23, 60, '对', 1, 5, 0, '2026-01-13 14:32:47', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (951, 246, 23, 63, 'B', 0, 0, 0, '2026-01-13 14:32:47', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (952, 268, 23, 22, 'A', 1, 5, 0, '2026-01-13 14:32:48', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (953, 268, 23, 29, '错', 1, 5, 0, '2026-01-13 14:32:48', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (954, 268, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:32:48', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (955, 268, 23, 58, 'D', 0, 0, 0, '2026-01-13 14:32:48', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (956, 268, 23, 59, '错', 1, 5, 0, '2026-01-13 14:32:48', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (957, 268, 23, 63, 'C', 1, 5, 0, '2026-01-13 14:32:48', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (958, 266, 23, 22, 'A', 1, 5, 0, '2026-01-13 14:32:49', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (959, 266, 23, 29, '错', 1, 5, 0, '2026-01-13 14:32:49', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (960, 266, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:32:49', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (961, 266, 23, 58, 'C', 1, 5, 0, '2026-01-13 14:32:49', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (962, 266, 23, 60, '对', 1, 5, 0, '2026-01-13 14:32:49', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (963, 266, 23, 63, 'B', 0, 0, 0, '2026-01-13 14:32:49', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (964, 272, 23, 22, 'A', 1, 5, 0, '2026-01-13 14:32:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (965, 272, 23, 29, '错', 1, 5, 0, '2026-01-13 14:32:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (966, 272, 23, 56, 'D', 0, 0, 0, '2026-01-13 14:32:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (967, 272, 23, 57, 'B', 1, 5, 0, '2026-01-13 14:32:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (968, 272, 23, 58, 'C', 1, 5, 0, '2026-01-13 14:32:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (969, 272, 23, 59, '错', 1, 5, 0, '2026-01-13 14:32:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (970, 245, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调 自动灌溉等。系统通常由控制器 执行器和被控对象组成，并', 0, 11, 300, '2026-01-13 14:32:55', 12, 96.80, 46.60, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (971, 273, 23, 22, 'A', 1, 5, 0, '2026-01-13 14:32:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (972, 273, 23, 29, '错', 1, 5, 0, '2026-01-13 14:32:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (973, 273, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:32:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (974, 273, 23, 57, 'B', 1, 5, 0, '2026-01-13 14:32:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (975, 273, 23, 58, 'C', 1, 5, 0, '2026-01-13 14:32:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (976, 273, 23, 61, '错', 0, 0, 0, '2026-01-13 14:32:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (977, 256, 23, 22, 'A', 1, 5, 0, '2026-01-13 14:32:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (978, 256, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:32:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (979, 256, 23, 58, 'C', 1, 5, 0, '2026-01-13 14:32:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (980, 256, 23, 59, '错', 1, 5, 0, '2026-01-13 14:32:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (981, 256, 23, 60, '对', 1, 5, 0, '2026-01-13 14:32:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (982, 256, 23, 63, 'C', 1, 5, 0, '2026-01-13 14:32:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (983, 247, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:33:06', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (984, 247, 23, 57, 'B', 1, 5, 0, '2026-01-13 14:33:06', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (985, 247, 23, 58, 'C', 1, 5, 0, '2026-01-13 14:33:06', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (986, 247, 23, 59, '错', 1, 5, 0, '2026-01-13 14:33:06', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (987, 247, 23, 60, '对', 1, 5, 0, '2026-01-13 14:33:06', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (988, 247, 23, 63, 'B', 0, 0, 0, '2026-01-13 14:33:06', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (989, 267, 23, 22, 'A', 1, 5, 0, '2026-01-13 14:33:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (990, 267, 23, 29, '错', 1, 5, 0, '2026-01-13 14:33:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (991, 267, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:33:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (992, 267, 23, 58, 'C', 1, 5, 0, '2026-01-13 14:33:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (993, 267, 23, 59, '错', 1, 5, 0, '2026-01-13 14:33:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (994, 267, 23, 63, 'C', 1, 5, 0, '2026-01-13 14:33:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (995, 275, 23, 22, 'A', 1, 5, 0, '2026-01-13 14:33:09', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (996, 275, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:33:09', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (997, 275, 23, 57, 'B', 1, 5, 0, '2026-01-13 14:33:09', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (998, 275, 23, 58, 'C', 1, 5, 0, '2026-01-13 14:33:09', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (999, 275, 23, 59, '对', 0, 0, 0, '2026-01-13 14:33:09', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1000, 275, 23, 61, '对', 1, 5, 0, '2026-01-13 14:33:09', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1001, 255, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:33:15', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1002, 255, 23, 57, 'B', 1, 5, 0, '2026-01-13 14:33:15', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1003, 255, 23, 58, 'C', 1, 5, 0, '2026-01-13 14:33:15', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1004, 255, 23, 59, '对', 0, 0, 0, '2026-01-13 14:33:15', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1005, 255, 23, 60, '对', 1, 5, 0, '2026-01-13 14:33:15', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1006, 255, 23, 63, 'C', 1, 5, 0, '2026-01-13 14:33:15', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1007, 262, 23, 22, 'A', 1, 5, 0, '2026-01-13 14:33:24', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1008, 262, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:33:24', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1009, 262, 23, 58, 'D', 0, 0, 0, '2026-01-13 14:33:24', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1010, 262, 23, 59, '错', 1, 5, 0, '2026-01-13 14:33:24', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1011, 262, 23, 60, '对', 1, 5, 0, '2026-01-13 14:33:24', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1012, 262, 23, 63, 'B', 0, 0, 0, '2026-01-13 14:33:24', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1013, 257, 23, 22, 'A', 1, 5, 0, '2026-01-13 14:33:24', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1014, 257, 23, 29, '错', 1, 5, 0, '2026-01-13 14:33:24', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1015, 257, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:33:24', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1016, 257, 23, 58, 'C', 1, 5, 0, '2026-01-13 14:33:24', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1017, 257, 23, 63, 'C', 1, 5, 0, '2026-01-13 14:33:24', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1018, 238, 23, 22, 'A', 1, 5, 0, '2026-01-13 14:33:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1019, 238, 23, 29, '错', 1, 5, 0, '2026-01-13 14:33:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1020, 238, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:33:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1021, 238, 23, 57, 'B', 1, 5, 0, '2026-01-13 14:33:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1022, 238, 23, 60, '对', 1, 5, 0, '2026-01-13 14:33:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1023, 238, 23, 63, 'B', 0, 0, 0, '2026-01-13 14:33:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1024, 245, 23, 56, 'C', 0, 0, 0, '2026-01-13 14:33:38', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1025, 245, 23, 57, 'B', 1, 5, 0, '2026-01-13 14:33:38', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1026, 245, 23, 58, 'C', 1, 5, 0, '2026-01-13 14:33:38', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1027, 245, 23, 59, '错', 1, 5, 0, '2026-01-13 14:33:38', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1028, 245, 23, 61, '对', 1, 5, 0, '2026-01-13 14:33:38', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1029, 245, 23, 63, 'D', 0, 0, 0, '2026-01-13 14:33:38', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1030, 263, 23, 22, 'A', 1, 5, 0, '2026-01-13 14:33:41', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1031, 263, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:33:41', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1032, 263, 23, 58, 'C', 1, 5, 0, '2026-01-13 14:33:41', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1033, 263, 23, 59, '错', 1, 5, 0, '2026-01-13 14:33:41', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1034, 263, 23, 60, '对', 1, 5, 0, '2026-01-13 14:33:41', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1035, 263, 23, 63, 'B', 0, 0, 0, '2026-01-13 14:33:41', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1036, 271, 23, 22, 'A', 1, 5, 0, '2026-01-13 14:33:44', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1037, 271, 23, 29, '错', 1, 5, 0, '2026-01-13 14:33:44', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1038, 271, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:33:44', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1039, 271, 23, 57, 'B', 1, 5, 0, '2026-01-13 14:33:44', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1040, 271, 23, 58, 'C', 1, 5, 0, '2026-01-13 14:33:44', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1041, 271, 23, 59, '错', 1, 5, 0, '2026-01-13 14:33:44', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1042, 270, 23, 22, 'A', 1, 5, 0, '2026-01-13 14:33:52', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1043, 270, 23, 29, '错', 1, 5, 0, '2026-01-13 14:33:52', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1044, 270, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:33:52', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1045, 270, 23, 57, 'B', 1, 5, 0, '2026-01-13 14:33:52', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1046, 270, 23, 58, 'C', 1, 5, 0, '2026-01-13 14:33:52', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1047, 270, 23, 59, '错', 1, 5, 0, '2026-01-13 14:33:52', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1048, 258, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性', 1, 40, 300, '2026-01-13 14:33:52', 23, 100.00, 87.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1049, 252, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:34:02', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1050, 252, 23, 57, 'B', 1, 5, 0, '2026-01-13 14:34:02', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1051, 252, 23, 58, 'B', 0, 0, 0, '2026-01-13 14:34:02', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1052, 252, 23, 59, '错', 1, 5, 0, '2026-01-13 14:34:02', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1053, 252, 23, 61, '错', 0, 0, 0, '2026-01-13 14:34:02', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1054, 252, 23, 63, 'B', 0, 0, 0, '2026-01-13 14:34:02', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1055, 248, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:34:10', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1056, 248, 23, 57, 'B', 1, 5, 0, '2026-01-13 14:34:10', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1057, 248, 23, 58, 'B', 0, 0, 0, '2026-01-13 14:34:10', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1058, 248, 23, 59, '错', 1, 5, 0, '2026-01-13 14:34:10', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1059, 248, 23, 60, '对', 1, 5, 0, '2026-01-13 14:34:10', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1060, 248, 23, 63, 'B', 0, 0, 0, '2026-01-13 14:34:10', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1061, 249, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:34:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1062, 249, 23, 57, 'B', 1, 5, 0, '2026-01-13 14:34:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1063, 249, 23, 58, 'A', 0, 0, 0, '2026-01-13 14:34:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1064, 249, 23, 60, '对', 1, 5, 0, '2026-01-13 14:34:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1065, 249, 23, 61, '错', 0, 0, 0, '2026-01-13 14:34:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1066, 249, 23, 63, 'B', 0, 0, 0, '2026-01-13 14:34:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1067, 234, 23, 22, 'A', 1, 5, 0, '2026-01-13 14:34:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1068, 234, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:34:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1069, 234, 23, 57, 'B', 1, 5, 0, '2026-01-13 14:34:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1070, 234, 23, 60, '对', 1, 5, 0, '2026-01-13 14:34:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1071, 234, 23, 61, '对', 1, 5, 0, '2026-01-13 14:34:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1072, 234, 23, 63, 'B', 0, 0, 0, '2026-01-13 14:34:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1073, 244, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:34:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1074, 244, 23, 57, 'B', 1, 5, 0, '2026-01-13 14:34:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1075, 244, 23, 58, 'B', 0, 0, 0, '2026-01-13 14:34:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1076, 244, 23, 59, '错', 1, 5, 0, '2026-01-13 14:34:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1077, 244, 23, 61, '对', 1, 5, 0, '2026-01-13 14:34:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1078, 244, 23, 63, 'B', 0, 0, 0, '2026-01-13 14:34:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1079, 235, 23, 22, 'A', 1, 5, 0, '2026-01-13 14:34:20', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1080, 235, 23, 56, 'A', 0, 0, 0, '2026-01-13 14:34:20', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1081, 235, 23, 57, 'B', 1, 5, 0, '2026-01-13 14:34:20', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1082, 235, 23, 60, '对', 1, 5, 0, '2026-01-13 14:34:20', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1083, 235, 23, 61, '错', 0, 0, 0, '2026-01-13 14:34:20', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1084, 235, 23, 63, 'B', 0, 0, 0, '2026-01-13 14:34:20', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1085, 243, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制箱、执行性和被控对象组成，并常接祖充值卡', 0, 11, 300, '2026-01-13 14:34:30', 12, 89.90, 47.30, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1086, 250, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:34:34', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1087, 250, 23, 57, 'B', 1, 5, 0, '2026-01-13 14:34:34', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1088, 250, 23, 58, 'B', 0, 0, 0, '2026-01-13 14:34:34', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1089, 250, 23, 60, '对', 1, 5, 0, '2026-01-13 14:34:34', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1090, 250, 23, 61, '对', 1, 5, 0, '2026-01-13 14:34:34', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1091, 250, 23, 63, 'C', 1, 5, 0, '2026-01-13 14:34:34', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1092, 258, 23, 22, 'A', 1, 5, 0, '2026-01-13 14:34:49', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1093, 258, 23, 29, '错', 1, 5, 0, '2026-01-13 14:34:49', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1094, 258, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:34:49', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1095, 258, 23, 58, 'C', 1, 5, 0, '2026-01-13 14:34:49', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1096, 258, 23, 60, '对', 1, 5, 0, '2026-01-13 14:34:49', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1097, 258, 23, 63, 'C', 1, 5, 0, '2026-01-13 14:34:49', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1098, 253, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:35:03', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1099, 253, 23, 57, 'B', 1, 5, 0, '2026-01-13 14:35:03', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1100, 253, 23, 58, 'C', 1, 5, 0, '2026-01-13 14:35:03', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1101, 253, 23, 59, '错', 1, 5, 0, '2026-01-13 14:35:03', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1102, 253, 23, 61, '对', 1, 5, 0, '2026-01-13 14:35:03', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1103, 253, 23, 63, 'B', 0, 0, 0, '2026-01-13 14:35:03', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1104, 243, 23, 56, 'D', 0, 0, 0, '2026-01-13 14:35:48', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1105, 243, 23, 57, 'B', 1, 5, 0, '2026-01-13 14:35:48', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1106, 243, 23, 58, 'A', 0, 0, 0, '2026-01-13 14:35:48', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1107, 243, 23, 60, '对', 1, 5, 0, '2026-01-13 14:35:48', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1108, 243, 23, 61, '错', 0, 0, 0, '2026-01-13 14:35:48', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1109, 243, 23, 63, 'B', 0, 0, 0, '2026-01-13 14:35:48', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1110, 236, 23, 55, '/profile/upload/2026/01/13/3_20260113143743A035.docx', 0, 22, 0, '2026-01-13 14:37:44', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/3_20260113143743A035.pdf');
INSERT INTO `biz_student_answer` VALUES (1111, 236, 23, 22, 'A', 1, 5, 0, '2026-01-13 14:37:52', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1112, 236, 23, 29, '错', 1, 5, 0, '2026-01-13 14:37:52', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1113, 236, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:37:52', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1114, 236, 23, 57, 'B', 1, 5, 0, '2026-01-13 14:37:52', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1115, 236, 23, 61, '对', 1, 5, 0, '2026-01-13 14:37:52', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1116, 236, 23, 63, 'C', 1, 5, 0, '2026-01-13 14:37:52', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1117, 268, 23, 55, '/profile/upload/2026/01/13/36_20260113143839A036.docx', 0, 30, 0, '2026-01-13 14:38:39', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/36_20260113143839A036.pdf');
INSERT INTO `biz_student_answer` VALUES (1118, 262, 23, 55, '/profile/upload/2026/01/13/29_20260113143839A037.docx', 0, 30, 0, '2026-01-13 14:38:39', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/29_20260113143839A037.pdf');
INSERT INTO `biz_student_answer` VALUES (1119, 246, 23, 55, '/profile/upload/2026/01/13/13_20260113143852A038.docx', 0, 20, 0, '2026-01-13 14:38:53', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/13_20260113143852A038.pdf');
INSERT INTO `biz_student_answer` VALUES (1120, 242, 23, 55, '/profile/upload/2026/01/13/9_20260113143855A039.docx', 0, 20, 0, '2026-01-13 14:38:55', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/9_20260113143855A039.pdf');
INSERT INTO `biz_student_answer` VALUES (1121, 256, 23, 55, '/profile/upload/2026/01/13/23_20260113143917A040.docx', 0, 30, 0, '2026-01-13 14:39:18', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/23_20260113143917A040.pdf');
INSERT INTO `biz_student_answer` VALUES (1122, 260, 23, 55, '/profile/upload/2026/01/13/27_20260113143926A041.docx', 0, 30, 0, '2026-01-13 14:39:27', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/27_20260113143926A041.pdf');
INSERT INTO `biz_student_answer` VALUES (1123, 261, 23, 55, '/profile/upload/2026/01/13/学习单28号  _20260112125728A001_20260113143926A042.docx', 0, 30, 0, '2026-01-13 14:39:27', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/学习单28号  _20260112125728A001_20260113143926A042.pdf');
INSERT INTO `biz_student_answer` VALUES (1124, 263, 23, 55, '/profile/upload/2026/01/13/30_20260113143927A043.docx', 0, 30, 0, '2026-01-13 14:39:28', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/30_20260113143927A043.pdf');
INSERT INTO `biz_student_answer` VALUES (1125, 245, 23, 55, '/profile/upload/2026/01/13/12_20260113143928A044.docx', 0, 20, 0, '2026-01-13 14:39:28', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/12_20260113143928A044.pdf');
INSERT INTO `biz_student_answer` VALUES (1126, 247, 23, 55, '/profile/upload/2026/01/13/14_20260113143946A045.docx', 0, 30, 0, '2026-01-13 14:39:47', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/14_20260113143946A045.pdf');
INSERT INTO `biz_student_answer` VALUES (1127, 259, 23, 55, '/profile/upload/2026/01/13/26_20260113143955A046.docx', 0, 26, 0, '2026-01-13 14:39:55', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/26_20260113143955A046.pdf');
INSERT INTO `biz_student_answer` VALUES (1128, 254, 23, 55, '/profile/upload/2026/01/13/21_20260113144011A047.docx', 0, 26, 0, '2026-01-13 14:40:11', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/21_20260113144011A047.pdf');
INSERT INTO `biz_student_answer` VALUES (1129, 255, 23, 55, '/profile/upload/2026/01/13/学习单_20260112125728A001_20260113144027A048.docx', 0, 20, 0, '2026-01-13 14:40:27', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/学习单_20260112125728A001_20260113144027A048.pdf');
INSERT INTO `biz_student_answer` VALUES (1130, 244, 23, 55, '/profile/upload/2026/01/13/11_20260113144031A049.docx', 0, 26, 0, '2026-01-13 14:40:32', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/11_20260113144031A049.pdf');
INSERT INTO `biz_student_answer` VALUES (1131, 252, 23, 55, '/profile/upload/2026/01/13/19_20260113144104A050.docx', 0, 25, 0, '2026-01-13 14:41:04', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/19_20260113144104A050.pdf');
INSERT INTO `biz_student_answer` VALUES (1133, 274, 23, 55, '/profile/upload/2026/01/13/2020710143_20260113144126A052.docx', 0, 20, 0, '2026-01-13 14:41:27', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/2020710143_20260113144126A052.pdf');
INSERT INTO `biz_student_answer` VALUES (1134, 257, 23, 55, '/profile/upload/2026/01/13/24_20260113144138A053.docx', 0, 28, 0, '2026-01-13 14:41:39', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/24_20260113144138A053.pdf');
INSERT INTO `biz_student_answer` VALUES (1135, 258, 23, 55, '/profile/upload/2026/01/13/25_20260113144143A054.docx', 0, 29, 0, '2026-01-13 14:41:43', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/25_20260113144143A054.pdf');
INSERT INTO `biz_student_answer` VALUES (1136, 235, 23, 55, '/profile/upload/2026/01/13/2_20260113144211A055.docx', 0, 28, 0, '2026-01-13 14:42:11', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/2_20260113144211A055.pdf');
INSERT INTO `biz_student_answer` VALUES (1137, 266, 23, 55, '/profile/upload/2026/01/13/34_20260113144219A056.docx', 0, 0, 0, '2026-01-13 14:42:19', NULL, NULL, NULL, 'failed', NULL);
INSERT INTO `biz_student_answer` VALUES (1139, 270, 23, 55, '/profile/upload/2026/01/13/39_20260113144223A057.docx', 0, 29, 0, '2026-01-13 14:42:24', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/39_20260113144223A057.pdf');
INSERT INTO `biz_student_answer` VALUES (1140, 273, 23, 55, '/profile/upload/2026/01/13/41_20260113144230A058.docx', 0, 28, 0, '2026-01-13 14:42:30', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/41_20260113144230A058.pdf');
INSERT INTO `biz_student_answer` VALUES (1141, 250, 23, 55, '/profile/upload/2026/01/13/17_20260113144235A059.docx', 0, 24, 0, '2026-01-13 14:42:36', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/17_20260113144235A059.pdf');
INSERT INTO `biz_student_answer` VALUES (1142, 274, 23, 22, 'A', 1, 5, 0, '2026-01-13 14:42:43', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1143, 274, 23, 29, '错', 1, 5, 0, '2026-01-13 14:42:43', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1144, 274, 23, 56, 'A', 0, 0, 0, '2026-01-13 14:42:43', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1145, 274, 23, 57, 'B', 1, 5, 0, '2026-01-13 14:42:43', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1146, 274, 23, 58, 'D', 0, 0, 0, '2026-01-13 14:42:43', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1147, 274, 23, 61, '对', 1, 5, 0, '2026-01-13 14:42:43', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1148, 271, 23, 55, '/profile/upload/2026/01/13/40_20260113144252A060.docx', 0, 10, 0, '2026-01-13 14:42:53', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/40_20260113144252A060.pdf');
INSERT INTO `biz_student_answer` VALUES (1150, 251, 23, 55, '/profile/upload/2026/01/13/18_20260113144258A062.docx', 0, 28, 0, '2026-01-13 14:42:59', NULL, NULL, NULL, 'failed', NULL);
INSERT INTO `biz_student_answer` VALUES (1151, 264, 23, 22, 'A', 1, 5, 0, '2026-01-13 14:43:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1152, 264, 23, 56, 'D', 0, 0, 0, '2026-01-13 14:43:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1153, 264, 23, 58, 'C', 1, 5, 0, '2026-01-13 14:43:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1154, 264, 23, 59, '错', 1, 5, 0, '2026-01-13 14:43:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1155, 264, 23, 60, '对', 1, 5, 0, '2026-01-13 14:43:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1156, 264, 23, 63, 'C', 1, 5, 0, '2026-01-13 14:43:11', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1158, 234, 23, 55, '/profile/upload/2026/01/13/1_20260113144314A063.docx', 0, 30, 0, '2026-01-13 14:43:15', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/1_20260113144314A063.pdf');
INSERT INTO `biz_student_answer` VALUES (1159, 253, 23, 55, '/profile/upload/2026/01/13/20_20260113144316A064.docx', 0, 29, 0, '2026-01-13 14:43:17', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/20_20260113144316A064.pdf');
INSERT INTO `biz_student_answer` VALUES (1160, 267, 23, 55, '/profile/upload/2026/01/13/35_20260113144337A065.docx', 0, 0, 0, '2026-01-13 14:43:38', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/35_20260113144337A065.pdf');
INSERT INTO `biz_student_answer` VALUES (1161, 241, 23, 55, '/profile/upload/2026/01/13/8_20260113144338A066.docx', 0, 29, 0, '2026-01-13 14:43:38', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/8_20260113144338A066.pdf');
INSERT INTO `biz_student_answer` VALUES (1162, 237, 23, 22, 'A', 1, 5, 0, '2026-01-13 14:43:43', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1163, 237, 23, 29, '错', 1, 5, 0, '2026-01-13 14:43:43', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1164, 237, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:43:43', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1165, 237, 23, 57, 'B', 1, 5, 0, '2026-01-13 14:43:43', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1166, 237, 23, 61, '对', 1, 5, 0, '2026-01-13 14:43:43', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1167, 237, 23, 63, 'C', 1, 5, 0, '2026-01-13 14:43:43', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1168, 264, 23, 55, '/profile/upload/2026/01/13/学习单_20260112125728A001_20260113144346A067.docx', 0, 0, 0, '2026-01-13 14:43:47', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/学习单_20260112125728A001_20260113144346A067.pdf');
INSERT INTO `biz_student_answer` VALUES (1169, 251, 23, 56, 'B', 1, 5, 0, '2026-01-13 14:43:47', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1170, 251, 23, 57, 'B', 1, 5, 0, '2026-01-13 14:43:47', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1171, 251, 23, 58, 'C', 1, 5, 0, '2026-01-13 14:43:47', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1172, 251, 23, 59, '错', 1, 5, 0, '2026-01-13 14:43:47', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1173, 251, 23, 61, '错', 0, 0, 0, '2026-01-13 14:43:47', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1174, 251, 23, 63, 'B', 0, 0, 0, '2026-01-13 14:43:47', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1175, 237, 23, 55, '/profile/upload/2026/01/13/4_20260113144352A068.docx', 0, 25, 0, '2026-01-13 14:43:52', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/4_20260113144352A068.pdf');
INSERT INTO `biz_student_answer` VALUES (1176, 275, 23, 55, '/profile/upload/2026/01/13/42_20260113144354A069.docx', 0, 23, 0, '2026-01-13 14:43:55', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/42_20260113144354A069.pdf');
INSERT INTO `biz_student_answer` VALUES (1177, 249, 23, 55, '/profile/upload/2026/01/13/16_20260113144402A070.docx', 0, 30, 0, '2026-01-13 14:44:02', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/16_20260113144402A070.pdf');
INSERT INTO `biz_student_answer` VALUES (1178, 238, 23, 55, '/profile/upload/2026/01/13/05_20260113144406A071.docx', 0, 28, 0, '2026-01-13 14:44:06', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/13/05_20260113144406A071.pdf');
INSERT INTO `biz_student_answer` VALUES (1179, 240, 24, 62, 'A', 1, 50, 0, '2026-01-14 16:21:39', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1180, 240, 24, 63, 'C', 1, 50, 0, '2026-01-14 16:21:39', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1183, 400, 23, 22, 'A', 1, 5, 0, '2026-01-16 12:40:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1184, 400, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:40:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1185, 400, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:40:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1186, 400, 23, 58, 'C', 1, 5, 0, '2026-01-16 12:40:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1187, 400, 23, 60, '错', 0, 0, 0, '2026-01-16 12:40:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1188, 400, 23, 61, '错', 0, 0, 0, '2026-01-16 12:40:25', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1189, 396, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。', 1, 40, 241, '2026-01-16 12:43:35', 33, 100.00, 100.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1190, 381, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，', 0, 4, 250, '2026-01-16 12:44:02', 8, 100.00, 24.40, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1191, 376, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。', 1, 40, 250, '2026-01-16 12:44:15', 31, 100.00, 100.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1192, 396, 23, 22, 'A', 1, 5, 0, '2026-01-16 12:44:28', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1193, 396, 23, 29, '错', 1, 5, 0, '2026-01-16 12:44:28', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1194, 396, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:44:28', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1195, 396, 23, 58, 'C', 1, 5, 0, '2026-01-16 12:44:28', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1196, 396, 23, 61, '对', 1, 5, 0, '2026-01-16 12:44:28', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1197, 396, 23, 63, 'B', 0, 0, 0, '2026-01-16 12:44:28', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1198, 381, 23, 29, '对', 0, 0, 0, '2026-01-16 12:44:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1199, 381, 23, 56, 'C', 0, 0, 0, '2026-01-16 12:44:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1200, 381, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:44:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1201, 381, 23, 58, 'A', 0, 0, 0, '2026-01-16 12:44:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1202, 381, 23, 59, '错', 1, 5, 0, '2026-01-16 12:44:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1203, 381, 23, 63, 'B', 0, 0, 0, '2026-01-16 12:44:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1204, 393, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借', 0, 13, 300, '2026-01-16 12:44:41', 13, 100.00, 49.60, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1205, 362, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性', 1, 40, 300, '2026-01-16 12:44:41', 23, 100.00, 87.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1206, 375, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反观信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。', 1, 40, 277, '2026-01-16 12:44:42', 28, 99.20, 99.20, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1207, 399, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。', 0, 18, 300, '2026-01-16 12:44:43', 15, 100.00, 58.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1208, 388, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过', 1, 25, 300, '2026-01-16 12:44:47', 18, 100.00, 68.70, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1209, 389, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中其关键作用：通过指', 1, 24, 300, '2026-01-16 12:44:47', 18, 98.90, 68.70, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1210, 395, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过制令实现更精准、复杂和', 1, 30, 300, '2026-01-16 12:44:49', 20, 99.00, 76.30, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1211, 366, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起光解作用：通过租赁实现', 1, 24, 300, '2026-01-16 12:44:49', 18, 95.70, 68.70, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1212, 397, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率、计算机', 1, 40, 300, '2026-01-16 12:44:50', 24, 99.20, 91.60, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1213, 382, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调`自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常', 0, 12, 300, '2026-01-16 12:44:50', 13, 98.40, 48.10, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1214, 370, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的', 1, 33, 300, '2026-01-16 12:44:50', 21, 100.00, 79.40, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1215, 373, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，', 1, 35, 300, '2026-01-16 12:44:50', 21, 100.00, 81.70, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1216, 361, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升', 1, 36, 300, '2026-01-16 12:44:52', 22, 100.00, 83.20, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1217, 369, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和', 0, 9, 300, '2026-01-16 12:44:53', 11, 100.00, 42.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1218, 398, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起', 0, 21, 300, '2026-01-16 12:44:54', 17, 100.00, 63.40, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1219, 368, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调 自动灌溉等。系统通常由控制器，执行器黑背', 0, 8, 300, '2026-01-16 12:44:54', 10, 92.90, 39.70, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1220, 377, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控', 0, 10, 300, '2026-01-16 12:44:55', 11, 100.00, 43.50, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1221, 402, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象', 0, 11, 300, '2026-01-16 12:44:55', 12, 100.00, 45.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1222, 385, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调，自动灌溉等。系统通常由控制器，执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其', 0, 19, 300, '2026-01-16 12:44:57', 16, 97.50, 60.30, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1223, 378, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈', 0, 16, 300, '2026-01-16 12:44:58', 15, 100.00, 55.70, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1224, 387, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性', 1, 40, 300, '2026-01-16 12:44:58', 23, 100.00, 87.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1225, 379, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指', 1, 25, 300, '2026-01-16 12:45:00', 18, 100.00, 69.50, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1226, 391, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用', 0, 23, 300, '2026-01-16 12:45:01', 17, 100.00, 66.40, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1227, 390, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常', 0, 13, 300, '2026-01-16 12:45:01', 13, 100.00, 48.90, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1228, 403, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如', 0, 3, 300, '2026-01-16 12:45:02', 7, 100.00, 25.20, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1229, 374, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调 自动灌溉等。系统通常由控制器 执行器和', 0, 8, 300, '2026-01-16 12:45:03', 11, 96.40, 40.50, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1230, 372, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常节奏', 0, 12, 300, '2026-01-16 12:45:04', 13, 97.00, 48.90, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1232, 364, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助', 0, 13, 300, '2026-01-16 12:45:06', 13, 100.00, 50.40, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1233, 380, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器', 0, 15, 300, '2026-01-16 12:45:07', 14, 100.00, 52.70, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1234, 363, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取fan', 0, 15, 300, '2026-01-16 12:45:09', 14, 95.90, 54.20, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1235, 401, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调 自动灌溉等。系统通常有控制器 执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中其关键作用 通过指令实现更精准 复杂和智能的控制，提升系统灵活性与效卢。', 1, 35, 300, '2026-01-16 12:45:13', 22, 94.10, 84.70, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1236, 371, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常有控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中其关键作用：', 0, 22, 300, '2026-01-16 12:45:13', 17, 97.70, 65.60, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1237, 384, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调，自动灌概', 0, 4, 300, '2026-01-16 12:45:15', 8, 95.00, 29.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1238, 394, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控', 0, 7, 295, '2026-01-16 12:45:15', 10, 100.00, 36.60, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1239, 393, 23, 22, 'A', 1, 5, 0, '2026-01-16 12:45:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1240, 393, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:45:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1241, 393, 23, 58, 'B', 0, 0, 0, '2026-01-16 12:45:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1242, 393, 23, 60, '对', 1, 5, 0, '2026-01-16 12:45:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1243, 393, 23, 61, '对', 1, 5, 0, '2026-01-16 12:45:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1244, 393, 23, 63, 'B', 0, 0, 0, '2026-01-16 12:45:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1245, 376, 23, 29, '错', 1, 5, 0, '2026-01-16 12:45:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1246, 376, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:45:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1247, 376, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:45:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1248, 376, 23, 58, 'C', 1, 5, 0, '2026-01-16 12:45:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1249, 376, 23, 61, '错', 0, 0, 0, '2026-01-16 12:45:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1250, 376, 23, 63, 'C', 1, 5, 0, '2026-01-16 12:45:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1251, 370, 23, 22, 'A', 1, 5, 0, '2026-01-16 12:45:24', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1252, 370, 23, 29, '错', 1, 5, 0, '2026-01-16 12:45:24', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1253, 370, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:45:24', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1254, 370, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:45:24', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1255, 370, 23, 60, '对', 1, 5, 0, '2026-01-16 12:45:24', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1256, 370, 23, 63, 'C', 1, 5, 0, '2026-01-16 12:45:24', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1257, 392, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调 自动', 0, 4, 300, '2026-01-16 12:45:26', 7, 97.40, 28.20, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1258, 383, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被', 0, 10, 300, '2026-01-16 12:45:26', 11, 100.00, 42.70, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1259, 361, 23, 22, 'A', 1, 5, 0, '2026-01-16 12:45:31', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1260, 361, 23, 29, '错', 1, 5, 0, '2026-01-16 12:45:31', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1261, 361, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:45:31', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1262, 361, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:45:31', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1263, 361, 23, 60, '对', 1, 5, 0, '2026-01-16 12:45:31', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1264, 361, 23, 63, 'D', 0, 0, 0, '2026-01-16 12:45:31', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1265, 397, 23, 22, 'A', 1, 5, 0, '2026-01-16 12:45:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1266, 397, 23, 56, 'A', 0, 0, 0, '2026-01-16 12:45:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1267, 397, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:45:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1268, 397, 23, 58, 'A', 0, 0, 0, '2026-01-16 12:45:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1269, 397, 23, 59, '错', 1, 5, 0, '2026-01-16 12:45:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1270, 397, 23, 60, '对', 1, 5, 0, '2026-01-16 12:45:32', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1271, 368, 23, 22, 'A', 1, 5, 0, '2026-01-16 12:45:34', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1272, 368, 23, 29, '错', 1, 5, 0, '2026-01-16 12:45:34', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1273, 368, 23, 56, 'A', 0, 0, 0, '2026-01-16 12:45:34', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1274, 368, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:45:34', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1275, 368, 23, 60, '对', 1, 5, 0, '2026-01-16 12:45:34', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1276, 368, 23, 63, 'A', 0, 0, 0, '2026-01-16 12:45:34', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1277, 366, 23, 22, 'A', 1, 5, 0, '2026-01-16 12:45:37', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1278, 366, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:45:37', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1279, 366, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:45:37', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1280, 366, 23, 59, '错', 1, 5, 0, '2026-01-16 12:45:37', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1281, 366, 23, 60, '对', 1, 5, 0, '2026-01-16 12:45:37', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1282, 366, 23, 63, 'C', 1, 5, 0, '2026-01-16 12:45:37', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1283, 392, 23, 22, 'A', 1, 5, 0, '2026-01-16 12:45:39', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1284, 392, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:45:39', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1285, 392, 23, 58, 'A', 0, 0, 0, '2026-01-16 12:45:39', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1286, 392, 23, 60, '对', 1, 5, 0, '2026-01-16 12:45:39', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1287, 392, 23, 61, '错', 0, 0, 0, '2026-01-16 12:45:39', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1288, 392, 23, 63, 'B', 0, 0, 0, '2026-01-16 12:45:39', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1289, 373, 23, 29, '错', 1, 5, 0, '2026-01-16 12:45:42', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1290, 373, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:45:42', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1291, 373, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:45:42', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1292, 373, 23, 58, 'C', 1, 5, 0, '2026-01-16 12:45:42', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1293, 373, 23, 59, '错', 1, 5, 0, '2026-01-16 12:45:42', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1294, 373, 23, 63, 'A', 0, 0, 0, '2026-01-16 12:45:42', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1295, 398, 23, 22, 'A', 1, 5, 0, '2026-01-16 12:45:45', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1296, 398, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:45:45', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1297, 398, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:45:45', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1298, 398, 23, 58, 'A', 0, 0, 0, '2026-01-16 12:45:45', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1299, 398, 23, 59, '错', 1, 5, 0, '2026-01-16 12:45:45', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1300, 398, 23, 60, '对', 1, 5, 0, '2026-01-16 12:45:45', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1301, 400, 23, 54, '第1课自动控制系统简绍了自动控制系统能在无人干预下自动实现目标，如空调', 0, 3, 300, '2026-01-16 12:45:48', 7, 97.10, 26.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1302, 382, 23, 29, '错', 1, 5, 0, '2026-01-16 12:45:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1303, 382, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:45:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1304, 382, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:45:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1305, 382, 23, 58, 'C', 1, 5, 0, '2026-01-16 12:45:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1306, 382, 23, 59, '错', 1, 5, 0, '2026-01-16 12:45:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1307, 382, 23, 63, 'D', 0, 0, 0, '2026-01-16 12:45:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1308, 378, 23, 29, '错', 1, 5, 0, '2026-01-16 12:45:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1309, 378, 23, 56, 'C', 0, 0, 0, '2026-01-16 12:45:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1310, 378, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:45:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1311, 378, 23, 58, 'C', 1, 5, 0, '2026-01-16 12:45:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1312, 378, 23, 61, '对', 1, 5, 0, '2026-01-16 12:45:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1313, 378, 23, 63, 'C', 1, 5, 0, '2026-01-16 12:45:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1314, 395, 23, 22, 'A', 1, 5, 0, '2026-01-16 12:45:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1315, 395, 23, 29, '错', 1, 5, 0, '2026-01-16 12:45:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1316, 395, 23, 56, 'D', 0, 0, 0, '2026-01-16 12:45:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1317, 395, 23, 58, 'C', 1, 5, 0, '2026-01-16 12:45:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1318, 395, 23, 61, '对', 1, 5, 0, '2026-01-16 12:45:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1319, 395, 23, 63, 'B', 0, 0, 0, '2026-01-16 12:45:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1320, 391, 23, 22, 'A', 1, 5, 0, '2026-01-16 12:45:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1321, 391, 23, 29, '错', 1, 5, 0, '2026-01-16 12:45:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1322, 391, 23, 56, 'A', 0, 0, 0, '2026-01-16 12:45:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1323, 391, 23, 58, 'C', 1, 5, 0, '2026-01-16 12:45:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1324, 391, 23, 60, '对', 1, 5, 0, '2026-01-16 12:45:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1325, 391, 23, 63, 'D', 0, 0, 0, '2026-01-16 12:45:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1326, 399, 23, 22, 'A', 1, 5, 0, '2026-01-16 12:45:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1327, 399, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:45:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1328, 399, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:45:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1329, 399, 23, 58, 'C', 1, 5, 0, '2026-01-16 12:45:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1330, 399, 23, 59, '错', 1, 5, 0, '2026-01-16 12:45:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1331, 399, 23, 60, '对', 1, 5, 0, '2026-01-16 12:45:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1332, 388, 23, 22, 'A', 1, 5, 0, '2026-01-16 12:45:58', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1333, 388, 23, 29, '错', 1, 5, 0, '2026-01-16 12:45:58', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1334, 388, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:45:58', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1335, 388, 23, 58, 'C', 1, 5, 0, '2026-01-16 12:45:58', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1336, 388, 23, 61, '对', 1, 5, 0, '2026-01-16 12:45:58', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1337, 388, 23, 63, 'C', 1, 5, 0, '2026-01-16 12:45:58', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1338, 387, 23, 22, 'A', 1, 5, 0, '2026-01-16 12:46:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1339, 387, 23, 29, '错', 1, 5, 0, '2026-01-16 12:46:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1340, 387, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:46:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1341, 387, 23, 58, 'C', 1, 5, 0, '2026-01-16 12:46:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1342, 387, 23, 61, '错', 0, 0, 0, '2026-01-16 12:46:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1343, 387, 23, 63, 'C', 1, 5, 0, '2026-01-16 12:46:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1344, 364, 23, 22, 'A', 1, 5, 0, '2026-01-16 12:46:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1345, 364, 23, 29, '错', 1, 5, 0, '2026-01-16 12:46:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1346, 364, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:46:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1347, 364, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:46:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1348, 364, 23, 59, '对', 0, 0, 0, '2026-01-16 12:46:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1349, 364, 23, 63, 'A', 0, 0, 0, '2026-01-16 12:46:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1350, 363, 23, 22, 'A', 1, 5, 0, '2026-01-16 12:46:03', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1351, 363, 23, 29, '错', 1, 5, 0, '2026-01-16 12:46:03', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1352, 363, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:46:03', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1353, 363, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:46:03', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1354, 363, 23, 59, '错', 1, 5, 0, '2026-01-16 12:46:03', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1355, 363, 23, 63, 'A', 0, 0, 0, '2026-01-16 12:46:03', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1356, 374, 23, 29, '错', 1, 5, 0, '2026-01-16 12:46:04', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1357, 374, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:46:04', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1358, 374, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:46:04', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1359, 374, 23, 58, 'C', 1, 5, 0, '2026-01-16 12:46:04', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1360, 374, 23, 59, '对', 0, 0, 0, '2026-01-16 12:46:04', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1361, 374, 23, 63, 'B', 0, 0, 0, '2026-01-16 12:46:04', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1362, 386, 23, 22, 'A', 1, 5, 0, '2026-01-16 12:46:06', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1363, 386, 23, 29, '错', 1, 5, 0, '2026-01-16 12:46:06', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1364, 386, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:46:06', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1365, 386, 23, 58, 'C', 1, 5, 0, '2026-01-16 12:46:06', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1366, 386, 23, 61, '对', 1, 5, 0, '2026-01-16 12:46:06', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1367, 386, 23, 63, 'C', 1, 5, 0, '2026-01-16 12:46:06', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1368, 375, 23, 29, '错', 1, 5, 0, '2026-01-16 12:46:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1369, 375, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:46:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1370, 375, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:46:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1371, 375, 23, 58, 'C', 1, 5, 0, '2026-01-16 12:46:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1372, 375, 23, 59, '对', 0, 0, 0, '2026-01-16 12:46:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1373, 375, 23, 63, 'C', 1, 5, 0, '2026-01-16 12:46:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1374, 390, 23, 22, 'A', 1, 5, 0, '2026-01-16 12:46:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1375, 390, 23, 29, '错', 1, 5, 0, '2026-01-16 12:46:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1376, 390, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:46:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1377, 390, 23, 58, 'C', 1, 5, 0, '2026-01-16 12:46:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1378, 390, 23, 60, '对', 1, 5, 0, '2026-01-16 12:46:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1379, 390, 23, 63, 'B', 0, 0, 0, '2026-01-16 12:46:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1380, 401, 23, 22, 'B', 0, 0, 0, '2026-01-16 12:46:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1381, 401, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:46:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1382, 401, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:46:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1383, 401, 23, 58, 'C', 1, 5, 0, '2026-01-16 12:46:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1384, 401, 23, 60, '对', 1, 5, 0, '2026-01-16 12:46:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1385, 401, 23, 61, '错', 0, 0, 0, '2026-01-16 12:46:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1386, 403, 23, 22, 'A', 1, 5, 0, '2026-01-16 12:46:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1387, 403, 23, 56, 'C', 0, 0, 0, '2026-01-16 12:46:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1388, 403, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:46:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1389, 403, 23, 58, 'B', 0, 0, 0, '2026-01-16 12:46:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1390, 403, 23, 59, '错', 1, 5, 0, '2026-01-16 12:46:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1391, 403, 23, 61, '错', 0, 0, 0, '2026-01-16 12:46:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1392, 385, 23, 22, 'A', 1, 5, 0, '2026-01-16 12:46:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1393, 385, 23, 56, 'A', 0, 0, 0, '2026-01-16 12:46:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1394, 385, 23, 58, 'B', 0, 0, 0, '2026-01-16 12:46:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1395, 385, 23, 60, '对', 1, 5, 0, '2026-01-16 12:46:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1396, 385, 23, 61, '对', 1, 5, 0, '2026-01-16 12:46:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1397, 385, 23, 63, 'A', 0, 0, 0, '2026-01-16 12:46:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1398, 379, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:46:27', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1399, 379, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:46:27', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1400, 379, 23, 58, 'C', 1, 5, 0, '2026-01-16 12:46:27', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1401, 379, 23, 59, '错', 1, 5, 0, '2026-01-16 12:46:27', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1402, 379, 23, 61, '对', 1, 5, 0, '2026-01-16 12:46:27', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1403, 379, 23, 63, 'C', 1, 5, 0, '2026-01-16 12:46:27', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1404, 383, 23, 29, '错', 1, 5, 0, '2026-01-16 12:46:39', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1405, 383, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:46:39', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1406, 383, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:46:39', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1407, 383, 23, 58, 'C', 1, 5, 0, '2026-01-16 12:46:39', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1408, 383, 23, 59, '错', 1, 5, 0, '2026-01-16 12:46:39', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1409, 383, 23, 63, 'B', 0, 0, 0, '2026-01-16 12:46:39', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1410, 402, 23, 22, 'A', 1, 5, 0, '2026-01-16 12:46:39', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1411, 402, 23, 56, 'C', 0, 0, 0, '2026-01-16 12:46:39', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1412, 402, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:46:39', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1413, 402, 23, 58, 'B', 0, 0, 0, '2026-01-16 12:46:39', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1414, 402, 23, 59, '错', 1, 5, 0, '2026-01-16 12:46:39', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1415, 402, 23, 61, '错', 0, 0, 0, '2026-01-16 12:46:39', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1416, 380, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:46:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1417, 380, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:46:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1418, 380, 23, 58, 'C', 1, 5, 0, '2026-01-16 12:46:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1419, 380, 23, 59, '错', 1, 5, 0, '2026-01-16 12:46:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1420, 380, 23, 61, '对', 1, 5, 0, '2026-01-16 12:46:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1421, 380, 23, 63, 'B', 0, 0, 0, '2026-01-16 12:46:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1422, 384, 23, 22, 'A', 1, 5, 0, '2026-01-16 12:47:04', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1423, 384, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:47:04', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1424, 384, 23, 58, 'C', 1, 5, 0, '2026-01-16 12:47:04', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1425, 384, 23, 60, '对', 1, 5, 0, '2026-01-16 12:47:04', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1426, 384, 23, 61, '对', 1, 5, 0, '2026-01-16 12:47:04', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1427, 384, 23, 63, 'B', 0, 0, 0, '2026-01-16 12:47:04', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1428, 371, 23, 56, 'C', 0, 0, 0, '2026-01-16 12:47:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1429, 371, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:47:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1430, 371, 23, 58, 'C', 1, 5, 0, '2026-01-16 12:47:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1431, 371, 23, 59, '错', 1, 5, 0, '2026-01-16 12:47:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1432, 371, 23, 61, '对', 1, 5, 0, '2026-01-16 12:47:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1433, 371, 23, 63, 'C', 1, 5, 0, '2026-01-16 12:47:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1434, 372, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:47:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1435, 372, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:47:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1436, 372, 23, 58, 'C', 1, 5, 0, '2026-01-16 12:47:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1437, 372, 23, 59, '错', 1, 5, 0, '2026-01-16 12:47:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1438, 372, 23, 61, '对', 1, 5, 0, '2026-01-16 12:47:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1439, 372, 23, 63, 'C', 1, 5, 0, '2026-01-16 12:47:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1440, 367, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更', 1, 40, 300, '2026-01-16 12:48:28', 25, 100.00, 95.40, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1441, 367, 23, 22, 'A', 1, 5, 0, '2026-01-16 12:49:00', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1442, 367, 23, 56, 'A', 0, 0, 0, '2026-01-16 12:49:00', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1443, 367, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:49:00', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1444, 367, 23, 59, '错', 1, 5, 0, '2026-01-16 12:49:00', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1445, 367, 23, 60, '对', 1, 5, 0, '2026-01-16 12:49:00', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1446, 367, 23, 63, 'B', 0, 0, 0, '2026-01-16 12:49:00', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1447, 396, 23, 55, '/profile/upload/2026/01/16/37考试_20260116124938A002.docx', 0, 29, 0, '2026-01-16 12:49:39', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/37考试_20260116124938A002.pdf');
INSERT INTO `biz_student_answer` VALUES (1448, 366, 23, 55, '/profile/upload/2026/01/16/6_20260116125029A003.docx', 0, 28, 0, '2026-01-16 12:50:30', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/6_20260116125029A003.pdf');
INSERT INTO `biz_student_answer` VALUES (1449, 397, 23, 55, '/profile/upload/2026/01/16/38_20260116125039A004.docx', 0, 29, 0, '2026-01-16 12:50:39', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/38_20260116125039A004.pdf');
INSERT INTO `biz_student_answer` VALUES (1450, 375, 23, 55, '/profile/upload/2026/01/16/15_20260116125043A005.docx', 0, 30, 0, '2026-01-16 12:50:44', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/15_20260116125043A005.pdf');
INSERT INTO `biz_student_answer` VALUES (1451, 395, 23, 55, '/profile/upload/2026/01/16/36_20260116125054A006.docx', 0, 28, 0, '2026-01-16 12:50:55', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/36_20260116125054A006.pdf');
INSERT INTO `biz_student_answer` VALUES (1452, 388, 23, 55, '/profile/upload/2026/01/16/28_20260116125101A007.docx', 0, 30, 0, '2026-01-16 12:51:02', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/28_20260116125101A007.pdf');
INSERT INTO `biz_student_answer` VALUES (1453, 362, 23, 55, '/profile/upload/2026/01/16/02_20260116125104A008.docx', 0, 30, 0, '2026-01-16 12:51:05', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/02_20260116125104A008.pdf');
INSERT INTO `biz_student_answer` VALUES (1454, 370, 23, 55, '/profile/upload/2026/01/16/10_20260116125113A009.docx', 0, 30, 0, '2026-01-16 12:51:14', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/10_20260116125113A009.pdf');
INSERT INTO `biz_student_answer` VALUES (1455, 386, 23, 55, '/profile/upload/2026/01/16/26_20260116125121A010.docx', 0, 30, 0, '2026-01-16 12:51:22', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/26_20260116125121A010.pdf');
INSERT INTO `biz_student_answer` VALUES (1456, 391, 23, 55, '/profile/upload/2026/01/16/32_20260116125124A011.docx', 0, 30, 0, '2026-01-16 12:51:24', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/32_20260116125124A011.pdf');
INSERT INTO `biz_student_answer` VALUES (1457, 364, 23, 55, '/profile/upload/2026/01/16/4_20260116125124A012.docx', 0, 2, 0, '2026-01-16 12:51:24', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/4_20260116125124A012.pdf');
INSERT INTO `biz_student_answer` VALUES (1458, 373, 23, 55, '/profile/upload/2026/01/16/13_20260116125126A013.docx', 0, 30, 0, '2026-01-16 12:51:26', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/13_20260116125126A013.pdf');
INSERT INTO `biz_student_answer` VALUES (1459, 362, 23, 22, 'A', 1, 5, 0, '2026-01-16 12:51:28', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1460, 362, 23, 29, '错', 1, 5, 0, '2026-01-16 12:51:28', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1461, 362, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:51:28', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1462, 362, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:51:28', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1463, 362, 23, 60, '对', 1, 5, 0, '2026-01-16 12:51:28', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1464, 362, 23, 63, 'C', 1, 5, 0, '2026-01-16 12:51:28', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1465, 389, 23, 55, '/profile/upload/2026/01/16/29_20260116125141A014.docx', 0, 28, 0, '2026-01-16 12:51:42', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/29_20260116125141A014.pdf');
INSERT INTO `biz_student_answer` VALUES (1466, 392, 23, 55, '/profile/upload/2026/01/16/33_20260116125142A015.docx', 0, 5, 0, '2026-01-16 12:51:42', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/33_20260116125142A015.pdf');
INSERT INTO `biz_student_answer` VALUES (1467, 398, 23, 55, '/profile/upload/2026/01/16/39_20260116125142A016.docx', 0, 30, 0, '2026-01-16 12:51:43', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/39_20260116125142A016.pdf');
INSERT INTO `biz_student_answer` VALUES (1468, 378, 23, 55, '/profile/upload/2026/01/16/18号_20260116125144A017.docx', 0, 30, 0, '2026-01-16 12:51:45', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/18号_20260116125144A017.pdf');
INSERT INTO `biz_student_answer` VALUES (1469, 393, 23, 55, '/profile/upload/2026/01/16/34_20260116125145A018.docx', 0, 25, 0, '2026-01-16 12:51:45', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/34_20260116125145A018.pdf');
INSERT INTO `biz_student_answer` VALUES (1471, 387, 23, 55, '/profile/upload/2026/01/16/学习单_27_20260116125154A020.docx', 0, 25, 0, '2026-01-16 12:51:55', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/学习单_27_20260116125154A020.pdf');
INSERT INTO `biz_student_answer` VALUES (1472, 377, 23, 55, '/profile/upload/2026/01/16/17_20260116125205A021.docx', 0, 29, 0, '2026-01-16 12:52:06', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/17_20260116125205A021.pdf');
INSERT INTO `biz_student_answer` VALUES (1473, 374, 23, 55, '/profile/upload/2026/01/16/14_20260116125206A022.docx', 0, 30, 0, '2026-01-16 12:52:07', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/14_20260116125206A022.pdf');
INSERT INTO `biz_student_answer` VALUES (1474, 363, 23, 55, '/profile/upload/2026/01/16/3_20260116125210A023.docx', 0, 25, 0, '2026-01-16 12:52:10', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/3_20260116125210A023.pdf');
INSERT INTO `biz_student_answer` VALUES (1475, 361, 23, 55, '/profile/upload/2026/01/16/1_20260116125211A024.docx', 0, 30, 0, '2026-01-16 12:52:12', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/1_20260116125211A024.pdf');
INSERT INTO `biz_student_answer` VALUES (1476, 377, 23, 29, '错', 1, 5, 0, '2026-01-16 12:52:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1477, 377, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:52:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1478, 377, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:52:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1479, 377, 23, 58, 'D', 0, 0, 0, '2026-01-16 12:52:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1480, 377, 23, 61, '错', 0, 0, 0, '2026-01-16 12:52:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1481, 377, 23, 63, 'C', 1, 5, 0, '2026-01-16 12:52:12', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1482, 369, 23, 55, '/profile/upload/2026/01/16/9_20260116125215A025.docx', 0, 27, 0, '2026-01-16 12:52:15', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/9_20260116125215A025.pdf');
INSERT INTO `biz_student_answer` VALUES (1483, 369, 23, 22, 'A', 1, 5, 0, '2026-01-16 12:52:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1484, 369, 23, 29, '错', 1, 5, 0, '2026-01-16 12:52:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1485, 369, 23, 56, 'B', 1, 5, 0, '2026-01-16 12:52:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1486, 369, 23, 57, 'B', 1, 5, 0, '2026-01-16 12:52:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1487, 369, 23, 60, '对', 1, 5, 0, '2026-01-16 12:52:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1488, 369, 23, 63, 'B', 0, 0, 0, '2026-01-16 12:52:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1489, 382, 23, 55, '/profile/upload/2026/01/16/22_20260116125231A026.docx', 0, 20, 0, '2026-01-16 12:52:32', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/22_20260116125231A026.pdf');
INSERT INTO `biz_student_answer` VALUES (1490, 371, 23, 55, '/profile/upload/2026/01/16/11_20260116125241A027.docx', 0, 28, 0, '2026-01-16 12:52:42', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/11_20260116125241A027.pdf');
INSERT INTO `biz_student_answer` VALUES (1491, 399, 23, 55, '/profile/upload/2026/01/16/3_20260116125242A028.docx', 0, 29, 0, '2026-01-16 12:52:42', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/3_20260116125242A028.pdf');
INSERT INTO `biz_student_answer` VALUES (1492, 385, 23, 55, '/profile/upload/2026/01/16/25_20260116125251A029.docx', 0, 10, 0, '2026-01-16 12:52:52', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/25_20260116125251A029.pdf');
INSERT INTO `biz_student_answer` VALUES (1493, 368, 23, 55, '/profile/upload/2026/01/16/8_20260116125251A030.docx', 0, 26, 0, '2026-01-16 12:52:52', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/8_20260116125251A030.pdf');
INSERT INTO `biz_student_answer` VALUES (1494, 379, 23, 55, '/profile/upload/2026/01/16/19_20260116125302A031.docx', 0, 30, 0, '2026-01-16 12:53:02', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/19_20260116125302A031.pdf');
INSERT INTO `biz_student_answer` VALUES (1495, 400, 23, 55, '/profile/upload/2026/01/16/41_20260116125312A032.docx', 0, 20, 0, '2026-01-16 12:53:13', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/41_20260116125312A032.pdf');
INSERT INTO `biz_student_answer` VALUES (1496, 402, 23, 55, '/profile/upload/2026/01/16/42_20260116125324A033.docx', 0, 30, 0, '2026-01-16 12:53:24', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/42_20260116125324A033.pdf');
INSERT INTO `biz_student_answer` VALUES (1497, 367, 23, 55, '/profile/upload/2026/01/16/07_20260116125334A034.docx', 0, 0, 0, '2026-01-16 12:53:35', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/07_20260116125334A034.pdf');
INSERT INTO `biz_student_answer` VALUES (1498, 386, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等', 0, 14, 114, '2026-01-16 12:53:46', 22, 100.00, 31.30, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1499, 372, 23, 55, '/profile/upload/2026/01/16/12_20260116125350A035.docx', 0, 25, 0, '2026-01-16 12:53:50', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/12_20260116125350A035.pdf');
INSERT INTO `biz_student_answer` VALUES (1500, 376, 23, 55, '/profile/upload/2026/01/16/16_20260116125351A036.docx', 0, 30, 0, '2026-01-16 12:53:51', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/16_20260116125351A036.pdf');
INSERT INTO `biz_student_answer` VALUES (1501, 383, 23, 55, '/profile/upload/2026/01/16/23_20260116125352A037.docx', 0, 20, 0, '2026-01-16 12:53:52', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/23_20260116125352A037.pdf');
INSERT INTO `biz_student_answer` VALUES (1502, 381, 23, 55, '/profile/upload/2026/01/16/21_20260116125359A038.docx', 0, 15, 0, '2026-01-16 12:54:00', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/21_20260116125359A038.pdf');
INSERT INTO `biz_student_answer` VALUES (1504, 390, 23, 55, '/profile/upload/2026/01/16/30_20260116125408A039.docx', 0, 20, 0, '2026-01-16 12:54:08', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/30_20260116125408A039.pdf');
INSERT INTO `biz_student_answer` VALUES (1505, 401, 23, 55, '/profile/upload/2026/01/16/31_20260116125412A040.docx', 0, 28, 0, '2026-01-16 12:54:13', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/31_20260116125412A040.pdf');
INSERT INTO `biz_student_answer` VALUES (1506, 380, 23, 55, '/profile/upload/2026/01/16/20_20260116125451A041.docx', 0, 20, 0, '2026-01-16 12:54:52', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/20_20260116125451A041.pdf');
INSERT INTO `biz_student_answer` VALUES (1507, 384, 23, 55, '/profile/upload/2026/01/16/24_20260116125527A042.docx', 0, 5, 0, '2026-01-16 12:55:28', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/24_20260116125527A042.pdf');
INSERT INTO `biz_student_answer` VALUES (1508, 403, 23, 55, '/profile/upload/2026/01/16/学习单__20260116125540A043.docx', 0, 5, 0, '2026-01-16 12:55:41', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/学习单__20260116125540A043.pdf');
INSERT INTO `biz_student_answer` VALUES (1512, 426, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。', 1, 40, 221, '2026-01-16 14:26:54', 36, 100.00, 100.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1513, 415, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。', 1, 40, 294, '2026-01-16 14:28:02', 27, 100.00, 100.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1514, 433, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调，自动灌噶等。系统通常有控制器，执行', 0, 7, 275, '2026-01-16 14:28:03', 11, 92.50, 37.40, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1515, 409, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常有控制器、执行器和被控对象组成，并长借助传感器获取反馈信息。', 0, 17, 293, '2026-01-16 14:28:05', 15, 97.40, 56.50, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1516, 412, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用', 0, 23, 300, '2026-01-16 14:28:08', 17, 100.00, 66.40, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1517, 405, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调 自动灌溉等。系统通常由控制器 执行器和被控对象组成，并常借助传感器获取反馈信息。', 0, 16, 300, '2026-01-16 14:28:11', 15, 97.40, 56.50, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1518, 441, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助', 0, 13, 300, '2026-01-16 14:28:11', 13, 100.00, 50.40, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1519, 435, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调 自动灌溉等。系统通常由控制器 执行器和被控对象组成，并常', 0, 11, 300, '2026-01-16 14:28:12', 12, 96.90, 47.30, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1520, 416, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常', 0, 13, 300, '2026-01-16 14:28:13', 13, 100.00, 48.90, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1521, 407, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控制对组成，并常借助传感器获取反馈信息。计算机在', 0, 18, 300, '2026-01-16 14:28:13', 16, 97.50, 59.50, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1522, 419, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动  等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂', 1, 29, 300, '2026-01-16 14:28:15', 20, 98.00, 74.80, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1523, 430, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成、', 0, 11, 300, '2026-01-16 14:28:16', 12, 98.40, 46.60, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1524, 411, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。', 0, 18, 300, '2026-01-16 14:28:16', 15, 100.00, 58.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1525, 429, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起', 0, 21, 300, '2026-01-16 14:28:16', 17, 100.00, 63.40, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1526, 423, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调 自动灌溉等。系统通常由控制器 执行器和被控对象组成，并', 0, 11, 300, '2026-01-16 14:28:17', 12, 96.80, 46.60, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1527, 422, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调 自动灌溉等。系统通常由控制器 执行器和被控对象组成，', 0, 11, 300, '2026-01-16 14:28:17', 12, 96.80, 45.80, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1528, 414, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预是自动十四目标，如空调’自动挂概', 0, 3, 300, '2026-01-16 14:28:17', 7, 85.00, 26.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1529, 439, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌鐦等。系统通常由控制器、执行器和被控对象组成，', 0, 11, 300, '2026-01-16 14:28:18', 12, 98.40, 46.60, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1532, 443, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人课余下自动实现每 ，如空调 自动  等。系统通常', 0, 4, 300, '2026-01-16 14:28:21', 8, 84.80, 29.80, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1533, 410, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调，自动   。系统通常由控制器 执行器和被控对象组成，并常借助', 0, 11, 300, '2026-01-16 14:28:21', 12, 92.40, 46.60, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1534, 426, 23, 22, 'A', 1, 5, 0, '2026-01-16 14:28:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1535, 426, 23, 56, 'B', 1, 5, 0, '2026-01-16 14:28:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1536, 426, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:28:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1537, 426, 23, 59, '错', 1, 5, 0, '2026-01-16 14:28:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1538, 426, 23, 61, '错', 0, 0, 0, '2026-01-16 14:28:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1539, 426, 23, 63, 'D', 0, 0, 0, '2026-01-16 14:28:22', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1540, 408, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的', 1, 33, 300, '2026-01-16 14:28:23', 21, 100.00, 79.40, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1541, 444, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。', 1, 40, 298, '2026-01-16 14:28:23', 26, 100.00, 100.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1542, 420, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制', 1, 34, 300, '2026-01-16 14:28:23', 21, 100.00, 80.90, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1543, 431, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调’自动灌溉等。系统通常有控制器。执行器和被控对象组成，并常借助传感器', 0, 13, 300, '2026-01-16 14:28:24', 13, 95.70, 50.40, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1544, 421, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在', 0, 20, 300, '2026-01-16 14:28:26', 16, 100.00, 61.10, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1545, 424, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调·自动灌溉等。系统通常由控制器·执行器和被控对象组成，', 0, 11, 300, '2026-01-16 14:28:27', 12, 96.80, 45.80, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1546, 427, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与', 1, 40, 300, '2026-01-16 14:28:28', 23, 100.00, 87.80, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1547, 432, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调 自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现', 1, 26, 300, '2026-01-16 14:28:28', 19, 98.90, 71.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1548, 404, 23, 54, '第1课自动控制系统介绍了自动控制系统嫩子无人赶项目僵尸新娘军多多多多多多多多多多多多多多多多多多多多多多多多多多多多多多多多多多多多多无扩', 0, 1, 175, '2026-01-16 14:28:28', 7, 29.00, 15.30, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1549, 417, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调 自动灌溉等。系统通常由控制器，执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用；通过指令实现', 1, 24, 300, '2026-01-16 14:28:28', 18, 96.80, 69.50, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1550, 445, 23, 54, '肚1课自动控制输入这是了自动控制', 0, 0, 300, '2026-01-16 14:28:35', 2, 68.80, 8.40, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1551, 440, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用；通过指令实现', 1, 26, 300, '2026-01-16 14:28:36', 19, 98.90, 71.00, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1552, 428, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被', 0, 10, 300, '2026-01-16 14:28:39', 11, 100.00, 42.70, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1553, 413, 23, 54, '第1课自动控制系统自身了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器', 0, 8, 300, '2026-01-16 14:28:43', 10, 96.30, 39.70, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1554, 415, 23, 22, 'A', 1, 5, 0, '2026-01-16 14:28:48', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1555, 415, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:28:48', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1556, 415, 23, 58, 'C', 1, 5, 0, '2026-01-16 14:28:48', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1557, 415, 23, 59, '错', 1, 5, 0, '2026-01-16 14:28:48', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1558, 415, 23, 60, '对', 1, 5, 0, '2026-01-16 14:28:48', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1559, 415, 23, 63, 'C', 1, 5, 0, '2026-01-16 14:28:48', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1560, 408, 23, 22, 'A', 1, 5, 0, '2026-01-16 14:28:55', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1561, 408, 23, 56, 'B', 1, 5, 0, '2026-01-16 14:28:55', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1562, 408, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:28:55', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1563, 408, 23, 58, 'C', 1, 5, 0, '2026-01-16 14:28:55', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1564, 408, 23, 60, '对', 1, 5, 0, '2026-01-16 14:28:55', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1565, 408, 23, 61, '错', 0, 0, 0, '2026-01-16 14:28:55', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1566, 409, 23, 22, 'A', 1, 5, 0, '2026-01-16 14:28:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1567, 409, 23, 29, '错', 1, 5, 0, '2026-01-16 14:28:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1568, 409, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:28:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1569, 409, 23, 58, 'A', 0, 0, 0, '2026-01-16 14:28:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1570, 409, 23, 60, '对', 1, 5, 0, '2026-01-16 14:28:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1571, 409, 23, 63, 'A', 0, 0, 0, '2026-01-16 14:28:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1572, 411, 23, 22, 'A', 1, 5, 0, '2026-01-16 14:28:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1573, 411, 23, 29, '错', 1, 5, 0, '2026-01-16 14:28:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1574, 411, 23, 57, 'A', 0, 0, 0, '2026-01-16 14:28:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1575, 411, 23, 58, 'C', 1, 5, 0, '2026-01-16 14:28:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1576, 411, 23, 59, '错', 1, 5, 0, '2026-01-16 14:28:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1577, 411, 23, 63, 'B', 0, 0, 0, '2026-01-16 14:28:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1578, 431, 23, 22, 'A', 1, 5, 0, '2026-01-16 14:28:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1579, 431, 23, 29, '错', 1, 5, 0, '2026-01-16 14:28:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1580, 431, 23, 56, 'B', 1, 5, 0, '2026-01-16 14:28:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1581, 431, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:28:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1582, 431, 23, 61, '对', 1, 5, 0, '2026-01-16 14:28:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1583, 431, 23, 63, 'C', 1, 5, 0, '2026-01-16 14:28:56', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1584, 416, 23, 22, 'A', 1, 5, 0, '2026-01-16 14:28:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1585, 416, 23, 29, '错', 1, 5, 0, '2026-01-16 14:28:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1586, 416, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:28:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1587, 416, 23, 58, 'C', 1, 5, 0, '2026-01-16 14:28:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1588, 416, 23, 60, '对', 1, 5, 0, '2026-01-16 14:28:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1589, 416, 23, 63, 'C', 1, 5, 0, '2026-01-16 14:28:57', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1590, 410, 23, 22, 'A', 1, 5, 0, '2026-01-16 14:28:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1591, 410, 23, 29, '错', 1, 5, 0, '2026-01-16 14:28:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1592, 410, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:28:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1593, 410, 23, 58, 'A', 0, 0, 0, '2026-01-16 14:28:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1594, 410, 23, 59, '错', 1, 5, 0, '2026-01-16 14:28:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1595, 410, 23, 63, 'A', 0, 0, 0, '2026-01-16 14:28:59', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1596, 444, 23, 29, '错', 1, 5, 0, '2026-01-16 14:29:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1597, 444, 23, 56, 'B', 1, 5, 0, '2026-01-16 14:29:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1598, 444, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:29:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1599, 444, 23, 58, 'C', 1, 5, 0, '2026-01-16 14:29:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1600, 444, 23, 61, '对', 1, 5, 0, '2026-01-16 14:29:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1601, 444, 23, 63, 'D', 0, 0, 0, '2026-01-16 14:29:01', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1602, 405, 23, 22, 'A', 1, 5, 0, '2026-01-16 14:29:05', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1603, 405, 23, 56, 'B', 1, 5, 0, '2026-01-16 14:29:05', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1604, 405, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:29:05', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1605, 405, 23, 58, 'C', 1, 5, 0, '2026-01-16 14:29:05', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1606, 405, 23, 59, '错', 1, 5, 0, '2026-01-16 14:29:05', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1607, 405, 23, 60, '错', 0, 0, 0, '2026-01-16 14:29:05', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1608, 425, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中', 0, 21, 300, '2026-01-16 14:29:05', 16, 100.00, 62.60, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1609, 404, 23, 22, 'A', 1, 5, 0, '2026-01-16 14:29:07', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1610, 404, 23, 56, 'B', 1, 5, 0, '2026-01-16 14:29:07', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1611, 404, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:29:07', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1612, 404, 23, 58, 'D', 0, 0, 0, '2026-01-16 14:29:07', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1613, 404, 23, 59, '错', 1, 5, 0, '2026-01-16 14:29:07', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1614, 404, 23, 61, '错', 0, 0, 0, '2026-01-16 14:29:07', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1615, 419, 23, 22, 'A', 1, 5, 0, '2026-01-16 14:29:07', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1616, 419, 23, 29, '错', 1, 5, 0, '2026-01-16 14:29:07', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1617, 419, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:29:07', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1618, 419, 23, 58, 'C', 1, 5, 0, '2026-01-16 14:29:07', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1619, 419, 23, 59, '对', 0, 0, 0, '2026-01-16 14:29:07', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1620, 419, 23, 63, 'D', 0, 0, 0, '2026-01-16 14:29:07', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1621, 439, 23, 56, 'B', 1, 5, 0, '2026-01-16 14:29:10', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1622, 439, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:29:10', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1623, 439, 23, 58, 'C', 1, 5, 0, '2026-01-16 14:29:10', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1624, 439, 23, 60, '错', 0, 0, 0, '2026-01-16 14:29:10', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1625, 439, 23, 61, '对', 1, 5, 0, '2026-01-16 14:29:10', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1626, 439, 23, 63, 'C', 1, 5, 0, '2026-01-16 14:29:10', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1627, 429, 23, 22, 'A', 1, 5, 0, '2026-01-16 14:29:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1628, 429, 23, 29, '错', 1, 5, 0, '2026-01-16 14:29:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1629, 429, 23, 56, 'B', 1, 5, 0, '2026-01-16 14:29:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1630, 429, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:29:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1631, 429, 23, 59, '错', 1, 5, 0, '2026-01-16 14:29:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1632, 429, 23, 63, 'A', 0, 0, 0, '2026-01-16 14:29:14', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1633, 407, 23, 22, 'A', 1, 5, 0, '2026-01-16 14:29:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1634, 407, 23, 56, 'B', 1, 5, 0, '2026-01-16 14:29:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1635, 407, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:29:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1636, 407, 23, 58, 'B', 0, 0, 0, '2026-01-16 14:29:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1637, 407, 23, 59, '错', 1, 5, 0, '2026-01-16 14:29:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1638, 407, 23, 60, '对', 1, 5, 0, '2026-01-16 14:29:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1639, 430, 23, 22, 'A', 1, 5, 0, '2026-01-16 14:29:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1640, 430, 23, 29, '错', 1, 5, 0, '2026-01-16 14:29:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1641, 430, 23, 56, 'B', 1, 5, 0, '2026-01-16 14:29:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1642, 430, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:29:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1643, 430, 23, 59, '错', 1, 5, 0, '2026-01-16 14:29:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1644, 430, 23, 63, 'C', 1, 5, 0, '2026-01-16 14:29:18', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1645, 412, 23, 22, 'A', 1, 5, 0, '2026-01-16 14:29:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1646, 412, 23, 29, '错', 1, 5, 0, '2026-01-16 14:29:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1647, 412, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:29:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1648, 412, 23, 58, 'C', 1, 5, 0, '2026-01-16 14:29:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1649, 412, 23, 59, '错', 1, 5, 0, '2026-01-16 14:29:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1650, 412, 23, 63, 'D', 0, 0, 0, '2026-01-16 14:29:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1651, 440, 23, 56, 'B', 1, 5, 0, '2026-01-16 14:29:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1652, 440, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:29:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1653, 440, 23, 58, 'C', 1, 5, 0, '2026-01-16 14:29:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1654, 440, 23, 60, '对', 1, 5, 0, '2026-01-16 14:29:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1655, 440, 23, 61, '对', 1, 5, 0, '2026-01-16 14:29:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1656, 440, 23, 63, 'B', 0, 0, 0, '2026-01-16 14:29:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1657, 435, 23, 29, '错', 1, 5, 0, '2026-01-16 14:29:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1658, 435, 23, 56, 'B', 1, 5, 0, '2026-01-16 14:29:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1659, 435, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:29:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1660, 435, 23, 58, 'C', 1, 5, 0, '2026-01-16 14:29:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1661, 435, 23, 61, '对', 1, 5, 0, '2026-01-16 14:29:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1662, 435, 23, 63, 'B', 0, 0, 0, '2026-01-16 14:29:21', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1663, 417, 23, 22, 'A', 1, 5, 0, '2026-01-16 14:29:23', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1664, 417, 23, 29, '错', 1, 5, 0, '2026-01-16 14:29:23', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1665, 417, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:29:23', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1666, 417, 23, 58, 'A', 0, 0, 0, '2026-01-16 14:29:23', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1667, 417, 23, 60, '对', 1, 5, 0, '2026-01-16 14:29:23', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1668, 417, 23, 63, 'A', 0, 0, 0, '2026-01-16 14:29:23', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1669, 413, 23, 22, 'A', 1, 5, 0, '2026-01-16 14:29:33', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1670, 413, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:29:33', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1671, 413, 23, 58, 'C', 1, 5, 0, '2026-01-16 14:29:33', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1672, 413, 23, 59, '错', 1, 5, 0, '2026-01-16 14:29:33', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1673, 413, 23, 60, '对', 1, 5, 0, '2026-01-16 14:29:33', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1674, 413, 23, 63, 'D', 0, 0, 0, '2026-01-16 14:29:33', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1675, 433, 23, 22, 'A', 1, 5, 0, '2026-01-16 14:29:34', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1676, 433, 23, 29, '错', 1, 5, 0, '2026-01-16 14:29:34', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1677, 433, 23, 56, 'B', 1, 5, 0, '2026-01-16 14:29:34', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1678, 433, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:29:34', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1679, 433, 23, 61, '错', 0, 0, 0, '2026-01-16 14:29:34', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1680, 433, 23, 63, 'B', 0, 0, 0, '2026-01-16 14:29:34', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1681, 422, 23, 22, 'A', 1, 5, 0, '2026-01-16 14:29:37', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1682, 422, 23, 29, '对', 0, 0, 0, '2026-01-16 14:29:37', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1683, 422, 23, 56, 'C', 0, 0, 0, '2026-01-16 14:29:37', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1684, 422, 23, 57, 'A', 0, 0, 0, '2026-01-16 14:29:37', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1685, 422, 23, 59, '错', 1, 5, 0, '2026-01-16 14:29:37', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1686, 422, 23, 63, 'D', 0, 0, 0, '2026-01-16 14:29:37', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1687, 445, 23, 29, '错', 1, 5, 0, '2026-01-16 14:29:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1688, 445, 23, 56, 'C', 0, 0, 0, '2026-01-16 14:29:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1689, 445, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:29:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1690, 445, 23, 58, 'A', 0, 0, 0, '2026-01-16 14:29:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1691, 445, 23, 60, '对', 1, 5, 0, '2026-01-16 14:29:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1692, 445, 23, 63, 'D', 0, 0, 0, '2026-01-16 14:29:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1693, 427, 23, 22, 'A', 1, 5, 0, '2026-01-16 14:29:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1694, 427, 23, 56, 'B', 1, 5, 0, '2026-01-16 14:29:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1695, 427, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:29:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1696, 427, 23, 59, '错', 1, 5, 0, '2026-01-16 14:29:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1697, 427, 23, 61, '对', 1, 5, 0, '2026-01-16 14:29:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1698, 427, 23, 63, 'B', 0, 0, 0, '2026-01-16 14:29:40', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1699, 432, 23, 22, 'A', 1, 5, 0, '2026-01-16 14:29:45', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1700, 432, 23, 29, '错', 1, 5, 0, '2026-01-16 14:29:45', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1701, 432, 23, 56, 'B', 1, 5, 0, '2026-01-16 14:29:45', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1702, 432, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:29:45', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1703, 432, 23, 61, '错', 0, 0, 0, '2026-01-16 14:29:45', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1704, 432, 23, 63, 'C', 1, 5, 0, '2026-01-16 14:29:45', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1705, 414, 23, 22, 'A', 1, 5, 0, '2026-01-16 14:29:51', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1706, 414, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:29:51', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1707, 414, 23, 58, 'C', 1, 5, 0, '2026-01-16 14:29:51', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1708, 414, 23, 59, '错', 1, 5, 0, '2026-01-16 14:29:51', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1709, 414, 23, 60, '对', 1, 5, 0, '2026-01-16 14:29:51', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1710, 414, 23, 63, 'A', 0, 0, 0, '2026-01-16 14:29:51', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1711, 441, 23, 56, 'B', 1, 5, 0, '2026-01-16 14:29:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1712, 441, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:29:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1713, 441, 23, 58, 'C', 1, 5, 0, '2026-01-16 14:29:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1714, 441, 23, 60, '对', 1, 5, 0, '2026-01-16 14:29:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1715, 441, 23, 61, '对', 1, 5, 0, '2026-01-16 14:29:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1716, 441, 23, 63, 'B', 0, 0, 0, '2026-01-16 14:29:53', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1717, 428, 23, 22, 'A', 1, 5, 0, '2026-01-16 14:29:58', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1718, 428, 23, 56, 'C', 0, 0, 0, '2026-01-16 14:29:58', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1719, 428, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:29:58', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1720, 428, 23, 59, '对', 0, 0, 0, '2026-01-16 14:29:58', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1721, 428, 23, 61, '错', 0, 0, 0, '2026-01-16 14:29:58', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1722, 428, 23, 63, 'C', 1, 5, 0, '2026-01-16 14:29:58', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1723, 424, 23, 22, 'A', 1, 5, 0, '2026-01-16 14:29:58', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1724, 424, 23, 29, '错', 1, 5, 0, '2026-01-16 14:29:58', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1725, 424, 23, 56, 'B', 1, 5, 0, '2026-01-16 14:29:58', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1726, 424, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:29:58', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1727, 424, 23, 61, '错', 0, 0, 0, '2026-01-16 14:29:58', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1728, 424, 23, 63, 'B', 0, 0, 0, '2026-01-16 14:29:58', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1729, 436, 23, 54, '第1课自动控制系统家啥了自动控制系统能在无人干预下自动实现木好，', 0, 2, 300, '2026-01-16 14:30:15', 6, 87.50, 21.40, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1730, 425, 23, 22, 'A', 1, 5, 0, '2026-01-16 14:30:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1731, 425, 23, 29, '错', 1, 5, 0, '2026-01-16 14:30:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1732, 425, 23, 56, 'B', 1, 5, 0, '2026-01-16 14:30:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1733, 425, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:30:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1734, 425, 23, 61, '错', 0, 0, 0, '2026-01-16 14:30:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1735, 425, 23, 63, 'B', 0, 0, 0, '2026-01-16 14:30:19', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1736, 443, 23, 29, '错', 1, 5, 0, '2026-01-16 14:30:38', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1737, 443, 23, 56, 'C', 0, 0, 0, '2026-01-16 14:30:38', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1738, 443, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:30:38', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1739, 443, 23, 58, 'A', 0, 0, 0, '2026-01-16 14:30:38', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1740, 443, 23, 61, '对', 1, 5, 0, '2026-01-16 14:30:38', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1741, 443, 23, 63, 'B', 0, 0, 0, '2026-01-16 14:30:38', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1742, 438, 23, 54, '第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助', 0, 23, 174, '2026-01-16 14:31:22', 23, 100.00, 50.40, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1743, 438, 23, 29, '错', 1, 5, 0, '2026-01-16 14:32:15', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1744, 438, 23, 56, 'B', 1, 5, 0, '2026-01-16 14:32:15', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1745, 438, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:32:15', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1746, 438, 23, 58, 'C', 1, 5, 0, '2026-01-16 14:32:15', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1747, 438, 23, 60, '对', 1, 5, 0, '2026-01-16 14:32:15', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1748, 438, 23, 63, 'B', 0, 0, 0, '2026-01-16 14:32:15', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1749, 436, 23, 29, '对', 0, 0, 0, '2026-01-16 14:32:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1750, 436, 23, 56, 'B', 1, 5, 0, '2026-01-16 14:32:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1751, 436, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:32:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1752, 436, 23, 58, 'D', 0, 0, 0, '2026-01-16 14:32:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1753, 436, 23, 61, '错', 0, 0, 0, '2026-01-16 14:32:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1754, 436, 23, 63, 'B', 0, 0, 0, '2026-01-16 14:32:17', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1755, 419, 23, 55, '/profile/upload/2026/01/16/16_20260116143320A045.docx', 0, 20, 0, '2026-01-16 14:33:20', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/16_20260116143320A045.pdf');
INSERT INTO `biz_student_answer` VALUES (1756, 415, 23, 55, '/profile/upload/2026/01/16/12_20260116143322A046.docx', 0, 28, 0, '2026-01-16 14:33:23', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/12_20260116143322A046.pdf');
INSERT INTO `biz_student_answer` VALUES (1757, 431, 23, 55, '/profile/upload/2026/01/16/29_20260116143359A047.docx', 0, 20, 0, '2026-01-16 14:33:59', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/29_20260116143359A047.pdf');
INSERT INTO `biz_student_answer` VALUES (1758, 444, 23, 55, '/profile/upload/2026/01/16/22_20260116143416A048.docx', 0, 20, 0, '2026-01-16 14:34:17', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/22_20260116143416A048.pdf');
INSERT INTO `biz_student_answer` VALUES (1759, 416, 23, 55, '/profile/upload/2026/01/16/13_20260116143458A049.docx', 0, 15, 0, '2026-01-16 14:34:58', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/13_20260116143458A049.pdf');
INSERT INTO `biz_student_answer` VALUES (1760, 440, 23, 55, '/profile/upload/2026/01/16/38_20260116143509A050.docx', 0, 20, 0, '2026-01-16 14:35:10', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/38_20260116143509A050.pdf');
INSERT INTO `biz_student_answer` VALUES (1761, 411, 23, 55, '/profile/upload/2026/01/16/606      8_20260116143526A051.docx', 0, 20, 0, '2026-01-16 14:35:27', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/606      8_20260116143526A051.pdf');
INSERT INTO `biz_student_answer` VALUES (1762, 408, 23, 55, '/profile/upload/2026/01/16/5_20260116143530A052.docx', 0, 25, 0, '2026-01-16 14:35:30', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/5_20260116143530A052.pdf');
INSERT INTO `biz_student_answer` VALUES (1763, 417, 23, 55, '/profile/upload/2026/01/16/14_20260116143534A053.docx', 0, 30, 0, '2026-01-16 14:35:34', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/14_20260116143534A053.pdf');
INSERT INTO `biz_student_answer` VALUES (1764, 432, 23, 55, '/profile/upload/2026/01/16/30_20260116143538A054.docx', 0, 28, 0, '2026-01-16 14:35:38', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/30_20260116143538A054.pdf');
INSERT INTO `biz_student_answer` VALUES (1765, 410, 23, 55, '/profile/upload/2026/01/16/7_20260116143541A055.docx', 0, 5, 0, '2026-01-16 14:35:42', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/7_20260116143541A055.pdf');
INSERT INTO `biz_student_answer` VALUES (1766, 420, 23, 55, '/profile/upload/2026/01/16/学习单017_20260116143546A056.docx', 0, 30, 0, '2026-01-16 14:35:46', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/学习单017_20260116143546A056.pdf');
INSERT INTO `biz_student_answer` VALUES (1767, 405, 23, 55, '/profile/upload/2026/01/16/2_20260116143553A057.docx', 0, 20, 0, '2026-01-16 14:35:54', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/2_20260116143553A057.pdf');
INSERT INTO `biz_student_answer` VALUES (1768, 409, 23, 55, '/profile/upload/2026/01/16/06_20260116143556A058.docx', 0, 15, 0, '2026-01-16 14:35:56', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/06_20260116143556A058.pdf');
INSERT INTO `biz_student_answer` VALUES (1769, 439, 23, 55, '/profile/upload/2026/01/16/37_20260116143559A059.docx', 0, 1, 0, '2026-01-16 14:35:59', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/37_20260116143559A059.pdf');
INSERT INTO `biz_student_answer` VALUES (1770, 420, 23, 22, 'A', 1, 5, 0, '2026-01-16 14:36:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1771, 420, 23, 29, '错', 1, 5, 0, '2026-01-16 14:36:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1772, 420, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:36:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1773, 420, 23, 58, 'C', 1, 5, 0, '2026-01-16 14:36:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1774, 420, 23, 59, '错', 1, 5, 0, '2026-01-16 14:36:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1775, 420, 23, 63, 'D', 0, 0, 0, '2026-01-16 14:36:08', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1776, 421, 23, 55, '/profile/upload/2026/01/16/18_20260116143610A060.docx', 0, 25, 0, '2026-01-16 14:36:10', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/18_20260116143610A060.pdf');
INSERT INTO `biz_student_answer` VALUES (1778, 407, 23, 55, '/profile/upload/2026/01/16/4号_20260116143637A062.docx', 0, 20, 0, '2026-01-16 14:36:37', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/4号_20260116143637A062.pdf');
INSERT INTO `biz_student_answer` VALUES (1779, 427, 23, 55, '/profile/upload/2026/01/16/25_20260116143641A063.docx', 0, 30, 0, '2026-01-16 14:36:41', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/25_20260116143641A063.pdf');
INSERT INTO `biz_student_answer` VALUES (1780, 421, 23, 22, 'A', 1, 5, 0, '2026-01-16 14:36:48', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1781, 421, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:36:48', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1782, 421, 23, 58, 'C', 1, 5, 0, '2026-01-16 14:36:48', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1783, 421, 23, 59, '错', 1, 5, 0, '2026-01-16 14:36:48', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1784, 421, 23, 60, '对', 1, 5, 0, '2026-01-16 14:36:48', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1785, 421, 23, 63, 'B', 0, 0, 0, '2026-01-16 14:36:48', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1786, 437, 23, 54, '第1课自动控制系统介绍了自动控制系统能', 0, 6, 60, '2026-01-16 14:36:49', 19, 100.00, 14.50, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1788, 435, 23, 55, '/profile/upload/2026/01/16/33_20260116143706A064.docx', 0, 23, 0, '2026-01-16 14:37:06', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/33_20260116143706A064.pdf');
INSERT INTO `biz_student_answer` VALUES (1789, 412, 23, 55, '/profile/upload/2026/01/16/9_20260116143706A065.docx', 0, 28, 0, '2026-01-16 14:37:07', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/9_20260116143706A065.pdf');
INSERT INTO `biz_student_answer` VALUES (1790, 425, 23, 55, '/profile/upload/2026/01/16/23_20260116143715A066.docx', 0, 2, 0, '2026-01-16 14:37:15', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/23_20260116143715A066.pdf');
INSERT INTO `biz_student_answer` VALUES (1791, 423, 23, 55, '/profile/upload/2026/01/16/20_20260116143724A067.docx', 0, 6, 0, '2026-01-16 14:37:25', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/20_20260116143724A067.pdf');
INSERT INTO `biz_student_answer` VALUES (1792, 443, 23, 55, '/profile/upload/2026/01/16/41_20260116143726A068.docx', 0, 5, 0, '2026-01-16 14:37:27', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/41_20260116143726A068.pdf');
INSERT INTO `biz_student_answer` VALUES (1793, 441, 23, 55, '/profile/upload/2026/01/16/39_20260116143726A069.docx', 0, 20, 0, '2026-01-16 14:37:27', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/39_20260116143726A069.pdf');
INSERT INTO `biz_student_answer` VALUES (1794, 428, 23, 55, '/profile/upload/2026/01/16/26_20260116143730A070.docx', 0, 20, 0, '2026-01-16 14:37:30', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/26_20260116143730A070.pdf');
INSERT INTO `biz_student_answer` VALUES (1795, 429, 23, 55, '/profile/upload/2026/01/16/27_20260116143746A071.docx', 0, 28, 0, '2026-01-16 14:37:47', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/27_20260116143746A071.pdf');
INSERT INTO `biz_student_answer` VALUES (1796, 430, 23, 55, '/profile/upload/2026/01/16/28_20260116143755A072.docx', 0, 20, 0, '2026-01-16 14:37:55', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/28_20260116143755A072.pdf');
INSERT INTO `biz_student_answer` VALUES (1797, 404, 23, 55, '/profile/upload/2026/01/16/1_20260116143755A073.docx', 0, 10, 0, '2026-01-16 14:37:56', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/1_20260116143755A073.pdf');
INSERT INTO `biz_student_answer` VALUES (1798, 422, 23, 55, '/profile/upload/2026/01/16/19_20260116143801A074.docx', 0, 5, 0, '2026-01-16 14:38:01', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/19_20260116143801A074.pdf');
INSERT INTO `biz_student_answer` VALUES (1799, 413, 23, 55, '/profile/upload/2026/01/16/10_20260116143802A075.docx', 0, 10, 0, '2026-01-16 14:38:02', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/10_20260116143802A075.pdf');
INSERT INTO `biz_student_answer` VALUES (1800, 426, 23, 55, '/profile/upload/2026/01/16/24_20260116143844A076.docx', 0, 25, 0, '2026-01-16 14:38:45', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/24_20260116143844A076.pdf');
INSERT INTO `biz_student_answer` VALUES (1801, 414, 23, 55, '/profile/upload/2026/01/16/11_20260116143852A077.docx', 0, 2, 0, '2026-01-16 14:38:52', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/11_20260116143852A077.pdf');
INSERT INTO `biz_student_answer` VALUES (1802, 424, 23, 55, '/profile/upload/2026/01/16/21_20260116143852A078.docx', 0, 26, 0, '2026-01-16 14:38:52', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/21_20260116143852A078.pdf');
INSERT INTO `biz_student_answer` VALUES (1803, 438, 23, 55, '/profile/upload/2026/01/16/36_20260116143901A079.docx', 0, 25, 0, '2026-01-16 14:39:01', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/36_20260116143901A079.pdf');
INSERT INTO `biz_student_answer` VALUES (1804, 433, 23, 55, '/profile/upload/2026/01/16/31_20260116143935A080.docx', 0, 24, 0, '2026-01-16 14:39:36', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/31_20260116143935A080.pdf');
INSERT INTO `biz_student_answer` VALUES (1805, 437, 23, 29, '错', 1, 5, 0, '2026-01-16 14:39:46', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1806, 437, 23, 56, 'B', 1, 5, 0, '2026-01-16 14:39:46', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1807, 437, 23, 57, 'B', 1, 5, 0, '2026-01-16 14:39:46', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1808, 437, 23, 58, 'C', 1, 5, 0, '2026-01-16 14:39:46', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1809, 437, 23, 60, '对', 1, 5, 0, '2026-01-16 14:39:46', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1810, 437, 23, 63, 'B', 0, 0, 0, '2026-01-16 14:39:46', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `biz_student_answer` VALUES (1811, 437, 23, 55, '/profile/upload/2026/01/16/35_20260116143958A081.docx', 0, 20, 0, '2026-01-16 14:39:59', NULL, NULL, NULL, 'success', '/profile/upload/2026/01/16/35_20260116143958A081.pdf');

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
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '教师-班级管理关联表' ROW_FORMAT = Dynamic;

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
INSERT INTO `biz_teacher_class` VALUES (23, 104, 139, '2020', '4', '2026-01-12 12:51:53');
INSERT INTO `biz_teacher_class` VALUES (24, 104, 139, '2020', '7', '2026-01-12 12:51:53');
INSERT INTO `biz_teacher_class` VALUES (25, 104, 139, '2020', '1', '2026-01-12 14:47:33');
INSERT INTO `biz_teacher_class` VALUES (26, 104, 139, '2020', '2', '2026-01-12 14:47:33');
INSERT INTO `biz_teacher_class` VALUES (27, 104, 139, '2020', '8', '2026-01-12 14:47:33');
INSERT INTO `biz_teacher_class` VALUES (28, 104, 139, '2020', '3', '2026-01-16 11:54:28');
INSERT INTO `biz_teacher_class` VALUES (29, 104, 139, '2020', '5', '2026-01-16 11:54:33');
INSERT INTO `biz_teacher_class` VALUES (30, 104, 139, '2020', '6', '2026-01-16 11:54:37');

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
INSERT INTO `sys_config` VALUES (4, '账号自助-验证码开关', 'sys.account.captchaEnabled', 'false', 'Y', 'admin', '2025-06-12 14:49:21', '', NULL, '是否开启验证码功能（true开启，false关闭）');
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
) ENGINE = InnoDB AUTO_INCREMENT = 759 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统访问记录' ROW_FORMAT = DYNAMIC;

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
INSERT INTO `sys_logininfor` VALUES (305, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-09 08:57:26');
INSERT INTO `sys_logininfor` VALUES (306, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '验证码已失效', '2026-01-09 09:02:38');
INSERT INTO `sys_logininfor` VALUES (307, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-09 09:02:47');
INSERT INTO `sys_logininfor` VALUES (308, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-09 09:08:52');
INSERT INTO `sys_logininfor` VALUES (309, '2025720501', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-09 09:09:20');
INSERT INTO `sys_logininfor` VALUES (310, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '验证码错误', '2026-01-09 09:32:14');
INSERT INTO `sys_logininfor` VALUES (311, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2026-01-09 09:32:54');
INSERT INTO `sys_logininfor` VALUES (312, '2025720501', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-09 09:43:05');
INSERT INTO `sys_logininfor` VALUES (313, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2026-01-09 09:44:34');
INSERT INTO `sys_logininfor` VALUES (314, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-09 09:44:43');
INSERT INTO `sys_logininfor` VALUES (315, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-09 11:16:08');
INSERT INTO `sys_logininfor` VALUES (316, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-09 12:17:38');
INSERT INTO `sys_logininfor` VALUES (317, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-09 13:08:27');
INSERT INTO `sys_logininfor` VALUES (318, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2026-01-09 14:02:56');
INSERT INTO `sys_logininfor` VALUES (319, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-09 14:03:08');
INSERT INTO `sys_logininfor` VALUES (320, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2026-01-09 14:53:04');
INSERT INTO `sys_logininfor` VALUES (321, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-09 14:53:14');
INSERT INTO `sys_logininfor` VALUES (322, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '验证码已失效', '2026-01-12 09:14:05');
INSERT INTO `sys_logininfor` VALUES (323, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-12 09:14:09');
INSERT INTO `sys_logininfor` VALUES (324, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-12 09:31:19');
INSERT INTO `sys_logininfor` VALUES (325, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-12 10:05:34');
INSERT INTO `sys_logininfor` VALUES (326, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-12 10:05:59');
INSERT INTO `sys_logininfor` VALUES (327, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-12 10:06:26');
INSERT INTO `sys_logininfor` VALUES (328, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-12 10:12:48');
INSERT INTO `sys_logininfor` VALUES (329, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-12 10:13:15');
INSERT INTO `sys_logininfor` VALUES (330, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-12 10:14:34');
INSERT INTO `sys_logininfor` VALUES (331, '2025720506', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-12 10:14:45');
INSERT INTO `sys_logininfor` VALUES (332, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-12 12:14:28');
INSERT INTO `sys_logininfor` VALUES (333, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-12 12:14:55');
INSERT INTO `sys_logininfor` VALUES (334, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-12 12:15:10');
INSERT INTO `sys_logininfor` VALUES (335, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '验证码错误', '2026-01-12 12:50:36');
INSERT INTO `sys_logininfor` VALUES (336, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '验证码错误', '2026-01-12 12:50:43');
INSERT INTO `sys_logininfor` VALUES (337, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2026-01-12 12:50:49');
INSERT INTO `sys_logininfor` VALUES (338, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-12 12:51:10');
INSERT INTO `sys_logininfor` VALUES (339, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-12 13:12:20');
INSERT INTO `sys_logininfor` VALUES (340, '2020710433', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:14:20');
INSERT INTO `sys_logininfor` VALUES (341, '2020710430', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:14:23');
INSERT INTO `sys_logininfor` VALUES (342, '2020710411', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '用户不存在/密码错误', '2026-01-12 13:14:24');
INSERT INTO `sys_logininfor` VALUES (343, '2020710431', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:14:24');
INSERT INTO `sys_logininfor` VALUES (344, '2020710432', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:14:25');
INSERT INTO `sys_logininfor` VALUES (345, '2020710428', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:14:26');
INSERT INTO `sys_logininfor` VALUES (346, '2020710402', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:14:27');
INSERT INTO `sys_logininfor` VALUES (347, '2020710442', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:14:27');
INSERT INTO `sys_logininfor` VALUES (348, '2020710414', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:14:31');
INSERT INTO `sys_logininfor` VALUES (349, '2020710411', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:14:31');
INSERT INTO `sys_logininfor` VALUES (350, '2020710441', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:14:31');
INSERT INTO `sys_logininfor` VALUES (351, '2020710419', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:14:32');
INSERT INTO `sys_logininfor` VALUES (352, '2020710428', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:14:33');
INSERT INTO `sys_logininfor` VALUES (353, '2020710425', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:14:33');
INSERT INTO `sys_logininfor` VALUES (354, '2020710426', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:14:33');
INSERT INTO `sys_logininfor` VALUES (355, '2020710419', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:14:35');
INSERT INTO `sys_logininfor` VALUES (356, '2020710439', '127.0.0.1', '内网IP', 'Chrome 9', 'Windows 10', '0', '登录成功', '2026-01-12 13:14:40');
INSERT INTO `sys_logininfor` VALUES (357, '2020710420', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:14:40');
INSERT INTO `sys_logininfor` VALUES (358, '2020710428', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:14:40');
INSERT INTO `sys_logininfor` VALUES (359, '2020710443', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:14:40');
INSERT INTO `sys_logininfor` VALUES (360, '2020710421', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '用户不存在/密码错误', '2026-01-12 13:14:40');
INSERT INTO `sys_logininfor` VALUES (361, '2020710411', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:14:41');
INSERT INTO `sys_logininfor` VALUES (362, '2020710419', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:14:43');
INSERT INTO `sys_logininfor` VALUES (363, '2020710407', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:14:44');
INSERT INTO `sys_logininfor` VALUES (364, '2020710440', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:14:48');
INSERT INTO `sys_logininfor` VALUES (365, '2020710438', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:14:49');
INSERT INTO `sys_logininfor` VALUES (366, '2020710428', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:14:50');
INSERT INTO `sys_logininfor` VALUES (367, '2020710435', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:14:50');
INSERT INTO `sys_logininfor` VALUES (368, '2020710401', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:14:52');
INSERT INTO `sys_logininfor` VALUES (369, '2020710424', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:14:53');
INSERT INTO `sys_logininfor` VALUES (370, '2020710422', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:14:53');
INSERT INTO `sys_logininfor` VALUES (371, '2020710437', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-12 13:14:53');
INSERT INTO `sys_logininfor` VALUES (372, '2020710428', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:14:55');
INSERT INTO `sys_logininfor` VALUES (373, '2020710434', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:14:56');
INSERT INTO `sys_logininfor` VALUES (374, '2020710435', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:14:56');
INSERT INTO `sys_logininfor` VALUES (375, '2020710424', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码已失效', '2026-01-12 13:14:58');
INSERT INTO `sys_logininfor` VALUES (376, '2020710404', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:14:59');
INSERT INTO `sys_logininfor` VALUES (377, '2020710421', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '用户不存在/密码错误', '2026-01-12 13:14:59');
INSERT INTO `sys_logininfor` VALUES (378, '2020710415', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:15:00');
INSERT INTO `sys_logininfor` VALUES (379, '2020710418', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:15:01');
INSERT INTO `sys_logininfor` VALUES (380, '2020710428', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:15:01');
INSERT INTO `sys_logininfor` VALUES (381, '2020710435', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:15:03');
INSERT INTO `sys_logininfor` VALUES (382, '2020710412', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:15:03');
INSERT INTO `sys_logininfor` VALUES (383, '2020710423', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:15:06');
INSERT INTO `sys_logininfor` VALUES (384, '20207140429', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:15:07');
INSERT INTO `sys_logininfor` VALUES (385, '2020710406', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:15:09');
INSERT INTO `sys_logininfor` VALUES (386, '2020710423', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:15:09');
INSERT INTO `sys_logininfor` VALUES (387, '2020710417', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:15:18');
INSERT INTO `sys_logininfor` VALUES (388, '2020710406', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:15:18');
INSERT INTO `sys_logininfor` VALUES (389, '2020710405', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:15:18');
INSERT INTO `sys_logininfor` VALUES (390, '2020710405', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:15:19');
INSERT INTO `sys_logininfor` VALUES (391, '2020710419', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:15:20');
INSERT INTO `sys_logininfor` VALUES (392, '2020710421', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:15:21');
INSERT INTO `sys_logininfor` VALUES (393, '20207140429', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '用户不存在/密码错误', '2026-01-12 13:15:21');
INSERT INTO `sys_logininfor` VALUES (394, '2020710427', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:15:22');
INSERT INTO `sys_logininfor` VALUES (395, '2020710416', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:15:24');
INSERT INTO `sys_logininfor` VALUES (396, '2020710419', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:15:26');
INSERT INTO `sys_logininfor` VALUES (397, '2020710410', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:15:27');
INSERT INTO `sys_logininfor` VALUES (398, '2020710436', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:15:28');
INSERT INTO `sys_logininfor` VALUES (399, '2020710405', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:15:33');
INSERT INTO `sys_logininfor` VALUES (400, '2020710419', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:15:40');
INSERT INTO `sys_logininfor` VALUES (401, '2020710429', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:15:45');
INSERT INTO `sys_logininfor` VALUES (402, '2020710419', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:15:48');
INSERT INTO `sys_logininfor` VALUES (403, '2020710408', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:15:50');
INSERT INTO `sys_logininfor` VALUES (404, '2020710419', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:15:55');
INSERT INTO `sys_logininfor` VALUES (405, '2020710443', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:15:56');
INSERT INTO `sys_logininfor` VALUES (406, '2020710433', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:16:13');
INSERT INTO `sys_logininfor` VALUES (407, '2020710419', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:16:13');
INSERT INTO `sys_logininfor` VALUES (408, '2020710405', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:16:15');
INSERT INTO `sys_logininfor` VALUES (409, '2020710419', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:16:18');
INSERT INTO `sys_logininfor` VALUES (410, '2020710419', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:16:26');
INSERT INTO `sys_logininfor` VALUES (411, '2020710419', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:16:37');
INSERT INTO `sys_logininfor` VALUES (412, '2020710419', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:16:46');
INSERT INTO `sys_logininfor` VALUES (413, '2020710406', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2026-01-12 13:16:56');
INSERT INTO `sys_logininfor` VALUES (414, '2020710406', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:17:13');
INSERT INTO `sys_logininfor` VALUES (415, '2020710406', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2026-01-12 13:18:08');
INSERT INTO `sys_logininfor` VALUES (416, '2020710435', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:18:27');
INSERT INTO `sys_logininfor` VALUES (417, '2020710435', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2026-01-12 13:18:36');
INSERT INTO `sys_logininfor` VALUES (418, '2020710406', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:18:47');
INSERT INTO `sys_logininfor` VALUES (419, '2020710438', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:22:33');
INSERT INTO `sys_logininfor` VALUES (420, '2020710417', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:24:56');
INSERT INTO `sys_logininfor` VALUES (421, '2020710413', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:35:52');
INSERT INTO `sys_logininfor` VALUES (422, '2020710417', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码已失效', '2026-01-12 13:42:49');
INSERT INTO `sys_logininfor` VALUES (423, '2020710438', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:43:00');
INSERT INTO `sys_logininfor` VALUES (424, '2020710417', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:43:01');
INSERT INTO `sys_logininfor` VALUES (425, '2020710419', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:43:07');
INSERT INTO `sys_logininfor` VALUES (426, '2020710419', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:43:11');
INSERT INTO `sys_logininfor` VALUES (427, '2020710417', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:43:15');
INSERT INTO `sys_logininfor` VALUES (428, '2020710419', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:43:20');
INSERT INTO `sys_logininfor` VALUES (429, '2020710437', '127.0.0.1', '内网IP', 'Chrome 9', 'Windows 10', '0', '登录成功', '2026-01-12 13:43:20');
INSERT INTO `sys_logininfor` VALUES (430, '2020710423', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:43:22');
INSERT INTO `sys_logininfor` VALUES (431, '2020710427', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码已失效', '2026-01-12 13:43:37');
INSERT INTO `sys_logininfor` VALUES (432, '2020710435', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-12 13:43:40');
INSERT INTO `sys_logininfor` VALUES (433, '2020710427', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:43:45');
INSERT INTO `sys_logininfor` VALUES (434, '2020710420', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:43:51');
INSERT INTO `sys_logininfor` VALUES (435, '2020710437', '127.0.0.1', '内网IP', 'Chrome 9', 'Windows 10', '0', '退出成功', '2026-01-12 13:45:13');
INSERT INTO `sys_logininfor` VALUES (436, '2020710421', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:45:18');
INSERT INTO `sys_logininfor` VALUES (437, '2020710437', '127.0.0.1', '内网IP', 'Chrome 9', 'Windows 10', '1', '验证码错误', '2026-01-12 13:45:23');
INSERT INTO `sys_logininfor` VALUES (438, '2020710437', '127.0.0.1', '内网IP', 'Chrome 9', 'Windows 10', '1', '验证码错误', '2026-01-12 13:45:29');
INSERT INTO `sys_logininfor` VALUES (439, '2020710437', '127.0.0.1', '内网IP', 'Chrome 9', 'Windows 10', '1', '验证码错误', '2026-01-12 13:45:35');
INSERT INTO `sys_logininfor` VALUES (440, '2020710437', '127.0.0.1', '内网IP', 'Chrome 9', 'Windows 10', '0', '登录成功', '2026-01-12 13:45:44');
INSERT INTO `sys_logininfor` VALUES (441, '2020710422', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:45:58');
INSERT INTO `sys_logininfor` VALUES (442, '2020710415', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:46:25');
INSERT INTO `sys_logininfor` VALUES (443, '2020710412', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:47:26');
INSERT INTO `sys_logininfor` VALUES (444, '2020710412', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:47:34');
INSERT INTO `sys_logininfor` VALUES (445, '2020710422', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:48:33');
INSERT INTO `sys_logininfor` VALUES (446, '2020710424', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:48:51');
INSERT INTO `sys_logininfor` VALUES (447, '2020710424', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:48:59');
INSERT INTO `sys_logininfor` VALUES (448, '2020710424', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:49:07');
INSERT INTO `sys_logininfor` VALUES (449, '2020710402', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:49:58');
INSERT INTO `sys_logininfor` VALUES (450, '2020710418', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:50:00');
INSERT INTO `sys_logininfor` VALUES (451, '2020710418', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:50:08');
INSERT INTO `sys_logininfor` VALUES (452, '2020710428', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:50:28');
INSERT INTO `sys_logininfor` VALUES (453, '2020710425', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:50:28');
INSERT INTO `sys_logininfor` VALUES (454, '2020710431', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:50:53');
INSERT INTO `sys_logininfor` VALUES (455, '2020710427', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:50:55');
INSERT INTO `sys_logininfor` VALUES (456, '2020710417', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:50:55');
INSERT INTO `sys_logininfor` VALUES (457, '2020710431', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:51:04');
INSERT INTO `sys_logininfor` VALUES (458, '2020710417', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:51:05');
INSERT INTO `sys_logininfor` VALUES (459, '2020710431', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:51:17');
INSERT INTO `sys_logininfor` VALUES (460, '2020710431', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2026-01-12 13:51:22');
INSERT INTO `sys_logininfor` VALUES (461, '2020710431', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:51:27');
INSERT INTO `sys_logininfor` VALUES (462, '2020710407', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:51:36');
INSERT INTO `sys_logininfor` VALUES (463, '2020710410', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:51:47');
INSERT INTO `sys_logininfor` VALUES (464, '2020710442', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:51:56');
INSERT INTO `sys_logininfor` VALUES (465, '2020710426', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:52:29');
INSERT INTO `sys_logininfor` VALUES (466, '2020710401', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:52:33');
INSERT INTO `sys_logininfor` VALUES (467, '2020710430', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-12 13:52:43');
INSERT INTO `sys_logininfor` VALUES (468, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-12 14:07:13');
INSERT INTO `sys_logininfor` VALUES (469, '2020710705', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-12 14:52:38');
INSERT INTO `sys_logininfor` VALUES (470, '2020710705', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-12 14:53:19');
INSERT INTO `sys_logininfor` VALUES (471, '2020710705', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-12 14:57:49');
INSERT INTO `sys_logininfor` VALUES (472, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-12 15:05:37');
INSERT INTO `sys_logininfor` VALUES (473, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-12 15:05:46');
INSERT INTO `sys_logininfor` VALUES (474, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-12 15:09:11');
INSERT INTO `sys_logininfor` VALUES (475, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-12 15:09:24');
INSERT INTO `sys_logininfor` VALUES (476, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-12 16:10:32');
INSERT INTO `sys_logininfor` VALUES (477, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-12 17:15:51');
INSERT INTO `sys_logininfor` VALUES (478, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2026-01-13 08:48:46');
INSERT INTO `sys_logininfor` VALUES (479, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 08:48:54');
INSERT INTO `sys_logininfor` VALUES (480, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-13 08:49:33');
INSERT INTO `sys_logininfor` VALUES (481, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 08:49:42');
INSERT INTO `sys_logininfor` VALUES (482, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-13 08:53:58');
INSERT INTO `sys_logininfor` VALUES (483, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 08:54:18');
INSERT INTO `sys_logininfor` VALUES (484, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-13 08:54:45');
INSERT INTO `sys_logininfor` VALUES (485, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 08:54:53');
INSERT INTO `sys_logininfor` VALUES (486, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-13 08:59:15');
INSERT INTO `sys_logininfor` VALUES (487, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 08:59:26');
INSERT INTO `sys_logininfor` VALUES (488, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-13 09:03:06');
INSERT INTO `sys_logininfor` VALUES (489, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 09:03:18');
INSERT INTO `sys_logininfor` VALUES (490, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-13 09:05:28');
INSERT INTO `sys_logininfor` VALUES (491, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 09:05:40');
INSERT INTO `sys_logininfor` VALUES (492, '2020710403', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 09:24:00');
INSERT INTO `sys_logininfor` VALUES (493, '2020710403', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-13 09:26:35');
INSERT INTO `sys_logininfor` VALUES (494, '2020710403', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 09:26:57');
INSERT INTO `sys_logininfor` VALUES (495, '2020710403', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-13 09:27:38');
INSERT INTO `sys_logininfor` VALUES (496, '2020710404', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 09:27:55');
INSERT INTO `sys_logininfor` VALUES (497, '2020710404', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-13 09:31:04');
INSERT INTO `sys_logininfor` VALUES (498, '2020710403', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 09:31:10');
INSERT INTO `sys_logininfor` VALUES (499, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 10:05:34');
INSERT INTO `sys_logininfor` VALUES (500, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 10:43:34');
INSERT INTO `sys_logininfor` VALUES (501, '2020710404', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 11:12:54');
INSERT INTO `sys_logininfor` VALUES (502, '2020710404', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 11:13:31');
INSERT INTO `sys_logininfor` VALUES (503, '2020710404', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-13 11:14:58');
INSERT INTO `sys_logininfor` VALUES (504, '2020710403', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 11:15:14');
INSERT INTO `sys_logininfor` VALUES (505, '2020710403', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-13 11:16:11');
INSERT INTO `sys_logininfor` VALUES (506, '2020710404', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2026-01-13 11:16:18');
INSERT INTO `sys_logininfor` VALUES (507, '2020710404', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 11:16:22');
INSERT INTO `sys_logininfor` VALUES (508, '2020710404', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-13 11:19:33');
INSERT INTO `sys_logininfor` VALUES (509, '2020710706', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 11:19:54');
INSERT INTO `sys_logininfor` VALUES (510, '2020710706', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-13 11:34:05');
INSERT INTO `sys_logininfor` VALUES (511, '2020710407', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 11:34:12');
INSERT INTO `sys_logininfor` VALUES (512, '2020710407', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-13 11:34:29');
INSERT INTO `sys_logininfor` VALUES (513, '2020710408', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 11:34:37');
INSERT INTO `sys_logininfor` VALUES (514, '2020710408', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-13 11:36:34');
INSERT INTO `sys_logininfor` VALUES (515, '2020710411', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 11:36:54');
INSERT INTO `sys_logininfor` VALUES (516, '2020710407', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 12:58:42');
INSERT INTO `sys_logininfor` VALUES (517, '2020710407', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-13 12:59:16');
INSERT INTO `sys_logininfor` VALUES (518, '2020710408', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 12:59:33');
INSERT INTO `sys_logininfor` VALUES (519, '2020710408', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-13 13:01:25');
INSERT INTO `sys_logininfor` VALUES (520, '2020710415', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 13:01:45');
INSERT INTO `sys_logininfor` VALUES (521, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 13:02:54');
INSERT INTO `sys_logininfor` VALUES (522, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 13:09:31');
INSERT INTO `sys_logininfor` VALUES (523, '2020710243', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:14');
INSERT INTO `sys_logininfor` VALUES (524, '2020710206', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:20');
INSERT INTO `sys_logininfor` VALUES (525, '2020710215', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:20');
INSERT INTO `sys_logininfor` VALUES (526, '2020710232', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:20');
INSERT INTO `sys_logininfor` VALUES (527, '2020710204', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:21');
INSERT INTO `sys_logininfor` VALUES (528, '2020710233', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:22');
INSERT INTO `sys_logininfor` VALUES (529, '2020710213', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:22');
INSERT INTO `sys_logininfor` VALUES (530, '2020710205', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:23');
INSERT INTO `sys_logininfor` VALUES (531, '2020710242', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:23');
INSERT INTO `sys_logininfor` VALUES (532, '2020710207', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:23');
INSERT INTO `sys_logininfor` VALUES (533, '2020710226', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:24');
INSERT INTO `sys_logininfor` VALUES (534, '2020710202', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:25');
INSERT INTO `sys_logininfor` VALUES (535, '2020710209', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:25');
INSERT INTO `sys_logininfor` VALUES (536, '2020710228', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:25');
INSERT INTO `sys_logininfor` VALUES (537, '2020710208', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:25');
INSERT INTO `sys_logininfor` VALUES (538, '2020710237', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:26');
INSERT INTO `sys_logininfor` VALUES (539, '2020710217', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:26');
INSERT INTO `sys_logininfor` VALUES (540, '2020710238', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:26');
INSERT INTO `sys_logininfor` VALUES (541, '202071021 6', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '用户不存在/密码错误', '2026-01-13 13:20:26');
INSERT INTO `sys_logininfor` VALUES (542, '2020710231', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:27');
INSERT INTO `sys_logininfor` VALUES (543, '2020710224', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:27');
INSERT INTO `sys_logininfor` VALUES (544, '2020710241', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:27');
INSERT INTO `sys_logininfor` VALUES (545, '2020710219', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:27');
INSERT INTO `sys_logininfor` VALUES (546, '2020710212', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:27');
INSERT INTO `sys_logininfor` VALUES (547, '2020710240', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:28');
INSERT INTO `sys_logininfor` VALUES (548, '2020710210', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:28');
INSERT INTO `sys_logininfor` VALUES (549, '2020710229', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:29');
INSERT INTO `sys_logininfor` VALUES (550, '2020710234', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:30');
INSERT INTO `sys_logininfor` VALUES (551, '2020710214', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '用户不存在/密码错误', '2026-01-13 13:20:30');
INSERT INTO `sys_logininfor` VALUES (552, '2020710227', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:31');
INSERT INTO `sys_logininfor` VALUES (553, '2020710211', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:31');
INSERT INTO `sys_logininfor` VALUES (554, '2020710239', '127.0.0.1', '内网IP', 'Chrome 9', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:32');
INSERT INTO `sys_logininfor` VALUES (555, '2020710225', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:32');
INSERT INTO `sys_logininfor` VALUES (556, '2020710223', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:33');
INSERT INTO `sys_logininfor` VALUES (557, '2020710211', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:34');
INSERT INTO `sys_logininfor` VALUES (558, '2020710218', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:34');
INSERT INTO `sys_logininfor` VALUES (559, '2020710203', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '用户不存在/密码错误', '2026-01-13 13:20:35');
INSERT INTO `sys_logininfor` VALUES (560, '2020710216', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:36');
INSERT INTO `sys_logininfor` VALUES (561, '2020710220', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:37');
INSERT INTO `sys_logininfor` VALUES (562, '2020710236', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:37');
INSERT INTO `sys_logininfor` VALUES (563, '2020710214', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:38');
INSERT INTO `sys_logininfor` VALUES (564, '2020710201', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:40');
INSERT INTO `sys_logininfor` VALUES (565, '2020710235', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:42');
INSERT INTO `sys_logininfor` VALUES (566, '2020710203', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:45');
INSERT INTO `sys_logininfor` VALUES (567, '2020710237', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:20:59');
INSERT INTO `sys_logininfor` VALUES (568, '2020710221', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:21:10');
INSERT INTO `sys_logininfor` VALUES (569, '2020710222', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:21:33');
INSERT INTO `sys_logininfor` VALUES (570, '2020710230', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 13:21:45');
INSERT INTO `sys_logininfor` VALUES (571, '2020710242', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:42:27');
INSERT INTO `sys_logininfor` VALUES (572, '2020710221', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:42:29');
INSERT INTO `sys_logininfor` VALUES (573, '2020710210', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:42:49');
INSERT INTO `sys_logininfor` VALUES (574, '2020710235', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:42:53');
INSERT INTO `sys_logininfor` VALUES (575, '2020710237', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:43:02');
INSERT INTO `sys_logininfor` VALUES (576, '2020710236', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:43:03');
INSERT INTO `sys_logininfor` VALUES (577, '2020710224', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:43:19');
INSERT INTO `sys_logininfor` VALUES (578, '2020710227', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:45:23');
INSERT INTO `sys_logininfor` VALUES (579, '2020710232', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:48:27');
INSERT INTO `sys_logininfor` VALUES (580, '2020710232', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:49:43');
INSERT INTO `sys_logininfor` VALUES (581, '2020710217', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:49:57');
INSERT INTO `sys_logininfor` VALUES (582, '2020710215', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:49:59');
INSERT INTO `sys_logininfor` VALUES (583, '2020710243', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:50:06');
INSERT INTO `sys_logininfor` VALUES (584, '2020710233', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:50:09');
INSERT INTO `sys_logininfor` VALUES (585, '2020710231', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:50:12');
INSERT INTO `sys_logininfor` VALUES (586, '2020710242', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:50:16');
INSERT INTO `sys_logininfor` VALUES (587, '2020710229', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:50:17');
INSERT INTO `sys_logininfor` VALUES (588, '2020710207', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:50:28');
INSERT INTO `sys_logininfor` VALUES (589, '2020710228', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:50:29');
INSERT INTO `sys_logininfor` VALUES (590, '2020710225', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:50:33');
INSERT INTO `sys_logininfor` VALUES (591, '2020710211', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:50:33');
INSERT INTO `sys_logininfor` VALUES (592, '2020710238', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:50:35');
INSERT INTO `sys_logininfor` VALUES (593, '2020710219', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:50:36');
INSERT INTO `sys_logininfor` VALUES (594, '2020710208', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:50:41');
INSERT INTO `sys_logininfor` VALUES (595, '2020710209', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:50:42');
INSERT INTO `sys_logininfor` VALUES (596, '2020710206', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 13:50:46');
INSERT INTO `sys_logininfor` VALUES (597, '202710218', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '用户不存在/密码错误', '2026-01-13 13:50:56');
INSERT INTO `sys_logininfor` VALUES (598, '2020710107', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:20:45');
INSERT INTO `sys_logininfor` VALUES (599, '2020710107', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2026-01-13 14:26:15');
INSERT INTO `sys_logininfor` VALUES (600, '2020710109', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:43');
INSERT INTO `sys_logininfor` VALUES (601, '2020710120', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:43');
INSERT INTO `sys_logininfor` VALUES (602, '2020710134', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:43');
INSERT INTO `sys_logininfor` VALUES (603, '2020710142', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:45');
INSERT INTO `sys_logininfor` VALUES (604, '2020710125', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:46');
INSERT INTO `sys_logininfor` VALUES (605, '2020710124', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:46');
INSERT INTO `sys_logininfor` VALUES (606, '2020710102', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:47');
INSERT INTO `sys_logininfor` VALUES (607, '2020710144', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:47');
INSERT INTO `sys_logininfor` VALUES (608, '2020710121', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:47');
INSERT INTO `sys_logininfor` VALUES (609, '2020710110', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:48');
INSERT INTO `sys_logininfor` VALUES (610, '2020710103', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:50');
INSERT INTO `sys_logininfor` VALUES (611, '2020710128', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:50');
INSERT INTO `sys_logininfor` VALUES (612, '2020710114', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:50');
INSERT INTO `sys_logininfor` VALUES (613, '2020710118', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:51');
INSERT INTO `sys_logininfor` VALUES (614, '2020710143', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:51');
INSERT INTO `sys_logininfor` VALUES (615, '2020710123', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:52');
INSERT INTO `sys_logininfor` VALUES (616, '2020710139', '127.0.0.1', '内网IP', 'Chrome 9', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:52');
INSERT INTO `sys_logininfor` VALUES (617, '2020710141', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:52');
INSERT INTO `sys_logininfor` VALUES (618, '2020710136', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:52');
INSERT INTO `sys_logininfor` VALUES (619, '2020710113', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:53');
INSERT INTO `sys_logininfor` VALUES (620, '2020710135', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:53');
INSERT INTO `sys_logininfor` VALUES (621, '2020710105', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:53');
INSERT INTO `sys_logininfor` VALUES (622, '2020710111', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:53');
INSERT INTO `sys_logininfor` VALUES (623, '2020710131', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:55');
INSERT INTO `sys_logininfor` VALUES (624, '2020710115', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:55');
INSERT INTO `sys_logininfor` VALUES (625, '2020710129', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:55');
INSERT INTO `sys_logininfor` VALUES (626, '2020710112', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:55');
INSERT INTO `sys_logininfor` VALUES (627, '2020710119', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:55');
INSERT INTO `sys_logininfor` VALUES (628, '2020710126', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:55');
INSERT INTO `sys_logininfor` VALUES (629, '2020710130', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:55');
INSERT INTO `sys_logininfor` VALUES (630, '2020710127', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:55');
INSERT INTO `sys_logininfor` VALUES (631, '2020710101', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:56');
INSERT INTO `sys_logininfor` VALUES (632, '2020710116', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:57');
INSERT INTO `sys_logininfor` VALUES (633, '2020710108', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:58');
INSERT INTO `sys_logininfor` VALUES (634, '2020710140', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:26:59');
INSERT INTO `sys_logininfor` VALUES (635, '20207117', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '用户不存在/密码错误', '2026-01-13 14:27:00');
INSERT INTO `sys_logininfor` VALUES (636, '2020710122', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:27:01');
INSERT INTO `sys_logininfor` VALUES (637, '2020710104', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:27:08');
INSERT INTO `sys_logininfor` VALUES (638, '202071017', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '用户不存在/密码错误', '2026-01-13 14:27:12');
INSERT INTO `sys_logininfor` VALUES (639, '2020710117', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:27:20');
INSERT INTO `sys_logininfor` VALUES (640, '2020710110', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:28:10');
INSERT INTO `sys_logininfor` VALUES (641, '2020710122', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:39:35');
INSERT INTO `sys_logininfor` VALUES (642, '2020710129', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:39:46');
INSERT INTO `sys_logininfor` VALUES (643, '2020710122', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '用户不存在/密码错误', '2026-01-13 14:41:29');
INSERT INTO `sys_logininfor` VALUES (644, '2020710122', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-13 14:41:38');
INSERT INTO `sys_logininfor` VALUES (645, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 15:05:07');
INSERT INTO `sys_logininfor` VALUES (646, '2020710105', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 15:05:28');
INSERT INTO `sys_logininfor` VALUES (647, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-13 16:33:00');
INSERT INTO `sys_logininfor` VALUES (648, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-13 16:43:57');
INSERT INTO `sys_logininfor` VALUES (649, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-14 08:40:13');
INSERT INTO `sys_logininfor` VALUES (650, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-14 10:42:22');
INSERT INTO `sys_logininfor` VALUES (651, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-14 14:52:28');
INSERT INTO `sys_logininfor` VALUES (652, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-14 16:07:39');
INSERT INTO `sys_logininfor` VALUES (653, '2020710107', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-14 16:21:30');
INSERT INTO `sys_logininfor` VALUES (654, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-15 08:40:32');
INSERT INTO `sys_logininfor` VALUES (655, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-15 15:25:25');
INSERT INTO `sys_logininfor` VALUES (656, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-15 15:45:10');
INSERT INTO `sys_logininfor` VALUES (657, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '用户不存在/密码错误', '2026-01-16 11:39:33');
INSERT INTO `sys_logininfor` VALUES (658, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-16 11:39:40');
INSERT INTO `sys_logininfor` VALUES (659, '2020710503', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:34:49');
INSERT INTO `sys_logininfor` VALUES (660, '2020710537', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:27');
INSERT INTO `sys_logininfor` VALUES (661, '2020710534', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:33');
INSERT INTO `sys_logininfor` VALUES (662, '2020710502', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:35');
INSERT INTO `sys_logininfor` VALUES (663, '2020710522', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:36');
INSERT INTO `sys_logininfor` VALUES (664, '2020710540', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:38');
INSERT INTO `sys_logininfor` VALUES (665, '2020710533', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:38');
INSERT INTO `sys_logininfor` VALUES (666, '2020710541', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:39');
INSERT INTO `sys_logininfor` VALUES (667, '2020710531', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:39');
INSERT INTO `sys_logininfor` VALUES (668, '2020710529', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:39');
INSERT INTO `sys_logininfor` VALUES (669, '2020710528', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:40');
INSERT INTO `sys_logininfor` VALUES (670, '2020710506', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:40');
INSERT INTO `sys_logininfor` VALUES (671, '2020710538', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:40');
INSERT INTO `sys_logininfor` VALUES (672, '2020710521', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:41');
INSERT INTO `sys_logininfor` VALUES (673, '2020710527', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:41');
INSERT INTO `sys_logininfor` VALUES (674, '2020710513', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:41');
INSERT INTO `sys_logininfor` VALUES (675, '2020710526', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:41');
INSERT INTO `sys_logininfor` VALUES (676, '2020710517', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:41');
INSERT INTO `sys_logininfor` VALUES (677, '2020710519', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:42');
INSERT INTO `sys_logininfor` VALUES (678, '2020710520', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:42');
INSERT INTO `sys_logininfor` VALUES (679, '2020710536', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:42');
INSERT INTO `sys_logininfor` VALUES (680, '2020710510', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:42');
INSERT INTO `sys_logininfor` VALUES (681, '2020710514', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:42');
INSERT INTO `sys_logininfor` VALUES (682, '2020710511', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:43');
INSERT INTO `sys_logininfor` VALUES (683, '2020710501', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:43');
INSERT INTO `sys_logininfor` VALUES (684, '2020710509', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:43');
INSERT INTO `sys_logininfor` VALUES (685, '2020710518', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:43');
INSERT INTO `sys_logininfor` VALUES (686, '2020710512', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:43');
INSERT INTO `sys_logininfor` VALUES (687, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:45');
INSERT INTO `sys_logininfor` VALUES (688, '2020710543', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:45');
INSERT INTO `sys_logininfor` VALUES (689, '2020710535', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:45');
INSERT INTO `sys_logininfor` VALUES (690, '2020710530', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:45');
INSERT INTO `sys_logininfor` VALUES (691, '2020710542', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:45');
INSERT INTO `sys_logininfor` VALUES (692, '2020710504', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:46');
INSERT INTO `sys_logininfor` VALUES (693, '2020710525', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:46');
INSERT INTO `sys_logininfor` VALUES (694, '2020710539', '127.0.0.1', '内网IP', 'Chrome 9', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:48');
INSERT INTO `sys_logininfor` VALUES (695, '2020710515', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:48');
INSERT INTO `sys_logininfor` VALUES (696, '2020710508', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:49');
INSERT INTO `sys_logininfor` VALUES (697, '2020710532', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:52');
INSERT INTO `sys_logininfor` VALUES (698, '2020710503', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:53');
INSERT INTO `sys_logininfor` VALUES (699, '2020710524', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:53');
INSERT INTO `sys_logininfor` VALUES (700, '2020710516', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:39:55');
INSERT INTO `sys_logininfor` VALUES (701, '2020710523', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:40:09');
INSERT INTO `sys_logininfor` VALUES (702, '2020710507', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:43:20');
INSERT INTO `sys_logininfor` VALUES (703, '2020710531', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 12:53:50');
INSERT INTO `sys_logininfor` VALUES (704, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-16 13:13:58');
INSERT INTO `sys_logininfor` VALUES (705, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-16 13:37:19');
INSERT INTO `sys_logininfor` VALUES (706, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-16 13:37:29');
INSERT INTO `sys_logininfor` VALUES (707, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-16 13:48:48');
INSERT INTO `sys_logininfor` VALUES (708, '19157727791', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-16 13:49:00');
INSERT INTO `sys_logininfor` VALUES (709, '2020710605', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:19:11');
INSERT INTO `sys_logininfor` VALUES (710, '2020710605', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2026-01-16 14:22:32');
INSERT INTO `sys_logininfor` VALUES (711, '2020710613', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:01');
INSERT INTO `sys_logininfor` VALUES (712, '2020710609', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:01');
INSERT INTO `sys_logininfor` VALUES (713, '2020710606', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:01');
INSERT INTO `sys_logininfor` VALUES (714, '2020710602', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:02');
INSERT INTO `sys_logininfor` VALUES (715, '2020710612', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:02');
INSERT INTO `sys_logininfor` VALUES (716, '2020710636', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:02');
INSERT INTO `sys_logininfor` VALUES (717, '2020710638', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:03');
INSERT INTO `sys_logininfor` VALUES (718, '2020710604', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:04');
INSERT INTO `sys_logininfor` VALUES (719, '2020710605', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:04');
INSERT INTO `sys_logininfor` VALUES (720, '2020710624', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:04');
INSERT INTO `sys_logininfor` VALUES (721, '2020710639', '127.0.0.1', '内网IP', 'Chrome 9', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:04');
INSERT INTO `sys_logininfor` VALUES (722, '2020710627', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:05');
INSERT INTO `sys_logininfor` VALUES (723, '2020710633', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:05');
INSERT INTO `sys_logininfor` VALUES (724, '2020710619', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:06');
INSERT INTO `sys_logininfor` VALUES (725, '2020710608', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:06');
INSERT INTO `sys_logininfor` VALUES (726, '2020710611', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:06');
INSERT INTO `sys_logininfor` VALUES (727, '2020710614', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '用户不存在/密码错误', '2026-01-16 14:23:06');
INSERT INTO `sys_logininfor` VALUES (728, '2020710620', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:07');
INSERT INTO `sys_logininfor` VALUES (729, '2020710616', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:07');
INSERT INTO `sys_logininfor` VALUES (730, '2020710623', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:07');
INSERT INTO `sys_logininfor` VALUES (731, '2020710629', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:07');
INSERT INTO `sys_logininfor` VALUES (732, '2020710618', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:08');
INSERT INTO `sys_logininfor` VALUES (733, '2020710625', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:08');
INSERT INTO `sys_logininfor` VALUES (734, '2020710637', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:08');
INSERT INTO `sys_logininfor` VALUES (735, '2020710617', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:08');
INSERT INTO `sys_logininfor` VALUES (736, '2020710628', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:08');
INSERT INTO `sys_logininfor` VALUES (737, '2020710631', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:09');
INSERT INTO `sys_logininfor` VALUES (738, '2020710641', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:09');
INSERT INTO `sys_logininfor` VALUES (739, '2020710634', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:09');
INSERT INTO `sys_logininfor` VALUES (740, '2020710622', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:10');
INSERT INTO `sys_logininfor` VALUES (741, '2020710626', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:10');
INSERT INTO `sys_logininfor` VALUES (742, '2020710607', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:11');
INSERT INTO `sys_logininfor` VALUES (743, '2020710621', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:11');
INSERT INTO `sys_logininfor` VALUES (744, '2020710601', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:11');
INSERT INTO `sys_logininfor` VALUES (745, '2020710610', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:11');
INSERT INTO `sys_logininfor` VALUES (746, '2020710635', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:11');
INSERT INTO `sys_logininfor` VALUES (747, '2020710642', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:13');
INSERT INTO `sys_logininfor` VALUES (748, '2020710630', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '用户不存在/密码错误', '2026-01-16 14:23:14');
INSERT INTO `sys_logininfor` VALUES (749, '2020710614', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:17');
INSERT INTO `sys_logininfor` VALUES (750, '2020710630', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:17');
INSERT INTO `sys_logininfor` VALUES (751, '2020710634', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:30');
INSERT INTO `sys_logininfor` VALUES (752, '2020710634', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '退出成功', '2026-01-16 14:23:30');
INSERT INTO `sys_logininfor` VALUES (753, '2020710634', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:23:37');
INSERT INTO `sys_logininfor` VALUES (754, '2020710634', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:24:47');
INSERT INTO `sys_logininfor` VALUES (755, '2020710623', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2026-01-16 14:31:55');
INSERT INTO `sys_logininfor` VALUES (756, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-16 15:19:03');
INSERT INTO `sys_logininfor` VALUES (757, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '退出成功', '2026-01-16 15:48:14');
INSERT INTO `sys_logininfor` VALUES (758, 'laoda', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-16 15:48:22');

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
) ENGINE = InnoDB AUTO_INCREMENT = 2044 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '菜单权限表' ROW_FORMAT = DYNAMIC;

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
INSERT INTO `sys_menu` VALUES (2036, '题库管理导出', 2031, 6, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:question:export', '#', 'admin', '2025-08-18 20:03:35', 'admin', '2026-01-12 15:08:57', '');
INSERT INTO `sys_menu` VALUES (2037, '教师首页', 0, 0, 'teacher-dashboard', 'business/teacher/index', NULL, '', 1, 0, 'C', '0', '0', 'business:teacher:list', 'theme', 'admin', '2025-08-25 20:05:44', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2038, '班级管理', 0, 5, 'teacherClass', 'business/teacherClass/index', NULL, '', 1, 0, 'C', '0', '0', 'business:teacherClass:list', 'peoples', 'admin', '2025-12-29 14:22:28', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2039, '添加班级', 2038, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'business:teacherClass:add', '#', 'admin', '2025-12-29 14:22:28', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2040, '移除班级', 2038, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'business:teacherClass:remove', '#', 'admin', '2025-12-29 14:22:28', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2042, '成绩查询', 0, 5, 'score', 'business/score/index', NULL, '', 1, 0, 'C', '0', '0', 'business:score:list', 'job', '19157727791', '2026-01-08 09:44:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2043, '题目导入', 2031, 5, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'business:question:import', '#', 'admin', '2026-01-12 15:08:46', '', NULL, '');

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
) ENGINE = InnoDB AUTO_INCREMENT = 469 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '操作日志记录' ROW_FORMAT = DYNAMIC;

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
INSERT INTO `sys_oper_log` VALUES (407, '课程管理', 3, 'com.ruoyi.business.controller.BizLessonController.remove()', 'DELETE', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/16', '127.0.0.1', '内网IP', '[16]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-09 08:57:59', 112);
INSERT INTO `sys_oper_log` VALUES (408, '课程管理', 3, 'com.ruoyi.business.controller.BizLessonController.remove()', 'DELETE', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/17', '127.0.0.1', '内网IP', '[17]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-09 08:58:02', 23);
INSERT INTO `sys_oper_log` VALUES (409, '课程管理', 3, 'com.ruoyi.business.controller.BizLessonController.remove()', 'DELETE', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/19', '127.0.0.1', '内网IP', '[19]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-09 08:58:04', 17);
INSERT INTO `sys_oper_log` VALUES (410, '课程管理', 3, 'com.ruoyi.business.controller.BizLessonController.remove()', 'DELETE', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/20', '127.0.0.1', '内网IP', '[20]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-09 08:58:06', 19);
INSERT INTO `sys_oper_log` VALUES (411, '课程管理', 3, 'com.ruoyi.business.controller.BizLessonController.remove()', 'DELETE', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/18', '127.0.0.1', '内网IP', '[18]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-09 08:58:08', 24);
INSERT INTO `sys_oper_log` VALUES (412, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":21,\"lessonNum\":1,\"lessonTitle\":\"第一课\",\"params\":{},\"questions\":[{\"orderNum\":1,\"params\":{},\"questionContent\":\"一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。\",\"questionId\":43,\"questionScore\":10,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":189},{\"answer\":\"A\",\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":2,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":15,\"questionScore\":10,\"questionType\":\"choice\"},{\"answer\":\"F\",\"optionA\":\"\",\"optionB\":\"\",\"optionC\":\"\",\"optionD\":\"\",\"orderNum\":3,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":29,\"questionScore\":10,\"questionType\":\"judgment\"},{\"orderNum\":4,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/08/111_20260108085453A001.pdf\",\"questionContent\":\"操作18\",\"questionId\":51,\"questionScore\":70,\"questionType\":\"practical\"}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":21,\"lessonNum\":1,\"lessonTitle\":\"第一课\",\"params\":{},\"questions\":[{\"orderNum\":1,\"params\":{},\"questionContent\":\"一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。\",\"questionId\":43,\"questionScore\":10,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":189},{\"answer\":\"A\",\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":2,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":15,\"questionScore\":10,\"questionType\":\"choice\"},{\"answer\":\"F\",\"orderNum\":3,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":29,\"questionScore\":10,\"questionType\":\"judgment\"},{\"orderNum\":4,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/08/111_20260108085453A001.pdf\",\"questionContent\":\"操作18\",\"questionId\":51,\"questionScore\":70,\"questionType\":\"practical\"}],\"semester\":\"0\"}}', 0, NULL, '2026-01-09 08:59:22', 69);
INSERT INTO `sys_oper_log` VALUES (413, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":22,\"lessonNum\":2,\"lessonTitle\":\"第二课\",\"params\":{},\"questions\":[{\"answer\":\"A\",\"optionA\":\"a\",\"optionB\":\"b\",\"optionC\":\"cv\",\"optionD\":\"d\",\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":50,\"questionType\":\"choice\"},{\"orderNum\":2,\"params\":{},\"questionContent\":\"一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。\",\"questionId\":43,\"questionScore\":50,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":189}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":22,\"lessonNum\":2,\"lessonTitle\":\"第二课\",\"params\":{},\"questions\":[{\"answer\":\"A\",\"optionA\":\"a\",\"optionB\":\"b\",\"optionC\":\"cv\",\"optionD\":\"d\",\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":50,\"questionType\":\"choice\"},{\"orderNum\":2,\"params\":{},\"questionContent\":\"一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。\",\"questionId\":43,\"questionScore\":50,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":189}],\"semester\":\"0\"}}', 0, NULL, '2026-01-09 09:42:47', 36);
INSERT INTO `sys_oper_log` VALUES (414, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":22,\"lessonNum\":2,\"lessonTitle\":\"第二\",\"params\":{},\"questions\":[{\"answer\":\"A\",\"lessonId\":22,\"optionA\":\"a\",\"optionB\":\"b\",\"optionC\":\"cv\",\"optionD\":\"d\",\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":50,\"questionType\":\"choice\"},{\"lessonId\":22,\"orderNum\":2,\"params\":{},\"questionContent\":\"一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。\",\"questionId\":43,\"questionScore\":50,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":189}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":22,\"lessonNum\":2,\"lessonTitle\":\"第二\",\"params\":{},\"questions\":[{\"answer\":\"A\",\"lessonId\":22,\"optionA\":\"a\",\"optionB\":\"b\",\"optionC\":\"cv\",\"optionD\":\"d\",\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":50,\"questionType\":\"choice\"},{\"lessonId\":22,\"orderNum\":2,\"params\":{},\"questionContent\":\"一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。\",\"questionId\":43,\"questionScore\":50,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":189}],\"semester\":\"0\"}}', 0, NULL, '2026-01-09 12:17:56', 82);
INSERT INTO `sys_oper_log` VALUES (415, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\"],\"grade\":7,\"lessonId\":22,\"lessonNum\":2,\"lessonTitle\":\"第二\",\"params\":{},\"questions\":[{\"answer\":\"A\",\"lessonId\":22,\"optionA\":\"a\",\"optionB\":\"b\",\"optionC\":\"cv\",\"optionD\":\"d\",\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":50,\"questionType\":\"choice\"},{\"lessonId\":22,\"orderNum\":2,\"params\":{},\"questionContent\":\"一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。\",\"questionId\":43,\"questionScore\":50,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":189}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\"],\"grade\":7,\"lessonId\":22,\"lessonNum\":2,\"lessonTitle\":\"第二\",\"params\":{},\"questions\":[{\"answer\":\"A\",\"lessonId\":22,\"optionA\":\"a\",\"optionB\":\"b\",\"optionC\":\"cv\",\"optionD\":\"d\",\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":50,\"questionType\":\"choice\"},{\"lessonId\":22,\"orderNum\":2,\"params\":{},\"questionContent\":\"一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。\",\"questionId\":43,\"questionScore\":50,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":189}],\"semester\":\"0\"}}', 0, NULL, '2026-01-09 12:19:10', 40);
INSERT INTO `sys_oper_log` VALUES (416, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"5班\"],\"grade\":7,\"lessonId\":21,\"lessonNum\":1,\"lessonTitle\":\"第一课\",\"params\":{},\"questions\":[{\"lessonId\":21,\"orderNum\":1,\"params\":{},\"questionContent\":\"一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。\",\"questionId\":43,\"questionScore\":10,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":189},{\"answer\":\"A\",\"lessonId\":21,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":2,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":15,\"questionScore\":10,\"questionType\":\"choice\"},{\"answer\":\"F\",\"lessonId\":21,\"optionA\":\"\",\"optionB\":\"\",\"optionC\":\"\",\"optionD\":\"\",\"orderNum\":3,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":29,\"questionScore\":10,\"questionType\":\"judgment\"},{\"filePath\":\"/profile/upload/2026/01/08/111_20260108085453A001.docx\",\"lessonId\":21,\"orderNum\":4,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/08/111_20260108085453A001.pdf\",\"questionContent\":\"操作18\",\"questionId\":51,\"questionScore\":70,\"questionType\":\"practical\"}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"5班\"],\"grade\":7,\"lessonId\":21,\"lessonNum\":1,\"lessonTitle\":\"第一课\",\"params\":{},\"questions\":[{\"lessonId\":21,\"orderNum\":1,\"params\":{},\"questionContent\":\"一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。\",\"questionId\":43,\"questionScore\":10,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":189},{\"answer\":\"A\",\"lessonId\":21,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":2,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":15,\"questionScore\":10,\"questionType\":\"choice\"},{\"answer\":\"F\",\"lessonId\":21,\"orderNum\":3,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":29,\"questionScore\":10,\"questionType\":\"judgment\"},{\"filePath\":\"/profile/upload/2026/01/08/111_20260108085453A001.docx\",\"lessonId\":21,\"orderNum\":4,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/08/111_20260108085453A001.pdf\",\"questionContent\":\"操作18\",\"questionId\":51,\"questionScore\":70,\"questionType\":\"practical\"}],\"semester\":\"0\"}}', 0, NULL, '2026-01-09 12:19:21', 31);
INSERT INTO `sys_oper_log` VALUES (417, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":22,\"lessonNum\":2,\"lessonTitle\":\"第二\",\"params\":{},\"questions\":[{\"answer\":\"A\",\"lessonId\":22,\"optionA\":\"a\",\"optionB\":\"b\",\"optionC\":\"cv\",\"optionD\":\"d\",\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":50,\"questionType\":\"choice\"},{\"lessonId\":22,\"orderNum\":2,\"params\":{},\"questionContent\":\"一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。\",\"questionId\":43,\"questionScore\":50,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":189}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\",\"5班\"],\"grade\":7,\"lessonId\":22,\"lessonNum\":2,\"lessonTitle\":\"第二\",\"params\":{},\"questions\":[{\"answer\":\"A\",\"lessonId\":22,\"optionA\":\"a\",\"optionB\":\"b\",\"optionC\":\"cv\",\"optionD\":\"d\",\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":50,\"questionType\":\"choice\"},{\"lessonId\":22,\"orderNum\":2,\"params\":{},\"questionContent\":\"一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。\",\"questionId\":43,\"questionScore\":50,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":189}],\"semester\":\"0\"}}', 0, NULL, '2026-01-09 14:03:45', 76);
INSERT INTO `sys_oper_log` VALUES (418, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"19157727791\",\"createTime\":\"2026-01-12 09:14:59\",\"creatorId\":104,\"filePath\":\"/profile/upload/2026/01/12/浙里报报销流程（实验）_20260112091447A001.docx\",\"grade\":7,\"isPublic\":\"Y\",\"lessonNum\":4,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/12/浙里报报销流程（实验）_20260112091447A001.pdf\",\"previewStatus\":\"pending\",\"questionContent\":\"1212\",\"questionId\":52,\"questionType\":\"practical\",\"scoringItems\":[],\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-12 09:14:59', 119);
INSERT INTO `sys_oper_log` VALUES (419, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"19157727791\",\"createTime\":\"2026-01-12 09:21:09\",\"creatorId\":104,\"filePath\":\"/profile/upload/2026/01/12/浙里报报销流程（实验）_20260112092056A001.docx\",\"grade\":6,\"isPublic\":\"Y\",\"lessonNum\":1,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/12/浙里报报销流程（实验）_20260112092056A001.pdf\",\"previewStatus\":\"pending\",\"questionContent\":\"任务\",\"questionId\":53,\"questionType\":\"practical\",\"scoringItems\":[{\"itemId\":10,\"itemName\":\"界面\",\"itemScore\":100,\"orderNum\":0,\"params\":{},\"questionId\":53}],\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-12 09:21:09', 369);
INSERT INTO `sys_oper_log` VALUES (420, '题库管理', 2, 'com.ruoyi.business.controller.BizQuestionController.edit()', 'PUT', 1, '19157727791', '大目湾学校(初中部)', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"19157727791\",\"createTime\":\"2026-01-12 09:21:10\",\"creatorId\":104,\"filePath\":\"/profile/upload/2026/01/12/浙里报报销流程（实验）_20260112092056A001.docx\",\"grade\":7,\"isPublic\":\"Y\",\"lessonNum\":1,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/12/浙里报报销流程（实验）_20260112092056A001.pdf\",\"previewStatus\":\"pending\",\"questionContent\":\"任务\",\"questionId\":53,\"questionType\":\"practical\",\"scoringItems\":[{\"createTime\":\"2026-01-12 09:21:09\",\"itemId\":11,\"itemName\":\"界面\",\"itemScore\":100,\"orderNum\":0,\"params\":{},\"questionId\":53,\"updateTime\":\"2026-01-12 09:21:09\"}],\"semester\":\"1\",\"updateBy\":\"19157727791\",\"updateTime\":\"2026-01-12 09:28:02\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-12 09:28:02', 41);
INSERT INTO `sys_oper_log` VALUES (421, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"5班\"],\"grade\":7,\"lessonId\":21,\"lessonNum\":1,\"lessonTitle\":\"第一课\",\"params\":{},\"questions\":[{\"lessonId\":21,\"orderNum\":1,\"params\":{},\"questionContent\":\"一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。\",\"questionId\":43,\"questionScore\":10,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":189},{\"answer\":\"A\",\"lessonId\":21,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":2,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":15,\"questionScore\":10,\"questionType\":\"choice\"},{\"answer\":\"F\",\"lessonId\":21,\"optionA\":\"\",\"optionB\":\"\",\"optionC\":\"\",\"optionD\":\"\",\"orderNum\":3,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":29,\"questionScore\":10,\"questionType\":\"judgment\"},{\"filePath\":\"/profile/upload/2026/01/08/111_20260108085453A001.docx\",\"lessonId\":21,\"orderNum\":4,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/08/111_20260108085453A001.pdf\",\"questionContent\":\"操作18\",\"questionId\":51,\"questionScore\":70,\"questionType\":\"practical\"}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"5班\"],\"grade\":7,\"lessonId\":21,\"lessonNum\":1,\"lessonTitle\":\"第一课\",\"params\":{},\"questions\":[{\"lessonId\":21,\"orderNum\":1,\"params\":{},\"questionContent\":\"一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。\",\"questionId\":43,\"questionScore\":10,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":189},{\"answer\":\"A\",\"lessonId\":21,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":2,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":15,\"questionScore\":10,\"questionType\":\"choice\"},{\"answer\":\"F\",\"lessonId\":21,\"orderNum\":3,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":29,\"questionScore\":10,\"questionType\":\"judgment\"},{\"filePath\":\"/profile/upload/2026/01/08/111_20260108085453A001.docx\",\"lessonId\":21,\"orderNum\":4,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/08/111_20260108085453A001.pdf\",\"questionContent\":\"操作18\",\"questionId\":51,\"questionScore\":70,\"questionType\":\"practical\"}],\"semester\":\"0\"}}', 0, NULL, '2026-01-12 09:32:33', 69);
INSERT INTO `sys_oper_log` VALUES (422, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大目湾学校(初中部)', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\"],\"grade\":7,\"lessonId\":22,\"lessonNum\":2,\"lessonTitle\":\"第二\",\"params\":{},\"questions\":[{\"answer\":\"A\",\"lessonId\":22,\"optionA\":\"a\",\"optionB\":\"b\",\"optionC\":\"cv\",\"optionD\":\"d\",\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":5,\"questionType\":\"choice\"},{\"lessonId\":22,\"orderNum\":2,\"params\":{},\"questionContent\":\"一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。\",\"questionId\":43,\"questionScore\":50,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":189},{\"answer\":\"F\",\"optionA\":\"\",\"optionB\":\"\",\"optionC\":\"\",\"optionD\":\"\",\"orderNum\":3,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":14,\"questionScore\":5,\"questionType\":\"judgment\"},{\"answer\":\"A\",\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":4,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":15,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"A\",\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":5,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":18,\"questionScore\":5,\"questionType\":\"choice\"}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\"],\"grade\":7,\"lessonId\":22,\"lessonNum\":2,\"lessonTitle\":\"第二\",\"params\":{},\"questions\":[{\"answer\":\"A\",\"lessonId\":22,\"optionA\":\"a\",\"optionB\":\"b\",\"optionC\":\"cv\",\"optionD\":\"d\",\"orderNum\":1,\"params\":{},\"questionContent\":\"选择题\",\"questionId\":9,\"questionScore\":5,\"questionType\":\"choice\"},{\"lessonId\":22,\"orderNum\":2,\"params\":{},\"questionContent\":\"一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了若依管理系统，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。\",\"questionId\":43,\"questionScore\":50,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":189},{\"answer\":\"F\",\"orderNum\":3,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":14,\"questionScore\":5,\"questionType\":\"judgment\"},{\"answer\":\"A\",\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":4,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":15,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"A\",\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":5,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":18,\"questionScore\":5,\"questionType\":\"choice\"}],\"semester\":\"0\"}}', 0, NULL, '2026-01-12 10:52:58', 228);
INSERT INTO `sys_oper_log` VALUES (423, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, 'admin', '高中', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"avatar\":\"\",\"createBy\":\"admin\",\"createTime\":\"2025-06-25 10:28:28\",\"delFlag\":\"0\",\"dept\":{\"ancestors\":\"0,100,102\",\"children\":[],\"deptId\":169,\"deptName\":\"大目湾学校(初中部)\",\"orderNum\":0,\"params\":{},\"parentId\":102,\"status\":\"0\"},\"deptId\":107,\"deptIds\":[107,169,139],\"email\":\"\",\"loginDate\":\"2026-01-12 10:05:59\",\"loginIp\":\"127.0.0.1\",\"nickName\":\"郑东旭\",\"params\":{},\"phonenumber\":\"\",\"postIds\":[],\"pwdUpdateDate\":\"2025-07-02 10:14:56\",\"roleIds\":[100],\"roles\":[{\"admin\":false,\"dataScope\":\"1\",\"deptCheckStrictly\":false,\"flag\":false,\"menuCheckStrictly\":false,\"params\":{},\"roleId\":100,\"roleKey\":\"teacher\",\"roleName\":\"教师\",\"roleSort\":0,\"status\":\"0\"}],\"schoolId\":7,\"sex\":\"0\",\"status\":\"0\",\"updateBy\":\"admin\",\"userId\":104,\"userName\":\"19157727791\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-12 12:14:50', 90);
INSERT INTO `sys_oper_log` VALUES (424, '学生管理', 6, 'com.ruoyi.business.controller.BizStudentController.importTemplate()', 'POST', 1, '19157727791', '大徐小学', '/business/student/importTemplate', '127.0.0.1', '内网IP', '{\"deptId\":\"139\"}', NULL, 0, NULL, '2026-01-12 12:15:26', 2221);
INSERT INTO `sys_oper_log` VALUES (425, '学生管理', 6, 'com.ruoyi.business.controller.BizStudentController.importData()', 'POST', 1, '19157727791', '大徐小学', '/business/student/importData', '127.0.0.1', '内网IP', '', '{\"msg\":\"恭喜您，数据已全部导入成功！共 86 条，数据如下：<br/>1、学生 陈语馨 导入成功，登录账号为 2020710701<br/>2、学生 陈语诺 导入成功，登录账号为 2020710702<br/>3、学生 程梓硕 导入成功，登录账号为 2020710703<br/>4、学生 仇熙言 导入成功，登录账号为 2020710704<br/>5、学生 丁峙宇 导入成功，登录账号为 2020710705<br/>6、学生 方韩斌 导入成功，登录账号为 2020710706<br/>7、学生 高艺嘉 导入成功，登录账号为 2020710707<br/>8、学生 何奕宸 导入成功，登录账号为 2020710708<br/>9、学生 何禹申 导入成功，登录账号为 2020710709<br/>10、学生 赖俊杰 导入成功，登录账号为 2020710710<br/>11、学生 金依娜 导入成功，登录账号为 2020710711<br/>12、学生 赖铠哲 导入成功，登录账号为 2020710712<br/>13、学生 李栩森 导入成功，登录账号为 2020710713<br/>14、学生 娄逸轩 导入成功，登录账号为 2020710714<br/>15、学生 卢妙涵 导入成功，登录账号为 2020710715<br/>16、学生 罗志超 导入成功，登录账号为 2020710716<br/>17、学生 马梓辰 导入成功，登录账号为 2020710717<br/>18、学生 潘姿妤 导入成功，登录账号为 2020710718<br/>19、学生 潘梓睿 导入成功，登录账号为 2020710719<br/>20、学生 史睿栋 导入成功，登录账号为 2020710720<br/>21、学生 王浩宸 导入成功，登录账号为 2020710721<br/>22、学生 王悦涵 导入成功，登录账号为 2020710722<br/>23、学生 吴俊霖 导入成功，登录账号为 2020710723<br/>24、学生 奚一溦 导入成功，登录账号为 2020710724<br/>25、学生 项羿 导入成功，登录账号为 2020710725<br/>26、学生 肖恩迪 导入成功，登录账号为 2020710726<br/>27、学生 徐翎轩 导入成功，登录账号为 2020710727<br/>28、学生 叶凌轩 导入成功，登录账号为 2020710728<br/>29、学生 余梓晨 导入成功，登录账号为 2020710729<br/>30、学生 俞佩婕 导入成功，登录账号为 2020710730<br/>31、学生 俞瑞希 导入成功，登录账号为 2020710731<br/>32、学生 张江涛 导入成功，登录账号为 2020710732<br/>33、学生 张珂雯 导入成功，登录账号为 2020710733<br/>34、学生 章芯桐 导入成功，登录账号为 2020710734<br/>35、学生 赵启翔 导入成功，登录账号为 2020710735<br/>36、学生 郑伊诺 导入成功，登录账号为 2020710736<br/>37、学生 郑诣凡 导入成功，登录账号为 2020710737<br/>38、学生 朱晨恺 导入成功，登录账号为 2020710738<br/>39、学生 朱欣博 导入成功，登录账号为 2020710739<br/>40、学生 朱雅楠 导入成功，登录账号为 2020710740<br/>41、学生 王营 导入成功，登录账号为 2020710741<br/>42、学生 林夕 导入成功，登录账号为 2020710742<br/>43、学生 张艺帆 导入成功，登录账号为 2020710743<br/>44、学生 曹紫云 导入成功，登录账号为 2020710401<br/>45、学生 陈芊然 导入成功，登录账号为 2020710402<br/>46、学生 陈心阳 导入成功，登录账号为 2020710403<br/>47、学生 董洛熙 导入成功，登录账号为 2020710404<br/>48、学生 董悠然 导入成功，登录账号为 2020710405<br/>49、学生 顾丁奕 导入成功，登录账号为 2020710406<br/>50、学生 韩芷若 导入成功，登录账号为 2020710407<br/>51、学生 黄博壹 导入成功，登录账号为 2020710408<br/>52、学生 黄菀宁 导入成功，登录账号为 2020710409<br/>53、学生 金致远 导入成功，登录账号为 2020710410<br/>54、学生 金子珺 导入成功，登录账号为 2020710411<br/>55、学生 柯恩哲 导入成功，登录账号为 20207104', 0, NULL, '2026-01-12 12:51:32', 8229);
INSERT INTO `sys_oper_log` VALUES (426, '教师班级管理', 1, 'com.ruoyi.business.controller.BizTeacherClassController.batchAdd()', 'POST', 1, '19157727791', '大徐小学', '/business/teacherClass/batch', '127.0.0.1', '内网IP', '[{\"classCode\":\"4\",\"createTime\":\"2026-01-12 12:51:52\",\"deptId\":139,\"entryYear\":\"2020\",\"params\":{},\"userId\":104},{\"classCode\":\"7\",\"createTime\":\"2026-01-12 12:51:52\",\"deptId\":139,\"entryYear\":\"2020\",\"params\":{},\"userId\":104}]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-12 12:51:53', 32);
INSERT INTO `sys_oper_log` VALUES (427, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, '19157727791', '大徐小学', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"19157727791\",\"createTime\":\"2026-01-12 12:55:44\",\"creatorId\":104,\"grade\":6,\"isPublic\":\"Y\",\"lessonNum\":1,\"params\":{},\"questionContent\":\"第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。\",\"questionId\":54,\"questionType\":\"typing\",\"scoringItems\":[],\"semester\":\"1\",\"typingDuration\":5,\"wordCount\":131}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-12 12:55:45', 177);
INSERT INTO `sys_oper_log` VALUES (428, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, '19157727791', '大徐小学', '/business/question', '127.0.0.1', '内网IP', '{\"createBy\":\"19157727791\",\"createTime\":\"2026-01-12 12:57:47\",\"creatorId\":104,\"filePath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.docx\",\"grade\":6,\"isPublic\":\"Y\",\"lessonNum\":1,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.pdf\",\"previewStatus\":\"pending\",\"questionContent\":\"定时灌溉和智慧农场系统的区别\",\"questionId\":55,\"questionType\":\"practical\",\"scoringItems\":[{\"itemId\":12,\"itemName\":\"答案是否正常\",\"itemScore\":100,\"orderNum\":0,\"params\":{},\"questionId\":55}],\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-12 12:57:47', 58);
INSERT INTO `sys_oper_log` VALUES (429, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, '19157727791', '大徐小学', '/business/question', '127.0.0.1', '内网IP', '{\"answer\":\"B\",\"createBy\":\"19157727791\",\"createTime\":\"2026-01-12 12:59:42\",\"creatorId\":104,\"grade\":6,\"isPublic\":\"Y\",\"lessonNum\":1,\"optionA\":\"随意性\",\"optionB\":\"有限性\",\"optionC\":\"娱乐性\",\"optionD\":\"模糊性\",\"params\":{},\"questionContent\":\"下列哪一项属于算法的基本特征\",\"questionId\":56,\"questionType\":\"choice\",\"scoringItems\":[],\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-12 12:59:42', 10);
INSERT INTO `sys_oper_log` VALUES (430, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, '19157727791', '大徐小学', '/business/question', '127.0.0.1', '内网IP', '{\"answer\":\"B\",\"createBy\":\"19157727791\",\"createTime\":\"2026-01-12 13:00:46\",\"creatorId\":104,\"grade\":6,\"isPublic\":\"Y\",\"lessonNum\":1,\"optionA\":\"排序法\",\"optionB\":\"枚举法\",\"optionC\":\"加密法\",\"optionD\":\"压缩法\",\"params\":{},\"questionContent\":\"在“鸡兔同笼”问题中，常用的算法思想是？\",\"questionId\":57,\"questionType\":\"choice\",\"scoringItems\":[],\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-12 13:00:46', 16);
INSERT INTO `sys_oper_log` VALUES (431, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, '19157727791', '大徐小学', '/business/question', '127.0.0.1', '内网IP', '{\"answer\":\"C\",\"createBy\":\"19157727791\",\"createTime\":\"2026-01-12 13:01:54\",\"creatorId\":104,\"grade\":6,\"isPublic\":\"Y\",\"lessonNum\":1,\"optionA\":\"顺序结构\",\"optionB\":\"分支结构\",\"optionC\":\"循环结构\",\"optionD\":\"选择结构\",\"params\":{},\"questionContent\":\"猜数字游戏中，用于反复让用户输入的结构是\",\"questionId\":58,\"questionType\":\"choice\",\"scoringItems\":[],\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-12 13:01:54', 9);
INSERT INTO `sys_oper_log` VALUES (432, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, '19157727791', '大徐小学', '/business/question', '127.0.0.1', '内网IP', '{\"answer\":\"F\",\"createBy\":\"19157727791\",\"createTime\":\"2026-01-12 13:02:19\",\"creatorId\":104,\"grade\":6,\"isPublic\":\"Y\",\"lessonNum\":1,\"params\":{},\"questionContent\":\"算法只能由计算机来完成。\",\"questionId\":59,\"questionType\":\"judgment\",\"scoringItems\":[],\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-12 13:02:19', 7);
INSERT INTO `sys_oper_log` VALUES (433, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, '19157727791', '大徐小学', '/business/question', '127.0.0.1', '内网IP', '{\"answer\":\"T\",\"createBy\":\"19157727791\",\"createTime\":\"2026-01-12 13:02:46\",\"creatorId\":104,\"grade\":6,\"isPublic\":\"Y\",\"lessonNum\":1,\"params\":{},\"questionContent\":\"同一个问题可能有多种算法。\",\"questionId\":60,\"questionType\":\"judgment\",\"scoringItems\":[],\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-12 13:02:46', 7);
INSERT INTO `sys_oper_log` VALUES (434, '题库管理', 1, 'com.ruoyi.business.controller.BizQuestionController.add()', 'POST', 1, '19157727791', '大徐小学', '/business/question', '127.0.0.1', '内网IP', '{\"answer\":\"T\",\"createBy\":\"19157727791\",\"createTime\":\"2026-01-12 13:03:18\",\"creatorId\":104,\"grade\":6,\"isPublic\":\"Y\",\"lessonNum\":1,\"params\":{},\"questionContent\":\"在猜数字游戏中，如果采用二分查找的方法，最多 7 次就一定能猜出 1～100 之间的任意一个数字。\",\"questionId\":61,\"questionType\":\"judgment\",\"scoringItems\":[],\"semester\":\"1\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-12 13:03:18', 14);
INSERT INTO `sys_oper_log` VALUES (435, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大徐小学', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"7班\",\"4班\"],\"grade\":6,\"lessonId\":23,\"lessonNum\":1,\"lessonTitle\":\"1-1自动控制系统\",\"params\":{},\"questions\":[{\"orderNum\":1,\"params\":{},\"questionContent\":\"第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。\",\"questionId\":54,\"questionScore\":30,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":131},{\"orderNum\":2,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.pdf\",\"questionContent\":\"定时灌溉和智慧农场系统的区别\",\"questionId\":55,\"questionScore\":30,\"questionType\":\"practical\"},{\"answer\":\"C\",\"optionA\":\"顺序结构\",\"optionB\":\"分支结构\",\"optionC\":\"循环结构\",\"optionD\":\"选择结构\",\"orderNum\":3,\"params\":{},\"questionContent\":\"猜数字游戏中，用于反复让用户输入的结构是\",\"questionId\":58,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"B\",\"optionA\":\"排序法\",\"optionB\":\"枚举法\",\"optionC\":\"加密法\",\"optionD\":\"压缩法\",\"orderNum\":4,\"params\":{},\"questionContent\":\"在“鸡兔同笼”问题中，常用的算法思想是？\",\"questionId\":57,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"B\",\"optionA\":\"随意性\",\"optionB\":\"有限性\",\"optionC\":\"娱乐性\",\"optionD\":\"模糊性\",\"orderNum\":5,\"params\":{},\"questionContent\":\"下列哪一项属于算法的基本特征\",\"questionId\":56,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"A\",\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":6,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":22,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"T\",\"orderNum\":7,\"params\":{},\"questionContent\":\"在猜数字游戏中，如果采用二分查找的方法，最多 7 次就一定能猜出 1～100 之间的任意一个数字。\",\"questionId\":61,\"questionScore\":5,\"questionType\":\"judgment\"},{\"answer\":\"T\",\"orderNum\":8,\"params\":{},\"questionContent\":\"同一个问题可能有多种算法。\",\"questionId\":60,\"questionScore\":5,\"questionType\":\"judgment\"},{\"answer\":\"F\",\"orderNum\":9,\"params\":{},\"questionContent\":\"算法只能由计算机来完成。\",\"questionId\":59,\"questionScore\":5,\"questionType\":\"judgment\"},{\"answer\":\"F\",\"optionA\":\"\",\"optionB\":\"\",\"optionC\":\"\",\"optionD\":\"\",\"orderNum\":10,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":29,\"questionScore\":5,\"questionType\":\"judgment\"', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"7班\",\"4班\"],\"grade\":6,\"lessonId\":23,\"lessonNum\":1,\"lessonTitle\":\"1-1自动控制系统\",\"params\":{},\"questions\":[{\"orderNum\":1,\"params\":{},\"questionContent\":\"第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。\",\"questionId\":54,\"questionScore\":30,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":131},{\"orderNum\":2,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.pdf\",\"questionContent\":\"定时灌溉和智慧农场系统的区别\",\"questionId\":55,\"questionScore\":30,\"questionType\":\"practical\"},{\"answer\":\"C\",\"optionA\":\"顺序结构\",\"optionB\":\"分支结构\",\"optionC\":\"循环结构\",\"optionD\":\"选择结构\",\"orderNum\":3,\"params\":{},\"questionContent\":\"猜数字游戏中，用于反复让用户输入的结构是\",\"questionId\":58,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"B\",\"optionA\":\"排序法\",\"optionB\":\"枚举法\",\"optionC\":\"加密法\",\"optionD\":\"压缩法\",\"orderNum\":4,\"params\":{},\"questionContent\":\"在“鸡兔同笼”问题中，常用的算法思想是？\",\"questionId\":57,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"B\",\"optionA\":\"随意性\",\"optionB\":\"有限性\",\"optionC\":\"娱乐性\",\"optionD\":\"模糊性\",\"orderNum\":5,\"params\":{},\"questionContent\":\"下列哪一项属于算法的基本特征\",\"questionId\":56,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"A\",\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":6,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":22,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"T\",\"orderNum\":7,\"params\":{},\"questionContent\":\"在猜数字游戏中，如果采用二分查找的方法，最多 7 次就一定能猜出 1～100 之间的任意一个数字。\",\"questionId\":61,\"questionScore\":5,\"questionType\":\"judgment\"},{\"answer\":\"T\",\"orderNum\":8,\"params\":{},\"questionContent\":\"同一个问题可能有多种算法。\",\"questionId\":60,\"questionScore\":5,\"questionType\":\"judgment\"},{\"answer\":\"F\",\"orderNum\":9,\"params\":{},\"questionContent\":\"算法只能由计算机来完成。\",\"questionId\":59,\"questionScore\":5,\"questionType\":\"judgment\"},{\"answer\":\"F\",\"orderNum\":10,\"params\":{},\"questionContent\":\"地球是平的。\",\"questionId\":29,\"questionScore\":5,\"questionType\":\"judgment\"}],\"semester\":\"0\"}}', 0, NULL, '2026-01-12 13:06:20', 45);
INSERT INTO `sys_oper_log` VALUES (436, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大徐小学', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"7班\",\"4班\"],\"grade\":6,\"lessonId\":23,\"lessonNum\":1,\"lessonTitle\":\"1-1自动控制系统发refer分\",\"params\":{},\"questions\":[{\"lessonId\":23,\"orderNum\":1,\"params\":{},\"questionContent\":\"第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。\",\"questionId\":54,\"questionScore\":30,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":131},{\"filePath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.docx\",\"lessonId\":23,\"orderNum\":2,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.pdf\",\"questionContent\":\"定时灌溉和智慧农场系统的区别\",\"questionId\":55,\"questionScore\":30,\"questionType\":\"practical\"},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"顺序结构\",\"optionB\":\"分支结构\",\"optionC\":\"循环结构\",\"optionD\":\"选择结构\",\"orderNum\":3,\"params\":{},\"questionContent\":\"猜数字游戏中，用于反复让用户输入的结构是\",\"questionId\":58,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"排序法\",\"optionB\":\"枚举法\",\"optionC\":\"加密法\",\"optionD\":\"压缩法\",\"orderNum\":4,\"params\":{},\"questionContent\":\"在“鸡兔同笼”问题中，常用的算法思想是？\",\"questionId\":57,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"随意性\",\"optionB\":\"有限性\",\"optionC\":\"娱乐性\",\"optionD\":\"模糊性\",\"orderNum\":5,\"params\":{},\"questionContent\":\"下列哪一项属于算法的基本特征\",\"questionId\":56,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"A\",\"lessonId\":23,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":6,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":22,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"T\",\"lessonId\":23,\"orderNum\":7,\"params\":{},\"questionContent\":\"在猜数字游戏中，如果采用二分查找的方法，最多 7 次就一定能猜出 1～100 之间的任意一个数字。\",\"questionId\":61,\"questionScore\":5,\"questionType\":\"judgment\"},{\"answer\":\"T\",\"lessonId\":23,\"orderNum\":8,\"params\":{},\"questionContent\":\"同一个问题可能有多种算法。\",\"questionId\":60,\"questionScore\":5,\"questionType\":\"judgment\"},{\"answer\":\"F\",\"lessonId\":23,\"orderNum\":9,\"params\":{},\"questionContent\":\"算法只能由计算机来完成。\",\"questionId\":59,\"questionScore\":5,\"que', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"7班\",\"4班\"],\"grade\":6,\"lessonId\":23,\"lessonNum\":1,\"lessonTitle\":\"1-1自动控制系统发refer分\",\"params\":{},\"questions\":[{\"lessonId\":23,\"orderNum\":1,\"params\":{},\"questionContent\":\"第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。\",\"questionId\":54,\"questionScore\":30,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":131},{\"filePath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.docx\",\"lessonId\":23,\"orderNum\":2,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.pdf\",\"questionContent\":\"定时灌溉和智慧农场系统的区别\",\"questionId\":55,\"questionScore\":30,\"questionType\":\"practical\"},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"顺序结构\",\"optionB\":\"分支结构\",\"optionC\":\"循环结构\",\"optionD\":\"选择结构\",\"orderNum\":3,\"params\":{},\"questionContent\":\"猜数字游戏中，用于反复让用户输入的结构是\",\"questionId\":58,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"排序法\",\"optionB\":\"枚举法\",\"optionC\":\"加密法\",\"optionD\":\"压缩法\",\"orderNum\":4,\"params\":{},\"questionContent\":\"在“鸡兔同笼”问题中，常用的算法思想是？\",\"questionId\":57,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"随意性\",\"optionB\":\"有限性\",\"optionC\":\"娱乐性\",\"optionD\":\"模糊性\",\"orderNum\":5,\"params\":{},\"questionContent\":\"下列哪一项属于算法的基本特征\",\"questionId\":56,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"A\",\"lessonId\":23,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":6,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":22,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"T\",\"lessonId\":23,\"orderNum\":7,\"params\":{},\"questionContent\":\"在猜数字游戏中，如果采用二分查找的方法，最多 7 次就一定能猜出 1～100 之间的任意一个数字。\",\"questionId\":61,\"questionScore\":5,\"questionType\":\"judgment\"},{\"answer\":\"T\",\"lessonId\":23,\"orderNum\":8,\"params\":{},\"questionContent\":\"同一个问题可能有多种算法。\",\"questionId\":60,\"questionScore\":5,\"questionType\":\"judgment\"},{\"answer\":\"F\",\"lessonId\":23,\"orderNum\":9,\"params\":{},\"questionContent\":\"算法只能由计算机来完成。\",\"quest', 0, NULL, '2026-01-12 13:06:46', 31);
INSERT INTO `sys_oper_log` VALUES (437, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大徐小学', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"7班\",\"4班\"],\"grade\":6,\"lessonId\":23,\"lessonNum\":1,\"lessonTitle\":\"1-1自动控制系统\",\"params\":{},\"questions\":[{\"lessonId\":23,\"orderNum\":1,\"params\":{},\"questionContent\":\"第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。\",\"questionId\":54,\"questionScore\":30,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":131},{\"filePath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.docx\",\"lessonId\":23,\"orderNum\":2,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.pdf\",\"questionContent\":\"定时灌溉和智慧农场系统的区别\",\"questionId\":55,\"questionScore\":30,\"questionType\":\"practical\"},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"顺序结构\",\"optionB\":\"分支结构\",\"optionC\":\"循环结构\",\"optionD\":\"选择结构\",\"orderNum\":3,\"params\":{},\"questionContent\":\"猜数字游戏中，用于反复让用户输入的结构是\",\"questionId\":58,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"排序法\",\"optionB\":\"枚举法\",\"optionC\":\"加密法\",\"optionD\":\"压缩法\",\"orderNum\":4,\"params\":{},\"questionContent\":\"在“鸡兔同笼”问题中，常用的算法思想是？\",\"questionId\":57,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"随意性\",\"optionB\":\"有限性\",\"optionC\":\"娱乐性\",\"optionD\":\"模糊性\",\"orderNum\":5,\"params\":{},\"questionContent\":\"下列哪一项属于算法的基本特征\",\"questionId\":56,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"A\",\"lessonId\":23,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":6,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":22,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"T\",\"lessonId\":23,\"orderNum\":7,\"params\":{},\"questionContent\":\"在猜数字游戏中，如果采用二分查找的方法，最多 7 次就一定能猜出 1～100 之间的任意一个数字。\",\"questionId\":61,\"questionScore\":5,\"questionType\":\"judgment\"},{\"answer\":\"T\",\"lessonId\":23,\"orderNum\":8,\"params\":{},\"questionContent\":\"同一个问题可能有多种算法。\",\"questionId\":60,\"questionScore\":5,\"questionType\":\"judgment\"},{\"answer\":\"F\",\"lessonId\":23,\"orderNum\":9,\"params\":{},\"questionContent\":\"算法只能由计算机来完成。\",\"questionId\":59,\"questionScore\":5,\"questionTy', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"7班\",\"4班\"],\"grade\":6,\"lessonId\":23,\"lessonNum\":1,\"lessonTitle\":\"1-1自动控制系统\",\"params\":{},\"questions\":[{\"lessonId\":23,\"orderNum\":1,\"params\":{},\"questionContent\":\"第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。\",\"questionId\":54,\"questionScore\":30,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":131},{\"filePath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.docx\",\"lessonId\":23,\"orderNum\":2,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.pdf\",\"questionContent\":\"定时灌溉和智慧农场系统的区别\",\"questionId\":55,\"questionScore\":30,\"questionType\":\"practical\"},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"顺序结构\",\"optionB\":\"分支结构\",\"optionC\":\"循环结构\",\"optionD\":\"选择结构\",\"orderNum\":3,\"params\":{},\"questionContent\":\"猜数字游戏中，用于反复让用户输入的结构是\",\"questionId\":58,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"排序法\",\"optionB\":\"枚举法\",\"optionC\":\"加密法\",\"optionD\":\"压缩法\",\"orderNum\":4,\"params\":{},\"questionContent\":\"在“鸡兔同笼”问题中，常用的算法思想是？\",\"questionId\":57,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"随意性\",\"optionB\":\"有限性\",\"optionC\":\"娱乐性\",\"optionD\":\"模糊性\",\"orderNum\":5,\"params\":{},\"questionContent\":\"下列哪一项属于算法的基本特征\",\"questionId\":56,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"A\",\"lessonId\":23,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":6,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":22,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"T\",\"lessonId\":23,\"orderNum\":7,\"params\":{},\"questionContent\":\"在猜数字游戏中，如果采用二分查找的方法，最多 7 次就一定能猜出 1～100 之间的任意一个数字。\",\"questionId\":61,\"questionScore\":5,\"questionType\":\"judgment\"},{\"answer\":\"T\",\"lessonId\":23,\"orderNum\":8,\"params\":{},\"questionContent\":\"同一个问题可能有多种算法。\",\"questionId\":60,\"questionScore\":5,\"questionType\":\"judgment\"},{\"answer\":\"F\",\"lessonId\":23,\"orderNum\":9,\"params\":{},\"questionContent\":\"算法只能由计算机来完成。\",\"questionId\":', 0, NULL, '2026-01-12 13:06:55', 32);
INSERT INTO `sys_oper_log` VALUES (438, '学生管理', 2, 'com.ruoyi.business.controller.BizStudentController.edit()', 'PUT', 1, '19157727791', '大徐小学', '/business/student', '127.0.0.1', '内网IP', '{\"classCode\":\"4\",\"entryYear\":\"2020\",\"params\":{},\"studentId\":110,\"studentName\":\"顾丁奕\",\"studentNo\":\"7\",\"userId\":216,\"userName\":\"2020710406\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-12 13:47:32', 16);
INSERT INTO `sys_oper_log` VALUES (439, '学生管理', 2, 'com.ruoyi.business.controller.BizStudentController.edit()', 'PUT', 1, '19157727791', '大徐小学', '/business/student', '127.0.0.1', '内网IP', '{\"classCode\":\"4\",\"entryYear\":\"2020\",\"params\":{},\"studentId\":110,\"studentName\":\"顾丁奕\",\"studentNo\":\"6\",\"userId\":216,\"userName\":\"2020710406\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-12 13:47:46', 8);
INSERT INTO `sys_oper_log` VALUES (440, '学生管理', 3, 'com.ruoyi.business.controller.BizStudentController.remove()', 'DELETE', 1, '19157727791', '大徐小学', '/business/student/105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,142,143,144,145,146,147', '127.0.0.1', '内网IP', '[105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,142,143,144,145,146,147]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-12 14:11:23', 395);
INSERT INTO `sys_oper_log` VALUES (441, '学生管理', 6, 'com.ruoyi.business.controller.BizStudentController.importData()', 'POST', 1, '19157727791', '大徐小学', '/business/student/importData', '127.0.0.1', '内网IP', '', '{\"msg\":\"恭喜您，数据已全部导入成功！共 43 条，数据如下：<br/>1、学生 曹紫云 导入成功，登录账号为 2020710401<br/>2、学生 陈芊然 导入成功，登录账号为 2020710402<br/>3、学生 陈心阳 导入成功，登录账号为 2020710403<br/>4、学生 董洛熙 导入成功，登录账号为 2020710404<br/>5、学生 董悠然 导入成功，登录账号为 2020710405<br/>6、学生 顾丁奕 导入成功，登录账号为 2020710407<br/>7、学生 韩芷若 导入成功，登录账号为 2020710408<br/>8、学生 黄博壹 导入成功，登录账号为 2020710409<br/>9、学生 黄菀宁 导入成功，登录账号为 2020710410<br/>10、学生 金致远 导入成功，登录账号为 2020710411<br/>11、学生 金子珺 导入成功，登录账号为 2020710412<br/>12、学生 柯恩哲 导入成功，登录账号为 2020710413<br/>13、学生 赖佳彬 导入成功，登录账号为 2020710414<br/>14、学生 李尚恩 导入成功，登录账号为 2020710415<br/>15、学生 李妍儿 导入成功，登录账号为 2020710416<br/>16、学生 励宸煜 导入成功，登录账号为 2020710417<br/>17、学生 林鑫博 导入成功，登录账号为 2020710418<br/>18、学生 林意涵 导入成功，登录账号为 2020710419<br/>19、学生 刘玟言 导入成功，登录账号为 2020710420<br/>20、学生 陆施羽 导入成功，登录账号为 2020710421<br/>21、学生 马韶怿 导入成功，登录账号为 2020710422<br/>22、学生 孟小博 导入成功，登录账号为 2020710423<br/>23、学生 邵嘉翔 导入成功，登录账号为 2020710424<br/>24、学生 王子瑜 导入成功，登录账号为 2020710425<br/>25、学生 吴鼎轩 导入成功，登录账号为 2020710426<br/>26、学生 谢佳芯 导入成功，登录账号为 2020710427<br/>27、学生 徐辰若 导入成功，登录账号为 2020710428<br/>28、学生 余昊璟 导入成功，登录账号为 2020710429<br/>29、学生 张晨曦 导入成功，登录账号为 2020710431<br/>30、学生 张画 导入成功，登录账号为 2020710432<br/>31、学生 张天佑 导入成功，登录账号为 2020710433<br/>32、学生 张桐瑶 导入成功，登录账号为 2020710434<br/>33、学生 戴梓悦 导入成功，登录账号为 2020710435<br/>34、学生 张梓铭 导入成功，登录账号为 2020710436<br/>35、学生 郑若秋 导入成功，登录账号为 2020710437<br/>36、学生 周宸乐 导入成功，登录账号为 2020710438<br/>37、学生 周镇宇 导入成功，登录账号为 2020710439<br/>38、学生 朱优璇 导入成功，登录账号为 2020710440<br/>39、学生 李君航 导入成功，登录账号为 2020710441<br/>40、学生 吴恪 导入成功，登录账号为 2020710430<br/>41、学生 郑凯瑞 导入成功，登录账号为 2020710406<br/>42、学生 朱宸妍 导入成功，登录账号为 2020710443<br/>43、学生 杨淦淳 导入成功，登录账号为 2020710442\",\"code\":200}', 0, NULL, '2026-01-12 14:11:57', 3421);
INSERT INTO `sys_oper_log` VALUES (442, '学生管理', 6, 'com.ruoyi.business.controller.BizStudentController.importData()', 'POST', 1, '19157727791', '大徐小学', '/business/student/importData', '127.0.0.1', '内网IP', '', '{\"msg\":\"恭喜您，数据已全部导入成功！共 127 条，数据如下：<br/>1、学生 柴兆俊 导入成功，登录账号为 2020710201<br/>2、学生 陈增宇 导入成功，登录账号为 2020710202<br/>3、学生 陈志航 导入成功，登录账号为 2020710203<br/>4、学生 戴彦哲 导入成功，登录账号为 2020710204<br/>5、学生 方俊淇 导入成功，登录账号为 2020710205<br/>6、学生 金叙 导入成功，登录账号为 2020710206<br/>7、学生 李博文 导入成功，登录账号为 2020710207<br/>8、学生 李奕翔 导入成功，登录账号为 2020710208<br/>9、学生 李梓烁 导入成功，登录账号为 2020710209<br/>10、学生 史昊鑫 导入成功，登录账号为 2020710210<br/>11、学生 舒俊博 导入成功，登录账号为 2020710211<br/>12、学生 屠溢航 导入成功，登录账号为 2020710212<br/>13、学生 奚景贤 导入成功，登录账号为 2020710213<br/>14、学生 徐郁骁 导入成功，登录账号为 2020710214<br/>15、学生 许梓晨 导入成功，登录账号为 2020710215<br/>16、学生 郑翎恺 导入成功，登录账号为 2020710216<br/>17、学生 钟亦程 导入成功，登录账号为 2020710217<br/>18、学生 戴瑾言 导入成功，登录账号为 2020710218<br/>19、学生 陈静瑶 导入成功，登录账号为 2020710219<br/>20、学生 付姝绚 导入成功，登录账号为 2020710220<br/>21、学生 金奕萱 导入成功，登录账号为 2020710222<br/>22、学生 韩金雅 导入成功，登录账号为 2020710223<br/>23、学生 罗诗雅 导入成功，登录账号为 2020710224<br/>24、学生 沈熙涵 导入成功，登录账号为 2020710225<br/>25、学生 王晨萱 导入成功，登录账号为 2020710226<br/>26、学生 王晨雨 导入成功，登录账号为 2020710227<br/>27、学生 王晶莹 导入成功，登录账号为 2020710228<br/>28、学生 吴佳漩 导入成功，登录账号为 2020710229<br/>29、学生 项梓倚 导入成功，登录账号为 2020710230<br/>30、学生 叶梓诺 导入成功，登录账号为 2020710231<br/>31、学生 应雨铭 导入成功，登录账号为 2020710232<br/>32、学生 张嘉颖 导入成功，登录账号为 2020710233<br/>33、学生 张缈缈 导入成功，登录账号为 2020710234<br/>34、学生 章诺瞳 导入成功，登录账号为 2020710235<br/>35、学生 郑雯鑫 导入成功，登录账号为 2020710236<br/>36、学生 周柏儿 导入成功，登录账号为 2020710237<br/>37、学生 朱语晗 导入成功，登录账号为 2020710238<br/>38、学生 何萱妍 导入成功，登录账号为 2020710239<br/>39、学生 方颖滢 导入成功，登录账号为 2020710240<br/>40、学生 孙硕 导入成功，登录账号为 2020710241<br/>41、学生 赵轩逸 导入成功，登录账号为 2020710221<br/>42、学生 罗梓航 导入成功，登录账号为 2020710242<br/>43、学生 杨子轩 导入成功，登录账号为 2020710243<br/>44、学生 陈芳怡 导入成功，登录账号为 2020710101<br/>45、学生 陈灏满 导入成功，登录账号为 2020710102<br/>46、学生 陈文睿 导入成功，登录账号为 2020710103<br/>47、学生 程  诺 导入成功，登录账号为 2020710104<br/>48、学生 方曦航 导入成功，登录账号为 2020710105<br/>49、学生 葛笑瑜 导入成功，登录账号为 2020710106<br/>50、学生 何俊坤 导入成功，登录账号为 2020710107<br/>51、学生 胡芝悦 导入成功，登录账号为 2020710108<br/>52、学生 黄  硕 导入成功，登录账号为 2020710109<br/>53、学生 江思悯 导入成功，登录账号为 2020710110<br/>54、学生 蒋语诺 导入成功，登录账号为 2020710111<br/>55、学生 金子茵 导入成功，登录账号为 2020', 0, NULL, '2026-01-12 14:21:24', 9914);
INSERT INTO `sys_oper_log` VALUES (443, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大徐小学', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"7班\",\"4班\"],\"grade\":6,\"lessonId\":23,\"lessonNum\":1,\"lessonTitle\":\"1-1自动控制系统\",\"params\":{},\"questions\":[{\"lessonId\":23,\"orderNum\":1,\"params\":{},\"questionContent\":\"第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。\",\"questionId\":54,\"questionScore\":30,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":131},{\"filePath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.docx\",\"lessonId\":23,\"orderNum\":2,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.pdf\",\"questionContent\":\"定时灌溉和智慧农场系统的区别\",\"questionId\":55,\"questionScore\":30,\"questionType\":\"practical\"},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"顺序结构\",\"optionB\":\"分支结构\",\"optionC\":\"循环结构\",\"optionD\":\"选择结构\",\"orderNum\":3,\"params\":{},\"questionContent\":\"猜数字游戏中，用于反复让用户输入的结构是\",\"questionId\":58,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"排序法\",\"optionB\":\"枚举法\",\"optionC\":\"加密法\",\"optionD\":\"压缩法\",\"orderNum\":4,\"params\":{},\"questionContent\":\"在“鸡兔同笼”问题中，常用的算法思想是？\",\"questionId\":57,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"随意性\",\"optionB\":\"有限性\",\"optionC\":\"娱乐性\",\"optionD\":\"模糊性\",\"orderNum\":5,\"params\":{},\"questionContent\":\"下列哪一项属于算法的基本特征\",\"questionId\":56,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"A\",\"lessonId\":23,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":6,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":22,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"T\",\"lessonId\":23,\"orderNum\":7,\"params\":{},\"questionContent\":\"在猜数字游戏中，如果采用二分查找的方法，最多 7 次就一定能猜出 1～100 之间的任意一个数字。\",\"questionId\":61,\"questionScore\":5,\"questionType\":\"judgment\"},{\"answer\":\"T\",\"lessonId\":23,\"orderNum\":8,\"params\":{},\"questionContent\":\"同一个问题可能有多种算法。\",\"questionId\":60,\"questionScore\":5,\"questionType\":\"judgment\"},{\"answer\":\"F\",\"lessonId\":23,\"orderNum\":9,\"params\":{},\"questionContent\":\"算法只能由计算机来完成。\",\"questionId\":59,\"questionScore\":5,\"questionTy', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"7班\",\"4班\"],\"grade\":6,\"lessonId\":23,\"lessonNum\":1,\"lessonTitle\":\"1-1自动控制系统\",\"params\":{},\"questions\":[{\"lessonId\":23,\"orderNum\":1,\"params\":{},\"questionContent\":\"第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。\",\"questionId\":54,\"questionScore\":30,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":131},{\"filePath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.docx\",\"lessonId\":23,\"orderNum\":2,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.pdf\",\"questionContent\":\"定时灌溉和智慧农场系统的区别\",\"questionId\":55,\"questionScore\":30,\"questionType\":\"practical\"},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"顺序结构\",\"optionB\":\"分支结构\",\"optionC\":\"循环结构\",\"optionD\":\"选择结构\",\"orderNum\":3,\"params\":{},\"questionContent\":\"猜数字游戏中，用于反复让用户输入的结构是\",\"questionId\":58,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"排序法\",\"optionB\":\"枚举法\",\"optionC\":\"加密法\",\"optionD\":\"压缩法\",\"orderNum\":4,\"params\":{},\"questionContent\":\"在“鸡兔同笼”问题中，常用的算法思想是？\",\"questionId\":57,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"随意性\",\"optionB\":\"有限性\",\"optionC\":\"娱乐性\",\"optionD\":\"模糊性\",\"orderNum\":5,\"params\":{},\"questionContent\":\"下列哪一项属于算法的基本特征\",\"questionId\":56,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"A\",\"lessonId\":23,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":6,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":22,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"T\",\"lessonId\":23,\"orderNum\":7,\"params\":{},\"questionContent\":\"在猜数字游戏中，如果采用二分查找的方法，最多 7 次就一定能猜出 1～100 之间的任意一个数字。\",\"questionId\":61,\"questionScore\":5,\"questionType\":\"judgment\"},{\"answer\":\"T\",\"lessonId\":23,\"orderNum\":8,\"params\":{},\"questionContent\":\"同一个问题可能有多种算法。\",\"questionId\":60,\"questionScore\":5,\"questionType\":\"judgment\"},{\"answer\":\"F\",\"lessonId\":23,\"orderNum\":9,\"params\":{},\"questionContent\":\"算法只能由计算机来完成。\",\"questionId\":', 0, NULL, '2026-01-12 14:45:17', 32);
INSERT INTO `sys_oper_log` VALUES (444, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大徐小学', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"7班\",\"4班\"],\"grade\":6,\"lessonId\":23,\"lessonNum\":1,\"lessonTitle\":\"自动控制系统\",\"params\":{},\"questions\":[{\"lessonId\":23,\"orderNum\":1,\"params\":{},\"questionContent\":\"第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。\",\"questionId\":54,\"questionScore\":30,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":131},{\"filePath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.docx\",\"lessonId\":23,\"orderNum\":2,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.pdf\",\"questionContent\":\"定时灌溉和智慧农场系统的区别\",\"questionId\":55,\"questionScore\":30,\"questionType\":\"practical\"},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"顺序结构\",\"optionB\":\"分支结构\",\"optionC\":\"循环结构\",\"optionD\":\"选择结构\",\"orderNum\":3,\"params\":{},\"questionContent\":\"猜数字游戏中，用于反复让用户输入的结构是\",\"questionId\":58,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"排序法\",\"optionB\":\"枚举法\",\"optionC\":\"加密法\",\"optionD\":\"压缩法\",\"orderNum\":4,\"params\":{},\"questionContent\":\"在“鸡兔同笼”问题中，常用的算法思想是？\",\"questionId\":57,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"随意性\",\"optionB\":\"有限性\",\"optionC\":\"娱乐性\",\"optionD\":\"模糊性\",\"orderNum\":5,\"params\":{},\"questionContent\":\"下列哪一项属于算法的基本特征\",\"questionId\":56,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"A\",\"lessonId\":23,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":6,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":22,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"T\",\"lessonId\":23,\"orderNum\":7,\"params\":{},\"questionContent\":\"在猜数字游戏中，如果采用二分查找的方法，最多 7 次就一定能猜出 1～100 之间的任意一个数字。\",\"questionId\":61,\"questionScore\":5,\"questionType\":\"judgment\"},{\"answer\":\"T\",\"lessonId\":23,\"orderNum\":8,\"params\":{},\"questionContent\":\"同一个问题可能有多种算法。\",\"questionId\":60,\"questionScore\":5,\"questionType\":\"judgment\"},{\"answer\":\"F\",\"lessonId\":23,\"orderNum\":9,\"params\":{},\"questionContent\":\"算法只能由计算机来完成。\",\"questionId\":59,\"questionScore\":5,\"questionType\"', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"7班\",\"4班\"],\"grade\":6,\"lessonId\":23,\"lessonNum\":1,\"lessonTitle\":\"自动控制系统\",\"params\":{},\"questions\":[{\"lessonId\":23,\"orderNum\":1,\"params\":{},\"questionContent\":\"第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。\",\"questionId\":54,\"questionScore\":30,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":131},{\"filePath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.docx\",\"lessonId\":23,\"orderNum\":2,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.pdf\",\"questionContent\":\"定时灌溉和智慧农场系统的区别\",\"questionId\":55,\"questionScore\":30,\"questionType\":\"practical\"},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"顺序结构\",\"optionB\":\"分支结构\",\"optionC\":\"循环结构\",\"optionD\":\"选择结构\",\"orderNum\":3,\"params\":{},\"questionContent\":\"猜数字游戏中，用于反复让用户输入的结构是\",\"questionId\":58,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"排序法\",\"optionB\":\"枚举法\",\"optionC\":\"加密法\",\"optionD\":\"压缩法\",\"orderNum\":4,\"params\":{},\"questionContent\":\"在“鸡兔同笼”问题中，常用的算法思想是？\",\"questionId\":57,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"随意性\",\"optionB\":\"有限性\",\"optionC\":\"娱乐性\",\"optionD\":\"模糊性\",\"orderNum\":5,\"params\":{},\"questionContent\":\"下列哪一项属于算法的基本特征\",\"questionId\":56,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"A\",\"lessonId\":23,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":6,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":22,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"T\",\"lessonId\":23,\"orderNum\":7,\"params\":{},\"questionContent\":\"在猜数字游戏中，如果采用二分查找的方法，最多 7 次就一定能猜出 1～100 之间的任意一个数字。\",\"questionId\":61,\"questionScore\":5,\"questionType\":\"judgment\"},{\"answer\":\"T\",\"lessonId\":23,\"orderNum\":8,\"params\":{},\"questionContent\":\"同一个问题可能有多种算法。\",\"questionId\":60,\"questionScore\":5,\"questionType\":\"judgment\"},{\"answer\":\"F\",\"lessonId\":23,\"orderNum\":9,\"params\":{},\"questionContent\":\"算法只能由计算机来完成。\",\"questionId\":59,', 0, NULL, '2026-01-12 14:45:27', 28);
INSERT INTO `sys_oper_log` VALUES (445, '教师班级管理', 1, 'com.ruoyi.business.controller.BizTeacherClassController.batchAdd()', 'POST', 1, '19157727791', '大徐小学', '/business/teacherClass/batch', '127.0.0.1', '内网IP', '[{\"classCode\":\"1\",\"createTime\":\"2026-01-12 14:47:33\",\"deptId\":139,\"entryYear\":\"2020\",\"params\":{},\"userId\":104},{\"classCode\":\"2\",\"createTime\":\"2026-01-12 14:47:33\",\"deptId\":139,\"entryYear\":\"2020\",\"params\":{},\"userId\":104},{\"classCode\":\"8\",\"createTime\":\"2026-01-12 14:47:33\",\"deptId\":139,\"entryYear\":\"2020\",\"params\":{},\"userId\":104}]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-12 14:47:33', 22);
INSERT INTO `sys_oper_log` VALUES (446, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大徐小学', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"7班\",\"4班\",\"2班\",\"1班\",\"8班\"],\"grade\":6,\"lessonId\":24,\"lessonNum\":1,\"lessonTitle\":\"自动控制系统复习\",\"params\":{},\"questions\":[{\"answer\":\"A\",\"optionA\":\"a\",\"optionB\":\"b\",\"optionC\":\"c\",\"optionD\":\"d\",\"orderNum\":1,\"params\":{},\"questionContent\":\"<p><img src=\\\"/dev-api/profile/upload/2025/08/20/4_20250820110019A001.jpg\\\"></p><p>和规范化非常</p>\",\"questionId\":5,\"questionScore\":100,\"questionType\":\"choice\"}],\"semester\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"7班\",\"4班\",\"2班\",\"1班\",\"8班\"],\"grade\":6,\"lessonId\":24,\"lessonNum\":1,\"lessonTitle\":\"自动控制系统复习\",\"params\":{},\"questions\":[{\"answer\":\"A\",\"optionA\":\"a\",\"optionB\":\"b\",\"optionC\":\"c\",\"optionD\":\"d\",\"orderNum\":1,\"params\":{},\"questionContent\":\"<p><img src=\\\"/dev-api/profile/upload/2025/08/20/4_20250820110019A001.jpg\\\"></p><p>和规范化非常</p>\",\"questionId\":5,\"questionScore\":100,\"questionType\":\"choice\"}],\"semester\":\"0\"}}', 0, NULL, '2026-01-12 14:48:16', 36);
INSERT INTO `sys_oper_log` VALUES (447, '菜单管理', 1, 'com.ruoyi.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '高中', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"createBy\":\"admin\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuName\":\"题目导入\",\"menuType\":\"F\",\"orderNum\":5,\"params\":{},\"parentId\":2031,\"perms\":\"business:question:import\",\"status\":\"0\",\"visible\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-12 15:08:46', 52);
INSERT INTO `sys_oper_log` VALUES (448, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '高中', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"component\":\"\",\"createTime\":\"2025-08-18 20:03:35\",\"icon\":\"#\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2036,\"menuName\":\"题库管理导出\",\"menuType\":\"F\",\"orderNum\":6,\"params\":{},\"parentId\":2031,\"path\":\"#\",\"perms\":\"business:question:export\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-12 15:08:57', 21);
INSERT INTO `sys_oper_log` VALUES (449, '角色管理', 2, 'com.ruoyi.web.controller.system.SysRoleController.edit()', 'PUT', 1, 'admin', '高中', '/system/role', '127.0.0.1', '内网IP', '{\"admin\":false,\"createTime\":\"2025-06-17 18:39:24\",\"dataScope\":\"1\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[2037,1,100,1000,1001,1002,1003,1004,1005,1006,101,1007,1008,1009,1010,1011,102,1012,1013,1014,1015,103,1016,1017,1018,1019,104,1020,1021,1022,1023,1024,105,1025,1026,1027,1028,1029,106,1030,1031,1032,1033,1034,107,1035,1036,1037,1038,108,500,1039,1040,1041,501,1042,1043,1044,1045,2018,2019,2020,2021,2022,2023,2024,2031,2032,2033,2034,2035,2043,2036,2,109,1046,1047,1048,110,1049,1050,1051,1052,1053,1054,111,112,113,114,2006,2007,2008,2009,2010,2011,3,115,116,1055,1056,1057,1058,1059,1060,117,4,2038,2039,2040,2042,2026,2027,2028,2029,2030],\"params\":{},\"roleId\":100,\"roleKey\":\"teacher\",\"roleName\":\"教师\",\"roleSort\":0,\"status\":\"0\",\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-12 15:09:08', 70);
INSERT INTO `sys_oper_log` VALUES (450, '题库管理', 6, 'com.ruoyi.business.controller.BizQuestionController.importData()', 'POST', 1, '19157727791', '大徐小学', '/business/question/importData', '127.0.0.1', '内网IP', '{\"updateSupport\":\"0\"}', '{\"msg\":\"恭喜您，数据已全部导入成功！共 6 条，数据如下：<br/>1、题目 【选择题】中国的首都是哪里？... 导入成功<br/>2、题目 下列哪个不是编程语言？... 导入成功<br/>3、题目 【判断题】计算机病毒可以通过网... 导入成功<br/>4、题目 CPU是计算机的存储器。... 导入成功<br/>5、题目 【打字题】春眠不觉晓，处处闻啼... 导入成功<br/>6、题目 信息技术是指利用计算机和网络对... 导入成功\",\"code\":200}', 0, NULL, '2026-01-12 15:32:59', 799);
INSERT INTO `sys_oper_log` VALUES (451, '角色管理', 2, 'com.ruoyi.web.controller.system.SysRoleController.edit()', 'PUT', 1, 'admin', '高中', '/system/role', '127.0.0.1', '内网IP', '{\"admin\":false,\"createTime\":\"2025-06-17 18:39:24\",\"dataScope\":\"1\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[2037,2018,2019,2020,2021,2022,2023,2024,2031,2032,2033,2034,2035,2043,2036,2006,2007,2008,2009,2010,2011,2038,2039,2040,2042,2026,2027,2028,2029,2030],\"params\":{},\"roleId\":100,\"roleKey\":\"teacher\",\"roleName\":\"教师\",\"roleSort\":0,\"status\":\"0\",\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-13 08:53:54', 128);
INSERT INTO `sys_oper_log` VALUES (452, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大徐小学', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\",\"2班\",\"4班\",\"7班\",\"8班\"],\"grade\":6,\"lessonId\":23,\"lessonNum\":1,\"lessonTitle\":\"自动控制系统\",\"params\":{},\"questions\":[{\"lessonId\":23,\"orderNum\":1,\"params\":{},\"questionContent\":\"第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。\",\"questionId\":54,\"questionScore\":40,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":131},{\"filePath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.docx\",\"lessonId\":23,\"orderNum\":2,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.pdf\",\"questionContent\":\"定时灌溉和智慧农场系统的区别\",\"questionId\":55,\"questionScore\":30,\"questionType\":\"practical\"},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"顺序结构\",\"optionB\":\"分支结构\",\"optionC\":\"循环结构\",\"optionD\":\"选择结构\",\"orderNum\":3,\"params\":{},\"questionContent\":\"猜数字游戏中，用于反复让用户输入的结构是\",\"questionId\":58,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"排序法\",\"optionB\":\"枚举法\",\"optionC\":\"加密法\",\"optionD\":\"压缩法\",\"orderNum\":4,\"params\":{},\"questionContent\":\"在“鸡兔同笼”问题中，常用的算法思想是？\",\"questionId\":57,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"随意性\",\"optionB\":\"有限性\",\"optionC\":\"娱乐性\",\"optionD\":\"模糊性\",\"orderNum\":5,\"params\":{},\"questionContent\":\"下列哪一项属于算法的基本特征\",\"questionId\":56,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"A\",\"lessonId\":23,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":6,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":22,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"C\",\"optionA\":\"Python\",\"optionB\":\"Java\",\"optionC\":\"Word\",\"optionD\":\"Scratch\",\"orderNum\":7,\"params\":{},\"questionContent\":\"下列哪个不是编程语言？\",\"questionId\":63,\"questionScore\":10,\"questionType\":\"choice\"},{\"answer\":\"T\",\"lessonId\":23,\"orderNum\":8,\"params\":{},\"questionContent\":\"在猜数字游戏中，如果采用二分查找的方法，最多 7 次就一定能猜出 1～100 之间的任意一个数字。\",\"questionId\":61,\"questionScore\":5,\"questionType\":\"judgment\"},{\"answer\":\"T\",\"lessonId\":23,\"orderNum\":9,\"params\":{},\"questionC', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\",\"2班\",\"4班\",\"7班\",\"8班\"],\"grade\":6,\"lessonId\":23,\"lessonNum\":1,\"lessonTitle\":\"自动控制系统\",\"params\":{},\"questions\":[{\"lessonId\":23,\"orderNum\":1,\"params\":{},\"questionContent\":\"第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。\",\"questionId\":54,\"questionScore\":40,\"questionType\":\"typing\",\"typingDuration\":5,\"wordCount\":131},{\"filePath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.docx\",\"lessonId\":23,\"orderNum\":2,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.pdf\",\"questionContent\":\"定时灌溉和智慧农场系统的区别\",\"questionId\":55,\"questionScore\":30,\"questionType\":\"practical\"},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"顺序结构\",\"optionB\":\"分支结构\",\"optionC\":\"循环结构\",\"optionD\":\"选择结构\",\"orderNum\":3,\"params\":{},\"questionContent\":\"猜数字游戏中，用于反复让用户输入的结构是\",\"questionId\":58,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"排序法\",\"optionB\":\"枚举法\",\"optionC\":\"加密法\",\"optionD\":\"压缩法\",\"orderNum\":4,\"params\":{},\"questionContent\":\"在“鸡兔同笼”问题中，常用的算法思想是？\",\"questionId\":57,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"随意性\",\"optionB\":\"有限性\",\"optionC\":\"娱乐性\",\"optionD\":\"模糊性\",\"orderNum\":5,\"params\":{},\"questionContent\":\"下列哪一项属于算法的基本特征\",\"questionId\":56,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"A\",\"lessonId\":23,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":6,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":22,\"questionScore\":5,\"questionType\":\"choice\"},{\"answer\":\"C\",\"optionA\":\"Python\",\"optionB\":\"Java\",\"optionC\":\"Word\",\"optionD\":\"Scratch\",\"orderNum\":7,\"params\":{},\"questionContent\":\"下列哪个不是编程语言？\",\"questionId\":63,\"questionScore\":10,\"questionType\":\"choice\"},{\"answer\":\"T\",\"lessonId\":23,\"orderNum\":8,\"params\":{},\"questionContent\":\"在猜数字游戏中，如果采用二分查找的方法，最多 7 次就一定能猜出 1～100 之间的任意一个数字。\",\"questionId\":61,\"questionScore\":5,\"questionType\":\"judgment\"},{\"answer\":\"T\",\"lessonId\":23,\"or', 0, NULL, '2026-01-13 09:21:14', 153);
INSERT INTO `sys_oper_log` VALUES (453, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大徐小学', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\",\"2班\",\"4班\",\"7班\",\"8班\"],\"grade\":6,\"lessonId\":23,\"lessonNum\":1,\"lessonTitle\":\"自动控制系统\",\"params\":{},\"questions\":[{\"lessonId\":23,\"orderNum\":1,\"params\":{},\"questionContent\":\"第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。\",\"questionId\":54,\"questionScore\":40,\"questionType\":\"typing\",\"scoringItems\":[],\"typingDuration\":5,\"wordCount\":131},{\"filePath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.docx\",\"lessonId\":23,\"orderNum\":2,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.pdf\",\"questionContent\":\"定时灌溉和智慧农场系统的区别\",\"questionId\":55,\"questionScore\":30,\"questionType\":\"practical\",\"scoringItems\":[null]},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"顺序结构\",\"optionB\":\"分支结构\",\"optionC\":\"循环结构\",\"optionD\":\"选择结构\",\"orderNum\":3,\"params\":{},\"questionContent\":\"猜数字游戏中，用于反复让用户输入的结构是\",\"questionId\":58,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"排序法\",\"optionB\":\"枚举法\",\"optionC\":\"加密法\",\"optionD\":\"压缩法\",\"orderNum\":4,\"params\":{},\"questionContent\":\"在“鸡兔同笼”问题中，常用的算法思想是？\",\"questionId\":57,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"随意性\",\"optionB\":\"有限性\",\"optionC\":\"娱乐性\",\"optionD\":\"模糊性\",\"orderNum\":5,\"params\":{},\"questionContent\":\"下列哪一项属于算法的基本特征\",\"questionId\":56,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"A\",\"lessonId\":23,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":6,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":22,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"Python\",\"optionB\":\"Java\",\"optionC\":\"Word\",\"optionD\":\"Scratch\",\"orderNum\":7,\"params\":{},\"questionContent\":\"下列哪个不是编程语言？\",\"questionId\":63,\"questionScore\":10,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"T\",\"lessonId\":23,\"orderNum\":8,\"params\":{},\"questionContent\":\"在猜数字游戏中，如果采用二分查找的方法，最多 7 次就一定能猜', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\",\"2班\",\"4班\",\"7班\",\"8班\"],\"grade\":6,\"lessonId\":23,\"lessonNum\":1,\"lessonTitle\":\"自动控制系统\",\"params\":{},\"questions\":[{\"lessonId\":23,\"orderNum\":1,\"params\":{},\"questionContent\":\"第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。\",\"questionId\":54,\"questionScore\":40,\"questionType\":\"typing\",\"scoringItems\":[],\"typingDuration\":5,\"wordCount\":131},{\"filePath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.docx\",\"lessonId\":23,\"orderNum\":2,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.pdf\",\"questionContent\":\"定时灌溉和智慧农场系统的区别\",\"questionId\":55,\"questionScore\":30,\"questionType\":\"practical\",\"scoringItems\":[null]},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"顺序结构\",\"optionB\":\"分支结构\",\"optionC\":\"循环结构\",\"optionD\":\"选择结构\",\"orderNum\":3,\"params\":{},\"questionContent\":\"猜数字游戏中，用于反复让用户输入的结构是\",\"questionId\":58,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"排序法\",\"optionB\":\"枚举法\",\"optionC\":\"加密法\",\"optionD\":\"压缩法\",\"orderNum\":4,\"params\":{},\"questionContent\":\"在“鸡兔同笼”问题中，常用的算法思想是？\",\"questionId\":57,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"随意性\",\"optionB\":\"有限性\",\"optionC\":\"娱乐性\",\"optionD\":\"模糊性\",\"orderNum\":5,\"params\":{},\"questionContent\":\"下列哪一项属于算法的基本特征\",\"questionId\":56,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"A\",\"lessonId\":23,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":6,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":22,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"Python\",\"optionB\":\"Java\",\"optionC\":\"Word\",\"optionD\":\"Scratch\",\"orderNum\":7,\"params\":{},\"questionContent\":\"下列哪个不是编程语言？\",\"questionId\":63,\"questionScore\":10,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"T\",\"lessonId\":23,\"orderNum\":8,\"params\":{},\"questionContent\":', 0, NULL, '2026-01-13 11:19:02', 93);
INSERT INTO `sys_oper_log` VALUES (454, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大徐小学', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\",\"2班\",\"4班\",\"7班\",\"8班\"],\"grade\":6,\"lessonId\":23,\"lessonNum\":1,\"lessonTitle\":\"自动控制系统\",\"params\":{},\"questions\":[{\"lessonId\":23,\"orderNum\":1,\"params\":{},\"questionContent\":\"第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。\",\"questionId\":54,\"questionScore\":40,\"questionType\":\"typing\",\"scoringItems\":[],\"typingDuration\":5,\"wordCount\":131},{\"filePath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.docx\",\"lessonId\":23,\"orderNum\":2,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.pdf\",\"questionContent\":\"定时灌溉和智慧农场系统的区别\",\"questionId\":55,\"questionScore\":30,\"questionType\":\"practical\",\"scoringItems\":[null]},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"顺序结构\",\"optionB\":\"分支结构\",\"optionC\":\"循环结构\",\"optionD\":\"选择结构\",\"orderNum\":3,\"params\":{},\"questionContent\":\"猜数字游戏中，用于反复让用户输入的结构是\",\"questionId\":58,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"排序法\",\"optionB\":\"枚举法\",\"optionC\":\"加密法\",\"optionD\":\"压缩法\",\"orderNum\":4,\"params\":{},\"questionContent\":\"在“鸡兔同笼”问题中，常用的算法思想是？\",\"questionId\":57,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"随意性\",\"optionB\":\"有限性\",\"optionC\":\"娱乐性\",\"optionD\":\"模糊性\",\"orderNum\":5,\"params\":{},\"questionContent\":\"下列哪一项属于算法的基本特征\",\"questionId\":56,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"A\",\"lessonId\":23,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":6,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":22,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"Python\",\"optionB\":\"Java\",\"optionC\":\"Word\",\"optionD\":\"Scratch\",\"orderNum\":7,\"params\":{},\"questionContent\":\"下列哪个不是编程语言？\",\"questionId\":63,\"questionScore\":10,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"T\",\"lessonId\":23,\"orderNum\":8,\"params\":{},\"questionContent\":\"在猜数字游戏中，如果采用二分查找的方法，最多 7 次就一定能猜', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\",\"2班\",\"4班\",\"7班\",\"8班\"],\"grade\":6,\"lessonId\":23,\"lessonNum\":1,\"lessonTitle\":\"自动控制系统\",\"params\":{},\"questions\":[{\"lessonId\":23,\"orderNum\":1,\"params\":{},\"questionContent\":\"第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。\",\"questionId\":54,\"questionScore\":40,\"questionType\":\"typing\",\"scoringItems\":[],\"typingDuration\":5,\"wordCount\":131},{\"filePath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.docx\",\"lessonId\":23,\"orderNum\":2,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.pdf\",\"questionContent\":\"定时灌溉和智慧农场系统的区别\",\"questionId\":55,\"questionScore\":30,\"questionType\":\"practical\",\"scoringItems\":[null]},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"顺序结构\",\"optionB\":\"分支结构\",\"optionC\":\"循环结构\",\"optionD\":\"选择结构\",\"orderNum\":3,\"params\":{},\"questionContent\":\"猜数字游戏中，用于反复让用户输入的结构是\",\"questionId\":58,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"排序法\",\"optionB\":\"枚举法\",\"optionC\":\"加密法\",\"optionD\":\"压缩法\",\"orderNum\":4,\"params\":{},\"questionContent\":\"在“鸡兔同笼”问题中，常用的算法思想是？\",\"questionId\":57,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"随意性\",\"optionB\":\"有限性\",\"optionC\":\"娱乐性\",\"optionD\":\"模糊性\",\"orderNum\":5,\"params\":{},\"questionContent\":\"下列哪一项属于算法的基本特征\",\"questionId\":56,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"A\",\"lessonId\":23,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":6,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":22,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"Python\",\"optionB\":\"Java\",\"optionC\":\"Word\",\"optionD\":\"Scratch\",\"orderNum\":7,\"params\":{},\"questionContent\":\"下列哪个不是编程语言？\",\"questionId\":63,\"questionScore\":10,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"T\",\"lessonId\":23,\"orderNum\":8,\"params\":{},\"questionContent\":', 0, NULL, '2026-01-13 11:25:08', 58);
INSERT INTO `sys_oper_log` VALUES (455, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大徐小学', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\",\"2班\",\"4班\",\"7班\",\"8班\"],\"grade\":6,\"lessonId\":23,\"lessonNum\":1,\"lessonTitle\":\"自动控制系统\",\"params\":{},\"questions\":[{\"lessonId\":23,\"orderNum\":1,\"params\":{},\"questionContent\":\"第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。\",\"questionId\":54,\"questionScore\":40,\"questionType\":\"typing\",\"scoringItems\":[],\"typingDuration\":5,\"wordCount\":131},{\"filePath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.docx\",\"lessonId\":23,\"orderNum\":2,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.pdf\",\"questionContent\":\"定时灌溉和智慧农场系统的区别\",\"questionId\":55,\"questionScore\":30,\"questionType\":\"practical\",\"scoringItems\":[null]},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"顺序结构\",\"optionB\":\"分支结构\",\"optionC\":\"循环结构\",\"optionD\":\"选择结构\",\"orderNum\":3,\"params\":{},\"questionContent\":\"猜数字游戏中，用于反复让用户输入的结构是\",\"questionId\":58,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"排序法\",\"optionB\":\"枚举法\",\"optionC\":\"加密法\",\"optionD\":\"压缩法\",\"orderNum\":4,\"params\":{},\"questionContent\":\"在“鸡兔同笼”问题中，常用的算法思想是？\",\"questionId\":57,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"随意性\",\"optionB\":\"有限性\",\"optionC\":\"娱乐性\",\"optionD\":\"模糊性\",\"orderNum\":5,\"params\":{},\"questionContent\":\"下列哪一项属于算法的基本特征\",\"questionId\":56,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"A\",\"lessonId\":23,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":6,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":22,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"Python\",\"optionB\":\"Java\",\"optionC\":\"Word\",\"optionD\":\"Scratch\",\"orderNum\":7,\"params\":{},\"questionContent\":\"下列哪个不是编程语言？\",\"questionId\":63,\"questionScore\":10,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"T\",\"lessonId\":23,\"orderNum\":8,\"params\":{},\"questionContent\":\"在猜数字游戏中，如果采用二分查找的方法，最多 7 次就一定能猜', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\",\"2班\",\"4班\",\"7班\",\"8班\"],\"grade\":6,\"lessonId\":23,\"lessonNum\":1,\"lessonTitle\":\"自动控制系统\",\"params\":{},\"questions\":[{\"lessonId\":23,\"orderNum\":1,\"params\":{},\"questionContent\":\"第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。\",\"questionId\":54,\"questionScore\":40,\"questionType\":\"typing\",\"scoringItems\":[],\"typingDuration\":5,\"wordCount\":131},{\"filePath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.docx\",\"lessonId\":23,\"orderNum\":2,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.pdf\",\"questionContent\":\"定时灌溉和智慧农场系统的区别\",\"questionId\":55,\"questionScore\":30,\"questionType\":\"practical\",\"scoringItems\":[null]},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"顺序结构\",\"optionB\":\"分支结构\",\"optionC\":\"循环结构\",\"optionD\":\"选择结构\",\"orderNum\":3,\"params\":{},\"questionContent\":\"猜数字游戏中，用于反复让用户输入的结构是\",\"questionId\":58,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"排序法\",\"optionB\":\"枚举法\",\"optionC\":\"加密法\",\"optionD\":\"压缩法\",\"orderNum\":4,\"params\":{},\"questionContent\":\"在“鸡兔同笼”问题中，常用的算法思想是？\",\"questionId\":57,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"随意性\",\"optionB\":\"有限性\",\"optionC\":\"娱乐性\",\"optionD\":\"模糊性\",\"orderNum\":5,\"params\":{},\"questionContent\":\"下列哪一项属于算法的基本特征\",\"questionId\":56,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"A\",\"lessonId\":23,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":6,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":22,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"Python\",\"optionB\":\"Java\",\"optionC\":\"Word\",\"optionD\":\"Scratch\",\"orderNum\":7,\"params\":{},\"questionContent\":\"下列哪个不是编程语言？\",\"questionId\":63,\"questionScore\":10,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"T\",\"lessonId\":23,\"orderNum\":8,\"params\":{},\"questionContent\":', 0, NULL, '2026-01-13 11:30:52', 49);
INSERT INTO `sys_oper_log` VALUES (456, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大徐小学', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\",\"2班\",\"4班\",\"7班\",\"8班\"],\"grade\":6,\"lessonId\":23,\"lessonNum\":1,\"lessonTitle\":\"自动控制系统\",\"params\":{},\"questions\":[{\"lessonId\":23,\"orderNum\":1,\"params\":{},\"questionContent\":\"第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。\",\"questionId\":54,\"questionScore\":40,\"questionType\":\"typing\",\"scoringItems\":[],\"typingDuration\":5,\"wordCount\":131},{\"filePath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.docx\",\"lessonId\":23,\"orderNum\":2,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.pdf\",\"questionContent\":\"定时灌溉和智慧农场系统的区别\",\"questionId\":55,\"questionScore\":30,\"questionType\":\"practical\",\"scoringItems\":[null]},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"顺序结构\",\"optionB\":\"分支结构\",\"optionC\":\"循环结构\",\"optionD\":\"选择结构\",\"orderNum\":3,\"params\":{},\"questionContent\":\"猜数字游戏中，用于反复让用户输入的结构是\",\"questionId\":58,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"排序法\",\"optionB\":\"枚举法\",\"optionC\":\"加密法\",\"optionD\":\"压缩法\",\"orderNum\":4,\"params\":{},\"questionContent\":\"在“鸡兔同笼”问题中，常用的算法思想是？\",\"questionId\":57,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"随意性\",\"optionB\":\"有限性\",\"optionC\":\"娱乐性\",\"optionD\":\"模糊性\",\"orderNum\":5,\"params\":{},\"questionContent\":\"下列哪一项属于算法的基本特征\",\"questionId\":56,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"A\",\"lessonId\":23,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":6,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":22,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"Python\",\"optionB\":\"Java\",\"optionC\":\"Word\",\"optionD\":\"Scratch\",\"orderNum\":7,\"params\":{},\"questionContent\":\"下列哪个不是编程语言？\",\"questionId\":63,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"T\",\"lessonId\":23,\"orderNum\":8,\"params\":{},\"questionContent\":\"在猜数字游戏中，如果采用二分查找的方法，最多 7 次就一定能猜出', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\",\"2班\",\"4班\",\"7班\",\"8班\"],\"grade\":6,\"lessonId\":23,\"lessonNum\":1,\"lessonTitle\":\"自动控制系统\",\"params\":{},\"questions\":[{\"lessonId\":23,\"orderNum\":1,\"params\":{},\"questionContent\":\"第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。\",\"questionId\":54,\"questionScore\":40,\"questionType\":\"typing\",\"scoringItems\":[],\"typingDuration\":5,\"wordCount\":131},{\"filePath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.docx\",\"lessonId\":23,\"orderNum\":2,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.pdf\",\"questionContent\":\"定时灌溉和智慧农场系统的区别\",\"questionId\":55,\"questionScore\":30,\"questionType\":\"practical\",\"scoringItems\":[null]},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"顺序结构\",\"optionB\":\"分支结构\",\"optionC\":\"循环结构\",\"optionD\":\"选择结构\",\"orderNum\":3,\"params\":{},\"questionContent\":\"猜数字游戏中，用于反复让用户输入的结构是\",\"questionId\":58,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"排序法\",\"optionB\":\"枚举法\",\"optionC\":\"加密法\",\"optionD\":\"压缩法\",\"orderNum\":4,\"params\":{},\"questionContent\":\"在“鸡兔同笼”问题中，常用的算法思想是？\",\"questionId\":57,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"随意性\",\"optionB\":\"有限性\",\"optionC\":\"娱乐性\",\"optionD\":\"模糊性\",\"orderNum\":5,\"params\":{},\"questionContent\":\"下列哪一项属于算法的基本特征\",\"questionId\":56,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"A\",\"lessonId\":23,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":6,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":22,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"Python\",\"optionB\":\"Java\",\"optionC\":\"Word\",\"optionD\":\"Scratch\",\"orderNum\":7,\"params\":{},\"questionContent\":\"下列哪个不是编程语言？\",\"questionId\":63,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"T\",\"lessonId\":23,\"orderNum\":8,\"params\":{},\"questionContent\":\"', 0, NULL, '2026-01-13 11:33:00', 41);
INSERT INTO `sys_oper_log` VALUES (457, '学生管理', 1, 'com.ruoyi.business.controller.BizStudentController.add()', 'POST', 1, '19157727791', '大徐小学', '/business/student', '127.0.0.1', '内网IP', '{\"classCode\":\"10\",\"entryYear\":\"2020\",\"params\":{},\"studentId\":318,\"studentName\":\"555\",\"studentNo\":\"99\",\"userId\":424}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-14 10:22:14', 597);
INSERT INTO `sys_oper_log` VALUES (458, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大徐小学', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"1班\"],\"grade\":6,\"lessonId\":24,\"lessonNum\":1,\"lessonTitle\":\"自动控制系统复习\",\"params\":{},\"questions\":[{\"answer\":\"C\",\"optionA\":\"Python\",\"optionB\":\"Java\",\"optionC\":\"Word\",\"optionD\":\"Scratch\",\"orderNum\":1,\"params\":{},\"questionContent\":\"下列哪个不是编程语言？\",\"questionId\":63,\"questionScore\":50,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"A\",\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":2,\"params\":{},\"questionContent\":\"【选择题】中国的首都是哪里？\",\"questionId\":62,\"questionScore\":50,\"questionType\":\"choice\",\"scoringItems\":[]}],\"randomChoiceCount\":0,\"randomJudgmentCount\":0,\"semester\":\"0\",\"shuffleMode\":0}', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"1班\"],\"grade\":6,\"lessonId\":24,\"lessonNum\":1,\"lessonTitle\":\"自动控制系统复习\",\"params\":{},\"questions\":[{\"answer\":\"C\",\"optionA\":\"Python\",\"optionB\":\"Java\",\"optionC\":\"Word\",\"optionD\":\"Scratch\",\"orderNum\":1,\"params\":{},\"questionContent\":\"下列哪个不是编程语言？\",\"questionId\":63,\"questionScore\":50,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"A\",\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":2,\"params\":{},\"questionContent\":\"【选择题】中国的首都是哪里？\",\"questionId\":62,\"questionScore\":50,\"questionType\":\"choice\",\"scoringItems\":[]}],\"randomChoiceCount\":0,\"randomJudgmentCount\":0,\"semester\":\"0\",\"shuffleMode\":0}}', 0, NULL, '2026-01-14 16:19:29', 264);
INSERT INTO `sys_oper_log` VALUES (459, '学生管理', 6, 'com.ruoyi.business.controller.BizStudentController.importData()', 'POST', 1, '19157727791', '大徐小学', '/business/student/importData', '127.0.0.1', '内网IP', '', '{\"msg\":\"恭喜您，数据已全部导入成功！共 127 条，数据如下：<br/>1、学生 鲍祈帆 导入成功，登录账号为 2020710301<br/>2、学生 陈羽晟 导入成功，登录账号为 2020710302<br/>3、学生 程至锦 导入成功，登录账号为 2020710303<br/>4、学生 池慧诗 导入成功，登录账号为 2020710304<br/>5、学生 何曙瑞 导入成功，登录账号为 2020710306<br/>6、学生 胡梓琪 导入成功，登录账号为 2020710307<br/>7、学生 黄金奕 导入成功，登录账号为 2020710308<br/>8、学生 蒋心怡 导入成功，登录账号为 2020710309<br/>9、学生 赖逸帆 导入成功，登录账号为 2020710310<br/>10、学生 李秋阳 导入成功，登录账号为 2020710311<br/>11、学生 李紫奕 导入成功，登录账号为 2020710312<br/>12、学生 励睿轩 导入成功，登录账号为 2020710313<br/>13、学生 梁滋苡 导入成功，登录账号为 2020710314<br/>14、学生 林茜 导入成功，登录账号为 2020710315<br/>15、学生 林欣怡 导入成功，登录账号为 2020710316<br/>16、学生 卢锦琪 导入成功，登录账号为 2020710317<br/>17、学生 马广胤 导入成功，登录账号为 2020710318<br/>18、学生 潘一恒 导入成功，登录账号为 2020710319<br/>19、学生 潘伊菲 导入成功，登录账号为 2020710320<br/>20、学生 潘梓欣 导入成功，登录账号为 2020710321<br/>21、学生 庞德霖 导入成功，登录账号为 2020710322<br/>22、学生 邱文绍 导入成功，登录账号为 2020710323<br/>23、学生 邵铭轩 导入成功，登录账号为 2020710324<br/>24、学生 邵屹 导入成功，登录账号为 2020710325<br/>25、学生 盛杰睿 导入成功，登录账号为 2020710326<br/>26、学生 童子昱 导入成功，登录账号为 2020710328<br/>27、学生 王思淏 导入成功，登录账号为 2020710329<br/>28、学生 吴家顺 导入成功，登录账号为 2020710330<br/>29、学生 徐子羡 导入成功，登录账号为 2020710331<br/>30、学生 余子涵 导入成功，登录账号为 2020710332<br/>31、学生 俞昊 导入成功，登录账号为 2020710333<br/>32、学生 俞林希 导入成功，登录账号为 2020710334<br/>33、学生 虞茹熙 导入成功，登录账号为 2020710335<br/>34、学生 张语菲 导入成功，登录账号为 2020710336<br/>35、学生 郑希菡 导入成功，登录账号为 2020710337<br/>36、学生 郑希暖 导入成功，登录账号为 2020710338<br/>37、学生 郑紫忻 导入成功，登录账号为 2020710339<br/>38、学生 朱宇奇 导入成功，登录账号为 2020710340<br/>39、学生 赵煜罕 导入成功，登录账号为 2020710341<br/>40、学生 潘伟祺 导入成功，登录账号为 2020710342<br/>41、学生 刘逸诺 导入成功，登录账号为 2020710305<br/>42、学生 张瀚宇 导入成功，登录账号为 2020710327<br/>43、学生 鲍仟峰 导入成功，登录账号为 2020710501<br/>44、学生 陈首任 导入成功，登录账号为 2020710502<br/>45、学生 陈彦旭 导入成功，登录账号为 2020710503<br/>46、学生 陈奕涵 导入成功，登录账号为 2020710504<br/>47、学生 陈语诺 导入成功，登录账号为 2020710505<br/>48、学生 葛培栋 导入成功，登录账号为 2020710506<br/>49、学生 洪天逸 导入成功，登录账号为 2020710507<br/>50、学生 洪梓涵 导入成功，登录账号为 2020710508<br/>51、学生 黄智桐 导入成功，登录账号为 2020710509<br/>52、学生 林若妡 导入成功，登录账号为 2020710510<br/>53、学生 林一凡 导入成功，登录账号为 2020710511<br/>54、学生 刘科 导入成功，登录账号为 2020710512<br/>55、学生 刘一宇 导入成功，登录账号为 20207105', 0, NULL, '2026-01-16 11:54:41', 15499);
INSERT INTO `sys_oper_log` VALUES (460, '课程设计与指派', 1, 'com.ruoyi.business.controller.BizLessonController.saveAll()', 'POST', 1, '19157727791', '大徐小学', '/business/lesson/save-all', '127.0.0.1', '内网IP', '{\"assignedClassCodes\":[\"2班\",\"4班\",\"7班\",\"8班\",\"3班\",\"5班\",\"6班\"],\"grade\":6,\"lessonId\":23,\"lessonNum\":1,\"lessonTitle\":\"自动控制系统\",\"params\":{},\"questions\":[{\"lessonId\":23,\"orderNum\":1,\"params\":{},\"questionContent\":\"第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。\",\"questionId\":54,\"questionScore\":40,\"questionType\":\"typing\",\"scoringItems\":[],\"typingDuration\":5,\"wordCount\":131},{\"filePath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.docx\",\"lessonId\":23,\"orderNum\":2,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.pdf\",\"questionContent\":\"定时灌溉和智慧农场系统的区别\",\"questionId\":55,\"questionScore\":30,\"questionType\":\"practical\",\"scoringItems\":[{\"itemId\":12,\"itemName\":\"答案是否正常\",\"itemScore\":100,\"orderNum\":0,\"params\":{},\"questionId\":55}]},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"顺序结构\",\"optionB\":\"分支结构\",\"optionC\":\"循环结构\",\"optionD\":\"选择结构\",\"orderNum\":3,\"params\":{},\"questionContent\":\"猜数字游戏中，用于反复让用户输入的结构是\",\"questionId\":58,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"排序法\",\"optionB\":\"枚举法\",\"optionC\":\"加密法\",\"optionD\":\"压缩法\",\"orderNum\":4,\"params\":{},\"questionContent\":\"在“鸡兔同笼”问题中，常用的算法思想是？\",\"questionId\":57,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"随意性\",\"optionB\":\"有限性\",\"optionC\":\"娱乐性\",\"optionD\":\"模糊性\",\"orderNum\":5,\"params\":{},\"questionContent\":\"下列哪一项属于算法的基本特征\",\"questionId\":56,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"A\",\"lessonId\":23,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":6,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":22,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"Python\",\"optionB\":\"Java\",\"optionC\":\"Word\",\"optionD\":\"Scratch\",\"orderNum\":7,\"params\":{},\"questionContent\":\"下列哪个不是编程语言？\",\"questionId\":63,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer', '{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"assignedClassCodes\":[\"2班\",\"4班\",\"7班\",\"8班\",\"3班\",\"5班\",\"6班\"],\"grade\":6,\"lessonId\":23,\"lessonNum\":1,\"lessonTitle\":\"自动控制系统\",\"params\":{},\"questions\":[{\"lessonId\":23,\"orderNum\":1,\"params\":{},\"questionContent\":\"第1课自动控制系统介绍了自动控制系统能在无人干预下自动实现目标，如空调、自动灌溉等。系统通常由控制器、执行器和被控对象组成，并常借助传感器获取反馈信息。计算机在其中起关键作用：通过指令实现更精准、复杂和智能的控制，提升系统灵活性与效率。计算机能实现更智能的控制。\",\"questionId\":54,\"questionScore\":40,\"questionType\":\"typing\",\"scoringItems\":[],\"typingDuration\":5,\"wordCount\":131},{\"filePath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.docx\",\"lessonId\":23,\"orderNum\":2,\"params\":{},\"previewPath\":\"/profile/upload/2026/01/12/学习单_20260112125728A001.pdf\",\"questionContent\":\"定时灌溉和智慧农场系统的区别\",\"questionId\":55,\"questionScore\":30,\"questionType\":\"practical\",\"scoringItems\":[{\"itemId\":12,\"itemName\":\"答案是否正常\",\"itemScore\":100,\"orderNum\":0,\"params\":{},\"questionId\":55}]},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"顺序结构\",\"optionB\":\"分支结构\",\"optionC\":\"循环结构\",\"optionD\":\"选择结构\",\"orderNum\":3,\"params\":{},\"questionContent\":\"猜数字游戏中，用于反复让用户输入的结构是\",\"questionId\":58,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"排序法\",\"optionB\":\"枚举法\",\"optionC\":\"加密法\",\"optionD\":\"压缩法\",\"orderNum\":4,\"params\":{},\"questionContent\":\"在“鸡兔同笼”问题中，常用的算法思想是？\",\"questionId\":57,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"B\",\"lessonId\":23,\"optionA\":\"随意性\",\"optionB\":\"有限性\",\"optionC\":\"娱乐性\",\"optionD\":\"模糊性\",\"orderNum\":5,\"params\":{},\"questionContent\":\"下列哪一项属于算法的基本特征\",\"questionId\":56,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"A\",\"lessonId\":23,\"optionA\":\"北京\",\"optionB\":\"上海\",\"optionC\":\"广州\",\"optionD\":\"深圳\",\"orderNum\":6,\"params\":{},\"questionContent\":\"中国的首都是哪里？\",\"questionId\":22,\"questionScore\":5,\"questionType\":\"choice\",\"scoringItems\":[]},{\"answer\":\"C\",\"lessonId\":23,\"optionA\":\"Python\",\"optionB\":\"Java\",\"optionC\":\"Word\",\"optionD\":\"Scratch\",\"orderNum\":7,\"params\":{},\"questionContent\":\"下列哪个不是编程语言？\",\"questionId\":63,\"questionScore\":5,\"questionType\":\"cho', 0, NULL, '2026-01-16 11:55:17', 103);
INSERT INTO `sys_oper_log` VALUES (461, '用户管理', 1, 'com.ruoyi.web.controller.system.SysUserController.add()', 'POST', 1, 'admin', '高中', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"createBy\":\"admin\",\"deptId\":100,\"deptIds\":[100],\"nickName\":\"sxs\",\"params\":{},\"postIds\":[],\"roleIds\":[],\"status\":\"0\",\"userId\":552,\"userName\":\"sx\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-16 15:28:36', 119);
INSERT INTO `sys_oper_log` VALUES (462, '用户管理', 3, 'com.ruoyi.web.controller.system.SysUserController.remove()', 'DELETE', 1, 'admin', '高中', '/system/user/552', '127.0.0.1', '内网IP', '[552]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-16 15:29:12', 21);
INSERT INTO `sys_oper_log` VALUES (463, '用户管理', 1, 'com.ruoyi.web.controller.system.SysUserController.add()', 'POST', 1, 'admin', '高中', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"createBy\":\"admin\",\"deptId\":104,\"deptIds\":[104],\"nickName\":\"ce\",\"params\":{},\"postIds\":[],\"roleIds\":[],\"status\":\"0\",\"userId\":553,\"userName\":\"cece\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-16 15:29:34', 91);
INSERT INTO `sys_oper_log` VALUES (464, '用户管理', 3, 'com.ruoyi.web.controller.system.SysUserController.remove()', 'DELETE', 1, 'admin', '高中', '/system/user/553', '127.0.0.1', '内网IP', '[553]', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-16 15:29:41', 16);
INSERT INTO `sys_oper_log` VALUES (465, '角色管理', 1, 'com.ruoyi.web.controller.system.SysRoleController.add()', 'POST', 1, 'admin', '高中', '/system/role', '127.0.0.1', '内网IP', '{\"admin\":false,\"createBy\":\"admin\",\"deptCheckStrictly\":true,\"deptIds\":[],\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[2037,1,100,1000,1001,1002,1003,1004,1005,1006,101,1007,1008,1009,1010,1011,102,1012,1013,1014,1015,103,1016,1017,1018,1019,104,1020,1021,1022,1023,1024,105,1025,1026,1027,1028,1029,106,1030,1031,1032,1033,1034,107,1035,1036,1037,1038,108,500,1039,1040,1041,501,1042,1043,1044,1045,2018,2019,2020,2021,2022,2023,2024,2031,2032,2033,2034,2035,2043,2036,2,109,1046,1047,1048,110,1049,1050,1051,1052,1053,1054,111,112,113,114,2006,2007,2008,2009,2010,2011,3,115,116,1055,1056,1057,1058,1059,1060,117,4,2038,2039,2040,2042,2026,2027,2028,2029,2030],\"params\":{},\"roleId\":102,\"roleKey\":\"researcher\",\"roleName\":\"教研员\",\"roleSort\":3,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-16 15:44:28', 412);
INSERT INTO `sys_oper_log` VALUES (466, '角色管理', 2, 'com.ruoyi.web.controller.system.SysRoleController.dataScope()', 'PUT', 1, 'admin', '高中', '/system/role/dataScope', '127.0.0.1', '内网IP', '{\"admin\":false,\"createTime\":\"2026-01-16 15:44:28\",\"dataScope\":\"1\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"deptIds\":[],\"flag\":false,\"menuCheckStrictly\":true,\"params\":{},\"roleId\":102,\"roleKey\":\"researcher\",\"roleName\":\"教研员\",\"roleSort\":3,\"status\":\"0\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-16 15:44:56', 20);
INSERT INTO `sys_oper_log` VALUES (467, '用户管理', 1, 'com.ruoyi.web.controller.system.SysUserController.add()', 'POST', 1, 'admin', '高中', '/system/user', '127.0.0.1', '内网IP', '{\"admin\":false,\"createBy\":\"admin\",\"deptId\":100,\"deptIds\":[100],\"nickName\":\"陈\",\"params\":{},\"postIds\":[],\"roleIds\":[102],\"status\":\"0\",\"userId\":554,\"userName\":\"laoda\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-16 15:45:38', 200);
INSERT INTO `sys_oper_log` VALUES (468, '角色管理', 2, 'com.ruoyi.web.controller.system.SysRoleController.edit()', 'PUT', 1, 'admin', '高中', '/system/role', '127.0.0.1', '内网IP', '{\"admin\":false,\"createTime\":\"2026-01-16 15:44:28\",\"dataScope\":\"1\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[1,100,1000,1001,1002,1003,1004,1005,1006,101,1007,1008,1009,1010,1011,102,1012,1013,1014,1015,103,1016,1017,1018,1019,104,1020,1021,1022,1023,1024,106,1030,1031,1032,1033,1034,107,1035,1036,1037,1038,108,500,1039,1040,1041,501,1042,1043,1044,1045,2018,2019,2020,2021,2022,2023,2024,2031,2032,2033,2034,2035,2043,2036,2,109,1046,1047,1048,110,1049,1050,1051,1052,1053,1054,111,112,113,114,2042],\"params\":{},\"roleId\":102,\"roleKey\":\"researcher\",\"roleName\":\"教研员\",\"roleSort\":3,\"status\":\"0\",\"updateBy\":\"admin\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-01-16 15:48:08', 51);

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
) ENGINE = InnoDB AUTO_INCREMENT = 103 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'admin', 1, '1', 1, 1, '0', '0', 'admin', '2025-06-12 14:49:21', '', NULL, '超级管理员');
INSERT INTO `sys_role` VALUES (2, '普通角色', 'common', 2, '2', 1, 1, '0', '2', 'admin', '2025-06-12 14:49:21', 'admin', '2025-06-17 18:36:30', '普通角色');
INSERT INTO `sys_role` VALUES (100, '教师', 'teacher', 0, '1', 1, 1, '0', '0', 'admin', '2025-06-17 18:39:24', 'admin', '2026-01-13 08:53:54', NULL);
INSERT INTO `sys_role` VALUES (101, '学生', 'student', 2, '1', 1, 1, '0', '0', 'admin', '2025-06-19 09:45:57', '', NULL, NULL);
INSERT INTO `sys_role` VALUES (102, '教研员', 'researcher', 3, '1', 1, 1, '0', '0', 'admin', '2026-01-16 15:44:28', 'admin', '2026-01-16 15:48:08', NULL);

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
INSERT INTO `sys_role_menu` VALUES (100, 2043);
INSERT INTO `sys_role_menu` VALUES (101, 4);
INSERT INTO `sys_role_menu` VALUES (102, 1);
INSERT INTO `sys_role_menu` VALUES (102, 2);
INSERT INTO `sys_role_menu` VALUES (102, 100);
INSERT INTO `sys_role_menu` VALUES (102, 101);
INSERT INTO `sys_role_menu` VALUES (102, 102);
INSERT INTO `sys_role_menu` VALUES (102, 103);
INSERT INTO `sys_role_menu` VALUES (102, 104);
INSERT INTO `sys_role_menu` VALUES (102, 106);
INSERT INTO `sys_role_menu` VALUES (102, 107);
INSERT INTO `sys_role_menu` VALUES (102, 108);
INSERT INTO `sys_role_menu` VALUES (102, 109);
INSERT INTO `sys_role_menu` VALUES (102, 110);
INSERT INTO `sys_role_menu` VALUES (102, 111);
INSERT INTO `sys_role_menu` VALUES (102, 112);
INSERT INTO `sys_role_menu` VALUES (102, 113);
INSERT INTO `sys_role_menu` VALUES (102, 114);
INSERT INTO `sys_role_menu` VALUES (102, 500);
INSERT INTO `sys_role_menu` VALUES (102, 501);
INSERT INTO `sys_role_menu` VALUES (102, 1000);
INSERT INTO `sys_role_menu` VALUES (102, 1001);
INSERT INTO `sys_role_menu` VALUES (102, 1002);
INSERT INTO `sys_role_menu` VALUES (102, 1003);
INSERT INTO `sys_role_menu` VALUES (102, 1004);
INSERT INTO `sys_role_menu` VALUES (102, 1005);
INSERT INTO `sys_role_menu` VALUES (102, 1006);
INSERT INTO `sys_role_menu` VALUES (102, 1007);
INSERT INTO `sys_role_menu` VALUES (102, 1008);
INSERT INTO `sys_role_menu` VALUES (102, 1009);
INSERT INTO `sys_role_menu` VALUES (102, 1010);
INSERT INTO `sys_role_menu` VALUES (102, 1011);
INSERT INTO `sys_role_menu` VALUES (102, 1012);
INSERT INTO `sys_role_menu` VALUES (102, 1013);
INSERT INTO `sys_role_menu` VALUES (102, 1014);
INSERT INTO `sys_role_menu` VALUES (102, 1015);
INSERT INTO `sys_role_menu` VALUES (102, 1016);
INSERT INTO `sys_role_menu` VALUES (102, 1017);
INSERT INTO `sys_role_menu` VALUES (102, 1018);
INSERT INTO `sys_role_menu` VALUES (102, 1019);
INSERT INTO `sys_role_menu` VALUES (102, 1020);
INSERT INTO `sys_role_menu` VALUES (102, 1021);
INSERT INTO `sys_role_menu` VALUES (102, 1022);
INSERT INTO `sys_role_menu` VALUES (102, 1023);
INSERT INTO `sys_role_menu` VALUES (102, 1024);
INSERT INTO `sys_role_menu` VALUES (102, 1030);
INSERT INTO `sys_role_menu` VALUES (102, 1031);
INSERT INTO `sys_role_menu` VALUES (102, 1032);
INSERT INTO `sys_role_menu` VALUES (102, 1033);
INSERT INTO `sys_role_menu` VALUES (102, 1034);
INSERT INTO `sys_role_menu` VALUES (102, 1035);
INSERT INTO `sys_role_menu` VALUES (102, 1036);
INSERT INTO `sys_role_menu` VALUES (102, 1037);
INSERT INTO `sys_role_menu` VALUES (102, 1038);
INSERT INTO `sys_role_menu` VALUES (102, 1039);
INSERT INTO `sys_role_menu` VALUES (102, 1040);
INSERT INTO `sys_role_menu` VALUES (102, 1041);
INSERT INTO `sys_role_menu` VALUES (102, 1042);
INSERT INTO `sys_role_menu` VALUES (102, 1043);
INSERT INTO `sys_role_menu` VALUES (102, 1044);
INSERT INTO `sys_role_menu` VALUES (102, 1045);
INSERT INTO `sys_role_menu` VALUES (102, 1046);
INSERT INTO `sys_role_menu` VALUES (102, 1047);
INSERT INTO `sys_role_menu` VALUES (102, 1048);
INSERT INTO `sys_role_menu` VALUES (102, 1049);
INSERT INTO `sys_role_menu` VALUES (102, 1050);
INSERT INTO `sys_role_menu` VALUES (102, 1051);
INSERT INTO `sys_role_menu` VALUES (102, 1052);
INSERT INTO `sys_role_menu` VALUES (102, 1053);
INSERT INTO `sys_role_menu` VALUES (102, 1054);
INSERT INTO `sys_role_menu` VALUES (102, 2018);
INSERT INTO `sys_role_menu` VALUES (102, 2019);
INSERT INTO `sys_role_menu` VALUES (102, 2020);
INSERT INTO `sys_role_menu` VALUES (102, 2021);
INSERT INTO `sys_role_menu` VALUES (102, 2022);
INSERT INTO `sys_role_menu` VALUES (102, 2023);
INSERT INTO `sys_role_menu` VALUES (102, 2024);
INSERT INTO `sys_role_menu` VALUES (102, 2031);
INSERT INTO `sys_role_menu` VALUES (102, 2032);
INSERT INTO `sys_role_menu` VALUES (102, 2033);
INSERT INTO `sys_role_menu` VALUES (102, 2034);
INSERT INTO `sys_role_menu` VALUES (102, 2035);
INSERT INTO `sys_role_menu` VALUES (102, 2036);
INSERT INTO `sys_role_menu` VALUES (102, 2042);
INSERT INTO `sys_role_menu` VALUES (102, 2043);

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
) ENGINE = InnoDB AUTO_INCREMENT = 555 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 103, 'admin', '若依', '00', 'ry@163.com', '15888888888', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', '2026-01-16 15:19:03', '2025-06-12 14:49:21', 'admin', '2025-06-12 14:49:21', '', '2026-01-16 15:19:03', '管理员');
INSERT INTO `sys_user` VALUES (104, 107, '19157727791', '郑东旭', '00', '', '', '0', '', '$2a$10$PWHYbxojQaQh4Pi5ugZyjeXMMszlf621A5KRFASQIIc8.iwYmuAR6', '0', '0', '127.0.0.1', '2026-01-16 13:49:01', '2025-07-02 10:14:56', 'admin', '2025-06-25 10:28:28', 'admin', '2026-01-16 13:49:00', NULL);
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
INSERT INTO `sys_user` VALUES (163, 169, '2025720506', '张20255', '00', '', '', '0', '', '$2a$10$iS4FTMfcQgc7y5QVDx4Jrui7j4l0aEB/7NokcdaAbYwWac7Hi2VJq', '0', '0', '127.0.0.1', '2026-01-12 10:14:46', '2026-01-06 11:45:26', '19157727791', '2025-08-25 20:07:57', '', '2026-01-12 10:14:45', NULL);
INSERT INTO `sys_user` VALUES (164, 107, '202470201', 'ksj', '00', '', '', '0', '', '$2a$10$wDgjLAgL3U14mT0BqSQkK.8YIRCJkTe.Lxdz6Q5/VFXaJBnTeoRC.', '0', '0', '', NULL, NULL, '19157727791', '2025-10-08 15:01:11', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (165, 107, '202370102', 'oj', '00', '', '', '0', '', '$2a$10$QSBbMzU23cJhJ6DdFdl0ruV77odxwAxJVOugDpwRQK8odDv6kBik2', '0', '0', '', NULL, NULL, '19157727791', '2025-10-08 15:01:11', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (166, 169, 'yelaoshi', '叶', '00', '', '', '1', '', '$2a$10$eeTvwISIB/H9hmDrrWuKAu4Ot01x4GBvWokQm2papUg44a7DeckFe', '0', '0', '127.0.0.1', '2025-12-30 15:04:44', NULL, '19157727791', '2025-12-30 10:44:53', '', '2025-12-30 15:04:44', NULL);
INSERT INTO `sys_user` VALUES (167, 169, '2025720501', '真实姓名呢', '00', '', '', '0', '', '$2a$10$poNw5f1XTXzFcoK7iSCFDOKnWQGhBl6iDYPnwfJAf7f7ctL4rmUDa', '0', '0', '127.0.0.1', '2026-01-09 09:09:20', NULL, '19157727791', '2026-01-07 16:42:09', '', '2026-01-09 09:09:20', NULL);
INSERT INTO `sys_user` VALUES (168, 139, '2020710701', '陈语馨', '00', '', '', '0', '', '$2a$10$bhWBMnwyCerKdKkg1lcNtuTp8HEBzIXJGlynsdHY8vVuOvqG7aKEO', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:24', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (169, 139, '2020710702', '陈语诺', '00', '', '', '0', '', '$2a$10$NgMLZzKR3vQjfIRFvVD99eqY7cxDzVmbZlQhT9mghz3PBxj6qYEJS', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:24', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (170, 139, '2020710703', '程梓硕', '00', '', '', '0', '', '$2a$10$Kjz7phdDOHSXgsEaMe18mOjnFgjydWONVkTzcSdb9mu3vIcTs6wiq', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:24', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (171, 139, '2020710704', '仇熙言', '00', '', '', '0', '', '$2a$10$Sw8oaTOc767BpPRplLyhIe4sPSqQkOWY7lKu/lpf6wrRfK6V7snGK', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:25', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (172, 139, '2020710705', '丁峙宇', '00', '', '', '0', '', '$2a$10$1SP66ObwwNoUQqT9tQh5Z.D51UROTLlQENGLUc2x/LQCTFo406FXq', '0', '0', '127.0.0.1', '2026-01-12 14:57:49', NULL, '19157727791', '2026-01-12 12:51:25', '', '2026-01-12 14:57:49', NULL);
INSERT INTO `sys_user` VALUES (173, 139, '2020710706', '方韩斌', '00', '', '', '0', '', '$2a$10$o5I7o8aDstKp22OGGHrwre9jdJeKVfinU/yNdQ/TNTVCXl3cFxQs.', '0', '0', '127.0.0.1', '2026-01-13 11:19:55', NULL, '19157727791', '2026-01-12 12:51:25', '', '2026-01-13 11:19:54', NULL);
INSERT INTO `sys_user` VALUES (174, 139, '2020710707', '高艺嘉', '00', '', '', '0', '', '$2a$10$Sg9wbm3SHyWCOmr5hifEE.e.r1mdZFi7S02DKir06aP9G7ZXC1zXG', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:25', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (175, 139, '2020710708', '何奕宸', '00', '', '', '0', '', '$2a$10$Q4tuWpNy0jurbYrTcoh9VusfCDRGcqwFOdI2wqR5b.e7l6rtRC0c2', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:25', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (176, 139, '2020710709', '何禹申', '00', '', '', '0', '', '$2a$10$mF8wHULD1BsoaVQ95F7eMuWk29zIkr8vd7sauxZ3Dz1swA90tW38u', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:25', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (177, 139, '2020710710', '赖俊杰', '00', '', '', '0', '', '$2a$10$BVZIUUw1.3D/i4youH5VK.v2q4xwBUoxnETvFufCJn15G3vKIXJOy', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:25', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (178, 139, '2020710711', '金依娜', '00', '', '', '0', '', '$2a$10$EVYyQsxNpEnWufeTIwe6OeVHdVJYPwh688/YKfpU1sMwYBO3llHlG', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:25', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (179, 139, '2020710712', '赖铠哲', '00', '', '', '0', '', '$2a$10$qBx9weaCnrhO8qYgqph23uIY0YmOaPcW00ShJP1vvtocbe8ymjDoO', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:25', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (180, 139, '2020710713', '李栩森', '00', '', '', '0', '', '$2a$10$I5v8iciwaoJXZ44cbMnQA.r5abCdqta/2aUHVJgu1ETSCmLU4Clyi', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:25', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (181, 139, '2020710714', '娄逸轩', '00', '', '', '0', '', '$2a$10$eHSuecJQCGVvPmoi05xyR.kFKFsXYC5LAffnaYWBkONiNEzVu6efK', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:25', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (182, 139, '2020710715', '卢妙涵', '00', '', '', '0', '', '$2a$10$KcvLTLsmaAbQtb47VVBkye/bYTaWPG8OllIZpwgc84rY3jA1/HM3q', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:26', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (183, 139, '2020710716', '罗志超', '00', '', '', '0', '', '$2a$10$mjiufK7.nYY7lZjW4CahwuKqMuRqGjPVhTkmB8MoidTwlBRSjJN6W', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:26', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (184, 139, '2020710717', '马梓辰', '00', '', '', '0', '', '$2a$10$W24CZs3wOdINz286nBZ4vuLyaOx6N/iZfU7nCWGrDAddFoqOXuPK2', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:26', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (185, 139, '2020710718', '潘姿妤', '00', '', '', '0', '', '$2a$10$wlUvUnczZNWdB93UYMFxhOtk0FPAHzAeG/RwkNaLFr8VYugTg5RRm', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:26', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (186, 139, '2020710719', '潘梓睿', '00', '', '', '0', '', '$2a$10$bZEUlJa/StKUz6G5Pk5cROZN2aAg/EpfmoTXWlkt16AU0zKbHfz32', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:26', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (187, 139, '2020710720', '史睿栋', '00', '', '', '0', '', '$2a$10$ES5RnjsDn2u92kxZ0VE/re81iaa9Lf4g97cyoHOfJXI/0kiFlwT.6', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:26', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (188, 139, '2020710721', '王浩宸', '00', '', '', '0', '', '$2a$10$aoFEmyksQYgHzEOb5RqrqOVzI4viFyp1HwB39A3jGFtM/HlNt4l0G', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:26', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (189, 139, '2020710722', '王悦涵', '00', '', '', '0', '', '$2a$10$mSablsD1dBz3hJ5dMgRgmeEruLCmnjrDsXHPMe6AxI4V4d0BJ28Gi', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:26', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (190, 139, '2020710723', '吴俊霖', '00', '', '', '0', '', '$2a$10$DyitvuT5wPO2fLOnxarZ.elzdG.OES4H3DAbPllNQpTSx1E.PDe3q', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:26', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (191, 139, '2020710724', '奚一溦', '00', '', '', '0', '', '$2a$10$PqZI2ceY7C.rgdmh5RUmAOp2CTgF/VAS/6AKqte64oLnIMYxYXNwe', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:26', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (192, 139, '2020710725', '项羿', '00', '', '', '0', '', '$2a$10$Zp5buuMo0phwT8XftMd4WOf.GlrzU3u6dWdPmidWn2Rv1rZjmXWqm', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:27', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (193, 139, '2020710726', '肖恩迪', '00', '', '', '0', '', '$2a$10$JG8lR7VYUwlbAPYuPYaw/.kIbXVw0.u8ZEzv/eaMB9gqTfhgUg4jK', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:27', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (194, 139, '2020710727', '徐翎轩', '00', '', '', '0', '', '$2a$10$/RbJaPHp/Vs0aQq6fXhLZ.4ueMbPJIpj/mSL1gLLaytBmluV/n3s2', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:27', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (195, 139, '2020710728', '叶凌轩', '00', '', '', '0', '', '$2a$10$5jIbx2u7yek1QVDNtFPH2eelrTjpzDX6y97TByatrNSCVWs4kp0.G', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:27', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (196, 139, '2020710729', '余梓晨', '00', '', '', '0', '', '$2a$10$rc2JnMpDbYimKtSLe69E3ev5BPgP7cJCpgqj5hOSUIfQcAHJsAOB2', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:27', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (197, 139, '2020710730', '俞佩婕', '00', '', '', '0', '', '$2a$10$.DjbVS9hb5liYLVt.Fvu1e6PSjmRVq8kq7ubEsWVXNMqbwgWxYSf6', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:27', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (198, 139, '2020710731', '俞瑞希', '00', '', '', '0', '', '$2a$10$He0RWqjRNM8zxNSXkWxbR.Aesn6H.28bPNwGbmEiHOOrgeQgcEPM.', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:27', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (199, 139, '2020710732', '张江涛', '00', '', '', '0', '', '$2a$10$6eCrfqPiZqP2kT2wR0v2OeLEEt5p29sNxsZj56p43r2v8SyzVXAOm', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:27', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (200, 139, '2020710733', '张珂雯', '00', '', '', '0', '', '$2a$10$4GQmdY/ThOWCugfASTHBo.eyUUc99d7WpRw8a269cBLSiMmzVKYP6', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:27', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (201, 139, '2020710734', '章芯桐', '00', '', '', '0', '', '$2a$10$xFjB913.nAVRrbE3tpkjTOxqiEawHuJg0yLbA79hMzFPW3m05DQNO', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:27', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (202, 139, '2020710735', '赵启翔', '00', '', '', '0', '', '$2a$10$NJPotUOEj0KdoBgGJd6/ReSJ42jKbPn1lPvnqBtFV7FP/uZ/aEqqy', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:27', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (203, 139, '2020710736', '郑伊诺', '00', '', '', '0', '', '$2a$10$XPERjMWoPCgIZtCsZWdxJOsWQOrW99ME.mHERnkWp0vR9s.jUvrL2', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:27', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (204, 139, '2020710737', '郑诣凡', '00', '', '', '0', '', '$2a$10$PNsTUGC2bYHUGfHxH9uMvO5nNyyudygxqthEeAJJYMX68.7wbA8T2', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:28', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (205, 139, '2020710738', '朱晨恺', '00', '', '', '0', '', '$2a$10$NE3nbziE/s/VHeIigtPdEO2/phac5BHQ.49mSwGnrzUa0kpxCXjfa', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:28', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (206, 139, '2020710739', '朱欣博', '00', '', '', '0', '', '$2a$10$VsUQV5KRrJUf7QPIxGutDuJXvVQceO.Enljnl4JPcWSsO3N77mz8q', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:28', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (207, 139, '2020710740', '朱雅楠', '00', '', '', '0', '', '$2a$10$zq/cx3YU7V47S4KFNlW9Zu.B.HZmMx/m.j4lxfcy3Rf2qyun812a6', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:28', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (208, 139, '2020710741', '王营', '00', '', '', '0', '', '$2a$10$R93IH7AhNwGzPlR0lYvD/e6Pu/Ibn2sFp.KItefLKhTjyRR/I6/sG', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:28', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (209, 139, '2020710742', '林夕', '00', '', '', '0', '', '$2a$10$ZBN4827f3bj55gv4sb4v4e.TSEqoAcVw4SszgjCC9TKCTuyE1CBGi', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:28', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (210, 139, '2020710743', '张艺帆', '00', '', '', '0', '', '$2a$10$YJrzSz7Tczv63qGC8SXv8OIlJQDbHU6acEoPBtBF1olRSbJOOQ/qu', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 12:51:28', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (211, 139, '2020710401', '曹紫云', '00', '', '', '0', '', '$2a$10$ZCRvB7PJ00wlgWML9G1DfOdfjPqKXXMFGkunGtP20XxyK/cwdbo0W', '0', '2', '127.0.0.1', '2026-01-12 13:52:33', NULL, '19157727791', '2026-01-12 12:51:28', '', '2026-01-12 13:52:33', NULL);
INSERT INTO `sys_user` VALUES (212, 139, '2020710402', '陈芊然', '00', '', '', '0', '', '$2a$10$HKUi5woOD0P9GtMQ/yVUx.XMg71EmjePJXHMpjkwjKjhSWMwB7LSu', '0', '2', '127.0.0.1', '2026-01-12 13:49:58', NULL, '19157727791', '2026-01-12 12:51:28', '', '2026-01-12 13:49:58', NULL);
INSERT INTO `sys_user` VALUES (213, 139, '2020710403', '陈心阳', '00', '', '', '0', '', '$2a$10$I7lA1c31pkgBCiGp/DzrvO2lDSn097v.OIB05qz.XliQG4k9TXn6S', '0', '2', '', NULL, NULL, '19157727791', '2026-01-12 12:51:28', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (214, 139, '2020710404', '董洛熙', '00', '', '', '0', '', '$2a$10$q0p3ySNVW.Ml5noRjWEWoeF3QW4GZP.L63LZgn/NVheEF32m72qCi', '0', '2', '127.0.0.1', '2026-01-12 13:14:59', NULL, '19157727791', '2026-01-12 12:51:28', '', '2026-01-12 13:14:59', NULL);
INSERT INTO `sys_user` VALUES (215, 139, '2020710405', '董悠然', '00', '', '', '0', '', '$2a$10$feA/ePLlepJbIDj6gU93f.qlWPcShcKd2XVOfyKC7s7MFr55O5pJK', '0', '2', '127.0.0.1', '2026-01-12 13:16:15', NULL, '19157727791', '2026-01-12 12:51:28', '', '2026-01-12 13:16:15', NULL);
INSERT INTO `sys_user` VALUES (216, 139, '2020710406', '顾丁奕', '00', '', '', '0', '', '$2a$10$XQVcMT01W2dUvtXnWhlOXu0u6pyn31qf8Fy2jVpM7nOlevE0Y86RG', '0', '2', '127.0.0.1', '2026-01-12 13:18:48', NULL, '19157727791', '2026-01-12 12:51:29', '', '2026-01-12 13:47:46', NULL);
INSERT INTO `sys_user` VALUES (217, 139, '2020710407', '韩芷若', '00', '', '', '0', '', '$2a$10$z6KdoQcAcAzP.Eh.ysirVeSrqXW4MErC2YVRFptGIBDVkziZWPGn6', '0', '2', '127.0.0.1', '2026-01-12 13:51:37', NULL, '19157727791', '2026-01-12 12:51:29', '', '2026-01-12 13:51:36', NULL);
INSERT INTO `sys_user` VALUES (218, 139, '2020710408', '黄博壹', '00', '', '', '0', '', '$2a$10$MnJTlk2eOyoGKdencd7CXewVhvRGs535xvrQnLukGpe47NxbLvrna', '0', '2', '127.0.0.1', '2026-01-12 13:15:50', NULL, '19157727791', '2026-01-12 12:51:29', '', '2026-01-12 13:15:50', NULL);
INSERT INTO `sys_user` VALUES (219, 139, '2020710409', '黄菀宁', '00', '', '', '0', '', '$2a$10$edRonE7/t1rGJWB4F0enYOvAk755k0v/HqTTHe2Iry5/EDMDy4ulG', '0', '2', '', NULL, NULL, '19157727791', '2026-01-12 12:51:29', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (220, 139, '2020710410', '金致远', '00', '', '', '0', '', '$2a$10$17Xd9ntQBrAU0xTFEpLD/.GPI714SZYNBKWwS0WnCq.8Wh3VhnN.a', '0', '2', '127.0.0.1', '2026-01-12 13:51:48', NULL, '19157727791', '2026-01-12 12:51:29', '', '2026-01-12 13:51:47', NULL);
INSERT INTO `sys_user` VALUES (221, 139, '2020710411', '金子珺', '00', '', '', '0', '', '$2a$10$O60OTebOxMrgZ6RYpIY8GOYN8w20WLSk.Dg2cA2vymQn7X3sj6WfK', '0', '2', '127.0.0.1', '2026-01-12 13:14:41', NULL, '19157727791', '2026-01-12 12:51:29', '', '2026-01-12 13:14:41', NULL);
INSERT INTO `sys_user` VALUES (222, 139, '2020710412', '柯恩哲', '00', '', '', '0', '', '$2a$10$WUHOy4oG0OGzbBJKbuqeTedU4LvHsAA5oibskRCGNrdluJTJWPeuO', '0', '2', '127.0.0.1', '2026-01-12 13:47:34', NULL, '19157727791', '2026-01-12 12:51:29', '', '2026-01-12 13:47:34', NULL);
INSERT INTO `sys_user` VALUES (223, 139, '2020710413', '赖佳彬', '00', '', '', '0', '', '$2a$10$zT3fjesjpLrJzn9gmea4UO5Wf.lhe1F5QYsqm.g57aFdsgwRb1yvq', '0', '2', '127.0.0.1', '2026-01-12 13:35:52', NULL, '19157727791', '2026-01-12 12:51:29', '', '2026-01-12 13:35:52', NULL);
INSERT INTO `sys_user` VALUES (224, 139, '2020710414', '李尚恩', '00', '', '', '0', '', '$2a$10$.mpReCUzOBDOIwZSHVfS8ODV86zMH7My1xtfwKDDB8UwA3yJXNJAS', '0', '2', '127.0.0.1', '2026-01-12 13:14:31', NULL, '19157727791', '2026-01-12 12:51:29', '', '2026-01-12 13:14:31', NULL);
INSERT INTO `sys_user` VALUES (225, 139, '2020710415', '李妍儿', '00', '', '', '0', '', '$2a$10$i0BP5oS2ni/4KJVfrZxpzOcrAD0Mez51PuAD/6lAJgSBc4Cc7n4Ra', '0', '2', '127.0.0.1', '2026-01-12 13:46:25', NULL, '19157727791', '2026-01-12 12:51:29', '', '2026-01-12 13:46:25', NULL);
INSERT INTO `sys_user` VALUES (226, 139, '2020710416', '励宸煜', '00', '', '', '0', '', '$2a$10$k5elF7AvSUi/c5npFobp/O5iWdPz3.lAkMQc5hGnherXPjbLRhWmW', '0', '2', '127.0.0.1', '2026-01-12 13:15:25', NULL, '19157727791', '2026-01-12 12:51:29', '', '2026-01-12 13:15:24', NULL);
INSERT INTO `sys_user` VALUES (227, 139, '2020710417', '林鑫博', '00', '', '', '0', '', '$2a$10$H2TdDIoRH83foHzgoh9DLusYq1E778vlcn5qAcqngQ/v4uZTQvKoG', '0', '2', '127.0.0.1', '2026-01-12 13:51:05', NULL, '19157727791', '2026-01-12 12:51:29', '', '2026-01-12 13:51:05', NULL);
INSERT INTO `sys_user` VALUES (228, 139, '2020710418', '林意涵', '00', '', '', '0', '', '$2a$10$oPtrtOCaiawBfIDistkSb.h.ROe9igkFCDPir9nx2n7O4ZLU3wg2G', '0', '2', '127.0.0.1', '2026-01-12 13:50:08', NULL, '19157727791', '2026-01-12 12:51:30', '', '2026-01-12 13:50:08', NULL);
INSERT INTO `sys_user` VALUES (229, 139, '2020710419', '刘玟言', '00', '', '', '0', '', '$2a$10$BmveGDdLjLvfiq3DJkHAnePwXkIDbP3KOev0guRRsjIVDdRPS/KlS', '0', '2', '127.0.0.1', '2026-01-12 13:43:20', NULL, '19157727791', '2026-01-12 12:51:30', '', '2026-01-12 13:43:20', NULL);
INSERT INTO `sys_user` VALUES (230, 139, '2020710420', '陆施羽', '00', '', '', '0', '', '$2a$10$d5ewVjsb6b9VkqMBQBW56.jGPo6u7Ag2jWdfhWNGg7EUpIShToA5u', '0', '2', '127.0.0.1', '2026-01-12 13:43:51', NULL, '19157727791', '2026-01-12 12:51:30', '', '2026-01-12 13:43:51', NULL);
INSERT INTO `sys_user` VALUES (231, 139, '2020710421', '马韶怿', '00', '', '', '0', '', '$2a$10$JGU6nR4ZPsX/n0VV09ibROzsZh009M8YS4hBlN3TNxZhpAPqM0TWy', '0', '2', '127.0.0.1', '2026-01-12 13:45:19', NULL, '19157727791', '2026-01-12 12:51:30', '', '2026-01-12 13:45:18', NULL);
INSERT INTO `sys_user` VALUES (232, 139, '2020710422', '孟小博', '00', '', '', '0', '', '$2a$10$Kq1HDmdRIGKdw2Ud2KgLIe4cpWbKJ8/XAW/RFCVFlK0gCQcwUlTfC', '0', '2', '127.0.0.1', '2026-01-12 13:48:33', NULL, '19157727791', '2026-01-12 12:51:30', '', '2026-01-12 13:48:33', NULL);
INSERT INTO `sys_user` VALUES (233, 139, '2020710423', '邵嘉翔', '00', '', '', '0', '', '$2a$10$YA/df4Ua/Lqh4nLs1YfZeuyGlusw59KB.jouq0th73Uii/wCZSvA.', '0', '2', '127.0.0.1', '2026-01-12 13:43:23', NULL, '19157727791', '2026-01-12 12:51:30', '', '2026-01-12 13:43:22', NULL);
INSERT INTO `sys_user` VALUES (234, 139, '2020710424', '王子瑜', '00', '', '', '0', '', '$2a$10$oc1tBVCY6WZbWehndNc1QeKein2yE3db0.N3r/CQsvNfF8FTXf21i', '0', '2', '127.0.0.1', '2026-01-12 13:49:08', NULL, '19157727791', '2026-01-12 12:51:30', '', '2026-01-12 13:49:07', NULL);
INSERT INTO `sys_user` VALUES (235, 139, '2020710425', '吴鼎轩', '00', '', '', '0', '', '$2a$10$xRvSX91cdz0O8qE8txJ/3.tsmf1G5ypiIL2/PQfwz.2Es7oA6q32C', '0', '2', '127.0.0.1', '2026-01-12 13:50:29', NULL, '19157727791', '2026-01-12 12:51:30', '', '2026-01-12 13:50:28', NULL);
INSERT INTO `sys_user` VALUES (236, 139, '2020710426', '谢佳芯', '00', '', '', '0', '', '$2a$10$GAcNnq6MaCriK6Z5xla5ZOIEn8yk2.rqJF7OPOrdxIM44WVGmd4Xy', '0', '2', '127.0.0.1', '2026-01-12 13:52:29', NULL, '19157727791', '2026-01-12 12:51:30', '', '2026-01-12 13:52:29', NULL);
INSERT INTO `sys_user` VALUES (237, 139, '2020710427', '徐辰若', '00', '', '', '0', '', '$2a$10$INpMMgvPz.ClcGmpd7R82ui64.SQO5yPQ1LW4S0D9hOrCO0VyaF0K', '0', '2', '127.0.0.1', '2026-01-12 13:50:55', NULL, '19157727791', '2026-01-12 12:51:30', '', '2026-01-12 13:50:55', NULL);
INSERT INTO `sys_user` VALUES (238, 139, '2020710428', '余昊璟', '00', '', '', '0', '', '$2a$10$rMFy6yEPkEGbaws3V3x9/.yNbO/GyAUtgmZR4985fi4GbGpo2IHRS', '0', '2', '127.0.0.1', '2026-01-12 13:50:28', NULL, '19157727791', '2026-01-12 12:51:30', '', '2026-01-12 13:50:28', NULL);
INSERT INTO `sys_user` VALUES (239, 139, '2020710429', '张晨曦', '00', '', '', '0', '', '$2a$10$g5utycPQLgG045D0adD8GO652FFX03j2u.LjUSNhZSQY1m.lbMIWC', '0', '2', '127.0.0.1', '2026-01-12 13:15:45', NULL, '19157727791', '2026-01-12 12:51:31', '', '2026-01-12 13:15:45', NULL);
INSERT INTO `sys_user` VALUES (240, 139, '2020710430', '张画', '00', '', '', '0', '', '$2a$10$H6ghCzNxvjDisn.QahKOdua/KHhqyTx0TMiJA1ZNP8YSO94pzG.2m', '0', '2', '127.0.0.1', '2026-01-12 13:52:44', NULL, '19157727791', '2026-01-12 12:51:31', '', '2026-01-12 13:52:43', NULL);
INSERT INTO `sys_user` VALUES (241, 139, '2020710431', '张天佑', '00', '', '', '0', '', '$2a$10$hiigTJW5Ss2uvvo8M2JTxOrTHl4M9BhkvcacDx/AU2Tp38DXaJ.t.', '0', '2', '127.0.0.1', '2026-01-12 13:51:28', NULL, '19157727791', '2026-01-12 12:51:31', '', '2026-01-12 13:51:27', NULL);
INSERT INTO `sys_user` VALUES (242, 139, '2020710432', '张桐瑶', '00', '', '', '0', '', '$2a$10$BeFaASz3eoKWCoNslYb/A.ncaPRu8TFXkssEdZBaj40QjHyCl5.WC', '0', '2', '127.0.0.1', '2026-01-12 13:14:25', NULL, '19157727791', '2026-01-12 12:51:31', '', '2026-01-12 13:14:25', NULL);
INSERT INTO `sys_user` VALUES (243, 139, '2020710433', '戴梓悦', '00', '', '', '0', '', '$2a$10$dlC4Ov5BeWo08bqXirHTTOllvH3GMw/eB5BCTKWCRzWmMwFtxL7OG', '0', '2', '127.0.0.1', '2026-01-12 13:16:14', NULL, '19157727791', '2026-01-12 12:51:31', '', '2026-01-12 13:16:13', NULL);
INSERT INTO `sys_user` VALUES (244, 139, '2020710434', '张梓铭', '00', '', '', '0', '', '$2a$10$di80iat/azaiagA0.5v16eLTSgDpZAf8x5zFm.6f3B25PmwSA4n/m', '0', '2', '127.0.0.1', '2026-01-12 13:14:56', NULL, '19157727791', '2026-01-12 12:51:31', '', '2026-01-12 13:14:56', NULL);
INSERT INTO `sys_user` VALUES (245, 139, '2020710435', '郑若秋', '00', '', '', '0', '', '$2a$10$itq/n4gsa1IOlLHvfYZQDuR7fbCGx1yB4Ep6MYC/vykBws0rAPqkO', '0', '2', '127.0.0.1', '2026-01-12 13:43:40', NULL, '19157727791', '2026-01-12 12:51:31', '', '2026-01-12 13:43:40', NULL);
INSERT INTO `sys_user` VALUES (246, 139, '2020710436', '周宸乐', '00', '', '', '0', '', '$2a$10$EgOPm2bB/bn7Dz5aCrUrGufGTuw4tn0olTUpcR76o6FjgmPYUUq/i', '0', '2', '127.0.0.1', '2026-01-12 13:15:29', NULL, '19157727791', '2026-01-12 12:51:31', '', '2026-01-12 13:15:28', NULL);
INSERT INTO `sys_user` VALUES (247, 139, '2020710437', '周镇宇', '00', '', '', '0', '', '$2a$10$Vu8Z5IMzVH7HORL9BDiW9uKGU1j1c2qKbWakeSF2Ti2.VzrzBQsQu', '0', '2', '127.0.0.1', '2026-01-12 13:45:44', NULL, '19157727791', '2026-01-12 12:51:31', '', '2026-01-12 13:45:44', NULL);
INSERT INTO `sys_user` VALUES (248, 139, '2020710438', '朱优璇', '00', '', '', '0', '', '$2a$10$TCQtgPiPcOf7qOZzppkmMulOiFAQXiw.1ZFaLJDOMsoTlrB1XJwoq', '0', '2', '127.0.0.1', '2026-01-12 13:43:00', NULL, '19157727791', '2026-01-12 12:51:31', '', '2026-01-12 13:43:00', NULL);
INSERT INTO `sys_user` VALUES (249, 139, '2020710439', '李君航', '00', '', '', '0', '', '$2a$10$zGliiUNFyGijJoHtTl6mreaMzizuaSGwmIqZ7IhBYu3nLDP7duE4C', '0', '2', '127.0.0.1', '2026-01-12 13:14:40', NULL, '19157727791', '2026-01-12 12:51:31', '', '2026-01-12 13:14:40', NULL);
INSERT INTO `sys_user` VALUES (250, 139, '2020710440', '吴恪', '00', '', '', '0', '', '$2a$10$WMYjew8c2RQpNgPVmN4wj./EspMcRlzXcjPCOL89v33Obi8in5efO', '0', '2', '127.0.0.1', '2026-01-12 13:14:49', NULL, '19157727791', '2026-01-12 12:51:32', '', '2026-01-12 13:14:48', NULL);
INSERT INTO `sys_user` VALUES (251, 139, '2020710441', '郑凯瑞', '00', '', '', '0', '', '$2a$10$eaEFERHWebTJDtFTEgV9NefYt77evqcIxXIRnDTBnFmMcx1LJjeiG', '0', '2', '127.0.0.1', '2026-01-12 13:14:32', NULL, '19157727791', '2026-01-12 12:51:32', '', '2026-01-12 13:14:31', NULL);
INSERT INTO `sys_user` VALUES (252, 139, '2020710442', '朱宸妍', '00', '', '', '0', '', '$2a$10$8EgU794xiZWa76PXltGtquQiSVXutP9SKgQaAdl3WWsdsZ/kvtez2', '0', '2', '127.0.0.1', '2026-01-12 13:51:57', NULL, '19157727791', '2026-01-12 12:51:32', '', '2026-01-12 13:51:56', NULL);
INSERT INTO `sys_user` VALUES (253, 139, '2020710443', '杨淦淳', '00', '', '', '0', '', '$2a$10$HehrrfqKBXIC5dsHzXahnOdA0TNSBqYZgW.oKa/KPWy11Kd6gJJp2', '0', '2', '127.0.0.1', '2026-01-12 13:15:57', NULL, '19157727791', '2026-01-12 12:51:32', '', '2026-01-12 13:15:56', NULL);
INSERT INTO `sys_user` VALUES (254, 139, '2020710401', '曹紫云', '00', '', '', '0', '', '$2a$10$TmXCg4n9wB.lsP/3bvc.TOcNbtaMTvSsfrPGRFUpLGkBv9jTjgnQe', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:53', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (255, 139, '2020710402', '陈芊然', '00', '', '', '0', '', '$2a$10$qN12gAvj5r/FUtwV0mZ2oeOsaxqnXeT2hjR8fioFwB3.gQ4El5FFW', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:53', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (256, 139, '2020710403', '陈心阳', '00', '', '', '0', '', '$2a$10$i0qsz3hDsjWAi0KQOSpb/OgbfMDxBG689sH22Owk9J9NJmSgEJGq6', '0', '0', '127.0.0.1', '2026-01-13 11:15:15', NULL, '19157727791', '2026-01-12 14:11:53', '', '2026-01-13 11:15:14', NULL);
INSERT INTO `sys_user` VALUES (257, 139, '2020710404', '董洛熙', '00', '', '', '0', '', '$2a$10$/sJo5NIPLhgpl1mgJKaE5uOQyvHui2oyU/B0JQMDQZHS1WseqRTru', '0', '0', '127.0.0.1', '2026-01-13 11:16:22', NULL, '19157727791', '2026-01-12 14:11:54', '', '2026-01-13 11:16:22', NULL);
INSERT INTO `sys_user` VALUES (258, 139, '2020710405', '董悠然', '00', '', '', '0', '', '$2a$10$BSTwKVZFAu4KPfSkefxO6.rS/IaCVS5Tq3XoYHBebQOeTPgR952Dm', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:54', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (259, 139, '2020710407', '顾丁奕', '00', '', '', '0', '', '$2a$10$OqcbpSVLZ/E5s46inF8Z3u.rvCPtFWpnjP5xelzjWYNsgJaCX6/Sy', '0', '0', '127.0.0.1', '2026-01-13 12:58:43', NULL, '19157727791', '2026-01-12 14:11:54', '', '2026-01-13 12:58:42', NULL);
INSERT INTO `sys_user` VALUES (260, 139, '2020710408', '韩芷若', '00', '', '', '0', '', '$2a$10$JEEN5sNYXfbupoJw5D/4keezlGZglbDWuePHb2WSQgxX/MmOh0AUe', '0', '0', '127.0.0.1', '2026-01-13 12:59:34', NULL, '19157727791', '2026-01-12 14:11:54', '', '2026-01-13 12:59:33', NULL);
INSERT INTO `sys_user` VALUES (261, 139, '2020710409', '黄博壹', '00', '', '', '0', '', '$2a$10$R9gRqd06LeRU.3mv7Ff2CeoLzwxHRexaw1cZc/0qhIRGiphqOFYjm', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:54', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (262, 139, '2020710410', '黄菀宁', '00', '', '', '0', '', '$2a$10$rZpeB/DAv4kOQdlNCWPquuaEnx/888KSfwrwO1IYBy.6ViMvgvFy.', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:54', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (263, 139, '2020710411', '金致远', '00', '', '', '0', '', '$2a$10$1UAgf4MNhxRsITX5LeL9pOxX0nCgmAjc67MZv1WZ.s3ZPmNHYDMNC', '0', '0', '127.0.0.1', '2026-01-13 11:36:54', NULL, '19157727791', '2026-01-12 14:11:54', '', '2026-01-13 11:36:54', NULL);
INSERT INTO `sys_user` VALUES (264, 139, '2020710412', '金子珺', '00', '', '', '0', '', '$2a$10$IQqWMYDVQEymAQlzE6Ax..ph6Q610EIkeopwOoKyCKYJbSn9.pukC', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:54', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (265, 139, '2020710413', '柯恩哲', '00', '', '', '0', '', '$2a$10$PPnirVHkY5hzCxfbTmt6rua8nb2sQJuCFjObMT/jXR4ihwxLpgvce', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:54', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (266, 139, '2020710414', '赖佳彬', '00', '', '', '0', '', '$2a$10$Ux8tepodhS1.j0Lkywc7ue/6c9VYECR.AGlc.aSD.R3V4etAp9A6C', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:54', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (267, 139, '2020710415', '李尚恩', '00', '', '', '0', '', '$2a$10$qOMaIKSIxuRNTM9Lbd86w.SQDfI4ySe9.yx1P.PU5LSomHiHnpxzu', '0', '0', '127.0.0.1', '2026-01-13 13:01:45', NULL, '19157727791', '2026-01-12 14:11:54', '', '2026-01-13 13:01:45', NULL);
INSERT INTO `sys_user` VALUES (268, 139, '2020710416', '李妍儿', '00', '', '', '0', '', '$2a$10$2A/XsqbaXhBQCah/h9.hIexsd6NCLsQKQMjKgzc3uGY0xEaMZAcVa', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:54', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (269, 139, '2020710417', '励宸煜', '00', '', '', '0', '', '$2a$10$LrrWkDfi4NgwMZPaECjT9Od6PtC./bqTY6soOjcnMO1JcR9a977xy', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:54', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (270, 139, '2020710418', '林鑫博', '00', '', '', '0', '', '$2a$10$ZyjnGwkUTq1dj2DZNHwWdOq0F.ADhC5h1308eL4i9zL/mer0mVvk.', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:55', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (271, 139, '2020710419', '林意涵', '00', '', '', '0', '', '$2a$10$mBoFn8gjAgJFnHNIkKEwr.8ydEucF4tHEoQqFH1vUu2XWcOFpaqQu', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:55', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (272, 139, '2020710420', '刘玟言', '00', '', '', '0', '', '$2a$10$5vSMrWKJKW8.u47Ass7.geUk33qDTQyfTrA.6q8TCiaNeAztpH.s2', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:55', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (273, 139, '2020710421', '陆施羽', '00', '', '', '0', '', '$2a$10$Qos85poPEsXpIMQAqxSeXeCwq/18KhL9xYEpgsFMsaOMFFGOFNGBe', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:55', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (274, 139, '2020710422', '马韶怿', '00', '', '', '0', '', '$2a$10$RjO83SQUxYCn/tXp6SC2DepxZgQLho7R.TkTd9GzkvfJ/SfQHkYVi', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:55', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (275, 139, '2020710423', '孟小博', '00', '', '', '0', '', '$2a$10$/lmgOQB/e04bo1qNPcAIKOpXlVi4bmToPXA6o0NA6CzOuVtLPmrzS', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:55', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (276, 139, '2020710424', '邵嘉翔', '00', '', '', '0', '', '$2a$10$5p9UCM4d3csU7KOI6GtuB.zi84njg.pdDProNYIdgds9U3PtSPw9y', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:55', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (277, 139, '2020710425', '王子瑜', '00', '', '', '0', '', '$2a$10$LSGD2woANYlrOu.WhAm6zOK4kiwIUoRdOktRs52rQQNCht/01qptW', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:55', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (278, 139, '2020710426', '吴鼎轩', '00', '', '', '0', '', '$2a$10$ARLyiuXwzsaNHjINpZqRoOn7Gr.xQALYM85cKzT59r.p06QUL4TsS', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:55', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (279, 139, '2020710427', '谢佳芯', '00', '', '', '0', '', '$2a$10$YRb3U6GpdmJoYlZLry/pw.XOzlMs4zKcny.p4Dz87sGUW4dUf0w7a', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:55', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (280, 139, '2020710428', '徐辰若', '00', '', '', '0', '', '$2a$10$xDnq2xLPFMBSUkXTFVnBA.RJ/XfMKt3cLQpSdu2BoIzfghFwTIfnu', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:55', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (281, 139, '2020710429', '余昊璟', '00', '', '', '0', '', '$2a$10$Q3WrAhD8LCANLkML77CoF.M8S9CNqeQiNKbJBuehxqyzml6jjhgve', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:55', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (282, 139, '2020710431', '张晨曦', '00', '', '', '0', '', '$2a$10$.QUfS4CcK40O4HKOOgA1ButWVfN3Zeb3aSDCYvMdIW6zu/cYs/sRu', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:55', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (283, 139, '2020710432', '张画', '00', '', '', '0', '', '$2a$10$fbUINuNDtdsF5foNvuV/be/cumOSzvB4NzY0s8ryts3wTkC3ex5Le', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:56', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (284, 139, '2020710433', '张天佑', '00', '', '', '0', '', '$2a$10$SFPLWrB.MiuDoCUZFhLeYu4Ly1NAVlsjP/B4d1FJIUh/aEb4M3aDu', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:56', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (285, 139, '2020710434', '张桐瑶', '00', '', '', '0', '', '$2a$10$oLl8Qaz89YSBpCkR5.ngQuUmHtmxHqEVR/uOVQeXZzrKsud1H5kAO', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:56', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (286, 139, '2020710435', '戴梓悦', '00', '', '', '0', '', '$2a$10$2KdfYov7.YvvGX9/kdAFwuRPHzFWUVtDONANeTZQyj2ShNpkIWnK.', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:56', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (287, 139, '2020710436', '张梓铭', '00', '', '', '0', '', '$2a$10$MvdEvRcb0MKv5/ADDQY6bu3mHSFU6IYANik7UDqLw7QqRhiUMJW12', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:56', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (288, 139, '2020710437', '郑若秋', '00', '', '', '0', '', '$2a$10$7JEsh/8AJLXoTCkLi0HzuO1mzPH9584MRLezJA6eeT7MJ406RIeNy', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:56', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (289, 139, '2020710438', '周宸乐', '00', '', '', '0', '', '$2a$10$2AeW5NdNaQix2mh9iCR5.eH2x52QCJHwiqop.uXt/m8Iw9kVbyhh.', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:56', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (290, 139, '2020710439', '周镇宇', '00', '', '', '0', '', '$2a$10$pdoGsnr2IVnzeSh50ps1h.PLULOrKc2yR.J02WvOLGUNgK6.Yvqne', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:56', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (291, 139, '2020710440', '朱优璇', '00', '', '', '0', '', '$2a$10$0c9Ty0DJtEpKdCxoHRzo3esJ.aFgr6jZGeUARDwTQ0lZrIzOwJMDK', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:56', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (292, 139, '2020710441', '李君航', '00', '', '', '0', '', '$2a$10$0yYnPqZOYygPdjDAf2aH9OQCTwrWYbOmgOl.4HLfF9O1ceNMWC6by', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:56', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (293, 139, '2020710430', '吴恪', '00', '', '', '0', '', '$2a$10$uTbIx14Zq6E56/BOPiQ./.WMpeD.jb4JebXaph4HByVg07vIwr2QK', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:56', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (294, 139, '2020710406', '郑凯瑞', '00', '', '', '0', '', '$2a$10$EpoytleTi7cRZ3FvMs4xNO6ol3fD.m8dqu27jl2IarKXyeiwrUH.m', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:56', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (295, 139, '2020710443', '朱宸妍', '00', '', '', '0', '', '$2a$10$Yvrz.PgkIU9p8xOSg9gSr.dDSbPIUGuiWGxDw/Ly9Ob7RN3a0tYDi', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:56', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (296, 139, '2020710442', '杨淦淳', '00', '', '', '0', '', '$2a$10$Pq4qOqls9nwSUAu9InxI8e64mufPT7G/u0R8jya.57RuCAeK3bzvW', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:11:57', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (297, 139, '2020710201', '柴兆俊', '00', '', '', '0', '', '$2a$10$7wRxTzlF951KjrThWgHPEuQ0cw50zb2UyKDlvGDfh9ko.suYynBKq', '0', '0', '127.0.0.1', '2026-01-13 13:20:40', NULL, '19157727791', '2026-01-12 14:21:14', '', '2026-01-13 13:20:40', NULL);
INSERT INTO `sys_user` VALUES (298, 139, '2020710202', '陈增宇', '00', '', '', '0', '', '$2a$10$y4VLV8rApltdORSffoEbHOji1pRU9FoGawMVqI7ZXXRu7oVkFdK2.', '0', '0', '127.0.0.1', '2026-01-13 13:20:25', NULL, '19157727791', '2026-01-12 14:21:14', '', '2026-01-13 13:20:25', NULL);
INSERT INTO `sys_user` VALUES (299, 139, '2020710203', '陈志航', '00', '', '', '0', '', '$2a$10$bULuSjH1PGRS7hlv/7/K2OSiZ9giO/Mwim4corzjTLyx2UQFZjRR2', '0', '0', '127.0.0.1', '2026-01-13 13:20:46', NULL, '19157727791', '2026-01-12 14:21:15', '', '2026-01-13 13:20:45', NULL);
INSERT INTO `sys_user` VALUES (300, 139, '2020710204', '戴彦哲', '00', '', '', '0', '', '$2a$10$qMK4ROAWiXYsiyoHib61jenINY0B0QAJvbtFJEYQ5qrbufK.X9KWW', '0', '0', '127.0.0.1', '2026-01-13 13:20:22', NULL, '19157727791', '2026-01-12 14:21:15', '', '2026-01-13 13:20:21', NULL);
INSERT INTO `sys_user` VALUES (301, 139, '2020710205', '方俊淇', '00', '', '', '0', '', '$2a$10$A1X18J1DdXdmG5NPDgmUHewxHVmHubgrOVcPJu1Go66FROK900nBG', '0', '0', '127.0.0.1', '2026-01-13 13:20:23', NULL, '19157727791', '2026-01-12 14:21:15', '', '2026-01-13 13:20:23', NULL);
INSERT INTO `sys_user` VALUES (302, 139, '2020710206', '金叙', '00', '', '', '0', '', '$2a$10$Akj4U.KBAkAyR1e8Hum3s.P6rfGr7bN5QpYV2aMY.fyDPZBnQrb1K', '0', '0', '127.0.0.1', '2026-01-13 13:50:47', NULL, '19157727791', '2026-01-12 14:21:15', '', '2026-01-13 13:50:46', NULL);
INSERT INTO `sys_user` VALUES (303, 139, '2020710207', '李博文', '00', '', '', '0', '', '$2a$10$mdiL7omd7eMd.oF/qpORBOHnV3EM32dSLdPEwm2H15Tmwk/E6Uoma', '0', '0', '127.0.0.1', '2026-01-13 13:50:29', NULL, '19157727791', '2026-01-12 14:21:15', '', '2026-01-13 13:50:28', NULL);
INSERT INTO `sys_user` VALUES (304, 139, '2020710208', '李奕翔', '00', '', '', '0', '', '$2a$10$ikrkgZrH7sQX9OAR3/lUpulBE1CYTVFeNd7QRpPwci084B.CosqjK', '0', '0', '127.0.0.1', '2026-01-13 13:50:41', NULL, '19157727791', '2026-01-12 14:21:15', '', '2026-01-13 13:50:41', NULL);
INSERT INTO `sys_user` VALUES (305, 139, '2020710209', '李梓烁', '00', '', '', '0', '', '$2a$10$rs2UqCveebXm1SQzPLDjGeWBAqQuf5EeEab8/XkqBmNrMIJx71Ztq', '0', '0', '127.0.0.1', '2026-01-13 13:50:43', NULL, '19157727791', '2026-01-12 14:21:15', '', '2026-01-13 13:50:42', NULL);
INSERT INTO `sys_user` VALUES (306, 139, '2020710210', '史昊鑫', '00', '', '', '0', '', '$2a$10$dUXJughZ4FSYus.5YMjbR.s3mjJxZJekA3/UcOLr.PW.9AJnlmg.O', '0', '0', '127.0.0.1', '2026-01-13 13:42:49', NULL, '19157727791', '2026-01-12 14:21:15', '', '2026-01-13 13:42:49', NULL);
INSERT INTO `sys_user` VALUES (307, 139, '2020710211', '舒俊博', '00', '', '', '0', '', '$2a$10$10cd8GX2BczPxp3A8xOLouLw4NCYbMR2JTgKly.3J9Wfnm.0z09ci', '0', '0', '127.0.0.1', '2026-01-13 13:50:34', NULL, '19157727791', '2026-01-12 14:21:15', '', '2026-01-13 13:50:33', NULL);
INSERT INTO `sys_user` VALUES (308, 139, '2020710212', '屠溢航', '00', '', '', '0', '', '$2a$10$ds2.gNJoCSxOCdxJtrqY3eUqj5BbAi6Cc0ORdgByOT/tksjvn4LSy', '0', '0', '127.0.0.1', '2026-01-13 13:20:28', NULL, '19157727791', '2026-01-12 14:21:15', '', '2026-01-13 13:20:27', NULL);
INSERT INTO `sys_user` VALUES (309, 139, '2020710213', '奚景贤', '00', '', '', '0', '', '$2a$10$FkpIWtuOE9SW45ZbanEIn.ecWD..hNdUw6HFEGhR2l6ykTdlK99ZS', '0', '0', '127.0.0.1', '2026-01-13 13:20:22', NULL, '19157727791', '2026-01-12 14:21:15', '', '2026-01-13 13:20:22', NULL);
INSERT INTO `sys_user` VALUES (310, 139, '2020710214', '徐郁骁', '00', '', '', '0', '', '$2a$10$4rrwxjGL64Sf0ziCWy07LujIsOG1ZNNB0lx76UlEAHjezd2.iRRwu', '0', '0', '127.0.0.1', '2026-01-13 13:20:38', NULL, '19157727791', '2026-01-12 14:21:15', '', '2026-01-13 13:20:38', NULL);
INSERT INTO `sys_user` VALUES (311, 139, '2020710215', '许梓晨', '00', '', '', '0', '', '$2a$10$AjgCNsbN0MRdPL6LRiKJDetXSAK/d24yf4n6Ziy1xIAOjyNA8jS5.', '0', '0', '127.0.0.1', '2026-01-13 13:49:59', NULL, '19157727791', '2026-01-12 14:21:15', '', '2026-01-13 13:49:59', NULL);
INSERT INTO `sys_user` VALUES (312, 139, '2020710216', '郑翎恺', '00', '', '', '0', '', '$2a$10$Jffz.0Q84BYNA4zUvKPA3upU/RzjOjONdw6SUeim/DW3uQ1zqV1.y', '0', '0', '127.0.0.1', '2026-01-13 13:20:36', NULL, '19157727791', '2026-01-12 14:21:16', '', '2026-01-13 13:20:36', NULL);
INSERT INTO `sys_user` VALUES (313, 139, '2020710217', '钟亦程', '00', '', '', '0', '', '$2a$10$gLdLY1GwiI8T2qwrCqP5v.mlj/ixtDUFRV8PibJV/y/X.SMvfKxg.', '0', '0', '127.0.0.1', '2026-01-13 13:49:57', NULL, '19157727791', '2026-01-12 14:21:16', '', '2026-01-13 13:49:57', NULL);
INSERT INTO `sys_user` VALUES (314, 139, '2020710218', '戴瑾言', '00', '', '', '0', '', '$2a$10$uKrpM58FxJAp1Yv4UIpMvepYOc88jMCWLo4AgOw5wd.Nen7W7i4vu', '0', '0', '127.0.0.1', '2026-01-13 13:20:35', NULL, '19157727791', '2026-01-12 14:21:16', '', '2026-01-13 13:20:34', NULL);
INSERT INTO `sys_user` VALUES (315, 139, '2020710219', '陈静瑶', '00', '', '', '0', '', '$2a$10$EbKZD94af0tTfY2dxx6t4ecINNluxiE9cV7WgFxCkjm6P29b9RFJu', '0', '0', '127.0.0.1', '2026-01-13 13:50:36', NULL, '19157727791', '2026-01-12 14:21:16', '', '2026-01-13 13:50:36', NULL);
INSERT INTO `sys_user` VALUES (316, 139, '2020710220', '付姝绚', '00', '', '', '0', '', '$2a$10$2aeSRpy4YWiCqJeiX77gLunW8GVbgE36nWdfp.Yntjj1UV4QZRGZe', '0', '0', '127.0.0.1', '2026-01-13 13:20:38', NULL, '19157727791', '2026-01-12 14:21:16', '', '2026-01-13 13:20:37', NULL);
INSERT INTO `sys_user` VALUES (317, 139, '2020710222', '金奕萱', '00', '', '', '0', '', '$2a$10$12hsXZxzLZ4ACqRntJDGpepL.hth5O0Une3F/S3pIm8zpKXdOu3ye', '0', '0', '127.0.0.1', '2026-01-13 13:21:33', NULL, '19157727791', '2026-01-12 14:21:16', '', '2026-01-13 13:21:33', NULL);
INSERT INTO `sys_user` VALUES (318, 139, '2020710223', '韩金雅', '00', '', '', '0', '', '$2a$10$5BllghAETvaRZOcdEUYDROy7Q3HSqtOQrCZTC/emCOUCOhna9wtui', '0', '0', '127.0.0.1', '2026-01-13 13:20:33', NULL, '19157727791', '2026-01-12 14:21:16', '', '2026-01-13 13:20:33', NULL);
INSERT INTO `sys_user` VALUES (319, 139, '2020710224', '罗诗雅', '00', '', '', '0', '', '$2a$10$Nlro9sNzbcd8tC/e9N3wsO31voHLRvJncu9hFK3B0cTbQhwvNahWm', '0', '0', '127.0.0.1', '2026-01-13 13:43:19', NULL, '19157727791', '2026-01-12 14:21:16', '', '2026-01-13 13:43:19', NULL);
INSERT INTO `sys_user` VALUES (320, 139, '2020710225', '沈熙涵', '00', '', '', '0', '', '$2a$10$LbmGKr0qW00ShTFemXPB0uR1wCWJMGNZPd/LeJ45bf/uvoemq9M.m', '0', '0', '127.0.0.1', '2026-01-13 13:50:34', NULL, '19157727791', '2026-01-12 14:21:16', '', '2026-01-13 13:50:33', NULL);
INSERT INTO `sys_user` VALUES (321, 139, '2020710226', '王晨萱', '00', '', '', '0', '', '$2a$10$GY1yxpu73azSzKhXO1q5se1WQMoT70a5CvLZcDAVkPZ/4fkOWccIq', '0', '0', '127.0.0.1', '2026-01-13 13:20:25', NULL, '19157727791', '2026-01-12 14:21:16', '', '2026-01-13 13:20:24', NULL);
INSERT INTO `sys_user` VALUES (322, 139, '2020710227', '王晨雨', '00', '', '', '0', '', '$2a$10$TItzOMOlRnXJZLLu0Kui4Oy5MUFTKm6dfDAVRAggBs/.XVrwcceVO', '0', '0', '127.0.0.1', '2026-01-13 13:45:23', NULL, '19157727791', '2026-01-12 14:21:16', '', '2026-01-13 13:45:23', NULL);
INSERT INTO `sys_user` VALUES (323, 139, '2020710228', '王晶莹', '00', '', '', '0', '', '$2a$10$mYcZ.ELHAWJguR0i4Ku6KuMRlMxpmWgSsiyWwSo3i8hFG/ynyugwW', '0', '0', '127.0.0.1', '2026-01-13 13:50:30', NULL, '19157727791', '2026-01-12 14:21:16', '', '2026-01-13 13:50:29', NULL);
INSERT INTO `sys_user` VALUES (324, 139, '2020710229', '吴佳漩', '00', '', '', '0', '', '$2a$10$C2RvqX30PkfMTP/8b7GDpuJ4VQZ3bX1Q93oLIJf2E6vhTyDZ9osJO', '0', '0', '127.0.0.1', '2026-01-13 13:50:18', NULL, '19157727791', '2026-01-12 14:21:16', '', '2026-01-13 13:50:17', NULL);
INSERT INTO `sys_user` VALUES (325, 139, '2020710230', '项梓倚', '00', '', '', '0', '', '$2a$10$pPXUmgzgMGrvWBIPFP5vqeJGp9p0q7neYK5RuwRJn.JYz.SJcyPFe', '0', '0', '127.0.0.1', '2026-01-13 13:21:45', NULL, '19157727791', '2026-01-12 14:21:16', '', '2026-01-13 13:21:45', NULL);
INSERT INTO `sys_user` VALUES (326, 139, '2020710231', '叶梓诺', '00', '', '', '0', '', '$2a$10$Uy5DQXvkru6mFnX48o0D1OVKiB5zld/hJhFCNQXf11LmqfdyU.w/u', '0', '0', '127.0.0.1', '2026-01-13 13:50:13', NULL, '19157727791', '2026-01-12 14:21:17', '', '2026-01-13 13:50:12', NULL);
INSERT INTO `sys_user` VALUES (327, 139, '2020710232', '应雨铭', '00', '', '', '0', '', '$2a$10$zzdxpXBpKCkq7BUQcm54CerV85TgGZFHfnawZCtpRBxikjN82AK2i', '0', '0', '127.0.0.1', '2026-01-13 13:49:44', NULL, '19157727791', '2026-01-12 14:21:17', '', '2026-01-13 13:49:43', NULL);
INSERT INTO `sys_user` VALUES (328, 139, '2020710233', '张嘉颖', '00', '', '', '0', '', '$2a$10$aqWefmkNqPjwepeWHuq1kOy76mgRCFaU5efuawEyVBrYQ7R27pK5S', '0', '0', '127.0.0.1', '2026-01-13 13:50:09', NULL, '19157727791', '2026-01-12 14:21:17', '', '2026-01-13 13:50:09', NULL);
INSERT INTO `sys_user` VALUES (329, 139, '2020710234', '张缈缈', '00', '', '', '0', '', '$2a$10$d.XnjzXR7xSMd6VFd/Wq1uKwttd1.stgEDM5qlfe8BHSbFfN0VMJO', '0', '0', '127.0.0.1', '2026-01-13 13:20:30', NULL, '19157727791', '2026-01-12 14:21:17', '', '2026-01-13 13:20:30', NULL);
INSERT INTO `sys_user` VALUES (330, 139, '2020710235', '章诺瞳', '00', '', '', '0', '', '$2a$10$fZmHzlvACGgwWUf8EIgZJ.4mGDvya8aL1KPhaQhNTE0nu2fu908f.', '0', '0', '127.0.0.1', '2026-01-13 13:42:54', NULL, '19157727791', '2026-01-12 14:21:17', '', '2026-01-13 13:42:53', NULL);
INSERT INTO `sys_user` VALUES (331, 139, '2020710236', '郑雯鑫', '00', '', '', '0', '', '$2a$10$xKE5qcE/DncrcoEmq/ZjMOedaLMEluODW2L/2WqLxh9UPzT1VRkZm', '0', '0', '127.0.0.1', '2026-01-13 13:43:03', NULL, '19157727791', '2026-01-12 14:21:17', '', '2026-01-13 13:43:03', NULL);
INSERT INTO `sys_user` VALUES (332, 139, '2020710237', '周柏儿', '00', '', '', '0', '', '$2a$10$o7eU925cU4DXYsVpYM/UOOPpou6L5ooA6q.XtdrkuFwbM39oK8JRC', '0', '0', '127.0.0.1', '2026-01-13 13:43:03', NULL, '19157727791', '2026-01-12 14:21:17', '', '2026-01-13 13:43:02', NULL);
INSERT INTO `sys_user` VALUES (333, 139, '2020710238', '朱语晗', '00', '', '', '0', '', '$2a$10$p5xcsvnQ.XgruITOo/xuweHWnTIfMRSZks6TpXLb.P3tNDojoOk.O', '0', '0', '127.0.0.1', '2026-01-13 13:50:36', NULL, '19157727791', '2026-01-12 14:21:17', '', '2026-01-13 13:50:35', NULL);
INSERT INTO `sys_user` VALUES (334, 139, '2020710239', '何萱妍', '00', '', '', '0', '', '$2a$10$6UYgc/Z5yhJsKUdhYIRSvevmbThVI.f/v8ngr7VvaKD9siYAbosjS', '0', '0', '127.0.0.1', '2026-01-13 13:20:32', NULL, '19157727791', '2026-01-12 14:21:17', '', '2026-01-13 13:20:32', NULL);
INSERT INTO `sys_user` VALUES (335, 139, '2020710240', '方颖滢', '00', '', '', '0', '', '$2a$10$D.C2mFxGaA1f2GVKCnc9SeTbta2sZArvHoSn/f5nzI3wIWuOQJv3m', '0', '0', '127.0.0.1', '2026-01-13 13:20:28', NULL, '19157727791', '2026-01-12 14:21:17', '', '2026-01-13 13:20:28', NULL);
INSERT INTO `sys_user` VALUES (336, 139, '2020710241', '孙硕', '00', '', '', '0', '', '$2a$10$EX1K06RGAdXArvLXYuOZwOgUkWZM5OHhfWVct9KM7x5zmp5ulPkXa', '0', '0', '127.0.0.1', '2026-01-13 13:20:27', NULL, '19157727791', '2026-01-12 14:21:17', '', '2026-01-13 13:20:27', NULL);
INSERT INTO `sys_user` VALUES (337, 139, '2020710221', '赵轩逸', '00', '', '', '0', '', '$2a$10$gC5XFXVqP/HboFxQeEP1lu2KAxNMqQyast/AvQqfYZqRgTzMh.jqi', '0', '0', '127.0.0.1', '2026-01-13 13:42:30', NULL, '19157727791', '2026-01-12 14:21:17', '', '2026-01-13 13:42:29', NULL);
INSERT INTO `sys_user` VALUES (338, 139, '2020710242', '罗梓航', '00', '', '', '0', '', '$2a$10$S2gLMxsHAeVg2VXtsxDqGu5uHmPiDhaXSXzi9X9fda03WB8dsecla', '0', '0', '127.0.0.1', '2026-01-13 13:50:17', NULL, '19157727791', '2026-01-12 14:21:17', '', '2026-01-13 13:50:16', NULL);
INSERT INTO `sys_user` VALUES (339, 139, '2020710243', '杨子轩', '00', '', '', '0', '', '$2a$10$mdunuxjLBSKTHaRZt.UJSeK6OsANCkfS6x9FfFG0YvfJFXW3mM2Tu', '0', '0', '127.0.0.1', '2026-01-13 13:50:07', NULL, '19157727791', '2026-01-12 14:21:18', '', '2026-01-13 13:50:06', NULL);
INSERT INTO `sys_user` VALUES (340, 139, '2020710101', '陈芳怡', '00', '', '', '0', '', '$2a$10$/rGN.lfMa2O3bFPty/m3wel2y074yV1NthLi6j9jEFR3FJOdauyia', '0', '0', '127.0.0.1', '2026-01-13 14:26:57', NULL, '19157727791', '2026-01-12 14:21:18', '', '2026-01-13 14:26:56', NULL);
INSERT INTO `sys_user` VALUES (341, 139, '2020710102', '陈灏满', '00', '', '', '0', '', '$2a$10$VteqinLeGNFIJNjBj78YAeR9lSi2/UK/ZeNQcUBOI2V0HgDQNaD3G', '0', '0', '127.0.0.1', '2026-01-13 14:26:47', NULL, '19157727791', '2026-01-12 14:21:18', '', '2026-01-13 14:26:47', NULL);
INSERT INTO `sys_user` VALUES (342, 139, '2020710103', '陈文睿', '00', '', '', '0', '', '$2a$10$j2gk50ZFvVQNoz1O7SqLweCjWbzXA6oloen8alarAHe4SszMhXJM6', '0', '0', '127.0.0.1', '2026-01-13 14:26:50', NULL, '19157727791', '2026-01-12 14:21:18', '', '2026-01-13 14:26:50', NULL);
INSERT INTO `sys_user` VALUES (343, 139, '2020710104', '程  诺', '00', '', '', '0', '', '$2a$10$xvhFlC1Vp5bYLnmOxfoMRuKuHcEOAvFPu1SJnlf1Q5KdOiAsvokO2', '0', '0', '127.0.0.1', '2026-01-13 14:27:09', NULL, '19157727791', '2026-01-12 14:21:18', '', '2026-01-13 14:27:08', NULL);
INSERT INTO `sys_user` VALUES (344, 139, '2020710105', '方曦航', '00', '', '', '0', '', '$2a$10$6TliSUSyu3CypEoNzoOTqem3qdKKbyh4bF0Rav0g7Zj7HIy.fcPDa', '0', '0', '127.0.0.1', '2026-01-13 15:05:29', NULL, '19157727791', '2026-01-12 14:21:18', '', '2026-01-13 15:05:28', NULL);
INSERT INTO `sys_user` VALUES (345, 139, '2020710106', '葛笑瑜', '00', '', '', '0', '', '$2a$10$cb5JZ85bn5CabSR.AddzCOKzbTu6w32DixaIyJ6/1Mlij9YcvDOPK', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:18', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (346, 139, '2020710107', '何俊坤', '00', '', '', '0', '', '$2a$10$dA3lpigQ/h.3XhxtNNrn1.p60bsn2fbAKjWwaIovjsGqSRW5aiLeG', '0', '0', '127.0.0.1', '2026-01-14 16:21:31', NULL, '19157727791', '2026-01-12 14:21:18', '', '2026-01-14 16:21:30', NULL);
INSERT INTO `sys_user` VALUES (347, 139, '2020710108', '胡芝悦', '00', '', '', '0', '', '$2a$10$qG30fYEsKqJ3QHYNgIJ4ruadp5rY9mFWOFlvlpIca.17VpjK2h9su', '0', '0', '127.0.0.1', '2026-01-13 14:26:59', NULL, '19157727791', '2026-01-12 14:21:18', '', '2026-01-13 14:26:58', NULL);
INSERT INTO `sys_user` VALUES (348, 139, '2020710109', '黄  硕', '00', '', '', '0', '', '$2a$10$CHDXEVxrAdoZuxzI1CJfX.RnReMqIzx.1faK0UoMHNBkDEeS.EU1y', '0', '0', '127.0.0.1', '2026-01-13 14:26:43', NULL, '19157727791', '2026-01-12 14:21:18', '', '2026-01-13 14:26:43', NULL);
INSERT INTO `sys_user` VALUES (349, 139, '2020710110', '江思悯', '00', '', '', '0', '', '$2a$10$KN2VKS/Q7yDLJE.Me.jMo.6Tv9pKfZelVH9uS6xhfteB8raBmGpWO', '0', '0', '127.0.0.1', '2026-01-13 14:28:10', NULL, '19157727791', '2026-01-12 14:21:18', '', '2026-01-13 14:28:10', NULL);
INSERT INTO `sys_user` VALUES (350, 139, '2020710111', '蒋语诺', '00', '', '', '0', '', '$2a$10$YFqqlYAQ1pBco6fsyhBEK.HY631bTS1fNbDGWpsaCPAJp.Z7BuXI2', '0', '0', '127.0.0.1', '2026-01-13 14:26:54', NULL, '19157727791', '2026-01-12 14:21:18', '', '2026-01-13 14:26:53', NULL);
INSERT INTO `sys_user` VALUES (351, 139, '2020710112', '金子茵', '00', '', '', '0', '', '$2a$10$X3JAC7TitUskXM0jCp9D0uersqVhkwnd1arsTU.v60x/wjCm346cK', '0', '0', '127.0.0.1', '2026-01-13 14:26:55', NULL, '19157727791', '2026-01-12 14:21:18', '', '2026-01-13 14:26:55', NULL);
INSERT INTO `sys_user` VALUES (352, 139, '2020710113', '柯子涵', '00', '', '', '0', '', '$2a$10$az.pp38HHVZjjvH4Ajf4DOtLi59JVPmXmt.KZ6LhQgqCmK3OS4Scq', '0', '0', '127.0.0.1', '2026-01-13 14:26:53', NULL, '19157727791', '2026-01-12 14:21:19', '', '2026-01-13 14:26:53', NULL);
INSERT INTO `sys_user` VALUES (353, 139, '2020710114', '李恩慧', '00', '', '', '0', '', '$2a$10$Y.U8JJ1R8.RycMkUi7fX/ep64lGrnO.kjmhH6Vrrz9XURZDUeP56e', '0', '0', '127.0.0.1', '2026-01-13 14:26:51', NULL, '19157727791', '2026-01-12 14:21:19', '', '2026-01-13 14:26:50', NULL);
INSERT INTO `sys_user` VALUES (354, 139, '2020710115', '李国俊', '00', '', '', '0', '', '$2a$10$F7NJjjv1IEUWB01wWn6nQOTDVTi7Ybl/ChCSP.l2poqYMEX5Hxo1.', '0', '0', '127.0.0.1', '2026-01-13 14:26:55', NULL, '19157727791', '2026-01-12 14:21:19', '', '2026-01-13 14:26:55', NULL);
INSERT INTO `sys_user` VALUES (355, 139, '2020710116', '李淑颐', '00', '', '', '0', '', '$2a$10$8J.fNlvnNKT6eT5EYh97n.xZ5TV.ok1OboahFTWxFtt/yMMc6t8PW', '0', '0', '127.0.0.1', '2026-01-13 14:26:58', NULL, '19157727791', '2026-01-12 14:21:19', '', '2026-01-13 14:26:57', NULL);
INSERT INTO `sys_user` VALUES (356, 139, '2020710117', '李艺晴', '00', '', '', '0', '', '$2a$10$JLSK6Vsi0kdy3.L1JDeqs.c56EMTaocrwGV1wAfkNaOd56dWNrJ8q', '0', '0', '127.0.0.1', '2026-01-13 14:27:21', NULL, '19157727791', '2026-01-12 14:21:19', '', '2026-01-13 14:27:20', NULL);
INSERT INTO `sys_user` VALUES (357, 139, '2020710118', '林溢晨', '00', '', '', '0', '', '$2a$10$rcOeJjxF9WskqGVEdxhEaOKxg5I12sTJVYJb7ku.gG/19l0t7UXzi', '0', '0', '127.0.0.1', '2026-01-13 14:26:51', NULL, '19157727791', '2026-01-12 14:21:19', '', '2026-01-13 14:26:51', NULL);
INSERT INTO `sys_user` VALUES (358, 139, '2020710119', '吕梓萱', '00', '', '', '0', '', '$2a$10$Fu59KjcKsnHntkAnSG28EO5EFLelsqHk5Cx9ep6sRnE3NJDAzqIHO', '0', '0', '127.0.0.1', '2026-01-13 14:26:55', NULL, '19157727791', '2026-01-12 14:21:19', '', '2026-01-13 14:26:55', NULL);
INSERT INTO `sys_user` VALUES (359, 139, '2020710120', '渠家闰', '00', '', '', '0', '', '$2a$10$laGNFN1tCeLBk3Jkx85tle7S.eSU3A2Bs7ZZSR7l6.a0FxIcFTviO', '0', '0', '127.0.0.1', '2026-01-13 14:26:43', NULL, '19157727791', '2026-01-12 14:21:19', '', '2026-01-13 14:26:43', NULL);
INSERT INTO `sys_user` VALUES (360, 139, '2020710121', '盛羽浩', '00', '', '', '0', '', '$2a$10$YNuxdq51MYmI8vZV3V0pjeu9IETYHLIpGBSXrFm/AvEsGWxWhupXm', '0', '0', '127.0.0.1', '2026-01-13 14:26:47', NULL, '19157727791', '2026-01-12 14:21:19', '', '2026-01-13 14:26:47', NULL);
INSERT INTO `sys_user` VALUES (361, 139, '2020710122', '盛羽祺', '00', '', '', '0', '', '$2a$10$zUUMSkQQURo0aHzQTfUih.nQ6MSyOp5fmHm05I9BEY/9KtBeBhcYi', '0', '0', '127.0.0.1', '2026-01-13 14:41:39', NULL, '19157727791', '2026-01-12 14:21:19', '', '2026-01-13 14:41:38', NULL);
INSERT INTO `sys_user` VALUES (362, 139, '2020710123', '史昱翎', '00', '', '', '0', '', '$2a$10$iPn6AUQI4CO15wgBgGW5NuzXT8EO3CGvq6ryeFhZHPT.w6gK0Z0gq', '0', '0', '127.0.0.1', '2026-01-13 14:26:52', NULL, '19157727791', '2026-01-12 14:21:19', '', '2026-01-13 14:26:52', NULL);
INSERT INTO `sys_user` VALUES (363, 139, '2020710124', '肖奕忻', '00', '', '', '0', '', '$2a$10$0OTLvY.dauWXL0xqCKWpruCXIa4KLRcmdBgitbcMB4DeWgoPQa7ke', '0', '0', '127.0.0.1', '2026-01-13 14:26:47', NULL, '19157727791', '2026-01-12 14:21:19', '', '2026-01-13 14:26:46', NULL);
INSERT INTO `sys_user` VALUES (364, 139, '2020710125', '杨  浩', '00', '', '', '0', '', '$2a$10$b1zZ7J.z9NIGziFOL8dpOOfpz0Oke3qLWVNE6gaTVu7xaFEt22dCO', '0', '0', '127.0.0.1', '2026-01-13 14:26:46', NULL, '19157727791', '2026-01-12 14:21:19', '', '2026-01-13 14:26:46', NULL);
INSERT INTO `sys_user` VALUES (365, 139, '2020710126', '杨茁芠', '00', '', '', '0', '', '$2a$10$NzKUI4ZVHn9MaZ.U/9rOeeoFT5nonCKu.KhYYfhc4s4KCk0/eKchO', '0', '0', '127.0.0.1', '2026-01-13 14:26:55', NULL, '19157727791', '2026-01-12 14:21:20', '', '2026-01-13 14:26:55', NULL);
INSERT INTO `sys_user` VALUES (366, 139, '2020710127', '叶梓童', '00', '', '', '0', '', '$2a$10$5nCzPoNebm1G27hGcQDvrOIlE0jQQjKXNjSF6GdUo2DghopLmAC/m', '0', '0', '127.0.0.1', '2026-01-13 14:26:56', NULL, '19157727791', '2026-01-12 14:21:20', '', '2026-01-13 14:26:55', NULL);
INSERT INTO `sys_user` VALUES (367, 139, '2020710128', '俞宸昊', '00', '', '', '0', '', '$2a$10$ca.GHTGkSPMt3SS4U1eP5OI55VIS85w.RctSn20qaF3TiwsZnVfcq', '0', '0', '127.0.0.1', '2026-01-13 14:26:50', NULL, '19157727791', '2026-01-12 14:21:20', '', '2026-01-13 14:26:50', NULL);
INSERT INTO `sys_user` VALUES (368, 139, '2020710129', '俞佳池', '00', '', '', '0', '', '$2a$10$uCj0391k1VSTOImjXV5gY.R0U9l01rt17RZxWgt6rxY9OHeI7YwqG', '0', '0', '127.0.0.1', '2026-01-13 14:39:47', NULL, '19157727791', '2026-01-12 14:21:20', '', '2026-01-13 14:39:46', NULL);
INSERT INTO `sys_user` VALUES (369, 139, '2020710130', '虞滨榕', '00', '', '', '0', '', '$2a$10$Yq7xA15F/KJAh1V5ZtFOIuoykkbrF13KvNVvIsdZ5w8hbsTffqllK', '0', '0', '127.0.0.1', '2026-01-13 14:26:55', NULL, '19157727791', '2026-01-12 14:21:20', '', '2026-01-13 14:26:55', NULL);
INSERT INTO `sys_user` VALUES (370, 139, '2020710131', '袁若释', '00', '', '', '0', '', '$2a$10$qfyM8ypNsvQRUODvNJJOYuK7UgFtaEqprPN7P9DA6T/8HSVFasO0y', '0', '0', '127.0.0.1', '2026-01-13 14:26:55', NULL, '19157727791', '2026-01-12 14:21:20', '', '2026-01-13 14:26:55', NULL);
INSERT INTO `sys_user` VALUES (371, 139, '2020710132', '张恩绮', '00', '', '', '0', '', '$2a$10$ixIVr0AuOhtHe5Jdb83dburdmg9.ODvRE7ASo/KjGsxyscjV74nIW', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:20', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (372, 139, '2020710134', '张志恒', '00', '', '', '0', '', '$2a$10$pMAaLAwO2a61J9j4ihioAOPizK5kqXoU.sst6RQVDUGtTzJuw/0aW', '0', '0', '127.0.0.1', '2026-01-13 14:26:44', NULL, '19157727791', '2026-01-12 14:21:20', '', '2026-01-13 14:26:43', NULL);
INSERT INTO `sys_user` VALUES (373, 139, '2020710135', '郑博毅', '00', '', '', '0', '', '$2a$10$xwA6X0MvK2KdZ.XLwmfO4OdqMEygoBcc8.kFHUf8WY4jUW3yCWKaa', '0', '0', '127.0.0.1', '2026-01-13 14:26:53', NULL, '19157727791', '2026-01-12 14:21:20', '', '2026-01-13 14:26:53', NULL);
INSERT INTO `sys_user` VALUES (374, 139, '2020710136', '郑睿宸', '00', '', '', '0', '', '$2a$10$YKpJWUkDY05DyQLUXLX94ufnM0jksBq0STGD/qEmdg.O9J6fYB2qu', '0', '0', '127.0.0.1', '2026-01-13 14:26:53', NULL, '19157727791', '2026-01-12 14:21:20', '', '2026-01-13 14:26:52', NULL);
INSERT INTO `sys_user` VALUES (375, 139, '2020710138', '周宇轩', '00', '', '', '0', '', '$2a$10$T63Q.5X94UVDsLR5ZBOMM.NeLdDN8NrGMLFuv7p52cwA5kv.2J3ba', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:20', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (376, 139, '2020710139', '朱嘉乐', '00', '', '', '0', '', '$2a$10$q2BxyxqDksQEyOY0Y8HdXu0G47upYcJImMqn.NfG.sLsGuZBDOUuq', '0', '0', '127.0.0.1', '2026-01-13 14:26:52', NULL, '19157727791', '2026-01-12 14:21:20', '', '2026-01-13 14:26:52', NULL);
INSERT INTO `sys_user` VALUES (377, 139, '2020710140', '庄梦恬', '00', '', '', '0', '', '$2a$10$ahWRaNgBI7fnMxSXqdazIOo14BpQ.Z0adatgjMNuvXQMddVhfHn9.', '0', '0', '127.0.0.1', '2026-01-13 14:27:00', NULL, '19157727791', '2026-01-12 14:21:21', '', '2026-01-13 14:26:59', NULL);
INSERT INTO `sys_user` VALUES (378, 139, '2020710141', '聂雨涵', '00', '', '', '0', '', '$2a$10$HQY5YKrfPybmvLpee2GZu.ZUxKlLhz5Hha7xinFiM965Y2QE46xRS', '0', '0', '127.0.0.1', '2026-01-13 14:26:53', NULL, '19157727791', '2026-01-12 14:21:21', '', '2026-01-13 14:26:52', NULL);
INSERT INTO `sys_user` VALUES (379, 139, '2020710142', '熊亦可', '00', '', '', '0', '', '$2a$10$3uuonRa658H69SDE61TVw.KNKkENCHIziDIAsc.oEEGPxlM1vcXDC', '0', '0', '127.0.0.1', '2026-01-13 14:26:46', NULL, '19157727791', '2026-01-12 14:21:21', '', '2026-01-13 14:26:45', NULL);
INSERT INTO `sys_user` VALUES (380, 139, '2020710143', '沈晨宇', '00', '', '', '0', '', '$2a$10$54lLbAHFGmfrp/qayWy7iujxZ.mbv15UxqR7mXQI/5MIOOUaWgrFu', '0', '0', '127.0.0.1', '2026-01-13 14:26:51', NULL, '19157727791', '2026-01-12 14:21:21', '', '2026-01-13 14:26:51', NULL);
INSERT INTO `sys_user` VALUES (381, 139, '2020710144', '梁雨嫣', '00', '', '', '0', '', '$2a$10$ovJN8nQ7c9WaaQrJIVNCOeB00cWDx3iMIlijP1FQB0NbL9DXMumra', '0', '0', '127.0.0.1', '2026-01-13 14:26:47', NULL, '19157727791', '2026-01-12 14:21:21', '', '2026-01-13 14:26:47', NULL);
INSERT INTO `sys_user` VALUES (382, 139, '2020710801', '鲍辰曦', '00', '', '', '0', '', '$2a$10$9DFRaG53W4RYUQ5clHRXWuqd7LN3ps52wnOzVmCOs3gEonQdF35PW', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:21', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (383, 139, '2020710802', '柴靖毅', '00', '', '', '0', '', '$2a$10$.hml2BdXR2O4lCu/PCYDze7I1yH1fd6YfWwmS/pOhcnuOSIPrdpOa', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:21', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (384, 139, '2020710803', '陈诺', '00', '', '', '0', '', '$2a$10$VYMI.4M0Cl7akKeE0XLYg.noxWKRMQUvYvJI2sl0p18zrONwFojOu', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:21', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (385, 139, '2020710804', '陈耀东', '00', '', '', '0', '', '$2a$10$C62u3iZjbobliJ9tf4I44ufkNeNzhtGHHUZnys6raidhNV4Gl24pW', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:21', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (386, 139, '2020710805', '陈哲凯', '00', '', '', '0', '', '$2a$10$etp/i0RDgcuV5NMiQEOvouhOsvYY6drlTCY5BYkzw/kBlpEp6IFAm', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:21', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (387, 139, '2020710806', '陈梓域', '00', '', '', '0', '', '$2a$10$fSazy95GU8AXHPTAwUU8bOIhl8szUFj2UIg3gSjk.E1/eikl6UhQ6', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:21', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (388, 139, '2020710807', '代存宸', '00', '', '', '0', '', '$2a$10$VB8nYbpAFGaAQ2o5CHE5G.Gv/OWYca/xlSbvmBOuxqkoQpD0/TMQK', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:21', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (389, 139, '2020710808', '冯俊锋', '00', '', '', '0', '', '$2a$10$Yfe.dHunFa4RdC2NPHDxv.X.uOpVj7pzAIeB49i9JnIbHJflI6beG', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:21', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (390, 139, '2020710809', '顾航睿', '00', '', '', '0', '', '$2a$10$balFZSrE69EL5i3XUqjSruCHBftlJUTK.tE1hbJ3.xWeVUwvMMfd6', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:22', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (391, 139, '2020710810', '何姝瑞', '00', '', '', '0', '', '$2a$10$ICK.do4HfTfl8/MvpBw12OIrcgSFoyluuSMUDspUvBX7NGafor6C.', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:22', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (392, 139, '2020710811', '黄宥翰', '00', '', '', '0', '', '$2a$10$kzIFtEYoaGExQFS2gjJNsedqAg.igTVQ6cnw6yMpT3f5HvWHPcqua', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:22', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (393, 139, '2020710812', '黄子烆', '00', '', '', '0', '', '$2a$10$NIkQII8ISc0gC7qeMlWfXOI4Ty6E1v8dd.1ep46Qk9g94Y8ZHqWhi', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:22', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (394, 139, '2020710813', '孔祥坤', '00', '', '', '0', '', '$2a$10$hxwc9fpM4uD/osrYu1gJEuy2o6yZXIzjPH1gXO12B45Nk1EFrd59W', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:22', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (395, 139, '2020710814', '赖佳欣', '00', '', '', '0', '', '$2a$10$SRQsZdQKu7M7dkF.1Bbyg.0lXNTxnFDbTxWuUGxkPvUdZf8zMKbr.', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:22', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (396, 139, '2020710815', '李正', '00', '', '', '0', '', '$2a$10$zi6E3sTuEYf/Fu1.BBYntuki1z7rAyofaXAyIQt7QFLjgwGaF87R2', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:22', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (397, 139, '2020710816', '梁齐壬', '00', '', '', '0', '', '$2a$10$5NUQI1e9sLPDSqO73pKuk.DeVvn2BFaQuMiBG/y4/q7uRPCiXq16q', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:22', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (398, 139, '2020710817', '林泓铮', '00', '', '', '0', '', '$2a$10$ureLRwJGDmFRwiIl7.oKu.dUJlRFDf7qmPUN5.hjd.8qUJUHbNLui', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:22', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (399, 139, '2020710818', '盛梓淇', '00', '', '', '0', '', '$2a$10$CAZIHD6vsZq79EExDhs3Aehv8VzBlbTY43BpOe3oj3AOPCLen3.12', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:22', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (400, 139, '2020710819', '王渤竣', '00', '', '', '0', '', '$2a$10$1Awug0rD1F.j7tu6U42dsOUXsjjmhXUrdDVWL5VAwAAVq90pGqMB6', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:22', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (401, 139, '2020710820', '王浩然', '00', '', '', '0', '', '$2a$10$2QnzLTIaJdrdmNoun2qhCeUvxRc4H.UMKeuN6./TGoa8AxwnjMU8S', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:22', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (402, 139, '2020710821', '王文迪', '00', '', '', '0', '', '$2a$10$vlzifpb57jAoABHueSB.8.otQtufsfzSifQm62GTpbPsQqD4QvL7a', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:22', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (403, 139, '2020710822', '王欣茹', '00', '', '', '0', '', '$2a$10$aib.w8.vLORlkYuPOAVUc.rtaHhOTQAEJImRPNikpejFul.RnRYQa', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:23', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (404, 139, '2020710823', '陈瀚宇', '00', '', '', '0', '', '$2a$10$PqbAZCoeUjuOjwwyHNa1tOspaF/A4T69xBBwaZRgvQz6xQN1h7L8K', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:23', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (405, 139, '2020710824', '王予诺', '00', '', '', '0', '', '$2a$10$MgMNHPg2xj6iImnnmnjFq.LEcgk9PgCwFqYwPtRaiViWgydEVlANS', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:23', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (406, 139, '2020710825', '翁嘉泽', '00', '', '', '0', '', '$2a$10$cDm0qWV8sW/Qn8xBIcuRhuNWD4prhOkgoMD0QQTpKe/TWdkGLjf1S', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:23', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (407, 139, '2020710826', '徐晨核', '00', '', '', '0', '', '$2a$10$m19ayZSpC91M1Kwfw7I5futYwidnT4IukLsfrIPhZ9dp5vsQK4WrK', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:23', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (408, 139, '2020710827', '许静蕊', '00', '', '', '0', '', '$2a$10$qqii61ew8PUfelVUB86tY.m5Ot0guISOsmiFktj5g5BkU0fglcO3q', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:23', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (409, 139, '2020710828', '许露译', '00', '', '', '0', '', '$2a$10$l5nJfUoeOOZ/Ip.qpI7ujuPlvq5Hrpl9QukOU/Wz21YrdTM.OA9fG', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:23', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (410, 139, '2020710829', '薛博文', '00', '', '', '0', '', '$2a$10$DLuqIfvXYgrjhdbGKjRR8.avB3aR9ZZnV5lUxAaOPu.2u3DW10oo6', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:23', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (411, 139, '2020710830', '杨博帆', '00', '', '', '0', '', '$2a$10$1FEVzJNYZzpDtkaGH57LZu3CrS18hjgmexEZUC1dFZMbJ3U/aMj5C', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:23', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (412, 139, '2020710831', '马跃航', '00', '', '', '0', '', '$2a$10$XB8i4R5M1Mz/Mg2aNDsEv.I1HvazgcaZCitLmWy2ZnJesGeOItaLG', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:23', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (413, 139, '2020710833', '张夏锦禾', '00', '', '', '0', '', '$2a$10$U9kLJz2KpSTZVFrfEbE8Te3GPPW3aFhpQuwafy4saav1n9RZjRLaW', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:23', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (414, 139, '2020710834', '张政言', '00', '', '', '0', '', '$2a$10$/NplYN1qtLodEExgm4Je4uacDfROX.Rvx7tnELqa/Kdrs1b1nY3cC', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:23', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (415, 139, '2020710835', '赵涵暻', '00', '', '', '0', '', '$2a$10$G5jnyb2vp/p.5sQdBcm86.CfFKAw5UzxIumHY7xywsSfufCQPXUvu', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:23', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (416, 139, '2020710836', '周欣柔', '00', '', '', '0', '', '$2a$10$Xtg0BOkDLSHUvjh6wgdjiOvDkMjOcE8ZQXAnzhCqrKqyf4k8XH.o2', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:23', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (417, 139, '2020710837', '朱柏睿', '00', '', '', '0', '', '$2a$10$BgDKcbjsHnoKp4Rt9E20UehYyNzyXu3CGzdmplXuA7XnzmXux83Ea', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:24', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (418, 139, '2020710838', '朱钰鑫', '00', '', '', '0', '', '$2a$10$/lEhAF2UyoUzZ2Xma5QDeO7bY21PlzkUCLhyTtBtG8C7Pxo/uC61K', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:24', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (419, 139, '2020710839', '朱震宇', '00', '', '', '0', '', '$2a$10$G8HbMB77.v33Y89q3p0wrOgcn6LC../8JgPAW4U7OACvdBl3HcL5C', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:24', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (420, 139, '2020710840', '彭欣蕊', '00', '', '', '0', '', '$2a$10$x/W4MFvUz1DcZmJCtKrUIORwpiAsiBRVJAZMwaHDCj/eeV8rTYcUG', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:24', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (421, 139, '2020710841', '张赫杋', '00', '', '', '0', '', '$2a$10$dJICxGSuUaicuR8G45WAI.9OCQIHS9H71pIT2rIHJhcTDpQZspVRS', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:24', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (422, 139, '2020710842', '谢懿纯', '00', '', '', '0', '', '$2a$10$W2s4l2bEyZvkAy3Mmmy0ZOmzcvsehMWabnndau.SLFX9e7ZiGW1RG', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:24', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (423, 139, '2020710832', '范芷睿', '00', '', '', '0', '', '$2a$10$IeVrCN7jZnire1QrngZm/unpNPHLwfxkOyy9vSUKnz/jfPTGEpOSO', '0', '0', '', NULL, NULL, '19157727791', '2026-01-12 14:21:24', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (424, 139, '2020711099', '555', '00', '', '', '0', '', '$2a$10$XWApYVIUuxKbwlkgJFrqs.j0buY8iymmyBnXaB2PHkVcaY.Lamajq', '0', '0', '', NULL, NULL, '19157727791', '2026-01-14 10:22:14', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (425, 139, '2020710301', '鲍祈帆', '00', '', '', '0', '', '$2a$10$.qLxlVirYuKTGCU1f17ia.ZOjHPTo8ici3fxytbO/Q6XD78gHWyqK', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:27', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (426, 139, '2020710302', '陈羽晟', '00', '', '', '0', '', '$2a$10$Nft1RKFjyiRCY.r7sSwDQeUe0QGFu23qCPKFtEG0dLiDy1WmxTmvG', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:27', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (427, 139, '2020710303', '程至锦', '00', '', '', '0', '', '$2a$10$m1xz7t5Bni1rCVypSKX6g.13I20nUDPmNLhe/hSjcKVmrARE8lrey', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:27', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (428, 139, '2020710304', '池慧诗', '00', '', '', '0', '', '$2a$10$TMQsLp86AuAWqf/DnCe5VefHgr5V70K5HiuwF4dO7OlJvfCNfjT8S', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:28', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (429, 139, '2020710306', '何曙瑞', '00', '', '', '0', '', '$2a$10$eAX8VyVH3SI3lA.2YSC2s.EdfqldeKhVeYDo/zvADOHPwzZ0D0Jcy', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:28', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (430, 139, '2020710307', '胡梓琪', '00', '', '', '0', '', '$2a$10$pAMISDur49Ddnp0PY.ahOeAQa7HK3XnpjeCQeWuwd9B1gzWpDYJTW', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:28', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (431, 139, '2020710308', '黄金奕', '00', '', '', '0', '', '$2a$10$XzhBV82/nK/7428YlHX4h.F3t165h.N7FvYNf3ZkGyHQnghzBHM9y', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:28', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (432, 139, '2020710309', '蒋心怡', '00', '', '', '0', '', '$2a$10$kuMZOhAaudK6EOQ6.Vqwp.wAn1OJllQYNK2OQB4duPqvMLJtKHDWa', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:28', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (433, 139, '2020710310', '赖逸帆', '00', '', '', '0', '', '$2a$10$2tSdnAX4MMSDpucP.H7xWOraFlyGc7ALLZdUjdObC1sPrsAGcDqRi', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:28', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (434, 139, '2020710311', '李秋阳', '00', '', '', '0', '', '$2a$10$6OcFy7lstuOlyA/dfJ8Hluo6ZAJQOZ.J1ebe52EjfmbMnR6lTmI5S', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:28', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (435, 139, '2020710312', '李紫奕', '00', '', '', '0', '', '$2a$10$hWkwgwuBaFjBfomdDiABWO/GW0LsxcxKRYADPOOf0nQ/rKdU.oI02', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:28', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (436, 139, '2020710313', '励睿轩', '00', '', '', '0', '', '$2a$10$I8Khpxm0mc.XeHXOS6a03OY0LAmhi/zlUSOn1bPdAFalCZGl6FRJK', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:29', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (437, 139, '2020710314', '梁滋苡', '00', '', '', '0', '', '$2a$10$.xdisst9f586f.kAI2gwhOT19QhCjm1mTMjltKaswE2xf8Tiu9Dce', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:29', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (438, 139, '2020710315', '林茜', '00', '', '', '0', '', '$2a$10$OKWIW/DznfdORRhPgogi/ODk1ockVcZHj1MArulIPolgx9ORLAgGG', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:29', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (439, 139, '2020710316', '林欣怡', '00', '', '', '0', '', '$2a$10$/tqKXmDVtAUnTNij3vvMze0RuquDlsj8AuUMpA0efAm9WIorAZUgq', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:29', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (440, 139, '2020710317', '卢锦琪', '00', '', '', '0', '', '$2a$10$bG0kCsB4RVU8NmFWRcNvc.d0GPuqp0jKXdKxHP6gouG6QfDYiimE6', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:29', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (441, 139, '2020710318', '马广胤', '00', '', '', '0', '', '$2a$10$FKJVU1wWeeNAuMU5sHFmJebdV3Iz4B/o3x5gQh4ksAxbNvO1.UbTC', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:29', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (442, 139, '2020710319', '潘一恒', '00', '', '', '0', '', '$2a$10$Zc/Kcb0PTZtyVn05sLbd4O4ju3KH4HUyMdm3rvqdpEDq1Yrn8K8ZO', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:29', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (443, 139, '2020710320', '潘伊菲', '00', '', '', '0', '', '$2a$10$ABmT380iMoTrsQSKEQoH2.kZlngJTznhZl5WyZcmJreZ6i7CQuk2m', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:29', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (444, 139, '2020710321', '潘梓欣', '00', '', '', '0', '', '$2a$10$ypY8sVRxsjE6DUX0EjJT3O4zwoAUD1qxqShQIUIUopAhPGLMmLK8u', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:29', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (445, 139, '2020710322', '庞德霖', '00', '', '', '0', '', '$2a$10$ZCn3JXzl654NW4GQMyQcSeF9oaNrSgBrzbVDfH3ejLxiyr3wjvtFC', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:30', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (446, 139, '2020710323', '邱文绍', '00', '', '', '0', '', '$2a$10$SGKbh7n.dIvUihR16ZxtPOO93FLTccZ819Ar1MFAYAsRnABZT58C.', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:30', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (447, 139, '2020710324', '邵铭轩', '00', '', '', '0', '', '$2a$10$6kuPdBlXcAAbPE2.QyCnm.5R2BKgY8F.pNhr40UlkasQX16YQCpj6', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:30', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (448, 139, '2020710325', '邵屹', '00', '', '', '0', '', '$2a$10$BbChn5rb1DLruFUnsK6m.u9IyL8V6Gr6Jy6plui2FO5UtMJjVDNWm', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:30', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (449, 139, '2020710326', '盛杰睿', '00', '', '', '0', '', '$2a$10$S46QH/7k4knAVK7TzD0dVOJAcymiAWcoOguANaqAh7Y.mh2etUIAS', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:30', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (450, 139, '2020710328', '童子昱', '00', '', '', '0', '', '$2a$10$HwKABvKjchFx7TpN98GyEeykMJ8W6.No45kTi8lBY3RTG1BiyCsBC', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:30', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (451, 139, '2020710329', '王思淏', '00', '', '', '0', '', '$2a$10$o1CbhFPbjD7Bc7B5qKGE0uFcFWnOlGN8ZpKL/8.DTiRfVGHTyabYe', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:30', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (452, 139, '2020710330', '吴家顺', '00', '', '', '0', '', '$2a$10$X2RsRD3jnN3tJql8/IdKCey70zphw43iJeXgVCl5vzgZ69fwv6NXq', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:30', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (453, 139, '2020710331', '徐子羡', '00', '', '', '0', '', '$2a$10$k4MVVSOHWJ7vkpx3zS5gTuzT1ENSFL2bApFj7t8PSo8K0xXIcZ8y2', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:30', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (454, 139, '2020710332', '余子涵', '00', '', '', '0', '', '$2a$10$bW7E2cIROZtQEBURbL3iE.R/JjALDvgZY9kpDOQUVdE61kMJGQaiW', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:31', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (455, 139, '2020710333', '俞昊', '00', '', '', '0', '', '$2a$10$TBuijdVcoBp6Vhh.k8SsJedYd27wRhE1Cqd7GqBpQJxgnYLkxJJoS', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:31', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (456, 139, '2020710334', '俞林希', '00', '', '', '0', '', '$2a$10$qO2LVmIeKYWrQyRaDuLcDuTLHdUZq6A1HoziaPQN/l8NBgqwwj6qK', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:31', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (457, 139, '2020710335', '虞茹熙', '00', '', '', '0', '', '$2a$10$2RHPy8REWzhVNuuDRUZ79eDWnyLQC7BuplcMG6ack15zsFi1u.qQO', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:31', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (458, 139, '2020710336', '张语菲', '00', '', '', '0', '', '$2a$10$Yc8bY2ci84739u5hG2gUROiDLaWIYK.iB3Pf9IzWjBUqJcQi3vOci', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:31', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (459, 139, '2020710337', '郑希菡', '00', '', '', '0', '', '$2a$10$tHLVA8xNw389VYlgQRIOV.Kut3Ll.3BB0FYgDrPr0cen8Pz.fLegu', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:31', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (460, 139, '2020710338', '郑希暖', '00', '', '', '0', '', '$2a$10$.XC3qeC8g4NqSziOl1fIYOb0LTdhjEm9X9xOt0KnWG2sHTUFIIAGC', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:31', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (461, 139, '2020710339', '郑紫忻', '00', '', '', '0', '', '$2a$10$ycOSfgyLuErW6Xhg6vFwnOMv1NVeopRg8A8T7iSqTbgK5PeJ5Kphe', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:31', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (462, 139, '2020710340', '朱宇奇', '00', '', '', '0', '', '$2a$10$B6fP8Iktft5vW68vTeahvuonXlvSQYySRcdkhfQeyqmrLHnldT7/m', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:31', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (463, 139, '2020710341', '赵煜罕', '00', '', '', '0', '', '$2a$10$Qs4Xr1Mcc3h12MN3xhL8t.8nW3E9aninBFG1UZH6IPqqPnC6cXWxq', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:32', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (464, 139, '2020710342', '潘伟祺', '00', '', '', '0', '', '$2a$10$MMgjuEmr8p24NXdS5dOi7uVp2SRDNSUyYegMWx9aTTd8UbEYxbfvm', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:32', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (465, 139, '2020710305', '刘逸诺', '00', '', '', '0', '', '$2a$10$AqQzTHnKrsQp1xBAKdKz5eXmJ20DKtUiekgFJe0mLeQT5laH7OkNC', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:32', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (466, 139, '2020710327', '张瀚宇', '00', '', '', '0', '', '$2a$10$nB5wKNgRqVA/aoPDHKKonOjM5/..EtIn6UgbiNetX0BjoCkxYPJaa', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:32', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (467, 139, '2020710501', '鲍仟峰', '00', '', '', '0', '', '$2a$10$JeBWpS1CTdTIN2N0OZEa.OlwestLhvqorR0BXE/1iM0l82OqOBxRC', '0', '0', '127.0.0.1', '2026-01-16 12:39:43', NULL, '19157727791', '2026-01-16 11:54:32', '', '2026-01-16 12:39:43', NULL);
INSERT INTO `sys_user` VALUES (468, 139, '2020710502', '陈首任', '00', '', '', '0', '', '$2a$10$W9akzq7Oj3/QeQPuEx.5We2qXr.vkCezBy.eZKHEOtJWkuYj8sa.q', '0', '0', '127.0.0.1', '2026-01-16 12:39:36', NULL, '19157727791', '2026-01-16 11:54:32', '', '2026-01-16 12:39:35', NULL);
INSERT INTO `sys_user` VALUES (469, 139, '2020710503', '陈彦旭', '00', '', '', '0', '', '$2a$10$Pdt/D93iYa6eiQvf9ccncuoo3fA1iV.oG9shqRHni3xj8GFI6FMUe', '0', '0', '127.0.0.1', '2026-01-16 12:39:53', NULL, '19157727791', '2026-01-16 11:54:32', '', '2026-01-16 12:39:53', NULL);
INSERT INTO `sys_user` VALUES (470, 139, '2020710504', '陈奕涵', '00', '', '', '0', '', '$2a$10$fNS2PFWttHxrznEZW88y2.bfA7vbYTiL68YiUXTXWIUWK.OFXLi/q', '0', '0', '127.0.0.1', '2026-01-16 12:39:46', NULL, '19157727791', '2026-01-16 11:54:32', '', '2026-01-16 12:39:46', NULL);
INSERT INTO `sys_user` VALUES (471, 139, '2020710505', '陈语诺', '00', '', '', '0', '', '$2a$10$f3Zm6MNpQlfP276kqWtoteysNsBziaHQV7A4TDjp/FvKtmq2kAruu', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:32', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (472, 139, '2020710506', '葛培栋', '00', '', '', '0', '', '$2a$10$NxNZwW.7CDAvvVsMODGyS.B3SKXhxtiG8a23D8.55MAw/fWiHbUOW', '0', '0', '127.0.0.1', '2026-01-16 12:39:40', NULL, '19157727791', '2026-01-16 11:54:32', '', '2026-01-16 12:39:40', NULL);
INSERT INTO `sys_user` VALUES (473, 139, '2020710507', '洪天逸', '00', '', '', '0', '', '$2a$10$FqrUajU6Ia/Ogdb4yo1GxOYDoPW9p0aVh/wVIOeysWgYF/F9MjY8y', '0', '0', '127.0.0.1', '2026-01-16 12:43:20', NULL, '19157727791', '2026-01-16 11:54:33', '', '2026-01-16 12:43:20', NULL);
INSERT INTO `sys_user` VALUES (474, 139, '2020710508', '洪梓涵', '00', '', '', '0', '', '$2a$10$ybEpGG7lDTbS1tZJChdRRuIHxLXwFzItfozr2Nn2WuCQJKRLaoTju', '0', '0', '127.0.0.1', '2026-01-16 12:39:49', NULL, '19157727791', '2026-01-16 11:54:33', '', '2026-01-16 12:39:49', NULL);
INSERT INTO `sys_user` VALUES (475, 139, '2020710509', '黄智桐', '00', '', '', '0', '', '$2a$10$J/CPyQ397l7fdbsxCu1NKOmpyQbSXjDUuVYValTdBWG5wOVjC872e', '0', '0', '127.0.0.1', '2026-01-16 12:39:43', NULL, '19157727791', '2026-01-16 11:54:33', '', '2026-01-16 12:39:43', NULL);
INSERT INTO `sys_user` VALUES (476, 139, '2020710510', '林若妡', '00', '', '', '0', '', '$2a$10$zoqy5B3491RGxTbYXi.vz.58IpxNuT29IlM.SKgtXp2eWevzIddM2', '0', '0', '127.0.0.1', '2026-01-16 12:39:43', NULL, '19157727791', '2026-01-16 11:54:33', '', '2026-01-16 12:39:42', NULL);
INSERT INTO `sys_user` VALUES (477, 139, '2020710511', '林一凡', '00', '', '', '0', '', '$2a$10$4JT3O8t.7ItKk0URjNdGoeW/y0.JPtY88xCXBahRv.mN1iMKRu7Iy', '0', '0', '127.0.0.1', '2026-01-16 12:39:43', NULL, '19157727791', '2026-01-16 11:54:33', '', '2026-01-16 12:39:43', NULL);
INSERT INTO `sys_user` VALUES (478, 139, '2020710512', '刘科', '00', '', '', '0', '', '$2a$10$OIB2wSE2hG4l0hS14/kgY.R801rLwqfkagQVyBGZw3BknQSygKBq.', '0', '0', '127.0.0.1', '2026-01-16 12:39:44', NULL, '19157727791', '2026-01-16 11:54:33', '', '2026-01-16 12:39:43', NULL);
INSERT INTO `sys_user` VALUES (479, 139, '2020710513', '刘一宇', '00', '', '', '0', '', '$2a$10$4PUCBmHVZK1Moo6vPha4VOMHQhWFt1V81p2DyT9DLTkQ.B1a1nvTy', '0', '0', '127.0.0.1', '2026-01-16 12:39:42', NULL, '19157727791', '2026-01-16 11:54:33', '', '2026-01-16 12:39:41', NULL);
INSERT INTO `sys_user` VALUES (480, 139, '2020710514', '刘紫轩', '00', '', '', '0', '', '$2a$10$wiHyQr1HnbqVUY.AVKnBgOvexaU5bnT4UQfS5UkGED7VypbBqLEt6', '0', '0', '127.0.0.1', '2026-01-16 12:39:43', NULL, '19157727791', '2026-01-16 11:54:33', '', '2026-01-16 12:39:42', NULL);
INSERT INTO `sys_user` VALUES (481, 139, '2020710515', '邵欣悦', '00', '', '', '0', '', '$2a$10$kVmluUKJpZWRZwSQJnpF.uqoG1eZEzMpp7wuHi7MnA5HQBt4WnnJW', '0', '0', '127.0.0.1', '2026-01-16 12:39:48', NULL, '19157727791', '2026-01-16 11:54:33', '', '2026-01-16 12:39:48', NULL);
INSERT INTO `sys_user` VALUES (482, 139, '2020710516', '沈思彤', '00', '', '', '0', '', '$2a$10$CjLqmtc95FqApnkYk6pUEOBcMI/qV4U.zBAH2WH.7jH7oac1DT3YS', '0', '0', '127.0.0.1', '2026-01-16 12:39:55', NULL, '19157727791', '2026-01-16 11:54:34', '', '2026-01-16 12:39:55', NULL);
INSERT INTO `sys_user` VALUES (483, 139, '2020710517', '石彬佑', '00', '', '', '0', '', '$2a$10$QQuIFSyRnf9qZvBWhUraH.TIrFYEH9KXigXw5j0r9fZoBp.4wCCNm', '0', '0', '127.0.0.1', '2026-01-16 12:39:42', NULL, '19157727791', '2026-01-16 11:54:34', '', '2026-01-16 12:39:42', NULL);
INSERT INTO `sys_user` VALUES (484, 139, '2020710518', '史昊哲', '00', '', '', '0', '', '$2a$10$npqjET8uXcn5HK9EUXv65uNb8VrOCr22XYIvG6/5./SePkJsKXhnm', '0', '0', '127.0.0.1', '2026-01-16 12:39:44', NULL, '19157727791', '2026-01-16 11:54:34', '', '2026-01-16 12:39:43', NULL);
INSERT INTO `sys_user` VALUES (485, 139, '2020710519', '史彧麒', '00', '', '', '0', '', '$2a$10$R94XYp2Z3tShr2Y0NkWvXu1xMmIBnirRRuGn6f1lkEJ4dBOLYtChu', '0', '0', '127.0.0.1', '2026-01-16 12:39:42', NULL, '19157727791', '2026-01-16 11:54:34', '', '2026-01-16 12:39:42', NULL);
INSERT INTO `sys_user` VALUES (486, 139, '2020710520', '史梓乐', '00', '', '', '0', '', '$2a$10$7Ip3fllnKQa.oArwsXgS5eo5K/DtG0eoKYcg0JROMqMFyx3sNDoie', '0', '0', '127.0.0.1', '2026-01-16 12:39:42', NULL, '19157727791', '2026-01-16 11:54:34', '', '2026-01-16 12:39:42', NULL);
INSERT INTO `sys_user` VALUES (487, 139, '2020710521', '陶紫萱', '00', '', '', '0', '', '$2a$10$pYXxHcSCXwY5QW3XaIlTYux6sbdoWcdHkS9tmeGF./scTEJQnZmOS', '0', '0', '127.0.0.1', '2026-01-16 12:39:41', NULL, '19157727791', '2026-01-16 11:54:34', '', '2026-01-16 12:39:41', NULL);
INSERT INTO `sys_user` VALUES (488, 139, '2020710522', '王柄凯', '00', '', '', '0', '', '$2a$10$AMU2rNwk5HF/qc4JQoSj0egScmy7yZUcQG.8iDcpGXxd3lVdqxUuW', '0', '0', '127.0.0.1', '2026-01-16 12:39:36', NULL, '19157727791', '2026-01-16 11:54:34', '', '2026-01-16 12:39:36', NULL);
INSERT INTO `sys_user` VALUES (489, 139, '2020710523', '吴一昊', '00', '', '', '0', '', '$2a$10$S.O4vG4WPmaN./KOxOv3m.qNv7kO4wqs.e/cTOd2iHiNg3XNHaE0G', '0', '0', '127.0.0.1', '2026-01-16 12:40:09', NULL, '19157727791', '2026-01-16 11:54:34', '', '2026-01-16 12:40:09', NULL);
INSERT INTO `sys_user` VALUES (490, 139, '2020710524', '肖晨铃', '00', '', '', '0', '', '$2a$10$HA5WotREDoB4s0FoLJwY4OXduGjN90FtFK02WlxLFJY6kP35/nII.', '0', '0', '127.0.0.1', '2026-01-16 12:39:54', NULL, '19157727791', '2026-01-16 11:54:35', '', '2026-01-16 12:39:53', NULL);
INSERT INTO `sys_user` VALUES (491, 139, '2020710525', '谢卓佳', '00', '', '', '0', '', '$2a$10$a55dF.vY4r.PNo3OdKZeEezSA5NhLiNSCQRYBnJ/zTmC/h1x66jv.', '0', '0', '127.0.0.1', '2026-01-16 12:39:47', NULL, '19157727791', '2026-01-16 11:54:35', '', '2026-01-16 12:39:46', NULL);
INSERT INTO `sys_user` VALUES (492, 139, '2020710526', '徐嘉轩', '00', '', '', '0', '', '$2a$10$F9etW8LUbhzuTSYk.cjMruROh994.ovPelBYvv.eURQFpfAAmL9KC', '0', '0', '127.0.0.1', '2026-01-16 12:39:42', NULL, '19157727791', '2026-01-16 11:54:35', '', '2026-01-16 12:39:41', NULL);
INSERT INTO `sys_user` VALUES (493, 139, '2020710527', '徐婉童', '00', '', '', '0', '', '$2a$10$pVHzUibvVIAV.TIKfaFyWe.dKB75bgKGbZNR8Yc6Y8HxircH8AEr2', '0', '0', '127.0.0.1', '2026-01-16 12:39:41', NULL, '19157727791', '2026-01-16 11:54:35', '', '2026-01-16 12:39:41', NULL);
INSERT INTO `sys_user` VALUES (494, 139, '2020710528', '姚哲涵', '00', '', '', '0', '', '$2a$10$qEuwa95ZSPAKC465aAdjoeT7Lq5CnmDGay1CrCPa2jugVqBge57ty', '0', '0', '127.0.0.1', '2026-01-16 12:39:40', NULL, '19157727791', '2026-01-16 11:54:35', '', '2026-01-16 12:39:40', NULL);
INSERT INTO `sys_user` VALUES (495, 139, '2020710529', '余启含', '00', '', '', '0', '', '$2a$10$//p.UemNRX.R5mUPKw92QOLbEAsHsfCfCs1AIRACvDVLV9B3EpnJK', '0', '0', '127.0.0.1', '2026-01-16 12:39:40', NULL, '19157727791', '2026-01-16 11:54:35', '', '2026-01-16 12:39:39', NULL);
INSERT INTO `sys_user` VALUES (496, 139, '2020710530', '虞玺可', '00', '', '', '0', '', '$2a$10$KrjrdHOQkH2th0ITmhUW6.3SLiTqATS.Rcv/L1MTd/RPD3.B6tytu', '0', '0', '127.0.0.1', '2026-01-16 12:39:46', NULL, '19157727791', '2026-01-16 11:54:35', '', '2026-01-16 12:39:45', NULL);
INSERT INTO `sys_user` VALUES (497, 139, '2020710532', '张可儿', '00', '', '', '0', '', '$2a$10$GUG5xjn/UN2YdPeRCYfJsOo8hAjHJbPRbMs19D2n88Mgx.m57Uxj2', '0', '0', '127.0.0.1', '2026-01-16 12:39:53', NULL, '19157727791', '2026-01-16 11:54:35', '', '2026-01-16 12:39:52', NULL);
INSERT INTO `sys_user` VALUES (498, 139, '2020710533', '张淇瑄', '00', '', '', '0', '', '$2a$10$ZU04Fz0iJ53yT4AoN4ho3.3d6fo4Quavz6dWMK3xWMnc.GrttdCXm', '0', '0', '127.0.0.1', '2026-01-16 12:39:39', NULL, '19157727791', '2026-01-16 11:54:35', '', '2026-01-16 12:39:38', NULL);
INSERT INTO `sys_user` VALUES (499, 139, '2020710534', '张栩臣', '00', '', '', '0', '', '$2a$10$F96cDjargDCdzSNhYDSBKOSieEGqU3xA5shmQlPA4qP2zph4Jttoy', '0', '0', '127.0.0.1', '2026-01-16 12:39:34', NULL, '19157727791', '2026-01-16 11:54:36', '', '2026-01-16 12:39:33', NULL);
INSERT INTO `sys_user` VALUES (500, 139, '2020710535', '张艺成', '00', '', '', '0', '', '$2a$10$sNstRZ873kNo0iiIWNC.PuQw9cglVRNMuCRYvukR3nJb4SNXQhYBK', '0', '0', '127.0.0.1', '2026-01-16 12:39:45', NULL, '19157727791', '2026-01-16 11:54:36', '', '2026-01-16 12:39:45', NULL);
INSERT INTO `sys_user` VALUES (501, 139, '2020710536', '赵彬伊', '00', '', '', '0', '', '$2a$10$f6S2oBkZplEn/IPxUBtkDu5HHhAQk.6VPtds1WryYo2P0RuMMaCAq', '0', '0', '127.0.0.1', '2026-01-16 12:39:42', NULL, '19157727791', '2026-01-16 11:54:36', '', '2026-01-16 12:39:42', NULL);
INSERT INTO `sys_user` VALUES (502, 139, '2020710537', '赵育锌', '00', '', '', '0', '', '$2a$10$zg9vNDj5qgmRpLKqJi/uN.LanXUqLuCTmPWwrIdgQQlhIaFcst2w6', '0', '0', '127.0.0.1', '2026-01-16 12:39:28', NULL, '19157727791', '2026-01-16 11:54:36', '', '2026-01-16 12:39:27', NULL);
INSERT INTO `sys_user` VALUES (503, 139, '2020710538', '郑涵予', '00', '', '', '0', '', '$2a$10$LkBgXSpdSADS4PHdnLRtQ.zV6kALLBG0ILCeYMc1GPLTRVixmCd26', '0', '0', '127.0.0.1', '2026-01-16 12:39:41', NULL, '19157727791', '2026-01-16 11:54:36', '', '2026-01-16 12:39:40', NULL);
INSERT INTO `sys_user` VALUES (504, 139, '2020710539', '周瑜轩', '00', '', '', '0', '', '$2a$10$Gx8cndpCcwVtP/SvdGqNL.ZT1WPIdXc9VdeL4SG.GIRr810606Oma', '0', '0', '127.0.0.1', '2026-01-16 12:39:48', NULL, '19157727791', '2026-01-16 11:54:36', '', '2026-01-16 12:39:48', NULL);
INSERT INTO `sys_user` VALUES (505, 139, '2020710540', '朱梓淇', '00', '', '', '0', '', '$2a$10$1vsaytorXt5JVF2E4NubxeNPIRYRa4ld7X8ucmMUwwwGbgCsE3.LW', '0', '0', '127.0.0.1', '2026-01-16 12:39:38', NULL, '19157727791', '2026-01-16 11:54:36', '', '2026-01-16 12:39:38', NULL);
INSERT INTO `sys_user` VALUES (506, 139, '2020710541', '蔡宁远', '00', '', '', '0', '', '$2a$10$snWyhAY/ZcteUyCGOrFz5OJ5V1hwJybTqzXNmQw5rVCNWQ50BlLSq', '0', '0', '127.0.0.1', '2026-01-16 12:39:39', NULL, '19157727791', '2026-01-16 11:54:36', '', '2026-01-16 12:39:39', NULL);
INSERT INTO `sys_user` VALUES (507, 139, '2020710531', '张峻豪', '00', '', '', '0', '', '$2a$10$jzbwkGwrtGCG392xCPIiBuDLKgzlfsVyO3vPHrcefCvTp8U2kdw5S', '0', '0', '127.0.0.1', '2026-01-16 12:53:50', NULL, '19157727791', '2026-01-16 11:54:36', '', '2026-01-16 12:53:50', NULL);
INSERT INTO `sys_user` VALUES (508, 139, '2020710542', '刘鑫悦', '00', '', '', '0', '', '$2a$10$9WfI3Et8VcsuLz69NLEU6uoKtZxU4doTnuNyactf4B1V7v3WsaV5K', '0', '0', '127.0.0.1', '2026-01-16 12:39:46', NULL, '19157727791', '2026-01-16 11:54:36', '', '2026-01-16 12:39:45', NULL);
INSERT INTO `sys_user` VALUES (509, 139, '2020710543', '杨琪涵', '00', '', '', '0', '', '$2a$10$v0VE2uLnJJaBdxEWJRjpTuVo1Lrti6Yk6dduAL1jeSjthxk2iwFhG', '0', '0', '127.0.0.1', '2026-01-16 12:39:45', NULL, '19157727791', '2026-01-16 11:54:37', '', '2026-01-16 12:39:45', NULL);
INSERT INTO `sys_user` VALUES (510, 139, '2020710601', '陈锦浩', '00', '', '', '0', '', '$2a$10$xr/Z9igf/7co2YnunUyuTu62LclvEenZHqGigmF6CFLTtoMkt2vUm', '0', '0', '127.0.0.1', '2026-01-16 14:23:11', NULL, '19157727791', '2026-01-16 11:54:37', '', '2026-01-16 14:23:11', NULL);
INSERT INTO `sys_user` VALUES (511, 139, '2020710602', '陈沛钧', '00', '', '', '0', '', '$2a$10$DNg3AV6g35XvduDKxJClLuy1b5QYo.LTPPARR5prx.hkcPek62lRa', '0', '0', '127.0.0.1', '2026-01-16 14:23:02', NULL, '19157727791', '2026-01-16 11:54:37', '', '2026-01-16 14:23:02', NULL);
INSERT INTO `sys_user` VALUES (512, 139, '2020710603', '陈于诺', '00', '', '', '0', '', '$2a$10$bzjVAL7IYjImFtTpphzpXOtBArHVS1kc3Z96n498j5SH7EnzjV1Zm', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:37', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (513, 139, '2020710604', '程思琪', '00', '', '', '0', '', '$2a$10$oBhjmyJU69FcMVTZNiuzGO5XxcLzjAEn46cPTzmFyjoFKMNCjTyIq', '0', '0', '127.0.0.1', '2026-01-16 14:23:04', NULL, '19157727791', '2026-01-16 11:54:37', '', '2026-01-16 14:23:04', NULL);
INSERT INTO `sys_user` VALUES (514, 139, '2020710605', '龚习', '00', '', '', '0', '', '$2a$10$WQ5pIhNyu6xjCXcXIYGEoe1FrVfjbL5IAVvLYJswdakYUWxOUKo3m', '0', '0', '127.0.0.1', '2026-01-16 14:23:05', NULL, '19157727791', '2026-01-16 11:54:37', '', '2026-01-16 14:23:04', NULL);
INSERT INTO `sys_user` VALUES (515, 139, '2020710606', '郭建平', '00', '', '', '0', '', '$2a$10$BI92RwEM37YBPtLxgvR8YODG0kFfBLZUSbd/oj6CGiUifEHCK1CA.', '0', '0', '127.0.0.1', '2026-01-16 14:23:02', NULL, '19157727791', '2026-01-16 11:54:37', '', '2026-01-16 14:23:01', NULL);
INSERT INTO `sys_user` VALUES (516, 139, '2020710607', '郭馨童', '00', '', '', '0', '', '$2a$10$u3H609pYDFmVk2hPubJ1zOjo9R5/PY58F9TVL.nayDfKdDNJYXjme', '0', '0', '127.0.0.1', '2026-01-16 14:23:11', NULL, '19157727791', '2026-01-16 11:54:37', '', '2026-01-16 14:23:11', NULL);
INSERT INTO `sys_user` VALUES (517, 139, '2020710608', '胡琛珧', '00', '', '', '0', '', '$2a$10$fdjsv8lx6aAi.dCMUHXYAuZgiS4f8ImVmm6z0O0wHyjqjwglBxI.e', '0', '0', '127.0.0.1', '2026-01-16 14:23:07', NULL, '19157727791', '2026-01-16 11:54:37', '', '2026-01-16 14:23:06', NULL);
INSERT INTO `sys_user` VALUES (518, 139, '2020710609', '黄怡萱', '00', '', '', '0', '', '$2a$10$8gsHnyqFE0N0yXG6VJhY6ODIbFefFRoLVmEmC5rI7VNoTaZgmsKEy', '0', '0', '127.0.0.1', '2026-01-16 14:23:01', NULL, '19157727791', '2026-01-16 11:54:38', '', '2026-01-16 14:23:01', NULL);
INSERT INTO `sys_user` VALUES (519, 139, '2020710610', '蒋易宸', '00', '', '', '0', '', '$2a$10$IthMAEgpbXWj/1I4T1RkV.Nb34lWRrQyUwQ1hA0JTsyaTCl372APW', '0', '0', '127.0.0.1', '2026-01-16 14:23:12', NULL, '19157727791', '2026-01-16 11:54:38', '', '2026-01-16 14:23:11', NULL);
INSERT INTO `sys_user` VALUES (520, 139, '2020710611', '蒋易恒', '00', '', '', '0', '', '$2a$10$y4b4tvmKchhokIwkqPfda..NedqOO3z7PCySdHmK4XWv0E2tdFiSG', '0', '0', '127.0.0.1', '2026-01-16 14:23:07', NULL, '19157727791', '2026-01-16 11:54:38', '', '2026-01-16 14:23:06', NULL);
INSERT INTO `sys_user` VALUES (521, 139, '2020710612', '赖乙木', '00', '', '', '0', '', '$2a$10$slYRmjHZ5AM7MlhVvdQhx.aveVjeUZ6T4IZOH8NUeui1DK6KYl/.2', '0', '0', '127.0.0.1', '2026-01-16 14:23:03', NULL, '19157727791', '2026-01-16 11:54:38', '', '2026-01-16 14:23:02', NULL);
INSERT INTO `sys_user` VALUES (522, 139, '2020710613', '李心怡', '00', '', '', '0', '', '$2a$10$GK0wHNGXkgy3rtWDZBdmF.xUoSxokifgZRJW16QqzAbclR3L19owO', '0', '0', '127.0.0.1', '2026-01-16 14:23:01', NULL, '19157727791', '2026-01-16 11:54:38', '', '2026-01-16 14:23:01', NULL);
INSERT INTO `sys_user` VALUES (523, 139, '2020710614', '励安心', '00', '', '', '0', '', '$2a$10$3VENkhv5d8E09gO0gd0JweXbz.aMLLZTu0yHzTED3nN5HJlPEO/mK', '0', '0', '127.0.0.1', '2026-01-16 14:23:17', NULL, '19157727791', '2026-01-16 11:54:38', '', '2026-01-16 14:23:17', NULL);
INSERT INTO `sys_user` VALUES (524, 139, '2020710615', '励梓晨', '00', '', '', '0', '', '$2a$10$JLr.x0o4xFtdRY/tngGM5ugixAhcZF.tuT9666ujIE3eRbPMeFyzi', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:38', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (525, 139, '2020710616', '马淇萱', '00', '', '', '0', '', '$2a$10$aKgnPLExTk/qPtOnOYn7uuHOlIjfhPesbNcwWyWOLk5CQrx9x2Zqe', '0', '0', '127.0.0.1', '2026-01-16 14:23:08', NULL, '19157727791', '2026-01-16 11:54:38', '', '2026-01-16 14:23:07', NULL);
INSERT INTO `sys_user` VALUES (526, 139, '2020710617', '欧艺轩', '00', '', '', '0', '', '$2a$10$idTuaVNkjo/6.G84Q2GsM.ytTL8lFXjTlokfGOBM5Ey4n6FfJlIMS', '0', '0', '127.0.0.1', '2026-01-16 14:23:09', NULL, '19157727791', '2026-01-16 11:54:38', '', '2026-01-16 14:23:08', NULL);
INSERT INTO `sys_user` VALUES (527, 139, '2020710618', '陈紫', '00', '', '', '0', '', '$2a$10$CudoLSDpQNngIEQ5oQGM3uW3TqEwEPpr9B19l6cpFdycgWdcAou5e', '0', '0', '127.0.0.1', '2026-01-16 14:23:08', NULL, '19157727791', '2026-01-16 11:54:39', '', '2026-01-16 14:23:08', NULL);
INSERT INTO `sys_user` VALUES (528, 139, '2020710619', '史梓镔', '00', '', '', '0', '', '$2a$10$uqLa/gn2ANmkN7HRx3J0xenc5jCZEjl8Y0dcKB9sU.qQ8WnWmfd5i', '0', '0', '127.0.0.1', '2026-01-16 14:23:06', NULL, '19157727791', '2026-01-16 11:54:39', '', '2026-01-16 14:23:06', NULL);
INSERT INTO `sys_user` VALUES (529, 139, '2020710620', '汪睿涛', '00', '', '', '0', '', '$2a$10$Jh9dMNfdERemfaV4QzZvRuh6CTKIysNfAH/VpRHsezUMT0jbMbQiu', '0', '0', '127.0.0.1', '2026-01-16 14:23:07', NULL, '19157727791', '2026-01-16 11:54:39', '', '2026-01-16 14:23:07', NULL);
INSERT INTO `sys_user` VALUES (530, 139, '2020710621', '汪诗颖', '00', '', '', '0', '', '$2a$10$iNVvrA87zng6MNZBMEkZLucHDl1kXEFYabuOyg3Eelm9yc0rgOpt2', '0', '0', '127.0.0.1', '2026-01-16 14:23:11', NULL, '19157727791', '2026-01-16 11:54:39', '', '2026-01-16 14:23:11', NULL);
INSERT INTO `sys_user` VALUES (531, 139, '2020710623', '王彦雯', '00', '', '', '0', '', '$2a$10$KqWyj5ZvFbLy8/182.2GYeCnOv2dpX3Ab8O7hFD.zfGlVaZWM1Tzq', '0', '0', '127.0.0.1', '2026-01-16 14:31:56', NULL, '19157727791', '2026-01-16 11:54:39', '', '2026-01-16 14:31:55', NULL);
INSERT INTO `sys_user` VALUES (532, 139, '2020710624', '王雨菡', '00', '', '', '0', '', '$2a$10$lMM.9VDD117Bf6pKnTYMo.ne1SGMMxfIVgE9HJVWPy4PPOIs0mcXq', '0', '0', '127.0.0.1', '2026-01-16 14:23:05', NULL, '19157727791', '2026-01-16 11:54:39', '', '2026-01-16 14:23:04', NULL);
INSERT INTO `sys_user` VALUES (533, 139, '2020710625', '肖子萱', '00', '', '', '0', '', '$2a$10$QmmboT7gBYab1PqIpmTJjeJuZ3ATTQ172dp3OyUG2JP9a9eOKjYQi', '0', '0', '127.0.0.1', '2026-01-16 14:23:08', NULL, '19157727791', '2026-01-16 11:54:39', '', '2026-01-16 14:23:08', NULL);
INSERT INTO `sys_user` VALUES (534, 139, '2020710626', '徐翎哲', '00', '', '', '0', '', '$2a$10$7MK4XZJbpjdahbVD.usDb.f6rChkogp7qL5y4oCz0kDD3Crec/QpS', '0', '0', '127.0.0.1', '2026-01-16 14:23:11', NULL, '19157727791', '2026-01-16 11:54:39', '', '2026-01-16 14:23:10', NULL);
INSERT INTO `sys_user` VALUES (535, 139, '2020710627', '徐若勋', '00', '', '', '0', '', '$2a$10$o7hN27A7K5IuWP7Ka6pWZORq1J32zf2cA9UYyZ5DUK5RalG6XPO/i', '0', '0', '127.0.0.1', '2026-01-16 14:23:05', NULL, '19157727791', '2026-01-16 11:54:39', '', '2026-01-16 14:23:05', NULL);
INSERT INTO `sys_user` VALUES (536, 139, '2020710628', '许诺萱', '00', '', '', '0', '', '$2a$10$v4LBFT3V0/YsE4yQPx95NuIPFy8mPue3jflsTRR9HHJbTlsJ8.Mim', '0', '0', '127.0.0.1', '2026-01-16 14:23:09', NULL, '19157727791', '2026-01-16 11:54:39', '', '2026-01-16 14:23:08', NULL);
INSERT INTO `sys_user` VALUES (537, 139, '2020710629', '叶锦杭', '00', '', '', '0', '', '$2a$10$0r6Mp2FHJkqF0EXFR.GuiuWLoDEOFwT0nrQ9P67yjLPsP4lqJ9b2i', '0', '0', '127.0.0.1', '2026-01-16 14:23:08', NULL, '19157727791', '2026-01-16 11:54:40', '', '2026-01-16 14:23:07', NULL);
INSERT INTO `sys_user` VALUES (538, 139, '2020710630', '叶于锌', '00', '', '', '0', '', '$2a$10$mJxauPkJ2SSMzPyl7H/.3OyRO2i8zrne/Jl5pIFtehCchC5/1I/z.', '0', '0', '127.0.0.1', '2026-01-16 14:23:18', NULL, '19157727791', '2026-01-16 11:54:40', '', '2026-01-16 14:23:17', NULL);
INSERT INTO `sys_user` VALUES (539, 139, '2020710631', '余佳盈', '00', '', '', '0', '', '$2a$10$ufMc90b8ilfx9VtGLSRfAuuZAk0AEyRk3Wj8AYqwwiWXCLu.Un0Vq', '0', '0', '127.0.0.1', '2026-01-16 14:23:09', NULL, '19157727791', '2026-01-16 11:54:40', '', '2026-01-16 14:23:09', NULL);
INSERT INTO `sys_user` VALUES (540, 139, '2020710632', '张浩轩', '00', '', '', '0', '', '$2a$10$zIGSs.DFU/KdZTg1i.egXORxKP1.I7lMxfDDJmhXv5oXnHtjodHIq', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:40', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (541, 139, '2020710633', '张心如', '00', '', '', '0', '', '$2a$10$/e2rO1ql48tWXzyem/PvC.4P7ZwfQaT2R4kIaIGxEK.dakx9Mj1z6', '0', '0', '127.0.0.1', '2026-01-16 14:23:06', NULL, '19157727791', '2026-01-16 11:54:40', '', '2026-01-16 14:23:05', NULL);
INSERT INTO `sys_user` VALUES (542, 139, '2020710634', '张哲瀚', '00', '', '', '0', '', '$2a$10$wynpqjSj1qn3epM6XvtmgOCig9xjAXXZcWiVvAzD.Dx8mZJjd4Ycu', '0', '0', '127.0.0.1', '2026-01-16 14:24:47', NULL, '19157727791', '2026-01-16 11:54:40', '', '2026-01-16 14:24:47', NULL);
INSERT INTO `sys_user` VALUES (543, 139, '2020710635', '赵梓轩', '00', '', '', '0', '', '$2a$10$YzTFNEZCn/QoKMgEfcogQOwlQXLlVO01ul6OHfLkbBh9EOTEiqqLW', '0', '0', '127.0.0.1', '2026-01-16 14:23:12', NULL, '19157727791', '2026-01-16 11:54:40', '', '2026-01-16 14:23:11', NULL);
INSERT INTO `sys_user` VALUES (544, 139, '2020710636', '郑奕尧', '00', '', '', '0', '', '$2a$10$0bfuHEmdF1JxIKKxGHB46.IB1WUp7CfQG/9B66lDb3SOTdWBe1I56', '0', '0', '127.0.0.1', '2026-01-16 14:23:03', NULL, '19157727791', '2026-01-16 11:54:40', '', '2026-01-16 14:23:02', NULL);
INSERT INTO `sys_user` VALUES (545, 139, '2020710637', '周星辰', '00', '', '', '0', '', '$2a$10$PKRurGCELTUcX5cgjadymucskLgFgQYMUNueSI43yGLBdLO7vtX9q', '0', '0', '127.0.0.1', '2026-01-16 14:23:09', NULL, '19157727791', '2026-01-16 11:54:40', '', '2026-01-16 14:23:08', NULL);
INSERT INTO `sys_user` VALUES (546, 139, '2020710638', '周星宇', '00', '', '', '0', '', '$2a$10$VumLg6ePpVufjFBJFwBbFOoAWHUOjmauenLLeWwh7jom4RXm8INkC', '0', '0', '127.0.0.1', '2026-01-16 14:23:04', NULL, '19157727791', '2026-01-16 11:54:40', '', '2026-01-16 14:23:03', NULL);
INSERT INTO `sys_user` VALUES (547, 139, '2020710639', '朱锦瑞', '00', '', '', '0', '', '$2a$10$OgFZw9oPdmU7v18G/jQcmO68GoWoSth5FWdKbXgzDvp.u0rWLKdca', '0', '0', '127.0.0.1', '2026-01-16 14:23:05', NULL, '19157727791', '2026-01-16 11:54:41', '', '2026-01-16 14:23:04', NULL);
INSERT INTO `sys_user` VALUES (548, 139, '2020710640', '朱妙涵', '00', '', '', '0', '', '$2a$10$y0u8hI8pgEgd1rVarM1ZI.I2T6UfHvHSzujE4Etj8bmCsyNjBmIP2', '0', '0', '', NULL, NULL, '19157727791', '2026-01-16 11:54:41', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (549, 139, '2020710641', '陈梓鑫', '00', '', '', '0', '', '$2a$10$8/weMqNO8gfzqKU6FNUmx.UK8LnvRzpCfs587TSO5DZd.epDiJItW', '0', '0', '127.0.0.1', '2026-01-16 14:23:10', NULL, '19157727791', '2026-01-16 11:54:41', '', '2026-01-16 14:23:09', NULL);
INSERT INTO `sys_user` VALUES (550, 139, '2020710622', '朱晨洁', '00', '', '', '0', '', '$2a$10$uaU1te.vjwonA8JjEU2Li.iTG6yE.QU/anLezQARFCq9eCXCLg8jO', '0', '0', '127.0.0.1', '2026-01-16 14:23:10', NULL, '19157727791', '2026-01-16 11:54:41', '', '2026-01-16 14:23:10', NULL);
INSERT INTO `sys_user` VALUES (551, 139, '2020710642', '周瑜宸', '00', '', '', '0', '', '$2a$10$8PPLfD3nHPoooPKIBo11BOJmlk4X/Axk5bQUjMCRh7st/6/04TnUK', '0', '0', '127.0.0.1', '2026-01-16 14:23:14', NULL, '19157727791', '2026-01-16 11:54:41', '', '2026-01-16 14:23:13', NULL);
INSERT INTO `sys_user` VALUES (552, 100, 'sx', 'sxs', '00', '', '', '0', '', '$2a$10$GbzjLLvPfSArP2q0XRDPvudjeCLDIXWsnWNre791//qXWNI4D0e3O', '0', '2', '', NULL, NULL, 'admin', '2026-01-16 15:28:36', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (553, 104, 'cece', 'ce', '00', '', '', '0', '', '$2a$10$TJ5Bo0KY0gZNTRKbbEuKk.WhT.1hD6etZzK.sEwpYmXmrPZ.msgDa', '0', '2', '', NULL, NULL, 'admin', '2026-01-16 15:29:34', '', NULL, NULL);
INSERT INTO `sys_user` VALUES (554, 100, 'laoda', '陈', '00', '', '', '0', '', '$2a$10$DSpAxx6kJPDUeUdMTPakfu7Uql2a80mb3q1izckAV7vUB97484yOa', '0', '0', '127.0.0.1', '2026-01-16 15:48:22', NULL, 'admin', '2026-01-16 15:45:38', '', '2026-01-16 15:48:22', NULL);

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
INSERT INTO `sys_user_dept` VALUES (554, 100);
INSERT INTO `sys_user_dept` VALUES (104, 107);
INSERT INTO `sys_user_dept` VALUES (104, 139);
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
INSERT INTO `sys_user_role` VALUES (168, 4);
INSERT INTO `sys_user_role` VALUES (169, 4);
INSERT INTO `sys_user_role` VALUES (170, 4);
INSERT INTO `sys_user_role` VALUES (171, 4);
INSERT INTO `sys_user_role` VALUES (172, 4);
INSERT INTO `sys_user_role` VALUES (173, 4);
INSERT INTO `sys_user_role` VALUES (174, 4);
INSERT INTO `sys_user_role` VALUES (175, 4);
INSERT INTO `sys_user_role` VALUES (176, 4);
INSERT INTO `sys_user_role` VALUES (177, 4);
INSERT INTO `sys_user_role` VALUES (178, 4);
INSERT INTO `sys_user_role` VALUES (179, 4);
INSERT INTO `sys_user_role` VALUES (180, 4);
INSERT INTO `sys_user_role` VALUES (181, 4);
INSERT INTO `sys_user_role` VALUES (182, 4);
INSERT INTO `sys_user_role` VALUES (183, 4);
INSERT INTO `sys_user_role` VALUES (184, 4);
INSERT INTO `sys_user_role` VALUES (185, 4);
INSERT INTO `sys_user_role` VALUES (186, 4);
INSERT INTO `sys_user_role` VALUES (187, 4);
INSERT INTO `sys_user_role` VALUES (188, 4);
INSERT INTO `sys_user_role` VALUES (189, 4);
INSERT INTO `sys_user_role` VALUES (190, 4);
INSERT INTO `sys_user_role` VALUES (191, 4);
INSERT INTO `sys_user_role` VALUES (192, 4);
INSERT INTO `sys_user_role` VALUES (193, 4);
INSERT INTO `sys_user_role` VALUES (194, 4);
INSERT INTO `sys_user_role` VALUES (195, 4);
INSERT INTO `sys_user_role` VALUES (196, 4);
INSERT INTO `sys_user_role` VALUES (197, 4);
INSERT INTO `sys_user_role` VALUES (198, 4);
INSERT INTO `sys_user_role` VALUES (199, 4);
INSERT INTO `sys_user_role` VALUES (200, 4);
INSERT INTO `sys_user_role` VALUES (201, 4);
INSERT INTO `sys_user_role` VALUES (202, 4);
INSERT INTO `sys_user_role` VALUES (203, 4);
INSERT INTO `sys_user_role` VALUES (204, 4);
INSERT INTO `sys_user_role` VALUES (205, 4);
INSERT INTO `sys_user_role` VALUES (206, 4);
INSERT INTO `sys_user_role` VALUES (207, 4);
INSERT INTO `sys_user_role` VALUES (208, 4);
INSERT INTO `sys_user_role` VALUES (209, 4);
INSERT INTO `sys_user_role` VALUES (210, 4);
INSERT INTO `sys_user_role` VALUES (254, 4);
INSERT INTO `sys_user_role` VALUES (255, 4);
INSERT INTO `sys_user_role` VALUES (256, 4);
INSERT INTO `sys_user_role` VALUES (257, 4);
INSERT INTO `sys_user_role` VALUES (258, 4);
INSERT INTO `sys_user_role` VALUES (259, 4);
INSERT INTO `sys_user_role` VALUES (260, 4);
INSERT INTO `sys_user_role` VALUES (261, 4);
INSERT INTO `sys_user_role` VALUES (262, 4);
INSERT INTO `sys_user_role` VALUES (263, 4);
INSERT INTO `sys_user_role` VALUES (264, 4);
INSERT INTO `sys_user_role` VALUES (265, 4);
INSERT INTO `sys_user_role` VALUES (266, 4);
INSERT INTO `sys_user_role` VALUES (267, 4);
INSERT INTO `sys_user_role` VALUES (268, 4);
INSERT INTO `sys_user_role` VALUES (269, 4);
INSERT INTO `sys_user_role` VALUES (270, 4);
INSERT INTO `sys_user_role` VALUES (271, 4);
INSERT INTO `sys_user_role` VALUES (272, 4);
INSERT INTO `sys_user_role` VALUES (273, 4);
INSERT INTO `sys_user_role` VALUES (274, 4);
INSERT INTO `sys_user_role` VALUES (275, 4);
INSERT INTO `sys_user_role` VALUES (276, 4);
INSERT INTO `sys_user_role` VALUES (277, 4);
INSERT INTO `sys_user_role` VALUES (278, 4);
INSERT INTO `sys_user_role` VALUES (279, 4);
INSERT INTO `sys_user_role` VALUES (280, 4);
INSERT INTO `sys_user_role` VALUES (281, 4);
INSERT INTO `sys_user_role` VALUES (282, 4);
INSERT INTO `sys_user_role` VALUES (283, 4);
INSERT INTO `sys_user_role` VALUES (284, 4);
INSERT INTO `sys_user_role` VALUES (285, 4);
INSERT INTO `sys_user_role` VALUES (286, 4);
INSERT INTO `sys_user_role` VALUES (287, 4);
INSERT INTO `sys_user_role` VALUES (288, 4);
INSERT INTO `sys_user_role` VALUES (289, 4);
INSERT INTO `sys_user_role` VALUES (290, 4);
INSERT INTO `sys_user_role` VALUES (291, 4);
INSERT INTO `sys_user_role` VALUES (292, 4);
INSERT INTO `sys_user_role` VALUES (293, 4);
INSERT INTO `sys_user_role` VALUES (294, 4);
INSERT INTO `sys_user_role` VALUES (295, 4);
INSERT INTO `sys_user_role` VALUES (296, 4);
INSERT INTO `sys_user_role` VALUES (297, 4);
INSERT INTO `sys_user_role` VALUES (298, 4);
INSERT INTO `sys_user_role` VALUES (299, 4);
INSERT INTO `sys_user_role` VALUES (300, 4);
INSERT INTO `sys_user_role` VALUES (301, 4);
INSERT INTO `sys_user_role` VALUES (302, 4);
INSERT INTO `sys_user_role` VALUES (303, 4);
INSERT INTO `sys_user_role` VALUES (304, 4);
INSERT INTO `sys_user_role` VALUES (305, 4);
INSERT INTO `sys_user_role` VALUES (306, 4);
INSERT INTO `sys_user_role` VALUES (307, 4);
INSERT INTO `sys_user_role` VALUES (308, 4);
INSERT INTO `sys_user_role` VALUES (309, 4);
INSERT INTO `sys_user_role` VALUES (310, 4);
INSERT INTO `sys_user_role` VALUES (311, 4);
INSERT INTO `sys_user_role` VALUES (312, 4);
INSERT INTO `sys_user_role` VALUES (313, 4);
INSERT INTO `sys_user_role` VALUES (314, 4);
INSERT INTO `sys_user_role` VALUES (315, 4);
INSERT INTO `sys_user_role` VALUES (316, 4);
INSERT INTO `sys_user_role` VALUES (317, 4);
INSERT INTO `sys_user_role` VALUES (318, 4);
INSERT INTO `sys_user_role` VALUES (319, 4);
INSERT INTO `sys_user_role` VALUES (320, 4);
INSERT INTO `sys_user_role` VALUES (321, 4);
INSERT INTO `sys_user_role` VALUES (322, 4);
INSERT INTO `sys_user_role` VALUES (323, 4);
INSERT INTO `sys_user_role` VALUES (324, 4);
INSERT INTO `sys_user_role` VALUES (325, 4);
INSERT INTO `sys_user_role` VALUES (326, 4);
INSERT INTO `sys_user_role` VALUES (327, 4);
INSERT INTO `sys_user_role` VALUES (328, 4);
INSERT INTO `sys_user_role` VALUES (329, 4);
INSERT INTO `sys_user_role` VALUES (330, 4);
INSERT INTO `sys_user_role` VALUES (331, 4);
INSERT INTO `sys_user_role` VALUES (332, 4);
INSERT INTO `sys_user_role` VALUES (333, 4);
INSERT INTO `sys_user_role` VALUES (334, 4);
INSERT INTO `sys_user_role` VALUES (335, 4);
INSERT INTO `sys_user_role` VALUES (336, 4);
INSERT INTO `sys_user_role` VALUES (337, 4);
INSERT INTO `sys_user_role` VALUES (338, 4);
INSERT INTO `sys_user_role` VALUES (339, 4);
INSERT INTO `sys_user_role` VALUES (340, 4);
INSERT INTO `sys_user_role` VALUES (341, 4);
INSERT INTO `sys_user_role` VALUES (342, 4);
INSERT INTO `sys_user_role` VALUES (343, 4);
INSERT INTO `sys_user_role` VALUES (344, 4);
INSERT INTO `sys_user_role` VALUES (345, 4);
INSERT INTO `sys_user_role` VALUES (346, 4);
INSERT INTO `sys_user_role` VALUES (347, 4);
INSERT INTO `sys_user_role` VALUES (348, 4);
INSERT INTO `sys_user_role` VALUES (349, 4);
INSERT INTO `sys_user_role` VALUES (350, 4);
INSERT INTO `sys_user_role` VALUES (351, 4);
INSERT INTO `sys_user_role` VALUES (352, 4);
INSERT INTO `sys_user_role` VALUES (353, 4);
INSERT INTO `sys_user_role` VALUES (354, 4);
INSERT INTO `sys_user_role` VALUES (355, 4);
INSERT INTO `sys_user_role` VALUES (356, 4);
INSERT INTO `sys_user_role` VALUES (357, 4);
INSERT INTO `sys_user_role` VALUES (358, 4);
INSERT INTO `sys_user_role` VALUES (359, 4);
INSERT INTO `sys_user_role` VALUES (360, 4);
INSERT INTO `sys_user_role` VALUES (361, 4);
INSERT INTO `sys_user_role` VALUES (362, 4);
INSERT INTO `sys_user_role` VALUES (363, 4);
INSERT INTO `sys_user_role` VALUES (364, 4);
INSERT INTO `sys_user_role` VALUES (365, 4);
INSERT INTO `sys_user_role` VALUES (366, 4);
INSERT INTO `sys_user_role` VALUES (367, 4);
INSERT INTO `sys_user_role` VALUES (368, 4);
INSERT INTO `sys_user_role` VALUES (369, 4);
INSERT INTO `sys_user_role` VALUES (370, 4);
INSERT INTO `sys_user_role` VALUES (371, 4);
INSERT INTO `sys_user_role` VALUES (372, 4);
INSERT INTO `sys_user_role` VALUES (373, 4);
INSERT INTO `sys_user_role` VALUES (374, 4);
INSERT INTO `sys_user_role` VALUES (375, 4);
INSERT INTO `sys_user_role` VALUES (376, 4);
INSERT INTO `sys_user_role` VALUES (377, 4);
INSERT INTO `sys_user_role` VALUES (378, 4);
INSERT INTO `sys_user_role` VALUES (379, 4);
INSERT INTO `sys_user_role` VALUES (380, 4);
INSERT INTO `sys_user_role` VALUES (381, 4);
INSERT INTO `sys_user_role` VALUES (382, 4);
INSERT INTO `sys_user_role` VALUES (383, 4);
INSERT INTO `sys_user_role` VALUES (384, 4);
INSERT INTO `sys_user_role` VALUES (385, 4);
INSERT INTO `sys_user_role` VALUES (386, 4);
INSERT INTO `sys_user_role` VALUES (387, 4);
INSERT INTO `sys_user_role` VALUES (388, 4);
INSERT INTO `sys_user_role` VALUES (389, 4);
INSERT INTO `sys_user_role` VALUES (390, 4);
INSERT INTO `sys_user_role` VALUES (391, 4);
INSERT INTO `sys_user_role` VALUES (392, 4);
INSERT INTO `sys_user_role` VALUES (393, 4);
INSERT INTO `sys_user_role` VALUES (394, 4);
INSERT INTO `sys_user_role` VALUES (395, 4);
INSERT INTO `sys_user_role` VALUES (396, 4);
INSERT INTO `sys_user_role` VALUES (397, 4);
INSERT INTO `sys_user_role` VALUES (398, 4);
INSERT INTO `sys_user_role` VALUES (399, 4);
INSERT INTO `sys_user_role` VALUES (400, 4);
INSERT INTO `sys_user_role` VALUES (401, 4);
INSERT INTO `sys_user_role` VALUES (402, 4);
INSERT INTO `sys_user_role` VALUES (403, 4);
INSERT INTO `sys_user_role` VALUES (404, 4);
INSERT INTO `sys_user_role` VALUES (405, 4);
INSERT INTO `sys_user_role` VALUES (406, 4);
INSERT INTO `sys_user_role` VALUES (407, 4);
INSERT INTO `sys_user_role` VALUES (408, 4);
INSERT INTO `sys_user_role` VALUES (409, 4);
INSERT INTO `sys_user_role` VALUES (410, 4);
INSERT INTO `sys_user_role` VALUES (411, 4);
INSERT INTO `sys_user_role` VALUES (412, 4);
INSERT INTO `sys_user_role` VALUES (413, 4);
INSERT INTO `sys_user_role` VALUES (414, 4);
INSERT INTO `sys_user_role` VALUES (415, 4);
INSERT INTO `sys_user_role` VALUES (416, 4);
INSERT INTO `sys_user_role` VALUES (417, 4);
INSERT INTO `sys_user_role` VALUES (418, 4);
INSERT INTO `sys_user_role` VALUES (419, 4);
INSERT INTO `sys_user_role` VALUES (420, 4);
INSERT INTO `sys_user_role` VALUES (421, 4);
INSERT INTO `sys_user_role` VALUES (422, 4);
INSERT INTO `sys_user_role` VALUES (423, 4);
INSERT INTO `sys_user_role` VALUES (424, 4);
INSERT INTO `sys_user_role` VALUES (425, 4);
INSERT INTO `sys_user_role` VALUES (426, 4);
INSERT INTO `sys_user_role` VALUES (427, 4);
INSERT INTO `sys_user_role` VALUES (428, 4);
INSERT INTO `sys_user_role` VALUES (429, 4);
INSERT INTO `sys_user_role` VALUES (430, 4);
INSERT INTO `sys_user_role` VALUES (431, 4);
INSERT INTO `sys_user_role` VALUES (432, 4);
INSERT INTO `sys_user_role` VALUES (433, 4);
INSERT INTO `sys_user_role` VALUES (434, 4);
INSERT INTO `sys_user_role` VALUES (435, 4);
INSERT INTO `sys_user_role` VALUES (436, 4);
INSERT INTO `sys_user_role` VALUES (437, 4);
INSERT INTO `sys_user_role` VALUES (438, 4);
INSERT INTO `sys_user_role` VALUES (439, 4);
INSERT INTO `sys_user_role` VALUES (440, 4);
INSERT INTO `sys_user_role` VALUES (441, 4);
INSERT INTO `sys_user_role` VALUES (442, 4);
INSERT INTO `sys_user_role` VALUES (443, 4);
INSERT INTO `sys_user_role` VALUES (444, 4);
INSERT INTO `sys_user_role` VALUES (445, 4);
INSERT INTO `sys_user_role` VALUES (446, 4);
INSERT INTO `sys_user_role` VALUES (447, 4);
INSERT INTO `sys_user_role` VALUES (448, 4);
INSERT INTO `sys_user_role` VALUES (449, 4);
INSERT INTO `sys_user_role` VALUES (450, 4);
INSERT INTO `sys_user_role` VALUES (451, 4);
INSERT INTO `sys_user_role` VALUES (452, 4);
INSERT INTO `sys_user_role` VALUES (453, 4);
INSERT INTO `sys_user_role` VALUES (454, 4);
INSERT INTO `sys_user_role` VALUES (455, 4);
INSERT INTO `sys_user_role` VALUES (456, 4);
INSERT INTO `sys_user_role` VALUES (457, 4);
INSERT INTO `sys_user_role` VALUES (458, 4);
INSERT INTO `sys_user_role` VALUES (459, 4);
INSERT INTO `sys_user_role` VALUES (460, 4);
INSERT INTO `sys_user_role` VALUES (461, 4);
INSERT INTO `sys_user_role` VALUES (462, 4);
INSERT INTO `sys_user_role` VALUES (463, 4);
INSERT INTO `sys_user_role` VALUES (464, 4);
INSERT INTO `sys_user_role` VALUES (465, 4);
INSERT INTO `sys_user_role` VALUES (466, 4);
INSERT INTO `sys_user_role` VALUES (467, 4);
INSERT INTO `sys_user_role` VALUES (468, 4);
INSERT INTO `sys_user_role` VALUES (469, 4);
INSERT INTO `sys_user_role` VALUES (470, 4);
INSERT INTO `sys_user_role` VALUES (471, 4);
INSERT INTO `sys_user_role` VALUES (472, 4);
INSERT INTO `sys_user_role` VALUES (473, 4);
INSERT INTO `sys_user_role` VALUES (474, 4);
INSERT INTO `sys_user_role` VALUES (475, 4);
INSERT INTO `sys_user_role` VALUES (476, 4);
INSERT INTO `sys_user_role` VALUES (477, 4);
INSERT INTO `sys_user_role` VALUES (478, 4);
INSERT INTO `sys_user_role` VALUES (479, 4);
INSERT INTO `sys_user_role` VALUES (480, 4);
INSERT INTO `sys_user_role` VALUES (481, 4);
INSERT INTO `sys_user_role` VALUES (482, 4);
INSERT INTO `sys_user_role` VALUES (483, 4);
INSERT INTO `sys_user_role` VALUES (484, 4);
INSERT INTO `sys_user_role` VALUES (485, 4);
INSERT INTO `sys_user_role` VALUES (486, 4);
INSERT INTO `sys_user_role` VALUES (487, 4);
INSERT INTO `sys_user_role` VALUES (488, 4);
INSERT INTO `sys_user_role` VALUES (489, 4);
INSERT INTO `sys_user_role` VALUES (490, 4);
INSERT INTO `sys_user_role` VALUES (491, 4);
INSERT INTO `sys_user_role` VALUES (492, 4);
INSERT INTO `sys_user_role` VALUES (493, 4);
INSERT INTO `sys_user_role` VALUES (494, 4);
INSERT INTO `sys_user_role` VALUES (495, 4);
INSERT INTO `sys_user_role` VALUES (496, 4);
INSERT INTO `sys_user_role` VALUES (497, 4);
INSERT INTO `sys_user_role` VALUES (498, 4);
INSERT INTO `sys_user_role` VALUES (499, 4);
INSERT INTO `sys_user_role` VALUES (500, 4);
INSERT INTO `sys_user_role` VALUES (501, 4);
INSERT INTO `sys_user_role` VALUES (502, 4);
INSERT INTO `sys_user_role` VALUES (503, 4);
INSERT INTO `sys_user_role` VALUES (504, 4);
INSERT INTO `sys_user_role` VALUES (505, 4);
INSERT INTO `sys_user_role` VALUES (506, 4);
INSERT INTO `sys_user_role` VALUES (507, 4);
INSERT INTO `sys_user_role` VALUES (508, 4);
INSERT INTO `sys_user_role` VALUES (509, 4);
INSERT INTO `sys_user_role` VALUES (510, 4);
INSERT INTO `sys_user_role` VALUES (511, 4);
INSERT INTO `sys_user_role` VALUES (512, 4);
INSERT INTO `sys_user_role` VALUES (513, 4);
INSERT INTO `sys_user_role` VALUES (514, 4);
INSERT INTO `sys_user_role` VALUES (515, 4);
INSERT INTO `sys_user_role` VALUES (516, 4);
INSERT INTO `sys_user_role` VALUES (517, 4);
INSERT INTO `sys_user_role` VALUES (518, 4);
INSERT INTO `sys_user_role` VALUES (519, 4);
INSERT INTO `sys_user_role` VALUES (520, 4);
INSERT INTO `sys_user_role` VALUES (521, 4);
INSERT INTO `sys_user_role` VALUES (522, 4);
INSERT INTO `sys_user_role` VALUES (523, 4);
INSERT INTO `sys_user_role` VALUES (524, 4);
INSERT INTO `sys_user_role` VALUES (525, 4);
INSERT INTO `sys_user_role` VALUES (526, 4);
INSERT INTO `sys_user_role` VALUES (527, 4);
INSERT INTO `sys_user_role` VALUES (528, 4);
INSERT INTO `sys_user_role` VALUES (529, 4);
INSERT INTO `sys_user_role` VALUES (530, 4);
INSERT INTO `sys_user_role` VALUES (531, 4);
INSERT INTO `sys_user_role` VALUES (532, 4);
INSERT INTO `sys_user_role` VALUES (533, 4);
INSERT INTO `sys_user_role` VALUES (534, 4);
INSERT INTO `sys_user_role` VALUES (535, 4);
INSERT INTO `sys_user_role` VALUES (536, 4);
INSERT INTO `sys_user_role` VALUES (537, 4);
INSERT INTO `sys_user_role` VALUES (538, 4);
INSERT INTO `sys_user_role` VALUES (539, 4);
INSERT INTO `sys_user_role` VALUES (540, 4);
INSERT INTO `sys_user_role` VALUES (541, 4);
INSERT INTO `sys_user_role` VALUES (542, 4);
INSERT INTO `sys_user_role` VALUES (543, 4);
INSERT INTO `sys_user_role` VALUES (544, 4);
INSERT INTO `sys_user_role` VALUES (545, 4);
INSERT INTO `sys_user_role` VALUES (546, 4);
INSERT INTO `sys_user_role` VALUES (547, 4);
INSERT INTO `sys_user_role` VALUES (548, 4);
INSERT INTO `sys_user_role` VALUES (549, 4);
INSERT INTO `sys_user_role` VALUES (550, 4);
INSERT INTO `sys_user_role` VALUES (551, 4);
INSERT INTO `sys_user_role` VALUES (554, 102);

SET FOREIGN_KEY_CHECKS = 1;
