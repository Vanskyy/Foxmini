package com.foxfox.demo.dto.experiment;

import com.foxfox.demo.model.StudentAnswer;

import java.io.Serializable;
import java.time.LocalDateTime;

public class StudentAnswerResponse implements Serializable {
    private Integer answerId;
    private Integer stageId;
    private Integer publishedExperimentId;
    private String answerContent;
    private String codeContent;
    private boolean finalSubmit;
    private LocalDateTime savedAt;
    private LocalDateTime submittedAt;

    public StudentAnswerResponse() {}

    public StudentAnswerResponse(Integer answerId, Integer stageId, Integer publishedExperimentId,
                                 String answerContent, String codeContent,
                                 boolean finalSubmit, LocalDateTime savedAt, LocalDateTime submittedAt) {
        this.answerId = answerId;
        this.stageId = stageId;
        this.publishedExperimentId = publishedExperimentId;
        this.answerContent = answerContent;
        this.codeContent = codeContent;
        this.finalSubmit = finalSubmit;
        this.savedAt = savedAt;
        this.submittedAt = submittedAt;
    }

    public static StudentAnswerResponse from(StudentAnswer entity) {
        if (entity == null) return null;
        return new StudentAnswerResponse(
                entity.getId(),
                entity.getStage() == null ? null : entity.getStage().getId(),
                entity.getPublishedExperiment() == null ? null : entity.getPublishedExperiment().getId(),
                entity.getAnswerContent(),
                entity.getCodeContent(),
                entity.isFinalSubmit(),
                entity.getSavedAt(),
                entity.getSubmittedAt()
        );
    }

    public Integer getAnswerId() { return answerId; }
    public Integer getStageId() { return stageId; }
    public Integer getPublishedExperimentId() { return publishedExperimentId; }
    public String getAnswerContent() { return answerContent; }
    public String getCodeContent() { return codeContent; }
    public boolean isFinalSubmit() { return finalSubmit; }
    public LocalDateTime getSavedAt() { return savedAt; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
}