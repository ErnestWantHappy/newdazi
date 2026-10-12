<template>
  <div class="login-container">
    <!-- 背景已降级为静态柔和渐变，移除实时模糊DOM以释放云机房合成性能 -->

    <!-- 顶部导航栏：左上角放置统一Logo图与系统名称，带优雅偏距 -->
    <header class="login-header">
      <div class="header-brand">
        <img :src="logoImg" alt="平台Logo" class="brand-logo" />
        <div class="brand-text">
          <h1 class="brand-title">{{ title }}</h1>
          <span class="brand-subtitle">学业测评与教研协同综合管理系统</span>
        </div>
      </div>
    </header>

    <!-- 主体区域：严格对称的 2x2 四模块 CSS Grid 布局 -->
    <main class="login-main">
      <div class="login-layout-grid">
        <!-- 模块1（左上）：最新教研活动焦点图（5图图文结合走马灯轮播，支持现场实景大图） -->
        <section class="grid-cell cell-focus module-card focus-card" aria-labelledby="focus-title">
          <div class="card-header">
            <div class="header-title-box">
              <span class="pulse-dot" />
              <h2 id="focus-title" class="card-title">最新教研活动焦点</h2>
            </div>
            <button class="header-action-link" type="button" @click="promptResearchLogin">
              进入教研中心 <span class="arrow" aria-hidden="true">›</span>
            </button>
          </div>
          <div class="focus-carousel-wrap">
            <el-carousel
              height="100%"
              indicator-position="outside"
              arrow="hover"
              :interval="4500"
              class="research-carousel"
            >
              <el-carousel-item v-for="(slide, index) in researchSlides" :key="index">
                <div class="carousel-slide-box" @click="promptResearchLogin">
              <img :src="slide.image" :alt="slide.title" class="slide-img" loading="lazy" decoding="async" @error="handleSlideImageError(slide, index)" />
                  <div class="slide-overlay">

                    <div class="slide-meta">
                      <span class="slide-tag">{{ slide.tag }}</span>
                      <span class="slide-date">{{ slide.date }}</span>
                    </div>
                    <h3 class="slide-title" :title="slide.title">{{ slide.title }}</h3>
                    <p class="slide-school" :title="slide.school">{{ slide.school }}</p>
                  </div>
                </div>
              </el-carousel-item>
            </el-carousel>
          </div>
        </section>

        <!-- 模块2（右上）：用户登录框（与焦点图等高对称） -->
        <section class="grid-cell cell-login module-card login-card" aria-label="用户登录">
          <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-card-form">
            <div class="form-header">
              <h2 class="form-title">用户登录</h2>
              <p class="form-subtitle">欢迎进入信息科技学业测评平台</p>
            </div>

            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                type="text"
                size="large"
                auto-complete="off"
                placeholder="请输入您的账号"
                aria-label="账号"
                @keyup.enter="handleLogin"
              >
                <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
              </el-input>
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                size="large"
                show-password
                auto-complete="off"
                placeholder="请输入您的密码"
                aria-label="密码"
                @input="handlePasswordInput"
                @keydown="checkCapsLock"
                @keyup="checkCapsLock"
                @keyup.enter="handleLogin"
              >
                <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
              </el-input>
            </el-form-item>

            <!-- 方案一：大写锁定侦测轻量提示 -->
            <transition name="el-fade-in-linear">
              <div v-if="capsLockActive" class="caps-lock-hint" role="status">
                <span class="caps-icon" aria-hidden="true">⚠️</span> 大写锁定已开启 (Caps Lock On)
              </div>
            </transition>

            <el-form-item class="form-submit">
              <el-button
                :loading="loading"
                size="large"
                type="primary"
                class="submit-btn"
                @click.prevent="handleLogin"
              >
                <span v-if="!loading">登 录</span>
                <span v-else>登 录 中...</span>
              </el-button>
            </el-form-item>
          </el-form>
        </section>

        <!-- 模块3（左下）：区域学校开课动态与监管看板（移至左侧，享有充裕横向空间） -->
        <section class="grid-cell cell-school module-card showroom-card school-showroom" aria-labelledby="school-title">
          <div class="card-header">
            <div class="header-title-box">
              <span class="category-dot dot-cyan" />
              <h2 id="school-title" class="card-title">区域学校开课动态与监管看板</h2>
            </div>
          </div>
          <div class="card-body">
            <p v-if="schoolLoading" class="state-hint" role="status">正在加载学校动态…</p>
            <p v-else-if="schoolError" class="state-hint" role="status">
              学校动态暂时无法加载
              <button type="button" class="retry-btn" @click="loadSchoolPreviews">重试</button>
            </p>
            <div v-else class="school-table-wrapper" tabindex="0" role="region" aria-label="区域学校开课动态，可左右滚动查看">
              <table class="school-table">
                <colgroup>
                  <col style="width: 14%" /><col style="width: 18%" /><col style="width: 11%" />
                  <col style="width: 20%" /><col style="width: 12%" /><col style="width: 10%" /><col style="width: 15%" />
                </colgroup>
                <thead>
                  <tr>
                    <th scope="col">使用时间</th>
                    <th scope="col">学校名称</th>
                    <th scope="col">授课教师</th>
                    <th scope="col">课程主题</th>
                    <th scope="col">年级班级</th>
                    <th scope="col">参与学生</th>
                    <th scope="col">批改状态</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(lesson, index) in schoolPreviews" :key="index">
                    <td>{{ lesson.time }}</td>
                    <td :title="lesson.school">{{ lesson.school }}</td>
                    <td :title="lesson.teacher">{{ lesson.teacher }}</td>
                    <td class="lesson-topic" :title="lesson.topic">{{ lesson.topic }}</td>
                    <td :title="lesson.className">{{ lesson.className }}</td>
                    <td>{{ lesson.participantCount }}/{{ lesson.totalStudentCount }}</td>
                    <td>
                      <span
                        class="status-pill"
                        :class="lesson.pending > 0 ? 'is-pending' : (lesson.submittedCount > 0 ? 'is-complete' : 'is-empty')"
                      >
                        {{ lesson.pending > 0 ? `${lesson.pending}份待批改` : (lesson.submittedCount > 0 ? '全部批改完成' : '暂无待批') }}
                      </span>
                    </td>
                  </tr>
                  <tr v-if="!schoolPreviews.length">
                    <td colspan="7" class="empty-cell">近 90 天暂无开课动态</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </section>

        <!-- 模块4（右下）：最新教研活动预览（移至右侧，登录框下方，紧凑精致） -->
        <section class="grid-cell cell-research module-card showroom-card research-showroom" aria-labelledby="research-title">
          <div class="card-header">
            <div class="header-title-box">
              <span class="category-dot" />
              <h2 id="research-title" class="card-title">最新教研活动预览</h2>
            </div>
            <button class="header-action-link" type="button" @click="promptResearchLogin">
              查看更多 <span class="arrow" aria-hidden="true">›</span>
            </button>
          </div>
          <div class="card-body">
            <p v-if="researchLoading" class="state-hint" role="status">正在加载教研活动…</p>
            <p v-else-if="researchError" class="state-hint" role="status">
              教研活动暂时无法加载
              <button type="button" class="retry-btn" @click="loadResearchPreviews">重试</button>
            </p>
            <p v-else-if="!researchPreviews.length" class="state-hint">暂无教研活动</p>
            <ul v-else class="research-list">
              <li v-for="(activity, index) in researchPreviews" :key="index">
                <button class="research-item" type="button" @click="promptResearchLogin">
                  <time class="date-badge">{{ activity.date || '近期' }}</time>
                  <span class="research-info">
                    <span class="item-title" :title="activity.title">{{ activity.title }}</span>
                    <span class="item-sub">{{ activity.school || '学校未标注' }} · {{ activity.teacher || '教师未标注' }}</span>
                  </span>
                  <span class="item-arrow" aria-hidden="true">›</span>
                </button>
              </li>
            </ul>
          </div>
        </section>
      </div>
    </main>


    <!-- 底部备案版权信息 -->
    <footer class="login-footer" aria-label="平台版权信息">
      <div>版权所有：象山县教育局教科研中心&nbsp;&nbsp;&nbsp;&nbsp;备案号：浙ICP备05007927号</div>
    </footer>

    <!-- 验证码弹窗 -->
    <el-dialog v-model="captchaDialogVisible" title="登录安全验证" width="min(390px, 92vw)" append-to-body @closed="clearCaptcha">
      <el-form ref="captchaRef" :model="loginForm" :rules="loginRules" @submit.prevent="submitCaptcha">
        <el-form-item prop="code">
          <div class="captcha-row">
            <el-input v-model="loginForm.code" placeholder="请输入验证码" aria-label="验证码" size="large" @keyup.enter="submitCaptcha" />
            <button class="captcha-image-btn" type="button" aria-label="刷新验证码" @click="getCode">
              <span v-if="captchaError">重试验证码</span>
              <img v-else :src="codeUrl" alt="登录验证码" />
            </button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" :loading="loading" @click="submitCaptcha">验证并登录</el-button>
      </template>
    </el-dialog>

    <!-- 多校区选择弹窗 -->
    <el-dialog
      v-model="schoolDialogVisible"
      title="选择所属校区"
      width="420px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      append-to-body
    >
      <el-form>
        <el-form-item label="校区">
          <el-radio-group v-model="selectedSchoolId" class="school-radio-group">
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
import { getToken, needsSchoolSelection } from '@/utils/auth'
import { ElMessageBox } from 'element-plus'
import { getLoginActivityPreviews, getLoginSchoolPreviews, getLoginFocusSlides } from '@/api/business/loginPreview'
import logoImg from '@/assets/logo/logo.png'
import slide1 from '@/assets/images/research/slide-1.jpg'
import slide2 from '@/assets/images/research/slide-2.jpg'
import slide3 from '@/assets/images/research/slide-3.jpg'
import slide4 from '@/assets/images/research/slide-4.webp'
import slide5 from '@/assets/images/research/slide-5.webp'

