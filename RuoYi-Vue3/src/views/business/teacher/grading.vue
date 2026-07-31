<template>
  <div class="app-container grading-page" ref="gradingPageRef">
    <!-- 顶部控制栏 -->
    <div class="grading-header" v-show="!isFullscreen">
      <div class="left-filters">
        <span class="filter-label">课程：</span>
        <el-select v-model="selectedLessonId" placeholder="请选择课程" @change="onLessonChange" style="width: 200px">
          <el-option-group v-for="group in gradeGroups" :key="group.entryYear" :label="group.entryYear + '级 ' + group.gradeName">
            <el-option v-for="l in group.lessons" :key="l.lessonId" :label="l.lessonTitle" :value="l.lessonId" />
          </el-option-group>
        </el-select>
        
        <span class="filter-label" style="margin-left: 16px">班级：</span>
        <el-select v-model="selectedClassCode" placeholder="请选择班级" @change="onClassChange" :disabled="!selectedLessonId || classes.length === 0" style="width: 180px">
          <el-option v-for="c in classes" :key="c.classCode" :value="c.classCode">
            <div class="class-option" :class="getClassOptionClass(c)">
              <span>{{ c.classCode }}班</span>
              <span v-if="c.practicalUngraded > 0" class="ungraded-badge">{{ c.practicalUngraded }}人未批</span>
              <span v-else-if="c.practicalSubmitted > 0" class="graded-badge">✓</span>
              <span v-else class="no-submit-badge">暂无提交</span>
            </div>
          </el-option>
        </el-select>
        
        <!-- 无班级提示 -->
        <el-tag v-if="selectedLessonId && classes.length === 0 && !loading" type="warning" style="margin-left: 8px">
          暂无学生提交作业
        </el-tag>
        
        <span class="filter-label" style="margin-left: 16px">操作题：</span>

        <!-- 只有一道操作题时直接显示题目名称 -->
        <span v-if="questions.length === 1" class="single-question-name" style="font-weight: 500; color: #303133; max-width: 280px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: inline-block; vertical-align: middle; line-height: 32px; height: 32px; padding: 0 11px; border: 1px solid #dcdfe6; border-radius: 4px; background: #f5f7fa;">
          {{ questions[0].questionContent }}
        </span>
        <!-- 多道操作题时显示下拉框 -->
        <el-select v-else v-model="selectedQuestionId" placeholder="请选择操作题" @change="onQuestionChange" :disabled="!selectedClassCode" style="width: 280px">
          <el-option v-for="q in questions" :key="q.questionId" :label="q.questionContent" :value="q.questionId" />
        </el-select>
      </div>
      
      <div class="right-actions">
        <el-button
          type="warning"
          plain
          :loading="retryLoading"
          :disabled="!canRetryFailedPreviews"
          @click="handleRetryFailedPreviews"
        >
          重新转换本班异常文件
        </el-button>
        <el-button type="primary" plain @click="toggleFullscreen">
           <el-icon><FullScreen /></el-icon> {{ isFullscreen ? '退出全屏' : '全屏批改' }}
        </el-button>
      </div>
    </div>

    <el-card
      v-if="selectedClassCode && deadlineStatus"
      shadow="never"
      class="deadline-status-panel"
      :class="`is-${String(deadlineStatus.statusCode || '').toLowerCase()}`"
    >
      <div class="deadline-panel-head">
        <div>
          <strong>{{ selectedLessonTitle }} · {{ selectedClassCode }}班操作题批改时限</strong>
          <el-tag :type="deadlineStatusMeta(deadlineStatus.statusCode).type">
            {{ deadlineStatusMeta(deadlineStatus.statusCode).label }}
          </el-tag>
        </div>
        <strong>{{ formatDeadlineRemaining(deadlineStatus) }}</strong>
      </div>
      <el-alert
        v-if="!deadlineStatus.canGrade"
        title="已逾期，操作题批改已锁定"
        description="已有成绩和学生提交仍可查看；如需继续批改，请联系教研员调整截止时间。"
        type="error"
        :closable="false"
        show-icon
      />
      <div class="deadline-panel-grid">
        <span>已有答题记录：{{ deadlineStatus.answeredStudentCount }}/{{ deadlineStatus.totalStudentCount }}</span>
        <span>触发时间：{{ formatDeadlineTime(deadlineStatus.triggerTime) }}</span>
        <span>截止时间：{{ formatDeadlineTime(deadlineStatus.currentDeadlineTime) }}</span>
        <span>应批/已批/未批：{{ deadlineStatus.dueCount }}/{{ deadlineStatus.gradedCount }}/{{ deadlineStatus.ungradedCount }}</span>
      </div>
      <el-progress
        :percentage="deadlineStatus.dueCount ? Math.round(deadlineStatus.gradedCount * 100 / deadlineStatus.dueCount) : 0"
        :status="deadlineStatus.statusCode === 'COMPLETED' ? 'success' : undefined"
      />
    </el-card>

    <!-- 主工作区 -->
    <div class="grading-main" v-loading="loading">
      <!-- 左侧：学生列表 -->
      <div class="student-list-panel">
         <div class="panel-title">
            <span>学生列表</span>
             <span class="grading-stats" v-if="selectedClassCode">
               已交: <b class="score-num">{{ submittedCount }}</b> / <span class="score-num">{{ currentClassTotalStudents }}</span>
               <span style="margin: 0 6px; color: #dcdfe6">|</span>
               已批: <b class="score-num">{{ gradedCount }}</b> / <span class="score-num">{{ submittedCount }}</span>
            </span>
         </div>
         <div class="student-list-scroll">
            <div 
               v-for="(s, index) in submissions" 
               :key="s.studentId" 
               class="student-item"
               :class="{ 
                  'active': currentStudent?.studentId === s.studentId, 
                  'graded': s.submitted && s.score != null,
                  'not-submitted': !s.submitted
               }"
               @click="s.submitted ? selectStudent(s, index) : null"
            >
               <div class="s-info">
                   <div class="s-name" :style="s.remark ? { color: '#E6A23C' } : {}">{{ s.studentName }}</div>
                   <div class="s-remark" v-if="s.remark">{{ s.remark }}</div>
                   <div class="s-no">{{ s.studentNo }}</div>
                   <div
                     v-if="s.submitted"
                     class="s-preview-status"
                     :class="getPreviewStatusClass(s.previewStatus)"
                   >
                     {{ getPreviewStatusText(s) }}
                   </div>
               </div>
               <div class="s-status" v-if="!s.submitted">未交</div>
               <div class="s-status score-num" v-else-if="s.score != null">{{ s.score }}分</div>
               <div class="s-status ungrad" v-else>未批</div>
            </div>
            <el-empty v-if="submissions.length === 0" description="暂无学生" :image-size="60" />
         </div>
      </div>

      <!-- 中间：预览区 -->
      <div class="preview-panel">
         <div v-if="currentStudent" class="preview-content">
             <div class="preview-header">
                 <div class="header-info">
                    <span class="student-label">{{ currentStudent.studentName }} 的提交作品</span>
                    <span v-if="currentStudent.studentAnswer" class="file-name">📄 {{ getFileName(currentStudent.studentAnswer) }}</span>
                 </div>
                 <a v-if="currentStudent.studentAnswer" :href="getFileUrl(currentStudent.studentAnswer)" target="_blank" class="download-link">下载源文件</a>
             </div>
             <iframe 
                v-if="previewUrl" 
                :src="previewUrl" 
                class="pdf-frame" 
                frameborder="0"
             ></iframe>
             <el-alert
                v-else-if="currentStudent.previewStatus === 'pending'"
                title="作品已交卷，预览排队中"
                description="交卷已成功；预览转换与批改评分互不阻断，可先下载源文件。"
                type="info"
                :closable="false"
                show-icon
             />
             <el-alert
                v-else-if="currentStudent.previewStatus === 'converting'"
                title="预览转换中"
                description="交卷已成功，正在生成 PDF 预览，请稍候或下载源文件批改。"
                type="info"
                :closable="false"
                show-icon
             />
             <el-alert
                v-else-if="currentStudent.previewStatus === 'failed'"
                title="预览失败（交卷仍有效）"
                :description="(currentStudent.previewErrorMessage || '转换失败') + '。可下载源文件批改，或使用「重新转换本班异常文件」。'"
                type="warning"
                :closable="false"
                show-icon
             />
             <el-empty v-else description="该生未提交文件或文件不可预览" />
         </div>
         <el-empty v-else description="请从左侧选择一名学生开始批改" />
      </div>

      <!-- 右侧：打分面板 -->
      <div class="scoring-panel" v-if="currentStudent && currentStudent.submitted">
         <div class="score-card">
            <div class="card-title">批改打分</div>
            
            <div class="question-info">
                <div class="q-score">满分：{{ currentStudent.maxScore }} 分</div>
            </div>
            
            <!-- P6: 评分模式切换 -->
            <div class="scoring-mode-switch" v-if="scoringItems.length > 0">
               <el-switch 
                  v-model="useItemScoring" 
                  :disabled="!deadlineStatus?.canGrade"
                  active-text="分项评分" 
                  inactive-text="直接打分"
                  @change="onScoringModeChange"
               />
            </div>

            <!-- 直接打分模式 -->
            <div class="score-input-area" v-if="!useItemScoring">
                <div class="input-label">得分：</div>
                <el-input-number 
                   v-model="currentScore" 
                   :disabled="!deadlineStatus?.canGrade"
                   :min="0" 
                   :max="currentStudent.maxScore" 
                   :precision="0"
                   controls-position="right"
                   size="large"
                   ref="scoreInputRef"
                   @keyup.enter="submitScore"
                />
            </div>
            
            <!-- P6: 分项评分模式 -->
            <div class="item-scoring-area" v-else>
                  <div v-for="(item, index) in scoringItems" :key="item.itemId" class="item-row">
                     <span class="item-name">{{ item.itemName }}</span>
                     <div class="item-input">
                        <el-input-number 
                           :ref="el => setItemInputRef(el, index)"
                           v-model="itemScores[item.itemId]" 
                           :disabled="!deadlineStatus?.canGrade"
                           :min="0" 
                           :max="Math.round(item.itemScore * scalingRatio)" 
                           :precision="0"
                           size="small"
                           @change="onItemScoreChange"
                           @keydown.enter="onItemEnter(index)"
                        />
                        <span class="item-max">/ {{ Math.round(item.itemScore * scalingRatio) }} 分</span>
                     </div>
                  </div>
                  <div class="item-total">
                     分项合计: <span class="total-score score-num">{{ itemTotalScore }}</span> / <span class="score-num">{{ currentQuestionScore }}</span> 分
                  </div>
            </div>
            
            <el-button type="primary" size="large" class="submit-btn" :disabled="!deadlineStatus?.canGrade" @click="submitScore">
               提交并下一位 (Enter)
            </el-button>
            
            <div class="nav-actions">
               <el-button @click="prevStudent" :disabled="currentIndex <= 0">上一位 (PgUp)</el-button>
               <el-button @click="nextStudent" :disabled="currentIndex >= submissions.length - 1">下一位 (PgDn)</el-button>
            </div>
         </div>
      </div>
    </div>
  </div>
