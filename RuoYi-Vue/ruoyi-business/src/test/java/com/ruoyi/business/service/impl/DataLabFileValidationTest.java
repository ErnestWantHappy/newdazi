package com.ruoyi.business.service.impl;

import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import java.util.Base64;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import com.ruoyi.common.exception.ServiceException;
import static org.junit.jupiter.api.Assertions.*;

class DataLabFileValidationTest {
    @Test void rejectsAmbiguousSourceAndPaths() {
        assertThrows(ServiceException.class, () -> DataLabServiceImpl.requireSource("PLAN", null));
        assertThrows(ServiceException.class, () -> DataLabServiceImpl.requireSource("BASE_VERSION", 0L));
        for (String name : new String[]{"../a.xlsx", "C:\\a.xlsx", "/a.xlsx", "a\nb.xlsx"}) {
            assertThrows(ServiceException.class, () -> DataLabServiceImpl.validateFileName(name));
        }
        assertThrows(ServiceException.class, () -> DataLabServiceImpl.resolveExcelFile("/profile/datalab/excel/../../a.xlsx"));
        assertThrows(ServiceException.class, () -> DataLabServiceImpl.resolveExcelFile("C:/private.xlsx"));
        assertDoesNotThrow(() -> DataLabServiceImpl.validateFileName("高中运动会成绩统计.xlsx"));
    }

    @Test void validatesRealPngAndRejectsFakeOrOversizedImages() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", output);
        String png = Base64.getEncoder().encodeToString(output.toByteArray());
        assertArrayEquals(output.toByteArray(), DataLabServiceImpl.decodePng(png));
        assertThrows(ServiceException.class, () -> DataLabServiceImpl.decodePng(Base64.getEncoder().encodeToString("not png".getBytes("UTF-8"))));
        char[] large = new char[8 * 1024 * 1024 + 1];
        java.util.Arrays.fill(large, 'A');
        assertThrows(ServiceException.class, () -> DataLabServiceImpl.decodePng(new String(large)));
    }
}
