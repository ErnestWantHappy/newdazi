<template>
  <div class="app-container python-practice-page">
    <div class="page-heading">
      <div><h2>Python 练习题单</h2><p>像设计课程一样选班级、挑题目、发布；需要加练时再建一份普通题单。</p></div>
      <el-button v-if="canManage" type="primary" @click="openCreate">新建题单</el-button>
    </div>

    <el-card shadow="never" class="list-card">
      <el-table v-loading="loading" :data="plans" row-key="plan_id">
        <el-table-column prop="plan_name" label="题单名称" min-width="190" />
        <el-table-column label="目标班级" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">{{ row.class_names || '尚未选择班级' }}</template>
        </el-table-column>
        <el-table-column prop="question_count" label="题数" width="72" align="center" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.version_status === 'DRAFT' && row.published_version_id" type="warning">有待发布修改</el-tag>
            <el-tag v-else-if="row.version_status === 'PUBLISHED'" type="success">已发布</el-tag>
            <el-tag v-else type="info">草稿</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button v-if="canManage" link type="primary" @click="openDesigner(row)">设计题单</el-button>
            <el-button v-if="canViewAnalytics && row.published_version_id" link type="primary" @click="openAnalytics(row)">查看学情</el-button>
            <el-button v-if="canManage" link type="danger" @click="removePlan(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !plans.length" description="暂无练习题单" />
    </el-card>

    <el-dialog v-model="createVisible" title="新建练习题单" width="620px">
      <el-form label-width="90px">
        <el-form-item label="题单名称"><el-input v-model="createForm.planName" maxlength="128" show-word-limit placeholder="例如：循环结构第 1 次练习" /></el-form-item>
        <el-form-item label="目标班级">
          <el-select v-model="createForm.classKeys" multiple filterable collapse-tags collapse-tags-tooltip placeholder="至少选择一个班级" style="width:100%">
            <el-option v-for="item in managedClasses" :key="classKey(item)" :value="classKey(item)" :label="classLabel(item)" />
          </el-select>
          <div class="field-tip"><el-button link type="primary" @click="selectAllClasses(createForm)">选择全部管理班级</el-button>“全年级”就是把该届班级全部选中。</div>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="createVisible=false">取消</el-button><el-button type="primary" :loading="saving" @click="createPlan">创建并选题</el-button></template>
    </el-dialog>

    <el-drawer v-model="designerVisible" size="94%" :with-header="false" class="designer-drawer">
      <div v-if="currentPlan" class="designer-shell">
        <header class="designer-header">
          <div><el-button link @click="designerVisible=false">← 返回题单</el-button><strong>{{ currentPlan.plan_name }}</strong><el-tag v-if="currentPlan.version_status==='DRAFT'" type="warning">草稿版本</el-tag><el-tag v-else type="success">已发布</el-tag></div>
          <div><el-button @click="saveSettings">保存设置</el-button><el-button type="success" :loading="publishing" @click="publish">发布题单</el-button></div>
        </header>
        <section class="settings-bar">
          <el-input v-model="editForm.planName" maxlength="128" placeholder="题单名称" />
          <el-select v-model="editForm.classKeys" multiple filterable collapse-tags collapse-tags-tooltip placeholder="选择目标班级">
            <el-option v-for="item in managedClasses" :key="classKey(item)" :value="classKey(item)" :label="classLabel(item)" />
          </el-select>
          <el-button @click="selectAllClasses(editForm)">全选管理班级</el-button>
          <span class="draft-tip">已发布题单的修改先保存为草稿，重新发布后学生才看到变化。</span>
        </section>

        <main class="designer-main">
          <section class="selected-pane">
            <div class="pane-title"><span>已选题目 <b>{{ selectedQuestions.length }}</b></span><el-button type="primary" plain :loading="recommending" @click="recommend">一键推荐 12 题</el-button></div>
            <el-scrollbar class="question-scroll">
              <div v-for="(item,index) in selectedQuestions" :key="item.question_id" class="selected-item">
                <span class="order">{{ index + 1 }}</span>
                <div class="selected-body"><b>{{ item.question_title || item.question_content }}</b><small>{{ item.knowledge_points || '未分类' }} · {{ difficultyLabel(item.difficulty) }}</small></div>
                <div class="selected-actions"><el-button link :disabled="index===0" @click="moveQuestion(index,-1)">上移</el-button><el-button link :disabled="index===selectedQuestions.length-1" @click="moveQuestion(index,1)">下移</el-button><el-button link type="danger" @click="removeQuestion(item)">移除</el-button></div>
              </div>
              <el-empty v-if="!selectedQuestions.length" description="从右侧题库批量加入题目" />
            </el-scrollbar>
          </section>

          <section class="bank-pane">
            <div class="pane-title"><span>Python 题库</span><el-button type="primary" :disabled="!batchSelection.length" @click="batchAdd">加入所选（{{ batchSelection.length }}）</el-button></div>
            <el-form :inline="true" class="bank-filter" @submit.prevent>
              <el-form-item label="标题"><el-input v-model="questionQuery.questionContent" clearable placeholder="搜索标题或题面" @keyup.enter="loadQuestionOptions" /></el-form-item>
              <el-form-item label="知识点"><el-input v-model="questionQuery.knowledgePoints" clearable placeholder="例如：循环" @keyup.enter="loadQuestionOptions" /></el-form-item>
              <el-form-item label="难度"><el-select v-model="questionQuery.difficulty" clearable style="width:110px"><el-option label="简单" value="SIMPLE" /><el-option label="中等" value="MEDIUM" /><el-option label="困难" value="HARD" /></el-select></el-form-item>
              <el-button type="primary" @click="loadQuestionOptions">查询</el-button>
            </el-form>
            <el-table ref="bankTable" v-loading="questionLoading" :data="questionOptions" row-key="questionId" height="calc(100vh - 330px)" @selection-change="batchSelection=$event">
              <el-table-column type="selection" width="44" :selectable="canSelectQuestion" />
              <el-table-column label="题目" min-width="240"><template #default="{ row }"><b>{{ row.programmingTitle || row.questionContent }}</b><div class="row-meta">ID {{ row.questionId }} · {{ row.knowledgePoints || '未分类' }}</div></template></el-table-column>
              <el-table-column label="难度" width="76"><template #default="{ row }">{{ difficultyLabel(row.difficulty) }}</template></el-table-column>
              <el-table-column label="验证" width="80"><template #default="{ row }"><el-tag size="small" :type="row.validationStatus==='VALID'?'success':'info'">{{ row.validationStatus==='VALID'?'已验证':'待验证' }}</el-tag></template></el-table-column>
              <el-table-column label="操作" width="88"><template #default="{ row }"><el-button link type="primary" @click="previewQuestion(row)">预览</el-button></template></el-table-column>
            </el-table>
            <pagination v-show="questionTotal>0" :total="questionTotal" v-model:page="questionQuery.pageNum" v-model:limit="questionQuery.pageSize" @pagination="loadQuestionOptions" />
          </section>
        </main>
      </div>
    </el-drawer>

    <el-dialog v-model="previewVisible" title="题目预览" width="780px" append-to-body>
      <template v-if="previewQuestionData"><h3>{{ previewConfig.title || previewQuestionData.programmingTitle }}</h3><div class="preview-meta">{{ difficultyLabel(previewQuestionData.difficulty) }} · {{ previewConfig.knowledgePoints || '未分类' }}</div><div class="statement">{{ previewQuestionData.questionContent }}</div><el-descriptions :column="1" border><el-descriptions-item label="输入说明">{{ previewConfig.inputDescription || '无输入' }}</el-descriptions-item><el-descriptions-item label="输出说明">{{ previewConfig.outputDescription || '无' }}</el-descriptions-item><el-descriptions-item label="公开样例"><pre>{{ previewCases.map(item=>`${item.inputText || '(无输入)'}\n=> ${item.expectedOutput}`).join('\n\n') || '暂无' }}</pre></el-descriptions-item></el-descriptions></template>
    </el-dialog>

    <el-dialog v-model="classPickerVisible" title="选择班级查看学情" width="620px">
      <el-radio-group v-model="analyticsClassKey" class="class-picker"><el-radio-button v-for="item in analyticsClasses" :key="classKey(item)" :label="classKey(item)">{{ classLabel(item) }}</el-radio-button></el-radio-group>
      <template #footer><el-button @click="classPickerVisible=false">取消</el-button><el-button type="primary" :disabled="!analyticsClassKey" @click="loadAnalytics">查看学情</el-button></template>
    </el-dialog>

    <el-drawer v-model="analyticsVisible" title="题单学情" size="78%">
      <div v-if="analyticsData.summary" class="summary-grid"><div><b>{{ analyticsData.summary.targetStudents }}</b><span>目标学生</span></div><div><b>{{ analyticsData.summary.startedStudents }}</b><span>已开始</span></div><div><b>{{ analyticsData.summary.completedStudents }}</b><span>全部通过</span></div><div><b>{{ analyticsData.summary.completionRate }}%</b><span>完成率</span></div><div><b>{{ analyticsData.summary.totalSubmissions }}</b><span>提交次数</span></div></div>
      <el-tabs v-model="analyticsTab">
        <el-tab-pane label="学生进度" name="students"><el-table :data="analyticsData.students || []"><el-table-column prop="student_name" label="学生" /><el-table-column prop="student_no" label="学号" /><el-table-column prop="class_label" label="班级" width="90" /><el-table-column label="完成情况"><template #default="{ row }">{{ row.passed_count }}/{{ row.question_count }}</template></el-table-column><el-table-column prop="attempted_count" label="已尝试题" /><el-table-column prop="submit_count" label="提交次数" /><el-table-column prop="last_practice_time" label="最近练习" min-width="160" /></el-table></el-tab-pane>
        <el-tab-pane label="薄弱题目" name="questions"><el-table :data="analyticsData.questions || []"><el-table-column prop="question_title" label="题目" min-width="220" /><el-table-column prop="knowledge_points" label="知识点" /><el-table-column prop="attempted_student_count" label="尝试人数" /><el-table-column prop="passed_student_count" label="通过人数" /><el-table-column label="通过率"><template #default="{ row }"><el-progress :percentage="row.passRate || 0" /></template></el-table-column><el-table-column prop="submit_count" label="提交次数" /></el-table></el-tab-pane>
      </el-tabs>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listQuestion } from '@/api/business/question'
