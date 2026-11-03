import { defineStore } from 'pinia'
import { getExtensionContext, saveStudentSpace } from '@/api/business/extension'
import useUserStore from './user'

export default defineStore('studentSpace', {
  // requestId 用于丢弃迟到响应：同机换学生账号时，上一账号的空间上下文不能写进新会话。
  state: () => ({ context: null, account: null, requestId: 0 }),
  actions: {
    async load(force = false) {
      const account = useUserStore().id
      if (!force && this.account === account && this.context) return this.context
      const requestId = ++this.requestId
      const { data } = await getExtensionContext()
      if (requestId !== this.requestId || account !== useUserStore().id) {
        // 期间已换账号或已有更新的请求，放弃本次结果。
        return this.account === useUserStore().id ? this.context : null
      }
      this.account = account
      this.context = data
      return data
    },
    async select(space) {
      await saveStudentSpace(space)
      this.context.space_pref = space
      this.context.space_selected = 1
      return space === 'extension' ? '/student/extension' : '/student/index'
    },
    // 退出登录时清空，避免共用学生机短暂显示上一账号的班级信息。
    reset() {
      this.requestId += 1
      this.context = null
      this.account = null
    }
  }
})
