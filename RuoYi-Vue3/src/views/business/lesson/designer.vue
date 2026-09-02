<template>
  <div class="app-container">
    <el-row :gutter="20" class="designer-grid">
      <!-- 左侧：课程内容区 -->
      <el-col :span="24" :xl="11">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>课程内容设计</span>
            </div>
          </template>
          <!-- 核心修复：将 :model 指向 form 本身 -->
          <el-form ref="lessonRef" :model="form" :rules="rules" label-width="80px">
            <el-form-item label="课程标题" prop="lessonTitle">
              <el-input v-model="form.lessonTitle" placeholder="请输入课程/作业标题" />
            </el-form-item>
            <el-row>
              <el-col :span="12">
                <el-form-item label="年级" prop="grade">
                  <el-select v-model="form.grade" placeholder="请选择年级" style="width:100%" disabled @change="handleGradeChange">
                    <el-option
                      v-for="dict in biz_grade"
                      :key="dict.value"
                      :label="dict.label"
                      :value="parseInt(dict.value)"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="学期" prop="semester">
                  <el-select v-model="form.semester" placeholder="请选择学期" style="width:100%" :disabled="isAddMode">
                    <el-option
                      v-for="dict in biz_semester"
                      :key="dict.value"
                      :label="dict.label"
                      :value="dict.value"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="第几课" prop="lessonNum">
              <el-input-number v-model="form.lessonNum" placeholder="课程序号" :min="1" :max="30" disabled />
              <el-tooltip content="系统根据已创建课程自动计算" placement="top">
                <span class="form-help-dot">?</span>
              </el-tooltip>
            </el-form-item>

            <el-form-item label="课程用途">
              <el-radio-group v-model="form.lessonMode">
                <el-radio value="assessment">常规课</el-radio>
                <el-radio value="attendance">课堂考勤</el-radio>
              </el-radio-group>
              <el-tooltip content="常规课可出题、绑导学单；课堂考勤可不选题，学生仅签到且不计入作业均分。" placement="top">
                <span class="form-help-dot">?</span>
              </el-tooltip>
            </el-form-item>
            <el-form-item v-if="form.lessonMode === 'attendance'" label="教师说明">
              <el-input
                v-model="form.teacherNote"
                type="textarea"
                :rows="2"
                maxlength="500"
                show-word-limit
                placeholder="可选：本节课说明，学生端可见"
              />
            </el-form-item>

            <el-form-item v-if="form.lessonMode !== 'attendance'" label="扩展功能">
              <div class="feature-settings">
                <div class="feature-row">
                  <div class="feature-label">
                    <b>物联网实验</b>
                    <el-tooltip content="开启后，教师和学生首页显示物联入口；实验项目和班级分组在物联页配置。" placement="top">
                      <span class="form-help-dot">?</span>
                    </el-tooltip>
                  </div>
                  <el-switch v-model="form.iotEnabled" inline-prompt active-text="开" inactive-text="关" />
                </div>
                <div class="feature-row">
                  <div class="feature-label">
                    <b>在线协作</b>
                    <span class="feature-status">{{ collaborationStatusText }}</span>
                    <el-tooltip content="必须先选择带 Word、Excel 或 PPT 起始文件的“文件作品”操作题；Python 编程题不能作为协作文档。" placement="top">
                      <span class="form-help-dot">?</span>
                    </el-tooltip>
                  </div>
                  <el-switch :model-value="collaborationForm.enabled" inline-prompt active-text="开" inactive-text="关" @change="handleCollaborationToggle" />
                </div>
              </div>
            </el-form-item>

            <el-form-item v-if="form.lessonMode !== 'attendance'" label="本课工具">
              <div style="width: 100%">
                <div class="compact-setting-heading">
                  <span>{{ form.lessonTools.length ? `已配置 ${form.lessonTools.length} 个工具` : '未配置' }}</span>
                  <div>
                    <el-tooltip content="学生可从首页的“学生实验工具”面板打开本课网址。" placement="top">
                      <span class="form-help-dot">?</span>
                    </el-tooltip>
                    <el-button link type="primary" @click="lessonToolsExpanded = !lessonToolsExpanded">{{ lessonToolsExpanded ? '收起' : '配置' }}</el-button>
                  </div>
                </div>
                <div v-show="lessonToolsExpanded">
                  <div v-for="(t, ti) in form.lessonTools" :key="ti" class="lesson-tool-row">
                  <el-input v-model="t.toolName" placeholder="工具名称，如：实验一" size="small" style="width: 150px" />
                  <el-input v-model="t.toolUrl" placeholder="http:// 或 https:// 网址" size="small" style="flex: 1" />
                  <el-button type="danger" link icon="Delete" @click="removeLessonTool(ti)" />
                  </div>
                  <el-button size="small" type="primary" plain icon="Plus" @click="addLessonTool">添加工具</el-button>
                </div>
              </div>
            </el-form-item>

            <el-form-item label="指派班级" prop="assignedClasses">
              <el-checkbox-group v-model="form.assignedClasses">
                <el-checkbox 
                  v-for="cls in filteredManagedClasses" 
                  :key="cls.id" 
                  :value="cls.classCode + '班'"
                >
                  {{ cls.classCode }}班
                </el-checkbox>
              </el-checkbox-group>
              <!-- 提示语 -->
              <div v-if="form.assignedClasses.length === 0 && form.grade && filteredManagedClasses.length > 0" style="color: #E6A23C; font-size: 12px; margin-top: 5px;">
                可暂不指派班级，后续正式上课前再补充即可
              </div>
              <div v-else-if="form.grade && filteredManagedClasses.length === 0" style="color: #909399; font-size: 12px;">
                您没有管理该年级对应的班级，请先在"班级管理"中添加
              </div>
              <div v-else-if="!form.grade" style="color: #909399; font-size: 12px;">
                请先选择年级
              </div>
            </el-form-item>

            <el-form-item
              v-if="form.lessonMode !== 'attendance' && (hasTheorySelected || hasPracticalSelected)"
              label="学生开放"
            >
              <div class="initial-gate-panel">
                <div v-if="hasTheorySelected" class="initial-gate-row">
                  <div class="feature-label">
                    <b>理论测试题</b>
                    <el-tooltip content="仅初始化新指派班级；已有班级状态不覆盖，课中可在成绩查询开启。" placement="top">
                      <span class="form-help-dot">?</span>
                    </el-tooltip>
                  </div>
                  <el-switch v-model="form.initialTheoryOpen" inline-prompt active-text="开" inactive-text="关" />
                </div>
                <div v-if="hasPracticalSelected" class="initial-gate-row">
                  <div class="feature-label">
                    <b>操作题（含 Python）</b>
                    <el-tooltip content="仅初始化新指派班级；已有班级状态不覆盖，课中可在成绩查询开启。" placement="top">
                      <span class="form-help-dot">?</span>
                    </el-tooltip>
                  </div>
                  <el-switch v-model="form.initialPracticalOpen" inline-prompt active-text="开" inactive-text="关" />
                </div>
              </div>
            </el-form-item>

            <lesson-guide-sheet-panel
              v-model:enabled="form.guideSheetEnabled"
              v-model:sourceSheetId="form.guideSheetSourceSheetId"
              v-model:replaceRequested="form.guideSheetReplaceRequested"
              :current-binding="initialGuideSheetBinding"
              :grade="form.grade"
              :semester="form.semester"
              :lesson-num="form.lessonNum"
              :grade-options="biz_grade"
              :semester-options="biz_semester"
            />

            <!-- 出题模式设置 -->
            <el-divider content-position="left">出题设置</el-divider>
            <el-form-item label="出题模式">
              <el-radio-group v-model="form.shuffleMode">
                <el-radio :value="0">固定顺序</el-radio>
                <el-radio :value="1">随机排序</el-radio>
                <el-radio :value="2">随机抽题</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <!-- 随机抽题数量设置 (仅模式2时显示) -->
            <el-row v-if="form.shuffleMode === 2" :gutter="10">
              <el-col :span="12">
                <el-form-item label="选择题">
                  <el-input-number 
                    v-model="form.randomChoiceCount" 
                    :min="0" 
                    :max="choiceCount"
                    :disabled="choiceCount === 0"
                  />
                  <span style="margin-left: 8px; color: #909399; font-size: 12px;">/ {{ choiceCount }} 道</span>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="判断题">
                  <el-input-number 
                    v-model="form.randomJudgmentCount" 
                    :min="0" 
                    :max="judgmentCount"
                    :disabled="judgmentCount === 0"
                  />
                  <span style="margin-left: 8px; color: #909399; font-size: 12px;">/ {{ judgmentCount }} 道</span>
                </el-form-item>
              </el-col>
            </el-row>
            <div v-if="form.shuffleMode === 2" style="color: #E6A23C; font-size: 12px; margin-bottom: 10px;">
              💡 提示：设置为0表示不限制（使用全部题目）
            </div>

          </el-form>

          <el-divider />
          <h4 :style="{ color: selectedQuestions.length === 0 ? '#607d8b' : totalScore === 100 ? '#67C23A' : '#F56C6C' }">
            已选普通题目
            <template v-if="selectedQuestions.length > 0">（当前总分：{{ totalScore }} / 100）</template>
            <span v-if="selectedQuestions.length === 0" style="font-size: 12px; font-weight: normal; margin-left: 10px;">
              未选择普通题，电子导学单将按独立口径计分
            </span>
            <span v-else-if="totalScore !== 100" style="font-size: 12px; font-weight: normal; margin-left: 10px;">
              (还差 {{ 100 - totalScore }} 分)
            </span>
            <span v-else style="font-size: 12px; font-weight: normal; margin-left: 10px;">
              (已达标)
            </span>
          </h4>
          <div v-if="hasInconsistentScores" style="color: #E6A23C; font-size: 12px; margin-bottom: 10px;">
            ⚠️ 注意：检测到同类题目分值不一致。随机抽题模式下，建议保持同题型分值相同，否则学生试卷总分可能浮动。当前预览总分仅供参考。
          </div>
          
          <div v-if="choiceCount || judgmentCount" class="selected-question-tools">
            <el-popover placement="bottom-start" :width="390" trigger="click">
              <template #reference><el-button size="small" plain>批量改分</el-button></template>
              <div class="batch-popover">
                <el-select v-model="batchScoreType" placeholder="选择题型" style="width: 150px" size="small">
                  <el-option :label="`选择题 (${choiceCount}题)`" value="choice" />
                  <el-option :label="`判断题 (${judgmentCount}题)`" value="judgment" />
                </el-select>
                <el-input-number v-model="batchScoreValue" :min="0" :max="100" size="small" controls-position="right" style="width: 100px" />
                <span>分</span>
                <el-button type="primary" size="small" @click="applyBatchScore">应用</el-button>
              </div>
            </el-popover>
          </div>
          <el-table :data="selectedQuestions" row-key="questionId" style="width: 100%">
            <el-table-column label="题干" prop="questionContent" :show-overflow-tooltip="true">
              <template #default="scope">
                <div class="question-content-text">{{ stripHtml(scope.row.questionContent) }}</div>
                <div v-if="scope.row.questionType === 'choice'" class="options-list">
                  <p>A. {{ scope.row.optionA || '未配置' }}</p>
                  <p>B. {{ scope.row.optionB || '未配置' }}</p>
                  <p>C. {{ scope.row.optionC || '未配置' }}</p>
                  <p>D. {{ scope.row.optionD || '未配置' }}</p>
                  <p class="correct-answer">正确答案：{{ scope.row.answer }}</p>
                </div>
                <!-- 新增：判断题在已选列表中回显正确答案 -->
                <div v-else-if="scope.row.questionType === 'judgment'" class="judge-info">
                  正确答案：{{ formatJudgeAnswer(scope.row.answer) }}
                </div>
                <div v-else-if="scope.row.questionType === 'typing'" class="typing-info">
                  <span>总字数：{{ scope.row.wordCount || 0 }}</span>
                  <span style="margin-left: 15px;">时长：</span>
                  <el-input-number 
                    v-model="scope.row.typingDuration" 
                    :min="1" :max="30" size="small" 
                    controls-position="right"
                    style="width: 100px; margin-right: 4px;"
                  /> 分钟
                  <span style="margin-left: 8px; color: #909399; font-size: 11px;">
                    {{ getTypingDurationHint(scope.row) }}
                  </span>
                </div>
                <!-- 操作题显示评分标准 -->
                <div v-else-if="scope.row.questionType === 'practical'" class="scoring-info">
                  <div v-if="scope.row.practicalMode === 'PYTHON'" class="no-scoring">操作题 · Python 在线编程（自动判题）</div>
                  <div v-else-if="scope.row.practicalMode === 'FLOWCHART'" class="no-scoring">操作题 · 画程流程图（结构检查＋教师确认）</div>
                  <div v-else-if="scope.row.scoringItems && scope.row.scoringItems.length > 0">
                    <span class="scoring-label">评分标准：</span>
                    <span v-for="(item, idx) in scope.row.scoringItems" :key="item?.itemId || idx" class="scoring-item">
                      <template v-if="item">{{ item.itemName }}({{ item.itemScore }}%){{ idx < scope.row.scoringItems.length - 1 ? ' / ' : '' }}</template>
                    </span>
                  </div>
                  <div v-else class="no-scoring">暂无评分标准</div>
                </div>
                <div v-else-if="scope.row.questionType === 'python'" class="scoring-info">
                  <div class="no-scoring">操作题 · Python 在线编程（等待数据迁移）</div>
                </div>
                <!-- 异常处理：未知题型 -->
                <div v-else class="unknown-type-error" style="color: #F56C6C; background: #fef0f0; padding: 5px; margin-top: 5px; border-radius: 4px;">
                   ⚠️ 题目数据异常或原题已被删除 (ID: {{ scope.row.questionId }})
                </div>
              </template>
            </el-table-column>
            <el-table-column label="题型 / 方式" align="center" width="150">
               <template #default="scope">
                  <dict-tag :options="biz_question_type" :value="scope.row.questionType"/>
                  <div v-if="scope.row.questionType === 'practical'" class="answer-mode-text">{{ practicalModeLabel(scope.row.practicalMode) }}</div>
              </template>
            </el-table-column>
            <el-table-column label="分值" align="center" width="105">
              <template #default="scope">
                <el-input-number v-model="scope.row.questionScore" :min="0" :max="100" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="120" fixed="right">
              <template #default="scope">
                <!-- 新增：操作题支持在已选列表中直接预览 -->
                <el-button
                  v-if="scope.row.questionType === 'practical' && (scope.row.previewPath || scope.row.practicalMode === 'PYTHON')"
                  link
                  type="success"
                  @click="scope.row.practicalMode === 'PYTHON' ? openPythonPreview(scope.row) : handlePreviewFile(scope.row)"
                >预览</el-button>
                <el-button link type="danger" @click="handleRemoveQuestion(scope.row)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧：题库选题区 -->
      <el-col :span="24" :xl="13">
        <el-card>
           <template #header>
             <div class="card-header">
               <span>教学资源库</span>
             </div>
           </template>
          <div class="resource-tabs">
          <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="68px">
            <el-form-item label="题干" prop="questionContent">
              <el-input v-model="queryParams.questionContent" placeholder="请输入题干关键词" clearable @keyup.enter="handleQuery"/>
            </el-form-item>
            <el-form-item label="年级" prop="grade">
              <el-select v-model="queryParams.grade" placeholder="年级" clearable style="width: 120px">
                <el-option v-for="dict in biz_grade" :key="dict.value" :label="dict.label" :value="dict.value"/>
              </el-select>
            </el-form-item>
            <el-form-item label="学期" prop="semester">
              <el-select v-model="queryParams.semester" placeholder="学期" clearable style="width: 100px">
                <el-option v-for="dict in biz_semester" :key="dict.value" :label="dict.label" :value="dict.value"/>
              </el-select>
            </el-form-item>
             <el-form-item label="题型" prop="questionType">
              <el-select v-model="queryParams.questionType" placeholder="题目类型" clearable style="width: 120px">
                <el-option v-for="dict in biz_question_type" :key="dict.value" :label="dict.label" :value="dict.value"/>
              </el-select>
            </el-form-item>
            <el-form-item label="课时" prop="lessonNum">
              <el-select v-model="queryParams.lessonNum" placeholder="第几课" clearable style="width: 140px">
                <el-option v-for="n in 20" :key="n" :label="'第' + n + '课'" :value="n"/>
              </el-select>
            </el-form-item>
             <el-form-item label="来源" prop="isPublic">
               <el-select v-model="queryParams.isPublic" placeholder="题目来源" clearable style="width: 120px">
                 <el-option label="公共题库" value="Y" />
                 <el-option label="我的私有" value="N" />
               </el-select>
             </el-form-item>
            <el-form-item label="操作方式" prop="practicalMode">
              <el-select v-model="queryParams.practicalMode" placeholder="操作方式" clearable style="width: 140px">
                 <el-option label="Python 在线编程" value="PYTHON" />
                 <el-option label="画程流程图" value="FLOWCHART" />
                 <el-option label="文件作品" value="FILE" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table v-loading="loading" :data="questionBankList" row-key="questionId">
            <el-table-column label="题干/选项" min-width="260">
              <template #default="scope">
                <div class="question-content-text">{{ stripHtml(scope.row.questionContent) }}</div>
                <div v-if="scope.row.questionType === 'choice'" class="options-list">
                  <p>A. {{ scope.row.optionA || '未配置' }}</p>
                  <p>B. {{ scope.row.optionB || '未配置' }}</p>
                  <p>C. {{ scope.row.optionC || '未配置' }}</p>
                  <p>D. {{ scope.row.optionD || '未配置' }}</p>
                  <p class="correct-answer">正确答案：{{ scope.row.answer }}</p>
                </div>
                <div v-else-if="scope.row.questionType === 'judgment'" class="judge-info">
                  正确答案：{{ formatJudgeAnswer(scope.row.answer) }}
                </div>
                <div v-else-if="scope.row.questionType === 'typing'" class="typing-info">
                  <span>总字数：{{ scope.row.wordCount || 0 }}</span>
                  <span style="margin-left: 15px;">时长：{{ scope.row.typingDuration || 0 }} 分钟</span>
                </div>
                <!-- 操作题显示评分标准 -->
                <div v-else-if="scope.row.questionType === 'practical'" class="scoring-info">
                  <div v-if="scope.row.scoringItems && scope.row.scoringItems.length > 0">
                    <span class="scoring-label">评分标准：</span>
                    <span v-for="(item, idx) in scope.row.scoringItems" :key="idx" class="scoring-item">
                      {{ item.itemName }}({{ item.itemScore }}%){{ idx < scope.row.scoringItems.length - 1 ? ' / ' : '' }}
                    </span>
                  </div>
                  <div v-else class="no-scoring">暂无评分标准</div>
                </div>
              </template>
            </el-table-column>
             <el-table-column label="题型" align="center" width="100">
               <template #default="scope">
                  <dict-tag :options="biz_question_type" :value="scope.row.questionType"/>
               </template>
             </el-table-column>
             <el-table-column label="作答方式" align="center" width="145">
               <template #default="scope">
                 <span v-if="scope.row.questionType === 'practical'">{{ practicalModeLabel(scope.row.practicalMode) }}</span>
                 <span v-else>-</span>
               </template>
             </el-table-column>
             <el-table-column label="出题人" align="center" width="120" show-overflow-tooltip>
               <template #default="scope">{{ scope.row.nickName || scope.row.createBy || '-' }}</template>
             </el-table-column>
             <el-table-column label="操作" align="center" width="100">
               <template #default="scope">
                 <el-button
                   v-if="scope.row.questionType === 'practical' && (scope.row.previewPath || scope.row.practicalMode === 'PYTHON')"
                   link
                   type="success"
                   @click="scope.row.practicalMode === 'PYTHON' ? openPythonPreview(scope.row) : handlePreviewFile(scope.row)"
                 >预览</el-button>
                 <el-button 
                   link 
                   type="primary" 
                   @click="handleAddQuestion(scope.row)" 
                   :disabled="isQuestionSelected(scope.row.questionId)"
                 >添加</el-button>
               </template>
             </el-table-column>
          </el-table>
           <pagination
             v-show="total > 0"
             :total="total"
             v-model:page="queryParams.pageNum"
             v-model:limit="queryParams.pageSize"
             @pagination="getQuestionList"
           />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="footer-toolbar">
      <el-button type="primary" @click="submitForm">保 存</el-button>
      <el-button @click="router.push('/teacher-dashboard')">返回教师首页</el-button>
    </div>

    <pdf-preview ref="pdfPreviewRef" />
    <el-dialog v-model="pythonPreviewVisible" title="Python 题目预览" width="760px" append-to-body>
      <template v-if="pythonPreviewQuestion">
        <h3 class="python-preview-title">{{ pythonPreviewQuestion.questionContent }}</h3>
        <div class="python-preview-meta">题目 ID：{{ pythonPreviewQuestion.questionId }}　年级：{{ pythonPreviewQuestion.grade }}</div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="输入说明">{{ pythonPreviewConfig.inputDescription || '无' }}</el-descriptions-item>
          <el-descriptions-item label="输出说明">{{ pythonPreviewConfig.outputDescription || '无' }}</el-descriptions-item>
          <el-descriptions-item label="样例">{{ pythonPreviewCases.map(item => `输入：${item.inputText || '无'}；输出：${item.expectedOutput}`).join('；') || '暂无样例' }}</el-descriptions-item>
          <el-descriptions-item label="限制条件">{{ pythonPreviewConfig.constraintsText || '无' }}</el-descriptions-item>
          <el-descriptions-item label="起始代码"><pre class="python-preview-code">{{ pythonPreviewConfig.starterCode || '暂无' }}</pre></el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>
    <el-dialog v-model="collaborationMaterialVisible" title="选择在线协作文件" width="520px" append-to-body>
      <p class="collaboration-material-tip">请选择用于协作的文件作品及起始文件。每个授课班会获得一份独立副本。</p>
      <el-radio-group v-model="collaborationForm.materialId" class="collaboration-material-list">
        <el-radio v-for="item in collaborationCandidates" :key="item.materialId" :value="item.materialId">
          {{ item.questionTitle ? `${item.questionTitle} · ` : '' }}{{ item.fileName }}
        </el-radio>
      </el-radio-group>
      <template #footer>
        <el-button @click="cancelCollaborationMaterial">取消</el-button>
        <el-button type="primary" @click="confirmEnableCollaboration">确认开启</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup name="LessonDesigner">
