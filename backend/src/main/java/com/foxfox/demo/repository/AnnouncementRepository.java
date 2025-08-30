package com.foxfox.demo.repository;

import com.foxfox.demo.model.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Integer> {

    List<Announcement> findByPublishedExperiment_IdOrderByCreatedAtDesc(Integer publishId);

    List<Announcement> findTop5ByPublishedExperiment_IdAndImportantTrueOrderByCreatedAtDesc(Integer publishId);
}