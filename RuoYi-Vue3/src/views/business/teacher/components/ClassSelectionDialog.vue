<template>
  <el-dialog 
    :title="dialogTitle" 
    v-model="visible" 
    width="900px"
    append-to-body 
    align-center
    @closed="handleClose"
  >
    <div class="class-list" v-loading="loading">
      <div 
        v-for="cls in classes" 
        :key="cls.classCode" 
        class="class-item"
        @click="handleSelect(cls.classCode)"
      >
        <el-button 
          type="primary" 
          plain 
          class="class-btn"
          :class="getBtnClass(cls)"
        >
          <span class="class-name">{{ cls.classCode }}班</span>
          <template v-if="mode === 'score'">
            <span v-if="statsLoading && !cls.hasData" class="badge-loading">统计中...</span>
            <span v-else-if="cls.scoreReadyCount > 0" class="badge-graded">{{ cls.scoreReadyCount }}/{{ cls.totalStudents || 0 }}有成绩</span>
            <span v-else class="badge-none">暂无成绩</span>
          </template>
          <template v-else>
            <span v-if="cls.hasData" class="grading-status-text" :class="gradingStatusClass(cls)">
              {{ gradingStatusText(cls) }}
            </span>
          </template>
        </el-button>
      </div>
    </div>
    <div v-if="!loading && classes.length === 0" class="empty-tip">
      暂无关联班级
    </div>
    <div v-if="mode === 'score' && statsLoading && classes.length > 0" class="stats-tip">
      正在加载成绩统计，可先选择班级查看成绩
    </div>
    <div v-if="mode === 'score' && statsError" class="stats-tip error">
      成绩统计暂时加载失败，可直接选择班级查看成绩
    </div>
  </el-dialog>
</template>

<script setup>
import { computed, ref } from 'vue';
import { getClassesByLesson } from '@/api/business/teacherGrading';

const visible = ref(false);
const loading = ref(false);
const statsLoading = ref(false);
const statsError = ref(false);
const classes = ref([]);
const resolvePromise = ref(null);
const mode = ref('grading');
const dialogTitle = computed(() => mode.value === 'score' ? '请选择班级查看成绩' : '请选择班级');
let requestSeq = 0;

/**
 * 打开选择框
 * @param {Array} simpleClassList 简单的班级列表字符串数组（旧模式）
 * @param {Number} lessonId 课程ID（新模式，如有则自动加载详细统计）
 * @returns {Promise<String|null>} 返回选中的班级名称，未选中返回null
 */
function open(simpleClassList, lessonId = null, openMode = 'grading') {
  visible.value = true;
  loading.value = false;
  statsLoading.value = false;
  statsError.value = false;
  classes.value = [];
  mode.value = openMode;
  const requestId = ++requestSeq;
  const simpleClasses = buildClassItems(simpleClassList || [], false);
  const shouldShowSimpleFirst = openMode === 'score' && simpleClasses.length > 0;

  if (shouldShowSimpleFirst) {
    classes.value = simpleClasses;
  }
  
  if (lessonId) {
    // 成绩入口先展示班级，统计数据回来后再补状态。
    if (shouldShowSimpleFirst) {
      statsLoading.value = true;
    } else {
      loading.value = true;
    }
    getClassesByLesson(lessonId)
      .then(res => {
        if (requestId !== requestSeq) return;
        const detailClasses = buildClassItems(res.data || [], true);
        if (shouldShowSimpleFirst) {
          mergeClassStats(detailClasses);
        } else {
          classes.value = detailClasses;
        }
      })
      .catch(() => {
        if (requestId !== requestSeq) return;
        statsError.value = shouldShowSimpleFirst;
      })
      .finally(() => {
        if (requestId !== requestSeq) return;
        loading.value = false;
        statsLoading.value = false;
        if (shouldShowSimpleFirst) {
          classes.value = classes.value.map(cls => ({ ...cls, hasData: true }));
        }
      });
  } else if (simpleClassList) {
    // 旧模式：使用传入的字符串列表
    classes.value = simpleClasses;
  }

  return new Promise((resolve) => {
    resolvePromise.value = resolve;
  });
}

function buildClassItems(list, hasData) {
  if (!Array.isArray(list)) return [];
  return list
    .map(item => {
      const classCode = normalizeClassCode(item);
      if (!classCode) return null;
      return {
        classCode,
        entryYear: item?.entryYear || item?.entry_year || null,
        practicalSubmitted: item?.practicalSubmitted || item?.practicalsubmitted || 0,
        practicalUngraded: item?.practicalUngraded || item?.practicalungraded || 0,
        deadlineStatusCode: item?.deadlineStatusCode || item?.deadlinestatuscode || '',
        currentDeadlineTime: item?.currentDeadlineTime || item?.currentdeadlinetime || null,
        serverNow: item?.serverNow || item?.servernow || null,
        canGrade: item?.canGrade ?? item?.cangrade ?? true,
        scoreReadyCount: item?.scoreReadyCount || item?.scorereadycount || 0,
        totalStudents: item?.totalStudents || item?.totalstudents || 0,
        hasData
      };
    })
    .filter(Boolean)
    .sort((a, b) => sortClassCode(a.classCode, b.classCode));
}

