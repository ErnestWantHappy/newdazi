<template>
  <el-dialog :model-value="modelValue" title="我的作答与反馈" width="min(1000px, 96%)" destroy-on-close @close="$emit('update:modelValue', false)">
    <div class="feedback-toolbar">
      <el-select v-model="selectedLesson" placeholder="选择课程" @change="load()">
        <el-option v-for="lesson in lessons" :key="lesson.lessonId" :value="lesson.lessonId" :label="lesson.lessonTitle" />
      </el-select>
      <el-button :loading="loading" @click="refresh()">刷新反馈</el-button>
    </div>
    <div v-loading="loading">
      <el-alert v-if="error" :title="error" type="warning" :closable="false" />
      <el-empty v-else-if="!rows.length && !loading" description="本课还没有作答记录" />
      <el-card v-for="row in rows" :key="row.questionId" class="feedback-row">
        <template #header><strong>{{ row.question }}</strong><el-tag class="score-tag" :type="row.score == null ? 'info' : 'success'">{{ row.submitted === false ? '未提交' : row.score == null ? '待批改' : `${row.score}/${row.maxScore}分` }}</el-tag></template>
        <el-alert v-if="row.taskState === 'RETURNED'" title="老师已退回重交，原成绩暂时保留；请在当前课程更新作品后提交。" type="warning" :closable="false" />
        <template v-if="row.answer != null"><p>我的作答：{{ row.answer }}</p><p v-if="row.reference">参考答案：{{ row.reference }}</p><p v-if="row.analysis">解析：{{ row.analysis }}</p></template>
        <flowchart-editor v-if="row.flowchartDocument" :model-value="row.flowchartDocument" mode="READONLY" :height="400" />
        <div v-if="row.artifact?.attachments?.length" class="work-list">
          <div v-for="attachment in row.artifact.attachments" :key="attachment.attachmentId">
            {{ attachment.originalFileName }}（{{ formatPracticalFileSize(attachment.fileSize) }}）
            <el-button link type="primary" @click="download(attachment)">下载作品</el-button>
            <practical-text-preview v-if="['TEXT', 'HTML'].includes(attachment.fileKind)" :attachment-id="attachment.attachmentId" />
          </div>
        </div>
        <el-table v-if="row.criteria?.length" :data="criterionScores(row)" size="small">
          <el-table-column prop="itemName" label="评分项" /><el-table-column prop="actualScore" label="得分" width="100" />
        </el-table>
        <div v-if="row.aiFeedback" class="personal-ai">
          <strong>老师发布的 AI 学习反馈</strong>
          <p v-if="row.aiFeedback.publishedAt" class="muted">发布时间：{{ row.aiFeedback.publishedAt }}</p>
          <p class="muted">分析时得分 {{ row.aiFeedback.scoreAtAnalysis }}/{{ row.aiFeedback.maxScoreAtAnalysis }}；AI 判断供学习参考，正式成绩以上方为准。</p>
          <el-alert v-if="row.aiFeedback.oldVersion" title="这是旧作品的分析，新作品尚未发布学习反馈。" type="warning" :closable="false" />
          <el-empty v-if="!row.aiFeedback.diagnoses?.length" description="本报告没有可供展示的个人诊断证据" :image-size="50" />
          <div v-for="(diagnosis, index) in row.aiFeedback.diagnoses" :key="index" class="diagnosis">
            <strong>{{ diagnosis.knowledge }}</strong><p>{{ diagnosis.observation }}</p>
            <div v-for="(item, errorIndex) in diagnosis.errors" :key="errorIndex">
              <p v-if="item.observedError">需要检查：{{ item.observedError }}</p>
              <p v-if="item.actualFragment">我的原答：{{ item.actualFragment }}</p>
              <p v-if="item.expectedApproach">改进建议：{{ item.expectedApproach }}</p>
              <p v-if="item.verificationQuestion">再想一想：{{ item.verificationQuestion }}</p>
            </div>
          </div>
          <p v-for="(reason,index) in row.aiFeedback.reviewReasons" :key="index">待核对：{{ reason }}</p>
        </div>
        <p v-else-if="row.questionType === 'practical'" class="muted">老师尚未发布本题的 AI 学习反馈；正式成绩和评分项会及时显示。</p>
      </el-card>
    </div>
  </el-dialog>
