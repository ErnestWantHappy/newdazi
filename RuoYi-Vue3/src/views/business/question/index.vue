<template>
  <div class="app-container qw">
    <!-- 顶部：标题、次要操作、默认展开的筛选区 -->
    <header class="qw__head">
      <div class="qw__title">
        <h2>题库工作台</h2>
        <p>按题型整理题目，从这里直接开始出题</p>
      </div>
      <div class="qw__head-ops">
        <el-dropdown v-if="canImport || canExport" trigger="click" @command="handleMoreCommand">
          <el-button>导入导出<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-if="canImport" command="import-common">导入常规题（Excel）</el-dropdown-item>
              <el-dropdown-item v-if="canImport" command="import-python">导入 Python 题（双 Sheet）</el-dropdown-item>
              <el-dropdown-item v-if="canExport" command="export" divided>导出当前题型（操作题不可导出）</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>
    <el-alert v-if="lastChoiceSave" type="success" :closable="true" class="choice-save-result" @close="lastChoiceSave=null">
      <template #title>{{ lastChoiceSave.forked ? '已保留历史版本，本次修改已保存为新题，原课程保持不变。' : '题目已保存。' }}</template>
      <el-button link type="primary" @click="handleDetail({ questionId:lastChoiceSave.questionId })">查看本次保存的题目</el-button>
      <el-button v-if="hasPermi('business:question:edit')" link type="primary" @click="handleUpdate({ questionId:lastChoiceSave.questionId })">继续编辑</el-button>
    </el-alert>

    <!-- 筛选默认展开：第一排搜索/年级/学期/课次，第二排公开、创建人、难度与搜索操作 -->
    <section class="qw__filters" :class="{ 'is-collapsed': !filtersOpen }">
      <el-form v-show="filtersOpen" ref="queryRef" :model="filters" :inline="true" class="qw__filter-form" @submit.prevent="handleQuery">
        <div class="qw__filter-row">
          <el-form-item label="题目搜索">
            <el-input v-model="filters.questionContent" placeholder="搜索题干、Python 标题" clearable style="width: 220px" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="年级">
            <el-select v-model="filters.grade" placeholder="全部年级" clearable style="width: 140px">
              <el-option v-for="dict in biz_grade" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="学期">
            <el-select v-model="filters.semester" placeholder="全部学期" clearable style="width: 130px">
              <el-option v-for="dict in biz_semester" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="课次">
            <el-select v-model="filters.lessonNum" placeholder="全部课次" clearable style="width: 120px">
              <el-option v-for="n in 20" :key="n" :label="'第' + n + '课'" :value="n" />
            </el-select>
          </el-form-item>
        </div>
        <div class="qw__filter-row">
          <el-form-item label="公开状态">
            <el-select v-model="filters.isPublic" placeholder="全部" clearable style="width: 120px">
              <el-option v-for="dict in sys_yes_no" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="创建人">
            <el-input v-model="filters.createBy" placeholder="姓名或账号" clearable style="width: 150px" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="Python 难度">
            <el-select v-model="filters.difficulty" placeholder="全部" clearable style="width: 110px">
              <el-option label="简单" value="SIMPLE" />
              <el-option label="中等" value="MEDIUM" />
              <el-option label="困难" value="HARD" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            <el-button link type="primary" @click="filtersOpen = false">收起筛选<el-icon><ArrowUp /></el-icon></el-button>
          </el-form-item>
        </div>
      </el-form>
      <div v-if="!filtersOpen" class="qw__filter-collapsed">
        <span>筛选条件已收起</span>
        <el-button link type="primary" @click="filtersOpen = true">展开筛选<el-icon><ArrowDown /></el-icon></el-button>
      </div>
      <p class="qw__scope-note">
        年级、学期、课次只作用于选择/判断/打字/文件作品/流程图；Python 题按难度筛选；数据分析实验不绑定这些字段。
      </p>
    </section>

    <template v-if="!viewingAll">
      <!-- 第一行：打字题独占整行 -->
      <workbench-section
        title="打字题"
        icon="Grid"
        tone="typing"
        :state="sections.typing"
        :can-add="canAdd"
        :grade-dict="biz_grade"
        :semester-dict="biz_semester"
        :actions-of="actionsOf"
        @add="handleAddFrom('typing')"
        @view-all="openAll(scopeOf('typing'))"
        @retry="loadModule('typing')"
        @load-more="loadMore('typing')"
        @view="handleDetail"
        @edit="handleUpdate"
        @remove="handleDelete"
        @primary="handleDetail"
      />

      <!-- 第二行：选择题左、判断题右 -->
      <div class="qw__pair">
        <workbench-section
          class="qw__half"
          title="选择题"
          icon="List"
          tone="choice simple"
          :state="sections.choice"
          :can-add="canAdd"
          :grade-dict="biz_grade"
          :semester-dict="biz_semester"
          :actions-of="actionsOf"
          @add="handleAddFrom('choice')"
          @view-all="openAll(scopeOf('choice'))"
          @retry="loadModule('choice')"
          @load-more="loadMore('choice')"
          @view="handleDetail"
          @edit="handleUpdate"
          @remove="handleDelete"
          @primary="handleDetail"
        />
        <workbench-section
          class="qw__half"
          title="判断题"
          icon="Check"
          tone="judgment simple"
          :state="sections.judgment"
          :can-add="canAdd"
          :grade-dict="biz_grade"
          :semester-dict="biz_semester"
          :actions-of="actionsOf"
          @add="handleAddFrom('judgment')"
          @view-all="openAll(scopeOf('judgment'))"
          @retry="loadModule('judgment')"
          @load-more="loadMore('judgment')"
          @view="handleDetail"
          @edit="handleUpdate"
          @remove="handleDelete"
          @primary="handleDetail"
        />
      </div>

      <!-- 第三行：操作题整行，四种作答方式 + 当前方式题目 -->
      <workbench-section
        title="操作题"
        icon="Document"
        tone="practical"
        hint="选择作答方式，下方列出该方式的题目"
        :state="sections.practical"
        :can-add="false"
        :show-all="false"
        :grade-dict="biz_grade"
        :semester-dict="biz_semester"
        :actions-of="actionsOf"
        @retry="loadModule('practical')"
        @load-more="loadMore('practical')"
        @view="handleDetail"
        @preview="handlePreview"
        @edit="handleUpdate"
        @remove="handleDelete"
        @primary="handlePrimary"
      >
        <practical-mode-picker
          :model-value="practicalMode"
          :counts="practicalCounts"
          :failed-counts="failedPracticalModes"
          :can-add="canAdd"
          @update:model-value="changePracticalMode"
          @add="handleAddFromPractical"
          @retry-count="retryPracticalModeTotal"
          @view-all="openAll(practicalScope())"
        />
      </workbench-section>
    </template>

    <!-- 查看全部：完整卡片列表、真实分页与筛选，返回保留工作台状态 -->
    <section v-else class="qw__all">
      <header class="qw__all-head">
        <div>
          <h3>{{ allViewTitle }}<span v-if="allState.error" class="qw__total">题量未知</span><span v-else-if="allState.loaded" class="qw__total">{{ allState.total }} 题</span></h3>
          <p>按当前筛选显示该范围的完整题目，可翻页后继续查看。</p>
        </div>
        <div class="qw__ops">
          <el-button v-if="canBulkManage && !selectionMode" @click="enterSelection">批量管理</el-button>
          <el-button icon="Back" @click="closeAll">返回工作台</el-button>
          <el-button v-if="canAdd" type="primary" plain @click="handleAddFromScope"><el-icon><Plus /></el-icon>新增{{ activeScope.title }}</el-button>
        </div>
      </header>
      <div v-if="selectionMode" class="qw__bulk">
        <el-checkbox v-model="allChecked" :indeterminate="someChecked" @change="toggleAllChecked">全选本页</el-checkbox>
        <span>已选 {{ selectedIds.length }} 题<template v-if="selectedIds.length && !canManageSelected">，其中包含他人题目，不能批量处理</template></span>
        <el-button v-if="hasPermi('business:question:edit')" size="small" :disabled="selectedIds.length !== 1 || !canManageSelected" @click="editSelected">编辑选中题目</el-button>
        <el-button v-if="hasPermi('business:question:remove')" size="small" type="danger" plain :disabled="!selectedIds.length || !canManageSelected" @click="deleteSelected">删除选中题目</el-button>
        <el-button size="small" @click="exitSelection">退出批量管理</el-button>
      </div>
      <div v-if="allState.loading && !allState.loaded" class="qw__state">正在加载题目…</div>
      <div v-else-if="allState.error" class="qw__state qw__state--error">
        加载失败，请重试。<el-button link type="primary" @click="loadAllList">重试</el-button>
      </div>
      <template v-else>
        <div v-if="!allState.items.length" class="qw__state">该题型下暂无符合条件的题目。</div>
        <div v-else class="qw__all-grid" :class="`qw__all-grid--${activeScope.grid}`">
          <div v-for="row in allState.items" :key="row.questionId" class="qw__all-item">
            <el-checkbox v-if="selectionMode" :model-value="selectedIds.includes(row.questionId)" :disabled="!canManageQuestion(row)" @change="toggleSelected(row, $event)" />
            <question-card
              :question="row"
              :actions="actionsOf(row)"
              :grade-dict="biz_grade"
              :semester-dict="biz_semester"
              @view="handleDetail(row)"
              @preview="handlePreview(row)"
              @edit="handleUpdate(row)"
              @remove="handleDelete(row)"
              @primary="handlePrimary(row)"
            />
          </div>
        </div>
        <pagination v-show="allState.total > 0" :total="allState.total" v-model:page="allQuery.pageNum" v-model:limit="allQuery.pageSize" @pagination="loadAllList" />
      </template>
    </section>

    <!-- 以下弹窗与编辑器保持原有实现，确保复杂编辑能力不丢失 -->
    <el-dialog :title="title" v-model="open" :class="{ 'choice-question-editor':form.questionType==='choice' }" :width="isFlowchartPracticalQuestion(form) ? '96%' : form.questionType==='choice' ? 'min(980px, 96vw)' : '980px'" append-to-body>
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
          <div style="width:100%" @paste="form.questionType === 'choice' && stemMediaInput?.paste($event)" @dragover.prevent @drop="form.questionType === 'choice' && stemMediaInput?.drop($event)">
          <el-input
            v-model="form.questionContent"
            :rows="questionContentRows"
            type="textarea"
            placeholder="请输入内容"
          />
          <QuestionMediaInput v-if="form.questionType === 'choice'" :key="form.saveRequestId" ref="stemMediaInput" v-model="form.media.stem" :limit="3" label="题干图片" :context="{ questionId: form.questionId }" :disabled="submitting" @blocked="questionImageBlocked.stem=$event" />
          </div>
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
          <ChoiceOptionFields :key="form.saveRequestId" v-model:question="form" :disabled="submitting" @blocked="questionImageBlocked.options=$event" />
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
            <!-- 验证失败时把每个测试点的输入、期望、实际和原因直接摆出来，老师不用猜 -->
            <el-table v-if="validationFailures.length" :data="validationFailures" border size="small" class="validation-diff">
              <el-table-column label="测试点" prop="caseName" width="130" />
              <el-table-column label="程序输入"><template #default="{ row }"><pre>{{ row.inputText || '（无输入）' }}</pre></template></el-table-column>
              <el-table-column label="你填的期望输出"><template #default="{ row }"><pre>{{ row.expectedOutput || '（空）' }}</pre></template></el-table-column>
              <el-table-column label="程序实际输出"><template #default="{ row }"><pre>{{ row.actualOutput || '（空）' }}</pre></template></el-table-column>
              <el-table-column label="原因" min-width="220"><template #default="{ row }"><span class="diff-hint">{{ row.hint || row.errorSummary || '输出和期望不一致' }}</span></template></el-table-column>
            </el-table>
            <el-collapse v-if="validationFailures.length" class="validation-more">
              <el-collapse-item title="按参考代码重新生成期望输出" name="generate">
                <p class="generate-note">系统会用参考代码跑一遍每个测试点，把结果填进“期望输出”。只改本页内容，保存后才生效。</p>
                <el-button type="primary" plain :loading="generatingOutputs" @click="generateExpectedOutputs">用参考代码生成输出</el-button>
              </el-collapse-item>
            </el-collapse>
            <el-form-item label="起始代码"><el-input v-model="form.programming.starterCode" type="textarea" :rows="5" placeholder="可留空；如填写，只给出必要结构，不泄露解法" /></el-form-item>
            <el-form-item label="参考代码" prop="answer" required><el-input v-model="form.answer" type="textarea" :rows="10" placeholder="仅教师可见。保存后系统会用这份代码跑完全部测试点。" /></el-form-item>
            <p class="answer-tip">可以放心写 input("提示文字")，系统会识别提示语，不会因此判错。</p>
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
              :file-type="['doc', 'docx', 'pdf', 'ppt', 'pptx', 'xls', 'xlsx', 'jpg', 'jpeg', 'png', 'txt', 'html', 'htm', 'zip', 'rar', '7z']"
              :file-size="50"
              :limit="1"
            />
            <div style="color: #909399; font-size: 12px; margin-top: 6px;">
              可不上传；如上传，学生可下载它继续完成作品。压缩包（ZIP/RAR/7Z）不生成在线预览，仅供下载。
            </div>
          </el-form-item>
          <el-form-item label="学生提交格式">
            <el-checkbox-group v-model="form.practicalAllowedExtensionList">
              <el-checkbox v-for="item in practicalExtensionOptions" :key="item" :value="item">{{ item.toUpperCase() }}</el-checkbox>
            </el-checkbox-group>
            <div class="el-form-item__tip">PY 可查看源码和运行控制台程序；TXT 以文本预览；HTML/HTM 请提交单个网页（内联 CSS/JS）；ZIP/RAR/7Z 仅下载查看。这些格式由老师手动评分。PY/TXT/HTML 不超过 2 MiB。</div>
          </el-form-item>
          <el-form-item label="图片上限">
            <el-input-number v-model="form.practicalImageMaxCount" :min="1" :max="10" />
            <span style="margin-left: 10px; color: #909399; font-size: 12px;">仅在学生提交图片组时生效</span>
          </el-form-item>
          <el-form-item label="补充资源">
            <file-upload
              v-model="form.practicalResourceFiles"
              :file-type="['doc', 'docx', 'pdf', 'ppt', 'pptx', 'xls', 'xlsx', 'jpg', 'jpeg', 'png', 'txt', 'html', 'htm', 'css', 'zip', 'rar', '7z']"
              :file-size="50"
              :limit="5"
            />
            <div style="color: #909399; font-size: 12px; margin-top: 6px;">学生可见，可包含 CSS、ZIP/RAR/7Z 资源包；不作为学生提交格式。</div>
          </el-form-item>
          <el-form-item label="教师参考答案">
            <file-upload
              v-model="form.practicalReferenceFiles"
              :file-type="['doc', 'docx', 'pdf', 'ppt', 'pptx', 'xls', 'xlsx', 'jpg', 'jpeg', 'png', 'txt', 'html', 'htm', 'zip', 'rar', '7z']"
              :file-size="50"
              :limit="5"
            />
            <div style="color: #e6a23c; font-size: 12px; margin-top: 6px;">仅教师和 AI 批改可见，不会下发给学生；压缩包不能作为可识别答案，AI 批改时会跳过。</div>
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
            <flowchart-question-designer v-model="form.flowchartConfig" :question-meta="flowchartAiMeta" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button v-if="!isPythonPracticalQuestion(form)" type="primary" :loading="submitting" :disabled="submitting || Object.values(questionImageBlocked).some(Boolean)" @click="submitForm(false)">保存题目</el-button>
          <template v-else>
            <el-button :loading="submitting && !validating" :disabled="submitting" @click="submitForm(false)">保存草稿</el-button>
            <el-button type="primary" :loading="submitting || validating" :disabled="submitting" @click="submitForm(true)">保存并验证全部测试点</el-button>
          </template>
          <el-button :disabled="submitting" @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="pythonImportOpen" title="导入 Python OJ 题目" width="760px" append-to-body>
      <el-alert title="Excel 必须包含“题目”和“测试点”两个 Sheet。每道题的测试点权重合计必须为 100（可写 20% 或 20，空权重按 1 计）。上传只做预检，不会立即写入题库。" type="info" :closable="false" />
      <div class="python-import-actions">
        <el-upload :auto-upload="false" accept=".xlsx,.xls" :show-file-list="false" :on-change="handlePythonWorkbook">
          <el-button type="primary" :loading="pythonImportLoading">选择 Excel 并预检</el-button>
        </el-upload>
        <el-button link type="primary" @click="downloadPythonTemplate">下载双 Sheet 模板</el-button>
        <el-button v-if="pythonImportReport && !pythonImportReport.ready" link type="success" @click="averageImportWeights">平均分配权重并重新预检</el-button>
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
          <el-button type="primary" :loading="upload.isUploading" :disabled="upload.isUploading" @click="submitFileForm">确 定</el-button>
          <el-button @click="cancelUpload">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="importErrorVisible" title="题库导入校验未通过" width="min(760px, 94vw)" append-to-body>
      <p class="import-error-summary">共 {{ importErrors.length }} 处错误，整批未导入。请按行号修正后重新上传。</p>
      <el-table :data="importErrors" size="small" max-height="420" border>
        <el-table-column prop="row" label="Excel 行" width="90" />
        <el-table-column prop="reason" label="错误原因" min-width="280" show-overflow-tooltip />
      </el-table>
      <template #footer>
        <el-button @click="downloadImportErrors">下载错误清单</el-button>
        <el-button type="primary" @click="importErrorVisible = false">关 闭</el-button>
      </template>
    </el-dialog>

    <python-question-preview-dialog v-model="pythonPreviewVisible" :question="pythonPreviewQuestion" :can-read-answer="!!pythonPreviewQuestion && canManageQuestion(pythonPreviewQuestion) && hasPermi('business:question:edit')" />
    <flowchart-question-preview-dialog v-model="flowchartPreviewVisible" :question="flowchartPreviewQuestion" :can-read-answer="!!flowchartPreviewQuestion && canManageQuestion(flowchartPreviewQuestion)" />
    <el-dialog v-model="detailVisible" title="题目详情" width="min(760px, 94vw)" append-to-body>
      <div v-loading="detailLoading" class="question-detail">
        <template v-if="detailQuestion">
          <p class="detail-text">{{ detailQuestion.questionContent }}</p>
          <QuestionMedia :question="detailQuestion" />
          <template v-if="detailQuestion.questionType === 'choice'">
            <div v-for="key in ['A', 'B', 'C', 'D']" :key="key" class="detail-text">{{ key }}. {{ detailQuestion['option' + key] }}<QuestionMedia :question="detailQuestion" :slot="key" /></div>
          </template>
          <template v-if="['choice', 'judgment'].includes(detailQuestion.questionType)">
            <el-divider />
            <p class="detail-text">标准答案：{{ detailQuestion.answer || '未设置' }}</p>
            <p class="detail-text">解析：{{ detailQuestion.analysis || '暂无解析' }}</p>
          </template>
        </template>
        <el-empty v-else-if="!detailLoading" description="题目读取失败，请关闭后重试" />
      </div>
    </el-dialog>
    <!-- 路由切页动画要求单一根节点，弹窗组件也须保留在页面根节点内。 -->
    <DataLabEditor ref="dataLabEditor" @saved="onSaved" />
    <QuestionImportPreview v-model="questionImportVisible" @completed="refreshAll" @view="id => { questionImportVisible=false;handleDetail({ questionId:id }) }" />
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
import { getProgrammingQuestion, saveProgrammingQuestion, validateProgrammingQuestion, previewProgrammingImport, confirmProgrammingImport, generateProgrammingOutputs } from "@/api/business/programming";
import { getFlowchartQuestion, saveFlowchartQuestion } from "@/api/business/flowchart";
import FlowchartQuestionDesigner from "@/components/FlowchartEditor/FlowchartQuestionDesigner.vue";
import FlowchartQuestionPreviewDialog from "@/components/FlowchartEditor/FlowchartQuestionPreviewDialog.vue";
import PythonQuestionPreviewDialog from './components/PythonQuestionPreviewDialog.vue';
import DataLabEditor from "@/components/DataLabExperiment/Editor.vue";
import WorkbenchSection from "./components/WorkbenchSection.vue";
import QuestionCard from "./components/QuestionCard.vue";
import PracticalModePicker from "./components/PracticalModePicker.vue";
import { EMPTY_FLOWCHART, DEFAULT_FLOWCHART_PERMISSIONS, parseFlowchartDocument } from "@/components/FlowchartEditor/schema";
import {
  WORKBENCH_DEFAULTS,
  buildAllListQuery,
  buildModuleQuery,
  cardActions,
  createModuleState,
  hasMoreAfter,
  mergeQuestionPage,
  practicalModeMeta,
  scopeOf,
  supportsGradeScope,
} from "@/utils/questionWorkbench";
import { computed, getCurrentInstance, nextTick, reactive, ref, toRefs, watch } from "vue";
import { ElLoading, ElMessage } from "element-plus"; // P6 import
import * as XLSX from "xlsx";
import {
  handleSessionExpired,
  isSessionExpiredCode,
  isSessionExpiredError,
  refreshAuthorizationHeader
} from "@/utils/session";
import useUserStore from "@/store/modules/user";
import QuestionMedia from '@/components/QuestionMedia/QuestionMedia.vue';
import QuestionMediaInput from '@/components/QuestionMedia/QuestionMediaInput.vue';
import ChoiceOptionFields from '@/components/QuestionMedia/ChoiceOptionFields.vue';
import QuestionImportPreview from './components/QuestionImportPreview.vue';
import { emptyQuestionMedia, normalizeQuestionMedia, hasChoiceOption, slotImages, newQuestionRequestId } from '@/utils/questionMedia';
import { getQuestionSaveReceipt } from '@/api/business/questionImages';
const stemMediaInput = ref(null);
const questionImageBlocked = ref({ stem: false, options: false });
const questionImportVisible = ref(false);
const lastChoiceSave = ref(null);

