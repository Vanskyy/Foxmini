package com.foxfox.demo.dto;

import java.time.LocalDateTime;

public class GroupMemberResponse {
    private Integer userId;
    private String username;
    private String realName;
    private LocalDateTime joinedAt;

    public GroupMemberResponse() {}

    public GroupMemberResponse(Integer userId, String username, String realName, LocalDateTime joinedAt) {
        this.userId = userId;
        this.username = username;
        this.realName = realName;
        this.joinedAt = joinedAt;
    }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}