const title = import.meta.env.VITE_APP_TITLE || '信息科技学业测评平台'
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()

// 5张教研活动精美图文轮播兜底数据
const defaultResearchSlides = [
  {
    image: slide1,
    title: '智能创造与开源硬件教学研讨',
    school: '象山县实验初中 · 信息科技教研组',
    date: '跨学科实践',
    tag: '跨学科实践'
  },
  {
    image: slide2,
    title: '课堂互评与同课异构教研评课会',
    school: '象山县教师进修学校 · 专题教研',
    date: '学科研讨',
    tag: '同课异构'
  },
  {
    image: slide3,
    title: '信息科技机房测评与编程实践展示',
    school: '丹城二中 · 数字化示范课堂',
    date: '课堂教学',
    tag: '学业测评'
  },
  {
    image: slide4,
    title: '算法思维与项目式学习专题研修',
    school: '象山县中小学信息科技大组',
    date: '素养提升',
    tag: '项目式学习'
  },
  {
    image: slide5,
    title: '现代信息科技教研协同与实践展评',
    school: '象山县教育局教研室',
    date: '区域协同',
    tag: '资源共享'
  }
]

// 焦点图轮播：优先展示最新真实教研/课堂实拍大图（如象山二中潘老师等真实机房上课图），若暂无则优雅降级
const focusSlides = ref([])
const focusLoading = ref(false)
const failedSlideImages = ref(new Set())
const visibleSlideCount = ref(1)
let showroomLoadTimer
let showroomIdleCallback
let delayedSlideTimer

