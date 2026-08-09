<template>
  <div class="app-container grading-page" ref="gradingPageRef">
    <!-- 顶部控制栏 -->
    <div class="grading-header" v-show="!isFullscreen || (selectedClassCode && deadlineStatus)">
      <div class="grading-toolbar" v-show="!isFullscreen">
       <div class="left-filters">
        <span class="filter-label">课程：</span>
        <el-select v-model="selectedLessonId" placeholder="请选择课程" @change="onLessonChange" style="width: 200px">
          <el-option-group v-for="group in gradeGroups" :key="group.entryYear" :label="group.entryYear + '级 ' + group.gradeName">
            <el-option v-for="l in group.lessons" :key="l.lessonId" :label="l.lessonTitle" :value="l.lessonId" />
          </el-option-group>
        </el-select>
        
        <span class="filter-label" style="margin-left: 16px">班级：</span>
        <el-select v-model="selectedClassCode" placeholder="请选择班级" @change="onClassChange" :disabled="!selectedLessonId || classes.length === 0" style="width: 180px">
          <el-option v-for="c in classes" :key="c.classCode" :value="c.classCode">
            <div class="class-option" :class="getClassOptionClass(c)">
              <span>{{ c.classCode }}班</span>
              <span v-if="c.practicalUngraded > 0" class="ungraded-badge">{{ c.practicalUngraded }}人未批</span>
              <span v-else-if="c.practicalSubmitted > 0" class="graded-badge">✓</span>
              <span v-else class="no-submit-badge">暂无提交</span>
            </div>
          </el-option>
        </el-select>
        
        <!-- 无班级提示 -->
        <el-tag v-if="selectedLessonId && classes.length === 0 && !loading" type="warning" style="margin-left: 8px">
          暂无学生提交作业
        </el-tag>
        
        <span class="filter-label" style="margin-left: 16px">操作题：</span>

        <!-- 只有一道操作题时直接显示题目名称 -->
        <span v-if="questions.length === 1" class="single-question-name" style="font-weight: 500; color: #303133; max-width: 280px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: inline-block; vertical-align: middle; line-height: 32px; height: 32px; padding: 0 11px; border: 1px solid #dcdfe6; border-radius: 4px; background: #f5f7fa;">
          {{ questions[0].questionContent }}
        </span>
        <!-- 多道操作题时显示下拉框 -->
        <el-select v-else v-model="selectedQuestionId" placeholder="请选择操作题" @change="onQuestionChange" :disabled="!selectedClassCode" style="width: 280px">
          <el-option v-for="q in questions" :key="q.questionId" :label="q.questionContent" :value="q.questionId" />
        </el-select>
       </div>
      
       <div class="right-actions">
        <el-button plain @click="openAiConfig">AI 设置</el-button>
        <el-button
          type="success"
          plain
          :loading="aiStarting"
          :disabled="!canStartAiJob"
          @click="openAiJobDialog"
        >
          批量生成 AI 建议
        </el-button>
        <el-button v-if="aiJob" type="primary" plain :loading="aiBatchApplying"
          :disabled="!canBatchApplyAiSuggestions" @click="applyAiSuggestionsInBatch">
          批量采用 AI 建议
        </el-button>
        <el-tag v-if="aiJob" :type="aiJobTagType">{{ aiJobStatusText }}</el-tag>
        <el-button v-if="aiJob" link type="primary" @click="openAiJobDetail">AI 处理详情</el-button>
        <el-button v-if="['PENDING','RUNNING'].includes(aiJob?.jobStatus)" link type="warning" @click="controlAiJob('pause')">暂停</el-button>
        <el-button v-if="aiJob?.jobStatus === 'PAUSED' && aiJobBatchAdoptAllowed" link type="success" @click="controlAiJob('resume')">继续</el-button>
        <el-tag v-if="aiJob?.jobStatus === 'PAUSED' && !aiJobBatchAdoptAllowed" type="danger">旧任务缺参考答案，请取消后重建</el-tag>
        <el-button v-if="['PENDING','RUNNING','PAUSED'].includes(aiJob?.jobStatus)" link type="danger" @click="controlAiJob('cancel')">取消</el-button>
        <el-button v-if="['FAILED','PARTIAL_FAILED'].includes(aiJob?.jobStatus)" link type="warning" @click="controlAiJob('retry')">重试失败项</el-button>
        <el-button
          type="warning"
          plain
          :loading="retryLoading"
          :disabled="!canRetryFailedPreviews"
          @click="handleRetryFailedPreviews"
        >
          重新转换本班异常文件
        </el-button>
        <el-button type="primary" plain @click="toggleFullscreen">
           <el-icon><FullScreen /></el-icon> {{ isFullscreen ? '退出全屏' : '全屏批改' }}
        </el-button>
       </div>
      </div>

      <div
        v-if="selectedClassCode && deadlineStatus"
        class="deadline-status-panel"
        :class="`is-${String(deadlineStatus.statusCode || '').toLowerCase()}`"
      >
      <div class="deadline-panel-head">
        <div class="deadline-summary">
          <strong>批改进度</strong>
          <el-tag :type="deadlineStatusMeta(deadlineStatus.statusCode).type">
            {{ deadlineStatusMeta(deadlineStatus.statusCode).label }}
          </el-tag>
          <span>已有答题：{{ deadlineStatus.answeredStudentCount }}/{{ deadlineStatus.totalStudentCount }}</span>
          <span>应批/已批/未批：{{ deadlineStatus.dueCount }}/{{ deadlineStatus.gradedCount }}/{{ deadlineStatus.ungradedCount }}</span>
        </div>
        <strong>{{ formatDeadlineRemaining(deadlineStatus) }}</strong>
      </div>
      <el-alert
        v-if="!deadlineStatus.canGrade"
        title="已逾期，操作题批改已锁定"
        description="已有成绩和学生提交仍可查看；如需继续批改，请联系教研员调整截止时间。"
        type="error"
        :closable="false"
        show-icon
      />
      <div class="deadline-panel-grid">
        <span>触发时间：{{ formatDeadlineTime(deadlineStatus.triggerTime) }}</span>
        <span>截止时间：{{ formatDeadlineTime(deadlineStatus.currentDeadlineTime) }}</span>
      </div>
      <el-progress
        :percentage="deadlineStatus.dueCount ? Math.round(deadlineStatus.gradedCount * 100 / deadlineStatus.dueCount) : 0"
        :status="deadlineStatus.statusCode === 'COMPLETED' ? 'success' : undefined"
      />
      </div>
    </div>

    <!-- 主工作区 -->
    <div ref="gradingMainRef" class="grading-main" v-loading="loading">
      <!-- 左侧：学生列表 -->
      <div class="student-list-panel">
         <div class="panel-title">
            <span>学生列表</span>
             <span class="grading-stats" v-if="selectedClassCode">
               已交: <b class="score-num">{{ submittedCount }}</b> / <span class="score-num">{{ currentClassTotalStudents }}</span>
               <span style="margin: 0 6px; color: #dcdfe6">|</span>
               已批: <b class="score-num">{{ gradedCount }}</b> / <span class="score-num">{{ submittedCount }}</span>
            </span>
         </div>
         <div class="student-list-scroll">
            <div 
               v-for="(s, index) in submissions" 
               :key="s.studentId" 
               class="student-item"
               :class="{ 
                  'active': currentStudent?.studentId === s.studentId, 
                  'graded': s.submitted && s.score != null,
                  'not-submitted': !s.submitted
               }"
               @click="s.submitted ? selectStudent(s, index) : null"
            >
               <div class="s-info">
                   <div class="s-name" :style="s.remark ? { color: '#E6A23C' } : {}">{{ s.studentName }}</div>
                   <div class="s-remark" v-if="s.remark">{{ s.remark }}</div>
                   <div class="s-no">{{ s.studentNo }}</div>
                   <div
                     v-if="s.submitted"
                     class="s-preview-status"
                     :class="getPreviewStatusClass(s.previewStatus)"
                   >
                     {{ getPreviewStatusText(s) }}
                   </div>
               </div>
               <div class="s-status" v-if="!s.submitted">未交</div>
               <div class="s-status score-num" v-else-if="s.score != null">{{ s.score }}分</div>
               <div class="s-status ungrad" v-else>未批</div>
               <div v-if="aiResultFor(s)?.resultStatus === 'SUCCESS'" class="s-ai">AI {{ aiResultFor(s).suggestedScore }}分</div>
            </div>
            <el-empty v-if="submissions.length === 0" description="暂无学生" :image-size="60" />
         </div>
      </div>

      <!-- 中间：预览区 -->
      <div class="preview-panel">
         <div v-if="currentStudent" class="preview-content">
             <div class="preview-header">
                 <div class="header-info">
                    <span class="student-label">{{ currentStudent.studentName }} 的提交作品</span>
                    <span v-if="currentAttachment" class="file-name">📄 {{ currentAttachment.originalFileName || getFileName(currentAttachment.resourcePath) }}</span>
                 </div>
                 <a v-if="currentAttachment" :href="getFileUrl(currentAttachment.resourcePath)" target="_blank" class="download-link">下载当前文件</a>
             </div>
             <div v-if="currentAttachments.length > 1" class="attachment-tabs">
                <el-button
                  v-for="(attachment, attachmentIndex) in currentAttachments"
                  :key="attachment.attachmentId || attachmentIndex"
                  size="small"
                  :type="attachmentIndex === currentAttachmentIndex ? 'primary' : 'default'"
                  @click="selectAttachment(attachmentIndex)"
                >
                  {{ attachmentIndex + 1 }}. {{ attachment.originalFileName || getFileName(attachment.resourcePath) }}
                </el-button>
             </div>
             <div v-if="currentNormalizedPages.length" class="normalized-page-strip">
                <img
                  v-for="(pagePath, pageIndex) in currentNormalizedPages"
                  :key="pagePath"
                  :src="getPreviewUrl(pagePath)"
                  :alt="`作品第 ${pageIndex + 1} 页`"
                  class="normalized-page"
                />
             </div>
             <img
                v-else-if="currentAttachment?.fileKind === 'IMAGE'"
                :src="getPreviewUrl(currentAttachment.resourcePath)"
                class="image-frame"
                alt="学生提交图片"
             />
             <iframe 
                v-else-if="previewUrl"
                :src="previewUrl" 
                class="pdf-frame" 
                frameborder="0"
             ></iframe>
             <el-alert
                v-else-if="currentPreviewStatus === 'pending'"
                title="作品已交卷，预览排队中"
                description="交卷已成功；预览转换与批改评分互不阻断，可先下载源文件。"
                type="info"
                :closable="false"
                show-icon
             />
             <el-alert
                v-else-if="currentPreviewStatus === 'converting'"
                title="预览转换中"
                description="交卷已成功，正在生成 PDF 预览，请稍候或下载源文件批改。"
                type="info"
                :closable="false"
                show-icon
             />
             <el-alert
                v-else-if="currentPreviewStatus === 'failed'"
                title="预览失败（交卷仍有效）"
                :description="(currentAttachment?.previewErrorMessage || currentStudent.previewErrorMessage || '转换失败') + '。可下载源文件批改，或使用「重新转换本班异常文件」。'"
                type="warning"
                :closable="false"
                show-icon
             />
             <el-empty v-else description="该生未提交文件或文件不可预览" />
         </div>
         <el-empty v-else description="请从左侧选择一名学生开始批改" />
      </div>

      <!-- 右侧：打分面板 -->
      <div class="scoring-panel" v-if="currentStudent && currentStudent.submitted">
         <div class="score-card">
            <div class="card-title">批改打分</div>
            
            <div class="question-info">
                <div class="q-score">满分：{{ currentStudent.maxScore }} 分</div>
            </div>

            <div v-if="currentAiSuggestion" class="ai-suggestion-card">
               <div class="ai-suggestion-head">
                  <strong>AI 建议：{{ currentAiSuggestion.suggestedScore }} 分</strong>
                  <el-tag size="small" type="warning">仅供教师复核</el-tag>
               </div>
               <div v-if="currentAiSummary" class="ai-summary">{{ currentAiSummary }}</div>
               <div v-if="currentAiItemDetails.length" class="ai-item-details">
                  <div v-for="item in currentAiItemDetails" :key="item.itemId" class="ai-item-detail">
                     <span>{{ item.itemName }}</span>
                     <strong>{{ item.score }} / {{ item.maxScore }} 分</strong>
                     <small v-if="item.reason">{{ item.reason }}</small>
                  </div>
               </div>
               <div class="ai-confidence">置信度：{{ formatConfidence(currentAiSuggestion.confidence) }}</div>
               <el-tag v-if="['APPLIED', 'APPLIED_OVERWRITE'].includes(currentAiSuggestion.applyStatus)" size="small" type="success">
                 {{ currentAiSuggestion.applyStatus === 'APPLIED_OVERWRITE' ? '已覆盖正式成绩' : '已写入正式成绩' }}
               </el-tag>
               <el-button size="small" type="success" plain @click="applyAiSuggestion">采用到评分框</el-button>
            </div>
            <el-alert
              v-else-if="currentAiResult?.resultStatus === 'FAILED'"
              :title="currentAiResult.errorMessage || '该份 AI 建议生成失败'"
              type="warning"
              :closable="false"
            />
            
            <!-- P6: 评分模式切换 -->
            <div class="scoring-mode-switch" v-if="scoringItems.length > 0">
               <el-switch 
                  v-model="useItemScoring" 
                  :disabled="submitting || !deadlineStatus?.canGrade"
                  active-text="分项评分" 
                  inactive-text="直接打分"
                  @change="onScoringModeChange"
               />
            </div>

            <!-- 直接打分模式 -->
            <div class="score-input-area" v-if="!useItemScoring">
                <div class="input-label">得分：</div>
                <el-input-number 
                   v-model="currentScore" 
                   :disabled="submitting || !deadlineStatus?.canGrade"
                   :min="0" 
                   :max="currentStudent.maxScore" 
                   :precision="0"
                   controls-position="right"
                   size="large"
                   ref="scoreInputRef"
                   @keyup.enter="submitScore"
                />
            </div>
            
            <!-- P6: 分项评分模式 -->
            <div class="item-scoring-area" v-else>
                  <div v-for="(item, index) in scoringItems" :key="item.itemId" class="item-row">
                     <span class="item-name">{{ item.itemName }}</span>
                     <div class="item-input">
                        <el-input-number 
                           :ref="el => setItemInputRef(el, index)"
                           v-model="itemScores[item.itemId]" 
                           :disabled="submitting || !deadlineStatus?.canGrade"
                           :min="0" 
                           :max="item.maxScore"
                           :precision="0"
                           size="small"
                           @change="onItemScoreChange"
                           @keydown.enter="onItemEnter(index)"
                        />
                        <span class="item-max">/ {{ item.maxScore }} 分</span>
                     </div>
                  </div>
                  <div class="item-total">
                     分项合计: <span class="total-score score-num">{{ itemTotalScore }}</span> / <span class="score-num">{{ currentQuestionScore }}</span> 分
                  </div>
            </div>
            
            <el-button type="primary" size="large" class="submit-btn" :loading="submitting" :disabled="submitting || !deadlineStatus?.canGrade" @click="submitScore">
               提交并下一位 (Enter)
            </el-button>
            
            <div class="nav-actions">
               <el-button @click="prevStudent" :disabled="submitting || currentIndex <= 0">上一位 (PgUp)</el-button>
               <el-button @click="nextStudent" :disabled="submitting || currentIndex >= submissions.length - 1">下一位 (PgDn)</el-button>
            </div>
         </div>
      </div>
    </div>

    <el-dialog v-model="aiConfigVisible" title="AI 辅助批改设置" width="520px" append-to-body>
      <el-alert
        title="API Key 只在后端加密保存，页面不会再次显示明文；AI 建议不会自动写入正式成绩。"
        type="info" :closable="false" show-icon style="margin-bottom: 16px"
      />
      <el-alert
        v-if="aiConfig && !aiConfig.masterKeyConfigured"
        title="服务器尚未配置 PRACTICAL_AI_MASTER_KEY，暂不能保存教师 Key。"
        type="error" :closable="false" style="margin-bottom: 16px"
      />
      <el-form label-width="100px">
        <el-form-item label="模型厂商"><el-input model-value="阿里云百炼·通义千问" disabled /></el-form-item>
        <el-form-item label="视觉模型">
          <el-select v-model="aiConfigForm.modelName" style="width: 100%">
            <el-option label="Qwen3.7-Plus（推荐，质量优先）" value="qwen3.7-plus" />
            <el-option label="Qwen3.6-Flash（速度/成本优先）" value="qwen3.6-flash" />
          </el-select>
        </el-form-item>
        <el-form-item label="API Key">
          <div class="ai-key-field">
            <el-input v-model="aiConfigForm.apiKey" type="password" show-password autocomplete="new-password"
              :placeholder="aiConfig?.configured ? `已配置 ${aiConfig.apiKeyHint}；输入新 Key 可替换` : '请输入百炼 API Key'" />
            <el-link
              class="ai-key-apply-link"
              type="primary"
              :underline="false"
              href="https://bailian.console.aliyun.com/?apiKey=1&tab=model"
              target="_blank"
              rel="noopener noreferrer"
            >
              还没有 Key？前往阿里云百炼申请 API Key
            </el-link>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button v-if="aiConfig?.configured" type="danger" plain @click="removeAiConfig">删除配置</el-button>
        <el-button v-if="aiConfig?.configured" :loading="aiTesting" @click="testSavedAiConfig">测试连通性</el-button>
        <el-button @click="aiConfigVisible = false">取消</el-button>
        <el-button type="primary" :loading="aiConfigSaving" :disabled="!aiConfig?.masterKeyConfigured" @click="submitAiConfig">加密保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="aiJobDialogVisible" title="开始 AI 批改" width="620px" append-to-body>
      <el-alert title="请先确认批改范围。AI 会对照空白起始材料和教师参考答案，先生成建议，不会在此步骤写入正式成绩。"
        type="info" :closable="false" show-icon style="margin-bottom: 16px" />
      <div v-loading="aiPreflightLoading">
        <div class="ai-preflight-counts" v-if="aiPreflight">
          已提交 {{ aiPreflight.submittedCount }} 人，已人工批改 {{ aiPreflight.gradedCount }} 人，
          未批 {{ aiPreflight.ungradedCount }} 人，可供 AI 识别 {{ aiPreflight.readyCount }} 人。
        </div>
        <el-radio-group v-model="aiScopeMode" class="ai-scope-options">
          <el-radio value="UNGRADED_ONLY">
            仅批改剩余未批学生（推荐，预计 {{ aiPreflight?.readyUngradedCount || 0 }} 人）
          </el-radio>
          <el-radio value="ALL_SUBMITTED">
            全班重新生成建议（只作对照，不覆盖已有人工成绩）
          </el-radio>
        </el-radio-group>
        <div class="ai-reference-row">
          <div>
            <strong>教师参考答案（AI 批改必填）</strong>
            <div class="ai-reference-name" :class="{ 'is-missing': !aiPreflight?.referenceReady }">
              {{ aiPreflight?.referenceFileName || '尚未上传' }}
            </div>
            <small>支持 Word、PDF、PPT、Excel、JPG、JPEG、PNG，单文件不超过 50 MiB。</small>
          </div>
          <el-upload :show-file-list="false" :http-request="uploadAiReference" :accept="aiReferenceAccept">
            <el-button type="primary" plain :loading="aiReferenceUploading">
              {{ aiPreflight?.referenceReady ? '替换参考答案' : '上传参考答案' }}
            </el-button>
          </el-upload>
        </div>
        <el-alert v-if="aiPreflight && !aiPreflight.referenceReady" title="请先上传教师参考答案，才能开始 AI 批改。"
          type="warning" :closable="false" style="margin-top: 12px" />
        <div class="ai-starter-note">空白起始材料：{{ aiPreflight?.starterCount || 0 }} 个；没有起始材料时，AI 仍会按题干与参考答案评分。</div>
      </div>
      <template #footer>
        <el-button @click="aiJobDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="aiStarting"
          :disabled="!aiPreflight?.referenceReady || selectedAiScopeCount <= 0" @click="startAiJob">
          确认生成建议
        </el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="aiDetailVisible" title="AI 批改处理详情" size="660px" append-to-body>
      <el-alert
        v-if="aiProgress?.stalled"
        title="任务较长时间没有新心跳，可能正在等待模型或文件转换"
        description="已完成建议不会丢失。可先查看下方当前阶段与错误记录；服务重启后会自动从未完成作品接续。"
        type="warning"
        :closable="false"
        show-icon
        class="ai-detail-alert"
      />
      <div class="ai-detail-summary">
        <div><strong>{{ aiProgress?.completedCount || 0 }}</strong><span>已结束</span></div>
        <div><strong>{{ aiProgress?.processingCount || 0 }}</strong><span>处理中</span></div>
        <div><strong>{{ aiProgress?.waitingCount || 0 }}</strong><span>等待中</span></div>
        <div><strong>{{ formatDuration((aiProgress?.averageDurationMs || 0) / 1000) }}</strong><span>平均每份</span></div>
      </div>
      <el-progress :percentage="aiCompletionPercentage" :status="aiJob?.jobStatus === 'COMPLETED' ? 'success' : undefined" />
      <div class="ai-current-stage">
        <div><span>当前处理：</span><strong>{{ aiCurrentStudentName }}</strong></div>
        <div><span>当前阶段：</span>{{ aiStageText(aiProgress?.currentStage || aiPreparationStage) }}</div>
        <div><span>已运行：</span>{{ formatDuration(aiProgress?.elapsedSeconds) }}</div>
        <div><span>预计剩余：</span>{{ formatEta(aiProgress?.estimatedRemainingSeconds) }}</div>
        <div><span>最近心跳：</span>{{ formatAiTime(aiProgress?.heartbeatTime) }}</div>
      </div>

      <h4 class="ai-detail-title">逐份处理状态</h4>
      <el-table :data="aiDetailRows" size="small" max-height="330" stripe empty-text="暂无处理记录">
        <el-table-column prop="sequence" label="#" width="48" />
        <el-table-column prop="studentName" label="学生" min-width="100" show-overflow-tooltip />
        <el-table-column label="状态" width="92">
          <template #default="scope">
            <el-tag size="small" :type="aiResultTagType(scope.row.resultStatus)">
              {{ aiResultStatusText(scope.row.resultStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="阶段" min-width="130">
          <template #default="scope">{{ aiStageText(scope.row.processingStage) }}</template>
        </el-table-column>
        <el-table-column label="耗时" width="82">
          <template #default="scope">{{ formatDuration((scope.row.durationMs || 0) / 1000) }}</template>
        </el-table-column>
        <el-table-column prop="errorMessage" label="说明" min-width="160" show-overflow-tooltip />
      </el-table>

      <h4 class="ai-detail-title">安全处理日志</h4>
      <div class="ai-event-list" v-loading="aiEventsLoading">
        <div v-for="item in aiEvents" :key="item.eventId" class="ai-event-item" :class="`is-${String(item.eventLevel || '').toLowerCase()}`">
          <span class="ai-event-time">{{ formatAiTime(item.createTime) }}</span>
          <el-tag size="small" :type="item.eventLevel === 'ERROR' ? 'danger' : item.eventLevel === 'WARN' ? 'warning' : 'info'">
            {{ aiStageText(item.eventStage) }}
          </el-tag>
          <span>{{ aiEventStudentName(item) }}{{ item.eventMessage }}</span>
        </div>
        <el-empty v-if="!aiEventsLoading && !aiEvents.length" description="暂无日志" :image-size="55" />
      </div>
      <p class="ai-detail-privacy">为保护数据安全，这里不会显示 API Key、完整提示词、模型原始输出或后台异常堆栈。</p>
    </el-drawer>
  </div>
</template>

<script setup name="TeacherGrading">
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import { getDashboardData } from '@/api/business/teacher';
import { getClassesByLesson, getPracticalQuestions, getPracticalSubmissions, retryFailedPreviews, getPracticalDeadlineStatus, gradeSubmission } from '@/api/business/teacherGrading';
import { getScoringItems, getScoringDetails } from '@/api/business/scoringItem';  // P6
import { getAiConfig, saveAiConfig, deleteAiConfig, testAiConfig, createAiJob, getAiJob, getAiJobEvents,
    getAiPreflight, getLatestAiJob, uploadAiReferenceAnswer, batchApplyAiSuggestions,
    pauseAiJob, resumeAiJob, cancelAiJob, retryFailedAiJob } from '@/api/business/practicalAiGrading';
import { ElMessage, ElMessageBox } from 'element-plus';
import { FullScreen } from '@element-plus/icons-vue';
import { deadlineStatusMeta, formatDeadlineRemaining, formatDeadlineTime } from '@/utils/practicalDeadline';

const route = useRoute();
const loading = ref(false);
const STUCK_PREVIEW_TIMEOUT_MS = 10 * 60 * 1000;
const gradeGroups = ref([]);
const lessons = ref([]);
const classes = ref([]);        // P3.5: 班级列表
const questions = ref([]);
const submissions = ref([]);

const selectedLessonId = ref(null);
const selectedClassCode = ref(null);  // P3.5: 选中的班级
const selectedQuestionId = ref(null);
const deadlineStatus = ref(null);

const selectedLessonTitle = computed(() => {
    for (const group of gradeGroups.value) {
        const lesson = group.lessons?.find(item => item.lessonId === selectedLessonId.value);
        if (lesson) return lesson.lessonTitle;
    }
    return '当前课程';
});

const currentStudent = ref(null);
const currentIndex = ref(-1);
const currentScore = ref(undefined);
const previewUrl = ref('');
const currentAttachmentIndex = ref(0);
const retryLoading = ref(false);
const submitting = ref(false);
const aiConfigVisible = ref(false);
const aiConfigSaving = ref(false);
const aiTesting = ref(false);
const aiStarting = ref(false);
const aiJobDialogVisible = ref(false);
const aiPreflightLoading = ref(false);
const aiReferenceUploading = ref(false);
const aiBatchApplying = ref(false);
const aiPreflight = ref(null);
const aiScopeMode = ref('UNGRADED_ONLY');
const aiJobBatchAdoptAllowed = ref(false);
const aiReferenceAccept = '.doc,.docx,.pdf,.ppt,.pptx,.xls,.xlsx,.jpg,.jpeg,.png';
const aiConfig = ref(null);
const aiConfigForm = ref({ apiKey: '', modelName: 'qwen3.7-plus' });
const aiJob = ref(null);
const aiProgress = ref(null);
const aiDetailVisible = ref(false);
const aiEventsLoading = ref(false);
const aiEvents = ref([]);
const aiResultsByAnswer = ref({});
let aiPollTimer = null;
let scoringItemsRequestId = 0;
let scoringDetailsRequestId = 0;

const isFullscreen = ref(false);
const gradingPageRef = ref(null);
const gradingMainRef = ref(null);
const scoreInputRef = ref(null);

// P6: 分项评分相关状态
const scoringItems = ref([]);      // 评分项列表
const itemScores = ref({});        // 各评分项得分 { itemId: score }
const useItemScoring = ref(false); // 是否使用分项评分

const currentAttachments = computed(() => {
    const attachments = currentStudent.value?.attachments;
    if (Array.isArray(attachments) && attachments.length) return attachments;
    if (!currentStudent.value?.studentAnswer) return [];
    return [{
        resourcePath: currentStudent.value.studentAnswer,
        previewPath: currentStudent.value.previewPath,
        previewStatus: currentStudent.value.previewStatus,
        previewErrorMessage: currentStudent.value.previewErrorMessage
    }];
});

const currentAttachment = computed(() => currentAttachments.value[currentAttachmentIndex.value] || null);
const currentNormalizedPages = computed(() => Array.isArray(currentAttachment.value?.normalizedPages)
    ? currentAttachment.value.normalizedPages : []);
const currentPreviewStatus = computed(() => currentAttachment.value?.previewStatus || currentStudent.value?.previewStatus || '');
const currentAiResult = computed(() => aiResultFor(currentStudent.value));
const currentAiSuggestion = computed(() => currentAiResult.value?.resultStatus === 'SUCCESS' ? currentAiResult.value : null);
const currentAiSummary = computed(() => {
    if (!currentAiSuggestion.value?.evidenceJson) return '';
    try { return JSON.parse(currentAiSuggestion.value.evidenceJson)?.overallComment || ''; } catch (e) { return ''; }
});
const currentAiItemDetails = computed(() => {
    if (!currentAiSuggestion.value) return [];
    try {
        const details = JSON.parse(currentAiSuggestion.value.scoringDetailsJson || '[]');
        const evidence = JSON.parse(currentAiSuggestion.value.evidenceJson || '{}');
        const reasons = new Map((evidence.rubricResults || []).map(item => [Number(item.rubricItemId), item.reason || '']));
        return details.map(detail => {
            const item = scoringItems.value.find(candidate => Number(candidate.itemId) === Number(detail.itemId));
            return {
                itemId: detail.itemId,
                itemName: item?.itemName || `评分项 ${detail.itemId}`,
                score: detail.score,
                maxScore: item?.maxScore ?? '--',
                reason: reasons.get(Number(detail.itemId)) || ''
            };
        });
    } catch (e) { return []; }
});
const selectedAiScopeCount = computed(() => aiScopeMode.value === 'ALL_SUBMITTED'
    ? (aiPreflight.value?.readyCount || 0) : (aiPreflight.value?.readyUngradedCount || 0));
const batchApplicableCount = computed(() => submissions.value.filter(student =>
    student.submitted && student.score == null && aiResultFor(student)?.resultStatus === 'SUCCESS'
).length);
const batchOverwriteExistingCount = computed(() => submissions.value.filter(student =>
    student.submitted && student.score != null && aiResultFor(student)?.resultStatus === 'SUCCESS'
).length);
const batchAdoptableCount = computed(() => batchApplicableCount.value + batchOverwriteExistingCount.value);
const canBatchApplyAiSuggestions = computed(() => Boolean(aiJobBatchAdoptAllowed.value
    && ['COMPLETED', 'PARTIAL_FAILED'].includes(aiJob.value?.jobStatus)
    && batchAdoptableCount.value > 0 && deadlineStatus.value?.canGrade));
const canStartAiJob = computed(() => Boolean(
    aiConfig.value?.configured && aiConfig.value?.masterKeyConfigured
    && selectedLessonId.value && selectedQuestionId.value && selectedClassCode.value
    && submittedCount.value > 0 && deadlineStatus.value?.canGrade
    && !['PENDING', 'RUNNING', 'PAUSED', 'CANCEL_REQUESTED'].includes(aiJob.value?.jobStatus)
));
const aiJobTagType = computed(() => ['COMPLETED'].includes(aiJob.value?.jobStatus) ? 'success'
    : ['FAILED', 'PARTIAL_FAILED'].includes(aiJob.value?.jobStatus) ? 'danger'
    : aiJob.value?.jobStatus === 'PAUSED' ? 'warning' : 'info');
const aiJobStatusText = computed(() => {
    if (!aiJob.value) return '';
    const labels = { PENDING: 'AI 排队中', RUNNING: 'AI 批改中', COMPLETED: 'AI 建议已完成',
        PARTIAL_FAILED: 'AI 部分失败', FAILED: 'AI 任务失败', PAUSED: 'AI 已暂停',
        CANCEL_REQUESTED: 'AI 正在取消', CANCELLED: 'AI 已取消' };
    const done = aiProgress.value?.completedCount ?? ((aiJob.value.successCount || 0) + (aiJob.value.failedCount || 0));
    const progress = `${done}/${aiJob.value.totalCount || 0}`;
    return `${labels[aiJob.value.jobStatus] || aiJob.value.jobStatus} ${progress}`;
});
const aiCompletionPercentage = computed(() => {
    const total = Number(aiJob.value?.totalCount || 0);
    return total ? Math.min(100, Math.round(Number(aiProgress.value?.completedCount || 0) * 100 / total)) : 0;
});
const aiPreparationStage = computed(() => aiProgress.value?.preparationStatus === 'PREPARING'
    ? 'PREPARING_REFERENCE' : aiJob.value?.jobStatus === 'PENDING' ? 'QUEUED' : 'WAITING');
const aiCurrentStudentName = computed(() => {
    const answerId = aiProgress.value?.currentAnswerId;
    if (!answerId) return aiProgress.value?.preparationStatus === 'PREPARING' ? '公共参考材料' : '暂无';
    return submissions.value.find(item => Number(item.answerId) === Number(answerId))?.studentName || '当前作品';
});
const aiDetailRows = computed(() => Object.values(aiResultsByAnswer.value)
    .sort((a, b) => Number(a.resultId) - Number(b.resultId))
    .map((item, index) => ({
        ...item,
        sequence: index + 1,
        studentName: submissions.value.find(student => Number(student.answerId) === Number(item.answerId))?.studentName || '未知学生'
    })));

// P6.1: 计算当前题目在课程中的设定的总分
const currentQuestionScore = computed(() => {
    if (!selectedQuestionId.value) return 100;
    const q = questions.value.find(item => item.questionId === selectedQuestionId.value);
    // 如果没有找到或未设置分数，默认100
    // 注意：biz_lesson_question 中字段是 questionScore
    return q ? (q.questionScore ?? 100) : 100;
});

// 分项上限由服务端统一分配，前端只汇总教师输入值。
const itemTotalScore = computed(() => {
    let total = 0;
    for (const key in itemScores.value) {
        total += itemScores.value[key] || 0;
    }
    return total;
});

// 初始化加载课程数据
onMounted(() => {
  fetchDashboardData();
  loadAiConfigStatus();
  document.addEventListener('keydown', handleGlobalKeydown);
  document.addEventListener('fullscreenchange', handleFullscreenChange);
  
  // 检查URL参数
  const queryLessonId = route.query.lessonId;
  if (queryLessonId) {
    selectedLessonId.value = parseInt(queryLessonId);
    // 等待数据加载后触发change，或者直接触发
    onLessonChange(selectedLessonId.value);
  }
});

function handleGlobalKeydown(e) {
  if (submitting.value) return;
  if (e.key === 'PageUp') {
    e.preventDefault();
    prevStudent();
  }
  if (e.key === 'PageDown') {
    e.preventDefault();
    nextStudent();
  }
}

function handleFullscreenChange() {
  isFullscreen.value = !!document.fullscreenElement;
}

onBeforeUnmount(() => {
  stopAiPolling();
  scoringItemsRequestId++;
  scoringDetailsRequestId++;
  document.removeEventListener('keydown', handleGlobalKeydown);
  document.removeEventListener('fullscreenchange', handleFullscreenChange);
});

function fetchDashboardData() {
  getDashboardData().then(res => {
    gradeGroups.value = res.data;
  });
}

// 根据班级批改状态返回样式类
function getClassOptionClass(classItem) {
  if (classItem.practicalUngraded > 0) {
    return 'has-ungraded';
  } else if (classItem.practicalSubmitted > 0) {
    return 'all-graded';
  }
  return 'no-submit';
}

// P3.5: 选择课程后加载班级列表
async function onLessonChange(val) {
  resetAiJobView();
  selectedClassCode.value = null;
  selectedQuestionId.value = null;
  classes.value = [];
  questions.value = [];
  submissions.value = [];
  currentStudent.value = null;
  
  if (val) {
    // 先加载操作题列表（需要在班级选择前完成，以便自动选择）
    const questionsRes = await getPracticalQuestions(val);
    questions.value = questionsRes.data || [];
    
    // 加载班级列表
    getClassesByLesson(val).then(res => {
        classes.value = res.data;
        
        // 检查URL参数中的 classCode，自动选中
        const queryClassCode = route.query.classCode;
        if (queryClassCode && classes.value.some(c => c.classCode === queryClassCode)) {
            selectedClassCode.value = queryClassCode;
            onClassChange(selectedClassCode.value);
        } else if (classes.value.length === 1) {
            // 如果只有一个班级，自动选中
            selectedClassCode.value = classes.value[0].classCode;
            onClassChange(selectedClassCode.value);
        }
    });
  }
}

// P3.5: 选择班级后加载操作题提交记录
function onClassChange(val) {
    if (!val) return;
    resetAiJobView();
    loadDeadlineStatus();
    // 如果已选择了题目，则加载该题目的提交记录
    if (selectedQuestionId.value) {
        loadSubmissions();
        loadScoringItems(); // P6: 也要加载评分项
    } else if (questions.value.length > 0) {
        // 自动选择第一个题目
        selectedQuestionId.value = questions.value[0].questionId;
        loadSubmissions();
        loadScoringItems(); // P6: 也要加载评分项
    }
}

function onQuestionChange(val) {
    if (val && selectedClassCode.value) {
        resetAiJobView();
        loadSubmissions();
        // P6: 加载评分项
        loadScoringItems();
    }
}

// P6: 加载评分项
function loadScoringItems(practicalVersionId = currentStudent.value?.practicalVersionId) {
    if (!selectedLessonId.value || !selectedQuestionId.value) return Promise.resolve();
    const lessonId = selectedLessonId.value;
    const questionId = selectedQuestionId.value;
    const requestId = ++scoringItemsRequestId;
    return getScoringItems(lessonId, questionId, practicalVersionId).then(res => {
        if (requestId !== scoringItemsRequestId
            || lessonId !== selectedLessonId.value
            || questionId !== selectedQuestionId.value
            || practicalVersionId !== currentStudent.value?.practicalVersionId) {
            return;
        }
        scoringItems.value = res.data || [];
        // 重置分项得分
        itemScores.value = {};
        scoringItems.value.forEach(item => {
            itemScores.value[item.itemId] = 0;
        });
        // 如果有评分项，默认使用分项评分
        useItemScoring.value = scoringItems.value.length > 0;
    });
}

// P6: 评分模式切换
function onScoringModeChange(useItem) {
    if (useItem) {
        // 切换到分项评分时，重置分项得分
        scoringItems.value.forEach(item => {
            itemScores.value[item.itemId] = 0;
        });
    }
}

function onItemScoreChange() {
    // 可以在此添加额外的逻辑，当前仅依赖computed属性即可
}

// P6: 评分项输入框引用数组
const itemInputRefs = ref([]);

// P6: 设置评分项输入框引用
function setItemInputRef(el, index) {
    if (el) {
        itemInputRefs.value[index] = el;
    }
}

// 自动聚焦只能选中评分框，不能把整个三栏工作区一起滚走。
function focusScoreInput(input) {
    if (!input) return;
    try {
        input.focus({ preventScroll: true });
    } catch (e) {
        input.focus();
    }
    input.select();
    if (gradingMainRef.value) {
        gradingMainRef.value.scrollTop = 0;
        gradingMainRef.value.scrollLeft = 0;
    }
}

// P6: 回车切换下一项或提交
function onItemEnter(index) {
    if (submitting.value) return;
    if (index < scoringItems.value.length - 1) {
        // 还有下一项，聚焦下一个输入框
        nextTick(() => {
            const nextInput = itemInputRefs.value[index + 1];
            if (nextInput && nextInput.$el) {
                const input = nextInput.$el.querySelector('input');
                if (input) {
                    focusScoreInput(input);
                }
            }
        });
    } else {
        // 最后一项，提交并切换到下一个学生
        submitScore();
    }
}

// P6: 聚焦第一个评分项输入框
function focusFirstItem() {
    nextTick(() => {
        // 增加延时确保 itemInputRefs 已更新
        setTimeout(() => {
            if (itemInputRefs.value.length > 0 && itemInputRefs.value[0]) {
                const input = itemInputRefs.value[0].$el?.querySelector('input');
                if (input) {
                    focusScoreInput(input);
                }
            }
        }, 50);
    });
}

// 加载提交记录
function loadSubmissions() {
    if (!selectedLessonId.value || !selectedQuestionId.value || !selectedClassCode.value) return;
    
    // P5: 获取当前班级的entryYear
    const classInfo = classes.value.find(c => c.classCode === selectedClassCode.value);
    const entryYear = classInfo?.entryYear || '';
    const previousStudentId = currentStudent.value?.studentId;
    
    loading.value = true;
    return getPracticalSubmissions(selectedLessonId.value, selectedQuestionId.value, selectedClassCode.value, entryYear).then(res => {
        submissions.value = res.data;
        loading.value = false;
        restoreLatestAiJob();
        const preservedStudent = previousStudentId != null
            ? submissions.value.find(s => s.studentId === previousStudentId && s.submitted)
            : null;
        const nextStudent = preservedStudent || submissions.value.find(s => s.submitted);
        if (nextStudent) {
            const idx = submissions.value.findIndex(s => s.studentId === nextStudent.studentId);
            selectStudent(nextStudent, idx);
            return;
        }
        currentStudent.value = null;
        currentIndex.value = -1;
        currentScore.value = undefined;
        previewUrl.value = '';
    }).catch(() => {
        loading.value = false;
        submissions.value = [];
        currentStudent.value = null;
        currentIndex.value = -1;
        currentScore.value = undefined;
        previewUrl.value = '';
        ElMessage.error('加载学生提交记录失败');
    });
}

const gradedCount = computed(() => submissions.value.filter(s => s.submitted && s.score != null).length);

// 已提交学生数量
const submittedCount = computed(() => submissions.value.filter(s => s.submitted).length);
function isRecoverablePreview(student) {
    if (!student?.submitted) return false;
    const attachments = Array.isArray(student.attachments) ? student.attachments : [];
    if (attachments.length) {
        return attachments.some(attachment => {
            if ((attachment.normalizedRetryCount || 0) >= 3) return false;
            if (attachment.normalizedStatus === 'failed') return true;
            if (attachment.normalizedStatus !== 'converting') return false;
            const referenceTime = attachment.normalizedLastRetryTime || attachment.updateTime || student.submitTime;
            return referenceTime && Date.now() - new Date(referenceTime).getTime() >= STUCK_PREVIEW_TIMEOUT_MS;
        });
    }
    if (student.previewPath) return false;
    const answerPath = (student.studentAnswer || '').toLowerCase();
    const isWordFile = answerPath.endsWith('.docx') || answerPath.endsWith('.doc');
    if (!isWordFile) return false;
    if (student.previewStatus === 'failed') return true;
    if (student.previewStatus !== 'pending' && student.previewStatus !== 'converting') return false;
    const referenceTime = student.previewLastRetryTime || student.submitTime;
    if (!referenceTime) return false;
    return Date.now() - new Date(referenceTime).getTime() >= STUCK_PREVIEW_TIMEOUT_MS;
}

const failedSubmissionCount = computed(() => submissions.value.filter(
    isRecoverablePreview
).length);
const canRetryFailedPreviews = computed(() => Boolean(
    selectedLessonId.value && selectedQuestionId.value && selectedClassCode.value && failedSubmissionCount.value > 0
));

// P4: 获取当前选中班级的学生总人数
const currentClassTotalStudents = computed(() => {
    if (!selectedClassCode.value || !classes.value.length) return 0;
    const classInfo = classes.value.find(c => c.classCode === selectedClassCode.value);
    return classInfo?.totalStudents || 0;
});

// P3: 获取当前选中课程的年级名称
const currentGradeName = computed(() => {
    if (!selectedLessonId.value || !gradeGroups.value.length) return '';
    for (const group of gradeGroups.value) {
        const found = group.lessons?.find(l => l.lessonId === selectedLessonId.value);
        if (found) {
            return group.gradeName;
        }
    }
    return '';
});

const currentEntryYear = computed(() => {
    if (!selectedClassCode.value || !classes.value.length) return '';
    const classInfo = classes.value.find(c => c.classCode === selectedClassCode.value);
    return classInfo?.entryYear || '';
});

function getFileUrl(path) {
    if (!path) return '';
    return import.meta.env.VITE_APP_BASE_API + '/common/download/resource?resource=' + encodeURIComponent(path);
}

function loadAiConfigStatus() {
    return getAiConfig().then(res => {
        aiConfig.value = res.data || {};
        aiConfigForm.value.modelName = aiConfig.value.modelName || 'qwen3.7-plus';
    });
}

async function openAiConfig() {
    await loadAiConfigStatus();
    aiConfigForm.value.apiKey = '';
    aiConfigVisible.value = true;
}

async function submitAiConfig() {
    if (!aiConfigForm.value.apiKey) {
        ElMessage.warning('请输入新的 API Key');
        return;
    }
    aiConfigSaving.value = true;
    try {
        await saveAiConfig(aiConfigForm.value);
        ElMessage.success('API Key 已在后端加密保存');
        aiConfigVisible.value = false;
        aiConfigForm.value.apiKey = '';
        await loadAiConfigStatus();
    } finally {
        aiConfigSaving.value = false;
    }
}

async function removeAiConfig() {
    await ElMessageBox.confirm('删除后将无法继续执行新的 AI 批改任务，确定删除吗？', '删除 AI 配置', { type: 'warning' });
    await deleteAiConfig();
    ElMessage.success('AI 配置已删除');
    aiConfigVisible.value = false;
    await loadAiConfigStatus();
}

async function testSavedAiConfig() {
    aiTesting.value = true;
    try {
        await testAiConfig();
        ElMessage.success('API Key、网络和视觉模型连通正常');
    } finally {
        aiTesting.value = false;
    }
}

function aiScopeParams() {
    return {
        lessonId: selectedLessonId.value,
        questionId: selectedQuestionId.value,
        entryYear: currentEntryYear.value,
        classCode: selectedClassCode.value
    };
}

async function openAiJobDialog() {
    aiScopeMode.value = 'UNGRADED_ONLY';
    aiJobDialogVisible.value = true;
    await loadAiPreflight();
}

async function loadAiPreflight() {
    aiPreflightLoading.value = true;
    try {
        const res = await getAiPreflight(aiScopeParams());
        aiPreflight.value = res.data || {};
    } finally {
        aiPreflightLoading.value = false;
    }
}

async function uploadAiReference(options) {
    aiReferenceUploading.value = true;
    try {
        const formData = new FormData();
        formData.append('file', options.file);
        for (const [key, value] of Object.entries(aiScopeParams())) formData.append(key, value);
        await uploadAiReferenceAnswer(formData);
        ElMessage.success('教师参考答案已保存');
        await loadAiPreflight();
        options.onSuccess?.();
    } catch (error) {
        options.onError?.(error);
    } finally {
        aiReferenceUploading.value = false;
    }
}

async function startAiJob() {
    aiStarting.value = true;
    try {
        const res = await createAiJob({
            ...aiScopeParams(),
            scopeMode: aiScopeMode.value
        });
        aiJob.value = res.data;
        aiProgress.value = null;
        aiJobBatchAdoptAllowed.value = true;
        aiResultsByAnswer.value = {};
        aiJobDialogVisible.value = false;
        ElMessage.success('AI 批改任务已进入后台队列');
        pollAiJob();
    } finally {
        aiStarting.value = false;
    }
}

async function pollAiJob() {
    stopAiPolling();
    if (!aiJob.value?.jobId) return;
    try {
        const res = await getAiJob(aiJob.value.jobId);
        aiJob.value = res.data?.job || aiJob.value;
        aiProgress.value = res.data?.progress || aiProgress.value;
        aiJobBatchAdoptAllowed.value = Boolean(res.data?.batchAdoptAllowed);
        const mapped = {};
        for (const result of (res.data?.results || [])) mapped[result.answerId] = result;
        aiResultsByAnswer.value = mapped;
        if (aiDetailVisible.value) loadAiEvents();
        if (['PENDING', 'RUNNING', 'CANCEL_REQUESTED'].includes(aiJob.value.jobStatus)) {
            aiPollTimer = window.setTimeout(pollAiJob, 2000);
        }
    } catch (e) {
        aiPollTimer = window.setTimeout(pollAiJob, 4000);
    }
}

async function restoreLatestAiJob() {
    if (!selectedLessonId.value || !selectedQuestionId.value || !selectedClassCode.value) return;
    const scope = aiScopeParams();
    try {
        const res = await getLatestAiJob(scope);
        if (scope.lessonId !== selectedLessonId.value || scope.questionId !== selectedQuestionId.value
            || scope.classCode !== selectedClassCode.value) return;
        const detail = res.data;
        if (!detail?.job) return;
        aiJob.value = detail.job;
        aiProgress.value = detail.progress || null;
        aiJobBatchAdoptAllowed.value = Boolean(detail.batchAdoptAllowed);
        const mapped = {};
        for (const result of (detail.results || [])) mapped[result.answerId] = result;
        aiResultsByAnswer.value = mapped;
        if (['PENDING', 'RUNNING', 'CANCEL_REQUESTED'].includes(aiJob.value.jobStatus)) pollAiJob();
    } catch (e) {
        // 恢复任务失败不阻断教师人工批改。
    }
}

async function applyAiSuggestionsInBatch() {
    if (!canBatchApplyAiSuggestions.value) return;
    let applyMode = 'FILL_UNGRADED';
    try {
        await ElMessageBox.confirm(
            `成功建议共 ${batchAdoptableCount.value} 份：未评分 ${batchApplicableCount.value} 份，已有正式成绩 ${batchOverwriteExistingCount.value} 份。请选择采用方式。`,
            '选择批量采用方式',
            {
                type: 'warning',
                confirmButtonText: `仅补未评分（${batchApplicableCount.value}）`,
                cancelButtonText: `覆盖已有评分（${batchOverwriteExistingCount.value}）`,
                distinguishCancelAndClose: true,
                closeOnClickModal: false
            }
        );
    } catch (action) {
        if (action !== 'cancel') return;
        if (batchOverwriteExistingCount.value === 0) {
            ElMessage.info('当前没有可覆盖的已有正式成绩');
            return;
        }
        try {
            await ElMessageBox.confirm(
                `本次将用 AI 建议覆盖 ${batchOverwriteExistingCount.value} 份已有正式成绩，并补齐 ${batchApplicableCount.value} 份未评分成绩。系统会逐份保留覆盖前后的审计记录。`,
                '确认覆盖已有正式成绩',
                {
                    type: 'error',
                    confirmButtonText: `确认覆盖 ${batchOverwriteExistingCount.value} 份`,
                    cancelButtonText: '返回',
                    closeOnClickModal: false
                }
            );
            applyMode = 'OVERWRITE_ALL';
        } catch (e) { return; }
    }
    aiBatchApplying.value = true;
    try {
        const res = await batchApplyAiSuggestions(aiJob.value.jobId, applyMode);
        const summary = res.data || {};
        if (applyMode === 'OVERWRITE_ALL') {
            ElMessage.success(`已采用 ${summary.appliedCount || 0} 人：覆盖已有成绩 ${summary.overwrittenCount || 0} 人，补齐未评分 ${summary.filledUngradedCount || 0} 人；版本变化 ${summary.skippedVersionCount || 0} 人`);
        } else {
            ElMessage.success(`已补齐未评分 ${summary.filledUngradedCount || 0} 人；跳过已有成绩 ${summary.skippedManualCount || 0} 人，版本变化 ${summary.skippedVersionCount || 0} 人`);
        }
        await loadSubmissions();
        await loadDeadlineStatus();
        await pollAiJob();
    } finally {
        aiBatchApplying.value = false;
    }
}

async function controlAiJob(action) {
    if (!aiJob.value?.jobId) return;
    const calls = { pause: pauseAiJob, resume: resumeAiJob, cancel: cancelAiJob, retry: retryFailedAiJob };
    await calls[action](aiJob.value.jobId);
    ElMessage.success({ pause: '任务已暂停', resume: '任务已继续', cancel: '已请求取消任务', retry: '失败作品已重新入队' }[action]);
    pollAiJob();
}

function stopAiPolling() {
    if (aiPollTimer) window.clearTimeout(aiPollTimer);
    aiPollTimer = null;
}

function resetAiJobView() {
    stopAiPolling();
    aiJob.value = null;
    aiProgress.value = null;
    aiDetailVisible.value = false;
    aiEvents.value = [];
    aiResultsByAnswer.value = {};
    aiJobBatchAdoptAllowed.value = false;
    aiPreflight.value = null;
}

async function openAiJobDetail() {
    if (!aiJob.value?.jobId) return;
    aiDetailVisible.value = true;
    await Promise.all([pollAiJob(), loadAiEvents()]);
}

async function loadAiEvents() {
    if (!aiJob.value?.jobId || aiEventsLoading.value) return;
    aiEventsLoading.value = true;
    try {
        const res = await getAiJobEvents(aiJob.value.jobId);
        aiEvents.value = Array.isArray(res.data) ? [...res.data].reverse() : [];
    } finally {
        aiEventsLoading.value = false;
    }
}

function aiStageText(value) {
    const labels = {
        QUEUED: '等待执行', WAITING: '等待处理', JOB_STARTED: '任务启动',
        PREPARING_REFERENCE: '准备参考材料', REFERENCE_READY: '参考材料就绪',
        PREPARING_STUDENT: '准备学生作品', REQUESTING_MODEL: '等待视觉模型',
        VALIDATING_RESULT: '校验并保存', COMPLETED: '处理完成', FAILED: '处理失败',
        PARTIAL_FAILED: '部分失败', PAUSED: '已暂停', RESUMED: '已继续',
        CANCEL_REQUESTED: '正在取消', CANCELLED: '已取消', RETRY_QUEUED: '失败项重试',
        AUTO_RECOVERED: '自动接续', JOB_FAILED: '任务失败', LEGACY_MIGRATED: '历史任务'
    };
    return labels[value] || value || '等待处理';
}

function aiResultStatusText(value) {
    return { PENDING: '等待', PROCESSING: '处理中', SUCCESS: '成功', FAILED: '失败', CANCELLED: '已取消' }[value] || value;
}

function aiResultTagType(value) {
    return value === 'SUCCESS' ? 'success' : value === 'FAILED' ? 'danger'
        : value === 'CANCELLED' ? 'info' : value === 'PROCESSING' ? 'warning' : 'info';
}

function aiEventStudentName(event) {
    if (!event?.resultId) return '';
    const row = aiDetailRows.value.find(item => Number(item.resultId) === Number(event.resultId));
    return row ? `${row.studentName}：` : '';
}

function formatDuration(seconds) {
    const value = Math.max(0, Math.round(Number(seconds) || 0));
    if (!value) return '--';
    if (value < 60) return `${value}秒`;
    const minutes = Math.floor(value / 60);
    const remain = value % 60;
    return remain ? `${minutes}分${remain}秒` : `${minutes}分钟`;
}

function formatEta(seconds) {
    const value = Number(seconds);
    return Number.isFinite(value) && value >= 0 ? formatDuration(value) : '完成首份后估算';
}

function formatAiTime(value) {
    if (!value) return '--';
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleString('zh-CN', { hour12: false });
}

function aiResultFor(student) {
    const result = student?.answerId ? aiResultsByAnswer.value[student.answerId] : null;
    return result && result.practicalVersionId === student.practicalVersionId ? result : null;
}

function formatConfidence(value) {
    const numeric = Number(value);
    return Number.isFinite(numeric) ? `${Math.round(numeric * 100)}%` : '--';
}

function applyAiSuggestion() {
    const suggestion = currentAiSuggestion.value;
    if (!suggestion) return;
    currentScore.value = suggestion.suggestedScore;
    try {
        const details = JSON.parse(suggestion.scoringDetailsJson || '[]');
        if (Array.isArray(details) && details.length && scoringItems.value.length) {
            const next = {};
            for (const item of scoringItems.value) next[item.itemId] = 0;
            for (const detail of details) if (Object.prototype.hasOwnProperty.call(next, detail.itemId)) next[detail.itemId] = detail.score;
            itemScores.value = next;
            useItemScoring.value = true;
        }
        ElMessage.success('AI 建议已填入评分框，请教师复核后提交');
    } catch (e) {
        useItemScoring.value = false;
        ElMessage.warning('已填入 AI 总分，分项建议格式异常，请人工核对');
    }
}

async function loadDeadlineStatus() {
    if (!selectedLessonId.value || !selectedClassCode.value) {
        deadlineStatus.value = null;
        return;
    }
    const classInfo = classes.value.find(c => c.classCode === selectedClassCode.value);
    try {
        const res = await getPracticalDeadlineStatus(
            selectedLessonId.value, classInfo?.entryYear || '', selectedClassCode.value);
        deadlineStatus.value = res.data || null;
    } catch (e) {
        deadlineStatus.value = null;
    }
}

// P2: 从文件路径中提取文件名
function getFileName(path) {
    if (!path) return '';
    const parts = path.split('/');
    return parts[parts.length - 1] || path;
}

function getPreviewUrl(relativePath) {
    if (!relativePath) return '';
    // 使用 common/resource/view 接口进行预览
    return import.meta.env.VITE_APP_BASE_API + "/common/resource/view?resource=" + encodeURIComponent(relativePath);
}

function selectStudent(student, index) {
    if (submitting.value && currentStudent.value?.answerId !== student?.answerId) {
        return;
    }
    scoringDetailsRequestId++;
    currentStudent.value = student;
    currentIndex.value = index;
    currentAttachmentIndex.value = 0;
    currentScore.value = student.score != null ? student.score : null; // 默认为空，方便直接输入
    const scoringItemsPromise = loadScoringItems(student.practicalVersionId)
    
    // 生成预览URL
    refreshSelectedAttachmentPreview();
    
    // P6: 加载已保存的分项得分（如果学生已被批改）
    if (student.answerId && student.score != null) {
        scoringItemsPromise.then(() => {
            if (currentStudent.value?.answerId === student.answerId) {
                loadScoringDetailsForStudent(student.answerId);
            }
        });
    }
    
    // 聚焦输入框 (根据评分模式选择对应输入框)
    nextTick(() => {
        setTimeout(() => {
            if (useItemScoring.value && scoringItems.value.length > 0) {
                // 分项评分模式：聚焦第一个评分项输入框
                focusFirstItem();
            } else if (scoreInputRef.value) {
                // 直接打分模式：聚焦总分输入框
                const input = scoreInputRef.value.$el?.querySelector('input');
                focusScoreInput(input);
            }
        }, 100); // 增加延时确保DOM更新完成
    });
}

// P6: 加载学生已保存的分项得分
function loadScoringDetailsForStudent(answerId) {
    const requestId = ++scoringDetailsRequestId;
    getScoringDetails(answerId).then(res => {
        if (requestId !== scoringDetailsRequestId || currentStudent.value?.answerId !== answerId) {
            return;
        }
        const details = res.data || [];
        // 先重置为0
        scoringItems.value.forEach(item => {
            itemScores.value[item.itemId] = 0;
        });
        // 填充已保存的分数
        details.forEach(detail => {
            if (itemScores.value.hasOwnProperty(detail.itemId)) {
                itemScores.value[detail.itemId] = detail.score || 0;
            }
        });
    }).catch(() => {
        // 如果加载失败，保持0
    });
}

function selectAttachment(index) {
    currentAttachmentIndex.value = index;
    refreshSelectedAttachmentPreview();
}

function refreshSelectedAttachmentPreview() {
    const attachment = currentAttachment.value;
    previewUrl.value = attachment?.fileKind !== 'IMAGE'
        && attachment?.previewPath
        && attachment?.previewStatus === 'success'
        ? getPreviewUrl(attachment.previewPath)
        : '';
}

async function submitScore() {
    if (!currentStudent.value || submitting.value) return;
    if (!deadlineStatus.value?.canGrade) {
        ElMessage.error('已逾期，操作题批改已锁定；如需继续批改，请联系教研员调整截止时间');
        return;
    }
    
    // P6: 如果使用分项评分，计算总分
    let finalScore = currentScore.value;
    let scoringDetails = null;
    
    if (useItemScoring.value && scoringItems.value.length > 0) {
        // 使用分项评分
        finalScore = itemTotalScore.value;
        scoringDetails = scoringItems.value.map(item => ({
            itemId: item.itemId,
            score: itemScores.value[item.itemId] || 0
        }));
    }
    
    // P1: 分数校验
    const maxScore = currentStudent.value.maxScore || 0;
    if (finalScore == null || !Number.isFinite(Number(finalScore))) {
        ElMessage.warning('请输入有效分数');
        return;
    }
    if (finalScore < 0) {
        ElMessage.warning('分数不能为负数');
        return;
    }
    if (finalScore > maxScore) {
        ElMessage.warning(`分数不能超过满分 ${maxScore} 分`);
        return;
    }
    
    // P6: 构造请求数据
    const targetStudent = currentStudent.value;
    const targetAnswerId = targetStudent.answerId;
    const targetIndex = submissions.value.findIndex(item => item.answerId === targetAnswerId);
    const expectedScore = targetStudent.score ?? null;
    const requestData = {
        answerId: targetAnswerId,
        score: finalScore,
        expectedScore,
        practicalVersionId: targetStudent.practicalVersionId || null,
        submitTime: targetStudent.submitTime,
        scoringDetails: scoringDetails
    };

    let shouldAdvance = false;
    submitting.value = true;
    try {
        await gradeSubmission(requestData);
        ElMessage.success('批改保存成功');

        const wasUngraded = expectedScore == null;
        const targetItem = targetIndex >= 0 ? submissions.value[targetIndex] : null;
        if (targetItem) targetItem.score = finalScore;
        if (currentStudent.value?.answerId === targetAnswerId) {
            currentStudent.value.score = finalScore;
        }

        if (wasUngraded && selectedClassCode.value) {
            const classInfo = classes.value.find(c => c.classCode === selectedClassCode.value);
            if (classInfo && classInfo.practicalUngraded > 0) {
                classInfo.practicalUngraded--;
            }
        }
        loadDeadlineStatus();
        shouldAdvance = currentStudent.value?.answerId === targetAnswerId;
    } catch (error) {
        // 统一请求封装已经展示错误信息，此处只负责阻止失败后跳转。
        shouldAdvance = false;
    } finally {
        submitting.value = false;
    }

    if (shouldAdvance) {
        nextSubmittedStudent();
    }
}

function getPreviewStatusText(student) {
    if (!student?.submitted) return '';
    if (student.previewStatus === 'success') return '可预览';
    if (student.previewStatus === 'pending') return '待转换';
    if (student.previewStatus === 'converting') return '转换中';
    if (student.previewStatus === 'failed') {
        const retryCount = student.previewRetryCount || 0;
        return retryCount > 0 ? `转换失败，已重试${retryCount}次` : '转换失败';
    }
    return student.previewPath ? '可预览' : '已提交';
}

function getPreviewStatusClass(status) {
    if (status === 'success') return 'is-success';
    if (status === 'pending' || status === 'converting') return 'is-pending';
    if (status === 'failed') return 'is-failed';
    return '';
}

async function handleRetryFailedPreviews() {
    if (!canRetryFailedPreviews.value) return;

    try {
        await ElMessageBox.confirm(
            `当前班级有 ${failedSubmissionCount.value} 份失败或卡住的文件，确定重新转换吗？`,
            '重新转换确认',
            {
                type: 'warning',
                confirmButtonText: '开始重转',
                cancelButtonText: '取消'
            }
        );
    } catch {
        return;
    }

    retryLoading.value = true;
    try {
        const res = await retryFailedPreviews({
            lessonId: selectedLessonId.value,
            questionId: selectedQuestionId.value,
            classCode: selectedClassCode.value,
            entryYear: currentEntryYear.value
        });
        const data = res?.data || {};
        const matchedCount = data.matchedCount || 0;
        const triggeredCount = data.triggeredCount || 0;
        const skippedCount = data.skippedCount || 0;

        if (matchedCount === 0) {
            ElMessage.info('当前班级暂无异常文件需要重转');
        } else {
            ElMessage.success(`已触发 ${triggeredCount} 条重转任务，跳过 ${skippedCount} 条`);
        }
        loadSubmissions();
    } catch (error) {
        ElMessage.error(error?.message || '重新转换失败，请稍后再试');
    } finally {
        retryLoading.value = false;
    }
}

// P6: 跳转到下一个已提交的学生 (P1: 优先跳转未批改)
function nextSubmittedStudent() {
    // 1. 优先寻找尚未批改(分数为空)的已提交学生
    // 从当前位置向后找
    for (let i = currentIndex.value + 1; i < submissions.value.length; i++) {
        if (submissions.value[i].submitted && submissions.value[i].score == null) {
            selectStudent(submissions.value[i], i);
            autoFocusItem();
            return;
        }
    }
    // 从头向当前位置找
    for (let i = 0; i < currentIndex.value; i++) {
        if (submissions.value[i].submitted && submissions.value[i].score == null) {
            selectStudent(submissions.value[i], i);
            autoFocusItem();
            return;
        }
    }
    
    // 2. 如果都批改了，则寻找下一个已提交的学生(无论是否批改)
    for (let i = currentIndex.value + 1; i < submissions.value.length; i++) {
        if (submissions.value[i].submitted) {
            selectStudent(submissions.value[i], i);
            autoFocusItem();
            return;
        }
    }
    
    ElMessage.info('已经是最后一位已提交学生了');
    // 如果是全屏状态，自动退出
    if (isFullscreen.value) {
        toggleFullscreen();
    }
}

function prevStudent() {
    if (submitting.value) return;
    if (currentIndex.value > 0) {
        selectStudent(submissions.value[currentIndex.value - 1], currentIndex.value - 1);
    }
}

function nextStudent() {
    if (submitting.value) return;
    if (currentIndex.value < submissions.value.length - 1) {
        selectStudent(submissions.value[currentIndex.value + 1], currentIndex.value + 1);
    } else {
        ElMessage.info('已经是最后一位了');
    }
}

// 全屏处理
function toggleFullscreen() {
    if (!document.fullscreenElement) {
        gradingPageRef.value.requestFullscreen();
        isFullscreen.value = true;
    } else {
        document.exitFullscreen();
        isFullscreen.value = false;
    }
}

function autoFocusItem() {
    if (useItemScoring.value && scoringItems.value.length > 0) {
        focusFirstItem();
    }
}

</script>

<style lang="scss" scoped>
.ai-key-field {
  width: 100%;
}

.ai-key-apply-link {
  margin-top: 8px;
  font-size: 13px;
}

.ai-preflight-counts,
.ai-starter-note {
  color: #606266;
  line-height: 1.7;
}

.ai-scope-options {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
  margin: 16px 0;
}

.ai-reference-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  padding: 14px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background: #fafafa;
}

.ai-reference-name {
  margin: 6px 0;
  color: #67c23a;
  word-break: break-all;

  &.is-missing { color: #f56c6c; }
}

.ai-starter-note { margin-top: 12px; font-size: 13px; }

.ai-detail-alert { margin-bottom: 16px; }

.ai-detail-summary {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin-bottom: 14px;

  > div {
    padding: 12px 8px;
    text-align: center;
    border: 1px solid #ebeef5;
    border-radius: 6px;
    background: #fafafa;
  }

  strong { display: block; color: #409eff; font-size: 20px; }
  span { color: #909399; font-size: 12px; }
}

.ai-current-stage {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 20px;
  margin: 14px 0 18px;
  padding: 12px;
  color: #303133;
  font-size: 13px;
  border-radius: 6px;
  background: #f4f7fb;

  span { color: #909399; }
}

.ai-detail-title { margin: 20px 0 10px; color: #303133; }

.ai-event-list {
  max-height: 280px;
  overflow: auto;
  border: 1px solid #ebeef5;
  border-radius: 6px;
}

.ai-event-item {
  display: grid;
  grid-template-columns: 145px 100px minmax(0, 1fr);
  align-items: center;
  gap: 8px;
  padding: 9px 10px;
  border-bottom: 1px solid #f0f2f5;
  color: #606266;
  font-size: 12px;

  &:last-child { border-bottom: 0; }
  &.is-error { background: #fef0f0; }
  &.is-warn { background: #fdf6ec; }
}

.ai-event-time { color: #909399; }
.ai-detail-privacy { color: #909399; font-size: 12px; line-height: 1.6; }

.grading-page {
  height: calc(100vh - 84px);
  display: flex;
  flex-direction: column;
  background-color: #f0f2f5;
  
  &.is-fullscreen {
     position: fixed;
     top: 0;
     left: 0;
     width: 100vw;
     height: 100vh;
     z-index: 9999;
     background: #fff;
     padding: 20px;
  }
}

.deadline-status-panel {
  width: 100%;
  margin-top: 12px;
  padding: 12px 0 0 12px;
  border-top: 1px solid #ebeef5;
  border-left: 5px solid #409eff;
  box-sizing: border-box;
}

.deadline-status-panel.is-overdue {
  border-left-color: #f56c6c;
  background: linear-gradient(90deg, #fef0f0 0, rgba(254, 240, 240, 0) 45%);
}

.deadline-status-panel.is-due_soon {
  border-left-color: #e6a23c;
}

.deadline-status-panel.is-reopened {
  border-left-color: #9b59b6;
}

.deadline-panel-head,
.deadline-summary,
.deadline-panel-grid {
  display: flex;
  align-items: center;
  gap: 12px;
}

.deadline-panel-head {
  justify-content: space-between;
  margin-bottom: 6px;
}

.deadline-panel-grid {
  flex-wrap: wrap;
  color: #606266;
  font-size: 13px;
  margin: 6px 0;
}

.grading-header {
  background: #fff;
  padding: 15px 20px;
  border-radius: 4px;
  margin-bottom: 10px;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);

  .grading-toolbar {
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 12px;
  }

  .left-filters {
    flex: 1;
    min-width: 0;
  }

  .right-actions {
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-end;
    align-items: center;
  }
  
  .filter-label {
    font-weight: bold;
    color: #606266;
    margin-right: 8px;
  }
  
  .stats {
    display: inline-block;
    margin-right: 20px;
    color: #909399;
    .highlight {
      color: #67c23a;
      font-weight: bold;
      font-size: 18px;
    }
  }
}

.grading-main {
  flex: 1;
  min-height: 0;
  display: flex;
  gap: 10px;
  overflow: hidden;
}

.student-list-panel {
  width: 250px;
  min-height: 0;
  background: #fff;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  
  .panel-title {
    padding: 15px;
    border-bottom: 1px solid #EBEEF5;
    font-weight: bold;
    background: #FAFAFA;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .grading-stats {
       font-size: 13px;
       font-weight: normal;
       color: #909399;
       
       b {
          color: #67c23a;
          font-weight: bold;
       }
    }
  }
  
  .student-list-scroll {
    flex: 1;
    overflow-y: auto;
  }
  
  .student-item {
    padding: 12px 15px;
    border-bottom: 1px solid #f5f7fa;
    cursor: pointer;
    display: flex;
    justify-content: space-between;
    align-items: center;
    transition: all 0.2s;
    
    &:hover {
      background: #f5f7fa;
    }
    
    &.active {
      background: #ecf5ff;
      border-left: 3px solid #409EFF;
      
      .s-name { color: #409EFF; font-weight: bold; }
    }
    
    .s-info {
       .s-name { font-size: 14px; color: #303133; }
       .s-remark { font-size: 11px; color: #E6A23C; margin-top: 2px; }
       .s-no { font-size: 12px; color: #909399; }
       .s-preview-status {
          font-size: 11px;
          margin-top: 4px;
          color: #909399;

          &.is-success {
             color: #67c23a;
          }

          &.is-pending {
             color: #409eff;
          }

          &.is-failed {
             color: #e6a23c;
          }
       }
    }
    
    .s-status {
       font-size: 14px;
       font-weight: bold;
       color: #67c23a;
       &.ungrad {
          color: #909399;
          font-weight: normal;
          font-size: 12px;
       }
    }
    
    // P5: 未提交学生灰显样式
    &.not-submitted {
       background: #f9f9fa;
       cursor: not-allowed;
       opacity: 0.7;
       
       &:hover {
          background: #f9f9fa;
       }
       
       .s-name { color: #909399 !important; }
       .s-status { color: #c0c4cc; font-weight: normal; }
    }
  }
}

.preview-panel {
  flex: 1;
  min-height: 0;
  background: #fff;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  
  .preview-content {
     height: 100%;
     display: flex;
     flex-direction: column;
  }
  
  .preview-header {
     padding: 10px 15px;
     background: #FAFAFA;
     border-bottom: 1px solid #EBEEF5;
     display: flex;
     justify-content: space-between;
     align-items: center;
     
     .header-info {
        display: flex;
        flex-direction: column;
        gap: 4px;
     }
     .student-label {
        font-weight: bold;
        font-size: 14px;
        color: #303133;
     }
     .file-name {
        font-size: 12px;
        color: #909399;
        font-weight: normal;
     }
     
     .download-link {
        color: #409EFF;
        text-decoration: none;
        font-size: 13px;
        &:hover { text-decoration: underline; }
     }
  }
  
  .pdf-frame {
     flex: 1;
     width: 100%;
     height: 0; /* flex grow will handle height */
  }

  .image-frame {
     flex: 1;
     width: 100%;
     height: 0;
     object-fit: contain;
     background: #f5f7fa;
  }

  .normalized-page-strip {
     flex: 1;
     min-height: 0;
     overflow: auto;
     padding: 14px;
     background: #e9edf2;
  }

  .normalized-page {
     display: block;
     max-width: 100%;
     margin: 0 auto 14px;
     background: #fff;
     box-shadow: 0 2px 10px rgba(0, 0, 0, 0.16);
  }

  .attachment-tabs {
     display: flex;
     flex-wrap: wrap;
     gap: 6px;
     padding: 8px 12px;
     border-bottom: 1px solid #ebeef5;
     background: #fff;
  }
}

.scoring-panel {
  width: 300px;
  min-height: 0;
  overflow-y: auto;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  padding: 20px;
  
  .card-title {
    font-size: 18px;
    font-weight: bold;
    margin-bottom: 20px;
    padding-left: 10px;
    border-left: 4px solid #409EFF;
  }
  
  .question-info {
     margin-bottom: 16px;
     background: #f4f4f5;
     padding: 15px;
     border-radius: 4px;
     
     .q-score {
        font-size: 16px;
        color: #606266;
     }
  }

  .ai-suggestion-card {
     margin-bottom: 16px;
     padding: 12px;
     border: 1px solid #b3e19d;
     border-radius: 6px;
     background: #f0f9eb;

     .ai-suggestion-head { display: flex; justify-content: space-between; align-items: center; gap: 8px; }
     .ai-summary { margin: 8px 0; color: #606266; line-height: 1.5; font-size: 13px; }
     .ai-item-details { margin: 8px 0; border-top: 1px dashed #b3e19d; }
     .ai-item-detail {
        display: grid;
        grid-template-columns: minmax(0, 1fr) auto;
        gap: 4px 8px;
        padding: 7px 0;
        border-bottom: 1px dashed #d9ecff;

        small { grid-column: 1 / -1; color: #606266; line-height: 1.4; }
     }
     .ai-confidence { margin: 6px 0 10px; color: #909399; font-size: 12px; }
  }
  
  .score-input-area {
     margin-bottom: 30px;
     text-align: center;
     
     .input-label {
        font-size: 16px;
        margin-bottom: 10px;
        color: #303133;
     }
  }
  
  .submit-btn {
     width: 100%;
     margin-bottom: 20px;
  }
  
  .nav-actions {
     display: flex;
     justify-content: space-between;
     button { flex: 1; margin: 0 5px; }
  }
  
  // P6: 评分模式切换
  .scoring-mode-switch {
     margin-bottom: 15px;
     text-align: center;
  }
  
  // P6: 分项评分区域
  .item-scoring-area {
     margin-bottom: 20px;
     
     .item-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 8px 0;
        border-bottom: 1px dashed #ebeef5;
        
        .item-name {
           font-size: 14px;
           color: #303133;
        }
        .item-input {
           display: flex;
           align-items: center;
           gap: 5px;
           
           .item-max {
              font-size: 12px;
              color: #909399;
           }
        }
     }
     
     .item-total {
        margin-top: 15px;
        text-align: right;
        font-size: 16px;
        color: #409EFF;
        
        strong {
           font-size: 22px;
           font-weight: bold;
        }
     }
  }
  
  // 班级选项样式
  .class-option {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    
    .ungraded-badge {
      background: #F56C6C;
      color: #fff;
      padding: 2px 6px;
      border-radius: 10px;
      font-size: 11px;
      margin-left: 8px;
    }
    
    .graded-badge {
      color: #67C23A;
      font-weight: bold;
      margin-left: 8px;
    }
    
    .no-submit-badge {
      color: #909399;
      font-size: 12px;
      margin-left: 8px;
    }
  }
}

.s-ai {
  margin-top: 3px;
  color: #67c23a;
  font-size: 11px;
  white-space: nowrap;
}
</style>
