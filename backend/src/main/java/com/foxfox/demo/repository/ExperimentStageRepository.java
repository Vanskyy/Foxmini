package com.foxfox.demo.repository;

import com.foxfox.demo.model.ExperimentStage;
import com.foxfox.demo.model.StageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ExperimentStageRepository extends JpaRepository<ExperimentStage, Integer> {



    List<ExperimentStage> findByExperiment_IdOrderBySequenceAsc(Integer experimentId);

    Optional<ExperimentStage> findByExperiment_IdAndSequence(Integer experimentId, Integer sequence);

    long countByExperiment_Id(Integer experimentId);

    boolean existsByExperiment_IdAndTitle(Integer experimentId, String title);

    List<ExperimentStage> findByExperiment_IdAndTypeOrderBySequenceAsc(Integer experimentId, StageType type);

    @Query("select coalesce(max(s.sequence),0) from ExperimentStage s where s.experiment.id = :experimentId")
    int findMaxSequenceByExperimentId(Integer experimentId);
}