import { ref, computed, onMounted, getCurrentInstance } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getLessonDetails, saveAllLessonDetails } from "@/api/business/lesson";
import { getCollaborationLesson, saveCollaborationLesson } from '@/api/business/collaboration';
import { getQuestion, listQuestion } from "@/api/business/question";
import { previewProgrammingQuestion } from "@/api/business/programming";
import { getMyClasses } from "@/api/business/teacherClass";
import { listScoringItems } from "@/api/business/scoringItem";
import PdfPreview from '@/components/PdfPreview/index.vue';
import LessonGuideSheetPanel from './components/LessonGuideSheetPanel.vue';
import { calculateEntryYearFromGrade } from '@/utils/academicYear';

const { proxy } = getCurrentInstance();
const route = useRoute();
const router = useRouter();

const { biz_grade, biz_semester, biz_question_type } = proxy.useDict("biz_grade", "biz_semester", "biz_question_type");

const loading = ref(true);
const total = ref(0);
const pdfPreviewRef = ref(null);
const isAddMode = ref(false);
const pythonPreviewVisible = ref(false);
const pythonPreviewQuestion = ref(null);
const pythonPreviewConfig = ref({});
const pythonPreviewCases = ref([]);
const collaborationForm = ref({ enabled: false, questionId: null, materialId: null });
const collaborationCandidates = ref([]);
const collaborationMaterialVisible = ref(false);
const lessonToolsExpanded = ref(false);

