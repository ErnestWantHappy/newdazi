<template>
  <div class="app-container">
    <el-radio-group v-model="bankView" class="bank-view-tabs" @change="changeBankView">
      <el-radio-button label="ALL">全部题目</el-radio-button>
      <el-radio-button label="COMMON">常规题库</el-radio-button>
      <el-radio-button label="PYTHON">Python 编程题库</el-radio-button>
    </el-radio-group>
    <!-- 搜索区域 -->
    <el-form
      :model="queryParams"
      ref="queryRef"
      :inline="true"
      v-show="showSearch"
      label-width="68px"
    >
      <el-form-item v-if="bankView !== 'PYTHON'" label="年级" prop="grade">
        <el-select
          v-model="queryParams.grade"
          placeholder="请选择年级"
          clearable
          style="width: 200px"
        >
          <el-option
            v-for="dict in biz_grade"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-if="bankView !== 'PYTHON'" label="学期" prop="semester">
        <el-select
          v-model="queryParams.semester"
          placeholder="请选择学期"
          clearable
          style="width: 200px"
        >
          <el-option
            v-for="dict in biz_semester"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-if="bankView !== 'PYTHON'" label="第几课" prop="lessonNum">
        <el-select
          v-model="queryParams.lessonNum"
          placeholder="请选择"
          clearable
          style="width: 120px"
        >
          <el-option
            v-for="n in 20"
            :key="n"
            :label="'第' + n + '课'"
            :value="n"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-if="bankView !== 'PYTHON'" label="题目类型" prop="questionType">
        <el-select
          v-model="queryParams.questionType"
          placeholder="请选择类型"
          clearable
          style="width: 200px"
        >
          <el-option
            v-for="dict in biz_question_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-if="bankView !== 'PYTHON'" label="操作方式" prop="practicalMode">
        <el-select v-model="queryParams.practicalMode" placeholder="请选择操作方式" clearable style="width: 200px">
          <el-option label="Python 在线编程" value="PYTHON" />
          <el-option label="画程流程图" value="FLOWCHART" />
          <el-option label="文件作品" value="FILE" />
        </el-select>
      </el-form-item>
      <el-form-item label="难度" prop="difficulty">
        <el-select v-model="queryParams.difficulty" placeholder="请选择难度" clearable style="width: 160px">
          <el-option label="简单" value="SIMPLE" />
          <el-option label="中等" value="MEDIUM" />
          <el-option label="困难" value="HARD" />
        </el-select>
      </el-form-item>
      <el-form-item label="是否公开" prop="isPublic">
        <el-select
          v-model="queryParams.isPublic"
          placeholder="请选择"
          clearable
          style="width: 200px"
        >
          <el-option
            v-for="dict in sys_yes_no"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="题目内容" prop="questionContent">
        <el-input
          v-model="queryParams.questionContent"
          placeholder="请输入题目内容关键字"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="创建人" prop="createBy">
        <el-input
          v-model="queryParams.createBy"
          placeholder="请输入创建人"
          clearable
          style="width: 160px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery"
          >搜索</el-button
        >
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['business:question:add']"
          >新增</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-if="bankView !== 'PYTHON'"
          type="info"
          plain
          icon="Upload"
          @click="handleImport"
          v-hasPermi="['business:question:import']"
          >批量导入</el-button
        >
      </el-col>
      <el-col v-if="bankView === 'PYTHON'" :span="1.5">
        <el-button type="info" plain icon="Upload" @click="openPythonImport" v-hasPermi="['business:question:import']">Python 双 Sheet 导入</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['business:question:edit']"
          >修改</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['business:question:remove']"
          >删除</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['business:question:export']"
          >导出</el-button
        >
      </el-col>
      <right-toolbar
        v-model:showSearch="showSearch"
        @queryTable="getList"
      ></right-toolbar>
    </el-row>

    <!-- 表格 -->
    <el-table
      v-loading="loading"
      :data="questionList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column
        label="题目ID"
        align="center"
        prop="questionId"
        width="80"
      />
      <el-table-column
        v-if="bankView !== 'PYTHON'"
        label="题目类型"
        align="center"
        prop="questionType"
        width="100"
      >
        <template #default="scope"
          ><dict-tag
            :options="biz_question_type"
            :value="scope.row.questionType"
        /></template>
      </el-table-column>
      <el-table-column v-if="bankView !== 'PYTHON'" label="操作方式" align="center" prop="practicalMode" width="140">
        <template #default="scope">
          <span v-if="scope.row.questionType === 'practical'">
            {{ practicalModeLabel(scope.row.practicalMode) }}
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column v-if="bankView === 'PYTHON'" label="题目标题" align="left" prop="programmingTitle" min-width="180" show-overflow-tooltip />
      <el-table-column
        :label="bankView === 'PYTHON' ? '题目描述' : '题目内容'"
        align="left"
        prop="questionContent"
        :show-overflow-tooltip="true"
      />
      <el-table-column v-if="bankView !== 'PYTHON'"
        label="总字数"
        align="center"
        prop="wordCount"
        width="100"
      />
      <el-table-column v-if="bankView !== 'PYTHON'"
        label="打字时长(分)"
        align="center"
        prop="typingDuration"
        width="120"
      />
      <el-table-column v-if="bankView !== 'PYTHON'" label="年级" align="center" prop="grade" width="100">
        <template #default="scope"><span v-if="scope.row.practicalMode === 'PYTHON'">-</span><dict-tag v-else :options="biz_grade" :value="String(scope.row.grade)" /></template>
      </el-table-column>
      <el-table-column label="难度" align="center" prop="difficulty" width="90">
        <template #default="scope">
          <span v-if="scope.row.practicalMode === 'PYTHON'">{{ difficultyLabel(scope.row.difficulty) }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column v-if="bankView !== 'PYTHON'" label="学期" align="center" prop="semester" width="100">
        <template #default="scope"><span v-if="scope.row.practicalMode === 'PYTHON'">-</span><dict-tag v-else :options="biz_semester" :value="String(scope.row.semester)" /></template>
      </el-table-column>
      <el-table-column v-if="bankView !== 'PYTHON'"
        label="第几课"
        align="center"
        prop="lessonNum"
        width="80"
      >
        <template #default="scope">
          <span v-if="scope.row.lessonNum">第{{ scope.row.lessonNum }}课</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column v-if="bankView === 'PYTHON'" label="知识点" align="left" prop="knowledgePoints" min-width="150" show-overflow-tooltip />
      <el-table-column v-if="bankView === 'PYTHON'" label="测试点" align="center" prop="testCaseCount" width="80" />
      <el-table-column v-if="bankView === 'PYTHON'" label="验证状态" align="center" prop="validationStatus" width="110">
        <template #default="scope"><el-tag :type="validationTagType(scope.row.validationStatus)">{{ validationLabel(scope.row.validationStatus) }}</el-tag></template>
      </el-table-column>
      <el-table-column
        label="是否公开"
        align="center"
        prop="isPublic"
        width="100"
      >
        <template #default="scope"
          ><dict-tag :options="sys_yes_no" :value="scope.row.isPublic"
        /></template>
      </el-table-column>
      <el-table-column
        label="创建人"
        align="center"
        width="120"
      >
        <template #default="scope">
          {{ scope.row.nickName || scope.row.createBy }}
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        align="center"
        class-name="small-padding fixed-width"
        width="200"
        fixed="right"
      >
        <template #default="scope">
          <el-button
            v-if="isFlowchartPracticalQuestion(scope.row) || (scope.row.questionType === 'practical' && scope.row.previewPath)"
            link
            type="success"
            icon="View"
            @click="handlePreview(scope.row)"
            >预览</el-button
          >
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['business:question:edit']"
            >修改</el-button
          >
          <el-button
            link
            type="primary"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['business:question:remove']"
            >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改对话框 -->
    <el-dialog :title="title" v-model="open" :width="isFlowchartPracticalQuestion(form) ? '96%' : '980px'" append-to-body>
      <el-form
        ref="questionRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <!-- 通用表单项 -->
        <el-row v-if="!isPythonPracticalQuestion(form)">
          <el-col :span="12">
            <el-form-item label="题目类型" prop="questionType">
              <el-select
                v-model="form.questionType"
                placeholder="请选择题目类型"
              >
                <el-option
                  v-for="dict in biz_question_type"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否公开" prop="isPublic">
              <el-radio-group v-model="form.isPublic">
                <el-radio
                  v-for="dict in sys_yes_no"
                  :key="dict.value"
                  :label="dict.value"
                  >{{ dict.label }}</el-radio
                >
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-if="!isPythonPracticalQuestion(form)">
          <el-col :span="12">
            <el-form-item label="年级" prop="grade">
              <el-select v-model="form.grade" placeholder="请选择年级">
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
              <el-select v-model="form.semester" placeholder="请选择学期">
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
        <el-row v-if="!isPythonPracticalQuestion(form)">
          <el-col :span="12">
            <el-form-item label="第几课" prop="lessonNum">
              <el-select
                v-model="form.lessonNum"
                placeholder="请选择第几课"
                clearable
              >
                <el-option
                  v-for="n in 20"
                  :key="n"
                  :label="'第' + n + '课'"
                  :value="n"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="!isPythonPracticalQuestion(form)" :label="questionContentLabel" prop="questionContent">
          <el-input
            v-model="form.questionContent"
            :rows="questionContentRows"
            type="textarea"
            placeholder="请输入内容"
          />
        </el-form-item>

        <!-- 动态表单项: 打字题专属 -->
        <div v-if="form.questionType === 'typing'">
          <el-row>
            <el-col :span="8">
              <el-form-item label="总字数">
                <el-input
                  :value="form.wordCount || 0"
                  disabled
                  style="width: 100%"
                >
                  <template #suffix>字</template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="打字时长">
                <el-input-number
                  v-model="form.typingDuration"
                  :min="1"
                  :max="120"
                  style="width: 100%"
                  @change="markTypingDurationCustomized"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="基准速度">
                <el-input
                  :value="typingBaseSpeed + ' 字/分'"
                  disabled
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <div style="color: #909399; font-size: 12px; margin-bottom: 18px; line-height: 1.6;">
            <span>时长 = 字数 ÷ 基准速度（向上取整），仅作为答题时间限制。</span><br/>
            <span>评分公式：得分 = 满分 × (正确字数 ÷ 总字数) × 正确率</span>
          </div>
        </div>

        <!-- 动态表单项: 选择题专属 -->
        <div v-if="form.questionType === 'choice'">
          <el-form-item label="选项A" prop="optionA"
            ><el-input v-model="form.optionA" placeholder="请输入选项A"
          /></el-form-item>
          <el-form-item label="选项B" prop="optionB"
            ><el-input v-model="form.optionB" placeholder="请输入选项B"
          /></el-form-item>
          <el-form-item label="选项C" prop="optionC"
            ><el-input v-model="form.optionC" placeholder="请输入选项C"
          /></el-form-item>
          <el-form-item label="选项D" prop="optionD"
            ><el-input v-model="form.optionD" placeholder="请输入选项D"
          /></el-form-item>
          <el-form-item label="标准答案" prop="answer">
            <el-radio-group v-model="form.answer">
              <el-radio value="A">A</el-radio>
              <el-radio value="B">B</el-radio>
              <el-radio value="C">C</el-radio>
              <el-radio value="D">D</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="题目解析" prop="analysis">
            <el-input
              v-model="form.analysis"
              :rows="3"
              type="textarea"
              placeholder="请输入解析"
            />
          </el-form-item>
        </div>

        <!-- 动态表单项: 判断题专属 -->
        <div v-if="form.questionType === 'judgment'">
          <el-form-item label="标准答案" prop="answer">
            <el-radio-group v-model="form.answer">
              <el-radio value="T">正确</el-radio>
              <el-radio value="F">错误</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="题目解析" prop="analysis">
            <el-input
              v-model="form.analysis"
              :rows="3"
              type="textarea"
              placeholder="请输入解析"
            />
          </el-form-item>
        </div>

        <div v-if="isPythonPracticalQuestion(form)" class="python-wizard">
          <el-steps :active="pythonStep" align-center finish-status="success" class="python-steps">
            <el-step title="题面与格式" description="把任务说清楚" />
            <el-step title="测试点" description="公开样例与隐藏数据" />
            <el-step title="代码与验证" description="参考代码一键验题" />
          </el-steps>

          <div v-show="pythonStep === 0" class="python-step-panel">
            <el-row :gutter="14">
              <el-col :span="12"><el-form-item label="题目标题" required><el-input v-model="form.programming.title" maxlength="255" placeholder="例如：计算长方形的面积" /></el-form-item></el-col>
              <el-col :span="6"><el-form-item label="难度" prop="difficulty"><el-select v-model="form.difficulty" style="width:100%"><el-option label="简单" value="SIMPLE" /><el-option label="中等" value="MEDIUM" /><el-option label="困难" value="HARD" /></el-select></el-form-item></el-col>
              <el-col :span="6"><el-form-item label="无输入题"><el-switch v-model="form.programming.noInput" active-value="1" inactive-value="0" /></el-form-item></el-col>
            </el-row>
            <el-form-item label="知识点"><el-input v-model="form.programming.knowledgePoints" placeholder="多个知识点用逗号分隔，例如：输入输出,整数运算" /></el-form-item>
            <el-form-item label="题目描述" prop="questionContent"><el-input v-model="form.questionContent" type="textarea" :rows="5" placeholder="说明已知条件、要完成的任务，不要只写“按题面输入输出”" /></el-form-item>
            <el-row :gutter="14">
              <el-col :span="12"><el-form-item label="输入格式"><el-input v-model="form.programming.inputDescription" type="textarea" :rows="3" :disabled="form.programming.noInput === '1'" placeholder="逐行说明类型、数量、分隔方式" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="输出格式"><el-input v-model="form.programming.outputDescription" type="textarea" :rows="3" placeholder="说明输出内容和精度、空格、换行要求" /></el-form-item></el-col>
            </el-row>
            <el-row :gutter="14">
              <el-col :span="8"><el-form-item label="数据范围"><el-input v-model="form.programming.constraintsText" type="textarea" :rows="3" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="样例解释"><el-input v-model="form.programming.sampleExplanation" type="textarea" :rows="3" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="提示"><el-input v-model="form.programming.notesText" type="textarea" :rows="3" /></el-form-item></el-col>
            </el-row>
          </div>

          <div v-show="pythonStep === 1" class="python-step-panel">
            <el-alert title="公开测试点会作为样例展示；隐藏测试点只用于正式判题。每行输入输出会原样传给程序。" type="info" :closable="false" />
            <div class="case-summary">
              <div>
                <el-tag type="success">公开样例 {{ programmingPublicCaseCount }} 个</el-tag>
                <el-tag type="warning">隐藏测试点 {{ programmingHiddenCaseCount }} 个</el-tag>
                <el-tag :type="programmingWeightTotal === 100 ? 'success' : 'danger'">总权重 {{ programmingWeightTotal }} / 100</el-tag>
              </div>
              <el-button link type="primary" @click="distributeProgrammingWeights">平均分配到 100</el-button>
            </div>
            <el-alert v-if="programmingWeightTotal !== 100" :title="programmingWeightErrorText" type="error" :closable="false" show-icon />
            <el-table :data="form.programming.testCases" border class="case-table">
              <el-table-column label="名称" min-width="120"><template #default="{ row }"><el-input v-model="row.caseName" /></template></el-table-column>
              <el-table-column label="输入" min-width="180"><template #default="{ row }"><el-input v-model="row.inputText" type="textarea" :rows="3" :disabled="form.programming.noInput === '1'" /></template></el-table-column>
              <el-table-column label="期望输出" min-width="180"><template #default="{ row }"><el-input v-model="row.expectedOutput" type="textarea" :rows="3" /></template></el-table-column>
              <el-table-column label="类型" width="130"><template #default="{ row }"><el-select v-model="row.isPublic"><el-option label="公开样例" value="1" /><el-option label="隐藏测试" value="0" /></el-select></template></el-table-column>
              <el-table-column label="权重" width="120"><template #default="{ row }"><el-input-number v-model="row.scoreWeight" :min="0.1" :step="0.5" controls-position="right" /></template></el-table-column>
              <el-table-column label="操作" width="70"><template #default="{ $index }"><el-button link type="danger" icon="Delete" @click="form.programming.testCases.splice($index, 1)" /></template></el-table-column>
            </el-table>
            <div class="case-actions"><el-button type="primary" plain icon="Plus" @click="addProgrammingCase">添加测试点</el-button><span>至少 1 个公开样例和 1 个隐藏测试点；系统题建议 2 个公开、4 个隐藏。</span></div>
          </div>

          <div v-show="pythonStep === 2" class="python-step-panel">
            <el-alert :type="validationAlertType" :title="validationStatusText" :closable="false" class="validation-alert" />
            <el-form-item label="起始代码"><el-input v-model="form.programming.starterCode" type="textarea" :rows="5" placeholder="可留空；如填写，只给出必要结构，不泄露解法" /></el-form-item>
            <el-form-item label="参考代码" prop="answer" required><el-input v-model="form.answer" type="textarea" :rows="10" placeholder="仅教师可见。保存后系统会用这份代码跑完全部测试点。" /></el-form-item>
            <el-collapse>
              <el-collapse-item title="高级资源限制（通常无需修改）" name="limits">
                <el-row :gutter="12"><el-col :span="8"><el-form-item label="时限（秒）"><el-input-number v-model="form.programming.timeLimitSeconds" :min="0.1" :max="10" :step="0.1" /></el-form-item></el-col><el-col :span="8"><el-form-item label="内存（KB）"><el-input-number v-model="form.programming.memoryLimitKb" :min="16384" :max="524288" :step="1024" /></el-form-item></el-col><el-col :span="8"><el-form-item label="输出（KB）"><el-input-number v-model="form.programming.maxOutputKb" :min="1" :max="1024" /></el-form-item></el-col></el-row>
              </el-collapse-item>
            </el-collapse>
          </div>

          <div class="wizard-actions"><el-button v-if="pythonStep > 0" @click="pythonStep--">上一步</el-button><el-button v-if="pythonStep < 2" type="primary" @click="nextPythonStep">下一步</el-button></div>
        </div>

        <!-- 动态表单项: 操作题专属 -->
        <div v-if="form.questionType === 'practical'">
          <el-form-item label="作答方式">
            <el-radio-group v-model="form.practicalMode">
              <el-radio value="FILE">文件作品</el-radio>
              <el-radio value="PYTHON">Python 在线编程</el-radio>
              <el-radio value="FLOWCHART">画程流程图</el-radio>
            </el-radio-group>
          </el-form-item>
          <template v-if="form.practicalMode === 'FILE'">
          <el-form-item label="学生起始文件">
            <file-upload
              v-model="form.filePath"
              :file-type="['doc', 'docx', 'pdf', 'ppt', 'pptx', 'xls', 'xlsx', 'jpg', 'jpeg', 'png']"
              :file-size="50"
              :limit="1"
            />
            <div style="color: #909399; font-size: 12px; margin-top: 6px;">
              可不上传；如上传，学生可下载它继续完成作品。
            </div>
          </el-form-item>
          <el-form-item label="学生提交格式">
            <el-checkbox-group v-model="form.practicalAllowedExtensionList">
              <el-checkbox v-for="item in practicalExtensionOptions" :key="item" :value="item">{{ item.toUpperCase() }}</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <el-form-item label="图片上限">
            <el-input-number v-model="form.practicalImageMaxCount" :min="1" :max="10" />
            <span style="margin-left: 10px; color: #909399; font-size: 12px;">仅在学生提交图片组时生效</span>
          </el-form-item>
          <el-form-item label="补充资源">
            <file-upload
              v-model="form.practicalResourceFiles"
              :file-type="['doc', 'docx', 'pdf', 'ppt', 'pptx', 'xls', 'xlsx', 'jpg', 'jpeg', 'png', 'zip']"
              :file-size="50"
              :limit="5"
            />
            <div style="color: #909399; font-size: 12px; margin-top: 6px;">学生可见，可包含 ZIP 资源包；不作为学生提交格式。</div>
          </el-form-item>
          <el-form-item label="教师参考答案">
            <file-upload
              v-model="form.practicalReferenceFiles"
              :file-type="['doc', 'docx', 'pdf', 'ppt', 'pptx', 'xls', 'xlsx', 'jpg', 'jpeg', 'png']"
              :file-size="50"
              :limit="5"
            />
            <div style="color: #e6a23c; font-size: 12px; margin-top: 6px;">仅教师和 AI 批改可见，不会下发给学生；压缩包不能作为可识别答案。</div>
          </el-form-item>

          <el-divider content-position="left"
            >评分项配置（比例分配）</el-divider
          >
          <el-form-item label-width="0">
            <el-table :data="form.scoringItems" border style="width: 100%">
              <el-table-column type="index" width="50" align="center" />
              <el-table-column label="评分项名称" prop="itemName">
                <template #default="scope">
                  <el-input
                    v-model="scope.row.itemName"
                    placeholder="如：界面设计"
                    maxlength="50"
                  />
                </template>
              </el-table-column>
              <el-table-column
                label="比例值 (建议合计100)"
                prop="itemScore"
                width="180"
                align="center"
              >
                <template #default="scope">
                  <el-input-number
                    v-model="scope.row.itemScore"
                    :min="1"
                    :max="100"
                    controls-position="right"
                    style="width: 100px"
                  />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" align="center">
                <template #default="scope">
                  <el-button
                    link
                    type="danger"
                    icon="Delete"
                    @click="removeScoringItem(scope.$index)"
                  ></el-button>
                </template>
              </el-table-column>
            </el-table>
            <div
              style="
                margin-top: 10px;
                display: flex;
                justify-content: space-between;
                align-items: center;
              "
            >
              <div>
                <el-button
                  type="primary"
                  plain
                  icon="Plus"
                  size="small"
                  @click="addScoringItem"
                  >添加评分项</el-button
                >
                <span style="margin-left: 10px; color: #909399; font-size: 12px"
                  >提示：比例值合计应为100，系统会按课程设置的总分自动折算。</span
                >
              </div>
              <div style="font-weight: bold; color: #606266">
                当前合计:
                <span
                  :style="{
                    color: scoringItemsSum === 100 ? '#67c23a' : '#f56c6c',
                  }"
                  >{{ scoringItemsSum }}</span
                >
                / 100
              </div>
            </div>
          </el-form-item>
          </template>
          <el-form-item v-else-if="form.practicalMode === 'FLOWCHART'" label-width="0">
            <flowchart-question-designer v-model="form.flowchartConfig" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button v-if="!isPythonPracticalQuestion(form)" type="primary" @click="submitForm(false)">确 定</el-button>
          <template v-else>
            <el-button @click="submitForm(false)">保存草稿</el-button>
            <el-button type="primary" :loading="validating" @click="submitForm(true)">保存并验证全部测试点</el-button>
          </template>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="pythonImportOpen" title="导入 Python OJ 题目" width="760px" append-to-body>
      <el-alert title="Excel 必须包含“题目”和“测试点”两个 Sheet。上传只做预检，不会立即写入题库。" type="info" :closable="false" />
      <div class="python-import-actions">
        <el-upload :auto-upload="false" accept=".xlsx,.xls" :show-file-list="false" :on-change="handlePythonWorkbook">
          <el-button type="primary" :loading="pythonImportLoading">选择 Excel 并预检</el-button>
        </el-upload>
        <el-button link type="primary" @click="downloadPythonTemplate">下载双 Sheet 模板</el-button>
      </div>
      <div v-if="pythonImportReport" class="import-report">
        <el-descriptions :column="3" border><el-descriptions-item label="题目数">{{ pythonImportReport.questionCount }}</el-descriptions-item><el-descriptions-item label="测试点数">{{ pythonImportReport.testCaseCount }}</el-descriptions-item><el-descriptions-item label="预检结果"><el-tag :type="pythonImportReport.ready ? 'success' : 'danger'">{{ pythonImportReport.ready ? '全部通过' : '需要修正' }}</el-tag></el-descriptions-item></el-descriptions>
        <el-alert v-if="pythonImportReport.ready" :title="`参考代码已通过全部测试点；确认令牌 ${pythonImportReport.expiresInMinutes} 分钟内有效。`" type="success" :closable="false" class="import-alert" />
        <div v-else class="import-errors"><h4>错误清单</h4><ol><li v-for="(error, index) in pythonImportReport.errors" :key="index">{{ error }}</li></ol></div>
        <el-table v-if="pythonImportReport.validation?.length" :data="pythonImportReport.validation" size="small" max-height="240"><el-table-column prop="externalId" label="外部题号" width="110" /><el-table-column prop="title" label="标题" /><el-table-column prop="passedCount" label="通过" width="70" /><el-table-column prop="totalCount" label="总点数" width="80" /></el-table>
      </div>
      <template #footer><el-button @click="pythonImportOpen = false">取消</el-button><el-button type="primary" :disabled="!pythonImportReport?.ready" :loading="pythonImportConfirming" @click="confirmPythonWorkbook">确认事务导入</el-button></template>
    </el-dialog>

    <!-- 题目导入对话框 -->
    <el-dialog
      :title="upload.title"
      v-model="upload.open"
      width="400px"
      append-to-body
    >
      <el-upload
        ref="uploadRef"
        :limit="1"
        accept=".xlsx, .xls"
        :headers="upload.headers"
        :action="upload.url + '?updateSupport=' + upload.updateSupport"
        :disabled="upload.isUploading"
        :on-progress="handleFileUploadProgress"
        :on-success="handleFileSuccess"
        :on-error="handleFileError"
        :auto-upload="false"
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip text-center">
            <span>仅允许导入xls、xlsx格式文件。</span>
            <el-link
              type="primary"
              :underline="false"
              style="font-size: 12px; vertical-align: baseline"
              @click="importTemplate"
              >下载模板</el-link
            >
          </div>
        </template>
      </el-upload>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitFileForm">确 定</el-button>
          <el-button @click="cancelUpload">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <flowchart-question-preview-dialog v-model="flowchartPreviewVisible" :question="flowchartPreviewQuestion" />
  </div>
