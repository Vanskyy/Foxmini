package com.foxfox.demo.controller;

import com.foxfox.demo.dto.NotificationResponse;
import com.foxfox.demo.model.User;
import com.foxfox.demo.repository.NotificationRepository;
import com.foxfox.demo.repository.UserRepository;
import com.foxfox.demo.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(NotificationRepository notificationRepository,
                                  NotificationService notificationService,
                                  UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    // 临时: 获取第一个用户 ID，实际需接入认证
    private Integer currentUserId() {
        return userRepository.findAll().stream()
                .map(User::getId)
                .findFirst()
                .orElse(null);
    }

    @GetMapping
    public List<NotificationResponse> list() {
        Integer uid = currentUserId();
        if (uid == null) return List.of();
        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(uid)
                .stream().map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/unread-count")
    public long unreadCount() {
        Integer uid = currentUserId();
        if (uid == null) return 0;
        return notificationRepository.countByUser_IdAndReadFalse(uid);
    }

    @PostMapping("/{id}/read")
    public boolean markRead(@PathVariable Integer id) {
        Integer uid = currentUserId();
        return uid != null && notificationService.markRead(id, uid);
    }
}