function scheduleShowroomLoad() {
  if (showroomLoadTimer) return
  const load = () => {
    showroomLoadTimer = undefined
    if (typeof document !== 'undefined' && document.visibilityState === 'hidden') return
    loadFocusSlides()
    loadResearchPreviews()
    loadSchoolPreviews()
  }
  if (typeof window !== 'undefined' && 'requestIdleCallback' in window) {
    showroomLoadTimer = window.setTimeout(() => {
      showroomLoadTimer = undefined
      showroomIdleCallback = window.requestIdleCallback(() => {
        showroomIdleCallback = undefined
        load()
      }, { timeout: 2500 })
    }, 1200)
  } else {
    showroomLoadTimer = window.setTimeout(load, 1800)
  }
}

async function loadFocusSlides() {
  if (focusLoading.value) return
  focusLoading.value = true
  try {
    const list = await getLoginFocusSlides(previewAbortController.signal)
    if (Array.isArray(list) && list.length > 0) {
      focusSlides.value = list
      scheduleDelayedSlides(list.length)
    }
  } catch (error) {
    // 降级由 computed 处理
  } finally {
    focusLoading.value = false
  }
}

function scheduleDelayedSlides(total) {
  if (delayedSlideTimer) window.clearTimeout(delayedSlideTimer)
  visibleSlideCount.value = 1
  if (total <= 1) return
  // 首屏输入完成后再把其余轮播项放入 DOM，避免 el-carousel 初始化时并发下载所有真实图片。
  delayedSlideTimer = window.setTimeout(() => {
    delayedSlideTimer = undefined
    visibleSlideCount.value = total
  }, 15000)
}

