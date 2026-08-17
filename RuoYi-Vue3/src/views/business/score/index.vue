<template>
  <div class="app-container">
    <!-- 筛选区域 -->
    <el-card class="filter-card">
      <div class="filter-row">
        <span class="filter-label">入学年份：</span>
        <el-select v-model="queryParams.entryYear" placeholder="选择年份" @change="onYearChange" style="width: 120px">
          <el-option v-for="item in yearOptions" :key="item.entryYear" :label="item.entryYear + '级'" :value="item.entryYear" />
        </el-select>
        
        <span class="filter-label">班级：</span>
        <el-select v-model="queryParams.classCode" placeholder="全部班级" clearable @change="onClassChange" style="width: 120px">
          <el-option v-for="item in classOptions" :key="item.classCode" :label="item.classCode + '班'" :value="item.classCode" />
        </el-select>
        
        <span class="filter-label">课程：</span>
        <el-select v-model="dropdownLessonIds" placeholder="全部课程" multiple collapse-tags collapse-tags-tooltip clearable style="width: 280px" @change="onDropdownChange">
          <el-option v-for="item in lessonOptions" :key="item.lessonId" :label="item.lessonTitle" :value="item.lessonId" />
        </el-select>
        
        <!-- 学生搜索 -->
        <span class="filter-label">搜索学生：</span>
        <el-input v-model="searchKeyword" placeholder="姓名、学号或账号" clearable style="width: 170px" @keyup.enter="handleQuery" />
        
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button
          v-if="guideSheetContext?.enabled"
          type="success"
          plain
          :loading="guideContextLoading"
          @click="openGuideSheetScores"
        >
          电子导学单成绩
        </el-button>
        <!-- D1：导学单分独立说明常驻，避免教师误并入作业均分 -->
        <el-tooltip
          v-if="guideSheetContext?.enabled"
          content="电子导学单成绩独立统计，不进入作业均分、排名与课程总分。"
          placement="bottom"
        >
          <el-tag type="info" effect="plain" class="guide-score-hint-tag">导学单分不计入作业均分</el-tag>
        </el-tooltip>
        
        <!-- 选中课程提示 -->
        <span v-if="selectedLessonIds.length > 0" class="selected-tip">
          已选中 {{ selectedLessonIds.length }} 门课程
          <el-button link type="primary" @click="clearSelection">清除选择</el-button>
        </span>
      </div>
    </el-card>

    <!-- 图表区域 -->
    <div v-if="tableData.length > 0">
      <!-- 1. 年级概览模式 (全选班级) -->
      <div v-if="isGradeMode">
        <el-row :gutter="15" class="chart-row">
          <el-col :span="24">
            <class-score-chart :data="tableData" />
          </el-col>
        </el-row>
        
        <!-- 红榜与潜力榜 -->
        <el-row :gutter="20" style="margin-top: 20px; margin-bottom: 20px;">
          <el-col :xs="24" :sm="24" :md="12" style="margin-bottom: 10px;">
            <student-rank-list 
              :data="displayData" 
              title="🏆 年级红榜 (Top 50)" 
              type="top" 
              :limit="50" 
            />
          </el-col>
          <el-col :xs="24" :sm="24" :md="12" style="margin-bottom: 10px;">
            <student-rank-list 
              :data="displayData" 
              title="💡 潜力榜 (Bottom 50)" 
              type="bottom" 
              :limit="50" 
            />
          </el-col>
        </el-row>
      </div>

      <!-- 2. 班级详细模式 (单选班级) -->
      <div v-else>
         <el-row :gutter="15" class="chart-row">
          <!-- 单选课程或无选择时，显示总分分布 -->
          <el-col :span="24" v-if="selectedLessonIds.length <= 1">
            <rank-chart :data="tableData" />
          </el-col>
          <!-- 多选课程时，显示课程对比图 -->
          <el-col :span="24" v-else>
            <course-comparison-chart 
              :data="tableData" 
              :lesson-options="lessonOptions" 
              :selected-lesson-ids="selectedLessonIds" 
            />
          </el-col>
        </el-row>
      </div>
    </div>
    
    <!-- 打字题专属图表区域 (仅班级模式显示) -->
    <el-row :gutter="15" v-if="!isGradeMode && tableData.length > 0 && hasTypingData" class="chart-row">
      <el-col :span="24">
         <typing-chart :data="tableData" :lesson-options="lessonOptions" />
      </el-col>
    </el-row>

    <!-- 答题分析区域 - 放在成绩表上方 (仅班级模式显示) -->
    <el-card v-if="!isGradeMode && selectedLessonIds.length === 1 && analysisData.length > 0 && hasTheoryQuestions" class="analysis-card" style="margin-bottom: 15px;">
      <template #header>
        <div class="chart-header">
          📊 答题情况分析 - {{ lessonOptions.find(l => l.lessonId === selectedLessonIds[0])?.lessonTitle || '当前课程' }}
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="24">
           <div ref="analysisChartRef" style="width: 100%; height: 350px;"></div>
        </el-col>
      </el-row>

      <div class="chart-header" style="margin-top: 30px; margin-bottom: 10px; font-weight: bold; font-size: 16px; border-left: 5px solid #67C23A; padding-left: 10px;">
        📋 详细题目分析
      </div>
      
      <el-table :data="analysisData" border stripe>
        <el-table-column label="题目内容" prop="questionContent" min-width="250">
          <template #default="scope">
            <span class="question-type-tag" :class="scope.row.questionType">
              [{{ questionTypeLabel(scope.row.questionType) }}]
            </span>
            {{ scope.row.questionContent }}
          </template>
        </el-table-column>
        <el-table-column label="正确答案" width="120" align="center">
          <template #default="scope">
            <template v-if="scope.row.questionType === 'judgment'">
              <span>{{ scope.row.answer === 'T' ? '正确' : '错误' }}</span>
            </template>
            <template v-else>
              <span>{{ scope.row.answer }}</span>
              <span v-if="scope.row.optionContents && scope.row.optionContents[scope.row.answer]" style="color: #909399; font-size: 12px;">
                : {{ scope.row.optionContents[scope.row.answer] }}
              </span>
            </template>
          </template>
        </el-table-column>
        <el-table-column label="正确率" prop="accuracy" width="150" sortable>
          <template #default="scope">
            <el-progress :percentage="scope.row.accuracy || 0" :color="getAccuracyColor(scope.row.accuracy || 0)" />
          </template>
        </el-table-column>
        <el-table-column label="答题人数" prop="studentCount" width="100" align="center" sortable>
          <template #default="scope">
            {{ scope.row.studentCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="选项分布" min-width="350">
           <template #default="scope">
             <div class="distribution-bar" v-if="scope.row.answerDistribution">
               <div v-for="(count, opt) in scope.row.answerDistribution" :key="opt" class="dist-item">
                 <div class="dist-info">
                   <span class="opt-label" :class="{ correct: opt === scope.row.answer }">{{ opt }}</span>
                   <span class="opt-content" v-if="scope.row.optionContents && scope.row.optionContents[opt]" :title="scope.row.optionContents[opt]">
                      : {{ scope.row.optionContents[opt] }}
                   </span>
                   <span class="count">({{ count }}人)</span>
                 </div>
                 <div class="dist-progress" :style="{ width: getDistPercent(count, scope.row.studentCount) + '%' }"></div>
               </div>
             </div>
             <span v-else>-</span>
           </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 学生答题详情矩阵 -->
    <analysis-matrix 
      v-if="!isGradeMode && selectedLessonIds.length === 1 && matrixData.length > 0 && hasTheoryQuestions" 
      :matrix-data="matrixData"
      :questions="analysisData"
      :loading="matrixLoading"
    />

    <!-- 课堂表现管理区域（仅单选课程时显示） -->
    <performance-section
      v-if="!isGradeMode && selectedLessonIds.length === 1"
      :lesson-id="selectedLessonIds[0]"
      :class-code="queryParams.classCode"
      :entry-year="queryParams.entryYear"
      @saved="handleQuery"
    />

    <!-- 数据表格 -->
    <el-card class="data-card">
      <template #header>
        <div class="card-header">
          <span style="font-weight: bold; font-size: 16px;">📊 学生成绩汇总表</span>
          <div class="header-actions">
            <span v-if="selectedLessonIds.length === 1" style="font-size: 13px; color: #909399; margin-right: 15px;">
              <el-icon style="vertical-align: middle; margin-top: -2px;"><Calendar /></el-icon> 点击姓名右侧图标可标记请假
            </span>
            <el-switch
              v-model="excludeZeroScore"
              active-text="排除0分"
              inactive-text=""
              size="small"
              style="margin-right: 10px;"
            />
            <el-button type="info" size="small" @click="ratioDialogVisible = true" :disabled="!tableData.length">
              <el-icon><Setting /></el-icon> 设置比例
            </el-button>
            <el-button type="success" size="small" icon="Download" @click="exportDialogVisible = true" :disabled="!tableData.length">导出 Excel</el-button>
            
            <!-- 列设置 -->
            <el-popover placement="bottom-end" :width="200" trigger="click">
              <template #reference>
                <el-button size="small">
                  <el-icon><Setting /></el-icon> 列设置
                </el-button>
              </template>
              <div class="column-settings">
                <el-checkbox 
                  v-for="col in columnOptions" 
                  :key="col.key" 
                  v-model="visibleColumns[col.key]"
                  style="display: block; margin: 5px 0;"
                >{{ col.label }}</el-checkbox>
              </div>
            </el-popover>
          </div>
        </div>
      </template>
      <el-table :data="displayDataWithGrade" v-loading="loading" border stripe :default-sort="{ prop: 'studentNo', order: 'ascending' }" max-height="600" style="width: 100%">
        <el-table-column prop="userName" label="账号" width="120" align="center" sortable fixed="left" />
        <el-table-column prop="className" label="班级" width="80" align="center" sortable :sort-method="(a, b) => Number(a.className) - Number(b.className)" fixed="left" />
        <el-table-column prop="studentNo" label="学号" width="80" align="center" sortable fixed="left" />
        <el-table-column prop="studentName" label="姓名" width="100" align="center" sortable :sort-method="(a, b) => a.studentName.localeCompare(b.studentName, 'zh-CN')" fixed="left">
          <template #default="scope">
            <el-button link type="primary" @click="showStudentProfile(scope.row)">{{ scope.row.studentName }}</el-button>
          </template>
        </el-table-column>
        
        <!-- 请假状态列 (仅在选中单门课程时显示，更直观) -->
        <el-table-column v-if="selectedLessonIds.length === 1" label="请假" width="60" align="center" fixed="left">
          <template #default="scope">
            <el-button 
                circle
                size="small" 
                :type="isLessonAbsent(scope.row, selectedLessonIds[0]) ? 'info' : 'warning'" 
                :title="isLessonAbsent(scope.row, selectedLessonIds[0]) ? '恢复得分' : '设为请假'"
                plain 
                @click="handleAbsent(scope.row.studentId, selectedLessonIds[0], !isLessonAbsent(scope.row, selectedLessonIds[0]))"
                style="width: 24px; height: 24px; padding: 0;"
            >
                <el-icon style="font-size: 12px;"><Calendar /></el-icon>
            </el-button>
          </template>
        </el-table-column>

        <el-table-column v-if="selectedLessonIds.length === 1" label="改分" width="60" align="center" fixed="left">
          <template #default="scope">
            <el-button
              circle
              size="small"
              :type="getSelectedLessonScore(scope.row)?.manualAdjusted ? 'danger' : 'primary'"
              :title="getSelectedLessonScore(scope.row)?.manualAdjusted ? '查看/取消人工改分' : '人工改作业分'"
              plain
              @click="openManualScoreDialog(scope.row)"
              style="width: 24px; height: 24px; padding: 0;"
            >
              <el-icon style="font-size: 12px;"><EditPen /></el-icon>
            </el-button>
          </template>
        </el-table-column>
        
        <el-table-column v-if="visibleColumns.remark" prop="remark" label="备注" width="100" align="center" show-overflow-tooltip fixed="left">
          <template #default="scope">
            <span v-if="scope.row.remark" style="color: #E6A23C;">{{ scope.row.remark }}</span>
            <span v-else style="color: #C0C4CC;">-</span>
          </template>
        </el-table-column>
        
        <!-- 动态课程列 (当选中课程数 <= 5 时显示) -->
        <template v-if="selectedLessonIds.length > 1 && selectedLessonIds.length <= 5">
            <el-table-column 
                v-for="lessonId in selectedLessonIds" 
                :key="lessonId"
                :label="getLessonName(lessonId)" 
                align="center"
                sortable
                :sort-method="(a, b) => getLessonScore(a, lessonId) - getLessonScore(b, lessonId)"
                width="120"
            >
                <template #default="scope">
                    <span class="score-num" :class="getScoreClass(getLessonScore(scope.row, lessonId))" :style="{ color: isLessonAbsent(scope.row, lessonId) ? '#909399' : '' }">
                        {{ getLessonScoreDisplay(scope.row, lessonId) }}
                    </span>
                </template>
            </el-table-column>
        </template>

        <!-- 各课程成绩：带复选框 (仅当选中课程 > 5 或 <= 1 时显示，或者没有选中任何课程时显示所有) -->
        <el-table-column 
            v-if="selectedLessonIds.length > 5 || selectedLessonIds.length <= 1"
            label="各课程成绩（点击勾选参与统计）" 
            align="center" 
            min-width="300"
        >
          <template #default="scope">
            <div class="score-list">
              <div v-for="score in scope.row.scores" :key="score.lessonId" class="score-item">
                <el-checkbox 
                  :model-value="selectedLessonIds.includes(score.lessonId)"
                  @change="(val) => toggleLesson(score.lessonId, val)"
                  size="small"
                />
                <span class="lesson-name">{{ score.lessonTitle }}</span>
                <el-popover placement="bottom" :width="240" trigger="hover">
                  <template #reference>
                    <el-tag 
                      :type="score.isAbsent ? 'info' : getScoreType(score.finalScore ?? score.totalScore)" 
                      size="small"
                      :class="{ 'selected-tag': selectedLessonIds.includes(score.lessonId) }"
                      class="score-num"
                    >{{ score.isAbsent ? '请假' : (score.finalScore ?? score.totalScore) }}</el-tag>
                  </template>
                  <div class="score-detail">
                    <p><b>打字：</b><span class="score-num">{{ score.typingScore }}</span> 分</p>
                    <p><b>理论：</b><span class="score-num">{{ score.theoryScore }}</span> 分</p>
                    <p><b>操作：</b><span class="score-num">{{ score.practicalScore }}</span> 分</p>
                    <p>
                      <b>作业分：</b><span class="score-num">{{ score.totalScore || 0 }}</span> 分
                      <el-tag v-if="score.manualAdjusted" size="small" type="danger" effect="plain" class="manual-score-mark">修</el-tag>
                    </p>
                    <p v-if="score.manualAdjusted"><b>原始作业分：</b><span class="score-num">{{ score.originalTotalScore || 0 }}</span> 分</p>
                    <p v-if="score.manualAdjusted"><b>修正原因：</b>{{ score.adjustmentReason || '-' }}</p>
                    <p><b>课堂表现：</b><span class="score-num">{{ (score.performanceScore || 0) > 0 ? '+' : '' }}{{ score.performanceScore || 0 }}</span> 分</p>
                    <p><b>课程总分：</b><span class="score-num">{{ score.isAbsent ? '请假' : (score.finalScore ?? score.totalScore) }}</span></p>
                    <el-divider v-if="score.avgTypingSpeed" style="margin: 8px 0" />
                    <template v-if="score.avgTypingSpeed">
                      <p><b>打字速度：</b><span class="score-num">{{ score.avgTypingSpeed }}</span> <small>字/分</small></p>
                      <p><b>正确率：</b><span class="score-num">{{ score.avgAccuracyRate }}%</span></p>
                      <p><b>完成率：</b><span class="score-num">{{ score.avgCompletionRate }}%</span></p>
                    </template>
                  </div>
                </el-popover>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column v-if="visibleColumns.avgTyping" prop="avgTyping" label="打字平均" width="95" align="center" sortable>
          <template #default="scope">
            <span class="gray-text score-num">{{ scope.row.avgTyping }}</span>
          </template>
        </el-table-column>
        
        <el-table-column v-if="visibleColumns.overallTypingSpeed" prop="overallTypingSpeed" label="打字速度" width="100" align="center" sortable>
          <template #default="scope">
            <span v-if="scope.row.overallTypingSpeed" class="typing-speed score-num">{{ scope.row.overallTypingSpeed }} <small>字/分</small></span>
            <span v-else class="gray-text">-</span>
          </template>
        </el-table-column>
        
        <el-table-column v-if="visibleColumns.overallAccuracy" prop="overallAccuracy" label="打字正确率" width="100" align="center" sortable>
          <template #default="scope">
            <span v-if="scope.row.overallAccuracy" class="typing-accuracy score-num">{{ scope.row.overallAccuracy }}%</span>
            <span v-else class="gray-text">-</span>
          </template>
        </el-table-column>
        
        <el-table-column v-if="visibleColumns.overallCompletion" prop="overallCompletion" label="打字完成率" width="100" align="center" sortable>
          <template #default="scope">
            <span v-if="scope.row.overallCompletion" class="typing-completion score-num">{{ scope.row.overallCompletion }}%</span>
            <span v-else class="gray-text">-</span>
          </template>
        </el-table-column>
        
        <el-table-column v-if="visibleColumns.avgTheory" prop="avgTheory" label="理论平均" width="95" align="center" sortable>
          <template #default="scope">
            <span class="gray-text score-num">{{ scope.row.avgTheory }}</span>
          </template>
        </el-table-column>

        <el-table-column v-if="visibleColumns.avgPractical" prop="avgPractical" label="操作平均" width="95" align="center" sortable>
          <template #default="scope">
            <span class="gray-text score-num">{{ scope.row.avgPractical }}</span>
          </template>
        </el-table-column>
        
        <!-- 作业总分 -->
        <el-table-column v-if="visibleColumns.filteredTotal" prop="filteredTotal" :label="scoreLabels.filteredTotal" width="100" align="center" sortable>
          <template #default="scope">
            <div class="data-bar-cell">
              <div class="data-bar" :style="{ width: getBarWidth(scope.row.filteredTotal, maxTotal) + '%' }"></div>
              <span class="data-bar-value total-score score-num">{{ formatScoreDisplay(scope.row.filteredTotal, isMultiScoreMode) }}</span>
            </div>
          </template>
        </el-table-column>
        
        <!-- 课堂表现分 -->
        <el-table-column v-if="visibleColumns.totalPerformance" prop="totalPerformance" :label="scoreLabels.totalPerformance" width="110" align="center" sortable>
          <template #default="scope">
            <span 
              class="score-num" 
              :style="{ 
                color: scope.row.totalPerformance > 0 ? '#67C23A' : (scope.row.totalPerformance < 0 ? '#F56C6C' : '#909399'),
                fontWeight: 'bold'
              }"
            >{{ scope.row.totalPerformance > 0 ? '+' : '' }}{{ formatScoreDisplay(scope.row.totalPerformance, isMultiScoreMode) }}</span>
          </template>
        </el-table-column>
        
        <!-- 课程总分 -->
        <el-table-column v-if="visibleColumns.finalTotal" prop="finalTotal" :label="scoreLabels.finalTotal" width="110" align="center" sortable>
          <template #default="scope">
            <span class="score-num" style="font-weight: bold; color: #409EFF;">{{ formatScoreDisplay(scope.row.finalTotal, isMultiScoreMode) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column v-if="visibleColumns.filteredAverage" prop="filteredAverage" :label="scoreLabels.filteredAverage" width="100" align="center" sortable>
          <template #default="scope">
            <div class="data-bar-cell avg-bar">
              <div class="data-bar" :style="{ width: getBarWidth(scope.row.filteredAverage, 100) + '%' }"></div>
              <span class="data-bar-value avg-score score-num">{{ formatScoreDisplay(scope.row.filteredAverage, isMultiScoreMode) }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column v-if="visibleColumns.gradeLevel" prop="gradeLevel" label="等级" width="90" align="center" sortable>
          <template #default="scope">
            <el-tag 
              :type="getGradeTagType(scope.row.gradeLevel)" 
              size="small"
            >{{ scope.row.gradeLevel }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column v-if="visibleColumns.scaledScore" prop="scaledScore" label="赋分" width="80" align="center" sortable>
          <template #default="scope">
            <span class="score-num" style="font-weight: bold; color: #E6A23C;">{{ scope.row.scaledScore }}</span>
          </template>
        </el-table-column>
      </el-table>
      
      <div v-if="!tableData.length && !loading" class="empty-tip">
        请选择入学年份后点击查询
      </div>
    </el-card>

    <!-- 学生画像弹窗 -->
    <!-- 学生画像弹窗 -->
    <student-profile-dialog 
      v-model="profileDialogVisible" 
      :student="currentStudent"
    />
    
    <!-- 等级比例设置对话框 -->
    <grade-ratio-dialog
      v-model="ratioDialogVisible"
      :ratios="gradeRatios"
      @confirm="handleRatioConfirm"
    />
    
    <!-- 导出选项对话框 -->
    <export-dialog
      v-model="exportDialogVisible"
      :columns="exportColumnOptions"
      @export="handleExportWithColumns"
    />

    <el-dialog
      v-model="manualScoreDialogVisible"
      title="人工改作业分"
      width="460px"
      append-to-body
    >
      <el-descriptions :column="2" border size="small" class="manual-score-desc">
        <el-descriptions-item label="学生">{{ manualScoreForm.studentName }}</el-descriptions-item>
        <el-descriptions-item label="课程">{{ manualScoreForm.lessonTitle }}</el-descriptions-item>
        <el-descriptions-item label="原始作业分">{{ manualScoreForm.originalScore }}</el-descriptions-item>
        <el-descriptions-item label="当前作业分">{{ manualScoreForm.currentScore }}</el-descriptions-item>
        <el-descriptions-item label="课堂表现">{{ manualScoreForm.performanceScore > 0 ? '+' : '' }}{{ manualScoreForm.performanceScore }}</el-descriptions-item>
        <el-descriptions-item label="预估总分">{{ manualScorePreviewFinal }}</el-descriptions-item>
      </el-descriptions>

      <el-form :model="manualScoreForm" label-width="86px" class="manual-score-form">
        <el-form-item label="作业分">
          <el-input-number
            v-model="manualScoreForm.adjustedScore"
            :min="0"
            :max="100"
            :step="1"
            step-strictly
            controls-position="right"
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="原因">
          <el-input
            v-model="manualScoreForm.reason"
            type="textarea"
            maxlength="255"
            show-word-limit
            :autosize="{ minRows: 3, maxRows: 5 }"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="manualScoreDialogVisible = false">取消</el-button>
        <el-button
          v-if="manualScoreForm.manualAdjusted"
          type="warning"
          plain
          :loading="manualScoreSaving"
          @click="cancelManualScore"
        >取消修正</el-button>
        <el-button type="primary" :loading="manualScoreSaving" @click="submitManualScore">保存</el-button>
      </template>
    </el-dialog>
    
  </div>
</template>

<script setup name="ScoreQuery">
import { ref, watch, onMounted, nextTick, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { resolveBlobDownloadFilename } from '@/utils/downloadFilename';
import { getScoreClasses, getScoreLessons, getScoreSummary, exportScoreExcel, getQuestionAnalysis, getStudentAnswerMatrix, setStudentAbsent, saveManualHomeworkScore, cancelManualHomeworkScore, getGuideSheetScoreContext } from '@/api/business/score';
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus';
import { FullScreen, Search, Download, Setting, Calendar, EditPen } from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import { isSessionExpiredError } from '@/utils/session';
import { calculateGradeNumber } from '@/utils/academicYear';
import { questionTypeLabel } from '@/utils/questionType';

import StudentRankList from './components/GradeOverview/StudentRankList.vue';
import ClassScoreChart from './components/charts/ClassScoreChart.vue';
import RankChart from './components/charts/RankChart.vue';
import TypingChart from './components/charts/TypingChart.vue';
import CourseComparisonChart from './components/charts/CourseComparisonChart.vue';
import StudentProfileDialog from './components/StudentProfileDialog.vue';
import AnalysisMatrix from './components/AnalysisMatrix.vue';
import GradeRatioDialog from './components/GradeRatioDialog.vue';
import ExportDialog from './components/ExportDialog.vue';
import PerformanceSection from './components/PerformanceSection.vue';



const route = useRoute();
const router = useRouter();
const loading = ref(false);
const matrixLoading = ref(false);
const matrixData = ref([]);
const yearOptions = ref([]);
const classOptions = ref([]);
const lessonOptions = ref([]);
const dropdownLessonIds = ref([]);
const rawData = ref([]);
const tableData = ref([]);
const selectedLessonIds = ref([]);
const searchKeyword = ref('');
const guideSheetContext = ref(null);
const guideContextLoading = ref(false);
let guideContextRequestId = 0;

// 图表相关 - 仅保留答题分析图表
const analysisChartRef = ref(null);
let analysisChartInstance = null;



// 学生画像弹窗
const profileDialogVisible = ref(false);
const currentStudent = ref(null);

// 答题分析相关
const analysisData = ref([]);
const analysisLoading = ref(false);

const hasTheoryQuestions = computed(() => {
  return analysisData.value && analysisData.value.some(q => q.questionType === 'choice' || q.questionType === 'judgment');
});

// 等级比例设置
const ratioDialogVisible = ref(false);
const gradeRatios = ref({ excellent: 25, good: 40, pass: 30, fail: 5 });

// 导出对话框
const exportDialogVisible = ref(false);

// 人工改分弹窗
const manualScoreDialogVisible = ref(false);
const manualScoreSaving = ref(false);
const manualScoreForm = ref({
  studentId: null,
  lessonId: null,
  studentName: '',
  lessonTitle: '',
  originalScore: 0,
  currentScore: 0,
  performanceScore: 0,
  adjustedScore: 0,
  reason: '',
  manualAdjusted: false
});

// 排除0分学生开关（默认不排除）
const excludeZeroScore = ref(false);

// 列显示配置
const columnSettingsVisible = ref(false);
const visibleColumns = ref({
  remark: true,
  avgTyping: true,
  overallTypingSpeed: true,
  overallAccuracy: true,
  overallCompletion: true,
  avgTheory: true,
  avgPractical: true,
  filteredTotal: true,
  totalPerformance: true,
  finalTotal: true,
  filteredAverage: false,
  gradeLevel: true,
  scaledScore: true
});

// 列配置选项
const columnOptions = [
  { key: 'remark', label: '备注' },
  { key: 'avgTyping', label: '打字平均' },
  { key: 'overallTypingSpeed', label: '打字速度' },
  { key: 'overallAccuracy', label: '打字正确率' },
  { key: 'overallCompletion', label: '打字完成率' },
  { key: 'avgTheory', label: '理论平均' },
  { key: 'avgPractical', label: '操作平均' },
  { key: 'filteredTotal', label: '作业总分' },
  { key: 'totalPerformance', label: '课堂表现分' },
  { key: 'finalTotal', label: '课程总分' },
  { key: 'filteredAverage', label: '平均分' },
  { key: 'gradeLevel', label: '等级' },
  { key: 'scaledScore', label: '赋分' }
];

// 导出列配置
const exportColumnOptions = computed(() => [
  { key: 'userName', label: '账号', required: true },
  { key: 'className', label: '班级', required: true },
  { key: 'studentNo', label: '学号', required: true },
  { key: 'studentName', label: '姓名', required: true },
  { key: 'remark', label: '备注', required: false },
  { key: 'avgTyping', label: '打字平均', required: false },
  { key: 'overallTypingSpeed', label: '打字速度', required: false },
  { key: 'overallAccuracy', label: '打字正确率', required: false },
  { key: 'overallCompletion', label: '打字完成率', required: false },
  { key: 'avgTheory', label: '理论平均', required: false },
  { key: 'avgPractical', label: '操作平均', required: false },
  { key: 'filteredTotal', label: '作业分', required: false },
  { key: 'totalPerformance', label: '课堂表现分', required: false },
  { key: 'finalTotal', label: '课程总分', required: false },
  { key: 'filteredAverage', label: '平均分', required: false },
  { key: 'gradeLevel', label: '等级', required: false },
  { key: 'scaledScore', label: '赋分', required: false }
]);

// 处理等级比例确认
function handleRatioConfirm(newRatios) {
  gradeRatios.value = newRatios;
}

// 计算等级和赋分的数据
const displayDataWithGrade = computed(() => {
  const data = displayData.value;
  if (data.length === 0) return [];
  
  // 等级与赋分按课程总分口径计算，避免多课时被作业分带偏。
  const getRankScore = (student) => Number(student.finalTotal ?? student.filteredAverage ?? 0);
  const validStudents = data.filter(s => getRankScore(s) > 0);
  const zeroStudents = data.filter(s => getRankScore(s) <= 0);
  
  // 按课程总分/课程平均分排名计算等级
  const sortedByTotal = [...validStudents].sort((a, b) => getRankScore(b) - getRankScore(a));
  const totalCount = sortedByTotal.length;
  
  const gradeMap = new Map();
  const scoreMap = new Map();
  
  if (totalCount > 0) {
    // 计算各等级的人数边界
    const excellentCount = Math.ceil(totalCount * gradeRatios.value.excellent / 100);
    const goodCount = Math.ceil(totalCount * gradeRatios.value.good / 100);
    const passCount = Math.ceil(totalCount * gradeRatios.value.pass / 100);
    
    // 为每个学生分配等级
    sortedByTotal.forEach((student, index) => {
      let grade;
      if (index < excellentCount) {
        grade = '优秀';
      } else if (index < excellentCount + goodCount) {
        grade = '良好';
      } else if (index < excellentCount + goodCount + passCount) {
        grade = '及格';
      } else {
        grade = '不及格';
      }
      gradeMap.set(student.studentId, grade);
    });
    
    // 按平均分排名计算赋分（并列名次赋相同分数）
    const sortedByAvg = [...validStudents].sort((a, b) => getRankScore(b) - getRankScore(a));
    
    if (totalCount === 1) {
      scoreMap.set(sortedByAvg[0].studentId, 100);
    } else {
      let currentRank = 0;
      let prevAvg = null;
      
      sortedByAvg.forEach((student, index) => {
        const currentScore = getRankScore(student);
        if (prevAvg === null || currentScore !== prevAvg) {
          currentRank = index;
        }
        prevAvg = currentScore;
        
        const scaledScore = Math.round(100 - (currentRank / (totalCount - 1)) * 45);
        scoreMap.set(student.studentId, scaledScore);
      });
    }
  }
  
  // 0分学生标记为未评级
  zeroStudents.forEach(student => {
    gradeMap.set(student.studentId, '-');
    scoreMap.set(student.studentId, '-');
  });
  
  // 根据开关决定是否排除0分学生
  const resultData = excludeZeroScore.value ? validStudents : data;
  
  return resultData.map(student => ({
    ...student,
    gradeLevel: gradeMap.get(student.studentId) || '-',
    scaledScore: scoreMap.get(student.studentId) || '-'
  }));
});



const queryParams = ref({
  entryYear: null,
  classCode: null
});

// 计算属性：是否为年级概览模式（未选择特定班级）
const isGradeMode = computed(() => !queryParams.value.classCode);

// 搜索过滤后的数据
const displayData = computed(() => {
  return tableData.value;
});

const isMultiScoreMode = computed(() => selectedLessonIds.value.length !== 1);

const scoreLabels = computed(() => {
  const multiMode = isMultiScoreMode.value;
  return {
    filteredTotal: multiMode ? '作业平均' : '作业分',
    totalPerformance: multiMode ? '课堂表现平均' : '课堂表现分',
    finalTotal: multiMode ? '课程平均分' : '课程总分',
    filteredAverage: multiMode ? '平均分' : '平均分'
  };
});

function roundOne(value) {
  return Math.round(Number(value || 0) * 10) / 10;
}

function formatScoreDisplay(value, keepOneDecimal = false) {
  const num = Number(value || 0);
  return keepOneDecimal ? roundOne(num).toFixed(1) : String(Math.round(num));
}

function clampClientScore(score) {
  const num = Number(score || 0);
  return Math.min(Math.max(Math.round(num), 0), 100);
}

const manualScorePreviewFinal = computed(() => {
  return clampClientScore(Number(manualScoreForm.value.adjustedScore || 0) + Number(manualScoreForm.value.performanceScore || 0));
});

// 计算最大总分（用于 Data Bar 比例）
const maxTotal = computed(() => {
  if (tableData.value.length === 0) return 100;
  return Math.max(...tableData.value.map(s => s.filteredTotal || 0), 1);
});

// 计算 Data Bar 宽度百分比
function getBarWidth(value, max) {
  if (!value || !max) return 0;
  return Math.min(100, Math.round((value / max) * 100));
}

onMounted(async () => {
  await loadClasses();
  
  const urlLessonId = route.query.lessonId;
  const urlEntryYear = route.query.entryYear;
  const urlClassCode = route.query.classCode;
  
  if (urlEntryYear) {
    queryParams.value.entryYear = urlEntryYear;
    if (urlClassCode) {
      queryParams.value.classCode = urlClassCode;
    }
    
    if (window._allClasses) {
      classOptions.value = window._allClasses
        .filter(c => (c.entry_year || c.entryYear) === urlEntryYear)
        .map(c => ({ classCode: c.class_code || c.classCode }))
        // 班级号可能含字母，禁止 parseInt 产生 NaN 打乱排序
        .sort((a, b) => naturalCodeCompare(a.classCode, b.classCode));
    }
    
    const lessonRes = await getScoreLessons(urlEntryYear);
    lessonOptions.value = lessonRes.data || [];
    
    if (urlLessonId) {
      const lessonIdNum = Number(urlLessonId);
      selectedLessonIds.value = [lessonIdNum];
      dropdownLessonIds.value = [lessonIdNum];
    }
    
    handleQuery();
  }
});

function loadClasses() {
  return getScoreClasses().then(res => {
    const data = res.data || [];
    // 防御性过滤：排除 entry_year 或 class_code 为空的无效记录
    const validData = data.filter(item => item && (item.entry_year || item.entryYear) && (item.class_code || item.classCode));
    const yearSet = new Set();
    validData.forEach(item => yearSet.add(item.entry_year || item.entryYear));
    yearOptions.value = Array.from(yearSet).map(y => ({ entryYear: y })).sort((a, b) => b.entryYear - a.entryYear);
    window._allClasses = validData;
  });
}

function onYearChange(val) {
  guideSheetContext.value = null;
  queryParams.value.classCode = null;
  tableData.value = [];
  rawData.value = [];
  selectedLessonIds.value = [];
  dropdownLessonIds.value = [];
  lessonOptions.value = [];
  
  if (val && window._allClasses) {
    classOptions.value = window._allClasses
      .filter(c => c && (c.entry_year || c.entryYear) === val && (c.class_code || c.classCode))
      .map(c => ({ classCode: c.class_code || c.classCode }))
      // 班级号可能含字母，禁止 parseInt 产生 NaN 打乱排序
      .sort((a, b) => naturalCodeCompare(a.classCode, b.classCode));
  }
  
  if (val) {
    getScoreLessons(val).then(res => {
      lessonOptions.value = res.data || [];
      
      // 如果课程列表为空，清空已选择的课程（避免显示ID而非名称）
      if (lessonOptions.value.length === 0) {
        dropdownLessonIds.value = [];
        selectedLessonIds.value = [];
      }
    });
  }
}

function onClassChange() {
  guideSheetContext.value = null;
  tableData.value = [];
  rawData.value = [];
}

async function refreshGuideSheetContext() {
  const requestId = ++guideContextRequestId;
  guideSheetContext.value = null;
  if (selectedLessonIds.value.length !== 1 || !queryParams.value.entryYear || !queryParams.value.classCode) {
    guideContextLoading.value = false;
    return;
  }
  guideContextLoading.value = true;
  try {
    const response = await getGuideSheetScoreContext(
      selectedLessonIds.value[0],
      queryParams.value.entryYear,
      queryParams.value.classCode
    );
    if (requestId !== guideContextRequestId) return;
    const context = response.data || response;
    const bindingId = context?.bindingId ?? context?.currentBindingId;
    const enabled = context?.enabled ?? context?.guideSheetEnabled;
    guideSheetContext.value = enabled && bindingId ? { ...context, enabled: true, bindingId } : null;
  } catch (_error) {
    // 后端权限是最终边界，失败时不暴露成绩入口。
    if (requestId === guideContextRequestId) guideSheetContext.value = null;
  } finally {
    if (requestId === guideContextRequestId) guideContextLoading.value = false;
  }
}

function openGuideSheetScores() {
  const context = guideSheetContext.value;
  if (!context?.bindingId) return;
  router.push({
    name: 'GuideSheetDashboard',
    params: { bindingId: context.bindingId },
    query: {
      from: 'score',
      lessonId: selectedLessonIds.value[0],
      entryYear: queryParams.value.entryYear,
      classCode: queryParams.value.classCode
    }
  });
}

// 获取正确率颜色
function getAccuracyColor(accuracy) {
  if (accuracy >= 80) return '#67C23A';
  if (accuracy >= 60) return '#E6A23C';
  return '#F56C6C';
}

// 计算选项分布百分比
function getDistPercent(count, total) {
  if (!total || total === 0) return 0;
  return Math.min(100, Math.round((count / total) * 100));
}

function handleQuery() {
  if (!queryParams.value.entryYear) {
    ElMessage.warning('请选择入学年份');
    return;
  }
  
  loading.value = true;
  refreshGuideSheetContext();
  
  getScoreSummary(
    queryParams.value.entryYear,
    queryParams.value.classCode,
    selectedLessonIds.value,
    searchKeyword.value.trim() || null
  )
    .then(res => {
      rawData.value = res.rows || res.data || [];
      processData();
      // 使用延时确保 DOM 完全渲染后再初始化图表
      nextTick(() => {
        setTimeout(() => {
          renderCharts();
        }, 100);
      });
      // 如果是单课程，自动加载分析
      if (selectedLessonIds.value.length === 1) {
        loadAnalysis(selectedLessonIds.value[0]);
      } else {
        analysisData.value = [];
      }
    })
    .finally(() => {
      loading.value = false;
    });
}

function toggleLesson(lessonId, checked) {
  if (checked) {
    if (!selectedLessonIds.value.includes(lessonId)) {
      selectedLessonIds.value.push(lessonId);
    }
  } else {
    selectedLessonIds.value = selectedLessonIds.value.filter(id => id !== lessonId);
  }
  dropdownLessonIds.value = [...selectedLessonIds.value];
  handleQuery();
}

function clearSelection() {
  selectedLessonIds.value = [];
  dropdownLessonIds.value = [];
  handleQuery();
}

function onDropdownChange(val) {
  selectedLessonIds.value = [...val];
  handleQuery();
}

function filterStudents() {
  // 使用 computed displayData 自动过滤
}

function calculateGrade(entryYear) {
  // 本页历史上按初中年级展示；日期边界统一使用平台的 7 月 20 日规则。
  return calculateGradeNumber(entryYear, '2') || 0;
}

function processData() {
  const selectedIds = selectedLessonIds.value;
  const entryYear = parseInt(queryParams.value.entryYear);
  const multiMode = !selectedIds || selectedIds.length !== 1;
  
  tableData.value = rawData.value.map(student => {
    let className = '';
    if (student.classCode) {
      // 使用后端返回的年级 + 班级号，格式如 "601"
      const grade = student.grade || calculateGrade(entryYear);
      const code = String(student.classCode).padStart(2, '0');
      className = `${grade}${code}`;
    }

    let filteredScores = student.scores || [];
    if (selectedIds && selectedIds.length > 0) {
      filteredScores = filteredScores.filter(s => selectedIds.includes(s.lessonId));
    }
    
    let sumTyping = 0, sumTheory = 0, sumPractical = 0, sumTotal = 0;
    let sumPerformance = 0, sumFinal = 0; // 课堂表现分和课程总分
    let validScoreCount = 0; // 有效（非请假）课次数
    
    // 打字统计：累加有效记录
    let typingSpeedSum = 0, accuracySum = 0, completionSum = 0, typingCount = 0;
    
    filteredScores.forEach(s => {
      if (s.isAbsent) return; // 缺考请假的课程不参与均分计算
      
      validScoreCount++;
      sumTyping += (s.typingScore || 0);
      sumTheory += (s.theoryScore || 0);
      sumPractical += (s.practicalScore || 0);
      sumTotal += (s.totalScore || 0);
      sumPerformance += (s.performanceScore || 0);
      sumFinal += (s.finalScore ?? s.totalScore ?? 0); // 课程总分
      
      // 累加打字统计（只统计有数据的记录）
      if (s.avgTypingSpeed) {
        typingSpeedSum += Number(s.avgTypingSpeed);
        accuracySum += Number(s.avgAccuracyRate || 0);
        completionSum += Number(s.avgCompletionRate || 0);
        typingCount++;
      }
    });
    
    const avgTyping = validScoreCount > 0 ? Math.round(sumTyping / validScoreCount) : 0;
    const avgTheory = validScoreCount > 0 ? Math.round(sumTheory / validScoreCount) : 0;
    const avgPractical = validScoreCount > 0 ? Math.round(sumPractical / validScoreCount) : 0;
    const avgHomework = validScoreCount > 0 ? roundOne(sumTotal / validScoreCount) : 0;
    const avgPerformance = validScoreCount > 0 ? roundOne(sumPerformance / validScoreCount) : 0;
    const filteredAverage = validScoreCount > 0 ? roundOne(sumFinal / validScoreCount) : 0;
    
    // 计算整体打字指标
    const overallTypingSpeed = typingCount > 0 ? Math.round(typingSpeedSum / typingCount) : null;
    const overallAccuracy = typingCount > 0 ? Math.round(accuracySum / typingCount) : null; // P0: 取整
    const overallCompletion = typingCount > 0 ? Math.round(completionSum / typingCount) : null; // P0: 取整
    
    return {
      ...student,
      // 学号保留原字符串：字母数字学号 parseInt 会变 NaN，展示与排序都坏
      studentNo: student.studentNo == null ? '' : String(student.studentNo),
      // 班级号同样可能非纯数字（如 9A），Number() 会 NaN
      className: className == null || className === '' ? '' : String(className),
      filteredTotal: multiMode ? avgHomework : Math.round(sumTotal), // 多课模式展示均分，避免总分口径混乱
      filteredAverage: filteredAverage,
      avgTyping: avgTyping,
      avgTheory: avgTheory,
      avgPractical: avgPractical,
      overallTypingSpeed,
      overallAccuracy,
      overallCompletion,
      totalPerformance: multiMode ? avgPerformance : sumPerformance,
      finalTotal: filteredAverage
    };
  }).sort((a, b) => naturalCodeCompare(a.studentNo, b.studentNo));
}

/** 学号/班级号自然序：纯数字按数值，字母数字按数字段拆分比较，永不产生 NaN */
function naturalCodeCompare(a, b) {
  const sa = a == null ? '' : String(a);
  const sb = b == null ? '' : String(b);
  return sa.localeCompare(sb, 'zh-CN', { numeric: true, sensitivity: 'base' });
}

// 渲染图表
// 渲染图表
function renderCharts() {
  // 只渲染分析图表（如果需要）
  if (selectedLessonIds.value.length === 1 && analysisData.value.length > 0) {
      renderAnalysisChart();
  }
}

// 计算是否有打字数据
const hasTypingData = computed(() => {
  return tableData.value.some(s => s.overallTypingSpeed !== null && s.overallTypingSpeed !== undefined);
});

// 打字统计表格数据
const typingTableData = computed(() => {
  return tableData.value
    .filter(s => s.overallTypingSpeed)
    .map(s => ({
      className: s.className,
      studentNo: s.studentNo,
      studentName: s.studentName,
      speed: Number(s.overallTypingSpeed) || 0,
      accuracy: Number(s.overallAccuracy) || 0,
      completion: Number(s.overallCompletion) || 0,
      score: Number(s.avgTyping) || 0
    }))
    .sort((a, b) => b.speed - a.speed);
});



watch(() => selectedLessonIds.value, (newIds) => {
  if (rawData.value.length > 0) {
    processData();
    // 单课程时自动加载分析
    if (newIds.length === 1) {
        if (!isGradeMode.value) {
            loadAnalysis(newIds[0]);
        }
    } else {
        analysisData.value = [];
    }
  } else {
    analysisData.value = [];
  }
}, { deep: true });

// 跳转到学生个人画像页面
function showStudentProfile(row) {
  router.push({
    path: '/business/student-profile',
    query: { studentId: row.studentId }
  });
}

function handleExport(selectedColumns) {
  if (!rawData.value.length) {
    ElMessage.warning('暂无数据可导出');
    return Promise.resolve();
  }
  
  const loadingMsg = ElLoading.service({
    lock: true,
    text: '正在生成 Excel...',
    background: 'rgba(255, 255, 255, 0.65)'
  });
  
  return exportScoreExcel(
    queryParams.value.entryYear, 
    queryParams.value.classCode, 
    selectedLessonIds.value,
    searchKeyword.value.trim() || null,
    selectedColumns
  ).then(res => {
    const blob = res instanceof Blob ? res : new Blob([res]);
    const link = document.createElement('a');
    link.href = window.URL.createObjectURL(blob);
    link.download = resolveBlobDownloadFilename(blob, `成绩汇总_${queryParams.value.entryYear}级.xlsx`);
    link.click();
    window.URL.revokeObjectURL(link.href);
    ElMessage.success('导出成功');
  }).catch((error) => {
    if (isSessionExpiredError(error)) {
      return;
    }
    ElMessage.error(error?.message || '导出失败');
  }).finally(() => {
    loadingMsg.close();
  });
}

// 处理答题分析
// 加载答题分析
function loadAnalysis(lessonId) {
  analysisLoading.value = true;
  analysisData.value = [];
  
  // 传入班级和年份进行过滤
  const params = {
      lessonId,
      classCode: queryParams.value.classCode,
      entryYear: queryParams.value.entryYear
  };
  
  
  getQuestionAnalysis(lessonId, queryParams.value.classCode, queryParams.value.entryYear).then(res => {
    analysisData.value = res.data || [];
    analysisLoading.value = false;
    nextTick(() => {
      renderAnalysisChart();
    });
    // 加载学生答题矩阵
    loadMatrix(lessonId);
  }).catch(() => {
     analysisLoading.value = false;
  });
}

// 加载学生答题矩阵
function loadMatrix(lessonId) {
    matrixLoading.value = true;
    matrixData.value = [];
    getStudentAnswerMatrix(lessonId, queryParams.value.classCode, queryParams.value.entryYear).then(res => {
        // 数据转换：将 results 数组转换为 component 需要的 answersMap 对象列表
        // 同时处理班级显示名称 "601"
        const entryYear = parseInt(queryParams.value.entryYear || 0);
        const grade = calculateGradeNumber(entryYear, '2') || 0;

        const processedData = (res || []).map(student => {
            const answersMap = {};
            if (student.results && Array.isArray(student.results)) {
                student.results.forEach(r => {
                    answersMap[r.questionId] = {
                        studentAnswer: r.userAnswer,
                        // 兼容多种 boolean 表示："1", 1, "T", true
                        isCorrect: r.isCorrect == 1 || r.isCorrect === '1' || r.isCorrect === 'T' || r.isCorrect === true
                    };
                });
            }
            
            // 格式化班级名
            let formattedClassName = String(student.className || '');
            if (grade > 0 && student.className) {
                // 如果班级名只是1-2位数字（如 "1" 或 "04"），则拼接成 "601" 或 "604"
                // 如果班级名已经是3位以上（如 "604"），则认为已格式化，直接使用
                const classNameStr = String(student.className);
                if (!isNaN(student.className) && classNameStr.length <= 2) {
                    const classNum = parseInt(student.className);
                    formattedClassName = `${grade}${classNum < 10 ? '0' + classNum : classNum}`;
                }
            }
            
            return {
                ...student,
                answersMap,
                formattedClassName
            };
        });
        
        matrixData.value = processedData;
    }).catch(e => {
        console.error('加载矩阵失败', e);
    }).finally(() => {
        matrixLoading.value = false;
    });
}

// 渲染矩阵单元格


// 渲染易错题图表
function renderAnalysisChart() {
  if (!analysisChartRef.value) return;
  
  // 检查实例是否已被销毁（DOM被v-if移除后），需要重新初始化
  if (analysisChartInstance) {
    try {
      // 尝试获取实例的DOM，如果抛出异常或返回null说明实例已失效
      const dom = analysisChartInstance.getDom();
      if (!dom || !document.body.contains(dom)) {
        analysisChartInstance.dispose();
        analysisChartInstance = null;
      }
    } catch (e) {
      analysisChartInstance = null;
    }
  }
  
  if (!analysisChartInstance) {
    analysisChartInstance = echarts.init(analysisChartRef.value);
  }
  
  // 1. 数据过滤与排序
  // 过滤掉无人作答的题目
  const validData = analysisData.value.filter(d => d.studentCount > 0);
  
  // 先按错误率取最易错的题，再反转给 ECharts，让最高错误率显示在顶部。
  const sorted = validData
    .map(item => {
      const correct = Number(item.correctCount || 0);
      const total = Number(item.studentCount || 0);
      const wrongCount = Math.max(total - correct, 0);
      const wrongRate = total > 0 ? wrongCount / total : 0;
      return { ...item, wrongCount, wrongRate };
    })
    .sort((a, b) => {
      if (b.wrongRate !== a.wrongRate) return b.wrongRate - a.wrongRate;
      return b.wrongCount - a.wrongCount;
    })
    .slice(0, 10)
    .reverse();
  
  // 2. 准备数据
  const yAxisData = []; // 题目名称
  const correctSeries = []; // 正确人数
  const wrongSeries = [];   // 错误人数
  
  sorted.forEach(item => {
    // 处理题目名称过长
    let title = item.questionContent;
    if (title.length > 15) title = title.substring(0, 15) + '...';
    yAxisData.push(title);
    
    const correct = item.correctCount || 0;
    const total = item.studentCount || 0;
    const wrong = item.wrongCount;
    
    correctSeries.push(correct);
    wrongSeries.push(wrong);
  });

  const option = {
    tooltip: {
       trigger: 'axis',
       backgroundColor: 'rgba(255, 255, 255, 0.95)',
       extraCssText: 'box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);',
       textStyle: { color: '#333' },
       formatter: function(params) {
          // 由于是同一个类目轴，params[0] 对应的数据index是一样的
          const index = params[0].dataIndex;
          const item = sorted[index];
          
          let html = `<div style="max-width:400px; white-space:normal; line-height: 1.6; font-size: 13px;">`;
          
          // 标题头
          html += `<div style="margin-bottom:8px; border-bottom:1px solid #ebeef5; padding-bottom:5px; font-family: 'Microsoft YaHei', 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;">
                      <span style="font-weight:600; font-size:14px; color:#303133;">${item.questionContent}</span>
                   </div>`;
          
          // 核心指标
          html += `<div style="display:flex; justify-content:space-between; margin-bottom:8px;">
                      <span>类型：<b>${questionTypeLabel(item.questionType)}</b></span>
                      <span>正确率：<b style="color:${getAccuracyColor(item.accuracy)}">${item.accuracy}%</b></span>
                      <span>错误率：<b style="color:#F56C6C">${Math.round((item.wrongRate || 0) * 100)}%</b></span>
                   </div>`;
          
          // 选项详情表格
          html += `<table style="width:100%; border-collapse: collapse; font-size: 12px;">
                    <tr style="background:#f5f7fa; color:#909399;">
                        <td style="padding:4px;">选项</td>
                        <td style="padding:4px;">内容</td>
                        <td style="padding:4px; text-align:right;">人数</td>
                    </tr>`;
          
          // 遍历选项
          const opts = item.optionContents || {};
          const dist = item.answerDistribution || {};
          // 合并判断题 Key
          let distMap = { ...dist };
          if (distMap['T']) { distMap['对'] = (distMap['对'] || 0) + distMap['T']; delete distMap['T']; }
          if (distMap['F']) { distMap['错'] = (distMap['错'] || 0) + distMap['F']; delete distMap['F']; }
          
          let keys = item.questionType === 'choice' ? ['A', 'B', 'C', 'D'] : ['对', '错'];
          
          keys.forEach(k => {
             const txt = opts[k] || (k === '对' ? '正确' : (k === '错' ? '错误' : ''));
             const count = distMap[k] || 0;
             const isCorrect = (k === item.answer) || 
                               (item.answer === 'T' && k === '对') || 
                               (item.answer === 'F' && k === '错');
             
             // 样式处理
             const rowBg = isCorrect ? 'background-color:#f0f9eb;' : '';
             const colorStyle = isCorrect ? 'color:#67C23A; font-weight:bold;' : (count > 0 ? 'color:#F56C6C;' : 'color:#C0C4CC;');
             const mark = isCorrect ? '✅' : '';
             
             html += `<tr style="${rowBg}">
                        <td style="padding:4px; font-weight:bold;">${k} ${mark}</td>
                        <td style="padding:4px; ${colorStyle}">${txt || '-'}</td>
                        <td style="padding:4px; text-align:right; font-weight:bold;">${count}</td>
                      </tr>`;
          });
          
          html += `</table></div>`;
          return html;
       }
    },
    legend: {
       data: ['正确人数', '错误人数'],
       top: 0
    },
    grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
    },
    xAxis: {
      type: 'value',
      position: 'top', // X轴放在上面更容易阅读
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    yAxis: {
      type: 'category',
      data: yAxisData,
      axisLabel: { 
          interval: 0,
          width: 150,
          overflow: 'truncate',
          textStyle: { 
            fontSize: 14, 
            fontFamily: '"Microsoft YaHei", "PingFang SC", "Helvetica Neue", Arial, sans-serif' 
          },
          formatter: function (value) {
              return value;
          }
      },
      axisTick: { show: false }
    },
    series: [
      {
        name: '正确人数',
        type: 'bar',
        stack: 'total',
        label: { show: true, position: 'inside', formatter: (p) => p.value > 0 ? p.value : '' },
        itemStyle: { color: '#52c41a' }, // 绿色
        data: correctSeries
      },
      {
        name: '错误人数',
        type: 'bar',
        stack: 'total',
        label: { show: true, position: 'inside', formatter: (p) => p.value > 0 ? p.value : '' },
        itemStyle: { color: '#ff4d4f' }, // 红色
        data: wrongSeries
      }
    ]
  };
  
  analysisChartInstance.setOption(option);
}



function getLessonName(lessonId) {
    const l = lessonOptions.value.find(item => item.lessonId === lessonId);
    return l ? l.lessonTitle : `课程${lessonId}`;
}

function getSelectedLessonScore(student) {
    if (!student?.scores || selectedLessonIds.value.length !== 1) return null;
    return student.scores.find(item => item.lessonId === selectedLessonIds.value[0]) || null;
}

function getLessonScore(student, lessonId) {
    if (!student.scores) return 0;
    const s = student.scores.find(item => item.lessonId === lessonId);
    return s ? (s.finalScore ?? s.totalScore ?? 0) : 0;
}

function getLessonScoreDisplay(student, lessonId) {
    if (!student.scores) return 0;
    const s = student.scores.find(item => item.lessonId === lessonId);
    if (s && s.isAbsent) return '请假';
    if (!s) return 0;
    const performance = s.performanceScore || 0;
    const performanceText = performance > 0 ? `+${performance}` : String(performance);
    const manualText = s.manualAdjusted ? '修' : '';
    return `${manualText}${s.totalScore || 0}/${performanceText}/${s.finalScore ?? s.totalScore ?? 0}`;
}

function isLessonAbsent(student, lessonId) {
    if (!student.scores) return false;
    const s = student.scores.find(item => item.lessonId === lessonId);
    return s ? !!s.isAbsent : false;
}

const handleAbsent = async (studentId, lessonId, isAbsent) => {
  try {
    const actionText = isAbsent ? '设为请假' : '取消请假';
    await ElMessageBox.confirm(`确定要将该生本节课的成绩状态${actionText}吗？`, '提示', {
      type: 'warning'
    });
    
    await setStudentAbsent(studentId, lessonId, isAbsent);
    ElMessage.success(`${actionText}成功`);
    handleQuery(); // 重新加载数据
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e);
      ElMessage.error('操作失败');
    }
  }
};

function openManualScoreDialog(row) {
  const lessonId = selectedLessonIds.value[0];
  const score = getSelectedLessonScore(row) || {
    lessonId,
    lessonTitle: getLessonName(lessonId),
    originalTotalScore: 0,
    totalScore: 0,
    performanceScore: 0,
    manualAdjusted: false
  };
  if (score.isAbsent) {
    ElMessage.warning('该学生本节课已请假，请先取消请假后再改分');
    return;
  }

  manualScoreForm.value = {
    studentId: row.studentId,
    lessonId,
    studentName: row.studentName,
    lessonTitle: score.lessonTitle || getLessonName(lessonId),
    originalScore: score.originalTotalScore ?? score.totalScore ?? 0,
    currentScore: score.totalScore ?? 0,
    performanceScore: score.performanceScore || 0,
    adjustedScore: score.totalScore ?? 0,
    reason: score.adjustmentReason || '',
    manualAdjusted: !!score.manualAdjusted
  };
  manualScoreDialogVisible.value = true;
}

async function submitManualScore() {
  const adjustedScore = Number(manualScoreForm.value.adjustedScore);
  const reason = (manualScoreForm.value.reason || '').trim();
  if (!Number.isFinite(adjustedScore) || adjustedScore < 0 || adjustedScore > 100) {
    ElMessage.warning('作业分必须在0到100之间');
    return;
  }
  if (!reason) {
    ElMessage.warning('请填写改分原因');
    return;
  }

  manualScoreSaving.value = true;
  try {
    await saveManualHomeworkScore({
      studentId: manualScoreForm.value.studentId,
      lessonId: manualScoreForm.value.lessonId,
      adjustedScore: Math.round(adjustedScore),
      reason
    });
    ElMessage.success('改分成功');
    manualScoreDialogVisible.value = false;
    handleQuery();
  } catch (e) {
    ElMessage.error(e?.msg || e?.message || '改分失败');
  } finally {
    manualScoreSaving.value = false;
  }
}

async function cancelManualScore() {
  try {
    await ElMessageBox.confirm('确定取消该学生本节课的人工修正吗？', '提示', {
      type: 'warning'
    });
  } catch (e) {
    return;
  }

  manualScoreSaving.value = true;
  try {
    await cancelManualHomeworkScore({
      studentId: manualScoreForm.value.studentId,
      lessonId: manualScoreForm.value.lessonId,
      reason: '取消人工修正'
    });
    ElMessage.success('已取消修正');
    manualScoreDialogVisible.value = false;
    handleQuery();
  } catch (e) {
    ElMessage.error(e?.msg || e?.message || '取消修正失败');
  } finally {
    manualScoreSaving.value = false;
  }
}

function getScoreClass(score) {
    if (score >= 90) return 'text-success';
    if (score < 60) return 'text-danger';
    return '';
}

function getScoreType(score) {
  if (score >= 90) return 'success';
  if (score >= 60) return 'primary';
  return 'danger';
}

// 等级标签颜色
function getGradeTagType(grade) {
  const typeMap = {
    '优秀': 'success',
    '良好': 'primary',
    '及格': 'warning',
    '不及格': 'danger'
  };
  return typeMap[grade] || 'info';
}

// 服务端全量导出，避免分页后只导出当前页。
async function handleExportWithColumns(selectedColumns) {
  if (!rawData.value.length) {
    ElMessage.warning('暂无数据可导出');
    return;
  }

  try {
    // 服务端导出按当前筛选条件生成全量数据，不受当前分页限制。
    await handleExport(selectedColumns);
  } catch (e) {
    ElMessage.error('导出失败：' + e.message);
  }
}



</script>

<style lang="scss" scoped>
.filter-card {
  margin-bottom: 15px;
  
  .filter-row {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-wrap: wrap;
  }
  
  .filter-label {
    color: #606266;
    font-weight: bold;
  }
  
  .guide-score-hint-tag {
    margin-left: 4px;
    vertical-align: middle;
  }
  .selected-tip {
    margin-left: 15px;
    color: #67C23A;
    font-size: 13px;
  }
}

.chart-row {
  margin-bottom: 15px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  
  .header-actions {
    display: flex;
    gap: 10px;
  }
}

.chart-card {
  position: relative;
  
  .chart-container {
    height: 560px;  // 原来280px，增高2倍
    background: #fff;
    padding: 10px;
  }
  
  // 头部全屏按钮样式
  :deep(.el-card__header) {
      display: flex;
      justify-content: space-between;
      align-items: center;
  }
}







.data-card {
  .score-list {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }
  
  .score-item {
    display: flex;
    align-items: center;
    gap: 4px;
    
    .lesson-name {
      font-size: 12px;
      color: #909399;
    }
    
    .selected-tag {
      box-shadow: 0 0 0 2px #67C23A;
    }
  }
  
  .score-detail {
    p {
      margin: 5px 0;
    }
    b {
      color: #606266;
    }
  }
  
  .total-score {
    font-size: 16px;
    font-weight: bold;
    color: #409EFF;
  }
  
  .avg-score {
    font-size: 16px;
    font-weight: bold;
    color: #67C23A;
  }
  
  .gray-text {
    color: #606266;
  }
  
  .typing-speed {
    font-weight: bold;
    color: #E6A23C;
    
    small {
      font-size: 10px;
      font-weight: normal;
      color: #909399;
    }
  }
  
  .typing-detail {
    p {
      margin: 5px 0;
    }
    b {
      color: #606266;
    }
  }
  
  .typing-accuracy {
    font-weight: bold;
    color: #67C23A;
  }
  
  .typing-completion {
    font-weight: bold;
    color: #409EFF;
  }
  
  // Data Bar 样式
  .data-bar-cell {
    position: relative;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .data-bar {
      position: absolute;
      left: 0;
      top: 2px;
      bottom: 2px;
      background: linear-gradient(90deg, #e6f4ff, #bae0ff);
      border-radius: 3px;
      transition: width 0.3s ease;
    }
    
    .data-bar-value {
      position: relative;
      z-index: 1;
    }
    
    // 答题分析弹窗样式
    .question-type-tag {
      font-size: 12px;
      font-weight: bold;
      margin-right: 5px;
      
      &.choice { color: #409EFF; }
      &.judgment { color: #E6A23C; }
    }
    
    .dist-bar-container {
      display: flex;
      flex-direction: column;
      gap: 4px;
      
      .dist-item {
        display: flex;
        align-items: center;
        width: 100%;
        
        .dist-label {
          width: 20px;
          text-align: center;
          font-weight: bold;
          margin-right: 5px;
          color: #909399;
          
          &.correct {
            color: #67C23A;
            text-decoration: underline;
          }
        }
        
        .dist-bar-bg {
          flex: 1;
          height: 10px;
          background-color: #f0f2f5;
          border-radius: 5px;
          margin-right: 8px;
          overflow: hidden;
          
          .dist-bar {
            height: 100%;
            background-color: #409EFF;
          }
        }
        
        .dist-count {
          font-size: 12px;
          color: #606266;
          width: 40px;
        }
      }
    }
    
    &.avg-bar .data-bar {
      background: linear-gradient(90deg, #f0f9eb, #c6e6b8);
    }
  }
  
  .empty-tip {
    text-align: center;
    padding: 40px;
    color: #909399;
  }
}

.manual-score-mark {
  margin-left: 4px;
  vertical-align: 1px;
}

.manual-score-desc {
  margin-bottom: 16px;
}

.manual-score-form {
  padding-top: 4px;
}

.profile-content {
  .profile-header {
    display: flex;
    gap: 30px;
    margin-bottom: 15px;
    padding: 15px;
    background: #f5f7fa;
    border-radius: 8px;
    
    span {
      font-size: 14px;
      color: #606266;
    }
  }
  
  .profile-filters {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
    gap: 10px;
    padding: 10px 0;
    border-bottom: 1px solid #ebeef5;
  }
  
  .profile-chart {
    height: 280px;
  }
}

// 打字图表头部样式
.typing-chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.typing-chart-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>

.text-success {
  color: #67C23A;
  font-weight: bold;
}
.text-danger {
  color: #F56C6C;
  font-weight: bold;
}