</template>

<script setup name="Question">
import {
  listQuestion,
  getQuestion,
  delQuestion,
  addQuestion,
  updateQuestion,
} from "@/api/business/question";
import { getProgrammingQuestion, saveProgrammingQuestion, validateProgrammingQuestion, previewProgrammingImport, confirmProgrammingImport } from "@/api/business/programming";
import { getFlowchartQuestion, saveFlowchartQuestion } from "@/api/business/flowchart";
import FlowchartQuestionDesigner from "@/components/FlowchartEditor/FlowchartQuestionDesigner.vue";
import FlowchartQuestionPreviewDialog from "@/components/FlowchartEditor/FlowchartQuestionPreviewDialog.vue";
import { EMPTY_FLOWCHART, DEFAULT_FLOWCHART_PERMISSIONS, parseFlowchartDocument } from "@/components/FlowchartEditor/schema";
import { computed, getCurrentInstance, reactive, ref, watch } from "vue";
import { ElLoading, ElMessage } from "element-plus"; // P6 import
import * as XLSX from "xlsx";
import {
  handleSessionExpired,
  isSessionExpiredCode,
  isSessionExpiredError,
  refreshAuthorizationHeader
} from "@/utils/session";

const { proxy } = getCurrentInstance();
const { biz_question_type, sys_yes_no, biz_grade, biz_semester } =
  proxy.useDict("biz_question_type", "sys_yes_no", "biz_grade", "biz_semester");

