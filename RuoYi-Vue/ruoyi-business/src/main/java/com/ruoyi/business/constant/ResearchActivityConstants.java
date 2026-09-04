package com.ruoyi.business.constant;

/**
 * 教研活动业务常量。
 * 枚举集中定义，避免权限和组合校验散落魔法字符串。
 */
public final class ResearchActivityConstants
{
    private ResearchActivityConstants() { }

    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_TEACHER = "teacher";
    public static final String ROLE_RESEARCHER = "researcher";

    public static final String TOPIC_NOTICE = "NOTICE";
    public static final String TOPIC_SHARE = "SHARE";
    public static final String POST_COMMENT = "COMMENT";
    public static final String POST_MOMENT = "MOMENT";
    public static final String POST_RESOURCE = "RESOURCE";

    public static final String NOTICE_NONE = "0";
    public static final String NOTICE_NORMAL = "1";
    public static final String SCOPE_NONE = "0";
    public static final String SCOPE_STAGE = "1";
    public static final String SCOPE_USER = "2";
    public static final String SOURCE_STAGE = "S";
    public static final String SOURCE_USER = "U";

    public static final String RESOURCE_FILE = "F";
    public static final String RESOURCE_LINK = "L";
    public static final String FILE_KEEP = "KEEP";
    public static final String FILE_REMOVE = "REMOVE";
    public static final String FILE_REPLACE = "REPLACE";

    public static final String LESSON_NUMBER = "N";
    public static final String LESSON_SPECIAL = "S";
    public static final String LESSON_REVIEW = "R";
    public static final String YES = "Y";
    public static final String NO = "N";
    public static final String DEL_NORMAL = "0";
    public static final String DEL_DELETED = "2";

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 50;
    public static final int MAX_LINKS = 3;
    public static final int MAX_IMAGES = 20;
    public static final int PUBLIC_SHARE_DEFAULT_DAYS = 30;
    public static final int PUBLIC_SHARE_SHORT_DAYS = 7;
    public static final int PUBLIC_SHARE_MAX_DAYS = 30;
    public static final long MAX_PACKAGE_BYTES = 50L * 1024L * 1024L;
    public static final long MAX_IMAGE_BYTES = 10L * 1024L * 1024L;
    public static final int MAX_HTML_LENGTH = 500_000;
    public static final int MAX_TEXT_LENGTH = 100_000;
}
