package com.foxfox.demo.dto;

import java.time.LocalDateTime;

public class GroupResponse {
    private Integer id;
    private String name;
    private String description;
    private Integer memberCount;
    private LocalDateTime createdAt;

    public GroupResponse() {}

    public GroupResponse(Integer id, String name, String description, Integer memberCount, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.memberCount = memberCount;
        this.createdAt = createdAt;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getMemberCount() { return memberCount; }
    public void setMemberCount(Integer memberCount) { this.memberCount = memberCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
