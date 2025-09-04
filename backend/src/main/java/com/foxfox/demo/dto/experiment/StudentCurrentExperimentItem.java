package com.foxfox.demo.dto.experiment;

import java.time.LocalDateTime;

public class StudentCurrentExperimentItem {
    private Integer publishedExperimentId; // 发布实例ID
    private Integer experimentId;          // 实验ID
    private String experimentTitle;        // 实验标题
    private LocalDateTime deadline;        // 结束时间/截止时间
    private Integer totalStages;           // 总阶段数
    private Integer finishedStages;        // 已完成阶段(最终提交数)

    public Integer getPublishedExperimentId() { return publishedExperimentId; }
    public void setPublishedExperimentId(Integer publishedExperimentId) { this.publishedExperimentId = publishedExperimentId; }
    public Integer getExperimentId() { return experimentId; }
    public void setExperimentId(Integer experimentId) { this.experimentId = experimentId; }
    public String getExperimentTitle() { return experimentTitle; }
    public void setExperimentTitle(String experimentTitle) { this.experimentTitle = experimentTitle; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public Integer getTotalStages() { return totalStages; }
    public void setTotalStages(Integer totalStages) { this.totalStages = totalStages; }
    public Integer getFinishedStages() { return finishedStages; }
    public void setFinishedStages(Integer finishedStages) { this.finishedStages = finishedStages; }
}
