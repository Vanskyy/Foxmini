package com.foxfox.demo.repository;

import com.foxfox.demo.model.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Integer> {

    // 唯一组合（user + stage + publish）
    Optional<StudentAnswer> findByUserIdAndStageIdAndPublishedExperimentId(Integer userId,
                                                                           Integer stageId,
                                                                           Integer publishedExperimentId);

    // 获取某学生在某发布实验下所有阶段的答案
    List<StudentAnswer> findByUserIdAndPublishedExperimentId(Integer userId, Integer publishedExperimentId);

    // 获取某学生某阶段的最终提交（finalSubmit=true）
    Optional<StudentAnswer> findByUserIdAndStageIdAndPublishedExperimentIdAndFinalSubmitTrue(Integer userId,
                                                                                             Integer stageId,
                                                                                             Integer publishedExperimentId);

    // 获取某发布实验下所有已最终提交的答案
    List<StudentAnswer> findByPublishedExperimentIdAndFinalSubmitTrue(Integer publishedExperimentId);

    List<StudentAnswer> findByUserId(Integer userId);

    Optional<StudentAnswer> findByStage_IdAndUser_IdAndPublishedExperiment_Id(Integer stageId,
                                                                              Integer userId,
                                                                              Integer publishId);

    long countByPublishedExperimentId(Integer publishedExperimentId);
}