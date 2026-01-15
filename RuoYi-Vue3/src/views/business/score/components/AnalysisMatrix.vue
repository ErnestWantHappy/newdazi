<template>
  <el-card class="analysis-matrix-card" shadow="hover">
    <template #header>
      <div class="card-header">
         <span style="font-weight: bold; font-size: 16px;">📋 学生理论测试详情</span>
      </div>
    </template>
    <el-table 
      :data="matrixDataWithAccuracy" 
      border 
      stripe 
      height="500" 
      v-loading="loading"
    >
      <!-- 班级列：直接显示 formattedClassName -->
      <el-table-column prop="formattedClassName" label="班级" width="100" fixed sortable :sort-method="(a, b) => Number(a.className) - Number(b.className)" />
      
      <el-table-column prop="studentNo" label="学号" width="100" fixed sortable :sort-method="(a, b) => Number(a.studentNo) - Number(b.studentNo)" />
      <el-table-column prop="studentName" label="姓名" width="100" fixed />
      
      <!-- 新增：正确率列 -->
      <el-table-column label="正确率" width="100" fixed align="center" sortable :sort-method="(a, b) => a.accuracy - b.accuracy">
          <template #default="scope">
              <span :class="getAccuracyClass(scope.row.accuracy)">{{ scope.row.accuracy }}%</span>
          </template>
      </el-table-column>
      
      <el-table-column v-for="(q, index) in questions" :key="q.questionId" width="70" align="center">
          <template #header>
              <el-tooltip :content="q.questionContent" placement="top" :show-after="200" max-width="300">
                  <span style="cursor: help; text-decoration: underline dashed;">第{{ q.orderNum || index + 1 }}题</span>
              </el-tooltip>
          </template>
          <template #default="scope">
              <div class="matrix-cell">
                  <template v-if="getCellData(scope.row, q.questionId) && getCellData(scope.row, q.questionId).hasAnswer">
                      <span v-if="getCellData(scope.row, q.questionId).isCorrect" class="icon-correct">√</span>
                      
                      <!-- 错题显示 Tooltip -->
                      <el-tooltip v-else placement="top" :show-after="200">
                          <template #content>
                              <div style="max-width: 200px;">
                                  <div><b>错误选项：</b> {{ getCellData(scope.row, q.questionId).studentAnswer }}</div>
                                  <div><b>选项内容：</b> {{ getOptionContent(q, getCellData(scope.row, q.questionId).studentAnswer) }}</div>
                              </div>
                          </template>
                          <span class="icon-wrong">×</span>
                      </el-tooltip>
                  </template>
                  <span v-else class="text-gray">-</span>
              </div>
          </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  matrixData: {
    type: Array,
    required: true
  },
  questions: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
});

// 预处理数据：计算正确率（只统计表头显示的理论题）
const matrixDataWithAccuracy = computed(() => {
    // 获取表头显示的题目 ID 集合
    const theoryQuestionIds = new Set(props.questions.map(q => String(q.questionId)));
    
    return props.matrixData.map((row, index) => {
        let correctCount = 0;
        let answeredCount = 0;
        
        if (row.answersMap) {
             Object.entries(row.answersMap).forEach(([qId, ans]) => {
                 // 只统计表头显示的理论题
                 if (!theoryQuestionIds.has(String(qId))) {
                     return; // 跳过非理论题
                 }
                 
                 // 只统计实际作答的题目（studentAnswer 非空）
                 if (ans.studentAnswer && ans.studentAnswer !== '') {
                     answeredCount++;
                     if (ans.isCorrect) {
                         correctCount++;
                     }
                 }
             });
        }
        
        const accuracy = answeredCount > 0 ? Math.round((correctCount / answeredCount) * 100) : 0;
        
        return {
            ...row,
            accuracy,
            formattedClassName: row.formattedClassName || row.className
        };
    });
});

function getCellData(row, questionId) {
  const answerObj = row.answersMap ? row.answersMap[questionId] : null;
  if (!answerObj) return null;
  return {
      hasAnswer: true,
      isCorrect: answerObj.isCorrect,
      studentAnswer: answerObj.studentAnswer
  };
}

function getOptionContent(question, optionKey) {
    if (!question || !question.optionContents) return '未知内容';
    // 简单清洗一下 Key
    const key = (optionKey || '').trim();
    return question.optionContents[key] || '未知内容';
}

function getAccuracyClass(acc) {
    if (acc >= 90) return 'text-success';
    if (acc < 60) return 'text-danger';
    return '';
}
</script>

<style scoped>
.analysis-matrix-card {
    margin-bottom: 15px;
}
.icon-correct {
    color: #67C23A;
    font-weight: bold;
    font-size: 16px;
}
.icon-wrong {
    color: #F56C6C;
    font-weight: bold;
    font-size: 16px;
    cursor: pointer;
}
.text-gray {
    color: #d9d9d9;
}
.text-success {
  color: #67C23A;
  font-weight: bold;
}
.text-danger {
  color: #F56C6C;
  font-weight: bold;
}
</style>