const { proxy } = getCurrentInstance();
const userStore = useUserStore();
try { lastChoiceSave.value = JSON.parse(localStorage.getItem(`choice-last-saved:${userStore.id}`) || 'null'); } catch { /* 本机提示不影响服务器题库。 */ }
watch(lastChoiceSave, value => { try { if(value) localStorage.setItem(`choice-last-saved:${userStore.id}`,JSON.stringify(value)); else localStorage.removeItem(`choice-last-saved:${userStore.id}`); } catch {} });
const dataLabEditor = ref(null);
const { biz_question_type, sys_yes_no, biz_grade, biz_semester } =
  proxy.useDict("biz_question_type", "sys_yes_no", "biz_grade", "biz_semester");

// --- 按钮级权限：沿用 v-hasPermi 同一份权限码，这里读出来决定是否渲染，不留禁用占位 ---
const permissions = ref(Array.isArray(userStore.permissions) ? userStore.permissions : []);
const isAdmin = computed(() => (userStore.roles || []).includes("admin"));
const hasPermi = code => permissions.value.includes("*:*:*") || permissions.value.includes(code);
const canAdd = computed(() => hasPermi("business:question:add"));
const canImport = computed(() => hasPermi("business:question:import"));
const canExport = computed(() => hasPermi("business:question:export"));

