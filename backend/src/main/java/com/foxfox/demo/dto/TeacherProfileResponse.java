package com.foxfox.demo.dto;

import java.time.LocalDateTime;

public class TeacherProfileResponse {
    private Integer userId;
    private Integer teacherProfileId;
    private String username;
    private String realName; // 真实姓名
    private String email;    // 邮箱
    private String avatar;   // 头像
    private String teacherId; // 教师工号
    private String title;      // 职称
    private String department; // 所属院系
    private LocalDateTime createdAt; // 用户创建时间
    private LocalDateTime updatedAt; // 用户更新时间

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getTeacherProfileId() { return teacherProfileId; }
    public void setTeacherProfileId(Integer teacherProfileId) { this.teacherProfileId = teacherProfileId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}