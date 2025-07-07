/**
 * 根据学生账号和当前日期计算学生当前年级
 * @param {string} studentAccount - 学生账号, 例如 "2022710422"
 * @returns {string} - 计算出的年级描述, 例如 "三年级" 或 "未知年级"
 */
export function calculateGrade(studentAccount) {
  // 检查账号是否合法 (至少包含4位年份)
  if (!studentAccount || typeof studentAccount !== 'string' || studentAccount.length < 4) {
    return "未知年级";
  }

  // 1. 从账号中提取入学年份
  const entryYear = parseInt(studentAccount.substring(0, 4));
  if (isNaN(entryYear)) {
    return "未知年级";
  }

  // 2. 获取当前的真实年份和月份
  const currentDate = new Date();
  const currentYear = currentDate.getFullYear();
  const currentMonth = currentDate.getMonth() + 1; // getMonth() 返回 0-11, 所以要 +1

  // 3. 根据9月开学季规则计算年级
  // 核心逻辑：新学年是从每年的9月份开始的
  let grade = currentYear - entryYear;
  if (currentMonth >= 9) {
    // 如果当前是9月或之后, 年级 = 当前年份 - 入学年份 + 1
    grade = grade + 1;
  }
  // 如果当前是9月之前 (1-8月), 年级 = 当前年份 - 入学年份
  // grade 的值已经正确, 无需操作

  // 4. 将数字年级转换为中文描述
  const gradeMap = {
    1: '一年级',
    2: '二年级',
    3: '三年级',
    4: '四年级',
    5: '五年级',
    6: '六年级',
    7: '七年级',
    8: '八年级',
    9: '九年级',
    10: '高一',
    11: '高二',
    12: '高三',
  };

  // 如果计算出的年级超过12, 或者小于1, 则可能已毕业或未入学
  if (grade > 12 || grade < 1) {
    return '已毕业或未入学';
  }

  return gradeMap[grade] || "未知年级";
}