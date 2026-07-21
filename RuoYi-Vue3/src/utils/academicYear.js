/**
 * 全平台统一在 7 月 20 日（含当天）切换新学年。
 * 日期按浏览器本地时区解释，避免 UTC 换算导致边界日提前或延后。
 */
export function resolveAcademicStartYear(date = new Date()) {
  const target = normalizeDate(date)
  const year = target.getFullYear()
  const month = target.getMonth() + 1
  const day = target.getDate()
  return month > 7 || (month === 7 && day >= 20) ? year : year - 1
}

/** 根据入学年份计算当前在本学段内的第几年。 */
export function calculateYearsInSection(entryYear, date = new Date()) {
  const normalizedEntryYear = Number.parseInt(String(entryYear), 10)
  if (!Number.isInteger(normalizedEntryYear)) return null
  return resolveAcademicStartYear(date) - normalizedEntryYear + 1
}

/**
 * 根据入学年份和学段计算平台年级编号：小学 1-6、初中 7-9、高中 10-12。
 */
export function calculateGradeNumber(entryYear, schoolType = '1', date = new Date()) {
  const yearsInSection = calculateYearsInSection(entryYear, date)
  const config = {
    '1': { offset: 0, length: 6 },
    '2': { offset: 6, length: 3 },
    '3': { offset: 9, length: 3 }
  }[String(schoolType)]
  if (!config || yearsInSection == null || yearsInSection < 1 || yearsInSection > config.length) {
    return null
  }
  return config.offset + yearsInSection
}

/** 根据平台年级编号反推出当前学年的入学年份。 */
export function calculateEntryYearFromGrade(grade, date = new Date()) {
  const normalizedGrade = Number.parseInt(String(grade), 10)
  let gradeInSection
  if (normalizedGrade >= 1 && normalizedGrade <= 6) {
    gradeInSection = normalizedGrade
  } else if (normalizedGrade >= 7 && normalizedGrade <= 9) {
    gradeInSection = normalizedGrade - 6
  } else if (normalizedGrade >= 10 && normalizedGrade <= 12) {
    gradeInSection = normalizedGrade - 9
  } else {
    return null
  }
  return String(resolveAcademicStartYear(date) - gradeInSection + 1)
}

/** 生成一个平台学年的查询范围，截止日为下一年 7 月 19 日。 */
export function createAcademicYearOption(startYear) {
  const year = Number.parseInt(String(startYear), 10)
  if (!Number.isInteger(year)) return null
  return {
    value: String(year),
    label: `${year}-${year + 1} 学年`,
    start: `${year}-07-20`,
    end: `${year + 1}-07-19`
  }
}

function normalizeDate(value) {
  const date = value instanceof Date ? value : new Date(value)
  if (Number.isNaN(date.getTime())) {
    throw new TypeError('无效日期')
  }
  return date
}
