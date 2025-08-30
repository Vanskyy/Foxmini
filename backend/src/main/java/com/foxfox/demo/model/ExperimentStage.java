//实验的各个阶段任务
package com.foxfox.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "experiment_stages",
        indexes = @Index(name = "idx_stage_experiment_id", columnList = "experiment_id"))
public class ExperimentStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stage_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id", nullable = false,
            foreignKey = @ForeignKey(name = "experiment_stages_ibfk_1"))
    private Experiment experiment;

    @Column(nullable = false, length = 100)
    private String title;

    @Lob
    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private Integer sequence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StageType type;

    @Lob
    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Lob
    @Column(columnDefinition = "text")
    private String hint;

    @Column(name = "max_score", nullable = false)
    private Integer maxScore = 0;

    @OneToOne(mappedBy = "stage", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private EvaluationCriteria evaluationCriteria;

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

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
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

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public Integer getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }

    public EvaluationCriteria getEvaluationCriteria() {
        return evaluationCriteria;
    }
}