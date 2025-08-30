package com.foxfox.demo.repository;

import com.foxfox.demo.model.Experiment;
import com.foxfox.demo.model.ExperimentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExperimentRepository extends JpaRepository<Experiment, Integer> {

    Page<Experiment> findByCreator_Id(Integer creatorId, Pageable pageable);

    Page<Experiment> findByStatus(ExperimentStatus status, Pageable pageable);

    Page<Experiment> findByCreator_IdAndStatus(Integer creatorId, ExperimentStatus status, Pageable pageable);
    // 新增：教师ID + 标题关键词
    Page<Experiment> findByCreator_IdAndTitleContainingIgnoreCase(Integer creatorId, String keyword, Pageable pageable);

    // 新增：教师ID + 状态 + 标题关键词
    Page<Experiment> findByCreator_IdAndStatusAndTitleContainingIgnoreCase(Integer creatorId,
                                                                           ExperimentStatus status,
                                                                           String keyword,
                                                                           Pageable pageable);


    List<Experiment> findTop10ByCreator_IdOrderByCreatedAtDesc(Integer creatorId);

    boolean existsByIdAndCreator_Id(Integer id, Integer creatorId);

    long countByCreator_Id(Integer creatorId);

    Page<Experiment> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"stages", "resources"})
    Optional<Experiment> findWithStagesAndResourcesById(Integer id);
}