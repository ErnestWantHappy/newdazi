<template>
  <div class="app-container classroom-desktop">
    <!-- 顶部主工具栏与控制中心 -->
    <div class="desktop-toolbar">
      <div class="title-section">
        <div class="main-title-row">
          <h2 class="page-title">{{ lessonTitle || `${entryYear}级${classCode}班` }} · 课堂监控大屏</h2>
          <el-tag v-if="historical" type="warning" effect="dark" size="small" class="history-tag">历史数据（只读）</el-tag>
        </div>
        <p class="subtitle-hint">
          <el-icon><Monitor /></el-icon>
          <span>终端在线与作答进度分别统计，在线状态不等同于考勤</span>
        </p>
      </div>

      <div class="toolbar-actions">
        <!-- 课堂常驻发令闸：一键开启理论题与操作题 -->
        <div v-if="lessonId && (hasTheory || hasPractical)" class="gate-launcher">
          <span class="gate-launcher-label">🚩 题目发令：</span>
          <div v-if="hasTheory" class="gate-switch-item" title="切换当前班级理论测试题的学生可见状态">
            <span class="gate-item-title">理论题</span>
            <el-switch
              v-model="gateTheoryOpen"
              :disabled="historical"
              size="small"
              :loading="gateSaving === 'theory'"
              active-text="开"
              inactive-text="关"
              inline-prompt
              @change="(val) => handleGateToggle('theory', val)"
            />
          </div>
          <div v-if="hasPractical" class="gate-switch-item" title="切换当前班级操作题的学生可见状态">
            <span class="gate-item-title">操作题</span>
            <el-switch
              v-model="gatePracticalOpen"
              :disabled="historical"
              size="small"
              :loading="gateSaving === 'practical'"
              active-text="开"
              inactive-text="关"
              inline-prompt
              @change="(val) => handleGateToggle('practical', val)"
            />
          </div>
        </div>

        <el-divider v-if="lessonId && (hasTheory || hasPractical)" direction="vertical" class="toolbar-divider" />

        <!-- 每排人数调节器（弹性缩放） -->
        <div class="columns-controller" title="调整每排显示的同学数量，卡片将自适应放大或缩小">
          <span class="ctrl-label">
            <el-icon><Grid /></el-icon>
            每排人数：
          </span>
          <el-button-group size="small" class="cols-preset-group">
            <el-button
              v-for="preset in [4, 6, 8, 10]"
              :key="preset"
              :type="columnsCount === preset ? 'primary' : 'default'"
              @click="setColumns(preset)"
            >
              {{ preset }}列
            </el-button>
          </el-button-group>
          <el-input-number
            v-model="columnsCount"
            :min="3"
            :max="12"
            size="small"
            controls-position="right"
            class="cols-number-input"
            @change="handleColumnsChange"
          />
        </div>

        <el-divider direction="vertical" class="toolbar-divider" />

        <!-- 视图与功能按钮组 -->
        <div class="view-controls">
          <el-switch v-model="showGroups" active-text="分组视图" class="group-switch" />
          <el-button type="primary" plain :icon="Setting" @click="openGroupDialog">设置分组</el-button>
          <el-button :type="layoutMode ? 'warning' : 'default'" :icon="Edit" @click="layoutMode = !layoutMode">
            {{ layoutMode ? '退出调座' : '调整座位' }}
          </el-button>
          <el-button :icon="Refresh" :loading="loading" @click="() => load(false)">刷新</el-button>
        </div>
      </div>
    </div>

    <!-- 课堂状态概览指示条 -->
    <div v-if="students.length > 0" class="classroom-summary-bar">
      <div class="summary-item total">
        <span class="summary-label">全班总人数</span>
        <span class="summary-num">{{ students.length }}</span>
      </div>
      <div class="summary-item online">
        <span class="summary-dot is-online" />
        <span class="summary-label">当前已连接</span>
        <span class="summary-num">{{ onlineStudentCount }}</span>
      </div>
      <div class="summary-item offline">
        <span class="summary-dot is-offline" />
        <span class="summary-label">未连接终端</span>
        <span class="summary-num">{{ students.length - onlineStudentCount }}</span>
      </div>
      <div v-if="lessonId" class="summary-item progress">
        <span class="summary-label">已提交作业</span>
        <span class="summary-num">{{ submittedStudentCount }}</span>
      </div>
      <div v-if="absentStudentCount > 0" class="summary-item absent">
        <span class="summary-label">本节请假</span>
        <span class="summary-num">{{ absentStudentCount }}</span>
      </div>
      <div class="summary-scale-hint">
        <span>当前视图：每排 {{ columnsCount }} 人（{{ getScaleLabel() }}）</span>
      </div>
    </div>

    <!-- 异常与空状态 -->
    <el-alert v-if="error" type="warning" :closable="false" :title="error" show-icon class="mb12" />
    <el-empty v-if="!loading && !students.length" description="当前班级暂无学生数据" />

    <!-- 核心学生流式展示区（Flex 响应式流式布局） -->
    <div v-loading="loading" class="student-monitor-container">
      <!-- 模式一：分组流式展示 -->
      <template v-if="showGroups">
        <section v-for="group in groupedStudents" :key="group.key" class="group-cluster-box">
          <div class="group-cluster-header">
            <div class="group-cluster-title">
              <span class="group-indicator-color" :style="{ backgroundColor: group.color || '#409EFF' }" />
              <strong class="group-name-text">{{ group.name }}</strong>
              <el-tag size="small" type="info" effect="plain" class="group-count-tag">{{ group.students.length }} 人</el-tag>
            </div>
            <div v-if="group.leaderName" class="group-cluster-leader-chip">
              <span class="leader-icon">👑</span>
              <span class="leader-text">本组组长：<strong>{{ group.leaderName }}</strong></span>
            </div>
          </div>

          <!-- 组内卡片 Flex 流式包裹器 -->
          <div class="flex-fluid-grid">
            <div
              v-for="student in group.students"
              :key="student.studentId"
              class="fluid-card-wrapper"
              :style="cardFluidStyle"
            >
              <div
                class="student-card-item"
                :class="[
                  student.online ? 'is-connected' : 'is-disconnected',
                  { 'is-leader': student.leaderStudentId === student.studentId },
                  { 'is-absent': student.performance?.isAbsent },
                  cardDensityClass
                ]"
                :draggable="layoutMode"
                @dragstart="dragStart(student)"
                @dragover.prevent
                @drop="drop(student)"
              >
                <!-- 组长金色角标 -->
                <div v-if="student.leaderStudentId === student.studentId" class="card-leader-ribbon" title="本组组长">
                  👑 组长
                </div>

                <!-- 卡片头部：状态灯、姓名学号、连接状态 -->
                <div class="card-top-bar">
                  <div class="student-identity">
                    <span
                      class="presence-beacon"
                      :class="student.online ? 'beacon-online' : 'beacon-offline'"
                      :title="student.online ? '终端已连接' : '未连接'"
                    />
                    <div class="name-box">
                      <span class="student-no-badge">#{{ student.studentNo }}</span>
                      <strong class="student-name" :title="student.studentName">{{ student.studentName }}</strong>
                    </div>
                  </div>
                  <el-tag
                    size="small"
                    :type="student.online ? 'success' : 'info'"
                    effect="light"
                    class="conn-status-tag"
                  >
                    {{ student.online ? '在线' : '离线' }}
                  </el-tag>
                </div>

                <!-- 作答状态胶囊（课程模式下） -->
                <div v-if="lessonId" class="card-task-capsule">
                  <el-tag size="small" :type="taskTagType(student.taskState)" effect="plain" class="task-state-tag">
                    {{ taskLabel(student.taskState) }}
                  </el-tag>
                  <span v-if="student.totalQuestionCount > 0" class="task-progress-text">
                    {{ student.startedQuestionCount || 0 }}/{{ student.totalQuestionCount }} 题已开
                  </span>
                </div>

                <!-- 多维度成绩微指标矩阵 -->
                <div class="card-metrics-matrix">
                  <div v-if="hasTyping" class="metric-pill typing" title="打字成绩">
                    <span class="pill-icon">⚡</span>
                    <span class="pill-title">打字</span>
                    <span class="pill-value">{{ scoreLabel(student.typing?.score) }}分</span>
                    <span class="pill-sub">({{ scoreLabel(student.typing?.speed) }}字/分)</span>
                  </div>
                  <div v-if="hasTheory" class="metric-pill theory" title="理论成绩">
                    <span class="pill-icon">📝</span>
                    <span class="pill-title">理论</span>
                    <span class="pill-value">{{ scoreLabel(student.theory?.score) }}分</span>
                    <span class="pill-sub">({{ scoreLabel(student.theory?.accuracy) }}%)</span>
                  </div>
                  <div v-if="hasPractical" class="metric-pill practical" title="操作题成绩">
                    <span class="pill-icon">💻</span>
                    <span class="pill-title">操作</span>
                    <span class="pill-value">{{ practicalShortLabel(student.practical) }}</span>
                  </div>
                  <div class="metric-pill performance" title="课堂表现分">
                    <span class="pill-icon">⭐</span>
                    <span class="pill-title">表现</span>
                    <span class="pill-value" :class="{ 'has-score': student.performance?.score !== 0 }">
                      {{ student.performance?.score > 0 ? `+${student.performance.score}` : (student.performance?.score || 0) }}分
                    </span>
                  </div>
                </div>

                <!-- 终端信息与备注 -->
                <div class="card-meta-line">
                  <span class="ip-address" :title="`终端 IP: ${student.connectionIp || '未连接'}`">
                    IP: {{ student.connectionIp ? formatIp(student.connectionIp) : '未接入' }}
                  </span>
                  <span v-if="student.remark" class="student-remark" :title="student.remark">
                    注: {{ student.remark }}
                  </span>
                </div>

                <!-- 请假标记横条 -->
                <div v-if="student.performance?.isAbsent" class="card-absent-banner">
                  <span>🏖️ 本节课已请假</span>
                </div>

                <!-- 卡片底部快捷操作 -->
                <div class="card-footer-actions">
                  <el-button size="small" type="primary" plain class="action-mini-btn" @click="openPerformance(student)">
                    课堂表现
                  </el-button>
                  <el-button
                    size="small"
                    :type="student.performance?.isAbsent ? 'warning' : 'info'"
                    plain
                    class="action-mini-btn"
                    @click="toggleAbsent(student)"
                  >
                    {{ student.performance?.isAbsent ? '取消请假' : '请假' }}
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </section>
      </template>

      <!-- 模式二：普通全班流式展示 -->
      <template v-else>
        <div class="flex-fluid-grid">
          <div
            v-for="student in orderedStudents"
            :key="student.studentId"
            class="fluid-card-wrapper"
            :style="cardFluidStyle"
          >
            <div
              class="student-card-item"
              :class="[
                student.online ? 'is-connected' : 'is-disconnected',
                { 'is-leader': student.leaderStudentId === student.studentId },
                { 'is-absent': student.performance?.isAbsent },
                cardDensityClass
              ]"
              :draggable="layoutMode"
              @dragstart="dragStart(student)"
              @dragover.prevent
              @drop="drop(student)"
            >
              <!-- 组长专属流金角标 -->
              <div v-if="student.leaderStudentId === student.studentId" class="card-leader-ribbon" title="本班/本组组长">
                👑 组长
              </div>

              <!-- 卡片头部：状态灯、姓名学号、连接状态 -->
              <div class="card-top-bar">
                <div class="student-identity">
                  <span
                    class="presence-beacon"
                    :class="student.online ? 'beacon-online' : 'beacon-offline'"
                    :title="student.online ? '终端已连接' : '未连接'"
                  />
                  <div class="name-box">
                    <span class="student-no-badge">#{{ student.studentNo }}</span>
                    <strong class="student-name" :title="student.studentName">{{ student.studentName }}</strong>
                  </div>
                </div>
                <el-tag
                  size="small"
                  :type="student.online ? 'success' : 'info'"
                  effect="light"
                  class="conn-status-tag"
                >
                  {{ student.online ? '在线' : '离线' }}
                </el-tag>
              </div>

              <!-- 作答状态胶囊（课程模式下） -->
              <div v-if="lessonId" class="card-task-capsule">
                <el-tag size="small" :type="taskTagType(student.taskState)" effect="plain" class="task-state-tag">
                  {{ taskLabel(student.taskState) }}
                </el-tag>
                <span v-if="student.totalQuestionCount > 0" class="task-progress-text">
                  {{ student.startedQuestionCount || 0 }}/{{ student.totalQuestionCount }} 题已开
                </span>
              </div>

              <!-- 多维度成绩微指标矩阵 -->
              <div class="card-metrics-matrix">
                <div v-if="hasTyping" class="metric-pill typing" title="打字成绩">
                  <span class="pill-icon">⚡</span>
                  <span class="pill-title">打字</span>
                  <span class="pill-value">{{ scoreLabel(student.typing?.score) }}分</span>
                  <span class="pill-sub">({{ scoreLabel(student.typing?.speed) }}字/分)</span>
                </div>
                <div v-if="hasTheory" class="metric-pill theory" title="理论成绩">
                  <span class="pill-icon">📝</span>
                  <span class="pill-title">理论</span>
                  <span class="pill-value">{{ scoreLabel(student.theory?.score) }}分</span>
                  <span class="pill-sub">({{ scoreLabel(student.theory?.accuracy) }}%)</span>
                </div>
                <div v-if="hasPractical" class="metric-pill practical" title="操作题成绩">
                  <span class="pill-icon">💻</span>
                  <span class="pill-title">操作</span>
                  <span class="pill-value">{{ practicalShortLabel(student.practical) }}</span>
                </div>
                <div class="metric-pill performance" title="课堂表现分">
                  <span class="pill-icon">⭐</span>
                  <span class="pill-title">表现</span>
                  <span class="pill-value" :class="{ 'has-score': student.performance?.score !== 0 }">
                    {{ student.performance?.score > 0 ? `+${student.performance.score}` : (student.performance?.score || 0) }}分
                  </span>
                </div>
              </div>

              <!-- 终端信息与备注 -->
              <div class="card-meta-line">
                <span class="ip-address" :title="`终端 IP: ${student.connectionIp || '未连接'}`">
                  IP: {{ student.connectionIp ? formatIp(student.connectionIp) : '未接入' }}
                </span>
                <span v-if="student.remark" class="student-remark" :title="student.remark">
                  注: {{ student.remark }}
                </span>
              </div>

              <!-- 请假标记横条 -->
              <div v-if="student.performance?.isAbsent" class="card-absent-banner">
                <span>🏖️ 本节课已请假</span>
              </div>

              <!-- 卡片底部快捷操作 -->
              <div class="card-footer-actions">
                <el-button size="small" type="primary" plain class="action-mini-btn" @click="openPerformance(student)">
                  课堂表现
                </el-button>
                <el-button
                  size="small"
                  :type="student.performance?.isAbsent ? 'warning' : 'info'"
                  plain
                  class="action-mini-btn"
                  @click="toggleAbsent(student)"
                >
                  {{ student.performance?.isAbsent ? '取消请假' : '请假' }}
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- 座位调整保存浮动底栏 -->
    <div v-if="layoutMode" class="layout-footer-bar">
      <div class="footer-tip">
        <el-icon><Operation /></el-icon>
        <span>已进入座位调整模式：按住同学卡片拖动可调换顺序，每排按当前设置的 {{ columnsCount }} 人排列。调整完成后请保存。</span>
      </div>
      <div class="footer-btns">
        <el-button size="small" @click="layoutMode = false">取消退出</el-button>
        <el-button type="primary" size="small" @click="saveLayout">保存当前布局与列数</el-button>
      </div>
    </div>

    <!-- “设置分组”弹窗（最后一列明确显示组长） -->
    <el-dialog
      v-model="groupDialogVisible"
      title="班级固定分组设置"
      width="min(1080px, 96vw)"
      destroy-on-close
      class="custom-group-dialog"
    >
      <el-alert
        title="本班固定分组保存后在所有课程和学生桌面中共用；默认每组4人，与在线协作分组独立。"
        type="info"
        :closable="false"
        show-icon
        class="mb12"
      />

      <!-- 分组生成配置栏 -->
      <div class="group-generate-toolbar">
        <div class="form-item">
          <span class="item-label">方案名称：</span>
          <el-input v-model="groupForm.schemeName" placeholder="例如：机房4人座位分组" style="width: 200px" maxlength="100" />
        </div>
        <div class="form-item">
          <span class="item-label">每组人数：</span>
          <el-input-number
            v-model="groupForm.membersPerGroup"
            :min="1"
            :max="students.length || 1"
            controls-position="right"
            style="width: 110px"
          />
        </div>
        <div class="form-item">
          <span class="item-label">规则模式：</span>
          <el-select v-model="groupForm.mode" style="width: 130px">
            <el-option label="按学号连续" value="RANGE" />
            <el-option label="按学号交错" value="INTERLEAVE" />
          </el-select>
        </div>
        <el-button type="primary" :loading="groupLoading" @click="generateGroups">
          重新自动生成预览
        </el-button>
      </div>

      <el-empty v-if="!groupForm.groups.length" description="请先点击自动生成分组，或选择已有方案" />

      <!-- 分组结构化表格：第3列（最后一列）明确标识为“👑 组长专属列” -->
      <div v-if="groupForm.groups.length > 0" class="group-table-box">
        <!-- 表头 -->
        <div class="group-table-header">
          <div class="th-col th-name">分组编号与名称</div>
          <div class="th-col th-members">本组成员选择与名单</div>
          <div class="th-col th-leader">👑 组长（最后一列明确指定）</div>
        </div>

        <!-- 表格行列表 -->
        <div class="group-table-body">
          <div
            v-for="(group, index) in groupForm.groups"
            :key="group.key || index"
            class="group-table-row"
          >
            <!-- 第一列：分组序号与名称 -->
            <div class="td-col td-name">
              <span class="group-seq-badge">第 {{ index + 1 }} 组</span>
              <el-input
                v-model="group.groupName"
                placeholder="分组名称"
                maxlength="50"
                size="small"
                class="group-name-input"
              />
              <span class="member-count-hint">{{ (group.studentIds || []).length }} 人</span>
            </div>

            <!-- 第二列：成员选择与名单 -->
            <div class="td-col td-members">
              <el-select
                v-model="group.studentIds"
                multiple
                filterable
                collapse-tags
                collapse-tags-tooltip
                placeholder="搜索并选择本组学生"
                size="small"
                class="group-member-multiselect"
                @change="() => handleGroupMembersChange(group)"
              >
                <el-option
                  v-for="student in students"
                  :key="student.studentId"
                  :label="`${student.studentNo} ${student.studentName}`"
                  :value="student.studentId"
                />
              </el-select>

              <!-- 成员微标签预览（可直接点击设为组长） -->
              <div class="members-chip-cloud">
                <span v-if="!group.studentIds.length" class="empty-member-text">暂无成员</span>
                <span
                  v-for="sid in group.studentIds"
                  :key="sid"
                  class="member-clickable-chip"
                  :class="{ 'is-current-leader': Number(group.leaderStudentId) === Number(sid) }"
                  title="点击可直接将该同学设为组长"
                  @click="group.leaderStudentId = Number(sid)"
                >
                  <span v-if="Number(group.leaderStudentId) === Number(sid)" class="chip-crown">👑</span>
                  {{ studentLabel(sid) }}
                </span>
              </div>
            </div>

            <!-- 第三列（最后一列）：组长明确标识与选择 -->
            <div class="td-col td-leader">
              <div class="leader-cell-card">
                <div class="leader-select-wrapper">
                  <el-select
                    v-model="group.leaderStudentId"
                    clearable
                    placeholder="请选择指定组长"
                    size="small"
                    class="leader-select-input"
                  >
                    <el-option
                      v-for="studentId in group.studentIds"
                      :key="studentId"
                      :label="studentLabel(studentId)"
                      :value="Number(studentId)"
                    />
                  </el-select>
                </div>

                <!-- 明确高亮显示当前组长身份 -->
                <div v-if="group.leaderStudentId" class="leader-designated-banner">
                  <span class="crown-glow">👑</span>
                  <span class="designated-text">组长：<strong>{{ studentLabel(group.leaderStudentId) }}</strong></span>
                </div>
                <div v-else class="leader-unassigned-banner">
                  <span class="unassigned-hint">⚠️ 尚未指定组长</span>
                  <el-link
                    v-if="group.studentIds.length > 0"
                    type="primary"
                    :underline="false"
                    class="quick-set-link"
                    @click="group.leaderStudentId = Number(group.studentIds[0])"
                  >
                    设首位为组长
                  </el-link>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer-actions">
          <span class="footer-hint">提示：保存后分组在学生桌面与课程监控生效，每位同学需归属且仅归属一个分组。</span>
          <div class="btn-group">
            <el-button @click="groupDialogVisible = false">取消</el-button>
            <el-button
              type="primary"
              :loading="groupSaving"
              :disabled="!groupForm.groups.length"
              @click="saveGroups"
            >
              保存固定分组
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 课堂表现管理弹窗 -->
    <el-dialog v-model="performanceDialogVisible" title="学生课堂表现评定" width="440px" destroy-on-close>
      <div v-if="performanceStudent" class="performance-modal-content">
        <div class="student-target-header">
          <span class="student-avatar-badge">{{ performanceStudent.studentName?.slice(0, 1) }}</span>
          <div class="student-detail-info">
            <div class="title-row">
              <strong>{{ performanceStudent.studentNo }} {{ performanceStudent.studentName }}</strong>
            </div>
            <div class="current-score-text">
              当前表现积分：<span class="score-highlight">{{ performanceStudent.performance?.score || 0 }} 分</span>
            </div>
          </div>
        </div>

        <div class="form-block">
          <label class="form-label">课堂表现分（-10 扣分 ~ +10 加分，正数加分、负数扣分）：</label>
          <PerformanceScoreStepper v-model="performanceForm.score" :min="-10" :max="10" :step="1" />
        </div>

        <div class="form-block">
          <label class="form-label">原因说明（可留空）：</label>
          <el-input
            v-model="performanceForm.reason"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="例如：课堂发言积极、主动协助同学排查问题..."
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="performanceDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="performanceSaving" @click="savePerformanceChange">保存评定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Edit,
  Refresh,
  Setting,
  Grid,
  Monitor,
  Operation
} from '@element-plus/icons-vue'
import {
  getClassroomDesktop,
  getClassroomDesktopOverview,
  saveClassroomLayout,
  getClassGroupSchemes,
  saveClassGroupScheme,
  previewClassGroupScheme
} from '@/api/business/classGrouping'
import { savePerformance } from '@/api/business/classroomPerformance'
import PerformanceScoreStepper from '@/components/PerformanceScoreStepper/index.vue'
import { setStudentAbsent, getLessonGate, setLessonGate } from '@/api/business/score'

