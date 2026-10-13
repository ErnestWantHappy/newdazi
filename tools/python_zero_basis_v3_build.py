#!/usr/bin/env python3
"""生成并验证小学零基础 Python V3 题包、幂等导入 SQL 与回滚 SQL。"""

from __future__ import annotations

import ast
import hashlib
import json
import os
import subprocess
import sys
import tempfile
from collections import Counter
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]
PACKAGE_DIR = ROOT / "contexts/python-judge0/python-zero-basis-v3"
OUTPUT_DIR = ROOT / "output/python-zero-basis-v3"
IMPORT_SQL_PATTERN = "python_zero_basis_questions_v3_batch_{batch:02d}.sql"
ROLLBACK_SQL = ROOT / "sql/python_zero_basis_questions_v3_rollback.sql"
SYSTEM_CREATOR = "python-system-v3"
ROLLBACK_PROCEDURE_NAME = "rollback_python_zero_basis_v3"


if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")


QUESTIONS: list[dict[str, Any]] = []


def add(
    title: str,
    knowledge_points: list[str],
    description: str,
    input_description: str,
    output_description: str,
    constraints: str,
    hint: str,
    reference_code: str,
    inputs: list[str],
    *,
    no_input: bool = False,
) -> None:
    index = len(QUESTIONS) + 1
    difficulty = "SIMPLE" if index <= 78 else "MEDIUM" if index <= 95 else "HARD"
    learning_tier = "非常简单" if index <= 78 else "简单进阶" if index <= 95 else "趣味挑战"
    if index <= 78:
        school_stage = "小学五、六年级零基础"
    elif index <= 95:
        school_stage = "小学六年级或初中一年级入门"
    else:
        school_stage = "小学高年级拓展或初中入门"
    QUESTIONS.append(
        {
            "externalId": f"PYV3-{index:03d}",
            "title": title,
            "schoolStage": school_stage,
            "learningTier": learning_tier,
            "knowledgePoints": knowledge_points,
            "difficulty": difficulty,
            "description": description,
            "inputDescription": "本题无需输入。" if no_input else input_description,
            "outputDescription": output_description,
            "constraints": constraints,
            "sampleExplanation": "运行程序后，会得到公开样例所示的结果。",
            "hint": hint,
            "noInput": no_input,
            "starterCode": "# 请在这里编写程序\n",
            "referenceCode": reference_code.rstrip() + "\n",
            "timeLimitSeconds": 2.0,
            "memoryLimitKb": 131072,
            "isPublic": True,
            "testInputs": inputs,
        }
    )


# 第一组：print() 输出入门（18 道）
add("你好，Python", ["print输出"], "编写程序，在屏幕上输出一行“你好，Python”。", "", "输出一行：你好，Python", "必须完全按照指定文字输出。", "把要输出的文字放在 print() 的括号中，并用引号括起来。", 'print("你好，Python")', ["", ""], no_input=True)
add("输出数字 100", ["print输出", "整数"], "编写程序，直接输出整数 100。", "", "输出一行整数 100。", "不要输出其他文字。", "数字不需要加引号，也可以直接放进 print()。", "print(100)", ["", ""], no_input=True)
add("输出我的学校", ["print输出", "字符串"], "编写程序，输出“我的学校是向阳小学”。", "", "输出一行：我的学校是向阳小学", "文字和标点必须完全一致。", "中文文字要写在引号中。", 'print("我的学校是向阳小学")', ["", ""], no_input=True)
add("输出两行文字", ["print输出", "多行输出"], "第一行输出“开始学习”，第二行输出“天天进步”。", "", "按要求输出两行文字。", "每句话单独占一行。", "连续使用两次 print()，每次输出一行。", 'print("开始学习")\nprint("天天进步")', ["", ""], no_input=True)
add("输出三行数字", ["print输出", "多行输出"], "依次输出数字 1、2、3，每个数字单独占一行。", "", "依次输出三行数字 1、2、3。", "每行只能有一个数字。", "可以连续写三条 print() 语句。", "print(1)\nprint(2)\nprint(3)", ["", ""], no_input=True)
add("我爱信息科技", ["print输出", "字符串"], "编写程序，输出“我爱信息科技”。", "", "输出一行：我爱信息科技", "不要添加句号或空格。", "把完整句子作为一个字符串输出。", 'print("我爱信息科技")', ["", ""], no_input=True)
add("输出一串星号", ["print输出", "字符串"], "编写程序，输出 8 个连续的星号。", "", "输出一行 8 个星号。", "星号之间没有空格。", "先数好星号个数，再放进引号中。", 'print("********")', ["", ""], no_input=True)
add("输出两个英文单词", ["print输出", "空格"], "在同一行输出英文单词 Hello 和 Python，中间留一个空格。", "", "输出一行：Hello Python", "大小写必须一致，中间只有一个空格。", "可以把两个单词和空格一起写成一个字符串。", 'print("Hello Python")', ["", ""], no_input=True)
add("输出中文问候语", ["print输出", "字符串"], "编写程序，输出“同学，你好！”。", "", "输出一行：同学，你好！", "使用中文逗号和中文感叹号。", "注意核对中文标点。", 'print("同学，你好！")', ["", ""], no_input=True)
add("输出今天星期五", ["print输出", "字符串"], "编写程序，输出“今天星期五”。", "", "输出一行：今天星期五", "不要输出日期或其他说明。", "固定文字直接用 print() 输出即可。", 'print("今天星期五")', ["", ""], no_input=True)
add("输出两次欢迎语", ["print输出", "重复输出"], "连续输出两行“欢迎学习Python”。", "", "输出两行相同的欢迎语。", "两行内容必须完全相同。", "可以连续使用两次相同的 print()。", 'print("欢迎学习Python")\nprint("欢迎学习Python")', ["", ""], no_input=True)
add("输出简单表情", ["print输出", "字符"], "编写程序，输出由符号组成的笑脸 ^_^。", "", "输出一行：^_^", "不要在符号之间添加空格。", "符号也要作为字符串放进引号中。", 'print("^_^")', ["", ""], no_input=True)
add("输出一个整数", ["print输出", "整数"], "编写程序，输出整数 2026。", "", "输出一行整数 2026。", "不要输出小数点。", "直接把整数交给 print()。", "print(2026)", ["", ""], no_input=True)
add("输出一段固定对话", ["print输出", "多行输出"], "模拟一段固定对话：第一行输出“小明：你好！”，第二行输出“小红：你好！”。", "", "按顺序输出两行对话。", "姓名后的冒号使用中文冒号。", "每个人说的话使用一条 print()。", 'print("小明：你好！")\nprint("小红：你好！")', ["", ""], no_input=True)
add("输出 Hello", ["print输出", "英文字符串"], "编写程序，只输出英文单词 Hello。", "", "输出一行：Hello", "H 必须大写，其余字母小写。", "英文单词需要放在引号中。", 'print("Hello")', ["", ""], no_input=True)
add("输出一张固定成绩单", ["print输出", "多行输出"], "输出一张三行成绩单：第一行“姓名：小明”，第二行“学科：信息科技”，第三行“成绩：100”。", "", "严格按题目要求输出三行。", "使用中文冒号，不要添加多余空格。", "每一项使用一条 print()。", 'print("姓名：小明")\nprint("学科：信息科技")\nprint("成绩：100")', ["", ""], no_input=True)
add("用星号画小三角形", ["print输出", "字符图形"], "用星号输出一个三层小三角形，三行分别有 1、2、3 个星号。", "", "输出三行星号组成的小三角形。", "每行星号之间没有空格。", "从一颗星开始，每行增加一颗。", 'print("*")\nprint("**")\nprint("***")', ["", ""], no_input=True)
add("制作一张文字小名片", ["print输出", "多行输出"], "制作固定小名片：依次输出“姓名：小科”“班级：六（1）班”“爱好：编程”。", "", "严格按要求输出三行名片内容。", "使用中文标点，文字不能省略。", "把名片的每一项分别输出。", 'print("姓名：小科")\nprint("班级：六（1）班")\nprint("爱好：编程")', ["", ""], no_input=True)


