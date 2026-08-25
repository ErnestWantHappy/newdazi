<template>
  <div class="login">
    <div class="login-backgrounds" aria-hidden="true">
      <div
        v-for="(item, index) in loginBackgrounds"
        :key="item.name"
        class="login-background"
        :class="{ 'is-active': activeBackgroundIndex === index }"
        :style="{ backgroundImage: `url(${item.src})` }"
      />
    </div>

    <div class="login-slide-content">
      <transition name="slide-copy" mode="out-in">
        <div :key="activeBackgroundIndex" class="login-slide-copy">
          <div class="login-slide-eyebrow">{{ activeBackground.eyebrow }}</div>
          <h2>{{ activeBackground.title }}</h2>
          <p>{{ activeBackground.subtitle }}</p>
          <div v-if="activeBackground.tags" class="login-slide-tags">
            <span v-for="tag in activeBackground.tags" :key="tag">{{ tag }}</span>
          </div>
        </div>
      </transition>
    </div>

    <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form">
      <h3 class="title">{{ title }}</h3>
      <el-form-item prop="username">
        <el-input
          v-model="loginForm.username"
          type="text"
          size="large"
          auto-complete="off"
          placeholder="账号"
        >
          <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          size="large"
          auto-complete="off"
          placeholder="密码"
          @keyup.enter="handleLogin"
        >
          <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="code" v-if="captchaEnabled">
        <el-input
          v-model="loginForm.code"
          size="large"
          auto-complete="off"
          placeholder="验证码"
          style="width: 63%"
          @keyup.enter="handleLogin"
        >
          <template #prefix><svg-icon icon-class="validCode" class="el-input__icon input-icon" /></template>
        </el-input>
        <div class="login-code">
          <img :src="codeUrl" @click="getCode" class="login-code-img"/>
        </div>
      </el-form-item>
      <!-- 记住密码（已移除） -->
      <el-form-item style="width:100%;">
        <el-button
          :loading="loading"
          size="large"
          type="primary"
          style="width:100%;"
          @click.prevent="handleLogin"
        >
          <span v-if="!loading">登 录</span>
          <span v-else>登 录 中...</span>
        </el-button>
        <div style="float: right;" v-if="register">
          <router-link class="link-type" :to="'/register'">立即注册</router-link>
        </div>
      </el-form-item>
    </el-form>
    <div class="login-carousel-dots" aria-hidden="true">
      <span
        v-for="(item, index) in loginBackgrounds"
        :key="item.name"
        class="login-carousel-dot"
        :class="{ 'is-active': activeBackgroundIndex === index }"
      />
    </div>
    <!-- 登录页保留必要的版权与备案信息，降低对登录操作的干扰。 -->
    <div class="el-login-footer" aria-label="平台版权信息">
      <div>版权所有：象山县教育局教科研中心&nbsp;&nbsp;备案号：浙ICP备05007927号</div>
    </div>
    <el-dialog
      v-model="schoolDialogVisible"
      title="选择校区"
      width="400px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      append-to-body
    >
      <el-form>
        <el-form-item label="校区">
          <el-radio-group v-model="selectedSchoolId">
            <el-radio v-for="item in schoolOptions" :key="item.deptId" :value="item.deptId">
              {{ item.deptName }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelSchoolSelection">取 消</el-button>
        <el-button type="primary" @click="confirmSchoolSelection">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from "@/utils/jsencrypt"
import useUserStore from '@/store/modules/user'
import knowledgeWallBackground from '@/assets/images/login-knowledge-wall-v1.webp'
import classroomBackground from '@/assets/images/login-classroom-closeup-v1.webp'
import xiangshanBackground from '@/assets/images/login-xiangshan-coast-v2.webp'

const title = import.meta.env.VITE_APP_TITLE
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()

const loginBackgrounds = [
  {
    name: '信息科技知识展墙',
    src: knowledgeWallBackground,
    eyebrow: '贯通小学 · 初中 · 高中',
    title: '从数字启蒙，到智能创造',
    subtitle: '覆盖信息科技学习的核心主题与实践能力。',
    tags: ['数字与编码', '算法与编程', '过程与控制', '网络与安全', '物联网', '人工智能']
  },
  {
    name: '真实信息科技课堂',
    src: classroomBackground,
    eyebrow: '真实课堂',
    title: '教、学、练、评，在课堂中发生',
    subtitle: '选择、判断、操作与打字，让每一次学习过程都看得见。'
  },
  {
    name: '象山山海与数字教育',
    src: xiangshanBackground,
    eyebrow: '山海象山',
    title: '数字教育，连接每一间课堂',
    subtitle: '让山海之间的学校共享资源、协同成长。'
  }
]
const activeBackgroundIndex = ref(0)
const activeBackground = computed(() => loginBackgrounds[activeBackgroundIndex.value])
let backgroundTimer

const loginForm = ref({
  username: "",
  password: "",
  rememberMe: false,
  code: "",
  uuid: ""
})

const loginRules = {
  username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
  password: [{ required: true, trigger: "blur", message: "请输入您的密码" }],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
}

const codeUrl = ref("")
const loading = ref(false)
// 验证码开关
const captchaEnabled = ref(true)
// 注册开关
const register = ref(false)
const redirect = ref(undefined)
const schoolDialogVisible = ref(false)
const schoolOptions = ref([])
const selectedSchoolId = ref()

watch(route, (newRoute) => {
    redirect.value = newRoute.query && newRoute.query.redirect
}, { immediate: true })

function handleLogin() {
  proxy.$refs.loginRef.validate(valid => {
    if (valid) {
      loading.value = true
      // 勾选了需要记住密码设置在 cookie 中设置记住用户名和密码
      if (loginForm.value.rememberMe) {
        Cookies.set("username", loginForm.value.username, { expires: 30 })
        Cookies.set("password", encrypt(loginForm.value.password), { expires: 30 })
        Cookies.set("rememberMe", loginForm.value.rememberMe, { expires: 30 })
      } else {
        // 否则移除
        Cookies.remove("username")
        Cookies.remove("password")
        Cookies.remove("rememberMe")
      }
      // 调用action的登录方法
      userStore.login(loginForm.value).then(res => {
        const password = loginForm.value.password;
        const isStrong = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{6,20}$/.test(password);
        userStore.needChangePwd = !isStrong;

        if (res && res.needsSchoolSelection) {
          schoolOptions.value = Array.isArray(res.schools) ? res.schools : []
          selectedSchoolId.value = schoolOptions.value[0]?.deptId
          schoolDialogVisible.value = true
          loading.value = false
          return
        }
        continueAfterLogin()
      }).catch((error) => {
        loading.value = false
        proxy.$modal.msgError(error?.message || "登录失败，请检查账号、密码和验证码")
        // 重新获取验证码
        if (captchaEnabled.value) {
          getCode()
        }
      })
    }
  })
}

function continueAfterLogin() {
  const query = route.query
  const otherQueryParams = Object.keys(query).reduce((acc, cur) => {
    if (cur !== "redirect") {
      acc[cur] = query[cur]
    }
    return acc
  }, {})
  loading.value = false
  
  // 根据角色决定默认跳转路径
  let defaultPath = "/"
  const roles = userStore.roles || []
  if (roles.includes('student')) {
    defaultPath = "/student"
  } else if (roles.includes('teacher')) {
    defaultPath = "/teacher-dashboard"
  }
  // admin 或其他角色保持默认 "/" → "/index"
  
  router.push({ path: redirect.value || defaultPath, query: otherQueryParams })
}

function confirmSchoolSelection() {
  if (!selectedSchoolId.value) {
    proxy.$message.warning("请先选择校区")
    return
  }
  loading.value = true
  userStore.selectSchool(selectedSchoolId.value).then(() => {
    schoolDialogVisible.value = false
    continueAfterLogin()
  }).catch(() => {
    loading.value = false
  })
}

function cancelSchoolSelection() {
  schoolDialogVisible.value = false
  loading.value = false
  userStore.logOut().catch(() => {})
  if (captchaEnabled.value) {
    getCode()
  }
}

function getCode() {
  getCodeImg().then(res => {
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/gif;base64," + res.img
      loginForm.value.uuid = res.uuid
    }
  })
}