</template>

<script setup name="TeacherGrading">
import { ref, computed, onMounted, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import { getDashboardData } from '@/api/business/teacher';
import { getClassesByLesson, getPracticalQuestions, getPracticalSubmissions, retryFailedPreviews, getPracticalDeadlineStatus } from '@/api/business/teacherGrading';
import { getScoringItems, getScoringDetails } from '@/api/business/scoringItem';  // P6
import { getToken } from '@/utils/auth';  // P6 fix: use Cookies token
import { ElMessage, ElMessageBox } from 'element-plus';
import { FullScreen } from '@element-plus/icons-vue';
import { deadlineStatusMeta, formatDeadlineRemaining, formatDeadlineTime } from '@/utils/practicalDeadline';

const route = useRoute();
const loading = ref(false);
const STUCK_PREVIEW_TIMEOUT_MS = 10 * 60 * 1000;
const gradeGroups = ref([]);
const lessons = ref([]);
const classes = ref([]);        // P3.5: 班级列表
const questions = ref([]);
const submissions = ref([]);

const selectedLessonId = ref(null);
const selectedClassCode = ref(null);  // P3.5: 选中的班级
const selectedQuestionId = ref(null);
const deadlineStatus = ref(null);

const selectedLessonTitle = computed(() => {
    for (const group of gradeGroups.value) {
        const lesson = group.lessons?.find(item => item.lessonId === selectedLessonId.value);
        if (lesson) return lesson.lessonTitle;
    }
    return '当前课程';
});

const currentStudent = ref(null);
const currentIndex = ref(-1);
const currentScore = ref(undefined);
const previewUrl = ref('');
const retryLoading = ref(false);

const isFullscreen = ref(false);
const gradingPageRef = ref(null);
const scoreInputRef = ref(null);

// P6: 分项评分相关状态
const scoringItems = ref([]);      // 评分项列表
const itemScores = ref({});        // 各评分项得分 { itemId: score }
const useItemScoring = ref(false); // 是否使用分项评分

// P6.1: 计算当前题目在课程中的设定的总分
const currentQuestionScore = computed(() => {
    if (!selectedQuestionId.value) return 100;
    const q = questions.value.find(item => item.questionId === selectedQuestionId.value);
    // 如果没有找到或未设置分数，默认100
    // 注意：biz_lesson_question 中字段是 questionScore
    return q ? (q.questionScore || 100) : 100;
});

// P6.1: 计算评分项定义的总分基数
const itemTotalBaseScore = computed(() => {
    if (!scoringItems.value || scoringItems.value.length === 0) return 0;
    return scoringItems.value.reduce((sum, item) => sum + (item.itemScore || 0), 0);
});

// P6.1: 计算折算倍率
const scalingRatio = computed(() => {
    const base = itemTotalBaseScore.value;
    if (base === 0) return 1;
    return currentQuestionScore.value / base;
});

// P6: 计算分项得分总和 (自动应用了输入框中的折算后分数)
const itemTotalScore = computed(() => {
    let total = 0;
    for (const key in itemScores.value) {
        total += itemScores.value[key] || 0;
    }
    // 确保总分不超过题目满分 (四舍五入取整? 或者保留一位小数? 这里通常是整数)
    return Math.round(total);
});

// 初始化加载课程数据
onMounted(() => {
  fetchDashboardData();
  document.addEventListener('keydown', handleGlobalKeydown);
  
  // 检查URL参数
  const queryLessonId = route.query.lessonId;
  if (queryLessonId) {
    selectedLessonId.value = parseInt(queryLessonId);
    // 等待数据加载后触发change，或者直接触发
    onLessonChange(selectedLessonId.value);
  }
});

function handleGlobalKeydown(e) {
  if (e.key === 'PageUp') prevStudent();
  if (e.key === 'PageDown') nextStudent();
}

function fetchDashboardData() {
  getDashboardData().then(res => {
    gradeGroups.value = res.data;
  });
}

// 根据班级批改状态返回样式类
function getClassOptionClass(classItem) {
  if (classItem.practicalUngraded > 0) {
    return 'has-ungraded';
  } else if (classItem.practicalSubmitted > 0) {
    return 'all-graded';
  }
  return 'no-submit';
}

// P3.5: 选择课程后加载班级列表
async function onLessonChange(val) {
  selectedClassCode.value = null;
  selectedQuestionId.value = null;
  classes.value = [];
  questions.value = [];
  submissions.value = [];
  currentStudent.value = null;
  
  if (val) {
    // 先加载操作题列表（需要在班级选择前完成，以便自动选择）
    const questionsRes = await getPracticalQuestions(val);
    questions.value = questionsRes.data || [];
    
    // 加载班级列表
    getClassesByLesson(val).then(res => {
        classes.value = res.data;
        
        // 检查URL参数中的 classCode，自动选中
        const queryClassCode = route.query.classCode;
        if (queryClassCode && classes.value.some(c => c.classCode === queryClassCode)) {
            selectedClassCode.value = queryClassCode;
            onClassChange(selectedClassCode.value);
        } else if (classes.value.length === 1) {
            // 如果只有一个班级，自动选中
            selectedClassCode.value = classes.value[0].classCode;
            onClassChange(selectedClassCode.value);
        }
    });
  }
}

// P3.5: 选择班级后加载操作题提交记录
function onClassChange(val) {
    if (!val) return;
    loadDeadlineStatus();
    // 如果已选择了题目，则加载该题目的提交记录
    if (selectedQuestionId.value) {
        loadSubmissions();
        loadScoringItems(); // P6: 也要加载评分项
    } else if (questions.value.length > 0) {
        // 自动选择第一个题目
        selectedQuestionId.value = questions.value[0].questionId;
        loadSubmissions();
        loadScoringItems(); // P6: 也要加载评分项
    }
}

function onQuestionChange(val) {
    if (val && selectedClassCode.value) {
        loadSubmissions();
        // P6: 加载评分项
        loadScoringItems();
    }
}

// P6: 加载评分项
function loadScoringItems() {
    if (!selectedLessonId.value || !selectedQuestionId.value) return;
    getScoringItems(selectedLessonId.value, selectedQuestionId.value).then(res => {
        scoringItems.value = res.data || [];
        // 重置分项得分
        itemScores.value = {};
        scoringItems.value.forEach(item => {
            itemScores.value[item.itemId] = 0;
        });
        // 如果有评分项，默认使用分项评分
        useItemScoring.value = scoringItems.value.length > 0;
    });
}

// P6: 评分模式切换
function onScoringModeChange(useItem) {
    if (useItem) {
        // 切换到分项评分时，重置分项得分
        scoringItems.value.forEach(item => {
            itemScores.value[item.itemId] = 0;
        });
    }
}

function onItemScoreChange() {
    // 可以在此添加额外的逻辑，当前仅依赖computed属性即可
}

// P6: 评分项输入框引用数组
const itemInputRefs = ref([]);

// P6: 设置评分项输入框引用
function setItemInputRef(el, index) {
    if (el) {
        itemInputRefs.value[index] = el;
    }
}

// P6: 回车切换下一项或提交
function onItemEnter(index) {
    if (index < scoringItems.value.length - 1) {
        // 还有下一项，聚焦下一个输入框
        nextTick(() => {
            const nextInput = itemInputRefs.value[index + 1];
            if (nextInput && nextInput.$el) {
                const input = nextInput.$el.querySelector('input');
                if (input) {
                    input.focus();
                    input.select(); // 自动选中内容，方便直接输入
                }
            }
        });
    } else {
        // 最后一项，提交并切换到下一个学生
        submitScore();
    }
}

// P6: 聚焦第一个评分项输入框
function focusFirstItem() {
    nextTick(() => {
        // 增加延时确保 itemInputRefs 已更新
        setTimeout(() => {
            if (itemInputRefs.value.length > 0 && itemInputRefs.value[0]) {
                const input = itemInputRefs.value[0].$el?.querySelector('input');
                if (input) {
                    input.focus();
                    input.select(); // 自动选中内容
                }
            }
        }, 50);
    });
}

// 加载提交记录
function loadSubmissions() {
    if (!selectedLessonId.value || !selectedQuestionId.value || !selectedClassCode.value) return;
    
    // P5: 获取当前班级的entryYear
    const classInfo = classes.value.find(c => c.classCode === selectedClassCode.value);
    const entryYear = classInfo?.entryYear || '';
    const previousStudentId = currentStudent.value?.studentId;
    
    loading.value = true;
    getPracticalSubmissions(selectedLessonId.value, selectedQuestionId.value, selectedClassCode.value, entryYear).then(res => {
        submissions.value = res.data;
        loading.value = false;
        const preservedStudent = previousStudentId != null
            ? submissions.value.find(s => s.studentId === previousStudentId && s.submitted)
            : null;
        const nextStudent = preservedStudent || submissions.value.find(s => s.submitted);
        if (nextStudent) {
            const idx = submissions.value.findIndex(s => s.studentId === nextStudent.studentId);
            selectStudent(nextStudent, idx);
            return;
        }
        currentStudent.value = null;
        currentIndex.value = -1;
        currentScore.value = undefined;
        previewUrl.value = '';
    }).catch(() => {
        loading.value = false;
        submissions.value = [];
        currentStudent.value = null;
        currentIndex.value = -1;
        currentScore.value = undefined;
        previewUrl.value = '';
        ElMessage.error('加载学生提交记录失败');
    });
}

const gradedCount = computed(() => submissions.value.filter(s => s.submitted && s.score != null).length);

// 已提交学生数量
const submittedCount = computed(() => submissions.value.filter(s => s.submitted).length);
function isRecoverablePreview(student) {
    if (!student?.submitted || student.previewPath) return false;
    const answerPath = (student.studentAnswer || '').toLowerCase();
    const isWordFile = answerPath.endsWith('.docx') || answerPath.endsWith('.doc');
    if (!isWordFile) return false;
    if (student.previewStatus === 'failed') return true;
    if (student.previewStatus !== 'pending' && student.previewStatus !== 'converting') return false;
    const referenceTime = student.previewLastRetryTime || student.submitTime;
    if (!referenceTime) return false;
    return Date.now() - new Date(referenceTime).getTime() >= STUCK_PREVIEW_TIMEOUT_MS;
}

const failedSubmissionCount = computed(() => submissions.value.filter(
    isRecoverablePreview
).length);
const canRetryFailedPreviews = computed(() => Boolean(
    selectedLessonId.value && selectedQuestionId.value && selectedClassCode.value && failedSubmissionCount.value > 0
));

// P4: 获取当前选中班级的学生总人数
const currentClassTotalStudents = computed(() => {
    if (!selectedClassCode.value || !classes.value.length) return 0;
    const classInfo = classes.value.find(c => c.classCode === selectedClassCode.value);
    return classInfo?.totalStudents || 0;
});

// P3: 获取当前选中课程的年级名称
const currentGradeName = computed(() => {
    if (!selectedLessonId.value || !gradeGroups.value.length) return '';
    for (const group of gradeGroups.value) {
        const found = group.lessons?.find(l => l.lessonId === selectedLessonId.value);
        if (found) {
            return group.gradeName;
        }
    }
    return '';
});

const currentEntryYear = computed(() => {
    if (!selectedClassCode.value || !classes.value.length) return '';
    const classInfo = classes.value.find(c => c.classCode === selectedClassCode.value);
    return classInfo?.entryYear || '';
});

function getFileUrl(path) {
    if (!path) return '';
    return import.meta.env.VITE_APP_BASE_API + '/common/download/resource?resource=' + encodeURIComponent(path);
}

async function loadDeadlineStatus() {
    if (!selectedLessonId.value || !selectedClassCode.value) {
        deadlineStatus.value = null;
        return;
    }
    const classInfo = classes.value.find(c => c.classCode === selectedClassCode.value);
    try {
        const res = await getPracticalDeadlineStatus(
            selectedLessonId.value, classInfo?.entryYear || '', selectedClassCode.value);
        deadlineStatus.value = res.data || null;
    } catch (e) {
        deadlineStatus.value = null;
    }
}

// P2: 从文件路径中提取文件名
function getFileName(path) {
    if (!path) return '';
    const parts = path.split('/');
    return parts[parts.length - 1] || path;
}

function getPreviewUrl(relativePath) {
    if (!relativePath) return '';
    // 使用 common/resource/view 接口进行预览
    return import.meta.env.VITE_APP_BASE_API + "/common/resource/view?resource=" + encodeURIComponent(relativePath);
}

function selectStudent(student, index) {
    currentStudent.value = student;
    currentIndex.value = index;
    currentScore.value = student.score != null ? student.score : null; // 默认为空，方便直接输入
    
    // 生成预览URL
    if (student.previewPath && student.previewStatus === 'success') {
        previewUrl.value = getPreviewUrl(student.previewPath);
    } else {
        previewUrl.value = '';
    }
    
    // P6: 加载已保存的分项得分（如果学生已被批改）
    if (student.answerId && student.score != null) {
        loadScoringDetailsForStudent(student.answerId);
    } else {
        // 重置分项得分为0
        scoringItems.value.forEach(item => {
            itemScores.value[item.itemId] = 0;
        });
    }
    
    // 聚焦输入框 (根据评分模式选择对应输入框)
    nextTick(() => {
        setTimeout(() => {
            if (useItemScoring.value && scoringItems.value.length > 0) {
                // 分项评分模式：聚焦第一个评分项输入框
                focusFirstItem();
            } else if (scoreInputRef.value) {
                // 直接打分模式：聚焦总分输入框
                scoreInputRef.value.focus();
                const input = scoreInputRef.value.$el?.querySelector('input');
                if (input) input.select();
            }
        }, 100); // 增加延时确保DOM更新完成
    });
}

// P6: 加载学生已保存的分项得分
function loadScoringDetailsForStudent(answerId) {
    getScoringDetails(answerId).then(res => {
        const details = res.data || [];
        // 先重置为0
        scoringItems.value.forEach(item => {
            itemScores.value[item.itemId] = 0;
        });
        // 填充已保存的分数
        details.forEach(detail => {
            if (itemScores.value.hasOwnProperty(detail.itemId)) {
                itemScores.value[detail.itemId] = detail.score || 0;
            }
        });
    }).catch(() => {
        // 如果加载失败，保持0
    });
}

function submitScore() {
    if (!currentStudent.value) return;
    if (!deadlineStatus.value?.canGrade) {
        ElMessage.error('已逾期，操作题批改已锁定；如需继续批改，请联系教研员调整截止时间');
        return;
    }
    
    // P6: 如果使用分项评分，计算总分
    let finalScore = currentScore.value;
    let scoringDetails = null;
    
    if (useItemScoring.value && scoringItems.value.length > 0) {
        // 使用分项评分
        finalScore = itemTotalScore.value;
        scoringDetails = scoringItems.value.map(item => ({
            itemId: item.itemId,
            score: itemScores.value[item.itemId] || 0
        }));
    }
    
    // P1: 分数校验
    const maxScore = currentStudent.value.maxScore || 0;
    if (finalScore < 0) {
        ElMessage.warning('分数不能为负数');
        return;
    }
    if (finalScore > maxScore) {
        ElMessage.warning(`分数不能超过满分 ${maxScore} 分`);
        return;
    }
    
    // P6: 构造请求数据
    const requestData = {
        answerId: currentStudent.value.answerId,
        score: finalScore,
        scoringDetails: scoringDetails
    };
    
    // 使用fetch发送请求（因为gradeSubmission需要body）
    const token = getToken();
    fetch(import.meta.env.VITE_APP_BASE_API + '/business/teacher/grading/grade', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token
        },
        body: JSON.stringify(requestData)
    }).then(res => res.json()).then(res => {
        if (res.code === 200) {
            ElMessage.success('批改保存成功');
            // 更新本地数据
            const wasUngraded = currentStudent.value.score == null;
            currentStudent.value.score = finalScore;
            const item = submissions.value[currentIndex.value];
            if(item) item.score = finalScore;
            
            // Bug 2: 同步更新班级下拉框的未批改计数
            if (wasUngraded && selectedClassCode.value) {
                const classInfo = classes.value.find(c => c.classCode === selectedClassCode.value);
                if (classInfo && classInfo.practicalUngraded > 0) {
                    classInfo.practicalUngraded--;
                }
            }
            loadDeadlineStatus();
            
            // 自动跳转下一个已提交的学生
            nextSubmittedStudent();
        } else {
            ElMessage.error(res.msg || '批改失败');
        }
    });
}