# 第二组：变量与四则运算（22 道）
add("计算 1+2", ["加法", "print输出"], "计算 1 加 2 的结果并输出。", "", "输出计算结果 3。", "使用 Python 的加号完成计算。", "可以直接在 print() 中写算式。", "print(1 + 2)", ["", ""], no_input=True)
add("计算 10-3", ["减法", "print输出"], "计算 10 减 3 的结果并输出。", "", "输出计算结果 7。", "使用 Python 的减号完成计算。", "减法符号是 -。", "print(10 - 3)", ["", ""], no_input=True)
add("计算 4×5", ["乘法", "print输出"], "计算 4 乘 5 的结果并输出。", "", "输出计算结果 20。", "Python 中乘法符号是 *。", "不要使用数学书中的乘号 × 作为运算符。", "print(4 * 5)", ["", ""], no_input=True)
add("计算 20÷4", ["整数除法", "print输出"], "计算 20 除以 4 的整数结果并输出。", "", "输出整数 5。", "结果要求是整数。", "可以使用 // 得到整数商。", "print(20 // 4)", ["", ""], no_input=True)
add("三个数相加", ["加法", "变量"], "把 3、5、7 三个整数相加，并输出总和。", "", "输出三个数的和 15。", "三个数都是固定整数。", "可以先把三个数分别保存到变量中。", "a = 3\nb = 5\nc = 7\nprint(a + b + c)", ["", ""], no_input=True)
add("记录小明的年龄", ["变量", "赋值"], "用变量 age 记录小明的年龄 12，并输出这个变量。", "", "输出整数 12。", "变量名必须能表达它保存的内容。", "先赋值，再把变量交给 print()。", "age = 12\nprint(age)", ["", ""], no_input=True)
add("明年我几岁", ["变量", "加法", "输入"], "输入一个整数表示你今年的年龄，计算并输出明年的年龄。", "一行一个整数，表示今年的年龄。", "输出一行整数，表示明年的年龄。", "1 ≤ 年龄 ≤ 100。", "把输入转换为整数，再加 1。", "age = int(input())\nprint(age + 1)", ["12\n", "1\n", "99\n", "45\n"])
add("两数相减", ["变量", "减法", "输入"], "输入两个整数 a 和 b，输出 a-b 的结果。", "一行两个整数 a、b，中间用一个空格分隔。", "输出一行整数，表示 a-b。", "-1000 ≤ a,b ≤ 1000。", "先用 split() 分开两个数，再做减法。", "a, b = map(int, input().split())\nprint(a - b)", ["10 3\n", "3 10\n", "0 0\n", "-5 -8\n"])
add("两数相乘", ["变量", "乘法", "输入"], "输入两个整数，计算并输出它们的乘积。", "一行两个整数，中间用一个空格分隔。", "输出一行整数，表示两数乘积。", "-100 ≤ 两个整数 ≤ 100。", "Python 的乘法运算符是 *。", "a, b = map(int, input().split())\nprint(a * b)", ["4 5\n", "0 9\n", "-3 6\n", "-4 -7\n"])
add("求整数除法的商", ["整数除法", "输入"], "输入被除数和除数，输出整数除法得到的商。", "一行两个整数 a、b，b 不为 0。", "输出 a//b 的整数结果。", "0 ≤ a ≤ 10000，1 ≤ b ≤ 100。", "使用 // 运算符。", "a, b = map(int, input().split())\nprint(a // b)", ["20 4\n", "17 5\n", "0 3\n", "99 10\n"])
add("求除法的余数", ["取余", "输入"], "输入两个正整数，输出第一个数除以第二个数的余数。", "一行两个正整数 a、b。", "输出 a%b 的结果。", "1 ≤ a ≤ 10000，1 ≤ b ≤ 100。", "取余运算符是 %。", "a, b = map(int, input().split())\nprint(a % b)", ["17 5\n", "20 4\n", "3 5\n", "100 9\n"])
add("买苹果花多少钱", ["乘法", "生活应用"], "苹果每个 3 元，输入购买数量，计算总价。", "一行一个整数，表示苹果数量。", "输出一行整数，表示总价。", "0 ≤ 数量 ≤ 100。", "总价等于单价乘数量。", "count = int(input())\nprint(3 * count)", ["5\n", "0\n", "1\n", "20\n"])
add("买铅笔花多少钱", ["乘法", "生活应用"], "铅笔每支 2 元，输入购买数量，输出需要的钱数。", "一行一个整数，表示铅笔支数。", "输出一行整数，表示总价。", "0 ≤ 数量 ≤ 200。", "用固定单价 2 乘支数。", "count = int(input())\nprint(2 * count)", ["6\n", "0\n", "1\n", "50\n"])
add("买书包花多少钱", ["加法", "生活应用"], "一个书包 68 元，还要付 2 元环保袋费用。输入购买的书包数量，输出总费用。", "一行一个整数，表示书包数量。", "输出一行整数，表示书包总价加 2 元环保袋费用。", "1 ≤ 数量 ≤ 10。", "先算书包总价，再加固定的 2 元。", "count = int(input())\nprint(68 * count + 2)", ["1\n", "2\n", "5\n", "10\n"])
add("买东西找零钱", ["减法", "生活应用"], "输入付款金额和商品价格，计算应找回多少钱。", "一行两个整数，依次是付款金额和商品价格。", "输出一行整数，表示找零金额。", "付款金额不小于商品价格，二者不超过 1000。", "找零等于付款金额减商品价格。", "paid, price = map(int, input().split())\nprint(paid - price)", ["100 68\n", "50 50\n", "20 7\n", "1000 1\n"])
add("正方形的周长", ["乘法", "几何应用"], "输入正方形边长，计算并输出周长。", "一行一个正整数，表示边长。", "输出边长乘 4 的结果。", "1 ≤ 边长 ≤ 1000。", "正方形四条边一样长。", "side = int(input())\nprint(side * 4)", ["5\n", "1\n", "10\n", "1000\n"])
add("正方形的面积", ["乘法", "几何应用"], "输入正方形边长，计算并输出面积。", "一行一个正整数，表示边长。", "输出边长乘边长的结果。", "1 ≤ 边长 ≤ 1000。", "面积等于边长×边长。", "side = int(input())\nprint(side * side)", ["5\n", "1\n", "12\n", "1000\n"])
add("长方形的周长", ["加法", "乘法", "几何应用"], "输入长方形的长和宽，计算周长。", "一行两个正整数，依次表示长和宽。", "输出长方形周长。", "1 ≤ 长、宽 ≤ 1000。", "先把长和宽相加，再乘 2。", "length, width = map(int, input().split())\nprint((length + width) * 2)", ["6 4\n", "1 1\n", "10 3\n", "1000 999\n"])
add("长方形的面积", ["乘法", "几何应用"], "输入长方形的长和宽，计算面积。", "一行两个正整数，依次表示长和宽。", "输出长方形面积。", "1 ≤ 长、宽 ≤ 1000。", "面积等于长乘宽。", "length, width = map(int, input().split())\nprint(length * width)", ["6 4\n", "1 1\n", "10 3\n", "1000 999\n"])
add("三角形的周长", ["加法", "几何应用"], "输入三角形三条边的长度，输出三条边长度之和。", "一行三个正整数，依次表示三条边。", "输出三角形周长。", "输入保证可以组成三角形，每条边不超过 1000。", "周长就是三条边相加。", "a, b, c = map(int, input().split())\nprint(a + b + c)", ["3 4 5\n", "5 5 5\n", "2 3 4\n", "100 120 150\n"])
add("温度升高一度", ["变量", "加法", "输入"], "输入现在的整数温度，输出升高 1 摄氏度后的温度。", "一行一个整数，表示当前温度。", "输出一行整数，表示升高后的温度。", "-50 ≤ 温度 ≤ 50。", "温度可能是负数，直接加 1 即可。", "temperature = int(input())\nprint(temperature + 1)", ["20\n", "0\n", "-5\n", "50\n"])
add("计算三个成绩的平均分", ["加法", "除法", "格式化输出"], "输入三个整数成绩，计算平均分，并保留一位小数输出。", "一行三个整数成绩，中间用空格分隔。", "输出平均分，保留一位小数。", "0 ≤ 每个成绩 ≤ 100。", "先求总分，再除以 3，使用 :.1f 保留一位小数。", "a, b, c = map(int, input().split())\naverage = (a + b + c) / 3\nprint(f\"{average:.1f}\")", ["90 80 70\n", "100 100 100\n", "0 0 0\n", "88 89 90\n"])


