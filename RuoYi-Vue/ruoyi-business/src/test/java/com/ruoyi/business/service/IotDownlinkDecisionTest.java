package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** 旧页面接口不会触发命令下发，且仍必须通过小组访问校验。 */
class IotDownlinkDecisionTest
{
    @Test
    void shouldRejectLegacyCommandWithoutPublishing()
    {
        IotExperimentService access = mock(IotExperimentService.class);
        IotDownlinkService service = new IotDownlinkService();
        ReflectionTestUtils.setField(service, "experimentService", access);
        Map<String, Object> result = service.manualSend(1L, "开灯", "ON");
        assertEquals(false, result.get("published"));
        assertEquals(false, result.get("downlinkEnabled"));
        verify(access).requireManageableGroup(1L);
    }

    @Test
    void shouldKeepGroupAccessBoundary()
    {
        IotExperimentService access = mock(IotExperimentService.class);
        when(access.requireManageableGroup(2L)).thenThrow(new IllegalArgumentException("无权限"));
        IotDownlinkService service = new IotDownlinkService();
        ReflectionTestUtils.setField(service, "experimentService", access);
        assertThrows(IllegalArgumentException.class, () -> service.manualSend(2L, "开灯", "ON"));
    }
}
