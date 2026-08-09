<template>
  <div class="app-container teacher-dashboard">
    <ResearchNotificationBar />
    <div v-if="countyGradingEntry.hasTask" class="county-grading-entry" @click="goToCountyExamGrading">
      <div>
        <strong>区域抽测评卷</strong>
        <span>{{ pendingCountyGradingCount }} 份匿名答卷待处理</span>
      </div>
      <el-button type="primary" icon="Right">进入评卷</el-button>
    </div>
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>课程设置</span>
          <div class="header-actions">
            <el-button type="success" plain icon="Document" @click="goToExemption">
              免抽测申请
            </el-button>
            <el-button type="primary" plain icon="DArrowRight" :loading="advanceLoading" @click="handleOneClickAdvance">
              手动一键课堂推进
            </el-button>
            <el-button icon="Setting" @click="openCourseSettings">设置</el-button>
          </div>
        </div>
      </template>
      <div v-if="loading" class="loading-state">正在加载教学数据...</div>
      <div v-if="!loading && gradeGroups.length === 0" class="empty-state">
        您还没有导入任何学生，无法进行课程设置。请先前往【学生管理】导入学生。
      </div>
      <div v-for="group in gradeGroups" :key="group.entryYear" class="grade-group">
        <div class="grade-header">
          <span class="grade-title">{{ group.entryYear }}级（当前{{ group.gradeName }}）</span>
        </div>
        <div
          v-for="section in getCourseGradeSections(group)"
          :key="getCourseSectionKey(group, section)"
          class="course-grade-section"
          :class="{ 'is-history': section.isHistory }"
        >
          <div
            class="course-grade-header"
            :class="{ clickable: section.isHistory }"
            @click="section.isHistory && toggleHistorySection(group, section)"
          >
            <div class="course-grade-heading">
              <strong>{{ section.gradeName }}课程</strong>
              <el-tag v-if="section.isCurrent" type="primary" size="small">当前年级</el-tag>
              <el-tag v-else type="info" size="small">历史课程</el-tag>
              <span>{{ section.lessons.length }} 门</span>
            </div>
            <span v-if="section.isHistory" class="history-toggle-text">
              {{ isCourseSectionExpanded(group, section) ? '收起' : '点击显示' }}
            </span>
          </div>

          <div v-show="isCourseSectionExpanded(group, section)" class="lesson-container">
            <div
              v-for="lesson in getVisibleLessons(group, section)"
              :key="lesson.lessonId"
              class="lesson-folder"
            >
              <div class="folder-delete" @click.stop="handleDeleteLesson(lesson.lessonId)">
                <el-icon><Close /></el-icon>
              </div>

              <div class="folder-content">
                <div class="folder-title-vertical" :title="lesson.lessonTitle">
                  {{ lesson.lessonTitle }}
                </div>
                <div class="folder-info">
                  <div v-if="lesson.assignedClasses?.length" class="assigned-classes">
                    <span v-for="cls in lesson.assignedClasses" :key="cls" class="assigned-tag">{{ cls }}</span>
                  </div>
                  <div class="lesson-count-tag">
                    第{{ lesson.lessonNum }}课
                  </div>
                  <div v-if="lesson.lessonMode === 'attendance'" class="attendance-mode-tag">考勤</div>
                </div>
              </div>

              <div class="folder-actions">
                <div class="action-btn design" @click.stop="handleEditLesson(lesson, group)" title="设计课程">
                  <el-icon><Edit /></el-icon>
                  <span>设计</span>
                </div>
                <div
                  v-if="lesson.lessonMode === 'attendance'"
                  class="action-btn grade"
                  @click.stop="openCheckinRoster(lesson, group)"
                  title="签到名单"
                >
                  <el-icon><Check /></el-icon>
                  <span>签到</span>
                </div>
                <div
                  v-if="lesson.hasPractical"
                  class="action-btn grade"
                  @click.stop="goToGrading(lesson, group)"
                  title="批改作业"
                >
                  <el-icon><Check /></el-icon>
                  <span>批改</span>
                  <span v-if="hasUngradedPractical(lesson)" class="grading-red-dot" aria-label="存在未批操作题"></span>
                </div>
                <div
                  v-if="lesson.lessonMode !== 'attendance'"
                  class="action-btn score"
                  @click.stop="goToScoreAnalysis(lesson, group)"
                  title="查看成绩"
                >
                  <el-icon><DataLine /></el-icon>
                  <span>成绩</span>
                </div>
              </div>
            </div>

            <div
              v-if="hasHiddenLessons(group, section)"
              class="expand-lessons-btn"
              title="展开更早课程"
              @click="expandLessons(group, section)"
            >
              <el-icon class="more-icon"><MoreFilled /></el-icon>
              <div class="more-text">还有 {{ section.lessons.length - 5 }} 节</div>
            </div>

            <div
              v-if="section.canAdd"
              class="add-lesson-btn"
              @click="handleAddNewLesson(group, section)"
            >
              <el-icon class="add-icon"><Plus /></el-icon>
              <div class="add-text">
                添加{{ section.gradeName }}{{ section.isHistory ? '历史' : '' }}课程
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 班级选择弹窗 -->
    <ClassSelectionDialog ref="classDialogRef" />

    <!-- 手动一键课堂推进：年级 + 多选班级（默认全选当前为常规课的班级） -->
    <el-dialog v-model="advanceDialogVisible" title="手动一键课堂推进" width="480px" destroy-on-close>
      <p class="settings-intro">
        默认已选中当前年级所有<strong>常规课班级</strong>，可取消部分班级；当前为考勤课的班级不会参与推进。需有成绩人数达到设置中的统一比例（默认 50%）。
      </p>
      <el-form label-width="80px">
        <el-form-item label="年级">
          <el-select v-model="advanceForm.entryYear" placeholder="请选择年级" style="width: 100%" @change="onAdvanceGradeChange">
            <el-option
              v-for="g in advanceGradeOptions"
              :key="g.entryYear"
              :label="`${g.entryYear}级（${g.gradeName}）`"
              :value="g.entryYear"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="班级">
          <div class="class-multi-tools">
            <el-button link type="primary" :disabled="!advanceClassOptions.length" @click="selectAllAdvanceClasses">全选</el-button>
            <el-button link :disabled="!advanceForm.classCodes.length" @click="advanceForm.classCodes = []">清空</el-button>
            <span class="class-multi-count">已选 {{ advanceForm.classCodes.length }} / {{ advanceClassOptions.length }}</span>
          </div>
          <el-select
            v-model="advanceForm.classCodes"
            multiple
            collapse-tags
            collapse-tags-tooltip
            placeholder="请选择班级（可多选）"
            style="width: 100%"
            :disabled="!advanceForm.entryYear"
          >
            <el-option
              v-for="cls in advanceClassOptions"
              :key="cls"
              :label="formatClassLabel(cls)"
              :value="normalizeClassCode(cls)"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="advanceDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="advanceLoading" @click="confirmOneClickAdvance">确认推进</el-button>
      </template>
    </el-dialog>

    <!-- 统一课程推进设置（全校常规课共用一套） -->
    <el-dialog v-model="settingsVisible" title="课程推进设置" width="480px" destroy-on-close>
      <p class="settings-intro">
        以下设置对您的<strong>全部常规课</strong>统一生效。开启后，班级有成绩达到比例并等待指定时间，会自动切到下一课；也可随时用右上角「手动一键课堂推进」立即切换。
      </p>
      <el-form v-loading="policyLoading" label-width="120px" class="policy-form">
        <el-form-item label="自动推进">
          <el-switch v-model="policyForm.autoAdvanceEnabled" active-text="开启" inactive-text="关闭" />
        </el-form-item>
        <el-form-item label="有成绩达到">
          <el-input-number
            v-model="policyForm.autoAdvanceThresholdPct"
            :min="30"
            :max="100"
            :step="5"
            controls-position="right"
            style="width: 140px"
          />
          <span class="settings-unit">%</span>
        </el-form-item>
        <el-form-item label="等待时间">
          <el-input-number
            v-model="policyForm.autoAdvanceDelayHours"
            :min="0.5"
            :max="24"
            :step="0.5"
            :precision="1"
            controls-position="right"
            style="width: 140px"
            :disabled="!policyForm.autoAdvanceEnabled"
          />
          <span class="settings-unit">小时后自动推进</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="settingsVisible = false">取消</el-button>
        <el-button type="primary" :loading="policySaving" @click="saveAdvancePolicy">保存</el-button>
      </template>
    </el-dialog>

    <!-- 考勤签到名单 -->
    <el-dialog v-model="checkinDialogVisible" :title="checkinDialogTitle" width="640px" destroy-on-close>
      <div v-if="checkinMeta.total != null" class="checkin-summary">
        已签到 {{ checkinMeta.checkedInCount || 0 }} / {{ checkinMeta.total || 0 }} 人
      </div>
      <el-table v-loading="checkinLoading" :data="checkinRows" size="small" max-height="420">
        <el-table-column label="学号" prop="studentNo" width="120" />
        <el-table-column label="姓名" prop="studentName" min-width="100" />
        <el-table-column label="班级" prop="classCode" width="80">
          <template #default="{ row }">{{ row.classCode }}班</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.checkinId ? 'success' : 'info'" size="small">
              {{ row.checkinId ? '已签到' : '未签到' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="签到时间" min-width="160">
          <template #default="{ row }">
            {{ row.checkinTime ? formatCheckinTime(row.checkinTime) : '—' }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="TeacherDashboard">
import ResearchNotificationBar from '@/views/business/researchActivity/components/ResearchNotificationBar.vue'
import { computed, ref, onMounted, onActivated } from 'vue';
import { useRouter } from 'vue-router';
import { getDashboardData, getDashboardPracticalStatus } from '@/api/business/teacher';
import { getCountyExamGradingEntry } from '@/api/business/countyExam';
import {
  delLesson,
  getLessonCheckinRoster,
  getAdvancePolicy,
  updateAdvancePolicy,
  manualAdvanceLesson
} from '@/api/business/lesson';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Close, Edit, Check, DataLine, MoreFilled, DArrowRight, Setting } from '@element-plus/icons-vue';
import ClassSelectionDialog from './components/ClassSelectionDialog.vue';

const router = useRouter();
const loading = ref(true);
const gradeGroups = ref([]);

function hasUngradedPractical(lesson) {
  return (lesson.practicalDeadlineClasses || []).some(item => Number(item.ungradedCount || 0) > 0);
}

function goToExemption() {
  router.push('/teacher-exemption')
}
const classDialogRef = ref(null);
const expandedLessonKeys = ref(new Set());
const expandedHistoryKeys = ref(new Set());
const countyGradingEntry = ref({ hasTask: false, taskCount: 0 });
const pendingCountyGradingCount = computed(() => countyGradingEntry.value.pendingTaskCount ?? countyGradingEntry.value.taskCount ?? 0);
const checkinDialogVisible = ref(false);
const checkinDialogTitle = ref('签到名单');
const checkinLoading = ref(false);
const checkinRows = ref([]);
const checkinMeta = ref({});

// 统一推进设置
const settingsVisible = ref(false);
const policyLoading = ref(false);
const policySaving = ref(false);
const policyForm = ref({
  autoAdvanceEnabled: false,
  autoAdvanceThresholdPct: 50,
  autoAdvanceDelayHours: 2
});

// 手动一键课堂推进（多选班级，默认全选当前为常规课的班级）
const advanceDialogVisible = ref(false);
const advanceLoading = ref(false);
const advanceForm = ref({ entryYear: '', classCodes: [] });

function getAdvanceClassesForGroup(group) {
  const managedClasses = new Set((group?.allClassesInGrade || []).map(normalizeClassCode).filter(Boolean));
  const classCodes = (group?.lessons || [])
    .filter(lesson => lesson.lessonMode !== 'attendance' && Number(lesson.grade) === Number(group.gradeId))
    .flatMap(lesson => lesson.assignedClasses || [])
    .map(normalizeClassCode)
    .filter(classCode => classCode && managedClasses.has(classCode));
  return [...new Set(classCodes)].sort((left, right) => Number(left) - Number(right));
}

const advanceGradeOptions = computed(() =>
  (gradeGroups.value || []).filter(group => getAdvanceClassesForGroup(group).length > 0)
);

const advanceClassOptions = computed(() => {
  const group = (gradeGroups.value || []).find(g => String(g.entryYear) === String(advanceForm.value.entryYear));
  return getAdvanceClassesForGroup(group);
});

function normalizeClassCode(cls) {
  return String(cls || '').replace('班', '').trim();
}

function formatClassLabel(cls) {
  const code = normalizeClassCode(cls);
  return code ? `${code}班` : String(cls || '');
}

/** 默认选中当前年级所有当前为常规课的班级，考勤班级不进入请求。 */
function selectAllAdvanceClasses() {
  advanceForm.value.classCodes = advanceClassOptions.value.map(normalizeClassCode).filter(Boolean);
}

function compareLessonsByLatest(a, b) {
  const timeA = a.createTime ? new Date(a.createTime).getTime() : 0;
  const timeB = b.createTime ? new Date(b.createTime).getTime() : 0;
  if (timeA !== timeB) {
    return timeB - timeA;
  }
  return (b.lessonId || 0) - (a.lessonId || 0);
}

function formatGradeName(gradeId, fallback = '') {
  const grade = Number(gradeId);
  const names = ['', '一年级', '二年级', '三年级', '四年级', '五年级', '六年级', '七年级', '八年级', '九年级'];
  if (grade >= 1 && grade <= 9) return names[grade];
  if (grade === 10) return '高一';
  if (grade === 11) return '高二';
  if (grade === 12) return '高三';
  return fallback || '年级待核对';
}

function getCourseGradeSections(group) {
  const currentGrade = Number(group.gradeId);
  const lessonsByGrade = new Map();
  (group.lessons || []).forEach(lesson => {
    const grade = Number(lesson.grade);
    const key = Number.isFinite(grade) && grade > 0 ? grade : 'unknown';
    if (!lessonsByGrade.has(key)) lessonsByGrade.set(key, []);
    lessonsByGrade.get(key).push(lesson);
  });
  if (currentGrade > 0 && !lessonsByGrade.has(currentGrade)) {
    lessonsByGrade.set(currentGrade, []);
  }
  return [...lessonsByGrade.entries()]
    .map(([gradeId, lessons]) => {
      const numericGrade = Number(gradeId);
      const isCurrent = currentGrade > 0 && numericGrade === currentGrade;
      return {
        gradeId: Number.isFinite(numericGrade) ? numericGrade : null,
        gradeName: isCurrent ? group.gradeName : formatGradeName(numericGrade),
        isCurrent,
        isHistory: !isCurrent,
        canAdd: Number.isFinite(numericGrade) && numericGrade > 0,
        lessons: [...lessons].sort(compareLessonsByLatest)
      };
    })
    .sort((left, right) => {
      if (left.isCurrent) return -1;
      if (right.isCurrent) return 1;
      return Number(right.gradeId || -1) - Number(left.gradeId || -1);
    });
}

function getCourseSectionKey(group, section) {
  return `${group.entryYear || ''}-${section.gradeId ?? 'unknown'}`;
}

function isCourseSectionExpanded(group, section) {
  return section.isCurrent || expandedHistoryKeys.value.has(getCourseSectionKey(group, section));
}

function toggleHistorySection(group, section) {
  const key = getCourseSectionKey(group, section);
  const next = new Set(expandedHistoryKeys.value);
  if (next.has(key)) next.delete(key);
  else next.add(key);
  expandedHistoryKeys.value = next;
}

function getVisibleLessons(group, section) {
  if (section.isHistory || expandedLessonKeys.value.has(getCourseSectionKey(group, section))) {
    return section.lessons;
  }
  return section.lessons.slice(0, 5);
}

function hasHiddenLessons(group, section) {
  return section.isCurrent
    && !expandedLessonKeys.value.has(getCourseSectionKey(group, section))
    && section.lessons.length > 5;
}

function expandLessons(group, section) {
  const next = new Set(expandedLessonKeys.value);
  next.add(getCourseSectionKey(group, section));
  expandedLessonKeys.value = next;
}

let dashboardRequestSeq = 0;
let countyRequestSeq = 0;

async function fetchCountyGradingData() {
  const requestSeq = ++countyRequestSeq;
  try {
    const response = await getCountyExamGradingEntry();
    if (requestSeq === countyRequestSeq) {
      countyGradingEntry.value = response.data || { hasTask: false, pendingTaskCount: 0, taskCount: 0 };
    }
  } catch (e) {
    if (requestSeq === countyRequestSeq) {
      countyGradingEntry.value = { hasTask: false, pendingTaskCount: 0, taskCount: 0 };
    }
  }
}

async function fetchPracticalStatuses(groups, requestSeq) {
  const lessonIds = (groups || [])
    .flatMap(group => group.lessons || [])
    .filter(lesson => lesson.hasPractical)
    .map(lesson => lesson.lessonId)
    .filter(Boolean);
  if (!lessonIds.length) return;
  try {
    const response = await getDashboardPracticalStatus([...new Set(lessonIds)]);
    if (requestSeq !== dashboardRequestSeq) return;
    const statusByLesson = response.data || {};
    gradeGroups.value = gradeGroups.value.map(group => ({
      ...group,
      lessons: (group.lessons || []).map(lesson => ({
        ...lesson,
        practicalDeadlineClasses: statusByLesson[String(lesson.lessonId)] || []
      }))
    }));
  } catch (e) {
    // 红点加载失败不影响已经显示的课程卡片。
  }
}

/** 先加载课程核心数据，批改红点和区域抽测入口不再阻塞首屏。 */
async function fetchDashboardData() {
  const requestSeq = ++dashboardRequestSeq;
  loading.value = true;
  try {
    const response = await getDashboardData();
    if (requestSeq !== dashboardRequestSeq) return;
    const groups = (response.data || []).map(group => ({
      ...group,
      lessons: [...(group.lessons || [])].sort(compareLessonsByLatest)
    }));
    gradeGroups.value = groups;
    loading.value = false;
    fetchPracticalStatuses(groups, requestSeq);
  } catch (e) {
    if (requestSeq === dashboardRequestSeq) {
      loading.value = false;
    }
  }
}

function goToCountyExamGrading() {
  router.push('/business/county-exam-grading');
}

/** 新建课按目标开设年级独立编号；历史入口必须再次说明目标年级。 */
async function handleAddNewLesson(group, section) {
  if (section.isHistory) {
    try {
      await ElMessageBox.confirm(
        `将为${group.entryYear}级新增一门${section.gradeName}历史课程，课次会在该年级内继续计算。确认继续？`,
        '添加历史课程',
        { type: 'warning', confirmButtonText: '确认添加', cancelButtonText: '取消' }
      );
    } catch (e) {
      return;
    }
  }
  const maxLessonNum = section.lessons.length > 0
    ? Math.max(...section.lessons.map(l => l.lessonNum || 0))
    : 0;
  router.push({
    path: '/business/lesson-auth/designer',
    query: {
      grade: section.gradeId,
      entryYear: group.entryYear,
      gradeName: group.gradeName,
      classes: JSON.stringify(group.allClassesInGrade),
      nextNum: maxLessonNum + 1
    }
  });
}

async function openCourseSettings() {
  settingsVisible.value = true;
  policyLoading.value = true;
  try {
    const res = await getAdvancePolicy();
    const data = res.data || res || {};
    policyForm.value = {
      autoAdvanceEnabled: Boolean(data.autoAdvanceEnabled),
      autoAdvanceThresholdPct: Number(data.autoAdvanceThresholdPct) || 50,
      autoAdvanceDelayHours: Number(data.autoAdvanceDelayHours) || 2
    };
  } catch (e) {
    policyForm.value = {
      autoAdvanceEnabled: false,
      autoAdvanceThresholdPct: 50,
      autoAdvanceDelayHours: 2
    };
  } finally {
    policyLoading.value = false;
  }
}

async function saveAdvancePolicy() {
  policySaving.value = true;
  try {
    await updateAdvancePolicy({
      autoAdvanceEnabled: Boolean(policyForm.value.autoAdvanceEnabled),
      autoAdvanceThresholdPct: Number(policyForm.value.autoAdvanceThresholdPct) || 50,
      autoAdvanceDelayHours: Number(policyForm.value.autoAdvanceDelayHours) || 2
    });
    ElMessage.success('已保存，全部常规课已同步');
    settingsVisible.value = false;
  } catch (e) {
    // 全局拦截器提示
  } finally {
    policySaving.value = false;
  }
}

/** 右上角：手动一键课堂推进（默认多选当前年级全部常规课班级） */
async function handleOneClickAdvance() {
  if (!advanceGradeOptions.value.length) {
    ElMessage.warning('暂无可推进的常规课班级');
    return;
  }
  // 带出统一阈值，便于确认文案准确
  try {
    const res = await getAdvancePolicy();
    const data = res.data || res || {};
    policyForm.value.autoAdvanceThresholdPct = Number(data.autoAdvanceThresholdPct) || 50;
  } catch (e) {
    policyForm.value.autoAdvanceThresholdPct = 50;
  }
  const first = advanceGradeOptions.value[0];
  advanceForm.value = {
    entryYear: first.entryYear || '',
    classCodes: []
  };
  // 等 options 就绪后默认全选当前年级的常规课班级
  advanceDialogVisible.value = true;
  selectAllAdvanceClasses();
}

function onAdvanceGradeChange() {
  // 切换年级后默认全选该年级当前为常规课的班级
  selectAllAdvanceClasses();
}

async function confirmOneClickAdvance() {
  const entryYear = advanceForm.value.entryYear;
  const classCodes = (advanceForm.value.classCodes || []).map(normalizeClassCode).filter(Boolean);
  if (!entryYear) {
    ElMessage.warning('请选择年级');
    return;
  }
  if (!classCodes.length) {
    ElMessage.warning('请至少选择一个班级');
    return;
  }
  const threshold = Number(policyForm.value.autoAdvanceThresholdPct) || 50;
  const classLabel = classCodes.length === advanceClassOptions.value.length
    ? `全部 ${classCodes.length} 个班`
    : `${classCodes.length} 个班（${classCodes.map(c => c + '班').join('、')}）`;
  try {
    await ElMessageBox.confirm(
      `确认将 ${classLabel} 的当前课程推进到下一课？\n需各班有成绩人数达到 ${threshold}%。未达标的班级会跳过并提示。`,
      '手动一键课堂推进',
      { type: 'warning', confirmButtonText: '确认推进', cancelButtonText: '取消' }
    );
  } catch {
    return;
  }
  advanceLoading.value = true;
  try {
    const res = await manualAdvanceLesson({
      entryYear,
      classCodes
    });
    const msg = res.msg || res.data?.message || '推进完成';
    const failed = Number(res.data?.failed || 0);
    if (failed > 0) {
      ElMessage.warning(msg);
    } else {
      ElMessage.success(msg);
    }
    advanceDialogVisible.value = false;
    fetchDashboardData();
  } catch (e) {
    // 全局拦截器已提示业务错误
  } finally {
    advanceLoading.value = false;
  }
}

/** 处理修改课程 (设计) */
function handleEditLesson(lesson, group) {
  router.push({
    path: `/business/lesson-auth/designer/${lesson.lessonId}`,
    query: { entryYear: lesson.entryYear || group.entryYear }
  });
}

function formatCheckinTime(value) {
  try {
    return new Date(value).toLocaleString();
  } catch (e) {
    return String(value || '');
  }
}

/** 查看考勤课签到名单 */
async function openCheckinRoster(lesson, group) {
  const selectedClass = await classDialogRef.value.open(group.allClassesInGrade, lesson.lessonId, 'score');
  if (!selectedClass) return;
  const pureClass = String(selectedClass).replace('班', '').trim();
  checkinDialogTitle.value = `${lesson.lessonTitle || '课程'} · ${pureClass}班签到`;
  checkinDialogVisible.value = true;
  checkinLoading.value = true;
  checkinRows.value = [];
  checkinMeta.value = {};
  try {
    const res = await getLessonCheckinRoster({
      lessonId: lesson.lessonId,
      entryYear: group.entryYear,
      classCode: pureClass
    });
    checkinRows.value = res.data || [];
    checkinMeta.value = {
      total: res.total,
      checkedInCount: res.checkedInCount
    };
  } catch (e) {
    checkinDialogVisible.value = false;
  } finally {
    checkinLoading.value = false;
  }
}

/** 跳转批改 */
async function goToGrading(lesson, group) {
  // 1. 选择班级
  // 1. 选择班级 (传入 lessonId 以获取批改状态)
  const selectedClass = await classDialogRef.value.open(null, lesson.lessonId);
  if (!selectedClass) return; // 用户取消

  // 2. 跳转
  router.push({
      path: '/business/teacher/grading',
      query: {
         lessonId: lesson.lessonId,
         classCode: selectedClass
      }
  });
}

/** 跳转成绩分析 */
async function goToScoreAnalysis(lesson, group) {
  // 1. 选择班级
  const selectedClass = await classDialogRef.value.open(group.allClassesInGrade, lesson.lessonId, 'score');
  if (!selectedClass) return; // 用户取消

  // 2. 跳转
  const entryYear = lesson.entryYear || group.entryYear;
  if (String(entryYear) !== String(group.entryYear)) {
    ElMessage.error('课程所属入学年份与当前分组不一致，请刷新后重试');
    return;
  }
  router.push({
    path: '/business/score',
    query: {
      lessonId: lesson.lessonId,
      entryYear,
      classCode: selectedClass
    }
  });
}

/** 删除课程 */
function handleDeleteLesson(lessonId) {
  ElMessageBox.confirm(
    '是否确认删除该课程？此操作将同时删除所有关联的题目和班级指派，且不可恢复。',
    '警告',
    {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(() => {
      delLesson(lessonId).then(() => {
        ElMessage({
          type: 'success',
          message: '删除成功'
        });
        fetchDashboardData();
      });
    })
    .catch(() => {});
}

function refreshDashboard() {
  fetchDashboardData();
  fetchCountyGradingData();
}

let skipFirstActivatedRefresh = true;
onMounted(() => {
  refreshDashboard();
});

// 从其他页面返回时（如课程设计页），重新加载数据
onActivated(() => {
  // KeepAlive 首次挂载会紧接着触发 activated，跳过这一次以免首页重复请求。
  if (skipFirstActivatedRefresh) {
    skipFirstActivatedRefresh = false;
    return;
  }
  refreshDashboard();
});
</script>

<style scoped lang="scss">
.teacher-dashboard {
  .box-card {
    min-height: calc(100vh - 84px);
  }
}

.county-grading-entry {
  min-height: 64px;
  border: 1px solid #b3d8ff;
  border-radius: 8px;
  background: #ecf5ff;
  margin-bottom: 12px;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;

  strong,
  span {
    display: block;
  }

  strong {
    color: #1f2937;
    margin-bottom: 4px;
  }

  span {
    color: #606266;
    font-size: 13px;
  }
}

.grade-group {
  & + & {
    margin-top: 28px;
  }
}

.grade-header {
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.grade-title {
  color: #303133;
  font-size: 17px;
  font-weight: 700;
}

.course-grade-section {
  margin-bottom: 16px;
  padding: 12px 14px 14px;
  border: 1px solid #d9ecff;
  border-radius: 10px;
  background: #f7fbff;

  &.is-history {
    border-color: #e4e7ed;
    background: #fafafa;
  }
}

.course-grade-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 28px;
  margin-bottom: 12px;

  &.clickable {
    cursor: pointer;
  }
}

.course-grade-heading {
  display: flex;
  align-items: center;
  gap: 8px;

  strong {
    color: #303133;
    font-size: 15px;
  }

  > span:last-child {
    color: #909399;
    font-size: 12px;
  }
}

.history-toggle-text {
  color: #409eff;
  font-size: 13px;
}

.lesson-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: stretch;
}

.lesson-folder {
  position: relative;
  width: 200px;
  height: 140px;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 2px 6px rgba(0,0,0,0.04);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  overflow: hidden;
  border: 1px solid #ebeef5;
  display: flex;
  flex-direction: row; 
}

.lesson-folder:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.08);
  border-color: #c6e2ff;
}

/* 左侧：内容区 */
.folder-content {
  flex: 1; 
  display: flex;
  flex-direction: row;
  align-items: stretch;
  cursor: pointer;
  position: relative;
  background-color: #fff;
}

/* 竖排课程标题 (实为窄列自动换行) */
.folder-title-vertical {
  width: 55px; /* 进一步收窄，每行约2字 */
  height: 100%; 
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
  padding: 12px 4px; /* 减少内边距 */
  display: flex;
  align-items: flex-start; 
  justify-content: center; 
  overflow: hidden;
  text-overflow: ellipsis;
  
  /* 强制换行控制 */
  word-break: break-all;
  white-space: normal;
  text-align: center;
  line-height: 1.3;
  
  border-right: 1px solid #f0f2f5;
  box-sizing: border-box;
  background-color: #f5f7fa;
}

.folder-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 8px; /* 稍微减少内边距 */
  overflow: hidden; /* 防止溢出 */
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.attendance-mode-tag {
  margin-top: 4px;
  font-size: 11px;
  color: #e6a23c;
  font-weight: 600;
}
.checkin-summary {
  margin-bottom: 10px;
  color: #606266;
  font-size: 13px;
}
.settings-intro {
  margin: 0 0 16px;
  color: #606266;
  font-size: 13px;
  line-height: 1.55;
}
.settings-unit {
  margin-left: 8px;
  color: #909399;
  font-size: 13px;
}
.policy-form {
  padding-right: 8px;
}
.class-multi-tools {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  width: 100%;
}
.class-multi-count {
  margin-left: auto;
  color: #909399;
  font-size: 12px;
}
.lesson-count-tag {
  font-size: 12px; 
  color: #909399;
  text-align: right; /* 页码右对齐好看些 */
  margin-top: 4px;
}

.grading-red-dot {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 8px;
  height: 8px;
  border: 2px solid #fff;
  border-radius: 50%;
  background: #f56c6c;
  box-sizing: content-box;
}

/* 已指派班级区域 */
.assigned-classes {
  display: flex;
  flex-direction: row; /*改为横向 */
  flex-wrap: wrap;     /* 允许换行 */
  gap: 4px;
  align-content: flex-start;
  height: 100%;
  overflow-y: auto; /*如果班级太多允许滚动 */
}

.assigned-tag {
  display: inline-block;
  width: calc(50% - 2px); /* 一行两个 */
  padding: 2px 0;         /* 减小左右padding用居中代替 */
  font-size: 12px;
  font-weight: normal;
  background-color: #e1f3d8;
  color: #67c23a;
  border-radius: 4px;
  text-align: center;
  box-sizing: border-box;
  text-overflow: ellipsis;
  overflow: hidden;
  white-space: nowrap;
}

/* 右侧：操作区 */
.folder-actions {
  width: 60px; /* 保持60px */
  display: flex;
  flex-direction: column;
  border-left: 1px solid #f0f2f5;
  background-color: #fbfbfb;
}

.action-btn {
  position: relative;
  flex: 1;
  display: flex;
  flex-direction: column; 
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s;
  gap: 2px;
  
  &:hover {
     background-color: #fff;
     font-weight: 600;
  }
  
  &.design:hover { color: #409EFF; background-color: #ecf5ff; }
  &.grade:hover { color: #67C23A; background-color: #f0f9eb; }
  &.score:hover { color: #E6A23C; background-color: #fdf6ec; }
  
  &:not(:last-child) {
     border-bottom: 1px solid #f0f2f5;
  }
}

/* 共享标签 */
.shared-tag {
  display: inline-block;
  padding: 2px 6px;
  margin-left: 4px;
  font-size: 10px;
  color: #fff;
  background: linear-gradient(135deg, #67c23a, #529b2e);
  border-radius: 4px;
  vertical-align: middle;
  font-weight: normal;
  transform: translateY(-2px);
}

/* 删除按钮 */
.folder-delete {
  position: absolute;
  top: 4px;
  left: auto;
  right: 65px; 
  
  opacity: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 4px;
  color: #909399;
  cursor: pointer;
  transition: all 0.2s;
  z-index: 10;
}
.lesson-folder:hover .folder-delete {
    opacity: 1;
}
.folder-delete:hover {
  background-color: #fef0f0;
  color: #f56c6c;
}

.expand-lessons-btn {
  width: 200px;
  height: 140px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s;
  border: 1px dashed #c0c4cc;
  color: #606266;
  background-color: #f7f9fb;
}
.expand-lessons-btn:hover {
  color: #409eff;
  border-color: #409eff;
  background-color: #ecf5ff;
}
.more-icon {
  font-size: 32px;
  margin-bottom: 8px;
}
.more-text {
  font-size: 13px;
  color: inherit;
}

/* 新增按钮 */
.add-lesson-btn {
  width: 200px; /* 匹配卡片宽度 */
  height: 140px; 
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s;
  border: 1px dashed #dcdfe6;
  color: #909399;
  background: transparent;
}
.add-lesson-btn:hover {
  color: #409eff;
  border-color: #409eff;
  background-color: rgba(64,158,255,0.04);
}
.add-icon {
    font-size: 28px; /* 加大图标 */
    margin-bottom: 8px;
}
</style>
