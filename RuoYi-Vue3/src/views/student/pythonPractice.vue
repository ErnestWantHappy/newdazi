<template>
  <div ref="workspace" class="oj-page">
    <header class="oj-header">
      <div class="header-left">
        <el-button text class="list-button" @click="listVisible = true">☰ 题目列表</el-button>
        <span class="header-divider">/</span>
        <strong>{{ questionTitle }}</strong>
      </div>
      <div class="header-actions">
        <el-tag type="success" effect="plain">已通过 {{ passedCount }}/{{ questions.length }}</el-tag>
        <el-button text @click="toggleFullscreen">{{ fullscreen ? '退出全屏' : '全屏' }}</el-button>
        <el-button text @click="$router.push('/student/index')">返回首页</el-button>
      </div>
    </header>

    <el-alert v-if="pageError" type="error" :closable="false" class="page-alert">
      <template #default>{{ pageError }}<el-button text type="primary" @click="loadOverview">重试</el-button></template>
    </el-alert>

    <div v-if="current" v-loading="questionLoading" class="oj-workspace">
      <splitpanes class="main-split" @resized="refreshEditor">
        <pane :size="50" :min-size="28">
          <section class="statement-pane">
            <div class="statement-scroll">
              <div class="problem-heading">
                <h1>{{ questionTitle }}</h1>
                <div class="problem-meta">
                  <el-tag v-if="current.difficulty" size="small" :type="difficultyType(current.difficulty)">{{ difficultyLabel(current.difficulty) }}</el-tag>
                  <el-tag v-for="tag in knowledgeTags" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
                  <span>时间限制：{{ current.time_limit_seconds || 2 }}s</span>
                  <span>内存限制：{{ formatMemory(current.memory_limit_kb) }}</span>
                </div>
              </div>

              <statement-block title="题目描述" :content="current.question_content" />
              <statement-block title="输入格式" :content="current.input_description || (current.no_input === '1' ? '本题没有输入。' : '')" />
              <statement-block title="输出格式" :content="current.output_description" />
              <statement-block v-if="current.constraints_text" title="数据范围" :content="current.constraints_text" />

              <section v-if="current.publicCases?.length" class="statement-section">
                <h2>样例</h2>
                <div v-for="(item, index) in current.publicCases" :key="item.snapshot_case_id" class="sample-card">
                  <div class="sample-title">样例 {{ index + 1 }}</div>
                  <div class="sample-grid">
                    <div><div class="sample-label">输入</div><pre>{{ item.input_text || '（无输入）' }}</pre></div>
                    <div><div class="sample-label">输出</div><pre>{{ item.expected_output }}</pre></div>
                  </div>
                </div>
              </section>
              <statement-block v-if="current.sample_explanation" title="样例解释" :content="current.sample_explanation" />
              <statement-block v-if="current.notes_text" title="提示" :content="current.notes_text" />
            </div>
          </section>
        </pane>

        <pane :size="50" :min-size="30">
          <splitpanes horizontal class="right-split" @resized="refreshEditor">
            <pane :size="62" :min-size="32">
              <section class="code-pane">
                <div class="pane-toolbar">
                  <div><span class="pane-title">&lt;/&gt; 代码</span><span class="draft-state">{{ draftState }}</span></div>
                  <div class="toolbar-actions">
                    <el-button text @click="darkTheme = !darkTheme">{{ darkTheme ? '浅色' : '深色' }}</el-button>
                    <el-select model-value="Python 3" size="small" class="language-select" disabled><el-option label="Python 3" value="Python 3" /></el-select>
                    <el-button :loading="running" @click="runSamples">运行样例</el-button>
                    <el-button type="primary" :loading="submitting" @click="submit">提交</el-button>
                  </div>
                </div>
                <div ref="editorElement" class="code-editor" />
              </section>
            </pane>
            <pane :size="38" :min-size="24">
              <section class="console-pane">
                <el-tabs v-model="consoleTab" class="console-tabs">
                  <el-tab-pane label="自定义测试" name="custom">
                    <div class="custom-console">
                      <div class="console-column">
                        <div class="console-label">输入</div>
                        <el-input v-model="customInput" type="textarea" resize="none" :rows="7" placeholder="按题目输入格式填写；无输入题可留空" />
                      </div>
                      <div class="console-column output-column">
                        <div class="console-label">输出</div>
                        <pre class="console-output">{{ customOutput }}</pre>
                      </div>
                    </div>
                    <div class="console-actions"><el-button type="primary" :loading="customRunning" @click="runCustom">▶ 运行</el-button></div>
                  </el-tab-pane>
                  <el-tab-pane label="样例结果" name="samples">
                    <case-results :result="latestRun" empty-text="运行样例后，在这里查看期望输出与实际输出。" />
                  </el-tab-pane>
                  <el-tab-pane label="判题结果" name="judge">
                    <case-results :result="latestSubmit" empty-text="正式提交后，在这里查看各测试点状态。" />
                  </el-tab-pane>
                  <el-tab-pane label="提交记录" name="history">
                    <el-table :data="history" size="small" height="210" @row-click="showHistoryResult">
                      <el-table-column prop="submitted_at" label="时间" min-width="150" />
                      <el-table-column prop="submit_type" label="类型" width="105"><template #default="scope">{{ submitTypeLabel(scope.row.submit_type) }}</template></el-table-column>
                      <el-table-column prop="status_message" label="状态" min-width="100" />
                      <el-table-column prop="score" label="得分" width="68"><template #default="scope">{{ scope.row.score ?? '-' }}</template></el-table-column>
                    </el-table>
                  </el-tab-pane>
                </el-tabs>
              </section>
            </pane>
          </splitpanes>
        </pane>
      </splitpanes>
    </div>
    <el-empty v-else v-loading="overviewLoading" class="empty-state" description="暂无可练习的 Python 题目" />

    <el-drawer v-model="listVisible" title="我的 Python 题单" size="360px">
      <div class="filter-row"><el-radio-group v-model="filter" size="small"><el-radio-button label="ALL">全部</el-radio-button><el-radio-button label="TODO">未完成</el-radio-button><el-radio-button label="DONE">已完成</el-radio-button><el-radio-button label="WRONG">错题</el-radio-button></el-radio-group></div>
      <div v-for="section in sections" :key="section.key" class="question-section">
        <div class="section-label">{{ section.label }}</div>
        <button v-for="item in section.items" :key="keyOf(item)" class="question-item" :class="{ active: activeKey === keyOf(item) }" @click="selectQuestion(item); listVisible = false">
          <span><b>{{ item.sort_no }}.</b> {{ titleOf(item) }}</span><small :class="statusClass(item)">{{ statusOf(item) }}</small>
        </button>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { autocompletion, completeFromList } from '@codemirror/autocomplete'
