package com.ruoyi.business.util;

import com.ruoyi.common.utils.file.DownloadFileNameUtils;
import com.ruoyi.common.utils.file.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DownloadFileNameUtilsTest
{
    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 8, 14, 14, 30, 25);

    @Test
    void addsUnifiedTimestampBeforeExtension()
    {
        assertEquals("课程管理数据_20260814_143025.xlsx",
                DownloadFileNameUtils.withTimestamp("课程管理数据.xlsx", FIXED_TIME));
    }

    @Test
    void restoresOriginalNameFromRuoYiStoragePath()
    {
        assertEquals("课堂练习.docx", DownloadFileNameUtils.fromStoredPath(
                "/profile/upload/2026/08/14/课堂练习_20260814143025A001.docx", FIXED_TIME));
    }

    @Test
    void hidesUuidStorageNameWhenOriginalNameCannotBeRestored()
    {
        assertEquals("附件_20260814_143025.pdf", DownloadFileNameUtils.fromStoredPath(
                "/profile/upload/2026/08/14/6bd49c229fb84c91915d121d57f94511.pdf", FIXED_TIME));
    }

    @Test
    void removesPathAndIllegalCharacters()
    {
        assertEquals("课程_成绩_.xlsx",
                DownloadFileNameUtils.sanitize("../课程:成绩?.xlsx", "下载文件"));
    }

    @Test
    void responseHeaderExposesSameNormalizedChineseName() throws Exception
    {
        MockHttpServletResponse response = new MockHttpServletResponse();
        FileUtils.setAttachmentResponseHeader(response, "课程:成绩.xlsx");

        assertEquals("%E8%AF%BE%E7%A8%8B_%E6%88%90%E7%BB%A9.xlsx",
                response.getHeader("download-filename"));
        assertTrue(response.getHeader("Content-Disposition").contains("filename*=utf-8''"));
    }
}
