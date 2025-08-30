package com.foxfox.demo.dto;

public class StudentProfileResponse {
    private int userId;
    private int studentProfileId;
    private String username;

    private String grade;
    private String major;


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


}