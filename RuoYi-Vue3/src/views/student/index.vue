<template>
  <div class="student-dashboard">
    <!-- 顶部导航栏 -->
    <header class="dashboard-header">
      <div class="header-left">
        <img src="@/assets/logo/logo.png" class="logo" alt="Logo" />
        <span class="platform-name">智慧课堂 - 学生端</span>
      </div>
      <div class="header-right">
        <div class="header-actions">
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
    </header>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading"><Loading /></el-icon>
      <p>正在加载课程内容...</p>
    </div>

    <!-- 主内容区 -->
    <main v-else class="main-content">
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
                >{{ studentInfo.gradeName
                }}{{ studentInfo.classCode }}班</el-tag
              >
              <el-tag type="warning" effect="dark">{{
                studentInfo.studentName || "同学"
              }}</el-tag>
            </p>
          </div>
          <div class="banner-right">
            <div class="course-score-box">
              <div class="score-label">课程总分</div>
              <div class="score-value total">{{ courseTotalScore }}</div>
            </div>
            <div class="score-divider"></div>
            <div class="course-score-box">
              <div class="score-label">我的得分</div>
              <div
                class="score-value my"
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

      <div v-else class="task-container">
        <!-- 1. 打字练习区域 -->
        <div v-if="typingQuestions.length > 0" class="section-block">
          <div class="section-title">
            <el-icon><Monitor /></el-icon> 打字练习
            <span class="section-score-info">
              总分: {{ typingTotalScore }}分
              <template v-if="typingMyScore !== null">
                | 得分:
                <span class="section-score-value">{{ typingMyScore }}分</span>
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
                    typingStates[q.questionId]?.submitted
                  "
                >
                  提交打字成绩
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
                <div class="text-content">
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
              总分: {{ theoryTotalScore }}分
              <template v-if="theorySubmitted">
                | 得分:
                <span class="section-score-value">{{ theoryScore }}分</span>
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
                  <span class="score">{{ q.questionScore }}分</span>
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
                  @click="answers[q.questionId] = opt"
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
                <el-radio-group v-model="answers[q.questionId]">
                  <el-radio label="对" border>正确</el-radio>
                  <el-radio label="错" border>错误</el-radio>
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
            <span class="section-score-info">
              总分: {{ practicalTotalScore }}分
              <template v-if="practicalMyScore !== null">
                | 得分:
                <span class="section-score-value"
                  >{{ practicalMyScore }}分</span
                >
              </template>
            </span>
          </div>
          <div class="practical-list">
            <el-card
              v-for="(q, index) in practicalQuestions"
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
                      <span class="scored"
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
                {{ index + 1 }}. {{ q.questionContent }}
              </div>

              <!-- 素材文件下载 -->
              <div v-if="q.filePath" class="material-section">
                <span class="material-label">素材文件：</span>
                <span class="material-name">{{ getFileName(q.filePath) }}</span>
                <el-button
                  type="primary"
                  size="small"
                  icon="Download"
                  @click="downloadMaterial(q.filePath)"
                >
                  下载素材
                </el-button>
              </div>

              <!-- 作品上传区域 -->
              <div class="upload-section">
                <span class="upload-label">提交作品：</span>
                <el-upload
                  v-if="!practicalUploads[q.questionId]"
                  class="work-uploader"
                  :action="uploadUrl"
                  :headers="uploadHeaders"
                  :on-success="(res) => handleUploadSuccess(q.questionId, res)"
                  :on-error="handleUploadError"
                  :show-file-list="false"
                  accept=".docx,.doc,.pdf,.pptx,.ppt,.xlsx,.xls"
                >
                  <el-button type="success" icon="Upload">上传作品</el-button>
                </el-upload>

                <!-- 正在上传/转换中 -->
                <div
                  v-else-if="uploadingQuestionId === q.questionId"
                  class="uploading-status"
                >
                  <el-icon class="is-loading"><Loading /></el-icon>
                  <span>正在转换中，请稍候...</span>
                </div>

                <!-- 已上传文件展示 -->
                <div v-else class="uploaded-file">
                  <el-icon><Document /></el-icon>
                  <span class="file-name">{{
                    getFileName(practicalUploads[q.questionId])
                  }}</span>
                  <el-button-group>
                    <el-button
                      type="primary"
                      size="small"
                      icon="View"
                      @click="previewWork(q.questionId)"
                      >预览</el-button
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
              </div>
            </el-card>
          </div>
        </div>
      </div>
    </main>

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
        <el-table-column
          prop="practicalScore"
          label="操作"
          width="80"
          align="center"
        />
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
                  selected: wrongAnswers[q.questionId] === 'true',
                  'correct-result':
                    wrongSubmitted[q.questionId] && q.answer === 'true',
                  'wrong-result':
                    wrongSubmitted[q.questionId] &&
                    wrongAnswers[q.questionId] === 'true' &&
                    q.answer !== 'true',
                }"
                @click="selectWrongAnswer(q.questionId, 'true')"
              >
                正确
              </div>
              <div
                class="opt-btn"
                :class="{
                  selected: wrongAnswers[q.questionId] === 'false',
                  'correct-result':
                    wrongSubmitted[q.questionId] && q.answer === 'false',
                  'wrong-result':
                    wrongSubmitted[q.questionId] &&
                    wrongAnswers[q.questionId] === 'false' &&
                    q.answer !== 'false',
                }"
                @click="selectWrongAnswer(q.questionId, 'false')"
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
                <div class="label">正确答案：{{ q.answer }}</div>
                <div class="content">{{ q.analysis || "暂无解析" }}</div>
              </div>
            </div>
          </el-card>
        </div>
      </el-scrollbar>
    </el-dialog>

    <!-- PDF预览组件 -->
    <pdf-preview ref="pdfPreviewRef" />
  </div>
