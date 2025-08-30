package com.foxfox.demo.dto.experiment;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

public class ExperimentProgressResponse implements Serializable {
    private Integer publishedExperimentId;
    private Integer totalStages;
    private Integer finishedStages; // final 提交数
    private Set<Integer> finalSubmittedStageIds;
    private Integer currentStageId; // 前端当前展示阶段
    private LocalDateTime lastSavedAt;

    public ExperimentProgressResponse() {}

    public ExperimentProgressResponse(Integer publishedExperimentId, Integer totalStages,
                                      Integer finishedStages, Set<Integer> finalSubmittedStageIds,
                                      Integer currentStageId, LocalDateTime lastSavedAt) {
        this.publishedExperimentId = publishedExperimentId;
        this.totalStages = totalStages;
        this.finishedStages = finishedStages;
        this.finalSubmittedStageIds = finalSubmittedStageIds;
        this.currentStageId = currentStageId;
        this.lastSavedAt = lastSavedAt;
    }

    public Integer getPublishedExperimentId() { return publishedExperimentId; }
    public Integer getTotalStages() { return totalStages; }
    public Integer getFinishedStages() { return finishedStages; }
    public Set<Integer> getFinalSubmittedStageIds() { return finalSubmittedStageIds; }
    public Integer getCurrentStageId() { return currentStageId; }
    public LocalDateTime getLastSavedAt() { return lastSavedAt; }
}