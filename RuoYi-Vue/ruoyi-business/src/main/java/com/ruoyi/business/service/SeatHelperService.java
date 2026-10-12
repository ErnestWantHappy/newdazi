package com.ruoyi.business.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.*;
import com.ruoyi.business.mapper.*;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 凭据认证设备、单次凭证关联会话；机位基准只能由教师显式确认。 */
@Service
public class SeatHelperService {
    @Autowired private SeatHelperMapper mapper;
    @Autowired private SeatHelperPairService pairing;
    @Autowired private BizStudentMapper students;
    @Autowired private BizLessonAssignmentMapper assignments;
    @Autowired private GuideSheetAccessService access;
    @Autowired private StringRedisTemplate redis;
    @Autowired private com.ruoyi.common.core.redis.RedisCache sessionCache;
    @Autowired private ObjectMapper json;
    @Value("${seat-helper.enabled:false}") private boolean enabled;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DefaultRedisScript<String> CONSUME = new DefaultRedisScript<>("local v=redis.call('GET',KEYS[1]); if v then redis.call('DEL',KEYS[1]); end; return v",String.class);
    private static final String PREFIX="seat-helper:";

    public boolean isEnabled(Long dept) {
        if (!enabled) return false;
        return schoolEnabled(mapper.school(dept));
    }
    private boolean schoolEnabled(Map<String,Object> school) {
        return enabled && school != null && (Boolean.TRUE.equals(school.get("enabled")) || "1".equals(String.valueOf(school.get("enabled"))));
    }
    private void requireEnabled(Long dept) { if (!isEnabled(dept)) throw new ServiceException("学校尚未启用机位助手"); }
    public Map<String,Object> admin(Long dept) {
        if (!enabled) return map("available",false,"enabled",false,"devices",Collections.emptyList());
        return map("available",true,"school",mapper.school(dept),"devices",mapper.devices(dept));
    }
    public void configure(Long dept,boolean active,String url) {
        if (!enabled) throw new ServiceException("请管理员先完成迁移并启用服务配置");
        if (dept == null || dept <= 0) throw new ServiceException("请选择学校");
        validatePlatformUrl(url); mapper.configure(dept,active,url.replaceAll("/+$",""));
    }
    public static void validatePlatformUrl(String value) {
        try {
            URI uri=new URI(value); String host=uri.getHost();
            // 学校内网可使用现有 HTTP 站点；仍只允许网页根地址，避免配置本地文件或携带凭据的链接。
            if (host == null || uri.getUserInfo()!=null || uri.getQuery()!=null || uri.getFragment()!=null
                    || !("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                    || uri.getPort() == 0 || uri.getPort() > 65535
                    || !(uri.getPath().isEmpty() || "/".equals(uri.getPath()))) throw new Exception();
        } catch(Exception e) { throw new ServiceException("平台地址需为HTTP或HTTPS站点根地址，不含账号、接口路径、查询参数或片段"); }
    }
    public String enrollment(Long dept,String room,String seat) throws Exception {
        requireEnabled(dept); room=label(room,80); seat=label(seat,40);
        if (mapper.activeSeat(dept,room,seat)>0) throw new ServiceException("该机位已有设备，请先撤销旧设备");
        String token=randomToken(); save("enroll:"+hash(token),map("deptId",dept,"roomName",room,"seatName",seat),1800);
        return token;
    }
    @Transactional(rollbackFor=Exception.class)
    public Map<String,Object> register(String token,String fingerprint) throws Exception {
        if (!enabled || token==null || token.length()!=64 || fingerprint==null || !fingerprint.matches("[a-f0-9]{64}")) throw new ServiceException("注册信息无效");
        Map<String,Object> enrollment=consume("enroll:"+hash(token));
        if (enrollment==null) throw new ServiceException("注册码已使用或过期，请管理员重新生成");
        Long dept=number(enrollment.get("deptId")); requireEnabled(dept);
        String room=(String)enrollment.get("roomName"),seat=(String)enrollment.get("seatName");
        // 学校配置行串行化注册，避免同一机位并发注册两个有效设备。
        if (!schoolEnabled(mapper.lockSchool(dept))) throw new ServiceException("学校尚未启用机位助手");
        SeatHelperDevice registered = mapper.fingerprintForUpdate(dept, fingerprint);
        if (registered != null && registered.active)
            throw new ServiceException("这台电脑已经注册，请在机位管理中修改名称，不要重复注册");
        if (!mapper.activeSeatForUpdate(dept,room,seat).isEmpty()) throw new ServiceException("该机位已有设备");
        SeatHelperDevice device=new SeatHelperDevice(); device.deviceId=UUID.randomUUID().toString().replace("-","");
        device.deptId=dept; device.roomName=room; device.seatName=seat; device.fingerprint=fingerprint;
        String secret=randomToken(); device.credentialHash=hash(secret); mapper.register(device);
        return map("deviceId",device.deviceId,"secret",secret,"roomName",room,"seatName",seat,"platformUrl",mapper.school(dept).get("platformUrl"));
    }
    public SeatHelperDevice authenticate(Map<String,Object> request) {
        if (!enabled) throw new ServiceException("机位助手未启用");
        String id=string(request,"deviceId"),secret=string(request,"secret"),fingerprint=string(request,"fingerprint");
        if (!id.matches("[a-f0-9]{32}") || !secret.matches("[a-f0-9]{64}")) throw new ServiceException("设备认证失败");
        SeatHelperDevice device=mapper.device(id);
        if (device==null || !device.active || !MessageDigest.isEqual(device.credentialHash.getBytes(StandardCharsets.US_ASCII),hash(secret).getBytes(StandardCharsets.US_ASCII))
                || !Objects.equals(device.fingerprint,fingerprint)) throw new ServiceException("设备已撤销或身份变化，请管理员重新配置");
        requireEnabled(device.deptId); return device;
    }
    public Map<String,Object> heartbeat(Map<String,Object> request,boolean open) throws Exception {
        SeatHelperDevice device=authenticate(request);
        String ip=string(request,"localIp");
        if (!isPrivateIpv4(ip)) ip="";
        save("live:"+device.deviceId,map("localIp",ip,"at",System.currentTimeMillis()),75);
        if (!open) return map("ok",true);
        String token=randomToken(); save("open:"+hash(token),map("deviceId",device.deviceId),60);
        return map("url",mapper.school(device.deptId).get("platformUrl")+"/student/index#seat-helper="+token,"expiresIn",60);
    }
    public Map<String,Object> associate(String token) throws Exception {
        BizStudent student=access.requireCurrentStudent(); requireEnabled(student.getDeptId());
        if (token==null || !token.matches("[a-f0-9]{64}")) throw new ServiceException("关联凭证无效");
        Map<String,Object> opened=consume("open:"+hash(token));
        if (opened==null) throw new ServiceException("关联凭证已使用或过期，请重新打开桌面快捷方式");
        SeatHelperDevice device=opened.get("pairToken")!=null ? pairing.registerForStudent((String)opened.get("pairToken"),student.getDeptId()) : mapper.device((String)opened.get("deviceId"));
        if (device==null || !device.active || !student.getDeptId().equals(device.deptId)) throw new ServiceException("设备与账号不属于同一学校");
        LoginUser login=SecurityUtils.getLoginUser();
        save("session:"+hash(login.getToken()),map("deviceId",device.deviceId),3600);
        return studentStatus();
    }
    public Map<String,Object> studentStatus() throws Exception {
        BizStudent student=access.requireCurrentStudent();
        if (!isEnabled(student.getDeptId())) return map("enabled",false);
        LoginUser login=SecurityUtils.getLoginUser();
        Map<String,Object> link=read("session:"+hash(login.getToken()));
        if (link==null) return map("enabled",true,"associated",false);
        SeatHelperDevice device=mapper.device((String)link.get("deviceId"));
        if (device==null || !device.active || !student.getDeptId().equals(device.deptId)) return map("enabled",true,"associated",false);
        save("session:"+hash(login.getToken()),link,3600);
        save("student:"+student.getStudentId(),map("deviceId",device.deviceId,"session",login.getToken(),"userId",login.getUserId()),90);
        Map<String,Object> live=read("live:"+device.deviceId);
        return map("enabled",true,"associated",true,"online",live!=null,"roomName",device.roomName,"seatName",device.seatName,"localIp",live==null?null:live.get("localIp"));
    }
    public Map<String,Object> classroom(Long lesson,String year,String clazz,String room) throws Exception {
        Long dept=access.requireViewableLessonClassDept(lesson,year,clazz);
        if (!isEnabled(dept)) return map("enabled",false);
        clazz=com.ruoyi.business.config.ClassroomRoomKey.normalizeClassCode(clazz);
        List<BizStudent> roster=students.selectPaperImportRoster(dept,year,clazz);
        // 移动设备不能使原机房已确认的长期基准从界面消失。
        List<String> rooms=mapper.classroomRooms(dept,year,clazz);
        if (room==null || room.isEmpty()) room=rooms.isEmpty()?"":rooms.get(0);
        Map<Long,String> baseline=new HashMap<>();
        for(Map<String,Object> b:mapper.bindings(dept,room,year,clazz)) baseline.put(number(b.get("studentId")),(String)b.get("deviceId"));
        List<Map<String,Object>> rows=new ArrayList<>(); Map<String,Integer> counts=new HashMap<>();
        for(BizStudent student:roster) {
            SeatHelperDevice device=liveStudentDevice(student);
            if (device!=null) counts.put(device.deviceId,counts.getOrDefault(device.deviceId,0)+1);
            SeatHelperDevice expected=baseline.containsKey(student.getStudentId())?mapper.device(baseline.get(student.getStudentId())):null;
            Map<String,Object> live=device==null?null:read("live:"+device.deviceId);
            String state=device==null?"UNIDENTIFIED":!room.equals(device.roomName)?"OTHER_ROOM":expected==null?"UNBOUND":expected.deviceId.equals(device.deviceId)?"NORMAL":"CHANGED";
            rows.add(map("studentId",student.getStudentId(),"studentName",student.getStudentName(),"deviceId",device==null?null:device.deviceId,"roomName",device==null?null:device.roomName,"seatName",device==null?null:device.seatName,
                    "localIp",live==null?null:live.get("localIp"),"expectedSeat",expected==null?null:expected.seatName,"state",state));
        }
        for(Map<String,Object> row:rows) if(counts.getOrDefault(row.get("deviceId"),0)>1) row.put("state","SHARED_DEVICE");
        return map("enabled",true,"roomName",room,"rooms",rooms,"rows",rows);
    }
    @Transactional(rollbackFor=Exception.class)
    public Map<String,Object> confirm(Long lesson,String year,String clazz,String room,List<Map<String,Object>> expected) throws Exception {
        Long dept=access.requireViewableLessonClassDept(lesson,year,clazz); requireEnabled(dept); room=label(room,80);
        clazz=com.ruoyi.business.config.ClassroomRoomKey.normalizeClassCode(clazz);
        BizLessonAssignment current=assignments.selectCurrentAssignmentForUpdate(year,clazz,dept);
        if (current==null || !Objects.equals(current.getLessonId(),lesson)) throw new ServiceException("历史课堂不能修改长期机位");
        if (!schoolEnabled(mapper.lockSchool(dept))) throw new ServiceException("学校尚未启用机位助手");
        List<Map<String,Object>> bindings=mapper.bindingsForUpdate(dept,room,year,clazz);
        Map<String,Object> context=classroom(lesson,year,clazz,room);
        Map<Long,String> requested=new HashMap<>();
        for(Map<String,Object> item:expected) requested.put(number(item.get("studentId")),(String)item.get("deviceId"));
        List<Map<String,Object>> candidates=new ArrayList<>();
        for(Map<String,Object> row:(List<Map<String,Object>>)context.get("rows")) {
            if (!requested.containsKey(number(row.get("studentId")))) continue;
            if (!Objects.equals(requested.get(number(row.get("studentId"))),row.get("deviceId"))) throw new ServiceException("机位信息已变化，请刷新后重新确认");
            if (Arrays.asList("NORMAL","UNBOUND","CHANGED").contains(row.get("state"))) candidates.add(row);
        }
        // 无法识别、重复登录和缺席学生保留原绑定，不能被当前观测覆盖。
        candidates=preserveOccupiedBindings(candidates,bindings);
        int count=0;
        for(Map<String,Object> row:candidates) {
            mapper.bind(dept,room,year,clazz,number(row.get("studentId")),(String)row.get("deviceId"),SecurityUtils.getUserId()); count++;
        }
        return map("confirmed",count,"preserved",requested.size()-count);
    }
    static List<Map<String,Object>> preserveOccupiedBindings(List<Map<String,Object>> observed,List<Map<String,Object>> bindings) {
        List<Map<String,Object>> valid=new ArrayList<>(observed);
        boolean changed;
        do {
            Set<Long> changing=valid.stream().map(r -> Long.valueOf(r.get("studentId").toString())).collect(java.util.stream.Collectors.toSet());
            Set<String> reserved=new HashSet<>();
            for(Map<String,Object> b:bindings) if(!changing.contains(Long.valueOf(b.get("studentId").toString()))) reserved.add((String)b.get("deviceId"));
            // 一个调整被保留后，其原机位也要保留；反复检查，避免链式换位产生重复基准。
            changed=valid.removeIf(row -> reserved.contains(row.get("deviceId")));
        } while(changed);
        return valid;
    }
    public void revoke(String id) { SeatHelperDevice device=mapper.device(id); if(device==null) throw new ServiceException("设备不存在"); mapper.revoke(id); redis.delete(PREFIX+"live:"+id); }
    @Transactional(rollbackFor=Exception.class)
    public void rename(String id,String room,String seat) {
        SeatHelperDevice device=mapper.device(id);if(device==null||!device.active)throw new ServiceException("设备不存在或已撤销");
        room=label(room,80);seat=label(seat,40);
        mapper.lockSchool(device.deptId);
        if (mapper.activeSeatForUpdate(device.deptId,room,seat).stream().anyMatch(other -> !Objects.equals(id,other)))throw new ServiceException("该机位已有其他电脑，请使用不同机位名称");
        mapper.rename(id,room,seat);
    }
    public String platformUrl(Long dept) {requireEnabled(dept);return (String)mapper.school(dept).get("platformUrl");}
    @Transactional(rollbackFor=Exception.class)
    public void renameInClass(Long lesson,String year,String clazz,String id,String room,String seat) throws Exception {
        Long dept=access.requireViewableLessonClassDept(lesson,year,clazz);requireEnabled(dept);
        BizLessonAssignment assignment=assignments.selectCurrentAssignmentForUpdate(year,com.ruoyi.business.config.ClassroomRoomKey.normalizeClassCode(clazz),dept);
        if(assignment==null||!Objects.equals(assignment.getLessonId(),lesson))throw new ServiceException("历史课堂不能修改机位名称");
        Map<String,Object> current=classroom(lesson,year,clazz,null);
        boolean observed=((List<Map<String,Object>>)current.get("rows")).stream().anyMatch(row -> Objects.equals(id,row.get("deviceId")));
        if(!observed)throw new ServiceException("该电脑不在当前班级的在线机位中，请刷新后再设置");
        rename(id,room,seat);
    }
    private SeatHelperDevice liveStudentDevice(BizStudent student) throws Exception {
        Map<String,Object> link=read("student:"+student.getStudentId()); if(link==null) return null;
        LoginUser login=sessionCache.getCacheObject(CacheConstants.LOGIN_TOKEN_KEY+link.get("session"));
        if(login==null || !Objects.equals(login.getUserId(),number(link.get("userId")))) return null;
        SeatHelperDevice device=mapper.device((String)link.get("deviceId"));
        return device!=null && device.active && student.getDeptId().equals(device.deptId) && read("live:"+device.deviceId)!=null?device:null;
    }
    private Map<String,Object> read(String key) throws Exception { String value=redis.opsForValue().get(PREFIX+key); return value==null?null:json.readValue(value,Map.class); }
    private Map<String,Object> consume(String key) throws Exception { String value=redis.execute(CONSUME,Collections.singletonList(PREFIX+key)); return value==null?null:json.readValue(value,Map.class); }
    private void save(String key,Object value,long seconds) throws Exception { redis.opsForValue().set(PREFIX+key,json.writeValueAsString(value),seconds,TimeUnit.SECONDS); }
    public static boolean isPrivateIpv4(String ip) {
        if(ip==null || !ip.matches("\\d{1,3}(\\.\\d{1,3}){3}")) return false;
        String[] parts=ip.split("\\."); int a=Integer.parseInt(parts[0]),b=Integer.parseInt(parts[1]);
        for(String part:parts) if(Integer.parseInt(part)>255) return false;
        return a==10 || a==192&&b==168 || a==172&&b>=16&&b<=31;
    }
    public static String hash(String value) { try { byte[] bytes=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)); StringBuilder out=new StringBuilder(); for(byte b:bytes) out.append(String.format("%02x",b&255)); return out.toString(); } catch(Exception e) { throw new IllegalStateException(e); } }
    private String randomToken() { byte[] bytes=new byte[32]; RANDOM.nextBytes(bytes); StringBuilder out=new StringBuilder(); for(byte b:bytes) out.append(String.format("%02x",b&255)); return out.toString(); }
    private String label(String value,int max) { if(value==null || value.trim().isEmpty() || value.trim().length()>max) throw new ServiceException("机房或机位名称无效"); return value.trim(); }
    private String string(Map<String,Object> request,String key) { Object value=request==null?null:request.get(key); return value==null?"":String.valueOf(value); }
    private Long number(Object value) { return value==null?null:Long.valueOf(value.toString()); }
    private Map<String,Object> map(Object... values) { Map<String,Object> result=new LinkedHashMap<>(); for(int i=0;i<values.length;i+=2) result.put((String)values[i],values[i+1]); return result; }
}