const route = useRoute()
// URL 可能被浏览器历史或外部链接写成 lessonId=undefined；不应让该占位文本触发后端类型转换错误。
const rawLessonId = String(route.query.lessonId || '').trim()
const lessonId = /^\d+$/.test(rawLessonId) && Number(rawLessonId) > 0 ? rawLessonId : ''
const lessonTitle = String(route.query.lessonTitle || '')
const entryYear = String(route.query.entryYear || '')
const classCode = String(route.query.classCode || '')
const historical = ref(false) // 未指派班级的历史课只读模式
const loading = ref(false)
const error = ref('')
const students = ref([])
const layout = ref(null)
const layoutMode = ref(false)
const showGroups = ref(false)

// 每排显示同学数量（默认6列，可3~12自由调节，实现流式方框缩放）
const columnsCount = ref(6)

const groupDialogVisible = ref(false)
const groupLoading = ref(false)
const groupSaving = ref(false)
const groupForm = ref({ schemeName: '课堂分组', membersPerGroup: 4, mode: 'RANGE', groups: [] })
const dragging = ref(null)
const hasTyping = ref(false)
const hasTheory = ref(false)
const hasPractical = ref(false)

// 题目发令闸状态与加载控制
const gateTheoryOpen = ref(false)
const gatePracticalOpen = ref(false)
const gateSaving = ref('')

