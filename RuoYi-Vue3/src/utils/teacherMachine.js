const TEACHER_IP_KEY = 'current_teacher_machine_ip'
const TEACHER_PORT = '5000'

export function setTeacherMachineIp(ip) {
  localStorage.setItem(TEACHER_IP_KEY, ip)
}

export function getTeacherMachineIp() {
  return localStorage.getItem(TEACHER_IP_KEY) || ''
}

export function getUploadUrl() {
  const ip = getTeacherMachineIp()
  if (!ip) return ''
  return `http://${ip}:${TEACHER_PORT}/upload`
}

export function getWorksUrl(filename) {
  const ip = getTeacherMachineIp()
  if (!ip) return ''
  return `http://${ip}:${TEACHER_PORT}/works/${filename}`
}

export function getListUrl(classCode) {
  const ip = getTeacherMachineIp()
  if (!ip) return ''
  return `http://${ip}:${TEACHER_PORT}/list?class_code=${encodeURIComponent(classCode || '')}`
}
