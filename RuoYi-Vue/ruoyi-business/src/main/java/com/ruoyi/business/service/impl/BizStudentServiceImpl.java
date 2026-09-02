package com.ruoyi.business.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.redis.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.BizTeacherClass;
import com.ruoyi.business.domain.vo.StudentImportResult;
import com.ruoyi.business.domain.vo.StudentCorrectionPreview;
import com.ruoyi.business.domain.vo.StudentCorrectionRow;
import com.ruoyi.business.service.IBizStudentService;
import com.ruoyi.business.service.IBizTeacherClassService;
import com.ruoyi.business.service.AnswerDeletionGuardService;
import com.ruoyi.business.util.StudentImportRules;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysUserRole;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.mapper.SysUserRoleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 学生管理Service业务层处理 (编译修复版)
 */
@Service
public class BizStudentServiceImpl implements IBizStudentService
{
    private static final Long STUDENT_ROLE_ID = 101L;

    private static final int IMPORT_BATCH_SIZE = 200;

    private static final int IMPORT_DETAIL_LIMIT = 20;

    private static final String STUDENT_IMPORT_LOCK_PREFIX = "business:student-import:dept:";

    private static final Logger log = LoggerFactory.getLogger(BizStudentServiceImpl.class);
    @Autowired
    private BizStudentMapper bizStudentMapper;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private SysUserRoleMapper userRoleMapper;
    @Autowired
    private SysDeptMapper deptMapper;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private IBizTeacherClassService teacherClassService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private AnswerDeletionGuardService answerDeletionGuardService;

    @Override
    public BizStudent selectBizStudentByStudentId(Long studentId)
    {
        return bizStudentMapper.selectBizStudentByStudentId(studentId);
    }

