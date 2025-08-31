<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 左侧：课程内容区 -->
      <el-col :span="10" :xs="24">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>课程内容设计</span>
            </div>
          </template>
          <!-- 核心修复：将 :model 指向 form 本身 -->
          <el-form ref="lessonRef" :model="form" :rules="rules" label-width="80px">
            <el-form-item label="课程标题" prop="lessonTitle">
              <el-input v-model="form.lessonTitle" placeholder="请输入课程/作业标题" />
            </el-form-item>
            <el-row>
              <el-col :span="12">
                <el-form-item label="年级" prop="grade">
                  <el-select v-model="form.grade" placeholder="请选择年级" style="width:100%" :disabled="isAddMode">
                    <el-option
                      v-for="dict in biz_grade"
                      :key="dict.value"
                      :label="dict.label"
                      :value="parseInt(dict.value)"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="学期" prop="semester">
                  <el-select v-model="form.semester" placeholder="请选择学期" style="width:100%" :disabled="isAddMode">
                    <el-option
                      v-for="dict in biz_semester"
                      :key="dict.value"
                      :label="dict.label"
                      :value="dict.value"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="第几课" prop="lessonNum">
              <el-input-number v-model="form.lessonNum" placeholder="课程序号" :min="1" :max="30" />
            </el-form-item>

            <!-- 核心修复：将 v-model 指向 form 内部的属性 -->
            <el-form-item label="指派班级" prop="assignedClassCodes">
              <el-checkbox-group v-model="form.assignedClassCodes">
                <el-checkbox v-for="className in allClassesInGrade" :key="className" :label="className" />
              </el-checkbox-group>
            </el-form-item>

          </el-form>

          <el-divider />
          <h4>已选题目列表 (总分: {{ totalScore }})</h4>
          <el-table :data="selectedQuestions" row-key="questionId" style="width: 100%">
            <el-table-column label="题干" prop="questionContent" :show-overflow-tooltip="true" />
            <el-table-column label="题型" align="center" width="100">
               <template #default="scope">
                  <dict-tag :options="biz_question_type" :value="scope.row.questionType"/>
               </template>
            </el-table-column>
            <el-table-column label="分值" align="center" width="120">
              <template #default="scope">
                <el-input-number v-model="scope.row.questionScore" :min="0" :max="100" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="80">
              <template #default="scope">
                <el-button link type="danger" @click="handleRemoveQuestion(scope.row)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧：题库选题区 -->
      <el-col :span="14" :xs="24">
        <el-card>
           <template #header>
             <div class="card-header">
               <span>题库选题区</span>
             </div>
           </template>
          <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="68px">
            <el-form-item label="题干" prop="questionContent">
              <el-input v-model="queryParams.questionContent" placeholder="请输入题干关键词" clearable @keyup.enter="handleQuery"/>
            </el-form-item>
            <el-form-item label="年级" prop="grade">
              <el-select v-model="queryParams.grade" placeholder="年级" clearable>
                <el-option v-for="dict in biz_grade" :key="dict.value" :label="dict.label" :value="dict.value"/>
              </el-select>
            </el-form-item>
            <el-form-item label="学期" prop="semester">
              <el-select v-model="queryParams.semester" placeholder="学期" clearable>
                <el-option v-for="dict in biz_semester" :key="dict.value" :label="dict.label" :value="dict.value"/>
              </el-select>
            </el-form-item>
            <el-form-item label="题型" prop="questionType">
              <el-select v-model="queryParams.questionType" placeholder="题目类型" clearable>
                <el-option v-for="dict in biz_question_type" :key="dict.value" :label="dict.label" :value="dict.value"/>
              </el-select>
            </el-form-item>
             <el-form-item label="来源" prop="isPublic">
               <el-select v-model="queryParams.isPublic" placeholder="题目来源" clearable>
                 <el-option label="公共题库" value="Y" />
                 <el-option label="我的私有" value="N" />
               </el-select>
             </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table v-loading="loading" :data="questionBankList" row-key="questionId">
             <el-table-column label="题干/选项" min-width="250">
               <template #default="scope">
                 <div class="question-content-text">{{ stripHtml(scope.row.questionContent) }}</div>
                 <div v-if="scope.row.questionType === 'choice'" class="options-list">
                   <p>A. {{ scope.row.optionA }}</p>
                   <p>B. {{ scope.row.optionB }}</p>
                   <p>C. {{ scope.row.optionC }}</p>
                   <p>D. {{ scope.row.optionD }}</p>
                 </div>
                 <div v-if="scope.row.questionType === 'typing'" class="typing-info">
                   <span>总字数: {{ scope.row.wordCount }}</span>
                   <span style="margin-left: 15px;">时长: {{ scope.row.typingDuration }} 分钟</span>
                 </div>
               </template>
             </el-table-column>
             <el-table-column label="题型" align="center" width="100">
               <template #default="scope">
                  <dict-tag :options="biz_question_type" :value="scope.row.questionType"/>
               </template>
             </el-table-column>
             <el-table-column label="操作" align="center" width="100">
               <template #default="scope">
                 <el-button
                   v-if="scope.row.questionType === 'practical' && scope.row.previewPath"
                   link
                   type="success"
                   @click="handlePreviewFile(scope.row)"
                 >预览</el-button>
                 <el-button 
                   link 
                   type="primary" 
                   @click="handleAddQuestion(scope.row)" 
                   :disabled="isQuestionSelected(scope.row.questionId)"
                 >添加</el-button>
               </template>
             </el-table-column>
          </el-table>
          <pagination
             v-show="total > 0"
             :total="total"
             v-model:page="queryParams.pageNum"
             v-model:limit="queryParams.pageSize"
             @pagination="getQuestionList"
           />
        </el-card>
      </el-col>
    </el-row>

    <div class="footer-toolbar">
      <el-button type="primary" @click="submitForm">保 存</el-button>
      <el-button @click="cancel">取 消</el-button>
    </div>

    <pdf-preview ref="pdfPreviewRef" />
  </div>