// 课堂表现弹窗状态
const performanceDialogVisible = ref(false)
const performanceStudent = ref(null)
const performanceSaving = ref(false)
const performanceForm = ref({ score: 0, reason: '' })
let presenceRefreshTimer

// 统计数据计算
const onlineStudentCount = computed(() => students.value.filter(s => s.online).length)
const submittedStudentCount = computed(() =>
  students.value.filter(s => s.taskState === 'SUBMITTED' || s.taskState === 'GRADED').length
)
const absentStudentCount = computed(() =>
  students.value.filter(s => s.performance?.isAbsent).length
)

// 学生排序列表
const orderedStudents = computed(() =>
  students.value.slice().sort((a, b) => (a.sortNo ?? 999999) - (b.sortNo ?? 999999))
)

// 分组列表（计算组长名称以便大屏清晰显示）
const groupedStudents = computed(() => {
  const groups = new Map()
  orderedStudents.value.forEach(student => {
    const key = student.groupId ? `group-${student.groupId}` : 'ungrouped'
    if (!groups.has(key)) {
      groups.set(key, {
        key,
        name: student.groupName || '未分组',
        color: student.groupColor,
        leaderStudentId: student.leaderStudentId,
        students: []
      })
    }
    groups.get(key).students.push(student)
  })
  return Array.from(groups.values()).map(g => {
    const leader = g.students.find(s => Number(s.studentId) === Number(s.leaderStudentId))
    return {
      ...g,
      leaderName: leader ? `${leader.studentNo} ${leader.studentName}` : ''
    }
  })
})

