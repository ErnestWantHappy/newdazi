package com.ruoyi.business.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.config.WpsWebOfficeProperties;
import com.ruoyi.business.provider.CryptPadAdapter;
import com.ruoyi.business.provider.MockCollaborationProvider;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizLessonAssignment;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.CollaborationRoom;
import com.ruoyi.business.domain.PracticalQuestionMaterial;
import com.ruoyi.business.domain.dto.CollaborationSettingsRequest;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizQuestionMapper;
import com.ruoyi.business.mapper.BizLessonQuestionMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.CollaborationMapper;
import com.ruoyi.business.mapper.PracticalArtifactMapper;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/** 在线协作业务层：课程和班级归平台，编辑器通过 Provider 接入。 */
@Service
public class CollaborationRoomService
{
    private static final Set<String> EDITABLE_EXTENSIONS = new HashSet<String>(Arrays.asList(
            "doc", "dot", "wps", "wpt", "docx", "dotx", "docm", "dotm", "rtf",
            "ppt", "pptx", "pptm", "ppsx", "ppsm", "pps", "potx", "potm", "dpt", "dps",
            "xls", "xlt", "et", "xlsx", "xltx", "csv", "xlsm", "xltm"));

    @Value("${collaboration.enabled:false}")
    private boolean enabled;

    @Value("${collaboration.provider:}")
    private String provider;

    @Autowired private WpsWebOfficeProperties properties;
    @Autowired private CollaborationMapper collaborationMapper;
    @Autowired private BizLessonMapper lessonMapper;
    @Autowired private BizLessonQuestionMapper lessonQuestionMapper;
    @Autowired private BizQuestionMapper questionMapper;
    @Autowired private BizLessonAssignmentMapper assignmentMapper;
    @Autowired private PracticalArtifactMapper artifactMapper;
    @Autowired private BizStudentMapper studentMapper;
    @Autowired private CollaborationTokenService tokenService;
    @Autowired private CryptPadAdapter cryptPadAdapter;
    @Autowired private MockCollaborationProvider mockCollaborationProvider;
    @Autowired private CollaborationSecretService secretService;

    public Map<String, Object> health()
    {
        if (isCryptPadProvider()) return cryptPadHealth();
        PublicAddressAssessment publicAddress = assessPublicAddress(properties.getPublicBaseUrl());
        boolean writable = storageWritable();
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("enabled", enabled);
        result.put("appIdConfigured", StringUtils.isNotBlank(properties.getAppId()));
        result.put("appSecretConfigured", StringUtils.isNotBlank(properties.getAppSecret()));
        result.put("tokenSecretConfigured", StringUtils.isNotBlank(properties.getTokenSecret()));
        result.put("publicBaseUrlConfigured", StringUtils.isNotBlank(properties.getPublicBaseUrl()));
        result.put("publicBaseUrlLooksPublic", publicAddress.isPublic());
        result.put("publicBaseUrlHost", publicAddress.getHost());
        result.put("publicBaseUrlResolvedAddresses", publicAddress.getResolvedAddresses());
        result.put("sdkUrlConfigured", StringUtils.isNotBlank(properties.getSdkUrl()));
        result.put("storageWritable", writable);
        result.put("testConcurrentDocumentLimit", 5);
        result.put("testMaxFileBytes", properties.getTestMaxFileBytes());
        List<String> problems = new ArrayList<String>();
        if (!enabled) problems.add("未设置 COLLABORATION_ENABLED=true");
        if (StringUtils.isBlank(properties.getAppId())) problems.add("未配置 WPS_WEBOFFICE_APP_ID");
        if (StringUtils.isBlank(properties.getAppSecret())) problems.add("未配置 WPS_WEBOFFICE_APP_SECRET");
        if (StringUtils.isBlank(properties.getTokenSecret())) problems.add("未配置 WPS_WEBOFFICE_TOKEN_SECRET");
        if (!publicAddress.isPublic()) problems.add(publicAddress.getProblem());
        if (StringUtils.isBlank(properties.getSdkUrl())) problems.add("未配置 WPS_WEBOFFICE_SDK_URL");
        if (!writable) problems.add("协作文档存储目录不可写");
        result.put("ready", problems.isEmpty());
        result.put("problems", problems);
        return result;
    }

