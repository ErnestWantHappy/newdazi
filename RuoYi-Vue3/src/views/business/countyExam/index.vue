<template>
  <div class="app-container county-exam-page">
    <!-- E2：状态流水线（默认展开，降低教研员迷路成本） -->
    <el-alert
      class="status-pipeline"
      type="info"
      :closable="false"
      show-icon
      title="状态流水线：草稿 → 开启 → 关闭 → 已发布"
      description="草稿可配置组卷与参考班；开启后学生作答；关闭后评卷；发布后成绩对学校可见。评卷入口与「关闭/发布」独立控制。"
    />

    <div class="toolbar-panel">
      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="72px">
        <el-form-item label="抽测名称" prop="examName">
          <el-input v-model="queryParams.examName" placeholder="请输入名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="学段" prop="schoolType">
          <el-select v-model="queryParams.schoolType" placeholder="全部" clearable style="width: 140px">
            <el-option label="小学" value="1" />
            <el-option label="初中" value="2" />
            <el-option label="高中" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          <el-button type="success" icon="Plus" @click="handleAdd">新建抽测</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-panel">
      <el-table v-loading="loading" :data="examList" border>
        <el-table-column label="抽测名称" prop="examName" min-width="180" show-overflow-tooltip />
        <el-table-column label="学段" width="90">
          <template #default="{ row }">{{ schoolTypeText(row.schoolType) }}</template>
        </el-table-column>
        <el-table-column label="年级" prop="examGrade" width="80" />
        <el-table-column label="出题模式" width="110">
          <template #default="{ row }">{{ shuffleModeText(row.shuffleMode) }}</template>
        </el-table-column>
        <el-table-column label="作答时长" width="100">
          <template #default="{ row }">{{ row.durationMinutes || 40 }}分钟</template>
        </el-table-column>
        <el-table-column label="总分" prop="totalScore" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="评卷入口" width="100">
          <template #default="{ row }">
            <el-tag :type="gradingTagType(row.gradingEnabled)" effect="plain">{{ gradingEnabledText(row.gradingEnabled) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="170" />
        <el-table-column label="操作" width="480" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="View" @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status === '0'" link type="primary" icon="Edit" @click="handleEdit(row)">配置</el-button>
            <el-button v-if="row.status === '0'" link type="success" icon="Tickets" @click="goDesigner(row)">组卷</el-button>
            <el-button v-if="row.status === '0'" link type="warning" icon="School" @click="openClassDrawer(row)">班级</el-button>
            <el-button v-if="row.status === '0'" link type="success" icon="VideoPlay" @click="handleOpen(row)">开启</el-button>
            <el-button v-if="row.status === '1'" link type="warning" icon="SwitchButton" @click="handleClose(row)">关闭</el-button>
            <el-button v-if="row.status !== '3'" link type="primary" icon="User" @click="openAllocateDialog(row)">
              {{ row.status === '2' ? '评卷' : '评卷配置' }}
            </el-button>
            <el-button v-if="row.status === '2' && row.gradingEnabled !== '1'" link type="success" icon="VideoPlay" @click="handleEnableGrading(row)">开评卷</el-button>
            <el-button v-if="row.status === '2' && row.gradingEnabled === '1'" link type="warning" icon="CircleClose" @click="handleDisableGrading(row)">关评卷</el-button>
            <el-button v-if="row.status === '2'" link type="success" icon="Finished" @click="handlePublish(row)">发布</el-button>
            <el-button v-if="row.status === '0'" link type="danger" icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </div>

    <el-dialog :title="dialogTitle" v-model="dialogOpen" width="560px" append-to-body>
      <el-form ref="examRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="抽测名称" prop="examName">
          <el-input v-model="form.examName" placeholder="请输入区域抽测名称" />
        </el-form-item>
        <el-form-item label="学段" prop="schoolType">
          <el-select v-model="form.schoolType" placeholder="请选择学段" style="width: 100%">
            <el-option label="小学" value="1" />
            <el-option label="初中" value="2" />
            <el-option label="高中" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="年级" prop="examGrade">
          <el-input-number v-model="form.examGrade" :min="1" :max="12" controls-position="right" />
        </el-form-item>
        <el-form-item label="作答时长" prop="durationMinutes">
          <el-input-number v-model="form.durationMinutes" :min="1" :max="240" controls-position="right" />
          <span class="form-unit">分钟</span>
        </el-form-item>
        <!-- 出题模式已移至组卷页面配置 -->
      </el-form>
      <template #footer>
        <el-button @click="dialogOpen = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- 班级选择抽屉 — 每行一个学校 + Radio 单选班级 + 显示人数 -->
    <el-drawer v-model="classDrawer.open" :title="`参考班级 - ${classDrawer.exam?.examName || ''}`" size="620px">
      <div class="class-drawer-content">
        <div class="class-drawer-toolbar">
          <el-button icon="Refresh" @click="loadAssignableClasses">刷新</el-button>
          <el-button type="warning" @click="randomSelectClasses">🎲 系统随机抽选</el-button>
        </div>

        <div v-loading="classDrawer.loading" class="school-list">
          <div v-if="schoolGroups.length === 0" class="empty-tip">
            暂无可选班级，请确认该学段下学校已创建班级数据
          </div>
          <div v-for="school in schoolGroups" :key="school.deptId" class="school-card">
            <div class="school-header">
              <div class="school-name">{{ school.deptName }}</div>
              <el-button
                v-if="getSelectedClassCode(school.deptId)"
                type="danger"
                link
                size="small"
                @click="clearSchoolSelection(school.deptId)"
              >
                本校不参加抽测
              </el-button>
            </div>
            <el-radio-group 
              :model-value="getSelectedClassCode(school.deptId)" 
              @update:model-value="val => onClassRadioChange(school, val)"
              class="class-radio-group"
            >
              <div v-for="cls in school.classes" :key="cls.classCode" class="class-radio-item">
                <el-radio :value="cls.classCode">
                  {{ formatClassName(classDrawer.exam?.examGrade, cls.classCode) }}
                  <span class="student-count">({{ cls.studentCount || 0 }}人)</span>
                </el-radio>
              </div>
            </el-radio-group>
          </div>
        </div>

        <div class="class-drawer-footer">
          <span class="selected-summary">已选 {{ selectedClasses.length }} 所学校的班级（未选择学校不参加抽测）</span>
          <div>
            <el-button @click="clearSelectedClasses">清空</el-button>
            <el-button type="primary" icon="Check" @click="saveClasses">保存班级</el-button>
          </div>
        </div>
      </div>
    </el-drawer>

    <el-dialog title="配置匿名评卷" v-model="allocateDialog.open" width="920px" append-to-body>
      <el-alert
        title="按操作题配置评卷教师和份数。可用「一键均分」预填份数；不填/0 表示关闭抽测生成任务时对该题剩余答卷自动均分。教师首页仅在有待评任务时显示「区域抽测评卷」入口（须先关闭抽测并生成任务）。搜索教师姓名/账号可跨校、跨学段命中（含小学部与初中部双账号）。"
        type="info"
        :closable="false"
        show-icon
      />
      <div class="grading-progress-summary">
        <div class="progress-card">
          <span>参考人数</span>
          <strong>{{ allocateDialog.progress.participantCount || 0 }}</strong>
        </div>
        <div class="progress-card">
          <span>操作题提交</span>
          <strong>{{ allocateDialog.progress.submittedCount || 0 }}</strong>
        </div>
        <div class="progress-card">
          <span>已分配</span>
          <strong>{{ allocateDialog.progress.assignedCount || 0 }}</strong>
        </div>
        <div class="progress-card">
          <span>已评</span>
          <strong>{{ allocateDialog.progress.gradedCount || 0 }}</strong>
        </div>
        <div class="progress-card">
          <span>待评</span>
          <strong>{{ allocateDialog.progress.pendingCount || 0 }}</strong>
        </div>
      </div>
      <div class="grader-toolbar">
        <el-input
          v-model="allocateDialog.keyword"
          placeholder="搜索教师姓名、账号或学校（如：郑东旭）"
          clearable
          style="width: 300px"
          @keyup.enter="loadAssignableGraders"
        />
        <el-button icon="Search" :loading="allocateDialog.teacherLoading" @click="loadAssignableGraders">搜索教师</el-button>
        <el-button type="success" plain icon="Finished" @click="evenlyAllocateAllQuestions">一键均分全部操作题</el-button>
      </div>
      <div v-loading="allocateDialog.loading" class="allocate-question-list">
        <el-empty v-if="practicalAllocateQuestions.length === 0" description="本场区域抽测没有操作题" />
        <div v-for="question in practicalAllocateQuestions" :key="question.questionId" class="allocate-question">
          <div class="allocate-question-header">
            <div>
              <span class="badge">操作题</span>
              <span class="question-title">{{ question.questionContent }}</span>
              <span class="question-progress">
                提交 {{ questionProgress(question.questionId).submittedCount || 0 }} /
                参考 {{ allocateDialog.progress.participantCount || 0 }}，
                已分配 {{ questionProgress(question.questionId).assignedCount || 0 }}，
                已评 {{ questionProgress(question.questionId).gradedCount || 0 }}，
                待评 {{ questionProgress(question.questionId).pendingCount || 0 }}
              </span>
            </div>
            <div class="allocate-question-actions">
              <el-button type="success" link @click="evenlyAllocateQuestion(question.questionId)">一键均分</el-button>
              <el-button type="primary" link icon="Plus" @click="addGraderRow(question.questionId)">添加教师</el-button>
            </div>
          </div>
          <el-table :data="allocateDialog.configs[question.questionId] || []" border size="small">
            <el-table-column label="评卷教师" min-width="260">
              <template #default="{ row }">
                <el-select v-model="row.graderId" filterable placeholder="请选择教师" style="width: 100%">
                  <el-option
                    v-for="teacher in allocateDialog.teachers"
                    :key="teacher.userId"
                    :label="teacherLabel(teacher)"
                    :value="teacher.userId"
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="批改份数" width="150">
              <template #default="{ row }">
                <el-input-number v-model="row.targetCount" :min="0" :max="9999" controls-position="right" placeholder="自动" />
              </template>
            </el-table-column>
            <el-table-column label="已评" prop="gradedCount" width="80" />
            <el-table-column label="操作" width="90">
              <template #default="{ $index }">
                <el-button link type="danger" icon="Delete" @click="removeGraderRow(question.questionId, $index)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <template #footer>
        <el-button @click="allocateDialog.open = false">取消</el-button>
        <el-button v-if="allocateDialog.exam?.status === '2'" type="warning" @click="resetAllocateTasks">重置评卷任务</el-button>
        <el-button type="primary" @click="submitAllocate">保存配置</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailDrawer.open" :title="`抽测详情 - ${detailDrawer.exam?.examName || ''}`" size="86%">
      <el-descriptions v-if="detailDrawer.exam" :column="4" border>
        <el-descriptions-item label="学段">{{ schoolTypeText(detailDrawer.exam.schoolType) }}</el-descriptions-item>
        <el-descriptions-item label="年级">{{ detailDrawer.exam.examGrade }}</el-descriptions-item>
        <el-descriptions-item label="出题模式">{{ shuffleModeText(detailDrawer.exam.shuffleMode) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusText(detailDrawer.exam.status) }}</el-descriptions-item>
        <el-descriptions-item label="评卷入口">
          <el-tag :type="gradingTagType(detailDrawer.exam.gradingEnabled)" effect="plain">
            {{ gradingEnabledText(detailDrawer.exam.gradingEnabled) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="总分">{{ detailDrawer.exam.totalScore || 0 }}</el-descriptions-item>
        <el-descriptions-item label="作答时长">{{ detailDrawer.exam.durationMinutes || 40 }}分钟</el-descriptions-item>
        <el-descriptions-item label="开启时间">{{ detailDrawer.exam.openTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="关闭时间">{{ detailDrawer.exam.closeTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ detailDrawer.exam.publishTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-tabs v-model="detailDrawer.activeTab" class="detail-tabs" @tab-change="loadDetailTab">
        <el-tab-pane label="学校汇总" name="summary">
          <AnalysisPanel
            :official="analysisOfficial"
            :overview="analysisOverview"
            :schools="summaryRows"
            :distribution="analysisDistribution"
            :questions="analysisQuestions"
          />
          <div class="summary-table-title">学校汇总明细</div>
          <el-table :data="summaryRows" border>
            <el-table-column label="排名" width="70" align="center">
              <template #default="{ $index }">{{ $index + 1 }}</template>
            </el-table-column>
            <el-table-column label="学校" prop="deptName" min-width="180" />
            <el-table-column label="选中班级" prop="classInfo" min-width="140" />
            <el-table-column label="人数" prop="studentCount" width="90" />
            <el-table-column label="平均分" prop="avgScore" width="100" />
            <el-table-column label="最高分" prop="maxScore" width="100" />
            <el-table-column label="最低分" prop="minScore" width="100" />
            <el-table-column label="及格率" prop="passRate" width="100">
              <template #default="{ row }">{{ row.passRate }}%</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="学生明细" name="students">
          <div class="student-filter">
            <el-input v-model="studentKeyword" placeholder="姓名、账号、学号、学校" clearable style="width: 260px" @keyup.enter="loadStudents" />
            <el-button icon="Search" @click="loadStudents">查询</el-button>
            <el-button type="success" icon="Download" @click="handleExport">导出 Excel</el-button>
          </div>
          <el-table v-loading="studentsLoading" :data="studentRows" border>
            <el-table-column label="学校" prop="deptName" min-width="160" show-overflow-tooltip />
            <el-table-column label="班级" prop="classInfo" width="110" />
            <el-table-column label="姓名" prop="studentName" width="110" />
            <el-table-column label="账号" prop="userName" width="130" />
            <el-table-column label="学号" prop="studentNo" width="130" />
            <el-table-column label="理论分" prop="theoryScore" width="90" />
            <el-table-column label="打字分" prop="typingScore" width="90" />
            <el-table-column label="操作分" prop="practicalScore" width="90" />
            <el-table-column label="总分" prop="totalScore" width="90" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === '1' ? 'success' : 'info'">{{ row.status === '1' ? '已提交' : '未提交' }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="评卷进度" name="grading">
          <div class="grading-progress-summary drawer-progress">
            <div class="progress-card">
              <span>参考人数</span>
              <strong>{{ detailDrawer.progress.participantCount || 0 }}</strong>
            </div>
            <div class="progress-card">
              <span>操作题提交</span>
              <strong>{{ detailDrawer.progress.submittedCount || 0 }}</strong>
            </div>
            <div class="progress-card">
              <span>已分配</span>
              <strong>{{ detailDrawer.progress.assignedCount || 0 }}</strong>
            </div>
            <div class="progress-card">
              <span>已评</span>
              <strong>{{ detailDrawer.progress.gradedCount || 0 }}</strong>
            </div>
            <div class="progress-card">
              <span>待评</span>
              <strong>{{ detailDrawer.progress.pendingCount || 0 }}</strong>
            </div>
          </div>
          <el-table :data="detailQuestionProgressRows" border>
            <el-table-column label="操作题" prop="questionContent" min-width="240" show-overflow-tooltip />
            <el-table-column label="提交数" prop="submittedCount" width="90" />
            <el-table-column label="已分配" prop="assignedCount" width="90" />
            <el-table-column label="已评" prop="gradedCount" width="90" />
            <el-table-column label="待评" prop="pendingCount" width="90" />
            <el-table-column label="完成度" width="180">
              <template #default="{ row }">
                <el-progress :percentage="progressPercent(row)" :stroke-width="8" />
              </template>
            </el-table-column>
          </el-table>
          <el-table :data="detailGraderRows" border class="grader-progress-table">
            <el-table-column label="操作题" prop="questionContent" min-width="220" show-overflow-tooltip />
            <el-table-column label="评卷教师" prop="graderName" min-width="150" show-overflow-tooltip />
            <el-table-column label="任务份数" prop="targetCount" width="100" />
            <el-table-column label="已评" prop="gradedCount" width="90" />
            <el-table-column label="待评" width="90">
              <template #default="{ row }">{{ Math.max(Number(row.targetCount || 0) - Number(row.gradedCount || 0), 0) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="题目" name="paper">
          <el-table :data="detailDrawer.questions" border row-key="questionId" class="question-table">
            <el-table-column label="题型" width="90">
              <template #default="{ row }">{{ questionTypeText(row.questionType) }}</template>
            </el-table-column>
            <el-table-column label="题目" min-width="520">
              <template #default="{ row }">
                <div class="question-cell">
                  <div class="question-title">{{ row.questionContent || '-' }}</div>
                  <div v-if="row.questionType === 'choice'" class="question-extra">
                    <div class="option-grid">
                      <div
                        v-for="option in questionOptions(row)"
                        :key="option.label"
                        class="option-item"
                        :class="{ correct: option.label === row.answer }"
                      >
                        <span class="option-label">{{ option.label }}</span>
                        <span class="option-content">{{ option.content || '未配置' }}</span>
                      </div>
                    </div>
                    <div class="answer-line">正确答案：<el-tag size="small" type="success">{{ formatQuestionAnswer(row) }}</el-tag></div>
                  </div>
                  <div v-else-if="row.questionType === 'judgment'" class="question-extra">
                    <div class="answer-line">正确答案：<el-tag size="small" type="success">{{ formatQuestionAnswer(row) }}</el-tag></div>
                  </div>
                  <div v-else-if="row.questionType === 'typing'" class="question-extra meta-line">
                    <el-tag type="info" effect="plain">打字时长：{{ row.typingDuration || 0 }} 分钟</el-tag>
                    <el-tag v-if="row.wordCount" type="info" effect="plain">字数：{{ row.wordCount }}</el-tag>
                  </div>
                  <div v-else-if="row.questionType === 'practical'" class="question-extra">
                    <div class="practical-toolbar">
                      <el-button
                        type="primary"
                        plain
                        size="small"
                        icon="View"
                        :disabled="!row.previewPath"
                        @click="handlePreviewQuestionFile(row)"
                      >文档预览</el-button>
                      <span v-if="!row.previewPath" class="muted-text">暂无可预览文档</span>
                    </div>
                    <div v-if="row.scoringItems && row.scoringItems.length" class="scoring-list">
                      <span
                        v-for="(item, index) in row.scoringItems"
                        :key="item.itemId || index"
                        class="scoring-chip"
                      >{{ item.itemName }}（{{ item.itemScore || 0 }}%）</span>
                    </div>
                    <div v-else class="muted-text">暂无评分标准</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="分值" prop="questionScore" width="90" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-drawer>
    <PdfPreview ref="pdfPreviewRef" />
  </div>
</template>

<script setup name="CountyExam">
import { computed, getCurrentInstance, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { saveAs } from 'file-saver'
import { ElMessage, ElMessageBox } from 'element-plus'
import PdfPreview from '@/components/PdfPreview/index.vue'
import AnalysisPanel from './components/AnalysisPanel.vue'
import { questionTypeLabel } from '@/utils/questionType'
import {
  addCountyExam,
  allocateCountyExamGraders,
  closeCountyExam,
  delCountyExam,
  disableCountyExamGrading,
  enableCountyExamGrading,
  getAssignableCountyExamGraders,
  exportCountyExamStudents,
  getAssignableCountyExamClasses,
  getCountyExam,
  getCountyExamSummary,
  listCountyExam,
  listCountyExamStudents,
  openCountyExam,
  publishCountyExam,
  resetCountyExamGraders,
  saveCountyExamClasses,
  updateCountyExam
} from '@/api/business/countyExam'

const { proxy } = getCurrentInstance()
const router = useRouter()
const pdfPreviewRef = ref(null)

// E3：状态文案与按钮边界保持「开启/关闭/评卷/发布」语义一致
const statusOptions = [
  { label: '草稿', value: '0' },
  { label: '开启', value: '1' },
  { label: '关闭', value: '2' },
  { label: '已发布', value: '3' }
]

const loading = ref(false)
const examList = ref([])
const total = ref(0)
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  examName: '',
  schoolType: '',
  status: ''
})

const dialogOpen = ref(false)
const dialogTitle = ref('')
const form = ref(defaultForm())
const rules = {
  examName: [{ required: true, message: '抽测名称不能为空', trigger: 'blur' }],
  schoolType: [{ required: true, message: '学段不能为空', trigger: 'change' }],
  examGrade: [{ required: true, message: '年级不能为空', trigger: 'blur' }],
  durationMinutes: [{ required: true, message: '作答时长不能为空', trigger: 'blur' }]
}

// 组卷抽屉已替换为独立路由页面，不再需要以下变量
// questionDrawer / questionBank / questionTotal / questionQuery / selectedQuestions

const classDrawer = reactive({ open: false, loading: false, exam: null })
const assignableClasses = ref([])
const selectedClasses = ref([])

const allocateDialog = reactive({
  open: false,
  exam: null,
  loading: false,
  teacherLoading: false,
  keyword: '',
  teachers: [],
  questions: [],
  progress: {},
  configs: {}
})

const detailDrawer = reactive({
  open: false,
  exam: null,
  questions: [],
  classes: [],
  graders: [],
  progress: {},
  activeTab: 'summary'
})
const summaryRows = ref([])
const analysisOverview = ref({})
const analysisDistribution = ref([])
const analysisQuestions = ref([])
const analysisOfficial = ref(false)
const studentRows = ref([])
const studentKeyword = ref('')
const studentsLoading = ref(false)

// selectedQuestionTotal 已随组卷抽屉移除

function defaultForm() {
  return {
    examId: undefined,
    examName: '',
    schoolType: '1',
    examGrade: 1,
    durationMinutes: 40
  }
}

function getList() {
  loading.value = true
  listCountyExam(queryParams).then(response => {
    examList.value = response.rows || []
    total.value = response.total || 0
  }).finally(() => {
    loading.value = false
  })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleAdd() {
  form.value = defaultForm()
  dialogTitle.value = '新建区域抽测'
  dialogOpen.value = true
}

function handleEdit(row) {
  form.value = { ...defaultForm(), ...row }
  dialogTitle.value = '配置区域抽测'
  dialogOpen.value = true
}

function submitForm() {
  proxy.$refs.examRef.validate(valid => {
    if (!valid) return
    const request = form.value.examId ? updateCountyExam(form.value) : addCountyExam(form.value)
    request.then(() => {
      ElMessage.success('保存成功')
      dialogOpen.value = false
      getList()
    })
  })
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除“${row.examName}”？`, '删除区域抽测', { type: 'warning' })
  await delCountyExam(row.examId)
  ElMessage.success('删除成功')
  getList()
}

async function handleOpen(row) {
  // E1：开启前核对清单（参考班、组卷、时长）
  const detailResponse = await getCountyExam(row.examId).catch(() => ({ data: {} }))
  const detail = detailResponse.data || {}
  const questions = detail.questions || []
  const classes = detail.classes || detail.assignedClasses || []
  const classCount = Array.isArray(classes) ? classes.length : (detail.classCount || 0)
  const questionCount = Array.isArray(questions) ? questions.length : (detail.questionCount || 0)
  if (questionCount <= 0) {
    ElMessage.warning('开启前请先完成组卷（至少 1 道题）')
    return
  }
  if (classCount <= 0) {
    ElMessage.warning('开启前请先选择参考班级（每校最多 1 个班）')
    return
  }
  const checklist = [
    `抽测名称：${row.examName || '-'}`,
    `组卷题目：${questionCount} 道`,
    `参考班级：${classCount} 个`,
    '开启后将冻结组卷与参考班级，并为学生生成试卷',
    '学生端优先阻断日常课程与导学单',
    '请确认作答时长（默认 40 分钟）'
  ].join('\n')
  const { value } = await ElMessageBox.prompt(
    checklist + '\n\n请输入本场区域抽测作答时长（分钟）：',
    '开启前核对',
    {
      type: 'warning',
      inputValue: String(row.durationMinutes || detail.durationMinutes || 40),
      inputPlaceholder: '默认 40',
      inputValidator: value => {
        const duration = Number(value)
        return Number.isInteger(duration) && duration > 0 ? true : '作答时长必须是大于 0 的整数'
      },
      confirmButtonText: '确认开启',
      cancelButtonText: '取消',
      customClass: 'county-open-checklist'
    }
  )
  const durationMinutes = Number(value)
  const response = await openCountyExam(row.examId, { durationMinutes })
  ElMessage.success(`已开启，作答时长 ${response.data?.durationMinutes || durationMinutes} 分钟，参考学生 ${response.data?.participantCount || 0} 人`)
  getList()
}

async function handleClose(row) {
  const detailResponse = await getCountyExam(row.examId).catch(() => ({ data: {} }))
  const questions = detailResponse.data?.questions || []
  const graders = detailResponse.data?.graders || []
  const hasPractical = questions.some(item => item.questionType === 'practical')
  if (hasPractical && graders.length === 0) {
    ElMessage.warning('请先为每一道操作题配置评卷教师，再关闭区域抽测')
    return
  }
  await ElMessageBox.confirm(
    '关闭后学生不能继续保存或提交，系统会自动提交所有未提交学生的已保存内容。确认关闭？',
    '关闭区域抽测',
    { type: 'warning' }
  )
  const response = await closeCountyExam(row.examId)
  const autoSubmitCount = response.data?.autoSubmitCount || 0
  ElMessage.success(autoSubmitCount > 0 ? `已关闭，自动提交 ${autoSubmitCount} 名未提交学生` : '已关闭')
  getList()
}

async function handlePublish(row) {
  await ElMessageBox.confirm('发布前会校验操作题评卷是否完成。确认发布成绩？', '发布区域抽测', { type: 'warning' })
  await publishCountyExam(row.examId)
  ElMessage.success('已发布')
  getList()
}

async function handleEnableGrading(row) {
  await ElMessageBox.confirm(
    '开启后，有待评任务的教师会在首页看到匿名评卷入口。确认开启？',
    '开启区域抽测评卷',
    { type: 'warning' }
  )
  await enableCountyExamGrading(row.examId)
  ElMessage.success('评卷入口已开启')
  getList()
}

async function handleDisableGrading(row) {
  await ElMessageBox.confirm(
    '关闭后教师首页不再显示本场评卷入口，已保存的评卷结果不会删除。确认关闭？',
    '关闭区域抽测评卷',
    { type: 'warning' }
  )
  await disableCountyExamGrading(row.examId)
  ElMessage.success('评卷入口已关闭')
  getList()
}

// 组卷跳转到独立页面
function goDesigner(row) {
  router.push(`/business/county-exam-designer/${row.examId}`)
}

async function openClassDrawer(row) {
  classDrawer.exam = row
  classDrawer.open = true
  selectedClasses.value = []
  const response = await getCountyExam(row.examId)
  selectedClasses.value = (response.data?.classes || []).map(item => ({ ...item, deptName: `学校ID ${item.deptId}` }))
  loadAssignableClasses()
}

function loadAssignableClasses() {
  classDrawer.loading = true
  getAssignableCountyExamClasses({ schoolType: classDrawer.exam?.schoolType }).then(response => {
    assignableClasses.value = response.data || []
    // 回填已选班级的学校名称和人数
    selectedClasses.value = selectedClasses.value.map(item => {
      const matched = assignableClasses.value.find(row => classKey(row) === classKey(item))
      return matched ? { ...item, deptName: matched.deptName, studentCount: matched.studentCount } : item
    })
  }).finally(() => {
    classDrawer.loading = false
  })
}

function classKey(row) {
  return `${row.deptId}-${row.entryYear}-${row.classCode}`
}

// 根据年级数字(1-12)计算对应的入学年份
function gradeToEntryYear(grade) {
  if (!grade) return null
  
  const now = new Date()
  const currentYear = now.getFullYear()
  const month = now.getMonth() + 1
  const day = now.getDate()
  
  // 平台统一在 7 月 20 日切换新学年，避免暑期抽测选到上一届学生。
  const academicStartYear = month > 7 || (month === 7 && day >= 20)
    ? currentYear
    : currentYear - 1
  
  let gradeInSection
  if (grade >= 1 && grade <= 6) {
    gradeInSection = grade
  } else if (grade >= 7 && grade <= 9) {
    gradeInSection = grade - 6
  } else if (grade >= 10 && grade <= 12) {
    gradeInSection = grade - 9
  } else {
    return null
  }
  
  return String(academicStartYear - gradeInSection + 1)
}

// 格式化班级名称：年级+classCode，例如六年级1班 → "601"
function formatClassName(examGrade, classCode) {
  if (!examGrade || !classCode) return `${classCode}班`
  const code = String(classCode).padStart(2, '0')
  return `${examGrade}${code}`
}

// 按学校分组，并根据抽测年级过滤对应入学年份的班级
const schoolGroups = computed(() => {
  const targetEntryYear = gradeToEntryYear(classDrawer.exam?.examGrade)
  
  // 先按入学年份过滤
  const filtered = targetEntryYear
    ? assignableClasses.value.filter(cls => String(cls.entryYear) === targetEntryYear)
    : assignableClasses.value
  
  const map = {}
  for (const cls of filtered) {
    if (!map[cls.deptId]) {
      map[cls.deptId] = {
        deptId: cls.deptId,
        deptName: cls.deptName,
        entryYear: cls.entryYear,
        classes: []
      }
    }
    map[cls.deptId].classes.push(cls)
  }
  // 每个学校内的班级按 classCode 排序
  for (const school of Object.values(map)) {
    school.classes.sort((a, b) => Number(a.classCode) - Number(b.classCode))
  }
  return Object.values(map).sort((a, b) => a.deptName.localeCompare(b.deptName))
})

// 获取某学校当前选中的班级号
function getSelectedClassCode(deptId) {
  const found = selectedClasses.value.find(item => item.deptId === deptId)
  return found ? found.classCode : ''
}

// Radio 切换时更新选择
function onClassRadioChange(school, classCode) {
  // 找到对应班级的完整数据
  const cls = school.classes.find(c => c.classCode === classCode)
  if (!cls) return
  
  // 移除该学校之前的选择
  const idx = selectedClasses.value.findIndex(item => item.deptId === school.deptId)
  if (idx > -1) {
    selectedClasses.value.splice(idx, 1)
  }
  // 加入新选择
  selectedClasses.value.push({ ...cls, type: '1' })
}

function clearSchoolSelection(deptId) {
  selectedClasses.value = selectedClasses.value.filter(item => item.deptId !== deptId)
}

// 清空所有已选班级
function clearSelectedClasses() {
  selectedClasses.value = []
}

// 系统随机抽选 — 为每个学校随机选中一个班级
function randomSelectClasses() {
  selectedClasses.value = []
  for (const school of schoolGroups.value) {
    if (school.classes.length === 0) continue
    const randomIndex = Math.floor(Math.random() * school.classes.length)
    selectedClasses.value.push({ ...school.classes[randomIndex], type: '1' })
  }
  ElMessage.success(`已随机抽选 ${selectedClasses.value.length} 个班级，可手动调整后保存`)
}

async function saveClasses() {
  const payload = selectedClasses.value.map(item => ({
    deptId: item.deptId,
    type: item.type || '1',
    entryYear: item.entryYear,
    classCode: item.classCode
  }))
  await saveCountyExamClasses(classDrawer.exam.examId, payload)
  ElMessage.success('参考班级已保存')
  classDrawer.open = false
}

const practicalAllocateQuestions = computed(() =>
  allocateDialog.questions.filter(item => item.questionType === 'practical')
)
const detailQuestionProgressRows = computed(() => {
  const progressRows = detailDrawer.progress.questionProgress || []
  return progressRows.map(item => ({
    ...item,
    questionContent: findQuestionTitle(item.questionId)
  }))
})
const detailGraderRows = computed(() => detailDrawer.graders.map(item => ({
  ...item,
  questionContent: findQuestionTitle(item.questionId)
})))

async function openAllocateDialog(row) {
  allocateDialog.exam = row
  allocateDialog.open = true
  allocateDialog.loading = true
  allocateDialog.keyword = ''
  allocateDialog.questions = []
  allocateDialog.progress = {}
  allocateDialog.configs = {}
  try {
    const [detailResponse] = await Promise.all([
      getCountyExam(row.examId),
      loadAssignableGraders()
    ])
    allocateDialog.exam = detailResponse.data?.exam || row
    allocateDialog.questions = detailResponse.data?.questions || []
    allocateDialog.progress = detailResponse.data?.gradingProgress || {}
    const configs = {}
    practicalAllocateQuestions.value.forEach(question => {
      configs[question.questionId] = []
    })
    ;(detailResponse.data?.graders || []).forEach(item => {
      if (!configs[item.questionId]) {
        configs[item.questionId] = []
      }
      configs[item.questionId].push({
        questionId: item.questionId,
        graderId: item.graderId,
        targetCount: item.targetCount || 0,
        gradedCount: item.gradedCount || 0
      })
    })
    allocateDialog.configs = configs
  } finally {
    allocateDialog.loading = false
  }
}

async function loadAssignableGraders() {
  allocateDialog.teacherLoading = true
  try {
    const response = await getAssignableCountyExamGraders({ keyword: allocateDialog.keyword })
    allocateDialog.teachers = response.data || []
  } finally {
    allocateDialog.teacherLoading = false
  }
}

function addGraderRow(questionId) {
  if (!allocateDialog.configs[questionId]) {
    allocateDialog.configs[questionId] = []
  }
  allocateDialog.configs[questionId].push({
    questionId,
    graderId: undefined,
    targetCount: 0,
    gradedCount: 0
  })
}

function removeGraderRow(questionId, index) {
  allocateDialog.configs[questionId]?.splice(index, 1)
}

/**
 * 可分配规模：有提交用该题提交数，否则用参考人数（关闭生成任务时仍以真实答卷为准）。
 */
function allocatePoolSize(questionId) {
  const submitted = Number(questionProgress(questionId).submittedCount) || 0
  if (submitted > 0) {
    return submitted
  }
  return Number(allocateDialog.progress.participantCount) || 0
}

/**
 * 将可分配份数均分写入该操作题下各评卷教师行（余数前几人 +1）。
 * @param {boolean} silent 批量调用时不弹 toast
 * @returns {{ ok: boolean, reason?: string, pool?: number, graders?: number }}
 */
function evenlyAllocateQuestion(questionId, silent = false) {
  const rows = (allocateDialog.configs[questionId] || []).filter(row => row.graderId)
  if (rows.length === 0) {
    if (!silent) {
      ElMessage.warning('请先为该操作题选择至少一位评卷教师，再一键均分')
    }
    return { ok: false, reason: 'no-grader' }
  }
  const pool = allocatePoolSize(questionId)
  if (pool <= 0) {
    if (!silent) {
      ElMessage.warning('当前无可分配规模（参考人数与提交数均为 0），可先不填份数，关闭抽测时自动均分')
    }
    return { ok: false, reason: 'empty-pool' }
  }
  const base = Math.floor(pool / rows.length)
  let remainder = pool % rows.length
  for (const row of rows) {
    row.targetCount = base + (remainder > 0 ? 1 : 0)
    if (remainder > 0) {
      remainder -= 1
    }
  }
  if (!silent) {
    ElMessage.success(`已按 ${pool} 份均分给 ${rows.length} 位教师`)
  }
  return { ok: true, pool, graders: rows.length }
}

function evenlyAllocateAllQuestions() {
  if (practicalAllocateQuestions.value.length === 0) {
    ElMessage.warning('本场没有操作题')
    return
  }
  let successCount = 0
  for (const question of practicalAllocateQuestions.value) {
    const result = evenlyAllocateQuestion(question.questionId, true)
    if (result.ok) {
      successCount += 1
    }
  }
  if (successCount === 0) {
    ElMessage.warning('请先为操作题选择评卷教师（且参考人数或提交数大于 0），再一键均分')
    return
  }
  ElMessage.success(`已对 ${successCount} 道操作题完成一键均分`)
}

function teacherLabel(teacher) {
  const name = teacher.nickName || teacher.userName || teacher.userId
  // 兼教多校时后端可能返回 deptNames；展示全部学校避免误以为「只有小学部账号」
  let dept = ''
  if (Array.isArray(teacher.deptNames) && teacher.deptNames.length > 0) {
    dept = ` - ${teacher.deptNames.join(' / ')}`
  } else if (teacher.deptName) {
    dept = ` - ${teacher.deptName}`
  }
  return `${name}（${teacher.userName || teacher.userId}）${dept}`
}

async function submitAllocate() {
  const assignments = []
  const uniqueKeys = new Set()
  for (const question of practicalAllocateQuestions.value) {
    const rows = allocateDialog.configs[question.questionId] || []
    for (const row of rows) {
      if (!row.graderId) {
        ElMessage.warning('请选择评卷教师')
        return
      }
      const key = `${question.questionId}-${row.graderId}`
      if (uniqueKeys.has(key)) {
        ElMessage.warning('同一道操作题不能重复配置同一位评卷教师')
        return
      }
      uniqueKeys.add(key)
      assignments.push({
        questionId: question.questionId,
        graderId: row.graderId,
        targetCount: Number(row.targetCount) > 0 ? Number(row.targetCount) : 0
      })
    }
  }
  if (assignments.length === 0) {
    ElMessage.warning('请至少配置一位评卷教师')
    return
  }
  const response = await allocateCountyExamGraders(allocateDialog.exam.examId, { assignments })
  if (response.data?.allocated) {
    ElMessage.success(`已生成 ${response.data?.assignedCount || 0} 份匿名评卷任务`)
  } else {
    ElMessage.success('评卷配置已保存，关闭区域抽测后将生成任务')
  }
  allocateDialog.open = false
  getList()
}

async function resetAllocateTasks() {
  if (!allocateDialog.exam) return
  await ElMessageBox.confirm(
    '重置会清空本场操作题已提交的评卷结果，并按当前已保存配置重新生成匿名任务。确认继续？',
    '重置评卷任务',
    { type: 'warning' }
  )
  const response = await resetCountyExamGraders(allocateDialog.exam.examId)
  ElMessage.success(`已重置 ${response.data?.resetCount || 0} 份操作题答卷，重新生成 ${response.data?.assignedCount || 0} 份任务`)
  await openAllocateDialog(allocateDialog.exam)
  getList()
}

async function openDetail(row) {
  const response = await getCountyExam(row.examId)
  detailDrawer.exam = response.data?.exam || row
  detailDrawer.questions = response.data?.questions || []
  detailDrawer.classes = response.data?.classes || []
  detailDrawer.graders = response.data?.graders || []
  detailDrawer.progress = response.data?.gradingProgress || {}
  detailDrawer.activeTab = 'summary'
  summaryRows.value = []
  analysisOverview.value = {}
  analysisDistribution.value = []
  analysisQuestions.value = []
  analysisOfficial.value = detailDrawer.exam?.status === '3'
  detailDrawer.open = true
  loadSummary(row.examId)
}

function loadDetailTab(tabName) {
  if (!detailDrawer.exam) return
  if (tabName === 'summary') loadSummary(detailDrawer.exam.examId)
  if (tabName === 'students') loadStudents()
}

async function loadSummary(examId) {
  const response = await getCountyExamSummary(examId)
  summaryRows.value = response.data?.schools || []
  analysisOverview.value = response.data?.overview || {}
  analysisDistribution.value = response.data?.distribution || []
  analysisQuestions.value = response.data?.questions || []
  analysisOfficial.value = response.data?.official === true
}

function loadStudents() {
  if (!detailDrawer.exam) return
  studentsLoading.value = true
  listCountyExamStudents(detailDrawer.exam.examId, { keyword: studentKeyword.value, pageNum: 1, pageSize: 9999 })
    .then(response => {
      studentRows.value = response.rows || []
    })
    .finally(() => {
      studentsLoading.value = false
    })
}

async function handleExport() {
  if (!detailDrawer.exam) return
  const blob = await exportCountyExamStudents(detailDrawer.exam.examId)
  saveAs(new Blob([blob]), `${detailDrawer.exam.examName || '区域抽测'}-成绩.xlsx`)
}

function schoolTypeText(value) {
  return ({ 1: '小学', 2: '初中', 3: '高中' })[value] || '-'
}

function statusText(value) {
  return ({ 0: '草稿', 1: '开启', 2: '关闭', 3: '已发布' })[value] || '-'
}

function statusTagType(value) {
  return ({ 0: 'info', 1: 'success', 2: 'warning', 3: 'primary' })[value] || 'info'
}

function gradingEnabledText(value) {
  return value === '1' ? '已开启' : '未开启'
}

function gradingTagType(value) {
  return value === '1' ? 'success' : 'info'
}

function shuffleModeText(value) {
  return ({ 0: '固定顺序', 1: '随机排序', 2: '随机抽题' })[value] || '-'
}

function questionTypeText(value) {
  return questionTypeLabel(value)
}

function questionOptions(row) {
  return [
    { label: 'A', content: row.optionA },
    { label: 'B', content: row.optionB },
    { label: 'C', content: row.optionC },
    { label: 'D', content: row.optionD }
  ]
}

function formatQuestionAnswer(row) {
  if (row.questionType === 'judgment') {
    return formatJudgeAnswer(row.answer)
  }
  return row.answer || '未配置'
}

function formatJudgeAnswer(answer) {
  if (answer === null || answer === undefined || answer === '') return '未配置'
  const normalized = String(answer).trim().toLowerCase()
  const truthy = ['1', 'true', 't', 'y', 'yes', '对', '正确', 'right']
  const falsy = ['0', 'false', 'f', 'n', 'no', '错', '错误', 'wrong']
  if (truthy.includes(normalized)) return '对'
  if (falsy.includes(normalized)) return '错'
  return String(answer)
}

function handlePreviewQuestionFile(row) {
  if (!row.previewPath) {
    ElMessage.warning('该操作题暂无可预览文档')
    return
  }
  pdfPreviewRef.value?.open(import.meta.env.VITE_APP_BASE_API + '/common/resource/view?resource=' + encodeURIComponent(row.previewPath))
}

function questionProgress(questionId) {
  return allocateDialog.progress.questionProgressMap?.[questionId] || {}
}

function progressPercent(row) {
  const assigned = Number(row.assignedCount || 0)
  if (assigned <= 0) return 0
  return Math.min(100, Math.round((Number(row.gradedCount || 0) / assigned) * 100))
}

function findQuestionTitle(questionId) {
  const question = detailDrawer.questions.find(item => String(item.questionId) === String(questionId))
    || allocateDialog.questions.find(item => String(item.questionId) === String(questionId))
  return question?.questionContent || `操作题 ${questionId || ''}`
}

getList()
</script>

<style scoped lang="scss">
.county-exam-page {
  .toolbar-panel,
  .table-panel,
  .drawer-section {
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    background: #fff;
  }

  .status-pipeline {
    margin-bottom: 12px;
  }
  .toolbar-panel {
    padding: 16px 16px 0;
    margin-bottom: 12px;
  }

  .table-panel {
    padding: 12px;
  }

  .summary-table-title {
    margin: 2px 0 10px;
    color: #303133;
    font-size: 14px;
    font-weight: 600;
  }

  .drawer-layout {
    display: grid;
    grid-template-columns: minmax(0, 1fr) minmax(420px, 0.9fr);
    gap: 12px;
  }

  .class-layout {
    grid-template-columns: minmax(0, 1fr) minmax(360px, 0.75fr);
  }

  .drawer-section {
    padding: 12px;
    min-width: 0;
  }

  .section-header {
    min-height: 40px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 10px;
    font-weight: 600;
  }

  .selected-section :deep(.el-input-number) {
    width: 84px;
  }

  .form-unit {
    margin-left: 8px;
    color: #606266;
  }

  .grader-toolbar {
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 14px 0;
  }

  .grading-progress-summary {
    display: grid;
    grid-template-columns: repeat(5, minmax(0, 1fr));
    gap: 10px;
    margin: 12px 0;
  }

  .progress-card {
    min-height: 70px;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    padding: 10px 12px;
    background: #f8fafc;

    span {
      display: block;
      color: #606266;
      font-size: 12px;
      margin-bottom: 8px;
    }

    strong {
      color: #1f2937;
      font-size: 22px;
    }
  }

  .allocate-question-list {
    max-height: 58vh;
    overflow-y: auto;
    padding-right: 4px;
  }

  .allocate-question {
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    padding: 12px;
    margin-bottom: 12px;
  }

  .allocate-question-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 10px;
  }

  .allocate-question-actions {
    display: flex;
    align-items: center;
    gap: 4px;
    flex-shrink: 0;
  }

  .question-progress {
    display: block;
    margin-top: 8px;
    color: #606266;
    font-size: 12px;
    line-height: 1.5;
  }

  .drawer-progress {
    margin-top: 0;
  }

  .grader-progress-table {
    margin-top: 12px;
  }

  .badge {
    display: inline-flex;
    align-items: center;
    height: 22px;
    padding: 0 8px;
    border-radius: 4px;
    background: #ecf5ff;
    color: #409eff;
    font-size: 12px;
    margin-right: 8px;
  }

  .question-title {
    color: #303133;
    font-weight: 600;
  }

  .detail-tabs {
    margin-top: 14px;
  }

  .student-filter {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 10px;
  }

  .question-table {
    :deep(.el-table__cell) {
      vertical-align: top;
    }
  }

  .question-cell {
    display: flex;
    flex-direction: column;
    gap: 10px;
    line-height: 1.6;
  }

  .question-extra {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .option-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px;
  }

  .option-item {
    display: grid;
    grid-template-columns: 28px minmax(0, 1fr);
    align-items: start;
    gap: 8px;
    min-height: 34px;
    padding: 7px 10px;
    border: 1px solid #e5e7eb;
    border-radius: 6px;
    background: #fafafa;

    &.correct {
      border-color: #95d475;
      background: #f0f9eb;
    }
  }

  .option-label {
    color: #606266;
    font-weight: 600;
  }

  .option-content {
    min-width: 0;
    word-break: break-word;
  }

  .answer-line,
  .meta-line,
  .practical-toolbar,
  .scoring-list {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
  }

  .scoring-chip {
    display: inline-flex;
    align-items: center;
    min-height: 26px;
    padding: 0 8px;
    border-radius: 4px;
    background: #f4f4f5;
    color: #606266;
    font-size: 12px;
  }

  .muted-text {
    color: #909399;
    font-size: 13px;
  }

  /* 班级选择抽屉 — 新UI样式 */
  .class-drawer-content {
    display: flex;
    flex-direction: column;
    height: 100%;
  }

  .class-drawer-toolbar {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #e5e7eb;
  }

  .school-list {
    flex: 1;
    overflow-y: auto;
    padding-right: 4px;
  }

  .empty-tip {
    text-align: center;
    color: #909399;
    padding: 40px 0;
    font-size: 14px;
  }

  .school-card {
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    padding: 14px 16px;
    margin-bottom: 12px;
    background: #fafafa;
    transition: box-shadow 0.2s;

    &:hover {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    }
  }

  .school-name {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
  }

  .school-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 10px;
    padding-bottom: 8px;
    border-bottom: 1px dashed #dcdfe6;
    gap: 12px;
  }

  .class-radio-group {
    display: flex;
    flex-wrap: wrap;
    gap: 4px 0;
  }

  .class-radio-item {
    min-width: 140px;
    padding: 4px 0;
  }

  .student-count {
    color: #909399;
    font-size: 12px;
    margin-left: 2px;
  }

  .class-drawer-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 14px;
    margin-top: 12px;
    border-top: 1px solid #e5e7eb;
  }

  .selected-summary {
    color: #606266;
    font-size: 14px;
    font-weight: 500;
  }
}
</style>
