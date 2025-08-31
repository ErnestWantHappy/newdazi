<template>
  <div class="app-container teacher-dashboard">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>课程设置</span>
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
          <div v-for="lesson in group.lessons" :key="lesson.lessonId" class="lesson-folder">
            <el-dropdown trigger="contextmenu" style="width:100%; height:100%;">
              <div class="folder-content" @click="handleEditLesson(lesson.lessonId)">
                <div class="folder-icon">
                  <!-- 核心修复：使用更美观的SVG图标 -->
                  <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="64" height="64"><path fill="#FFCA28" d="M885.333333 256H512l-85.333333-85.333333H138.666667c-35.2 0-64 28.8-64 64v512c0 35.2 28.8 64 64 64h746.666666c35.2 0 64-28.8 64-64V320c0-35.2-28.8-64-64-64z"></path><path fill="#FFB300" d="M949.333333 320H426.666667l-85.333334-85.333333H138.666667a64 64 0 0 0-64 64v85.333333h874.666666V320z"></path></svg>
                </div>
                <div class="folder-name" :title="lesson.lessonTitle">{{ lesson.lessonTitle }}</div>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleEditLesson(lesson.lessonId)">修改</el-dropdown-item>
                  <el-dropdown-item @click="handleDeleteLesson(lesson.lessonId)" divided style="color: #F56C6C;">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <!-- 新增课程按钮 -->
          <div class="add-lesson-btn" @click="handleAddNewLesson(group)">
            <el-icon><Plus /></el-icon>
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
import { delLesson } from '@/api/business/lesson'; // 导入删除API
import { ElMessage, ElMessageBox } from 'element-plus';

const { proxy } = getCurrentInstance();
const router = useRouter();
const loading = ref(true);
const gradeGroups = ref([]);

/** 获取首页数据 */
function fetchDashboardData() {
  loading.value = true;
  getDashboardData().then(response => {
    gradeGroups.value = response.data;
    loading.value = false;
  }).catch(() => {
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

/** 处理修改课程 */
function handleEditLesson(lessonId) {
  router.push(`/business/lesson-auth/designer/${lessonId}`);
}

/** 核心新增：处理删除课程 */
function handleDeleteLesson(lessonId) {
  ElMessageBox.confirm(
    '是否确认删除该课程？此操作将同时删除所有关联的题目和班级指派，且不可恢复。',
    '警告',
    {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    delLesson(lessonId).then(() => {
      ElMessage({
        type: 'success',
        message: '删除成功',
      });
      fetchDashboardData(); // 重新加载数据
    });
  }).catch(() => {
    // 用户取消操作
  });
}

onMounted(() => {
  fetchDashboardData();
});
</script>

<style scoped>
/* ... (大部分样式保持不变) ... */
.lesson-folder {
  width: 120px; /* 稍微加宽以容纳更多文字和菜单 */
  height: 110px;
  padding: 10px;
  box-sizing: border-box;
  border: 1px solid transparent; /* 默认无边框 */
}
.lesson-folder:hover {
  background-color: #f5f7fa;
  border-color: #e0e0e0;
}
.folder-content {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.folder-icon {
  width: 64px;
  height: 64px;
}
.folder-name {
  margin-top: 8px;
  font-size: 14px;
  text-align: center;
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding: 0 5px;
}
.add-lesson-btn {
  width: 120px;
  height: 110px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s;
  border: 2px dashed #dcdfe6;
  color: #c0c4cc;
}
</style>
