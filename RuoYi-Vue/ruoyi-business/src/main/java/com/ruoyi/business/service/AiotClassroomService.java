package com.ruoyi.business.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.IotExperiment;
import com.ruoyi.business.domain.IotGroup;
import com.ruoyi.business.domain.IotGroupStudent;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.BizTeacherClassMapper;
import com.ruoyi.business.mapper.IotMapper;
import com.ruoyi.business.domain.BizTeacherClass;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * AIoT 课堂上下文服务：为独立的课堂工具提供"按当前登录人计算"的身份与分组数据。
 *
 * 安全边界：
 * - 只读查询，不写任何业务表；
 * - 学生只能拿到本人的班级/小组信息，小组号一律由后台查询，绝不信前端提交；
 * - 教师只能访问本校（同 deptId）的课程与班级；
 * - 任何接口都不返回 MQTT 账号/口令/Topic，Broker 连接只发生在 AIoT 独立后端。
 */
@Service
public class AiotClassroomService
{
    /** 设备在线判定窗口（毫秒），与 IotExperimentService 学生概览保持一致 */
    private static final long ONLINE_WINDOW_MS = 120_000L;

    @Autowired private IotMapper mapper;
    @Autowired private BizLessonMapper lessonMapper;
    @Autowired private BizLessonAssignmentMapper assignmentMapper;
    @Autowired private BizStudentMapper studentMapper;
    @Autowired private BizTeacherClassMapper teacherClassMapper;

    /**
     * 学生课堂上下文：当前登录学生 + 指定课程的班级/小组/设备状态。
     * 未加入小组时返回 status=NO_GROUP，由前端提示联系老师。
     * experimentId 为空时保持旧行为（取第一个实验），供旧食堂工具兼容；
     * 新语音控灯工具必须显式传入 experimentId，避免一课多实验串课。
     */
    public Map<String, Object> studentContext(Long lessonId, Long experimentId)
    {
        if (lessonId == null)
        {
            throw new ServiceException("缺少 lessonId 参数");
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null)
        {
            throw new ServiceException("请先登录");
        }
        BizStudent student = studentMapper.selectBizStudentByUserId(loginUser.getUserId());
        if (student == null)
        {
            throw new ServiceException("未找到学生档案信息");
        }
        Long deptId = SecurityUtils.getDeptId();
        if (student.getDeptId() == null || !student.getDeptId().equals(deptId))
        {
            throw new ServiceException("学生档案学校与当前登录学校不一致");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("studentId", student.getStudentId());
        data.put("studentNo", student.getStudentNo());
        String studentName = loginUser.getUser().getNickName() != null
                ? loginUser.getUser().getNickName() : student.getStudentName();
        data.put("studentName", studentName);
        data.put("lessonId", lessonId);
        data.put("entryYear", student.getEntryYear());
        data.put("classCode", student.getClassCode());
        data.put("classId", student.getDeptId());
        data.put("schoolId", student.getDeptId());
        data.put("className", student.getEntryYear() + "级" + student.getClassCode() + "班");

        // 学生只能访问本班当前指派课程，防止猜测 lessonId 读取其他课程
        Long currentLessonId = assignmentMapper.selectCurrentLessonByClass(
                student.getEntryYear(), normalizeClass(student.getClassCode()), deptId);
        if (currentLessonId == null || !lessonId.equals(currentLessonId))
        {
            throw new ServiceException("只能访问本班当前课程的课堂");
        }
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(lessonId);
        data.put("lessonName", lesson != null ? lesson.getLessonTitle() : null);

        List<IotExperiment> experiments = mapper.selectExperimentsByLesson(lessonId, deptId);
        if (experiments == null || experiments.isEmpty())
        {
            data.put("status", "NO_EXPERIMENT");
            return data;
        }
        IotExperiment experiment = null;
        if (experimentId != null)
        {
            for (IotExperiment e : experiments)
            {
                if (experimentId.equals(e.getExperimentId()))
                {
                    experiment = e;
                    break;
                }
            }
            if (experiment == null)
            {
                throw new ServiceException("指定的物联网实验不存在或不属于本课程");
            }
        }
        else
        {
            experiment = experiments.get(0);
        }
        data.put("experimentId", experiment.getExperimentId());
        data.put("experimentTitle", experiment.getTitle());
        // 本班小组总数：供课堂工具开通对应数量的分组
        List<IotGroup> classGroups = mapper.selectGroupsByExperimentAndClass(
                experiment.getExperimentId(), student.getEntryYear(),
                normalizeClass(student.getClassCode()));
        data.put("groupCount", classGroups != null ? classGroups.size() : 0);

        // 小组归属必须由后台查询，前端提交的任何组号都不作为依据
        IotGroupStudent gs = mapper.selectGroupStudentByExpAndStudent(
                experiment.getExperimentId(), student.getStudentId());
        if (gs == null)
        {
            data.put("status", "NO_GROUP");
            return data;
        }
        IotGroup group = mapper.selectGroupById(gs.getGroupId());
        if (group == null)
        {
            data.put("status", "NO_GROUP");
            return data;
        }
        boolean belongsToClass = false;
        if (classGroups != null)
        {
            for (IotGroup candidate : classGroups)
            {
                if (candidate.getGroupId().equals(group.getGroupId())) belongsToClass = true;
            }
        }
        if (!belongsToClass) throw new ServiceException("小组不属于当前学生班级");
        data.put("status", "OK");
        data.put("groupId", group.getGroupId());
        data.put("groupNo", group.getGroupNo());
        data.put("groupName", group.getGroupName());
        data.put("deviceOnline", group.getLastSeenAt() != null
                && System.currentTimeMillis() - group.getLastSeenAt().getTime() < ONLINE_WINDOW_MS);
        data.put("lastSeenAt", group.getLastSeenAt());
        return data;
    }

    /** 旧签名兼容：旧食堂工具仍按单参调用，保持取第一个实验的行为。 */
    public Map<String, Object> studentContext(Long lessonId)
    {
        return studentContext(lessonId, null);
    }

    /**
     * 教师课堂上下文。
     * 不带 lessonId：返回教师的班级与各自当前课程列表，供前端自动选择；
     * 带 lessonId：返回课程/班级/学生数/小组明细（成员名单、设备在线）。
     * experimentId 为空时保持旧行为（取第一个实验），供旧工具兼容；
     * 新语音控灯工具必须显式传入，避免一课多实验串课。
     */
    public Map<String, Object> teacherContext(Long lessonId, String entryYear, String classCode, Long experimentId)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null)
        {
            throw new ServiceException("请先登录");
        }
        boolean isAdmin = loginUser.getUser() != null && loginUser.getUser().isAdmin();
        Long deptId = SecurityUtils.getDeptId();

