// src/main/java/com/foxfox/demo/dto/experiment/ExperimentUpdateRequest.java
package com.foxfox.demo.dto.experiment;

import com.foxfox.demo.model.ExperimentStatus;
import jakarta.validation.Valid;

import java.util.List;

public class ExperimentUpdateRequest {
    private String title;
    private String description;
    private String objective; // 预留
    private ExperimentStatus status; // 与创建保持一致

    @Valid
    private List<ExperimentStageCreateRequest> stages; // 全量替换列表（可为空表示清空）

    @Valid
    private List<ExperimentResourceCreateRequest> resources; // 全量替换资源

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getObjective() { return objective; }
    public void setObjective(String objective) { this.objective = objective; }
    public ExperimentStatus getStatus() { return status; }
    public void setStatus(ExperimentStatus status) { this.status = status; }
    public List<ExperimentStageCreateRequest> getStages() { return stages; }
    public void setStages(List<ExperimentStageCreateRequest> stages) { this.stages = stages; }
    public List<ExperimentResourceCreateRequest> getResources() { return resources; }
    public void setResources(List<ExperimentResourceCreateRequest> resources) { this.resources = resources; }
}