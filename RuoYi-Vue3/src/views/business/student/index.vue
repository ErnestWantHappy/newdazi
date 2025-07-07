<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="关联的用户ID (sys_user.user_id)" prop="userId">
        <el-input
          v-model="queryParams.userId"
          placeholder="请输入关联的用户ID (sys_user.user_id)"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="所属学校ID" prop="schoolId">
        <el-input
          v-model="queryParams.schoolId"
          placeholder="请输入所属学校ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="学号" prop="studentNo">
        <el-input
          v-model="queryParams.studentNo"
          placeholder="请输入学号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="入学年份" prop="entryYear">
        <el-input
          v-model="queryParams.entryYear"
          placeholder="请输入入学年份"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="班级编号" prop="classCode">
        <el-input
          v-model="queryParams.classCode"
          placeholder="请输入班级编号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['business:student:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
    <el-button
      type="info"
      plain
      icon="Upload"
      @click="handleImport"
      v-hasPermi="['business:student:import']"
    >导入</el-button>
  </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['business:student:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['business:student:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['business:student:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="studentList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="学生主键ID" align="center" prop="studentId" />
      <el-table-column label="关联的用户ID (sys_user.user_id)" align="center" prop="userId" />
      <el-table-column label="所属学校ID" align="center" prop="schoolId" />
      <el-table-column label="学号" align="center" prop="studentNo" />
      <el-table-column label="入学年份" align="center" prop="entryYear" />
      <el-table-column label="班级编号" align="center" prop="classCode" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:student:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:student:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改学生管理对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="studentRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="关联的用户ID (sys_user.user_id)" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入关联的用户ID (sys_user.user_id)" />
        </el-form-item>
        <el-form-item label="所属学校ID" prop="schoolId">
          <el-input v-model="form.schoolId" placeholder="请输入所属学校ID" />
        </el-form-item>
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="form.studentNo" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="入学年份" prop="entryYear">
          <el-input v-model="form.entryYear" placeholder="请输入入学年份" />
        </el-form-item>
        <el-form-item label="班级编号" prop="classCode">
          <el-input v-model="form.classCode" placeholder="请输入班级编号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 学生导入对话框 -->
<el-dialog :title="upload.title" v-model="upload.open" width="400px" append-to-body>
  <el-upload
    ref="uploadRef"
    :limit="1"
    accept=".xlsx, .xls"
    :headers="upload.headers"
    :action="upload.url"
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
      <el-button @click="upload.open = false">取 消</el-button>
    </div>
  </template>
</el-dialog>
  </div>
</template>

<script setup name="Student">
import { listStudent, getStudent, delStudent, addStudent, updateStudent } from "@/api/business/student"
import { getToken } from "@/utils/auth"; // 在顶部import区域添加这一行
const { proxy } = getCurrentInstance()

const studentList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userId: null,
    schoolId: null,
    studentNo: null,
    entryYear: null,
    classCode: null
  },
  rules: {
    userId: [
      { required: true, message: "关联的用户ID (sys_user.user_id)不能为空", trigger: "blur" }
    ],
    schoolId: [
      { required: true, message: "所属学校ID不能为空", trigger: "blur" }
    ],
  }
})
/*** 学生导入参数 */
const upload = reactive({
  // 是否显示弹出层（学生导入）
  open: false,
  // 弹出层标题（学生导入）
  title: "",
  // 是否禁用上传
  isUploading: false,
  // 设置上传的请求头部
  headers: { Authorization: "Bearer " + getToken() },
  // 上传的地址
  url: import.meta.env.VITE_APP_BASE_API + "/business/student/importData"
});
const { queryParams, form, rules } = toRefs(data)

/** 查询学生管理列表 */
function getList() {
  loading.value = true
  listStudent(queryParams.value).then(response => {
    studentList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

// 取消按钮
function cancel() {
  open.value = false
  reset()
}

// 表单重置
function reset() {
  form.value = {
    studentId: null,
    userId: null,
    schoolId: null,
    studentNo: null,
    entryYear: null,
    classCode: null
  }
  proxy.resetForm("studentRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.studentId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加学生管理"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _studentId = row.studentId || ids.value
  getStudent(_studentId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改学生管理"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["studentRef"].validate(valid => {
    if (valid) {
      if (form.value.studentId != null) {
        updateStudent(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addStudent(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _studentIds = row.studentId || ids.value
  proxy.$modal.confirm('是否确认删除学生管理编号为"' + _studentIds + '"的数据项？').then(function() {
    return delStudent(_studentIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('business/student/export', {
    ...queryParams.value
  }, `student_${new Date().getTime()}.xlsx`)
}
/** 导入按钮操作 */
function handleImport() {
  upload.title = "学生导入";
  upload.open = true;
};

/** 下载模板操作 */
function importTemplate() {
  proxy.download("business/student/importTemplate", {
  }, `student_template_${new Date().getTime()}.xlsx`);
};

/**文件上传中处理 */
const handleFileUploadProgress = (event, file, fileList) => {
  upload.isUploading = true;
};

/** 文件上传成功处理 */
const handleFileSuccess = (response, file, fileList) => {
  upload.open = false;
  upload.isUploading = false;
  proxy.$refs["uploadRef"].handleRemove(file);
  proxy.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.msg + "</div>", "导入结果", { dangerouslyUseHTMLString: true });
  getList();
};

/** 提交上传文件 */
function submitFileForm() {
  proxy.$refs["uploadRef"].submit();
};
getList()
</script>
