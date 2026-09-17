-- 修复 Python 系统题在历史导入时被错误字符集替换成 ? 的题面和配置文本。
-- 执行前请备份目标库；仅更新 create_by=python-system-v1 且题号 1755-1834 的系统题。
SET NAMES utf8mb4;

UPDATE biz_question
SET question_content = CASE question_id
  WHEN 1755 THEN '[Python系统题V1-001] 输出 Hello Python。请根据输入说明编写程序并输出结果。'
  WHEN 1756 THEN '[Python系统题V1-002] 输出一句信息科技口号。请根据输入说明编写程序并输出结果。'
  WHEN 1757 THEN '[Python系统题V1-003] 输出当前练习年份。请根据输入说明编写程序并输出结果。'
  WHEN 1758 THEN '[Python系统题V1-004] 向同学问好。请根据输入说明编写程序并输出结果。'
  WHEN 1759 THEN '[Python系统题V1-005] 求下一个整数。请根据输入说明编写程序并输出结果。'
  WHEN 1760 THEN '[Python系统题V1-006] 两个数求和。请根据输入说明编写程序并输出结果。'
  WHEN 1761 THEN '[Python系统题V1-007] 长方形面积。请根据输入说明编写程序并输出结果。'
  WHEN 1762 THEN '[Python系统题V1-008] 长方形周长。请根据输入说明编写程序并输出结果。'
  WHEN 1763 THEN '[Python系统题V1-009] 摄氏温度转华氏温度。请根据输入说明编写程序并输出结果。'
  WHEN 1764 THEN '[Python系统题V1-010] 分钟换算成小时和分钟。请根据输入说明编写程序并输出结果。'
  WHEN 1765 THEN '[Python系统题V1-011] 秒数换算成时分秒。请根据输入说明编写程序并输出结果。'
  WHEN 1766 THEN '[Python系统题V1-012] 三个数的平均数。请根据输入说明编写程序并输出结果。'
  WHEN 1767 THEN '[Python系统题V1-013] 三角形周长。请根据输入说明编写程序并输出结果。'
  WHEN 1768 THEN '[Python系统题V1-014] 分数转小数。请根据输入说明编写程序并输出结果。'
  WHEN 1769 THEN '[Python系统题V1-015] 两位数倒序。请根据输入说明编写程序并输出结果。'
  WHEN 1770 THEN '[Python系统题V1-016] 一个数的平方和立方。请根据输入说明编写程序并输出结果。'
  WHEN 1771 THEN '[Python系统题V1-017] 时钟加分钟。请根据输入说明编写程序并输出结果。'
  WHEN 1772 THEN '[Python系统题V1-018] 计算 BMI。请根据输入说明编写程序并输出结果。'
  WHEN 1773 THEN '[Python系统题V1-019] 计算简单利息后的金额。请根据输入说明编写程序并输出结果。'
  WHEN 1774 THEN '[Python系统题V1-020] 苹果总数。请根据输入说明编写程序并输出结果。'
  WHEN 1775 THEN '[Python系统题V1-021] 判断奇偶。请根据输入说明编写程序并输出结果。'
  WHEN 1776 THEN '[Python系统题V1-022] 求两个数的较大值。请根据输入说明编写程序并输出结果。'
  WHEN 1777 THEN '[Python系统题V1-023] 求三个数的最大值。请根据输入说明编写程序并输出结果。'
  WHEN 1778 THEN '[Python系统题V1-024] 判断正数负数或零。请根据输入说明编写程序并输出结果。'
  WHEN 1779 THEN '[Python系统题V1-025] 按分数输出等级。请根据输入说明编写程序并输出结果。'
  WHEN 1780 THEN '[Python系统题V1-026] 判断闰年。请根据输入说明编写程序并输出结果。'
  WHEN 1781 THEN '[Python系统题V1-027] 判断是否整除。请根据输入说明编写程序并输出结果。'
  WHEN 1782 THEN '[Python系统题V1-028] 计算 1 到 n 的和。请根据输入说明编写程序并输出结果。'
  WHEN 1783 THEN '[Python系统题V1-029] 计算 1 到 n 的偶数和。请根据输入说明编写程序并输出结果。'
  WHEN 1784 THEN '[Python系统题V1-030] 计算阶乘。请根据输入说明编写程序并输出结果。'
  WHEN 1785 THEN '[Python系统题V1-031] 统计整数位数。请根据输入说明编写程序并输出结果。'
  WHEN 1786 THEN '[Python系统题V1-032] 倒序输出字符串。请根据输入说明编写程序并输出结果。'
  WHEN 1787 THEN '[Python系统题V1-033] 判断回文字符串。请根据输入说明编写程序并输出结果。'
  WHEN 1788 THEN '[Python系统题V1-034] 统计字母 a 的个数。请根据输入说明编写程序并输出结果。'
  WHEN 1789 THEN '[Python系统题V1-035] 把字符串变成大写。请根据输入说明编写程序并输出结果。'
  WHEN 1790 THEN '[Python系统题V1-036] 删除字符串中的空格。请根据输入说明编写程序并输出结果。'
  WHEN 1791 THEN '[Python系统题V1-037] 统计指定字符出现次数。请根据输入说明编写程序并输出结果。'
  WHEN 1792 THEN '[Python系统题V1-038] 列表最大值。请根据输入说明编写程序并输出结果。'
  WHEN 1793 THEN '[Python系统题V1-039] 列表最小值。请根据输入说明编写程序并输出结果。'
  WHEN 1794 THEN '[Python系统题V1-040] 列表平均数。请根据输入说明编写程序并输出结果。'
  WHEN 1795 THEN '[Python系统题V1-041] 统计列表中的偶数。请根据输入说明编写程序并输出结果。'
  WHEN 1796 THEN '[Python系统题V1-042] 倒序输出列表。请根据输入说明编写程序并输出结果。'
  WHEN 1797 THEN '[Python系统题V1-043] 升序排列列表。请根据输入说明编写程序并输出结果。'
  WHEN 1798 THEN '[Python系统题V1-044] 列表去重并保持顺序。请根据输入说明编写程序并输出结果。'
  WHEN 1799 THEN '[Python系统题V1-045] 输出相邻元素差。请根据输入说明编写程序并输出结果。'
  WHEN 1800 THEN '[Python系统题V1-046] 斐波那契数列第 n 项。请根据输入说明编写程序并输出结果。'
  WHEN 1801 THEN '[Python系统题V1-047] 求最大公约数。请根据输入说明编写程序并输出结果。'
  WHEN 1802 THEN '[Python系统题V1-048] 判断素数。请根据输入说明编写程序并输出结果。'
  WHEN 1803 THEN '[Python系统题V1-049] 输出乘法表一行。请根据输入说明编写程序并输出结果。'
  WHEN 1804 THEN '[Python系统题V1-050] 统计单词个数。请根据输入说明编写程序并输出结果。'
  WHEN 1805 THEN '[Python系统题V1-051] 找出最长单词长度。请根据输入说明编写程序并输出结果。'
  WHEN 1806 THEN '[Python系统题V1-052] 替换句子中的单词。请根据输入说明编写程序并输出结果。'
  WHEN 1807 THEN '[Python系统题V1-053] 逗号分隔数字求和。请根据输入说明编写程序并输出结果。'
  WHEN 1808 THEN '[Python系统题V1-054] 三个数的中位数。请根据输入说明编写程序并输出结果。'
  WHEN 1809 THEN '[Python系统题V1-055] 列表循环左移一位。请根据输入说明编写程序并输出结果。'
  WHEN 1810 THEN '[Python系统题V1-056] 二维列表元素和。请根据输入说明编写程序并输出结果。'
  WHEN 1811 THEN '[Python系统题V1-057] 二维列表主对角线和。请根据输入说明编写程序并输出结果。'
  WHEN 1812 THEN '[Python系统题V1-058] 转置一个 2×2 矩阵。请根据输入说明编写程序并输出结果。'
  WHEN 1813 THEN '[Python系统题V1-059] 统计出现次数最多的颜色。请根据输入说明编写程序并输出结果。'
  WHEN 1814 THEN '[Python系统题V1-060] 简单游程编码。请根据输入说明编写程序并输出结果。'
  WHEN 1815 THEN '[Python系统题V1-061] 函数计算平方。请根据输入说明编写程序并输出结果。'
  WHEN 1816 THEN '[Python系统题V1-062] 函数计算幂。请根据输入说明编写程序并输出结果。'
  WHEN 1817 THEN '[Python系统题V1-063] 字典查找水果价格。请根据输入说明编写程序并输出结果。'
  WHEN 1818 THEN '[Python系统题V1-064] 字典数值求和。请根据输入说明编写程序并输出结果。'
  WHEN 1819 THEN '[Python系统题V1-065] 成绩字典求平均。请根据输入说明编写程序并输出结果。'
  WHEN 1820 THEN '[Python系统题V1-066] 展开二维列表。请根据输入说明编写程序并输出结果。'
  WHEN 1821 THEN '[Python系统题V1-067] 输出每行的和。请根据输入说明编写程序并输出结果。'
  WHEN 1822 THEN '[Python系统题V1-068] 输出每列的和。请根据输入说明编写程序并输出结果。'
  WHEN 1823 THEN '[Python系统题V1-069] 输出杨辉三角指定行。请根据输入说明编写程序并输出结果。'
  WHEN 1824 THEN '[Python系统题V1-070] 计算两点距离。请根据输入说明编写程序并输出结果。'
  WHEN 1825 THEN '[Python系统题V1-071] 模拟存钱。请根据输入说明编写程序并输出结果。'
  WHEN 1826 THEN '[Python系统题V1-072] 倒计时输出。请根据输入说明编写程序并输出结果。'
  WHEN 1827 THEN '[Python系统题V1-073] 队列按顺序输出。请根据输入说明编写程序并输出结果。'
  WHEN 1828 THEN '[Python系统题V1-074] 字母循环右移。请根据输入说明编写程序并输出结果。'
  WHEN 1829 THEN '[Python系统题V1-075] 判断两个单词是否为字母异位词。请根据输入说明编写程序并输出结果。'
  WHEN 1830 THEN '[Python系统题V1-076] 求两组数字交集。请根据输入说明编写程序并输出结果。'
  WHEN 1831 THEN '[Python系统题V1-077] 字典反向索引。请根据输入说明编写程序并输出结果。'
  WHEN 1832 THEN '[Python系统题V1-078] 按分数给名单排序。请根据输入说明编写程序并输出结果。'
  WHEN 1833 THEN '[Python系统题V1-079] 找零硬币。请根据输入说明编写程序并输出结果。'
  WHEN 1834 THEN '[Python系统题V1-080] 两门课程总分。请根据输入说明编写程序并输出结果。'