import { defaultKeymap, history as cmHistory, historyKeymap, indentWithTab } from '@codemirror/commands'
import { python } from '@codemirror/lang-python'
import { bracketMatching, defaultHighlightStyle, indentOnInput, syntaxHighlighting } from '@codemirror/language'
import { Compartment, EditorState } from '@codemirror/state'
import { oneDark } from '@codemirror/theme-one-dark'
import { EditorView, keymap, lineNumbers } from '@codemirror/view'
import { computed, defineComponent, h, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Pane, Splitpanes } from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'
import { getStudentPracticeOverview, getStudentPracticeQuestion, saveStudentPracticeDraft, submitStudentPractice } from '@/api/business/pythonPractice'

const StatementBlock = defineComponent({
  props: { title: String, content: String },
  setup(props) { return () => props.content ? h('section', { class: 'statement-section' }, [h('h2', props.title), h('div', { class: 'statement-text' }, props.content)]) : null }
})

const CaseResults = defineComponent({
  props: { result: Object, emptyText: String },
  setup(props) {
    return () => {
      if (!props.result) return h('div', { class: 'result-empty' }, props.emptyText)
      const cases = props.result.caseResults || []
      return h('div', { class: 'result-panel' }, [
        h('div', { class: ['result-summary', statusClassName(props.result.status_code)] }, resultTitle(props.result)),
        cases.length ? h('div', { class: 'case-grid' }, cases.map((item, index) => h('div', { class: ['case-card', statusClassName(item.status_code)] }, [
          h('div', { class: 'case-head' }, [h('strong', item.case_name || `测试点 ${index + 1}`), h('span', statusLabel(item.status_code))]),
          h('div', { class: 'case-metrics' }, `${item.time_seconds ?? '-'}s · ${item.memory_kb ?? '-'}KB`),
          item.is_public === '1' && item.expected_output != null ? h('div', { class: 'case-compare' }, [h('div', [h('label', '期望输出'), h('pre', item.expected_output)]), h('div', [h('label', '实际输出'), h('pre', item.output_text || '（无输出）')])]) : null,
          item.error_summary ? h('pre', { class: 'case-error' }, item.error_summary) : null
        ]))) : h('div', { class: 'result-empty' }, '判题结果正在生成，请稍候。')
      ])
    }
  }
})

