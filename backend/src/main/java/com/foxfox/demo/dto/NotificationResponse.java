package com.foxfox.demo.dto;

import com.foxfox.demo.model.Notification;
import com.foxfox.demo.model.NotificationType;

import java.time.LocalDateTime;

public class NotificationResponse {

    private Integer id;
    private String title;
    private String content;
    private NotificationType type;
    private Integer relatedId;
    private boolean read;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification n) {
        NotificationResponse r = new NotificationResponse();
        r.id = n.getId();
        r.title = n.getTitle();
        r.content = n.getContent();
        r.type = n.getType();
        r.relatedId = n.getRelatedId();
        r.read = n.isRead();
        r.createdAt = n.getCreatedAt();
        return r;
    }

    public Integer getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public NotificationType getType() { return type; }
    public Integer getRelatedId() { return relatedId; }
    public boolean isRead() { return read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}