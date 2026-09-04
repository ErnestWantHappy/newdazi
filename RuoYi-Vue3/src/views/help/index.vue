<template>
  <div class="help-page">
    <section class="platform-hero">
      <div class="hero-copy">
        <p class="eyebrow">TEACH · LEARN · PRACTICE · ASSESS</p>
        <h1>AI 驱动的信息科技<br><span>教学与多维学业测评平台</span></h1>
        <p class="hero-summary">面向中小学真实课堂打造的全流程数字化教学基础设施，贯通题库建设、课程设计、学习任务、作品提交、AI 智能批改与多维学情分析，让每一次作答都沉淀为可解释、可追踪、可改进的教学证据。</p>
        <div class="hero-tags"><span>全题型教学闭环</span><span>操作题在线批改</span><span>AI 多维评价</span><span>课堂与区域监测</span></div>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="scrollTo(isTeacher ? 'smart-check' : 'manager-guide')">{{ isTeacher ? '检查我的备课条件' : '查看管理指南' }}<el-icon><ArrowDown /></el-icon></el-button>
          <el-button class="contact-button" size="large" @click="contactDeveloper">联系开发者<el-icon><TopRight /></el-icon></el-button>
        </div>
        <p class="developer-line"><i />由象山县一线信息科技教师 <b>郑东旭主要开发，朱屹辅助支持</b> 设计并持续开发</p>
      </div>
      <div class="hero-showcase" aria-label="课程设计真实平台页面">
        <div class="showcase-window"><div class="window-bar"><i /><i /><i /><span>真实平台 · 课程设计</span></div><img :src="stepCourseDesignerImage" alt="课程内容设计与教学资源库真实页面"></div>
        <div class="showcase-caption"><span>教学练评闭环</span><b>从课程设计开始，把题目、班级与评价真正连接起来</b></div>
      </div>
    </section>

    <!-- 平台推荐环境：浏览器 / Mind+ / 掌控板（对所有角色可见） -->
    <section id="recommended-env" class="help-section env-section">
      <header class="section-head">
        <div><p>RECOMMENDED ENVIRONMENT</p><h2>平台推荐环境</h2><span>建议使用以下配置访问平台与开展课堂实验，Windows / macOS 均为通用版本。</span></div>
      </header>
      <div class="env-grid">
        <article v-for="item in recommendedEnv" :key="item.name" class="env-card">
          <div class="env-icon"><el-icon><component :is="envIcon(item.name)" /></el-icon></div>
          <h3>{{ item.name }}</h3>
          <el-tag type="primary" effect="plain" class="env-version">{{ item.version }}</el-tag>
          <p>{{ item.purpose }}</p>
          <div class="env-meta"><span>{{ item.metaLabel || '适用系统' }}：{{ item.metaValue || item.os }}</span></div>
          <el-button v-if="item.url" type="primary" plain size="small" @click="openExternal(item.url)">前往下载<el-icon><TopRight /></el-icon></el-button>
        </article>
      </div>
    </section>
    <template v-if="isTeacher">
      <section id="smart-check" class="help-section smart-section">
        <header class="section-head">
          <div><p>SMART START</p><h2>先检查，再开始备课</h2><span>平台正在根据你的真实数据给出下一步建议。</span></div>
          <el-button text :loading="checking" @click="loadReadiness"><el-icon><Refresh /></el-icon>重新检查</el-button>
        </header>
        <div class="smart-grid" v-loading="checking">
          <article v-for="item in readinessCards" :key="item.key" :class="['smart-card', item.state]">
            <div class="smart-icon"><el-icon><component :is="item.icon" /></el-icon></div>
            <div class="smart-copy"><span>{{ item.kicker }}</span><h3>{{ item.title }}</h3><p>{{ item.description }}</p></div>
            <el-button :type="item.state === 'warning' ? 'primary' : ''" @click="goTo(item.path)">{{ item.action }}<el-icon><ArrowRight /></el-icon></el-button>
          </article>
        </div>
        <div class="shared-rule"><el-icon><InfoFilled /></el-icon><p><b>学生数据是校内共享的，不需要每位教师重复导入。</b><span>同一学校、同一届别、同一班级只要导入一次；任课教师在“班级管理”中认领自己实际任教的班级即可。</span></p></div>
      </section>

      <section id="teacher-flow" class="help-section flow-section">
        <header class="section-head"><div><p>VISUAL CLASSROOM WORKFLOW</p><h2>六步完成教学练评闭环</h2><span>主页保留横向流程；点击“查看图文教程”后，进入接近全屏的真实页面分步教程。</span></div></header>
        <div class="flow-scroll" aria-label="教师六步教学流程">
          <div class="flow-rail">
            <article v-for="(step, index) in teacherFlow" :key="step.id" class="flow-node">
              <div class="flow-node-head"><b>{{ String(index + 1).padStart(2, '0') }}</b><span>{{ step.kicker }}</span></div>
              <h3>{{ step.title }}</h3>
              <p>{{ step.description }}</p>
              <div class="flow-actions">
                <el-button type="primary" link @click="goTo(step.path)">打开功能<el-icon><ArrowRight /></el-icon></el-button>
                <el-button link @click="openTutorial(step)">查看图文教程<el-icon><Reading /></el-icon></el-button>
              </div>
            </article>
          </div>
        </div>
      </section>

      <section id="faq" class="help-section faq-section">
        <header class="section-head"><div><p>TROUBLESHOOTING</p><h2>新手教师最常遇到的问题</h2></div></header>
        <el-collapse v-model="activeFaq" accordion>
          <el-collapse-item v-for="(item, index) in teacherFaq" :key="item.question" :name="index"><template #title><span class="faq-index">Q{{ index + 1 }}</span><b>{{ item.question }}</b></template><p>{{ item.answer }}</p></el-collapse-item>
        </el-collapse>
      </section>
    </template>

    <template v-else>
      <section id="manager-guide" class="help-section manager-section">
        <header class="section-head"><div><p>REGIONAL GOVERNANCE</p><h2>区域组织与平台治理</h2><span>该内容只向教研员和管理员展示，教师不会看到。</span></div></header>
        <div class="manager-grid"><article v-for="item in managementGuides" :key="item.title"><span>{{ item.role }}</span><h3>{{ item.title }}</h3><ol><li v-for="step in item.steps" :key="step">{{ step }}</li></ol></article></div>
      </section>
    </template>

    <el-dialog v-model="tutorialVisible" fullscreen destroy-on-close class="tutorial-dialog" :show-close="false">
      <template #header>
        <div class="tutorial-dialog-head" v-if="activeTutorial">
          <div><span>TEACHER VISUAL GUIDE</span><h2>{{ activeTutorial.title }}</h2><p>{{ activeTutorial.summary }}</p></div>
          <div class="tutorial-head-actions"><el-button type="primary" @click="goTo(activeTutorial.path)">{{ activeTutorial.action }}<el-icon><ArrowRight /></el-icon></el-button><el-button circle aria-label="关闭教程" @click="tutorialVisible = false"><el-icon><Close /></el-icon></el-button></div>
        </div>
      </template>

      <div v-if="activeTutorial" class="tutorial-shell">
        <section v-if="activeTutorial.branches" class="branch-board">
          <div class="branch-root"><span>入口</span><b>{{ activeTutorial.title }}</b></div>
          <div class="branch-lines" />
          <div class="branch-list">
            <article v-for="branch in activeTutorial.branches" :key="branch.title">
              <span>{{ branch.description }}</span><h3>{{ branch.title }}</h3>
              <div class="branch-children"><b v-for="child in branch.children" :key="child">{{ child }}</b></div>
              <p v-if="branch.warning">{{ branch.warning }}</p>
            </article>
          </div>
        </section>

        <nav class="tutorial-index" aria-label="本教程步骤">
          <button v-for="(section, index) in activeTutorial.sections" :key="section.title" type="button" @click="scrollTutorialSection(index)"><b>{{ String(index + 1).padStart(2, '0') }}</b><span>{{ section.title }}</span></button>
        </nav>

        <div class="tutorial-sections">
          <article v-for="(section, index) in activeTutorial.sections" :id="`tutorial-section-${index}`" :key="section.title" class="tutorial-section">
            <div class="tutorial-media">
              <div class="media-bar"><i /><i /><i /><span>真实操作界面</span></div>
              <img :src="section.image" :alt="`${section.title}真实功能截图`">
            </div>
            <div class="tutorial-copy">
              <span>{{ section.eyebrow }}</span><h2>{{ section.title }}</h2><p>{{ section.text }}</p>
              <ul><li v-for="tip in section.tips" :key="tip">{{ tip }}</li></ul>
              <div v-if="section.smartTip" class="smart-tip"><el-icon><InfoFilled /></el-icon><b>智能提示</b><span>{{ section.smartTip }}</span></div>
              <div v-if="section.danger" class="danger-tip"><b>请特别注意</b><span>{{ section.danger }}</span></div>
              <div v-if="section.path || section.links" class="section-links">
                <el-button v-if="section.path" type="primary" @click="goTo(section.path)">{{ section.action }}<el-icon><ArrowRight /></el-icon></el-button>
                <el-button v-for="link in section.links" :key="link.url" @click="openExternal(link.url)">{{ link.label }}<el-icon><TopRight /></el-icon></el-button>
              </div>
            </div>
          </article>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, markRaw, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown, ArrowRight, Close, Collection, DataAnalysis, InfoFilled, Reading, Refresh, School, Tickets, TopRight } from '@element-plus/icons-vue'
