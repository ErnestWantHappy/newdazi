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
            <el-form-item label="指派班级" prop="assignedClasses">
              <el-checkbox-group v-model="form.assignedClasses">
                <el-checkbox 
                  v-for="cls in filteredManagedClasses" 
                  :key="cls.id" 
                  :label="cls.classCode + '班'"
                >
                  {{ cls.classCode }}班
                </el-checkbox>
              </el-checkbox-group>
              <div v-if="form.grade && filteredManagedClasses.length === 0" style="color: #909399; font-size: 12px;">
                您没有管理该年级对应的班级，请先在"班级管理"中添加
              </div>
              <div v-else-if="!form.grade" style="color: #909399; font-size: 12px;">
                请先选择年级
              </div>
            </el-form-item>

          </el-form>

          <el-divider />
          <h4>已选题目列表 (总分: {{ totalScore }})</h4>
          <el-table :data="selectedQuestions" row-key="questionId" style="width: 100%">
            <el-table-column label="题干" prop="questionContent" :show-overflow-tooltip="true">
              <template #default="scope">
                <div class="question-content-text">{{ stripHtml(scope.row.questionContent) }}</div>
                <div v-if="scope.row.questionType === 'choice'" class="options-list">
                  <p>A. {{ scope.row.optionA || '未配置' }}</p>
                  <p>B. {{ scope.row.optionB || '未配置' }}</p>
                  <p>C. {{ scope.row.optionC || '未配置' }}</p>
                  <p>D. {{ scope.row.optionD || '未配置' }}</p>
                </div>
                <!-- 新增：判断题在已选列表中回显正确答案 -->
                <div v-else-if="scope.row.questionType === 'judgment'" class="judge-info">
                  正确答案：{{ formatJudgeAnswer(scope.row.answer) }}
                </div>
                <div v-else-if="scope.row.questionType === 'typing'" class="typing-info">
                  <span>总字数：{{ scope.row.wordCount || 0 }}</span>
                  <span style="margin-left: 15px;">时长：{{ scope.row.typingDuration || 0 }} 分钟</span>
                </div>
              </template>
            </el-table-column>
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
            <el-table-column label="操作" align="center" width="140">
              <template #default="scope">
                <!-- 新增：操作题支持在已选列表中直接预览 -->
                <el-button
                  v-if="scope.row.questionType === 'practical' && scope.row.previewPath"
                  link
                  type="success"
                  @click="handlePreviewFile(scope.row)"
                >预览</el-button>
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
            <el-form-item label="课时" prop="lessonNum">
              <el-select v-model="queryParams.lessonNum" placeholder="第几课" clearable style="width: 100px">
                <el-option v-for="n in 20" :key="n" :label="'第' + n + '课'" :value="n"/>
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
            <el-table-column label="题干/选项" min-width="260">
              <template #default="scope">
                <div class="question-content-text">{{ stripHtml(scope.row.questionContent) }}</div>
                <div v-if="scope.row.questionType === 'choice'" class="options-list">
                  <p>A. {{ scope.row.optionA || '未配置' }}</p>
                  <p>B. {{ scope.row.optionB || '未配置' }}</p>
                  <p>C. {{ scope.row.optionC || '未配置' }}</p>
                  <p>D. {{ scope.row.optionD || '未配置' }}</p>
                </div>
                <!-- 新增：判断题在题库列表中直观展示正确答案 -->
                <div v-else-if="scope.row.questionType === 'judgment'" class="judge-info">
                  正确答案：{{ formatJudgeAnswer(scope.row.answer) }}
                </div>
                <div v-else-if="scope.row.questionType === 'typing'" class="typing-info">
                  <span>总字数：{{ scope.row.wordCount || 0 }}</span>
                  <span style="margin-left: 15px;">时长：{{ scope.row.typingDuration || 0 }} 分钟</span>
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
import { ref, computed, onMounted, getCurrentInstance, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getLessonDetails, saveAllLessonDetails } from "@/api/business/lesson";
import { listQuestion } from "@/api/business/question";
import { getMyClasses } from "@/api/business/teacherClass";
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
  assignedClasses: [], // 改为存储 "entryYear-classCode" 格式
});
const selectedQuestions = ref([]);
const myManagedClasses = ref([]); // 教师管理的班级列表

