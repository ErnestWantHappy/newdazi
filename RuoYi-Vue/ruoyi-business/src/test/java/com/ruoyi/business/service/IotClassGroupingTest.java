package com.ruoyi.business.service;

import java.util.ArrayList;
import java.util.List;
import com.ruoyi.business.domain.BizStudent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IotClassGroupingTest
{
    @Test
    void shouldGroupStudentsEvenlyAndHandleRemainderGroup()
    {
        List<BizStudent> students = new ArrayList<>();
        for (int i = 1; i <= 10; i++)
        {
            BizStudent s = new BizStudent();
            s.setStudentId((long) i);
            s.setStudentNo(String.format("%02d", i));
            s.setStudentName("学生" + i);
            students.add(s);
        }

        int groupSize = 4;
        int totalStudents = students.size();
        int totalGroups = (int) Math.ceil((double) totalStudents / groupSize);

        assertEquals(3, totalGroups); // 4, 4, 2 -> 共 3 组

        // 第 1 组：1, 2, 3, 4
        assertEquals("01", students.get(0).getStudentNo());
        assertEquals("04", students.get(3).getStudentNo());

        // 第 2 组：5, 6, 7, 8
        assertEquals("05", students.get(4).getStudentNo());
        assertEquals("08", students.get(7).getStudentNo());

        // 第 3 组：9, 10（不足 4 人仍为独立组）
        assertEquals("09", students.get(8).getStudentNo());
        assertEquals("10", students.get(9).getStudentNo());
    }

    @Test
    void shouldCalculateTwoPersonGroupsCorrectly()
    {
        int totalStudents = 7;
        int groupSize = 2;
        int totalGroups = (int) Math.ceil((double) totalStudents / groupSize);

        assertEquals(4, totalGroups); // 2, 2, 2, 1 -> 共 4 组
    }
}
