<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="课程名称" prop="lessonName">
        <el-input
          v-model="queryParams.lessonName"
          placeholder="请输入课程/主题名称"
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
          v-hasPermi="['business:lesson:add']"
        >新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="lessonList">
      <el-table-column label="课程ID" align="center" prop="lessonId" />
      <el-table-column label="课程/主题名称" align="center" prop="lessonName" />
      <el-table-column label="类型" align="center" prop="lessonType">
         <template #default="scope">
          <dict-tag :options="lesson_type" :value="scope.row.lessonType"/>
        </template>
      </el-table-column>
      <el-table-column label="年级" align="center" prop="gradeLevel">
        <template #default="scope">
          <span>{{ scope.row.gradeLevel }}年级</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
         <template #default="scope">
          <dict-tag :options="sys_normal_disable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:lesson:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:lesson:remove']">删除</el-button>
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

    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="lessonRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="课程名称" prop="lessonName">
          <el-input v-model="form.lessonName" placeholder="请输入课程/主题名称" />
        </el-form-item>

        <el-form-item label="课程类型" prop="lessonType">
          <el-radio-group v-model="form.lessonType">
            <el-radio label="daily">日常</el-radio>
            <el-radio label="exam">考试</el-radio>
            </el-radio-group>
        </el-form-item>

        <el-form-item label="所属年级" prop="gradeLevel">
           <el-select v-model="form.gradeLevel" placeholder="请选择年级">
              <el-option
                 v-for="grade in gradeOptions"
                 :key="grade.value"
                 :label="grade.label"
                 :value="grade.value"
              ></el-option>
           </el-select>
        </el-form-item>

        <el-form-item label="适用班级" prop="classIds">
          <el-input v-model="form.classIds" placeholder="请输入适用班级ID,多个用逗号隔开" />
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Lesson">
import { listLesson, getLesson, delLesson, addLesson, updateLesson } from "@/api/business/lesson";

const { proxy } = getCurrentInstance();
// 从字典中获取课程类型和状态的选项
const { lesson_type } = proxy.useDict("lesson_type");
const { sys_normal_disable } = proxy.useDict("sys_normal_disable");

const lessonList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

// 年级选项
const gradeOptions = ref([
  { value: 1, label: '一年级'},
  { value: 2, label: '二年级'},
  { value: 3, label: '三年级'},
  { value: 4, label: '四年级'},
  { value: 5, label: '五年级'},
  { value: 6, label: '六年级'},
  { value: 7, label: '七年级'},
  { value: 8, 'label': '八年级'},
  { value: 9, 'label': '九年级'},
]);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    lessonName: null,
  },
  rules: {
    lessonName: [
      { required: true, message: "课程/主题名称不能为空", trigger: "blur" }
    ],
    lessonType: [
      { required: true, message: "课程类型不能为空", trigger: "change" }
    ],
    gradeLevel: [
      { required: true, message: "所属年级不能为空", trigger: "change" }
    ],
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询课程/主题列表 */
function getList() {
  loading.value = true;
  listLesson(queryParams.value).then(response => {
    lessonList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

// 表单重置
function reset() {
  form.value = {
    lessonId: null,
    lessonName: null,
    lessonType: "daily", // 默认选中"日常"
    gradeLevel: null,
    classIds: null,
    status: "0",
    remark: null
  };
  proxy.resetForm("lessonRef");
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

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加课程/主题";
}

/** 修改按钮操作 */
async function handleUpdate(row) {
  reset();
  const lessonId = row.lessonId || ids.value[0];
  const response = await getLesson(lessonId);
  form.value = response.data;
  open.value = true;
  title.value = "修改课程/主题";
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["lessonRef"].validate(valid => {
    if (valid) {
      if (form.value.lessonId != null) {
        updateLesson(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addLesson(form.value).then(response => {
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
  // ... 省略，可按需实现
}

getList();
</script>