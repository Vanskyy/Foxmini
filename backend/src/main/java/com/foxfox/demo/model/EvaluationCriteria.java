//教师设置的评测标准
package com.foxfox.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "evaluation_criteria",
       uniqueConstraints = @UniqueConstraint(name = "uk_eval_criteria_stage_id", columnNames = "stage_id"))
public class EvaluationCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "criteria_id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false,
            foreignKey = @ForeignKey(name = "evaluation_criteria_ibfk_1"))
    private ExperimentStage stage;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private StageType type;

    @Lob
    @Column(name = "correct_answer", columnDefinition = "text")
    private String correctAnswer;

    @Lob
    @Column(name = "test_cases", columnDefinition = "text")
    private String testCases; // JSON array

    @Lob
    @Column(name = "evaluation_script", columnDefinition = "text")
    private String evaluationScript;

    @Column(name = "partial_credit", nullable = false)
    private boolean partialCredit = false;

    // getters & setters
    public Integer getId() { return id; }
    public ExperimentStage getStage() { return stage; }
    public void setStage(ExperimentStage stage) { this.stage = stage; }
    public StageType getType() { return type; }
    public void setType(StageType type) { this.type = type; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public String getTestCases() { return testCases; }
    public void setTestCases(String testCases) { this.testCases = testCases; }
    public String getEvaluationScript() { return evaluationScript; }
    public void setEvaluationScript(String evaluationScript) { this.evaluationScript = evaluationScript; }
    public boolean isPartialCredit() { return partialCredit; }
    public void setPartialCredit(boolean partialCredit) { this.partialCredit = partialCredit; }
}