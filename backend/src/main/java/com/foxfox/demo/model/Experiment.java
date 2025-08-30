// 实验主实体：承载一个教学/练习实验的基础定义（标题、描述、阶段集合、资源集合、状态）。
// 状态流转：DRAFT -> PUBLISHED -> ARCHIVED。
// 发布后会生成 PublishedExperiment 以承载具体时间窗口与分配信息。
package com.foxfox.demo.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "experiments")
public class Experiment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "experiment_id")
    private Integer id; // 主键

    @Column(nullable = false, length = 100)
    private String title; // 实验标题

    @Lob
    @Column(columnDefinition = "text")
    private String description; // 实验整体描述/目标说明

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false,
            foreignKey = @ForeignKey(name = "experiments_ibfk_1"))
    private User creator; // 创建教师

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 创建时间

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 最近更新时间

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ExperimentStatus status = ExperimentStatus.DRAFT; // 当前状态

    @OneToMany(mappedBy = "experiment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence ASC")
    private List<ExperimentStage> stages = new ArrayList<>(); // 阶段集合

    @OneToMany(mappedBy = "experiment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExperimentResource> resources = new ArrayList<>(); // 附加资源

    public void addStage(ExperimentStage stage) {
        stages.add(stage);
        stage.setExperiment(this);
    }

    public void removeStage(ExperimentStage stage) {
        stages.remove(stage);
        stage.setExperiment(null);
    }

    public void addResource(ExperimentResource resource) {
        resources.add(resource);
        resource.setExperiment(this);
    }

    public void removeResource(ExperimentResource resource) {
        resources.remove(resource);
        resource.setExperiment(null);
    }

    // getters & setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public ExperimentStatus getStatus() { return status; }
    public void setStatus(ExperimentStatus status) { this.status = status; }
    public List<ExperimentStage> getStages() { return stages; }
    public void setStages(List<ExperimentStage> stages) { this.stages = stages; }
    public List<ExperimentResource> getResources() { return resources; }
    public void setResources(List<ExperimentResource> resources) { this.resources = resources; }
}