        if (lessonId == null)
        {
            // 未指定课程：列出该教师的班级与各自当前指派课程，前端自动选中唯一项
            List<BizTeacherClass> classes =
                    teacherClassMapper.selectTeacherClassListWithCount(loginUser.getUserId(), deptId);
            List<Map<String, Object>> items = new ArrayList<>();
            if (classes != null)
            {
                for (BizTeacherClass tc : classes)
                {
                    Long currentLesson = assignmentMapper.selectCurrentLessonByClass(
                            tc.getEntryYear(), normalizeClass(tc.getClassCode()), deptId);
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("entryYear", tc.getEntryYear());
                    item.put("classCode", tc.getClassCode());
                    item.put("className", tc.getEntryYear() + "级" + tc.getClassCode() + "班");
                    item.put("studentCount", tc.getStudentCount());
                    if (currentLesson != null)
                    {
                        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(currentLesson);
                        item.put("lessonId", currentLesson);
                        item.put("lessonName", lesson != null ? lesson.getLessonTitle() : null);
                    }
                    items.add(item);
                }
            }
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("mode", "select");
            data.put("classes", items);
            return data;
        }

        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(lessonId);
        if (lesson == null)
        {
            throw new ServiceException("课程不存在");
        }
        // 教师只能访问本校课程；管理员放开
        if (!isAdmin && (lesson.getDeptId() == null || !lesson.getDeptId().equals(deptId)))
        {
            throw new ServiceException("无权访问该课程");
        }

