package com.foxfox.demo.dto.experiment;

import java.io.Serializable;
import java.util.List;

public class ExperimentExecutionInitResponse implements Serializable {
    private ExperimentDetailResponse experiment;
    private ExperimentProgressResponse progress;
    private StudentAnswerResponse currentAnswer;
    private List<StudentAnswerResponse> allStageDrafts; // 可选：用于预加载

    public ExperimentExecutionInitResponse() {}

    public ExperimentExecutionInitResponse(ExperimentDetailResponse experiment,
                                           ExperimentProgressResponse progress,
                                           StudentAnswerResponse currentAnswer,
                                           List<StudentAnswerResponse> allStageDrafts) {
        this.experiment = experiment;
        this.progress = progress;
        this.currentAnswer = currentAnswer;
        this.allStageDrafts = allStageDrafts;
    }

    public ExperimentDetailResponse getExperiment() { return experiment; }
    public ExperimentProgressResponse getProgress() { return progress; }
    public StudentAnswerResponse getCurrentAnswer() { return currentAnswer; }
    public List<StudentAnswerResponse> getAllStageDrafts() { return allStageDrafts; }
}