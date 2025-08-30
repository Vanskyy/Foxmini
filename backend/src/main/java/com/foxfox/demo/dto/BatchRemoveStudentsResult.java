package com.foxfox.demo.dto;

import java.util.List;

public class BatchRemoveStudentsResult {
    private final List<Integer> removedIds;
    private final List<Integer> notInGroupIds;

    public BatchRemoveStudentsResult(List<Integer> removedIds, List<Integer> notInGroupIds) {
        this.removedIds = removedIds;
        this.notInGroupIds = notInGroupIds;
    }

    public List<Integer> getRemovedIds() { return removedIds; }
    public List<Integer> getNotInGroupIds() { return notInGroupIds; }
}