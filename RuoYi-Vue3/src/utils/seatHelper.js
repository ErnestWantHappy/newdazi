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
export const seatStateLabels = { NORMAL: '常用机位', CHANGED: '异机登录', IP_MISMATCH: '异机登录', UNBOUND: '尚未确认常用机位', UNIDENTIFIED: '未连助手', OTHER_ROOM: '当前在其他机房', SHARED_DEVICE: '同机多账号，请核对' };

// 自动登记的指纹名称仅用于内部识别，不作为课堂中的机位名称展示。
export function displaySeatName(name) {
  const value = String(name || '').trim();
  return /^电脑-[a-f0-9]{12}$/i.test(value) ? '' : value;
}

export function classroomIp(student, observation) {
  // 连接地址可能经过学校出口转换，助手上报的地址才是电脑内网地址。
  if (observation?.localIp) return { value: observation.localIp, source: '内网 IP' };
  return { value: '', source: '内网 IP' };
}

// 回环认领协议自 0.2.3 起稳定；只比较最低版本，助手小版本升级不应让网页认不出已安装的助手。
const MIN_HELPER_VERSION = [0, 2, 3];
// 只接受纯数字点分版本；"0.2.3a"、"0.2.3-beta" 这类自报名不能靠 parseInt 前缀混过版本门槛。
const HELPER_VERSION_PATTERN = /^\d+(?:\.\d+)+$/;
export function helperVersionAtLeast(version, min = MIN_HELPER_VERSION) {
  const text = String(version ?? '').trim();
  if (!HELPER_VERSION_PATTERN.test(text)) return false;
  const parts = text.split('.').map(part => Number(part));
  for (let i = 0; i < min.length; i++) {
    const value = parts[i] ?? 0;
    if (value !== min[i]) return value > min[i];
  }
  return true;
}

export async function probeLocalSeatHelper(fetcher = fetch) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), 800);
  try {
    const response = await fetcher('http://127.0.0.1:28123/identity', { signal: controller.signal, cache: 'no-store', credentials: 'omit' });
    if (!response.ok) return null;
    const data = await response.json();
    return data.installed === true && helperVersionAtLeast(data.version) ? data : null;
  } catch { return null; }
  finally { clearTimeout(timer); }
}
