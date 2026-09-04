package com.ruoyi.business.domain.vo;

/** 通知目标教师远程选择项。 */
public class ResearchTeacherOptionVo
{
    private Long userId;
    private String nickName;
    private String userName;
    private String phonenumber;
    private Long deptId;
    private String deptName;
    private String schoolType;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getPhonenumber() { return phonenumber; }
    public void setPhonenumber(String phonenumber) { this.phonenumber = phonenumber; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getSchoolType() { return schoolType; }
    public void setSchoolType(String schoolType) { this.schoolType = schoolType; }
}