const apiBase = import.meta.env.VITE_APP_BASE_API || ''

function formatSlideImage(url) {
  if (!url || typeof url !== 'string') return url
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) {
    return url
  }
  if (url.startsWith('/business/')) {
    return `${apiBase}${url}`
  }
  return url
}

function handleSlideImageError(slide, index) {
  const fallback = defaultResearchSlides[index % defaultResearchSlides.length]
  if (slide.image !== fallback.image) {
    failedSlideImages.value.add(slide.image)
  }
}

const allResearchSlides = computed(() => {
  const baseList = focusSlides.value.length > 0 ? focusSlides.value : defaultResearchSlides.map((slide, index) => {
    const real = researchPreviews.value[index]
    if (real && real.title) {
      return {
        ...slide,
        image: real.image || slide.image,
        title: real.title,
        school: `${real.school || '本校'} · ${real.teacher || '教师'}`,
        date: real.date || slide.date,
        tag: slide.tag || '教研动态'
      }
    }
    return slide
  })
  return baseList.map((slide, index) => ({
    ...slide,
    image: failedSlideImages.value.has(formatSlideImage(slide.image))
      ? defaultResearchSlides[index % defaultResearchSlides.length].image : formatSlideImage(slide.image)
  }))
})

const researchSlides = computed(() => allResearchSlides.value.slice(0, visibleSlideCount.value))


// 方案一：键盘大写锁定侦测与中文误输入过滤
const capsLockActive = ref(false)

function checkCapsLock(event) {
  if (event && event.getModifierState) {
    capsLockActive.value = Boolean(event.getModifierState('CapsLock'))
  }
}

function handlePasswordInput(val) {
  if (!val) return
  if (/[\u4e00-\u9fa5]/.test(val)) {
    loginForm.value.password = val.replace(/[\u4e00-\u9fa5]/g, '')
    proxy.$modal.msgWarning('密码不支持中文字符，已为您自动过滤')
  }
}


const researchPreviews = ref([])
const schoolPreviews = ref([])
const researchLoading = ref(false)
const schoolLoading = ref(false)
const researchError = ref(false)
const schoolError = ref(false)
const previewAbortController = new AbortController()

async function loadResearchPreviews() {
  if (researchLoading.value) return
  researchLoading.value = true
  researchError.value = false
  try {
    researchPreviews.value = await getLoginActivityPreviews(previewAbortController.signal)
  } catch (error) {
    if (error.code !== 'ERR_CANCELED') researchError.value = true
  } finally {
    researchLoading.value = false
  }
}

async function loadSchoolPreviews() {
  if (schoolLoading.value) return
  schoolLoading.value = true
  schoolError.value = false
  try {
    schoolPreviews.value = await getLoginSchoolPreviews(previewAbortController.signal)
  } catch (error) {
    if (error.code !== 'ERR_CANCELED') schoolError.value = true
  } finally {
    schoolLoading.value = false
  }
}

function promptResearchLogin() {
  ElMessageBox.alert('进入教研中心需先验证登录身份', '温馨提示', { confirmButtonText: '去登录' })
    .then(() => proxy.$refs.loginRef?.$el.querySelector('input')?.focus())
    .catch(() => {})
}

// 登录表单数据
const loginForm = ref({
  username: "",
  password: "",
  code: "",
  uuid: ""
})

const loginRules = {
  username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
  password: [{ required: true, trigger: "blur", message: "请输入您的密码" }],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
}

const codeUrl = ref("")
const captchaError = ref(false)
const loading = ref(false)
const captchaEnabled = ref(true)
const captchaDialogVisible = ref(false)
const captchaRef = ref()
const redirect = ref(undefined)
const schoolDialogVisible = ref(false)
const schoolOptions = ref([])
const selectedSchoolId = ref()

watch(route, (newRoute) => {
  redirect.value = newRoute.query && newRoute.query.redirect
}, { immediate: true })

