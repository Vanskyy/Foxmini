package com.foxfox.demo.dto.experiment;

import com.foxfox.demo.model.StageType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public class ExperimentStageCreateRequest {

    @NotBlank
    @Size(max = 100)
    private String title;

    private String description;

    @NotNull
    private StageType type;

    @NotBlank
    private String content;

    private String hint;

    @PositiveOrZero
    private Integer maxScore;

    // 可选，缺省由顺序自动分配
    @Positive
    private Integer sequence;

    @Valid
    private EvaluationCriteriaCreateRequest evaluation;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public StageType getType() {
        return type;
    }

    public void setType(StageType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public EvaluationCriteriaCreateRequest getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(EvaluationCriteriaCreateRequest evaluation) {
        this.evaluation = evaluation;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}