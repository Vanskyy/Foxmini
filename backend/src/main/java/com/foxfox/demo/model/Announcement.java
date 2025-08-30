// 公告实体：教师针对已发布实验(PublishedExperiment)发布的公告/通知，可标记重要性。
// 典型用途：更新说明、补充资料、截止日期提醒。
package com.foxfox.demo.model;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "announcements",
       indexes = {
           @Index(name = "idx_announcement_publish_id", columnList = "publish_id"),
           @Index(name = "idx_announcement_created_by", columnList = "created_by")
       })
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "announcement_id")
    private Integer id; // 主键

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publish_id", nullable = false,
            foreignKey = @ForeignKey(name = "announcements_ibfk_1"))
    private PublishedExperiment publishedExperiment; // 所属发布实例

    @Column(nullable = false, length = 100)
    private String title; // 公告标题

    @Lob
    @Column(nullable = false, columnDefinition = "text")
    private String content; // 公告正文

    @Column(name = "is_important", nullable = false)
    private boolean important = false; // 是否标记为重要

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 创建时间

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false,
            foreignKey = @ForeignKey(name = "announcements_ibfk_2"))
    private User createdBy; // 发布者（教师）

    // getters & setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isImportant() { return important; }
    public void setImportant(boolean important) { this.important = important; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public PublishedExperiment getPublishedExperiment() { return publishedExperiment; }
    public void setPublishedExperiment(PublishedExperiment publishedExperiment) { this.publishedExperiment = publishedExperiment; }
}