</template>

<script setup name="LessonDesigner">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getLessonDetails, saveAllLessonDetails } from "@/api/business/lesson";
import { listQuestion } from "@/api/business/question";
import PdfPreview from '@/components/PdfPreview/index.vue';

const { proxy } = getCurrentInstance();
const route = useRoute();
const router = useRouter();

const { biz_grade, biz_semester, biz_question_type } = proxy.useDict("biz_grade", "biz_semester", "biz_question_type");

const loading = ref(true);
const total = ref(0);
const pdfPreviewRef = ref(null);
const isAddMode = ref(false);

// 核心修复：将 assignedClassCodes 整合到 form 对象中
const form = ref({
  lessonId: null,
  lessonTitle: null,
  grade: null,
  semester: null,
  lessonNum: 1,
  assignedClassCodes: [], // 初始化为空数组
});
const selectedQuestions = ref([]);
const allClassesInGrade = ref([]);

const questionBankList = ref([]);
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  questionContent: null,
  grade: null,
  semester: null,
  isPublic: null,
  questionType: null,
});

const rules = {
  lessonTitle: [{ required: true, message: "课程/作业标题不能为空", trigger: "blur" }],
  grade: [{ required: true, message: "年级不能为空", trigger: "change" }],
  semester: [{ required: true, message: "学期不能为空", trigger: "change" }],
  assignedClassCodes: [{ required: true, type: 'array', min: 1, message: "请至少指派一个班级", trigger: "change" }]
};

const totalScore = computed(() => {
  return selectedQuestions.value.reduce((sum, question) => {
    return sum + (question.questionScore || 0);
  }, 0);
});

function getDefaultSemester() {
  const now = new Date();
  const month = now.getMonth() + 1;
  if (month >= 2 && month <= 7) {
    return '1';
  } else {
    return '0';
  }
}

function stripHtml(html) {
  if (!html) return "";
  let tmp = document.createElement("DIV");
  tmp.innerHTML = html;
  return tmp.textContent || tmp.innerText || "";
}