import useUserStore from '@/store/modules/user'
import { getDashboardData } from '@/api/business/teacher'
import { getMyClasses } from '@/api/business/teacherClass'
import { listStudent } from '@/api/business/student'
import { listQuestion } from '@/api/business/question'
import stepCourseDesignerImage from '@/assets/help/step-04-course-designer.png'
import { teacherFlow, teacherTutorials } from './tutorials'
import { recommendedEnv } from './recommendedEnv'

const router = useRouter()
const userStore = useUserStore()
const isTeacher = computed(() => userStore.roles.includes('teacher'))
const developerSiteUrl = import.meta.env.VITE_DEVELOPER_SITE_URL || 'http://10.52.1.123:3012/'
const checking = ref(false)
const activeFaq = ref(0)
const tutorialVisible = ref(false)
const activeTutorial = ref(null)
const readiness = ref({ students: null, classes: null, questions: null, courses: null })

const readinessCards = computed(() => [
  buildReadiness('students', '学生基础', School, readiness.value.students, '还没有导入学生', '已发现学生数据', '学生是校内共享数据；若学校已有人导入，你无需重复操作。', '/studentguanli', '前往学生管理'),
  buildReadiness('classes', '任教关系', Collection, readiness.value.classes, '还没有管理班级', '已管理班级', '只有认领任教班级后，教师首页才会出现对应届别的课程入口。', '/teacherClass', '前往班级管理'),
  buildReadiness('questions', '备课资源', Tickets, readiness.value.questions, '题库还是空的', '题库已有内容', '可新建选择、判断、打字和操作题，也可使用 Excel 批量导入客观题。', '/question', '前往题库管理'),
  buildReadiness('courses', '课堂任务', DataAnalysis, readiness.value.courses, '还没有创建课程', '本学期已有课程', '准备好题目与班级后，从教师首页创建课程。', '/teacher-dashboard/index', '前往教师首页')
])