    public Map<String, Object> teacherSettings(Long lessonId)
    {
        BizLesson lesson = requireTeacherLesson(lessonId);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("health", health());
        result.put("lessonId", lessonId);
        result.put("lessonTitle", lesson.getLessonTitle());
        result.put("candidates", materialCandidates(lessonId));
        List<CollaborationRoom> rooms = collaborationMapper.selectRoomsByLesson(lessonId, lesson.getDeptId());
        result.put("enabled", hasOpenRoom(rooms));
        result.put("rooms", publicRooms(rooms, true));
        List<Map<String, Object>> members = new ArrayList<Map<String, Object>>();
        for (CollaborationRoom room : rooms)
        {
            BizStudent query = new BizStudent();
            query.setDeptId(lesson.getDeptId());
            query.setEntryYear(room.getEntryYear());
            query.setClassCode(room.getClassCode());
            for (BizStudent student : studentMapper.selectBizStudentList(query))
            {
                Map<String, Object> member = new LinkedHashMap<String, Object>();
                member.put("studentId", student.getStudentId());
                member.put("studentNo", student.getStudentNo());
                member.put("studentName", student.getStudentName());
                member.put("entryYear", student.getEntryYear());
                member.put("classCode", student.getClassCode());
                member.put("loginBound", student.getUserId() != null);
                members.add(member);
            }
        }
        result.put("members", members);
        if (!rooms.isEmpty())
        {
            result.put("questionId", rooms.get(0).getQuestionId());
            result.put("materialId", rooms.get(0).getSourceMaterialId());
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> saveTeacherSettings(Long lessonId, CollaborationSettingsRequest request)
            throws IOException
    {
        BizLesson lesson = requireTeacherLesson(lessonId);
        if (request == null || !Boolean.TRUE.equals(request.getEnabled()))
        {
            // 只关闭未绑定小组活动的旧全班房间；小组房间与历史作品不受课程开关影响，密钥也不轮换。
            collaborationMapper.closeNonActivityRooms(lessonId, lesson.getDeptId());
            if (isCryptPadProvider())
            {
                for (CollaborationRoom room : collaborationMapper.selectRoomsByLesson(lessonId, lesson.getDeptId()))
                {
                    if (collaborationMapper.countActivityRoom(room.getRoomId()) > 0) continue;
                    collaborationMapper.updateRoomProvider(room.getRoomId(), "CRYPTPAD",
                            secretService.encrypt(secretService.generateKey()));
                }
            }
            return teacherSettings(lessonId);
        }
        requireReady();
        if (request.getQuestionId() == null || request.getMaterialId() == null)
        {
            throw new ServiceException("请先选择操作题和可编辑的起始文件");
        }
        requirePracticalQuestion(lessonId, request.getQuestionId());
        PracticalQuestionMaterial material = requireStarterMaterial(request.getQuestionId(), request.getMaterialId());
        validateMaterial(material);
        List<BizLessonAssignment> assignments = assignmentMapper.selectAssignmentsByLessonId(lessonId);
        if (assignments == null || assignments.isEmpty())
        {
            throw new ServiceException("课程尚未指派班级，无法创建独立协作房间");
        }
        // 新配置只关闭未绑定小组活动的旧全班房间；本次选中的同一业务房间会在下面重新打开。
        collaborationMapper.closeNonActivityRooms(lessonId, lesson.getDeptId());
        for (BizLessonAssignment assignment : assignments)
        {
            if (!lesson.getDeptId().equals(assignment.getDeptId())) continue;
            createOrReopenRoom(lesson, assignment, material);
        }
        return teacherSettings(lessonId);
    }

    public List<Map<String, Object>> currentStudentRooms()
    {
        // 编辑器停用期间不再向学生端暴露历史房间，保留数据仅用于后续迁移。
        if (!enabled) return new ArrayList<Map<String, Object>>();
        Long userId = SecurityUtils.getUserId();
        Long deptId = SecurityUtils.getDeptId();
        BizStudent student = studentMapper.selectBizStudentByUserId(userId);
        if (student == null) throw new ServiceException("当前账号不是学生");
        Long lessonId = assignmentMapper.selectCurrentLessonByClass(
                student.getEntryYear(), normalizeClass(student.getClassCode()), deptId);
        if (lessonId == null) return new ArrayList<Map<String, Object>>();
        Map<String,Object> active = collaborationMapper.selectCurrentActivity(lessonId, deptId, student.getEntryYear(), normalizeClass(student.getClassCode()));
        List<CollaborationRoom> matches = new ArrayList<CollaborationRoom>();
        for (CollaborationRoom room : collaborationMapper.selectRoomsByLesson(lessonId, deptId))
        {
            if (student.getEntryYear().equals(room.getEntryYear())
                    && normalizeClass(student.getClassCode()).equals(normalizeClass(room.getClassCode()))
                    && "OPEN".equals(room.getStatus()))
            {
                if ((active == null && collaborationMapper.countActivityRoom(room.getRoomId()) == 0)
                        || collaborationMapper.countActivityRoomMembership(room.getRoomId(), student.getStudentId()) > 0)
                    matches.add(room);
            }
        }
        // 小组活动房间只对本组成员可见：打标供学生端正名展示（本组房间/全班共享）
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (CollaborationRoom room : matches)
        {
            Map<String, Object> item = publicRoom(room, false);
            item.put("groupRoom", collaborationMapper.countActivityRoom(room.getRoomId()) > 0);
            result.add(item);
        }
        return result;
    }

    public List<Map<String,Object>> studentHistory()
    {
        BizStudent student = studentMapper.selectBizStudentByUserId(SecurityUtils.getUserId());
        if (student == null) throw new ServiceException("当前账号不是学生");
        return collaborationMapper.selectStudentHistory(student.getStudentId(), SecurityUtils.getDeptId());
    }
    /**
     * 编辑器成员抽屉：本组学生花名册、各人最近进入时间与进入过的教师。
     * 权限沿用房间访问规则，学生只能看到本人小组。
     */
    public Map<String, Object> roomRoster(Long roomId)
    {
        CollaborationRoom room = requireRoom(roomId);
        requireRoomAccess(room, SecurityUtils.getUserId());
        List<Map<String, Object>> roster = collaborationMapper.selectRoomRoster(roomId);
        String groupName = roster.isEmpty() ? null : String.valueOf(roster.get(0).get("groupName"));
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("roomId", roomId);
        result.put("roomTitle", room.getRoomTitle());
        result.put("fileName", room.getCurrentFileName());
        result.put("groupName", groupName);
        result.put("members", roster);
        result.put("teachers", collaborationMapper.selectRoomTeacherEnters(roomId));
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createSession(Long roomId)
    {
        CollaborationRoom room = requireRoom(roomId);
        if ("CLOSED".equals(room.getStatus())) throw new ServiceException("该班级协作房间已关闭");
        Long userId = SecurityUtils.getUserId();
        String scope = requireRoomAccess(room, userId);
        collaborationMapper.lockLesson(room.getLessonId());
        room = requireRoom(roomId);
        if ("CLOSED".equals(room.getStatus())) throw new ServiceException("该班级协作房间已关闭");
        scope = requireRoomAccess(room, userId);
        // 先完成班级权限判断，再阻断不可达配置，避免向无权用户暴露内部网络诊断。
        requireReady();
        collaborationMapper.markRoomOpened(roomId, new Date());
        collaborationMapper.insertOperationEvent(roomId, userId,
                collaborationMapper.selectStudentIdByUserId(userId), "ENTER", null, new Date());
        if ("STUDENT".equals(scope)) collaborationMapper.freezeActivityForRoom(roomId, new Date());
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        if (isCryptPadProvider())
        {
            result.putAll(cryptPadAdapter.session(room, userId, scope,
                    sessionDisplayName(userId, scope)));
        }
        else
        {
            result.put("appId", properties.getAppId());
            result.put("fileId", room.getPublicFileId());
            result.put("officeType", officeType(room.getCurrentFileExtension()));
            result.put("token", tokenService.issue(userId, roomId, scope));
            result.put("tokenTimeout", properties.getTokenMinutes() * 60_000L);
            result.put("sdkUrl", properties.getSdkUrl());
            result.put("endpoint", properties.getEndpoint());
        }
        result.put("room", publicRoom(room, true));
        result.put("readOnly", "READ_ONLY".equals(room.getStatus()));
        return result;
    }

    /**
     * 协作参与者需要看到可辨认的课堂身份，但不应把内部用户 ID 或登录账号暴露给编辑器。
     */
    private String sessionDisplayName(Long userId, String scope)
    {
        if ("STUDENT".equals(scope))
        {
            BizStudent student = studentMapper.selectBizStudentByUserId(userId);
            if (student != null)
            {
                String studentNo = cleanDisplayPart(student.getStudentNo());
                String studentName = cleanDisplayPart(student.getStudentName());
                if (StringUtils.isNotBlank(studentNo) && StringUtils.isNotBlank(studentName))
                    return studentNo + " " + studentName;
                if (StringUtils.isNotBlank(studentName)) return studentName;
                if (StringUtils.isNotBlank(studentNo)) return studentNo;
            }
        }
        try
        {
            String nickName = cleanDisplayPart(SecurityUtils.getLoginUser().getUser().getNickName());
            if (StringUtils.isNotBlank(nickName)) return nickName;
        }
        catch (Exception ignored)
        {
            // 会话已经通过权限校验，展示名读取失败时只回退到通用名称，不影响进入房间。
        }
        return "协作用户";
    }

    private String cleanDisplayPart(String value)
    {
        return StringUtils.defaultString(value).replaceAll("[\\r\\n\\t]", " ").trim();
    }

    public CollaborationRoom requireRoom(Long roomId)
    {
        CollaborationRoom room = collaborationMapper.selectRoomById(roomId);
        if (room == null) throw new ServiceException("协作房间不存在");
        return room;
    }

    /** 教师监管：读取房间不可变版本历史（管理员或课程创建者，且课程属于本校）。 */
    public List<Map<String, Object>> listRevisions(Long roomId)
    {
        CollaborationRoom room = requireRoom(roomId);
        requireTeacherLesson(room.getLessonId());
        List<Map<String, Object>> revisions = collaborationMapper.selectRevisionsByRoomId(roomId);
        return revisions == null ? new ArrayList<Map<String, Object>>() : revisions;
    }

    public CollaborationRoom requireRoomByFileId(String fileId)
    {
        CollaborationRoom room = collaborationMapper.selectRoomByPublicFileId(fileId);
        if (room == null) throw new ServiceException("协作文档不存在");
        return room;
    }

    /** 为非计分小组活动复制独立起始文件；taskVersionId 仅作为房间内部题目标识，成绩链路不会读取。 */
    public CollaborationRoom createGroupActivityRoom(BizLesson lesson, String entryYear, String classCode,
                                                      PracticalQuestionMaterial material, Long taskVersionId,
                                                      String title) throws IOException
    {
        validateMaterial(material);
        String fileId = "g" + UUID.randomUUID().toString().replace("-", "");
        String extension = normalizeExtension(material.getFileExtension(), material.getOriginalFileName());
        String fileName = sanitizeFileName(material.getOriginalFileName(), extension);
        String relativePath = "collaboration/rooms/" + fileId + "/v1." + extension;
        Path target = resolveStoredFile(relativePath);
        Files.createDirectories(target.getParent());
        Files.copy(resolveMaterialPath(material.getResourcePath()), target, StandardCopyOption.COPY_ATTRIBUTES);
        Date now = new Date();
        CollaborationRoom room = new CollaborationRoom();
        room.setProvider(providerName()); room.setProviderSessionKey(isCryptPadProvider() ? secretService.encrypt(secretService.generateKey()) : null);
        // 小组任务使用负值命名空间，避免与旧全班房间的真实题目编号碰撞。
        room.setPublicFileId(fileId); room.setLessonId(lesson.getLessonId()); room.setQuestionId(-taskVersionId);
        room.setSourceMaterialId(material.getMaterialId()); room.setDeptId(lesson.getDeptId()); room.setEntryYear(entryYear); room.setClassCode(normalizeClass(classCode));
        room.setRoomTitle(truncate(StringUtils.defaultIfBlank(title, lesson.getLessonTitle()) + " - " + classCode + "组协作", 240)); room.setStatus("OPEN"); room.setCurrentVersion(1);
        room.setCurrentFileName(fileName); room.setCurrentFilePath(relativePath); room.setCurrentFileExtension(extension); room.setCurrentMimeType(material.getMimeType());
        room.setCurrentFileSize(Files.size(target)); room.setCurrentSha256(digest(target, "SHA-256")); room.setCreatorUserId(SecurityUtils.getUserId()); room.setModifierUserId(SecurityUtils.getUserId()); room.setCreateTime(now); room.setUpdateTime(now);
        collaborationMapper.insertRoom(room);
        collaborationMapper.insertRevision(room.getRoomId(), 1, fileName, relativePath, room.getCurrentFileSize(), room.getCurrentSha256(), "sha256", room.getCurrentSha256(), false, SecurityUtils.getUserId(), now);
        return room;
    }

    /**
     * 小组活动只能复用当前课程已选操作题的可编辑起始文件，不能按题目 ID 跨课程取材料。
     */
    public PracticalQuestionMaterial requireGroupActivityStarter(Long lessonId, Long questionId, Long materialId)
            throws IOException
    {
        if (questionId == null || materialId == null)
            throw new ServiceException("任务版本缺少操作题或起始文件");
        requirePracticalQuestion(lessonId, questionId);
        PracticalQuestionMaterial material = requireStarterMaterial(questionId, materialId);
        validateMaterial(material);
        return material;
    }

    /** 文档下载和保存接口共用同一套平台房间权限校验。 */
    public String assertRoomAccess(Long roomId)
    {
        CollaborationRoom room = requireRoom(roomId);
        if ("CLOSED".equals(room.getStatus())) throw new ServiceException("该班级协作房间已关闭");
        return requireRoomAccess(room, SecurityUtils.getUserId());
    }

    public Path resolveStoredFile(String relativePath)
    {
        if (StringUtils.isBlank(relativePath) || relativePath.contains(".."))
            throw new ServiceException("协作文档路径无效");
        Path root = Paths.get(RuoYiConfig.getProfile()).toAbsolutePath().normalize();
        Path target = root.resolve(relativePath.replace('\\', '/')).normalize();
        if (!target.startsWith(root)) throw new ServiceException("协作文档路径越界");
        return target;
    }

    public String publicBaseUrl()
    {
        return StringUtils.removeEnd(StringUtils.trimToEmpty(properties.getPublicBaseUrl()), "/");
    }

    private void createOrReopenRoom(BizLesson lesson, BizLessonAssignment assignment,
                                    PracticalQuestionMaterial material) throws IOException
    {
        String classCode = normalizeClass(assignment.getClassCode());
        CollaborationRoom existing = collaborationMapper.selectRoomByClass(lesson.getLessonId(),
                material.getQuestionId(), lesson.getDeptId(), assignment.getEntryYear(), classCode);
        if (existing != null)
        {
            if (!material.getMaterialId().equals(existing.getSourceMaterialId()))
                throw new ServiceException("已有班级房间使用另一份起始文件，请新建操作题后再切换模板");
            if (!providerName().equalsIgnoreCase(StringUtils.defaultString(existing.getProvider())))
            {
                String key = isCryptPadProvider() ? secretService.encrypt(secretService.generateKey()) : null;
                collaborationMapper.updateRoomProvider(existing.getRoomId(), providerName(), key);
            }
            collaborationMapper.reopenRoom(existing.getRoomId(), "OPEN");
            return;
        }
        String fileId = "c" + UUID.randomUUID().toString().replace("-", "");
        String extension = normalizeExtension(material.getFileExtension(), material.getOriginalFileName());
        String fileName = sanitizeFileName(material.getOriginalFileName(), extension);
        String relativePath = "collaboration/rooms/" + fileId + "/v1." + extension;
        Path source = resolveMaterialPath(material.getResourcePath());
        Path target = resolveStoredFile(relativePath);
        Files.createDirectories(target.getParent());
        Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
        long size = Files.size(target);
        String sha256 = digest(target, "SHA-256");
        Date now = new Date();
        CollaborationRoom room = new CollaborationRoom();
        room.setProvider(providerName());
        if (isCryptPadProvider()) room.setProviderSessionKey(secretService.encrypt(secretService.generateKey()));
        room.setPublicFileId(fileId);
        room.setLessonId(lesson.getLessonId());
        room.setQuestionId(material.getQuestionId());
        room.setSourceMaterialId(material.getMaterialId());
        room.setDeptId(lesson.getDeptId());
        room.setEntryYear(assignment.getEntryYear());
        room.setClassCode(classCode);
        room.setRoomTitle(truncate(lesson.getLessonTitle() + " - " + classCode + "班协作", 240));
        room.setStatus("OPEN");
        room.setCurrentVersion(1);
        room.setCurrentFileName(fileName);
        room.setCurrentFilePath(relativePath);
        room.setCurrentFileExtension(extension);
        room.setCurrentMimeType(material.getMimeType());
        room.setCurrentFileSize(size);
        room.setCurrentSha256(sha256);
        room.setCreatorUserId(SecurityUtils.getUserId());
        room.setModifierUserId(SecurityUtils.getUserId());
        room.setCreateTime(now);
        room.setUpdateTime(now);
        collaborationMapper.insertRoom(room);
        collaborationMapper.insertRevision(room.getRoomId(), 1, fileName, relativePath, size, sha256,
                "sha256", sha256, false, SecurityUtils.getUserId(), now);
    }

    private BizLesson requireTeacherLesson(Long lessonId)
    {
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(lessonId);
        Long userId = SecurityUtils.getUserId();
        if (lesson == null || lesson.getDeptId() == null || !lesson.getDeptId().equals(SecurityUtils.getDeptId()))
            throw new ServiceException("课程不存在或不属于当前学校");
        if (!SecurityUtils.isAdmin(userId) && !userId.equals(lesson.getCreatorId()))
            throw new ServiceException("只能配置自己创建的课程");
        return lesson;
    }

    private String requireRoomAccess(CollaborationRoom room, Long userId)
    {
        BizStudent student = studentMapper.selectBizStudentByUserId(userId);
        if (student != null)
        {
            if ("READ_ONLY".equals(room.getStatus()) && room.getDeptId().equals(SecurityUtils.getDeptId())
                    && collaborationMapper.countActivityRoom(room.getRoomId()) > 0
                    && collaborationMapper.countActivityRoomMembership(room.getRoomId(), student.getStudentId()) > 0)
                return "STUDENT";
            Long current = assignmentMapper.selectCurrentLessonByClass(student.getEntryYear(),
                    normalizeClass(student.getClassCode()), SecurityUtils.getDeptId());
            if (!room.getDeptId().equals(SecurityUtils.getDeptId())
                    || !room.getLessonId().equals(current)
                    || !room.getEntryYear().equals(student.getEntryYear())
                    || !normalizeClass(room.getClassCode()).equals(normalizeClass(student.getClassCode())))
                throw new ServiceException("只能进入自己当前课程的班级协作房间");
            if (collaborationMapper.countActivityRoom(room.getRoomId()) > 0
                    && collaborationMapper.countActivityRoomMembership(room.getRoomId(), student.getStudentId()) == 0)
                throw new ServiceException("只能进入本人小组的协作房间");
            return "STUDENT";
        }
        BizLesson lesson = requireTeacherLesson(room.getLessonId());
        if (!lesson.getDeptId().equals(room.getDeptId())) throw new ServiceException("无权访问该房间");
        return "TEACHER";
    }

    /**
     * 协作起始文件只来源于题库（公开或本人创建的文件作品题），与课程是否选用操作题无关。
     * 管理员不过滤可见范围，教师只看公开或本人题目。
     */
    public List<Map<String, Object>> bankMaterialCandidates()
    {
        Long userId = SecurityUtils.getUserId();
        Long creatorId = SecurityUtils.isAdmin(userId) ? null : userId;
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> row : collaborationMapper.selectBankStarterCandidates(creatorId))
        {
            Map<String, Object> item = enrichCandidate(row);
            if (item != null) result.add(item);
        }
        return result;
    }

    /**
     * 协作选题弹窗：按年级/学期/课次/关键词分页搜索，返回 {total, rows}。
     */
    public Map<String, Object> searchBankMaterials(Long grade, String semester, Integer lessonNum, String keyword, int pageNum, int pageSize)
    {
        Long userId = SecurityUtils.getUserId();
        Long creatorId = SecurityUtils.isAdmin(userId) ? null : userId;
        com.github.pagehelper.PageHelper.startPage(Math.max(pageNum, 1), Math.min(Math.max(pageSize, 1), 50));
        List<Map<String, Object>> rows = collaborationMapper.selectBankStarterSearch(creatorId, grade, semester, lessonNum, keyword);
        long total = new com.github.pagehelper.PageInfo<Map<String, Object>>(rows).getTotal();
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> row : rows)
        {
            Map<String, Object> item = enrichCandidate(row);
            if (item != null) result.add(item);
        }
        Map<String, Object> page = new LinkedHashMap<String, Object>();
        page.put("total", total);
        page.put("rows", result);
        return page;
    }

    /**
     * 候选行装配：过滤不可编辑格式，file_size 缺失时按物理文件回填。
     */
    private Map<String, Object> enrichCandidate(Map<String, Object> row)
    {
        String extension = normalizeExtension(StringUtils.defaultString((String) row.get("fileExtension")), StringUtils.defaultString((String) row.get("fileName")));
        if (!EDITABLE_EXTENSIONS.contains(extension)) return null;
        Number fileSize = (Number) row.get("fileSize");
        // 存量数据的 file_size 可能为空或 0，运行时按物理文件回填，保证前端不再显示 0B
        if (fileSize == null || fileSize.longValue() <= 0)
        {
            Long physical = physicalStarterSize((String) row.get("resourcePath"));
            if (physical != null && physical > 0) fileSize = physical;
        }
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        item.put("questionId", row.get("questionId"));
        item.put("questionContent", row.get("questionContent"));
        item.put("grade", row.get("grade"));
        item.put("semester", row.get("semester"));
        item.put("lessonNum", row.get("lessonNum"));
        item.put("materialId", row.get("materialId"));
        item.put("previewPath", row.get("previewPath"));
        item.put("fileName", row.get("fileName"));
        item.put("fileExtension", extension);
        item.put("fileSize", fileSize == null ? 0 : fileSize.longValue());
        item.put("withinTestLimit", fileSize == null || fileSize.longValue() <= maxFileBytes());
        return item;
    }

    /**
     * 读取起始文件的物理大小；路径非法或文件缺失时返回 null，不抛错。
     */
    private Long physicalStarterSize(String resourcePath)
    {
        try
        {
            Path source = resolveMaterialPath(resourcePath);
            if (!Files.isRegularFile(source)) return null;
            return Files.size(source);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private List<Map<String, Object>> materialCandidates(Long lessonId)
    {
        return bankMaterialCandidates();
    }

    /**
     * 协作只能复用题库中的文件作品题（公开或本人创建），不能跨权限取他人私有题目。
     */
    private void requireBankPracticalQuestion(Long questionId)
    {
        com.ruoyi.business.domain.BizQuestion question = questionId == null ? null
                : questionMapper.selectBizQuestionByQuestionId(questionId);
        if (question == null || !"practical".equalsIgnoreCase(question.getQuestionType())
                || !"FILE".equalsIgnoreCase(question.getPracticalMode()))
            throw new ServiceException("所选操作题不是题库中的文件作品题");
        Long userId = SecurityUtils.getUserId();
        if (!SecurityUtils.isAdmin(userId) && !"Y".equalsIgnoreCase(question.getIsPublic())
                && !"1".equals(question.getIsPublic()) && !userId.equals(question.getCreatorId()))
            throw new ServiceException("无权使用该操作题作为协作起始文件");
    }

    private void requirePracticalQuestion(Long lessonId, Long questionId)
    {
        requireBankPracticalQuestion(questionId);
    }

    private PracticalQuestionMaterial requireStarterMaterial(Long questionId, Long materialId)
    {
        for (PracticalQuestionMaterial material : artifactMapper.selectMaterialsByQuestion(questionId))
            if (materialId.equals(material.getMaterialId()) && "STARTER".equalsIgnoreCase(material.getMaterialType())) return material;
        throw new ServiceException("所选文件不是当前操作题的起始文件");
    }

    private void validateMaterial(PracticalQuestionMaterial material) throws IOException
    {
        String extension = normalizeExtension(material.getFileExtension(), material.getOriginalFileName());
        if (!EDITABLE_EXTENSIONS.contains(extension)) throw new ServiceException("在线协作仅支持可编辑的 Word、Excel、PPT 文件");
        Path source = resolveMaterialPath(material.getResourcePath());
        if (!Files.isRegularFile(source)) throw new ServiceException("操作题起始文件在服务器上不存在");
        if (Files.size(source) > maxFileBytes())
            throw new ServiceException("文件超过在线协作单文件限制，请换用较小文件");
    }

    private Path resolveMaterialPath(String resourcePath)
    {
        String normalized = StringUtils.defaultString(resourcePath).replace('\\', '/');
        int index = normalized.toLowerCase(Locale.ROOT).indexOf("/profile/");
        if (index < 0 && normalized.toLowerCase(Locale.ROOT).startsWith("profile/")) index = -1;
        String relative;
        if (index >= 0) relative = normalized.substring(index + "/profile/".length());
        else if (normalized.toLowerCase(Locale.ROOT).startsWith("profile/")) relative = normalized.substring("profile/".length());
        else throw new ServiceException("起始文件不是平台 profile 资源");
        Path root = Paths.get(RuoYiConfig.getProfile()).toAbsolutePath().normalize();
        Path target = root.resolve(relative).normalize();
        if (!target.startsWith(root)) throw new ServiceException("起始文件路径越界");
        return target;
    }

    private void requireReady()
    {
        if (isCryptPadProvider())
        {
            if (!enabled) throw new ServiceException("在线协作功能当前未开启");
            if (!cryptPadAdapter.ready()) throw new ServiceException("CryptPad 尚未就绪，请检查外置配置");
            return;
        }
        @SuppressWarnings("unchecked")
        List<String> problems = (List<String>) health().get("problems");
        if (!problems.isEmpty()) throw new ServiceException("WPS PoC 尚未就绪：" + String.join("；", problems));
    }

    private boolean isCryptPadProvider()
    {
        return "CRYPTPAD".equalsIgnoreCase(StringUtils.defaultString(provider));
    }

    private String providerName()
    {
        if (isCryptPadProvider()) return "CRYPTPAD";
        if ("MOCK".equalsIgnoreCase(StringUtils.defaultString(provider))) return "MOCK";
        return "WPS";
    }

    private long maxFileBytes()
    {
        return isCryptPadProvider() ? 50L * 1024L * 1024L : properties.getTestMaxFileBytes();
    }

    private Map<String, Object> cryptPadHealth()
    {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("enabled", enabled);
        result.put("provider", providerName());
        result.put("storageWritable", storageWritable());
        result.put("maxFileBytes", maxFileBytes());
        result.putAll(cryptPadAdapter.health());
        List<String> problems = new ArrayList<String>();
        if (!enabled) problems.add("未设置 COLLABORATION_ENABLED=true");
        if (!cryptPadAdapter.ready()) problems.add("CryptPad 地址、集成脚本或密钥外置配置不完整");
        if (!storageWritable()) problems.add("协作文档存储目录不可写");
        result.put("ready", problems.isEmpty());
        result.put("problems", problems);
        return result;
    }

    private boolean hasOpenRoom(List<CollaborationRoom> rooms)
    {
        for (CollaborationRoom room : rooms) if (!"CLOSED".equals(room.getStatus())) return true;
        return false;
    }

    private List<Map<String, Object>> publicRooms(List<CollaborationRoom> rooms, boolean teacher)
    {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (CollaborationRoom room : rooms) result.add(publicRoom(room, teacher));
        return result;
    }

    private Map<String, Object> publicRoom(CollaborationRoom room, boolean teacher)
    {
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        item.put("roomId", room.getRoomId());
        item.put("lessonId", room.getLessonId());
        item.put("questionId", room.getQuestionId());
        item.put("entryYear", room.getEntryYear());
        item.put("classCode", room.getClassCode());
        item.put("roomTitle", room.getRoomTitle());
        item.put("status", room.getStatus());
        item.put("fileName", room.getCurrentFileName());
        item.put("fileExtension", room.getCurrentFileExtension());
        item.put("fileSize", room.getCurrentFileSize());
        item.put("version", room.getCurrentVersion());
        item.put("lastOpenTime", room.getLastOpenTime());
        item.put("lastSaveTime", room.getLastSaveTime());
        if (teacher)
        {
            item.put("lastCallbackType", room.getLastCallbackType());
            item.put("lastCallbackStatus", room.getLastCallbackStatus());
            item.put("lastWpsRequestId", room.getLastWpsRequestId());
            item.put("lastErrorMessage", room.getLastErrorMessage());
        }
        return item;
    }

    private boolean storageWritable()
    {
        try
        {
            Path root = Paths.get(RuoYiConfig.getProfile(), "collaboration").toAbsolutePath().normalize();
            Files.createDirectories(root);
            return Files.isWritable(root);
        }
        catch (Exception e) { return false; }
    }

    private PublicAddressAssessment assessPublicAddress(String value)
    {
        if (StringUtils.isBlank(value))
            return PublicAddressAssessment.failure(null, "未配置 WPS_WEBOFFICE_PUBLIC_BASE_URL");
        try
        {
            URI uri = URI.create(value.trim());
            String scheme = StringUtils.lowerCase(uri.getScheme());
            String host = uri.getHost();
            if (!("http".equals(scheme) || "https".equals(scheme)) || StringUtils.isBlank(host))
                return PublicAddressAssessment.failure(host,
                        "WPS_WEBOFFICE_PUBLIC_BASE_URL 必须是带 http 或 https 的完整公网地址");
            InetAddress[] addresses = InetAddress.getAllByName(host);
            List<String> resolved = new ArrayList<String>();
            boolean hasPublicAddress = false;
            for (InetAddress address : addresses)
            {
                resolved.add(address.getHostAddress());
                if (!isPrivateAddress(address)) hasPublicAddress = true;
            }
            if (!hasPublicAddress)
                return PublicAddressAssessment.failure(host, resolved,
                        "WPS 公网回调域名 " + host + " 当前只解析到内网地址 "
                                + String.join(", ", resolved) + "，WPS 云端无法访问；请配置公网 DNS 和公网反向代理/NAT");
            return PublicAddressAssessment.success(host, resolved);
        }
        catch (Exception e)
        {
            return PublicAddressAssessment.failure(null,
                    "WPS 公网回调地址无法解析：" + safeMessage(e) + "；请检查公网 DNS 记录");
        }
    }

    private boolean isPrivateAddress(InetAddress address)
    {
        if (address.isAnyLocalAddress() || address.isLoopbackAddress()
                || address.isLinkLocalAddress() || address.isSiteLocalAddress()) return true;
        if (address instanceof Inet6Address)
        {
            byte[] bytes = address.getAddress();
            // IPv6 fc00::/7 是唯一本地地址，也不能作为 WPS 公网回调入口。
            return (bytes[0] & 0xFE) == 0xFC;
        }
        return false;
    }

    private String safeMessage(Exception e)
    {
        return StringUtils.defaultIfBlank(e.getMessage(), e.getClass().getSimpleName())
                .replace('\r', ' ').replace('\n', ' ');
    }

    private static final class PublicAddressAssessment
    {
        private final boolean publicAddress;
        private final String host;
        private final List<String> resolvedAddresses;
        private final String problem;

        private PublicAddressAssessment(boolean publicAddress, String host,
                                        List<String> resolvedAddresses, String problem)
        {
            this.publicAddress = publicAddress;
            this.host = host;
            this.resolvedAddresses = resolvedAddresses;
            this.problem = problem;
        }

        private static PublicAddressAssessment success(String host, List<String> resolvedAddresses)
        {
            return new PublicAddressAssessment(true, host, resolvedAddresses, null);
        }

        private static PublicAddressAssessment failure(String host, String problem)
        {
            return failure(host, new ArrayList<String>(), problem);
        }

        private static PublicAddressAssessment failure(String host, List<String> resolvedAddresses, String problem)
        {
            return new PublicAddressAssessment(false, host, resolvedAddresses, problem);
        }

        private boolean isPublic() { return publicAddress; }
        private String getHost() { return host; }
        private List<String> getResolvedAddresses() { return resolvedAddresses; }
        private String getProblem() { return problem; }
    }

    private String normalizeClass(String value) { return StringUtils.trimToEmpty(value).replace("班", ""); }

    private String normalizeExtension(String configured, String fileName)
    {
        String extension = StringUtils.trimToEmpty(configured).replace(".", "").toLowerCase(Locale.ROOT);
        if (StringUtils.isBlank(extension) && StringUtils.isNotBlank(fileName) && fileName.contains("."))
            extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        return extension;
    }

    private String sanitizeFileName(String value, String extension)
    {
        String name = StringUtils.defaultIfBlank(value, "班级协作文档." + extension)
                .replaceAll("[\\\\/|\":*?<>]", "_");
        return truncate(name, 240);
    }

    private String officeType(String extension)
    {
        String ext = StringUtils.lowerCase(extension);
        if (Arrays.asList("doc", "dot", "wps", "wpt", "docx", "dotx", "docm", "dotm", "rtf").contains(ext)) return "w";
        if (Arrays.asList("ppt", "pptx", "pptm", "ppsx", "ppsm", "pps", "potx", "potm", "dpt", "dps").contains(ext)) return "p";
        if (Arrays.asList("xls", "xlt", "et", "xlsx", "xltx", "csv", "xlsm", "xltm").contains(ext)) return "s";
        throw new ServiceException("WPS 不支持该文件类型：" + extension);
    }

    private String digest(Path file, String algorithm) throws IOException
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            try (InputStream input = Files.newInputStream(file); DigestInputStream ignored = new DigestInputStream(input, md))
            {
                byte[] buffer = new byte[8192];
                while (ignored.read(buffer) >= 0) { }
            }
            return Hex.encodeHexString(md.digest());
        }
        catch (IOException e) { throw e; }
        catch (Exception e) { throw new IOException("计算文件摘要失败", e); }
    }

    private String truncate(String value, int max) { return value != null && value.length() > max ? value.substring(0, max) : value; }
}
