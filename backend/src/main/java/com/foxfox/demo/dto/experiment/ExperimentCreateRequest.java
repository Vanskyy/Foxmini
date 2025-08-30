package com.foxfox.demo.dto.experiment;

import com.foxfox.demo.model.ExperimentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ExperimentCreateRequest {

    @NotBlank
    @Size(max = 100)
    private String title;

    private String description;

    // 可选：不传默认 DRAFT（在 Service 内处理）
    private ExperimentStatus status;

    @Valid
    private List<ExperimentStageCreateRequest> stages;

    @Valid
    private List<ExperimentResourceCreateRequest> resources;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ExperimentStatus getStatus() {
        return status;
    }

    public void setStatus(ExperimentStatus status) {
        this.status = status;
    }

    public List<ExperimentStageCreateRequest> getStages() {
        return stages;
    }

    public void setStages(List<ExperimentStageCreateRequest> stages) {
        this.stages = stages;
    }

    public List<ExperimentResourceCreateRequest> getResources() {
        return resources;
    }

    public void setResources(List<ExperimentResourceCreateRequest> resources) {
        this.resources = resources;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}