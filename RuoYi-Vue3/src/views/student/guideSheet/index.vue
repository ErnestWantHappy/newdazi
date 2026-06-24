<template>
  <div class="student-guide-sheet">
    <!-- 顶部导航栏 - 与主页统一 -->
    <header class="dashboard-header">
      <div class="header-left">
        <img src="@/assets/logo/logo.png" class="logo" alt="Logo" />
        <span class="platform-name">智慧课堂 - 学生端</span>
        <div class="view-toggle">
          <el-button size="small" plain @click="switchToHome">主页</el-button>
          <el-button size="small" type="primary" disabled>导学单</el-button>
        </div>
      </div>
      <div class="header-right">
        <el-dropdown trigger="click" @command="handleCommand">
          <div class="user-info">
            <el-avatar :size="36" shape="circle" icon="UserFilled" />
            <span class="user-name">{{ studentName }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-state">
      <el-icon class="is-loading" :size="48"><Loading /></el-icon>
      <p>正在加载导学单...</p>
    </div>

    <!-- 无导学单 -->
    <div v-else-if="!hasSheet" class="empty-state">
      <el-empty description="暂无导学单，请等待教师发布" :image-size="160">
        <el-button type="primary" icon="ArrowLeft" @click="switchToHome">返回智慧课堂首页</el-button>
      </el-empty>
    </div>

    <!-- 导学单内容 -->
    <div v-else class="sheet-wrapper">
      <div class="sheet-container">
        <div class="sheet-operations">
          <div class="left-info">
            <el-tag v-if="submitted" type="success" size="large">
              <el-icon><CircleCheckFilled /></el-icon> 已提交（可重新提交）
            </el-tag>
            <el-tag v-else type="warning" size="large">待完成</el-tag>
          </div>
          <div class="right-actions">
            <el-button icon="Check" type="primary" size="large" @click="handleSubmit" :loading="submitting">
              提交导学单
            </el-button>
            <el-button icon="Refresh" size="large" @click="handleSave" :loading="saving">
              保存草稿
            </el-button>
          </div>
        </div>

        <div v-if="teacherMsg" class="teacher-msg-bar">
          <el-alert :title="teacherMsg" type="warning" show-icon :closable="true" @close="teacherMsg = ''" />
        </div>

        <div class="form-wrapper">
          <v-form-render
            ref="renderRef"
            :form-json="formJsonObj"
            :form-data="answerData"
            :option-data="optionData"
          />
        </div>

        <div v-if="teacherMachineIp" class="upload-hint">
          <el-alert type="info" :closable="false" show-icon>
            <template #title>文件上传地址：{{ teacherMachineIp }}:5000</template>
            图片/视频等大文件将直接上传到教师机本地服务器
          </el-alert>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="StudentGuideSheet">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { getStudentGuideSheet, submitGuideSheet, sendHeartbeat } from '@/api/business/guideSheet'
import { setTeacherMachineIp } from '@/utils/teacherMachine'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, CircleCheckFilled } from '@element-plus/icons-vue'
import useUserStore from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const hasSheet = ref(false)
const sheetTitle = ref('')
const teacherMachineIp = ref('')
const sheetId = ref(null)
const formJsonObj = ref(null)
const answerData = ref({})
const optionData = ref({})
const maxPages = ref(0)
const submitted = ref(false)
const submitting = ref(false)
const saving = ref(false)
const teacherMsg = ref('')
const renderRef = ref(null)

const studentName = ref('')

let heartbeatTimer = null
let autoSaveTimer = null

function switchToHome() {
  router.replace('/student/index')
}

function handleCommand(cmd) {
  if (cmd === 'logout') {
    ElMessageBox.confirm('确定注销并退出系统吗？', '提示').then(() => {
      userStore.logOut().then(() => {
        location.href = '/index'
      })
    })
  }
}

function handleSave() {
  if (!renderRef.value) return
  saving.value = true
  const data = {
    sheetId: sheetId.value,
    answerJson: JSON.stringify(answerData.value),
    currentPage: 0,
    action: 'save'
  }
  submitGuideSheet(data).then(() => {
    ElMessage.success('草稿已保存')
  }).finally(() => { saving.value = false })
}