// 核心修复：将 assignedClassCodes 整合到 form 对象中
const form = ref({
  lessonId: null,
  lessonTitle: null,
  grade: null,
  entryYear: null,
  semester: null,
  lessonNum: 1,
  assignedClasses: [], // 改为存储 "entryYear-classCode" 格式
  initialTheoryOpen: true,
  initialPracticalOpen: true,
  shuffleMode: 0,      // 出题模式: 0=固定, 1=随机排序, 2=随机抽取
  randomChoiceCount: 0,   // 随机抽取选择题数
  randomJudgmentCount: 0, // 随机抽取判断题数
  lessonMode: 'assessment', // assessment 常规课 / attendance 课堂考勤
  // 课程级物联网实验开关：开启后教师/学生首页才显示物联入口（考勤课强制关闭）
  iotEnabled: false,
  // 自动推进在教师首页设置，设计器仅保留字段以便保存时透传已有配置
  autoAdvanceEnabled: false,
  autoAdvanceThresholdPct: 50,
  autoAdvanceDelayHours: 2,
  teacherNote: '',
  lessonTools: [], // 本节课工具（学生端实验工具面板先显示，随课程保存）
  guideSheetEnabled: false,
  guideSheetSourceSheetId: null,
  guideSheetReplaceRequested: false,
});
const selectedQuestions = ref([]);
const myManagedClasses = ref([]); // 教师管理的班级列表
const initialGuideSheetBinding = ref(null);

