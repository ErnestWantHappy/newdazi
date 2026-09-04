package com.ruoyi.business.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.config.IotMqttProperties;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.IotClassConfig;
import com.ruoyi.business.domain.IotDevice;
import com.ruoyi.business.domain.IotEvent;
import com.ruoyi.business.domain.IotExperiment;
import com.ruoyi.business.domain.IotGroup;
import com.ruoyi.business.domain.IotGroupStudent;
import com.ruoyi.business.domain.IotMessage;
import com.ruoyi.business.domain.dto.IotClassGroupingRequest;
import com.ruoyi.business.domain.dto.IotDeviceRequest;
import com.ruoyi.business.domain.dto.IotExperimentRequest;
import com.ruoyi.business.domain.dto.IotGroupRequest;
import com.ruoyi.business.domain.dto.IotRotatePasscodeRequest;
import com.ruoyi.business.domain.vo.IotClassCardVo;
import com.ruoyi.business.domain.vo.IotCredentialVo;
import com.ruoyi.business.domain.vo.IotStudentOverviewVo;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.IotMapper;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 物联网实验业务服务（方案 A）：
 * 统一管理班级共享账号、6位易读口令、学号自动分组快照与 EMQX 管理同步。
 */
@Service
public class IotExperimentService
{
    public static final String BROKER_SYNC_PENDING = "PENDING";
    public static final String BROKER_SYNC_SYNCED = "SYNCED";
    public static final String BROKER_SYNC_FAILED = "FAILED";

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired private IotMapper mapper;
    @Autowired private BizLessonMapper lessonMapper;
    @Autowired private BizLessonAssignmentMapper assignmentMapper;
    @Autowired private BizStudentMapper studentMapper;
    @Autowired private IotEmqxAdapter emqxAdapter;
    @Autowired private IotSiotCredentialAdapter siotCredentialAdapter;
    @Autowired private IotMqttProperties mqttProperties;

    @Transactional
    public IotExperiment createExperiment(IotExperimentRequest request)
    {
        if (request == null || request.getLessonId() == null) throw new ServiceException("课程不能为空");
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(request.getLessonId());
        if (lesson == null || lesson.getDeptId() == null || !lesson.getDeptId().equals(SecurityUtils.getDeptId())
                || !canManageLesson(lesson))
        {
            throw new ServiceException("课程不存在或不属于当前学校");
        }
        IotExperiment result = new IotExperiment();
        result.setLessonId(lesson.getLessonId());
        result.setDeptId(lesson.getDeptId());
        result.setActivityCode(request.getActivityCode().trim());
        result.setTitle(request.getTitle().trim());
        result.setDescription(StringUtils.trim(request.getDescription()));
        result.setTopicPrefix("county/" + safe(lesson.getDeptId()) + "/" + safe(lesson.getLessonId()));
        result.setCreateBy(SecurityUtils.getUsername());
        mapper.insertExperiment(result);
        return result;
    }

    public List<IotExperiment> listExperiments(Long lessonId)
    {
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(lessonId);
        if (lesson == null || lesson.getDeptId() == null || !lesson.getDeptId().equals(SecurityUtils.getDeptId())
                || !canViewLesson(lesson))
        {
            throw new ServiceException("课程不存在或无权查看");
        }
        return mapper.selectExperimentsByLesson(lessonId, SecurityUtils.getDeptId());
    }

    /**
     * 查询当前课程已指派的班级列表
     */
    public List<Map<String, Object>> listLessonClasses(Long lessonId)
    {
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(lessonId);
        if (lesson == null || lesson.getDeptId() == null || !lesson.getDeptId().equals(SecurityUtils.getDeptId())
                || !canViewLesson(lesson))
        {
            throw new ServiceException("课程不存在或无权查看");
        }
        return mapper.selectAssignedClassesByLesson(lessonId, SecurityUtils.getDeptId());
    }

    /**
     * 查询实验在指定班级的配置与口令快照
     */
    public IotClassConfig getClassConfig(Long experimentId, String entryYear, String classCode)
    {
        IotExperiment experiment = requireExperiment(experimentId);
        String ey = entryYear != null ? entryYear.trim() : "";
        String cc = classCode != null ? classCode.replace("班", "").trim() : "";
        if (!canViewClassConfig(experiment, ey, cc)) throw new ServiceException("无权查看该班级物联配置");
        IotClassConfig config = mapper.selectClassConfig(experimentId, ey, cc);
        if (config != null && BROKER_SYNC_SYNCED.equals(config.getBrokerSyncStatus())
                && config.getPasscodeCiphertext() != null)
        {
            config.setPasscode(IotPasscodeUtil.decrypt(config.getPasscodeCiphertext(), mqttProperties.getPasscodeSecret()));
        }
        return config;
    }

