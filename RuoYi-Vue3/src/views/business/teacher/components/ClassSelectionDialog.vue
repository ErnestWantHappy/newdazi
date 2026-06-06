<template>
  <el-dialog 
    :title="dialogTitle" 
    v-model="visible" 
    width="500px" 
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
            <span v-if="cls.scoreReadyCount > 0" class="badge-graded">{{ cls.scoreReadyCount }}/{{ cls.totalStudents || 0 }}有成绩</span>
            <span v-else class="badge-none">暂无成绩</span>
          </template>
          <template v-else>
            <span v-if="cls.practicalUngraded > 0" class="badge-ungraded">{{ cls.practicalUngraded }}人未批</span>
            <span v-else-if="cls.practicalSubmitted > 0" class="badge-graded">已批改</span>
            <span v-else-if="cls.hasData" class="badge-none">暂无提交</span>
          </template>
        </el-button>
      </div>
    </div>
    <div v-if="!loading && classes.length === 0" class="empty-tip">
      暂无关联班级
    </div>
  </el-dialog>
</template>

<script setup>
import { computed, ref } from 'vue';
import { getClassesByLesson } from '@/api/business/teacherGrading';

const visible = ref(false);
const loading = ref(false);
const classes = ref([]);
const resolvePromise = ref(null);
const mode = ref('grading');
const dialogTitle = computed(() => mode.value === 'score' ? '请选择班级查看成绩' : '请选择班级');

/**
 * 打开选择框
 * @param {Array} simpleClassList 简单的班级列表字符串数组（旧模式）
 * @param {Number} lessonId 课程ID（新模式，如有则自动加载详细统计）
 * @returns {Promise<String|null>} 返回选中的班级名称，未选中返回null
 */
function open(simpleClassList, lessonId = null, openMode = 'grading') {
  visible.value = true;
  loading.value = false;
  classes.value = [];
  mode.value = openMode;
  
  if (lessonId) {
    // 新模式：加载带统计的班级列表
    loading.value = true;
    getClassesByLesson(lessonId).then(res => {
      // 转换后端数据结构
      classes.value = (res.data || []).map(item => ({
        classCode: item.classCode,
        practicalSubmitted: item.practicalSubmitted || item.practicalsubmitted || 0, // 兼容大小写
        practicalUngraded: item.practicalUngraded || item.practicalungraded || 0, // 兼容大小写
        scoreReadyCount: item.scoreReadyCount || item.scorereadycount || 0,
        totalStudents: item.totalStudents || item.totalstudents || 0,
        hasData: true // 标记为从后端获取的详细数据
      })).sort((a, b) => sortClassCode(a.classCode, b.classCode));
    }).finally(() => {
      loading.value = false;
    });
  } else if (simpleClassList) {
    // 旧模式：使用传入的字符串列表
    classes.value = simpleClassList.map(c => ({
      classCode: c.replace(/[^\d]/g, ''),
      practicalSubmitted: 0,
      practicalUngraded: 0,
      scoreReadyCount: 0,
      totalStudents: 0,
      hasData: false
    })).sort((a, b) => sortClassCode(a.classCode, b.classCode));
  }

  return new Promise((resolve) => {
    resolvePromise.value = resolve;
  });
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

defineExpose({
  open
});
</script>

<style scoped lang="scss">
.class-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr); // 改为每行2个，留更多空间显示状态
  gap: 15px;
  padding: 10px;
  max-height: 400px;
  overflow-y: auto;
}

.class-btn {
  width: 100%;
  margin: 0 !important;
  height: 50px; // 增加高度
  display: flex !important;
  justify-content: space-between;
  align-items: center;
  padding: 0 15px;
  position: relative;
  
  .class-name {
    font-size: 16px;
    font-weight: bold;
  }
  
  // 状态徽标样式
  .badge-ungraded {
    background-color: #F56C6C;
    color: white;
    font-size: 12px;
    padding: 2px 8px;
    border-radius: 10px;
  }
  
  .badge-graded {
    color: #67C23A;
    font-weight: bold;
    font-size: 12px;
  }
  
  .badge-none {
    color: #909399;
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
</style>
