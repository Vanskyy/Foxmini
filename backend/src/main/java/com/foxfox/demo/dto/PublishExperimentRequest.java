package com.foxfox.demo.dto;

import com.foxfox.demo.model.AssignmentTargetType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public class PublishExperimentRequest {

    @NotNull
    private Integer experimentId;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    private boolean allowLateSubmission;

    // 任务分配目标列表（可包含 ALL / INDIVIDUAL / GROUP 多条；ALL 通常只需一条）
    @Size(min = 1)
    private List<TargetSpec> targets;

    // 可选：发布时附带的首条公告
    private AnnouncementCreateRequest initialAnnouncement;

    public Integer getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(Integer experimentId) {
        this.experimentId = experimentId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public boolean isAllowLateSubmission() {
        return allowLateSubmission;
    }

    public void setAllowLateSubmission(boolean allowLateSubmission) {
        this.allowLateSubmission = allowLateSubmission;
    }

    public List<TargetSpec> getTargets() {
        return targets;
    }

    public void setTargets(List<TargetSpec> targets) {
        this.targets = targets;
    }

    public AnnouncementCreateRequest getInitialAnnouncement() {
        return initialAnnouncement;
    }

    public void setInitialAnnouncement(AnnouncementCreateRequest initialAnnouncement) {
        this.initialAnnouncement = initialAnnouncement;
    }

    public static class TargetSpec {
        @NotNull
        private AssignmentTargetType targetType;
        // ALL 时可为 null
        private Integer targetId;
        // 可附带一个简短标题/描述（若需要）
        private String title;
        private String description;

        public AssignmentTargetType getTargetType() {
            return targetType;
        }

        public void setTargetType(AssignmentTargetType targetType) {
            this.targetType = targetType;
        }

        public Integer getTargetId() {
            return targetId;
        }

        public void setTargetId(Integer targetId) {
            this.targetId = targetId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}