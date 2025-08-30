package com.foxfox.demo.dto;

import java.util.List;

public class BatchAddStudentsResult {
    private List<Integer> successIds;
    private List<Integer> alreadyInGroupIds;
    private List<Integer> notFoundIds;

    public BatchAddStudentsResult(List<Integer> successIds,
                                  List<Integer> alreadyInGroupIds,
                                  List<Integer> notFoundIds) {
        this.successIds = successIds;
        this.alreadyInGroupIds = alreadyInGroupIds;
        this.notFoundIds = notFoundIds;
    }

    public List<Integer> getSuccessIds() { return successIds; }
    public List<Integer> getAlreadyInGroupIds() { return alreadyInGroupIds; }
    public List<Integer> getNotFoundIds() { return notFoundIds; }
}