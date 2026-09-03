
<template>
  <div class="student-dashboard">
    <!-- 顶部导航栏 -->
    <header class="dashboard-header">
      <div class="dashboard-header__inner">
        <div class="header-left">
          <img src="@/assets/logo/logo.png" class="logo" alt="Logo" />
          <span class="platform-name">智慧课堂 - 学生端</span>
          <div v-if="hasGuideSheet" class="view-toggle" role="tablist" aria-label="课程内容切换">
            <el-button
              size="small"
              :type="activeLearningMode === 'daily' ? 'primary' : 'default'"
              :plain="activeLearningMode !== 'daily'"
              @click="switchToDailyCourse"
            >日常课程题目</el-button>
            <el-button
              size="small"
              :type="activeLearningMode === 'guide' ? 'primary' : 'default'"
              :plain="activeLearningMode !== 'guide'"
              @click="switchToGuideSheet"
            >电子导学单</el-button>
          </div>
        </div>
        <div class="header-right">
          <div class="header-actions">
            <el-button
              v-if="iotEnabled"
              type="success"
              link
              icon="Cpu"
              @click="$router.push({ path: '/student/iot', query: { lessonId } })"
              >物联实验</el-button
            >
            <el-button
              type="warning"
              link
              icon="Cpu"
              @click="$router.push('/student/python-practice')"
              >Python 练习</el-button
            >
            <el-button
              type="info"
              link
              icon="Link"
              @click="studentToolVisible = true"
              >学生实验工具</el-button
            >
            <el-button
              type="primary"
              link
              icon="Timer"
              @click="handleCommand('history')"
              >历史成绩</el-button
            >
            <el-button
              type="danger"
              link
              icon="Edit"
              @click="handleCommand('wrong_book')"
              >我的错题</el-button
            >
          </div>
          <el-divider direction="vertical" class="header-divider" />
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="36" shape="circle" icon="UserFilled" />
              <span class="user-name">{{
                studentInfo.studentName || "同学"
              }}</span>
              <el-icon><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout"
                  >退出登录</el-dropdown-item
                >
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <!-- 学生实验工具面板：先本节课，后常驻；点击新标签页打开 -->
    <el-dialog
      v-model="studentToolVisible"
      title="学生实验工具"
      width="560px"
      append-to-body
      destroy-on-close
      class="student-tool-dialog"
    >
      <div v-if="!hasToolList" class="student-tool-empty">
        <el-empty description="当前没有可用的实验工具" :image-size="80" />
      </div>
      <template v-else>
        <div v-if="lessonTools.length" class="tool-group">
          <div class="tool-group-title">
            <el-icon><Collection /></el-icon> 本节课工具
          </div>
          <div class="tool-grid">
            <div v-for="tool in lessonTools" :key="'l' + tool.toolId" class="tool-item">
              <el-link type="primary" :href="tool.toolUrl" target="_blank" rel="noopener noreferrer">
                <el-icon><Link /></el-icon>{{ tool.toolName }}
              </el-link>
            </div>
          </div>
        </div>
        <div v-if="residentTools.length" class="tool-group">
          <div class="tool-group-title">
            <el-icon><Star /></el-icon> 常驻工具
          </div>
          <div class="tool-grid">
            <div v-for="tool in residentTools" :key="'r' + tool.toolId" class="tool-item">
              <el-link type="primary" :href="tool.toolUrl" target="_blank" rel="noopener noreferrer">
                <el-icon><Link /></el-icon>{{ tool.toolName }}
              </el-link>
            </div>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 加载中 -->
    <div v-if="loading && activeLearningMode === 'daily'" class="loading-container">
      <el-icon class="is-loading"><Loading /></el-icon>
      <p>正在加载课程内容...</p>
    </div>

    <el-result
      v-else-if="accessCheckFailed"
      icon="warning"
      title="暂时无法进入课程"
      sub-title="系统未能确认区域抽测状态，请检查网络后重试。为保证抽测优先级，当前不会加载日常课程。"
      class="access-error-state"
    >
      <template #extra>
        <el-button type="primary" :loading="loading" @click="fetchData">重新检查</el-button>
      </template>
    </el-result>

    <!-- 主内容区 -->
    <main v-else v-show="activeLearningMode === 'daily'" class="main-content">
      <!-- 课程信息Banner -->
      <div class="lesson-banner" v-if="hasLesson">
        <div class="banner-content">
          <div class="banner-left">
            <h1>{{ lessonTitle }}</h1>
            <p>
              <el-tag type="info" effect="dark">{{
                studentInfo.deptName
              }}</el-tag>
              <el-tag type="success" effect="dark"
                >{{ studentClassLabel }}</el-tag
              >
              <el-tag type="warning" effect="dark">{{
                studentInfo.studentName || "同学"
              }}</el-tag>
            </p>
          </div>
          <div class="banner-right">
            <div class="course-score-box">
              <div class="score-label">课程总分</div>
              <div class="score-value total score-num">{{ courseTotalScore }}</div>
            </div>
            <div class="score-divider"></div>
            <div class="course-score-box">
              <div class="score-label">我的得分</div>
              <div
                class="score-value my score-num"
                :class="{ pending: courseMyScore === null }"
              >
                {{ courseMyScore !== null ? courseMyScore : "待完成" }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 无课程提示 -->
      <el-empty v-if="!hasLesson" description="暂无课程，请休息一下吧~" />

      <!-- 课堂考勤：展示课程名、签到状态、教师说明；不计作业分 -->
      <div v-else-if="lessonMode === 'attendance'" class="task-container attendance-panel">
        <el-card shadow="never" class="attendance-card">
          <div class="attendance-header">
            <el-tag type="warning" effect="dark">课堂考勤</el-tag>
            <h2 class="attendance-title">{{ lessonTitle }}</h2>
          </div>
          <p v-if="teacherNote" class="attendance-note">{{ teacherNote }}</p>
          <p v-else class="attendance-note muted">教师暂无额外说明，请完成签到即可。</p>
          <div class="attendance-status">
            <template v-if="checkedIn">
              <el-result icon="success" title="已签到" :sub-title="checkinTimeText">
              </el-result>
            </template>
            <template v-else>
              <el-button type="primary" size="large" :loading="checkinLoading" @click="handleStudentCheckin">
                立即签到
              </el-button>
              <p class="attendance-hint">签到不计作业分，不计入作业均分。</p>
            </template>
          </div>
        </el-card>
      </div>

      <div v-else class="task-container">
        <!-- 题目未开放提示：课程有题但老师未在课堂开启 -->
        <el-alert v-if="!theoryOpen && hasTheory && !practicalOpen && hasPractical" type="info" :closable="false" show-icon class="gate-tip"
          title="本课理论测试题与操作题暂未开放，请等老师在课堂开启后作答" />
        <el-alert v-else-if="!theoryOpen && hasTheory" type="info" :closable="false" show-icon class="gate-tip"
          title="本课理论测试题暂未开放，请等老师在课堂开启后作答" />
        <el-alert v-else-if="!practicalOpen && hasPractical" type="info" :closable="false" show-icon class="gate-tip"
          title="本课操作题暂未开放，请等老师在课堂开启后作答" />
        <el-card v-if="collaborationRooms.length" shadow="never" class="collaboration-card">
          <template #header><div class="card-header"><span>班级在线协作</span><el-tag type="success">同班共享</el-tag></div></template>
          <div v-for="room in collaborationRooms" :key="room.roomId" class="collaboration-room-row">
            <div><strong>{{ room.roomTitle }}</strong><span class="collaboration-meta">{{ room.fileName }} · 第 {{ room.version }} 版</span></div>
            <el-button type="primary" @click="openCollaboration(room)">进入房间</el-button>
          </div>
        </el-card>
        <!-- 空状态提示 -->
        <el-empty 
          v-if="typingQuestions.length === 0 && theoryQuestions.length === 0 && practicalQuestions.length === 0"
          description="本课程暂无练习题目" 
        />
        <!-- 1. 打字练习区域 -->
        <div v-if="typingQuestions.length > 0" class="section-block">
          <div class="section-title">
            <el-icon><Monitor /></el-icon> 打字练习
            <span class="section-score-info">
              总分: <span class="score-num">{{ typingTotalScore }}</span>分
              <template v-if="typingMyScore !== null">
                | 得分:
                <span class="section-score-value score-num">{{ typingMyScore }}分</span>
              </template>
            </span>
          </div>

          <div
            v-for="q in typingQuestions"
            :key="q.questionId"
            class="typing-panel"
          >
            <!-- 左侧数据栏 -->
            <div class="typing-stats">
              <!-- <div class="stat-line"></div> 移除旧的分数显示和分割线 -->

              <div class="stat-item time">
                <label>剩余时间</label>
                <span
                  :class="{
                    warning: typingStates[q.questionId]?.timeLeft < 60,
                  }"
                >
                  {{ formatTime(typingStates[q.questionId]?.timeLeft || 0) }}
                </span>
              </div>
              <div class="stat-item">
                <label>总字数</label>
                <span>{{ q.wordCount || q.questionContent?.length || 0 }}</span>
              </div>
              <div class="stat-item">
                <label>完成字数</label>
                <span>{{
                  typingStates[q.questionId]?.completedCount || 0
                }}</span>
              </div>
              <div class="stat-item">
                <label>错误字数</label>
                <span class="error-text">{{
                  typingStates[q.questionId]?.errorCount || 0
                }}</span>
              </div>
              <div class="stat-item">
                <label>正确字数</label>
                <span class="success-text">{{
                  typingStates[q.questionId]?.correctCount || 0
                }}</span>
              </div>
              <div class="stat-item">
                <label>正确率</label>
                <span>{{ typingStates[q.questionId]?.accuracy || 100 }}%</span>
              </div>
              <div class="stat-item highlight">
                <label>打字速度</label>
                <span>{{ typingStates[q.questionId]?.speed || 0 }} 字/分</span>
              </div>
              <div class="stat-item">
                <label>完成率</label>
                <span>{{ typingStates[q.questionId]?.progress || 0 }}%</span>
              </div>

              <div class="action-buttons">
                <!-- 开始/重新打字按钮 -->
                <el-button
                  v-if="!typingStates[q.questionId]?.started"
                  type="success"
                  class="action-btn"
                  @click="startTypingPractice(q.questionId)"
                >
                  开始练习
                </el-button>

                <el-button
                  v-else-if="typingStates[q.questionId]?.submitted"
                  type="warning"
                  class="action-btn"
                  @click="restartTyping(q.questionId)"
                >
                  重新打字
                </el-button>

                <!-- 提交按钮 -->
                <el-button
                  type="primary"
                  class="action-btn"
                  @click="submitTyping(q)"
                  :disabled="
                    !typingStates[q.questionId]?.started ||
                    typingStates[q.questionId]?.submitted ||
                    typingStates[q.questionId]?.submitting
                  "
                >
                  {{ typingStates[q.questionId]?.submitting ? "提交中..." : "提交打字成绩" }}
                </el-button>
              </div>
            </div>

            <!-- 右侧练习区 -->
            <div class="typing-area">
              <!-- 未开始提示 -->
              <div
                class="typing-status-bar"
                v-if="!typingStates[q.questionId]?.started"
              >
                <el-alert
                  title="请点击左侧「开始练习」按钮开始计时"
                  type="info"
                  :closable="false"
                  center
                  show-icon
                />
              </div>

              <!-- 已提交提示 -->
              <div
                class="typing-status-bar"
                v-else-if="typingStates[q.questionId]?.submitted"
              >
                <el-alert
                  :title="`成绩已提交！得分: ${
                    typingStates[q.questionId]?.myScore || 0
                  }分`"
                  type="success"
                  :closable="false"
                  center
                  show-icon
                />
              </div>

              <!-- 原文展示区 -->
              <div class="original-text-box">
                <div class="box-label">文章段落：</div>
                <div 
                  class="text-content"
                  @copy.prevent 
                  @paste.prevent 
                  @cut.prevent 
                  @dragstart.prevent 
                  @contextmenu.prevent
                >
                  <span
                    v-for="(char, idx) in q.questionContent || ''"
                    :key="idx"
                    :class="getCharClass(q.questionId, idx)"
                    >{{ char }}</span
                  >
                </div>
              </div>

              <!-- 输入框 -->
              <div class="input-box">
                <label>输入框：</label>
                <el-input
                  :ref="
                    (el) => {
                      if (el) inputRefs[q.questionId] = el;
                    }
                  "
                  v-model="answers[q.questionId]"
                  type="textarea"
                  :rows="6"
                  placeholder="在此输入上方文字...（禁止复制粘贴）"
                  resize="none"
                  class="typing-input"
                  @input="handleTypingInput(q.questionId, $event)"
                  @paste.prevent="handlePasteBlock"
                  @copy.prevent
                  @cut.prevent
                  @contextmenu.prevent
                  :disabled="
                    !typingStates[q.questionId]?.started ||
                    typingStates[q.questionId]?.submitted
                  "
                />
              </div>
            </div>
          </div>
        </div>

        <!-- 2. 理论测试区域 -->
        <div v-if="theoryQuestions.length > 0" class="section-block">
          <div class="section-title">
            <el-icon><EditPen /></el-icon> 理论测试
            <span class="section-score-info">
              总分: <span class="score-num">{{ theoryTotalScore }}</span>分
              <template v-if="theorySubmitted">
                | 得分:
                <span class="section-score-value score-num">{{ theoryScore }}分</span>
              </template>
            </span>
          </div>
          <div class="theory-grid">
            <el-card
              v-for="(q, index) in theoryQuestions"
              :key="q.questionId"
              class="theory-card"
              shadow="hover"
            >
              <template #header>
                <div class="card-header">
                  <span class="badge">{{
                    getQuestionTypeLabel(q.questionType)
                  }}</span>
                  <span class="header-right-info">
                    <template v-if="theorySubmitted && q.answer">
                      <span v-if="isQuestionAnswerCorrect(q, answers[q.questionId])" class="result-tag correct">
                        <el-icon><Check /></el-icon> 正确
                      </span>
                      <span v-else class="result-tag wrong">
                        <el-icon><Close /></el-icon> 错误
                      </span>
                    </template>
                    <span class="score">{{ q.questionScore }}分</span>
                  </span>
                </div>
              </template>

              <div class="question-stem">
                {{ index + 1 }}. {{ q.questionContent }}
              </div>

              <!-- 选择题 -->
              <div v-if="q.questionType === 'choice'" class="options-group">
                <div
                  v-for="opt in ['A', 'B', 'C', 'D']"
                  :key="opt"
                  class="option-radio"
                  :class="{ active: answers[q.questionId] === opt }"
                  @click="selectTheoryAnswer(q.questionId, opt)"
                >
                  <span class="opt-label">{{ opt }}</span>
                  <span class="opt-text">{{
                    q["option" + opt] || "未配置"
                  }}</span>
                </div>
              </div>

              <!-- 判断题 -->
              <div
                v-else-if="q.questionType === 'judgment'"
                class="audit-group"
              >
                <el-radio-group v-model="answers[q.questionId]" :disabled="theorySubmitted" @change="markQuestionWorking(q.questionId)">
                  <el-radio value="T" border>正确</el-radio>
                  <el-radio value="F" border>错误</el-radio>
                </el-radio-group>
              </div>
            </el-card>
          </div>
          <div class="submit-theory-bar">
            <el-button
              type="success"
              size="large"
              @click="submitTheory"
              :disabled="theorySubmitted"
            >
              {{ theorySubmitted ? "已提交" : "提交理论测试" }}
            </el-button>
          </div>
        </div>

        <!-- 3. 操作题区域 -->
        <div v-if="practicalQuestions.length > 0" class="section-block">
          <div class="section-title">
            <el-icon><FolderOpened /></el-icon> 操作题
            <span class="section-score-info" v-if="practicalQuestions.length > 1">
              总分: <span class="score-num">{{ practicalTotalScore }}</span>分
              <template v-if="practicalMyScore !== null">
                | 得分:
                <span class="section-score-value score-num"
                  >{{ practicalMyScore }}分</span
                >
              </template>
            </span>
          </div>
          <div class="practical-list">
            <el-card
              v-for="(q, index) in filePracticalQuestions"
              :key="q.questionId"
              class="practical-card"
              shadow="hover"
            >
              <template #header>
                <div class="card-header">
                  <span class="badge">操作题</span>
                  <span class="score-status">
                    <!-- 已提交且已批阅：显示分数 -->
                    <template
                      v-if="
                        practicalUploads[q.questionId] &&
                        practicalScores[q.questionId] !== null &&
                        practicalScores[q.questionId] !== undefined
                      "
                    >
                      <span class="scored score-num"
                        >{{ practicalScores[q.questionId] }}/{{
                          q.questionScore
                        }}分</span
                      >
                    </template>
                    <!-- 已提交但未批阅 -->
                    <template v-else-if="practicalUploads[q.questionId]">
                      <span class="pending">待批阅</span>
                    </template>
                    <!-- 未提交 -->
                    <template v-else>
                      <span class="not-submitted">{{ q.questionScore }}分</span>
                    </template>
                  </span>
                </div>
              </template>

              <!-- 题目描述 -->
              <div class="question-stem">
                <span v-if="filePracticalQuestions.length > 1">{{ index + 1 }}. </span>
                {{ q.questionContent }}
              </div>

              <!-- 评分标准展示 -->
              <div v-if="q.scoringItems && q.scoringItems.length > 0" class="scoring-standards" style="margin: 10px 0; padding: 10px; background: #fdf6ec; border-radius: 4px;">
                 <div style="font-weight: bold; color: #e6a23c; margin-bottom: 5px; font-size: 13px;">评分标准：</div>
                 <div v-for="(item, idx) in q.scoringItems" :key="item?.itemId || idx" style="font-size: 13px; color: #606266; line-height: 1.6;">
                    <template v-if="item">• {{ item.itemName }} <span style="color: #909399">({{ item.itemScore }}%)</span></template>
                 </div>
              </div>

              <!-- 素材文件下载 -->
              <div v-if="getStudentMaterials(q).length" class="material-section">
                <span class="material-label">素材文件：</span>
                <div class="material-files">
                  <div v-for="(material, materialIndex) in getStudentMaterials(q)" :key="material.materialId || materialIndex" class="material-file-row">
                    <span class="material-name">{{ material.originalFileName || getFileName(material.resourcePath) }}</span>
                    <el-button type="primary" size="small" icon="Download" @click="downloadMaterial(material.resourcePath, material.originalFileName)">下载</el-button>
                  </div>
                </div>
              </div>

              <!-- 作品上传区域 -->
              <div class="upload-section">
                <span class="upload-label">提交作品：</span>
                <el-upload
                  class="work-uploader"
                  :action="uploadUrl"
                  :data="{ lessonId, questionId: q.questionId }"
                  :headers="uploadHeaders"
                  :multiple="true"
                  :limit="getPracticalUploadLimit(q)"
                  :before-upload="(file) => beforePracticalUpload(q, file)"
                  :on-success="(res, file) => handleUploadSuccess(q.questionId, res, file)"
                  :on-error="(error, file) => handleUploadError(q.questionId, error, file)"
                  :on-exceed="() => handleUploadExceed(q)"
                  :show-file-list="false"
                  :accept="getPracticalAccept(q)"
                >
                  <el-button type="primary" icon="Upload" :loading="submittingPracticalQuestionId === q.questionId">
                    {{ practicalUploads[q.questionId] ? "选择并提交新版本" : "选择并提交作品" }}
                  </el-button>
                </el-upload>

                <div v-if="getPracticalDrafts(q.questionId).length" class="practical-draft-list">
                  <div v-for="(draft, draftIndex) in getPracticalDrafts(q.questionId)" :key="draft.uploadToken" class="draft-file">
                    <span>{{ draftIndex + 1 }}. {{ draft.originalFileName }}</span>
                    <el-button link type="danger" @click="removePracticalDraft(q.questionId, draftIndex)">移除</el-button>
                  </div>
                  <span class="upload-tip">文件上传完成后会自动提交。Office/PDF 仅 1 个；图片可按顺序提交 1～{{ getPracticalUploadLimit(q) }} 张</span>
                </div>

                <!-- 新版本已提交，预览转换由服务器异步完成 -->
                <div
                  v-else-if="uploadingQuestionId === q.questionId"
                  class="uploading-status"
                >
                  <el-icon class="is-loading"><Loading /></el-icon>
                  <span>作品已上传，等待服务器转换...</span>
                </div>

                <!-- 已上传文件展示 -->
                <div v-else-if="practicalUploads[q.questionId]" class="uploaded-file">
                  <el-icon><Document /></el-icon>
                  <div class="uploaded-meta">
                    <span class="file-name">{{
                      getPracticalDisplayName(q.questionId)
                    }}</span>
                    <span
                      class="preview-state"
                      :class="getPracticalPreviewClass(q.questionId)"
                    >
                      {{ getPracticalPreviewLabel(q.questionId) }}
                    </span>
                  </div>
                  <el-button-group v-if="getPracticalAttachments(q.questionId).length <= 1">
                    <el-button
                      type="primary"
                      size="small"
                      icon="View"
                      @click="previewWork(q.questionId)"
                      :disabled="!canPreviewPractical(q.questionId)"
                      >预览</el-button
                    >
                    <el-button
                      type="info"
                      size="small"
                      icon="Download"
                      @click="downloadSubmittedWork(q.questionId)"
                      >下载</el-button
                    >
                    <el-button
                      type="danger"
                      size="small"
                      icon="Delete"
                      @click="deleteWork(q.questionId)"
                      >删除</el-button
                    >
                  </el-button-group>
                </div>
                <div v-if="getPracticalAttachments(q.questionId).length > 1" class="practical-attachment-list">
                  <div v-for="(attachment, attachmentIndex) in getPracticalAttachments(q.questionId)" :key="attachment.attachmentId || attachmentIndex" class="attachment-row">
                    <span>{{ attachmentIndex + 1 }}. {{ attachment.originalFileName || getFileName(attachment.resourcePath) }}</span>
                    <el-button
                      link
                      type="primary"
                      :disabled="!canPreviewPractical(q.questionId, attachmentIndex)"
                      @click="previewWork(q.questionId, attachmentIndex)"
                    >预览</el-button>
                    <el-button link type="info" @click="downloadSubmittedWork(q.questionId, attachmentIndex)">下载</el-button>
                  </div>
                </div>
              </div>
            </el-card>
          </div>
          <div v-if="flowchartPracticalQuestions.length" class="flowchart-question-list">
            <el-card v-for="(q, index) in flowchartPracticalQuestions" :key="q.questionId" class="practical-card flowchart-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span class="badge flowchart-badge">画程流程图</span>
                  <span class="score-status">
                    <span v-if="practicalScores[q.questionId] != null" class="scored score-num">{{ practicalScores[q.questionId] }}/{{ q.questionScore }}分</span>
                    <span v-else-if="practicalUploads[q.questionId]" class="pending">已提交 · 待批阅</span>
                    <span v-else class="not-submitted">{{ q.questionScore }}分</span>
                  </span>
                </div>
              </template>
              <div class="question-stem"><span v-if="flowchartPracticalQuestions.length > 1">{{ index + 1 }}. </span>{{ q.questionContent }}</div>
              <div class="flowchart-entry">
                <div>
                  <strong>在平台内直接完成</strong>
                  <p>拖动图形、连接箭头，系统会自动保存草稿；完成后请点击“完成并提交”。</p>
                </div>
                <el-button type="primary" size="large" @click="openFlowchart(q)">
                  {{ practicalUploads[q.questionId] ? '查看画程作品' : '打开画程开始作答' }}
                </el-button>
              </div>
            </el-card>
          </div>
          <student-programming-question v-for="q in pythonPracticalQuestions" :key="q.questionId" :lesson-id="lessonId" :question="q" @completed="fetchData" />
        </div>
      </div>
    </main>

    <student-guide-sheet
      v-if="hasGuideSheet && activeLearningMode === 'guide'"
      ref="guideSheetRef"
      embedded
      :expected-binding-id="guideSheetBindingId"
      @switch-mode="activeLearningMode = 'daily'"
    />

    <!-- 修改密码弹窗 -->
    <el-dialog
      v-model="pwdDialogVisible"
      title="修改密码"
      width="400px"
      append-to-body
    >
      <el-form
        ref="pwdFormRef"
        :model="pwdForm"
        :rules="pwdRules"
        label-width="80px"
      >
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input
            v-model="pwdForm.oldPassword"
            type="password"
            placeholder="请输入旧密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="pwdForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="pwdForm.confirmPassword"
            type="password"
            placeholder="请确认新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="pwdDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitPwd">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 历史成绩弹窗 -->
    <el-dialog v-model="historyDialogVisible" title="历史成绩单" width="800px">
      <div class="history-header">
        <el-select
          v-model="historyYear"
          placeholder="选择年份"
          @change="loadHistoryScores"
          style="width: 120px"
        >
          <el-option
            v-for="y in yearOptions"
            :key="y"
            :label="y + '年'"
            :value="y"
          />
        </el-select>
      </div>
      <el-table
        :data="historyList"
        v-loading="historyLoading"
        style="width: 100%"
      >
        <el-table-column prop="lessonTitle" label="课程名称" min-width="180" />
        <el-table-column label="总分/得分" width="120" align="center">
          <template #default="{ row }">
            <span
              :class="{ 'score-success': row.myScore >= row.totalScore * 0.6 }"
            >
              {{ row.myScore }}/{{ row.totalScore }}
            </span>
          </template>
        </el-table-column>
        <el-table-column
          prop="typingScore"
          label="打字"
          width="80"
          align="center"
        />
        <el-table-column
          prop="theoryScore"
          label="理论"
          width="80"
          align="center"
        />
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <span>{{ row.practicalScore }}</span>
            <small v-if="row.filePracticalScore || row.pythonPracticalScore || row.flowchartPracticalScore" class="practical-score-detail">
              文件 {{ row.filePracticalScore || '0/0' }} · Python {{ row.pythonPracticalScore || '0/0' }} · 画程 {{ row.flowchartPracticalScore || '0/0' }}
            </small>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.submitTime) }}
          </template>
        </el-table-column>
      </el-table>
      <el-empty
        v-if="!historyLoading && historyList.length === 0"
        description="暂无成绩记录"
      />
    </el-dialog>

    <!-- 错题本弹窗 -->
    <el-dialog
      v-model="wrongDialogVisible"
      title="我的错题本"
      width="800px"
      append-to-body
    >
      <div class="wrong-book-header">
        <el-select
          v-model="selectedWrongLessonId"
          placeholder="选择课程"
          @change="loadWrongQuestions"
          style="width: 240px"
        >
          <el-option
            v-for="opt in wrongLessonOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
        <span class="wrong-stats" v-if="wrongList.length"
          >共 {{ wrongList.length }} 道错题</span
        >
      </div>

      <el-scrollbar height="60vh">
        <div
          v-if="wrongLoading"
          class="loading-wrapper"
          style="padding: 20px; text-align: center"
        >
          <el-icon class="is-loading"><Loading /></el-icon> 加载中...
        </div>
        <div v-else-if="wrongList.length === 0" class="empty-wrapper">
          <el-empty description="太棒了，没有错题！" />
        </div>
        <div v-else class="wrong-list">
          <el-card
            v-for="(q, idx) in wrongList"
            :key="q.questionId"
            class="wrong-card"
            shadow="hover"
          >
            <template #header>
              <div class="card-header">
                <span class="badge">{{
                  getQuestionTypeLabel(q.questionType)
                }}</span>
                <span class="score-info">本题 {{ q.questionScore }} 分</span>
              </div>
            </template>

            <div class="q-content">{{ idx + 1 }}. {{ q.questionContent }}</div>

            <!-- 可交互选项 -->
            <div v-if="q.questionType === 'choice'" class="options-interactive">
              <div
                class="opt-btn"
                :class="{
                  selected: wrongAnswers[q.questionId] === 'A',
                  'correct-result':
                    wrongSubmitted[q.questionId] && q.answer === 'A',
                  'wrong-result':
                    wrongSubmitted[q.questionId] &&
                    wrongAnswers[q.questionId] === 'A' &&
                    q.answer !== 'A',
                }"
                @click="selectWrongAnswer(q.questionId, 'A')"
              >
                A. {{ q.optionA }}
              </div>
              <div
                class="opt-btn"
                :class="{
                  selected: wrongAnswers[q.questionId] === 'B',
                  'correct-result':
                    wrongSubmitted[q.questionId] && q.answer === 'B',
                  'wrong-result':
                    wrongSubmitted[q.questionId] &&
                    wrongAnswers[q.questionId] === 'B' &&
                    q.answer !== 'B',
                }"
                @click="selectWrongAnswer(q.questionId, 'B')"
              >
                B. {{ q.optionB }}
              </div>
              <div
                class="opt-btn"
                :class="{
                  selected: wrongAnswers[q.questionId] === 'C',
                  'correct-result':
                    wrongSubmitted[q.questionId] && q.answer === 'C',
                  'wrong-result':
                    wrongSubmitted[q.questionId] &&
                    wrongAnswers[q.questionId] === 'C' &&
                    q.answer !== 'C',
                }"
                @click="selectWrongAnswer(q.questionId, 'C')"
              >
                C. {{ q.optionC }}
              </div>
              <div
                class="opt-btn"
                :class="{
                  selected: wrongAnswers[q.questionId] === 'D',
                  'correct-result':
                    wrongSubmitted[q.questionId] && q.answer === 'D',
                  'wrong-result':
                    wrongSubmitted[q.questionId] &&
                    wrongAnswers[q.questionId] === 'D' &&
                    q.answer !== 'D',
                }"
                @click="selectWrongAnswer(q.questionId, 'D')"
              >
                D. {{ q.optionD }}
              </div>
            </div>
            <div
              v-else-if="q.questionType === 'judgment'"
              class="options-interactive"
            >
              <div
                class="opt-btn"
                :class="{
                  selected: wrongAnswers[q.questionId] === 'T',
                  'correct-result':
                    wrongSubmitted[q.questionId] && isQuestionAnswerOption(q, 'T'),
                  'wrong-result':
                    wrongSubmitted[q.questionId] &&
                    wrongAnswers[q.questionId] === 'T' &&
                    !isQuestionAnswerOption(q, 'T'),
                }"
                @click="selectWrongAnswer(q.questionId, 'T', q.questionType)"
              >
                正确
              </div>
              <div
                class="opt-btn"
                :class="{
                  selected: wrongAnswers[q.questionId] === 'F',
                  'correct-result':
                    wrongSubmitted[q.questionId] && isQuestionAnswerOption(q, 'F'),
                  'wrong-result':
                    wrongSubmitted[q.questionId] &&
                    wrongAnswers[q.questionId] === 'F' &&
                    !isQuestionAnswerOption(q, 'F'),
                }"
                @click="selectWrongAnswer(q.questionId, 'F', q.questionType)"
              >
                错误
              </div>
            </div>

            <!-- 操作按钮区 -->
            <div class="action-area">
              <!-- 未提交状态 -->
              <el-button
                v-if="!wrongSubmitted[q.questionId]"
                type="primary"
                @click="submitWrongAnswer(q)"
                >提交答案</el-button
              >

              <!-- 提交后显示结果 -->
              <template v-if="wrongSubmitted[q.questionId]">
                <span
                  v-if="wrongResults[q.questionId] === 'correct'"
                  class="result-correct"
                >
                  <el-icon><CircleCheckFilled /></el-icon> 回答正确！
                </span>
                <span v-else class="result-wrong">
                  <el-icon><CircleCloseFilled /></el-icon> 回答错误
                  <el-button
                    type="warning"
                    size="small"
                    @click="retryWrongQuestion(q.questionId)"
                    style="margin-left: 10px"
                  >
                    再做一次
                  </el-button>
                </span>
              </template>
            </div>

            <!-- 解析区（仅正确后显示） -->
            <div
              v-if="wrongResults[q.questionId] === 'correct'"
              class="answer-analysis"
            >
              <div class="analysis-box">
                <div class="label">正确答案：{{ getQuestionAnswerLabel(q) }}</div>
                <div class="content">{{ q.analysis || "暂无解析" }}</div>
              </div>
            </div>
          </el-card>
        </div>
      </el-scrollbar>
    </el-dialog>

    <!-- PDF预览组件 -->
    <pdf-preview ref="pdfPreviewRef" />
    <student-flowchart-dialog v-model="flowchartDialogVisible" :lesson-id="lessonId"
      :question="activeFlowchartQuestion" @submitted="fetchData" />
  </div>
</template>

<script setup name="StudentIndex">
import { ref, computed, onMounted, onUnmounted, nextTick } from "vue";
import {
  getCurrentLesson,
  submitAnswers as submitAnswersApi,
  getHistoryScores,
  getWrongQuestions,
  studentCheckin,
  submitPracticalArtifact,
  deletePracticalArtifact,
  markClassroomTaskState,
} from "@/api/business/studentHome";
import { checkCurrentCountyExam } from "@/api/business/countyExam";
import { updateUserPwd } from "@/api/system/user";
import useUserStore from "@/store/modules/user";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import PdfPreview from "@/components/PdfPreview/index.vue";
import StudentGuideSheet from "@/views/student/guideSheet/index.vue";
import StudentProgrammingQuestion from "@/components/StudentProgrammingQuestion/index.vue";
import StudentFlowchartDialog from "@/components/FlowchartEditor/StudentFlowchartDialog.vue";
import { getStudentProgramming } from "@/api/business/programming";
import { questionTypeLabel } from "@/utils/questionType";
import Download from "@/plugins/download";
import { getCurrentCollaborationRooms } from "@/api/business/collaboration";

// PDF预览组件引用
const pdfPreviewRef = ref(null);

const router = useRouter();
const userStore = useUserStore();

const loading = ref(true);
const hasLesson = ref(false);
const lessonId = ref(null);
// 学生实验工具 + 题目开放开关状态
const studentToolVisible = ref(false);
const lessonTools = ref([]);
const residentTools = ref([]);
const hasToolList = computed(() => lessonTools.value.length > 0 || residentTools.value.length > 0);
const theoryOpen = ref(false);
const practicalOpen = ref(false);
const hasTheory = ref(false);
const hasPractical = ref(false);
const lessonTitle = ref("");
const lessonMode = ref("assessment");
const teacherNote = ref("");
const checkedIn = ref(false);
const checkinTime = ref(null);
const checkinLoading = ref(false);
const iotEnabled = ref(false);
const hasGuideSheet = ref(false);
const guideSheetBindingId = ref(null);
const activeLearningMode = ref("daily");
const guideSheetRef = ref(null);
const accessCheckFailed = ref(false);
const allQuestions = ref([]);
const lessonConfig = ref({
  shuffleMode: 0,
  randomChoiceCount: 0,
  randomJudgmentCount: 0,
});
const studentInfo = ref({});
const studentClassLabel = computed(() => {
  const gradeName = studentInfo.value.gradeName || '未知年级'
  const classCode = studentInfo.value.classCode || ''
  const gradeLabel = gradeName === '已毕业' && studentInfo.value.entryYear
    ? `${studentInfo.value.entryYear}级（已毕业）`
    : gradeName
  return `${gradeLabel}${classCode}班`
})
const collaborationRooms = ref([]);

function openCollaboration(room) {
  router.push(`/student/collaboration/${room.roomId}`);
}

const checkinTimeText = computed(() => {
  if (!checkinTime.value) return "签到成功";
  try {
    return `签到时间：${new Date(checkinTime.value).toLocaleString()}`;
  } catch (e) {
    return "签到成功";
  }
});

// 确定性随机：使用 seed 生成固定随机序列
function seededRandom(seed) {
  const x = Math.sin(seed) * 10000;
  return x - Math.floor(x);
}

function seededShuffle(array, seed) {
  const result = [...array];
  // 使用 Park-Miller 算法（Minimal Standard LCG）避免整数溢出
  const m = 2147483647; // 2^31 - 1 (梅森素数)
  const a = 16807;      // 乘数
  let s = Math.abs(seed) % m;
  if (s === 0) s = 1;   // 避免种子为0
  
  for (let i = result.length - 1; i > 0; i--) {
    s = (s * a) % m;
    const j = s % (i + 1);
    [result[i], result[j]] = [result[j], result[i]];
  }
  return result;
}

// 应用随机逻辑到题目列表
function applyRandomShuffle(questions, config, studentId, lessonIdVal) {
  const { shuffleMode, randomChoiceCount, randomJudgmentCount } = config;
  if (shuffleMode === 0) return questions; // 固定顺序
  
  // 生成唯一种子：studentId + lessonId
  const seed = (studentId || 0) * 10000 + (lessonIdVal || 0);
  // 分类
  const typing = questions.filter(q => q.questionType === 'typing');
  const practical = questions.filter(q => q.questionType === 'practical');
  let choice = questions.filter(q => q.questionType === 'choice');
  let judgment = questions.filter(q => q.questionType === 'judgment');
  
  // 对选择题和判断题应用随机
  choice = seededShuffle(choice, seed);
  judgment = seededShuffle(judgment, seed + 1);
  
  // 模式2：随机抽题
  if (shuffleMode === 2) {
    if (randomChoiceCount > 0 && randomChoiceCount < choice.length) {
      choice = choice.slice(0, randomChoiceCount);
    }
    if (randomJudgmentCount > 0 && randomJudgmentCount < judgment.length) {
      judgment = judgment.slice(0, randomJudgmentCount);
    }
  }
  
  // 合并：打字 > 操作 > 选择 > 判断
  return [...typing, ...practical, ...choice, ...judgment];
}
const answers = ref({});
const pwdDialogVisible = ref(false);
const pwdFormRef = ref(null);
const pwdForm = ref({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});
const pwdRules = {
  oldPassword: [{ required: true, message: "请输入旧密码", trigger: "blur" }],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, max: 20, message: "长度在 6 到 20 个字符", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请确认新密码", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.value.newPassword) {
          callback(new Error("两次输入的密码不一致"));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
};

function submitPwd() {
  pwdFormRef.value.validate((valid) => {
    if (valid) {
      updateUserPwd(pwdForm.value.oldPassword, pwdForm.value.newPassword).then(
        (response) => {
          ElMessage.success("修改成功，请重新登录");
          pwdDialogVisible.value = false;
          userStore.logOut().then(() => {
            location.href = "/index";
          });
        }
      );
    }
  });
}

const inputRefs = ref({});

// 历史成绩状态
const historyDialogVisible = ref(false);
const historyYear = ref(new Date().getFullYear());
const historyList = ref([]);
const historyLoading = ref(false);
const yearOptions = computed(() => {
  const currentYear = new Date().getFullYear();
  return [currentYear, currentYear - 1, currentYear - 2];
});

// 错题本状态
const wrongDialogVisible = ref(false);
const wrongList = ref([]);
const wrongLoading = ref(false);
const selectedWrongLessonId = ref(null);
const wrongLessonOptions = ref([]);
// 交互式练习状态
const wrongAnswers = ref({}); // { questionId: 'A' } 用户选择的答案
const wrongSubmitted = ref({}); // { questionId: true } 是否已提交
const wrongResults = ref({}); // { questionId: 'correct'|'wrong' } 结果

function normalizeJudgmentAnswer(answer) {
  if (answer === null || answer === undefined) {
    return null;
  }

  const trimmed = String(answer).trim();
  if (!trimmed) {
    return null;
  }

  if (
    trimmed === "对" ||
    trimmed === "正确" ||
    trimmed === "T" ||
    trimmed === "1" ||
    trimmed.toLowerCase() === "true"
  ) {
    return "T";
  }

  if (
    trimmed === "错" ||
    trimmed === "错误" ||
    trimmed === "F" ||
    trimmed === "0" ||
    trimmed.toLowerCase() === "false"
  ) {
    return "F";
  }

  return trimmed.toUpperCase();
}

function normalizeQuestionAnswer(questionType, answer) {
  if (questionType === "judgment") {
    return normalizeJudgmentAnswer(answer);
  }
  return answer;
}

function isQuestionAnswerCorrect(question, userAnswer) {
  const normalizedUserAnswer = normalizeQuestionAnswer(
    question?.questionType,
    userAnswer
  );
  const normalizedStandardAnswer = normalizeQuestionAnswer(
    question?.questionType,
    question?.answer
  );

  if (normalizedUserAnswer === null || normalizedStandardAnswer === null) {
    return false;
  }

  return normalizedUserAnswer === normalizedStandardAnswer;
}

function isQuestionAnswerOption(question, option) {
  return isQuestionAnswerCorrect(question, option);
}

function getQuestionAnswerLabel(question) {
  const normalizedAnswer = normalizeQuestionAnswer(
    question?.questionType,
    question?.answer
  );

  if (question?.questionType === "judgment") {
    if (normalizedAnswer === "T") {
      return "正确";
    }
    if (normalizedAnswer === "F") {
      return "错误";
    }
  }

  return question?.answer ?? "";
}

// 加载错题
async function loadWrongQuestions() {
  wrongLoading.value = true;
  // 重置练习状态
  wrongAnswers.value = {};
  wrongSubmitted.value = {};
  wrongResults.value = {};
  try {
    const res = await getWrongQuestions(selectedWrongLessonId.value);
    let list = [];
    if (Array.isArray(res)) {
      list = res;
    } else if (res && Array.isArray(res.data)) {
      list = res.data;
    }
    wrongList.value = list;
  } catch (err) {
    console.error("Failed to load wrong questions:", err);
    wrongList.value = [];
  } finally {
    wrongLoading.value = false;
  }
}

// 打开错题本
function openWrongBook() {
  wrongDialogVisible.value = true;
  // 默认选中当前课程
  if (lessonId.value) {
    selectedWrongLessonId.value = lessonId.value;
    loadLessonOptions();
  } else {
    selectedWrongLessonId.value = null;
    loadLessonOptions();
  }
  loadWrongQuestions();
}

async function loadLessonOptions() {
  let options = [{ label: "所有课程", value: null }];
  if (lessonId.value) {
    options.push({
      label: "(当前) " + lessonTitle.value,
      value: lessonId.value,
    });
  }
  try {
    const res = await getHistoryScores(new Date().getFullYear());
    let hList = [];
    if (Array.isArray(res)) hList = res;
    else if (res && Array.isArray(res.data)) hList = res.data;
    hList.forEach((h) => {
      if (h.lessonId !== lessonId.value) {
        options.push({ label: h.lessonTitle, value: h.lessonId });
      }
    });
  } catch (e) {
    console.error(e);
  }
  wrongLessonOptions.value = options;
}

// 选择答案
function selectWrongAnswer(questionId, answer, questionType = null) {
  // 如果已提交且正确，不允许修改
  if (
    wrongSubmitted.value[questionId] &&
    wrongResults.value[questionId] === "correct"
  ) {
    return;
  }
  wrongAnswers.value[questionId] = normalizeQuestionAnswer(questionType, answer);
  // 如果之前提交错误，现在重新选择，清除提交状态
  if (wrongSubmitted.value[questionId]) {
    wrongSubmitted.value[questionId] = false;
    wrongResults.value[questionId] = null;
  }
}

// 提交单题答案
function submitWrongAnswer(question) {
  const questionId = question.questionId;
  const userAnswer = wrongAnswers.value[questionId];
  if (!userAnswer) {
    ElMessage.warning("请先选择答案");
    return;
  }
  wrongSubmitted.value[questionId] = true;
  if (isQuestionAnswerCorrect(question, userAnswer)) {
    wrongResults.value[questionId] = "correct";
    ElMessage.success("回答正确！");
  } else {
    wrongResults.value[questionId] = "wrong";
    ElMessage.error("回答错误，请再试一次");
  }
}

// 重做
function retryWrongQuestion(questionId) {
  wrongAnswers.value[questionId] = null;
  wrongSubmitted.value[questionId] = false;
  wrongResults.value[questionId] = null;
}

// 打字题状态管理
const typingStates = ref({});
const timerIntervals = {};

// 理论测试得分状态
const theoryScore = ref(null); // null表示未提交
const theorySubmitted = ref(false);

// 分类题目
const typingQuestions = computed(() =>
  allQuestions.value.filter((q) => q.questionType === "typing")
);
// 理论题：老师课堂上开启后才显示（老师未开启时不渲染作答区）
const theoryQuestions = computed(() =>
  theoryOpen.value
    ? allQuestions.value.filter((q) =>
        ["choice", "judgment"].includes(q.questionType)
      )
    : []
);

// 理论测试总分
const theoryTotalScore = computed(() => {
  return theoryQuestions.value.reduce(
    (sum, q) => sum + (q.questionScore || 0),
    0
  );
});

// 操作题：老师课堂上开启后才显示（老师未开启时不渲染作答区）
const practicalQuestions = computed(() =>
  practicalOpen.value
    ? allQuestions.value.filter((q) => q.questionType === "practical")
    : []
);
// 兼容历史接口的下划线字段和不同大小写，避免 Python 题误回退到文件上传。
function isPythonPracticalQuestion(question) {
  const mode = question?.practicalMode ?? question?.practical_mode;
  return String(mode || "").trim().toUpperCase() === "PYTHON";
}

function isFlowchartPracticalQuestion(question) {
  const mode = question?.practicalMode ?? question?.practical_mode;
  return String(mode || "").trim().toUpperCase() === "FLOWCHART";
}

// 兼容历史 DTO 漏传 practicalMode 的情况：仅对作答方式为空的操作题回查平台后端。
// 普通文件题的回查会被后端拒绝，仍按文件上传处理，不会误显示为编程题。
async function resolveMissingPracticalModes(questions, currentLessonId) {
  const pending = (questions || []).filter(
    (question) => question?.questionType === "practical" && !String(question?.practicalMode ?? question?.practical_mode ?? "").trim()
  );
  await Promise.all(pending.map(async (question) => {
    try {
      const result = await getStudentProgramming(currentLessonId, question.questionId);
      if (String(result?.data?.config?.languageCode || "").toLowerCase() === "python") {
        question.practicalMode = "PYTHON";
      }
    } catch (error) {
      // 无 Python 配置或没有权限时保留 FILE 回退，不影响普通操作题作答。
    }
  }));
}
const pythonPracticalQuestions = computed(() =>
  practicalQuestions.value.filter(isPythonPracticalQuestion)
);
const flowchartPracticalQuestions = computed(() =>
  practicalQuestions.value.filter(isFlowchartPracticalQuestion)
);
const filePracticalQuestions = computed(() =>
  practicalQuestions.value.filter((q) => !isPythonPracticalQuestion(q) && !isFlowchartPracticalQuestion(q))
);
const flowchartDialogVisible = ref(false);
const activeFlowchartQuestion = ref(null);

function openFlowchart(question) {
  markQuestionWorking(question.questionId);
  activeFlowchartQuestion.value = question;
  flowchartDialogVisible.value = true;
}
const practicalUploads = ref({}); // { questionId: uploadedFilePath }
const practicalScores = ref({}); // { questionId: score | null } - null表示未批阅
const practicalPreviewStatuses = ref({}); // { questionId: previewStatus }
const practicalPreviewPaths = ref({}); // { questionId: previewPath }
const practicalArtifacts = ref({}); // { questionId: 当前不可变作品版本 }
const practicalDrafts = ref({}); // { questionId: 暂存文件凭证[] }
const practicalUploadStates = ref({});
const practicalAutoCommitTimers = new Map();
const submittedAnswers = ref({}); // 学生已提交的答案 { questionId: { answer, score, previewStatus, previewPath } }
const uploadingQuestionId = ref(null); // 正在上传/转换的题目ID（用于显示loading）
const submittingPracticalQuestionId = ref(null);
const practicalPollingTimers = {};
const enteredTaskIds = new Set();
const workingTaskIds = new Set();

function reportTaskState(questionId, taskState) {
  if (!lessonId.value || !questionId) return Promise.resolve();
  return markClassroomTaskState({ lessonId: lessonId.value, questionId, taskState }).catch(() => undefined);
}

function markQuestionEntered(questionId) {
  if (enteredTaskIds.has(questionId) || submittedAnswers.value[questionId]) return;
  enteredTaskIds.add(questionId);
  reportTaskState(questionId, 'ENTERED');
}

function markQuestionWorking(questionId) {
  if (workingTaskIds.has(questionId) || submittedAnswers.value[questionId]) return;
  workingTaskIds.add(questionId);
  reportTaskState(questionId, 'WORKING');
}

function selectTheoryAnswer(questionId, answer) {
  if (theorySubmitted.value) return;
  answers.value[questionId] = answer;
  markQuestionWorking(questionId);
}

// 操作题总分
const practicalTotalScore = computed(() => {
  return practicalQuestions.value.reduce(
    (sum, q) => sum + (q.questionScore || 0),
    0
  );
});

// 打字题总分
const typingTotalScore = computed(() => {
  return typingQuestions.value.reduce(
    (sum, q) => sum + (q.questionScore || 0),
    0
  );
});

// 课程总分（所有题目分值之和）
const courseTotalScore = computed(() => {
  return allQuestions.value.reduce((sum, q) => sum + (q.questionScore || 0), 0);
});

// 我的得分（已提交题目的得分之和）
const courseMyScore = computed(() => {
  let total = 0;
  let hasAnyScore = false;

  // 打字题得分
  typingQuestions.value.forEach((q) => {
    const state = typingStates.value[q.questionId];
    if (state?.submitted && state?.myScore !== undefined) {
      total += state.myScore;
      hasAnyScore = true;
    }
  });

  // 理论测试得分
  if (theorySubmitted.value && theoryScore.value !== null) {
    total += theoryScore.value;
    hasAnyScore = true;
  }

  // 操作题得分（已批阅的）
  practicalQuestions.value.forEach((q) => {
    const score = isPythonPracticalQuestion(q)
      ? submittedAnswers.value[q.questionId]?.score
      : practicalScores.value[q.questionId];
    if (score !== null && score !== undefined) {
      total += score;
      hasAnyScore = true;
    }
  });

  return hasAnyScore ? total : null;
});

// 操作题我的得分
const practicalMyScore = computed(() => {
  let total = 0;
  let hasAnyScore = false;
  practicalQuestions.value.forEach((q) => {
    const score = isPythonPracticalQuestion(q)
      ? submittedAnswers.value[q.questionId]?.score
      : practicalScores.value[q.questionId];
    if (score !== null && score !== undefined) {
      total += score;
      hasAnyScore = true;
    }
  });
  return hasAnyScore ? total : null;
});

// 打字题我的得分
const typingMyScore = computed(() => {
  let total = 0;
  let hasAnyScore = false;
  typingQuestions.value.forEach((q) => {
    const state = typingStates.value[q.questionId];
    if (state?.submitted && state?.myScore !== undefined) {
      total += state.myScore;
      hasAnyScore = true;
    }
  });
  return hasAnyScore ? total : null;
});

// 上传配置
const uploadUrl = import.meta.env.VITE_APP_BASE_API + "/business/student-home/practical-upload";
const uploadHeaders = computed(() => ({
  Authorization: "Bearer " + userStore.token,
}));

// 加载数据（silent=静默轮询：不触发整屏 loading，失败时保留现有页面状态）
async function fetchData(opts = {}) {
  const silent = opts && opts.silent === true;
  if (!silent) {
    loading.value = true;
  }
  accessCheckFailed.value = false;
  try {
    // 区域抽测检查失败时停止加载日常课程，避免网络异常导致优先级失效。
    const countyExamRes = await checkCurrentCountyExam();
    if (countyExamRes.data?.hasExam && !countyExamRes.data?.ended) {
      router.replace("/student/county-exam");
      return;
    }
    const res = await getCurrentLesson();
    try {
      const collaborationRes = await getCurrentCollaborationRooms();
      collaborationRooms.value = collaborationRes.data || collaborationRes || [];
    } catch (collaborationError) {
      collaborationRooms.value = [];
    }
    if (res.blockedByCountyExam) {
      router.replace("/student/county-exam");
      return;
    }
    hasLesson.value = res.hasLesson || false;
    hasGuideSheet.value = false;
    guideSheetBindingId.value = null;
    lessonMode.value = "assessment";
    teacherNote.value = "";
    checkedIn.value = false;
    checkinTime.value = null;
    iotEnabled.value = false;
    if (res.hasLesson) {
      lessonId.value = res.lessonId;
      lessonTitle.value = res.lessonTitle;
      lessonMode.value = res.lessonMode === "attendance" ? "attendance" : "assessment";
      teacherNote.value = res.teacherNote || "";
      checkedIn.value = Boolean(res.checkedIn);
      checkinTime.value = res.checkinTime || null;
      // 课程级物联网开关：只有教师开启后才显示「物联实验」入口。
      iotEnabled.value = Boolean(res.iotEnabled);
      guideSheetBindingId.value = res.guideSheetBindingId || res.guideSheetBinding?.bindingId || null;
      hasGuideSheet.value = Boolean(res.guideSheetEnabled && guideSheetBindingId.value);
      
      // 保存课程随机配置
      lessonConfig.value = {
        shuffleMode: res.shuffleMode ?? 0,
        randomChoiceCount: res.randomChoiceCount ?? 0,
        randomJudgmentCount: res.randomJudgmentCount ?? 0,
      };
      
      // 应用随机逻辑
      const rawQuestions = res.questions || [];
      await resolveMissingPracticalModes(rawQuestions, res.lessonId);
      
      const studentId = res.studentInfo?.studentId || 0;
      allQuestions.value = applyRandomShuffle(
        rawQuestions, 
        lessonConfig.value, 
        studentId, 
        res.lessonId
      );
      
      studentInfo.value = res.studentInfo || {};
      submittedAnswers.value = res.submittedAnswers || {};
      // 学生实验工具与题目开放开关（班级x当前课程，推进自动复位）
      lessonTools.value = res.studentTools?.lessonTools || [];
      residentTools.value = res.studentTools?.residentTools || [];
      theoryOpen.value = Boolean(res.theoryOpen);
      practicalOpen.value = Boolean(res.practicalOpen);
      hasTheory.value = Boolean(res.hasTheory);
      hasPractical.value = Boolean(res.hasPractical);
      initTypingStates();
      initPracticalStates(); // 初始化操作题状态
      initTheoryState(); // 初始化理论测试状态（检查是否已提交）
      if (!silent) {
        [...theoryQuestions.value, ...typingQuestions.value, ...practicalQuestions.value]
          .forEach((question) => markQuestionEntered(question.questionId));
      }
    }
  } catch (err) {
    // 静默轮询失败不清空现有状态，下个周期再试；避免一次网络抖动抹掉整页课程
    if (silent) {
      return;
    }
    // 抽测状态无法确认时保持关闭，禁止回落到日常课程。
    accessCheckFailed.value = true;
    hasLesson.value = false;
    hasGuideSheet.value = false;
    guideSheetBindingId.value = null;
    lessonMode.value = "assessment";
    teacherNote.value = "";
    checkedIn.value = false;
    checkinTime.value = null;
    iotEnabled.value = false;
    allQuestions.value = [];
    lessonTools.value = [];
    residentTools.value = [];
    theoryOpen.value = false;
    practicalOpen.value = false;
    hasTheory.value = false;
    hasPractical.value = false;
    activeLearningMode.value = 'daily';
  } finally {
    loading.value = false;
  }
}

async function handleStudentCheckin() {
  if (!lessonId.value || checkinLoading.value) return;
  checkinLoading.value = true;
  try {
    const res = await studentCheckin(lessonId.value);
    checkedIn.value = true;
    checkinTime.value = res.checkinTime || new Date().toISOString();
    ElMessage.success(res.msg || "签到成功");
  } catch (e) {
    // request 拦截器通常已提示
  } finally {
    checkinLoading.value = false;
  }
}

// 初始化操作题状态（加载已提交的作品）
function initPracticalStates() {
  filePracticalQuestions.value.forEach((q) => {
    syncPracticalSubmission(q.questionId, submittedAnswers.value[q.questionId]);
  });
}

function syncPracticalSubmission(questionId, submitted) {
  if (submitted && submitted.answer) {
    practicalUploads.value[questionId] = submitted.answer;
    practicalScores.value[questionId] = submitted.score;
    practicalPreviewPaths.value[questionId] = submitted.previewPath || "";
    practicalPreviewStatuses.value[questionId] = submitted.previewStatus || (submitted.previewPath ? "success" : "");
    practicalArtifacts.value[questionId] = submitted.artifact || null;
  } else {
    delete practicalUploads.value[questionId];
    delete practicalScores.value[questionId];
    delete practicalPreviewStatuses.value[questionId];
    delete practicalPreviewPaths.value[questionId];
    delete practicalArtifacts.value[questionId];
  }
}

function clearPracticalPolling(questionId) {
  if (practicalPollingTimers[questionId]) {
    clearTimeout(practicalPollingTimers[questionId]);
    delete practicalPollingTimers[questionId];
  }
}

async function refreshPracticalSubmission(questionId) {
  const res = await getCurrentLesson();
  const latestAnswers = res.submittedAnswers || {};
  submittedAnswers.value = latestAnswers;
  const submitted = latestAnswers[questionId];
  syncPracticalSubmission(questionId, submitted);
  return submitted;
}

function schedulePracticalPreviewPolling(questionId, attempt = 0) {
  clearPracticalPolling(questionId);
  practicalPollingTimers[questionId] = setTimeout(async () => {
    try {
      const submitted = await refreshPracticalSubmission(questionId);
      const previewStatus = submitted?.previewStatus || "";
      if (previewStatus === "success" || submitted?.previewPath) {
        uploadingQuestionId.value = null;
        ElMessage.success("转换完成，可以预览了");
        clearPracticalPolling(questionId);
        return;
      }
      if (previewStatus === "failed") {
        uploadingQuestionId.value = null;
        ElMessage.warning("作品已上传，预览暂不可用，请先下载原文件查看");
        clearPracticalPolling(questionId);
        return;
      }
      if (attempt >= 9) {
        uploadingQuestionId.value = null;
        ElMessage.info("作品已上传，服务器仍在转换预览，稍后刷新页面即可查看最新状态");
        clearPracticalPolling(questionId);
        return;
      }
      schedulePracticalPreviewPolling(questionId, attempt + 1);
    } catch (error) {
      uploadingQuestionId.value = null;
      clearPracticalPolling(questionId);
      ElMessage.warning("已上传作品，但刷新转换状态失败，请稍后查看");
    }
  }, 2000);
}

// 初始化理论测试状态（检查是否已提交）
function initTheoryState() {
  // 检查是否有任何理论题已提交
  let hasSubmitted = false;
  let totalScore = 0;

  theoryQuestions.value.forEach((q) => {
    const submitted = submittedAnswers.value[q.questionId];
    if (submitted) {
      hasSubmitted = true;
      totalScore += submitted.score || 0;
      // 恢复已提交的答案到界面
      if (submitted.answer) {
        answers.value[q.questionId] = normalizeQuestionAnswer(
          q.questionType,
          submitted.answer
        );
      }
    }
  });

  if (hasSubmitted) {
    theorySubmitted.value = true;
    theoryScore.value = totalScore;
  }
}

function initTypingStates() {
  typingQuestions.value.forEach((q) => {
    // 检查是否已提交过答案
    const submitted = submittedAnswers.value[q.questionId];
    const hasSubmitted = !!submitted;

    typingStates.value[q.questionId] = {
      started: hasSubmitted, // 如果已提交，则标记为已开始，显示"重新打字"按钮
      finished: hasSubmitted,
      submitted: hasSubmitted,
      startTime: 0,
      timeLeft: hasSubmitted ? 0 : (q.typingDuration || 10) * 60,
      durationLimit: (q.typingDuration || 10) * 60,
      completedCount: hasSubmitted && submitted.answer ? submitted.answer.length : 0,
      errorCount: 0,
      correctCount: 0,
      accuracy: 100,
      speed: 0,
      progress: 0,
      submitting: false,
      myScore: hasSubmitted ? (submitted.score || 0) : 0,
    };
    
    // 如果已提交，恢复之前的文本
    if (hasSubmitted && submitted.answer) {
      answers.value[q.questionId] = submitted.answer;
      // 简单计算一下统计数据（可选）
      nextTick(() => {
        updateTypingStats(q.questionId, submitted.answer);
      });
    } else {
      answers.value[q.questionId] = "";
    }
  });
}

// ================== 打字逻辑 ==================

// 点击"开始练习"按钮
function startTypingPractice(qid) {
  markQuestionWorking(qid);
  const state = typingStates.value[qid];
  if (!state || state.started) return;

  state.started = true;
  state.startTime = Date.now();

  // 聚焦输入框
  nextTick(() => {
    const inputEl = inputRefs.value[qid];
    if (inputEl) inputEl.focus();
  });

  // 开始倒计时
  timerIntervals[qid] = setInterval(() => {
    if (state.timeLeft > 0) {
      state.timeLeft--;
      updateTypingStats(qid, answers.value[qid] || "");
    } else {
      // 时间到，自动提交
      autoSubmitTyping(qid);
    }
  }, 1000);
}

// 点击"重新打字"按钮
function restartTyping(qid) {
  const state = typingStates.value[qid];
  const question = allQuestions.value.find((q) => q.questionId === qid);
  if (!state || !question) return;

  // 清除旧计时器
  if (timerIntervals[qid]) {
    clearInterval(timerIntervals[qid]);
  }

  // 重置状态
  state.started = false;
  state.finished = false;
  state.submitted = false;
  state.timeLeft = (question.typingDuration || 10) * 60;
  state.durationLimit = state.timeLeft;
  state.completedCount = 0;
  state.errorCount = 0;
  state.correctCount = 0;
  state.accuracy = 100;
  state.speed = 0;
  state.progress = 0;
  state.submitting = false;
  state.myScore = 0;

  // 清空输入
  answers.value[qid] = "";
  
  // 自动开始
  startTypingPractice(qid);
}

// 禁止粘贴，提示用户
function handlePasteBlock() {
  ElMessage.warning("打字练习禁止使用粘贴功能，请手动输入");
}

function handleTypingInput(qid, val) {
  markQuestionWorking(qid);
  updateTypingStats(qid, val);

  // 检测是否打完所有字
  const question = allQuestions.value.find((q) => q.questionId === qid);
  const original = question?.questionContent || "";
  const state = typingStates.value[qid];

  if (state && val.length >= original.length) {
    // 打完了，自动提交
    autoSubmitTyping(qid);
  }
}

function updateTypingStats(qid, inputVal) {
  const state = typingStates.value[qid];
  if (!state) return;

  const question = allQuestions.value.find((q) => q.questionId === qid);
  const original = question?.questionContent || "";

  let correct = 0;
  let error = 0;

  for (let i = 0; i < inputVal.length; i++) {
    if (i >= original.length) break;
    if (inputVal[i] === original[i]) {
      correct++;
    } else {
      error++;
    }
  }

  state.completedCount = inputVal.length;
  state.correctCount = correct;
  state.errorCount = error;
  state.accuracy =
    inputVal.length > 0 ? ((correct / inputVal.length) * 100).toFixed(1) : 100;

  const timeElapsed = state.durationLimit - state.timeLeft;
  const minutes = timeElapsed > 0 ? timeElapsed / 60 : 1 / 60;
  state.speed = (correct / minutes).toFixed(1);
  state.progress =
    original.length > 0 ? ((correct / original.length) * 100).toFixed(1) : 0;
}

// 自动提交（打完或时间到）
function autoSubmitTyping(qid) {
  const state = typingStates.value[qid];
  if (!state || state.submitted || state.submitting) return;

  state.submitting = true;

  // 停止计时
  if (timerIntervals[qid]) {
    clearInterval(timerIntervals[qid]);
  }
  state.finished = true;

  // 提交到后端
  const question = allQuestions.value.find((q) => q.questionId === qid);
  const submitData = { [qid]: answers.value[qid] };

  // 计算实际耗时（秒）
  // 必须使用 state.durationLimit（秒单位），而非 question.typingDuration（分钟单位）
  const durationLimit = state.durationLimit;
  let timeSpent = 0;

  if (
    durationLimit > 0 &&
    state.timeLeft !== undefined &&
    state.timeLeft >= 0
  ) {
    timeSpent = durationLimit - state.timeLeft; // 例如：300 - 253 = 47秒
  } else {
    timeSpent = 1; // 兜底
  }

  // 边界检查
  if (timeSpent > durationLimit) timeSpent = durationLimit;
  if (timeSpent < 1) timeSpent = 1;

  const submitTimes = { [qid]: timeSpent };

  // 打字详情统计
  const typingStats = {
    [qid]: {
      typingSpeed: Math.round(parseFloat(state.speed) || 0), // 字符/分钟
      accuracyRate: parseFloat(state.accuracy) || 0, // 正确率 %
      completionRate: parseFloat(state.progress) || 0, // 完成率 %
    },
  };

  submitAnswersApi({
    lessonId: lessonId.value,
    answers: submitData,
    answerTimes: submitTimes,
    typingStats: typingStats, // 新增：打字详情
  })
    .then((res) => {
      state.submitted = true;
      state.submitting = false;
      state.myScore = res.totalScore || 0;
      ElMessage.success(`打字成绩已自动提交！得分: ${state.myScore}分`);
    })
    .catch(() => {
      state.submitting = false;
      state.finished = false;
      ElMessage.error("提交失败，请手动点击提交按钮重试");
    });
}

// 手动点击提交
function submitTyping(q) {
  const state = typingStates.value[q.questionId];
  if (!state?.started) {
    ElMessage.warning("请先点击开始练习");
    return;
  }
  if (state.submitted) {
    ElMessage.info("成绩已提交，如需重新打字请点击「重新打字」");
    return;
  }
  if (state.submitting) {
    ElMessage.info("成绩正在提交，请稍候");
    return;
  }

  ElMessageBox.confirm("确定提交当前打字成绩吗？", "提示", { type: "info" })
    .then(() => {
      autoSubmitTyping(q.questionId);
    })
    .catch(() => {});
}

function getCharClass(qid, idx) {
  const inputVal = answers.value[qid] || "";
  const question = allQuestions.value.find((q) => q.questionId === qid);
  const original = question?.questionContent || "";

  if (idx >= inputVal.length) return "char-pending";
  if (inputVal[idx] === original[idx]) return "char-correct";
  return "char-error";
}

// ================== 理论测试逻辑 ==================

function submitTheory() {
  // 检查是否已经提交过
  if (theorySubmitted.value) {
    ElMessage.warning("理论测试已提交，不可重复提交");
    return;
  }

  const theoryQuestionMap = new Map(
    theoryQuestions.value.map((q) => [q.questionId, q])
  );
  const ids = theoryQuestions.value.map((q) => q.questionId);
  const submitData = {};
  let answeredCount = 0;

  ids.forEach((id) => {
    if (answers.value[id]) {
      const question = theoryQuestionMap.get(id);
      submitData[id] = normalizeQuestionAnswer(
        question?.questionType,
        answers.value[id]
      );
      answeredCount++;
    }
  });

  if (answeredCount === 0) {
    ElMessage.warning("请至少完成一道题目");
    return;
  }

  ElMessageBox.confirm(
    `已完成 ${answeredCount}/${theoryQuestions.value.length} 道题目，确定提交吗？`,
    "提示",
    { type: "warning" }
  ).then(() => {
    submitAnswersApi({
      lessonId: lessonId.value,
      answers: submitData,
    }).then((res) => {
      theoryScore.value = res.totalScore || 0;
      theorySubmitted.value = true;
      ElMessage.success(
        `提交成功！理论测试得分: ${res.totalScore}/${theoryTotalScore.value}`
      );
    });
  });
}

// ================== 公共逻辑 ==================

function formatTime(seconds) {
  const min = Math.floor(seconds / 60);
  const sec = seconds % 60;
  return `${min}分${sec.toString().padStart(2, "0")}秒`;
}

// ================== 操作题逻辑 ==================

// 获取文件名
function getFileName(filePath) {
  if (!filePath) return "";
  return filePath.split("/").pop();
}

// 下载素材文件
function downloadMaterial(filePath, originalFileName) {
  if (!filePath) return;
  Download.resource(filePath, `课堂题目素材_${originalFileName || getFileName(filePath)}`);
}

function getPracticalAllowedExtensions(question) {
  const configured = String(question?.practicalAllowedExtensions || "").toLowerCase()
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean);
  return configured.length
    ? configured
    : ["doc", "docx", "pdf", "ppt", "pptx", "xls", "xlsx", "jpg", "jpeg", "png"];
}

function getStudentMaterials(question) {
  if (Array.isArray(question?.practicalMaterials) && question.practicalMaterials.length) {
    return question.practicalMaterials;
  }
  return question?.filePath ? [{ resourcePath: question.filePath }] : [];
}

function getPracticalAccept(question) {
  return getPracticalAllowedExtensions(question).map((item) => `.${item}`).join(",");
}

function getPracticalUploadLimit(question) {
  return Math.min(Math.max(Number(question?.practicalImageMaxCount || 10), 1), 10);
}

function beforePracticalUpload(question, file) {
  markQuestionWorking(question.questionId);
  const extension = String(file?.name || "").split(".").pop().toLowerCase();
  if (!getPracticalAllowedExtensions(question).includes(extension)) {
    ElMessage.error(`当前题目不允许上传 .${extension || "未知"} 文件`);
    return false;
  }
  if (file.size > 50 * 1024 * 1024) {
    ElMessage.error("单个文件不能超过 50MB");
    return false;
  }
  const state = practicalUploadStates.value[question.questionId] || { pendingFileUids: new Set(), failed: false };
  if (!state.pendingFileUids.size) state.failed = false;
  state.pendingFileUids.add(file.uid);
  practicalUploadStates.value[question.questionId] = state;
  clearPracticalAutoCommit(question.questionId);
  return true;
}

function getPracticalDrafts(questionId) {
  return practicalDrafts.value[questionId] || [];
}

function removePracticalDraft(questionId, index) {
  const drafts = [...getPracticalDrafts(questionId)];
  drafts.splice(index, 1);
  practicalDrafts.value[questionId] = drafts;
}

function clearPracticalAutoCommit(questionId) {
  const timer = practicalAutoCommitTimers.get(questionId);
  if (timer) {
    clearTimeout(timer);
    practicalAutoCommitTimers.delete(questionId);
  }
}

function schedulePracticalAutoCommit(question) {
  const questionId = question.questionId;
  clearPracticalAutoCommit(questionId);
  // 同一次选择多张图片时，等待全部上传回调结束后再合并为一个作品版本。
  practicalAutoCommitTimers.set(questionId, setTimeout(() => {
    practicalAutoCommitTimers.delete(questionId);
    const state = practicalUploadStates.value[questionId];
    if (!state?.pendingFileUids.size && !state?.failed) {
      commitPracticalArtifact(question).catch(() => {
        ElMessage.error("作品提交失败，请重新选择文件后再试");
      });
    }
  }, 200));
}

// 上传成功后立即提交，避免学生再做一次重复的“确认提交”操作。
function handleUploadSuccess(questionId, res, file) {
  const state = practicalUploadStates.value[questionId];
  state?.pendingFileUids.delete(file?.uid);
  if (res.code === 200) {
    practicalDrafts.value[questionId] = [
      ...getPracticalDrafts(questionId),
      {
        uploadToken: res.uploadToken,
        originalFileName: res.newFileName || file?.name || getFileName(res.fileName),
        fileKind: res.fileKind,
        fileExtension: res.fileExtension,
        fileSize: res.fileSize,
      },
    ];
    schedulePracticalAutoCommit({ questionId });
  } else {
    if (state) state.failed = true;
    ElMessage.error(res.msg || "上传失败");
  }
}

async function commitPracticalArtifact(question) {
  const questionId = question.questionId;
  const drafts = getPracticalDrafts(questionId);
  if (!drafts.length || submittingPracticalQuestionId.value) return;
  submittingPracticalQuestionId.value = questionId;
  try {
    await submitPracticalArtifact({
      lessonId: lessonId.value,
      questionId,
      expectedVersionId: practicalArtifacts.value[questionId]?.versionId || null,
      uploadTokens: drafts.map((item) => item.uploadToken),
    });
    practicalDrafts.value[questionId] = [];
    uploadingQuestionId.value = questionId;
    const submitted = await refreshPracticalSubmission(questionId);
    const previewStatus = submitted?.previewStatus || "";
    if (previewStatus === "success" || submitted?.previewPath) {
      uploadingQuestionId.value = null;
      ElMessage.success("作品提交成功，可以直接预览");
    } else {
      ElMessage.success("作品提交成功，预览正在后台生成");
      schedulePracticalPreviewPolling(questionId);
    }
  } finally {
    submittingPracticalQuestionId.value = null;
  }
}

function handleUploadError(questionId, _error, file) {
  const state = practicalUploadStates.value[questionId];
  if (state) {
    state.pendingFileUids.delete(file?.uid);
    state.failed = true;
  }
  ElMessage.error("上传失败，请重试");
}

function handleUploadExceed(question) {
  ElMessage.warning(`图片作品最多 ${getPracticalUploadLimit(question)} 张，Office/PDF 只能选择 1 个`);
}

function getPracticalAttachments(questionId) {
  const attachments = practicalArtifacts.value[questionId]?.attachments;
  if (Array.isArray(attachments) && attachments.length) return attachments;
  const resourcePath = practicalUploads.value[questionId];
  return resourcePath ? [{ resourcePath, previewPath: practicalPreviewPaths.value[questionId], previewStatus: practicalPreviewStatuses.value[questionId] }] : [];
}

function getPracticalDisplayName(questionId) {
  const attachments = getPracticalAttachments(questionId);
  if (attachments.length > 1) return `${attachments.length} 个文件（图片组）`;
  return attachments[0]?.originalFileName || getFileName(attachments[0]?.resourcePath);
}

function getPracticalPreviewLabel(questionId) {
  // C3：交卷成功与预览成功解耦的文案
  const status = practicalPreviewStatuses.value[questionId];
  if (getPracticalAttachments(questionId).some(isPracticalAttachmentPreviewable)) return "可预览";
  if (status === "success") return "可预览";
  if (status === "pending") return "已交卷·预览排队";
  if (status === "converting") return "已交卷·预览转换中";
  if (status === "failed") return "已交卷·预览暂不可用";
  return "待处理";
}

function getPracticalPreviewClass(questionId) {
  const status = practicalPreviewStatuses.value[questionId];
  if (getPracticalAttachments(questionId).some(isPracticalAttachmentPreviewable)) return "success";
  if (status === "success") return "success";
  if (status === "pending" || status === "converting") return "pending";
  if (status === "failed") return "failed";
  return "";
}

function isPracticalAttachmentPreviewable(attachment) {
  if (!attachment) return false;
  // 历史图片可能没有旧 previewPath，但图片源文件本身就是可预览资源。
  if (attachment.fileKind === "IMAGE") return !!attachment.resourcePath;
  return !!attachment.previewPath && attachment.previewStatus !== "failed";
}

function canPreviewPractical(questionId, attachmentIndex = 0) {
  const attachment = getPracticalAttachments(questionId)[attachmentIndex];
  return isPracticalAttachmentPreviewable(attachment);
}

function downloadSubmittedWork(questionId, attachmentIndex = 0) {
  const attachment = getPracticalAttachments(questionId)[attachmentIndex];
  const filePath = attachment?.resourcePath;
  if (!filePath) return;
  Download.resource(filePath, `学生操作题作品_${attachment?.originalFileName || getFileName(filePath)}`);
}

// 预览作品（使用PDF预览组件，借助后端LibreOffice转换）
function previewWork(questionId, attachmentIndex = 0) {
  const attachment = getPracticalAttachments(questionId)[attachmentIndex];
  const filePath = attachment?.resourcePath;
  if (!filePath) return;
  const baseUrl = import.meta.env.VITE_APP_BASE_API;
  const previewStatus = attachment?.previewStatus || practicalPreviewStatuses.value[questionId];
  const previewPath = attachment?.previewPath || practicalPreviewPaths.value[questionId];

  // 使用后端专用的预览接口，解决特殊字符文件名导致的404问题
  // 接口地址: /common/resource/view?resource=xxx
  const previewApi = `${baseUrl}/common/resource/view?resource=`;

  if (attachment?.fileKind === "IMAGE" && filePath) {
    window.open(previewApi + encodeURIComponent(filePath), "_blank", "noopener,noreferrer");
    return;
  }
  if (previewStatus === "success" && previewPath) {
    const resourceUrl = previewApi + encodeURIComponent(previewPath);
    pdfPreviewRef.value?.open(resourceUrl);
    return;
  }

  if (previewStatus === "pending" || previewStatus === "converting") {
    ElMessage.info(
      previewStatus === "pending"
        ? "交卷已成功，预览排队中，请稍候再试或先下载原文件"
        : "交卷已成功，预览转换中，请稍候再试或先下载原文件"
    );
    return;
  }

  ElMessage.warning("交卷已成功，预览暂不可用，请下载原文件查看");
}

// 删除已上传作品
function deleteWork(questionId) {
  ElMessageBox.confirm("确定删除已上传的作品吗？删除后需重新上传", "提示", {
    type: "warning",
  }).then(() => {
    deletePracticalArtifact({
      lessonId: lessonId.value,
      questionId,
      expectedVersionId: practicalArtifacts.value[questionId]?.versionId || null,
    }).then(() => {
      clearPracticalPolling(questionId);
      delete practicalUploads.value[questionId];
      delete practicalScores.value[questionId];
      delete practicalPreviewStatuses.value[questionId];
      delete practicalPreviewPaths.value[questionId];
      delete practicalArtifacts.value[questionId];
      ElMessage.success("已删除");
    });
  });
}

function getQuestionTypeLabel(type) {
  return questionTypeLabel(type);
}

function handleCommand(cmd) {
  if (cmd === "logout") {
    ElMessageBox.confirm("确定注销并退出系统吗？", "提示").then(() => {
      userStore.logOut().then(() => {
        location.href = "/index";
      });
    });
  } else if (cmd === "password") {
    pwdDialogVisible.value = true;
  } else if (cmd === "history") {
    historyDialogVisible.value = true;
    loadHistoryScores();
  } else if (cmd === "wrong_book") {
    openWrongBook();
  }
}

// 加载历史成绩
async function loadHistoryScores() {
  historyLoading.value = true;
  try {
    const res = await getHistoryScores(historyYear.value);
    // 兼容可能的数据结构：可能是直接数组，也可能是 {code, data}
    let list = [];
    if (Array.isArray(res)) {
      list = res;
    } else if (res && Array.isArray(res.data)) {
      list = res.data;
    }
    historyList.value = list;
  } catch (err) {
    console.error("Failed to load history:", err);
    historyList.value = [];
  } finally {
    historyLoading.value = false;
  }
}

// 格式化日期时间
function formatDateTime(dateStr) {
  if (!dateStr) return "-";
  const date = new Date(dateStr);
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  const h = String(date.getHours()).padStart(2, "0");
  const min = String(date.getMinutes()).padStart(2, "0");
  return `${y}-${m}-${d} ${h}:${min}`;
}

function switchToGuideSheet() {
  if (hasGuideSheet.value) activeLearningMode.value = 'guide';
}

async function switchToDailyCourse() {
  const canLeave = await guideSheetRef.value?.ensureCanLeave?.()
  if (canLeave !== false) activeLearningMode.value = 'daily'
}

// 防误关：还有没交的作答或正在进行的打字题时，刷新/关闭页面前浏览器强制二次确认
const beforeUnloadHandler = (e) => {
  const hasPendingAnswer = Object.values(answers.value || {}).some(
    (v) => v !== null && v !== undefined && v !== ""
  );
  const hasRunningTyping = Object.values(typingStates.value || {}).some(
    (s) => s && s.started && !s.submitted
  );
  if (hasPendingAnswer || hasRunningTyping) {
    e.preventDefault();
    e.returnValue = "";
  }
};

// 老师开启题目后学生端自动出现：页面可见时每 60 秒静默重拉一次课程数据
let gatePollTimer = null;
// 是否有进行中的打字作答（已开始未提交）；此时刷新会重置计时与进度，必须跳过
function hasActiveTypingSession() {
  return Object.values(typingStates.value).some(
    (s) => s && s.started && !s.submitted
  );
}
function scheduleGatePoll() {
  if (gatePollTimer) clearTimeout(gatePollTimer);
  gatePollTimer = setTimeout(async () => {
    if (document.visibilityState === "visible" && !hasActiveTypingSession()) {
      try {
        await fetchData({ silent: true });
      } catch (e) {
        // 静默失败，下个周期再试
      }
    }
    scheduleGatePoll();
  }, 60000);
}

onMounted(() => {
  window.addEventListener("beforeunload", beforeUnloadHandler);
  fetchData();
  scheduleGatePoll();
});

onUnmounted(() => {
  window.removeEventListener("beforeunload", beforeUnloadHandler);
  if (gatePollTimer) clearTimeout(gatePollTimer);
  Object.values(timerIntervals).forEach((i) => clearInterval(i));
  Object.values(practicalPollingTimers).forEach((i) => clearTimeout(i));
  practicalAutoCommitTimers.forEach((timer) => clearTimeout(timer));
  practicalAutoCommitTimers.clear();
});
</script>

<style lang="scss" scoped>
.student-dashboard {
  background-color: #f5f7fa;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.dashboard-header {
  height: 64px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  padding: 0 32px;
  position: sticky;
  top: 0;
  z-index: 2000;
}

.dashboard-header__inner {
  width: 100%;
  max-width: 1200px;
  height: 100%;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.view-toggle {
  display: flex;
  align-items: center;
  margin-left: 4px;
}
.view-toggle .el-button {
  margin-left: 0;
  border-radius: 0;
}
.view-toggle .el-button:first-child {
  border-radius: 4px 0 0 4px;
}
.view-toggle .el-button:last-child {
  border-radius: 0 4px 4px 0;
}
.logo {
  height: 32px;
}
.platform-name {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.header-right .user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 20px;
  transition: background 0.2s;
}
.header-right .user-info:hover {
  background: #f0f2f5;
}
.user-name {
  font-weight: 500;
  font-size: 14px;
}

.loading-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.main-content {
  flex: 1;
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.attendance-panel {
  max-width: 720px;
  margin: 0 auto;
}
.attendance-card {
  border-radius: 12px;
}
.attendance-header {
  text-align: center;
  margin-bottom: 12px;
}
.attendance-title {
  margin: 12px 0 0;
  font-size: 24px;
  color: #1f2d3d;
}
.attendance-note {
  text-align: center;
  color: #303133;
  line-height: 1.6;
  margin: 8px 0 20px;
}
.attendance-note.muted {
  color: #909399;
}
.attendance-status {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding-bottom: 12px;
}
.attendance-hint {
  margin: 0;
  font-size: 13px;
  color: #909399;
}
.lesson-banner {
  margin-bottom: 24px;
}
.lesson-banner h1 {
  margin: 0 0 12px 0;
  font-size: 28px;
  color: #1f2d3d;
}

.banner-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.banner-left {
  flex: 1;
}
.banner-right {
  display: flex;
  align-items: center;
  gap: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 16px 24px;
  border-radius: 12px;
  color: #fff;
}
.course-score-box {
  text-align: center;
}
.course-score-box .score-label {
  font-size: 12px;
  opacity: 0.8;
  margin-bottom: 4px;
}
.course-score-box .score-value {
  font-size: 28px;
  font-weight: bold;
}
.course-score-box .score-value.total {
  color: #fff;
}
.course-score-box .score-value.my {
  color: #ffd700;
}
.course-score-box .score-value.pending {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}
.score-divider {
  width: 1px;
  height: 40px;
  background: rgba(255, 255, 255, 0.3);
}

.section-block {
  margin-bottom: 40px;
}

.collaboration-card {
  margin-bottom: 20px;
}
.collaboration-room-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}
.collaboration-room-row:last-child {
  border-bottom: 0;
}
.collaboration-meta {
  display: block;
  margin-top: 4px;
  color: #909399;
  font-size: 12px;
}

.section-title {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 统一的区域分数信息样式 */
.section-score-info {
  margin-left: auto;
  font-size: 14px;
  font-weight: normal;
  color: #606266;
}
.section-score-value {
  color: #67c23a;
  font-weight: bold;
}

/* 打字面板 */
.typing-panel {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  display: flex;
  overflow: hidden;
  min-height: 520px;
  margin-bottom: 20px;
}

.typing-stats {
  width: 200px;
  background: #f8f9fa;
  border-right: 1px solid #ebeef5;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* 分数显示 */
.score-display {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  padding: 16px;
  color: #fff;
  text-align: center;
}
.score-item {
  margin-bottom: 8px;
}
.score-item:last-child {
  margin-bottom: 0;
}
.score-item label {
  display: block;
  font-size: 12px;
  opacity: 0.8;
  margin-bottom: 4px;
}
.total-score {
  font-size: 24px;
  font-weight: bold;
}
.my-score {
  font-size: 20px;
  font-weight: bold;
  color: #ffd700;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #606266;
}
.stat-item label {
  color: #909399;
}
.stat-item span {
  font-weight: bold;
  color: #303133;
}
.stat-item.time span {
  font-size: 15px;
  color: #f56c6c;
}
.stat-item.highlight {
  background: #e6f7ff;
  padding: 8px;
  border-radius: 4px;
  margin: 4px -8px;
}
.stat-item.highlight span {
  color: #1890ff;
}

.stat-line {
  height: 1px;
  background: #ebeef5;
  margin: 10px 0;
}
.action-buttons {
  margin-top: auto;
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.action-btn {
  width: 100%;
  margin: 0 !important;
}
.error-text {
  color: #f56c6c !important;
}
.success-text {
  color: #67c23a !important;
}

.typing-area {
  flex: 1;
  padding: 24px;
  display: flex;
  flex-direction: column;
  background-color: #fcfcfc;
}

.typing-status-bar {
  margin-bottom: 16px;
}

.original-text-box {
  flex: 1;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 16px;
  margin-bottom: 16px;
  overflow-y: auto;
}
.box-label {
  font-weight: bold;
  color: #909399;
  margin-bottom: 12px;
  font-size: 13px;
}
.text-content {
  font-size: 18px;
  line-height: 2;
  letter-spacing: 1px;
  color: #c00;
  /* 防止学生选中、复制、拖拽原文 */
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  pointer-events: none;
  pointer-events: none;
  font-family: Consolas, "Courier New", monospace, "Microsoft YaHei"; /* 使用等宽字体确保对齐 */
  word-break: break-all; /* 强制换行策略一致 */
  white-space: pre-wrap; /* 保留空白符 */
  
  /* 盒模型与输入框完全一致 */
  box-sizing: border-box;
  padding: 5px 15px; /* 与el-textarea默认一致 */
  border: 1px solid transparent; 
  width: 100%;
}

.char-pending {
  color: #606266;
}
.char-correct {
  color: #67c23a;
}
.char-error {
  color: #f56c6c;
  background: #fef0f0;
}

.input-box {
  display: flex;
  flex-direction: column;
  gap: 8px;
  /* 补偿 .original-text-box 的 padding(16px) + border(1px) = 17px，确保宽度一致 */
  padding: 0 17px;
}
.input-box label {
  font-weight: bold;
  color: #303133;
}

/* 打字输入框与原文字体对齐 */
.typing-input :deep(textarea) {
  font-size: 18px !important;
  line-height: 2 !important;
  letter-spacing: 1px !important;
  font-family: Consolas, "Courier New", monospace, "Microsoft YaHei" !important;
  word-break: break-all !important;
  padding: 5px 15px !important; /* 统一 Padding */
  box-sizing: border-box !important;
}

/* 理论测试 */
.theory-score-info {
  margin-left: auto;
  font-size: 14px;
  font-weight: normal;
  color: #606266;
}
.theory-score-value {
  color: #67c23a;
  font-weight: bold;
  font-size: 16px;
}

.theory-grid {
  display: grid;
  gap: 20px;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  margin-bottom: 20px;
}

.theory-card {
  border-radius: 8px;
  transition: border-color 0.3s;
}
.theory-card:hover {
  border-color: #409eff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.badge {
  background: #ecf5ff;
  color: #409eff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}
.score {
  color: #e6a23c;
  font-weight: bold;
}

.question-stem {
  font-size: 15px;
  color: #303133;
  margin: 12px 0 20px 0;
  line-height: 1.5;
  font-weight: 500;
}

.options-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.option-radio {
  display: flex;
  align-items: center;
  padding: 10px 14px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}
.option-radio:hover {
  background: #f5f7fa;
}
.option-radio.active {
  border-color: #409eff;
  background: #ecf5ff;
  color: #409eff;
}
.opt-label {
  font-weight: bold;
  margin-right: 10px;
}

.header-right-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.result-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}
.result-tag.correct {
  background: #f0f9eb;
  color: #67c23a;
}
.result-tag.wrong {
  background: #fef0f0;
  color: #f56c6c;
}

/* 判断题禁用状态下保持选中高亮 - 增强选择器权重 */
.audit-group :deep(.el-radio.is-disabled.is-checked .el-radio__inner) {
  background-color: #409eff !important;
  border-color: #409eff !important;
}
.audit-group :deep(.el-radio.is-disabled.is-checked .el-radio__label) {
  color: #409eff !important;
}
.audit-group :deep(.el-radio.is-disabled.is-bordered.is-checked) {
  border-color: #409eff !important;
  background-color: #ecf5ff !important;
}
.audit-group :deep(.el-radio.is-disabled.is-bordered.is-checked::after) {
  display: none !important; /* 移除可能存在的禁用遮罩 */
}

.submit-theory-bar {
  text-align: center;
  margin-top: 20px;
}

/* 操作题样式 */
.practical-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.practical-card {
  border-radius: 8px;
}

.flowchart-question-list { display: grid; gap: 14px; margin-bottom: 16px; }
.flowchart-card { border-color: #b9def0; }
.flowchart-badge { background: linear-gradient(135deg, #1597bb, #36b37e); color: #fff; }
.flowchart-entry { display: flex; align-items: center; justify-content: space-between; gap: 20px; padding: 16px; margin-top: 12px; border-radius: 12px; background: linear-gradient(135deg, #ecf8ff, #f0f9eb); }
.flowchart-entry strong { color: #24526d; font-size: 16px; }
.flowchart-entry p { margin: 6px 0 0; color: #61788b; font-size: 13px; }
@media (max-width: 760px) { .flowchart-entry { align-items: stretch; flex-direction: column; } }

.material-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 16px 0;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.material-label,
.upload-label {
  font-weight: 500;
  color: #606266;
}

.material-name {
  color: #409eff;
  flex: 1;
}

.material-files {
  flex: 1;
}

.material-file-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 4px 0;
}

.upload-section {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 16px;
}

.practical-draft-list,
.practical-attachment-list {
  min-width: 360px;
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fafafa;
}

.draft-file,
.attachment-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 6px;
}

.upload-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

.uploaded-file {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  background: #f0f9eb;
  border: 1px solid #c2e7b0;
  border-radius: 4px;
}

.uploaded-file .el-icon {
  font-size: 20px;
  color: #67c23a;
}

.file-name {
  color: #67c23a;
  font-weight: 500;
}

.uploaded-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.preview-state {
  font-size: 12px;
  color: #909399;
}

.preview-state.success {
  color: #67c23a;
}

.preview-state.pending {
  color: #e6a23c;
}

.preview-state.failed {
  color: #f56c6c;
}

/* 上传/转换中状态 */
.uploading-status {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #e6f7ff;
  border: 1px solid #91d5ff;
  border-radius: 4px;
  color: #1890ff;
}
.uploading-status .el-icon {
  font-size: 18px;
}

/* 批阅状态样式 */
.score-status {
  font-weight: bold;
}
.score-status .scored {
  color: #67c23a;
}
.score-status .pending {
  color: #e6a23c;
  font-size: 12px;
  background: #fdf6ec;
  padding: 2px 8px;
  border-radius: 4px;
}
.score-status .not-submitted {
  color: #909399;
}

/* 历史成绩弹窗 */
.history-header {
  margin-bottom: 16px;
}
.score-success {
  color: #67c23a;
  font-weight: bold;
}

.header-right {
  display: flex;
  align-items: center;
}
.header-actions {
  display: flex;
  align-items: center;
  margin-right: 16px;
}
.header-divider {
  margin-right: 16px;
  height: 20px;
}
</style>
/* 学生实验工具面板（对话框挂 body，需全局样式） */
.gate-tip {
  margin-bottom: 14px;
}
.student-tool-dialog .tool-group {
  margin-bottom: 16px;
}
.student-tool-dialog .tool-group:last-child {
  margin-bottom: 0;
}
.student-tool-dialog .tool-group-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 14px;
  color: #303133;
  margin-bottom: 8px;
}
.student-tool-dialog .tool-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px 12px;
}
.student-tool-dialog .tool-item {
  padding: 6px 10px;
  background: #f5f7fa;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.student-tool-dialog .tool-item .el-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
}
.student-tool-dialog .student-tool-empty {
  padding: 10px 0;
}
@media (max-width: 600px) {
  .student-tool-dialog .tool-grid {
    grid-template-columns: 1fr;
  }
}

<style lang="scss" scoped>
/* 错题本相关 */
.wrong-book-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.wrong-stats {
  font-size: 14px;
  color: #909399;
}
.wrong-card {
  margin-bottom: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    .badge {
      background: #f0f2f5;
      padding: 2px 8px;
      border-radius: 4px;
      color: #606266;
    }
  }
}
.options-view {
  margin: 15px 0;
  .opt-item {
    padding: 8px 12px;
    margin-bottom: 8px;
    border-radius: 4px;
    border: 1px solid #e4e7ed;
    background: #fff;

    &.correct-opt {
      background-color: #f0f9eb;
      border-color: #67c23a;
      color: #67c23a;
      font-weight: bold;
    }

    &.wrong-opt {
      background-color: #fef0f0;
      border-color: #f56c6c;
      color: #f56c6c;
    }
  }
}
.answer-analysis {
  background: #fbfbfb;
  padding: 15px;
  border-radius: 4px;
  margin-top: 15px;

  .my-answer {
    margin-bottom: 10px;
    font-weight: bold;
    .wrong-text {
      color: #f56c6c;
    }
    .correct-text {
      color: #67c23a;
    }
  }

  .analysis-box {
    .label {
      font-weight: bold;
      margin-bottom: 5px;
      color: #303133;
    }
    .content {
      color: #606266;
      line-height: 1.6;
    }
  }
}

/* 交互式选项样式 */
.options-interactive {
  margin: 15px 0;

  .opt-btn {
    padding: 12px 16px;
    margin-bottom: 10px;
    border-radius: 8px;
    border: 2px solid #e4e7ed;
    background: #fff;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      border-color: #409eff;
      background: #ecf5ff;
    }

    &.selected {
      border-color: #409eff;
      background: #ecf5ff;
      font-weight: bold;
    }

    &.correct-result {
      border-color: #67c23a !important;
      background: #f0f9eb !important;
      color: #67c23a;
      font-weight: bold;
    }

    &.wrong-result {
      border-color: #f56c6c !important;
      background: #fef0f0 !important;
      color: #f56c6c;
      text-decoration: line-through;
    }
  }
}

.action-area {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px dashed #e4e7ed;
}

.result-correct {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: #67c23a;
  font-weight: bold;
  font-size: 16px;
}

.result-wrong {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: #f56c6c;
  font-weight: bold;
  font-size: 16px;
}

@media (max-width: 768px) {
  .dashboard-header {
    height: auto;
    min-height: 64px;
    padding: 8px 12px;
  }
  .dashboard-header__inner {
    flex-wrap: wrap;
    gap: 8px;
  }
  .header-left {
    width: 100%;
    min-width: 0;
    gap: 8px;
  }
  .logo {
    height: 28px;
  }
  .platform-name {
    overflow: hidden;
    max-width: 96px;
    font-size: 14px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .view-toggle {
    margin-left: auto;
  }
  .view-toggle :deep(.el-button) {
    padding-right: 9px;
    padding-left: 9px;
  }
  .header-right {
    display: flex;
    width: 100%;
    align-items: center;
    justify-content: flex-end;
    border-top: 1px solid #edf0f2;
    padding-top: 6px;
  }
  .header-actions {
    display: flex;
    min-width: 0;
  }
  .header-actions :deep(.el-button) {
    margin-left: 8px;
  }
  .header-divider {
    margin: 0 8px;
  }
  .header-right .user-info {
    padding: 4px;
  }
  .user-name {
    display: none;
  }
  .main-content {
    padding: 12px;
  }
  .banner-content {
    align-items: stretch;
    flex-direction: column;
    gap: 12px;
  }
  .lesson-banner h1 {
    font-size: 22px;
  }
}

@media (max-width: 420px) {
  .platform-name {
    display: none;
  }
  .view-toggle :deep(.el-button) {
    font-size: 12px;
  }
}
</style>
