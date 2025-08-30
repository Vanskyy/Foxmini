package com.foxfox.demo.dto;

import java.time.LocalDateTime;

public class StudentExperimentHistoryItem {
    private Long experimentId;
    private String experimentTitle;
    private Long answerId;
    private Integer latestScore;
    private LocalDateTime submittedAt;

    public Long getExperimentId() {
        return experimentId;
    }
    public void setExperimentId(Long experimentId) {
        this.experimentId = experimentId;
    }
    public String getExperimentTitle() {
        return experimentTitle;
    }
    public void setExperimentTitle(String experimentTitle) {
        this.experimentTitle = experimentTitle;
    }
    public Long getAnswerId() {
        return answerId;
    }
    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
    }
    public Integer getLatestScore() {
        return latestScore;
    }
    public void setLatestScore(Integer latestScore) {
        this.latestScore = latestScore;
    }
    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}