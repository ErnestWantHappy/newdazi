package com.ruoyi.business.service;

import java.nio.charset.Charset;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import com.ruoyi.common.exception.ServiceException;
import static org.junit.jupiter.api.Assertions.*;

class PracticalNewFileFormatsTest {
    private final PracticalFilePolicyService policy = new PracticalFilePolicyService();
    @Test void fileNameBoundaryMatchesStorageAndExplainsHowToRecover() {
        String name = new String(new char[96]).replace('\0', 'a') + ".txt";
        assertEquals(100, name.length());
        assertDoesNotThrow(() -> policy.inspect(text(name, "text/plain", "作品"), "txt"));
        ServiceException error = assertThrows(ServiceException.class, () -> policy.inspect(text("a" + name, "text/plain", "作品"), "txt"));
        assertTrue(error.getMessage().contains("100"));
        assertTrue(error.getMessage().contains("缩短文件名"));
    }
    @Test void newFormatsRequireExplicitQuestionPermission() {
        MockMultipartFile file = text("index.html", "text/html", "<script>document.body.innerHTML='作品'</script>");
        assertThrows(ServiceException.class, () -> policy.inspect(file, null));
        assertEquals("HTML", policy.inspect(file, "html").getFileKind());
        assertEquals("TEXT", policy.inspect(text("代码.txt", "text/plain", "<script>代码</script>"), "txt").getFileKind());
    }
    @Test void supportsChineseAndBomWithoutSilentlyReplacingCharacters() {
        assertEquals("中文代码", PracticalTextCodec.decode("中文代码".getBytes(Charset.forName("GB18030"))));
        assertEquals("中文", PracticalTextCodec.decode("\ufeff中文".getBytes(Charset.forName("UTF-8"))));
        assertThrows(ServiceException.class, () -> PracticalTextCodec.decode(new byte[] {0,1,2}));
        assertThrows(ServiceException.class, () -> PracticalTextCodec.decode(new byte[PracticalTextCodec.MAX_BYTES + 1]));
    }
    @Test void archivesAreOpaqueAndRequireMatchingHeader() {
        assertEquals("ARCHIVE", policy.inspect(new MockMultipartFile("file", "作业.zip", "application/zip", new byte[] {'P','K',3,4}), "zip").getFileKind());
        assertEquals("ARCHIVE", policy.inspect(new MockMultipartFile("file", "作业.rar", "application/vnd.rar", new byte[] {'R','a','r','!',0x1a,7,0}), "rar").getFileKind());
        assertEquals("ARCHIVE", policy.inspect(new MockMultipartFile("file", "作业.7z", "application/x-7z-compressed", new byte[] {0x37,0x7a,(byte)0xbc,(byte)0xaf,0x27,0x1c}), "7z").getFileKind());
        assertThrows(ServiceException.class, () -> policy.inspect(text("fake.zip", "application/zip", "不是压缩包"), "zip"));
    }
    private MockMultipartFile text(String name, String mime, String text) {
        return new MockMultipartFile("file", name, mime, text.getBytes(Charset.forName("UTF-8")));
    }
}
