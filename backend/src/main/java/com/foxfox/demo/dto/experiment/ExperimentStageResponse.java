package com.foxfox.demo.dto.experiment;

import com.foxfox.demo.model.ExperimentStage;

import java.io.Serializable;

public class ExperimentStageResponse implements Serializable {
    private Integer stageId;      // 原始ID
    private String name;          // 原标题
    private String description;   // 描述（非题干）
    private String stageType;     // 类型
    private Integer orderNo;      // 原顺序
    // ==== 新增字段（用于前端编辑回显） ====
    private String title;         // 与 name 同义，便于统一
    private Integer sequence;     // 与 orderNo 同义
    private String content;       // 题干 / 主体内容（选择题：题干）
    private String hint;          // 提示
    private Integer maxScore;     // 满分
    // 选择题正确答案（如 "BC"），填空题/代码题可复用此列（按现有模型）
    private String correctAnswer;

    public ExperimentStageResponse() {}

    public ExperimentStageResponse(Integer stageId, String name, String description,
                                   String stageType, Integer orderNo,
                                   String content, String hint, Integer maxScore, String correctAnswer) {
        this.stageId = stageId;
        this.name = name;
        this.description = description; // 单纯描述
        this.stageType = stageType;
        this.orderNo = orderNo;
        this.title = name; // 同步
        this.sequence = orderNo; // 同步
        this.content = content; // 题干
        this.hint = hint;
        this.maxScore = maxScore;
        this.correctAnswer = correctAnswer;
    }

    public static ExperimentStageResponse from(ExperimentStage stage) {
        if (stage == null) return null;
        String correct = null;
        if (stage.getEvaluationCriteria() != null) {
            correct = stage.getEvaluationCriteria().getCorrectAnswer();
        }
        return new ExperimentStageResponse(
                stage.getId(),
                stage.getTitle(),
                stage.getDescription(), // 纯描述
                stage.getType() == null ? null : stage.getType().name(),
                stage.getSequence(),
                stage.getContent(),     // 题干
                stage.getHint(),
                stage.getMaxScore(),
                correct
        );
    }

    // ==== getters (保持旧字段名称) ====
    public Integer getStageId() { return stageId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getStageType() { return stageType; }
    public Integer getOrderNo() { return orderNo; }
    public String getTitle() { return title; }
    public Integer getSequence() { return sequence; }
    public String getContent() { return content; }
    public String getHint() { return hint; }
    public Integer getMaxScore() { return maxScore; }
    public String getCorrectAnswer() { return correctAnswer; }
}