const filePracticalQuestions = computed(() => selectedQuestions.value.filter(isFilePractical));
const collaborationStatusText = computed(() => {
  if (collaborationForm.value.enabled) return '已开启';
  return filePracticalQuestions.value.length ? '未开启' : '需先添加文件作品题';
});

const questionBankList = ref([]);
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  questionContent: null,
  grade: null,
  semester: null,
  isPublic: null,
  questionType: null,
  practicalMode: null,
  lessonNum: null,
  orderByColumn: 'createTime',  // 按创建时间排序
  isAsc: 'desc',                 // 降序，最新的在前
});

const rules = {
  lessonTitle: [{ required: true, message: "课程/作业标题不能为空", trigger: "blur" }],
  grade: [{ required: true, message: "年级不能为空", trigger: "change" }],
  semester: [{ required: true, message: "学期不能为空", trigger: "change" }]
};

const totalScore = computed(() => {
  // 1. 分离题目类型
  const choices = selectedQuestions.value.filter(q => q.questionType === 'choice');
  const judgments = selectedQuestions.value.filter(q => q.questionType === 'judgment');
  const others = selectedQuestions.value.filter(q => q.questionType !== 'choice' && q.questionType !== 'judgment');

  let score = 0;

  // 2. 其他题目（打字、操作）：全部计入
  score += others.reduce((sum, q) => sum + (q.questionScore || 0), 0);

  // 3. 选择题：根据 shuffleMode 和 randomChoiceCount 决定
  if (form.value.shuffleMode === 2 && form.value.randomChoiceCount > 0) {
    // 随机抽题：取前 N 题计算预计总分
    const count = Math.min(form.value.randomChoiceCount, choices.length);
    score += choices.slice(0, count).reduce((sum, q) => sum + (q.questionScore || 0), 0);
  } else {
    // 固定/全量：全部计入
    score += choices.reduce((sum, q) => sum + (q.questionScore || 0), 0);
  }

  // 4. 判断题：同上
  if (form.value.shuffleMode === 2 && form.value.randomJudgmentCount > 0) {
    const count = Math.min(form.value.randomJudgmentCount, judgments.length);
    score += judgments.slice(0, count).reduce((sum, q) => sum + (q.questionScore || 0), 0);
  } else {
    score += judgments.reduce((sum, q) => sum + (q.questionScore || 0), 0);
  }

  return score;
});