// --- 工作台状态 ---
// 筛选输入与“已应用筛选”分开：只有点搜索或回车才刷新，避免每敲一个字就请求所有模块。
function emptyFilters() {
  return { questionContent: null, grade: null, semester: null, lessonNum: null, isPublic: null, createBy: null, difficulty: null };
}
const filters = ref(emptyFilters());
const appliedFilters = ref(emptyFilters());
const filtersOpen = ref(true);
const practicalMode = ref("FILE");
const sections = reactive({
  typing: createModuleState("typing"),
  choice: createModuleState("choice"),
  judgment: createModuleState("judgment"),
  practical: createModuleState("practical"),
});
const viewingAll = ref(false);
const allState = reactive({ items: [], total: 0, loaded: false, loading: false, error: false, requestId: 0 });
const allQuery = reactive({ pageNum: 1, pageSize: 10 });
const selectionMode = ref(false);
const selectedIds = ref([]);
const selectedQuestions = ref([]);
const allChecked = ref(false);

// 工作台滚动位置按账号隔离保存在内存，返回时恢复；不写 URL，也不写持久缓存。
const workbenchScroll = ref(0);
const activeScopeKey = ref("typing");

const activeScope = computed(() => scopeOf(activeScopeKey.value));
const allViewTitle = computed(() =>
  activeScope.value.title === "操作题" ? `操作题 · ${practicalModeMeta(practicalMode.value).label}` : activeScope.value.title
);

function actionsOf(row) {
  return cardActions(row, {
    permissions: permissions.value,
    userId: userStore.id,
    isAdmin: isAdmin.value,
  });
}