# 第三组：input() 输入入门（20 道）
add("我的第一个输入", ["input输入", "字符串"], "输入一行文字，再把这行文字原样输出。", "一行文字，不包含换行符。", "原样输出输入的文字。", "文字长度为 1～30 个字符。", "input() 读到的内容本来就是字符串。", "text = input()\nprint(text)", ["Python\n", "信息科技\n", "123\n", "你好！\n"])
add("输入名字打招呼", ["input输入", "字符串拼接"], "输入一个名字，输出“你好，名字！”。", "一行一个名字。", "按“你好，名字！”的格式输出。", "名字长度为 1～10 个字符。", "可以使用 f 字符串把名字放进问候语。", "name = input()\nprint(f\"你好，{name}！\")", ["小明\n", "小红\n", "Python\n", "李老师\n"])
add("输入数字再输出", ["input输入", "整数"], "输入一个整数，再输出这个整数。", "一行一个整数。", "输出同一个整数。", "-10000 ≤ 整数 ≤ 10000。", "先用 int() 把输入转换成整数。", "number = int(input())\nprint(number)", ["8\n", "0\n", "-12\n", "10000\n"])
add("输入年龄加一岁", ["input输入", "变量更新"], "输入今年的年龄，把年龄变量增加 1 后输出。", "一行一个整数，表示今年年龄。", "输出增加 1 后的年龄。", "1 ≤ 年龄 ≤ 100。", "可以写 age = age + 1 更新变量。", "age = int(input())\nage = age + 1\nprint(age)", ["10\n", "1\n", "18\n", "99\n"])
add("输入两个数求和", ["input输入", "加法"], "输入两个整数，计算并输出它们的和。", "一行两个整数，中间用空格分隔。", "输出一行整数，表示两数之和。", "-10000 ≤ 两数 ≤ 10000。", "用 map(int, input().split()) 一次读入两个整数。", "a, b = map(int, input().split())\nprint(a + b)", ["3 5\n", "0 0\n", "-3 8\n", "10000 -10000\n"])
add("输入两个数求差", ["input输入", "减法"], "输入两个整数，输出第一个数减第二个数的结果。", "一行两个整数 a、b。", "输出 a-b。", "-10000 ≤ a,b ≤ 10000。", "注意减法有先后顺序。", "a, b = map(int, input().split())\nprint(a - b)", ["9 4\n", "4 9\n", "0 0\n", "-2 -7\n"])
add("输入两个数求积", ["input输入", "乘法"], "输入两个整数，输出它们的乘积。", "一行两个整数，中间用空格分隔。", "输出两数乘积。", "-1000 ≤ 两数 ≤ 1000。", "乘法运算符是 *。", "a, b = map(int, input().split())\nprint(a * b)", ["6 7\n", "0 99\n", "-5 4\n", "-8 -9\n"])
add("输入两个数求商", ["input输入", "整数除法"], "输入两个整数，输出第一个数整除第二个数的商。", "一行两个整数 a、b，b 不为 0。", "输出 a//b 的整数商。", "0 ≤ a ≤ 10000，1 ≤ b ≤ 100。", "使用 //，输出中不会出现小数点。", "a, b = map(int, input().split())\nprint(a // b)", ["18 3\n", "19 4\n", "0 5\n", "999 10\n"])
add("输入姓名和学校", ["input输入", "多行输入", "格式化输出"], "第一行输入姓名，第二行输入学校，输出“姓名在学校学习”。", "第一行是姓名，第二行是学校名称。", "按“姓名在学校学习”的格式输出。", "姓名和学校名称均不超过 20 个字符。", "连续使用两次 input() 读取两行。", "name = input()\nschool = input()\nprint(f\"{name}在{school}学习\")", ["小明\n向阳小学\n", "小红\n实验小学\n", "李华\n未来学校\n"])
add("你喜欢什么颜色", ["input输入", "字符串拼接"], "输入一种颜色，输出“我喜欢颜色”。", "一行一个颜色名称。", "按“我喜欢颜色”的格式输出。", "颜色名称长度为 1～10 个字符。", "把输入的颜色拼接到“我喜欢”后面。", "color = input()\nprint(f\"我喜欢{color}\")", ["蓝色\n", "红色\n", "绿色\n", "橙色\n"])
add("输入三个数求和", ["input输入", "加法"], "输入三个整数，输出它们的总和。", "一行三个整数，中间用空格分隔。", "输出一行整数，表示三个数之和。", "-1000 ≤ 每个整数 ≤ 1000。", "三个变量都要参与加法。", "a, b, c = map(int, input().split())\nprint(a + b + c)", ["1 2 3\n", "0 0 0\n", "-1 5 6\n", "1000 1000 1000\n"])
add("购买文具算总价", ["input输入", "乘法", "生活应用"], "输入文具单价和购买数量，输出总价。", "一行两个非负整数，依次是单价和数量。", "输出单价乘数量的结果。", "0 ≤ 单价 ≤ 100，0 ≤ 数量 ≤ 100。", "总价=单价×数量。", "price, count = map(int, input().split())\nprint(price * count)", ["3 5\n", "0 8\n", "12 1\n", "100 100\n"])
add("输入边长算周长", ["input输入", "乘法", "正方形"], "输入正方形边长，输出它的周长。", "一行一个正整数，表示边长。", "输出边长的 4 倍。", "1 ≤ 边长 ≤ 10000。", "正方形有四条相等的边。", "side = int(input())\nprint(side * 4)", ["7\n", "1\n", "25\n", "10000\n"])
add("输入边长算面积", ["input输入", "乘法", "正方形"], "输入正方形边长，输出它的面积。", "一行一个正整数，表示边长。", "输出边长乘边长的结果。", "1 ≤ 边长 ≤ 10000。", "面积不是边长乘 4，要用边长乘自己。", "side = int(input())\nprint(side * side)", ["7\n", "1\n", "25\n", "10000\n"])
add("输入长宽算周长", ["input输入", "长方形", "乘法"], "输入长方形的长和宽，输出周长。", "一行两个正整数，依次表示长和宽。", "输出长方形周长。", "1 ≤ 长、宽 ≤ 10000。", "周长=(长+宽)×2。", "length, width = map(int, input().split())\nprint((length + width) * 2)", ["8 5\n", "1 1\n", "20 3\n", "10000 1\n"])
add("输入长宽算面积", ["input输入", "长方形", "乘法"], "输入长方形的长和宽，输出面积。", "一行两个正整数，依次表示长和宽。", "输出长方形面积。", "1 ≤ 长、宽 ≤ 10000。", "面积=长×宽。", "length, width = map(int, input().split())\nprint(length * width)", ["8 5\n", "1 1\n", "20 3\n", "10000 1\n"])
add("厘米换算成米", ["input输入", "单位换算", "格式化输出"], "输入整数厘米数，换算成米并保留两位小数。", "一行一个非负整数，单位是厘米。", "输出米数，保留两位小数。", "0 ≤ 厘米数 ≤ 100000。", "1 米等于 100 厘米。", "cm = int(input())\nprint(f\"{cm / 100:.2f}\")", ["250\n", "0\n", "1\n", "100000\n"])
add("分钟换算成秒", ["input输入", "单位换算", "乘法"], "输入分钟数，换算成秒。", "一行一个非负整数，表示分钟数。", "输出对应的秒数。", "0 ≤ 分钟数 ≤ 10000。", "1 分钟等于 60 秒。", "minutes = int(input())\nprint(minutes * 60)", ["5\n", "0\n", "1\n", "10000\n"])
add("小时换算成分钟", ["input输入", "单位换算", "乘法"], "输入小时数，换算成分钟。", "一行一个非负整数，表示小时数。", "输出对应的分钟数。", "0 ≤ 小时数 ≤ 1000。", "1 小时等于 60 分钟。", "hours = int(input())\nprint(hours * 60)", ["2\n", "0\n", "1\n", "1000\n"])
add("给考试成绩加五分", ["input输入", "加法", "成绩"], "输入一个不超过 95 的考试成绩，输出加 5 分后的成绩。", "一行一个整数成绩。", "输出成绩加 5 后的结果。", "0 ≤ 成绩 ≤ 95。", "直接在原成绩基础上加 5。", "score = int(input())\nprint(score + 5)", ["80\n", "0\n", "90\n", "95\n"])