const teacherFaq = [
  { question: '教师首页为什么没有“新建课程”？', answer: '最常见原因是还没有管理任何班级。先到“班级管理”认领自己实际任教的班级，再回到教师首页刷新。' },
  { question: '其他老师已经导入过学生，我还要再导入吗？', answer: '不需要。学生属于学校、届别和班级共享的基础数据，同一班级只导入一次。你只需认领任教班级。' },
  { question: '学生模板中的班级编号应该怎么填？', answer: '只填班号 01～99，不要写年级。正确示例是“学号 01、入学年份 2025、班级编号 11”；601、602 等三位班号会被拒绝。' },
  { question: '为什么 Excel 不能批量导入操作题？', answer: '操作题还包含起始文件、补充资源、提交格式、教师参考答案和评分项，必须在题库中使用“新增”逐题配置。' },
  { question: '学生登录后为什么看不到课程？', answer: '检查课程是否已经指派到学生当前学校、届别和班级，并确认教师管理着该班级。' },
  { question: 'AI 批改为什么提示没有配置 Key？', answer: '进入操作题批改页的“AI 设置”，保存阿里云百炼 API Key 并完成连通测试；同时确认百炼已开通并且免费额度或账户余额可用。' }
]

const managementGuides = [
  { role: '教研员', title: '区域教学质量监测', steps: ['查看学校、教师、课程与作业完成总体状态', '组织区域抽测并完成组卷、发布、评卷和复核', '通过教学监管定位课程开设与异常情况', '组织教研活动并维护共享资源'] },
  { role: '管理员', title: '组织、权限与系统运行', steps: ['维护学校组织与用户基础信息', '按角色控制菜单和数据权限边界', '在诊断中心检查服务和请求异常', '核心配置、迁移和发布前完成备份与回滚准备'] }
]