/**
 * Flex 弹性流式卡片尺寸动态样式
 * 按照教师设定的每排 columnsCount 计算卡片宽度，自适应放大缩小
 */
const cardFluidStyle = computed(() => {
  const cols = Math.max(3, Math.min(12, Number(columnsCount.value) || 6))
  const gapPx = 12
  // 使用 calc((100% - (cols - 1) * gap) / cols) 精确控制一排显示的卡片数量
  return {
    flex: `0 0 calc((100% - ${(cols - 1) * gapPx}px) / ${cols})`,
    maxWidth: `calc((100% - ${(cols - 1) * gapPx}px) / ${cols})`
  }
})

/**
 * 根据列数不同，给卡片赋予舒适或紧凑样式类，实现方框内字体和间距的视觉梯度
 */
const cardDensityClass = computed(() => {
  const cols = Number(columnsCount.value) || 6
  if (cols <= 4) return 'density-comfortable' // 宽敞大方框：字号大，细节全
  if (cols >= 9) return 'density-compact'     // 紧凑小方框：全屏高密度
  return '' // 标准列数用基础样式（无 density-standard 定义，避免挂空类名）
})

function getScaleLabel() {
  const cols = Number(columnsCount.value) || 6
  if (cols <= 4) return '放大视图'
  if (cols >= 9) return '紧凑缩小视图'
  return '标准视图'
}