        // 班级缺省时取该课程指派的第一个班级
        List<Map<String, Object>> assigned = mapper.selectAssignedClassesByLesson(lessonId, deptId);
        if (assigned == null || assigned.isEmpty())
        {
            throw new ServiceException("该课程没有已指派的班级");
        }
        String useEntryYear = entryYear;
        String useClassCode = normalizeClass(classCode);
        if (useEntryYear == null || useClassCode == null)
        {
            Map<String, Object> first = assigned.get(0);
            if (useEntryYear == null) useEntryYear = String.valueOf(first.get("entryYear"));
            if (useClassCode == null) useClassCode = normalizeClass(String.valueOf(first.get("classCode")));
        }
        // 校验该班级确实指派了本课程
        // 同校不等于授课授权，独立接口也必须验证教师与班级的关系。
        boolean teachesClass = false;
        List<BizTeacherClass> permitted = teacherClassMapper.selectTeacherClassListWithCount(loginUser.getUserId(), deptId);
        if (permitted != null)
        {
            for (BizTeacherClass tc : permitted)
            {
                if (useEntryYear.equals(tc.getEntryYear()) && useClassCode.equals(normalizeClass(tc.getClassCode())))
                    teachesClass = true;
            }
        }
        if (!teachesClass) throw new ServiceException("不在当前教师授课范围内");
        Long currentLessonId = assignmentMapper.selectCurrentLessonByClass(useEntryYear, useClassCode, deptId);
        if (currentLessonId == null || !lessonId.equals(currentLessonId))
        {
            throw new ServiceException("该班级未指派当前课程");
        }

        List<BizStudent> students = mapper.selectStudentsByClass(deptId, useEntryYear, useClassCode);
        List<IotExperiment> experiments = mapper.selectExperimentsByLesson(lessonId, deptId);
        IotExperiment experiment = null;
        if (experimentId != null)
        {
            if (experiments != null)
            {
                for (IotExperiment e : experiments)
                {
                    if (experimentId.equals(e.getExperimentId()))
                    {
                        experiment = e;
                        break;
                    }
                }
            }
            if (experiment == null)
            {
                throw new ServiceException("指定的物联网实验不存在或不属于本课程");
            }
        }
        else if (experiments != null && !experiments.isEmpty())
        {
            experiment = experiments.get(0);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("mode", "lesson");
        data.put("lessonId", lessonId);
        data.put("lessonName", lesson.getLessonTitle());
        data.put("schoolId", lesson.getDeptId());
        data.put("entryYear", useEntryYear);
        data.put("classCode", useClassCode);
        data.put("className", useEntryYear + "级" + useClassCode + "班");
        data.put("studentCount", students != null ? students.size() : 0);
        data.put("experimentId", experiment != null ? experiment.getExperimentId() : null);
        data.put("experimentTitle", experiment != null ? experiment.getTitle() : null);
        // 一课多实验时同时返回候选列表，供新工具明确选择（旧工具忽略该字段）
        if (experiments != null && experiments.size() > 1)
        {
            List<Map<String, Object>> candidates = new ArrayList<>();
            for (IotExperiment e : experiments)
            {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("experimentId", e.getExperimentId());
                item.put("title", e.getTitle());
                candidates.add(item);
            }
            data.put("experiments", candidates);
        }

        List<Map<String, Object>> groupList = new ArrayList<>();
        if (experiment != null)
        {
            List<IotGroup> groups = mapper.selectGroupsByExperimentAndClass(
                    experiment.getExperimentId(), useEntryYear, useClassCode);
            if (groups != null)
            {
                for (IotGroup group : groups)
                {
                    Map<String, Object> g = new LinkedHashMap<>();
                    g.put("groupId", group.getGroupId());
                    g.put("groupNo", group.getGroupNo());
                    g.put("groupName", group.getGroupName());
                    List<IotGroupStudent> members =
                            mapper.selectGroupStudentsByGroupId(group.getGroupId());
                    List<String> names = new ArrayList<>();
                    if (members != null)
                    {
                        for (IotGroupStudent m : members)
                        {
                            names.add(m.getStudentName());
                        }
                    }
                    g.put("memberNames", names);
                    boolean online = group.getLastSeenAt() != null
                            && System.currentTimeMillis() - group.getLastSeenAt().getTime() < ONLINE_WINDOW_MS;
                    g.put("deviceOnline", online);
                    g.put("lastSeenAt", group.getLastSeenAt());
                    groupList.add(g);
                }
            }
        }
        data.put("groupCount", groupList.size());
        data.put("groups", groupList);
        return data;
    }

    /** 旧签名兼容：旧食堂工具仍按三参调用，保持取第一个实验的行为。 */
    public Map<String, Object> teacherContext(Long lessonId, String entryYear, String classCode)
    {
        return teacherContext(lessonId, entryYear, classCode, null);
    }

    /** 班号归一化：去掉"班"字、前导零按数值比较（与 IotExperimentService 一致） */
    private String normalizeClass(String code)
    {
        if (code == null)
        {
            return null;
        }
        String text = code.trim().replace("班", "");
        if (text.matches("\\d+"))
        {
            return String.valueOf(Integer.parseInt(text));
        }
        return text;
    }
}
