package com.ruoyi.web.controller.monitor;
import java.util.*;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.framework.web.service.OnlinePresenceService;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.system.service.ISysDeptService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SysUserOnlineControllerTest {
    private JSONObject row(long id, long dept) {
        JSONObject row = new JSONObject(); row.put("userId", id); row.put("userName", "student" + id);
        row.put("deptId", dept); row.put("ipaddr", "10.0.0.1"); return row;
    }
    @Test void departmentIncludesDescendantsWithoutSubstringConfusion() {
        SysDept dept = new SysDept(); dept.setAncestors("0,1,12");
        assertTrue(SysUserOnlineController.matches(row(1,123), null,null,12L,dept));
        assertFalse(SysUserOnlineController.matches(row(1,123), null,null,2L,dept));
        assertTrue(SysUserOnlineController.matches(row(1,123), null,null,123L,dept));
    }
    @Test void filteringPrecedesPaginationAndSharedIpDoesNotMergeAccounts() {
        SysUserOnlineController controller = new SysUserOnlineController();
        OnlinePresenceService presence = mock(OnlinePresenceService.class);
        ISysDeptService depts = mock(ISysDeptService.class);
        ReflectionTestUtils.setField(controller,"presence",presence); ReflectionTestUtils.setField(controller,"deptService",depts);
        when(depts.selectDeptList(any())).thenReturn(Collections.emptyList());
        when(presence.activeUsers()).thenReturn(Arrays.asList(row(1,12),row(2,13),row(3,12)));
        Map<String,Object> result = controller.list("10.0.0.1",null,12L,2,1);
        assertEquals(3,result.get("onlineTotal")); assertEquals(2,result.get("total"));
        List<JSONObject> rows = (List<JSONObject>)result.get("rows");
        assertEquals(3L,rows.get(0).getLongValue("userId"));
        assertEquals(0,((List<?>)controller.list(null,null,null,Integer.MAX_VALUE,100).get("rows")).size());
    }
    @Test void forceLogoutUsesTokenServiceToRemovePresence() {
        SysUserOnlineController controller = new SysUserOnlineController(); TokenService tokens=mock(TokenService.class);
        ReflectionTestUtils.setField(controller,"tokenService",tokens); controller.forceLogout("session"); verify(tokens).delLoginUser("session");
    }
}