# 第四组：if 条件判断（18 道）
add("数字大于 10 吗", ["if判断", "比较运算"], "输入一个整数。如果大于 10 输出“是”，否则输出“否”。", "一行一个整数。", "根据判断输出“是”或“否”。", "-1000 ≤ 整数 ≤ 1000。", "比较表达式是 number > 10。", "number = int(input())\nif number > 10:\n    print(\"是\")\nelse:\n    print(\"否\")", ["15\n", "10\n", "11\n", "-3\n"])
add("判断是不是正数", ["if判断", "正数"], "输入一个整数，正数输出“正数”，否则输出“不是正数”。", "一行一个整数。", "输出判断结果。", "-1000 ≤ 整数 ≤ 1000。", "正数必须严格大于 0。", "number = int(input())\nif number > 0:\n    print(\"正数\")\nelse:\n    print(\"不是正数\")", ["8\n", "0\n", "-1\n", "1000\n"])
add("判断是不是零", ["if判断", "相等判断"], "输入一个整数，等于 0 输出“是零”，否则输出“不是零”。", "一行一个整数。", "输出判断结果。", "-1000 ≤ 整数 ≤ 1000。", "判断相等要使用 ==。", "number = int(input())\nif number == 0:\n    print(\"是零\")\nelse:\n    print(\"不是零\")", ["0\n", "1\n", "-1\n", "999\n"])
add("判断是不是偶数", ["if判断", "取余", "偶数"], "输入一个整数，偶数输出“偶数”，否则输出“不是偶数”。", "一行一个整数。", "输出判断结果。", "-10000 ≤ 整数 ≤ 10000。", "能被 2 整除的数除以 2 余数为 0。", "number = int(input())\nif number % 2 == 0:\n    print(\"偶数\")\nelse:\n    print(\"不是偶数\")", ["8\n", "7\n", "0\n", "-4\n"])
add("判断是不是奇数", ["if判断", "取余", "奇数"], "输入一个整数，奇数输出“奇数”，否则输出“不是奇数”。", "一行一个整数。", "输出判断结果。", "-10000 ≤ 整数 ≤ 10000。", "奇数除以 2 的余数不是 0。", "number = int(input())\nif number % 2 != 0:\n    print(\"奇数\")\nelse:\n    print(\"不是奇数\")", ["7\n", "8\n", "0\n", "-3\n"])
add("两个数谁更大", ["if判断", "比较运算"], "输入两个整数，输出较大的数；两个数相等时输出“相等”。", "一行两个整数 a、b。", "输出较大整数或文字“相等”。", "-1000 ≤ a,b ≤ 1000。", "需要分别处理 a>b、a<b 和相等。", "a, b = map(int, input().split())\nif a > b:\n    print(a)\nelif b > a:\n    print(b)\nelse:\n    print(\"相等\")", ["8 3\n", "3 8\n", "5 5\n", "-2 -7\n"])
add("两个数谁更小", ["if判断", "比较运算"], "输入两个整数，输出较小的数；两个数相等时输出“相等”。", "一行两个整数 a、b。", "输出较小整数或文字“相等”。", "-1000 ≤ a,b ≤ 1000。", "把大于号换成小于号，仍要处理相等。", "a, b = map(int, input().split())\nif a < b:\n    print(a)\nelif b < a:\n    print(b)\nelse:\n    print(\"相等\")", ["8 3\n", "3 8\n", "5 5\n", "-2 -7\n"])
add("是不是满分", ["if判断", "成绩"], "输入考试成绩，等于 100 输出“满分”，否则输出“继续努力”。", "一行一个整数成绩。", "根据成绩输出指定文字。", "0 ≤ 成绩 ≤ 100。", "满分必须正好等于 100。", "score = int(input())\nif score == 100:\n    print(\"满分\")\nelse:\n    print(\"继续努力\")", ["100\n", "99\n", "0\n", "60\n"])
add("考试及格了吗", ["if判断", "成绩"], "输入考试成绩，达到 60 分输出“及格”，否则输出“不及格”。", "一行一个整数成绩。", "输出“及格”或“不及格”。", "0 ≤ 成绩 ≤ 100。", "60 分也算及格，所以使用 >=。", "score = int(input())\nif score >= 60:\n    print(\"及格\")\nelse:\n    print(\"不及格\")", ["60\n", "59\n", "100\n", "0\n"])
add("上学有没有迟到", ["if判断", "时间"], "输入到校时间比规定时间晚了多少分钟。数值不大于 0 输出“准时”，大于 0 输出“迟到”。", "一行一个整数，表示晚到的分钟数；提前到用负数表示。", "输出“准时”或“迟到”。", "-60 ≤ 分钟数 ≤ 120。", "0 表示刚好按时，也属于准时。", "minutes = int(input())\nif minutes <= 0:\n    print(\"准时\")\nelse:\n    print(\"迟到\")", ["0\n", "5\n", "-10\n", "120\n"])
add("年龄是否符合活动要求", ["if判断", "逻辑与"], "活动允许 10～14 岁学生参加。输入年龄，符合输出“可以参加”，否则输出“不能参加”。", "一行一个整数年龄。", "输出是否可以参加。", "1 ≤ 年龄 ≤ 100。", "年龄要同时满足不小于 10 和不大于 14。", "age = int(input())\nif 10 <= age <= 14:\n    print(\"可以参加\")\nelse:\n    print(\"不能参加\")", ["12\n", "10\n", "14\n", "9\n", "15\n"])
add("温度是否过高", ["if判断", "温度"], "输入整数温度，35 摄氏度及以上输出“温度过高”，否则输出“温度正常”。", "一行一个整数温度。", "输出温度判断结果。", "-50 ≤ 温度 ≤ 60。", "临界值 35 也属于过高。", "temperature = int(input())\nif temperature >= 35:\n    print(\"温度过高\")\nelse:\n    print(\"温度正常\")", ["35\n", "34\n", "60\n", "-10\n"])
add("自动开灯还是关灯", ["if判断", "传感器"], "输入光线亮度值。亮度小于 50 输出“开灯”，否则输出“关灯”。", "一行一个整数亮度值。", "输出“开灯”或“关灯”。", "0 ≤ 亮度值 ≤ 100。", "亮度越小表示环境越暗。", "light = int(input())\nif light < 50:\n    print(\"开灯\")\nelse:\n    print(\"关灯\")", ["30\n", "50\n", "0\n", "100\n"])
add("判断输入的口令", ["if判断", "字符串比较"], "输入一行口令。口令等于 python 时输出“正确”，否则输出“错误”。", "一行一个只含小写英文字母的口令。", "输出“正确”或“错误”。", "口令长度为 1～20。", "字符串比较也使用 ==，大小写要一致。", "password = input()\nif password == \"python\":\n    print(\"正确\")\nelse:\n    print(\"错误\")", ["python\n", "Python\n", "hello\n", "pytho\n"])
add("数字是否不超过 100", ["if判断", "比较运算"], "输入一个整数，不超过 100 输出“范围内”，否则输出“超出范围”。", "一行一个整数。", "输出范围判断结果。", "-10000 ≤ 整数 ≤ 10000。", "不超过表示小于或等于。", "number = int(input())\nif number <= 100:\n    print(\"范围内\")\nelse:\n    print(\"超出范围\")", ["100\n", "101\n", "-5\n", "10000\n"])
add("三个数谁最大", ["if判断", "最大值"], "输入三个整数，使用条件判断找出并输出最大值。", "一行三个整数。", "输出三个数中的最大值。", "-1000 ≤ 每个整数 ≤ 1000。", "先把第一个数当作最大值，再依次比较。", "a, b, c = map(int, input().split())\nmaximum = a\nif b > maximum:\n    maximum = b\nif c > maximum:\n    maximum = c\nprint(maximum)", ["3 9 5\n", "8 2 1\n", "4 4 4\n", "-3 -8 -1\n"])
add("下雨是否带伞", ["if判断", "字符串比较", "生活应用"], "输入天气状态“下雨”或“不下雨”。下雨输出“带伞”，否则输出“不带伞”。", "一行一个天气状态。", "输出是否带伞。", "输入只会是“下雨”或“不下雨”。", "直接判断输入是否等于“下雨”。", "weather = input()\nif weather == \"下雨\":\n    print(\"带伞\")\nelse:\n    print(\"不带伞\")", ["下雨\n", "不下雨\n", "下雨\n"])
add("简单成绩等级判断", ["if判断", "多分支", "成绩"], "输入成绩：90 分及以上为 A，80～89 为 B，60～79 为 C，低于 60 为 D。", "一行一个整数成绩。", "输出对应的大写等级字母。", "0 ≤ 成绩 ≤ 100。", "条件应从最高分段开始判断。", "score = int(input())\nif score >= 90:\n    print(\"A\")\nelif score >= 80:\n    print(\"B\")\nelif score >= 60:\n    print(\"C\")\nelse:\n    print(\"D\")", ["95\n", "90\n", "89\n", "60\n", "59\n"])


