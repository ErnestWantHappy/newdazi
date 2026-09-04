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
            <el-option v-for="year in entryYearOptions" :key="year" :label="formatEntryYear(year)" :value="year" />
         </el-select>
      </el-form-item>
      <el-form-item label="班级" prop="classCode">
         <el-select v-model="queryParams.classCode" placeholder="请选择班级" clearable style="width: 200px">
           <el-option v-for="n in 99" :key="n" :label="`${String(n).padStart(2, '0')}班`" :value="String(n)" />
         </el-select>
      </el-form-item>
      <el-form-item label="使用状态" prop="status">
         <el-select v-model="queryParams.status" placeholder="正常" style="width: 120px">
           <el-option label="正常使用" value="0" />
           <el-option label="已停用" value="1" />
           <el-option label="全部" value="all" />
         </el-select>
      </el-form-item>
      <el-form-item label="登录锁定" prop="lockStatus">
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
        <el-button type="primary" plain icon="EditPen" @click="openCorrection" v-hasPermi="['business:student:import']">
          批量纠错
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="CircleClose" :disabled="multiple" @click="handleBatchStatus('1')" v-hasPermi="['business:student:edit']">
          批量停用
        </el-button>
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
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['business:student:export']"
        >导出</el-button>
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
      <el-table-column label="入学年份" align="center" prop="entryYear">
        <template #default="scope">{{ formatEntryYear(scope.row.entryYear) }}</template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.status === '1'" type="info">已停用</el-tag>
          <el-tag v-else-if="lockStatusMap[scope.row.userName]" type="danger">锁定</el-tag>
          <el-tag v-else type="success">正常</el-tag>
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
      <el-table-column label="操作" align="center" width="350" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link :type="lockStatusMap[scope.row.userName] ? 'warning' : 'primary'" icon="Key" @click="handleResetPwd(scope.row)">
            {{ lockStatusMap[scope.row.userName] ? '重置密码并解锁' : '重置密码' }}
          </el-button>
          <el-button link :type="scope.row.status === '1' ? 'success' : 'warning'" @click="handleRowStatus(scope.row)">
            {{ scope.row.status === '1' ? '恢复' : '停用' }}
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
              <el-option v-for="year in entryYearOptions" :key="year" :label="formatEntryYear(year)" :value="year" />
           </el-select>
        </el-form-item>
        <el-form-item label="班级编号" prop="classCode">
           <el-select v-model="form.classCode" placeholder="请选择班级编号" style="width:100%">
             <el-option v-for="n in 99" :key="n" :label="`${String(n).padStart(2, '0')}班`" :value="String(n)" />
           </el-select>
        </el-form-item>
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="form.studentNo" placeholder="请输入本班学号，例如 01" maxlength="2" />
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
      <el-alert
        class="student-import-alert"
        style="margin-bottom: 16px"
        type="warning"
        :closable="false"
        show-icon
      >
        <template #title><b>班级编号只填 01～99，不要填写年级号</b></template>
        <div>正确：学号 01、入学年份 2025、班级编号 01；错误：班级编号 601、602。</div>
      </el-alert>
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
        :before-upload="validateStudentImportFile"
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

    <el-dialog title="批量纠错学生信息" v-model="correction.open" width="1050px" append-to-body>
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 14px">
        <template #title>先下载当前学生纠错表，只修改姓名、年份、班级、学号和备注。</template>
        不要修改“学生永久编号”和“原登录账号”。确认后 studentId 不变，原来的答题、成绩和作品都会保留。
      </el-alert>
      <div style="display:flex;gap:10px;align-items:flex-start;margin-bottom:14px">
        <el-button type="primary" plain icon="Download" @click="downloadCorrectionTemplate">下载当前纠错表</el-button>
        <el-upload
          ref="correctionUploadRef"
          :auto-upload="false"
          :limit="1"
          accept=".xlsx,.xls"
          :on-change="handleCorrectionFile"
          :on-remove="clearCorrection"
        >
          <el-button type="primary" plain icon="Upload">选择修改后的 Excel</el-button>
        </el-upload>
        <el-button type="primary" :loading="correction.previewing" :disabled="!correction.file" @click="previewCorrection">校验并预览</el-button>
      </div>
      <div v-if="correction.result" style="margin-bottom:10px">
        共 {{ correction.result.totalCount }} 条；可执行 {{ correction.result.validCount }} 条；有变化
        {{ correction.result.changedCount }} 条；错误 {{ correction.result.invalidCount }} 条。
      </div>
      <el-table v-if="correction.result" :data="correction.result.rows" max-height="430" border size="small">
        <el-table-column label="行" prop="rowNumber" width="55" />
        <el-table-column label="永久编号" prop="studentId" width="100" />
        <el-table-column label="原账号" prop="currentUserName" min-width="145" />
        <el-table-column label="新账号" prop="targetUserName" min-width="145" />
        <el-table-column label="姓名" prop="studentName" width="100" />
        <el-table-column label="新班级" width="120">
          <template #default="scope">{{ scope.row.entryYear }}级 {{ scope.row.classCode }}班</template>
        </el-table-column>
        <el-table-column label="新学号" prop="studentNo" width="75" />
        <el-table-column label="结果" min-width="220">
          <template #default="scope">
            <el-tag :type="scope.row.valid ? (scope.row.changed ? 'warning' : 'info') : 'danger'">
              {{ scope.row.message }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="correction.open = false">取消</el-button>
        <el-button
          type="primary"
          :loading="correction.applying"
          :disabled="!correction.result || correction.result.invalidCount > 0 || correction.result.changedCount === 0"
          @click="applyCorrection"
        >确认纠错</el-button>
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
                    <el-option v-for="year in entryYearOptions" :key="year" :label="formatEntryYear(year)" :value="year" />
                  </el-select>
                </el-form-item>
                <el-form-item label="班级">
                  <el-select v-model="deleteDialog.classCode" placeholder="请选择班级" style="width: 200px">
                    <el-option v-for="n in 99" :key="n" :label="`${String(n).padStart(2, '0')}班`" :value="String(n)" />
                  </el-select>
                </el-form-item>
              </el-form>
              <div style="color: #F56C6C; font-size: 13px; margin-top: 10px; line-height: 1.5;">
                <el-icon style="vertical-align: middle; margin-right: 4px;"><warning /></el-icon>
                <span style="vertical-align: middle;">只会彻底删除<b>没有任何业务记录</b>的学生；有答题、成绩或其他记录的学生会被系统拦住，请改用“停用”。</span>
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
import * as XLSX from "xlsx";
import { calculateYearsInSection } from "@/utils/academicYear";
import {
  listStudent, getStudent, delStudent, addStudent, updateStudent, resetStudentPwd, getLockStatus,
  delStudentByClass, previewStudentCorrection, applyStudentCorrection, changeStudentStatus
} from "@/api/business/student";
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

const correction = reactive({
  open: false,
  file: null,
  result: null,
  previewing: false,
  applying: false
});

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

// 当前校区的学部决定同一个入学年份对应的年级；切换校区后自动重新计算。
const currentSchoolType = computed(() => {
  const currentSchool = (userStore.schools || []).find(
    school => Number(school.deptId) === Number(userStore.currentDeptId)
  );
  if (currentSchool?.schoolType) return String(currentSchool.schoolType);
  const deptName = String(currentSchool?.deptName || "");
  if (deptName.includes("初中")) return "2";
  if (deptName.includes("高中")) return "3";
  return "1";
});

function formatEntryYear(year) {
  if (year == null || year === "") return "-";
  const grade = calculateYearsInSection(year);
  const schoolType = currentSchoolType.value;
  if (grade == null) return String(year);
  if (schoolType === "1" && grade >= 1 && grade <= 6) return `${year}（${grade}年级）`;
  if (schoolType === "2" && grade >= 1 && grade <= 3) return `${year}（初${grade}）`;
  if (schoolType === "3" && grade >= 1 && grade <= 3) return `${year}（高${grade}）`;
  return String(year);
}

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    studentName: null,
    entryYear: null,
    classCode: null,
    status: "0",
    lockStatus: null,
    deptId: userStore.currentDeptId || null,
  },
  rules: {
    studentName: [ { required: true, message: "学生姓名不能为空", trigger: "blur" } ],
    entryYear: [ { required: true, message: "入学年份不能为空", trigger: "change" } ],
    classCode: [ { required: true, message: "班级编号不能为空", trigger: "change" } ],
    studentNo: [
      { required: true, message: "学号不能为空", trigger: "blur" },
      { pattern: /^(0?[1-9]|[1-9]\d)$/, message: "学号只能填写 01～99", trigger: "blur" }
    ],
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

function openCorrection() {
  correction.open = true;
  correction.file = null;
  correction.result = null;
}

function downloadCorrectionTemplate() {
  proxy.download('business/student/correctionTemplate', {
    studentName: queryParams.value.studentName,
    entryYear: queryParams.value.entryYear,
    classCode: queryParams.value.classCode,
    deptId: userStore.currentDeptId || null
  }, `student_correction_${new Date().getTime()}.xlsx`)
}

function handleCorrectionFile(uploadFile) {
  correction.file = uploadFile.raw;
  correction.result = null;
}

function clearCorrection() {
  correction.file = null;
  correction.result = null;
}

async function previewCorrection() {
  if (!correction.file) return;
  correction.previewing = true;
  try {
    const response = await previewStudentCorrection(correction.file);
    correction.result = response.data;
  } finally {
    correction.previewing = false;
  }
}

function applyCorrection() {
  if (!correction.result || correction.result.invalidCount > 0) return;
  proxy.$modal.confirm(`确认原地纠错 ${correction.result.changedCount} 名学生吗？学生永久编号和历史成绩不会改变。`).then(async () => {
    correction.applying = true;
    try {
      const response = await applyStudentCorrection(correction.result.rows);
      proxy.$modal.msgSuccess(response.msg || "纠错完成");
      correction.open = false;
      getList();
    } finally {
      correction.applying = false;
    }
  }).catch(() => {});
}

function handleBatchStatus(status) {
  if (!ids.value.length) return;
  const verb = status === '1' ? '停用' : '恢复';
  proxy.$modal.confirm(`确认${verb}已选的 ${ids.value.length} 名学生吗？停用不会删除历史数据。`).then(() => {
    return changeStudentStatus(ids.value, status);
  }).then(response => {
    proxy.$modal.msgSuccess(response.msg || `${verb}成功`);
    getList();
  }).catch(() => {});
}

function handleRowStatus(row) {
  const status = row.status === '1' ? '0' : '1';
  const verb = status === '1' ? '停用' : '恢复';
  proxy.$modal.confirm(`确认${verb}学生“${row.studentName}”吗？${status === '1' ? '停用后不能登录，但历史数据仍保留。' : ''}`).then(() => {
    return changeStudentStatus([row.studentId], status);
  }).then(response => {
    proxy.$modal.msgSuccess(response.msg || `${verb}成功`);
    getList();
  }).catch(() => {});
}

/** 下载模板操作 */
function importTemplate() {
  const rows = [
    ["学号", "入学年份", "班级编号", "真实姓名", "备注"],
    ["01", "2025", "01", "示例学生一", "示例行，导入前请替换"],
    ["02", "2025", "02", "示例学生二", "班号只填 01～99，不要写 601、602"]
  ];
  const worksheet = XLSX.utils.aoa_to_sheet(rows);
  worksheet["!cols"] = [{ wch: 10 }, { wch: 12 }, { wch: 14 }, { wch: 18 }, { wch: 38 }];
  ["A2", "A3", "C2", "C3"].forEach(cell => {
    if (worksheet[cell]) worksheet[cell].z = "@";
  });
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, "学生导入数据");
  XLSX.writeFile(workbook, `student_template_${new Date().getTime()}.xlsx`);
};

