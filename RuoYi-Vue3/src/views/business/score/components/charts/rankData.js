// 无成绩学生保留在名单末尾；空值只用于画占位，不作为真实零分。
export function buildRankData(students) {
  return students.map(student => ({
    ...student,
    chartScore: student.finalTotal ?? student.filteredTotal ?? null
  })).sort((a, b) => (b.chartScore ?? -Infinity) - (a.chartScore ?? -Infinity));
}