    /**
     * 按照学号自动生成班级分组快照
     */
    @Transactional
    public Map<String, Object> generateClassGrouping(IotClassGroupingRequest request)
    {
        if (request == null || request.getExperimentId() == null) throw new ServiceException("实验不能为空");
        requirePasscodeSecret();
        IotExperiment experiment = requireExperiment(request.getExperimentId());
        if (!canManageExperiment(experiment)) throw new ServiceException("无权管理该实验分组");

        String entryYear = request.getEntryYear().trim();
        String classCode = request.getClassCode().replace("班", "").trim();
        int groupSize = request.getGroupSize() != null && request.getGroupSize() > 0 ? request.getGroupSize() : 4;

        // 检查是否已有快照
        IotClassConfig existingConfig = mapper.selectClassConfig(experiment.getExperimentId(), entryYear, classCode);
        if (existingConfig != null && existingConfig.getGroupedAt() != null && Boolean.FALSE.equals(request.getForce()))
        {
            throw new ServiceException("当前班级已生成分组快照，如需重新分组请在提示框中确认");
        }

        // 读取当前班级学生名单
        List<BizStudent> students = mapper.selectStudentsByClass(experiment.getDeptId(), entryYear, classCode);
        if (students == null || students.isEmpty())
        {
            throw new ServiceException("当前班级暂无学生数据");
        }

        // 按学号自然排序（数字升序，无法解析按字符串升序）
        students.sort((a, b) -> IotPasscodeUtil.compareStudentNo(a.getStudentNo(), b.getStudentNo()));

        // 生成或复用 6 位课堂口令
        String passcode;
        if (existingConfig != null && existingConfig.getPasscodeCiphertext() != null)
        {
            passcode = IotPasscodeUtil.decrypt(existingConfig.getPasscodeCiphertext(), mqttProperties.getPasscodeSecret());
            if (passcode == null || passcode.trim().isEmpty())
            {
                passcode = IotPasscodeUtil.generatePasscode();
            }
        }
        else
        {
            passcode = IotPasscodeUtil.generatePasscode();
        }

        String classSegment = (classCode.matches("\\d") ? "0" + classCode : classCode);
        String mqttUsername = "class_" + experiment.getDeptId() + "_" + entryYear + "_" + classSegment;
        String passcodeCiphertext = IotPasscodeUtil.encrypt(passcode, mqttProperties.getPasscodeSecret());
        String passcodeHash = IotPasscodeUtil.hashPasscode(passcode);

        // 清理旧分组快照
        mapper.deleteGroupStudentsByExperimentAndClass(experiment.getExperimentId(), entryYear, classCode);
        mapper.deleteGroupsByExperimentAndClass(experiment.getExperimentId(), entryYear, classCode);

        // 连续分组并持久化快照
        int totalStudents = students.size();
        int totalGroups = (int) Math.ceil((double) totalStudents / groupSize);
        List<IotGroup> createdGroups = new ArrayList<>();

        for (int g = 1; g <= totalGroups; g++)
        {
            int groupNo = g;
            String groupCode = String.format("group%02d", groupNo);
            String groupName = "第" + groupNo + "组";
            String groupTopic = buildGroupTopic(experiment, entryYear, classCode, groupNo);

            IotGroup group = new IotGroup();
            group.setExperimentId(experiment.getExperimentId());
            group.setEntryYear(entryYear);
            group.setClassCode(classCode);
            group.setGroupCode(groupCode);
            group.setGroupNo(groupNo);
            group.setGroupName(groupName);
            group.setTopic(groupTopic);
            group.setCreateBy(SecurityUtils.getUsername());
            mapper.insertGroup(group);

            int startIdx = (g - 1) * groupSize;
            int endIdx = Math.min(startIdx + groupSize, totalStudents);
            List<IotGroupStudent> memberList = new ArrayList<>();

            for (int i = startIdx; i < endIdx; i++)
            {
                BizStudent student = students.get(i);
                IotGroupStudent gs = new IotGroupStudent();
                gs.setGroupId(group.getGroupId());
                gs.setExperimentId(experiment.getExperimentId());
                gs.setStudentId(student.getStudentId());
                gs.setStudentNo(student.getStudentNo());
                gs.setStudentName(student.getStudentName());
                gs.setSortOrder((i - startIdx) + 1);
                mapper.insertGroupStudent(gs);
                memberList.add(gs);
            }
            group.setStudentList(memberList);
            group.setStudentCount(memberList.size());
            createdGroups.add(group);
        }

        // 保存或更新班级配置
        if (existingConfig == null)
        {
            IotClassConfig config = new IotClassConfig();
            config.setExperimentId(experiment.getExperimentId());
            config.setDeptId(experiment.getDeptId());
            config.setEntryYear(entryYear);
            config.setClassCode(classCode);
            config.setGroupSize(groupSize);
            config.setMqttUsername(mqttUsername);
            config.setPasscodeCiphertext(passcodeCiphertext);
            config.setPasscodeHash(passcodeHash);
            config.setPasscodeVersion(1);
            config.setGroupVersion(1);
            config.setPasscodeUpdatedAt(new Date());
            config.setGroupedAt(new Date());
            config.setBrokerSyncStatus(BROKER_SYNC_PENDING);
            config.setStatus("0");
            config.setCreateBy(SecurityUtils.getUsername());
            mapper.insertClassConfig(config);
            config.setPasscode(passcode);
            config.setStudentCount(totalStudents);
            config.setGroupCount(totalGroups);
            existingConfig = config;
        }
        else
        {
            existingConfig.setGroupSize(groupSize);
            existingConfig.setMqttUsername(mqttUsername);
            existingConfig.setPasscodeCiphertext(passcodeCiphertext);
            existingConfig.setPasscodeHash(passcodeHash);
            existingConfig.setGroupVersion(existingConfig.getGroupVersion() != null ? existingConfig.getGroupVersion() + 1 : 1);
            existingConfig.setGroupedAt(new Date());
            existingConfig.setBrokerSyncStatus(BROKER_SYNC_PENDING);
            existingConfig.setBrokerSyncedAt(null);
            existingConfig.setBrokerSyncError(null);
            mapper.updateClassConfig(existingConfig);
            existingConfig.setPasscode(passcode);
            existingConfig.setStudentCount(totalStudents);
            existingConfig.setGroupCount(totalGroups);
        }

        // 只有账号与精确 Topic ACL 均成功后，前端才允许分发可运行配置。
        syncClassBroker(existingConfig, experiment, passcode, false);
        existingConfig.setPasscode(BROKER_SYNC_SYNCED.equals(existingConfig.getBrokerSyncStatus()) ? passcode : null);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("classConfig", existingConfig);
        result.put("groups", createdGroups);
        result.put("totalStudents", totalStudents);
        result.put("totalGroups", totalGroups);
        return result;
    }

