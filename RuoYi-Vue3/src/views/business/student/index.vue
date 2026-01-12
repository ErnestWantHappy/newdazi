<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="学生姓名" prop="studentName">
        <el-input
          v-model="queryParams.studentName"
          placeholder="请输入学生姓名"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="入学年份" prop="entryYear">
         <el-select v-model="queryParams.entryYear" placeholder="请选择年份" clearable style="width: 200px">
            <el-option v-for="year in entryYearOptions" :key="year" :label="year" :value="year" />
         </el-select>
      </el-form-item>
      <el-form-item label="班级" prop="classCode">
         <el-select v-model="queryParams.classCode" placeholder="请选择班级" clearable style="width: 200px">
           <el-option v-for="n in 15" :key="n" :label="`${n}班`" :value="String(n)" />
         </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮区域 -->
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
        >批量导入</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['business:student:remove']"
        >批量删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Key"
          :disabled="multiple"
          @click="handleResetPwd"
          v-hasPermi="['business:student:edit']"
        >批量重置密码</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="studentList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="学生姓名" align="center" prop="studentName" />
      <el-table-column label="登录账号" align="center" prop="userName" />
      <el-table-column label="班级" align="center" prop="classCode">
        <template #default="scope">
          <span>{{ scope.row.classCode }}班</span>
        </template>
      </el-table-column>
      <el-table-column label="学号" align="center" prop="studentNo" />
      <el-table-column label="入学年份" align="center" prop="entryYear" />
      <el-table-column label="操作" align="center" width="280" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="primary" icon="Key" @click="handleResetPwd(scope.row)">重置密码</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
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

    <!-- 添加或修改学生对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="studentRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="学生姓名" prop="studentName">
          <el-input v-model="form.studentName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="入学年份" prop="entryYear">
           <el-select v-model="form.entryYear" placeholder="请选择入学年份" style="width:100%">
              <el-option v-for="year in entryYearOptions" :key="year" :label="year" :value="year" />
           </el-select>
        </el-form-item>
        <el-form-item label="班级编号" prop="classCode">
           <el-select v-model="form.classCode" placeholder="请选择班级编号" style="width:100%">
             <el-option v-for="n in 15" :key="n" :label="`${n}班`" :value="String(n)" />
           </el-select>
        </el-form-item>
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="form.studentNo" placeholder="请输入学生在本班的学号(1-99)" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

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
        :on-error="handleFileError"
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
import { getCurrentInstance, reactive, ref, toRefs, watch } from "vue";
import useUserStore from "@/store/modules/user";
import { listStudent, getStudent, delStudent, addStudent, updateStudent, resetStudentPwd } from "@/api/business/student";
import { getToken } from "@/utils/auth";

const userStore = useUserStore();
const { proxy } = getCurrentInstance();

const studentList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const userIds = ref([]); // 新增：用于存放选中学生的userId
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const entryYearOptions = ref([]);
const currentYear = new Date().getFullYear();
for (let i = 0; i < 10; i++) {
  entryYearOptions.value.push(String(currentYear - i));
}

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    studentName: null,
    entryYear: null,
    classCode: null,
    deptId: userStore.currentDeptId || null,
  },
  rules: {
    studentName: [ { required: true, message: "学生姓名不能为空", trigger: "blur" } ],
    entryYear: [ { required: true, message: "入学年份不能为空", trigger: "change" } ],
    classCode: [ { required: true, message: "班级编号不能为空", trigger: "change" } ],
    studentNo: [ { required: true, message: "学号不能为空", trigger: "blur" } ],
  }
});

const { queryParams, form, rules } = toRefs(data);

