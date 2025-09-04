package com.foxfox.demo.controller;

import com.foxfox.demo.dto.*;
import com.foxfox.demo.service.ExperimentPublishService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 假设前缀
@RestController
@RequestMapping("/api/published-experiments")
@Validated
public class ExperimentPublishController {

    private final ExperimentPublishService service;

    public ExperimentPublishController(ExperimentPublishService service) {
        this.service = service;
    }

    // 发布实验（teacherUserId 可从登录上下文；示例用请求参数）
    @PostMapping
    public PublishedExperimentResponse publish(@RequestParam Integer teacherUserId,
                                               @RequestBody @Valid PublishExperimentRequest req) {
        return service.publish(teacherUserId, req);
    }

    // 新增：下线（提前结束）
    @PatchMapping("/{id}/unpublish")
    public PublishedExperimentResponse unpublish(@PathVariable Integer id, @RequestParam Integer teacherUserId) {
        return service.unpublish(teacherUserId, id);
    }

    @GetMapping("/{id}")
    public PublishedExperimentResponse detail(@PathVariable Integer id) {
        return service.getDetail(id);
    }

    @GetMapping("/running")
    public List<PublishedExperimentResponse> running() {
        return service.listRunning();
    }

    @GetMapping("/finished")
    public List<PublishedExperimentResponse> finished() {
        return service.listFinished();
    }

    @PostMapping("/{id}/announcements")
    public AnnouncementResponse createAnnouncement(@PathVariable Integer id,
                                                   @RequestParam Integer teacherUserId,
                                                   @RequestBody @Valid AnnouncementCreateRequest req) {
        return service.createAnnouncement(id, teacherUserId, req);
    }

    @PutMapping("/announcements/{announcementId}")
    public AnnouncementResponse updateAnnouncement(@PathVariable Integer announcementId,
                                                    @RequestParam Integer teacherUserId,
                                                    @RequestBody AnnouncementUpdateRequest req) {
        return service.updateAnnouncement(announcementId, teacherUserId, req);
    }

    @DeleteMapping("/announcements/{announcementId}")
    public void deleteAnnouncement(@PathVariable Integer announcementId,
                                   @RequestParam Integer teacherUserId) {
        service.deleteAnnouncement(announcementId, teacherUserId);
    }

    @GetMapping("/{id}/announcements")
    public List<AnnouncementResponse> listAnnouncements(@PathVariable Integer id) {
        return service.listAnnouncements(id);
    }

    @GetMapping("/{id}/assignments/visible")
    public List<AssignmentDTO> visibleAssignments(@PathVariable Integer id,
                                                  @RequestParam Integer userId) {
        return service.visibleAssignments(id, userId);
    }

    // 删除已结束的发布实例
    @DeleteMapping("/{id}")
    public void deleteFinished(@PathVariable Integer id, @RequestParam Integer teacherUserId) {
        service.deleteFinished(teacherUserId, id);
    }
}