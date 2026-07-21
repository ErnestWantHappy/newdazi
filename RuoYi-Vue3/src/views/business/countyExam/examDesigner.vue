<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 左侧：抽测组卷配置区 -->
      <el-col :span="10" :xs="24">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>抽测组卷配置</span>
              <el-tag type="info" size="small">{{ examName }}</el-tag>
            </div>
          </template>
          <el-form ref="examRef" :model="form" :rules="rules" label-width="80px">
            <el-row>
              <el-col :span="12">
                <el-form-item label="年级" prop="grade">
                  <el-select v-model="form.grade" placeholder="请选择年级" style="width:100%">
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
                  <el-select v-model="form.semester" placeholder="请选择学期" style="width:100%">
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
          <h4 :style="{ color: totalScore === 100 ? '#67C23A' : '#F56C6C' }">
            已选题目列表 (当前总分: {{ totalScore }} / 100)
            <span v-if="totalScore !== 100" style="font-size: 12px; font-weight: normal; margin-left: 10px;">
              (还差 {{ 100 - totalScore }} 分)
            </span>
            <span v-else style="font-size: 12px; font-weight: normal; margin-left: 10px;">
              (已达标)
            </span>
          </h4>
          <div v-if="hasInconsistentScores" style="color: #F56C6C; font-size: 12px; margin-bottom: 10px;">
            随机抽题模式要求同一题型分值一致，否则不同学生的试卷总分会发生变化，当前配置不能保存。
          </div>
          
          <!-- 批量改分工具栏 -->
          <div class="batch-toolbar" style="margin-bottom: 10px; display: flex; align-items: center; gap: 10px; background: #f8f9fa; padding: 10px; border-radius: 4px;">
            <span style="font-size: 14px; font-weight: bold; color: #606266;">批量设置分数：</span>
            <el-select v-model="batchScoreType" placeholder="选择题型" style="width: 140px" size="small">
              <el-option :label="`选择题 (${choiceCount}题)`" value="choice" />
              <el-option :label="`判断题 (${judgmentCount}题)`" value="judgment" />
            </el-select>
            <el-input-number v-model="batchScoreValue" :min="0" :max="100" size="small" controls-position="right" style="width: 100px" />
            <span style="font-size: 14px; color: #606266;">分</span>
            <el-button type="primary" size="small" @click="applyBatchScore">应 用</el-button>
          </div>
          <el-table :data="selectedQuestions" row-key="questionId" style="width: 100%">
            <template #header v-if="selectedQuestions.length === 0 || selectedQuestions.length > 1">
              <div style="padding: 5px; background: #e6f7ff; color: #0050b3; font-size: 12px;">
                当前已选 {{ selectedQuestions.length }} 道题目
              </div>
            </template>
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
                  <div v-if="scope.row.scoringItems && scope.row.scoringItems.length > 0">
                    <span class="scoring-label">评分标准：</span>
                    <span v-for="(item, idx) in scope.row.scoringItems" :key="item?.itemId || idx" class="scoring-item">
                      <template v-if="item">{{ item.itemName }}({{ item.itemScore }}%){{ idx < scope.row.scoringItems.length - 1 ? ' / ' : '' }}</template>
                    </span>
                  </div>
                  <div v-else class="no-scoring">暂无评分标准</div>
                </div>
                <!-- 异常处理：未知题型 -->
                <div v-else class="unknown-type-error" style="color: #F56C6C; background: #fef0f0; padding: 5px; margin-top: 5px; border-radius: 4px;">
                   ⚠️ 题目数据异常或原题已被删除 (ID: {{ scope.row.questionId }})
                </div>
              </template>
            </el-table-column>
            <el-table-column label="题型" align="center" width="100">
               <template #default="scope">
                  <dict-tag :options="biz_question_type" :value="scope.row.questionType"/>
               </template>
            </el-table-column>
            <el-table-column label="分值" align="center" width="120">
              <template #default="scope">
                <el-input-number v-model="scope.row.questionScore" :min="0" :max="100" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="140">
              <template #default="scope">
                <el-button
                  v-if="scope.row.questionType === 'practical' && scope.row.previewPath"
                  link
                  type="success"
                  @click="handlePreviewFile(scope.row)"
                >预览</el-button>
                <el-button link type="danger" @click="handleRemoveQuestion(scope.row)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧：题库选题区 -->
      <el-col :span="14" :xs="24">
        <el-card>
           <template #header>
             <div class="card-header">
               <span>题库选题区</span>
             </div>
           </template>
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
             <el-table-column label="操作" align="center" width="100">
               <template #default="scope">
                 <el-button
                   v-if="scope.row.questionType === 'practical' && scope.row.previewPath"
                   link
                   type="success"
                   @click="handlePreviewFile(scope.row)"
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
        </el-card>
      </el-col>
    </el-row>

    <div class="footer-toolbar">
      <el-button type="primary" @click="submitForm">保 存</el-button>
      <el-button @click="router.push('/county-exam')">返回抽测列表</el-button>
    </div>

    <pdf-preview ref="pdfPreviewRef" />
  </div>