function buildReadiness(key, kicker, icon, count, emptyTitle, readyTitle, description, path, action) {
  const failed = count === null
  return { key, kicker, icon: markRaw(icon), state: failed ? 'unknown' : count > 0 ? 'ready' : 'warning', title: failed ? '暂未取得检查结果' : count > 0 ? `${readyTitle} · ${count}` : emptyTitle, description: failed ? '你仍可直接进入对应页面继续操作。' : description, path, action }
}

function responseCount(response) {
  if (Number.isFinite(Number(response?.total))) return Number(response.total)
  if (Array.isArray(response?.data)) return response.data.length
  if (Array.isArray(response?.rows)) return response.rows.length
  return 0
}

async function loadReadiness() {
  if (!isTeacher.value) return
  checking.value = true
  const results = await Promise.allSettled([listStudent({ pageNum: 1, pageSize: 1 }), getMyClasses(), listQuestion({ pageNum: 1, pageSize: 1 }), getDashboardData()])
  const value = { students: null, classes: null, questions: null, courses: null }
  if (results[0].status === 'fulfilled') value.students = responseCount(results[0].value)
  if (results[1].status === 'fulfilled') value.classes = responseCount(results[1].value)
  if (results[2].status === 'fulfilled') value.questions = responseCount(results[2].value)
  if (results[3].status === 'fulfilled') value.courses = (results[3].value?.data || []).reduce((sum, group) => sum + (group.lessons || []).length, 0)
  readiness.value = value
  checking.value = false
}

