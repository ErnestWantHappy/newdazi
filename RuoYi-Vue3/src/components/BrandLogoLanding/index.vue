<template>
  <Teleport to="body">
    <div v-if="visible" class="brand-logo-landing" aria-hidden="true">
      <div ref="backdropRef" class="brand-logo-landing__backdrop"></div>
      <img ref="logoRef" :src="logo" alt="" class="brand-logo-landing__logo" />
    </div>
  </Teleport>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import logo from '@/assets/logo/logo.webp'
import { consumeLoginBrandAnimation } from '@/utils/loginBrandAnimation'

const visible = ref(false)
const logoRef = ref(null)
const backdropRef = ref(null)
let startTimer
let finishTimer

onMounted(async () => {
  const pending = consumeLoginBrandAnimation()
  const reduceMotion = window.matchMedia?.('(prefers-reduced-motion: reduce)').matches
  if (!pending || reduceMotion) return

  visible.value = true
  await nextTick()
  const element = logoRef.value
  if (!element) return

  const baseSize = Math.min(
    220,
    Math.max(128, Math.min(window.innerWidth, window.innerHeight) * 0.26)
  )
  element.style.width = `${baseSize}px`
  element.style.height = `${baseSize}px`
  element.style.opacity = '0'
  element.style.transform = 'translate3d(-50%, -50%, 0) scale(0.86)'

  requestAnimationFrame(() => {
    requestAnimationFrame(() => {
      if (!logoRef.value) return
      element.style.transition = 'transform 280ms cubic-bezier(0.2, 0.8, 0.2, 1), opacity 180ms ease-out'
      element.style.opacity = '1'
      element.style.transform = 'translate3d(-50%, -50%, 0) scale(1)'
      // 入场淡入会占用约 0.2 秒，计时 1.4 秒可让肉眼可见的居中停留约为 1.2 秒。
      startTimer = window.setTimeout(() => flyToTarget(element, baseSize), 1400)
    })
  })
})

onBeforeUnmount(() => {
  window.clearTimeout(startTimer)
  window.clearTimeout(finishTimer)
})

function flyToTarget(element, baseSize) {
  if (!logoRef.value) return
  if (backdropRef.value) {
    backdropRef.value.style.transition = 'opacity 780ms ease-out'
    backdropRef.value.style.opacity = '0'
  }
  const rect = document.querySelector('[data-brand-logo-target]')?.getBoundingClientRect()
  const targetVisible = rect
    && rect.width > 0
    && rect.height > 0
    && rect.right > 0
    && rect.bottom > 0
    && rect.left < window.innerWidth
    && rect.top < window.innerHeight
  const target = targetVisible
    ? rect
    : { left: 16, top: 12, width: 32, height: 32 }

  const deltaX = target.left + target.width / 2 - window.innerWidth / 2
  const deltaY = target.top + target.height / 2 - window.innerHeight / 2
  const scale = Math.min(1, Math.max(0.15, target.width / baseSize))

  element.style.transition = 'transform 860ms cubic-bezier(0.32, 0.72, 0.24, 1), opacity 260ms ease-out 560ms'
  element.style.transform = `translate3d(calc(-50% + ${deltaX}px), calc(-50% + ${deltaY}px), 0) scale(${scale})`
  element.style.opacity = '0'
  finishTimer = window.setTimeout(() => {
    visible.value = false
  }, 1000)
}
</script>

<style lang="scss" scoped>
.brand-logo-landing {
  position: fixed;
  inset: 0;
  z-index: 3000;
  overflow: hidden;
  pointer-events: auto;

  &__backdrop {
    position: absolute;
    inset: 0;
    background: linear-gradient(180deg, #f8fbff 0%, #eef5fb 100%);
  }

  &__logo {
    position: absolute;
    top: 50%;
    left: 50%;
    display: block;
    object-fit: contain;
    opacity: 0;
    filter: drop-shadow(0 20px 38px rgba(22, 119, 255, 0.24));
    transform: translate3d(-50%, -50%, 0);
    will-change: transform, opacity;
  }
}
</style>