function handleLogin(captchaVerified = false) {
  if (loading.value) return
  // 其他标签页退出后 Cookie 已失效，不能拿本页残留令牌跳过账号认证。
  if (userStore.token !== getToken()) {
    userStore.token = getToken() || ''
    userStore.roles = []
    userStore.permissions = []
  }
  loading.value = true
  proxy.$refs.loginRef.validate(valid => {
    if (!valid) {
      loading.value = false
      return
    }
    // 启用验证码的环境在提交时弹出校验，不绕过后端认证
    if (captchaEnabled.value && !userStore.token && captchaVerified !== true) {
      captchaDialogVisible.value = true
      loading.value = false
      return
    }
    if (valid) {
      // 彻底清理历史可能残留的密码Cookie，保障安全
      Cookies.remove("password")
      Cookies.remove("rememberMe")
      if (userStore.token) {
        if (needsSchoolSelection()) {
          restoreSchoolSelection().catch(() => {})
          return
        }
        continueAfterLogin().catch(() => {
          loading.value = false
          proxy.$modal.msgError('登录已成功，页面加载失败，请稍后再点登录重试')
        })
        return
      }

      userStore.login(loginForm.value).then(res => {
        captchaDialogVisible.value = false
        const password = loginForm.value.password
        const isStrong = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{6,20}$/.test(password)
        userStore.needChangePwd = !isStrong

        if (res && res.needsSchoolSelection) {
          schoolOptions.value = Array.isArray(res.schools) ? res.schools : []
          selectedSchoolId.value = schoolOptions.value[0]?.deptId
          schoolDialogVisible.value = true
          loading.value = false
          return
        }
        return continueAfterLogin()
      }).catch((error) => {
        loading.value = false
        if (!error?.notified) proxy.$modal.msgError(error?.message || "登录失败，请检查账号、密码和验证码")
        if (captchaEnabled.value) {
          getCode()
        }
      })
    }
  })
}

function submitCaptcha() {
  if (loading.value) return
  captchaRef.value?.validate(valid => {
    if (valid) handleLogin(true)
  })
}

function clearCaptcha() {
  loginForm.value.code = ''
  captchaRef.value?.clearValidate()
}

async function continueAfterLogin() {
  await userStore.getInfo()
  const query = route.query
  const otherQueryParams = Object.keys(query).reduce((acc, cur) => {
    if (cur !== "redirect") acc[cur] = query[cur]
    return acc
  }, {})

  let defaultPath = "/"
  const roles = userStore.roles || []
  if (roles.includes('student')) {
    defaultPath = "/student/index"
  } else if (roles.includes('teacher')) {
    defaultPath = "/teacher-dashboard"
  }

  const target = roles.includes('student') ? '/student/index' : (redirect.value || defaultPath)
  try {
    await router.push({ path: target, query: otherQueryParams })
  } finally {
    loading.value = false
  }
}

function confirmSchoolSelection() {
  if (loading.value) return
  if (!selectedSchoolId.value) {
    proxy.$modal.msgWarning("请先选择校区")
    return
  }
  loading.value = true
  userStore.selectSchool(selectedSchoolId.value).then(() => {
    schoolDialogVisible.value = false
    return continueAfterLogin()
  }).catch(() => {
    loading.value = false
  })
}

async function restoreSchoolSelection() {
  loading.value = true
  try {
    await userStore.getInfo()
    schoolOptions.value = userStore.schools
    selectedSchoolId.value = schoolOptions.value[0]?.deptId
    schoolDialogVisible.value = true
  } finally { loading.value = false }
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
  loginForm.value.code = ''
  captchaError.value = false
  getCodeImg().then(res => {
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/gif;base64," + res.img
      loginForm.value.uuid = res.uuid
    }
  }).catch(() => {
    captchaError.value = true
  })
}

function getCookie() {
  const username = Cookies.get("username")
  if (username) {
    loginForm.value.username = username
  }
  Cookies.remove("password")
  Cookies.remove("rememberMe")
}

onMounted(() => {
  if (getToken() && needsSchoolSelection()) restoreSchoolSelection().catch(() => {})
  scheduleShowroomLoad()
})

onBeforeUnmount(() => {
  if (showroomLoadTimer) {
    window.clearTimeout(showroomLoadTimer)
    showroomLoadTimer = undefined
  }
  if (showroomIdleCallback !== undefined && 'cancelIdleCallback' in window) {
    window.cancelIdleCallback(showroomIdleCallback)
    showroomIdleCallback = undefined
  }
  if (delayedSlideTimer) {
    window.clearTimeout(delayedSlideTimer)
    delayedSlideTimer = undefined
  }
  previewAbortController.abort()
})

getCode()
getCookie()
</script>