// 检查随机模式下分值是否一致
const hasInconsistentScores = computed(() => {
  if (form.value.shuffleMode !== 2) return false;
  
  const choices = selectedQuestions.value.filter(q => q.questionType === 'choice');
  const judgments = selectedQuestions.value.filter(q => q.questionType === 'judgment');

  const isConsistent = (arr) => {
    if (arr.length <= 1) return true;
    const first = arr[0].questionScore || 0;
    return arr.every(q => (q.questionScore || 0) === first);
  };
  
  // 只有当启用了随机抽题（count > 0）且题目列表不为空时才检查
  if (form.value.randomChoiceCount > 0 && choices.length > 0 && !isConsistent(choices)) return true;
  if (form.value.randomJudgmentCount > 0 && judgments.length > 0 && !isConsistent(judgments)) return true;
  
  return false;
});

// 过滤后的班级列表（根据选择的入学年份过滤）
const filteredManagedClasses = computed(() => {
  if (!form.value.grade) {
    return [];
  }
  // 课程归属以显式入学年份为准；仅兼容尚未返回该字段的旧接口。
  const targetEntryYear = form.value.entryYear || calculateEntryYearFromGrade(form.value.grade);
  
  if (!targetEntryYear) {
    return [];
  }
  
  const result = myManagedClasses.value.filter(cls => {
    // 使用 == 进行宽松匹配
    return cls.entryYear == targetEntryYear;
  });
  return result;
});

// 普通编辑只能调整内容年级，已持久化的课程届别保持不变。
function handleGradeChange(newGrade) {
  if (!form.value.entryYear) {
    form.value.entryYear = calculateEntryYearFromGrade(newGrade);
  }
}

// ... (省略中间代码)

// 排序权重映射
const TYPE_WEIGHT = {
  'typing': 0,
  'practical': 1,
  'choice': 2,
  'judgment': 3
};

function sortQuestions() {
  selectedQuestions.value.sort((a, b) => {
    const wA = TYPE_WEIGHT[a.questionType] ?? 99;
    const wB = TYPE_WEIGHT[b.questionType] ?? 99;
    if (wA !== wB) {
      return wA - wB;
    }
    // 同类型保持相对顺序 (或按 orderNum 可能更好，这里暂保持添加顺序)
    return 0;
  });
  
  // 重新计算 orderNum 以保持连续（可选，视后端需求而定）
  selectedQuestions.value.forEach((q, index) => {
    q.orderNum = index + 1;
  });
}

// 提交表单
function submitForm() {
  proxy.$refs["lessonRef"].validate(valid => {
    if (valid) {
      if (form.value.guideSheetEnabled && !form.value.guideSheetSourceSheetId) {
        proxy.$modal.msgError('已开启电子导学单，请先选择一份导学单模板。');
        return;
      }
      const isAttendance = form.value.lessonMode === 'attendance'
      const canKeepClosedGuideSheet = Boolean(form.value.lessonId && initialGuideSheetBinding.value)
      // 考勤课允许 0 题且不绑导学单；常规课仍要求至少一类内容
      if (!isAttendance && selectedQuestions.value.length === 0 && !form.value.guideSheetEnabled && !canKeepClosedGuideSheet) {
        proxy.$modal.msgError('请至少选择普通题目或开启电子导学单；若仅考勤请将课程用途设为「课堂考勤」。');
        return;
      }
      // 电子导学单独立计分，仅普通题存在时校验 100 分。
      if (selectedQuestions.value.length > 0 && totalScore.value !== 100) {
        proxy.$modal.msgError(`当前总分为 ${totalScore.value} 分，必须凑满 100 分才能保存！`);
        return;
      }

      // 提交前确保排序
      sortQuestions();
      
      // 考勤课强制关闭自动推进，避免误开
      const isAttendanceSubmit = form.value.lessonMode === 'attendance'
      // 构造提交数据
      const data = {
        ...form.value,
        autoAdvanceEnabled: isAttendanceSubmit ? false : Boolean(form.value.autoAdvanceEnabled),
        autoAdvanceThresholdPct: Number(form.value.autoAdvanceThresholdPct) || 50,
        autoAdvanceDelayHours: Number(form.value.autoAdvanceDelayHours) || 2,
        // 物联网开关：考勤课强制关闭
        iotEnabled: isAttendanceSubmit ? false : Boolean(form.value.iotEnabled),
        // 本节课工具：随课程保存，学生端面板先展示
        lessonTools: (form.value.lessonTools || []).filter(t => t && t.toolName && t.toolUrl),
        questions: selectedQuestions.value,
        // 入学年份随表单显式提交，避免跨学年时再由年级反推错届。
        assignedClassCodes: form.value.assignedClasses 
      };

      if (form.value.lessonId) {
        // 修改模式使用 saveAll
        saveAllLessonDetails(data).then(async response => {
          await synchronizeCollaboration(response.data?.lessonId || response.lessonId || form.value.lessonId);
          proxy.$modal.msgSuccess("修改成功");
          // 修改成功后跳转回来源页面（通常是教师首页或列表页）
          if (route.query.redirect) {
              router.push({
                path: route.query.redirect,
                query: { refresh: String(Date.now()) }
              });
          } else {
              router.push({ path: '/teacher-dashboard/index', query: { refresh: String(Date.now()) } }); // 默认回教师首页
          }
        });
      } else {
        // 新增模式
        saveAllLessonDetails(data).then(async response => {
          await synchronizeCollaboration(response.data?.lessonId || response.lessonId);
          proxy.$modal.msgSuccess("新增成功");
          router.push({ path: '/teacher-dashboard/index', query: { refresh: String(Date.now()) } });
        });
      }
    }
  });
}

// ... 