# 第五组：for 循环（12 道）
add("输出 1 到 5", ["for循环", "range"], "使用 for 循环依次输出 1、2、3、4、5，每个数字占一行。", "", "依次输出五行数字 1 到 5。", "必须使用循环完成。", "range(1, 6) 会产生 1 到 5。", "for number in range(1, 6):\n    print(number)", ["", ""], no_input=True)
add("输出 1 到 10", ["for循环", "range"], "使用 for 循环依次输出 1 到 10，每个数字占一行。", "", "依次输出十行数字 1 到 10。", "必须使用循环完成。", "range 的结束值不包含在结果中。", "for number in range(1, 11):\n    print(number)", ["", ""], no_input=True)
add("输出五次 Hello", ["for循环", "重复输出"], "使用 for 循环输出五行 Hello。", "", "连续输出五行 Hello。", "必须使用循环，大小写要一致。", "循环变量可以暂时不用，用下划线表示。", "for _ in range(5):\n    print(\"Hello\")", ["", ""], no_input=True)
add("输出 1 到 n", ["for循环", "range", "输入"], "输入正整数 n，使用 for 循环从 1 输出到 n，每个数字占一行。", "一行一个正整数 n。", "依次输出 1 到 n。", "1 ≤ n ≤ 100。", "range(1, n+1) 可以包含 n。", "n = int(input())\nfor number in range(1, n + 1):\n    print(number)", ["5\n", "1\n", "2\n", "10\n"])
add("输出 2 到 10 的偶数", ["for循环", "range", "偶数"], "使用 for 循环输出 2、4、6、8、10，每个数字占一行。", "", "按从小到大的顺序输出五个偶数。", "必须使用循环完成。", "range 可以设置步长为 2。", "for number in range(2, 11, 2):\n    print(number)", ["", ""], no_input=True)
add("输出 1 到 9 的奇数", ["for循环", "range", "奇数"], "使用 for 循环输出 1、3、5、7、9，每个数字占一行。", "", "按从小到大的顺序输出五个奇数。", "必须使用循环完成。", "从 1 开始，每次增加 2。", "for number in range(1, 10, 2):\n    print(number)", ["", ""], no_input=True)
add("计算 1 到 5 的和", ["for循环", "累加"], "使用 for 循环计算 1+2+3+4+5，并输出结果。", "", "输出整数 15。", "必须使用循环和累加变量。", "先把总和设为 0，每次把当前数字加进去。", "total = 0\nfor number in range(1, 6):\n    total += number\nprint(total)", ["", ""], no_input=True)
add("计算 1 到 n 的和", ["for循环", "累加", "输入"], "输入正整数 n，计算 1 到 n 所有整数的和。", "一行一个正整数 n。", "输出 1 到 n 的总和。", "1 ≤ n ≤ 1000。", "使用变量 total 记录不断增加的总和。", "n = int(input())\ntotal = 0\nfor number in range(1, n + 1):\n    total += number\nprint(total)", ["5\n", "1\n", "10\n", "1000\n"])
add("输出 n 次星号", ["for循环", "重复输出", "输入"], "输入正整数 n，使用 for 循环输出 n 行星号，每行一个星号。", "一行一个正整数 n。", "输出 n 行，每行一个 *。", "1 ≤ n ≤ 20。", "循环执行几次，print() 就会运行几次。", "n = int(input())\nfor _ in range(n):\n    print(\"*\")", ["3\n", "1\n", "5\n", "20\n"])
add("小猫叫 n 声", ["for循环", "重复输出", "输入"], "输入正整数 n，使用循环输出 n 行“喵”。", "一行一个正整数 n。", "输出 n 行，每行一个“喵”。", "1 ≤ n ≤ 20。", "每次循环输出一次“喵”。", "n = int(input())\nfor _ in range(n):\n    print(\"喵\")", ["2\n", "1\n", "4\n", "20\n"])
add("从 5 倒数到 1", ["for循环", "倒序range"], "使用 for 循环依次输出 5、4、3、2、1，每个数字占一行。", "", "按倒序输出五行数字。", "必须使用循环完成。", "倒序 range 的步长可以写成 -1。", "for number in range(5, 0, -1):\n    print(number)", ["", ""], no_input=True)
add("九九乘法表的一行", ["for循环", "乘法表", "输入"], "输入 1～9 的整数 n，输出乘法表中第 n 行，从 n*1 到 n*n，每个算式占一行。", "一行一个整数 n。", "依次输出 n*1 到 n*n 的算式与结果。", "1 ≤ n ≤ 9。", "循环变量从 1 到 n，结果是 n 乘循环变量。", "n = int(input())\nfor i in range(1, n + 1):\n    print(f\"{n}*{i}={n * i}\")", ["3\n", "1\n", "5\n", "9\n"])


# 第六组：while 循环（5 道）
add("while 输出 1 到 5", ["while循环", "计数变量"], "使用 while 循环依次输出 1 到 5，每个数字占一行。", "", "依次输出五行数字 1 到 5。", "必须使用 while 循环。", "每轮循环后都要让计数变量增加 1。", "number = 1\nwhile number <= 5:\n    print(number)\n    number += 1", ["", ""], no_input=True)
add("while 输出 1 到 n", ["while循环", "计数变量", "输入"], "输入正整数 n，使用 while 循环依次输出 1 到 n。", "一行一个正整数 n。", "每个数字占一行，依次输出 1 到 n。", "1 ≤ n ≤ 100。", "循环条件可以写 number <= n。", "n = int(input())\nnumber = 1\nwhile number <= n:\n    print(number)\n    number += 1", ["4\n", "1\n", "10\n", "100\n"])
add("while 计算累加和", ["while循环", "累加", "输入"], "输入正整数 n，使用 while 循环计算 1 到 n 的和。", "一行一个正整数 n。", "输出 1 到 n 的总和。", "1 ≤ n ≤ 1000。", "同时维护计数变量和总和变量。", "n = int(input())\nnumber = 1\ntotal = 0\nwhile number <= n:\n    total += number\n    number += 1\nprint(total)", ["5\n", "1\n", "10\n", "1000\n"])
add("输入 0 结束统计", ["while循环", "哨兵值", "计数"], "每行输入一个整数，输入 0 时结束。统计并输出在 0 之前输入了几个非零整数。", "若干行整数，最后一行一定是 0。", "输出非零整数的个数。", "0 之前最多有 100 个整数。", "0 是结束标记，不计入数量。", "count = 0\nnumber = int(input())\nwhile number != 0:\n    count += 1\n    number = int(input())\nprint(count)", ["3\n5\n0\n", "0\n", "7\n0\n", "1\n2\n3\n4\n0\n"])
add("倒计时直到零", ["while循环", "倒计时", "输入"], "输入非负整数 n，使用 while 循环从 n 倒数到 0，每个数字占一行。", "一行一个非负整数 n。", "依次输出 n、n-1，直到 0。", "0 ≤ n ≤ 100。", "每轮输出后让 n 减少 1，循环条件要允许输出 0。", "number = int(input())\nwhile number >= 0:\n    print(number)\n    number -= 1", ["3\n", "0\n", "1\n", "10\n"])


