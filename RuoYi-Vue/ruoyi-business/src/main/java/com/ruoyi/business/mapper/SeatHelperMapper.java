package com.ruoyi.business.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.*;
import com.ruoyi.business.domain.SeatHelperDevice;

public interface SeatHelperMapper {
    @Select("SELECT dept_id deptId,enabled,platform_url platformUrl FROM biz_seat_helper_school WHERE dept_id=#{dept}")
    Map<String,Object> school(Long dept);
    @Select("SELECT dept_id deptId,enabled,platform_url platformUrl FROM biz_seat_helper_school WHERE dept_id=#{dept} FOR UPDATE")
    Map<String,Object> lockSchool(Long dept);
    @Insert("INSERT INTO biz_seat_helper_school(dept_id,enabled,platform_url) VALUES(#{dept},#{enabled},#{url}) ON DUPLICATE KEY UPDATE enabled=VALUES(enabled),platform_url=VALUES(platform_url),updated_at=NOW()")
    int configure(@Param("dept") Long dept,@Param("enabled") boolean enabled,@Param("url") String url);
    String DEVICE_COLUMNS="device_id deviceId,dept_id deptId,room_name roomName,seat_name seatName,credential_hash credentialHash,fingerprint,active";
    @Select("SELECT "+DEVICE_COLUMNS+" FROM biz_seat_helper_device WHERE device_id=#{id}")
    SeatHelperDevice device(String id);
    @Select("SELECT "+DEVICE_COLUMNS+" FROM biz_seat_helper_device WHERE dept_id=#{dept} AND fingerprint=#{fingerprint} AND active=1 ORDER BY registered_at DESC,device_id DESC LIMIT 1 FOR UPDATE")
    SeatHelperDevice fingerprintForUpdate(@Param("dept") Long dept,@Param("fingerprint") String fingerprint);
    @Update("UPDATE biz_seat_helper_device SET credential_hash=#{hash} WHERE device_id=#{id} AND active=1")
    int updateCredential(@Param("id") String id,@Param("hash") String hash);
    @Update("UPDATE biz_seat_helper_device SET room_name=#{room},seat_name=#{seat} WHERE device_id=#{id} AND active=1")
    int rename(@Param("id") String id,@Param("room") String room,@Param("seat") String seat);
    @Select("SELECT device_id deviceId,room_name roomName,seat_name seatName,active,registered_at registeredAt FROM biz_seat_helper_device WHERE dept_id=#{dept} ORDER BY room_name,seat_name,registered_at")
    List<Map<String,Object>> devices(Long dept);
    @Select("SELECT room_name FROM biz_seat_helper_device WHERE dept_id=#{dept} AND active=1 UNION SELECT room_name FROM biz_seat_helper_binding WHERE dept_id=#{dept} AND entry_year=#{year} AND class_code=#{clazz} ORDER BY room_name")
    List<String> classroomRooms(@Param("dept") Long dept,@Param("year") String year,@Param("clazz") String clazz);
    @Select("SELECT COUNT(*) FROM biz_seat_helper_device WHERE dept_id=#{dept} AND room_name=#{room} AND seat_name=#{seat} AND active=1")
    int activeSeat(@Param("dept") Long dept,@Param("room") String room,@Param("seat") String seat);
    @Select("SELECT device_id FROM biz_seat_helper_device WHERE dept_id=#{dept} AND room_name=#{room} AND seat_name=#{seat} AND active=1 FOR UPDATE")
    List<String> activeSeatForUpdate(@Param("dept") Long dept,@Param("room") String room,@Param("seat") String seat);
    @Insert("INSERT INTO biz_seat_helper_device(device_id,dept_id,room_name,seat_name,credential_hash,fingerprint) VALUES(#{deviceId},#{deptId},#{roomName},#{seatName},#{credentialHash},#{fingerprint})")
    int register(SeatHelperDevice device);
    @Update("UPDATE biz_seat_helper_device SET active=0,revoked_at=NOW() WHERE device_id=#{id}")
    int revoke(String id);
    @Select("SELECT student_id studentId,device_id deviceId FROM biz_seat_helper_binding WHERE dept_id=#{dept} AND room_name=#{room} AND entry_year=#{year} AND class_code=#{clazz} FOR UPDATE")
    List<Map<String,Object>> bindingsForUpdate(@Param("dept") Long dept,@Param("room") String room,@Param("year") String year,@Param("clazz") String clazz);
    @Select("SELECT student_id studentId,device_id deviceId FROM biz_seat_helper_binding WHERE dept_id=#{dept} AND room_name=#{room} AND entry_year=#{year} AND class_code=#{clazz}")
    List<Map<String,Object>> bindings(@Param("dept") Long dept,@Param("room") String room,@Param("year") String year,@Param("clazz") String clazz);
    @Insert("INSERT INTO biz_seat_helper_binding(dept_id,room_name,entry_year,class_code,student_id,device_id,confirmed_by) VALUES(#{dept},#{room},#{year},#{clazz},#{student},#{device},#{teacher}) ON DUPLICATE KEY UPDATE device_id=VALUES(device_id),confirmed_by=VALUES(confirmed_by),confirmed_at=NOW()")
    int bind(@Param("dept") Long dept,@Param("room") String room,@Param("year") String year,@Param("clazz") String clazz,@Param("student") Long student,@Param("device") String device,@Param("teacher") Long teacher);
}