function canManageQuestion(question) {
  return isAdmin.value || String(question?.creatorId || "") === String(userStore.id || "");
}

const canManageSelected = computed(() => selectedQuestions.value.length > 0 && selectedQuestions.value.every(canManageQuestion));
// 查看全部里的批量管理入口：只有具备对应权限时才出现，不把批量当隐藏功能也不留空入口。
const canBulkManage = computed(() => hasPermi("business:question:edit") || hasPermi("business:question:remove"));
const someChecked = computed(() => allState.items.some(row => selectedIds.value.includes(row.questionId)));

// 模态框状态（沿用原实现）
const open = ref(false);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const title = ref("");
const pythonStep = ref(0);
const validating = ref(false);
const submitting = ref(false);
// 新增过程中是否已经建成主记录：失败重试只补配置，不重复建题。
const createdQuestionId = ref(null);
const modifyStartQuestionId = ref(null);
// 新增/修改开始时的题型与操作方式：修改题型后旧分类也要刷新。
const startQuestionType = ref(null);
const startPracticalMode = ref(null);
const pythonImportOpen = ref(false);
const pythonImportLoading = ref(false);
const pythonImportConfirming = ref(false);
const pythonImportReport = ref(null);
// 最近一次解析出的导入载荷，用于“平均分配权重”后重新预检（用户主动点击才改权重）。
const lastPythonPayload = ref(null);
const flowchartPreviewVisible = ref(false);
const pythonPreviewVisible = ref(false);
const pythonPreviewQuestion = ref(null);
const flowchartPreviewQuestion = ref(null);
const originalFlowchartConfig = ref(null);
const detailVisible = ref(false);
const detailLoading = ref(false);
const detailQuestion = ref(null);
let detailRequestId = 0;

const data = reactive({
  form: {},
});

const { form } = toRefs(data);

function moduleState(key) {
  if (key === "practical") return sections.practical;
  return sections[key];
}

/**
 * 加载或刷新某个模块。
 * 每类用自己的查询范围与条数有界加载；requestId 丢弃过期响应，避免慢请求覆盖新筛选结果。
 */
async function loadModule(key, { append = false } = {}) {
  const state = moduleState(key);
  const scope = key === "practical" ? practicalScope() : scopeOf(key);
  const pageSize = key === "practical" ? WORKBENCH_DEFAULTS.practical : WORKBENCH_DEFAULTS[key];
  if (append) {
    if (state.loadingMore || !state.hasMore) return;
    state.loadingMore = true;
    state.loadMoreError = false;
  } else {
    state.requestId += 1;
    state.loading = true;
    state.error = false;
    state.loadMoreError = false;
    state.hasMore = false;
    state.items = [];
    state.pageNum = 1;
  }
  const requestId = state.requestId;
  const pageNum = append ? state.pageNum + 1 : 1;
  try {
    const response = await listQuestion(buildModuleQuery(appliedFilters.value, scope, pageNum, pageSize));
    if (requestId !== state.requestId) return;
    const rows = response.rows || [];
    if (append) {
      const merged = mergeQuestionPage(state.items, rows, response.total);
      state.items = merged.items;
      state.hasMore = merged.hasMore;
      state.pageNum = pageNum;
    } else {
      state.items = rows;
      state.total = Number(response.total || 0);
      state.hasMore = hasMoreAfter({ total: state.total }, rows.length);
    }
    state.total = Number(response.total || state.total || 0);
    state.loaded = true;
  } catch (error) {
    if (requestId !== state.requestId) return;
    // 追加失败保留已有列表；首次加载失败只标记本模块，其余模块照常显示。
    if (append) state.loadMoreError = true;
    else state.error = true;
  } finally {
    if (requestId === state.requestId) {
      state.loading = false;
      state.loadingMore = false;
    }
  }
}

function loadMore(key) {
  return loadModule(key, { append: true });
}

/** 刷新全部模块；保存、删除、导入后统一走这里，避免旧请求把旧题或旧数量写回来。 */
function refreshAll() {
  loadPracticalModeTotals();
  return Promise.all([loadModule("typing"), loadModule("choice"), loadModule("judgment"), loadModule("practical")]);
}

function handleQuery() {
  appliedFilters.value = { ...filters.value, grade: filters.value.grade || null };
  refreshAll();
  if (viewingAll.value) loadAllList();
}

function resetQuery() {
  filters.value = emptyFilters();
  proxy.resetForm("queryRef");
  appliedFilters.value = emptyFilters();
  refreshAll();
  if (viewingAll.value) loadAllList();
}

// 操作题列表按“当前方式”查询：未选方式时中文案是 total，必须显式传方式，否则会退回整类操作题。
function changePracticalMode(mode) {
  if (!mode || mode === practicalMode.value) return;
  practicalMode.value = mode;
  loadModule("practical");
}

/** 操作题模块的查询范围 = 操作题 + 当前作答方式。 */
function practicalScope() {
  return scopeOf(practicalMode.value);
}

// 每种方式仅查询一条以取得真实 total，不拉取整类数据，也不重复累计全操作题。
// 查询失败时为 null，入口显示“题量未知”，不能把失败或局部条数当成 0。
const PRACTICAL_MODE_KEYS = ["FILE", "PYTHON", "DATA_LAB", "FLOWCHART"];
const modeTotals = reactive({ counts: null, failed: [], requestId: 0 });

async function loadPracticalModeTotals() {
  const requestId = ++modeTotals.requestId;
  // 四种方式是独立统计；单个弱网失败不能把其他三种已经拿到的真实题量一并丢弃。
  const results = await Promise.allSettled(PRACTICAL_MODE_KEYS.map(mode => listQuestion({
    ...buildModuleQuery(appliedFilters.value, scopeOf(mode), 1, 1),
  })));
  if (requestId !== modeTotals.requestId) return;
  const counts = {};
  const failed = [];
  PRACTICAL_MODE_KEYS.forEach((mode, index) => {
    const result = results[index];
    if (result.status === 'fulfilled') counts[mode] = Number(result.value.total || 0);
    else { counts[mode] = null; failed.push(mode); }
  });
  modeTotals.counts = counts;
  modeTotals.failed = failed;
}

async function retryPracticalModeTotal(mode) {
  if (!PRACTICAL_MODE_KEYS.includes(mode)) return;
  const requestId = modeTotals.requestId;
  try {
    const response = await listQuestion({
      ...buildModuleQuery(appliedFilters.value, scopeOf(mode), 1, 1),
    });
    if (requestId !== modeTotals.requestId) return;
    modeTotals.counts = { ...(modeTotals.counts || {}), [mode]: Number(response.total || 0) };
    modeTotals.failed = modeTotals.failed.filter(item => item !== mode);
  } catch (_) {
    if (requestId !== modeTotals.requestId) return;
    modeTotals.counts = { ...(modeTotals.counts || {}), [mode]: null };
    if (!modeTotals.failed.includes(mode)) modeTotals.failed = [...modeTotals.failed, mode];
  }
}

const practicalCounts = computed(() => modeTotals.counts);
const failedPracticalModes = computed(() => modeTotals.failed);

/** “查看全部”：在当前页切换视图，不新增菜单和路由。 */
function openAll(scope) {
  activeScopeKey.value = scope.key;
  allQuery.pageNum = 1;
  selectionMode.value = false;
  selectedIds.value = [];
  selectedQuestions.value = [];
  viewingAll.value = true;
  if (typeof window !== "undefined") workbenchScroll.value = window.scrollY;
  loadAllList();
  nextTick(() => window.scrollTo({ top: 0 }));
}

function closeAll() {
  viewingAll.value = false;
  nextTick(() => window.scrollTo({ top: workbenchScroll.value || 0 }));
}

async function loadAllList() {
  clearSelection();
  allState.requestId += 1;
  const requestId = allState.requestId;
  allState.loading = true;
  allState.error = false;
  try {
    const response = await listQuestion(buildAllListQuery(appliedFilters.value, activeScope.value, allQuery.pageNum, allQuery.pageSize));
    if (requestId !== allState.requestId) return;
    allState.items = response.rows || [];
    allState.total = Number(response.total || 0);
    allState.loaded = true;
    // 删除最后一条后回退到有效页，避免停在空页。
    if (!allState.items.length && allState.total > 0 && allQuery.pageNum > 1) {
      allQuery.pageNum = Math.ceil(allState.total / allQuery.pageSize);
      return loadAllList();
    }
  } catch (error) {
    if (requestId === allState.requestId) allState.error = true;
  } finally {
    if (requestId === allState.requestId) allState.loading = false;
  }
}

