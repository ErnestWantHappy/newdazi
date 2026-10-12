package com.ruoyi.business.service;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.ruoyi.common.exception.ServiceException;
class SeatHelperPolicyTest {
    @Test void rejectedSeatChangeAlsoPreservesItsOldSeatForOtherStudents() {
        java.util.List<java.util.Map<String,Object>> bindings=java.util.Arrays.asList(row(1,"a"),row(2,"b"),row(3,"c"));
        java.util.List<java.util.Map<String,Object>> changes=java.util.Arrays.asList(row(1,"b"),row(2,"c"));
        assertTrue(SeatHelperService.preserveOccupiedBindings(changes,bindings).isEmpty());
        assertEquals(2,SeatHelperService.preserveOccupiedBindings(java.util.Arrays.asList(row(1,"b"),row(2,"a")),bindings).size());
    }
    private java.util.Map<String,Object> row(long student,String device) {
        java.util.Map<String,Object> row=new java.util.HashMap<>(); row.put("studentId",student); row.put("deviceId",device); return row;
    }
    @Test void onlyPrivateValidIpv4IsShownAsLanAddress() {
        assertTrue(SeatHelperService.isPrivateIpv4("10.52.1.20"));
        assertTrue(SeatHelperService.isPrivateIpv4("172.16.1.1"));
        assertTrue(SeatHelperService.isPrivateIpv4("192.168.1.1"));
        assertFalse(SeatHelperService.isPrivateIpv4("172.32.1.1"));
        assertFalse(SeatHelperService.isPrivateIpv4("192.168.1.999"));
        assertFalse(SeatHelperService.isPrivateIpv4("127.0.0.1"));
        assertFalse(SeatHelperService.isPrivateIpv4("8.8.8.8"));
    }
    @Test void schoolPlatformSupportsExistingHttpAndHttpsSites() {
        SeatHelperService.validatePlatformUrl("https://example.edu.cn");
        SeatHelperService.validatePlatformUrl("http://localhost:5173");
        SeatHelperService.validatePlatformUrl("http://10.52.1.130");
        SeatHelperService.validatePlatformUrl("http://example.edu.cn");
        SeatHelperService.validatePlatformUrl("http://10.1.1.1:3010/");
        assertThrows(ServiceException.class,()->SeatHelperService.validatePlatformUrl("https://example.edu.cn/student?token=1"));
        assertThrows(ServiceException.class,()->SeatHelperService.validatePlatformUrl("https://user:password@example.edu.cn"));
        assertThrows(ServiceException.class,()->SeatHelperService.validatePlatformUrl("file:///C:/Windows/notepad.exe"));
        assertThrows(ServiceException.class,()->SeatHelperService.validatePlatformUrl("javascript:alert(1)"));
        assertThrows(ServiceException.class,()->SeatHelperService.validatePlatformUrl("http://10.52.1.130#token"));
        assertThrows(ServiceException.class,()->SeatHelperService.validatePlatformUrl("http://10.52.1.130:65536"));
        assertThrows(ServiceException.class,()->SeatHelperService.validatePlatformUrl("http://10.52.1.130:0"));
    }
}
