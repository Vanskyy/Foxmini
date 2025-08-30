package com.foxfox.demo.service.impl;

import com.foxfox.demo.dto.*;
import com.foxfox.demo.model.*;
import com.foxfox.demo.repository.*;
import com.foxfox.demo.service.ExperimentPublishService;
import com.foxfox.demo.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExperimentPublishServiceImpl implements ExperimentPublishService {

    private final PublishedExperimentRepository publishedExperimentRepository;
    private final ExperimentAssignmentRepository experimentAssignmentRepository;
    private final AnnouncementRepository announcementRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ExperimentPublishServiceImpl(PublishedExperimentRepository publishedExperimentRepository,
                                        ExperimentAssignmentRepository experimentAssignmentRepository,
                                        AnnouncementRepository announcementRepository,
                                        GroupMemberRepository groupMemberRepository,
                                        UserRepository userRepository,
                                        NotificationService notificationService) {
        this.publishedExperimentRepository = publishedExperimentRepository;
        this.experimentAssignmentRepository = experimentAssignmentRepository;
        this.announcementRepository = announcementRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public PublishedExperimentResponse publish(Integer teacherUserId, PublishExperimentRequest req) {
        if (req.getStartTime().isAfter(req.getEndTime())) {
            throw new IllegalArgumentException("开始时间需早于截止时间");
        }
        User publisher = userRepository.findById(teacherUserId)
                .orElseThrow(() -> new EntityNotFoundException("发布教师不存在"));

        PublishedExperiment pe = new PublishedExperiment();
        pe.setPublisher(publisher);
        pe.setStartTime(req.getStartTime());
        pe.setEndTime(req.getEndTime());

        Experiment experimentRef = new Experiment();
        experimentRef.setId(req.getExperimentId());
        pe.setExperiment(experimentRef);
        pe.setAllowLateSubmit(req.isAllowLateSubmission());
        pe.setPublishedAt(LocalDateTime.now());
        pe = publishedExperimentRepository.save(pe);

        List<ExperimentAssignment> assignments = new ArrayList<>();
        for (PublishExperimentRequest.TargetSpec spec : req.getTargets()) {
            ExperimentAssignment a = new ExperimentAssignment();
            a.setPublishedExperiment(pe);
            a.setTargetType(spec.getTargetType());
            a.setTargetId(spec.getTargetType() == AssignmentTargetType.ALL ? null : spec.getTargetId());
            // 若实体含有 title/description 字段，可取消下列注释
            // a.setTitle(spec.getTitle());
            // a.setDescription(spec.getDescription());
            assignments.add(a);
        }
        experimentAssignmentRepository.saveAll(assignments);

        if (req.getInitialAnnouncement() != null) {
            createAnnouncementInternal(pe, req.getInitialAnnouncement());
        }

        Set<Integer> notifyUserIds = collectTargetUserIds(assignments);
        notificationService.notifyExperimentPublished(pe, notifyUserIds);

        return toResponse(pe, assignments);
    }

    @Override
    public PublishedExperimentResponse getDetail(Integer id) {
        PublishedExperiment pe = publishedExperimentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("发布不存在"));
        List<ExperimentAssignment> list = experimentAssignmentRepository.findByPublishedExperiment_Id(id);
        return toResponse(pe, list);
    }

    @Override
    public List<PublishedExperimentResponse> listRunning() {
        LocalDateTime now = LocalDateTime.now();
        return publishedExperimentRepository.findRunning(now)
                .stream()
                .map(pe -> toResponse(pe,
                        experimentAssignmentRepository.findByPublishedExperiment_Id(pe.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<PublishedExperimentResponse> listFinished() {
        LocalDateTime now = LocalDateTime.now();
        return publishedExperimentRepository.findFinished(now)
                .stream()
                .map(pe -> toResponse(pe,
                        experimentAssignmentRepository.findByPublishedExperiment_Id(pe.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AnnouncementResponse createAnnouncement(Integer publishId, AnnouncementCreateRequest req) {
        PublishedExperiment pe = publishedExperimentRepository.findById(publishId)
                .orElseThrow(() -> new EntityNotFoundException("发布不存在"));
        Announcement a = createAnnouncementInternal(pe, req);
        Set<Integer> userIds = collectTargetUserIds(
                experimentAssignmentRepository.findByPublishedExperiment_Id(publishId));
        notificationService.notifyAnnouncement(a, userIds);
        return toAnnouncementDTO(a);
    }

    @Override
    public List<AnnouncementResponse> listAnnouncements(Integer publishId) {
        return announcementRepository
                .findByPublishedExperiment_IdOrderByCreatedAtDesc(publishId)
                .stream()
                .map(this::toAnnouncementDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignmentDTO> visibleAssignments(Integer publishId, Integer userId) {
        return experimentAssignmentRepository
                .findVisibleAssignmentsForUser(publishId, userId)
                .stream()
                .map(this::toAssignmentDTO)
                .collect(Collectors.toList());
    }

    private Announcement createAnnouncementInternal(PublishedExperiment pe, AnnouncementCreateRequest req) {
        Announcement a = new Announcement();
        a.setPublishedExperiment(pe);
        a.setTitle(req.getTitle());
        a.setContent(req.getContent());
        a.setImportant(req.isImportant());
        a.setCreatedAt(LocalDateTime.now());
        return announcementRepository.save(a);
    }

    private PublishedExperimentResponse toResponse(PublishedExperiment pe,
                                                   List<ExperimentAssignment> assignments) {
        PublishedExperimentResponse r = new PublishedExperimentResponse();
        r.setId(pe.getId());
        r.setExperimentId(pe.getExperiment() != null ? pe.getExperiment().getId() : null);
        r.setPublisherId(pe.getPublisher() != null ? pe.getPublisher().getId() : null);
        r.setStartTime(pe.getStartTime());
        r.setEndTime(pe.getEndTime());
        r.setAllowLateSubmission(pe.isAllowLateSubmit());
        r.setPublishedAt(pe.getPublishedAt());
        r.setAssignments(assignments.stream().map(this::toAssignmentDTO).toList());
        return r;
    }

    private AssignmentDTO toAssignmentDTO(ExperimentAssignment a) {
        AssignmentDTO dto = new AssignmentDTO();
        dto.setId(a.getId());
        dto.setTargetType(a.getTargetType());
        dto.setTargetId(a.getTargetId());
        // dto.setTitle(a.getTitle());
        // dto.setDescription(a.getDescription());
        return dto;
    }

    private AnnouncementResponse toAnnouncementDTO(Announcement a) {
        AnnouncementResponse dto = new AnnouncementResponse();
        dto.setId(a.getId());
        dto.setPublishedExperimentId(a.getPublishedExperiment().getId());
        dto.setTitle(a.getTitle());
        dto.setContent(a.getContent());
        dto.setImportant(a.isImportant());
        dto.setCreatedAt(a.getCreatedAt());
        return dto;
    }

    private Set<Integer> collectTargetUserIds(List<ExperimentAssignment> assignments) {
        Set<Integer> userIds = new HashSet<>();
        assignments.stream()
                .filter(a -> a.getTargetType() == AssignmentTargetType.INDIVIDUAL && a.getTargetId() != null)
                .forEach(a -> userIds.add(a.getTargetId()));
        List<Integer> groupIds = assignments.stream()
                .filter(a -> a.getTargetType() == AssignmentTargetType.GROUP && a.getTargetId() != null)
                .map(ExperimentAssignment::getTargetId)
                .toList();
        if (!groupIds.isEmpty()) {
            groupIds.forEach(gid -> groupMemberRepository.findByGroup_Id(gid)
                    .forEach(gm -> userIds.add(gm.getUser().getId())));
        }
        return userIds;
    }
}