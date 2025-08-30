package com.foxfox.demo.service;

        import com.foxfox.demo.dto.AnnouncementCreateRequest;
        import com.foxfox.demo.dto.AnnouncementResponse;
        import com.foxfox.demo.dto.AssignmentDTO;
        import com.foxfox.demo.dto.PublishExperimentRequest;
        import com.foxfox.demo.dto.PublishedExperimentResponse;

        import java.util.List;

        public interface ExperimentPublishService {

            PublishedExperimentResponse publish(Integer teacherUserId, PublishExperimentRequest req);

            PublishedExperimentResponse getDetail(Integer id);

            List<PublishedExperimentResponse> listRunning();

            List<PublishedExperimentResponse> listFinished();

            AnnouncementResponse createAnnouncement(Integer publishId, AnnouncementCreateRequest req);

            List<AnnouncementResponse> listAnnouncements(Integer publishId);

            List<AssignmentDTO> visibleAssignments(Integer publishId, Integer userId);
        }