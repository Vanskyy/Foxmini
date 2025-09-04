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
    private final ExperimentRepository experimentRepository; // 新增：用于校验并更新实验状态
    private final GroupRepository groupRepository; // 新增：用于校验分组归属
    private final StudentAnswerRepository studentAnswerRepository; // 新增

    public ExperimentPublishServiceImpl(PublishedExperimentRepository publishedExperimentRepository,
                                        ExperimentAssignmentRepository experimentAssignmentRepository,
                                        AnnouncementRepository announcementRepository,
                                        GroupMemberRepository groupMemberRepository,
                                        UserRepository userRepository,
                                        NotificationService notificationService,
                                        ExperimentRepository experimentRepository,
                                        GroupRepository groupRepository,
                                        StudentAnswerRepository studentAnswerRepository) { // 新增参数
        this.publishedExperimentRepository = publishedExperimentRepository;
        this.experimentAssignmentRepository = experimentAssignmentRepository;
        this.announcementRepository = announcementRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.experimentRepository = experimentRepository; // 赋值
        this.groupRepository = groupRepository; // 赋值
        this.studentAnswerRepository = studentAnswerRepository; // 赋值
    }

    @Override
    @Transactional
    public PublishedExperimentResponse publish(Integer teacherUserId, PublishExperimentRequest req) {
        if (req.getStartTime().isAfter(req.getEndTime())) {
            throw new IllegalArgumentException("开始时间需早于截止时间");
        }
        if (req.getTargets() == null || req.getTargets().isEmpty()) {
            throw new IllegalArgumentException("至少指定一个分配目标");
        }
        User publisher = userRepository.findById(teacherUserId)
                .orElseThrow(() -> new EntityNotFoundException("发布教师不存在"));
        // 校验实验
        Experiment experiment = experimentRepository.findById(req.getExperimentId())
                .orElseThrow(() -> new EntityNotFoundException("实验不存在"));
        if (experiment.getCreator() == null || !Objects.equals(experiment.getCreator().getId(), teacherUserId)) {
            throw new IllegalArgumentException("无权发布该实验");
        }
        if (experiment.getStatus() != ExperimentStatus.PUBLISHED) {
            experiment.setStatus(ExperimentStatus.PUBLISHED);
            experimentRepository.save(experiment);
        }

        // ====== 重新解释 targets：
        // 需求：
        //  1. INDIVIDUAL: 为每个学生创建一个仅包含该学生的临时分组（若已存在可复用）。
        //  2. ALL: 创建（或复用）一个包含全部学生的分组（由当前教师拥有）。
        //  3. GROUP: 保持不变。
        // 不修改模型结构，只在发布时动态创建 Group 与 GroupMember。

        // 预处理：大写/去重
        List<PublishExperimentRequest.TargetSpec> rawSpecs = req.getTargets();
        Set<String> seenKeys = new HashSet<>();
        List<PublishExperimentRequest.TargetSpec> normalized = new ArrayList<>();
        for (PublishExperimentRequest.TargetSpec s : rawSpecs) {
            if (s.getTargetType() == null) continue;
            String key = s.getTargetType() + "#" + (s.getTargetId() == null ? "" : s.getTargetId());
            if (seenKeys.add(key)) normalized.add(s);
        }

        // 收集所有学生用户（仅当需要 ALL 或 INDIVIDUAL 创建时）
        // 简化：从 userRepository 拉全部 STUDENT 角色（需 Role.STUDENT；若表大需分页，这里假设可以）
        List<User> allStudents = Collections.emptyList();
        boolean needAllStudents = normalized.stream().anyMatch(t -> t.getTargetType() == AssignmentTargetType.ALL || t.getTargetType() == AssignmentTargetType.INDIVIDUAL);
        if (needAllStudents) {
            // 简易实现：拉取全部用户后过滤角色
            allStudents = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.STUDENT)
                    .toList();
        }

        // 用于缓存 单学生临时组：key: studentId -> groupId
        Map<Integer, Integer> singleGroups = new HashMap<>();
        // ALL 全体组缓存（仅创建一次）
        Integer allGroupId = null;

        // 查已有分组（该教师创建的）做复用：命名规则
        String singlePrefix = "__AUTO_SINGLE_"; // __AUTO_SINGLE_{uid}
        String allGroupName = "__AUTO_ALL_STUDENTS__";
        List<Group> teacherGroups = groupRepository.findByCreatedBy_Id(teacherUserId);
        for (Group g : teacherGroups) {
            String name = g.getName();
            if (allGroupName.equals(name)) {
                allGroupId = g.getId();
            } else if (name != null && name.startsWith(singlePrefix)) {
                // 解析 uid
                try {
                    int uid = Integer.parseInt(name.substring(singlePrefix.length()));
                    singleGroups.put(uid, g.getId());
                } catch (NumberFormatException ignored) {}
            }
        }

        // 若需要 ALL 组且不存在则创建并填充所有学生
        if (normalized.stream().anyMatch(t -> t.getTargetType() == AssignmentTargetType.ALL)) {
            if (allGroupId == null) {
                Group g = new Group();
                g.setName(allGroupName);
                g.setDescription("系统自动生成的全体学生分组");
                g.setCreatedBy(publisher);
                groupRepository.save(g);
                allGroupId = g.getId();
                // 批量插入成员
                for (User stu : allStudents) {
                    if (!groupMemberRepository.existsByGroup_IdAndUser_Id(allGroupId, stu.getId())) {
                        GroupMember gm = new GroupMember();
                        gm.setGroup(g);
                        gm.setUser(stu);
                        groupMemberRepository.save(gm);
                    }
                }
            }
        }

        // 为每个 INDIVIDUAL 目标创建或复用单人组
        for (PublishExperimentRequest.TargetSpec spec : normalized) {
            if (spec.getTargetType() == AssignmentTargetType.INDIVIDUAL) {
                Integer uid = spec.getTargetId();
                if (uid == null) throw new IllegalArgumentException("个人目标缺少用户ID");
                // 校验该用户存在且是学生
                User user = allStudents.stream().filter(u -> Objects.equals(u.getId(), uid)).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("用户 " + uid + " 非学生或不存在"));
                Integer gid = singleGroups.get(uid);
                if (gid == null) {
                    Group g = new Group();
                    g.setName(singlePrefix + uid);
                    g.setDescription("自动生成的单学生分组");
                    g.setCreatedBy(publisher);
                    groupRepository.save(g);
                    GroupMember gm = new GroupMember();
                    gm.setGroup(g);
                    gm.setUser(user);
                    groupMemberRepository.save(gm);
                    gid = g.getId();
                    singleGroups.put(uid, gid);
                }
                // 重写为 GROUP 类型引用该 gid
                spec.setTargetType(AssignmentTargetType.GROUP);
                spec.setTargetId(gid);
            } else if (spec.getTargetType() == AssignmentTargetType.ALL) {
                // 把 ALL 也改写为 GROUP 引用 allGroupId
                if (allGroupId == null) {
                    throw new IllegalStateException("全体分组尚未创建");
                }
                spec.setTargetType(AssignmentTargetType.GROUP);
                spec.setTargetId(allGroupId);
            } else if (spec.getTargetType() == AssignmentTargetType.GROUP) {
                // 原样保留，同时验证归属
                Integer gid = spec.getTargetId();
                if (gid == null) throw new IllegalArgumentException("分组目标缺少分组ID");
                Group g = groupRepository.findById(gid).orElseThrow(() -> new IllegalArgumentException("分组不存在: " + gid));
                if (g.getCreatedBy() == null || !Objects.equals(g.getCreatedBy().getId(), teacherUserId)) {
                    throw new IllegalArgumentException("分组 " + gid + " 不属于当前教师");
                }
            }
        }

        // 现在 normalized 全部是 GROUP 类型，去重 group id
        Set<Integer> finalGroupIds = normalized.stream()
                .filter(s -> s.getTargetType() == AssignmentTargetType.GROUP && s.getTargetId() != null)
                .map(PublishExperimentRequest.TargetSpec::getTargetId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (finalGroupIds.isEmpty()) {
            throw new IllegalArgumentException("无有效的分配组");
        }

        PublishedExperiment pe = new PublishedExperiment();
        pe.setPublisher(publisher);
        pe.setStartTime(req.getStartTime());
        pe.setEndTime(req.getEndTime());
        pe.setExperiment(experiment);
        pe.setAllowLateSubmit(req.isAllowLateSubmission());
        pe.setPublishedAt(LocalDateTime.now());
        pe = publishedExperimentRepository.save(pe);

        List<ExperimentAssignment> assignments = new ArrayList<>();
        for (Integer gid : finalGroupIds) {
            ExperimentAssignment a = new ExperimentAssignment();
            a.setPublishedExperiment(pe);
            a.setTargetType(AssignmentTargetType.GROUP);
            a.setTargetId(gid);
            assignments.add(a);
        }
        experimentAssignmentRepository.saveAll(assignments);

        if (req.getInitialAnnouncement() != null) {
            // 设置首条公告创建者为发布教师
            createAnnouncementInternal(publisher, pe, req.getInitialAnnouncement());
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
    public AnnouncementResponse createAnnouncement(Integer publishId, Integer teacherUserId, AnnouncementCreateRequest req) {
        PublishedExperiment pe = publishedExperimentRepository.findById(publishId)
                .orElseThrow(() -> new EntityNotFoundException("发布不存在"));
        User creator = userRepository.findById(teacherUserId)
                .orElseThrow(() -> new EntityNotFoundException("教师不存在"));
        if (pe.getPublisher() == null || !Objects.equals(pe.getPublisher().getId(), teacherUserId)) {
            throw new IllegalArgumentException("无权为该发布创建公告");
        }
        Announcement a = createAnnouncementInternal(creator, pe, req);
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

    @Override
    @Transactional
    public PublishedExperimentResponse unpublish(Integer teacherUserId, Integer publishId) {
        PublishedExperiment pe = publishedExperimentRepository.findById(publishId)
                .orElseThrow(() -> new EntityNotFoundException("发布不存在"));
        if (pe.getPublisher() == null || !Objects.equals(pe.getPublisher().getId(), teacherUserId)) {
            throw new IllegalArgumentException("无权下线该发布");
        }
        // 已结束则直接返回
        LocalDateTime now = LocalDateTime.now();
        if (!pe.getEndTime().isBefore(now)) {
            // 将结束时间设为当前时间的 1 秒前，确保 running 查询不再包含
            pe.setEndTime(now.minusSeconds(1));
            publishedExperimentRepository.save(pe);
        }
        List<ExperimentAssignment> list = experimentAssignmentRepository.findByPublishedExperiment_Id(publishId);
        return toResponse(pe, list);
    }

    @Override
    @Transactional
    public void deleteFinished(Integer teacherUserId, Integer publishId) {
        PublishedExperiment pe = publishedExperimentRepository.findById(publishId)
                .orElseThrow(() -> new EntityNotFoundException("发布不存在"));
        if (pe.getPublisher() == null || !Objects.equals(pe.getPublisher().getId(), teacherUserId)) {
            throw new IllegalArgumentException("无权删除该发布实例");
        }
        LocalDateTime now = LocalDateTime.now();
        if (!pe.getEndTime().isBefore(now)) {
            throw new IllegalStateException("仅已结束的发布实例可删除");
        }
        // 级联 orphanRemoval 已配置: assignments / announcements / (学生答案 referencing publish_id 有外键, 不能直接删) -> 先校验是否存在学生答案
        long answerCount = studentAnswerRepository.countByPublishedExperimentId(publishId);
        if (answerCount > 0) {
            throw new IllegalStateException("该发布实例已有学生作答记录，禁止删除");
        }
        publishedExperimentRepository.delete(pe);
    }

    @Override
    @Transactional
    public AnnouncementResponse updateAnnouncement(Integer announcementId, Integer teacherUserId, AnnouncementUpdateRequest req) {
        Announcement a = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new EntityNotFoundException("公告不存在"));
        if (a.getCreatedBy() == null || !Objects.equals(a.getCreatedBy().getId(), teacherUserId)) {
            throw new IllegalArgumentException("无权修改该公告");
        }
        if (req.getTitle() != null && !req.getTitle().isBlank()) a.setTitle(req.getTitle());
        if (req.getContent() != null && !req.getContent().isBlank()) a.setContent(req.getContent());
        if (req.getImportant() != null) a.setImportant(req.getImportant());
        announcementRepository.save(a);
        return toAnnouncementDTO(a);
    }

    @Override
    @Transactional
    public void deleteAnnouncement(Integer announcementId, Integer teacherUserId) {
        Announcement a = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new EntityNotFoundException("公告不存在"));
        if (a.getCreatedBy() == null || !Objects.equals(a.getCreatedBy().getId(), teacherUserId)) {
            throw new IllegalArgumentException("无权删除该公告");
        }
        announcementRepository.delete(a);
    }

    private Announcement createAnnouncementInternal(User creator, PublishedExperiment pe, AnnouncementCreateRequest req) {
        Announcement a = new Announcement();
        a.setPublishedExperiment(pe);
        a.setTitle(req.getTitle());
        a.setContent(req.getContent());
        a.setImportant(req.isImportant());
        a.setCreatedAt(LocalDateTime.now());
        a.setCreatedBy(creator);
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
        dto.setCreatedByUserId(a.getCreatedBy() != null ? a.getCreatedBy().getId() : null);
        return dto;
    }

    private Set<Integer> collectTargetUserIds(List<ExperimentAssignment> assignments) {
        Set<Integer> userIds = new HashSet<>();
        // assignments 已统一转为 GROUP，保留健壮性处理
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