# 第七组：教材经典趣味题（5 道）
add("猜数字第一关：判断是否猜对", ["if判断", "猜数字"], "第一行输入秘密数字，第二行输入猜测数字。如果相同输出“猜对了”，否则输出“没猜对”。", "两行整数，第一行是秘密数字，第二行是猜测数字。", "输出判断结果。", "1 ≤ 两个数字 ≤ 100，不使用随机数。", "直接比较秘密数字和猜测数字是否相等。", "secret = int(input())\nguess = int(input())\nif guess == secret:\n    print(\"猜对了\")\nelse:\n    print(\"没猜对\")", ["36\n36\n", "36\n20\n", "1\n100\n", "75\n75\n"])
add("猜数字第二关：猜大了还是猜小了", ["多分支", "猜数字"], "第一行输入秘密数字，第二行输入猜测数字。猜对输出“猜对了”，猜大输出“猜大了”，猜小输出“猜小了”。", "两行整数，第一行是秘密数字，第二行是猜测数字。", "输出三种判断结果之一。", "1 ≤ 两个数字 ≤ 100，不使用随机数。", "先判断相等，再比较猜测数字的大小。", "secret = int(input())\nguess = int(input())\nif guess == secret:\n    print(\"猜对了\")\nelif guess > secret:\n    print(\"猜大了\")\nelse:\n    print(\"猜小了\")", ["36\n36\n", "36\n50\n", "36\n20\n", "1\n100\n", "100\n1\n"])
add("鸡兔同笼第一关：计算鸡兔总腿数", ["变量", "乘法", "鸡兔同笼"], "输入鸡的只数和兔的只数，计算它们一共有多少条腿。鸡有 2 条腿，兔有 4 条腿。", "一行两个非负整数，依次是鸡和兔的只数。", "输出腿的总数。", "0 ≤ 鸡、兔只数 ≤ 100。", "总腿数=鸡数×2+兔数×4。", "chickens, rabbits = map(int, input().split())\nprint(chickens * 2 + rabbits * 4)", ["3 2\n", "0 5\n", "8 0\n", "0 0\n", "100 100\n"])
add("鸡兔同笼第二关：枚举鸡和兔的数量", ["for循环", "枚举", "鸡兔同笼"], "输入动物总只数和腿的总数，枚举并输出鸡兔数量，格式为“鸡x 兔y”；没有答案时输出“无解”。", "一行两个非负整数，依次是总只数和总腿数。", "输出唯一的鸡兔数量组合，或输出“无解”。", "0 ≤ 总只数 ≤ 100，0 ≤ 总腿数 ≤ 400。", "枚举兔的只数，鸡的只数等于总只数减兔数。", "heads, legs = map(int, input().split())\nfound = False\nfor rabbits in range(heads + 1):\n    chickens = heads - rabbits\n    if chickens * 2 + rabbits * 4 == legs:\n        print(f\"鸡{chickens} 兔{rabbits}\")\n        found = True\n        break\nif not found:\n    print(\"无解\")", ["5 14\n", "10 20\n", "10 40\n", "3 7\n", "0 0\n"])
add("韩信点兵入门：在小范围内寻找符合余数条件的数字", ["for循环", "取余", "枚举"], "输入上限 n，在 1～n 中寻找最小的整数 x，使 x 除以 3 余 2、除以 5 余 3、除以 7 余 2。找到就输出 x，否则输出“没有找到”。", "一行一个正整数 n。", "输出最小的符合条件的整数，或“没有找到”。", "1 ≤ n ≤ 500。", "从 1 开始逐个尝试，同时检查三个余数条件。", "n = int(input())\nfound = False\nfor number in range(1, n + 1):\n    if number % 3 == 2 and number % 5 == 3 and number % 7 == 2:\n        print(number)\n        found = True\n        break\nif not found:\n    print(\"没有找到\")", ["30\n", "22\n", "23\n", "100\n", "500\n"])


def normalized_output(value: str) -> str:
    lines = [line.rstrip(" \t\r") for line in value.replace("\r\n", "\n").replace("\r", "\n").split("\n")]
    while lines and lines[-1] == "":
        lines.pop()
    return "\n".join(lines)


def run_reference(code: str, input_text: str, external_id: str) -> str:
    child_env = os.environ.copy()
    child_env["PYTHONIOENCODING"] = "utf-8"
    completed = subprocess.run(
        [sys.executable, "-c", code],
        input=input_text,
        text=True,
        encoding="utf-8",
        capture_output=True,
        timeout=3,
        cwd=tempfile.gettempdir(),
        env=child_env,
    )
    if completed.returncode != 0:
        raise RuntimeError(f"{external_id} 参考答案运行失败：{completed.stderr.strip()}")
    return normalized_output(completed.stdout) + "\n"


def weights(count: int) -> list[int]:
    base, remainder = divmod(100, count)
    return [base + (1 if index < remainder else 0) for index in range(count)]


def finalize_questions() -> None:
    for question in QUESTIONS:
        inputs = question.pop("testInputs")
        case_weights = weights(len(inputs))
        cases = []
        for index, input_text in enumerate(inputs, start=1):
            cases.append(
                {
                    "caseName": "公开样例" if index == 1 else f"隐藏测试点 {index - 1}",
                    "inputText": "" if question["noInput"] else input_text,
                    "expectedOutput": run_reference(question["referenceCode"], "" if question["noInput"] else input_text, question["externalId"]),
                    "isPublic": index == 1,
                    "scoreWeight": case_weights[index - 1],
                    "orderNum": index,
                }
            )
        question["testCases"] = cases
        sample = cases[0]
        if question["noInput"]:
            question["sampleExplanation"] = "本题没有输入，程序运行后直接输出固定内容。"
        else:
            shown_input = sample["inputText"].strip().replace("\n", "、")
            shown_output = sample["expectedOutput"].strip().replace("\n", "、")
            question["sampleExplanation"] = f"输入 {shown_input} 时，按题意计算后输出 {shown_output}。"


def executable_line_count(code: str) -> int:
    return sum(1 for line in code.splitlines() if line.strip() and not line.lstrip().startswith("#"))


def validate_questions() -> dict[str, Any]:
    errors: list[str] = []
    if len(QUESTIONS) != 100:
        errors.append(f"题目数量应为 100，当前为 {len(QUESTIONS)}")
    expected_titles = [
        "你好，Python", "输出数字 100", "输出我的学校", "输出两行文字", "输出三行数字", "我爱信息科技", "输出一串星号", "输出两个英文单词", "输出中文问候语", "输出今天星期五", "输出两次欢迎语", "输出简单表情", "输出一个整数", "输出一段固定对话", "输出 Hello", "输出一张固定成绩单", "用星号画小三角形", "制作一张文字小名片",
        "计算 1+2", "计算 10-3", "计算 4×5", "计算 20÷4", "三个数相加", "记录小明的年龄", "明年我几岁", "两数相减", "两数相乘", "求整数除法的商", "求除法的余数", "买苹果花多少钱", "买铅笔花多少钱", "买书包花多少钱", "买东西找零钱", "正方形的周长", "正方形的面积", "长方形的周长", "长方形的面积", "三角形的周长", "温度升高一度", "计算三个成绩的平均分",
        "我的第一个输入", "输入名字打招呼", "输入数字再输出", "输入年龄加一岁", "输入两个数求和", "输入两个数求差", "输入两个数求积", "输入两个数求商", "输入姓名和学校", "你喜欢什么颜色", "输入三个数求和", "购买文具算总价", "输入边长算周长", "输入边长算面积", "输入长宽算周长", "输入长宽算面积", "厘米换算成米", "分钟换算成秒", "小时换算成分钟", "给考试成绩加五分",
        "数字大于 10 吗", "判断是不是正数", "判断是不是零", "判断是不是偶数", "判断是不是奇数", "两个数谁更大", "两个数谁更小", "是不是满分", "考试及格了吗", "上学有没有迟到", "年龄是否符合活动要求", "温度是否过高", "自动开灯还是关灯", "判断输入的口令", "数字是否不超过 100", "三个数谁最大", "下雨是否带伞", "简单成绩等级判断",
        "输出 1 到 5", "输出 1 到 10", "输出五次 Hello", "输出 1 到 n", "输出 2 到 10 的偶数", "输出 1 到 9 的奇数", "计算 1 到 5 的和", "计算 1 到 n 的和", "输出 n 次星号", "小猫叫 n 声", "从 5 倒数到 1", "九九乘法表的一行",
        "while 输出 1 到 5", "while 输出 1 到 n", "while 计算累加和", "输入 0 结束统计", "倒计时直到零",
        "猜数字第一关：判断是否猜对", "猜数字第二关：猜大了还是猜小了", "鸡兔同笼第一关：计算鸡兔总腿数", "鸡兔同笼第二关：枚举鸡和兔的数量", "韩信点兵入门：在小范围内寻找符合余数条件的数字",
    ]
    actual_titles = [question["title"] for question in QUESTIONS]
    if actual_titles != expected_titles:
        errors.append("题目标题或顺序与用户指定清单不一致")
    if len(set(actual_titles)) != len(actual_titles):
        errors.append("题目标题存在重复")
    if len({question["externalId"] for question in QUESTIONS}) != len(QUESTIONS):
        errors.append("外部题号存在重复")
    difficulty_counts = Counter(question["difficulty"] for question in QUESTIONS)
    if difficulty_counts != Counter({"SIMPLE": 78, "MEDIUM": 17, "HARD": 5}):
        errors.append(f"难度分布不正确：{dict(difficulty_counts)}")
    for index, question in enumerate(QUESTIONS, start=1):
        prefix = question["externalId"]
        try:
            tree = ast.parse(question["referenceCode"], filename=prefix)
        except SyntaxError as exc:
            errors.append(f"{prefix} 参考答案语法错误：{exc}")
            continue
        for node in ast.walk(tree):
            if isinstance(node, (ast.Import, ast.ImportFrom)):
                errors.append(f"{prefix} 不允许导入模块")
            if isinstance(node, ast.Call) and isinstance(node.func, ast.Name) and node.func.id in {"eval", "exec", "open", "compile", "__import__"}:
                errors.append(f"{prefix} 使用了禁止函数 {node.func.id}")
        line_count = executable_line_count(question["referenceCode"])
        if index <= 60 and line_count > 5:
            errors.append(f"{prefix} 前 60 题参考答案超过 5 行：{line_count}")
        cases = question["testCases"]
        expected_case_count = 2 if question["noInput"] else None
        if expected_case_count is not None and len(cases) != expected_case_count:
            errors.append(f"{prefix} 固定输出题必须恰好 2 个测试点")
        if not question["noInput"] and not 3 <= len(cases) <= 5:
            errors.append(f"{prefix} 有输入题测试点数量不是 3～5")
        if sum(1 for case in cases if case["isPublic"]) < 1 or sum(1 for case in cases if not case["isPublic"]) < 1:
            errors.append(f"{prefix} 缺少公开或隐藏测试点")
        if sum(case["scoreWeight"] for case in cases) != 100:
            errors.append(f"{prefix} 测试点权重之和不是 100")
        for case in cases:
            actual = run_reference(question["referenceCode"], case["inputText"], prefix)
            if normalized_output(actual) != normalized_output(case["expectedOutput"]):
                errors.append(f"{prefix} {case['caseName']} 预期输出不一致")
    group_counts = {
        "print输出": 18,
        "变量与四则运算": 22,
        "input输入": 20,
        "if条件判断": 18,
        "for循环": 12,
        "while循环": 5,
        "趣味挑战": 5,
    }
    report = {
        "valid": not errors,
        "errors": errors,
        "questionCount": len(QUESTIONS),
        "testCaseCount": sum(len(question["testCases"]) for question in QUESTIONS),
        "difficultyCounts": dict(difficulty_counts),
        "groupCounts": group_counts,
        "first60MaxExecutableLines": max(executable_line_count(question["referenceCode"]) for question in QUESTIONS[:60]),
        "noInputCount": sum(1 for question in QUESTIONS if question["noInput"]),
        "inputQuestionCount": sum(1 for question in QUESTIONS if not question["noInput"]),
    }
    return report


