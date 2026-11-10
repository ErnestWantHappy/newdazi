<template>
  <div class="python-submission-preview">
    <div class="preview-header">
      <div class="header-info">
        <div class="title-line">
          <span class="student-label">{{ student?.studentName || '学生' }} 的提交作品</span>
          <el-tag type="primary" effect="dark" size="small">Python 在线编程</el-tag>
          <el-tag v-if="systemStatusText" :type="systemStatusType" size="small">{{ systemStatusText }}</el-tag>
        </div>
        <div class="question-line" v-if="question?.questionContent">
          <span class="question-label">题目</span>
          <span class="question-text">{{ question.questionContent }}</span>
        </div>
      </div>
      <div class="preview-actions">
        <el-button link type="primary" :disabled="!sourceCode" @click="copySource">复制代码</el-button>
        <el-button link type="primary" :disabled="!sourceCode" @click="downloadSource">下载 .py</el-button>
        <el-button
          link
          type="primary"
          :disabled="!sourceCode"
          @click="openRun"
        >{{ runVisible ? '收起运行结果' : '运行查看' }}</el-button>
      </div>
    </div>

    <div v-if="hasSystemResult" class="system-result-bar">
      <div class="result-main">
        <span class="result-label">系统判题</span>
        <strong class="result-score">{{ systemScoreText }}</strong>
        <span class="result-max">/ {{ maxScore }} 分</span>
      </div>
      <div class="result-meta">
        <span v-if="hasCaseResult">通过用例 <b>{{ passedCaseCount }}</b> / {{ totalCaseCount }}</span>
        <span v-if="student?.systemTimeSeconds != null">用时 {{ formatSeconds(student.systemTimeSeconds) }}</span>
        <span v-if="student?.systemMemoryKb != null">内存 {{ formatMemory(student.systemMemoryKb) }}</span>
        <span v-if="student?.systemJudgedAt">判题时间 {{ student.systemJudgedAt }}</span>
      </div>
    </div>
    <el-alert
      v-else-if="sourceCode"
      title="未找到该生最近一次系统判题记录，仍可按代码人工评分。"
      type="info"
      :closable="false"
      show-icon
    />

    <el-empty v-if="!sourceCode.trim()" description="该生尚未提交 Python 代码" />
    <template v-else>
      <section v-if="runVisible" class="run-panel" aria-label="Python 作品运行">
        <div class="run-head">
          <strong>运行查看</strong>
          <span>运行结果仅供人工评分参考，不写入成绩</span>
        </div>
        <!-- 与学生端同一个终端：高版本浏览器本机运行，其余自动走服务器，交互方式一致 -->
        <python-terminal
          ref="terminal"
          :code="sourceCode"
          :input-count="inputCount"
          :server-run="serverRun"
          note="这段程序会在运行时提示你需要输入什么；按提示逐行填写即可，不需要一次填完。"
        />
      </section>
      <div class="code-panel">
      <div class="code-title">
        <span>main.py</span>
        <span>{{ lineCount }} 行</span>
      </div>
      <div class="code-scroll" role="region" aria-label="学生 Python 代码">
        <div v-for="(line, index) in codeLines" :key="index" class="code-row">
          <span class="line-no">{{ index + 1 }}</span>
          <span class="line-code">{{ line || ' ' }}</span>
        </div>
      </div>
    </div>
    </template>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { runPythonSubmission } from '@/api/business/teacherGrading';
import PythonTerminal from '@/components/PythonRunner/PythonTerminal.vue';
import { guessInputCount } from '@/components/PythonRunner/serverInteractive';

const props = defineProps({
  student: { type: Object, required: true },
  question: { type: Object, default: null }
});

// 系统分与代码必须来自同一条最高分提交；答题表仅作历史兼容回退。
const sourceCode = computed(() => String(
  props.student?.systemSourceCode ?? props.student?.studentAnswer ?? ''
));
const codeLines = computed(() => sourceCode.value.split(/\r?\n/));
const lineCount = computed(() => codeLines.value.length);
const maxScore = computed(() => Number(props.student?.maxScore ?? 0));
const hasSystemResult = computed(() => Boolean(
  props.student?.systemStatusCode
  || props.student?.systemScore != null
  || props.student?.systemSubmissionId != null
));
const hasCaseResult = computed(() => Number(props.student?.systemTotalCaseCount || 0) > 0);
const passedCaseCount = computed(() => Number(props.student?.systemPassedCaseCount || 0));
const totalCaseCount = computed(() => Number(props.student?.systemTotalCaseCount || 0));
const systemScoreText = computed(() => props.student?.systemScore == null ? '--' : props.student.systemScore);
const systemStatusText = computed(() => statusLabel(props.student?.systemStatusCode));
const systemStatusType = computed(() => statusTagType(props.student?.systemStatusCode));
const runVisible = ref(false);
const terminal = ref(null);
// 只在文案里提示会输入几次，不是判题依据
const inputCount = computed(() => guessInputCount(sourceCode.value));