<style lang="scss" scoped>
/* 全局页面容器：温润柔和的浅色渐变背景 */
.login-container {
  position: relative;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  min-height: 100svh;
  color: #1e293b;
  background: linear-gradient(135deg, #f5f8fc 0%, #edf3fa 50%, #e5eef7 100%);
  overflow-x: hidden;
}

/* 移除动态高模糊光晕，避免云机房和老旧电脑的渲染合成管线满载 */
.login-bg-mesh {
  display: none !important;
}

/* 顶部导航 Header：左上角放置统一Logo与标题，带优雅偏距 */
.login-header {
  position: relative;
  z-index: 2;
  padding: 24px clamp(24px, 4vw, 56px) 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;

  .header-brand {
    display: flex;
    align-items: center;
    gap: 16px;
    user-select: none;

    .brand-logo {
      width: 58px;
      height: 58px;
      object-fit: contain;
      filter: drop-shadow(0 4px 12px rgba(22, 119, 255, 0.25));
      transition: transform 0.3s ease;

      &:hover {
        transform: scale(1.06);
      }
    }

    .brand-text {
      display: flex;
      flex-direction: column;

      .brand-title {
        margin: 0;
        font-size: 24px;
        font-weight: 700;
        letter-spacing: 0.8px;
        color: #0f2744;
        line-height: 1.25;
      }

      .brand-subtitle {
        font-size: 12px;
        font-weight: 500;
        color: #64748b;
        letter-spacing: 0.5px;
        margin-top: 3px;
      }
    }
  }
}

/* 主体内容：左右两大栏，2x2四模块 */
.login-main {
  position: relative;
  z-index: 2;
  flex: 1;
  padding: 8px clamp(16px, 3.5vw, 48px) 24px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-layout-grid {
  display: grid;
  grid-template-columns: minmax(0, 60fr) minmax(0, 40fr);
  grid-template-rows: minmax(330px, 39vh) minmax(330px, 39vh);
  gap: 22px;
  align-items: stretch;
  width: 100%;
  max-width: min(1560px, 94vw);
  margin: 0 auto;
}

.grid-cell {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

/* 通用模块白卡片样式：纯白微投影卡片，移除 backdrop-filter 确保全机房与云桌面 60FPS 流畅 */
.module-card {
  position: relative;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border: 1px solid rgba(220, 233, 245, 0.95);
  border-radius: 14px;
  box-shadow: 0 10px 30px rgba(18, 52, 86, 0.05);
  overflow: hidden;
  transition: box-shadow 0.25s ease, border-color 0.25s ease;

  &:hover {
    box-shadow: 0 14px 38px rgba(18, 52, 86, 0.09);
    border-color: rgba(186, 215, 243, 0.95);
  }

  .card-header {
    flex-shrink: 0;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 20px 12px;
    border-bottom: 1px solid rgba(226, 236, 247, 0.8);

    .header-title-box {
      display: flex;
      align-items: center;
      gap: 10px;

      .pulse-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: #1677ff;
        box-shadow: 0 0 8px rgba(22, 119, 255, 0.6);
        animation: pulse-wave 2s infinite ease-in-out;
      }

      .category-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: #0284c7;

        &.dot-cyan {
          background: #0d9488;
        }
      }

      .card-title {
        margin: 0;
        font-size: 15px;
        font-weight: 600;
        color: #1e293b;
        letter-spacing: 0.3px;
      }
    }

    .header-action-link {
      background: none;
      border: none;
      padding: 0;
      color: #1677ff;
      font-size: 12px;
      font-weight: 500;
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 3px;
      transition: color 0.2s;

      &:hover {
        color: #0958d9;
      }

      .arrow {
        font-size: 14px;
      }
    }
  }
}

@keyframes pulse-wave {
  0%, 100% { transform: scale(0.9); opacity: 0.6; }
  50% { transform: scale(1.2); opacity: 1; }
}

/* 模块1（左上）：最新教研活动焦点图轮播 */
.focus-card {
  .focus-carousel-wrap {
    flex: 1;
    min-height: 0;
    padding: 12px 18px 10px;
    display: flex;
    flex-direction: column;
  }
}

.research-carousel {
  flex: 1;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  border-radius: 10px;
  overflow: hidden;

  :deep(.el-carousel__container) {
    flex: 1;
    height: 100% !important;
  }

  :deep(.el-carousel__indicators--outside) {
    margin-top: 8px;
    button {
      background-color: #cbd5e1;
      border-radius: 4px;
      height: 4px;
      transition: all 0.3s;
    }
    .is-active button {
      background-color: #1677ff;
      width: 24px;
    }
  }
}

.carousel-slide-box {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;

  .slide-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
    transition: transform 0.6s cubic-bezier(0.25, 1, 0.5, 1);
  }

  &:hover .slide-img {
    transform: scale(1.035);
  }

  .slide-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 24px 18px 14px;
    background: linear-gradient(180deg, transparent 0%, rgba(15, 23, 42, 0.72) 55%, rgba(15, 23, 42, 0.92) 100%);
    color: #ffffff;
    pointer-events: none;

    .slide-meta {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 6px;

      .slide-tag {
        display: inline-block;
        padding: 2px 8px;
        font-size: 11px;
        font-weight: 500;
        border-radius: 4px;
        background: rgba(22, 119, 255, 0.85);
        color: #ffffff;
      }

      .slide-date {
        font-size: 11px;
        color: rgba(255, 255, 255, 0.85);
      }
    }

    .slide-title {
      margin: 0 0 4px;
      font-size: 15px;
      font-weight: 600;
      line-height: 1.4;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      color: #ffffff;
    }

    .slide-school {
      margin: 0;
      font-size: 12px;
      color: rgba(226, 232, 240, 0.9);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}

/* 模块2（右上）：用户登录卡片（与左上焦点图等高对称） */
.login-card {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 24px 32px;

  .login-card-form {
    width: 100%;
    margin: 0 auto;

    .form-header {
      text-align: left;
      margin-bottom: 20px;

      .form-title {
        margin: 0;
        font-size: 20px;
        font-weight: 700;
        color: #0f172a;
        letter-spacing: 0.5px;
      }

      .form-subtitle {
        margin: 6px 0 0;
        font-size: 12px;
        color: #64748b;
      }
    }

    :deep(.el-form-item) {
      margin-bottom: 18px;
    }

    :deep(.el-input__wrapper) {
      min-height: 44px;
      border-radius: 8px;
      background: #f8fafc !important;
      border: 1px solid #cbd5e1 !important;
      box-shadow: none !important;
      transition: all 0.25s ease;

      &:hover {
        background: #ffffff !important;
        border-color: #94a3b8 !important;
      }

      &.is-focus {
        background: #ffffff !important;
        border-color: #1677ff !important;
        box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.14) !important;
      }

      .el-input__inner {
        color: #0f172a !important;
        font-size: 14px;
        &::placeholder {
          color: #94a3b8 !important;
        }
      }
    }

    .input-icon {
      width: 16px;
      height: 18px;
      margin-right: 6px;
      color: #64748b;
    }

    .caps-lock-hint {
      display: flex;
      align-items: center;
      gap: 6px;
      margin-top: -8px;
      margin-bottom: 14px;
      padding: 5px 12px;
      font-size: 12px;
      font-weight: 500;
      color: #b45309;
      background: #fef3c7;
      border: 1px solid #fde68a;
      border-radius: 6px;
      line-height: 1.4;

      .caps-icon {
        font-size: 14px;
      }
    }

    .form-submit {
      margin: 18px 0 0;
    }

    .submit-btn {
      width: 100%;
      height: 44px;
      font-size: 15px;
      font-weight: 600;
      border-radius: 8px;
      border: none;
      background: linear-gradient(135deg, #1677ff 0%, #0958d9 100%);
      box-shadow: 0 6px 18px rgba(22, 119, 255, 0.32);
      letter-spacing: 2px;
      transition: all 0.25s ease;

      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 8px 22px rgba(22, 119, 255, 0.45);
      }
    }
  }
}

