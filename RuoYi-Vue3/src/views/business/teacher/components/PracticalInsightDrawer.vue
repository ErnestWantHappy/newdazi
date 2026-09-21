<template>
  <el-drawer :model-value="true" title="班级 AI 学情诊断" size="min(1440px, 100%)" @close="$emit('close')">
    <div class="insight">
      <div class="scope"><strong>{{ scope.lessonTitle }}</strong> · {{ scope.classCode }}班 · {{ scope.questionTitle }}</div>
      <el-collapse v-model="settingsOpen"><el-collapse-item title="分析设置与运行记录" name="settings"><el-form inline class="toolbar" @submit.prevent>
        <el-form-item label="测评年级"><el-select v-model="form.grade" placeholder="请选择" style="width: 110px"><el-option v-for="n in 12" :key="n" :value="n" :label="`${n}年级`" /></el-select></el-form-item>
        <el-form-item label="学科"><el-input v-model="form.subject" maxlength="40" style="width: 120px" /></el-form-item>
        <el-form-item label="课时"><el-input-number v-model="form.lessonMinutes" :min="10" :max="120" controls-position="right" style="width: 100px" /></el-form-item>
        <el-button type="primary" :icon="DataAnalysis" :loading="busy" :disabled="!data?.enabled || running || !form.grade" @click="generate">生成诊断</el-button>
        <el-button v-if="running" :icon="Close" :disabled="busy" @click="action(cancelInsight)">取消剩余分析</el-button>
        <el-button v-if="retryable" :disabled="busy || data.stale" @click="action(retryInsight)">重试未完成部分</el-button>
      </el-form><p v-if="job" class="muted">{{ statusNames[job.status] }} · Token折算约 {{ job.estimatedCostYuan ?? '待确认' }} 元；费用软上限 {{ data?.maxCostYuan }} 元，在途请求可能超出。</p></el-collapse-item></el-collapse>
      <div class="actions"><el-button v-if="error" :icon="Refresh" @click="load">重试读取</el-button><el-button v-if="report && data?.stale" type="primary" :loading="busy" :disabled="running" @click="generate">更新诊断</el-button></div>
      <el-alert v-if="error" :title="error" type="error" :closable="false" />
      <el-alert v-if="data && !data.enabled" :title="data.message" type="info" :closable="false" />
      <div v-if="running || busy" class="progress" role="status" aria-live="polite">
        <strong>{{ busy && !running ? '正在提交分析请求' : statusNames[job.stage] || '分析中' }}</strong>
        <template v-if="running"><span>已处理 {{ job.completed + job.failed }}/{{ job.total }} · 待核对 {{ report?.reviewCount || 0 }} · 失败 {{ job.failed }} · 剩余 {{ Math.max(0, job.total - job.completed - job.failed) }}</span>
          <el-progress :percentage="Math.min(100, Math.round((job.completed + job.failed) / Math.max(job.total, 1) * 100))" />
          <span>已用时 {{ elapsed }} · 最近进展 {{ progressTime }} · 约 {{ job.estimatedCostYuan ?? '待确认' }} 元</span>
          <el-button :icon="Close" :disabled="busy" @click="action(cancelInsight)">取消剩余分析</el-button>
        </template>
      </div>
      <el-alert v-if="job?.errorMessage" :title="job.errorMessage" type="warning" :closable="false" />
      <el-alert v-if="data?.stale" title="材料或成绩已变化，以下为历史报告，请重新生成。" type="warning" :closable="false" />
      <template v-if="report">
        <el-alert v-if="!running" :title="report.coverage.sufficient ? '样本达到展示门槛，各知识点仍按有效证据判断' : '样本不足，当前只能作为初步诊断'" :description="`有效 ${report.coverage.valid}/${report.coverage.total} 人（${report.coverage.rate ?? 0}%），正式确认 ${report.coverage.confirmed} 人。${report.coverage.sufficient ? '' : '不能据此推断全班共性；至少10人且覆盖80%。'}`" :type="report.coverage.sufficient ? 'success' : 'warning'" :closable="false" show-icon />
        <section class="paper-summary"><h3>这张卷子考什么</h3><p>{{ brief(report.paper.summary, 130) }}</p></section>
        <el-alert v-if="referenceIssues.length" type="warning" :closable="false" :title="`有 ${referenceIssues.length} 个作答位的参考答案待核对，争议证据不参与判错`"><el-collapse><el-collapse-item title="查看参考答案疑点"><button v-for="item in referenceIssues" :key="item.questionId + item.partId" class="student-link" @click="openReferenceIssue(item)">{{ item.number }} · {{ item.prompt || item.partId }}：{{ item.issue }}</button></el-collapse-item></el-collapse></el-alert>
        <div class="overview">
          <section><h3>{{ scopeLabel }}薄弱知识点</h3><button v-for="k in report.weaknesses.slice(0, 3)" :key="k.id" class="student-link" @click="openKnowledge(k.id)">{{ k.name }}<small>{{ k.partial + k.misconception }}/{{ k.valid }} 人需巩固 · {{ k.sufficient ? '本次班级' : '仅样本' }}</small></button><p v-if="!report.weaknesses.length" class="muted">暂无可确认薄弱点</p></section>
          <section><h3>高频错误 TOP</h3><button v-for="e in report.errorStats.slice(0, 3)" :key="e.id" class="student-link" @click="openError(e)">{{ brief(e.name, 36) }}<small>{{ e.count }}/{{ e.denominator }} 人 · {{ e.common ? '本次共性问题' : '样本内观察' }}</small></button><p v-if="!report.errorStats.length" class="muted">暂无可确认错误</p></section>
          <section><h3>重点关注 {{ report.focusStudents.length }} 人</h3><button v-for="s in report.focusStudents.slice(0, 3)" :key="s.studentId" class="student-link" @click="openStudent(s.studentId)">{{ s.name }}<small>{{ brief(s.reasons[0], 48) }}</small></button><p v-if="!report.focusStudents.length" class="muted">暂无额外观察标记</p></section>
          <section><h3>下一节课优先</h3><p>{{ brief(report.teaching?.activities?.[0]?.objective || '先补充证据，核对作答', 95) }}</p><p v-if="!report.coverage.sufficient" class="muted">暂定建议，先验证再决定全班补教</p><el-button v-if="report.teaching?.activities?.length" link type="primary" @click="showTeaching">查看讲法与练习</el-button></section>
        </div>
        <section class="band"><h3>知识点掌握情况</h3>
          <p class="muted">比例仅按该知识点有效作答计算，空白和证据不足不计入错误。</p>
          <InsightChart :option="knowledgeChart" :height="Math.max(250, report.knowledgeStats.length * 42 + 65)" label="知识点掌握度及有效样本" @select="selectKnowledge" />
          <el-collapse><el-collapse-item title="查看知识点人数"><el-table :data="knowledgeRows" size="small" @row-click="row => openKnowledge(row.id)"><el-table-column prop="name" label="知识点" min-width="180" /><el-table-column label="有效 / 全班" width="115"><template #default="{ row }">{{ row.valid }}/{{ report.coverage.total }}</template></el-table-column><el-table-column label="掌握比例" width="110"><template #default="{ row }">{{ row.masteryRate == null ? '证据不足' : row.masteryRate + '%' }}</template></el-table-column><el-table-column label="判断范围" width="105"><template #default="{ row }">{{ row.sufficient ? '本次班级' : '仅当前样本' }}</template></el-table-column></el-table></el-collapse-item></el-collapse>
          <el-collapse><el-collapse-item title="查看题干、参考答案与知识点依据" name="paper"><p>{{ report.paper.summary }}</p><article v-for="q in report.paper.questions" :key="q.id"><h4>{{ q.number || q.id }}</h4><p>{{ q.stem }}</p><pre v-if="q.codeContext">{{ q.codeContext }}</pre><p v-for="(o, i) in q.options || []" :key="i">{{ o }}</p><p>参考答案：{{ q.referenceAnswer }}</p></article><p v-for="k in report.paper.knowledgePoints" :key="k.id">{{ k.name }}：{{ k.rationale }}</p></el-collapse-item></el-collapse>
        </section>
        <section class="band"><h3>哪些错误最值得先讲？</h3><InsightChart v-if="report.errorStats.length" :option="errorChart" label="已分析样本高频错误人数" @select="selectError" /><el-empty v-else description="暂无可确认的高频错误" :image-size="55" /><p class="muted">按具体错误模式统计涉及人数；同一学生跨题重复另计次数。样本不足时不称为班级共性。</p></section>
        <section class="band"><h3>学生与知识点</h3><div class="filters"><el-select v-model="selectedKnowledge" clearable placeholder="全部知识点" style="width: 260px"><el-option v-for="k in report.knowledgeStats" :key="k.id" :label="k.name" :value="k.id" /></el-select><el-checkbox v-model="focusOnly">仅重点观察</el-checkbox></div>
          <p class="muted">仅列正式确认的 {{ report.coverage.confirmed }} 人；其余 {{ report.coverage.total - report.coverage.confirmed }} 人未纳入诊断。空白和证据不足不判为知识错误。</p>
          <div class="matrix"><InsightChart :option="matrixChart" :height="Math.max(310, Math.min(650, matrixRows.length * 28 + 125))" label="学生知识点掌握热力图" @select="selectCell" /></div>
          <el-collapse><el-collapse-item title="学生名单与关注原因"><el-table :data="matrixRows" size="small" max-height="340"><el-table-column prop="name" label="学生" width="95" /><el-table-column label="观察依据" min-width="200"><template #default="{ row }">{{ focusReason(row.studentId) }}</template></el-table-column><el-table-column label="证据" width="85"><template #default="{ row }"><el-button link type="primary" @click="openStudent(row.studentId)">查看原卷</el-button></template></el-table-column></el-table></el-collapse-item></el-collapse>
        </section>
        <section ref="teachingSection" class="band"><h3>{{ report.coverage.sufficient ? '下一节课安排' : '样本补练与补证安排（暂定）' }}</h3><el-alert v-if="report.teachingError" :title="report.teachingError" type="warning" :closable="false" /><el-collapse v-model="teachingOpen"><el-collapse-item v-for="(a, i) in report.teaching?.activities || []" :key="i" :name="i" :title="`${i + 1}. ${a.objective} · ${a.minutes} 分钟`"><p><strong>怎么讲：</strong>{{ a.explanation }}</p><p><strong>基础练习：</strong>{{ a.foundationPractice }}</p><p><strong>提升练习：</strong>{{ a.extensionPractice }}</p><p><strong>5分钟检测：</strong>{{ a.exitQuestion }}</p><p><strong>达标标准：</strong>{{ a.successCriteria }}</p><p><strong>练习答案：</strong>{{ a.referenceAnswer }}</p><p><strong>检测答案：</strong>{{ a.exitAnswer }}</p></el-collapse-item></el-collapse></section>
        <section class="band"><el-collapse><el-collapse-item title="成绩背景与评分追溯"><InsightChart :option="scoreChart" label="正式成绩区间人数" /><el-table :data="report.scoringStats?.points || []" size="small"><el-table-column prop="name" label="评分点（非知识点）" /><el-table-column prop="count" label="人数" /><el-table-column prop="average" label="平均分" /></el-table></el-collapse-item><el-collapse-item title="分析限制"><p v-for="(item, i) in report.limitations" :key="i" class="muted">{{ item }}</p></el-collapse-item></el-collapse></section>
      </template><el-empty v-else-if="data?.enabled && !running" description="尚无诊断报告" />
    </div>
    <el-drawer v-model="evidenceVisible" :title="evidenceTitle" size="min(1000px, 100%)" append-to-body @close="closeEvidence">
      <el-alert v-if="evidenceError" :title="evidenceError" type="error" :closable="false" />
      <el-select v-if="evidenceCandidates.length > 1" :model-value="evidence?.student.studentId" placeholder="相关学生" class="candidate-select" @change="id => loadStudent(id)"><el-option v-for="s in evidenceCandidates" :key="s.studentId" :value="s.studentId" :label="s.name" /></el-select>
      <template v-if="evidence"><h3>{{ evidence.student.name }} · 正式成绩 {{ evidence.student.score }}/{{ evidence.student.maxScore }}</h3>
        <div class="evidence-layout"><div>
          <el-select v-if="evidenceDiagnoses.length" v-model="activeKnowledge" placeholder="知识点" class="candidate-select" @change="locateEvidence"><el-option v-for="d in evidenceDiagnoses" :key="d.knowledgeId" :value="d.knowledgeId" :label="`${knowledgeName(d.knowledgeId)} · ${stateNames[d.state]}`" /></el-select>
          <template v-if="activeDiagnosis"><p>{{ activeDiagnosis.observation }}</p><article v-for="e in activeEvidence" :key="e.id" class="answer-comparison"><h4>{{ question(e.questionId)?.number || e.questionId }} · {{ question(e.questionId)?.parts?.find(p => p.id === e.partId)?.prompt || e.partId }}<el-tag v-if="highlightedIds.includes(e.id)" size="small">当前错误证据</el-tag></h4><p>{{ question(e.questionId)?.stem }}</p><pre v-if="question(e.questionId)?.codeContext">{{ question(e.questionId).codeContext }}</pre><p v-for="(o, i) in question(e.questionId)?.options || []" :key="i">{{ o }}</p><p><strong>学生原答案：</strong>{{ e.status === 'blank' ? '空白' : e.status === 'uncertain' ? `无法确认：${e.text || '需核对原卷'}` : e.text }}</p><p><strong>参考答案：</strong>{{ referenceFor(e) }}</p><el-alert v-if="referenceIssueFor(e)" :title="`参考答案待核对：${referenceIssueFor(e)}`" type="warning" :closable="false" /><el-button link type="primary" @click="showPage(e.page)">原卷第 {{ e.page }} 页</el-button></article>
            <article v-for="(detail, i) in activeErrors" :key="i"><p><strong>具体错误：</strong>{{ detail.observedError }}</p><p><strong>正确做法：</strong>{{ detail.expectedApproach }}</p><p><strong>可能原因（待验证）：</strong>{{ detail.causeHypothesis }}</p><p><strong>老师可以追问：</strong>{{ detail.verificationQuestion }}</p></article>
            <el-collapse><el-collapse-item title="完整 AI 解释与评分依据"><p>{{ activeDiagnosis.causeHypothesis || '暂无更多解释' }}</p><p v-if="activeDiagnosis.comparability">可比依据：{{ activeDiagnosis.comparability }}</p><pre>{{ evidence.student.scoringDetails }}</pre></el-collapse-item></el-collapse>
          </template><el-empty v-else description="本份证据尚未完成" />
        </div><div class="original-page"><el-pagination v-if="evidence.student.pageCount" v-model:current-page="page" :total="evidence.student.pageCount" :page-size="1" layout="prev, pager, next" @current-change="showPage" /><img v-if="pageUrl" :src="pageUrl" class="paper-image" alt="当前学生原卷页" /><p v-else class="muted">{{ evidence.student.pageCount ? '正在读取原卷…' : '当前没有可读取的原卷页' }}</p></div></div>
      </template><el-empty v-else-if="!evidenceCandidates.length" description="暂无相关学生作答证据" />
    </el-drawer>
  </el-drawer>