const questionList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const bankView = ref("ALL");
const pythonStep = ref(0);
const validating = ref(false);
const pythonImportOpen = ref(false);
const pythonImportLoading = ref(false);
const pythonImportConfirming = ref(false);
const pythonImportReport = ref(null);
const flowchartPreviewVisible = ref(false);
const flowchartPreviewQuestion = ref(null);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    questionType: null,
    practicalMode: null,
    difficulty: null,
    questionContent: null,
    isPublic: null,
    grade: null,
    semester: null,
    lessonNum: null,
    bankView: "ALL",
    createBy: null,
  },
});

const { queryParams, form } = toRefs(data);

// --- 动态校验规则 ---
const rules = computed(() => {
  const pythonQuestion = isPythonPracticalQuestion(form.value);
  const baseRules = {
    questionType: [{ required: true, message: "题目类型不能为空" }],
    questionContent: [{ required: true, message: "题目内容不能为空" }],
    isPublic: [{ required: true, message: "是否公开不能为空" }],
    ...(pythonQuestion ? {} : {
      grade: [{ required: true, message: "年级不能为空" }],
      semester: [{ required: true, message: "学期不能为空" }],
    }),
  };

  if (form.value.questionType === "choice") {
    return {
      ...baseRules,
      optionA: [{ required: true, message: "选项A不能为空" }],
      optionB: [{ required: true, message: "选项B不能为空" }],
      optionC: [{ required: true, message: "选项C不能为空" }],
      optionD: [{ required: true, message: "选项D不能为空" }],
      answer: [{ required: true, message: "标准答案不能为空" }],
    };
  } else if (form.value.questionType === "judgment") {
    return {
      ...baseRules,
      answer: [{ required: true, message: "标准答案不能为空" }],
    };
  } else if (form.value.questionType === "practical") {
    return {
      ...baseRules,
      ...(pythonQuestion ? {
        difficulty: [{ required: true, message: "难度不能为空" }],
        answer: [{ required: true, message: "参考代码不能为空" }],
      } : {}),
    };
  } else if (form.value.questionType === "typing") {
    return {
      ...baseRules,
      typingDuration: [{ required: true, message: "打字时长不能为空" }],
    };
  }
  return baseRules;
});

