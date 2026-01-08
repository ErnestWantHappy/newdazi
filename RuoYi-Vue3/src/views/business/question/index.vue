<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="年级" prop="grade">
        <el-select v-model="queryParams.grade" placeholder="请选择年级" clearable style="width: 200px">
          <el-option v-for="dict in biz_grade" :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="学期" prop="semester">
        <el-select v-model="queryParams.semester" placeholder="请选择学期" clearable style="width: 200px">
          <el-option v-for="dict in biz_semester" :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="第几课" prop="lessonNum">
        <el-select v-model="queryParams.lessonNum" placeholder="请选择" clearable style="width: 120px">
          <el-option v-for="n in 20" :key="n" :label="'第' + n + '课'" :value="n"/>
        </el-select>
      </el-form-item>
      <el-form-item label="题目类型" prop="questionType">
        <el-select v-model="queryParams.questionType" placeholder="请选择类型" clearable style="width: 200px">
          <el-option v-for="dict in biz_question_type" :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="是否公开" prop="isPublic">
        <el-select v-model="queryParams.isPublic" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="dict in sys_yes_no" :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="题目内容" prop="questionContent">
        <el-input v-model="queryParams.questionContent" placeholder="请输入题目内容关键字" clearable style="width: 240px" @keyup.enter="handleQuery"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:question:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Upload" @click="handleImport" v-hasPermi="['business:question:import']">批量导入</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['business:question:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['business:question:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="questionList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="题目ID" align="center" prop="questionId" width="80" />
      <el-table-column label="题目类型" align="center" prop="questionType" width="100">
        <template #default="scope"><dict-tag :options="biz_question_type" :value="scope.row.questionType"/></template>
      </el-table-column>
      <el-table-column label="题目内容" align="left" prop="questionContent" :show-overflow-tooltip="true" />
      <el-table-column label="总字数" align="center" prop="wordCount" width="100" />
      <el-table-column label="打字时长(分)" align="center" prop="typingDuration" width="120" />
      <el-table-column label="年级" align="center" prop="grade" width="100">
        <template #default="scope"><dict-tag :options="biz_grade" :value="String(scope.row.grade)"/></template>
      </el-table-column>
      <el-table-column label="学期" align="center" prop="semester" width="100">
        <template #default="scope"><dict-tag :options="biz_semester" :value="String(scope.row.semester)"/></template>
      </el-table-column>
      <el-table-column label="第几课" align="center" prop="lessonNum" width="80">
        <template #default="scope">
          <span v-if="scope.row.lessonNum">第{{ scope.row.lessonNum }}课</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="是否公开" align="center" prop="isPublic" width="100">
        <template #default="scope"><dict-tag :options="sys_yes_no" :value="scope.row.isPublic"/></template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:question:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:question:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList"/>

    <!-- 添加或修改对话框 -->
    <el-dialog :title="title" v-model="open" width="800px" append-to-body>
      <el-form ref="questionRef" :model="form" :rules="rules" label-width="120px">
        <!-- 通用表单项 -->
        <el-row>
          <el-col :span="12">
            <el-form-item label="题目类型" prop="questionType">
              <el-select v-model="form.questionType" placeholder="请选择题目类型">
                <el-option v-for="dict in biz_question_type" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否公开" prop="isPublic">
              <el-radio-group v-model="form.isPublic">
                <el-radio v-for="dict in sys_yes_no" :key="dict.value" :label="dict.value">{{dict.label}}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="年级" prop="grade">
              <el-select v-model="form.grade" placeholder="请选择年级">
                <el-option v-for="dict in biz_grade" :key="dict.value" :label="dict.label" :value="parseInt(dict.value)"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学期" prop="semester">
              <el-select v-model="form.semester" placeholder="请选择学期">
                <el-option v-for="dict in biz_semester" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="第几课" prop="lessonNum">
              <el-select v-model="form.lessonNum" placeholder="请选择第几课" clearable>
                <el-option v-for="n in 20" :key="n" :label="'第' + n + '课'" :value="n"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item :label="questionContentLabel" prop="questionContent">
          <el-input v-model="form.questionContent" :rows="questionContentRows" type="textarea" placeholder="请输入内容" />
        </el-form-item>

        <!-- 动态表单项: 打字题专属 -->
        <div v-if="form.questionType === 'typing'">
          <el-row>
            <el-col :span="12">
              <el-form-item label="打字时长(分钟)" prop="typingDuration">
                <el-input-number v-model="form.typingDuration" :min="1" placeholder="请输入时长" controls-position="right" style="width: 100%;"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="总字数" prop="wordCount">
                <el-input v-model="form.wordCount" placeholder="由题目内容自动计算" :disabled="true" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 动态表单项: 选择题专属 -->
        <div v-if="form.questionType === 'choice'">
          <el-form-item label="选项A" prop="optionA"><el-input v-model="form.optionA" placeholder="请输入选项A" /></el-form-item>
          <el-form-item label="选项B" prop="optionB"><el-input v-model="form.optionB" placeholder="请输入选项B" /></el-form-item>
          <el-form-item label="选项C" prop="optionC"><el-input v-model="form.optionC" placeholder="请输入选项C" /></el-form-item>
          <el-form-item label="选项D" prop="optionD"><el-input v-model="form.optionD" placeholder="请输入选项D" /></el-form-item>
          <el-form-item label="标准答案" prop="answer">
            <el-radio-group v-model="form.answer">
              <el-radio label="A">A</el-radio>
              <el-radio label="B">B</el-radio>
              <el-radio label="C">C</el-radio>
              <el-radio label="D">D</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="题目解析" prop="analysis">
             <el-input v-model="form.analysis" :rows="3" type="textarea" placeholder="请输入解析" />
          </el-form-item>
        </div>

        <!-- 动态表单项: 判断题专属 -->
        <div v-if="form.questionType === 'judgment'">
           <el-form-item label="标准答案" prop="answer">
            <el-radio-group v-model="form.answer">
              <el-radio label="T">正确</el-radio>
              <el-radio label="F">错误</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="题目解析" prop="analysis">
             <el-input v-model="form.analysis" :rows="3" type="textarea" placeholder="请输入解析" />
          </el-form-item>
        </div>

        <!-- 动态表单项: 操作题专属 -->
        <div v-if="form.questionType === 'practical'">
          <el-form-item label="操作文件" prop="filePath">
             <!-- 核心修复：限制文件类型为.docx -->
            <file-upload v-model="form.filePath" :file-type="['docx']" />
          </el-form-item>
          
          <el-divider content-position="left">评分项配置（比例分配）</el-divider>
          <el-form-item label-width="0">
            <el-table :data="form.scoringItems" border style="width: 100%">
               <el-table-column type="index" width="50" align="center" />
               <el-table-column label="评分项名称" prop="itemName">
                  <template #default="scope">
                     <el-input v-model="scope.row.itemName" placeholder="如：界面设计" maxlength="50" />
                  </template>
               </el-table-column>
               <el-table-column label="比例值 (建议合计100)" prop="itemScore" width="180" align="center">
                  <template #default="scope">
                     <el-input-number v-model="scope.row.itemScore" :min="1" :max="100" controls-position="right" style="width: 100px" />
                  </template>
               </el-table-column>
               <el-table-column label="操作" width="80" align="center">
                  <template #default="scope">
                     <el-button link type="danger" icon="Delete" @click="removeScoringItem(scope.$index)"></el-button>
                  </template>
               </el-table-column>
            </el-table>
            <div style="margin-top: 10px; display: flex; justify-content: space-between; align-items: center;">
               <div>
                  <el-button type="primary" plain icon="Plus" size="small" @click="addScoringItem">添加评分项</el-button>
                  <span style="margin-left: 10px; color: #909399; font-size: 12px">提示：比例值合计应为100，系统会按课程设置的总分自动折算。</span>
               </div>
               <div style="font-weight: bold; color: #606266;">
                  当前合计: <span :style="{ color: scoringItemsSum === 100 ? '#67c23a' : '#f56c6c' }">{{ scoringItemsSum }}</span> / 100
               </div>
            </div>
          </el-form-item>
        </div>

      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 题目导入对话框 -->
    <el-dialog :title="upload.title" v-model="upload.open" width="400px" append-to-body>
      <el-upload
        ref="uploadRef"
        :limit="1"
        accept=".xlsx, .xls"
        :headers="upload.headers"
        :action="upload.url + '?updateSupport=' + upload.updateSupport"
        :disabled="upload.isUploading"
        :on-progress="handleFileUploadProgress"
        :on-success="handleFileSuccess"
        :auto-upload="false"
        drag
        >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip text-center">
            <span>仅允许导入xls、xlsx格式文件。</span>
            <el-link type="primary" :underline="false" style="font-size:12px;vertical-align: baseline;" @click="importTemplate">下载模板</el-link>
          </div>
        </template>
      </el-upload>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitFileForm">确 定</el-button>
          <el-button @click="cancelUpload">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Question">