import { previewProgrammingQuestion } from '@/api/business/programming'
import { addTeacherQuestions, createTeacherPlan, deleteTeacherPlan, getTeacherAnalytics, getTeacherManagedClasses, getTeacherPlan, getTeacherPlans, publishTeacherPlan, recommendTeacherQuestions, removeTeacherQuestion, reorderTeacherQuestions, updateTeacherPlan } from '@/api/business/pythonPractice'
import useUserStore from '@/store/modules/user'

const userStore=useUserStore(), canManage=computed(()=>userStore.roles.includes('admin')||userStore.roles.includes('teacher')), canViewAnalytics=computed(()=>canManage.value||userStore.roles.includes('researcher'))
const plans=ref([]),loading=ref(false),managedClasses=ref([]),createVisible=ref(false),designerVisible=ref(false),saving=ref(false),publishing=ref(false),recommending=ref(false)
const currentPlan=ref(null),createForm=reactive({planName:'',classKeys:[]}),editForm=reactive({planName:'',classKeys:[]})
const questionOptions=ref([]),questionTotal=ref(0),questionLoading=ref(false),batchSelection=ref([]),bankTable=ref(null)
const questionQuery=reactive({pageNum:1,pageSize:20,questionType:'practical',practicalMode:'PYTHON',validationStatus:'VALID',difficulty:'',questionContent:'',knowledgePoints:''})
const previewVisible=ref(false),previewQuestionData=ref(null),previewConfig=ref({}),previewCases=ref([])
const classPickerVisible=ref(false),analyticsVisible=ref(false),analyticsPlan=ref(null),analyticsClasses=ref([]),analyticsClassKey=ref(''),analyticsData=ref({}),analyticsTab=ref('students')
const selectedQuestions=computed(()=>currentPlan.value?.questions||[]), selectedIds=computed(()=>new Set(selectedQuestions.value.map(i=>Number(i.question_id))))

