package com.ruoyi.business.controller;

import java.util.*;
import java.nio.file.*;
import javax.servlet.http.HttpServletResponse;
import com.ruoyi.business.service.SeatHelperService;
import com.ruoyi.business.service.GuideSheetAccessService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.file.FileUtils;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/business/seat-helper")
public class SeatHelperController {
    @Autowired private SeatHelperService service;
    @Autowired private com.ruoyi.business.service.SeatHelperPairService pairing;
    @Autowired private GuideSheetAccessService access;
    @Value("${seat-helper.client-path:clients/seat-helper/dist/SeatHelper.exe}") private String clientPath;
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/admin") public AjaxResult admin(@RequestParam Long deptId) { return AjaxResult.success(service.admin(deptId)); }
    @PreAuthorize("@ss.hasRole('admin')")
    @PostMapping("/admin/configure") public AjaxResult configure(@RequestBody Configure request) { service.configure(request.deptId,request.enabled,request.platformUrl); return AjaxResult.success(); }
    @PreAuthorize("@ss.hasRole('admin')")
    @PostMapping("/admin/enrollment") public AjaxResult enrollment(@RequestBody Configure request) throws Exception { return AjaxResult.success(Collections.singletonMap("code",service.enrollment(request.deptId,request.roomName,request.seatName))); }
    @PreAuthorize("@ss.hasRole('admin')")
    @PostMapping("/admin/revoke/{id}") public AjaxResult revoke(@PathVariable String id) { service.revoke(id); return AjaxResult.success(); }
    @PreAuthorize("@ss.hasRole('admin')")
    @PostMapping("/admin/device") public AjaxResult rename(@RequestBody DeviceName request) {service.rename(request.deviceId,request.roomName,request.seatName);return AjaxResult.success();}
    @Anonymous
    @PostMapping("/device/pair/start") public AjaxResult start(@RequestBody Map<String,Object> request) throws Exception {return AjaxResult.success(pairing.begin(request));}
    @Anonymous
    @PostMapping("/device/pair/poll") public AjaxResult poll(@RequestBody Map<String,Object> request) throws Exception {return AjaxResult.success(pairing.poll(request,false));}
    @Anonymous
    @PostMapping("/device/pair/ack") public AjaxResult acknowledge(@RequestBody Map<String,Object> request) throws Exception {return AjaxResult.success(pairing.poll(request,true));}
    @Anonymous
    @PostMapping("/device/register") public AjaxResult register(@RequestBody Map<String,String> request) throws Exception { return AjaxResult.success(service.register(request.get("code"),request.get("fingerprint"))); }
    @Anonymous
    @PostMapping("/device/heartbeat") public AjaxResult heartbeat(@RequestBody Map<String,Object> request) throws Exception { return AjaxResult.success(service.heartbeat(request,false)); }
    @Anonymous
    @PostMapping("/device/open") public AjaxResult open(@RequestBody Map<String,Object> request) throws Exception { return AjaxResult.success(service.heartbeat(request,true)); }
    @PreAuthorize("@studentSs.isStudent()")
    @PostMapping("/student/associate") public AjaxResult associate(@RequestBody Map<String,String> request) throws Exception { return AjaxResult.success(service.associate(request.get("token"))); }
    @PreAuthorize("@studentSs.isStudent()")
    @GetMapping("/student/status") public AjaxResult status() throws Exception { return AjaxResult.success(service.studentStatus()); }
    @PreAuthorize("@studentSs.isStudent()")
    @GetMapping("/student/download") public void download(HttpServletResponse response) throws Exception {
        Long dept=access.requireCurrentStudent().getDeptId();
        if (!service.isEnabled(dept)) throw new ServiceException("学校尚未启用机位助手");
        String site=service.platformUrl(dept);
        Path path=Paths.get(clientPath).toAbsolutePath().normalize();
        if (!Files.isRegularFile(path)) throw new ServiceException("客户端安装包尚未配置，请联系管理员");
        response.setContentType("application/octet-stream"); response.setHeader("X-Content-Type-Options","nosniff");
        FileUtils.setAttachmentResponseHeader(response,"SeatHelper.exe"); Files.copy(path,response.getOutputStream());
        // PE尾部只附带公开站点信息，不含学生密码、登录Token或设备注册码。
        byte[] bootstrap=new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsBytes(Collections.singletonMap("platformUrl",site));
        response.getOutputStream().write(bootstrap);
        response.getOutputStream().write(java.nio.ByteBuffer.allocate(4).order(java.nio.ByteOrder.LITTLE_ENDIAN).putInt(bootstrap.length).array());
        response.getOutputStream().write("SEATHELPER-SITE01".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
    }
    @PreAuthorize("@ss.hasRole('teacher') or @ss.hasRole('admin')")
    @GetMapping("/classroom") public AjaxResult classroom(@RequestParam Long lessonId,@RequestParam String entryYear,@RequestParam String classCode,@RequestParam(required=false) String roomName) throws Exception { return AjaxResult.success(service.classroom(lessonId,entryYear,classCode,roomName)); }
    @PreAuthorize("@ss.hasRole('teacher') or @ss.hasRole('admin')")
    @PostMapping("/classroom/confirm") public AjaxResult confirm(@RequestBody Confirm request) throws Exception {
        if (request.students==null || request.students.size()>200) throw new ServiceException("请刷新机位信息后确认");
        return AjaxResult.success(service.confirm(request.lessonId,request.entryYear,request.classCode,request.roomName,request.students));
    }
    public static class Configure { public Long deptId; public boolean enabled; public String platformUrl,roomName,seatName; }
    @PreAuthorize("@ss.hasRole('teacher') or @ss.hasRole('admin')")
    @PostMapping("/classroom/device") public AjaxResult renameInClass(@RequestBody DeviceName request) throws Exception {service.renameInClass(request.lessonId,request.entryYear,request.classCode,request.deviceId,request.roomName,request.seatName);return AjaxResult.success();}
    public static class DeviceName {public Long lessonId;public String entryYear,classCode,deviceId,roomName,seatName;}
    public static class Confirm { public Long lessonId; public String entryYear,classCode,roomName; public List<Map<String,Object>> students; }
}
