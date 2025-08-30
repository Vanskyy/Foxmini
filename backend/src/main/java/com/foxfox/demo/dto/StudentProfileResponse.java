package com.foxfox.demo.dto;

import java.time.LocalDateTime;

public class StudentProfileResponse {
    private int userId;
    private int studentProfileId;
    private String username;

    private String grade;
    private String major;

    // 新增字段
    private String realName; // 真实姓名
    private String email;    // 邮箱
    private String studentId; // 学号
    private String className; // 班级
    private LocalDateTime updatedAt; // 用户更新时间

    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public int getStudentProfileId() {
        return studentProfileId;
    }
    public void setStudentProfileId(int studentProfileId) {
        this.studentProfileId = studentProfileId;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getGrade() {
        return grade;
    }
    public void setGrade(String grade) {
        this.grade = grade;
    }
    public String getMajor() {
        return major;
    }
    public void setMajor(String major) {
        this.major = major;
    }

    // 新增字段 getter/setter
    public String getRealName() {
        return realName;
    }
    public void setRealName(String realName) {
        this.realName = realName;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getStudentId() {
        return studentId;
    }
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    public String getClassName() {
        return className;
    }
    public void setClassName(String className) {
        this.className = className;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}