/**
 * 上传前先在浏览器检查班号。这样教师能立即看到具体行号，避免把 601、602
 * 这类“年级 + 班号”误写法提交到服务器后才发现整批失败。
 */
async function validateStudentImportFile(rawFile) {
  try {
    const workbook = XLSX.read(await rawFile.arrayBuffer(), { type: "array", raw: false });
    const sheet = workbook.Sheets[workbook.SheetNames[0]];
    const rows = XLSX.utils.sheet_to_json(sheet, { header: 1, raw: false, defval: "" });
    const invalidRows = [];
    rows.slice(1).forEach((row, index) => {
      if (row.every(value => String(value).trim() === "")) return;
      const classCode = String(row[2] ?? "").trim();
      const classNumber = Number(classCode);
      if (!/^\d{1,2}$/.test(classCode) || classNumber < 1 || classNumber > 99) invalidRows.push(index + 2);
    });
    if (invalidRows.length) {
      proxy.$modal.msgError(`第 ${invalidRows.slice(0, 5).join("、")} 行班级编号无效：只填 01～99，不要写 601、602 等带年级的三位数。`);
      return false;
    }
    return true;
  } catch (error) {
    proxy.$modal.msgError("无法读取 Excel，请重新下载平台模板后填写。");
    return false;
  }
}

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
  const metrics = response?.data || {};
  const hasMetrics = Number.isFinite(Number(metrics.totalCount));
  const summary = hasMetrics
    ? `<div style="padding:10px 12px;margin-bottom:8px;background:#f5f7fa;border-radius:4px;">共 ${metrics.totalCount} 条，成功 ${metrics.successCount} 条，失败 ${metrics.failureCount} 条；总耗时 ${metrics.totalDurationMs} ms</div>`
    : "";
  proxy.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + summary + response.msg + "</div>", "导入结果", { dangerouslyUseHTMLString: true });
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