const workspace = ref(null); const questions = ref([]); const current = ref(null); const sourceCode = ref(''); const history = ref([]); const activeKey = ref(''); const filter = ref('ALL'); const saving = ref(false); const running = ref(false); const customRunning = ref(false); const submitting = ref(false); const overviewLoading = ref(false); const questionLoading = ref(false); const pageError = ref(''); const questionError = ref(''); const draftState = ref('草稿未保存'); const editorElement = ref(null); const listVisible = ref(false); const consoleTab = ref('custom'); const customInput = ref(''); const darkTheme = ref(false); const fullscreen = ref(false); const selectedHistory = ref(null); let editorView; let pollTimer; let pollAttempts = 0
const themeCompartment = new Compartment()
const passedCount = computed(() => questions.value.filter(item => item.progress?.passed_flag === '1').length)
const questionTitle = computed(() => current.value ? titleOf(current.value) : 'Python 练习')
const knowledgeTags = computed(() => String(current.value?.knowledge_points || '').split(/[,，]/).map(item => item.trim()).filter(Boolean))
const filteredQuestions = computed(() => questions.value.filter(item => { const progress = item.progress; if (filter.value === 'DONE') return progress?.passed_flag === '1'; if (filter.value === 'WRONG') return progress && progress.passed_flag !== '1' && progress.submit_count > 0; if (filter.value === 'TODO') return !progress || progress.passed_flag !== '1'; return true }))
const sections = computed(() => {
  const groups = new Map()
  filteredQuestions.value.forEach(item => {
    const key = String(item.source_id)
    if (!groups.has(key)) groups.set(key, { key, label: item.plan_name || 'Python 练习题单', items: [] })
    groups.get(key).items.push(item)
  })
  return [...groups.values()]
})
const latestRun = computed(() => selectedHistory.value?.submit_type === 'RUN' ? selectedHistory.value : history.value.find(item => item.submit_type === 'RUN'))
const latestSubmit = computed(() => selectedHistory.value?.submit_type === 'SUBMIT' ? selectedHistory.value : history.value.find(item => item.submit_type === 'SUBMIT'))
const latestCustom = computed(() => history.value.find(item => item.submit_type === 'CUSTOM_RUN'))
const customOutput = computed(() => latestCustom.value?.caseResults?.[0]?.output_text || latestCustom.value?.caseResults?.[0]?.error_summary || (customRunning.value ? '正在运行…' : '运行结果将在这里显示'))