    /**
     * 教师主动轮换 6 位课堂口令
     */
    @Transactional
    public IotClassConfig rotateClassPasscode(IotRotatePasscodeRequest request)
    {
        if (request == null || request.getExperimentId() == null) throw new ServiceException("实验不能为空");
        requirePasscodeSecret();
        IotExperiment experiment = requireExperiment(request.getExperimentId());
        if (!canManageExperiment(experiment)) throw new ServiceException("无权轮换该班级口令");

        String entryYear = request.getEntryYear().trim();
        String classCode = request.getClassCode().replace("班", "").trim();

        IotClassConfig config = mapper.selectClassConfig(experiment.getExperimentId(), entryYear, classCode);
        if (config == null)
        {
            throw new ServiceException("该班级尚未配置实验，请先生成分组");
        }

        String newPasscode = IotPasscodeUtil.generatePasscode();
        String ciphertext = IotPasscodeUtil.encrypt(newPasscode, mqttProperties.getPasscodeSecret());
        String hash = IotPasscodeUtil.hashPasscode(newPasscode);

        config.setPasscodeCiphertext(ciphertext);
        config.setPasscodeHash(hash);
        config.setPasscodeVersion(config.getPasscodeVersion() != null ? config.getPasscodeVersion() + 1 : 1);
        config.setPasscodeUpdatedAt(new Date());
        config.setBrokerSyncStatus(BROKER_SYNC_PENDING);
        config.setBrokerSyncedAt(null);
        config.setBrokerSyncError(null);
        mapper.updateClassConfig(config);

        // 同步新密码、精确 ACL，并踢掉仍持有旧口令的在线连接。
        syncClassBroker(config, experiment, newPasscode, true);

        config.setPasscode(BROKER_SYNC_SYNCED.equals(config.getBrokerSyncStatus()) ? newPasscode : null);
        return config;
    }

    /**
     * 生成适合课堂投屏/打印的班级物联配置卡（绝不返回任何管理凭据）
     */
    public IotClassCardVo getClassCard(Long experimentId, String entryYear, String classCode)
    {
        requirePasscodeSecret();
        IotExperiment experiment = requireExperiment(experimentId);
        String ey = entryYear != null ? entryYear.trim() : "";
        String cc = classCode != null ? classCode.replace("班", "").trim() : "";
        if (!canViewClassConfig(experiment, ey, cc)) throw new ServiceException("无权查看该班级配置卡");

        IotClassConfig config = mapper.selectClassConfig(experimentId, ey, cc);
        if (config == null || config.getGroupedAt() == null)
        {
            throw new ServiceException("该班级尚未生成分组快照");
        }
        if (!BROKER_SYNC_SYNCED.equals(config.getBrokerSyncStatus()))
        {
            throw new ServiceException("班级 MQTT 权限尚未同步成功，请先在班级配置中点击“重试同步”");
        }

        String passcode = IotPasscodeUtil.decrypt(config.getPasscodeCiphertext(), mqttProperties.getPasscodeSecret());
        List<IotGroup> groups = mapper.selectGroupsByExperimentAndClass(experimentId, ey, cc);
        List<IotGroupStudent> groupStudents = mapper.selectGroupStudentsByExperimentAndClass(experimentId, ey, cc);

        Map<Long, List<String>> memberMap = groupStudents.stream().collect(
                Collectors.groupingBy(IotGroupStudent::getGroupId,
                        Collectors.mapping(gs -> gs.getStudentNo() + " " + gs.getStudentName(), Collectors.toList()))
        );

        IotClassCardVo vo = new IotClassCardVo();
        vo.setConfigId(config.getConfigId());
        vo.setBrokerUrl(parseBrokerHost(mqttProperties.getBrokerUrl()));
        vo.setBrokerPort(parseBrokerPort(mqttProperties.getBrokerUrl()));
        vo.setMqttUsername(config.getMqttUsername());
        vo.setPasscode(passcode);
        vo.setExperimentTitle(experiment.getTitle());
        vo.setEntryYear(ey);
        vo.setClassCode(cc);
        vo.setGroupSize(config.getGroupSize());
        vo.setStudentCount(groupStudents.size());
        vo.setGroupCount(groups.size());
        vo.setBrokerSyncStatus(config.getBrokerSyncStatus());
        vo.setBrokerSyncedAt(config.getBrokerSyncedAt());
        vo.setBrokerSyncError(config.getBrokerSyncError());

        List<IotClassCardVo.GroupItem> groupItems = new ArrayList<>();
        for (IotGroup g : groups)
        {
            IotClassCardVo.GroupItem item = new IotClassCardVo.GroupItem();
            item.setGroupId(g.getGroupId());
            item.setGroupNo(g.getGroupNo());
            item.setGroupCode(g.getGroupCode());
            item.setGroupName(g.getGroupName());
            item.setTopic(g.getTopic());
            item.setPythonClientId(buildPrimaryClientId(g.getGroupId()));
            item.setMemberNames(memberMap.getOrDefault(g.getGroupId(), Collections.emptyList()));
            groupItems.add(item);
        }
        vo.setGroups(groupItems);
        return vo;
    }

