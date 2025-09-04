package com.foxfox.demo.service;

import com.foxfox.demo.dto.AnnouncementCreateRequest;
import com.foxfox.demo.dto.AnnouncementResponse;
import com.foxfox.demo.dto.AnnouncementUpdateRequest;
import com.foxfox.demo.dto.AssignmentDTO;
import com.foxfox.demo.dto.PublishExperimentRequest;
import com.foxfox.demo.dto.PublishedExperimentResponse;

import java.util.List;

public interface ExperimentPublishService {

    PublishedExperimentResponse publish(Integer teacherUserId, PublishExperimentRequest req);

    PublishedExperimentResponse getDetail(Integer id);

    List<PublishedExperimentResponse> listRunning();

    List<PublishedExperimentResponse> listFinished();

    // 增加 teacherUserId 以便设置公告创建者
    AnnouncementResponse createAnnouncement(Integer publishId, Integer teacherUserId, AnnouncementCreateRequest req);

    List<AnnouncementResponse> listAnnouncements(Integer publishId);

    AnnouncementResponse updateAnnouncement(Integer announcementId, Integer teacherUserId, AnnouncementUpdateRequest req);

    void deleteAnnouncement(Integer announcementId, Integer teacherUserId);

    List<AssignmentDTO> visibleAssignments(Integer publishId, Integer userId);

    // 新增: 下线(提前结束)一个已发布实验，将其 endTime 设置为当前时间之前
    PublishedExperimentResponse unpublish(Integer teacherUserId, Integer publishId);

    // 新增: 删除一个已结束的发布实例（仅发布者，且 endTime < now）。无返回体。
    void deleteFinished(Integer teacherUserId, Integer publishId);
}