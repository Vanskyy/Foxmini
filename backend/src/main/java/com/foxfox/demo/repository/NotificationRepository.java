package com.foxfox.demo.repository;

import com.foxfox.demo.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByUser_IdOrderByCreatedAtDesc(Integer userId);

    long countByUser_IdAndReadFalse(Integer userId);
}