-- Python 系统题库 v1：80 道分层入门题，幂等导入统一题库与 Python 判题配置。
-- 执行前请完成目标库备份；本脚本只触碰 create_by=python-system-v1 标记的数据。
SET NAMES utf8mb4;
CREATE TEMPORARY TABLE tmp_python_system_question (seq INT PRIMARY KEY, stage VARCHAR(32), title VARCHAR(255), input1 MEDIUMTEXT, output1 MEDIUMTEXT, input2 MEDIUMTEXT, output2 MEDIUMTEXT, starter_code MEDIUMTEXT, reference_code MEDIUMTEXT) ENGINE=InnoDB;
INSERT INTO tmp_python_system_question (seq,stage,title,input1,output1,input2,output2,starter_code,reference_code) VALUES
(1,'BEGINNER','输出 Hello Python','','Hello Python','','Hello Python','# 请在这里完成程序\n','print("Hello Python")'),
(2,'BEGINNER','输出一句信息科技口号','','信息科技让生活更美好','','信息科技让生活更美好','# 请在这里完成程序\n','print("信息科技让生活更美好")'),
(3,'BEGINNER','输出当前练习年份','','2026','','2026','# 请在这里完成程序\n','print(2026)'),
(4,'BEGINNER','向同学问好','小明','你好，小明','小红','你好，小红','# 请在这里完成程序\n','name=input().strip(); print("你好，"+name)'),
(5,'BEGINNER','求下一个整数','12','13','-3','-2','# 请在这里完成程序\n','n=int(input()); print(n+1)'),
(6,'BEGINNER','两个数求和','3 5','8','-2 7','5','# 请在这里完成程序\n','a,b=map(int,input().split()); print(a+b)'),
(7,'BEGINNER','长方形面积','4 6','24','10 3','30','# 请在这里完成程序\n','a,b=map(int,input().split()); print(a*b)'),
(8,'BEGINNER','长方形周长','4 6','20','10 3','26','# 请在这里完成程序\n','a,b=map(int,input().split()); print(2*(a+b))'),
(9,'BEGINNER','摄氏温度转华氏温度','25','77.0','0','32.0','# 请在这里完成程序\n','c=float(input()); print(f"{c*9/5+32:.1f}")'),
(10,'BEGINNER','分钟换算成小时和分钟','135','2 15','60','1 0','# 请在这里完成程序\n','m=int(input()); print(m//60,m%60)'),
(11,'BEGINNER','秒数换算成时分秒','3661','1:1:1','7322','2:2:2','# 请在这里完成程序\n','s=int(input()); print(f"{s//3600}:{s//60%60}:{s%60}")'),
(12,'BEGINNER','三个数的平均数','3 4 8','5.0','10 20 30','20.0','# 请在这里完成程序\n','a=list(map(int,input().split())); print(f"{sum(a)/len(a):.1f}")'),
(13,'BEGINNER','三角形周长','3 4 5','12','5 5 6','16','# 请在这里完成程序\n','a=list(map(int,input().split())); print(sum(a))'),
(14,'BEGINNER','分数转小数','125','1.25','50','0.5','# 请在这里完成程序\n','n=int(input()); print(f"{n/100:.2f}".rstrip("0").rstrip("."))'),
(15,'BEGINNER','两位数倒序','37','73','80','8','# 请在这里完成程序\n','n=input().strip(); print(int(n[::-1]))'),
(16,'BEGINNER','一个数的平方和立方','4','16 64','3','9 27','# 请在这里完成程序\n','n=int(input()); print(n*n,n*n*n)'),
(17,'BEGINNER','时钟加分钟','23 50','0 20','8 15','8 45','# 请在这里完成程序\n','h,m=map(int,input().split()); t=(h*60+m+30)%1440; print(t//60,t%60)'),
(18,'BEGINNER','计算 BMI','45 1.5','20.0','72 1.8','22.2','# 请在这里完成程序\n','w,h=map(float,input().split()); print(f"{w/(h*h):.1f}")'),
(19,'BEGINNER','计算简单利息后的金额','1000 3 2','1060.00','500 5 1','525.00','# 请在这里完成程序\n','p,r,y=map(float,input().split()); print(f"{p*(1+r*y/100):.2f}")'),
(20,'BEGINNER','苹果总数','3 4','12','5 6','30','# 请在这里完成程序\n','a,b=map(int,input().split()); print(a*b)'),
(21,'SYNTAX','判断奇偶','7','奇数','12','偶数','# 请在这里完成程序\n','n=int(input()); print("偶数" if n%2==0 else "奇数")'),
(22,'SYNTAX','求两个数的较大值','3 9','9','10 2','10','# 请在这里完成程序\n','a,b=map(int,input().split()); print(max(a,b))'),
(23,'SYNTAX','求三个数的最大值','4 8 6','8','-1 -5 -2','-1','# 请在这里完成程序\n','a=list(map(int,input().split())); print(max(a))'),
(24,'SYNTAX','判断正数负数或零','-3','负数','0','零','# 请在这里完成程序\n','n=int(input()); print("正数" if n>0 else ("负数" if n<0 else "零"))'),
(25,'SYNTAX','按分数输出等级','86','优秀','72','良好','# 请在这里完成程序\n','s=int(input()); print("优秀" if s>=85 else ("良好" if s>=70 else ("及格" if s>=60 else "不及格")) )'),
(26,'SYNTAX','判断闰年','2024','是','1900','否','# 请在这里完成程序\n','y=int(input()); print("是" if y%400==0 or (y%4==0 and y%100!=0) else "否")'),
(27,'SYNTAX','判断是否整除','15 3','是','14 3','否','# 请在这里完成程序\n','a,b=map(int,input().split()); print("是" if a%b==0 else "否")'),
(28,'SYNTAX','计算 1 到 n 的和','10','55','100','5050','# 请在这里完成程序\n','n=int(input()); print(n*(n+1)//2)'),
(29,'SYNTAX','计算 1 到 n 的偶数和','10','30','20','110','# 请在这里完成程序\n','n=int(input()); print(sum(range(2,n+1,2)))'),
(30,'SYNTAX','计算阶乘','5','120','0','1','# 请在这里完成程序\n','n=int(input()); r=1
for i in range(2,n+1): r*=i
print(r)'),
(31,'SYNTAX','统计整数位数','12345','5','-80','2','# 请在这里完成程序\n','s=input().strip().lstrip("-"); print(len(s))'),
(32,'SYNTAX','倒序输出字符串','abcde','edcba','Python','nohtyP','# 请在这里完成程序\n','print(input().strip()[::-1])'),
(33,'SYNTAX','判断回文字符串','level','是','python','否','# 请在这里完成程序\n','s=input().strip(); print("是" if s==s[::-1] else "否")'),
(34,'SYNTAX','统计字母 a 的个数','banana','3','apple','1','# 请在这里完成程序\n','print(input().strip().count("a"))'),
(35,'SYNTAX','把字符串变成大写','hello','HELLO','InfoTech','INFOTECH','# 请在这里完成程序\n','print(input().strip().upper())'),
(36,'SYNTAX','删除字符串中的空格','a b c','abc','hello world','helloworld','# 请在这里完成程序\n','print(input().replace(" ",""))'),
(37,'SYNTAX','统计指定字符出现次数','banana a','3','hello l','2','# 请在这里完成程序\n','s,ch=input().split(); print(s.count(ch))'),
(38,'SYNTAX','列表最大值','3 8 2 9','9','-1 -5 -2','-1','# 请在这里完成程序\n','a=list(map(int,input().split())); print(max(a))'),
(39,'SYNTAX','列表最小值','3 8 2 9','2','-1 -5 -2','-5','# 请在这里完成程序\n','a=list(map(int,input().split())); print(min(a))'),
(40,'SYNTAX','列表平均数','2 4 6 8','5.0','1 2 3','2.0','# 请在这里完成程序\n','a=list(map(int,input().split())); print(f"{sum(a)/len(a):.1f}")'),
(41,'LIST','统计列表中的偶数','1 2 3 4 5 6 7 8','4','2 4 6','3','# 请在这里完成程序\n','a=list(map(int,input().split())); print(sum(x%2==0 for x in a))'),
(42,'LIST','倒序输出列表','1 2 3','3 2 1','5 6','6 5','# 请在这里完成程序\n','print(*input().split()[::-1])'),
(43,'LIST','升序排列列表','4 1 3 2','1 2 3 4','9 7 8','7 8 9','# 请在这里完成程序\n','print(*sorted(map(int,input().split())))'),
(44,'LIST','列表去重并保持顺序','1 2 1 3 2','1 2 3','4 4 5 4','4 5','# 请在这里完成程序\n','a=input().split(); print(*dict.fromkeys(a))'),
(45,'LIST','输出相邻元素差','1 4 9','3 5','2 5 10 14','3 5 4','# 请在这里完成程序\n','a=list(map(int,input().split())); print(*[a[i+1]-a[i] for i in range(len(a)-1)])'),
(46,'LIST','斐波那契数列第 n 项','8','21','10','55','# 请在这里完成程序\n','n=int(input()); a,b=0,1
for _ in range(n): a,b=b,a+b
print(a)'),
(47,'LIST','求最大公约数','18 24','6','35 49','7','# 请在这里完成程序\n','import math
a,b=map(int,input().split()); print(math.gcd(a,b))'),
(48,'LIST','判断素数','29','是','30','否','# 请在这里完成程序\n','n=int(input()); print("是" if n>1 and all(n%i for i in range(2,int(n**0.5)+1)) else "否")'),
(49,'LIST','输出乘法表一行','3','3 6 9 12 15 18 21 24 27','5','5 10 15 20 25 30 35 40 45','# 请在这里完成程序\n','n=int(input()); print(*[n*i for i in range(1,10)])'),
(50,'LIST','统计单词个数','I love Python','3','This is a test','4','# 请在这里完成程序\n','print(len(input().split()))'),
(51,'LIST','找出最长单词长度','red blue green','5','one three seven','5','# 请在这里完成程序\n','print(max(map(len,input().split())))'),
(52,'LIST','替换句子中的单词','I like cat|cat|dog','I like dog','we like tea|tea|code','we like code','# 请在这里完成程序\n','s,a,b=input().split("|"); print(s.replace(a,b))'),
(53,'LIST','逗号分隔数字求和','1,2,3,4','10','10,20','30','# 请在这里完成程序\n','print(sum(map(int,input().split(","))))'),
(54,'LIST','三个数的中位数','1 3 5','3','9 2 6','6','# 请在这里完成程序\n','a=sorted(map(int,input().split())); print(a[1])'),
(55,'LIST','列表循环左移一位','1 2 3 4','2 3 4 1','a b c','b c a','# 请在这里完成程序\n','a=input().split(); print(*(a[1:]+a[:1]))'),
(56,'LIST','二维列表元素和','1 2\n3 4','10','5 6\n7 8','26','# 请在这里完成程序\n','a=[list(map(int,input().split())) for _ in range(2)]; print(sum(map(sum,a)))'),
(57,'LIST','二维列表主对角线和','1 2\n3 4','5','5 1\n2 6','11','# 请在这里完成程序\n','a=[list(map(int,input().split())) for _ in range(2)]; print(a[0][0]+a[1][1])'),
(58,'LIST','转置一个 2×2 矩阵','1 2\n3 4','1 3\n2 4','5 6\n7 8','5 7\n6 8','# 请在这里完成程序\n','a=[input().split() for _ in range(2)]; print(*a[0][0:1],*a[1][0:1]); print(*a[0][1:2],*a[1][1:2])'),
(59,'LIST','统计出现次数最多的颜色','red red blue','red','a b b c','b','# 请在这里完成程序\n','a=input().split(); print(max(set(a),key=a.count))'),
(60,'LIST','简单游程编码','aaabbc','a3 b2 c1','xxxyy','x3 y2','# 请在这里完成程序\n','s=input().strip(); r=[]; i=0
while i<len(s):
 j=i
 while j<len(s) and s[j]==s[i]: j+=1
 r.append(s[i]+str(j-i)); i=j
print(*r)'),
(61,'ADVANCED','函数计算平方','7','49','-4','16','# 请在这里完成程序\n','def square(n): return n*n
print(square(int(input())))'),
(62,'ADVANCED','函数计算幂','2 5','32','3 3','27','# 请在这里完成程序\n','def power(a,b): return a**b
a,b=map(int,input().split()); print(power(a,b))'),
(63,'ADVANCED','字典查找水果价格','apple\napple 3\nbanana 5','3','banana\napple 3\nbanana 5','5','# 请在这里完成程序\n','name=input().strip(); d={}
for _ in range(2):
 k,v=input().split(); d[k]=v
print(d[name])'),
(64,'ADVANCED','字典数值求和','a 2\nb 3\nc 4','9','x 10\ny 20','30','# 请在这里完成程序\n','import sys
d={};
for line in sys.stdin:
 k,v=line.split(); d[k]=int(v)
print(sum(d.values()))'),
(65,'ADVANCED','成绩字典求平均','a 80\nb 90\nc 70','80.0','a 60\nb 90','75.0','# 请在这里完成程序\n','import sys
x=[int(line.split()[1]) for line in sys.stdin]; print(f"{sum(x)/len(x):.1f}")'),
(66,'ADVANCED','展开二维列表','1 2\n3 4','1 2 3 4','a b\nc d','a b c d','# 请在这里完成程序\n','a=[input().split() for _ in range(2)]; print(*[x for row in a for x in row])'),
(67,'ADVANCED','输出每行的和','1 2\n3 4','3 7','5 6\n7 8','11 15','# 请在这里完成程序\n','a=[list(map(int,input().split())) for _ in range(2)]; print(*map(sum,a))'),
(68,'ADVANCED','输出每列的和','1 2\n3 4','4 6','5 6\n7 8','12 14','# 请在这里完成程序\n','a=[list(map(int,input().split())) for _ in range(2)]; print(sum(x[0] for x in a),sum(x[1] for x in a))'),
(69,'ADVANCED','输出杨辉三角指定行','4','1 3 3 1','5','1 4 6 4 1','# 请在这里完成程序\n','import math
n=int(input())-1; print(*[math.comb(n,k) for k in range(n+1)])'),
(70,'ADVANCED','计算两点距离','0 0 3 4','5.0','1 1 4 5','5.0','# 请在这里完成程序\n','import math
x1,y1,x2,y2=map(float,input().split()); print(f"{math.hypot(x2-x1,y2-y1):.1f}")'),
(71,'ADVANCED','模拟存钱','100 10 3','130','500 20 4','580','# 请在这里完成程序\n','a,b,n=map(int,input().split()); print(a+b*n)'),
(72,'ADVANCED','倒计时输出','5','5 4 3 2 1','3','3 2 1','# 请在这里完成程序\n','n=int(input()); print(*range(n,0,-1))'),
(73,'ADVANCED','队列按顺序输出','1 2 3','1 2 3','a b c','a b c','# 请在这里完成程序\n','print(*input().split())'),
(74,'ADVANCED','字母循环右移','abc 2','cde','xyz 1','yza','# 请在这里完成程序\n','s,k=input().split(); k=int(k); print("".join(chr((ord(c)-97+k)%26+97) for c in s))'),
(75,'ADVANCED','判断两个单词是否为字母异位词','listen silent','是','hello world','否','# 请在这里完成程序\n','a,b=input().split(); print("是" if sorted(a)==sorted(b) else "否")'),
(76,'ADVANCED','求两组数字交集','1 2 3\n2 3 4','2 3','a b c\nb c d','b c','# 请在这里完成程序\n','a=set(input().split()); b=set(input().split()); print(*sorted(a&b,key=lambda x:(not x.isdigit(),x)))'),
(77,'ADVANCED','字典反向索引','a 1\nb 2','1:a 2:b','x 3\ny 4','3:x 4:y','# 请在这里完成程序\n','import sys
d={};
for line in sys.stdin:
 k,v=line.split(); d[v]=k
print(*[f"{k}:{d[k]}" for k in sorted(d)])'),
(78,'ADVANCED','按分数给名单排序','a 3\nb 1\nc 2','b 1\nc 2\na 3','x 9\ny 7','y 7\nx 9','# 请在这里完成程序\n','import sys; a=[line.split() for line in sys.stdin]; print(*[" ".join(x) for x in sorted(a,key=lambda x:int(x[1]))],sep="\\n")'),
(79,'ADVANCED','找零硬币','13','10 2 1','28','10 10 5 2 1','# 请在这里完成程序\n','n=int(input()); r=[]
for c in (10,5,2,1):
 while n>=c: r.append(c); n-=c
print(*r)'),
(80,'ADVANCED','两门课程总分','math 80\npython 90','170','art 70\nscience 85','155','# 请在这里完成程序\n','a=int(input().split()[1]); b=int(input().split()[1]); print(a+b)');
INSERT INTO biz_question (question_type,question_content,grade,semester,lesson_num,answer,analysis,practical_mode,is_public,creator_id,create_by,create_time,update_by,update_time)
SELECT 'practical',CONCAT('[Python系统题V1-',LPAD(t.seq,3,'0'),'] ',t.title,'。请根据输入说明编写程序并输出结果。'),7,'0',t.seq,t.reference_code,CONCAT('阶段：',t.stage,'；系统题库 V1。'),'PYTHON','1',1,'python-system-v1',NOW(),'python-system-v1',NOW()
FROM tmp_python_system_question t LEFT JOIN biz_question q ON q.create_by='python-system-v1' AND q.question_content LIKE CONCAT('[Python系统题V1-',LPAD(t.seq,3,'0'),']%') WHERE q.question_id IS NULL;
UPDATE biz_question q JOIN tmp_python_system_question t ON q.create_by='python-system-v1' AND q.question_content LIKE CONCAT('[Python系统题V1-',LPAD(t.seq,3,'0'),']%') SET q.answer=t.reference_code,q.update_by='python-system-v1',q.update_time=NOW();
INSERT INTO biz_programming_question_config (question_id,language_code,starter_code,input_description,output_description,sample_explanation,constraints_text,notes_text,time_limit_seconds,memory_limit_kb,max_processes,max_file_size_kb,max_output_kb,enabled,create_by,create_time,update_by,update_time)
SELECT q.question_id,'python',t.starter_code,'输入按题面给出；无输入题请直接运行。','按样例格式输出，行末不要添加多余说明。','公开样例用于自测，隐藏样例用于正式判题。','只使用 Python 标准语法，不使用第三方库。','输出应与期望结果完全一致。',2.00,131072,8,1024,64,'1','python-system-v1',NOW(),'python-system-v1',NOW() FROM tmp_python_system_question t JOIN biz_question q ON q.create_by='python-system-v1' AND q.question_content LIKE CONCAT('[Python系统题V1-',LPAD(t.seq,3,'0'),']%') ON DUPLICATE KEY UPDATE starter_code=VALUES(starter_code),input_description=VALUES(input_description),output_description=VALUES(output_description),sample_explanation=VALUES(sample_explanation),constraints_text=VALUES(constraints_text),notes_text=VALUES(notes_text),enabled='1',update_time=NOW();
DELETE c FROM biz_programming_test_case c JOIN biz_question q ON q.question_id=c.question_id AND q.create_by='python-system-v1';
INSERT INTO biz_programming_test_case (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
SELECT q.question_id,'公开样例',t.input1,t.output1,'1',40.00,1,'python-system-v1',NOW(),'python-system-v1',NOW() FROM tmp_python_system_question t JOIN biz_question q ON q.create_by='python-system-v1' AND q.question_content LIKE CONCAT('[Python系统题V1-',LPAD(t.seq,3,'0'),']%');
INSERT INTO biz_programming_test_case (question_id,case_name,input_text,expected_output,is_public,score_weight,order_num,create_by,create_time,update_by,update_time)
SELECT q.question_id,'隐藏样例',t.input2,t.output2,'0',60.00,2,'python-system-v1',NOW(),'python-system-v1',NOW() FROM tmp_python_system_question t JOIN biz_question q ON q.create_by='python-system-v1' AND q.question_content LIKE CONCAT('[Python系统题V1-',LPAD(t.seq,3,'0'),']%');
SELECT COUNT(*) AS system_question_count FROM biz_question WHERE create_by='python-system-v1' AND question_type='practical' AND practical_mode='PYTHON';
SELECT COUNT(*) AS config_count FROM biz_programming_question_config c JOIN biz_question q ON q.question_id=c.question_id WHERE q.create_by='python-system-v1' AND c.enabled='1';
SELECT COUNT(*) AS test_case_count, COUNT(DISTINCT c.question_id) AS covered_question_count FROM biz_programming_test_case c JOIN biz_question q ON q.question_id=c.question_id WHERE q.create_by='python-system-v1';
