<script setup>
import { ref } from 'vue'
import QuestionMediaInput from './QuestionMediaInput.vue'
import { normalizeQuestionMedia } from '@/utils/questionMedia'
const props = defineProps({ question: { type: Object, required: true }, disabled: Boolean })
const emit = defineEmits(['update:question','blocked'])
const inputs = {}, blocked = ref({})
function text(key,value) { emit('update:question', { ...props.question, [`option${key}`]: value }) }
function pictures(key,value) { const media=normalizeQuestionMedia(props.question.media);media.options[key]=value;emit('update:question',{...props.question,media}) }
function uploadState(key,value) { blocked.value={...blocked.value,[key]:value};emit('blocked',Object.values(blocked.value).some(Boolean)) }
</script>
<template>
  <el-form-item v-for="key in ['A','B','C','D']" :key="key" :label="`选项${key}`" :prop="`option${key}`">
    <div class="choice-option-editor" @paste="inputs[key]?.paste($event)" @dragover.prevent @drop="inputs[key]?.drop($event)">
      <el-input :model-value="question['option'+key]" :disabled="disabled" placeholder="填写文字或添加图片" maxlength="255" @update:model-value="text(key,$event)" />
      <QuestionMediaInput :ref="el => inputs[key]=el" :model-value="question.media?.options?.[key] || []" :disabled="disabled" :limit="1" compact :label="`选项${key}图片`" :context="{ questionId: question.questionId }" @update:model-value="pictures(key,$event)" @blocked="uploadState(key,$event)" />
    </div>
  </el-form-item>
</template>
<style scoped>.choice-option-editor{display:flex;gap:10px;align-items:center;min-width:0;width:100%}.choice-option-editor>.el-input{flex:1;min-width:100px}.choice-option-editor>.media-input{flex:0 1 auto;min-width:56px;max-width:260px}@media(max-width:600px){.choice-option-editor{gap:6px;flex-wrap:wrap}.choice-option-editor>.el-input{flex:1 1 120px}}</style>
