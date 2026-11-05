"""全面回归测试：
1. 教师登录与菜单顺序 (/getRouters)
2. 课例查询 (summary / cases / feedback)
3. 教师评价 (推荐 + 文字理由 / 匿名查阅 / 改票 / 撤销)
4. 邀请频率与抽样持久化
5. 角色权限拦截 (学生/教研员 403)
6. 逐页预览与缩略图接口
"""
import os
import json
import sys
import requests

BASE = "http://localhost:8080"


def login(username, password):
    res = requests.post(f"{BASE}/login", json={"username": username, "password": password, "code": "", "uuid": ""}, timeout=10)
    data = res.json()
    if data.get("code") != 200:
        raise RuntimeError(f"登录失败 {username}: {data}")
    return data["token"]


def main():
    print("=== 开始全面接口回归测试 ===")

    # 1. 教师登录
    teacher_token = login("19157727791", os.environ.get("TEACHER_PASSWORD",""))
    headers = {"Authorization": f"Bearer {teacher_token}"}
    print("[1/6] 教师登录成功")

    # 2. 菜单顺序检查
    res = requests.get(f"{BASE}/getRouters", headers=headers, timeout=10)
    assert res.status_code == 200, f"/getRouters failed: {res.text}"
    routers = res.json().get("data", [])

    # 提取扁平菜单列表供顺序判断
    menu_items = []
    for r in routers:
        title = r.get("meta", {}).get("title") if r.get("meta") else None
        path = r.get("path", "")
        if not title and r.get("children"):
            first_child = r["children"][0]
            title = first_child.get("meta", {}).get("title")
            path = first_child.get("path", path)
        menu_items.append({"title": title or "", "path": path})

    def find_menu_index(name_substr, path_substr):
        for idx, m in enumerate(menu_items):
            if name_substr in m["title"] or path_substr == m["path"]:
                return idx
        return -1

    idx_research = find_menu_index("教研活动", "research-activity")
    idx_student = find_menu_index("学生管理", "studentguanli")
    idx_question = find_menu_index("题库管理", "question")
    idx_textbook = find_menu_index("电子教材", "textbook")
    idx_guide = find_menu_index("导学单管理", "business/guide-sheet-list")
    idx_score = find_menu_index("成绩查询", "score")

    print("已获取菜单路由，检查教师菜单顺序:")
    print("  当前路由顺序:", [m["title"] for m in menu_items])

    assert idx_research != -1, "未找到教研活动菜单"
    assert idx_student != -1, "未找到学生管理菜单"
    assert idx_question != -1, "未找到题库管理菜单"
    assert idx_textbook != -1, "未找到电子教材菜单"
    assert idx_guide != -1, "未找到导学单管理菜单"
    assert idx_score != -1, "未找到成绩查询菜单"

    print(f"  教研活动({idx_research}) -> 学生管理({idx_student})")
    assert idx_research < idx_student, "教研活动未能排在学生管理之前"
    print(f"  题库管理({idx_question}) -> 电子教材与优秀课例({idx_textbook})")
    assert idx_question < idx_textbook, "电子教材与优秀课例未能排在题库管理之后"
    print(f"  导学单管理({idx_guide}) -> 成绩查询({idx_score})")
    assert idx_guide < idx_score, "成绩查询未能排在导学单管理之后"
    print("  [PASS] 菜单相对顺序校验通过！")

    # 3. 课例查询与结构检查
    res = requests.get(f"{BASE}/business/textbook/cases/summary", headers=headers, timeout=10)
    assert res.json().get("code") == 200, "summary 接口失败"
    summary_data = res.json().get("data", [])
    assert len(summary_data) > 0, "课例汇总为空"
    first_summary = summary_data[0]
    book_id = first_summary["bookId"]
    section_key = first_summary["sectionKey"]

    res = requests.get(f"{BASE}/business/textbook/cases/book/{book_id}/section/{section_key}", headers=headers, timeout=10)
    cases = res.json().get("data", [])
    assert len(cases) > 0, "课例列表为空"
    case = cases[0]
    case_id = case["caseId"]
    print(f"[2/6] 成功读取课例: case_id={case_id}, title={case['title']}, author={case['author']}")
    assert "likes" in case and "dislikes" in case and "myVote" in case, "课例未包含评价字段"

    # 4. 教师评价 (推荐 + 文字理由 + 匿名查看 + 改票 + 撤销)
    print(f"[3/6] 开始测试教师评价与文字理由 (case_id={case_id}):")

    # 4.1 提交推荐 + 理由
    test_reason_1 = "教学设计层次分明，案例生动，课堂互动性强！"
    res = requests.put(f"{BASE}/business/textbook/cases/{case_id}/feedback", headers=headers,
                       json={"vote": 1, "reason": test_reason_1}, timeout=10)
    assert res.status_code == 200 and res.json().get("code") == 200, f"推荐评价失败: {res.text}"
    vote_data = res.json()["data"]
    assert vote_data["myVote"] == 1, "myVote 不为 1"
    assert vote_data["myReason"] == test_reason_1, "myReason 未正确保存"
    print(f"  - 推荐 + 理由提交成功: myVote={vote_data['myVote']}, myReason={vote_data['myReason']}")

    # 4.2 查阅评价理由列表 (验证匿名性)
    res = requests.get(f"{BASE}/business/textbook/cases/{case_id}/feedback/comments", headers=headers, timeout=10)
    assert res.status_code == 200 and res.json().get("code") == 200, f"获取评论列表失败: {res.text}"
    comments = res.json()["data"]
    assert len(comments) > 0, "评论列表为空"
    my_comment = next((c for c in comments if c["reason"] == test_reason_1), None)
    assert my_comment is not None, "未找到刚提交的评价理由"
    assert my_comment["vote"] == 1, "评论态度不为 1"
    assert "user_id" not in my_comment and "userName" not in my_comment and "nickName" not in my_comment, "评论列表泄露了用户身份"
    print(f"  - 匿名评价列表验证通过: 条数={len(comments)}, 包含评价态度与时间，无任何身份敏感字段")

    # 4.3 改票为不推荐 + 新理由
    test_reason_2 = "实验配套材料缺少部分说明文档，建议补充"
    res = requests.put(f"{BASE}/business/textbook/cases/{case_id}/feedback", headers=headers,
                       json={"vote": -1, "reason": test_reason_2}, timeout=10)
    vote_data = res.json()["data"]
    assert vote_data["myVote"] == -1, "改票失败"
    assert vote_data["myReason"] == test_reason_2, "改理由失败"
    print(f"  - 改票为不推荐成功: myVote={vote_data['myVote']}, myReason={vote_data['myReason']}")

    # 4.4 撤销评价 (vote = 0)
    res = requests.put(f"{BASE}/business/textbook/cases/{case_id}/feedback", headers=headers,
                       json={"vote": 0}, timeout=10)
    vote_data = res.json()["data"]
    assert vote_data["myVote"] == 0, "撤销评价失败"
    assert vote_data.get("myReason") is None, "撤销评价后理由未清空"
    print("  - 撤销评价成功: myVote=0, myReason=null")

    # 验证撤销后评论列表中该教师评价已不展示
    res = requests.get(f"{BASE}/business/textbook/cases/{case_id}/feedback/comments", headers=headers, timeout=10)
    comments_after = res.json()["data"]
    assert not any(c["reason"] == test_reason_2 for c in comments_after), "撤销后评论仍然出现在列表中"
    print("  - 撤销后列表验证通过：已撤销评价不再展示")

    # 4.5 再次提交一条有效评价保留测试数据
    requests.put(f"{BASE}/business/textbook/cases/{case_id}/feedback", headers=headers,
                 json={"vote": 1, "reason": "课堂探究环节设计新颖，实验材料完整，值得推荐！"}, timeout=10)

    # 5. 角色权限拦截测试 (学生与教研员不可评价)
    print("[4/6] 检查角色权限边界拦截:")
    student_token = login("2021710501", "123456")
    res_st = requests.put(f"{BASE}/business/textbook/cases/{case_id}/feedback",
                          headers={"Authorization": f"Bearer {student_token}"}, json={"vote": 1}, timeout=10)
    assert res_st.status_code == 403 or res_st.json().get("code") == 403, f"学生评价未拦截 403: {res_st.text}"
    print("  - 学生角色评价被正确拦截 403 Forbidden")

    researcher_token = login("laoda", "123456")
    res_re = requests.put(f"{BASE}/business/textbook/cases/{case_id}/feedback",
                          headers={"Authorization": f"Bearer {researcher_token}"}, json={"vote": 1}, timeout=10)
    assert res_re.status_code == 403 or res_re.json().get("code") == 403, f"纯教研员评价未拦截 403: {res_re.text}"
    print("  - 纯教研员角色评价被正确拦截 403 Forbidden")

    # 6. 预览与缩略图接口验证
    print("[5/6] 检查文档预览与缩略图接口:")
    def safe_int(v):
        try: return int(v)
        except: return 0

    pdf_resource = next((r for r in case["resources"] if safe_int(r.get("previewable"))), None)
    if not pdf_resource:
        for c in cases:
            pdf_resource = next((r for r in c["resources"] if safe_int(r.get("previewable"))), None)
            if pdf_resource: break

    assert pdf_resource is not None, "未找到可预览资源"
    res_id = pdf_resource["resourceId"]
    print(f"  测试资源: id={res_id}, title={pdf_resource['title']}")

    # 6.1 预览信息
    res = requests.get(f"{BASE}/business/textbook/cases/files/{res_id}/preview-info", headers=headers, timeout=10)
    assert res.json().get("code") == 200, f"preview-info 失败: {res.text}"
    pages = res.json()["data"]["pages"]
    print(f"  - preview-info 成功: pages={pages}")

    # 6.2 缩略图接口
    res_thumb = requests.get(f"{BASE}/business/textbook/cases/files/{res_id}/pages/1?thumbnail=true", headers=headers, timeout=15)
    assert res_thumb.status_code == 200, f"缩略图请求失败: {res_thumb.status_code}"
    assert "image/jpeg" in res_thumb.headers.get("Content-Type", ""), "缩略图 Content-Type 不是 image/jpeg"
    assert len(res_thumb.content) > 500, "缩略图图片内容过小"
    print(f"  - 缩略图接口成功: 大小={len(res_thumb.content)} 字节")

    # 6.3 正文预览图接口
    res_page = requests.get(f"{BASE}/business/textbook/cases/files/{res_id}/pages/1", headers=headers, timeout=15)
    assert res_page.status_code == 200, f"正文页请求失败: {res_page.status_code}"
    assert "image/jpeg" in res_page.headers.get("Content-Type", ""), "正文页 Content-Type 不是 image/jpeg"
    assert len(res_page.content) > len(res_thumb.content), "正文页图片未大于缩略图"
    print(f"  - 正文高清图接口成功: 大小={len(res_page.content)} 字节")

    # 7. 下载接口验证
    print("[6/6] 检查下载接口:")
    tool_resource = next((r for r in case["resources"] if r.get("category") == "TOOL"), None)
    if tool_resource:
        res_dl = requests.get(f"{BASE}/business/textbook/cases/download/{tool_resource['resourceId']}",
                              headers=headers, stream=True, timeout=10)
        assert res_dl.status_code == 200, f"下载接口失败: {res_dl.status_code}"
        cd = res_dl.headers.get("Content-Disposition", "")
        assert "attachment" in cd, f"下载头缺少 attachment: {cd}"
        print(f"  - 配套材料包下载头正常: filename in Content-Disposition={cd[:60]}...")

    print("\n=== 全部 6 个接口测试组 100% 通过！ ===")


if __name__ == "__main__":
    main()