function getPreviewStatusText(student) {
    if (!student?.submitted) return '';
    if (student.previewStatus === 'success') return '可预览';
    if (student.previewStatus === 'pending') return '待转换';
    if (student.previewStatus === 'converting') return '转换中';
    if (student.previewStatus === 'failed') {
        const retryCount = student.previewRetryCount || 0;
        return retryCount > 0 ? `转换失败，已重试${retryCount}次` : '转换失败';
    }
    return student.previewPath ? '可预览' : '已提交';
}

function getPreviewStatusClass(status) {
    if (status === 'success') return 'is-success';
    if (status === 'pending' || status === 'converting') return 'is-pending';
    if (status === 'failed') return 'is-failed';
    return '';
}

async function handleRetryFailedPreviews() {
    if (!canRetryFailedPreviews.value) return;

    try {
        await ElMessageBox.confirm(
            `当前班级有 ${failedSubmissionCount.value} 份失败或卡住的文件，确定重新转换吗？`,
            '重新转换确认',
            {
                type: 'warning',
                confirmButtonText: '开始重转',
                cancelButtonText: '取消'
            }
        );
    } catch {
        return;
    }

    retryLoading.value = true;
    try {
        const res = await retryFailedPreviews({
            lessonId: selectedLessonId.value,
            questionId: selectedQuestionId.value,
            classCode: selectedClassCode.value,
            entryYear: currentEntryYear.value
        });
        const data = res?.data || {};
        const matchedCount = data.matchedCount || 0;
        const triggeredCount = data.triggeredCount || 0;
        const skippedCount = data.skippedCount || 0;

        if (matchedCount === 0) {
            ElMessage.info('当前班级暂无异常文件需要重转');
        } else {
            ElMessage.success(`已触发 ${triggeredCount} 条重转任务，跳过 ${skippedCount} 条`);
        }
        loadSubmissions();
    } catch (error) {
        ElMessage.error(error?.message || '重新转换失败，请稍后再试');
    } finally {
        retryLoading.value = false;
    }
}

