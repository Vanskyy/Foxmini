//实验分配
package com.foxfox.demo.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "experiment_assignments",
       indexes = @Index(name = "idx_assignment_publish_id", columnList = "publish_id"))
public class ExperimentAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publish_id", nullable = false,
            foreignKey = @ForeignKey(name = "experiment_assignments_ibfk_1"))
    private PublishedExperiment publishedExperiment;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 15)
    private AssignmentTargetType targetType;

    // 可根据 targetType 解释为 group_id 或 user_id；为保持与表一致不做多态关联
    @Column(name = "target_id")
    private Integer targetId;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    // getters & setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public AssignmentTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(AssignmentTargetType targetType) {
        this.targetType = targetType;
    }

    public Integer getTargetId() {
        return targetId;
    }

    public void setTargetId(Integer targetId) {
        this.targetId = targetId;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public PublishedExperiment getPublishedExperiment() {
        return publishedExperiment;
    }

    public void setPublishedExperiment(PublishedExperiment publishedExperiment) {
        this.publishedExperiment = publishedExperiment;
    }
}