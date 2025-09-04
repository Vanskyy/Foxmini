package com.foxfox.demo.service.impl;

import com.foxfox.demo.model.Announcement;
import com.foxfox.demo.model.Notification;
import com.foxfox.demo.model.NotificationType;
import com.foxfox.demo.model.PublishedExperiment;
import com.foxfox.demo.model.User;
import com.foxfox.demo.repository.NotificationRepository;
import com.foxfox.demo.repository.UserRepository;
import com.foxfox.demo.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void notifyExperimentPublished(PublishedExperiment publishedExperiment, Collection<Integer> userIds) {
        if (publishedExperiment == null || userIds == null || userIds.isEmpty()) return;
        String expTitle = publishedExperiment.getExperiment().getTitle();
        List<Notification> list = userIds.stream()
                .filter(Objects::nonNull)
                .map(uid -> {
                    User ref = userRepository.getReferenceById(uid);
                    Notification n = new Notification()
                            .setTitle("实验发布: " + expTitle)
                            .setContent("实验【" + expTitle + "】已发布，请按时完成。")
                            .setType(NotificationType.ASSIGNMENT)
                            .setRelatedId(publishedExperiment.getId());
                    n.setUser(ref);
                    return n;
                }).collect(Collectors.toList());
        notificationRepository.saveAll(list);
    }

    @Override
    @Transactional
    public void notifyAnnouncement(Announcement announcement, Collection<Integer> userIds) {
        if (announcement == null || userIds == null || userIds.isEmpty()) return;
        List<Notification> list = userIds.stream()
                .filter(Objects::nonNull)
                .map(uid -> {
                    User ref = userRepository.getReferenceById(uid);
                    Notification n = new Notification()
                            .setTitle("公告: " + announcement.getTitle())
                            .setContent(announcement.getContent())
                            .setType(NotificationType.ANNOUNCEMENT)
                            .setRelatedId(announcement.getId());
                    n.setUser(ref);
                    return n;
                }).collect(Collectors.toList());
        notificationRepository.saveAll(list);
    }

    @Override
    @Transactional
    public boolean markRead(Integer notificationId, Integer userId) {
        return notificationRepository.findById(notificationId)
                .filter(n -> n.getUser().getId().equals(userId) && !n.isRead())
                .map(n -> {
                    n.setRead(true);
                    notificationRepository.save(n);
                    return true;
                }).orElse(false);
    }

    @Override
    @Transactional
    public long markAllRead(Integer userId) {
        List<Notification> list = notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId)
                .stream().filter(n -> !n.isRead()).collect(Collectors.toList());
        list.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(list);
        return list.size();
    }
}