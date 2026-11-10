"""扫描正式库 RESOURCE 帖子，输出候选池与跳过原因；不写库。"""
from __future__ import annotations

import argparse
import json
from pathlib import Path

from common import mysql_readonly, parse_json_lines, read_json, safe_report, utc_now, write_json


SQL = r"""
SELECT JSON_OBJECT(
  'postId', p.post_id,
  'topicId', p.topic_id,
  'topicTitle', t.title,
  'courseTitle', p.course_title,
  'grade', p.grade,
  'semester', p.semester,
  'lessonKind', p.lesson_kind,
  'lessonNo', p.lesson_no,
  'author', u.nick_name,
  'school', d.dept_name,
  'postUpdatedAt', DATE_FORMAT(COALESCE(p.update_time,p.create_time), '%Y-%m-%d %H:%i:%s'),
  'topicUpdatedAt', DATE_FORMAT(COALESCE(t.update_time,t.create_time), '%Y-%m-%d %H:%i:%s'),
  'privateFiles', (SELECT COUNT(*) FROM biz_research_resource r
                   WHERE r.post_id=p.post_id AND r.del_flag='0' AND r.resource_type='F'),
  'cloudLinks', (SELECT COUNT(*) FROM biz_research_resource r
                 WHERE r.post_id=p.post_id AND r.del_flag='0' AND r.resource_type='L')
)
FROM biz_research_post p
JOIN biz_research_topic t ON t.topic_id=p.topic_id AND t.del_flag='0'
LEFT JOIN sys_user u ON u.user_id=p.author_id
LEFT JOIN sys_dept d ON d.dept_id=p.dept_id
WHERE p.del_flag='0' AND p.post_type='RESOURCE'
ORDER BY p.post_id;
"""

NON_COURSE_KEYWORDS = ("名单", "课时安排", "平台介绍", "功能介绍", "工具介绍", "赋能", "workbuddy")
COURSE_TITLES = {"语音合成", "返回成语现场"}
DEFERRED_POST_IDS: set[int] = set()


def classify(item: dict) -> tuple[str, str]:
    post_id = int(item["postId"])
    title = str(item.get("courseTitle") or "")
    if post_id in DEFERRED_POST_IDS:
        return "DEFERRED", "用户明确暂缓，等待作者补交文件"
    if not title or any(keyword.lower() in title.lower() for keyword in NON_COURSE_KEYWORDS):
        return "SKIP", "不是教材课例：会议材料、平台或工具介绍"
    if title.strip("《》") not in COURSE_TITLES:
        return "PENDING", "课程标题不在本次已核实教材课例白名单"
    if not all(item.get(key) for key in ("topicTitle", "author", "school", "grade", "semester", "lessonNo")):
        return "PENDING", "活动、教师、学校或年级/学期/课次信息不完整"
    if not item.get("privateFiles") and not item.get("cloudLinks"):
        return "PENDING", "没有可用附件或云盘链接"
    return "CANDIDATE", "已核实教材课例且有可用来源"


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--run-id", required=True)
    parser.add_argument("--after-post-id", type=int, default=0)
    parser.add_argument("--post-ids", default="")
    args = parser.parse_args()

    rows = parse_json_lines(mysql_readonly(SQL))
    selected = set()
    if args.post_ids.strip():
        selected = {int(value) for value in args.post_ids.split(",") if value.strip()}
    result = []
    for item in rows:
        state, reason = classify(item)
        item["state"] = state
        item["reason"] = reason
        if selected and int(item["postId"]) not in selected:
            item["state"] = "SKIP"
            item["reason"] = "本次未指定该帖子"
        elif int(item["postId"]) <= args.after_post_id:
            item["state"] = "SKIP"
            item["reason"] = "不晚于上次成功游标"
        result.append(item)
    from common import OUTPUT_ROOT
    output = OUTPUT_ROOT / ("scan-%s.json" % args.run_id)
    write_json(output, {"generatedAt": utc_now(), "source": "130-readonly", "posts": safe_report(result)})
    print(json.dumps({"runId": args.run_id, "report": str(output),
                      "candidates": [item["postId"] for item in result if item["state"] == "CANDIDATE"],
                      "skipped": [{"postId": item["postId"], "reason": item["reason"]}
                                  for item in result if item["state"] != "CANDIDATE"]},
                     ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
