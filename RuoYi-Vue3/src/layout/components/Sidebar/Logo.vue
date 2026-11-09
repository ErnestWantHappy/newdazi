<template>
  <div class="sidebar-logo-container" :class="{ 'collapse': collapse }">
    <transition name="sidebarLogoFade">
      <div v-if="collapse" key="collapse" class="sidebar-logo-link">
        <PlatformLogo
          :src="logo"
          class="sidebar-logo"
          data-brand-logo-target
          alt="信息科技学业测评平台 Logo"
        />
      </div>
      <div v-else key="expand" class="sidebar-logo-link">
        <PlatformLogo
          :src="logo"
          class="sidebar-logo"
          data-brand-logo-target
          alt="信息科技学业测评平台 Logo"
        />
        <router-link class="sidebar-title-link" to="/">
          <h1 class="sidebar-title">{{ title }}</h1>
        </router-link>
      </div>
    </transition>
  </div>
</template>

<script setup>
import logo from '@/assets/logo/logo.png'
import PlatformLogo from '@/components/PlatformLogo/index.vue'
import useSettingsStore from '@/store/modules/settings'
import variables from '@/assets/styles/variables.module.scss'

defineProps({
  collapse: {
    type: Boolean,
    required: true
  }
})

const title = import.meta.env.VITE_APP_TITLE
const settingsStore = useSettingsStore()
const sideTheme = computed(() => settingsStore.sideTheme)

// 获取Logo背景色
const getLogoBackground = computed(() => {
  if (settingsStore.isDark) {
    return 'var(--sidebar-bg)'
  }
  return sideTheme.value === 'theme-dark' ? variables.menuBg : variables.menuLightBg
})

// 获取Logo文字颜色
const getLogoTextColor = computed(() => {
  if (settingsStore.isDark) {
    return 'var(--sidebar-text)'
  }
  return sideTheme.value === 'theme-dark' ? '#fff' : variables.menuLightText
})
</script>

<style lang="scss" scoped>
.sidebarLogoFade-enter-active {
  transition: opacity 1.5s;
}

.sidebarLogoFade-enter,
.sidebarLogoFade-leave-to {
  opacity: 0;
}

.sidebar-logo-container {
  position: relative;
  width: 100%;
  height: 50px;
  line-height: 50px;
  background: v-bind(getLogoBackground);
  text-align: center;
  overflow: hidden;

  & .sidebar-logo-link {
    display: flex;
    width: 100%;
    height: 100%;
    align-items: center;
    justify-content: center;

    & .sidebar-logo {
      width: 32px;
      height: 32px;
      vertical-align: middle;
      margin-right: 12px;
    }

    & .sidebar-title-link {
      display: inline-flex;
      height: 100%;
      align-items: center;
      text-decoration: none;
    }

    & .sidebar-title {
      display: inline-block;
      margin: 0;
      color: v-bind(getLogoTextColor);
      font-weight: 600;
      line-height: 50px;
      font-size: 14px;
      font-family: Avenir, Helvetica Neue, Arial, Helvetica, sans-serif;
      vertical-align: middle;
    }
  }

  &.collapse {
    .sidebar-logo {
      margin-right: 0px;
    }
  }
}
</style>
