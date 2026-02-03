package com.ruoyi.business.service.impl;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.mapper.StudentProfileMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.vo.StudentProfileVo;
import com.ruoyi.business.service.IStudentProfileService;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.SysDeptMapper;

/**
 * 学生成绩画像 Service 实现
 */
@Service
public class StudentProfileServiceImpl implements IStudentProfileService {

    @Autowired
    private StudentProfileMapper studentProfileMapper;

    @Autowired
    private BizStudentMapper studentMapper;

    @Autowired
    private SysDeptMapper deptMapper;

    @Override
    public StudentProfileVo getStudentProfile(Long studentId, String semesterStart, String semesterEnd) {
        StudentProfileVo profile = new StudentProfileVo();
        
        // 1. 获取学生基础信息
        Map<String, Object> basicInfo = studentProfileMapper.selectStudentBasicInfo(studentId);
        if (basicInfo == null) {
            return null;
        }
        
        profile.setStudentId(studentId);
        profile.setStudentName((String) basicInfo.get("studentName"));
        profile.setClassCode((String) basicInfo.get("classCode"));
        profile.setEntryYear((String) basicInfo.get("entryYear"));
        profile.setRemark((String) basicInfo.get("remark"));
        
        // 计算年级名称
        String gradeName = calculateGradeName((String) basicInfo.get("entryYear"));
        profile.setGradeName(gradeName);
        
        // 2. 获取课程成绩并计算统计
        List<StudentProfileVo.CourseScoreItem> courses = getCourseScores(studentId, semesterStart, semesterEnd);
        profile.setTotalCourses(courses.size());
        
        // 计算总体平均分
        if (!courses.isEmpty()) {
            double avgScore = courses.stream()
                .filter(c -> c.getStudentScore() != null)
                .mapToDouble(StudentProfileVo.CourseScoreItem::getStudentScore)
                .average()
                .orElse(0.0);
            profile.setAverageScore(Math.round(avgScore * 10) / 10.0);
        }
        
        // 3. 计算课堂表现平均分（0分不参与）
        List<StudentProfileVo.PerformanceItem> performances = getPerformances(studentId, semesterStart, semesterEnd);
        if (!performances.isEmpty()) {
            double perfAvg = performances.stream()
                .filter(p -> p.getPerformance() != null && p.getPerformance() != 0)
                .mapToInt(StudentProfileVo.PerformanceItem::getPerformance)
                .average()
                .orElse(0.0);
            profile.setPerformanceAvg(Math.round(perfAvg * 10) / 10.0);
        }
        
        // 4. 计算打字平均速度
        List<StudentProfileVo.TypingSpeedItem> typingSpeeds = getTypingSpeeds(studentId, semesterStart, semesterEnd);
        if (!typingSpeeds.isEmpty()) {
            double speedAvg = typingSpeeds.stream()
                .filter(t -> t.getTypingSpeed() != null)
                .mapToInt(StudentProfileVo.TypingSpeedItem::getTypingSpeed)
                .average()
                .orElse(0.0);
            profile.setTypingSpeedAvg(Math.round(speedAvg * 10) / 10.0);
        }
        
        // 5. 计算当前排名
        Long deptId = SecurityUtils.getDeptId();
        List<StudentProfileVo.RankItem> ranks = getRankChanges(studentId, semesterStart, semesterEnd);
        if (!ranks.isEmpty()) {
            StudentProfileVo.RankItem lastRank = ranks.get(ranks.size() - 1);
            profile.setCurrentRank(lastRank.getRank());
            profile.setClassTotal(lastRank.getClassTotal());
        } else {
            Integer classTotal = studentProfileMapper.selectClassStudentCount(
                (String) basicInfo.get("entryYear"),
                (String) basicInfo.get("classCode"),
                deptId
            );
            profile.setClassTotal(classTotal);
        }
        
        return profile;
    }

    @Override
    public List<StudentProfileVo.CourseScoreItem> getCourseScores(Long studentId, String semesterStart, String semesterEnd) {
        // 获取学生信息
        Map<String, Object> basicInfo = studentProfileMapper.selectStudentBasicInfo(studentId);
        if (basicInfo == null) {
            return Collections.emptyList();
        }
        
        Long deptId = SecurityUtils.getDeptId();
        
        // 获取学生课程成绩
        List<StudentProfileVo.CourseScoreItem> courses = studentProfileMapper.selectCourseScores(studentId, semesterStart, semesterEnd);
        
        // 获取班级平均分
        List<Map<String, Object>> classAvgs = studentProfileMapper.selectClassAvgScores(
            (String) basicInfo.get("entryYear"),
            (String) basicInfo.get("classCode"),
            deptId,
            semesterStart,
            semesterEnd
        );
        
        // 转换为 Map 方便查找
        Map<Long, Double> avgMap = classAvgs.stream()
            .collect(Collectors.toMap(
                m -> ((Number) m.get("lessonId")).longValue(),
                m -> m.get("classAvgScore") != null ? ((Number) m.get("classAvgScore")).doubleValue() : 0.0
            ));
        
        // 填充班级平均分
        for (StudentProfileVo.CourseScoreItem course : courses) {
            course.setClassAvgScore(avgMap.getOrDefault(course.getLessonId(), 0.0));
        }
        
        return courses;
    }