// ... upload 参数省略，保持不变 ...
const upload = reactive({
  open: false,
  title: "",
  isUploading: false,
  headers: { Authorization: "Bearer " + getToken() },
  url: import.meta.env.VITE_APP_BASE_API + "/business/student/importData"
});
/** 查询学生管理列表 */
function getList() {
  loading.value = true;
  queryParams.value.deptId = userStore.currentDeptId || null;
  listStudent(queryParams.value).then(response => {
    studentList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

// ... cancel, reset, handleQuery, resetQuery 函数省略，保持不变 ...

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

// 表单重置
function reset() {
  form.value = {
    studentId: null,
    studentName: null,
    entryYear: null,
    classCode: null,
    studentNo: null,
  };
  proxy.resetForm("studentRef");
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
  ids.value = selection.map(item => item.studentId);
  userIds.value = selection.map(item => item.userId); // 关键：同时获取userId
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

// ... handleAdd, handleUpdate, submitForm 函数省略，保持不变 ...
/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "新增学生";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const _studentId = row.studentId || ids.value[0];
  getStudent(_studentId).then(response => {
    form.value = response.data;
    // 后端返回的classCode可能是数字，下拉框需要字符串
    form.value.classCode = String(form.value.classCode); 
    open.value = true;
    title.value = "修改学生信息";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["studentRef"].validate(valid => {
    if (valid) {
      if (form.value.studentId != null) {
        updateStudent(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addStudent(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const studentIds = row.studentId || ids.value;
  const studentNames = row.studentName || studentList.value.filter(item => ids.value.includes(item.studentId)).map(item => item.studentName).join(',');
  proxy.$modal.confirm('是否确认删除学生姓名为"' + studentNames + '"的数据项？').then(function() {
    return delStudent(studentIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 重置密码按钮操作 */
function handleResetPwd(row) {
  const uIds = row.userId ? [row.userId] : userIds.value;
  const studentNames = row.studentName || studentList.value.filter(item => userIds.value.includes(item.userId)).map(item => item.studentName).join(',');
  proxy.$modal.confirm('确认要重置学生"' + studentNames + '"的密码为“123456”吗？').then(function () {
    return resetStudentPwd(uIds);
  }).then(() => {
    proxy.$modal.msgSuccess("重置成功");
  }).catch(() => {});
}

// ... 导入相关函数省略，保持不变 ...

/** 导出按钮操作 */
function handleExport() {
  proxy.download('business/student/export', {
    ...queryParams.value
  }, `student_${new Date().getTime()}.xlsx`)
}

/** 导入按钮操作 */
function handleImport() {
  upload.headers.Authorization = "Bearer " + getToken();
  upload.title = "学生导入";
  upload.open = true;
};

/** 下载模板操作 */
function importTemplate() {
  proxy.download("business/student/importTemplate", { deptId: userStore.currentDeptId || null }, `student_template_${new Date().getTime()}.xlsx`);
};

import { ElLoading } from 'element-plus';

let uploadLoadingInstance;

/** 文件上传中处理 */
const handleFileUploadProgress = (event, file, fileList) => {
  upload.isUploading = true;
  uploadLoadingInstance = ElLoading.service({ text: "正在导入数据，请稍候", background: "rgba(0, 0, 0, 0.7)" });
};

/** 文件上传成功处理 */
const handleFileSuccess = (response, file, fileList) => {
  upload.open = false;
  upload.isUploading = false;
  proxy.$refs["uploadRef"].handleRemove(file);
  if (uploadLoadingInstance) {
    uploadLoadingInstance.close();
  }
  proxy.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.msg + "</div>", "导入结果", { dangerouslyUseHTMLString: true });
  getList();
};

/** 文件上传失败处理 */
const handleFileError = (err, file, fileList) => {
  upload.isUploading = false;
  if (uploadLoadingInstance) {
    uploadLoadingInstance.close();
  }
  proxy.$modal.msgError("上传失败");
};

/** 提交上传文件 */
function submitFileForm() {
  proxy.$refs["uploadRef"].submit();
};

getList();

watch(() => userStore.currentDeptId, (deptId) => {
  queryParams.value.deptId = deptId || null;
  handleQuery();
});
</script>
