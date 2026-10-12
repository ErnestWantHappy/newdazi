import Cookies from 'js-cookie'

const TokenKey = 'Admin-Token'
const SchoolSelectionKey = 'School-Selection-Pending'

export function needsSchoolSelection() {
  return Cookies.get(SchoolSelectionKey) === '1'
}

export function setSchoolSelectionPending(pending) {
  if (pending) Cookies.set(SchoolSelectionKey, '1')
  else Cookies.remove(SchoolSelectionKey)
}

export function getToken() {
  return Cookies.get(TokenKey)
}

export function setToken(token) {
  return Cookies.set(TokenKey, token)
}

export function removeToken() {
  setSchoolSelectionPending(false)
  return Cookies.remove(TokenKey)
}
