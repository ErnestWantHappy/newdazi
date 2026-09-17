<template>
  <div class="student-selector">
    <el-form :inline="true">
      <!-- 学年选择 -->
      <el-form-item label="学年">
        <el-select v-model="currentAcademicYear" placeholder="选择学年" style="width: 180px" @change="onAcademicYearChange">
          <el-option 
            v-for="year in academicYearOptions" 
            :key="year.value" 
            :label="year.label" 
            :value="year.value" 
          />
        </el-select>
      </el-form-item>
      
      <!-- 班级筛选 -->
      <el-form-item label="班级">
        <el-cascader
          v-model="selectedClass"
          :options="classOptions"
          :props="{ expandTrigger: 'hover' }"
          placeholder="筛选班级 (可选)"
          clearable
          style="width: 180px"
          @change="onClassChange"
        />
      </el-form-item>
      
      <!-- 学生选择/搜索 -->
      <el-form-item label="学生">
        <el-select 
          v-model="currentStudentId" 
          filterable 
          :remote="!isClassSelected"
          :reserve-keyword="!isClassSelected"
          :placeholder="isClassSelected ? '请选择学生' : '输入姓名搜索'"
          :remote-method="searchStudents"
          :loading="searchLoading"
          style="width: 200px"
          @change="onStudentChange"
        >
          <el-option 
            v-for="stu in studentOptions" 
            :key="stu.studentId" 
            :label="stu.studentName" 
            :value="stu.studentId" 
          />
        </el-select>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { listStudent } from '@/api/business/student'
import { getClasses, getStudentProfileSummary, getStudentsByClass } from '@/api/business/studentProfile'
import { createAcademicYearOption, resolveAcademicStartYear } from '@/utils/academicYear'

const props = defineProps({
  studentId: { type: Number, default: null },
  academicYear: { type: Object, default: null }
})

const emit = defineEmits(['update:studentId', 'update:academicYear', 'change'])

// === 学年逻辑 ===
const academicYearOptions = computed(() => {
  const currentAcademicStartYear = resolveAcademicStartYear()
  
  const options = []
  for (let i = 0; i < 4; i++) {
    const y = currentAcademicStartYear - i
    options.push(createAcademicYearOption(y))
  }
  return options
})

const defaultAcademicYear = computed(() => String(resolveAcademicStartYear()))

const currentAcademicYear = ref(defaultAcademicYear.value)

function onAcademicYearChange(val) {
  const yearInfo = academicYearOptions.value.find(item => item.value === val)
  emit('update:academicYear', yearInfo ? { start: yearInfo.start, end: yearInfo.end, value: yearInfo.value, label: yearInfo.label } : null)
  emit('change')
}

// === 班级筛选逻辑 ===
const selectedClass = ref([]) // [entryYear, classCode]
const classOptions = ref([])
const isClassSelected = computed(() => selectedClass.value && selectedClass.value.length === 2)

async function loadClassOptions() {
  try {
    const res = await getClasses()
    const data = res.data || []
    
    // Group by entryYear
    const groups = {}
    data.forEach(item => {
      if (!groups[item.entryYear]) {
        groups[item.entryYear] = []
      }
      groups[item.entryYear].push(item.classCode)
    })
    
    // Convert to Cascader format
    classOptions.value = Object.keys(groups).sort().reverse().map(year => ({
      value: year,
      label: `${year}级`,
      children: groups[year].sort().map(code => ({
        value: code,
        label: `${code}班`
      }))
    }))
  } catch (e) {
    console.error('加载班级列表失败', e)
  }
}

async function onClassChange(val) {
  currentStudentId.value = null // 重置选中学生
  studentOptions.value = []
  
  if (val && val.length === 2) {
    // 选中了班级，加载该班级学生
    searchLoading.value = true
    try {
      const res = await getStudentsByClass(val[0], val[1])
      studentOptions.value = res.data || []
      // 如果班级里有学生，默认选中第一个？或者不选等待用户选
    } catch (e) {
      console.error('加载班级学生失败', e)
    } finally {
      searchLoading.value = false
    }
  } else {
    //同样需要重置，且恢复远程搜索模式
  }
}

// === 学生选择逻辑 ===
const currentStudentId = ref(props.studentId)
const studentOptions = ref([])
const searchLoading = ref(false)

// 仅在未选择班级时启用远程搜索
async function searchStudents(query) {
  if (isClassSelected.value) return // 班级模式下不进行远程搜索
  
  if (query && query.length > 0) {
    searchLoading.value = true
    try {
      const res = await listStudent({ studentName: query, pageNum: 1, pageSize: 20 })
      studentOptions.value = res.rows || []
    } catch (e) {
      console.error('搜索学生失败:', e)
      studentOptions.value = []
    } finally {
      searchLoading.value = false
    }
  } else {
    studentOptions.value = []
  }
}

function onStudentChange(val) {
  emit('update:studentId', val)
  emit('change')
}

// === 初始化 ===
onMounted(() => {
  onAcademicYearChange(currentAcademicYear.value)
  loadClassOptions()
})

watch(() => props.academicYear, (val) => {
  if (val?.value && val.value !== currentAcademicYear.value) {
    currentAcademicYear.value = val.value
  }
}, { immediate: true })

watch(() => props.studentId, async (val) => {
  if (val && val !== currentStudentId.value) {
    currentStudentId.value = val
    // 如果不在当前列表中，则尝试加载该学生信息回显
    const exists = studentOptions.value.find(s => s.studentId === val)
    if (!exists) {
      try {
        const res = await getStudentProfileSummary(val)
        const student = res.data
        if (student) {
          studentOptions.value = [student]
          
          // 自动设置班级筛选器
          if (student.entryYear && student.classCode) {
            selectedClass.value = [student.entryYear, student.classCode]
          }
        }
      } catch (e) {
        console.error('加载学生信息失败:', e)
      }
    }
  }
}, { immediate: true })
</script>

<style scoped>
.student-selector {
  display: flex;
  align-items: center;
}
</style>