</template>
<script setup>
import { formatPracticalFileSize } from '@/utils/practicalFileDisplay';
import { ref, watch, onBeforeUnmount } from 'vue';
import request from '@/utils/request';
import Download from '@/plugins/download';
import FlowchartEditor from '@/components/FlowchartEditor/FlowchartEditor.vue';
import PracticalTextPreview from '@/components/PracticalTextPreview/index.vue';
const props = defineProps({ modelValue: Boolean, lessonId: [Number, String] });
defineEmits(['update:modelValue']);
const lessons = ref([]), selectedLesson = ref(null), rows = ref([]), loading = ref(false), error = ref('');
let sequence = 0, timer, lessonsReady = false;
watch(() => props.modelValue, (visible) => {
  clearInterval(timer); sequence++; rows.value = []; error.value = ''; loading.value = false;
  lessons.value = []; selectedLesson.value = null; lessonsReady = false;
  if (!visible) return;
  refresh();
  timer = setInterval(() => { if (lessonsReady && !loading.value) load(true); }, 15000);
}, { immediate: true });
async function refresh() {
  if (!props.modelValue) return;
  const ticket = ++sequence;
  loading.value = true; error.value = ''; rows.value = []; lessonsReady = false;
  try {
    const response = await request({ url: '/business/student/feedback/lessons' });
    if (ticket !== sequence || !props.modelValue) return;
    lessons.value = response.data || [];
    if (props.lessonId && !lessons.value.some(l => String(l.lessonId) === String(props.lessonId))) lessons.value.unshift({ lessonId: Number(props.lessonId), lessonTitle: '当前课程' });
    if (!lessons.value.some(l => String(l.lessonId) === String(selectedLesson.value))) {
      selectedLesson.value = props.lessonId ? Number(props.lessonId) : lessons.value[0]?.lessonId;
    }
    lessonsReady = true;
    await load();
  } catch (e) {
    if (ticket === sequence && props.modelValue) error.value = '课程反馈读取失败，请点击刷新反馈重试。';
  } finally { if (ticket === sequence) loading.value = false; }
}
async function load(silent = false) {
  if (!selectedLesson.value || !props.modelValue) return;
  const ticket = ++sequence;
  if (!silent) { loading.value = true; error.value = ''; rows.value = []; }
  try {
    const response = await request({ url: '/business/student/feedback', params: { lessonId: selectedLesson.value } });
    if (ticket === sequence && props.modelValue) { rows.value = response.data || []; error.value = ''; }
  } catch (e) { if (ticket === sequence) error.value = '反馈读取失败，请刷新重试。'; }
  finally { if (ticket === sequence) loading.value = false; }
}
function criterionScores(row) {
  return row.criteria.map(item => ({ ...item, actualScore: row.score == null ? '待批改' : row.scoringDetails?.find(d => String(d.itemId) === String(item.itemId))?.score ?? '未分项评分' }));
}
function download(attachment) { Download.zip(`/business/practical-attachment/${attachment.attachmentId}/download`, attachment.originalFileName); }
onBeforeUnmount(() => { sequence++; clearInterval(timer); });
</script>
<style scoped>
.feedback-toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
.feedback-row { margin-bottom: 16px; }
.score-tag { margin-left: 12px; }
.personal-ai { padding: 16px 0; }
.muted { color: #606266; }
.diagnosis { border-top: 1px solid #eee; padding: 12px 0; }
.work-list { padding: 12px 0; }
</style>
