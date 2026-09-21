<template>
  <section v-loading="loading">
    <el-form inline :model="filters">
      <el-form-item label="学年"><el-select v-model="filters.academicYear" style="width: 170px">
        <el-option v-for="year in years" :key="year" :label="`${year}-${Number(year) + 1}学年`" :value="year" />
      </el-select></el-form-item>
      <el-form-item label="学期"><el-select v-model="filters.semester" style="width: 130px">
        <el-option label="第一学期" value="1" /><el-option label="第二学期" value="2" />
      </el-select></el-form-item>
      <el-form-item label="学校"><el-input v-model="filters.deptName" clearable @keyup.enter="load" /></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="load">查询</el-button></el-form-item>
    </el-form>
    <el-radio-group v-model="schoolType" @change="draw">
      <el-radio-button value="1">小学组</el-radio-button>
      <el-radio-button value="2">初中组</el-radio-button>
      <el-radio-button value="3">高中组</el-radio-button>
      <el-radio-button value="all">全部学校</el-radio-button>
    </el-radio-group>
    <h3>各校打字平均速度对比</h3>
    <el-alert v-if="failed" title="统计读取失败，请重新查询" type="warning" :closable="false" />
    <div ref="chartElement" class="typing-chart" :style="{ height: `${Math.max(280, visibleRows.length * 34 + 70)}px` }" />
    <el-table :data="visibleRows" border empty-text="本学期暂无符合条件的记录">
      <el-table-column label="学校" prop="deptName" min-width="200" />
      <el-table-column label="平均打字速度（字/分）" min-width="180" sortable :sort-method="compareSpeed">
        <template #default="{ row }">{{ row.avgTypingSpeed ?? '暂无记录' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="95"><template #default="{ row }">
        <el-button link type="primary" @click="openClasses(row)">班级明细</el-button>
      </template></el-table-column>
    </el-table>
    <el-dialog v-model="classDialog" :title="`${selectedSchool?.deptName || ''}打字统计`" width="min(700px, 94vw)">
      <el-table v-loading="classLoading" :data="classRows" border>
        <el-table-column label="班级" prop="deptName" min-width="150" />
        <el-table-column label="平均速度（字/分）" min-width="160"><template #default="{ row }">{{ row.avgTypingSpeed ?? '暂无记录' }}</template></el-table-column>
      </el-table>
      <pagination v-show="classTotal > 0" :total="classTotal" v-model:page="classPage.pageNum" v-model:limit="classPage.pageSize" @pagination="loadClasses" />
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { listTermStats, listTypingClassDetails } from '@/api/business/schoolScore'

// 旧打字统计接口仍按9月到次年8月划分学年，默认筛选须与其区间一致。
const today = new Date()
const month = today.getMonth() + 1
const year = String(today.getFullYear() - (month < 9 ? 1 : 0))
const years = Array.from({ length: 5 }, (_, i) => String(Number(year) - i))
const filters = reactive({ academicYear: year, semester: month >= 2 && month <= 8 ? '2' : '1', deptName: '' })
const schoolType = ref('1'), rows = ref([]), loading = ref(false), failed = ref(false)
const chartElement = ref(null)
let chart, observer, requestId = 0, classRequestId = 0
let loadedFilters = { ...filters }
const compareSpeed = (a, b) => Number(a.avgTypingSpeed ?? -1) - Number(b.avgTypingSpeed ?? -1)
const visibleRows = computed(() => rows.value.filter(row => schoolType.value === 'all' || String(row.schoolType) === schoolType.value).sort((a, b) => compareSpeed(b, a)))
async function draw() {
  await nextTick()
  if (!chartElement.value) return
  if (!chart) chart = echarts.init(chartElement.value)
  chart.resize()
  chart.setOption({
    tooltip: { trigger: 'axis', renderMode: 'richText', axisPointer: { type: 'shadow' } },
    grid: { left: 12, right: 60, top: 20, bottom: 35, containLabel: true },
    xAxis: { type: 'value', name: '字/分' },
    yAxis: { type: 'category', inverse: true, data: visibleRows.value.map(row => row.deptName), axisLabel: { width: 150, overflow: 'truncate' } },
    series: [{ type: 'bar', data: visibleRows.value.map(row => row.avgTypingSpeed ?? null), itemStyle: { color: '#249c8a' }, label: { show: true, position: 'right' }, barMaxWidth: 22 }]
  }, true)
}
async function load() {
  const id = ++requestId, query = { ...filters }
  loading.value = true; failed.value = false
  try {
    const response = await listTermStats(query)
    if (id !== requestId) return
    rows.value = response.data || []; loadedFilters = query
  } catch (_) {
    if (id !== requestId) return
    rows.value = []; failed.value = true
  } finally {
    if (id === requestId) { loading.value = false; await draw() }
  }
}
const classDialog = ref(false), classLoading = ref(false), classRows = ref([]), classTotal = ref(0), selectedSchool = ref(null)
const classPage = reactive({ pageNum: 1, pageSize: 10 })
function openClasses(row) {
  selectedSchool.value = row; classDialog.value = true; classPage.pageNum = 1
  loadClasses()
}
async function loadClasses() {
  const id = ++classRequestId
  classLoading.value = true; classRows.value = []; classTotal.value = 0
  try {
    const response = await listTypingClassDetails({ ...loadedFilters, ...classPage, deptId: selectedSchool.value.deptId })
    if (id !== classRequestId) return
    classRows.value = response.rows || []; classTotal.value = response.total || 0
  } catch (_) {
    // 请求层统一显示错误，不能留下上一所学校的数据。
  } finally { if (id === classRequestId) classLoading.value = false }
}
onMounted(() => {
  observer = new ResizeObserver(() => chart?.resize())
  observer.observe(chartElement.value)
  load()
})
onBeforeUnmount(() => { requestId++; classRequestId++; observer?.disconnect(); chart?.dispose() })
</script>

<style scoped>
.typing-chart { width: 100%; min-width: 0; }
h3 { font-size: 16px; margin: 20px 0 8px; }
</style>