const questionBankList = ref([]);
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  questionContent: null,
  grade: null,
  semester: null,
  isPublic: null,
  questionType: null,
  lessonNum: null,
});

const rules = {
  lessonTitle: [{ required: true, message: "课程/作业标题不能为空", trigger: "blur" }],
  grade: [{ required: true, message: "年级不能为空", trigger: "change" }],
  semester: [{ required: true, message: "学期不能为空", trigger: "change" }],
  assignedClasses: [{ required: true, type: 'array', min: 1, message: "请至少指派一个班级", trigger: "change" }]
};

const totalScore = computed(() => {
  return selectedQuestions.value.reduce((sum, question) => {
    return sum + (question.questionScore || 0);
  }, 0);
});

// 根据年级数字(1-12)计算对应的入学年份
function gradeToEntryYear(grade) {
  if (!grade) return null;
  
  const now = new Date();
  const currentYear = now.getFullYear();
  const month = now.getMonth() + 1;
  
  // 计算当前学年起始年（9月开学）
  const academicStartYear = month >= 9 ? currentYear : currentYear - 1;
  
  // 根据年级判断学部类型和该学部内的年级序号
  let gradeInSection; // 该学部内的年级序号（1-6或1-3）
  
  if (grade >= 1 && grade <= 6) {
    // 小学：1-6年级
    gradeInSection = grade;
  } else if (grade >= 7 && grade <= 9) {
    // 初中：7-9年级，转换为1-3
    gradeInSection = grade - 6;
  } else if (grade >= 10 && grade <= 12) {
    // 高中：10-12年级，转换为1-3
    gradeInSection = grade - 9;
  } else {
    return null;
  }
  
  // 入学年份 = 当前学年起始年 - (该学部内年级序号 - 1)
  // 例如：2025学年，一年级/七年级/高一 的入学年份都是2025
  return String(academicStartYear - gradeInSection + 1);
}

// 过滤后的班级列表（根据选择的入学年份过滤）
const filteredManagedClasses = computed(() => {
  if (!form.value.grade) {
    return [];
  }
  // 使用 gradeToEntryYear 转换年级为入学年份
  const targetEntryYear = gradeToEntryYear(form.value.grade);
  
  if (!targetEntryYear) {
    return [];
  }
  
  const result = myManagedClasses.value.filter(cls => {
    // 使用 == 进行宽松匹配
    return cls.entryYear == targetEntryYear;
  });
  return result;
});

// 监听年级变化，清空已选班级，防止跨年级指派错误
// 注意：oldVal为null时是初始化阶段，不清空
watch(() => form.value.grade, (newVal, oldVal) => {
  if (oldVal !== null && newVal !== oldVal) {
    form.value.assignedClasses = [];
  }
});

// ... (省略中间代码)

// 提交表单
function submitForm() {
  proxy.$refs["lessonRef"].validate(valid => {
    if (valid) {
      // 构造提交数据
      const data = {
        ...form.value,
        questions: selectedQuestions.value,
        // 直接提交班级名称列表（后端会自动根据grade计算entryYear）
        assignedClassCodes: form.value.assignedClasses 
      };

      if (form.value.lessonId) {
        // 修改模式使用 saveAll
        saveAllLessonDetails(data).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          // 修改成功后跳转回来源页面（通常是教师首页或列表页）
          if (route.query.redirect) {
             router.push(route.query.redirect);
          } else {
             router.push('/index'); // 默认回首页
          }
        });
      } else {
        // 新增模式
        saveAllLessonDetails(data).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          router.push('/index'); 
        });
      }
    }
  });
}

