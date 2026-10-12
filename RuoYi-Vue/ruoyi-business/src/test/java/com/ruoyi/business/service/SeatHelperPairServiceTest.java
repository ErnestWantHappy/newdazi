package com.ruoyi.business.service;
import java.util.*;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.SeatHelperDevice;
import com.ruoyi.business.mapper.SeatHelperMapper;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.*;
import org.springframework.data.redis.core.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class SeatHelperPairServiceTest {
    SeatHelperPairService service;SeatHelperMapper mapper;StringRedisTemplate redis;ValueOperations<String,String> ops;
    Map<String,String> cache;ObjectMapper json=new ObjectMapper();String fp=repeat('a'),proof=repeat('b');
    @BeforeEach void setup() {
        service=new SeatHelperPairService();mapper=mock(SeatHelperMapper.class);redis=mock(StringRedisTemplate.class);ops=mock(ValueOperations.class);cache=new HashMap<>();
        ReflectionTestUtils.setField(service,"mapper",mapper);ReflectionTestUtils.setField(service,"redis",redis);ReflectionTestUtils.setField(service,"json",json);ReflectionTestUtils.setField(service,"enabled",true);
        when(redis.opsForValue()).thenReturn(ops);when(ops.get(anyString())).thenAnswer(call -> cache.get(call.getArgument(0)));
        doAnswer(call -> {cache.put(call.getArgument(0),call.getArgument(1));return null;}).when(ops).set(anyString(),anyString(),anyLong(),any(TimeUnit.class));
        when(redis.delete(anyCollection())).thenAnswer(call -> {for(Object key:(Collection<?>)call.getArgument(0))cache.remove(key);return 1L;});
        when(mapper.lockSchool(9L)).thenReturn(map("enabled",true,"platformUrl","http://10.52.1.130"));
    }
    @AfterEach void cleanup() {if(TransactionSynchronizationManager.isSynchronizationActive())TransactionSynchronizationManager.clearSynchronization();}
    String begin() throws Exception {return (String)service.begin(map("fingerprint",fp,"proof",proof,"localIp","192.168.1.20","deptId",123)).get("token");}
    Map<String,Object> pollRequest(String token) {return map("token",token,"fingerprint",fp,"proof",proof);}
    @Test void beginningDoesNotTrustReportedSchoolOrRegisterDevice() throws Exception {String t=begin();assertEquals(64,t.length());verifyNoInteractions(mapper);assertEquals("WAITING",service.poll(pollRequest(t),false).get("status"));}
    @Test void invalidFingerprintCannotAllocatePairing() {assertThrows(ServiceException.class,()->service.begin(map("fingerprint","invalid","proof",proof)));assertTrue(cache.isEmpty());}
    @Test void disabledGlobalDoesNotStartPair() {ReflectionTestUtils.setField(service,"enabled",false);assertThrows(ServiceException.class,()->begin());assertTrue(cache.isEmpty());}
    @Test void browserTokenAloneCannotClaimDeviceCredential() throws Exception {String t=begin();assertThrows(ServiceException.class,()->service.poll(map("token",t,"fingerprint",fp,"proof",repeat('c')),false));}
    @Test void schoolComesFromAuthenticatedStudentAndIpIsLive() throws Exception {String t=begin();SeatHelperDevice d=service.registerForStudent(t,9L);assertEquals(9L,d.deptId);assertTrue(d.active);assertEquals("未分配机房",d.roomName);assertTrue(cache.containsKey("seat-helper:live:"+d.deviceId));verify(mapper).register(d);}
    @Test void unknownOrExpiredPairCannotRegister() {assertThrows(ServiceException.class,()->service.registerForStudent(repeat('c'),9L));verifyNoInteractions(mapper);}
    @Test void disabledSchoolCannotRegister() throws Exception {String t=begin();when(mapper.lockSchool(9L)).thenReturn(map("enabled",false));assertThrows(ServiceException.class,()->service.registerForStudent(t,9L));verify(mapper,never()).register(any());}
    SeatHelperDevice existing(boolean active) {SeatHelperDevice d=new SeatHelperDevice();d.deviceId="existing";d.deptId=9L;d.active=active;d.fingerprint=fp;d.roomName="机房A";d.seatName="01";return d;}
    @Test void lostConfigReusesDeviceAndLabelsInsteadOfDuplicatingSeat() throws Exception {String t=begin();SeatHelperDevice d=existing(true);when(mapper.fingerprintForUpdate(9L,fp)).thenReturn(d);assertSame(d,service.registerForStudent(t,9L));assertEquals("01",d.seatName);verify(mapper).updateCredential(eq("existing"),anyString());verify(mapper,never()).register(any());}
    @Test void revokedDeviceCannotReenrollByRedownloading() throws Exception {String t=begin();when(mapper.fingerprintForUpdate(9L,fp)).thenReturn(existing(false));assertThrows(ServiceException.class,()->service.registerForStudent(t,9L));verify(mapper,never()).updateCredential(anyString(),anyString());assertEquals("WAITING",service.poll(pollRequest(t),false).get("status"));}
    @Test void credentialIsPublishedOnlyAfterDatabaseCommit() throws Exception {
        String t=begin();TransactionSynchronizationManager.initSynchronization();service.registerForStudent(t,9L);
        assertEquals("WAITING",service.poll(pollRequest(t),false).get("status"));
        for(TransactionSynchronization sync:TransactionSynchronizationManager.getSynchronizations())sync.afterCommit();
        assertEquals("READY",service.poll(pollRequest(t),false).get("status"));
    }
    @Test void rollbackDoesNotPublishCredential() throws Exception {String t=begin();TransactionSynchronizationManager.initSynchronization();service.registerForStudent(t,9L);for(TransactionSynchronization sync:TransactionSynchronizationManager.getSynchronizations())sync.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);assertEquals("WAITING",service.poll(pollRequest(t),false).get("status"));}
    @Test void failedInsertDoesNotPublishCredential() throws Exception {String t=begin();doThrow(new IllegalStateException("插入失败")).when(mapper).register(any());assertThrows(IllegalStateException.class,()->service.registerForStudent(t,9L));assertEquals("WAITING",service.poll(pollRequest(t),false).get("status"));}
    @Test void resultCanBeRetriedBeforeSaveAndIsDestroyedAfterAck() throws Exception {String t=begin();service.registerForStudent(t,9L);assertEquals(service.poll(pollRequest(t),false),service.poll(pollRequest(t),false));assertEquals("SAVED",service.poll(pollRequest(t),true).get("status"));assertThrows(ServiceException.class,()->service.poll(pollRequest(t),false));}
    static String repeat(char c) {char[] s=new char[64];Arrays.fill(s,c);return new String(s);}
    static Map<String,Object> map(Object... pairs) {Map<String,Object> m=new HashMap<>();for(int i=0;i<pairs.length;i+=2)m.put((String)pairs[i],pairs[i+1]);return m;}
}
