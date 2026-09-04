package com.ruoyi.business.judge;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class Judge0MockClientTest {
    @Test
    void returnsQueuedThenAcceptedResult() {
        Judge0Request request = new Judge0Request();
        request.setSourceCode("print(input())");
        request.setExpectedOutput("hello");
        Judge0MockClient client = new Judge0MockClient();

        Judge0Result queued = client.submit(request);
        Judge0Result result = client.poll(queued.getToken());

        assertEquals(1, queued.getStatusId());
        assertNotNull(queued.getToken());
        assertEquals(3, result.getStatusId());
        assertEquals("hello", result.getStdout());
    }

    @Test
    void supportsLimitSimulationMarkers() {
        Judge0MockClient client = new Judge0MockClient();
        Judge0Request timeout = new Judge0Request();
        timeout.setSourceCode("# mock-timeout");
        Judge0Request memory = new Judge0Request();
        memory.setSourceCode("# mock-memory");

        assertEquals(5, client.poll(client.submit(timeout).getToken()).getStatusId());
        assertEquals(12, client.poll(client.submit(memory).getToken()).getStatusId());
    }
}
