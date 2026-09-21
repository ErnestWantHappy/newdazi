<template><div ref="host" :style="{ height: `${height}px`, width: '100%' }" role="img" :aria-label="label" /></template>
<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
const props = defineProps({ option: Object, height: { type: Number, default: 280 }, label: String })
const emit = defineEmits(['select'])
const host = ref(null)
let chart, observer
onMounted(() => {
  chart = echarts.init(host.value)
  chart.on('click', item => emit('select', item))
  chart.setOption({ ...props.option, animation: false }, true)
  observer = new ResizeObserver(() => chart?.resize())
  observer.observe(host.value)
})
watch(() => props.option, option => chart?.setOption({ ...option, animation: false }, true), { deep: true })
onBeforeUnmount(() => { observer?.disconnect(); chart?.dispose(); chart = null })
</script>
