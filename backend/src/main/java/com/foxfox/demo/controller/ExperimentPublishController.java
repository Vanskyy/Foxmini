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
                                                   @RequestBody @Valid AnnouncementCreateRequest req) {
        return service.createAnnouncement(id, req);
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
}