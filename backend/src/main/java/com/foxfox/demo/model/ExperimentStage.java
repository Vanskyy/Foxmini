// 实验阶段（题目/任务）实体：隶属于 Experiment。
// 字段说明：
//  - content: 题干 / 主体（最新约定）
//  - description: 补充描述 / 说明（可选）
//  - type: 阶段类型 (CHOICE/FILL/CODE/TEXT)
//  - evaluationCriteria: 评测标准（含正确答案、测试用例等）
package com.foxfox.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "experiment_stages",
        indexes = @Index(name = "idx_stage_experiment_id", columnList = "experiment_id"))
public class ExperimentStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stage_id")
    private Integer id; // 主键

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id", nullable = false,
            foreignKey = @ForeignKey(name = "experiment_stages_ibfk_1"))
    private Experiment experiment; // 所属实验

    @Column(nullable = false, length = 100)
    private String title; // 阶段标题

    @Lob
    @Column(columnDefinition = "text")
    private String description; // 整体描述 概述

    @Column(nullable = false)
    private Integer sequence; // 序号（升序）

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StageType type; // 阶段类型

    @Lob
    @Column(nullable = false, columnDefinition = "text")
    private String content; // 题干 / 主体

    @Lob
    @Column(columnDefinition = "text")
    private String hint; // 小提示（可选）

    @Column(name = "max_score", nullable = false)
    private Integer maxScore = 0; // 满分

    @OneToOne(mappedBy = "stage", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private EvaluationCriteria evaluationCriteria; // 评测标准

    // helpers
    public void setEvaluationCriteria(EvaluationCriteria criteria) {
        if (criteria == null) {
            if (this.evaluationCriteria != null) {
                this.evaluationCriteria.setStage(null);
            }
        } else {
            criteria.setStage(this);
        }
        this.evaluationCriteria = criteria;
    }

    // getters & setters
    public Experiment getExperiment() { return experiment; }
    public void setExperiment(Experiment experiment) { this.experiment = experiment; }
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getSequence() { return sequence; }
    public void setSequence(Integer sequence) { this.sequence = sequence; }
    public StageType getType() { return type; }
    public void setType(StageType type) { this.type = type; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getHint() { return hint; }
    public void setHint(String hint) { this.hint = hint; }
    public Integer getMaxScore() { return maxScore; }
    public void setMaxScore(Integer maxScore) { this.maxScore = maxScore; }
    public EvaluationCriteria getEvaluationCriteria() { return evaluationCriteria; }
}