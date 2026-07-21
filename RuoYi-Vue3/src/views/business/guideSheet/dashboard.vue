<template>
  <div class="guide-sheet-dashboard-page">
  <div class="app-container guide-sheet-dashboard">
    <div class="dashboard-header">
      <el-button icon="ArrowLeft" @click="goBack">{{ lockedContext ? '返回成绩查询' : '返回模板库' }}</el-button>
      <h2 class="dashboard-title">{{ sheetTitle || '导学单数据看板' }}</h2>
      <div class="dashboard-header-actions">
        <el-tag v-if="lockedContext" type="primary" effect="plain" size="large">
          {{ entryYear }}级 {{ classCode }}班
        </el-tag>
        <el-select v-else v-model="classKey" placeholder="选择班级" style="width:180px" size="small" clearable @change="onClassChange">
          <el-option label="全部班级" value="" />
          <el-option v-for="c in assignedClasses" :key="c.key" :label="c.label" :value="c.key" />
        </el-select>
        <el-button icon="UploadFilled" size="small" :disabled="lockedContext && !classCode" @click="openUploads">上传记录</el-button>
        <el-button icon="Download" type="primary" size="small" :disabled="lockedContext && !classCode" @click="handleExport">导出结果</el-button>
      </div>
    </div>

    <!-- 第一行：填写进度（左）+ 统计概览（右） -->
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <el-card shadow="hover" header="填写进度" class="chart-card">
          <div v-if="!classCode" class="chart-placeholder">
            <el-icon :size="48"><Warning /></el-icon>
            <p>请选择指定班级查看教学进度</p>
          </div>
          <div v-else ref="barChartRef" style="height:280px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" header="统计概览">
          <div class="stat-cards-row">
            <div class="stat-card">
              <div class="stat-label">总人数</div>
              <div class="stat-value">{{ computedActiveTotal }}</div>
            </div>
            <div class="stat-card stat-card-success">
              <div class="stat-label">已提交</div>
              <div class="stat-value">{{ computedActiveSubmitted }}</div>
            </div>
            <div class="stat-card stat-card-warning">
              <div class="stat-label">填写中</div>
              <div class="stat-value">{{ computedActiveInProgress }}</div>
            </div>
            <div class="stat-card stat-card-info">
              <div class="stat-label">未开始</div>
              <div class="stat-value">{{ computedActiveNotStarted }}</div>
            </div>
            <div class="stat-card stat-card-rate">
              <div class="stat-label">完成率</div>
              <div class="stat-value">{{ computedCompletionRate }}%</div>
            </div>
            <div class="stat-card stat-card-accuracy">
              <div class="stat-label">正确率</div>
              <div class="stat-value">{{ computedAccuracyRate }}%</div>
            </div>
          </div>
          <div class="pie-charts-row">
            <div class="pie-chart-item">
              <div v-if="computedActiveTotal === 0" class="chart-placeholder" style="height:220px"><p>暂无数据</p></div>
              <div v-else ref="completionPieRef" style="height:220px"></div>
              <div class="pie-chart-title">完成率</div>
            </div>
            <div class="pie-chart-item">
              <div v-if="computedActiveSubmitted === 0 || computedAvgScore === 0" class="chart-placeholder" style="height:220px"><p>暂无提交数据</p></div>
              <div v-else ref="accuracyPieRef" style="height:220px"></div>
              <div class="pie-chart-title">正确率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 控制台：页面切换 -->
    <el-row :gutter="16" style="margin-top:12px" v-if="classCode && tabPages.length > 0">
      <el-col :span="24">
        <div class="dashboard-control-bar">
          <span class="control-label">页面切换：</span>
          <el-radio-group v-model="selectedPage" size="small" @change="onPageChange">
            <el-radio-button :value="0">全部</el-radio-button>
            <el-radio-button v-for="(name, idx) in tabPages" :key="idx + 1" :value="idx + 1">
              {{ name || '第' + (idx + 1) + '页' }}
            </el-radio-button>
          </el-radio-group>
          <el-input v-model="broadcastMessage" placeholder="向本班学生发送消息" size="small" style="width:240px" />
          <el-button size="small" type="primary" @click="sendBroadcast">发送</el-button>
          <el-button size="small" @click="sendRefresh">刷新学生页面</el-button>
        </div>
      </el-col>
    </el-row>

    <!-- D2：提交墙 — 3 秒扫一眼谁没交 -->
    <el-row v-if="classCode" :gutter="16" style="margin-top:16px">
      <el-col :span="24">
        <el-card shadow="never" class="submit-wall-card">
          <template #header>
            <div class="submit-wall-header">
              <span>提交墙</span>
              <span class="submit-wall-meta">
                已交 {{ computedActiveSubmitted }} · 填写中 {{ computedActiveInProgress }} · 未交 {{ computedActiveNotStarted }}
              </span>
            </div>
          </template>
          <div class="submit-wall">
            <div class="wall-group">
              <div class="wall-label done">已提交</div>
              <div class="wall-chips">
                <el-tag
                  v-for="row in wallSubmitted"
                  :key="'s-' + row.studentId"
                  type="success"
                  effect="plain"
                  size="small"
                >{{ row.studentName }}</el-tag>
                <span v-if="!wallSubmitted.length" class="wall-empty">暂无</span>
              </div>
            </div>
            <div class="wall-group">
              <div class="wall-label progress">填写中</div>
              <div class="wall-chips">
                <el-tag
                  v-for="row in wallInProgress"
                  :key="'p-' + row.studentId"
                  type="warning"
                  effect="plain"
                  size="small"
                >{{ row.studentName }}</el-tag>
                <span v-if="!wallInProgress.length" class="wall-empty">暂无</span>
              </div>
            </div>
            <div class="wall-group">
              <div class="wall-label todo">未开始</div>
              <div class="wall-chips">
                <el-tag
                  v-for="row in wallNotStarted"
                  :key="'n-' + row.studentId"
                  type="info"
                  effect="plain"
                  size="small"
                >{{ row.studentName }}</el-tag>
                <span v-if="!wallNotStarted.length" class="wall-empty">暂无</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第二行：学生完成情况 + 填写进度详情 -->
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <el-card shadow="never" header="学生完成情况">
          <div v-if="!classCode" class="chart-placeholder">
            <el-icon :size="48"><Warning /></el-icon>
            <p>请选择指定班级查看进度</p>
          </div>
          <div v-else-if="sortedFilteredList.length === 0" class="chart-placeholder">
            <p>暂无数据</p>
          </div>
          <div v-else class="progress-detail-list">
            <div v-for="row in sortedFilteredList" :key="row.studentId" class="progress-detail-row">
              <div class="progress-detail-name">
                <span class="student-name">{{ row.studentName }}</span>
                <span class="student-class" v-if="!classCode">{{ row.classCode }}</span>
              </div>
              <div class="progress-detail-content">
                <!-- 指定页面模式 -->
                <template v-if="selectedPage > 0">
                  <!-- 学生尚未到达此页 -->
                  <template v-if="!row.currentPage || row.currentPage < selectedPage">
                    <span class="progress-tag info">未开始</span>
                  </template>
                  <!-- 学生已提交：显示当前页评分详情 -->
                  <template v-else-if="row.isSubmitted === 'Y'">
                    <div class="grading-inline">
                      <span class="grading-score">{{ filterPageGradingDetails(row.progressDetail, selectedPage).totalScore }}/{{ filterPageGradingDetails(row.progressDetail, selectedPage).maxScore }}分</span>
                      <span class="grading-detail-items">
                        <span
                          v-for="(item, idx) in filterPageGradingDetails(row.progressDetail, selectedPage).details"
                          :key="idx"
                          class="grading-detail-item"
                        >
                          <span
                            class="grading-detail-tag"
                            :class="item.score === item.maxScore ? 'correct' : item.score === 0 ? 'wrong' : 'partial'"
                          >{{ item.fieldTitle || getFieldLabel(item.fieldKey) }} {{ item.score }}/{{ item.maxScore }}</span>
                        </span>
                        <el-button type="primary" size="small" link class="detail-btn" @click="openGradingDetail(filterPageGradingDetails(row.progressDetail, selectedPage).details, row)">详情</el-button>
                      </span>
                    </div>
                    <!-- 自评 -->
                    <div v-if="hasRateFields && getSelfScores(row.studentId).length > 0" class="self-assessment-inline">
                      <span class="self-assessment-label">自评：</span>
                      <span v-for="(s, si) in getSelfScores(row.studentId)" :key="si" class="self-rate-item">
                        <span class="self-rate-item-label">{{ s.label }}</span>
                        <el-rate v-model="s.value" disabled size="small" />
                      </span>
                    </div>
                  </template>
                  <!-- 学生已超过此页但未提交 -->
                  <template v-else-if="row.currentPage > selectedPage">
                    <span class="progress-tag success">已完成</span>
                  </template>
                  <!-- 学生正在此页：显示字段级完成情况 -->
                  <template v-else>
                    <div class="page-field-details">
                      <span
                        v-for="(filled, fieldName) in parseProgressDetail(row.progressDetail).fields"
                        :key="fieldName"
                        class="field-tag"
                        :class="filled ? 'field-filled' : 'field-empty'"
                      >{{ getFieldLabel(fieldName) }}</span>
                      <span v-if="Object.keys(parseProgressDetail(row.progressDetail).fields).length === 0" class="progress-percent">
                        {{ parseProgressDetail(row.progressDetail).percent }}%
                      </span>
                    </div>
                  </template>
                </template>
                <!-- 全部页面模式 -->
                <template v-else>
                  <!-- 未开始 -->
                  <template v-if="!row.currentPage || row.currentPage === 0">
                    <span class="progress-tag info">未开始</span>
                  </template>
                  <!-- 已提交 -->
                  <template v-else-if="row.isSubmitted === 'Y'">
                    <div class="grading-inline">
                      <span class="grading-score">{{ parseGradingDetail(row.progressDetail).totalScore }}分</span>
                      <span class="grading-detail-items">
                        <span
                          v-for="(item, idx) in parseGradingDetail(row.progressDetail).details"
                          :key="idx"
                          class="grading-detail-item"
                        >
                          <span
                            class="grading-detail-tag"
                            :class="item.score === item.maxScore ? 'correct' : item.score === 0 ? 'wrong' : 'partial'"
                          >{{ item.fieldTitle || item.fieldKey }} {{ item.score }}/{{ item.maxScore }}</span>
                        </span>
                        <el-button type="primary" size="small" link class="detail-btn" @click="openGradingDetail(parseGradingDetail(row.progressDetail).details, row)">详情</el-button>
                      </span>
                    </div>
                    <!-- 自评 -->
                    <div v-if="hasRateFields && getSelfScores(row.studentId).length > 0" class="self-assessment-inline">
                      <span class="self-assessment-label">自评：</span>
                      <span v-for="(s, si) in getSelfScores(row.studentId)" :key="si" class="self-rate-item">
                        <span class="self-rate-item-label">{{ s.label }}</span>
                        <el-rate v-model="s.value" disabled size="small" />
                      </span>
                    </div>
                  </template>
                  <!-- 填写中 -->
                  <template v-else>
                    <template v-if="parseProgressDetail(row.progressDetail).filled < parseProgressDetail(row.progressDetail).total">
                      <span class="progress-percent">{{ parseProgressDetail(row.progressDetail).percent }}%</span>
                    </template>
                    <template v-else>
                      <span class="progress-tag success">当前页已完成</span>
                    </template>
                  </template>
                </template>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" header="填写进度详情">
          <div v-if="!classCode" class="chart-placeholder">
            <el-icon :size="48"><Warning /></el-icon>
            <p>请选择指定班级查看教学进度</p>
          </div>
          <el-table v-else :data="sortedFilteredList" stripe size="small" max-height="calc(100vh - 420px)" v-loading="loading">
            <el-table-column label="班级" prop="classCode" width="80" v-if="!classCode" />
            <el-table-column label="姓名" prop="studentName" width="120" />
            <el-table-column label="账号" width="140" show-overflow-tooltip>
              <template #default="{ row }">{{ row.studentUserName || row.studentAccount || row.userName || '-' }}</template>
            </el-table-column>
            <el-table-column label="学号" prop="studentNo" width="120" />
            <el-table-column label="所在页面" width="120" align="center">
              <template #default="scope">
                <template v-if="!scope.row.currentPage || scope.row.currentPage === 0">-</template>
                <template v-else>{{ pageNameMap[scope.row.currentPage] || '第' + scope.row.currentPage + '页' }}</template>
              </template>
            </el-table-column>
            <el-table-column label="页码" width="70" align="center">
              <template #default="scope">
                <template v-if="!scope.row.currentPage || scope.row.currentPage === 0">-</template>
                <template v-else>{{ scope.row.currentPage }}</template>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="scope">
                <el-tag v-if="scope.row.isSubmitted === 'Y'" type="success" size="small">已提交</el-tag>
                <el-tag v-else-if="!scope.row.currentPage || scope.row.currentPage === 0" type="info" size="small">未开始</el-tag>
                <el-tag v-else type="primary" size="small">填写中</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="最后心跳" prop="lastHeartbeat" width="180" />
            <el-table-column label="提交时间" prop="submitTime" width="180" />
            <el-table-column label="自动评分" width="90" align="center">
              <template #default="{ row }">{{ getScoreBreakdown(row).autoScore }}</template>
            </el-table-column>
            <el-table-column label="人工调整" width="90" align="center">
              <template #default="{ row }">{{ getScoreBreakdown(row).manualScore }}</template>
            </el-table-column>
            <el-table-column label="最终得分" width="90" align="center">
              <template #default="{ row }">{{ getScoreBreakdown(row).finalScore }}</template>
            </el-table-column>
            <el-table-column label="操作" width="250" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" :disabled="!row.answerId" @click="openStudentAnswer(row)">查看答卷</el-button>
                <el-button link type="success" :disabled="!hasManualGradingItems(row)" @click="openManualGrading(row)">人工批改</el-button>
                <el-button
                  link
                  type="warning"
                  :disabled="!row.answerId"
                  :loading="rescoringAnswerId === row.answerId"
                  @click="handleRescore(row)"
                >重新评分</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>

  <!-- 批改详情弹窗 -->
  <el-dialog v-model="gradingDetailVisible" title="批改详情" width="620px" append-to-body>
    <!-- 自评数据（置顶，固定不随滚动） -->
    <div v-if="hasRateFields && getSelfScores(currentDetailStudentId).length > 0" class="self-assessment-detail self-assessment-sticky">
      <div class="self-assessment-sticky-title">学生自评</div>
      <div v-for="(s, si) in getSelfScores(currentDetailStudentId)" :key="si" class="self-assessment-row">
        <span class="self-assessment-label">{{ s.label }}：</span>
        <el-rate v-model="s.value" disabled show-score size="default" />
      </div>
    </div>
    <div class="grading-detail-dialog">
      <div class="detail-item"
        v-for="(item, idx) in currentGradingItems"
        :key="idx"
        :class="{ 'detail-item-last': idx === currentGradingItems.length - 1 }"
      >
        <div class="detail-item-header">
          <span class="detail-item-title">{{ idx + 1 }}. {{ item.fieldTitle || item.fieldKey }}</span>
          <el-input-number
            v-if="item.matchType === 'manual'"
            v-model="item.score"
            :min="0"
            :max="item.maxScore"
            :step="1"
            size="small"
            controls-position="right"
          />
          <span v-else class="detail-item-score"
            :class="item.score === item.maxScore ? 'correct' : item.score === 0 ? 'wrong' : 'partial'"
          >{{ item.score }}/{{ item.maxScore }} 分</span>
          <el-tag v-if="item.desc && item.desc.startsWith('AI评分')" type="warning" size="small">AI评分</el-tag>
          <el-tag v-else-if="item.matchType === 'manual'" type="info" size="small">人工批改</el-tag>
          <el-tag v-else type="success" size="small">自动批改</el-tag>
        </div>
        <div class="detail-item-body">
          <div class="detail-row">
            <span class="detail-row-label">学生答案：</span>
            <span class="detail-answer">{{ formatStudentAnswer(item.studentAnswer) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-row-label">参考答案：</span>
            <span class="detail-answer">{{ item.referenceAnswer || '无' }}</span>
          </div>
          <div class="detail-row" v-if="item.aiComment">
            <span class="detail-row-label">AI评语：</span>
            <span class="detail-comment">{{ item.aiComment }}</span>
          </div>
          <div class="detail-row" v-if="item.desc">
            <span class="detail-row-label">批改结果：</span>
            <span>{{ item.desc }}</span>
          </div>
          <div class="detail-row" v-if="item.matchType === 'manual'">
            <span class="detail-row-label">人工评语：</span>
            <el-input v-model="item.manualComment" maxlength="200" show-word-limit placeholder="可选" />
          </div>
        </div>
      </div>
    </div>
    <template #footer>
      <el-button @click="gradingDetailVisible = false">取消</el-button>
      <el-button v-if="hasManualItems" type="primary" :loading="manualSaving" @click="saveManualGrades">保存人工评分</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="answerDialogVisible" title="学生答卷" width="720px" append-to-body>
    <el-descriptions :column="2" border size="small" class="answer-student-info">
      <el-descriptions-item label="学生">{{ answerStudent.studentName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="账号">{{ answerStudent.studentUserName || answerStudent.studentAccount || answerStudent.userName || '-' }}</el-descriptions-item>
    </el-descriptions>
    <el-table :data="answerDetailRows" stripe border max-height="480px" style="margin-top: 14px">
      <el-table-column prop="label" label="题目/字段" min-width="190" show-overflow-tooltip />
      <el-table-column label="学生答案" min-width="320">
        <template #default="{ row }">{{ formatStudentAnswer(row.value) }}</template>
      </el-table-column>
    </el-table>
    <template #footer>
      <el-button @click="answerDialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="uploadsVisible" title="上传记录" width="760px" append-to-body>
    <el-table :data="uploads" v-loading="uploadsLoading" stripe>
      <el-table-column prop="questionName" label="题目" min-width="150" show-overflow-tooltip />
      <el-table-column prop="fileName" label="文件名" min-width="180" show-overflow-tooltip />
      <el-table-column prop="fileSize" label="大小" width="110">
        <template #default="{ row }">{{ formatFileSize(row.fileSize) }}</template>
      </el-table-column>
      <el-table-column prop="uploadTime" label="上传时间" width="180" />
      <el-table-column label="操作" width="72" align="center" fixed="right">
        <template #default="{ row }">
          <el-tooltip content="下载学生作品" placement="top">
            <el-button :icon="Download" circle text type="primary" @click="downloadUpload(row)" />
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
  </div>
</template>

<script setup name="GuideSheetDashboard">
import { ref, computed, onMounted, onBeforeUnmount, onActivated, onDeactivated, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { saveAs } from 'file-saver'
import {
  exportGuideSheet,
  downloadGuideSheetUpload,
  getGuideSheetBindingSnapshot,
  getProgress,
  getTeacherGuideSheetAnswer,
  getUploads,
  saveGuideSheetManualGrades,
  rescoreGuideSheetAnswer
} from '@/api/business/guideSheet'
import { Download, Warning } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { sortDashboardStudents } from './utils/dashboardStudentList.js'
import websocketClient from '@/plugins/websocket'

const router = useRouter()
const route = useRoute()
const lockedContext = computed(() => route.query.from === 'score')

const sheetTitle = ref('')
const classKey = ref('')
const deptId = ref(null)
const broadcastMessage = ref('')
const assignedClasses = ref([])
const selectedClass = computed(() => assignedClasses.value.find(item => item.key === classKey.value) || null)
const entryYear = computed(() => selectedClass.value?.entryYear || '')
const classCode = computed(() => selectedClass.value?.classCode || '')
const progressData = ref({ total: 0, submitted: 0, avgScore: 0, list: [] })
const loading = ref(false)

// 自评数据
const selfAssessment = ref({ rateFields: [], studentScores: {} })

// 批改详情弹窗
const gradingDetailVisible = ref(false)
const currentGradingItems = ref([])
const currentDetailStudentId = ref(null)
const manualSaving = ref(false)
const uploadsVisible = ref(false)
const uploadsLoading = ref(false)
const uploads = ref([])
const answerDialogVisible = ref(false)
const answerStudent = ref({})
const answerDetailRows = ref([])
const rescoringAnswerId = ref(null)

const hasManualItems = computed(() => currentGradingItems.value.some(item => item.matchType === 'manual'))

async function loadStudentAnswer(row) {
  if (row?.answerJson) return row.answerJson
  if (!row?.answerId || !row?.studentId) return null
  const response = await getTeacherGuideSheetAnswer(
    route.params.bindingId,
    row.studentId,
    entryYear.value,
    classCode.value
  )
  const data = response.data || response
  if (!data.hasAnswer) return null
  row.answerJson = data.answerJson || '{}'
  return row.answerJson
}

/** 打开批改详情弹窗 */
async function openGradingDetail(items, row) {
  let studentAnswers = {}
  try {
    const answerJson = await loadStudentAnswer(row)
    studentAnswers = answerJson ? JSON.parse(answerJson) : {}
  } catch (_error) {
    ElMessage.error('获取学生答卷失败')
    return
  }
  currentGradingItems.value = (items || []).map(item => ({
    ...item,
    studentAnswer: studentAnswers[item.fieldKey],
    manualComment: item.manualComment || ''
  }))
  currentDetailStudentId.value = row?.studentId || null
  gradingDetailVisible.value = true
}

async function openStudentAnswer(row) {
  let answers = {}
  try {
    const answerJson = await loadStudentAnswer(row)
    answers = answerJson ? JSON.parse(answerJson) : {}
  } catch (_error) {
    ElMessage.error('获取学生答卷失败或答卷格式无效')
    return
  }
  answerStudent.value = row
  answerDetailRows.value = Object.entries(answers).map(([key, value]) => ({
    key,
    label: getFieldLabel(key),
    value
  }))
  answerDialogVisible.value = true
}

function openManualGrading(row) {
  const grading = parseGradingDetail(row.progressDetail)
  openGradingDetail(grading.details || [], row)
}

function hasManualGradingItems(row) {
  return parseGradingDetail(row?.progressDetail).details.some(item => item.matchType === 'manual')
}

async function handleRescore(row) {
  if (!row.answerId) return
  rescoringAnswerId.value = row.answerId
  try {
    await rescoreGuideSheetAnswer(row.answerId, {
      bindingId: route.params.bindingId,
      entryYear: entryYear.value,
      classCode: classCode.value
    })
    ElMessage.success('重新评分完成')
    refresh()
  } finally {
    rescoringAnswerId.value = null
  }
}

async function saveManualGrades() {
  const items = currentGradingItems.value
    .filter(item => item.matchType === 'manual')
    .map(item => ({
      fieldKey: item.fieldKey,
      score: Number(item.score || 0),
      comment: item.manualComment || ''
    }))
  if (!currentDetailStudentId.value || items.length === 0) return
  manualSaving.value = true
  try {
    await saveGuideSheetManualGrades(route.params.bindingId, currentDetailStudentId.value, {
      entryYear: entryYear.value,
      classCode: classCode.value,
      items
    })
    ElMessage.success('人工评分已保存')
    gradingDetailVisible.value = false
    refresh()
  } finally {
    manualSaving.value = false
  }
}

function formatStudentAnswer(value) {
  if (value === null || value === undefined || value === '') return '未作答'
  if (Array.isArray(value)) return value.join('、')
  if (typeof value === 'object') return JSON.stringify(value)
  return String(value)
}

function formatFileSize(value) {
  const bytes = Number(value || 0)
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}

async function openUploads() {
  uploadsVisible.value = true
  uploadsLoading.value = true
  try {
    const response = await getUploads(route.params.bindingId, entryYear.value || undefined, classCode.value || undefined)
    uploads.value = response.data || []
  } finally {
    uploadsLoading.value = false
  }
}

async function downloadUpload(row) {
  const response = await downloadGuideSheetUpload(
    row.uploadId,
    entryYear.value,
    classCode.value
  )
  saveAs(response instanceof Blob ? response : new Blob([response]), row.fileName || '学生作品')
}

async function handleExport() {
  const response = await exportGuideSheet(
    route.params.bindingId,
    entryYear.value || undefined,
    classCode.value || undefined
  )
  const suffix = selectedClass.value ? `-${selectedClass.value.label}` : '-全部班级'
  saveAs(new Blob([response]), `${sheetTitle.value || '导学单'}${suffix}-结果.xlsx`)
}

/** 获取学生自评数据 */
function getSelfScores(studentId) {
  const scores = selfAssessment.value.studentScores || {}
  return scores[String(studentId)] || []
}

/** 是否有评分组件 */
const hasRateFields = computed(() => {
  return (selfAssessment.value.rateFields || []).length > 0
})

/** 标签页名称映射：pageIndex(1-based) → pageName */
const pageNameMap = ref({})
/** 标签页列表 */
const tabPages = ref([])
/** 每页字段信息：pageIndex(1-based) → [{name, label}] */
const pageFields = ref({})
/** 当前选中的页面（0=全部） */
const selectedPage = ref(0)

/**
 * 活跃班级集合：有至少1人提交的班级
 */
const activeClassSet = computed(() => {
  const list = progressData.value.list || []
  const set = new Set()
  for (const row of list) {
    if (row.isSubmitted === 'Y') {
      set.add(row.classCode)
    }
  }
  return set
})

/**
 * 全部班级模式显示所有学生（含未开始的），指定班级模式显示该班级所有学生
 */
const activeList = computed(() => {
  const list = progressData.value.list || []
  // 指定班级时显示该班级所有学生（含未开始）
  if (classCode.value) {
    return list
  }
  // 全部班级模式：显示所有学生（含未开始），修复 OPT-06
  return list
})

/** 活跃班级总人数 */
const computedActiveTotal = computed(() => activeList.value.length)
/** 活跃班级已提交人数 */
const computedActiveSubmitted = computed(() => {
  return activeList.value.filter(row => row.isSubmitted === 'Y').length
})
/** 活跃班级未开始人数 */
const computedActiveNotStarted = computed(() => {
  return activeList.value.filter(row => !row.currentPage || row.currentPage === 0).length
})
/** 活跃班级填写中人数 */
const computedActiveInProgress = computed(() => {
  return computedActiveTotal.value - computedActiveSubmitted.value - computedActiveNotStarted.value
})

/** 平均分 */
const computedAvgScore = computed(() => progressData.value.avgScore || 0)

/** 完成率 */
const computedCompletionRate = computed(() => {
  if (computedActiveTotal.value === 0) return '0.0'
  return ((computedActiveSubmitted.value / computedActiveTotal.value) * 100).toFixed(1)
})

/** 正确率 */
const computedAccuracyRate = computed(() => {
  const avg = computedAvgScore.value
  if (avg === 0) return '0.0'
  return avg.toFixed(1)
})

/**
 * 解析进度详情 JSON（填写中学生）
 * 返回 { filled, total, percent, fields }
 */
function parseProgressDetail(detail) {
  if (!detail) return { filled: 0, total: 0, percent: 0, fields: {} }
  try {
    const parsed = typeof detail === 'string' ? JSON.parse(detail) : detail
    const filled = parsed.filled || 0
    const total = parsed.total || 0
    const percent = total > 0 ? Math.round((filled / total) * 100) : 0
    return { filled, total, percent, fields: parsed.fields || {} }
  } catch (e) {
    return { filled: 0, total: 0, percent: 0, fields: {} }
  }
}

/**
 * 解析评分详情 JSON（已提交学生）
 * 返回 { totalScore, details }
 */
function parseGradingDetail(detail) {
  if (!detail) return { totalScore: 0, details: [] }
  try {
    const parsed = typeof detail === 'string' ? JSON.parse(detail) : detail
    const rawDetails = parsed.details || []
    // 去重：按 fieldKey 去重，防止同一字段被重复评分
    const seenKeys = new Set()
    const details = rawDetails.filter(d => {
      const key = d.fieldKey
      if (key && seenKeys.has(key)) return false
      if (key) seenKeys.add(key)
      return true
    })
    // 重新计算总分（从去重后的详情累加）
    const totalScore = details.reduce((sum, d) => sum + (d.score || 0), 0)
    return { totalScore, details }
  } catch (e) {
    return { totalScore: 0, details: [] }
  }
}

function getScoreBreakdown(row) {
  if (row?.isSubmitted !== 'Y') {
    return { autoScore: '-', manualScore: '-', finalScore: '-' }
  }
  const details = parseGradingDetail(row.progressDetail).details
  const derivedAutoScore = details
    .filter(item => item.matchType !== 'manual')
    .reduce((sum, item) => sum + Number(item.score || 0), 0)
  const derivedManualScore = details
    .filter(item => item.matchType === 'manual')
    .reduce((sum, item) => sum + Number(item.score || 0), 0)
  return {
    autoScore: row.autoScore ?? derivedAutoScore,
    manualScore: row.manualAdjustment ?? row.manualScore ?? derivedManualScore,
    finalScore: row.finalScore ?? row.totalScore ?? (derivedAutoScore + derivedManualScore)
  }
}

/**
 * 过滤评分详情，仅保留指定页面的字段
 */
function filterPageGradingDetails(detail, pageIndex) {
  const parsed = parseGradingDetail(detail)
  if (!parsed.details.length) return parsed
  const pageFieldsArr = pageFields.value[pageIndex] || []
  const pageFieldNames = new Set(pageFieldsArr.map(f => f.name))
  const pageFieldLabels = new Set(pageFieldsArr.map(f => f.label))
  const filtered = parsed.details.filter(item => {
    return pageFieldNames.has(item.fieldKey) || pageFieldLabels.has(item.fieldTitle)
  })
  // 计算当前页得分
  const pageScore = filtered.reduce((sum, item) => sum + (item.score || 0), 0)
  const pageMaxScore = filtered.reduce((sum, item) => sum + (item.maxScore || 0), 0)
  return { totalScore: pageScore, maxScore: pageMaxScore, details: filtered }
}

/**
 * 获取字段显示标签（优先使用 label，回退到 name）
 */
function getFieldLabel(fieldName) {
  for (const pageIdx of Object.keys(pageFields.value)) {
    const arr = pageFields.value[pageIdx] || []
    const found = arr.find(f => f.name === fieldName)
    if (found) return found.label
  }
  return fieldName
}

const sortedFilteredList = computed(() => {
  return sortDashboardStudents(progressData.value.list)
})

/** 提交墙分组：已交 / 填写中 / 未开始 */
const wallSubmitted = computed(() =>
  activeList.value.filter(row => row.isSubmitted === 'Y')
)
const wallNotStarted = computed(() =>
  activeList.value.filter(row => !row.currentPage || row.currentPage === 0)
)
const wallInProgress = computed(() =>
  activeList.value.filter(row => row.isSubmitted !== 'Y' && row.currentPage > 0)
)

const barChartRef = ref(null)
const completionPieRef = ref(null)
const accuracyPieRef = ref(null)
let barChart = null
let completionPie = null
let accuracyPie = null
let pollTimer = null
let refreshSequence = 0
let refreshInFlight = false
let refreshQueued = false
let pageActive = true

function goBack() {
  if (lockedContext.value) {
    router.push({
      path: '/business/score',
      query: {
        lessonId: route.query.lessonId,
        entryYear: route.query.entryYear,
        classCode: route.query.classCode
      }
    })
    return
  }
  router.push({ name: 'GuideSheet' })
}

/** 图表自适应窗口大小 */
function handleResize() {
  if (barChart) barChart.resize()
  if (completionPie) completionPie.resize()
  if (accuracyPie) accuracyPie.resize()
}

/**
 * 从 formJson 中提取标签页名称及每页字段信息
 */
function extractTabPages(formJson) {
  const pages = []
  const fields = {}
  const visited = new Set()
  try {
    const json = typeof formJson === 'string' ? JSON.parse(formJson) : formJson
    function walk(value) {
      if (value == null || typeof value !== 'object' || visited.has(value)) return
      visited.add(value)
      if (Array.isArray(value)) {
        for (const item of value) walk(item)
      } else {
        if (value.type === 'tab') {
          const tabs = value.tabs
          if (Array.isArray(tabs)) {
            for (let i = 0; i < tabs.length; i++) {
              const tab = tabs[i]
              pages.push(tab.options?.label || tab.options?.name || '')
              // 提取当前 tab-pane 内的字段信息
              const paneFields = []
              if (Array.isArray(tab.widgetList)) {
                collectFieldInfo(tab.widgetList, paneFields, new Set())
              }
              fields[i + 1] = paneFields
            }
          }
        }
        for (const key of Object.keys(value)) {
          const v = value[key]
          if (v && typeof v === 'object') walk(v)
        }
      }
    }
    walk(json?.widgetList || [])
  } catch (e) {
    // 忽略
  }
  const map = {}
  pages.forEach((name, i) => { map[i + 1] = name })
  return { pages, map, fields }
}

/**
 * 递归收集 widgetList 中的字段信息（name + label）
 */
function collectFieldInfo(widgets, result, visited) {
  if (!widgets) return
  for (const w of widgets) {
    if (!w || visited.has(w)) continue
    visited.add(w)
    const type = w.type
    // 跳过纯容器类型
    if (type === 'tab' || type === 'tab-pane' || type === 'grid' || type === 'grid-col'
        || type === 'card' || type === 'table' || type === 'table-cell') {
      for (const key of Object.keys(w)) {
        const v = w[key]
        if (Array.isArray(v)) collectFieldInfo(v, result, visited)
      }
      continue
    }
    const name = w.name || w.id
    if (name && type) {
      result.push({ name, label: w.options?.label || w.label || name })
    }
    for (const key of Object.keys(w)) {
      const v = w[key]
      if (Array.isArray(v)) collectFieldInfo(v, result, visited)
    }
  }
}

function onClassChange() {
  disposeCharts()
  stopPolling()
  selectedPage.value = 0
  websocketClient.disconnect()
  if (pageActive && entryYear.value && classCode.value && deptId.value) {
    websocketClient.connectClassroom(deptId.value, entryYear.value, classCode.value)
  }
  refresh()
  startPolling()
}

function onPageChange(page) {
  if (page > 0) websocketClient.send({ type: 'page_change', page })
}

function sendBroadcast() {
  if (!broadcastMessage.value.trim()) return
  websocketClient.send({ type: 'message', content: broadcastMessage.value.trim() })
  broadcastMessage.value = ''
}

function sendRefresh() {
  websocketClient.send({ type: 'refresh' })
}

function disposeCharts() {
  if (barChart) { barChart.dispose(); barChart = null }
  if (completionPie) { completionPie.dispose(); completionPie = null }
  if (accuracyPie) { accuracyPie.dispose(); accuracyPie = null }
}

function refresh() {
  const bindingId = route.params.bindingId
  const requestContext = `${bindingId || ''}:${entryYear.value}:${classCode.value}`
  if (!pageActive || !bindingId || !entryYear.value || !classCode.value) {
    progressData.value = { total: 0, submitted: 0, avgScore: 0, list: [] }
    loading.value = false
    return
  }
  if (refreshInFlight) {
    refreshQueued = true
    return
  }
  refreshInFlight = true
  refreshQueued = false
  const requestId = ++refreshSequence
  loading.value = true
  getProgress(bindingId, entryYear.value || undefined, classCode.value || undefined).then(res => {
    const currentContext = `${route.params.bindingId || ''}:${entryYear.value}:${classCode.value}`
    if (!pageActive || requestId !== refreshSequence || requestContext !== currentContext) return
    const data = res.data || res
    progressData.value = data
    selfAssessment.value = data.selfAssessment || { rateFields: [], studentScores: {} }
    const resolvedDeptId = data.list?.find(item => item?.deptId)?.deptId
    if (!deptId.value && resolvedDeptId) {
      deptId.value = resolvedDeptId
      if (entryYear.value && classCode.value) {
        websocketClient.connectClassroom(deptId.value, entryYear.value, classCode.value)
      }
    }
    nextTick(() => {
      updateBarChart()
      updateCompletionPie()
      updateAccuracyPie()
    })
  }).finally(() => {
    if (requestId === refreshSequence) loading.value = false
    refreshInFlight = false
    if (refreshQueued && pageActive) {
      refreshQueued = false
      nextTick(refresh)
    }
  })
}

function startPolling() {
  if (!pageActive || pollTimer || !entryYear.value || !classCode.value) return
  pollTimer = setInterval(refresh, 5000)
}

function stopPolling() {
  if (!pollTimer) return
  clearInterval(pollTimer)
  pollTimer = null
}

function activateDashboard() {
  pageActive = true
  if (entryYear.value && classCode.value && deptId.value) {
    websocketClient.connectClassroom(deptId.value, entryYear.value, classCode.value)
  }
  refresh()
  startPolling()
}

function deactivateDashboard() {
  pageActive = false
  refreshSequence++
  refreshQueued = false
  loading.value = false
  stopPolling()
  websocketClient.disconnect()
}

/**
 * 百分比堆积柱状图：按分页显示，每页独立100%堆积
 */
function updateBarChart() {
  if (!classCode.value) return
  if (!barChartRef.value) return
  if (!barChart) {
    barChart = echarts.init(barChartRef.value)
  }

  const total = computedActiveTotal.value

  if (total === 0) {
    barChart.setOption({
      title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#999', fontSize: 14 } }
    })
    return
  }

  const pages = tabPages.value
  const list = activeList.value

  const pageCounts = {}
  pages.forEach((_, i) => { pageCounts[i + 1] = { submitted: 0, inProgress: 0 } })
  for (const row of list) {
    const page = row.currentPage || 0
    if (page > 0 && pageCounts[page]) {
      if (row.isSubmitted === 'Y') {
        pageCounts[page].submitted++
      } else {
        pageCounts[page].inProgress++
      }
    }
  }

  const pageLabels = pages.map((name, i) => name || '第' + (i + 1) + '页')
  const submittedPcts = []
  const inProgressPcts = []
  const notStartedPcts = []
  pages.forEach((_, i) => {
    const c = pageCounts[i + 1] || { submitted: 0, inProgress: 0 }
    const pageSubmitted = parseFloat(((c.submitted / total) * 100).toFixed(1))
    const pageInProgress = parseFloat(((c.inProgress / total) * 100).toFixed(1))
    const pageNotStarted = parseFloat((100 - pageSubmitted - pageInProgress).toFixed(1))
    submittedPcts.push(pageSubmitted)
    inProgressPcts.push(pageInProgress)
    notStartedPcts.push(pageNotStarted)
  })

  barChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: function(params) {
        const idx = params[0].dataIndex
        let html = '<b>' + params[0].axisValue + '</b><br/>'
        const pc = pageCounts[idx + 1] || { submitted: 0, inProgress: 0 }
        html += params.find(p => p.seriesName === '已提交')?.marker + ' 已提交: ' + pc.submitted + '人 (' + submittedPcts[idx] + '%)<br/>'
        html += params.find(p => p.seriesName === '填写中')?.marker + ' 填写中: ' + pc.inProgress + '人 (' + inProgressPcts[idx] + '%)<br/>'
        const pageNotStarted = total - pc.submitted - pc.inProgress
        html += params.find(p => p.seriesName === '未开始')?.marker + ' 未开始: ' + pageNotStarted + '人 (' + notStartedPcts[idx] + '%)<br/>'
        return html
      }
    },
    legend: { bottom: '0%', data: ['已提交', '填写中', '未开始'] },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '8%', containLabel: true },
    xAxis: { type: 'category', data: pageLabels, axisLabel: { rotate: pageLabels.length > 4 ? 30 : 0 } },
    yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
    series: [
      { name: '已提交', type: 'bar', stack: 'total', data: submittedPcts, color: '#67C23A',
        barWidth: '50%', label: { show: true, formatter: p => p.value > 0 ? p.value + '%' : '', position: 'inside' } },
      { name: '填写中', type: 'bar', stack: 'total', data: inProgressPcts, color: '#409EFF',
        label: { show: true, formatter: p => p.value > 0 ? p.value + '%' : '', position: 'inside' } },
      { name: '未开始', type: 'bar', stack: 'total', data: notStartedPcts, color: '#C0C4CC',
        label: { show: true, formatter: p => p.value > 0 ? p.value + '%' : '', position: 'inside' } }
    ]
  })
}

/** 完成率饼图 */
function updateCompletionPie() {
  if (!completionPieRef.value) return
  if (!completionPie) {
    completionPie = echarts.init(completionPieRef.value)
  }
  const submitted = computedActiveSubmitted.value
  const total = computedActiveTotal.value
  if (total === 0) return

  const notSubmitted = total - submitted
  completionPie.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}人 ({d}%)'
    },
    legend: { bottom: '0%' },
    series: [{
      name: '完成率',
      type: 'pie',
      radius: ['35%', '52%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      label: { show: true, formatter: '{b}\n{d}%' },
      emphasis: { label: { fontSize: 18, fontWeight: 'bold' } },
      data: [
        { value: submitted, name: '已提交', itemStyle: { color: '#67C23A' } },
        { value: notSubmitted, name: '未提交', itemStyle: { color: '#C0C4CC' } }
      ]
    }]
  })
}