function initialize() {
  const { lessonId } = route.params;
  const { grade, entryYear, classes, semester, guideSheetId } = route.query;
  const presetGuideSheetId = Number.parseInt(guideSheetId, 10);
  const hasPresetGuideSheet = Number.isInteger(presetGuideSheetId) && presetGuideSheetId > 0;

  // 先加载教师管理的班级
  loadMyManagedClasses();

  if (lessonId) {
    isAddMode.value = false;
    getLessonDetails(lessonId).then(response => {
      const detail = response.data || {};
      
      // 后端返回的是 ["1班", "5班"] 格式，直接使用
      const assignedClasses = detail.assignedClassCodes || [];

      form.value = {
        lessonId: detail.lessonId,
        lessonTitle: detail.lessonTitle,
        grade: detail.grade,
        entryYear: detail.entryYear ? String(detail.entryYear) : null,
        semester: detail.semester ?? getDefaultSemester(),
        lessonNum: detail.lessonNum,
        assignedClasses: assignedClasses,
        // 该设置只应用于本次新增加的班级，已有班级状态由后端保留。
        initialTheoryOpen: true,
        initialPracticalOpen: true,
        shuffleMode: detail.shuffleMode ?? 0,
        randomChoiceCount: detail.randomChoiceCount ?? 0,
        randomJudgmentCount: detail.randomJudgmentCount ?? 0,
        lessonMode: detail.lessonMode === 'attendance' ? 'attendance' : 'assessment',
        teacherNote: detail.teacherNote || '',
        lessonTools: (detail.lessonTools || []).map(t => ({ toolName: t.toolName, toolUrl: t.toolUrl })),
        iotEnabled: detail.lessonMode !== 'attendance'
          && (detail.iotEnabled === true || detail.iotEnabled === 1 || detail.iotEnabled === '1'),
        autoAdvanceEnabled: detail.lessonMode === 'attendance'
          ? false
          : (detail.autoAdvanceEnabled === true || detail.autoAdvanceEnabled === 1 || detail.autoAdvanceEnabled === '1'),
        autoAdvanceThresholdPct: detail.autoAdvanceThresholdPct != null ? Number(detail.autoAdvanceThresholdPct) : 50,
        autoAdvanceDelayHours: detail.autoAdvanceDelayHours != null ? Number(detail.autoAdvanceDelayHours) : 2,
        guideSheetEnabled: Boolean(detail.guideSheetEnabled),
        guideSheetSourceSheetId: detail.guideSheetSourceSheetId ?? detail.currentGuideSheetBinding?.sourceSheetId ?? null,
        guideSheetReplaceRequested: false,
      };
      initialGuideSheetBinding.value = detail.currentGuideSheetBinding || null;
      loadCollaborationSettings(detail.lessonId).catch(() => {
        collaborationForm.value = { enabled: false, questionId: null, materialId: null };
      });
      selectedQuestions.value = (detail.questions || []).map((item, index) => ({
        ...item,
        questionScore: item.questionScore != null ? item.questionScore : 0,
        orderNum: item.orderNum != null ? item.orderNum : index + 1,
      }));
      sortQuestions(); // 加载详情后排序
      getQuestionList();
    });
  } else {
    isAddMode.value = true;
    const purpose = route.query.purpose || (route.query.lessonMode === 'attendance' ? 'attendance' : 'assessment');
    const initMode = purpose === 'attendance' || route.query.lessonMode === 'attendance' ? 'attendance' : 'assessment';
    const initialGrade = grade ? parseInt(grade, 10) : null;
    // purpose=guide：默认开启导学单；可从模板库带入 sheetId
    const enableGuide = hasPresetGuideSheet || purpose === 'guide';
    form.value = {
      lessonId: null,
      lessonTitle: null,
      grade: initialGrade,
      entryYear: entryYear ? String(entryYear) : calculateEntryYearFromGrade(initialGrade),
      semester: semester !== undefined ? String(semester) : getDefaultSemester(),
      lessonNum: route.query.nextNum ? parseInt(route.query.nextNum, 10) : 1,
      assignedClasses: [],
      initialTheoryOpen: true,
      initialPracticalOpen: true,
      shuffleMode: 0,
      randomChoiceCount: 0,
      randomJudgmentCount: 0,
      lessonMode: initMode,
      teacherNote: '',
      lessonTools: [],
      iotEnabled: false,
      autoAdvanceEnabled: false,
      autoAdvanceThresholdPct: 50,
      autoAdvanceDelayHours: 2,
      guideSheetEnabled: enableGuide,
      guideSheetSourceSheetId: hasPresetGuideSheet ? presetGuideSheetId : null,
      guideSheetReplaceRequested: false,
    };
    initialGuideSheetBinding.value = null;
    
    // 如果URL有预设班级 (e.g. ["1班"])，尝试设置
    if (classes) {
        try {
            const classList = JSON.parse(classes);
            if (Array.isArray(classList)) {
                form.value.assignedClasses = classList;
            }
        } catch (e) {
            console.error("解析classes参数失败", e);
        }
    }

    selectedQuestions.value = [];
    getQuestionList();
  }
}

function getDefaultSemester() {
  const now = new Date();
  const month = now.getMonth() + 1;
  if (month >= 2 && month <= 7) {
    return '1';
  } else {
    return '0';
  }
}

// 新增：统一判断题答案显示文本，兼容多种答案写法
// 新增：统一判断题答案显示文本，兼容多种答案写法
function formatJudgeAnswer(answer) {
  if (answer === null || answer === undefined || answer === '') {
    return '未配置';
  }
  const normalized = String(answer).trim().toLowerCase();
  const truthy = ['1', 'true', 't', 'y', 'yes', '对', '正确', 'right'];
  const falsy = ['0', 'false', 'f', 'n', 'no', '错', '错误', 'wrong'];
  if (truthy.includes(normalized)) {
    return '对';
  }
  if (falsy.includes(normalized)) {
    return '错';
  }
  return normalized || '未配置';
}
// 打字题时长推荐提示
function getTypingDurationHint(row) {
  const wordCount = row.wordCount || 0;
  if (wordCount === 0 || !row.typingDuration) return '';
  
  // 根据年级判断基准速度（小学20字/分，初高中40字/分）
  const grade = form.value.grade || 7;
  const baseSpeed = grade <= 6 ? 20 : 40;
  const recommendedMin = Math.ceil(wordCount / baseSpeed);
  const duration = row.typingDuration;
  
  if (duration === recommendedMin) {
    return '✓ 推荐时长';
  } else if (duration < recommendedMin) {
    return `⚠️ 时间较短，推荐 ${recommendedMin} 分钟，可能导致整体分数偏低`;
  } else {
    return `⚠️ 时间较长，推荐 ${recommendedMin} 分钟，可能导致整体分数偏高`;
  }
}

function stripHtml(html) {
  if (!html) return "";
  let tmp = document.createElement("DIV");
  tmp.innerHTML = html;
  return tmp.textContent || tmp.innerText || "";
}

function getQuestionList() {
  loading.value = true;
  // queryParams.value.grade = form.value.grade; // Removed to prevent auto-reset
  listQuestion(queryParams.value).then(async response => {
    const rows = response.rows || [];
    // 为操作题加载评分项
    for (const q of rows) {
      if (q.questionType === 'practical') {
        try {
          const res = await listScoringItems(null, q.questionId);
          q.scoringItems = res.data || [];
        } catch (e) {
          q.scoringItems = [];
        }
      }
    }
    questionBankList.value = rows;
    total.value = response.total;
    loading.value = false;
  });
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  getQuestionList();
}

function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

function isQuestionSelected(questionId) {
  return selectedQuestions.value.some(q => q.questionId === questionId);
}

