package com.ruoyi.business.service;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.SeatHelperDevice;
import com.ruoyi.business.mapper.SeatHelperMapper;
import com.ruoyi.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** 电脑先发起短期配对，学校由现有学生登录确定；浏览器不能领取设备密钥。 */
@Service
public class SeatHelperPairService {
    @Autowired private SeatHelperMapper mapper;
    @Autowired private StringRedisTemplate redis;
    @Autowired private ObjectMapper json;
    @Value("${seat-helper.enabled:false}") private boolean enabled;
    private static final String PREFIX="seat-helper:";
    private static final SecureRandom RANDOM=new SecureRandom();

    public Map<String,Object> begin(Map<String,Object> request) throws Exception {
        if (!enabled) throw new ServiceException("机位助手未启用，网页可正常使用");
        String fingerprint=value(request,"fingerprint"), proof=value(request,"proof");
        requireToken(fingerprint); requireToken(proof);
        String ip=value(request,"localIp");
        String token=randomToken();
        save("pair:"+hash(token),map("fingerprint",fingerprint,"proofHash",hash(proof),"localIp",SeatHelperService.isPrivateIpv4(ip)?ip:""),300);
        save("open:"+hash(token),map("pairToken",token),300);
        return map("token",token,"expiresIn",300);
    }

    public Map<String,Object> poll(Map<String,Object> request,boolean acknowledge) throws Exception {
        String token=value(request,"token");
        Map<String,Object> pending=authenticatePoll(request);
        Map<String,Object> result=read("pair-result:"+hash(token));
        if (result==null) return map("status","WAITING");
        if (acknowledge) {
            redis.delete(Arrays.asList(PREFIX+"pair:"+hash(token),PREFIX+"pair-result:"+hash(token)));
            return map("status","SAVED");
        }
        return map("status","READY","registration",result);
    }

    private Map<String,Object> authenticatePoll(Map<String,Object> request) throws Exception {
        String token=value(request,"token"),proof=value(request,"proof"),fingerprint=value(request,"fingerprint");
        requireToken(token); requireToken(proof); requireToken(fingerprint);
        Map<String,Object> pending=read("pair:"+hash(token));
        if (pending==null || !Objects.equals(pending.get("fingerprint"),fingerprint)
                || !MessageDigest.isEqual(String.valueOf(pending.get("proofHash")).getBytes(StandardCharsets.US_ASCII),hash(proof).getBytes(StandardCharsets.US_ASCII)))
            throw new ServiceException("自动关联已过期，请再次打开助手；网页可正常使用");
        return pending;
    }

    @Transactional(rollbackFor=Exception.class)
    public SeatHelperDevice registerForStudent(String token,Long dept) throws Exception {
        requireToken(token);
        if (!enabled) throw new ServiceException("机位助手未启用");
        Map<String,Object> pending=read("pair:"+hash(token));
        if (pending==null) throw new ServiceException("自动关联已过期，请再次打开助手");
        Map<String,Object> school=mapper.lockSchool(dept);
        if (school==null || !(Boolean.TRUE.equals(school.get("enabled")) || "1".equals(String.valueOf(school.get("enabled")))))
            throw new ServiceException("学校尚未启用机位助手");
        String fingerprint=(String)pending.get("fingerprint");
        SeatHelperDevice device=mapper.fingerprintForUpdate(dept,fingerprint);
        // 撤销是管理员的明确决定，重新下载不能绕过；配置损坏则复用未撤销设备。
        if (device!=null && !device.active) throw new ServiceException("该电脑已被撤销，网页可正常使用；请联系学校管理员");
        String secret=randomToken();
        if (device==null) {
            device=new SeatHelperDevice();device.deviceId=UUID.randomUUID().toString().replace("-","");
            device.deptId=dept;device.fingerprint=fingerprint;device.roomName="未分配机房";device.seatName="电脑-"+fingerprint.substring(0,12);device.active=true;
            device.credentialHash=hash(secret);mapper.register(device);
        } else {device.credentialHash=hash(secret);mapper.updateCredential(device.deviceId,device.credentialHash);}
        final SeatHelperDevice registered=device;
        final Map<String,Object> result=map("deviceId",device.deviceId,"secret",secret,"roomName",device.roomName,"seatName",device.seatName,"platformUrl",school.get("platformUrl"));
        // 密钥只在数据库成功提交后发布，客户端另用随机轮询凭据领取并确认保存。
        afterCommit(() -> {
            try {
                save("pair-result:"+hash(token),result,120);
                save("live:"+registered.deviceId,map("localIp",pending.get("localIp"),"at",System.currentTimeMillis()),75);
            } catch(Exception e) {throw new IllegalStateException("自动关联结果暂不可用，请重新打开助手",e);}
        });
        return device;
    }
    static void afterCommit(Runnable callback) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {callback.run();return;}
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() { @Override public void afterCommit() {callback.run();} });
    }
    private Map<String,Object> read(String key) throws Exception {String text=redis.opsForValue().get(PREFIX+key);return text==null?null:json.readValue(text,Map.class);}
    private void save(String key,Object data,long seconds) throws Exception {redis.opsForValue().set(PREFIX+key,json.writeValueAsString(data),seconds,TimeUnit.SECONDS);}
    private static String value(Map<String,Object> map,String name) {return map==null||map.get(name)==null?"":String.valueOf(map.get(name));}
    private static void requireToken(String value) {if(value==null||!value.matches("[a-f0-9]{64}"))throw new ServiceException("自动关联信息无效");}
    private static String hash(String value) {return SeatHelperService.hash(value);}
    private static String randomToken() {byte[] bytes=new byte[32];RANDOM.nextBytes(bytes);StringBuilder s=new StringBuilder();for(byte b:bytes)s.append(String.format("%02x",b&255));return s.toString();}
    private static Map<String,Object> map(Object... pairs) {Map<String,Object> m=new LinkedHashMap<>();for(int i=0;i<pairs.length;i+=2)m.put((String)pairs[i],pairs[i+1]);return m;}
}