/** 正确率饼图 */
function updateAccuracyPie() {
  if (!accuracyPieRef.value) return
  if (!accuracyPie) {
    accuracyPie = echarts.init(accuracyPieRef.value)
  }
  const avgScore = computedAvgScore.value
  if (avgScore === 0) return

  const correct = avgScore
  const incorrect = parseFloat((100 - correct).toFixed(1))
  accuracyPie.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}分 ({d}%)'
    },
    legend: { bottom: '0%' },
    series: [{
      name: '正确率',
      type: 'pie',
      radius: ['35%', '52%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      label: { show: true, formatter: '{b}\n{d}%' },
      emphasis: { label: { fontSize: 18, fontWeight: 'bold' } },
      data: [
        { value: correct, name: '正确', itemStyle: { color: '#409EFF' } },
        { value: incorrect, name: '错误', itemStyle: { color: '#F56C6C' } }
      ]
    }]
  })
}

onMounted(() => {
  pageActive = true
  const bindingId = route.params.bindingId
  if (bindingId) {
    getGuideSheetBindingSnapshot(bindingId).then(res => {
      const detail = res.data || res
      sheetTitle.value = detail.snapshotTitle || detail.sheetTitle || ''
      deptId.value = detail.deptId
      const classes = lockedContext.value
        ? [{ entryYear: route.query.entryYear, classCode: route.query.classCode }]
        : (detail.assignedClasses || [])
      assignedClasses.value = classes
        .filter(item => item?.classCode)
        .map(item => ({
          key: item.key || `${item.entryYear}::${item.classCode}`,
          entryYear: item.entryYear,
          classCode: item.classCode,
          label: item.label || `${item.entryYear}级 ${item.classCode}班`
        }))
      if (lockedContext.value && assignedClasses.value.length) {
        classKey.value = assignedClasses.value[0].key
      }
      const { pages, map, fields } = extractTabPages(detail.snapshotFormJson || detail.formJson)
      tabPages.value = pages
      pageNameMap.value = map
      pageFields.value = fields
      if (lockedContext.value) onClassChange()
    })
  }
  window.addEventListener('resize', handleResize)
})