function scrollTo(id) { document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' }) }
function goTo(path) { tutorialVisible.value = false; router.push(path) }
// 推荐环境卡片图标映射（按名称关键词）
function envIcon(name) {
  if (name.includes('Chrome')) return 'Monitor'
  if (name.includes('Mind+')) return 'Cpu'
  return 'Cpu'
}

function contactDeveloper() { openExternal(developerSiteUrl) }
function openExternal(url) { window.open(url, '_blank', 'noopener,noreferrer') }
function openTutorial(step) { activeTutorial.value = teacherTutorials[step.id]; tutorialVisible.value = true }
function scrollTutorialSection(index) { document.getElementById(`tutorial-section-${index}`)?.scrollIntoView({ behavior: 'smooth', block: 'start' }) }

onMounted(loadReadiness)
</script>

<style scoped>
.help-page{--blue:#3468ff;--cyan:#36d8ee;--ink:var(--el-text-color-primary);max-width:1580px;margin:auto;padding:22px;color:var(--ink)}
.platform-hero{position:relative;display:grid;grid-template-columns:minmax(0,1.08fr) minmax(430px,.92fr);gap:48px;min-height:520px;padding:58px 62px;overflow:hidden;border-radius:30px;color:#fff;background:radial-gradient(circle at 90% 4%,rgb(78 225 255/25%),transparent 30%),radial-gradient(circle at 10% 100%,rgb(101 86 255/22%),transparent 34%),linear-gradient(135deg,#081d3d,#123a6b 54%,#082f4e);box-shadow:0 30px 90px rgb(8 29 61/20%)}
.platform-hero:after{position:absolute;inset:0;opacity:.18;background-image:linear-gradient(rgb(255 255 255/8%) 1px,transparent 1px),linear-gradient(90deg,rgb(255 255 255/8%) 1px,transparent 1px);background-size:34px 34px;mask-image:linear-gradient(90deg,#000,transparent 80%);content:""}.hero-copy{position:relative;z-index:2;align-self:center}.eyebrow,.section-head p{margin:0 0 12px;color:#6ee9ff;font:700 11px/1 monospace;letter-spacing:2.2px}.hero-copy h1{margin:0;font-size:clamp(38px,4vw,62px);line-height:1.08;letter-spacing:-.05em}.hero-copy h1 span{color:#7de9ff}.hero-summary{max-width:720px;margin:24px 0 0;color:rgb(255 255 255/76%);font-size:15px;line-height:1.9}.hero-tags{display:flex;flex-wrap:wrap;gap:8px;margin-top:22px}.hero-tags span{padding:7px 11px;border:1px solid rgb(126 233 255/22%);border-radius:99px;color:#cceeff;background:rgb(15 71 118/55%);font-size:11px}.hero-actions{display:flex;gap:12px;margin-top:28px}.hero-actions .el-icon,.smart-card .el-icon,.flow-actions .el-icon,.developer-callout .el-icon,.section-links .el-icon,.tutorial-head-actions .el-icon{margin-left:6px}.contact-button{color:#fff;border-color:rgb(255 255 255/28%);background:rgb(255 255 255/8%)}.developer-line{display:flex;align-items:center;gap:8px;margin:24px 0 0;color:rgb(255 255 255/56%);font-size:12px}.developer-line i{width:7px;height:7px;border-radius:50%;background:#5bf2a5;box-shadow:0 0 14px #5bf2a5}
.hero-showcase{position:relative;z-index:2;align-self:center;min-width:0}.showcase-window{overflow:hidden;padding:8px;border:1px solid rgb(255 255 255/18%);border-radius:20px;background:rgb(3 20 44/78%);box-shadow:0 28px 60px rgb(0 0 0/32%);transform:perspective(1200px) rotateY(-4deg) rotateX(1deg)}.window-bar,.media-bar{display:flex;align-items:center;gap:6px;height:30px;padding:0 6px}.window-bar i,.media-bar i{width:7px;height:7px;border-radius:50%;background:#ffca5c}.window-bar i:nth-child(2),.media-bar i:nth-child(2){background:#62d99f}.window-bar i:nth-child(3),.media-bar i:nth-child(3){background:#5da3ff}.window-bar span,.media-bar span{margin-left:auto;color:rgb(255 255 255/48%);font:9px monospace;letter-spacing:1px}.showcase-window img{display:block;width:100%;aspect-ratio:16/9;object-fit:cover;object-position:top;border-radius:12px;background:#fff}.showcase-caption{position:relative;margin:-26px 20px 0;padding:18px 20px;border:1px solid rgb(126 233 255/22%);border-radius:14px;background:rgb(8 34 67/92%);box-shadow:0 18px 36px rgb(0 0 0/24%);backdrop-filter:blur(12px)}.showcase-caption span{display:block;margin-bottom:5px;color:#75e8ff;font-size:10px;letter-spacing:1.3px}.showcase-caption b{font-size:13px;line-height:1.6}
.help-section{scroll-margin-top:22px;padding:64px 10px}.section-head{display:flex;align-items:flex-start;justify-content:space-between;gap:30px;margin-bottom:30px}.section-head p{color:var(--blue)}.section-head h2{margin:0 0 8px;font-size:32px;letter-spacing:-.03em}.section-head span{color:var(--el-text-color-secondary)}
.smart-grid{display:grid;grid-template-columns:repeat(2,1fr);gap:14px;min-height:220px}.smart-card{display:grid;grid-template-columns:52px 1fr auto;align-items:center;gap:16px;padding:22px;border:1px solid var(--el-border-color-lighter);border-radius:18px;background:var(--el-bg-color)}.smart-card.warning{border-color:rgb(255 166 31/35%);background:linear-gradient(120deg,var(--el-bg-color),rgb(255 166 31/7%))}.smart-icon{display:grid;place-items:center;width:50px;height:50px;border-radius:15px;color:var(--blue);background:var(--el-color-primary-light-9);font-size:24px}.smart-card.warning .smart-icon{color:#e88911;background:rgb(255 166 31/12%)}.smart-copy>span{color:var(--el-text-color-placeholder);font-size:10px;letter-spacing:1.4px}.smart-copy h3{margin:5px 0 6px}.smart-copy p{margin:0;color:var(--el-text-color-secondary);font-size:12px;line-height:1.65}.shared-rule{display:flex;gap:14px;margin-top:16px;padding:20px 22px;border-radius:16px;color:#135677;background:linear-gradient(110deg,#e8faff,#eef3ff)}.shared-rule>.el-icon{flex:0 0 24px;margin-top:2px;font-size:24px}.shared-rule p{display:flex;flex-direction:column;gap:5px;margin:0}.shared-rule span{font-size:12px;line-height:1.7}
.flow-section{margin-inline:-22px;padding-inline:32px;border-radius:28px;background:var(--el-fill-color-extra-light)}.flow-scroll{overflow-x:auto;padding:12px 2px 22px}.flow-rail{position:relative;display:grid;grid-template-columns:repeat(6,minmax(185px,1fr));gap:14px;min-width:1160px}.flow-rail:before{position:absolute;top:39px;right:5%;left:5%;height:2px;background:linear-gradient(90deg,var(--cyan),var(--blue),#8a70ff);content:""}.flow-node{position:relative;min-height:225px;padding:20px 18px;border:1px solid var(--el-border-color-lighter);border-radius:18px;background:var(--el-bg-color);box-shadow:0 12px 34px rgb(24 57 112/7%)}.flow-node-head{position:relative;display:flex;align-items:center;gap:9px}.flow-node-head b{display:grid;place-items:center;width:40px;height:40px;border:5px solid var(--el-bg-color);border-radius:50%;color:#fff;background:linear-gradient(135deg,var(--cyan),var(--blue));box-shadow:0 8px 18px rgb(52 104 255/22%);font:800 10px monospace}.flow-node-head span{color:var(--blue);font-size:10px;letter-spacing:.8px}.flow-node h3{margin:22px 0 8px;font-size:20px}.flow-node>p{min-height:42px;margin:0;color:var(--el-text-color-secondary);font-size:12px;line-height:1.7}.flow-actions{display:flex;align-items:flex-start;flex-direction:column;margin-top:17px}.flow-actions .el-button{margin-left:0}
.faq-section :deep(.el-collapse){border:0}.faq-section :deep(.el-collapse-item){margin-bottom:10px}.faq-section :deep(.el-collapse-item__header){min-height:62px;padding:0 18px;border:1px solid var(--el-border-color-lighter);border-radius:12px;background:var(--el-bg-color)}.faq-section :deep(.el-collapse-item__wrap){border:0;background:transparent}.faq-section :deep(.el-collapse-item__content){padding:18px 24px}.faq-section p{margin:0;color:var(--el-text-color-secondary);line-height:1.8}.faq-index{margin-right:14px;color:var(--cyan);font:700 12px monospace}.manager-grid{display:grid;grid-template-columns:repeat(2,1fr);gap:18px}.manager-grid article{padding:28px;border:1px solid var(--el-border-color-lighter);border-radius:18px;background:var(--el-bg-color)}.manager-grid article>span{color:var(--blue);font-size:11px}.manager-grid h3{margin:10px 0 20px;font-size:22px}.manager-grid ol{display:grid;gap:12px;padding-left:20px;color:var(--el-text-color-secondary);line-height:1.7}
.developer-callout{display:flex;align-items:center;justify-content:space-between;gap:28px;margin:20px 0 34px;padding:36px 42px;border-radius:22px;color:#fff;background:linear-gradient(120deg,#163967,#273b7d 58%,#145573)}.developer-callout p{margin:0 0 6px;color:#75e8ff;font-size:11px;letter-spacing:1.5px}.developer-callout h2{margin:0 0 7px}.developer-callout span{color:rgb(255 255 255/66%)}
:global(.tutorial-dialog){margin:0!important;background:var(--el-bg-color-page)}:global(.tutorial-dialog .el-dialog__header){position:sticky;z-index:20;top:0;margin:0;padding:0;border-bottom:1px solid var(--el-border-color-lighter);background:rgb(255 255 255/92%);backdrop-filter:blur(18px)}:global(html.dark .tutorial-dialog .el-dialog__header){background:rgb(16 21 30/92%)}:global(.tutorial-dialog .el-dialog__body){padding:0}.tutorial-dialog-head{display:flex;align-items:center;justify-content:space-between;gap:28px;max-width:1500px;margin:auto;padding:18px 34px}.tutorial-dialog-head>div:first-child{min-width:0}.tutorial-dialog-head span{color:var(--blue);font:700 10px monospace;letter-spacing:1.5px}.tutorial-dialog-head h2{margin:4px 0;font-size:24px}.tutorial-dialog-head p{overflow:hidden;margin:0;color:var(--el-text-color-secondary);font-size:12px;text-overflow:ellipsis;white-space:nowrap}.tutorial-head-actions{display:flex;flex:0 0 auto;gap:10px}.tutorial-shell{max-width:1500px;margin:auto;padding:28px 34px 80px}.branch-board{position:relative;padding:30px;border-radius:24px;color:#fff;background:radial-gradient(circle at 80% 10%,rgb(55 214 239/20%),transparent 28%),linear-gradient(125deg,#0a2245,#153c6d)}.branch-root{display:flex;align-items:center;flex-direction:column;gap:5px;width:240px;margin:auto;padding:14px;border:1px solid rgb(116 232 255/22%);border-radius:14px;background:rgb(9 45 83/72%)}.branch-root span{color:#7de9ff;font-size:10px;letter-spacing:1px}.branch-lines{width:55%;height:34px;margin:auto;border-right:1px solid rgb(116 232 255/45%);border-bottom:1px solid rgb(116 232 255/45%);border-left:1px solid rgb(116 232 255/45%)}.branch-list{display:grid;grid-template-columns:repeat(auto-fit,minmax(260px,1fr));gap:14px}.branch-list article{padding:18px;border:1px solid rgb(116 232 255/16%);border-radius:14px;background:rgb(5 29 59/64%)}.branch-list article>span{color:#7de9ff;font-size:10px}.branch-list h3{margin:5px 0 12px}.branch-children{display:flex;flex-wrap:wrap;gap:7px}.branch-children b{padding:6px 9px;border-radius:8px;color:#d8f7ff;background:rgb(74 190 230/13%);font-size:11px}.branch-list p{margin:12px 0 0;color:#ffcb66;font-size:11px}.tutorial-index{position:sticky;z-index:10;top:93px;display:flex;gap:8px;overflow-x:auto;margin:22px 0;padding:10px;border:1px solid var(--el-border-color-lighter);border-radius:15px;background:rgb(255 255 255/88%);box-shadow:0 10px 26px rgb(24 57 112/8%);backdrop-filter:blur(16px)}:global(html.dark) .tutorial-index{background:rgb(16 21 30/88%)}.tutorial-index button{display:flex;align-items:center;gap:8px;min-width:max-content;padding:8px 11px;border:0;border-radius:10px;color:var(--el-text-color-regular);background:transparent;cursor:pointer}.tutorial-index button:hover{color:var(--blue);background:var(--el-color-primary-light-9)}.tutorial-index b{color:var(--blue);font:700 10px monospace}.tutorial-index span{font-size:11px}.tutorial-sections{display:grid;gap:28px}.tutorial-section{scroll-margin-top:162px;display:grid;grid-template-columns:minmax(0,1.25fr) minmax(340px,.75fr);gap:32px;align-items:center;padding:24px;border:1px solid var(--el-border-color-lighter);border-radius:22px;background:var(--el-bg-color);box-shadow:0 14px 42px rgb(24 57 112/6%)}.tutorial-section:nth-child(even) .tutorial-media{order:2}.tutorial-media{overflow:hidden;border:1px solid #263b59;border-radius:16px;background:#0a1930}.media-bar{padding-inline:12px}.tutorial-media img{display:block;width:100%;max-height:650px;object-fit:contain;background:#f4f6fa}.tutorial-copy{padding:12px}.tutorial-copy>span{color:var(--blue);font:700 10px monospace;letter-spacing:1.3px}.tutorial-copy h2{margin:9px 0 12px;font-size:28px}.tutorial-copy>p{margin:0;color:var(--el-text-color-secondary);line-height:1.9}.tutorial-copy ul{display:grid;gap:9px;margin:18px 0;padding-left:20px;color:var(--el-text-color-regular);font-size:13px;line-height:1.7}.smart-tip,.danger-tip{display:grid;grid-template-columns:auto auto 1fr;gap:8px;align-items:start;margin-top:14px;padding:14px;border-radius:12px;font-size:12px;line-height:1.7}.smart-tip{color:#145b79;background:#e7f9ff}.danger-tip{grid-template-columns:auto 1fr;color:#a83a30;background:#fff1ef}.section-links{display:flex;flex-wrap:wrap;gap:10px;margin-top:18px}
@media(max-width:1200px){.platform-hero{grid-template-columns:1fr;padding:48px}.hero-showcase{width:min(100%,760px);margin:auto}.tutorial-section{grid-template-columns:1fr}.tutorial-section:nth-child(even) .tutorial-media{order:initial}}
.env-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:16px}.env-card{display:flex;align-items:stretch;flex-direction:column;gap:8px;padding:24px;border:1px solid var(--el-border-color-lighter);border-radius:18px;background:var(--el-bg-color);box-shadow:0 10px 26px rgb(24 57 112/6%)}.env-icon{display:grid;place-items:center;width:46px;height:46px;margin-bottom:4px;border-radius:14px;color:var(--blue);background:var(--el-color-primary-light-9);font-size:22px}.env-card h3{margin:0;font-size:17px}.env-card p{margin:0;color:var(--el-text-color-secondary);font-size:12px;line-height:1.7;flex:1}.env-meta span{color:var(--el-text-color-placeholder);font-size:11px}.env-version{align-self:flex-start}@media(max-width:900px){.env-grid{grid-template-columns:1fr}}@media(max-width:780px){.help-page{padding:10px}.platform-hero{padding:34px 22px;border-radius:22px}.hero-showcase{margin-top:18px}.showcase-window{transform:none}.showcase-caption{margin:-10px 8px 0;padding:14px}.hero-actions{align-items:stretch;flex-direction:column}.smart-grid,.manager-grid{grid-template-columns:1fr}.smart-card{grid-template-columns:48px 1fr}.smart-card>.el-button{grid-column:1/3}.flow-section{margin-inline:-10px;padding-inline:20px}.developer-callout{align-items:flex-start;flex-direction:column;padding:28px 24px}.section-head{align-items:flex-start;flex-direction:column}.section-head h2{font-size:28px}.tutorial-dialog-head{padding:14px 16px}.tutorial-dialog-head p{display:none}.tutorial-head-actions>.el-button:first-child{display:none}.tutorial-shell{padding:16px 12px 60px}.branch-board{padding:20px 14px}.branch-root{width:auto}.branch-lines{width:70%}.tutorial-index{top:72px;margin-block:14px}.tutorial-section{gap:16px;padding:12px;border-radius:16px}.tutorial-copy{padding:8px}.tutorial-copy h2{font-size:22px}.tutorial-media img{max-height:none}.smart-tip{grid-template-columns:auto 1fr}.smart-tip span{grid-column:1/3}.section-links{align-items:stretch;flex-direction:column}.section-links .el-button{margin-left:0}}
</style>
