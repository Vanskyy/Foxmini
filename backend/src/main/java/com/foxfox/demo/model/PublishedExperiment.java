// 已发布实验：绑定时间窗口（开始/结束/迟交截止）与分配、公告等运行期信息。
// 注意：Experiment 为模板，本实体为一次具体发布实例，可多次发布。
package com.foxfox.demo.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "published_experiments",
        indexes = {
                @Index(name = "idx_published_experiment_id", columnList = "experiment_id"),
                @Index(name = "idx_published_publisher_id", columnList = "publisher_id")
        })
public class PublishedExperiment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "publish_id")
    private Integer id; // 主键

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id", nullable = false,
            foreignKey = @ForeignKey(name = "published_experiments_ibfk_1"))
    private Experiment experiment; // 来源实验模板

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id", nullable = false,
            foreignKey = @ForeignKey(name = "published_experiments_ibfk_2"))
    private User publisher; // 发布教师

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime; // 开始时间

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime; // 截止时间

    @Column(name = "allow_late_submit", nullable = false)
    private boolean allowLateSubmit = false; // 是否允许迟交

    @Column(name = "late_submit_deadline")
    private LocalDateTime lateSubmitDeadline; // 迟交最终期限（可空）

    @CreationTimestamp
    @Column(name = "published_at", nullable = false, updatable = false)
    private LocalDateTime publishedAt; // 发布时间

    @OneToMany(mappedBy = "publishedExperiment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExperimentAssignment> assignments = new ArrayList<>(); // 分配记录

    @OneToMany(mappedBy = "publishedExperiment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Announcement> announcements = new ArrayList<>(); // 公告列表

    public void addAssignment(ExperimentAssignment a) {
        assignments.add(a);
        a.setPublishedExperiment(this);
    }
    public void removeAssignment(ExperimentAssignment a) {
        assignments.remove(a);
        a.setPublishedExperiment(null);
    }
    public void addAnnouncement(Announcement ann) {
        announcements.add(ann);
        ann.setPublishedExperiment(this);
    }
    public void removeAnnouncement(Announcement ann) {
        announcements.remove(ann);
        ann.setPublishedExperiment(null);
    }

    // getters & setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Experiment getExperiment() { return experiment; }
    public void setExperiment(Experiment experiment) { this.experiment = experiment; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public User getPublisher() { return publisher; }
    public void setPublisher(User publisher) { this.publisher = publisher; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public boolean isAllowLateSubmit() { return allowLateSubmit; }
    public void setAllowLateSubmit(boolean allowLateSubmit) { this.allowLateSubmit = allowLateSubmit; }
    public LocalDateTime getLateSubmitDeadline() { return lateSubmitDeadline; }
    public void setLateSubmitDeadline(LocalDateTime lateSubmitDeadline) { this.lateSubmitDeadline = lateSubmitDeadline; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    public List<ExperimentAssignment> getAssignments() { return assignments; }
    public void setAssignments(List<ExperimentAssignment> assignments) { this.assignments = assignments; }
    public List<Announcement> getAnnouncements() { return announcements; }
    public void setAnnouncements(List<Announcement> announcements) { this.announcements = announcements; }
}