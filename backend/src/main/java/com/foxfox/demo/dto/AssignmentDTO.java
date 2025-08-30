package com.foxfox.demo.dto;

import com.foxfox.demo.model.AssignmentTargetType;

public class AssignmentDTO {

    private Integer id;
    private AssignmentTargetType targetType;
    private Integer targetId;
    private String title;
    private String description;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public AssignmentTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(AssignmentTargetType targetType) {
        this.targetType = targetType;
    }

    public Integer getTargetId() {
        return targetId;
    }

    public void setTargetId(Integer targetId) {
        this.targetId = targetId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}