/** 调节列数并持久化记忆 */
function setColumns(cols) {
  columnsCount.value = cols
  localStorage.setItem('classroom_monitor_columns', String(cols))
}

function handleColumnsChange(val) {
  if (val) {
    localStorage.setItem('classroom_monitor_columns', String(val))
  }
}

async function fetchLessonGateState() {
  if (!lessonId || !entryYear || !classCode) return
  try {
    const res = await getLessonGate(lessonId, entryYear, classCode)
    const data = res.data || {}
    gateTheoryOpen.value = Boolean(data.theoryOpen)
    gatePracticalOpen.value = Boolean(data.practicalOpen)
  } catch (e) {
    // 静默降级，不阻断大屏主渲染
  }
}

async function handleGateToggle(kind, open) {
  if (historical.value) {
    ElMessage.warning('历史数据只读，不能修改题目开放状态')
    return
  }
  if (!lessonId || !entryYear || !classCode) return
  gateSaving.value = kind
  try {
    await setLessonGate(lessonId, entryYear, classCode, kind, open)
    ElMessage.success(`${kind === 'theory' ? '理论测试题' : '操作题'}已${open ? '开启并向学生开放' : '关闭'}`)
  } catch (e) {
    if (kind === 'theory') gateTheoryOpen.value = !open
    if (kind === 'practical') gatePracticalOpen.value = !open
    ElMessage.error(e?.msg || '设置题目开放状态失败')
  } finally {
    gateSaving.value = ''
  }
}

function loadDesktop(silent = false) {
  if (!entryYear || !classCode) {
    error.value = '缺少班级参数'
    return
  }
  if (loading.value) return
  if (!silent) loading.value = true
  error.value = ''
  const request = lessonId
    ? getClassroomDesktopOverview({ lessonId, entryYear, classCode })
    : getClassroomDesktop({ entryYear, classCode })

  return request
    .then(res => {
      historical.value = !!res.data?.historical
      students.value = res.data?.students || []
      layout.value = res.data?.layout || null
      hasTyping.value = !!res.data?.hasTyping
      hasTheory.value = !!res.data?.hasTheory
      hasPractical.value = !!res.data?.hasPractical

      // 优先采用后端保存的列数设置，其次取本地偏好，最后默认 6
      if (res.data?.layout?.columnsCount && Number(res.data.layout.columnsCount) >= 3) {
        columnsCount.value = Number(res.data.layout.columnsCount)
      } else {
        const cached = localStorage.getItem('classroom_monitor_columns')
        if (cached && !isNaN(Number(cached))) {
          columnsCount.value = Number(cached)
        }
      }

      if (lessonId) {
        fetchLessonGateState()
      }
    })
    .catch(e => {
      error.value = e?.msg || '课堂大屏加载失败'
    })
    .finally(() => {
      loading.value = false
    })
}

function load(silent = false) {
  return loadDesktop(silent)
}

function dragStart(student) {
  if (layoutMode.value) dragging.value = student
}

function drop(target) {
  if (!layoutMode.value || !dragging.value || dragging.value.studentId === target.studentId) return
  const from = students.value.indexOf(dragging.value)
  const to = students.value.indexOf(target)
  const list = students.value.slice()
  const [item] = list.splice(from, 1)
  list.splice(to, 0, item)
  students.value = list
  students.value.forEach((s, i) => {
    s.sortNo = i
  })
  dragging.value = null
}

function saveLayout() {
  const cols = Number(columnsCount.value) || 6
  saveClassroomLayout({
    entryYear,
    classCode,
    columnsCount: cols,
    items: students.value.map((s, i) => ({
      studentId: s.studentId,
      gridRow: Math.floor(i / cols),
      gridCol: i % cols,
      sortNo: i
    }))
  }).then(res => {
    layout.value = res.data?.layout || layout.value
    students.value = res.data?.students || students.value
    layoutMode.value = false
    ElMessage.success(`布局与 ${cols} 列设置已保存`)
  }).catch(() => {
    // 保存失败保持调座模式并提示，避免用户误以为已生效
    ElMessage.error('布局保存失败，请稍后重试')
  })
}

function studentLabel(studentId) {
  const student = students.value.find(item => Number(item.studentId) === Number(studentId))
  return student ? `${student.studentNo} ${student.studentName}` : String(studentId)
}

function formatIp(ip) {
  if (!ip) return ''
  // 简写过长的 IPv6/本地回环
  if (ip === '127.0.0.1' || ip === '0:0:0:0:0:0:0:1') return '本机'
  return ip.replace('::ffff:', '')
}

function normalizeGroups(groups, members = []) {
  const memberMap = members.reduce((result, item) => {
    const groupId = String(item.groupId)
    if (!result[groupId]) result[groupId] = []
    result[groupId].push(Number(item.studentId))
    return result
  }, {})
  return (groups || []).map((group, index) => ({
    key: `group-${group.groupId || index}-${Date.now()}`,
    groupName: group.groupName || `第${index + 1}组`,
    color: group.color || '#409EFF',
    studentIds: (group.studentIds || memberMap[String(group.groupId)] || []).map(Number),
    leaderStudentId: group.leaderStudentId == null ? null : Number(group.leaderStudentId)
  }))
}

