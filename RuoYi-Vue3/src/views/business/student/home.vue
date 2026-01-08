<template>
  <div class="app-container student-home">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>当前课程</span>
        </div>
      </template>

      <!-- 加载中 -->
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        正在加载课程...
      </div>

      <!-- 无课程 -->
      <div v-else-if="!hasLesson" class="empty-state">
        <el-empty description="暂无课程，请等待教师开启课程" />
      </div>

      <!-- 有课程 -->
      <div v-else class="lesson-content">
        <h2 class="lesson-title">{{ lessonTitle }}</h2>
        
        <div class="question-list">
          <div 
            v-for="(question, index) in questions" 
            :key="question.questionId" 
            class="question-item"
          >
            <div class="question-header">
              <span class="question-num">第 {{ index + 1 }} 题</span>
              <span class="question-type">{{ getQuestionTypeLabel(question.questionType) }}</span>
              <span class="question-score">( {{ question.questionScore }} 分 )</span>
            </div>
            
            <div class="question-content">{{ question.questionContent }}</div>
            
            <!-- 选择题选项 -->
            <div v-if="question.questionType === 'choice'" class="options-list">
              <div class="option-item">A. {{ question.optionA || '未配置' }}</div>
              <div class="option-item">B. {{ question.optionB || '未配置' }}</div>
              <div class="option-item">C. {{ question.optionC || '未配置' }}</div>
              <div class="option-item">D. {{ question.optionD || '未配置' }}</div>
            </div>
            
            <!-- 判断题答案提示 -->
            <div v-if="question.questionType === 'judgment'" class="judge-hint">
              请判断上述陈述是否正确
            </div>
            
            <!-- 打字题信息 -->
            <div v-if="question.questionType === 'typing'" class="typing-info">
              <span>时长: {{ question.typingDuration || 0 }} 分钟</span>
              <span>字数: {{ question.wordCount || 0 }} 字</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup name="StudentHome">
import { ref, onMounted } from 'vue';
import { getCurrentLesson } from '@/api/business/studentHome';
import { Loading } from '@element-plus/icons-vue';

const loading = ref(true);
const hasLesson = ref(false);
const lessonTitle = ref('');
const questions = ref([]);

function getQuestionTypeLabel(type) {
  const typeMap = {
    'choice': '选择题',
    'judgment': '判断题',
    'typing': '打字题',
    'practical': '实操题'
  };
  return typeMap[type] || type;
}

function fetchCurrentLesson() {
  loading.value = true;
  getCurrentLesson().then(response => {
    hasLesson.value = response.hasLesson || false;
    lessonTitle.value = response.lessonTitle || '';
    questions.value = response.questions || [];
    loading.value = false;
  }).catch(() => {
    loading.value = false;
  });
}

onMounted(() => {
  fetchCurrentLesson();
});
</script>

<style scoped>
.student-home {
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: #909399;
  font-size: 16px;
}

.loading-state .el-icon {
  margin-right: 8px;
  font-size: 20px;
}

.empty-state {
  padding: 40px 0;
}

.lesson-title {
  font-size: 24px;
  color: #303133;
  margin-bottom: 24px;
  text-align: center;
  border-bottom: 2px solid #409eff;
  padding-bottom: 12px;
}

.question-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.question-item {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px 20px;
  border-left: 4px solid #409eff;
}

.question-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.question-num {
  font-weight: bold;
  color: #409eff;
}

.question-type {
  background: #e6f7ff;
  color: #1890ff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.question-score {
  color: #67c23a;
  font-size: 14px;
}

.question-content {
  font-size: 16px;
  color: #303133;
  line-height: 1.6;
  margin-bottom: 12px;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-left: 16px;
}

.option-item {
  font-size: 14px;
  color: #606266;
  padding: 8px 12px;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.option-item:hover {
  background: #ecf5ff;
  color: #409eff;
}

.judge-hint {
  font-size: 14px;
  color: #909399;
  font-style: italic;
}

.typing-info {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #909399;
}
</style>
