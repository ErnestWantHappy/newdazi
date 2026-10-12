import { onBeforeUnmount, provide, ref, watch } from 'vue'
import request from '@/utils/request'
import useUserStore from '@/store/modules/user'
import { takeSeatHelperToken } from '@/utils/seatHelper'

export function provideStudentSeatSession() {
  const user = useUserStore()
  const status = ref({ enabled: false }), message = ref('')
  let generation = 0, timer, disposed = false
  provide('student-seat-helper', { status, message })
  async function start([id, token]) {
    const ticket = ++generation
    clearTimeout(timer)
    status.value = { enabled: false }; message.value = ''
    if (!id || !token) return
    const active = () => !disposed && generation === ticket && user.id === id && user.token === token
    const association = takeSeatHelperToken()
    if (association) {
      try { await request({ url: '/business/seat-helper/student/associate', method: 'post', data: { token: association } }) }
      catch { if (active()) message.value = '机位关联未完成，答题不受影响；可再次打开助手自动关联。' }
    }
    async function poll() {
      if (!active()) return
      try {
        const response = await request({ url: '/business/seat-helper/student/status' })
        if (!active()) return
        status.value = response.data || { enabled: false }
        if (status.value.associated) message.value = ''
      } catch {
        if (active()) { message.value = '助手信息暂不可用，显示上次状态；答题可以继续。' }
      } finally {
        // 保留短时失效规则；布局内切到 Python、物联或协作仍持续证明当前网页会话。
        if (active()) timer = setTimeout(poll, 30000)
      }
    }
    await poll()
  }
  watch(() => [user.id, user.token], start, { immediate: true })
  const link = () => { if (user.id && user.token) start([user.id, user.token]) }
  window.addEventListener('seat-helper-pending', link)
  onBeforeUnmount(() => { disposed = true; generation++; clearTimeout(timer); window.removeEventListener('seat-helper-pending', link) })
}
