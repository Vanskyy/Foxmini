package com.foxfox.demo.dto.experiment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public class StudentAnswerSaveRequest implements Serializable {

    @NotNull
    private Integer publishedExperimentId;

    @NotNull
    private Integer stageId;

    @Size(max = 20000)
    private String answerContent;

    @Size(max = 20000)
    private String codeContent;

    public StudentAnswerSaveRequest() {}

    public StudentAnswerSaveRequest(Integer publishedExperimentId, Integer stageId,
                                    String answerContent, String codeContent) {
        this.publishedExperimentId = publishedExperimentId;
        this.stageId = stageId;
        this.answerContent = answerContent;
        this.codeContent = codeContent;
    }

    public Integer getPublishedExperimentId() { return publishedExperimentId; }
    public Integer getStageId() { return stageId; }
    public String getAnswerContent() { return answerContent; }
    public String getCodeContent() { return codeContent; }
}