watch(() => props.student?.systemSubmissionId, () => {
  runVisible.value = false;
  terminal.value?.reset();
});

function openRun() {
  if (!props.student?.systemSubmissionId) {
    ElMessage.warning('未找到可运行的系统提交记录，请下载后运行');
    return;
  }
  // 再点一次收起，避免教师找不到返回代码列表的入口
  runVisible.value = !runVisible.value;
}

async function run() {
  terminal.value?.start();
}

/** 服务器运行：把累计输入交给后端，由后端按真实提示语回放终端文本。 */
function serverRun(payload) {
  if (!props.student?.systemSubmissionId) throw new Error('未找到可运行的系统提交记录');
  return runPythonSubmission(props.student.systemSubmissionId, payload?.stdin ?? '');
}

function statusLabel(status) {
  return ({
    ACCEPTED: '全部通过',
    PARTIAL: '部分通过',
    WRONG_ANSWER: '答案错误',
    SYNTAX_ERROR: '语法错误',
    RUNTIME_ERROR: '运行错误',
    TIME_LIMIT: '运行超时',
    MEMORY_LIMIT: '内存超限',
    SERVICE_ERROR: '判题服务异常',
    WAITING: '等待判题',
    JUDGING: '判题中',
    CANCELLED: '已取消'
  })[String(status || '').toUpperCase()] || '';
}

function statusTagType(status) {
  const value = String(status || '').toUpperCase();
  if (value === 'ACCEPTED') return 'success';
  if (value === 'PARTIAL') return 'warning';
  if (['SERVICE_ERROR', 'WAITING', 'JUDGING', 'CANCELLED'].includes(value)) return 'info';
  return value ? 'danger' : 'info';
}

function formatSeconds(value) {
  const number = Number(value);
  return Number.isFinite(number) ? `${number.toFixed(3)}s` : '--';
}

function formatMemory(value) {
  const number = Number(value);
  if (!Number.isFinite(number)) return '--';
  return number >= 1024 ? `${(number / 1024).toFixed(1)}MB` : `${number}KB`;
}

async function copySource() {
  if (!sourceCode.value) return;
  try {
    await navigator.clipboard.writeText(sourceCode.value);
    ElMessage.success('代码已复制');
  } catch (_) {
    ElMessage.warning('当前浏览器不允许自动复制，请手动选择代码');
  }
}

function downloadSource() {
  if (!sourceCode.value) return;
  const blob = new Blob([sourceCode.value], { type: 'text/x-python;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  const safeName = String(props.student?.studentName || 'student').replace(/[\\/:*?"<>|]/g, '_');
  link.href = url;
  link.download = `${safeName}_python.py`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}
</script>

<style scoped>
.python-submission-preview {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 15px 15px;
  overflow: hidden;
}
.preview-header {
  flex: none;
  padding-top: 10px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}
.header-info { min-width: 0; display: flex; flex-direction: column; gap: 6px; }
.title-line { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.student-label { font-weight: 700; font-size: 14px; color: #303133; }
.question-line { display: flex; align-items: baseline; gap: 8px; min-width: 0; }
.question-label { flex: none; font-size: 12px; color: #909399; }
.question-text { min-width: 0; color: #606266; font-size: 12px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.preview-actions { flex: none; display: flex; align-items: center; gap: 10px; }
.system-result-bar {
  flex: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 10px 14px;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}
.result-main { display: flex; align-items: baseline; gap: 6px; }
.result-label { color: #606266; font-size: 13px; }
.result-score { color: #303133; font-size: 22px; line-height: 1; }
.result-max { color: #909399; font-size: 13px; }
.result-meta { display: flex; align-items: center; justify-content: flex-end; flex-wrap: wrap; gap: 14px; color: #606266; font-size: 12px; }
.result-meta b { color: #303133; }
.run-panel {
  flex: none;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px 12px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background: #f8fafc;
}
.run-head { display: flex; align-items: center; justify-content: space-between; gap: 12px; color: #303133; }
.run-head span { color: #909399; font-size: 12px; }
.code-panel {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #0f172a;
}
.code-title {
  flex: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 7px 12px;
  background: #1e293b;
  color: #cbd5e1;
  font-size: 12px;
}
.code-scroll { flex: 1; min-height: 0; overflow: auto; }
.code-row {
  display: flex;
  min-width: max-content;
  line-height: 1.65;
  font-family: Consolas, 'Courier New', monospace;
  font-size: 13px;
}
.line-no {
  position: sticky;
  left: 0;
  z-index: 1;
  flex: none;
  width: 48px;
  padding-right: 12px;
  text-align: right;
  color: #64748b;
  background: #0f172a;
  user-select: none;
}
.line-code { white-space: pre; padding-right: 20px; color: #e2e8f0; }

@media (max-width: 600px) {
  .preview-header {
    flex-wrap: wrap;
    gap: 6px;
  }

  .system-result-bar {
    align-items: flex-start;
    flex-direction: column;
    gap: 6px;
  }

  .result-meta {
    justify-content: flex-start;
    gap: 6px 12px;
  }
}
</style>
