import stepCourseDesignerImage from '@/assets/help/step-04-course-designer.png'
import stepStudentTaskImage from '@/assets/help/step-05-student-task.png'
import studentManagementPage from '@/assets/help/v4/student-management-page.png'
import studentImportDialog from '@/assets/help/v4/student-import-dialog.png'
import studentImportTemplate from '@/assets/help/v4/student-import-template.png'
import classManagementPage from '@/assets/help/v4/class-management-page.png'
import classManagedPanel from '@/assets/help/v4/class-managed-panel.png'
import classAvailablePanel from '@/assets/help/v4/class-available-panel.png'
import questionBankPage from '@/assets/help/v4/question-bank-page.png'
import questionAddChoice from '@/assets/help/v4/question-add-choice.png'
import questionAddJudgment from '@/assets/help/v4/question-add-judgment.png'
import questionAddTyping from '@/assets/help/v4/question-add-typing.png'
import questionAddPractical from '@/assets/help/v4/question-add-practical.png'
import questionImportDialog from '@/assets/help/v4/question-import-dialog.png'
import teacherDashboardPage from '@/assets/help/v4/teacher-dashboard-page.png'
import scoreAnalysisPage from '@/assets/help/v4/score-analysis-page.png'
import gradingPage from '@/assets/help/v4/grading-page.png'
import aiGradingSettingsDialog from '@/assets/help/v4/ai-grading-settings-dialog.png'
import aliyunBailianConsole from '@/assets/help/v4/aliyun-bailian-console.png'

export const teacherFlow = [
  { id: 'students', title: '确认学生', kicker: '共享基础数据', description: '学生管理与批量导入', path: '/studentguanli', action: '打开学生管理' },
  { id: 'classes', title: '班级管理', kicker: '建立任教关系', description: '添加或移除管理班级', path: '/teacherClass', action: '打开班级管理' },
  { id: 'questions', title: '准备题目', kicker: '建设教学资源', description: '单个新增或批量导入', path: '/question', action: '打开题库管理' },
  { id: 'courses', title: '新建课程', kicker: '组织课堂任务', description: '选题、设分并指派班级', path: '/teacher-dashboard/index', action: '打开教师首页' },
  { id: 'learning', title: '学生作答', kicker: '实施真实课堂', description: '作答、上传与提交作品', path: '/teacher-dashboard/index', action: '查看课程状态' },
  { id: 'assessment', title: '批改与学情', kicker: '形成教学证据', description: '人工/AI 批改与成绩分析', path: '/business/teacher/grading', action: '打开批改页面' }
]