    @Override
    public List<BizStudent> selectBizStudentList(BizStudent bizStudent)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long currentDeptId = null;
        Long currentUserId = null;
        if (loginUser != null && loginUser.getUser() != null)
        {
            currentDeptId = loginUser.getUser().getDeptId();
            currentUserId = loginUser.getUserId();
        }
        // 非超级管理员需要关联教师班级表过滤
        if (loginUser != null && loginUser.getUser() != null && !loginUser.getUser().isAdmin())
        {
            bizStudent.setDeptId(currentDeptId);
            bizStudent.setTeacherUserId(currentUserId);
        }
        if (StringUtils.isEmpty(bizStudent.getStatus()))
        {
            // 日常名单只展示仍在使用的账号；查看停用学生时由页面明确传 status=1/all。
            bizStudent.setStatus("0");
        }
        // 管理员未指定校区时保留空条件，展示其权限范围内的全部学生；
        // 传入 deptId 时仍按页面明确选择的校区过滤，避免默认部门造成空列表。
        return bizStudentMapper.selectBizStudentList(bizStudent);
    }

    @Override
    @Transactional
    public int insertBizStudent(BizStudent bizStudent)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long teacherDeptId = loginUser.getUser().getDeptId();
        String operName = loginUser.getUsername();
        if (StringUtils.isNull(teacherDeptId)) {
            throw new ServiceException("操作的教师账号没有关联学校，无法新增学生！");
        }

        SysDept school = deptMapper.selectDeptById(teacherDeptId);
        if (school == null || StringUtils.isEmpty(school.getSchoolCode())) {
            throw new ServiceException("教师关联的学校信息不完整，缺少学校官方ID (school_code)");
        }
        String schoolCode = school.getSchoolCode();

        bizStudent.setEntryYear(StudentImportRules.normalizeEntryYear(bizStudent.getEntryYear()));
        bizStudent.setClassCode(StudentImportRules.normalizeClassCode(bizStudent.getClassCode()));
        bizStudent.setStudentNo(StudentImportRules.normalizeStudentNo(bizStudent.getStudentNo()));
        String formattedClassCode = StringUtils.leftPad(bizStudent.getClassCode(), 2, '0');
        String formattedStudentNo = StringUtils.leftPad(bizStudent.getStudentNo(), 2, '0');
        String generatedUserName = bizStudent.getEntryYear() + schoolCode + formattedClassCode + formattedStudentNo;

        if (userMapper.checkUserNameUnique(generatedUserName) != null)
        {
            throw new ServiceException("生成登录账号 " + generatedUserName + " 已存在，请检查入学年份、班级和学号");
        }

        SysUser newUser = new SysUser();
        newUser.setDeptId(teacherDeptId);
        newUser.setUserName(generatedUserName);
        newUser.setNickName(bizStudent.getStudentName());
        newUser.setPassword(SecurityUtils.encryptPassword("123456"));
        newUser.setCreateBy(operName);
        userMapper.insertUser(newUser);

        SysUserRole ur = new SysUserRole();
        ur.setUserId(newUser.getUserId());
        ur.setRoleId(STUDENT_ROLE_ID);
        userRoleMapper.batchUserRole(Arrays.asList(ur));

        bizStudent.setUserId(newUser.getUserId());
        int result = bizStudentMapper.insertBizStudent(bizStudent);
        
        // 自动将该班级添加到教师的管理班级中
        autoAssignTeacherClass(loginUser.getUserId(), teacherDeptId, bizStudent.getEntryYear(), bizStudent.getClassCode());
        
        return result;
    }

    @Override
    @Transactional
    public int updateBizStudent(BizStudent bizStudent)
    {
        SysUser userUpdate = new SysUser();
        userUpdate.setUserId(bizStudent.getUserId());
        userUpdate.setNickName(bizStudent.getStudentName());
        userMapper.updateUser(userUpdate);

        return bizStudentMapper.updateBizStudent(bizStudent);
    }

    @Override
    @Transactional
    public int deleteBizStudentByClass(String entryYear, String classCode, Long deptId) {
        BizStudent query = new BizStudent();
        query.setEntryYear(entryYear);
        query.setClassCode(classCode);
        query.setDeptId(deptId);
        
        // 为了安全起见，非管理员只能删除自己校区的
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null && !loginUser.getUser().isAdmin()) {
            query.setDeptId(loginUser.getUser().getDeptId());
        }

        Long targetDeptId = query.getDeptId();

        List<BizStudent> students = bizStudentMapper.selectBizStudentList(query);
        if (students == null || students.isEmpty()) {
            recycleEmptyClass(targetDeptId, entryYear, classCode);
            return 0;
        }

        Long[] studentIds = new Long[students.size()];
        int i = 0;
        for (BizStudent student : students) {
            studentIds[i++] = student.getStudentId();
        }
        answerDeletionGuardService.assertStudentsDeletable(studentIds);
        for (BizStudent student : students) {
            if (student.getUserId() != null) {
                userMapper.deleteUserById(student.getUserId());
                userRoleMapper.deleteUserRoleByUserId(student.getUserId());
            }
        }
        int rows = bizStudentMapper.deleteBizStudentByStudentIds(studentIds);
        recycleEmptyClass(targetDeptId, entryYear, classCode);
        return rows;
    }

    @Override
    @Transactional
    public int deleteBizStudentByStudentIds(Long[] studentIds)
    {
        List<BizStudent> students = selectAuthorizedStudents(studentIds);
        answerDeletionGuardService.assertStudentsDeletable(studentIds);
        Set<String> affectedClasses = new LinkedHashSet<>();
        for (BizStudent student : students) {
            markAffectedClass(affectedClasses, student.getDeptId(), student.getEntryYear(), student.getClassCode());
            if (student.getUserId() != null) {
                userMapper.deleteUserById(student.getUserId());
                userRoleMapper.deleteUserRoleByUserId(student.getUserId());
            }
        }
        int rows = bizStudentMapper.deleteBizStudentByStudentIds(studentIds);
        recycleEmptyClasses(affectedClasses);
        return rows;
    }

    @Override
    @Transactional
    public int deleteBizStudentByStudentId(Long studentId)
    {
        BizStudent student = selectAuthorizedStudents(new Long[] { studentId }).get(0);
        answerDeletionGuardService.assertStudentsDeletable(new Long[] { studentId });
        if (student.getUserId() != null) {
            userMapper.deleteUserById(student.getUserId());
            userRoleMapper.deleteUserRoleByUserId(student.getUserId());
        }
        int rows = bizStudentMapper.deleteBizStudentByStudentId(studentId);
        recycleEmptyClass(student.getDeptId(), student.getEntryYear(), student.getClassCode());
        return rows;
    }

    @Override
    public StudentCorrectionPreview previewStudentCorrections(List<StudentCorrectionRow> rows)
    {
        LoginUser loginUser = requireOperator();
        SysDept school = deptMapper.selectDeptById(loginUser.getUser().getDeptId());
        if (school == null || StringUtils.isEmpty(school.getSchoolCode()))
        {
            throw new ServiceException("当前账号关联的学校缺少学校官方ID，无法生成纠错后的登录账号");
        }
        return buildCorrectionPreview(rows, loginUser, school.getSchoolCode());
    }

    @Override
    @Transactional
    public StudentCorrectionPreview applyStudentCorrections(List<StudentCorrectionRow> rows, String operName)
    {
        LoginUser loginUser = requireOperator();
        Long deptId = loginUser.getUser().getDeptId();
        SysDept school = deptMapper.selectDeptById(deptId);
        if (school == null || StringUtils.isEmpty(school.getSchoolCode()))
        {
            throw new ServiceException("当前账号关联的学校缺少学校官方ID，无法生成纠错后的登录账号");
        }
        try (StudentImportLock ignored = acquireStudentImportLock(deptId))
        {
            // 确认提交时重新校验，防止预览后账号或学生资料被别人改动。
            StudentCorrectionPreview preview = buildCorrectionPreview(rows, loginUser, school.getSchoolCode());
            if (preview.getInvalidCount() > 0)
            {
                throw new ServiceException("纠错表中仍有 " + preview.getInvalidCount() + " 条错误，请按预览提示修改后重新上传");
            }

            Set<String> oldClasses = new LinkedHashSet<>();
            Set<Long> changedUserIds = new LinkedHashSet<>();
            for (StudentCorrectionRow row : preview.getRows())
            {
                if (!Boolean.TRUE.equals(row.getChanged()))
                {
                    continue;
                }
                BizStudent current = bizStudentMapper.selectBizStudentByStudentId(row.getStudentId());
                if (current == null || current.getUserId() == null)
                {
                    throw new ServiceException("学生永久编号 " + row.getStudentId() + " 已不存在，纠错已回滚");
                }
                markAffectedClass(oldClasses, current.getDeptId(), current.getEntryYear(), current.getClassCode());
                int userRows = userMapper.updateStudentAccountIdentity(current.getUserId(), row.getTargetUserName(),
                        row.getStudentName(), operName);
                BizStudent update = new BizStudent();
                update.setStudentId(row.getStudentId());
                update.setStudentNo(row.getStudentNo());
                update.setEntryYear(row.getEntryYear());
                update.setClassCode(row.getClassCode());
                update.setRemark(row.getRemark());
                int studentRows = bizStudentMapper.updateBizStudent(update);
                if (userRows != 1 || studentRows != 1)
                {
                    throw new ServiceException("学生永久编号 " + row.getStudentId() + " 更新失败，纠错已回滚");
                }
                changedUserIds.add(current.getUserId());
                autoAssignTeacherClass(loginUser.getUserId(), current.getDeptId(), row.getEntryYear(), row.getClassCode());
            }
            recycleEmptyClasses(oldClasses);
            evictStudentSessionsAfterCommit(changedUserIds);
            return preview;
        }
    }

    @Override
    @Transactional
    public int updateStudentStatus(Long[] studentIds, String status)
    {
        if (!"0".equals(status) && !"1".equals(status))
        {
            throw new ServiceException("学生账号状态只能是正常或停用");
        }
        LoginUser loginUser = requireOperator();
        List<BizStudent> students = selectAuthorizedStudents(studentIds);
        Set<String> affectedClasses = new LinkedHashSet<>();
        Set<Long> userIds = new LinkedHashSet<>();
        int updated = 0;
        for (BizStudent student : students)
        {
            SysUser user = new SysUser();
            user.setUserId(student.getUserId());
            user.setStatus(status);
            updated += userService.updateUserStatus(user);
            userIds.add(student.getUserId());
            markAffectedClass(affectedClasses, student.getDeptId(), student.getEntryYear(), student.getClassCode());
            if ("0".equals(status))
            {
                autoAssignTeacherClass(loginUser.getUserId(), student.getDeptId(), student.getEntryYear(), student.getClassCode());
            }
        }
        recycleEmptyClasses(affectedClasses);
        evictStudentSessionsAfterCommit(userIds);
        return updated;
    }

    @Override
    @Transactional
    public StudentImportResult importStudent(List<BizStudent> studentList, String operName)
    {
        long serviceStartedAt = System.currentTimeMillis();
        if (StringUtils.isNull(studentList) || studentList.isEmpty())
        {
            throw new ServiceException("导入学生数据不能为空！");
        }

        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long teacherDeptId = loginUser.getUser().getDeptId();
        if (StringUtils.isNull(teacherDeptId))
        {
            throw new ServiceException("操作的教师账号没有关联学校，无法导入学生！");
        }
        SysDept school = deptMapper.selectDeptById(teacherDeptId);
        if (school == null || StringUtils.isEmpty(school.getSchoolCode()))
        {
            throw new ServiceException("教师关联的学校信息不完整，缺少学校官方ID (school_code)");
        }

        try (StudentImportLock ignored = acquireStudentImportLock(teacherDeptId))
        {
            long validationStartedAt = System.currentTimeMillis();
            List<String> failureDetails = new ArrayList<>();
            List<PreparedStudent> preparedStudents = new ArrayList<>();
            Set<String> fileUserNames = new LinkedHashSet<>();
            int rowIndex = 0;
            for (BizStudent student : studentList)
            {
                rowIndex++;
                String validationError = prepareStudent(student, rowIndex, school.getSchoolCode(),
                        fileUserNames, preparedStudents);
                if (validationError != null)
                {
                    failureDetails.add(validationError);
                }
            }

            List<String> candidateUserNames = new ArrayList<>();
            for (PreparedStudent prepared : preparedStudents)
            {
                candidateUserNames.add(prepared.userName);
            }
            Set<String> existingUserNames = new LinkedHashSet<>();
            for (SysUser existing : selectActiveUsersByUserNames(candidateUserNames))
            {
                if (StringUtils.isNotEmpty(existing.getUserName()))
                {
                    existingUserNames.add(existing.getUserName());
                }
            }
            List<PreparedStudent> writableStudents = new ArrayList<>();
            for (PreparedStudent prepared : preparedStudents)
            {
                if (existingUserNames.contains(prepared.userName))
                {
                    failureDetails.add(failureDetail(prepared.rowIndex, prepared.student,
                            "生成的登录账号 " + prepared.userName + " 已存在"));
                }
                else
                {
                    writableStudents.add(prepared);
                }
            }
            long validationDurationMs = System.currentTimeMillis() - validationStartedAt;

            long passwordDurationMs = 0L;
            long databaseDurationMs = 0L;
            if (!writableStudents.isEmpty())
            {
                long passwordStartedAt = System.currentTimeMillis();
                // 同一导入批次使用同一个初始口令，哈希只需计算一次；登录后仍可按现有流程修改密码。
                String initialPassword = SecurityUtils.encryptPassword("123456");
                passwordDurationMs = System.currentTimeMillis() - passwordStartedAt;

                long databaseStartedAt = System.currentTimeMillis();
                persistImportedStudents(writableStudents, teacherDeptId, loginUser.getUserId(), operName,
                        initialPassword);
                databaseDurationMs = System.currentTimeMillis() - databaseStartedAt;
            }

            StudentImportResult result = new StudentImportResult();
            result.setTotalCount(studentList.size());
            result.setSuccessCount(writableStudents.size());
            result.setFailureCount(failureDetails.size());
            result.setValidationDurationMs(validationDurationMs);
            result.setPasswordDurationMs(passwordDurationMs);
            result.setDatabaseDurationMs(databaseDurationMs);
            result.setTotalDurationMs(System.currentTimeMillis() - serviceStartedAt);
            result.setMessage(buildImportMessage(writableStudents, failureDetails, result));
            log.info("学生导入完成 deptId={}, total={}, success={}, failure={}, validationMs={}, passwordMs={}, databaseMs={}, totalMs={}",
                    teacherDeptId, result.getTotalCount(), result.getSuccessCount(), result.getFailureCount(),
                    validationDurationMs, passwordDurationMs, databaseDurationMs, result.getTotalDurationMs());
            return result;
        }
    }

    private StudentCorrectionPreview buildCorrectionPreview(List<StudentCorrectionRow> rows, LoginUser loginUser,
            String schoolCode)
    {
        if (rows == null || rows.isEmpty())
        {
            throw new ServiceException("纠错表不能为空");
        }
        if (rows.size() > 2000)
        {
            throw new ServiceException("单次最多纠错 2000 名学生，请拆分后再操作");
        }

        Set<Long> requestedIds = new LinkedHashSet<>();
        for (StudentCorrectionRow row : rows)
        {
            if (row != null && row.getStudentId() != null)
            {
                requestedIds.add(row.getStudentId());
            }
        }
        Long teacherUserId = loginUser.getUser().isAdmin() ? null : loginUser.getUserId();
        // 纠错账号由学校编码生成，即使是管理员也只能处理当前所选学校，避免串校生成账号。
        Long deptId = loginUser.getUser().getDeptId();
        Map<Long, BizStudent> currentById = new LinkedHashMap<>();
        if (!requestedIds.isEmpty())
        {
            for (BizStudent student : bizStudentMapper.selectBizStudentsByIds(new ArrayList<>(requestedIds),
                    teacherUserId, deptId))
            {
                currentById.put(student.getStudentId(), student);
            }
        }

        Set<Long> seenIds = new LinkedHashSet<>();
        Set<String> targetNames = new LinkedHashSet<>();
        List<String> candidateNames = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++)
        {
            StudentCorrectionRow row = rows.get(i);
            if (row == null)
            {
                continue;
            }
            row.setRowNumber(i + 2);
            row.setValid(Boolean.TRUE);
            row.setMessage("校验通过");
            if (row.getStudentId() == null)
            {
                invalidateCorrection(row, "学生永久编号不能为空");
                continue;
            }
            if (!seenIds.add(row.getStudentId()))
            {
                invalidateCorrection(row, "同一个学生永久编号在 Excel 中重复");
                continue;
            }
            BizStudent current = currentById.get(row.getStudentId());
            if (current == null)
            {
                invalidateCorrection(row, "学生不存在，或不属于当前教师可管理的班级");
                continue;
            }
            fillCurrentCorrectionFields(row, current);
            if (StringUtils.isEmpty(row.getOriginalUserName())
                    || !row.getOriginalUserName().trim().equals(current.getUserName()))
            {
                invalidateCorrection(row, "原登录账号与系统当前记录不一致，请重新下载最新纠错表");
                continue;
            }
            try
            {
                row.setStudentName(StringUtils.trim(row.getStudentName()));
                if (StringUtils.isEmpty(row.getStudentName()))
                {
                    throw new ServiceException("真实姓名不能为空");
                }
                row.setEntryYear(StudentImportRules.normalizeEntryYear(row.getEntryYear()));
                row.setClassCode(StudentImportRules.normalizeClassCode(row.getClassCode()));
                row.setStudentNo(StudentImportRules.normalizeStudentNo(row.getStudentNo()));
                String targetUserName = row.getEntryYear() + schoolCode
                        + StringUtils.leftPad(row.getClassCode(), 2, '0')
                        + StringUtils.leftPad(row.getStudentNo(), 2, '0');
                row.setTargetUserName(targetUserName);
                candidateNames.add(targetUserName);
                if (!targetNames.add(targetUserName))
                {
                    invalidateCorrection(row, "纠错表内生成了重复的新登录账号 " + targetUserName);
                }
            }
            catch (ServiceException ex)
            {
                invalidateCorrection(row, ex.getMessage());
            }
        }

        Map<String, SysUser> existingByName = new LinkedHashMap<>();
        for (SysUser user : selectActiveUsersByUserNames(candidateNames))
        {
            existingByName.put(user.getUserName(), user);
        }
        for (StudentCorrectionRow row : rows)
        {
            if (row == null || !Boolean.TRUE.equals(row.getValid()) || StringUtils.isEmpty(row.getTargetUserName()))
            {
                continue;
            }
            BizStudent current = currentById.get(row.getStudentId());
            SysUser conflict = existingByName.get(row.getTargetUserName());
            if (conflict != null && current != null && !Objects.equals(conflict.getUserId(), current.getUserId()))
            {
                invalidateCorrection(row, "目标登录账号 " + row.getTargetUserName() + " 已被其他用户占用");
                continue;
            }
            row.setChanged(current != null && (!Objects.equals(row.getTargetUserName(), current.getUserName())
                    || !Objects.equals(row.getStudentName(), current.getStudentName())
                    || !Objects.equals(row.getEntryYear(), current.getEntryYear())
                    || !Objects.equals(row.getClassCode(), current.getClassCode())
                    || !Objects.equals(row.getStudentNo(), current.getStudentNo())
                    || !Objects.equals(row.getRemark(), current.getRemark())));
            row.setMessage(Boolean.TRUE.equals(row.getChanged()) ? "可纠错" : "资料没有变化");
        }

        StudentCorrectionPreview preview = new StudentCorrectionPreview();
        preview.setTotalCount(rows.size());
        preview.setRows(rows);
        for (StudentCorrectionRow row : rows)
        {
            if (row != null && Boolean.TRUE.equals(row.getValid()))
            {
                preview.setValidCount(preview.getValidCount() + 1);
                if (Boolean.TRUE.equals(row.getChanged()))
                {
                    preview.setChangedCount(preview.getChangedCount() + 1);
                }
            }
            else
            {
                preview.setInvalidCount(preview.getInvalidCount() + 1);
            }
        }
        return preview;
    }

    private void fillCurrentCorrectionFields(StudentCorrectionRow row, BizStudent current)
    {
        row.setCurrentUserName(current.getUserName());
        row.setCurrentStudentName(current.getStudentName());
        row.setCurrentEntryYear(current.getEntryYear());
        row.setCurrentClassCode(current.getClassCode());
        row.setCurrentStudentNo(current.getStudentNo());
    }

    private void invalidateCorrection(StudentCorrectionRow row, String message)
    {
        row.setValid(Boolean.FALSE);
        row.setChanged(Boolean.FALSE);
        row.setMessage(message);
    }

    private LoginUser requireOperator()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || loginUser.getUser() == null || loginUser.getUser().getDeptId() == null)
        {
            throw new ServiceException("当前账号没有关联学校，无法操作学生");
        }
        return loginUser;
    }

    private List<BizStudent> selectAuthorizedStudents(Long[] studentIds)
    {
        LoginUser loginUser = requireOperator();
        if (studentIds == null || studentIds.length == 0)
        {
            throw new ServiceException("请选择学生");
        }
        Set<Long> uniqueIds = new LinkedHashSet<>();
        for (Long studentId : studentIds)
        {
            if (studentId != null)
            {
                uniqueIds.add(studentId);
            }
        }
        if (uniqueIds.isEmpty())
        {
            throw new ServiceException("请选择有效学生");
        }
        Long teacherUserId = loginUser.getUser().isAdmin() ? null : loginUser.getUserId();
        Long deptId = loginUser.getUser().isAdmin() ? null : loginUser.getUser().getDeptId();
        List<BizStudent> students = bizStudentMapper.selectBizStudentsByIds(new ArrayList<>(uniqueIds),
                teacherUserId, deptId);
        if (students.size() != uniqueIds.size())
        {
            throw new ServiceException("部分学生不存在，或不属于当前账号可管理的班级");
        }
        return students;
    }

    private String prepareStudent(BizStudent student, int rowIndex, String schoolCode, Set<String> fileUserNames,
            List<PreparedStudent> preparedStudents)
    {
        if (student == null)
        {
            return "第 " + rowIndex + " 行：学生数据为空";
        }
        if (StringUtils.isEmpty(student.getStudentName()))
        {
            return failureDetail(rowIndex, student, "学生姓名不能为空");
        }
        if (StringUtils.isEmpty(student.getEntryYear()))
        {
            return failureDetail(rowIndex, student, "入学年份不能为空");
        }
        if (StringUtils.isEmpty(student.getClassCode()))
        {
            return failureDetail(rowIndex, student, "班级不能为空");
        }
        if (StringUtils.isEmpty(student.getStudentNo()))
        {
            return failureDetail(rowIndex, student, "学号不能为空");
        }
        try
        {
            student.setEntryYear(StudentImportRules.normalizeEntryYear(student.getEntryYear()));
            student.setClassCode(StudentImportRules.normalizeClassCode(student.getClassCode()));
            student.setStudentNo(StudentImportRules.normalizeStudentNo(student.getStudentNo()));
        }
        catch (ServiceException ex)
        {
            return failureDetail(rowIndex, student, ex.getMessage());
        }

        String formattedClassCode = StringUtils.leftPad(student.getClassCode(), 2, '0');
        String formattedStudentNo = StringUtils.leftPad(student.getStudentNo(), 2, '0');
        String generatedUserName = student.getEntryYear() + schoolCode + formattedClassCode + formattedStudentNo;
        if (!fileUserNames.add(generatedUserName))
        {
            return failureDetail(rowIndex, student, "Excel 内登录账号 " + generatedUserName + " 重复");
        }
        preparedStudents.add(new PreparedStudent(rowIndex, student, generatedUserName));
        return null;
    }

    private List<SysUser> selectActiveUsersByUserNames(List<String> userNames)
    {
        List<SysUser> users = new ArrayList<>();
        for (int start = 0; start < userNames.size(); start += IMPORT_BATCH_SIZE)
        {
            int end = Math.min(start + IMPORT_BATCH_SIZE, userNames.size());
            users.addAll(userMapper.selectActiveUsersByUserNames(userNames.subList(start, end)));
        }
        return users;
    }

    private void persistImportedStudents(List<PreparedStudent> preparedStudents, Long deptId, Long teacherUserId,
            String operName, String initialPassword)
    {
        List<SysUser> users = new ArrayList<>();
        for (PreparedStudent prepared : preparedStudents)
        {
            SysUser user = new SysUser();
            user.setDeptId(deptId);
            user.setUserName(prepared.userName);
            user.setNickName(prepared.student.getStudentName());
            user.setPassword(initialPassword);
            user.setCreateBy(operName);
            users.add(user);
        }
        for (int start = 0; start < users.size(); start += IMPORT_BATCH_SIZE)
        {
            int end = Math.min(start + IMPORT_BATCH_SIZE, users.size());
            List<SysUser> batch = users.subList(start, end);
            int inserted = userMapper.batchInsertUsers(batch);
            if (inserted != batch.size())
            {
                throw new ServiceException("批量创建学生账号数量不一致，导入已回滚");
            }
        }

        List<String> userNames = new ArrayList<>();
        for (PreparedStudent prepared : preparedStudents)
        {
            userNames.add(prepared.userName);
        }
        Map<String, SysUser> persistedByName = new LinkedHashMap<>();
        for (SysUser persisted : selectActiveUsersByUserNames(userNames))
        {
            if (persistedByName.put(persisted.getUserName(), persisted) != null)
            {
                throw new ServiceException("检测到重复学生账号，导入已回滚");
            }
        }
        if (persistedByName.size() != preparedStudents.size())
        {
            throw new ServiceException("创建后的学生账号回查数量不一致，导入已回滚");
        }

        List<SysUserRole> roles = new ArrayList<>();
        List<BizStudent> students = new ArrayList<>();
        for (PreparedStudent prepared : preparedStudents)
        {
            SysUser user = persistedByName.get(prepared.userName);
            if (user == null || user.getUserId() == null)
            {
                throw new ServiceException("学生账号ID回查失败，导入已回滚");
            }
            SysUserRole role = new SysUserRole();
            role.setUserId(user.getUserId());
            role.setRoleId(STUDENT_ROLE_ID);
            roles.add(role);
            prepared.student.setUserId(user.getUserId());
            students.add(prepared.student);
        }
        for (int start = 0; start < roles.size(); start += IMPORT_BATCH_SIZE)
        {
            int end = Math.min(start + IMPORT_BATCH_SIZE, roles.size());
            List<SysUserRole> batch = roles.subList(start, end);
            int inserted = userRoleMapper.batchUserRole(batch);
            if (inserted != batch.size())
            {
                throw new ServiceException("批量分配学生角色数量不一致，导入已回滚");
            }
        }
        for (int start = 0; start < students.size(); start += IMPORT_BATCH_SIZE)
        {
            int end = Math.min(start + IMPORT_BATCH_SIZE, students.size());
            List<BizStudent> batch = students.subList(start, end);
            int inserted = bizStudentMapper.batchInsertBizStudents(batch);
            if (inserted != batch.size())
            {
                throw new ServiceException("批量创建学生档案数量不一致，导入已回滚");
            }
        }
        ensureTeacherClasses(teacherUserId, deptId, preparedStudents);
    }

    private void ensureTeacherClasses(Long teacherUserId, Long deptId, List<PreparedStudent> preparedStudents)
    {
        BizTeacherClass query = new BizTeacherClass();
        query.setUserId(teacherUserId);
        query.setDeptId(deptId);
        Set<String> existingKeys = new LinkedHashSet<>();
        List<BizTeacherClass> existingClasses = teacherClassService.selectBizTeacherClassList(query);
        if (existingClasses != null)
        {
            for (BizTeacherClass existing : existingClasses)
            {
                existingKeys.add(classKey(existing.getEntryYear(), existing.getClassCode()));
            }
        }
        Set<String> insertedKeys = new LinkedHashSet<>();
        for (PreparedStudent prepared : preparedStudents)
        {
            String key = classKey(prepared.student.getEntryYear(), prepared.student.getClassCode());
            if (existingKeys.contains(key) || !insertedKeys.add(key))
            {
                continue;
            }
            BizTeacherClass teacherClass = new BizTeacherClass();
            teacherClass.setUserId(teacherUserId);
            teacherClass.setDeptId(deptId);
            teacherClass.setEntryYear(prepared.student.getEntryYear());
            teacherClass.setClassCode(prepared.student.getClassCode());
            teacherClassService.insertBizTeacherClass(teacherClass);
        }
    }

    private String buildImportMessage(List<PreparedStudent> successes, List<String> failures,
            StudentImportResult result)
    {
        StringBuilder message = new StringBuilder();
        if (!failures.isEmpty())
        {
            message.append("<b style='color:#F56C6C'>导入失败 ").append(failures.size()).append(" 条：</b>");
            for (int i = 0; i < Math.min(failures.size(), IMPORT_DETAIL_LIMIT); i++)
            {
                message.append("<br/>").append(i + 1).append("、").append(failures.get(i));
            }
            appendOmittedCount(message, failures.size());
        }
        if (!successes.isEmpty())
        {
            if (!failures.isEmpty())
            {
                message.append("<br/><br/>");
            }
            message.append("<b style='color:#67C23A'>导入成功 ").append(successes.size()).append(" 条：</b>");
            for (int i = 0; i < Math.min(successes.size(), IMPORT_DETAIL_LIMIT); i++)
            {
                PreparedStudent prepared = successes.get(i);
                message.append("<br/>").append(i + 1).append("、学生 ")
                        .append(escapeHtml(prepared.student.getStudentName())).append("，登录账号为 ")
                        .append(prepared.userName);
            }
            appendOmittedCount(message, successes.size());
        }
        message.append("<br/><br/><span style='color:#909399'>服务端处理耗时 ")
                .append(result.getTotalDurationMs()).append(" ms（校验 ")
                .append(result.getValidationDurationMs()).append(" ms，密码处理 ")
                .append(result.getPasswordDurationMs()).append(" ms，数据库 ")
                .append(result.getDatabaseDurationMs()).append(" ms）</span>");
        return message.toString();
    }

    private void appendOmittedCount(StringBuilder message, int total)
    {
        if (total > IMPORT_DETAIL_LIMIT)
        {
            message.append("<br/>……其余 ").append(total - IMPORT_DETAIL_LIMIT)
                    .append(" 条已计入汇总，不在弹窗中逐条展开");
        }
    }

    private String failureDetail(int rowIndex, BizStudent student, String reason)
    {
        String studentName = student == null || StringUtils.isEmpty(student.getStudentName())
                ? "未命名学生" : escapeHtml(student.getStudentName());
        return "第 " + rowIndex + " 行学生 " + studentName + "：" + reason;
    }

    private String escapeHtml(String value)
    {
        if (value == null)
        {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String classKey(String entryYear, String classCode)
    {
        return StringUtils.trimToEmpty(entryYear) + "|" + StringUtils.trimToEmpty(classCode);
    }

    private StudentImportLock acquireStudentImportLock(Long deptId)
    {
        String key = STUDENT_IMPORT_LOCK_PREFIX + deptId;
        String token = UUID.randomUUID().toString();
        try
        {
            Boolean acquired = redisCache.setCacheObjectIfAbsent(key, token, 10, TimeUnit.MINUTES);
            if (!Boolean.TRUE.equals(acquired))
            {
                throw new ServiceException("本校已有学生导入正在处理，请等待完成后再试");
            }
        }
        catch (ServiceException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new ServiceException("学生导入防重服务暂不可用，请稍后重试");
        }
        StudentImportLock lock = new StudentImportLock(key, token);
        if (TransactionSynchronizationManager.isSynchronizationActive())
        {
            lock.deferRelease = true;
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()
            {
                @Override
                public void afterCompletion(int status)
                {
                    lock.releaseNow();
                }
            });
        }
        return lock;
    }

    private final class StudentImportLock implements AutoCloseable
    {
        private final String key;

        private final String token;

        private boolean deferRelease;

        private boolean released;

        private StudentImportLock(String key, String token)
        {
            this.key = key;
            this.token = token;
        }

        @Override
        public void close()
        {
            if (!deferRelease)
            {
                releaseNow();
            }
        }

        private void releaseNow()
        {
            if (released)
            {
                return;
            }
            released = true;
            try
            {
                redisCache.deleteObjectIfValueMatches(key, token);
            }
            catch (Exception ex)
            {
                log.warn("学生导入锁释放失败 key={}: {}", key, ex.getMessage());
            }
        }
    }

    private static final class PreparedStudent
    {
        private final int rowIndex;

        private final BizStudent student;

        private final String userName;

        private PreparedStudent(int rowIndex, BizStudent student, String userName)
        {
            this.rowIndex = rowIndex;
            this.student = student;
            this.userName = userName;
        }
    }

    @Override
    public int resetStudentPwd(Long[] userIds) {
        int successCount = 0;
        String password = SecurityUtils.encryptPassword("123456");
        LoginUser caller = SecurityUtils.getLoginUser();
        // 越权防护基准：非管理员只能重置本校学生；学生身份以 biz_student 学籍事实为准——
        // 平台从未批量维护学生的 sys_user_role（全库 role_id=101 记录为 0），不能依赖角色判断。
        boolean callerIsAdmin = caller != null && caller.getUser() != null && caller.getUser().isAdmin();
        Long callerDeptId = caller != null && caller.getUser() != null ? caller.getUser().getDeptId() : null;
        for(Long userId : userIds) {
            // 查询用户信息获取用户名
            SysUser existingUser = userMapper.selectUserById(userId);
            if (existingUser == null) {
                continue;
            }

            if (!callerIsAdmin && (callerDeptId == null || !callerDeptId.equals(existingUser.getDeptId()))) {
                log.warn("拒绝跨校重置密码: 操作人dept={}, 目标userId={}, 目标dept={}",
                        callerDeptId, userId, existingUser.getDeptId());
                continue;
            }
            BizStudent studentRecord = bizStudentMapper.selectBizStudentByUserId(userId);
            if (studentRecord == null) {
                log.warn("拒绝重置非学生账号密码: userId={}", userId);
                continue;
            }

            // 重置密码
            SysUser user = new SysUser();
            user.setUserId(userId);
            user.setPassword(password);
            successCount += userService.resetPwd(user);

            // 清除登录失败次数缓存（解锁账号）
            String cacheKey = CacheConstants.PWD_ERR_CNT_KEY + existingUser.getUserName();
            if (redisCache.hasKey(cacheKey)) {
                redisCache.deleteObject(cacheKey);
                log.info("已解锁学生账号: {}", existingUser.getUserName());
            }
        }
        return successCount;
    }

    /**
     * 自动将班级添加到教师的管理班级中（如果尚未管理）
     */
    private void autoAssignTeacherClass(Long userId, Long deptId, String entryYear, String classCode) {
        try {
            // 检查是否已存在关联
            BizTeacherClass query = new BizTeacherClass();
            query.setUserId(userId);
            query.setEntryYear(entryYear);
            query.setClassCode(classCode);
            List<BizTeacherClass> existing = teacherClassService.selectBizTeacherClassList(query);
            
            // 如果不存在，自动添加
            if (existing == null || existing.isEmpty()) {
                BizTeacherClass tc = new BizTeacherClass();
                tc.setUserId(userId);
                tc.setDeptId(deptId);
                tc.setEntryYear(entryYear);
                tc.setClassCode(classCode);
                teacherClassService.insertBizTeacherClass(tc);
                log.info("自动添加教师管理班级: userId={}, entryYear={}, classCode={}", userId, entryYear, classCode);
            }
        } catch (Exception e) {
            log.warn("自动添加教师管理班级失败: {}", e.getMessage());
        }
    }
    /**
     * 班级删空后同步回收教师管理关系，避免保留 0 人空班。
     */
    private void recycleEmptyClass(Long deptId, String entryYear, String classCode) {
        if (deptId == null || StringUtils.isEmpty(entryYear) || StringUtils.isEmpty(classCode)) {
            return;
        }

        int remainingCount = bizStudentMapper.countByDeptIdAndClass(deptId, entryYear, classCode);
        if (remainingCount > 0) {
            return;
        }

        int removed = teacherClassService.deleteByDeptIdAndClass(deptId, entryYear, classCode);
        if (removed > 0) {
            log.info("班级已清空，自动清理教师管理关系: deptId={}, entryYear={}, classCode={}, removed={}",
                    deptId, entryYear, classCode, removed);
        }
    }

    private void recycleEmptyClasses(Set<String> affectedClasses) {
        for (String classKey : affectedClasses) {
            String[] parts = classKey.split("\\|", 3);
            recycleEmptyClass(Long.valueOf(parts[0]), parts[1], parts[2]);
        }
    }

    private void evictStudentSessionsAfterCommit(Set<Long> userIds)
    {
        if (userIds == null || userIds.isEmpty())
        {
            return;
        }
        Runnable evict = () -> {
            try
            {
                Collection<String> keys = redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
                for (String key : keys)
                {
                    LoginUser cached = redisCache.getCacheObject(key);
                    if (cached != null && userIds.contains(cached.getUserId()))
                    {
                        redisCache.deleteObject(key);
                    }
                }
            }
            catch (Exception ex)
            {
                // 数据库状态已经正确，清会话失败只记日志，用户下一次鉴权仍会受账号状态限制。
                log.warn("清理学生旧登录会话失败 userIds={}: {}", userIds, ex.getMessage());
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive())
        {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()
            {
                @Override
                public void afterCommit()
                {
                    evict.run();
                }
            });
        }
        else
        {
            evict.run();
        }
    }

    private void markAffectedClass(Set<String> affectedClasses, Long deptId, String entryYear, String classCode) {
        if (deptId == null || StringUtils.isEmpty(entryYear) || StringUtils.isEmpty(classCode)) {
            return;
        }
        affectedClasses.add(deptId + "|" + entryYear + "|" + classCode);
    }
}
