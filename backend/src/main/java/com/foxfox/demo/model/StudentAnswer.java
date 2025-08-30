//学生答案
package com.foxfox.demo.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_answers",
       uniqueConstraints = @UniqueConstraint(name = "uk_answer_user_stage_publish",
               columnNames = {"user_id","stage_id","publish_id"}),
       indexes = {
           @Index(name = "idx_answer_stage_id", columnList = "stage_id"),
           @Index(name = "idx_answer_publish_id", columnList = "publish_id")
       })
public class StudentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "student_answers_ibfk_1"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false,
            foreignKey = @ForeignKey(name = "student_answers_ibfk_2"))
    private ExperimentStage stage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publish_id", nullable = false,
            foreignKey = @ForeignKey(name = "student_answers_ibfk_3"))
    private PublishedExperiment publishedExperiment;

    @Lob
    @Column(name = "answer_content", columnDefinition = "text")
    private String answerContent;

    @Lob
    @Column(name = "code_content", columnDefinition = "text")
    private String codeContent;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "is_final", nullable = false)
    private boolean finalSubmit = false;//是否为最终提交，不是则作为自动保存使用

    @UpdateTimestamp
    @Column(name = "saved_at", nullable = false)
    private LocalDateTime savedAt;

    @OneToOne(mappedBy = "answer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Evaluation evaluation;

    // helpers
    public void setEvaluation(Evaluation evaluation) {
        if (evaluation == null) {
            if (this.evaluation != null) this.evaluation.setAnswer(null);
        } else {
            evaluation.setAnswer(this);
        }
        this.evaluation = evaluation;
    }

    // getters & setters
    public Integer getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public ExperimentStage getStage() { return stage; }
    public void setStage(ExperimentStage stage) { this.stage = stage; }
    public PublishedExperiment getPublishedExperiment() { return publishedExperiment; }
    public void setPublishedExperiment(PublishedExperiment publishedExperiment) { this.publishedExperiment = publishedExperiment; }
    public String getAnswerContent() { return answerContent; }
    public void setAnswerContent(String answerContent) { this.answerContent = answerContent; }
    public String getCodeContent() { return codeContent; }
    public void setCodeContent(String codeContent) { this.codeContent = codeContent; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public boolean isFinalSubmit() { return finalSubmit; }
    public void setFinalSubmit(boolean finalSubmit) { this.finalSubmit = finalSubmit; }
    public LocalDateTime getSavedAt() { return savedAt; }
    public Evaluation getEvaluation() { return evaluation; }
}