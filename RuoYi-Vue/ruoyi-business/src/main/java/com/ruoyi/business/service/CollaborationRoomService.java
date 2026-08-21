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
            collaborationMapper.updateRoomStatus(lessonId, lesson.getDeptId(), "CLOSED");
            if (isCryptPadProvider())
            {
                for (CollaborationRoom room : collaborationMapper.selectRoomsByLesson(lessonId, lesson.getDeptId()))
                    collaborationMapper.updateRoomProvider(room.getRoomId(), "CRYPTPAD",
                            secretService.encrypt(secretService.generateKey()));
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
        // 新配置先关闭该课程旧房间；本次选中的同一业务房间会在下面重新打开。
        collaborationMapper.updateRoomStatus(lessonId, lesson.getDeptId(), "CLOSED");
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
        List<CollaborationRoom> matches = new ArrayList<CollaborationRoom>();
        for (CollaborationRoom room : collaborationMapper.selectRoomsByLesson(lessonId, deptId))
        {
            if (student.getEntryYear().equals(room.getEntryYear())
                    && normalizeClass(student.getClassCode()).equals(normalizeClass(room.getClassCode()))
                    && "OPEN".equals(room.getStatus()))
            {
                matches.add(room);
            }
        }
        return publicRooms(matches, false);
    }

    public Map<String, Object> createSession(Long roomId)
    {
        CollaborationRoom room = requireRoom(roomId);
        if ("CLOSED".equals(room.getStatus())) throw new ServiceException("该班级协作房间已关闭");
        Long userId = SecurityUtils.getUserId();
        String scope = requireRoomAccess(room, userId);
        // 先完成班级权限判断，再阻断不可达配置，避免向无权用户暴露内部网络诊断。
        requireReady();
        collaborationMapper.markRoomOpened(roomId, new Date());
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
            Long current = assignmentMapper.selectCurrentLessonByClass(student.getEntryYear(),
                    normalizeClass(student.getClassCode()), SecurityUtils.getDeptId());
            if (!room.getDeptId().equals(SecurityUtils.getDeptId())
                    || !room.getLessonId().equals(current)
                    || !room.getEntryYear().equals(student.getEntryYear())
                    || !normalizeClass(room.getClassCode()).equals(normalizeClass(student.getClassCode())))
                throw new ServiceException("只能进入自己当前课程的班级协作房间");
            return "STUDENT";
        }
        BizLesson lesson = requireTeacherLesson(room.getLessonId());
        if (!lesson.getDeptId().equals(room.getDeptId())) throw new ServiceException("无权访问该房间");
        return "TEACHER";
    }

    private List<Map<String, Object>> materialCandidates(Long lessonId)
    {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (BizLessonQuestionDetailVo question : lessonQuestionMapper.selectDetailsByLessonId(lessonId))
        {
            if (!"practical".equalsIgnoreCase(question.getQuestionType())) continue;
            for (PracticalQuestionMaterial material : artifactMapper.selectMaterialsByQuestion(question.getQuestionId()))
            {
                String extension = normalizeExtension(material.getFileExtension(), material.getOriginalFileName());
                if (!"STARTER".equalsIgnoreCase(material.getMaterialType()) || !EDITABLE_EXTENSIONS.contains(extension))
                    continue;
                Map<String, Object> item = new LinkedHashMap<String, Object>();
                item.put("questionId", question.getQuestionId());
                item.put("questionContent", question.getQuestionContent());
                item.put("materialId", material.getMaterialId());
                item.put("fileName", material.getOriginalFileName());
                item.put("fileExtension", extension);
                item.put("fileSize", material.getFileSize());
                item.put("withinTestLimit", material.getFileSize() == null
                        || material.getFileSize() <= maxFileBytes());
                result.add(item);
            }
        }
        return result;
    }

    private void requirePracticalQuestion(Long lessonId, Long questionId)
    {
        for (BizLessonQuestionDetailVo question : lessonQuestionMapper.selectDetailsByLessonId(lessonId))
            if (questionId.equals(question.getQuestionId()) && "practical".equalsIgnoreCase(question.getQuestionType())) return;
        throw new ServiceException("所选操作题不在当前课程中");
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
