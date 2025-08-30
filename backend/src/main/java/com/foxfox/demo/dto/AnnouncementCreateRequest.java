package com.foxfox.demo.dto;

import jakarta.validation.constraints.NotBlank;

public class AnnouncementCreateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private boolean important;

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
}