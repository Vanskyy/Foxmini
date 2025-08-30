package com.foxfox.demo.dto;

public class UpdateTeacherProfileRequest {
    private String title;
    private String department;

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