async function loadPlans(){loading.value=true;try{plans.value=(await getTeacherPlans()).data||[]}finally{loading.value=false}}
function classKey(item){return `${item.entry_year??item.entryYear}::${item.class_code??item.classCode}`}
function classObject(key){const [entryYear,classCode]=String(key).split('::');return{entryYear,classCode}}
function classLabel(item){return `${item.class_label??item.classLabel??`${item.entry_year??item.entryYear}级${item.class_code??item.classCode}班`}${item.student_count!=null?`（${item.student_count}人）`:''}`}
function selectedClassObjects(keys){return(keys||[]).map(classObject)}
function selectAllClasses(target){target.classKeys=managedClasses.value.map(classKey)}
function difficultyLabel(v){return v==='SIMPLE'?'简单':v==='HARD'?'困难':'中等'}
function openCreate(){Object.assign(createForm,{planName:'',classKeys:[]});createVisible.value=true}
async function createPlan(){if(!createForm.planName.trim()||!createForm.classKeys.length)return ElMessage.warning('请填写题单名称并选择班级');saving.value=true;try{const res=await createTeacherPlan({planName:createForm.planName.trim(),classes:selectedClassObjects(createForm.classKeys)});createVisible.value=false;await loadPlans();await openDesigner({plan_id:res.data.planId});ElMessage.success('题单已创建，请从右侧选择题目')}finally{saving.value=false}}
async function openDesigner(row){currentPlan.value=(await getTeacherPlan(row.plan_id)).data||{};Object.assign(editForm,{planName:currentPlan.value.plan_name,classKeys:(currentPlan.value.classes||[]).map(classKey)});designerVisible.value=true;await loadQuestionOptions()}
async function refreshPlan(){if(!currentPlan.value)return;currentPlan.value=(await getTeacherPlan(currentPlan.value.plan_id)).data||{};Object.assign(editForm,{planName:currentPlan.value.plan_name,classKeys:(currentPlan.value.classes||[]).map(classKey)})}
async function saveSettings(){if(!editForm.planName.trim()||!editForm.classKeys.length)return ElMessage.warning('题单名称和目标班级不能为空');currentPlan.value=(await updateTeacherPlan(currentPlan.value.plan_id,{planName:editForm.planName.trim(),classes:selectedClassObjects(editForm.classKeys)})).data;ElMessage.success('题单设置已保存为草稿');await loadPlans()}
async function removePlan(row){try{await ElMessageBox.confirm(`确定永久删除“${row.plan_name}”吗？该题单的学生练习记录也会一并删除，删除后不可恢复。`,'永久删除题单',{type:'warning',confirmButtonText:'永久删除',cancelButtonText:'取消'});await deleteTeacherPlan(row.plan_id);ElMessage.success('题单已永久删除');await loadPlans()}catch(e){if(e!=='cancel')throw e}}
async function loadQuestionOptions(){questionLoading.value=true;try{const res=await listQuestion({...questionQuery});questionOptions.value=res.rows||res.data?.rows||[];questionTotal.value=res.total||res.data?.total||questionOptions.value.length}finally{questionLoading.value=false}}
function canSelectQuestion(row){return row.validationStatus==='VALID'&&!selectedIds.value.has(Number(row.questionId))}
async function batchAdd(){const ids=batchSelection.value.filter(canSelectQuestion).map(i=>Number(i.questionId));if(!ids.length)return;await addTeacherQuestions(currentPlan.value.plan_version_id,ids);await refreshPlan();bankTable.value?.clearSelection();ElMessage.success(`已加入 ${ids.length} 道题`)}
async function recommend(){try{await ElMessageBox.confirm('将按难度和知识点加入最多 12 道 V2 系统题，之后仍可调整顺序或移除。','一键推荐',{type:'info'});recommending.value=true;const res=await recommendTeacherQuestions(currentPlan.value.plan_version_id,12);await refreshPlan();ElMessage.success(`已推荐 ${res.data?.recommendedCount||0} 道题`)}catch(e){if(e!=='cancel')throw e}finally{recommending.value=false}}
async function removeQuestion(item){await removeTeacherQuestion(currentPlan.value.plan_version_id,item.question_id);await refreshPlan();ElMessage.success('题目已移除')}
async function moveQuestion(index,delta){const target=index+delta;if(target<0||target>=selectedQuestions.value.length)return;const ids=selectedQuestions.value.map(i=>Number(i.question_id));[ids[index],ids[target]]=[ids[target],ids[index]];await reorderTeacherQuestions(currentPlan.value.plan_version_id,ids);await refreshPlan()}
async function publish(){if(!selectedQuestions.value.length||!editForm.classKeys.length)return ElMessage.warning('请先选择班级和题目');publishing.value=true;try{await saveSettings();await publishTeacherPlan(currentPlan.value.plan_id,currentPlan.value.plan_version_id);await refreshPlan();await loadPlans();ElMessage.success('题单已发布，目标班级学生现在可以看到')}finally{publishing.value=false}}
async function previewQuestion(row){const res=await previewProgrammingQuestion(Number(row.questionId));previewQuestionData.value=row;previewConfig.value=res.data||{};previewCases.value=res.testCases||[];previewVisible.value=true}
async function openAnalytics(row){const detail=(await getTeacherPlan(row.plan_id)).data||{};analyticsPlan.value={...row,...detail};analyticsClasses.value=detail.publishedClasses||[];analyticsClassKey.value='';classPickerVisible.value=true}
async function loadAnalytics(){const cls=classObject(analyticsClassKey.value);classPickerVisible.value=false;analyticsData.value=(await getTeacherAnalytics({planVersionId:analyticsPlan.value.published_version_id,entryYear:cls.entryYear,classCode:cls.classCode})).data||{};analyticsVisible.value=true}
onMounted(async()=>{await Promise.all([loadPlans(),getTeacherManagedClasses().then(r=>{managedClasses.value=r.data||[]})])})
</script>