// ... 

function initialize() {
  const { lessonId } = route.params;
  const { grade, classes } = route.query;

  // 先加载教师管理的班级
  loadMyManagedClasses();

  if (lessonId) {
    isAddMode.value = false;
    getLessonDetails(lessonId).then(response => {
      const detail = response.data || {};
      
      // 后端返回的是 ["1班", "5班"] 格式，直接使用
      const assignedClasses = detail.assignedClassCodes || [];

      form.value = {
        lessonId: detail.lessonId,
        lessonTitle: detail.lessonTitle,
        grade: detail.grade,
        semester: detail.semester ?? getDefaultSemester(),
        lessonNum: detail.lessonNum,
        assignedClasses: assignedClasses,
      };
      selectedQuestions.value = (detail.questions || []).map((item, index) => ({
        ...item,
        questionScore: item.questionScore != null ? item.questionScore : 0,
        orderNum: item.orderNum != null ? item.orderNum : index + 1,
      }));
      getQuestionList();
    });
  } else {
    isAddMode.value = true;
    form.value = {
      lessonId: null,
      lessonTitle: null,
      grade: grade ? parseInt(grade, 10) : null,
      semester: getDefaultSemester(),
      lessonNum: 1,
      assignedClasses: [],
    };
    
    // 如果URL有预设班级 (e.g. ["1班"])，尝试设置
    if (classes) {
        try {
            const classList = JSON.parse(classes);
            if (Array.isArray(classList)) {
                form.value.assignedClasses = classList;
            }
        } catch (e) {
            console.error("解析classes参数失败", e);
        }
    }

    selectedQuestions.value = [];
    getQuestionList();
  }
}

function getDefaultSemester() {
  const now = new Date();
  const month = now.getMonth() + 1;
  if (month >= 2 && month <= 7) {
    return '1';
  } else {
    return '0';
  }
}

// 新增：统一判断题答案显示文本，兼容多种答案写法
// 新增：统一判断题答案显示文本，兼容多种答案写法
function formatJudgeAnswer(answer) {
  if (answer === null || answer === undefined || answer === '') {
    return '未配置';
  }
  const normalized = String(answer).trim().toLowerCase();
  const truthy = ['1', 'true', 't', 'y', 'yes', '对', '正确', 'right'];
  const falsy = ['0', 'false', 'f', 'n', 'no', '错', '错误', 'wrong'];
  if (truthy.includes(normalized)) {
    return '对';
  }
  if (falsy.includes(normalized)) {
    return '错';
  }
  return normalized || '未配置';
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
            proxy.$modal.msgError('一门课程最多只能添加一道操作题。');
            return;
        }
    }
    if (row.questionType === 'typing') {
        const hasTyping = selectedQuestions.value.some(q => q.questionType === 'typing');
        if (hasTyping) {
            proxy.$modal.msgError('一门课程最多只能添加一道打字题。');
            return;
        }
    }
    if (!isQuestionSelected(row.questionId)) {
        // 新增：携带选项、答案及附件信息，方便在已选列表和回显场景展示
        const newQuestion = {
            questionId: row.questionId,
            questionContent: row.questionContent,
            questionType: row.questionType,
            questionScore: 10,
            orderNum: selectedQuestions.value.length + 1,
            optionA: row.optionA,
            optionB: row.optionB,
            optionC: row.optionC,
            optionD: row.optionD,
            answer: row.answer,
            previewPath: row.previewPath,
            typingDuration: row.typingDuration,
            wordCount: row.wordCount,
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



// 加载教师管理的班级
function loadMyManagedClasses() {
  getMyClasses().then(response => {
    myManagedClasses.value = response.data || [];
  });
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

.judge-info {
  margin-top: 8px;
  font-size: 13px;
  color: #606266;
}
</style>










