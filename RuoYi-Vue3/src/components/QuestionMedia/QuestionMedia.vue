<script setup>
import { computed } from 'vue'
import QuestionImage from './QuestionImage.vue'
import { slotImages } from '@/utils/questionMedia'
const props = defineProps({ question: { type: Object, required: true }, slot: { type: String, default: 'stem' }, context: { type: Object, default: () => ({}) }, compact: Boolean })
const images = computed(() => slotImages(props.question, props.slot))
const imageContext = computed(() => ({ questionId: props.question.questionId, ...props.context }))
</script>
<template>
  <div v-if="images.length" class="question-media" :class="{ compact }">
    <QuestionImage v-for="(image,index) in compact ? images.slice(0,1) : images" :key="`${image.imageId}:${index}`" :image-id="image.imageId" :context="imageContext" :compact="compact" :alt="slot === 'stem' ? `题干图${index+1}` : `选项${slot}图片`" />
  </div>
</template>
<style scoped>.question-media{display:grid;gap:10px;width:100%;margin:10px 0}.question-media.compact{display:flex;width:auto;margin:0;flex:none}</style>
