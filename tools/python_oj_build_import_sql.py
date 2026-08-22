#!/usr/bin/env python3
"""校验完整的 Python 系统题 V2 题包，并生成可审计、可幂等执行的 MySQL 导入脚本。"""

from __future__ import annotations

import argparse
import hashlib
import json
import subprocess
import sys
import tempfile
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_BLUEPRINT = ROOT / "contexts/python-judge0/python-system-question-blueprint-v2.json"
VALIDATOR = ROOT / "tools/python_oj_validate.py"
SYSTEM_CREATOR = "python-system-v2"
PROCEDURE_NAME = "import_python_system_v2"


if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="生成 Python 系统题 V2 的幂等 MySQL 导入脚本")
    parser.add_argument("packages", nargs="+", type=Path, help="六批 UTF-8 JSON 题包")
    parser.add_argument("--blueprint", type=Path, default=DEFAULT_BLUEPRINT, help="120 题唯一蓝图")
    parser.add_argument("--output", type=Path, required=True, help="输出 SQL 文件")
    parser.add_argument("--report", type=Path, help="保留校验器生成的 JSON 报告")
    return parser.parse_args()


def utf8_sql(value: str | None) -> str:
    """用十六进制承载文本，避免 Windows 管道或客户端代码页把中文写成问号。"""
    if value is None:
        return "NULL"
    raw = value.encode("utf-8")
    if not raw:
        return "''"
    return f"CONVERT(0x{raw.hex().upper()} USING utf8mb4)"


def number_sql(value: int | float) -> str:
    if isinstance(value, bool):
        raise ValueError("布尔值不能作为数值写入 SQL")
    if isinstance(value, int):
        return str(value)
    return format(value, ".10g")


def load_questions(paths: list[Path]) -> list[dict[str, Any]]:
    questions: list[dict[str, Any]] = []
    for path in paths:
        data = json.loads(path.read_text(encoding="utf-8-sig"))
        if not isinstance(data, list):
            raise ValueError(f"{path} 根节点不是数组")
        questions.extend(data)
    return sorted(questions, key=lambda item: item["externalId"])


def validate_packages(paths: list[Path], blueprint: Path, report: Path | None) -> dict[str, Any]:
    report_path = report
    temporary: tempfile.TemporaryDirectory[str] | None = None
    if report_path is None:
        temporary = tempfile.TemporaryDirectory(prefix="python-oj-import-report-")
        report_path = Path(temporary.name) / "report.json"
    assert report_path is not None
    report_path.parent.mkdir(parents=True, exist_ok=True)
    command = [
        sys.executable,
        str(VALIDATOR),
        *[str(path) for path in paths],
        "--blueprint",
        str(blueprint),
        "--require-complete-blueprint",
        "--report",
        str(report_path),
    ]
    completed = subprocess.run(command, cwd=ROOT, text=True, encoding="utf-8", capture_output=True)
    if completed.stdout:
        print(completed.stdout, end="")
    if completed.returncode != 0:
        if completed.stderr:
            print(completed.stderr, file=sys.stderr, end="")
        if temporary is not None:
            temporary.cleanup()
        raise RuntimeError("题包未通过完整蓝图和参考代码校验，拒绝生成导入 SQL")
    result = json.loads(report_path.read_text(encoding="utf-8"))
    if temporary is not None:
        temporary.cleanup()
    if result.get("valid") is not True:
        raise RuntimeError("校验报告不是 valid=true，拒绝生成导入 SQL")
    return result