<style scoped>
.page-heading{display:flex;align-items:flex-start;justify-content:space-between;margin-bottom:12px}.page-heading h2{margin:0 0 6px}.page-heading p{margin:0;color:#7d8590}.list-card{border-radius:8px}.field-tip{margin-top:6px;color:#909399;font-size:12px}.designer-shell{height:100vh;display:flex;flex-direction:column;background:#f5f7fa}.designer-header{height:62px;padding:0 20px;display:flex;align-items:center;justify-content:space-between;background:#fff;border-bottom:1px solid #e5e7eb}.designer-header>div{display:flex;align-items:center;gap:12px}.settings-bar{display:grid;grid-template-columns:minmax(220px,1fr) minmax(360px,2fr) auto minmax(220px,1fr);gap:12px;align-items:center;padding:14px 20px;background:#fff}.draft-tip{color:#909399;font-size:12px}.designer-main{display:grid;grid-template-columns:minmax(400px,42%) minmax(520px,58%);gap:12px;padding:12px;min-height:0;flex:1}.selected-pane,.bank-pane{display:flex;flex-direction:column;min-height:0;background:#fff;border:1px solid #e4e7ed;border-radius:8px}.pane-title{height:54px;display:flex;align-items:center;justify-content:space-between;padding:0 16px;border-bottom:1px solid #ebeef5}.question-scroll{flex:1}.selected-item{display:flex;align-items:center;gap:10px;padding:12px 14px;border-bottom:1px solid #eef0f2}.order{width:28px;height:28px;display:grid;place-items:center;border-radius:50%;background:#ecf5ff;color:#337ecc}.selected-body{min-width:0;flex:1}.selected-body b,.selected-body small{display:block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.selected-body small,.row-meta,.preview-meta{margin-top:5px;color:#909399;font-size:12px}.selected-actions{white-space:nowrap}.bank-filter{padding:12px 14px 0}.statement{white-space:pre-wrap;line-height:1.8;margin:16px 0}.class-picker{display:flex;flex-wrap:wrap;gap:10px}.summary-grid{display:grid;grid-template-columns:repeat(5,1fr);gap:12px;margin-bottom:18px}.summary-grid>div{padding:18px;border:1px solid #e4e7ed;border-radius:8px;background:#fafbfc}.summary-grid b,.summary-grid span{display:block}.summary-grid b{font-size:26px;color:#337ecc}.summary-grid span{margin-top:6px;color:#7d8590}@media(max-width:1100px){.settings-bar{grid-template-columns:1fr 1fr}.designer-main{grid-template-columns:1fr}.selected-pane{min-height:420px}.draft-tip{display:none}}@media(max-width:700px){.page-heading{flex-direction:column;gap:12px}.summary-grid{grid-template-columns:repeat(2,1fr)}}
</style>