</template>

<script setup name="CountyExamDesigner">
// 区域抽测组卷设计器 — 复制自教师端 designer.vue 并改造
// 改造点：移除课程标题/第几课/指派班级，对接 countyExam API
import { ref, computed, onMounted, getCurrentInstance } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getCountyExam, updateCountyExam, saveCountyExamQuestions } from '@/api/business/countyExam';
import { listQuestion } from '@/api/business/question';
import { listScoringItems } from '@/api/business/scoringItem';
import PdfPreview from '@/components/PdfPreview/index.vue';

const { proxy } = getCurrentInstance();
const route = useRoute();
const router = useRouter();

const { biz_grade, biz_semester, biz_question_type } = proxy.useDict("biz_grade", "biz_semester", "biz_question_type");

const loading = ref(true);
const total = ref(0);
const pdfPreviewRef = ref(null);
const examName = ref('');  // 用于顶部显示抽测名称
const examId = ref(null);

const form = ref({
  grade: null,
  semester: null,
  shuffleMode: 0,
  randomChoiceCount: 0,
  randomJudgmentCount: 0,
});
const selectedQuestions = ref([]);

const questionBankList = ref([]);
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  questionContent: null,
  grade: null,
  semester: null,
  isPublic: null,
  questionType: null,
  lessonNum: null,
  orderByColumn: 'createTime',
  isAsc: 'desc',
});

const rules = {
  grade: [{ required: true, message: "年级不能为空", trigger: "change" }],
  semester: [{ required: true, message: "学期不能为空", trigger: "change" }]
};

// 总分计算（与教师端逻辑一致）
const totalScore = computed(() => {
  const choices = selectedQuestions.value.filter(q => q.questionType === 'choice');
  const judgments = selectedQuestions.value.filter(q => q.questionType === 'judgment');
  const others = selectedQuestions.value.filter(q => q.questionType !== 'choice' && q.questionType !== 'judgment');

  let score = 0;
  score += others.reduce((sum, q) => sum + (q.questionScore || 0), 0);

  if (form.value.shuffleMode === 2 && form.value.randomChoiceCount > 0) {
    const count = Math.min(form.value.randomChoiceCount, choices.length);
    score += choices.slice(0, count).reduce((sum, q) => sum + (q.questionScore || 0), 0);
  } else {
    score += choices.reduce((sum, q) => sum + (q.questionScore || 0), 0);
  }

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
  
  if (form.value.randomChoiceCount > 0 && choices.length > 0 && !isConsistent(choices)) return true;
  if (form.value.randomJudgmentCount > 0 && judgments.length > 0 && !isConsistent(judgments)) return true;
  
  return false;
});

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
    return 0;
  });
  
  selectedQuestions.value.forEach((q, index) => {
    q.orderNum = index + 1;
  });
}

