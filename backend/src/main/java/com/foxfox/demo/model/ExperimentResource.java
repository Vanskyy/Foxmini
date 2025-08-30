// 实验附加资源：文件/链接/视频等，供学生查看。大小 size 可选（文件型适用）。
package com.foxfox.demo.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "experiment_resources",
        indexes = @Index(name = "idx_resource_experiment_id", columnList = "experiment_id"))
public class ExperimentResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resource_id")
    private Integer id; // 主键

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id", nullable = false,
            foreignKey = @ForeignKey(name = "experiment_resources_ibfk_1"))
    private Experiment experiment; // 所属实验

    @Column(nullable = false, length = 100)
    private String name; // 资源显示名称

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ResourceType type; // 资源类型枚举

    @Column(nullable = false, length = 255)
    private String url; // 访问地址 / 存储路径

    @Column
    private Integer size; // 可选：文件大小（字节）

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt; // 上传时间

    // getters & setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ResourceType getType() { return type; }
    public void setType(ResourceType type) { this.type = type; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public Experiment getExperiment() { return experiment; }
    public void setExperiment(Experiment experiment) { this.experiment = experiment; }
}