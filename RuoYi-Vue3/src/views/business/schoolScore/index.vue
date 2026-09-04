<template>
  <div class="app-container supervision-page">
    <el-card v-show="mainTab === 'supervision'" shadow="never" class="filter-card">
      <template #header>
        <div class="card-header">
          <div>
            <strong>课程与成绩监管</strong>
            <span class="scope-note">按真实课堂使用日期查看，课程创建时间仅在详情展示</span>
          </div>
          <div>
            <el-radio-group v-model="viewMode" size="small" @change="handleViewModeChange">
              <el-radio-button value="timeline">按时间查看</el-radio-button>
              <el-radio-button value="school">按学校查看</el-radio-button>
            </el-radio-group>
            <el-button link @click="filtersExpanded = !filtersExpanded">
              {{ filtersExpanded ? '收起筛选' : '展开筛选' }}
            </el-button>
            <el-button
              v-hasPermi="['business:practicalDeadline:config']"
              type="primary"
              plain
              @click="openConfig"
            >
              批改期限配置
            </el-button>
          </div>
        </div>
      </template>
      <el-form v-show="filtersExpanded" :model="filters" inline label-width="72px">
        <el-form-item label="学年">
          <el-select v-model="filters.academicYear" style="width: 150px">
            <el-option v-for="year in academicYears" :key="year" :label="`${year}-${Number(year) + 1}学年`" :value="year" />
          </el-select>
        </el-form-item>
        <el-form-item label="学期">
          <el-select v-model="filters.semester" style="width: 130px">
            <el-option label="全部学期" value="all" />
            <el-option label="第一学期" value="1" />
            <el-option label="第二学期" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="使用日期">
          <el-date-picker
            v-model="usageDateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 250px"
          />
        </el-form-item>
        <el-form-item label="年级">
          <el-select v-model="filters.grade" clearable style="width: 110px">
            <el-option v-for="grade in gradeOptions" :key="grade" :label="`${grade}年级`" :value="String(grade)" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级">
          <el-input v-model="filters.classCode" clearable placeholder="如101" style="width: 110px" />
        </el-form-item>
        <el-form-item label="时间排序">
          <el-select v-model="filters.usageSort" style="width: 130px">
            <el-option label="最新使用优先" value="desc" />
            <el-option label="最早使用优先" value="asc" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程类型">
          <el-select v-model="filters.lessonMode" clearable style="width: 150px">
            <el-option label="常规测评课" value="assessment" />
            <el-option label="课堂考勤课" value="attendance" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作题">
          <el-select v-model="filters.hasPractical" clearable style="width: 130px">
            <el-option label="包含操作题" :value="true" />
            <el-option label="不含操作题" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item label="批改状态">
          <el-select v-model="filters.statusCode" clearable style="width: 150px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" clearable placeholder="课程或教师" @keyup.enter="reloadCurrent" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="reloadCurrent">查询</el-button>
          <el-button icon="Refresh" @click="resetFilters">重置</el-button>
          <el-button
            v-hasPermi="['business:teachingSupervision:export']"
            icon="Download"
            @click="exportCurrent"
          >
            导出当前层级
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div v-if="viewMode === 'school'" class="level-nav">
      <el-button link type="primary" @click="goLevel('school')">全部学校</el-button>
      <template v-if="selectedSchool">
        <span>/</span>
        <el-button link type="primary" @click="goLevel('teacher')">{{ selectedSchool.deptName }}</el-button>
      </template>
      <template v-if="selectedTeacher">
        <span>/</span>
        <el-button link type="primary" @click="goLevel('course')">{{ selectedTeacher.teacherName }}</el-button>
      </template>
    </div>

    <el-card v-if="viewMode === 'timeline'" shadow="never" v-loading="loading">
      <el-table :data="rows" border stripe>
        <el-table-column label="使用日期" min-width="165" fixed>
          <template #default="{ row }">{{ formatUsageTime(row.usageDate) }}</template>
        </el-table-column>
        <el-table-column label="课程名称" prop="lessonTitle" min-width="210" fixed />
        <el-table-column label="类型" width="105">
          <template #default="{ row }">{{ row.lessonMode === 'attendance' ? '课堂考勤' : '常规课' }}</template>
        </el-table-column>
        <el-table-column label="学校" prop="deptName" min-width="180" />
        <el-table-column label="教师" prop="teacherName" min-width="110" />
        <el-table-column label="年级" width="80"><template #default="{ row }">{{ row.grade }}年级</template></el-table-column>
        <el-table-column label="班级" width="80"><template #default="{ row }">{{ row.classCode }}班</template></el-table-column>
        <el-table-column label="参与学生" width="105">
          <template #default="{ row }">{{ row.participantCount }}/{{ row.totalStudentCount }}</template>
        </el-table-column>
        <el-table-column label="批改情况" min-width="165">
          <template #default="{ row }">
            <el-tag :type="simpleGradingMeta(row).type">{{ simpleGradingMeta(row).label }}</el-tag>
            <span v-if="row.practicalUngradedCount > 0" class="inline-count">{{ row.practicalUngradedCount }}份未批</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }"><el-button link type="primary" @click="openCourseDetail(row)">课程明细</el-button></template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="page.pageNum"
        v-model:limit="page.pageSize"
        @pagination="loadCurrent"
      />
    </el-card>

    <el-card v-else shadow="never" v-loading="loading">
      <el-table v-if="level === 'school'" :data="rows" border stripe>
        <el-table-column label="学校" prop="deptName" min-width="180" fixed />
        <el-table-column label="课程总数" prop="courseCount" width="100" align="center" />
        <el-table-column label="常规课" prop="assessmentCourseCount" width="90" align="center" />
        <el-table-column label="考勤课" prop="attendanceCourseCount" width="90" align="center" />
        <el-table-column label="有课程教师" prop="teacherCount" width="110" align="center" />
        <el-table-column label="指派班级" prop="classCount" width="100" align="center" />
        <el-table-column label="参与学生" prop="participantCount" width="100" align="center" />
        <el-table-column label="应批/已批/未批" min-width="150" align="center">
          <template #default="{ row }">{{ row.practicalDueCount }}/{{ row.practicalGradedCount }}/{{ row.practicalUngradedCount }}</template>
        </el-table-column>
        <el-table-column label="完成比例" width="100" align="center">
          <template #default="{ row }">{{ row.gradingRate ?? 0 }}%</template>
        </el-table-column>
        <el-table-column label="逾期班级" prop="overdueClassCount" width="100" align="center" />
        <el-table-column label="平均分" prop="avgTotalScore" width="90" align="center" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }"><el-button link type="primary" @click="openTeachers(row)">查看教师</el-button></template>
        </el-table-column>
      </el-table>

      <el-table v-else-if="level === 'teacher'" :data="rows" border stripe>
        <el-table-column label="教师" prop="teacherName" min-width="150" />
        <el-table-column label="账号" prop="teacherUserName" min-width="140" />
        <el-table-column label="课程总数" prop="courseCount" width="100" align="center" />
        <el-table-column label="常规课" prop="assessmentCourseCount" width="90" align="center" />
        <el-table-column label="考勤课" prop="attendanceCourseCount" width="90" align="center" />
        <el-table-column label="指派班级" prop="classCount" width="100" align="center" />
        <el-table-column label="参与学生" prop="participantCount" width="100" align="center" />
        <el-table-column label="应批/已批/未批" min-width="150" align="center">
          <template #default="{ row }">{{ row.practicalDueCount }}/{{ row.practicalGradedCount }}/{{ row.practicalUngradedCount }}</template>
        </el-table-column>
        <el-table-column label="平均分" prop="avgTotalScore" width="90" align="center" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }"><el-button link type="primary" @click="openCourses(row)">查看课程</el-button></template>
        </el-table-column>
      </el-table>

      <el-table v-else :data="rows" border stripe>
        <el-table-column label="课程" prop="lessonTitle" min-width="220" fixed />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">{{ row.lessonMode === 'attendance' ? '课堂考勤' : '常规测评' }}</template>
        </el-table-column>
        <el-table-column label="入学年份" prop="entryYear" width="100" />
        <el-table-column label="年级" prop="grade" width="80" />
        <el-table-column label="课次" prop="lessonNum" width="70" />
        <el-table-column label="题目" prop="questionCount" width="70" />
        <el-table-column label="班级" prop="classCount" width="70" />
        <el-table-column label="参与学生" prop="participantCount" width="90" />
        <el-table-column label="应批/已批/未批" min-width="145">
          <template #default="{ row }">{{ row.practicalDueCount }}/{{ row.practicalGradedCount }}/{{ row.practicalUngradedCount }}</template>
        </el-table-column>
        <el-table-column label="使用日期" min-width="165">
          <template #default="{ row }">{{ formatUsageTime(row.usageDate) }}</template>
        </el-table-column>
        <el-table-column label="最近截止" min-width="170">
          <template #default="{ row }">{{ formatDeadlineTime(row.nearestDeadlineTime) }}</template>
        </el-table-column>
        <el-table-column label="创建/修改" min-width="180">
          <template #default="{ row }">
            <div>{{ formatCourseCreateTime(row.createTime) }}</div>
            <div class="muted">{{ formatCourseUpdateTime(row.updateTime) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }"><el-button link type="primary" @click="openCourseDetail(row)">课程明细</el-button></template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="page.pageNum"
        v-model:limit="page.pageSize"
        @pagination="loadCurrent"
      />
    </el-card>

    <div v-show="mainTab === 'exemption'" class="exemption-panel">
      <el-card shadow="never" class="filter-card">
        <template #header>
          <div class="card-header">
            <div>
              <strong>免抽测申请</strong>
              <span class="scope-note">查看教师申报时的数据快照，审核不会修改真实比例</span>
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
        <el-form :model="exemptionFilters" inline label-width="72px">
          <el-form-item label="学年">
            <el-select v-model="exemptionFilters.academicYear" style="width: 150px">
              <el-option v-for="year in academicYears" :key="year" :label="`${year}-${Number(year) + 1}学年`" :value="year" />
            </el-select>
          </el-form-item>
          <el-form-item label="学期">
            <el-select v-model="exemptionFilters.semester" style="width: 120px">
              <el-option label="第一学期" value="1" />
              <el-option label="第二学期" value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="年级">
            <el-select v-model="exemptionFilters.grade" clearable style="width: 110px">
              <el-option v-for="grade in gradeOptions" :key="grade" :label="`${grade}年级`" :value="grade" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="exemptionFilters.status" clearable style="width: 120px">
              <el-option label="待审核" value="PENDING" />
              <el-option label="通过" value="PASS" />
              <el-option label="不通过" value="FAIL" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="exemptionFilters.keyword" clearable placeholder="学校或教师" @keyup.enter="reloadExemptions" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="reloadExemptions">查询</el-button>
            <el-button @click="resetExemptionFilters">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card shadow="never" v-loading="exemptionLoading">
        <el-table :data="exemptionRows" border stripe>
          <el-table-column label="学校" prop="deptName" min-width="170" fixed />
          <el-table-column label="教师" prop="teacherName" width="110" />
          <el-table-column label="学年学期" min-width="185">
            <template #default="{ row }">
              {{ row.academicYear }}-{{ Number(row.academicYear) + 1 }}学年 第{{ row.semester }}学期
            </template>
          </el-table-column>
          <el-table-column label="年级" width="80"><template #default="{ row }">{{ row.grade }}年级</template></el-table-column>
          <el-table-column label="班级数" prop="classCount" width="80" />
          <el-table-column label="各班平台使用摘要" prop="classUsageSummary" min-width="260" show-overflow-tooltip />
          <el-table-column label="操作题批改率" width="125">
            <template #default="{ row }">{{ rateText(row.practicalRate, '暂无提交') }}</template>
          </el-table-column>
          <el-table-column label="教师备注" prop="teacherRemark" min-width="180" show-overflow-tooltip />
          <el-table-column label="申请时间" min-width="165">
            <template #default="{ row }">{{ formatDeadlineTime(row.submitTime) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="95">
            <template #default="{ row }"><el-tag :type="exemptionStatusMeta(row.status).type">{{ exemptionStatusMeta(row.status).label }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="90" fixed="right">
            <template #default="{ row }"><el-button link type="primary" @click="openExemptionDetail(row.applicationId)">查看</el-button></template>
          </el-table-column>
        </el-table>
        <pagination
          v-show="exemptionTotal > 0"
          :total="exemptionTotal"
          v-model:page="exemptionPage.pageNum"
          v-model:limit="exemptionPage.pageSize"
          @pagination="loadExemptions"
        />
      </el-card>
    </div>

    <el-dialog v-model="exemptionDetailVisible" title="免抽测申请详情" width="88%">
      <template v-if="exemptionDetail">
        <el-descriptions :column="4" border>
          <el-descriptions-item label="学校">{{ exemptionDetail.deptName }}</el-descriptions-item>
          <el-descriptions-item label="教师">{{ exemptionDetail.teacherName }}</el-descriptions-item>
          <el-descriptions-item label="年级">{{ exemptionDetail.grade }}年级</el-descriptions-item>
          <el-descriptions-item label="状态">{{ exemptionStatusMeta(exemptionDetail.status).label }}</el-descriptions-item>
          <el-descriptions-item label="教师总备注" :span="4">{{ exemptionDetail.teacherRemark || '无' }}</el-descriptions-item>
          <el-descriptions-item label="审核备注" :span="4">{{ exemptionDetail.reviewRemark || '尚未审核' }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="exemptionDetail.classes" border class="detail-section">
          <el-table-column type="expand">
            <template #default="{ row }">
              <el-table :data="exemptionCoursesForClass(row)" border size="small">
                <el-table-column label="使用日期" min-width="160"><template #default="{ row: course }">{{ formatUsageTime(course.usageDate) }}</template></el-table-column>
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
        <div v-if="exemptionDetail.attachments?.length" class="detail-section">
          <strong>证明附件：</strong>
          <el-link
            v-for="item in exemptionDetail.attachments"
            :key="item.attachmentId"
            type="primary"
            :href="attachmentUrl(item.resourcePath)"
            target="_blank"
            class="attachment-link"
          >
            {{ item.originalFileName }}
          </el-link>
        </div>
        <el-form v-if="exemptionDetail.status === 'PENDING'" label-width="90px" class="detail-section">
          <el-form-item label="审核备注">
            <el-input v-model="reviewForm.reviewRemark" type="textarea" :rows="3" maxlength="1000" show-word-limit />
          </el-form-item>
          <el-form-item>
            <el-button type="success" :loading="reviewSaving" @click="submitReview('PASS')">通过</el-button>
            <el-button type="danger" :loading="reviewSaving" @click="submitReview('FAIL')">不通过</el-button>
          </el-form-item>
        </el-form>
      </template>
    </el-dialog>

    <el-dialog v-model="standardDialogVisible" title="每班应使用课数设置" width="680px">
      <el-form inline>
        <el-form-item label="学年">
          <el-select v-model="standardPeriod.academicYear" style="width: 150px">
            <el-option v-for="year in academicYears" :key="year" :label="`${year}-${Number(year) + 1}学年`" :value="year" />
          </el-select>
        </el-form-item>
        <el-form-item label="学期">
          <el-select v-model="standardPeriod.semester" style="width: 120px">
            <el-option label="第一学期" value="1" />
            <el-option label="第二学期" value="2" />
          </el-select>
        </el-form-item>
        <el-button @click="loadStandards">读取</el-button>
      </el-form>
      <el-table :data="standardRows" border>
        <el-table-column label="年级" width="140"><template #default="{ row }">{{ row.grade }}年级</template></el-table-column>
        <el-table-column label="每班应使用课数">
          <template #default="{ row }"><el-input-number v-model="row.requiredLessonCount" :min="1" :max="100" /></template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }"><el-button link type="primary" @click="saveStandardRow(row)">保存</el-button></template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-drawer v-model="courseDrawer" :title="courseTitle" size="82%">
      <el-tabs v-model="courseTab">
        <el-tab-pane label="班级参与与批改" name="classes">
          <el-table :data="classRows" border v-loading="classLoading">
            <el-table-column label="班级" width="120">
              <template #default="{ row }">{{ row.entryYear }}级{{ row.classCode }}班</template>
            </el-table-column>
            <el-table-column label="当前指派" width="90">
              <template #default="{ row }"><el-tag :type="row.currentAssigned ? 'success' : 'info'">{{ row.currentAssigned ? '是' : '历史' }}</el-tag></template>
            </el-table-column>
            <el-table-column label="参与/总人数" width="120">
              <template #default="{ row }">{{ row.participantCount }}/{{ row.totalStudentCount }}</template>
            </el-table-column>
            <el-table-column label="使用日期" min-width="165">
              <template #default="{ row }">{{ formatUsageTime(row.usageDate) }}</template>
            </el-table-column>
            <el-table-column label="应批/已批/未批" width="145">
              <template #default="{ row }">{{ row.practicalDueCount }}/{{ row.practicalGradedCount }}/{{ row.practicalUngradedCount }}</template>
            </el-table-column>
            <el-table-column label="状态" width="110">
              <template #default="{ row }"><el-tag :type="deadlineStatusMeta(row.statusCode).type">{{ deadlineStatusMeta(row.statusCode).label }}</el-tag></template>
            </el-table-column>
            <el-table-column label="触发时间" min-width="170">
              <template #default="{ row }">{{ formatDeadlineTime(row.triggerTime) }}</template>
            </el-table-column>
            <el-table-column label="当前截止" min-width="170">
              <template #default="{ row }">{{ formatDeadlineTime(row.currentDeadlineTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" min-width="210" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openStudents(row)">学生成绩</el-button>
                <el-button
                  v-if="row.deadlineId"
                  v-hasPermi="['business:practicalDeadline:adjust']"
                  link
                  type="warning"
                  @click="openAdjust(row)"
                >
                  {{ row.statusCode === 'OVERDUE' ? '重新开放' : '延期' }}
                </el-button>
                <el-button v-if="row.deadlineId" link @click="openAudits(row)">调整记录</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="题目只读详情" name="questions">
          <el-table :data="questionRows" border>
            <el-table-column label="序号" prop="orderNum" width="70" />
            <el-table-column label="题型" width="110">
              <template #default="{ row }">{{ questionTypeLabel(row.questionType) }}</template>
            </el-table-column>
            <el-table-column label="题干" prop="questionContent" min-width="360" show-overflow-tooltip />
            <el-table-column label="满分" prop="maxScore" width="80" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-drawer>

    <el-drawer v-model="studentDrawer" :title="studentTitle" size="76%">
      <div class="drawer-tools">
        <el-input v-model="studentKeyword" clearable placeholder="姓名或学号" style="width: 220px" @keyup.enter="loadStudents" />
        <el-button type="primary" @click="loadStudents">查询</el-button>
        <el-button
          v-hasPermi="['business:teachingSupervision:export']"
          @click="exportStudents"
        >
          导出本班学生明细
        </el-button>
      </div>
      <el-table :data="studentRows" border v-loading="studentLoading">
        <el-table-column label="姓名" prop="studentName" min-width="100" />
        <el-table-column label="学号" prop="studentNo" min-width="130" />
        <el-table-column label="提交" width="80">
          <template #default="{ row }">{{ row.submitted ? '已提交' : '未提交' }}</template>
        </el-table-column>
        <el-table-column label="作业分" prop="homeworkScore" width="85" />
        <el-table-column label="课堂表现" prop="performanceScore" width="90" />
        <el-table-column label="课程总成绩" prop="finalScore" width="100" />
        <el-table-column label="应批/已批/未批" width="145">
          <template #default="{ row }">{{ row.practicalDueCount }}/{{ row.practicalGradedCount }}/{{ row.practicalUngradedCount }}</template>
        </el-table-column>
        <el-table-column label="操作题状态" width="110">
          <template #default="{ row }">{{ practicalStudentStatus(row.practicalStatus) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }"><el-button link type="primary" @click="openAnswers(row)">查看提交</el-button></template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="studentTotal > 0"
        :total="studentTotal"
        v-model:page="studentPage.pageNum"
        v-model:limit="studentPage.pageSize"
        @pagination="loadStudents"
      />
    </el-drawer>

    <el-dialog v-model="answerDialog" title="操作题提交只读详情" width="760px">
      <el-table :data="answerRows" border>
        <el-table-column label="题目ID" prop="questionId" width="90" />
        <el-table-column label="提交内容/附件" min-width="320" show-overflow-tooltip>
          <template #default="{ row }">
            <a v-if="row.studentAnswer" :href="resourceUrl(row.studentAnswer)" target="_blank">{{ row.studentAnswer }}</a>
            <span v-else>未提交</span>
          </template>
        </el-table-column>
        <el-table-column label="预览" width="80">
          <template #default="{ row }"><a v-if="row.previewPath" :href="viewUrl(row.previewPath)" target="_blank">查看</a><span v-else>--</span></template>
        </el-table-column>
        <el-table-column label="成绩" width="80">
          <template #default="{ row }">{{ row.score === null || row.score === undefined ? '未批改' : `${row.score}分` }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="configDialog" title="全局操作题批改期限" width="460px">
      <el-alert title="新配置只影响以后首次触发的课程班级，已生成期限不会自动变化。" type="info" :closable="false" />
      <el-form label-width="120px" class="dialog-form">
        <el-form-item label="期限天数">
          <el-input-number v-model="deadlineDays" :min="1" :max="365" />
          <span class="muted">自然日</span>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="configDialog = false">取消</el-button><el-button type="primary" @click="saveConfig">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="adjustDialog" :title="adjustTitle" width="520px">
      <el-form label-width="100px">
        <el-form-item label="新截止时间">
          <el-date-picker v-model="adjustForm.newDeadlineTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="调整原因">
          <el-input v-model="adjustForm.reason" type="textarea" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="adjustDialog = false">取消</el-button><el-button type="primary" @click="saveAdjust">确认</el-button></template>
    </el-dialog>

    <el-dialog v-model="auditDialog" title="批改期限调整记录" width="760px">
      <el-table :data="auditRows" border>
        <el-table-column label="类型" prop="actionType" width="90" />
        <el-table-column label="原截止" min-width="170"><template #default="{ row }">{{ formatDeadlineTime(row.oldDeadlineTime) }}</template></el-table-column>
        <el-table-column label="新截止" min-width="170"><template #default="{ row }">{{ formatDeadlineTime(row.newDeadlineTime) }}</template></el-table-column>
        <el-table-column label="原因" prop="reason" min-width="180" />
        <el-table-column label="操作人" prop="operatorName" width="100" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="TeachingSupervision">
import { getCurrentInstance, onMounted, reactive, ref } from 'vue'
import {
  listSupervisionSchools,
  listSupervisionTeachers,
  listSupervisionCourses,
  listSupervisionTimeline,
  listSupervisionClasses,
  listSupervisionStudents,
  listSupervisionQuestions,
  listPracticalAnswerDetails,
  getDeadlineConfig,
  updateDeadlineConfig,
  adjustPracticalDeadline,
  listDeadlineAudits
} from '@/api/business/schoolScore'
import {
  listExemptionReviews,
  getExemptionDetail,
  reviewExemption,
  listExemptionStandards,
  saveExemptionStandard
} from '@/api/business/exemption'
import { deadlineStatusMeta, formatDeadlineTime } from '@/utils/practicalDeadline'
import { resolveAcademicSemester, resolveAcademicStartYear } from '@/utils/academicYear'
import { questionTypeLabel } from '@/utils/questionType'

const { proxy } = getCurrentInstance()
const currentAcademicYear = String(resolveAcademicStartYear())
const currentSemester = resolveAcademicSemester()
const academicYears = [String(Number(currentAcademicYear) - 2), String(Number(currentAcademicYear) - 1), currentAcademicYear]
const gradeOptions = Array.from({ length: 9 }, (_, index) => index + 1)
const statusOptions = [
  { label: '待批改', value: 'WAITING' },
  { label: '已批改', value: 'COMPLETED' },
  { label: '已逾期', value: 'OVERDUE' }
]
const mainTab = ref('supervision')
const viewMode = ref('timeline')
const filtersExpanded = ref(true)
const usageDateRange = ref([])
const filters = reactive({
  academicYear: currentAcademicYear,
  semester: 'all',
  grade: '',
  classCode: '',
  usageSort: 'desc',
  keyword: '',
  lessonMode: '',
  hasPractical: null,
  statusCode: ''
})
const level = ref('school')
const selectedSchool = ref(null)
const selectedTeacher = ref(null)
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const page = reactive({ pageNum: 1, pageSize: 10 })

function baseQuery() {
  return {
    ...filters,
    usageStartDate: usageDateRange.value?.[0],
    usageEndDate: usageDateRange.value?.[1],
    pageNum: page.pageNum,
    pageSize: page.pageSize,
    deptId: selectedSchool.value?.deptId,
    teacherId: selectedTeacher.value?.teacherId
  }
}

async function loadCurrent() {
  loading.value = true
  try {
    const api = viewMode.value === 'timeline'
      ? listSupervisionTimeline
      : level.value === 'school'
        ? listSupervisionSchools
        : level.value === 'teacher' ? listSupervisionTeachers : listSupervisionCourses
    const res = await api(baseQuery())
    rows.value = res.rows || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function handleViewModeChange() {
  page.pageNum = 1
  loadCurrent()
}

function simpleGradingMeta(row) {
  const due = Number(row.practicalDueCount || 0)
  const ungraded = Number(row.practicalUngradedCount || 0)
  if (due === 0) return { label: '暂无提交', type: 'info' }
  if (ungraded === 0) return { label: '已批改', type: 'success' }
  if (row.statusCode === 'OVERDUE') return { label: '已逾期', type: 'danger' }
  return { label: '待批改', type: 'warning' }
}

function reloadCurrent() {
  page.pageNum = 1
  loadCurrent()
}

function resetFilters() {
  Object.assign(filters, {
    academicYear: currentAcademicYear,
    semester: 'all',
    grade: '',
    classCode: '',
    usageSort: 'desc',
    keyword: '',
    lessonMode: '',
    hasPractical: null,
    statusCode: ''
  })
  usageDateRange.value = []
  reloadCurrent()
}

function formatUsageTime(value) {
  return value ? formatDeadlineTime(value) : '暂无真实使用记录'
}

function formatCourseCreateTime(value) {
  return value ? `创建：${formatDeadlineTime(value)}` : '创建：历史数据未记录'
}

function formatCourseUpdateTime(value) {
  return value ? `修改：${formatDeadlineTime(value)}` : '修改：暂无记录'
}

const exemptionFilters = reactive({
  academicYear: currentAcademicYear,
  semester: currentSemester,
  grade: '',
  status: '',
  keyword: ''
})
const exemptionRows = ref([])
const exemptionTotal = ref(0)
const exemptionLoading = ref(false)
const exemptionLoaded = ref(false)
const exemptionPage = reactive({ pageNum: 1, pageSize: 10 })
const exemptionDetailVisible = ref(false)
const exemptionDetail = ref(null)
const reviewForm = reactive({ reviewRemark: '' })
const reviewSaving = ref(false)
const standardDialogVisible = ref(false)
const standardPeriod = reactive({
  academicYear: currentAcademicYear,
  semester: currentSemester
})
const standardRows = ref([])

function handleMainTabChange(name) {
  if (name === 'exemption' && !exemptionLoaded.value) {
    loadExemptions()
  }
}

async function loadExemptions() {
  exemptionLoading.value = true
  try {
    const res = await listExemptionReviews({
      ...exemptionFilters,
      grade: exemptionFilters.grade || undefined,
      pageNum: exemptionPage.pageNum,
      pageSize: exemptionPage.pageSize
    })
    exemptionRows.value = res.rows || []
    exemptionTotal.value = res.total || 0
    exemptionLoaded.value = true
  } finally {
    exemptionLoading.value = false
  }
}

function reloadExemptions() {
  exemptionPage.pageNum = 1
  loadExemptions()
}

function resetExemptionFilters() {
  Object.assign(exemptionFilters, {
    academicYear: currentAcademicYear,
    semester: currentSemester,
    grade: '',
    status: '',
    keyword: ''
  })
  reloadExemptions()
}

function exemptionStatusMeta(status) {
  return {
    PENDING: { label: '待审核', type: 'warning' },
    PASS: { label: '通过', type: 'success' },
    FAIL: { label: '不通过', type: 'danger' }
  }[status] || { label: '未知', type: 'info' }
}

function rateText(value, empty = '--') {
  return value === null || value === undefined ? empty : `${value}%`
}

async function openExemptionDetail(applicationId) {
  const res = await getExemptionDetail(applicationId)
  exemptionDetail.value = res.data || null
  reviewForm.reviewRemark = ''
  exemptionDetailVisible.value = true
}

function exemptionCoursesForClass(classRow) {
  const courses = exemptionDetail.value?.courses || []
  return courses.filter(item => Number(item.classSnapshotId) === Number(classRow.classSnapshotId))
}

function attachmentUrl(path) {
  return resourceUrl(path)
}

async function submitReview(status) {
  if (!exemptionDetail.value) return
  const action = status === 'PASS' ? '通过' : '不通过'
  await proxy.$modal.confirm(`确认将该申请审核为“${action}”吗？`)
  reviewSaving.value = true
  try {
    const res = await reviewExemption(exemptionDetail.value.applicationId, {
      status,
      reviewRemark: reviewForm.reviewRemark,
      version: exemptionDetail.value.version
    })
    exemptionDetail.value = res.data
    proxy.$modal.msgSuccess(`审核结果已保存：${action}`)
    await loadExemptions()
  } finally {
    reviewSaving.value = false
  }
}

async function openStandardDialog() {
  standardDialogVisible.value = true
  await loadStandards()
}

async function loadStandards() {
  const res = await listExemptionStandards(standardPeriod)
  const saved = new Map((res.data || []).map(item => [Number(item.grade), Number(item.requiredLessonCount)]))
  standardRows.value = gradeOptions.map(grade => ({
    grade,
    requiredLessonCount: saved.get(grade) || 15
  }))
}

async function saveStandardRow(row) {
  await saveExemptionStandard({
    academicYear: standardPeriod.academicYear,
    semester: standardPeriod.semester,
    grade: row.grade,
    requiredLessonCount: row.requiredLessonCount
  })
  proxy.$modal.msgSuccess(`${row.grade}年级应使用课数已保存`)
}

function goLevel(target) {
  level.value = target
  if (target === 'school') {
    selectedSchool.value = null
    selectedTeacher.value = null
  } else if (target === 'teacher') {
    selectedTeacher.value = null
  }
  reloadCurrent()
}

function openTeachers(row) {
  selectedSchool.value = row
  selectedTeacher.value = null
  level.value = 'teacher'
  reloadCurrent()
}

function openCourses(row) {
  selectedTeacher.value = row
  level.value = 'course'
  reloadCurrent()
}

const courseDrawer = ref(false)
const courseTab = ref('classes')
const currentCourse = ref(null)
const classRows = ref([])
const classLoading = ref(false)
const questionRows = ref([])
const courseTitle = ref('')

async function openCourseDetail(row) {
  currentCourse.value = row
  courseTitle.value = `${row.lessonTitle} · 课程与班级事实`
  courseDrawer.value = true
  courseTab.value = 'classes'
  classLoading.value = true
  try {
    const query = { ...baseQuery(), lessonId: row.lessonId, pageNum: 1, pageSize: 200 }
    const [classes, questions] = await Promise.all([
      listSupervisionClasses(query),
      listSupervisionQuestions({
        lessonId: row.lessonId,
        academicYear: filters.academicYear,
        semester: filters.semester
      })
    ])
    classRows.value = classes.rows || []
    questionRows.value = questions.data || []
  } finally {
    classLoading.value = false
  }
}

const studentDrawer = ref(false)
const currentClass = ref(null)
const studentTitle = ref('')
const studentRows = ref([])
const studentTotal = ref(0)
const studentLoading = ref(false)
const studentKeyword = ref('')
const studentPage = reactive({ pageNum: 1, pageSize: 20 })

function openStudents(row) {
  currentClass.value = row
  studentTitle.value = `${currentCourse.value.lessonTitle} · ${row.entryYear}级${row.classCode}班学生成绩`
  studentDrawer.value = true
  studentKeyword.value = ''
  studentPage.pageNum = 1
  loadStudents()
}

async function loadStudents() {
  if (!currentCourse.value || !currentClass.value) return
  studentLoading.value = true
  try {
    const res = await listSupervisionStudents({
      ...filters,
      deptId: currentClass.value.deptId,
      lessonId: currentCourse.value.lessonId,
      entryYear: currentClass.value.entryYear,
      classCode: currentClass.value.classCode,
      keyword: studentKeyword.value,
      pageNum: studentPage.pageNum,
      pageSize: studentPage.pageSize
    })
    studentRows.value = res.rows || []
    studentTotal.value = res.total || 0
  } finally {
    studentLoading.value = false
  }
}

const answerDialog = ref(false)
const answerRows = ref([])
async function openAnswers(student) {
  const res = await listPracticalAnswerDetails({
    academicYear: filters.academicYear,
    semester: filters.semester,
    lessonId: currentCourse.value.lessonId,
    deptId: currentClass.value.deptId,
    entryYear: currentClass.value.entryYear,
    classCode: currentClass.value.classCode,
    studentId: student.studentId
  })
  answerRows.value = res.data || []
  answerDialog.value = true
}

function resourceUrl(path) {
  return `${import.meta.env.VITE_APP_BASE_API}/common/download/resource?resource=${encodeURIComponent(path)}`
}
function viewUrl(path) {
  return `${import.meta.env.VITE_APP_BASE_API}/common/resource/view?resource=${encodeURIComponent(path)}`
}
function practicalStudentStatus(status) {
  return { NOT_SUBMITTED: '未提交', UNGRADED: '未批改', GRADED: '已批改' }[status] || '--'
}

const configDialog = ref(false)
const deadlineDays = ref(21)
async function openConfig() {
  const res = await getDeadlineConfig()
  deadlineDays.value = Number(res.deadlineDays ?? res.data?.deadlineDays ?? 21)
  configDialog.value = true
}
async function saveConfig() {
  await updateDeadlineConfig(deadlineDays.value)
  proxy.$modal.msgSuccess('配置已更新')
  configDialog.value = false
}

const adjustDialog = ref(false)
const adjustTitle = ref('')
const adjustRow = ref(null)
const adjustForm = reactive({ newDeadlineTime: '', reason: '' })
function openAdjust(row) {
  adjustRow.value = row
  adjustTitle.value = row.statusCode === 'OVERDUE' ? '重新开放操作题批改' : '延期操作题批改'
  adjustForm.newDeadlineTime = ''
  adjustForm.reason = ''
  adjustDialog.value = true
}
async function saveAdjust() {
  if (!adjustForm.newDeadlineTime || !adjustForm.reason.trim()) {
    proxy.$modal.msgWarning('请填写新截止时间和调整原因')
    return
  }
  await adjustPracticalDeadline(adjustRow.value.deadlineId, adjustForm)
  proxy.$modal.msgSuccess('批改期限已调整')
  adjustDialog.value = false
  openCourseDetail(currentCourse.value)
}

const auditDialog = ref(false)
const auditRows = ref([])
async function openAudits(row) {
  const res = await listDeadlineAudits(row.deadlineId)
  auditRows.value = res.data || []
  auditDialog.value = true
}

function exportCurrent() {
  const endpoint = level.value === 'school' ? 'schools' : 'courses'
  proxy.download(`/business/schoolScore/export/${endpoint}`, baseQuery(), `课程监管-${endpoint}-${Date.now()}.csv`)
}
function exportStudents() {
  proxy.download('/business/schoolScore/export/students', {
    ...filters,
    deptId: currentClass.value.deptId,
    lessonId: currentCourse.value.lessonId,
    entryYear: currentClass.value.entryYear,
    classCode: currentClass.value.classCode,
    keyword: studentKeyword.value
  }, `学生成绩明细-${Date.now()}.csv`)
}

onMounted(loadCurrent)
</script>

<style scoped>
.mode-card { margin-bottom: 12px; }
.filter-card { margin-bottom: 12px; }
.card-header, .level-nav, .drawer-tools { display: flex; align-items: center; gap: 10px; }
.card-header { justify-content: space-between; }
.scope-note { margin-left: 12px; color: #909399; font-size: 13px; }
.level-nav { margin: 8px 0 12px; color: #909399; }
.muted { color: #909399; margin-left: 8px; font-size: 12px; }
.drawer-tools { margin-bottom: 12px; }
.dialog-form { margin-top: 20px; }
.detail-section { margin-top: 16px; }
.attachment-link { margin-left: 12px; }
.inline-count { margin-left: 8px; color: #606266; font-size: 12px; }
</style>
