const TEACHER_IP_KEY = 'current_teacher_machine_ip'
const TEACHER_PORT_KEY = 'current_teacher_machine_port'
const TEACHER_ENABLED_KEY = 'current_teacher_machine_enabled'

export function setTeacherMachineConfig(ip, port = 5000, enabled = false) {
  localStorage.setItem(TEACHER_IP_KEY, ip)
  localStorage.setItem(TEACHER_PORT_KEY, String(port))
  localStorage.setItem(TEACHER_ENABLED_KEY, enabled ? '1' : '0')
}

export function getTeacherMachineIp() {
  return localStorage.getItem(TEACHER_IP_KEY) || ''
}

export function isTeacherMachineEnabled() {
  return localStorage.getItem(TEACHER_ENABLED_KEY) === '1'
}

export function getTeacherMachinePort() {
  return localStorage.getItem(TEACHER_PORT_KEY) || '5000'
}

export function getUploadUrl() {
  const ip = getTeacherMachineIp()
  if (!ip || !isTeacherMachineEnabled()) return ''
  return `http://${ip}:${getTeacherMachinePort()}/upload`
}

export function getWorksUrl(filename) {
  const ip = getTeacherMachineIp()
  if (!ip || !isTeacherMachineEnabled()) return ''
  return `http://${ip}:${getTeacherMachinePort()}/works/${filename}`
}

export function getListUrl(classCode) {
  const ip = getTeacherMachineIp()
  if (!ip || !isTeacherMachineEnabled()) return ''
  return `http://${ip}:${getTeacherMachinePort()}/list?class_code=${encodeURIComponent(classCode || '')}`
}