function getQuestionList() {
  loading.value = true;
  queryParams.value.grade = form.value.grade;
  listQuestion(queryParams.value).then(response => {
    questionBankList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  getQuestionList();
}

function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

function isQuestionSelected(questionId) {
  return selectedQuestions.value.some(q => q.questionId === questionId);
}

function handleAddQuestion(row) {
    if (row.questionType === 'practical') {
        const hasPractical = selectedQuestions.value.some(q => q.questionType === 'practical');
        if (hasPractical) {
            proxy.$modal.msgError("一门课程最多只能添加一道操作题。");
            return;
        }
    }
    if (row.questionType === 'typing') {
        const hasTyping = selectedQuestions.value.some(q => q.questionType === 'typing');
        if (hasTyping) {
            proxy.$modal.msgError("一门课程最多只能添加一道打字题。");
            return;
        }
    }
    if (!isQuestionSelected(row.questionId)) {
        const newQuestion = { 
            questionId: row.questionId,
            questionContent: row.questionContent,
            questionType: row.questionType,
            questionScore: 10,
            orderNum: selectedQuestions.value.length + 1
        };
        selectedQuestions.value.push(newQuestion);
    }
}

function handleRemoveQuestion(row) {
  const index = selectedQuestions.value.findIndex(q => q.questionId === row.questionId);
  if (index > -1) {
    selectedQuestions.value.splice(index, 1);
  }
}

function handlePreviewFile(row) {
  if (pdfPreviewRef.value && row.previewPath) {
    const baseUrl = import.meta.env.VITE_APP_BASE_API;
    const fullPdfUrl = baseUrl + row.previewPath;
    pdfPreviewRef.value.open(fullPdfUrl);
  } else {
    proxy.$modal.msgError("没有可预览的PDF文件。");
  }
}

function submitForm() {
  proxy.$refs["lessonRef"].validate(valid => {
    if (valid) {
      if (selectedQuestions.value.length === 0) {
        proxy.$modal.msgError("请至少选择一道题目");
        return;
      }
      if (totalScore.value !== 100) {
        proxy.$modal.msgError("所有题目总分必须等于100分！当前总分：" + totalScore.value);
        return;
      }
      
      const payload = {
        ...form.value,
        questions: selectedQuestions.value,
        // assignedClassCodes 已经在 form.value 中，所以这里不需要再单独添加
      };
      
      saveAllLessonDetails(payload).then(response => {
        proxy.$modal.msgSuccess("保存成功");
        close();
      });
    }
  });
}

function close() {
  proxy.$tab.closePage(route);
  router.push({ path: "/teacher-dashboard" });
}

function cancel() {
  close();
}

function initialize() {
  const { lessonId } = route.params;
  const { grade, classes } = route.query;

  if (lessonId) {
    isAddMode.value = false;
    getLessonDetails(lessonId).then(response => {
      form.value = response.data;
      selectedQuestions.value = response.data.questions || [];
      // 核心修复：确保 assignedClassCodes 在 form 对象内部
      form.value.assignedClassCodes = response.data.assignedClassCodes || [];
      allClassesInGrade.value = response.data.allClassesInGrade || [];
      getQuestionList();
    });
  } else {
    isAddMode.value = true;
    // 核心修复：初始化时就包含 assignedClassCodes
    form.value = {
      lessonId: null,
      lessonTitle: null,
      grade: grade ? parseInt(grade) : null,
      semester: getDefaultSemester(),
      lessonNum: 1,
      assignedClassCodes: [],
    };
    if(classes) {
        allClassesInGrade.value = JSON.parse(classes);
    }
    getQuestionList();
  }
}

onMounted(() => {
  initialize();
});
</script>


<style scoped>
.footer-toolbar {
  position: fixed;
  bottom: 0;
  right: 0;
  width: 100%;
  height: 56px;
  line-height: 56px;
  padding: 0 24px;
  background: #fff;
  border-top: 1px solid #e8e8e8;
  box-shadow: 0 -1px 2px rgba(0, 0, 0, 0.03);
  text-align: right;
  z-index: 9;
}
.options-list {
  margin-top: 8px;
  padding-left: 15px;
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}
.options-list p {
  margin: 4px 0;
  white-space: normal;
  word-break: break-all;
}
.question-content-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.typing-info {
  margin-top: 8px;
  font-size: 13px;
  color: #909399;
}
</style>
