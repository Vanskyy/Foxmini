package com.foxfox.demo.dto.experiment;

import com.foxfox.demo.model.ExperimentStage;
import com.foxfox.demo.model.EvaluationCriteria;

import java.io.Serializable;

public class ExperimentStageResponse implements Serializable {
    private Integer stageId;      // 原始ID
    private String name;          // 原标题
    private String description;   // 描述（非题干）
    private String stageType;     // 类型
    private Integer orderNo;      // 原顺序
    private String title;         // 与 name 同义
    private Integer sequence;     // 与 orderNo 同义
    private String content;       // 题干 / 主体内容
    private String hint;          // 提示
    private Integer maxScore;     // 满分
    private String correctAnswer; // 兼容旧字段
    // 新增：结构化评测信息
    private Evaluation evaluation;

    public static class Evaluation {
        private String correctOptions; // 选择题答案（紧凑或逗号形式）
        private Boolean multiple;      // 是否多选
        private String fillAnswers;    // 填空题答案
        private String testCases;      // 编程题测试用例(JSON)
        public String getCorrectOptions() { return correctOptions; }
        public void setCorrectOptions(String correctOptions) { this.correctOptions = correctOptions; }
        public Boolean getMultiple() { return multiple; }
        public void setMultiple(Boolean multiple) { this.multiple = multiple; }
        public String getFillAnswers() { return fillAnswers; }
        public void setFillAnswers(String fillAnswers) { this.fillAnswers = fillAnswers; }
        public String getTestCases() { return testCases; }
        public void setTestCases(String testCases) { this.testCases = testCases; }
    }

    public ExperimentStageResponse() {}

    public ExperimentStageResponse(Integer stageId, String name, String description,
                                   String stageType, Integer orderNo,
                                   String content, String hint, Integer maxScore, String correctAnswer,
                                   Evaluation evaluation) {
        this.stageId = stageId;
        this.name = name;
        this.description = description;
        this.stageType = stageType;
        this.orderNo = orderNo;
        this.title = name;
        this.sequence = orderNo;
        this.content = content;
        this.hint = hint;
        this.maxScore = maxScore;
        this.correctAnswer = correctAnswer;
        this.evaluation = evaluation;
    }

    public static ExperimentStageResponse from(ExperimentStage stage) {
        if (stage == null) return null;
        EvaluationCriteria ec = stage.getEvaluationCriteria();
        String correct = ec == null ? null : ec.getCorrectAnswer();
        Evaluation eval = null;
        if (ec != null) {
            eval = new Evaluation();
            switch (stage.getType()) {
                case CHOICE -> {
                    eval.setCorrectOptions(correct);
                }
                case FILL -> eval.setFillAnswers(correct);
                case CODE -> eval.setTestCases(ec.getTestCases());
                default -> {}
            }
        }
        return new ExperimentStageResponse(
                stage.getId(),
                stage.getTitle(),
                stage.getDescription(),
                stage.getType() == null ? null : stage.getType().name(),
                stage.getSequence(),
                stage.getContent(),
                stage.getHint(),
                stage.getMaxScore(),
                correct,
                eval
        );
    }

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
    public Evaluation getEvaluation() { return evaluation; }
}