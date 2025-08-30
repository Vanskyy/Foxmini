package com.foxfox.demo.dto;

import java.util.List;

public class BatchRemoveStudentsRequest {
    private List<Integer> userIds;
    public List<Integer> getUserIds() { return userIds; }
    public void setUserIds(List<Integer> userIds) { this.userIds = userIds; }
}