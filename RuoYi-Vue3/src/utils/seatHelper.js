// 只暂存服务端签发的短期单次凭证，浏览器字段不作为设备身份。
const pendingKey = 'seat-helper-pending';
export function clearSeatHelperToken() {
  try { sessionStorage.removeItem(pendingKey); } catch { }
}
export function captureSeatHelperToken(hash = window.location.hash) {
  const match = /^#seat-helper=([a-f0-9]{64})$/.exec(hash);
  if (!match) return;
  try { sessionStorage.setItem(pendingKey, JSON.stringify({ token: match[1], at: Date.now() })); } catch { }
  window.history.replaceState(window.history.state, '', window.location.pathname + window.location.search);
  window.dispatchEvent?.(new Event('seat-helper-pending'));
}
export function takeSeatHelperToken() {
  try {
    const data = JSON.parse(sessionStorage.getItem(pendingKey) || 'null');
    sessionStorage.removeItem(pendingKey);
    return data && Date.now() - data.at < 300000 ? data.token : null;
  } catch { return null; }
}
export const seatStateLabels = { NORMAL: '常用机位', CHANGED: '机位有变化', UNBOUND: '尚未确认常用机位', UNIDENTIFIED: '未关联或信息已过期', OTHER_ROOM: '当前在其他机房', SHARED_DEVICE: '同机多账号，请核对' };

// 自动登记的指纹名称仅用于内部识别，不作为课堂中的机位名称展示。
export function displaySeatName(name) {
  const value = String(name || '').trim();
  return /^电脑-[a-f0-9]{12}$/i.test(value) ? '' : value;
}

export function classroomIp(student, observation) {
  // 连接地址可能经过学校出口转换，助手上报的地址才是电脑内网地址。
  if (observation?.localIp) return { value: observation.localIp, source: '内网 IP' };
  if (student.connectionIp) return { value: student.connectionIp, source: '连接 IP' };
  return { value: '', source: '内网 IP' };
}
