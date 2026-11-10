# 字段与页面映射

| 页面信息 | 数据库事实 | 说明 |
| --- | --- | --- |
| 课程标题 | `biz_research_post.course_title` | 顾意丽帖子带书名号，导入课例标题去掉外层书名号，活动标题保持原文 |
| 上课教师 | `author_id → sys_user.nick_name` | 不从正文猜姓名 |
| 学校 | `dept_id → sys_dept.dept_name` | 使用帖子发布时的学校快照 |
| 来源帖子 | `biz_research_post.post_id` | 唯一幂等键；同名课例按此区分 |
| 来源活动 | `biz_research_topic.title` | 完整快照写入 `research_topic_title`，不缩写、不塞进 `origin` |
| 教材册 | `biz_textbook_book.book_code` | 四上 `xxkj-4-0-2026`，八下 `xxkj-8-1-2026` |
| 教材课次 | `biz_textbook_section.section_key` | 四上第17课 `lesson-17`，八下第2课 `lesson-2`，且 `kind='LESSON'` |

`biz_textbook_case.source_key = research-post-<post_id>`，`research_post_id` 建唯一约束且旧数据为 NULL。`origin` 是固定枚举：旧值保留，`LEGACY` 仍显示「优秀课例」，`NINGBO2023` 显示「宁波版 2023 参考」，`NINGBO2026` 显示「宁波版 2026」，新来源 `RESEARCH_ACTIVITY` 显示「教研活动课例」。

资源使用 `resource_key = sha256("research-post:<post_id>:<文件SHA-256>")`；同内容文件按哈希去重。资源路径按 `<sha256前两位>/<sha256><扩展名>` 落在课例私有资源根下，PDF 预览放 `previews/`，不进入公开 `/profile`。
