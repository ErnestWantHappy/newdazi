<template>
  <div class="student-dashboard">
    <div class="header">
      <h1>欢迎你，{{ student.name }} 同学！</h1>
      <p>
        <span>学校：{{ student.school }}</span>
        <span>班级：{{ student.className }}</span>
        <span>当前年级：{{ currentGrade }}</span>
      </p>
    </div>

    <div class="content">
      <h2>我的任务</h2>
      <div class="task-list">
        <div class="task-card" v-for="task in tasks" :key="task.id">
          <h3>{{ task.title }}</h3>
          <p>课程：{{ task.lessonName }}</p>
          <p>截止时间：{{ task.dueDate }}</p>
          <button class="start-button">开始答题</button>
        </div>
         <div v-if="tasks.length === 0" class="no-task">
          <p>🎉 暂时没有新的任务，休息一下吧！</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
// 导入我们刚刚创建的年级计算工具函数
import { calculateGrade } from '@/utils/student.js';

// --- Mock Data: 模拟数据 ---
// 在真实开发中, 这些数据将通过API从后端获取
// 我们假设学生登录后, 从后端获取到了他自己的信息
const student = ref({
  account: '2022710422', // 这是一个关键的模拟账号
  name: '张三',
  school: '象山县实验小学',
  className: '三年级(4)班'
});

// 模拟待办任务列表
const tasks = ref([
  { id: 1, title: '第一课课后练习', lessonName: '智能物联系统的控制', dueDate: '2025-06-20' },
  { id: 2, title: '打字速度测试', lessonName: '第五单元-键盘风云', dueDate: '2025-06-22' }
]);
// 如果想测试没有任务的情况, 可以把上面的数组置空: const tasks = ref([]);


// --- Logic: 页面逻辑 ---

// 使用计算属性来动态计算年级, 这样如果学生账号变化, 年级也会自动更新
const currentGrade = computed(() => {
  return calculateGrade(student.value.account);
});

onMounted(() => {
  // 页面加载时可以执行一些操作, 比如从后端获取真实数据
  console.log('学生端首页加载完成。');
  console.log(`根据账号 ${student.value.account} 计算出的当前年级是: ${currentGrade.value}`);
});

</script>

<style lang="scss" scoped>
.student-dashboard {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: 100vh;
}

.header {
  background-color: #fff;
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.header h1 {
  margin: 0 0 10px 0;
  font-size: 24px;
}

.header p span {
  margin-right: 20px;
  color: #606266;
}

.content h2 {
  font-size: 20px;
  margin-bottom: 16px;
}

.task-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.task-card {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  transition: transform 0.3s ease;
}

.task-card:hover {
    transform: translateY(-5px);
}

.task-card h3 {
  margin: 0 0 12px 0;
}

.start-button {
    margin-top: 16px;
    padding: 10px 20px;
    background-color: #409EFF;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    transition: background-color 0.3s ease;
}

.start-button:hover {
    background-color: #66b1ff;
}

.no-task {
    text-align: center;
    padding: 40px;
    background-color: #fff;
    border-radius: 8px;
    color: #909399;
    grid-column: 1 / -1; /* 让这个元素横跨所有列 */
}
</style>