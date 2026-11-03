package com.ruoyi.business.service;

import java.time.LocalDate;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.mapper.BizExtensionClassMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.util.AcademicYearUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.common.core.domain.entity.SysDept;

/** 拓展班独立管理；名单变更与签到使用同一班级锁，历史名单只读快照。 */
@Service
public class ExtensionClassService {
    private static final int CANDIDATE_LIMIT = 200;
    @Autowired private BizExtensionClassMapper mapper;
    @Autowired private BizStudentMapper studentMapper;
    @Autowired private SysDeptMapper deptMapper;

    public boolean admin() { return SecurityUtils.isAdmin(SecurityUtils.getUserId()) || SecurityUtils.hasRole("admin"); }
    /** 只要有教师角色即可管理：同时兼任教研员的教师也要能管自己的班；纯教研员仍拒绝。负责人候选仍只列纯教师。 */
    public void requireTeacher() {
        if (!admin() && !SecurityUtils.hasRole("teacher"))
            throw denied("仅教师和管理员可以管理拓展班");
    }
    public List<Map<String,Object>> classes() {
        requireTeacher();
        List<Map<String,Object>> rows = mapper.classes(SecurityUtils.getDeptId(), admin() ? null : SecurityUtils.getUserId());
        for (Map<String,Object> row : rows) row.put("dashboard", dashboard(number(row.get("extension_class_id"))));
        return rows;
    }
    public List<Map<String,Object>> teachers() { requireTeacher(); return mapper.teachers(SecurityUtils.getDeptId()); }
    public Map<String,Object> authorized(Long id, boolean writable) {
        requireTeacher();
        Map<String,Object> row = mapper.find(id);
        if (row == null || !SecurityUtils.getDeptId().equals(number(row.get("dept_id"))) ||
            (!admin() && !SecurityUtils.getUserId().equals(number(row.get("owner_user_id")))))
            throw denied("无权管理该拓展班");
        if (writable && (!"ACTIVE".equals(row.get("status")) || number(row.get("legacy_flag")) == 1L))
            throw new ServiceException("历史或已归档拓展班仅可查看");
        return row;
    }
    @Transactional(rollbackFor=Exception.class)
    public Map<String,Object> create(Map<String,Object> request) {
        requireTeacher();
        Long deptId=SecurityUtils.getDeptId();
        mapper.lockSchool(deptId);
        String year=String.valueOf(LocalDate.now().getYear());
        int code=51;
        while (code<=99 && mapper.occupied(deptId,year,String.valueOf(code))>0) code++;
        if (code>99) throw new ServiceException("本校拓展班载体已用完，请联系管理员");
        Long owner=admin() ? number(request.get("ownerUserId")) : SecurityUtils.getUserId();
        // 候选下拉只列纯教师；双角色教师本人建班不经过下拉，这里放行本人，避免建出自己管不了的班
        boolean validOwner = owner != null && (mapper.teacher(deptId,owner)>0
                || (!admin() && SecurityUtils.hasRole("teacher") && owner.equals(SecurityUtils.getUserId())));
        if (!validOwner) throw new ServiceException("请选择本校有效的负责人教师");
        Map<String,Object> row=new HashMap<>();
        row.put("deptId",deptId); row.put("entryYear",year); row.put("classCode",String.valueOf(code));
        row.put("ownerUserId",owner); row.put("className",text(request,"className",100,true));
        row.put("remark",text(request,"remark",500,false)); row.put("createBy",SecurityUtils.getUsername());
        try { mapper.insert(row); } catch (DuplicateKeyException ex) { throw new ServiceException("本校已有同名拓展班"); }
        return mapper.find(number(row.get("extensionClassId")));
    }
    @Transactional(rollbackFor=Exception.class)
    public void update(Long id,Map<String,Object> request) {
        mapper.lock(id); authorized(id,true);
        Map<String,Object> row=new HashMap<>(); row.put("extensionClassId",id);
        row.put("className",text(request,"className",100,true)); row.put("remark",text(request,"remark",500,false));
        try { mapper.rename(row); } catch (DuplicateKeyException ex) { throw new ServiceException("本校已有同名拓展班"); }
    }
    @Transactional(rollbackFor=Exception.class)
    public void archive(Long id) {
        mapper.lock(id); authorized(id,true);
        for (Map<String,Object> member : mapper.members(id)) mapper.resetSpace(number(member.get("student_id")));
        mapper.archive(id); mapper.clearMembers(id);
    }
    public List<Map<String,Object>> members(Long id) { authorized(id,false); return mapper.members(id); }
    /** 候选学生一次查询带上学校类型；超出上限时明确告诉前端结果被截断，而不是静默少人。 */
    public Map<String,Object> candidates(String keyword) {
        requireTeacher();
        List<Map<String,Object>> rows=new ArrayList<>(mapper.candidates(SecurityUtils.getDeptId(),keyword));
        boolean truncated=rows.size()>CANDIDATE_LIMIT;
        if (truncated) rows=new ArrayList<>(rows.subList(0,CANDIDATE_LIMIT));
        rows.removeIf(row -> !enrolled(row,row.get("school_type")));
        Map<String,Object> result=new HashMap<>();
        result.put("items",rows); result.put("truncated",truncated);
        return result;
    }
    private boolean enrolled(Map<String,Object> row,Object schoolType) {
        if (schoolType==null) return false;
        try {
            int grade=AcademicYearUtils.resolveAbsoluteGrade(String.valueOf(row.get("entry_year")),String.valueOf(schoolType),LocalDate.now());
            return grade>0 && grade<=12;
        } catch (RuntimeException ex) { return false; }
    }
    @Transactional(rollbackFor=Exception.class)
    public void add(Long id,List<Long> studentIds) {
        mapper.lock(id); Map<String,Object> group=authorized(id,true);
        if (studentIds==null || studentIds.isEmpty() || studentIds.size()>200) throw new ServiceException("请选择1至200名学生");
        mapper.clearInactiveMembers();
        // 成员只能来自本班同一学校，学校类型取一次即可，避免逐人查 sys_dept。
        SysDept addDept=deptMapper.selectDeptById(number(group.get("dept_id")));
        String schoolType=addDept==null ? null : addDept.getSchoolType();
        List<Long> sorted=new ArrayList<>(new TreeSet<>(studentIds));
        for (Long sid: sorted) {
            Map<String,Object> student=mapper.lockStudent(sid);
            if (student==null || !group.get("dept_id").equals(student.get("dept_id")) ||
                !"0".equals(student.get("status")) || !"0".equals(student.get("del_flag")) || !enrolled(student,schoolType))
                throw denied("只能加入本校在读的有效学生");
            if (mapper.carrier(number(group.get("dept_id")),String.valueOf(student.get("entry_year")),String.valueOf(student.get("class_code")))!=null)
                throw new ServiceException("历史拓展账号不能加入，请使用原行政班账号");
            Map<String,Object> existing=mapper.membership(sid);
            if (existing!=null) {
                if (id.equals(number(existing.get("extension_class_id")))) continue;
                throw new ServiceException("所选学生已在其他拓展班，请先移出");
            }
            Map<String,Object> row=new HashMap<>(); row.put("id",id); row.put("studentId",sid);
            row.put("deptId",group.get("dept_id")); row.put("createBy",SecurityUtils.getUsername());
            try { mapper.addMember(row); } catch (DuplicateKeyException ex) { throw new ServiceException("学生已在其他拓展班，请刷新后重试"); }
            // 新成员立即获得已发布题单，学情也要补记投放名单；签到仍只认建课时的快照。
            mapper.joinPublishedPlans(id,sid);
            mapper.resetSpace(sid);
        }
    }
    @Transactional(rollbackFor=Exception.class)
    public void remove(Long id,Long sid) {
        mapper.lock(id); authorized(id,true); mapper.lockStudent(sid);
        if (mapper.removeMember(id,sid)>0) mapper.resetSpace(sid);
    }
    @Transactional(rollbackFor=Exception.class)
    public Map<String,Object> createLesson(Long id,Map<String,Object> request) {
        mapper.lock(id); Map<String,Object> group=authorized(id,true);
        Map<String,Object> row=new HashMap<>(); row.put("id",id); row.put("entryYear",group.get("entry_year"));
        row.put("classCode",group.get("class_code")); row.put("ownerId",group.get("owner_user_id"));
        row.put("deptId",group.get("dept_id")); row.put("createBy",SecurityUtils.getUsername());
        row.put("lessonTitle",text(request,"lessonTitle",100,true)); row.put("remark",text(request,"remark",500,false));
        try { row.put("lessonDate",LocalDate.parse(text(request,"lessonDate",10,true)).toString()); }
        catch (RuntimeException ex) { throw new ServiceException("请选择有效课程日期"); }
        mapper.insertLesson(row); mapper.snapshot(id,number(row.get("lessonId"))); mapper.assign(row);
        return mapper.currentLesson(id);
    }
    public Map<String,Object> dashboard(Long id) {
        authorized(id,false); Map<String,Object> result=new HashMap<>();
        Map<String,Object> lesson=mapper.currentLesson(id);
        List<Map<String,Object>> roster=lesson==null ? Collections.emptyList() : mapper.roster(number(lesson.get("lesson_id")));
        result.put("currentLesson",lesson); result.put("roster",roster); result.put("total",roster.size());
        result.put("checkedIn",roster.stream().filter(row -> row.get("checkin_time")!=null).count());
        result.put("lessons",mapper.lessons(id));
        return result;
    }
    public Map<String,Object> legacy(Long id) {
        authorized(id,false); Map<String,Object> result=new HashMap<>();
        result.put("lessons",mapper.lessons(id)); result.put("plans",mapper.plans(id)); return result;
    }
    public List<Map<String,Object>> roster(Long id,Long lessonId) {
        authorized(id,false);
        Map<String,Object> lesson=mapper.lesson(lessonId);
        if (lesson==null || !id.equals(number(lesson.get("extension_class_id")))) throw denied("课程不属于该拓展班");
        return mapper.roster(lessonId);
    }
    public BizStudent student() {
        BizStudent student=studentMapper.selectBizStudentByUserId(SecurityUtils.getUserId());
        if (student==null) throw denied("当前账号不是学生");
        return student;
    }
    public Map<String,Object> membership(BizStudent student) {
        Map<String,Object> row=mapper.membership(student.getStudentId());
        if (row!=null && !student.getDeptId().equals(number(row.get("dept_id")))) throw denied("拓展班学校不匹配");
        return row;
    }
    public Map<String,Object> context() {
        BizStudent student=student(); Map<String,Object> result=new HashMap<>();
        result.put("regularLabel",student.getEntryYear()+"级"+student.getClassCode()+"班");
        SysDept dept=deptMapper.selectDeptById(student.getDeptId());
        if (dept!=null && dept.getSchoolType()!=null) {
            try { result.put("regularLabel",AcademicYearUtils.resolveGradeName(student.getEntryYear(),dept.getSchoolType(),LocalDate.now())+"（"+student.getClassCode()+"）班"); } catch (RuntimeException ignored) { }
        }
        Map<String,Object> group=membership(student); result.put("extensionClass",group);
        result.putAll(mapper.preference(student.getStudentId()));
        if (group==null) result.put("space_pref","regular");
        return result;
    }
    @Transactional(rollbackFor=Exception.class)
    public void saveSpace(String space) {
        if (!"regular".equals(space) && !"extension".equals(space)) throw new ServiceException("学习空间无效");
        BizStudent student=student(); Map<String,Object> group=membership(student);
        if (group!=null) mapper.lock(number(group.get("extension_class_id")));
        if ("extension".equals(space) && mapper.lockMembership(student.getStudentId())==null) throw denied("你已不在该拓展班");
        mapper.saveSpace(student.getStudentId(),space);
    }
    public Map<String,Object> home() {
        BizStudent student=student(); Map<String,Object> group=membership(student);
        if (group==null) throw denied("你已不在该拓展班");
        Long id=number(group.get("extension_class_id")); Map<String,Object> result=new HashMap<>();
        Map<String,Object> lesson=mapper.currentLesson(id);
        result.put("extensionClass",group); result.put("currentLesson",lesson);
        result.put("eligible",lesson!=null && mapper.inSnapshot(number(lesson.get("lesson_id")),student.getStudentId())>0);
        List<Map<String,Object>> history=mapper.history(student.getStudentId(),id); result.put("history",history);
        if (lesson!=null) for (Map<String,Object> row:mapper.roster(number(lesson.get("lesson_id")))) {
            if (student.getStudentId().equals(number(row.get("student_id")))) result.put("checkinTime",row.get("checkin_time"));
        }
        return result;
    }
    /** 在签到事务内持有班级锁；成员移出或新课替换不能与鉴权穿插。 */
    public boolean extensionCheckin(BizStudent student,Long lessonId) {
        Map<String,Object> lesson=mapper.lesson(lessonId);
        if (lesson==null || "regular".equals(lesson.get("lesson_scope"))) return false;
        if (!"extension".equals(lesson.get("lesson_scope"))) throw denied("历史拓展课不能签到");
        Long id=number(lesson.get("extension_class_id")); mapper.lock(id);
        Map<String,Object> group=mapper.lockMembership(student.getStudentId()),current=mapper.currentLessonForUpdate(id);
        if (group==null || !id.equals(number(group.get("extension_class_id"))) || current==null ||
            !lessonId.equals(number(current.get("lesson_id"))) || mapper.inSnapshot(lessonId,student.getStudentId())==0)
            throw denied("只能由本课名单中的在班学生签到当前拓展课");
        return true;
    }
    public static Long number(Object value) { return value==null ? null : Long.valueOf(String.valueOf(value)); }
    private static String text(Map<String,Object> row,String key,int max,boolean required) {
        String value=row.get(key)==null ? "" : String.valueOf(row.get(key)).trim();
        if ((required && value.isEmpty()) || value.length()>max) throw new ServiceException("请填写有效的"+("className".equals(key)?"班级名称":key));
        return value;
    }
    private static ServiceException denied(String message) { return new ServiceException(message,403); }
}
