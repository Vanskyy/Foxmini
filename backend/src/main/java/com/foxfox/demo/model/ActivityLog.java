// 用户行为日志实体：记录用户在系统中的关键操作（登录、创建、修改、删除等），方便审计与追踪。
// 建议：后续可加枚举 action 分类，或 details 中存 JSON 结构便于查询。
package com.foxfox.demo.model;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs",
       indexes = {
           @Index(name = "idx_activity_logs_user", columnList = "user_id"),
           @Index(name = "idx_activity_logs_entity", columnList = "entity_type,entity_id")
       })
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer id; // 主键

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "activity_logs_ibfk_1"))
    private User user; // 触发该行为的用户

    @Column(nullable = false, length = 50)
    private String action; // 动作标识（如 LOGIN, CREATE_EXPERIMENT）

    @Column(name = "entity_type", length = 50)
    private String entityType; // 关联实体类型（如 Experiment, Stage）

    @Column(name = "entity_id")
    private Integer entityId; // 关联实体ID

    @Lob
    @Column(columnDefinition = "text")
    private String details; // 详细信息（可 JSON）

    @Column(name = "ip_address", length = 45)
    private String ipAddress; // 发起请求的客户端IP

    @Column(name = "user_agent", length = 255)
    private String userAgent; // UA 信息

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 记录创建时间

    // getters & setters
    public Integer getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public ActivityLog setUser(User user) {
        this.user = user;
        return this;
    }

    public String getAction() {
        return action;
    }

    public ActivityLog setAction(String action) {
        this.action = action;
        return this;
    }

    public String getEntityType() {
        return entityType;
    }

    public ActivityLog setEntityType(String entityType) {
        this.entityType = entityType;
        return this;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public ActivityLog setEntityId(Integer entityId) {
        this.entityId = entityId;
        return this;
    }

    public String getDetails() {
        return details;
    }

    public ActivityLog setDetails(String details) {
        this.details = details;
        return this;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public ActivityLog setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public ActivityLog setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}