export const teacherTutorials = {
  students: {
    title: '导入或确认学生',
    summary: '学生是学校共享的基础数据。先查询，确实没有时再批量导入，避免重复账号。',
    path: '/studentguanli',
    action: '打开学生管理',
    sections: [
      {
        title: '左侧点击“学生管理”',
        eyebrow: '第 1 步',
        image: studentManagementPage,
        text: '教师登录后，在左侧菜单进入学生管理。先按入学年份和班级查询，本校同一届同一班只需要导入一次。',
        tips: ['列表中已经有该班学生：直接进入班级管理，不要再次导入。', '列表中没有该班学生：点击页面上方“批量导入”。'],
        smartTip: '学生数据由同一学校的教师共享，不是每位任课教师各自保存一份。'
      },
      {
        title: '点击“批量导入”',
        eyebrow: '第 2 步',
        image: studentImportDialog,
        text: '打开学生导入窗口后，先点击“下载模板”，不要自己另建列名或更改表头。',
        tips: ['只支持 .xls、.xlsx 文件。', '每次先上传一个班或一届的小批量数据，确认无误后再继续。']
      },
      {
        title: '下载模板并按示例填写',
        eyebrow: '第 3 步',
        image: studentImportTemplate,
        text: '新版模板已经内置两行示例数据。请替换示例姓名，再填写自己的学生。',
        tips: ['学号：填写本班序号，例如 01、02，范围 01～99。', '入学年份：填写 4 位年份，例如 2025。', '班级编号：只填写 01～99，例如 01、02、11。'],
        danger: '班级编号不要写 601、602，也不要包含“六年级”等文字。三位数班号会被系统拒绝导入。'
      },
      {
        title: '上传文件并查看导入结果',
        eyebrow: '第 4 步',
        image: studentImportDialog,
        text: '选择填写完成的模板并点击“确定”。系统会逐行显示成功或失败原因。',
        tips: ['失败行不会静默跳过，请按提示修改后只重传失败数据。', '学生初始密码为平台统一默认密码，登录账号由入学年份、学校、班号和学号自动组成。']
      }
    ]
  },
  classes: {
    title: '班级管理：认领任教班级',
    summary: '班级管理只表达“我是否教这个班”，不会复制或删除学校共享的学生。',
    path: '/teacherClass',
    action: '打开班级管理',
    sections: [
      {
        title: '左侧点击“班级管理”',
        eyebrow: '第 1 步',
        image: classManagementPage,
        text: '页面左侧是“我管理的班级”，右侧是“本校可选班级”。',
        tips: ['一个班级可以同时由多位任课教师管理。', '认领后，教师首页才会显示对应届别和课程入口。']
      },
      {
        title: '右侧选择班级并“添加管理”',
        eyebrow: '第 2 步',
        image: classAvailablePanel,
        text: '在右侧勾选自己正在任教的班级，再点击下方“添加管理”。',
        tips: ['“未管理”表示你还没有认领，不代表班级没有学生。', '已经显示“已管理”的班级无需重复添加。']
      },
      {
        title: '左侧查看或“移除管理”',
        eyebrow: '第 3 步',
        image: classManagedPanel,
        text: '左侧只保留自己实际任教的班级；不再教某班时点击“移除管理”。',
        tips: ['移除管理不会删除学生。', '移除后，该班不再出现在你的课程指派范围中。']
      }
    ]
  },
  questions: {
    title: '题库管理：新增与批量导入',
    summary: '先进入题库，再根据题型和数量选择“单个新增”或“批量导入”。操作题只能单个新增。',
    path: '/question',
    action: '打开题库管理',
    branches: [
      { title: '单个新增', description: '适合精细配置', children: ['选择题', '判断题', '打字题', '操作题'] },
      { title: '批量导入', description: '适合大量客观题', children: ['选择题', '判断题', '打字题'], warning: '不支持操作题' }
    ],
    sections: [
      {
        title: '左侧点击“题库管理”',
        eyebrow: '第 1 步',
        image: questionBankPage,
        text: '先搜索已有题目，确认没有可复用内容后，再选择“新增”或“批量导入”。',
        tips: ['可以按年级、学期、课次、题型和关键词筛选。', '“是否公开”选“是”后，其他教师可以检索并复用；选“否”只保留给创建者使用。']
      },
      {
        title: '新增选择题',
        eyebrow: '单个新增 · 选择题',
        image: questionAddChoice,
        text: '填写题干、A～D 四个选项、唯一标准答案和题目解析。',
        tips: ['年级、学期决定后续在课程资源库中的筛选范围。', '“第几课”可选，用于按教材课次快速找题。', '解析会用于答题反馈，建议写清错误选项为什么不正确。']
      },
      {
        title: '新增判断题',
        eyebrow: '单个新增 · 判断题',
        image: questionAddJudgment,
        text: '填写完整判断陈述，标准答案选择“正确”或“错误”，并补充解析。',
        tips: ['题干避免使用双重否定。', '解析应说明判断依据，而不是只重复答案。']
      },
      {
        title: '新增打字题',
        eyebrow: '单个新增 · 打字题',
        image: questionAddTyping,
        text: '粘贴学生需要录入的标准文本，系统自动统计字数并推荐答题时长。',
        tips: ['小学默认按 20 字/分钟、初中及以上按 40 字/分钟估算。', '评分同时考虑正确字数和正确率。']
      },
      {
        title: '新增操作题（重点）',
        eyebrow: '单个新增 · 操作题',
        image: questionAddPractical,
        text: '操作题需要同时说明任务、学生材料、提交格式和评分依据。',
        tips: ['学生起始文件：学生下载后继续制作，可不上传。', '补充资源：学生可见，例如图片、数据或 ZIP 素材包。', '教师参考答案：仅教师和 AI 批改可见，绝不会下发给学生。', '学生提交格式：限制允许提交的文件类型；图片组还可设置张数上限。', '评分项：至少一项，比例合计必须为 100，课程会按题目分值自动折算。'],
        danger: '不要把参考答案放进“学生起始文件”或“补充资源”，否则学生可以直接下载答案。'
      },
      {
        title: '批量导入客观题',
        eyebrow: '批量导入分支',
        image: questionImportDialog,
        text: '下载题库模板后按列填写并上传，适合一次导入大量选择题、判断题和打字题。',
        tips: ['先用少量数据试导，确认答案格式和年级正确。', '导入后回到题库按题型筛选并抽查。'],
        danger: '批量导入不支持操作题。操作题包含附件、提交格式和评分项，必须使用“新增”逐题配置。'
      }
    ]
  },
  courses: {
    title: '从教师首页新建课程',
    summary: '先准备好班级和题目，再从教师首页进入课程设计器。',
    path: '/teacher-dashboard/index',
    action: '打开教师首页',
    sections: [
      {
        title: '在教师首页找到目标届别',
        eyebrow: '第 1 步',
        image: teacherDashboardPage,
        text: '教师首页按入学年份展示课程。进入实际任教的届别后点击“新建课程”。',
        tips: ['找不到目标届别或新建入口：先进入班级管理，认领任教班级。']
      },
      {
        title: '在课程设计器完成选题和指派',
        eyebrow: '第 2 步',
        image: stepCourseDesignerImage,
        text: '左侧填写课程信息，右侧从教学资源库添加题目，再设置分值和班级。',
        tips: ['按年级、学期和课次筛选题目。', '核对课程总分、题目顺序、随机规则和目标班级后保存。']
      }
    ]
  },
  learning: {
    title: '学生作答与作品提交',
    summary: '课程指派成功后，学生从首页进入当前任务，完成客观题、打字题或操作题作品。',
    path: '/teacher-dashboard/index',
    action: '查看课程状态',
    sections: [
      {
        title: '教师确认课程已经指派',
        eyebrow: '第 1 步',
        image: teacherDashboardPage,
        text: '在教师首页确认课程存在，并且已经指派给正确的届别和班级。',
        tips: ['学生看不到课程时，优先检查班级指派，不要让学生反复刷新或重登。']
      },
      {
        title: '学生进入任务并提交',
        eyebrow: '第 2 步',
        image: stepStudentTaskImage,
        text: '学生完成选择、判断、打字和操作题；操作题上传后等待“处理完成”再离开。',
        tips: ['理论题按页面机制保存，交卷前仍需复核。', '作品正在转换时不要连续重复上传。']
      }
    ]
  },
  assessment: {
    title: '批改反馈与学情分析',
    summary: '第六步分成三条路线：查看学情、人工批改、AI 辅助批改。',
    path: '/business/teacher/grading',
    action: '打开批改页面',
    branches: [
      { title: '学情分析', description: '教师首页 → 成绩查询', children: ['选择届别/班级', '选择具体课程', '查看题型与学生成绩'] },
      { title: '手动批改', description: '进入操作题批改', children: ['选择课程与班级', '按评分项给分', 'Enter 提交并下一位'] },
      { title: 'AI 批改', description: '先配置百炼 Key', children: ['实名认证/开通', '确认额度或余额', '创建 API Key', '平台加密保存并测试'] }
    ],
    sections: [
      {
        title: '成绩查询与学情分析',
        eyebrow: '路线 A',
        image: scoreAnalysisPage,
        text: '左侧点击“成绩查询”，依次选择入学年份、班级和一节具体课程，再点击查询。',
        tips: ['页面会汇总选择题、判断题、打字题、操作题和课程总分。', '单选一节课程后，可查看题目答题情况、学生矩阵和班级表现。'],
        path: '/score',
        action: '打开成绩查询'
      },
      {
        title: '手动批改操作题',
        eyebrow: '路线 B',
        image: gradingPage,
        text: '选择课程、班级和操作题，从左侧学生列表逐份查看作品，并在右侧按评分项给分。',
        tips: ['评分项输入完成后按 Enter，可提交并进入下一位学生。', 'PgUp/PgDn 可以切换上一位和下一位。', '作品预览失败时仍可下载源文件人工批改。'],
        path: '/business/teacher/grading',
        action: '打开操作题批改'
      },
      {
        title: '在平台保存百炼 API Key',
        eyebrow: '路线 C · 平台设置',
        image: aiGradingSettingsDialog,
        text: '在批改页面点击“AI 设置”，选择视觉模型并粘贴百炼 API Key，完成加密保存和连通测试。',
        tips: ['Key 只在创建时复制一次；平台后端加密保存，页面不会再次显示明文。', 'AI 只生成评分建议，教师采用并确认后才写入正式成绩。'],
        danger: '不要把 API Key 发到群聊、文档或截图中。泄露后可能产生模型调用费用。'
      },
      {
        title: '在阿里云百炼创建 API Key',
        eyebrow: '路线 C · 阿里云官方页面',
        image: aliyunBailianConsole,
        text: '使用阿里云账号开通百炼。若控制台提示未实名认证，先完成实名认证；再进入 API Key 页面创建密钥。',
        tips: ['创建前确认账号已开通百炼模型服务。', '查看免费额度、用量或账户余额，额度用完且余额不足时调用会失败。', '创建 Key 后立即复制保存；关闭创建弹窗后可能无法再次查看完整明文。'],
        links: [
          { label: '打开百炼 API Key 控制台', url: 'https://bailian.console.aliyun.com/?apiKey=1&tab=model' },
          { label: '查看阿里云官方获取教程', url: 'https://help.aliyun.com/zh/model-studio/get-api-key' }
        ]
      }
    ]
  }
}
