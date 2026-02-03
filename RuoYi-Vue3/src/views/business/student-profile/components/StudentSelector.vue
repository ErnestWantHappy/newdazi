<template>
  <div class="student-selector">
    <el-form :inline="true">
      <!-- 学期选择 -->
      <el-form-item label="学期">
        <el-select v-model="currentSemester" placeholder="选择学期" style="width: 160px" @change="onSemesterChange">
          <el-option 
            v-for="sem in semesterOptions" 
            :key="sem.value" 
            :label="sem.label" 
            :value="sem.value" 
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
import { getClasses, getStudentsByClass } from '@/api/business/studentProfile'

const props = defineProps({
  studentId: { type: Number, default: null },
  semester: { type: Object, default: null }
})

const emit = defineEmits(['update:studentId', 'update:semester', 'change'])

// === 学期逻辑 ===
const semesterOptions = computed(() => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  const currentYear = month >= 7 ? year : year - 1
  
  const options = []
  for (let i = 0; i < 4; i++) {
    const y = currentYear - i
    options.push({
      value: `${y}-1`,
      label: `${y}-${y+1} 第一学期`,
      start: `${y}-09-01`,
      end: `${y+1}-02-28`
    })
    options.push({
      value: `${y}-2`,
      label: `${y}-${y+1} 第二学期`,
      start: `${y+1}-03-01`,
      end: `${y+1}-08-31`
    })
  }
  return options
})

const defaultSemester = computed(() => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  if (month >= 9 || month <= 2) {
    const y = month >= 9 ? year : year - 1
    return `${y}-1`
  } else {
    return `${year - 1}-2`
  }
})

const currentSemester = ref(defaultSemester.value)

function onSemesterChange(val) {
  const semInfo = semesterOptions.value.find(s => s.value === val)
  emit('update:semester', semInfo ? { start: semInfo.start, end: semInfo.end } : null)
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
  onSemesterChange(currentSemester.value)
  loadClassOptions()
})

watch(() => props.studentId, async (val) => {
  if (val && val !== currentStudentId.value) {
    currentStudentId.value = val
    // 如果不在当前列表中，则尝试加载该学生信息回显
    const exists = studentOptions.value.find(s => s.studentId === val)
    if (!exists) {
      try {
        const res = await listStudent({ studentId: val, pageNum: 1, pageSize: 1 })
        if (res.rows && res.rows.length > 0) {
          const student = res.rows[0]
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
