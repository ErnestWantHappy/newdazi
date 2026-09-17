<template>
  <div class="druid-page">
    <el-alert
      v-if="!druidReady"
      type="warning"
      show-icon
      :closable="false"
      title="数据监控（Druid）当前未启用"
      class="druid-alert"
    >
      <p>
        正式环境默认关闭 Druid 控制台（<code>DRUID_STAT_ENABLED=false</code>），直接打开会出现 404 / Whitelabel 页面。
        日常排障请使用 <strong>系统监控 → 系统诊断中心</strong>。
      </p>
      <p v-if="probeError" class="probe-error">探测结果：{{ probeError }}</p>
      <el-button type="primary" size="small" @click="goDiagnosis">打开系统诊断中心</el-button>
      <el-button size="small" :loading="probing" @click="probeDruid">重新探测</el-button>
    </el-alert>
    <i-frame v-if="druidReady" v-model:src="url"></i-frame>
  </div>
</template>

<script setup>
import iFrame from '@/components/iFrame'
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const base = import.meta.env.VITE_APP_BASE_API || ''
const url = ref(base + '/druid/login.html')
const druidReady = ref(false)
const probing = ref(false)
const probeError = ref('')

/** 探测 Druid 是否可达；关闭时避免直接嵌 iframe 展示 Whitelabel 404 */
async function probeDruid() {
  probing.value = true
  probeError.value = ''
  try {
    const resp = await fetch(url.value, { method: 'GET', credentials: 'include', redirect: 'manual' })
    // 2xx / 3xx 视为控制台可达（登录页或重定向）
    if (resp.status >= 200 && resp.status < 400) {
      druidReady.value = true
    } else {
      druidReady.value = false
      probeError.value = `HTTP ${resp.status}`
    }
  } catch (e) {
    druidReady.value = false
    probeError.value = e?.message || '网络错误'
  } finally {
    probing.value = false
  }
}

function goDiagnosis() {
  router.push('/monitor/diagnosis').catch(() => {
    router.push({ path: '/monitor/diagnosis' })
  })
}

onMounted(() => {
  probeDruid()
})
</script>

<style scoped>
.druid-page {
  padding: 12px;
}
.druid-alert {
  max-width: 920px;
}
.druid-alert p {
  margin: 8px 0;
  line-height: 1.6;
}
.probe-error {
  color: var(--el-color-warning-dark-2);
  font-size: 13px;
}
</style>
