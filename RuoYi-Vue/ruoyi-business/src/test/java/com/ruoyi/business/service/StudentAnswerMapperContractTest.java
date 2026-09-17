package com.ruoyi.business.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 答案表的 student_id 存储 biz_student.student_id，不能与登录 user_id 混用。
 */
class StudentAnswerMapperContractTest
{
    @Test
    void answerQueriesJoinStudentByStudentId() throws Exception
    {
        assertStudentIdJoin("mapper/business/BizStudentAnswerMapper.xml");
        assertStudentIdJoin("mapper/business/SchoolScoreMapper.xml");
        assertStudentIdJoin("mapper/business/StudentProfileMapper.xml");
        assertStudentIdJoin("mapper/business/BizLessonMapper.xml");
    }

    private void assertStudentIdJoin(String resource) throws Exception
    {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
            String xml = new String(readFully(input), StandardCharsets.UTF_8);
            assertFalse(xml.matches("(?s).*student_id\\s*=\\s*[a-zA-Z_]+\\.user_id.*"));
            assertFalse(xml.matches("(?s).*[a-zA-Z_]+\\.user_id\\s*=\\s*[a-zA-Z_]+\\.student_id.*"));
            assertTrue(xml.contains("student_id"));
        }
    }

    private byte[] readFully(InputStream input) throws Exception
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) != -1) {
            output.write(buffer, 0, length);
        }
        return output.toByteArray();
    }
}
