<template>
  <el-dialog
    v-model="visible"
    title="📋 课堂表现管理"
    width="900px"
    :close-on-click-modal="false"
    @open="loadData"
  >
    <div class="performance-container">
      <!-- 说明 -->
      <el-alert type="info" :closable="false" style="margin-bottom: 15px">
        <template #title>
          平时分范围 <b>-10 ~ +10</b>，正数为加分，负数为扣分。总分 = min(考试分 + 平时分, 100)
        </template>
      </el-alert>

      <!-- 学生表格 -->
      <el-table 
        :data="studentList" 
        v-loading="loading" 
        border 
        stripe
        max-height="450"
        :row-class-name="tableRowClassName"
      >
        <el-table-column prop="studentNo" label="学号" width="80" align="center" />
        <el-table-column prop="studentName" label="姓名" width="100" align="center" />
        <el-table-column label="平时分" width="150" align="center">
          <template #default="scope">
            <el-input-number 
              v-model="scope.row.score" 
              :min="-10" 
              :max="10" 
              :precision="0"
              size="small"
              controls-position="right"
              @change="onScoreChange(scope.row)"
              style="width: 100px"
            />
          </template>
        </el-table-column>
        <el-table-column label="原因" min-width="200">
          <template #default="scope">
            <el-input 
              v-model="scope.row.reason" 
              placeholder="加分/扣分原因"
              size="small"
              clearable
              @change="onReasonChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.modified" type="warning" size="small">未保存</el-tag>
            <el-tag v-else-if="scope.row.performanceId" type="success" size="small">已保存</el-tag>
            <el-tag v-else type="info" size="small">-</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && studentList.length === 0" description="暂无学生数据" />
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleSave" :loading="saving">
        保存全部
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { getPerformanceList, batchSavePerformance } from '@/api/business/classroomPerformance'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: Boolean,
  lessonId: Number,
  classCode: String,
  entryYear: String
})

const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const saving = ref(false)
const studentList = ref([])

function loadData() {
  if (!props.lessonId || !props.classCode || !props.entryYear) {
    studentList.value = []
    return
  }
  
  loading.value = true
  getPerformanceList({ lessonId: props.lessonId, classCode: props.classCode, entryYear: props.entryYear })
    .then(res => {
      studentList.value = (res.data || []).map(item => ({
        ...item,
        score: item.score || 0,
        reason: item.reason || '',
        modified: false
      }))
    })
    .finally(() => {
      loading.value = false
    })
}

function onScoreChange(row) {
  row.modified = true
}

function onReasonChange(row) {
  row.modified = true
}

function tableRowClassName({ row }) {
  if (row.score > 0) return 'positive-score'
  if (row.score < 0) return 'negative-score'
  return ''
}

function handleSave() {
  if (studentList.value.length === 0) return
  
  saving.value = true
  const performances = studentList.value.map(item => ({
    studentId: item.studentId,
    score: item.score,
    reason: item.reason
  }))
  
  batchSavePerformance({
    lessonId: props.lessonId,
    performances
  }).then(res => {
    ElMessage.success(res.msg || '保存成功')
    studentList.value.forEach(item => {
      item.modified = false
      item.performanceId = true
    })
    emit('saved')
  }).finally(() => {
    saving.value = false
  })
}

// 监听参数变化
watch(() => [props.lessonId, props.classCode, props.entryYear], () => {
  if (visible.value) {
    loadData()
  }
})
</script>

<style lang="scss" scoped>
.performance-container {
  min-height: 300px;
}

:deep(.positive-score) {
  background-color: #f0f9eb !important;
}

:deep(.negative-score) {
  background-color: #fef0f0 !important;
}
</style>