    @Override
    public List<StudentProfileVo.TypingSpeedItem> getTypingSpeeds(Long studentId, String semesterStart, String semesterEnd) {
        List<StudentProfileVo.TypingSpeedItem> speeds = studentProfileMapper.selectTypingSpeeds(studentId, semesterStart, semesterEnd);
        
        // 获取学生信息确定年级基准
        Map<String, Object> basicInfo = studentProfileMapper.selectStudentBasicInfo(studentId);
        if (basicInfo != null) {
            String gradeName = calculateGradeName((String) basicInfo.get("entryYear"));
            int baseline = determineBaseSpeed(gradeName);
            
            for (StudentProfileVo.TypingSpeedItem item : speeds) {
                item.setGradeBaseline(baseline);
            }
        }
        
        return speeds;
    }

    @Override
    public List<StudentProfileVo.PerformanceItem> getPerformances(Long studentId, String semesterStart, String semesterEnd) {
        return studentProfileMapper.selectPerformances(studentId, semesterStart, semesterEnd);
    }

    @Override
    public List<StudentProfileVo.RankItem> getRankChanges(Long studentId, String semesterStart, String semesterEnd) {
        // 获取学生信息
        Map<String, Object> basicInfo = studentProfileMapper.selectStudentBasicInfo(studentId);
        if (basicInfo == null) {
            return Collections.emptyList();
        }
        
        Long deptId = SecurityUtils.getDeptId();
        String entryYear = (String) basicInfo.get("entryYear");
        String classCode = (String) basicInfo.get("classCode");
        
        // 获取班级学生总数
        Integer classTotal = studentProfileMapper.selectClassStudentCount(entryYear, classCode, deptId);
        
        // 获取班级所有学生的历次课程成绩
        List<Map<String, Object>> allScores = studentProfileMapper.selectClassStudentScores(
            entryYear, classCode, deptId, semesterStart, semesterEnd
        );
        
        // 获取这个学生的课程列表（用于确定顺序）
        List<StudentProfileVo.CourseScoreItem> studentCourses = studentProfileMapper.selectCourseScores(studentId, semesterStart, semesterEnd);
        
        // 计算每个课程后的排名
        List<StudentProfileVo.RankItem> result = new ArrayList<>();
        
        // 按课程顺序遍历
        Map<Long, Double> cumulativeScores = new HashMap<>(); // studentId -> 累计总分
        Set<Long> processedLessons = new HashSet<>();
        
        for (StudentProfileVo.CourseScoreItem course : studentCourses) {
            Long lessonId = course.getLessonId();
            if (processedLessons.contains(lessonId)) continue;
            processedLessons.add(lessonId);
            
            // 更新所有学生在此课程的累计分数
            for (Map<String, Object> score : allScores) {
                if (((Number) score.get("lessonId")).longValue() == lessonId) {
                    Long sid = ((Number) score.get("studentId")).longValue();
                    Double lessonScore = score.get("totalScore") != null ? ((Number) score.get("totalScore")).doubleValue() : 0.0;
                    cumulativeScores.merge(sid, lessonScore, Double::sum);
                }
            }
            
            // 计算排名
            List<Map.Entry<Long, Double>> sorted = cumulativeScores.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .collect(Collectors.toList());
            
            int rank = 1;
            Double studentCumulativeScore = cumulativeScores.get(studentId);
            for (int i = 0; i < sorted.size(); i++) {
                if (sorted.get(i).getKey().equals(studentId)) {
                    rank = i + 1;
                    break;
                }
            }
            
            // 计算截至当时的累计平均分
            int courseCount = processedLessons.size();
            double cumulativeAvg = studentCumulativeScore != null ? studentCumulativeScore / courseCount : 0.0;
            
            StudentProfileVo.RankItem item = new StudentProfileVo.RankItem();
            item.setLessonId(lessonId);
            item.setLessonTitle(course.getLessonTitle());
            item.setRank(rank);
            item.setClassTotal(classTotal);
            item.setCumulativeAvg(Math.round(cumulativeAvg * 10) / 10.0);
            
            result.add(item);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getClassList() {
        Long deptId = SecurityUtils.getDeptId();
        List<BizStudent> list = studentMapper.selectDistinctYearAndClassByDeptId(deptId);
        
        return list.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("entryYear", s.getEntryYear());
            map.put("classCode", s.getClassCode());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<BizStudent> getStudentList(String entryYear, String classCode) {
        BizStudent query = new BizStudent();
        query.setEntryYear(entryYear);
        query.setClassCode(classCode);
        query.setDeptId(SecurityUtils.getDeptId());
        List<BizStudent> list = studentMapper.selectBizStudentList(query);
        
        // 去重 (以 studentId 为准)
        return list.stream()
            .collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(BizStudent::getStudentId))),
                ArrayList::new
            ));
    }

    /**
     * 根据入学年份计算年级名称
     */
    private String calculateGradeName(String entryYear) {
        if (entryYear == null) return "未知年级";
        
        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH) + 1;
        
        int academicStartYear = currentYear;
        if (currentMonth < 7) {
            academicStartYear = currentYear - 1;
        }
        
        int yearsInSchool = academicStartYear - Integer.parseInt(entryYear) + 1;
        
        // 默认初中
        String[] gradeNames = new String[]{"七年级", "八年级", "九年级"};
        
        if (yearsInSchool >= 1 && yearsInSchool <= gradeNames.length) {
            return gradeNames[yearsInSchool - 1];
        }
        return "未知年级";
    }

    /**
     * 根据年级名称判断打字基准速度
     */
    private int determineBaseSpeed(String gradeName) {
        if (gradeName == null) {
            return 40;
        }
        
        // 小学：一年级~六年级
        if (gradeName.matches(".*[一二三四五六]年级.*") || 
            gradeName.matches(".*[1-6]年级.*")) {
            return 20;
        }
        
        // 初中、高中
        return 40;
    }
}