def utf8_sql(value: str | None) -> str:
    if value is None:
        return "NULL"
    raw = value.encode("utf-8")
    if not raw:
        return "''"
    return f"CONVERT(0x{raw.hex().upper()} USING utf8mb4)"


def number_sql(value: int | float) -> str:
    return str(value) if isinstance(value, int) else format(value, ".10g")


def id_list_sql(questions: list[dict[str, Any]] | None = None) -> str:
    items = QUESTIONS if questions is None else questions
    return ",".join(utf8_sql(question["externalId"]) for question in items)


def title_list_sql(questions: list[dict[str, Any]] | None = None) -> str:
    items = QUESTIONS if questions is None else questions
    return ",".join(utf8_sql(question["title"]) for question in items)


def question_sql(question: dict[str, Any]) -> list[str]:
    external_id = question["externalId"]
    variable = "@python_v3_question_id"
    knowledge_points = "，".join(question["knowledgePoints"])
    notes = f"适用学段：{question['schoolStage']}。难度层级：{question['learningTier']}。解题提示：{question['hint']}"
    analysis = f"Python 零基础系统题 V3；稳定题号：{external_id}。"
    lines = [
        f"    -- {external_id} {question['title']}",
        f"    SET {variable} = (SELECT question_id FROM biz_programming_question_config WHERE external_id={utf8_sql(external_id)} LIMIT 1);",
        "    INSERT INTO biz_question",
        "        (question_type,question_content,grade,difficulty,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)",
        "    SELECT",
        f"        'practical',{utf8_sql(question['description'])},NULL,{utf8_sql(question['difficulty'])},NULL,NULL,{utf8_sql(question['referenceCode'])},{utf8_sql(analysis)},'PYTHON','Y',v_creator_id,v_creator_username,NOW(),v_creator_username,NOW()",
        f"    WHERE {variable} IS NULL;",
        f"    SET {variable}=COALESCE({variable},LAST_INSERT_ID());",
        "    UPDATE biz_question SET",
        f"        question_type='practical',question_content={utf8_sql(question['description'])},grade=NULL,difficulty={utf8_sql(question['difficulty'])},semester=NULL,lesson_num=NULL,",
        f"        answer={utf8_sql(question['referenceCode'])},analysis={utf8_sql(analysis)},practical_mode='PYTHON',is_public='Y',creator_id=v_creator_id,create_by=v_creator_username,update_by=v_creator_username,update_time=NOW()",
        f"    WHERE question_id={variable};",
        "    INSERT INTO biz_programming_question_config",
        "        (question_id,language_code,external_id,title,knowledge_points,no_input,validation_status,validated_at,validated_by,content_version,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)",
        "    VALUES",
        f"        ({variable},'python',{utf8_sql(external_id)},{utf8_sql(question['title'])},{utf8_sql(knowledge_points)},{'1' if question['noInput'] else '0'},'VALID',NOW(),'{SYSTEM_CREATOR}',3,{utf8_sql(question['starterCode'])},{utf8_sql(question['inputDescription'])},{utf8_sql(question['outputDescription'])},{utf8_sql(question['sampleExplanation'])},{utf8_sql(question['constraints'])},{utf8_sql(notes)},{number_sql(question['timeLimitSeconds'])},{number_sql(question['memoryLimitKb'])},8,1024,64,'1','{SYSTEM_CREATOR}',NOW(),'{SYSTEM_CREATOR}',NOW())",
        "    ON DUPLICATE KEY UPDATE",
        "        title=VALUES(title),knowledge_points=VALUES(knowledge_points),no_input=VALUES(no_input),validation_status='VALID',validated_at=NOW(),validated_by=VALUES(validated_by),content_version=3,starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),time_limit_seconds=VALUES(time_limit_seconds),memory_limit_kb=VALUES(memory_limit_kb),max_processes=8,max_file_size_kb=1024,max_output_kb=64,enabled='1',update_by=VALUES(update_by),update_time=NOW();",
        f"    DELETE FROM biz_programming_test_case WHERE question_id={variable};",
    ]
    for case in question["testCases"]:
        lines.extend(
            [
                "    INSERT INTO biz_programming_test_case",
                "        (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)",
                f"    VALUES ({variable},{utf8_sql(case['caseName'])},{utf8_sql(case['inputText'])},{utf8_sql(case['expectedOutput'])},{'1' if case['isPublic'] else '0'},{case['scoreWeight']},{case['orderNum']},'{SYSTEM_CREATOR}',NOW(),'{SYSTEM_CREATOR}',NOW());",
            ]
        )
    lines.append("")
    return lines