function keyOf(item) { return `${item.source_type}-${item.source_id}-${item.question_id}` }
function titleOf(item) { return item.question_title || String(item.question_content || '').split(/\r?\n/)[0].replace(/^#+\s*/, '') || `Python 题目 ${item.question_id}` }
function statusOf(item) { if (item.progress?.passed_flag === '1') return '已通过'; if (item.progress?.submit_count) return '待改进'; return '未开始' }
function statusClass(item) { return item.progress?.passed_flag === '1' ? 'accepted' : item.progress?.submit_count ? 'failed' : '' }
function statusClassName(status) { return String(status || '').toLowerCase().replaceAll('_', '-') }
function statusLabel(status) { return ({ ACCEPTED: '通过', PARTIAL: '部分通过', WRONG_ANSWER: '答案错误', SYNTAX_ERROR: '语法错误', RUNTIME_ERROR: '运行时错误', TIME_LIMIT: '运行超时', MEMORY_LIMIT: '内存超限', WAITING: '等待中', JUDGING: '判题中', SERVICE_ERROR: '服务异常', SERVICE_BUSY: '队列繁忙' })[status] || status || '未知' }
function resultTitle(item) { return `${item.status_message || statusLabel(item.status_code)}${item.score == null ? '' : ` · ${item.score} 分`}（${item.passed_case_count || 0}/${item.total_case_count || 0}）` }
function submitTypeLabel(type) { return ({ RUN: '运行样例', SUBMIT: '正式提交', CUSTOM_RUN: '自定义运行' })[type] || type }
function difficultyLabel(value) { return ({ easy: '简单', medium: '中等', hard: '困难', EASY: '简单', MEDIUM: '中等', HARD: '困难' })[value] || value }
function difficultyType(value) { const text = String(value).toLowerCase(); return text === 'hard' ? 'danger' : text === 'medium' ? 'warning' : 'success' }
function formatMemory(kb) { return `${Math.round((Number(kb) || 131072) / 1024)}MB` }

function mountEditor() {
  if (editorView || !editorElement.value) return
  editorView = new EditorView({ state: EditorState.create({ doc: sourceCode.value, extensions: [lineNumbers(), python(), indentOnInput(), bracketMatching(), syntaxHighlighting(defaultHighlightStyle, { fallback: true }), cmHistory(), autocompletion({ override: [completeFromList(['print', 'input', 'if', 'elif', 'else', 'for', 'while', 'range', 'len', 'int', 'float', 'str', 'list', 'dict', 'set', 'sum', 'min', 'max', 'sorted'].map(label => ({ label, type: 'keyword' })))] }), keymap.of([...defaultKeymap, ...historyKeymap, indentWithTab]), themeCompartment.of([]), EditorView.updateListener.of(update => { if (update.docChanged) { sourceCode.value = update.state.doc.toString(); draftState.value = '草稿待保存' } }), EditorView.theme({ '&': { height: '100%', fontSize: '14px' }, '.cm-content': { fontFamily: 'Consolas, "Courier New", monospace', padding: '12px 0' }, '.cm-scroller': { overflow: 'auto' } })] }), parent: editorElement.value })
}
function setCode(value) { sourceCode.value = value || ''; if (editorView && editorView.state.doc.toString() !== sourceCode.value) editorView.dispatch({ changes: { from: 0, to: editorView.state.doc.length, insert: sourceCode.value } }) }
function refreshEditor() { requestAnimationFrame(() => editorView?.requestMeasure()) }
watch(darkTheme, value => editorView?.dispatch({ effects: themeCompartment.reconfigure(value ? oneDark : []) }))

async function loadOverview() { if (overviewLoading.value) return; overviewLoading.value = true; pageError.value = ''; try { const res = await getStudentPracticeOverview(); questions.value = res.data?.questions || []; if (questions.value.length) await selectQuestion(questions.value[0]) } catch (error) { pageError.value = '题单加载失败，请检查网络后重试'; ElMessage.error(pageError.value) } finally { overviewLoading.value = false } }
async function selectQuestion(item) { if (!item) return; clearTimeout(pollTimer); pollAttempts = 0; activeKey.value = keyOf(item); questionError.value = ''; questionLoading.value = true; selectedHistory.value = null; try { const res = await getStudentPracticeQuestion({ sourceType: item.source_type, sourceId: item.source_id, questionId: item.question_id }); current.value = res.data || {}; await nextTick(mountEditor); setCode(current.value.draft?.source_code || current.value.starter_code || ''); draftState.value = current.value.draft ? '草稿已保存' : '草稿未保存'; history.value = current.value.history || []; if (history.value[0]?.status_code === 'WAITING' || history.value[0]?.status_code === 'JUDGING') poll() } catch (error) { questionError.value = '题目加载失败，请重试'; ElMessage.error(questionError.value) } finally { questionLoading.value = false } }
async function saveDraft() { if (!current.value || saving.value) return; saving.value = true; try { await saveStudentPracticeDraft({ sourceType: current.value.source_type, sourceId: current.value.source_id, questionId: current.value.question_id, sourceCode: sourceCode.value }); draftState.value = '草稿已保存' } catch (error) { ElMessage.error('草稿保存失败，请稍后重试'); throw error } finally { saving.value = false } }
async function enqueue(type) { if (!current.value || !sourceCode.value.trim()) return ElMessage.warning('请先输入 Python 代码'); if (type === 'RUN') running.value = true; else if (type === 'CUSTOM_RUN') customRunning.value = true; else submitting.value = true; try { await saveDraft(); const res = await submitStudentPractice({ sourceType: current.value.source_type, sourceId: current.value.source_id, questionId: current.value.question_id, sourceCode: sourceCode.value, submitType: type, customInput: type === 'CUSTOM_RUN' ? customInput.value : undefined }); selectedHistory.value = res.data || {}; consoleTab.value = type === 'SUBMIT' ? 'judge' : type === 'RUN' ? 'samples' : 'custom'; pollAttempts = 0; poll() } catch (error) { ElMessage.error('提交失败，请检查网络后重试') } finally { running.value = false; customRunning.value = false; submitting.value = false } }
function runSamples() { return enqueue('RUN') } function runCustom() { return enqueue('CUSTOM_RUN') } function submit() { return enqueue('SUBMIT') }
async function poll() { clearTimeout(pollTimer); if (pollAttempts >= 30) { ElMessage.warning('判题等待较久，可稍后在提交记录中查看'); return } pollAttempts += 1; pollTimer = setTimeout(async () => { if (!current.value) return; try { const res = await getStudentPracticeQuestion({ sourceType: current.value.source_type, sourceId: current.value.source_id, questionId: current.value.question_id }); history.value = res.data?.history || []; selectedHistory.value = history.value[0] || selectedHistory.value; if (selectedHistory.value?.status_code === 'WAITING' || selectedHistory.value?.status_code === 'JUDGING') poll(); else { const overview = await getStudentPracticeOverview(); questions.value = overview.data?.questions || questions.value } } catch (error) { ElMessage.error('判题结果查询失败，可稍后在提交记录中查看') } }, 700) }
function showHistoryResult(row) { selectedHistory.value = row; consoleTab.value = row.submit_type === 'SUBMIT' ? 'judge' : row.submit_type === 'RUN' ? 'samples' : 'custom' }
async function toggleFullscreen() { if (!document.fullscreenElement) await workspace.value?.requestFullscreen(); else await document.exitFullscreen() }
function onFullscreenChange() { fullscreen.value = !!document.fullscreenElement; refreshEditor() }
onMounted(async () => { document.addEventListener('fullscreenchange', onFullscreenChange); await nextTick(mountEditor); await loadOverview() })
onUnmounted(() => { document.removeEventListener('fullscreenchange', onFullscreenChange); clearTimeout(pollTimer); editorView?.destroy() })
</script>

<style scoped>
.oj-page{height:calc(100vh - 84px);min-height:640px;background:#fff;color:#252b33;overflow:hidden}.oj-page:fullscreen{height:100vh}.oj-header{height:56px;display:flex;align-items:center;justify-content:space-between;padding:0 20px;border-bottom:1px solid #e7e9ed;background:#fff}.header-left,.header-actions,.toolbar-actions,.problem-meta{display:flex;align-items:center;gap:12px}.header-left{min-width:0}.header-left strong{overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.header-divider{color:#c0c4cc}.list-button{font-size:15px}.page-alert{position:absolute;z-index:5;top:58px;left:12px;right:12px}.oj-workspace{height:calc(100% - 56px)}.main-split,.right-split{height:100%}.statement-pane,.code-pane,.console-pane{height:100%;background:#fff;overflow:hidden}.statement-scroll{height:100%;overflow:auto;padding:26px 28px 60px}.problem-heading h1{margin:0 0 10px;font-size:25px}.problem-meta{flex-wrap:wrap;color:#7b818a;font-size:13px}.statement-section{margin-top:26px}.statement-section h2{margin:0 0 12px;font-size:18px}.statement-text{white-space:pre-wrap;word-break:break-word;line-height:1.85;color:#3a4048}.sample-card{margin:12px 0;border:1px solid #e5e7eb;border-radius:7px;overflow:hidden}.sample-title{padding:8px 12px;background:#f7f8fa;font-weight:600}.sample-grid{display:grid;grid-template-columns:1fr 1fr}.sample-grid>div+div{border-left:1px solid #e5e7eb}.sample-label{padding:7px 12px;color:#737983;font-size:13px;border-bottom:1px solid #eceef1}.sample-grid pre,.case-compare pre{min-height:44px;margin:0;padding:11px 12px;overflow:auto;background:#fff;font:13px/1.55 Consolas,"Courier New",monospace;white-space:pre-wrap}.pane-toolbar{height:48px;display:flex;align-items:center;justify-content:space-between;padding:0 12px;border-bottom:1px solid #e6e8eb;background:#fafbfc}.pane-title{font-weight:600}.draft-state{margin-left:12px;color:#909399;font-size:12px}.language-select{width:112px}.code-editor{height:calc(100% - 49px)}.code-editor :deep(.cm-focused){outline:none}.console-tabs{height:100%}.console-tabs :deep(.el-tabs__header){height:42px;margin:0;padding:0 14px}.console-tabs :deep(.el-tabs__content){height:calc(100% - 42px);overflow:auto}.console-tabs :deep(.el-tab-pane){height:100%}.custom-console{display:grid;grid-template-columns:1fr 1fr;height:calc(100% - 48px)}.console-column{padding:10px 12px}.console-column+.console-column{border-left:1px solid #e7e9ed}.console-label{margin-bottom:7px;color:#646b75;font-size:13px}.custom-console :deep(.el-textarea),.custom-console :deep(.el-textarea__inner){height:calc(100% - 24px)}.console-output{height:calc(100% - 24px);margin:0;overflow:auto;white-space:pre-wrap;font:13px/1.55 Consolas,"Courier New",monospace}.console-actions{position:absolute;right:14px;bottom:8px}.result-panel{padding:10px 14px 18px}.result-summary{padding:8px 10px;margin-bottom:10px;border-radius:5px;background:#f5f7fa;font-weight:600}.result-summary.accepted{color:#16803b;background:#eef9f1}.result-summary.wrong-answer,.result-summary.syntax-error,.result-summary.runtime-error,.result-summary.service-error{color:#c13a32;background:#fff1f0}.case-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(190px,1fr));gap:9px}.case-card{padding:10px;border:1px solid #e4e7ed;border-left:4px solid #909399;border-radius:6px}.case-card.accepted{border-left-color:#20a45a}.case-card.wrong-answer,.case-card.syntax-error,.case-card.runtime-error,.case-card.time-limit,.case-card.memory-limit{border-left-color:#e24b43}.case-head{display:flex;justify-content:space-between;gap:8px}.case-metrics{margin-top:5px;color:#909399;font-size:12px}.case-compare{display:grid;grid-template-columns:1fr 1fr;gap:6px;margin-top:8px}.case-compare label{font-size:12px;color:#777}.case-compare pre{margin-top:3px;padding:6px;background:#f7f8fa;border-radius:4px}.case-error{white-space:pre-wrap;color:#c13a32;font-size:12px}.result-empty{display:flex;align-items:center;justify-content:center;height:150px;color:#909399}.empty-state{height:calc(100% - 56px)}.filter-row{margin-bottom:16px}.question-section{margin-bottom:18px}.section-label{padding:8px 10px;background:#f5f7fa;color:#606266;font-weight:600}.question-item{display:flex;align-items:flex-start;justify-content:space-between;gap:10px;width:100%;padding:11px 10px;border:0;border-bottom:1px solid #ebeef5;background:#fff;text-align:left;cursor:pointer;color:#303133}.question-item:hover,.question-item.active{background:#ecf5ff;color:#337ecc}.question-item small{flex:none;color:#909399}.question-item small.accepted{color:#16803b}.question-item small.failed{color:#d98215}.oj-page :deep(.splitpanes__splitter){position:relative;background:#e8eaed}.oj-page :deep(.splitpanes--vertical>.splitpanes__splitter){width:7px}.oj-page :deep(.splitpanes--horizontal>.splitpanes__splitter){height:7px}.oj-page :deep(.splitpanes__splitter:hover){background:#409eff}@media(max-width:900px){.oj-page{height:auto;min-height:calc(100vh - 84px);overflow:auto}.oj-workspace{height:auto}.main-split{display:block}.main-split>.splitpanes__pane{width:100%!important;height:auto}.statement-pane{height:60vh}.right-split{height:800px}.sample-grid,.custom-console{grid-template-columns:1fr}.sample-grid>div+div,.console-column+.console-column{border-left:0;border-top:1px solid #e5e7eb}.header-actions .el-tag{display:none}}
</style>
