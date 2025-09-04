package com.foxfox.demo.dto;

import java.time.LocalDateTime;

public class StudentExperimentHistoryItem {
    private Long experimentId;
    private String experimentTitle;
    private Long answerId;
    private Integer latestScore;
    private LocalDateTime submittedAt;
    // 新增：发布实验 ID（用于进度查询/区分不同发布）
    private Long publishedExperimentId;
    // 新增：阶段总数 & 已完成阶段数（finalSubmit 阶段数）
    private Integer totalStages;
    private Integer finishedStages;

    public Long getExperimentId() { return experimentId; }
    public void setExperimentId(Long experimentId) { this.experimentId = experimentId; }
    public String getExperimentTitle() { return experimentTitle; }
    public void setExperimentTitle(String experimentTitle) { this.experimentTitle = experimentTitle; }
    public Long getAnswerId() { return answerId; }
    public void setAnswerId(Long answerId) { this.answerId = answerId; }
    public Integer getLatestScore() { return latestScore; }
    public void setLatestScore(Integer latestScore) { this.latestScore = latestScore; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public Long getPublishedExperimentId() { return publishedExperimentId; }
    public void setPublishedExperimentId(Long publishedExperimentId) { this.publishedExperimentId = publishedExperimentId; }
    public Integer getTotalStages() { return totalStages; }
    public void setTotalStages(Integer totalStages) { this.totalStages = totalStages; }
    public Integer getFinishedStages() { return finishedStages; }
    public void setFinishedStages(Integer finishedStages) { this.finishedStages = finishedStages; }
}