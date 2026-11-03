package com.ruoyi.business.service;

import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.mapper.*;
import com.ruoyi.common.core.domain.entity.*;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.mapper.SysDeptMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExtensionClassServiceTest {
    ExtensionClassService service;
    BizExtensionClassMapper mapper;
    BizStudentMapper students;
    SysDeptMapper depts;
    Map<String,Object> group;
    static Map<String,Object> row(Object... values) {
        Map<String,Object> row=new HashMap<>();
        for(int i=0;i<values.length;i+=2) row.put((String)values[i],values[i+1]);
        return row;
    }
    void login(Long userId,Long deptId,String role) {
        SysUser user=new SysUser(); user.setUserId(userId); user.setDeptId(deptId); user.setUserName("test");
        SysRole r=new SysRole(); r.setRoleKey(role); user.setRoles(Collections.singletonList(r));
        LoginUser login=new LoginUser(userId,deptId,user,Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(login,null,Collections.emptyList()));
    }
    @BeforeEach void setup() {
        service=new ExtensionClassService(); mapper=mock(BizExtensionClassMapper.class); students=mock(BizStudentMapper.class);
        ReflectionTestUtils.setField(service,"mapper",mapper); ReflectionTestUtils.setField(service,"studentMapper",students);
        depts=mock(SysDeptMapper.class); ReflectionTestUtils.setField(service,"deptMapper",depts);
        group=row("extension_class_id",3L,"dept_id",10L,"owner_user_id",20L,"status","ACTIVE","legacy_flag",0);
        when(mapper.find(3L)).thenReturn(group);
        login(20L,10L,"teacher");
    }
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }
    @Test void ownerCanManage() { assertSame(group,service.authorized(3L,true)); }
    @Test void otherTeacherRejected() { login(21L,10L,"teacher"); assertThrows(ServiceException.class,()->service.authorized(3L,true)); }
    @Test void differentSchoolAdminRejected() { login(1L,11L,"admin"); assertThrows(ServiceException.class,()->service.authorized(3L,true)); }
    @Test void schoolAdminCanManage() { login(1L,10L,"admin"); assertSame(group,service.authorized(3L,true)); }
    @Test void researcherRejected() { login(22L,10L,"researcher"); assertThrows(ServiceException.class,()->service.classes()); }
    void loginRoles(Long userId,Long deptId,String... roles) {
        SysUser user=new SysUser(); user.setUserId(userId); user.setDeptId(deptId); user.setUserName("test");
        List<SysRole> list=new ArrayList<>();
        for (String role : roles) { SysRole r=new SysRole(); r.setRoleKey(role); list.add(r); }
        user.setRoles(list);
        LoginUser login=new LoginUser(userId,deptId,user,Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(login,null,Collections.emptyList()));
    }
    @Test void dualRoleTeacherCanManageOwnClass() {
        loginRoles(20L,10L,"teacher","researcher");
        assertSame(group,service.authorized(3L,true));
    }
    @Test void dualRoleTeacherCanCreateOwnClassEvenIfExcludedFromCandidates() {
        loginRoles(20L,10L,"teacher","researcher");
        when(mapper.teacher(10L,20L)).thenReturn(0);   // 候选排除仍然生效：双角色不在负责人下拉里
        when(mapper.insert(anyMap())).thenAnswer(inv -> {
            ((Map<String,Object>) inv.getArgument(0)).put("extensionClassId",3L);
            return 1;
        });
        Map<String,Object> request=new HashMap<>(); request.put("className","周五拓展班");
        assertSame(group,service.create(request));
        verify(mapper).insert(anyMap());
    }
    @Test void adminCannotPickDualRoleTeacherAsOwner() {
        login(1L,10L,"admin");
        when(mapper.teacher(10L,30L)).thenReturn(0);
        Map<String,Object> request=new HashMap<>(); request.put("className","x"); request.put("ownerUserId",30L);
        assertThrows(ServiceException.class,()->service.create(request));
        verify(mapper,never()).insert(anyMap());
    }
    @Test void archivedIsReadOnly() { group.put("status","ARCHIVED"); assertSame(group,service.authorized(3L,false)); assertThrows(ServiceException.class,()->service.authorized(3L,true)); }
    @Test void legacyIsReadOnlyEvenIfStatusInvalidlyActive() { group.put("legacy_flag",1); assertThrows(ServiceException.class,()->service.authorized(3L,true)); }
    @Test void archiveReleasesMembersButNeverDeletesLessonSnapshots() {
        when(mapper.members(3L)).thenReturn(Collections.singletonList(row("student_id",9L)));
        service.archive(3L); verify(mapper).resetSpace(9L); verify(mapper).archive(3L); verify(mapper).clearMembers(3L);
    }
    @Test void crossSchoolStudentIsRejectedBeforeInsert() {
        when(mapper.lockStudent(9L)).thenReturn(row("dept_id",11L,"status","0","del_flag","0"));
        assertThrows(ServiceException.class,()->service.add(3L,Collections.singletonList(9L)));
        verify(mapper,never()).addMember(anyMap());
    }
    @Test void emptyMembershipBatchRejected() { assertThrows(ServiceException.class,()->service.add(3L,Collections.emptyList())); }
    @Test void newMemberGetsPublishedPlanRosterButNotExistingLessonSnapshot() {
        SysDept dept=new SysDept(); dept.setSchoolType("1"); when(depts.selectDeptById(10L)).thenReturn(dept);
        when(mapper.lockStudent(9L)).thenReturn(row("dept_id",10L,"status","0","del_flag","0","entry_year",String.valueOf(java.time.LocalDate.now().getYear()),"class_code","1"));
        when(mapper.carrier(anyLong(),anyString(),anyString())).thenReturn(null);
        when(mapper.membership(9L)).thenReturn(null);
        service.add(3L,Collections.singletonList(9L));
        verify(mapper).joinPublishedPlans(3L,9L);
        verify(mapper,never()).snapshot(anyLong(),anyLong());
    }
    @Test void extensionCheckinRequiresCurrentLessonAndFrozenRoster() {
        BizStudent student=new BizStudent(); student.setStudentId(9L); student.setDeptId(10L);
        when(mapper.lesson(8L)).thenReturn(row("lesson_scope","extension","extension_class_id",3L));
        when(mapper.lockMembership(9L)).thenReturn(group);
        when(mapper.currentLessonForUpdate(3L)).thenReturn(row("lesson_id",8L));
        when(mapper.inSnapshot(8L,9L)).thenReturn(1);
        assertTrue(service.extensionCheckin(student,8L));
        when(mapper.inSnapshot(8L,9L)).thenReturn(0);
        assertThrows(ServiceException.class,()->service.extensionCheckin(student,8L));
    }
    @Test void removedMemberCannotCheckinEvenWithSnapshot() {
        BizStudent student=new BizStudent(); student.setStudentId(9L); student.setDeptId(10L);
        when(mapper.lesson(8L)).thenReturn(row("lesson_scope","extension","extension_class_id",3L));
        when(mapper.currentLessonForUpdate(3L)).thenReturn(row("lesson_id",8L));
        when(mapper.inSnapshot(8L,9L)).thenReturn(1);
        assertThrows(ServiceException.class,()->service.extensionCheckin(student,8L));
    }
    @Test void replacedLessonCannotCheckin() {
        BizStudent student=new BizStudent(); student.setStudentId(9L); student.setDeptId(10L);
        when(mapper.lesson(8L)).thenReturn(row("lesson_scope","extension","extension_class_id",3L));
        when(mapper.lockMembership(9L)).thenReturn(group);
        when(mapper.currentLessonForUpdate(3L)).thenReturn(row("lesson_id",12L));
        assertThrows(ServiceException.class,()->service.extensionCheckin(student,8L));
    }
    @Test void legacyCannotCheckin() {
        when(mapper.lesson(8L)).thenReturn(row("lesson_scope","legacy"));
        assertThrows(ServiceException.class,()->service.extensionCheckin(new BizStudent(),8L));
    }
    @Test void regularUsesExistingCheckinRules() {
        when(mapper.lesson(8L)).thenReturn(row("lesson_scope","regular"));
        assertFalse(service.extensionCheckin(new BizStudent(),8L));
    }
    @Test void lessonRosterCannotBeReadFromAnotherGroup() {
        when(mapper.lesson(8L)).thenReturn(row("extension_class_id",4L));
        assertThrows(ServiceException.class,()->service.roster(3L,8L));
        verify(mapper,never()).roster(8L);
    }
    @Test void candidatesCapAtLimitAndReportTruncationWithoutPerRowDeptLookup() {
        List<Map<String,Object>> rows=new ArrayList<>();
        for(int i=0;i<201;i++) rows.add(row("student_id",(long)i,"student_no","s"+i,"student_name","n"+i,
                "entry_year","2025","class_code","1","dept_id",10L,"school_type","1"));
        when(mapper.candidates(10L,"")).thenReturn(rows);
        Map<String,Object> result=service.candidates("");
        assertEquals(200,((List<?>)result.get("items")).size());
        assertEquals(Boolean.TRUE,result.get("truncated"));
        // 学校类型随 SQL 返回，不能再逐行查 sys_dept。
        verify(depts,never()).selectDeptById(anyLong());
    }
    @Test void candidatesWithinLimitAreNotMarkedTruncated() {
        when(mapper.candidates(10L,"张三")).thenReturn(new ArrayList<>(Collections.singletonList(
                row("student_id",1L,"student_no","s1","student_name","张三","entry_year","2025",
                        "class_code","1","dept_id",10L,"school_type","1"))));
        Map<String,Object> result=service.candidates("张三");
        assertEquals(1,((List<?>)result.get("items")).size());
        assertEquals(Boolean.FALSE,result.get("truncated"));
    }
    @Test void summaryUsesTheSameSnapshotAsTheRoster() {
        when(mapper.currentLesson(3L)).thenReturn(row("lesson_id",8L));
        when(mapper.roster(8L)).thenReturn(Arrays.asList(row("checkin_time",new Date()),row("student_id",10L)));
        Map<String,Object> dashboard=service.dashboard(3L);
        assertEquals(2,dashboard.get("total")); assertEquals(1L,dashboard.get("checkedIn"));
    }
}
