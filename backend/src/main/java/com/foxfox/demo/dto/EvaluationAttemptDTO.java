package com.foxfox.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

public class EvaluationAttemptDTO {
    public static class TestCaseResult {
        private String name;
        private String status; // PASS / FAIL
        private Long timeMs;
        private String message;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Long getTimeMs() { return timeMs; }
        public void setTimeMs(Long timeMs) { this.timeMs = timeMs; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    private int attempt;
    private LocalDateTime evaluatedAt;
    private String evaluatedBy;
    private Integer score;
    private String feedback;
    private List<TestCaseResult> cases;

    public int getAttempt() { return attempt; }
    public void setAttempt(int attempt) { this.attempt = attempt; }
    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }
    public String getEvaluatedBy() { return evaluatedBy; }
    public void setEvaluatedBy(String evaluatedBy) { this.evaluatedBy = evaluatedBy; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public List<TestCaseResult> getCases() { return cases; }
    public void setCases(List<TestCaseResult> cases) { this.cases = cases; }
}