function handleSubmit() {
  if (!renderRef.value) return
  submitting.value = true
  renderRef.value.getFormData().then(formData => {
    const data = {
      sheetId: sheetId.value,
      answerJson: JSON.stringify(formData),
      currentPage: 0,
      action: 'submit'
    }
    submitGuideSheet(data).then(() => {
      submitted.value = true
      ElMessage.success('导学单已提交')
    }).finally(() => { submitting.value = false })
  }).catch(error => {
    ElMessage.error(error || '表单验证失败')
    submitting.value = false
  })
}

onMounted(() => {
  studentName.value = userStore.nickName || userStore.name || '同学'

  getStudentGuideSheet().then(res => {
    hasSheet.value = res.hasSheet || false
    if (hasSheet.value) {
      sheetTitle.value = res.sheetTitle || ''
      sheetId.value = res.sheetId
      if (res.formJson) {
        try {
          formJsonObj.value = JSON.parse(res.formJson)
        } catch (e) {
          console.warn('表单JSON解析失败', e)
        }
      }
      maxPages.value = res.maxPages || 0
      teacherMachineIp.value = res.teacherMachineIp || ''
      if (teacherMachineIp.value) {
        setTeacherMachineIp(teacherMachineIp.value)
      }
      const existing = res.existingAnswer
      if (existing && existing.status === '2' && existing.answerJson) {
        submitted.value = true
        try {
          answerData.value = JSON.parse(existing.answerJson) || {}
        } catch (e) {
          answerData.value = {}
        }
      } else if (existing && existing.answerJson) {
        try {
          answerData.value = JSON.parse(existing.answerJson) || {}
        } catch (e) {
          answerData.value = {}
        }
      }
      // 心跳定时器
      heartbeatTimer = setInterval(() => {
        sendHeartbeat({ sheetId: sheetId.value, currentPage: 0 }).catch(() => {})
      }, 30000)
      // 自动保存定时器
      autoSaveTimer = setInterval(() => {
        handleSave()
      }, 30000)
    }
    loading.value = false
  }).catch(() => {
    ElMessage.error('加载导学单失败')
    loading.value = false
  })

  // 页面关闭前保存
  window.addEventListener('beforeunload', onBeforeUnload)
})

function onBeforeUnload() {
  if (answerData.value && Object.keys(answerData.value).length > 0) {
    const data = {
      sheetId: sheetId.value,
      answerJson: JSON.stringify(answerData.value),
      currentPage: 0,
      action: 'save'
    }
    navigator.sendBeacon
      ? navigator.sendBeacon('/dev-api/business/guide-sheet/student/submit',
          new Blob([JSON.stringify(data)], { type: 'application/json' }))
      : submitGuideSheet(data)
  }
}

onBeforeUnmount(() => {
  if (heartbeatTimer) clearInterval(heartbeatTimer)
  if (autoSaveTimer) clearInterval(autoSaveTimer)
  window.removeEventListener('beforeunload', onBeforeUnload)
})
</script>

<style scoped>
.student-guide-sheet {
  background-color: #f5f7fa;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 头部导航 - 与智慧课堂学生端保持一致的视觉风格 */
.dashboard-header {
  height: 64px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32px;
  position: sticky;
  top: 0;
  z-index: 2000;
  flex-shrink: 0;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.view-toggle {
  display: flex;
  align-items: center;
  gap: 0;
  margin-left: 16px;
}
.view-toggle .el-button {
  border-radius: 0;
}
.view-toggle .el-button:first-child {
  border-radius: 4px 0 0 4px;
}
.view-toggle .el-button:last-child {
  border-radius: 0 4px 4px 0;
}
.logo {
  height: 32px;
}
.platform-name {
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
}
.header-right {
  display: flex;
  align-items: center;
}
.header-right .user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 20px;
  transition: background 0.2s;
}
.header-right .user-info:hover {
  background: #f0f2f5;
}
.user-name {
  font-weight: 500;
  font-size: 14px;
}

/* 加载/空状态 */
.loading-state, .empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 16px;
}
.loading-state { gap: 16px; }

/* 导学单内容区 */
.sheet-wrapper {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}
.sheet-container {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  max-width: 1000px;
  margin: 0 auto;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.sheet-operations {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid #409EFF;
}
.left-info {
  display: flex;
  align-items: center;
  gap: 8px;
}
.right-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.form-wrapper {
  max-width: 900px;
  margin: 0 auto;
}
.teacher-msg-bar {
  max-width: 900px;
  margin: 0 auto 12px;
}
.upload-hint {
  max-width: 900px;
  margin: 20px auto 0;
}
</style>