END,
update_by='python-system-v1', update_time=NOW()
WHERE create_by='python-system-v1' AND question_id BETWEEN 1755 AND 1834;

UPDATE biz_programming_question_config
SET input_description='输入按题面给出；无输入题请直接运行。',
    output_description='按样例格式输出，行末不要添加多余说明。',
    sample_explanation='公开样例用于自测，隐藏样例用于正式判题。',
    constraints_text='只使用 Python 标准语法，不使用第三方库。',
    notes_text='输出应与期望结果完全一致。',
    update_time=NOW()
WHERE question_id BETWEEN 1755 AND 1834;

UPDATE biz_python_practice_question_snapshot s
JOIN biz_question q ON q.question_id=s.question_id AND q.create_by='python-system-v1'
SET s.question_content=q.question_content,
    s.input_description='输入按题面给出；无输入题请直接运行。',
    s.output_description='按样例格式输出，行末不要添加多余说明。',
    s.sample_explanation='公开样例用于自测，隐藏样例用于正式判题。',
    s.constraints_text='只使用 Python 标准语法，不使用第三方库。',
    s.notes_text='输出应与期望结果完全一致。'
WHERE q.question_id BETWEEN 1755 AND 1834;

SELECT COUNT(*) AS repaired_question_count
FROM biz_question
WHERE create_by='python-system-v1' AND question_id BETWEEN 1755 AND 1834 AND question_content NOT LIKE '%?%';
