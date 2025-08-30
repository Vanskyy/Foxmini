
package com.foxfox.demo.repository;

import com.foxfox.demo.model.EvaluationCriteria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationCriteriaRepository extends JpaRepository<EvaluationCriteria, Integer> {
    boolean existsByStage_Id(Integer stageId);
}