def build_import_sql(questions: list[dict[str, Any]], package_path: Path, batch: int) -> str:
    ids = id_list_sql(questions)
    titles = title_list_sql(questions)
    expected_questions = len(questions)
    expected_cases = sum(len(question["testCases"]) for question in questions)
    procedure_name = f"import_python_zero_basis_v3_batch_{batch:02d}"
    lines = [
        f"-- Python 零基础系统题库 V3 第 {batch} 批幂等导入脚本。",
        "-- 执行前必须备份目标数据库；每次执行会按 PYV3 外部题号更新本题包并重建对应测试点。",
        "-- 全部中文以 UTF-8 十六进制写入，避免 Windows 客户端代码页污染。",
        f"-- 题目数：{expected_questions}；测试点数：{expected_cases}。",
        f"-- {package_path.name}: {hashlib.sha256(package_path.read_bytes()).hexdigest().upper()}",
        "SET NAMES utf8mb4;",
        f"DROP PROCEDURE IF EXISTS {procedure_name};",
        "DELIMITER $$",
        f"CREATE PROCEDURE {procedure_name}()",
        "BEGIN",
        "    DECLARE v_count INT DEFAULT 0;",
        "    DECLARE v_creator_id BIGINT DEFAULT NULL;",
        "    DECLARE v_creator_username VARCHAR(64) DEFAULT NULL;",
        "    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;",
        f"    SELECT COUNT(*) INTO v_count FROM sys_user u WHERE u.nick_name={utf8_sql('郑东旭')} COLLATE utf8mb4_general_ci AND u.status='0' AND u.del_flag='0' AND EXISTS (SELECT 1 FROM sys_user_role ur INNER JOIN sys_role r ON r.role_id=ur.role_id WHERE ur.user_id=u.user_id AND r.role_key='teacher' AND r.status='0');",
        "    IF v_count<>1 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='郑东旭有效教师账号不是唯一一条，已拒绝导入'; END IF;",
        f"    SELECT u.user_id,u.user_name INTO v_creator_id,v_creator_username FROM sys_user u WHERE u.nick_name={utf8_sql('郑东旭')} COLLATE utf8mb4_general_ci AND u.status='0' AND u.del_flag='0' AND EXISTS (SELECT 1 FROM sys_user_role ur INNER JOIN sys_role r ON r.role_id=ur.role_id WHERE ur.user_id=u.user_id AND r.role_key='teacher' AND r.status='0') LIMIT 1;",
        f"    SELECT COUNT(*) INTO v_count FROM biz_programming_question_config WHERE external_id IN ({ids}) AND COALESCE(create_by,'')<>'{SYSTEM_CREATOR}';",
        "    IF v_count<>0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='PYV3 外部题号已被其他来源占用，已拒绝导入'; END IF;",
        f"    SELECT COUNT(*) INTO v_count FROM biz_programming_question_config WHERE title IN ({titles}) AND (external_id IS NULL OR external_id NOT IN ({ids}));",
        "    IF v_count<>0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='题库已存在同名非 PYV3 题目，已拒绝导入'; END IF;",
        "    START TRANSACTION;",
        "",
    ]
    for question in questions:
        lines.extend(question_sql(question))
    lines.extend(
        [
            "    SELECT COUNT(*) INTO v_count FROM biz_programming_question_config c INNER JOIN biz_question q ON q.question_id=c.question_id",
            f"    WHERE c.external_id IN ({ids}) AND c.create_by='{SYSTEM_CREATOR}' AND q.creator_id=v_creator_id AND CAST(q.create_by AS BINARY)=CAST(v_creator_username AS BINARY) AND q.question_type='practical' AND q.practical_mode='PYTHON' AND q.grade IS NULL AND q.semester IS NULL AND q.lesson_num IS NULL AND q.is_public='Y' AND c.validation_status='VALID' AND c.enabled='1';",
            f"    IF v_count<>{expected_questions} THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='本批 PYV3 题目或配置后检数量不一致，事务已回滚'; END IF;",
            "    SELECT COUNT(*) INTO v_count FROM biz_programming_test_case tc INNER JOIN biz_programming_question_config c ON c.question_id=tc.question_id",
            f"    WHERE c.external_id IN ({ids});",
            f"    IF v_count<>{expected_cases} THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='PYV3 测试点后检数量不一致，事务已回滚'; END IF;",
            "    SELECT COUNT(*) INTO v_count FROM (",
            "        SELECT c.question_id FROM biz_programming_question_config c INNER JOIN biz_programming_test_case tc ON tc.question_id=c.question_id",
            f"        WHERE c.external_id IN ({ids}) GROUP BY c.question_id,c.no_input",
            "        HAVING SUM(tc.is_public='1')>=1 AND SUM(tc.is_public='0')>=1 AND ROUND(SUM(tc.score_weight),6)=100",
            "           AND ((c.no_input='1' AND COUNT(*)=2) OR (c.no_input='0' AND COUNT(*) BETWEEN 3 AND 5))",
            "    ) checked_questions;",
            f"    IF v_count<>{expected_questions} THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='本批 PYV3 测试点公开性、数量或权重后检失败，事务已回滚'; END IF;",
            "    COMMIT;",
            "END$$",
            "DELIMITER ;",
            f"CALL {procedure_name}();",
            f"DROP PROCEDURE IF EXISTS {procedure_name};",
            "SELECT q.difficulty,COUNT(*) question_count FROM biz_programming_question_config c INNER JOIN biz_question q ON q.question_id=c.question_id",
            f"WHERE c.external_id IN ({ids}) GROUP BY q.difficulty ORDER BY q.difficulty;",
            "SELECT COUNT(*) question_count,SUM(case_count) test_case_count FROM (SELECT c.question_id,COUNT(tc.test_case_id) case_count FROM biz_programming_question_config c INNER JOIN biz_programming_test_case tc ON tc.question_id=c.question_id",
            f"WHERE c.external_id IN ({ids}) GROUP BY c.question_id) x;",
            "",
        ]
    )
    return "\n".join(lines)


def build_rollback_sql() -> str:
    ids = id_list_sql()
    dependency_tables = [
        "biz_lesson_question",
        "biz_programming_draft",
        "biz_programming_submission",
        "biz_python_practice_plan_question",
        "biz_python_practice_extension_question",
        "biz_python_practice_question_snapshot",
        "biz_python_practice_draft",
        "biz_python_practice_submission",
        "biz_python_practice_progress",
    ]
    dependency_terms = [f"(SELECT COUNT(*) FROM {table} d INNER JOIN biz_programming_question_config c ON c.question_id=d.question_id WHERE c.external_id IN ({ids}))" for table in dependency_tables]
    lines = [
        "-- Python 零基础系统题库 V3 精确回滚脚本。存在任何课程、题单、快照、草稿或提交依赖时拒绝删除。",
        "SET NAMES utf8mb4;",
        f"DROP PROCEDURE IF EXISTS {ROLLBACK_PROCEDURE_NAME};",
        "DELIMITER $$",
        f"CREATE PROCEDURE {ROLLBACK_PROCEDURE_NAME}()",
        "BEGIN",
        "    DECLARE v_count INT DEFAULT 0;",
        "    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;",
        "    SELECT " + " + ".join(dependency_terms) + " INTO v_count;",
        "    IF v_count<>0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='PYV3 题目已有业务依赖，拒绝直接回滚删除'; END IF;",
        "    START TRANSACTION;",
        f"    DELETE tc FROM biz_programming_test_case tc INNER JOIN biz_programming_question_config c ON c.question_id=tc.question_id WHERE c.external_id IN ({ids}) AND c.create_by='{SYSTEM_CREATOR}';",
        f"    DELETE FROM biz_programming_question_config WHERE external_id IN ({ids}) AND create_by='{SYSTEM_CREATOR}';",
        f"    DELETE q FROM biz_question q LEFT JOIN biz_programming_question_config c ON c.question_id=q.question_id WHERE c.question_id IS NULL AND q.analysis LIKE {utf8_sql('Python 零基础系统题 V3；稳定题号：%')};",
        "    COMMIT;",
        "END$$",
        "DELIMITER ;",
        f"CALL {ROLLBACK_PROCEDURE_NAME}();",
        f"DROP PROCEDURE IF EXISTS {ROLLBACK_PROCEDURE_NAME};",
        "",
    ]
    return "\n".join(lines)


def write_outputs(report: dict[str, Any]) -> list[Path]:
    PACKAGE_DIR.mkdir(parents=True, exist_ok=True)
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    blueprint = [
        {
            "externalId": question["externalId"],
            "title": question["title"],
            "schoolStage": question["schoolStage"],
            "learningTier": question["learningTier"],
            "knowledgePoints": question["knowledgePoints"],
            "difficulty": question["difficulty"],
        }
        for question in QUESTIONS
    ]
    (PACKAGE_DIR / "blueprint.json").write_text(json.dumps(blueprint, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    package_paths = []
    for batch in range(5):
        path = PACKAGE_DIR / f"batch-{batch + 1:02d}.json"
        path.write_text(json.dumps(QUESTIONS[batch * 20 : (batch + 1) * 20], ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        package_paths.append(path)
    report["packageSha256"] = {path.name: hashlib.sha256(path.read_bytes()).hexdigest().upper() for path in package_paths}
    (OUTPUT_DIR / "validation-report.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    import_paths = []
    for batch, package_path in enumerate(package_paths, start=1):
        import_path = ROOT / "sql" / IMPORT_SQL_PATTERN.format(batch=batch)
        questions = QUESTIONS[(batch - 1) * 20 : batch * 20]
        import_path.write_text(build_import_sql(questions, package_path, batch), encoding="utf-8", newline="\n")
        import_paths.append(import_path)
    ROLLBACK_SQL.write_text(build_rollback_sql(), encoding="utf-8", newline="\n")
    return package_paths + import_paths


def main() -> int:
    try:
        finalize_questions()
        report = validate_questions()
        if not report["valid"]:
            for error in report["errors"]:
                print(error, file=sys.stderr)
            return 1
        generated_paths = write_outputs(report)
    except (OSError, ValueError, RuntimeError, subprocess.SubprocessError) as exc:
        print(f"生成失败：{exc}", file=sys.stderr)
        return 1
    print(f"已生成 100 道题、{report['testCaseCount']} 个测试点，5 个批次。")
    print(f"难度分布：{report['difficultyCounts']}；前 60 题最多 {report['first60MaxExecutableLines']} 行有效代码。")
    print("导入 SQL：5 个 batch 文件；回滚 SQL：" + ROLLBACK_SQL.name + "。")
    for path in generated_paths[:5]:
        print(f"{path.name} SHA-256 {report['packageSha256'][path.name]}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
