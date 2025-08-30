package com.foxfox.demo.dto;

import java.util.List;

public class EvaluationHistoryResponse {
    private Integer answerId;
    private Integer latestScore;
    private List<EvaluationAttemptDTO> attempts;

    public Integer getAnswerId() { return answerId; }
    public void setAnswerId(Integer answerId) { this.answerId = answerId; }
    public Integer getLatestScore() { return latestScore; }
    public void setLatestScore(Integer latestScore) { this.latestScore = latestScore; }
    public List<EvaluationAttemptDTO> getAttempts() { return attempts; }
    public void setAttempts(List<EvaluationAttemptDTO> attempts) { this.attempts = attempts; }
}