function handleAddQuestion(row) {
    if (row.questionType === 'typing') {
        const hasTyping = selectedQuestions.value.some(q => q.questionType === 'typing');
        if (hasTyping) {
            proxy.$modal.msgError('一门课程最多只能添加一道打字题。');
            return;
        }
    }
    if (!isQuestionSelected(row.questionId)) {
        // 新增：携带选项、答案及附件信息，方便在已选列表和回显场景展示
        const newQuestion = {
            questionId: row.questionId,
            questionContent: row.questionContent,
            questionType: row.questionType,
            questionScore: 10,
            orderNum: selectedQuestions.value.length + 1,
            optionA: row.optionA,
            optionB: row.optionB,
            optionC: row.optionC,
            optionD: row.optionD,
            answer: row.answer,
            previewPath: row.previewPath,
            practicalMode: row.questionType === 'practical' ? (row.practicalMode || 'FILE') : null,
            typingDuration: row.typingDuration,
            wordCount: row.wordCount,
            scoringItems: row.scoringItems || [],
        };
        selectedQuestions.value.push(newQuestion);
        sortQuestions(); // 添加后自动排序
        proxy.$modal.msgSuccess("已添加");
    }
}

function handleRemoveQuestion(row) {
  const index = selectedQuestions.value.findIndex(q => q.questionId === row.questionId);
  if (index > -1) {
    selectedQuestions.value.splice(index, 1);
    if (Number(collaborationForm.value.questionId) === Number(row.questionId)) {
      collaborationForm.value = { enabled: false, questionId: null, materialId: null };
      proxy.$modal.msgWarning('协作所用文件作品题已移除，在线协作已关闭。');
    }
  }
}

function isFilePractical(row) {
  return row.questionType === 'practical' && (row.practicalMode || 'FILE') === 'FILE';
}

function practicalModeLabel(mode) {
  if (mode === 'PYTHON') return 'Python 在线编程';
  if (mode === 'FLOWCHART') return '画程流程图';
  return '文件作品';
}

function isCollaborationQuestion(row) {
  return Boolean(collaborationForm.value.enabled)
    && Number(collaborationForm.value.questionId) === Number(row.questionId);
}

// 本节课工具：添加 / 删除行
function addLessonTool() {
  form.value.lessonTools = form.value.lessonTools || [];
  form.value.lessonTools.push({ toolName: '', toolUrl: '' });
  lessonToolsExpanded.value = true;
}
function removeLessonTool(index) {
  form.value.lessonTools.splice(index, 1);
}

async function loadCollaborationSettings(lessonId) {
  if (!lessonId) return;
  const response = await getCollaborationLesson(lessonId);
  const payload = response.data || response;
  collaborationCandidates.value = payload.candidates || [];
  collaborationForm.value = {
    enabled: Boolean(payload.enabled),
    questionId: payload.questionId || null,
    materialId: payload.materialId || null
  };
}

async function handleCollaborationToggle(enabled) {
  if (!enabled) {
    collaborationForm.value = { enabled: false, questionId: null, materialId: null };
    collaborationCandidates.value = [];
    return;
  }
  if (!filePracticalQuestions.value.length) {
    proxy.$modal.msgWarning('请先添加一道“文件作品”操作题，再开启在线协作。');
    return;
  }

  const candidates = [];
  for (const row of filePracticalQuestions.value) {
    candidates.push(...await loadQuestionCollaborationCandidates(row, false));
  }
  collaborationCandidates.value = candidates;
  if (!candidates.length) {
    proxy.$modal.msgError('已选文件作品题没有可用于在线协作的 Word、Excel 或 PPT 起始文件。');
    return;
  }
  if (candidates.length === 1) {
    collaborationForm.value = {
      enabled: true,
      questionId: candidates[0].questionId,
      materialId: candidates[0].materialId
    };
    proxy.$modal.msgInfo('在线协作已加入本次保存，保存课程后即可使用。');
    return;
  }

  collaborationForm.value = { enabled: true, questionId: null, materialId: null };
  collaborationMaterialVisible.value = true;
}

async function toggleCollaboration(row) {
  if (isCollaborationQuestion(row)) {
    collaborationForm.value.enabled = false;
    return;
  }
  collaborationForm.value = { enabled: true, questionId: row.questionId, materialId: null };
  if (!form.value.lessonId) {
    const candidates = await loadQuestionCollaborationCandidates(row);
    if (!candidates.length) {
      collaborationForm.value.enabled = false;
      proxy.$modal.msgError('该文件作品没有可用于在线协作的 Word、Excel 或 PPT 起始文件。');
      return;
    }
    if (candidates.length === 1) {
      collaborationForm.value.materialId = candidates[0].materialId;
      proxy.$modal.msgInfo('保存课程后将按已指派班级自动创建协作房间。');
      return;
    }
    collaborationMaterialVisible.value = true;
    return;
  }
  const desired = { ...collaborationForm.value };
  await loadCollaborationSettings(form.value.lessonId);
  collaborationForm.value = desired;
  const candidates = collaborationCandidates.value.filter(item => Number(item.questionId) === Number(row.questionId));
  if (!candidates.length) {
    collaborationForm.value.enabled = false;
    proxy.$modal.msgError('该文件作品没有可用于在线协作的 Word、Excel 或 PPT 起始文件。');
    return;
  }
  if (candidates.length === 1) {
    collaborationForm.value.materialId = candidates[0].materialId;
    return;
  }
  collaborationCandidates.value = candidates;
  collaborationMaterialVisible.value = true;
}

async function loadQuestionCollaborationCandidates(row, updateStore = true) {
  try {
    const response = await getQuestion(row.questionId);
    const detail = response.data || {};
    const editableExtensions = new Set(['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx']);
    const candidates = (detail.practicalMaterials || [])
      .filter(item => String(item.materialType || '').toUpperCase() === 'STARTER')
      .filter(item => {
        const name = item.originalFileName || item.resourcePath || '';
        return editableExtensions.has(name.split('.').pop()?.toLowerCase());
      })
      .map(item => ({
        questionId: Number(row.questionId),
        materialId: item.materialId,
        fileName: item.originalFileName || String(item.resourcePath || '').split('/').pop(),
        questionTitle: stripHtml(row.questionContent).trim().slice(0, 36) || `题目 ${row.questionId}`
      }));
    if (updateStore) collaborationCandidates.value = candidates;
    return candidates;
  } catch (error) {
    if (updateStore) collaborationCandidates.value = [];
    return [];
  }
}

function confirmEnableCollaboration() {
  if (!collaborationForm.value.materialId) {
    proxy.$modal.msgWarning('请选择一份起始文件。');
    return;
  }
  const selected = collaborationCandidates.value.find(item => Number(item.materialId) === Number(collaborationForm.value.materialId));
  if (!selected) {
    proxy.$modal.msgWarning('所选起始文件已不可用，请重新选择。');
    return;
  }
  collaborationForm.value = {
    enabled: true,
    questionId: selected.questionId,
    materialId: selected.materialId
  };
  collaborationMaterialVisible.value = false;
}

function cancelCollaborationMaterial() {
  collaborationMaterialVisible.value = false;
  collaborationForm.value = { enabled: false, questionId: null, materialId: null };
}