</template>
<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { Refresh, DataAnalysis, Close } from '@element-plus/icons-vue'
import { latestInsight, createInsight, readInsight, retryInsight, cancelInsight, insightStudent, insightPage } from '@/api/business/paperScan'
import InsightChart from './InsightChart.vue'
const props = defineProps({ scope: { type: Object, required: true } })
defineEmits(['close'])
const scope = { ...props.scope }, form = ref({ grade: null, subject: '信息科技', lessonMinutes: 40 })
const data = ref(null), error = ref(''), busy = ref(false), selectedKnowledge = ref(''), focusOnly = ref(false)
const evidenceVisible = ref(false), evidence = ref(null), evidenceError = ref(''), page = ref(1), pageUrl = ref('')
const settingsOpen = ref([]), teachingOpen = ref([]), teachingSection = ref(null)
const evidenceContext = ref({}), evidenceCandidates = ref([]), activeKnowledge = ref('')
let timer, disposed = false, evidenceSeq = 0, imageSeq = 0
const job = computed(() => data.value?.job), report = computed(() => data.value?.report)
const running = computed(() => ['PENDING', 'RUNNING'].includes(job.value?.status))
const retryable = computed(() => ['FAILED', 'PARTIAL', 'INTERRUPTED', 'CANCELLED'].includes(job.value?.status))
const scopeLabel = computed(() => report.value?.coverage.sufficient ? '优先核对' : '样本内')
const stateNames = { MASTERED: '掌握', PARTIAL: '部分掌握', MISCONCEPTION: '存在错误', CONFLICT: '表现矛盾', BLANK: '空白', INSUFFICIENT: '证据不足', NOT_COVERED: '未覆盖' }
const states = Object.keys(stateNames), colors = ['#389c78', '#e7b44b', '#d66969', '#986cb3', '#9ca3af', '#d9dee5', '#eef0f3']
const statusNames = { PENDING: '等待分析', RUNNING: '分析中', READING_PAPER: '解析试卷知识点与作答位', REUSING_EVIDENCE: '读取已有批改原答', DIAGNOSING_BATCH: '分批诊断已有作答', READING_STUDENTS: '诊断学生作答', PLANNING_LESSON: '编排教学建议', COMPLETED: '诊断完成', PARTIAL: '部分完成', CANCELLED: '已取消', INTERRUPTED: '任务中断', FAILED: '分析失败', STALE: '报告已过期' }
const now = ref(Date.now())
const clock = setInterval(() => { now.value = Date.now() }, 1000)
const progressTime = computed(() => { const date = new Date(job.value?.updatedAt || job.value?.createdAt); return Number.isNaN(date.getTime()) ? '等待更新' : date.toLocaleTimeString('zh-CN', { hour12: false }) })
onBeforeUnmount(() => clearInterval(clock))
const elapsed = computed(() => { const seconds = Math.max(0, Math.floor((now.value - new Date(job.value?.createdAt || now.value).getTime()) / 1000)); return `${Math.floor(seconds / 60)}分${seconds % 60}秒` })
const knowledgeRows = computed(() => report.value?.knowledgeStats || [])
const displayedKnowledge = computed(() => knowledgeRows.value.filter(k => !selectedKnowledge.value || k.id === selectedKnowledge.value))
const matrixRows = computed(() => (report.value?.studentMatrix || []).filter(s => !focusOnly.value || report.value.focusStudents.some(f => f.studentId === s.studentId)))
const tooltip = { trigger: 'item', renderMode: 'richText', confine: true }
const knowledgeChart = computed(() => ({ tooltip: { ...tooltip, formatter: p => { const k = knowledgeRows.value[p.dataIndex]; return `${k.name}\n掌握 ${k.mastered} · 部分 ${k.partial} · 错误 ${k.misconception}\n有效 ${k.valid}/${report.value.coverage.total} 人\n${k.sufficient ? '本次班级' : '仅当前样本'}` } }, legend: { bottom: 0 }, grid: { left: 180, right: 24, top: 15, bottom: 45 }, xAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } }, yAxis: { type: 'category', inverse: true, data: knowledgeRows.value.map(k => k.name), axisLabel: { width: 165, overflow: 'truncate' } }, series: ['mastered', 'partial', 'misconception', 'unknown'].map((key, i) => ({ name: ['掌握', '部分掌握', '存在错误', '无有效证据'][i], type: 'bar', stack: 'rate', itemStyle: { color: [...colors.slice(0, 3), '#d9dee5'][i] }, data: knowledgeRows.value.map(k => i === 3 ? (k.valid ? 0 : 100) : (k.valid ? k[key] / k.valid * 100 : 0)) })) }))
const errorChart = computed(() => ({ tooltip: { ...tooltip, formatter: p => { const e = report.value.errorStats[p.dataIndex]; return `${e.name}\n涉及 ${e.count}/${e.denominator} 人（${e.rate}%）\n发生 ${e.occurrences ?? e.count} 次 · ${e.common ? '本次共性问题' : '样本内观察'}` } }, grid: { left: 190, right: 30, top: 10, bottom: 25 }, xAxis: { type: 'value', minInterval: 1 }, yAxis: { type: 'category', inverse: true, data: report.value.errorStats.slice(0, 5).map(e => e.name), axisLabel: { width: 175, overflow: 'truncate' } }, series: [{ type: 'bar', itemStyle: { color: '#d66969' }, data: report.value.errorStats.slice(0, 5).map(e => e.count), label: { show: true, position: 'right' } }] }))
const scoreChart = computed(() => {
  const counts = [0, 0, 0, 0]
  for (const s of report.value.studentMatrix) { const rate = s.maxScore > 0 ? s.score / s.maxScore : null; counts[rate == null ? 3 : rate < .6 ? 0 : rate < .85 ? 1 : 2]++ }
  return { tooltip, grid: { left: 40, right: 15, top: 15, bottom: 45 }, xAxis: { type: 'category', data: ['不足60%', '60%至不足85%', '85%及以上', '满分未知'], axisLabel: { interval: 0, rotate: 15 } }, yAxis: { type: 'value', minInterval: 1 }, series: [{ type: 'bar', itemStyle: { color: '#6098b8' }, data: counts, label: { show: true, position: 'top' } }] }
})
const matrixChart = computed(() => ({ tooltip: { ...tooltip, formatter: p => `${matrixRows.value[p.value[1]]?.name}\n${displayedKnowledge.value[p.value[0]]?.name}\n${stateNames[states[p.value[2]]]}` }, grid: { left: 90, right: 35, top: 90, bottom: 80 }, xAxis: { type: 'category', position: 'top', data: displayedKnowledge.value.map(k => k.name), axisLabel: { rotate: 22, width: 110, overflow: 'truncate' } }, yAxis: { type: 'category', inverse: true, data: matrixRows.value.map(s => s.name) }, dataZoom: [{ type: 'slider', yAxisIndex: 0, right: 0, start: 0, end: Math.min(100, 18 / Math.max(1, matrixRows.value.length) * 100), filterMode: 'none' }], visualMap: { type: 'piecewise', orient: 'horizontal', bottom: 0, left: 'center', pieces: states.map((s, value) => ({ value, label: stateNames[s], color: colors[value] })) }, series: [{ type: 'heatmap', data: matrixRows.value.flatMap((s, y) => displayedKnowledge.value.map((k, x) => [x, y, states.indexOf(s.cells.find(c => c.knowledgeId === k.id)?.state || 'INSUFFICIENT')])), itemStyle: { borderColor: '#fff', borderWidth: 2 } }] }))
function knowledgeName(id) { return knowledgeRows.value.find(k => k.id === id)?.name || id }
function selectKnowledge(item) { const k = knowledgeRows.value[item.dataIndex]; if (k) openKnowledge(k.id) }
function selectError(item) { const e = report.value.errorStats[item.dataIndex]; if (e) openError(e) }
function selectCell(item) { if (item.value) openStudent(matrixRows.value[item.value[1]].studentId, { knowledgeId: displayedKnowledge.value[item.value[0]].id }) }
function focusReason(id) { return report.value.focusStudents.find(s => s.studentId === id)?.reasons.join('；') || '暂无额外观察标记' }
function brief(text, length) { return text?.length > length ? text.slice(0, length) + '…' : text }
async function showTeaching() { teachingOpen.value = [0]; await nextTick(); teachingSection.value?.scrollIntoView({ behavior: 'smooth', block: 'start' }) }
function accept(result) { if (!disposed) { if (!report.value && result.data?.report) settingsOpen.value = []; data.value = result.data } }
function schedule() { clearTimeout(timer); if (!disposed && running.value) timer = setTimeout(poll, 3000) }
async function poll() { try { accept(await readInsight(job.value.reportId)); error.value = '' } catch (e) { error.value = e.message || '状态读取失败，可刷新接续' } finally { schedule() } }
async function load() { if (busy.value) return; busy.value = true; clearTimeout(timer); try { accept(await latestInsight({ ...scope, ...form.value })); if (data.value.scope) form.value = { grade: data.value.scope.grade, subject: data.value.scope.subject, lessonMinutes: data.value.scope.lessonMinutes }; settingsOpen.value = !report.value ? ['settings'] : []; error.value = '' } catch (e) { error.value = e.message || '读取失败' } finally { busy.value = false; schedule() } }
async function generate() { await execute(() => createInsight({ ...scope, ...form.value })) }
async function action(fn) { await execute(() => fn(job.value.reportId)) }
async function execute(fn) { if (busy.value) return; busy.value = true; clearTimeout(timer); try { accept(await fn()); error.value = '' } catch (e) { error.value = e.message || '请求失败，已有报告已保留' } finally { busy.value = false; schedule() } }
function clearImage() { imageSeq++; if (pageUrl.value) URL.revokeObjectURL(pageUrl.value); pageUrl.value = '' }
const evidenceTitle = computed(() => evidenceContext.value.error?.name || (evidenceContext.value.knowledgeId ? knowledgeName(evidenceContext.value.knowledgeId) : '学生作答证据'))
const evidenceDiagnoses = computed(() => (evidence.value?.result?.diagnoses || []).filter(d => !evidenceContext.value.knowledgeId || d.knowledgeId === evidenceContext.value.knowledgeId))
const activeDiagnosis = computed(() => evidenceDiagnoses.value.find(d => d.knowledgeId === activeKnowledge.value))
const activeErrors = computed(() => (activeDiagnosis.value?.errors || []).filter(e => !evidenceContext.value.error || ((evidenceContext.value.error.examples || []).some(x => x.studentId === evidence.value?.student.studentId && x.questionId === e.questionId && x.evidenceIds?.some(id => e.evidenceIds.includes(id))))))
const highlightedIds = computed(() => activeErrors.value.flatMap(e => e.evidenceIds || []))
const referenceIssues = computed(() => (report.value?.paper.questions || []).flatMap(q => (q.parts || []).filter(p => p.referenceIssue).map(p => ({ questionId: q.id, partId: p.id, number: q.number, prompt: p.prompt, issue: p.referenceIssue }))))
const activeEvidence = computed(() => {
  const contextError = evidenceContext.value.error
  const questionIds = evidenceContext.value.questionId ? [evidenceContext.value.questionId] : contextError?.examples
    ? contextError.examples.filter(x => x.studentId === evidence.value?.student.studentId).map(x => x.questionId)
    : report.value?.paper.knowledgePoints.find(k => k.id === activeKnowledge.value)?.questionIds || []
  return (evidence.value?.result?.evidence || []).filter(e => questionIds.includes(e.questionId))
})
function question(id) { return report.value?.paper.questions.find(q => q.id === id) }
function referenceFor(e) { const q = question(e.questionId); return q?.parts?.find(p => p.id === e.partId)?.referenceAnswer || q?.referenceAnswer || '参考答案待补充' }
function referenceIssueFor(e) { return question(e.questionId)?.parts?.find(p => p.id === e.partId)?.referenceIssue }
function openReferenceIssue(item) {
  const knowledgeId = report.value.paper.knowledgePoints.find(k => k.questionIds.includes(item.questionId))?.id
  openContext({ knowledgeId, questionId: item.questionId, partId: item.partId }, report.value.studentMatrix)
}
function closeEvidence() { evidenceSeq++; clearImage() }
function openKnowledge(id) {
  const order = ['MISCONCEPTION', 'PARTIAL', 'CONFLICT', 'BLANK', 'INSUFFICIENT', 'NOT_COVERED', 'MASTERED']
  const priority = s => order.indexOf(s.cells.find(c => c.knowledgeId === id)?.state)
  openContext({ knowledgeId: id }, [...report.value.studentMatrix].sort((a, b) => priority(a) - priority(b)))
}
function openError(error) { openContext({ knowledgeId: error.knowledgeId, error }, report.value.studentMatrix.filter(s => (error.studentIds || error.examples?.map(e => e.studentId) || []).includes(s.studentId))) }
function openStudent(id, context = {}) { openContext(context, report.value.studentMatrix.filter(s => s.studentId === id)) }
function openContext(context, candidates) {
  evidenceContext.value = context; evidenceCandidates.value = candidates; evidenceVisible.value = true; evidence.value = null; evidenceError.value = ''; evidenceSeq++; clearImage()
  if (candidates.length) loadStudent(candidates[0].studentId)
}
async function loadStudent(id) {
  const seq = ++evidenceSeq; evidence.value = null; evidenceError.value = ''; clearImage()
  try { const r = await insightStudent(job.value.reportId, id); if (disposed || seq !== evidenceSeq || !evidenceVisible.value) return; evidence.value = r.data
    const diagnoses = evidenceDiagnoses.value
    activeKnowledge.value = evidenceContext.value.knowledgeId || diagnoses.find(d => ['CONFLICT', 'MISCONCEPTION', 'PARTIAL', 'BLANK'].includes(d.state))?.knowledgeId || diagnoses[0]?.knowledgeId || ''
    await locateEvidence()
  } catch (e) { if (seq === evidenceSeq) evidenceError.value = e.message || '证据读取失败' }
}
async function locateEvidence() {
  const preferred = activeEvidence.value.find(e => e.partId === evidenceContext.value.partId) || activeEvidence.value.find(e => highlightedIds.value.includes(e.id)) || activeEvidence.value[0]
  if (evidence.value?.student.pageCount) await showPage(preferred?.page || 1)
}
async function showPage(n) { clearImage(); evidenceError.value = ''; const seq = imageSeq; page.value = n; try { const blob = await insightPage(job.value.reportId, evidence.value.student.studentId, n); if (disposed || seq !== imageSeq || !evidenceVisible.value) return; if (!blob.type.startsWith('image/')) throw new Error('原卷读取失败或授权已失效'); pageUrl.value = URL.createObjectURL(blob) } catch (e) { if (seq === imageSeq) evidenceError.value = e.message || '原卷读取失败' } }
onMounted(load)
onBeforeUnmount(() => { disposed = true; evidenceSeq++; clearTimeout(timer); clearImage() })
</script>
<style scoped>
.insight { color: #303840; line-height: 1.6; letter-spacing: 0; }.scope { margin-bottom: 16px; overflow-wrap: anywhere; }.toolbar { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; margin-bottom: 18px; }.toolbar :deep(.el-form-item) { margin: 0; }.progress { display: flex; flex-wrap: wrap; justify-content: space-between; gap: 12px; margin: 16px 0; }.progress :deep(.el-progress) { width: 100%; }.insight :deep(.el-alert) { margin: 10px 0; }
.overview { display: grid; grid-template-columns: 1.3fr 1fr 1fr 1.3fr; gap: 24px; padding: 20px 0; border-bottom: 1px solid #dfe5e9; }.overview section { min-width: 0; }h3 { font-size: 16px; margin: 0 0 12px; }h4 { font-size: 14px; margin: 14px 0 8px; }p { white-space: pre-wrap; overflow-wrap: anywhere; margin: 8px 0; }.knowledge-name { display: inline-block; margin: 3px 10px 3px 0; color: #286b77; font-size: 13px; }.student-link { display: block; text-align: left; background: none; border: 0; color: #35718b; padding: 4px 0; cursor: pointer; line-height: 1.5; }.muted { color: #697680; font-size: 13px; }.band { padding: 24px 0; border-bottom: 1px solid #e3e7ea; }.two-columns { display: grid; grid-template-columns: 1fr 1fr; gap: 32px; }.two-columns section { min-width: 0; }.filters { display: flex; flex-wrap: wrap; gap: 16px; }.activity { border-bottom: 1px solid #e3e7ea; padding: 10px 0 18px; }.paper-image { display: block; width: 100%; height: auto; margin-top: 18px; }
.paper-summary { padding:14px 0 0; }.student-link { width:100%; overflow-wrap:anywhere; font-size:14px; }.student-link small { display:block; color:#697680; font-size:12px; }.student-link:hover { text-decoration:underline; }.actions { margin:10px 0; }.candidate-select { width:100%; margin-bottom:14px; }.evidence-layout { display:grid; grid-template-columns:minmax(0,1fr) minmax(0,1fr); gap:20px; }.evidence-layout > div { min-width:0; }.answer-comparison { border-top:1px solid #dfe5e9; padding:10px 0; }.original-page { align-self:start; position:sticky; top:0; }pre { white-space:pre-wrap; overflow-wrap:anywhere; background:#f5f7f8; padding:10px; font-size:13px; }.insight :deep(.el-collapse-item__header) { height:auto; min-height:44px; line-height:1.5; padding:6px 0; }
@media(max-width: 900px) { .overview { grid-template-columns: 1fr 1fr; }.evidence-layout { grid-template-columns:1fr; }.original-page { position:static; } }@media(max-width: 550px) { .overview { gap:14px; }.overview h3 { font-size:14px; }.student-link { font-size:13px; }.matrix { overflow-x: auto; }.matrix > div { min-width: 650px; } }
</style>
