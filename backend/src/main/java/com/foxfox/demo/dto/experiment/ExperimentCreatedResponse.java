package com.foxfox.demo.dto.experiment;

import java.util.List;

public class ExperimentCreatedResponse {

    private Integer experimentId;
    private List<Integer> stageIds;
    private List<Integer> resourceIds;

    public ExperimentCreatedResponse(Integer experimentId, List<Integer> stageIds, List<Integer> resourceIds) {
        this.experimentId = experimentId;
        this.stageIds = stageIds;
        this.resourceIds = resourceIds;
    }

    public Integer getExperimentId() {
        return experimentId;
    }

    public List<Integer> getStageIds() {
        return stageIds;
    }

    public List<Integer> getResourceIds() {
        return resourceIds;
    }
}