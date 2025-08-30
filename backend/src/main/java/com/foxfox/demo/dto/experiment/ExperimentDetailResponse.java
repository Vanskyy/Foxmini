package com.foxfox.demo.dto.experiment;

import com.foxfox.demo.model.Experiment;
import com.foxfox.demo.model.PublishedExperiment;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class ExperimentDetailResponse implements Serializable {
    private Integer experimentId;
    private Integer publishedExperimentId;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<ExperimentStageResponse> stages;

    public ExperimentDetailResponse() {}

    public ExperimentDetailResponse(Integer experimentId, Integer publishedExperimentId,
                                    String title, String description,
                                    LocalDateTime startTime, LocalDateTime endTime,
                                    List<ExperimentStageResponse> stages) {
        this.experimentId = experimentId;
        this.publishedExperimentId = publishedExperimentId;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.stages = stages;
    }

    public static ExperimentDetailResponse from(Experiment exp,
                                                PublishedExperiment pub,
                                                List<ExperimentStageResponse> stages) {
        return new ExperimentDetailResponse(
                exp == null ? null : exp.getId(),
                pub == null ? null : pub.getId(),
                exp == null ? null : exp.getTitle(),
                exp == null ? null : exp.getDescription(),
                pub == null ? null : pub.getStartTime(),
                pub == null ? null : pub.getEndTime(),
                stages
        );
    }

    public Integer getExperimentId() { return experimentId; }
    public Integer getPublishedExperimentId() { return publishedExperimentId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public List<ExperimentStageResponse> getStages() { return stages; }
}