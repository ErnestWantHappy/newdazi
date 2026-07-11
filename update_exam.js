const fs = require('fs');
const path = require('path');

const file = path.join(__dirname, 'RuoYi-Vue3/src/views/student/countyExam.vue');
const sourceFile = path.join(__dirname, 'RuoYi-Vue3/src/views/student/index.vue');

let content = fs.readFileSync(sourceFile, 'utf8');

// 1. component name
content = content.replace('<script setup name="StudentIndex">', '<script setup name="StudentCountyExam">');

// 2. imports
content = content.replace(/import \{([\s\S]*?)getCurrentLesson,([\s\S]*?)submitAnswers as submitAnswersApi,([\s\S]*?)\} from "@\/api\/business\/studentHome";\r?\nimport \{ getCurrentCountyExam \} from "@\/api\/business\/countyExam";/,
`import {
  getHistoryScores,
  getWrongQuestions,
} from "@/api/business/studentHome";
import { 
  getCurrentCountyExam,
  submitCountyExam,
  saveCountyExamDraft as submitAnswersApi
} from "@/api/business/countyExam";`);

// 3. fetchData
content = content.replace(/const countyExamRes = await getCurrentCountyExam\(\)\.catch\(\(\) => \(\{ data: \{ hasExam: false \} \}\)\);\s*if \(countyExamRes\.data\?\.hasExam\) \{\s*router\.replace\("\/student\/county-exam"\);\s*return;\s*\}\s*const res = await getCurrentLesson\(\);\s*hasLesson\.value = res\.hasLesson \|\| false;/,
`const resRes = await getCurrentCountyExam().catch(() => ({ data: { hasExam: false } }));
    const res = resRes.data || {};
    hasLesson.value = res.hasExam || false;`);

// 4. variable assignments
content = content.replace(/lessonId\.value = res\.lessonId;/g, 'lessonId.value = res.examId;');
content = content.replace(/lessonTitle\.value = res\.lessonTitle;/g, 'lessonTitle.value = res.examName;');
content = content.replace(/res\.lessonId/g, 'res.examId');
content = content.replace(/lessonId: lessonId\.value/g, 'examId: lessonId.value');

// 5. refreshPracticalSubmission
content = content.replace(/const res = await getCurrentLesson\(\);/g, 'const res = (await getCurrentCountyExam()).data || {};');

// 6. Header buttons
content = content.replace(/<div class="header-actions">[\s\S]*?<\/div>/,
`<div class="header-actions">
          <el-button type="success" icon="Check" @click="submitFinalExam">确认交卷</el-button>
        </div>`);

// 7. submitFinalExam
content = content.replace('function formatTime(seconds) {',
`function submitFinalExam() {
  ElMessageBox.confirm("确定交卷吗？交卷后将无法再修改答案。", "提示", {
    type: "warning",
    confirmButtonText: "确认交卷",
    cancelButtonText: "继续检查",
  }).then(() => {
    loading.value = true;
    submitCountyExam({ examId: lessonId.value }).then(() => {
      ElMessage.success("交卷成功！");
      router.replace("/student/index");
    }).catch(() => {
      loading.value = false;
      ElMessage.error("交卷失败，请重试");
    });
  }).catch(() => {});
}

function formatTime(seconds) {`);

// 8. Fix hasLesson to hasExam
content = content.replace(/if \(res\.hasLesson\)/g, 'if (res.hasExam)');

fs.writeFileSync(file, content, 'utf8');
console.log('Update finished safely.');
