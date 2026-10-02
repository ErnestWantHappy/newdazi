import os
import requests
import json

BASE_URL = "http://localhost:8080"

# 1. 教师登录 (账号见 AGENTS.md 4.1)
login_res = requests.post(f"{BASE_URL}/login", json={
    "username": "19157727791",
    "password": os.environ["LOCAL_TEACHER_PASSWORD"]
})
login_data = login_res.json()
assert login_data.get("code") == 200, f"Teacher login failed: {login_data}"
token = login_data["token"]
headers = {"Authorization": f"Bearer {token}"}
print("1. 教师登录成功, Token 获取正常")

# 2. 查询实验配置
config_res = requests.get(f"{BASE_URL}/business/dataLab/config/999901", headers=headers)
config_data = config_res.json()
assert config_data.get("code") == 200, f"Get config failed: {config_data}"
cfg = config_data.get("data", {})
print(f"2. 教师获取实验题配置: {cfg.get('experimentName')}, 满分: {cfg.get('maxScore')}")

# 3. 教师查询学生提交列表
subs_res = requests.get(f"{BASE_URL}/business/dataLab/submissions", headers=headers, params={
    "questionId": 999901,
    "sourceType": "BASE_VERSION",
    "sourceId": 1
})
subs_data = subs_res.json()
assert subs_data.get("code") == 200, f"Submissions query failed: {subs_data}"
submissions = subs_data.get("data", [])
print(f"3. 教师查询该实验题提交数: {len(submissions)}")
assert len(submissions) > 0, "Should have at least 1 student submission"
sub = submissions[0]
sub_id = sub["submissionId"]
print(f"   待批改学生: {sub.get('studentName')} ({sub.get('studentNumber')}), 提交时间: {sub.get('submittedAt')}")

# 4. 教师批改打分
score_res = requests.put(f"{BASE_URL}/business/dataLab/submission/{sub_id}/score", headers=headers, json={
    "score": 95,
    "scoreComment": "柱状图排版美观，数据筛选准确，干得漂亮！"
})
score_data = score_res.json()
assert score_data.get("code") == 200, f"Score failed: {score_data}"
print(f"4. 教师打分成功: 95分")

# 5. 复查提交详情验证成绩回显
detail_res = requests.get(f"{BASE_URL}/business/dataLab/submission/{sub_id}", headers=headers)
detail_data = detail_res.json()
assert detail_data.get("code") == 200, f"Detail failed: {detail_data}"
d = detail_data.get("data", {})
print(f"5. 成绩复核确认: 得分={d.get('score')}分, 评语={d.get('scoreComment')}, 评分人={d.get('scoredBy')}")
assert d.get("score") == 95, "Score should be 95"

print("\n[SUCCESS] Teacher DataLab management and grading APIs verified successfully!")
