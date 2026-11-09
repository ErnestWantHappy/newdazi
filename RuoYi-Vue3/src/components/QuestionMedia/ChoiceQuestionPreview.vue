<script setup>
import QuestionMedia from './QuestionMedia.vue'
import { hasChoiceOption } from '@/utils/questionMedia'
defineProps({ question: { type:Object,required:true }, context: { type:Object,default:()=>({}) }, showAnswer: { type:Boolean,default:false } })
</script>
<template>
  <article class="choice-preview">
    <p class="choice-stem">{{ question.questionContent || '图片选择题' }}</p><QuestionMedia :question="question" :context="context" />
    <template v-if="question.questionType === 'choice'"><div v-for="key in ['A','B','C','D'].filter(k=>hasChoiceOption(question,k))" :key="key" class="choice-preview-option"><b>{{ key }}.</b><div><span>{{ question['option'+key] }}</span><QuestionMedia :question="question" :slot="key" :context="context" /></div></div></template>
    <template v-if="showAnswer"><p class="choice-answer">标准答案：{{ question.answer || '未设置' }}</p><p v-if="question.analysis" class="choice-analysis">解析：{{ question.analysis }}</p></template>
  </article>
</template>
<style scoped>.choice-preview{min-width:0}.choice-stem{line-height:1.8;white-space:pre-wrap}.choice-preview-option{display:flex;gap:12px;border:1px solid #e5e9ef;border-radius:4px;padding:12px;margin:10px 0}.choice-preview-option>b{color:#6e7e91;flex:none}.choice-preview-option>div{flex:1;min-width:0;white-space:pre-wrap}.choice-answer{color:#5e993b;margin-top:15px}.choice-analysis{font-size:13px;line-height:1.7;color:#7f8b99}</style>