def question_statements(question: dict[str, Any]) -> list[str]:
    external_id = question["externalId"]
    knowledge_points = "，".join(question["knowledgePoints"])
    notes = f"系统题号：{external_id}；题库版本：V2。"
    analysis = f"Python 系统题库 V2；稳定题号：{external_id}。"
    lines = [
        f"    -- {external_id} {question['title']}",
        "    SET @python_v2_question_id = (",
        "        SELECT question_id FROM biz_programming_question_config",
        f"        WHERE external_id = {utf8_sql(external_id)} LIMIT 1",
        "    );",
        "    INSERT INTO biz_question",
        "        (question_type, question_content, grade, difficulty, semester, lesson_num, answer, analysis,",
        "         practical_mode, is_public, creator_id, create_by, create_time, update_by, update_time)",
        "    SELECT",
        f"        'practical', {utf8_sql(question['description'])}, NULL, {utf8_sql(question['difficulty'])}, NULL, NULL,",
        f"        {utf8_sql(question['referenceCode'])}, {utf8_sql(analysis)}, 'PYTHON', 'Y', v_creator_id,",
        f"        v_creator_username, NOW(), v_creator_username, NOW()",
        "    WHERE @python_v2_question_id IS NULL;",
        "    SET @python_v2_question_id = COALESCE(@python_v2_question_id, LAST_INSERT_ID());",
        "    UPDATE biz_question",
        "    SET question_type='practical', question_content=" + utf8_sql(question["description"]) + ",",
        "        grade=NULL, difficulty=" + utf8_sql(question["difficulty"]) + ", semester=NULL, lesson_num=NULL,",
        "        answer=" + utf8_sql(question["referenceCode"]) + ", analysis=" + utf8_sql(analysis) + ",",
        "        practical_mode='PYTHON', is_public='Y', creator_id=v_creator_id, create_by=v_creator_username,",
        "        update_by=v_creator_username, update_time=NOW()",
        "    WHERE question_id=@python_v2_question_id;",
        "    INSERT INTO biz_programming_question_config",
        "        (question_id, language_code, external_id, title, knowledge_points, no_input, validation_status,",
        "         validated_at, validated_by, content_version, starter_code, input_description, output_description,",
        "         sample_explanation, constraints_text, notes_text, time_limit_seconds, memory_limit_kb, max_processes,",
        "         max_file_size_kb, max_output_kb, enabled, create_by, create_time, update_by, update_time)",
        "    VALUES",
        f"        (@python_v2_question_id, 'python', {utf8_sql(external_id)}, {utf8_sql(question['title'])},",
        f"         {utf8_sql(knowledge_points)}, {'1' if question['noInput'] else '0'}, 'VALID', NOW(), '{SYSTEM_CREATOR}', 2,",
        f"         {utf8_sql(question['starterCode'])}, {utf8_sql(question['inputDescription'])},",
        f"         {utf8_sql(question['outputDescription'])}, {utf8_sql(question['sampleExplanation'])},",
        f"         {utf8_sql(question['constraints'])}, {utf8_sql(notes)}, {number_sql(question['timeLimitSeconds'])},",
        f"         {number_sql(question['memoryLimitKb'])}, 8, 1024, 64, '1', '{SYSTEM_CREATOR}', NOW(), '{SYSTEM_CREATOR}', NOW())",
        "    ON DUPLICATE KEY UPDATE",
        "        title=VALUES(title), knowledge_points=VALUES(knowledge_points), no_input=VALUES(no_input),",
        "        validation_status='VALID', validated_at=NOW(), validated_by=VALUES(validated_by),",
        "        content_version=2, starter_code=VALUES(starter_code), input_description=VALUES(input_description),",
        "        output_description=VALUES(output_description), sample_explanation=VALUES(sample_explanation),",
        "        constraints_text=VALUES(constraints_text), notes_text=VALUES(notes_text),",
        "        time_limit_seconds=VALUES(time_limit_seconds), memory_limit_kb=VALUES(memory_limit_kb),",
        "        max_processes=8, max_file_size_kb=1024, max_output_kb=64, enabled='1',",
        f"        update_by='{SYSTEM_CREATOR}', update_time=NOW();",
        "    DELETE FROM biz_programming_test_case WHERE question_id=@python_v2_question_id;",
    ]
    for case in sorted(question["testCases"], key=lambda item: item["orderNum"]):
        lines.extend(
            [
                "    INSERT INTO biz_programming_test_case",
                "        (question_id, case_name, input_text, expected_output, is_public, score_weight, order_num,",
                "         create_by, create_time, update_by, update_time)",
                "    VALUES",
                f"        (@python_v2_question_id, {utf8_sql(case['caseName'])}, {utf8_sql(case['inputText'])},",
                f"         {utf8_sql(case['expectedOutput'])}, {'1' if case['isPublic'] else '0'},",
                f"         {number_sql(case['scoreWeight'])}, {number_sql(case['orderNum'])},",
                f"         '{SYSTEM_CREATOR}', NOW(), '{SYSTEM_CREATOR}', NOW());",
            ]
        )
    lines.append("")
    return lines