/* 模块3（左下）：区域学校开课动态与监管看板（宽幅流式展示） */
.school-showroom {
  .card-body {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
    padding: 12px 18px 14px;
  }

  .school-table-wrapper {
    flex: 1;
    min-height: 0;
    width: 100%;
    overflow: auto;
    scrollbar-width: thin;
    scrollbar-color: #cbd5e1 transparent;
  }

  .school-table {
    width: 100%;
    min-width: 680px;
    table-layout: fixed;
    border-collapse: collapse;
    text-align: left;
    font-size: 13px;

    th {
      position: sticky;
      top: 0;
      z-index: 1;
      color: #475569;
      background: #f1f5f9;
      font-weight: 600;
      padding: 10px 10px;
      border-bottom: 1px solid #e2e8f0;
    }

    td {
      padding: 11px 10px;
      border-bottom: 1px solid #f1f5f9;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      color: #334155;
    }

    tr:hover td {
      background: rgba(241, 245, 249, 0.65);
    }

    .lesson-topic {
      max-width: 220px;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .empty-cell {
      text-align: center;
      padding: 24px;
      color: #94a3b8;
    }
  }

  .status-pill {
    display: inline-block;
    padding: 3px 8px;
    border-radius: 4px;
    font-size: 11px;
    font-weight: 500;
    line-height: 1.4;

    &.is-complete {
      color: #15803d;
      background: #dcfce7;
      border: 1px solid #bbf7d0;
    }

    &.is-pending {
      color: #b45309;
      background: #fef3c7;
      border: 1px solid #fde68a;
    }

    &.is-empty {
      color: #64748b;
      background: #f1f5f9;
      border: 1px solid #e2e8f0;
    }
  }
}

/* 模块4（右下）：最新教研活动预览列表（右侧紧凑卡片） */
.research-showroom {
  .card-body {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    padding: 12px 18px 14px;
    scrollbar-width: thin;
    scrollbar-color: #cbd5e1 transparent;
  }

  .research-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
    padding: 0;
    margin: 0;
    list-style: none;
  }

  .research-item {
    display: flex;
    align-items: center;
    gap: 12px;
    width: 100%;
    min-height: 52px;
    padding: 9px 12px;
    text-align: left;
    color: inherit;
    border: 1px solid rgba(226, 232, 240, 0.85);
    border-radius: 9px;
    background: rgba(248, 250, 252, 0.85);
    cursor: pointer;
    transition: background 0.2s, border-color 0.2s, transform 0.2s;

    &:hover {
      background: rgba(238, 246, 255, 0.95);
      border-color: #93c5fd;
      transform: translateX(2px);
    }

    .date-badge {
      flex-shrink: 0;
      padding: 3px 7px;
      border-radius: 4px;
      color: #0369a1;
      border: 1px solid rgba(186, 230, 253, 0.9);
      background: #f0f9ff;
      font-size: 12px;
      font-weight: 500;
    }

    .research-info {
      display: grid;
      gap: 2px;
      min-width: 0;
      flex: 1;

      .item-title {
        font-size: 13px;
        font-weight: 500;
        color: #1e293b;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .item-sub {
        font-size: 12px;
        color: #64748b;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }

    .item-arrow {
      color: #94a3b8;
      font-size: 17px;
      flex-shrink: 0;
    }
  }
}

/* 状态占位与交互辅助 */
.state-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  min-height: 160px;
  margin: 0;
  color: #64748b;
  font-size: 13px;

  .retry-btn {
    border: none;
    background: none;
    color: #1677ff;
    cursor: pointer;
    font-size: 13px;
    padding: 0;
    &:hover {
      text-decoration: underline;
    }
  }
}

/* 底部 Footer */
.login-footer {
  position: relative;
  z-index: 2;
  padding: 16px 20px 20px;
  color: #64748b;
  font-size: 12px;
  text-align: center;
  letter-spacing: 0.2px;
}

/* 验证码与弹窗 */
.captcha-row {
  display: flex;
  width: 100%;
  gap: 12px;
  .el-input { flex: 1; }
}

.captcha-image-btn {
  flex-shrink: 0;
  width: 112px;
  padding: 0;
  border: 1px solid #dce5f1;
  border-radius: 6px;
  background: #f8fafc;
  cursor: pointer;
  img {
    display: block;
    width: 100%;
    height: 40px;
    object-fit: contain;
  }
}

.school-radio-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* 响应式断点适配 */
@media (max-width: 1100px) {
  .login-layout-grid {
    grid-template-columns: 1fr;
    grid-template-rows: auto;
    gap: 20px;
    max-width: 680px;
  }

  .focus-card {
    .focus-carousel-wrap {
      min-height: 260px;
    }
  }

  .login-card {
    padding: 24px 20px;
  }
}


@media (max-width: 640px) {
  .login-header {
    padding: 18px 20px 10px;
    .brand-title { font-size: 18px; }
  }

  .login-main {
    padding: 10px 16px 20px;
  }

  .login-card-form {
    padding: 24px 20px 18px;
  }

  .school-table {
    min-width: 600px;
  }
}
</style>