function getCookie() {
  const username = Cookies.get("username")
  const password = Cookies.get("password")
  const rememberMe = Cookies.get("rememberMe")
  loginForm.value = {
    username: username === undefined ? loginForm.value.username : username,
    password: password === undefined ? loginForm.value.password : decrypt(password),
    rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
  }
}

function stopBackgroundRotation() {
  if (backgroundTimer) {
    window.clearInterval(backgroundTimer)
    backgroundTimer = undefined
  }
}

function startBackgroundRotation() {
  stopBackgroundRotation()
  backgroundTimer = window.setInterval(() => {
    activeBackgroundIndex.value = (activeBackgroundIndex.value + 1) % loginBackgrounds.length
  }, 5000)
}

function switchBackground(step) {
  activeBackgroundIndex.value = (
    activeBackgroundIndex.value + step + loginBackgrounds.length
  ) % loginBackgrounds.length
  startBackgroundRotation()
}

function handleBackgroundKeydown(event) {
  const target = event.target
  const isEditing = target instanceof Element && target.closest('input, textarea, select, [contenteditable="true"]')
  if (isEditing || !['ArrowLeft', 'ArrowRight'].includes(event.key)) {
    return
  }
  event.preventDefault()
  switchBackground(event.key === 'ArrowLeft' ? -1 : 1)
}

