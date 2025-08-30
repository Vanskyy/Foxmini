package com.foxfox.demo.repository;

import com.foxfox.demo.model.PublishedExperiment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PublishedExperimentRepository extends JpaRepository<PublishedExperiment, Integer> {

    List<PublishedExperiment> findByExperiment_Id(Integer experimentId);

    @Query("select p from PublishedExperiment p " +
           "where p.startTime <= :now and p.endTime >= :now")
    List<PublishedExperiment> findRunning(LocalDateTime now);

    @Query("select p from PublishedExperiment p " +
           "where p.publisher.id = :teacherId order by p.publishedAt desc")
    List<PublishedExperiment> findByPublisher(Integer teacherId);

    @Query("select p from PublishedExperiment p " +
           "where p.endTime < :now order by p.endTime desc")
    List<PublishedExperiment> findFinished(LocalDateTime now);
}