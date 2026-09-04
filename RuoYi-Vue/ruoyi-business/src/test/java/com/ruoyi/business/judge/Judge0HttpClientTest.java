package com.ruoyi.business.judge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class Judge0HttpClientTest {
    @Test
    void shouldRoundTripUtf8TextThroughBase64() {
        String value = "开始学习 Python！\n第二行🙂";
        assertEquals(value, Judge0HttpClient.decodeText(Judge0HttpClient.encodeText(value)));
        assertEquals(value, Judge0HttpClient.decodeText(Judge0HttpClient.encodeText(value) + "\n"));
    }

    @Test
    void shouldPreserveNullAndToleratePlainText() {
        assertNull(Judge0HttpClient.encodeText(null));
        assertNull(Judge0HttpClient.decodeText(null));
        assertEquals("not-base64!", Judge0HttpClient.decodeText("not-base64!"));
    }
}