// 提交表单 — 先更新抽测配置，再保存题目
function submitForm() {
  proxy.$refs["examRef"].validate(valid => {
    if (valid) {
      if (totalScore.value !== 100) {
        proxy.$modal.msgError(`当前总分为 ${totalScore.value} 分，必须凑满 100 分才能保存！`);
        return;
      }
      if (hasInconsistentScores.value) {
        proxy.$modal.msgError('随机抽题模式下，同一题型的分值必须一致！');
        return;
      }

      sortQuestions();
      
      // 1. 更新抽测的出题模式配置
      const examData = {
        examId: examId.value,
        shuffleMode: form.value.shuffleMode,
        randomChoiceCount: form.value.randomChoiceCount,
        randomJudgmentCount: form.value.randomJudgmentCount,
        totalScore: totalScore.value,
      };

      // 2. 构造题目数据
      const questionsPayload = selectedQuestions.value
        .sort((a, b) => Number(a.orderNum || 0) - Number(b.orderNum || 0))
        .map((item, index) => ({
          questionId: item.questionId,
          questionScore: Number(item.questionScore || 0),
          orderNum: index + 1,
        }));

      // 先更新抽测配置，再保存题目
      updateCountyExam(examData).then(() => {
        return saveCountyExamQuestions(examId.value, questionsPayload);
      }).then(() => {
        proxy.$modal.msgSuccess("组卷已保存");
        router.push('/county-exam');
      });
    }
  });
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
    if (row.questionType === 'practical') {
        const hasPractical = selectedQuestions.value.some(q => q.questionType === 'practical');
        if (hasPractical) {
            proxy.$modal.msgError('一次抽测最多只能添加一道操作题。');
            return;
        }
    }
    if (row.questionType === 'typing') {
        const hasTyping = selectedQuestions.value.some(q => q.questionType === 'typing');
        if (hasTyping) {
            proxy.$modal.msgError('一次抽测最多只能添加一道打字题。');
            return;
        }
    }
    if (!isQuestionSelected(row.questionId)) {
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
            typingDuration: row.typingDuration,
            wordCount: row.wordCount,
            scoringItems: row.scoringItems || [],
        };
        selectedQuestions.value.push(newQuestion);
        sortQuestions();
        proxy.$modal.msgSuccess("已添加");
    }
}

function handleRemoveQuestion(row) {
  const index = selectedQuestions.value.findIndex(q => q.questionId === row.questionId);
  if (index > -1) {
    selectedQuestions.value.splice(index, 1);
  }
}

function handlePreviewFile(row) {
  if (pdfPreviewRef.value && row.previewPath) {
    const baseUrl = import.meta.env.VITE_APP_BASE_API;
    const fullPdfUrl = baseUrl + row.previewPath;
    pdfPreviewRef.value.open(fullPdfUrl);
  } else {
    proxy.$modal.msgError("没有可预览的PDF文件。");
  }
}

// 批量设置分数
const batchScoreType = ref('choice');
const batchScoreValue = ref(5);

const choiceCount = computed(() => selectedQuestions.value.filter(q => q.questionType === 'choice').length);
const judgmentCount = computed(() => selectedQuestions.value.filter(q => q.questionType === 'judgment').length);

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

// 初始化 — 通过路由参数加载抽测数据
function initialize() {
  const id = route.params.examId;
  if (!id) {
    proxy.$modal.msgError("缺少抽测ID参数");
    router.push('/county-exam');
    return;
  }
  
  examId.value = id;
  
  getCountyExam(id).then(response => {
    const data = response.data || {};
    const exam = data.exam || data;
    
    examName.value = exam.examName || '';
    
    form.value = {
      grade: exam.examGrade || null,
      semester: getDefaultSemester(),
      shuffleMode: exam.shuffleMode ?? 0,
      randomChoiceCount: exam.randomChoiceCount ?? 0,
      randomJudgmentCount: exam.randomJudgmentCount ?? 0,
    };
    
    // 加载已有题目
    selectedQuestions.value = (data.questions || []).map((item, index) => ({
      ...item,
      questionScore: item.questionScore != null ? item.questionScore : 0,
      orderNum: item.orderNum != null ? item.orderNum : index + 1,
    }));
    
    sortQuestions();
    getQuestionList();
  });
}

onMounted(() => {
  initialize();
});
</script>


<style scoped>
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
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  padding-bottom: 80px; /* 防止底部工具栏遮挡内容 */
}
</style>
