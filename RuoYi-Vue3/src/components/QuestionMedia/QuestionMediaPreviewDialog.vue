<script setup>
import { computed } from 'vue'
import ChoiceQuestionPreview from './ChoiceQuestionPreview.vue'
const props=defineProps({ question:Object,context:{type:Object,default:()=>({})} })
const emit=defineEmits(['close'])
const normalized=computed(()=>props.question?{...props.question,...Object.fromEntries(['A','B','C','D'].map(k=>['option'+k,props.question['option'+k]??props.question.optionContents?.[k]]))}:null)
</script>
<template><el-dialog :model-value="!!question" title="图文题目预览" width="min(780px, 95vw)" append-to-body destroy-on-close @update:model-value="value => { if(!value) emit('close') }"><ChoiceQuestionPreview v-if="normalized" :question="normalized" :context="context" show-answer /></el-dialog></template>
