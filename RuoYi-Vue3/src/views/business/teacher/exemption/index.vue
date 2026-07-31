<template>
  <div class="app-container exemption-page">
    <el-card shadow="never">
      <template #header>
        <div class="page-header">
          <div>
            <strong>教师免抽测申请</strong>
            <div class="muted">系统自动统计全部真实任教班级，班级不能手动排除。</div>
          </div>
          <el-tag type="info">一次提交，教研员直接审核</el-tag>
        </div>
      </template>

      <el-form :model="form" inline label-width="72px">
        <el-form-item label="学年">
          <el-select v-model="form.academicYear" style="width: 160px">
            <el-option
              v-for="year in academicYears"
              :key="year"
              :label="`${year}-${Number(year) + 1}学年`"
              :value="year"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学期">
          <el-select v-model="form.semester" style="width: 130px">
            <el-option label="第一学期" value="1" />
            <el-option label="第二学期" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="年级">
          <el-select v-model="form.grade" placeholder="请选择年级" style="width: 130px">
            <el-option v-for="grade in gradeOptions" :key="grade" :label="`${grade}年级`" :value="grade" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="previewLoading" @click="loadPreview">读取系统统计</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <template v-if="preview">
      <el-card shadow="never" class="section-card">
        <template #header><strong>基本条件与任教班级</strong></template>
        <el-descriptions :column="4" border>
          <el-descriptions-item label="学校">{{ preview.deptName }}</el-descriptions-item>
          <el-descriptions-item label="教师">{{ preview.teacherName }}</el-descriptions-item>
          <el-descriptions-item label="年级">{{ preview.grade }}年级</el-descriptions-item>
          <el-descriptions-item label="应使用课数">{{ preview.requiredLessonCount }}课/班</el-descriptions-item>
        </el-descriptions>
        <el-alert
          v-if="preview.alreadySubmitted"
          title="该学年、学期和年级已经提交过申请，不能二次提交。"
          type="warning"
          :closable="false"
          class="section-gap"
        />
        <el-empty v-if="preview.classes.length === 0" description="未找到该年级的真实任教班级" />
      </el-card>

      <div class="metric-grid">
        <el-card shadow="never">
          <div class="metric-title">平台使用情况</div>
          <div class="metric-value">{{ preview.allClassesQualified ? '全部班级达标' : '存在未达标班级' }}</div>
          <el-tag :type="preview.allClassesQualified ? 'success' : 'danger'">
            每班独立达到80%
          </el-tag>
        </el-card>
        <el-card shadow="never">
          <div class="metric-title">操作题批改情况</div>
          <div class="metric-value">
            {{ preview.practicalRate == null ? '暂无操作题提交' : `${preview.practicalRate}%` }}
          </div>
          <div class="muted">
            已交 {{ preview.practicalDueCount }} · 已批 {{ preview.practicalGradedCount }} ·
            未批 {{ preview.practicalUngradedCount }}
          </div>
        </el-card>
      </div>

      <el-card shadow="never" class="section-card">
        <template #header><strong>班级与课程明细</strong></template>
        <el-table :data="preview.classes" border>
          <el-table-column type="expand">
            <template #default="{ row }">
              <el-table :data="row.courses" border size="small">
                <el-table-column label="使用日期" min-width="165">
                  <template #default="{ row: course }">{{ formatTime(course.usageDate) }}</template>
                </el-table-column>
                <el-table-column label="课程名称" prop="lessonTitle" min-width="210" />
                <el-table-column label="有效学生" prop="validStudentCount" width="90" />
                <el-table-column label="实际参与" prop="participantCount" width="90" />
                <el-table-column label="参与率" width="90">
                  <template #default="{ row: course }">{{ rateText(course.participationRate) }}</template>
                </el-table-column>
                <el-table-column label="计入使用课" width="105">
                  <template #default="{ row: course }">
                    <el-tag :type="course.countedAsUsed ? 'success' : 'info'">
                      {{ course.countedAsUsed ? '计入' : '不计入' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作题上交/已批" width="135">
                  <template #default="{ row: course }">
                    {{ course.practicalDueCount }}/{{ course.practicalGradedCount }}
                  </template>
                </el-table-column>
                <el-table-column label="批改率" width="90">
                  <template #default="{ row: course }">{{ rateText(course.practicalRate, '暂无') }}</template>
                </el-table-column>
              </el-table>
            </template>
          </el-table-column>
          <el-table-column label="班级" width="110">
            <template #default="{ row }">{{ row.classCode }}班</template>
          </el-table-column>
          <el-table-column label="有效学生" prop="validStudentCount" width="100" />
          <el-table-column label="实际/应使用课" width="130">
            <template #default="{ row }">{{ row.usedLessonCount }}/{{ row.requiredLessonCount }}</template>
          </el-table-column>
          <el-table-column label="平台使用率" width="115">
            <template #default="{ row }">{{ rateText(row.usageRate) }}</template>
          </el-table-column>
          <el-table-column label="平台结论" width="100">
            <template #default="{ row }">
              <el-tag :type="row.usageQualified ? 'success' : 'danger'">
                {{ row.usageQualified ? '达标' : '未达标' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作题上交/已批/未批" min-width="175">
            <template #default="{ row }">
              {{ row.practicalDueCount }}/{{ row.practicalGradedCount }}/{{ row.practicalUngradedCount }}
            </template>
          </el-table-column>
          <el-table-column label="操作题批改率" width="125">
            <template #default="{ row }">{{ rateText(row.practicalRate, '暂无提交') }}</template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card shadow="never" class="section-card">
        <template #header><strong>补充材料与提交</strong></template>
        <el-form label-width="110px">
          <el-form-item label="总补充说明">
            <el-input
              v-model="form.teacherRemark"
              type="textarea"
              :rows="5"
              maxlength="2000"
              show-word-limit
              placeholder="可填写台风停课、磨课、机房维修、学校活动、个别班级课堂调整等系统无法自动识别的情况"
            />
          </el-form-item>
          <el-form-item label="证明附件">
            <el-upload
              v-model:file-list="uploadFiles"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :limit="5"
              :before-upload="beforeUpload"
              :on-success="uploadSuccess"
              :on-error="uploadError"
              multiple
            >
              <el-button>选择附件</el-button>
              <template #tip>
                <div class="el-upload__tip">可选，最多5个，每个不超过10MB。</div>
              </template>
            </el-upload>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              :loading="submitting"
              :disabled="preview.alreadySubmitted || preview.classes.length === 0"
              @click="submitApplication"
            >
              确认提交一次申请
            </el-button>
            <span class="muted submit-note">未达到80%也允许提交，系统比例不会被修改。</span>
          </el-form-item>
        </el-form>
      </el-card>
    </template>

    <el-card shadow="never" class="section-card">
      <template #header><strong>我的申请记录</strong></template>
      <el-table :data="historyRows" border v-loading="historyLoading">
        <el-table-column label="学年学期" min-width="180">
          <template #default="{ row }">
            {{ row.academicYear }}-{{ Number(row.academicYear) + 1 }}学年 第{{ row.semester }}学期
          </template>
        </el-table-column>
        <el-table-column label="年级" width="90">
          <template #default="{ row }">{{ row.grade }}年级</template>
        </el-table-column>
        <el-table-column label="班级数" prop="classCount" width="90" />
        <el-table-column label="平台结论" width="110">
          <template #default="{ row }">{{ row.allClassesQualified ? '全部达标' : '存在未达标' }}</template>
        </el-table-column>
        <el-table-column label="操作题批改率" width="130">
          <template #default="{ row }">{{ rateText(row.practicalRate, '暂无提交') }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><el-tag :type="statusMeta(row.status).type">{{ statusMeta(row.status).label }}</el-tag></template>
        </el-table-column>
        <el-table-column label="申请时间" min-width="170">
          <template #default="{ row }">{{ formatTime(row.submitTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90">
          <template #default="{ row }"><el-button link type="primary" @click="openDetail(row.applicationId)">查看</el-button></template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailVisible" title="免抽测申请快照" width="82%">
      <template v-if="detail">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="学校">{{ detail.deptName }}</el-descriptions-item>
          <el-descriptions-item label="教师">{{ detail.teacherName }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ statusMeta(detail.status).label }}</el-descriptions-item>
          <el-descriptions-item label="教师说明" :span="3">{{ detail.teacherRemark || '无' }}</el-descriptions-item>
          <el-descriptions-item label="审核备注" :span="3">{{ detail.reviewRemark || '尚未审核' }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="detail.classes" border class="section-gap">
          <el-table-column label="班级"><template #default="{ row }">{{ row.classCode }}班</template></el-table-column>
          <el-table-column label="实际/应使用"><template #default="{ row }">{{ row.usedLessonCount }}/{{ row.requiredLessonCount }}</template></el-table-column>
          <el-table-column label="平台使用率"><template #default="{ row }">{{ rateText(row.usageRate) }}</template></el-table-column>
          <el-table-column label="操作题上交/已批"><template #default="{ row }">{{ row.practicalDueCount }}/{{ row.practicalGradedCount }}</template></el-table-column>
          <el-table-column label="批改率"><template #default="{ row }">{{ rateText(row.practicalRate, '暂无提交') }}</template></el-table-column>
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="TeacherExemption">
import { computed, getCurrentInstance, onMounted, reactive, ref } from 'vue'
import { resolveAcademicSemester, resolveAcademicStartYear } from '@/utils/academicYear'
import { refreshAuthorizationHeader } from '@/utils/session'
import {
  getExemptionDetail,
  listMyExemptions,
  previewExemption,
  submitExemption
} from '@/api/business/exemption'

const { proxy } = getCurrentInstance()
const currentAcademicYear = String(resolveAcademicStartYear())
const academicYears = [
  String(Number(currentAcademicYear) - 2),
  String(Number(currentAcademicYear) - 1),
  currentAcademicYear
]
const gradeOptions = [1, 2, 3, 4, 5, 6, 7, 8, 9]
const form = reactive({
  academicYear: currentAcademicYear,
  semester: resolveAcademicSemester(),
  grade: null,
  teacherRemark: ''
})
const preview = ref(null)
const previewLoading = ref(false)
const submitting = ref(false)
const historyRows = ref([])
const historyLoading = ref(false)
const detailVisible = ref(false)
const detail = ref(null)
const uploadFiles = ref([])
const uploadUrl = `${import.meta.env.VITE_APP_BASE_API}/common/upload`
const uploadHeaders = computed(() => refreshAuthorizationHeader())

async function loadPreview() {
  if (!form.grade) {
    proxy.$modal.msgWarning('请先选择年级')
    return
  }
  previewLoading.value = true
  try {
    const res = await previewExemption({
      academicYear: form.academicYear,
      semester: form.semester,
      grade: form.grade
    })
    preview.value = res.data
  } finally {
    previewLoading.value = false
  }
}

async function submitApplication() {
  await proxy.$modal.confirm('提交后不能修改或二次提交，是否确认继续？')
  submitting.value = true
  try {
    const attachments = uploadFiles.value
      .filter(file => file.resourcePath)
      .map(file => ({
        originalFileName: file.name,
        resourcePath: file.resourcePath,
        fileSize: file.size,
        mimeType: file.raw?.type || file.mimeType
      }))
    await submitExemption({
      academicYear: form.academicYear,
      semester: form.semester,
      grade: form.grade,
      teacherRemark: form.teacherRemark,
      attachments
    })
    proxy.$modal.msgSuccess('免抽测申请已提交')
    uploadFiles.value = []
    await Promise.all([loadPreview(), loadHistory()])
  } finally {
    submitting.value = false
  }
}

function beforeUpload(file) {
  const allowed = ['doc', 'docx', 'xls', 'xlsx', 'pdf', 'jpg', 'jpeg', 'png', 'zip']
  const extension = file.name.split('.').pop()?.toLowerCase()
  if (!allowed.includes(extension)) {
    proxy.$modal.msgError(`仅支持 ${allowed.join('/')} 格式`)
    return false
  }
  if (file.size > 10 * 1024 * 1024) {
    proxy.$modal.msgError('单个附件不能超过10MB')
    return false
  }
  return true
}

function uploadSuccess(response, file) {
  if (response.code !== 200) {
    proxy.$modal.msgError(response.msg || '上传失败')
    return
  }
  file.resourcePath = response.fileName
  file.mimeType = file.raw?.type
}

function uploadError() {
  proxy.$modal.msgError('附件上传失败')
}

async function loadHistory() {
  historyLoading.value = true
  try {
    const res = await listMyExemptions()
    historyRows.value = res.data || []
  } finally {
    historyLoading.value = false
  }
}

async function openDetail(applicationId) {
  const res = await getExemptionDetail(applicationId)
  detail.value = res.data
  detailVisible.value = true
}

function rateText(value, empty = '--') {
  return value === null || value === undefined ? empty : `${value}%`
}

function formatTime(value) {
  return value ? proxy.parseTime(value, '{y}-{m}-{d} {h}:{i}') : '暂无真实使用'
}

function statusMeta(status) {
  return ({
    PENDING: { label: '待审核', type: 'warning' },
    PASS: { label: '通过', type: 'success' },
    FAIL: { label: '不通过', type: 'danger' }
  })[status] || { label: '未知状态', type: 'info' }
}

onMounted(loadHistory)
</script>

<style scoped lang="scss">
.exemption-page {
  background: #f5f7fa;
}
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.muted {
  color: #909399;
  font-size: 13px;
}
.section-card,
.section-gap {
  margin-top: 16px;
}
.metric-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 16px;
}
.metric-title {
  color: #606266;
}
.metric-value {
  margin: 10px 0;
  font-size: 24px;
  font-weight: 600;
}
.submit-note {
  margin-left: 12px;
}
@media (max-width: 900px) {
  .metric-grid {
    grid-template-columns: 1fr;
  }
}
</style>