async function handlePrimary(row) {
  const action = actionsOf(row).find(item => item === "preview") || (actionsOf(row).includes("view") ? "view" : "");
  if (action === "preview") return handlePreview(row);
  if (action === "view") return handleDetail(row);
}

// --- 详情 ---
async function handleDetail(row) {
  const requestId = ++detailRequestId;
  detailVisible.value = true;
  detailLoading.value = true;
  detailQuestion.value = null;
  try {
    const response = await getQuestion(row.questionId);
    if (requestId === detailRequestId) detailQuestion.value = response.data;
  } catch (_) {
    // 请求层已提示失败，详情不能继续显示上一题的内容。
  } finally {
    if (requestId === detailRequestId) detailLoading.value = false;
  }
}

function openPythonImport() {
  pythonImportReport.value = null;
  lastPythonPayload.value = null;
  pythonImportOpen.value = true;
}

/** 导出能力收敛到顶部下拉，操作题仍明确提示无法导出 Excel。 */
function handleMoreCommand(command) {
  if (command === "import-common") return handleImport();
  if (command === "import-python") return openPythonImport();
  if (command === "export") return handleExport();
}

function cell(row, ...names) {
  for (const name of names) if (row[name] !== undefined && row[name] !== null) return row[name];
  return "";
}
function parseNumber(value, fallback) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : fallback;
}

// 权重列：允许 "20"、"20%"、"20％"、首尾空格；空值沿用兜底，不静默改写用户数字。
function parseWeight(value, fallback) {
  if (value === undefined || value === null) return fallback;
  const text = String(value).trim().replace(/[%％]$/, '').trim();
  if (!text) return fallback;
  const parsed = Number(text);
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
        externalId: String(cell(row, "外部题号", "externalId")), caseName: String(cell(row, "用例名称", "caseName")), inputText: String(cell(row, "输入", "inputText")), expectedOutput: String(cell(row, "期望输出", "expectedOutput")), isPublic: String(cell(row, "是否公开", "isPublic")), scoreWeight: parseWeight(cell(row, "权重", "scoreWeight"), 1), orderNum: parseNumber(cell(row, "顺序", "orderNum"), index + 1),
      })),
    };
    lastPythonPayload.value = payload;
    pythonImportReport.value = (await previewProgrammingImport(payload)).data || {};
    if (pythonImportReport.value.ready) ElMessage.success("预检通过，可以确认导入"); else ElMessage.warning(`预检发现 ${pythonImportReport.value.errors?.length || 0} 个问题`);
  } catch (error) {
    ElMessage.error(error?.message || "Excel 解析或预检失败");
  } finally {
    pythonImportLoading.value = false;
  }
}

// 用户主动平均分配：按外部题号分组，组内权重均分到合计 100（保留两位小数，余数归最后一项），再重新预检。
async function averageImportWeights() {
  const payload = lastPythonPayload.value;
  if (!payload) return;
  const groups = {};
  payload.testCases.forEach(tc => { (groups[tc.externalId] = groups[tc.externalId] || []).push(tc); });
  Object.values(groups).forEach(list => {
    if (!list.length) return;
    const base = Math.floor(10000 / list.length) / 100;
    let assigned = 0;
    list.forEach((tc, i) => {
      tc.scoreWeight = i === list.length - 1 ? Math.round((100 - assigned) * 100) / 100 : base;
      assigned += tc.scoreWeight;
    });
  });
  pythonImportLoading.value = true;
  try {
    pythonImportReport.value = (await previewProgrammingImport(payload)).data || {};
    if (pythonImportReport.value.ready) ElMessage.success("权重已平均分配，预检通过"); else ElMessage.warning(`已平均分配权重，但仍有 ${pythonImportReport.value.errors?.length || 0} 个问题待修正`);
  } catch (error) {
    ElMessage.error(error?.message || "Excel 解析或预检失败");
  } finally {
    pythonImportLoading.value = false;
  }
}

// 模板列名必须与 handlePythonWorkbook 的取值列名完全一致，否则导入读不到数据。
function downloadPythonTemplate() {
  const templateQuestions = [{
    "外部题号": "PY001", "标题": "两数之和", "难度": "easy", "知识点": "输入输出",
    "题目描述": "读入两个整数，输出它们的和。",
    "输入格式": "一行两个整数，以空格分隔。",
    "输出格式": "一个整数，表示两数之和。",
    "数据范围": "|a|,|b| <= 10^9",
    "样例解释": "3 + 5 = 8", "提示": "注意数据范围，使用长整型。",
    "起始代码": "",
    "参考代码": "a, b = map(int, input().split())\nprint(a + b)",
    "无输入题": "否", "是否公开": "是", "时间限制(秒)": 2, "内存限制(KB)": 131072,
  }];
  const cases = [
    { "外部题号": "PY001", "用例名称": "样例1", "输入": "3 5\n", "期望输出": "8\n", "是否公开": "是", "权重": 50, "顺序": 1 },
    { "外部题号": "PY001", "用例名称": "负数边界", "输入": "-10 4\n", "期望输出": "-6\n", "是否公开": "否", "权重": 50, "顺序": 2 },
  ];
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, XLSX.utils.json_to_sheet(templateQuestions), "题目");
  XLSX.utils.book_append_sheet(workbook, XLSX.utils.json_to_sheet(cases), "测试点");
  XLSX.writeFile(workbook, "Python_OJ题目双Sheet导入模板.xlsx");
}