async function openGroupDialog() {
  groupDialogVisible.value = true
  groupLoading.value = true
  try {
    const response = await getClassGroupSchemes({ entryYear, classCode })
    const data = response.data || {}
    const latest = (data.schemes || [])[0]
    groupForm.value.membersPerGroup = Math.min(4, Math.max(1, students.value.length || 1))
    if (latest) {
      groupForm.value.schemeName = latest.schemeName || groupForm.value.schemeName
      groupForm.value.groups = normalizeGroups(latest.groups, latest.members)
    } else if (students.value.length) {
      // 没有保存过的固定组：直接给出默认四人连续预览
      const preview = await previewClassGroupScheme({
        entryYear,
        classCode,
        membersPerGroup: groupForm.value.membersPerGroup,
        mode: 'RANGE'
      })
      const previewData = preview.data || {}
      groupForm.value.mode = 'RANGE'
      groupForm.value.groups = normalizeGroups(previewData.groups, previewData.members)
    } else {
      groupForm.value.groups = []
    }
  } catch (e) {
    groupForm.value.groups = []
    ElMessage.warning(e?.msg || '分组方案加载失败，请先确认当前班级参数')
  } finally {
    groupLoading.value = false
  }
}

function handleGroupMembersChange(group) {
  // 如果当前选中的组长不在最新的成员列表中，清空组长
  if (group.leaderStudentId && !group.studentIds.includes(Number(group.leaderStudentId))) {
    group.leaderStudentId = null
  }
}

async function generateGroups() {
  groupLoading.value = true
  try {
    const response = await previewClassGroupScheme({
      entryYear,
      classCode,
      membersPerGroup: groupForm.value.membersPerGroup,
      mode: groupForm.value.mode
    })
    const data = response.data || {}
    groupForm.value.groups = normalizeGroups(data.groups, data.members)
  } catch (e) {
    ElMessage.error(e?.msg || '自动生成分组失败')
  } finally {
    groupLoading.value = false
  }
}

async function saveGroups() {
  if (!groupForm.value.schemeName.trim()) {
    ElMessage.warning('请输入方案名称')
    return
  }
  const groups = groupForm.value.groups.map(group => ({
    ...group,
    studentIds: (group.studentIds || []).map(Number),
    leaderStudentId: group.leaderStudentId ? Number(group.leaderStudentId) : null
  }))
  const allIds = groups.flatMap(group => group.studentIds)
  if (allIds.length !== students.value.length || new Set(allIds).size !== students.value.length) {
    ElMessage.warning('请确保每名学生只分配到一个组，且不能遗漏；如名单刚变化请关闭重进')
    return
  }
  groupSaving.value = true
  try {
    await saveClassGroupScheme({
      entryYear,
      classCode,
      schemeName: groupForm.value.schemeName.trim(),
      groups
    })
    groupDialogVisible.value = false
    showGroups.value = true
    await loadDesktop()
    ElMessage.success('班级固定分组已保存')
  } catch (e) {
    ElMessage.error(e?.msg || '分组保存失败')
  } finally {
    groupSaving.value = false
  }
}

function taskLabel(taskState) {
  return (
    {
      ENTERED: '已进入',
      WORKING: '作答中',
      SUBMITTED: '已提交',
      GRADED: '已批改',
      RETURNED: '待重做',
      NO_TASK: '暂无任务',
      NOT_ENTERED: '未进入'
    }[taskState] || '同步中'
  )
}

function taskTagType(taskState) {
  return { WORKING: 'warning', SUBMITTED: 'success', GRADED: 'info', RETURNED: 'danger' }[taskState] || 'info'
}

function scoreLabel(value) {
  return value == null ? '-' : value
}

function practicalShortLabel(data) {
  if (!data || !data.submittedCount) return '未交'
  if (data.score == null) return '待批'
  return `${data.score}分`
}

function openPerformance(student) {
  if (student.performance?.isAbsent) {
    ElMessage.warning('该学生已请假，请先取消请假后再记录课堂表现')
    return
  }
  performanceStudent.value = student
  performanceForm.value = { score: Number(student.performance?.score || 0), reason: student.performance?.reason || '' }
  performanceDialogVisible.value = true
}

async function savePerformanceChange() {
  if (!performanceStudent.value) return
  // 与成绩查询页一致：直接保存绝对分（-10 扣分 ~ +10 加分），不再做方向+分值二次换算
  const score = Math.round(Number(performanceForm.value.score || 0))
  if (!Number.isFinite(score) || score < -10 || score > 10) {
    ElMessage.warning('课堂表现总分范围为 -10 到 +10')
    return
  }
  const reason = String(performanceForm.value.reason || '').trim()
  performanceSaving.value = true
  try {
    // isAbsent 为 Integer（0/1）：必须传数字 0，传布尔 false 会触发后端“请求参数格式错误”400
    await savePerformance({
      studentId: performanceStudent.value.studentId,
      lessonId: Number(lessonId),
      score,
      reason,
      isAbsent: 0
    })
    performanceStudent.value.performance = {
      ...(performanceStudent.value.performance || {}),
      score,
      reason,
      isAbsent: 0
    }
    performanceDialogVisible.value = false
    ElMessage.success('课堂表现已保存')
  } catch (e) {
    ElMessage.error(e?.msg || '课堂表现保存失败')
  } finally {
    performanceSaving.value = false
  }
}

async function toggleAbsent(student) {
  try {
    const next = !student.performance?.isAbsent
    await setStudentAbsent(student.studentId, Number(lessonId), next)
    student.performance = {
      ...(student.performance || {}),
      isAbsent: next,
      score: next ? 0 : student.performance?.score || 0
    }
    ElMessage.success(next ? '已标记请假' : '已取消请假')
  } catch (e) {
    ElMessage.error(e?.msg || '请假状态保存失败')
  }
}

onMounted(() => {
  load()
  presenceRefreshTimer = window.setInterval(() => {
    if (document.visibilityState === 'visible' && !layoutMode.value) {
      loadDesktop(true)
    }
  }, 30000)
})

onBeforeUnmount(() => {
  window.clearInterval(presenceRefreshTimer)
})
</script>

<style scoped>
/* 容器与基底样式 */
.classroom-desktop {
  padding: 16px 20px 40px;
  background: #f4f6fa;
  min-height: calc(100vh - 84px);
  box-sizing: border-box;
}

/* 顶部主工具栏 */
.desktop-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
  background: #ffffff;
  padding: 16px 20px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
  border: 1px solid #e4e7ed;
  margin-bottom: 14px;
}

.title-section .main-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1d2129;
  letter-spacing: -0.2px;
}

