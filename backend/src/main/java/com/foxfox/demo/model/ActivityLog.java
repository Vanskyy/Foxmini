//记录用户活动
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
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "activity_logs_ibfk_1"))
    private User user;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Integer entityId;

    @Lob
    @Column(columnDefinition = "text")
    private String details;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

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