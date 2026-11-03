import { onBeforeUnmount, provide, ref, watch } from 'vue'
import request from '@/utils/request'
import useUserStore from '@/store/modules/user'
import { takeSeatHelperToken, probeLocalSeatHelper } from '@/utils/seatHelper'

// 机房管控软件常拦截进程归属查询，助手可能长期回报“无法识别浏览器”。
// 连续失败若干轮后改成可操作的提示并降频，避免学生一直看到“正在重试”。
const UNIDENTIFIED_ALERT_AFTER = 5

export function provideStudentSeatSession() {
  const user = useUserStore()
  const status = ref({ enabled: false }), message = ref(''), probing = ref(false), localRunning = ref(false)
  let generation = 0, timer, disposed = false, unidentifiedStreak = 0
  provide('student-seat-helper', { status, message, probing, localRunning })
  async function start([id, token]) {
    const ticket = ++generation
    clearTimeout(timer)
    unidentifiedStreak = 0
    status.value = { enabled: false }; message.value = ''; localRunning.value = false; probing.value = !!id && !!token
    if (!id || !token) return
    const active = () => !disposed && generation === ticket && user.id === id && user.token === token
    const seatRequest = config => request({ ...config, silentError: true, timeout: 5000, headers: { isToken: false, Authorization: `Bearer ${token}` } })
    const association = takeSeatHelperToken()
    if (association) {
      try { await seatRequest({ url: '/business/seat-helper/student/associate', method: 'post', data: { token: association } }) }
      catch { if (active()) message.value = '机位关联未完成，答题不受影响；可再次打开助手自动关联。' }
    }
    async function poll() {
      if (!active()) return
      let unidentified = false
      try {
        const identity = await probeLocalSeatHelper()
        if (!active()) return
        localRunning.value = !!identity
        if (!identity) status.value = { ...status.value, associated: false, localIp: null }
        unidentified = !!identity?.browserUnidentified
        if (unidentified) unidentifiedStreak += 1
        else unidentifiedStreak = 0
        let response = await seatRequest({ url: '/business/seat-helper/student/status' })
        if (!active()) return
        if (!response.data?.enabled) {
          status.value = { enabled: false }; localRunning.value = false; message.value = ''; return
        }
        if (identity?.token) {
          response = await seatRequest({ url: '/business/seat-helper/student/claim-local', method: 'post', data: { token: identity.token } })
          if (!active()) return
        }
        status.value = response.data || { enabled: false }
        // URL关联不能证明助手仍在运行，本地探测失败后不展示历史IP。
        if (!identity) status.value = { ...status.value, associated: false, localIp: null }
        if (unidentified) {
          message.value = unidentifiedStreak >= UNIDENTIFIED_ALERT_AFTER
            ? '助手已运行，但持续无法识别浏览器；答题不受影响，可请老师人工确认机位'
            : '助手已运行，暂无法识别浏览器，正在重试'
        } else if (status.value.browserDeferred) message.value = status.value.preferredBrowser ? '当前机位优先关联默认浏览器中的账号' : '当前机位已由另一浏览器关联，退出后将自动接替'
        else if (status.value.associated) message.value = ''
      } catch {
        if (active()) { message.value = '机位信息暂不可用，答题可以继续。' }
      } finally {
        // 保留短时失效规则；布局内切到 Python、物联或协作仍持续证明当前网页会话。
        if (active()) {
          probing.value = false
          // 已被本次账号更新登录取代的旧页面不再高频取票和刷机位。
          const delay = status.value.enabled === false && !message.value ? 30000
            : status.value.superseded ? 30000
            : unidentified && unidentifiedStreak >= UNIDENTIFIED_ALERT_AFTER ? 15000
            : status.value.associated || status.value.browserDeferred ? 10000
            : 2000
          timer = setTimeout(poll, delay)
        }
      }
    }
    await poll()
  }
  watch(() => [user.id, user.token], start, { immediate: true })
  const link = () => { if (user.id && user.token) start([user.id, user.token]) }
  window.addEventListener('seat-helper-pending', link)
  onBeforeUnmount(() => { disposed = true; generation++; clearTimeout(timer); window.removeEventListener('seat-helper-pending', link) })
}
