<template>
  <section class="app-main">
    <router-view v-slot="{ Component, route }">
      <transition name="fade-transform" mode="out-in">
        <keep-alive :include="tagsViewStore.cachedViews">
          <component v-if="!route.meta.link" :is="Component" :key="route.path"/>
        </keep-alive>
      </transition>
    </router-view>
    <iframe-toggle />
    <copyright />
  </section>
</template>

<script setup>
import copyright from "./Copyright/index"
import iframeToggle from "./IframeToggle/index"
import useTagsViewStore from '@/store/modules/tagsView'

const route = useRoute()
const tagsViewStore = useTagsViewStore()

onMounted(() => {
  addIframe()
})

watchEffect(() => {
  addIframe()
})

function addIframe() {
  if (route.meta.link) {
    useTagsViewStore().addIframeView(route)
  }
}
</script>

<style lang="scss" scoped>
.app-main {
  /* 50= navbar  50  */
  min-height: calc(100vh - 50px);
  width: 100%;
  position: relative;
  overflow: hidden;
}

/* 路由懒加载时阻止用户继续看到或操作已经离开的业务页面。 */
:global(html.route-pending .app-main::before) {
  content: "";
  position: absolute;
  inset: 0;
  z-index: 2100;
  background: rgba(255, 255, 255, 0.94);
}

:global(html.route-pending .app-main::after) {
  content: "页面加载中";
  position: absolute;
  top: 42%;
  left: 50%;
  z-index: 2101;
  transform: translate(-50%, -50%);
  padding: 12px 18px 12px 44px;
  border-radius: 4px;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.14);
  color: #606266;
  font-size: 14px;
}

:global(html.route-pending .app-main) {
  pointer-events: none;
}

.fixed-header + .app-main {
  padding-top: 50px;
}

.hasTagsView {
  .app-main {
    /* 84 = navbar + tags-view = 50 + 34 */
    min-height: calc(100vh - 84px);
  }

  .fixed-header + .app-main {
    padding-top: 84px;
  }
}
</style>

<style lang="scss">
// fix css style bug in open el-dialog
.el-popup-parent--hidden {
  .fixed-header {
    padding-right: 6px;
  }
}

::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background-color: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background-color: #c0c0c0;
  border-radius: 3px;
}
</style>