// P6: 跳转到下一个已提交的学生 (P1: 优先跳转未批改)
function nextSubmittedStudent() {
    // 1. 优先寻找尚未批改(分数为空)的已提交学生
    // 从当前位置向后找
    for (let i = currentIndex.value + 1; i < submissions.value.length; i++) {
        if (submissions.value[i].submitted && submissions.value[i].score == null) {
            selectStudent(submissions.value[i], i);
            autoFocusItem();
            return;
        }
    }
    // 从头向当前位置找
    for (let i = 0; i < currentIndex.value; i++) {
        if (submissions.value[i].submitted && submissions.value[i].score == null) {
            selectStudent(submissions.value[i], i);
            autoFocusItem();
            return;
        }
    }
    
    // 2. 如果都批改了，则寻找下一个已提交的学生(无论是否批改)
    for (let i = currentIndex.value + 1; i < submissions.value.length; i++) {
        if (submissions.value[i].submitted) {
            selectStudent(submissions.value[i], i);
            autoFocusItem();
            return;
        }
    }
    
    ElMessage.info('已经是最后一位已提交学生了');
    // 如果是全屏状态，自动退出
    if (isFullscreen.value) {
        toggleFullscreen();
    }
}

function prevStudent() {
    if (currentIndex.value > 0) {
        selectStudent(submissions.value[currentIndex.value - 1], currentIndex.value - 1);
    }
}

