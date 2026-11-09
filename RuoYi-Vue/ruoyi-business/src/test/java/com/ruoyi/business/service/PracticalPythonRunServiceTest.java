package com.ruoyi.business.service;

import java.util.Map;
import java.util.concurrent.Semaphore;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.judge.Judge0Client;
import com.ruoyi.business.judge.Judge0Request;
import com.ruoyi.business.judge.Judge0Result;
import com.ruoyi.business.judge.Judge0Properties;
import com.ruoyi.common.exception.ServiceException;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PracticalPythonRunServiceTest {
    @Test void classroomCapacityIsAlignedAndSlotIsReleasedAfterFailure() {
        Judge0Client client = mock(Judge0Client.class);
        PracticalPythonRunService service = service(client);
        Semaphore slots = (Semaphore) ReflectionTestUtils.getField(service, "slots");
        assertEquals(8, slots.availablePermits());

        when(client.submit(any())).thenThrow(new ServiceException("不可用"));
        assertThrows(ServiceException.class, () -> service.run("print(1)", ""));
        assertEquals(8, slots.availablePermits());
    }

    @Test void isolatedExecutionReturnsOutputWithoutLeakingToken() {
        Judge0Client client = mock(Judge0Client.class);
        PracticalPythonRunService service = service(client);
        Judge0Result result = new Judge0Result();
        result.setStatusId(3); result.setStdout("你好\n"); result.setToken("internal-token");
        when(client.submit(any())).thenReturn(result);
        Map<String, Object> data = service.run("print(input())", "你好");
        assertEquals(true, data.get("success"));
        assertEquals("你好\n", data.get("output"));
        assertFalse(data.toString().contains("internal-token"));
        ArgumentCaptor<Judge0Request> request = ArgumentCaptor.forClass(Judge0Request.class);
        verify(client).submit(request.capture());
        assertEquals("你好", request.getValue().getStdin());
        assertEquals(3D, request.getValue().getCpuTimeLimit());
        assertEquals(131072, request.getValue().getMemoryLimitKb());
        assertEquals(8, request.getValue().getMaxProcesses());
    }
    @Test void failureReleasesCapacityAndInputLimitsRunBeforeSubmission() {
        Judge0Client client = mock(Judge0Client.class);
        PracticalPythonRunService service = service(client);
        assertThrows(ServiceException.class, () -> service.run(new String(new char[65537]), ""));
        assertThrows(ServiceException.class, () -> service.run("print(1)", new String(new char[4097])));
        verifyNoInteractions(client);
        when(client.submit(any())).thenThrow(new ServiceException("不可用"));
        for (int i = 0; i < 3; i++) {
            assertEquals("不可用", assertThrows(ServiceException.class, () -> service.run("print(1)", "")).getMessage());
        }
    }
    private PracticalPythonRunService service(Judge0Client client) {
        PracticalPythonRunService service = new PracticalPythonRunService();
        ReflectionTestUtils.setField(service, "client", client);
        Judge0Properties properties = new Judge0Properties();
        ReflectionTestUtils.setField(properties, "mode", "http");
        ReflectionTestUtils.setField(service, "properties", properties);
        return service;
    }
}
