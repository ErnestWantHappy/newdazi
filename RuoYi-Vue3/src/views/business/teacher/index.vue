<template>
  <div class="app-container teacher-dashboard">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>课程设置</span>
          <el-button type="primary" plain @click="goToGrading" style="margin-left: 20px">批改作业</el-button>
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
          <!-- 课程文件夹列表 -->
          <div
            v-for="lesson in group.lessons"
            :key="lesson.lessonId"
            class="lesson-folder"
          >
            <!-- 新增：右上角显示删除按钮，左键点击即可触发删除逻辑 -->
            <div class="folder-delete" @click.stop="handleDeleteLesson(lesson.lessonId)">
              <el-icon><Close /></el-icon>
            </div>
            
            <!-- 上半部分：课程信息 -->
            <div class="folder-content" @click="handleEditLesson(lesson.lessonId)">
              <div class="card-decoration"></div>
              <div class="folder-name" :title="lesson.lessonTitle">
                {{ lesson.lessonTitle }}
                <span v-if="lesson.courseType === 'shared'" class="shared-tag">共享</span>
              </div>
              <div class="lesson-meta">
                  <span class="meta-tag">{{ group.gradeName }}</span>
              </div>
            </div>
            
            <!-- 下半部分：操作栏 -->
            <div class="folder-actions">
               <div class="action-btn design" @click.stop="handleEditLesson(lesson.lessonId)">
                  <el-icon><Edit /></el-icon> 设计
               </div>
               <div class="action-btn grade" @click.stop="goToGrading(lesson.lessonId)">
                  <el-icon><Check /></el-icon> 批改
               </div>
               <div class="action-btn score" @click.stop="goToScoreAnalysis(lesson.lessonId)">
                  <el-icon><DataLine /></el-icon> 成绩
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
  </div>
</template>

<script setup name="TeacherDashboard">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getDashboardData } from '@/api/business/teacher';
import { delLesson } from '@/api/business/lesson'; // 删除课程接口
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Close, Edit, Check, DataLine } from '@element-plus/icons-vue'; // 引入加号与关闭图标

const router = useRouter();
const loading = ref(true);
const gradeGroups = ref([]);

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

/** 跳转批改 (修复：携带课程ID) */
function goToGrading(lessonId) {
  let query = {};
  if (typeof lessonId === 'number' || typeof lessonId === 'string') {
     query.lessonId = lessonId;
  }
  router.push({
      path: '/business/teacher/grading',
      query: query
  });
}

/** 跳转成绩分析 (占位) */
function goToScoreAnalysis(lessonId) {
  ElMessage.info('成绩查询与导出功能即将上线');
}

/** 核心新增：左键触发删除课程并弹出确认框 */
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
        fetchDashboardData(); // 左键删除后刷新教师首页数据
      });
    })
    .catch(() => {
      // 用户取消操作
    });
}

onMounted(() => {
  fetchDashboardData();
});
</script>

<style scoped lang="scss">
/* 调整布局：课程卡片横向排列且自动换行 */
.lesson-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: stretch;
}
.lesson-folder {
  position: relative;
  width: 240px; 
  height: 140px; 
  box-sizing: border-box;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 2px 6px rgba(0,0,0,0.04);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  overflow: hidden;
  border: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
}
.lesson-folder:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.08);
  border-color: #c6e2ff;
}

/* 上半部分内容 */
.folder-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-start; /* 左对齐 */
  justify-content: center;
  cursor: pointer;
  padding: 15px 20px;
  background: #fff;
  position: relative;
}

/* 左侧装饰条 */
.card-decoration {
    position: absolute;
    left: 0;
    top: 15px;
    bottom: 15px;
    width: 4px;
    background: #409EFF;
    border-radius: 0 4px 4px 0;
}

.folder-delete {
  position: absolute;
  top: 8px;
  right: 8px;
  opacity: 0; /* 默认隐藏 */
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
    opacity: 1; /* 悬停显示 */
}
.folder-delete:hover {
  background-color: #fef0f0;
  color: #f56c6c;
}

.folder-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  width: 100%;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding-right: 15px; /* check overlap */
}

.lesson-meta {
    display: flex;
    gap: 8px;
}
.meta-tag {
    font-size: 12px;
    color: #909399;
    background: #f4f4f5;
    padding: 2px 6px;
    border-radius: 4px;
}

/* 底部操作栏 */
.folder-actions {
  height: 36px;
  border-top: 1px solid #f0f2f5;
  display: flex;
  background-color: #fbfbfb;
}
.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s;
  gap: 4px;
  
  &:hover {
     background-color: #fff;
     font-weight: 600;
  }
  
  &.design:hover { color: #409EFF; background-color: #ecf5ff; }
  &.grade:hover { color: #67C23A; background-color: #f0f9eb; }
  &.score:hover { color: #E6A23C; background-color: #fdf6ec; }
  
  &:not(:last-child) {
     border-right: 1px solid #f0f2f5;
  }
}

.add-lesson-btn {
  width: 240px;
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
    font-size: 24px;
    margin-bottom: 8px;
}
.add-text {
    font-size: 14px;
}
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
  transform: translateY(-1px);
}
</style>