def build_sql(questions: list[dict[str, Any]], package_paths: list[Path]) -> str:
    ids = [question["externalId"] for question in questions]
    id_list = ",".join(utf8_sql(external_id) for external_id in ids)
    expected_questions = len(questions)
    expected_cases = sum(len(question["testCases"]) for question in questions)
    hashes = [f"-- {path.name}: {hashlib.sha256(path.read_bytes()).hexdigest().upper()}" for path in package_paths]
    lines = [
        "-- Python 系统题库 V2 自动生成导入脚本。",
        "-- 必须先执行 sql/python_oj_modernization_v1.sql，并在目标库完成备份。",
        "-- 文本全部使用 UTF-8 十六进制写入，避免 Windows 客户端代码页污染中文。",
        f"-- 题目数：{expected_questions}；测试点数：{expected_cases}。",
        *hashes,
        "SET NAMES utf8mb4;",
        f"DROP PROCEDURE IF EXISTS {PROCEDURE_NAME};",
        "DELIMITER $$",
        f"CREATE PROCEDURE {PROCEDURE_NAME}()",
        "BEGIN",
        "    DECLARE v_count INT DEFAULT 0;",
        "    DECLARE v_creator_id BIGINT DEFAULT NULL;",
        "    DECLARE v_creator_username VARCHAR(64) DEFAULT NULL;",
        "    DECLARE EXIT HANDLER FOR SQLEXCEPTION",
        "    BEGIN",
        "        ROLLBACK;",
        "        RESIGNAL;",
        "    END;",
        "",
        "    SELECT COUNT(*) INTO v_count FROM sys_user",
        f"    WHERE nick_name={utf8_sql('郑东旭')} COLLATE utf8mb4_general_ci",
        "      AND status='0' AND del_flag='0';",
        "    IF v_count <> 1 THEN",
        "        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='郑东旭有效账号不是唯一一条，已拒绝导入';",
        "    END IF;",
        "    SELECT user_id,user_name INTO v_creator_id,v_creator_username FROM sys_user",
        f"    WHERE nick_name={utf8_sql('郑东旭')} COLLATE utf8mb4_general_ci",
        "      AND status='0' AND del_flag='0' LIMIT 1;",
        "",
        "    SELECT COUNT(*) INTO v_count",
        "    FROM biz_programming_question_config c",
        f"    WHERE c.external_id IN ({id_list})",
        f"      AND COALESCE(c.create_by,'') <> '{SYSTEM_CREATOR}';",
        "    IF v_count <> 0 THEN",
        "        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='V2 外部题号已被非系统题占用，已拒绝导入';",
        "    END IF;",
        "",
        "    START TRANSACTION;",
        "",
    ]
    for question in questions:
        lines.extend(question_statements(question))
    lines.extend(
        [
            "    SELECT COUNT(*) INTO v_count",
            "    FROM biz_programming_question_config c",
            "    INNER JOIN biz_question q ON q.question_id=c.question_id",
            f"    WHERE c.external_id IN ({id_list})",
            f"      AND c.create_by='{SYSTEM_CREATOR}' AND q.creator_id=v_creator_id",
            "      AND CAST(q.create_by AS BINARY)=CAST(v_creator_username AS BINARY)",
            "      AND q.question_type='practical' AND q.practical_mode='PYTHON'",
            "      AND q.grade IS NULL AND q.semester IS NULL AND q.lesson_num IS NULL",
            "      AND q.is_public='Y' AND c.validation_status='VALID' AND c.enabled='1';",
            f"    IF v_count <> {expected_questions} THEN",
            "        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='V2 题目或配置后检数量不一致，事务已回滚';",
            "    END IF;",
            "",
            "    SELECT COUNT(*) INTO v_count",
            "    FROM biz_programming_test_case tc",
            "    INNER JOIN biz_programming_question_config c ON c.question_id=tc.question_id",
            f"    WHERE c.external_id IN ({id_list});",
            f"    IF v_count <> {expected_cases} THEN",
            "        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='V2 测试点后检数量不一致，事务已回滚';",
            "    END IF;",
            "",
            "    SELECT COUNT(*) INTO v_count FROM (",
            "        SELECT c.question_id",
            "        FROM biz_programming_question_config c",
            "        INNER JOIN biz_programming_test_case tc ON tc.question_id=c.question_id",
            f"        WHERE c.external_id IN ({id_list})",
            "        GROUP BY c.question_id",
            "        HAVING SUM(CASE WHEN tc.is_public='1' THEN 1 ELSE 0 END)=2",
            "           AND SUM(CASE WHEN tc.is_public='0' THEN 1 ELSE 0 END)>=4",
            "           AND ROUND(SUM(tc.score_weight), 6)=100",
            "    ) valid_questions;",
            f"    IF v_count <> {expected_questions} THEN",
            "        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='V2 公开/隐藏测试点或权重后检失败，事务已回滚';",
            "    END IF;",
            "",
            "    COMMIT;",
            "END$$",
            "DELIMITER ;",
            f"CALL {PROCEDURE_NAME}();",
            f"DROP PROCEDURE IF EXISTS {PROCEDURE_NAME};",
            "",
            "SELECT COUNT(*) AS v2_question_count",
            "FROM biz_programming_question_config c",
            "INNER JOIN biz_question q ON q.question_id=c.question_id",
            f"WHERE c.external_id IN ({id_list}) AND c.create_by='{SYSTEM_CREATOR}';",
            "SELECT COUNT(*) AS v2_test_case_count",
            "FROM biz_programming_test_case tc",
            "INNER JOIN biz_programming_question_config c ON c.question_id=tc.question_id",
            f"WHERE c.external_id IN ({id_list});",
            "",
        ]
    )
    return "\n".join(lines)


def main() -> int:
    args = parse_args()
    try:
        report = validate_packages(args.packages, args.blueprint, args.report)
        questions = load_questions(args.packages)
        if report.get("questionCount") != len(questions):
            raise RuntimeError("校验报告题数与实际题包不一致")
        sql = build_sql(questions, args.packages)
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(sql, encoding="utf-8", newline="\n")
    except (OSError, UnicodeError, json.JSONDecodeError, KeyError, ValueError, RuntimeError) as exc:
        print(f"生成失败：{exc}", file=sys.stderr)
        return 1
    digest = hashlib.sha256(args.output.read_bytes()).hexdigest().upper()
    print(f"已生成：{args.output}")
    print(f"题目 {len(questions)} 道，测试点 {sum(len(q['testCases']) for q in questions)} 个，SQL SHA-256 {digest}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
