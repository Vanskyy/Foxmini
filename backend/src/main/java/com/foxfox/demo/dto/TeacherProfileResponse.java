package com.foxfox.demo.dto;

public class TeacherProfileResponse {
    private Integer userId;
    private Integer teacherProfileId;
    private String username;
    private String title;      // 职称
    private String department; // 所属院系

    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public Integer getTeacherProfileId() {
        return teacherProfileId;
    }
    public void setTeacherProfileId(Integer teacherProfileId) {
        this.teacherProfileId = teacherProfileId;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDepartment() {
        return department;
    }
    public void setDepartment(String department) {
        this.department = department;
    }
}