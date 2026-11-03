package com.ruoyi.business.controller;

import java.nio.file.*;
import java.nio.*;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.service.*;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SeatHelperDownloadTest {
    @Test void downloadPreservesExecutableAndAddsExplicitAliases() throws Exception {
        Path executable=Files.createTempFile("seat-download-",".exe");
        try {
            byte[] original={77,90,1,2};Files.write(executable,original);
            SeatHelperController controller=new SeatHelperController();
            SeatHelperService service=mock(SeatHelperService.class);GuideSheetAccessService access=mock(GuideSheetAccessService.class);
            BizStudent student=new BizStudent();student.setDeptId(9L);
            when(access.requireCurrentStudent()).thenReturn(student);when(service.isEnabled(9L)).thenReturn(true);
            when(service.platformUrl(9L)).thenReturn("http://10.52.1.130");
            ReflectionTestUtils.setField(controller,"service",service);ReflectionTestUtils.setField(controller,"access",access);
            ReflectionTestUtils.setField(controller,"clientPath",executable.toString());
            MockHttpServletResponse response=new MockHttpServletResponse();controller.download(response);
            byte[] body=response.getContentAsByteArray();assertArrayEquals(original,Arrays.copyOf(body,original.length));
            assertEquals("SEATHELPER-SITE01",new String(body,body.length-17,17,"US-ASCII"));
            int length=ByteBuffer.wrap(body,body.length-21,4).order(ByteOrder.LITTLE_ENDIAN).getInt();
            Map<String,Object> bootstrap=new ObjectMapper().readValue(Arrays.copyOfRange(body,body.length-21-length,body.length-21),Map.class);
            assertEquals(Arrays.asList("http://10.52.1.130","http://xxkj.xsedu.net.cn"),bootstrap.get("allowedOrigins"));
            assertFalse(bootstrap.containsKey("secret"));
            ReflectionTestUtils.setField(controller,"allowedOrigins","javascript:alert(1)");
            MockHttpServletResponse invalid=new MockHttpServletResponse();
            assertThrows(Exception.class,()->controller.download(invalid));assertEquals(0,invalid.getContentAsByteArray().length);
        } finally {Files.deleteIfExists(executable);}
    }
}
