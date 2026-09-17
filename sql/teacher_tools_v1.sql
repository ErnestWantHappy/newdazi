-- 教师工具单页导航 v1
-- MySQL 8；可重复执行。只新增本平台数据，不修改 80/3005 旧站。

CREATE TABLE IF NOT EXISTS biz_teacher_tool_category (
    category_id       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分类主键',
    category_code     VARCHAR(32)  NOT NULL COMMENT '稳定分类编码',
    category_name     VARCHAR(50)  NOT NULL COMMENT '分类名称',
    description       VARCHAR(200) NULL COMMENT '分类说明',
    icon              VARCHAR(50)  NULL COMMENT '平台SVG图标名称',
    section_level     VARCHAR(16)  NOT NULL DEFAULT 'SECONDARY' COMMENT 'PRIMARY重点/SECONDARY次要',
    default_expanded  CHAR(1)      NOT NULL DEFAULT 'N' COMMENT 'Y默认展开/N默认折叠',
    preview_limit     INT          NOT NULL DEFAULT 4 COMMENT '折叠时预览数量',
    sort_order        INT          NOT NULL DEFAULT 100 COMMENT '页面排序',
    status            CHAR(1)      NOT NULL DEFAULT '0' COMMENT '0启用/1停用',
    del_flag          CHAR(1)      NOT NULL DEFAULT '0' COMMENT '0正常/2删除',
    create_by         VARCHAR(64)  NULL,
    create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by         VARCHAR(64)  NULL,
    update_time       DATETIME     NULL,
    PRIMARY KEY (category_id),
    UNIQUE KEY uk_teacher_tool_category_code (category_code),
    KEY idx_teacher_tool_category_sort (status, del_flag, sort_order),
    CONSTRAINT chk_teacher_tool_category_level CHECK (section_level IN ('PRIMARY', 'SECONDARY')),
    CONSTRAINT chk_teacher_tool_category_preview CHECK (preview_limit BETWEEN 1 AND 20)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='教师工具分类';

CREATE TABLE IF NOT EXISTS biz_teacher_tool (
    tool_id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '工具主键',
    title              VARCHAR(100)  NOT NULL COMMENT '工具名称',
    description        VARCHAR(500)  NOT NULL COMMENT '一行用途说明',
    url                VARCHAR(1000) NOT NULL COMMENT 'http/https工具地址',
    icon_url           VARCHAR(1000) NULL COMMENT '图标地址，失败时前端回退',
    tags               VARCHAR(500)  NULL COMMENT '逗号分隔标签',
    access_type        VARCHAR(20)   NOT NULL DEFAULT 'DIRECT' COMMENT 'DIRECT/LOGIN_REQUIRED/INTRANET_ONLY/DOWNLOAD',
    source_type        VARCHAR(20)   NOT NULL DEFAULT 'MANUAL' COMMENT 'LOCAL_3005/LOCAL_80/ZJ_DISCIPLINE/MANUAL',
    source_ref         VARCHAR(200)  NULL COMMENT '首次导入来源稳定标识',
    is_recommended     CHAR(1)       NOT NULL DEFAULT 'N' COMMENT 'Y常用推荐/N普通',
    recommend_order    INT           NOT NULL DEFAULT 100 COMMENT '推荐区排序',
    sort_order         INT           NOT NULL DEFAULT 100 COMMENT '分类内排序',
    status             CHAR(1)       NOT NULL DEFAULT '0' COMMENT '0上架/1下架',
    del_flag           CHAR(1)       NOT NULL DEFAULT '0' COMMENT '0正常/2软删除',
    create_by          VARCHAR(64)   NULL,
    create_time        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by          VARCHAR(64)   NULL,
    update_time        DATETIME      NULL,
    PRIMARY KEY (tool_id),
    UNIQUE KEY uk_teacher_tool_source (source_type, source_ref),
    KEY idx_teacher_tool_catalog (status, del_flag, sort_order),
    KEY idx_teacher_tool_recommend (is_recommended, recommend_order),
    CONSTRAINT chk_teacher_tool_access CHECK (access_type IN ('DIRECT', 'LOGIN_REQUIRED', 'INTRANET_ONLY', 'DOWNLOAD')),
    CONSTRAINT chk_teacher_tool_source CHECK (source_type IN ('LOCAL_3005', 'LOCAL_80', 'ZJ_DISCIPLINE', 'MANUAL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='教师工具';

CREATE TABLE IF NOT EXISTS biz_teacher_tool_category_rel (
    tool_id       BIGINT   NOT NULL COMMENT '工具ID',
    category_id   BIGINT   NOT NULL COMMENT '分类ID',
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (tool_id, category_id),
    KEY idx_teacher_tool_rel_category (category_id, tool_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='教师工具多分类关系';

INSERT INTO biz_teacher_tool_category
    (category_code, category_name, description, icon, section_level, default_expanded, preview_limit, sort_order, status, del_flag, create_by)
SELECT s.category_code, s.category_name, s.description, s.icon, s.section_level, s.default_expanded, s.preview_limit, s.sort_order, '0', '0', 'system'
FROM (
    SELECT 'primary-school' category_code, '小学实用工具' category_name, '小学信息科技课堂常用实验与练习入口' description, 'education' icon, 'PRIMARY' section_level, 'Y' default_expanded, 4 preview_limit, 10 sort_order
    UNION ALL SELECT 'grade-seven', '七年级实用工具', '按七年级教材课次整理的课堂工具', 'guide', 'PRIMARY', 'Y', 4, 20
    UNION ALL SELECT 'grade-eight', '八年级实用工具', '按八年级教材课次整理的课堂工具', 'skill', 'PRIMARY', 'Y', 4, 30
    UNION ALL SELECT 'zj-discipline', '省平台学科工具', '中小学信息科技与人工智能学习平台公开学科工具入口', 'international', 'PRIMARY', 'Y', 4, 40
    UNION ALL SELECT 'student-no-login', '学生免登录工具', '适合课堂直接发给学生使用的入口', 'peoples', 'SECONDARY', 'N', 4, 50
    UNION ALL SELECT 'ai-websites', 'AI 网站', '备课、创作、搜索与智能应用开发工具', 'chat', 'SECONDARY', 'N', 4, 60
    UNION ALL SELECT 'programming-learning', '编程学习', '编程课程、教程与硬件开发资料', 'code', 'SECONDARY', 'N', 4, 70
    UNION ALL SELECT 'office-materials', '办公与素材', '文档、图片、演示文稿和素材处理工具', 'documentation', 'SECONDARY', 'N', 4, 80
    UNION ALL SELECT 'teacher-websites', '教师通用网站', '教师研修、资源、评价与公共服务平台', 'education', 'SECONDARY', 'N', 4, 90
    UNION ALL SELECT 'homeroom-tools', '班主任工具', '班级管理、点名、成绩和考试辅助工具', 'people', 'SECONDARY', 'N', 4, 100
    UNION ALL SELECT 'cross-subject', '跨学科工具', '语文、英语、音乐、生物等学科教学应用', 'tree', 'SECONDARY', 'N', 4, 110
    UNION ALL SELECT 'regional-platforms', '校本与区域平台', '象山区域及校本系统入口', 'build', 'SECONDARY', 'N', 4, 120
) s
WHERE NOT EXISTS (SELECT 1 FROM biz_teacher_tool_category c WHERE c.category_code=s.category_code);

-- 教师侧一级入口紧跟教师首页；管理权限作为隐藏按钮能力，不额外占菜单。
INSERT INTO sys_menu
    (menu_name, parent_id, order_num, path, component, query, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '教师工具', 0, 0, 'teacher-tools', 'business/teacherTools/index', '', 'TeacherTools',
       1, 0, 'C', '0', '0', 'business:teacherTool:list', 'tool', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='business:teacherTool:list');

SET @teacher_tool_menu_id := (SELECT menu_id FROM sys_menu WHERE perms='business:teacherTool:list' ORDER BY menu_id LIMIT 1);

INSERT INTO sys_menu
    (menu_name, parent_id, order_num, path, component, query, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '教师工具管理', @teacher_tool_menu_id, 1, '#', '', '', '',
       1, 0, 'F', '1', '0', 'business:teacherTool:manage', '#', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='business:teacherTool:manage');

SET @teacher_tool_manage_menu_id := (SELECT menu_id FROM sys_menu WHERE perms='business:teacherTool:manage' ORDER BY menu_id LIMIT 1);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, @teacher_tool_menu_id FROM sys_role r
WHERE r.role_key IN ('admin', 'teacher', 'researcher')
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=@teacher_tool_menu_id);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, @teacher_tool_manage_menu_id FROM sys_role r
WHERE r.role_key IN ('admin', 'researcher')
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=@teacher_tool_manage_menu_id);

DELETE rm FROM sys_role_menu rm
JOIN sys_role r ON r.role_id=rm.role_id AND r.role_key='student'
WHERE rm.menu_id IN (@teacher_tool_menu_id, @teacher_tool_manage_menu_id);

-- 首批清洗数据：不含旧站循环入口、明文凭据、私人协作房间、娱乐站、翻墙入口和高风险下载站。
INSERT INTO biz_teacher_tool
    (title, description, url, tags, access_type, source_type, source_ref, is_recommended, recommend_order, sort_order, status, del_flag, create_by)
SELECT s.title, s.description, s.url, s.tags, s.access_type, s.source_type, s.source_ref, s.is_recommended, s.recommend_order, s.sort_order, '0', '0', 'system'
FROM (
    SELECT '打字平台' title, '课堂五分钟键盘录入练习' description, 'http://10.52.1.94/' url, '打字,键盘,学生练习' tags, 'INTRANET_ONLY' access_type, 'LOCAL_3005' source_type, '3005-typing' source_ref, 'Y' is_recommended, 10 recommend_order, 10 sort_order, 'primary-school' category_code
    UNION ALL SELECT '小学信息科技实验教学导航', '小学信息科技实验工具集合', 'http://10.52.1.123:3003/', '小学,实验,信息科技', 'INTRANET_ONLY', 'LOCAL_3005', '3005-primary-lab', 'Y', 20, 20, 'primary-school'
    UNION ALL SELECT '七上邮件系统', '七年级电子邮件课堂模拟系统，账号由任课教师分配', 'http://10.52.1.123:3002/', '七上,邮件,课堂实验', 'INTRANET_ONLY', 'LOCAL_3005', '3005-mail', 'N', 100, 10, 'grade-seven'
    UNION ALL SELECT '小型网络搭建仿真', '拖动设备完成小型网络搭建练习', 'http://10.52.1.123:3020/', '七上,网络,仿真', 'INTRANET_ONLY', 'LOCAL_3005', '3005-network', 'Y', 30, 20, 'grade-seven'
    UNION ALL SELECT '草料二维码', '快速生成并美化二维码', 'https://cli.im/', '七下,二维码,生成器', 'DIRECT', 'LOCAL_3005', '3005-cli', 'N', 100, 30, 'grade-seven'
    UNION ALL SELECT '物联网数据流程演示', '演示物联网数据发送过程', 'http://10.52.1.123:3006/', '七下,物联网,流程', 'INTRANET_ONLY', 'LOCAL_3005', '3005-iot-flow', 'N', 100, 40, 'grade-seven'
    UNION ALL SELECT '文字转语音', '将文本快速转换为语音，适合课堂素材制作', 'https://www.ttson.cn/', '八下,文字转语音,音频', 'DIRECT', 'LOCAL_3005', '3005-tts', 'N', 100, 10, 'grade-eight'
    UNION ALL SELECT '图像识别课堂应用', '八年级图像识别实验应用', 'http://10.52.1.123:3001/', '八下,图像识别,人工智能', 'INTRANET_ONLY', 'LOCAL_3005', '3005-image-recognition', 'Y', 40, 20, 'grade-eight'

    UNION ALL SELECT '小程序教育平台', '省平台小程序教育工具入口', 'https://xxkj.zjer.cn/disciplinaryTool?active=I10', '省平台,小程序,编程', 'LOGIN_REQUIRED', 'ZJ_DISCIPLINE', 'zj-I10', 'N', 100, 10, 'zj-discipline'
    UNION ALL SELECT '物联数据中台', '省平台物联网数据实验入口', 'https://xxkj.zjer.cn/disciplinaryTool?active=I4', '省平台,物联网,数据', 'LOGIN_REQUIRED', 'ZJ_DISCIPLINE', 'zj-I4', 'Y', 50, 20, 'zj-discipline'
    UNION ALL SELECT 'Python 编程', '省平台 Python 在线编程入口', 'https://xxkj.zjer.cn/disciplinaryTool?active=I1', '省平台,Python,编程', 'LOGIN_REQUIRED', 'ZJ_DISCIPLINE', 'zj-I1', 'Y', 60, 30, 'zj-discipline'
    UNION ALL SELECT '在线流程图', '省平台在线流程图绘制工具', 'https://xxkj.zjer.cn/disciplinaryTool?active=I5', '省平台,流程图,实用工具', 'LOGIN_REQUIRED', 'ZJ_DISCIPLINE', 'zj-I5', 'N', 100, 40, 'zj-discipline'
    UNION ALL SELECT '在线打字', '省平台键盘输入练习工具', 'https://xxkj.zjer.cn/disciplinaryTool?active=I6', '省平台,打字,实用工具', 'LOGIN_REQUIRED', 'ZJ_DISCIPLINE', 'zj-I6', 'N', 100, 50, 'zj-discipline'
    UNION ALL SELECT '场景实验', '省平台人工智能场景实验入口', 'https://xxkj.zjer.cn/disciplinaryTool?active=I9', '省平台,人工智能,场景实验', 'LOGIN_REQUIRED', 'ZJ_DISCIPLINE', 'zj-I9', 'N', 100, 60, 'zj-discipline'
    UNION ALL SELECT '在线协作', '省平台多人在线协作工具', 'https://xxkj.zjer.cn/disciplinaryTool?active=I8', '省平台,协作,实用工具', 'LOGIN_REQUIRED', 'ZJ_DISCIPLINE', 'zj-I8', 'N', 100, 70, 'zj-discipline'
    UNION ALL SELECT '在线文档', '省平台在线文档编辑工具', 'https://xxkj.zjer.cn/disciplinaryTool?active=I7', '省平台,文档,实用工具', 'LOGIN_REQUIRED', 'ZJ_DISCIPLINE', 'zj-I7', 'N', 100, 80, 'zj-discipline'
    UNION ALL SELECT '图形化编程', '省平台图形化编程工具', 'https://xxkj.zjer.cn/disciplinaryTool?active=I3', '省平台,图形化编程,算法', 'LOGIN_REQUIRED', 'ZJ_DISCIPLINE', 'zj-I3', 'N', 100, 90, 'zj-discipline'
    UNION ALL SELECT '校园精灵', '省平台校园智能助手入口', 'https://xxkj.zjer.cn/disciplinaryTool?active=I11', '省平台,人工智能,校园', 'LOGIN_REQUIRED', 'ZJ_DISCIPLINE', 'zj-I11', 'N', 100, 100, 'zj-discipline'

    UNION ALL SELECT '当贝 AI', '面向学生的多模型智能对话入口', 'https://ai.dangbei.com/', 'AI,学生,对话', 'DIRECT', 'LOCAL_3005', '3005-dangbei-ai', 'N', 100, 10, 'student-no-login'
    UNION ALL SELECT '问小白 AI', '便捷的智能问答与学习助手', 'https://www.wenxiaobai.com/', 'AI,学生,问答', 'DIRECT', 'LOCAL_3005', '3005-wenxiaobai', 'N', 100, 20, 'student-no-login'
    UNION ALL SELECT '百度图片', '图片检索与基础智能图片服务', 'https://image.baidu.com/', '图片,学生,检索', 'DIRECT', 'LOCAL_3005', '3005-baidu-image', 'N', 100, 30, 'student-no-login'
    UNION ALL SELECT 'Raphael AI 绘图', '浏览器内快速生成教学配图', 'https://raphael.app/zh', 'AI绘图,图片,免登录', 'DIRECT', 'LOCAL_3005', '3005-raphael', 'N', 100, 40, 'student-no-login'
    UNION ALL SELECT '一小时编程', '游戏化编程入门课程', 'https://hourofcode.com/cn/zh/learn', '编程,学生,游戏化学习', 'DIRECT', 'LOCAL_3005', '3005-hour-of-code', 'N', 100, 50, 'student-no-login'

    UNION ALL SELECT '象山 AI', '象山区域自建智能对话应用', 'http://10.52.1.124/chat/D3bSGCRWkFzdH0Si', 'AI,象山,区域应用', 'INTRANET_ONLY', 'LOCAL_3005', '3005-xiangshan-ai', 'Y', 70, 10, 'ai-websites'
    UNION ALL SELECT '豆包', '智能对话、写作与图片生成', 'https://www.doubao.com/', 'AI,对话,图片生成', 'LOGIN_REQUIRED', 'LOCAL_3005', '3005-doubao', 'N', 100, 20, 'ai-websites'
    UNION ALL SELECT 'DeepSeek', '通用推理、写作与备课助手', 'https://chat.deepseek.com/', 'AI,推理,备课', 'LOGIN_REQUIRED', 'LOCAL_3005', '3005-deepseek', 'Y', 80, 30, 'ai-websites'
    UNION ALL SELECT 'Kimi', '长文档阅读、写作与演示文稿辅助', 'https://kimi.moonshot.cn/', 'AI,长文档,PPT', 'LOGIN_REQUIRED', 'LOCAL_3005', '3005-kimi', 'N', 100, 40, 'ai-websites'
    UNION ALL SELECT '通义千问', '通用写作、对话与教学内容生成', 'https://tongyi.aliyun.com/qianwen/', 'AI,写作,对话', 'LOGIN_REQUIRED', 'LOCAL_3005', '3005-qwen', 'N', 100, 50, 'ai-websites'
    UNION ALL SELECT '秘塔 AI 搜索', '带来源的智能资料检索', 'https://metaso.cn/', 'AI搜索,资料,备课', 'DIRECT', 'LOCAL_3005', '3005-metaso', 'N', 100, 60, 'ai-websites'
    UNION ALL SELECT '秘塔 AI 学习', '面向学习场景的智能辅助工具', 'https://metaso.cn/study', 'AI,学习,学生', 'DIRECT', 'LOCAL_3005', '3005-metaso-study', 'N', 100, 70, 'ai-websites'
    UNION ALL SELECT '百度千帆 AppBuilder', '搭建智能体与工作流应用', 'https://console.bce.baidu.com/ai_apaas/personalSpace/app', 'AI开发,智能体,工作流', 'LOGIN_REQUIRED', 'LOCAL_3005', '3005-baidu-appbuilder', 'N', 100, 80, 'ai-websites'
    UNION ALL SELECT '百度智能云控制台', '管理百度智能云 AI 服务与接口', 'https://console.bce.baidu.com/ai/#/ai/intelligentwriting/app/list', 'AI开发,云服务,API', 'LOGIN_REQUIRED', 'LOCAL_3005', '3005-baidu-console', 'N', 100, 90, 'ai-websites'
    UNION ALL SELECT '百度 UNIT', '创建和维护对话机器人', 'https://ai.baidu.com/unit/v2#/myrobot', 'AI开发,机器人,对话', 'LOGIN_REQUIRED', 'LOCAL_3005', '3005-baidu-unit', 'N', 100, 100, 'ai-websites'
    UNION ALL SELECT '大模型竞技场', '对比不同大模型输出表现', 'https://lmarena.ai/', 'AI,模型评测,对比', 'LOGIN_REQUIRED', 'LOCAL_3005', '3005-lmarena', 'N', 100, 110, 'ai-websites'
    UNION ALL SELECT 'Noiz AI 配音', '生成课堂旁白和配音素材', 'https://noiz.ai/landing', 'AI配音,音频,素材', 'LOGIN_REQUIRED', 'LOCAL_3005', '3005-noiz', 'N', 100, 120, 'ai-websites'
    UNION ALL SELECT 'Viggle AI 视频', '使用图片驱动人物视频生成', 'https://www.viggle.ai/home-gallery', 'AI视频,人物,素材', 'LOGIN_REQUIRED', 'LOCAL_3005', '3005-viggle', 'N', 100, 130, 'ai-websites'
    UNION ALL SELECT '即梦 AI', '生成教学图片和短视频素材', 'https://jimeng.jianying.com/ai-tool/home', 'AI,图片,视频', 'LOGIN_REQUIRED', 'LOCAL_3005', '3005-jimeng', 'N', 100, 140, 'ai-websites'
    UNION ALL SELECT 'Gamma', 'AI 辅助生成演示文稿', 'https://gamma.app/', 'AI,PPT,演示文稿', 'LOGIN_REQUIRED', 'LOCAL_3005', '3005-gamma', 'N', 100, 150, 'ai-websites'
    UNION ALL SELECT 'Dify 应用平台', '象山内网大语言模型应用开发平台', 'http://10.52.1.124/apps', 'AI开发,Dify,内网', 'INTRANET_ONLY', 'LOCAL_3005', '3005-dify', 'N', 100, 160, 'ai-websites'

    UNION ALL SELECT '菜鸟教程', '覆盖多种编程语言和开发技术的中文教程', 'https://www.runoob.com/', '编程,教程,开发', 'DIRECT', 'LOCAL_3005', '3005-runoob', 'Y', 90, 10, 'programming-learning'
    UNION ALL SELECT 'W3School', 'Web 前端技术基础教程', 'https://www.w3school.com.cn/', 'Web,前端,教程', 'DIRECT', 'LOCAL_3005', '3005-w3school', 'N', 100, 20, 'programming-learning'
    UNION ALL SELECT '慕课网', 'IT 与设计类在线课程平台', 'https://www.imooc.com/', '编程,课程,设计', 'LOGIN_REQUIRED', 'LOCAL_3005', '3005-imooc', 'N', 100, 30, 'programming-learning'
    UNION ALL SELECT '掌控板开发教程', '掌控板与 MicroPython 开发文档', 'https://mpythonsoftware.readthedocs.io/zh/master/', '掌控板,MicroPython,硬件', 'DIRECT', 'LOCAL_3005', '3005-mpython-doc', 'N', 100, 40, 'programming-learning'

    UNION ALL SELECT '文叔叔', '大文件临时传输与分享', 'https://www.wenshushu.cn/records', '文件传输,分享,办公', 'LOGIN_REQUIRED', 'LOCAL_3005', '3005-wenshushu', 'N', 100, 10, 'office-materials'
    UNION ALL SELECT '办公人导航', '办公工具与效率网站分类导航', 'https://www.bgrdh.com/', '办公,导航,效率', 'DIRECT', 'LOCAL_3005', '3005-bgrdh', 'N', 100, 20, 'office-materials'
    UNION ALL SELECT 'PDF24 工具箱', '合并、拆分、压缩和转换 PDF', 'https://tools.pdf24.org/zh/', 'PDF,转换,办公', 'DIRECT', 'LOCAL_3005', '3005-pdf24', 'Y', 100, 30, 'office-materials'
    UNION ALL SELECT 'Optimizilla 图片压缩', '在线批量压缩 JPG 和 PNG 图片', 'https://imagecompressor.com/zh/', '图片压缩,素材,办公', 'DIRECT', 'LOCAL_3005', '3005-optimizilla', 'N', 100, 40, 'office-materials'
    UNION ALL SELECT 'TinyPNG', '压缩 WebP、JPEG 和 PNG 图片', 'https://tinypng.com/', '图片压缩,WebP,素材', 'DIRECT', 'LOCAL_3005', '3005-tinypng', 'N', 100, 50, 'office-materials'
    UNION ALL SELECT 'Ezgif', '制作、裁剪和优化 GIF 动图', 'https://ezgif.com/', 'GIF,动图,素材', 'DIRECT', 'LOCAL_3005', '3005-ezgif', 'N', 100, 60, 'office-materials'
    UNION ALL SELECT 'Aspose 表格转换', '在线转换 Excel 和表格文件格式', 'https://products.aspose.app/cells/zh/conversion', 'Excel,格式转换,办公', 'DIRECT', 'LOCAL_3005', '3005-aspose-cells', 'N', 100, 70, 'office-materials'
    UNION ALL SELECT 'GoQR', '简洁的在线二维码生成器', 'https://goqr.me/', '二维码,生成器,办公', 'DIRECT', 'LOCAL_3005', '3005-goqr', 'N', 100, 80, 'office-materials'
    UNION ALL SELECT 'Iconfont', '阿里巴巴矢量图标素材库', 'https://www.iconfont.cn/', '图标,素材,设计', 'LOGIN_REQUIRED', 'LOCAL_3005', '3005-iconfont', 'N', 100, 90, 'office-materials'
    UNION ALL SELECT '优品 PPT', 'PPT 模板、图表和背景素材', 'https://www.ypppt.com/', 'PPT,模板,素材', 'DIRECT', 'LOCAL_3005', '3005-ypppt', 'N', 100, 100, 'office-materials'
    UNION ALL SELECT 'Pexels', '可用于教学创作的免费图片和视频素材', 'https://www.pexels.com/zh-cn/', '图片,视频,免费素材', 'DIRECT', 'LOCAL_3005', '3005-pexels', 'N', 100, 110, 'office-materials'

    UNION ALL SELECT '中小学信息科技与人工智能学习平台', '浙江省信息科技与人工智能教学资源平台', 'https://xxkj.zjer.cn/home/index', '信息科技,人工智能,省平台', 'LOGIN_REQUIRED', 'LOCAL_3005', '3005-xxkj-home', 'Y', 110, 10, 'teacher-websites'
    UNION ALL SELECT '国家中小学智慧教育平台', '国家课程资源与教师研修平台', 'https://basic.smartedu.cn/', '国家平台,研修,资源', 'LOGIN_REQUIRED', 'LOCAL_3005', '3005-smartedu', 'N', 100, 20, 'teacher-websites'
    UNION ALL SELECT '21 世纪教育网', '学科教学资源与备课服务', 'https://passport.21cnjy.com/', '教学资源,备课,教师', 'LOGIN_REQUIRED', 'LOCAL_3005', '3005-21cnjy', 'N', 100, 30, 'teacher-websites'
    UNION ALL SELECT '浙江教师培训管理平台', '教师培训报名与学分管理', 'https://pxglpt.zjedu.gov.cn/Login.aspx', '教师培训,学分,浙江', 'LOGIN_REQUIRED', 'LOCAL_3005', '3005-zj-training', 'N', 100, 40, 'teacher-websites'
    UNION ALL SELECT '国家教育质量监测平台', '国家教育质量监测业务入口', 'https://eachina.changyan.cn/portalweb/index.html', '质量监测,教育评价,国家平台', 'LOGIN_REQUIRED', 'LOCAL_3005', '3005-national-assessment', 'N', 100, 50, 'teacher-websites'
    UNION ALL SELECT '专业技术人员考试报名', '全国专业技术人员资格考试报名服务', 'https://zg.cpta.com.cn/examfront/login/saveLogin.htm#', '考试报名,教师服务', 'LOGIN_REQUIRED', 'LOCAL_3005', '3005-cpta', 'N', 100, 60, 'teacher-websites'
    UNION ALL SELECT '浙江万里学院培训平台', '非学历培训学习入口', 'https://fxl.zwu.edu.cn/mizar/studentspace/study/listStuding.do', '培训,继续教育', 'LOGIN_REQUIRED', 'LOCAL_3005', '3005-zwu', 'N', 100, 70, 'teacher-websites'
    UNION ALL SELECT '宁波网络图书馆', '宁波图书馆数字资源入口', 'https://elib.nblib.cn/SSO/main/main.jsp', '图书馆,数字资源,宁波', 'LOGIN_REQUIRED', 'LOCAL_3005', '3005-nblib', 'N', 100, 80, 'teacher-websites'
    UNION ALL SELECT '灯塔系统学校端', '学校学生综合评价管理入口', 'https://school.lighthouse.ren/', '学生评价,学校,管理', 'LOGIN_REQUIRED', 'LOCAL_3005', '3005-lighthouse', 'N', 100, 90, 'teacher-websites'
    UNION ALL SELECT 'Seed 平台', '学生综合评价与发展平台', 'https://seed.bnu.edu.cn/tree/index.html', '学生评价,发展,教师', 'LOGIN_REQUIRED', 'LOCAL_3005', '3005-seed', 'N', 100, 100, 'teacher-websites'

    UNION ALL SELECT '课堂随机点名系统', '导入名单后随机点名并保留抽取记录', 'https://www.alipan.com/s/F1SXTVpMHbQ', '班主任,点名,课堂', 'DOWNLOAD', 'LOCAL_80', 'root-01-random', 'N', 100, 10, 'homeroom-tools'
    UNION ALL SELECT '班级电子宠物养成系统', '用积分与奖励小铺辅助班级管理', 'https://www.alipan.com/s/TWmwzXGEpgR', '班主任,班级管理,积分', 'DOWNLOAD', 'LOCAL_80', 'root-03-pet', 'N', 100, 20, 'homeroom-tools'
    UNION ALL SELECT '考试座位贴生成工具', '导入 Excel 自动生成可打印考试座位贴', 'https://1831383013.share.123865.com/123pan/uwa6jv-kXM13', '班主任,考试,Word', 'DOWNLOAD', 'LOCAL_80', 'root-05-seat', 'N', 100, 30, 'homeroom-tools'
    UNION ALL SELECT '成绩分析系统', '导入 Excel 完成排名、班级统计和报表导出', 'https://1831383013.share.123865.com/123pan/uwa6jv-zzS13', '成绩分析,Excel,班主任', 'DOWNLOAD', 'LOCAL_80', 'root-06-score', 'N', 100, 40, 'homeroom-tools'
    UNION ALL SELECT '错题刷题系统 2.0', '上传 Excel 题库，支持刷题、错题本和成绩统计', 'http://10.52.1.123/aicuoti/', '刷题,错题,学业评价', 'DIRECT', 'LOCAL_80', 'root-04-wrong-question', 'N', 100, 50, 'homeroom-tools'

    UNION ALL SELECT 'AI 学业评价系统', 'AI 生成测评题并汇总班级学情与教学建议', 'http://10.52.1.123/aiquanxuekexiangshan/', 'AI,学业评价,学情分析', 'DIRECT', 'LOCAL_80', 'root-07-ai-assessment', 'Y', 120, 10, 'cross-subject'
    UNION ALL SELECT '学生免登录音乐学习工具', '音阶热身、音高反馈和 K 歌练习', 'https://pan.quark.cn/s/072499fe829b', '音乐,音准,学生练习', 'DOWNLOAD', 'LOCAL_80', 'root-08-music', 'N', 100, 20, 'cross-subject'
    UNION ALL SELECT 'AI 音乐学习工具 2.0', '支持自定义歌曲、试唱和学生创作的音乐练习平台', 'https://music.teacherlin.store/miandenglu/', '音乐,AI,K歌', 'DIRECT', 'LOCAL_80', 'root-09-ai-music', 'N', 100, 30, 'cross-subject'
    UNION ALL SELECT 'AI 智能朗读评价系统', '从字音、流利度、语调和情感等维度评价朗读', 'https://readfensi.teacherlin.store/', '语文,英语,朗读评价', 'DIRECT', 'LOCAL_80', 'root-10-reading', 'N', 100, 40, 'cross-subject'
    UNION ALL SELECT '英语单词节奏跟读工具', '按节奏展示并朗读单词，适合早读和课堂热身', 'https://pan.quark.cn/s/96f797e3ba09', '英语,单词,早读', 'DOWNLOAD', 'LOCAL_80', 'root-12-english-word', 'N', 100, 50, 'cross-subject'
    UNION ALL SELECT '血液循环互动演示', '动态展示体循环、肺循环和气体交换过程', 'https://teacherlin.store/shengwu/index.html', '生物,互动演示,可视化', 'DIRECT', 'LOCAL_80', 'root-13-biology', 'N', 100, 60, 'cross-subject'
    UNION ALL SELECT 'AI 英语情境任务舱', '在真实情境中开展英语表达与任务练习', 'http://47.242.6.251:3024/', '英语,AI,情境学习', 'DIRECT', 'LOCAL_80', 'root-14-ai-english', 'N', 100, 70, 'cross-subject'

    UNION ALL SELECT '象山教育云盘', '象山教师资料存储与共享平台', 'https://yun.xsedu.net.cn/#/', '象山,云盘,资料', 'LOGIN_REQUIRED', 'LOCAL_3005', '3005-xs-cloud', 'N', 100, 10, 'regional-platforms'
    UNION ALL SELECT '象山科教网', '象山县教科研信息发布平台', 'https://jky.xsedu.net.cn/', '象山,教科研,通知', 'DIRECT', 'LOCAL_3005', '3005-xs-jky', 'N', 100, 20, 'regional-platforms'
    UNION ALL SELECT '大目湾实验学校官网', '学校新闻与公共信息入口', 'http://www.dmwschool.cn/default.aspx', '校本,学校官网,大目湾', 'DIRECT', 'LOCAL_3005', '3005-dmw-school', 'N', 100, 30, 'regional-platforms'
    UNION ALL SELECT '课堂点名校本系统', '大目湾校内课堂随机点名入口', 'http://10.52.27.227:5002/', '校本,点名,课堂', 'INTRANET_ONLY', 'LOCAL_3005', '3005-dmw-rollcall', 'N', 100, 40, 'regional-platforms'
    UNION ALL SELECT '倍思教务', '校本成绩与教务分析系统', 'https://jw.basejy.com/#/', '教务,成绩分析,校本', 'LOGIN_REQUIRED', 'LOCAL_3005', '3005-beisi', 'N', 100, 50, 'regional-platforms'
) s
WHERE NOT EXISTS (
    SELECT 1 FROM biz_teacher_tool t WHERE t.source_type=s.source_type AND t.source_ref=s.source_ref
);

-- 首次主分类关系；工具可在管理页继续维护多个分类。
INSERT INTO biz_teacher_tool_category_rel (tool_id, category_id)
SELECT t.tool_id, c.category_id
FROM biz_teacher_tool t
JOIN (
    SELECT '3005-typing' source_ref, 'primary-school' category_code UNION ALL SELECT '3005-primary-lab','primary-school'
    UNION ALL SELECT '3005-mail','grade-seven' UNION ALL SELECT '3005-network','grade-seven' UNION ALL SELECT '3005-cli','grade-seven' UNION ALL SELECT '3005-iot-flow','grade-seven'
    UNION ALL SELECT '3005-tts','grade-eight' UNION ALL SELECT '3005-image-recognition','grade-eight'
    UNION ALL SELECT 'zj-I10','zj-discipline' UNION ALL SELECT 'zj-I4','zj-discipline' UNION ALL SELECT 'zj-I1','zj-discipline' UNION ALL SELECT 'zj-I5','zj-discipline' UNION ALL SELECT 'zj-I6','zj-discipline' UNION ALL SELECT 'zj-I9','zj-discipline' UNION ALL SELECT 'zj-I8','zj-discipline' UNION ALL SELECT 'zj-I7','zj-discipline' UNION ALL SELECT 'zj-I3','zj-discipline' UNION ALL SELECT 'zj-I11','zj-discipline'
    UNION ALL SELECT '3005-dangbei-ai','student-no-login' UNION ALL SELECT '3005-wenxiaobai','student-no-login' UNION ALL SELECT '3005-baidu-image','student-no-login' UNION ALL SELECT '3005-raphael','student-no-login' UNION ALL SELECT '3005-hour-of-code','student-no-login'
    UNION ALL SELECT '3005-xiangshan-ai','ai-websites' UNION ALL SELECT '3005-doubao','ai-websites' UNION ALL SELECT '3005-deepseek','ai-websites' UNION ALL SELECT '3005-kimi','ai-websites' UNION ALL SELECT '3005-qwen','ai-websites' UNION ALL SELECT '3005-metaso','ai-websites' UNION ALL SELECT '3005-metaso-study','ai-websites' UNION ALL SELECT '3005-baidu-appbuilder','ai-websites' UNION ALL SELECT '3005-baidu-console','ai-websites' UNION ALL SELECT '3005-baidu-unit','ai-websites' UNION ALL SELECT '3005-lmarena','ai-websites' UNION ALL SELECT '3005-noiz','ai-websites' UNION ALL SELECT '3005-viggle','ai-websites' UNION ALL SELECT '3005-jimeng','ai-websites' UNION ALL SELECT '3005-gamma','ai-websites' UNION ALL SELECT '3005-dify','ai-websites'
    UNION ALL SELECT '3005-runoob','programming-learning' UNION ALL SELECT '3005-w3school','programming-learning' UNION ALL SELECT '3005-imooc','programming-learning' UNION ALL SELECT '3005-mpython-doc','programming-learning'
    UNION ALL SELECT '3005-wenshushu','office-materials' UNION ALL SELECT '3005-bgrdh','office-materials' UNION ALL SELECT '3005-pdf24','office-materials' UNION ALL SELECT '3005-optimizilla','office-materials' UNION ALL SELECT '3005-tinypng','office-materials' UNION ALL SELECT '3005-ezgif','office-materials' UNION ALL SELECT '3005-aspose-cells','office-materials' UNION ALL SELECT '3005-goqr','office-materials' UNION ALL SELECT '3005-iconfont','office-materials' UNION ALL SELECT '3005-ypppt','office-materials' UNION ALL SELECT '3005-pexels','office-materials'
    UNION ALL SELECT '3005-xxkj-home','teacher-websites' UNION ALL SELECT '3005-smartedu','teacher-websites' UNION ALL SELECT '3005-21cnjy','teacher-websites' UNION ALL SELECT '3005-zj-training','teacher-websites' UNION ALL SELECT '3005-national-assessment','teacher-websites' UNION ALL SELECT '3005-cpta','teacher-websites' UNION ALL SELECT '3005-zwu','teacher-websites' UNION ALL SELECT '3005-nblib','teacher-websites' UNION ALL SELECT '3005-lighthouse','teacher-websites' UNION ALL SELECT '3005-seed','teacher-websites'
    UNION ALL SELECT 'root-01-random','homeroom-tools' UNION ALL SELECT 'root-03-pet','homeroom-tools' UNION ALL SELECT 'root-05-seat','homeroom-tools' UNION ALL SELECT 'root-06-score','homeroom-tools' UNION ALL SELECT 'root-04-wrong-question','homeroom-tools'
    UNION ALL SELECT 'root-07-ai-assessment','cross-subject' UNION ALL SELECT 'root-08-music','cross-subject' UNION ALL SELECT 'root-09-ai-music','cross-subject' UNION ALL SELECT 'root-10-reading','cross-subject' UNION ALL SELECT 'root-12-english-word','cross-subject' UNION ALL SELECT 'root-13-biology','cross-subject' UNION ALL SELECT 'root-14-ai-english','cross-subject'
    UNION ALL SELECT '3005-xs-cloud','regional-platforms' UNION ALL SELECT '3005-xs-jky','regional-platforms' UNION ALL SELECT '3005-dmw-school','regional-platforms' UNION ALL SELECT '3005-dmw-rollcall','regional-platforms' UNION ALL SELECT '3005-beisi','regional-platforms'
) m ON m.source_ref=t.source_ref
JOIN biz_teacher_tool_category c ON c.category_code=m.category_code
WHERE NOT EXISTS (SELECT 1 FROM biz_teacher_tool_category_rel r WHERE r.tool_id=t.tool_id AND r.category_id=c.category_id);

-- 补充交叉归类，验证并使用多对多能力；常用推荐仍只由工具标记生成。
INSERT INTO biz_teacher_tool_category_rel (tool_id, category_id)
SELECT t.tool_id, c.category_id
FROM biz_teacher_tool t
JOIN (
    SELECT 'zj-I1' source_ref, 'programming-learning' category_code
    UNION ALL SELECT 'zj-I3','programming-learning'
    UNION ALL SELECT 'zj-I6','student-no-login'
    UNION ALL SELECT '3005-hour-of-code','programming-learning'
    UNION ALL SELECT '3005-gamma','office-materials'
    UNION ALL SELECT '3005-xiangshan-ai','regional-platforms'
    UNION ALL SELECT '3005-dify','regional-platforms'
    UNION ALL SELECT 'root-04-wrong-question','teacher-websites'
    UNION ALL SELECT 'root-07-ai-assessment','ai-websites'
) m ON m.source_ref=t.source_ref
JOIN biz_teacher_tool_category c ON c.category_code=m.category_code
WHERE NOT EXISTS (SELECT 1 FROM biz_teacher_tool_category_rel r WHERE r.tool_id=t.tool_id AND r.category_id=c.category_id);

-- 验收查询：应为12个分类、至少70个工具、0个孤儿、0个学生菜单、0个禁止协议/URL凭据。
SELECT COUNT(*) AS category_count FROM biz_teacher_tool_category WHERE del_flag='0';
SELECT COUNT(*) AS tool_count FROM biz_teacher_tool WHERE del_flag='0';
SELECT COUNT(*) AS orphan_tool_count FROM biz_teacher_tool t LEFT JOIN biz_teacher_tool_category_rel r ON r.tool_id=t.tool_id WHERE t.del_flag='0' AND r.tool_id IS NULL;
SELECT COUNT(*) AS student_permission_count FROM sys_role_menu rm JOIN sys_role r ON r.role_id=rm.role_id WHERE r.role_key='student' AND rm.menu_id IN (@teacher_tool_menu_id, @teacher_tool_manage_menu_id);
SELECT COUNT(*) AS unsafe_url_count FROM biz_teacher_tool WHERE url REGEXP '^(javascript|data|file):' OR url REGEXP '^https?://[^/]*@';
