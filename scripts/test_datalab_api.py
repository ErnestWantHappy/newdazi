import os
import requests
import json

BASE_URL = "http://localhost:8080"

# 1. 学生登录
login_res = requests.post(f"{BASE_URL}/login", json={
    "username": "2020710101",
    "password": os.environ["LOCAL_STUDENT_PASSWORD"]
})
login_data = login_res.json()
assert login_data.get("code") == 200, f"Login failed: {login_data}"
token = login_data["token"]
headers = {"Authorization": f"Bearer {token}"}
print("1. 学生登录成功, Token 获取正常")

# 2. 验证题单概览
overview_res = requests.get(f"{BASE_URL}/business/python-practice/student/overview", headers=headers)
overview_data = overview_res.json()
assert overview_data.get("code") == 200, f"Overview failed: {overview_data}"
questions = overview_data.get("data", {}).get("questions", [])
print(f"2. 题单题目总数: {len(questions)}")
target_q = next((q for q in questions if q.get("question_id") == 999901), None)
if target_q:
    print(f"   目标实验题找到: {target_q.get('question_title')}, practical_mode={target_q.get('practical_mode')}")
    assert target_q.get("practical_mode") == "DATA_LAB", "practical_mode should be DATA_LAB"
else:
    print("   未在题单概览中找到 999901")

# 3. 验证获取实验配置详情
config_res = requests.get(f"{BASE_URL}/business/dataLab/student/config/999901", headers=headers)
config_data = config_res.json()
assert config_data.get("code") == 200, f"Config failed: {config_data}"
cfg = config_data.get("data", {})
print(f"3. 实验题配置名称: {cfg.get('experimentName')}, 数据文件: {cfg.get('excelFileName')}")
assert cfg.get("excelFileName") == "高中运动会成绩统计.xlsx"

# 4. 验证 Excel 数据文件下载
excel_res = requests.get(f"{BASE_URL}/business/dataLab/student/excel/999901", headers=headers)
assert excel_res.status_code == 200, f"Excel download failed: {excel_res.status_code}"
assert len(excel_res.content) > 1000, f"Excel file too small: {len(excel_res.content)} bytes"
print(f"4. Excel 文件下载成功, 大小: {len(excel_res.content)} 字节 (正规 XLSX 文件)")

# 5. 验证提交实验成果
tiny_png_base64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
submit_res = requests.post(f"{BASE_URL}/business/dataLab/student/submit", headers=headers, json={
    "questionId": 999901,
    "sourceType": "BASE_VERSION",
    "sourceId": 1,
    "sourceCode": "import pandas as pd\nimport matplotlib.pyplot as plt\ndf = pd.read_excel('高中运动会成绩统计.xlsx')\nprint('数据行数:', len(df))\nplt.bar(['1班', '2班'], [10, 20])\nplt.show()",
    "chartImageBase64": tiny_png_base64,
    "outputText": "数据行数: 500\n绘图完成"
})
submit_data = submit_res.json()
assert submit_data.get("code") == 200, f"Submit failed: {submit_data}"
sub = submit_data.get("data", {})
print(f"5. 实验成果提交成功: submissionId={sub.get('submissionId')}, chartImageUrl={sub.get('chartImageUrl')}")
assert sub.get("chartImageUrl") is not None, "Chart image URL should be saved"

# 6. 验证查询个人历史提交
history_res = requests.get(f"{BASE_URL}/business/dataLab/student/submission", headers=headers, params={
    "questionId": 999901,
    "sourceType": "BASE_VERSION",
    "sourceId": 1
})
history_data = history_res.json()
assert history_data.get("code") == 200, f"History query failed: {history_data}"
h = history_data.get("data", {})
print(f"6. 历史提交查询成功: submissionId={h.get('submissionId')}, studentId={h.get('studentId')}, chartUrl={h.get('chartImageUrl')}")

print("\n[SUCCESS] Phase 1 backend and student APIs verified successfully!")