import { listQuestion, getQuestion, delQuestion, addQuestion, updateQuestion } from "@/api/business/question";
import { getToken } from "@/utils/auth";
import { computed } from 'vue';
import { ElLoading, ElMessage } from 'element-plus'; // P6 import

const { proxy } = getCurrentInstance();
const { biz_question_type, sys_yes_no, biz_grade, biz_semester } = proxy.useDict('biz_question_type', 'sys_yes_no', 'biz_grade', 'biz_semester');

const questionList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    questionType: null,
    questionContent: null,
    isPublic: null,
    grade: null,
    semester: null,
    lessonNum: null,
  },
});

const { queryParams, form } = toRefs(data);

// --- 动态校验规则 --- 
const rules = computed(() => {
  const baseRules = {
    questionType: [{ required: true, message: "题目类型不能为空" }],
    questionContent: [{ required: true, message: "题目内容不能为空" }],
    isPublic: [{ required: true, message: "是否公开不能为空" }],
    grade: [{ required: true, message: "年级不能为空" }],
    semester: [{ required: true, message: "学期不能为空" }],
  };

  if (form.value.questionType === 'choice') {
    return {
      ...baseRules,
      optionA: [{ required: true, message: "选项A不能为空" }],
      optionB: [{ required: true, message: "选项B不能为空" }],
      optionC: [{ required: true, message: "选项C不能为空" }],
      optionD: [{ required: true, message: "选项D不能为空" }],
      answer: [{ required: true, message: "标准答案不能为空" }],
    };
  } else if (form.value.questionType === 'judgment') {
    return {
      ...baseRules,
      answer: [{ required: true, message: "标准答案不能为空" }],
    };
  } else if (form.value.questionType === 'practical') {
    return {
      ...baseRules,
      filePath: [{ required: true, message: "操作文件不能为空" }],
    };
  } else if (form.value.questionType === 'typing') {
    return {
      ...baseRules,
      typingDuration: [{ required: true, message: "打字时长不能为空" }],
    };
  }
  return baseRules;
});