</template>

<script setup name="StudentIndex">
import { ref, computed, onMounted, onUnmounted, nextTick } from "vue";
import {
  getCurrentLesson,
  submitAnswers as submitAnswersApi,
  getHistoryScores,
  getWrongQuestions,
} from "@/api/business/studentHome";
import { updateUserPwd } from "@/api/system/user";
import useUserStore from "@/store/modules/user";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import PdfPreview from "@/components/PdfPreview/index.vue";

// PDF预览组件引用
const pdfPreviewRef = ref(null);

const router = useRouter();
const userStore = useUserStore();

const loading = ref(true);
const hasLesson = ref(false);
const lessonId = ref(null);
const lessonTitle = ref("");
const allQuestions = ref([]);
const lessonConfig = ref({
  shuffleMode: 0,
  randomChoiceCount: 0,
  randomJudgmentCount: 0,
});
const studentInfo = ref({});

// 确定性随机：使用 seed 生成固定随机序列
function seededRandom(seed) {
  const x = Math.sin(seed) * 10000;
  return x - Math.floor(x);
}

function seededShuffle(array, seed) {
  const result = [...array];
  for (let i = result.length - 1; i > 0; i--) {
    seed = seed * 1103515245 + 12345;
    const j = Math.floor(Math.abs(seed % (i + 1)));
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

// 加载错题
async function loadWrongQuestions() {
  wrongLoading.value = true;
  // 重置练习状态
  wrongAnswers.value = {};
  wrongSubmitted.value = {};
  wrongResults.value = {};
  try {
    const res = await getWrongQuestions(selectedWrongLessonId.value);
    console.log("Wrong questions data:", res);
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
function selectWrongAnswer(questionId, answer) {
  // 如果已提交且正确，不允许修改
  if (
    wrongSubmitted.value[questionId] &&
    wrongResults.value[questionId] === "correct"
  ) {
    return;
  }
  wrongAnswers.value[questionId] = answer;
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
  if (userAnswer === question.answer) {
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
const theoryQuestions = computed(() =>
  allQuestions.value.filter((q) =>
    ["choice", "judgment"].includes(q.questionType)
  )
);

// 理论测试总分
const theoryTotalScore = computed(() => {
  return theoryQuestions.value.reduce(
    (sum, q) => sum + (q.questionScore || 0),
    0
  );
});

// 操作题
const practicalQuestions = computed(() =>
  allQuestions.value.filter((q) => q.questionType === "practical")
);
const practicalUploads = ref({}); // { questionId: uploadedFilePath }
const practicalScores = ref({}); // { questionId: score | null } - null表示未批阅
const submittedAnswers = ref({}); // 学生已提交的答案 { questionId: { answer, score } }
const uploadingQuestionId = ref(null); // 正在上传/转换的题目ID（用于显示loading）

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
    const score = practicalScores.value[q.questionId];
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
    const score = practicalScores.value[q.questionId];
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
const uploadUrl = import.meta.env.VITE_APP_BASE_API + "/common/upload";
const uploadHeaders = computed(() => ({
  Authorization: "Bearer " + userStore.token,
}));

// 加载数据
async function fetchData() {
  loading.value = true;
  try {
    const res = await getCurrentLesson();
    hasLesson.value = res.hasLesson || false;
    if (res.hasLesson) {
      lessonId.value = res.lessonId;
      lessonTitle.value = res.lessonTitle;
      
      // 保存课程随机配置
      lessonConfig.value = {
        shuffleMode: res.shuffleMode ?? 0,
        randomChoiceCount: res.randomChoiceCount ?? 0,
        randomJudgmentCount: res.randomJudgmentCount ?? 0,
      };
      
      // 应用随机逻辑
      const rawQuestions = res.questions || [];
      const studentId = res.studentInfo?.studentId || 0;
      allQuestions.value = applyRandomShuffle(
        rawQuestions, 
        lessonConfig.value, 
        studentId, 
        res.lessonId
      );
      
      studentInfo.value = res.studentInfo || {};
      submittedAnswers.value = res.submittedAnswers || {};
      initTypingStates();
      initPracticalStates(); // 初始化操作题状态
      initTheoryState(); // 初始化理论测试状态（检查是否已提交）
    }
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
}

// 初始化操作题状态（加载已提交的作品）
function initPracticalStates() {
  practicalQuestions.value.forEach((q) => {
    const submitted = submittedAnswers.value[q.questionId];
    if (submitted && submitted.answer) {
      practicalUploads.value[q.questionId] = submitted.answer;
      practicalScores.value[q.questionId] = submitted.score; // null表示未批阅
    }
  });
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
        answers.value[q.questionId] = submitted.answer;
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
  if (!state || state.submitted) return;

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

  console.log("=== 前端提交调试 Start ===");
  console.log("题目ID:", qid);
  console.log("提交内容:", answers.value[qid]);
  console.log("实际耗时:", timeSpent, "秒");
  console.log("当前统计状态:", JSON.parse(JSON.stringify(state)));
  console.log("=== 前端提交调试 End ===");

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
      state.myScore = res.totalScore || 0;
      ElMessage.success(`打字成绩已自动提交！得分: ${state.myScore}分`);
    })
    .catch(() => {
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

  const ids = theoryQuestions.value.map((q) => q.questionId);
  const submitData = {};
  let answeredCount = 0;

  ids.forEach((id) => {
    if (answers.value[id]) {
      submitData[id] = answers.value[id];
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
function downloadMaterial(filePath) {
  if (!filePath) return;
  const baseUrl = import.meta.env.VITE_APP_BASE_API;
  const fullUrl = baseUrl + filePath;
  // 使用a标签下载
  const link = document.createElement("a");
  link.href = fullUrl;
  link.download = getFileName(filePath);
  link.target = "_blank";
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

// 上传成功处理
function handleUploadSuccess(questionId, res) {
  if (res.code === 200) {
    const filePath = res.fileName;
    practicalUploads.value[questionId] = filePath;
    practicalScores.value[questionId] = null; // 刚上传，未批阅

    // 设置loading状态（后端会进行LibreOffice转换，需要等待）
    uploadingQuestionId.value = questionId;

    // 提交到后端保存（会触发异步LibreOffice转换）
    submitAnswersApi({
      lessonId: lessonId.value,
      answers: { [questionId]: filePath },
    })
      .then(() => {
        // 后端返回成功只表示已触发转换，实际转换需要10-15秒
        // 延迟12秒后再显示预览按钮，确保转换完成
        ElMessage.info("作品已上传，正在后台转换为预览格式，请稍候...");
        setTimeout(() => {
          uploadingQuestionId.value = null;
          ElMessage.success("转换完成，可以预览了！");
        }, 3000);
      })
      .catch(() => {
        ElMessage.warning("作品已上传，但保存到服务器失败，请重试");
        uploadingQuestionId.value = null;
      });
  } else {
    ElMessage.error(res.msg || "上传失败");
  }
}

function handleUploadError() {
  ElMessage.error("上传失败，请重试");
}

// 预览作品（使用PDF预览组件，借助后端LibreOffice转换）
function previewWork(questionId) {
  const filePath = practicalUploads.value[questionId];
  if (!filePath) return;
  const baseUrl = import.meta.env.VITE_APP_BASE_API;

  // 对于docx文件，需要查找对应的previewPath（LibreOffice转换后的PDF路径）
  const question = practicalQuestions.value.find(
    (q) => q.questionId === questionId
  );
  const fileName = filePath.toLowerCase();

  // 使用后端专用的预览接口，解决特殊字符文件名导致的404问题
  // 接口地址: /common/resource/view?resource=xxx
  const previewApi = `${baseUrl}/common/resource/view?resource=`;

  if (fileName.endsWith(".pdf")) {
    // PDF直接使用预览组件
    const resourceUrl = previewApi + encodeURIComponent(filePath);
    console.log("【Preview】PDF URL:", resourceUrl);
    pdfPreviewRef.value?.open(resourceUrl);
  } else if (fileName.endsWith(".docx") || fileName.endsWith(".doc")) {
    // Office文件：使用转换后的PDF路径预览
    const pdfPath = filePath.replace(/\.docx?$/i, ".pdf");
    const resourceUrl = previewApi + encodeURIComponent(pdfPath);
    console.log("【Preview】Converted PDF URL:", resourceUrl);
    pdfPreviewRef.value?.open(resourceUrl);
  } else {
    // 其他文件尝试直接预览
    const resourceUrl = previewApi + encodeURIComponent(filePath);
    window.open(resourceUrl, "_blank");
  }
}

// 删除已上传作品
function deleteWork(questionId) {
  ElMessageBox.confirm("确定删除已上传的作品吗？删除后需重新上传", "提示", {
    type: "warning",
  }).then(() => {
    // 从后端删除记录
    submitAnswersApi({
      lessonId: lessonId.value,
      answers: { [questionId]: "" }, // 空字符串表示删除
    }).then(() => {
      delete practicalUploads.value[questionId];
      delete practicalScores.value[questionId];
      ElMessage.success("已删除");
    });
  });
}

function getQuestionTypeLabel(type) {
  return { choice: "选择题", judgment: "判断题" }[type] || type;
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
    console.log("History data:", res);
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

onMounted(() => {
  fetchData();
});

onUnmounted(() => {
  Object.values(timerIntervals).forEach((i) => clearInterval(i));
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
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
}
.input-box label {
  font-weight: bold;
  color: #303133;
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

.upload-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
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
</style>
