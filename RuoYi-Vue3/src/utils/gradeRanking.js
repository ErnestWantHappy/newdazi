// 等级与赋分的排名口径：等级按课程总分合计，赋分按平均分；与成绩页面同源
// 页面表格和导出 Excel 都调用这里，避免页面 A 等级、导出 B 等级。
export function buildGradeMaps(students, ratios, getRankTotal) {
  const grades = new Map();
  const scores = new Map();
  const byTotal = students.filter(s => getRankTotal(s) > 0).sort((a, b) => getRankTotal(b) - getRankTotal(a));
  const total = byTotal.length;
  if (total === 0) return { grades, scores };
  const excellent = Math.ceil(total * (ratios.excellent ?? 25) / 100);
  const good = Math.ceil(total * (ratios.good ?? 40) / 100);
  const pass = Math.ceil(total * (ratios.pass ?? 30) / 100);
  byTotal.forEach((student, index) => {
    grades.set(student.studentId, index < excellent ? '优秀'
      : index < excellent + good ? '良好'
        : index < excellent + good + pass ? '及格' : '不及格');
  });
  if (total === 1) {
    scores.set(byTotal[0].studentId, 100);
    return { grades, scores };
  }
  // 并列总分名次相同、赋分相同
  let rank = 0;
  let prev = null;
  byTotal.forEach((student, index) => {
    const current = getRankTotal(student);
    if (prev === null || current !== prev) rank = index;
    prev = current;
    scores.set(student.studentId, Math.round(100 - (rank / (total - 1)) * 45));
  });
  return { grades, scores };
}