.subtitle-hint {
  margin: 4px 0 0;
  font-size: 12px;
  color: #86909c;
  display: flex;
  align-items: center;
  gap: 4px;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar-divider {
  height: 24px;
  margin: 0 4px;
  border-color: #e5e6eb;
}

/* 题目发令闸 */
.gate-launcher {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #eef8eb;
  border: 1px solid #bfe3b4;
  padding: 4px 12px;
  border-radius: 8px;
}

.gate-launcher-label {
  font-size: 13px;
  font-weight: 600;
  color: #278619;
  white-space: nowrap;
}

.gate-switch-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.gate-item-title {
  font-size: 12px;
  color: #3b8823;
  font-weight: 500;
  white-space: nowrap;
}

/* 每排人数控制组件 */
.columns-controller {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f2f3f5;
  padding: 3px 8px;
  border-radius: 8px;
  border: 1px solid #e5e6eb;
}

.ctrl-label {
  font-size: 12px;
  font-weight: 600;
  color: #4e5969;
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}

.cols-preset-group .el-button {
  padding: 4px 8px;
  font-size: 12px;
}

.cols-number-input {
  width: 78px;
}

.view-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 课堂概览指示条 */
.classroom-summary-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px 20px;
  background: #ffffff;
  border-radius: 8px;
  padding: 10px 18px;
  margin-bottom: 16px;
  border: 1px solid #e5e6eb;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.02);
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #4e5969;
}

.summary-label {
  color: #86909c;
  font-size: 12px;
}

.summary-num {
  font-size: 15px;
  font-weight: 700;
  color: #1d2129;
}

.summary-item.online .summary-num {
  color: #00b42a;
}

.summary-item.offline .summary-num {
  color: #86909c;
}

.summary-item.progress .summary-num {
  color: #165dff;
}

.summary-item.absent .summary-num {
  color: #ff7d00;
}

.summary-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.summary-dot.is-online {
  background: #00b42a;
  box-shadow: 0 0 6px rgba(0, 180, 42, 0.4);
}

.summary-dot.is-offline {
  background: #c9cdd4;
}

.summary-scale-hint {
  margin-left: auto;
  font-size: 12px;
  color: #86909c;
}

/* 核心 Flex 弹性流式容器 */
.student-monitor-container {
  min-height: 200px;
}

.flex-fluid-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  width: 100%;
}

.fluid-card-wrapper {
  box-sizing: border-box;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 学生卡片核心设计 */
.student-card-item {
  position: relative;
  background: #ffffff;
  border-radius: 10px;
  border: 1px solid #e5e6eb;
  padding: 12px 14px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
  overflow: hidden;
}

.student-card-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

/* 在线与离线边框风格 */
.student-card-item.is-connected {
  border-top: 3px solid #00b42a;
}

.student-card-item.is-disconnected {
  border-top: 3px solid #c9cdd4;
  background: #fafbfc;
}

/* 组长卡片专属光晕 */
.student-card-item.is-leader {
  border-color: #f7ba2a;
  box-shadow: 0 0 0 1.5px #f7ba2a inset, 0 2px 8px rgba(247, 186, 42, 0.15);
}

.student-card-item.is-absent {
  background: #fffcf5;
  border-color: #ffd591;
}

/* 组长悬浮金标 */
.card-leader-ribbon {
  position: absolute;
  top: 0;
  right: 0;
  background: linear-gradient(135deg, #f7ba2a 0%, #d48806 100%);
  color: #ffffff;
  font-size: 10px;
  font-weight: 700;
  padding: 2px 8px;
  border-bottom-left-radius: 8px;
  box-shadow: -1px 1px 4px rgba(0, 0, 0, 0.15);
  letter-spacing: 0.5px;
  z-index: 2;
}

/* 卡片顶部栏 */
.card-top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  padding-right: 18px; /* 留出组长角标间距 */
}

.student-identity {
  display: flex;
  align-items: center;
  gap: 8px;
  overflow: hidden;
}

/* 呼吸灯在线指示器 */
.presence-beacon {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
  transition: all 0.3s;
}

.presence-beacon.beacon-online {
  background: #00b42a;
  box-shadow: 0 0 0 0 rgba(0, 180, 42, 0.7);
  animation: beacon-pulse 2s infinite;
}

.presence-beacon.beacon-offline {
  background: #c9cdd4;
}

@keyframes beacon-pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(0, 180, 42, 0.6);
  }
  70% {
    box-shadow: 0 0 0 6px rgba(0, 180, 42, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(0, 180, 42, 0);
  }
}

.name-box {
  display: flex;
  align-items: center;
  gap: 5px;
  overflow: hidden;
}

.student-no-badge {
  font-size: 11px;
  font-weight: 700;
  color: #4e5969;
  background: #f2f3f5;
  padding: 1px 4px;
  border-radius: 4px;
  font-family: monospace;
}

.student-name {
  font-size: 14px;
  color: #1d2129;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}

.conn-status-tag {
  flex-shrink: 0;
  font-size: 10px;
  padding: 0 4px;
  height: 20px;
  line-height: 18px;
}

/* 作答进度胶囊 */
.card-task-capsule {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  background: #f7f8fa;
  padding: 3px 8px;
  border-radius: 6px;
  font-size: 11px;
}

.task-state-tag {
  font-size: 11px;
  padding: 0 6px;
  height: 20px;
  line-height: 18px;
}

.task-progress-text {
  font-size: 11px;
  color: #86909c;
  font-weight: 500;
}

/* 多维度成绩矩阵 */
.card-metrics-matrix {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 6px;
  margin-bottom: 8px;
}

.metric-pill {
  display: flex;
  align-items: center;
  gap: 4px;
  background: #f7f8fa;
  border: 1px solid #f2f3f5;
  padding: 4px 6px;
  border-radius: 6px;
  font-size: 11px;
  overflow: hidden;
}

.pill-icon {
  font-size: 11px;
}

.pill-title {
  color: #86909c;
  font-size: 10px;
  white-space: nowrap;
}

.pill-value {
  color: #1d2129;
  font-weight: 600;
  white-space: nowrap;
}

.pill-value.has-score {
  color: #ff7d00;
}

.pill-sub {
  color: #c9cdd4;
  font-size: 10px;
  white-space: nowrap;
}

/* IP 与备注小行 */
.card-meta-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 11px;
  color: #86909c;
  margin-bottom: 6px;
  overflow: hidden;
  white-space: nowrap;
}

.ip-address {
  font-family: monospace;
  text-overflow: ellipsis;
  overflow: hidden;
}

.student-remark {
  color: #4e5969;
  background: #fff7e8;
  padding: 0 4px;
  border-radius: 3px;
  text-overflow: ellipsis;
  overflow: hidden;
}