function setupBackgroundRotation() {
  // 预加载全部背景，避免首次轮播时因网络请求造成闪白或突变。
  loginBackgrounds.forEach(({ src }) => {
    const image = new Image()
    image.src = src
  })
  window.addEventListener('keydown', handleBackgroundKeydown)
  startBackgroundRotation()
}

function teardownBackgroundRotation() {
  window.removeEventListener('keydown', handleBackgroundKeydown)
  stopBackgroundRotation()
}

onMounted(setupBackgroundRotation)
onBeforeUnmount(teardownBackgroundRotation)

getCode()
getCookie()
</script>

<style lang='scss' scoped>
.login {
  position: relative;
  isolation: isolate;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  overflow: hidden;
  background: #17344b;
}
.login-backgrounds {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
}
.login-background {
  position: absolute;
  inset: 0;
  opacity: 0;
  background-position: center;
  background-size: cover;
  transform: scale(1.01);
  will-change: opacity, transform;
  transition:
    opacity 1.8s cubic-bezier(0.4, 0, 0.2, 1),
    transform 5.4s ease-out;
  &.is-active {
    opacity: 1;
    transform: scale(1.035);
  }
}
.login-slide-content {
  position: absolute;
  top: 48px;
  left: 48px;
  z-index: 1;
  width: min(420px, calc(50vw - 250px));
  pointer-events: none;
}
.login-slide-copy {
  padding: 18px 20px;
  color: #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.24);
  border-radius: 14px;
  background: rgba(8, 35, 58, 0.66);
  box-shadow: 0 14px 42px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  .login-slide-eyebrow {
    margin-bottom: 8px;
    color: #aeefff;
    font-size: 13px;
    font-weight: 600;
    letter-spacing: 2px;
  }
  h2 {
    margin: 0 0 9px;
    font-size: 25px;
    line-height: 1.35;
  }
  p {
    margin: 0;
    color: rgba(255, 255, 255, 0.86);
    font-size: 14px;
    line-height: 1.7;
  }
}
.login-slide-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-top: 13px;
  span {
    padding: 4px 9px;
    color: #eafaff;
    border: 1px solid rgba(174, 239, 255, 0.34);
    border-radius: 999px;
    background: rgba(22, 122, 160, 0.26);
    font-size: 12px;
  }
}
.login-carousel-dots {
  position: absolute;
  bottom: 54px;
  left: 50%;
  z-index: 3;
  display: flex;
  gap: 10px;
  transform: translateX(-50%);
  .login-carousel-dot {
    display: block;
    width: 9px;
    height: 9px;
    border: 1px solid rgba(255, 255, 255, 0.82);
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.28);
    transition: width 0.25s ease, border-radius 0.25s ease, background 0.25s ease;
    &.is-active {
      width: 28px;
      border-radius: 999px;
      background: #ffffff;
    }
  }
}
.slide-copy-enter-active,
.slide-copy-leave-active {
  transition: opacity 0.7s ease, transform 0.7s ease;
}
.slide-copy-enter-from,
.slide-copy-leave-to {
  opacity: 0;
  transform: translateY(8px);
}
.title {
  margin: 0px auto 30px auto;
  text-align: center;
  color: #707070;
}

.login-form {
  position: relative;
  border-radius: 6px;
  background: #ffffff;
  width: 400px;
  padding: 25px 25px 5px 25px;
  z-index: 2;
  box-shadow: 0 18px 55px rgba(0, 0, 0, 0.22);
  .el-input {
    height: 40px;
    input {
      height: 40px;
    }
  }
  .input-icon {
    height: 39px;
    width: 14px;
    margin-left: 0px;
  }
}
.login-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}
.login-code {
  width: 33%;
  height: 40px;
  float: right;
  img {
    cursor: pointer;
    vertical-align: middle;
  }
}
.el-login-footer {
  position: fixed;
  bottom: 18px;
  z-index: 3;
  width: 100%;
  padding: 0 20px;
  color: rgba(255, 255, 255, 0.78);
  font-family: Arial, "Microsoft YaHei", sans-serif;
  font-size: 12px;
  line-height: 1.9;
  text-align: center;
  letter-spacing: 0.2px;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.55);
  pointer-events: none;
}
.login-code-img {
  height: 40px;
  padding-left: 12px;
}

@media (max-width: 1100px) {
  .login-slide-content {
    top: 24px;
    left: 24px;
    width: min(280px, calc(50vw - 230px));
  }
  .login-slide-copy {
    padding: 14px 16px;
    h2 {
      font-size: 20px;
    }
  }
  .login-slide-tags {
    display: none;
  }
}

@media (max-width: 900px) {
  .login-slide-content {
    display: none;
  }

  .el-login-footer {
    bottom: 10px;
    font-size: 11px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .login-background,
  .slide-copy-enter-active,
  .slide-copy-leave-active {
    transition: none;
  }
}
</style>