function normalizeClassCode(item) {
  if (item == null) return '';
  const raw = typeof item === 'object' ? (item.classCode || item.class_code || item.label || item.value) : item;
  return String(raw ?? '').replace(/[^\d]/g, '');
}

function mergeClassStats(detailClasses) {
  detailClasses.forEach(detail => {
    const existing = classes.value.find(cls =>
      cls.classCode === detail.classCode && (!cls.entryYear || !detail.entryYear || cls.entryYear === detail.entryYear)
    );
    if (existing) {
      Object.assign(existing, detail, { hasData: true });
    } else {
      classes.value.push(detail);
    }
  });
  classes.value = [...classes.value].sort((a, b) => sortClassCode(a.classCode, b.classCode));
}

function sortClassCode(a, b) {
  const numA = parseInt(a) || 0;
  const numB = parseInt(b) || 0;
  return numA - numB;
}

function handleSelect(classCode) {
  if (resolvePromise.value) {
    resolvePromise.value(classCode);
    resolvePromise.value = null;
  }
  visible.value = false;
}

function handleClose() {
  if (resolvePromise.value) {
    resolvePromise.value(null);
    resolvePromise.value = null;
  }
}

function getBtnClass(cls) {
  if (!cls.hasData) return '';
  if (mode.value === 'score') {
    if (cls.scoreReadyCount > 0 && cls.scoreReadyCount >= (cls.totalStudents || 0)) return 'btn-all-done';
    if (cls.scoreReadyCount > 0) return 'btn-action-needed';
    return 'btn-no-data';
  }
  if (cls.practicalUngraded > 0) return 'btn-action-needed';
  if (cls.practicalSubmitted > 0) return 'btn-all-done';
  return 'btn-no-data';
}

function gradingStatusText(cls) {
  const submitted = Number(cls.practicalSubmitted || 0);
  const ungraded = Number(cls.practicalUngraded || 0);
  if (submitted === 0) return '暂无提交';
  if (ungraded === 0) return '已批改';
  if (cls.deadlineStatusCode === 'OVERDUE' || cls.canGrade === false) {
    return `已逾期 · ${ungraded}人未批`;
  }
  if (cls.deadlineStatusCode === 'NOT_TRIGGERED' || !cls.currentDeadlineTime) {
    return `未达到期限起算条件 · ${ungraded}人未批`;
  }
  const due = new Date(cls.currentDeadlineTime);
  const now = cls.serverNow ? new Date(cls.serverNow) : new Date();
  const remainingDays = Math.ceil((due.getTime() - now.getTime()) / 86400000);
  const remainingText = remainingDays > 0 ? `还剩${remainingDays}天` : '不足1天';
  return `截止 ${formatDate(due)} · ${remainingText} · ${ungraded}人未批`;
}

function gradingStatusClass(cls) {
  if (Number(cls.practicalSubmitted || 0) === 0) return 'is-empty';
  if (Number(cls.practicalUngraded || 0) === 0) return 'is-completed';
  if (cls.deadlineStatusCode === 'OVERDUE' || cls.canGrade === false) return 'is-overdue';
  return 'is-waiting';
}

function formatDate(date) {
  if (Number.isNaN(date.getTime())) return '日期待确认';
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`;
}

defineExpose({
  open
});
</script>

<style scoped lang="scss">
.class-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 15px;
  padding: 10px;
  min-height: 360px;
  max-height: 56vh;
  overflow-y: auto;
}

.class-item {
  min-width: 0;
}

.class-btn {
  width: 100%;
  margin: 0 !important;
  min-height: 76px;
  display: flex !important;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  position: relative;
  
  .class-name {
    font-size: 16px;
    font-weight: bold;
  }
  
  .grading-status-text {
    margin-left: 16px;
    font-size: 12px;
    text-align: right;
    white-space: normal;

    &.is-completed { color: #67c23a; font-weight: 600; }
    &.is-overdue { color: #f56c6c; font-weight: 600; }
    &.is-waiting { color: #e6a23c; }
    &.is-empty { color: #909399; }
  }

  .badge-loading {
    color: #409EFF;
    font-size: 12px;
  }
}

// 按钮状态背景色
.btn-action-needed {
  :deep(span) { color: #303133; }
  background-color: #fef0f0 !important;
  border-color: #fab6b6 !important;
  
  &:hover {
    background-color: #fde2e2 !important;
  }
}

.btn-all-done {
  background-color: #f0f9eb !important;
  border-color: #e1f3d8 !important;
  
  &:hover {
     background-color: #e1f3d8 !important;
  }
}

.btn-no-data {
  background-color: #f4f4f5 !important;
  border-color: #e9e9eb !important;
  color: #909399 !important;
}

.empty-tip {
  text-align: center;
  color: #909399;
  padding: 20px;
}

.stats-tip {
  padding: 0 10px 8px;
  color: #909399;
  font-size: 12px;
  line-height: 18px;

  &.error {
    color: #E6A23C;
  }
}
</style>
