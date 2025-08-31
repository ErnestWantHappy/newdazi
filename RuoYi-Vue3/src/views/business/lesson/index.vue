<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="课程/作业标题" prop="lessonTitle">
        <el-input
          v-model="queryParams.lessonTitle"
          placeholder="请输入课程/作业标题"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="年级 (例如: 1, 2, 3, 4, 5, 6)" prop="grade">
        <el-select v-model="queryParams.grade" placeholder="请选择年级 (例如: 1, 2, 3, 4, 5, 6)" clearable>
          <el-option
            v-for="dict in biz_grade"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="学期 (0上册, 1下册)" prop="semester">
        <el-select v-model="queryParams.semester" placeholder="请选择学期 (0上册, 1下册)" clearable>
          <el-option
            v-for="dict in biz_semester"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="第几课 (例如: 1, 2, 3)" prop="lessonNum">
        <el-input
          v-model="queryParams.lessonNum"
          placeholder="请输入第几课 (例如: 1, 2, 3)"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="创建教师ID" prop="creatorId">
        <el-input
          v-model="queryParams.creatorId"
          placeholder="请输入创建教师ID"
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
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['business:lesson:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['business:lesson:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['business:lesson:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="lessonList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="课程ID" align="center" prop="lessonId" />
      <el-table-column label="课程/作业标题" align="center" prop="lessonTitle" />
      <el-table-column label="年级 (例如: 1, 2, 3, 4, 5, 6)" align="center" prop="grade">
        <template #default="scope">
          <dict-tag :options="biz_grade" :value="scope.row.grade"/>
        </template>
      </el-table-column>
      <el-table-column label="学期 (0上册, 1下册)" align="center" prop="semester">
        <template #default="scope">
          <dict-tag :options="biz_semester" :value="scope.row.semester"/>
        </template>
      </el-table-column>
      <el-table-column label="第几课 (例如: 1, 2, 3)" align="center" prop="lessonNum" />
      <el-table-column label="创建教师ID" align="center" prop="creatorId" />
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

    <!-- 添加或修改课程/作业信息对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="lessonRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="课程/作业标题" prop="lessonTitle">
          <el-input v-model="form.lessonTitle" placeholder="请输入课程/作业标题" />
        </el-form-item>
        <el-form-item label="年级 (例如: 1, 2, 3, 4, 5, 6)" prop="grade">
          <el-select v-model="form.grade" placeholder="请选择年级 (例如: 1, 2, 3, 4, 5, 6)">
            <el-option
              v-for="dict in biz_grade"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="学期 (0上册, 1下册)" prop="semester">
          <el-select v-model="form.semester" placeholder="请选择学期 (0上册, 1下册)">
            <el-option
              v-for="dict in biz_semester"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="第几课 (例如: 1, 2, 3)" prop="lessonNum">
          <el-input v-model="form.lessonNum" placeholder="请输入第几课 (例如: 1, 2, 3)" />
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
import { listLesson, getLesson, delLesson, addLesson, updateLesson } from "@/api/business/lesson"

const { proxy } = getCurrentInstance()
const { biz_semester, biz_grade } = proxy.useDict('biz_semester', 'biz_grade')
const router = useRouter(); // 新增
const lessonList = ref([])
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
    lessonTitle: null,
    grade: null,
    semester: null,
    lessonNum: null,
    creatorId: null,
  },
  rules: {
    lessonTitle: [
      { required: true, message: "课程/作业标题不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询课程/作业信息列表 */
function getList() {
  loading.value = true
  listLesson(queryParams.value).then(response => {
    lessonList.value = response.rows
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
    lessonId: null,
    lessonTitle: null,
    grade: null,
    semester: null,
    lessonNum: null,
    creatorId: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null
  }
  proxy.resetForm("lessonRef")
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
  ids.value = selection.map(item => item.lessonId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  router.push("/business/lesson-auth/designer");
}

/** 修改按钮操作 */
function handleUpdate(row) {
  const lessonId = row.lessonId || ids.value[0];
  router.push("/business/lesson-auth/designer/" + lessonId);
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["lessonRef"].validate(valid => {
    if (valid) {
      if (form.value.lessonId != null) {
        updateLesson(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addLesson(form.value).then(response => {
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
  const _lessonIds = row.lessonId || ids.value
  proxy.$modal.confirm('是否确认删除课程/作业信息编号为"' + _lessonIds + '"的数据项？').then(function() {
    return delLesson(_lessonIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('business/lesson/export', {
    ...queryParams.value
  }, `lesson_${new Date().getTime()}.xlsx`)
}

getList()
</script>