/*** 题目导入参数 */
const upload = reactive({
  open: false,
  title: "",
  isUploading: false,
  updateSupport: 0,
  headers: { Authorization: "Bearer " + getToken() },
  url: import.meta.env.VITE_APP_BASE_API + "/business/question/importData"
});

const questionContentLabel = computed(() => {
  switch (form.value.questionType) {
    case 'typing':
      return '打字题内容';
    case 'practical':
      return '操作题任务';
    default:
      return '题目内容';
  }
});

const questionContentRows = computed(() => {
  return form.value.questionType === 'typing' ? 8 : 3;
});


/** 查询题库管理列表 */
function getList() {
  loading.value = true;
  listQuestion(queryParams.value).then(response => {
    questionList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

// 导入弹窗取消按钮
function cancelUpload() {
  upload.open = false;
  proxy.resetForm("uploadRef");
}

// 表单重置
function reset() {
  form.value = {
    questionId: null,
    questionType: null,
    questionContent: null,
    grade: null,
    semester: null,
    optionA: null,
    optionB: null,
    optionC: null,
    optionD: null,
    answer: null,
    analysis: null,
    filePath: null,
    isPublic: "Y",
    typingDuration: null,
    wordCount: null,
    lessonNum: null,
    creatorId: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    scoringItems: [] // P6
  };
  proxy.resetForm("questionRef");
}

/** P6.1: 计算评分项比例合计 */
const scoringItemsSum = computed(() => {
   if (!form.value.scoringItems || form.value.scoringItems.length === 0) return 0;
   return form.value.scoringItems.reduce((sum, i) => sum + (i.itemScore || 0), 0);
});

/** P6: 添加评分项 */
function addScoringItem() {
  if (!form.value.scoringItems) {
    form.value.scoringItems = [];
  }
  form.value.scoringItems.push({
    itemName: '',
    itemScore: 10
  });
}

/** P6: 删除评分项 */
function removeScoringItem(index) {
  form.value.scoringItems.splice(index, 1);
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.questionId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加题目";
  proxy.$nextTick(() => {
    proxy.$refs["questionRef"].clearValidate();
  });
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const _questionId = row.questionId || ids.value[0];
  getQuestion(_questionId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改题目";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["questionRef"].validate(valid => {
    if (valid) {
      // P6.1: 操作题设置了评分项时，比例值必须合计为100
      if (form.value.questionType === 'practical' && 
          form.value.scoringItems && form.value.scoringItems.length > 0) {
         if (scoringItemsSum.value !== 100) {
            ElMessage.warning('评分项比例值合计必须为100，当前为 ' + scoringItemsSum.value);
            return;
         }
      }
      
      // P6: 添加Loading效果
      const loadingInstance = ElLoading.service({
         lock: true,
         text: '正在保存数据...（操作题若包含文件转换可能需要较长时间，请耐心等待）',
         background: 'rgba(0, 0, 0, 0.7)',
      });
      
      if (form.value.questionId != null) {
        updateQuestion(form.value).then(response => {
          loadingInstance.close();
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        }).catch(() => {
          loadingInstance.close();
        });
      } else {
        addQuestion(form.value).then(response => {
          loadingInstance.close();
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        }).catch(() => {
          loadingInstance.close();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _questionIds = row.questionId || ids.value;
  proxy.$modal.confirm('是否确认删除题库管理编号为"' + _questionIds + '"的数据项？').then(function() {
    return delQuestion(_questionIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('business/question/export', {
    ...queryParams.value
  }, `question_${new Date().getTime()}.xlsx`);
}

/** 导入按钮操作 */
function handleImport() {
  upload.title = "题目导入";
  upload.open = true;
};

/** 下载模板操作 */
function importTemplate() {
  proxy.download("business/question/importTemplate", {}, `question_template_${new Date().getTime()}.xlsx`);
};

/**文件上传中处理 */
const handleFileUploadProgress = (event, file, fileList) => {
  upload.isUploading = true;
};

/** 文件上传成功处理 */
const handleFileSuccess = (response, file, fileList) => {
  upload.open = false;
  upload.isUploading = false;
  proxy.$refs["uploadRef"].clearFiles();
  proxy.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.msg + "</div>", "导入结果", { dangerouslyUseHTMLString: true });
  getList();
};

/** 提交上传文件 */
function submitFileForm() {
  proxy.$refs["uploadRef"].submit();
};

getList();
</script>
