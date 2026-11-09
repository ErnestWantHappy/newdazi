"""130 课堂/抽测空闲与其登记版本的最小只读核对；字段名先探测再统计，避免整段脚本被一条 SQL 打断。"""
import json
import shlex
import sys
from datetime import datetime

from migrate_local import ROOT

sys.path.insert(0, str(ROOT / "scripts/deploy"))
import remote130

CONFIG_DIR = "/data/apps/xueyeceping/releases/20261008_seat_install_v1/config"


def main():
    jump, client, password = remote130.connect()
    cfg = shlex.quote(f"{CONFIG_DIR}/application-druid.yml")
    out = {}

    def sql(name, query, sudo=True):
        command = f"""cfg={cfg}
user=$(awk '/username:/{{print $2; exit}}' "$cfg" | tr -d '"')
pass=$(awk '/password:/{{print $2; exit}}' "$cfg" | tr -d '"')
export MYSQL_PWD="$pass"
printf %s {shlex.quote(query)} | mysql -h 127.0.0.1 -u "$user" --default-character-set=utf8mb4 -N -B ry-vue
"""
        code, text, error = remote130.sudo(client, password, command) if sudo else remote130.run(client, command)
        out[name] = {"exit": code, "stdout": text.strip(), "stderr": error.strip()[:600]}
        print(f"### {name} (exit {code})\n{text.strip()}\n{error.strip()[:300]}\n")
        return text

    try:
        sql("columns", """
SELECT TABLE_NAME, COLUMN_NAME FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN ('biz_lesson','biz_student_answer','biz_county_exam','biz_lesson_assignment')
   AND COLUMN_NAME IN ('status','create_time','submit_time','end_time','start_time','lesson_id','exam_id','answer_id')
 ORDER BY TABLE_NAME, ORDINAL_POSITION;
""")
        sql("tables", """
SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE()
 AND TABLE_NAME LIKE 'biz_county_exam%' GROUP BY TABLE_NAME;
""")
        sql("idle", """
SELECT 'lesson_status_1_2_3', COUNT(*) FROM biz_lesson WHERE status IN ('1','2','3');
SELECT 'lesson_status_0', COUNT(*) FROM biz_lesson WHERE status = '0';
SELECT 'lesson_today', COUNT(*) FROM biz_lesson WHERE DATE(create_time)=CURDATE();
SELECT 'answer_last_30min', COUNT(*) FROM biz_student_answer WHERE submit_time >= DATE_SUB(NOW(), INTERVAL 30 MINUTE);
SELECT 'answer_today', COUNT(*) FROM biz_student_answer WHERE DATE(submit_time)=CURDATE();
SELECT 'answer_any_last', IFNULL(MAX(submit_time),'NONE') FROM biz_student_answer;
""")
        sql("schema_lesson", "SHOW COLUMNS FROM biz_lesson;")
        sql("schema_answer", "SHOW COLUMNS FROM biz_student_answer;")
        sql("lesson_recent", """
SELECT * FROM biz_lesson ORDER BY lesson_id DESC LIMIT 5;
""")
        sql("lesson_active", """
SELECT lesson_id, status, create_time, update_time FROM biz_lesson WHERE status IN ('1','2','3') ORDER BY lesson_id DESC;
""")
        sql("answer_recent", """
SELECT answer_id, lesson_id, submit_time FROM biz_student_answer ORDER BY answer_id DESC LIMIT 5;
""")
        sql("answer_last_hour", """
SELECT COUNT(*) FROM biz_student_answer WHERE submit_time >= DATE_SUB(NOW(), INTERVAL 60 MINUTE);
""")
        sql("county_exam_active", """
SELECT exam_id, status, create_time FROM biz_county_exam WHERE status NOT IN ('0','4') ORDER BY exam_id DESC LIMIT 5;
""")
        sql("update158", """
SELECT update_id, version_no, status, title, create_time FROM biz_platform_update ORDER BY update_id DESC LIMIT 4;
""")

        code, text, error = remote130.run(client, f"ls -ld /data/upload; stat -c '%U:%G %a %n' /data/upload")
        out["profile_perm"] = {"exit": code, "stdout": text.strip(), "stderr": error.strip()[:300]}
        print("### profile_perm\n" + text.strip())

        out["checkedAt"] = datetime.now().isoformat()
        target = ROOT / "output/choice-media/production-idle-20261108.json"
        target.write_text(json.dumps(out, ensure_ascii=False, indent=2), encoding="utf-8")
        print("已保存：" + str(target.relative_to(ROOT)))
    finally:
        client.close()
        jump.close()


if __name__ == "__main__":
    main()
