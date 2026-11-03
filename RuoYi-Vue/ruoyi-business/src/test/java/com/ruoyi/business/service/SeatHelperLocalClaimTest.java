package com.ruoyi.business.service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.*;
import com.ruoyi.business.mapper.*;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.*;
import org.springframework.data.redis.core.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class SeatHelperLocalClaimTest {
    SeatHelperService service; SeatHelperMapper mapper; GuideSheetAccessService access;
    BizStudentMapper students; BizLessonAssignmentMapper assignments; StringRedisTemplate redis;
    Map<String,String> cache; ObjectMapper json=new ObjectMapper(); BizStudent a,b; SeatHelperDevice device;
    LoginUser login; Set<String> deadTokens=new HashSet<>(); long clock;
    @BeforeEach void setup() throws Exception {
        service=new SeatHelperService();mapper=mock(SeatHelperMapper.class);access=mock(GuideSheetAccessService.class);
        students=mock(BizStudentMapper.class);assignments=mock(BizLessonAssignmentMapper.class);redis=mock(StringRedisTemplate.class);
        RedisCache sessions=mock(RedisCache.class);ValueOperations<String,String> ops=mock(ValueOperations.class);cache=new HashMap<>();
        ReflectionTestUtils.setField(service,"mapper",mapper);ReflectionTestUtils.setField(service,"access",access);
        ReflectionTestUtils.setField(service,"students",students);ReflectionTestUtils.setField(service,"assignments",assignments);
        ReflectionTestUtils.setField(service,"redis",redis);ReflectionTestUtils.setField(service,"sessionCache",sessions);
        ReflectionTestUtils.setField(service,"json",json);ReflectionTestUtils.setField(service,"enabled",true);
        when(redis.opsForValue()).thenReturn(ops);when(ops.get(anyString())).thenAnswer(c->cache.get(c.getArgument(0)));
        doAnswer(c->{cache.put(c.getArgument(0),c.getArgument(1));return null;}).when(ops).set(anyString(),anyString(),anyLong(),any(TimeUnit.class));
        when(redis.execute(any(org.springframework.data.redis.core.script.RedisScript.class),anyList())).thenAnswer(c->{List<String> keys=c.getArgument(1);return cache.remove(keys.get(0));});
        when(redis.delete(anyString())).thenAnswer(c->cache.remove(c.getArgument(0))!=null);
        a=student(1);b=student(2);device=new SeatHelperDevice();device.deviceId="d";device.deptId=9L;device.active=true;device.roomName="未分配机房";device.seatName="电脑-aaaaaaaaaaaa";
        when(mapper.device("d")).thenReturn(device);when(mapper.school(9L)).thenReturn(map("enabled",true));when(mapper.lockSchool(9L)).thenReturn(map("enabled",true));
        when(access.requireViewableLessonClassDept(10L,"2026","1")).thenReturn(9L);
        when(students.selectPaperImportRoster(9L,"2026","1")).thenReturn(Arrays.asList(a,b));
        when(sessions.getCacheObject(anyString())).thenAnswer(c->{String key=c.getArgument(0);if(deadTokens.contains(key))return null;LoginUser u=new LoginUser();u.setUserId(key.endsWith("session-b")?2L:1L);return u;});
        BizLessonAssignment assignment=new BizLessonAssignment();assignment.setLessonId(10L);
        when(assignments.selectCurrentAssignmentForUpdate("2026","1",9L)).thenReturn(assignment);
        put("live:d",map("localIp","192.168.16.101"));signIn(a,"session-a");
    }
    @AfterEach void cleanup() {SecurityContextHolder.clearContext();}
    BizStudent student(long id) {BizStudent s=new BizStudent();s.setStudentId(id);s.setDeptId(9L);s.setStudentName("学生"+id);return s;}
    void signIn(BizStudent s,String token) {signIn(s,token,++clock);}
    void signIn(BizStudent s,String token,long at) {login=new LoginUser();login.setToken(token);login.setUserId(s.getStudentId());login.setLoginTime(at);SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(login,null,Collections.emptyList()));when(access.requireCurrentStudent()).thenReturn(s);}
    void put(String key,Map<String,Object> value) throws Exception {cache.put("seat-helper:"+key,json.writeValueAsString(value));}
    Map<String,Object> get(String key) throws Exception {return json.readValue(cache.get("seat-helper:"+key),Map.class);}
    String claim(char character) throws Exception {char[] chars=new char[64];Arrays.fill(chars,character);String token=new String(chars);put("open:"+SeatHelperService.hash(token),map("deviceId","d"));service.associate(token);return token;}
    Map<String,Object> browserClaim(char value,String browser,boolean preferred,long until) throws Exception {
        String token=String.join("",Collections.nCopies(64,String.valueOf(value)));
        put("open:"+SeatHelperService.hash(token),map("deviceId","d","browserKey",browser,"preferredBrowser",preferred,"browserUntil",until));
        return service.associate(token);
    }
    @Test void defaultBrowserTakesOverAndOtherBrowserCannotStealBack() throws Exception {
        long until=System.currentTimeMillis()+75000;
        browserClaim('a',"chrome",false,until);
        signIn(b,"session-b");assertEquals(true,browserClaim('b',"edge",true,until).get("associated"));
        signIn(a,"session-a");assertEquals(true,browserClaim('c',"chrome",false,until).get("browserDeferred"));
        assertEquals("session-b",get("owner:d").get("session"));
        // 普通状态轮询不能使旧账号覆盖所有者或延长浏览器优先权。
        assertEquals(true,service.studentStatus().get("browserDeferred"));
        assertEquals("session-b",get("owner:d").get("session"));
    }
    @Test void authenticatedDeviceTicketCarriesPriorityNotStudentInput() throws Exception {
        SeatHelperService issued=spy(service);doReturn(device).when(issued).authenticate(anyMap());
        when(mapper.school(9L)).thenReturn(map("enabled",true,"platformUrl","http://10.52.1.130"));
        String browser=String.join("",Collections.nCopies(64,"a"));
        Map<String,Object> opened=issued.heartbeat(map("browserKey",browser,"preferredBrowser",true,"localIp","192.168.16.101"),true);
        String token=opened.get("url").toString().split("#seat-helper=")[1];
        Map<String,Object> ticket=get("open:"+SeatHelperService.hash(token));
        assertEquals(browser,ticket.get("browserKey"));assertEquals(true,ticket.get("preferredBrowser"));
        assertTrue(((Number)ticket.get("browserUntil")).longValue()>System.currentTimeMillis()+70000);
        opened=issued.heartbeat(map("browserKey","invalid","preferredBrowser",true),true);
        token=opened.get("url").toString().split("#seat-helper=")[1];
        assertNull(get("open:"+SeatHelperService.hash(token)).get("browserKey"));
    }
    @Test void priorityExpiresAndNewStudentInSameBrowserCanClaim() throws Exception {
        browserClaim('a',"edge",true,System.currentTimeMillis()-1);
        signIn(b,"session-b");assertEquals(true,browserClaim('b',"chrome",false,System.currentTimeMillis()+75000).get("associated"));
        signIn(a,"session-a");assertEquals(true,browserClaim('c',"chrome",false,System.currentTimeMillis()+75000).get("associated"));
        assertEquals("session-a",get("owner:d").get("session"));
    }
    @Test void samePriorityKeepsFirstBrowserAndLegacyLinksCannotOverrideDefault() throws Exception {
        browserClaim('a',"chrome",false,System.currentTimeMillis()+75000);
        signIn(b,"session-b");assertEquals(true,browserClaim('b',"firefox",false,System.currentTimeMillis()+75000).get("browserDeferred"));
        browserClaim('c',"edge",true,System.currentTimeMillis()+75000);
        signIn(a,"session-a");claim('d');assertEquals("session-b",get("owner:d").get("session"));
    }
    List<Map<String,Object>> rows(String room) throws Exception {return (List<Map<String,Object>>)service.classroom(10L,"2026","1",room).get("rows");}
    @Test void secondStudentClaimsRunningDeviceAndOldObservationDisappears() throws Exception {
        claim('a');assertEquals(true,service.studentStatus().get("associated"));
        signIn(b,"session-b");claim('b');assertEquals("192.168.16.101",service.studentStatus().get("localIp"));
        assertNull(rows("创新机房1").get(0).get("deviceId"));assertEquals("d",rows("创新机房1").get(1).get("deviceId"));
        signIn(a,"session-a");assertEquals(false,service.studentStatus().get("associated"));
    }
    @Test void replayedAndUnknownTokenCannotClaimDevice() throws Exception {
        String token=claim('a');assertThrows(ServiceException.class,()->service.associate(token));
        assertThrows(ServiceException.class,()->service.associate(String.join("",Collections.nCopies(64,"c"))));
    }
    @Test void foreignSchoolAndRevokedDeviceCannotClaim() throws Exception {
        device.deptId=8L;assertThrows(ServiceException.class,()->claim('a'));
        device.deptId=9L;device.active=false;assertThrows(ServiceException.class,()->claim('b'));
        assertFalse(cache.containsKey("seat-helper:session:"+SeatHelperService.hash("session-a")));
    }
    @Test void missingHeartbeatNeverBecomesIdentifiedIp() throws Exception {
        claim('a');cache.remove("seat-helper:live:d");assertEquals("UNIDENTIFIED",rows("创新机房1").get(0).get("state"));
        assertNull(rows("创新机房1").get(0).get("localIp"));
    }
    @Test void batchSaveAssignsRoomAndSnapshotsIpWithoutNamingEveryComputer() throws Exception {
        claim('a');Map<String,Object> result=service.confirm(10L,"2026","1","创新机房1",Arrays.asList(map("studentId",1L,"deviceId","d","localIp","192.168.16.101")));
        assertEquals(1,result.get("confirmed"));verify(mapper).rename("d","创新机房1","101机");
        verify(mapper).bind(9L,"创新机房1","2026","1",1L,"d","192.168.16.101","101机",1L);
    }
    @Test void oldIpAndHistoricalClassCannotOverwriteBaseline() throws Exception {
        claim('a');assertThrows(ServiceException.class,()->service.confirm(10L,"2026","1","创新机房1",Arrays.asList(map("studentId",1L,"deviceId","d","localIp","192.168.16.102"))));
        when(assignments.selectCurrentAssignmentForUpdate("2026","1",9L)).thenReturn(null);
        assertThrows(ServiceException.class,()->service.confirm(10L,"2026","1","创新机房1",Collections.emptyList()));
        verify(mapper,never()).bind(anyLong(),anyString(),anyString(),anyString(),anyLong(),anyString(),anyString(),anyString(),anyLong());
    }
    @Test void duplicateIpCannotBeSavedAsTwoSeats() throws Exception {
        claim('a');SeatHelperDevice other=new SeatHelperDevice();other.deviceId="e";other.deptId=9L;other.active=true;other.roomName="未分配机房";other.seatName="电脑-bbbbbbbbbbbb";
        when(mapper.device("e")).thenReturn(other);put("live:e",map("localIp","192.168.16.101"));put("student:2",map("deviceId","e","session","session-b","userId",2L));
        put("latest:2",map("session","session-b","at",2L));put("owner:e",map("studentId",2L,"session","session-b"));
        assertEquals("SHARED_DEVICE",rows("创新机房1").get(0).get("state"));
        assertEquals(0,service.confirm(10L,"2026","1","创新机房1",Arrays.asList(map("studentId",1L,"deviceId","d"),map("studentId",2L,"deviceId","e"))).get("confirmed"));
    }
    @Test void recentRoomAndIpMismatchUseConfirmationSnapshot() throws Exception {
        claim('a');when(mapper.recentClassroom(9L,"2026","1")).thenReturn("创新机房1");
        when(mapper.bindings(9L,"创新机房1","2026","1")).thenReturn(Arrays.asList(map("studentId",1L,"deviceId","d","localIp","192.168.16.1","seatName","01机")));
        Map<String,Object> classroom=service.classroom(10L,"2026","1",null);
        assertEquals("创新机房1",classroom.get("roomName"));Map<String,Object> row=((List<Map<String,Object>>)classroom.get("rows")).get(0);
        assertEquals("IP_MISMATCH",row.get("state"));assertEquals("01机",row.get("expectedSeat"));
    }
    @Test void noIpSnapshotStillUsesDeviceBaselineUntilTeacherResaves() {
        assertEquals("NORMAL",SeatHelperService.observationState(device,map("deviceId","d"),"192.168.16.101"));
        assertEquals("CHANGED",SeatHelperService.observationState(device,map("deviceId","e"),"192.168.16.101"));
        assertEquals("UNIDENTIFIED",SeatHelperService.observationState(device,map("deviceId","d"),"8.8.8.8"));
    }
    @Test void supersededLoginCannotRenewStudentObservation() throws Exception {
        claim('a');signIn(a,"session-a2",2);
        assertEquals(false,service.studentStatus().get("associated"));
        signIn(a,"session-a",1);
        Map<String,Object> status=service.studentStatus();
        assertEquals(false,status.get("associated"));assertEquals(true,status.get("superseded"));
        assertFalse(cache.containsKey("seat-helper:student:1"));
        assertNull(rows("创新机房1").get(0).get("deviceId"));
    }
    @Test void newestLoginWithoutAssistantClearsOldSeatCard() throws Exception {
        claim('a');signIn(a,"session-a2");
        Map<String,Object> status=service.studentStatus();
        assertEquals(false,status.get("associated"));
        assertFalse(cache.containsKey("seat-helper:student:1"));
        Map<String,Object> row=rows("创新机房1").get(0);
        assertNull(row.get("deviceId"));assertEquals("UNIDENTIFIED",row.get("state"));
    }
    @Test void ownerlessDeviceIsNotRecognized() throws Exception {
        claim('a');cache.remove("seat-helper:owner:d");
        assertEquals("UNIDENTIFIED",rows("创新机房1").get(0).get("state"));
        assertEquals(false,service.studentStatus().get("associated"));
    }
    @Test void supersededAssociateDoesNotOverwriteOwnerOrBurnTicket() throws Exception {
        claim('a');signIn(a,"session-a2",2);
        assertEquals(false,service.studentStatus().get("associated"));
        signIn(a,"session-a",1);
        String token=String.join("",Collections.nCopies(64,"c"));
        put("open:"+SeatHelperService.hash(token),map("deviceId","d"));
        Map<String,Object> result=service.associate(token);
        assertEquals(false,result.get("associated"));assertEquals(true,result.get("superseded"));
        assertEquals("session-a",get("owner:d").get("session"));
        assertNotNull(get("open:"+SeatHelperService.hash(token)));
    }
    @Test void deadLatestSessionReleasesOwnership() throws Exception {
        claim('a');deadTokens.add("login_tokens:session-a");
        signIn(a,"session-a2",0);
        String token=String.join("",Collections.nCopies(64,"b"));
        put("open:"+SeatHelperService.hash(token),map("deviceId","d"));
        assertEquals(true,service.associate(token).get("associated"));
        assertEquals("session-a2",get("latest:1").get("session"));
        assertEquals("d",rows("创新机房1").get(0).get("deviceId"));
    }
    @Test void legacyRedisWithoutLatestRecoversOnNextAssistedPoll() throws Exception {
        claim('a');cache.remove("seat-helper:latest:1");
        assertNull(rows("创新机房1").get(0).get("deviceId"));
        signIn(a,"session-a",1);
        assertEquals(true,service.studentStatus().get("associated"));
        assertEquals("d",rows("创新机房1").get(0).get("deviceId"));
    }
    Map<String,Object> upgradeRequest() {
        String id=String.join("",Collections.nCopies(32,"d")),secret=String.join("",Collections.nCopies(64,"a"));
        device.deviceId=id;device.credentialHash=SeatHelperService.hash(secret);device.fingerprint=String.join("",Collections.nCopies(64,"b"));
        when(mapper.device(id)).thenReturn(device);
        return map("deviceId",id,"secret",secret,"fingerprint",device.fingerprint,"nextFingerprint",String.join("",Collections.nCopies(64,"c")));
    }
    @Test void authenticatedFingerprintUpgradePreservesDeviceIdAndBaseline() {
        Map<String,Object> request=upgradeRequest();service.upgradeIdentity(request);
        verify(mapper).updateFingerprint(device.deviceId,(String)request.get("nextFingerprint"));verify(mapper,never()).register(any());
    }
    @Test void fingerprintUpgradeRejectsWrongSecretAndRevokedDevice() {
        Map<String,Object> request=upgradeRequest();request.put("secret",String.join("",Collections.nCopies(64,"f")));
        assertThrows(ServiceException.class,()->service.upgradeIdentity(request));final Map<String,Object> revoked=upgradeRequest();device.active=false;
        assertThrows(ServiceException.class,()->service.upgradeIdentity(revoked));
        verify(mapper,never()).updateFingerprint(anyString(),anyString());
    }
    @Test void fingerprintUpgradeCannotTakeAnotherRegisteredIdentity() {
        Map<String,Object> request=upgradeRequest();SeatHelperDevice other=new SeatHelperDevice();other.deviceId="other";
        when(mapper.fingerprintForUpdate(9L,(String)request.get("nextFingerprint"))).thenReturn(other);
        assertThrows(ServiceException.class,()->service.upgradeIdentity(request));verify(mapper,never()).updateFingerprint(anyString(),anyString());
    }
    @Test void clonedOldCredentialMustBeRevalidatedAfterTakingSchoolLock() {
        Map<String,Object> request=upgradeRequest();
        when(mapper.lockSchool(9L)).thenAnswer(c->{device.fingerprint=String.join("",Collections.nCopies(64,"e"));return map("enabled",true);});
        assertThrows(ServiceException.class,()->service.upgradeIdentity(request));verify(mapper,never()).updateFingerprint(anyString(),anyString());
    }
    static Map<String,Object> map(Object... pairs) {Map<String,Object> out=new HashMap<>();for(int i=0;i<pairs.length;i+=2)out.put((String)pairs[i],pairs[i+1]);return out;}
}