    /**
     * 学生端获取当前课程物联网实验配置与专属小组信息
     */
    public IotStudentOverviewVo getStudentOverview(Long lessonId)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) throw new ServiceException("请先登录");
        BizStudent student = studentMapper.selectBizStudentByUserId(loginUser.getUserId());
        if (student == null) throw new ServiceException("未找到学生档案信息");
        Long currentDeptId = SecurityUtils.getDeptId();
        if (student.getDeptId() == null || !student.getDeptId().equals(currentDeptId))
            throw new ServiceException("学生档案学校与当前登录学校不一致");

        IotStudentOverviewVo vo = new IotStudentOverviewVo();
        vo.setEntryYear(student.getEntryYear());
        vo.setClassCode(student.getClassCode());
        vo.setStudentNo(student.getStudentNo());
        vo.setStudentName(loginUser.getUser().getNickName() != null ? loginUser.getUser().getNickName() : student.getStudentName());
        vo.setIsolationNotice("本班同学使用相同账号和课堂口令，不同小组使用不同 Topic。请不要修改系统生成的 Topic。（注：此为课堂业务隔离，非组间强安全隔离）");

        if (lessonId == null)
        {
            vo.setHasExperiment(false);
            return vo;
        }

        // 学生只能查看本班当前指派课程，不能靠猜测 lessonId 读取同校其他课程的实验元数据。
        Long currentLessonId = assignmentMapper.selectCurrentLessonByClass(
                student.getEntryYear(), normalizeClass(student.getClassCode()), currentDeptId);
        if (currentLessonId == null || !lessonId.equals(currentLessonId))
            throw new ServiceException("只能查看当前课程的物联网实验");

        // 课程级物联网开关：教师在课程设计中未开启时，即使历史存在实验也不展示。
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(lessonId);
        boolean lessonIotEnabled = lesson != null && Boolean.TRUE.equals(lesson.getIotEnabled());
        vo.setIotEnabled(lessonIotEnabled);
        if (!lessonIotEnabled)
        {
            vo.setHasExperiment(false);
            return vo;
        }

        List<IotExperiment> experiments = mapper.selectExperimentsByLesson(lessonId, currentDeptId);
        if (experiments == null || experiments.isEmpty())
        {
            vo.setHasExperiment(false);
            return vo;
        }

        IotExperiment experiment = experiments.get(0);
        vo.setHasExperiment(true);
        vo.setExperimentId(experiment.getExperimentId());
        vo.setExperimentTitle(experiment.getTitle());
        vo.setActivityCode(experiment.getActivityCode());
        vo.setDescription(experiment.getDescription());

        // 查询班级配置
        IotClassConfig classConfig = mapper.selectClassConfig(experiment.getExperimentId(), student.getEntryYear(), student.getClassCode());
        if (classConfig != null)
        {
            vo.setBrokerSyncStatus(classConfig.getBrokerSyncStatus());
            vo.setBrokerSyncError(classConfig.getBrokerSyncError());
            if (BROKER_SYNC_SYNCED.equals(classConfig.getBrokerSyncStatus())
                    && classConfig.getPasscodeCiphertext() != null)
            {
                vo.setBrokerUrl(parseBrokerHost(mqttProperties.getBrokerUrl()));
                vo.setBrokerPort(parseBrokerPort(mqttProperties.getBrokerUrl()));
                vo.setMqttUsername(classConfig.getMqttUsername());
                vo.setPasscode(IotPasscodeUtil.decrypt(classConfig.getPasscodeCiphertext(), mqttProperties.getPasscodeSecret()));
            }
        }

        // 查询学生所属小组快照
        IotGroupStudent gs = mapper.selectGroupStudentByExpAndStudent(experiment.getExperimentId(), student.getStudentId());
        if (gs != null)
        {
            IotGroup group = mapper.selectGroupById(gs.getGroupId());
            if (group != null)
            {
                vo.setGroupId(group.getGroupId());
                vo.setGroupNo(group.getGroupNo());
                vo.setGroupName(group.getGroupName());
                vo.setGroupCode(group.getGroupCode());
                vo.setTopic(group.getTopic());
                vo.setPythonClientId(buildPrimaryClientId(group.getGroupId()));
                vo.setLastSeenAt(group.getLastSeenAt());
                vo.setIsOnline(group.getLastSeenAt() != null && System.currentTimeMillis() - group.getLastSeenAt().getTime() < 120000);

                // 查询同组成员
                List<IotGroupStudent> members = mapper.selectGroupStudentsByGroupId(group.getGroupId());
                List<IotStudentOverviewVo.StudentMemberVo> memberVos = members.stream()
                        .map(m -> new IotStudentOverviewVo.StudentMemberVo(m.getStudentNo(), m.getStudentName(), m.getStudentId().equals(student.getStudentId())))
                        .collect(Collectors.toList());
                vo.setGroupMembers(memberVos);

                // 查询最近一条消息
                List<IotMessage> recent = mapper.selectRecentMessagesByGroup(group.getGroupId(), 1);
                if (recent != null && !recent.isEmpty())
                {
                    vo.setLatestPayloadText(recent.get(0).getPayloadText());
                    vo.setLatestPayloadType(recent.get(0).getPayloadType());
                    vo.setLatestReceivedAt(recent.get(0).getReceivedAt());
                }
            }
        }

        return vo;
    }

    /**
     * 教师手动重试班级账号与精确 Topic 权限同步。
     */
    @Transactional
    public IotClassConfig retryClassBrokerSync(Long configId)
    {
        requirePasscodeSecret();
        IotClassConfig config = mapper.selectClassConfigById(configId);
        if (config == null) throw new ServiceException("班级物联配置不存在");
        IotExperiment experiment = requireExperiment(config.getExperimentId());
        if (!canViewClassConfig(experiment, config.getEntryYear(), config.getClassCode()))
            throw new ServiceException("无权同步该班级物联配置");
        String passcode = IotPasscodeUtil.decrypt(config.getPasscodeCiphertext(), mqttProperties.getPasscodeSecret());
        syncClassBroker(config, experiment, passcode, true);
        config.setPasscode(BROKER_SYNC_SYNCED.equals(config.getBrokerSyncStatus()) ? passcode : null);
        return config;
    }

    /**
     * 学生端历史数据：分页查询本人所在小组收到的上报消息（按接收时间倒序）。
     * 校验链与学生概览一致：仅当前指派课程、物联已开启、且只能看自己小组的数据。
     */
    public Map<String, Object> listStudentMessages(Long lessonId, Integer pageNum, Integer pageSize)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) throw new ServiceException("请先登录");
        BizStudent student = studentMapper.selectBizStudentByUserId(loginUser.getUserId());
        if (student == null) throw new ServiceException("未找到学生档案信息");
        Long currentDeptId = SecurityUtils.getDeptId();
        if (student.getDeptId() == null || !student.getDeptId().equals(currentDeptId))
            throw new ServiceException("学生档案学校与当前登录学校不一致");
        if (lessonId == null) throw new ServiceException("课程不能为空");

        // 学生只能查询本班当前指派课程，不能靠猜测 lessonId 读取其他课程数据
        Long currentLessonId = assignmentMapper.selectCurrentLessonByClass(
                student.getEntryYear(), normalizeClass(student.getClassCode()), currentDeptId);
        if (currentLessonId == null || !lessonId.equals(currentLessonId))
            throw new ServiceException("只能查看当前课程的物联网实验");

        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(lessonId);
        if (lesson == null || !Boolean.TRUE.equals(lesson.getIotEnabled()))
            throw new ServiceException("当前课程尚未开启物联网实验");

        List<IotExperiment> experiments = mapper.selectExperimentsByLesson(lessonId, currentDeptId);
        if (experiments == null || experiments.isEmpty())
            throw new ServiceException("当前课程暂未配置物联网实验");
        IotExperiment experiment = experiments.get(0);

        IotGroupStudent gs = mapper.selectGroupStudentByExpAndStudent(experiment.getExperimentId(), student.getStudentId());
        if (gs == null) throw new ServiceException("你尚未被分配到物联网小组，请联系老师生成分组");
        IotGroup group = mapper.selectGroupById(gs.getGroupId());
        if (group == null) throw new ServiceException("小组信息不存在，请联系老师重新生成分组");

        int page = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        int offset = (page - 1) * size;

        List<IotMessage> rows = mapper.selectMessagePage(experiment.getExperimentId(), null, null,
                group.getGroupId(), null, null, offset, size);
        int total = mapper.countMessagePage(experiment.getExperimentId(), null, null,
                group.getGroupId(), null, null);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("groupId", group.getGroupId());
        result.put("groupName", group.getGroupName());
        result.put("rows", rows == null ? Collections.emptyList() : rows);
        result.put("total", total);
        return result;
    }

    public List<IotGroup> listGroups(Long experimentId)
    {
        IotExperiment experiment = requireExperiment(experimentId);
        if (!canViewExperiment(experiment)) throw new ServiceException("无权查看该实验数据");
        List<IotGroup> groups = canManageExperiment(experiment) ? mapper.selectGroupsByExperiment(experimentId)
                : mapper.selectGroupsByExperimentForTeacher(experimentId, SecurityUtils.getUserId(), SecurityUtils.getDeptId());

        // 填充组内学生成员
        List<IotGroupStudent> allStudents = mapper.selectGroupStudentsByExperimentAndClass(experimentId, null, null);
        Map<Long, List<IotGroupStudent>> groupStudentMap = allStudents.stream().collect(Collectors.groupingBy(IotGroupStudent::getGroupId));
        for (IotGroup g : groups)
        {
            List<IotGroupStudent> list = groupStudentMap.getOrDefault(g.getGroupId(), Collections.emptyList());
            g.setStudentList(list);
            g.setStudentCount(list.size());
        }
        return groups;
    }

    public List<IotGroup> listGroups(Long experimentId, String entryYear, String classCode)
    {
        IotExperiment experiment = requireExperiment(experimentId);
        String ey = entryYear != null ? entryYear.trim() : "";
        String cc = classCode != null ? classCode.replace("班", "").trim() : "";
        if (!canViewClassConfig(experiment, ey, cc)) throw new ServiceException("无权查看该班级物联数据");
        List<IotGroup> groups = mapper.selectGroupsByExperimentAndClass(experimentId, ey, cc);

        List<IotGroupStudent> allStudents = mapper.selectGroupStudentsByExperimentAndClass(experimentId, ey, cc);
        Map<Long, List<IotGroupStudent>> groupStudentMap = allStudents.stream().collect(Collectors.groupingBy(IotGroupStudent::getGroupId));
        for (IotGroup g : groups)
        {
            List<IotGroupStudent> list = groupStudentMap.getOrDefault(g.getGroupId(), Collections.emptyList());
            g.setStudentList(list);
            g.setStudentCount(list.size());
        }
        return groups;
    }

    public List<IotDevice> listDevices(Long groupId)
    {
        IotGroup group = mapper.selectGroupById(groupId);
        if (group == null) throw new ServiceException("小组不存在");
        IotExperiment experiment = requireExperiment(group.getExperimentId());
        if (!canViewExperiment(experiment)) throw new ServiceException("无权查看该小组设备");
        return mapper.selectDevicesByGroup(groupId);
    }

    @Transactional
    public IotGroup createGroup(IotGroupRequest request)
    {
        IotExperiment experiment = requireExperiment(request == null ? null : request.getExperimentId());
        if (!canManageExperiment(experiment)) throw new ServiceException("无权添加小组");
        IotGroup result = new IotGroup();
        result.setExperimentId(experiment.getExperimentId());
        result.setEntryYear(request.getEntryYear().trim());
        result.setClassCode(request.getClassCode().replace("班", "").trim());
        result.setGroupCode(request.getGroupCode().trim());
        result.setGroupName(request.getGroupName().trim());
        result.setTopic(buildGroupTopic(experiment, result.getEntryYear(), result.getClassCode(), 1));
        result.setCreateBy(SecurityUtils.getUsername());
        mapper.insertGroup(result);
        return result;
    }

    @Transactional
    public Map<String, Object> createDevice(IotDeviceRequest request)
    {
        if (request == null || request.getGroupId() == null) throw new ServiceException("小组不能为空");
        IotGroup group = mapper.selectGroupById(request.getGroupId());
        if (group == null || !canManageExperiment(requireExperiment(group.getExperimentId()))) throw new ServiceException("小组不存在或无权操作");
        IotExperiment experiment = requireExperiment(group.getExperimentId());
        IotDevice device = new IotDevice();
        device.setGroupId(group.getGroupId());
        device.setDeviceCode(request.getDeviceCode().trim());
        device.setDeviceName(request.getDeviceName().trim());
        device.setCreateBy(SecurityUtils.getUsername());
        mapper.insertDevice(device);
        return rotateCredential(device, experiment);
    }

    @Transactional
    public Map<String, Object> rotateCredential(Long deviceId)
    {
        IotDevice device = mapper.selectDeviceById(deviceId);
        if (device == null || !canManageExperiment(requireExperiment(device.getExperimentId()))) throw new ServiceException("设备不存在或无权操作");
        return rotateCredential(device, requireExperiment(device.getExperimentId()));
    }

    public Map<String, Object> dashboard(Long experimentId, int limit)
    {
        return dashboard(experimentId, null, null, limit);
    }

    public Map<String, Object> dashboard(Long experimentId, String entryYear, String classCode, int limit)
    {
        IotExperiment experiment = requireExperiment(experimentId);
        if (!canViewExperiment(experiment)) throw new ServiceException("无权查看该实验数据");
        int safeLimit = Math.max(1, Math.min(limit <= 0 ? 50 : limit, 200));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("experiment", mapper.selectExperimentById(experimentId));

        boolean manager = canManageExperiment(experiment);
        List<IotGroup> groups;
        if (StringUtils.isNotEmpty(entryYear) && StringUtils.isNotEmpty(classCode))
        {
            groups = listGroups(experimentId, entryYear, classCode);
            IotClassConfig config = getClassConfig(experimentId, entryYear, classCode);
            result.put("classConfig", config);
        }
        else
        {
            groups = listGroups(experimentId);
        }

        result.put("groups", groups);
        result.put("messages", manager ? mapper.selectRecentMessages(experimentId, safeLimit)
                : mapper.selectRecentMessagesForTeacher(experimentId, SecurityUtils.getUserId(), SecurityUtils.getDeptId(), safeLimit));
        result.put("events", manager ? mapper.selectRecentEvents(experimentId, safeLimit)
                : mapper.selectRecentEventsForTeacher(experimentId, SecurityUtils.getUserId(), SecurityUtils.getDeptId(), safeLimit));
        result.put("brokerUrl", parseBrokerHost(mqttProperties.getBrokerUrl()));
        result.put("brokerPort", parseBrokerPort(mqttProperties.getBrokerUrl()));
        result.put("emqxApiConfigured", emqxAdapter.isApiConfigured());
        result.put("diagnosticStages", new String[]{"网络未到达", "MQTT认证", "Topic", "消息格式", "平台接收"});
        return result;
    }

    /**
     * 教师数据收集：分页查询实验收到的学生/设备上报消息，并返回班级内小组消息汇总。
     * 数据来自设备经 MQTT 上报后入库（IOT_MQTT_ENABLED 开启时才有真实数据）。
     */
    public Map<String, Object> listMessages(Long experimentId, String entryYear, String classCode,
                                            Long groupId, String payloadType, String keyword,
                                            Integer pageNum, Integer pageSize)
    {
        IotExperiment experiment = requireExperiment(experimentId);
        if (!canViewExperiment(experiment)) throw new ServiceException("无权查看该实验数据");
        boolean manager = canManageExperiment(experiment);
        String ey = StringUtils.trimToNull(entryYear);
        String cc = classCode == null ? null : classCode.replace("班", "").trim();
        if (StringUtils.isEmpty(cc)) cc = null;
        String type = StringUtils.trimToNull(payloadType);
        String word = StringUtils.trimToNull(keyword);

        int page = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        int offset = (page - 1) * size;

        List<IotMessage> rows;
        int total;
        if (manager)
        {
            rows = mapper.selectMessagePage(experimentId, ey, cc, groupId, type, word, offset, size);
            total = mapper.countMessagePage(experimentId, ey, cc, groupId, type, word);
        }
        else
        {
            rows = mapper.selectMessagePageForTeacher(experimentId, ey, cc, groupId, type, word,
                    SecurityUtils.getUserId(), SecurityUtils.getDeptId(), offset, size);
            total = mapper.countMessagePageForTeacher(experimentId, ey, cc, groupId, type, word,
                    SecurityUtils.getUserId(), SecurityUtils.getDeptId());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rows", rows == null ? Collections.emptyList() : rows);
        result.put("total", total);
        if (ey != null && cc != null)
        {
            List<Map<String, Object>> stats = manager
                    ? mapper.selectMessageGroupStats(experimentId, ey, cc)
                    : mapper.selectMessageGroupStatsForTeacher(experimentId, ey, cc,
                            SecurityUtils.getUserId(), SecurityUtils.getDeptId());
            result.put("stats", stats == null ? Collections.emptyList() : stats);
        }
        return result;
    }

    private Map<String, Object> rotateCredential(IotDevice device, IotExperiment experiment)
    {
        boolean sharedCredential = mqttProperties.useSharedDeviceCredential();
        String secret = sharedCredential ? mqttProperties.getDevicePassword() : IotPasscodeUtil.generatePasscode();
        Date expires = sharedCredential ? null : new Date(System.currentTimeMillis()
                + Math.max(15, mqttProperties.getCredentialTtlMinutes()) * 60 * 1000L);
        String username = sharedCredential ? mqttProperties.getDeviceUsername()
                : "iot_" + experiment.getDeptId() + "_" + device.getDeviceId();
        String topic = topicOf(experiment, device);
        device.setBrokerUsername(username);
        device.setCredentialHash(sharedCredential ? null : encoder.encode(secret));
        device.setCredentialExpiresAt(expires);
        mapper.updateDeviceCredential(device);
        siotCredentialAdapter.provision(username, secret, topic);

        IotCredentialVo credential = new IotCredentialVo();
        credential.setDeviceId(device.getDeviceId());
        credential.setUsername(username);
        if (!sharedCredential) credential.setSecret(secret);
        credential.setExpiresAt(expires);
        credential.setBrokerMode("标准 EMQX 统一接入，Topic 已由平台业务隔离");
        credential.setBrokerUrl(parseBrokerHost(mqttProperties.getBrokerUrl()));
        credential.setTopic(topic);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("device", device);
        result.put("credential", credential);
        return result;
    }

    /**
     * 将数据库中的课堂口令同步为 EMQX 账号，并绑定到本班 Topic 前缀。
     * 任一步失败都保留 FAILED 状态，前端据此阻止分发半成功的连接参数。
     */
    private void syncClassBroker(IotClassConfig config, IotExperiment experiment, String passcode, boolean disconnectOldClients)
    {
        config.setBrokerSyncStatus(BROKER_SYNC_PENDING);
        config.setBrokerSyncedAt(null);
        config.setBrokerSyncError(null);
        mapper.updateClassConfig(config);

        if (StringUtils.isEmpty(passcode))
        {
            markBrokerSyncFailed(config, "课堂口令解密失败，请检查服务器加密密钥");
            return;
        }
        if (!emqxAdapter.isBuiltInAuthorizationReady())
        {
            markBrokerSyncFailed(config, "EMQX 精确授权源未就绪");
            return;
        }
        if (!emqxAdapter.syncClassAccount(config.getMqttUsername(), passcode))
        {
            markBrokerSyncFailed(config, "EMQX 班级账号同步失败");
            return;
        }
        if (!emqxAdapter.syncClassAcl(config.getMqttUsername(),
                buildClassTopicPrefix(experiment, config.getEntryYear(), config.getClassCode())))
        {
            markBrokerSyncFailed(config, "EMQX 班级 Topic 权限同步失败");
            return;
        }
        if (disconnectOldClients && !emqxAdapter.disconnectClientsByUsername(config.getMqttUsername()))
        {
            markBrokerSyncFailed(config, "新权限已写入，但旧 MQTT 连接清理失败，请重试");
            return;
        }

        config.setBrokerSyncStatus(BROKER_SYNC_SYNCED);
        config.setBrokerSyncedAt(new Date());
        config.setBrokerSyncError(null);
        mapper.updateClassConfig(config);
    }

    private void markBrokerSyncFailed(IotClassConfig config, String message)
    {
        config.setBrokerSyncStatus(BROKER_SYNC_FAILED);
        config.setBrokerSyncedAt(null);
        config.setBrokerSyncError(message);
        mapper.updateClassConfig(config);
    }

    private String buildPrimaryClientId(Long groupId)
    {
        return "primary_g" + (groupId == null ? "0" : groupId);
    }

    public String buildClassTopicPrefix(IotExperiment experiment, String entryYear, String classCode)
    {
        String classSegment = (classCode.matches("\\d") ? "0" + classCode : classCode);
        String classId = entryYear + "-" + classSegment;
        return "county/" + safe(experiment.getDeptId()) + "/" + safe(experiment.getLessonId()) + "/" + safe(classId) + "/#";
    }

    public String buildGroupTopic(IotExperiment experiment, String entryYear, String classCode, int groupNo)
    {
        String classSegment = (classCode.matches("\\d") ? "0" + classCode : classCode);
        String classId = entryYear + "-" + classSegment;
        String actCode = StringUtils.isNotEmpty(experiment.getActivityCode()) ? safe(experiment.getActivityCode()) : "exp" + experiment.getExperimentId();
        String groupCode = String.format("group%02d", groupNo);
        return "county/" + safe(experiment.getDeptId()) + "/" + safe(experiment.getLessonId()) + "/" + safe(classId) + "/" + actCode + "/" + groupCode + "/data";
    }

    private String topicOf(IotExperiment experiment, IotDevice device)
    {
        IotGroup group = mapper.selectGroupById(device.getGroupId());
        if (group != null && StringUtils.isNotEmpty(group.getTopic()))
        {
            return group.getTopic();
        }
        return buildGroupTopic(experiment, group != null ? group.getEntryYear() : "2024", group != null ? group.getClassCode() : "01", group != null && group.getGroupNo() != null ? group.getGroupNo() : 1);
    }

    private IotExperiment requireExperiment(Long id)
    {
        if (id == null) throw new ServiceException("实验不能为空");
        IotExperiment experiment = mapper.selectExperimentById(id);
        if (experiment == null || !belongsToSchool(experiment.getDeptId())) throw new ServiceException("实验不存在或无权查看");
        return experiment;
    }

    private boolean canManageLesson(BizLesson lesson)
    {
        return SecurityUtils.isAdmin(SecurityUtils.getUserId())
                || SecurityUtils.getUserId().equals(lesson.getCreatorId());
    }

    private void requirePasscodeSecret()
    {
        if (org.apache.commons.lang3.StringUtils.isBlank(mqttProperties.getPasscodeSecret()))
            throw new ServiceException("物联网口令加密密钥未配置，请先设置 IOT_PASSCODE_SECRET");
    }

    private boolean canViewLesson(BizLesson lesson)
    {
        return canManageLesson(lesson) || mapper.countTeacherLessonScope(lesson.getLessonId(), SecurityUtils.getUserId(), SecurityUtils.getDeptId()) > 0;
    }

    private boolean canManageExperiment(IotExperiment experiment)
    {
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(experiment.getLessonId());
        return lesson != null && canManageLesson(lesson);
    }

    public void assertCanViewExperiment(Long experimentId)
    {
        IotExperiment experiment = requireExperiment(experimentId);
        if (!canViewExperiment(experiment)) throw new ServiceException("无权查看该实验数据");
    }

    private boolean canViewExperiment(IotExperiment experiment)
    {
        if (canManageExperiment(experiment)) return true;
        return mapper.countTeacherGroupScope(experiment.getExperimentId(), SecurityUtils.getUserId(), SecurityUtils.getDeptId()) > 0;
    }

    private boolean canViewClassConfig(IotExperiment experiment, String entryYear, String classCode)
    {
        return canManageExperiment(experiment)
                || mapper.countTeacherClassScope(SecurityUtils.getUserId(), SecurityUtils.getDeptId(),
                        entryYear, classCode) > 0;
    }

    private boolean belongsToSchool(Long deptId) { return deptId != null && deptId.equals(SecurityUtils.getDeptId()); }

    private String safe(Object value) { return String.valueOf(value).replaceAll("[^A-Za-z0-9_-]", "_"); }

    private String normalizeClass(String value)
    {
        return value == null ? "" : value.replace("班", "").trim();
    }

    private String parseBrokerHost(String brokerUrl)
    {
        if (brokerUrl == null || brokerUrl.trim().isEmpty()) return "10.52.1.129";
        String value = brokerUrl.trim();
        try
        {
            URI uri = new URI(value.contains("://") ? value : "tcp://" + value);
            String host = uri.getHost();
            if (host != null && !host.trim().isEmpty())
            {
                return host.startsWith("[") && host.endsWith("]")
                        ? host.substring(1, host.length() - 1) : host;
            }
        }
        catch (URISyntaxException ignored)
        {
            // 兼容旧配置中的非标准地址，下面只保留主机部分，避免坏配置阻断页面加载。
        }
        String host = value.replaceFirst("^[A-Za-z][A-Za-z0-9+.-]*://", "");
        int slash = host.indexOf('/');
        if (slash >= 0) host = host.substring(0, slash);
        if (host.startsWith("["))
        {
            int closing = host.indexOf(']');
            if (closing > 0) return host.substring(1, closing);
        }
        int colon = host.indexOf(':');
        return colon > 0 ? host.substring(0, colon) : host;
    }

    private int parseBrokerPort(String brokerUrl)
    {
        if (brokerUrl == null || brokerUrl.trim().isEmpty()) return 1883;
        String value = brokerUrl.trim();
        try
        {
            URI uri = new URI(value.contains("://") ? value : "tcp://" + value);
            if (uri.getPort() > 0 && uri.getPort() <= 65535) return uri.getPort();
        }
        catch (URISyntaxException ignored) { }
        int separator = value.lastIndexOf(':');
        if (separator >= 0 && separator < value.length() - 1)
        {
            try
            {
                int port = Integer.parseInt(value.substring(separator + 1));
                if (port > 0 && port <= 65535) return port;
            }
            catch (NumberFormatException ignored) { }
        }
        return 1883;
    }
}