async function synchronizeCollaboration(lessonId) {
  if (!lessonId) return;
  if (!collaborationForm.value.enabled) {
    if (form.value.lessonId) await saveCollaborationLesson(lessonId, { enabled: false });
    return;
  }
  const desired = { ...collaborationForm.value };
  await loadCollaborationSettings(lessonId);
  collaborationForm.value = desired;
  const candidates = collaborationCandidates.value.filter(item => Number(item.questionId) === Number(collaborationForm.value.questionId));
  if (!candidates.length) throw new Error('所选文件作品没有可用于在线协作的起始文件');
  if (!collaborationForm.value.materialId) {
    if (candidates.length > 1) throw new Error('该操作题有多份起始文件，请先选择用于在线协作的文件');
    collaborationForm.value.materialId = candidates[0].materialId;
  }
  await saveCollaborationLesson(lessonId, {
    enabled: true,
    questionId: collaborationForm.value.questionId,
    materialId: collaborationForm.value.materialId
  });
}

function handlePreviewFile(row) {
  if (pdfPreviewRef.value && row.previewPath) {
    const baseUrl = import.meta.env.VITE_APP_BASE_API;
    // iframe 无法附加 Authorization，统一由授权资源接口读取预览文件。
    const fullPdfUrl = `${baseUrl}/common/resource/view?resource=${encodeURIComponent(row.previewPath)}`;
    pdfPreviewRef.value.open(fullPdfUrl);
  } else {
    proxy.$modal.msgError("没有可预览的PDF文件。");
  }
}

async function openPythonPreview(row) {
  const response = await previewProgrammingQuestion(Number(row.questionId));
  pythonPreviewQuestion.value = row;
  pythonPreviewConfig.value = response.data || {};
  pythonPreviewCases.value = response.testCases || [];
  pythonPreviewVisible.value = true;
}



// 批量设置分数
const batchScoreType = ref('choice');
const batchScoreValue = ref(5);

const choiceCount = computed(() => selectedQuestions.value.filter(q => q.questionType === 'choice').length);
const judgmentCount = computed(() => selectedQuestions.value.filter(q => q.questionType === 'judgment').length);
const hasTheorySelected = computed(() => choiceCount.value > 0 || judgmentCount.value > 0);
const hasPracticalSelected = computed(() => selectedQuestions.value.some(q => q.questionType === 'practical'));

function applyBatchScore() {
  if (!batchScoreType.value) {
    proxy.$modal.msgWarning("请先选择要批量设置的题型");
    return;
  }
  
  let count = 0;
  selectedQuestions.value.forEach(q => {
    if (q.questionType === batchScoreType.value) {
      q.questionScore = batchScoreValue.value;
      count++;
    }
  });
  
  if (count > 0) {
    proxy.$modal.msgSuccess(`已批量更新 ${count} 道${batchScoreType.value === 'choice' ? '选择题' : '判断题'}的分数`);
  } else {
    proxy.$modal.msgInfo(`当前已选列表中没有${batchScoreType.value === 'choice' ? '选择题' : '判断题'}`);
  }
}

// 加载教师管理的班级
function loadMyManagedClasses() {
  getMyClasses().then(response => {
    myManagedClasses.value = response.data || [];
  });
}

onMounted(() => {
  initialize();
});
</script>


<style scoped>

.resource-tabs :deep(.el-tabs__header) {
  margin-bottom: 18px;
}

.resource-tabs :deep(.el-tabs__item) {
  height: 44px;
  padding: 0 24px;
  font-weight: 650;
}

.footer-toolbar {
  position: fixed;
  bottom: 0;
  right: 0;
  width: 100%;
  height: 56px;
  line-height: 56px;
  padding: 0 24px;
  background: #fff;
  border-top: 1px solid #e8e8e8;
  box-shadow: 0 -1px 2px rgba(0, 0, 0, 0.03);
  text-align: right;
  z-index: 9;
}
.options-list {
  margin-top: 8px;
  padding-left: 15px;
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}
.options-list p {
  margin: 4px 0;
  white-space: normal;
  word-break: break-all;
}
.question-content-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.typing-info {
  margin-top: 8px;
  font-size: 13px;
  color: #909399;
}

.judge-info {
  margin-top: 8px;
  font-size: 13px;
  color: #606266;
}
.correct-answer {
  color: #67C23A;
  font-weight: bold;
  margin-top: 5px;
}
.scoring-info {
  margin-top: 8px;
  font-size: 13px;
  color: #606266;
}
.scoring-label {
  color: #409EFF;
  font-weight: bold;
}
.scoring-item {
  color: #606266;
}
.no-scoring {
  color: #909399;
  font-style: italic;
}
.app-container {
  /* 固定底栏 56px + 资源表「添加」按钮行高余量，1920 下避免底栏遮挡操作 */
  padding-bottom: 120px;
}

/* 资源列表底部预留，防止最后一行操作按钮被 fixed 工具栏遮住 */
.resource-tabs {
  padding-bottom: 24px;
}

.python-preview-title { margin: 0 0 8px; }
.python-preview-meta { color: #909399; margin-bottom: 16px; }
.python-preview-code { white-space: pre-wrap; background: #f6f8fa; padding: 10px; margin: 0; }
.collaboration-material-tip { color: #606266; line-height: 1.6; }
.collaboration-material-list { display: flex; flex-direction: column; gap: 12px; }
.designer-grid > :deep(.el-col) { margin-bottom: 20px; }
.form-help-dot {
  display: inline-grid;
  place-items: center;
  width: 18px;
  height: 18px;
  margin-left: 7px;
  border: 1px solid #a8b3bd;
  border-radius: 50%;
  color: #7b8792;
  font-size: 12px;
  line-height: 1;
  cursor: help;
}
.feature-settings,
.initial-gate-panel {
  width: 100%;
  overflow: hidden;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #fff;
}
.feature-row,
.initial-gate-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-height: 44px;
  padding: 7px 12px;
}
.feature-row + .feature-row,
.initial-gate-row + .initial-gate-row { border-top: 1px solid #ebeef5; }
.feature-label { display: flex; align-items: center; min-width: 0; gap: 4px; }
.feature-status { margin-left: 8px; color: #909399; font-size: 12px; font-weight: 400; }
.compact-setting-heading { display: flex; align-items: center; justify-content: space-between; min-height: 32px; color: #606266; }
.compact-setting-heading > div { display: flex; align-items: center; gap: 4px; }
.selected-question-tools { display: flex; justify-content: flex-end; margin: -4px 0 8px; }
.batch-popover { display: flex; align-items: center; gap: 8px; }
.answer-mode-text { margin-top: 4px; color: #909399; font-size: 12px; }

@media (max-width: 768px) {
  .resource-tabs { overflow-x: auto; }
}

.lesson-tool-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

@media (max-width: 1199px) {
  .designer-grid > :deep(.el-col) { margin-bottom: 16px; }
}
</style>