function nextStudent() {
    if (currentIndex.value < submissions.value.length - 1) {
        selectStudent(submissions.value[currentIndex.value + 1], currentIndex.value + 1);
    } else {
        ElMessage.info('已经是最后一位了');
    }
}

// 全屏处理
function toggleFullscreen() {
    if (!document.fullscreenElement) {
        gradingPageRef.value.requestFullscreen();
        isFullscreen.value = true;
    } else {
        document.exitFullscreen();
        isFullscreen.value = false;
    }
}

document.addEventListener('fullscreenchange', () => {
    isFullscreen.value = !!document.fullscreenElement;
});

function autoFocusItem() {
    if (useItemScoring.value && scoringItems.value.length > 0) {
        focusFirstItem();
    }
}

</script>

<style lang="scss" scoped>
.grading-page {
  height: calc(100vh - 84px);
  display: flex;
  flex-direction: column;
  background-color: #f0f2f5;
  
  &.is-fullscreen {
     position: fixed;
     top: 0;
     left: 0;
     width: 100vw;
     height: 100vh;
     z-index: 9999;
     background: #fff;
     padding: 20px;
  }
}

.deadline-status-panel {
  margin: 10px 0;
  border-left: 5px solid #409eff;
}

.deadline-status-panel.is-overdue {
  border-left-color: #f56c6c;
  background: #fef0f0;
}