/*** 题目导入参数 */
const upload = reactive({
  open: false,
  title: "",
  isUploading: false,
  updateSupport: 0,
  headers: refreshAuthorizationHeader(),
  url: import.meta.env.VITE_APP_BASE_API + "/business/question/importData",
});

function refreshUploadHeaders() {
  upload.headers = refreshAuthorizationHeader(upload.headers);
}

const questionContentLabel = computed(() => {
  switch (form.value.questionType) {
    case "typing":
      return "打字题内容";
    case "practical":
      return "操作题名称";
    default:
      return "题目内容";
  }
});

const questionContentRows = computed(() => {
  return form.value.questionType === "typing" ? 8 : 3;
});

/** 查询题库管理列表 */
function getList() {
  loading.value = true;
  listQuestion(queryParams.value).then((response) => {
    questionList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

function difficultyLabel(value) {
  return value === "SIMPLE" ? "简单" : value === "HARD" ? "困难" : "中等";
}

function validationLabel(value) {
  return ({ VALID: "已验证", INVALID: "验证失败", VALIDATING: "验证中", DRAFT: "待验证" })[value] || "待验证";
}

function validationTagType(value) {
  return value === "VALID" ? "success" : value === "INVALID" ? "danger" : value === "VALIDATING" ? "warning" : "info";
}

function changeBankView(value) {
  queryParams.value.bankView = value;
  queryParams.value.pageNum = 1;
  if (value === "PYTHON") {
    Object.assign(queryParams.value, { questionType: null, practicalMode: null, grade: null, semester: null, lessonNum: null });
  }
  getList();
}

function openPythonImport() {
  pythonImportReport.value = null;
  pythonImportOpen.value = true;
}

function cell(row, ...names) {
  for (const name of names) if (row[name] !== undefined && row[name] !== null) return row[name];
  return "";
}

function parseNumber(value, fallback) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : fallback;
}

async function handlePythonWorkbook(file) {
  if (!file?.raw) return;
  pythonImportLoading.value = true;
  pythonImportReport.value = null;
  try {
    const workbook = XLSX.read(await file.raw.arrayBuffer(), { type: "array" });
    const questionSheet = workbook.Sheets["题目"];
    const caseSheet = workbook.Sheets["测试点"];
    if (!questionSheet || !caseSheet) throw new Error("Excel 必须同时包含“题目”和“测试点”两个 Sheet");
    const questionRows = XLSX.utils.sheet_to_json(questionSheet, { defval: "", raw: false });
    const caseRows = XLSX.utils.sheet_to_json(caseSheet, { defval: "", raw: false });
    const payload = {
      questions: questionRows.map(row => ({
        externalId: String(cell(row, "外部题号", "externalId")), title: String(cell(row, "标题", "title")), difficulty: String(cell(row, "难度", "difficulty")), knowledgePoints: String(cell(row, "知识点", "knowledgePoints")),
        description: String(cell(row, "题目描述", "description")), inputDescription: String(cell(row, "输入格式", "inputDescription")), outputDescription: String(cell(row, "输出格式", "outputDescription")), constraintsText: String(cell(row, "数据范围", "constraintsText")), sampleExplanation: String(cell(row, "样例解释", "sampleExplanation")), notesText: String(cell(row, "提示", "notesText")),
        starterCode: String(cell(row, "起始代码", "starterCode")), referenceCode: String(cell(row, "参考代码", "referenceCode")), noInput: String(cell(row, "无输入题", "noInput")), isPublic: String(cell(row, "是否公开", "isPublic")),
        timeLimitSeconds: parseNumber(cell(row, "时间限制(秒)", "时间限制", "timeLimitSeconds"), 2), memoryLimitKb: parseNumber(cell(row, "内存限制(KB)", "内存限制", "memoryLimitKb"), 131072),
      })),
      testCases: caseRows.map((row, index) => ({
        externalId: String(cell(row, "外部题号", "externalId")), caseName: String(cell(row, "用例名称", "caseName")), inputText: String(cell(row, "输入", "inputText")), expectedOutput: String(cell(row, "期望输出", "expectedOutput")), isPublic: String(cell(row, "是否公开", "isPublic")), scoreWeight: parseNumber(cell(row, "权重", "scoreWeight"), 1), orderNum: parseNumber(cell(row, "顺序", "orderNum"), index + 1),
      })),
    };
    pythonImportReport.value = (await previewProgrammingImport(payload)).data || {};
    if (pythonImportReport.value.ready) ElMessage.success("预检通过，可以确认导入"); else ElMessage.warning(`预检发现 ${pythonImportReport.value.errors?.length || 0} 个问题`);
  } catch (error) {
    ElMessage.error(error?.message || "Excel 解析或预检失败");
  } finally {
    pythonImportLoading.value = false;
  }
}

function downloadPythonTemplate() {
  const questions = [{ "外部题号": "PY001", "标题": "两个整数的和", "难度": "简单", "知识点": "输入输出,整数运算", "题目描述": "读入两个整数，输出它们的和。", "输入格式": "一行两个整数 a 和 b，用一个空格分隔。", "输出格式": "输出一个整数，表示 a+b。", "数据范围": "-10000 ≤ a,b ≤ 10000", "样例解释": "3+5=8。", "提示": "", "起始代码": "a, b = map(int, input().split())\n", "参考代码": "a, b = map(int, input().split())\nprint(a + b)\n", "无输入题": "否", "是否公开": "是", "时间限制(秒)": 2, "内存限制(KB)": 131072 }];
  const cases = [{ "外部题号": "PY001", "用例名称": "样例1", "输入": "3 5\n", "期望输出": "8\n", "是否公开": "是", "权重": 1, "顺序": 1 }, { "外部题号": "PY001", "用例名称": "负数边界", "输入": "-10 4\n", "期望输出": "-6\n", "是否公开": "否", "权重": 1, "顺序": 2 }];
  const workbook = XLSX.utils.book_new(); XLSX.utils.book_append_sheet(workbook, XLSX.utils.json_to_sheet(questions), "题目"); XLSX.utils.book_append_sheet(workbook, XLSX.utils.json_to_sheet(cases), "测试点"); XLSX.writeFile(workbook, "Python_OJ题目双Sheet导入模板.xlsx");
}

async function confirmPythonWorkbook() {
  const token = pythonImportReport.value?.confirmToken;
  if (!token) return;
  pythonImportConfirming.value = true;
  try {
    const result = (await confirmProgrammingImport(token)).data || {};
    ElMessage.success(`已成功导入 ${result.importedCount || 0} 道已验证 Python 题`);
    pythonImportOpen.value = false; pythonImportReport.value = null; getList();
  } finally {
    pythonImportConfirming.value = false;
  }
}

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

// 导入弹窗取消按钮
function cancelUpload() {
  upload.open = false;
  proxy.resetForm("uploadRef");
}

// 表单重置
function reset() {
  pythonStep.value = 0;
  form.value = {
    questionId: null,
    questionType: null,
    questionContent: null,
    grade: null,
    semester: null,
    difficulty: null,
    optionA: null,
    optionB: null,
    optionC: null,
    optionD: null,
    answer: null,
    analysis: null,
    filePath: null,
    isPublic: "Y",
    typingDuration: null,
    wordCount: null,
    lessonNum: null,
    creatorId: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    scoringItems: [], // P6
    practicalAllowedExtensions: "doc,docx,pdf,ppt,pptx,xls,xlsx,jpg,jpeg,png",
    practicalMode: "FILE",
    practicalAllowedExtensionList: ["doc", "docx", "pdf", "ppt", "pptx", "xls", "xlsx", "jpg", "jpeg", "png"],
    practicalImageMaxCount: 10,
    practicalMaterials: [],
    practicalResourceFiles: "",
    practicalReferenceFiles: "",
    flowchartConfig: defaultFlowchartConfig(),
    programming: defaultProgrammingConfig(),
  };
  proxy.resetForm("questionRef");
}

/** P6.1: 计算评分项比例合计 */
const scoringItemsSum = computed(() => {
  if (!form.value.scoringItems || form.value.scoringItems.length === 0)
    return 0;
  return form.value.scoringItems.reduce(
    (sum, i) => sum + (i.itemScore || 0),
    0
  );
});

/** 打字题基准速度：小学 20 字/分，初中及以上 40 字/分 */
const typingBaseSpeed = computed(() => {
  const grade = form.value.grade;
  if (grade && grade >= 1 && grade <= 6) {
    return 20; // 小学
  }
  return 40; // 初中及以上
});

/** 推荐打字时长（分钟，向上取整） */
const recommendedDuration = computed(() => {
  const wordCount = form.value.wordCount || 0;
  if (wordCount <= 0) return 1;
  return Math.ceil(wordCount / typingBaseSpeed.value);
});

const practicalExtensionOptions = ["doc", "docx", "pdf", "ppt", "pptx", "xls", "xlsx", "jpg", "jpeg", "png"];

function materialPaths(materials, type) {
  return (materials || [])
    .filter((item) => item.materialType === type)
    .map((item) => item.resourcePath)
    .filter(Boolean)
    .join(",");
}

function splitMaterialPaths(value, materialType) {
  return String(value || "").split(",").map((item) => item.trim()).filter(Boolean)
    .map((resourcePath) => ({ materialType, resourcePath }));
}

const typingDurationCustomized = ref(false);

function defaultProgrammingConfig() {
  return {
    languageCode: "python",
    title: "",
    knowledgePoints: "",
    noInput: "0",
    validationStatus: "DRAFT",
    contentVersion: 1,
    starterCode: "",
    inputDescription: "",
    outputDescription: "",
    sampleExplanation: "",
    constraintsText: "",
    notesText: "",
    timeLimitSeconds: 2,
    memoryLimitKb: 131072,
    maxProcesses: 8,
    maxFileSizeKb: 1024,
    maxOutputKb: 64,
    enabled: "1",
    testCases: [
      {
        caseName: "示例 1",
        inputText: "",
        expectedOutput: "",
        isPublic: "1",
        scoreWeight: 50,
      },
      {
        caseName: "隐藏测试 1",
        inputText: "",
        expectedOutput: "",
        isPublic: "0",
        scoreWeight: 50,
      },
    ],
  };
}

function isPythonPracticalQuestion(question) {
  return question?.questionType === "practical" && question?.practicalMode === "PYTHON";
}

function isFlowchartPracticalQuestion(question) {
  return question?.questionType === "practical" && question?.practicalMode === "FLOWCHART";
}

function practicalModeLabel(mode) {
  if (mode === "PYTHON") return "Python 在线编程";
  if (mode === "FLOWCHART") return "画程流程图";
  return "文件作品";
}

function defaultFlowchartConfig() {
  return {
    questionId: null,
    configRevision: 0,
    schemaVersion: "1.0",
    starterJson: JSON.stringify(EMPTY_FLOWCHART),
    answerJson: JSON.stringify(EMPTY_FLOWCHART),
    permissionsJson: JSON.stringify(DEFAULT_FLOWCHART_PERMISSIONS),
    rulesJson: "[]",
  };
}

function hasFlowchartRules(rulesJson) {
  try {
    const rules = JSON.parse(rulesJson || "[]");
    return Array.isArray(rules) && rules.length > 0;
  } catch (_) {
    return false;
  }
}

function addProgrammingCase() {
  if (!form.value.programming) form.value.programming = defaultProgrammingConfig();
  form.value.programming.testCases.push({
    caseName: `测试点 ${form.value.programming.testCases.length + 1}`,
    inputText: "",
    expectedOutput: "",
    isPublic: "0",
    scoreWeight: 1,
  });
}

const programmingPublicCaseCount = computed(() => (form.value.programming?.testCases || []).filter((item) => item.isPublic === "1").length);
const programmingHiddenCaseCount = computed(() => (form.value.programming?.testCases || []).filter((item) => item.isPublic !== "1").length);
const programmingWeightTotal = computed(() => Math.round((form.value.programming?.testCases || []).reduce((sum, item) => sum + (Number(item.scoreWeight) || 0), 0) * 100) / 100);
const programmingWeightErrorText = computed(() => {
  const difference = Math.round((100 - programmingWeightTotal.value) * 100) / 100;
  return difference > 0
    ? `测试点权重还差 ${difference}，必须合计为 100 才能保存。`
    : `测试点权重超出 ${Math.abs(difference)}，必须合计为 100 才能保存。`;
});

function distributeProgrammingWeights() {
  const cases = form.value.programming?.testCases || [];
  if (!cases.length) return ElMessage.warning("请先添加测试点");
  const base = Math.floor((100 / cases.length) * 100) / 100;
  let assigned = 0;
  cases.forEach((item, index) => {
    const weight = index === cases.length - 1 ? Math.round((100 - assigned) * 100) / 100 : base;
    item.scoreWeight = weight;
    assigned += weight;
  });
  ElMessage.success("测试点权重已平均分配为 100");
}

const validationAlertType = computed(() => form.value.programming?.validationStatus === "VALID" ? "success" : form.value.programming?.validationStatus === "INVALID" ? "error" : "warning");
const validationStatusText = computed(() => {
  const status = form.value.programming?.validationStatus;
  if (status === "VALID") return "验证通过：参考代码已通过全部测试点，可以加入课程或刷题题单。";
  if (status === "INVALID") return "验证未通过：请检查参考代码或测试点后重新保存并验证。";
  if (status === "VALIDATING") return "正在验证全部测试点。";
  return "尚未验证：题面、测试点或代码修改后都需要重新验证。";
});

function validatePythonBasics() {
  const programming = form.value.programming || {};
  if (!String(programming.title || "").trim()) return "请填写题目标题";
  if (!String(form.value.questionContent || "").trim()) return "请填写题目描述";
  if (programming.noInput !== "1" && !String(programming.inputDescription || "").trim()) return "请写清楚输入格式";
  if (!String(programming.outputDescription || "").trim()) return "请写清楚输出格式";
  return "";
}

function validatePythonCases() {
  const testCases = form.value.programming?.testCases || [];
  if (!testCases.some((item) => item.isPublic === "1")) return "至少需要一个公开样例";
  if (!testCases.some((item) => item.isPublic !== "1")) return "至少需要一个隐藏测试点";
  if (testCases.some((item) => item.expectedOutput == null || String(item.expectedOutput).trim() === "")) return "每个测试点都必须填写期望输出";
  if (testCases.some((item) => !Number.isFinite(Number(item.scoreWeight)) || Number(item.scoreWeight) <= 0)) return "每个测试点权重都必须大于 0";
  if (Math.abs(programmingWeightTotal.value - 100) > 0.000001) return `测试点权重合计必须为 100，当前为 ${programmingWeightTotal.value}`;
  return "";
}

function nextPythonStep() {
  const message = pythonStep.value === 0 ? validatePythonBasics() : validatePythonCases();
  if (message) return ElMessage.warning(message);
  pythonStep.value += 1;
}

function syncTypingWordStats(newContent) {
  const content = newContent || "";
  form.value.wordCount = content.replace(/\s/g, "").length;
  if (!typingDurationCustomized.value) {
    form.value.typingDuration = recommendedDuration.value;
  }
}

function markTypingDurationCustomized() {
  if (form.value.questionType === "typing") {
    typingDurationCustomized.value = true;
  }
}

/** 监听打字题内容变化，自动计算字数和推荐时长 */
watch(
  () => form.value.questionContent,
  (newContent) => {
    if (form.value.questionType === "typing") {
      syncTypingWordStats(newContent);
    }
  }
);

/** 监听年级变化，重新计算推荐时长 */
watch(
  () => form.value.grade,
  () => {
    if (form.value.questionType === "typing" && form.value.wordCount > 0 && !typingDurationCustomized.value) {
      form.value.typingDuration = recommendedDuration.value;
    }
  }
);

/** P6: 添加评分项 */
function addScoringItem() {
  if (!form.value.scoringItems) {
    form.value.scoringItems = [];
  }
  form.value.scoringItems.push({
    itemName: "",
    itemScore: 10,
  });
}

/** P6: 删除评分项 */
function removeScoringItem(index) {
  form.value.scoringItems.splice(index, 1);
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.questionId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  typingDurationCustomized.value = false;
  open.value = true;
  title.value = "添加题目";
  proxy.$nextTick(() => {
    proxy.$refs["questionRef"].clearValidate();
  });
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const _questionId = row.questionId || ids.value[0];
  getQuestion(_questionId).then((response) => {
    form.value = response.data;
    form.value.programming = defaultProgrammingConfig();
    form.value.practicalAllowedExtensionList = String(
      form.value.practicalAllowedExtensions || "doc,docx,pdf,ppt,pptx,xls,xlsx,jpg,jpeg,png"
    ).split(",").filter(Boolean);
    form.value.practicalResourceFiles = materialPaths(form.value.practicalMaterials, "RESOURCE");
    form.value.practicalReferenceFiles = materialPaths(form.value.practicalMaterials, "REFERENCE");
    typingDurationCustomized.value = form.value.questionType === "typing" && form.value.typingDuration != null;
    const showEditor = () => {
      open.value = true;
      title.value = "修改题目";
    };
    if (isPythonPracticalQuestion(form.value)) {
      getProgrammingQuestion(_questionId).then((programmingResponse) => {
        const config = programmingResponse.data || {};
        form.value.programming = {
          ...defaultProgrammingConfig(),
          ...config,
          testCases: programmingResponse.testCases || [],
        };
        showEditor();
      }).catch(() => {
        showEditor();
      });
    } else if (isFlowchartPracticalQuestion(form.value)) {
      getFlowchartQuestion(_questionId).then((flowchartResponse) => {
        form.value.flowchartConfig = { ...defaultFlowchartConfig(), ...(flowchartResponse.data || {}) };
        showEditor();
      }).catch(() => {
        form.value.flowchartConfig = defaultFlowchartConfig();
        showEditor();
      });
    } else {
      showEditor();
    }
  });
}

/** 保存普通题，或保存 Python 题后用参考代码验证全部测试点。 */
function submitForm(validateAfterSave = false) {
  proxy.$refs["questionRef"].validate(async (valid) => {
    if (valid) {
      // P6.1: 操作题必须配置评分项，且比例值合计必须为100
      if (form.value.questionType === "practical" && form.value.practicalMode === "FILE") {
        if (!form.value.practicalAllowedExtensionList?.length) {
          ElMessage.warning("请至少选择一种学生提交格式");
          return;
        }
        if (!form.value.scoringItems || form.value.scoringItems.length === 0) {
          ElMessage.warning("操作题必须配置至少一个评分项");
          return;
        }
        if (scoringItemsSum.value !== 100) {
          ElMessage.warning(
            "评分项比例值合计必须为100，当前为 " + scoringItemsSum.value
          );
          return;
        }
        form.value.practicalAllowedExtensions = form.value.practicalAllowedExtensionList.join(",");
        form.value.practicalMaterials = [
          ...splitMaterialPaths(form.value.practicalResourceFiles, "RESOURCE"),
          ...splitMaterialPaths(form.value.practicalReferenceFiles, "REFERENCE"),
        ];
      }

      if (isFlowchartPracticalQuestion(form.value)) {
        const config = form.value.flowchartConfig || defaultFlowchartConfig();
        if (parseFlowchartDocument(config.answerJson).nodes.length === 0) {
          ElMessage.warning("请先制作画程标准答案");
          return;
        }
        if (parseFlowchartDocument(config.starterJson).nodes.length === 0) {
          ElMessage.warning("请先制作发给学生的基础流程图");
          return;
        }
        if (!hasFlowchartRules(config.rulesJson)) {
          ElMessage.warning("请在画程的“检查规则”中从标准答案生成规则");
          return;
        }
        form.value.filePath = null;
        form.value.practicalMaterials = [];
        form.value.scoringItems = [];
      }

      if (isPythonPracticalQuestion(form.value)) {
        const basicMessage = validatePythonBasics();
        const caseMessage = validatePythonCases();
        if (basicMessage || caseMessage) { ElMessage.warning(basicMessage || caseMessage); return; }
        // Python 题是通用技能题，年级、学期和课次只在课程或题单关联层表达。
        form.value.grade = null; form.value.semester = null; form.value.lessonNum = null; form.value.isPublic = "Y";
        if (form.value.programming.noInput === "1") {
          form.value.programming.inputDescription = "本题没有输入。";
          form.value.programming.testCases.forEach((item) => { item.inputText = ""; });
        }
      }

      // P6: 添加Loading效果
      const loadingInstance = ElLoading.service({
        lock: true,
        text: "正在保存数据...（操作题若包含文件转换可能需要较长时间，请耐心等待）",
        background: "rgba(0, 0, 0, 0.7)",
      });

      validating.value = validateAfterSave;
      try {
        let questionId = form.value.questionId;
        if (questionId != null) {
          await updateQuestion(form.value);
        } else {
          const response = await addQuestion(form.value);
          questionId = response.questionId || response.data?.questionId;
          form.value.questionId = questionId;
        }
        if (isPythonPracticalQuestion(form.value)) {
          if (!questionId) throw new Error("保存题目后未返回题目 ID");
          await saveProgrammingQuestion(questionId, form.value.programming);
          if (validateAfterSave) {
            const validation = await validateProgrammingQuestion(questionId);
            form.value.programming.validationStatus = validation.data?.validationStatus || "INVALID";
            const passed = validation.data?.passedCount || 0;
            const totalCases = validation.data?.totalCount || 0;
            if (!validation.data?.valid) {
              pythonStep.value = 2;
              ElMessage.error(`验证未通过：${passed}/${totalCases} 个测试点通过`);
              return;
            }
            proxy.$modal.msgSuccess(`验证通过：${passed}/${totalCases} 个测试点全部通过`);
          } else {
            proxy.$modal.msgSuccess("Python 题已保存，验证状态已重置为待验证");
          }
        } else if (isFlowchartPracticalQuestion(form.value)) {
          if (!questionId) throw new Error("保存题目后未返回题目 ID");
          const saved = await saveFlowchartQuestion(questionId, {
            ...form.value.flowchartConfig,
            questionId,
          });
          form.value.flowchartConfig = saved.data || form.value.flowchartConfig;
          proxy.$modal.msgSuccess("画程流程图题已保存");
        } else {
          proxy.$modal.msgSuccess(questionId === form.value.questionId ? "保存成功" : "新增成功");
        }
        open.value = false;
        getList();
      } catch (error) {
        if (validateAfterSave) pythonStep.value = 2;
      } finally {
        validating.value = false;
        loadingInstance.close();
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _questionIds = row.questionId || ids.value;
  proxy.$modal
    .confirm('是否确认删除题库管理编号为"' + _questionIds + '"的数据项？')
    .then(function () {
      return delQuestion(_questionIds);
    })
    .then(() => {
      getList();
      proxy.$modal.msgSuccess("删除成功");
    })
    .catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  // 检查是否筛选了操作题
  if (queryParams.value.questionType === 'practical') {
    proxy.$modal.msgWarning("操作题包含附件文件，无法导出到Excel。请选择其他题型进行导出。");
    return;
  }
  proxy.download(
    "business/question/export",
    {
      ...queryParams.value,
    },
    `question_${new Date().getTime()}.xlsx`
  );
}

/** 导入按钮操作 */
function handleImport() {
  refreshUploadHeaders();
  upload.title = "题目导入";
  upload.open = true;
}

/** 下载模板操作 */
function importTemplate() {
  proxy.download(
    "business/question/importTemplate",
    {},
    `question_template_${new Date().getTime()}.xlsx`
  );
}

let uploadLoadingInstance;

/**文件上传中处理 */
const handleFileUploadProgress = (event, file, fileList) => {
  upload.isUploading = true;
  uploadLoadingInstance = ElLoading.service({ text: "正在导入数据，请稍候", background: "rgba(0, 0, 0, 0.7)" });
};

/** 文件上传成功处理 */
const handleFileSuccess = (response, file, fileList) => {
  if (isSessionExpiredCode(response?.code)) {
    upload.isUploading = false;
    proxy.$refs["uploadRef"].clearFiles();
    if (uploadLoadingInstance) {
      uploadLoadingInstance.close();
    }
    handleSessionExpired(response?.msg);
    return;
  }
  upload.open = false;
  upload.isUploading = false;
  proxy.$refs["uploadRef"].clearFiles();
  if (uploadLoadingInstance) {
    uploadLoadingInstance.close();
  }
  proxy.$alert(
    "<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" +
      response.msg +
      "</div>",
    "导入结果",
    { dangerouslyUseHTMLString: true }
  );
  getList();
};

/** 文件上传失败处理 */
const handleFileError = (err, file, fileList) => {
  upload.isUploading = false;
  if (uploadLoadingInstance) {
    uploadLoadingInstance.close();
  }
  if (isSessionExpiredError(err)) {
    handleSessionExpired();
    return;
  }
  proxy.$modal.msgError("上传失败");
};

/** 提交上传文件 */
function submitFileForm() {
  refreshUploadHeaders();
  proxy.$refs["uploadRef"].submit();
}

/** 预览操作题附件 */
function handlePreview(row) {
  if (isFlowchartPracticalQuestion(row)) {
    flowchartPreviewQuestion.value = row;
    flowchartPreviewVisible.value = true;
    return;
  }
  if (row.previewPath) {
    const baseUrl = import.meta.env.VITE_APP_BASE_API;
    // /profile/** 已禁止静态直读，必须通过服务端做登录态和题目归属校验。
    const fullUrl = `${baseUrl}/common/resource/view?resource=${encodeURIComponent(row.previewPath)}`;
    window.open(fullUrl, "_blank");
  } else {
    proxy.$modal.msgWarning("该题目没有可预览的附件");
  }
}

getList();
</script>

<style scoped>
.bank-view-tabs{margin-bottom:16px}.python-wizard{margin-top:18px;padding:18px;border:1px solid #e4e7ed;border-radius:8px;background:#fafbfc}.python-steps{margin-bottom:24px}.python-step-panel{min-height:390px;padding:18px 10px 4px;background:#fff;border-radius:6px}.case-summary{display:flex;align-items:center;justify-content:space-between;margin:14px 0 10px}.case-summary>div{display:flex;gap:8px;flex-wrap:wrap}.case-table{margin-top:14px}.case-actions{display:flex;align-items:center;gap:12px;margin-top:12px;color:#909399;font-size:12px}.validation-alert{margin-bottom:16px}.wizard-actions{display:flex;justify-content:center;gap:10px;margin-top:16px}.python-import-actions{display:flex;align-items:center;gap:14px;margin:18px 0}.import-report{margin-top:14px}.import-alert{margin:12px 0}.import-errors{max-height:260px;margin-top:12px;padding:10px 16px;overflow:auto;background:#fff5f5;color:#b42318;border-radius:6px}.import-errors h4{margin:0 0 8px}.import-errors li{margin:5px 0}
</style>
