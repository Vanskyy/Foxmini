package com.foxfox.demo.dto.experiment;

import java.time.LocalDateTime;
import java.util.List;

public class ExperimentResponse {
    private Integer id;
    private String title;
    private String description;
    private String objective;
    private String status;
    private Integer ownerUserId;
    // 新增：阶段列表
    private List<ExperimentStageResponse> stages;
    // 新增：资源列表
    private List<ExperimentResourceResponse> resources;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getObjective() { return objective; }
    public void setObjective(String objective) { this.objective = objective; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Integer ownerUserId) { this.ownerUserId = ownerUserId; }
    public List<ExperimentStageResponse> getStages() { return stages; }
    public void setStages(List<ExperimentStageResponse> stages) { this.stages = stages; }
    public List<ExperimentResourceResponse> getResources() { return resources; }
    public void setResources(List<ExperimentResourceResponse> resources) { this.resources = resources; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}