/* 请假横条 */
.card-absent-banner {
  background: #fff7e6;
  border: 1px solid #ffd591;
  color: #d46b08;
  font-size: 11px;
  font-weight: 600;
  text-align: center;
  padding: 3px 0;
  border-radius: 4px;
  margin-bottom: 6px;
}

/* 底部操作条 */
.card-footer-actions {
  display: flex;
  gap: 6px;
  margin-top: auto;
  padding-top: 6px;
  border-top: 1px dashed #f2f3f5;
}

.action-mini-btn {
  flex: 1;
  font-size: 11px;
  padding: 4px 6px;
}

/* 密度阶梯自适应：舒适模式（列数 <= 4） */
.density-comfortable {
  padding: 16px 18px;
}
.density-comfortable .student-name {
  font-size: 16px;
}
.density-comfortable .student-no-badge {
  font-size: 12px;
  padding: 2px 6px;
}
.density-comfortable .metric-pill {
  padding: 6px 8px;
  font-size: 12px;
}
.density-comfortable .action-mini-btn {
  font-size: 12px;
  height: 28px;
}

/* 密度阶梯自适应：紧凑模式（列数 >= 9） */
.density-compact {
  padding: 8px 10px;
}
.density-compact .student-name {
  font-size: 12px;
}
.density-compact .student-no-badge {
  font-size: 10px;
}
.density-compact .card-metrics-matrix {
  gap: 4px;
}
.density-compact .metric-pill {
  padding: 2px 4px;
  font-size: 10px;
}
.density-compact .pill-sub {
  display: none; /* 紧凑视图隐藏副指标 */
}
.density-compact .card-meta-line {
  font-size: 10px;
}
.density-compact .action-mini-btn {
  font-size: 10px;
  height: 22px;
  padding: 0;
}

/* 分组聚类容器 */
.group-cluster-box {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e5e6eb;
  padding: 16px 18px;
  margin-bottom: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.02);
}

.group-cluster-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f2f3f5;
}

.group-cluster-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.group-indicator-color {
  width: 12px;
  height: 12px;
  border-radius: 3px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

.group-name-text {
  font-size: 15px;
  font-weight: 700;
  color: #1d2129;
}

.group-cluster-leader-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #fffbe6;
  border: 1px solid #ffe58f;
  padding: 3px 10px;
  border-radius: 6px;
  color: #ad6800;
  font-size: 12px;
}

/* 调座模式悬浮底栏 */
.layout-footer-bar {
  position: sticky;
  bottom: 12px;
  background: #1d2129;
  color: #ffffff;
  border-radius: 10px;
  padding: 12px 24px;
  margin-top: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.25);
  z-index: 100;
}

.footer-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #e5e6eb;
}

/* “设置分组”弹窗专享样式：最后一列明确突出组长 */
.group-generate-toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 14px;
  background: #f7f8fa;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 14px;
}

.group-generate-toolbar .form-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.group-generate-toolbar .item-label {
  font-size: 13px;
  font-weight: 500;
  color: #4e5969;
}

.group-table-box {
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  overflow: hidden;
  margin-top: 10px;
}

.group-table-header {
  display: flex;
  background: #f2f3f5;
  padding: 10px 14px;
  font-size: 13px;
  font-weight: 700;
  color: #1d2129;
  border-bottom: 1px solid #e5e6eb;
}

.th-col {
  box-sizing: border-box;
}

.th-name {
  width: 180px;
  flex-shrink: 0;
}

.th-members {
  flex: 1;
  padding: 0 12px;
}

/* 组长列标题（最后一列特别突出） */
.th-leader {
  width: 280px;
  flex-shrink: 0;
  color: #d48806;
  background: #fffbe6;
  padding: 4px 12px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.group-table-body {
  max-height: 480px;
  overflow-y: auto;
}

.group-table-row {
  display: flex;
  padding: 12px 14px;
  border-bottom: 1px solid #f2f3f5;
  align-items: center;
  transition: background 0.2s;
}

.group-table-row:last-child {
  border-bottom: none;
}

.group-table-row:hover {
  background: #fafbfc;
}

.td-col {
  box-sizing: border-box;
}

.td-name {
  width: 180px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.group-seq-badge {
  font-size: 12px;
  font-weight: 700;
  color: #165dff;
}

.group-name-input {
  width: 150px;
}

.member-count-hint {
  font-size: 11px;
  color: #86909c;
}

.td-members {
  flex: 1;
  padding: 0 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.group-member-multiselect {
  width: 100%;
}

.members-chip-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.empty-member-text {
  font-size: 12px;
  color: #c9cdd4;
}

.member-clickable-chip {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  background: #f2f3f5;
  color: #4e5969;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.2s;
}

.member-clickable-chip:hover {
  background: #e5e6eb;
  border-color: #c9cdd4;
}

.member-clickable-chip.is-current-leader {
  background: #fff7e6;
  color: #d46b08;
  border-color: #ffd591;
  font-weight: 600;
}

/* 最后一列：组长专属展示卡片 */
.td-leader {
  width: 280px;
  flex-shrink: 0;
  background: #fffcf0;
  border: 1px dashed #ffe58f;
  border-radius: 8px;
  padding: 10px 12px;
}

.leader-cell-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.leader-select-input {
  width: 100%;
}

.leader-designated-banner {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #fff7e6;
  border: 1px solid #ffd591;
  padding: 4px 8px;
  border-radius: 6px;
  color: #d46b08;
  font-size: 12px;
}

.crown-glow {
  font-size: 14px;
}

.leader-unassigned-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 11px;
  color: #86909c;
}

.quick-set-link {
  font-size: 11px;
}

.dialog-footer-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.footer-hint {
  font-size: 12px;
  color: #86909c;
}

/* 课堂表现评定弹窗 */
.performance-modal-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.student-target-header {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #f7f8fa;
  padding: 12px 16px;
  border-radius: 8px;
}

.student-avatar-badge {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #165dff;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
}

.student-detail-info .title-row {
  font-size: 15px;
  color: #1d2129;
}

.current-score-text {
  font-size: 12px;
  color: #86909c;
  margin-top: 2px;
}

.score-highlight {
  color: #ff7d00;
  font-weight: 700;
}

.form-block {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-label {
  font-size: 13px;
  font-weight: 600;
  color: #4e5969;
}

.mb12 {
  margin-bottom: 12px;
}

/* 响应式适配 */
@media (max-width: 1024px) {
  .desktop-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
  .toolbar-actions {
    width: 100%;
  }
  .group-table-header {
    display: none; /* 极小屏折叠表头 */
  }
  .group-table-row {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  .td-name, .td-members, .td-leader {
    width: 100%;
    padding: 0;
  }
}
</style>
