package com.foxfox.demo.dto.experiment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public class StudentAnswerFinalSubmitRequest implements Serializable {

    @NotNull
    private Integer publishedExperimentId;

    @NotNull
    private Integer stageId;

    @Size(max = 20000)
    private String answerContent; // 允许最终提交时一并覆盖

    @Size(max = 20000)
    private String codeContent;

    // 可选：提交确认（前端二次确认）
    private boolean confirm;

    public StudentAnswerFinalSubmitRequest() {}

    public StudentAnswerFinalSubmitRequest(Integer publishedExperimentId, Integer stageId,
                                           String answerContent, String codeContent, boolean confirm) {
        this.publishedExperimentId = publishedExperimentId;
        this.stageId = stageId;
        this.answerContent = answerContent;
        this.codeContent = codeContent;
        this.confirm = confirm;
    }

    public Integer getPublishedExperimentId() { return publishedExperimentId; }
    public Integer getStageId() { return stageId; }
    public String getAnswerContent() { return answerContent; }
    public String getCodeContent() { return codeContent; }
    public boolean isConfirm() { return confirm; }
}