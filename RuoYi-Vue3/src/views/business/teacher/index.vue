<template>
  <div class="app-container teacher-dashboard">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>课程设置</span>
          <!-- 顶部批量入口保留，但在卡片中提供了更便捷的操作 -->
        </div>
      </template>
      <div v-if="loading" class="loading-state">正在加载教学数据...</div>
      <div v-if="!loading && gradeGroups.length === 0" class="empty-state">
        您还没有导入任何学生，无法进行课程设置。请先前往【学生管理】导入学生。
      </div>
      <div v-for="group in gradeGroups" :key="group.entryYear" class="grade-group">
        <div class="grade-header">
          <span class="grade-title">{{ group.entryYear }}级 ({{ group.gradeName }})</span>
        </div>
        <div class="lesson-container">
          <!-- 课程卡片列表 -->
          <div
            v-for="lesson in group.lessons"
            :key="lesson.lessonId"
            class="lesson-folder"
          >
            <!-- 删除按钮 -->
            <div class="folder-delete" @click.stop="handleDeleteLesson(lesson.lessonId)">
              <el-icon><Close /></el-icon>
            </div>
            
            <!-- 左侧：课程核心信息 -->
            <div class="folder-content">
              <!-- 竖排课程标题 -->
              <div class="folder-title-vertical" :title="lesson.lessonTitle">
                {{ lesson.lessonTitle }}
              </div>
              
              <!-- 右侧内容区 -->
              <div class="folder-info">
                <!-- 已指派班级竖向排列 -->
                <div v-if="lesson.assignedClasses?.length" class="assigned-classes">
                  <span v-for="cls in lesson.assignedClasses" :key="cls" class="assigned-tag">{{ cls }}</span>
                </div>
                <div class="lesson-count-tag">
                  第{{ lesson.lessonNum }}课
                </div>
              </div>
            </div>
            
            <!-- 右侧：竖排操作栏 (25%) -->
            <div class="folder-actions">
               <div class="action-btn design" @click.stop="handleEditLesson(lesson.lessonId)" title="设计课程">
                  <el-icon><Edit /></el-icon>
                  <span>设计</span>
               </div>
               <!-- 只有包含操作题时才显示批改 -->
               <div 
                 v-if="lesson.hasPractical" 
                 class="action-btn grade" 
                 @click.stop="goToGrading(lesson, group)"
                 title="批改作业"
               >
                  <el-icon><Check /></el-icon>
                  <span>批改</span>
               </div>
               <div class="action-btn score" @click.stop="goToScoreAnalysis(lesson, group)" title="查看成绩">
                  <el-icon><DataLine /></el-icon>
                  <span>成绩</span>
               </div>
            </div>
          </div>
          
          <!-- 新增课程按钮 -->
          <div class="add-lesson-btn" @click="handleAddNewLesson(group)">
            <el-icon class="add-icon"><Plus /></el-icon>
            <div class="add-text">添加课程</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 班级选择弹窗 -->
    <ClassSelectionDialog ref="classDialogRef" />
  </div>
</template>

<script setup name="TeacherDashboard">
import { ref, onMounted, onActivated } from 'vue';
import { useRouter } from 'vue-router';
import { getDashboardData } from '@/api/business/teacher';
import { delLesson } from '@/api/business/lesson';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Close, Edit, Check, DataLine } from '@element-plus/icons-vue';
import ClassSelectionDialog from './components/ClassSelectionDialog.vue';

const router = useRouter();
const loading = ref(true);
const gradeGroups = ref([]);
const classDialogRef = ref(null);

/** 获取首页数据 */
function fetchDashboardData() {
  loading.value = true;
  getDashboardData()
    .then(response => {
      gradeGroups.value = response.data;
      loading.value = false;
    })
    .catch(() => {
      loading.value = false;
    });
}

/** 处理新增课程 */
function handleAddNewLesson(group) {
  router.push({
    path: '/business/lesson-auth/designer',
    query: {
      grade: group.gradeId,
      gradeName: group.gradeName,
      classes: JSON.stringify(group.allClassesInGrade)
    }
  });
}

/** 处理修改课程 (设计) */
function handleEditLesson(lessonId) {
  router.push(`/business/lesson-auth/designer/${lessonId}`);
}

/** 跳转批改 */
async function goToGrading(lesson, group) {
  // 1. 选择班级
  const selectedClass = await classDialogRef.value.open(group.allClassesInGrade);
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
  const selectedClass = await classDialogRef.value.open(group.allClassesInGrade);
  if (!selectedClass) return; // 用户取消

  // 2. 跳转
  router.push({
    path: '/business/score',
    query: {
      lessonId: lesson.lessonId,
      entryYear: group.entryYear,
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

onMounted(() => {
  fetchDashboardData();
});

// 从其他页面返回时（如课程设计页），重新加载数据
onActivated(() => {
  fetchDashboardData();
});
</script>

<style scoped lang="scss">
.teacher-dashboard {
  .box-card {
    min-height: calc(100vh - 84px);
  }
}

.lesson-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: stretch;
}

.lesson-folder {
  position: relative;
  width: 200px; /* 进一步减小宽度 (原220px) */
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

.lesson-count-tag {
  font-size: 12px; 
  color: #909399;
  text-align: right; /* 页码右对齐好看些 */
  margin-top: 4px;
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
