//评测结果
package com.foxfox.demo.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "evaluations",
       uniqueConstraints = @UniqueConstraint(name = "uk_evaluation_answer_id", columnNames = "answer_id"),
       indexes = @Index(name = "idx_evaluation_teacher_id", columnList = "teacher_id"))
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "evaluation_id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", nullable = false,
            foreignKey = @ForeignKey(name = "evaluations_ibfk_1"))
    private StudentAnswer answer;

    @Column(name = "score")
    private Integer score;

    @Lob
    @Column(name = "feedback", columnDefinition = "text")
    private String feedback;

    @Lob
    @Column(name = "test_results", columnDefinition = "text")
    private String testResults; // JSON array of test case results

    @CreationTimestamp
    @Column(name = "evaluated_at", nullable = false, updatable = false)
    private LocalDateTime evaluatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluated_by", nullable = false, length = 10)
    private EvaluationBy evaluatedBy = EvaluationBy.SYSTEM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id",
            foreignKey = @ForeignKey(name = "evaluations_ibfk_2"))
    private User teacher; // nullable, only when TEACHER

    // getters & setters
    public Integer getId() { return id; }
    public StudentAnswer getAnswer() { return answer; }
    public void setAnswer(StudentAnswer answer) { this.answer = answer; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public String getTestResults() { return testResults; }
    public void setTestResults(String testResults) { this.testResults = testResults; }
    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public EvaluationBy getEvaluatedBy() { return evaluatedBy; }
    public void setEvaluatedBy(EvaluationBy evaluatedBy) { this.evaluatedBy = evaluatedBy; }
    public User getTeacher() { return teacher; }
    public void setTeacher(User teacher) { this.teacher = teacher; }

    public void setEvaluatedAt(LocalDateTime evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}