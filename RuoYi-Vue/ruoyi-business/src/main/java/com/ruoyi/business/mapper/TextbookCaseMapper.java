package com.ruoyi.business.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 课例运行期只读，发布由审核后的离线导入完成。 */
public interface TextbookCaseMapper
{
    String PUBLISHED = " FROM biz_textbook_case c JOIN biz_textbook_book b ON b.book_id=c.book_id AND b.status='0' "
        + "JOIN biz_textbook_section s ON s.book_id=c.book_id AND s.section_key=c.section_key AND s.kind='LESSON' WHERE c.status='PUBLISHED' ";

    @Select("SELECT c.book_id AS bookId,c.section_key AS sectionKey,COUNT(*) AS total" + PUBLISHED
        + " GROUP BY c.book_id,c.section_key")
    List<Map<String, Object>> summary();

    @Select("SELECT c.case_id AS caseId,c.title,c.author,c.school" + PUBLISHED
        + " AND c.book_id=#{bookId} AND c.section_key=#{sectionKey} ORDER BY c.sort_no,c.case_id")
    List<Map<String, Object>> cases(@Param("bookId") Long bookId, @Param("sectionKey") String sectionKey);

    @Select("SELECT resource_id AS resourceId,category,title,filename,bytes,description,requirements,"
        + "CASE WHEN preview_path IS NULL OR preview_path='' THEN 0 ELSE 1 END AS previewable,"
        + "CASE WHEN LOWER(original_path) LIKE '%.html' OR LOWER(original_path) LIKE '%.htm' THEN 1 ELSE 0 END AS htmlPreview "
        + "FROM biz_textbook_case_resource WHERE case_id=#{caseId} ORDER BY sort_no,resource_id")
    List<Map<String, Object>> resources(@Param("caseId") Long caseId);

    @Select("SELECT r.filename,r.original_path AS originalPath,r.preview_path AS previewPath "
        + "FROM biz_textbook_case_resource r JOIN biz_textbook_case c ON c.case_id=r.case_id "
        + "JOIN biz_textbook_book b ON b.book_id=c.book_id AND b.status='0' "
        + "JOIN biz_textbook_section s ON s.book_id=c.book_id AND s.section_key=c.section_key AND s.kind='LESSON' "
        + "WHERE r.resource_id=#{id} AND c.status='PUBLISHED'")
    Map<String, Object> file(@Param("id") Long id);
}
