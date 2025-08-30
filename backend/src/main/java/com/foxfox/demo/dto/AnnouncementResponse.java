package com.foxfox.demo.dto;

import java.time.LocalDateTime;

public class AnnouncementResponse {

    private Integer id;
    private Integer publishedExperimentId;
    private String title;
    private String content;
    private boolean important;
    private LocalDateTime createdAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPublishedExperimentId() {
        return publishedExperimentId;
    }

    public void setPublishedExperimentId(Integer publishedExperimentId) {
        this.publishedExperimentId = publishedExperimentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isImportant() {
        return important;
    }

    public void setImportant(boolean important) {
        this.important = important;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}