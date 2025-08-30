package com.foxfox.demo.repository;

import com.foxfox.demo.model.ExperimentResource;
import com.foxfox.demo.model.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExperimentResourceRepository extends JpaRepository<ExperimentResource, Integer> {

    List<ExperimentResource> findByExperiment_Id(Integer experimentId);

    List<ExperimentResource> findByExperiment_IdAndType(Integer experimentId, ResourceType type);

    long countByExperiment_Id(Integer experimentId);

    void deleteByExperiment_IdAndId(Integer experimentId, Integer id);

    boolean existsByExperiment_IdAndName(Integer experimentId, String name);
}