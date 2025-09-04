package com.foxfox.demo.dto;

public class AnnouncementUpdateRequest {
    private String title; // 可选
    private String content; // 可选
    private Boolean important; // 可选

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Boolean getImportant() { return important; }
    public void setImportant(Boolean important) { this.important = important; }
}