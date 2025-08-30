// src/main/java/com/foxfox/demo/dto/experiment/ExperimentUpdateRequest.java
package com.foxfox.demo.dto.experiment;

public class ExperimentUpdateRequest {
    private String title;
    private String description;
    private String objective;
    private String status; // 可选: DRAFT/PUBLISHED/ARCHIVED

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getObjective() { return objective; }
    public void setObjective(String objective) { this.objective = objective; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}