package com.foxfox.demo.service;

import com.foxfox.demo.model.Announcement;
import com.foxfox.demo.model.PublishedExperiment;

import java.util.Collection;

public interface NotificationService {

    void notifyExperimentPublished(PublishedExperiment publishedExperiment, Collection<Integer> userIds);

    void notifyAnnouncement(Announcement announcement, Collection<Integer> userIds);

    boolean markRead(Integer notificationId, Integer userId);

    // 新增：全部标记已读，返回被更新的数量
    long markAllRead(Integer userId);
}