onActivated(activateDashboard)
onDeactivated(deactivateDashboard)

onBeforeUnmount(() => {
  deactivateDashboard()
  window.removeEventListener('resize', handleResize)
  disposeCharts()
})
</script>

<style scoped>
.dashboard-header { display: flex; align-items: center; gap: 10px; min-width: 0; }
.dashboard-title {
  flex: 1 1 280px;
  min-width: 0;
  margin: 0;
  line-height: 1.25;
  overflow-wrap: anywhere;
}
.dashboard-header-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
  max-width: 100%;
}
@media (max-width: 1200px) {
  .dashboard-header { flex-wrap: wrap; }
  .dashboard-header-actions { flex: 1 0 100%; }
}
.chart-card {  }
.chart-placeholder {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  height: 280px; color: #909399;
}
.chart-placeholder p { margin-top: 12px; font-size: 14px; }

/* 控制台 */
.dashboard-control-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px;
  background: #f5f7fa;
  border-radius: 6px;
}
.control-label {
  font-size: 12px;
  color: #606266;
  font-weight: 500;
  white-space: nowrap;
}
.stat-cards-row { display: flex; gap: 8px; justify-content: space-around; flex-wrap: wrap; }
.stat-card { text-align: center; padding: 6px 0; flex: 1; min-width: 70px; }
.stat-card .stat-label { font-size: 12px; color: #909399; margin-bottom: 4px; }
.stat-card .stat-value { font-size: 28px; font-weight: 600; color: #303133; }
.stat-card-success .stat-value { color: #67C23A; }
.stat-card-warning .stat-value { color: #409EFF; }
.stat-card-info .stat-value { color: #909399; }
.stat-card-rate .stat-value { color: #67C23A; }
.stat-card-accuracy .stat-value { color: #409EFF; }

.pie-charts-row { display: flex; gap: 16px; margin-top: 16px; padding-top: 12px; border-top: 1px solid #EBEEF5; }
.pie-chart-item { flex: 1; text-align: center; }
.pie-chart-title { font-size: 12px; color: #909399; margin-top: 4px; }

/* 进度完成情况列表 */
.progress-detail-list {
  max-height: calc(100vh - 420px);
  overflow-y: auto;
}
.progress-detail-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}
.progress-detail-row:last-child {
  border-bottom: none;
}
.progress-detail-name {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  min-width: 0;
}
.student-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
.student-class {
  font-size: 12px;
  color: #909399;
}
.progress-detail-content {
  flex-shrink: 0;
  margin-left: 12px;
}
.progress-percent {
  font-size: 16px;
  font-weight: 700;
  color: #409EFF;
}
.progress-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}
.progress-tag.info { background: #f0f2f5; color: #909399; }
.progress-tag.success { background: #f0f9eb; color: #67C23A; }
.grading-summary {
  display: flex;
  align-items: center;
  gap: 4px;
}
.grading-inline {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: flex-end;
}
.grading-score {
  font-weight: 600;
  font-size: 14px;
  color: #67C23A;
}
.grading-detail-items {
  display: flex;
  flex-wrap: wrap;
  gap: 3px;
  justify-content: flex-end;
}
.grading-detail-tag {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 3px;
  white-space: nowrap;
  background: #f0f9eb;
  color: #67C23A;
}
.grading-detail-tag.correct { background: #f0f9eb; color: #67C23A; }
.grading-detail-tag.wrong { background: #fef0f0; color: #F56C6C; }
.grading-detail-tag.partial { background: #fdf6ec; color: #E6A23C; }

/* 评分详情项（标签 + 详情按钮） */
.grading-detail-item {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}
.detail-btn {
  font-size: 11px;
  padding: 0 2px;
  height: 18px;
}

/* 批改详情弹窗 */
.grading-detail-dialog {
  max-height: 60vh;
  overflow-y: auto;
}
.detail-item {
  padding: 14px 0;
  border-bottom: 1px solid #ebeef5;
}
.detail-item-last {
  border-bottom: none;
  padding-bottom: 0;
}
.detail-item-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.detail-item-title {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
}
.detail-item-score.correct { color: #67C23A; font-weight: 600; }
.detail-item-score.wrong { color: #F56C6C; font-weight: 600; }
.detail-item-score.partial { color: #E6A23C; font-weight: 600; }
.detail-item-body {
  padding-left: 18px;
}
.detail-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 4px;
  line-height: 1.6;
  font-size: 13px;
}
.detail-row-label {
  flex-shrink: 0;
  color: #909399;
  margin-right: 4px;
}
.detail-answer {
  color: #409EFF !important;
  font-weight: 500;
}
.detail-comment {
  color: #E6A23C !important;
  font-style: italic;
}

/* 单页字段级完成情况 */
.page-field-details {
  display: flex;
  flex-wrap: wrap;
  gap: 3px;
  justify-content: flex-end;
}
.field-tag {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 3px;
  white-space: nowrap;
}
.field-tag.field-filled { background: #f0f9eb; color: #67C23A; }
.field-tag.field-empty { background: #fef0f0; color: #F56C6C; }

/* 自评样式 */
.self-assessment-inline {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
  flex-wrap: wrap;
  justify-content: flex-end;
}
.self-assessment-label {
  font-size: 12px;
  color: #909399;
}
.self-rate-item {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}
.self-rate-item-label {
  font-size: 11px;
  color: #606266;
  white-space: nowrap;
}
.self-rate-inline {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}
.self-rate-label {
  font-size: 11px;
  color: #909399;
}
.self-assessment-sticky {
  position: sticky;
  top: 0;
  z-index: 10;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  padding: 10px 0;
  margin-bottom: 12px;
}
.self-assessment-sticky-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  padding-bottom: 6px;
  border-bottom: 1px dashed #dcdfe6;
}
.self-assessment-detail {
  margin-top: 0;
}
.self-assessment-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
}
.self-assessment-row .self-assessment-label {
  font-size: 14px;
  color: #606266;
  min-width: 80px;
}
.submit-wall-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.submit-wall-meta {
  color: #909399;
  font-size: 12px;
  font-weight: normal;
}
.submit-wall {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.wall-group {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}
.wall-label {
  flex: 0 0 64px;
  font-size: 13px;
  font-weight: 600;
  padding-top: 2px;
}
.wall-label.done { color: #67c23a; }
.wall-label.progress { color: #e6a23c; }
.wall-label.todo { color: #909399; }
.wall-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  min-width: 0;
}
.wall-empty {
  color: #c0c4cc;
  font-size: 12px;
}
</style>
