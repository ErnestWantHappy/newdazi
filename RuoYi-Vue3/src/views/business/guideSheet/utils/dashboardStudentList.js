function statusRank(row) {
  if (row?.isSubmitted === 'Y') return 2
  return row?.currentPage ? 1 : 0
}

function numberValue(value) {
  const parsed = Number.parseInt(value, 10)
  return Number.isFinite(parsed) ? parsed : Number.MAX_SAFE_INTEGER
}

export function sortDashboardStudents(rows) {
  return [...(rows || [])].sort((left, right) => {
    const classDifference = numberValue(left?.classCode) - numberValue(right?.classCode)
    if (classDifference !== 0) return classDifference

    const statusDifference = statusRank(right) - statusRank(left)
    if (statusDifference !== 0) return statusDifference

    const studentNoDifference = numberValue(left?.studentNo) - numberValue(right?.studentNo)
    if (studentNoDifference !== 0) return studentNoDifference
    return numberValue(left?.studentId) - numberValue(right?.studentId)
  })
}
