package com.foxfox.demo.dto.experiment;

import com.foxfox.demo.model.ExperimentStage;

import java.io.Serializable;

public class ExperimentStageResponse implements Serializable {
    private Integer stageId;      // 原始ID
    private String name;          // 原标题
    private String description;   // 描述
    private String stageType;     // 类型
    private Integer orderNo;      // 原顺序
    // ==== 新增字段（用于前端编辑回显） ====
    private String title;         // 与 name 同义，便于统一
    private Integer sequence;     // 与 orderNo 同义
    private String content;       // 题干/内容
    private String hint;          // 提示
    private Integer maxScore;     // 满分

    public ExperimentStageResponse() {}

    public ExperimentStageResponse(Integer stageId, String name, String description,
                                   String stageType, Integer orderNo,
                                   String content, String hint, Integer maxScore) {
        this.stageId = stageId;
        this.name = name;
        this.description = description;
        this.stageType = stageType;
        this.orderNo = orderNo;
        this.title = name; // 同步
        this.sequence = orderNo; // 同步
        this.content = content;
        this.hint = hint;
        this.maxScore = maxScore;
    }

    public static ExperimentStageResponse from(ExperimentStage stage) {
        if (stage == null) return null;
        return new ExperimentStageResponse(
                stage.getId(),
                stage.getTitle(),
                stage.getDescription(),
                stage.getType() == null ? null : stage.getType().name(),
                stage.getSequence(),
                stage.getContent(),
                stage.getHint(),
                stage.getMaxScore()
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
}