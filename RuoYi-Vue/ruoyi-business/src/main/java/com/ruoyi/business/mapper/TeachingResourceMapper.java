package com.ruoyi.business.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 册级教学设计汇编与课程指南只读查询；发布由离线导入完成。 */
public interface TeachingResourceMapper
{
    @Select("SELECT standard_id AS standardId,standard_key AS standardKey,title,audience,filename,bytes "
        + "FROM biz_textbook_standard WHERE status='0' ORDER BY sort_no,standard_id")
    List<Map<String, Object>> standards();

    @Select("SELECT title,filename,original_path AS originalPath,preview_path AS previewPath "
        + "FROM biz_textbook_standard WHERE standard_key=#{key} AND status='0'")
    Map<String, Object> standardFile(@Param("key") String key);

    @Select("SELECT r.id,r.category,r.title,r.filename,r.bytes,r.description,r.requirements,"
        + "CASE WHEN r.preview_path IS NULL OR r.preview_path='' THEN 0 ELSE 1 END AS previewable "
        + "FROM biz_textbook_book_resource r JOIN biz_textbook_book b ON b.book_id=r.book_id AND b.status='0' "
        + "WHERE r.book_id=#{bookId} ORDER BY r.sort_no,r.id")
    List<Map<String, Object>> bookResources(@Param("bookId") Long bookId);

    @Select("SELECT r.id,r.book_id AS bookId,r.category,r.title,r.filename,r.bytes,r.description,"
        + "CASE WHEN r.preview_path IS NULL OR r.preview_path='' THEN 0 ELSE 1 END AS previewable "
        + "FROM biz_textbook_book_resource r JOIN biz_textbook_book b ON b.book_id=r.book_id AND b.status='0' "
        + "ORDER BY r.book_id,r.sort_no,r.id")
    List<Map<String, Object>> allBookResources();

    @Select("SELECT r.filename,r.original_path AS originalPath,r.preview_path AS previewPath "
        + "FROM biz_textbook_book_resource r JOIN biz_textbook_book b ON b.book_id=r.book_id AND b.status='0' "
        + "WHERE r.id=#{id}")
    Map<String, Object> bookFile(@Param("id") Long id);

    @Select("SELECT g.grade,g.title,g.pdf_filename AS pdfFilename,g.pdf_bytes AS pdfBytes,"
        + "g.usage_filename AS usageFilename,g.usage_bytes AS usageBytes,g.note,"
        + "(SELECT COUNT(*) FROM biz_guide_lesson l WHERE l.guide_id=g.guide_id AND l.available=1) AS lessonCount,"
        + "(SELECT COUNT(*) FROM biz_guide_lesson l WHERE l.guide_id=g.guide_id AND l.available=0) AS missingCount "
        + "FROM biz_guide_grade g WHERE g.status='0' ORDER BY g.sort_no,g.grade")
    List<Map<String, Object>> guides();

    @Select("SELECT g.guide_id AS guideId,g.grade,g.title,g.pdf_filename AS pdfFilename,g.pdf_path AS pdfPath,"
        + "g.pdf_bytes AS pdfBytes,g.usage_filename AS usageFilename,g.usage_path AS usagePath,g.usage_bytes AS usageBytes,g.note "
        + "FROM biz_guide_grade g WHERE g.grade=#{grade} AND g.status='0'")
    Map<String, Object> guide(@Param("grade") Integer grade);

    @Select("SELECT l.lesson_id AS lessonId,l.lesson_no AS lessonNo,l.title,l.filename,l.bytes,l.available,l.note "
        + "FROM biz_guide_lesson l JOIN biz_guide_grade g ON g.guide_id=l.guide_id AND g.status='0' "
        + "WHERE g.grade=#{grade} ORDER BY l.lesson_no")
    List<Map<String, Object>> guideLessons(@Param("grade") Integer grade);

    @Select("SELECT l.filename,l.original_path AS originalPath,l.preview_path AS previewPath "
        + "FROM biz_guide_lesson l JOIN biz_guide_grade g ON g.guide_id=l.guide_id AND g.status='0' "
        + "WHERE l.lesson_id=#{id} AND l.available=1")
    Map<String, Object> guideLessonFile(@Param("id") Long id);
}
