package com.foxfox.demo.dto;

import com.foxfox.demo.model.Role;

public class RegisterRequest {

    private String username;
    private String password;
    private String email;
    private String realName;
    private Role role;

    // 学生信息（当 role == STUDENT 时可填）
    private String studentId;
    private String className;
    private String major;
    private String grade;

    // 教师信息（当 role == TEACHER 时可填）
    private String teacherId;
    private String department;
    private String title;

    // getter & setter
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}