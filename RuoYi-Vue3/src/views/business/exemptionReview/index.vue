<template>
  <div class="app-container exemption-review-page">
    <el-card shadow="never" class="filter-card">
      <template #header>
        <div class="card-header">
          <div>
            <strong>免抽测申请审核</strong>
            <span class="scope-note">查看教师提交时的数据快照，审核不会修改统计比例</span>
          </div>
          <el-button
            v-hasPermi="['business:exemption:standard']"
            type="primary"
            plain
            @click="openStandardDialog"
          >
            应使用课数设置
          </el-button>
        </div>
      </template>
      <el-form :model="filters" inline label-width="72px">
        <el-form-item label="学年">
          <el-select v-model="filters.academicYear" style="width: 150px">
            <el-option v-for="year in academicYears" :key="year" :label="`${year}-${Number(year) + 1}学年`" :value="year" />
          </el-select>
        </el-form-item>
        <el-form-item label="学期">
          <el-select v-model="filters.semester" style="width: 120px">
            <el-option label="第一学期" value="1" />
            <el-option label="第二学期" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="年级">
          <el-select v-model="filters.grade" clearable style="width: 110px">
            <el-option v-for="grade in gradeOptions" :key="grade" :label="`${grade}年级`" :value="grade" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" clearable style="width: 120px">
            <el-option label="待审核" value="PENDING" />
            <el-option label="通过" value="PASS" />
            <el-option label="不通过" value="FAIL" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" clearable placeholder="学校或教师" @keyup.enter="reload" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="reload">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" v-loading="loading">
      <el-table :data="rows" border stripe>
        <el-table-column label="学校" prop="deptName" min-width="170" fixed />
        <el-table-column label="教师" prop="teacherName" width="110" />
        <el-table-column label="学年学期" min-width="185">
          <template #default="{ row }">{{ row.academicYear }}-{{ Number(row.academicYear) + 1 }}学年 第{{ row.semester }}学期</template>
        </el-table-column>
        <el-table-column label="年级" width="80"><template #default="{ row }">{{ row.grade }}年级</template></el-table-column>
        <el-table-column label="班级数" prop="classCount" width="80" />
        <el-table-column label="各班平台使用摘要" prop="classUsageSummary" min-width="260" show-overflow-tooltip />
        <el-table-column label="操作题批改率" width="125"><template #default="{ row }">{{ rateText(row.practicalRate, '暂无提交') }}</template></el-table-column>
        <el-table-column label="教师备注" prop="teacherRemark" min-width="180" show-overflow-tooltip />
        <el-table-column label="申请时间" min-width="165"><template #default="{ row }">{{ formatTime(row.submitTime) }}</template></el-table-column>
        <el-table-column label="状态" width="95"><template #default="{ row }"><el-tag :type="statusMeta(row.status).type">{{ statusMeta(row.status).label }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="90" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="openDetail(row.applicationId)">查看</el-button></template></el-table-column>
      </el-table>
      <pagination v-show="total > 0" :total="total" v-model:page="page.pageNum" v-model:limit="page.pageSize" @pagination="loadRows" />
    </el-card>

    <el-dialog v-model="detailVisible" title="免抽测申请详情" width="88%">
      <template v-if="detail">
        <el-descriptions :column="4" border>
          <el-descriptions-item label="学校">{{ detail.deptName }}</el-descriptions-item>
          <el-descriptions-item label="教师">{{ detail.teacherName }}</el-descriptions-item>
          <el-descriptions-item label="年级">{{ detail.grade }}年级</el-descriptions-item>
          <el-descriptions-item label="状态">{{ statusMeta(detail.status).label }}</el-descriptions-item>
          <el-descriptions-item label="教师总备注" :span="4">{{ detail.teacherRemark || '无' }}</el-descriptions-item>
          <el-descriptions-item label="审核备注" :span="4">{{ detail.reviewRemark || '尚未审核' }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="detail.classes" border class="detail-section">
          <el-table-column type="expand">
            <template #default="{ row }">
              <el-table :data="coursesForClass(row)" border size="small">
                <el-table-column label="使用日期" min-width="160"><template #default="{ row: course }">{{ formatTime(course.usageDate, '暂无真实使用') }}</template></el-table-column>
                <el-table-column label="课程" prop="lessonTitle" min-width="190" />
                <el-table-column label="有效/参与" width="100"><template #default="{ row: course }">{{ course.validStudentCount }}/{{ course.participantCount }}</template></el-table-column>
                <el-table-column label="参与率" width="90"><template #default="{ row: course }">{{ rateText(course.participationRate) }}</template></el-table-column>
                <el-table-column label="计入使用课" width="100"><template #default="{ row: course }">{{ course.countedAsUsed ? '是' : '否' }}</template></el-table-column>
                <el-table-column label="操作题上交/已批" width="130"><template #default="{ row: course }">{{ course.practicalDueCount }}/{{ course.practicalGradedCount }}</template></el-table-column>
              </el-table>
            </template>
          </el-table-column>
          <el-table-column label="班级" width="90"><template #default="{ row }">{{ row.classCode }}班</template></el-table-column>
          <el-table-column label="有效学生" prop="validStudentCount" width="90" />
          <el-table-column label="实际/应使用课" width="125"><template #default="{ row }">{{ row.usedLessonCount }}/{{ row.requiredLessonCount }}</template></el-table-column>
          <el-table-column label="平台使用率" width="105"><template #default="{ row }">{{ rateText(row.usageRate) }}</template></el-table-column>
          <el-table-column label="平台结论" width="95"><template #default="{ row }">{{ row.usageQualified ? '达标' : '未达标' }}</template></el-table-column>
          <el-table-column label="操作题上交/已批" width="130"><template #default="{ row }">{{ row.practicalDueCount }}/{{ row.practicalGradedCount }}</template></el-table-column>
          <el-table-column label="批改率" width="100"><template #default="{ row }">{{ rateText(row.practicalRate, '暂无提交') }}</template></el-table-column>
        </el-table>
        <div v-if="detail.attachments?.length" class="detail-section">
          <strong>证明附件：</strong>
          <el-link v-for="item in detail.attachments" :key="item.attachmentId" type="primary" :href="attachmentUrl(item.resourcePath)" target="_blank" class="attachment-link">{{ item.originalFileName }}</el-link>
        </div>
        <el-form v-if="detail.status === 'PENDING'" label-width="90px" class="detail-section">
          <el-form-item label="审核备注"><el-input v-model="reviewForm.reviewRemark" type="textarea" :rows="3" maxlength="1000" show-word-limit /></el-form-item>
          <el-form-item>
            <el-button type="success" :loading="reviewSaving" @click="submitReview('PASS')">通过</el-button>
            <el-button type="danger" :loading="reviewSaving" @click="submitReview('FAIL')">不通过</el-button>
          </el-form-item>
        </el-form>
      </template>
    </el-dialog>

    <el-dialog v-model="standardVisible" title="每班应使用课数设置" width="680px">
      <el-form inline>
        <el-form-item label="学年">
          <el-select v-model="standardPeriod.academicYear" style="width: 150px"><el-option v-for="year in academicYears" :key="year" :label="`${year}-${Number(year) + 1}学年`" :value="year" /></el-select>
        </el-form-item>
        <el-form-item label="学期">
          <el-select v-model="standardPeriod.semester" style="width: 120px"><el-option label="第一学期" value="1" /><el-option label="第二学期" value="2" /></el-select>
        </el-form-item>
        <el-button @click="loadStandards">读取</el-button>
      </el-form>
      <el-table :data="standardRows" border>
        <el-table-column label="年级" width="140"><template #default="{ row }">{{ row.grade }}年级</template></el-table-column>
        <el-table-column label="每班应使用课数"><template #default="{ row }"><el-input-number v-model="row.requiredLessonCount" :min="1" :max="100" /></template></el-table-column>
        <el-table-column label="操作" width="100"><template #default="{ row }"><el-button link type="primary" @click="saveStandard(row)">保存</el-button></template></el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="ExemptionReview">
import { getCurrentInstance, onMounted, reactive, ref } from 'vue'
import { resolveAcademicSemester, resolveAcademicStartYear } from '@/utils/academicYear'
import {
  getExemptionDetail,
  listExemptionReviews,
  listExemptionStandards,
  reviewExemption,
  saveExemptionStandard
} from '@/api/business/exemption'

const { proxy } = getCurrentInstance()
const currentAcademicYear = String(resolveAcademicStartYear())
const currentSemester = resolveAcademicSemester()
const academicYears = [String(Number(currentAcademicYear) - 2), String(Number(currentAcademicYear) - 1), currentAcademicYear]
const gradeOptions = Array.from({ length: 9 }, (_, index) => index + 1)
const filters = reactive({ academicYear: currentAcademicYear, semester: currentSemester, grade: '', status: '', keyword: '' })
const rows = ref([])
const total = ref(0)
const loading = ref(false)
const page = reactive({ pageNum: 1, pageSize: 10 })
const detailVisible = ref(false)
const detail = ref(null)
const reviewForm = reactive({ reviewRemark: '' })
const reviewSaving = ref(false)
const standardVisible = ref(false)
const standardPeriod = reactive({ academicYear: currentAcademicYear, semester: currentSemester })
const standardRows = ref([])

async function loadRows() {
  loading.value = true
  try {
    const res = await listExemptionReviews({ ...filters, grade: filters.grade || undefined, pageNum: page.pageNum, pageSize: page.pageSize })
    rows.value = res.rows || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function reload() { page.pageNum = 1; loadRows() }
function resetFilters() {
  Object.assign(filters, { academicYear: currentAcademicYear, semester: currentSemester, grade: '', status: '', keyword: '' })
  reload()
}
function statusMeta(status) {
  return { PENDING: { label: '待审核', type: 'warning' }, PASS: { label: '通过', type: 'success' }, FAIL: { label: '不通过', type: 'danger' } }[status] || { label: '未知', type: 'info' }
}
function rateText(value, empty = '--') { return value === null || value === undefined ? empty : `${value}%` }
function formatTime(value, empty = '--') { return value ? proxy.parseTime(value, '{y}-{m}-{d} {h}:{i}') : empty }

async function openDetail(applicationId) {
  const res = await getExemptionDetail(applicationId)
  detail.value = res.data || null
  reviewForm.reviewRemark = ''
  detailVisible.value = true
}
function coursesForClass(classRow) {
  return (detail.value?.courses || []).filter(item => Number(item.classSnapshotId) === Number(classRow.classSnapshotId))
}
function attachmentUrl(path) {
  return `${import.meta.env.VITE_APP_BASE_API}/common/download/resource?resource=${encodeURIComponent(path)}`
}
async function submitReview(status) {
  if (!detail.value) return
  const action = status === 'PASS' ? '通过' : '不通过'
  await proxy.$modal.confirm(`确认将该申请审核为“${action}”吗？`)
  reviewSaving.value = true
  try {
    const res = await reviewExemption(detail.value.applicationId, { status, reviewRemark: reviewForm.reviewRemark, version: detail.value.version })
    detail.value = res.data
    proxy.$modal.msgSuccess(`审核结果已保存：${action}`)
    await loadRows()
  } finally {
    reviewSaving.value = false
  }
}
async function openStandardDialog() { standardVisible.value = true; await loadStandards() }
async function loadStandards() {
  const res = await listExemptionStandards(standardPeriod)
  const saved = new Map((res.data || []).map(item => [Number(item.grade), Number(item.requiredLessonCount)]))
  standardRows.value = gradeOptions.map(grade => ({ grade, requiredLessonCount: saved.get(grade) || 15 }))
}
async function saveStandard(row) {
  await saveExemptionStandard({ academicYear: standardPeriod.academicYear, semester: standardPeriod.semester, grade: row.grade, requiredLessonCount: row.requiredLessonCount })
  proxy.$modal.msgSuccess(`${row.grade}年级应使用课数已保存`)
}

onMounted(loadRows)
</script>

<style scoped>
.filter-card { margin-bottom: 12px; }
.card-header { display: flex; align-items: center; justify-content: space-between; gap: 10px; }
.scope-note { margin-left: 12px; color: #909399; font-size: 13px; }
.detail-section { margin-top: 16px; }
.attachment-link { margin-left: 12px; }
</style>
