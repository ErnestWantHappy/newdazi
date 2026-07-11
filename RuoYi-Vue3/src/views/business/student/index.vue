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
      <el-form-item label="账号状态" prop="lockStatus">
         <el-select v-model="queryParams.lockStatus" placeholder="全部" clearable style="width: 120px">
           <el-option label="全部" value="" />
           <el-option label="正常" value="normal" />
           <el-option label="锁定" value="locked" />
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
          @click="openBatchDelete"
          v-hasPermi="['business:student:remove']"
        >综合批量删除</el-button>
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
    <el-table v-loading="loading" :data="filteredStudentList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="登录账号" align="center" prop="userName" />
      <el-table-column label="学生姓名" align="center" prop="studentName" />
      <el-table-column label="班级" align="center" prop="classCode">
        <template #default="scope">
          <span>{{ scope.row.classCode }}班</span>
        </template>
      </el-table-column>
      <el-table-column label="学号" align="center" prop="studentNo" />
      <el-table-column label="入学年份" align="center" prop="entryYear" />
      <el-table-column label="状态" align="center" width="80">
        <template #default="scope">
          <span v-if="lockStatusMap[scope.row.userName]" style="color: #F56C6C; font-weight: 500;">锁定</span>
          <span v-else>正常</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" min-width="100" show-overflow-tooltip>
        <template #default="scope">
          <el-input
            v-if="editingRemarkId === scope.row.studentId"
            v-model="scope.row.remark"
            size="small"
            @blur="saveRemark(scope.row)"
            @keyup.enter="saveRemark(scope.row)"
            ref="remarkInputRef"
            style="width: 100%"
          />
          <span 
            v-else 
            @click="startEditRemark(scope.row)"
            style="cursor: pointer; display: inline-block; min-width: 50px; color: #909399;"
          >
            {{ scope.row.remark || '-' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="280" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link :type="lockStatusMap[scope.row.userName] ? 'warning' : 'primary'" icon="Key" @click="handleResetPwd(scope.row)">
            {{ lockStatusMap[scope.row.userName] ? '重置密码并解锁' : '重置密码' }}
          </el-button>
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
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="如：转班、转校、休学等" />
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

    <!-- 综合批量删除对话框 -->
    <el-dialog title="批量删除设置" v-model="deleteDialog.open" width="550px" append-to-body>
      <div style="margin-bottom: 20px;">
        <el-radio-group v-model="deleteDialog.mode" style="display: flex; flex-direction: column; align-items: flex-start; gap: 20px;">
          <!-- 模式一：删除勾选 -->
          <el-radio value="selected" :disabled="!ids.length" style="height: auto; white-space: normal;">
            <div style="font-size: 15px; font-weight: bold; margin-bottom: 5px;">删除表格已勾选项</div>
            <div style="color: #909399; font-size: 13px;">
              <span v-if="ids.length">当前已勾选 <b>{{ ids.length }}</b> 名学生。系统将仅删除当前页面所勾选的这些数据。</span>
              <span v-else>当前未勾选任何学生。</span>
            </div>
          </el-radio>
          <!-- 模式二：按班级删除 -->
          <el-radio value="byClass" style="height: auto; white-space: normal; padding-top: 10px; border-top: 1px dashed #eee; width: 100%;">
            <div style="font-size: 15px; font-weight: bold; margin-bottom: 15px;">按班级整班删除（跨页全删）</div>
            <div v-show="deleteDialog.mode === 'byClass'" style="padding-left: 24px;">
              <el-form label-width="80px">
                <el-form-item label="入学年份">
                  <el-select v-model="deleteDialog.entryYear" placeholder="请选择年份" style="width: 200px">
                    <el-option v-for="year in entryYearOptions" :key="year" :label="year" :value="year" />
                  </el-select>
                </el-form-item>
                <el-form-item label="班级">
                  <el-select v-model="deleteDialog.classCode" placeholder="请选择班级" style="width: 200px">
                    <el-option v-for="n in 15" :key="n" :label="`${n}班`" :value="String(n)" />
                  </el-select>
                </el-form-item>
              </el-form>
              <div style="color: #F56C6C; font-size: 13px; margin-top: 10px; line-height: 1.5;">
                <el-icon style="vertical-align: middle; margin-right: 4px;"><warning /></el-icon>
                <span style="vertical-align: middle;">警告：此操作将彻底清空该班级下<b>全部有效学生账号</b>及其关联数据，无需逐页勾选，不可恢复！</span>
              </div>
            </div>
          </el-radio>
        </el-radio-group>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="danger" @click="submitBatchDelete">确 认 删 除</el-button>
          <el-button @click="deleteDialog.open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Student">
import { getCurrentInstance, reactive, ref, toRefs, watch, onMounted, computed } from "vue";
import { useRoute } from "vue-router";
import useUserStore from "@/store/modules/user";
import { listStudent, getStudent, delStudent, addStudent, updateStudent, resetStudentPwd, getLockStatus, delStudentByClass } from "@/api/business/student";
import {
  handleSessionExpired,
  isSessionExpiredCode,
  isSessionExpiredError,
  refreshAuthorizationHeader
} from "@/utils/session";

const route = useRoute();
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
const lockStatusMap = ref({});
const editingRemarkId = ref(null); // 当前正在编辑备注的学生ID

const deleteDialog = reactive({
  open: false,
  mode: 'selected',
  entryYear: null,
  classCode: null
});

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
    lockStatus: null,
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

// 根据锁定状态筛选的学生列表
const filteredStudentList = computed(() => {
  if (!queryParams.value.lockStatus) {
    return studentList.value;
  }
  return studentList.value.filter(s => {
    const isLocked = lockStatusMap.value[s.userName] === true;
    if (queryParams.value.lockStatus === 'locked') {
      return isLocked;
    } else if (queryParams.value.lockStatus === 'normal') {
      return !isLocked;
    }
    return true;
  });
});

// ... upload 参数省略，保持不变 ...
const upload = reactive({
  open: false,
  title: "",
  isUploading: false,
  headers: refreshAuthorizationHeader(),
  url: import.meta.env.VITE_APP_BASE_API + "/business/student/importData"
});

function refreshUploadHeaders() {
  upload.headers = refreshAuthorizationHeader(upload.headers);
}
/** 查询学生管理列表 */
function getList() {
  loading.value = true;
  queryParams.value.deptId = userStore.currentDeptId || null;
  listStudent(queryParams.value).then(response => {
    studentList.value = response.rows;
    total.value = response.total;
    loading.value = false;
    // 加载锁定状态
    loadLockStatus();
  });
}

/** 加载学生锁定状态 */
function loadLockStatus() {
  if (!studentList.value || studentList.value.length === 0) {
    lockStatusMap.value = {};
    return;
  }
  const userNames = studentList.value.map(s => s.userName).filter(u => u);
  if (userNames.length === 0) return;
  
  getLockStatus(userNames).then(res => {
    lockStatusMap.value = res.data || {};
  }).catch(() => {
    lockStatusMap.value = {};
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
    remark: null,
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

/** 综合批量删除弹出 */
function openBatchDelete() {
  deleteDialog.mode = ids.value.length > 0 ? 'selected' : 'byClass';
  deleteDialog.entryYear = queryParams.value.entryYear || null;
  deleteDialog.classCode = queryParams.value.classCode || null;
  deleteDialog.open = true;
}

/** 提交综合批量删除 */
function submitBatchDelete() {
  if (deleteDialog.mode === 'selected') {
    if (!ids.value.length) return;
    const studentNames = studentList.value.filter(item => ids.value.includes(item.studentId)).map(item => item.studentName).join(',');
    const confirmMsg = studentNames 
      ? '确认删除已选学生（如：' + studentNames.split(',').slice(0,3).join(',') + ' 等）吗？' 
      : '确认删除已选名学生吗？';
    
    proxy.$modal.confirm(confirmMsg).then(function() {
      return delStudent(ids.value);
    }).then(() => {
      deleteDialog.open = false;
      getList();
      proxy.$modal.msgSuccess("删除成功");
    }).catch(() => {});
  } else if (deleteDialog.mode === 'byClass') {
    if (!deleteDialog.entryYear || !deleteDialog.classCode) {
      proxy.$modal.msgError("请选择要删除的入学年份和班级！");
      return;
    }
    proxy.$modal.confirm('此严重警告操作！确定要彻底清空 ' + deleteDialog.entryYear + '级 ' + deleteDialog.classCode + '班 的所有学生吗？').then(function() {
      return delStudentByClass({
        entryYear: deleteDialog.entryYear,
        classCode: deleteDialog.classCode,
        deptId: userStore.currentDeptId
      });
    }).then((res) => {
      deleteDialog.open = false;
      getList();
      proxy.$modal.msgSuccess(res.msg || "班级数据已清空");
    }).catch(() => {});
  }
}

/** 开始编辑备注 */
function startEditRemark(row) {
  editingRemarkId.value = row.studentId;
  // 延迟聚焦
  setTimeout(() => {
    const input = document.querySelector('.el-table .el-input__inner');
    if (input) input.focus();
  }, 50);
}

/** 保存备注 */
function saveRemark(row) {
  editingRemarkId.value = null;
  if (row.studentId) {
    updateStudent({ studentId: row.studentId, remark: row.remark || '' }).catch(() => {
      proxy.$modal.msgError("备注保存失败");
    });
  }
}

/** 重置密码按钮操作 */
function handleResetPwd(row) {
  const uIds = row.userId ? [row.userId] : userIds.value;
  const studentNames = row.studentName || studentList.value.filter(item => userIds.value.includes(item.userId)).map(item => item.studentName).join(',');
  proxy.$modal.confirm('确认要重置学生"' + studentNames + '"的密码为“123456”吗？').then(function () {
    return resetStudentPwd(uIds);
  }).then(() => {
    proxy.$modal.msgSuccess("重置成功");
    // 刷新锁定状态
    loadLockStatus();
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
  refreshUploadHeaders();
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
  if (isSessionExpiredCode(response?.code)) {
    upload.isUploading = false;
    proxy.$refs["uploadRef"].clearFiles();
    if (uploadLoadingInstance) {
      uploadLoadingInstance.close();
    }
    handleSessionExpired(response?.msg);
    return;
  }
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
  if (isSessionExpiredError(err)) {
    handleSessionExpired();
    return;
  }
  proxy.$modal.msgError("上传失败");
};

/** 提交上传文件 */
function submitFileForm() {
  refreshUploadHeaders();
  proxy.$refs["uploadRef"].submit();
};

// 页面加载时读取URL参数，自动设置筛选条件
onMounted(() => {
  const urlEntryYear = route.query.entryYear;
  const urlClassCode = route.query.classCode;
  
  if (urlEntryYear) {
    queryParams.value.entryYear = urlEntryYear;
  }
  if (urlClassCode) {
    queryParams.value.classCode = urlClassCode;
  }
  
  getList();
});

watch(() => userStore.currentDeptId, (deptId) => {
  queryParams.value.deptId = deptId || null;
  handleQuery();
});
</script>