.deadline-status-panel.is-due_soon {
  border-left-color: #e6a23c;
}

.deadline-status-panel.is-reopened {
  border-left-color: #9b59b6;
}

.deadline-panel-head,
.deadline-panel-head > div,
.deadline-panel-grid {
  display: flex;
  align-items: center;
  gap: 12px;
}

.deadline-panel-head {
  justify-content: space-between;
  margin-bottom: 10px;
}

.deadline-panel-grid {
  flex-wrap: wrap;
  color: #606266;
  font-size: 13px;
  margin: 10px 0;
}

.grading-header {
  background: #fff;
  padding: 15px 20px;
  border-radius: 4px;
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  
  .filter-label {
    font-weight: bold;
    color: #606266;
    margin-right: 8px;
  }
  
  .stats {
    display: inline-block;
    margin-right: 20px;
    color: #909399;
    .highlight {
      color: #67c23a;
      font-weight: bold;
      font-size: 18px;
    }
  }
}

.grading-main {
  flex: 1;
  display: flex;
  gap: 10px;
  overflow: hidden;
}

.student-list-panel {
  width: 250px;
  background: #fff;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  
  .panel-title {
    padding: 15px;
    border-bottom: 1px solid #EBEEF5;
    font-weight: bold;
    background: #FAFAFA;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .grading-stats {
       font-size: 13px;
       font-weight: normal;
       color: #909399;
       
       b {
          color: #67c23a;
          font-weight: bold;
       }
    }
  }
  
  .student-list-scroll {
    flex: 1;
    overflow-y: auto;
  }
  
  .student-item {
    padding: 12px 15px;
    border-bottom: 1px solid #f5f7fa;
    cursor: pointer;
    display: flex;
    justify-content: space-between;
    align-items: center;
    transition: all 0.2s;
    
    &:hover {
      background: #f5f7fa;
    }
    
    &.active {
      background: #ecf5ff;
      border-left: 3px solid #409EFF;
      
      .s-name { color: #409EFF; font-weight: bold; }
    }
    
    .s-info {
       .s-name { font-size: 14px; color: #303133; }
       .s-remark { font-size: 11px; color: #E6A23C; margin-top: 2px; }
       .s-no { font-size: 12px; color: #909399; }
       .s-preview-status {
          font-size: 11px;
          margin-top: 4px;
          color: #909399;

          &.is-success {
             color: #67c23a;
          }

          &.is-pending {
             color: #409eff;
          }

          &.is-failed {
             color: #e6a23c;
          }
       }
    }
    
    .s-status {
       font-size: 14px;
       font-weight: bold;
       color: #67c23a;
       &.ungrad {
          color: #909399;
          font-weight: normal;
          font-size: 12px;
       }
    }
    
    // P5: 未提交学生灰显样式
    &.not-submitted {
       background: #f9f9fa;
       cursor: not-allowed;
       opacity: 0.7;
       
       &:hover {
          background: #f9f9fa;
       }
       
       .s-name { color: #909399 !important; }
       .s-status { color: #c0c4cc; font-weight: normal; }
    }
  }
}

.preview-panel {
  flex: 1;
  background: #fff;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  
  .preview-content {
     height: 100%;
     display: flex;
     flex-direction: column;
  }
  
  .preview-header {
     padding: 10px 15px;
     background: #FAFAFA;
     border-bottom: 1px solid #EBEEF5;
     display: flex;
     justify-content: space-between;
     align-items: center;
     
     .header-info {
        display: flex;
        flex-direction: column;
        gap: 4px;
     }
     .student-label {
        font-weight: bold;
        font-size: 14px;
        color: #303133;
     }
     .file-name {
        font-size: 12px;
        color: #909399;
        font-weight: normal;
     }
     
     .download-link {
        color: #409EFF;
        text-decoration: none;
        font-size: 13px;
        &:hover { text-decoration: underline; }
     }
  }
  
  .pdf-frame {
     flex: 1;
     width: 100%;
     height: 0; /* flex grow will handle height */
  }
}

.scoring-panel {
  width: 300px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  padding: 20px;
  
  .card-title {
    font-size: 18px;
    font-weight: bold;
    margin-bottom: 20px;
    padding-left: 10px;
    border-left: 4px solid #409EFF;
  }
  
  .question-info {
     margin-bottom: 30px;
     background: #f4f4f5;
     padding: 15px;
     border-radius: 4px;
     
     .q-score {
        font-size: 16px;
        color: #606266;
     }
  }
  
  .score-input-area {
     margin-bottom: 30px;
     text-align: center;
     
     .input-label {
        font-size: 16px;
        margin-bottom: 10px;
        color: #303133;
     }
  }
  
  .submit-btn {
     width: 100%;
     margin-bottom: 20px;
  }
  
  .nav-actions {
     display: flex;
     justify-content: space-between;
     button { flex: 1; margin: 0 5px; }
  }
  
  // P6: 评分模式切换
  .scoring-mode-switch {
     margin-bottom: 15px;
     text-align: center;
  }
  
  // P6: 分项评分区域
  .item-scoring-area {
     margin-bottom: 20px;
     
     .item-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 8px 0;
        border-bottom: 1px dashed #ebeef5;
        
        .item-name {
           font-size: 14px;
           color: #303133;
        }
        .item-input {
           display: flex;
           align-items: center;
           gap: 5px;
           
           .item-max {
              font-size: 12px;
              color: #909399;
           }
        }
     }
     
     .item-total {
        margin-top: 15px;
        text-align: right;
        font-size: 16px;
        color: #409EFF;
        
        strong {
           font-size: 22px;
           font-weight: bold;
        }
     }
  }
  
  // 班级选项样式
  .class-option {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    
    .ungraded-badge {
      background: #F56C6C;
      color: #fff;
      padding: 2px 6px;
      border-radius: 10px;
      font-size: 11px;
      margin-left: 8px;
    }
    
    .graded-badge {
      color: #67C23A;
      font-weight: bold;
      margin-left: 8px;
    }
    
    .no-submit-badge {
      color: #909399;
      font-size: 12px;
      margin-left: 8px;
    }
  }
}
</style>