async function confirmPythonWorkbook() {
  const token = pythonImportReport.value?.confirmToken;
  if (!token) return;
  pythonImportConfirming.value = true;
  try {
    const result = (await confirmProgrammingImport(token)).data || {};
    ElMessage.success(`已成功导入 ${result.importedCount || 0} 道已验证 Python 题`);
    pythonImportOpen.value = false; pythonImportReport.value = null; refreshAll();
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
  questionImageBlocked.value = { stem: false, options: false };
  pythonStep.value = 0;
  originalFlowchartConfig.value = null;
  createdQuestionId.value = null;
  modifyStartQuestionId.value = null;
  form.value = {
    questionId: null,
    media: emptyQuestionMedia(),
    saveRequestId: newQuestionRequestId(),
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
    // 新建题默认勾选全部可提交格式，减少逐项勾选；编辑已有题时由后端值覆盖
    practicalAllowedExtensions: "doc,docx,pdf,ppt,pptx,xls,xlsx,jpg,jpeg,png,txt,html,htm,py,zip,rar,7z",
    practicalMode: "FILE",
    practicalAllowedExtensionList: ["doc", "docx", "pdf", "ppt", "pptx", "xls", "xlsx", "jpg", "jpeg", "png", "txt", "html", "htm", "py", "zip", "rar", "7z"],
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

const practicalExtensionOptions = ["doc", "docx", "pdf", "ppt", "pptx", "xls", "xlsx", "jpg", "jpeg", "png", "txt", "html", "htm", "py", "zip", "rar", "7z"];

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

// 供“拍照 + AI 生成流程图标准答案”使用：把题目名称、年级/学期标签和第几课传给设计器，全部取本题已填字段。
const flowchartAiMeta = computed(() => {
  const gradeDict = (biz_grade.value || []).find(item => parseInt(item.value, 10) === Number(form.value.grade))
  const semesterDict = (biz_semester.value || []).find(item => item.value === form.value.semester)
  return {
    title: form.value.questionContent,
    gradeText: gradeDict ? gradeDict.label : '',
    semesterText: semesterDict ? semesterDict.label : '',
    lessonNum: form.value.lessonNum
  }
})

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

/**
 * 操作题名称等基础信息与流程图配置分开保存，避免未改图时无意义地写入配置并占用乐观锁修订号。
 */
function comparableFlowchartConfig(config) {
  return {
    starterJson: canonicalizeFlowchartJson(config?.starterJson),
    answerJson: canonicalizeFlowchartJson(config?.answerJson),
    permissionsJson: canonicalizeFlowchartJson(config?.permissionsJson),
    rulesJson: canonicalizeFlowchartJson(config?.rulesJson),
  };
}

function canonicalizeFlowchartJson(value) {
  try {
    return JSON.stringify(sortFlowchartJson(JSON.parse(value || '{}')));
  } catch (_) {
    return String(value || '');
  }
}

function sortFlowchartJson(value) {
  if (Array.isArray(value)) return value.map(sortFlowchartJson);
  if (value && typeof value === 'object') {
    return Object.keys(value).sort().reduce((result, key) => {
      result[key] = sortFlowchartJson(value[key]);
      return result;
    }, {});
  }
  return value;
}

function flowchartConfigChanged(config) {
  return JSON.stringify(comparableFlowchartConfig(config)) !== JSON.stringify(originalFlowchartConfig.value);
}

function hasFlowchartRules(rulesJson) {
  try {
    const rules = JSON.parse(rulesJson || "[]");
    return Array.isArray(rules) && rules.length > 0;
  } catch (_) {
    return false;
  }
}

/**
 * 与后端 FlowchartDocumentService 保持同一拦截口径，提前指出具体图元；题干富文本不走这里。
 */
function invalidFlowchartTextMessage(documentJson, documentName) {
  let document
  try {
    document = JSON.parse(documentJson || '{}')
  } catch (_) {
    return ''
  }
  const unsafeText = (value) => {
    const text = typeof value === 'object' && value ? String(value.value || '') : String(value || '')
    return /<\/?[A-Za-z][A-Za-z0-9]*(?:\s+[A-Za-z_:][A-Za-z0-9:._-]*(?:\s*=\s*(?:"[^"]*"|'[^']*'|[^\s"'=<>`]+))?)*\s*\/?>/i.test(text)
      || /<!--[\s\S]*?-->/.test(text)
      || /\bjavascript\s*:/i.test(text)
  }
  const entries = [
    ...(Array.isArray(document.nodes) ? document.nodes : []).map((item, index) => ({ item, label: `第 ${index + 1} 个节点` })),
    ...(Array.isArray(document.edges) ? document.edges : []).map((item, index) => ({ item, label: `第 ${index + 1} 条连线` }))
  ]
  const invalid = entries.find(({ item }) => unsafeText(item?.text))
  return invalid
    ? `${documentName}${invalid.label}的文字不能包含 HTML 标签、HTML 注释或 javascript: 脚本文本，请删除后再保存`
    : ''
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
const validationCases = ref([]);
const generatingOutputs = ref(false);
// 只展示未通过的测试点，通过的不占版面
const validationFailures = computed(() => (validationCases.value || []).filter(item => item.statusCode !== "ACCEPTED"));

/**
 * 用参考代码跑出真实输出并回填。
 * 只改本页表单，教师还需点保存；这样不会静默覆盖已人工核对的期望输出。
 */
async function generateExpectedOutputs() {
  const questionId = form.value.questionId || createdQuestionId.value;
  if (!questionId) return ElMessage.warning("请先保存题目，再生成期望输出");
  if (!String(form.value.answer || "").trim()) return ElMessage.warning("请先填写参考代码");
  generatingOutputs.value = true;
  try {
    const result = (await generateProgrammingOutputs(questionId)).data || {};
    const rows = result.cases || [];
    const byId = new Map(rows.map(item => [String(item.testCaseId), item]));
    let filled = 0;
    for (const testCase of form.value.programming?.testCases || []) {
      const matched = byId.get(String(testCase.testCaseId));
      if (matched && matched.statusCode === "ACCEPTED") {
        testCase.expectedOutput = matched.expectedOutput ?? "";
        filled++;
      }
    }
    if (filled) {
      ElMessage.success(`已按参考代码填写 ${filled} 个测试点的期望输出，请核对后保存`);
    } else {
      ElMessage.error("参考代码未能正常运行，请先检查代码或测试点输入");
    }
  } catch (error) {
    ElMessage.error(error?.msg || error?.message || "生成期望输出失败，请稍后重试");
  } finally {
    generatingOutputs.value = false;
  }
}
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

// 多选框选中数据（查看全部内的批量管理）
function handleSelectionChange(selection) {
  selectedQuestions.value = selection;
  ids.value = selection.map((item) => item.questionId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

function toggleAllChecked(checked) {
  const manageable = allState.items.filter(canManageQuestion);
  selectedIds.value = checked ? manageable.map(row => row.questionId) : [];
  selectedQuestions.value = allState.items.filter(row => selectedIds.value.includes(row.questionId));
  allChecked.value = checked;
}

function toggleSelected(row, checked) {
  if (checked) {
    if (!canManageQuestion(row)) return;
    selectedIds.value = [...selectedIds.value, row.questionId];
  } else {
    selectedIds.value = selectedIds.value.filter(id => id !== row.questionId);
  }
  selectedQuestions.value = allState.items.filter(item => selectedIds.value.includes(item.questionId));
}

function exitSelection() {
  clearSelection();
  selectionMode.value = false;
}

function clearSelection() {
  selectedIds.value = [];
  selectedQuestions.value = [];
  allChecked.value = false;
}

function editSelected() {
  if (!hasPermi('business:question:edit') || !canManageSelected.value || selectedIds.value.length !== 1) return;
  const target = allState.items.find(row => row.questionId === selectedIds.value[0]);
  if (target && canManageQuestion(target)) return handleUpdate(target);
}

function deleteSelected() {
  if (!hasPermi('business:question:remove') || !canManageSelected.value || !selectedIds.value.length) return;
  // 只处理当前可见页的选择，分页或重新筛选后不携带隐藏题目。
  const targets = allState.items.filter(row => selectedIds.value.includes(row.questionId));
  if (targets.length !== selectedIds.value.length || !targets.every(canManageQuestion)) return;
  return handleDelete({ questionIds: targets.map(row => row.questionId) });
}

function enterSelection() {
  selectionMode.value = true;
}

/**
 * 按模块新增：重置表单后直接带入题型与操作方式，教师不必再选一遍。
 * 年级/学期/课次只在当前筛选明确选择时带入，未选择时不猜测。
 */
function handleAddFrom(key) {
  const scope = scopeOf(key);
  beginAdd(scope.questionType, scope.practicalMode || null);
}

function handleAddFromPractical(mode) {
  beginAdd("practical", mode);
}

function handleAddFromScope() {
  const scope = activeScope.value;
  beginAdd(scope.questionType, scope.practicalMode || null);
}

const QUESTION_TYPE_NAMES = { typing: "打字题", choice: "选择题", judgment: "判断题", practical: "操作题" };

function addTitle(questionType, practicalMode) {
  if (questionType === "practical") return `新增${practicalModeMeta(practicalMode || "FILE").label}题`;
  return `新增${QUESTION_TYPE_NAMES[questionType] || "题目"}`;
}

function beginAdd(questionType, practicalMode = null) {
  // DATA_LAB 使用独立编辑器：直接打开，不塞进普通操作题表单。
  if (questionType === "practical" && practicalMode === "DATA_LAB") {
    dataLabEditor.value?.open();
    return;
  }
  reset();
  typingDurationCustomized.value = false;
  form.value.questionType = questionType;
  if (questionType === "practical") form.value.practicalMode = practicalMode || "FILE";
  const scope = practicalMode ? practicalModeMeta(practicalMode) : null;
  if (supportsGradeScope({ questionType, practicalMode })) {
    const applied = appliedFilters.value;
    if (applied.grade) form.value.grade = Number(applied.grade);
    if (applied.semester) form.value.semester = applied.semester;
    if (applied.lessonNum) form.value.lessonNum = Number(applied.lessonNum);
  }
  title.value = addTitle(questionType, practicalMode);
  startQuestionType.value = questionType;
  startPracticalMode.value = practicalMode;
  open.value = true;
  nextTick(() => proxy.$refs.questionRef?.clearValidate());
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const _questionId = row.questionId || ids.value[0];
  if (!_questionId) return ElMessage.warning("请选择要编辑的题目");
  getQuestion(_questionId).then((response) => {
    if (response.data?.practicalMode === 'DATA_LAB') {
      dataLabEditor.value?.open(_questionId);
      return;
    }
    form.value = response.data;
    form.value.media = normalizeQuestionMedia(form.value.media);
    form.value.expectedSourceFingerprint = form.value.sourceFingerprint;
    form.value.saveRequestId = newQuestionRequestId();
    modifyStartQuestionId.value = form.value.questionId;
    form.value.programming = defaultProgrammingConfig();
    form.value.practicalAllowedExtensionList = String(
      form.value.practicalAllowedExtensions || "doc,docx,pdf,ppt,pptx,xls,xlsx,jpg,jpeg,png"
    ).split(",").filter(Boolean);
    form.value.practicalResourceFiles = materialPaths(form.value.practicalMaterials, "RESOURCE");
    form.value.practicalReferenceFiles = materialPaths(form.value.practicalMaterials, "REFERENCE");
    typingDurationCustomized.value = form.value.questionType === "typing" && form.value.typingDuration != null;
    const showEditor = () => {
      startQuestionType.value = form.value.questionType;
      startPracticalMode.value = form.value.practicalMode || null;
      open.value = true;
      title.value = `编辑${form.value.questionType === "practical" ? practicalModeMeta(form.value.practicalMode).label : QUESTION_TYPE_NAMES[form.value.questionType] || ""}题`;
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
        originalFlowchartConfig.value = comparableFlowchartConfig(form.value.flowchartConfig);
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

/**
 * 保存后定位新题：切到对应模块并刷新；若当前筛选可能看不到它，明确提示，不硬插进列表。
 * 四个模块始终一起刷新，因此修改题型后旧分类与新分类的题目和计数都会更新。
 */
function afterQuestionSaved(questionType, savedMode) {
  if (savedMode && ["FILE", "PYTHON", "DATA_LAB", "FLOWCHART"].includes(savedMode)) {
    practicalMode.value = savedMode;
  }
  const scopeKey = questionType === "practical" ? (savedMode || "FILE") : questionType;
  if (viewingAll.value) {
    activeScopeKey.value = scopeOf(scopeKey).key;
    allQuery.pageNum = 1;
  }
  open.value = false;
  refreshAll();
  if (viewingAll.value) loadAllList();
}

function newQuestionFilterHint(questionType, practicalMode) {
  const scoped = supportsGradeScope({ questionType, practicalMode });
  const hidden = Boolean(
    appliedFilters.value.questionContent
    || appliedFilters.value.createBy
    || appliedFilters.value.isPublic
    || (scoped && (appliedFilters.value.grade || appliedFilters.value.semester || appliedFilters.value.lessonNum))
    || (practicalMode === "PYTHON" && appliedFilters.value.difficulty)
  );
  return hidden ? "已保存，但当前筛选条件下可能看不到这道新题，可清除筛选后查看。" : "";
}

/** 保存普通题，或保存 Python 题后用参考代码验证全部测试点。 */
function submitForm(validateAfterSave = false) {
  if (submitting.value) return;
  if (Object.values(questionImageBlocked.value).some(Boolean)) return ElMessage.warning('请先处理上传中或失败的图片');
  proxy.$refs["questionRef"].validate(async (valid) => {
    if (!valid) return;
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
        const unsafeTextMessage = invalidFlowchartTextMessage(config.starterJson, '学生基础图')
          || invalidFlowchartTextMessage(config.answerJson, '标准答案')
        if (unsafeTextMessage) {
          ElMessage.warning(unsafeTextMessage)
          return
        }
        if (parseFlowchartDocument(config.answerJson).nodes.length > 0 && !hasFlowchartRules(config.rulesJson)) {
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

      submitting.value = true;
      validating.value = validateAfterSave;
      let savedQuestionId = null;
      const questionType = form.value.questionType;
      const practicalMode = questionType === "practical" ? (form.value.practicalMode || "FILE") : null;
      const isNew = form.value.questionId == null;
      try {
        let questionId = form.value.questionId;
        if (questionId != null) {
          // 编辑：失败时保留原记录，下次重试仍是更新，不会重复建题。
          const saved = await updateQuestion(form.value);
          if (questionType === 'choice') {
            questionId = saved.questionId || saved.data?.questionId || questionId;
            form.value.questionId = questionId;
            lastChoiceSave.value = { questionId, forked: saved.saveMode === 'FORKED' };
          }
        } else if (createdQuestionId.value != null) {
          // 上一步已建好主记录但配置保存失败：只更新，不重复新增。
          form.value.questionId = createdQuestionId.value;
          await updateQuestion(form.value);
          questionId = createdQuestionId.value;
        } else {
          const response = await addQuestion(form.value);
          questionId = response.questionId || response.data?.questionId;
          form.value.questionId = questionId;
          createdQuestionId.value = questionId;
          if (questionType === 'choice') lastChoiceSave.value = { questionId, forked: false };
        }
        if (isPythonPracticalQuestion(form.value)) {
          if (!questionId) throw new Error("保存题目后未返回题目 ID");
          await saveProgrammingQuestion(questionId, form.value.programming);
          if (validateAfterSave) {
            const validation = await validateProgrammingQuestion(questionId);
            form.value.programming.validationStatus = validation.data?.validationStatus || "INVALID";
            validationCases.value = validation.data?.cases || [];
            const passed = validation.data?.passedCount || 0;
            const totalCases = validation.data?.totalCount || 0;
            if (!validation.data?.valid) {
              pythonStep.value = 2;
              ElMessage.error(`未通过验证：${passed}/${totalCases} 个测试点通过，题目已保存为草稿；第 3 步已列出具体差异`);
              // 题目本身已写入，列表与计数必须刷新，否则教师会以为没有保存。
              afterQuestionSaved(questionType, practicalMode);
              open.value = true;
              title.value = addTitle(questionType, practicalMode).replace("新增", "编辑");
              return;
            }
          }
          afterQuestionSaved(questionType, practicalMode);
        } else if (isFlowchartPracticalQuestion(form.value)) {
          if (!questionId) throw new Error("保存题目后未返回题目 ID");
          if (flowchartConfigChanged(form.value.flowchartConfig)) {
            const saved = await saveFlowchartQuestion(questionId, {
              ...form.value.flowchartConfig,
              questionId,
            });
            form.value.flowchartConfig = saved.data || form.value.flowchartConfig;
            originalFlowchartConfig.value = comparableFlowchartConfig(form.value.flowchartConfig);
          }
          afterQuestionSaved(questionType, practicalMode);
        } else {
          afterQuestionSaved(questionType, practicalMode);
        }
        savedQuestionId = questionId;
        createdQuestionId.value = null;
        const savedType = form.value.questionType;
        const savedMode = savedType === "practical" ? (form.value.practicalMode || "FILE") : null;
        if (!isPythonPracticalQuestion(form.value) || !validateAfterSave || form.value.programming.validationStatus === "VALID") {
          if (isNew) {
            const hint = newQuestionFilterHint(savedType, savedMode);
            if (hint) ElMessage.warning(hint);
            else ElMessage.success("新增成功，已显示在对应题型中");
          } else {
            ElMessage.success(lastChoiceSave.value?.forked && savedType === 'choice' ? '已保留历史版本，本次修改已保存为新题，原课程不变' : (questionType !== savedType || practicalMode !== savedMode ? "已保存，题型已更新" : "保存成功"));
          }
        }
      } catch (error) {
        if(questionType === 'choice') {
          try {
            const recovered=(await getQuestionSaveReceipt(form.value.saveRequestId)).data;
            if(recovered?.questionId) {
              const saved=(await getQuestion(recovered.questionId)).data;
              form.value.questionId=recovered.questionId;
              form.value.expectedSourceFingerprint=saved.sourceFingerprint;
              form.value.saveRequestId=newQuestionRequestId();
              createdQuestionId.value=recovered.questionId;
              lastChoiceSave.value={questionId:recovered.questionId,forked:recovered.saveMode==='FORKED'};
              refreshAll();
              ElMessage.success('已确认上次保存成功；当前表单已保留，可继续修改后保存');
              return recovered.questionId;
            }
          } catch { /* 无法核对时保留原请求键，重试仍可安全查询原结果。 */ }
        }
        // 失败保留表单、附件与已建记录：再次点击保存只会补写配置。
        if (validateAfterSave) pythonStep.value = 2;
        if (!error?.__messageShown) {
          ElMessage.error(isNew && createdQuestionId.value ? "题目已建立，后续配置保存失败，可直接重试" : "保存失败，表单内容已保留，可重试");
        }
      } finally {
        validating.value = false;
        submitting.value = false;
        loadingInstance.close();
      }
      return savedQuestionId;
  });
}

/** 删除按钮操作：单条与批量共用确认文案 */
function handleDelete(row) {
  const _questionIds = row?.questionIds?.length ? row.questionIds : (row?.questionId || ids.value);
  const label = Array.isArray(_questionIds) || String(_questionIds).includes(",")
    ? `所选 ${String(_questionIds).split(",").length} 道题目`
    : `题目「${(row?.programmingTitle || row?.questionContent || _questionIds).toString().slice(0, 24)}」`;
  proxy.$modal
    .confirm(`确认删除${label}？删除后无法恢复。`)
    .then(function () {
      return delQuestion(_questionIds);
    })
    .then(() => {
      proxy.$modal.msgSuccess("删除成功");
      refreshAll();
      if (viewingAll.value) loadAllList();
      selectedIds.value = [];
      selectedQuestions.value = [];
      selectionMode.value = false;
    })
    .catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  // 检查是否筛选了操作题
  if (activeScope.value.questionType === 'practical') {
    proxy.$modal.msgWarning("操作题包含附件文件，无法导出到Excel。请切换到其他题型再导出。");
    return;
  }
  proxy.download(
    "business/question/export",
    {
      ...buildAllListQuery(appliedFilters.value, activeScope.value, 1, 10000),
    },
    `question_${new Date().getTime()}.xlsx`
  );
}

/** 导入按钮操作 */
function handleImport() {
  questionImportVisible.value = true;
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

/*** 题目导入参数 */
const upload = reactive({
  open: false,
  title: "",
  isUploading: false,
  updateSupport: 0,
  headers: refreshAuthorizationHeader(),
  url: import.meta.env.VITE_APP_BASE_API + "/business/question/importData",
});
const importErrors = ref([]);
const importErrorVisible = ref(false);

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
      questionContent: [{ validator: (_r,_v,done) => done(String(form.value.questionContent || '').trim() || slotImages(form.value).length ? undefined : new Error('题干需填写文字或添加图片')) }],
      ...Object.fromEntries(['A','B','C','D'].map(key => ['option'+key, [{ validator: (_r,_v,done) => done(hasChoiceOption(form.value,key) || (form.value.questionId && ['C','D'].includes(key) && form.value.answer !== key) ? undefined : new Error('选项'+key+'需填写文字或添加图片')) }]])),
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

// 数据分析实验题保存后由编辑器自行提示，这里只刷新工作台。
function onSaved() {
  refreshAll();
  if (viewingAll.value) loadAllList();
}

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
  const details = Array.isArray(response?.details) ? response.details : [];
  if (details.length) {
    importErrors.value = details;
    importErrorVisible.value = true;
    return;
  }
  proxy.$alert(
    "<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" +
      response.msg +
      "</div>",
    "导入结果",
    { dangerouslyUseHTMLString: true }
  );
  refreshAll();
}

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
  if (upload.isUploading) return;
  const uploadRef = proxy.$refs["uploadRef"];
  // Element Plus 未暴露 uploadFiles，按已渲染的文件项判断是否选了文件。
  const hasFile = !!uploadRef?.$el?.querySelector(".el-upload-list__item");
  if (!hasFile) {
    proxy.$modal.msgWarning("请先选择要导入的 Excel 文件");
    return;
  }
  refreshUploadHeaders();
  upload.isUploading = true;
  uploadRef.submit();
}

/** 导出题库导入错误清单，便于老师按行号批量修正 */
function downloadImportErrors() {
  const rows = [["Excel 行", "错误原因"], ...importErrors.value.map(item => [item.row, item.reason])];
  const csv = "﻿" + rows
    .map(row => row.map(value => `"${String(value ?? "").replace(/"/g, '""')}"`).join(","))
    .join("\n");
  const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = `题库导入错误_${new Date().getTime()}.csv`;
  link.click();
  URL.revokeObjectURL(url);
}

/** 预览操作题附件 */
function handlePreview(row) {
  if (isPythonPracticalQuestion(row)) {
    pythonPreviewQuestion.value = row;
    pythonPreviewVisible.value = true;
    return;
  }
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

// 首次进入：筛选默认展开，四个模块各自有界加载。
refreshAll();
</script>

<style scoped>
/* 工作台：浅灰底、白色模块、细边框；题目与操作优先，不做装饰性视觉。 */
.qw { padding-bottom: 24px; background: #f5f6f8; }
.qw__head { display: flex; flex-wrap: wrap; gap: 12px; align-items: flex-start; justify-content: space-between; padding: 16px 18px 4px; }
.qw__title h2 { margin: 0; font-size: 22px; color: #1f2329; }
.qw__title p { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
.qw__head-ops { display: flex; gap: 8px; }
.qw__filters { margin: 12px 0; padding: 14px 16px; border: 1px solid #e6e9ef; border-radius: 12px; background: #fff; }
.qw__filter-form :deep(.el-form-item) { margin-bottom: 10px; margin-right: 14px; }
.qw__filter-row { display: flex; flex-wrap: wrap; align-items: center; }
.qw__scope-note { margin: 2px 0 0; color: #8a9099; font-size: 12px; line-height: 1.6; }
.qw__filter-collapsed { display: flex; gap: 10px; align-items: center; color: #6b7280; font-size: 13px; }
.qw__pair { display: grid; gap: 12px; margin: 12px 0; grid-template-columns: repeat(2, minmax(0, 1fr)); }
.qw__half { min-width: 0; }
.qw__all { padding: 14px 16px 20px; border: 1px solid #e6e9ef; border-radius: 12px; background: #fff; }
.qw__all-head { display: flex; flex-wrap: wrap; gap: 10px; align-items: flex-start; justify-content: space-between; }
.qw__all-head h3 { display: flex; gap: 8px; align-items: center; margin: 0; font-size: 18px; color: #1f2329; }
.qw__all-head p { margin: 4px 0 0; color: #8a9099; font-size: 12px; }
.qw__total { padding: 1px 8px; border-radius: 10px; background: #f2f4f7; color: #5b6472; font-size: 12px; font-weight: 400; }
.qw__ops { display: flex; flex-wrap: wrap; gap: 8px; }
.qw__bulk { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; margin-top: 12px; padding: 8px 12px; border-radius: 8px; background: #f5f8ff; color: #5b6472; font-size: 12px; }
.qw__all-grid { display: grid; gap: 12px; margin-top: 12px; grid-template-columns: minmax(0, 1fr); }
.qw__all-item { display: flex; gap: 8px; align-items: flex-start; min-width: 0; }
.qw__all-item > .qcard { flex: 1; min-width: 0; }
.qw__state { margin-top: 12px; padding: 24px; border: 1px dashed #e6e9ef; border-radius: 8px; color: #8a9099; text-align: center; }
.qw__state--error { color: #c45656; }
@media (min-width: 1200px) {
  .qw__all-grid--simple { grid-template-columns: repeat(3, minmax(0, 1fr)); }
  .qw__all-grid--practical { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}
@media (max-width: 768px) {
  .qw__head { padding: 12px; }
  .qw__filters { padding: 10px 12px; }
  /* 窄屏：打字题→选择题→判断题→操作题顺序转单列 */
  .qw__pair { grid-template-columns: minmax(0, 1fr); }
  .qw__filter-form :deep(.el-form-item) { margin-right: 8px; }
}
/* 保留原有编辑器辅助样式 */
.detail-text { white-space: pre-wrap; overflow-wrap: anywhere; line-height: 1.8; }
.question-detail { min-height: 120px; }
.import-error-summary { margin: 0 0 12px; color: #b42318; font-weight: 700; }
.python-wizard{margin-top:18px;padding:18px;border:1px solid #e4e7ed;border-radius:8px;background:#fafbfc}.python-steps{margin-bottom:24px}.python-step-panel{min-height:390px;padding:18px 10px 4px;background:#fff;border-radius:6px}.case-summary{display:flex;align-items:center;justify-content:space-between;margin:14px 0 10px}.case-summary>div{display:flex;gap:8px;flex-wrap:wrap}.case-table{margin-top:14px}.case-actions{display:flex;align-items:center;gap:12px;margin-top:12px;color:#909399;font-size:12px}.validation-alert{margin-bottom:16px}.wizard-actions{display:flex;justify-content:center;gap:10px;margin-top:16px}.python-import-actions{display:flex;align-items:center;gap:14px;margin:18px 0}.import-report{margin-top:14px}.import-alert{margin:12px 0}.import-errors{max-height:260px;margin-top:12px;padding:10px 16px;overflow:auto;background:#fff5f5;color:#b42318;border-radius:6px}.import-errors h4{margin:0 0 8px}.import-errors li{margin:5px 0}
</style>
<style>
@media(max-width:640px){
  .choice-question-editor .el-col-12{flex:0 0 100%;max-width:100%}
  .choice-question-editor .el-form-item__label{width:78px!important}
  .choice-question-editor .el-dialog__body{padding:12px 4px}
  .choice-question-editor .el-dialog__footer{position:sticky;bottom:0;background:white;padding:12px 4px;border-top:1px solid #ebeef5}
}
</style>
